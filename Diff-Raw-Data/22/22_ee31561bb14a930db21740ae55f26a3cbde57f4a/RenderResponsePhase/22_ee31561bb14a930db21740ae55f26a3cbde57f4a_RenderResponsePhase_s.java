 /*
  * Copyright 2004-2007 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.teeda.core.lifecycle.impl;
 
 import java.io.IOException;
 
 import javax.faces.FacesException;
 import javax.faces.application.ViewHandler;
 import javax.faces.context.FacesContext;
 import javax.faces.el.EvaluationException;
 import javax.faces.event.PhaseId;
 import javax.faces.internal.scope.RedirectScope;
 
 import org.seasar.teeda.core.lifecycle.AbstractPhase;
 import org.seasar.teeda.core.util.FacesContextUtil;
 import org.seasar.teeda.core.util.PortletUtil;
 
 /**
  * @author shot
  */
 public class RenderResponsePhase extends AbstractPhase {
 
     public void executePhase(FacesContext context) throws FacesException {
         ViewHandler viewHandler = FacesContextUtil.getViewHandler(context);
         try {
             viewHandler.renderView(context, context.getViewRoot());
         } catch (IOException e) {
             throw new FacesException(e.getMessage(), e);
         } catch (EvaluationException ex) {
             Throwable cause = ex.getCause();
             if (cause instanceof RuntimeException) {
                 throw (RuntimeException) cause;
             } else if (cause instanceof Error) {
                 throw (Error) cause;
             } else {
                 throw ex;
             }
         } finally {
             synchronized (this) {
                if (RedirectScope.isRedirecting(context) &&
                        !context.getResponseComplete()) {
                    String path = RedirectScope.getRedirectingPath(context);
                    RedirectScope.clearContext(context);
                    RedirectScope.setRedirectedPath(context, path);
                 } else {
                     // PortletSupport
                     if (!PortletUtil.isPortlet(context)) {
                         RedirectScope.clearContext(context);
                     }
                 }
             }
         }
     }
 
     protected PhaseId getCurrentPhaseId() {
         return PhaseId.RENDER_RESPONSE;
     }
 
 }
