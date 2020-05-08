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
 package rewrite.org.apache.myfaces.extensions.scripting.jsf.dynamicdecorators.factories;
 
import org.apache.myfaces.extensions.scripting.api.Decorated;
import org.apache.myfaces.extensions.scripting.core.util.WeavingContext;
 import rewrite.org.apache.myfaces.extensions.scripting.jsf.dynamicdecorators.implementations.LifefcycleProxy;
 
 import javax.faces.lifecycle.Lifecycle;
 import javax.faces.lifecycle.LifecycleFactory;
 import java.util.Iterator;
 
 /**
  * Lifecyclefactory which introduces scripting proxies
  * for their artefacts
  *
  * @author Werner Punz
  */
public class ScriptingLifecycleFactory extends LifecycleFactory implements Decorated {
 
     LifecycleFactory _delegate;
 
 
     public ScriptingLifecycleFactory(LifecycleFactory delegate) {
         _delegate = delegate;
     }
 
     public void addLifecycle(String s, Lifecycle lifecycle) {
        if (WeavingContext.isScriptingEnabled()  && !(lifecycle instanceof LifefcycleProxy))
             lifecycle = new LifefcycleProxy(lifecycle);
         _delegate.addLifecycle(s, lifecycle);
     }
 
     public Lifecycle getLifecycle(String s) {
         Lifecycle retVal = _delegate.getLifecycle(s);
        if (WeavingContext.isScriptingEnabled()  && !(retVal instanceof LifefcycleProxy))
             retVal = new LifefcycleProxy(retVal);
 
         return retVal;
     }
 
     public Iterator getLifecycleIds() {
         return _delegate.getLifecycleIds();
     }
 
     public void setDelegate(LifecycleFactory delegate) {
         this._delegate = delegate;
     }
 
     @Override
     public LifecycleFactory getWrapped() {
         return _delegate.getWrapped();
     }
 
     public Object getDelegate() {
         return _delegate;
     }
 }
