 /*
  * The ISGCI specific implementation of the tabbedpane. Will create a
  * startpage upon creation. Tabs can (and should be created) via 
  * the {@link #addTab(Graph)} method, because it closes the startpage
  * and draws a new graph - which this class should be used for.
  *
  * $Header$
  *
  * This file is part of the Information System on Graph Classes and their
  * Inclusions (ISGCI) at http://www.graphclasses.org.
  * Email: isgci@graphclasses.org
  */
 
 package teo.isgci.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JLayeredPane;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import org.jgrapht.Graph;
 import org.jgrapht.graph.DefaultEdge;
 
 import teo.isgci.db.Algo;
 import teo.isgci.db.Algo.NamePref;
 import teo.isgci.db.DataSet;
 import teo.isgci.drawing.DrawingLibraryFactory;
 import teo.isgci.drawing.DrawingLibraryInterface;
 import teo.isgci.drawing.GraphManipulationInterface;
 import teo.isgci.gc.GraphClass;
 import teo.isgci.grapht.GAlg;
 import teo.isgci.grapht.Inclusion;
 import teo.isgci.problem.Complexity;
 import teo.isgci.problem.Problem;
 import teo.isgci.util.Updatable;
 import teo.isgci.util.UserSettings;
 
 /**
  * The ISGCI specific implementation of the tabbedpane. Will create a
  * startpage upon creation. Tabs can (and should be created) via 
  * the {@link #addTab(Graph)} method, because it closes the startpage
  * and draws a new graph - which this class should be used for.
  */
 public class ISGCITabbedPane extends JTabbedPane implements Updatable {
     
     /**
      * The version of this class. Should be changed every time
      * this class is modified.
      */
     private static final long serialVersionUID = 3L;
 
     /**
      * Maps the content of the tabs to their corresponding
      * DrawingLibraryInterface.
      */
     private HashMap<JComponent, DrawingLibraryInterface> panelToInterfaceMap
         = new HashMap<JComponent, DrawingLibraryInterface>();
     
     /**
      * Maps the tab to their corresponding drawUnproper state.
      */
     private HashMap<JComponent, Boolean> panelToDrawUnproper
         = new HashMap<JComponent, Boolean>();
 
     /**
      * Maps the content of the tabs to their corresponding
      * naming preference.
      */
     private HashMap<JComponent, Algo.NamePref> panelToNamingPref
         = new HashMap<JComponent, Algo.NamePref>();
 
     
     /**
      * The mode which indicates the default preferred name of a Node.
      */
     private Algo.NamePref defaultMode = UserSettings.getDefaultNamingPref();
     
 
     /**
      * A popup menu which is shown after right-clicking a node.
      */
     private NodePopup nodePopup;
 
     /**
      * A popup menu which is shown after right-clicking an edge.
      */
     private EdgePopup edgePopup;
     
     
     /**
      * The reference to the mainframe that contains this panel.
      */
     private ISGCIMainFrame mainframe;
     
     /**
      * A listener which triggers if a tab is changed and then adjusts the
      * state of the drawUnproper menu item in the mainframe to match the
      * drawUnproper state of the tab.
      * Also adjusts the selection of the color problem menu to match the
      * problem of the currently active tab.
      */
     private ChangeListener changeListener = new ChangeListener() {
         public void stateChanged(ChangeEvent changeEvent) {
 
             UserSettings.setActiveTab((JComponent) getSelectedComponent());
             if (!(getSelectedComponent() instanceof StartPanel) 
                     && getSelectedIndex() >= 0) {
                 mainframe.setDrawUnproper(
                         getDrawUnproper(getSelectedComponent()));
                 mainframe.setColorProblem(UserSettings.getProblem());
             }
         }
     };
     
     /**
      * A mouse listener.
      * On right-click it opens a popup window if the clicked object is a node
      * or a edge.
      */
     private MouseAdapter mouseAdapter = new MouseAdapter() {  
         
         @Override
         public void mousePressed(MouseEvent e) {            
             if (getTabComponentAt(getSelectedIndex()) 
                     instanceof AddTabComponent) {
                 addTabComponent.addTab();
             }            
         };
         
         @Override            
         public void mouseClicked(MouseEvent e) {  
             
             DrawingLibraryInterface drawLib 
                 = getActiveDrawingLibraryInterface();
             // if no drawing library is active do nothing
             if (drawLib == null) {
                 return;
             }
             
             GraphManipulationInterface manipulationInterface =
                     drawLib.getGraphManipulationInterface();
             
             Object node = drawLib.getNodeAt(e.getPoint());
             Object edge = drawLib.getEdgeAt(e.getPoint());
             
             // Right-click event
             if (e.getButton() == MouseEvent.BUTTON3 
                     && drawLib != null) {
                 
                 if (node != null) {
                     nodePopup = new NodePopup(mainframe);
                     nodePopup.setNode((Set<GraphClass>) node);
                     nodePopup.show(e.getComponent(), e.getX(), e.getY());
                     
                 } else if (edge != null) {
                     edgePopup = new EdgePopup(mainframe);
                     edgePopup.setEdge((DefaultEdge) edge);
                     edgePopup.show(e.getComponent(), e.getX(), e.getY());
                 }
                 
             // Double-click event
             }
             
             if (e.getClickCount() == 2 && node != null 
                     && e.getButton() == MouseEvent.BUTTON1)  {
                 
                 JDialog d = new GraphClassInformationDialog(mainframe, 
                         ((Set<GraphClass>) node).iterator().next());
                 
                 d.setLocation(50, 50);
                 d.pack();
                 d.setSize(800, 600);
                 d.setVisible(true);
             }
         }
     };
 
     /**
      * tab component which adds a new tab if clicked.
      */
     private AddTabComponent addTabComponent;
     
     /**
      * Creates a new Tabbed pane with a startpage as only active tab.
      * @param parent
      *          The reference to the ISGCIMainFrame that contains this pane.
      */
     public ISGCITabbedPane(ISGCIMainFrame parent) {
         mainframe = parent;
         addStartpage();
         
         //Adding the Add-Tab Tab
         //TODO finding a better name for the Add-Tab tab
         StartPanel addTabTab = new StartPanel(mainframe);
         addTab("", addTabTab);
         setSelectedComponent(addTabTab);
         addTabComponent = new AddTabComponent(this);
         setTabComponentAt(getSelectedIndex(), addTabComponent);
         setSelectedIndex(0);
         
         addMouseListener(mouseAdapter);
         addChangeListener(changeListener);
         UserSettings.subscribeToOptionChanges(this);
     }
 
     /**
      * Adds a Startpage to the ISGCITabbedPane.
      */
     public void addStartpage() {
         StartPanel startpage = new StartPanel(mainframe);
         addTab("", startpage);
         setSelectedComponent(startpage);
         ISGCITabComponent closeButton 
             = new ISGCITabComponent(this, "Welcome to ISGCI");
         
         setTabComponentAt(getSelectedIndex(), closeButton);
       
         if (addTabComponent != null) {
             addTabComponent.resetTabPosition();
         }
     }
 
     /**
      * Removes the startpage from the ISGCITabbedPane.
      */
     private void removeStartpage() {
         if (getSelectedComponent() instanceof StartPanel) {
             remove(getSelectedComponent());
         }
     }
     
     /**
      * Adds a new tab with a graph that is drawn via the DrawingInterface.
      * Will close the startpage if it's still open.
      * @param <V>
      *          The class of the vertices.
      * @param <E>
      *          The class of the edges.
      * @param graph
      *          The graph that will be drawn and interacted with within 
      *          this tab.
      * @param name
      *          The name of the Tab
      */
     public <V, E> void drawInNewTab(Graph<V, E> graph, String name) {       
         
         final DrawingLibraryInterface<V, E> drawingInterface = 
                 DrawingLibraryFactory.createNewInterface(graph);
 
         drawingInterface.getGraphManipulationInterface().beginNotUndoable();
         
         final JComponent graphPanel = drawingInterface.getPanel();
         final JComponent graphOutline = drawingInterface.getGraphOutline(); 
         
         // layers
         JLayeredPane layeredPane = new JLayeredPane();
         layeredPane.add(graphOutline, new Integer(2));
         layeredPane.add(graphPanel, new Integer(1));
         
         
         // layeredpane has no layoutmanager and doesn't really work with one
         final int offset = 20;
         final double outlineFactor = 0.2;
         
         graphPanel.setBounds(0, 0, getWidth(), 
                 getHeight() - this.getTabComponentAt(0).getHeight() - offset);
         graphOutline.setBounds(0, 0, 0, 0);
         
         layeredPane.addComponentListener(new ComponentListener() {
             
             @Override
             public void componentShown(ComponentEvent e) { setSize(e); }
             
             @Override
             public void componentResized(ComponentEvent e) { setSize(e); }
             
             @Override
             public void componentMoved(ComponentEvent e) { setSize(e); }
             
             @Override
             public void componentHidden(ComponentEvent e) { setSize(e); }
             
             /** Sets the size. @param e The event */
             private void setSize(ComponentEvent e) {
                 graphPanel.setBounds(0, 0, e.getComponent().getWidth(), 
                         e.getComponent().getHeight());
                 
                 int outlineWidth = (int) (outlineFactor 
                         * e.getComponent().getWidth());
                 
                 int outlineHeight = (int) (outlineFactor 
                         * e.getComponent().getHeight());
                 
                 graphOutline.setBounds(
                         e.getComponent().getWidth() - offset - outlineWidth, 
                         e.getComponent().getHeight() - offset - outlineHeight, 
                         outlineWidth, outlineHeight);
             }
         });
         
         JPanel tabPanel = new JPanel(new BorderLayout());
         tabPanel.add(layeredPane, BorderLayout.CENTER);
         
         addTab("", tabPanel);
         ISGCITabComponent tabComponent = new ISGCITabComponent(this, name);
         
         // set tabcomponent as .. tabcomponent
         setSelectedComponent(tabPanel);
         setTabComponentAt(getSelectedIndex(), tabComponent);
         
         drawingInterface.getGraphEventInterface().
                 registerMouseAdapter(mouseAdapter);
          
         panelToInterfaceMap.put(tabPanel, drawingInterface);
         panelToNamingPref.put(tabPanel, defaultMode);
         
         setProperness();
         setProblem(null, tabPanel);
         applyNamingPref(tabPanel);
 
         SwingUtilities.invokeLater(new Runnable() {
             
             @Override
             public void run() {
                 drawingInterface.getGraphManipulationInterface()
                         .reapplyHierarchicalLayout();
 
                 drawingInterface.getGraphManipulationInterface()
                         .endNotUndoable();
 
             }
         });
         
         addTabComponent.resetTabPosition();
         mainframe.closeDialogs();
     }
     
     /**
      * Draws the graph in the currently active tab. If the startpage is still
      * active, the startpage will be closed and a new tab will be created
      * instead.
      * 
      * @param graph
      *          The graph that will be drawn.
      *          
      * @param <V>
      *          The class of the vertex.
      *          
      * @param <E>
      *          The class of the edge.
      *          
      * @param name
      *          The name of the Tab
      */
     public <V, E> void drawInActiveTab(Graph<V, E> graph, String name) {
         if (getSelectedComponent() == null || getTabCount() == 1 
                 || getTabComponentAt(getSelectedIndex()) 
                 instanceof AddTabComponent) {
             drawInNewTab(graph, name);
         } else if (getSelectedComponent() instanceof StartPanel) {
             remove(getSelectedComponent());
             drawInNewTab(graph, name);
         } else {
             
             final DrawingLibraryInterface dLib 
                 = getActiveDrawingLibraryInterface(); 
             
             dLib.setGraph(graph);
             
             dLib.getGraphManipulationInterface().beginNotUndoable();
             
             //reapply properness and coloring
             setProperness();
             setProblem(getProblem(getSelectedComponent()), 
                     getSelectedComponent());
             
             // set title
             ISGCITabComponent closeButton 
                 = new ISGCITabComponent(this, name);
         
             setTabComponentAt(getSelectedIndex(), closeButton);
             applyNamingPref(getSelectedComponent());
             
             SwingUtilities.invokeLater(new Runnable() {
                 
                 @Override
                 public void run() {
                     dLib.getGraphManipulationInterface()
                             .reapplyHierarchicalLayout();
 
                     dLib.getGraphManipulationInterface().endNotUndoable();
 
                 }
             });
             
             
             mainframe.closeDialogs();
         }
     }
     
     /**
      * Returns the active DrawingLibraryInterface.
      * 
      * @return
      *          The DrawingLibraryInterface that is associated with the 
      *          currently active tab.
      */
     public DrawingLibraryInterface getActiveDrawingLibraryInterface() {
         Component panel = getSelectedComponent();
         
         if (!panelToInterfaceMap.containsKey(panel) 
                 || panel instanceof StartPanel) {
             return null;
         }
         
         return panelToInterfaceMap.get(panel);
     }
     
     /**
      * @param c 
      *          The tab for which the naming preference is wanted.
      * 
      * @return 
      *          The default naming preference of the given Tab.
      */
     public NamePref getNamingPref() {
         if (panelToNamingPref.containsKey(getSelectedComponent())) {
             return panelToNamingPref.get(getSelectedComponent());
         } else {
             return defaultMode;
         }
     }
 
     /**
      * Changes the naming preferences of target tab.
      * 
      * @param pref
      *          The new default naming preference of this tab.
      * @param c
      *          The Tab for which the naming preference is changed.
      */
     public void setNamingPref(NamePref pref) {
         if (pref != null 
                 && getTabComponentAt(getSelectedIndex()) != addTabComponent) {
             panelToNamingPref.put((JComponent) getSelectedComponent(), pref);
             applyNamingPref(getSelectedComponent());
         }
        
        getActiveDrawingLibraryInterface().getGraphManipulationInterface()
                       .reapplyHierarchicalLayout();
     }
     
     /**
      * Applies the naming preference of a given tab on each of its nodes.
      * 
      * @param c
      *         the tab on which the nodes will be renamed
      */
     private void applyNamingPref(Component c) {
         DrawingLibraryInterface graphInterface = panelToInterfaceMap.get(c);
         
         if (graphInterface == null) {
             return;
         }
         
         Graph graph = graphInterface.getGraph();
         Algo.NamePref namePref;
         if (panelToNamingPref.containsKey(c)) {
             namePref = panelToNamingPref.get(c);
         } else {
             namePref = defaultMode;
         }
         
         graphInterface.getGraphManipulationInterface().beginUpdate();
         for (Object node : graph.vertexSet()) {
             String newName = Algo.getName((Set<GraphClass>) node, namePref);
             graphInterface.getGraphManipulationInterface()
                 .renameNode(node, newName);
         }
         graphInterface.getGraphManipulationInterface().endUpdate();
     }
 
     /**
      * Sets the drawUnproper value for the currently open tab.
      * 
      * @param c
      *          The Tab for which the drawUnproper state is changed.
      * @param state
      *          the new drawUnproper state of the open tab
      */
     public void setDrawUnproper(boolean state, Component c) {
         if (getTabComponentAt(getSelectedIndex()) != addTabComponent) {
             panelToDrawUnproper
                 .put((JComponent) getSelectedComponent(), state);
             setProperness();
         }
     }
     
     /**
      * @param c
      *          The tab for which the drawUnproper state is wanted.
      * @return
      *          true if unproper inclusions shall be drawn, else false.
      */
     public boolean getDrawUnproper(Component c) {
         if (!panelToDrawUnproper.containsKey(getSelectedComponent())) {
             return true;
         } else {
             return (panelToDrawUnproper.get(getSelectedComponent()));
         }
     }
 
     /**
      * Sets the problem for a given tab. The problem of a tab determines the
      * coloring of the graph on it.
      * 
      * @param problem
      *          the new problem of the tab.
      *          
      * @param c
      *          the tab for which the problem is changed.
      */
     private void setProblem(Problem problem, Component c) {
         
         if (getSelectedComponent() instanceof StartPanel  
                 || !panelToInterfaceMap.containsKey(c)) { 
             return; 
         }        
         Graph graph = panelToInterfaceMap.get(c).getGraph();
         
         HashMap<Color , List<Set<GraphClass>>> colorToNodes = 
                 new HashMap<Color, List<Set<GraphClass>>>();
         
         
         // Put all nodes with the same color in a list
         for (Object o : graph.vertexSet()) {
             Set<GraphClass> node = (Set<GraphClass>) o;
             Color color = complexityColor(node);
             if (!colorToNodes.containsKey(color)) {
                 colorToNodes.put(color, new ArrayList<Set<GraphClass>>());
             }
             colorToNodes.get(color).add(node);           
         }
         
         
         // Color all lists with their color
         GraphManipulationInterface gmi 
         = getActiveDrawingLibraryInterface().getGraphManipulationInterface(); 
         
         gmi.beginNotUndoable();
         gmi.beginUpdate();
         
         try {
             
             for (Color color : colorToNodes.keySet()) {
                 gmi.colorNode(colorToNodes.get(color).toArray(), color);
             }
             
             //set special colors
             gmi.setFontColor(UserSettings.getCurrentFontColor());
             gmi.setBackgroundColor(UserSettings.getCurrentBackgroundColor());
             gmi.setHighlightColor(UserSettings.getCurrentHighlightColor());
             gmi.setSelectionColor(UserSettings.getCurrentSelectionColor());
             
             
         } finally {
             gmi.endUpdate();
             gmi.endNotUndoable();
         }
         
         getSelectedComponent().repaint();
     }
     
     /**
      * Gives the problem for a given tab. The problem of a tab determines the
      * coloring of the graph on it.
      * 
      * @param c
      *          the tab for which the problem is wanted
      *          
      * @return
      *          the problem of the given tab, 
      *          null or "none" if no problem is chosen
      */
     private Problem getProblem(Component c) {
         try {
             Component pre = getSelectedComponent();
             setSelectedComponent(c);
             Problem retValue = UserSettings.getProblem();
             setSelectedComponent(pre);
             return retValue;
         } catch (Exception e) {
             return null;
         }
     }
 
     /**
      * Returns the properness of a given edge, in a given graph.
      * 
      * @param graph
      *          the graph in which the edge is in
      * 
      * @param edge
      *          the edge which is checked for properness.
      *          
      * @return
      *          true if the edge is proper, false otherwise.
      */
     private boolean getProperness(Graph graph, DefaultEdge edge){
         GraphClass source = (GraphClass) ((Set<GraphClass>) 
                             graph.getEdgeSource(edge)).iterator().next();
         GraphClass target = (GraphClass) ((Set<GraphClass>) 
                             graph.getEdgeTarget(edge)).iterator().next();
         List<Inclusion> path = GAlg.getPath(DataSet.inclGraph, source, target);
         return (Algo.isPathProper(path)  
                 || Algo.isPathProper(Algo.makePathProper(path)));
     }
     
     /**
      * Marks edges of the currently active tab, depending on it's drawUnproper
      * state.
      */
     private void setProperness() {
         if (getSelectedComponent() instanceof StartPanel  
                 || getActiveDrawingLibraryInterface() == null) {
             return; }
         Graph graph = getActiveDrawingLibraryInterface().getGraph();
         List<DefaultEdge> markEdges = new ArrayList<DefaultEdge>();
         List<DefaultEdge> unmarkEdges = new ArrayList<DefaultEdge>();
         for (Object o : graph.edgeSet()) {
             DefaultEdge edge = (DefaultEdge) o;
             boolean isProper = getProperness(graph, edge);
             if (!isProper && getDrawUnproper(getSelectedComponent())) {
                 markEdges.add(edge);
             } else if (!isProper 
                         && !getDrawUnproper(getSelectedComponent())){
                 unmarkEdges.add(edge);
             }
         }
         if (markEdges.size() > 0) {
             getActiveDrawingLibraryInterface().
             getGraphManipulationInterface().markEdge(markEdges.toArray());
         } else if (unmarkEdges.size() > 0) {
             getActiveDrawingLibraryInterface().
             getGraphManipulationInterface().unmarkEdge(unmarkEdges.toArray());
         } 
     }
     
     /**
      * @param node 
      *          the node for which the color is returned.
      * 
      * @return 
      *          the color for node considering its complexity for the active
      *          problem.
      */
     protected Color complexityColor(Set<GraphClass> node) {
         Problem problem = getProblem(getSelectedComponent());
         
         if (problem != null) {
             Complexity complexity = 
                     problem.getComplexity(node.iterator().next());
             return UserSettings.getColor(complexity);
         }
         
         return UserSettings.getColor(Complexity.UNKNOWN);
     }
 
     @Override
     public void updateOptions() {
         // Reapply color, to change all nodes to the new color scheme
         for (Component tab : this.getComponents()) {
             setProblem(getProblem(tab), tab);
         }
         
         if (!getNamingPref().equals(UserSettings
                 .getNamingPref((JComponent) getSelectedComponent()))) {
             setNamingPref(UserSettings.getNamingPref(
                     (JComponent) getSelectedComponent()));
         }
     }
 }
 
 /* EOF */
