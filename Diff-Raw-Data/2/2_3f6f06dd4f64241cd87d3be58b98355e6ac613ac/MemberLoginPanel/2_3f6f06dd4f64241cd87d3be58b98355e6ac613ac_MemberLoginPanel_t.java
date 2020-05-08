 package com.madalla.webapp.components.member;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.wicket.Component;
 import org.apache.wicket.MarkupContainer;
 import org.apache.wicket.Page;
 import org.apache.wicket.RestartResponseAtInterceptPageException;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
 import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.PasswordTextField;
 import org.apache.wicket.markup.html.form.RequiredTextField;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.panel.ComponentFeedbackPanel;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.model.ResourceModel;
 import org.apache.wicket.model.StringResourceModel;
 
 import com.madalla.bo.member.MemberData;
 import com.madalla.util.security.ICredentialHolder;
 import com.madalla.util.security.SecureCredentials;
 import com.madalla.webapp.admin.member.MemberSession;
 import com.madalla.wicket.animation.AnimationOpenSlide;
 
 public class MemberLoginPanel extends AbstractMemberPanel{
 	private static final long serialVersionUID = 1L;
 	private final static Log log = LogFactory.getLog(MemberLoginPanel.class);
 	
 	public abstract class SignInform extends Form<Object>{
 		private static final long serialVersionUID = 1L;
 		
 		public SignInform(final String id, final ICredentialHolder credentials) {
 			super(id);
 			
 			final TextField<String> username;
 			add(username = new RequiredTextField<String>("username", new PropertyModel<String>(credentials, "username")));
 			username.setRequired(true);
 			
 			final PasswordTextField password;
 			add(password = new PasswordTextField("password", new PropertyModel<String>(credentials,"password")));
             password.setRequired(true);
             
 			username.setPersistent(true);
 		}
 		
 		public abstract boolean signIn(String username, String password);
 		
 	}
 	
 	public MemberLoginPanel(String id){
 		this(id, new SecureCredentials(), null);
 	}
 
 	public MemberLoginPanel(String id, final ICredentialHolder credentials, Class<? extends Page> destinationParam) {
 		super(id);
 		
 		final Class<? extends Page> destination = destinationParam == null? getApplication().getHomePage(): destinationParam;
 		
 		final MemberSession session = getAppSession().getMemberSession();
 		
 		//if we have a valid populated credential then validate
 		if (StringUtils.isNotEmpty(credentials.getUsername()) && StringUtils.isNotEmpty(credentials.getPassword()) &&
 				session.signIn(credentials.getUsername(), credentials.getPassword())){
 			
 			throw new RestartResponseAtInterceptPageException(destination);
 			
 		}
 		
 		// logged in message
 		final Component loginInfo = new Label("loginInfo", new StringResourceModel("login.info",new Model<MemberData>(session.getMember()))){
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void onBeforeRender() {
 				setOutputMarkupId(true);
 				setVisibilityAllowed(true);
 				if (!session.isSignedIn()){
 					setVisible(false);
 				}
 				super.onBeforeRender();
 			}
 
 		};
 		add(loginInfo);
 		
 		final Form<Object> signinForm = new SignInform("signInForm", credentials){
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public boolean signIn(String username, String password) {
 				return session.signIn(username, password);
 			}
 			
 		};
 		add(signinForm);
 		signinForm.setEnabled(!session.isSignedIn());
 		
 		final FeedbackPanel signinFeedback = new FeedbackPanel("loginFeedback");
 		signinFeedback.setOutputMarkupId(true);
 		signinForm.add(signinFeedback);
 		
		signinForm.add(new IndicatingAjaxButton("submitLink", new ResourceModel("label.memberLogin"), signinForm){
 
             private static final long serialVersionUID = 1L;
 
             @Override
 			protected void onError(AjaxRequestTarget target, Form<?> form) {
 				log.debug("Ajax onError called");
 				target.addComponent(signinFeedback);
 			}
 
 			@Override
 			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
 				log.debug("Ajax submit called");
 				target.addComponent(signinFeedback);
 				
 				if (session.signIn(credentials.getUsername(), credentials.getPassword())){
 					signinFeedback.info(getLocalizer().getString("signInFailed", this, "Success"));
 					setResponsePage(destination);
 				} else {
 					signinFeedback.error(getLocalizer().getString("signInFailed", this, "Sign in failed"));
 					target.addComponent(signinFeedback);
 				}
 				
 			}
 			
 		});
 		
 		add(new AjaxFallbackLink<Object>("logout"){
 
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void onClick(AjaxRequestTarget target) {
 				session.signOut();
 				target.addComponent(loginInfo);
 				processSignOut();
 			}
 
 			@Override
 			protected void onBeforeRender() {
 				setOutputMarkupId(true);
 				setVisibilityAllowed(true);
 				if (!session.isSignedIn()){
 					setVisible(false);
 				}
 				super.onBeforeRender();
 			}
 			
 		});
 		
 		Component resetLink = new Label("resetLink", new ResourceModel("label.forgot"));
 		add(resetLink);
 		resetLink.setVisible(!session.isSignedIn());
 		
 		MarkupContainer resetDiv = new WebMarkupContainer("resetDiv");
 		add(resetDiv);
 		
 		resetLink.add(new AnimationOpenSlide("onclick", resetDiv, 10,"em"));
 		
 		// Reset Form
 		final Form<String> resetForm;
 		resetDiv.add(resetForm = new Form<String>("resetForm"));
 		
 		final TextField<String> username ;
 		resetForm.add(username = new RequiredTextField<String>("memberId", new Model<String>(credentials.getUsername())));
 
 		final Component resetFeedback;
 		resetForm.add(resetFeedback = new ComponentFeedbackPanel("resetFeedback", username).setOutputMarkupId(true));
 		resetForm.add(new IndicatingAjaxButton("submitLink", new ResourceModel("label.reset"), resetForm) {
 
 			private static final long serialVersionUID = 1L;
 			
 			@Override
 			protected void onError(AjaxRequestTarget target, Form<?> form) {
 				target.addComponent(resetFeedback);
 			}
 
 			@Override
 			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
 				target.addComponent(resetFeedback);
 				
 				final String memberId = username.getModelObject();
 				
 				if (memberExists(memberId)) {
 					final MemberData member = getRepositoryService().getMember(memberId);
 					final String email = member.getEmail();
 					if (StringUtils.isNotEmpty(email)){
 						if (sendResetPasswordEmail(member)){
 							username.info(getString("message.reset.success"));
 						} else {
 							log.error("password reset - Send failure! " + member);
 							username.error(getString("error.reset"));
 						}
 					} else {
 						log.info("password reset - Unable to send email. No value for email. " + member);
 						username.error(getString("error.reset"));
 					}
 				} else {
 					username.error(getString("error.reset"));
 				}
 				target.addComponent(resetFeedback);
 			}	
 		});
 	}
 	
 	private boolean memberExists(String memberId){
 		return getRepositoryService().isMemberExist(memberId);
 	}
 		
 	protected void processSignOut(){
 		
 	}
 	
 }
