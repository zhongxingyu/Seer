 package com.tonbeller.wcf.web;
 
 import java.io.IOException;
 
 import javax.xml.transform.TransformerException;
 
 import org.jaxen.JaxenException;
 import org.xml.sax.SAXException;
 
 import com.meterware.httpunit.GetMethodWebRequest;
 import com.meterware.httpunit.WebForm;
 import com.meterware.httpunit.WebLink;
 import com.meterware.httpunit.WebResponse;
 
 public class FormTest extends HttpUnitTestCase {
 
   public FormTest(String arg0) {
     super(arg0);
   }
 
   private void check(String name) throws JaxenException, IOException, SAXException, TransformerException {
     utils.check(name, "/com/tonbeller/wcf/web/form.xsl", "form01");
   }
 
   /**
    * BEA does not recognize the response charEncoding when including files,
    * so we tests &lt;c:import ../> here.
    */
   public void testCharEnc() throws Exception {
     wc.setHeaderField("accept-charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
     GetMethodWebRequest req = new GetMethodWebRequest(servletUrl + "/charencmain.jsp");
     wc.sendRequest(req);
     WebResponse wr = utils.submitButton("form01", "submit");
     check("form-charenc-01");
 
     // 2-ter test:
     // URL mit UTF-8 encodierten Zeichen, funktioniert im Browser (entgegen der Spec)
     // Funktioniert nicht mit httpunit. httpunit liefert unter Linux andere
     // Ergebnisse als unter Windows, das XML enthaelt non-utf-8 Zeichen.
     WebLink link = wr.getLinkWithID("a02");
     wr = link.click();
     // check("form-charenc-02");
   }
 
   public void testRequiredField() throws Exception {
     wc.sendRequest(new GetMethodWebRequest(servletUrl + "/multicolumn.jsp"));
    utils.submitButton("form01", "multicolumnForm.ok");
     check("form-required");
   }
 
   public void testMultiColumn() throws Exception {
     wc.sendRequest(new GetMethodWebRequest(servletUrl + "/multicolumn.jsp"));
     check("form-multicolumn");
   }
   /**
    * run form tests w/o faces servlet. This tests WCF EL and navigation
    */
   public void testWcfForm() throws Exception {
     wc.sendRequest(new GetMethodWebRequest(servletUrl + "/formdemo.jsp"));
     runTests();
   }
 
   /**
    * run form tests in faces context. This tests non-faces JSP thru faces servlet
    */
   public void testJsfForm() throws Exception {
     wc.sendRequest(new GetMethodWebRequest(servletUrl + "/formdemo.faces"));
     runTests();
   }
 
   private void runTests() throws Exception {
     check("form-01");
     WebForm wf = wc.getCurrentPage().getFormWithID("form01");
 
     // ********* test valid input ****************
     wf.setParameter("string", "----");
     wf.setParameter("int", "12");
     wf.setParameter("double", "123.456");
     wf.setParameter("date", "12/31/2003");
     wf.setParameter("password", "x");
     wf.setParameter("textarea", "some\nmulti\nline\ntext\n");
     wf.setParameter("checkbox1", "x");
     wf.setParameter("checkbox2", "x");
     wf.setParameter("list1", "list1.1");
     wf.setParameter("listN", new String[]{ "listN.1", "listN.4"});
     wf.setParameter("group1", "radio1");
     wf.submit(wf.getSubmitButtonWithID("formcomp.validate"));
     check("form-02");
 
     // ******** test invalid input *************
     wf = wc.getCurrentPage().getFormWithID("form01");
     wf.setParameter("int", "nan");
     wf.setParameter("double", "nan");
     wf.setParameter("date", "nad");
     wf.submit(wf.getSubmitButtonWithID("formcomp.ok"));
     check("form-03");
     wf.submit(wf.getSubmitButtonWithID("formcomp.revert"));
     check("form-04");
   }
 
 }
