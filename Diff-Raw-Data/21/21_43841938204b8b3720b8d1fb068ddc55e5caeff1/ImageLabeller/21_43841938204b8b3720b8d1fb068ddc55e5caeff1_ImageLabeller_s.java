 package src;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicReference;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 
 /**
  * Main class of the program. Instantiates the JFrame etc etc.
  */
 public class ImageLabeller {
 
     public static final JFileChooser fileChooser = new JFileChooser();
 
     public static void main(String argv[]) {
         // The user may pass in an initial image name at the command line.
         final String imageName = (argv.length > 0) ? argv[0] : "";
 
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 final JFrame f = new JFrame();
                 f.setLayout(new BorderLayout());
                 f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
                 JPanel contentPane = new JPanel();
                 f.setContentPane(contentPane);
 
                 final ImagePanel imagePanel = new ImagePanel(imageName);
 
                 JPanel sidePanel = new JPanel();
                 GridLayout sidePanelLayout = new GridLayout(2, 1);
                 sidePanelLayout.setVgap(20);
                 sidePanel.setLayout(sidePanelLayout);
 
                 // Here is the beginning of the toolbox code
                 final JPanel toolboxPanel = new JPanel();
 
                 AtomicReference<JPanel> toolBoxReferencePanel = new AtomicReference<JPanel>(
                         toolboxPanel);
                 final LabelPanel labelPanel = new LabelPanel(toolBoxReferencePanel);
 
                 JButton newPolyButton = new JButton("Done");
                 newPolyButton.setMnemonic(KeyEvent.VK_N);
                 newPolyButton.setSize(50, 20);
                 newPolyButton.setEnabled(true);
                 newPolyButton.addActionListener(new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         imagePanel.addNewPolygon();
                         toolboxPanel.setVisible(false);
                     }
                 });
                 newPolyButton.setToolTipText("Finish editing polygon");
 
                 toolboxPanel.add(newPolyButton);
 
                 JButton undoButton = new JButton(new ImageIcon("hci/icons/undo.png"));
                 undoButton.addActionListener(new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         imagePanel.undo();
                     }
                 });
                 undoButton.setToolTipText("Undo");
 
                 JButton redoButton = new JButton(new ImageIcon("hci/icons/redo.png"));
                 redoButton.addActionListener(new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         imagePanel.redo();
                     }
                 });
                 undoButton.setToolTipText("Redo");
 
                 JButton cancelButton = new JButton("Cancel");
                 cancelButton.addActionListener(new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         imagePanel.cancel();
                     }
                 });
 
                 JButton openButton = new JButton("Open");
                 openButton.addActionListener(new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         int returnValue = fileChooser.showOpenDialog(f);
                         if (returnValue == JFileChooser.APPROVE_OPTION) {
                             imagePanel.setImageFile(fileChooser.getSelectedFile());
                             labelPanel.clearLabels();
                         }
                     }
                 });
 
                 JButton saveButton = new JButton("Save");
                 saveButton.addActionListener(new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         int returnValue = fileChooser.showSaveDialog(f);
                         if (returnValue == JFileChooser.APPROVE_OPTION) {
                             File chosenFile = fileChooser.getSelectedFile();
                             try {
                                 BufferedWriter out = new BufferedWriter(new FileWriter(chosenFile));
                                 List<String> labels = labelPanel.getLabels();
                                 for (String label : labels) {
                                     out.write(label);
                                     out.newLine();
                                 }
                                 out.close();
                             } catch (IOException e1) {
                                 // TODO Auto-generated catch block
                                 e1.printStackTrace();
                             }
 
                         }
                     }
                 });
 
                 toolboxPanel.add(undoButton);
                 toolboxPanel.add(redoButton);
                 toolboxPanel.add(cancelButton);
                 // Here is the end of the toolbox code
 
                 sidePanel.add(labelPanel);
                 sidePanel.add(toolboxPanel);
                 toolboxPanel.setVisible(false);

                 JPanel somePanel = new JPanel();
                 somePanel.add(openButton);
                 somePanel.add(saveButton);
                 f.add(somePanel, BorderLayout.NORTH);
                f.add(sidePanel, BorderLayout.EAST);
                f.add(imagePanel, BorderLayout.CENTER);
 
                 f.pack();
                 f.setVisible(true);
             }
         });
     }
 }
