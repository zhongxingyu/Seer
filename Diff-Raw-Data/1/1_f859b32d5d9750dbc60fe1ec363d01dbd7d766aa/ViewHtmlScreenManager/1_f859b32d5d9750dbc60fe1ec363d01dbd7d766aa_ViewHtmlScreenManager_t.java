 /*
  * Copyright 2006 Giordano Maestro (giordano.maestro--at--assetdata.it)
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
  */
 package org.romaframework.aspect.view.html.screen;
 
 import org.romaframework.aspect.view.screen.Screen;
 import org.romaframework.aspect.view.screen.config.ScreenManager;
 import org.romaframework.core.schema.xmlannotations.XmlFormAreaAnnotation;
 
 public class ViewHtmlScreenManager extends ScreenManager {
 
 	public Screen createScreenFromDefaultFactory(final String name, final XmlFormAreaAnnotation areaTag, String defaultArea) {
 		final HtmlViewConfigurableScreen htmlViewConfigurableScreen = new HtmlViewConfigurableScreen();
 		htmlViewConfigurableScreen.setName(name);
 		htmlViewConfigurableScreen.configure(areaTag);
 		htmlViewConfigurableScreen.setDefautlArea(defaultArea);
 		return htmlViewConfigurableScreen;
 	}
 }
