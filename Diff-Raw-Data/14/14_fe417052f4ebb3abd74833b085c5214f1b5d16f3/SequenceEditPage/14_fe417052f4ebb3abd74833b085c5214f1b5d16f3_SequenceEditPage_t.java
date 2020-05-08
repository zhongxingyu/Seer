 package org.wicketstuff.pickwick.backend.pages;
 
 import java.io.File;
 
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.TreeModel;
 import javax.swing.tree.TreeNode;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.MarkupContainer;
 import org.apache.wicket.PageParameters;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.wicketstuff.dojo.markup.html.DojoLink;
 import org.wicketstuff.pickwick.backend.ImageUtils;
 import org.wicketstuff.pickwick.backend.panel.SequencePropertiesPanel;
 import org.wicketstuff.pickwick.bean.DisplaySequence;
 import org.wicketstuff.pickwick.bean.Folder;
 import org.wicketstuff.pickwick.bean.Sequence;
 import org.wicketstuff.pickwick.frontend.pages.BasePage;
 import org.wicketstuff.pickwick.frontend.panel.FolderTreePanel;
 import org.wicketstuff.pickwick.frontend.panel.MetaDisplayPanel;
 
 import com.google.inject.Inject;
 
 public class SequenceEditPage extends BasePage {
 	SequencePropertiesPanel panel;
 	
 	@Inject
 	private ImageUtils imageUtils;
 
 	private FolderTreePanel tree;
 
 	private String uri;
 	
 	private MetaDisplayPanel meta;
 	
 	public SequenceEditPage(PageParameters params) {
 		super();
 		uri = params.getString("uri");
 		
 		File imageDirectory = null;
		if (uri != null){
 			imageDirectory = imageUtils.toFile(uri);
		}else{
			imageDirectory = imageUtils.toFile("");
		}
 
 		panel = new SequencePropertiesPanel("properties") {
 			@Override
 			public void onSave(AjaxRequestTarget target) {
 				readSequence();
 				target.addComponent(SequenceEditPage.this.getTree());
 				target.addComponent(meta);
 			}
 		};
 		panel.setImageDirectory(imageDirectory);
 		panel.setOutputMarkupId(true);
 
 		tree = new FolderTreePanel("folders") {
 			@Override
 			public MarkupContainer newNodeLink(MarkupContainer parent, String id, TreeNode node) {
 				final Folder folder = (Folder) ((DefaultMutableTreeNode) node).getUserObject();
 				DojoLink nodeLink = new DojoLink(id) {
 
 					@Override
 					public void onClick(AjaxRequestTarget target) {
 						panel.setImageDirectory(folder.getFile());
 						target.addComponent(panel);
 					}
 				};
 
 				return nodeLink;
 			}
 			
 			/**
 			 * Creates the model that feeds the tree.
 			 * @return
 			 * 		New instance of tree model.
 			 */
 			protected TreeModel createTreeModel() 
 			{
 				return convertToTreeModel(imageUtils.getFolder());
 			}
 		};
 		tree.setOutputMarkupId(true);
 
 		add(tree);
 		add(panel);
 		add(meta = new MetaDisplayPanel("meta", uri));
 
 	}
 
 	protected Component getTree() {
 		return tree;
 	}
 	
 	public void readSequence() {
 		if (uri != null) {
 			File imageDir = imageUtils.toFile(uri);
 			Sequence sequence = imageUtils.readSequence(imageDir);
 			setModel(new CompoundPropertyModel(new DisplaySequence(sequence, imageDir)));
 		}
 	}
 }
