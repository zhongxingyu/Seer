 package com.madalla.wicket.form;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
 import org.apache.wicket.markup.html.IHeaderResponse;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 
 import com.madalla.webapp.scripts.JavascriptResources;
 import com.madalla.webapp.scripts.scriptaculous.Scriptaculous;
 
 public class AjaxValidationBehaviour extends AjaxFormComponentUpdatingBehavior {
 	private static final long serialVersionUID = 1L;
 	
 	private Log log = LogFactory.getLog(this.getClass());
 	private final FeedbackPanel feedbackPanel;
 	
 	private String validClass ;
 	private String invalidClass ;
 	
 	public AjaxValidationBehaviour() {
 		this(null);
 	}
 	public AjaxValidationBehaviour(String validClass, String invalidClass){
 		this(null, validClass, invalidClass);
 	}
 	public AjaxValidationBehaviour(FeedbackPanel feedbackPanel) {
 		this(feedbackPanel,"inputValid","inputError");
 	}
 	public AjaxValidationBehaviour(FeedbackPanel feedbackPanel, String validClass, String invalidClass) {
 		super("onblur");
		if (feedbackPanel != null){
			feedbackPanel.setOutputMarkupId(true);
		}
 		this.feedbackPanel = feedbackPanel;
 		this.validClass = validClass;
 		this.invalidClass = invalidClass;
 	}
 	
 	@Override
 	protected void onUpdate(AjaxRequestTarget target) {
 		log.debug("onUpdate - "+getFormComponent());
 		target.addComponent(getFormComponent());
 		getFormComponent().info("Valid");
 		//refreshForm(getFormComponent().getForm(), target);
 		if (feedbackPanel != null){
 			target.addComponent(feedbackPanel);
 			target.appendJavascript(
 				"$('"+feedbackPanel.getMarkupId()+"').parentNode.addClassName('"+validClass+"');"+
 				"$('"+feedbackPanel.getMarkupId()+"').parentNode.removeClassName('"+invalidClass+"');");
 				//"$('"+feedbackPanel.getMarkupId()+"').update('Valid')");
 		}
 	}
 
 	@Override
 	protected void onError(AjaxRequestTarget target, RuntimeException e) {
 		log.debug("onError -" + getFormComponent());
 
 		target.addComponent(getFormComponent());
 		//target.appendJavascript("anim"+ getComponent().getMarkupId() +".toggle();");
 		//target.appendJavascript("console.log('anim"+ getComponent().getMarkupId() +".toggle();');");
 		if (feedbackPanel != null){
 			target.addComponent(feedbackPanel);
 			target.appendJavascript(
                 "$('"+feedbackPanel.getMarkupId()+"').parentNode.addClassName('"+invalidClass+"');"+
                 "$('"+feedbackPanel.getMarkupId()+"').parentNode.removeClassName('"+validClass+"');");
 		}
 	}
 
 	@Override
 	public void renderHead(IHeaderResponse response) {
 		super.renderHead(response);
 		response.renderJavascriptReference(Scriptaculous.PROTOTYPE);
 		response.renderJavascriptReference(JavascriptResources.ANIMATOR_RESOURCE);
 		String animator = "var anim"+ getComponent().getMarkupId()+" = new Animator().addSubject(new DiscreteStyleSubject(wicketGet('"+ getComponent().getMarkupId() +"'),'opacity','0','1'));";
 		response.renderJavascript(animator, "animator");
 	}
 	
 }
