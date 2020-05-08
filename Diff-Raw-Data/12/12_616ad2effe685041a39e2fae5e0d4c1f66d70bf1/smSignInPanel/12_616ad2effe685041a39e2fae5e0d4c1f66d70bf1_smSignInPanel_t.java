 package swarm.client.view.tabs.account;
 
 import swarm.client.app.smAppContext;
 import swarm.client.input.smClickManager;
 import swarm.client.input.smI_ClickHandler;
 import swarm.client.managers.smClientAccountManager;
 import swarm.client.states.StateMachine_Base;
 import swarm.client.states.account.Action_SignInOrUp_SetNewPassword;
 import swarm.client.states.account.Action_SignInOrUp_SignIn;
 import swarm.client.states.account.State_AccountStatusPending;
 import swarm.client.states.account.State_SignInOrUp;
 import swarm.client.view.smViewContext;
 import swarm.client.view.tooltip.smE_ToolTipType;
 import swarm.client.view.tooltip.smToolTipConfig;
 import swarm.client.view.tooltip.smToolTipManager;
 import swarm.client.view.widget.smI_TextBox;
 import swarm.client.view.widget.smI_TextBoxChangeListener;
 import swarm.client.view.widget.smPasswordTextBox;
 import swarm.client.view.widget.smTextBox;
 import swarm.client.view.widget.smTextBoxWrapper;
 import swarm.shared.account.smE_SignInCredentialType;
 import swarm.shared.account.smE_SignInValidationError;
 import swarm.shared.account.smE_SignUpCredentialType;
 import swarm.shared.account.smE_SignUpValidationError;
 import swarm.shared.account.smI_CredentialType;
 import swarm.shared.account.smI_SignInCredentialValidator;
 import swarm.shared.account.smI_SignUpCredentialValidator;
 import swarm.shared.account.smI_ValidationError;
 import swarm.shared.account.smSignInCredentials;
 import swarm.shared.account.smSignInValidationResult;
 import swarm.shared.account.smSignUpCredentials;
 import swarm.shared.account.smSignUpValidationResult;
 import swarm.shared.statemachine.smA_Action;
 import swarm.shared.statemachine.smI_StateEventListener;
 import swarm.shared.statemachine.smStateEvent;
 import com.google.gwt.dom.client.Style.Cursor;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.BlurEvent;
 import com.google.gwt.event.dom.client.BlurHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 public class smSignInPanel extends VerticalPanel implements smI_StateEventListener
 {
 	enum E_SubmitType
 	{
 		SIGN_IN, RESET_PASSWORD;
 	};
 	
 	private final FlowPanel m_panel = new FlowPanel();
 	
 	private final VerticalPanel m_stack = new VerticalPanel();
 	
 	private final smSignInOrUpErrorField m_emailErrorField = new smSignInOrUpErrorField();
 	private final smSignInOrUpErrorField m_passwordErrorField = new smSignInOrUpErrorField();
 	
 	private final smTextBoxWrapper m_emailInput = new smTextBoxWrapper(new smTextBox("Email or Username"));
 	private final smTextBoxWrapper m_passwordInput = new smTextBoxWrapper(new smPasswordTextBox("Password"));
 	
 	private final smSignInOrUpButton m_button = new smSignInOrUpButton();
 	private final smStaySignedInCheckbox m_checkbox;
 	
 	private smSignInOrUpErrorField 	m_errorFields[] = {m_emailErrorField, m_passwordErrorField};
 	private smTextBoxWrapper	m_inputs[] = {m_emailInput, m_passwordInput};
 	
 	private int m_lastFocusedFieldIndex = -1;
 	
 	private final Action_SignInOrUp_SignIn.Args m_actionArgs = new Action_SignInOrUp_SignIn.Args();
 	
 	private final Anchor m_changePassword = new Anchor();
 	
 	private final smViewContext m_viewContext;
 
 	smSignInPanel(smViewContext viewContext)
 	{
 		m_viewContext = viewContext;
 		
 		m_checkbox = new smStaySignedInCheckbox(m_viewContext);
 		
 		this.addStyleName("sm_signuporin_sub_panel_wrapper");
 		m_panel.addStyleName("sm_signuporin_sub_panel");
 		m_stack.setWidth("100%");
 		m_button.addStyleName("sm_signin_button");
 		
 		m_viewContext.toolTipMngr.addTip(m_button, new smToolTipConfig(smE_ToolTipType.MOUSE_OVER, "Do it!"));
 		m_viewContext.toolTipMngr.addTip(m_changePassword, new smToolTipConfig(smE_ToolTipType.MOUSE_OVER,
				"Forgot your password? Enter a new one along with your e-mail or username, then click here."));
 		
 		FlowPanel passwordResetContainer = new FlowPanel();
 		m_changePassword.setHref("javascript:void(0)");
 		m_changePassword.setText("change");
 		m_changePassword.addStyleName("sm_js_anchor");
 		m_viewContext.clickMngr.addClickHandler(m_changePassword, new smI_ClickHandler()
 		{
 			@Override
 			public void onClick()
 			{
 				submit(-1, E_SubmitType.RESET_PASSWORD);
 			}
 		});
 		passwordResetContainer.add(m_changePassword);
 		m_changePassword.getElement().getStyle().setMarginLeft(3, Unit.PX);
 		
 		m_passwordInput.getTextBox().getElement().getStyle().setMarginBottom(0, Unit.PX);
 		
 		m_button.setText("Sign In");
 		
 		//--- DRK > Register callbacks so we can display error messages when text in the input fields change.
 		for( int i = 0; i < m_errorFields.length; i++ )
 		{
 			final int final_i = i;
 			
 			((smI_TextBox)m_inputs[i].getTextBox()).setChangeListener(new smI_TextBoxChangeListener()
 			{
 				@Override
 				public void onTextChange()
 				{
 					smE_SignInCredentialType credential = smE_SignInCredentialType.values()[final_i];
 					smI_SignInCredentialValidator validator = credential.getValidator();
 					
 					smE_SignInValidationError error = validator.validateCredential(m_inputs[final_i].getText());
 					
 					smE_SignInValidationError visibleError = filterErrorFromTextChange(error);
 					
 					m_errorFields[final_i].setError(credential, error, visibleError);
 					
 					if( final_i == smE_SignInCredentialType.PASSWORD.ordinal() )
 					{
 						error = (smE_SignInValidationError) m_errorFields[smE_SignInCredentialType.EMAIL.ordinal()].getCurrentError();
 						if( error == smE_SignInValidationError.UNKNOWN_COMBINATION )
 						{
 							m_errorFields[smE_SignInCredentialType.EMAIL.ordinal()].setError(credential, smE_SignInValidationError.NO_ERROR, smE_SignInValidationError.NO_ERROR);
 						}
 					}
 				}
 
 				@Override
 				public void onEnterPressed()
 				{
 					submit(final_i, E_SubmitType.SIGN_IN);
 				}
 			});
 			
 			m_inputs[i].getTextBox().addStyleName("sm_signinorup_field");
 			
 			m_inputs[i].getTextBox().addBlurHandler(new BlurHandler()
 			{
 				@Override
 				public void onBlur(BlurEvent event)
 				{
 					smI_ValidationError currentError = m_errorFields[final_i].getCurrentError();
 					
 					if( currentError == null || currentError != null && !currentError.isServerGeneratedError() )
 					{
 						smE_SignInCredentialType credential = smE_SignInCredentialType.values()[final_i];
 						smI_SignInCredentialValidator validator = credential.getValidator();
 						
 						smE_SignInValidationError error = validator.validateCredential(m_inputs[final_i].getText());
 						
 						smE_SignInValidationError visibleError = filterErrorFromTextChange(error);
 						
 						m_errorFields[final_i].setError(credential, error, visibleError);
 					}
 				}
 			});
 		}
 		
 		m_viewContext.clickMngr.addClickHandler(m_button, new smI_ClickHandler()
 		{
 			@Override
 			public void onClick()
 			{
 				submit(-1, E_SubmitType.SIGN_IN);
 			}
 		});
 		
 		m_stack.add(m_emailErrorField);
 		m_stack.add(m_emailInput);
 		m_stack.add(m_passwordErrorField);
 		m_stack.add(m_passwordInput);
 		m_stack.add(passwordResetContainer);
 		
 		m_stack.setCellHorizontalAlignment(m_emailErrorField, HasHorizontalAlignment.ALIGN_RIGHT);
 		m_stack.setCellHorizontalAlignment(m_passwordErrorField, HasHorizontalAlignment.ALIGN_RIGHT);
 		
 		HorizontalPanel bottomDock = new HorizontalPanel();
 		FlowPanel wrapper = new FlowPanel();
 		wrapper.getElement().getStyle().setMarginLeft(0, Unit.PX);
 		wrapper.getElement().getStyle().setMarginBottom(6, Unit.PX);
 		bottomDock.setSize("100%", "100%");
 		wrapper.add(m_checkbox);
 		bottomDock.add(wrapper);
 		bottomDock.add(m_button);
 		bottomDock.setCellVerticalAlignment(wrapper, HasVerticalAlignment.ALIGN_BOTTOM);
 		bottomDock.setCellHorizontalAlignment(m_button, HasHorizontalAlignment.ALIGN_RIGHT);
 		bottomDock.setCellHorizontalAlignment(wrapper, HasHorizontalAlignment.ALIGN_LEFT);
 		
 		m_stack.add(bottomDock);
 
 		final Label header = new Label("I have an account...");
 		header.getElement().getStyle().setMarginLeft(4, Unit.PX);
 		header.getElement().getStyle().setMarginBottom(2, Unit.PX);
 		
 		m_panel.add(m_stack);
 		
 		this.add(header);
 		this.add(m_panel);
 		
 		this.setCellHorizontalAlignment(header, HasHorizontalAlignment.ALIGN_CENTER);
 	}
 	
 	private smE_SignInValidationError filterErrorFromTextChange(smE_SignInValidationError original)
 	{
 		if( original == smE_SignInValidationError.PASSWORD_TOO_SHORT )
 		{
 			original = smE_SignInValidationError.NO_ERROR;
 		}
 		else if( original == smE_SignInValidationError.EMPTY )
 		{
 			original = smE_SignInValidationError.NO_ERROR;
 		}
 		
 		return original;
 	}
 	
 	private void submit(int focusedFieldIndex, E_SubmitType type)
 	{
 		boolean canSubmit = true;
 		
 		for( int i = 0; i < m_errorFields.length; i++ )
 		{
 			m_errorFields[i].m_blockEmptyErrors = false;
 			
 			smE_SignInCredentialType credential = smE_SignInCredentialType.values()[i];
 			smI_ValidationError currentError = m_errorFields[i].getCurrentError();
 			smI_ValidationError newError = null;
 			
 			if( currentError != null )//&& currentError.isServerGeneratedError() )
 			{
 				if( type == E_SubmitType.RESET_PASSWORD )
 				{
 					if( currentError == smE_SignInValidationError.UNKNOWN_COMBINATION )
 					{
 						newError  = smE_SignInValidationError.NO_ERROR;
 					}
 					else if( currentError == smE_SignInValidationError.NO_ERROR && i == smE_SignInCredentialType.PASSWORD.ordinal() )
 					{
 						newError = smE_SignInCredentialType.PASSWORD.getValidator().validateCredential(m_inputs[i].getText());
 					}
 					else
 					{
 						newError = currentError;
 					}
 				}
 				else
 				{
 					if( currentError == smE_SignInValidationError.PASSWORD_TOO_SHORT )
 					{
 						newError = smE_SignInValidationError.NO_ERROR;
 					}
 					else
 					{
 						newError = currentError;
 					}
 				}
 			}
 			else
 			{
 				smI_SignInCredentialValidator validator = credential.getValidator();
 				newError = validator.validateCredential(m_inputs[i].getText());
 				
 				if( type == E_SubmitType.SIGN_IN )
 				{
 					if( newError == smE_SignInValidationError.PASSWORD_TOO_SHORT )
 					{
 						newError = smE_SignInValidationError.NO_ERROR;
 					}
 				}
 			}
 			
 			if( !newError.isRetryable() )
 			{
 				if( newError.isError() )
 				{
 					canSubmit = false;
 				}
 				
 				m_errorFields[i].setError(credential, newError, newError);
 			}
 			else
 			{
 				m_errorFields[i].setError(credential, smE_SignInValidationError.NO_ERROR, smE_SignInValidationError.NO_ERROR);
 			}
 		}
 		
 		if( canSubmit )
 		{
 			boolean rememberMe = m_checkbox.isChecked();
 			
 			smSignInCredentials creds = new smSignInCredentials(rememberMe, m_inputs[0].getText(), m_inputs[1].getText());
 			m_actionArgs.setCreds(creds);
 			
 			if( type == E_SubmitType.SIGN_IN )
 			{
 				m_viewContext.stateContext.performAction(Action_SignInOrUp_SignIn.class, m_actionArgs);
 			}
 			else if( type == E_SubmitType.RESET_PASSWORD )
 			{
 				m_viewContext.stateContext.performAction(Action_SignInOrUp_SetNewPassword.class, m_actionArgs);
 			}
 			
 			m_lastFocusedFieldIndex = focusedFieldIndex;
 			
 			if( m_lastFocusedFieldIndex >= 0 )
 			{
 				m_inputs[m_lastFocusedFieldIndex].getTextBox().setFocus(false);
 			}
 		}
 		else
 		{
 			m_lastFocusedFieldIndex = -1;
 		}
 	}
 
 	@Override
 	public void onStateEvent(smStateEvent event)
 	{
 		switch( event.getType() )
 		{
 			case DID_FOREGROUND:
 			{
 				if( event.getState() instanceof State_SignInOrUp )
 				{
 					smClientAccountManager accountManager = m_viewContext.appContext.accountMngr;
 					smSignInValidationResult result = accountManager.checkOutLatestBadSignInResult();
 					if( result != null )
 					{
 						for( int i = 0; i < m_errorFields.length; i++ )
 						{
 							m_errorFields[i].m_blockEmptyErrors = false;
 							smE_SignInCredentialType credential = smE_SignInCredentialType.values()[i];
 							smE_SignInValidationError error = result.getError(credential);
 							m_errorFields[i].setError(credential, error, error);
 						}
 					}
 					
 					if( m_lastFocusedFieldIndex >= 0 )
 					{
 						m_inputs[m_lastFocusedFieldIndex].getTextBox().setFocus(true);
 						
 						m_lastFocusedFieldIndex = -1;
 					}
 				}
 				
 				break;
 			}
 			
 			case DID_PERFORM_ACTION:
 			{
 				if( event.getAction() == StateMachine_Base.OnAccountManagerResponse.class )
 				{
 					StateMachine_Base.OnAccountManagerResponse.Args args = event.getActionArgs();
 					smClientAccountManager.E_ResponseType responseType = args.getType();
 					
 					switch(responseType)
 					{
 						case SIGN_UP_SUCCESS:
 						case SIGN_IN_SUCCESS:
 						case PASSWORD_CONFIRM_SUCCESS:
 						{
 							for( int i = 0; i < m_inputs.length; i++ )
 							{
 								m_inputs[i].getTextBox().setText("");
 								
 								m_errorFields[i].reset();
 							}
 							
 							m_lastFocusedFieldIndex = -1;
 							
 							break;
 						}
 					}
 				}
 				
 				break;
 			}
 		}
 	}
 }
