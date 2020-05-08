 package net.nan21.dnet.core.web.controller.ui.extjs;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.HttpResponseException;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.type.TypeReference;
 
 public class DependencyLoader {
 
 	private ObjectMapper jsonMapper = new ObjectMapper();
 	private HttpClient httpClient = new DefaultHttpClient();
 
 	private String urlUiExtjsModules;
 	private String urlUiExtjsModuleSubpath;
 	private String urlUiExtjsModulesI18n;
 
 	public void packFrameCmp(String bundle, String name, File file)
 			throws Exception {
 
 		List<String> list = new ArrayList<String>();
 		this.resolveFrameDependencies(bundle, name, null, list, null);
		Writer writer = new FileWriter(file);
 
 		try {
 			for (String dep : list) {
 				this.writeContentCmp(dep, writer);
 			}
 		} finally {
 			writer.close();
 		}
 	}
 
 	public void packFrameTrl(String bundle, String name, String language,
 			File file) throws Exception {
 
 		List<String> list = new ArrayList<String>();
 		this.resolveFrameDependencies(bundle, name, language, null, list);
		Writer writer = new FileWriter(file);
 
 		try {
 			for (String dep : list) {
 				this.writeContentTrl(dep, language, writer);
 			}
 		} finally {
 			writer.close();
 		}
 	}
 
 	public Dependencies resolveFrameDependencies(String frame) throws Exception {
 		Dependencies d = new Dependencies();
 		this.resolveAllDependencies(frame, d);
 		return d;
 	}
 
 	/**
 	 * Resolve all the dependencies used in this frame adding them to the
 	 * specified lists.One list for the components the other one for the
 	 * translations for the given language..
 	 * 
 	 * @param cmp
 	 * @return
 	 * @throws Exception
 	 */
 	public void resolveFrameDependencies(String bundle, String name,
 			String language, List<String> listCmp, List<String> listTrl)
 			throws Exception {
 
 		String cmp = bundle + "/frame/" + name;
 
 		Dependencies d = this.resolveFrameDependencies(cmp);
 
 		if (d.getDs() != null) {
 			for (String dep : d.getDs()) {
 				String[] tokens = dep.split("/");
 				String _bundle = tokens[0];
 				String _type = tokens[1];
 				String _name = tokens[2];
 
 				if (listCmp != null) {
 					listCmp.add(urlCmp(_bundle, _type, _name));
 				}
 
 				if (listTrl != null) {
 					listTrl.add(urlTrl(_bundle, _type, _name, language));
 				}
 
 			}
 		}
 
 		if (d.getLov() != null) {
 			for (String dep : d.getLov()) {
 				if (listCmp != null) {
 					listCmp.add(urlCmp(dep));
 				}
 			}
 		}
 
 		if (d.getAsgn() != null) {
 			for (String dep : d.getAsgn()) {
 				if (listCmp != null) {
 					listCmp.add(urlCmp(dep));
 				}
 			}
 		}
 
 		if (d.getDc() != null) {
 			for (String dep : d.getDc()) {
 				if (listCmp != null) {
 					listCmp.add(urlCmp(dep));
 				}
 			}
 		}
 
 		if (listCmp != null) {
 			listCmp.add(urlCmp(bundle, Dependencies.TYPE_UI, name));
 		}
 		if (listTrl != null) {
 			listTrl.add(urlTrl(bundle, Dependencies.TYPE_UI, name, language));
 		}
 
 	}
 
 	private void writeContentTrl(String cmp, String language, Writer writer)
 			throws Exception {
 		this.writeContentByUrl(cmp, writer);
 	}
 
 	private void writeContentCmp(String cmp, Writer writer) throws Exception {
 		this.writeContentByUrl(cmp, writer);
 	}
 
 	private void writeContentByUrl(String url, Writer writer) throws Exception {
 		HttpGet get = new HttpGet(url);
 		ResponseHandler<String> responseHandler = new BasicResponseHandler();
 		String responseBody = getHttpClient().execute(get, responseHandler);
 		writer.write(responseBody);
 		get.releaseConnection();
 	}
 
 	/**
 	 * Resolve the dependencies of the given component and returns them in a
 	 * list.
 	 * 
 	 * @param cmp
 	 * @return
 	 * @throws Exception
 	 */
 	private List<String> resolveCmpDependencies(String cmp) throws Exception {
 
 		HttpGet get = new HttpGet(this.urlDpd(cmp));
 		ResponseHandler<String> responseHandler = new BasicResponseHandler();
 
 		List<String> result = null;
 		try {
 
 			String responseBody = getHttpClient().execute(get, responseHandler);
 
 			result = getJsonMapper().readValue(responseBody,
 					new TypeReference<List<String>>() {
 					});
 
 		} catch (HttpResponseException e) {
 			if (e.getStatusCode() != 404) {
 				e.printStackTrace();
 			}
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		get.releaseConnection();
 		return result;
 	}
 
 	/**
 	 * Recursively resolve all the dependencies of the given component adding
 	 * the elements to the given stack.
 	 * 
 	 * @param cmp
 	 * @param stack
 	 * @throws Exception
 	 */
 	private void resolveAllDependencies(String cmp, Dependencies stack)
 			throws Exception {
 
 		List<String> deps = this.resolveCmpDependencies(cmp);
 		if (deps != null) {
 			for (String dep : deps) {
 
 				String _type = dep.split("/")[1];
 
 				if (_type.matches(Dependencies.TYPE_DS)) {
 					stack.addDs(dep);
 				}
 
 				if (_type.matches(Dependencies.TYPE_DC)) {
 					stack.addDc(dep);
 				}
 
 				if (_type.matches(Dependencies.TYPE_LOV)) {
 					stack.addLov(dep);
 				}
 
 				if (_type.matches(Dependencies.TYPE_ASGN)) {
 					stack.addAsgn(dep);
 				}
 				this.resolveAllDependencies(dep, stack);
 			}
 		}
 	}
 
 	// private helpers
 
 	/**
 	 * Return the URL of the dependencies file for the specified component.
 	 * 
 	 * @param bundle
 	 * @param type
 	 * @param name
 	 * @return
 	 */
 	private String urlDpd(String cmp) {
 		String[] tokens = cmp.split("/");
 		String bundle = tokens[0];
 		String type = tokens[1];
 		String name = tokens[2];
 
 		return this.urlDpd(bundle, type, name);
 	}
 
 	/**
 	 * Return the URL of the dependencies file for the specified component.
 	 * 
 	 * @param bundle
 	 * @param type
 	 * @param name
 	 * @return
 	 */
 	private String urlDpd(String bundle, String type, String name) {
 		return this.urlUiExtjsModules + "/" + bundle
 				+ this.urlUiExtjsModuleSubpath + "/" + type + "/" + name
 				+ ".jsdp";
 	}
 
 	/**
 	 * Return the URL for the specified component.
 	 * 
 	 * @param bundle
 	 * @param type
 	 * @param name
 	 * @return
 	 */
 	private String urlCmp(String cmp) {
 		String[] tokens = cmp.split("/");
 		String bundle = tokens[0];
 		String type = tokens[1];
 		String name = tokens[2];
 
 		return this.urlCmp(bundle, type, name);
 	}
 
 	/**
 	 * Return the URL for the specified component.
 	 * 
 	 * @param bundle
 	 * @param type
 	 * @param name
 	 * @return
 	 */
 	private String urlCmp(String bundle, String type, String name) {
 		return urlUiExtjsModules + "/" + bundle + urlUiExtjsModuleSubpath + "/"
 				+ type + "/" + name + ".js";
 	}
 
 	/**
 	 * Return the URL of the translation file for the specified component.
 	 * 
 	 * @param bundle
 	 * @param type
 	 * @param name
 	 * @param language
 	 * @return
 	 */
 	private String urlTrl(String cmp, String language) {
 		String[] tokens = cmp.split("/");
 		String bundle = tokens[0];
 		String type = tokens[1];
 		String name = tokens[2];
 
 		return this.urlTrl(bundle, type, name, language);
 	}
 
 	/**
 	 * Return the URL of the translation file for the specified component.
 	 * 
 	 * @param bundle
 	 * @param type
 	 * @param name
 	 * @param language
 	 * @return
 	 */
 	private String urlTrl(String bundle, String type, String name,
 			String language) {
 		return urlUiExtjsModulesI18n + "/" + language + "/" + bundle + "/"
 				+ type + "/" + name + ".js";
 	}
 
 	public HttpClient getHttpClient() {
 		return httpClient;
 	}
 
 	public void setHttpClient(HttpClient httpClient) {
 		this.httpClient = httpClient;
 	}
 
 	public ObjectMapper getJsonMapper() {
 		return jsonMapper;
 	}
 
 	public void setJsonMapper(ObjectMapper jsonMapper) {
 		this.jsonMapper = jsonMapper;
 	}
 
 	public String getUrlUiExtjsModules() {
 		return urlUiExtjsModules;
 	}
 
 	public void setUrlUiExtjsModules(String urlUiExtjsModules) {
 		this.urlUiExtjsModules = urlUiExtjsModules;
 	}
 
 	public String getUrlUiExtjsModuleSubpath() {
 		return urlUiExtjsModuleSubpath;
 	}
 
 	public void setUrlUiExtjsModuleSubpath(String urlUiExtjsModuleSubpath) {
 		this.urlUiExtjsModuleSubpath = urlUiExtjsModuleSubpath;
 	}
 
 	public String getUrlUiExtjsModulesI18n() {
 		return urlUiExtjsModulesI18n;
 	}
 
 	public void setUrlUiExtjsModulesI18n(String urlUiExtjsModulesI18n) {
 		this.urlUiExtjsModulesI18n = urlUiExtjsModulesI18n;
 	}
 }
