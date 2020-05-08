 /**
  * Copyright (C) 2012 Ness Computing, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.nesscomputing.tracking;
 
 
 import java.io.IOException;
 import java.util.UUID;
 
import javax.annotation.Nonnull;
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.google.inject.Singleton;
 import com.nesscomputing.logging.Log;
 import com.nesscomputing.scopes.threaddelegate.servlet.ThreadDelegatingScopeFilter;
 import com.nesscomputing.tracking.adapters.ServletApiAdapter;
 
 /**
  * Tracking filter ensures that the tracking token springs into existence as early as possible (but it must be after the
  * ThreadDelegatingScopeFilter is run!). Also sets the MDC and the response header correctly.
  */
 @Singleton
 public class TrackingFilter implements Filter
 {
     /** Name of the Tracking Header. The value is legacy. */
     public static final String X_NESS_TRACK = "X-Trumpet-Track";
 
 
     private static final Log LOG = Log.findLog();
 
     private final TrackingUUIDProvider trackingTokenProvider;
     private final Provider<TrackingToken> scopedProvider;
 
     @Inject
     TrackingFilter(final TrackingUUIDProvider trackingUUIDProvider,
                    final Provider<TrackingToken> scopedProvider)
     {
         this.trackingTokenProvider = trackingUUIDProvider;
         this.scopedProvider = scopedProvider;
     }
 
     @Override
     public void init(FilterConfig filterConfig) throws ServletException
     {
         LOG.info("Request tracking enabled.");
     }
 
     @Override
     public void destroy()
     {
     }
 
     @Override
    public void doFilter(@Nonnull ServletRequest request, @Nonnull ServletResponse response, @Nonnull FilterChain chain) throws IOException, ServletException
     {
         final HttpServletRequest req = (HttpServletRequest) request;
         final HttpServletResponse res = (HttpServletResponse) response;
         try {
             if (request.getAttribute(ThreadDelegatingScopeFilter.THREAD_DELEGATING_SCOPE_ACTIVE) == null) {
                 LOG.warn("Called the tracking filter before the ThreadDelegatingScopeFilter. Tracking will not work. You need to reshuffle your guice modules to bind the filters in the right order!");
             }
             else {
                 final TrackingToken trackingToken = scopedProvider.get();
                 final UUID trackingUUID = trackingTokenProvider.get(new ServletApiAdapter(req));
                 trackingToken.setValue(trackingUUID);
 
                 if (trackingUUID != null) {
                    request.setAttribute(X_NESS_TRACK, trackingUUID);
                    res.setHeader(X_NESS_TRACK, trackingUUID.toString());
                 }
             }
         }
         finally {
             chain.doFilter(request, response);
         }
     }
 
 }
 
 
