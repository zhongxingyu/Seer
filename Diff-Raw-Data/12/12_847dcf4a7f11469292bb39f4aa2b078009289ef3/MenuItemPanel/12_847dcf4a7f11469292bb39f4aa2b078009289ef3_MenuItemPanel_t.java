 package delicious.pos.ui.components.menu;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import javax.swing.BoxLayout;
 import javax.swing.JLabel;
 import javax.swing.SwingConstants;
 
 import delicious.pos.App;
 import delicious.pos.business.logic.dao.PriceDAO;
 import delicious.pos.business.logic.view.PriceView;
 import delicious.pos.business.logic.view.gen.ItemView;
 import delicious.pos.ui.components.extensions.UILabel;
 import delicious.pos.ui.components.extensions.UIPanel;
 import delicious.pos.ui.components.order.OrderPanel;
 import delicious.pos.ui.screens.MenuScreen;
 
 
 public class MenuItemPanel extends UIPanel {
 	private static final long serialVersionUID = 1L;
 	private ItemPricesPanel itemPricesPanel = null;
 	private MenuScreen menuScreen;
 	private OrderPanel orderPanel;
 	private ItemView item;
 	private PriceView price;
 	private PriceDAO priceDAO;
 	
 	public MenuItemPanel(ItemView item, MenuScreen parentPanel) {
 		this.menuScreen = parentPanel;
 		this.item = item;
 		this.init();
 	}
 	
 	public MenuItemPanel(ItemView item, OrderPanel orderPanel) {
 		this.orderPanel = orderPanel;
 		this.item = item;
 		this.init();
 	}
 
 	public void init() {
 		this.setPreferredSize(new Dimension(400, 50));
 		this.setMinimumSize(this.getPreferredSize());
 		this.setMaximumSize(this.getPreferredSize());
 		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
 
 		this.priceDAO = new PriceDAO(App.DBConnection, App.JDBCUtilities.dbName, App.JDBCUtilities.dbms);
 
 		JLabel lblItemName = new UILabel(this.item.getName());
 		lblItemName.setVerticalAlignment(SwingConstants.CENTER);
 		add(lblItemName);
 		
 		ArrayList<PriceView> itemPrices = null;
 		
 		itemPrices = this.priceDAO.findByItemName(this.item.getName());
 
 		itemPricesPanel = new ItemPricesPanel(itemPrices, this);
 		add(itemPricesPanel);	
 		
 		itemPricesPanel.getRemmoveBtn().addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				removeOrderedItem();
 			}
 		});
 	}
 	
 	public void setOrdered(boolean isOrdered, PriceView price) {
 		this.price = price;
 		
 		itemPricesPanel.setOrdered(isOrdered, price);
 	}
 	
 	public void removeOrderedItem() {
 		this.orderPanel.removeOrderedItem(this, this.item, this.price);		
 	}
 
 	public void orderItem(ItemPricesPanel price, ItemPricePanel itemPricePanel) {
 		if(menuScreen != null) {
 			itemPricesPanel.deselectPrices();
 			this.menuScreen.orderItem(this.item, itemPricePanel.getPrice());
 		}
 	}
 }
