 package gui;
 
 /**
  * Created with IntelliJ IDEA.
  * User: i.k.
  * Date: 13.06.13
  * Time: 11:38
  */
 
 import graph.Graph;
 
 import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ContainerAdapter;
import java.io.File;
 import java.io.FileReader;
 
 public class GraphForm extends Component {
     private JPanel boringPanel;
     private JLabel captionLabel;
     private JLabel numberOfLayersCaptionLabel;
     private JLabel numberOfLayersLabel;
     private JPanel graphPanel;
     private JPanel wrapperPanel;
     private JButton loadButton;
     private JTextArea matrixTextArea;
     private JButton commitButton;
 
     private Graph graph;
 
     public static void main(String[] args) {
         JFrame frame = new JFrame("GraphForm");
         frame.setContentPane(new GraphForm().wrapperPanel);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.pack();
         frame.setVisible(true);
     }
 
     public GraphForm(){
         captionLabel.setText("Матрица смежности");
         numberOfLayersCaptionLabel.setText("Количество слоев:");
         loadButton.setText("Загрузить");
         commitButton.setText("Пересчитать");
 
         loadButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 JFileChooser jfc = new JFileChooser();
                 int returnVal = jfc.showOpenDialog(GraphForm.this);
                 if (returnVal == JFileChooser.APPROVE_OPTION) {
                     try {
                         int[][] arr = Graph.readFromFile(new FileReader(jfc.getSelectedFile().getPath()));
                         graph = new Graph(arr.length);
                         graph.setGraphFromMatrix(arr);
 
                         int[][] matrix = graph.buildAdjacencyMatrix();
                         String matrixToText = "";
                         for(int[] r : matrix){
                             for (int c : r)
                                 matrixToText += c + " ";
                             matrixToText += "\n";
                         }
 
                         matrixTextArea.setText(matrixToText);
 
                        //graph.findLayers();
                         numberOfLayersLabel.setText(String.valueOf(graph.getLayers()));
 
                         graphPanel = new GraphPanel(graph);
                     } catch (Exception e1) {
                        e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                     }
                 }
                 else {
                     System.err.println("Open command cancelled by user.");
                 }
             }
         });
         commitButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
 
             }
         });
     }
 
     public JPanel getGraphPanel() {
         return graphPanel;
     }
 
     public void setGraphPanel(JPanel graphPanel) {
         this.graphPanel = graphPanel;
     }
 
     public JPanel getWrapperPanel() {
         return wrapperPanel;
     }
 
     public void setWrapperPanel(JPanel wrapperPanel) {
         this.wrapperPanel = wrapperPanel;
     }
 
 
 }
