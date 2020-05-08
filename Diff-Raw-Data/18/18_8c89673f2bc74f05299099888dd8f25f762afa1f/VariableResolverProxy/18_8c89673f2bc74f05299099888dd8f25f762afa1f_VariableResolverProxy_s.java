 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.myfaces.scripting.jsf.dynamicdecorators.implemetations;
 
 import org.apache.myfaces.scripting.api.Decorated;
 import org.apache.myfaces.scripting.api.ScriptingConst;
 import org.apache.myfaces.scripting.core.util.WeavingContext;
 
 import javax.faces.el.VariableResolver;
 import javax.faces.el.EvaluationException;
 import javax.faces.context.FacesContext;
 
 /**
  * objects loaded must
  * be checked if a reloading is needed
  *
  * @author Werner Punz
  */
 public class VariableResolverProxy extends VariableResolver implements Decorated {
     VariableResolver _delegate;
 
     public VariableResolverProxy(VariableResolver delegate) {
         _delegate = delegate;
     }
 
     public Object resolveVariable(FacesContext facesContext, String s) throws EvaluationException {
         Object variable = _delegate.resolveVariable(facesContext, s);
        if (WeavingContext.isDynamic(variable.getClass()))
             variable = WeavingContext.getWeaver().reloadScriptingInstance(variable, ScriptingConst.ARTIFACT_TYPE_MANAGEDBEAN);
         return variable;
     }
 
     public Object getDelegate() {
         return _delegate;  //To change body of implemented methods use File | Settings | File Templates.
     }
 }
