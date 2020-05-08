 package it.wolfed.swing;
 
 import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
 import com.mxgraph.layout.mxCompactTreeLayout;
 import com.mxgraph.layout.mxOrganicLayout;
 import com.mxgraph.view.mxGraph;
 import it.wolfed.model.PetriNetGraph;
 import it.wolfed.operations.AlternationOperation;
 import it.wolfed.operations.DefferedChoiceOperation;
 import it.wolfed.operations.ExplicitChoiceOperation;
 import it.wolfed.operations.MutualExclusionOperation;
 import it.wolfed.operations.OneOrMoreIterationOperation;
 import it.wolfed.operations.OneServePerTimeOperation;
 import it.wolfed.operations.ParallelismOperation;
 import it.wolfed.operations.SequencingOperation;
 import it.wolfed.operations.ZeroOrMoreIterationOperation;
 import it.wolfed.util.Constants;
 import it.wolfed.util.IterableNodeList;
 import java.awt.Component;
 import java.awt.Toolkit;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JTabbedPane;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.xml.sax.SAXException;
 
 /**
  * Wolfed.
  * WOrkflow Light Fast EDitor.
  * 
  * @see     <a href="https://github.com/Dewos/wolfed">Github Repository</a>
  * @see     <a href="http://www.cli.di.unipi.it/~rbruni/MPB-12/index.html">MPB Course</a>
  * @author  Fabio Piro
  * @author  Said Daoudagh
  */
 public class WolfedEditor extends JFrame
 {
     /**
      * Run WolfedEditor.
      * 
      * @param args 
      */
     public static void main(String[] args) 
     {
         Component editor = new WolfedEditor();
         editor.setVisible(true);
     }
 
     /**
      * Specifies current version.
      */
     public static final String VERSION = "0.9.9.0";
     
     /**
      * Holds opened graphs available in editor tabs.
      */
     private List<PetriNetGraph> openedGraphs = new ArrayList<>();
     
     /**
      * Tabs controller. 
      * 
      * A tab contains an GraphContainer
      * with the GraphComponent and AnalysisComponent.
      */
     private JTabbedPane tabs = new JTabbedPane();
     
     /**
      * Available operations (in menu).
      */
     private String[] operations =
     {
         Constants.OPERATION_ALTERNATION,
         Constants.OPERATION_DEFFEREDCHOICE,
         Constants.OPERATION_EXPLICITCHOICE,
         Constants.OPERATION_ITERATIONONEORMORE,
         Constants.OPERATION_ITERATIONONESERVEPERTIME,
         Constants.OPERATION_ITERATIONZEROORMORE,
         Constants.OPERATION_MUTUALEXCLUSION,
         Constants.OPERATION_PARALLELISM,
         Constants.OPERATION_SEQUENCING
     };
     
     /**
      * Available layouts (in menu).
      */
     private String[] layouts =
     {
         Constants.LAYOUT_VERTICALTREE,
         Constants.LAYOUT_HIERARCHICAL,
         Constants.LAYOUT_ORGANIC,
     };
 
     /**
      * Constructor.
      */
     public WolfedEditor()
     { 
         setTitle("Wolfed " + WolfedEditor.VERSION);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setJMenuBar(new MenuBarController(this));
         getContentPane().add(tabs);
         setLookAndFeel();
     }
     
     /**
      * Returns all the available operations.
      *
      * @return String[]
      */
     public String[] getOperations()
     {
         return operations;
     }
 
     /**
      * Returns all the available layouts.
      *
      * @return String[]
      */
     public String[] getLayouts()
     {
         return layouts;
     }
 
     /**
      * Returns all the opened graphs in the editor.
      *
      * @return List<PetriNetGraph>
      */
     public List<PetriNetGraph> getOpenedGraphs()
     {
         return openedGraphs;
     }
     
     /**
      * Returns the selected graph.
      *
      * @return PetriNetGraph
      */
     public PetriNetGraph getSelectedGraph()
     {
         GraphViewContainer view = (GraphViewContainer) tabs.getSelectedComponent();
         return view.getGraph();
     }
 
     /**
      * Sets look and feel.
      */
     private void setLookAndFeel()
     {        
         try
         {
             // Sets system look
             for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) 
             {
                 if ("Nimbus".equals(info.getName())) 
                 {
                     UIManager.setLookAndFeel(info.getClassName());
                 }
                 else
                 {
                     UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                 }
             }
             
             // Force fullScreen
             Toolkit tk = Toolkit.getDefaultToolkit();
             int xSize = ((int) tk.getScreenSize().getWidth());
             int ySize = ((int) tk.getScreenSize().getHeight() - 40);
             this.setSize(xSize, ySize);
         } 
         catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex)
         {
             showErrorMessage(ex.getMessage());
         }
     }
 
     /**
      * Insert a new tab\graph in the editor and selects it.
      *
      * @param tabName
      * @param graph
      * @return PetriNetGraph
      */
     public PetriNetGraph insertGraph(String tabName, PetriNetGraph graph)
     {
         tabs.add(tabName, new GraphViewContainer(graph));
         tabs.setSelectedIndex(tabs.getTabCount() - 1);
         getOpenedGraphs().add(graph);
         
         return graph;
     }
 
     /**
      * Add a new empty graph in the editor.
      *
      * @return PetriNetGraph
      */
     public PetriNetGraph newFile()
     {
         String name = "new_" + String.valueOf(tabs.getTabCount() + 1);
         PetriNetGraph graph = new PetriNetGraph(name);
         insertGraph(name, graph);
         
         return graph;
     }
 
     /**
      * Open the filechooser and import a valid xml\pnml file.
      */
     public void openFile()
     {
         JFileChooser fc = new JFileChooser(".");
         fc.setFileFilter(new FileNameExtensionFilter("xml, pnml", "xml", "pnml"));
         fc.setCurrentDirectory(new File("nets"));
 
         if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
         {
             File file = fc.getSelectedFile();
             importFile(file);
         }
     }
 
     /**
      * Import a pnml file. 
      * Note: A pnml file can contains one or MORE nets.
      *
      * @see <a href="http://www.pnml.org/">http://www.pnml.org/</a>
      * @param File pnml complaint file
      */
     private void importFile(File filePnml)
     {
         try
         {
             DocumentBuilder builder = DocumentBuilderFactory
                     .newInstance()
                     .newDocumentBuilder();
 
             Document doc = builder.parse(filePnml);
             doc.getDocumentElement().normalize();
 
             for (final Node netNode : new IterableNodeList(doc.getElementsByTagName(Constants.PNML_NET)))
             {
                 // Sets the graph id as the filename without ext
                 String defaultId = filePnml.getName().substring(0, filePnml.getName().lastIndexOf('.'));
                 insertGraph(filePnml.getName(), PetriNetGraph.factory(netNode, defaultId));
             }
         } 
         catch (ParserConfigurationException | SAXException | IOException ex)
         {
             showErrorMessage(ex.getMessage());
         }
     }
 
     /**
      * Exports a graph in a new file.
      * 
      * @param graph 
      * @todo
      */
     public void saveFile(PetriNetGraph graph)
     {
          showErrorMessage("Not supported yet");
     }
 
     /**
      * Execute an operation (composition) to a graph.
      * 
      * The graph can be a valid workflownet or a simple
      * petrinet. Specific checks will be made in Operation().
      *
      * @todo  refactor this
      * @param operationName
      */
     public void executeOperation(String operationName)
     {
         PetriNetGraph opGraph = null;
         OperationDialog selectionBox;
         
         // Shortcut for the operations that required
         // only an input graph (selected graph is always selected)
         List<PetriNetGraph> singleInputGraph = new ArrayList<>();
         singleInputGraph.add(getSelectedGraph());
         
         try
         { 
             switch (operationName)
             {
                 case Constants.OPERATION_ALTERNATION:
                      selectionBox = new OperationDialog(this, 1);
                     
                     selectionBox.setExtendedGraph();
                     opGraph = (new AlternationOperation(selectionBox.getInputGraphs(), selectionBox.getExtendedSelectedGraphs())).getOperationGraph();
                     break;
                     
                 case Constants.OPERATION_DEFFEREDCHOICE:
                     selectionBox = new OperationDialog(this, 1);
                     opGraph = (new DefferedChoiceOperation(selectionBox.getSelectedGraphs())).getOperationGraph();
                     break;
                     
                 case Constants.OPERATION_EXPLICITCHOICE:
                     selectionBox = new OperationDialog(this, 1);
                     opGraph = (new ExplicitChoiceOperation(selectionBox.getSelectedGraphs())).getOperationGraph();
                     break;
                     
                 case Constants.OPERATION_ITERATIONONEORMORE:
                     opGraph = (new OneOrMoreIterationOperation(singleInputGraph)).getOperationGraph();
                     break;
                     
                 case Constants.OPERATION_ITERATIONONESERVEPERTIME:
                     opGraph = (new OneServePerTimeOperation(singleInputGraph)).getOperationGraph();
                     break;
                     
                 case Constants.OPERATION_ITERATIONZEROORMORE:
                     opGraph = (new ZeroOrMoreIterationOperation(singleInputGraph)).getOperationGraph();
                     break;
                     
                 case Constants.OPERATION_MUTUALEXCLUSION:
                    selectionBox = new OperationDialog(this, 1);
                     selectionBox.setExtendedGraph();
                     opGraph = (new MutualExclusionOperation(selectionBox.getInputGraphs(), selectionBox.getExtendedSelectedGraphs())).getOperationGraph();
                     break;
                     
                 case Constants.OPERATION_PARALLELISM:
                     selectionBox = new OperationDialog(this, 1);
                     opGraph = (new ParallelismOperation(selectionBox.getSelectedGraphs())).getOperationGraph();
                     break;
                     
                 case Constants.OPERATION_SEQUENCING:
                     selectionBox = new OperationDialog(this, 1);
                     opGraph = (new SequencingOperation(selectionBox.getSelectedGraphs())).getOperationGraph();
                     break;
             }
             
             insertGraph(opGraph.getId(), opGraph);
             executeLayout(opGraph, Constants.LAYOUT_HORIZONTALTREE);
         } 
         catch (Exception ex)
         {
             showErrorMessage(ex.getMessage());
         }
     }
     
     /**
      * Styling a graph with a specific layout.
      *
      * @param graph 
      * @param layoutName
      */
     public void executeLayout(mxGraph graph, String layoutName)
     {
         if(graph == null)
         {
             graph = getSelectedGraph();
         }
         
         Object parent = graph.getDefaultParent();
         
         switch (layoutName)
         {
             case Constants.LAYOUT_VERTICALTREE:
                 (new mxCompactTreeLayout(graph)).execute(parent);
                 break;
 
             case Constants.LAYOUT_HORIZONTALTREE:
                 (new mxCompactTreeLayout(graph, true)).execute(parent);
                 break;
 
             case Constants.LAYOUT_HIERARCHICAL:
                 (new mxHierarchicalLayout(graph)).execute(parent);
                 break;
 
             case Constants.LAYOUT_ORGANIC:
                 (new mxOrganicLayout(graph)).execute(parent);
                 break;
         }
     }
     
     /**
      * Shows an error message.
      * 
      * @param text 
      */
     public void showErrorMessage(String text)
     {
         JOptionPane.showMessageDialog(this,
             text,
             "Error",
             JOptionPane.ERROR_MESSAGE);
         
         System.err.println(text);
     }
 }
