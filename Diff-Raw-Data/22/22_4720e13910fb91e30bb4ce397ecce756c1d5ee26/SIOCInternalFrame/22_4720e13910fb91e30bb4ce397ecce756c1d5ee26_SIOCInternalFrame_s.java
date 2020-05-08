 package de.m0ep.socc.shop.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.List;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JInternalFrame;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTree;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeCellRenderer;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.MutableTreeNode;
 
 import org.ontoware.rdf2go.model.node.Resource;
 import org.rdfs.sioc.Forum;
 import org.rdfs.sioc.Site;
 import org.rdfs.sioc.Thread;
 
 import com.google.common.base.Preconditions;
 
 import de.m0ep.socc.IConnector;
 import de.m0ep.socc.exceptions.ConnectorException;
 import de.m0ep.socc.shop.SOCCShopApplication;
 
 public class SIOCInternalFrame extends JInternalFrame implements Runnable {
     private static final long serialVersionUID = 1750490537282051026L;
 
     private SOCCShopApplication app;
     private JList<String> listPosts;
     private JTree treeContainers;
     private DefaultMutableTreeNode treeRoot;
     private DefaultTreeModel treeContainersModel;
 
     ImageIcon loadingIcon = new ImageIcon(
 	    SIOCInternalFrame.class.getResource("/images/imagesLoadingOLD.gif"));
     ImageIcon refreshIcon = new ImageIcon(
 	    SIOCInternalFrame.class.getResource("/images/arrow_refresh.png"));
     private JButton btnUpdate;
 
     /**
      * Create the frame.
      */
     public SIOCInternalFrame(final SOCCShopApplication app) {
 	setTitle("SIOC Viewer");
 	setMinimumSize(new Dimension(250, 150));
 	setFrameIcon(new ImageIcon(
 		SIOCInternalFrame.class.getResource("/images/user_comment.png")));
 	setResizable(true);
 	setMaximizable(true);
 	setIconifiable(true);
 	setClosable(true);
 	this.app = Preconditions.checkNotNull(app, "App can not be null.");
 
 	setBounds(100, 100, 450, 300);
 
 	JSplitPane splitPane = new JSplitPane();
 	getContentPane().add(splitPane, BorderLayout.CENTER);
 
 	treeRoot = new DefaultMutableTreeNode(new SIOCTreeItem(
 		SIOCTreeItem.ROOT, "SIOC", null), true);
 	treeContainersModel = new DefaultTreeModel(treeRoot);
 
 	treeContainers = new JTree(treeContainersModel);
 	treeContainers.setCellRenderer(new MyCellRenderer());
 	JScrollPane scrollPane = new JScrollPane(treeContainers);
 	splitPane.setLeftComponent(scrollPane);
 
 	listPosts = new JList<String>();
 	scrollPane = new JScrollPane(listPosts);
 	splitPane.setRightComponent(scrollPane);
 
 	JPanel buttonPanel = new JPanel();
 	getContentPane().add(buttonPanel, BorderLayout.SOUTH);
 
 	btnUpdate = new JButton("Update");
 	btnUpdate.addActionListener(new ActionListener() {
 	    @Override
 	    public void actionPerformed(ActionEvent e) {
 		SwingUtilities.invokeLater(SIOCInternalFrame.this);
 	    }
 	});
 	buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
 	btnUpdate.setIcon(refreshIcon);
 	buttonPanel.add(btnUpdate);
 
 	SwingUtilities.invokeLater(this);
     }
 
     @Override
     public void run() {
 	btnUpdate.setIcon(loadingIcon);
 
 	List<IConnector> connectors = app.getSocc().getConnectors();
 	treeRoot = new DefaultMutableTreeNode(new SIOCTreeItem(
 		SIOCTreeItem.ROOT, "SIOC", null), true);
 	for (IConnector connector : connectors) {
 	    try {
 		Site site = connector.getSite();
 
 		MutableTreeNode siteNode = new DefaultMutableTreeNode(
 			new SIOCTreeItem(SIOCTreeItem.SITE, site.getName(),
 				null), true);
 
 		List<Forum> forums = connector.getForums();
 		for (Forum forum : forums) {
 		    MutableTreeNode forumNode = new DefaultMutableTreeNode(
 			    new SIOCTreeItem(SIOCTreeItem.FORUM,
 				    forum.getName(), null), true);
 		    siteNode.insert(forumNode, siteNode.getChildCount());
 
 		    List<Thread> threads = connector.getThreads(forum);
 		    for (Thread thread : threads) {
 			MutableTreeNode threadNode = new DefaultMutableTreeNode(
 				new SIOCTreeItem(SIOCTreeItem.THREAD,
 					thread.getName(), null), true);
 			forumNode.insert(threadNode, forumNode.getChildCount());
 		    }
 		}
 
 		treeRoot.insert(siteNode, treeRoot.getChildCount());
 	    } catch (ConnectorException e) {
 		e.printStackTrace();
 	    }
 	}
 	treeContainersModel.setRoot(treeRoot);
 	treeContainers.revalidate();
 	btnUpdate.setIcon(refreshIcon);
     }
 
     public SOCCShopApplication getApp() {
 	return this.app;
     }
 
    private void clearTreeNode(MutableTreeNode node) {
	while (0 < node.getChildCount()) {
	    node.remove(0);
	}
    }

     private static class SIOCTreeItem {
 	public static final int ROOT = 0;
 	public static final int SITE = 1;
 	public static final int FORUM = 2;
 	public static final int THREAD = 3;
 
 	private int type;
 	private String label;
	private Resource resource;
 
 	public SIOCTreeItem(int type, String label, Resource resource) {
 	    super();
 	    this.type = type;
 	    this.label = label;
	    this.resource = resource;
 	}
 
 	public int getType() {
 	    return this.type;
 	}
 
 	public String getLabel() {
 	    return this.label;
 	}
 
	public Resource getResource() {
	    return this.resource;
	}
 
 	@Override
 	public String toString() {
 	    return getLabel();
 	}
     }
 
     private static class MyCellRenderer extends DefaultTreeCellRenderer {
 
 	private ImageIcon siteIcon;
 	private ImageIcon forumIcon;
 	private ImageIcon threadIcon;
 
 	public MyCellRenderer() {
 	    super();
 
 	    siteIcon = new ImageIcon(
 		    MyCellRenderer.class.getResource("/images/site.png"));
 	    forumIcon = new ImageIcon(
 		    MyCellRenderer.class.getResource("/images/forum.png"));
 	    threadIcon = new ImageIcon(
 		    MyCellRenderer.class.getResource("/images/thread.png"));
 	}
 
 	@Override
 	public Component getTreeCellRendererComponent(JTree tree, Object value,
 		boolean selected, boolean expanded, boolean leaf, int row,
 		boolean hasFocus) {
 
 	    DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
 	    SIOCTreeItem item = (SIOCTreeItem) node.getUserObject();
 	    int type = item.getType();
 
 	    if (SIOCTreeItem.SITE == type) {
 		setIcon(siteIcon);
 	    } else if (SIOCTreeItem.FORUM == type) {
 		setIcon(forumIcon);
 	    } else if (SIOCTreeItem.THREAD == type) {
 		setIcon(threadIcon);
 	    } else if (leaf)
 		setIcon(getLeafIcon());
 	    else if (expanded)
 		setIcon(getOpenIcon());
 	    else
 		setIcon(getClosedIcon());
 
 	    setText(value.toString());
 	    this.selected = selected;
 	    this.hasFocus = hasFocus;
 	    setHorizontalAlignment(LEFT);
 	    setOpaque(false);
 	    setVerticalAlignment(CENTER);
 	    setEnabled(true);
 	    super.setFont(UIManager.getFont("Tree.font"));
 
 	    if (selected) {
 		super.setBackground(getBackgroundSelectionColor());
 		setForeground(getTextSelectionColor());
 
 		if (hasFocus)
 		    setBorderSelectionColor(UIManager.getLookAndFeelDefaults()
 			    .getColor("Tree.selectionBorderColor"));
 		else
 		    setBorderSelectionColor(null);
 	    } else {
 		super.setBackground(getBackgroundNonSelectionColor());
 		setForeground(getTextNonSelectionColor());
 		setBorderSelectionColor(null);
 	    }
 
 	    return this;
 	}
     }
 }
