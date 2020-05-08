 package au.com.gaiaresources.bdrs.controller.webservice;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.math.BigDecimal;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import javax.annotation.security.RolesAllowed;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import au.com.gaiaresources.bdrs.json.JSONArray;
 import au.com.gaiaresources.bdrs.json.JSONObject;
 
 import org.apache.commons.beanutils.PropertyUtils;
 import org.apache.log4j.Logger;
 import org.postgresql.util.Base64;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import au.com.gaiaresources.bdrs.controller.AbstractController;
 import au.com.gaiaresources.bdrs.controller.attribute.formfield.RecordProperty;
 import au.com.gaiaresources.bdrs.controller.attribute.formfield.RecordPropertyType;
 import au.com.gaiaresources.bdrs.file.FileService;
 import au.com.gaiaresources.bdrs.model.location.Location;
 import au.com.gaiaresources.bdrs.model.location.LocationDAO;
 import au.com.gaiaresources.bdrs.model.metadata.Metadata;
 import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
 import au.com.gaiaresources.bdrs.model.method.CensusMethod;
 import au.com.gaiaresources.bdrs.model.method.CensusMethodDAO;
 import au.com.gaiaresources.bdrs.model.record.Record;
 import au.com.gaiaresources.bdrs.model.record.RecordDAO;
 import au.com.gaiaresources.bdrs.model.survey.Survey;
 import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
 import au.com.gaiaresources.bdrs.model.taxa.Attribute;
 import au.com.gaiaresources.bdrs.model.taxa.AttributeScope;
 import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
 import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
 import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
 import au.com.gaiaresources.bdrs.model.taxa.TaxaService;
 import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
 import au.com.gaiaresources.bdrs.model.user.User;
 import au.com.gaiaresources.bdrs.model.user.UserDAO;
 import au.com.gaiaresources.bdrs.security.Role;
 
 @Controller
 public class ApplicationService extends AbstractController {
     
     public static final String CLIENT_SYNC_STATUS_KEY = "status";
     
     private Logger log = Logger.getLogger(getClass());
 
     @Autowired
     private SurveyDAO surveyDAO;
     
     @Autowired
     private TaxaDAO taxaDAO;
 
     @Autowired
     private UserDAO userDAO;
     
     @Autowired
     private RecordDAO recordDAO;
     
     @Autowired
     private MetadataDAO metadataDAO;
     
     @Autowired
     private CensusMethodDAO censusMethodDAO;
     
     @Autowired
     private TaxaService taxaService;
     
     @Autowired
     private FileService fileService;
     
     @Autowired
     private au.com.gaiaresources.bdrs.model.location.LocationService locationService;
     
     @Autowired
     private LocationDAO locationDAO;
     
     private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
     
     @RolesAllowed({Role.ADMIN, Role.ROOT, Role.POWERUSER, Role.SUPERVISOR, Role.USER, Role.ANONYMOUS})
     @RequestMapping(value = "/webservice/application/survey.htm", method = RequestMethod.GET)
     public void getSurvey( HttpServletRequest request, HttpServletResponse response,
                            @RequestParam(value = "ident", defaultValue = "") String ident,
                            @RequestParam(value = "sid", defaultValue = "-1") int surveyRequested) throws IOException {
 
     	long now = System.currentTimeMillis();
         // Checks if a user exists with the provided ident. If not a response error is returned.
         if (userDAO.getUserByRegistrationKey(ident) == null) {
             response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
             return;
         }
         
         if(surveyRequested < 1) {
             // The survey that you want cannot exist.
             response.sendError(HttpServletResponse.SC_NOT_FOUND);
             return;
         }
         log.debug("Authenticated in  :" + (System.currentTimeMillis() - now));now = System.currentTimeMillis();
         
         //retrieve requested survey
         Survey survey = surveyDAO.getSurvey(surveyRequested);
         log.debug("Retrieved Survey in  :" + (System.currentTimeMillis() - now));now = System.currentTimeMillis();
         
         // Retrieve taxon groups.
         List<TaxonGroup> taxonGroups = taxaDAO.getTaxonGroup(survey);
         log.debug("Got groups  :" + (System.currentTimeMillis() - now));now = System.currentTimeMillis();
         
         //Retrieve species from survey if any, otherwise get all from model
         Collection<IndicatorSpecies> species;
 
         // Remove data from the requested survey that already exists on the device.
         JSONArray surveysOnDeviceArray = null;
         if(request.getParameter("surveysOnDevice") != null) {
             surveysOnDeviceArray = JSONArray.fromString(request.getParameter("surveysOnDevice"));
         }
         if ((surveysOnDeviceArray != null) && (surveysOnDeviceArray.size() > 0)) {
             List<Survey> surveysOnDevice = new ArrayList<Survey>();
             for (int i=0; i<surveysOnDeviceArray.size(); i++) {
                 int sid = surveysOnDeviceArray.getInt(i);
             	Survey s = surveyDAO.getSurvey(sid);
             	surveysOnDevice.add(s);
                 //remove species
                 //species.removeAll(surveyDAO.getSurveyData((Integer) sid).getSpecies());
                 //remove taxonGroups
                 taxonGroups.removeAll(taxaDAO.getTaxonGroup(s));
             }
             log.debug("Removed extra groups in  :" + (System.currentTimeMillis() - now));now = System.currentTimeMillis();
             
             log.debug("Querying for species in survey : " + survey.getId() + " , not in : " + surveysOnDevice.toString());
             species = surveyDAO.getSpeciesForSurvey(survey, surveysOnDevice);
             
             log.debug("Got limited set of Species in  :" + (System.currentTimeMillis() - now));now = System.currentTimeMillis();
         } else {
             if (survey.getSpecies().size() == 0) {
                 species = new ArrayList<IndicatorSpecies>();
                 species.addAll(taxaDAO.getIndicatorSpecies());
                 log.debug("Got all species in  :" + (System.currentTimeMillis() - now));now = System.currentTimeMillis();
                 
             } else {
                 species = new HashSet<IndicatorSpecies>();
                 species.addAll(survey.getSpecies());
                 log.debug("Got survey species in  :" + (System.currentTimeMillis() - now));now = System.currentTimeMillis();
                 
             }
         }
 
         
         // Restructure survey data
         JSONArray attArray = new JSONArray();
         JSONArray locArray = new JSONArray();
         JSONArray speciesArray = new JSONArray();
         JSONArray taxonGroupArray = new JSONArray();
         JSONArray censusMethodArray = new JSONArray();
         JSONArray recordPropertiesArray = new JSONArray();
         for (Attribute a : survey.getAttributes()) {
             // Not sending any location scoped attributes because they do not get populated by the recording form.
             if(!AttributeScope.LOCATION.equals(a.getScope())) {
                 attArray.add(a.flatten(1, true, true));
             }
         }
         log.debug("Flattened attributes in  :" + (System.currentTimeMillis() - now));now = System.currentTimeMillis();
         
         for (Location l : survey.getLocations()) {
             locArray.add(l.flatten(1, true, true));
         }
         log.debug("Flatted locations in  :" + (System.currentTimeMillis() - now));now = System.currentTimeMillis();
         
         Map<String, Object> speciesMap = null;
         int count = 0;
         for (IndicatorSpecies s : species) {
         	
 	            // TODO AJ modified so that we don't send down taxon group 
 	        	// attributes with EVERY taxa, need to figure out how to 
 	        	// efficiently cram in indicator_species_attributes.
 	        	// previously this was flattening to depth 2.
 	        	speciesMap = s.flatten(1, true, true);
 	        	speciesMap.put("taxonGroup", s.getTaxonGroup().getId()); // unflatten the taxongroup.
 	        	speciesMap.remove("_class");
 	        	speciesArray.add(speciesMap);
         	
         	count++;
         }
         log.debug("Flattened Species in  :" + (System.currentTimeMillis() - now));now = System.currentTimeMillis();
         
         for (TaxonGroup t : taxonGroups) {
             taxonGroupArray.add(t.flatten(2, true, true));
         }
         log.debug("Flatted Taxon Groups in  :" + (System.currentTimeMillis() - now));now = System.currentTimeMillis();
         
         for(CensusMethod method : survey.getCensusMethods()) {
             censusMethodArray.add(recurseFlattenCensusMethod(method));
         }
         log.debug("Flatted Census Methods in  :" + (System.currentTimeMillis() - now));now = System.currentTimeMillis();
         
         for (RecordPropertyType recordPropertyType : RecordPropertyType.values()) {
         	RecordProperty recordProperty = new RecordProperty(survey, recordPropertyType, metadataDAO);
         	if (!recordProperty.isHidden()) {
         		recordPropertiesArray.add(recordProperty.flatten(true, false));
         	}
         }
         log.debug("Flatted RecordProperties in  :" + (System.currentTimeMillis() - now));now = System.currentTimeMillis();
         
         // Store restructured survey data in JSONObject
         JSONObject surveyData = new JSONObject();
         surveyData.put("attributesAndOptions", attArray);
        surveyData.put("locations", locArray.toString());
         surveyData.put("indicatorSpecies_server_ids", survey.flatten());
         surveyData.put("indicatorSpecies", speciesArray);
         surveyData.put("taxonGroups", taxonGroupArray);
         surveyData.put("censusMethods", censusMethodArray);
         surveyData.put("recordProperties", recordPropertiesArray);
 
         // support for JSONP
         if (request.getParameter("callback") != null) {
             response.setContentType("application/javascript");
             response.getWriter().write(request.getParameter("callback")
                     + "(");
         } else {
             response.setContentType("application/json");
         }
 
         response.getWriter().write(surveyData.toString());
         if (request.getParameter("callback") != null) {
             response.getWriter().write(");");
         }
         log.debug("Wrote out data in  :" + (System.currentTimeMillis() - now));now = System.currentTimeMillis();
     }
     
     @RolesAllowed({Role.ADMIN, Role.ROOT, Role.POWERUSER, Role.SUPERVISOR, Role.USER, Role.ANONYMOUS})
     @RequestMapping(value = "/webservice/application/clientSync.htm", method = RequestMethod.POST)
     public ModelAndView clientSync(HttpServletRequest request, HttpServletResponse response,
             @RequestParam(value="inFrame", defaultValue="true") boolean inFrame) throws IOException {
         /*
          * { 
          *      status : 200,
          *      200 : { ... } 
          * }
          * 
          * or
          * 
          * {
          *      status : 500,
          *      500 : { ... }
          * }
          */
         
         JSONObject jsonObj = new JSONObject();
         try {
             String ident = request.getParameter("ident");
             if(ident == null) {
                 throw new NullPointerException("Missing GET parameter 'ident'.");
             }
             
             String jsonData = request.getParameter("syncData");
 
             if(jsonData == null) {
                 throw new NullPointerException("Missing GET parameter 'syncData'.");
             }
             
             if (userDAO.getUserByRegistrationKey(ident) != null) {
                 User user = userDAO.getUserByRegistrationKey(ident);
                 JSONObject status = new JSONObject();
 
                 // The list of json objects that shall be passed back to the 
                 // client.
                 // This should be a list of objects that map the client id 
                 // to the new server id.
                 List<Map<String, Object>> syncResponseList = new ArrayList<Map<String, Object>>(); 
                 JSONArray clientData = JSONArray.fromString(jsonData);
                 
                 for(Object jsonRecordBean : clientData){
                     syncRecord(syncResponseList, jsonRecordBean, user);
                 }
                 
                 status.put("sync_result", syncResponseList);
                 jsonObj.put(CLIENT_SYNC_STATUS_KEY, HttpServletResponse.SC_OK);
                 jsonObj.put(HttpServletResponse.SC_OK, status);
             } else {
                 JSONObject auth = new JSONObject();
                 auth.put("message", "Unauthorized");
                 
                 jsonObj.put(CLIENT_SYNC_STATUS_KEY, HttpServletResponse.SC_UNAUTHORIZED);
                 jsonObj.put(HttpServletResponse.SC_UNAUTHORIZED, auth);
             }
         } catch(Throwable e) {
             // Catching throwable is bad but we do not want to cause an 
             // unhandled anything. Ever.
             // The reason is that the cross window communication on the client
             // side won't get triggered and the client will not have any idea
             // what happened.
             
             log.error(e.getMessage(), e);
             
             JSONObject error = new JSONObject();
             error.put("type", jsonStringEscape(e.getClass().getSimpleName().toString()));
             error.put("message", jsonStringEscape(e.getMessage()));
             
             jsonObj.put(CLIENT_SYNC_STATUS_KEY, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
             jsonObj.put(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
         }
 
         if (inFrame) {
             ModelAndView mv = new ModelAndView("postMessage");
             mv.addObject("message", jsonObj.toString());
             log.debug(jsonObj.toString());
             return mv;
         } else {
             this.writeJson(request, response, jsonObj.toString());
             return null;
         }
     }
     
     @RolesAllowed({Role.ADMIN, Role.ROOT, Role.POWERUSER, Role.SUPERVISOR, Role.USER, Role.ANONYMOUS})
     @RequestMapping(value = "/webservice/application/ping.htm", method = RequestMethod.GET)
     public void ping(HttpServletRequest request, HttpServletResponse response) throws IOException {
         // support for JSONP
         if (request.getParameter("callback") != null) {
             response.setContentType("application/javascript");
             response.getWriter().write(request.getParameter("callback")
                     + "();");
         }
     }
     
     private void syncRecord(List<Map<String, Object>> syncResponseList, Object jsonRecordBean, User user) 
         throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, IOException {
         
         String clientID = getJSONString(jsonRecordBean, "id", null);
         if(clientID == null) {
             throw new NullPointerException();
         }
         
         Integer recordPk = getJSONInteger(jsonRecordBean, "server_id", 0);
         Record rec;
         if(recordPk < 1) {
             rec = recordDAO.getRecordByClientID(clientID);
             if(rec == null) {
                 rec = new Record();
             }
         } else {
             rec = recordDAO.getRecord(recordPk);
         }
         
 		
 		String latitudeString = getJSONString(jsonRecordBean, "latitude", "");
         Double latitude = latitudeString.trim().isEmpty() ? null : Double.parseDouble(latitudeString);
 		String longitudeString = getJSONString(jsonRecordBean, "longitude", "");
         Double longitude = longitudeString.trim().isEmpty() ? null : Double.parseDouble(longitudeString);
 		if (latitude != null && longitude != null) {
 			rec.setPoint(locationService.createPoint(latitude, longitude));
 		}
 
 		String accuracyStr = getJSONString(jsonRecordBean, "accuracy", "");
         Double accuracy = accuracyStr.trim().isEmpty() ? null : Double.parseDouble(accuracyStr);
 		rec.setAccuracyInMeters(accuracy);
 		
 		//set location for record if exists
 		Integer locationId = getJSONInteger(jsonRecordBean, "location", null);
 		if (locationId != null) {
 			Location l = locationDAO.getLocation(locationId.intValue());
 			rec.setLocation(l);
 		}
 		
 		//set other dwc values
         Date when = getJSONDate(jsonRecordBean, "when", null);
         rec.setWhen(when);
         rec.setTime(when == null ? null : when.getTime());
         
         Date lastDate = getJSONDate(jsonRecordBean, "lastDate", null);
         rec.setLastDate(lastDate == null ? when : lastDate);
         
         Long lastTime = null;
         if (lastDate == null) {
         	if (when != null) {
         		lastTime = when.getTime();
         	}
         } else {
 	        lastTime = lastDate.getTime();
         }
         rec.setLastTime(lastTime);
         
         String notes = getJSONString(jsonRecordBean, "notes", null);
         rec.setNotes(notes);
         
         Integer number = getJSONInteger(jsonRecordBean, "number", null);
         rec.setNumber(number);
         
         Integer censusMethodPk = getJSONInteger(jsonRecordBean, "censusMethod_id", null);
         if(censusMethodPk != null) {
             rec.setCensusMethod(censusMethodDAO.get(censusMethodPk));
         }
         
         Integer surveyPk = getJSONInteger(jsonRecordBean, "survey_id", null);
         
         String scientificName = getJSONString(jsonRecordBean, "scientificName", null);
         Integer taxonPk = getJSONInteger(jsonRecordBean, "taxon_id", null);
         if(taxonPk != null) {
             IndicatorSpecies taxon = taxaDAO.getIndicatorSpecies(taxonPk);
             if(taxon == null) {
                 // Must be a field species
                 taxon = taxaService.getFieldSpecies();
                 AttributeValue fieldName = new AttributeValue();
                 fieldName.setStringValue(scientificName);
                 fieldName.setAttribute(taxaService.getFieldNameAttribute());
                 fieldName = recordDAO.saveAttributeValue(fieldName);
                 rec.getAttributes().add(fieldName);
             }
             rec.setSpecies(taxon);
         }
         
         rec.setUser(user);
         if(surveyPk != null) {
 	        rec.setSurvey(surveyDAO.getSurvey(surveyPk));
 	    }
 
         rec = recordDAO.saveRecord(rec);
         
         Map<String, Object> map = new HashMap<String, Object>();
         map.put("id", clientID);
         map.put("server_id", rec.getId().intValue());
         map.put("klass", Record.class.getSimpleName());
         
         syncResponseList.add(map);
         
         List<Object> recAttrBeanList = (List<Object>) PropertyUtils.getProperty(jsonRecordBean, "attributeValues");
         for(Object jsonRecAttrBean : recAttrBeanList) { 
             AttributeValue recAttr = syncRecordAttribute(syncResponseList, jsonRecAttrBean);
             rec.getAttributes().add(recAttr);
         }
         
         // Save the client ID.
         Metadata md = recordDAO.getRecordMetadataForKey(rec, Metadata.RECORD_CLIENT_ID_KEY);
         md.setValue(clientID);
         md = metadataDAO.save(md);
         rec.getMetadata().add(md);
         
         recordDAO.saveRecord(rec);
     }
     
     private AttributeValue syncRecordAttribute(List<Map<String, Object>> syncResponseList, Object jsonRecAttrBean) 
         throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException {
         String id = getJSONString(jsonRecAttrBean, "id", null);
         if(id == null) {
             throw new NullPointerException();
         }
         
         Integer recAttrPk = getJSONInteger(jsonRecAttrBean, "server_id", 0);
         Integer attrPk = getJSONInteger(jsonRecAttrBean, "attribute_id", null);
         String value = getJSONString(jsonRecAttrBean, "value", "");
         
         Attribute attr = taxaDAO.getAttribute(attrPk);
         
         AttributeValue recAttr = recAttrPk < 1 ? new AttributeValue() : recordDAO.getAttributeValue(recAttrPk);
         recAttr.setAttribute(attr);
         String filename = null;
         String base64 = null;
         switch(attr.getType()) {
             case INTEGER:
             case INTEGER_WITH_RANGE:
             case DECIMAL:
                 recAttr.setStringValue(value);
                 if(value != null && !value.isEmpty()) {
                     recAttr.setNumericValue(new BigDecimal(value));
                 }
                 break;
             
             case DATE:
                 Date date = getJSONDate(jsonRecAttrBean, "value", null);
                 if(date != null) {
                     recAttr.setDateValue(date);
                     recAttr.setStringValue(dateFormat.format(date));
                 } else {
                     recAttr.setDateValue(null);
                     recAttr.setStringValue("");
                 }
                 break;
             case HTML:
             case HTML_NO_VALIDATION:
             case HTML_COMMENT:
             case HTML_HORIZONTAL_RULE:
             case STRING:
             case STRING_AUTOCOMPLETE:
             case TEXT:
             case TIME:
             case STRING_WITH_VALID_VALUES:
             case MULTI_CHECKBOX:
             case MULTI_SELECT:
             case BARCODE:
             case REGEX:
                 recAttr.setStringValue(value);
                 break;
             case SINGLE_CHECKBOX:
             	recAttr.setBooleanValue(value);
             	break;
             case IMAGE:
                 if(value != null && !value.isEmpty()) {
                     base64 = value;
                     // The mobile only uploads jpeg images.
                     filename = String.format("%s.jpeg",UUID.randomUUID().toString());
                     recAttr.setStringValue(filename);
                 } else {
                     filename = null;
                     base64 = null;
                     recAttr.setStringValue("");
                 }
                 break;
                 
             case FILE:
                 log.error("File (Record) Attribute Type is not supported.");
                 break;
             default:
                 throw new UnsupportedOperationException("Unsupported Attribute Type: "+attr.getType().toString());
         }
         recAttr = recordDAO.saveAttributeValue(recAttr);
         
         if(filename != null && base64 != null) {
             fileService.createFile(recAttr.getClass(), recAttr.getId(), filename, Base64.decode(base64));
         }
         
         Map<String, Object> map = new HashMap<String, Object>();
         map.put("id", id);
         map.put("server_id", recAttr.getId().intValue());
         map.put("klass", AttributeValue.class.getSimpleName());
         
         syncResponseList.add(map);
         return recAttr;
     }
     
     private Integer getJSONInteger(Object bean, String propertyName, Integer defaultValue) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
         Integer ret = defaultValue;
         Object obj = PropertyUtils.getProperty(bean, propertyName);
         
         if(obj != null) {
             try {
                 ret = Integer.parseInt(obj.toString(), 10);
             } catch(NumberFormatException nfe) {
                 ret = defaultValue;
             }
         }
         
         return ret;
     }
     
     private String getJSONString(Object bean, String propertyName, String defaultValue) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
         Object obj = PropertyUtils.getProperty(bean, propertyName);
         return obj == null ? defaultValue : obj.toString();
     }
     
     private Date getJSONDate(Object bean, String propertyName, Date defaultValue) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
         Date ret = defaultValue;
         Object obj = PropertyUtils.getProperty(bean, propertyName);
         
         if(obj != null) {
             try {
                 ret = new Date(Long.parseLong(obj.toString(), 10));
             } catch(NumberFormatException nfe) {
                 ret = defaultValue;
             }
         }
         
         return ret;
     }
     
     private String jsonStringEscape(String str) {
         return str == null ? str : str.replaceAll("\"", "\\\\\"");
     }
     
     private Map<String, Object> recurseFlattenCensusMethod(CensusMethod method) {
         // Not using the depth because I do not want all the sub census methods.
         Map<String, Object> flatCensusMethod = method.flatten(0, true, true);
         
         List<Map<String, Object>> attributeList = new ArrayList<Map<String, Object>>();
         for(Attribute attr : method.getAttributes()) {
             attributeList.add(attr.flatten(1, true, true));
         }
         flatCensusMethod.put("attributes", attributeList);
         
         List<Map<String, Object>> subCensusMethodList = new ArrayList<Map<String, Object>>();
         for(CensusMethod subMethod : method.getCensusMethods()) {
             subCensusMethodList.add(recurseFlattenCensusMethod(subMethod));
         }
         flatCensusMethod.put("censusMethods", subCensusMethodList);
         
         return flatCensusMethod;
     }
 }
