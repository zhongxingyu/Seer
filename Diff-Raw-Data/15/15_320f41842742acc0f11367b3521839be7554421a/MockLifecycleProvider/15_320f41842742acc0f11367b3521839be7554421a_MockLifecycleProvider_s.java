 package com.ewerp.mud.plugins;
 
 import com.ewerp.mud.EjMudException;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Copyright 2012 Curtis Boyden
  * <p/>
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * <p/>
  * http://www.apache.org/licenses/LICENSE-2.0
  * <p/>
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 public class MockLifecycleProvider implements ILifecycleProvider {
     public List<ILifecycleListener> lifecycleListenerList = new ArrayList<ILifecycleListener>();
 
     @Override
     public void registerListener(ILifecycleListener listener) throws IllegalArgumentException {
         if(null == listener) {
             throw new IllegalArgumentException();
         }
         lifecycleListenerList.add(listener);
     }
 
     @Override
    public void unregisterListener(ILifecycleListener listener) throws EjMudException {
         if(lifecycleListenerList.contains(listener)) {
             lifecycleListenerList.remove(listener);
         }
     }
 }
