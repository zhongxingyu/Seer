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
 
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.IAjaxCallDecorator;
 import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
 import org.apache.wicket.behavior.SimpleAttributeModifier;
 import wicket.contrib.webbeans.containers.BeanForm;
 import wicket.contrib.webbeans.model.ElementMetaData;
 import org.apache.wicket.feedback.IFeedback;
 import org.apache.wicket.markup.ComponentTag;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.SubmitLink;
 import org.apache.wicket.markup.html.panel.Panel;
 
 /**
  * Bean Form submit button. <p>
  * 
  * @author Dan Syrstad
  */
 public class BeanSubmitButton extends Panel
 {
     public static final String PARAM_DEFAULT = "default";
     public static final String PARAM_AJAX = "ajax";
     public static final String PARAM_CONFIRM = "confirm";
 
     private static final long serialVersionUID = 3103634601101052411L;
 
     private IAjaxCallDecorator decorator = null;
     
     /**
      * Construct a BeanSubmitButton. element.config may contain:<p> 
      * <ul>
      *  <li>confirm=msg - delivers a confirmation message when clicked. If answer is "Yes", the action proceeeds,
      *    otherwise it is canceled.</li>
      *  <li>ajax - true/false whether the action should be invoked with Ajax. Otherwise, it is a regular
      *    form submit.</li>
      *  <li>default - true/false whether the button should be fired when the Enter key is pressed anywhere
      *    on the form.</li>
      * </ul>     
      *
      * @param id
      * @param model
      */
     public BeanSubmitButton(String id, ElementMetaData element, Form form, final Object bean)
     {
         this(id, element.getLabelComponent("label"), form, bean, 
                         element.getParameter(PARAM_CONFIRM),
                         element.getParameter(PARAM_AJAX),
                         element.getParameter(PARAM_DEFAULT)); 
     }
 
     /**
      * Construct a BeanSubmitButton. 
      *
      * @param id
      * @param model
      */
     public BeanSubmitButton(String id, String label, Form form, final Object bean)
     {
         this(id, new Label("label", label), form, bean, null, null, null); 
     }
 
     /**
      * Construct a BeanSubmitButton. The link has a class of "beanSubmitButton" if label is a 
      * regular Label, otherwise the class is "beanSubmitImageButton".
      *
      * @param id
      * @param label
      * @param form
      * @param bean
      * @param confirmMsg if non-null, a confirm message will be displayed before the action is taken.
      *  If the answer is "Yes", the action is taken, otherwise it is canceled.
      * @param ajaxFlag if a string whose value is "true", the button's action is fired with an Ajax form submit.
      *  Otherwise if null or not "true", the button is fired with a regular form submit.
      * @param isDefault if "true", the button is invoked when enter is pressed on the form.
      */
     private BeanSubmitButton(String id, final Component label, Form form, final Object bean,
         final String confirmMsg, String ajaxFlag, String isDefault)
     {
         super(id);
         
         setRenderBodyOnly(true);
         
         WebMarkupContainer button;
         if (Boolean.valueOf(ajaxFlag)) {
             button = new AjaxSubmitLink("button", form) {
                 @Override
                 protected void onSubmit(AjaxRequestTarget target, Form form)
                 {
                     BeanSubmitButton.this.onAction(target, form, bean);
                     updateFeedbackPanels(target);
                 }
     
     
                 @Override
                 protected void onError(AjaxRequestTarget target, Form form)
                 {
                     BeanSubmitButton.this.onError(target, form, bean);
                     updateFeedbackPanels(target);
                 }
     
                 @Override
                 protected IAjaxCallDecorator getAjaxCallDecorator()
                 {
                     return decorator;
                 }
     
                 @Override
                 protected void onComponentTag(ComponentTag tag)
                 {
                     super.onComponentTag(tag);
                     tag.put("class", (label instanceof Label ? "beanSubmitButton" : "beanSubmitImageButton") );
                     tag.put("href", "javascript:void(0)"); // don't do href="#"
                 }
             };
         }
         else {
             button = new SubmitLink("button", form) {
                 @Override
                 public void onSubmit()
                 {
                     BeanSubmitButton.this.onAction(null, getForm(), bean);
                 }
     
                 @Override
                 protected void onComponentTag(ComponentTag tag)
                 {
                     super.onComponentTag(tag);
                     tag.put("class", (label instanceof Label ? "beanSubmitButton" : "beanSubmitImageButton") );
                 }
             };
         }
             
         if (confirmMsg != null) {
             button.add( new AttributeModifier("onclick", true, null) {
                 @Override
                 protected String newValue(String currentValue, String replacementValue)
                 {
                     return "if (!confirm('" + confirmMsg + "')) return false; else { " + currentValue + " }";
                 }
             });
         }
         
         if (Boolean.valueOf(isDefault)) {
             button.add( new SimpleAttributeModifier("id", "bfDefaultButton") );
         }
 
         add(button);
         button.add(label);
     }
     
     /**
      * {@inheritDoc}
      * @see wicket.Component#onAttach()
      */
     @Override
    protected void onAttach()
     {
        super.onAttach();
         BeanForm beanForm  = (BeanForm)findParent(BeanForm.class);
         if (beanForm != null) {
             // Only set this if we're in a BeanForm.
             decorator = BeanForm.AjaxBusyDecorator.INSTANCE;
         }
     }
 
     /**
      * Called when the button is clicked and no errors exist. Feedback panels are automatically
      * added to target. 
      *
      * @param target the Ajax target, which may be null if not in an Ajax context.
      * @param form the form that was submitted.
      * @param bean the bean that the button corresponds to.
      */
     protected void onAction(AjaxRequestTarget target, Form form, Object bean)
     {
     }
 
 
     /**
      * Called when the button is clicked and errors exist. Feedback panels are automatically
      * added to target. 
      *
      * @param target the Ajax target, which may be null if not in an Ajax context.
      * @param form the form that was submitted.
      * @param bean the bean that the button corresponds to.
      */
     protected void onError(final AjaxRequestTarget target, Form form, Object bean)
     {
     }
 
     /**
      * Called to update feedback panels.
      *
      * @param target the Ajax target, which may be null if not in an Ajax context.
      * @param form the form that was submitted.
      * @param bean the bean that the button corresponds to.
      */
     protected void updateFeedbackPanels(final AjaxRequestTarget target)
     {
         getPage().visitChildren(IFeedback.class, new IVisitor() {
             public Object component(Component component) 
             {
                 target.addComponent(component);
                 return IVisitor.CONTINUE_TRAVERSAL;
             }            
         });
     }
 }
