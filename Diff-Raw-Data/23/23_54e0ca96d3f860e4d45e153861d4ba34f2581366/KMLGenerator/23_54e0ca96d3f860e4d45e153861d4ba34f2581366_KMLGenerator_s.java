 package org.waterforpeople.mapping.app.web;
 
 import java.io.StringWriter;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.commons.lang.time.DateFormatUtils;
 import org.apache.velocity.Template;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.VelocityEngine;
 import org.waterforpeople.mapping.dao.AccessPointDao;
 import org.waterforpeople.mapping.dao.GeoRegionDAO;
 import org.waterforpeople.mapping.domain.AccessPoint;
 import org.waterforpeople.mapping.domain.GeoRegion;
 import org.waterforpeople.mapping.domain.TechnologyType;
 import org.waterforpeople.mapping.domain.AccessPoint.AccessPointType;
 
 import com.gallatinsystems.common.Constants;
 import com.gallatinsystems.framework.dao.BaseDAO;
 
 public class KMLGenerator {
 	private static final Logger log = Logger.getLogger(KMLGenerator.class
 			.getName());
 
 	private VelocityEngine engine;
 
 	public KMLGenerator() {
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
 		context = null;
 		return writer.toString();
 	}
 
 	public String generateDocument(String placemarksVMName) {
 		try {
 			VelocityContext context = new VelocityContext();
 			context.put("folderContents", generatePlacemarks(placemarksVMName,
 					Constants.ALL_RESULTS));
 			context
 					.put("regionPlacemark",
 							generateRegionOutlines("Regions.vm"));
 			return mergeContext(context, "Document.vm");
 		} catch (Exception ex) {
 			log.log(Level.SEVERE, "Could create kml", ex);
 		}
 		return null;
 	}
 
 	public String generateDocument(String placemarksVMName, String countryCode) {
 		try {
 			VelocityContext context = new VelocityContext();
 			context.put("folderContents", generatePlacemarks(
 					"PlacemarksNewLook.vm", countryCode));
 			context
 					.put("regionPlacemark",
 							generateRegionOutlines("Regions.vm"));
 			return mergeContext(context, "Document.vm");
 		} catch (Exception ex) {
 			log.log(Level.SEVERE, "Could create kml", ex);
 		}
 		return null;
 	}
 
 	@SuppressWarnings("unused")
 	private String generateFolderContents(
 			HashMap<String, ArrayList<String>> contents, String vmName)
 			throws Exception {
 		VelocityContext context = new VelocityContext();
 		StringBuilder techFolders = new StringBuilder();
 
 		for (Entry<String, ArrayList<String>> techItem : contents.entrySet()) {
 			String key = techItem.getKey();
 			StringBuilder sbFolderPl = new StringBuilder();
 			for (String placemark : techItem.getValue()) {
 				sbFolderPl.append(placemark);
 			}
 			context.put("techFolderName", key);
 			context.put("techPlacemarks", sbFolderPl);
 			techFolders.append(mergeContext(context, "techFolders.vm"));
 		}
 		context.put("techFolders", techFolders.toString());
 		return mergeContext(context, vmName);
 
 	}
 
 	public void generateCountryOrderedPlacemarks(String vmName,
 			String countryCode, String technologyType) {
 
 	}
 
 	public HashMap<String, ArrayList<String>> generateCountrySpecificPlacemarks(
 			String vmName, String countryCode) {
 		if (countryCode.equals("MW")) {
 			HashMap<String, ArrayList<AccessPoint>> techMap = new HashMap<String, ArrayList<AccessPoint>>();
 			BaseDAO<TechnologyType> techDAO = new BaseDAO<TechnologyType>(
 					TechnologyType.class);
 			List<TechnologyType> techTypeList = (List<TechnologyType>) techDAO
 					.list("all");
 			AccessPointDao apDao = new AccessPointDao();
 			List<AccessPoint> waterAPList = apDao.searchAccessPoints(
 					countryCode, null, null, null, "WATER_POINT", null, null,
 					null, null, null, "all");
 			for (TechnologyType techType : techTypeList) {
 				// log.info("TechnologyType: " + techType.getName());
 				ArrayList<AccessPoint> techTypeAPList = new ArrayList<AccessPoint>();
 				for (AccessPoint item : waterAPList) {
 
 					if (techType.getName().toLowerCase().equals(
 							"unimproved waterpoint")
 							&& item.getTypeTechnologyString().toLowerCase()
 									.contains("unimproved waterpoint")) {
 						techTypeAPList.add(item);
 					} else if (item.getTypeTechnologyString().equals(
 							techType.getName())) {
 						techTypeAPList.add(item);
 					}
 				}
 				techMap.put(techType.getName(), techTypeAPList);
 			}
 
 			List<AccessPoint> sanitationAPList = apDao.searchAccessPoints(
 					countryCode, null, null, null, "SANITATION_POINT", null,
 					null, null, null, null, "all");
 			HashMap<String, AccessPoint> sanitationMap = new HashMap<String, AccessPoint>();
 			for (AccessPoint item : sanitationAPList) {
 				sanitationMap.put(item.getGeocells().toString(), item);
 			}
 			sanitationAPList = null;
 			HashMap<String, ArrayList<String>> techPlacemarksMap = new HashMap<String, ArrayList<String>>();
 			for (Entry<String, ArrayList<AccessPoint>> item : techMap
 					.entrySet()) {
 				String key = item.getKey();
 				ArrayList<String> placemarks = new ArrayList<String>();
 				for (AccessPoint waterAP : item.getValue()) {
 
 					AccessPoint sanitationAP = sanitationMap.get(waterAP
 							.getGeocells().toString());
 					if (sanitationAP != null) {
 						placemarks.add(buildMainPlacemark(waterAP,
 								sanitationAP, vmName));
 					} else {
 						log.info("No matching sanitation point found for "
 								+ waterAP.getLatitude() + ":"
 								+ waterAP.getLongitude() + ":"
 								+ waterAP.getCommunityName());
 					}
 				}
 				techPlacemarksMap.put(key, placemarks);
 			}
 			return techPlacemarksMap;
 		}
 
 		return null;
 	}
 
 	private HashMap<String, String> loadContextBindings(AccessPoint waterAP,
 			AccessPoint sanitationAP) {
 		// log.info(waterAP.getCommunityCode());
 		try {
 			HashMap<String, String> contextBindingsMap = new HashMap<String, String>();
 			contextBindingsMap.put("communityCode", encodeNullDefault(waterAP
 					.getCommunityCode(), "Unknown"));
 			contextBindingsMap.put("communityName", encodeNullDefault(waterAP
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
 			contextBindingsMap.put("waterSystemStatus",
 					encodeStatusString(waterAP.getPointStatus()));
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
 					.getAltitude().toString(), "0.0"));
 			contextBindingsMap.put("pinStyle", encodePinStyle(waterAP
 					.getPointType(), waterAP.getPointStatus()));
 			return contextBindingsMap;
 		} catch (NullPointerException nex) {
 			log.log(Level.SEVERE, "Could not load context bindings", nex);
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
 			log.log(Level.SEVERE, "Could not build main placemark", e);
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
 			// log.info("value that generated nex: " + value);
 			log.log(Level.SEVERE, "Could not encode null default", ex);
 		}
 		return null;
 	}
 
 	public String generatePlacemarks(String vmName, String countryCode) {
 		StringBuilder sb = new StringBuilder();
 		AccessPointDao apDAO = new AccessPointDao();
 		List<AccessPoint> entries = null;
 		if (countryCode.equals("all"))
 			entries = apDAO.list(Constants.ALL_RESULTS);
 		else
 			entries = apDAO.searchAccessPoints(countryCode, null, null, null,
 					null, null, null, null, null, null, Constants.ALL_RESULTS);
 
 		// loop through accessPoints and bind to variables
 		for (AccessPoint ap : entries) {
 			try {
 				sb.append(bindPlacemark(ap, vmName));
 			} catch (Exception e) {
 				log.log(Level.INFO, "Error generating placemarks: "
 						+ ap.getCommunityCode(), e);
 			}
 		}
 		return sb.toString();
 	}
 
 	public String bindPlacemark(AccessPoint ap, String vmName) throws Exception {
 		// if (ap.getCountryCode() != null && !ap.getCountryCode().equals("MW"))
 		// {
 		if (ap.getCountryCode() == null)
 			ap.setCountryCode("Unknown");
 		if (ap.getCountryCode() != null) {
 
 			VelocityContext context = new VelocityContext();
 			context.put("countryCode", ap.getCountryCode());
 			if (ap.getCollectionDate() != null) {
 				String timestamp = DateFormatUtils.formatUTC(ap
 						.getCollectionDate(), DateFormatUtils.ISO_DATE_FORMAT
 						.getPattern());
 				String formattedDate = DateFormat.getDateInstance(
 						DateFormat.SHORT).format(ap.getCollectionDate());
 				context.put("collectionDate", formattedDate);
 				context.put("timestamp", timestamp);
				String collectionYear = new SimpleDateFormat("yyyy").format(ap.getCollectionDate());
 				context.put("collectionYear", collectionYear);
 			} else {
 				String timestamp = DateFormatUtils.formatUTC(new Date(),
 						DateFormatUtils.ISO_DATE_FORMAT.getPattern());
 				String formattedDate = DateFormat.getDateInstance(
 						DateFormat.SHORT).format(new Date());
 				context.put("collectionDate", formattedDate);
 				context.put("timestamp", timestamp);
 			}
 
 			context.put("latitude", ap.getLatitude());
 			context.put("longitude", ap.getLongitude());
 			if (ap.getAltitude() == null)
 				context.put("altitude", 0.0);
 			else
 				context.put("altitude", ap.getAltitude());
 
 			if (ap.getCommunityCode() != null)
 				context.put("communityCode", ap.getCommunityCode());
 			else
 				context.put("communityCode", "Unknown" + new Date());
 
 			if (ap.getPhotoURL() != null)
 				context.put("photoUrl", ap.getPhotoURL());
 			else
 				context
 						.put("photoUrl",
 								"http://waterforpeople.s3.amazonaws.com/images/wfplogo.jpg");
 			if (ap.getPointType() != null) {
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
 				} else if (ap.getPointType().equals(AccessPointType.SCHOOL)) {
 					context.put("typeOfPoint", "School");
 					context.put("type", "school");
 				}
 			} else {
 				context.put("typeOfPoint", "Water");
 				context.put("type", "water");
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
 					|| ap.getConstructionDateYear().trim().equals("")) {
 				context.put("constructionDateOfWaterPoint", "Unknown");
 			} else {
 				context.put("constructionDateOfWaterPoint", ap
 						.getConstructionDateYear());
 			}
 			if (ap.getNumberOfHouseholdsUsingPoint() == null) {
 				context.put("numberOfHouseholdsUsingWaterPoint", "Unknown");
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
 					|| ap.getFarthestHouseholdfromPoint().trim().equals("")) {
 				context.put("farthestHouseholdfromWaterPoint", "N/A");
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
 				context.put("waterPointPhotoCaption", "Water For People");
 			} else {
 				context
 						.put("waterPointPhotoCaption", ap
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
 				context.put("meetGovtQuantityStandardFlag",
 						encodeBooleanDisplay(ap
 								.getMeetGovtQuantityStandardFlag()));
 			}
 
 			if (ap.getWhoRepairsPoint() == null) {
 				context.put("whoRepairsPoint", "N/A");
 			} else {
 				context.put("whoRepairsPoint", ap.getWhoRepairsPoint());
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
 				context.put("provideAdequateQuantity", encodeBooleanDisplay(ap
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
 				context.put("provideAdequateQuantity", encodeBooleanDisplay(ap
 						.getProvideAdequateQuantity()));
 			}
 
 			if (ap.getDescription() != null)
 				context.put("description", ap.getDescription());
 			else
 				context.put("description", "Unknown");
 
 			// Need to check this
 			if (ap.getPointType() != null) {
 				context.put("pinStyle", encodePinStyle(ap.getPointType(), ap
 						.getPointStatus()));
 				encodeStatusString(ap.getPointStatus(), context);
 			} else {
 				context.put("pinStyle", "waterpushpinblk");
 			}
 			String output = mergeContext(context, vmName);
 			context = null;
 			return output;
 
 		}
 		return null;
 
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
 
 	private String encodePinStyle(AccessPointType type,
 			AccessPoint.Status status) {
 		String prefix = "water";
 		if (AccessPointType.SANITATION_POINT == type) {
 			prefix = "sani";
 		} else if (AccessPointType.SCHOOL == type) {
 			prefix = "schwater";
 		} else if (AccessPointType.PUBLIC_INSTITUTION == type) {
 			prefix = "pubwater";
 		}
 		if (AccessPoint.Status.FUNCTIONING_HIGH == status) {
 			return prefix + "pushpingreen";
 		} else if (AccessPoint.Status.FUNCTIONING_OK == status) {
 			return prefix + "pushpinyellow";
 		} else if (AccessPoint.Status.FUNCTIONING_WITH_PROBLEMS == status) {
 			return prefix + "pushpinred";
 		} else if (AccessPoint.Status.NO_IMPROVED_SYSTEM == status) {
 			return prefix + "pushpinblk";
 		} else {
 			return prefix + "pushpinblk";
 		}
 	}
 
 	private String encodeStatusString(AccessPoint.Status status,
 			VelocityContext context) {
 
 		if (status != null) {
 			if (AccessPoint.Status.FUNCTIONING_HIGH == status) {
 				context.put("waterSystemStatus",
 						"System Functioning and Meets Government Standards");
 				return "System Functioning and Meets Government Standards";
 			} else if (AccessPoint.Status.FUNCTIONING_OK == status) {
 				context.put("waterSystemStatus",
 						"Functioning but with Problems");
 				return "Functioning but with Problems";
 			} else if (AccessPoint.Status.FUNCTIONING_WITH_PROBLEMS == status) {
 				context.put("waterSystemStatus", "Broken-down system");
 				return "Broken-down system";
 			} else if (AccessPoint.Status.NO_IMPROVED_SYSTEM == status) {
 				context.put("waterSystemStatus", "No Improved System");
 				return "No Improved System";
 			} else {
 				context.put("waterSystemStatus", "Unknown");
 				return "Unknown";
 			}
 		} else {
 			context.put("waterSystemStatus", "Unknown");
 			return "Unknown";
 		}
 	}
 
 	private String encodeStatusString(AccessPoint.Status status) {
 		if (status == null) {
 			return "Unknown";
 		}
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
