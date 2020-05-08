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
 package org.apache.myfaces.extensions.scripting.jsf.dynamicdecorators.implemetations;
 
 import org.apache.myfaces.extensions.scripting.api.Decorated;
 import org.apache.myfaces.extensions.scripting.api.ScriptingConst;
 import org.apache.myfaces.extensions.scripting.core.util.WeavingContext;
 
 import javax.faces.context.*;
 import javax.faces.application.Application;
 import javax.faces.application.FacesMessage;
 import javax.faces.application.ProjectStage;
 import javax.faces.render.RenderKit;
 import javax.faces.component.UIViewRoot;
 import javax.faces.event.PhaseId;
 import javax.el.ELContext;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.List;
 
 /**
  * A reloading, weaving  faces context
  * this is needed because groovy fails on
  * the introspection of the standard java myfaces
  * faces context due to pending references
  * of the _impl into the portlet context
  * not sure if this works in portlets
  * though
  *
  * @author Werner Punz
  */
 public class FacesContextProxy extends FacesContext implements Decorated {
 
     public FacesContext _delegate = null;
 
     private void weaveDelegate() {
        //in case of a context destroyed the weaver might be accessed a last time
        //but is already null due to having no weaver in the shutdown thread
        if (_delegate != null && WeavingContext.getWeaver() != null)
             _delegate = (FacesContext) WeavingContext.getWeaver().reloadScriptingInstance(_delegate, ScriptingConst.ARTIFACT_TYPE_FACESCONTEXT);
     }
 
     public ELContext getELContext() {
         return _delegate.getELContext();
     }
 
     public Application getApplication() {
         return _delegate.getApplication();
     }
 
     public Iterator<String> getClientIdsWithMessages() {
         return _delegate.getClientIdsWithMessages();
     }
 
     public ExternalContext getExternalContext() {
         return _delegate.getExternalContext();
     }
 
     public FacesMessage.Severity getMaximumSeverity() {
         return _delegate.getMaximumSeverity();
     }
 
     public Iterator<FacesMessage> getMessages() {
         return _delegate.getMessages();
     }
 
     public Iterator<FacesMessage> getMessages(String s) {
         return _delegate.getMessages(s);
     }
 
     public RenderKit getRenderKit() {
         return _delegate.getRenderKit();
     }
 
     public boolean getRenderResponse() {
         return _delegate.getRenderResponse();
     }
 
     public boolean getResponseComplete() {
         return _delegate.getResponseComplete();
     }
 
     public ResponseStream getResponseStream() {
         return _delegate.getResponseStream();
     }
 
     public void setResponseStream(ResponseStream responseStream) {
         _delegate.setResponseStream(responseStream);
     }
 
     public ResponseWriter getResponseWriter() {
         return _delegate.getResponseWriter();
     }
 
     public void setResponseWriter(ResponseWriter responseWriter) {
         _delegate.setResponseWriter(responseWriter);
     }
 
     public UIViewRoot getViewRoot() {
         return _delegate.getViewRoot();
     }
 
     public void setViewRoot(UIViewRoot uiViewRoot) {
         weaveDelegate();//perfect place no matter what the viewRoot is about once per request set
         _delegate.setViewRoot(uiViewRoot);
     }
 
     public void addMessage(String s, FacesMessage facesMessage) {
         _delegate.addMessage(s, facesMessage);
     }
 
     public void release() {
         _delegate.release();
     }
 
     public void renderResponse() {
         _delegate.renderResponse();
     }
 
     public void responseComplete() {
         _delegate.responseComplete();
     }
 
     public FacesContextProxy(FacesContext delegate) {
         _delegate = delegate;
         weaveDelegate();
     }
 
     @Override
     public Map<Object, Object> getAttributes() {
         return _delegate.getAttributes();
     }
 
     @Override
     public PhaseId getCurrentPhaseId() {
         return _delegate.getCurrentPhaseId();
     }
 
     @Override
     public ExceptionHandler getExceptionHandler() {
         return _delegate.getExceptionHandler();
     }
 
     @Override
     public List<FacesMessage> getMessageList() {
         return _delegate.getMessageList();
     }
 
     @Override
     public List<FacesMessage> getMessageList(String s) {
         return _delegate.getMessageList(s);
     }
 
     @Override
     public PartialViewContext getPartialViewContext() {
         return _delegate.getPartialViewContext();
     }
 
     @Override
     public boolean isValidationFailed() {
         return _delegate.isValidationFailed();
     }
 
     @Override
     public boolean isPostback() {
         return _delegate.isPostback();
     }
 
     @Override
     public boolean isProcessingEvents() {
         return _delegate.isProcessingEvents();
     }
 
     @Override
     public void setCurrentPhaseId(PhaseId phaseId) {
         _delegate.setCurrentPhaseId(phaseId);
     }
 
     @Override
     public void setExceptionHandler(ExceptionHandler exceptionHandler) {
         _delegate.setExceptionHandler(exceptionHandler);
     }
 
     @Override
     public void setProcessingEvents(boolean b) {
         _delegate.setProcessingEvents(b);
     }
 
     @Override
     public void validationFailed() {
         _delegate.validationFailed();
     }
 
     @Override
     public boolean isProjectStage(ProjectStage projectStage) {
         return _delegate.isProjectStage(projectStage);
     }
 
     public Object getDelegate() {
         return _delegate;  //To change body of implemented methods use File | Settings | File Templates.
     }
 }
