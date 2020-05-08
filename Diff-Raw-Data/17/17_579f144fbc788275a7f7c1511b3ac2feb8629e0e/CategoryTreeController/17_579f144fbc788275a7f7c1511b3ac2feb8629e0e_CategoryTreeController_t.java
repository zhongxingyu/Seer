 package com.vroozi.categorytree.controller;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.vroozi.categorytree.model.CategoryNode;
 import com.vroozi.categorytree.service.CategoryTreeService;
 import com.vroozi.categorytree.utils.StringUtils;
 
 /**
  * 
  * @author Mamoon Habib
  * 
  */
 @Controller
 public class CategoryTreeController {
 
 	public final static Logger LOG = LoggerFactory.getLogger(CategoryTreeController.class);
 
 	@Autowired
 	CategoryTreeService categoryTreeService;
 
 	@RequestMapping(value = "/category-tree/cvgroup/company/{unitid}/groupid/{groupid}/name/{name}/cgrouptoken/{cGroupToken}/status/{status}", method = RequestMethod.POST)
 	public void addUpdateContentViewGroup(@PathVariable("groupid")String groupId, @PathVariable("unitid")String unitId, @PathVariable("name")String name, @PathVariable("cGroupToken")String token, @PathVariable("status")Boolean active) {
 		
 		categoryTreeService.addUpdateContentViewGroup(groupId, unitId, name, token, active);
 	}
 
 	@RequestMapping(value = "/category-tree/cvgroup/goupids/{goupids}/status/{status}", method = RequestMethod.PUT)
 	public void updateProfileGroups(@PathVariable("goupids") String groupIds, @PathVariable("status") Boolean active) {
 		List<String> groupIdList = StringUtils.toList(groupIds);
 		categoryTreeService.updateProfileGroups(groupIdList, active);
 	}
 	
 	@RequestMapping(value = "/category-tree/cvgroup/goupids/{goupids}", method = RequestMethod.DELETE)
 	public void deleteContentViewGroup(@PathVariable("goupids") String groupIds) {
 
 		List<String> groupIdList = StringUtils.toList(groupIds);
 		categoryTreeService.deleteContentViewGroup(groupIdList);
 	}
 
 	@RequestMapping(value = "/category-tree/cview/cviewid/{cviewid}/cvname/{cvname}/status/{status}", method = RequestMethod.POST)
 	void addUpdateContentView(@PathVariable("cviewid") String contentViewId, @PathVariable("cvname") String cvname,
 			@PathVariable("status") Boolean active) {
 		categoryTreeService.addUpdateContentView(contentViewId, cvname, active);
 	}
 	
 	@RequestMapping(value = "/category-tree/cview/cviewids/{cviewids}/status/{status}", method = RequestMethod.PUT)
 	void updateContentView(@PathVariable("cviewids") String contentViewIds, @PathVariable("status") Boolean active) {
 		List<String> contentViewIdList = StringUtils.toList(contentViewIds);
 		categoryTreeService.updateContentViews(contentViewIdList, active);
 	}
 	
 	@RequestMapping(value = "/category-tree/cview/company/{unitid}/cvgroupid/{cvgroupid}/cviewid/{cviewid}/cvname/{cvname}", method = RequestMethod.POST)
 	public void addContentView(@PathVariable("unitid") String unitId,
 			@PathVariable("cvgroupid") String contentViewGroupId, 
 			@PathVariable("cviewid") String contentViewId,
 			@PathVariable("cvname") String contenViewName, @RequestBody List<String> catalogIds) {
 		
 		categoryTreeService.addContentView(unitId, contentViewGroupId, contentViewId, contenViewName, catalogIds);
 	}
 
 	@RequestMapping(value = "/category-tree/cview/cviewids/{cviewids}", method = RequestMethod.DELETE)
 	public void removeContentViews(@PathVariable("cviewids")String cviewIds) {
 		List<String> cviewList = StringUtils.toList(cviewIds);
 		categoryTreeService.removeContentViews(cviewList);
 	}
 
 	@RequestMapping(value = "/category-tree/cview/link/cviewgroup/cviewid/{cviewid}/cvgroupids/{cvgroupids}", method = RequestMethod.PUT)
 	public void addConentViewGroupLink(@PathVariable("cviewid") String cviewId, @PathVariable("cvgroupids") String groupIds) {
 		
 		List<String> cvgoupList = StringUtils.toList(groupIds);
 		categoryTreeService.addContentViewToGroups(cviewId, cvgoupList);
 	}
 
 	@RequestMapping(value = "/category-tree/cview/ulink/cviewgroup/cviewid/{cviewid}/cvgroupids/{cvgroupids}", method = RequestMethod.PUT)
 	public void removeConentViewGroupLink(@PathVariable("cviewid") String cviewId, @PathVariable("cvgroupids") String groupIds) {
 		
 		List<String> cvgoupList = StringUtils.toList(groupIds);
 		categoryTreeService.removeContentViewFromGroups(cviewId, cvgoupList);
 	}
 
 	@RequestMapping(value = "/category-tree/cview/link/company/{unitid}/catalogid/{catalogid}/cviewids/{cviewids}", method = RequestMethod.PUT)
 	public void addCatalogToConentViews(@PathVariable("unitid") String unitId, @PathVariable("catalogid") String catalogId, @PathVariable("cviewids") String cviewids) {
 		
 		List<String> cvgoupList = StringUtils.toList(cviewids);
 		categoryTreeService.addCatalogToConentViews(unitId, catalogId, cvgoupList);
 	}
 
 	@RequestMapping(value = "/category-tree/cview/ulink/company/{unitid}/catalogid/{catalogid}/cviewids/{cviewids}", method = RequestMethod.PUT)
 	public void removeCatalogFromConentViews(@PathVariable("unitid") String unitId, @PathVariable("catalogid") String catalogId, @PathVariable("cviewids") String cviewids) {
 		
 		List<String> cvgoupList = StringUtils.toList(cviewids);
 		categoryTreeService.removeCatalogFromConentViews(unitId, catalogId, cvgoupList);
 	}
 
	@RequestMapping(value = "/category-tree/catalog/company/{unitid}/catalogids/{catalogids}", method = RequestMethod.POST, consumes = "application/json")
 	public void addCatalog(@PathVariable("unitid")String unitId, @PathVariable("catalogids") String catalogIds, @RequestBody Map<String, Integer> catalogMatGroups) {
 		List<String> catalogIdList = StringUtils.toList(catalogIds);
 		categoryTreeService.addCatalog(unitId, catalogIdList, catalogMatGroups);
 	}
 
 //	@RequestMapping(value = "/category-tree/catalog/company/{unitid}", method = RequestMethod.PUT)
 //	public void updateCatalog(@PathVariable("unitid")String unitId, @RequestBody CatalogPost catalogPost) {
 //		
 //		categoryTreeService.updateCatalog(oldMatGroups, matGroups,
 //				contentViewIds);
 //	}
 
	@RequestMapping(value = "/category-tree/catalog/delete/company/{unitid}/catalogid/{catalogid}", method = RequestMethod.POST, consumes = "application/json")
 	public void deleteCatalog(@PathVariable("unitid")String unitId, @PathVariable("catalogid") String catalogId, @RequestBody Map<String, Integer> catalogMatGroups) {
 		
 		categoryTreeService.deleteCatalog(unitId, catalogId, catalogMatGroups);
 	}
 
 	@RequestMapping(value = "/category-tree/upload/company/{unitid}", method = RequestMethod.POST)
 	public void uploadCategories(@PathVariable("unitid")String unitId, @RequestBody String baseFile) {
 		
 		categoryTreeService.uploadCategories(unitId, baseFile);
 	}
 
 	@RequestMapping(value = "/category-tree/company/{unitid}/cgrouptoken/{cgrouptoken}", method = RequestMethod.GET, produces = "application/json")
 	public @ResponseBody
 	Collection<CategoryNode> getSearchResultsBasedOnUnitIdAndCGroupToken(
 			@PathVariable("unitid") String unitId,
 			@PathVariable("cgrouptoken") String cGroupToken) {
 
 		if (unitId == null || unitId.isEmpty() || cGroupToken == null || cGroupToken.isEmpty()) {
 			LOG.error("Incomplete information provided. Unit Id, Content View Group Token must be provided for category tree!");
 			return null;
 		}
 
 		Collection<CategoryNode> categoryMappings = categoryTreeService.getCategoryTree(unitId,
 				cGroupToken);
 
 		return categoryMappings;
 	}
 }
