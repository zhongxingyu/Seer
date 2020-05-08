 package au.com.sequation.sensis.web;
 
 import au.com.sequation.sensis.model.DigitalDisplayEntry;
 import au.com.sequation.sensis.model.data.AllDigitalDisplays;
 import au.com.sequation.sensis.model.tab.ContactTab;
 import au.com.sequation.sensis.model.tab.FindUsTab;
 import au.com.sequation.sensis.model.tab.Tab;
 import com.google.common.base.Predicate;
 import com.google.common.collect.FluentIterable;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller
 @RequestMapping("/business-listing-hash/*")
 public class BusinessListingHashController
 {
     private static final AllDigitalDisplays ALL_DIGITAL_DISPLAY_ENTRIES = AllDigitalDisplays.allDigitalDisplays();
 
     private static final String BASE_URL_PREFIX = "/business-listing-hash/";
 
     private static final String LOCATION_NO_CATEGORY = "";
     private static final String LOCATION_NO_SUBURB = "";
     private static final String LOCATION_NO_STATE = "";
     private static final String LOCATION_NO_CONTACT_ID = "";
 
 	@RequestMapping(value="{contentName}-{contentId}", method=RequestMethod.GET)
 	public String defaultView(Model model,
                               @PathVariable String contentName, @PathVariable String contentId) {
         DigitalDisplayEntry dde = ALL_DIGITAL_DISPLAY_ENTRIES.byContentId(contentId);
 
         TabLocation tabLocation = getDefaultTabCategory(dde);
         model.addAttribute("location", new Location(new PrimaryId(BASE_URL_PREFIX, contentName, contentId, LOCATION_NO_SUBURB, LOCATION_NO_STATE),
                 tabLocation,
                 tabLocation,
                 resolveFindUsTab(dde, tabLocation).getTabId(),
                 new Region(LOCATION_NO_SUBURB, LOCATION_NO_STATE, 0),
                 LOCATION_NO_CONTACT_ID));
 
         return render(dde, model);
 	}
 
 	@RequestMapping(value="{contentName}-{contentId}/{suburb}-{state}", method=RequestMethod.GET)
 	public String locationView(Model model,
                                @PathVariable String contentName, @PathVariable String contentId,
                                @PathVariable String suburb, @PathVariable String state) {
         DigitalDisplayEntry dde = ALL_DIGITAL_DISPLAY_ENTRIES.byContentId(contentId);
 
         TabLocation defaultTabCategory = getDefaultTabCategory(dde);
         TabLocation tabLocation = resolveFindUsTab(dde, defaultTabCategory);
         model.addAttribute("location", new Location(new PrimaryId(BASE_URL_PREFIX, contentName, contentId, suburb, state),
                 tabLocation,
                 defaultTabCategory,
                 resolveFindUsTab(dde, tabLocation).getTabId(),
                 new Region(suburb, state, 17),
                 LOCATION_NO_CONTACT_ID));
 
         return render(dde, model);
 	}
 
     /* *** private *** */
 
     private TabLocation resolveFindUsTab(DigitalDisplayEntry dde, TabLocation defaultTabLocation) {
         Tab tab = FluentIterable.from(dde.getTabs()).firstMatch(new Predicate<Tab>()
         {
             @Override
             public boolean apply(Tab tab)
             {
                 return FindUsTab.class.isAssignableFrom(tab.getClass());
             }
         }).orNull();
        return new TabLocation(tab != null ? tab.getId() : "none", LOCATION_NO_CATEGORY);
     }
 
     private TabLocation getDefaultTabCategory(DigitalDisplayEntry dde) {
         ContactTab firstTab = (ContactTab) dde.getTabs().get(0);
         return new TabLocation(firstTab.getId(), firstTab.getCategories().get(0).getId());
     }
 
     private String render(DigitalDisplayEntry dde, Model model) {
         model.addAttribute("dde", dde);
         return "views/businessListingHash";
     }
 
 }
