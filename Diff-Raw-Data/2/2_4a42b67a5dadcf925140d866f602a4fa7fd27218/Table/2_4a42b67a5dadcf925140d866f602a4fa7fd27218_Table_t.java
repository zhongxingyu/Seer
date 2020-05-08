 package snookerTables;
 
 import java.awt.BasicStroke;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.Toolkit;
 import java.awt.event.*;
 import java.text.NumberFormat;
 import java.util.Properties;
 
 import javax.swing.*;
 
 public class Table extends JPanel implements ActionListener, MouseListener {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private TableOrder order;
 	private Timer stopWatch = new Timer(this);
 	private String tableTime = "0:0:0", tableName;
 	private double hirePrice = 0.00, priceConstant,
 			drinkPrice, foodPrice, extraPrice, totalPrice, fullPrice, savedPrice, currentPrice, membershipPrice;
 	private JLabel timerLabel, priceLabel, currentPriceLabel;
 	private boolean running = false;
 	private JPanel center, controls, bottomBar, center2, topBottom,
 			bottomBottom, container;
 	private JButton start, priceButton, changeRate;
 	private NumberFormat formatter;
 	private Main main;
 	private JPopupMenu extraPopup, extraRate;
 	private int timeElapsed;
 	private int type, savedTime;
 	private JMenu current;
 
 	/** Stroke size. it is recommended to set it to 1 for better view */
 	protected int strokeSize = 1;
 	/** Color of shadow */
 	protected Color shadowColor = Color.black;
 	/** Sets if it drops shadow */
 	protected boolean shady = true;
 	/** Sets if it has an High Quality view */
 	protected boolean highQuality = true;
 	/** Double values for Horizontal and Vertical radius of corner arcs */
 	protected Dimension arcs = new Dimension(20, 20);
 	/** Distance between shadow border and opaque panel border */
 	protected int shadowGap = 5;
 	/** The offset of shadow. */
 	protected int shadowOffset = 4;
 	/** The transparency value of shadow. ( 0 - 255) */
 	protected int shadowAlpha = 150;
 
 	public Table(String tableName, int type, Main main) {
 		super();
 		setOpaque(false);
 
 		this.type = type;
 		this.main = main;
 		this.tableName = tableName;
 		order = new TableOrder(this);
 		// this.setLayout(new BorderLayout());
 		container = new JPanel(new BorderLayout());
 		this.add(container);
 		// this.setPreferredSize(new Dimension(220,215));
 		// setMaximumSize(new Dimension(100,100));
 
 		setHireCost();
 		
 		Toolkit toolkit = Toolkit.getDefaultToolkit();
 		Dimension dimension = toolkit.getScreenSize();
 		int height = dimension.height;
 		int width = dimension.width;
 //		this.setPreferredSize(new Dimension(width / 2 / 3, (height - 200) / 3));
 		this.setPreferredSize(new Dimension(205, 200));
 
 		JLabel name = new JLabel(tableName, JLabel.CENTER);
 		name.setFont(new Font(null, Font.BOLD, 15));
 		// name.setPreferredSize(new Dimension(0, 25));
 		container.add(name, BorderLayout.NORTH);
 		formatter = NumberFormat.getCurrencyInstance();
 
 		controls = new JPanel(new FlowLayout());
 
 		start = new JButton("Start");
 		start.setActionCommand("start");
 
 		extraRate = new JPopupMenu();
 		changeRate = new JButton("Full");
 		changeRate.addMouseListener(new MouseAdapter() {
 			public void mousePressed(MouseEvent e){
 				extraRate.show(e.getComponent(), e.getX(), e.getY());
 			}
 		});
 		
 		bottomBar = new JPanel(new BorderLayout());
 		container.add(bottomBar, BorderLayout.SOUTH);
 		bottomBar.add(changeRate);
 		topBottom = new JPanel();
 		bottomBar.add(topBottom, BorderLayout.NORTH);
 		topBottom.add(changeRate);
 
 		
 		
 		JMenuItem full = new JMenuItem("Full");
 		full.addActionListener(this);
 		full.setActionCommand("full");
 		JMenuItem half = new JMenuItem("Half");
 		half.addActionListener(this);
 		half.setActionCommand("half");
 		JMenuItem free = new JMenuItem("Free");
 		free.addActionListener(this);
 		free.setActionCommand("free");
 		extraRate.add(full);
 		extraRate.add(half);
 		extraRate.add(free);
 		
 		priceButton = new JButton("PPH: "
 				+ formatter.format((priceConstant * 60 * 60.0)));
 		priceButton.setActionCommand("price");
 		JButton reset = new JButton("Reset");
 		reset.setActionCommand("reset");
 		this.addMouseListener(this);
 		controls.add(start);
 		// controls.add(stop);
 		controls.add(reset);
 
 		center = new JPanel(new BorderLayout());
 		center.add(controls, BorderLayout.NORTH);
 
 	
 		
 		timerLabel = new JLabel(tableTime, JLabel.CENTER);
 		timerLabel.setFont(new Font(null, Font.PLAIN, 35));
 		priceLabel = new JLabel(formatter.format(hirePrice), JLabel.CENTER);
 		priceLabel.setFont(new Font(null, Font.PLAIN, 15));
 		center2 = new JPanel(new BorderLayout());
 		center2.add(timerLabel, BorderLayout.CENTER);
 		center2.add(priceLabel, BorderLayout.SOUTH);
 		container.add(center, BorderLayout.CENTER);
 		center.add(center2, BorderLayout.CENTER);
 
 		// JPanel bottomPanel = new JPanel();
 		
 		currentPriceLabel = new JLabel();
 		setCurrentPriceLabel();
 		topBottom.add(currentPriceLabel);
 		// bottomPanel.add(priceButton, BorderLayout.SOUTH);
 
 		JButton detail = new JButton("Details");
 		detail.setActionCommand("detail");
 		topBottom.add(detail);
 		detail.addActionListener(this);
 		// JButton orders = new JButton("Orders");
 		// orders.setActionCommand("orders");
 		// bottomPanel.add(orders);
  
 		
 		
 		bottomBottom = new JPanel();
 		bottomBar.add(bottomBottom, BorderLayout.SOUTH);
 		
 		extraPopup = new JPopupMenu();
 		JButton addExtra = new JButton("Add");
 		bottomBar.add(addExtra);
 		addExtra.addMouseListener(new MouseAdapter() {
 			public void mousePressed(MouseEvent e){
 				extraPopup.show(e.getComponent(), e.getX(), e.getY());
 			}
 		});
 		bottomBar.add(addExtra);
 		
 //		JMenuBar bottomMenu= new JMenuBar();
 //		bottomBar.add(bottomMenu);
 		
 //		JMenu addExtra = new JMenu("Add");
 //		bottomMenu.add(addExtra);
 		
 		JMenuItem drink = new JMenuItem("Drink");
 		drink.setActionCommand("drink");
 		drink.addActionListener(this);
 		extraPopup.add(drink);
 
 		JMenuItem food = new JMenuItem("Food");
 		food.setActionCommand("food");
 		food.addActionListener(this);
 		extraPopup.add(food);
 
 		JMenuItem extra = new JMenuItem("Extra");
 		extra.setActionCommand("extra");
 		extra.addActionListener(this);
 		extraPopup.add(extra);
 		
 		JMenuItem membership = new JMenuItem("Membership");
 		membership.setActionCommand("membership");
 		membership.addActionListener(this);
 		extraPopup.add(membership);
 
 		start.addActionListener(this);
 		// orders.addActionListener(this);
 		priceButton.addActionListener(this);
 		reset.addActionListener(this);
 
 		changeBackground(new Color(220, 20, 60));
 
 	}
 
 	public TableOrder getOrder() {
 		return order;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if ("start".equals(e.getActionCommand()) && !running) {
 			createTimer();
 			changeBackground(new Color(33, 200, 50));
 			running = true;
 			start.setText("Stop");
 			start.setActionCommand("stop");
 		} else if ("stop".equals(e.getActionCommand())) {
 			stopTimer();
 			running = false;
 			changeBackground(new Color(237, 145, 33));
 			start.setText("Resume");
 			start.setActionCommand("start");
 		} else if ("price".equals(e.getActionCommand())) {
 			try {
 				String amount = JOptionPane.showInputDialog("Amount per Hour");
 				if (amount != null) {
 					setPriceConstant(Double.parseDouble(amount));
 				}
 			} catch (NumberFormatException ex) {
 				JOptionPane.showMessageDialog(this, "Number required");
 			}
 		} else if ("reset".equals(e.getActionCommand())) {
 			if (JOptionPane.showConfirmDialog(this, "Reset " + getTableName()
 					+ "?", "Confirm", JOptionPane.YES_NO_OPTION) == 0) {
 				if (running == true) {
 					stopTimer();
 					running = false;
 				}
 				setTime(0, 0, 0);
 				hirePrice = 0;
 				drinkPrice = 0;
 				foodPrice = 0;
 				extraPrice = 0;
 				totalPrice = 0;
 				savedTime = 0;
 				savedPrice = 0;
 				setPrice();
 				order.updatePrice();
 				stopWatch.reset();
 				changeBackground(new Color(220, 20, 60));
 				start.setText("Start");
 				start.setActionCommand("start");
				changeRate.setText("Full");
 				changePrice(Globals.FULL);
 				setCurrentPriceLabel();	
 			}
 		} else if ("detail".equals(e.getActionCommand())) {
 			main.toggleOrderVisibility(this);
 		} else if ("drink".equals(e.getActionCommand())) {
 			String mess = JOptionPane.showInputDialog("Price of Drink (£)");
 			if (mess != null) {
 				try {
 					drinkPrice += Double.parseDouble(mess);
 					setPrice();
 				} catch (NumberFormatException f) {
 					JOptionPane
 							.showMessageDialog(this, "Error: Input a number");
 				}
 			}
 		} else if ("food".equals(e.getActionCommand())) {
 			String mess = JOptionPane.showInputDialog("Price of Food (£)");
 			if (mess != null) {
 				try {
 					foodPrice += Double.parseDouble(mess);
 					setPrice();
 				} catch (NumberFormatException f) {
 					JOptionPane
 							.showMessageDialog(this, "Error: Input a number");
 				}
 			}
 		} else if ("extra".equals(e.getActionCommand())) {
 			String mess = JOptionPane.showInputDialog("Price of Extra (£)");
 			if (mess != null) {
 				try {
 					extraPrice += Double.parseDouble(mess);
 					setPrice();
 				} catch (NumberFormatException f) {
 					JOptionPane
 							.showMessageDialog(this, "Error: Input a number");
 				}
 			}
 			
 		}else if ("membership".equals(e.getActionCommand())) {
 			String mess = JOptionPane.showInputDialog("Price of Membership (£)");
 			if (mess != null) {
 				try {
 					membershipPrice += Double.parseDouble(mess);
 					setPrice();
 				} catch (NumberFormatException f) {
 					JOptionPane
 							.showMessageDialog(this, "Error: Input a number");
 				}
 			}
 			}else if("full".equals(e.getActionCommand())){
 			changeRate.setText("Full");
 			changePrice(Globals.FULL);
 			setCurrentPriceLabel();			
 		}else if("half".equals(e.getActionCommand())){
 			changeRate.setText("Half");
 			changePrice(Globals.HALF);
 			setCurrentPriceLabel();
 		}else if("free".equals(e.getActionCommand())){
 			changeRate.setText("Free");
 			changePrice(Globals.FREE);
 			setCurrentPriceLabel();
 		}
 		main.resizeTables();
 
 	}
 	
 	private void changePrice(int level){
 		if(level==Globals.FULL){
 			priceConstant=fullPrice;
 			savedPrice = savedPrice + currentPrice;
 			currentPrice=0;
 			savedTime = timeElapsed;
 		}else if(level==Globals.HALF){
 			priceConstant=fullPrice/2;
 			savedPrice = savedPrice + currentPrice;
 			currentPrice=0;
 			savedTime = timeElapsed;
 		}else if(level==Globals.FREE){
 			priceConstant=0;
 			savedPrice = savedPrice + currentPrice;
 			currentPrice=0;
 			savedTime = timeElapsed;
 		}
 		
 	}
 
 	public Main getMain() {
 		return main;
 	}
 	
 	private void setCurrentPriceLabel(){
 		currentPriceLabel.setText(formatter.format(priceConstant*3600));
 	}
 	
 	private void setHireCost(){
 		//read in hireCosts
 		if(type==Globals.SNOOKER){
 			priceConstant = 36 / 60.0 / 60.0;
 		}else if(type==Globals.POOL){
 			priceConstant = 5 / 60.0 / 60.0;
 		}
 		fullPrice=priceConstant;
 	}
 
 	private void changeBackground(Color newColor) {
 		this.setBackground(newColor);
 		center.setBackground(newColor);
 		controls.setBackground(newColor);
 		center2.setBackground(newColor);
 		bottomBar.setBackground(newColor);
 		topBottom.setBackground(newColor);
 		bottomBottom.setBackground(newColor);
 		container.setBackground(newColor);
 	}
 
 	public void setPriceConstant(double perHour) {
 		priceConstant = perHour / 3600.0;
 		priceButton.setText("PPH: " + formatter.format(perHour));
 	}
 
 	public void setTime(int hour, int min, int sec) {
 		tableTime = hour + ":" + min + ":" + sec;
 		timerLabel.setText(tableTime);
 		timeElapsed = hour * 60 * 60 + min * 60 + sec;
 		setPrice();
 	}
 
 	public void setPrice() {
 		
 		
 		currentPrice = (timeElapsed-savedTime) * priceConstant;		
 		hirePrice = currentPrice+savedPrice;
 		totalPrice = hirePrice + drinkPrice + foodPrice + extraPrice + membershipPrice;
 		priceLabel.setText(formatter.format(totalPrice));
 		order.updatePrice();
 	}
 
 	public double getHirePrice() {
 		return hirePrice;
 	}
 	
 	public double getMembershipPrice() {
 		return membershipPrice;
 	}
 
 	public double getDrinkPrice() {
 		return drinkPrice;
 	}
 
 	public double getFoodPrice() {
 		return foodPrice;
 	}
 
 	public double getExtraPrice() {
 		return extraPrice;
 	}
 
 	private void createTimer() {
 		new Thread(stopWatch).start();
 	}
 
 	private void stopTimer() {
 		stopWatch.stop();
 	}
 
 	public String getTableName() {
 		return tableName;
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		// main.toggleOrderVisibility(this);
 
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	protected void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		int width = getWidth();
 		int height = getHeight();
 		int shadowGap = this.shadowGap;
 		Color shadowColorA = new Color(shadowColor.getRed(),
 				shadowColor.getGreen(), shadowColor.getBlue(), shadowAlpha);
 		Graphics2D graphics = (Graphics2D) g;
 
 		// Sets antialiasing if HQ.
 		if (highQuality) {
 			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 					RenderingHints.VALUE_ANTIALIAS_ON);
 		}
 
 		// Draws shadow borders if any.
 		if (shady) {
 			graphics.setColor(shadowColorA);
 			graphics.fillRoundRect(shadowOffset,// X position
 					shadowOffset,// Y position
 					width - strokeSize - shadowOffset, // width
 					height - strokeSize - shadowOffset, // height
 					arcs.width, arcs.height);// arc Dimension
 		} else {
 			shadowGap = 1;
 		}
 
 		// Draws the rounded opaque panel with borders.
 		graphics.setColor(getBackground());
 		graphics.fillRoundRect(0, 0, width - shadowGap, height - shadowGap,
 				arcs.width, arcs.height);
 		graphics.setColor(getForeground());
 		graphics.setStroke(new BasicStroke(strokeSize));
 		graphics.drawRoundRect(0, 0, width - shadowGap, height - shadowGap,
 				arcs.width, arcs.height);
 
 		// Sets strokes to default, is better.
 		graphics.setStroke(new BasicStroke());
 	}
 
 }
