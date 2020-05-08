 package is.idega.idegaweb.pheidippides.presentation;
 
 import is.idega.idegaweb.pheidippides.PheidippidesConstants;
 import is.idega.idegaweb.pheidippides.bean.PheidippidesBean;
 import is.idega.idegaweb.pheidippides.business.PheidippidesService;
 import is.idega.idegaweb.pheidippides.business.SearchParameter;
 import is.idega.idegaweb.pheidippides.data.Participant;
 
 import java.text.DateFormat;
 import java.text.MessageFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import javax.faces.context.FacesContext;
 
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.idega.block.web2.business.JQuery;
 import com.idega.facelets.ui.FaceletComponent;
 import com.idega.idegaweb.IWBundle;
 import com.idega.presentation.IWBaseComponent;
 import com.idega.presentation.IWContext;
 import com.idega.util.LocaleUtil;
 import com.idega.util.PresentationUtil;
 import com.idega.util.expression.ELUtil;
 
 public class ParticipantLostPassword extends IWBaseComponent {
 
 	private static final String PARAMETER_ACTION = "prm_action";
 	private static final String PARAMETER_PERSONAL_ID = "prm_personal_id";
 	private static final String PARAMETER_EMAIL = "prm_email";
 	private static final String PARAMETER_DATE_OF_BIRTH = "prm_date_of_birth";
 	
 	@Autowired
 	private PheidippidesService service;
 
 	@Autowired
 	private JQuery jQuery;
 	
 	private IWBundle iwb;
 
 	@Override
 	protected void initializeComponent(FacesContext context) {
 		IWContext iwc = IWContext.getIWContext(context);
 		iwb = getBundle(context, getBundleIdentifier());
 		
 		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, getJQuery().getBundleURIToJQueryLib());
 		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, getJQuery().getBundleURISToValidation());
 		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, iwb.getVirtualPathWithFileNameString("javascript/participantLostPassword.js"));
 
 		PresentationUtil.addStyleSheetToHeader(iwc, iwb.getVirtualPathWithFileNameString("style/pheidippides.css"));
 
 		PheidippidesBean bean = getBeanInstance("pheidippidesBean");
 		bean.setLocale(iwc.getCurrentLocale());
 
 		if (iwc.isParameterSet(PARAMETER_ACTION)) {
 			SearchParameter parameter = new SearchParameter();
 			
 			if (iwc.getCurrentLocale().equals(LocaleUtil.getIcelandicLocale())) {
 				String personalID = iwc.getParameter(PARAMETER_PERSONAL_ID);
 				parameter.setPersonalId(personalID);
 			}
 			else {
 				try {
 					DateFormat format = new SimpleDateFormat("dd.MM.yyyy");
 					
 					String email = iwc.getParameter(PARAMETER_EMAIL);
 					Date dateOfBirth = format.parse(iwc.getParameter(PARAMETER_DATE_OF_BIRTH));
 					
 					parameter.setEmail(email);
 					parameter.setDateOfBirth(dateOfBirth);
 				}
 				catch (ParseException pe) {
 					pe.printStackTrace();
 				}
 			}
 			
 			List<Participant> participants = getService().searchForParticipants(parameter);
 			if (participants != null && participants.size() == 1) {
 				Participant participant = participants.iterator().next();
 				getService().generateNewPassword(participant, iwc.getCurrentLocale());
 				
 				Object[] arguments = { participant.getEmail() };
 				bean.setStyleClass("success");
 				bean.setResponse(MessageFormat.format(iwb.getResourceBundle(iwc).getLocalizedString("lost_password.password_created", "A new password has been created and sent to {0}."), arguments));
 			}
 			else {
 				bean.setStyleClass("error");
 				bean.setResponse(iwb.getResourceBundle(iwc).getLocalizedString("lost_password.no_participant_found", "No participant found with the given email and date of birth."));
 			}
 		}
 		
 		FaceletComponent facelet = (FaceletComponent) iwc.getApplication().createComponent(FaceletComponent.COMPONENT_TYPE);
 		facelet.setFaceletURI(iwb.getFaceletURI("participantLostPassword/view.xhtml"));
 		add(facelet);
 	}
 
 	private PheidippidesService getService() {
 		if (service == null) {
 			ELUtil.getInstance().autowire(this);
 		}
 
 		return service;
 	}
 
 	private JQuery getJQuery() {
 		if (jQuery == null) {
 			ELUtil.getInstance().autowire(this);
 		}
 		
 		return jQuery;
 	}
 
 	private String getBundleIdentifier() {
 		return PheidippidesConstants.IW_BUNDLE_IDENTIFIER;
 	}
 }
