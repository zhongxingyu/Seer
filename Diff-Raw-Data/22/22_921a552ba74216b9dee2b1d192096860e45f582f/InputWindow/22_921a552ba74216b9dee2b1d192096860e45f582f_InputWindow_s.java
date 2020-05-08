 package FRC_Score_Sys;
 
 import java.awt.Color;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 
 public class InputWindow extends JFrame {
 	private static final long	serialVersionUID	= 1;
 	private MainMenu			myParent;
 
 	Inputwindow_ScorePanel		RedPanel;
 	Inputwindow_ScorePanel		BluePanel;
 
 	String						MatchNumber			= "Unk";
 
 	JTextField					WinnerDisplay;
 
 	Color						color_red			= new Color(255, 106, 0);
 	Color						color_blue			= new Color(30, 144, 255);
 	Color						color_yellow		= new Color(242, 255, 0);
 	boolean						loaded				= false;
 
 	InputWindow(MainMenu parent, String MatchNumber) {
 		myParent = parent;
 		addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent e) {
 				TellParent("im_closing", null);
 			}
 		});
 		System.out.println("Input Window for Match #" + MatchNumber + " Starting.");
 		setResizable(false);
 		setAlwaysOnTop(true);
 		setTitle("Input Match Results - Match " + MatchNumber);
 
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		getContentPane().setLayout(gridBagLayout);
 
 		SingleMatch BlueMatch = new SingleMatch();
 		SingleMatch RedMatch = new SingleMatch();
 		try {
 			List<SingleMatch> DBScores = myParent.CommHandle.SqlTalk.FetchMatch(MatchNumber);
 			if (DBScores.size() == 2) {
 				for (SingleMatch Match : DBScores) {
 					if (Match.aColor() == "R") {
 						RedMatch = Match;
 					}
 					if (Match.aColor() == "B") {
 						BlueMatch = Match;
 					}
 				}

 			} else {
				System.out.println("We didn't get both sides of the score?");
				pullThePlug();
 			}
 		} catch (Exception e) {
 			System.out.println("Unable to fetch match for DB");
 			pullThePlug();
 		}
 		// MAIN PANEL WHERE SCORES TALLY
 		JPanel MainPanel = new JPanel();
 		BluePanel = new Inputwindow_ScorePanel(this, color_blue, BlueMatch);
 		MainPanel.add(BluePanel);
 		RedPanel = new Inputwindow_ScorePanel(this, color_red, RedMatch);
 		MainPanel.add(RedPanel);

 		GridBagConstraints gbc_MainPanel = new GridBagConstraints();
 		gbc_MainPanel.fill = GridBagConstraints.BOTH;
 		gbc_MainPanel.insets = new Insets(0, 0, 5, 0);
 		gbc_MainPanel.gridx = 0;
 		gbc_MainPanel.gridy = 0;
 		getContentPane().add(MainPanel, gbc_MainPanel);
 
 		// BUTTONS PANEL
 		JPanel BtnsPanel = new JPanel();
 		BtnsPanel.setLayout(new GridLayout(0, 3, 0, 0));
 		JButton SaveBtn = new JButton("Save & Close");
 		SaveBtn.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				// BUTTON PRESSED
 				// Redo Calc to be sure we're up to date.
 				System.out.println("Pending an Input Window Close let's do one more Calculate, in case something changed.");
 				DoCalc();
 				// Save
 				System.out.println("Requesting Save to DB...");
 				// TODO: WRITE TO DATABASE!
 				// TODO: Need to be sure this error checks so we know if it was
 				// actually written or not.
 				System.out.println("Nothing was saved because this doesn't work yet.");
 
 				// Close Window
 				System.out.println("Input Window Close request pending..");
 				pullThePlug();
 
 			}
 		});
 		BtnsPanel.add(SaveBtn);
 
 		WinnerDisplay = new JTextField();
 		WinnerDisplay.setHorizontalAlignment(SwingConstants.CENTER);
 		CheckWinner(0, 0);
 		BtnsPanel.add(WinnerDisplay);
 
 		JButton RefBtn = new JButton("Refresh Calculations");
 		RefBtn.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				// BUTTON PRESSED
 				DoCalc();
 			}
 		});
 		BtnsPanel.add(RefBtn);
 
 		JButton ResetBtn = new JButton("Reset");
 		ResetBtn.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				// BUTTON PRESSED
 				RedPanel.RequestClear();
 				BluePanel.RequestClear();
 				DoCalc();
 			}
 		});
 		BtnsPanel.add(ResetBtn);
 
 		GridBagConstraints gbc_BtnsPanel = new GridBagConstraints();
 		gbc_BtnsPanel.fill = GridBagConstraints.CENTER;
 		gbc_BtnsPanel.gridx = 0;
 		gbc_BtnsPanel.gridy = 1;
 		getContentPane().add(BtnsPanel, gbc_BtnsPanel);
		loaded = true;
 		DoCalc();
 	}
 
 	private void CheckWinner(int Red, int Blue) {
 		System.out.println("CheckWinner Function processing..");
 		if (Red == Blue) {
 			WinnerDisplay.setText("DRAW");
 			WinnerDisplay.setBackground(color_yellow);
 			System.out.println("  WINNER: DRAW");
 		}
 		if (Red > Blue) {
 			WinnerDisplay.setText("RED WINS");
 			WinnerDisplay.setBackground(color_red);
 			System.out.println("  WINNER: RED");
 		}
 		if (Blue > Red) {
 			WinnerDisplay.setText("BLUE WINS");
 			WinnerDisplay.setBackground(color_blue);
 			System.out.println("  WINNER: BLUE");
 		}
 	}
 
 	public void DoCalc() {
 		if (loaded) {
 			System.out.println("Starting Calc Routine..");
 			int rPen = RedPanel.GetPenalties();
 			int bPen = BluePanel.GetPenalties();
 			int rFinal = RedPanel.DoRefresh(bPen);
 			int bFinal = BluePanel.DoRefresh(rPen);
 			CheckWinner(rFinal, bFinal);
 			System.out.println("FULL CALC REUTINE COMPLETE.");
 		}
 	}
 
 	public void pullThePlug() {
 		WindowEvent wev = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
 		dispatchEvent(wev);
 	}
 
 	private void TellParent(String Msg, Object Datagram) {
 		myParent.RecvChildWindowMsg(this, Msg, Datagram);
 	}
 }
