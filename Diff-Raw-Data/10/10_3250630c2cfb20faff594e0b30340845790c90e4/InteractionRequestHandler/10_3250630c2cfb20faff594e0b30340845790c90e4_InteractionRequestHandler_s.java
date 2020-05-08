 /*
  * Copyright 2012 INRIA
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
 package com.mymed.controller.core.requesthandler;
 
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.mymed.controller.core.exception.AbstractMymedException;
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.controller.core.manager.interaction.IInteractionManager;
 import com.mymed.controller.core.manager.interaction.InteractionManager;
 import com.mymed.controller.core.requesthandler.message.JsonMessage;
 import com.mymed.model.data.interaction.MInteractionBean;
 
 /**
  * Servlet implementation class InteractionRequestHandler
  */
 @WebServlet("/InteractionRequestHandler")
 public class InteractionRequestHandler extends AbstractRequestHandler {
 
     /**
      * Generated serial ID.
      */
     private static final long serialVersionUID = -4034400417968034794L;
 
     private IInteractionManager interactionManager;
 
     /**
      * @throws ServletException
      * @see HttpServlet#HttpServlet()
      */
     public InteractionRequestHandler() throws ServletException {
         super();
 
         try {
             interactionManager = new InteractionManager();
         } catch (final InternalBackEndException e) {
             throw new ServletException("InteractionManager is not accessible because: " + e.getMessage());
         }
     }
 
     /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
      */
     @Override
     protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
         final JsonMessage message = new JsonMessage(200, this.getClass().getName());
 
         try {
             final Map<String, String> parameters = getParameters(request);
             // Check the access token
             checkToken(parameters);
             final RequestCode code = REQUEST_CODE_MAP.get(parameters.get(JSON_CODE));
 
             switch (code) {
                 case READ :
                 case DELETE :
                 default :
                     throw new InternalBackEndException("InteractionManager.doGet(" + code + ") not exist!");
             }
         } catch (final AbstractMymedException e) {
             LOGGER.debug("Error in doGet operation", e);
             message.setStatus(e.getStatus());
             message.setDescription(e.getMessage());
         }
 
         printJSonResponse(message, response);
     }
 
     /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
      */
     @Override
     protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
         final JsonMessage message = new JsonMessage(200, this.getClass().getName());
 
         try {
             final Map<String, String> parameters = getParameters(request);
             // Check the access token
             checkToken(parameters);
             final RequestCode code = REQUEST_CODE_MAP.get(parameters.get(JSON_CODE));
             String application, producer, consumer, start, end, predicate, feedback;
 
            if (code.equals(RequestCode.READ)) {
                 if ((application = parameters.get(JSON_APPLICATION)) == null) {
                     throw new InternalBackEndException("missing application argument!");
                 } else if ((producer = parameters.get("producer")) == null) {
                     throw new InternalBackEndException("missing producer argument!");
                 } else if ((consumer = parameters.get("consumer")) == null) {
                     throw new InternalBackEndException("missing consumer argument!");
                 } else if ((start = parameters.get("start")) == null) {
                     throw new InternalBackEndException("missing start argument!");
                 } else if ((end = parameters.get("end")) == null) {
                     throw new InternalBackEndException("missing end argument!");
                 } else if ((predicate = parameters.get("predicate")) == null) {
                     throw new InternalBackEndException("missing predicate argument!");
                 }
 
                 // verify consumer != producer
                 if (consumer.equals(producer)) {
                     throw new InternalBackEndException("cannot create interaction between same users");
                 }
 
                 // CREATE THE INTERACTION
                 final MInteractionBean interaction = new MInteractionBean();
                 interaction.setId(application + producer + consumer + predicate);
                 interaction.setApplication(application);
                 interaction.setProducer(producer);
                 interaction.setConsumer(consumer);
                 interaction.setStart(Long.parseLong(start));
                 interaction.setEnd(Long.parseLong(end));
 
                 // ATOMIC INTERACTION
                 if ((feedback = parameters.get("feedback")) != null) {
                     interaction.setFeedback(Double.parseDouble(feedback));
                 }
 
                 interactionManager.create(interaction);
                 message.setDescription("interaction created!");
             } else {
                 throw new InternalBackEndException("InteractionManager.doPost(" + code + ") not exist!");
             }
         } catch (final AbstractMymedException e) {
             LOGGER.debug("Error in doPost operation", e);
             message.setStatus(e.getStatus());
             message.setDescription(e.getMessage());
         }
 
         printJSonResponse(message, response);
     }
 }
