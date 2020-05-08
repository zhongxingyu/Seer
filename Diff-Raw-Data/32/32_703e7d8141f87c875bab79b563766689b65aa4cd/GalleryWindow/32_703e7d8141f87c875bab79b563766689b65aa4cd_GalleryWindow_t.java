 /*
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
  */
 package org.hobsoft.symmetry.demo.gallery;
 
 import org.hobsoft.symmetry.demo.gallery.tab.ButtonTab;
 import org.hobsoft.symmetry.demo.gallery.tab.InputTab;
 import org.hobsoft.symmetry.demo.gallery.tab.LabelTab;
 import org.hobsoft.symmetry.demo.gallery.tab.LayoutTab;
 import org.hobsoft.symmetry.demo.gallery.tab.TableTab;
 import org.hobsoft.symmetry.demo.gallery.tab.TreeTab;
 import org.hobsoft.symmetry.demo.gallery.tab.WizardTab;
 import org.hobsoft.symmetry.ui.Label;
 import org.hobsoft.symmetry.ui.TabBox;
 import org.hobsoft.symmetry.ui.Window;
 
 /**
  * The Symmetry Gallery demo.
  * 
  * @author Mark Hobson
  */
 public class GalleryWindow extends Window
 {
 	// constructors -----------------------------------------------------------
 	
 	public GalleryWindow()
 	{
 		setTitle("Symmetry Gallery");
 		
 		add(
 			new Label("Symmetry Gallery 0.1.0-SNAPSHOT"),
 			new TabBox(
 				new LabelTab(),
 				new ButtonTab(),
 				new InputTab(),
 				new LayoutTab(),
 				new TableTab(),
 				new TreeTab(),
 				new WizardTab()
 			)
 		);
 		
 		setVisible(true);
 	}
 }
