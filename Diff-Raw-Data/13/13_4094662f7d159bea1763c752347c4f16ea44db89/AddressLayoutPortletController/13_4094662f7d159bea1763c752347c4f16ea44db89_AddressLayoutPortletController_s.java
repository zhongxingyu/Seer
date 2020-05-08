 package org.openmrs.module.addresshierarchy.web.controller.portlet;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
 import org.openmrs.module.addresshierarchy.AddressHierarchyLevel;
 import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
 import org.openmrs.module.addresshierarchy.util.AddressHierarchyUtil;
 import org.springframework.web.servlet.ModelAndView;
 
 public class AddressLayoutPortletController extends org.openmrs.web.controller.layout.AddressLayoutPortletController {
 	
 	protected static final Log log = LogFactory.getLog(AddressLayoutPortletController.class);
 	
 	/** Overrides the handle request to use the custom address hierarchy jsp if in edit mode */
 	
	@SuppressWarnings("unchecked")
     @Override
 	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException,
 	                                                                                           IOException {
 	
 		ModelAndView mav = super.handleRequest(request, response);
 		
 		String originalPortletPath = mav.getViewName();
 		
 		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
 		
 		// we only want to override with our custom page if 1) override is enabled, 2) size="full", 
 		// 3) we aren't in view mode, and 4) we have defined address hierarchy levels
 		try {
			Map map = (Map) mav.getModelMap().get("model");	
 			if (AddressHierarchyUtil.getGlobalPropertyAsBoolean("addresshierarchy.enableOverrideOfAddressPortlet") == true &&
 					map.containsKey("size") && ((String) map.get("size")).equals("full") &&
 					(!map.containsKey("layoutMode") || ((String) map.get("layoutMode")).equals("edit")) &&
 					ahService.getAddressHierarchyLevelsCount() > 0) {
 				
 				// get the ordered address hierarchy levels and add them to the map
 				List<AddressHierarchyLevel> levels = ahService.getOrderedAddressHierarchyLevels(false);
 				mav.getModelMap().addAttribute("hierarchyLevels", levels);
 				
 				// figure out at what point we need to switch to free text entry by iterating backwards
 				// through the levels until we find a level with entries
 				Integer i;
 				for (i = levels.size()-1; i > 0; i--) {
 					List<AddressHierarchyEntry> entries = Context.getService(AddressHierarchyService.class).getAddressHierarchyEntriesByLevel(levels.get(i));
 					if (entries != null && entries.size() > 0) {
 						break;
 					}
 				}
 				mav.getModelMap().addAttribute("switchToFreetext", i+1);
 				
 				// add the global property that specifies whether we should allow freetext entries for levels which have entries
 				mav.getModelMap().addAttribute("allowFreetext", AddressHierarchyUtil.getGlobalPropertyAsBoolean("addresshierarchy.allowFreetext"));
 				
 				// set the path to the custom page
 				String portletPath = "/module/addresshierarchy/portlets/addressLayout";
 				mav.setViewName(portletPath);
 			}
 		}
 		catch (Exception e) {
 			// if we run into an error, we only want to soft-fail and note that we were unable to override the portlet
 			log.error("Unable to override address portlet ", e);
 			mav.setViewName(originalPortletPath);
 		}
 		
 		return mav;
 	}
 }
