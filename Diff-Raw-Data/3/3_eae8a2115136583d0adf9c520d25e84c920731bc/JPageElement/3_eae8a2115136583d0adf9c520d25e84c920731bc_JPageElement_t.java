 package edu.wustl.cab2b.client.ui.pagination;
 
 /*
  * > I have a JFrame Window and a JLabel on it with a long text. The problem
  > is that the text of the JLabel does not fit the size of the window -
  > the value for the width of the window is to less. But I want not to
  > make the window wider. How can I make it that the text of the JLabel is
  > automatically divided when the end of the window width is reached and
  > the remaining text will be placed in the next line?
 
  That will not be easy with a JLabel. You an easily create a JLabel that
  spans multiple lines (using HTML tags), but getting it to wrap is
  something else.
 
  Consider using one of the multi-line JTextComponent subclasses. You can
  change the opacity, colors, font, and other properties to make it look the
  same as the the JLabel.
  */
 
 /*
  * JLabel for output
  Why using JLabel for output is usually bad
 
  It's possible to change the text of a JLabel, although this is not generally a good idea after
  the user interface is already displayed. For output JTextField is often a better choice.
  The use of JLabel for output is mentioned because some textbooks display output this way.
  Here are some reasons not to use it.
 
  * Can't copy to clipboard. The user can not copy text from a JLabel, but can from a JTextField.
  * Can't set background. Changing the background of individual components probably isn't a good
  idea, so this restriction on JLabels is not serious. You can change the background of a
  JTextField, for better or worse.
  * Text length. This is where there are some serious issues. You can always see the entire text
  in a JTextField, altho you might have to scroll it it's long. There are several
  possibilities with a JLabel. You may either not see all of the long text in a JLabel,
  or putting long text into a JLabel may cause the layout to be recomputed, resulting
  in a truly weird user experience.
 
  */
 
 import java.awt.Dimension;
 import java.awt.FontMetrics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.Vector;
 
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 
 import edu.wustl.cab2b.client.ui.RiverLayout;
 import edu.wustl.cab2b.client.ui.controls.Cab2bCheckBox;
 import edu.wustl.cab2b.client.ui.controls.Cab2bHyperlink;
 import edu.wustl.cab2b.client.ui.controls.Cab2bLabel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bPanel;
 import edu.wustl.common.util.logger.Logger;
 
 /**
  * Each JPageElement should know its pageIndex(String) and index in that page.
  * This two index is needed for Creating an PageSelectionEvent and throwing it to JPagination.
  *
  *  JPagination can then maintain its SelectionModel properly.
  *
  * @author chetan_bh
  */
 public class JPageElement extends Cab2bPanel implements ActionListener, PropertyChangeListener {
 
     private static final long serialVersionUID = 1L;
 
     /**
      * An <code>Cab2bHyperlink</code> for this composite component. 
      */
     private Cab2bHyperlink hyperlink;
 
     /**
      * A <code>JCheckBox</code> for enabling selections on the page elements.
      * displayed in a page panel. 
      */
     private JCheckBox checkBox;
 
     /**
      * A <code>JLabel</code> to display the description associated with each page elements.
      */
     private Cab2bLabel descriptionLabel;
 
     private PageElement pageElement;
 
     /**
      * A boolean to indicate whether this component is selectable or not.
      */
     private boolean isSelectable;
 
     private PageElementIndex elementIndex;
 
     /**
      * Pagination reference.
      */
     private JPagination pagination;
 
     //	private final int MAXCHARSONLINE = (int)(Toolkit.getDefaultToolkit().getScreenSize().width * 0.50d);
 
     public JPageElement(
             JPagination pagination,
             PageElement pageElement,
             boolean isSelectable,
             PageElementIndex pageElementIndex) {
         this.pagination = pagination;
         this.pageElement = pageElement;
         this.isSelectable = isSelectable;
         this.elementIndex = pageElementIndex;
         initGUI();
         initListeners();
         this.setPreferredSize(new Dimension(976, ((int) (this.getPreferredSize().getHeight() + 10))));
     }
 
     private void initListeners() {
         if (pagination != null)
             pagination.addPropertyChangeListener("isSelectable", this);
     }
 
     /**
      * Initialize GUI for page element.
      */
     private void initGUI() {
         this.removeAll();
 
         this.setLayout(new RiverLayout(0, 0));
 
         hyperlink = new Cab2bHyperlink();
         hyperlink.setText(pageElement.getDisplayName());
         Logger.out.debug("Entity : " + pageElement.getDisplayName());
         hyperlink.setUserObject(pageElement);
         descriptionLabel = new Cab2bLabel(pageElement.getDescription());
         FontMetrics fontMetrics = descriptionLabel.getFontMetrics(descriptionLabel.getFont());
         int stringWidth = fontMetrics.stringWidth(pageElement.getDescription());
         descriptionLabel.setToolTipText(getWrappedText(stringWidth, pageElement.getDescription()));
 
         if (isSelectable && pagination != null) {
             checkBox = new Cab2bCheckBox();
             checkBox.setOpaque(false);
             checkBox.addActionListener(this);
             boolean isSelected = pageElement.isSelected();
             if (isSelected)
                 checkBox.setSelected(true);
             this.add("br", checkBox);
         } else {
             this.add("br", new JLabel("     "));
         }
         this.add("tab", hyperlink);
         this.add("br", new JLabel("     "));
         this.add("tab", descriptionLabel);
 
     }
 
     /**
      * Method to wrap the text and send it accross
      * @return
      */
     private String getWrappedText(int textSizeInPixel, String text) {
         StringBuffer sb = new StringBuffer();
         sb.append("<HTML>");
         int textLength = text.length();
         //Logger.out.debug (textLength);
         int currentStart = 0;
         String currentString = null;
         int offset = 75;
         int strLen = 0;
         int len = 0;
         while (currentStart < textLength && textLength > offset) {
             currentString = text.substring(currentStart, (currentStart + offset));
             strLen = strLen + currentString.length() + len;
             sb.append(currentString);
             int index = text.indexOf(" ", (currentStart + offset));
             if (index != -1) {
                 len = index - strLen;
                 currentString = text.substring((currentStart + offset), (currentStart + offset + len));
                 sb.append(currentString);
                 sb.append("<P>");
             } else {
            	if(currentStart==0){
            		currentStart=offset;
            	}
                 sb.append(text.substring(currentStart));
                 return sb.toString();
             }
 
             currentStart = currentStart + offset + len;
             if ((currentStart + offset + len) > textLength)
                 break;
         }
         sb.append(text.substring(currentStart));
         sb.append("</HTML>");
         return sb.toString();
     }
 
     /**
      * Returns true if this is selected, else false.
      * @return true if selected
      */
     public boolean isSelected() {
         return checkBox.isSelected();
     }
 
     /**
      * Capture events happening on JPageElements CheckBox 
      * Create a PageSelectionEvent and fire an selectionChangedEvent to JPagination.
      */
     public void actionPerformed(ActionEvent e) {
 
         if (pageElement.isSelected())
             pageElement.setSelected(false);
         else
             pageElement.setSelected(true);
 
         PageSelectionEvent selectionEvent = new PageSelectionEvent(this, elementIndex);
 
         pagination.fireSelectionValueChanged(selectionEvent);
     }
 
     public void addHyperlinkActionListener(ActionListener ae) {
         hyperlink.addActionListener(ae);
     }
 
     public void addHyperlinkActionListeners(Vector<ActionListener> aes) {
         for (ActionListener ae : aes) {
             hyperlink.addActionListener(ae);
         }
     }
 
     public void removeHyperlinkActionListener(ActionListener ae) {
         hyperlink.removeActionListener(ae);
     }
 
     /**
      * Property Change Listener, listens for specific changes in JPagination property change like "isSelectable" property.
      */
     public void propertyChange(PropertyChangeEvent propChangeEvent) {
         String propertyName = propChangeEvent.getPropertyName();
         Object newValue = propChangeEvent.getNewValue();
         if (propertyName.equals("isSelectable")) {
             Boolean newBoolValue = (Boolean) newValue;
             if (newBoolValue) {
                 isSelectable = newBoolValue;
                 initGUI();
                 this.revalidate();
             } else {
                 isSelectable = newBoolValue;
                 this.remove(checkBox);
                 this.revalidate();
             }
         }
     }
 }
