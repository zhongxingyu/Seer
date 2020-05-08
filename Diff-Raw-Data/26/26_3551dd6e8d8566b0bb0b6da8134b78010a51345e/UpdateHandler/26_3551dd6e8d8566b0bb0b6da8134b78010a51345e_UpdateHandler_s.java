 /**
  * Copyright 2010 Marko Lavikainen
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
  *
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package net.contextfw.web.application.internal.service;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.concurrent.ThreadPoolExecutor;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.contextfw.web.application.ResourceCleaner;
 import net.contextfw.web.application.WebApplication;
 import net.contextfw.web.application.WebApplicationException;
 import net.contextfw.web.application.PageHandle;
 import net.contextfw.web.application.configuration.Configuration;
 import net.contextfw.web.application.internal.page.PageScope;
 import net.contextfw.web.application.internal.page.WebApplicationPage;
 import net.contextfw.web.application.lifecycle.LifecycleListener;
 import net.contextfw.web.application.remote.ResourceResponse;
 import net.contextfw.web.application.scope.Execution;
 import net.contextfw.web.application.scope.PageScopedExecutor;
 import net.contextfw.web.application.scope.ScopedWebApplicationExecution;
 import net.contextfw.web.application.scope.WebApplicationStorage;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.gson.Gson;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 
 @Singleton
 public class UpdateHandler {
 
     private static final String CONTEXTFW_REFRESH = "contextfw-refresh";
 
     private static final String CONTEXTFW_UPDATE = "contextfw-update";
 
     private static final String CONTEXTFW_REMOVE = "contextfw-remove";
     
     private ThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(10);
 
     private Logger logger = LoggerFactory.getLogger(UpdateHandler.class);
 
     private final LifecycleListener listeners;
 
     private final Gson gson;
 
     private final DirectoryWatcher watcher;
 
     private final ResourceCleaner cleaner;
 
     private PageScope pageScope;
 
     private final long maxInactivity;
 
     private final WebApplicationStorage storage;
 
     @Inject
     public UpdateHandler(LifecycleListener listeners,
             DirectoryWatcher watcher,
             ResourceCleaner cleaner,
             WebApplicationStorage storage,
             Configuration configuration,
             PageScope pageScope,
             Gson gson) {
 
         this.gson = gson;
         this.listeners = listeners;
         this.maxInactivity = configuration.get(Configuration.MAX_INACTIVITY);
         this.storage = storage;
         this.pageScope = pageScope;
 
         if (configuration.get(Configuration.DEVELOPMENT_MODE)) {
             this.cleaner = cleaner;
             this.watcher = watcher;
         } else {
             this.cleaner = null;
             this.watcher = null;
         }
     }
 
     private int getCommandStart(String[] splits) {
        if (splits.length > 2) {
            String command = splits[splits.length - 2];
            if (CONTEXTFW_REMOVE.equals(command) ||
                    CONTEXTFW_REFRESH.equals(command)) {
                return splits.length - 2;
            }
        }
        if (splits.length > 4) {
            String command = splits[splits.length - 4];
            if (CONTEXTFW_UPDATE.equals(command)) {
                return splits.length - 4;
            }
        }
        if (splits.length > 5) {
            String command = splits[splits.length - 5];
            if (CONTEXTFW_UPDATE.equals(command)) {
                return splits.length - 5;
             }
         }
         return -1;
     }
 
     public final void handleRequest(final HttpServlet servlet,
             final HttpServletRequest request,
             final HttpServletResponse response)
             throws ServletException, IOException {
 
         if (watcher != null && watcher.hasChanged()) {
             logger.info("Reloading resources");
             cleaner.clean();
         }
 
         final String[] uriSplits = request.getRequestURI().split("/");
         final int commandStart = getCommandStart(uriSplits);
         if (commandStart != -1) {
 
             String command = uriSplits[commandStart];
             PageHandle handle = new PageHandle(uriSplits[commandStart + 1]);
 
             if (CONTEXTFW_REMOVE.equals(command)) {
                 storage.remove(handle, request);
                 response.setStatus(HttpServletResponse.SC_NO_CONTENT);
             } else if (CONTEXTFW_REFRESH.equals(command)) {
                 storage.refresh(handle, request, System.currentTimeMillis() + maxInactivity);
                 response.setStatus(HttpServletResponse.SC_NO_CONTENT);
             } else if (CONTEXTFW_UPDATE.equals(command)) {
                 
                 final UpdateInvocation[] invocation = new UpdateInvocation[1];
                 invocation[0] = UpdateInvocation.NOT_DELAYED;
                 storage.update(
                         handle, 
                         request,
                         System.currentTimeMillis() + maxInactivity, 
                         new ScopedWebApplicationExecution() {
                     @Override
                     public void execute(WebApplication application) {
                         if (application == null) {
                             try {
                                 response.sendError(HttpServletResponse.SC_NOT_FOUND);
                             } catch (IOException e) {
                                 throw new WebApplicationException(e);
                             }
                         } else {
                             WebApplicationPage page = (WebApplicationPage) application;
                             pageScope.activatePage(page, servlet, request, response);
                             try {
                                 invocation[0] =
                                         page.getWebApplication().updateState(
                                                 uriSplits[commandStart + 2],
                                                 uriSplits[commandStart + 3]);
                                 if (invocation[0].isDelayed()) {
                                     pageScope.deactivateCurrentPage();
                                     return;
                                 }
 
                                 if (!invocation[0].isResource() && !invocation[0].isCancelled()) {
                                     listeners.beforeRender();
                                     setHeaders(response);
                                     response.setContentType("text/xml; charset=UTF-8");
                                     page.getWebApplication().sendResponse();
                                     listeners.afterRender();
                                 }
                             } catch (Exception e) {
                                 listeners.onException(e);
                             } finally {
                                 pageScope.deactivateCurrentPage();
                             }
                         }
                     }
                 });
 
                 Set<Execution> afterRun =  new HashSet<Execution>();
                 
                 Object retVal = null;
                 
                 if (invocation[0].isResource()) {
                     retVal = handleResource(request, response, invocation[0]);
                 } else {
                     retVal = invocation[0].getRetVal();
                 }
                 
                 if (retVal instanceof Execution) {
                     afterRun.add((Execution) retVal);
                 } else if (retVal instanceof Iterable) {
                     for (Object i : ((Iterable<?>) retVal)) {
                         afterRun.add((Execution) i);
                     }
                 } else if (retVal instanceof Execution[]) {
                     for (Execution i : ((Execution[]) retVal)) {
                         afterRun.add(i);
                     }
                 }
 
                 if (!afterRun.isEmpty()) {
                     runAfterRun(handle, afterRun);
                 }
             }
         }
         else {
             response.sendError(HttpServletResponse.SC_BAD_REQUEST);
         }
     }
 
     private void runAfterRun(final PageHandle handle,
                              final Set<Execution> afterRun) throws IOException {
 
         final PageScopedExecutor pageScopedExecutor = new PageScopedExecutor() {
             @Override
             public void execute(final Runnable execution) {
                     storage.execute(handle,
                                     new ScopedWebApplicationExecution() {
                         @Override
                         public void execute(WebApplication application) {
                             if (application != null) {
                                 WebApplicationPage page = (WebApplicationPage) application;
                                 pageScope.activatePage(page, null, null, null);
                                 try {
                                     execution.run();
                                 } finally {
                                     pageScope.deactivateCurrentPage();
                                 }
                             }
                         }
                     });
             }
         };
         for (final Execution exec : afterRun) {
             executor.execute(new Runnable() {
                 public void run() {
                     try {
                         exec.execute(pageScopedExecutor);
                     } catch (RuntimeException e) {
                         logger.error("Error during running Execution", e);
                     }
                 }
             });
         }
     }
 
     private Execution handleResource(final HttpServletRequest request,
                                     final HttpServletResponse response,
                                     UpdateInvocation invocation) throws IOException {
 
         if (invocation.getRetVal() == null) {
             response.getWriter().close();
             return null;
         }
         if (invocation.getRetVal() instanceof ResourceResponse) {
             return ((ResourceResponse) invocation.getRetVal()).serve(request, response);
         } else {
             setHeaders(response);
             response.setContentType("application/json; charset=UTF-8");
             gson.toJson(invocation.getRetVal(), response.getWriter());
             response.getWriter().close();
             return null;
         }
     }
 
     public void setHeaders(HttpServletResponse response) {
         response.addHeader("Expires", "Sun, 19 Nov 1978 05:00:00 GMT");
         response.addHeader("Last-Modified", new Date().toString());
         response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate");
         // response.addHeader("Cache-Control","post-check=0, pre-check=0");
         response.addHeader("Pragma", "no-cache");
         response.setHeader("Connection", "Keep-Alive");
     }
 
     @Inject
     public void setPageScope(PageScope pageScope) {
         this.pageScope = pageScope;
     }
 }
