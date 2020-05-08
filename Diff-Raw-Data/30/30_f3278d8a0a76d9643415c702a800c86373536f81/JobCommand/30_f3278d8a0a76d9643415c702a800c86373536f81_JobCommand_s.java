 /**
  * palava - a java-php-bridge
  * Copyright (C) 2007-2010  CosmoCode GmbH
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 package de.cosmocode.palava.bridge.command;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Preconditions;
 
 import de.cosmocode.palava.bridge.Content;
 import de.cosmocode.palava.bridge.Server;
 import de.cosmocode.palava.bridge.call.Call;
 import de.cosmocode.palava.bridge.request.HttpRequest;
 import de.cosmocode.palava.bridge.scope.Scopes;
 import de.cosmocode.palava.bridge.session.HttpSession;
 import de.cosmocode.patterns.Adapter;
 
 /**
  * {@link Job} to {@link Command} adapter.
  *
  * @author Willi Schoenborn
  */
 @Adapter(Command.class)
 final class JobCommand implements Command {
     
     private static final Logger log = LoggerFactory.getLogger(JobCommand.class); 
 
     private final Server server;
         
     private final Job job;
     
     public JobCommand(Server server, Job job) {
         this.server = Preconditions.checkNotNull(server, "Server");
         this.job = Preconditions.checkNotNull(job, "Job");
     }
     
     @Override
     public Content execute(Call call) throws CommandException {
         
         final Response response = new DummyResponse();
        final HttpRequest request = Scopes.getCurrentRequest();
         log.debug("Local request: {}", request);
        final HttpSession session = request.getHttpSession();
         log.debug("Local session: {}", session);
 
         try {
             // TODO keep looking for NPEs (caddy shouldn't be used anymore
             job.process(call, response, session, server, null);
         /*CHECKSTYLE:OFF*/
         } catch (RuntimeException e) {
         /*CHECKSTYLE:ON*/
             throw e;
         /*CHECKSTYLE:OFF*/
         } catch (Exception e) {
         /*CHECKSTYLE:ON*/
             throw new CommandException(e);
         }
         
         Preconditions.checkState(response.hasContent(), "No content set");
         
         return response.getContent();
     }
     
     /**
      * Dummy implementation of the {@link Response} interface which allows simple
      * content retrieval.
      *
      * @author Willi Schoenborn
      */
     private final class DummyResponse implements Response {
 
         private Content content;
 
         @Override
         public void setContent(Content content) {
             this.content = content;
         }
         
         @Override
         public Content getContent() {
             return content;
         }
 
         @Override
         public boolean hasContent() {
             return content != null;
         }
 
     }
 
     /**
      * Provide the underlying job instance.
      * 
      * @return the job's class
      */
     public Class<? extends Job> getConcreteClass() {
         return job.getClass();
     }
     
 }
