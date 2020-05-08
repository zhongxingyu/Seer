 /**
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.hadoop.cmf;
 
 import org.apache.hadoop.cmf.MasterService;
 import org.apache.hadoop.cmf.KeystoreService;
 import org.apache.hadoop.cmf.AliasService;
 import org.apache.hadoop.cmf.impl.CMFAliasService;
 import org.apache.hadoop.cmf.impl.CMFMasterService;
 import org.apache.hadoop.cmf.impl.CMFKeystoreService;
 
 /**
 * This is a simple example of a service to bootstrap the CMF framework.
 * It demonstrates the setup and simple error handling.
 */
 public class CMFBootstrapService {
 	private MasterService ms = null;
 	private KeystoreService ks = null;
 	private AliasService as = null;
 	String securityDir = null;
 	String keystoreDir = null;
 	
 	public void init(String securityDir, String keystoreDir, String serviceName) {
 		this.securityDir = securityDir;
 		this.keystoreDir = keystoreDir;
 		ms = new CMFMasterService(serviceName);
 		try {
 			ks = new CMFKeystoreService(keystoreDir, serviceName, ms);
 		}
 		catch(KeystoreServiceException kse) {
 		  // log approrpiate message
 		  System.out.println("CMF Startup Abort: Issue with Keystore.");
 		  // you will want to halt system startup as appropriate for your application
          System.exit(1);
 		}
 		as = new CMFAliasService(ks);
 	}
 	
 	public void start(boolean persist) {
 		try {
 			ms.setupMasterSecret(securityDir, persist);
 			if (!ks.isCredentialStoreAvailable()) {
 				ks.createCredentialStore();
 			}
 		}
 		catch(KeystoreServiceException kse) {
 		  // log approrpiate message
 		  System.out.println("CMF Startup Abort: Issue with Keystore.");
 		  // you will want to halt system startup as appropriate for you
          System.exit(1);
 		}
 		catch(MasterServiceException kse) {
 		  // log approrpiate message
 		  System.out.println("CMF Startup Abort: Issue with MasterService.");
 		  // you will want to halt system startup as appropriate for you
          System.exit(1);
 		}
 	}
 	
 	public void stop() {
 	}
 	
 	public void destroy() {
 	}
 	
 	public AliasService getAliasService() {
 		return as;
 	}
 }
