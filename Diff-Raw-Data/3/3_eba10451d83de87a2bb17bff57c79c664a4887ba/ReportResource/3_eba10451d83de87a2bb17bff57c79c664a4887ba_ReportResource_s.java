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
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
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
         List<ReportParameter> params = report.getReportUserParameters();
 
         if (errors != null) {
             String[] errs = errors.split(",");
             for (String err : errs) {
                 if (!err.isEmpty()) {
                     for (ReportParameter p : params) {
                         if (p.getName().equals(err)) {
                             p.setError(true);
                             break;
                         }
                     }
                 }
             }
         }
         return getView("editParams").arg("params", params).arg("target", target);
     }
 
     protected boolean readParams(Map<String, Object> userParams,
             List<String> paramsInError) throws Exception {
         List<ReportParameter> params = report.getReportUserParameters();
         if (params.size() > 0) {
             FormData data = getContext().getForm();
             if (data != null) {
                 for (ReportParameter param : params) {
                     if (data.getString(param.getName()) != null) {
                         String strValue = data.getString(param.getName());
 
                         if (param.setAndValidateValue(strValue)) {
                             Object value = param.getObjectValue();
                             userParams.put(param.getName(), value);
                         } else {
                             paramsInError.add(param.getName());
                         }
                     } else {
                         paramsInError.add(param.getName());
                     }
                 }
             }
             if (paramsInError.size() > 0) {
                 return false;
             }
         }
         return true;
     }
 
     @POST
     @Produces("text/html")
     @javax.ws.rs.Path("html")
     public Object editAndRenderHtml() throws Exception {
         return html();
     }
 
     protected Object validateInput(Map<String, Object> userParams, String target)
             throws Exception {
         List<String> errors = new ArrayList<String>();
         if (!readParams(userParams, errors)) {
             String errorList = "";
             for (String err : errors) {
                 errorList = errorList + err + ",";
             }
             return redirect(getPath() + "/editParams?target=" + target
                     + "&errors=" + errorList);
         }
         return null;
     }
 
     @GET
     @Produces("text/html")
     @javax.ws.rs.Path("html")
     public Object html() throws Exception {
         Map<String, Object> userParams = new HashMap<String, Object>();
         Object validationError = validateInput(userParams, "html");
         if (validationError != null) {
             return validationError;
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
 
     @POST
     @Produces("application/pdf")
     @javax.ws.rs.Path("pdf")
     public Object editAndRenderPdf() throws Exception {
         return pdf();
     }
 
     @GET
     @Produces("application/pdf")
     @javax.ws.rs.Path("pdf")
     public Object pdf() throws Exception {
         Map<String, Object> userParams = new HashMap<String, Object>();
         Object validationError = validateInput(userParams, "pdf");
         if (validationError != null) {
             return validationError;
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
 
 }
