 package com.madalla.wicket.form;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.panel.ComponentFeedbackPanel;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 
 public abstract class AjaxValidationStyleSubmitButton extends IndicatingAjaxButton{
 
 	private static final long serialVersionUID = 5857888282264283281L;
 	private static final Log log = LogFactory.getLog(AjaxValidationStyleSubmitButton.class);
 	
 	public AjaxValidationStyleSubmitButton(String id, Form form) {
 		super(id, form);
 	}
 
 	@Override
 	protected void onSubmit(final AjaxRequestTarget target, Form form){
        log.debug("Ajax onsubmit called.");
         form.visitChildren(new Component.IVisitor() {
             public Object component(Component component){
                 log.debug("formVisitor="+component);
                 if (component instanceof ValidationStyleComponent) {
                     FormComponent formComponent = (FormComponent) component;
                     if (formComponent.isValid() ){
                         target.addComponent(formComponent);
                     }
                } else if (component instanceof ComponentFeedbackPanel){
                    log.debug("Ajax submit - adding feedback to target.");
                    ComponentFeedbackPanel feedback = (ComponentFeedbackPanel) component;
                     target.addComponent(feedback);
                 }
                 return null;
             }
         });
 	}
 	
 	@Override
 	protected void onError(final AjaxRequestTarget target, Form form){
         log.debug("Ajax onError called");
         form.visitChildren(new Component.IVisitor() {
             public Object component(Component component) {
                log.debug("formVisitor="+component);
                 if (component instanceof FormComponent) {
                     FormComponent formComponent = (FormComponent) component;
                     if (!formComponent.isValid() ){
                         target.addComponent(formComponent);
                        log.debug("Component is invalid. Component MarkupId="+formComponent.getMarkupId()+". Message is " +formComponent.getFeedbackMessage().getMessage());
                     }
                 } else if (component instanceof FeedbackPanel){
                    log.debug("Ajax onerror - adding feedback to target.");
                     FeedbackPanel feedback = (FeedbackPanel) component;
                     target.addComponent(feedback);
                 }
                 return null;
             }
         });
 
     }
 
 }
