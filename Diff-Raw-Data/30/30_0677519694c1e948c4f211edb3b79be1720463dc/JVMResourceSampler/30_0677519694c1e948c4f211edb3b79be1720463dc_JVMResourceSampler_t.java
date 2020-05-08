 /***********************************************************************
  * Copyright (c) 2006 The Apache Software Foundation.             *
  * All rights reserved.                                                *
  * ------------------------------------------------------------------- *
  * Licensed under the Apache License, Version 2.0 (the "License"); you *
  * may not use this file except in compliance with the License. You    *
  * may obtain a copy of the License at:                                *
  *                                                                     *
  *     http://www.apache.org/licenses/LICENSE-2.0                      *
  *                                                                     *
  * Unless required by applicable law or agreed to in writing, software *
  * distributed under the License is distributed on an "AS IS" BASIS,   *
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or     *
  * implied.  See the License for the specific language governing       *
  * permissions and limitations under the License.                      *
  ***********************************************************************/
 
 
 package org.apache.james.postage.jmx;
 
 import org.apache.james.postage.SamplingException;
 import org.apache.james.postage.execution.Sampler;
 import org.apache.james.postage.result.PostageRunnerResult;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Method;
 
 /**
  * wraps JVMResourceSamplerWorker to allow J2SE 1.4 compatibility
  * does not access this class through direct reference, only through reflection
  * in the case this compatibility is removed, simply inline the Worker class
  * 
  * @see org.apache.james.postage.jmx.JVMResourceSamplerWorker for how to configure James to be JVM-JMX agnostic
  */
 public class JVMResourceSampler implements Sampler {
 
     private Object jvmResourceSampleWorker = null;
     private Method m_connectMethod;
     private Method m_doSampleMethod;
    private static final Class[] VOID_ARGUMENT_LIST = new Class[]{};
 
     public static boolean isJMXAvailable() {
         try {
             // this class is only present, when the package _is run_ at least under Java 5 
             Class jmxFactoryClass = Class.forName("javax.management.remote.JMXConnectorFactory");
         } catch (ClassNotFoundException e) {
             return false;
         }
         return true;
     }
 
     public JVMResourceSampler(String host, int port, PostageRunnerResult results) {
         Class workerClass;
         Constructor constructor;
         try {
             // the class is only present, when the package _was compiled_ with at least Java 5 (see build script)
             workerClass = Class.forName("org.apache.james.postage.jmx.JVMResourceSamplerWorker");
             Constructor[] constructors = workerClass.getConstructors();
             if (constructors.length != 1) throw new IllegalStateException("only constructor JVMResourceSamplerWorker(String host, int port, PostageRunnerResult results) is supported");
             constructor = constructors[0];
         } catch (ClassNotFoundException e) {
             return; // keep worker object NULL
         }
 
         try {
             jvmResourceSampleWorker = constructor.newInstance(new Object[]{host, new Integer(port), results});
         } catch (Exception e) {
            throw new IllegalStateException("could not create JVMResourceSamplerWorker", e);
         }
        if (jvmResourceSampleWorker == null) throw new IllegalStateException("could not create JVMResourceSamplerWorker");
 
         try {
            m_connectMethod = workerClass.getMethod("connectRemoteJamesJMXServer", VOID_ARGUMENT_LIST);
            m_doSampleMethod = workerClass.getMethod("doSample", VOID_ARGUMENT_LIST);
         } catch (NoSuchMethodException e) {
            throw new IllegalStateException("could not access delegation methods", e);
         }
 
     }
 
     public void connectRemoteJamesJMXServer() throws SamplingException {
         if(jvmResourceSampleWorker == null) throw new SamplingException("JSE specific features not present. (compile the project with JSE 5)");
         try {
            m_connectMethod.invoke(jvmResourceSampleWorker, VOID_ARGUMENT_LIST);
         } catch (Exception e) {
             throw new SamplingException("could not establish connection to remote James JMX. is James really configured for JMX and running under JSE5 or later?");
         }
     }
 
     public void doSample() throws SamplingException {
         if(jvmResourceSampleWorker == null) throw new SamplingException("JSE specific features not present. (compile the project with JSE 5)");
         try {
            m_doSampleMethod.invoke(jvmResourceSampleWorker, VOID_ARGUMENT_LIST);
         } catch (Exception e) {
             throw new SamplingException(e);
         }
     }
 }
