 /*
  *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 package org.apache.yoko.orb.OBPortableServer;
 
 import org.apache.yoko.orb.OBPortableServer.AcceptorConfig;
 import org.apache.yoko.orb.OBPortableServer.CommunicationsConcurrencyPolicy;
 import org.apache.yoko.orb.OBPortableServer.ENDPOINT_CONFIGURATION_POLICY_ID;
 import org.apache.yoko.orb.OBPortableServer.EndpointConfigurationPolicy;
 import org.apache.yoko.orb.OBPortableServer.EndpointConfigurationPolicyHelper;
 import org.apache.yoko.orb.OBPortableServer.GIOPVersionPolicy;
 import org.apache.yoko.orb.OBPortableServer.POAManager;
 import org.apache.yoko.orb.OBPortableServer.POAManagerFactory;
 
 final public class POAManagerFactory_impl extends org.omg.CORBA.LocalObject
         implements POAManagerFactory {
     //
     // The ORB Instance
     //
     private org.apache.yoko.orb.OB.ORBInstance orbInstance_;
 
     //
     // Hashtable mapping name -> POAManager
     //
     private java.util.Hashtable managers_ = new java.util.Hashtable();
 
     //
     // Running count for generating unique names
     //
     private int count_ = 0;
 
     //
     // The IMRActiveStateHolder
     //
     private org.apache.yoko.orb.IMR.ActiveState activeState_;
 
     //
     // The POALocator
     //
     private POALocator poaLocator_;
 
     //
     // The OAD::ProcessEndpoint
     //
     private org.apache.yoko.orb.OAD.ProcessEndpoint_impl processEndpoint_;
 
     //
     // This has to be a member of the ORB since we need to keep the
     // connection open for the lifespan of the process.
     //
     // TODO: When we have connection reaping then we'll have to have
     // to set a policy on this object to prevent the connection from
     // being reaped.
     //
     private org.omg.CORBA.Object endpointManager_;
 
     private String getUniqueName() {
         long now = System.currentTimeMillis();
 
         String name = "POAManager-" + now;
         name += count_++;
 
         org.apache.yoko.orb.OB.Assert._OB_assert(!managers_.containsKey(name));
 
         return name;
     }
 
     private void validateName(String name)
             throws org.omg.PortableServer.POAManagerFactoryPackage.ManagerAlreadyExists {
         //
         // Does the POAManager exist?
         //
         POAManager manager = (POAManager) managers_.get(name);
         if (manager != null) {
             //
             // If the POAManager is INACTIVE then remove it from
             // the list, and allow the user to re-add the
             // POAManager
             //
             if (manager.get_state() == org.omg.PortableServer.POAManagerPackage.State.INACTIVE)
                 managers_.remove(name);
             else
                 throw new org.omg.PortableServer.POAManagerFactoryPackage.ManagerAlreadyExists();
         }
     }
 
     private AcceptorConfig[] parseEndpointString(String endpoint) {
         org.apache.yoko.orb.OB.Logger logger = orbInstance_.getLogger();
 
         java.util.Vector configVec = new java.util.Vector();
 
         org.apache.yoko.orb.OCI.AccFactoryRegistry registry = orbInstance_
                 .getAccFactoryRegistry();
         org.apache.yoko.orb.OCI.AccFactory[] factories = registry
                 .get_factories();
 
         int pos = 0;
         while (pos != -1) {
             java.util.Vector params = new java.util.Vector();
             pos = org.apache.yoko.orb.OB.ParseParams.parse(endpoint, pos,
                     params);
             if (!params.isEmpty()) {
                 String prot = (String) params.firstElement();
                 params.removeElementAt(0);
                 boolean found = false;
                 int i;
                 for (i = 0; i < factories.length; i++) {
                     if (prot.equals(factories[i].id())) {
                         String[] paramSeq = new String[params.size()];
                         params.copyInto(paramSeq);
 
                         AcceptorConfig config = new AcceptorConfig(prot,
                                 paramSeq);
                         configVec.addElement(config);
 
                         found = true;
                         break;
                     }
                 }
                 if (!found) {
                     String err = "unknown endpoint protocol `" + prot + "'";
                     logger.error(err);
                     throw new org.omg.CORBA.INITIALIZE(err);
                 }
             }
         }
 
         if (configVec.size() == 0) {
             String err = "no endpoints defined";
             logger.error(err);
             throw new org.omg.CORBA.INITIALIZE(err);
         }
 
         AcceptorConfig[] configArr = new org.apache.yoko.orb.OBPortableServer.AcceptorConfig[configVec
                 .size()];
         configVec.copyInto(configArr);
 
         return configArr;
     }
 
     // ----------------------------------------------------------------------
     // OBPOAManagerFactory_impl public member implementation
     // ----------------------------------------------------------------------
 
     public POAManagerFactory_impl() {
         managers_ = new java.util.Hashtable(7);
         poaLocator_ = new POALocator();
     }
 
     // ----------------------------------------------------------------------
     // Standard IDL to Java mapping
     // ----------------------------------------------------------------------
 
     public org.omg.PortableServer.POAManager create_POAManager(String id,
             org.omg.CORBA.Policy[] policies)
             throws org.omg.PortableServer.POAManagerFactoryPackage.ManagerAlreadyExists,
             org.omg.CORBA.PolicyError {
         synchronized (managers_) {
             if (id.length() == 0) {
                 id = getUniqueName();
             } else {
                 validateName(id);
                 ++count_;
             }
 
             java.util.Properties props = orbInstance_.getProperties();
             org.apache.yoko.orb.OB.Logger logger = orbInstance_.getLogger();
 
             //
             // If no endpoint config policy is defined, this info will
             // have to be retrieved from the orb properties.
             //
             EndpointConfigurationPolicy endpointPolicy = null;
 
             //
             // We are only concerned with the endpoint config policy
             // here; other policies will be passed on to the POAManager_impl
             // constructor.
             //
             java.util.Vector tmpPolicyVector = new java.util.Vector();
 
             int nTmpPolicies = 0;
 
             int nPolicies = policies.length;
             if (nPolicies != 0) {
                 for (int i = 0; i < nPolicies; ++i) {
                     int policyType = policies[i].policy_type();
                     if (policyType == ENDPOINT_CONFIGURATION_POLICY_ID.value) {
                         endpointPolicy = EndpointConfigurationPolicyHelper
                                 .narrow(policies[i]);
                     } else {
                         ++nTmpPolicies;
                         tmpPolicyVector.addElement(policies[i]);
                     }
                 }
             }
             org.omg.CORBA.Policy[] tmpPolicies = new org.omg.CORBA.Policy[tmpPolicyVector
                     .size()];
             tmpPolicyVector.copyInto(tmpPolicies);
 
             AcceptorConfig[] config;
 
             if (endpointPolicy == null) {
                 //
                 // Get the endpoint configuration
                 //
                 String rootStr = null;
                 String paramStr = null;
                 if (id.equals("RootPOAManager"))
                     rootStr = props.getProperty("yoko.orb.oa.endpoint");
                 String propName = "yoko.orb.poamanager." + id + ".endpoint";
                 paramStr = props.getProperty(propName);
                
                 if (paramStr == null && rootStr == null)
                     paramStr = "iiop";
                 else if (paramStr == null)
                     paramStr = rootStr;
 
                 config = parseEndpointString(paramStr);
             }
 
             else {
                 //
                 // Create acceptors based on the endpoint config policy
                 //
                 config = endpointPolicy.value();
             }
 
             org.apache.yoko.orb.OCI.AccFactoryRegistry registry = orbInstance_
                     .getAccFactoryRegistry();
 
             java.util.Vector acceptors = new java.util.Vector();
             int nConfig = config.length;
             for (int i = 0; i < nConfig; i++) {
                 try {
                     org.apache.yoko.orb.OCI.AccFactory factory = registry
                             .get_factory(config[i].id);
                     acceptors.addElement(factory
                             .create_acceptor(config[i].params));
                 } catch (org.apache.yoko.orb.OCI.NoSuchFactory ex) {
                     String err = "cannot find factory: " + ex;
                     logger.error(err, ex);
                     throw (org.omg.CORBA.INITIALIZE)new org.omg.CORBA.INITIALIZE(err).initCause(ex);
                 } catch (org.apache.yoko.orb.OCI.InvalidParam ex) {
                     String err = "unable to create acceptor: " + ex.reason;
                     logger.error(err, ex);
                     throw (org.omg.CORBA.INITIALIZE)new org.omg.CORBA.INITIALIZE(err).initCause(ex);
                 }
             }
 
             //
             // Create the new POAManager_impl and add to the table
             //
             org.apache.yoko.orb.OCI.Acceptor[] arr = new org.apache.yoko.orb.OCI.Acceptor[acceptors
                     .size()];
             acceptors.copyInto(arr);
             POAManager manager = new POAManager_impl(orbInstance_, poaLocator_,
                     id, Integer.toString(count_), arr, tmpPolicies);
             managers_.put(id, manager);
 
             return manager;
         }
     }
 
     public org.omg.PortableServer.POAManager[] list() {
         java.util.Enumeration e = managers_.keys();
         java.util.Vector result = new java.util.Vector();
 
         while (e.hasMoreElements()) {
             String name = (String) e.nextElement();
             POAManager manager = (org.apache.yoko.orb.OBPortableServer.POAManager) managers_
                     .get(name);
             if (manager != null)
                 result.addElement(manager);
         }
 
         POAManager[] r = new POAManager[result.size()];
         result.copyInto(r);
 
         return r;
     }
 
     public org.omg.PortableServer.POAManager find(String id) {
         return (org.omg.PortableServer.POAManager) (managers_.get(id));
     }
 
     public void destroy() {
         //
         // Remove the references to the orbInstance_
         //
         orbInstance_ = null;
 
         //
         // Shutdown, if necessary
         //
         if (!managers_.isEmpty())
             _OB_deactivate();
     }
 
     public EndpointConfigurationPolicy create_endpoint_configuration_policy(
             String value) throws org.omg.CORBA.PolicyError {
         AcceptorConfig[] configArray = parseEndpointString(value);
         return new EndpointConfigurationPolicy_impl(configArray);
     }
 
     public CommunicationsConcurrencyPolicy create_communications_concurrency_policy(
             short value) throws org.omg.CORBA.PolicyError {
         return new CommunicationsConcurrencyPolicy_impl(value);
     }
 
     public GIOPVersionPolicy create_giop_version_policy(short value)
             throws org.omg.CORBA.PolicyError {
         return new GIOPVersionPolicy_impl(value);
     }
 
     //
     // Deactivate all POAManagers
     //
     public void _OB_deactivate() {
         //
         // Deactivate each of the POAManagers
         //
         java.util.Enumeration e = managers_.keys();
         while (e.hasMoreElements()) {
             String name = (String) e.nextElement();
             POAManager manager = (POAManager) managers_.get(name);
             if (manager != null) {
                 try {
                     manager.deactivate(true, true);
                 } catch (org.omg.PortableServer.POAManagerPackage.AdapterInactive ex) {
                     // Ignore
                 }
             }
         }
         managers_.clear();
 
         //
         // Tell the IMR that the ORB is STOPPING
         //
         if (activeState_ != null) {
             org.apache.yoko.orb.OB.Logger logger = orbInstance_.getLogger();
             String serverInstance = orbInstance_.getServerInstance();
             try {
                 activeState_.set_status(serverInstance,
                         org.apache.yoko.orb.IMR.ServerStatus.STOPPING);
             } catch (org.omg.CORBA.SystemException ex) {
                 String msg = orbInstance_.getServerId()
                         + ": Cannot contact IMR on shutdown";
                 logger.warning(msg, ex);
             }
 
             //
             // Clear the IMR::Server record
             //
             activeState_ = null;
         }
     }
 
     public DirectServant _OB_getDirectServant(org.omg.IOP.IOR ior,
             org.apache.yoko.orb.OB.RefCountPolicyList policies)
             throws org.apache.yoko.orb.OB.LocationForward {
         //
         // Optimization
         //
         if (managers_.isEmpty())
             return null;
 
         java.util.Enumeration e = managers_.keys();
         while (e.hasMoreElements()) {
             try {
                 String name = (String) e.nextElement();
                 POAManager_impl manager = (POAManager_impl) managers_.get(name);
                 if (manager != null) {
                     org.apache.yoko.orb.OCI.Acceptor[] acceptors = manager
                             .get_acceptors();
                     for (int i = 0; i < acceptors.length; i++) {
                         org.apache.yoko.orb.OCI.ProfileInfo[] profileInfos = acceptors[i]
                                 .get_local_profiles(ior);
 
                         //
                         // If the IOR is local then at least one ProfileInfo
                         // will be returned
                         //
                         if (profileInfos.length > 0) {
                             //
                             // In the case that the servant cannot support a
                             // direct invocation, null will be returned
                             //
                             return manager._OB_getDirectServant(
                                     profileInfos[0].key, policies);
                         }
                     }
                 }
             } catch (org.omg.PortableServer.POAManagerPackage.AdapterInactive ex) {
                 //
                 // Ignore -- the POAManager isn't valid anymore
                 //
             }
         }
 
         return null;
     }
 
     public void _OB_setORBInstance(org.apache.yoko.orb.OB.ORBInstance instance) {
         orbInstance_ = instance;
     }
 
     public void _OB_initializeIMR(POA_impl root,
             org.apache.yoko.orb.OB.ORBControl orbControl) {
         String serverId = orbInstance_.getServerId();
         String serverInstance = orbInstance_.getServerInstance();
 
         java.util.Properties properties = orbInstance_.getProperties();
         String noIMR = properties.getProperty("yoko.orb.noIMR");
         if (serverId.length() == 0 || noIMR != null)
             return;
 
         //
         // Create the OAD::ProcessMonitor servant
         //
         processEndpoint_ = new org.apache.yoko.orb.OAD.ProcessEndpoint_impl(
                 serverId, serverInstance, root, orbControl);
 
         org.apache.yoko.orb.IMR.Domain imrDomain = null;
         try {
             org.apache.yoko.orb.OB.InitialServiceManager initServiceManager = orbInstance_
                     .getInitialServiceManager();
             org.omg.CORBA.Object imrObj = initServiceManager
                     .resolveInitialReferences("IMR");
             imrDomain = org.apache.yoko.orb.IMR.DomainHelper.narrow(imrObj);
         } catch (org.omg.CORBA.ORBPackage.InvalidName ex) {
             // Ignore -- this will be handled later
         } catch (org.omg.CORBA.BAD_PARAM ex) {
             // narrow() failed
         }
 
         org.apache.yoko.orb.OB.Logger logger = orbInstance_.getLogger();
 
         //
         // IMR::IMRDomain not reachable?
         //
         if (imrDomain == null) {
             String msg = serverId + ": IMRDomain not reachable";
             logger.error(msg);
             throw new org.omg.CORBA.INITIALIZE(msg);
         }
 
         //
         // Check if need to register with the IMR
         //
         String exec = properties.getProperty("yoko.orb.imr.register");
         if (exec != null) {
             //
             // TODO: What do we do for Java?
             //
             String msg = serverId + ": Self registration not implemented"
                     + " for java servers";
             logger.error(msg);
             throw new org.omg.CORBA.INITIALIZE(msg);
         }
 
         //
         // Tell the IMR that we're starting up
         //
         try {
             //
             // This is the ProcessEndpointManager
             //
             org.apache.yoko.orb.OAD.ProcessEndpointManagerHolder endpoint = new org.apache.yoko.orb.OAD.ProcessEndpointManagerHolder();
 
             org.omg.PortableInterceptor.ObjectReferenceTemplate primary = root
                     .adapter_template();
 
             //
             // Tell the IMR that we are STARTING.
             //
             activeState_ = imrDomain.startup(serverId, serverInstance, primary,
                     endpoint);
 
             //
             // Link with the OAD ProcessEndpoint
             //
             org.apache.yoko.orb.OAD.ProcessEndpoint ref = processEndpoint_
                     ._this(orbInstance_.getORB());
             endpoint.value.establish_link(serverId, serverInstance, 0xFFFFFFFF,
                     ref);
             endpointManager_ = endpoint.value;
 
             //
             // Create an register the IORInterceptor for the IMR
             //
             org.omg.PortableInterceptor.IORInterceptor i = new org.apache.yoko.orb.PortableInterceptor.IMRIORInterceptor_impl(
                     logger, activeState_, serverInstance);
             org.apache.yoko.orb.OB.PIManager piManager = orbInstance_
                     .getPIManager();
 
             try {
                 piManager.addIORInterceptor(i, true);
             } catch (org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName ex) {
                 org.apache.yoko.orb.OB.Assert._OB_assert(ex);
             }
         } catch (org.omg.CORBA.BAD_PARAM ex) {
             String msg = serverId + ": (IMR) Server already running";
             logger.error(msg, ex);
             throw (org.omg.CORBA.INITIALIZE)new org.omg.CORBA.INITIALIZE(msg).initCause(ex);
         } catch (org.apache.yoko.orb.IMR.NoSuchServer ex) {
             String msg = serverId + ": (IMR) Not registered with IMR";
             logger.error(msg, ex);
             throw (org.omg.CORBA.INITIALIZE)new org.omg.CORBA.INITIALIZE(msg).initCause(ex);
         } catch (org.apache.yoko.orb.IMR.NoSuchOAD ex) {
             String msg = serverId + ": (IMR) No OAD for host";
             logger.error(msg, ex);
             throw (org.omg.CORBA.INITIALIZE)new org.omg.CORBA.INITIALIZE(msg).initCause(ex);
         } catch (org.apache.yoko.orb.IMR.OADNotRunning ex) {
             String msg = serverId + ": (IMR) OAD not running";
             logger.error(msg, ex);
             throw (org.omg.CORBA.INITIALIZE)new org.omg.CORBA.INITIALIZE(msg).initCause(ex);
         } catch (org.apache.yoko.orb.OAD.AlreadyLinked ex) {
             String msg = serverId + ": (IMR) Process registered with OAD";
             logger.error(msg, ex);
             throw (org.omg.CORBA.INITIALIZE)new org.omg.CORBA.INITIALIZE(msg).initCause(ex);
         }
     }
 }
