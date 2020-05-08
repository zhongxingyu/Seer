 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: AgentsModelImpl.java,v 1.8 2008-04-22 00:23:15 veiming Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.console.agentconfig.model;
 
 import com.iplanet.sso.SSOException;
 import com.iplanet.sso.SSOToken;
 import com.sun.identity.cli.CLIConstants;
 import com.sun.identity.common.configuration.AgentConfiguration;
 import com.sun.identity.common.configuration.ConfigurationException;
 import com.sun.identity.console.base.model.AMConsoleException;
 import com.sun.identity.console.base.model.AMFormatUtils;
 import com.sun.identity.console.base.model.AMModelBase;
 import com.sun.identity.idm.AMIdentity;
 import com.sun.identity.idm.AMIdentityRepository;
 import com.sun.identity.idm.IdConstants;
 import com.sun.identity.idm.IdRepoException;
 import com.sun.identity.idm.IdSearchResults;
 import com.sun.identity.idm.IdSearchControl;
 import com.sun.identity.idm.IdType;
 import com.sun.identity.idm.IdUtils;
 import com.sun.identity.sm.SMSException;
 import java.net.MalformedURLException;
 import java.text.MessageFormat;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 import javax.servlet.http.HttpServletRequest;
 
 /* - LOG COMPLETE - */
 
 /**
  * Agent Configuration model impplementation.
  */
 public class AgentsModelImpl
     extends AMModelBase
     implements AgentsModel
 {
     /**
      * Creates an instance of this model implementation class.
      *
      * @param req HTTP Servlet Request.
      * @param map Map of setting properties.
      */
     public AgentsModelImpl(HttpServletRequest req, Map map) {
         super(req, map);
     }
 
     /**
      * Returns agent names.
      *
      * @param realmName Realm where agents reside.
      * @param setTypes Agent Types.
      * @param pattern Search Pattern.
      * @param results Set to contains the results.
      * @return error code.
      * @throws AMConsoleException if result cannot be returned.
      */
     public int getAgentNames(
         String realmName,
         Set setTypes,
         String pattern,
         Set results
     ) throws AMConsoleException {
         int sizeLimit = getSearchResultLimit();
         int timeLimit = getSearchTimeOutLimit();
         String[] params = {realmName, setTypes.toString(), pattern,
             Integer.toString(sizeLimit), Integer.toString(timeLimit)};
         
         try {
             IdSearchControl idsc = new IdSearchControl();
             idsc.setMaxResults(sizeLimit);
             idsc.setTimeOut(timeLimit);
             idsc.setAllReturnAttributes(false);
             
             logEvent("ATTEMPT_SEARCH_AGENT", params);
             
             AMIdentityRepository repo = new AMIdentityRepository(
                 getUserSSOToken(), realmName);
             IdSearchResults isr = repo.searchIdentities(
                 IdType.AGENTONLY, pattern, idsc);
             Set res = isr.getSearchResults();
             
             if ((res != null) && !res.isEmpty()) {
                 for (Iterator i = res.iterator(); i.hasNext(); ) {
                     AMIdentity amid = (AMIdentity)i.next();
                     if (matchType(amid, setTypes)) {
                         results.add(amid);
                     }
                 }
             }
             
             logEvent("SUCCEED_SEARCH_AGENT", params);
             return isr.getErrorCode();
         } catch (IdRepoException e) {
             String[] paramsEx = {realmName, setTypes.toString(), pattern,
                 Integer.toString(sizeLimit), Integer.toString(timeLimit),
                 getErrorString(e)};
             logEvent("EXCEPTION_SEARCH_AGENT", paramsEx);
             if (debug.warningEnabled()) {
                 debug.warning("AgentsModelImpl.getAgentNames " + 
                     getErrorString(e));
             }
             throw new AMConsoleException("no.properties");
         } catch (SSOException e) {
             String[] paramsEx = {realmName, setTypes.toString(), pattern,
                 Integer.toString(sizeLimit), Integer.toString(timeLimit),
                 getErrorString(e)};
             logEvent("EXCEPTION_SEARCH_AGENT", paramsEx);
             debug.warning("AgentsModelImpl.getAgentNames ", e);
             throw new AMConsoleException(getErrorString(e));
         }
     }
     
     /**
      * Returns agent group names.
      *
      * @param realmName Realm where agent groups reside.
      * @param setTypes Agent Types.
      * @param pattern Search Pattern.
      * @param results Set to contains the results.
      * @return error code.
      * @throws AMConsoleException if result cannot be returned.
      */
     public int getAgentGroupNames(
         String realmName,
         Set setTypes,
         String pattern,
         Set results
     ) throws AMConsoleException {
         int sizeLimit = getSearchResultLimit();
         int timeLimit = getSearchTimeOutLimit();
         String[] params = {realmName, setTypes.toString(), pattern,
             Integer.toString(sizeLimit), Integer.toString(timeLimit)};
         
         try {
             IdSearchControl idsc = new IdSearchControl();
             idsc.setMaxResults(sizeLimit);
             idsc.setTimeOut(timeLimit);
             idsc.setAllReturnAttributes(false);
             logEvent("ATTEMPT_SEARCH_AGENT_GROUP", params);
             
             AMIdentityRepository repo = new AMIdentityRepository(
                 getUserSSOToken(), realmName);
             IdSearchResults isr = repo.searchIdentities(
                 IdType.AGENTGROUP, pattern, idsc);
             Set res = isr.getSearchResults();
             
             if ((res != null) && !res.isEmpty()) {
                 for (Iterator i = res.iterator(); i.hasNext(); ) {
                     AMIdentity amid = (AMIdentity)i.next();
                     if (matchType(amid, setTypes)) {
                         results.add(amid);
                     }
                 }
             }
             
             logEvent("SUCCEED_SEARCH_AGENT_GROUP", params);
             return isr.getErrorCode();
         } catch (IdRepoException e) {
             String[] paramsEx = {realmName, setTypes.toString(), pattern,
                 Integer.toString(sizeLimit), Integer.toString(timeLimit),
                 getErrorString(e)};
             logEvent("EXCEPTION_SEARCH_AGENT_GROUP", paramsEx);
             if (debug.warningEnabled()) {
                 debug.warning("AgentsModelImpl.getAgentGroupNames " + 
                     getErrorString(e));
             }
             throw new AMConsoleException("no.properties");
         } catch (SSOException e) {
             String[] paramsEx = {realmName, setTypes.toString(), pattern,
                 Integer.toString(sizeLimit), Integer.toString(timeLimit),
                 getErrorString(e)};
             logEvent("EXCEPTION_SEARCH_AGENT_GROUP", paramsEx);
             debug.warning("AgentsModelImpl.getAgentGroupNames ", e);
             throw new AMConsoleException(getErrorString(e));
         }
     }
     
     
     private boolean matchType(AMIdentity amid, Set setTypes)
         throws AMConsoleException {
         return setTypes.contains(getAgentType(amid));
     }
 
     /**
      * Returns agent type.
      * @param amid Identity Object.
      * @return agent type.
      * @throws AMConsoleException if agent type cannot be obtained.
      */
     public String getAgentType(AMIdentity amid) 
         throws AMConsoleException {
         try {
             Map attrValues = amid.getAttributes();
             Set set = (Set) attrValues.get(CLIConstants.ATTR_NAME_AGENT_TYPE);
             return ((set != null) && !set.isEmpty()) ? 
                 (String) set.iterator().next() : "";
         } catch (IdRepoException ex) {
             throw new AMConsoleException(ex.getMessage());
         } catch (SSOException ex) {
             throw new AMConsoleException(ex.getMessage());
         }
     }
     
     /**
      * Creates agent.
      *
      * @param realmName Realm where agent resides.
      * @param name Name of agent.
      * @param type Type of agent.
      * @param password Password of agent.
      * @param choice Choice of type of configuartion.
      * @throws AMConsoleException if agent cannot be created.
      */
     public void createAgent(
         String realmName,
         String name,
         String type,
         String password,
         String choice
     ) throws AMConsoleException {
         String[] params = {realmName, name, type};
 
         try {
             logEvent("ATTEMPT_CREATE_AGENT", params);
             Map map = AgentConfiguration.getDefaultValues(type);
             Set set = new HashSet(2);
             map.put(AgentConfiguration.ATTR_NAME_PWD, set);
             set.add(password);
             if ((choice != null) && (choice.equalsIgnoreCase(
                     AgentConfiguration.VAL_CONFIG_REPO_LOCAL))) 
             {
                 Set newset = new HashSet(2);
                 newset.add(AgentConfiguration.VAL_CONFIG_REPO_LOCAL);
                 map.put(AgentConfiguration.ATTR_CONFIG_REPO, newset);
             }
             AgentConfiguration.createAgent(getUserSSOToken(), realmName, name, 
                 type, map);
             logEvent("SUCCEED_CREATE_AGENT", params);
         } catch (ConfigurationException e) {
             String[] paramsEx = {realmName, name, type, getErrorString(e)};
             logEvent("EXCEPTION_CREATE_AGENT", paramsEx);
             debug.warning("AgentsModelImpl.createAgent", e);
             throw new AMConsoleException(getErrorString(e));
         } catch (SSOException e) {
             String[] paramsEx = {realmName, name, type, getErrorString(e)};
             logEvent("EXCEPTION_CREATE_AGENT", paramsEx);
             debug.warning("AgentsModelImpl.createAgent", e);
             throw new AMConsoleException(getErrorString(e));
         } catch (IdRepoException e) {
             String[] paramsEx = {realmName, name, type, getErrorString(e)};
             logEvent("EXCEPTION_CREATE_AGENT", paramsEx);
             debug.warning("AgentsModelImpl.createAgent", e);
             throw new AMConsoleException(getErrorString(e));
         } catch (SMSException e) {
             String[] paramsEx = {realmName, name, type, getErrorString(e)};
             logEvent("EXCEPTION_CREATE_AGENT", paramsEx);
             debug.warning("AgentsModelImpl.createAgent", e);
             throw new AMConsoleException(getErrorString(e));
         }
     }
 
     /**
      * Creates agent.
      *
      * @param realmName Realm where agent resides.
      * @param name Name of agent.
      * @param type Type of agent.
      * @param password Password of agent.
      * @param serverURL Server URL.
      * @param agentURL Agent URL.
      * @throws AMConsoleException if agent cannot be created.
      */
     public void createAgent(
         String realmName,
         String name,
         String type,
         String password,
         String serverURL,
         String agentURL
     ) throws AMConsoleException {
         String[] params = {realmName, name, type};
 
         try {
             logEvent("ATTEMPT_CREATE_AGENT", params);
             Map map = AgentConfiguration.getDefaultValues(type);
             Set set = new HashSet(2);
             map.put(AgentConfiguration.ATTR_NAME_PWD, set);
             set.add(password);
             AgentConfiguration.createAgent(getUserSSOToken(), realmName, name, 
                 type, map, serverURL, agentURL);
             logEvent("SUCCEED_CREATE_AGENT", params);
         } catch (ConfigurationException e) {
             String[] paramsEx = {realmName, name, type, getErrorString(e)};
             logEvent("EXCEPTION_CREATE_AGENT", paramsEx);
             debug.warning("AgentsModelImpl.createAgent", e);
             throw new AMConsoleException(getErrorString(e));
         } catch (MalformedURLException e) {
             String[] paramsEx = {realmName, name, type, getErrorString(e)};
             logEvent("EXCEPTION_CREATE_AGENT", paramsEx);
             debug.warning("AgentsModelImpl.createAgent", e);
             throw new AMConsoleException(getErrorString(e));
         } catch (SSOException e) {
             String[] paramsEx = {realmName, name, type, getErrorString(e)};
             logEvent("EXCEPTION_CREATE_AGENT", paramsEx);
             debug.warning("AgentsModelImpl.createAgent", e);
             throw new AMConsoleException(getErrorString(e));
         } catch (IdRepoException e) {
             String[] paramsEx = {realmName, name, type, getErrorString(e)};
             logEvent("EXCEPTION_CREATE_AGENT", paramsEx);
             debug.warning("AgentsModelImpl.createAgent", e);
             throw new AMConsoleException(getErrorString(e));
         } catch (SMSException e) {
             String[] paramsEx = {realmName, name, type, getErrorString(e)};
             logEvent("EXCEPTION_CREATE_AGENT", paramsEx);
             debug.warning("AgentsModelImpl.createAgent", e);
             throw new AMConsoleException(getErrorString(e));
         }
     }
 
     /**
      * Creates agent group.
      *
      * @param realmName Realm where agent group resides.
      * @param name Name of agent group.
      * @param type Type of agent group.
      * @param serverURL Server URL.
      * @param agentURL Agent URL.
      * @throws AMConsoleException if agent group cannot be created.
      */
     public void createAgentGroup(
         String realmName,
         String name,
         String type,
         String serverURL,
         String agentURL
     ) throws AMConsoleException {
         String[] params = {realmName, name, type};
 
         try {
             logEvent("ATTEMPT_CREATE_AGENT_GROUP", params);
             AgentConfiguration.createAgentGroup(getUserSSOToken(), realmName,
                 name, type,
                 AgentConfiguration.getDefaultValues(type), serverURL, agentURL);
             logEvent("SUCCEED_CREATE_AGENT_GROUP", params);
         } catch (MalformedURLException e) {
             String[] paramsEx = {realmName, name, type, getErrorString(e)};
             logEvent("EXCEPTION_CREATE_AGENT_GROUP", paramsEx);
             debug.warning("AgentsModelImpl.createAgentGroup", e);
             throw new AMConsoleException(getErrorString(e));
         } catch (ConfigurationException e) {
             String[] paramsEx = {realmName, name, type, getErrorString(e)};
             logEvent("EXCEPTION_CREATE_AGENT_GROUP", paramsEx);
             debug.warning("AgentsModelImpl.createAgentGroup", e);
             throw new AMConsoleException(getErrorString(e));
         } catch (SSOException e) {
             String[] paramsEx = {realmName, name, type, getErrorString(e)};
             logEvent("EXCEPTION_CREATE_AGENT_GROUP", paramsEx);
             debug.warning("AgentsModelImpl.createAgentGroup", e);
             throw new AMConsoleException(getErrorString(e));
         } catch (IdRepoException e) {
             String[] paramsEx = {realmName, name, type, getErrorString(e)};
             logEvent("EXCEPTION_CREATE_AGENT_GROUP", paramsEx);
             debug.warning("AgentsModelImpl.createAgentGroup", e);
             throw new AMConsoleException(getErrorString(e));
         } catch (SMSException e) {
             String[] paramsEx = {realmName, name, type, getErrorString(e)};
             logEvent("EXCEPTION_CREATE_AGENT_GROUP", paramsEx);
             debug.warning("AgentsModelImpl.createAgentGroup", e);
             throw new AMConsoleException(getErrorString(e));
         }
     }
 
     /**
      * Creates agent group.
      *
      * @param realmName Realm where agent group resides.
      * @param name Name of agent group.
      * @param type Type of agent group.
      * @throws AMConsoleException if agent group cannot be created.
      */
     public void createAgentGroup(String realmName, String name, String type) 
         throws AMConsoleException {
         String[] params = {realmName, name, type};
 
         try {
             logEvent("ATTEMPT_CREATE_AGENT_GROUP", params);
             AgentConfiguration.createAgentGroup(getUserSSOToken(), realmName,
                 name, type, AgentConfiguration.getDefaultValues(type));
             logEvent("SUCCEED_CREATE_AGENT_GROUP", params);
         } catch (ConfigurationException e) {
             String[] paramsEx = {realmName, name, type, getErrorString(e)};
             logEvent("EXCEPTION_CREATE_AGENT_GROUP", paramsEx);
             debug.warning("AgentsModelImpl.createAgentGroup", e);
             throw new AMConsoleException(getErrorString(e));
         } catch (SSOException e) {
             String[] paramsEx = {realmName, name, type, getErrorString(e)};
             logEvent("EXCEPTION_CREATE_AGENT_GROUP", paramsEx);
             debug.warning("AgentsModelImpl.createAgentGroup", e);
             throw new AMConsoleException(getErrorString(e));
         } catch (IdRepoException e) {
             String[] paramsEx = {realmName, name, type, getErrorString(e)};
             logEvent("EXCEPTION_CREATE_AGENT_GROUP", paramsEx);
             debug.warning("AgentsModelImpl.createAgentGroup", e);
             throw new AMConsoleException(getErrorString(e));
         } catch (SMSException e) {
             String[] paramsEx = {realmName, name, type, getErrorString(e)};
             logEvent("EXCEPTION_CREATE_AGENT_GROUP", paramsEx);
             debug.warning("AgentsModelImpl.createAgentGroup", e);
             throw new AMConsoleException(getErrorString(e));
         }
     }
     
     /**
      * Deletes agents.
      *
      * @param realmName Realm where agent resides.
      * @param agents Set of agent names to be deleted.
      * @throws AMConsoleException if agents cannot be deleted.
      */
     public void deleteAgents(String realmName, Set agents) 
         throws AMConsoleException {
         if ((agents != null) && !agents.isEmpty()) {
             String idNames = AMFormatUtils.toCommaSeparatedFormat(agents);
             String[] params = {realmName, idNames};
             logEvent("ATTEMPT_DELETE_AGENT", params);
 
             try {
                 AMIdentityRepository repo = new AMIdentityRepository(
                     getUserSSOToken(), realmName);
                 repo.deleteIdentities(getAMIdentity(agents));
                 logEvent("SUCCEED_DELETE_AGENT", params);
             } catch (IdRepoException e) {
                 String[] paramsEx = {realmName, idNames, getErrorString(e)};
                 logEvent("EXCEPTION_DELETE_AGENT", paramsEx);
                 throw new AMConsoleException(getErrorString(e));
             } catch (SSOException e) {
                 String[] paramsEx = {realmName, idNames, getErrorString(e)};
                 logEvent("EXCEPTION_DELETE_AGENT", paramsEx);
                 throw new AMConsoleException(getErrorString(e));
             }
         }
     }
     
     /**
      * Deletes agent groups.
      *
      * @param realmName Realm where agent group resides.
      * @param agentGroups Set of agent group names to be deleted.
      * @throws AMConsoleException if agents cannot be deleted.
      */
     public void deleteAgentGroups(String realmName, Set agentGroups) 
         throws AMConsoleException {
         if ((agentGroups != null) && !agentGroups.isEmpty()) {
             String idNames = AMFormatUtils.toCommaSeparatedFormat(agentGroups);
             String[] params = {realmName, idNames};
             logEvent("ATTEMPT_DELETE_AGENT_GROUP", params);
 
             try {
                 AMIdentityRepository repo = new AMIdentityRepository(
                     getUserSSOToken(), realmName);
                 repo.deleteIdentities(getAMIdentity(agentGroups));
                 logEvent("SUCCEED_DELETE_AGENT_GROUP", params);
             } catch (IdRepoException e) {
                 String[] paramsEx = {realmName, idNames, getErrorString(e)};
                 logEvent("EXCEPTION_DELETE_AGENT_GROUP", paramsEx);
                 throw new AMConsoleException(getErrorString(e));
             } catch (SSOException e) {
                 String[] paramsEx = {realmName, idNames, getErrorString(e)};
                 logEvent("EXCEPTION_DELETE_AGENT_GROUP", paramsEx);
                 throw new AMConsoleException(getErrorString(e));
             }
         }
     }
 
     
     private Set getAMIdentity(Set names)
         throws IdRepoException {
         Set identities = new HashSet(names.size() *2);
         SSOToken ssoToken = getUserSSOToken();
         for (Iterator iter = names.iterator(); iter.hasNext(); ) {
             identities.add(IdUtils.getIdentity(ssoToken, (String)iter.next()));
         }
         return identities; 
     }
 
     /**
      * Returns the group of which agent belongs to.
      *
      * @param realmName Realm where agent group resides.
      * @param universalId Universal ID of the agent.
      * @return the group of which agent belongs to.
      * @throws AMConsoleException if object cannot located.
      */
     public String getAgentGroupId(String realmName, String universalId)
         throws AMConsoleException {
         String groupName = getAgentGroup(realmName, universalId);
         if (groupName != null) {
             AMIdentity amid = new AMIdentity(getUserSSOToken(), 
                 groupName, IdType.AGENTGROUP, realmName, null);
             return amid.getUniversalId();
         } else {
             return null;
         }
     }
     
     /**
      * Returns the group of which agent belongs to.
      *
      * @param realmName Realm where agent group resides.
      * @param universalId Universal ID of the agent.
      * @return the group of which agent belongs to.
      * @throws AMConsoleException if object cannot located.
      */
     public String getAgentGroup(String realmName, String universalId) 
         throws AMConsoleException {
        String[] param = {realmName, universalId};
         logEvent("ATTEMPT_GET_AGENT_ATTRIBUTE_VALUES", param);
         String groupName = null;
         try {
 
             AMIdentity amid = IdUtils.getIdentity(
                 getUserSSOToken(), universalId);
             Set groups = amid.getMemberships(IdType.AGENTGROUP);
             if ((groups != null) && !groups.isEmpty()) {
                 AMIdentity group = (AMIdentity)groups.iterator().next();
                 groupName = group.getName();
             }
             logEvent("SUCCEED_GET_AGENT_ATTRIBUTE_VALUES", param);
             return groupName;
         } catch (SSOException e) {
            String[] paramsEx = {realmName, universalId, getErrorString(e)};
             logEvent("EXCEPTION_GET_AGENT_ATTRIBUTE_VALUES", paramsEx);
             throw new AMConsoleException(getErrorString(e));
         } catch (IdRepoException e) {
            String[] paramsEx = {realmName, universalId, getErrorString(e)};
             logEvent("EXCEPTION_GET_AGENT_ATTRIBUTE_VALUES", paramsEx);
             throw new AMConsoleException(getErrorString(e));
         }
     }
     
     /**
      * Returns attribute values of an agent or agent group.
      *
      * @param realmName Realm where agent or agent group resides.
      * @param universalId Universal ID of the agent/agent group.
      * @param withInheritValues <code>true</code> to include inherited values.
      * @return attribute values of an agent or agent group.
      * @throws AMConsoleException if object cannot located.
      */
     public Map getAttributeValues(
         String realmName,
         String universalId, 
         boolean withInheritValues
     ) throws AMConsoleException {
         String[] param = {universalId};
         logEvent("ATTEMPT_GET_AGENT_ATTRIBUTE_VALUES", param);
         
         try {
             AMIdentity amid = IdUtils.getIdentity(
                 getUserSSOToken(), universalId);
             Map values = AgentConfiguration.getAgentAttributes(amid, false);
             
             if (withInheritValues) {
                 String groupId = getAgentGroupId(realmName, universalId);
                 
                 if ((groupId != null) && (groupId.trim().length() > 0)) {
                     AMIdentity group = IdUtils.getIdentity(
                         getUserSSOToken(), groupId);
                     if (!group.isExists()) {
                         Object[] arg = {group.getName()};
                         throw new AMConsoleException(MessageFormat.format(
                             getLocalizedString("agent.group.does.not.exist"),
                             arg));
                     }
                     Map groupValues = AgentConfiguration.getAgentAttributes(
                         group, false);
                     groupValues.putAll(values);
                     values = groupValues;
                 }
             }
             
             logEvent("SUCCEED_GET_AGENT_ATTRIBUTE_VALUES", param);
             return values;
         } catch (SSOException e) {
             String[] paramsEx = {universalId, getErrorString(e)};
             logEvent("EXCEPTION_GET_AGENT_ATTRIBUTE_VALUES", paramsEx);
             throw new AMConsoleException(getErrorString(e));
         } catch (SMSException e) {
             String[] paramsEx = {universalId, getErrorString(e)};
             logEvent("EXCEPTION_GET_AGENT_ATTRIBUTE_VALUES", paramsEx);
             throw new AMConsoleException(getErrorString(e));
         } catch (IdRepoException e) {
             String[] paramsEx = {universalId, getErrorString(e)};
             logEvent("EXCEPTION_GET_AGENT_ATTRIBUTE_VALUES", paramsEx);
             throw new AMConsoleException(getErrorString(e));
         }
     }
     
     /**
      * Returns attribute values of an agent group.
      *
      * @param realmName Realm where agent group resides.
      * @param agentName agent group.
      * @return attribute values of an agent group.
      * @throws AMConsoleException if object cannot located.
      */
     public Map getGroupAttributeValues(String realmName, String groupName)
         throws AMConsoleException {
        String[] param = {realmName, groupName};
         logEvent("ATTEMPT_GET_AGENT_ATTRIBUTE_VALUES", param);
         
         try {
             AMIdentity amid = new AMIdentity(
                 getUserSSOToken(), groupName, IdType.AGENTGROUP, realmName, 
                 null);
             Map values =  AgentConfiguration.getAgentAttributes(amid, false);
             logEvent("SUCCEED_GET_AGENT_ATTRIBUTE_VALUES", param);
             return values;
         } catch (SSOException e) {
            String[] paramsEx = {realmName, groupName, getErrorString(e)};
             logEvent("EXCEPTION_GET_AGENT_ATTRIBUTE_VALUES", paramsEx);
             throw new AMConsoleException(getErrorString(e));
         } catch (SMSException e) {
            String[] paramsEx = {realmName, groupName, getErrorString(e)};
             logEvent("EXCEPTION_GET_AGENT_ATTRIBUTE_VALUES", paramsEx);
             throw new AMConsoleException(getErrorString(e));
         } catch (IdRepoException e) {
            String[] paramsEx = {realmName, groupName, getErrorString(e)};
             logEvent("EXCEPTION_GET_AGENT_ATTRIBUTE_VALUES", paramsEx);
             throw new AMConsoleException(getErrorString(e));
         }
     }
     
     /**
      * Modifies agent or agent group attribute values.
      *
      * @param universalId Universal ID of the agent/agent group.
      * @param values attribute values of an agent or agent group.
      * @throws AMConsoleException if object cannot located.
      */
     public void setAttributeValues(String universalId, Map values)
         throws AMConsoleException {
         String[] param = {universalId};
         logEvent("ATTEMPT_SET_AGENT_ATTRIBUTE_VALUE", param);
         
         try {
             AMIdentity amid = IdUtils.getIdentity(
                 getUserSSOToken(), universalId);
             values.remove(IdConstants.AGENT_TYPE);
 
             amid.setAttributes(values);
             amid.store();
             logEvent("SUCCEED_SET_AGENT_ATTRIBUTE_VALUE", param);
         } catch (SSOException e) {
             String[] paramsEx = {universalId, getErrorString(e)};
             logEvent("EXCEPTION_SET_AGENT_ATTRIBUTE_VALUE", paramsEx);
             throw new AMConsoleException(getErrorString(e));
         } catch (IdRepoException e) {
             String[] paramsEx = {universalId, getErrorString(e)};
             logEvent("EXCEPTION_SET_AGENT_ATTRIBUTE_VALUE", paramsEx);
             throw new AMConsoleException(getErrorString(e));
         }
     }
     
     /**
      * Modifies agent's group.
      *
      * @param realmName realm where agent resides.
      * @param universalId Universal ID of the agent.
      * @param groupName Name of group.
      * @throws AMConsoleException if object cannot located.
      */
     public void setGroup(String realmName, String universalId, String groupName)
         throws AMConsoleException {
         String[] param = {realmName, universalId};
         logEvent("ATTEMPT_SET_AGENT_ATTRIBUTE_VALUE", param);
         
         try {
             AMIdentity amid = IdUtils.getIdentity(
                 getUserSSOToken(), universalId);
             if ((groupName != null) && (groupName.length() > 0)) {
                 AMIdentity group =  new AMIdentity(
                     getUserSSOToken(), groupName, IdType.AGENTGROUP, realmName,
                     null);
                 group.addMember(amid);
             } else {
                 Set groups = amid.getMemberships(IdType.AGENTGROUP);
                 if ((groups != null) && !groups.isEmpty()) {
                     AMIdentity group = (AMIdentity)groups.iterator().next();
                     group.removeMember(amid);
                 }
             }
             
             logEvent("SUCCEED_SET_AGENT_ATTRIBUTE_VALUE", param);
         } catch (SSOException e) {
             String[] paramsEx = {realmName, universalId, getErrorString(e)};
             logEvent("EXCEPTION_SET_AGENT_ATTRIBUTE_VALUE", paramsEx);
             throw new AMConsoleException(getErrorString(e));
         } catch (IdRepoException e) {
             String[] paramsEx = {realmName, universalId, getErrorString(e)};
             logEvent("EXCEPTION_SET_AGENT_ATTRIBUTE_VALUE", paramsEx);
             throw new AMConsoleException(getErrorString(e));
         }
     }
     
     /**
      * Returns all the authentication chains in a realm.
      *
      * @param realm Name of realm.
      * @return all the authentication chains in a realm.
      * @throws AMConsoleException if authentication chains cannot be returned.
      */
     public Set getAuthenticationChains()
         throws AMConsoleException {
         try {
             Set chains = new TreeSet();
             chains.addAll(AgentConfiguration.getChoiceValues(
                 "authenticationChain", "WSPAgent").keySet());
             return chains;
         } catch (SSOException e) {
             throw new AMConsoleException(getErrorString(e));
         } catch (SMSException e) {
             throw new AMConsoleException(getErrorString(e));
         }
     }
     
     /**
      * Returns a map of i18n key to supported security mechanism.
      *
      * @param agentType Type of agent.
      * @return a map of i18n key to supported security mechanism.
      */
     public Map getSecurityMechanisms(String agentType) {
         try {
             return AgentConfiguration.getChoiceValues(
                 "SecurityMech", agentType);
         } catch (SSOException e) {
             debug.error("AgentModelImpl.getSecurityMechanisms", e);
         } catch (SMSException e) {
             debug.error("AgentModelImpl.getSecurityMechanisms", e);
         }
         return Collections.EMPTY_MAP;
     }
     
     /**
      * Returns set of secure token service configurations.
      *
      * @return set of secure token service configurations.
      * @throws AMConsoleException if secure token service configurations cannot
      *         be returned.
      */
     public Set getSTSConfigurations() {
         try {
             Map map = AgentConfiguration.getChoiceValues(
                 "STS", "WSCAgent");
             if ((map != null) && !map.isEmpty()) {
                 return map.keySet();
             }
         } catch (SSOException e) {
             debug.error("AgentModelImpl.getSTSConfigurations", e);
         } catch (SMSException e) {
             debug.error("AgentModelImpl.getSTSConfigurations", e);
         }
         return Collections.EMPTY_SET;
     }
     
     /**
      * Returns set of discovery configurations.
      *
      * @return set of discovery configurations.
      * @throws AMConsoleException if discovery configurations cannot be returned.
      */
     public Set getDiscoveryConfigurations() {
         try {
             Map map = AgentConfiguration.getChoiceValues(
                 "Discovery", "WSCAgent");
             if ((map != null) && !map.isEmpty()) {
                 return map.keySet();
             }
         } catch (SSOException e) {
             debug.error("AgentModelImpl.getDiscoveryConfigurations", e);
         } catch (SMSException e) {
             debug.error("AgentModelImpl.getDiscoveryConfigurations", e);
         }
         return Collections.EMPTY_SET;
     }    
     
     /**
      * Returns display name of an agent/group.
      *
      * @param universalId Universal ID of the agent/agent group. 
      * @returns display name of an agent/group.
      * @throws AMConsoleException if object cannot located.
      */
     public String getDisplayName(String universalId) 
         throws AMConsoleException {
         try {
             AMIdentity amid = IdUtils.getIdentity(
                 getUserSSOToken(), universalId);
             return amid.getName();
         } catch (IdRepoException e) {
             throw new AMConsoleException(getErrorString(e));
         }
     }
     
     /**
      * Returns <code>true</code> if the identity if an agent group.
      *
      * @param universalId Universal ID of the agent/agent group. 
      * @returns <code>true</code> if the identity if an agent group.
      */
     public boolean isAgentGroup(String universalId) {
         boolean isGroup = false;
         try {
             AMIdentity amid = IdUtils.getIdentity(
                 getUserSSOToken(), universalId);
             isGroup = amid.getType().equals(IdType.AGENTGROUP);
         } catch (IdRepoException e) {
             debug.error("AgentModelImpl.getSecurityMechanisms", e);
         }
         return isGroup;
     }
 
     /**
      * Returns a set of inherited property names
      *
      * @param realmName Realm where agent resides
      * @param universalId Universal ID of the agent.
      * @return a set of inherited property names.
      */
     public Set getInheritedPropertyNames(String realmName, String universalId) {
         Set names = null;
         try {
             if (getAgentGroup(realmName, universalId) != null) {
                 AMIdentity amid = IdUtils.getIdentity(
                     getUserSSOToken(), universalId);
                 names = AgentConfiguration.getInheritedAttributeNames(amid);
             }
         } catch (AMConsoleException e) {
             debug.error("AgentModelImpl.getInheritedPropertyNames", e);
         } catch (SMSException e) {
             debug.error("AgentModelImpl.getInheritedPropertyNames", e);
         } catch (IdRepoException e) {
             debug.error("AgentModelImpl.getInheritedPropertyNames", e);
         } catch (SSOException e) {
             debug.error("AgentModelImpl.getInheritedPropertyNames", e);
         }
 
         return (names !=  null) ? names : Collections.EMPTY_SET;
     }
 
     /**
      * Returns attribute schemas of a given set of attributes.
      *
      * @param agentType Agent type.
      * @return attribute schemas of a given set of attributes.
      */
     public Map getAttributeSchemas(String agentType, Collection attributeNames) {
         Map map = null;
         try {
             map = AgentConfiguration.getAttributeSchemas(
                 agentType, attributeNames);
         } catch (SSOException e) {
             debug.error("AgentModelImpl.getLocalizedNames", e);
         } catch (SMSException e) {
             debug.error("AgentModelImpl.getLocalizedNames", e);
         }
         return (map != null) ? map : Collections.EMPTY_MAP;
     }
     
     /**
      * Updates inheritance setting.
      *
      * @param universalId Universal ID of the agent.
      * @param inherit Map of attribute name to either "1" or "0". "1" to 
      *        inherit and "0" not.
      * @throws AMConsoleException if update failed.
      */
     public void updateAgentConfigInheritance(String universalId, Map inherit)
         throws AMConsoleException {
         try {
             AMIdentity amid = IdUtils.getIdentity(
                 getUserSSOToken(), universalId);
             AgentConfiguration.updateInheritance(amid, inherit);
         } catch (SMSException e) {
             throw new AMConsoleException(getErrorString(e));
         } catch (SSOException e) {
             throw new AMConsoleException(getErrorString(e));
         } catch (IdRepoException e) {
             throw new AMConsoleException(getErrorString(e));
         }
     }
 
     /**
      * Returns attribute values of agent's group.
      *
      * @param realmName Realm where agent group resides
      * @param agentId Universal Id of the agent.
      * @param attrNames Attribute Names of interests.
      * @return attribute values of agent's group.
      * @throws AMConsoleException if unable to get the attribute values.
      */
     public Map getAgentGroupValues(
         String realmName,
         String agentId, 
         Set attrNames
     ) throws AMConsoleException {
         if (attrNames.isEmpty()) {
             return Collections.EMPTY_MAP;
         }
 
         String groupId = getAgentGroupId(realmName, agentId);
         if ((groupId != null) && (groupId.length() != 0)) {
             try {
                 AMIdentity amid = IdUtils.getIdentity(
                     getUserSSOToken(), groupId);
                 return amid.getAttributes(attrNames);
             } catch (SSOException e) {
                 throw new AMConsoleException(getErrorString(e));
             } catch (IdRepoException e) {
                 throw new AMConsoleException(getErrorString(e));
             }
         } else {
             return Collections.EMPTY_MAP;
         }
     }
     
     /**
      * Returns a set of members of an agent group.
      *
      * @param agentId Universal Id of the agent group.
      * @throws AMConsoleException if members cannot be returned.
      */
     public Set getAgentGroupMembers(String agentId)
         throws AMConsoleException {
         try {
             AMIdentity amid = IdUtils.getIdentity(getUserSSOToken(), agentId);
             return amid.getMembers(IdType.AGENTONLY);
         } catch (SSOException e) {
             throw new AMConsoleException(getErrorString(e));
         } catch (IdRepoException e) {
             throw new AMConsoleException(getErrorString(e));
         }
     }
 }
