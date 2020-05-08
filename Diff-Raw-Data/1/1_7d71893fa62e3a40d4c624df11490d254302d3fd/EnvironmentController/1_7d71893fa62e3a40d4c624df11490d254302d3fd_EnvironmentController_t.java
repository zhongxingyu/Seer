 /**
  * Copyright (c) 2012 Aon eSolutions
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.aon.esolutions.appconfig.web.controller;
 
 import java.security.Key;
 import java.security.KeyPair;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.aon.esolutions.appconfig.model.Application;
 import org.aon.esolutions.appconfig.model.Environment;
 import org.aon.esolutions.appconfig.model.PrivateKeyHolder;
 import org.aon.esolutions.appconfig.repository.ApplicationRepository;
 import org.aon.esolutions.appconfig.repository.EnvironmentRepository;
 import org.aon.esolutions.appconfig.repository.PrivateKeyRepository;
 import org.aon.esolutions.appconfig.util.AvailableUsersAndRolesProvider;
 import org.aon.esolutions.appconfig.util.InheritanceUtil;
 import org.aon.esolutions.appconfig.util.RSAEncryptUtil;
 import org.aon.esolutions.appconfig.util.UpdateUtility;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.access.AccessDeniedException;
 import org.springframework.security.acls.model.NotFoundException;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.context.request.RequestAttributes;
 import org.springframework.web.context.request.RequestContextHolder;
 
 import com.crygier.spring.util.web.MimeTypeViewResolver.ResponseMapping;
 
 @Controller
 @RequestMapping("/application/{applicationName}/environment")
 public class EnvironmentController {
 	
 	@Autowired private ApplicationRepository applicationRepository;	
 	@Autowired private EnvironmentRepository environmentRepository;	
 	@Autowired private PrivateKeyRepository privateKeyRepository;
 	@Autowired private UpdateUtility updateUtility;
 	@Autowired private InheritanceUtil inheritanceUtil;
 	@Autowired(required = false) private AvailableUsersAndRolesProvider usersAndRolesProvider;
 	
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	@ResponseMapping("environmentDetails")
 	public Set<Environment> getAllEnvironments(@PathVariable String applicationName) {
 		return environmentRepository.getAllEnvironmentsForApplication(applicationName);
 	}
 
 	@RequestMapping(value = "/{environmentName}", method = RequestMethod.GET)
 	@ResponseMapping("environmentDetails")
 	public Environment getEnvironment(@PathVariable String applicationName, @PathVariable String environmentName) {
 		Environment env = environmentRepository.getEnvironment(applicationName, environmentName);
 		if (env == null)
 			throw new NotFoundException("Can not find envioronment");
 		
 		populatePrivateKey(env);
 
 		RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
 			
 		if (attributes != null) {
 			attributes.setAttribute("allVariables", inheritanceUtil.getVariablesForEnvironment(env), RequestAttributes.SCOPE_REQUEST);
 			
 			if (usersAndRolesProvider != null) {
 				attributes.setAttribute("availableUsers", usersAndRolesProvider.getAvailableUsers(), RequestAttributes.SCOPE_REQUEST);
 				attributes.setAttribute("availableRoles", usersAndRolesProvider.getAvailableRoles(), RequestAttributes.SCOPE_REQUEST);
 			}
 		}
 				
 		return env;
 	}	
 	
 	@RequestMapping(value = "/{environmentName}", method = RequestMethod.POST)
 	public Environment updateEnvironmentDetails(@PathVariable String applicationName, @PathVariable String environmentName, Environment updatedEnv) {
 		Environment readEnv = updateUtility.getEnvironmentForWrite(applicationName, environmentName);
 		readEnv.setName(updatedEnv.getName());
 		readEnv.setPermittedUsers(updatedEnv.getPermittedUsers());
 		readEnv.setPermittedRoles(updatedEnv.getPermittedRoles());
 		readEnv.setVisibleToAll(updatedEnv.isVisibleToAll());
 		
 		return updateUtility.saveEnvironment(readEnv);
 	}
 	
 	@Transactional
 	@RequestMapping(value = "/{environmentName}", method = RequestMethod.PUT)	
 	public Environment addEnvironment(@PathVariable String applicationName, @PathVariable String environmentName, @RequestParam("parentId") String parentId) throws Exception {
 		Application app = applicationRepository.findByName(applicationName);
 		Environment parent = null;
 		
 		if (parentId != null)
 			parent = environmentRepository.findOne(Long.parseLong(parentId));
 		
 		Environment newEnv = new Environment();
 		newEnv.setName(environmentName);
 		newEnv.setParent(parent);
 		app.addEnvironment(newEnv);
 		
 		updateKeys(newEnv);
 		newEnv = environmentRepository.save(newEnv);
 		
 		return newEnv;
 	}
 	
 	@Transactional
 	@RequestMapping(value = "/{environmentName}/keys", method = RequestMethod.POST)
 	public Map<String, String> updateKeys(@PathVariable String applicationName, @PathVariable String environmentName) throws Exception {
 		Environment env = getEnvironment(applicationName, environmentName);
 		
 		Map<String, String> answer = updateKeys(env);
 		environmentRepository.save(env);
 		
 		return answer;
 	}
 	
 	@RequestMapping(value = "/{environmentName}/keys", method = RequestMethod.GET)
 	public Map<String, String> getKeys(@PathVariable String applicationName, @PathVariable String environmentName) throws Exception {
 		Environment env = getEnvironment(applicationName, environmentName);
 		
 		Map<String, String> answer = new HashMap<String, String>();
 		answer.put("public", env.getPublicKey());
 		answer.put("private", env.getPrivateKeyHolder().getPrivateKey());
 		
 		return answer;
 	}
 	
 	private Map<String, String> updateKeys(Environment env) throws Exception {
 		Map<String, String> answer = new HashMap<String, String>();
 
 		if (env != null) {
 			// First, get private key - Performs ACL Checking
 			PrivateKeyHolder holder = null;
 			if (env.getPrivateKeyHolder() != null)
 				holder = privateKeyRepository.findOne(env.getPrivateKeyHolder().getId());
 			
 			if (holder != null) {
 				Key key = RSAEncryptUtil.getPrivateKeyFromString(holder.getPrivateKey());
 				for (String encryptedVariable : env.getEncryptedVariables()) {
 					String encryptedValue = env.get(encryptedVariable);
 					if (encryptedValue != null) {
 						String decryptedValue = RSAEncryptUtil.decrypt(encryptedValue, key);
 						env.put(encryptedVariable, decryptedValue);
 					}
 				}
 			} else {
 				holder = new PrivateKeyHolder();
 				env.setPrivateKeyHolder(holder);
				holder.setEnvironment(env);
 			}
 
 			// Generate the new keys
 			KeyPair keyPair = RSAEncryptUtil.generateKey();
 			env.setPublicKey(RSAEncryptUtil.getKeyAsString(keyPair.getPublic()));
 			holder.setPrivateKey(RSAEncryptUtil.getKeyAsString(keyPair.getPrivate()));
 			
 			// Re-encrypt with the new values
 			for (String encryptedVariable : env.getEncryptedVariables()) {
 				String decryptedValue = env.get(encryptedVariable);
 				if (decryptedValue != null) {
 					String encryptedValue = RSAEncryptUtil.encrypt(decryptedValue, keyPair.getPublic());
 					env.put(encryptedVariable, encryptedValue);
 				}
 			}
 			
 			updateUtility.savePrivateKeyHolder(holder);
 			
 			answer.put("publicKey", env.getPublicKey());
 			answer.put("privateKey", holder.getPrivateKey());
 		}
 		
 		return answer;
 	}
 	
 	@Transactional
 	@RequestMapping(value = "/{environmentName}/variable/{existingKey:.*}", method = RequestMethod.POST)
 	public void updateVariable(@PathVariable String applicationName, @PathVariable String environmentName, 
 			                  @PathVariable String existingKey, @RequestParam("key") String updatedKey, @RequestParam("value") String updatedValue) {
 		Environment env = getEnvironment(applicationName, environmentName);
 		if (env != null) {
 			env.remove(existingKey);
 			env.put(updatedKey.trim(), updatedValue);
 			env.getEncryptedVariables().remove(existingKey);
 			
 			environmentRepository.save(env);
 		}
 	}
 	
 	@Transactional
 	@RequestMapping(value = "/{environmentName}/variable/{existingKey}/encrypt", method = RequestMethod.POST)
 	public  Map<String, String> encryptVariable(@PathVariable String applicationName, @PathVariable String environmentName, @PathVariable String existingKey) throws Exception {
 		Environment env = getEnvironment(applicationName, environmentName);
 		if (env != null) {
 			String existingValue = env.get(existingKey);
 			Key key = RSAEncryptUtil.getPublicKeyFromString(env.getPublicKey());
 			String encryptedValue = RSAEncryptUtil.encrypt(existingValue, key);
 			env.put(existingKey, encryptedValue);
 			env.addEncryptedVariable(existingKey);
 			
 			environmentRepository.save(env);
 			
 			Map<String, String> answer = new HashMap<String, String>();
 			answer.put("encryptedValue", encryptedValue);
 			return answer;
 		}
 		
 		return null;
 	}
 	
 	@Transactional
 	@RequestMapping(value = "/{environmentName}/variable/{existingKey}/decrypt", method = RequestMethod.POST)
 	public  Map<String, String> decryptVariable(@PathVariable String applicationName, @PathVariable String environmentName, @PathVariable String existingKey) throws Exception {
 		Environment env = getEnvironment(applicationName, environmentName);
 		if (env != null) {
 			PrivateKeyHolder holder = privateKeyRepository.findOne(env.getPrivateKeyHolder().getId());
 			
 			String existingValue = env.get(existingKey);
 			Key key = RSAEncryptUtil.getPrivateKeyFromString(holder.getPrivateKey());
 			String decryptedValue = RSAEncryptUtil.decrypt(existingValue, key);
 			env.put(existingKey, decryptedValue);
 			env.getEncryptedVariables().remove(existingKey);
 			
 			if (env.getEncryptedVariables().isEmpty())
 				env.addEncryptedVariable("____dummy_key_bug_work_around___");		// bug workaround
 			
 			environmentRepository.save(env);
 			
 			Map<String, String> answer = new HashMap<String, String>();
 			answer.put("decryptedValue", decryptedValue);
 			return answer;
 		}
 		
 		return null;
 	}
 	
 	@Transactional
 	@RequestMapping(value = "/{environmentName}/variable/{existingKey:.*}", method = RequestMethod.DELETE)
 	public void deleteVariable(@PathVariable String applicationName, @PathVariable String environmentName, @PathVariable String existingKey) {
 		Environment env = getEnvironment(applicationName, environmentName);
 		if (env != null) {
 			env.remove(existingKey);
 			env.getEncryptedVariables().remove(existingKey);
 			
 			environmentRepository.save(env);
 		}
 	}
 	
 	private void populatePrivateKey(Environment env) {
 		try {
 			PrivateKeyHolder holder = privateKeyRepository.findOne(env.getPrivateKeyHolder().getId());
 			env.setPrivateKeyHolder(holder);
 		} catch (AccessDeniedException e) {
 			// This is okay, they just can't see partial data
 		}
 	}
 }
