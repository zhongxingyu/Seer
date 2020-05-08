 
 package org.paxle.gui.impl.servlets;
 
 import java.awt.image.BufferedImage;
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Dictionary;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 import java.util.Vector;
 
 import javax.imageio.ImageIO;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileItemFactory;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.velocity.Template;
 import org.apache.velocity.context.Context;
 import org.apache.velocity.exception.ParseErrorException;
 import org.apache.velocity.exception.ResourceNotFoundException;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.Constants;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.cm.Configuration;
 import org.osgi.service.cm.ConfigurationAdmin;
 import org.osgi.service.metatype.AttributeDefinition;
 import org.osgi.service.metatype.MetaTypeInformation;
 import org.osgi.service.metatype.MetaTypeProvider;
 import org.osgi.service.metatype.MetaTypeService;
 import org.osgi.service.metatype.ObjectClassDefinition;
 import org.osgi.service.useradmin.Authorization;
 import org.osgi.service.useradmin.Group;
 import org.osgi.service.useradmin.Role;
 import org.osgi.service.useradmin.User;
 import org.osgi.service.useradmin.UserAdmin;
 import org.paxle.gui.ALayoutServlet;
 import org.paxle.gui.IServletManager;
 import org.paxle.gui.IStyleManager;
 import org.paxle.gui.impl.HttpContextAuth;
 import org.paxle.gui.impl.ServiceManager;
 import org.paxle.gui.impl.ServletManager;
 import org.paxle.tools.ieporter.cm.IConfigurationIEPorter;
 
 public class SettingsView extends ALayoutServlet {
 	private static final long serialVersionUID = 1L;
 	
 	private static final String ERROR_MSG = "errorMsg";
 	
 	/**
 	 * A mapping between type-nr and type string
 	 */
     private static final Map<Integer,String> dataTypes = new HashMap<Integer,String>();
     static {
     	dataTypes.put(Integer.valueOf(AttributeDefinition.BOOLEAN), "Boolean");
     	dataTypes.put(Integer.valueOf(AttributeDefinition.BYTE),"Byte");
     	dataTypes.put(Integer.valueOf(AttributeDefinition.CHARACTER),"Character");
     	dataTypes.put(Integer.valueOf(AttributeDefinition.DOUBLE),"Double");
     	dataTypes.put(Integer.valueOf(AttributeDefinition.FLOAT),"Float");
     	dataTypes.put(Integer.valueOf(AttributeDefinition.INTEGER),"Integer");
     	dataTypes.put(Integer.valueOf(AttributeDefinition.LONG),"Long");
     	dataTypes.put(Integer.valueOf(AttributeDefinition.SHORT),"Short");
     	dataTypes.put(Integer.valueOf(AttributeDefinition.STRING),"String");
     }	
     
     /**
      * For logging
      */
 	private Log logger = LogFactory.getLog( this.getClass());
 
 	/**
 	 * @param context
 	 * @return a map required by the {@link IConfigurationIEPorter} to export {@link Configuration#getProperties() configuration-properties}
 	 */
 	private Map<String, String> generatePidBundleLocationMap(HttpServletRequest request, Context context) {
 		// getting the service manager
 		ServiceManager manager = (ServiceManager) context.get(SERVICE_MANAGER);
 		if (manager == null) throw new IllegalStateException("ServiceManager not found");
 		
 		// getting the metatype service
 		MetaTypeService metaTypeService = (MetaTypeService) manager.getService(MetaTypeService.class.getName());
 		if (metaTypeService == null) throw new IllegalStateException("MetaTypeService not found");
 
 		String bundleID = request.getParameter("bundleID");
 		String pid = request.getParameter("pid");
 		
 		Map<String, String> pidBundleLocationTupel = new HashMap<String, String>();
 		if (bundleID != null && pid != null) {
 			Bundle bundle = manager.getBundle(Integer.parseInt(bundleID));
 			pidBundleLocationTupel.put(pid, bundle.getLocation());
 		} else {		
 			// loop through all bundles to see if they contain manageable services			
 			for (Bundle bundle: manager.getBundles()) {
 				// getting the metatypeinformation of the bundle
 				MetaTypeInformation metaType = metaTypeService.getMetaTypeInformation(bundle);
 							
 				// the bundleID comparison is necessary due to a bug in knopflerfish
 				if (metaType != null && metaType.getBundle().getBundleId() == bundle.getBundleId()) {
 					String[] pids = metaType.getPids();
 					if (pids != null && pids.length > 0) {
 						for (String nextPid : pids) {
 							pidBundleLocationTupel.put(nextPid, bundle.getLocation());
 						}
 					}
 				}
 			}
 		}
 		
 		return pidBundleLocationTupel;
 	}
 	
 	@Override
 	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
 		if (request.getParameter("getImage") != null) {	
 			try {
 				/*
 				 * this is a template less request therefore we need to overwrite doGet
 				 */
 
 				// create context
 				Context context = this.createContext(request, response);
 				this.writeImage(request, response, context);
 			} catch (Exception e) {
 				throw new IOException(e.getMessage());
 			}
 		} else if (request.getParameter("doExportConfig") != null) {
 			InputStream fileIn = null;
 			File tempFile = null;
 			try {
 				// create context
 				Context context = this.createContext(request, response);
 
 				// getting the config-exporter
 				ServiceManager manager = (ServiceManager) context.get(SERVICE_MANAGER);
 				IConfigurationIEPorter exporter = (IConfigurationIEPorter) manager.getService(IConfigurationIEPorter.class.getName());
 
 				// export configuration
 				response.setContentType("application/zip");
 				response.setHeader("Content-Disposition", "attachment; filename=paxleConfig" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".zip");
 				
 				tempFile = exporter.exportConfigsAsZip(this.generatePidBundleLocationMap(request, context));
 				fileIn = new BufferedInputStream(new FileInputStream(tempFile));
 
 				IOUtils.copy(fileIn, response.getOutputStream());
 
 				fileIn.close();
 				tempFile.delete();
 			} catch (Exception e) {
 				throw new IOException(e.getMessage());
 			} finally {
 				if (fileIn != null) try { fileIn.close(); } catch (Exception e) {/* ignore this */}
 				if (tempFile != null) try { tempFile.delete(); } catch (Exception e) {/* ignore this */}
 			}
 		} else {
 			super.doGet(request, response);
 		}
 	}
 
 	@Override
 	public Template handleRequest(HttpServletRequest request, HttpServletResponse response, Context context) throws Exception {
 		
 		Template template = null;
 		try {
 			// getting the servicemanager
 			ServiceManager manager = (ServiceManager) context.get(SERVICE_MANAGER);
 			ServletManager sManager = (ServletManager) manager.getService(IServletManager.class.getName());
 			
 			/* ====================================================================================
 			 * USER MANAGEMENT 
 			 * ==================================================================================== */
 			if (request.getParameter("doCreateUser") != null || request.getParameter("doUpdateUser") != null) {
 				UserAdmin userAdmin = (UserAdmin) manager.getService(UserAdmin.class.getName());
 				if (userAdmin != null) {
 					String name = request.getParameter("roleName");
 					Role user = null;
 					if (request.getParameter("doCreateUser") != null) {
 						user = userAdmin.createRole(name, Role.USER);
 						if (user == null) {
 							String errorMsg = String.format("Role with name '%s' already exists.",name);
 							this.logger.warn(errorMsg);
 							context.put("errorMsg",errorMsg);
 						}
 					} else if (request.getParameter("doUpdateUser") != null) {
 						user = userAdmin.getRole(name);
 						if (user == null) {
 							String errorMsg = String.format("Unable to find user with name '%s'.",name);
 							this.logger.warn(errorMsg);
 							context.put("errorMsg",errorMsg);
 						}
 					}
 					
 					this.updateUser(userAdmin, (User) user, request, context);
 				}
 				response.sendRedirect(request.getServletPath() + "?settings=user");
 			} else if (request.getParameter("doCreateGroup") != null || request.getParameter("doUpdateGroup") != null) {
 				UserAdmin userAdmin = (UserAdmin) manager.getService(UserAdmin.class.getName());
 				if (userAdmin != null) {
 					String name = request.getParameter("roleName");
 					Role group = null;
 					if (request.getParameter("doCreateGroup") != null) {
 						group = userAdmin.createRole(name, Role.GROUP);
 						if (group == null) {
 							String errorMsg = String.format("Role with name '%s' already exists.",name);
 							this.logger.warn(errorMsg);
 							context.put("errorMsg",errorMsg);
 						}
 					} else if (request.getParameter("doUpdateGroup") != null) {
 						group = userAdmin.getRole(name);
 						if (group == null) {
 							String errorMsg = String.format("Unable to find group with name '%s'.",name);
 							this.logger.warn(errorMsg);
 							context.put("errorMsg",errorMsg);
 						}
 						
 						// XXX nothing to edit at the moment
 					}
 				}
 			} else if (request.getParameter("doDeleteGroup") != null || request.getParameter("doDeleteUser") != null) {
 				UserAdmin userAdmin = (UserAdmin) manager.getService(UserAdmin.class.getName());
 				if (userAdmin != null) {
 					String name = request.getParameter("roleName");
 					Role role = userAdmin.getRole(name);
 					if (!role.getName().equals("Administrator")) {
 						boolean done = userAdmin.removeRole(name);
 						if (!done) context.put("errorMsg","Unable to delete role");
 					} else {
 						context.put("errorMsg","The administrator can not be deleted.");
 					}
 				}
 				response.sendRedirect(request.getServletPath() + "?settings=user");
 				
 			/* ====================================================================================
 			 * CONFIGURATION MANAGEMENT 
 			 * ==================================================================================== */
 				
 			} else if (request.getParameter("doEditConfig") != null) {
 				this.setPropertyValues(request, response, context, false);
 				if (ServletManager.PID.equals(request.getParameter("pid"))) {
 					String newPrefix = request.getParameter("org.paxle.gui.IServletManager.pathPrefix");
 					context.put("delayedRedirect",sManager.getFullAlias(newPrefix, "/config"));
 				} else {
 					response.sendRedirect(request.getServletPath() + "?settings=config");
 				}
 			} else if (request.getParameter("doResetConfig") != null) {
 				this.setPropertyValues(request, response, context, true);
 				if (ServletManager.PID.equals(request.getParameter("pid"))) {
 					String newPrefix = request.getParameter("org.paxle.gui.IServletManager.pathPrefix");
 					context.put("delayedRedirect",sManager.getFullAlias(newPrefix, "/config"));
 				} else {
 					response.sendRedirect(request.getServletPath() + "?settings=config");
 				}
 				
 			} else if (request.getParameter("viewImportedConfig") != null) {
 				if (ServletFileUpload.isMultipartContent(request)) {
 					// import config from file
 					Map<String, Dictionary<String, Object>> propsMap = this.importConfig(request, context);
 					
 					// add it into the context so that the servlet can read it
 					context.put("importedConfigProps", propsMap);
 					
 					// add it into the session so that the servlet can read it lateron
 					HttpSession session = request.getSession(true);
 					session.setAttribute("importedConfigProps", propsMap);
 				} else {
 					// read the imported-config from session into context so that the sevlet can read it
 					HttpSession session = request.getSession(true);
 										
 					Map<String, Dictionary<String, Object>> propsMap = (Map<String, Dictionary<String, Object>>) session.getAttribute("importedConfigProps");
 					context.put("importedConfigProps", propsMap);
 					
 					if (request.getParameter("doImportConfig") != null) {
 						// configure properties
 						this.setPropertyValues(request, response, context, false);
 						
 						// remove already imported properties from map
 						propsMap.remove(request.getParameter("pid"));
 						
 						// redirect to overview
 						response.sendRedirect(request.getServletPath() + "?settings=config&viewImportedConfig=");
 					}
 				}
 			} else if (request.getParameter("doInstallStyle") != null && ServletFileUpload.isMultipartContent(request)) {
 				// getting a reference to the stylemanager
 				IStyleManager styleManager = (IStyleManager) manager.getService(IStyleManager.class.getName());
 				
 				// get the file
 				this.installStyle(styleManager, request, context);
 			}
 
 			context.put("dataTypes", dataTypes);
 			context.put("settingsView", this);
 
 			template = this.getTemplate( "/resources/templates/SettingsView.vm");
 		} catch (ResourceNotFoundException e) {
 			logger.error( "resource: " + e);
 			e.printStackTrace();
 		} catch (ParseErrorException e) {
 			logger.error( "parse : " + e);
 			e.printStackTrace();
 		} catch (Exception e) {
 			logger.error( "exception: " + e);
 			e.printStackTrace();
 		}
 
 		return template;
 	}
 	
 	private void updateUser(UserAdmin userAdmin, User user, HttpServletRequest request, Context context) throws InvalidSyntaxException, UnsupportedEncodingException {
 		if (user == null) return;
 		
 		String loginName = request.getParameter(HttpContextAuth.USER_HTTP_LOGIN);
 		
 		/* ===========================================================
 		 * USERNAME + PWD
 		 * =========================================================== */
 		// check if the login-name is not empty
 		if (loginName == null || loginName.length() == 0) {
 			String errorMsg = String.format("The login name must not be null or empty.");
 			this.logger.warn(errorMsg);
 			context.put("errorMsg",errorMsg);
 			return;
 		}
 		
 		// check if the login name is unique
 		Role[] roles = userAdmin.getRoles(String.format("(%s=%s)",HttpContextAuth.USER_HTTP_LOGIN, loginName));
 		if (roles != null && (roles.length > 2 || (roles.length == 1 && !roles[0].equals(user)))) {
 			String errorMsg = String.format("The given login name '%s' is already used by a different user.", loginName);
 			this.logger.warn(errorMsg);
 			context.put("errorMsg",errorMsg);
 			return;
 		}
 		
 		// check if the password is typed correctly
 		String pwd1 = request.getParameter(HttpContextAuth.USER_HTTP_PASSWORD);
 		String pwd2 = request.getParameter(HttpContextAuth.USER_HTTP_PASSWORD + "2");
 		if (pwd1 == null || pwd2 == null || !pwd1.equals(pwd2)) {
 			String errorMsg = String.format("The password for login name '%s' was not typed correctly.", loginName);
 			this.logger.warn(errorMsg);
 			context.put("errorMsg",errorMsg);
 			return;
 		}
 		
 		// configure http-login data
 		Dictionary<String, Object> props = user.getProperties();
 		props.put(HttpContextAuth.USER_HTTP_LOGIN, loginName);
 		
 		Dictionary<String, Object> credentials = user.getCredentials();
		credentials.put(HttpContextAuth.USER_HTTP_PASSWORD, pwd1);
 		
 		/* ===========================================================
 		 * OPEN-ID
 		 * =========================================================== */
 		String openIdURL = request.getParameter("openid.url");
 		if (openIdURL != null && openIdURL.length() > 0) {
 			// check if URL is unique
 			roles = userAdmin.getRoles(String.format("(openid.url=%s)", openIdURL));
 			if (roles != null && (roles.length > 2 || (roles.length == 1 && !roles[0].equals(user)))) {
 				String errorMsg = String.format("The given OpenID URL '%s' is already used by a different user.", openIdURL);
 				this.logger.warn(errorMsg);
 				context.put("errorMsg",errorMsg);
 				return;
 			}
 			
 			// configure the OpenID URL
 			props = user.getProperties();
 			props.put("openid.url", openIdURL);			
 		} else {
 			// delete old URL
 			user.getProperties().remove("openid.url");
 		}
 		
 		/* ===========================================================
 		 * MEMBERSHIP
 		 * =========================================================== */
 		// process membership
 		Authorization auth = userAdmin.getAuthorization(user);		
 		String[] currentMembership = auth.getRoles();
 		if (currentMembership == null) currentMembership = new String[0];
 		
 		String[] newMembership = request.getParameterValues("membership");
 		if (newMembership == null) newMembership = new String[0];
 		
 		// new memberships
 		for (String groupName : newMembership) {
 			if (!auth.hasRole(groupName)) {
 				Role role = userAdmin.getRole(groupName);
 				if (role != null && role.getType() == Role.GROUP) {
 					((Group)role).addMember(user);
 				}
 			}
 		}
 
 		// memberships to remove
 		ArrayList<String> oldMemberships = new ArrayList<String>(Arrays.asList(currentMembership));
 		oldMemberships.removeAll(Arrays.asList(newMembership));
 		for (String roleName : oldMemberships) {
 			if (auth.hasRole(roleName)) {
 				Role role = userAdmin.getRole(roleName);
 				if (role != null && role.getType() == Role.GROUP) {
 					((Group)role).removeMember(user);
 				}
 			}
 		}
 	}
 	
 	public Group[] getParentGroups(UserAdmin userAdmin, User user) throws InvalidSyntaxException {
 		ArrayList<Group> groups = new ArrayList<Group>();		
 		
 		if (user != null) {
 			Authorization auth = userAdmin.getAuthorization(user);
 			if (auth != null) {
 				String[] currentRoles = auth.getRoles();
 				if (currentRoles != null) {
 					for (String roleName : currentRoles) {
 						Role role = userAdmin.getRole(roleName);
 						if (role != null && role.getType() == Role.GROUP) {
 							groups.add((Group) role);
 						}
 					}
 				}
 			}
 		}
 		
 		return groups.toArray(new Group[groups.size()]);
 	}
 	
 	private Map<String, Dictionary<String, Object>> importConfig(HttpServletRequest request, Context context) throws Exception {
 
 		// Create a factory for disk-based file items
 		FileItemFactory factory = new DiskFileItemFactory();
 
 		// Create a new file upload handler
 		ServletFileUpload upload = new ServletFileUpload(factory);
 
 		// Parse the request
 		List<FileItem> items = upload.parseRequest(request);
 
 		// Process the uploaded items
 		Iterator<FileItem> iter = items.iterator();
 		while (iter.hasNext()) {
 			FileItem item = iter.next();
 
 			if (!item.isFormField()) {
 				if (!item.getFieldName().equals("configFile")) {
 					String errorMsg = String.format("Unknown file-upload field '%s'.",item.getFieldName());
 					this.logger.warn(errorMsg);
 					context.put("errorMsg",errorMsg);
 					continue;
 				}
 
 				String fileName = item.getName();
 				if (fileName != null) {
 					fileName = FilenameUtils.getName(fileName);
 				} else {
 					String errorMsg = String.format("Fileupload field '%s' has no valid filename.", item.getFieldName());
 					this.logger.warn(errorMsg);
 					context.put("errorMsg",errorMsg);
 					continue;
 				}
 
 				// write the bundle to disk
 				File targetFile = File.createTempFile(fileName, ".tmp");      		    
 				item.write(targetFile);
 
 				// cleanup
 				item.delete();
 				
 				// read Settings
 				ServiceManager manager = (ServiceManager) context.get("manager");
 				IConfigurationIEPorter importer = (IConfigurationIEPorter) manager.getService(IConfigurationIEPorter.class.getName());
 				Map<String, Dictionary<String, Object>> propsMap = importer.importConfigurations(targetFile);
 				return propsMap;
 			}
 		}
 		
 		return null;
 	}
 	
 	private void installStyle(IStyleManager styleManager, HttpServletRequest request, Context context) throws Exception {
 
 		// Create a factory for disk-based file items
 		FileItemFactory factory = new DiskFileItemFactory();
 
 		// Create a new file upload handler
 		ServletFileUpload upload = new ServletFileUpload(factory);
 
 		// Parse the request
 		List<FileItem> items = upload.parseRequest(request);
 
 		// Process the uploaded items
 		Iterator<FileItem> iter = items.iterator();
 		while (iter.hasNext()) {
 			FileItem item = iter.next();
 
 			if (!item.isFormField()) {
 				if (!item.getFieldName().equals("styleJar")) {
 					String errorMsg = String.format("Unknown file-upload field '%s'.",item.getFieldName());
 					this.logger.warn(errorMsg);
 					context.put("errorMsg",errorMsg);
 					continue;
 				}
 
 				String fileName = item.getName();
 				if (fileName != null) {
 					fileName = FilenameUtils.getName(fileName);
 				} else {
 					String errorMsg = String.format("Fileupload field '%s' has no valid filename.", item.getFieldName());
 					this.logger.warn(errorMsg);
 					context.put("errorMsg",errorMsg);
 					continue;
 				}
 
 				// write the bundle to disk
 				File targetFile = new File(styleManager.getDataPath(),fileName);
 				if (targetFile.exists()) {
 					String errorMsg = String.format("Targetfile '%s' already exists.",targetFile.getCanonicalFile().toString());
 					this.logger.warn(errorMsg);
 					context.put("errorMsg",errorMsg);
 					continue;
 				}            		    
 				item.write(targetFile);
 
 				// cleanup
 				item.delete();
 			}
 		}	
 	}
 	
 	public void writeImage(HttpServletRequest request, HttpServletResponse response, Context context) throws Exception {	
 		String pid = request.getParameter("pid");
 		if (pid == null) {
 			response.sendError(501, "No pid supplied.");
 			return;
 		}
 		
 		String bundleID = request.getParameter("bundleID");
 		if (bundleID == null) {
 			response.sendError(501, "No bundle-ID supplied.");
 			return;
 		}
 		
 		ServiceManager manager = (ServiceManager) context.get(SERVICE_MANAGER);
 		if (manager == null) {
 			response.sendError(501, "No ServiceManager found.");
 			return;
 		}
 		
 		// getting the bundle the managed service belongs to
 		Bundle bundle = manager.getBundle(Long.parseLong(bundleID));
 		if (bundle == null) {
 			response.sendError(501, String.format("No bundle with ID '%s' found.",bundleID));
 			return;
 		}
 		
 		// loading metadata
 		ObjectClassDefinition ocd = this.getObjectClassDefinition(request, manager, bundle, pid);
 		if (ocd == null) {
 			response.sendError(501, String.format("No ObjectClassDefinition found for service with PID '%s'.",pid));
 			return;
 		}		
 		
 		try {
 			// trying to find a proper icon
 			int[] sizes = new int[] {16,32,64,128,256};
 
 			for (int size : sizes) {
 				InputStream in = ocd.getIcon(16);
 				if (in != null) {
 					BufferedImage img = ImageIO.read(in);
 					response.setHeader("Content-Type","image/png");
 					ImageIO.write(img, "png", response.getOutputStream());
 					return;
 				} 
 			}
 
 			// no icon found. loading a default icon now
 			BufferedImage img = ImageIO.read(this.getClass().getResourceAsStream("/resources/images/cog.png"));
 			response.setHeader("Content-Type","image/png");
 			ImageIO.write(img, "png", response.getOutputStream());
 			return;
 
 		} catch (Throwable e) {			
 			response.sendError(404, e.getMessage());
 			return;
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	public String getPreferedLocale(List<Locale> preferedLocales, String[] supportedLocalesArray) {
 		String localeToUse = Locale.ENGLISH.getLanguage();
 		
 		// getting languages provided by the available metatypes
 		HashSet<String> supportedLocale = new HashSet<String>(Arrays.asList(supportedLocalesArray==null?new String[0]:supportedLocalesArray));
 		
 		// find best match
 		for (Locale preferedLocale : preferedLocales) {
 			if (supportedLocale.contains(preferedLocale.toString())) {
 				localeToUse = preferedLocale.toString();
 				break;
 			}
 		}
 		
 		return localeToUse;
 	}
 		
 	
 	public void setPropertyValues(HttpServletRequest request, HttpServletResponse response, Context context, final boolean reset) throws Exception {		
 		Dictionary<String, Object> props = new Hashtable<String, Object>();		
 
 		String pid = request.getParameter("pid");
 		if (pid == null) {
 			context.put(ERROR_MSG, "No pid supplied.");
 			return;
 		}
 		
 		String bundleID = request.getParameter("bundleID");
 		if (bundleID == null) {
 			context.put(ERROR_MSG, "No bundle-ID supplied.");
 			return;
 		}
 		
 		ServiceManager manager = (ServiceManager) context.get(SERVICE_MANAGER);
 		if (manager == null) {
 			context.put(ERROR_MSG, "No ServiceManager found.");
 			return;
 		}
 		
 		// getting the bundle the managed service belongs to
 		Bundle bundle = manager.getBundle(Long.parseLong(bundleID));
 		if (bundle == null) {
 			context.put(ERROR_MSG, String.format("No bundle with ID '%s' found.",bundleID));
 			return;
 		}
 
 		ConfigurationAdmin configAdmin = (ConfigurationAdmin) manager.getService(ConfigurationAdmin.class.getName());
 		if (configAdmin == null) {
 			context.put(ERROR_MSG, "ConfigurationAdmin service not found.");
 			return;
 		}
 		
 		Configuration config = configAdmin.getConfiguration(pid, bundle.getLocation());
 		if (config == null) {
 			context.put(ERROR_MSG, "Configuration object not found.");
 			return;
 		}
 		
 		ObjectClassDefinition ocd = this.getObjectClassDefinition(request, manager, bundle, pid);
 		if (ocd == null) {
 			context.put(ERROR_MSG, String.format("No ObjectClassDefinition found for service with PID '%s'.",pid));
 			return;
 		}
 		
 		AttributeDefinition[] attributes = ocd.getAttributeDefinitions(ObjectClassDefinition.ALL);
 		if (attributes == null) {
 			context.put(ERROR_MSG, String.format("No AttributeDefinitions found for service with PID '%s' and locale '%s'.",pid));
 			return;
 		}
 		
 		for(AttributeDefinition attribute : attributes) {
 			String[] attributeDefaults = attribute.getDefaultValue();
 			
 			// getting configured values
 			String attributeID = attribute.getID();	
 			String[] attributeValueStrings = request.getParameterValues(attributeID);
 			
 			// if no values were found use defaults for the initial configuration
 			/*
 			if (attributeValueStrings == null && attributeDefaults != null && attributeDefaults.length > 0) {
 				attributeValueStrings = attributeDefaults;
 			}	
 			*/
 			
 			final Object finalAttributeValues = convertAttributeValues(
 					attribute,
 					(reset) ? attributeDefaults : attributeValueStrings,
 					context);
 			
 			if (finalAttributeValues != null) {
 				props.put(attributeID, finalAttributeValues);
 			}				
 		}
 		
 		// update configuration		
 		config.update(props);			
 	}
 	
 	private static Object convertAttributeValues(
 			final AttributeDefinition attribute,
 			final String[] attributeValueStrings,
 			final Context context) {
 		
 		// getting metadata
 		String attributeID = attribute.getID();	
 		int attributeType = attribute.getType();
 		int cardinality = attribute.getCardinality();		
 		
 		// init result structure
 		Object finalAttributeValues = null;
 		if (cardinality != 0) {				
 			if (cardinality >= 1) {
 				int valueArraySize = Math.min(cardinality, (attributeValueStrings == null) ? 0 : attributeValueStrings.length);
 				
 				// the attribute-value-list must be an array of ....
 				switch (attributeType) {
 					case AttributeDefinition.BOOLEAN:
 						finalAttributeValues = new boolean[valueArraySize];
 						break;
 					case AttributeDefinition.BYTE:
 						finalAttributeValues = new byte[valueArraySize];
 						break; 
 					case AttributeDefinition.CHARACTER:
 						finalAttributeValues = new char[valueArraySize];
 						break; 
 					case AttributeDefinition.DOUBLE:
 						finalAttributeValues = new double[valueArraySize];
 						break; 
 					case AttributeDefinition.FLOAT:
 						finalAttributeValues = new float[valueArraySize];
 						break; 
 					case AttributeDefinition.INTEGER:
 						finalAttributeValues = new int[valueArraySize];
 						break; 
 					case AttributeDefinition.LONG:
 						finalAttributeValues = new long[valueArraySize];
 						break; 
 					case AttributeDefinition.SHORT:
 						finalAttributeValues = new short[valueArraySize];
 						break; 
 					case AttributeDefinition.STRING:
 						finalAttributeValues = new String[valueArraySize];
 						break; 
 					default:
 						break;
 				}
 			} else {
 				// the attribute-value list must be a vector
 				finalAttributeValues = new Vector<Object>();
 			}
 		}			
 		
 		if(attributeValueStrings != null) {								
 			try {
 				int counter = 0;
 				for (String attributeValueString : attributeValueStrings) {
 					if (cardinality != 0 && (counter + 1 > Math.abs(cardinality))) {
 						context.put(ERROR_MSG, String.format("Too many values found for parameter '%s': %d",attributeID,counter+1));
 						return null;
 					}
 
 					// validate value
 					String validationProblem = attribute.validate(attributeValueString);
 					if (validationProblem != null && validationProblem.length() > 0) {
 						context.put(ERROR_MSG, String.format("Parameter '%s' has a wrong value: %s",attributeID,validationProblem));
 						return null;
 					}
 
 					// convert value
 					Object finalAttributeValue = null;
 					switch (attributeType) {
 						case AttributeDefinition.BOOLEAN:
 							finalAttributeValue = Boolean.valueOf(attributeValueString);
 							if (cardinality >= 1) ((boolean[])finalAttributeValues)[counter] = ((Boolean)finalAttributeValue).booleanValue();
 							else if (cardinality <= -1) ((Vector<Boolean>)finalAttributeValues).add((Boolean)finalAttributeValue);
 							break;
 						case AttributeDefinition.BYTE:
 							finalAttributeValue = Byte.valueOf(attributeValueString);
 							if (cardinality >= 1) ((byte[])finalAttributeValues)[counter] = ((Byte)finalAttributeValue).byteValue();
 							else if (cardinality <= -1) ((Vector<Byte>)finalAttributeValues).add((Byte)finalAttributeValue);								
 							break; 
 						case AttributeDefinition.CHARACTER:
 							//						attributeValue = Character.
 							break; 
 						case AttributeDefinition.DOUBLE:
 							finalAttributeValue = Double.valueOf(attributeValueString);
 							if (cardinality >= 1) ((double[])finalAttributeValues)[counter] = ((Double)finalAttributeValue).doubleValue();
 							else if (cardinality <= -1) ((Vector<Double>)finalAttributeValues).add((Double)finalAttributeValue);							
 							break; 
 						case AttributeDefinition.FLOAT:
 							finalAttributeValue = Float.valueOf(attributeValueString);
 							if (cardinality >= 1) ((float[])finalAttributeValues)[counter] = ((Float)finalAttributeValue).floatValue();
 							else if (cardinality <= -1) ((Vector<Float>)finalAttributeValues).add((Float)finalAttributeValue);								
 							break; 
 						case AttributeDefinition.INTEGER:
 							finalAttributeValue = Integer.valueOf(attributeValueString);
 							if (cardinality >= 1) ((int[])finalAttributeValues)[counter] = ((Integer)finalAttributeValue).intValue();
 							else if (cardinality <= -1) ((Vector<Integer>)finalAttributeValues).add((Integer)finalAttributeValue);								
 							break; 
 						case AttributeDefinition.LONG:
 							finalAttributeValue = Long.valueOf(attributeValueString);
 							if (cardinality >= 1) ((long[])finalAttributeValues)[counter] = ((Long)finalAttributeValue).longValue();
 							else if (cardinality <= -1) ((Vector<Long>)finalAttributeValues).add((Long)finalAttributeValue);								
 							break; 
 						case AttributeDefinition.SHORT:
 							finalAttributeValue = Short.valueOf(attributeValueString);
 							if (cardinality >= 1) ((short[])finalAttributeValues)[counter] = ((Short)finalAttributeValue).shortValue();
 							else if (cardinality <= -1) ((Vector<Short>)finalAttributeValues).add((Short)finalAttributeValue);								
 							break; 
 						case AttributeDefinition.STRING:
 							finalAttributeValue = attributeValueString;
 							if (cardinality >= 1) ((String[])finalAttributeValues)[counter] = (String) attributeValueString;
 							else if (cardinality <= -1) ((Vector<String>)finalAttributeValues).add((String)finalAttributeValue);								
 							break; 
 						default:
 							break;
 					}
 					
 					if (cardinality == 0) {
 						finalAttributeValues = finalAttributeValue;
 					} 
 					counter++;
 				}
 			} catch (NumberFormatException e) {
 				context.put(ERROR_MSG, String.format("The supplied parameter has a wrong format: %s",e.getMessage()));
 				return null;
 			}
 		}
 		
 		return finalAttributeValues;
 	}
 	
 	public Object getPropertyValue(@SuppressWarnings("unchecked") Dictionary props, AttributeDefinition attribute) {
 		if (attribute == null) throw new NullPointerException("Attribute definition is null");
 		
 		String propertyKey = attribute.getID();
 		Object value = (props == null)?null:props.get(propertyKey);
 		String[] defaultValues = attribute.getDefaultValue();
 		
 		if (value != null) {
 			return value;
 		} else if (defaultValues != null && defaultValues.length > 0){
 			return (attribute.getCardinality()==0)?defaultValues[0]:defaultValues;
 		} else {
 			return null;
 		}
 	}
 	
 	private Set<String> getMetaTypeServicePIDs(ServiceManager manager, Bundle bundle) {		
 		MetaTypeService metaTypeService = (MetaTypeService) manager.getService(MetaTypeService.class.getName());
 		if (metaTypeService == null) throw new NullPointerException("Unable to find the metatype service");	
 		
 		MetaTypeInformation mti = metaTypeService.getMetaTypeInformation(bundle);
 		String[] pidArray = (mti==null)?null:mti.getPids();
 		if (pidArray != null) {
 			return new HashSet<String>(Arrays.asList(pidArray));
 		} else {
 			return Collections.emptySet();
 		}
 	}
 	
 	private Set<String> getMetaTypeProviderPIDs(Bundle bundle) {
 		HashSet<String> metaTypePIDs = new HashSet<String>();
 		
 		ServiceReference[] serviceRefs = bundle.getRegisteredServices();
 		if (serviceRefs != null) {
 			for (ServiceReference ref : serviceRefs) {
 				// a managed-service always has a PID
 				String pid = (String) ref.getProperty(Constants.SERVICE_PID);
 				if (pid!= null) {			
 					HashSet<String> objectClassSet = new HashSet<String>(Arrays.asList((String[])ref.getProperty(Constants.OBJECTCLASS)));
 					if (objectClassSet.contains(MetaTypeProvider.class.getName()) && !metaTypePIDs.contains(pid)) {
 						metaTypePIDs.add(pid);
 					}
 				}
 			}
 		}
 		
 		return metaTypePIDs;
 	}
 	
 	public String[] getMetaTypePIDs(ServiceManager manager, Bundle bundle) {
 		HashSet<String> metaTypePIDs = new HashSet<String>();
 
 		// Loopup the PIDs of all metatypes provided via the metatype-service
 		metaTypePIDs.addAll(this.getMetaTypeServicePIDs(manager, bundle));		
 		
 		// search for additional metatype-providers
 		metaTypePIDs.addAll(this.getMetaTypeProviderPIDs(bundle));
 		
 		return metaTypePIDs.toArray(new String[metaTypePIDs.size()]);
 	}	
 	
 	private ObjectClassDefinition getObjectClassDefinitionFromMetaTypeService(ServiceManager manager, Bundle bundle, String PID, List<Locale> locales) {
 		MetaTypeService metaTypeService = (MetaTypeService) manager.getService(MetaTypeService.class.getName());
 		if (metaTypeService == null) throw new NullPointerException("Unable to find the metatype service");	
 		
 		if (this.getMetaTypeServicePIDs(manager, bundle).contains(PID)) { 
 			MetaTypeInformation mti = metaTypeService.getMetaTypeInformation(bundle);			
 			String locale = this.getPreferedLocale(locales, mti.getLocales());			
 			return mti.getObjectClassDefinition(PID, locale);
 		} else {
 			return null;
 		}
 	}
 	
 	private ObjectClassDefinition getObjectClassDefinitionFromMetaTypeProvider(ServiceManager manager, Bundle bundle, String PID, List<Locale> locales) {
 		ServiceReference[] serviceRefs = bundle.getRegisteredServices();
 		if (serviceRefs != null) {
 			for (ServiceReference ref : serviceRefs) {
 				// a managed-service always has a PID
 				String nextPID = (String) ref.getProperty(Constants.SERVICE_PID);
 				if (nextPID!= null && nextPID.equals(PID)) {
 					MetaTypeProvider mProvider = (MetaTypeProvider) manager.getService(ref);
 					String locale = this.getPreferedLocale(locales, mProvider.getLocales());
 					return mProvider.getObjectClassDefinition(PID, locale);
 				}
 			}
 		}
 		return null;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public ObjectClassDefinition getObjectClassDefinition(HttpServletRequest request, ServiceManager manager, Bundle bundle, String PID) {
 		final List<Locale> preferedLocale = Collections.list(request.getLocales());
 		
 		// try to find ocd via metatype-service
 		ObjectClassDefinition ocd = this.getObjectClassDefinitionFromMetaTypeService(manager, bundle, PID, preferedLocale);
 		if (ocd != null) return ocd;
 		
 		// try to find ocd via metatype-provider 
 		return this.getObjectClassDefinitionFromMetaTypeProvider(manager, bundle, PID, preferedLocale);
 	}
 }
