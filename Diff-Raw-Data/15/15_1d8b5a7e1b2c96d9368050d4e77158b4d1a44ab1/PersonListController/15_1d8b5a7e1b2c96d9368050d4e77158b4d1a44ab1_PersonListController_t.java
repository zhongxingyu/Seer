 package com.orangeleap.tangerine.json.controller;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.Resource;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import com.orangeleap.tangerine.domain.Person;
 import com.orangeleap.tangerine.service.ConstituentService;
 import com.orangeleap.tangerine.web.common.SortInfo;
 
 /**
  * @version 1.0
  */
 @Controller
 public class PersonListController {
 
     private final static Map<String, Object> NAME_MAP = new HashMap<String, Object>();
 
     static {
         NAME_MAP.put("id", "CONSTITUENT_ID");
        NAME_MAP.put("accountnumber", "ACCOUNT_NUMBER");
         NAME_MAP.put("last", "LAST_NAME");
         NAME_MAP.put("first", "FIRST_NAME");
         NAME_MAP.put("organization", "ORGANIZATION_NAME");
         NAME_MAP.put("majordonor", "MAJOR_DONOR");
         NAME_MAP.put("lapseddonor", "LAPSED_DONOR");
     }
 
 
     @Resource(name = "constituentService")
     private ConstituentService constituentService;
 
     @SuppressWarnings("unchecked")
     @RequestMapping("/personList.json")
     public ModelMap listConstituents(SortInfo sort) {
 
         List<Map> list = new ArrayList<Map>();
 
         // make sure we only got back a column name we care about
         if (sort.validateSortField(NAME_MAP.keySet())) {
             // replace Sort with the correct column name
             sort.setSort( (String) NAME_MAP.get(sort.getSort()) );
             List<Person> persons = constituentService.readAllConstituentsBySite(sort);
 
             for (Person person : persons) {
                 list.add(personToMap(person));
             }
         }
 
         int count = constituentService.getConstituentCountBySite();
 
         ModelMap map = new ModelMap("rows", list);
         map.put("totalRows", count);
 
         return map;
     }
 
     /*
      * Utility method to map a full Person object onto a Map so we only
      * return the needed fields rather than the full person object
      */
     private Map<String, Object> personToMap(Person constituent) {
 
         // keys should map with the NAME_MAP constant
         Map<String, Object> map = new HashMap<String, Object>();
         map.put("id", constituent.getId());
         map.put("accountNumber", constituent.getAccountNumber());
         map.put("last", constituent.getLastName());
         map.put("first", constituent.getFirstName());
         map.put("organization", constituent.getOrganizationName());
         map.put("majorDonor", constituent.isMajorDonor());
         map.put("lapsedDonor", constituent.isLapsedDonor());
 
         return map;
     }
 
 }
