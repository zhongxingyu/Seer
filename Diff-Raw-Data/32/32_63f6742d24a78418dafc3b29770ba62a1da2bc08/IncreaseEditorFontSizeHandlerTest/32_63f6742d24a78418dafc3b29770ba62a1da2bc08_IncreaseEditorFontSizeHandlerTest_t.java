 /*
 * Copyright 2011 Ingo Richter
 *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
  *     http://www.apache.org/licenses/LICENSE-2.0
 *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
 *
  */
 package de.richter.eclipse.fontresizer;
 
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.ui.internal.Workbench;
 import org.junit.Before;
 import org.junit.Test;
 
 import de.richter.eclipse.fontresizer.handlers.IncreaseEditorFontSizeHandler;
 
 public class IncreaseEditorFontSizeHandlerTest {
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@Test
	public void testExecute() throws org.eclipse.core.commands.ExecutionException {
 		IPreferenceStore preferenceStore = Workbench.getInstance().getPreferenceStore();
 		preferenceStore.addPropertyChangeListener(new IPropertyChangeListener() {
 			@Override
			public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) {
 				System.err.println("PCE Fired");
 				String property= event.getProperty();
 				System.err.println(property);
 			}
 		});

 		IncreaseEditorFontSizeHandler editorFontSizeHandler = new IncreaseEditorFontSizeHandler();
 		editorFontSizeHandler.execute(null);
 		// check if the font-size was increased by one
 		// either check the preferences directly or register a changelistener for the preferences
 	}
 }
