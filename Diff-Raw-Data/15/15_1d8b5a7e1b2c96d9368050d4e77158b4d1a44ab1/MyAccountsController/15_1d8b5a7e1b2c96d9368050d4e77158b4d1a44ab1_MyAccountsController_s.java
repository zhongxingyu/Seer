 package com.orangeleap.tangerine.json.controller;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpSession;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import com.orangeleap.tangerine.domain.Person;
 import com.orangeleap.tangerine.domain.paymentInfo.Gift;
 import com.orangeleap.tangerine.service.ConstituentService;
 import com.orangeleap.tangerine.service.GiftService;
 import com.orangeleap.tangerine.service.SiteService;
 import com.orangeleap.tangerine.util.TangerineUserHelper;
 
 /**
  * Controller used by the sidebar to get the accounts for
  * which the current user is the Account Manager
  * @version 1.0
  */
 @Controller
 public class MyAccountsController {
 
     @Resource(name="siteService")
     private SiteService siteService;
 
     @Resource(name="constituentService")
     private ConstituentService constituentService;
 
     @Resource(name="giftService")
     private GiftService giftService;
     
     @Resource(name = "tangerineUserHelper")
     private TangerineUserHelper tangerineUserHelper;
 
     @SuppressWarnings("unchecked")
 	@RequestMapping("/myAccounts.json")
     public ModelMap getAllAccounts(HttpSession session) {
 
         List<Map> response = new ArrayList<Map>();
 
         siteService.createSiteAndUserIfNotExist(tangerineUserHelper.lookupUserSiteName());
         Person constituent = constituentService.readConstituentById(tangerineUserHelper.lookupUserId());
         if (constituent == null) {
             return new ModelMap();
         }
         
         String acctString = constituent.getCustomFieldValue("individual.accountManagerFor");
 
         if (acctString != null) {
             String[] accounts = acctString.split(",");
 
             for (String account : accounts) {
 
                 Long acctId = Long.parseLong(account);
                 Person client = constituentService.readConstituentById(acctId);
 
                 BigDecimal totalGiving = new BigDecimal(0);
 
                 List<Gift> giftList = giftService.readMonetaryGifts(client.getId());
                 for (Gift gft : giftList) {
                     totalGiving = totalGiving.add(gft.getAmount() == null ? BigDecimal.ZERO : gft.getAmount());
                 }
 
 
                 response.add(fromConstituent(client, totalGiving, giftList.size()));
             }
         }
 
         ModelMap model = new ModelMap("data", response);
 
         return model;
     }
 
     @SuppressWarnings("unchecked")
     private Map fromConstituent(Person constituent, BigDecimal amount, int gifts) {
 
         Map<String,Object> ret = new HashMap<String,Object>();
         ret.put("id", constituent.getId());
         ret.put("first", constituent.getFirstName());
         ret.put("last", constituent.getLastName());
         ret.put("orgName", constituent.getOrganizationName());
         ret.put("majorDonor", constituent.isMajorDonor());
         ret.put("lapsedDonor", constituent.isLapsedDonor());
         ret.put("amount", amount);
         ret.put("gifts", gifts);
         return ret;
     }
 
 
 }
