 package uk.ac.aber.dcs.cs12420.aberpizza.gui;
 
 import java.awt.BorderLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 
 import uk.ac.aber.dcs.cs12420.aberpizza.data.Discount;
 import uk.ac.aber.dcs.cs12420.aberpizza.data.DiscountSetPercent;
 import uk.ac.aber.dcs.cs12420.aberpizza.data.DiscountSetValue;
 import uk.ac.aber.dcs.cs12420.aberpizza.data.Item;
 import uk.ac.aber.dcs.cs12420.aberpizza.data.ItemPizza;
 import uk.ac.aber.dcs.cs12420.aberpizza.data.OrderItem;
 import uk.ac.aber.dcs.cs12420.aberpizza.data.PizzaSizeEnum;
 import uk.ac.aber.dcs.cs12420.aberpizza.data.Till;
 /**
  * Edits the specifications of a discount, items and quantities
  * @author tim
  *
  */
 public class DiscountValueEditor extends JFrame implements ActionListener{
 	private Discount discount;
 	private DiscountEditor discountEditor;
 	private boolean isEditing;
 	private JList itemList;
 	private Till till;
 	private ArrayList<OrderItem> disItems = new ArrayList<OrderItem>();
 	private JTextField descriptionField, priceField; 
 	private static final long serialVersionUID = -722669284621653074L;
 	public DiscountValueEditor(Discount i, DiscountEditor ie, boolean editing, Till t){
 		discount = i;
 		till = t;
 		discountEditor = ie;
 		isEditing = editing;
 		initialPropagation();
 		JPanel mainPanel = new JPanel(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		c.insets = new Insets(2,2,2,2);
 		c.fill = GridBagConstraints.BOTH;
 		c.weightx = 1;
 		c.weighty = 0;
 		///////////////
 		c.gridx = 0;
 		c.gridy = 0;
 		JLabel l1 = new JLabel("Discount Type:");
 		mainPanel.add(l1,c);
 		c.gridx = 1;
 		c.gridwidth = 3;
 		String discountType = "Percent";
 		if(discount instanceof DiscountSetPercent){
 			discountType = "Percent";
 		}else if(discount instanceof DiscountSetValue){
 			discountType = "Set Value";
 		}
 		JLabel l2 = new JLabel(discountType);
 		mainPanel.add(l2,c);
 		////////////////
 		c.gridy = 1;
 		c.gridx = 0;
 		c.gridwidth = 1;
 		JLabel l3 = new JLabel("Description");
 		mainPanel.add(l3,c);
 		c.gridx = 1;
 		c.gridwidth = 3;
 		String descriptionInfo = "";
 		if(discount.getDescription()!=null&&discount.getDescription().equals("")==false){
 			descriptionInfo = discount.getDescription();
 		}
 		descriptionField = new JTextField(descriptionInfo);
 		mainPanel.add(descriptionField,c);
 		//////////////
 		c.gridy = 2;
 		c.gridx = 0;
 		c.gridwidth = 1;
 		JLabel l4 = new JLabel("Price");
 		mainPanel.add(l4,c);
 		c.gridx = 1;
 		c.gridwidth = 3;
 		String priceInfo = "";
 		if(discount.getDiscount()!=null){
 			priceInfo = discount.getDiscount().toPlainString();
 		}
 		priceField = new JTextField(priceInfo);
 		mainPanel.add(priceField,c);
 		/////////////
 		c.gridwidth = 1;
 		c.gridy = 4;
 		c.gridx = 0;
 		JButton done = new JButton("Done"), cancel = new JButton("Cancel");
 		done.addActionListener(this);
 		cancel.addActionListener(this);
 		mainPanel.add(cancel,c);
 		c.gridx = 3;
 		mainPanel.add(done,c);
 		JButton setQuantity = new JButton("Set Item Quantity");
 		setQuantity.addActionListener(this);
 		c.gridx = 1;
 		mainPanel.add(setQuantity,c);
 		c.gridx = 2;
 		JButton setPizzaSize = new JButton("Set Pizza Size");
 		setPizzaSize.addActionListener(this);
 		mainPanel.add(setPizzaSize,c);
 		////////////
 		c.gridx = 0;
 		c.gridy = 3;
 		c.gridwidth = 4;
 		c.weightx = 1;
 		c.weighty = 1;
 		c.fill = GridBagConstraints.BOTH;
 		itemList = new JList();
 		JScrollPane pane = new JScrollPane(itemList);
 		mainPanel.add(pane,c);
 		//////////////
 		this.add(mainPanel,BorderLayout.CENTER);
		this.setAlwaysOnTop(true);
 		this.setSize(600,300);
 		this.setLocationRelativeTo(discountEditor);
 		updateItemList();
 	}
 	public void updateItemList(){
 		itemList.setListData(orderItemToStringArray(disItems));
 	}
 	public String[] orderItemToStringArray(ArrayList<OrderItem> e){
 		String[] rVal = {""};
 		if(e.size()>0){
 			rVal = new String[e.size()];
 			for(int i=0; i<e.size(); i++){
 				OrderItem o = e.get(i);
 				if(o.getItem() instanceof ItemPizza){
 					ItemPizza p = (ItemPizza)o.getItem();
 					rVal[i] = p.getDescription()+" - "+o.getQuantity()+" - "+p.getSize();
 				}else{
 					rVal[i] = o.getItem().getDescription()+" - "+o.getQuantity();
 				}
 			}
 		}
 		return rVal;
 	}
 	/**
 	 * Adds all items to the visible list
 	 */
 	public void initialPropagation(){
 		for(OrderItem i: discount.getItems()){
 			disItems.add(i);
 		}
 		for(Item s: till.getItems()){
 			OrderItem o = new OrderItem(s, 0);
 			if(discount.findItem(o.getItem().getDescription())==null){
 				disItems.add(o);
 			}
 		}
 	}
 	public OrderItem getSelectedItem(){
 		String s = (String)itemList.getSelectedValue();
 		try{
 			String[] d = s.split("-");
 			if(d[0].length()>0){
 				for(OrderItem i: disItems){
 					if(i.getItem().getDescription().equals(d[0].trim())){
 						return i;
 					}
 				}
 			}
 		}catch(IndexOutOfBoundsException e){
 			System.err.println("Error selecting item");
 			return null;
 		}
 		return null;
 	}
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		String aC = arg0.getActionCommand();
 		if("Done".equals(aC)){
 			if(priceField.getText().equals("")){
 				JOptionPane.showMessageDialog(this, "No Price Set", "Error", JOptionPane.WARNING_MESSAGE);
 				return;
 			}else{
 				try{
 					new BigDecimal(priceField.getText());
 				}catch(NumberFormatException e){
 					JOptionPane.showMessageDialog(this, "Invalid number", "Error", JOptionPane.WARNING_MESSAGE);
 					return;
 				}
 			}
 			BigDecimal price = new BigDecimal(priceField.getText());
 			
 			if((price.compareTo(BigDecimal.ZERO)>0&&price.compareTo(new BigDecimal(100))<0)||discount instanceof DiscountSetValue){
 				discount.setDiscount(new BigDecimal(priceField.getText()));
 			}else{
 				JOptionPane.showMessageDialog(this, "Percent disocuint must be between 0 and 100", "Error", JOptionPane.WARNING_MESSAGE);
 				return;
 			}
 			if(descriptionField.getText().equals("")){
 				JOptionPane.showMessageDialog(this, "Description must not be empty", "Error", JOptionPane.WARNING_MESSAGE);
 				return;
 			}
 			discount.setDescription(descriptionField.getText());
 			ArrayList<OrderItem> newList = new ArrayList<OrderItem>();
 			for(OrderItem i: disItems){
 				if(i.getQuantity()>0){
 					newList.add(i);
 				}
 			}
 			if(newList.size()<1){
 				JOptionPane.showMessageDialog(this, "Must be at least one item with quantity 1+", "Error", JOptionPane.WARNING_MESSAGE);
 				return;
 			}
 			discount.setItems(newList);
 			if(!isEditing&&!discountEditor.addDiscount(discount)){
 				JOptionPane.showMessageDialog(this, "Discount exists", "Error", JOptionPane.WARNING_MESSAGE);
 			}else{
 				//discount.
 				this.dispose();
 			}
 		}else if("Cancel".equals(aC)){
 			this.dispose();
 		}else if("Set Item Quantity".equals(aC)){
 			String message = "Enter Item Quantity";
 			String rVal;
 			boolean worked = false;
 			do{
 				rVal = (String)JOptionPane.showInputDialog(this,
 						message,message,
 						JOptionPane.QUESTION_MESSAGE);
 				try{
 					Integer.parseInt(rVal);
 					worked = true;
 				}catch(NumberFormatException e){
 					worked = false;
 				}
 			}while(rVal==null||rVal.length()<0||!worked);
 			OrderItem oi = getSelectedItem();
 			if(oi==null){ return; }
 			int l = Integer.parseInt(rVal);
 			oi.setQuantity(l);
 			updateItemList();
 		}else if("Set Pizza Size".equals(aC)){
 			if(getSelectedItem().getItem() instanceof ItemPizza){
 				Object[] options = {"SMALL","MEDIUM","LARGE"};
 				String rVal = (String)JOptionPane.showInputDialog(this,
 						"Choose a size", "Choose a size",
 						JOptionPane.QUESTION_MESSAGE, null,
 						options, options[0]);
 				if(rVal!=null&&rVal.length()>0){
 					ItemPizza p = (ItemPizza)getSelectedItem().getItem();
 					p.setSize(PizzaSizeEnum.valueOf(rVal));
 					updateItemList();
 				}
 			}
 		}
 	}
 	
 }
