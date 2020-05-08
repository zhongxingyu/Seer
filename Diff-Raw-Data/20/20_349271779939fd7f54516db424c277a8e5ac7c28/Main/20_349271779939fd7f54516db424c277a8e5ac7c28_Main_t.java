 package snookerTables;
 
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.util.ArrayList;
 
 import javax.swing.*;
 
 public class Main extends JFrame implements ActionListener{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private JPanel tables, container, central;
 	private JScrollPane scroller;
 	private JLabel subTitle, temp;
 	private int numberOfTables = 0, active;
 	private ArrayList<Table> tableList;
 	private JButton tableReturn, settings;
 	private Table showingOrder;
 	private Color color;
 	
 	public Main(){
 		tableList = new ArrayList<Table>();
 		container = new JPanel(new BorderLayout());
         add(container);
 		color = new Color(56,142,142);
 		this.setVisible(true);
 		this.setSize(900, 800);
 		this.setLayout(new FlowLayout());
 		this.getContentPane().setBackground(color);
 		JPanel topBar = new JPanel();
 		topBar.setBackground(color);
 		JLabel name = new JLabel("Snooker Club");
 		name.setFont(new Font(null, Font.BOLD, 30));
 		topBar.add(name);
 		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
 		container.add(topBar, BorderLayout.NORTH);
 		container.setPreferredSize(new Dimension(this.getWidth()-10, this.getHeight()-40));
 		tables = new JPanel();		
 		central = new JPanel(new BorderLayout());
 		central.setPreferredSize(new Dimension(container.getWidth()-10, container.getHeight()-10));
 		central.setBackground(color);
 		subTitle = new JLabel("Tables", JLabel.CENTER);
 		subTitle.setFont(new Font(null,Font.BOLD, 20));
 		central.add(subTitle, BorderLayout.NORTH);
 		scroller = new JScrollPane(tables);  
 		central.add(scroller, BorderLayout.CENTER); 
 		container.add(central, BorderLayout.CENTER); 
 		tables.setBackground(color);
 //		for(int i=1; i<=numberOfTables; i++){
 //			Table table = new Table("Table "+i);
 //			tables.add(table);
 //		}
 		addTables(6);
 		int numberPerRow = (this.getWidth()-100)/260;
 		Double number = (double)numberOfTables / (double)numberPerRow;
 		int numberPerColumn = (int)Math.ceil(number);
 		scroller.setPreferredSize(new Dimension(this.getWidth()-40, this.getHeight()-120));
 		tables.setPreferredSize(new Dimension(this.getWidth()-100, numberPerColumn*310));
 		
 		JPanel bottomBar = new JPanel();
 		bottomBar.setBackground(color);
 		container.add(bottomBar, BorderLayout.SOUTH);
 		settings = new JButton("Settings");
 		settings.setActionCommand("settings");
 		settings.addActionListener(this);
 		bottomBar.add(settings);
 		tableReturn = new JButton("Tables");
 		tableReturn.setActionCommand("table");
 		tableReturn.addActionListener(this);
 		tableReturn.setVisible(false);
 		bottomBar.add(tableReturn);
 		updateUI();
 		
 		this.addComponentListener(new ComponentAdapter() {
 			public void componentResized(ComponentEvent comp){
 				resizeTables();				
 			}
 		});
 		
 		
 		
 		
 	}
 	
 	public void toggleTablesVisibility(){
 		
 		if(active!=0){
 			central.add(scroller, BorderLayout.CENTER);
 			subTitle.setText("Tables");
 			active=0;
 			settings.setVisible(true);
 			updateUI();
 		}else{
 			central.remove(scroller);
 			updateUI();
 		}
 	}
 	
 	public void toggleOrderVisibility(Table table){
 		showingOrder = table;
 		showingOrder.getOrder().toggleVisible();
 //		if(active!=1){
 //			turnActiveOff();
 //			central.add(table.getOrder(), BorderLayout.CENTER);
 //			table.getOrder().setPreferredSize(new Dimension(this.getWidth()-40, this.getHeight()-120));
 //			table.getOrder().setBackground(color);
 //			tableReturn.setVisible(true);
 //			settings.setVisible(false);
 //			active=1;
 //			subTitle.setText("Order");
 //			updateUI();
 //		}else{
 //			central.remove(table.getOrder());
 //			toggleTablesVisibility();
 //			tableReturn.setVisible(false);
 //			updateUI();
 //		}
 		
 	}
 	
 	
 	public void addTables(int newTables){
 		if(newTables>numberOfTables){
 			for(int i=numberOfTables; i<newTables; i++){
 				Table table = new Table("Table "+(i+1), this);
 				tables.add(table);
 				tableList.add(table);
 			}
 		}
 		if(newTables<numberOfTables){
 			for (int i=numberOfTables; i>newTables; i--){
 				tables.remove(tableList.get(i-1));
 				tableList.remove(i-1);
 			}
 		}
 		numberOfTables=newTables;
 //		tables.removeAll();
 //		for(int i=1; i<=numberOfTables; i++){
 //			Table table = new Table("Table "+i);
 //			tables.add(table);
 //			tableList.add(table);
 //		}
 	}
 	
 	public void setAllPrices(double price){
 		for(int i=0; i<tableList.size(); i++){
 			tableList.get(i).setPriceConstant(price);
 		}
 		
 	}
 	public int getNumberOfTables(){
 		return numberOfTables;
 	}
 	
 	public void newTables(int number){
 		addTables(number);
 		resizeTables();
 	}
 	
 	private void updateUI(){
		this.revalidate();
		tables.revalidate();
		scroller.revalidate();
		container.revalidate();
 	}
 
 	
 	public void resizeTables(){
 		scroller.setPreferredSize(new Dimension(this.getWidth()-40, this.getHeight()-120));
 		container.setPreferredSize(new Dimension(this.getWidth()-10, this.getHeight()-40));
 		int numberPerRow = (this.getWidth()-40)/260;
 		Double number = (double)numberOfTables / (double)numberPerRow;
 		int numberPerColumn = (int)Math.ceil(number);
 //		System.out.println(numberPerRow);
 		central.setPreferredSize(new Dimension(container.getWidth()-10, container.getHeight()-10));
 		tables.setPreferredSize(new Dimension(this.getWidth()-100, numberPerColumn*310));
 		updateUI();
 	}
 	
 	public static void main(String[] args){
 		new Main();
 	}
 
 	private void turnActiveOff(){
 		if(active==0){
 			toggleTablesVisibility();
 		}
 		if(active==1){
 			toggleOrderVisibility(tableList.get(0));
 		}
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if("settings".equals(e.getActionCommand())){
 			new Settings(this);
 		}else if("table".equals(e.getActionCommand())){
 			toggleOrderVisibility(showingOrder);
 		}
 		
 	}
 }
