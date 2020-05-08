 /*
  * ====================================================================
  *
  * Frame2 Open Source License
  *
  * Copyright (c) 2004 Megatome Technologies.  All rights
  * reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in
  *    the documentation and/or other materials provided with the
  *    distribution.
  *
  * 3. The end-user documentation included with the redistribution, if
  *    any, must include the following acknowlegement:
  *       "This product includes software developed by
  *        Megatome Technologies."
  *    Alternately, this acknowlegement may appear in the software itself,
  *    if and wherever such third-party acknowlegements normally appear.
  *
  * 4. The names "The Frame2 Project", and "Frame2", 
  *    must not be used to endorse or promote products derived
  *    from this software without prior written permission. For written
  *    permission, please contact iamthechad@sourceforge.net.
  *
  * 5. Products derived from this software may not be called "Frame2"
  *    nor may "Frame2" appear in their names without prior written
  *    permission of Megatome Technologies.
  *
  * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED.  IN NO EVENT SHALL MEGATOME TECHNOLOGIES OR
  * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
  * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  * ====================================================================
  */
 package org.megatome.frame2.taglib.error;
 
 import java.util.Locale;
 
 import javax.servlet.jsp.PageContext;
 import javax.servlet.jsp.tagext.Tag;
 
 import org.apache.cactus.JspTestCase;
 import org.apache.cactus.WebResponse;
 import org.megatome.frame2.errors.Errors;
 import org.megatome.frame2.taglib.error.ErrorsTag;
 import org.megatome.frame2.util.ResourceLocator;
 
 /**
  * 
  */
 public class TestErrorsTag extends JspTestCase {
 
 	private ErrorsTag _tag;
    private String _realBundleName;
 
 	public TestErrorsTag(String name) {
 		super(name);
 	}
 
 	protected void setUp() {
 		_tag = new ErrorsTag();
 		_tag.setPageContext(pageContext);
       
       _realBundleName = ResourceLocator.getBasename();
 	}
    
    protected void tearDown() {
       ResourceLocator.setBasename(_realBundleName);
    }
 
 	public void testNoErrors() throws Exception {
 		assertEquals(Tag.EVAL_BODY_INCLUDE, _tag.doStartTag());
 	}
 
 	public void endNoErrors(WebResponse response) {
 		String output = response.getText();
 		assertEquals("", output);
 	}
 
    public void testEmptyErrors() throws Exception {
      pageContext.getRequest().setAttribute(org.megatome.frame2.Globals.ERRORS,Errors.newInstance());
       assertEquals(Tag.EVAL_BODY_INCLUDE, _tag.doStartTag());
    }
 
    public void endEmptyErrors(WebResponse response) {
       String output = response.getText();
       assertEquals("", output);
    }
 
    public void testOneError() throws Exception {
       ResourceLocator.setBasename("test-errors-simple");
 
      Errors errors = Errors.newInstance();
 
       errors.add("example.error.key.1","foo");
 
       pageContext.getRequest().setAttribute(org.megatome.frame2.Globals.ERRORS,errors);
       assertEquals(Tag.EVAL_BODY_INCLUDE, _tag.doStartTag());
    }
 
    public void endOneError(WebResponse response) {
       String output = response.getText();
       assertEquals("Example error message one foo\n", output);
    }
 
    public void testTwoErrors() throws Exception {
       ResourceLocator.setBasename("test-errors-simple");
 
      Errors errors = Errors.newInstance();
 
       errors.add("example.error.key.1","foo");
       errors.add("example.error.key.2","bar");
 
       pageContext.getRequest().setAttribute(org.megatome.frame2.Globals.ERRORS,errors);
       assertEquals(Tag.EVAL_BODY_INCLUDE, _tag.doStartTag());
    }
 
    public void endTwoErrors(WebResponse response) {
       String output = response.getText();
       assertEquals("Example error message one foo\nExample error message two bar\n", output);
    }
 
    public void testErrorKeyAttribute() throws Exception {
       ResourceLocator.setBasename("test-errors-simple");
 
       _tag.setErrorKey("example.error.key.2");
 
      Errors errors = Errors.newInstance();
 
       errors.add("example.error.key.1","foo");
       errors.add("example.error.key.2","bar");
 
       pageContext.getRequest().setAttribute(org.megatome.frame2.Globals.ERRORS,errors);
       assertEquals(Tag.EVAL_BODY_INCLUDE, _tag.doStartTag());
    }
 
    public void endErrorKeyAttribute(WebResponse response) {
       String output = response.getText();
       assertEquals("Example error message two bar\n", output);
    }
 
    public void testLocaleAttribute() throws Exception {
       String localeKey = "local.key";
 
       ResourceLocator.setBasename("test-errors-simple");
 
       _tag.setLocaleKey(localeKey);
       pageContext.setAttribute(localeKey,Locale.FRENCH,PageContext.SESSION_SCOPE);
 
      Errors errors = Errors.newInstance();
 
       errors.add("example.error.key.1","foo");
 
       pageContext.getRequest().setAttribute(org.megatome.frame2.Globals.ERRORS,errors);
       assertEquals(Tag.EVAL_BODY_INCLUDE, _tag.doStartTag());
    }
 
    public void endLocaleAttribute(WebResponse response) {
       String output = response.getText();
       assertEquals("Message d'erreur d'exemple un foo\n", output);
    }
 
    public void testDecoratedErrors() throws Exception {
       ResourceLocator.setBasename("test-errors-decorated");
 
      Errors errors = Errors.newInstance();
 
       errors.add("example.error.key.1","foo");
       errors.add("example.error.key.2","bar");
 
       pageContext.getRequest().setAttribute(org.megatome.frame2.Globals.ERRORS,errors);
       assertEquals(Tag.EVAL_BODY_INCLUDE, _tag.doStartTag());
    }
 
    public void endDecoratedErrors(WebResponse response) {
       final String RESULT = "Header Decorator\n" +
          "<DEC>Example error message one foo</DEC>\n" +
          "<DEC>Example error message two bar</DEC>\n" +
          "Footer Decorator\n";
 
       String output = response.getText();
       assertEquals(RESULT,output);
    }
 
 }
