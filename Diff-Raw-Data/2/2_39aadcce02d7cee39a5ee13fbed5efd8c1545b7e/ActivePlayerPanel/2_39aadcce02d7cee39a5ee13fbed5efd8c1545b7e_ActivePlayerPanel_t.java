 import java.awt.Dimension;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.SpringLayout;
 import javax.swing.JLabel;
 import javax.swing.SwingConstants;
 import java.awt.Font;
 import javax.swing.JSeparator;
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JTextField;
 
 
 public class ActivePlayerPanel extends JPanel {
 
 	GameLogic gl;
 	SpringLayout layout;
 	private JButton trade;
 	private JTextField textP1cards;
 	private JTextField textP1points;
 	private JTextField textP1devCards;
 	private JTextField textP2cards;
 	private JTextField textP2points;
 	private JTextField textP2devCards;
 	private JTextField textP3cards;
 	private JTextField textP3points;
 	private JTextField textP3devCards;
 	private JTextField textP4cards;
 	private JTextField textP4points;
 	private JTextField textP4devCards;
 	private JTextField textP1sold;
 	private JTextField lumbT;
 	private JTextField sheepT;
 	private JTextField brickT;
 	private JTextField ironT;
 	private JTextField wheatT;
 	private JTextField soldierT;
 	private JTextField monopolyT;
 	private JTextField roadT;
 	private JTextField yearT;
 	private JTextField victoryT;
 	private JTextField lumbTrade;
 	private JTextField sheepTrade;
 	private JTextField brickTrade;
 	private JTextField ironTrade;
 	private JTextField WheatTrade;
 	private int activePlayerNum=0;//change this and afterwards to gamelogics
 	private int lumbI=0; 
 	private int sheepI=0;
 	private int brickI=0;
 	private int ironI=0;
 	private int wheatI=0;
 	private int soldierI=0;
 	private int monopolyI=0;
 	private int roadI=0;
 	private int yearI=0;
 	private int victoryI=0;
 	private int resources=0;
 	
 	
 	
 	public ActivePlayerPanel(GameLogic gl) {
 		this.gl = gl;
 		setPreferredSize(new Dimension(150, 700));
 		layout = new SpringLayout();
 		setLayout(layout);
 		
 		BtnListener lstn = new BtnListener();
 		
 		JLabel lblP1 = new JLabel("Active Player: 1");
 		layout.putConstraint(SpringLayout.NORTH, lblP1, 10, SpringLayout.NORTH, this);
 		layout.putConstraint(SpringLayout.WEST, lblP1, 0, SpringLayout.WEST, this);
 		layout.putConstraint(SpringLayout.EAST, lblP1, 0, SpringLayout.EAST, this);
 		lblP1.setFont(new Font("Tahoma", Font.BOLD, 14));
 		lblP1.setHorizontalAlignment(SwingConstants.CENTER);
 		add(lblP1);
 		
 		JSeparator p1Separator = new JSeparator();
 		layout.putConstraint(SpringLayout.NORTH, p1Separator, 3, SpringLayout.SOUTH, lblP1);
 		layout.putConstraint(SpringLayout.WEST, p1Separator, 10, SpringLayout.WEST, this);
 		layout.putConstraint(SpringLayout.EAST, p1Separator, -10, SpringLayout.EAST, this);
 		p1Separator.setForeground(Color.BLACK);
 		add(p1Separator);
 		
 				
 		textP1cards = new JTextField("0");
 		layout.putConstraint(SpringLayout.EAST, textP1cards, -3, SpringLayout.EAST, this);
 		textP1cards.setHorizontalAlignment(SwingConstants.RIGHT);
 		textP1cards.setEditable(false);
 		layout.putConstraint(SpringLayout.NORTH, textP1cards, 3, SpringLayout.SOUTH, p1Separator);
 		textP1cards.setColumns(3);
 		add(textP1cards);
 		
 		JLabel lblP1cards = new JLabel("Total Resources:");
 		layout.putConstraint(SpringLayout.WEST, lblP1cards, 3, SpringLayout.WEST, this);
 		layout.putConstraint(SpringLayout.NORTH, lblP1cards, 3, SpringLayout.NORTH, textP1cards);
 		add(lblP1cards);
 		
 		lumbT = new JTextField("0");
 		layout.putConstraint(SpringLayout.EAST, lumbT, 0, SpringLayout.EAST, textP1cards);
 		lumbT.setHorizontalAlignment(SwingConstants.RIGHT);
 		layout.putConstraint(SpringLayout.NORTH, lumbT, 6, SpringLayout.SOUTH, textP1cards);
 		lumbT.setEditable(false);
 		add(lumbT);
 		lumbT.setColumns(3);
 
 		JLabel lumb = new JLabel("Lumber:");
 		layout.putConstraint(SpringLayout.WEST, lumb, 0, SpringLayout.WEST, lblP1cards);
 		layout.putConstraint(SpringLayout.NORTH, lumb, 3, SpringLayout.NORTH, lumbT);
 		add(lumb);
 		
 		
 		sheepT = new JTextField("0");
 		layout.putConstraint(SpringLayout.EAST, sheepT, 0, SpringLayout.EAST, lumbT);
 		sheepT.setHorizontalAlignment(SwingConstants.RIGHT);
 		layout.putConstraint(SpringLayout.NORTH, sheepT, 6, SpringLayout.SOUTH, lumbT);
 		sheepT.setEditable(false);
 		sheepT.setColumns(3);
 		add(sheepT);
 		
 		JLabel sheep = new JLabel("Sheep:");
 		layout.putConstraint(SpringLayout.NORTH, sheep, 3, SpringLayout.NORTH, sheepT);
 		layout.putConstraint(SpringLayout.WEST, sheep, 0, SpringLayout.WEST, lblP1cards);
 		add(sheep);
 		
 		brickT = new JTextField("0");
 		layout.putConstraint(SpringLayout.EAST, brickT, 0, SpringLayout.EAST, sheepT);
 		brickT.setHorizontalAlignment(SwingConstants.RIGHT);
 		layout.putConstraint(SpringLayout.NORTH, brickT, 6, SpringLayout.SOUTH, sheepT);
 		brickT.setEditable(false);
 		brickT.setColumns(3);
 		add(brickT);
 		
 		
 		JLabel brick = new JLabel("Brick:");
 		layout.putConstraint(SpringLayout.NORTH, brick, 3, SpringLayout.NORTH, brickT);
 		layout.putConstraint(SpringLayout.WEST, brick, 0, SpringLayout.WEST, sheep);
 		add(brick);
 		
 		ironT = new JTextField("0");
 		layout.putConstraint(SpringLayout.EAST, ironT, 0, SpringLayout.EAST, brickT);
 		ironT.setHorizontalAlignment(SwingConstants.RIGHT);
 		layout.putConstraint(SpringLayout.NORTH, ironT, 6, SpringLayout.SOUTH, brickT);
 		ironT.setEditable(false);
 		ironT.setColumns(3);
 		add(ironT);
 		
 		JLabel iron = new JLabel("Iron:");
 		layout.putConstraint(SpringLayout.NORTH, iron, 3, SpringLayout.NORTH, ironT);
 		layout.putConstraint(SpringLayout.WEST, iron, 0, SpringLayout.WEST, sheep);
 		add(iron);		
 		
 		wheatT = new JTextField("0");
 		layout.putConstraint(SpringLayout.EAST, wheatT, 0, SpringLayout.EAST, ironT);
 		wheatT.setHorizontalAlignment(SwingConstants.RIGHT);
 		layout.putConstraint(SpringLayout.NORTH, wheatT, 6, SpringLayout.SOUTH, ironT);
 		wheatT.setEditable(false);
 		wheatT.setColumns(3);
 		add(wheatT);
 
 		JLabel wheat = new JLabel("Wheat:");
 		layout.putConstraint(SpringLayout.NORTH, wheat, 3, SpringLayout.NORTH, wheatT);
 		layout.putConstraint(SpringLayout.WEST, wheat, 0, SpringLayout.WEST, sheep);
 		add(wheat);
 		
 		JButton btnTrade = new JButton("Trade");
 		layout.putConstraint(SpringLayout.NORTH, btnTrade, 10, SpringLayout.SOUTH, wheat);
 		layout.putConstraint(SpringLayout.WEST, btnTrade, 15, SpringLayout.WEST, p1Separator);
 		layout.putConstraint(SpringLayout.EAST, btnTrade, -15, SpringLayout.EAST, p1Separator);
 		add(btnTrade);
 		btnTrade.setActionCommand("trade");
 		btnTrade.addActionListener(lstn);
 		
 		
 		
 		//after resources. knight cards, monopoly, road building, year of plenty, victory point
 		JLabel soldier = new JLabel("Soldier Cards:");
 		layout.putConstraint(SpringLayout.WEST, soldier, 0, SpringLayout.WEST, sheep);
 		add(soldier);
 		
 		soldierT = new JTextField("0");
 		layout.putConstraint(SpringLayout.NORTH, soldier, 3, SpringLayout.NORTH, soldierT);
 		soldierT.setHorizontalAlignment(SwingConstants.RIGHT);
 		layout.putConstraint(SpringLayout.NORTH, soldierT, 5, SpringLayout.SOUTH, btnTrade);
 		layout.putConstraint(SpringLayout.EAST, soldierT, 0, SpringLayout.EAST, wheatT);
 		soldierT.setEditable(false);
 		soldierT.setColumns(3);
 		add(soldierT);
 		
 				
 		JLabel monopoly = new JLabel("Monopoly Cards:");
 		layout.putConstraint(SpringLayout.WEST, monopoly, 0, SpringLayout.WEST, sheep);
 		add(monopoly);
 		
 		monopolyT = new JTextField("0");
 		layout.putConstraint(SpringLayout.NORTH, monopoly, 3, SpringLayout.NORTH, monopolyT);
 		monopolyT.setHorizontalAlignment(SwingConstants.RIGHT);
 		layout.putConstraint(SpringLayout.NORTH, monopolyT, 6, SpringLayout.SOUTH, soldierT);
 		layout.putConstraint(SpringLayout.EAST, monopolyT, 0, SpringLayout.EAST, soldierT);
 		monopolyT.setEditable(false);
 		add(monopolyT);
 		monopolyT.setColumns(3);
 		
 		JLabel road = new JLabel("R. Building Cards:");
 		layout.putConstraint(SpringLayout.WEST, road, 0, SpringLayout.WEST, sheep);
 		add(road);
 		
 		roadT = new JTextField("0");
 		layout.putConstraint(SpringLayout.NORTH, road, 3, SpringLayout.NORTH, roadT);
 		roadT.setHorizontalAlignment(SwingConstants.RIGHT);
 		layout.putConstraint(SpringLayout.EAST, roadT, 0, SpringLayout.EAST, monopolyT);
 		layout.putConstraint(SpringLayout.NORTH, roadT, 6, SpringLayout.SOUTH, monopolyT);
 		roadT.setEditable(false);
 		add(roadT);
 		roadT.setColumns(3);
 		
 		JLabel year = new JLabel("Y. of Plenty Cards:");
 		layout.putConstraint(SpringLayout.WEST, year, 0, SpringLayout.WEST, sheep);
 		add(year);
 		
 		yearT = new JTextField("0");
 		layout.putConstraint(SpringLayout.NORTH, year, 3, SpringLayout.NORTH, yearT);
 		yearT.setHorizontalAlignment(SwingConstants.RIGHT);
 		layout.putConstraint(SpringLayout.EAST, yearT, 0, SpringLayout.EAST, roadT);
 		layout.putConstraint(SpringLayout.NORTH, yearT, 6, SpringLayout.SOUTH, roadT);
 		yearT.setEditable(false);
 		add(yearT);
 		yearT.setColumns(3);
 		
 		JLabel victory = new JLabel("Victory Cards:");
 		layout.putConstraint(SpringLayout.WEST, victory, 0, SpringLayout.WEST, sheep);
 		add(victory);
 		
 		victoryT = new JTextField("0");
 		victoryT.setHorizontalAlignment(SwingConstants.RIGHT);
 		layout.putConstraint(SpringLayout.NORTH, victory, 3, SpringLayout.NORTH, victoryT);
 		layout.putConstraint(SpringLayout.EAST, victoryT, 0, SpringLayout.EAST, yearT);
 		layout.putConstraint(SpringLayout.NORTH, victoryT, 6, SpringLayout.SOUTH, yearT);
 		victoryT.setEditable(false);
 		add(victoryT);
 		victoryT.setColumns(3);
 		
 		JButton btnBuildRoad = new JButton("Road: 1W, 1B");
 		layout.putConstraint(SpringLayout.WEST, btnBuildRoad, 2, SpringLayout.WEST, this);
 		layout.putConstraint(SpringLayout.EAST, btnBuildRoad, -2, SpringLayout.EAST, this);
 		add(btnBuildRoad);
 		
 		JButton btnBuildSet = new JButton("Sett: 1L, 1B, 1W, 1S");
 		layout.putConstraint(SpringLayout.NORTH, btnBuildSet, 6, SpringLayout.SOUTH, btnBuildRoad);
 		layout.putConstraint(SpringLayout.WEST, btnBuildSet, 2, SpringLayout.WEST, this);
 		layout.putConstraint(SpringLayout.EAST, btnBuildSet, -2, SpringLayout.EAST, this);
 		add(btnBuildSet);
 		
		JButton btnBuildCity = new JButton("City: 2W, 3I, 1Sett");
 		layout.putConstraint(SpringLayout.NORTH, btnBuildCity, 6, SpringLayout.SOUTH, btnBuildSet);
 		layout.putConstraint(SpringLayout.WEST, btnBuildCity, 2, SpringLayout.WEST, this);
 		layout.putConstraint(SpringLayout.EAST, btnBuildCity, -2, SpringLayout.EAST, this);
 		add(btnBuildCity);
 		
 		JButton btnDevCardw = new JButton("Dev C:1W, 1S, 1I");
 		layout.putConstraint(SpringLayout.NORTH, btnDevCardw, 6, SpringLayout.SOUTH, btnBuildCity);
 		layout.putConstraint(SpringLayout.WEST, btnDevCardw, 2, SpringLayout.WEST, this);
 		layout.putConstraint(SpringLayout.EAST, btnDevCardw, -2, SpringLayout.EAST, this);
 		add(btnDevCardw);
 		
 		JLabel lblBuildBtns = new JLabel("Build:");
 		layout.putConstraint(SpringLayout.NORTH, btnBuildRoad, 3, SpringLayout.SOUTH, lblBuildBtns);
 		layout.putConstraint(SpringLayout.NORTH, lblBuildBtns, 15, SpringLayout.SOUTH, victoryT);
 		layout.putConstraint(SpringLayout.WEST, lblBuildBtns, 0, SpringLayout.WEST, this);
 		layout.putConstraint(SpringLayout.EAST, lblBuildBtns, 0, SpringLayout.EAST, this);
 		lblBuildBtns.setHorizontalAlignment(SwingConstants.CENTER);
 		lblBuildBtns.setFont(new Font("Tahoma", Font.BOLD, 12));
 		add(lblBuildBtns);
 		
 		
 	}
 	
 	public void update(GameLogic l){
 		//TODO
 		this.gl = l;
 		
 	}
 	
 	class BtnListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			String source = e.getActionCommand();
 			if(source.equals("trade")){
 				gl.newChatMessage("System: Player " + gl.getActivePlayer() + " would like a trade.");
 				gl.startTrade(activePlayerNum);
 			
 			
 			}
 		}
 		
 		
 		
 	}
 }
