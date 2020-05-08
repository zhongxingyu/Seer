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
 		String urlSuffix = dbSettings.get("urlSuffix",null);
 		boolean disableSchemaUpdate = dbSettings.getBoolean("disableSchemaUpdate", false);
 		
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
 				boolean onlyHostname = !serverURL.startsWith("jdbc");
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
 				EODatabaseContext dc = null;
 				try {
 					ec.lock();
 					dc = EODatabaseContext.forceConnectionWithModel(model,cd,ec);
 					dc.lock();
 					success &= verifyConnection(dc, model,logger, url, disableSchemaUpdate);
 				} catch (Exception e) {
 					StringBuilder message = new StringBuilder("Model '");
 					message.append(model.name());
 					int len = message.length();
 					message.append("' could not connect to database");
 					if(url != null)
 						message.append('\n').append(url);
 					logger.log((noSettings)? WOLogLevel.INFO : WOLogLevel.WARNING,
 							message.toString(), e);
 					if(!disableSchemaUpdate 
 							&& e.getMessage().toLowerCase().startsWith("unknown database")
 							&& DataBaseUtility.createDatabaseForModel(model)) {
 						logger.log(WOLogLevel.INFO,
 								"Autocreated database for model " + model.name());
 						try {
 							dc = EODatabaseContext.forceConnectionWithModel(model,cd,ec);
 							dc.lock();
							if(verifyConnection(dc, model,logger, url, disableSchemaUpdate)) {
								continue;
							}
 						} catch (Exception e2) {
 							message.delete(len, message.length());
 							message.append("' also could not connect to autocreated database");
 							message.append('\n').append(url);
 							logger.log(WOLogLevel.WARNING, message.toString(), e2);
 						}
 					}
 					if(noSettings) {
						logger.config("Skipping model '" + model.name() + '\'');
						mg.removeModel(model);
 					} else {
						boolean retry = false;
 						if(url != null) {
 							String untagged = (currSettings==null)?null:
 								currSettings.get("untagged", null);
 							if(untagged != null) { // try connecting to untagged
 								url = url.replaceFirst(dbName, untagged);
 								cd.takeValueForKey(url, "URL");
 								message.delete(len, message.length());
 								try {
 									logger.info("Trying to connect to untagged database");
 									dc = EODatabaseContext.forceConnectionWithModel(model, cd, ec);
 									dc.lock();
									retry = verifyConnection(
 											dc, model,logger,url, disableSchemaUpdate);
 								} catch (Exception ex) {
 									message.append("' also could not connect to database");
 									message.append('\n').append(url);
 									logger.log(WOLogLevel.WARNING, message.toString(), ex);
 								}
 							}
 						}
						success &= retry;
 					}
 				} finally {
 					if(dc != null)
 						dc.unlock();
 					ec.unlock();
 				}
 			}
 		} // while (models.hasMoreElements())
 		if(tag != null && os != null) {
 			Object store = (success)?os: NSKeyValueCoding.NullValue;
 			if(coordinatorsByTag == null)
 				coordinatorsByTag = new NSMutableDictionary(store,tag);
 			else
 				coordinatorsByTag.takeValueForKey(store, tag);
 		}
 		ec.dispose();
 		return success;
 	}
 	
 	protected static boolean verifyConnection(EODatabaseContext dc, EOModel model,
 			Logger logger, String url, boolean noUpd) {
 		boolean success = true;
 		EOAdaptorChannel ac = dc.availableChannel().adaptorChannel();
 		NSDictionary modelInfo = model.userInfo();
 		if(modelInfo != null)
 			modelInfo = (NSDictionary)modelInfo.valueForKey("schemaVersion");
 		if(modelInfo != null) {
 			int modelNum = Integer.parseInt(modelInfo.valueForKey("number").toString());
 			NSDictionary schemaVersion = null;
 			StringBuilder buf = new StringBuilder(
 "SELECT VERSION_NUMBER, VERSION_TITLE FROM SCHEMA_VERSION WHERE MODEL_NAME = '");
 			buf.append(model.name()).append("' ORDER BY VERSION_NUMBER DESC;");
 			try {
 				EOSQLExpression expr = ac.adaptorContext().adaptor().
 						expressionFactory().expressionForString(buf.toString());
 				ac.evaluateExpression(expr);
 				NSArray descResults = ac.describeResults();
 				EOAttribute attr = (EOAttribute)descResults.objectAtIndex(0);
 				attr.setName("number");
 				attr = (EOAttribute)descResults.objectAtIndex(1);
 				attr.setName("title");
 				ac.setAttributesToFetch(descResults);
 				schemaVersion = ac.fetchRow();
 				if(ac.isFetchInProgress())
 					ac.cancelFetch();
 			} catch (Exception e) {
 				logger.log(WOLogLevel.INFO,
 						"Failed to fetch schema info for model " + model.name(),e);
 			}
 			buf.delete(0, buf.length());
 			buf.append("Model '").append(model.name()).append('\'');
 			int schemNum = (schemaVersion == null)?0:
 				((Number)schemaVersion.valueForKey("number")).intValue();
 			if(modelNum != schemNum) {
 				buf.append(" requires schema version ").append(modelNum);
 				buf.append('(').append(modelInfo.valueForKey("title")).append(')');
 				if(schemaVersion != null) {
 					buf.append(". found version ").append(schemNum).append('(');
 					buf.append(schemaVersion.valueForKey("title")).append(')');
 				}
 				if(noUpd || schemNum > modelNum) {
 					logger.severe(buf.toString());
 					success = false;
 				} else {
 					logger.info(buf.toString());
 					buf.delete(0, buf.length());
 					try {
 						DataBaseUtility.updateModel(model, schemNum, ac);
 						buf.append("Schema for model '").append(model.name()).append("' updated");
 						if(schemaVersion != null) {
 							buf.append(" from v").append(schemNum).append(' ').append('(');
 							buf.append(schemaVersion.valueForKey("title")).append(')');
 						}
 						buf.append(" to v").append(modelNum).append(' ').append('(').append(
 								modelInfo.valueForKey("title")).append(')');
 						logger.info(buf.toString());
 					} catch (Exception e) {
 						buf.append("Failed to update schema for model '");
 						buf.append(model.name()).append('\'');
 						logger.log(WOLogLevel.SEVERE,buf.toString(),e);
 						success = false;
 					}
 				}
 			}
 		} // check modelInfo
 		ac.closeChannel();
 		if(success) {
 			StringBuilder message = new StringBuilder("Model '");
 			message.append(model.name()).append("' connected to database");
 			if(modelInfo != null) {
 				message.append(", schema version: ");
 				message.append(modelInfo.valueForKey("title"));
 			}
 			if(url != null)
 				message.append('\n').append(url);
 			logger.config(message.toString());
 		}
 		return success;
 	}
 	
 	public static EOObjectStore objectStoreForTag(String tag) {
 		Object os = coordinatorsByTag.valueForKey(tag);
 		if(os == null) {
 			os = new EOObjectStoreCoordinator();
 			if(!makeConnections((EOObjectStore)os, tag))
 				return null;
 		} else if(os == NSKeyValueCoding.NullValue) {
 			return null;
 		}
 		return (EOObjectStore)os;
 	}
 }
