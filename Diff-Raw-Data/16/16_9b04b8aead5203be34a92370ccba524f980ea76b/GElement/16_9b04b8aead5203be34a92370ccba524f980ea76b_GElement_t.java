 package gui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 
 import javax.swing.BorderFactory;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.border.LineBorder;
 
 import parser.LayoutParserTreeElement;
 import parser.TextActions;
 import tree.GenericTreeNode;
 import element_actions.BlockMouseListener;
 
 /**
  * Element panel.
  * 
  * @author Unknown-Revengers
  */
 @SuppressWarnings("serial")
 public class GElement extends JPanel {
 
 	private JScrollPane scrollPanel;
 
 	private JTextArea textArea;
 
 	public GenericTreeNode<LayoutParserTreeElement> element;
 	private GPopup popup;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param element Tree element.
 	 */
 	public GElement(GenericTreeNode<LayoutParserTreeElement> element) {
 		this.element = element;
 
 		// Make it transparent.
 		this.setOpaque(false);
 
 		// No layout.
 		this.setLayout(null);
 
 		// Set initial border.
 		this.setBorder(new LineBorder(Color.GREEN));
 	}
 
 	/**
 	 * Setter for popup.
 	 * 
	 * @param popup GPopup
 	 * 
 	 * @return void
 	 */
 	public void setPopup(GPopup popup) {
 		// Set local popup.
 		this.popup = popup;
 
 		// Set mouse listener for block change.
 		this.addMouseListener(new BlockMouseListener(popup));
 	}
 
 	/**
 	 * Getter for popup.
 	 * 
 	 * @return GPopup
 	 */
 	public GPopup getPopup() {
 		return this.popup;
 	}
 
 	/**
 	 * Seteaza un TextArea pentru elementul curent
 	 * 
 	 * @param height Text area height.
 	 * @param width Text area width.
 	 * @param visible Text area is visible or not.
 	 */
 	public void setTextArea(int width, int height, boolean visible) {
 		textArea = new JTextArea();
 		textArea.setBorder(BorderFactory.createLineBorder(Color.red));
		textArea.setLineWrap(false);
		textArea.setWrapStyleWord(false);
 
 		textArea.setText(TextActions.getText(this.element));
 		textArea.setVisible(visible);
 		textArea.setOpaque(true);
 		textArea.setEditable(false);
 
 		this.scrollPanel = new JScrollPane(textArea);
 		this.scrollPanel.setPreferredSize(new Dimension(width, height));
 		this.scrollPanel.setBounds(0, 0, width, height);
 		this.scrollPanel.setVisible(visible);
 
 		this.add(scrollPanel);
 	}
 
 	/**
 	 * Modifica vizibilitatea si existenta TextArea-ului asociat GElement-ului
 	 * 
 	 * @param visible Set text are visible or not.
 	 */
 	public void setTextAreaVisible(boolean visible) {
 		this.textArea.setText(TextActions.getText(this.element));
 		this.scrollPanel.setVisible(visible);
 		this.textArea.setVisible(visible);
 	}
 }
