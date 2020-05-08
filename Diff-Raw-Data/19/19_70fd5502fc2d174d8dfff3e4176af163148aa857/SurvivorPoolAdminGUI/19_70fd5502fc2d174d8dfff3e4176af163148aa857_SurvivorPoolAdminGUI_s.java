 import java.awt.*;
 import java.awt.event.*;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Random;
 
 import javax.swing.*;
 import java.util.Iterator;
 import java.util.Vector;
 
 /**
  * The administrative interface GUI - the administrator
  * add/edits/modifies/creates the backend functionality of the Blackberry
  * Survivor Game. This class creates the graphical frontend of the
  * administrative side. 2 Skins have been partially implemented
  * 
  * @author Manor Freeman, Hazel Rivera, Martin Grabarczyk, Liam Corrigan, Jeff
  *         Westaway, Delerina Hill V 1.0 03/01/12
  */
 
 public class SurvivorPoolAdminGUI extends JFrame implements ActionListener {
 	/****************************** Attributes ***************************************/
 	// The default measurements for the main window
 	private static final int WIDTH = 1024;
 	private static final int HEIGHT = 768;
 	final static int GAP = 20;
 
 	// Images
 	// private ImageIcon survivorLogoImg =
 	// createImageIcon("images/survivorLogo.png");
 	private ImageIcon title, quitImg, mainMenuBtnImg;
	private ImageIcon goldBackground = createImageIcon("images/ruins.jpg"),
			jungleBackground;
	private ImageIcon playerBtnImg, playersJungleImg,
	playersGoldImg = createImageIcon("images/bbG.png"), contestantImg,
	stadingsImg, bonusQImg, themeSelectImg;
	private ImageIcon playerJBg, playerGBg, contestantJBg, contestantGBg,
			standingGBg, standingJBg, bqGBg, bqJBg, blankGFrame, blankJFrame;
	standingGBg, standingJBg, bqGBg, bqJBg, blankGFrame, blankJFrame;
 	private ImageIcon uploadedImage;
 
 	// ///////// Attributes for the startGame Dialog box
 	// The Buttons
 	private JButton resetGameBtn, startGameBtn, saveSettingBtn,
 			playerModButton;
	playerModButton;
 	private JRadioButton eliminateYBtn;
 
 	// Labels to ID fields
 	private JLabel contLabel, playLabel, wagerLabel;
 	// Strings for labels
 	private String contString = "Number of Contestants: ";
 	private String playString = "Number of Players: ";
 	private String wagerString = "Amount Wagered: ";
 	// Fields for data entry
 	private JTextField contField;
 	private JFormattedTextField wagerField, roundField;
 	// Area that shows the number of players currently on record
 	private JTextArea playArea;
 	// Stores the number of contestants and total number of rounds
 	private int numConts;
 	private int numRounds;
 
 	// Buttons
 	private JButton quitBtn, mainMenuBtn, playersBtn, contestantsBtn,
 			standingsBtn, bonusQBtn, themeSelectBtn;
 	private JButton addPlayerBtn, addContBtn, updateBtn, deletePlayerBtn,
 			deleteContBtn, resetBtn, uploadBtn, contOptionsBtn, contListBtn;
 	private JButton[] gameSettingsButtons = new JButton[13];
 
 	// JLabels
 	private JLabel themeMaker = new JLabel(goldBackground), playerBg,
 			contestantBg, standingBg, bqBg, titleBanner, contestantPicFrame;
 
 	// For the standings Panel
 	private JLabel poolLabel, currWkLabel, numContLabel, recElimLabel;
 	private JTextArea poolArea, currWkArea, numContArea, recElimArea;
 
 	private String currWkString = "Current Week: ";
 
 	// JPanels
 	private JPanel mainPanel, quitPanel, mMenuBtnPanel, titlePanel,
 			mainButtonsPanel, pPanel, cPanel, cLPanel, sPanel, qPanel,
 			currWkPanel;
 
 	private TextInputFields textFields_p, textFields_c;
 
 	private PlayerListGUI standingsTable;
 	private ContestantListGUI contLiTable;
 	private BonusQuestionGUI bqPanel;
 	private Font gFont, jFont;
 
 	private String imagePath = null;
 
 	// contestant and player holder
 	private Player[] playersArray;
 	private Contestant[] contestantsArray;
 	private int contCount = 0;
 
 	// Variables for setters & getters
 	private boolean startGame = false;
 	private boolean contEliminated = false;
 	private int wager, roundNum=1, inputNumConts;
 	private Round[] rounds;
 
 	// STUFF YOU ADDED
 	private JComponent playPanel;
 
 	/******************************** Constructor *************************************/
 
 	/**
 	* Calculates the top three players
 	*/
 	public void checkWinners(){
 	int remainingRounds = numRounds - roundNum;
 	int moneyPool = wager*playersArray.length;
 	double firstMoney = moneyPool*0.6;
 	double secondMoney = moneyPool*0.3;
 	double thirdMoney = moneyPool*0.1;
 	Player first=playersArray[0];
 	Player second=playersArray[1];
 	Player third=playersArray[2];
 
 	for (int i = 0; i < playersArray.length;i++){
 	if (playersArray[i].getScore() > first.getScore()){
 	third = second;
 	second = first;
 	first = playersArray[i];
 	}
 	else if(playersArray[i].getScore() > second.getScore() && !playersArray[i].equals(first)){
 	third = second;
 	second = playersArray[i];
 	}
 	else if (playersArray[i].getScore() > third.getScore() && !playersArray[i].equals(first) && !playersArray[i].equals(second)){
 	third = playersArray[i];
 	}
 	}
 	if (roundNum > numRounds){
 	JOptionPane.showMessageDialog(this,"First Place: "+first.getFirst()+" "+first.getLast()+" wins $"+firstMoney+"\n"+"Second Place: "+second.getFirst()+" "+second.getLast()+" wins $"+secondMoney+"\n"+"Third Place: "+third.getFirst()+" "+third.getLast()+" wins $"+thirdMoney+"\n");
 	}
 	else{
 	JOptionPane.showMessageDialog(this,"Number of Rounds Remaining: "+remainingRounds);
 	}
 	}
 	
 	public boolean eliminateContestant(int contIndex, int roundNum) {
 		if (roundNum > numRounds) {
 			JOptionPane
 					.showMessageDialog(
 							this,
 							"The round that has been specified is after the final Round of the game. \n Elimination not saved.");
 		} else if (contIndex >= contCount) {
 			JOptionPane
 					.showMessageDialog(this,
 							"The specified contestant no longer exists. \n Elimination not saved.");
 		}
 		else {
 			if(this.rounds[roundNum-1].getContestantEliminated()!=null){
 				int eliminatedContIndex=findContestant(this.rounds[roundNum-1].getContestantEliminated().getID());
 				if(contestantsArray[eliminatedContIndex].getID().equals(contestantsArray[contIndex].getID())){
 					JOptionPane.showMessageDialog(this,
 							"The contestant "+ contestantsArray[eliminatedContIndex].getFirst()+" "+contestantsArray[eliminatedContIndex].getLast()+"was already eliminated in round "+roundNum);
 					return false;
 				}
 					
 				if(roundNum==this.numRounds){
 
 					 if(rounds[roundNum-1].getOtherContestantEliminated()!=null){
 
 						this.contestantsArray[eliminatedContIndex].setElimRound(null);
 						JOptionPane.showMessageDialog(this,
 								"The contestant elimination in round "+roundNum+", "+ contestantsArray[eliminatedContIndex].getFirst()+" "+contestantsArray[eliminatedContIndex].getLast()+" , has been overwritten ");
 						this.contestantsArray[contIndex].setElimRound(rounds[roundNum-1]);
 						this.rounds[roundNum-1].setOtherContestantEliminated(this.contestantsArray[contIndex]);
 						return true;
 						}
 					
 					else{
 						this.contestantsArray[contIndex].setElimRound(rounds[roundNum-1]);
 						this.rounds[roundNum-1].setOtherContestantEliminated(this.contestantsArray[contIndex]);
 						return true;
 					}
 				}
 				else if((boolean)(eliminatedContIndex!=-1)){
 					this.contestantsArray[eliminatedContIndex].setElimRound(null);
 					JOptionPane.showMessageDialog(this,
 							"The contestant elimination in round "+roundNum+", "+ contestantsArray[eliminatedContIndex].getFirst()+" "+contestantsArray[eliminatedContIndex].getLast()+" , has been overwritten ");
 					this.rounds[roundNum - 1]
 							.setContestantEliminated(this.contestantsArray[contIndex]);
 					contestantsArray[contIndex].setElimRound(new Round(roundNum));
 					return true;
 				}
 			}
 			else{
 				this.rounds[roundNum - 1]
 						.setContestantEliminated(this.contestantsArray[contIndex]);
 				contestantsArray[contIndex].setElimRound(new Round(roundNum));
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/** Initializes the Administrative GUI */
 	public SurvivorPoolAdminGUI() {
 		createGame();
 	}
 
 	/**
 	 * Initializes default theme components, images, font type & panels, & sets
 	 * them to the main frame As well as setting the main frame
 	 */
 	private void createGame() {
 		// initialize players and contestants array
 		String fileName = "players.txt";
 		readPlayers(fileName);
 		readContestants("contestants.txt");
 		readSettings("settings.txt");
 		if (inputNumConts != 0) {
 			readRounds("rounds.txt");
 		}
 
 		textFields_p = new TextInputFields(this.contestantsArray,
 				this.contCount, this.playersArray);
 		textFields_c = new TextInputFields(this.contestantsArray,
 				this.contCount, this.playersArray);
 		standingsTable = new PlayerListGUI(playersArray, roundNum);
 		// contLiTable = new ContestantListGUI(contCount,
 		// this.contestantsArray);
 		contLiTable = new ContestantListGUI(contestantsArray, contCount);
 		bqPanel = new BonusQuestionGUI(rounds, roundNum);
 		// Font for the Golden Ruin Theme
 		gFont = new Font("Pescadero", Font.PLAIN, 18);
 		// The jungle theme font
 		jFont = new Font("Viner Hand ITC", Font.PLAIN, 18);
 
 		// Call all set the backgrounds for each panel
 		playerGBg = createImageIcon("images/playerGoldenBg.jpg");
 		playerBg = new JLabel(playerGBg);
 
 		contestantGBg = createImageIcon("images/contestantGoldenBg.jpg");
 		contestantBg = new JLabel(contestantGBg);
 
 		blankGFrame = createImageIcon("images/uploadPicFrame_goldFrame.png");
 		contestantPicFrame = new JLabel(blankGFrame);
 
 		standingGBg = createImageIcon("images/standingsGolden.jpg");
 		standingBg = new JLabel(standingGBg);
 
 		bqGBg = createImageIcon("images/bqGolden.jpg");
 		bqBg = new JLabel(bqGBg);
 
 		SpringLayout guiLayout = new SpringLayout();
 		this.setLayout(guiLayout);
 
 		this.setJMenuBar(menuBar());
 
 		getContentPane().add(quitButton());
 		getContentPane().add(mainScreen());
 
 		guiLayout.putConstraint(SpringLayout.WEST, quitPanel, 40,
 				SpringLayout.WEST, getContentPane());
 		guiLayout.putConstraint(SpringLayout.NORTH, quitPanel, 40,
 				SpringLayout.NORTH, getContentPane());
 
 		guiLayout.putConstraint(SpringLayout.WEST, mainPanel, 0,
 				SpringLayout.WEST, getContentPane());
 		guiLayout.putConstraint(SpringLayout.NORTH, mainPanel, 0,
 				SpringLayout.NORTH, getContentPane());
 
 		goldenRuinsTheme(); // Set the default theme
 
 		// Set up methods for the frame
 		this.setSize(WIDTH, HEIGHT);
 		this.setLocationRelativeTo(null);
 		this.setTitle("UWOSurvivorPoolAdmin");
 		this.setVisible(true);
 		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
 	}// End of SurvivorPoolAdminGUI Constructor
 
 	/****************************** Methods *********************************/
 
 
 
 	public int findContestantWinner(){
 		if(roundNum>numRounds){
 			for(int i=0;i<contCount;i++){
 				if(this.contestantsArray[i].getElimRound()==null)
 					return i;
 			}
 
 		}
 		return -1;
 	}
 	public String assignRandomCont(int index, int roundNum) {
 		if (this.playersArray[index] == null)
 			return null;
 		else {
 			Random numGenerator = new Random();
 			int contIndex = numGenerator.nextInt(this.contCount);
 			this.playersArray[index].makeRoundPick(roundNum,
 					contestantsArray[contIndex]);
 			String s = "" + this.playersArray[index].getFirst() + " "
 					+ this.playersArray[index].getLast() + ": "
 					+ contestantsArray[contIndex].getFirst() + " "
 					+ contestantsArray[contIndex].getLast();
 			return s;
 		}
 
 	}
 
 	public Round getRound(int rndNum) {
 		if (numRounds == 0)
 			return null;
 		if (rndNum > numRounds)
 			return null;
 		else
 			return this.rounds[rndNum - 1];
 	}
 
 	public String assignRandomPicks(int roundNum) {
 		String s = "";
 		for (int i = 0; i < this.playersArray.length
 				&& this.playersArray[i] != null; i++) {
 			if (playersArray[i].getWeekPick(roundNum) == null)
 				s = s + assignRandomCont(i, roundNum) + "\n";
 		}
 		if (s.equals(""))
 			return null;
 		else
 			return s;
 	}
 
 	public void calcEliminationScore() {
 		if (startGame == false)
 			; // don't need to change peoples score
 		else if (roundNum > 1 && roundNum < numRounds + 2) {
 			for (int i = 0; i < playersArray.length; i++) {
 				playersArray[i].setScore(0);
 			}
 			String contElimID;
 			int max = roundNum - 1;
 			if (roundNum > numRounds) {
 				max = numRounds;
 				contElimID = contestantsArray[findContestantWinner()].getID();
 				for (int i = 0; i < playersArray.length; i++) {
 					if (playersArray[i].getFinal() != null
 							&& playersArray[i].getFinal().getContestant() != null) {
 						if (playersArray[i].getFinal().getContestant().getID()
 								.equals(contElimID)) {
 							playersArray[i].incScore(2 * (this.inputNumConts
 									- playersArray[i].getRoundOfFinal() + 1));
 
 						}
 					}
 				}
 			}
 			for (int i = 0; i < max; i++) {
 				contElimID = rounds[i].getContestantEliminated().getID();
 				for (int j = 0; j < playersArray.length; j++) {
 					if (playersArray[j].getWeekPick(i+1) != null) 
 						if (i == numRounds - 1) {
 							if (playersArray[j].getWeekPick(i+1) != null){
 								if (contestantsArray[findContestantWinner()].getID().equals(playersArray[j]
 										.getWeekPick(i + 1).getContestant().getID())) 
 									playersArray[j].incScore(40);
 							}
 
 						} else{
 							if (playersArray[j].getWeekPick(i+1) != null){
 								if (contElimID.equals(playersArray[j]
 										.getWeekPick(i + 1).getContestant().getID())) 
 									playersArray[j].incScore(20);
 							}
 						}
 
 
 				}
 			}
 		}
 	}
 
 	public void nextRound() {
 		if (startGame == false)
 			JOptionPane.showMessageDialog(this,
 					"You must start the game before going to the next round");
 		else if (this.rounds[roundNum - 1].getContestantEliminated() == null)
 			JOptionPane
 					.showMessageDialog(
 							this,
 					"You must eliminate a contestant before going to the next round. \n (Contestants >> Contestant Options >> Update Contestant)");
 		else if (this.roundNum==this.numRounds){
 			if(this.rounds[roundNum-1].getOtherContestantEliminated()==null)
 			JOptionPane
 			.showMessageDialog(
 					this,
 					"Two contestants must be eliminated before ending the final round. \n (Contestants >> Contestant Options >> Update Contestant)");
 			else{
 				roundNum++;
 				checkWinners();
 				writeSettings("settings.txt");
 				return;
 		}
 			}
 		else if(this.roundNum>this.numRounds){
 			checkWinners();
 			return;
 		}
 		else {
 			if (contCount == 0 || playersArray == null)
 				JOptionPane
 						.showMessageDialog(this,
 								"Please add players and contestants before changing to the next round");
 			else {
 				String s = assignRandomPicks(this.roundNum);
 				roundNum++;
 				calcEliminationScore();
 
 				if (s == null)
 					JOptionPane.showMessageDialog(this, "It is now round "
 							+ this.roundNum);
 				else {
 					JOptionPane
 							.showMessageDialog(
 									this,
 									"It is now round "
 											+ this.roundNum
 											+ "\n The following players were assigned random contestant choices: \n\n"
 											+ s);
 					calcEliminationScore();
 					writePlayers("players.txt");
 					writeSettings("settings.txt");
 				}
 			}
 		}
 	}
 
 	/**
 	 * Persistence for player fields - reads the player name and ID from the
 	 * file where all player records are stored.
 	 * 
 	 * @param fileName
 	 *            -- the pathname of the file that player records are stored to
 	 */
 	public void readPlayers(String fileName) {
 		try {
 			DataInputStream input = new DataInputStream(new FileInputStream(
 					fileName));
 			BufferedReader in = new BufferedReader(new InputStreamReader(input));
 			String currPlayer = in.readLine(), pFirst = "", pLast = "", pID = "", rndStr = "", finRndStr = "", pScrStr = "", cFirst = "", cLast = "", cID = "", cTrb = "", cPic = "";
 			Player currPlayerObj;
 			Contestant contElim;
 			int pScr, rnd, finRnd;
 			while (currPlayer != null && !currPlayer.trim().equals("")) {
 				int i = 0;
 				pFirst = pLast = pID = rndStr = finRndStr = pScrStr = cFirst = cLast = cID = cTrb = cPic = "";
 				pScr = rnd = finRnd = 0;
 				while (currPlayer.charAt(i) != '+' && i < currPlayer.length()) {
 					pFirst = pFirst + currPlayer.charAt(i);
 					i++;
 				}// end of first name field
 				i++;
 				while (currPlayer.charAt(i) != '+' && i < currPlayer.length()) {
 					pLast = pLast + currPlayer.charAt(i);
 					i++;
 				}// end of last name field
 				i++;
 				while (currPlayer.charAt(i) != '+' && i < currPlayer.length()) {
 					pID = pID + currPlayer.charAt(i);
 					i++;
 				}// end of first last name field
 				i++;
 				currPlayerObj = new Player(pFirst, pLast, pID);
 				for (; i < currPlayer.length(); i++) {
 					if (currPlayer.charAt(i) == '+') {
 						pScr = Integer.valueOf(pScrStr);
 						break;
 					} else {
 						pScrStr = pScrStr + currPlayer.charAt(i);
 					}
 				}
 				i++;
 				currPlayerObj.setScore(pScr);
 
 				if (currPlayer.charAt(i) == '_')
 					i = i + 2;
 				else {
 					String numBQAnswersStr = "", roundBQStr = "", answer = "", qstn = "", numQstnAnsStr = "", qstAns = "", qstCorAns = "";
 					int numBQAnswers = 0, roundBQ = 0, numQstAns = 0;
 					for (; i < currPlayer.length(); i++) {
 						if (currPlayer.charAt(i) == '{') {
 							numBQAnswers = Integer.valueOf(numBQAnswersStr);
 							break;
 						} else {
 							numBQAnswersStr = numBQAnswersStr
 									+ currPlayer.charAt(i);
 						}
 					}
 					i++;
 					for (int index = 0; index < numBQAnswers; index++) {
 						for (; i < currPlayer.length(); i++) {
 							if (currPlayer.charAt(i) == '{') {
 								roundBQ = Integer.valueOf(roundBQStr);
 								break;
 							} else {
 								roundBQStr = roundBQStr + currPlayer.charAt(i);
 							}
 						}
 						i++;
 						while (currPlayer.charAt(i) != '{'
 								&& i < currPlayer.length()) {
 							answer = answer + currPlayer.charAt(i);
 							i++;
 						}// end of first last name field
 						i++;
 						while (currPlayer.charAt(i) != '{'
 								&& i < currPlayer.length()) {
 							qstn = qstn + currPlayer.charAt(i);
 							i++;
 						}// end of first last name field
 						i++;
 						for (; i < currPlayer.length(); i++) {
 							if (currPlayer.charAt(i) == '{') {
 								numQstAns = Integer.valueOf(numQstnAnsStr);
 								break;
 							} else {
 								numQstnAnsStr = numQstnAnsStr
 										+ currPlayer.charAt(i);
 							}
 						}
 						i++;
 						String[] qstAnswers = new String[numQstAns];
 						for (int k = 0; k < numQstAns; k++) {
 							while (currPlayer.charAt(i) != '{'
 									&& i < currPlayer.length()) {
 								qstAns = qstAns + currPlayer.charAt(i);
 								i++;
 							}// end of first last name field
 							i++;
 							qstAnswers[k] = qstAns;
 							qstAns = "";
 						}
 						while (currPlayer.charAt(i) != '{'
 								&& i < currPlayer.length()) {
 							qstCorAns = qstCorAns + currPlayer.charAt(i);
 							i++;
 						}// end of first last name field
 						i++;
 						currPlayerObj.answerQuestion(new BonusQuestion(qstn,
 								qstAnswers, qstCorAns), answer, roundBQ);
 					}
 
 				}
 				if (currPlayer.charAt(i) == '_')
 					i++;
 				else {
 					for (; i < currPlayer.length(); i++) {
 						if (currPlayer.charAt(i) == '{') {
 							finRnd = Integer.valueOf(finRndStr);
 							break;
 						} else {
 							finRndStr = finRndStr + currPlayer.charAt(i);
 						}
 					}
 					i++;
 					for (; i < currPlayer.length(); i++) {
 						if (currPlayer.charAt(i) == '{') {
 							rnd = Integer.valueOf(rndStr);
 							break;
 						} else {
 							rndStr = rndStr + currPlayer.charAt(i);
 						}
 					}
 					i++;
 					while (i < currPlayer.length()
 							&& currPlayer.charAt(i) != '{') {
 						cFirst = cFirst + currPlayer.charAt(i);
 						i++;
 					}
 					i++;
 					while (i < currPlayer.length()
 							&& currPlayer.charAt(i) != '{') {
 						cLast = cLast + currPlayer.charAt(i);
 						i++;
 					}
 					i++;
 					while (i < currPlayer.length()
 							&& currPlayer.charAt(i) != '{') {
 						cID = cID + currPlayer.charAt(i);
 						i++;
 					}
 					i++;
 					while (i < currPlayer.length()
 							&& currPlayer.charAt(i) != '{') {
 						cPic = cPic + currPlayer.charAt(i);
 						i++;
 					}
 					i++;
 					while (i < currPlayer.length()
 							&& currPlayer.charAt(i) != '{') {
 						cTrb = cTrb + currPlayer.charAt(i);
 						i++;
 					}
 					contElim = new Contestant(cFirst, cLast, cID, cTrb, cPic);
 					currPlayerObj.chooseWinner(contElim, finRnd, rnd + 2);
 				}
 				i++;
 				cFirst = cLast = cID = cTrb = cPic = rndStr = "";
 				finRnd = rnd = 0;
 				for (; i < currPlayer.length();) {
 					if (currPlayer.charAt(i) == '_'
 							|| currPlayer.charAt(i) == '{')
 						i++;
 					else {
 						cFirst = cLast = cID = cTrb = cPic = rndStr = "";
 						rnd = 0;
 						for (; i < currPlayer.length(); i++) {
 							if (currPlayer.charAt(i) == '{') {
 								
 								rnd = Integer.valueOf(rndStr);
 
 								break;
 							} else {
 								rndStr = rndStr + currPlayer.charAt(i);
 							}
 						}
 						i++;
 						while (i < currPlayer.length()
 								&& currPlayer.charAt(i) != '{') {
 							cFirst = cFirst + currPlayer.charAt(i);
 							i++;
 						}
 						i++;
 						while (i < currPlayer.length()
 								&& currPlayer.charAt(i) != '{') {
 							cLast = cLast + currPlayer.charAt(i);
 							i++;
 						}
 						i++;
 						while (i < currPlayer.length()
 								&& currPlayer.charAt(i) != '{') {
 							cID = cID + currPlayer.charAt(i);
 							i++;
 						}
 						i++;
 						while (i < currPlayer.length()
 								&& currPlayer.charAt(i) != '{') {
 							cPic = cPic + currPlayer.charAt(i);
 							i++;
 						}
 						i++;
 						while (i < currPlayer.length()
 								&& currPlayer.charAt(i) != '{') {
 							cTrb = cTrb + currPlayer.charAt(i);
 							i++;
 						}
 						i++;
 						contElim = new Contestant(cFirst, cLast, cID, cTrb,
 								cPic);
 						currPlayerObj.makeRoundPick(rnd, contElim);
 					}
 				}
 				addPlayer(currPlayerObj);
 				currPlayer = in.readLine();
 
 			}
 		} catch (IOException e) {
 		}// unharmful
 	}
 
 	/**
 	 * Persistence for player fields -- writes player fields data to file
 	 * 
 	 * @param fileName
 	 *            -- the pathname of the file where player records are stored.
 	 */
 	public void writePlayers(String fileName) {
 		BufferedWriter bWr = null;
 		String currString = "";
 		try {
 
 			bWr = new BufferedWriter(new FileWriter(fileName));
 			for (int i = 0; i < this.playersArray.length; i++) {
 				
 				currString = currString + this.playersArray[i].getFirst() + "+";
 				currString = currString + this.playersArray[i].getLast() + "+";
 				currString = currString + this.playersArray[i].getID() + "+";
 				currString = currString + this.playersArray[i].getScore() + "+";
 				if (this.playersArray[i].getAnswers() == null)
 					currString = currString + "_{";
 				else {
 					Vector<BQAnswer> answersHolder = this.playersArray[i]
 							.getAnswers();
 					if (answersHolder.isEmpty())
 						currString = currString + "_{";
 					else {
 						int numAnswers = answersHolder.size();
 						currString = currString + numAnswers + "{";
 						Iterator<BQAnswer> answers = answersHolder.iterator();
 						for (int j = 0; i < numAnswers; i++) {
 							BQAnswer curAnswer = answers.next();
 							currString = currString + curAnswer.getRound()
 									+ "{";
 							currString = currString + curAnswer.getAnswer()
 									+ "{";
 							BonusQuestion question = curAnswer.getQuestion();
 							currString = currString + question.getQuestion()
 									+ "{";
 							String[] answersAr = question.getAnswers();
 							currString = currString + answersAr.length + "{";
 							for (int k = 0; k < answersAr.length; k++) {
 								currString = currString + answersAr[k] + "{";
 							}
 
 							currString = currString
 									+ question.getCorrectAnswer() + "{";
 
 						}
 					}
 
 				}
 
 				if (this.playersArray[i].getFinal() == null)
 					currString = currString + "_{";
 				else {
 					currString = currString
 							+ this.playersArray[i].getRoundOfFinal() + "{";
 					currString = currString
 							+ this.playersArray[i].getFinal().getRound() + "{";
 					currString = currString
 							+ this.playersArray[i].getFinal().getContestant()
 									.getFirst() + "{";
 					currString = currString
 							+ this.playersArray[i].getFinal().getContestant()
 									.getLast() + "{";
 					currString = currString
 							+ this.playersArray[i].getFinal().getContestant()
 									.getID() + "{";
 					currString = currString
 							+ this.playersArray[i].getFinal().getContestant()
 									.getPicture() + "{";
 					currString = currString
 							+ this.playersArray[i].getFinal().getContestant()
 									.getTribe() + "{";
 				}
 				if (this.playersArray[i].getAllWeekPicks() == null) {
 					;
 				} else {
 					RoundPick[] weekPicks = this.playersArray[i]
 							.getAllWeekPicks();
 					int max = weekPicks.length;
 
 					for (int j = 0; j < max; j++) {
 						if (weekPicks[j] == null)
 							;
 						else {
 							currString = currString + weekPicks[j].getRound()
 									+ "{";
 							currString = currString
 									+ weekPicks[j].getContestant().getFirst()
 									+ "{";
 							currString = currString
 									+ weekPicks[j].getContestant().getLast()
 									+ "{";
 							currString = currString
 									+ weekPicks[j].getContestant().getID()
 									+ "{";
 							currString = currString
 									+ weekPicks[j].getContestant().getPicture()
 									+ "{";
 							currString = currString
 									+ weekPicks[j].getContestant().getTribe()
 									+ "{";
 						}
 					}
 				}
 				currString = currString + "\n";
 
 			}
 			// Start writing to the output stream
 			bWr.write(currString);
 			bWr.flush();
 			bWr.close();
 		} catch (IOException e) {
 		}// unharmful
 
 	}
 
 	public void readRounds(String fileName) {
 		try {
 			DataInputStream input = new DataInputStream(new FileInputStream(
 					fileName));
 			BufferedReader in = new BufferedReader(new InputStreamReader(input));
 			String currRound = in.readLine();
 			String[] ans = new String[1];
 			String cFirst = "", cLast = "", cID = "", cPic = "", cTrb = "", bQuest = "", bAns = "", bCorAns = "", rndStr = "", numAnsStr = "", numBonusStr = "";
 			int rnd = 0, numAns = 0, numBonus = 0;
 			this.rounds = new Round[inputNumConts - 2];
 			for (int j = 0; currRound != null && currRound.trim() != ""; j++) {
 				int i = 0;
 				cFirst = cLast = cID = cPic = cTrb = bQuest = bAns = rndStr = numAnsStr = bCorAns = numBonusStr = "";
 				rnd = numAns = numBonus = 0;
 				for (; i < currRound.length(); i++) {
 					if (currRound.charAt(i) == '+') {
 						rnd = Integer.valueOf(rndStr);
 						rounds[j] = new Round(rnd);
 						break;
 					} else {
 						rndStr = rndStr + currRound.charAt(i);
 					}
 				}
 				i++;
 				if (currRound.charAt(i) == '_')
 					i++;
 				else {
 					cFirst = cLast = cID = cPic = cTrb = "";
 					while (i < currRound.length() && currRound.charAt(i) != '{') {
 						cFirst = cFirst + currRound.charAt(i);
 						i++;
 					}
 					i++;
 					while (i < currRound.length() && currRound.charAt(i) != '{') {
 						cLast = cLast + currRound.charAt(i);
 						i++;
 					}
 					i++;
 					while (i < currRound.length() && currRound.charAt(i) != '{') {
 						cID = cID + currRound.charAt(i);
 						i++;
 					}
 					i++;
 					while (i < currRound.length() && currRound.charAt(i) != '{') {
 						cPic = cPic + currRound.charAt(i);
 						i++;
 					}
 					i++;
 					while (i < currRound.length() && currRound.charAt(i) != '{') {
 						cTrb = cTrb + currRound.charAt(i);
 						i++;
 					}
 					this.rounds[j].setContestantEliminated(new Contestant(
 							cFirst, cLast, cID, cTrb, cPic));
 				}
 				i++;
 				if (currRound.charAt(i) == '_')
 					i++;
 				else {
 					bQuest = bAns = rndStr = numAnsStr = bCorAns = numBonusStr = "";
 					numAns = numBonus = 0;
 					for (; i < currRound.length(); i++) {
 						if (currRound.charAt(i) == '{') {
 							numBonus = Integer.valueOf(numBonusStr);
 							break;
 						} else {
 							numBonusStr = numBonusStr + currRound.charAt(i);
 						}
 					}
 					i++;
 					for (int k = 0; k < numBonus && i < currRound.length(); k++) {
 						if (currRound.charAt(i) == '_')
 							i = i + 2;
 						else {
 							while (i < currRound.length()
 									&& currRound.charAt(i) != '{') {
 								bQuest = bQuest + currRound.charAt(i);
 								i++;
 							}
 						}
 						i++;
 						if (currRound.charAt(i) == '_')
 							i = i + 2;
 						else {
 							for (; i < currRound.length(); i++) {
 								if (currRound.charAt(i) == '{') {
 									numAns = Integer.valueOf(numAnsStr);
 									ans = new String[numAns];
 									break;
 								} else {
 									numAnsStr = numAnsStr + currRound.charAt(i);
 								}
 							}
 							i++;
 							for (int l = 0; l < numAns
 									&& i < currRound.length(); l++) {
 								while (i < currRound.length()
 										&& currRound.charAt(i) != '{') {
 									bAns = bAns + currRound.charAt(i);
 									i++;
 								}
 								i++;
 								ans[l] = bAns;
 								bAns = "";
 							}
 
 						}
 						if (currRound.charAt(i) == '_')
 							i = i + 2;
 						else {
 							while (i < currRound.length()
 									&& currRound.charAt(i) != '{') {
 								bCorAns = bCorAns + currRound.charAt(i);
 								i++;
 							}
 							i++;
 						}
 						BonusQuestion bQuestion = new BonusQuestion(bQuest,
 								ans, bCorAns);
 						this.rounds[j].addBonusQuestion(bQuestion);
 					}
 				}
 				currRound = in.readLine();
 			}
 		} catch (IOException e) {
 		}
 	}
 
 	public void writeRounds(String fileName) {
 		BufferedWriter bWr = null;
 		String currString = "";
 		try {
 
 			bWr = new BufferedWriter(new FileWriter(fileName));
 			for (int i = 0; i < this.rounds.length; i++) {
 				currString = currString + this.rounds[i].getRoundNum() + "+";
 				if (rounds[i].getContestantEliminated() == null) {
 					currString = currString + "_{";
 				} else {
 					currString = currString
 							+ this.rounds[i].getContestantEliminated()
 									.getFirst() + "{";
 					currString = currString
 							+ this.rounds[i].getContestantEliminated()
 									.getLast() + "{";
 					currString = currString
 							+ this.rounds[i].getContestantEliminated().getID()
 							+ "{";
 					currString = currString
 							+ this.rounds[i].getContestantEliminated()
 									.getPicture() + "{";
 					currString = currString
 							+ this.rounds[i].getContestantEliminated()
 									.getTribe() + "{";
 				}
 				BonusQuestion[] bonus = this.rounds[i].getBonusQuestion();
 				if (bonus == null)
 					currString = currString + "_{";
 				else {
 					currString = currString + bonus.length + "{";
 					for (int j = 0; j < bonus.length; j++) {
 						if (bonus[j].getQuestion() == null)
 							currString = currString + "_{";
 						else
 							currString = currString + bonus[j].getQuestion()
 									+ "{";
 						if (bonus[j].getAnswers() == null)
 							currString = currString + "_{";
 						else {
 							String[] answers = bonus[j].getAnswers();
 							currString = currString + answers.length + "{";
 							for (int k = 0; k < answers.length; k++) {
 								currString = currString + answers[k] + "{";
 							}
 						}
 						if (bonus[j].getCorrectAnswer() == null)
 							currString = currString + "_{";
 						else {
 							currString = currString
 									+ bonus[j].getCorrectAnswer();
 						}
 
 					}
 				}
 				currString = currString + "\n";
 
 			}
 			bWr.write(currString);
 			bWr.flush();
 			bWr.close();
 		} catch (IOException e) {
 		} // won't happen
 	}
 
 	public void readSettings(String fileName) {
 		try {
 			DataInputStream input = new DataInputStream(new FileInputStream(
 					fileName));
 			BufferedReader in = new BufferedReader(new InputStreamReader(input));
 			String currRound = in.readLine();
 			if (currRound != null && !currRound.trim().equals("")) {
 				this.roundNum = Integer.valueOf(currRound.trim());
 
 				String gameStarted = in.readLine(), inputWager;
 				if (gameStarted != null && !gameStarted.trim().equals("")) {
 					if (gameStarted.equals("true")) {
 						this.startGame = true;
 					} else
 						this.startGame = false;
 					String inputSpecifier = in.readLine();
 					if (inputSpecifier != null) {
 						if (inputSpecifier.equals("R")) {
 							String inputNumRounds = in.readLine();
 							if (inputNumRounds != null) {
 								this.numRounds = Integer
 										.valueOf(inputNumRounds);
 
 								String inNumContestants = in.readLine();
 								if (inNumContestants != null) {
 									this.inputNumConts = Integer
 											.valueOf(inNumContestants);
 									this.rounds = new Round[inputNumConts - 2];
 									for (int i = 0; i < inputNumConts - 2; i++) {
 										this.rounds[i] = new Round(i + 1);
 									}
 									inputSpecifier = in.readLine();
 									if (inputSpecifier != null) {
 										if (inputSpecifier.equals("W")) {
 											inputWager = in.readLine();
 											if (inputWager != null)
 												this.wager = Integer
 														.valueOf(inputWager);
 										}
 									}
 								}
 							}
 						} else if (inputSpecifier.equals("W")) {
 							inputWager = in.readLine();
 							if (inputWager != null)
 								this.wager = Integer.valueOf(inputWager);
 						}
 					}
 				}
 			}
 		} catch (IOException e) {
 		}
 	}
 
 	public void writeSettings(String fileName) {
 		BufferedWriter bWr = null;
 		try {
 
 			bWr = new BufferedWriter(new FileWriter(fileName));
 			String settingsStr = "" + this.roundNum + "\n" + this.startGame
 					+ "\n";
 			if (this.inputNumConts != 0) {
 				settingsStr = settingsStr + "R\n" + numRounds + "\n"
 						+ this.inputNumConts + "\n";
 			}
 			if (this.wager != 0)
 				settingsStr = settingsStr + "W\n" + this.wager + "\n";
 			bWr.write(settingsStr);
 			bWr.flush();
 			bWr.close();
 		} catch (IOException e) {
 		}
 	}
 
 	public void readContestants(String fileName) {
 		try {
 			DataInputStream input = new DataInputStream(new FileInputStream(
 					fileName));
 			BufferedReader in = new BufferedReader(new InputStreamReader(input));
 			String currCont = in.readLine();
 			this.contCount = 0;
 			this.contestantsArray = new Contestant[15];
 			while (currCont != null && !currCont.trim().equals("")) {
 				int i = 0;
 				String cFirst = "", cLast = "", cID = "", cTrb = "", cPic = "", cElimRoundStr = "";
 				int elimRound = 0;
 				while (i < currCont.length() && currCont.charAt(i) != '+') {
 					cFirst = cFirst + currCont.charAt(i);
 					i++;
 				}
 				i++;
 				while (i < currCont.length() && currCont.charAt(i) != '+') {
 					cLast = cLast + currCont.charAt(i);
 					i++;
 				}
 				i++;
 				while (i < currCont.length() && currCont.charAt(i) != '+') {
 					cID = cID + currCont.charAt(i);
 					i++;
 				}
 				i++;
 				while (i < currCont.length() && currCont.charAt(i) != '+') {
 					cPic = cPic + currCont.charAt(i);
 					i++;
 				}
 				i++;
 				while (i < currCont.length() && currCont.charAt(i) != '+') {
 					cTrb = cTrb + currCont.charAt(i);
 					i++;
 				}
 				i++;
 				Contestant contElim = new Contestant(cFirst, cLast, cID, cTrb,
 						cPic);
 				
 				if (i < currCont.length() && currCont.charAt(i) != '+') {
 					for (; i < currCont.length(); i++) {
 						if (currCont.charAt(i) == '+') {
 							elimRound = Integer.valueOf(cElimRoundStr);
 							contElim.setElimRound(new Round(elimRound));
 							break;
 
 						} else {
 							cElimRoundStr = cElimRoundStr + currCont.charAt(i);
 						}
 					}
 				}
 				this.contestantsArray[contCount] = contElim;
 				
 				contCount++;
 				currCont = in.readLine();
 			}
 		} catch (IOException e) {
 		}
 	}
 
 	public void writeContestants(String fileName) {
 		BufferedWriter bWr = null;
 		String currString = "";
 		try {
 
 			bWr = new BufferedWriter(new FileWriter(fileName));
 			for (int i = 0; i < this.contCount; i++) {
 				currString = currString + this.contestantsArray[i].getFirst()
 						+ "+";
 				currString = currString + this.contestantsArray[i].getLast()
 						+ "+";
 				currString = currString + this.contestantsArray[i].getID()
 						+ "+";
 				currString = currString + this.contestantsArray[i].getPicture()
 						+ "+";
 				currString = currString + this.contestantsArray[i].getTribe()
 						+ "+";
 				if (this.contestantsArray[i].getElimRound() != null) {
 					currString = currString
 							+ this.contestantsArray[i].getElimRound()
 									.getRoundNum();
 				}
 				currString = currString + "+\n";
 			}
 			bWr.write(currString);
 			bWr.flush();
 			bWr.close();
 		} catch (Exception e) {
 		}
 	}
 
 	/**
 	 * Creates a new player array & stores the player object to the array If the
 	 * player file is empty, it stores the record p in the first line of the
 	 * file Otherwise, the input is appended to the players file
 	 * 
 	 * @param Player
 	 *            data that is read from readPlayers(String fileName)
 	 */
 	public void addPlayer(Player p) {
 
 		if (this.playersArray == null) {
 			this.playersArray = new Player[1];
 			this.playersArray[0] = p;
 		} else {
 			Player[] newPlayers = new Player[this.playersArray.length + 1];
 			for (int i = 0; i < this.playersArray.length; i++) {
 				newPlayers[i] = this.playersArray[i];
 			}
 			newPlayers[this.playersArray.length] = p;
 			this.playersArray = newPlayers;
 		}
 	}
 
 	public Player[] getPlayers() {
 		return this.playersArray;
 	}
 
 	/**
 	 * The menu bar
 	 * 
 	 * @return menu bar with menu items
 	 */
 	private JMenuBar menuBar() {
 		JMenuBar menuBar = new JMenuBar();
 		JMenu fileMenu, editMenu, windowMenu;
 		JMenuItem startGameItem, newItem, mainItem, quitItem, bonusQItem, playerItem, contestantItem, themeItem, statsItem, creditsItem, nextRoundItem;
 
 		fileMenu = new JMenu("File");
 		editMenu = new JMenu("Edit");
 		windowMenu = new JMenu("Window");
 
 		startGameItem = new JMenuItem("Game Settings");
 		startGameItem.setActionCommand("settings");
 		startGameItem.addActionListener(this);
 
 		newItem = new JMenuItem("New");
 		newItem.setActionCommand("new");
 		newItem.addActionListener(this);
 
 		nextRoundItem = new JMenuItem("Next Round");
 		nextRoundItem.setActionCommand("next");
 		nextRoundItem.addActionListener(this);
 		mainItem = new JMenuItem("Main Menu");
 		mainItem.setActionCommand("main");
 		mainItem.addActionListener(this);
 
 		quitItem = new JMenuItem("Quit");
 		quitItem.setActionCommand("q");
 		quitItem.addActionListener(this);
 
 		bonusQItem = new JMenuItem("Bonus Question Window");
 		bonusQItem.setActionCommand("bonus");
 		bonusQItem.addActionListener(this);
 
 		playerItem = new JMenuItem("Player Add/Edit Window");
 		playerItem.setActionCommand("players");
 		playerItem.addActionListener(this);
 
 		contestantItem = new JMenuItem("Contestant Add/Edit Window");
 		contestantItem.setActionCommand("contestants");
 		contestantItem.addActionListener(this);
 
 		themeItem = new JMenuItem("Change Theme");
 		themeItem.setToolTipText("Jungle Theme or Golden Ruins Theme");
 		themeItem.setActionCommand("theme");
 		themeItem.addActionListener(new ChangeThemeHandler());
 
 		statsItem = new JMenuItem("See Player Stats");
 		creditsItem = new JMenuItem("Credits");
 		creditsItem.setToolTipText("Images Cited"); // Will appear in a dialog
 		// box
 
 		fileMenu.add(newItem);
 		fileMenu.add(mainItem);
 		fileMenu.add(startGameItem);
 		fileMenu.add(nextRoundItem);
 		fileMenu.add(quitItem);
 
 		editMenu.add(bonusQItem);
 		editMenu.add(playerItem);
 		editMenu.add(contestantItem);
 
 		windowMenu.add(statsItem);
 		windowMenu.add(themeItem);
 		windowMenu.addSeparator();
 		windowMenu.add(creditsItem);
 
 		menuBar.add(fileMenu);
 		menuBar.add(editMenu);
 		menuBar.add(windowMenu);
 
 		return menuBar;
 	}
 
 	/**
 	 * Layout of the main screen that appears as the GUI when main method is run
 	 * 
 	 * @return panel with the main screen buttons & title panel
 	 */
 	private JComponent mainScreen() {
 		SpringLayout guiLayout = new SpringLayout();
 		mainPanel = new JPanel(guiLayout);
 		this.setLayout(guiLayout);
 
 		getContentPane().add(mainButtons());
 		getContentPane().add(titleComponent());
 		getContentPane().add(quitButton());
 		getContentPane().add(themeMaker); // Set the default theme as Golden
 		// ruins
 
 		// Layout the components in the frame
 		guiLayout.putConstraint(SpringLayout.WEST, themeMaker, 0,
 				SpringLayout.WEST, getContentPane());
 		guiLayout.putConstraint(SpringLayout.NORTH, themeMaker, 0,
 				SpringLayout.NORTH, getContentPane());
 
 		guiLayout.putConstraint(SpringLayout.WEST, titlePanel, 50,
 				SpringLayout.WEST, getContentPane());
 		guiLayout.putConstraint(SpringLayout.NORTH, titlePanel, 30,
 				SpringLayout.NORTH, getContentPane());
 
 		guiLayout.putConstraint(SpringLayout.WEST, mainButtonsPanel, 60,
 				SpringLayout.WEST, getContentPane());
 		guiLayout.putConstraint(SpringLayout.NORTH, mainButtonsPanel, 150,
 				SpringLayout.NORTH, getContentPane());
 
 		return mainPanel;
 	}// End of main panel
 
 	/**
 	 * Main Screen Buttons - Images that change with the theme
 	 * 
 	 * @return a panel with the 6 main buttons
 	 */
 	private JComponent mainButtons() {
 		mainButtonsPanel = new JPanel(new GridLayout(2, 3, 60, 30));
 
 		// Game Settings Button -- To Create New & to Start the Game
 		ImageIcon[] gameSettingsArray = new ImageIcon[13];
 
 		for (int i = 0; i < 13; i++) {
 			gameSettingsArray[i] = createImageIcon("images/" + i + ".png");
 			gameSettingsButtons[i] = new JButton("Game Settings",
 					gameSettingsArray[i]);
 			// Set the text position
 			gameSettingsButtons[i]
 					.setVerticalTextPosition(AbstractButton.BOTTOM);
 			gameSettingsButtons[i]
 					.setHorizontalTextPosition(AbstractButton.CENTER);
 			// Set the button layout
 			gameSettingsButtons[i].setOpaque(false);
 			gameSettingsButtons[i].setFocusPainted(false);
 			gameSettingsButtons[i].setBorderPainted(false);
 			gameSettingsButtons[i].setContentAreaFilled(false);
 			gameSettingsButtons[i].setBorder(BorderFactory.createEmptyBorder(0,
 					0, 0, 0));
 			gameSettingsButtons[i].setMnemonic(KeyEvent.VK_N);
 			// Set Action Commands & Listeners
 			gameSettingsButtons[i]
 					.setToolTipText("Current Game Settings - Current Round: "
 							+ i);
 			gameSettingsButtons[i].setActionCommand("settings");
 			gameSettingsButtons[i].addActionListener(this);
 		}
 		gameSettingsButtons[0]
 				.setToolTipText("Game Settings - Start Game - Create New Game");
 
 		// Players List Button - Set Gold image as part of default theme
 		playersBtn = new JButton("Players", playerBtnImg);
 		// Set the text position
 		playersBtn.setVerticalTextPosition(AbstractButton.BOTTOM);
 		playersBtn.setHorizontalTextPosition(AbstractButton.CENTER);
 		// Set the button layout
 		playersBtn.setOpaque(false);
 		playersBtn.setFocusPainted(false);
 		playersBtn.setBorderPainted(false);
 		playersBtn.setContentAreaFilled(false);
 		playersBtn.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
 		// Set Action Commands & Listeners
 		playersBtn.setMnemonic(KeyEvent.VK_P);
 		playersBtn.setToolTipText("Players List");
 		playersBtn.setActionCommand("players");
 		playersBtn.addActionListener(this);
 
 		// Contestants List Button
 		contestantImg = createImageIcon("images/contestants.png");
 		contestantsBtn = new JButton("Contestants", contestantImg);
 		// Set the text position
 		contestantsBtn.setVerticalTextPosition(AbstractButton.BOTTOM);
 		contestantsBtn.setHorizontalTextPosition(AbstractButton.CENTER);
 		// Set the button layout
 		contestantsBtn.setOpaque(false);
 		contestantsBtn.setFocusPainted(false);
 		contestantsBtn.setBorderPainted(false);
 		contestantsBtn.setContentAreaFilled(false);
 		contestantsBtn.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
 		// Set Action Commands & Listeners
 		contestantsBtn.setMnemonic(KeyEvent.VK_C);
 		contestantsBtn.setToolTipText("Contestants List");
 		contestantsBtn.setActionCommand("contestants");
 		contestantsBtn.addActionListener(this);
 
 		// Standings List Button
 		stadingsImg = createImageIcon("images/standings.png");
 		standingsBtn = new JButton("Current Standings", stadingsImg);
 		// Set the text position
 		standingsBtn.setVerticalTextPosition(AbstractButton.BOTTOM);
 		standingsBtn.setHorizontalTextPosition(AbstractButton.CENTER);
 		// Set the button layout
 		standingsBtn.setOpaque(false);
 		standingsBtn.setFocusPainted(false);
 		standingsBtn.setBorderPainted(false);
 		standingsBtn.setContentAreaFilled(false);
 		standingsBtn.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
 		// Set Action Commands & Listeners
 		standingsBtn.setMnemonic(KeyEvent.VK_S);
 		standingsBtn.setToolTipText("Current Contest Standings");
 		standingsBtn.setActionCommand("standings");
 		standingsBtn.addActionListener(this);
 
 		// Bonus Question Button
 		bonusQImg = createImageIcon("images/bq.png");
 		bonusQBtn = new JButton("Bonus Questions", bonusQImg);
 		// Set the text position
 		bonusQBtn.setVerticalTextPosition(AbstractButton.BOTTOM);
 		bonusQBtn.setHorizontalTextPosition(AbstractButton.CENTER);
 		// Set the button layout
 		bonusQBtn.setOpaque(false);
 		bonusQBtn.setFocusPainted(false);
 		bonusQBtn.setBorderPainted(false);
 		bonusQBtn.setContentAreaFilled(false);
 		bonusQBtn.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
 		// Set Action Commands & Listeners
 		bonusQBtn.setMnemonic(KeyEvent.VK_B);
 		bonusQBtn.setToolTipText("Add & Edit Bonus Questions");
 		bonusQBtn.setActionCommand("bonus");
 		bonusQBtn.addActionListener(this);
 
 		// Theme Selector Button
 		themeSelectImg = createImageIcon("images/themeSelect.png");
 		themeSelectBtn = new JButton("Select a Theme", themeSelectImg);
 		// Set the text position
 		themeSelectBtn.setVerticalTextPosition(AbstractButton.BOTTOM);
 		themeSelectBtn.setHorizontalTextPosition(AbstractButton.CENTER);
 		// Set the button layout
 		themeSelectBtn.setOpaque(false);
 		themeSelectBtn.setFocusPainted(false);
 		themeSelectBtn.setBorderPainted(false);
 		themeSelectBtn.setContentAreaFilled(false);
 		themeSelectBtn.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
 		// Set Action Commands & Listeners
 		themeSelectBtn.setMnemonic(KeyEvent.VK_T);
 		themeSelectBtn.setToolTipText("Golden Ruins Theme or Jungle Theme");
 		themeSelectBtn.setActionCommand("theme");
 		themeSelectBtn.addActionListener(new ChangeThemeHandler());
 
 		mainButtonsPanel.setOpaque(false);
 
 		mainButtonsPanel.add(playersBtn);
 
 		if (startGame == false) {
 			mainButtonsPanel.add(gameSettingsButtons[0]);
 		}
 		if (startGame == true) {
 			mainButtonsPanel.add(gameSettingsButtons[roundNum]);
 		}
 
 		mainButtonsPanel.add(contestantsBtn);
 		mainButtonsPanel.add(standingsBtn);
 		mainButtonsPanel.add(bonusQBtn);
 		mainButtonsPanel.add(themeSelectBtn);
 
 		return mainButtonsPanel;
 	}
 
 	/**
 	 * The Standard buttons for list input/update area addBtn: Adds the info to
 	 * the appropriate register deleteBtn: Finds and Deletes the record enitrely
 	 * from its register
 	 * 
 	 * @return 2 buttons floating right to left Add/Update
 	 */
 	private JComponent addDeletePlayerButtons() {
 		JPanel panel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
 
 		addPlayerBtn = new JButton("Add Player");
 		addPlayerBtn.setActionCommand("+P");
 		addPlayerBtn.addActionListener(this);
 
 		deletePlayerBtn = new JButton("Delete Player");
 		deletePlayerBtn.setActionCommand("deleteP");
 		deletePlayerBtn.addActionListener(this);
 
 		panel.add(addPlayerBtn);
 		panel.add(deletePlayerBtn);
 		panel.setOpaque(false);
 
 		return panel;
 	}
 
 	/**
 	 * The Standard buttons for list input/update area updateBtn: Finds the
 	 * record and allows user to make changes to any field resetBtn: Clears all
 	 * fields
 	 * 
 	 * @param op
 	 * @return 4 buttons floating right to left Add/Update/Remove/Reset
 	 */
 	private JComponent modifyPlayerButtons() {
 		JPanel panel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
 
 		updateBtn = new JButton("Update Player");
 		updateBtn.setActionCommand("updateP");
 		updateBtn.addActionListener(this);
 
 		resetBtn = new JButton("Reset");
 		resetBtn.setActionCommand("reset");
 		resetBtn.addActionListener(this);
 
 		panel.setOpaque(false);
 
 		// Add the buttons to the panel
 		panel.add(addDeletePlayerButtons());
 		panel.add(updateBtn);
 		panel.add(resetBtn);
 
 		// Match the SpringLayout's gap, subtracting 5 to make
 		// up for the default gap FlowLayout provides.
 		panel.setBorder(BorderFactory.createEmptyBorder(0, 0, GAP - 5, GAP - 5));
 
 		return panel;
 	}
 
 	/**
 	 * Add/Delete Buttons for the modify contestant panel
 	 * 
 	 * @return 2 buttons on a panel
 	 */
 	private JComponent addDeleteContButtons() {
 		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
 
 		addContBtn = new JButton("Add Contestant");
 		addContBtn.setActionCommand("+C");
 		addContBtn.addActionListener(this);
 
 		deleteContBtn = new JButton("Delete Contestant");
 		deleteContBtn.setActionCommand("deleteC");
 		deleteContBtn.addActionListener(this);
 
 		panel.setOpaque(false);
 		panel.add(addContBtn);
 		panel.add(deleteContBtn);
 
 		return panel;
 	}
 
 	/**
 	 * Update to modify player/contestant panels
 	 * 
 	 * @return 2 buttons on a panel
 	 */
 	private JComponent modifyContButtons() {
 		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
 
 		updateBtn = new JButton("Update Contestant");
 		updateBtn.setActionCommand("updateC");
 		updateBtn.addActionListener(this);
 
 		resetBtn = new JButton("Reset");
 		resetBtn.setActionCommand("reset");
 		resetBtn.addActionListener(this);
 
 		panel.add(updateBtn);
 		panel.add(resetBtn);
 		panel.setOpaque(false);
 
 		// Match the SpringLayout's gap, subtracting 5 to make
 		// up for the default gap FlowLayout provides.
 		panel.setBorder(BorderFactory.createEmptyBorder(0, 0, GAP - 5, GAP - 5));
 
 		return panel;
 	}
 
 	/**
 	 * Contestant Panel after game has started Shows a eliminate contestant
 	 * radio button & textField for Round Eliminated *
 	 */
 	private JComponent elimContPanel() {
 		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
 
 		eliminateYBtn = new JRadioButton("Yes");
 		JRadioButton eliminateNBtn = new JRadioButton("No");
 		// Group the radio buttons.
 		ButtonGroup yNgroup = new ButtonGroup();
 		yNgroup.add(eliminateYBtn);
 		yNgroup.add(eliminateNBtn);
 
 		eliminateNBtn.setSelected(true);
 
 		eliminateYBtn.addActionListener(this);
 
 		JLabel elimLabel = new JLabel("Eliminate Contestant");
 		// elimLabel.setLabelFor(ynPnl);
 
 		roundField = new JFormattedTextField();
 		roundField.setValue(new Integer(0));
 		roundField.setColumns(5);
 		JLabel roundLbl = new JLabel("Round Eliminated");
 		roundLbl.setLabelFor(roundField);
 
 		panel.add(elimLabel);
 		panel.add(eliminateYBtn);
 		panel.add(eliminateNBtn);
 		panel.add(roundLbl);
 		panel.add(roundField);
 		panel.setOpaque(false);
 
 		return panel;
 	}
 
 	private boolean setEliminated() {
 		return contEliminated = true;
 	}
 
 	private boolean getEliminated() {
 		return contEliminated;
 	}
 
 	private int getRoundEliminated() {
 		return (Integer) roundField.getValue();
 	}
 
 	/**
 	 * 
 	 */
 	/**
 	 * Once the game has started, admin cannot add/delete players & contestants
 	 * 
 	 * @param boolean button value
 	 * @return the button setting on/off
 	 */
 	public void setStartButtons() {
 		addDeletePlayerButtons().setEnabled(false);
 		addDeleteContButtons().setEnabled(false);
 		repaint();
 		validate();
 	}
 
 	public boolean setStartGame(boolean start) {
 		startGame = start;
 		return startGame;
 	}
 
 	public boolean getStartGame() {
 		return startGame;
 	}
 
 	public int getWager() {
 		return (Integer) wagerField.getValue();
 	}
 
 	/**
 	 * This button allows the user to upload a pic from file and it will fit the
 	 * image into a special frame depending on the theme.
 	 * 
 	 * @return upload button
 	 */
 	private JComponent uploadButton() {
 		JPanel p = new JPanel();
 
 		uploadedImage = createImageIcon("images/uploadPicFrame.jpg");
 
 		uploadBtn = new JButton("Upload a Picture", uploadedImage);
 		uploadBtn.setVerticalTextPosition(AbstractButton.BOTTOM);
 		uploadBtn.setHorizontalTextPosition(AbstractButton.CENTER);
 		uploadBtn.setActionCommand("upload");
 		uploadBtn.addActionListener(this);
 
 		p.setOpaque(false);
 		p.add(uploadBtn);
 		return p;
 	}
 
 	/**
 	 * The Title Banner
 	 * 
 	 * @return title panel
 	 */
 	private JComponent titleComponent() {
 		titlePanel = new JPanel();
 		title = createImageIcon("images/title.png");
 		titleBanner = new JLabel(title);
 		titleBanner
 				.setToolTipText("By Hazel R, Manor F, Jeff W, Liam C, Delerina H, Martin G");
 
 		titlePanel.setOpaque(false);
 		titlePanel.add(titleBanner);
 
 		return titlePanel;
 	}
 
 	/**
 	 * Main Menu Button
 	 * 
 	 * @return a panel with a main menu button on it that changes dynamically
 	 *         depending on the theme
 	 */
 	private JComponent mainMenuButton() {
 		mMenuBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
 		mainMenuBtnImg = createImageIcon("images/mainMenuButton.png");
 		mainMenuBtn = new JButton(mainMenuBtnImg);
 		mainMenuBtn.setToolTipText("Return to the Main Menu  ALT+1");
 
 		// Set the button layout
 		mainMenuBtn.setOpaque(false);
 		mainMenuBtn.setFocusPainted(false);
 		mainMenuBtn.setBorderPainted(false);
 		mainMenuBtn.setContentAreaFilled(false);
 		mainMenuBtn.setBorder(BorderFactory.createEmptyBorder(GAP, GAP * 4,
 				GAP, 400));
 
 		mMenuBtnPanel.setOpaque(false);
 
 		mainMenuBtn.setMnemonic(KeyEvent.VK_1);
 		mainMenuBtn.setActionCommand("main");
 		mainMenuBtn.addActionListener(this);
 
 		mMenuBtnPanel.add(mainMenuBtn);
 		mMenuBtnPanel.add(currentWeekBoard());
 		repaint();
 		validate();
 
 		return mMenuBtnPanel;
 	}
 
 	/**
 	 * Current Week Panel
 	 * 
 	 * @return a panel that displays the current week
 	 */
 	private JComponent currentWeekBoard() {
 		// Create an array of images & convert it to an array of labels
 		ImageIcon[] cWkImgArray = new ImageIcon[13];
 		JLabel[] cWkLabelArray = new JLabel[13];
 
 		for (int i = 1; i < 13; i++) {
 			cWkImgArray[i] = createImageIcon("images/currWkBoard" + i + ".png");
 			cWkLabelArray[i] = new JLabel(cWkImgArray[i]);
 		}
 
 		currWkPanel = new JPanel();
 		currWkPanel.setOpaque(false);
 
 		// currWkPanel.setPreferredSize(new
 		// Dimension(wkImage.getIconWidth(),wkImage.getIconHeight()));
 
 		// currWkPanel.add(week, BorderLayout.EAST);
 		if (startGame == true) {
 			currWkPanel.add(cWkLabelArray[roundNum], BorderLayout.WEST);
 		}
 
 		return currWkPanel;
 	}
 
 	/**
 	 * The Quit button
 	 * 
 	 * @return an image with the quit button
 	 */
 	private JComponent quitButton() {
 		quitPanel = new JPanel();
 
 		// Quit Button
 		quitImg = createImageIcon("images/Quit-icon.png");
 		quitBtn = new JButton(quitImg);
 
 		// Set the button layout
 		quitBtn.setOpaque(false);
 		quitBtn.setFocusPainted(false);
 		quitBtn.setBorderPainted(false);
 		quitBtn.setContentAreaFilled(false);
 		quitBtn.setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));
 
 		// Set the button panel layout
 		quitPanel.setOpaque(false);
 
 		// Set Action Commands
 		quitBtn.setMnemonic(KeyEvent.VK_Q);
 		quitBtn.setToolTipText("Quit");
 
 		quitBtn.setActionCommand("q");
 		quitBtn.addActionListener(this);
 
 		quitPanel.add(quitBtn);
 
 		return quitPanel;
 	}
 
 	/**
 	 * Bonus Question Panel A Bonus Q&A Input Area
 	 * 
 	 * @return a panel with the bonus question panel
 	 */
 	private JComponent bqPanel() {
 		bqPanel = new BonusQuestionGUI(rounds, roundNum);
 		bqPanel.setSize(800, 400);
 		Rectangle r = new Rectangle(800, 400);
 		bqPanel.setBounds(r);
 		qPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
 
 		qPanel.setLayout(new BoxLayout(qPanel, BoxLayout.PAGE_AXIS));
 		qPanel.add(bqPanel);
 		qPanel.setOpaque(false);
 		qPanel.setSize(800, 400);
 
 		// Assemble the text fields with the background as a large panel
 		SpringLayout bqLayout = new SpringLayout();
 		JPanel contPanel = new JPanel(bqLayout);
 		contPanel.setSize(800,400);
 		this.setLayout(bqLayout);
 
 		getContentPane().add(mainMenuButton());
 		getContentPane().add(qPanel);
 		getContentPane().add(bqBg);
 
 		bqLayout.putConstraint(SpringLayout.WEST, bqBg, 0,
 				SpringLayout.WEST, getContentPane());
 		bqLayout.putConstraint(SpringLayout.NORTH, bqBg, 0,
 				SpringLayout.NORTH, getContentPane());
 
 		bqLayout.putConstraint(SpringLayout.WEST, mMenuBtnPanel, 0,
 				SpringLayout.WEST, getContentPane());
 		bqLayout.putConstraint(SpringLayout.NORTH, mMenuBtnPanel, 0,
 				SpringLayout.NORTH, getContentPane());
 
 		bqLayout.putConstraint(SpringLayout.WEST, qPanel, 100,
 				SpringLayout.WEST, getContentPane());
 		bqLayout.putConstraint(SpringLayout.NORTH, qPanel, 170,
 				SpringLayout.NORTH, getContentPane());/*
 		bqLayout.putConstraint(SpringLayout.EAST, qPanel, 100,
 				SpringLayout.WEST, getContentPane());
 		bqLayout.putConstraint(SpringLayout.SOUTH, qPanel, 10,
 				SpringLayout.NORTH, getContentPane());*/
 
 		
 		return contPanel;
 	}
 	
 
 	/**
 	 * Standings Panel A Chart and current information
 	 * 
 	 * @return a panel with the player list table sort
 	 */
 	private JComponent standingsPanel() {
 		sPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
 
 		sPanel.setOpaque(false);
 		sPanel.add(standingsTable.createPlayerList());
 
 		// Create a modify players button that will navigate to the players
 		// panel
 		ImageIcon modPlayerImg = createImageIcon("images/playerModButton.png");
 		playerModButton = new JButton(modPlayerImg);
 
 		// Set the button layout
 		playerModButton.setOpaque(false);
 		playerModButton.setFocusPainted(false);
 		playerModButton.setBorderPainted(false);
 		playerModButton.setContentAreaFilled(false);
 		playerModButton.setBorder(BorderFactory.createEmptyBorder(GAP, GAP,
 				GAP, GAP));
 
 		// Set Action Commands
 		playerModButton.setActionCommand("players");
 		playerModButton.addActionListener(this);
 
 		// Assemble the text fields with the background as a large panel
 		SpringLayout sLayout = new SpringLayout();
 		JPanel standingPanel = new JPanel(sLayout);
 		this.setLayout(sLayout);
 
 		getContentPane().add(mainMenuButton());
 		getContentPane().add(sPanel);
 		getContentPane().add(playerModButton);
 		getContentPane().add(standingBg);
 
 		sLayout.putConstraint(SpringLayout.WEST, standingBg, 0,
 				SpringLayout.WEST, getContentPane());
 		sLayout.putConstraint(SpringLayout.NORTH, standingBg, 0,
 				SpringLayout.NORTH, getContentPane());
 
 		sLayout.putConstraint(SpringLayout.WEST, mMenuBtnPanel, 0,
 				SpringLayout.WEST, getContentPane());
 		sLayout.putConstraint(SpringLayout.NORTH, mMenuBtnPanel, 0,
 				SpringLayout.NORTH, getContentPane());
 
 		sLayout.putConstraint(SpringLayout.WEST, sPanel, GAP * 2,
 				SpringLayout.WEST, getContentPane());
 		sLayout.putConstraint(SpringLayout.NORTH, sPanel, 175,
 				SpringLayout.NORTH, getContentPane());
 
 		sLayout.putConstraint(SpringLayout.EAST, playerModButton, -100,
 				SpringLayout.EAST, getContentPane());
 		sLayout.putConstraint(SpringLayout.NORTH, playerModButton, 625,
 				SpringLayout.NORTH, getContentPane());
 
 		// Create the labels and text areas for this panel
 		String poolString = "Total in Pool";
 
 		JLabel poolLabel, currWkLabel, numContLabel, recElimLabel;
 		// private JTextArea poolArea, currWkArea, numContArea, recElimArea;
 
 		return standingPanel;
 	}
 
 	/**
 	 * Players Edit, Add & Store Panel/Screen
 	 * 
 	 * @return a panel to add/delete/modify players
 	 */
 	private JComponent playersPanel() {
 		pPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
 
 		JPanel leftHalf_p = new JPanel();
 		leftHalf_p.setLayout(new BoxLayout(leftHalf_p, BoxLayout.PAGE_AXIS));
 
 		leftHalf_p.add(textFields_p.playerDropDownList(playersArray));
 		leftHalf_p.add(textFields_p.createFieldsPlayer());
 		leftHalf_p.add(modifyPlayerButtons());
 		leftHalf_p.setOpaque(false);
 
 		// Create a separator
 		leftHalf_p.setBorder(BorderFactory.createEmptyBorder(GAP / 4, // top
 				0, // left
 				GAP / 4, // bottom
 				0)); // right
 		leftHalf_p.add(new JSeparator(JSeparator.VERTICAL),
 				BorderLayout.LINE_START);
 		// leftHalf_p.setPreferredSize(new Dimension(492,580));
 
 		pPanel.setOpaque(false);
 		pPanel.add(leftHalf_p);
 		pPanel.add(textFields_p.createRecordDisplay(), BorderLayout.CENTER);
 
 		// Assemble the text fields with the background as a large panel
 		SpringLayout pLayout = new SpringLayout();
 		JPanel playerPanel = new JPanel(pLayout);
 		this.setLayout(pLayout);
 
 		getContentPane().add(mainMenuButton());
 		getContentPane().add(pPanel);
 		getContentPane().add(playerBg);
 
 		pLayout.putConstraint(SpringLayout.WEST, playerBg, 0,
 				SpringLayout.WEST, getContentPane());
 		pLayout.putConstraint(SpringLayout.NORTH, playerBg, 0,
 				SpringLayout.NORTH, getContentPane());
 
 		pLayout.putConstraint(SpringLayout.WEST, mMenuBtnPanel, 0,
 				SpringLayout.WEST, getContentPane());
 		pLayout.putConstraint(SpringLayout.NORTH, mMenuBtnPanel, 0,
 				SpringLayout.NORTH, getContentPane());
 
 		pLayout.putConstraint(SpringLayout.WEST, pPanel, GAP * 2,
 				SpringLayout.WEST, getContentPane());
 		pLayout.putConstraint(SpringLayout.NORTH, pPanel, 150,
 				SpringLayout.NORTH, getContentPane());
 
 		return playerPanel;
 	}
 
 	/**
 	 * Contestant List Panel The a list that appears to show contestants in play
 	 * 
 	 * @return a panel that displays the contestant list & a button that will
 	 *         show a panel to add/delete/modify contestants
 	 */
 	private JComponent contListPanel() {
 		// Create a Add/Modify Contestant List Button
 		ImageIcon textImg = createImageIcon("images/contestantOptions.png");
 		contOptionsBtn = new JButton(textImg);
 
 		// Set the button layout
 		contOptionsBtn.setOpaque(false);
 		contOptionsBtn.setFocusPainted(false);
 		contOptionsBtn.setBorderPainted(false);
 		contOptionsBtn.setContentAreaFilled(false);
 		contOptionsBtn.setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP,
 				GAP));
 
 		// Set Action Commands & Listeners
 		contOptionsBtn.setMnemonic(KeyEvent.VK_I);
 		contOptionsBtn.setToolTipText("Add, Modify, Delete Contestants  Alt+I");
 		contOptionsBtn.setActionCommand("contMod");
 		contOptionsBtn.addActionListener(this);
 
 		cLPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
 
 		cLPanel.setLayout(new BoxLayout(cLPanel, BoxLayout.PAGE_AXIS));
 		cLPanel.add(contLiTable.createContList());
 		cLPanel.setOpaque(false);
 
 		// Assemble the text fields with the background as a large panel
 		SpringLayout clLayout = new SpringLayout();
 		JPanel contPanel = new JPanel(clLayout);
 		this.setLayout(clLayout);
 
 		getContentPane().add(mainMenuButton());
 		getContentPane().add(cLPanel);
 		getContentPane().add(contOptionsBtn);
 		getContentPane().add(contestantBg);
 
 		clLayout.putConstraint(SpringLayout.WEST, contestantBg, 0,
 				SpringLayout.WEST, getContentPane());
 		clLayout.putConstraint(SpringLayout.NORTH, contestantBg, 0,
 				SpringLayout.NORTH, getContentPane());
 
 		clLayout.putConstraint(SpringLayout.WEST, mMenuBtnPanel, 0,
 				SpringLayout.WEST, getContentPane());
 		clLayout.putConstraint(SpringLayout.NORTH, mMenuBtnPanel, 0,
 				SpringLayout.NORTH, getContentPane());
 
 		clLayout.putConstraint(SpringLayout.WEST, contOptionsBtn, 725,
 				SpringLayout.WEST, getContentPane());
 		clLayout.putConstraint(SpringLayout.NORTH, contOptionsBtn, 550,
 				SpringLayout.NORTH, getContentPane());
 
 		clLayout.putConstraint(SpringLayout.WEST, cLPanel, 77,
 				SpringLayout.WEST, getContentPane());
 		clLayout.putConstraint(SpringLayout.NORTH, cLPanel, 100,
 				SpringLayout.NORTH, getContentPane());
 
 		return contPanel;
 	}
 
 	/**
 	 * Contestants Edit, Add & Store Panel/Screen
 	 * 
 	 * @return a panel to add/delete/modify contestants & a button that will
 	 *         show a panel that displays the contestant list
 	 */
 	private JComponent contestantPanel() {
 		cPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
 
 		JPanel leftHalf_c = new JPanel();
 		leftHalf_c.setLayout(new BoxLayout(leftHalf_c, BoxLayout.PAGE_AXIS));
 		leftHalf_c.setOpaque(false);
 
 		leftHalf_c.add(textFields_c.contestantDropDownList(
 				this.contestantsArray, contCount));
 
 		leftHalf_c.add(textFields_c.createFieldsCont());
 
 		leftHalf_c.add(uploadButton());
 
 		// if the game has started show the eliminate contestant option
 		// otherwise show the add/delete contestant options
 		if (getStartGame() == true) {
 			leftHalf_c.add(elimContPanel());
 		}
 		if (getStartGame() == false) {
 			leftHalf_c.add(addDeleteContButtons());
 		}
 
 		leftHalf_c.add(modifyContButtons());
 
 		// leftHalf_c.setPreferredSize(new Dimension(492,580));
 
 		// Create a Contestant List Panel to switch back to the contestant list
 		ImageIcon retImg = createImageIcon("images/contListButton.png");
 		contListBtn = new JButton(retImg);
 		// Set the button layout
 		contListBtn.setOpaque(false);
 		contListBtn.setFocusPainted(false);
 		contListBtn.setBorderPainted(false);
 		contListBtn.setContentAreaFilled(false);
 		contListBtn.setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP,
 				GAP));
 
 		// Set Action Commands & Listeners
 		contListBtn.setMnemonic(KeyEvent.VK_L);
 		contListBtn.setToolTipText("Contestants List  Alt+L");
 		contListBtn.setActionCommand("contestants");
 		contListBtn.addActionListener(this);
 
 		JPanel rightHalf_c = new JPanel();
 		rightHalf_c.setLayout(new BoxLayout(rightHalf_c, BoxLayout.PAGE_AXIS));
 		rightHalf_c.setOpaque(false);
 		rightHalf_c.add(contestantPicFrame);
 		rightHalf_c.add(Box.createRigidArea(new Dimension(0, GAP / 2)));
 		rightHalf_c.add(textFields_c.createRecordDisplay(), BorderLayout.NORTH);
 		rightHalf_c.add(contListBtn);
 		rightHalf_c.setPreferredSize(new Dimension(350, 445));
 
 		cPanel.setOpaque(false);
 		cPanel.add(leftHalf_c);
 		cPanel.add(Box.createRigidArea(new Dimension(GAP * 2, 0)));
 		cPanel.add(rightHalf_c, BorderLayout.CENTER);
 
 		// Assemble the text fields with the background as a large panel
 		SpringLayout cLayout = new SpringLayout();
 		JPanel contestantPanel = new JPanel(cLayout);
 		this.setLayout(cLayout);
 
 		getContentPane().add(mainMenuButton());
 		getContentPane().add(cPanel);
 		getContentPane().add(contestantBg);
 
 		cLayout.putConstraint(SpringLayout.WEST, contestantBg, 0,
 				SpringLayout.WEST, getContentPane());
 		cLayout.putConstraint(SpringLayout.NORTH, contestantBg, 0,
 				SpringLayout.NORTH, getContentPane());
 
 		cLayout.putConstraint(SpringLayout.WEST, mMenuBtnPanel, 0,
 				SpringLayout.WEST, getContentPane());
 		cLayout.putConstraint(SpringLayout.NORTH, mMenuBtnPanel, 0,
 				SpringLayout.NORTH, getContentPane());
 
 		cLayout.putConstraint(SpringLayout.WEST, cPanel, GAP * 2,
 				SpringLayout.WEST, getContentPane());
 		cLayout.putConstraint(SpringLayout.NORTH, cPanel, 150,
 				SpringLayout.NORTH, getContentPane());
 
 		return contestantPanel;
 	}
 
 	/**
 	 * Creates an image icon of a requested image, prints a error message is
 	 * request path not found
 	 * 
 	 * @param path
 	 *            to the requested file
 	 * @return the requested file
 	 */
 	public ImageIcon createImageIcon(String path) {
 		java.net.URL imgURL = SurvivorPoolAdminGUI.class.getResource(path);
 		if (imgURL != null) {
 			return new ImageIcon(imgURL);
 		} else {
 			System.err.println("Couldn't find file: " + path);
 			return null;
 		}
 	}
 
 	/**
 	 * Test if a string contains only letters
 	 * 
 	 * @param input
 	 *            string to test
 	 * @return boolean true if contains no invalid characters
 	 */
 	public Boolean checkValidChars(String input) {
 		Boolean validChars = false;
 
 		// check if characters are valid
 		for (int i = 0; i < input.length(); i++) {
 
 			// store character one at a time
 			char character = input.charAt(i);
 
 			if ((character >= 'a' && character <= 'z')
 					|| (character >= 'A' && character <= 'Z')) {
 				validChars = true;
 			} else {
 				validChars = false;
 				break;
 			}
 
 		}
 		return validChars;
 	}
 
 	// *****************************************************************************************************************************************************
 	// *****************************************************************************************************************************************************
 	/**
 	 * Searches for contestant
 	 * 
 	 * @param ID
 	 *            of player
 	 * @return position of contestant in array if found, -1 if not
 	 */
 	public int findContestant(String id) {
 		int position = -1;
 		for (int i = 0; i < contCount; i++) {
 
 			if ((contestantsArray[i].getID().equals(id))) {
 				position = i;
 				break;
 			}
 		}
 		return position;
 	}
 
 	/**
 	 * Searches for a player
 	 * 
 	 * @param ID
 	 *            of player
 	 * @return position of player in array if found, -1 if not
 	 */
 	public int findPlayer(String id) {
 		int position = -1;
 		for (int i = 0; i < playersArray.length; i++) {
 
 			if ((playersArray[i].getID().equals(id))) {
 				position = i;
 				break;
 			}
 		}
 		return position;
 	}
 
 	/**
 	 * Sets up the game setting panel as a custom JOptionPane Dialog Admin can
 	 * enter numContestants,
 	 */
 	private JComponent gameSettingsPanel() {
 		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
 
 		// Create Labels
 		contLabel = new JLabel(contString);
 		playLabel = new JLabel(playString);
 		wagerLabel = new JLabel(wagerString);
 
 		// Create the text fields/areas & set them up
 		contField = new JTextField();
 		contField.setColumns(10);
 
 		wagerField = new JFormattedTextField();
 		wagerField.setValue(new Integer(0));
 		wagerField.setColumns(10);
 
 		playArea = new JTextArea();
 		// playArea.setText(getNumPlayers()) ************ TODO ***************
 		playArea.setEditable(false);
 		playArea.setColumns(10);
 
 		// Tell accessibility tools about label/textfield & area pairs
 		contLabel.setLabelFor(contField);
 		playLabel.setLabelFor(playArea);
 		wagerLabel.setLabelFor(wagerField);
 
 		// Layout the labels in a panel
 		JPanel labelPane = new JPanel(new GridLayout(0, 1));
 		labelPane.add(contLabel);
 		labelPane.add(playLabel);
 		labelPane.add(wagerLabel);
 
 		// Layout the text fields/area in a panel
 		JPanel fieldPane = new JPanel(new GridLayout(0, 1));
 		fieldPane.add(contField);
 		fieldPane.add(playArea);
 		fieldPane.add(wagerField);
 
 		// Create the buttons
 		resetGameBtn = new JButton("Reset Game");
 		resetGameBtn.addActionListener(this);
 		resetGameBtn.setActionCommand("resetGame");
 
 		startGameBtn = new JButton("Start Game");
 		startGameBtn.addActionListener(this);
 		startGameBtn.setActionCommand("startGame");
 
 		saveSettingBtn = new JButton("Save Settings");
 		saveSettingBtn.addActionListener(this);
 		saveSettingBtn.setActionCommand("saveSettings");
 
 		// Layout the buttons in a panel
 		JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
 		buttonPane.add(resetGameBtn);
 		buttonPane.add(startGameBtn);
 		buttonPane.add(saveSettingBtn);
 
 		// Put the panels together, labels on the left, text fields/area on the
 		// right, buttons on the bottom
 		panel.add(labelPane);
 		panel.add(fieldPane);
 		panel.add(buttonPane);
 
 		// Create a Game Settings Header
 		ImageIcon title = createImageIcon("images/gameSetTitle.png");
 		JLabel titleComp = new JLabel(title);
 
 		// Assemble the text fields with the background as a large panel
 		SpringLayout settingLayout = new SpringLayout();
 		JPanel settingPanel = new JPanel(settingLayout);
 		this.setLayout(settingLayout);
 
 		getContentPane().add(mainMenuButton());
 		getContentPane().add(titleComp);
 		getContentPane().add(panel);
 		getContentPane().add(themeMaker);
 
 		settingLayout.putConstraint(SpringLayout.WEST, themeMaker, 0,
 				SpringLayout.WEST, getContentPane());
 		settingLayout.putConstraint(SpringLayout.NORTH, themeMaker, 0,
 				SpringLayout.NORTH, getContentPane());
 
 		settingLayout.putConstraint(SpringLayout.WEST, mMenuBtnPanel, 0,
 				SpringLayout.WEST, getContentPane());
 		settingLayout.putConstraint(SpringLayout.NORTH, mMenuBtnPanel, 0,
 				SpringLayout.NORTH, getContentPane());
 
 		settingLayout.putConstraint(SpringLayout.WEST, panel, 150,
 				SpringLayout.WEST, getContentPane());
 		settingLayout.putConstraint(SpringLayout.NORTH, panel, 300,
 				SpringLayout.NORTH, getContentPane());
 
 		settingLayout.putConstraint(SpringLayout.WEST, titleComp, 300,
 				SpringLayout.WEST, getContentPane());
 		settingLayout.putConstraint(SpringLayout.NORTH, titleComp, 100,
 				SpringLayout.NORTH, getContentPane());
 
 		return settingPanel;
 	}
 
 	/**
 	 * The Golden Ruins Theme Modifications of components to the theme
 	 */
 	private void goldenRuinsTheme() {
 		// Switch the images
 		playerBg.setIcon(playerGBg);
 		contestantBg.setIcon(contestantGBg);
 		standingBg.setIcon(standingGBg);
 		bqBg.setIcon(bqGBg);
 		textFields_p.setGameFontP(gFont, Color.YELLOW);
 		textFields_c.setGameFontC(gFont, Color.YELLOW);
 
 		themeMaker.setIcon(goldBackground);
 		// mainMenuBtn.setIcon(mainMenuImgG);
 
 		playerBtnImg = playersGoldImg;
 		contestantPicFrame.setIcon(blankGFrame);
 		playersBtn.setIcon(playersGoldImg);
 
 		for (int i = 0; i < gameSettingsButtons.length; i++) {
 			gameSettingsButtons[i].setFont(gFont);
 			gameSettingsButtons[i].setForeground(Color.YELLOW);
 		}
 		playersBtn.setFont(gFont);
 		playersBtn.setForeground(Color.YELLOW);
 		contestantsBtn.setFont(gFont);
 		contestantsBtn.setForeground(Color.YELLOW);
 		bonusQBtn.setFont(gFont);
 		bonusQBtn.setForeground(Color.YELLOW);
 		standingsBtn.setFont(gFont);
 		standingsBtn.setForeground(Color.YELLOW);
 		themeSelectBtn.setFont(gFont);
 		themeSelectBtn.setForeground(Color.YELLOW);
 	}
 
 	/**
 	 * The Jungle Theme Modifications of components to the theme
 	 */
 	private void jungleTheme() {
 		playersJungleImg = createImageIcon("images/bbJ.png");
 		jungleBackground = createImageIcon("images/jungle1.jpg");
 		playerJBg = createImageIcon("images/playerJungleBg.jpg");
 		contestantJBg = createImageIcon("images/contestantJungleBg.jpg");
 		blankJFrame = createImageIcon("images/uploadPicFrame_jungleFrame.png");
 		standingJBg = createImageIcon("images/standingsJungle.jpg");
 		bqJBg = createImageIcon("images/bqJungle.jpg");
 
 		// Switch the images
 		contestantPicFrame.setIcon(blankJFrame);
 		playerBtnImg = playersJungleImg;
 		playersBtn.setIcon(playersJungleImg);
 		themeMaker.setIcon(jungleBackground);
 
 		playerBg.setIcon(playerJBg);
 		contestantBg.setIcon(contestantJBg);
 		standingBg.setIcon(standingJBg);
 		bqBg.setIcon(bqJBg);
 
 		textFields_p.setGameFontP(jFont, Color.WHITE);
 		textFields_c.setGameFontC(jFont, Color.WHITE);
 
 		for (int i = 0; i < gameSettingsButtons.length; i++) {
 			gameSettingsButtons[i].setFont(jFont);
 			gameSettingsButtons[i].setForeground(Color.WHITE);
 		}
 		playersBtn.setFont(jFont);
 		playersBtn.setForeground(Color.WHITE);
 		contestantsBtn.setFont(jFont);
 		contestantsBtn.setForeground(Color.WHITE);
 		bonusQBtn.setFont(jFont);
 		bonusQBtn.setForeground(Color.WHITE);
 		standingsBtn.setFont(jFont);
 		standingsBtn.setForeground(Color.WHITE);
 		themeSelectBtn.setFont(jFont);
 		themeSelectBtn.setForeground(Color.WHITE);
 	}
 
 	private class ChangeThemeHandler implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			/** Theme Selector Button Handler **/
 			if (e.getActionCommand().equals("theme")) {
 				// Sets the jungle theme
 				if (themeMaker.getIcon().equals(goldBackground))
 					jungleTheme();
 
 				// Sets the Golden Ruins Theme
 				else
 					goldenRuinsTheme();
 			}
 		}
 	}
 
 	/**
 	 * Handles all Button Actions Each case compares the string cast as an
 	 * ActionCommand
 	 * 
 	 * @param ActionEvent
 	 *            string of the button being pressed
 	 */
 	public void actionPerformed(ActionEvent e) {
 		/** Add Player Handler **/
 		if (e.getActionCommand().equals("+P")) {
 			// Show a message that disables add/delete players if the game as
 			// already started
 			if (getStartGame() == true) {
 				JOptionPane.showMessageDialog(this,
 						"You cannot add/delete anymore players",
 						"Game has started", JOptionPane.ERROR_MESSAGE);
 			} else {
 				String first = textFields_p.getFirstP();
 				String last = textFields_p.getLastP();
 				int firstLen = first.length(), lastLen = last.length();
 				if (firstLen < 1 || firstLen > 20) {
 					JOptionPane
 							.showMessageDialog(this,
 									"The player's first Name must be between 1 and 20 characters");
 					
 				} else if (lastLen < 1 || lastLen > 20) {
 					JOptionPane
 							.showMessageDialog(this,
 									"The player's last name must be between 1 and 20 characters");
 				} else {
 					String IDchars = "" + first.charAt(0), ID = "";
 					int IDnum = 1;
 					boolean isUnique = false;
 					if (last.length() >= 6)
 						IDchars = IDchars + last.substring(0, 6);
 					else
 						IDchars = IDchars + last;
 					ID = IDchars + IDnum;
 					while (!isUnique) {
 						isUnique = true;
 						if (playersArray != null) {
 							for (int i = 0; i < playersArray.length && isUnique; i++) {
 								if (playersArray[i].getID().equals(ID)) {
 									isUnique = false;
 									break;
 								}
 							}
 							if (isUnique == false) {
 								IDnum++;
 								ID = IDchars + IDnum;
 							}
 						}
 					}
 					this.addPlayer(new Player(first, last, ID));
 					this.writePlayers("players.txt");
 					standingsTable = new PlayerListGUI(playersArray, roundNum);
 					getContentPane().removeAll();
 					getContentPane().add(quitButton());
 					getContentPane().add(playersPanel());
 					repaint();
 					validate();
 				}
 			}
 		}
 
 		/** Update Player Handler **/
 		if (e.getActionCommand().equals("updateP")) {
 			String inputFirst = textFields_p.getFirstP();
 			String inputLast = textFields_p.getLastP();
 
 			// Validate Text Input Fields
 			if ((inputFirst.length() < 1) || (inputFirst.length() > 20)) {
 				JOptionPane
 						.showMessageDialog(
 								this,
 								"Sorry that's not a valid first name, it must be between 1 and 20 characters long");
 			} else if (!checkValidChars(inputFirst)) {
 				JOptionPane
 						.showMessageDialog(this,
 								"Sorry that's not a valid first name, it must only contain letters");
 			}
 
 			else if ((inputLast.length() < 1) || (inputLast.length() > 20)) {
 				JOptionPane
 						.showMessageDialog(
 								this,
 								"Sorry that's not a valid last name, it must be between 1 and 20 characters long");
 			} else if (!checkValidChars(inputLast)) {
 				JOptionPane
 						.showMessageDialog(this,
 								"Sorry that's not a valid last name, it must only contain letters");
 			}
 
 			int position = findPlayer(textFields_p.getMenuPlayerID());
 			if (position == -1) {
 				JOptionPane
 						.showMessageDialog(this,
 								"That player does not exist, please check your player ID and try again");
 			} else {
 				Player tempPlayer = new Player(inputFirst, inputLast,
 						textFields_p.getMenuPlayerID());
 
 				playersArray[position] = tempPlayer;
 
 				JOptionPane.showMessageDialog(this, "Player has been updated");
 
 				this.writePlayers("players.txt");
 
 				standingsTable = new PlayerListGUI(playersArray, roundNum);
 				getContentPane().removeAll();
 
 				getContentPane().add(quitButton());
 
 				getContentPane().add(playersPanel());
 
 				repaint();
 				validate();
 			}
 		}
 
 		if (e.getActionCommand().equals("next")) {
 			nextRound();
 		}
 
 		/** Delete Player Handler **/
 		if (e.getActionCommand().equals("deleteP")) {
 			if (getStartGame() == true) {
 				JOptionPane.showMessageDialog(this,
 						"You cannot add/delete anymore players",
 						"Game has started", JOptionPane.ERROR_MESSAGE);
 			} else {
 
 				String inputFirst = textFields_p.getFirstP();
 				String inputLast = textFields_p.getLastP();
 
 				if (getStartGame() == true) {
 					JOptionPane.showMessageDialog(this,
 							"You cannot add/delete anymore players",
 							"Game has started", JOptionPane.ERROR_MESSAGE);
 				} else {
 					if ((inputFirst.length() < 1) || (inputFirst.length() > 20)) {
 						JOptionPane
 								.showMessageDialog(
 										this,
 										"Sorry that's not a valid first name, it must be between 1 and 20 characters long");
 					} else if (!checkValidChars(inputFirst)) {
 						JOptionPane
 								.showMessageDialog(this,
 										"Sorry that's not a valid first name, it must only contain letters");
 					}
 
 					else if ((inputLast.length() < 1)
 							|| (inputLast.length() > 20)) {
 						JOptionPane
 								.showMessageDialog(
 										this,
 										"Sorry that's not a valid last name, it must be between 1 and 20 characters long");
 					} else if (!checkValidChars(inputLast)) {
 						JOptionPane
 								.showMessageDialog(this,
 										"Sorry that's not a valid last name, it must only contain letters");
 					}
 
 					else {
 						int position = findPlayer(textFields_p
 								.getMenuPlayerID());
 						if (position == -1) {
 							JOptionPane
 									.showMessageDialog(this,
 											"That player does not exist, please check your ID entry and try again");
 						} else {
 							playersArray[position] = null;
 							Player tempArray[] = new Player[playersArray.length - 1];
 
 							for (int i = 0; i < position; i++) {
 								tempArray[i] = playersArray[i];
 							}
 
 							for (int i = position; i < playersArray.length - 1; i++) {
 								tempArray[i] = playersArray[i + 1];
 							}
 
 							playersArray = tempArray;
 
 							JOptionPane.showMessageDialog(this, "" + inputFirst
 									+ " has been deleted");
 
 							this.writePlayers("players.txt");
 
 							standingsTable = new PlayerListGUI(playersArray,
 									roundNum);
 							getContentPane().removeAll();
 
 							getContentPane().add(quitButton());
 
 							getContentPane().add(playersPanel());
 
 							repaint();
 							validate();
 						}
 					}
 				}
 			}
 		}
 
 		/** Add Contestant Handler **/
 		if (e.getActionCommand().equals("+C")) {
 			String inputFirst = textFields_c.getFirstC();
 			String inputLast = textFields_c.getLastC();
 			String inputTribe = textFields_c.getTribe();
 
 			if (getStartGame() == true) {
 				JOptionPane.showMessageDialog(this,
 						"You cannot add/delete anymore players",
 						"Game has started", JOptionPane.ERROR_MESSAGE);
 			} else if (contCount > 15) {
 				JOptionPane
 						.showMessageDialog(
 								this,
 								"You cannot add/delete anymore players",
 								"The maximum number of contestants have been added (15)",
 								JOptionPane.ERROR_MESSAGE);
 			} else {
 				if ((inputFirst.length() < 1) || (inputFirst.length() > 20)) {
 					JOptionPane
 							.showMessageDialog(
 									this,
 									"Sorry that's not a valid first name, it must be between 1 and 20 characters long");
 				} else if (!checkValidChars(inputFirst)) {
 					JOptionPane
 							.showMessageDialog(this,
 									"Sorry that's not a valid first name, it must only contain letters");
 				}
 
 				else if ((inputLast.length() < 1) || (inputLast.length() > 20)) {
 					JOptionPane
 							.showMessageDialog(
 									this,
 									"Sorry that's not a valid last name, it must be between 1 and 20 characters long");
 				} else if (!checkValidChars(inputLast)) {
 					JOptionPane
 							.showMessageDialog(this,
 									"Sorry that's not a valid last name, it must only contain letters");
 				}
 
 				else if ((inputTribe.length() < 1)
 						|| (inputTribe.length() > 30)) {
 					JOptionPane
 							.showMessageDialog(
 									this,
 									"Sorry that's not a valid tribe name, it must be between 1 and 30 characters long");
 				}
 
 				else if (!checkValidChars(inputTribe)) {
 					JOptionPane
 							.showMessageDialog(this,
 									"Sorry that's not a valid tribe name, it must only contain letters");
 				} else {
 					char IDchar = 'a';
 					int IDint = 1;
 					String ID = "" + IDchar + IDint;
 					boolean isUnique = false;
 					while (!isUnique) {
 						isUnique = true;
 						for (int i = 0; i < contCount && isUnique; i++) {
 							if (contestantsArray[i] == null)
 								;
 							else if (contestantsArray[i].getID().equals(ID)) {
 								isUnique = false;
 								break;
 							}
 						}
 						if (isUnique == false) {
 							if (IDint == 9) {
 								IDchar++;
 								IDint = 1;
 							} else
 								IDint++;
 							ID = "" + IDchar + IDint;
 						}
 					}
 					Contestant newContestant = new Contestant(inputFirst,
 							inputLast, ID, inputTribe, imagePath);
 					contestantsArray[contCount] = newContestant;
 					contCount++;
 					this.writeContestants("contestants.txt");
 					if (imagePath == null) {
 						JOptionPane
 								.showMessageDialog(this,
 										"Contestant added, but you didn't add a picture!");
 					} else {
 						JOptionPane.showMessageDialog(this, "Contestant added");
 					}
 					contLiTable = new ContestantListGUI(contestantsArray,
 							contCount);
 					getContentPane().removeAll();
 					getContentPane().add(quitButton());
 					getContentPane().add(contestantPanel());
 					repaint();
 					validate();
 				}
 			}
 		}
 
 		/** Update Contestant Handler **/
 		if (e.getActionCommand().equals("updateC")) {
 			String inputFirst = textFields_c.getFirstC();
 			String inputLast = textFields_c.getLastC();
 			String inputTribe = textFields_c.getTribe();
 
 			// Validate Text Input Fields
 			if ((inputFirst.length() < 1) || (inputFirst.length() > 20)) {
 				JOptionPane
 						.showMessageDialog(
 								this,
 								"Sorry that's not a valid first name, it must be between 1 and 20 characters long");
 			} else if (!checkValidChars(inputFirst)) {
 				JOptionPane
 						.showMessageDialog(this,
 								"Sorry that's not a valid first name, it must only contain letters");
 			}
 
 			else if ((inputLast.length() < 1) || (inputLast.length() > 20)) {
 				JOptionPane
 						.showMessageDialog(
 								this,
 								"Sorry that's not a valid last name, it must be between 1 and 20 characters long");
 			} else if (!checkValidChars(inputLast)) {
 				JOptionPane
 						.showMessageDialog(this,
 								"Sorry that's not a valid last name, it must only contain letters");
 			}
 
 			else if ((inputTribe.length() < 1) || (inputTribe.length() > 30)) {
 				JOptionPane
 						.showMessageDialog(
 								this,
 								"Sorry that's not a valid tribe name, it must be between 1 and 30 characters long");
 			}
 
 			else if (!checkValidChars(inputTribe)) {
 				JOptionPane
 						.showMessageDialog(this,
 								"Sorry that's not a valid tribe name, it must only contain letters");
 			}
 
 			// When game has started the round eliminated & eliminate contestant
 			// fields will be visible
 			// Validate that the Round Eliminated is filled in appropriately and
 			// set update the contestant record array
 			// TODO
 			// if(e.getActionCommand().equals(eliminateYBtn)) {
 			// if(getRoundEliminated() == 0) {
 			// JOptionPane.showMessageDialog(this,
 			// "Please select the round this contestant was eliminated" );
 			// } else {
 			// setEliminated();
 			// getRoundEliminated();
 			// }
 
 			int position = findContestant(textFields_c.getMenuContsID());
 			if (position == -1) {
 				JOptionPane
 						.showMessageDialog(this,
 								"That contestant does not exist, please check your contestant ID and try again");
 			} else {
 				Contestant tempCont = new Contestant(inputFirst, inputLast,
 						textFields_c.getMenuContsID(), inputTribe, imagePath);
 				contestantsArray[position] = tempCont;
 
 				JOptionPane.showMessageDialog(this,
 						"Contestant has been updated");
 				this.writeContestants("contestants.txt");
 				if(startGame==true){
 				if (eliminateYBtn.isSelected()) {
 					if (roundField.getText() == null
 							|| roundField.getText().trim().equals("")) {
 						JOptionPane
 								.showMessageDialog(
 										this,
 										"To eliminate a contestant, specify the round the contestant is eliminated during");
 					} else {
 						if (Integer.valueOf(roundField.getText()) == 0) {
 							JOptionPane
 									.showMessageDialog(
 											this,
 											"To eliminate a contestant, specify the round the contestant is eliminated during");
 						} else {
 							if (this.eliminateContestant(position,
 									Integer.valueOf(roundField.getText()))) {
 								this.writeContestants("contestants.txt");
 								this.writeRounds("rounds.txt");
 								this.calcEliminationScore();
 								JOptionPane
 										.showMessageDialog(
 												this,
 												"In Round "
 														+ getRoundEliminated()
 														+ "\n"
 														+ textFields_c
 																.getFirstC()
 														+ " "
 														+ textFields_c
 																.getLastC()
 														+ " has been eliminated from Survivor!",
 												"Contestant Record Updated",
 												JOptionPane.PLAIN_MESSAGE);
 							}
 
 						}
 
 					}
 				}
 
 			}
 			}
 			contLiTable = new ContestantListGUI(contestantsArray, contCount);
 			getContentPane().removeAll();
 			getContentPane().add(quitButton());
 			getContentPane().add(contestantPanel());
 			repaint();
 			validate();
 		}
 
 		/** Delete Contestant Handler **/
 		if (e.getActionCommand().equals("deleteC")) {
 			String inputFirst = textFields_c.getFirstC();
 			String inputLast = textFields_c.getLastC();
 			String inputTribe = textFields_c.getTribe();
 
 			if (getStartGame() == true) {
 				JOptionPane.showMessageDialog(this,
 						"You cannot add/delete anymore players",
 						"Game has started", JOptionPane.ERROR_MESSAGE);
 			} else {
 				if ((inputFirst.length() < 1) || (inputFirst.length() > 20)) {
 					JOptionPane
 							.showMessageDialog(
 									this,
 									"Sorry that's not a valid first name, it must be between 1 and 20 characters long");
 				} else if (!checkValidChars(inputFirst)) {
 					JOptionPane
 							.showMessageDialog(this,
 									"Sorry that's not a valid first name, it must only contain letters");
 				}
 
 				else if ((inputLast.length() < 1) || (inputLast.length() > 20)) {
 					JOptionPane
 							.showMessageDialog(
 									this,
 									"Sorry that's not a valid last name, it must be between 1 and 20 characters long");
 				} else if (!checkValidChars(inputLast)) {
 					JOptionPane
 							.showMessageDialog(this,
 									"Sorry that's not a valid last name, it must only contain letters");
 				}
 
 				else if ((inputTribe.length() < 1)
 						|| (inputTribe.length() > 30)) {
 					JOptionPane
 							.showMessageDialog(
 									this,
 									"Sorry that's not a valid tribe name, it must be between 1 and 30 characters long");
 				}
 
 				else if (!checkValidChars(inputTribe)) {
 					JOptionPane
 							.showMessageDialog(this,
 									"Sorry that's not a valid tribe name, it must only contain letters");
 				}
 
 				else {
 					int position = findContestant(textFields_c.getMenuContsID());
 					if (position == -1) {
 						JOptionPane
 								.showMessageDialog(this,
 										"That contestant does not exist, please check your entry and try again");
 					} else {
 						contestantsArray[position] = null;
 						for (int i = position; i < contestantsArray.length - 1; i++) {
 							contestantsArray[i] = contestantsArray[i + 1];
 						}
 						JOptionPane.showMessageDialog(this, "" + inputFirst
 								+ " has been deleted");
 
 					}
 					contCount--;
 					this.writeContestants("contestants.txt");
 					contLiTable = new ContestantListGUI(contestantsArray,
 							contCount);
 					getContentPane().removeAll();
 					getContentPane().add(quitButton());
 					getContentPane().add(contestantPanel());
 					repaint();
 					validate();
 				}
 			}
 		}
 
 		/** Reset Fields Handler **/
 		if (e.getActionCommand().equals("reset")) {
 			String firstFieldC = textFields_c.getFirstC();
 			String lastFieldC = textFields_c.getLastC();
 			String tribeField = textFields_c.getTribe();
 
 			String firstFieldP = textFields_p.getTribe();
 			String lastFieldP = textFields_p.getTribe();
 
 			if (firstFieldP != "" || lastFieldP != " ") {
 				textFields_p.setFirstP("");
 				textFields_p.setLastP("");
 			}
 			if (firstFieldC != "" || lastFieldC != " ") {
 				textFields_c.setFirstC("");
 				textFields_c.setLastC("");
 				textFields_c.setTribe("");
 			}
 		}
 		/** Quit Button Handler **/
 		if (e.getActionCommand().equals("q")) {
 			quitBtn.setBorder(BorderFactory.createEmptyBorder());
 			System.exit(0);
 		}
 		/** Create New Game Button Handler **/
 		if (e.getActionCommand().equals("new")) {
 
 		}
 		/** Main Menu Button Handler **/
 		if (e.getActionCommand().equals("main")) {
 			getContentPane().removeAll();
 			getContentPane().add(mainScreen());
 
 			repaint();
 			validate();
 		}
 		/** Players Add/Modify Panel Button Handler **/
 		if (e.getActionCommand().equals("players")) {
 			getContentPane().removeAll();
 
 			getContentPane().add(quitButton());
 
 			getContentPane().add(playersPanel());
 
 			repaint();
 			validate();
 		}
 		/** Contestants List Button Handler **/
 		if (e.getActionCommand().equals("contestants")) {
 			getContentPane().removeAll();
 
 			getContentPane().add(quitButton());
 			getContentPane().add(contListPanel());
 
 			repaint();
 			validate();
 		}
 		/** Contestants List Button Handler **/
 		if (e.getActionCommand().equals("contMod")) {
 			getContentPane().removeAll();
 
 			getContentPane().add(quitButton());
 			getContentPane().add(contestantPanel());
 
 			repaint();
 			validate();
 		}
 		/**
 		 * Standings List Button Handler Enabled only if game has already begun
 		 **/
 		if (e.getActionCommand().equals("standings")) {
 			// if(getStartGame()==true) {
 			getContentPane().removeAll();
 
 			getContentPane().add(quitButton());
 			getContentPane().add(standingsPanel());
 
 			repaint();
 			validate();
 			// }
 			// else
 			// JOptionPane.showMessageDialog(this, "Game has not started yet.",
 			// null, JOptionPane.ERROR_MESSAGE);
 		}
 		/** Bonus Question Admin Button Handler **/
 		if (e.getActionCommand().equals("bonus")) {
 			getContentPane().removeAll();
 
 			getContentPane().add(quitButton());
 			getContentPane().add(bqPanel());
 
 			repaint();
 			revalidate();
 		}
 		/** Start the Game **/
 		if (e.getActionCommand().equals("settings")) {
 
 			if (startGame == true) {
 				JOptionPane
 						.showMessageDialog(
 								this,
 								"Settings can no longer be changed once the game has started. \n The number of players is: "
 										+ playersArray.length
 										+ "\nThe number of contestants is: "
 										+ contCount
 										+ "\nThe amount wager is: "
 										+ wager);
 			} else {
 				getContentPane().removeAll();
 
 				getContentPane().add(quitButton());
 				getContentPane().add(gameSettingsPanel());
 
 				repaint();
 				validate();
 				// If Game Start has confirmed ******** TODO ****************
 				// Disable edit and delete Player/Contestant Forms
 			}
 		}
 		/** Start a new Game **/
 		if (e.getActionCommand().equals("new")) {
 
 		}
 		/** Upload Button Handler **/
 		if (e.getActionCommand().equals("upload")) {
 			imagePath = JOptionPane.showInputDialog(null,
 					"Enter the photo's path : ");
 			ImageIcon contestantImage = createImageIcon(imagePath);
 			contestantPicFrame.setIcon(contestantImage);
 		}
 		/** Reset Game button handler **/
 		if (e.getActionCommand().equals("resetGame")) {
 			// TODO
 		}
 		/**
 		 * Start Game Button Handler When the game starts, TextFields will
 		 * convert to TextAreas that display settings panel TODO
 		 **/
 		if (e.getActionCommand().equals("startGame")) {
 			if (contField.getText() == null
 					|| contField.getText().trim().equals(""))
 				;
 
 			else {
 				if (Integer.valueOf(contField.getText()) < 6
 
 				|| Integer.valueOf(contField.getText()) > 15) {
 					contField.setText(""); // resets the field if the number of
 					// contestants is outside the range
 					JOptionPane.showMessageDialog(this,
 							"Number of Contestants must be between 6 and 15");
 
 				} else {
 					inputNumConts = Integer.valueOf(contField.getText());
 					if (numRounds == 0) {
 						numRounds = inputNumConts - 2;
 						this.rounds = new Round[numRounds];
 						for (int i = 0; i < numRounds; i++)
 							this.rounds[i] = new Round(i + 1);
 						JOptionPane.showMessageDialog(this,
 								"The total number of rounds will be: "
 										+ numRounds);
 					} else {
 						if (numRounds >= (inputNumConts - 2)) {
 							Round[] newRounds = new Round[inputNumConts - 2];
 							for (int i = 0; i < inputNumConts - 2; i++) {
 								newRounds[i] = this.rounds[i];
 							}
 						} else {
 							Round[] newRounds = new Round[inputNumConts - 2];
 							for (int i = 0; i < numRounds; i++) {
 								newRounds[i] = this.rounds[i];
 							}
 							for (int j = numRounds; j < (inputNumConts - 2); j++) {
 								newRounds[j] = new Round(j + 1);
 							}
 
 						}
 						numRounds = inputNumConts - 2;
 						JOptionPane.showMessageDialog(this,
 								"The total number of rounds will be: "
 										+ numRounds);
 					}
 					writeSettings("settings.txt");
 
 				}
 			}
 
 			if (getWager() == 0 && wager == 0) {
 				JOptionPane
 						.showMessageDialog(
 								this,
 								"Please enter the amount of money that each player is going to pitch in.",
 								"No Wager Entered",
 								JOptionPane.INFORMATION_MESSAGE);
 			} else if (inputNumConts == 0)
 				JOptionPane.showMessageDialog(this,
 						"Please enter the number of contestants in the game",
 						"", JOptionPane.INFORMATION_MESSAGE);
 			else if (inputNumConts != contCount) {
 				JOptionPane
 						.showMessageDialog(
 								this,
 								"The game cannot be started until the number of contestants specified have been added to the game",
 								"", JOptionPane.INFORMATION_MESSAGE);
 			} else {
 				// Before the game starts, a dialog box will appear to confirm
 				// settings
 				JOptionPane
 						.showMessageDialog(
 								this,
 								"Are you sure you want to start the game?",
 								"Once you start the gamem you will no longer be able to add/delete players & contestants.",
 								JOptionPane.PLAIN_MESSAGE);
 				writeRounds("rounds.txt");
 				setStartGame(true);
 				if (getWager() != 0) {
 					wager = getWager();
 				}
 				writeSettings("settings.txt");
 			}
 
 		}
 
 		/** Saves the inputted settings before game starts Button Handler **/
 		if (e.getActionCommand().equals("saveSettings")) {
 			if (contField.getText() == null
 					|| contField.getText().trim().equals("")) {
 
 				JOptionPane
 						.showMessageDialog(
 								this,
 								"Please enter the number of contestants. No settings were saved",
 								null, contCount);
 
 			} else {
 
 				if (Integer.valueOf(contField.getText()) < 6
 						|| Integer.valueOf(contField.getText()) > 15) {
 					contField.setText(""); // resets the field if the number of
 					// contestants is outside the range
 					JOptionPane.showMessageDialog(this,
 							"Number of Contestants must be between 6 and 15",
 							null, contCount); // notifies the user of this
 					// requirement
 
 				} else {
 					inputNumConts = Integer.valueOf(contField.getText());
 					if (numRounds == 0) {
 						numRounds = inputNumConts - 2;
 						this.rounds = new Round[numRounds];
 						for (int i = 0; i < numRounds; i++)
 							this.rounds[i] = new Round(i + 1);
 						JOptionPane.showMessageDialog(this,
 								"The total number of rounds will be: "
 										+ numRounds);
 					} else {
 						if (numRounds >= (inputNumConts - 2)) {
 							Round[] newRounds = new Round[inputNumConts - 2];
 							for (int i = 0; i < inputNumConts - 2; i++) {
 								newRounds[i] = this.rounds[i];
 							}
 						} else {
 							Round[] newRounds = new Round[inputNumConts - 2];
 							for (int i = 0; i < numRounds; i++) {
 								newRounds[i] = this.rounds[i];
 							}
 							for (int j = numRounds; j < (inputNumConts - 2); j++) {
 								newRounds[j] = new Round(j + 1);
 							}
 
 						}
 						numRounds = inputNumConts - 2;
 						writeRounds("rounds.txt");
 						JOptionPane.showMessageDialog(this,
 								"The total number of rounds will be: "
 										+ numRounds);
 					}
 					if (getWager() == 0) {
 						JOptionPane
 								.showMessageDialog(
 										this,
 										"Please enter the amount of money that each player is going to pitch in.",
 										"No Wager Entered",
 										JOptionPane.INFORMATION_MESSAGE);
 					} else {
 						wager = getWager();
 					}
 					writeSettings("settings.txt");
 				}
 			}
 		}
 	}
 }
