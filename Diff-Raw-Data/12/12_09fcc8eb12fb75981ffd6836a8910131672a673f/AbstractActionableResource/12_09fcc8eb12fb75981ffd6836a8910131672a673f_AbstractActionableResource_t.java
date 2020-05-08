 /*
  * Copyright © 2010 Red Hat, Inc.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package com.redhat.rhevm.api.common.resource;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.net.URI;
 import java.text.MessageFormat;
 import java.util.concurrent.Executor;
 
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 import javax.ws.rs.core.Response.Status;
 
 import com.redhat.rhevm.api.common.util.LinkHelper;
 import com.redhat.rhevm.api.common.util.ReapedMap;
 import com.redhat.rhevm.api.model.Action;
 import com.redhat.rhevm.api.model.BaseResource;
 import com.redhat.rhevm.api.model.Fault;
 import com.redhat.rhevm.api.resource.ActionResource;
 
 public abstract class AbstractActionableResource<R extends BaseResource> extends AbstractUpdatableResource<R> {
 
     private static final long REAP_AFTER = 2 * 60 * 60 * 1000L; // 2 hours
 
     protected Executor executor;
     protected ReapedMap<String, ActionResource> actions;
 
     public AbstractActionableResource(String id) {
         this(id, new SimpleExecutor());
     }
 
     public AbstractActionableResource(String id, Executor executor) {
         super(id);
         this.executor = executor;
         actions = new ReapedMap<String, ActionResource>(REAP_AFTER);
     }
 
 
     /**
      * Perform an action, managing asynchrony and returning an appropriate
      * response.
      *
      * @param uriInfo  wraps the URI for the current request
      * @param action   represents the pending action
      * @param task     fulfils the action
      * @return
      */
     protected Response doAction(UriInfo uriInfo, final AbstractActionTask task) {
         Action action = task.action;
         Response.Status status = null;
         R parent = newModel();
         parent.setId(getId());
         final ActionResource actionResource = new BaseActionResource<R>(uriInfo, task.action, parent);
         if (action.isSetAsync() && action.isAsync()) {
             action.setStatus(com.redhat.rhevm.api.model.Status.PENDING);
             actions.put(action.getId(), actionResource);
             executor.execute(new Runnable() {
                 public void run() {
                     perform(task);
                     actions.reapable(actionResource.getAction().getId());
                 }
             });
             status = Status.ACCEPTED;
         } else {
             // no need for self link in action if synchronous (as no querying
             // will ever be needed)
             //
             perform(task);
             status = Status.OK;
         }
 
         return Response.status(status).entity(action).build();
     }
 
     public ActionResource getActionSubresource(String action, String oid) {
         // redirect back to the target VM if action no longer cached
         // REVISIT: ultimately we should look at redirecting
         // to the event/audit log
         //
         ActionResource exists = actions.get(oid);
         return exists != null
                ? exists
                : new ActionResource() {
                     @Override
                     public Response get() {
                        R tmp = newModel();
                        tmp.setId(getId());
                        tmp = LinkHelper.addLinks(tmp);
                         Response.Status status = Response.Status.MOVED_PERMANENTLY;
                        return Response.status(status).location(URI.create(tmp.getHref())).build();
                     }
                     @Override
                     public Action getAction() {
                         return null;
                     }
                 };
     }
 
     public Executor getExecutor() {
 	return executor;
     }
 
     public void setExecutor(Executor executor) {
         this.executor = executor;
     }
 
     private void perform(AbstractActionTask task) {
         task.action.setStatus(com.redhat.rhevm.api.model.Status.IN_PROGRESS);
         if (task.action.getGracePeriod() != null) {
             try {
                 Thread.sleep(task.action.getGracePeriod().getExpiry());
             } catch (Exception e) {
                 // ignore
             }
         }
         task.run();
     }
 
     public static abstract class AbstractActionTask implements Runnable {
         protected Action action;
         protected String reason;
 
         public AbstractActionTask(Action action) {
             this(action, "");
         }
 
         public AbstractActionTask(Action action, String reason) {
             this.action = action;
             this.reason = reason;
         }
 
         public void run() {
             try {
                 execute();
                 action.setStatus(com.redhat.rhevm.api.model.Status.COMPLETE);
             } catch (Throwable t) {
                 String message = t.getMessage() != null ? t.getMessage() : t.getClass().getName();
                 setFault(MessageFormat.format(reason, message), t);
             }
         }
 
         protected abstract void execute();
 
         protected void setFault(String reason, Throwable t) {
             Fault fault = new Fault();
             fault.setReason(reason);
             fault.setDetail(trace(t));
             action.setFault(fault);
             action.setStatus(com.redhat.rhevm.api.model.Status.FAILED);
         }
 
         protected static String trace(Throwable t) {
             StringWriter sw = new StringWriter();
             t.printStackTrace(new PrintWriter(sw, true));
             return sw.toString();
         }
     }
 
     protected static class DoNothingTask extends AbstractActionTask {
         public DoNothingTask(Action action) {
             super(action);
         }
         public void execute(){
         }
     }
 
     /**
      * Fallback executor, starts a new thread for each task.
      */
     protected static class SimpleExecutor implements Executor {
         public void execute(Runnable task) {
             new Thread(task).start();
         }
     }
 }
