 /*
  * Copyright IBM Corp. 2012
  */
 
 package org.rstl;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.rstl.context.TemplateContext;
 import org.rstl.resource.client.ResourceRegistry;
 
import com.ibm.json.java.JSON;
 
 /**
  * Implementation of the resource references in a template
  */
 public class ResourceRef {
 	private static final String CLASS_NAME = ResourceRef.class.getCanonicalName();
 	private static final Logger _LOGGER = Logger.getLogger(CLASS_NAME);
 	private static final String RESOURCE_PREFIX = "res_";
 	private static final boolean RESOURCE_FETCHED = true;
 
 	private String resourceName;
 	private String widgetName;
 	private String format;
 	private String variableName;
 
 	private String resolvedResourceName;
 	private List<VarRef> variableReferences = new ArrayList<VarRef>();
 	private List<Object> idSegments;
 	private boolean resolved = false;
 	private boolean isBadResourceName = false;
 	private int syntaxErrorAt;
 
 	public ResourceRef(String resourceName, String widgetName,
 			String format, String variableName) {
 		this.resourceName = resourceName;
 		this.widgetName = widgetName;
 		this.format = format;
 		this.variableName = variableName;
 		if (resourceName.indexOf('{') != -1) {
 			idSegments = new ArrayList<Object>();
 			int k = resourceName.indexOf('{');
 			int l = 0;
 			while (k != -1) {
 				if (k != l) {
 					idSegments.add(resourceName.substring(l, k));
 				}
 				l = resourceName.indexOf('}', k);
 				if (l != -1) {
 					String varName = resourceName.substring(k + 1, l);
 					VarRef ref = new VarRef(varName);
 					idSegments.add(ref);
 					variableReferences.add(ref);
 				} else {
 					break;
 				}
 				l = l + 1;
 				if (l >= resourceName.length())
 					break;
 				k = resourceName.indexOf('{', l);
 			}
 			if (l != -1 && l < resourceName.length()) {
 				idSegments.add(resourceName.substring(l));
 			}
 			if (l == -1) {
 				isBadResourceName = true;
 				syntaxErrorAt = k;
 			}
 		}
 		if (null == idSegments) {
 			resolved = true;
 			resolvedResourceName = resourceName;
 		}
 	}
 
 	public boolean isBadResourceName() {
 		return isBadResourceName;
 	}
 
 	public int getErrorLocation() {
 		return syntaxErrorAt;
 	}
 
 	/**
 	 * Fetch the resolved resource from a remote system, Database or engine. If
 	 * there is no variable, then render the HTML representation of the resource
 	 * to the writer. If not, assign the resource representation (Map/Bean) to
 	 * the variable in the context.
 	 * 
 	 * @param ctxt
 	 * @param w
 	 * @param templateInfo
 	 *            If not null, the resource representation will be annotated
 	 *            with template metadata - generic id, widget if any etc. The
 	 *            key-values that are in this map will also be
 	 */
 	public void fetchResource(TemplateContext ctxt, Writer w,
 			Map<String, String> templateInfo) {
 		String resId = getResolvedId(ctxt, true);
 		if (!resolved) {
 			_LOGGER.logp(Level.FINE, CLASS_NAME, "fetchResource", "Warning: Resource : " + resId
 					+ " is not completely resolved.");
 		}
 
 		_LOGGER.logp(Level.FINE, CLASS_NAME, "fetchResource", "Fetching Resource " + resId);
 		HttpClient client = new DefaultHttpClient();
 		HttpGet req = new HttpGet(ResourceRegistry.getAbsoluteUri(resId));
 		if (format.equalsIgnoreCase("xhtml")) {
 			req.setHeader("Accept", "application/xhtml+xml");
 		} else {
 			req.setHeader("Accept", "application/json");
 		}
 		try {
 			HttpResponse res = client.execute(req);
 			// Is response valid ?
 			StatusLine status = res.getStatusLine();
 			if (status.getStatusCode() != 200) {
 				if (format.equalsIgnoreCase("json") && variableName != null
 						& !variableName.isEmpty()) {
 					// fetch failed - bail !
 				} else {
 					writeXhtmlResourceHeader(ctxt, w, templateInfo,
 							!RESOURCE_FETCHED);
 					writeXhtmlResourceFooter(ctxt, w, !RESOURCE_FETCHED);
 				}
 			} else {
 				char[] buf = new char[16384];
 				HttpEntity entity = res.getEntity();
 				InputStream is = entity.getContent();
 				BufferedReader reader = new BufferedReader(
 						new InputStreamReader(is));
 				Header contentType = res.getFirstHeader("Content-Type");
 				if ((contentType.getValue().equals("application/json") || contentType
 						.getValue().equals("text/json"))
 						&& format.equalsIgnoreCase("json")
 						&& variableName != null & !variableName.isEmpty()) {
 					
					Object jsonObj = JSON.parse(reader);
					if (jsonObj instanceof Map) {
						Map<String, Object> jObj = (Map<String, Object>) jsonObj;
 						jObj.put("resourceid", resolvedResourceName);
 						if (null != templateInfo) {
 							jObj.put("genericid", resourceName);
 							jObj.put("widget", widgetName);
 							for (String key : templateInfo.keySet()) {
 								jObj.put(key, templateInfo.get(key));
 							}
 						}
						jsonObj = jObj;
 					}
					ctxt.put(variableName, jsonObj);
 					
 				} else {
 					// write the representation to the provided writer
 					writeXhtmlResourceHeader(ctxt, w, templateInfo,
 							RESOURCE_FETCHED);
 					int readResult = 0;
 					do {
 						readResult = reader.read(buf);
 						if (readResult == -1) {
 							break;
 						}
 						w.write(buf, 0, readResult);
 					} while (true);
 					writeXhtmlResourceFooter(ctxt, w, RESOURCE_FETCHED);
 				}
 			}
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Get the resolved identifier of the resource, applying the variable values
 	 * from the context provided.
 	 * 
 	 * @param ctxt
 	 * @return
 	 */
 	public String getResolvedId(TemplateContext ctxt, boolean force) {
 		resolved = resolve(ctxt);
 
 		return getResolvedId(force);
 	}
 
 	private String getResolvedId(boolean force) {
 		if (resolvedResourceName != null) {
 			return resolvedResourceName;
 		}
 		String resourceId = null;
 		if (!force) {
 			if (!resolved) {
 				resourceId = "";
 			}
 		}
 		if (null == resourceId) {
 			StringBuilder sb = new StringBuilder();
 			for (Object segment : idSegments) {
 				if (segment instanceof VarRef) {
 					VarRef ref = (VarRef) segment;
 					sb.append((null != ref.variableValue) ? ref.variableValue
 							: "");
 				} else {
 					sb.append((String) segment);
 				}
 			}
 			resourceId = sb.toString();
 		}
 		if (resolved) {
 			resolvedResourceName = resourceId;
 		}
 		return resourceId;
 	}
 
 	/**
 	 * The resource identifier of the resource being referenced. The identifier
 	 * may include variable references enclosed in $(var), where var needs to be
 	 * resolved from the context.
 	 * 
 	 * @return unresolved resource identifier
 	 */
 	public String getResourceId() {
 		return resourceName;
 	}
 
 	/**
 	 * The list of unresolved variables in the resource identifier
 	 * 
 	 * @return
 	 */
 	public List<String> getVarRefsInId() {
 		List<String> variableNames = new ArrayList<String>();
 		for (VarRef ref : variableReferences) {
 			variableNames.add(ref.variableName);
 		}
 		return variableNames;
 	}
 
 	public List<String> getUnResolvedVarRefs() {
 		List<String> variableNames = new ArrayList<String>();
 		if (!resolved) {
 			for (VarRef ref : variableReferences) {
 				if (!ref.isResolved()) {
 					variableNames.add(ref.variableName);
 				}
 			}
 		}
 		return variableNames;
 	}
 
 	/**
 	 * The name of the variable that will contain the resource representation
 	 * for forward declarations
 	 * 
 	 * @return
 	 */
 	public String getVariableName() {
 		return variableName;
 	}
 
 	/**
 	 * The name of the widget that will be used to change the presentation
 	 * behavior of the resource.
 	 * 
 	 * @return name of widget
 	 */
 	public String getWidgetName() {
 		return widgetName;
 	}
 
 	/**
 	 * Resolve the identifier of the resource using the template context
 	 * provided at runtime. Also update the widget name with the default widget
 	 * if none was provided
 	 * 
 	 * @param ctxt
 	 *            the runtime context that contains the variable values for
 	 *            resolving a resource id
 	 * @return if there are no variable references or all the variable
 	 *         references are resolved, return true
 	 */
 	public boolean resolve(TemplateContext ctxt) {
 		if (resolved || variableReferences.isEmpty()) {
 			resolved = true;
 		}
 		resolved = true;
 		for (VarRef ref : variableReferences) {
 			Object value = ctxt.get(ref.variableName);
 			if (null != value) {
 				ref.setValue(value.toString());
 			} else {
 				resolved = false;
 			}
 		}
 		return resolved;
 	}
 
 	/**
 	 * Standard header to be injected for any resource
 	 */
 	private void writeXhtmlResourceHeader(TemplateContext ctxt, Writer w,
 			Map<String, String> templateInfo, boolean resourceFetched) {
 		int divId = 0;
 		String resDivId = null;
 
 		try {
 			// Write outer div for the resource
 			w.append("<div");
 			if (resourceFetched && null != widgetName && !widgetName.isEmpty()) {
 				AtomicInteger idgen = (AtomicInteger) ctxt
 						.get(Constants.DIVID_GENERATOR);
 				divId = idgen.incrementAndGet();
 				resDivId = RESOURCE_PREFIX + divId;
 				ctxt.put(Constants.RESOURCE_DIV_ID, resDivId);
 				w.append(" id=\"" + resDivId + "\"");
 			}
 			w.append(" class=\"resource\">\n");
 			// Write the resource id associated with this representation
 			w.append("\t<div class=\"resourceid\">");
 			w.append(resolvedResourceName);
 			w.append("</div>\n");
 			// Write the template metadata resource id if template Metadata is
 			// requested
 			if (null != templateInfo) {
 				w.append("\t<div class=\"templatemeta genericid\">")
 						.append(StringEscapeUtils.escapeXml(resourceName))
 						.append("</div>");
 				w.append("\t<div class=\"templatemeta widget\">")
 						.append(StringEscapeUtils.escapeXml(widgetName))
 						.append("</div>");
 				for (String key : templateInfo.keySet()) {
 					w.append("\t<div class=\"templatemeta " + key + "\">")
 							.append(StringEscapeUtils.escapeXml(templateInfo
 									.get(key))).append("</div>");
 				}
 			}
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 		}
 	}
 
 	/**
 	 * Standard footer to be injected for any resource
 	 */
 	private void writeXhtmlResourceFooter(TemplateContext ctxt, Writer w,
 			boolean resourceFetched) {
 		try {
 			w.append("</div>\n");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// Inject widget instantiation code after the resource representation
 		// and bind it to the representation
 		if (resourceFetched && widgetName != null && !widgetName.isEmpty()) {
 			TemplateGroup myTg = (TemplateGroup) ctxt
 					.get(Constants.TEMPLATE_GROUP);
 			Template widgetTemplate = myTg.getTemplate("widgets/"
 					+ widgetName + "/instantiate.ctl");
 			if (null != widgetTemplate) {
 				widgetTemplate.render(ctxt, w, false);
 				ctxt.remove(Constants.RESOURCE_DIV_ID);
 			}
 		}
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		sb.append(resourceName).append(widgetName).append(format)
 				.append(variableName);
 		return sb.toString();
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj == this)
 			return true;
 
 		if (!(obj instanceof ResourceRef))
 			return false;
 		ResourceRef tObj = (ResourceRef) obj;
 		if (!resourceName.equals(tObj.resourceName)) {
 			return false;
 		}
 		if (null != widgetName && !widgetName.equals(tObj.widgetName)) {
 			return false;
 		}
 		if (null != variableName && !variableName.equals(tObj.variableName)) {
 			return false;
 		}
 		if (null != format && !format.equals(tObj.format)) {
 			return false;
 		}
 		if (null == widgetName && tObj.widgetName != null) {
 			return false;
 		}
 		if (null == variableName && tObj.variableName != null) {
 			return false;
 		}
 		if (null == format && tObj.format != null) {
 			return false;
 		}
 
 		return true;
 	}
 
 	@Override
 	public int hashCode() {
 		return toString().hashCode();
 	}
 
 	/**
 	 * Variable reference in resource identifier
 	 */
 	public class VarRef {
 		private String variableName;
 		private boolean resolved;
 		private String variableValue;
 
 		public VarRef(String varName) {
 			this.variableName = varName;
 			resolved = false;
 			variableValue = null;
 		}
 
 		public boolean isResolved() {
 			return resolved;
 		}
 
 		public void setValue(String value) {
 			this.variableValue = value;
 			resolved = true;
 		}
 	}
 
 }
