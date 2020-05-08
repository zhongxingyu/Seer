 /* 
  ** Copyright [2012] [Megam Systems]
  **
  ** Licensed under the Apache License, Version 2.0 (the "License");
  ** you may not use this file except in compliance with the License.
  ** You may obtain a copy of the License at
  **
  ** http://www.apache.org/licenses/LICENSE-2.0
  **
  ** Unless required by applicable law or agreed to in writing, software
  ** distributed under the License is distributed on an "AS IS" BASIS,
  ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ** See the License for the specific language governing permissions and
  ** limitations under the License.
  */
 package org.megam.chef;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.megam.chef.exception.BootStrapChefException;
import org.yaml.snakeyaml.constructor.Constructor;
 import org.yaml.snakeyaml.TypeDescription;
 import org.yaml.snakeyaml.Yaml;
 
 /**
  * @author rajthilak
  * 
  */
 public class AppYamlLoader {
 
 	AppYamlLoadedSetup loadedYaml;
 	private boolean notReady = false;
 	AppYaml appYaml;
 
 	AppYamlLoader(String yamlFilePath) throws BootStrapChefException {
 
 		load(yamlFilePath);
 	}
 
 	private void load(String yamlFilePath) throws BootStrapChefException {
 		try {
 			InputStream input = new FileInputStream(new File(yamlFilePath));			
 			Constructor constructor = new Constructor(AppYamlLoadedSetup.class);
 			TypeDescription appDescription = new TypeDescription(
 					AppYamlLoadedSetup.class);
 			constructor.addTypeDescription(appDescription);
 			Yaml yaml = new Yaml(constructor);			
 			loadedYaml = (AppYamlLoadedSetup) yaml.load(input);			
 			notReady = (loadedYaml == null) ? true : false;
 		} catch (FileNotFoundException fnfe) {
 			fnfe.printStackTrace();
 			throw new BootStrapChefException(fnfe);
 		} catch (IOException ioe) {
 			throw new BootStrapChefException(ioe);
 		}
 	}
 
 	/**
 	 * Based on the configure: flag in the app.yaml file, send back the AppYaml
 	 * 
 	 * @return
 	 */
 	public AppYaml currentYaml() {
 		//AppYaml current = null;
 		//System.out.println("111" + appYaml.getConfig());
 		// switch () {
 		// case			
		System.out.println(new AppYaml(loadedYaml.getMegamchef()));
 		return (new AppYaml(new AppYamlLoadedSetup().getDevelopment()));
 	}
 
 	boolean notReady() {
 		return notReady;
 	}
 }
 /*
  * public class AppYamlLoadedSetup {
  * 
  * //Map<String, String> development; // Map<String, String> production;
  * //Map<String, String> megam_chef;
  * 
  * public Map<String, String> getDevelopment() { return development; }
  * 
  * public void setDevelopment(Map<String, String> development) {
  * this.development = development; }
  * 
  * public Map<String, String> getProduction() { return production; }
  * 
  * public void setProduction(Map<String, String> production) { this.production =
  * production; }
  * 
  * //Map<String, String> getMegam_Chef() { // return megam_chef; //}
  * 
  * //void setMegam_Chef(Map<String, String> megam_chef) { // this.megam_chef =
  * megam_chef; //}
  */
 // }
 // }
 // write a toString method for debugging.
 
