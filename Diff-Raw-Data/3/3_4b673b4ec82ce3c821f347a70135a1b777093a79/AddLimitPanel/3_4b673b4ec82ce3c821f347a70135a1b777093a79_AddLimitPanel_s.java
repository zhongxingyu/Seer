 package edu.wustl.cab2b.client.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GradientPaint;
 import java.awt.Image;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.border.EmptyBorder;
 
 import org.jdesktop.swingx.JXPanel;
 import org.jdesktop.swingx.JXTitledPanel;
 import org.jdesktop.swingx.painter.gradient.BasicGradientPainter;
 import org.openide.util.Utilities;
 
 import edu.wustl.cab2b.client.ui.controls.Cab2bButton;
 import edu.wustl.cab2b.client.ui.controls.Cab2bPanel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bTitledPanel;
 import edu.wustl.cab2b.client.ui.dag.MainDagPanel;
 import edu.wustl.cab2b.client.ui.main.AbstractTypePanel;
 import edu.wustl.cab2b.client.ui.main.IComponent;
 import edu.wustl.cab2b.client.ui.query.ClientPathFinder;
 import edu.wustl.cab2b.client.ui.query.IClientQueryBuilderInterface;
 import edu.wustl.cab2b.client.ui.query.IPathFinder;
 import edu.wustl.cab2b.client.ui.util.CommonUtils;
 import edu.wustl.cab2b.client.ui.util.CommonUtils.DagImages;
 import edu.wustl.common.querysuite.exceptions.MultipleRootsException;
 import edu.wustl.common.querysuite.queryobject.ICondition;
 import edu.wustl.common.querysuite.queryobject.IConstraintEntity;
 import edu.wustl.common.querysuite.queryobject.IExpression;
 import edu.wustl.common.querysuite.queryobject.IExpressionId;
 import edu.wustl.common.querysuite.queryobject.IRule;
 import edu.wustl.common.querysuite.queryobject.RelationalOperator;
 import static edu.wustl.cab2b.client.ui.util.ClientConstants.LIMIT_CONNECT_SELECTED;
 import static edu.wustl.cab2b.client.ui.util.ClientConstants.LIMIT_CONNECT_DESELECTED;
 import static edu.wustl.cab2b.client.ui.util.ClientConstants.PAPER_GRID_ADD_LIMIT;
 import static edu.wustl.cab2b.client.ui.util.ClientConstants.SELECT_ICON_ADD_LIMIT;
 import static edu.wustl.cab2b.client.ui.util.ClientConstants.SELECT_ICON_ADD_LIMIT_MOUSEOVER;
 import static edu.wustl.cab2b.client.ui.util.ClientConstants.PARENTHISIS_ICON_ADD_LIMIT;
 import static edu.wustl.cab2b.client.ui.util.ClientConstants.PARENTHISIS_ICON_ADD_LIMIT_MOUSEOVER;
 import static edu.wustl.cab2b.client.ui.util.ClientConstants.PORT_IMAGE_ADD_LIMIT;
 /**
  * This is the panel for the Add limit tab from the main search dialog. The
  * class is also an instance of the {@link ContentPanel},so that child
  * component can cause this panel to refresh in a way required by this panel.
  * 
  * @author mahesh_iyer
  */
 public class AddLimitPanel extends ContentPanel implements IUpdateAddLimitUIInterface {
     private static final long serialVersionUID = 1L;
 
     /** The titled panel for the top panel. */
     private JXTitledPanel m_topCenterPanel = null;
 
     private AbstractSearchResultPanel m_searchResultPanel;
 
     /** Scroll pane for the top panel. */
     private JScrollPane m_scrollPane = null;
 
     /**
      * The dynamically generated panel for the selected class from an
      * advanced/category search.
      */
     private JXPanel m_ContentForTopPanel = null;
 
     /** The advanced search panel along with the results panel. */
     private AbstractCategorySearchPanel categSearchPanel = null;
 
     /** The titled panel for the bottom panel. */
     private JXTitledPanel m_bottomCenterPanel = null;
 
     /** The simple view for the rules added. This is to be replaced by the DAG. */
     private MainDagPanel m_contentForBottomCenterPanel = null;
 
     /** Split pane between the top and center titled panels. */
     public static JSplitPane m_innerPane = null;
 
     /** Split pane between the LHS and RHS sections of the main panel. */
     private JSplitPane m_outerPane = null;
 
     /**
      * Default constructor
      */
     AddLimitPanel() {
         initGUI();
     }
 
     /**
      * Method initializes the panel by appropriately laying out child
      * components.
      */
     private void initGUI() {
         this.setLayout(new BorderLayout());
         /*
          * Pass the reference , so that the child can cause the parent to
          * refresh for any events triggered in the child.
          */
         if (categSearchPanel == null)
             categSearchPanel = new AddLimitCategorySearchPanel(this);
 
         /* The top center titled panel */
         m_topCenterPanel = new Cab2bTitledPanel("Define Search Rules");
         m_topCenterPanel.setTitleForeground(Color.BLACK);
         /* Set a gradient painter for the title panel */
         GradientPaint gp = new GradientPaint(new Point2D.Double(.3d, 0), new Color(185, 211, 238),
                 new Point2D.Double(.7, 0), Color.WHITE);
         m_topCenterPanel.setTitlePainter(new BasicGradientPainter(gp));
         /*
          * Set the preferred size for the top panel, as against the preferred
          * size for the contentPanel/child panel itself. Doing the later has the
          * undesired result of content panel getting clipped even if the actual
          * length is more than the set preferred size, and the vertical scroll
          * bars never come into existence. Again, size set based on usability
          * specs.
          */
         m_topCenterPanel.setPreferredSize(new Dimension(546, 341));
         m_topCenterPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
 
         /*
          * JXTitledPanels work better with only panel as child, and hence the
          * following panel.
          */
         this.m_ContentForTopPanel = new Cab2bPanel();
         this.m_ContentForTopPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
         this.m_scrollPane = new JScrollPane(this.m_ContentForTopPanel);
         this.m_scrollPane.getViewport().setBackground(Color.WHITE);
         this.m_topCenterPanel.add(this.m_scrollPane);
 
         /* The bottom center titled panel.Initialization on the same lines. */
         m_bottomCenterPanel = new Cab2bTitledPanel("Limit Set");
         m_bottomCenterPanel.setTitleForeground(Color.BLACK);
         m_bottomCenterPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
         m_bottomCenterPanel.setTitlePainter(new BasicGradientPainter(gp));
 
         /**
          * Generate ImageMap
          */
         Map<DagImages, Image> imageMap = new HashMap<DagImages, Image>();
         imageMap.put(DagImages.SelectIcon, Utilities.loadImage(SELECT_ICON_ADD_LIMIT));
         imageMap.put(DagImages.selectMOIcon, Utilities.loadImage(SELECT_ICON_ADD_LIMIT_MOUSEOVER));
         imageMap.put(DagImages.ArrowSelectIcon, Utilities.loadImage(LIMIT_CONNECT_DESELECTED));
         imageMap.put(DagImages.ArrowSelectMOIcon, Utilities.loadImage(LIMIT_CONNECT_SELECTED));
         imageMap.put(DagImages.ParenthesisIcon, Utilities.loadImage(PARENTHISIS_ICON_ADD_LIMIT));
         imageMap.put(DagImages.ParenthesisMOIcon, Utilities.loadImage(PARENTHISIS_ICON_ADD_LIMIT_MOUSEOVER));
         imageMap.put(DagImages.DocumentPaperIcon, Utilities.loadImage(PAPER_GRID_ADD_LIMIT));
         imageMap.put(DagImages.PortImageIcon, Utilities.loadImage(PORT_IMAGE_ADD_LIMIT));
 
         IPathFinder pathFinder = new ClientPathFinder();
         m_contentForBottomCenterPanel = new MainDagPanel(this, imageMap, pathFinder, false);
         m_bottomCenterPanel.add(m_contentForBottomCenterPanel);
 
         /* Add components to the conetent pane. */
         m_innerPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, m_topCenterPanel, m_bottomCenterPanel);
         // this.m_innerPane.setDividerLocation(0.5D);
         m_innerPane.setOneTouchExpandable(false);
         m_innerPane.setBorder(null);
         m_innerPane.setDividerSize(4);

         this.m_outerPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, categSearchPanel, m_innerPane);
         this.m_outerPane.setDividerLocation(0.2D);
         this.m_outerPane.setOneTouchExpandable(false);
         this.m_outerPane.setBorder(null);
         this.m_outerPane.setDividerSize(4);
         this.m_outerPane.setDividerLocation(242);
         this.add(BorderLayout.CENTER, this.m_outerPane);
     }
 
     /**
      * Method to add search result panel
      * 
      * @param resultPanel
      */
     public void addResultsPanel(AbstractSearchResultPanel resultPanel) {
         categSearchPanel.m_searchPanel.addResultsPanel(resultPanel);
     }
 
     /**
      * This method takes the newly added expression and renders the node
      * accordingly
      * 
      * @param expressionId
      */
     public void refreshBottomCenterPanel(IExpressionId expressionId) {
         // Here code to handle adding new limit will appear
         try {
             m_contentForBottomCenterPanel.updateGraph(expressionId);
         } catch (MultipleRootsException e) {
             CommonUtils.handleException(e, this, true, true, false, false);
         }
         this.updateUI();
     }
 
     /**
      * The method is a custom implementation for the refresh method from the
      * {@link ContentPanel} interface. Custom implementation is to simply set
      * the provided panel as the content panel for the top titled panel.
      * 
      * @param panelToBeRefreshed
      *            The panel to be refreshed.
      * 
      * @param strClassNameAsTitle
      *            The class/category name for which the dynamic UI is generated.
      */
 
     public void refresh(JXPanel[] arrPanel, String strClassNameAsTitle) {
         /* Set the title for the top titled panel. */
         this.m_topCenterPanel.setTitle("Define Search Rules '" + strClassNameAsTitle + "'");
         this.m_ContentForTopPanel.removeAll();
         int length = arrPanel.length;
         /* Add the individual panels to the top content panel. */
         for (int i = 0; i < length; i++) {
             if (arrPanel[i] != null) {
                 this.m_ContentForTopPanel.add("br", arrPanel[i]);
             }
         }
         validate();
     }
 
     /**
      * The method returns a reference to the bottom content panel. This is
      * invoked by the main panel in order to form the query.
      * 
      * @return JXPanel The bottom content panel.
      */
 
     public JXPanel getBottomCenterPanel() {
         return this.m_contentForBottomCenterPanel;
     }
 
     public void setQueryObject(IClientQueryBuilderInterface query) {
         m_contentForBottomCenterPanel.setQueryObject(query);
     }
 
     public void editAddLimitUI(IExpression expression) {
         IConstraintEntity entity = expression.getConstraintEntity();
         JXPanel[] panels = m_searchResultPanel.getEditLimitPanels(expression);
 
         // passing appropriate class name
         refresh(panels, edu.wustl.cab2b.common.util.Utility.getDisplayName(entity.getDynamicExtensionsEntity()));
         IRule rule = (IRule) expression.getOperand(0);
         int totalConditions = rule.size();
         // Populate panels with corresponding value
         for (int i = 0; i < totalConditions; i++) {
             ICondition condition = rule.getCondition(i);
             setValueForAttribute(panels, condition);
         }
         validate();
     }
 
     private void setValueForAttribute(JXPanel[] panels, ICondition condition) {
         // Don't consider panel 1 and panel end for getting attribute values
         // because first and last panels are Edit Limit button panels
         for (int i = 1; i < panels.length - 1; i++) {
             IComponent panel = (IComponent) panels[i];
             String panelAttributeName = panel.getAttributeName();
             int compareVal = panelAttributeName.compareToIgnoreCase(condition.getAttribute().getName());
             if (0 == compareVal) {
                 RelationalOperator operator = condition.getRelationalOperator();
                 panel.setCondition(edu.wustl.cab2b.client.ui.query.Utility.displayStringForRelationalOperator(operator));
                 ArrayList<String> values = (ArrayList<String>) condition.getValues();
                 panel.setValues(values);
                 break;
             }
         }
     }
 
     public void setSearchResultPanel(AbstractSearchResultPanel searchResultPanel) {
         m_searchResultPanel = searchResultPanel;
     }
 
     public AbstractSearchResultPanel getSearchResultPanel() {
         return m_searchResultPanel;
     }
 
     /*
      * Method to clear (refresh) AddLimitUI when Node is in edit mode
      * (non-Javadoc)
      * 
      * @see edu.wustl.cab2b.client.ui.IUpdateAddLimitUIInterface#clearAddLimitUI(edu.wustl.common.querysuite.queryobject.IExpression)
      */
     public void clearAddLimitUI() {
         Component[] components = m_ContentForTopPanel.getComponents();
 
         resetButton((Cab2bPanel) components[0], m_searchResultPanel.getAddLimitButtonTop());
         resetButton((Cab2bPanel) components[components.length - 1], m_searchResultPanel.getAddLimitButtonBottom());
 
         /* Add the individual panels to the top content panel. */
         for (Component component : components) {
             if (component instanceof AbstractTypePanel) {
                 ((AbstractTypePanel) component).resetPanel();
             }
         }
         revalidate();
         updateUI();
     }
 
     private void resetButton(Cab2bPanel cab2bPanel, Cab2bButton cab2bButton) {
         cab2bPanel.removeAll();
         cab2bPanel.add(cab2bButton);
     }
 
     public void resetPanel() {
         clearAddLimitUI();
         m_contentForBottomCenterPanel.clearDagPanel();
         revalidate();
         updateUI();
     }
 
     public AbstractCategorySearchPanel getSearchPanel() {
         return categSearchPanel;
     }
 
     public void setSearchText(String searchText) {
         categSearchPanel.getSearchPanel().setSearchtext(searchText);
     }
 
     public String getSearchText() {
         return categSearchPanel.getSearchPanel().getSearchtext();
     }
 
     public void setSearchPanel(AbstractCategorySearchPanel panel) {
         categSearchPanel = panel;
         m_searchResultPanel = panel.getSearchResultPanel();
     }
 
     public void refreshBottomCenterPanel(JXPanel panel) {
         // TODO Auto-generated method stub
     }
 
 }
