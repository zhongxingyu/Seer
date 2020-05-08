 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.event.KeyEvent;
 import java.io.File;
 
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.UIManager.LookAndFeelInfo;
 import javax.swing.border.LineBorder;
 import javax.swing.plaf.FileChooserUI;
 
 import model.spielautomat;
 
 
 public class Playfield extends JPanel{
 	private static final long serialVersionUID = 1L;
 	JPanel panelCreditPrize, panelSlotMachine, panelStartStopBtn;
 	JLabel credit, prize;
 	JTextField creditText, prizeText;
 	JButton startBtn, stopBtn;
 	JLabel[] automat = new JLabel[4]; 
 	spielautomat game;
 	Integer[] numerics;
 	RollingNumericsThread rollingNumericsThread;
 	PubModeThread pubModeThread;
 	
 	MainWindow mainWindow;
 	
 	UIManager uiManager;
 	LookAndFeelInfo lookAndFeelInfo[];
 
 	Playfield(MainWindow mainWindow){
 		this.mainWindow = mainWindow;
 		
 		this.lookAndFeelInfo = UIManager.getInstalledLookAndFeels();
 //		this.setLookAndFeel(this.lookAndFeelInfo[3].getClassName());
 			
 		this.game = spielautomat.getInstance();
 		this.game.addspielListener( new mySpielautomatListener(this));
 
 		this.rollingNumericsThread = new RollingNumericsThread(this);
 		this.pubModeThread = new PubModeThread(this);
 		
 		this.panelCreditPrize = new JPanel();
 		this.panelSlotMachine = new JPanel();
 		this.panelStartStopBtn = new JPanel();
 		
 		this.setLayout( new BorderLayout() );
 		
 		//First Panel - Credits, Prize
 		this.panelCreditPrize.setLayout( new GridLayout(2,2) );
 		
 		this.credit = new JLabel("Credit");
 		this.prize = new JLabel("Prize");
 		
 		this.creditText = new JTextField( Integer.toString( spielautomat.STARTGUTHABEN ) );
 		this.creditText.setFont( new Font("Arial", Font.PLAIN ,20));
 		this.creditText.setEditable(false);
 		this.prizeText = new JTextField("0");
 		this.prizeText.setFont( new Font("Arial", Font.PLAIN ,20));
 		this.prizeText.setEditable(false);
 		
 		this.panelCreditPrize.add(this.credit);
 		this.panelCreditPrize.add(this.prize);
 		this.panelCreditPrize.add(this.creditText);
 		this.panelCreditPrize.add(this.prizeText);
 		
 		//Second Panel - slot machine
 		this.panelSlotMachine.setLayout( new GridBagLayout() );
 		this.numerics = this.game.getZiffern();
 		
 		for(int i = 0; i < 4; i++){
 			automat[i] = new JLabel(this.numerics[i].toString(), JLabel.CENTER);
 			automat[i].setPreferredSize( new Dimension(50, 50) );
 			automat[i].setFont( new Font("Arial", Font.PLAIN, 28));
 			automat[i].setBorder( new LineBorder(Color.RED, 3) );
 
 			this.panelSlotMachine.add(automat[i]);
 		}
 		
 		//Third Panel - start/stop buttons
 		this.startBtn = new JButton("start");
 		this.startBtn.setMnemonic(KeyEvent.VK_S);
 		this.stopBtn = new JButton("stop");
 		this.stopBtn.setMnemonic(KeyEvent.VK_O);
 		this.startBtn.addActionListener( new ButtonListener(this) );
 		this.stopBtn.addActionListener( new ButtonListener(this) );
 		
 		this.panelStartStopBtn.add(this.startBtn);
 		this.panelStartStopBtn.add(this.stopBtn);
 		
 		//add the three panels to playfield
 		this.add(this.panelCreditPrize, BorderLayout.NORTH);
 		this.add(this.panelSlotMachine, BorderLayout.CENTER);
 		this.add(this.panelStartStopBtn, BorderLayout.SOUTH);
 				
 		System.out.println("Position x/y" + this.getLocation().x + " " + this.getLocation().y);
 	}
 	
 	public void startGame(){
 		this.game.startRollen();
 	}
 	
 	public void stopGame(){
 		this.game.stopRollen();
 		this.mainWindow.statusWindow.increaseRoundCount();
 	}
 	
 	public void printRollingNumerics(){
		this.numerics = this.game.getZiffern();
 		System.out.println("numerics: " +  numerics[0] + numerics[1] + numerics[2] + numerics[3] );
 
 		for(int i = 0; i < 4; i++){
 			this.automat[i].setText(Integer.toString(numerics[i]));
 		}
 	}
 	
 	public void createNewPubModeThread(){
 		this.pubModeThread = new PubModeThread(this);
 	}
 	
 	public void updateCreditPrize(){
 		this.creditText.setText( Integer.toString( this.game.getGuthaben() ) );
 		this.prizeText.setText( Integer.toString( this.game.getGewinn() ) );
 	}
 	
 	public void setLookAndFeel(String _lookClassName){
 		System.out.println("setLookAndFeel: " + _lookClassName);
 		
 		try{
 			UIManager.setLookAndFeel(_lookClassName);
 			SwingUtilities.updateComponentTreeUI(this);
 		}catch(Exception e){
 			JOptionPane.showMessageDialog(this, "Fehler LookAndFeel");
 		};
 		
 	}
 	
 	public void showJOption(){
 		/**
 		 * JOptionPane in all flavors.
 		 * Only modal.
 		 */
 //ToDo:		JOptionPane.showOptionDialog(this, "OptionDialog", "Titel", JOptionPane.YES_NO_OPTION, JOptionPane.MESSAGE_TYPE_PROPERTY, null, options, initialValue)
 //		JOptionPane.showMessageDialog(this, "MessageDialog");
 		JOptionPane.showConfirmDialog(this, "ConfirmDialog", "Titel", JOptionPane.YES_NO_CANCEL_OPTION );
 //		JOptionPane.showInputDialog(this, "InputDialog");
 	}
 	
 	public void showJDialog(){
 		/*
 		 * JDialog in all flavors.
 		 * modal or non-modal
 		 */
 		JDialog dialog = new JDialog();
 				
 		JPanel panelBtn = new JPanel();
 		panelBtn.setLayout(new GridLayout(1, 3));
 		panelBtn.add(new JButton("Abort"));
 		panelBtn.add(new JButton("No"));
 		panelBtn.add(new JButton("Yes"));
 		
 		dialog.add(new JLabel("label"), BorderLayout.NORTH);
 		dialog.add(panelBtn, BorderLayout.SOUTH);
 		dialog.setLocation(this.getLocation().x + 350, this.getLocation().y + 200);
 		dialog.setVisible(true);
 		dialog.pack();
 	}
 	
 	public void showJFileChooser(){
 
 		/**
 		 * 
 		 * returnValues: 
 		 * JFileChooser.APPROVE_OPTION = 0,
 		 * JFileChooser.ERROR_OPTION,
 		 * JFileChooser.CANCEL_OPTION = (1);
 		 * 
 		 */
 		JFileChooser fileChooser = new JFileChooser(new File("/home/norman"));
 
 		int fileValue = fileChooser.showOpenDialog(this); //fileChooser.showDialog(this, "machDoch");
 		System.out.println("fileValue: " + fileValue);
 		if (fileValue == JFileChooser.APPROVE_OPTION){
 			System.out.println("Approve Option pressed");
 		}
 
 	}
 	
 
 	
 }
