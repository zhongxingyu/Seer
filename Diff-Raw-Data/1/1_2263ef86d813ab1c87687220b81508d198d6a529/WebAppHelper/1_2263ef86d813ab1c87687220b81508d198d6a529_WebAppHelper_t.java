 package utils;
 
 import org.primefaces.model.DefaultTreeNode;
 import org.primefaces.model.TreeNode;
 
 import dao.DAO;
 import domain.Article;
 import domain.Section;
 
 /**
  * WebApp helper contains methods for manipulation articles and section in PrimeFaces.TreeNode and in Database 
 * It is separated for better testing
  */
 public class WebAppHelper {
 
 	/**
 	 * Save article and update PrimeFaces.TreeNode
 	 * @param rootTreeNode
 	 * @param rootSection
 	 * @param article
 	 */
 	public static void saveArticle(TreeNode rootTreeNode, Section rootSection, Article article){
 		// update tree
 		TreeNode currentTreeNode=Utils.findArticleInTree(rootTreeNode, article);
 		EntityHolder eh=(EntityHolder)currentTreeNode.getData();
 		eh.setShortName(Utils.formatArticleTitle(article));
 		
 		// update in db
 		DAO.getInstance().beginTransaction();
 		DAO.getInstance().updateArticle(article);
 		DAO.getInstance().commitTransaction();
 	}
 	
 	/**
 	 * Save section and update PrimeFaces.TreeNode
 	 * @param rootTreeNode
 	 * @param rootSection
 	 * @param section
 	 */
 	public static void saveSection(TreeNode rootTreeNode, Section rootSection, Section section){
 		// update tree
 		TreeNode currentTreeNode=Utils.findSectionInTree(rootTreeNode, section);
 		EntityHolder eh=(EntityHolder)currentTreeNode.getData();
 		eh.setShortName(Utils.formatSectionTitle(section));
 
 		// update in db
 		DAO.getInstance().beginTransaction();
 		DAO.getInstance().updateSection(rootSection);
 		DAO.getInstance().updateSection(section);
 		DAO.getInstance().commitTransaction();
 	}
 	
 	/**
 	 * Delete article and update PrimeFaces.TreeNode
 	 * @param rootTreeNode
 	 * @param rootSection
 	 * @param article
 	 */
 	public static void deleteArticle(TreeNode rootTreeNode, Section rootSection, Article article){
 		// Remove section from tree
 		TreeNode currentTreeNode=Utils.findArticleInTree(rootTreeNode, article);
 		TreeNode parentTreeNode=currentTreeNode.getParent();
 		parentTreeNode.getChildren().remove(currentTreeNode);
 
 		// Remove section from db
 		DAO.getInstance().beginTransaction();
 		DAO.getInstance().updateSection(rootSection);
 		Article currentPersistentArticle=Utils.findPersistentArticle(rootSection,article);
 		Section persistentParent=currentPersistentArticle.getSection();
 		persistentParent.removeArticle(currentPersistentArticle);
 		DAO.getInstance().saveSection(persistentParent);
 		DAO.getInstance().deleteArticle(currentPersistentArticle);
 		DAO.getInstance().commitTransaction();
 	}
 
 	/**
 	 * Delete section and update PrimeFaces.TreeNode
 	 * @param treeRoot
 	 * @param rootSection
 	 * @param section
 	 */
 	public static void deleteSection(TreeNode treeRoot, Section rootSection, Section section){
 		// Remove section from tree
 		TreeNode currentTreeNode=Utils.findSectionInTree(treeRoot, section);
 		TreeNode parentTreeNode=currentTreeNode.getParent();
 		parentTreeNode.getChildren().remove(currentTreeNode);
 
 		// Remove section from db
 		DAO.getInstance().beginTransaction();
 		DAO.getInstance().updateSection(rootSection);
 		Section currentPersistentSection=Utils.findPersistentSection(rootSection,section);
 		Section persistentParent=currentPersistentSection.getParent();
 		persistentParent.removeSection(currentPersistentSection);
 		DAO.getInstance().saveSection(persistentParent);
 		DAO.getInstance().deleteSection(currentPersistentSection);
 		DAO.getInstance().commitTransaction();
 
 		Utils.reconstructChildrenTitles(parentTreeNode);
 	}
 
 	/**
 	 * Insert new article to section tree and update PrimeFaces.TreeNode
 	 * @param treeRoot
 	 * @param rootSection
 	 * @param targetSection
 	 * @param sourceArticle
 	 */
 	public static void insertArticle(TreeNode treeRoot, Section rootSection, Section targetSection, Article sourceArticle){
 		TreeNode currentTreeNode=Utils.findSectionInTree(treeRoot, targetSection);
 
 		// add article to db
 		DAO.getInstance().beginTransaction();
 		DAO.getInstance().updateSection(rootSection);
 		Section currentPersistentSection=Utils.findPersistentSection(rootSection,targetSection);
 
 		Article newPersistentArticle=new Article(sourceArticle.getShortName(),sourceArticle.getFullName(),sourceArticle.getText());
 		currentPersistentSection.addArticle(newPersistentArticle);
 		DAO.getInstance().saveArticle(newPersistentArticle);
 		DAO.getInstance().updateSection(currentPersistentSection);
 		DAO.getInstance().commitTransaction();
 		
 		// Add section to tree
 		EntityHolder newEntity=new EntityHolder(newPersistentArticle.getId(), "article", Utils.formatArticleTitle(newPersistentArticle), newPersistentArticle);
 		new DefaultTreeNode(newEntity,currentTreeNode);
 	}
 
 	/**
 	 * Insert new section to section tree and update PrimeFaces.TreeNode
 	 * Items "shaken" if needed
 	 * @param treeRoot
 	 * @param rootSection
 	 * @param targetSection
 	 * @param sourceSection
 	 * @param asSubling
 	 */
 	public static void insertSection(TreeNode treeRoot, Section rootSection, Section targetSection, Section sourceSection, boolean asSubling){
 
 		TreeNode currentTreeNode=Utils.findSectionInTree(treeRoot, targetSection);
 		TreeNode parentTreeNode=currentTreeNode.getParent();
 		// add section to db
 		DAO.getInstance().beginTransaction();
 		DAO.getInstance().updateSection(rootSection);
 
 		// create new section
 		Section newPersistentSection=new Section(sourceSection.getShortName(),sourceSection.getFullName());
 
 		if (asSubling){
 			//EntityHolder eh=(EntityHolder)currentTreeNode.getData();
 			//Section persistentSection=Utils.findPersistentSection(rootSection, (Section)eh.getRef());
 			Section persistentSection=Utils.findPersistentSection(rootSection, targetSection);
 			persistentSection.addSection(newPersistentSection);
 			DAO.getInstance().saveSection(persistentSection);
 		}else{
 			//EntityHolder ehparent=(EntityHolder)parentTreeNode.getData();
 			//Section parentPersistentSection=Utils.findPersistentSection(rootSection,(Section)ehparent.getRef());
 			Section parentPersistentSection=Utils.findPersistentSection(rootSection,targetSection.getParent());
 			parentPersistentSection.addSection(newPersistentSection);
 			DAO.getInstance().saveSection(parentPersistentSection);
 		}
 
 		DAO.getInstance().saveSection(newPersistentSection);
 		DAO.getInstance().commitTransaction();
 
 		EntityHolder newEntity=new EntityHolder(newPersistentSection.getId(), "section", Utils.formatSectionTitle(newPersistentSection), newPersistentSection);
 		// Add section to tree
 		if (asSubling){
 			new DefaultTreeNode(newEntity,currentTreeNode);
 		}else{
 			new DefaultTreeNode(newEntity,parentTreeNode);
 		}
 		
 		// Shake tree nodes
 		if (asSubling){
 			Utils.shakeChildrenTitles(currentTreeNode);
 		}else{
 			Utils.shakeChildrenTitles(parentTreeNode);
 		}
 	}
 
 
 }
