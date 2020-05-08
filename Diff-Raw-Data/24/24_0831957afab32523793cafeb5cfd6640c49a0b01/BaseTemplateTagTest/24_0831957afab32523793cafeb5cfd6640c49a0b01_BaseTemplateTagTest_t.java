 /*
  * ====================================================================
  *
  * Frame2 Open Source License
  *
  * Copyright (c) 2004-2007 Megatome Technologies.  All rights
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
 package org.megatome.frame2.taglib.template.el;
 
 import org.apache.cactus.JspTestCase;
 import org.apache.cactus.WebResponse;
 import org.megatome.frame2.tagsupport.BaseFrame2Tag;
 
 public abstract class BaseTemplateTagTest extends JspTestCase {
 	String _testJspName;
 	String _testNegativeJspName;
 	String _expectedLiveJsp;
 	String _expectedNegativeLiveJsp;
 
 	static String ATTR_NAME_PRE = "test"; //$NON-NLS-1$
 	static String JSP_TEST_DIR = "/test/tags/template/"; //$NON-NLS-1$
 
 	public BaseTemplateTagTest(String theName) {
 		super(theName);
 	}
 
 	public abstract BaseFrame2Tag createTag();
 	
 
 	// Test out tag in a real live jsp.
 	public void testLiveJsp()
 		throws Exception {
 		this.pageContext.forward(JSP_TEST_DIR + this._testJspName);
 	}
 
 	public void endLiveJsp(WebResponse webResponse) throws Exception {
 		String actual = webResponse.getText().trim();
 
 		assertEquals(this._expectedLiveJsp, actual);
 
 	}
 	
 //	Test out tag in a real live jsp.
 	public void testNegativeLiveJsp() {
 		try {
 			this.pageContext.forward(JSP_TEST_DIR + this._testNegativeJspName);
			fail("Did not catch Expected Exception");
 		} catch (Exception e) {
			boolean failed = false;
			String failMessage = "Did not find expected error";
			Throwable t = e;
			while (!failed) {
				String message = t.getMessage();
				int idx = message.indexOf(this._expectedNegativeLiveJsp);
				if (idx == -1) {
					t = t.getCause();
					if (t == null) {
						failed = true;
						failMessage = "Exhausted stack trace looking for message";
					}
				} else {
					break;
				}
			}
			
			if (failed) {
				fail(failMessage);
			}
 		}
 	}
 
 }
