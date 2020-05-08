 package com.madalla.webapp.blog;
 
 import static com.madalla.webapp.blog.BlogParameters.BLOG_ENTRY_ID;
 
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.TreeModel;
 import javax.swing.tree.TreeNode;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.wicket.Component;
 import org.apache.wicket.PageParameters;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
 import org.apache.wicket.behavior.SimpleAttributeModifier;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.markup.html.tree.BaseTree;
 import org.apache.wicket.markup.html.tree.LinkIconPanel;
 import org.apache.wicket.markup.html.tree.LinkTree;
 import org.apache.wicket.markup.html.tree.BaseTree.LinkType;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 import com.madalla.service.blog.IBlogService;
 import com.madalla.service.blog.IBlogServiceProvider;
 import com.madalla.service.cms.BlogEntry;
 import com.madalla.util.ui.ICalendarTreeInput;
 
 public class BlogArchivePanel extends Panel {
 	private static final long serialVersionUID = 1L;
 	private Log log = LogFactory.getLog(this.getClass());
 	private static DateTimeFormatter df = DateTimeFormat.forPattern("MMM d");
 
 	public BlogArchivePanel(final String id, final String blogName, final BlogDisplayPanel display ) {
 		super(id);
 
 		//List existing Blogs
 		log.debug("construtor - retrieving blog entries from service.");
 		IBlogService service = getBlogService();
 		TreeModel blogEntries = service.getBlogEntriesAsTree(blogName);
 		log.debug("construtor - retrieved blog entries. root="
 				+ blogEntries.getRoot());
 
 		BaseTree tree = new LinkTree("BlogTree", blogEntries) {
 			private static final long serialVersionUID = 1L;
 			
 			protected IModel getNodeTextModel(IModel model) {
 				DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) model.getObject();
 				if (treeNode.getUserObject() instanceof ICalendarTreeInput ){
 					ICalendarTreeInput treeInput = (ICalendarTreeInput) treeNode.getUserObject();
 					String title = treeInput.getTitle();
 					return new Model(df.print(treeInput.getDateTime()) + (null == title?"":" - " + title));
 				} else {
 					return model;
 				}
 			}
 
 			protected Component newNodeComponent(String id, IModel model) {
 				return new LinkIconPanel(id, model, this) {
 					private static final long serialVersionUID = 1L;
 
 					protected void onNodeLinkClicked(TreeNode node,	BaseTree tree, AjaxRequestTarget target) {
 						super.onNodeLinkClicked(node, tree, target);
 						log.debug("onNodeLinkClicked - " + node);
 
 						if (node.isLeaf()) {
 							log.debug("onNodeLinkClicked - "+node);
 							DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
 							if (treeNode.getUserObject() instanceof BlogEntry){
 								BlogEntry blogEntry = (BlogEntry) treeNode.getUserObject();
 								String blogEntryId = blogEntry.getId();
 								log.debug("onNodeLinkClicked - blogId="+blogEntryId);
 								if (target == null){ //non-ajax
 									log.info("onNodeLinkClicked - no target found.");
 									PageParameters params = new PageParameters(BLOG_ENTRY_ID+"="+blogEntryId);
 									setResponsePage(getPage().getClass(), params);
 								} else {
 									display.changeModel(blogEntryId);
 									display.add(new SimpleAttributeModifier("class","showBlog"));
 									target.addComponent(display);
 								}
 							}
 						}
 					}
 
 					protected Component newContentComponent(String componentId, BaseTree tree, IModel model) {
 						return new Label(componentId, getNodeTextModel(model));
 					}
 				};
 			}
 
 		};
 		tree.getTreeState().expandAll();
 		tree.setLinkType(LinkType.AJAX_FALLBACK);
 		add(tree);
 		
 		Link home = new AjaxFallbackLink("home"){

			private static final long serialVersionUID = -3613367972403232618L;
 
 			@Override
 			public void onClick(AjaxRequestTarget target) {
				target.addComponent(display);
				display.add(new SimpleAttributeModifier("class","showHome"));
 				
 			}
 			
 		};
 		add(home);
 		
 
 	}
 	
     private IBlogService getBlogService(){
     	return ((IBlogServiceProvider)getApplication()).getBlogService();
     }
 
 }
