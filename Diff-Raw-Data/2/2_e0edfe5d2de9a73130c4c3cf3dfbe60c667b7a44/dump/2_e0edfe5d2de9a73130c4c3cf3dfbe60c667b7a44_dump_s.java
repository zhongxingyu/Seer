 /**
  * Copyright 2010 CosmoCode GmbH
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package de.cosmocode.palava.jobs.session;
 
 import java.util.Map;
 
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.google.inject.Singleton;
 
 import de.cosmocode.palava.ipc.IpcCall;
 import de.cosmocode.palava.ipc.IpcCommand;
 import de.cosmocode.palava.ipc.IpcCommandExecutionException;
 import de.cosmocode.palava.ipc.IpcSession;
 import de.cosmocode.rendering.MapRenderer;
 import de.cosmocode.rendering.Rendering;
 
 /**
  * debug job, dumping all session data.
  * 
  * @author Detlef Hüttemann
  */
 @Singleton
 public class dump implements IpcCommand {
 
     private final Provider<MapRenderer> provider;
     
     @Inject
     public dump(Provider<MapRenderer> provider) {
         this.provider = provider;
     }
 
     @Override
     public void execute(IpcCall call, Map<String, Object> result) throws IpcCommandExecutionException {
         final MapRenderer renderer = provider.get();
         final IpcSession session = call.getConnection().getSession();
        renderer.value(session, Rendering.maxLevel());
         final Map<String, Object> map = renderer.build();
         result.putAll(map);
     }
     
 }
