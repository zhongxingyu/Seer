 /*
  * (C) Copyright 2006-20011 Nuxeo SAS (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     Nuxeo - initial API and implementation
  *
  */
 
 package org.nuxeo.ecm.platform.reporting.jaxrs;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.OutputStream;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpSession;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.eclipse.birt.report.engine.api.HTMLRenderOption;
 import org.eclipse.birt.report.engine.api.HTMLServerImageHandler;
 import org.eclipse.birt.report.engine.api.PDFRenderOption;
 import org.nuxeo.common.utils.Path;
 import org.nuxeo.ecm.platform.reporting.api.ReportInstance;
 import org.nuxeo.ecm.platform.reporting.report.ReportParameter;
 import org.nuxeo.ecm.webengine.WebEngine;
 import org.nuxeo.ecm.webengine.forms.FormData;
 import org.nuxeo.ecm.webengine.model.WebObject;
 import org.nuxeo.ecm.webengine.model.impl.DefaultObject;
 
 /**
  * JAX-RS Resource to represent a {@link ReportInstance}.
  *
  * Provides html and PDF views
  *
  * @author Tiry (tdelprat@nuxeo.com)
  *
  */
 @WebObject(type = "report")
 public class ReportResource extends DefaultObject {
 
     protected static final String USER_PARAMS_NAME = "birtUserParams";
 
     protected ReportInstance report;
 
     @Override
     protected void initialize(Object... args) {
         report = (ReportInstance) args[0];
     }
 
     @GET
     @Produces("text/html")
     public String doGet() throws Exception {
         return report.getModel().getReportName();
     }
 
     protected String getReportKey() {
         return report.getDoc().getId() + "-"
                 + getContext().getUserSession().getPrincipal().getName();
     }
 
     protected String buildTmpPath(String key) {
         String dirPath = new Path(System.getProperty("java.io.tmpdir")).append(
                 "birt-" + key).toString();
         File baseDir = new File(dirPath);
         if (baseDir.exists()) {
             return dirPath;
         }
         baseDir.mkdir();
         File imagesDir = new File(dirPath + "/images");
         imagesDir.mkdir();
 
         return dirPath;
     }
 
     @GET
     @javax.ws.rs.Path("images/{key}/{name}")
     public Object getImage(@PathParam("key")
     String key, @PathParam("name")
     String name) throws Exception {
 
         String tmpPath = buildTmpPath(key);
         File imageFile = new File(tmpPath + "/images/" + name);
         return Response.ok(new FileInputStream(imageFile)).build();
     }
 
     @GET
     @Produces("text/html")
     @javax.ws.rs.Path("editParams")
     public Object editParams(@QueryParam("target")
     String target, @QueryParam("errors")
     String errors) throws Exception {
         HttpSession session = WebEngine.getActiveContext().getRequest().getSession();
         @SuppressWarnings("unchecked")
         Map<String, Object> userParams = (Map<String, Object>) session.getAttribute(USER_PARAMS_NAME);
 
         List<ReportParameter> params = report.getReportUserParameters();
         fillReportParameters(params, userParams);
         markReportParametersInError(params, errors);
         return getView("editParams").arg("params", params).arg("target", target);
     }
 
     protected void fillReportParameters(List<ReportParameter> reportParameters,
             Map<String, Object> userParams) {
         if (userParams != null) {
             for (ReportParameter p : reportParameters) {
                 if (userParams.containsKey(p.getName())) {
                     p.setObjectValue(userParams.get(p.getName()));
                 }
             }
         }
     }
 
     protected void markReportParametersInError(
             List<ReportParameter> reportParameters, String errors) {
         if (errors != null) {
             String[] errs = errors.split(",");
             for (String err : errs) {
                 if (!err.isEmpty()) {
                     for (ReportParameter p : reportParameters) {
                         if (p.getName().equals(err)) {
                             p.setObjectValue(null);
                             p.setError(true);
                             break;
                         }
                     }
                 }
             }
         }
     }
 
     protected void readParams(Map<String, Object> userParams,
             List<String> paramsInError) throws Exception {
         List<ReportParameter> params = report.getReportUserParameters();
         if (params.size() > 0) {
             FormData data = getContext().getForm();
             if (data != null) {
                 for (ReportParameter param : params) {
                     String name = param.getName();
                     if (data.getString(name) != null) {
                         String strValue = data.getString(name);
 
                         if (param.setAndValidateValue(strValue)) {
                             Object value = param.getObjectValue();
                             userParams.put(name, value);
                         } else {
                             paramsInError.add(name);
                         }
                     } else if (!userParams.containsKey(name)) {
                         paramsInError.add(name);
                     }
                 }
             }
         }
     }
 
     @POST
     @Produces("text/html")
     @javax.ws.rs.Path("html")
     public Object editAndRenderHtml() throws Exception {
         return html(false);
     }
 
     protected Object validateInput(Map<String, Object> userParams, String target)
             throws Exception {
         HttpSession session = WebEngine.getActiveContext().getRequest().getSession();
         @SuppressWarnings("unchecked")
         Map<String, Object> savedUserParams = (Map<String, Object>) session.getAttribute(USER_PARAMS_NAME);
         if (savedUserParams != null) {
             userParams.putAll(savedUserParams);
         }
 
         List<String> errors = new ArrayList<String>();
         readParams(userParams, errors);
         saveUserParameters(userParams);
 
         if (!errors.isEmpty()) {
             String errorList = "";
             for (String err : errors) {
                 errorList = errorList + err + ",";
             }
             errorList = URLEncoder.encode(errorList, "UTF-8");
             return redirect(getPath() + "/editParams?target=" + target
                     + "&errors=" + errorList);
         }
         return null;
     }
 
     @GET
     @Produces("text/html")
     @javax.ws.rs.Path("html")
     public Object html(@QueryParam("forceFormDisplay")
    Boolean forceFormDisplay) throws Exception {
         Map<String, Object> userParams = new HashMap<String, Object>();
         Object validationError = validateInput(userParams, "html");
 
         if (validationError != null) {
             return validationError;
         }
 
        if (forceFormDisplay != null && forceFormDisplay) {
             return redirect(getPath() + "/editParams?target=html");
         }
 
         String key = getReportKey();
         String tmpPath = buildTmpPath(key);
         File reportFile = new File(tmpPath + "/report");
         OutputStream out = new FileOutputStream(reportFile);
 
         HTMLRenderOption options = new HTMLRenderOption();
         options.setImageHandler(new HTMLServerImageHandler());
         options.setOutputFormat(HTMLRenderOption.OUTPUT_FORMAT_HTML);
         options.setOutputStream(out);
         options.setBaseImageURL("images/" + key);
         options.setImageDirectory(tmpPath + "/images");
 
         report.render(options, userParams);
         return Response.ok(new FileInputStream(reportFile), MediaType.TEXT_HTML).build();
     }
 
     protected void saveUserParameters(Map<String, Object> userParams) {
         HttpSession session = WebEngine.getActiveContext().getRequest().getSession();
         session.setAttribute(USER_PARAMS_NAME, userParams);
     }
 
     @POST
     @Produces("application/pdf")
     @javax.ws.rs.Path("pdf")
     public Object editAndRenderPdf() throws Exception {
         return pdf(false);
     }
 
     @GET
     @Produces("application/pdf")
     @javax.ws.rs.Path("pdf")
    public Object pdf(@QueryParam("forceDisplayForm")
    Boolean forceDisplayForm) throws Exception {
         Map<String, Object> userParams = new HashMap<String, Object>();
         Object validationError = validateInput(userParams, "pdf");
 
         if (validationError != null) {
             return validationError;
         }
 
         if (forceDisplayForm) {
             return redirect(getPath() + "/editParams?target=pdf");
         }
 
         String key = getReportKey();
         String tmpPath = buildTmpPath(key);
         File reportFile = new File(tmpPath + "/report");
         OutputStream out = new FileOutputStream(reportFile);
 
         PDFRenderOption options = new PDFRenderOption();
         options.setImageHandler(new HTMLServerImageHandler());
         options.setOutputFormat(PDFRenderOption.OUTPUT_FORMAT_PDF);
         options.setOutputStream(out);
 
         report.render(options, userParams);
         return Response.ok(new FileInputStream(reportFile),
                 MediaType.APPLICATION_OCTET_STREAM).header(
                 "Content-Disposition", "attachment;filename=" + key + ".pdf").build();
     }
 
     @GET
     @Produces("text/html")
     @javax.ws.rs.Path("clearParams")
     public Object clearParams(@QueryParam("target")
     String target) throws Exception {
         HttpSession session = WebEngine.getActiveContext().getRequest().getSession();
         session.removeAttribute(USER_PARAMS_NAME);
         return redirect(getPath() + "/editParams?target=" + target);
     }
 
 }
