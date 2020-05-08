 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 package me.schiz.jmeter.functions;
 
 import com.bitlove.fnv.FNV;
 import org.apache.jmeter.engine.util.CompoundVariable;
 import org.apache.jmeter.functions.AbstractFunction;
 import org.apache.jmeter.functions.InvalidVariableException;
 import org.apache.jmeter.samplers.SampleResult;
 import org.apache.jmeter.samplers.Sampler;
 import org.apache.jmeter.threads.JMeterContext;
 import org.apache.jmeter.threads.JMeterContextService;
 import org.apache.jmeter.threads.JMeterVariables;
 import org.apache.jorphan.logging.LoggingManager;
 import org.apache.log.Logger;
 
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * FNV1a_32 function
  */
 public class FNV1a_32 extends AbstractFunction {
 
     private static final Logger log = LoggingManager.getLoggerForClass();
 
     private static final List<String> desc = new LinkedList<String>();
     private static final String KEY = "__FNV1a_32"; //$NON-NLS-1$
     private static FNV hasher;
 
     static {
         desc.add("String to encode");
         desc.add("Variable to store value");
     }
 
     private Object[] values;
 
     public FNV1a_32() {
         if(hasher == null) {
             synchronized (FNV1a_32.class) {
                 if(hasher == null) {
                     hasher = new FNV();
                 }
             }
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public String execute(SampleResult previousResult, Sampler currentSampler)
             throws InvalidVariableException {
 
         JMeterContext jmctx = JMeterContextService.getContext();
         JMeterVariables vars = jmctx.getVariables();
 
         String value, resultStr, key = null;
         synchronized (values) {
             value = ((CompoundVariable) values[0]).execute().trim();
             if (values.length > 1) {
                 key = ((CompoundVariable) values[1]).execute().trim();
             }
         }
         synchronized (hasher) {
             resultStr = String.valueOf(hasher.fnv1a_32(value.getBytes()));
         }
        if(key != null) vars.put(key, resultStr);
         return resultStr;
 
     }
 
     /*
      * Helper method for use by scripts
      *
      */
     public void log_info(String s) {
         log.info(s);
     }
 
     /** {@inheritDoc} */
     @Override
     public synchronized void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
 
         checkParameterCount(parameters, 1, 2);
 
         values = parameters.toArray();
     }
 
     /** {@inheritDoc} */
     @Override
     public String getReferenceKey() {
         return KEY;
     }
 
     /** {@inheritDoc} */
     @Override
     public List<String> getArgumentDesc() {
         return desc;
     }
 
 }
