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
 package net.sourceforge.jcctray.ui.settings;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.TabFolder;
 
import net.sourceforge.jcctray.ui.settings.internal.BuildProjectsSettingsTab;
import net.sourceforge.jcctray.ui.settings.internal.GeneralSettingsTab;

 public class SettingTabs {
 
 	public SettingTabs(Shell shell) {
 		createTabs(shell);
 	}
 
 	private void createTabs(Shell shell) {
 		TabFolder tabFolder = new TabFolder(shell, SWT.NONE);
 		new BuildProjectsSettingsTab(tabFolder);
		new GeneralSettingsTab(tabFolder);
 	}
 }
