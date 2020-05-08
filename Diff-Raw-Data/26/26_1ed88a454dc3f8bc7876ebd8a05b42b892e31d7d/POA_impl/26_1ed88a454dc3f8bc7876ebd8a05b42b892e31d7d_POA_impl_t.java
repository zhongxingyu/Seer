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
 
 import org.apache.yoko.orb.OBPortableServer.DISPATCH_STRATEGY_POLICY_ID;
 import org.apache.yoko.orb.OBPortableServer.DispatchStrategyPolicy;
 import org.apache.yoko.orb.OBPortableServer.INTERCEPTOR_CALL_POLICY_ID;
 import org.apache.yoko.orb.OBPortableServer.POA;
 import org.apache.yoko.orb.OBPortableServer.POAManager;
 import org.apache.yoko.orb.OBPortableServer.POAManagerFactory;
 import org.apache.yoko.orb.OBPortableServer.POAManagerFactoryHelper;
 import org.apache.yoko.orb.OBPortableServer.POAManagerHelper;
 import org.apache.yoko.orb.OBPortableServer.SYNCHRONIZATION_POLICY_ID;
 import org.apache.yoko.orb.OBPortableServer.SynchronizationPolicy;
 import org.apache.yoko.orb.OBPortableServer.SynchronizationPolicyValue;
 
 final public class POA_impl extends org.omg.CORBA.LocalObject implements POA {
     //
     // The ORB
     //
     private org.omg.CORBA.ORB orb_;
 
     //
     // The ORBInstance object
     //
     private org.apache.yoko.orb.OB.ORBInstance orbInstance_;
 
     //
     // The name of this POA and the name of the root POA
     //
     private String name_;
 
     //
     // The adapter id
     //
     private byte[] adapterId_;
 
     //
     // The Server ID (or "_RootPOA" if not set
     //
     String serverId_;
 
     //
     // The POA name. This is the full path to the POA.
     //
     private String[] poaId_;
 
     //
     // My parent POA
     //
     private POA_impl parent_;
 
     //
     // My POA manager
     //
     private POAManager manager_;
 
     //
     // My servant location strategy
     //
     private ServantLocationStrategy servantLocationStrategy_;
 
     //
     // My POAControl object
     //
     private POAControl poaControl_;
 
     //
     // The associated AdapterActivator
     //
     private AdapterActivatorHolder adapterActivator_;
 
     //
     // All children of this POA
     //
     private java.util.Hashtable children_;
 
     //
     // My policies
     //
     private POAPolicies policies_;
 
     //
     // Used for generating object ids
     //
     private IdGenerationStrategy idGenerationStrategy_;
 
     //
     // The POA creation time, if not persistent
     //
     private int poaCreateTime_;
 
     //
     // If this POA has the single thread policy set then this mutex is
     // used ensure this policy is met
     //
     private org.apache.yoko.orb.OB.RecursiveMutex poaSyncMutex_ = new org.apache.yoko.orb.OB.RecursiveMutex();
 
     private org.apache.yoko.orb.OB.DispatchStrategy dispatchStrategy_;
 
     //
     // The Current implementation. The RootPOA is responsible for the
     // creation and deletion of the one and only PortableServer::Current.
     //
     private org.apache.yoko.orb.PortableServer.Current_impl poaCurrent_;
 
     //
     // The OCI::Current implementation. The RootPOA is responsible for
     // the creation and deletion of the one and only OCI::Current.
     //
     private org.apache.yoko.orb.OCI.Current_impl ociCurrent_;
 
     //
     // This is the set of policies that are not POA policies and are
     // registered with a valid PolicyFactory
     //
     private org.omg.CORBA.Policy[] rawPolicies_;
 
     //
     // The primary, secondary and current ObjectReferenceTemplate
     //
     private org.omg.PortableInterceptor.ObjectReferenceTemplate adapterTemplate_;
 
     private org.omg.PortableInterceptor.ObjectReferenceFactory currentFactory_;
 
     private org.omg.PortableInterceptor.ObjectReferenceFactory ort_;
 
     //
     // The IORInfo
     //
     private org.omg.PortableInterceptor.IORInfo iorInfo_;
 
     //
     // The child ORTs required for adapterStateChange call when
     // POA is destroyed.
     //
     private java.util.Vector childTemplates_;
 
     //
     // Should POA call AdapterStateChange when destroyed
     //
     boolean callAdapterStateChange_;
 
     // ----------------------------------------------------------------------
     // POA private member implementation
     // ----------------------------------------------------------------------
 
     //
     // Return the appropriate object reference template.
     //
     private org.omg.PortableInterceptor.ObjectReferenceFactory ort() {
         if (ort_ == null) {
             ort_ = current_factory();
             if (ort_ == null)
                 ort_ = adapterTemplate_;
         }
         return ort_;
     }
 
     //
     // Given an object id create an object key
     //
     private byte[] createObjectKey(byte[] id) {
         return org.apache.yoko.orb.OB.ObjectKey
                 .CreateObjectKey(new org.apache.yoko.orb.OB.ObjectKeyData(
                         serverId_,
                         poaId_,
                         id,
                         policies_.lifespanPolicy() == org.omg.PortableServer.LifespanPolicyValue.PERSISTENT,
                         poaCreateTime_));
     }
 
     //
     // Find the index of the given policy
     //
     private boolean findPolicyIndex(org.omg.CORBA.Policy[] p, int type,
             org.omg.CORBA.IntHolder i) {
         for (i.value = 0; i.value < p.length; i.value++)
             if (p[i.value].policy_type() == type)
                 return true;
         return false;
     }
 
     //
     // Validate a specific set of POA policies
     //
     private boolean validatePolicies(POAPolicies policies,
             org.omg.CORBA.Policy[] policyList, org.omg.CORBA.IntHolder i) {
         //
         // NON_RETAIN requires either USE_DEFAULT_SERVANT or
         // USE_SERVANT_MANAGER
         //
         if (policies.servantRetentionPolicy() == org.omg.PortableServer.ServantRetentionPolicyValue.NON_RETAIN
                 && policies.requestProcessingPolicy() != org.omg.PortableServer.RequestProcessingPolicyValue.USE_SERVANT_MANAGER
                 && policies.requestProcessingPolicy() != org.omg.PortableServer.RequestProcessingPolicyValue.USE_DEFAULT_SERVANT) {
             boolean ok = findPolicyIndex(policyList,
                     org.omg.PortableServer.SERVANT_RETENTION_POLICY_ID.value, i);
             org.apache.yoko.orb.OB.Assert._OB_assert(ok);
             return false;
         }
 
         //
         // USE_ACTIVE_OBJECT_MAP_ONLY requires RETAIN
         //
         if (policies.requestProcessingPolicy() == org.omg.PortableServer.RequestProcessingPolicyValue.USE_ACTIVE_OBJECT_MAP_ONLY
                 && policies.servantRetentionPolicy() != org.omg.PortableServer.ServantRetentionPolicyValue.RETAIN) {
             //
             // One of the two must be present
             //
             boolean ok = findPolicyIndex(policyList,
                     org.omg.PortableServer.SERVANT_RETENTION_POLICY_ID.value, i);
             if (!ok)
                 ok = findPolicyIndex(
                         policyList,
                         org.omg.PortableServer.REQUEST_PROCESSING_POLICY_ID.value,
                         i);
             org.apache.yoko.orb.OB.Assert._OB_assert(ok);
             return false;
         }
 
         //
         // USE_DEFAULT_SERVANT requires MULTIPLE_ID
         //
         if (policies.requestProcessingPolicy() == org.omg.PortableServer.RequestProcessingPolicyValue.USE_DEFAULT_SERVANT
                 && policies.idUniquenessPolicy() != org.omg.PortableServer.IdUniquenessPolicyValue.MULTIPLE_ID) {
             //
             // Since USE_DEFAULT_SERVANT, this must be present
             //
             boolean ok = findPolicyIndex(policyList,
                     org.omg.PortableServer.REQUEST_PROCESSING_POLICY_ID.value,
                     i);
             org.apache.yoko.orb.OB.Assert._OB_assert(ok);
             return false;
         }
 
         //
         // IMPLICIT_ACTIVATION requires SYSTEM_ID and RETAIN
         //
         if (policies.implicitActivationPolicy() == org.omg.PortableServer.ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION
                 && policies.servantRetentionPolicy() != org.omg.PortableServer.ServantRetentionPolicyValue.RETAIN
                 && policies.idAssignmentPolicy() != org.omg.PortableServer.IdAssignmentPolicyValue.SYSTEM_ID) {
             //
             // Since IMPLICIT_ACTIVATION , this must be present
             //
             boolean ok = findPolicyIndex(policyList,
                     org.omg.PortableServer.IMPLICIT_ACTIVATION_POLICY_ID.value,
                     i);
             org.apache.yoko.orb.OB.Assert._OB_assert(ok);
             return false;
         }
 
         return true;
     }
 
     //
     // Handle unknown exceptions
     //
     private void handleUnknownException(org.apache.yoko.orb.OB.Upcall upcall,
             Exception ex) {
         org.apache.yoko.orb.OB.UnknownExceptionStrategy strategy = orbInstance_
                 .getUnknownExceptionStrategy();
 
         org.apache.yoko.orb.OB.UnknownExceptionInfo info = new org.apache.yoko.orb.OB.UnknownExceptionInfo_impl(
                 upcall.operation(), upcall.responseExpected(), upcall
                         .transportInfo(), (RuntimeException) ex);
 
         try {
             strategy.unknown_exception(info);
 
             //
             // Return CORBA::UNKNOWN if the strategy doesn't raise
             // an exception
             //
             upcall.setSystemException(new org.omg.CORBA.UNKNOWN());
         } catch (org.omg.CORBA.SystemException sysEx) {
             upcall.setSystemException(sysEx);
         } catch (RuntimeException rex) {
             upcall.setSystemException(new org.omg.CORBA.UNKNOWN());
         }
     }
 
     //
     // Complete the destroy of the POA
     //
     private void completeDestroy() {
         //
         // Wait for all pending requests to terminate. If the POA has
         // destruction has already completed waitPendingRequests
         // returns false.
         //
         if (poaControl_.waitPendingRequests()) {
             //
             // Remove this POA from our parent
             //
             if (parent_ != null) {
                 synchronized (parent_.children_) {
                     org.apache.yoko.orb.OB.Assert._OB_assert(parent_.children_
                             .containsKey(name_));
                     parent_.children_.remove(name_);
                 }
             }
 
             //
             // Unregister from the POAManager If the manager_ is nil then
             // the POA is being destroyed on startup.
             //
             if (manager_ != null) {
                 POAManager_impl m = (POAManager_impl) manager_;
                 m._OB_removePOA(poaId_);
                 manager_ = null;
             }
 
             //
             // Call the adapter_state_change hook
             //
             if (callAdapterStateChange_) {
                 org.omg.PortableInterceptor.ObjectReferenceTemplate[] orts = new org.omg.PortableInterceptor.ObjectReferenceTemplate[childTemplates_
                         .size()];
                 childTemplates_.copyInto(orts);
 
                 org.apache.yoko.orb.OB.PIManager piManager = orbInstance_
                         .getPIManager();
                 piManager.adapterStateChange(orts,
                         org.omg.PortableInterceptor.NON_EXISTENT.value);
             }
 
             //
             // Destroy the servant location strategy
             //
             servantLocationStrategy_.destroy(this, poaControl_.etherealize());
 
             //
             // Clear internal state variables
             //
             adapterActivator_.destroy();
 
             //
             // Clear the ORB reference
             //
             orb_ = null;
 
             //
             // Mark the destroy as complete
             //
             poaControl_.markDestroyCompleted();
 
             //
             // Release the IORInfo object now
             //
             iorInfo_ = null;
         }
     }
 
     //
     // Common initialization code
     //
     private void init() {
         //
         // Set the POA create time
         //
         poaCreateTime_ = (int) (System.currentTimeMillis() / 1000);
 
         //
         // Setup the adapter id. This ID is unique for the lifespan of
         // this POA.
         //
         // TODO: It would be nicer if this didn't use CreateObjectKey
         //
         byte[] oid = new byte[0];
         org.apache.yoko.orb.OB.ObjectKeyData data = new org.apache.yoko.orb.OB.ObjectKeyData(
                 serverId_,
                 poaId_,
                 oid,
                 policies_.lifespanPolicy() == org.omg.PortableServer.LifespanPolicyValue.PERSISTENT,
                 poaCreateTime_);
         adapterId_ = org.apache.yoko.orb.OB.ObjectKey.CreateObjectKey(data);
 
         //
         // Setup the object-reference templates for this POA. This
         // involves running the IORInterceptor to establish the IOR
         // components, and after calling the components_established
         // interception point.
         //
         org.apache.yoko.orb.OBPortableServer.POAManager_impl m = (org.apache.yoko.orb.OBPortableServer.POAManager_impl) manager_;
 
         org.apache.yoko.orb.OCI.Acceptor[] acceptors = m._OB_getAcceptors();
 
         //
         // Create the IORInfo for this POA
         //
         org.apache.yoko.orb.PortableInterceptor.IORInfo_impl iorInfoImpl = new org.apache.yoko.orb.PortableInterceptor.IORInfo_impl(
                 orbInstance_, acceptors, rawPolicies_, policies_, m._OB_getAdapterManagerId(), m
                         ._OB_getAdapterState());
         iorInfo_ = iorInfoImpl;
 
         //
         // Establish the components to place in IORs generated by this
         // POA.
         //
         org.apache.yoko.orb.OB.PIManager piManager = orbInstance_
                 .getPIManager();
         piManager.establishComponents(iorInfo_);
 
         //
         // Once the components have been established the IORTemplate can
         // be created for this POA.
         //
         org.omg.IOP.IOR iorTemplate = new org.omg.IOP.IOR();
         iorTemplate.profiles = new org.omg.IOP.TaggedProfile[0];
         iorTemplate.type_id = new String();
 
         org.omg.IOP.IORHolder iorH = new org.omg.IOP.IORHolder(iorTemplate);
         iorInfoImpl._OB_addComponents(iorH, m._OB_getGIOPVersion());
 
         //
         // Create the primary, secondary and current object-reference
         // template.
         //
         String orbId = orbInstance_.getOrbId();
         String serverId = orbInstance_.getServerId();
         if (policies_.lifespanPolicy() == org.omg.PortableServer.LifespanPolicyValue.PERSISTENT)
             adapterTemplate_ = new org.apache.yoko.orb.OBPortableInterceptor.PersistentORT_impl(
                     orbInstance_, serverId, orbId, poaId_, iorTemplate);
         else
             adapterTemplate_ = new org.apache.yoko.orb.OBPortableInterceptor.TransientORT_impl(
                     orbInstance_, serverId, orbId, poaId_, poaCreateTime_,
                     iorTemplate);
 
         //
         // Set the primary template
         //
         iorInfoImpl._OB_adapterTemplate(adapterTemplate_);
 
         //
         // Call the componentsEstablished callback. This method can throw
         // an exception.
         //
         piManager.componentsEstablished(iorInfo_);
 
         //
         // After this point no exception should be thrown
         //
 
         //
         // Create the id generation strategy
         //
         idGenerationStrategy_ = IdGenerationStrategyFactory
                 .createIdGenerationStrategy(policies_);
 
         //
         // Create the adapter activator holder
         //
         adapterActivator_ = new AdapterActivatorHolder();
 
         //
         // Create the POA control
         //
         poaControl_ = new POAControl();
 
         //
         // Initialize the ServantLocationStategy
         //
         servantLocationStrategy_ = ServantLocationStrategyFactory
                 .createServantLocationStrategy(policies_, orbInstance_);
 
         //
         // Initialize the POA child hashtable
         //
         children_ = new java.util.Hashtable(31);
 
         //
         // Initialize the dispatch strategy
         //
         dispatchStrategy_ = policies_.dispatchStrategyPolicy();
 
         //
         // Register with the POAManager
         //
         m._OB_addPOA(this, poaId_);
     }
 
     // ----------------------------------------------------------------------
     // POA_impl constructors
     // ----------------------------------------------------------------------
 
     //
     // Constructor for any POA other than the root
     //
     private POA_impl(org.omg.CORBA.ORB orb,
             org.apache.yoko.orb.OB.ORBInstance orbInstance, String serverId,
             String name, POA_impl parent, POAManager manager,
             org.apache.yoko.orb.PortableServer.Current_impl poaCurrent,
             org.apache.yoko.orb.OCI.Current_impl ociCurrent,
             POAPolicies policies, org.omg.CORBA.Policy[] rawPolicies) {
         orb_ = orb;
         orbInstance_ = orbInstance;
         serverId_ = serverId;
         name_ = name;
         parent_ = parent;
         manager_ = manager;
         policies_ = policies;
         poaCurrent_ = poaCurrent;
         ociCurrent_ = ociCurrent;
         rawPolicies_ = rawPolicies;
         adapterTemplate_ = null;
         currentFactory_ = null;
         ort_ = null;
         childTemplates_ = new java.util.Vector();
         callAdapterStateChange_ = true;
 
         //
         // Set my POA id
         //
         org.apache.yoko.orb.OB.Assert._OB_assert(parent != null);
         poaId_ = new String[parent.poaId_.length + 1];
         System.arraycopy(parent.poaId_, 0, poaId_, 0, parent.poaId_.length);
         poaId_[parent.poaId_.length] = name;
 
         init();
     }
 
     //
     // Constructor for the root POA
     //
     public POA_impl(org.omg.CORBA.ORB orb,
             org.apache.yoko.orb.OB.ORBInstance orbInstance, String name,
             POAManager manager) {
         orb_ = orb;
         orbInstance_ = orbInstance;
         serverId_ = name;
         name_ = name;
         parent_ = null;
         manager_ = manager;
         policies_ = null;
         poaCurrent_ = null;
         ociCurrent_ = null;
         adapterTemplate_ = null;
         currentFactory_ = null;
         ort_ = null;
         childTemplates_ = new java.util.Vector();
         callAdapterStateChange_ = true;
 
         org.omg.CORBA.Policy[] pl = new org.omg.CORBA.Policy[1];
         pl[0] = new org.apache.yoko.orb.PortableServer.ImplicitActivationPolicy_impl(
                 org.omg.PortableServer.ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION);
 
         //
         // Create the policies_ member
         //
         policies_ = new POAPolicies(orbInstance_, pl);
 
         //
         // This is the default externally visible for the RootPOA
         //
         rawPolicies_ = new org.omg.CORBA.Policy[7];
         rawPolicies_[0] = new org.apache.yoko.orb.PortableServer.ThreadPolicy_impl(
                 org.omg.PortableServer.ThreadPolicyValue.ORB_CTRL_MODEL);
         rawPolicies_[1] = new org.apache.yoko.orb.PortableServer.LifespanPolicy_impl(
                 org.omg.PortableServer.LifespanPolicyValue.TRANSIENT);
         rawPolicies_[2] = new org.apache.yoko.orb.PortableServer.IdUniquenessPolicy_impl(
                 org.omg.PortableServer.IdUniquenessPolicyValue.UNIQUE_ID);
         rawPolicies_[3] = new org.apache.yoko.orb.PortableServer.IdAssignmentPolicy_impl(
                 org.omg.PortableServer.IdAssignmentPolicyValue.SYSTEM_ID);
         rawPolicies_[4] = new org.apache.yoko.orb.PortableServer.ServantRetentionPolicy_impl(
                 org.omg.PortableServer.ServantRetentionPolicyValue.RETAIN);
         rawPolicies_[5] = new org.apache.yoko.orb.PortableServer.RequestProcessingPolicy_impl(
                 org.omg.PortableServer.RequestProcessingPolicyValue.USE_ACTIVE_OBJECT_MAP_ONLY);
         rawPolicies_[6] = new org.apache.yoko.orb.PortableServer.ImplicitActivationPolicy_impl(
                 org.omg.PortableServer.ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION);
 
         //
         // Create the POA and OCI Current implementation object
         //
         poaCurrent_ = new org.apache.yoko.orb.PortableServer.Current_impl();
         ociCurrent_ = new org.apache.yoko.orb.OCI.Current_impl();
 
         //
         // Register the POA::Current, and the OCI::Current classes with
         // the ORB
         //
         org.apache.yoko.orb.OB.InitialServiceManager ism = orbInstance_
                 .getInitialServiceManager();
         try {
             ism.addInitialReference("POACurrent", poaCurrent_);
             ism.addInitialReference("OCICurrent", ociCurrent_);
         } catch (org.omg.CORBA.ORBPackage.InvalidName ex) {
             org.apache.yoko.orb.OB.Assert._OB_assert(ex);
         }
 
         //
         // Set the POAID
         //
         poaId_ = new String[0];
 
         //
         // Initialize the POA
         //
         init();
     }
 
     protected void finalize() throws Throwable {
         org.apache.yoko.orb.OB.Assert._OB_assert(poaControl_.getDestroyed());
 
         super.finalize();
     }
 
     // ----------------------------------------------------------------------
     // Standard IDL to Java mapping
     // ----------------------------------------------------------------------
 
     public org.omg.PortableServer.POA create_POA(String adapter,
             org.omg.PortableServer.POAManager manager,
             org.omg.CORBA.Policy[] rawPolicies)
             throws org.omg.PortableServer.POAPackage.AdapterAlreadyExists,
             org.omg.PortableServer.POAPackage.InvalidPolicy {
         org.apache.yoko.orb.OB.Assert._OB_assert(adapter != null);
 
         //
         // Has the POA been destroyed?
         //
         if (poaControl_.getDestroyed())
             throw new org.omg.CORBA.OBJECT_NOT_EXIST("POA has been destroyed");
 
         //
         // Are the requested policies valid?
         //
         POAPolicies policies = new POAPolicies(orbInstance_, rawPolicies);
         org.omg.CORBA.IntHolder idx = new org.omg.CORBA.IntHolder();
         if (!validatePolicies(policies, rawPolicies, idx))
             throw new org.omg.PortableServer.POAPackage.InvalidPolicy(
                     (short) idx.value);
 
         POA_impl child = null;
 
         //
         // It's not possible to a POA that already exists. If the POA is
         // being destroyed an AdapterAlreadyExists exception is still
         // thrown -- the application has to be capable of dealing with this
         // (by calling create_POA again)
         //
         synchronized (children_) {
             if (children_.containsKey(adapter))
                 throw new org.omg.PortableServer.POAPackage.AdapterAlreadyExists();
 
             //
             // If necessary create a new POAManager for this POA
             //
             POAManager obmanager = null;
             if (manager == null) {
                 try {
                     org.apache.yoko.orb.OB.InitialServiceManager ism = orbInstance_
                             .getInitialServiceManager();
                     POAManagerFactory factory = POAManagerFactoryHelper
                             .narrow(ism
                                     .resolveInitialReferences("POAManagerFactory"));
                     org.omg.CORBA.Policy[] emptyPl = new org.omg.CORBA.Policy[0];
                     obmanager = (org.apache.yoko.orb.OBPortableServer.POAManager) factory
                            .create_POAManager(adapter, emptyPl);
                 } catch (org.omg.CORBA.ORBPackage.InvalidName ex) {
                     org.apache.yoko.orb.OB.Assert._OB_assert(ex);
                 } catch (org.omg.PortableServer.POAManagerFactoryPackage.ManagerAlreadyExists ex) {
                     org.apache.yoko.orb.OB.Assert._OB_assert(ex);
                 }
                 // catch(org.apache.yoko.orb.OCI.InvalidParam ex)
                 // {
                 // org.apache.yoko.orb.OB.Assert._OB_assert(ex);
                 // }
                 catch (org.omg.CORBA.PolicyError ex) {
                     org.apache.yoko.orb.OB.Assert._OB_assert(ex);
                 }
             } else {
                 try {
                     obmanager = POAManagerHelper.narrow(manager);
                 } catch (org.omg.CORBA.BAD_PARAM ex) {
                     org.apache.yoko.orb.OB.Assert._OB_assert(ex);
                 }
             }
 
             //
             // Create the new POA
             //
             try {
                 child = new POA_impl(orb_, orbInstance_, serverId_, adapter,
                         this, obmanager, poaCurrent_, ociCurrent_, policies,
                         rawPolicies);
             } catch (org.omg.CORBA.SystemException ex) {
                 //
                 // If the creation of the POA fails and a new POAManager
                 // was created the deactivate the POAManager
                 //
                 if (manager == null) {
                     org.apache.yoko.orb.OB.Assert._OB_assert(obmanager != null);
                     try {
                         obmanager.deactivate(true, true);
                     } catch (org.omg.PortableServer.POAManagerPackage.AdapterInactive e) {
                     }
                 }
                 throw ex;
             }
 
             //
             // Add this to the child list
             //
             children_.put(adapter, child);
         }
 
         return child;
     }
 
     public org.omg.PortableServer.POA find_POA(String adapter, boolean activate)
             throws org.omg.PortableServer.POAPackage.AdapterNonExistent {
         org.apache.yoko.orb.OB.Assert._OB_assert(adapter != null);
 
         if (poaControl_.getDestroyed())
             throw new org.omg.CORBA.OBJECT_NOT_EXIST("POA has been destroyed");
 
         boolean check = true;
 
         //
         // If the child isn't registered, and we've been asked to
         // activate the POA and we have an AdapterActivator and the
         // POAManager's state is active then we activate the POA (see
         // below requirement)
         //
         // From 11-18:
         //
         // In addition, when a POA manager is in the holding state,
         // the adapter activators registered with the associated POAs
         // will not get called.
         //
         if (!children_.containsKey(adapter) && activate) {
             org.omg.PortableServer.AdapterActivator adapterActivator = adapterActivator_
                     .getAdapterActivator();
             if (adapterActivator != null) {
                 //
                 // From the spec:
                 //
                 // Creating a POA using a POA manager that is in the
                 // active state can lead to race conditions if the POA
                 // supports preexisting objects, because the new POA
                 // may receive a request before its adapter activator,
                 // servant manager, or default servant have been
                 // initialized. These problems do not occur if the POA
                 // is created by an adapter activator registered with
                 // a parent of the new POA, because requests are
                 // queued until the adapter activator returns. To
                 // avoid these problems when a POA must be explicitly
                 // initialized, the application can initialize the POA
                 // by invoking find_POA with a TRUE activate
                 // parameter.
                 //
                 // TODO: This requirement is not met
                 //
                 try {
                     check = adapterActivator.unknown_adapter(this, adapter);
                 } catch (org.omg.CORBA.SystemException ex) {
                     //
                     // 11.3.3.2:
                     //
                     // If unknown_adapter raises a system exception, the ORB
                     // will report an OBJ_ADAPTER system exception with
                     // standard minor code 1.
                     //
                     throw new org.omg.CORBA.OBJ_ADAPTER(
                             org.apache.yoko.orb.OB.MinorCodes
                                     .describeObjAdapter(org.apache.yoko.orb.OB.MinorCodes.MinorSystemExceptionInUnknownAdapter),
                             org.apache.yoko.orb.OB.MinorCodes.MinorSystemExceptionInUnknownAdapter,
                             org.omg.CORBA.CompletionStatus.COMPLETED_NO);
                 }
             }
         }
 
         //
         // Lookup the POA in our set of children.
         //
         org.omg.PortableServer.POA poa = null;
         if (check)
             poa = (org.omg.PortableServer.POA) children_.get(adapter);
 
         if (poa == null)
             throw new org.omg.PortableServer.POAPackage.AdapterNonExistent();
 
         return poa;
     }
 
     public void destroy(boolean etherealize, boolean waitForCompletion) {
         _OB_destroy(etherealize, waitForCompletion, null);
     }
 
     // ----------------------------------------------------------------------
     // Factories for policy objects
     // ----------------------------------------------------------------------
 
     //
     // These methods don't bother to check to see if the POA has
     // already been destroyed
     //
 
     public org.omg.PortableServer.ThreadPolicy create_thread_policy(
             org.omg.PortableServer.ThreadPolicyValue value) {
         return new org.apache.yoko.orb.PortableServer.ThreadPolicy_impl(value);
     }
 
     public org.omg.PortableServer.LifespanPolicy create_lifespan_policy(
             org.omg.PortableServer.LifespanPolicyValue value) {
         return new org.apache.yoko.orb.PortableServer.LifespanPolicy_impl(value);
     }
 
     public org.omg.PortableServer.IdUniquenessPolicy create_id_uniqueness_policy(
             org.omg.PortableServer.IdUniquenessPolicyValue value) {
         return new org.apache.yoko.orb.PortableServer.IdUniquenessPolicy_impl(
                 value);
     }
 
     public org.omg.PortableServer.IdAssignmentPolicy create_id_assignment_policy(
             org.omg.PortableServer.IdAssignmentPolicyValue value) {
         return new org.apache.yoko.orb.PortableServer.IdAssignmentPolicy_impl(
                 value);
     }
 
     public org.omg.PortableServer.ImplicitActivationPolicy create_implicit_activation_policy(
             org.omg.PortableServer.ImplicitActivationPolicyValue value) {
         return new org.apache.yoko.orb.PortableServer.ImplicitActivationPolicy_impl(
                 value);
     }
 
     public org.omg.PortableServer.ServantRetentionPolicy create_servant_retention_policy(
             org.omg.PortableServer.ServantRetentionPolicyValue value) {
         return new org.apache.yoko.orb.PortableServer.ServantRetentionPolicy_impl(
                 value);
     }
 
     public org.omg.PortableServer.RequestProcessingPolicy create_request_processing_policy(
             org.omg.PortableServer.RequestProcessingPolicyValue value) {
         return new org.apache.yoko.orb.PortableServer.RequestProcessingPolicy_impl(
                 value);
     }
 
     public SynchronizationPolicy create_synchronization_policy(
             SynchronizationPolicyValue value) {
         return new SynchronizationPolicy_impl(value);
     }
 
     public DispatchStrategyPolicy create_dispatch_strategy_policy(
             org.apache.yoko.orb.OB.DispatchStrategy value) {
         return new DispatchStrategyPolicy_impl(value);
     }
 
     // ----------------------------------------------------------------------
     // POA attributes
     // ----------------------------------------------------------------------
 
     public String the_name() {
         if (poaControl_.getDestroyed())
             throw new org.omg.CORBA.OBJECT_NOT_EXIST("POA has been destroyed");
 
         return name_;
     }
 
     public org.omg.PortableServer.POA the_parent() {
         if (poaControl_.getDestroyed())
             throw new org.omg.CORBA.OBJECT_NOT_EXIST("POA has been destroyed");
 
         return parent_;
     }
 
     public org.omg.PortableServer.POA[] the_children() {
         if (poaControl_.getDestroyed())
             throw new org.omg.CORBA.OBJECT_NOT_EXIST("POA has been destroyed");
 
         //
         // Since its possible that the children list changes while
         // this method call is in progress its necessary to check the
         // return value of Hashtable::get.
         //
         java.util.Vector content = new java.util.Vector();
         java.util.Enumeration e = children_.elements();
         while (e.hasMoreElements()) {
             org.omg.PortableServer.POA child = (org.omg.PortableServer.POA) e
                     .nextElement();
             if (child != null)
                 content.addElement(child);
         }
         org.omg.PortableServer.POA[] children = new org.omg.PortableServer.POA[content
                 .size()];
         content.copyInto(children);
 
         return children;
     }
 
     public org.omg.PortableServer.POAManager the_POAManager() {
         if (poaControl_.getDestroyed())
             throw new org.omg.CORBA.OBJECT_NOT_EXIST("POA has been destroyed");
 
         return manager_;
     }
 
     public org.omg.PortableServer.POAManagerFactory the_POAManagerFactory() {
         return orbInstance_.getPOAManagerFactory();
     }
 
     public org.omg.PortableServer.AdapterActivator the_activator() {
         if (poaControl_.getDestroyed())
             throw new org.omg.CORBA.OBJECT_NOT_EXIST("POA has been destroyed");
 
         return adapterActivator_.getAdapterActivator();
     }
 
     public void the_activator(org.omg.PortableServer.AdapterActivator activator) {
         if (poaControl_.getDestroyed())
             throw new org.omg.CORBA.OBJECT_NOT_EXIST("POA has been destroyed");
 
         adapterActivator_.setAdapterActivator(activator);
     }
 
     public org.omg.CORBA.Policy[] the_policies() {
         return policies_.recreate();
     }
 
     public org.apache.yoko.orb.OB.DispatchStrategy the_dispatch_strategy() {
         return policies_.dispatchStrategyPolicy();
     }
 
     public org.omg.PortableInterceptor.ObjectReferenceTemplate adapter_template() {
         return adapterTemplate_;
     }
 
     public org.omg.PortableInterceptor.ObjectReferenceFactory current_factory() {
         if (iorInfo_ != null) {
             try {
                 return ((org.apache.yoko.orb.PortableInterceptor.IORInfo_impl) iorInfo_)
                         .current_factory();
             } catch (ClassCastException e) {
                 return iorInfo_.current_factory();
             }
         }
         return null;
     }
 
     public org.omg.CORBA.ORB the_ORB() {
         return orb_;
     }
 
     // ----------------------------------------------------------------------
     // Servant manager registration
     // ----------------------------------------------------------------------
 
     public org.omg.PortableServer.ServantManager get_servant_manager()
             throws org.omg.PortableServer.POAPackage.WrongPolicy {
         if (poaControl_.getDestroyed())
             throw new org.omg.CORBA.OBJECT_NOT_EXIST("POA has been destroyed");
 
         ServantManagerStrategy servantManagerStrategy = servantLocationStrategy_
                 .getServantManagerStrategy();
 
         if (servantManagerStrategy == null)
             throw new org.omg.PortableServer.POAPackage.WrongPolicy();
 
         return servantManagerStrategy.getServantManager();
     }
 
     public void set_servant_manager(org.omg.PortableServer.ServantManager mgr)
             throws org.omg.PortableServer.POAPackage.WrongPolicy {
         if (poaControl_.getDestroyed())
             throw new org.omg.CORBA.OBJECT_NOT_EXIST("POA has been destroyed");
 
         ServantManagerStrategy servantManagerStrategy = servantLocationStrategy_
                 .getServantManagerStrategy();
 
         if (servantManagerStrategy == null)
             throw new org.omg.PortableServer.POAPackage.WrongPolicy();
 
         servantManagerStrategy.setServantManager(mgr);
     }
 
     public org.omg.PortableServer.Servant get_servant()
             throws org.omg.PortableServer.POAPackage.NoServant,
             org.omg.PortableServer.POAPackage.WrongPolicy {
         if (poaControl_.getDestroyed())
             throw new org.omg.CORBA.OBJECT_NOT_EXIST("POA has been destroyed");
 
         DefaultServantHolder defaultServantHolder = servantLocationStrategy_
                 .getDefaultServantHolder();
 
         if (defaultServantHolder == null)
             throw new org.omg.PortableServer.POAPackage.WrongPolicy();
 
         org.omg.PortableServer.Servant servant = defaultServantHolder
                 .getDefaultServant();
 
         if (servant == null)
             throw new org.omg.PortableServer.POAPackage.NoServant();
         return servant;
     }
 
     public void set_servant(org.omg.PortableServer.Servant servant)
             throws org.omg.PortableServer.POAPackage.WrongPolicy {
         org.apache.yoko.orb.OB.Assert._OB_assert(servant != null);
 
         if (poaControl_.getDestroyed())
             throw new org.omg.CORBA.OBJECT_NOT_EXIST("POA has been destroyed");
 
         DefaultServantHolder defaultServantHolder = servantLocationStrategy_
                 .getDefaultServantHolder();
         if (defaultServantHolder == null)
             throw new org.omg.PortableServer.POAPackage.WrongPolicy();
 
         ((org.omg.CORBA_2_3.ORB) orbInstance_.getORB()).set_delegate(servant);
 
         defaultServantHolder.setDefaultServant(servant);
     }
 
     public byte[] activate_object(org.omg.PortableServer.Servant servant)
             throws org.omg.PortableServer.POAPackage.ServantAlreadyActive,
             org.omg.PortableServer.POAPackage.WrongPolicy {
         org.apache.yoko.orb.OB.Assert._OB_assert(servant != null);
 
         if (poaControl_.getDestroyed())
             throw new org.omg.CORBA.OBJECT_NOT_EXIST("POA has been destroyed");
 
         byte[] oid = idGenerationStrategy_.createId();
 
         try {
             servantLocationStrategy_.activate(oid, servant);
         } catch (org.omg.PortableServer.POAPackage.ObjectAlreadyActive ex) {
             org.apache.yoko.orb.OB.Assert._OB_assert(ex); // Should not
                                                                 // occur
         }
 
         return oid;
     }
 
     public void notifyRouters(boolean activate, byte[] oid) {
         //
         // Get the list of routers for this target
         //
         org.omg.MessageRouting.RouterListHolder configRouterList = new org.omg.MessageRouting.RouterListHolder();
         configRouterList.value = new org.omg.MessageRouting.Router[0];
         org.apache.yoko.orb.OB.MessageRoutingUtil.getRouterListFromConfig(
                 orbInstance_, configRouterList);
 
         int numRouters = configRouterList.value.length;
         for (int i = 0; i < numRouters; ++i) {
             org.omg.MessageRouting.Router curRouter = configRouterList.value[i];
 
             //
             // Get the router admin
             //
             org.omg.MessageRouting.RouterAdmin routerAdmin = null;
             try {
                 routerAdmin = curRouter.admin();
             } catch (org.omg.CORBA.SystemException ex) {
             }
 
             //
             // Only continue if the router could be contacted and a valid
             // router admin object is available
             //
             if (routerAdmin != null) {
                 org.omg.CORBA.Object dest;
                 try {
                     dest = id_to_reference(oid);
                 } catch (org.omg.PortableServer.POAPackage.ObjectNotActive ex) {
                     break;
                 } catch (org.omg.PortableServer.POAPackage.WrongPolicy ex) {
                     break;
                 }
 
                 if (activate) {
                     org.apache.yoko.orb.OBMessageRouting.ImmediateSuspendPolicy_impl retryPolicy = new org.apache.yoko.orb.OBMessageRouting.ImmediateSuspendPolicy_impl();
 
                     int decaySeconds = 0;
                     org.apache.yoko.orb.OBMessageRouting.DecayPolicy_impl decayPolicy = new org.apache.yoko.orb.OBMessageRouting.DecayPolicy_impl(
                             decaySeconds);
 
                     //
                     // The object itself must be registered with the router
                     // admin. The 'oid' parameter can be used to create a
                     // reference for this object.
                     //
                     try {
                         routerAdmin.register_destination(dest, false,
                                 retryPolicy, decayPolicy);
                     } catch (org.omg.CORBA.SystemException ex) {
                     }
                 } else // deactivate
                 {
                     try {
                         routerAdmin.unregister_destination(dest);
                     } catch (org.omg.MessageRouting.InvalidState ex) {
                     } catch (org.omg.CORBA.SystemException ex) {
                     }
                 }
             }
         }
     }
 
     public void activate_object_with_id(byte[] oid,
             org.omg.PortableServer.Servant servant)
             throws org.omg.PortableServer.POAPackage.ServantAlreadyActive,
             org.omg.PortableServer.POAPackage.ObjectAlreadyActive,
             org.omg.PortableServer.POAPackage.WrongPolicy {
         org.apache.yoko.orb.OB.Assert._OB_assert(servant != null);
 
         if (poaControl_.getDestroyed())
             throw new org.omg.CORBA.OBJECT_NOT_EXIST("POA has been destroyed");
 
         //
         // Validate that the ObjectId is valid
         //
         if (!idGenerationStrategy_.isValid(oid))
             throw new org.omg.CORBA.BAD_PARAM(
                     org.apache.yoko.orb.OB.MinorCodes
                             .describeBadParam(org.apache.yoko.orb.OB.MinorCodes.MinorInvalidObjectId)
                             + ": POA has SYSTEM_ID policy but the object ID was not "
                             + "generated by this POA",
                     org.apache.yoko.orb.OB.MinorCodes.MinorInvalidObjectId,
                     org.omg.CORBA.CompletionStatus.COMPLETED_NO);
 
         servantLocationStrategy_.activate(oid, servant);
 
         //
         // Notify associated routers of activation
         //
         notifyRouters(true, oid);
     }
 
     public void deactivate_object(byte[] oid)
             throws org.omg.PortableServer.POAPackage.ObjectNotActive,
             org.omg.PortableServer.POAPackage.WrongPolicy {
         if (poaControl_.getDestroyed())
             throw new org.omg.CORBA.OBJECT_NOT_EXIST("POA has been destroyed");
 
         servantLocationStrategy_.deactivate(this, oid);
 
         //
         // Notify associated routers of activation
         //
         notifyRouters(false, oid);
     }
 
     public org.omg.CORBA.Object create_reference(String intf)
             throws org.omg.PortableServer.POAPackage.WrongPolicy {
         org.apache.yoko.orb.OB.Assert._OB_assert(intf != null);
 
         if (poaControl_.getDestroyed())
             throw new org.omg.CORBA.OBJECT_NOT_EXIST("POA has been destroyed");
 
         byte[] oid = idGenerationStrategy_.createId();
         return ort().make_object(intf, oid);
     }
 
     public org.omg.CORBA.Object create_reference_with_id(byte[] oid, String intf) {
         org.apache.yoko.orb.OB.Assert._OB_assert(intf != null);
 
         if (poaControl_.getDestroyed())
             throw new org.omg.CORBA.OBJECT_NOT_EXIST("POA has been destroyed");
 
         //
         // Validate that the ObjectId is valid
         //
         if (!idGenerationStrategy_.isValid(oid))
             throw new org.omg.CORBA.BAD_PARAM(
                     org.apache.yoko.orb.OB.MinorCodes
                             .describeBadParam(org.apache.yoko.orb.OB.MinorCodes.MinorInvalidObjectId)
                             + ": POA has SYSTEM_ID policy but the object ID was not "
                             + "generated by this POA",
                     org.apache.yoko.orb.OB.MinorCodes.MinorInvalidObjectId,
                     org.omg.CORBA.CompletionStatus.COMPLETED_NO);
 
         return ort().make_object(intf, oid);
     }
 
     public byte[] servant_to_id(org.omg.PortableServer.Servant servant)
             throws org.omg.PortableServer.POAPackage.ServantNotActive,
             org.omg.PortableServer.POAPackage.WrongPolicy {
         org.apache.yoko.orb.OB.Assert._OB_assert(servant != null);
 
         if (poaControl_.getDestroyed())
             throw new org.omg.CORBA.OBJECT_NOT_EXIST("POA has been destroyed");
 
         //
         // Requires USE_DEFAULT_SERVANT policy or RETAIN policy and
         // either the UNIQUE_ID policy or the IMPLICIT_ACTIVATION (w/
         // SYSTEM_ID) policies.
         //
         if (policies_.requestProcessingPolicy() != org.omg.PortableServer.RequestProcessingPolicyValue.USE_DEFAULT_SERVANT
                 && (policies_.servantRetentionPolicy() != org.omg.PortableServer.ServantRetentionPolicyValue.RETAIN || (policies_
                         .idUniquenessPolicy() != org.omg.PortableServer.IdUniquenessPolicyValue.UNIQUE_ID && policies_
                         .implicitActivationPolicy() != org.omg.PortableServer.ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION)))
             throw new org.omg.PortableServer.POAPackage.WrongPolicy();
 
         byte[] oid = servantLocationStrategy_.servantToId(servant, poaCurrent_);
 
         if (oid == null) {
             //
             // If the POA doesn't have the IMPLICIT_ACTIVATION
             // (w/ SYSTEM_ID) then a ServantNotActive exception
             //
             if (policies_.implicitActivationPolicy() != org.omg.PortableServer.ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION)
                 throw new org.omg.PortableServer.POAPackage.ServantNotActive();
 
             try {
                 oid = activate_object(servant);
             } catch (org.omg.PortableServer.POAPackage.ServantAlreadyActive ex) {
                 org.apache.yoko.orb.OB.Assert._OB_assert(ex); // Should
                                                                     // not occur
             }
         }
 
         return oid;
     }
 
     public org.omg.CORBA.Object servant_to_reference(
             org.omg.PortableServer.Servant servant)
             throws org.omg.PortableServer.POAPackage.ServantNotActive,
             org.omg.PortableServer.POAPackage.WrongPolicy {
         org.apache.yoko.orb.OB.Assert._OB_assert(servant != null);
 
         if (poaControl_.getDestroyed())
             throw new org.omg.CORBA.OBJECT_NOT_EXIST("POA has been destroyed");
 
         //
         // If the operation was invoked in the context of executing
         // request on the specified servant, the reference associated
         // with the current invocation is returned.
         //
         if (poaCurrent_._OB_inUpcall()
                 && poaCurrent_._OB_getServant() == servant) {
             try {
                 byte[] oid = poaCurrent_.get_object_id();
                 String intf = servant._all_interfaces(this, oid)[0];
                 return ort().make_object(intf, oid);
             } catch (org.omg.PortableServer.CurrentPackage.NoContext ex) {
                 org.apache.yoko.orb.OB.Assert._OB_assert(ex);
             }
         }
 
         byte[] oid = servant_to_id(servant);
         String intf = servant._all_interfaces(this, oid)[0];
         return create_reference_with_id(oid, intf);
     }
 
     public org.omg.PortableServer.Servant reference_to_servant(
             org.omg.CORBA.Object reference)
             throws org.omg.PortableServer.POAPackage.ObjectNotActive,
             org.omg.PortableServer.POAPackage.WrongAdapter,
             org.omg.PortableServer.POAPackage.WrongPolicy {
         org.apache.yoko.orb.OB.Assert._OB_assert(reference != null);
 
         if (poaControl_.getDestroyed())
             throw new org.omg.CORBA.OBJECT_NOT_EXIST("POA has been destroyed");
 
         //
         // Requires the RETAIN policy or the USE_DEFAULT_SERVANT
         // policy.
         //
         if (policies_.servantRetentionPolicy() != org.omg.PortableServer.ServantRetentionPolicyValue.RETAIN
                 && policies_.requestProcessingPolicy() != org.omg.PortableServer.RequestProcessingPolicyValue.USE_DEFAULT_SERVANT)
             throw new org.omg.PortableServer.POAPackage.WrongPolicy();
 
         byte[] oid = reference_to_id(reference);
 
         org.omg.PortableServer.Servant servant = servantLocationStrategy_
                 .idToServant(oid, true);
 
         if (servant == null)
             throw new org.omg.PortableServer.POAPackage.ObjectNotActive();
 
         return servant;
     }
 
     public byte[] reference_to_id(org.omg.CORBA.Object reference)
             throws org.omg.PortableServer.POAPackage.WrongAdapter,
             org.omg.PortableServer.POAPackage.WrongPolicy {
         org.apache.yoko.orb.OB.Assert._OB_assert(reference != null);
 
         if (poaControl_.getDestroyed())
             throw new org.omg.CORBA.OBJECT_NOT_EXIST("POA has been destroyed");
 
         org.apache.yoko.orb.CORBA.Delegate d = (org.apache.yoko.orb.CORBA.Delegate) ((org.omg.CORBA.portable.ObjectImpl) reference)
                 ._get_delegate();
         org.omg.IOP.IOR ior = d._OB_IOR();
 
         //
         // Extract the object key from the IOR of the object reference.
         //
         org.apache.yoko.orb.OBPortableServer.POAManager_impl m = (org.apache.yoko.orb.OBPortableServer.POAManager_impl) manager_;
         org.apache.yoko.orb.OCI.Acceptor[] acceptors = m._OB_getAcceptors();
 
         boolean local = false;
         org.apache.yoko.orb.OCI.ProfileInfo[] profileInfos = null;
 
         for (int i = 0; i < acceptors.length && !local; i++) {
             profileInfos = acceptors[i].get_local_profiles(ior);
             if (profileInfos.length > 0)
                 local = true;
         }
 
         //
         // Is the key local?
         //
         if (local) {
             //
             // Verify the poaIds are the same, have the persistency
             // values and the same create time if transient
             //
             org.apache.yoko.orb.OB.ObjectKeyData keyData = new org.apache.yoko.orb.OB.ObjectKeyData();
             if (org.apache.yoko.orb.OB.ObjectKey.ParseObjectKey(
                     profileInfos[0].key, keyData)
                     && _OB_poaMatches(keyData, true)) {
                 return keyData.oid;
             }
         }
 
         throw new org.omg.PortableServer.POAPackage.WrongAdapter();
     }
 
     public org.omg.PortableServer.Servant id_to_servant(byte[] oid)
             throws org.omg.PortableServer.POAPackage.ObjectNotActive,
             org.omg.PortableServer.POAPackage.WrongPolicy {
         if (poaControl_.getDestroyed())
             throw new org.omg.CORBA.OBJECT_NOT_EXIST("POA has been destroyed");
 
         //
         // Requires the RETAIN policy or the USE_DEFAULT_SERVANT policy.
         //
         if (policies_.servantRetentionPolicy() != org.omg.PortableServer.ServantRetentionPolicyValue.RETAIN
                 && policies_.requestProcessingPolicy() != org.omg.PortableServer.RequestProcessingPolicyValue.USE_DEFAULT_SERVANT)
             throw new org.omg.PortableServer.POAPackage.WrongPolicy();
 
         org.omg.PortableServer.Servant servant = servantLocationStrategy_
                 .idToServant(oid, true);
 
         if (servant == null)
             throw new org.omg.PortableServer.POAPackage.ObjectNotActive();
 
         return servant;
     }
 
     public org.omg.CORBA.Object id_to_reference(byte[] oid)
             throws org.omg.PortableServer.POAPackage.ObjectNotActive,
             org.omg.PortableServer.POAPackage.WrongPolicy {
         if (poaControl_.getDestroyed())
             throw new org.omg.CORBA.OBJECT_NOT_EXIST("POA has been destroyed");
 
         // Requires the RETAIN policy
         //
         if (policies_.servantRetentionPolicy() != org.omg.PortableServer.ServantRetentionPolicyValue.RETAIN)
             throw new org.omg.PortableServer.POAPackage.WrongPolicy();
 
         org.omg.PortableServer.Servant servant = servantLocationStrategy_
                 .idToServant(oid, false);
         if (servant == null)
             throw new org.omg.PortableServer.POAPackage.ObjectNotActive();
 
         String intf = servant._all_interfaces(this, oid)[0];
         return ort().make_object(intf, oid);
     }
 
     public byte[] id() {
         if (poaControl_.getDestroyed())
             throw new org.omg.CORBA.OBJECT_NOT_EXIST("POA has been destroyed");
 
         return adapterId_;
     }
 
     // ----------------------------------------------------------------------
     // Yoko specific functions
     // ----------------------------------------------------------------------
 
     public void _OB_preinvoke(String op, byte[] oid,
             org.omg.PortableServer.Servant servant, java.lang.Object cookie,
             org.apache.yoko.orb.OCI.TransportInfo info) {
         //
         // preinvoke the servant location strategy
         //
         servantLocationStrategy_.preinvoke(oid);
 
         //
         // Setup the OCI::Current context
         //
         ociCurrent_._OB_preinvoke(info);
 
         //
         // Setup the PortableServer::Current context
         //
         poaCurrent_._OB_preinvoke(this, servant, op, oid, cookie);
 
         //
         // Depending on the synchronization policy we have to lock a
         // variety of mutexes.
         //
         // TODO: DUMP THIS IF POSSIBLE
         //
         switch (policies_.synchronizationPolicy().value()) {
         case SynchronizationPolicyValue._NO_SYNCHRONIZATION:
             break;
 
         case SynchronizationPolicyValue._SYNCHRONIZE_ON_POA:
             poaSyncMutex_.lock();
             break;
 
         case SynchronizationPolicyValue._SYNCHRONIZE_ON_ORB:
             orbInstance_.getORBSyncMutex().lock();
             break;
         }
     }
 
     public void _OB_postinvoke() {
         byte[] oid = poaCurrent_._OB_getObjectId();
         String op = poaCurrent_._OB_getOp();
         org.omg.PortableServer.Servant servant = poaCurrent_._OB_getServant();
         java.lang.Object cookie = poaCurrent_._OB_getCookie();
 
         //
         // Depending on the synchronization policy we have to unlock a
         // variety of mutexes. Note that we have to do this before
         // calling postinvoke else we might operation on a destroyed
         // servant.
         //
         switch (policies_.synchronizationPolicy().value()) {
         case SynchronizationPolicyValue._NO_SYNCHRONIZATION:
             break;
 
         case SynchronizationPolicyValue._SYNCHRONIZE_ON_POA:
             poaSyncMutex_.unlock();
             break;
 
         case SynchronizationPolicyValue._SYNCHRONIZE_ON_ORB:
             orbInstance_.getORBSyncMutex().unlock();
             break;
         }
 
         //
         // Clean up the PortableServant::Current
         //
         poaCurrent_._OB_postinvoke();
 
         //
         // Clean up the OCI:Current
         //
         ociCurrent_._OB_postinvoke();
 
         //
         // postinvoke the servant location strategy
         //
         servantLocationStrategy_.postinvoke(oid, this, op, cookie, servant);
     }
 
     void _OB_locateServant(byte[] oid)
             throws org.apache.yoko.orb.OB.LocationForward {
         org.omg.PortableServer.ServantLocatorPackage.CookieHolder cookieHolder = new org.omg.PortableServer.ServantLocatorPackage.CookieHolder();
         String op = "_locate";
         org.omg.PortableServer.Servant servant = servantLocationStrategy_
                 .locate(oid, this, op, cookieHolder);
 
         if (servant == null)
             throw new org.omg.CORBA.OBJECT_NOT_EXIST(
                     org.apache.yoko.orb.OB.MinorCodes
                             .describeObjectNotExist(org.apache.yoko.orb.OB.MinorCodes.MinorCannotDispatch),
                     org.apache.yoko.orb.OB.MinorCodes.MinorCannotDispatch,
                     org.omg.CORBA.CompletionStatus.COMPLETED_NO);
 
         servantLocationStrategy_.preinvoke(oid);
         servantLocationStrategy_.postinvoke(oid, this, op, cookieHolder.value,
                 servant);
     }
 
     //
     // Create the upcall object for a method invocation
     //
     org.apache.yoko.orb.OB.Upcall _OB_createUpcall(byte[] oid,
             org.apache.yoko.orb.OB.UpcallReturn upcallReturn,
             org.apache.yoko.orb.OCI.ProfileInfo profileInfo,
             org.apache.yoko.orb.OCI.TransportInfo transportInfo, int requestId,
             String op, org.apache.yoko.orb.CORBA.InputStream in,
             org.omg.IOP.ServiceContext[] requestSCL)
             throws org.apache.yoko.orb.OB.LocationForward {
         org.apache.yoko.orb.OB.Upcall upcall = null;
 
         //
         // Increment the outstanding request count
         //
         if (poaControl_.incrementRequestCount()) {
             try {
                 //
                 // Create the upcall object
                 //
                 if (policies_.interceptorCallPolicy()) {
                     org.apache.yoko.orb.OB.PIManager piManager = orbInstance_
                             .getPIManager();
 
                     if (piManager.haveServerInterceptors()) {
                         org.apache.yoko.orb.OB.PIUpcall piUpcall = new org.apache.yoko.orb.OB.PIUpcall(
                                 orbInstance_, upcallReturn, profileInfo,
                                 transportInfo, requestId, op, in, requestSCL,
                                 piManager);
                         upcall = piUpcall;
 
                         //
                         // Call the receive_request_service_contexts
                         // interception point
                         //
                         piUpcall.receiveRequestServiceContexts(rawPolicies_,
                                 adapterId_, oid, adapterTemplate_);
 
                         piUpcall.contextSwitch();
                     } else {
                         upcall = new org.apache.yoko.orb.OB.Upcall(
                                 orbInstance_, upcallReturn, profileInfo,
                                 transportInfo, requestId, op, in, requestSCL);
                     }
                 } else {
                     upcall = new org.apache.yoko.orb.OB.Upcall(orbInstance_,
                             upcallReturn, profileInfo, transportInfo,
                             requestId, op, in, requestSCL);
                 }
 
                 org.apache.yoko.orb.OB.DispatchRequest_impl dispatchRequestImpl = new org.apache.yoko.orb.OB.DispatchRequest_impl(
                         this, oid, upcall);
 
                 upcall.setDispatchInfo(dispatchRequestImpl, dispatchStrategy_);
             } catch (org.omg.CORBA.SystemException ex) {
                 upcall.setSystemException(ex);
                 _OB_decrementRequestCount();
             } catch (org.apache.yoko.orb.OB.LocationForward ex) {
                 upcall.setLocationForward(ex.ior, ex.perm);
                 _OB_decrementRequestCount();
             }
         }
 
         //
         // If this POA has a BidirPolicy set to BOTH and we have
         // received some listening points in the SCL (which implies that
         // the client has the BidirPolicy as well), then we must make
         // sure to map these in the transportInfo structure
         //
         if (upcall != null)
             _OB_handleBidirSCL(transportInfo, requestSCL);
 
         return upcall;
     }
 
     //
     // Dispatch a method invocation
     //
     public void _OB_dispatch(byte[] oid, org.apache.yoko.orb.OB.Upcall upcall) {
         String op = upcall.operation();
 
         //
         // Notify the PIUpcall about a potential change in thread
         // context. This has to be done before the servant is located
         // since PICurrent information has to be available in servant
         // locators.
         //
         upcall.contextSwitch();
 
         try {
             //
             // Locating a servant can throw LocationForward and
             // SystemException errors
             //
             org.omg.PortableServer.ServantLocatorPackage.CookieHolder cookieHolder = new org.omg.PortableServer.ServantLocatorPackage.CookieHolder();
             org.omg.PortableServer.Servant servant = servantLocationStrategy_
                     .locate(oid, this, op, cookieHolder);
 
             //
             // If there is a servant then dispatch the request
             //
             if (servant != null) {
                 org.apache.yoko.orb.OCI.TransportInfo transportInfo = upcall
                         .transportInfo();
 
                 //
                 // Do any pre invocation stuff
                 //
                 _OB_preinvoke(op, oid, servant, cookieHolder.value,
                         transportInfo);
 
                 //
                 // The Upcall may call _OB_postinvoke
                 //
                 upcall.setServantAndPOA(servant, this);
 
                 //
                 // Dispatch the request
                 //
                 try {
                     ServantDispatcher dispatcher = new ServantDispatcher(
                             upcall, servant);
                     dispatcher.dispatch();
                 } finally {
                     //
                     // If postinvoke() hasn't been called, then do it now,
                     // since it may raise an exception that must be returned
                     // to the client
                     //
                     if (!upcall.postinvokeCalled()) {
                         _OB_postinvoke(); // May raise SystemException
                     }
                 }
             } else if (op.equals("_non_existent") || op.equals("_not_existent")) {
                 upcall.preUnmarshal();
                 upcall.postUnmarshal();
                 upcall.postinvoke();
                 org.omg.CORBA.portable.OutputStream out = upcall.preMarshal();
                 out.write_boolean(true);
                 upcall.postMarshal();
             } else {
                 upcall
                         .setSystemException(new org.omg.CORBA.OBJECT_NOT_EXIST(
                                 org.apache.yoko.orb.OB.MinorCodes
                                         .describeObjectNotExist(org.apache.yoko.orb.OB.MinorCodes.MinorCannotDispatch),
                                 org.apache.yoko.orb.OB.MinorCodes.MinorCannotDispatch,
                                 org.omg.CORBA.CompletionStatus.COMPLETED_NO));
             }
         } catch (org.apache.yoko.orb.OB.LocationForward ex) {
             upcall.setLocationForward(ex.ior, ex.perm);
         }
         /*
          * This can't happen in Java catch(org.omg.CORBA.UserException ex) {
          * upcall.setUserException(ex); }
          */
         catch (org.omg.CORBA.SystemException ex) {
             upcall.setSystemException(ex);
         } catch (Exception ex) {
             handleUnknownException(upcall, ex);
         }
 
         //
         // Java only
         //
         // If the skeleton has marshalled a user exception, it is now
         // safe to call endUserException
         //
         if (upcall.userException())
             upcall.endUserException();
 
         //
         // Decrement the outstanding request count
         //
         _OB_decrementRequestCount();
     }
 
     DirectServant _OB_getDirectServant(byte[] oid,
             org.apache.yoko.orb.OB.RefCountPolicyList policies)
             throws org.apache.yoko.orb.OB.LocationForward {
         return servantLocationStrategy_.createDirectStubImpl(this, oid,
                 policies);
     }
 
     public org.apache.yoko.orb.PortableServer.Current_impl _OB_POACurrent() {
         return poaCurrent_;
     }
 
     public void _OB_removeDirectServant(byte[] oid, DirectServant directServant) {
         servantLocationStrategy_.removeDirectStubImpl(oid, directServant);
     }
 
     //
     // Determine if this POA matches the provided object key
     //
     boolean _OB_poaMatches(org.apache.yoko.orb.OB.ObjectKeyData data,
             boolean full) {
         if (full) {
             //
             // Check server id
             //
             if (!data.serverId.equals(serverId_))
                 return false;
 
             //
             // If the length is incorrect, return false
             //
             if (data.poaId.length != poaId_.length)
                 return false;
 
             //
             // Check each name
             //
             for (int i = 0; i < data.poaId.length; ++i)
                 if (!data.poaId[i].equals(poaId_[i]))
                     return false;
         }
 
         //
         // Is the POA persistent? If so then the ObjectKeyData must be
         // persistent.
         //
         if (policies_.lifespanPolicy() == org.omg.PortableServer.LifespanPolicyValue.PERSISTENT)
             return data.persistent;
 
         //
         // Otherwise this POA is transient. The ObjectKeyData must be
         // transient, and the POA create times must be the same.
         //
         if (data.persistent)
             return false;
         return data.createTime == poaCreateTime_;
     }
 
     //
     // Increment the outstanding number of requests
     //
     public boolean _OB_incrementRequestCount() {
         return poaControl_.incrementRequestCount();
     }
 
     //
     // Decrement the outstanding number of requests
     //
     public void _OB_decrementRequestCount() {
         if (poaControl_.decrementRequestCount())
             completeDestroy();
     }
 
     //
     // Wait for all pending requests to terminate
     //
     public void _OB_waitPendingRequests() {
         poaControl_.waitPendingRequests();
     }
 
     //
     // Get the ORBInstance object
     //
     public org.apache.yoko.orb.OB.ORBInstance _OB_ORBInstance() {
         return orbInstance_;
     }
 
     //
     // Add the policy factory for the POA Policies
     //
     public void _OB_addPolicyFactory() {
         org.apache.yoko.orb.OB.PolicyFactoryManager pfm = orbInstance_
                 .getPolicyFactoryManager();
 
         org.omg.PortableInterceptor.PolicyFactory factory = new org.apache.yoko.orb.PortableInterceptor.POAPolicyFactory_impl();
         pfm.registerPolicyFactory(
                 org.omg.PortableServer.THREAD_POLICY_ID.value, factory, true);
         pfm.registerPolicyFactory(
                 org.omg.PortableServer.LIFESPAN_POLICY_ID.value, factory, true);
         pfm.registerPolicyFactory(
                 org.omg.PortableServer.ID_UNIQUENESS_POLICY_ID.value, factory,
                 true);
         pfm.registerPolicyFactory(
                 org.omg.PortableServer.ID_ASSIGNMENT_POLICY_ID.value, factory,
                 true);
         pfm.registerPolicyFactory(
                 org.omg.PortableServer.IMPLICIT_ACTIVATION_POLICY_ID.value,
                 factory, true);
         pfm.registerPolicyFactory(
                 org.omg.PortableServer.SERVANT_RETENTION_POLICY_ID.value,
                 factory, true);
         pfm.registerPolicyFactory(
                 org.omg.PortableServer.REQUEST_PROCESSING_POLICY_ID.value,
                 factory, true);
         pfm.registerPolicyFactory(SYNCHRONIZATION_POLICY_ID.value, factory,
                 true);
         pfm.registerPolicyFactory(DISPATCH_STRATEGY_POLICY_ID.value, factory,
                 true);
         pfm.registerPolicyFactory(INTERCEPTOR_CALL_POLICY_ID.value, factory,
                 true);
     }
 
     public void _OB_validateManagerState() {
         POAManager_impl m = (POAManager_impl) manager_;
         m._OB_validateState();
     }
 
     public void _OB_etherealize(POAManager_impl manager) {
         //
         // Recursively etherealize children's POA
         //
         java.util.Enumeration e = children_.elements();
         while (e.hasMoreElements()) {
             org.apache.yoko.orb.OBPortableServer.POA_impl child = (org.apache.yoko.orb.OBPortableServer.POA_impl) e
                     .nextElement();
             child._OB_etherealize(manager);
         }
 
         //
         // Eterealize our servants if the POA is assocatied with the
         // POAManager passed in argument.
         //
         if (manager == manager_) {
             servantLocationStrategy_.etherealize(this);
         }
     }
 
     public void _OB_destroy(boolean etherealize, boolean waitForCompletion,
             java.util.Vector templates) {
         if (poaControl_.getDestroyed())
             throw new org.omg.CORBA.OBJECT_NOT_EXIST("POA has been destroyed");
 
         //
         // If waitForCompletion is TRUE and the current thread is in
         // an invocation context dispatched from some POA belonging to
         // the same ORB as this POA, the BAD_INV_ORDER exception is
         // raised and POA destruction does not occur.
         //
         if (waitForCompletion && poaCurrent_._OB_inUpcall()) {
             try {
                 POA_impl p = (POA_impl) poaCurrent_.get_POA();
                 if (p._OB_ORBInstance() == orbInstance_) {
                     throw new org.omg.CORBA.BAD_INV_ORDER(
                             "Invocation in progress");
                 }
             } catch (org.omg.PortableServer.CurrentPackage.NoContext ex) {
             }
         }
 
         //
         // Add our primary template to the template sequence to return to
         // our parent. If templates is null then destroy() was called directly
         // on this POA.
         //
         if (templates != null) {
             templates.addElement(adapter_template());
             callAdapterStateChange_ = false;
         }
 
         //
         // Mark the POA as destroy pending. Only one thread will
         // continue. All others will wait (if waitForCompletion is
         // true) until the POA destruction is complete.
         //
         if (poaControl_.markDestroyPending(etherealize, waitForCompletion)) {
             //
             // Recursively destroy all children
             //
             java.util.Enumeration e = children_.elements();
             while (e.hasMoreElements()) {
                 org.apache.yoko.orb.OBPortableServer.POA_impl child = (org.apache.yoko.orb.OBPortableServer.POA_impl) e
                         .nextElement();
                 if (child != null) {
                     if (templates == null)
                         child._OB_destroy(etherealize, waitForCompletion,
                                 childTemplates_);
                     else
                         child._OB_destroy(etherealize, waitForCompletion,
                                 templates);
                 }
             }
 
             //
             // If waitForCompletion is FALSE, the destroy operation
             // destroys the POA and its children but waits neither for
             // active requests to complete nor for etherealization to
             // occur. If destroy is called multiple times before
             // destruction is complete (because there are active
             // requests), the etherealize_objects parameter applies
             // only to the first call of destroy. Subsequent calls
             // with conflicting etherealize_objects settings use the
             // value of etherealize_objects from the first call. The
             // waitForCompletion parameter is handled as defined above
             // for each individual call (some callers may choose to
             // block, while others may not).
             //
             // We only return if there are outstanding requests. In
             // this case when the last outstanding request is
             // completed then destruction of the the POA will
             // complete.
             //
             if (!waitForCompletion && poaControl_.hasPendingRequests())
                 return;
 
             completeDestroy();
         }
     }
 
     private void _OB_handleBidirSCL(
             org.apache.yoko.orb.OCI.TransportInfo transportInfo,
             org.omg.IOP.ServiceContext[] contexts) {
         if (policies_.bidirPolicy() != org.omg.BiDirPolicy.BOTH.value)
             return;
 
         if (transportInfo != null)
             transportInfo.handle_service_contexts(contexts);
     }
 }
