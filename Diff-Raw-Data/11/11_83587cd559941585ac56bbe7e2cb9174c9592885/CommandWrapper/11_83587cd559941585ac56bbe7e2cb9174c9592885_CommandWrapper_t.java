 /*
  * Copyright (C) 2012 Red Hat, Inc.     
  * 
  * This copyrighted material is made available to anyone wishing to use, 
  * modify, copy, or redistribute it subject to the terms and conditions of the 
  * GNU General Public License v.2.
  * 
  * Authors: Adam Saleh (asaleh at redhat dot com)
  *          Jan Rusnacko (jrusnack at redhat dot com)
  */
 package NitrateIntegration.CommandWrapper;
 
 import NitrateIntegration.TcmsEnvironment;
 import NitrateIntegration.TcmsProperties;
 import com.redhat.nitrate.TcmsCommand;
 import com.redhat.nitrate.TcmsConnection;
 import com.redhat.nitrate.TcmsException;
 import com.redhat.nitrate.command.Build;
 import com.redhat.nitrate.command.TestCase;
 import com.redhat.nitrate.command.TestCaseRun;
 import com.redhat.nitrate.command.TestRun;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.LinkedList;
 import redstone.xmlrpc.XmlRpcStruct;
 
 /**
  *
  * @author asaleh
  */
 /*
  * Yay, dependencies list! Way to shoot yourself to the leg :D
  */
 public abstract class CommandWrapper {
 
     public TcmsCommand current;
     private boolean executable;
     private boolean checked;
     private boolean performed;
 
     enum Status {
 
         UNKNOWN, COMPLETED, DUPLICATE, EXCEPTION, UNMET_DEPENDENCIES, SUBITEM_FAILED
     }
     protected Status status;
     private LinkedList<CommandWrapper> dependecy;
     protected Object result;
     protected Object unexpected;
     protected Class result_type;
     
         protected TcmsProperties properties;
         protected TcmsEnvironment env;
 
     protected interface WrapperConstructor{
         public CommandWrapper create(TcmsCommand current, Class result_type,TcmsProperties properties,TcmsEnvironment env);
     }
     protected static HashMap<Class<TcmsCommand>,WrapperConstructor> wrapperMap = new HashMap<Class<TcmsCommand>, WrapperConstructor>();
     protected static void enlistWrapper(Class commandType,WrapperConstructor wrapper) throws RuntimeException{
         if(wrapperMap.containsKey(commandType)) throw new RuntimeException("Command wrapper already present");
         wrapperMap.put(commandType, wrapper);
     }
     
     public CommandWrapper(TcmsCommand current, Class result_type,TcmsProperties properties,TcmsEnvironment env) {
         this.current = current;
         this.status = Status.UNKNOWN;
         this.checked = true;
         this.performed = false;
         this.executable = true;
         this.dependecy = new LinkedList<CommandWrapper>();
         this.result_type = result_type;
 
         this.properties = properties;
         this.env = env;
         
         result = null;
     }
 
     
     
     public LinkedList<CommandWrapper> getDependecies() {
         return dependecy;
     }
 
     public void addDependecy(CommandWrapper dep) {
         dependecy.push(dep);
     }
 
     /**
      * Returns true iff this command has dependency with which hashCode is equal
      * to parameter passed to method.
      *
      * @param hashCode
      * @return
      */
     public boolean hasDependency(Integer hashCode) {
         for (CommandWrapper w : dependecy) {
             int a = w.hashCode();
             if (hashCode.equals(w.hashCode())) {
                 return true;
             }
         }
         return false;
     }
 
     public boolean resolved() {
         if (dependecy.size() == 0) {
             return true;
         }
         for (CommandWrapper s : dependecy) {
             if (s.completed() == false && s.duplicate() == false && s.subitemFailed() == false) {
                 return false;
             }
         }
         return true;
     }
 
     public boolean completed() {
         return status == Status.COMPLETED;
     }
 
     public boolean duplicate() {
         return status == Status.DUPLICATE;
     }
 
     public void setDuplicate() {
         status = Status.DUPLICATE;
     }
 
     public boolean exception() {
         return status == Status.EXCEPTION;
     }
 
     TcmsCommand current() {
         return current;
     }
 
     public void setExecutable(boolean executable) {
         this.executable = executable;
     }
 
     public boolean isExecutable() {
         return executable;
     }
 
     public boolean isChecked() {
         return checked;
     }
 
     public boolean subitemFailed() {
         return status == Status.SUBITEM_FAILED;
     }
 
     public void setSubitemFailed() {
         status = Status.SUBITEM_FAILED;
     }
 
     public boolean unmetDependencies() {
         return status == Status.UNMET_DEPENDENCIES;
     }
 
     public void setUnmetDependencies() {
         status = Status.UNMET_DEPENDENCIES;
     }
 
     public void setChecked(boolean checked) {
         this.checked = checked;
     }
 
     public void setResult(Object o) {
         if (this.result == null) {
             Object r = null;
             if (result_type.isInstance(o)) {
                 result = o;
                 unexpected = null;
                 return;
             } else if (o instanceof XmlRpcStruct) {
                 XmlRpcStruct struct = (XmlRpcStruct) o;
                 if (struct.containsKey("args")) { // usualy when query shows no results
                     result = null;
                     unexpected = o;
                     status = Status.EXCEPTION;
                     return;
                 } else {
                     result = TcmsConnection.rpcStructToFields((XmlRpcStruct) o, result_type);
                     unexpected = null;
                     return;
                 }
             } else {
                 result = null;
                 unexpected = o;
                 status = Status.EXCEPTION;
                 for (CommandWrapper c : dependecy) {
                     c.setSubitemFailed();
                 }
                 return;
             }
         }
     }
 
     public boolean perform(TcmsConnection connection) {
         if (processDependecies()) {
             setPerforming();
            Object o = null;
            o = getResultIfDuplicate(connection);
             if (o == null) {
                 try {
                     o = connection.invoke(current());
                    setResult(o);
                 } catch (TcmsException ex) {
                    setResult(ex.getMessage());
                 }
                
                 if (status == Status.UNKNOWN) {
                     setCompleted();
                 }
             } else {
                 setResult(o);
                 setDuplicate();
             }
             return true;
         } else {
             setUnmetDependencies();
         }
         return false;
     }
 
     public <T extends Object> T getResult(Class<T> c) {
         return c.cast(result);
     }
 
     public Object getUnexpected() {
         return unexpected;
     }
 
     public void setPerforming() {
         performed = true;
     }
 
     public boolean performed() {
         return performed;
     }
 
     public void setCompleted() {
         status = Status.COMPLETED;
     }
 
     public abstract Object getResultIfDuplicate(TcmsConnection connection);
 
     public abstract boolean processDependecies();
 
     public Hashtable<String, String> description() {
         return current.descriptionMap();
     }
 
     public abstract String summary();
 
     public String name() {
         return current.name();
     }
 
      public static CommandWrapper wrap(TcmsCommand current, Class result_type, TcmsProperties properties, TcmsEnvironment env) {
         if (current instanceof TestCase.create) {
             return new TestCaseCreate(current, result_type, properties, env);
         } else if (current instanceof TestCaseRun.create) {
             return new TestCaseRunCreate(current, result_type, properties, env);
         } else if (current instanceof TestRun.create) {
             return new TestRunCreate(current, result_type, properties, env);
         } else if (current instanceof Build.create) {
             return new BuildCreate(current, result_type, properties, env);
         } else if (current instanceof TestRun.link_env_value) {
             return new LinkRunToVarCreate(current, result_type, properties, env);
         }
 
         return new Generic(current, result_type, properties, env);
     }
 }
