 /*******************************************************************************
  *  Copyright 2007 Ketan Padegaonkar http://ketan.padegaonkar.name
  *  
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *  
  *      http://www.apache.org/licenses/LICENSE-2.0
  *  
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  ******************************************************************************/
 package net.sourceforge.jcctray.ui;
 
 import java.io.IOException;
 
 import net.sourceforge.jcctray.model.IJCCTraySettings;
 import net.sourceforge.jcctray.model.ISettingsConstants;
 
 import org.jmock.Mock;
 import org.jmock.MockObjectTestCase;
 
 /**
  * @author Ketan Padegaonkar
  */
 public class BrowserTest extends MockObjectTestCase {
 
 	protected String	calledLaunch;
 	protected String	calledExec;
 	private Mock		traySettingsMock;
 	private String		launchUrl;
 
 	protected void setUp() throws Exception {
 		traySettingsMock = mock(IJCCTraySettings.class);
 		launchUrl = "myUrl";
 	}
 
 	public void testOpensBrowserWhenThereIsNoBrowserSet() throws Exception {
 		traySettingsMock.expects(once()).method("get").with(eq(ISettingsConstants.BROWSER_PATH)).will(returnValue("someBrowserPath"));
 		new Browser((IJCCTraySettings) traySettingsMock.proxy()){
 			protected void launch(String url) {
 				calledLaunch = url;
 			}
 			protected void exec(String string) throws IOException {
 				calledExec = string;
 			}
 		}.open(launchUrl);
 		assertEquals(launchUrl, calledLaunch);
 		assertNull(calledExec);
 	}
 
 	public void testOpensBrowserWhenThereIsAnInvalidBrowserSet() throws Exception {
 		Mock traySettingsMock = mock(IJCCTraySettings.class);
 		String executablePath = "build.xml";
 		traySettingsMock.expects(once()).method("get").with(eq(ISettingsConstants.BROWSER_PATH)).will(returnValue(executablePath));
 		new Browser((IJCCTraySettings) traySettingsMock.proxy()){
 			protected void launch(String url) {
 				calledLaunch = url;
 			}
 			protected void exec(String string) throws IOException {
 				throw new IOException();
 			}
 		}.open(launchUrl);
 		assertEquals(launchUrl, calledLaunch);
 		assertNull(calledExec);
 	}
 
 	public void testOpensBrowserWhenThereABrowserSet() throws Exception {
 		Mock traySettingsMock = mock(IJCCTraySettings.class);
 		String executablePath = "build.xml";
 		traySettingsMock.expects(once()).method("get").with(eq(ISettingsConstants.BROWSER_PATH)).will(returnValue(executablePath));
 		new Browser((IJCCTraySettings) traySettingsMock.proxy()){
 			protected void launch(String url) {
 				launchUrl = url;
 			}
 			protected void exec(String string) throws IOException {
 				calledExec = string;
 			}
 		}.open(launchUrl);
 		assertNull(calledLaunch);
		assertEquals(executablePath + " " + launchUrl, calledExec);
 	}
 
 	protected void tearDown() throws Exception {
 		traySettingsMock.verify();
 	}
 
 }
