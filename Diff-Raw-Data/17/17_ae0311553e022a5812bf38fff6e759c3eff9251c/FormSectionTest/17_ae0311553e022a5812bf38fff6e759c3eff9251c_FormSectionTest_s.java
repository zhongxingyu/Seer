 package org.fiz;
 
 /**
  * Junit tests for the FormSection class.
  */
 public class FormSectionTest extends junit.framework.TestCase {
     // The following class is a simple FormElement that makes its own
     // data request, named after its id.
     private static class FormElementFixture extends FormElement {
         String html = "";
         public FormElementFixture(String id) {
             super(new Dataset("id", id));
         }
         public void html(ClientRequest cr, Dataset data, StringBuilder out) {
             out.append(html);
         }
         public void registerRequests(ClientRequest cr, String query) {
             cr.registerDataRequest(query + "_" + id);
         }
     }
 
     protected ClientRequest cr;
 
     public void setUp() {
         cr = new ClientRequestFixture();
     }
 
     public void test_constructor_defaultButtonStyle() {
         FormSection form = new FormSection(
                 new Dataset("id", "form1", "request", "getPerson"),
                 new EntryFormElement("name", "Name:"),
                 new EntryFormElement("age", "Age:"));
         assertEquals("properties", "id:      form1\n" +
                 "request: getPerson\n", form.properties.toString());
         assertEquals("number of elements", 2, form.elements.length);
         assertEquals("buttonStyle", "FormSection.button",
                 form.buttonStyle);
     }
     public void test_constructor_explicitButtonStyle() {
         FormSection form = new FormSection(
                 new Dataset("id", "form1", "buttonStyle", "explicit"));
         assertEquals("buttonStyle", "explicit", form.buttonStyle);
     }
 
     public void test_collectFormData_noErrors() {
         FormSection form = new FormSection(
                 new Dataset("id", "form1", "request", "getPerson"),
                 new EntryFormElement("name", "Name:"),
                 new EntryFormElement("age", "Age:"));
         Dataset main = cr.getMainDataset();
         main.set("name", "Alice");
         main.set("age", "21");
         Dataset result = form.collectFormData(cr);
         assertEquals("returned dataset", "age:  21\n" +
                 "name: Alice\n",
                 result.toString());
     }
     public void test_collectFormData_validationErrors() {
         // The following class generates an exception in its collect method.
         class ErrorFormElement extends FormElement{
             String message = null;
             public ErrorFormElement(String id, String message) {
                 super(new Dataset("id", id));
                 this.message = message;
             }
             public void collect(ClientRequest cr, Dataset in, Dataset out)
                     throws FormSection.FormDataException {
                 throw new FormSection.FormDataException(message);
             }
             public void html(ClientRequest cr, Dataset data,
                     StringBuilder out) {}
         }
 
         FormSection form = new FormSection(
                 new Dataset("id", "form1", "request", "getPerson"),
                 new ErrorFormElement("name", "name error"),
                 new ErrorFormElement("age", "age error"));
         cr.setClientRequestType(ClientRequest.Type.POST);
         boolean gotException = false;
         try {
             form.collectFormData(cr);
         }
         catch (FormSection.PostError e) {
             gotException = true;
         assertEquals("Error datasets", "culprit: age\n" +
                 "message: age error\n",
                 StringUtil.join(e.getErrorData(), "\n"));
         }
         assertEquals("exception happened", true, gotException);
         assertEquals("javascript response",
                 "Fiz.clearBulletin();\n" +
                 "Fiz.addBulletinMessage(\"bulletinError\", \"bulletin: " +
                 "One or more of the input fields are invalid; " +
                 "see details below.\");\n" +
                 "Fiz.ids.form1.clearElementErrors();\n" +
                 "Fiz.ids.form1.elementError(\"form1_name\", " +
                 "\"<span class=\\\"error\\\">name error</span>\");\n" +
                 "Fiz.ids.form1.elementError(\"form1_age\", " +
                 "\"<span class=\\\"error\\\">age error</span>\");\n",
                 cr.jsCode.toString());
     }
 
     public void test_elementError_basics() {
         FormSection form = new FormSection(
                 new Dataset("id", "form1", "elementErrorStyle", "style22"),
                 new EntryFormElement("name", "Name:"),
                 new EntryFormElement("age", "Age:"));
         cr.setClientRequestType(ClientRequest.Type.POST);
         // Make sure that the template references data in the main dataset,
         // to verify that it is available.
         Config.setDataset("styles", new Dataset(
                 "style22", "error for @name: @message",
                 "bulletin", "bulletin: @message"));
         DataManagerFixture.setErrorData(new Dataset("message", "<failure>",
                 "culprit", "age"));
         Dataset main = cr.getMainDataset();
         main.set("name", "Alice");
         main.set("age", "21");
         form.elementError(cr, new Dataset("message", "<failure>",
                 "culprit", "age"), "id11");
         assertEquals("javascript response",
                 "Fiz.clearBulletin();\n" +
                 "Fiz.addBulletinMessage(\"bulletinError\", \"bulletin: " +
                 "One or more of the input fields are invalid; see " +
                 "details below.\");\n" +
                 "Fiz.ids.form1.clearElementErrors();\n" +
                 "Fiz.ids.form1.elementError(\"form1_id11\", \"error for " +
                 "Alice: &lt;failure&gt;\");\n",
                 cr.jsCode.toString());
     }
     public void test_elementError_notFirstError() {
         FormSection form = new FormSection(
                 new Dataset("id", "form1", "elementErrorStyle", "style22"),
                 new EntryFormElement("name", "Name:"),
                 new EntryFormElement("age", "Age:"));
         form.anyElementErrors = true;
         cr.setClientRequestType(ClientRequest.Type.POST);
         // Make sure that the template references data in the main dataset,
         // to verify that it is available.
         Config.setDataset("styles", new Dataset(
                 "style22", "error for @name: @message",
                 "bulletin", "bulletin: @message"));
         DataManagerFixture.setErrorData(new Dataset("message", "<failure>",
                 "culprit", "age"));
         Dataset main = cr.getMainDataset();
         main.set("name", "Alice");
         main.set("age", "21");
         form.elementError(cr, new Dataset("message", "<failure>",
                 "culprit", "age"), "id11");
         assertEquals("javascript response",
                 "Fiz.ids.form1.elementError(\"form1_id11\", \"error " +
                 "for Alice: &lt;failure&gt;\");\n",
                 cr.jsCode.toString());
     }
     public void test_elementError_defaultStyle() {
         FormSection form = new FormSection(
                 new Dataset("id", "form1"),
                 new EntryFormElement("name", "Name:"),
                 new EntryFormElement("age", "Age:"));
         form.anyElementErrors = true;
         cr.setClientRequestType(ClientRequest.Type.POST);
         Config.setDataset("styles", new Dataset("FormSection",
                 new Dataset("elementError", "default style: @message")));
         form.elementError(cr, new Dataset("message", "<failure>",
                 "culprit", "age"), "id11");
         assertEquals("javascript response",
                 "Fiz.ids.form1.elementError(\"form1_id11\", " +
                 "\"default style: &lt;failure&gt;\");\n",
                 cr.jsCode.toString());
     }
 
     public void test_html_requestErrorDefaultStyle() {
         Config.setDataset("styles", new Dataset("FormSection",
                 new Dataset("error", "error from @name: @message")));
         FormSection form = new FormSection(
                 new Dataset("id", "form1", "request", "error"));
         form.registerRequests(cr);
         form.html(cr);
         assertEquals("generated HTML", "\n" +
                 "<!-- Start FormSection form1 -->\n" +
                 "error from Alice: sample &lt;error&gt;\n" +
                 "<!-- End FormSection form1 -->\n",
                 cr.getHtml().getBody().toString());
     }
     public void test_html_requestErrorExplicitStyle() {
         Config.setDataset("styles", new Dataset(
                 "custom", "custom message: @message"));
         FormSection form = new FormSection(new Dataset(
                 "id", "form1", "request", "error", "errorStyle", "custom"));
         form.registerRequests(cr);
         form.html(cr);
         assertEquals("generated HTML", "\n" +
                 "<!-- Start FormSection form1 -->\n" +
                 "custom message: sample &lt;error&gt;\n" +
                 "<!-- End FormSection form1 -->\n",
                 cr.getHtml().getBody().toString());
     }
     public void test_html_lookForNestedData() {
         FormSection form = new FormSection(
                 new Dataset("id", "form1", "request", "getPerson",
                         "postUrl", "x/y"),
                 new EntryFormElement("name", "Name:"));
         cr.showSections(form);
         TestUtil.assertMatchingSubstring("form element displaying name",
                 "<input type=\"text\" name=\"name\" " +
                 "class=\"EntryFormElement\" value=\"David\" />",
                 cr.getHtml().getBody().toString(), "<input[^>]*text[^>]*>");
     }
     public void test_html_dataNotNested() {
         FormSection form = new FormSection(
                 new Dataset("id", "form1", "request", "getState",
                         "postUrl", "x/y"),
                 new EntryFormElement("name", "Name:"));
         cr.showSections(form);
         TestUtil.assertMatchingSubstring("form element displaying name",
                 "<input type=\"text\" name=\"name\" " +
                 "class=\"EntryFormElement\" value=\"California\" />",
                 cr.getHtml().getBody().toString(), "<input[^>]*text[^>]*>");
     }
     public void test_html_noRequestButInitialValues() {
         // In this case, the "name" value is supplied from a property.
         FormSection form = new FormSection(
                 YamlDataset.newStringInstance(
                     "id: form1\n" +
                     "postUrl: /x/y\n" +
                     "initialValues:\n" +
                     "    name: Fred\n"),
                 new EntryFormElement("name", "Name:"));
         cr.showSections(form);
         TestUtil.assertMatchingSubstring("form element displaying name",
                 "<input type=\"text\" name=\"name\" " +
                 "class=\"EntryFormElement\" value=\"Fred\" />",
                 cr.getHtml().getBody().toString(), "<input[^>]*text[^>]*>");
     }
     public void test_html_noRequestOrInitialValues() {
         // In this case, the "name" value is supplied from the main dataset.
         FormSection form = new FormSection(
                 new Dataset("id", "form1", "postUrl", "x/y"),
                 new EntryFormElement("name", "Name:"));
         cr.showSections(form);
         TestUtil.assertMatchingSubstring("form element displaying name",
                 "<input type=\"text\" name=\"name\" " +
                 "class=\"EntryFormElement\" value=\"Alice\" />",
                 cr.getHtml().getBody().toString(), "<input[^>]*text[^>]*>");
     }
     public void test_html_defaultCssFile() {
         FormSection form = new FormSection(
                 new Dataset("id", "form1", "request", "getPerson",
                         "postUrl", "x/y"),
                 new EntryFormElement("name", "Name:"));
         cr.showSections(form);
         assertEquals("CSS files", "EntryFormElement.css, FormSection.css",
                 cr.getHtml().getCssFiles());
     }
     public void test_html_noCssFile() {
         FormSection form = new FormSection(
                 new Dataset("id", "form1", "class", "class22",
                         "postUrl", "x/y"),
                 new EntryFormElement("name", "Name:"));
         cr.showSections(form);
         assertEquals("CSS files", "EntryFormElement.css",
                 cr.getHtml().getCssFiles());
     }
     public void test_html_boilerplate() {
         FormSection form = new FormSection(
                 new Dataset("id", "form1", "class", "class22",
                         "postUrl", "x/y"));
         cr.showSections(form);
         TestUtil.assertMatchingSubstring("form HTML", "\n" +
                 "<!-- Start FormSection form1 -->\n" +
                 "<div id=\"form1_target\" style=\"display:none;\"></div>\n" +
                 "<form id=\"form1\" class=\"class22\" " +
                 "onsubmit=\"return Fiz.ids.form1.submit();\" " +
                 "action=\"x/y\" method=\"post\" " +
                 "enctype=\"multipart/form-data\">\n" +
                 "  <input type=\"hidden\" name=\"fiz_formId\" " +
                 "value=\"form1\" />\n" +
                 "  <table",
                 cr.getHtml().getBody().toString(), ".*<table");
     }
     public void test_html_boilerplateNoClassOrUrl() {
         FormSection form = new FormSection(
                 new Dataset("id", "form1"));
         cr.showSections(form);
         TestUtil.assertMatchingSubstring("form HTML", "\n" +
                 "<!-- Start FormSection form1 -->\n" +
                 "<div id=\"form1_target\" style=\"display:none;\"></div>\n" +
                 "<form id=\"form1\" class=\"FormSection\" " +
                 "onsubmit=\"return Fiz.ids.form1.submit();\" " +
                 "action=\"post\" method=\"post\" " +
                 "enctype=\"multipart/form-data\">\n" +
                 "  <input type=\"hidden\" name=\"fiz_formId\" " +
                 "value=\"form1\" />\n" +
                 "  <table",
                 cr.getHtml().getBody().toString(), ".*<table");
     }
     public void test_html_javascript() {
         FormSection form = new FormSection(
                 new Dataset("id", "form1", "request", "getPerson",
                 "buttonStyle", "", "postUrl", "x/y"),
                 new EntryFormElement("name", "Name:"));
         cr.showSections(form);
         assertEquals("Javascript code",
                 "Fiz.ids.form1 = new Fiz.FormSection(\"form1\");\n",
                 cr.getHtml().jsCode.toString());
         assertEquals("Javascript files",
                 "fizlib/Ajax.js, fizlib/Fiz.js, fizlib/FormSection.js",
                 cr.getHtml().getJsFiles());
     }
 
     public void test_post_collectDataAndReturnResponse() {
         FormSection form = new FormSection(
                 new Dataset("id", "form1", "request", "getPerson"),
                 new EntryFormElement("name", "Name:"),
                 new EntryFormElement("age", "Age:"));
         DataManagerFixture.responseData = new Dataset("response", "4567");
         Dataset main = cr.getMainDataset();
         main.set("name", "Alice");
         main.set("age", "21");
         Dataset result = form.post(cr, "fixture1");
         assertEquals("request dataset",
                 "id:      fixture1\n" +
                 "manager: fixture\n" +
                 "record:\n" +
                 "    age:  21\n" +
                 "    name: Alice\n",
                 DataManagerFixture.requestData.toString());
         assertEquals("returned dataset", "response: 4567\n",
                 result.toString());
     }
     public void test_post_errorWithCulprits() {
         FormSection form = new FormSection(
                 new Dataset("id", "form1"),
                 new EntryFormElement("name", "Name:"),
                 new EntryFormElement("age", "Age:"));
         cr.setClientRequestType(ClientRequest.Type.POST);
         Config.setDataset("styles", new Dataset("FormSection",
                 new Dataset("elementError", "element: @message"),
                 "bulletin", "bulletin: @message"));
         DataManagerFixture.setErrorData(new Dataset("message", "<failure>",
                 "culprit", "age"), new Dataset("message", "error33",
                 "culprit", "name"));
         Dataset main = cr.getMainDataset();
         main.set("name", "Alice");
         main.set("age", "21");
         boolean gotException = false;
         try {
             form.post(cr, "fixture1");
         }
         catch (FormSection.PostError e) {
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
         assertEquals("javascript response",
                 "Fiz.ids.form1.clearElementErrors();\n" +
                 "Fiz.clearBulletin();\n" +
                 "Fiz.addBulletinMessage(\"bulletinError\", \"bulletin: " +
                 "One or more of the input fields are invalid; see " +
                 "details below.\");\n" +
                 "Fiz.ids.form1.elementError(\"form1_age\", " +
                 "\"element: &lt;failure&gt;\");\n" +
                 "Fiz.ids.form1.elementError(\"form1_name\", " +
                 "\"element: error33\");\n",
                 cr.jsCode.toString());
     }
     public void test_post_errorButCulpritNotFound() {
         FormSection form = new FormSection(
                 new Dataset("id", "form1"),
                 new EntryFormElement("name", "Name:"),
                 new EntryFormElement("age", "Age:"));
         cr.setClientRequestType(ClientRequest.Type.POST);
         DataManagerFixture.setErrorData(new Dataset("message", "<failure>",
                 "culprit", "height"));
         boolean gotException = false;
         try {
             form.post(cr, "fixture1");
         }
         catch (FormSection.PostError e) {
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
         assertEquals("javascript response",
                 "Fiz.ids.form1.clearElementErrors();\n" +
                 "Fiz.clearBulletin();\n" +
                 "Fiz.addBulletinMessage(\"bulletinError\", " +
                 "\"bulletin: &lt;failure&gt;\");\n",
                 cr.jsCode.toString());
     }
     public void test_post_errorWithNoCulpritValue() {
         FormSection form = new FormSection(
                 new Dataset("id", "form1", "request", "getPerson"),
                 new EntryFormElement("name", "Name:"),
                 new EntryFormElement("age", "Age:"));
         cr.setClientRequestType(ClientRequest.Type.POST);
         DataManagerFixture.setErrorData(new Dataset("message", "<failure>"));
         Dataset main = cr.getMainDataset();
         main.set("name", "Alice");
         main.set("age", "21");
         boolean gotException = false;
         try {
             form.post(cr, "fixture1");
         }
         catch (FormSection.PostError e) {
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
         assertEquals("javascript response",
                 "Fiz.ids.form1.clearElementErrors();\n" +
                 "Fiz.clearBulletin();\n" +
                 "Fiz.addBulletinMessage(\"bulletinError\", " +
                 "\"bulletin: &lt;failure&gt;\");\n",
                 cr.jsCode.toString());
     }
 
     public void test_registerRequests() {
         Config.setDataset("dataRequests", YamlDataset.newStringInstance(
                 "getPerson:\n" +
                 "  manager: fixture\n" +
                 "getPerson_id1:\n" +
                 "  manager: fixture\n" +
                 "getPerson_id2:\n" +
                 "  manager: fixture\n"));
         FormSection form = new FormSection(
                 new Dataset("id", "form1", "request", "getPerson"),
                 new FormElementFixture("id1"),
                 new FormElementFixture("id2"));
         form.registerRequests(cr);
         assertEquals("registered requests",
                 "getPerson, getPerson_id1, getPerson_id2",
                 cr.getRequestNames());
     }
     public void test_registerRequests_noRequest() {
         FormSection form = new FormSection(
                 new Dataset("id", "form1"));
         form.registerRequests(cr);
         assertEquals("registered requests", "", cr.getRequestNames());
     }
     public void test_registerRequests_requestDataset() {
         FormSection form = new FormSection(
                 YamlDataset.newStringInstance(
                 "id: form1\n" +
                 "request:\n" +
                 "  manager: test14\n" +
                 "  arg1: 45\n"));
         form.registerRequests(cr);
         assertEquals("count of registered requests", 1,
                 cr.unnamedRequests.size());
         assertEquals("contents of request", "arg1:    45\n" +
                 "manager: test14\n",
                 cr.unnamedRequests.get(0).getRequestData().toString());
     }
 
     public void test_clearOldElementErrors() {
         FormSection form = new FormSection(
                 new Dataset("id", "form1"));
         form.oldElementErrorsCleared = false;
         cr.setClientRequestType(ClientRequest.Type.POST);
         form.clearOldElementErrors(cr);
         assertEquals("javascript response",
                 "Fiz.ids.form1.clearElementErrors();\n",
                 cr.jsCode.toString());
         assertEquals("oldElementErrorsCleared value", true,
                 form.oldElementErrorsCleared);
     }
     public void test_clearOldElementErrors_alreadyCleared() {
         FormSection form = new FormSection(
                 new Dataset("id", "form1"));
         form.oldElementErrorsCleared = true;
         cr.setClientRequestType(ClientRequest.Type.POST);
         form.clearOldElementErrors(cr);
         assertEquals("javascript response", null, cr.jsCode);
     }
 
     public void test_getHelpText() {
         FormElement element1 = new TemplateFormElement(new Dataset(
                 "id", "element1", "template", "text1", "help", "Help #1"));
         FormElement element2 = new TemplateFormElement(new Dataset(
                 "id", "element2", "template", "text2"));
         FormElement element3 = new TemplateFormElement(new Dataset(
                 "id", "element3", "template", "text3"));
         FormElement element4 = new TemplateFormElement(new Dataset(
                 "id", "element4", "template", "text4"));
         Config.setDataset("help", YamlDataset.newStringInstance(
                 "element1: help.element1\n" +
                 "element2: help.element2\n" +
                 "element3: help.element3\n" +
                 "form1:\n" +
                 "  element1: help.form1.element1\n" +
                 "  element2: help.form1.element2\n"));
         FormSection form = new FormSection(
                 new Dataset("id", "form1"), element1, element2, element3,
                 element4);
         assertEquals("help from form element", "Help #1",
                 form.getHelpText(element1));
         assertEquals("help from nested help dataset", "help.form1.element2",
                 form.getHelpText(element2));
         assertEquals("help from main help dataset", "help.element3",
                 form.getHelpText(element3));
         assertEquals("no help text available", null,
                 form.getHelpText(element4));
     }
 
     public void test_innerHtml_noSubmitButton() {
         FormSection form = new FormSection(
                 new Dataset("id", "form1", "request", "getPerson",
                 "buttonStyle", "", "postUrl", "x/y"), new TemplateFormElement(
                 new Dataset("id", "id11", "template", "xyz")));
         cr.showSections(form);
         TestUtil.assertMatchingSubstring("form contents",
                 "<table cellspacing=\"0\" class=\"sideBySide\">\n" +
                 "    <tr id=\"form1_id11\">\n" +
                 "      <td class=\"label\"></td>\n" +
                 "      <td class=\"control\">xyz<div " +
                 "id=\"form1_id11_diagnostic\" class=\"diagnostic\" " +
                 "style=\"display:none\"></div></td>\n" +
                 "    </tr>\n" +
                 "  </table>",
                 cr.getHtml().getBody().toString(), "<table.*</table>");
         TestUtil.assertXHTML( cr.getHtml().toString());
     }
     public void test_innerHtml_hiddenElements() {
         FormSection form = new FormSection(
                 new Dataset("id", "form1", "request", "getPerson",
                 "buttonStyle", "", "postUrl", "x/y"),
                 new TemplateFormElement(
                         new Dataset("id", "id2", "template", "element html")),
                 new HiddenFormElement("name"),
                 new HiddenFormElement("iq"));
         cr.showSections(form);
         TestUtil.assertMatchingSubstring("form contents",
                 "<table cellspacing=\"0\" class=\"sideBySide\">\n" +
                 "    <tr id=\"form1_id2\">\n" +
                 "      <td class=\"label\"></td>\n" +
                 "      <td class=\"control\">element html" +
                 "<div id=\"form1_id2_diagnostic\" class=\"diagnostic\" " +
                 "style=\"display:none\"></div></td>\n" +
                 "    </tr>\n" +
                 "  </table>\n" +
                 "  <input type=\"hidden\" name=\"name\" value=\"David\" />\n" +
                 "  <input type=\"hidden\" name=\"iq\" />\n" +
                 "</form>",
                 cr.getHtml().getBody().toString(), "<table.*</form>");
         TestUtil.assertXHTML( cr.getHtml().toString());
     }
     public void test_innerHtml_submitButton() {
         FormSection form = new FormSection(
                 new Dataset("id", "form1", "request", "getPerson",
                 "postUrl", "x/y"));
         cr.showSections(form);
         TestUtil.assertMatchingSubstring("form contents",
                 "<table cellspacing=\"0\" class=\"sideBySide\">\n" +
                 "    <tr>\n" +
                 "      <td class=\"submit\" colspan=\"2\"><div " +
                 "class=\"buttons\"><input type=\"submit\" name=\"action\" " +
                 "value=\"Submit\" accesskey=\"s\" " +
                 "title=\"Press Alt+Shift+s to submit.\" /></div> </td>\n" +
                 "    </tr>\n" +
                 "  </table>",
                 cr.getHtml().getBody().toString(), "<table.*</table>");
         TestUtil.assertXHTML(cr.getHtml().toString());
     }
 
     public void test_sideBySideElement_labelHtmlReturnsFalse() {
         TemplateFormElement element1 = new TemplateFormElement(
                 new Dataset("id", "id1", "template", "element 1 html",
                 "span", "true"));
         TemplateFormElement element2 = new TemplateFormElement(
                 new Dataset("id", "id2", "template", "element 2 html"));
         FormSection form = new FormSection(
                 new Dataset("id", "form1", "request", "getPerson",
                 "buttonStyle", "", "postUrl", "x/y"), element1, element2);
         cr.showSections(form);
         TestUtil.assertMatchingSubstring("form contents",
                 "<table cellspacing=\"0\" class=\"sideBySide\">\n" +
                 "    <tr id=\"form1_id1\">\n" +
                 "      <td class=\"control\" colspan=\"2\">element 1 html" +
                 "<div id=\"form1_id1_diagnostic\" class=\"diagnostic\" " +
                 "style=\"display:none\"></div></td>\n" +
                 "    </tr>\n" +
                 "    <tr id=\"form1_id2\">\n" +
                 "      <td class=\"label\"></td>\n" +
                 "      <td class=\"control\">element 2 html" +
                 "<div id=\"form1_id2_diagnostic\" class=\"diagnostic\" " +
                 "style=\"display:none\"></div></td>\n" +
                 "    </tr>\n" +
                 "  </table>",
                 cr.getHtml().getBody().toString(), "<table.*</table>");
         TestUtil.assertXHTML(cr.getHtml().toString());
     }
     public void test_sideBySideElement_helpText() {
         TemplateFormElement element1 = new TemplateFormElement(
                 new Dataset("id", "id1", "template", "element 1 html",
                 "help", "Sample help text"));
         TemplateFormElement element2 = new TemplateFormElement(
                 new Dataset("id", "id2", "template", "element 2 html"));
         FormSection form = new FormSection(
                 new Dataset("id", "form1", "request", "getPerson",
                 "buttonStyle", "", "postUrl", "x/y"), element1, element2);
         cr.showSections(form);
         TestUtil.assertMatchingSubstring("form contents",
                 "<table cellspacing=\"0\" class=\"sideBySide\">\n" +
                 "    <tr id=\"form1_id1\" title=\"Sample help text\">\n" +
                 "      <td class=\"label\"></td>\n" +
                 "      <td class=\"control\">element 1 html" +
                 "<div id=\"form1_id1_diagnostic\" class=\"diagnostic\" " +
                 "style=\"display:none\"></div></td>\n" +
                 "    </tr>\n" +
                 "    <tr id=\"form1_id2\">\n" +
                 "      <td class=\"label\"></td>\n" +
                 "      <td class=\"control\">element 2 html" +
                 "<div id=\"form1_id2_diagnostic\" class=\"diagnostic\" " +
                 "style=\"display:none\"></div></td>\n" +
                 "    </tr>\n" +
                 "  </table>",
                 cr.getHtml().getBody().toString(), "<table.*</table>");
         TestUtil.assertXHTML(cr.getHtml().toString());
     }
     public void test_sideBySideElement_elements() {
         FormSection form = new FormSection(
                 new Dataset("id", "form1", "request", "getPerson",
                         "buttonStyle", "", "postUrl", "x/y"),
                 new EntryFormElement("name", "Name:"),
                 new EntryFormElement("age", "Age:"));
         cr.showSections(form);
         TestUtil.assertMatchingSubstring("form contents",
                 "<table cellspacing=\"0\" class=\"sideBySide\">\n" +
                 "    <tr id=\"form1_name\">\n" +
                 "      <td class=\"label\">Name:</td>\n" +
                 "      <td class=\"control\"><input type=\"text\" " +
                 "name=\"name\" class=\"EntryFormElement\" " +
                 "value=\"David\" />" +
                 "<div id=\"form1_name_diagnostic\" class=\"diagnostic\" " +
                 "style=\"display:none\"></div></td>\n" +
                 "    </tr>\n" +
                 "    <tr id=\"form1_age\">\n" +
                 "      <td class=\"label\">Age:</td>\n" +
                 "      <td class=\"control\"><input type=\"text\" " +
                 "name=\"age\" class=\"EntryFormElement\" " +
                 "value=\"66\" /><div id=\"form1_age_diagnostic\" " +
                 "class=\"diagnostic\" style=\"display:none\"></div></td>\n" +
                 "    </tr>\n" +
                 "  </table>",
                 cr.getHtml().getBody().toString(), "<table.*</table>");
         TestUtil.assertXHTML(cr.getHtml().toString());
     }
 
     public void test_verticalElement_labelHtmlReturnsFalse() {
         TemplateFormElement element1 = new TemplateFormElement(
                 new Dataset("id", "id1", "template", "element 1 html",
                 "span", "true"));
         TemplateFormElement element2 = new TemplateFormElement(
                 new Dataset("id", "id2", "template", "element 2 html",
                 "label", "sample"));
         FormSection form = new FormSection(
                 new Dataset("id", "form1", "request", "getPerson",
                 "buttonStyle", "", "layout", "vertical", "postUrl", "x/y"),
                 element1, element2);
         cr.showSections(form);
         TestUtil.assertMatchingSubstring("form contents",
                 "<table cellspacing=\"0\" class=\"vertical\">\n" +
                 "    <tr id=\"form1_id1\"><td>\n" +
                 "      <div class=\"control\">element 1 html</div>\n" +
                 "      <div id=\"form1_id1_diagnostic\" class=\"diagnostic\" " +
                 "style=\"display:none\"></div>\n" +
                 "    </td></tr>\n" +
                 "    <tr id=\"form1_id2\"><td>\n" +
                 "      <div class=\"label\">sample</div>\n" +
                 "      <div class=\"control\">element 2 html</div>\n" +
                 "      <div id=\"form1_id2_diagnostic\" class=\"diagnostic\" " +
                 "style=\"display:none\"></div>\n" +
                 "    </td></tr>\n" +
                 "  </table>",
                 cr.getHtml().getBody().toString(), "<table.*</table>");
         TestUtil.assertXHTML(cr.getHtml().toString());
     }
     public void test_verticalElement_helpText() {
         TemplateFormElement element1 = new TemplateFormElement(
                 new Dataset("id", "id1", "template", "element 1 html",
                 "help", "Sample help text"));
         TemplateFormElement element2 = new TemplateFormElement(
                 new Dataset("id", "id2", "template", "element 2 html"));
         FormSection form = new FormSection(
                 new Dataset("id", "form1", "request", "getPerson",
                 "buttonStyle", "", "layout", "vertical", "postUrl", "x/y"),
                 element1, element2);
         cr.showSections(form);
         TestUtil.assertMatchingSubstring("form contents",
                 "<table cellspacing=\"0\" class=\"vertical\">\n" +
                 "    <tr id=\"form1_id1\" title=\"Sample help text\"><td>\n" +
                 "      <div class=\"control\">element 1 html</div>\n" +
                 "      <div id=\"form1_id1_diagnostic\" class=\"diagnostic\" " +
                 "style=\"display:none\"></div>\n" +
                 "    </td></tr>\n" +
                 "    <tr id=\"form1_id2\"><td>\n" +
                 "      <div class=\"control\">element 2 html</div>\n" +
                 "      <div id=\"form1_id2_diagnostic\" class=\"diagnostic\" " +
                 "style=\"display:none\"></div>\n" +
                 "    </td></tr>\n" +
                 "  </table>",
                 cr.getHtml().getBody().toString(), "<table.*</table>");
         TestUtil.assertXHTML(cr.getHtml().toString());
     }
     public void test_verticalElement_elements() {
         FormSection form = new FormSection(
                 new Dataset("id", "form1", "request", "getPerson",
                 "buttonStyle", "", "layout", "vertical", "postUrl", "x/y"),
                 new EntryFormElement("name", "Name:"),
                 new EntryFormElement("age", "Age:"));
         cr.showSections(form);
         TestUtil.assertMatchingSubstring("form contents",
                 "<table cellspacing=\"0\" class=\"vertical\">\n" +
                 "    <tr id=\"form1_name\"><td>\n" +
                 "      <div class=\"label\">Name:</div>\n" +
                 "      <div class=\"control\"><input type=\"text\" " +
                 "name=\"name\" class=\"EntryFormElement\" " +
                 "value=\"David\" /></div>\n" +
                 "      <div id=\"form1_name_diagnostic\" " +
                 "class=\"diagnostic\" style=\"display:none\"></div>\n" +
                 "    </td></tr>\n" +
                 "    <tr id=\"form1_age\"><td>\n" +
                 "      <div class=\"label\">Age:</div>\n" +
                 "      <div class=\"control\"><input type=\"text\" " +
                 "name=\"age\" class=\"EntryFormElement\" " +
                 "value=\"66\" /></div>\n" +
                 "      <div id=\"form1_age_diagnostic\" " +
                 "class=\"diagnostic\" style=\"display:none\"></div>\n" +
                 "    </td></tr>\n" +
                 "  </table>",
                 cr.getHtml().getBody().toString(), "<table.*</table>");
         TestUtil.assertXHTML(cr.getHtml().toString());
     }
 
     public void test_sendFormResponse() {
         cr.getMainDataset().set("fiz_formId", "ab\"<$&");
         FormSection.sendFormResponse(cr, "x = \"abc<&>\";");
         assertEquals("form response Javascript",
                 "window.parent.Fiz.FormSection.handleResponse(" +
                 "\"ab\\\"<$&\", \"x = \\\"abc<&>\\\";\");\n",
                 cr.getHtml().jsCode.toString());
     }
 }
