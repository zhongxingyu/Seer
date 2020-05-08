 package Sirius.navigator.ui.tree;
 
 import java.beans.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import javax.swing.event.*;
 
 import Sirius.navigator.resource.*;
 import Sirius.navigator.method.*;
 import Sirius.navigator.search.dynamic.profile.QueryResultProfileManager;
 import Sirius.navigator.ui.ComponentRegistry;
 import Sirius.server.search.store.QueryInfo;
 
 import de.cismet.tools.gui.JPopupMenuButton;
 
 import org.apache.log4j.Logger;
 
 /**
  *
  * @author  pascal
  */
 public class SearchResultsTreePanel extends JPanel
 {
     private final Logger logger;
     
     private final SearchResultsTree searchResultsTree;
     private final JToolBar toolBar;
     
     private JButton     browseBackButton, browseForwardButton,
             removeButton, clearButton,
             saveButton;
     private JPopupMenuButton saveAllButton;
     private JCheckBox showDirectlyInMap;
     
     public SearchResultsTreePanel(SearchResultsTree searchResultsTree)
     {
         this(searchResultsTree, false);
     }
     
     /** Creates a new instance of SearchResultsTreePanel */
     public SearchResultsTreePanel(SearchResultsTree searchResultsTree, boolean advancedLayout)
     {
         super(new BorderLayout());
         this.searchResultsTree = searchResultsTree;
         this.toolBar = new JToolBar(org.openide.util.NbBundle.getMessage(SearchResultsTreePanel.class, "SearchResultsTreePanel.toolbar.name"),//NOI18N
                 JToolBar.HORIZONTAL) ;
         this.toolBar.setRollover(advancedLayout);
         this.toolBar.setFloatable(advancedLayout);
         
         this.logger = Logger.getLogger(this.getClass());
         
         this.init();
     }
     
     private void init()
     {
         this.createDefaultButtons();
         this.add(toolBar, BorderLayout.SOUTH);
         this.add(new JScrollPane(searchResultsTree), BorderLayout.CENTER);
         this.setButtonsEnabled();
         
         ButtonEnablingListener buttonEnablingListener = new ButtonEnablingListener();
         this.searchResultsTree.addTreeSelectionListener(buttonEnablingListener);
         this.searchResultsTree.addPropertyChangeListener("browse", buttonEnablingListener);//NOI18N
         this.addComponentListener(new ComponentEventForwarder());
     }
     
     private void createDefaultButtons()
     {
         ResourceManager resources = ResourceManager.getManager();
         ActionListener toolBarListener = new ToolBarListener();
         
         browseBackButton = new JButton(resources.getIcon("back24.gif"));//NOI18N
         browseBackButton.setToolTipText(org.openide.util.NbBundle.getMessage(SearchResultsTreePanel.class, "SearchResultsTreePanel.backButton.tooltip"));//NOI18N
         browseBackButton.setActionCommand("back");//NOI18N
         browseBackButton.setMargin(new Insets(4,4,4,4));
         browseBackButton.addActionListener(toolBarListener);
         toolBar.add(browseBackButton);
         //toolBar.addSeparator();
         
         browseForwardButton = new JButton(resources.getIcon("forward24.gif"));//NOI18N
         browseForwardButton.setToolTipText(org.openide.util.NbBundle.getMessage(SearchResultsTreePanel.class, "SearchResultsTreePanel.browseForwardButton.tooltip"));//NOI18N
         browseForwardButton.setActionCommand("forward");//NOI18N
         browseForwardButton.setMargin(new Insets(4,4,4,4));
         browseForwardButton.addActionListener(toolBarListener);
         toolBar.add(browseForwardButton);
         toolBar.addSeparator();
         
         removeButton = new JButton(resources.getIcon("remove24.gif"));//NOI18N
         removeButton.setToolTipText(org.openide.util.NbBundle.getMessage(SearchResultsTreePanel.class, "SearchResultsTreePanel.removeButton.tooltip"));//NOI18N
         removeButton.setActionCommand("remove");//NOI18N
         removeButton.setMargin(new Insets(4,4,4,4));
         removeButton.addActionListener(toolBarListener);
         toolBar.add(removeButton);
         //toolBar.addSeparator();
         
         clearButton = new JButton(resources.getIcon("delete24.gif"));//NOI18N
         clearButton.setToolTipText(org.openide.util.NbBundle.getMessage(SearchResultsTreePanel.class, "SearchResultsTreePanel.clearButton.tooltip"));//NOI18N
         clearButton.setActionCommand("clear");//NOI18N
         clearButton.setMargin(new Insets(4,4,4,4));
         clearButton.addActionListener(toolBarListener);
         toolBar.add(clearButton);
         toolBar.addSeparator();
         
         //saveAllButton = new JButton(resources.getIcon("saveall24.gif"));
         saveAllButton = new JPopupMenuButton();
         saveAllButton.setPopupMenu(new HistoryPopupMenu());
         saveAllButton.setIcon(resources.getIcon("saveall24.gif"));//NOI18N
         saveAllButton.setToolTipText(org.openide.util.NbBundle.getMessage(SearchResultsTreePanel.class, "SearchResultsTreePanel.saveAllButton.tooltip"));//NOI18N
         saveAllButton.setActionCommand("saveall");//NOI18N
         saveAllButton.setMargin(new Insets(4,4,4,4));
         saveAllButton.addActionListener(toolBarListener);
         toolBar.add(saveAllButton);
         
         
         showDirectlyInMap=new JCheckBox();
         showDirectlyInMap.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 searchResultsTree.setSyncWithMap(showDirectlyInMap.isSelected());
             }
         });
         showDirectlyInMap.setSelected(false);
         toolBar.add(showDirectlyInMap);
        JLabel showDirectlyInMapLabel= new JLabel(resources.getIcon("saveall24.gif"));//NOI18N
         showDirectlyInMapLabel.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent e) {
                 if (e.getClickCount()>1) {
                     searchResultsTree.syncWithMap(true);
                 }
             }
 
         });
         
         showDirectlyInMapLabel.setToolTipText(org.openide.util.NbBundle.getMessage(SearchResultsTreePanel.class, "SearchResultsTreePanel.showDirectInMapLabel.tooltipText"));//NOI18N
         toolBar.add(showDirectlyInMapLabel);
     }
     
     public void setButtonsEnabled()
     {
         browseBackButton.setEnabled(searchResultsTree.isBrowseBack());
         browseForwardButton.setEnabled(searchResultsTree.isBrowseForward());
         removeButton.setEnabled(!searchResultsTree.isEmpty() && searchResultsTree.getSelectedNodeCount() > 0);
         clearButton.setEnabled(!searchResultsTree.isEmpty());
         
         // not implemented:
         //saveButton.setEnabled(!searchResultsTree.isEmpty() && searchResultsTree.getSelectedNodeCount() > 0);
         //saveButton.setEnabled(false);
         
         //saveAllButton.setEnabled(!searchResultsTree.isEmpty());
         saveAllButton.setEnabled(true);
     }
     
     public JToolBar getToolBar()
     {
         return this.toolBar;
     }
     
     public SearchResultsTree getSearchResultsTree()
     {
         return this.searchResultsTree;
     }
     
     private class ComponentEventForwarder extends ComponentAdapter
     {
         
         /** Invoked when the component has been made invisible.
          *
          */
         public void componentHidden(ComponentEvent e)
         {
             searchResultsTree.dispatchEvent(e);
         }
         
         /** Invoked when the component has been made visible.
          *
          */
         public void componentShown(ComponentEvent e)
         {
             searchResultsTree.dispatchEvent(e);
         }
     }
     
     private class ToolBarListener implements ActionListener
     {
         /**
          * Invoked when an action occurs.
          */
         public void actionPerformed(ActionEvent e)
         {
             if(e.getActionCommand().equals("back"))//NOI18N
             {
                 searchResultsTree.browseBack();
             }
             else if(e.getActionCommand().equals("forward"))//NOI18N
             {
                 searchResultsTree.browseForward();
             }
             else if(e.getActionCommand().equals("remove"))//NOI18N
             {
                 searchResultsTree.removeSelectedResultNodes();
             }
             else if(e.getActionCommand().equals("clear"))//NOI18N
             {
                 searchResultsTree.clear();
             }
             else if(e.getActionCommand().equals("save"))//NOI18N
             {
                 //logger.warn("command 'save' not implemented");
             }
             else if(e.getActionCommand().equals("saveall"))//NOI18N
             {
                 MethodManager.getManager().showQueryResultProfileManager();
             }
             
             SearchResultsTreePanel.this.setButtonsEnabled();
         }
     }
     
     private class ButtonEnablingListener implements PropertyChangeListener, TreeSelectionListener
     {
         
         /** This method gets called when a bound property is changed.
          * @param evt A PropertyChangeEvent object describing the event source
          *   	and the property that has changed.
          *
          */
         public void propertyChange(PropertyChangeEvent e)
         {
             SearchResultsTreePanel.this.setButtonsEnabled();
         }
         
         /**
          * Called whenever the value of the selection changes.
          * @param e the event that characterizes the change.
          *
          */
         public void valueChanged(TreeSelectionEvent e)
         {
             SearchResultsTreePanel.this.setButtonsEnabled();
         }
     }
     
     private class HistoryPopupMenu extends JPopupMenu implements PopupMenuListener, ActionListener
     {
         private QueryResultProfileManager queryResultProfileManager = null;
         
         public HistoryPopupMenu()
         {
             if(logger.isDebugEnabled())logger.debug("HistoryPopupMenu(): creating new instance");//NOI18N
             this.addPopupMenuListener(this);
             
             // ugly workaround
             this.add(new JMenuItem("shouldnotseeme"));//NOI18N
         }
  
         public void popupMenuCanceled(PopupMenuEvent e) 
         {
              if(logger.isDebugEnabled())logger.debug("popupMenuCanceled()");//NOI18N
              
              // ugly workaround
             this.add(new JMenuItem("shouldnotseeme"));//NOI18N
         }
         
         public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
         {
             if(logger.isDebugEnabled())logger.debug("popupMenuWillBecomeInvisible()");//NOI18N
             
             // ugly workaround
             this.add(new JMenuItem("shouldnotseeme"));//NOI18N
         }
         
         public void popupMenuWillBecomeVisible(PopupMenuEvent e)
         {
             if(logger.isDebugEnabled())logger.debug("popupMenuWillBecomeVisible(): showing popup meu");//NOI18N
             
             if(this.queryResultProfileManager == null)
             {
                 this.queryResultProfileManager = ComponentRegistry.getRegistry().getQueryResultProfileManager();
             }
             
             if(this.queryResultProfileManager.getUserInfos() == null ||  this.queryResultProfileManager.getUserInfos().length == 0)
             {
                 this.queryResultProfileManager.updateQueryResultProfileManager();
             }
             
             this.removeAll();
             
             QueryInfo[] userInfo = this.queryResultProfileManager.getUserInfos();
             if(userInfo != null && userInfo.length > 0)
             {
                 for(int i = 0; i < userInfo.length; i++)
                 {
                     JMenuItem menuItem = new JMenuItem(userInfo[i].getName());
                     menuItem.setActionCommand(userInfo[i].getFileName());
                     menuItem.addActionListener(this);
                     
                     this.add(menuItem);
                 }
             }
             else if(logger.isDebugEnabled())
             {
                 logger.warn("HistoryPopupMenu: no query result profiles found");//NOI18N
             }
         }
         
         public void actionPerformed (ActionEvent e)
         {
             if (logger.isInfoEnabled()) {
                 logger.info("HistoryPopupMenu: loading query result profile '" + e.getActionCommand() + "'");//NOI18N
             }
             this.queryResultProfileManager.loadSearchResults(e.getActionCommand());
         }
     }
 }
