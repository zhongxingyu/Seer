 package gov.nih.nci.security.loginapp.actions;
 
 
 import gov.nih.nci.logging.api.user.UserInfoHelper;
 import gov.nih.nci.security.AuthenticationManager;
 import gov.nih.nci.security.AuthorizationManager;
 import gov.nih.nci.security.SecurityServiceProvider;
 import gov.nih.nci.security.UserProvisioningManager;
 import gov.nih.nci.security.authorization.domainobjects.Application;
 import gov.nih.nci.security.exceptions.CSConfigurationException;
 import gov.nih.nci.security.exceptions.CSException;
 import gov.nih.nci.security.loginapp.constants.DisplayConstants;
 import gov.nih.nci.security.loginapp.constants.ForwardConstants;
 import gov.nih.nci.security.loginapp.forms.LoginForm;
 import gov.nih.nci.security.loginapp.util.StringUtils;
 import gov.nih.nci.security.loginapp.util.properties.ObjectFactory;
 import gov.nih.nci.security.loginapp.util.properties.UPTApplication;
 import gov.nih.nci.security.loginapp.util.properties.UPTProperties;
 import gov.nih.nci.security.loginapp.util.properties.exceptions.UPTConfigurationException;
 
 import gov.nih.nci.security.util.StringUtilities;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.Driver;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import javax.sql.DataSource;
 
 import org.apache.log4j.Logger;
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 
 public class LoginAction extends Action 
 {	
 	private static final Logger log = Logger.getLogger(LoginAction.class);
 	
 	@SuppressWarnings("unchecked")
 	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
 	{
 
 		
 		
 		ActionErrors errors = new ActionErrors();		
 
 		AuthenticationManager authenticationManager = null;
 		AuthorizationManager authorizationManager = null;
 		UserProvisioningManager userProvisioningManager = null;
 		boolean loginSuccessful = false;
 		boolean hasPermission = false;
 		String uptContextName = DisplayConstants.UPT_CONTEXT_NAME;
 		String uptApplicationContextName = "";
 		
 		Application application = null;
 		
 		LoginForm loginForm = (LoginForm)form;
 		UserInfoHelper.setUserInfo(loginForm.getLoginId(), request.getSession().getId());
 		errors.clear();
 
 		/*try
 		{
 			authorizationManager = SecurityServiceProvider.getAuthorizationManager(uptContextName);
 			if (null == authorizationManager)
 			{
 				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(DisplayConstants.ERROR_ID, "Unable to initialize Authorization Manager for the given application context using new configuration"));
 				saveErrors( request,errors );
 				if (log.isDebugEnabled())
 					log.debug("|"+loginForm.getLoginId()+
 							"||Login|Failure|Unable to instantiate Authorization Manager for UPT application using new configuration||");
 				return mapping.findForward(ForwardConstants.LOGIN_FAILURE);
 			}
 		}
 		catch (CSException cse)
 		{
 //			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(DisplayConstants.ERROR_ID, cse.getMessage()));			
 //			saveErrors( request,errors );
 //			if (log.isDebugEnabled())
 //				log.debug("|"+loginForm.getLoginId()+
 //						"||Login|Failure|Unable to instantiate AuthorizationManager for UPT application|"+loginForm.toString()+"|"+cse.getMessage());
 //			return mapping.findForward(ForwardConstants.LOGIN_FAILURE);
 			authorizationManager = null;
 		}
 
 		if (null == authorizationManager)
 		{
 
 			try
 			{
 				uptContextName = getUPTContextName();
 				if (null == uptContextName || uptContextName.equalsIgnoreCase(""))
 				{
 					errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(DisplayConstants.ERROR_ID, "Unable to read the UPT Context Name from Security Config File"));			
 					saveErrors( request,errors );
 					if (log.isDebugEnabled())
 						log.debug("|"+loginForm.getLoginId()+
 								"||Login|Failure|Unable to read the UPT Context Name from Security Config File");
 					return mapping.findForward(ForwardConstants.LOGIN_FAILURE);
 				}
 			}
 			catch (Exception ex)
 			{
 				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(DisplayConstants.ERROR_ID, ex.getMessage()));			
 				saveErrors( request,errors );
 				if (log.isDebugEnabled())
 					log.debug("|"+loginForm.getLoginId()+
 							"||Login|Failure|Unable to read the UPT Context Name from Security Config File||");
 				return mapping.findForward(ForwardConstants.LOGIN_FAILURE);
 			}
 		}*/
 		try
 		{
 			
 			authenticationManager = SecurityServiceProvider.getAuthenticationManager(uptContextName);
 			if (null == authenticationManager)
 			{
 				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(DisplayConstants.ERROR_ID, "Unable to initialize Authentication Manager for the given application context"));			
 				saveErrors( request,errors );
 				if (log.isDebugEnabled())
 					log.debug("|"+loginForm.getLoginId()+
 							"||Login|Failure|Unable to instantiate AuthenticationManager for UPT application||");
 				return mapping.findForward(ForwardConstants.LOGIN_FAILURE);
 			}
 		}
 		catch (CSException cse)
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(DisplayConstants.ERROR_ID, cse.getMessage()));			
 			saveErrors( request,errors );
 			if (log.isDebugEnabled())
 				log.debug("|"+loginForm.getLoginId()+
 						"||Login|Failure|Unable to instantiate AuthenticationManager for UPT application|"+loginForm.toString()+"|"+cse.getMessage());
 			return mapping.findForward(ForwardConstants.LOGIN_FAILURE);
 		}
 		try
 		{
 			loginSuccessful = authenticationManager.login(loginForm.getLoginId(),loginForm.getPassword());
 		}
 		catch (CSException cse)
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(DisplayConstants.ERROR_ID, DisplayConstants.LOGIN_EXCEPTION_MESSAGE));
 			saveErrors( request,errors );
 			if (log.isDebugEnabled())
 				log.debug("|"+loginForm.getLoginId()+
 						"||Login|Failure|Login Failed for user name "+loginForm.getLoginId()+" and"+loginForm.getApplicationContextName()+" application|"+loginForm.toString()+"|"+cse.getMessage());
 			return mapping.findForward(ForwardConstants.LOGIN_FAILURE);
 		}
 		
 		
 		
 		ObjectFactory.initialize("upt-beans.xml");
 		UPTProperties uptProperties = null;
 		try {
 			uptProperties = (UPTProperties)ObjectFactory.getObject("UPTProperties");
 		} catch (UPTConfigurationException e) {
 		
 			e.printStackTrace();
 		}
 		
 		boolean isCentralUPTwithCSMUPTContext = false;
 		List<UPTApplication> lista = null;
 		Iterator listIterator = null;
 		
 		String text1 = uptProperties.getBackwardsCompatibilityInformation().getCentralUPTConfiguration();
 		if("true".equalsIgnoreCase(text1)){
 			isCentralUPTwithCSMUPTContext = true;
 			
 			// Get the UPT Application Context (the superadmin mode Application Context URL).
 			lista= uptProperties.getBackwardsCompatibilityInformation().getUptApplicationsList();
 			Iterator listIterate = lista.iterator();
 			while(listIterate.hasNext()){
 				UPTApplication uptApp = (UPTApplication) listIterate.next();
 				if(DisplayConstants.UPT_CONTEXT_NAME.equalsIgnoreCase(uptApp.getContextName())){
 					uptApplicationContextName = uptApp.getContextNameURL();
 				}
 			}
 			
 			
 		}else{
 			lista= uptProperties.getBackwardsCompatibilityInformation().getUptApplicationsList();
 			Collections.sort(lista);
 			Collections.reverse(lista);
 			listIterator = lista.iterator();	
 		}
 		
 	
 		int forLoopCount = 0;
 		
 		
 		if(isCentralUPTwithCSMUPTContext){
 			//authorizationManager = SecurityServiceProvider.getAuthorizationManager(uptContextName); - done below
 			// Set currentUptContextName = "csmupt"; done below
 			
 			// Based on the application (non SuperAdmin) context name, determine the version of CSM schema.
 			// a) modify csm_application table and add column to indicate version of the application.
 			// b) in the logic below, before setting request.setAttribute(DisplayConstants.APPLICATION_CONTEXT,uptApplicationContextName), 
 			//    make sure uptApplicationContextName is appropriately set.
 			
 			//set For loop to iterate once.
 			forLoopCount = 1;
 		}else{
 			if(null==lista){
 				forLoopCount = 1;
 			}else{
 				forLoopCount = lista.size();
 			}
 		}
 		
 		
 			//String[] currentUptContextNames = { "csmupt41","csmupt40","csmupt32"};
 			
 			boolean authorizationSuccess = false;
 			for(int i=0;i<forLoopCount; i++){
 				
 				if(authorizationSuccess) continue;
 				
 				
 				
 				boolean isLastContext = false;
 				if(forLoopCount==1){
 					isLastContext = true;
 				}else{
 					isLastContext = ((i+1)==lista.size()?true:false);
 				}
 				
 				String currentUptContextName = null;//currentUptContextNames[i];
 				
 				UPTApplication ua;
 				
 				if(isCentralUPTwithCSMUPTContext){
 					currentUptContextName = DisplayConstants.UPT_CONTEXT_NAME;
 					
 				}else{
 					ua = (UPTApplication) listIterator.next();
 					currentUptContextName = ua.getContextName();
 					uptApplicationContextName = ua.getContextNameURL();
 					
 				}
 				
 				 
 				
 				uptContextName = currentUptContextName;
 				
 				
 				
 				
 				try
 				{	
 					authorizationManager = SecurityServiceProvider.getAuthorizationManager(uptContextName);
 				}
 				catch (CSException cse)
 				{
 					// Probably CSM UPT for this context is not configured correctly or not available.
 					
 					continue; 
 				}
 			
 			
 				if (null == authorizationManager)
 				{
 					errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(DisplayConstants.ERROR_ID, "Unable to initialize Authorization Manager for the given application context"));			
 					saveErrors( request,errors );
 					if (log.isDebugEnabled())
 						log.debug("|"+loginForm.getLoginId()+
 								"||Login|Failure|Unable to instantiate Authorization Manager for UPT application||");
 					return mapping.findForward(ForwardConstants.LOGIN_FAILURE);
 				}
 			
 				try
 				{
 					hasPermission = authorizationManager.checkPermission(loginForm.getLoginId(),loginForm.getApplicationContextName(),null);
 					if (!hasPermission)
 					{
 						if(isLastContext){
 							errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(DisplayConstants.ERROR_ID, "Access permission denied for the application" ));				
 							saveErrors( request,errors );
 							if (log.isDebugEnabled())
 								log.debug("|"+loginForm.getLoginId()+
 										"||Login|Failure|User "+loginForm.getLoginId()+" doesnot have permission on "+loginForm.getApplicationContextName()+" application||");
 							return mapping.findForward(ForwardConstants.LOGIN_FAILURE);
 						}else{
 							authorizationManager= null;
 							continue;
 						}
 					}
 				}
 				catch (CSException cse)
 				{
 					errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(DisplayConstants.ERROR_ID, cse.getMessage()));			
 					saveErrors( request,errors );
 					if (log.isDebugEnabled())
 						log.debug("|"+loginForm.getLoginId()+
 								"||Login|Failure|Error in checking permission|"+loginForm.toString()+"|"+cse.getMessage());
 					return mapping.findForward(ForwardConstants.LOGIN_FAILURE);			
 				}		
 				try
 				{
 
 					application = authorizationManager.getApplication(loginForm.getApplicationContextName());
 					
 					if(application!= null && isCentralUPTwithCSMUPTContext){
 
 						// Determine the UPT Application Context Name for the non-superadmin Application.
 						String csmversion = getCSMVersionForApplicationViaJDBC(uptContextName, application.getApplicationName());
 						
 						if(DisplayConstants.UPT_CONTEXT_NAME.equalsIgnoreCase(application.getApplicationName())){
 							// no need to specify the uptApplicationContextName again since its already done above. 
 						}else{
 						
 							if(!StringUtilities.isBlank(csmversion)){
 								
 	
 								if(csmversion.equalsIgnoreCase("3.1")){
 									uptApplicationContextName = "upt31";	
 								}
 								if(csmversion.equalsIgnoreCase("3.2")){
 									uptApplicationContextName = "upt32";
 								}
 								if(csmversion.equalsIgnoreCase("4.0")){
 									uptApplicationContextName = "upt40";
 								}
 								if(csmversion.equalsIgnoreCase("4.1")){
 									uptApplicationContextName = "upt41";
 								}
 								if(csmversion.equalsIgnoreCase("4.2")){
 									uptApplicationContextName = "upt42";
 								}
 							}
 						}	
 							//Note for Central UPT (backwards version) hosting: If csmversion is not mentioned in the record 
 							// then the UPT Application Context Name (URL Context) shall be same as the 'csmupt' application context.
 						
 					}
 					
 					/*if (application!= null && !StringUtilities.isBlank(application.getDatabaseURL()))
 					{
 						HashMap hashMap = new HashMap();
 						hashMap.put("hibernate.connection.url", application.getDatabaseURL());
 						hashMap.put("hibernate.connection.username", application.getDatabaseUserName());
 						hashMap.put("hibernate.connection.password", application.getDatabasePassword());
 						hashMap.put("hibernate.dialect", application.getDatabaseDialect());
 						hashMap.put("hibernate.connection.driver_class", application.getDatabaseDriver());
 						userProvisioningManager = SecurityServiceProvider.getUserProvisioningManager(loginForm.getApplicationContextName(),hashMap);
 					}
 					else
 					{
 						userProvisioningManager = SecurityServiceProvider.getUserProvisioningManager(loginForm.getApplicationContextName());				
 					}
 					if (null == userProvisioningManager)
 					{
 						errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(DisplayConstants.ERROR_ID, "Unable to initialize Authorization Manager for the given application context"));			
 						saveErrors( request,errors );
 						if (log.isDebugEnabled())
 							log.debug("|"+loginForm.getLoginId()+
 									"||Login|Failure|Unable to instantiate User Provisioning Manager for "+loginForm.getApplicationContextName()+" application||");
 						return mapping.findForward(ForwardConstants.LOGIN_FAILURE);
 					}*/
 				}
 				catch (CSException cse)
 				{
 					errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(DisplayConstants.ERROR_ID, cse.getMessage()));			
 					saveErrors( request,errors );
 					if (log.isDebugEnabled())
 						log.debug("|"+loginForm.getLoginId()+
 								"||Login|Failure|Unable to instantiate User Provisioning Manager for |"+loginForm.toString()+"|"+cse.getMessage());
 					return mapping.findForward(ForwardConstants.LOGIN_FAILURE);			
 				}
 				
 				authorizationSuccess=true;
 				
 			}
 			
 		
 		HttpSession session = request.getSession(true);		
 		
 		authenticationManager = null;
 		authorizationManager = null;
 		
 		request.setAttribute("LOGIN_OBJECT",loginForm);
 		
 		if(isCentralUPTwithCSMUPTContext){
 			// Based on the application (non SuperAdmin) context name, determine the version of CSM schema.
 			// a) modify csm_application table and add column to indicate version of the application.
 			// b) Retrieve the version column details for the 'uptContextName' 
 			// c)in the logic below, before setting request.setAttribute(DisplayConstants.APPLICATION_CONTEXT,uptApplicationContextName), 
 			//    make sure uptApplicationContextName is appropriately set.
 			
 			
 			
 			request.setAttribute(DisplayConstants.APPLICATION_CONTEXT,uptApplicationContextName);
 		}else{
 			request.setAttribute(DisplayConstants.APPLICATION_CONTEXT,uptApplicationContextName);	
 		}
 		
 		
 		
 		if (((LoginForm)form).getApplicationContextName().equalsIgnoreCase(uptContextName))
 		{
 			request.setAttribute(DisplayConstants.ADMIN_USER,DisplayConstants.ADMIN_USER);
 
 			if (log.isDebugEnabled())
 				log.debug(session.getId()+"|"+loginForm.getLoginId()+
 				"||Login|Success|Login Successful for user "+loginForm.getLoginId()+" and "+loginForm.getApplicationContextName()+" application, Forwarding to the Super Admin Home Page||");
 			return (mapping.findForward("AdminLoginSuccessBounce"));
 		}
 		else
 		{
 			
 
 			
 			if (log.isDebugEnabled())
 				log.debug(session.getId()+"|"+loginForm.getLoginId()+
 				"||Login|Success|Login Successful for user "+loginForm.getLoginId()+" and "+loginForm.getApplicationContextName()+" application, Forwarding to the Home Page||");
 			return (mapping.findForward("LoginSuccessBounce"));
 		}
 	}
 	
 
 	
 	/**
 	 * testConnection method accepts
 	 * @param uptApplicationContextName 
 	 * 
 	 * @param appForm -
 	 *            The ApplicationForm with application database parameters to
 	 *            test connection for.
 	 * @return String - The message indicating that connection and a SQL query
 	 *         was successful
 	 * @throws CSException -
 	 *             The exception message indicates which kind of application
 	 *             database parameters are invalid.
 	 */
 	private static String getCSMVersionForApplicationViaJDBC(String applicationContextName, String applicationName) throws CSException {
 		
 		String version = null;
 		try {
 			
 			Context ctx = new InitialContext();
			DataSource ds = null;
			//System.out.println("Looking up initial context for: "+applicationContextName);
            if (gov.nih.nci.security.loginapp.util.ServerDetector.isJBoss()) {  
            	ds = (DataSource)ctx.lookup("java:"+applicationContextName);  
            } else if (gov.nih.nci.security.loginapp.util.ServerDetector.isTomcat()) {
            	ds = (DataSource)ctx.lookup("java:/comp/env/"+applicationContextName);
            	System.out.println("Looking up initial context for tomcat ");
            }

			
            if(ds == null)
    			throw new CSException(
    					DisplayConstants.APPLICATION_DATABASE_CONNECTION_FAILED_DRIVER);
            
 			Connection con = ds.getConnection();
 			con.setAutoCommit(false);
 			PreparedStatement pstmt = con.prepareStatement(
 			                            "SELECT csm_version FROM csm_application WHERE application_name = ?");
 			pstmt.setString(1, applicationName);
 			ResultSet rs = pstmt.executeQuery();
 			while (rs.next()) {
 			        version = rs.getString("csm_version");
 			}
 			
 			rs.close();
 			pstmt.close();
 			con.close();
 
 		} catch (SQLException e) {
 			throw new CSException(
 					DisplayConstants.APPLICATION_DATABASE_CONNECTION_FAILED_URL_USER_PASS);
 		} catch (NamingException e) {
 			throw new CSException(
 					DisplayConstants.APPLICATION_DATABASE_CONNECTION_FAILED_DRIVER);
 		} 
 
 		return version;
 	}
 
 	private UserProvisioningManager getAuthorizationManager(String contextName) throws CSConfigurationException, CSException {
 		contextName = "csmupt41";
 		if(StringUtilities.isBlank(contextName)){
 			return null;
 			
 		}
 		// TODO Auto-generated method stub
 		HashMap<String,String> connectionProperties = new HashMap<String, String>();
 		
 		if("csmupt41".equalsIgnoreCase(contextName))
 			connectionProperties.put("hibernate.connection.url","jdbc:mysql://localhost:3306/csmauthschema_dev_bkwrdscmptbl_4_1");
 		if("csmupt40".equalsIgnoreCase(contextName))
 			connectionProperties.put("hibernate.connection.url","jdbc:mysql://localhost:3306/csmauthschema_dev_bkwrdscmptbl_4_0");
 		if("csmupt32".equalsIgnoreCase(contextName))
 			connectionProperties.put("hibernate.connection.url","jdbc:mysql://localhost:3306/csmauthschema_dev_bkwrdscmptbl_3_2");
 		/*if("csmupt31".equalsIgnoreCase(contextName))
 			connectionProperties.put("hibernate.connection.url","jdbc:mysql://localhost:3306/csmauthschema_dev_bkwrdscmptbl_3_1");*/
 		
 		
 		connectionProperties.put("hibernate.connection.username","root");
 		connectionProperties.put("hibernate.connection.password","admin");
 		connectionProperties.put("hibernate.dialect","org.hibernate.dialect.MySQLDialect");
 		connectionProperties.put("hibernate.connection.driver_class","org.gjt.mm.mysql.Driver");
 		
 		
 	/*	connectionProperties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
 		connectionProperties.put("hibernate.connection.datasource", "java:/"+contextName);*/
 		
 		UserProvisioningManager upm = null;
 	  
 			upm = 	SecurityServiceProvider.getUserProvisioningManager(contextName,connectionProperties );
 		
 		
 		
 		
 		
 	return upm;
 		
 		
 	}
 
 	private static String getUPTContextName() throws Exception
 	{
 	
 		//TODO : Get UPT Context Name from upt-properties.xml
 		Document configDocument = null;
 		String uptContextNameValue = null;
 		String configFilePath = System.getProperty(DisplayConstants.CONFIG_FILE_PATH_PROPERTY_NAME);
 		if (null == configFilePath || configFilePath.trim().equals(""))
 			throw new CSConfigurationException("The system property gov.nih.nci.security.configFile is not set");
 		
 		SAXBuilder builder = new SAXBuilder();
 		try
 		{
 			configDocument = builder.build(new File(configFilePath));
 		}
 		catch (JDOMException e)
 		{
 			throw new CSConfigurationException("Error in parsing the Application Security Config file");
 		}
 		catch (IOException e)
 		{
 			throw new CSConfigurationException("Error in reading the Application Security Config file");
 		}
 		if (configDocument != null)
 		{
 			Element securityConfig = configDocument.getRootElement();
 			Element uptContextName = securityConfig.getChild("upt-context-name");
 			uptContextNameValue = uptContextName.getText().trim();
 		}
 		return uptContextNameValue;
 	}
 	
 }
