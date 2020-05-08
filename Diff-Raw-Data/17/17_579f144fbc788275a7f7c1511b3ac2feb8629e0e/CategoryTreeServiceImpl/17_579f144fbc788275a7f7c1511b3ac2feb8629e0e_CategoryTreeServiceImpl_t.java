 /**
  * 
  */
 package com.vroozi.categorytree.service;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.neo4j.conversion.EndResult;
 import org.springframework.data.neo4j.template.Neo4jOperations;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.vroozi.categorytree.model.Category;
 import com.vroozi.categorytree.model.CategoryNode;
 import com.vroozi.categorytree.model.ContentView;
 import com.vroozi.categorytree.model.ContentViewGroup;
 import com.vroozi.categorytree.processing.CategoryTreeProcess;
 import com.vroozi.categorytree.repository.CategoryRepository;
 import com.vroozi.categorytree.repository.ContentViewGroupRepository;
 import com.vroozi.categorytree.repository.ContentViewRepository;
 import com.vroozi.categorytree.service.rest.CatalogService;
 import com.vroozi.categorytree.utils.CategoryParser;
 
 /**
  * @author Mamoon Habib
  *
  */
 @Service
 public class CategoryTreeServiceImpl implements CategoryTreeService {
 
     @Autowired
     private Neo4jOperations neoTemplate;
 
     @Autowired
     private ContentViewGroupRepository cvGroupRepository;
     
 	@Autowired
 	private ContentViewRepository cvRepository;
 	
 	@Autowired
 	private CategoryRepository categoryRepository;
 	
 	@Autowired
 	CategoryTreeProcess categoryTreeProcess;
 	
 	@Autowired
 	CatalogService catalogService;
 	
 	@Override
 	public void addUpdateContentViewGroup(String groupId, String unitId, String name,
 			String token, Boolean active) {
 		ContentViewGroup cvgroup = cvGroupRepository.findByContentViewGroupId(groupId);
 		if(cvgroup == null) {
 			cvgroup = new ContentViewGroup();
 		}
 		cvgroup.setContentViewGroupId(groupId);
 		cvgroup.setUnitId(unitId);
 		cvgroup.setName(name);
 		cvgroup.setToken(token);
 		cvgroup.setActive(active);
 		cvGroupRepository.save(cvgroup);
 	}
 	
 	@Override
 	public void updateProfileGroups(List<String> groupIds, Boolean active) {
 		for (String groupId : groupIds) {
 			ContentViewGroup cvgroup = cvGroupRepository.findByContentViewGroupId(groupId);
 			cvgroup.setActive(active);
 			cvGroupRepository.save(cvgroup);
 		}
 	}
 	
 	@Override
 	public void deleteContentViewGroup(List<String> groupIds) {
 		for (String groupId : groupIds) {
 			ContentViewGroup cvgroup = cvGroupRepository.findByContentViewGroupId(groupId);
 //			ContentViewGroup cvgroup = cvGroupRepository.findByPropertyValue("contentViewGroupId", id);
 			cvGroupRepository.delete(cvgroup);
 		}
 	}
 
 	@Override
 	public void addUpdateContentView(String contentViewId, String name, Boolean active) {
 		ContentView contentView = cvRepository.findByContentViewId(contentViewId);
 		if(contentView==null) {
 			contentView = new ContentView();
 			contentView.setContentViewId(contentViewId);
 		}
		contentView.setName(name);
		contentView.setActive(active);
		contentView = cvRepository.save(contentView);
 	}
 	
 	@Override
 	public void updateContentViews(List<String> contentViewIds, Boolean active) {
 		for (String contentViewId : contentViewIds) {
 			ContentView contentView = cvRepository.findByContentViewId(contentViewId);
 			if(contentView!=null) {
 				contentView.setActive(active);
 				cvRepository.save(contentView);
 			}
 		}
 	}
 	@Override
 	public void removeContentViews(List<String> cviewIds) {
 		for (String cviewId : cviewIds) {
 			ContentView contentView = cvRepository.findByContentViewId(cviewId);
 			cvRepository.delete(contentView);
 		}
 	}
 
 	@Override
 	@Transactional
 	public void addContentViewToGroups(String contentViewId, List<String> groupIds) {
 		ContentView contentView = cvRepository.findByContentViewId(contentViewId);
 		for (String groupId : groupIds) {
 			ContentViewGroup cvgroup = cvGroupRepository.findByContentViewGroupId(groupId);
 			cvgroup.getContentViews().add(contentView);
 			cvGroupRepository.save(cvgroup);
 		}
 	}
 
 	@Override
 	@Transactional
 	public void removeContentViewFromGroups(String contentViewId,
 			List<String> groupIds) {
 		ContentView contentView = cvRepository.findByContentViewId(contentViewId);
 		for (String groupId : groupIds) {
 			ContentViewGroup cvgroup = cvGroupRepository.findByContentViewGroupId(groupId);
 			cvgroup.getContentViews().remove(contentView);
 			cvGroupRepository.save(cvgroup);
 		}
 	}
 
 	@Override
 	@Transactional
 	public void addCatalogToConentViews(String unitId, String catalogId,
 			List<String> contentViewIds) {
 
 		List<String> catalogIds = new ArrayList<String>(1);
 		catalogIds.add(catalogId);
 		Map<String, Integer> matGroups = catalogService.getMatGroupsByCatalogIds(catalogIds);
 		if(matGroups!=null && matGroups.size()>0) {
 			for (String contentViewId : contentViewIds) {
 				ContentView contentView = cvRepository.findByContentViewId(contentViewId);
 				linkContentViewCategories(unitId, contentView, matGroups);
 			}
 		}
 	}
 
 	@Override
 	@Transactional
 	public void removeCatalogFromConentViews(String unitId, String catalogId,
 			List<String> contentViewIds) {
 		
 		List<String> catalogIds = new ArrayList<String>(1);
 		catalogIds.add(catalogId);
 		Map<String, Integer> matGroups = catalogService.getMatGroupsByCatalogIds(catalogIds);
 		if(matGroups!=null && matGroups.size()>0) {
 			for (String contentViewId : contentViewIds) {
 				ContentView contentView = cvRepository.findByContentViewId(contentViewId);
 				unlinkContentViewCategories(unitId, contentView, matGroups);
 			}
 		}
 	}
 
 	@Override
 	@Transactional
 	public void addContentView(String unitId, String contentViewGroupId, String contentViewId,
 			String contenViewName, List<String> catalogIds) {
 		ContentView contentView = cvRepository.findByContentViewId(contentViewId);
 		if(contentView==null) {
 			contentView = new ContentView();
 			contentView.setContentViewId(contentViewId);
 			contentView.setName(contenViewName);
 			contentView = cvRepository.save(contentView);
 		}
 		
 		ContentViewGroup cvGroup = cvGroupRepository.findByContentViewGroupId(contentViewGroupId);
 		cvGroup.getContentViews().add(contentView);
 		cvGroupRepository.save(cvGroup);
 		
 		Map<String, Integer> matGroups = catalogService.getMatGroupsByCatalogIds(catalogIds);
 		if(matGroups!=null && matGroups.size()>0) {
 			linkContentViewCategories(unitId, contentView, matGroups);
 		}
 	}
 
 	private void linkContentViewCategories(String unitId, ContentView contentView, Map<String, Integer> matGroups) {
 		EndResult<Category> categories = categoryRepository.findAllByPropertyValue("unitId", unitId);
 		for (Category category : categories) {
 			if(mapCategory(category, matGroups)) {
 				contentView.addCategoryMapping(neoTemplate, category);
 			}
 		}
 		contentView = cvRepository.save(contentView);
 	}
 
 	private void unlinkContentViewCategories(String unitId, ContentView contentView, Map<String, Integer> matGroups) {
 		EndResult<Category> categories = categoryRepository.findAllByPropertyValue("unitId", unitId);
 		for (Category category : categories) {
 			if(mapCategory(category, matGroups)) {
 				contentView.removeCategoryMapping(neoTemplate, category);
 			}
 		}
 		contentView = cvRepository.save(contentView);
 	}
 	
 	@Override
 	@Transactional
 	public void addCatalog(String unitId, List<String> catalogIds, Map<String, Integer> matGroups) {
 		List<String> contentViewIds = catalogService.getContentViewIdsByCatalogIds(unitId, catalogIds); 
 		EndResult<Category> categories = categoryRepository.findAllByPropertyValue("unitId", unitId);
 		for (Category category : categories) {
 			if(mapCategory(category, matGroups)) {
 				for (String contentViewId : contentViewIds) {
 					ContentView contentView = cvRepository.findByContentViewId(contentViewId);
 					if(contentView != null) {
 						contentView.addCategoryMapping(neoTemplate, category);
 						contentView.setName(contentView.getName()+"+++++add");
 						cvRepository.save(contentView);
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	@Transactional
 	public void deleteCatalog(String unitId, String catalogId, Map<String, Integer> oldMatGroups) {
 		List<String> catalogIds = new ArrayList<String>(1);
 		catalogIds.add(catalogId);
 		List<String> contentViewIds = catalogService.getContentViewIdsByCatalogIds(unitId, catalogIds);
 		
 		List<ContentView> contentViews = new ArrayList<ContentView>(contentViewIds.size());
 		for (String contentViewId : contentViewIds) {
 			ContentView contentView = cvRepository.findByContentViewId(contentViewId);
 			contentViews.add(contentView);
 		}
 		
 		EndResult<Category> categories = categoryRepository.findAllByPropertyValue("unitId", unitId);
 		for (Category category : categories) {
 			if(mapCategory(category, oldMatGroups)) {
 				for (ContentView contentView : contentViews) {
 					contentView.removeCategoryMapping(neoTemplate, category);
 					cvRepository.save(contentView);
 				}
 			}
 		}
 	}
 
 	@Override
 	@Transactional
 	public void uploadCategories(String unitId, String baseFile) {
 		EndResult<Category> categories = categoryRepository.findAllByPropertyValue("unitId", unitId);
 		categoryRepository.delete(categories);
 		categoryTreeProcess.processCategories(unitId, baseFile);
 		updateCategoriesPath(unitId);
 	}
 
 	public void updateCategoriesPath(String unitId) {
 		List<String> catalogIds = catalogService.getLiveCatalogIds(unitId);
 		if(catalogIds!=null && catalogIds.size()>0) {
 			Map<String, Integer> catalogMatGroups = catalogService.getMatGroupsByCatalogIds(catalogIds);
 			addCatalog(unitId, catalogIds, catalogMatGroups);
 		}
 	}
 
 	@Override
 	@Transactional
 	public Collection<CategoryNode> getCategoryTree(String unitId, String contentViewGroupToken) {
 		List<Category> categories = categoryRepository.getCategories(contentViewGroupToken);
 		Map<Long, CategoryNode> rootNodes = new HashMap<Long, CategoryNode>();
 		for (Category category : categories) {
 			saveNode(rootNodes, category);
 		}
 		
 		return rootNodes.values();
 	}
 
 	private CategoryNode saveNode(Map<Long, CategoryNode> rootNodes, Category category) {
 		if(category.getParent()==null) {
 			CategoryNode node = rootNodes.get(category.getId());
 			if(node==null) {
 				node = populateCategoryNode(category);
 			}
 			rootNodes.put(category.getId(), node);
 			return node;
 		} else {
 			Category parent = neoTemplate.fetch(category.getParent());
 			CategoryNode parentNode = saveNode(rootNodes, parent);
 			CategoryNode categoryNode = populateCategoryNode(category);
 			return parentNode.addChild(categoryNode);
 		}
 	}
 
 	public CategoryNode populateCategoryNode(Category category) {
 		CategoryNode node = new CategoryNode();
 		node.setCatalogCategoryCode(category.getCatalogCategoryCode());
 		node.setId(category.getId());
 		node.setCategoryId(category.getCategoryId());
 		node.setNodeId(category.getRow());
 		node.setCatalogCategoryCode(category.getCatalogCategoryCode());
 		node.setCompanyCategoryCode(category.getCompanyCategoryCode());
 		node.setNodeTitle(category.getCompanyLabel());;
 		return node;
 	}
 
 	private boolean mapCategory(Category category, Map<String, Integer> matGroups) {
 		
 		for (Entry<String, Integer> matGroupEntry : matGroups.entrySet()) {
 			if(CategoryParser.matches(category.getCatalogCategoryCode(), matGroupEntry.getKey())) {
 				return true;
 			}
 		}
 		return false;
 	}
 }
