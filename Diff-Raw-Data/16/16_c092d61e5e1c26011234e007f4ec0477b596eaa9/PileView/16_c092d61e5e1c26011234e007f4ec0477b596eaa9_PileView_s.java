 package us.percept.pile.view;
 
 import com.intellij.uiDesigner.core.GridConstraints;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import us.percept.pile.model.Paper;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Vector;
 
 /**
  * Author: spartango
  * Date: 8/4/13
  * Time: 10:04 PM.
  */
 public class PileView extends JPanel implements ActionListener {
     private static final Logger logger = LoggerFactory.getLogger(PileView.class);
 
     static {
         GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
         try {
             InputStream streamL = PileView.class.getResourceAsStream("/fonts/Roboto-Regular.ttf");
             Font robotoRegular = Font.createFont(Font.TRUETYPE_FONT,
                                                  streamL);
             streamL.close();
            InputStream streamR = PileView.class.getResourceAsStream("/fonts/Roboto-Regular.ttf");
             Font robotoLight = Font.createFont(Font.TRUETYPE_FONT,
                                                streamR);
             streamR.close();
 
             ge.registerFont(robotoLight);
             ge.registerFont(robotoRegular);
             logger.info("Fonts added: " + robotoLight.getName() + " & " + robotoRegular.getName());
         } catch (FontFormatException | IOException e) {
             logger.warn("Failed to load Roboto font");
         }
     }
 
     private JTextField  searchField;
     private JScrollPane listScrollPane;
     private JPanel      paperList;
     private JLabel      searchLabel;
 
     private List<Paper>            papers    = new Vector<>();
     private List<PileViewListener> listeners = new ArrayList<>(1);
 
     private String actionString = "Archive";
 
     public PileView() {
         setup();
     }
 
     private void createUIComponents() {
         searchField = new JTextField();
         paperList = new JPanel(new GridBagLayout());
         listScrollPane = new JScrollPane();
     }
 
     public void addListener(PileViewListener listener) {
         listeners.add(listener);
     }
 
     public void removeListener(PileViewListener listener) {
         listeners.remove(listener);
     }
 
     public void addPaper(Paper paper) {
         papers.add(paper);
         renderPaper(paper);
     }
 
     private void renderPapers() {
         for(Paper paper : papers) {
             renderPaper(paper);
         }
     }
 
     private void renderPaper(Paper paper) {
         // Build a view for it
         PaperView view = new PaperView(actionString);
         view.setPaper(paper);
         view.addListeners(listeners);
 
         GridBagConstraints constraints = new GridBagConstraints();
         constraints.gridx = 0;
         constraints.gridy = paperList.getComponentCount();
 
         // UI components to be added
         paperList.add(view, constraints);
         addSpacer();
     }
 
 
     public void removePaper(Paper paper) {
         papers.remove(paper);
 
         // Remove everything from the view
         paperList.removeAll();
 
         // Readd everything
         renderPapers();
 
         listScrollPane.updateUI();
     }
 
     private Component addSpacer() {
         GridBagConstraints spacerConstraints = new GridBagConstraints();
         spacerConstraints.gridx = 0;
         spacerConstraints.gridy = paperList.getComponentCount();
 
         Component spacer = Box.createVerticalStrut(18);
         paperList.add(spacer, spacerConstraints);
 
         return spacer;
     }
 
     public void addPapers(Collection<Paper> papers) {
         for (Paper p : papers) {
             addPaper(p);
         }
     }
 
     public void clearPapers() {
         papers.clear();
         paperList.removeAll();
         addSpacer();
         listScrollPane.updateUI();
     }
 
     public void setSearchAction(String label) {
         searchLabel.setText(label + "  ");
     }
 
     public void notifySearchRequested(String query) {
         for (PileViewListener listener : listeners) {
             listener.onSearchRequested(query);
         }
     }
 
     @Override public void actionPerformed(ActionEvent e) {
         notifySearchRequested(e.getActionCommand());
     }
 
     public void setPaperAction(String actionString) {
         this.actionString = actionString;
     }
 
 
     private void setup() {
         createUIComponents();
 
         this.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
         this.setBackground(new Color(238, 238, 238));
         searchField.setFont(new Font("Roboto Regular", Font.PLAIN, 13));
         searchField.setHorizontalAlignment(JTextField.CENTER);
         searchField.addActionListener(this);
 
         this.add(searchField,
                  new com.intellij.uiDesigner.core.GridConstraints(0,
                                                                   0,
                                                                   1,
                                                                   1,
                                                                   com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST,
                                                                   com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL,
                                                                   com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW,
                                                                   com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
                                                                   null,
                                                                   new Dimension(150, -1),
                                                                   null,
                                                                   0,
                                                                   false));
 
         // Action button for search
         searchLabel = new JLabel();
         searchLabel.setText("Import  ");
         searchLabel.setForeground(new Color(56, 117, 206));
         searchLabel.setFont(new Font("Roboto Regular", Font.PLAIN, 15));
         this.add(searchLabel,
                  new GridConstraints(0,
                                      1,
                                      1,
                                      1,
                                      GridConstraints.ANCHOR_WEST,
                                      GridConstraints.FILL_NONE,
                                      GridConstraints.SIZEPOLICY_FIXED,
                                      GridConstraints.SIZEPOLICY_FIXED,
                                      null,
                                      null,
                                      null,
                                      0,
                                      false));
         searchLabel.addMouseListener(new MouseAdapter() {
             @Override public void mouseClicked(MouseEvent e) {
                 notifySearchRequested(searchField.getText());
             }
 
             @Override public void mouseEntered(MouseEvent e) {
                 searchLabel.setForeground(new Color(247, 178, 61));
 
             }
 
             @Override public void mouseExited(MouseEvent e) {
                 searchLabel.setForeground(new Color(56, 117, 206));
             }
         });
 
         // The list itself
         this.add(listScrollPane,
                  new com.intellij.uiDesigner.core.GridConstraints(1,
                                                                   0,
                                                                   1,
                                                                   2,
                                                                   com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER,
                                                                   com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH,
                                                                   com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                   | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW,
                                                                   com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                   | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   0,
                                                                   false));
         listScrollPane.setViewportView(paperList);
         listScrollPane.getVerticalScrollBar().setUnitIncrement(16);
         addSpacer();
     }
 
 
 }
