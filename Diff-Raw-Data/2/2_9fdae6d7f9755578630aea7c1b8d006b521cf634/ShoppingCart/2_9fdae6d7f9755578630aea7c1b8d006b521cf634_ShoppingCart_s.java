 package cards;
 
 import grp30.MainFrame;
 import gui.IMatColors;
 
 import java.awt.CardLayout;
 import java.awt.Color;
 
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.JList;
 import javax.swing.JPanel;
 
 import se.chalmers.ait.dat215.project.IMatDataHandler;
 import se.chalmers.ait.dat215.project.ShoppingCartListener;
 import javax.swing.JButton;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.JLabel;
 import SpecialPanels.ShoppingCartScrollPane;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import javax.swing.JDialog;
 
 public class ShoppingCart extends JPanel implements ShoppingCartListener {
 	JLabel lblNumpr;
 	JLabel lblSum;
 	JLabel lblEmptyCart;
 	private ShoppingCartScrollPane shoppingCartScrollPane;
 
 	public ShoppingCart(final MainFrame mf){
 		IMatDataHandler.getInstance().getShoppingCart().addShoppingCartListener(this);
 		setBackground(IMatColors.BASE_LIGHT);
 		
 		JButton btnTillKassan = new JButton("Till Kassan");
 		btnTillKassan.setToolTipText("Checka ut och best\u00E4ll dina varor");
 		btnTillKassan.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if(IMatDataHandler.getInstance().getShoppingCart().getItems().size() == 0){
 					lblEmptyCart.setVisible(true);
 				}
 				else{
 					
 					mf.swapCard("pay1");
 					
 				}
 			}
 		});
 		
 		JPanel panel = new JPanel();
 		panel.setBackground(IMatColors.BASE_LIGHT);
 		
 		shoppingCartScrollPane = new ShoppingCartScrollPane();
 		
 		
 		//TODO NDRA TILL JDIALOG 
 		lblEmptyCart = new JLabel("Varukorgen \u00E4r tom");
 		
 		GroupLayout groupLayout = new GroupLayout(this);
 		groupLayout.setHorizontalGroup(
 			groupLayout.createParallelGroup(Alignment.TRAILING)
 				.addGroup(groupLayout.createSequentialGroup()
 					.addContainerGap()
 					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 						.addComponent(shoppingCartScrollPane, GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
 						.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
 							.addComponent(lblEmptyCart)
 							.addGap(18)
 							.addComponent(btnTillKassan))
 						.addComponent(panel, GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE))
 					.addContainerGap())
 		);
 		groupLayout.setVerticalGroup(
 			groupLayout.createParallelGroup(Alignment.TRAILING)
 				.addGroup(groupLayout.createSequentialGroup()
 					.addContainerGap()
 					.addComponent(shoppingCartScrollPane, GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 						.addComponent(btnTillKassan)
 						.addComponent(lblEmptyCart))
 					.addContainerGap())
 		);
 		lblEmptyCart.setVisible(false);
 		
 		JLabel lblAntalVaror = new JLabel("Antal Varor:");
 		
 		lblNumpr = new JLabel("0");
 		
 		JLabel lblPrisTot = new JLabel("Pris Tot:");
 		
 		lblSum = new JLabel("0");
 		GroupLayout gl_panel = new GroupLayout(panel);
 		gl_panel.setHorizontalGroup(
 			gl_panel.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel.createSequentialGroup()
 					.addContainerGap()
 					.addComponent(lblAntalVaror)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(lblNumpr)
 					.addPreferredGap(ComponentPlacement.RELATED, 251, Short.MAX_VALUE)
 					.addComponent(lblPrisTot)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(lblSum)
 					.addContainerGap())
 		);
 		gl_panel.setVerticalGroup(
 			gl_panel.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel.createSequentialGroup()
 					.addContainerGap()
 					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
 						.addComponent(lblAntalVaror)
 						.addComponent(lblNumpr)
 						.addComponent(lblSum)
 						.addComponent(lblPrisTot))
 					.addContainerGap(12, Short.MAX_VALUE))
 		);
 		panel.setLayout(gl_panel);
 		setLayout(groupLayout);
 
 	}
 
 	private void refreshList() {
 		lblNumpr.setText(IMatDataHandler.getInstance().getShoppingCart()
 				.getItems().size()
 				+ " st varor");
 		lblSum.setText(IMatDataHandler.getInstance().getShoppingCart()
 				.getTotal()
 				+ " kr");
 	}
 
 	@Override
 	public void shoppingCartChanged() {
 		refreshList();
 
 	}
 	
 	
 	//refresh for previous saved shopping cart
 	public void shoppingCartChanged2() {
 		refreshList();
 		shoppingCartScrollPane.shoppingCartChanged();
 
 	}
 }
