 // DataBaseConnector.java
 
 /*
  * Copyright (c) 2008, Gennady & Michael Kushnir
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without modification, are
  * permitted provided that the following conditions are met:
  * 
  * 	•	Redistributions of source code must retain the above copyright notice, this
  * 		list of conditions and the following disclaimer.
  * 	•	Redistributions in binary form must reproduce the above copyright notice,
  * 		this list of conditions and the following disclaimer in the documentation
  * 		and/or other materials provided with the distribution.
  * 	•	Neither the name of the RUJEL nor the names of its contributors may be used
  * 		to endorse or promote products derived from this software without specific 
  * 		prior written permission.
  * 		
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
  * SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
  * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
  * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
  * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package net.rujel.reusables;
 
 import com.webobjects.foundation.*;
 //import com.webobjects.eocontrol.*;
 import com.webobjects.eoaccess.*;
 import com.webobjects.eocontrol.EOEditingContext;
 import com.webobjects.eocontrol.EOObjectStore;
 import com.webobjects.eocontrol.EOObjectStoreCoordinator;
 
 import java.util.Enumeration;
 import java.util.logging.Logger;
 import net.rujel.reusables.WOLogLevel;
 
 public class DataBaseConnector {
 	
 	protected static final String[] keys = new String[] 
 	                                      {"username","password","driver","plugin"};
 	
 	protected static NSMutableDictionary coordinatorsByTag;
 	
 	protected static NSMutableDictionary connectionDictionaryFromSettings (
 			SettingsReader settings, NSMutableDictionary dict) {
 		if(dict == null)
 			dict = new NSMutableDictionary();
 		
 		for (int i = 0; i < keys.length; i++) {
 			String key = keys[i];
 			String value = settings.get(key, null);
 			if(value != null)
 				dict.takeValueForKey(value, key);
 		}
 		return dict;
 	}
 
 	public static boolean makeConnections() {
 		return makeConnections(null,null);
 	}
 	
 	protected static EOEntity prototypesEntity;
 	public static boolean makeConnections(EOObjectStore os, String tag) {
 		Logger logger = Logger.getLogger("rujel.dbConnection");
 		SettingsReader dbSettings = SettingsReader.settingsForPath("dbConnection", false);
 		if(dbSettings == null) {
 			logger.log(WOLogLevel.CONFIG,
 					"No database connection settings found. Using predefined connections.");
 			return false;
 		}
 		EOModelGroup mg = EOModelGroup.defaultGroup();
 		String prototypes = dbSettings.get("prototypes",null);
 		if(prototypes != null) {
 	        EOEntity jdbcPrototypesEntity = mg.entityNamed("EOJDBCPrototypes");
 	        if(prototypesEntity == null)
 	        	prototypesEntity = mg.entityNamed(prototypes);
 			if(prototypesEntity == null) {
 				NSDictionary plist = (NSDictionary)PlistReader.readPlist(prototypes, null);
 				if(plist != null) {
 					EOModel model = jdbcPrototypesEntity.model();
 					prototypesEntity = new EOEntity(plist,model);
 					model.addEntity(prototypesEntity);
 				}
 			}
 			if(prototypesEntity != null && !prototypesEntity.equals(jdbcPrototypesEntity)) {
 				logger.log(WOLogLevel.CONFIG,"Using prototypes from " + prototypes);
 				Enumeration enu = jdbcPrototypesEntity.attributes().objectEnumerator();
 				while (enu.hasMoreElements()) {
 					EOAttribute jdbcPrototype =  (EOAttribute)enu.nextElement();
 					String prototypesName = (String)jdbcPrototype.name();
 					EOAttribute dbPrototype = 
 						(EOAttribute)prototypesEntity.attributeNamed(prototypesName);
 					if (dbPrototype != null) {
 						jdbcPrototype.setDefinition(dbPrototype.definition());
 						jdbcPrototype.setExternalType(dbPrototype.externalType()); 
 						jdbcPrototype.setPrecision(dbPrototype.precision()); 
 						jdbcPrototype.setReadFormat(dbPrototype.readFormat()); 
 						jdbcPrototype.setScale(dbPrototype.scale()); 
 						jdbcPrototype.setUserInfo(dbPrototype.userInfo()); 
 						jdbcPrototype.setValueType(dbPrototype.valueType()); 
 						jdbcPrototype.setWidth(dbPrototype.width()); 
 						jdbcPrototype.setWriteFormat(dbPrototype.writeFormat());
 	                }
 				}
 			} else {
 				logger.log(WOLogLevel.WARNING,"Could not load prototypes " + prototypes);
 			}
 		}
 		Enumeration enu = mg.models().immutableClone().objectEnumerator();
 		
 		String serverURL = dbSettings.get("serverURL",null);
		boolean onlyHostname = !serverURL.startsWith("jdbc");
 		String urlSuffix = dbSettings.get("urlSuffix",null);
 		
 		boolean success = true;
 		NSMutableDictionary connDict = connectionDictionaryFromSettings(dbSettings, null);
 		EOEditingContext ec = (os != null)?new EOEditingContext(os):new EOEditingContext();
 		SettingsReader dbMapping = dbSettings.subreaderForPath("dbMapping", false);
 		while (enu.hasMoreElements()) {
 			EOModel model = (EOModel) enu.nextElement();
 			if(model.name().endsWith("Prototypes")) {
 				Enumeration ents = model.entityNames().immutableClone().objectEnumerator();
 				while (ents.hasMoreElements()) {
 					String entName = (String) ents.nextElement();
 					if(!entName.equals("EOJDBCPrototypes")) {
 						model.removeEntity(model.entityNamed(entName));
 					}
  				}
 				continue;
 			}
 			SettingsReader currSettings = dbSettings.subreaderForPath(model.name(), false);
 			boolean noSettings = (currSettings == null); 
 			if(!noSettings && currSettings.getBoolean("skip", false)) {
 				mg.removeModel(model);
 				logger.config("Skipping model '" + model.name() + '\'');
 				continue;
 			}
 			NSMutableDictionary cd = connDict.mutableClone();
 			String url = null;
 			String dbName = null;
 			if(currSettings != null) {
 				cd = connectionDictionaryFromSettings(currSettings, cd);
 				url = currSettings.get("URL", null);
 				if(url == null)
 					dbName = currSettings.get("dbName", null);
 				if(dbName != null && dbMapping != null) {
 					Object mapped = dbMapping.valueForKey(dbName);
 					if(mapped == null && tag != null)
 						mapped = dbMapping.valueForKey(String.format(dbName, tag));
 					if(mapped != null) {
 						if(mapped instanceof String) {
 							dbName = (String)mapped;
 							if(dbName.startsWith("jdbc")) {
 								url = dbName;
 								dbName = null;
 							}
 						} else if(mapped instanceof SettingsReader) {
 							cd = connectionDictionaryFromSettings((SettingsReader)mapped, cd);
 							url = ((SettingsReader)mapped).get("URL", null);
 							if(url == null)
 								dbName = ((SettingsReader)mapped).get("dbName", null);
 						}
 					}
 				}
 			}
 			if(url == null && serverURL != null) {
 				String urlFromModel = (String)model.connectionDictionary().valueForKey("URL");
 				if(dbName == null && onlyHostname) {
 					url = urlFromModel.replaceFirst("localhost", serverURL);
 					if(urlSuffix != null) {
 						int idx = url.indexOf('?');
 						if(idx > 0)
 							url = url.substring(0,idx);
 						url = url + urlSuffix;
 					}
 				} else {
 					int index = urlFromModel.indexOf("localhost");
 					StringBuffer buf = new StringBuffer(serverURL);
 					if (onlyHostname)
 						buf.insert(0, urlFromModel.substring(0, index));
 					if(buf.charAt(buf.length() -1) == '/')
 						buf.deleteCharAt(buf.length() -1);
 					if(dbName == null) {
 						int idx = urlFromModel.indexOf('?',index + 9);
 						if(idx > 0 && urlSuffix != null) {
 							buf.append(urlFromModel.substring(index + 9,idx));
 						} else {
 							buf.append(urlFromModel.substring(index + 9));
 						}
 					} else {
 						if(onlyHostname)
 							buf.append(urlFromModel.charAt(index + 9));
 						else {
 							char c = buf.charAt(buf.length() -1);
 							if((c>='a'&&c<='z')||(c>='A'&&c<='Z')||(c>='0'&&c<='9'))
 								buf.append('/');
 						}
 						if(tag != null)
 							dbName = String.format(dbName, tag);
 						buf.append(dbName);
 					}
 					if(urlSuffix != null)
 						buf.append(urlSuffix);
 					url = buf.toString();
 				}
 			} // if(url == null && serverURL != null)
 			if(url != null) {
 				cd.takeValueForKey(url, "URL");
 			}
 			if(cd.count() > 0) {
 				try {
 					ec.lock();
 					EODatabaseContext.forceConnectionWithModel(model, cd, ec);
 					String message = "Model '" + model.name() + "' connected to database";
 					if(url != null)
 						message = message + '\n' + url;
 					logger.config(message);
 				} catch (Exception e) {
 					String message = "Model '" + model.name() + 
 										"' could not connect to database";
 					if(url != null)
 						message = message + '\n' + url;
 					if(noSettings) {
 						logger.log(WOLogLevel.INFO, message);
 //						mg.removeModel(model);
 					} else {
 						logger.log(WOLogLevel.WARNING, message, e);
 						success = false;
 						if(url != null) {
 							String untagged = (currSettings==null)?null:
 								currSettings.get("untagged", null);
 							if(untagged != null) {
 								url = url.replaceFirst(dbName, untagged);
 								cd.takeValueForKey(url, "URL");
 								try {
 									EODatabaseContext.forceConnectionWithModel(model, cd, ec);
 									message = "Model '" + model.name() +
 											"' connected to untagged database" + '\n' + url;
 									logger.config(message);
 									success = true;
 								} catch (Exception ex) {
 									message = "Model '" + model.name() + 
 									"' also could not connect to database" + '\n' + url;
 									logger.log(WOLogLevel.WARNING, message, ex);
 								}
 							}
 						}
 					}
 				} finally {
 					ec.unlock();
 				}
 			}
 		} // while (models.hasMoreElements())
 		if(success && tag != null && os != null) {
 			if(coordinatorsByTag == null)
 				coordinatorsByTag = new NSMutableDictionary(os,tag);
 			else
 				coordinatorsByTag.takeValueForKey(os, tag);
 		}
 		ec.dispose();
 		return success;
 	}
 	
 	public static EOObjectStore objectStoreForTag(String tag) {
 		EOObjectStore os = (EOObjectStore)coordinatorsByTag.valueForKey(tag);
 		if(os == null) {
 			os = new EOObjectStoreCoordinator();
 			if(!makeConnections(os, tag))
 				return null;
 		}
 		return os;
 	}
 }
