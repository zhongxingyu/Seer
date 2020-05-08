 package edu.gatech.oad.rocket.findmythings.server;
 
 import edu.gatech.oad.rocket.findmythings.server.model.AppMember;
 import edu.gatech.oad.rocket.findmythings.server.model.MessageBean;
 import edu.gatech.oad.rocket.findmythings.server.security.ProfileRealm;
 import edu.gatech.oad.rocket.findmythings.server.util.HTTP;
 import edu.gatech.oad.rocket.findmythings.server.util.HTTP.Status;
 import edu.gatech.oad.rocket.findmythings.server.util.MimeTypes;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

 import org.apache.shiro.SecurityUtils;
 import org.apache.shiro.mgt.RealmSecurityManager;
 import org.apache.shiro.realm.Realm;
 import org.apache.shiro.subject.PrincipalCollection;
 
import com.google.common.base.Charsets;

 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 import java.util.logging.Logger;
 
 public abstract class TemplateServlet extends HttpServlet {
 
 	private static final long serialVersionUID = 8526927539799303725L;
 
 	@SuppressWarnings("unused")
 	private static final Logger LOGGER = Logger.getLogger(TemplateServlet.class.getName());
 	
	private final PageGenerator generator;
 
	public TemplateServlet() {
		super();
 		try {
 			generator = new PageGenerator(getServletContext().getResource("/WEB-INF/templates/"), Locale.getDefault(), Charsets.UTF_8);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	private static int indexOfExtension(String filename) {
 		if (filename == null) {
 			return -1;
 		}
 		int extensionPos = filename.lastIndexOf(".");
 		int lastUnixPos = filename.lastIndexOf('/');
 		int lastWindowsPos = filename.lastIndexOf('\\');
 		return (Math.max(lastUnixPos, lastWindowsPos) > extensionPos ? -1 : extensionPos);
 	}
 
 	private static String removeExtension(String filename) {
 		if (filename == null) {
 			return null;
 		}
 		int index = indexOfExtension(filename);
 		if (index == -1) {
 			return filename;
 		} else {
 			return filename.substring(0, index);
 		}
 	}
 
 	private static String replaceExtensionWith(String filename, String extension) {
 		return removeExtension(filename) + "." + extension;
 	}
 
 	protected static String getDefaultTemplateURI(HttpServletRequest request) {
 		String URI = request.getRequestURI();
 		if (URI.endsWith("/")) URI = URI.substring(0, URI.length() - 1);
 		if (URI.length() == 0) URI = "index.html";
 		return replaceExtensionWith(URI, "ftl");
 	}
 
 	protected String createDocument(String templateName, Map<String,Object> userArgs) throws IOException {
 		return generator.createPage(templateName, userArgs);
 	}
 
 	protected String createDocument(String templateName, Object... args) throws IOException {
 		return createDocument(templateName, PageGenerator.map(args));
 	}
 
 	protected void writeDocument(HttpServletResponse response, String templateName, Map<String,Object> args) throws IOException {
 		HTTP.write(response, MimeTypes.HTML, Status.OK, createDocument(templateName, args));
 	}
 
 	protected void writeDocument(HttpServletResponse response, String templateName, Object... args) throws IOException {
 		writeDocument(response, templateName, PageGenerator.map(args));
 	}
 
 	String getCurrentUserEmail() {
 		PrincipalCollection principals = SecurityUtils.getSubject().getPrincipals();
 		if (principals == null || principals.isEmpty()) return null;
 		return (String)principals.getPrimaryPrincipal();
 	}
 
 	protected boolean memberExistsWithEmail(String email) {
 		if (email == null || email.length() == 0) return false;
 		RealmSecurityManager manager = (RealmSecurityManager)SecurityUtils.getSecurityManager();
 		for (Realm realm : manager.getRealms()) {
 			if (realm instanceof ProfileRealm) {
 				if (((ProfileRealm) realm).accountExists(email)) return true;
 			}
 		}
 		return false;
 	}
 
 	protected AppMember memberWithEmail(String email) {
 		if (email == null || email.length() == 0) return null;
 		RealmSecurityManager manager = (RealmSecurityManager)SecurityUtils.getSecurityManager();
 		for (Realm realm : manager.getRealms()) {
 			if (realm instanceof ProfileRealm) {
 				AppMember potential = ((ProfileRealm) realm).getAccount(email);
 				if (potential != null && potential.getEmail().equals(email)) return potential;
 			}
 		}
 
 		return null;
 	}
 
 	protected final Map<String,Object> getParameterMap(HttpServletRequest request) {
 		Map<String,Object> map = new HashMap<>();
 		addParametersToMap(request, map);
 		return map;
 	}
 
 	protected void addParametersToMap(HttpServletRequest request, Map<String, Object> params) {
 		Object failureReason = request.getAttribute(MessageBean.FAILURE_REASON);
 		if (failureReason != null) params.put(MessageBean.FAILURE_REASON, failureReason);
 	}
 
 }
