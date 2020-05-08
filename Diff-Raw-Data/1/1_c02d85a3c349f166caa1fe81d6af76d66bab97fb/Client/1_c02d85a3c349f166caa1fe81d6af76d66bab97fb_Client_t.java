 package org.opensuse.android.obs;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.EnumSet;
 import java.util.List;
 
 import org.opensuse.android.HttpCoreConnection;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import android.text.TextUtils;
 import android.util.Log;
 
 import zipwire.rest.ApacheRestConnection;
 import zipwire.text.CamelCase;
 import zipwire.xml.XmlNamingRule;
 
 public class Client extends HttpCoreConnection {
 
 	public static final String PREFERENCES = "preferences";
 	
 	private static final String DEFAULT_API_HOST = "api.opensuse.org";
 	private static final String DEFAULT_USERNAME = "";
 	private static final String DEFAULT_PASSWORD = "";
 	
 	/* Standard Rails-like naming rule */
 	static XmlNamingRule xmlNamingRule = new XmlNamingRule(){
 		public String transform(String method) {
 	      return new CamelCase(method).separateWith("-").toLowerCase();
 	    }
 	  };
 	
 	/* 
 	 * Constructor from context, which allows the client to access the preferences of
 	 * the application
 	 */
 	public Client(Context context) {
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 		// then you use
 		String username = prefs.getString("username", DEFAULT_USERNAME);
 		String password = prefs.getString("password", DEFAULT_PASSWORD);
 		setProtocol("https://");
 		setUsername(username);
 		setPassword(password);
 		setHost(DEFAULT_API_HOST);
 		setXmlNamingRule(xmlNamingRule);
 	}
 	
 	public List<Project> getProjects() {
 		return getProjectsMatching("");
 	}
 	
 	public List<ProjectId> getProjectIds() {
 		return getProjectIdsMatching("");
 	}
 	
 	public List<ProjectId> getProjectIdsMatching(String match) {
 		String path = "search/project_id" + "?match=" + encodeMatch(match);
 		Log.i("OBSCLIENT", "Retrieving: " + path);
 		return get(path)
 			.asList(ProjectId.class, "project");
 	}
 	
 	public List<Project> getProjectsMatching(String match) {
 		String path = "search/project" + "?match=" + encodeMatch(match);
 		Log.i("OBSCLIENT", "Retrieving: " + path);
 		return get(path)
 			.asList(Project.class, "project");
 	}
 	
 	public List<Package> getPackagesMatching(String match) {
 		String path = "search/package" + "?match=" + encodeMatch(match);
 		Log.i("OBSCLIENT", "Retrieving: " + path);
 		return get(path)
 			.asList(Package.class, "package");
 	}
 	
 	public List<PackageId> getPackageIdsMatching(String match) {
 		String path = "search/package_id" + "?match=" + encodeMatch(match);
 		Log.i("OBSCLIENT", "Retrieving: " + path);
 		return get(path)
 			.asList(PackageId.class, "package");
 	}
 	
 	public List<Distribution> getDistributions() {
 		return get("distributions").asList(Distribution.class, "distribution");
 	}
 	
 	public List<Request> getRequestsMatching(String match) {
 		String path = "search/request";
 		path = path + "?match=" + encodeMatch(match);
 		Log.i("OBSCLIENT", "Retrieving: " + path);
 		return get(path)
 			.asList(Request.class, "request");
 	}
 	
 	public List<Request> getRequests() {
 		return getRequestsMatching("");
 	}
 	
 	public List<Request> getMyRequests() {
 		List<String> xpaths = new ArrayList<String>();
 		List<String> projects = new ArrayList<String>();
 		String query = "(state/@name='new') and (";
 		List<ProjectId> projectIds = getProjectIdsMatching("person/@userid = '" + getUsername() + "' and person/@role = 'maintainer'");
 		for (ProjectId project: projectIds) {
			projects.add(project.getName());
 			xpaths.add("action/target/@project='" + project.getName() + "'");
 		}
 		
 		/*
 		List<PackageId> packages = getPackageIdsMatching("person/@userid = '" + getUsername() + "' and person/@role = 'maintainer'");
 		for (PackageId pkg: packages) {
 			// if the project is already in the list, no need to add this clause
 			// replace this with a real xPath join
 			if (projects.contains(pkg.getProject()))
 				continue;
 			xpaths.add("(action/target/@project='" + pkg.getProject() + "' and " + "action/target/@package='" + pkg.getName() + "')");
 		}
 		*/
 		Log.i("OBSCLIENT", String.valueOf(xpaths.size()));
 		
 		
 		query += TextUtils.join(" or ", xpaths);
 		query += ")";
 		return getRequestsMatching(query);
 	}
 	
 	/* Encode or return the same if not possible */
 	private String encodeMatch(String match) {
 		try {
 			return URLEncoder.encode(match, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			Log.w("URI", "Can't encode: " + match);
 			return match;
 		}
 	}
 }
