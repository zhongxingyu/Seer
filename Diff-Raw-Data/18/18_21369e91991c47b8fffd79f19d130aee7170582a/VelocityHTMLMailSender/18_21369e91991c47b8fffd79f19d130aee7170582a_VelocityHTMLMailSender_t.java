 package org.rotarysource.listener.mail.html;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.velocity.app.VelocityEngine;
 import org.apache.velocity.exception.VelocityException;
 import org.rotarysource.listener.mail.domain.MailDataBean;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.mail.MailException;
 import org.springframework.mail.MailPreparationException;
 import org.springframework.ui.velocity.VelocityEngineUtils;
 import org.springframework.util.Assert;
 
 public class VelocityHTMLMailSender extends HTMLBaseMailSender{
 
 	private static Logger  log = LoggerFactory.getLogger(VelocityHTMLMailSender.class);
 	
 	/**
 	 * Mail text String array index
 	 */	
 	private static final int MAIL_TEXT = 0;
 
 	/**
 	 * Mail Headered text String array index
 	 */		
 	private static final int MAIL_HEADERED_TEXT = 1;
 	
 	
 	/**
 	 * Store a template name for VelocityEngine.
 	 */
 	private String templateName;
 
 	/**
 	 * Velocity Engine for Mail dynamic generation
 	 */
 	private VelocityEngine velocityEngine;
 	
 	/**
 	 * Velocity Engine for Mail dynamic generation
 	 */
 	private String templateEncoding;
 	
 	
 
 	/**
 	 * Build an object instance with mandatory dependencies.
 	 * @param aiTemplateName String Used Velocity template Name. Template path must be customized in VelocityEngine setup.
 	 * @param aiVelocityEngine Velocity Engine reference <link>@see org.springframework.ui.velocity.VelocityEngineFactoryBean"</link>
 	 */
 	public VelocityHTMLMailSender(String aiTemplateName,
 			VelocityEngine aiVelocityEngine) {
 		super();
 		
 		Assert.notNull(aiTemplateName);
 		Assert.notNull(aiVelocityEngine);
 
 		this.templateName = aiTemplateName;
 		this.velocityEngine = aiVelocityEngine;
 		this.templateEncoding = "UTF-8";
 	}	
 	
 	
 	
 	public String sendMail(MailDataBean setupParams, Map<String, Object> composeParams,
 			String[] attachments, String[] resources) throws IllegalArgumentException,
 			MailException, RuntimeException {
 		
 			// Composition
 			String[] mailTexts = composeMail(composeParams);
 
 			// Send mail
 			super.sendMail(setupParams, mailTexts[MAIL_TEXT], attachments, resources);
 	
			return mailTexts[MAIL_HEADERED_TEXT];
 		
 	}
 	
 	
 	
	/**
	 * Compose a HTML test using the configured velocity engine. This method allow to
	 * generate two HTML versions.
	 *    <p>MAIL_TEXT is the text version to be sended.<p>
	 *    <p>MAIL_HEADERED_TEXT is a instrumentialized version that include Header information. To generate it,
	 *    velocity template must include a section like this:<p>
	 *    <p> #if ( ${includeHeader} == 1)
	 *        //// your code here
	 *        #end
	 *     <p>
	 * @param mailVariablesMap Variables to be substitute in velocity template
	 * @return String array with two rows: result[ MAIL_TEXT] and result[ MAIL_HEADERED_TEXT]
	 * @throws IllegalArgumentException Bad arguments used
	 * @throws MailPreparationException HTML text could not be composed
	 */
 	private String[] composeMail(Map<String, Object> mailVariablesMap)
 			throws IllegalArgumentException, MailPreparationException {
 		
 		String[] result = new String[2];
 
 		// Local copy of template variables
 		Map<String, Object> templateVars = new HashMap<String, Object>(mailVariablesMap);
 		
 		try{
 			// Get Mail without header
 			templateVars.put("includeHeader", 0);
 			result[ MAIL_TEXT] 			= VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, templateName, templateEncoding, templateVars);
 			// Get mail with header
 			templateVars.put("includeHeader", 1);
 	        result[ MAIL_HEADERED_TEXT] = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, templateName, templateEncoding, templateVars);;
 		}
 		catch(VelocityException exception){
 			//Map to MailPreparationException
 			log.error("Failure preparing Mail text. Nexted Exception: {}", exception.getMessage());
 			
 			throw new MailPreparationException("Failure preparing Mail text",exception);			
 		}
 		return result;
 	}
 
 	/**
 	 * Sets Template for this composer.
 	 *
 	 * @param aiTemplateName 
 	 *		String template identification
 	 */
 	public void setTemplateName(String aiTemplateName) {
 		this.templateName = aiTemplateName;
 	}
 
 	/**
 	 * Sets the Velocity Engine for mail dynamic generation
 	 *
 	 * @param aiVelocityEngine 
 	 *		Velocity Engine reference
 	 */	
 	public void setVelocityEngine(VelocityEngine aiVelocityEngine) {
 		this.velocityEngine = aiVelocityEngine;
 	}
 
 
 
 	public void setTemplateEncoding(String templateEncoding) {
 		this.templateEncoding = templateEncoding;
 	}
 }
