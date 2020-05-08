 package com.google.code.jskills.pages.competence;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreeModel;
 
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.AjaxLink;
 import org.apache.wicket.extensions.markup.html.tree.AbstractTree;
import org.wicketstuff.shiro.ShiroConstraint;
import org.wicketstuff.shiro.annotation.ShiroSecurityConstraint;
 
 import com.google.code.jskills.business.services.CompetencesService;
 import com.google.code.jskills.domain.Competences;
 import com.google.code.jskills.domain.Level;
 import com.google.code.jskills.pages.master.MasterPage;
 
 @SuppressWarnings("deprecation")
@ShiroSecurityConstraint(constraint = ShiroConstraint.IsAuthenticated)
 public abstract class BaseTree extends MasterPage{
 
 	private static final long serialVersionUID = -2298039056490908760L;
 	
 	@Inject
 	private transient CompetencesService competencesService;
 	
 	private int i = 0;
 	
 	public DefaultMutableTreeNode child;
 	/*
 	 * main constructor with 3 operation:
 	 * 1. ExpandAll
 	 * 2. CollapseAll
 	 * 3. SwitchRootless
 	 */
     public BaseTree()
     {
         add(new AjaxLink<Void>("expandAll")
         {
 			private static final long serialVersionUID = -4488512524498663080L;
 
 			@Override
             public void onClick(AjaxRequestTarget target)
             {
                 getTree().getTreeState().expandAll();
                 getTree().updateTree(target);
             }
         });
 
         add(new AjaxLink<Void>("collapseAll")
         {
 
 			private static final long serialVersionUID = 2998543577060345291L;
 
 			@Override
             public void onClick(AjaxRequestTarget target)
             {
                 getTree().getTreeState().collapseAll();
                 getTree().updateTree(target);
             }
         });
 
         add(new AjaxLink<Void>("switchRootless")
         {
 
 			private static final long serialVersionUID = -7158896687825483398L;
 
 			@Override
             public void onClick(AjaxRequestTarget target)
             {
                 getTree().setRootLess(!getTree().isRootLess());
                 getTree().updateTree(target);
             }
         });
     }
 	
 	protected abstract AbstractTree getTree();
 	
 	/**
 	 * create tree model with competences list and level list
 	 * 
 	 * @return TreeModel
 	 */
 	public TreeModel createTreeModel() {
 		List<Object> treeList = new ArrayList<Object>();
 
 		treeList.add(convertToString(competencesService.getCompetencesByParentId(1)));
 		treeList.add(convertToString(competencesService.getCompetencesByParentId(5)));
 		treeList.add(convertToString(competencesService.getCompetencesByParentId(9)));
 		treeList.add(convertToString(competencesService.getCompetencesByParentId(25)));
 		treeList.add(convertToString(competencesService.getCompetencesByParentId(30)));
 
 		return convertToTreeModel(treeList);
 	}
 
 	private TreeModel convertToTreeModel(List<Object> treeList) {
 		TreeModel model = null;
 		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(
 				new ModelBean("Competences"));
 		add(rootNode, treeList);
 		model = new DefaultTreeModel(rootNode);
 		return model;
 	}
 
 	@SuppressWarnings("unchecked")
 	private void add(DefaultMutableTreeNode parent, List<Object> sub) {
 		List<Competences> rootCompetences = new ArrayList<Competences>();
 		List<Level> level1  = new ArrayList<Level>();
 		List<Level> level2  = new ArrayList<Level>();
 		List<Level> level3  = new ArrayList<Level>();
 		List<Level> level4  = new ArrayList<Level>();
 		rootCompetences = competencesService.getRootCompetences();
 		level1 = competencesService.getLevelByLevelType(1);
 		level2 = competencesService.getLevelByLevelType(2);
 		level3 = competencesService.getLevelByLevelType(3);
 		level4 = competencesService.getLevelByLevelType(4);
 		int index = 0;
 		int index2 = 0;
 		for (Object obj : sub) {
 			if (obj instanceof List) {
 				child = new DefaultMutableTreeNode(
 						new ModelBean(rootCompetences.get(index).getDescription()));
 				index++;
 				parent.add(child);
 				index2 = 0;
 				add(child, (List<Object>) obj);
 			} else {
 				child = new DefaultMutableTreeNode(new ModelBean(sub.get(index2).toString(),
 						level1.get(i).getDescription(), level2.get(i).getDescription(),
 						level3.get(i).getDescription(), level4.get(i).getDescription()));
 				i++;
 				index2++;
 				parent.add(child);
 			}
 		}
 	}
 	
 	private List<String> convertToString(List<Competences> competences) {
 		List<String> result = new ArrayList<String>();
 		for (Competences item : competences) {
 			result.add(item.getDescription());
 		}
 		return result;
 	}
 }
