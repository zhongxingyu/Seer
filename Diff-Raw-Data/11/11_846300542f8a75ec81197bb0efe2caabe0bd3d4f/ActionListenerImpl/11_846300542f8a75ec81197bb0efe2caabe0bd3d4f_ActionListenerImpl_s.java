 /*
  * Copyright 2004-2006 the Seasar Foundation and the Others.
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
 package org.seasar.teeda.core.application;
 
 import java.io.IOException;
 
 import javax.faces.component.UICommand;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.el.EvaluationException;
 import javax.faces.el.MethodBinding;
 import javax.faces.event.AbortProcessingException;
 import javax.faces.event.ActionEvent;
 import javax.faces.event.ActionListener;
 
 import org.seasar.framework.log.Logger;
 import org.seasar.teeda.core.ErrorPageManager;
 import org.seasar.teeda.core.NullErrorPageManagerImpl;
 import org.seasar.teeda.core.util.InvokeUtil;
 import org.seasar.teeda.core.util.NavigationHandlerUtil;
 import org.seasar.teeda.core.util.UIParameterUtil;
 
 /**
  * @author higa
  */
 public class ActionListenerImpl implements ActionListener {
 
     //TODO testing
     private static Logger logger = Logger.getLogger(ActionListenerImpl.class);
 
     private ErrorPageManager errorPageManager_ = new NullErrorPageManagerImpl();
 
     public void processAction(ActionEvent actionEvent)
             throws AbortProcessingException {
         FacesContext context = FacesContext.getCurrentInstance();
        UICommand command = (UICommand) actionEvent.getComponent();
        UIParameterUtil.saveParametersToRequest(command, context);
        MethodBinding mb = command.getAction();
         String fromAction = null;
         String outcome = null;
         if (mb != null) {
             fromAction = mb.getExpressionString();
             try {
                 outcome = InvokeUtil.invoke(mb, context);
             } catch (EvaluationException ex) {
                 Throwable cause = ex.getCause();
                 ErrorPageManager manager = getErrorPageManager();
                 try {
                     ExternalContext extContext = context.getExternalContext();
                     if (manager.handleException(cause, extContext)) {
                         context.responseComplete();
                     } else {
                         throw ex;
                     }
                 } catch (IOException ioe) {
                     logger.log(ioe);
                     throw ex;
                 }
             }
         }
         NavigationHandlerUtil.handleNavigation(context, fromAction, outcome);
         context.renderResponse();
     }
 
     public ErrorPageManager getErrorPageManager() {
         return errorPageManager_;
     }
 
     public void setErrorPageManager(ErrorPageManager errorPageManager) {
         errorPageManager_ = errorPageManager;
     }
 
 }
