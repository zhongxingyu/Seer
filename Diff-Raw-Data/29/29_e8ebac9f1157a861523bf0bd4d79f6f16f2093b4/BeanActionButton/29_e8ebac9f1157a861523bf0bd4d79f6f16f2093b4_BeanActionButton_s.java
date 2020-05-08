 /*---
    Copyright 2006-2007 Visual Systems Corporation.
    http://www.vscorp.com
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
         http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 ---*/
 package wicket.contrib.webbeans.actions;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import wicket.Page;
 import wicket.ajax.AjaxRequestTarget;
 import wicket.contrib.webbeans.model.ElementMetaData;
 import wicket.markup.html.form.Form;
 import wicket.model.IModel;
 
 /**
  * Bean button for Actions. Action config properties:<p>
  * <ul>
  *  <li>confirm=msg - delivers a confirmation message when clicked. If answer is "Yes", the action proceeds,
  *    otherwise it is canceled.</li>
  *  <li>ajax - true/false where the action should be invoked with Ajax. Otherwise, it is a regular
  *    form submit.</li>
  *  <li>default - true/false whether the button should be fired when the Enter key is pressed anywhere
  *    on the form.</li>
  * </ul>     
  * <p>
  * 
  * @author Dan Syrstad
  */
 public class BeanActionButton extends BeanSubmitButton
 {
     private static final Class[] GENERIC_ACTION_PARAMS = new Class[] { AjaxRequestTarget.class, Form.class, Object.class };
     
     private ElementMetaData element;
     
     /**
      * Construct a BeanActionButton. 
      *
      * @param id
      * @param label
      * @param form
      * @param bean may be a direct bean or an IModel.
      */
     public BeanActionButton(String id, ElementMetaData element, Form form, Object bean)
     {
         super(id, element, form, bean);
         this.element = element;
     }
 
     @Override
     protected void onAction(AjaxRequestTarget target, Form form, Object bean)
     {
         if (bean instanceof IModel) {
            bean = ((IModel)bean).getObject(this);
         }
         
        Page page = getPage();
         String methodName = element.getActionMethodName();
        if (page == null) {
            throw new RuntimeException("BeanActionButton for action " + methodName + " is not on a Page");
        }
         
         try {
             // Try first to find a bean-specific method.
             Method method;
             try {
                method = page.getClass().getMethod(methodName, new Class[] { AjaxRequestTarget.class, Form.class, bean.getClass() } );
             }
             catch (Exception e) {
                 // Ignore and try generic parameters.
                method = page.getClass().getMethod(methodName, GENERIC_ACTION_PARAMS);
             }
             
            method.invoke(page, new Object[] { target, form, bean });
         }
         catch (NoSuchMethodException e) {
            throw new RuntimeException("Action method " + methodName + "(AjaxRequestTarget, Form, " + bean.getClass().getName() + "/Object) is not defined in class " + page.getClass());
         }
         catch (IllegalAccessException e) {
            throw new RuntimeException("Action method " + methodName + " defined in class " + page.getClass() + " must be declared public");
         }
         catch (InvocationTargetException e) {
            throw new RuntimeException("Error invoking action " + methodName + " defined in class " + page.getClass(), e.getCause());
         }
     }
 }
