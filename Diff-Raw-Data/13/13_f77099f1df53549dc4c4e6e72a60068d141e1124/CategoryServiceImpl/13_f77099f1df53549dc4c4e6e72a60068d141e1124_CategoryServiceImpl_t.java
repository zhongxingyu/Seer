 package org.yarquen.category.impl;
 
 import java.util.Iterator;
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.lang3.text.WordUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.data.mongodb.core.MongoTemplate;
 import org.springframework.data.mongodb.core.query.Criteria;
 import org.springframework.data.mongodb.core.query.Query;
 import org.springframework.stereotype.Service;
 import org.yarquen.article.Article;
 import org.yarquen.category.Category;
 import org.yarquen.category.CategoryBranch;
 import org.yarquen.category.CategoryBranchNode;
 import org.yarquen.category.CategoryRepository;
 import org.yarquen.category.CategoryService;
 import org.yarquen.category.SubCategory;
 
 /**
  * Category service impl
  * 
  * @author Jorge Riquelme Santana
  * @date 23/01/2013
  * @version $Id$
  * 
  */
 @Service
 public class CategoryServiceImpl implements CategoryService {
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(CategoryServiceImpl.class);
 
 	public static String generateCode(String categoryName) {
 		final String temp = StringUtils.stripAccents(categoryName);
 		String code = WordUtils.capitalize(temp);
 		code = code.replaceAll("\\s", "");
 		return code;
 	}
 
 	@Resource
 	private CategoryRepository categoryRepository;
 	@Resource
 	private MongoTemplate mongoTemplate;
 
 	@Override
 	public String addCategory(CategoryBranch categoryBranch, String newCategory) {
 		if (categoryBranch == null || categoryBranch.getNodes().isEmpty()) {
 			throw new IllegalArgumentException(
 					"category branch cannot be null/empty");
 		}
 		Category categoryParent = categoryRepository.findByCode(categoryBranch
 				.getNodes().get(0).getCode());
 		SubCategory subCategory = getLeaf(categoryParent, categoryBranch);
 		SubCategory subCategoryChild = new SubCategory();
 		String code = CategoryServiceImpl.generateCode(newCategory);
 		subCategoryChild.setCode(code);
 		subCategoryChild.setName(newCategory);
 		for (SubCategory sub : subCategory.getSubCategories()) {
 			if (sub.getCode().equals(subCategoryChild.getCode())
 					|| sub.getName().equals(subCategoryChild.getName())) {
 				throw new IllegalArgumentException(newCategory
 						+ " already exists");
 			}
 		}
 
 		subCategory.getSubCategories().add(subCategoryChild);
 		LOGGER.info("{}: {}  added successfull", categoryBranch, newCategory);
 		categoryRepository.save(categoryParent);
 		return code;
 	}
 
 	@Override
 	public void completeCategoryBranchNodeNames(CategoryBranch categoryBranch) {
 		final List<CategoryBranchNode> nodes = categoryBranch.getNodes();
 		if (nodes == null || nodes.isEmpty()) {
 			throw new IllegalArgumentException(
 					"category branch nodes cannot be null/empty");
 		} else {
 			final String categoryCode = nodes.get(0).getCode();
 			final SubCategory category = categoryRepository
 					.findByCode(categoryCode);
 			if (category == null) {
 				throw new RuntimeException("category " + categoryCode
 						+ " not found");
 			} else if (!(category instanceof Category)) {
 				throw new IllegalArgumentException(
 						"category branch root must be of type Category");
 			} else {
 				nodes.get(0).setName(category.getName());
 
 				final Iterator<CategoryBranchNode> iterator = nodes.iterator();
 				// discard element already processed
 				iterator.next();
 
 				completeSubCategory(iterator, category);
 			}
 		}
 	}
 
 	@Override
 	public void deleteCategory(CategoryBranch categoryBranch) {
 
 		if (categoryBranch == null || categoryBranch.getNodes().isEmpty()) {
 			throw new IllegalArgumentException(
 					"category branch cannot be null/empty");
 		}
 
 		Category categoryParent = categoryRepository.findByCode(categoryBranch
 				.getNodes().get(0).getCode());
 		if (categoryParent == null) {
 			throw new IllegalArgumentException("Branch " + categoryBranch
 					+ " not found");
 		}
 
 		if (categoryBranch.getNodes().size() == 1) {
 			// remove root category
 			LOGGER.info("Will remove root category: {}", categoryParent);
 			if (!hasArticlesWith(categoryParent.getCode())) {
 				categoryRepository.delete(categoryParent);
 				LOGGER.info("{} remove successfull", categoryParent.getCode());
 			} else {
 				throw new UnsupportedOperationException(
 						"Cannot remove category because exists articles with "
 								+ categoryParent.getCode() + " category");
 			}
 
 		} else {
 			// remove category leaf
 			CategoryBranchNode leaf = categoryBranch.getNodes().get(
 					categoryBranch.getNodes().size() - 1);
 			categoryBranch.getNodes().remove(leaf);
 			LOGGER.info("Will remove leaf: {} from {}", leaf, categoryBranch);
 			SubCategory subCategory = getLeaf(categoryParent, categoryBranch);
 			SubCategory subCategoryRemoved = null;
 
 			for (SubCategory sub : subCategory.getSubCategories()) {
 				if (sub.getCode().equals(leaf.getCode())) {
 					subCategoryRemoved = sub;
 				}
 			}
 			if (subCategoryRemoved == null) {
 				throw new IllegalArgumentException(categoryBranch + "/"
 						+ leaf.getCode() + " not found");
 			}
 
 			subCategory.getSubCategories().remove(subCategoryRemoved);
 
 			if (!hasArticlesWith(leaf.getCode())) {
 				categoryRepository.save(categoryParent);
 				LOGGER.info("{}: {}  remove successfull", categoryBranch,
 						leaf.getCode());
 			} else {
 				throw new UnsupportedOperationException(
 						"Cannot remove category because exists articles with "
 								+ leaf.getCode() + " category");
 			}
 
 		}
 
 	}
 
 	@Override
 	public SubCategory getLeaf(Category category, CategoryBranch categoryBranch) {
 		final List<CategoryBranchNode> nodes = categoryBranch.getNodes();
 		if (nodes == null || nodes.isEmpty()) {
 			throw new IllegalArgumentException(
 					"category branch nodes cannot be null/empty");
 		} else {
 			if (category == null) {
 				throw new IllegalArgumentException(
 						"category cannot be null/empty");
 			} else if (!(category instanceof Category)) {
 				throw new IllegalArgumentException(
 						"category branch root must be of type Category");
 			} else {
 				nodes.get(0).setName(category.getName());
 
 				final Iterator<CategoryBranchNode> iterator = nodes.iterator();
 				// discard element already processed
 				iterator.next();
 
 				return getLeafSubCategory(iterator, category);
 			}
 		}
 	}
 
 	public boolean hasArticlesWith(String code) {
 		Query query = new Query(Criteria.where("categories").elemMatch(
 				Criteria.where("nodes").elemMatch(
 						Criteria.where("code").is(code))));
 		List<Article> result = mongoTemplate.find(query, Article.class);
 		return !result.isEmpty();
 	}
 
 	@Override
 	public void renameCategory(CategoryBranch categoryBranch, String newNameNode) {
 		if (categoryBranch == null || categoryBranch.getNodes().isEmpty()) {
 			throw new IllegalArgumentException(
 					"category branch cannot be null/empty");
 		}
 		Category categoryParent = categoryRepository.findByCode(categoryBranch
 				.getNodes().get(0).getCode());
 		if (categoryParent == null) {
 			throw new IllegalArgumentException("Branch " + categoryBranch
 					+ " not found");
 		}
 
 		if (categoryBranch.getNodes().size() == 1) {
 			// renaming root category
 			categoryParent.setName(newNameNode);
 			categoryRepository.save(categoryParent);
 			LOGGER.info("{}: {}  renamed successfull", categoryBranch,
 					newNameNode);
 
 		} else {
 
 			// renaming category leaf
 			CategoryBranchNode leaf = categoryBranch.getNodes().get(
 					categoryBranch.getNodes().size() - 1);
 			categoryBranch.getNodes().remove(leaf);
 			LOGGER.info("Will remane leaf: {} with {}", leaf, newNameNode);
 			SubCategory subCategoryParent = getLeaf(categoryParent,
 					categoryBranch);
 			SubCategory subCategoryRenamed = null;
 			for (SubCategory sub : subCategoryParent.getSubCategories()) {
 				if (sub.getName().equals(newNameNode)) {
 					throw new IllegalArgumentException(newNameNode
 							+ " already exists");
 				}
 				if (sub.getCode().equals(leaf.getCode())) {
 					subCategoryRenamed = sub;
 				}
 			}
 			if (subCategoryRenamed == null) {
 				throw new IllegalArgumentException("Leaf " + leaf.getCode()
 						+ " not found");
 			}
 			subCategoryRenamed.setName(newNameNode);
 			LOGGER.info("{}: {}  renamed successfull", categoryBranch + "."
 					+ leaf.getCode(), newNameNode);
 			categoryRepository.save(categoryParent);
 		}
 
 	}
 
 	private void completeSubCategory(Iterator<CategoryBranchNode> iterator,
 			SubCategory parentCategory) {
 		if (iterator.hasNext()) {
 			final CategoryBranchNode node = iterator.next();
 			final List<SubCategory> subCategories = parentCategory
 					.getSubCategories();
 			if (subCategories == null) {
 				throw new RuntimeException(
 						"Invalid branch, no more subCategories from node "
 								+ node.getCode());
 			}
 			for (SubCategory subCategory : subCategories) {
 				if (subCategory.getCode().equals(node.getCode())) {
					node.setName(subCategory.getName(),true);
 					completeSubCategory(iterator, subCategory);
 					return;
 				}
 			}
 			throw new RuntimeException("Invalid branch, subCategory for node "
 					+ node.getCode() + " not found");
 		}
 	}
 
 	private SubCategory getLeafSubCategory(
 			Iterator<CategoryBranchNode> iterator, SubCategory parentCategory) {
 		if (iterator.hasNext()) {
 			final CategoryBranchNode node = iterator.next();
 			final List<SubCategory> subCategories = parentCategory
 					.getSubCategories();
 			if (subCategories == null) {
 				throw new RuntimeException(
 						"Invalid branch, no more subCategories from node "
 								+ node.getCode());
 			}
 			for (SubCategory subCategory : subCategories) {
 				if (subCategory.getCode().equals(node.getCode())) {
 					return getLeafSubCategory(iterator, subCategory);
 				}
 			}
 			throw new RuntimeException("Invalid branch, subCategory for node "
 					+ node.getCode() + " not found");
 		} else {
 			return parentCategory;
 		}
 	}
 
 }
