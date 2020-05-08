 package com.mccraftaholics.warpportals.manager;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import com.mccraftaholics.warpportals.helpers.Utils;
 import com.mccraftaholics.warpportals.objects.CoordsPY;
 import com.mccraftaholics.warpportals.objects.PortalInfo;
 
 public class PersistanceManager {
 
 	Logger mLogger;
 	File mDataFile;
 
 	PersistanceManager(Logger logger, File file) {
 		mLogger = logger;
 		mDataFile = file;
 	}
 
 	public void loadDataFile(HashMap<String, PortalInfo> portalMap, HashMap<String, CoordsPY> destMap) {
 		loadDataFile(portalMap, destMap, mDataFile);
 	}
 
 	public void loadDataFile(HashMap<String, PortalInfo> portalMap, HashMap<String, CoordsPY> destMap, File dataFile) {
 		try {
 			String data = Utils.readFile(dataFile.getAbsolutePath(), Charset.forName("UTF-8"));
 			if (data != null && !data.matches("")) {
 				String[] initS = data.split("\n");
 				String[] groups = Utils.ymlLevelCleanup(initS, "  ");
 				for (String group : groups) {
 					if (group != null) {
 						if (group.trim().startsWith("#")) {
 						} else if (group.contains("portals:")) {
 							String[] eLine = group.split("\n");
 							String[] items = Utils.ymlLevelCleanup(eLine, "    ");
 							for (String item : items) {
 								if (item != null) {
 									if (!item.contains("portals:")) {
 										String[] attrs = item.split("\n");
 										String portalName = "";
 										PortalInfo portalInfo = new PortalInfo();
 										for (String attr : attrs) {
 											String attrT = attr.trim();
 											try {
 												if (attrT.contains("tpCoords"))
 													portalInfo.tpCoords = new CoordsPY(attrT.split(":")[1].trim());
 												else if (attrT.contains("blocks"))
 													portalInfo.parseBlockCoordArr(attrT.split(":")[1].trim());
 												else
 													portalName = attrT.replace(":", "").trim();
 											} catch (Exception e) {
 												mLogger.info("Error in Portal's data file with String \"" + attrT + "\".");
 											}
 										}
 										portalMap.put(portalName, portalInfo);
 									}
 								}
 							}
 						} else if (group.contains("destinations")) {
 							String[] dests = group.split("\n  ");
 							for (String dest : dests) {
 								if (!dest.contains("destinations")) {
 									String destt = dest.trim();
 									String[] destd = destt.split(":");
 									if (destd.length == 2) {
 										try {
 											destMap.put(destd[0].trim(), new CoordsPY(destd[1].trim()));
 										} catch (Exception e) {
 											// Error loading this Destination
 											// from
 											// Memory
 										}
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 			mLogger.info(String.valueOf(portalMap.size()) + " Portals loaded!");
 		} catch (IOException e) {
 			mLogger.severe("Can't load data from Portal's data file");
 		}
 	}
 
 	public boolean saveDataFile(HashMap<String, PortalInfo> portalMap, HashMap<String, CoordsPY> destMap) {
 		return saveDataFile(portalMap, destMap, mDataFile);
 	}
 
 	public boolean saveDataFile(HashMap<String, PortalInfo> portalMap, HashMap<String, CoordsPY> destMap, File dataFile) {
 		boolean rtn = true;
 		if (dataFile.canWrite()) {
 			FileWriter fw = null;
 			BufferedWriter bw = null;
 			try {
 				fw = new FileWriter(dataFile.getAbsoluteFile());
 				bw = new BufferedWriter(fw);
 				bw.write("# I highly recommend that you don't edit this manually!");
 				// Save Portals
 				try {
 					bw.write("\nportals:");
 					for (String portalName : portalMap.keySet()) {
 						try {
 							PortalInfo portal = portalMap.get(portalName);
 							bw.write("\n  " + portalName + ":");
 							bw.write("\n    tpCoords: " + portal.tpCoords.toString());
 							bw.write("\n    blocks: " + portal.blockCoordArrToString());
 						} catch (IOException e) {
 							throw e;
 						} catch (Exception e) {
							mLogger.severe("Error saving Portal named " + String.valueOf(portalName) + ". Error Message:\n" + e.getMessage());
 							e.printStackTrace();
 							rtn = false;
 						}
 					}
 				} catch (Exception e) {
 					mLogger.severe("Error saving Portals!");
 					e.printStackTrace();
 					rtn = false;
 				}
 				try {
 					// Save Destinations
 					bw.write("\ndestinations:");
 					for (String destName : destMap.keySet()) {
 						try {
 							bw.write("\n  " + destName + ": " + destMap.get(destName).toString());
 						} catch (Exception e) {
 							mLogger.severe("Error saving Portal Destination named " + String.valueOf(destName) + ". Error Message:\n" + e.getMessage());
 							rtn = false;
 						}
 					}
 				} catch (Exception e) {
 					mLogger.severe("Error saving Portal Destinations!");
 					rtn = false;
 				}
 			} catch (IOException e) {
 				mLogger.severe("Error saving Portal data!");
 				rtn = false;
 			} finally {
 				if (bw != null)
 					try {
 						bw.close();
 					} catch (IOException e) {
 						rtn = false;
 					}
 			}
 		} else {
 			mLogger.severe("Can't save Portal data! Portals does not have write access to the save location \"" + dataFile.getAbsolutePath() + "\".");
 			rtn = false;
 		}
 		return rtn;
 	}
 
 }
