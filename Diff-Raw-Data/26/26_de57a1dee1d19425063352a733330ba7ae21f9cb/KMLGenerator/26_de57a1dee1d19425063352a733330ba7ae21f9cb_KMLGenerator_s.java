 package org.waterforpeople.mapping.app.web;
 
 import java.io.StringWriter;
 import java.text.DateFormat;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.velocity.Template;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.VelocityEngine;
 import org.waterforpeople.mapping.dao.AccessPointDao;
 import org.waterforpeople.mapping.dao.GeoRegionDAO;
 import org.waterforpeople.mapping.dao.KMLDAO;
 import org.waterforpeople.mapping.domain.AccessPoint;
 import org.waterforpeople.mapping.domain.GeoRegion;
 import org.waterforpeople.mapping.domain.AccessPoint.AccessPointType;
 
 import com.gallatinsystems.common.Constants;
 import com.gallatinsystems.framework.dao.BaseDAO;
 
 public class KMLGenerator {
 	private static final Logger log = Logger.getLogger(KMLGenerator.class
 			.getName());
 
 	private KMLDAO kmlDAO;
 	private VelocityEngine engine;
 
 	public KMLGenerator() {
 		kmlDAO = new KMLDAO();
 		engine = new VelocityEngine();
 		engine.setProperty("runtime.log.logsystem.class",
 				"org.apache.velocity.runtime.log.NullLogChute");
 		try {
 			engine.init();
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "Could not initialize velocity", e);
 		}
 	}
 
 	public String generateRegionDocumentString(String regionVMName) {
 		String regionKML = generateRegionOutlines(regionVMName);
 		return regionKML;
 	}
 
 	/**
 	 * merges a hydrated context with a template identified by the templateName
 	 * passed in.
 	 * 
 	 * @param context
 	 * @param templateName
 	 * @return
 	 * @throws Exception
 	 */
 	private String mergeContext(VelocityContext context, String templateName)
 			throws Exception {
 		Template t = engine.getTemplate(templateName);
 		StringWriter writer = new StringWriter();
 		t.merge(context, writer);
 		return writer.toString();
 	}
 
 	public String generateDocument(String placemarksVMName) {
 		String document = null;
 		try {
 			VelocityContext context = new VelocityContext();
 			String mwOutput = generateCountrySpecificPlacemarks(
 					"PlacemarkTabsMW.vm", "MW");
 			String otherCountryOutput = generatePlacemarks("PlacemarkTabs.vm");
 			context.put("Placemark", otherCountryOutput + mwOutput);
 			context
 					.put("regionPlacemark",
 							generateRegionOutlines("Regions.vm"));
 			document = mergeContext(context, "Document.vm");
 			kmlDAO.saveKML(document);
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		return document;
 	}
 
 	public String generateCountrySpecificPlacemarks(String vmName,
 			String countryCode) {
 		if (countryCode.equals("MW")) {
 			AccessPointDao apDao = new AccessPointDao();
 			List<AccessPoint> waterAPList = apDao.searchAccessPoints(
 					countryCode, null, null, null, "WATER_POINT", null, "all");
 			List<AccessPoint> sanitationAPList = apDao.searchAccessPoints(
 					countryCode, null, null, null, "SANITATION_POINT", null,
 					"all");
 			HashMap<String, AccessPoint> sanitationMap = new HashMap<String, AccessPoint>();
 			for (AccessPoint item : sanitationAPList) {
 				sanitationMap.put(item.getGeocells().toString(), item);
 			}
 			StringBuilder placemarksSB = new StringBuilder();
 			int iCount = 0;
 			for (AccessPoint item : waterAPList) {
				log.info("Point number: " + iCount++);
 				if (item.getPointType().equals(
 						AccessPoint.AccessPointType.WATER_POINT)) {
 					AccessPoint waterAP = item;
 					AccessPoint sanitationAP = sanitationMap.get(waterAP
 							.getGeocells().toString());
 					if (sanitationAP != null) {
 						placemarksSB.append(buildMainPlacemark(waterAP,
 								sanitationAP, vmName));
 					} else {
 						log.info("No matching sanitation point found for "
 								+ waterAP.getLatitude() + ":"
 								+ waterAP.getLongitude() + ":"
 								+ waterAP.getCommunityName());
 					}
 				}
 			}
 			return placemarksSB.toString();
 		}
 		return null;
 	}
 
 	private HashMap<String, String> loadContextBindings(AccessPoint waterAP,
 			AccessPoint sanitationAP) {
		log.info(waterAP.getCommunityCode());
		if (waterAP.getCommunityCode().equals("MB02E002")) {
			log.info("Bad record");
		}
 		try {
 			HashMap<String, String> contextBindingsMap = new HashMap<String, String>();
 			contextBindingsMap.put("communityCode", encodeNullDefault(waterAP
 					.getCommunityCode(), "Unknown"));
 			contextBindingsMap.put("communityName",  encodeNullDefault(waterAP
 					.getCommunityName(), "Unknown"));
 			contextBindingsMap.put("typeOfWaterPointTechnology",
 					encodeNullDefault(waterAP.getTypeTechnologyString(),
 							"Unknown"));
 			contextBindingsMap.put("constructionDateOfWaterPoint",
 					encodeNullDefault(waterAP.getConstructionDateYear(),
 							"Unknown"));
 			contextBindingsMap.put("numberOfHouseholdsUsingWaterPoint",
 					encodeNullDefault(
 							waterAP.getNumberOfHouseholdsUsingPoint(),
 							"Unknown"));
 			contextBindingsMap.put("costPer20ML", encodeNullDefault(waterAP
 					.getCostPer(), "Unknown"));
 			contextBindingsMap.put("farthestHouseholdFromWaterPoint",
 					encodeNullDefault(waterAP.getFarthestHouseholdfromPoint(),
 							"Unknown"));
 			contextBindingsMap.put("currentManagementStructureOfWaterPoint",
 					encodeNullDefault(waterAP
 							.getCurrentManagementStructurePoint(), "Unknown"));
 			contextBindingsMap.put("waterSystemStatus", encodeStatusString(
 					waterAP.getPointStatus()));
 			contextBindingsMap.put("photoUrl", encodeNullDefault(waterAP
 					.getPhotoURL(), "Unknown"));
 			contextBindingsMap.put("waterPointPhotoCaption", encodeNullDefault(
 					waterAP.getPointPhotoCaption(), "Unknown"));
 			contextBindingsMap.put("primarySanitationTechnology",
 					encodeNullDefault(sanitationAP.getTypeTechnologyString(),
 							"Unknown"));
 			contextBindingsMap.put(
 					"percentageOfHouseholdsWithImprovedSanitation",
 					encodeNullDefault(sanitationAP
 							.getNumberOfHouseholdsUsingPoint(), "Unknown"));
 			contextBindingsMap.put("photoOfPrimarySanitationtechnology",
 					encodeNullDefault(sanitationAP.getPhotoURL(), "Unknown"));
 			contextBindingsMap.put("sanitationPhotoCaption", encodeNullDefault(
 					sanitationAP.getPointPhotoCaption(), "Unknown"));
 			contextBindingsMap.put("footer", encodeNullDefault(waterAP
 					.getFooter(), "Unknown"));
 			contextBindingsMap.put("longitude", encodeNullDefault(waterAP
 					.getLongitude().toString(), "Unknown"));
 			contextBindingsMap.put("latitude", encodeNullDefault(waterAP
 					.getLatitude().toString(), "Unknown"));
 			contextBindingsMap.put("altitude", encodeNullDefault(waterAP
 					.getAltitude().toString(), "Unknown"));
 			contextBindingsMap.put("pinStyle", encodePinStyle(waterAP.getPointType(),waterAP.getPointStatus()));
 			return contextBindingsMap;
 		} catch (NullPointerException nex) {
 			nex.printStackTrace();
 		}
 		return null;
 	}
 
 	private String buildMainPlacemark(AccessPoint waterAP,
 			AccessPoint sanitationAP, String vmName) {
 		HashMap<String, String> contextBindingsMap = loadContextBindings(
 				waterAP, sanitationAP);
 		VelocityContext context = new VelocityContext();
 
 		for (Map.Entry<String, String> entry : contextBindingsMap.entrySet()) {
 			context.put(entry.getKey(), entry.getValue());
 		}
 		StringBuilder sb = new StringBuilder();
 		String output = null;
 		try {
 			output = mergeContext(context, vmName);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		sb.append(output);
 
 		return sb.toString();
 	}
 
 	private String encodeNullDefault(Object value, String defaultMissingVal) {
 		try {
 			if (value != null) {
 				return value.toString();
 			} else {
 				return defaultMissingVal;
 			}
 		} catch (Exception ex) {
			log.info("value that generated nex: " + value);
 			ex.printStackTrace();
 		}
 		return null;
 	}
 
 	public String generatePlacemarks(String vmName) {
 
 		// for testing
 
 		StringBuilder sb = new StringBuilder();
 		BaseDAO<AccessPoint> apDAO = new BaseDAO<AccessPoint>(AccessPoint.class);
 
 		List<AccessPoint> entries = apDAO.list(Constants.ALL_RESULTS);
 		try {
 			// loop through accessPoints and bind to variables
 			for (AccessPoint ap : entries) {
 				try {
 					if (!ap.getCountryCode().equals("MW")) {
 						VelocityContext context = new VelocityContext();
 						if (ap.getCollectionDate() != null) {
 							String formattedDate = DateFormat.getDateInstance(
 									DateFormat.SHORT).format(
 									ap.getCollectionDate());
 							context.put("collectionDate", formattedDate);
 						} else {
 							context.put("collectionDate", "N/A");
 						}
 						context.put("latitude", ap.getLatitude());
 						context.put("longitude", ap.getLongitude());
 						context.put("altitude", ap.getAltitude());
 						context.put("communityCode", ap.getCommunityCode());
 						context.put("photoUrl", ap.getPhotoURL());
 						if (ap.getPointType().equals(
 								AccessPoint.AccessPointType.WATER_POINT)) {
 							context.put("typeOfPoint", "Water");
 							context.put("type", "water");
 						} else if (ap.getPointType().equals(
 								AccessPointType.SANITATION_POINT)) {
 							context.put("typeOfPoint", "Sanitation");
 							context.put("type", "sanitation");
 						} else if (ap.getPointType().equals(
 								AccessPointType.PUBLIC_INSTITUTION)) {
 							context.put("typeOfPoint", "Public Institutions");
 							context.put("type", "public_institutions");
 						} else if (ap.getPointType().equals(
 								AccessPointType.HEALTH_POSTS)) {
 							context.put("typeOfPoint", "Health Posts");
 							context.put("type", "health_posts");
 						} else if (ap.getPointType().equals(
 								AccessPointType.SCHOOL)) {
 							context.put("typeOfPoint", "School");
 							context.put("type", "school");
 						}
 
 						if (ap.getTypeTechnologyString() == null) {
 							context.put("primaryTypeTechnology", "Unknown");
 						} else {
 							context.put("primaryTypeTechnology", ap
 									.getTypeTechnologyString());
 						}
 
 						if (ap.getHasSystemBeenDown1DayFlag() == null) {
 							context.put("down1DayFlag", "Unknown");
 						} else {
 							context.put("down1DayFlag", encodeBooleanDisplay(ap
 									.getHasSystemBeenDown1DayFlag()));
 						}
 
 						if (ap.getInstitutionName() == null) {
 							context.put("institutionName", "Unknown");
 						} else {
 							context.put("institutionName", "Unknown");
 						}
 
 						if (ap.getConstructionDateYear() == null
 								|| ap.getConstructionDateYear().trim().equals(
 										"")) {
 							context.put("constructionDateOfWaterPoint",
 									"Unknown");
 						} else {
 							context.put("constructionDateOfWaterPoint", ap
 									.getConstructionDateYear());
 						}
 						if (ap.getNumberOfHouseholdsUsingPoint() == null) {
 							context.put("numberOfHouseholdsUsingWaterPoint",
 									"Unknown");
 						} else {
 							context.put("numberOfHouseholdsUsingWaterPoint", ap
 									.getNumberOfHouseholdsUsingPoint());
 						}
 						if (ap.getCostPer() == null) {
 							context.put("costPer", "N/A");
 						} else {
 							context.put("costPer", ap.getCostPer());
 						}
 						if (ap.getFarthestHouseholdfromPoint() == null
 								|| ap.getFarthestHouseholdfromPoint().trim()
 										.equals("")) {
 							context.put("farthestHouseholdfromWaterPoint",
 									"N/A");
 						} else {
 							context.put("farthestHouseholdfromWaterPoint", ap
 									.getFarthestHouseholdfromPoint());
 						}
 						if (ap.getCurrentManagementStructurePoint() == null) {
 							context.put("currMgmtStructure", "N/A");
 						} else {
 							context.put("currMgmtStructure", ap
 									.getCurrentManagementStructurePoint());
 						}
 						if (ap.getPointPhotoCaption() == null
 								|| ap.getPointPhotoCaption().trim().equals("")) {
 							context.put("waterPointPhotoCaption",
 									"Water For People");
 						} else {
 							context.put("waterPointPhotoCaption", ap
 									.getPointPhotoCaption());
 						}
 						if (ap.getCommunityName() == null) {
 							context.put("communityName", "Unknown");
 						} else {
 							context.put("communityName", ap.getCommunityName());
 						}
 
 						if (ap.getHeader() == null) {
 							context.put("header", "Water For People");
 						} else {
 							context.put("header", ap.getHeader());
 						}
 
 						if (ap.getFooter() == null) {
 							context.put("footer", "Water For People");
 						} else {
 							context.put("footer", ap.getFooter());
 						}
 
 						if (ap.getPhotoName() == null) {
 							context.put("photoName", "Water For People");
 						} else {
 							context.put("photoName", ap.getPhotoName());
 						}
 
 						if (ap.getMeetGovtQualityStandardFlag() == null) {
 							context.put("meetGovtQualityStandardFlag", "N/A");
 						} else {
 							context.put("meetGovtQualityStandardFlag",
 									encodeBooleanDisplay(ap
 											.getMeetGovtQualityStandardFlag()));
 						}
 						if (ap.getMeetGovtQuantityStandardFlag() == null) {
 							context.put("meetGovtQuantityStandardFlag", "N/A");
 						} else {
 							context
 									.put(
 											"meetGovtQuantityStandardFlag",
 											encodeBooleanDisplay(ap
 													.getMeetGovtQuantityStandardFlag()));
 						}
 
 						if (ap.getWhoRepairsPoint() == null) {
 							context.put("whoRepairsPoint", "N/A");
 						} else {
 							context.put("whoRepairsPoint", ap
 									.getWhoRepairsPoint());
 						}
 
 						if (ap.getSecondaryTechnologyString() == null) {
 							context.put("secondaryTypeTechnology", "N/A");
 						} else {
 							context.put("secondaryTypeTechnology", ap
 									.getSecondaryTechnologyString());
 						}
 
 						if (ap.getProvideAdequateQuantity() == null) {
 							context.put("provideAdequateQuantity", "N/A");
 						} else {
 							context.put("provideAdequateQuantity",
 									encodeBooleanDisplay(ap
 											.getProvideAdequateQuantity()));
 						}
 
 						if (ap.getBalloonTitle() == null) {
 							context.put("title", "Water For People");
 						} else {
 							context.put("title", ap.getBalloonTitle());
 						}
 
 						if (ap.getProvideAdequateQuantity() == null) {
 							context.put("provideAdequateQuantity", "N/A");
 						} else {
 							context.put("provideAdequateQuantity",
 									encodeBooleanDisplay(ap
 											.getProvideAdequateQuantity()));
 						}
 
 						context.put("description", ap.getDescription());
 						// Need to check this
 						encodeStatus(ap.getPointType(), ap.getPointStatus(),
 								context);
 						String output = mergeContext(context, vmName);
 						sb.append(output);
 					}
 				} catch (Exception ex) {
					log.info(ex.getMessage());
 				}
 
 			}
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "Error generating placemarks", e);
 		}
 		return sb.toString();
 	}
 
 	public String generateRegionOutlines(String vmName) {
 		StringBuilder sb = new StringBuilder();
 		GeoRegionDAO grDAO = new GeoRegionDAO();
 		List<GeoRegion> grList = grDAO.list();
 		try {
 			if (grList != null && grList.size() > 0) {
 				String currUUID = grList.get(0).getUuid();
 				VelocityContext context = new VelocityContext();
 				StringBuilder sbCoor = new StringBuilder();
 
 				// loop through GeoRegions and bind to variables
 				for (int i = 0; i < grList.size(); i++) {
 					GeoRegion gr = grList.get(i);
 
 					if (currUUID.equals(gr.getUuid())) {
 						sbCoor.append(gr.getLongitude() + ","
 								+ gr.getLatitiude() + "," + 0 + "\n");
 					} else {
 						currUUID = gr.getUuid();
 						context.put("coordinateString", sbCoor.toString());
 						sb.append(mergeContext(context, vmName));
 
 						context = new VelocityContext();
 						sbCoor = new StringBuilder();
 						sbCoor.append(gr.getLongitude() + ","
 								+ gr.getLatitiude() + "," + 0 + "\n");
 					}
 				}
 
 				context.put("coordinateString", sbCoor.toString());
 				sb.append(mergeContext(context, vmName));
 				return sb.toString();
 			}
 
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "Error generating region outlines", e);
 		}
 		return "";
 	}
 
 	private String encodeBooleanDisplay(Boolean value) {
 		if (value) {
 			return "Yes";
 		} else {
 			return "No";
 		}
 	}
 
 	private void encodeStatus(AccessPointType type, AccessPoint.Status status,
 			VelocityContext context) {
 		if (type.equals(AccessPointType.SANITATION_POINT)) {
 			context.put("pinStyle", "pushpinpurple");
 		} else {
 			if (status.equals(AccessPoint.Status.FUNCTIONING_HIGH)) {
 				context.put("pinStyle", "pushpingreen");
 			} else if (status.equals(AccessPoint.Status.FUNCTIONING_OK)) {
 				context.put("pinStyle", "pushpinyellow");
 			} else if (status
 					.equals(AccessPoint.Status.FUNCTIONING_WITH_PROBLEMS)) {
 				context.put("pinStyle", "pushpinred");
 			} else if (status.equals(AccessPoint.Status.NO_IMPROVED_SYSTEM)) {
 				context.put("pinStyle", "pushpinblk");
 			} else {
 				context.put("pinStyle", "pushpinblk");
 			}
 		}
 		encodeStatusString(status, context);
 	}
 
 	private String encodePinStyle(AccessPointType type,
 			AccessPoint.Status status) {
 		if (status.equals(AccessPoint.Status.FUNCTIONING_HIGH)) {
 			return "pushpingreen";
 		} else if (status.equals(AccessPoint.Status.FUNCTIONING_OK)) {
 			return "pushpinyellow";
 		} else if (status.equals(AccessPoint.Status.FUNCTIONING_WITH_PROBLEMS)) {
 			return "pushpinred";
 		} else if (status.equals(AccessPoint.Status.NO_IMPROVED_SYSTEM)) {
 			return "pushpinblk";
 		} else {
 			return "pushpinblk";
 		}
 	}
 
 	private String encodeStatusString(AccessPoint.Status status,
 			VelocityContext context) {
 
 		if (status.equals(AccessPoint.Status.FUNCTIONING_HIGH)) {
 			context.put("waterSystemStatus",
 					"System Functioning and Meets Government Standards");
 			return "System Functioning and Meets Government Standards";
 		} else if (status.equals(AccessPoint.Status.FUNCTIONING_OK)) {
 			context.put("waterSystemStatus", "Functioning but with Problems");
 			return "Functioning but with Problems";
 		} else if (status.equals(AccessPoint.Status.FUNCTIONING_WITH_PROBLEMS)) {
 			context.put("waterSystemStatus", "Broken-down system");
 			return "Broken-down system";
 		} else if (status.equals(AccessPoint.Status.NO_IMPROVED_SYSTEM)) {
 			context.put("waterSystemStatus", "No Improved System");
 			return "No Improved System";
 		} else {
 			context.put("waterSystemStatus", "Unknown");
 			return "Unknown";
 		}
 	}
 
 	private String encodeStatusString(AccessPoint.Status status) {
 
 		if (status.equals(AccessPoint.Status.FUNCTIONING_HIGH)) {
 			return "System Functioning and Meets Government Standards";
 		} else if (status.equals(AccessPoint.Status.FUNCTIONING_OK)) {
 			return "Functioning but with Problems";
 		} else if (status.equals(AccessPoint.Status.FUNCTIONING_WITH_PROBLEMS)) {
 			return "Broken-down system";
 		} else if (status.equals(AccessPoint.Status.NO_IMPROVED_SYSTEM)) {
 			return "No Improved System";
 		} else {
 			return "Unknown";
 		}
 	}
 }
