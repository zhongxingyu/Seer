 /*******************************************************************************
  * Copyright 2013 See AUTHORS file.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package com.mangecailloux.pebble.language;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.utils.Array;
 import com.badlogic.gdx.utils.ObjectMap;
 
 public class LanguagesManager {       
     		
         public static String getSystemLanguage()
         {
         	return java.util.Locale.getDefault().toString();
         }
 	
         private final 	ObjectMap<String, String> 	languageMap;
         private 		String 						currentLanguage;
         private 		String 						filePath;
         private 		String 						defaultLanguage;
         private final	Array<LanguageListener>		listeners;
 
 		private final DocumentBuilderFactory 		dbf;
         private 	  DocumentBuilder 				db;
         
 
         
         public LanguagesManager() {
             // Create language map
         	languageMap = new ObjectMap<String, String>();
             currentLanguage = null;
             
             listeners = new Array<LanguageListener>(false, 1);
             
             dbf = DocumentBuilderFactory.newInstance();
             try {
 				db = dbf.newDocumentBuilder();
 			} catch (ParserConfigurationException e) {
 				Gdx.app.log("LanguagesManager", "News Document Error.");
 			}
         }
           
         public void init(String _path, String _defaultLanguage)
         {
             filePath = _path;
             defaultLanguage = _defaultLanguage;
         }  
         
         public String getCurrentLanguage() {
                 return currentLanguage;
         }
 
         public String getString(String _key) {
             if (languageMap != null) {
                     // Look for string in selected language
                     String string = languageMap.get(_key);
                     if (string != null)
                            return string;
             }
     
             // Key not found, return the key itself
             return _key;
         }
         
         public String getString(String _key, Object... _objects) {
            return String.format(getString(_key), _objects);
         }
         
         public void loadLanguage(String _languageName)
         {
             if (!_loadLanguage(_languageName))
                 _loadLanguage(defaultLanguage);
         }
         
         protected boolean _loadLanguage(String _languageName) 
         {
     		if(_languageName == null)
     			return false;
     		
     		if(_languageName.equals(currentLanguage))
     			return true;
     	
     		FileHandle fileHandle = Gdx.files.internal(filePath);
     		boolean success = false;
     		if(fileHandle != null && fileHandle.exists())
     		{
     			InputStream in = null;
     			try { 
     				 in = fileHandle.read();
 
                      Document doc;
                      doc = db.parse(in);
 
                      Element root = doc.getDocumentElement();
                      
                      NodeList languages = root.getElementsByTagName("language");
                      int numLanguages = languages.getLength();
                      
                      for (int i = 0; i < numLanguages; ++i)
                      {
                     	Node language = languages.item(i);    
                        if (language.getAttributes().getNamedItem("name").getTextContent().equals(_languageName)) 
                         {
                     	 	currentLanguage = _languageName;
                     	 	languageMap.clear();
                             Element languageElement = (Element)language;
                             NodeList strings = languageElement.getElementsByTagName("string");
                             int numStrings = strings.getLength();
                              
 							for (int j = 0; j < numStrings; ++j) 
 							{
 								NamedNodeMap attributes = strings.item(j).getAttributes();
 								String key = attributes.getNamedItem("key").getNodeValue();
 								String value = attributes.getNamedItem("value").getNodeValue();
 								value = value.replace("<br/>", "\n");
 								value = value.replace("\\n", "\n");
 								languageMap.put(key, value);
 							}
                          
 							for(int k=0; k < listeners.size; ++k)
 								listeners.get(k).onLoad(getCurrentLanguage());
 							
                     		success = true;
                         }
                    }   	
     			}
     			catch (IOException e) {
     				Gdx.app.log("LanguagesManager", "Cannot read internal file : "+ filePath +", load failed.");
     			} 
     			catch (SAXException e) {
     				Gdx.app.log("LanguagesManager", "Parse Error.");
 				}
     			finally
     			{
     				if (in != null) {
     					try {
     						in.close();
     					} catch (IOException e) {
     						Gdx.app.log("LanguagesManager", "Cannot close internal file : "+ filePath +", load failed.");
     					}
     				}
     			}
     		}
     		else if(fileHandle != null)
     		{
     			Gdx.app.log("LanguagesManager", "Internal file : "+ filePath +" doesn't exists.");
     		}
 	    		
 	    	return success;
         }
         
         public void addListener(LanguageListener _listener)
         {
         	if(_listener != null)
         		listeners.add(_listener);
         }
         
         public void removeListener(LanguageListener _listener)
         {
         	if(_listener != null)
         		listeners.removeValue(_listener, true);
         }
         
         public void clearListeners()
         {
         	listeners.clear();
         }
         
         public static interface LanguageListener
         {
         	public void onLoad(String _language);
         }
 }
