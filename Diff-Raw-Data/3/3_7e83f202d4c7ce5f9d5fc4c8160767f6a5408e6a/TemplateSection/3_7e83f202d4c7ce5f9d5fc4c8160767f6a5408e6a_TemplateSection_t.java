 package org.fiz;
 
 /**
  * A TemplateSection is a simple form of Section that generates HTML from
  * a single template.  TemplateSections support the following constructor
  * properties:
  *
  *   errorStyle:     (optional) If an error occurs in {@code request} then
  *                   this property contains the name of a template in the
  *                   {@code styles} dataset, which is expanded with the
  *                   error data and the main dataset.  The resulting HTML
  *                   is displayed in place of the TemplateSection.  In addition,
  *                   if there exists a template in the {@code styles} dataset
  *                   with the same name followed by "-bulletin", it is expanded
  *                   and the resulting HTML is displayed in the bulletin.
  *                   Defaults to "TemplateSection.error".
  *   file:           (optional) The name of a file in the {@code WEB-INF}
  *                   directory that contains the template for the section.
  *                   If this property has specified that it takes precedence
  *                   over {@code template}.  Expanded in the same way as
  *                   {@code request}.
  *   request:        (optional) The name of a data request registered by
  *                   the caller with ClientRequest.addDataRequest.  The
  *                   dataset returned by this request is used when
  *                   expanding the template.
  *   template:       (optional) Template that will generate HTML for the
  *                   section.  If {@code request} is specified then the
  *                   template is expanded in the context of the response
  *                   to that request plus the main dataset; otherwise the
  *                   template is expanded in the context of the main dataset.
  */
 public class TemplateSection extends Section {
     // The following variables hold values for the properties that define
     // the section; see above for definitions.
     protected String template;
 
     /**
      * Construct a TemplateSection from a dataset containing properties.
      * @param properties           Contains configuration information
      *                             for the section; see description above.
      */
     public TemplateSection(Dataset properties) {
         template = properties.check("template");
         this.properties = properties;
     }
 
     /**
      * Construct a TemplateSection from a template string.  The section
      * will not issue any data requests.
      * @param template             Value of the {@code template} property for
      *                             the section.
      */
     public TemplateSection(String template) {
         this.template = template;
     }
 
     /**
      * Construct a TemplateSection given values for the {@code template} and
      * {@code request} properties.
      * @param request              Value of the {@code request} property for
      *                             the section.
      * @param template             Value of the {@code template} property for
      *                             the section.
      */
     public TemplateSection(String request, String template) {
         this.properties = new Dataset("request", request);
         this.template = template;
     }
 
     /**
      * This method is invoked during the final phase of rendering a page;
      * it generates HTML for this section and appends it to the Html
      * object associated with {@code cr}.
      * @param cr                   Overall information about the client
      *                             request being serviced; HTML will be
      *                             appended to {@code cr.getHtml()}.
      */
     @Override
     public void render(ClientRequest cr) {
         Dataset data;
         String requestName;
         if (properties != null) {
             requestName = properties.check("request");
         } else {
             requestName = null;
         }
         if (requestName != null) {
             DataRequest dataRequest = cr.getDataRequest(requestName);
             Dataset response = dataRequest.getResponseData();
             if (response == null) {
                 // There was an error fetching our data; display
                 // appropriate error information.
                 Dataset[] errors = dataRequest.getErrorData();
                 String errorStyle = (properties == null) ? null :
                         properties.check("errorStyle");
                 cr.showErrorInfo(errorStyle, "TemplateSection.error",
                         errors[0]);
                 return;
             }
            if (response.containsKey("record")) {
                response = response.getChild("record");
            }
             data = new CompoundDataset(response, cr.getMainDataset());
         } else {
             data = cr.getMainDataset();
         }
 
         // Find the template (either a file on disk or a property from the
         // configuration data set) and expand it.
         if (properties != null) {
             String fileName = properties.check("file");
             if (fileName != null) {
                 StringBuilder contents = Util.readFileFromPath(fileName,
                         "template",
                         cr.getServletContext().getRealPath("WEB-INF"));
                 Template.expand(contents, data, cr.getHtml().getBody());
                 return;
             }
         }
         Template.expand(template, data, cr.getHtml().getBody());
     }
 }
