 package ntnu.it1901.gruppe4.gui;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.Box;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 import ntnu.it1901.gruppe4.db.OrderItem;
 
 /**
  * A panel that shows information about an {@link OrderItem}.
  * 
  * @author Leo
  */
 public class OrderSummaryItem extends ClickablePanel {
 	private JLabel name, price, description;
	private JTextArea descriptionInput;
 	private JButton save, delete;
 	private OrderItem item;
 
 	/**
 	 * Creates a new {@link OrderSummaryItem} containing the specified {@link OrderItem}.
 	 *  
 	 * @param item The <code>OrderItem</code> to be associated with the new <code>OrderSummaryItem</code>.
 	 */
 	public OrderSummaryItem(OrderItem item, Mode mode) {
 		this.item = item;
 		
 		name = new JLabel(item.getName());
 		price = new JLabel(Layout.decimalFormat.format(item.getAmount()) + " kr");
 		description = new JLabel(item.getDescription());
		descriptionInput = new JTextArea(description.getText());
 		save = new JButton("Lagre");
 		delete = new JButton("Slett");
 		
 		setBorder(Layout.summaryItemPadding);
 		name.setFont(Layout.summaryItemFont);
 		price.setFont(Layout.summaryItemFont);
 		description.setFont(Layout.summaryItemDescriptionFont);
 		save.setFont(Layout.summaryItemDescriptionFont);
 		delete.setFont(Layout.summaryItemDescriptionFont);
		
		/*Sets the border of the text area to whatever is the default border
		  for text fields on the current OS*/
		descriptionInput.setBorder(new JTextField().getBorder());
 		descriptionInput.setFont(Layout.summaryItemDescriptionFont);
		descriptionInput.setLineWrap(true);
 		
 		setLayout(new GridBagLayout());
 		GridBagConstraints gbc = new GridBagConstraints();
 		gbc.weightx = 1;
 		gbc.weighty = 1;
 		gbc.anchor = GridBagConstraints.WEST;
 		gbc.gridy = 0;
 		gbc.gridx = 0;
 		gbc.gridwidth = 3;
 		add(name, gbc);
 		
 		gbc.anchor = GridBagConstraints.EAST;
 		add(price, gbc);
 
 		gbc.gridy++;
 		add(Box.createVerticalStrut(5), gbc);
 		
 		changeFunction(false);
 		
 		//To prevent this component's height from growing
 		setMaximumSize(new Dimension(Short.MAX_VALUE, getPreferredSize().height));
 		
 		if (mode == Mode.ORDER) {
 			save.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					OrderSummaryItem.this.item.setDescription(descriptionInput.getText());
 					changeFunction(false);
 				}
 			});
 		}
 	}
 	
 	/**
 	 * Changes the layout of the {@link OrderSummaryItem} to edit the description of the contained {@link OrderItem}.
 	 * 
 	 * @param editingComment True to show the text area and button to edit the <code>OrderItem's</code> description. False to hide them.
 	 */
 	public void changeFunction(boolean editingComment) {
 		remove(descriptionInput);
 		remove(save);
 		remove(delete);
 		remove(description);
 		
 		GridBagConstraints gbc = new GridBagConstraints();
 		gbc.weightx = 1;
 		gbc.weighty = 1;
 		gbc.fill = GridBagConstraints.HORIZONTAL;
 		gbc.anchor = GridBagConstraints.WEST;
 		gbc.gridy = 2;
 		gbc.gridx = 0;
 		
 		if (editingComment) {
 			descriptionInput.setText(item.getDescription());
 			
 			add(descriptionInput, gbc);
 			
 			gbc.gridx++;
 			add(delete, gbc);
 			
 			gbc.gridx++;
 			add(save, gbc);
 			descriptionInput.grabFocus();
 		}
 		else {
 			description.setText(item.getDescription());
 			
 			add(description, gbc);
 		}
 		setMaximumSize(new Dimension(Short.MAX_VALUE, getPreferredSize().height));
 		revalidate();
 		repaint();
 	}
 	
 	/**
 	 * Adds a listener that will be called when the delete button in this {@link OrderSummaryItem} is clicked.
 	 * 
 	 * @param listener The listener that will be called when the item is clicked.
 	 */
 	public void addDeleteButtonListener(ActionListener listener) {
 		delete.addActionListener(listener);
 	}
 }
