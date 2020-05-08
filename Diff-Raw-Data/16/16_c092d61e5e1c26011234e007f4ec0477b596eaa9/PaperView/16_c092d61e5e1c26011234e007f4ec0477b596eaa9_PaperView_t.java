 package us.percept.pile.view;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import us.percept.pile.model.Paper;
 
 import javax.swing.*;
 import javax.swing.border.Border;
 import java.awt.*;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * Author: spartango
  * Date: 8/5/13
  * Time: 2:56 AM.
  */
 public class PaperView extends JPanel {
     private static final Logger logger = LoggerFactory.getLogger(PaperView.class);
 
     static {
         GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
         try {
             InputStream streamL = PaperView.class.getResourceAsStream("/fonts/Roboto-Regular.ttf");
             Font robotoRegular = Font.createFont(Font.TRUETYPE_FONT,
                                                  streamL);
             streamL.close();
            InputStream streamR = PaperView.class.getResourceAsStream("/fonts/Roboto-Light.ttf");
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
 
     private JLabel     titleField;
     private JLabel     authorsField;
     private JTextArea  summaryArea;
     private JLabel     dateField;
     private JLabel     statusLabel;
     private JSeparator separator;
 
     private Paper paper;
     private List<PaperViewListener> listeners = new ArrayList<>(1);
     private String actionString;
 
     public PaperView() {
         this("Archive");
     }
 
     public PaperView(String actionString) {
         this.actionString = actionString;
         setup();
     }
 
     public void addListener(PaperViewListener listener) {
         listeners.add(listener);
     }
 
     public void addListeners(Collection<? extends PaperViewListener> l) {
         listeners.addAll(l);
     }
 
     public void removeListener(PaperViewListener listener) {
         listeners.remove(listener);
     }
 
     public void notifyPaperArchived(Paper paper) {
         for (PaperViewListener listener : listeners) {
             listener.onPaperArchived(paper);
         }
     }
 
     public void notifyPaperOpened(Paper paper) {
         for (PaperViewListener listener : listeners) {
             listener.onPaperOpened(paper);
         }
     }
 
     public void setPaper(Paper p) {
         this.paper = p;
         titleField.setText("<html>" + paper.getTitle() + "</html>");
 
         StringBuilder authorString = new StringBuilder();
         java.util.List<String> authors = paper.getAuthors();
 
         authorString.append("<html>");
         // Get the last author and guard against empty authors
         String lastAuthor = authors.size() > 0 ? authors.get(authors.size() - 1) : authors.get(0);
         for (String author : authors) {
             authorString.append(author);
             if (author != lastAuthor) {
                 authorString.append(", ");
             }
         }
         authorString.append("</html>");
 
         authorsField.setText(authorString.toString());
         summaryArea.setText(paper.getSummary());
         summaryArea.setEditable(false);
 
         DateFormat df = new SimpleDateFormat("MMM yyy");
         String date = df.format(paper.getDate());
         dateField.setText(date + "  ");
         statusLabel.setToolTipText(paper.getFileLocation());
     }
 
     private void setup() {
         this.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));
         this.setBackground(Color.white);
 
         // Border is thin on top and thicker/darker on the bottom
         Border topBorder = BorderFactory.createMatteBorder(0, 2, 0, 2, new Color(228, 228, 228));
         Border bottomBorder = BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(208, 208, 208));
         this.setBorder(BorderFactory.createCompoundBorder(topBorder, bottomBorder));
 
         titleField = new JLabel();
         titleField.setFont(new Font("Roboto Light", Font.PLAIN, 23));
         titleField.setText("Title");
         titleField.setVerticalAlignment(0);
         this.add(titleField,
                  new com.intellij.uiDesigner.core.GridConstraints(0,
                                                                   0,
                                                                   1,
                                                                   1,
                                                                   com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST,
                                                                   com.intellij.uiDesigner.core.GridConstraints.FILL_NONE,
                                                                   com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW,
                                                                   com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
                                                                   null,
                                                                   new Dimension(500, 22),
                                                                   null,
                                                                   1,
                                                                   false));
         authorsField = new JLabel();
         authorsField.setText("Authors");
         authorsField.setFont(new Font("Roboto Light", Font.PLAIN, 17));
         authorsField.setEnabled(false);
         this.add(authorsField,
                  new com.intellij.uiDesigner.core.GridConstraints(1,
                                                                   0,
                                                                   1,
                                                                   1,
                                                                   com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST,
                                                                   com.intellij.uiDesigner.core.GridConstraints.FILL_NONE,
                                                                   com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW,
                                                                   1,
                                                                   null,
                                                                   new Dimension(500, 16),
                                                                   null,
                                                                   1,
                                                                   false));
         summaryArea = new JTextArea();
         summaryArea.setEditable(false);
         summaryArea.setLineWrap(true);
         summaryArea.setText("This is a summary of the article presented here.");
         summaryArea.setFont(new Font("Roboto Regular", Font.PLAIN, 13));
         this.add(summaryArea,
                  new com.intellij.uiDesigner.core.GridConstraints(3,
                                                                   0,
                                                                   1,
                                                                   2,
                                                                   com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER,
                                                                   com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH,
                                                                   com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW,
                                                                   com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   1,
                                                                   false));
 
         dateField = new JLabel();
         dateField.setEnabled(false);
         dateField.setFont(new Font("Roboto Regular", Font.PLAIN, 15));
         dateField.setText("Jan 2013");
         this.add(dateField,
                  new com.intellij.uiDesigner.core.GridConstraints(0,
                                                                   1,
                                                                   1,
                                                                   1,
                                                                   com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST,
                                                                   com.intellij.uiDesigner.core.GridConstraints.FILL_NONE,
                                                                   1,
                                                                   com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   1,
                                                                   false));
         statusLabel = new JLabel();
         // A peculiar shade of green
         statusLabel.setForeground(new Color(75, 168, 79));
         statusLabel.setText(actionString + "  ");
         statusLabel.setFont(new Font("Roboto Regular", Font.PLAIN, 15));
         this.add(statusLabel,
                  new com.intellij.uiDesigner.core.GridConstraints(1,
                                                                   1,
                                                                   1,
                                                                   1,
                                                                   com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST,
                                                                   com.intellij.uiDesigner.core.GridConstraints.FILL_NONE,
                                                                   com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
                                                                   com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   1,
                                                                   false));
         statusLabel.addMouseListener(new MouseAdapter() {
             @Override public void mouseClicked(MouseEvent e) {
                 notifyPaperArchived(paper);
             }
 
             @Override public void mouseEntered(MouseEvent e) {
                 statusLabel.setForeground(new Color(247, 178, 61));
 
             }
 
             @Override public void mouseExited(MouseEvent e) {
                 statusLabel.setForeground(new Color(75, 168, 79));
             }
         });
 
         separator = new JSeparator();
         this.add(separator,
                  new com.intellij.uiDesigner.core.GridConstraints(2,
                                                                   0,
                                                                   1,
                                                                   2,
                                                                   com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTH,
                                                                   com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL,
                                                                   com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW,
                                                                   com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   0,
                                                                   false));
 
 
         this.addMouseListener(new MouseAdapter() {
             @Override public void mouseClicked(MouseEvent e) {
                 notifyPaperOpened(paper);
             }
 
             @Override public void mouseEntered(MouseEvent e) {
                 Color highlight = new Color(245, 245, 245);
                 setBackground(highlight);
                 summaryArea.setBackground(highlight);
             }
 
             @Override public void mouseExited(MouseEvent e) {
                 setBackground(Color.white);
                 summaryArea.setBackground(Color.white);
             }
         });
     }
 
 
 }
