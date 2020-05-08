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
 
import com.google.common.base.Preconditions;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.google.inject.Singleton;
 
import de.cosmocode.palava.bridge.session.HttpSession;
 import de.cosmocode.palava.ipc.IpcCall;
 import de.cosmocode.palava.ipc.IpcCommand;
 import de.cosmocode.palava.ipc.IpcCommandExecutionException;
 import de.cosmocode.palava.ipc.IpcSession;
import de.cosmocode.palava.ipc.session.Entries;
 import de.cosmocode.rendering.MapRenderer;
 import de.cosmocode.rendering.Rendering;
 
 /**
  * debug job, dumping all session data.
  * 
 * @deprecated use {@link Entries}
  * @author Detlef Hüttemann
  */
 @Singleton
@Deprecated
 public class dump implements IpcCommand {
 
    private final Provider<MapRenderer> rendererProvider;

    private final Provider<HttpSession> sessionProvider;
     
     @Inject
    public dump(Provider<MapRenderer> rendererProvider, Provider<HttpSession> sessionProvider) {
        this.rendererProvider = Preconditions.checkNotNull(rendererProvider, "RendererProvider");
        this.sessionProvider = Preconditions.checkNotNull(sessionProvider, "SessionProvider");
     }
 
     @Override
     public void execute(IpcCall call, Map<String, Object> result) throws IpcCommandExecutionException {
        final MapRenderer renderer = rendererProvider.get();
        final HttpSession session = sessionProvider.get();
        // cast to Object is required to prevent treatment as Iterable
         renderer.value((Object) session);
        result.putAll(renderer.build());
     }
     
 }
