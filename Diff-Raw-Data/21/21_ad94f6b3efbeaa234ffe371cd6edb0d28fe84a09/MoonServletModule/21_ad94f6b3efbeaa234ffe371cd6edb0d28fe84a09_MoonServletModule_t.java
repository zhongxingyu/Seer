 /*
  * Copyright 2012 Goodow.com
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.goodow.moon.web.server;
 
 import com.goodow.moon.web.server.auth.AdminAuthFilter;
 import com.goodow.moon.web.server.auth.GoogleOAuthProvider;
 import com.goodow.moon.web.server.auth.InteractiveAuthFilter;
 import com.goodow.moon.web.server.auth.LoginHandler;
 import com.goodow.moon.web.server.auth.LogoutHandler;
 import com.goodow.moon.web.server.auth.OAuthCallbackHandler;
 import com.goodow.moon.web.server.auth.OAuthProvider;
 import com.goodow.moon.web.server.auth.OAuthRequestHelper;
 import com.goodow.moon.web.server.auth.QqOAuthProvider;
 import com.goodow.moon.web.server.auth.RpcAuthFilter;
 import com.goodow.moon.web.server.auth.UserContext;
 
 import com.google.common.collect.ImmutableMap;
 import com.google.inject.multibindings.MapBinder;
 import com.google.walkaround.util.server.servlet.AbstractHandler;
 import com.google.walkaround.util.server.servlet.ExactPathHandlers;
 import com.google.walkaround.util.server.servlet.HandlerServlet;
 import com.google.walkaround.util.server.servlet.PrefixPathHandlers;
 import com.google.walkaround.wave.server.WalkaroundServletModule;
 
 import java.util.Arrays;
 import java.util.Map;
 
 import javax.servlet.Filter;
 
 public class MoonServletModule extends WalkaroundServletModule {
   /** Path bindings for handlers that serve exact paths only. */
   private static final ImmutableMap<String, Class<? extends AbstractHandler>> EXACT_PATH_HANDLERS =
       new ImmutableMap.Builder<String, Class<? extends AbstractHandler>>()
       // Pages that browsers will navigate to.
           .put("/login", LoginHandler.class).build();
 
   /** Path bindings for handlers that serve all paths under some prefix. */
   private static final ImmutableMap<String, Class<? extends AbstractHandler>> PREFIX_PATH_HANDLERS =
       new ImmutableMap.Builder<String, Class<? extends AbstractHandler>>()
       // .put("/gadgets", GadgetsHandler.class)
           .build();
 
   /** Checks that there are no conflicts between paths in the handler maps. */
   private static void validatePaths() {
     for (String prefix : PREFIX_PATH_HANDLERS.keySet()) {
       for (String exact : EXACT_PATH_HANDLERS.keySet()) {
         if (exact.startsWith(prefix)) {
           throw new AssertionError("Handler conflict between prefix path " + prefix
               + " and exact path " + exact);
         }
       }
       for (String otherPrefix : PREFIX_PATH_HANDLERS.keySet()) {
         if (!otherPrefix.equals(prefix) && otherPrefix.startsWith(prefix)) {
           throw new AssertionError("Handler conflict between prefix path " + prefix
               + " and prefix path " + otherPrefix);
         }
       }
     }
   }
 
   MoonServletModule(Iterable<? extends Filter> extraFilters) {
     super(extraFilters);
   }
 
   @Override
   protected void configureServlets() {
     bind(com.google.walkaround.wave.server.auth.UserContext.class).to(UserContext.class);
     bind(com.google.walkaround.wave.server.auth.OAuthRequestHelper.class).to(
         OAuthRequestHelper.class);
     bind(com.google.walkaround.wave.server.auth.InteractiveAuthFilter.class).to(
         InteractiveAuthFilter.class);
     bind(com.google.walkaround.wave.server.auth.RpcAuthFilter.class).to(RpcAuthFilter.class);
     bind(com.google.walkaround.wave.server.auth.OAuthCallbackHandler.class).to(
         OAuthCallbackHandler.class);
     bind(com.google.walkaround.wave.server.servlet.LogoutHandler.class).to(LogoutHandler.class);
 
     super.configureServlets();
 
     // All of the exact paths in EXACT_PATH_HANDLERS, and all the path prefixes
     // from PREFIX_PATH_HANDLERS, are served with HandlerServlet.
     validatePaths();
     {
       MapBinder<String, AbstractHandler> exactPathBinder =
           MapBinder.newMapBinder(binder(), String.class, AbstractHandler.class,
               ExactPathHandlers.class);
       for (Map.Entry<String, Class<? extends AbstractHandler>> e : EXACT_PATH_HANDLERS.entrySet()) {
         serve(e.getKey()).with(HandlerServlet.class);
         exactPathBinder.addBinding(e.getKey()).to(e.getValue());
       }
     }
     {
       MapBinder<String, AbstractHandler> prefixPathBinder =
           MapBinder.newMapBinder(binder(), String.class, AbstractHandler.class,
               PrefixPathHandlers.class);
       for (Map.Entry<String, Class<? extends AbstractHandler>> e : PREFIX_PATH_HANDLERS.entrySet()) {
         serve(e.getKey() + "/*").with(HandlerServlet.class);
         prefixPathBinder.addBinding(e.getKey()).to(e.getValue());
       }
     }
 
    for (String path : Arrays.asList("/admin", "/admin/*", "/upload", "/uploadform", "/download",
         "/thumbnail", "/attachmentinfo", "/gadgets/*")) {
       filter(path).through(InteractiveAuthFilter.class);
     }
     for (String path : Arrays.asList("/gwterr")) {
       filter(path).through(RpcAuthFilter.class);
     }
    for (String path : Arrays.asList("/admin", "/admin/*")) {
      filter(path).through(AdminAuthFilter.class);
    }
 
     MapBinder<String, OAuthProvider> aAuthProviders =
         MapBinder.newMapBinder(binder(), String.class, OAuthProvider.class);
     aAuthProviders.addBinding("google").to(GoogleOAuthProvider.class);
     aAuthProviders.addBinding("qq").to(QqOAuthProvider.class);
   }
 
 }
