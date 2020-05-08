 package org.fiz;
 
 /**
  * A FormSection displays a collection of text entries and/or other controls
  * that allow the user to input data and then submit the result.  FormSections
  * support the following constructor properties:
  *
  *   buttonStyle:    (optional) The name of a style for the form's submit
  *                   button: the value is the name of a template in the
  *                   {@code styles} configuration dataset.  Defaults to
  *                   {@code FormSection.button}; use an empty
  *                   string if you don't want a submit button to appear.
  *   class:          (optional) Used as the {@code class} attribute for
  *                   the HTML table that displays the FormSection.
  *                   Defaults to {@code FormSection}.
  *   elementErrorStyle:   (optional) When the {@code post} method encounters
  *                   an error in its data request and displays an error
  *                   message next to the culprit form element, this value
  *                   specifies a template in the {@code styles} dataset,
  *                   which is expanded with the error data and the main
  *                   dataset to produce the HTML to display.  Defaults to
  *                   "FormSection.elementError".
  *   errorStyle:     (optional) If an error occurs in {@code request} then
  *                   this property contains the name of a template in the
  *                   {@code styles} dataset, which is expanded with the
  *                   error data and the main dataset.  The resulting HTML
  *                   is displayed in place of the FormSection.  In addition,
  *                   if there exists a template in the {@code styles} dataset
  *                   with the same name followed by "-bulletin", it is expanded
  *                   and the resulting HTML is displayed in the bulletin.
  *                   Defaults to "FormSection.error".
  *   id:             (required) Used as the {@code id} attribute for the
  *                   HTML form element that displays the FormSection and
  *                   for various other purposes.  Must be unique among all
  *                   id's for the page.
  *   initialValues:  (optional) A nested dataset providing initial values
  *                   to display in the form.  This property is used only
  *                   if the {@code request} property is omitted; the values
  *                   in the nested dataset are handled in the same way as the
  *                   values in a response to {@code request}.
  *   layout:         (optional) If specified with the value {@code vertical}
  *                   thean the form is laid out in a single column with
  *                   labels above controls.  Otherwise (default) a
  *                   side-by-side layout is used with labels in the left
  *                   column and controls in the right column.
  *   postUrl:        (optional) When the form is submitted an Ajax request
  *                   is sent to this URL.  The caller must ensure that
  *                   this URL is implemented by an Interactor.  Defaults
  *                   to {@code post}.
  *   request:        (optional) Specifies a DataRequest that will supply
  *                   initial values to display in the FormSection (either
  *                   the name of a request in the {@code dataRequests}
  *                   configuration dataset or a nested dataset containing
  *                   the request's arguments directly).  If the response to
  *                   the request contains a {@code record} nested dataset,
  *                   then the contents of that nested dataset will be used
  *                   to provide the form's initial data.  If there is no
  *                   {@code record} nested dataset then the top-level contents
  *                   of the results are used for the form's initial data.
  *                   If this property is omitted the form will initially be
  *                   empty.
  *
  * When a Fiz form is posted, the form response is targeted at a hidden
  * iframe, not the main window.  This means that the post methods for a
  * form should not generate HTML, since it will not be seen.  Instead,
  * a post method should normally invoke ClientRequest.redirect to
  * redirect the browser to a results page.  Post methods can also call
  * ClientRequest.evalJavascript to update the form page; this approach is
  * used by methods such as FormSection.elementError to display error
  * information inline in the form.
  *
  * FormSection automatically displays help text for a form element when
  * the mouse moves over the form element.  To locate help text for a
  * given element, FormSection checks the following locations, in order:
  * * A {@code help} property on the form element.
  * * The element named <i>formId.elementId</i> in the {@code help}
  *   configuration dataset, where <i>formId</i> is the {@code id}
  *   property for the form, and <i>elementId</i> is the {@code id} property
  *   for the form element.
  * * The element named <i>elementId</i> in the {@code help}
  *   configuration dataset, where <i>elementId</i> is the {@code id}
  *   property for the form element.
  * The help text is raw text, not HTML or a template.
  */
 public class FormSection extends Section {
     /**
      * PostError is thrown when the {@code post} method detects an error
      * in its DataRequest.
      */
     public static class PostError extends DatasetError
             implements HandledError {
         /**
          * Construct a PostError with the error data returned by the failed
          * DataRequest.
          * @param errorDatasets    One or more data sets, each describing
          *                         an error.
          */
         public PostError(Dataset... errorDatasets) {
             super(errorDatasets);
         }
     }
 
     /**
      * Form elements throw FormDataException in their {@code collect} methods
      * when they detect a problem with incoming form data.
      */
     public static class FormDataException extends Exception {
         /**
          * Construct a FormDataException with a given message.
          * @param message          Message describing the problem.
          */
         public FormDataException(String message) {
             super(message);
         }
     }
 
     // The following variables are copies of constructor arguments.  See
     // the constructor documentation for details.
     protected FormElement[] elements;
 
     // Style to use for the form's buttons; read from the "buttonStyle"
     // property.
     protected String buttonStyle;
 
     // The {@code id} property for the form, copied from {@code properties}.
     protected String id;
 
     // The {@code help} configuration dataset:
     protected Dataset helpConfig;
 
     // Nested dataset within {@code helpConfig} whose name matches our
     // id (contains help text specific to this form).  Null means no
     // such dataset.
     protected Dataset nestedHelp;
 
     // Used to generate a <div> into which error information for an element
     // can be injected later; @1 is the form's id, @2 is the element's id.
     protected static final String diagnosticTemplate =
             "<div id=\"@(1)_@(2)_diagnostic\" class=\"diagnostic\" " +
             "style=\"display:none\"></div>";
 
     // The following variable is used to identify the first error in
     // form data for a given post.  False means that the current call
     // to the {@code post} method has not yet invoked the {@code elementError}
     // method.
     protected boolean anyElementErrors;
 
     // The following variable is used to make sure old form element
     // errors get cleared no more than once during each invocation of the
     // {@code post} method.
     protected boolean oldElementErrorsCleared;
 
     /**
      * Construct a FormSection.
      * @param properties           Contains configuration information
      *                             for the form; see description above.
      * @param elements             The remaining arguments describe the
      *                             elements to appear in the form.
      */
     public FormSection(Dataset properties, FormElement ... elements) {
         this.properties = properties;
         this.elements = elements;
         buttonStyle = properties.check("buttonStyle");
         if (buttonStyle == null) {
             buttonStyle = "FormSection.button";
         }
         helpConfig = Config.getDataset("help");
         nestedHelp = helpConfig.checkChild(properties.get("id"));
         id = properties.get("id");
     }
 
     /**
      * Invoke the {@code collect} methods on each of the FormElements in this
      * form to collect and translate the form's posted data.  This method
      * assumes that submitted data from the form is available in the main
      * dataset for {@code cr}.
      * @param cr                   Overall information about the client
      *                             request being serviced.  Used for
      *                             handling validation errors.
      * @return                     A Dataset containing the submitted data
      *                             from the form, potentially translated
      *                             by the {@code collect} methods.
      * @throws PostError           Thrown if any of the {@code collect}
      *                             methods generated an error.  In this case
      *                             AJAX actions will have been generated to
      *                             display information about the error.
      */
     public Dataset collectFormData(ClientRequest cr) {
         // Give each of the form elements a chance to collect and transform
         // the data for which it is responsible.
         Dataset postData = new Dataset();
         Dataset main = cr.getMainDataset();
         Dataset errorData = null;
         for (FormElement element : elements) {
             try {
                 element.collect(cr, main, postData);
             }
             catch (FormDataException e) {
                 // The data for this form element was invalid; generate
                 // AJAX actions to display an error message.
                 String id = element.getId();
                 errorData = new Dataset("message", e.getMessage(),
                         "culprit", id);
                 elementError(cr, errorData, id);
             }
         }
 
         // If there were any errors generated by the form elements,
         // throw an error containing information about the last of them.
         if (errorData != null) {
             throw new PostError(errorData);
         }
         return postData;
     }
 
     /**
      * When an error is detected in form data and the problem can be traced
      * to a particular form element, this method is invoked to generate
      * AJAX actions to display information about the error next to the form
      * element.
      * @param cr                   Overall information about the client
      *                             request being serviced.
      * @param errorData            Dataset containing information about the
      *                             error.
      * @param elementId            Identifier for the form element
      *                             responsible for the error; typically the
      *                             same as the {@code culprit} value in
      *                             {@code errorData}.
      */
     public void elementError(ClientRequest cr, Dataset errorData,
             String elementId) {
         // Generate  HTML for the error message.
         StringBuilder html = new StringBuilder(100);
         String templateName = properties.check("elementErrorStyle");
         if (templateName == null) {
             templateName = "FormSection.elementError";
         }
         Template.expand(Config.getPath("styles", templateName),
                 new CompoundDataset(errorData, cr.getMainDataset()), html);
 
         // Display a bulletin message indicating that there are problems,
         // but only generate one message regardless of how many errors have
         // occurred during this post.
         if (!anyElementErrors) {
             cr.addErrorsToBulletin(new Dataset("message",
                     "One or more of the input fields are invalid; " +
                     "see details below."));
             anyElementErrors = true;
             clearOldElementErrors(cr);
         }
 
         // Invoke a Javascript method, passing it information about
         // the form element plus the HTML.
         cr.evalJavascript(Template.expand("Fiz.ids.@1.elementError(" +
                 "\"@(1)_@2\", \"@3\");\n", Template.SpecialChars.JAVASCRIPT,
                 id, elementId, html));
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
     public void html(ClientRequest cr) {
         Html html = cr.getHtml();
         StringBuilder out = html.getBody();
         Dataset mainDataset = cr.getMainDataset();
 
         // Create a dataset that provides access to the initial values
         // for the form elements.
         Dataset data;
         if (dataRequest != null) {
             Dataset response = dataRequest.getResponseData();
             if (response == null) {
                 // An error occurred in the request.
                 Template.expand("\n<!-- Start FormSection @id -->\n",
                         properties, out);
                 cr.showErrorInfo(properties.check("errorStyle"),
                         "FormSection.error", dataRequest.getErrorData());
                 Template.expand("\n<!-- End FormSection @id -->\n",
                         properties, out);
                 return;
             }
             if (response.containsKey("record")) {
                 response = response.getChild("record");
             }
             data = new CompoundDataset(response, mainDataset);
         } else {
             Dataset initialValues = properties.checkChild("initialValues");
             if (initialValues != null) {
                 data = new CompoundDataset(initialValues, mainDataset);
             } else {
                 data = cr.mainDataset;
             }
         }
 
         if (!properties.containsKey("class")) {
             html.includeCssFile("FormSection.css");
         }
         // Notes:
         // * The <div> below is used to hold a temporary iframe that
         //   receives responses to form submits.  See the documentation
         //   for the Javascript function Fiz.FormSection.submit for
         //   details.
         // * Create a hidden form element containing the form's id; we
         //   will need it in order to generate responses to form submissions.
         Template.expand("\n<!-- Start FormSection @id -->\n" +
                 "<div id=\"@(id)_target\" style=\"display:none;\"></div>\n" +
                 "<form id=\"@id\" class=\"@class?{FormSection}\" " +
                 "onsubmit=\"return Fiz.ids.@id.submit();\" " +
                 "action=\"@postUrl?{post}\" method=\"post\" " +
                 "enctype=\"multipart/form-data\">\n" +
                 "  <input type=\"hidden\" name=\"fiz_formId\" " +
                 "value=\"@id\" />\n",
                 properties, out);
         innerHtml(cr, data, out);
         Template.expand("</form>\n" +
                 "<!-- End FormSection @id -->\n",
                 properties, out);
 
         // Generate a Javascript object containing information about the form.
         cr.evalJavascript("Fiz.ids.@id = new Fiz.FormSection(\"@id\");\n",
                 properties);
         html.includeJsFile("fizlib/FormSection.js");
     }
 
     /**
      * Interactors invoke this method when a form is posted.  This
      * method invokes {@code requestName}, including in the request the data
      * from the form, and handles errors that occur along the way (e.g.,
      * if there is an error related to data in the form then an Ajax
      * response will be generated to display an error message in the form).
      * @param cr                   Overall information about the client
      *                             request being serviced; must be an
      *                             Ajax request.
      * @param requestName          Name of the DataRequest to invoke
      *                             using the form's data; must be defined
      *                             in the {@code dataRequests} configuration
      *                             dataset.
      * @return                     The dataset containing the result from
      *                             {@code requestName}.
      * @throws PostError           Thrown if {@code requestName} returns
      *                             an error or if form elements found some
      *                             of the submitted data to be invalid.  This
      *                             method will handle the error by scheduling
      *                             various Ajax responses to display error
      *                             information in the form;  the error is
      *                             thrown to abort any remaining processing
      *                             of the request.
      */
     public Dataset post(ClientRequest cr, String requestName)
             throws PostError {
         // Collect the form's data, issue a request to the data manager,
         // and wait for it to complete.
         anyElementErrors = false;
         oldElementErrorsCleared = false;
         DataRequest request = new DataRequest(requestName,
                 cr.getMainDataset());
         request.getRequestData().createChild("record", collectFormData(cr));
         Dataset responseData = request.getResponseData();
         if (responseData != null) {
             return responseData;
         }
 
         // One or more errors occurred while processing the request.  If an
         // error can be attributed to a particular form element, display an
         // error message next to the culprit.  Otherwise display the error
        // message in the bulletin.  If there are any element-specific
        // messages currently displayed because of a previous post,
        // undisplay them.
         Dataset[] errors = request.getErrorData();
        clearOldElementErrors(cr);
         for (Dataset errorData : errors) {
             String culprit = errorData.check("culprit");
             boolean foundCulprit = false;
             if (culprit != null) {
                 for (FormElement element : elements) {
                     if (element.responsibleFor(culprit)) {
                         elementError(cr, errorData, element.getId());
                         foundCulprit = true;
                         break;
                     }
                 }
             }
             if (!foundCulprit) {
                 cr.addErrorsToBulletin(errorData);
             }
         }
         throw new PostError(errors);
     }
 
     /**
      * This method is invoked during the first phase of rendering a page;
      * it calls {@code cr.registerDataRequest} for each of the
      * DataRequests needed by this section to gather data to be displayed.
      * @param cr                   Overall information about the client
      *                             request being serviced.
      */
     @Override
     public void registerRequests(ClientRequest cr) {
         dataRequest = cr.registerDataRequest(properties, "request");
         for (FormElement element : elements) {
             element.registerRequests(cr, properties.check("request"));
         }
     }
 
     /**
      * This method generates an AJAX action to clear any old error messages
      * attached to form elements from previous failed post operations.
      * This method makes sure that we only clear the messages once per
      * called to the {@code post} method.
      * @param cr                   Overall information about the client
      *                             request being serviced.
      */
     protected void clearOldElementErrors(ClientRequest cr) {
         if (!oldElementErrorsCleared) {
             cr.evalJavascript("Fiz.ids.@id.clearElementErrors();\n",
                     properties);
             oldElementErrorsCleared = true;
         }
     }
 
     /**
      * Try to find help text to display for a form element, checking in
      * three places: first, a {@code help} property on the form element;
      * second, a property {@code x.y} in the help configuration dataset,
      * where {@code x} is the {@code id} property of the FormSection and
      * {@code y} is the {@code id} property of the FormElement; and third,
      * a property in the help configuration dataset was named is the
      * value of the form element's {@code id} property.
      * @param element              A FormElement in this form.
      * @return                     The help text for the form element,
      *                             or null if none could be found.
      */
     protected String getHelpText(FormElement element) {
         String result = element.checkProperty("help");
         if (result != null) {
             return result;
         }
         String id = element.getId();
         if (nestedHelp != null) {
             result = nestedHelp.check(id);
             if (result != null) {
                 return result;
             }
         }
         return helpConfig.check(id);
     }
 
     /**
      * This method generates the inner portion of the HTML for the form
      * (everything inside the {@code <form>} element, which includes the
      * actual form elements and submit button).  Subclasses can override
      * this method to create different form layouts.
      * @param cr                   Overall information about the client
      *                             request being serviced.
      * @param data                 Data for the form (a CompoundDataset
      *                             including both form data, if any, and the
      *                             main dataset).
      * @param out                  Generated HTML is appended here.
      */
     protected void innerHtml(ClientRequest cr, Dataset data,
             StringBuilder out) {
         boolean anyHidden = false;
 
         String layout = properties.check("layout");
         boolean vertical = (layout != null) && layout.equals("vertical");
         Template.expand("  <table cellspacing=\"0\" class=\"@1\">\n", out,
                 vertical ? "vertical" : "sideBySide");
 
         // Each iteration of the following loop displays a single
         // FormElement.
         for (FormElement element : elements) {
             if (element instanceof HiddenFormElement) {
                 // Don't render hidden form elements here, since they may
                 // mess up the table formatting; they will get rendered
                 // separately, at the end of the table.
                 anyHidden = true;
                 continue;
             }
             if (vertical) {
                 verticalElement(cr, element, data, out);
             } else {
                 sideBySideElement(cr, element, data, out);
             }
         }
 
         // Add the submit button if desired, and finish up the form.
         if (buttonStyle.length() > 0) {
             Template.expand("    <tr>\n      <td class=\"submit\" " +
                     "{{colspan=\"@1\"}}>", out,
                     (vertical ? null : "2"));
             Template.expand(Config.getPath("styles", buttonStyle),
                     new CompoundDataset(properties, cr.getMainDataset()),
                     out);
             out.append("</td>\n    </tr>\n");
         }
         out.append("  </table>\n");
 
         // If there are hidden form elements, render them here, outside
         // the main table.
         if (anyHidden) {
             for (FormElement element : elements) {
                 if (element instanceof HiddenFormElement) {
                     out.append("  ");
                     element.html(cr, data, out);
                     out.append("\n");
                 }
             }
         }
     }
 
     /**
      * Generate HTML for a single FormElement, consisting of one table
      * row with a column for the label and the column for the control.
      * @param cr                   Overall information about the client
      *                             request being serviced.
      * @param element              The form element to be rendered.
      * @param data                 Data for the form (a CompoundDataset
      *                             including both form data, if any, and the
      *                             main dataset).
      * @param out                  Generated HTML is appended here.
      */
     protected void sideBySideElement(ClientRequest cr, FormElement element,
             Dataset data, StringBuilder out) {
         String elementId = element.getId();
         Template.expand("    <tr id=\"@(1)_@2\" {{title=\"@3\"}}>\n", out,
                 id, elementId, getHelpText(element));
         int startingLength = out.length();
         out.append("      <td class=\"label\">");
         if (!element.labelHtml(cr, data, out)) {
             // This element requests that we not display any label and
             // instead let the control span both columns.  Erase the
             // information for this row and regenerate the row with
             // a single column.
             out.setLength(startingLength);
             out.append("      <td class=\"control\" " +
                     "colspan=\"2\">");
         } else {
             out.append("</td>\n      <td class=\"control\">");
         }
         element.html(cr, data, out);
 
         // Create an extra <div> underneath the control for displaying
         // error messages pertaining to this form element.
         Template.expand(diagnosticTemplate, out, id, elementId);
         out.append("</td>\n    </tr>\n");
 
     }
 
     /**
      * Generate HTML for a single FormElement, consisting of a table
      * row separate divs for the label, the control, and diagnostic
      * information.
      * @param cr                   Overall information about the client
      *                             request being serviced.
      * @param element              The form element to be rendered.
      * @param data                 Data for the form (a CompoundDataset
      *                             including both form data, if any, and the
      *                             main dataset).
      * @param out                  Generated HTML is appended here.
      */
     protected void verticalElement(ClientRequest cr, FormElement element,
             Dataset data, StringBuilder out) {
         String elementId = element.getId();
         Template.expand("    <tr id=\"@(1)_@2\" {{title=\"@3\"}}><td>\n", out,
                 id, elementId, getHelpText(element));
         int rowStart = out.length();
         out.append("      <div class=\"label\">");
         int labelStart = out.length();
         if (!element.labelHtml(cr, data, out)
                 || (labelStart == out.length())) {
             // No label for this element; discard the entire row.
             out.setLength(rowStart);
         } else {
             out.append("</div>\n");
         }
         out.append("      <div class=\"control\">");
         element.html(cr, data, out);
         out.append("</div>\n      ");
 
         // Create an extra <div> underneath the control for displaying
         // error messages pertaining to this form element.
         Template.expand(diagnosticTemplate, out, id, elementId);
         out.append("\n    </td></tr>\n");
     }
 
     /**
      * This method is invoked by ClientRequest.finish to return the response
      * for a form submission.
      * @param cr                   Overall information about the client
      *                             request being serviced.
      * @param javascript           Javascript code that embodies the response;
      *                             This method will make sure the script is
      *                             eval-ed in the window/frame containing
      *                             the form responsible for the post.
      */
     protected static void sendFormResponse(ClientRequest cr,
             CharSequence javascript) {
         // In order to handle the response correctly, we need the id for the
         // form.  Fortunately, there should be a value "fiz_formId" in
         // the form's data.
         cr.getHtml().includeJavascript(
                 "window.parent.Fiz.FormSection.handleResponse(" +
                 "\"@1\", \"@2\");\n", cr.getMainDataset().get("fiz_formId"),
                 javascript);
 
     }
 }
