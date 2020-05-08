 package Integrated;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableModel;
 
 import Database.CustomerOrder;
 import aurelienribon.slidinglayout.SLAnimator;
 import aurelienribon.slidinglayout.SLConfig;
 import aurelienribon.slidinglayout.SLKeyframe;
 import aurelienribon.slidinglayout.SLPanel;
 import aurelienribon.slidinglayout.SLSide;
 
 class OrderPanel extends JPanel {
 	OrderSystem os;
 	
 	private HashMap<JButton, String> categoryList = new HashMap<JButton, String>();
 	private HashMap<JButton, String> menuList = new HashMap<JButton, String>();
 	
 	private final JSplitPane masterPanel = new JSplitPane();
 	private final SLPanel basePanel = new SLPanel();
 	private SLConfig payCfg, orderListCfg;
 	
 	//Menu Panels
 	private JPanel categoryPanel = new JPanel(), menuPanel = new JPanel();
 	private JScrollPane categoryScroll = new JScrollPane(categoryPanel,
 															JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
 															JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 	private JScrollPane menuScroll = new JScrollPane(menuPanel,
 															JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
 															JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 	
 	//OrderListPanel
 	private DefaultTableModel orderListTableModel = new DefaultTableModel();
 	private JTable orderListTable = new JTable(orderListTableModel);
 	private JScrollPane orderListTableScroll = new JScrollPane(orderListTable);
 	int currentNumber = 0; // ֹ ޴ 
 	private int orderTotal; //ֹ  հ
 	final int maximumNumber = 60; //    ֹ ޴ 
 	private String columnNames[] = { "޴", "", "ܰ", "޴ հ" };
 	/* data[][0]: ޴, data[][1]: ֹ, data[][2]: ܰ, data[][3]: ޴ֹհ */
 	private ArrayList<CustomerOrder> orderList = new ArrayList<CustomerOrder>();
 	public int latestOrderID;
 	
 	//Pay Panel
 	private PayPane payPane = new PayPane("â ", this);
 	private JButton payButton = new JButton("ϱ");
 	
 	//Mody Use Variable
 	private String modyName;
 	private boolean modyMode = false;
 	
 	
 
 	public OrderPanel(OrderSystem os, boolean modyMode) {//mody event
 		this(os);
 		this.modyMode = modyMode;
 	}
 
 	public OrderPanel(OrderSystem os) {
 		this.os = os;
 		this.orderTotal = 0;
 		this.latestOrderID = 0; // ֱ ŷ ȣ ʱȭ
 
 		//Menu Panels
 		categoryScroll.setBorder(null);
 		categoryPanel.setLayout(new ModifiedFlowLayout());
 		menuScroll.setBorder(null);
 		menuPanel.setLayout(new ModifiedFlowLayout());
 		try { //Loading Categories
 			java.sql.ResultSet rs = os.db
 					.exec("select distinct menu_category from herb_menu order by menu_category");
 			
 			categoryPanel.removeAll();
 			categoryList.clear();
 			ImageIcon imageIcon;
 			JButton categoryBtn;
 			while (rs.next()) {
 				imageIcon = new ImageIcon("image/order/" + rs.getString("menu_category").replace("/", "") + ".png");
 				categoryBtn = new JButton(imageIcon);
 				categoryBtn.setPreferredSize(new Dimension(imageIcon.getIconWidth(), imageIcon.getIconHeight()));
 				categoryBtn.addActionListener(new categoryListener());
 				categoryList.put(categoryBtn, rs.getString("menu_category"));
 				categoryPanel.add(categoryBtn);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		payButton.setPreferredSize(new Dimension(325, 80));
 		payButton.addActionListener(new ActionRunner(payAction));
 		
 		//OrderListPanel
 		orderListTableScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		orderListTableScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		updateOrderTable();
 				
 		//PayPanel
 		payPane.setPayBackAction(new ActionRunner(orderListAction));
 		
 		//Sliding Configs
 		payCfg = new SLConfig(basePanel).gap(0, 0).row(1f).col(1f).col(582)
 				.place(0, 0, orderListTableScroll).place(0, 1, payPane);
 		orderListCfg = new SLConfig(basePanel).gap(0, 0).row(1f).col(1f).col(2f)
 				.place(0, 0, menuScroll).place(0, 1, orderListTableScroll);
 
 		//basePanel
 		basePanel.setTweenManager(SLAnimator.createTweenManager());
 		basePanel.initialize(orderListCfg);
 		
 		//masterPanel
 		masterPanel.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
 		masterPanel.setDividerSize(1);
 		masterPanel.setDividerLocation(140);
 		masterPanel.setLeftComponent(categoryScroll);
 		masterPanel.setRightComponent(basePanel);
 		
 		this.setLayout(new BorderLayout());
 		this.add(masterPanel, BorderLayout.CENTER);
 	}
 
 	private final Runnable payAction = new Runnable() {
 		@Override
 		public void run() {
 			payPane.setTotal(orderTotal);
			payPane.refreshDate();
 			//õ ޴ Ʈ Nȭ
 			PythonSyncModule.recommandString = "õ ޴ ϱ⿡  ֹ մϴ.";
 			payPane.getRecommand(orderList);
 			basePanel
 					.createTransition()
 					.push(new SLKeyframe(payCfg, 0.6f)
 							.setEndSide(SLSide.LEFT, menuScroll)
 							.setStartSide(SLSide.RIGHT, payPane)
 							.setCallback(new SLKeyframe.Callback() {
 								@Override
 								public void done() {
 								}
 							})).play();
 		}
 	};
 
 	private final Runnable orderListAction = new Runnable() {
 		@Override
 		public void run() {
 			basePanel
 					.createTransition()
 					.push(new SLKeyframe(orderListCfg, 0.6f)
 							.setStartSide(SLSide.LEFT, menuScroll)
 							.setEndSide(SLSide.RIGHT, payPane)
 							.setCallback(new SLKeyframe.Callback() {
 								@Override
 								public void done() {
 								}
 							})).play();
 		}
 	};
 	
 	private class ActionRunner implements ActionListener {
 		Runnable action;
 		ActionRunner(Runnable action) { this.action = action; }
 		public void actionPerformed(ActionEvent e) {
 			if (action != null) action.run();
 		}
 	}
 	
 	private class categoryListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			JButton tmp1 = (JButton) e.getSource();
 			String name = categoryList.get(tmp1);
 			loadMenu(name);
 		}
 	}
 	
 	private class menuListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			JButton tmp1 = (JButton) e.getSource();
 			String name = menuList.get(tmp1);
 			if (modyMode) { modyName=name; return; } //In case of ModyMode
 			
 			int price = 0;
 			try {
 				java.sql.ResultSet rs = os.db
 						.exec("select * from herb_menu where menu_name = '"
 								+ name + "'");
 				if (rs.next()) {
 					price = Integer.parseInt(rs.getString("menu_price"));
 				}
 			} catch (SQLException sqle) {
 				sqle.printStackTrace();
 			}
 			
 			boolean isExist = false;
 			for(int i=0; i< orderList.size(); i++){
 				if(orderList.get(i).getMenuName().compareTo(name) == 0)
 				{
 					orderList.get(i).addMenuCount();
 					isExist = true;
 				}
 			}
 			
 			if( !isExist ){
 				orderList.add(new CustomerOrder(name, 1, price));
 			}
 			
 			orderTotal += price;	// ֹ հ迡  ֹ  Ѵ.
 			updateOrderTable();
 		}
 	}
 	
 	public void loadMenu(String category) {
 		try {
 			java.sql.ResultSet rs = os.db
 					.exec("select * from herb_menu where menu_category = '"	+ category + "'");
 			
 			menuPanel.removeAll();
 			menuList.clear();
 			if (!modyMode) menuPanel.add(payButton); //In case of Menu Modification Mode
 			//ImageIcon imageIcon;
 			JButton menuBtn;
 			while (rs.next()) {
 				//imageIcon = new ImageIcon("image/order/" + rs.getString("menu_name") + ".png");
 				menuBtn = new JButton(rs.getString("menu_name"));
 				menuBtn.setPreferredSize(new Dimension(160, 80));
 				menuBtn.addActionListener(new menuListener());
 				menuList.put(menuBtn, rs.getString("menu_name"));
 				menuPanel.add(menuBtn);
 			}
 			menuPanel.updateUI();
 			
 			ResultSet menuListQuery = os.db.exec("SELECT * FROM herb_menu");
 			try{//herb_menu table  ޴  
 				while(menuListQuery.next()){
 					MenuList.herbMenuInt.put(menuListQuery.getString(2), //menu_name
 							new Integer(menuListQuery.getInt(1)) // menu_id
 							);
 				}
 				ResultSet orderQuery = os.db.exec("SELECT order_id FROM herb_order");
 				while(orderQuery.next()){
 					if(this.latestOrderID < orderQuery.getInt(1))
 						this.latestOrderID = orderQuery.getInt(1);
 					// ֱ orderID ޾ƿ´.
 				}
 			}catch(SQLException ex){
 				ex.printStackTrace();
 			}
 			//End of Menu Recommend
 			
 		} catch (SQLException e) {
 
 		}
 	}
 
 	private void updateOrderTable() {
 		String data[][] = new String[orderList.size()][4];
 		for (int i = 0; i < orderList.size(); i++) {
 			data[i][0] = orderList.get(i).getMenuName();
 			data[i][1] = Integer.toString(orderList.get(i).getMenuCount());
 			data[i][2] = Integer.toString(orderList.get(i).getMenuPrice());
 			data[i][3] = Integer.toString(orderList.get(i).getTotalPrice());
 		}
 		
 		orderListTableModel.setDataVector(data, columnNames);
 	}
 	
 	public void resetTotal(){	//ֹ(ʱȭ)
 		this.orderTotal = 0;
 		this.orderList.clear();
 		updateOrderTable();
 	}
 	
 	public ArrayList<CustomerOrder> getOrderList(){
 		return this.orderList;
 	}
 	
 	
 	// Mody Use Method
 	public String returnName()
 	{
 		return modyName;
 	}
 	
 	public void dataUpdate() { //  Ǿ κ
 
 	}
 }
