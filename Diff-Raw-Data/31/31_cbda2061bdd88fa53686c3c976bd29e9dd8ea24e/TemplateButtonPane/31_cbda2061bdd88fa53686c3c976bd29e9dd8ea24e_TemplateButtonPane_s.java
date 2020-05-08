 package nl.nikhef.jgridstart.gui.util;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.print.PrinterException;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Properties;
 import javax.swing.Action;
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import org.w3c.dom.Document;
 
 
 /** A template pane in a scrolledwindow with buttons below */
 public class TemplateButtonPane extends JPanel {
     
     /** the actual html template pane */
     protected TemplatePane contentpane = null;
     /** pane containing the buttons */
     protected JPanel buttonpane = null;
     /** empty space around buttons */
     protected final int btnBorderWidth = 2;
     
     public TemplateButtonPane() {
 	super();
 	// add TemplatePane in scrollview
 	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 	contentpane = new TemplatePane();
 	JScrollPane scroll = new JScrollPane();
     	scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
     	scroll.setViewportView(contentpane);
 	add(scroll, null);
 	// add button pane
 	buttonpane = new JPanel();
 	buttonpane.setLayout(new BoxLayout(buttonpane, BoxLayout.X_AXIS));
 	buttonpane.setBorder(BorderFactory.createEmptyBorder(
 		btnBorderWidth, btnBorderWidth, btnBorderWidth, btnBorderWidth));
 	add(buttonpane, null);
     }
     
     public TemplateButtonPane(URL src) throws IOException {
 	this();
 	setPage(src);
     }
     
     /** remove all buttons currently present */
     public void removeActions() {
 	buttonpane.removeAll();
     }
     /** adds an action to the button list
      *
      * @param action Action to add
      * @param isDefault true whether this is the default action (set on only one)
      */
     public void addAction(Action action, boolean isDefault) {
 	if (buttonpane.getComponentCount()==0)
 	    buttonpane.add(Box.createHorizontalGlue());
 	
 	buttonpane.add(new JButton(action), null);
 	buttonpane.add(Box.createRigidArea(new Dimension(btnBorderWidth, 0)));
 	buttonpane.revalidate();
 	if (isDefault)
 	    contentpane.setSubmitAction(action);
     }
     /** adds an non-default action to the button list
      * 
      * @param action Action to add
      */
     public void addAction(Action action) {
 	addAction(action, false);
     }
     
     // plain delegates TODO complete
     public String getTitle() {
 	return contentpane.getTitle();
     }
     public void refresh() {
 	contentpane.refresh();
     }
     public void setData(Properties p) {
 	contentpane.setData(p);
     }
     public Properties data() {
 	return contentpane.data();
     }
     public void setPage(URL src) throws IOException {
 	contentpane.setPage(src);
     }
     public URL getPage() {
 	return contentpane.getPage();
     }
     public void setBackground(Color c) {
 	super.setBackground(c);
 	if (contentpane!=null) contentpane.setBackground(c);
     }
     public Document getDocument() {
 	return contentpane.getDocument();
     }
     public boolean print() throws PrinterException {
 	return contentpane.print();
     }
 }
