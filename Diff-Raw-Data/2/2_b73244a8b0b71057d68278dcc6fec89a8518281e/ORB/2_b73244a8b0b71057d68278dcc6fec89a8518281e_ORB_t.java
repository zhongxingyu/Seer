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
 package org.omg.CORBA;
 
 import org.apache.yoko.osgi.ProviderLocator;
 
 import java.security.PrivilegedAction;
 import java.security.AccessController;
 import java.util.Properties;
 
 public abstract class ORB {
 
     public abstract String[] list_initial_services();
 
     public abstract org.omg.CORBA.Object resolve_initial_references(
             String object_name) throws org.omg.CORBA.ORBPackage.InvalidName;
 
     public abstract String object_to_string(org.omg.CORBA.Object object);
 
     public abstract org.omg.CORBA.Object string_to_object(String str);
 
     public abstract NVList create_list(int count);
 
     /**
      * @deprecated Deprecated by CORBA 2.3.
      */
     public abstract NVList create_operation_list(OperationDef oper);
 
     // Empty method for binary compatibility with the 1.5
     public NVList create_operation_list(org.omg.CORBA.Object oper) {return null;};
 
     public abstract NamedValue create_named_value(String name, Any value,
             int flags);
 
     public abstract ExceptionList create_exception_list();
 
     public abstract ContextList create_context_list();
 
     public abstract Context get_default_context();
 
     public abstract Environment create_environment();
 
     public abstract void send_multiple_requests_oneway(Request[] req);
 
     public abstract void send_multiple_requests_deferred(Request[] req);
 
     public abstract boolean poll_next_response();
 
     public abstract Request get_next_response()
             throws org.omg.CORBA.WrongTransaction;
 
     public boolean get_service_information(short service_type,
             ServiceInformationHolder service_info) {
         throw new org.omg.CORBA.NO_IMPLEMENT();
     }
 
     public abstract TypeCode create_struct_tc(String id, String name,
             StructMember[] members);
 
     public abstract TypeCode create_union_tc(String id, String name,
             TypeCode discriminatorType, UnionMember[] members);
 
     public abstract TypeCode create_enum_tc(String id, String name,
             String[] members);
 
     public abstract TypeCode create_alias_tc(String id, String name,
             TypeCode originalType);
 
     public abstract TypeCode create_exception_tc(String id, String name,
             StructMember[] members);
 
     public abstract TypeCode create_interface_tc(String id, String name);
 
     public abstract TypeCode create_string_tc(int bound);
 
     public abstract TypeCode create_wstring_tc(int bound);
 
     public TypeCode create_fixed_tc(short digits, short scale) {
         throw new org.omg.CORBA.NO_IMPLEMENT();
     }
 
     public abstract TypeCode create_sequence_tc(int bound, TypeCode elementType);
 
     /**
      * @deprecated Deprecated by CORBA 2.3.
      */
     public abstract TypeCode create_recursive_sequence_tc(int bound, int offset);
 
     public abstract TypeCode create_array_tc(int length, TypeCode elementType);
 
     public TypeCode create_value_tc(String id, String name,
             short type_modifier, TypeCode concrete_base, ValueMember[] members) {
         throw new org.omg.CORBA.NO_IMPLEMENT();
     }
 
     public TypeCode create_value_box_tc(String id, String name,
             TypeCode boxed_type) {
         throw new org.omg.CORBA.NO_IMPLEMENT();
     }
 
     public TypeCode create_native_tc(String id, String name) {
         throw new org.omg.CORBA.NO_IMPLEMENT();
     }
 
     public TypeCode create_recursive_tc(String id) {
         throw new org.omg.CORBA.NO_IMPLEMENT();
     }
 
     public TypeCode create_abstract_interface_tc(String id, String name) {
         throw new org.omg.CORBA.NO_IMPLEMENT();
     }
 
     public abstract TypeCode get_primitive_tc(TCKind kind);
 
     public boolean work_pending() {
         throw new org.omg.CORBA.NO_IMPLEMENT();
     }
 
     public void perform_work() {
         throw new org.omg.CORBA.NO_IMPLEMENT();
     }
 
     public void run() {
         throw new org.omg.CORBA.NO_IMPLEMENT();
     }
 
     public void shutdown(boolean wait_for_completion) {
         throw new org.omg.CORBA.NO_IMPLEMENT();
     }
 
     public void destroy() {
         throw new org.omg.CORBA.NO_IMPLEMENT();
     }
 
     public abstract Any create_any();
 
     public abstract org.omg.CORBA.portable.OutputStream create_output_stream();
 
     // Empty method for binary compatibility with the 1.5
     public void connect(org.omg.CORBA.Object obj) {};
 
     // Empty method for binary compatibility with the 1.5
     public void disconnect(org.omg.CORBA.Object obj) {};
 
     public org.omg.CORBA.Policy create_policy(int policy_type,
             org.omg.CORBA.Any val) throws org.omg.CORBA.PolicyError {
         throw new org.omg.CORBA.NO_IMPLEMENT();
     }
 
     /**
      * @deprecated Deprecated by CORBA 2.2.
      */
     public Current get_current() {
         throw new org.omg.CORBA.NO_IMPLEMENT();
     }
 
     private static ORB ORBSingleton_;
     private static final String ORBClassPropertyKey = "org.omg.CORBA.ORBClass";
    private static final String ORBSingletonPropertyKey = "org.omg.CORBA.ORBSingletonClass";
 
     public static ORB init(String[] args, java.util.Properties props) {
 
         ORB orb = newOrb(props);
 
         ORBSingleton_ = orb;
 
         orb.set_parameters(args, props);
 
         return orb;
     }
 
     public static ORB init(java.applet.Applet app, java.util.Properties props) {
         ORB orb = newOrb(props);
 
         ORBSingleton_ = orb;
 
         orb.set_parameters(app, props);
 
         return ORBSingleton_;
     }
 
     private static ORB newOrb(Properties props) {
         String orbClassName = null;
 
         if (props != null) {
             orbClassName = props.getProperty(ORBClassPropertyKey);
         }
 
         ORB orb = null;
         if (orbClassName == null) {
             try {
                 orb = (ORB) ProviderLocator.getService(ORBClassPropertyKey, ORB.class, Thread.currentThread().getContextClassLoader());
             } catch (Exception ex) {
                 throw (INITIALIZE)new INITIALIZE("Invalid ORB class from osgi: ").initCause(ex);
             }
         }
 
         if (orb == null) {
             try {
                 if (orbClassName == null)
                     orbClassName = getSystemProperty(ORBClassPropertyKey);
             } catch (SecurityException ex) {
                 // ignore
             }
 
             if (orbClassName == null)
                 orbClassName = "org.apache.yoko.orb.CORBA.ORB";
 
 
             try {
                 // get the appropriate class for the loading.
                 ClassLoader loader = Thread.currentThread().getContextClassLoader();
                 orb = (ORB) ProviderLocator.loadClass(orbClassName, ORB.class, loader).newInstance();
             } catch (Throwable ex) {
                 throw (INITIALIZE)new INITIALIZE("Invalid ORB class: "
                         + orbClassName).initCause(ex);
             }
         }
         return orb;
     }
 
     public static ORB init() {
         if (ORBSingleton_ == null) {
             try {
                 ORBSingleton_ = (ORB) ProviderLocator.getService(ORBSingletonPropertyKey, ORB.class, Thread.currentThread().getContextClassLoader());
             } catch (Exception ex) {
                 throw (org.omg.CORBA.INITIALIZE)new org.omg.CORBA.INITIALIZE(
                         "Invalid ORB singleton class from osgi: ").initCause(ex);
 
             }
             if (ORBSingleton_ == null) {
                 String orbClassName = getSystemProperty(ORBSingletonPropertyKey);
 
                 if (orbClassName == null) {
                     orbClassName = "org.apache.yoko.orb.CORBA.ORBSingleton";
                 }
 
                 try {
                     ORBSingleton_ = (ORB) ProviderLocator.loadClass(orbClassName, ORB.class, Thread.currentThread().getContextClassLoader()).newInstance();
                 } catch (ClassNotFoundException ex) {
                     throw (org.omg.CORBA.INITIALIZE)new org.omg.CORBA.INITIALIZE(
                             "Invalid ORB singleton class from osgi: ").initCause(ex);
                 } catch (InstantiationException ex) {
                     throw (org.omg.CORBA.INITIALIZE)new org.omg.CORBA.INITIALIZE(
                             "Invalid ORB singleton class from osgi: ").initCause(ex);
                 } catch (IllegalAccessException ex) {
                     throw (org.omg.CORBA.INITIALIZE)new org.omg.CORBA.INITIALIZE(
                             "Invalid ORB singleton class from osgi: ").initCause(ex);
                 }
             }
         }
 
         return ORBSingleton_;
     }
 
     protected abstract void set_parameters(String[] args,
             java.util.Properties props);
 
     protected abstract void set_parameters(java.applet.Applet app,
             java.util.Properties props);
 
 
     /**
      * Simple utility for retrieving a system property
      * using the AccessController.
      *
      * @param name   The property name
      *
      * @return The property value.
      */
     private static String getSystemProperty(final String name) {
 	    return (String) AccessController.doPrivileged(
                 new PrivilegedAction() {
                     public java.lang.Object run() {
                         return System.getProperty(name);
                     }
                 }
 		    );
     }
 }
