 package it.wolfed.swing;
 
 import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
 import com.mxgraph.layout.mxCompactTreeLayout;
 import com.mxgraph.layout.mxOrganicLayout;
 import com.mxgraph.view.mxGraph;
 import it.wolfed.model.PetriNetGraph;
 import it.wolfed.operation.AlternationOperation;
 import it.wolfed.operation.CloneGraphOperation;
 import it.wolfed.operation.DefferedChoiceOperation;
 import it.wolfed.operation.ExplicitChoiceOperation;
 import it.wolfed.operation.FullMergeOperation;
 import it.wolfed.operation.Operation;
 import it.wolfed.operation.MutualExclusionOperation;
 import it.wolfed.operation.OneOrMoreIterationOperation;
 import it.wolfed.operation.OneServePerTimeOperation;
 import it.wolfed.operation.ParallelismOperation;
 import it.wolfed.operation.SequencingOperation;
 import it.wolfed.operation.ZeroOrMoreIterationOperation;
 import it.wolfed.util.Constants;
 import it.wolfed.util.IterableNodeList;
 import java.awt.Component;
 import java.awt.Toolkit;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
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
 import javax.xml.transform.TransformerException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.xml.sax.SAXException;
 
 /**
  * Wolfed. WOrkflow Light Fast EDitor.
  *
  * @see <a href="https://github.com/Dewos/wolfed">Github Repository</a>
  * @see <a href="http://www.cli.di.unipi.it/~rbruni/MPB-12/index.html">MPB Course</a>
  * @author Fabio Piro
  * @author Said Daoudagh
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
         (new WolfedEditor()).setVisible(true);
     }
     
     /**
      * Index of the graphs opened in the editor.
      * 
      * Must be in sync with any new graph inserts.
      */
     private int indexOpenedGraphs;
     
     /**
      * Tabs controller.
      *
      * A tab contains an GraphContainer with the GraphComponent and
      * AnalysisComponent.
      */
     private JTabbedPane tabs = new JTabbedPane();
  
     /**
      * Constructor.
      */
     public WolfedEditor()
     {
         setTitle(Constants.EDITOR_NAME + " " + Constants.EDITOR_VERSION);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setJMenuBar(new MenuBarController(this));
         getContentPane().add(tabs);
         setLookAndFeel();
     }
 
     /**
      * Returns all the opened graphs in the editor.
      *
      * @return List<PetriNetGraph>
      */
     public List<PetriNetGraph> getOpenedGraphs()
     {
         List<PetriNetGraph> openedGraphs = new ArrayList<>();
         
         for(Component graphComponent : tabs.getComponents())
         {
             if(graphComponent instanceof GraphViewContainer)
             {
                 openedGraphs.add(((GraphViewContainer) graphComponent).getGraph());
             }
         }
         
         return openedGraphs;
     }
     
     /**
      * Returns the selected graph.
      *
      * @return PetriNetGraph
      */
     public PetriNetGraph getSelectedGraph()
     {
         return ((GraphViewContainer) tabs.getSelectedComponent()).getGraph();
     }
 
     /**
      * Sets look and feel.
      */
     private void setLookAndFeel()
     {
         try
         {
             // Sets system look
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 
             // Force Numbus (for Said happiness )
             for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
             {
                 if ("Nimbus".equals(info.getName()))
                 {
                     UIManager.setLookAndFeel(info.getClassName());
                     break;
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
             showErrorMessage(ex);
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
         tabs.setTabComponentAt(tabs.getTabCount() - 1, new ButtonTabComponent(tabs, tabName));
         tabs.setSelectedIndex(tabs.getTabCount() - 1);
         indexOpenedGraphs++;
         return graph;
     }
 
     /**
      * Add a new empty graph in the editor.
      *
      * @return PetriNetGraph
      */
     public PetriNetGraph newFile()
     {
         String name = "new_" + String.valueOf(indexOpenedGraphs + 1);
         return insertGraph(name, new PetriNetGraph(name));
     }
 
     /**
      * Open the filechooser and import a valid xml\pnml file.
      */
     public void openFile()
     {
         JFileChooser fileChooser = new JFileChooser(".");
         fileChooser.setFileFilter(new FileNameExtensionFilter("xml, pnml", "xml", "pnml"));
         fileChooser.setCurrentDirectory(new File("nets"));
 
         if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
         {
             File file = fileChooser.getSelectedFile();
             importFile(file);
         }
     }
 
     /**
      * Import a pnml file. Note: A pnml file can contains one or MORE nets.
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
             showErrorMessage(ex);
         }
     }
 
     /**
      * Exports a graph in a new file.
      *
      * @param exportType
      * @see {@link Constants#EDITOR_EXPORT_PNML}
      * @see {@link Constants#EDITOR_EXPORT_DOT}
      */
     public void saveFile(String exportType)
     {
         try
         {
             JFileChooser fileChooser = new JFileChooser(".");
             fileChooser.setSelectedFile(new File(getSelectedGraph().getId() + exportType));
             
             int returnVal = fileChooser.showSaveDialog(this);
             if (returnVal == JFileChooser.APPROVE_OPTION)
             {                
                 if (fileChooser.getSelectedFile().exists())
                 {
                     int overrideFile = JOptionPane.showConfirmDialog(
                             this,
                             "The file exists, overwrite?",
                             "Existing file",
                             JOptionPane.YES_NO_CANCEL_OPTION
                     );
 
                     switch (overrideFile)
                     {
                         case JOptionPane.NO_OPTION:
                         {   
                             saveFile(exportType);
                             break;
                         }
                         case JOptionPane.CANCEL_OPTION: 
                         case JOptionPane.CLOSED_OPTION:
                         {
                             return;
                         }
                     }
                 }
 
                 File exportedFile = fileChooser.getSelectedFile();
                 String exportedGraph = "";
 
                 switch (exportType)
                 {
                     case Constants.EDITOR_EXPORT_PNML:
                     {
                         exportedGraph = getSelectedGraph().exportPNML();
                         break;
                     }
 
                     case Constants.EDITOR_EXPORT_DOT:
                     {
                         exportedGraph = getSelectedGraph().exportDOT();
                         break;
                     }
                 }
 
                 BufferedWriter pnmlWriter = new BufferedWriter(new FileWriter(exportedFile.getCanonicalPath()));
                 pnmlWriter.write(exportedGraph);
                 pnmlWriter.flush();
             }
         }
         catch (TransformerException | ParserConfigurationException | IOException ex)
         {
             showErrorMessage(ex);
         }
     }
 
     /**
      * Execute an operation (composition) to a graph.
      *
      * The graph can be a valid workflownet or a simple petrinet. Specific
      * checks will be made in Operation().
      *
      * @todo refactor this
      * @param operationName
      */
     public void executeOperation(String operationName)
     {
         Operation operation = null;
        PetriNetGraph operationGraph = new PetriNetGraph("new_" + (indexOpenedGraphs + 1));
 
         try
         {
             switch (operationName)
             {
                 case Constants.OPERATION_ALTERNATION:
                 {
                     OperationDialog selectionBox = new OperationDialog(getOpenedGraphs(), 1);
                     if(selectionBox.getSelectedGraphs().size() > 0){
                         operation = new AlternationOperation(operationGraph, getSelectedGraph(), selectionBox.getSelectedGraphs().get(0));
                     }
                     break;
                 }
                     
                 case Constants.OPERATION_CLONEGRAPH:
                 {
                     operation = new CloneGraphOperation(operationGraph, getSelectedGraph());
                     break;
                 }
 
 
                 case Constants.OPERATION_DEFFEREDCHOICE:
                 {
                     OperationDialog selectionBox = new OperationDialog(getOpenedGraphs(), 1);
                     if(selectionBox.getSelectedGraphs().size() > 0){
                         operation = new DefferedChoiceOperation(operationGraph, getSelectedGraph(), selectionBox.getSelectedGraphs().get(0));
                     }
                     break;
                 }
 
                 case Constants.OPERATION_EXPLICITCHOICE:
                 {
                     OperationDialog selectionBox = new OperationDialog(getOpenedGraphs(), 1);
                     if(selectionBox.getSelectedGraphs().size() > 0)
                     {
                         operation = new ExplicitChoiceOperation(operationGraph, getSelectedGraph(), selectionBox.getSelectedGraphs().get(0));
                     }
                     break;
                 }
 
                 case Constants.OPERATION_ITERATIONONEORMORE:
                 {
                     operation = new OneOrMoreIterationOperation(operationGraph, getSelectedGraph());
                     break;
                 }
 
                 case Constants.OPERATION_ITERATIONONESERVEPERTIME:
                 {
                     operation = new OneServePerTimeOperation(operationGraph, getSelectedGraph());
                     break;
                 }
 
                 case Constants.OPERATION_ITERATIONZEROORMORE:
                 {
                     operation = new ZeroOrMoreIterationOperation(operationGraph, getSelectedGraph());
                     break;
                 }
 
                 case Constants.OPERATION_MUTUALEXCLUSION:
                 {
                     OperationDialog selectionBox = new OperationDialog(getOpenedGraphs(), 1);
                     if(selectionBox.getSelectedGraphs().size() > 0)
                     {
                         operation = new MutualExclusionOperation(operationGraph, getSelectedGraph(), selectionBox.getSelectedGraphs().get(0));
                     }
                     break;
                 }
 
                 case Constants.OPERATION_FULLMERGE:
                 {
                     OperationDialog selectionBox = new OperationDialog(getOpenedGraphs(), 1);
                     if(selectionBox.getSelectedGraphs().size() > 0)
                     {
                         operation = new FullMergeOperation(operationGraph, getSelectedGraph(), selectionBox.getSelectedGraphs().get(0));
                     }
                     break;
                 }
 
                 case Constants.OPERATION_PARALLELISM:
                 {
                     OperationDialog selectionBox = new OperationDialog(getOpenedGraphs(), 1);
                     if(selectionBox.getSelectedGraphs().size() > 0)
                     {
                         operation = new ParallelismOperation(operationGraph, getSelectedGraph(), selectionBox.getSelectedGraphs().get(0));
                     }
                     break;
                 }
 
                 case Constants.OPERATION_SEQUENCING:
                 {
                     OperationDialog selectionBox = new OperationDialog(getOpenedGraphs(), 1);
                     if(selectionBox.getSelectedGraphs().size() > 0)
                     {
                         operation = new SequencingOperation(operationGraph, getSelectedGraph(), selectionBox.getSelectedGraphs().get(0));
                     }
                     break;
                 }
             }
             
             if(operation != null)
             {
                 operationGraph = operation.getOperationGraph();
                 insertGraph(operationGraph.getId(), operationGraph);
                 executeLayout(operationGraph, Constants.LAYOUT_VERTICALTREE);
             }
         }
         catch (Exception ex)
         {
             showErrorMessage(ex);
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
         if (graph == null)
         {
             graph = getSelectedGraph();
         }
 
         Object parent = graph.getDefaultParent();
 
         switch (layoutName)
         {
             case Constants.LAYOUT_VERTICALTREE:
             {
                 (new mxCompactTreeLayout(graph)).execute(parent);
                 break;
             }
 
             case Constants.LAYOUT_HORIZONTALTREE:
             {
                 (new mxCompactTreeLayout(graph, true)).execute(parent);
                 break;
             }
 
             case Constants.LAYOUT_HIERARCHICAL:
             {
                 (new mxHierarchicalLayout(graph)).execute(parent);
                 break ;
             }
                
             case Constants.LAYOUT_ORGANIC:
             {
                 (new mxOrganicLayout(graph)).execute(parent);
                 break;
             }
         }
     }
     
     /**
      * Show About Message
      */
     public void showAbout()
     {
         JOptionPane.showMessageDialog(this,
             Constants.EDITOR_ABOUT,
             "About",
             JOptionPane.INFORMATION_MESSAGE
         );
     }
 
     /**
      * Shows an error message.
      *
      * @param ex
      */
     @SuppressWarnings("CallToThreadDumpStack")
     public void showErrorMessage(Exception ex)
     {
         JOptionPane.showMessageDialog(this,
                 ex.getMessage(),
                 "Error",
                 JOptionPane.ERROR_MESSAGE);
 
         ex.printStackTrace();
     }
 }
