 package edu.brown.cs32.MFTG.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GridLayout;
 import java.awt.Point;
 import java.awt.RenderingHints;
 import java.awt.TextField;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import javax.imageio.ImageIO;
 import javax.swing.ButtonGroup;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JTextField;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.MouseInputAdapter;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.PlainDocument;
 
 import com.fasterxml.jackson.annotation.JsonProperty;
 
 import edu.brown.cs32.MFTG.monopoly.GamePlayer;
 import edu.brown.cs32.MFTG.monopoly.Player;
 import edu.brown.cs32.MFTG.tournament.Settings;
 import edu.brown.cs32.MFTG.tournament.Settings.Turns;
 import edu.brown.cs32.MFTG.tournament.Settings.WinningCondition;
 
 public class CreateBottomPanel extends JPanel {
 	
 	private int DEFAULT_NUM_SETS=5;
	private int DEFAULT_GAMES_PER_SET=100;
 	private int DEFAULT_TIME_BEGIN=60;
 	private int DEFAULT_TIME_BETWEEN=60;
 	private int DEFAULT_FREE_PARKING=-1;
 	private int DEFAULT_PORT=3232;
 		
 	private BufferedImage _whiteBack;
 	private final int BOTTOM_HEIGHT=Constants.FULL_HEIGHT*3/5;
 	private final int BOTTOM_WIDTH=Constants.FULL_WIDTH*15/16;
 	private final int PLAYER_WIDTH=100;
 	private JPanel _extraPanel, _fpPanel;
 	private JTextField _port, _timeBegin, _timeBetween, _numSets, _numGames, _freeParkingJackpot;
 	private JLabel _portLabel,_winCondLabel, _timeBeginLabel, _timeBetweenLabel, _numSetsLabel, _numGamesLabel, _gameFlowLabel, _extraLabel, _fpLabel;
 	private JRadioButton _mostMoney, _mostSets, _lastMatch, _chooseTogether, _rotateChoosing;
 	private JCheckBox _freeParking, _auctions, _doubleOnGo;
 	private ButtonGroup _winCond, _gameFlow;
 	private ImagePanel _p1h,_p1c,_p2h,_p2c,_p3h,_p3c,_p3x,_p4h,_p4c,_p4x;
 	private Integer _p2Curr, _p3Curr, _p4Curr; //0 is human, 1 is computer, 2 is none
 	private Font _headerFont, _sideFont;
 	public CreateBottomPanel() {
 		super();
 		try {
 			_whiteBack = Helper.resize(ImageIO.read(new File("images/WhiteBack.png")), BOTTOM_WIDTH, BOTTOM_HEIGHT);
 			this.setLayout(new BorderLayout());
 			setBackground(Constants.CLEAR);
 			this.setSize(new Dimension(_whiteBack.getWidth(),_whiteBack.getHeight()));
 			this.setPreferredSize(new Dimension(_whiteBack.getWidth(),_whiteBack.getHeight()));
 			this.setBorder(new EmptyBorder(20,20,20,20));
 			JPanel lowerPanel = new JPanel(new GridLayout(2,3));
 			lowerPanel.setBackground(Color.WHITE);
 			
 			JPanel portPanel = new JPanel();
 			portPanel.setBackground(Color.WHITE);
 			Dimension portSize= new Dimension(BOTTOM_WIDTH,100);
 			portPanel.setSize(portSize);
 			portPanel.setPreferredSize(portSize);
 			_port = new JTextField(4);
 			_port.setDocument(new NumDocument(4));
 			_port.setSize(200,30);
 			_port.setFont(new Font("myFont",Font.PLAIN,40));
 			_port.setText(DEFAULT_PORT + "");
 
 			_portLabel= new JLabel("Port: ");
 			_portLabel.setFont(new Font("portLabelFont",Font.PLAIN,40));
 			_portLabel.setSize(40, 40);
 			portPanel.add(_portLabel);
 			portPanel.add(_port);
 			
 			_headerFont = new Font("sansserif", Font.BOLD, 14);
 			_sideFont = new Font("sansserif", Font.PLAIN, 14);
 			
 			JPanel winCondPanel = new JPanel(new GridLayout(0,1));
 			winCondPanel.setAlignmentX(LEFT_ALIGNMENT);
 			winCondPanel.setBackground(Color.WHITE);
 			_winCondLabel = new JLabel("Win Condition:");
 			_winCondLabel.setFont(_headerFont);
 			_mostMoney=new JRadioButton("Most Cummulative Wealth");
 			_mostSets=new JRadioButton("Most Sets Won", true);
 			_lastMatch=new JRadioButton("Win Last Set");
 			_mostMoney.setBackground(Color.WHITE);
 			_mostSets.setBackground(Color.WHITE);
 			_lastMatch.setBackground(Color.WHITE);
 			
 			_winCond = new ButtonGroup();
 			_winCond.add(_mostSets);
 			_winCond.add(_mostMoney);
 			_winCond.add(_lastMatch);
 			
 			winCondPanel.add(_winCondLabel);
 			winCondPanel.add(_mostMoney);
 			winCondPanel.add(_mostSets);
 			winCondPanel.add(_lastMatch);
 			
 			JPanel timePanel = new JPanel(null);
 			timePanel.setAlignmentX(LEFT_ALIGNMENT);
 			timePanel.setBackground(Color.WHITE);
 			_timeBeginLabel=new JLabel("Time At Beginning(sec): ");
 			_timeBetweenLabel=new JLabel("Time Between Sets(sec): ");
 			_timeBeginLabel.setFont(_sideFont);
 			_timeBetweenLabel.setFont(_sideFont);
 			_timeBegin = new JTextField(1);
 			_timeBetween= new JTextField(1);
 			
 			Dimension timeSize = new Dimension(40,30);
 			Dimension timeLabelSize = new Dimension(200,15);
 			_timeBeginLabel.setSize(timeLabelSize);
 			_timeBeginLabel.setPreferredSize(timeLabelSize);
 			_timeBegin.setSize(timeSize);
 			_timeBegin.setPreferredSize(timeSize);
 			_timeBegin.setLocation(180,0);
 			_timeBeginLabel.setLocation(0,8);
 			
 			_timeBetweenLabel.setSize(timeLabelSize);
 			_timeBetweenLabel.setPreferredSize(timeLabelSize);
 			_timeBetween.setSize(timeSize);
 			_timeBetween.setPreferredSize(timeSize);
 			_timeBetween.setLocation(180,35);
 			_timeBetweenLabel.setLocation(0,42);
 			
 			_timeBegin.setDocument(new NumDocument(4));
 			_timeBegin.setText(DEFAULT_TIME_BEGIN + "");
 			_timeBetween.setDocument(new NumDocument(4));
 			_timeBetween.setText(DEFAULT_TIME_BETWEEN + "");
 			
 			timePanel.add(_timeBeginLabel);
 			timePanel.add(_timeBegin);
 			timePanel.add(_timeBetweenLabel);
 			timePanel.add(_timeBetween);
 			
 			JPanel gameLengthPanel = new JPanel(null);
 			gameLengthPanel.setBackground(Color.WHITE);
 			_numSetsLabel=new JLabel("Number of Sets: ");
 			_numGamesLabel=new JLabel("Games per Set: ");
 			_numSetsLabel.setFont(_sideFont);
 			_numGamesLabel.setFont(_sideFont);
 			_numSets = new JTextField(4);
 			_numSets.setText("");
 			_numGames= new JTextField(4);
 			_numSets.setDocument(new NumDocument(3));
 			_numSets.setText(DEFAULT_NUM_SETS +"");
 			_numGames.setDocument(new NumDocument(5));
 			_numGames.setText(DEFAULT_GAMES_PER_SET + "");
 			
 			Dimension numSize = new Dimension(60,30);
 			Dimension numLabelSize = new Dimension(200,15);
 			_numSetsLabel.setSize(numLabelSize);
 			_numSetsLabel.setPreferredSize(numLabelSize);
 			_numSets.setSize(numSize);
 			_numSets.setPreferredSize(numSize);
 			_numSets.setLocation(115,0);
 			_numSetsLabel.setLocation(0,8);
 			
 			_numGamesLabel.setSize(numLabelSize);
 			_numGamesLabel.setPreferredSize(numLabelSize);
 			_numGames.setSize(numSize);
 			_numGames.setPreferredSize(numSize);
 			_numGames.setLocation(115,35);
 			_numGamesLabel.setLocation(0,42);
 			
 			gameLengthPanel.add(_numSetsLabel);
 			gameLengthPanel.add(_numSets);
 			gameLengthPanel.add(_numGamesLabel);
 			gameLengthPanel.add(_numGames);
 			
 			JPanel gameFlowPanel = new JPanel(new GridLayout(0,1));
 			gameFlowPanel.setAlignmentX(LEFT_ALIGNMENT);
 			gameFlowPanel.setBackground(Color.WHITE);
 			_gameFlowLabel = new JLabel("Game Flow:");
 			_gameFlowLabel.setFont(_headerFont);
 			_chooseTogether=new JRadioButton("Choose Heuristics Together", true);
 			_rotateChoosing=new JRadioButton("Rotate Choosing Heuristics");
 			_chooseTogether.setBackground(Color.WHITE);
 			_rotateChoosing.setBackground(Color.WHITE);
 			
 			_gameFlow = new ButtonGroup();
 			_gameFlow.add(_chooseTogether);
 			_gameFlow.add(_rotateChoosing);
 			
 			gameFlowPanel.add(_gameFlowLabel);
 			gameFlowPanel.add(_chooseTogether);
 			gameFlowPanel.add(_rotateChoosing);
 			
 			_extraPanel = new JPanel(new GridLayout(0,1));
 			_extraPanel.setBackground(Color.WHITE);
 			_extraLabel = new JLabel("In-Game Rules:");
 			_extraLabel.setFont(_headerFont);
 			_extraLabel.setBackground(Color.WHITE);
 			_fpPanel = new JPanel(new GridLayout(1,0));
 			_fpPanel.setBackground(Color.WHITE);
 			_freeParking = new JCheckBox("Free Parking", false);
 			_freeParking.setBackground(Color.WHITE);
 			_freeParking.addItemListener(new FreeParkingListener());
 			_fpLabel = new JLabel("Default Jackpot: ");
 			_fpLabel.setBackground(Color.WHITE);
 			_freeParkingJackpot = new JTextField(4);
 			_freeParkingJackpot.setDocument(new NumDocument(3));
 			_fpPanel.add(_fpLabel);
 			_fpPanel.add(_freeParkingJackpot);
 			_fpPanel.setBackground(Color.WHITE);
 			_auctions = new JCheckBox("Auctions", false);
 			_auctions.setBackground(Color.WHITE);
 			_doubleOnGo = new JCheckBox("$400 landing on Go", false);
 			_doubleOnGo.setBackground(Color.WHITE);
 			_extraPanel.add(_extraLabel);
 			_extraPanel.add(_auctions);
 			_extraPanel.add(_doubleOnGo);
 			_extraPanel.add(_freeParking);
 			_extraPanel.add(_fpPanel);
 			_fpPanel.setVisible(false);
 			
 			
 			
 			lowerPanel.add(winCondPanel);
 			lowerPanel.add(timePanel);
 			lowerPanel.add(gameLengthPanel);
 			lowerPanel.add(gameFlowPanel);
 			lowerPanel.add(_extraPanel);
 			
 			
 			JPanel playerChoicePanel = new JPanel( new GridLayout(1,0));
 			playerChoicePanel.setBackground(Color.WHITE);
 			JPanel p1Panel = new JPanel();
 			p1Panel.setLayout(null);
 			p1Panel.setBackground(Color.WHITE);
 			JLabel player1 = new JLabel("Player 1");
 			Dimension p1LabelSize = new Dimension(PLAYER_WIDTH, 15);
 			player1.setSize(p1LabelSize);
 			player1.setSize(p1LabelSize);
 			player1.setPreferredSize(p1LabelSize);
 			player1.setLocation(25,0);
 			_p1h = new ImagePanel(Helper.resize(ImageIO.read(new File("images/boy1.jpeg")), PLAYER_WIDTH, PLAYER_WIDTH));
 			_p1h.setLocation(0,20);
 			Dimension p1size = new Dimension(PLAYER_WIDTH, 30+PLAYER_WIDTH);
 
 			p1Panel.setSize(p1size);
 			p1Panel.setPreferredSize(p1size);
 			p1Panel.add(player1);
 			p1Panel.add(_p1h);
 			
 			JPanel p2Panel = new JPanel();
 			p2Panel.setLayout(null);
 			p2Panel.setBackground(Color.WHITE);
 			JLabel player2 = new JLabel("Player 2");
 			Dimension p2LabelSize = new Dimension(PLAYER_WIDTH, 15);
 			player2.setSize(p2LabelSize);
 			player2.setSize(p2LabelSize);
 			player2.setPreferredSize(p2LabelSize);
 			player2.setLocation(25,0);
 			_p2h = new ImagePanel(Helper.resize(ImageIO.read(new File("images/girl1.jpeg")), PLAYER_WIDTH, PLAYER_WIDTH));
 			_p2h.setLocation(0,20);
 			_p2c = new ImagePanel(Helper.resize(ImageIO.read(new File("images/computer.jpg")), PLAYER_WIDTH, PLAYER_WIDTH));
 			_p2c.setLocation(0,30);
 			Dimension p2size = new Dimension(PLAYER_WIDTH, 30+PLAYER_WIDTH);
 
 			p2Panel.setSize(p2size);
 			p2Panel.setPreferredSize(p2size);
 			p2Panel.add(player2);
 			p2Panel.add(_p2h);
 			p2Panel.add(_p2c);
 			_p2Curr=0;
 			p2Panel.addMouseListener(new PlayerListener(_p2Curr, _p2h,_p2c));
 			_p2c.setVisible(false);
 			
 			JPanel p3Panel = new JPanel();
 			p3Panel.setLayout(null);
 			p3Panel.setBackground(Color.WHITE);
 			JLabel player3 = new JLabel("Player 3");
 			Dimension p3LabelSize = new Dimension(PLAYER_WIDTH, 15);
 			player3.setSize(p3LabelSize);
 			player3.setSize(p3LabelSize);
 			player3.setPreferredSize(p3LabelSize);
 			player3.setLocation(25,0);
 			_p3h = new ImagePanel(Helper.resize(ImageIO.read(new File("images/boy2.jpeg")), PLAYER_WIDTH, PLAYER_WIDTH));
 			_p3h.setLocation(0,20);
 			_p3c = new ImagePanel(Helper.resize(ImageIO.read(new File("images/computer.jpg")), PLAYER_WIDTH, PLAYER_WIDTH));
 			_p3c.setLocation(0,30);
 			_p3x = new ImagePanel(Helper.resize(ImageIO.read(new File("images/back-x.jpg")), PLAYER_WIDTH, PLAYER_WIDTH));
 			_p3x.setLocation(0,30);
 			Dimension p3size = new Dimension(PLAYER_WIDTH, 30+PLAYER_WIDTH);
 
 			p3Panel.setSize(p3size);
 			p3Panel.setPreferredSize(p3size);
 			p3Panel.add(player3);
 			p3Panel.add(_p3h);
 			p3Panel.add(_p3c);
 			p3Panel.add(_p3x);
 			_p3Curr=0;
 			p3Panel.addMouseListener(new PlayerListener(_p3Curr, _p3h,_p3c, _p3x));
 			_p3c.setVisible(false);
 			_p3x.setVisible(false);
 			
 			JPanel p4Panel = new JPanel();
 			p4Panel.setLayout(null);
 			p4Panel.setBackground(Color.WHITE);
 			JLabel player4 = new JLabel("Player 4");
 			Dimension p4LabelSize = new Dimension(PLAYER_WIDTH, 15);
 			player4.setSize(p4LabelSize);
 			player4.setSize(p4LabelSize);
 			player4.setPreferredSize(p4LabelSize);
 			player4.setLocation(25,0);
 			_p4h = new ImagePanel(Helper.resize(ImageIO.read(new File("images/girl2.jpeg")), PLAYER_WIDTH, PLAYER_WIDTH));
 			_p4h.setLocation(0,20);
 			_p4c = new ImagePanel(Helper.resize(ImageIO.read(new File("images/computer.jpg")), PLAYER_WIDTH, PLAYER_WIDTH));
 			_p4c.setLocation(0,30);
 			_p4x = new ImagePanel(Helper.resize(ImageIO.read(new File("images/back-x.jpg")), PLAYER_WIDTH, PLAYER_WIDTH));
 			_p4x.setLocation(0,30);
 			Dimension p4size = new Dimension(PLAYER_WIDTH, 30+PLAYER_WIDTH);
 
 			p4Panel.setSize(p4size);
 			p4Panel.setPreferredSize(p4size);
 			p4Panel.add(player4);
 			p4Panel.add(_p4h);
 			p4Panel.add(_p4c);
 			p4Panel.add(_p4x);
 			_p4Curr=0;
 			p4Panel.addMouseListener(new PlayerListener(_p4Curr, _p4h,_p4c, _p4x));
 			_p4c.setVisible(false);
 			_p4x.setVisible(false);
 			
 			
 			playerChoicePanel.add(p1Panel);
 			playerChoicePanel.add(p2Panel);
 			playerChoicePanel.add(p3Panel);
 			playerChoicePanel.add(p4Panel);
 			
 			add(portPanel, BorderLayout.NORTH);
 			add(lowerPanel, BorderLayout.CENTER);
 			add(playerChoicePanel, BorderLayout.SOUTH);
 
 
 		} catch (IOException e) {
 			System.out.println("ERROR: "+e.getMessage());
 			System.exit(1);
 		}
 	}
 	
 	public Settings getSettings(){
 		WinningCondition winCond;
 		if(_mostMoney.isSelected()){
 			winCond = WinningCondition.MOST_MONEY_MIDWAY;
 		} else if(_mostSets.isSelected()) {
 			winCond = WinningCondition.MOST_SETS_WON;
 		} else {
 			winCond = WinningCondition.LAST_SET_WON;
 		}
 		
 		Turns turnFlow;
 		if(_chooseTogether.isSelected()){
 			turnFlow = Turns.BUNCHED;
 		} else {
 			turnFlow = Turns.STAGGERED;
 		}
 		
 		int beginningTimeout = _timeBegin.getText().equals("") ? DEFAULT_TIME_BEGIN : Integer.parseInt(_timeBegin.getText());
 		int betweenTimeout = _timeBetween.getText().equals("") ? DEFAULT_TIME_BETWEEN : Integer.parseInt(_timeBetween.getText());
 		int numSets = _numSets.getText().equals("") ? DEFAULT_NUM_SETS : Integer.parseInt(_numSets.getText());
 		int gamesPerRound = _numGames.getText().equals("") ? DEFAULT_GAMES_PER_SET : Integer.parseInt(_numGames.getText());
 		int freeParking = _freeParkingJackpot.getText().equals("") ? DEFAULT_FREE_PARKING : Integer.parseInt(_freeParkingJackpot.getText());
 		boolean doubleOnGo = _doubleOnGo.isSelected();
 		boolean auctions = _auctions.isSelected();
 		
 		return new Settings(
 				gamesPerRound,numSets, doubleOnGo, freeParking, auctions,
 				turnFlow, winCond, beginningTimeout, betweenTimeout);
 	}
 	
 	public int getPort(){
 		return _port.getText().equals("") ? DEFAULT_PORT : Integer.parseInt(_port.getText());
 	}
 	
 	/**
 	 * 0 = human, 1 = computer, 2 = none
 	 * ... this is not very robust though
 	 * @return
 	 */
 	public Integer[] getPlayers(){
 		return new Integer[]{0,_p2Curr,_p3Curr,_p4Curr};
 	}
 	
 	//@Override
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		Graphics2D brush = (Graphics2D) g;
 		brush.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		brush.drawImage(_whiteBack, 0, 0, null);
 
 	}
 
 	/**
 	 * This class makes sure that the port can only take in numbers of length 4 or less
 	 * @author jschvime
 	 *
 	 */
 	private class NumDocument extends PlainDocument{
 		private int _maxLen;
 		public NumDocument(int maxLen) {
 			_maxLen=maxLen;
 		}
 		
 		@Override  
 		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException  {  
 			if (!isInteger(str)) {
 				return;
 			}
 			if (this.getLength() < _maxLen)  {  
 				int newLength = this.getLength() + str.length();  
 				if (newLength> _maxLen)  
 				{  
 					str = str.substring(0, _maxLen - this.getLength());  
 				}  
 				super.insertString(offs, str, a);  
 			}  
 		}  
 		
 		/**
 		 * checks if string is an integer
 		 * @param str
 		 * @return if its an integer
 		 */
 		private boolean isInteger(String str) {
 			try{
 				int num = Integer.parseInt(str);
 			} catch (NumberFormatException e) {
 				return false;
 			}
 			return true;
 		}
 	}
 	
 	/**
 	 * class that gets called when the checkbox changes
 	 * @author jschvime
 	 *
 	 */
 	private class FreeParkingListener implements ItemListener{
 
 		@Override
 		public void itemStateChanged(ItemEvent e) {
 			if(e.getStateChange()==ItemEvent.SELECTED) {
 				_fpPanel.setVisible(true);
 			}
 			else {
 				_fpPanel.setVisible(false);
 			}
 
 			
 		}
 		
 	}
 	
 	private class PlayerListener extends MouseInputAdapter{
 		List<ImagePanel> _imgs = new ArrayList<>();
 		Integer _curr;
 		public PlayerListener(Integer curr, ImagePanel ...imgs) {
 			super();
 			_imgs=Arrays.asList(imgs);
 			_curr=curr;
 		}
 
 		@Override
 		public void mousePressed(MouseEvent e) {
 			int newCurr=(_curr+1)%_imgs.size();
 			_imgs.get(newCurr).setVisible(true);
 			_imgs.get(_curr).setVisible(false);
 			_curr=newCurr;
 		}
 		
 	}
 }
