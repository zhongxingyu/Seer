 package com.orangeleap.tangerine.controller.relationship;
 
import java.util.Date;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import javax.annotation.Resource;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.validation.BindException;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.SimpleFormController;
 
 import com.orangeleap.tangerine.dao.FieldDao;
 import com.orangeleap.tangerine.domain.AbstractCustomizableEntity;
 import com.orangeleap.tangerine.domain.Person;
 import com.orangeleap.tangerine.domain.customization.ConstituentCustomFieldRelationship;
 import com.orangeleap.tangerine.domain.customization.CustomField;
 import com.orangeleap.tangerine.domain.customization.CustomFieldRelationship;
 import com.orangeleap.tangerine.domain.customization.FieldDefinition;
 import com.orangeleap.tangerine.service.ConstituentCustomFieldRelationshipService;
 import com.orangeleap.tangerine.service.ConstituentService;
 import com.orangeleap.tangerine.service.CustomFieldRelationshipService;
 import com.orangeleap.tangerine.service.RelationshipService;
 import com.orangeleap.tangerine.service.SiteService;
 
 public class RelationshipCustomizeFormController extends SimpleFormController {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
     
 
     @Resource(name="constituentCustomFieldRelationshipService")
     protected ConstituentCustomFieldRelationshipService constituentCustomFieldRelationshipService;
     
     @Resource(name = "constituentService")
     private ConstituentService constituentService;
 
     @Resource(name = "relationshipService")
     private RelationshipService relationshipService;
     
     @Resource(name = "siteService")
     private SiteService siteService;
     
     @Resource(name="customFieldRelationshipService")
     protected CustomFieldRelationshipService customFieldRelationshipService;
 
     @Resource(name = "fieldDAO")
     private FieldDao fieldDao;
  
 
     @Override
     protected Object formBackingObject(HttpServletRequest request) throws Exception {
         return "";
     }
 
 	
     @SuppressWarnings("unchecked")
     @Override
     protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException errors, Map controlModel) throws Exception {
     	return handleRequest(request, false);
     }
     
     
 	@Override
     public ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws ServletException {
     	return handleRequest(request, true);
     }
 
 	
 	protected Map<String, String> getMap(Map<String, CustomField> map) {
 		Map<String, String> result = new TreeMap<String, String>();
 		for (Map.Entry<String, CustomField> entry : map.entrySet()) {
 			result.put(entry.getValue().getName(), entry.getValue().getValue());
 		}
 		return result;
 	}
 	
 	@SuppressWarnings("unchecked")
 	protected Map<String, String> getMap(HttpServletRequest request) {
 		Map map = new TreeMap<String, String>();
 		Enumeration e = request.getParameterNames();
 		while (e.hasMoreElements()) {
 			String parm = (String)e.nextElement();
 			if (parm.startsWith("cfname")) {
 				String fieldnum = parm.substring(6);
 				String key = request.getParameter(parm).trim();
 				String value = request.getParameter("cfvalue"+fieldnum).trim();
 				if (key.length() > 0) map.put(key, value);
 			}
 		}
 		return map;
 	}
 	
 	protected void updateCustomFieldMap(Map<String, String> map, AbstractCustomizableEntity entity) {
 		entity.getCustomFieldMap().clear();
 		for (Map.Entry<String, String> e : map.entrySet()) {
 			entity.setCustomFieldValue(e.getKey(), e.getValue());
 		}
 	}
 	
 	private CustomField getCustomField(List<CustomField> list, Long customFieldId) {
 		Iterator<CustomField> it = list.iterator();
 		while (it.hasNext()) {
 			CustomField cf = it.next();
 			if (cf.getId().equals(customFieldId)) {
 				return cf;
 			}
 		}
 		return null;
 	}
 	
 	private ModelAndView handleRequest(HttpServletRequest request, boolean isSubmit) {
 		
 		Long constituentId = new Long(request.getParameter("personId"));
 		Person person = constituentService.readConstituentById(constituentId);
 		if (person == null) return null;
 		String fieldDefinitionId = request.getParameter("fieldDefinitionId");
 		FieldDefinition fieldDefinition = fieldDao.readFieldDefinition(fieldDefinitionId);
 		String masterfieldDefinitionId = customFieldRelationshipService.getMasterFieldDefinitonId(fieldDefinitionId);
 		Long customFieldId = new Long(request.getParameter("customFieldId"));
 		List<CustomField> savedlist = relationshipService.readCustomFieldsByConstituentAndFieldName(person.getId(), fieldDefinition.getCustomFieldName());
 		CustomField cf = getCustomField(savedlist, customFieldId);
 		
         ConstituentCustomFieldRelationship constituentCustomFieldRelationship = 
         	constituentCustomFieldRelationshipService.readByConstituentFieldDefinitionCustomFieldIds(constituentId, masterfieldDefinitionId, cf.getValue(), cf.getStartDate());
         ConstituentCustomFieldRelationship reverseConstituentCustomFieldRelationship = 
         	constituentCustomFieldRelationshipService.readByConstituentFieldDefinitionCustomFieldIds(new Long(cf.getValue()), masterfieldDefinitionId, constituentId.toString(), cf.getStartDate());
         
        constituentCustomFieldRelationship = init(constituentCustomFieldRelationship, constituentId, masterfieldDefinitionId, cf.getStartDate(), cf.getValue());
        reverseConstituentCustomFieldRelationship = init(reverseConstituentCustomFieldRelationship, new Long(cf.getValue()), masterfieldDefinitionId, cf.getStartDate(), constituentId.toString());
         
         
         // This logic syncs the forward and reverse fields so they show the same on each side.  
         // They could be maintained separately if directionality is required (e.g. "blocked communication" friend).
         if (isSubmit) {
         	
 	        updateCustomFieldMap(getMap(request), constituentCustomFieldRelationship);
 	        constituentCustomFieldRelationship = constituentCustomFieldRelationshipService.maintainConstituentCustomFieldRelationship(constituentCustomFieldRelationship);
 
 	        updateCustomFieldMap(getMap(request), reverseConstituentCustomFieldRelationship);
 	        reverseConstituentCustomFieldRelationship = constituentCustomFieldRelationshipService.maintainConstituentCustomFieldRelationship(reverseConstituentCustomFieldRelationship);
 	        
         } else {
         	
             mergeFieldsFromReverseRelationship(constituentCustomFieldRelationship, reverseConstituentCustomFieldRelationship);
             
         }
         
         
         Map<String, String> stringmap = getMap(constituentCustomFieldRelationship.getCustomFieldMap());
 
 		if (stringmap.size() == 0) stringmap.put("", "");
 		
 		ModelAndView mav = new ModelAndView(getSuccessView());
 		mav.addObject("map", stringmap);
 		mav.addObject("person", person);
 		mav.addObject("fieldDefinition", fieldDefinition);
 		mav.addObject("customField", cf);
 		mav.addObject("constituentCustomFieldRelationship", constituentCustomFieldRelationship);
 		mav.addObject("refvalue", request.getParameter("refvalue"));
         return mav;
 
 	}
 	
	private ConstituentCustomFieldRelationship init(ConstituentCustomFieldRelationship constituentCustomFieldRelationship, Long constituentId, String masterfieldDefinitionId, Date startDate, String cfvalue) {
        if (constituentCustomFieldRelationship == null) {
        	constituentCustomFieldRelationship = new ConstituentCustomFieldRelationship();
        	constituentCustomFieldRelationship.setConstituentId(constituentId);
        	constituentCustomFieldRelationship.setCustomFieldStartDate(startDate);
        	constituentCustomFieldRelationship.setCustomFieldValue(cfvalue);
        	constituentCustomFieldRelationship.setMasterFieldDefinitionId(masterfieldDefinitionId);
        	ensureDefaultFieldsAndValuesExist(constituentCustomFieldRelationship);
        	constituentCustomFieldRelationship = constituentCustomFieldRelationshipService.maintainConstituentCustomFieldRelationship(constituentCustomFieldRelationship);
        } else {
        	ensureDefaultFieldsAndValuesExist(constituentCustomFieldRelationship);
        }
        return constituentCustomFieldRelationship;
	}
	
 	private void mergeFieldsFromReverseRelationship(ConstituentCustomFieldRelationship constituentCustomFieldRelationship, ConstituentCustomFieldRelationship reverseConstituentCustomFieldRelationship) {
 		if (reverseConstituentCustomFieldRelationship == null) return;
 		Iterator<Map.Entry<String, CustomField>> it = reverseConstituentCustomFieldRelationship.getCustomFieldMap().entrySet().iterator();
 		while (it.hasNext()) {
 			CustomField cf = it.next().getValue();
 			constituentCustomFieldRelationship.setCustomFieldValue(cf.getName(), cf.getValue());
 		}
 	}
 	
 	private void clearFieldsOnReverseRelationship(ConstituentCustomFieldRelationship reverseConstituentCustomFieldRelationship) {
 		if (reverseConstituentCustomFieldRelationship == null) return;
     	reverseConstituentCustomFieldRelationship.getCustomFieldMap().clear();
     	constituentCustomFieldRelationshipService.maintainConstituentCustomFieldRelationship(reverseConstituentCustomFieldRelationship);
 	}
 	
 	
     // Add default custom fields and values on constituentCustomFieldRelationship from customFieldRelationship if they dont exist
 	private void ensureDefaultFieldsAndValuesExist(ConstituentCustomFieldRelationship ccr) {
 		CustomFieldRelationship cfr = customFieldRelationshipService.readByFieldDefinitionId(ccr.getMasterFieldDefinitionId());
 		if (cfr == null) return;
 		Iterator<Map.Entry<String, CustomField>> it = cfr.getCustomFieldMap().entrySet().iterator();
 		while (it.hasNext()) {
 			CustomField cf = it.next().getValue();
 			String name = cf.getName();
 			String defaultValue = cf.getValue();
 			boolean blank = defaultValue.equalsIgnoreCase("<blank>");
 			if (blank) defaultValue = "";
 			String value = ccr.getCustomFieldValue(name);
 			if (value == null || value.equals("")) {
 				ccr.setCustomFieldValue(name, defaultValue);
 			}
 		}
 	}
 	    
 	
 }
