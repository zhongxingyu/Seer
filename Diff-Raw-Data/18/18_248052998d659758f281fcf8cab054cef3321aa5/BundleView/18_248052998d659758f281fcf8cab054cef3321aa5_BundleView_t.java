 package org.paxle.gui.impl.servlets;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileItemFactory;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.io.FilenameUtils;
 import org.apache.velocity.Template;
 import org.apache.velocity.context.Context;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleException;
 import org.paxle.gui.ALayoutServlet;
 import org.paxle.gui.impl.ServiceManager;
 
 public class BundleView extends ALayoutServlet {
 
 	private static final String PARAM_BUNDLE_PATH = "bundlePath";
 	private static final String PARAM_INSTALL_URL = "installURL";
 	private static final String PARAM_BUNDLE_ID = "bundleID";
 	private static final String PARAM_ACTION = "action";
 	
 	private static final String ACTION_DETAILS = "details";
 	private static final String ACTION_UNINSTALL = "uninstall";
 	private static final String ACTION_RESTART = "restart";
 	private static final String ACTION_STOP = "stop";
 	private static final String ACTION_START = "start";
 	private static final String ACTION_UPDATE = "update";
 
 	private static final long serialVersionUID = 1L;
 	
     private static final Map<Integer,String> states = new HashMap<Integer,String>();
     static {
     	states.put(Integer.valueOf(Bundle.ACTIVE), "active");
     	states.put(Integer.valueOf(Bundle.INSTALLED), "installed");
     	states.put(Integer.valueOf(Bundle.RESOLVED), "resolved");
     	states.put(Integer.valueOf(Bundle.STARTING), "starting");
     	states.put(Integer.valueOf(Bundle.STOPPING), "stopping");
     	states.put(Integer.valueOf(Bundle.UNINSTALLED), "uninstalled");
     }
 	
     @Override
     public Template handleRequest( HttpServletRequest request,
             HttpServletResponse response,
             Context context ) {
     	
         Template template = null;
         try {
             template = this.getTemplate("/resources/templates/BundleView.vm");
             
             ServiceManager manager = (ServiceManager) context.get(SERVICE_MANAGER);
             if (request.getParameter(PARAM_BUNDLE_ID) != null) {
             	long bundleID = Long.parseLong(request.getParameter(PARAM_BUNDLE_ID));
             	Bundle bundle = manager.getBundle(bundleID);
         		if (bundle == null) {
         			String errorMsg = String.format(
         					"Bundle with ID '%s' not found.", 
         					request.getParameter(PARAM_BUNDLE_ID)
         			);
         			this.logger.warn(errorMsg);
         			context.put("errorMsg",errorMsg);
         		} else if (request.getParameter(PARAM_ACTION) != null) {
        			boolean doRedirect = false;
         			String action = request.getParameter(PARAM_ACTION);
         			try {
         				if (action.equals( ACTION_UPDATE)) {
         					bundle.update();
        					doRedirect = true;
         				} else if (action.equals( ACTION_START)) {
         					bundle.start();
        					doRedirect = true;
         				} else if (action.equals( ACTION_STOP)) {
         					bundle.stop();
        					doRedirect = true;
         				} else if (action.equals( ACTION_RESTART)) {
         					bundle.stop();
         					bundle.start();
        					doRedirect = true;
         				}else if(action.equals( ACTION_UNINSTALL)){
         					bundle.uninstall();
        					doRedirect = true;
         				}else if (action.equals(ACTION_DETAILS)) {
         					context.put("bundle", bundle);
         				}
         			} catch (BundleException e) {
             			String errorMsg = String.format(
             					"Unexpected exception while doing action '%s' on bundle with ID '%s", 
             					action,
             					request.getParameter(PARAM_BUNDLE_ID)
             			);
             			this.logger.warn(errorMsg, e);
             			context.put("errorMsg",e.getMessage());
         			}
        			
        			// redirect to the bundle view
        			if (doRedirect) {
        				response.sendRedirect(request.getRequestURI());
        				return null;
        			}
         		}
             }else if(request.getParameter(PARAM_INSTALL_URL) != null && request.getParameter(PARAM_BUNDLE_PATH)!=null){
             	ServiceManager.context.installBundle(request.getParameter(PARAM_BUNDLE_PATH));
             } else if (ServletFileUpload.isMultipartContent(request)) {
 
             	// Create a factory for disk-based file items
             	FileItemFactory factory = new DiskFileItemFactory();
 
 //          	// Set factory constraints
 //          	factory.setSizeThreshold(yourMaxMemorySize);
 //          	factory.setRepository(yourTempDirectory);
 
             	// Create a new file upload handler
             	ServletFileUpload upload = new ServletFileUpload(factory);
 
 //          	// Set overall request size constraint
 //          	upload.setSizeMax(yourMaxRequestSize);
 
             	// Parse the request
             	List<FileItem> items = upload.parseRequest(request);
 
             	// Process the uploaded items
             	Iterator<FileItem> iter = items.iterator();
             	while (iter.hasNext()) {
             		FileItem item = iter.next();
 
             		if (!item.isFormField()) {
             			if (!item.getFieldName().equals("bundleJar")) {
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
             		    File targetFile = new File(fileName);
             		    if (targetFile.exists()) {
             		    	String errorMsg = String.format("Targetfile '%s' already exists.",targetFile.getCanonicalFile().toString());
             		    	this.logger.warn(errorMsg);
             		    	context.put("errorMsg",errorMsg);
             		    	continue;
             		    }            		    
             		    item.write(targetFile);
             		    
             		    // installing bundle
             			ServiceManager.context.installBundle(targetFile.toURI().toURL().toString());
 
             			// cleanup
             			item.delete();
             		}
             	}
 
             }
             
             
             context.put("bundles", bundles2map(manager.getBundles()));
             context.put("states", states);
         } catch (Throwable e) {
             System.err.println("Exception caught: " + e.getMessage());
             e.printStackTrace();
         }
         
         return template;
     }
     
     private static TreeMap<Long,Bundle> bundles2map(Bundle[] bundles) {
     	final TreeMap<Long,Bundle> r = new TreeMap<Long,Bundle>();
     	for (Bundle bundle : bundles)
     		if (bundle.getBundleId() > 0)
     			r.put(Long.valueOf(bundle.getBundleId()), bundle);
     	return r;
     }
 }
