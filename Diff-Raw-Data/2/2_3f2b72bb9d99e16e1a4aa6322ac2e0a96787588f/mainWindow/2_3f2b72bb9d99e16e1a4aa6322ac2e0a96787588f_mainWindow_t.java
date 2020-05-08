 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JLayeredPane;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JList;
 import javax.swing.JSeparator;
 import javax.swing.JTextArea;
 import java.awt.CardLayout;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GraphicsConfiguration;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.awt.GridLayout;
 import java.awt.HeadlessException;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.Transparency;
 
 import javax.swing.JButton;
 import javax.swing.border.BevelBorder;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 import javax.swing.AbstractListModel;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.awt.image.ColorModel;
 import java.awt.image.PixelGrabber;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.Scanner;
 
 import javax.swing.JTable;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 
 
 public class mainWindow {
 
 	/**
 	 * The list of command categories
 	 */
 	final JList groupList = new JList();
 
 	/**
 	 * The entire panel on the left-hand side
 	 */
 	final JList cmdList = new JList();
 	JTextArea mAr, tAr, bAa, bAb;
 	JPanel argPanel,codePanel;
 	private CardLayout cl;
 	JTextArea codeTextArea;
 
 	private double angle = 0;
 	/**
 	 * The main window for the program
 	 */
 	JFrame window;
 	Driver d;
 	int gnum=-1;
 	private JTable rightTable;
 
 	final int RUN_MENU = 0;
 	final int SCAN_MENU = 1;
 	final int BULLET_MENU = 2;
 	final int WALL_MENU = 3;
 
 	private final Font labelFont = new Font(null, 1, 15);
 	/**
 	 * Create the application.
 	 */
 	public mainWindow(Driver d) {
 		this.d = d;
 		initialize();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 
 		Font labelFont = new Font(null, 1, 15);
 		ImageIcon icon = new ImageIcon("images/icon.png");
 		// Setup the window
 		window = new JFrame("RoboCode");
 		window.setSize(new Dimension(1400, 700));
 		window.setLocationRelativeTo(null);
 		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		window.setIconImage(icon.getImage());
 
 
 
 		//----[Begin Left-Hand Panel]----
 
 		int cmdPanelWidth = 200;
 		int cmdPanelHeight = window.getHeight();
 		int groupListHeight = 200;
 		int cmdListHeight = 400;
 		// Setup the panel to hold the left side
 		JPanel cmdPanel = new JPanel();
 		cmdPanel.setPreferredSize(new Dimension(cmdPanelWidth, cmdPanelHeight));
 		cmdPanel.setLayout(new BoxLayout(cmdPanel, BoxLayout.PAGE_AXIS));
 
 		// Setup the list of groups of commands
 
 		JLabel groupListText = new JLabel("Action Categories");
 		groupListText.setFont(labelFont);
 		groupListText.setAlignmentX(Component.LEFT_ALIGNMENT);
 
 		groupList.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseReleased(MouseEvent arg0) {
 				switchCmdList(groupList.getSelectedIndex());
 
 			}
 		});
 
 		groupList.setModel(new AbstractListModel() {
 			private static final long serialVersionUID = 1L;
 			String[] values = new String[] {"Basic Movement", "Basic Turning", "Basic Gun Control", "Advanced Turning"};
 			public int getSize() {
 				return values.length;
 			}
 			public Object getElementAt(int index) {
 				return values[index];
 			}
 		});
 		groupList.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
 		groupList.setPreferredSize(new Dimension(groupListHeight, cmdPanelWidth));
 		groupList.setSelectedIndex(0);
 		groupList.setAlignmentX(Component.LEFT_ALIGNMENT);
 		groupList.setFixedCellWidth(cmdPanelWidth);
 
 
 
 		// Setup the list of commands
 		JLabel cmdListText = new JLabel("Available Actions");
 		//cmdListText.setBounds(10, 145, 200, 20);
 		cmdListText.setFont(labelFont);
 		cmdListText.setAlignmentX(Component.LEFT_ALIGNMENT);
 
 		cmdList.addMouseListener(new MouseAdapter() {
 			public void mouseReleased(MouseEvent me) {
 				switchCmd(groupList.getSelectedIndex(), cmdList.getSelectedIndex());
 			}
 		});
 		cmdList.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
 		cmdList.setAlignmentX(Component.LEFT_ALIGNMENT);
 		cmdList.setPreferredSize(new Dimension(cmdListHeight, cmdPanelWidth));
 		cmdList.setFixedCellWidth(200);
 
 		cmdPanel.add(Box.createRigidArea(new Dimension(0,10)));
 		cmdPanel.add(groupListText);
 		cmdPanel.add(Box.createRigidArea(new Dimension(0,5)));
 		cmdPanel.add(groupList);
 		cmdPanel.add(Box.createRigidArea(new Dimension(0,10)));
 		cmdPanel.add(cmdListText);
 		cmdPanel.add(Box.createRigidArea(new Dimension(0,5)));
 		cmdPanel.add(cmdList);
 		cmdPanel.add(Box.createVerticalGlue());
 		//----[End Left-Hand Panel]----
 
 		//----[Begin Middle Panel]----
 		int controlPanelWidth = 100;
 		int controlPanelHeight = window.getHeight();
 		JPanel addPanel = new JPanel();
 		addPanel.setPreferredSize(new Dimension(controlPanelWidth, controlPanelHeight));
 		addPanel.setLayout(new BoxLayout(addPanel, BoxLayout.PAGE_AXIS));
 
 		
 		JLabel paramText = new JLabel("Parameters");
 		paramText.setFont(labelFont);
 		paramText.setAlignmentX(Component.CENTER_ALIGNMENT);
 
 		//=========================================================START PETER'S CODE
 		JPanel turning, moving, fire, wait, both, blank=new JPanel();
 		turning=new JPanel();
 		JLabel tLb=new JLabel("Degrees: ");
 		tAr=new JTextArea("0",1,4);
 		turning.add(tLb);
 		turning.add(tAr);
 		tAr.getDocument().addDocumentListener(new DocumentListener(){
 
 			@Override
 			public void changedUpdate(DocumentEvent source) {
 
 				Document sourceDocument = source.getDocument();
 				
 				if(sourceDocument.getLength() == 0){
 					angle = 0;
 				}else{
 					try {
 						angle = Math.toRadians(Integer.parseInt(sourceDocument.getText(0, sourceDocument.getLength())));
 					} catch (NumberFormatException e) {
 						// Do nothing
 					} catch (BadLocationException e) {
 						// Do nothing
 					}
 					window.repaint();
 				}
 
 			}
 			@Override
 			public void insertUpdate(DocumentEvent source) {
 				changedUpdate(source);
 
 			}
 
 			@Override
 			public void removeUpdate(DocumentEvent source) {
 				changedUpdate(source);
 			}
 
 		});
 		moving=new JPanel();
 		JLabel mLb=new JLabel("Pixels: ");
 		mAr=new JTextArea("0",1,4);
 		moving.add(mLb);
 		moving.add(mAr);
 
 		both=new JPanel();
 		both.add(new JLabel("Pixels: "));
 		bAa=new JTextArea("0",1,4);
 		both.add(bAa);
 		both.add(new JLabel("Degrees: "));
 		bAb=new JTextArea("0",1,4);
 		both.add(bAb);
 		fire=new JPanel();
 		fire.add(new JLabel("Fire"));
 
 		wait=new JPanel();
 		wait.add(new JLabel("Wait"));
 
 		cl=new CardLayout();
 		argPanel=new JPanel(cl);
 		argPanel.add(blank,"Blank");
 		argPanel.add(turning,"Turning");
 		argPanel.add(moving,"Moving");
 		argPanel.add(both,"Both");
 		argPanel.add(fire,"Fire");
 		argPanel.add(wait,"Wait");
 		argPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
 		argPanel.setMaximumSize(new Dimension(controlPanelWidth, 300));
 
 		switchCmdList(0);
 
 
 
 		//////////////////////
 		//===========================================================END PETER'S CODE
 		
 		JLabel addBtnText = new JLabel("Add Action");
 		addBtnText.setFont(labelFont);
 		addBtnText.setAlignmentX(Component.CENTER_ALIGNMENT);
 
 		JButton addBtn = new JButton(">>");
 		addBtn.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseReleased(MouseEvent evt) {
 				addCmd();
 			}
 		});
 		addBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
 
 		JLabel rmvBtnText = new JLabel("Remove Action");
 		rmvBtnText.setFont(labelFont);
 		rmvBtnText.setAlignmentX(Component.CENTER_ALIGNMENT);
 
 		JButton rmvBtn = new JButton("<<");
 		rmvBtn.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent evt){
 				removeCmd();
 			}
 		});
 		rmvBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
 
 
 		JLabel generateBtnText = new JLabel("Generate Code");
 		generateBtnText.setFont(labelFont);
 		generateBtnText.setAlignmentX(Component.CENTER_ALIGNMENT);
 
 		JButton generateBtn = new JButton("Start!");
 		generateBtn.addMouseListener(new MouseAdapter() {
 			public void mouseReleased(MouseEvent me) {
 				Writer.writeToJavaFile(d.run.data,d.scan.data,d.bullet.data,d.wall.data);
 				addCodeToWindow();
 				
 				JOptionPane.showMessageDialog(null, "Code Generated to D drive","Generation Success", 1);
 				
 				
 			}
 		});
 		generateBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
 		
 		addPanel.add(Box.createRigidArea(new Dimension(0,10)));
 		addPanel.add(programInfo());
 		addPanel.add(Box.createRigidArea(new Dimension(0,10)));
 		addPanel.add(robotDisplay());
 		addPanel.add(Box.createRigidArea(new Dimension(0,10)));
 		addPanel.add(paramText);
 		addPanel.add(Box.createRigidArea(new Dimension(0,5)));
 		addPanel.add(argPanel);
 		addPanel.add(Box.createRigidArea(new Dimension(0,10)));
 		addPanel.add(addBtnText);
 		addPanel.add(Box.createRigidArea(new Dimension(0,5)));
 		addPanel.add(addBtn);
 		addPanel.add(Box.createRigidArea(new Dimension(0,10)));
 		addPanel.add(rmvBtnText);
 		addPanel.add(Box.createRigidArea(new Dimension(0,5)));
 		addPanel.add(rmvBtn);
 		addPanel.add(Box.createRigidArea(new Dimension(0,10)));
 		addPanel.add(generateBtnText);
 		addPanel.add(Box.createRigidArea(new Dimension(0,5)));
 		addPanel.add(generateBtn);
 		addPanel.add(Box.createVerticalGlue());
 		//addPanel.setBorder(BorderFactory.createLineBorder(Color.black));
 		//----[End Middle Panel]----
 
 		//----[Begin Right-Hand Panel]----
 		//addPanel.setBounds(220, 10, 200, 420);
 		//addPanel.setLayout(new BoxLayout(addPanel, BoxLayout.PAGE_AXIS));
 
 		JPanel rightPanel = new JPanel();
 		rightPanel.setPreferredSize(new Dimension(200, 600));
 		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.PAGE_AXIS));
 
 		JLabel modeBtnText = new JLabel("Operating Modes");
 		modeBtnText.setFont(labelFont);
 		modeBtnText.setAlignmentX(Component.LEFT_ALIGNMENT);
 
 		JPanel radioButtons = new JPanel();
 		radioButtons.setLayout(new GridLayout(2, 2));
 		radioButtons.setMaximumSize(new Dimension(250, 50));
 
 		JRadioButton radioRun = new JRadioButton("Standard Behavior");
 		radioRun.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseReleased(MouseEvent arg0) {
 				switchRightList(RUN_MENU);
 			}
 		});
 		radioRun.setSelected(true);
 
 		JRadioButton radioScan = new JRadioButton("Scan Robot");
 		radioScan.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				switchRightList(SCAN_MENU);
 			}
 		});
 
 		JRadioButton radioBullet = new JRadioButton("Hit By Bullet");
 		radioBullet.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				switchRightList(BULLET_MENU);
 			}
 		});
 
 		JRadioButton radioWall = new JRadioButton("Hit Wall");
 		radioWall.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				switchRightList(WALL_MENU);
 			}
 		});
 
 		// Group the radio buttons
 		ButtonGroup methodChoice = new ButtonGroup();
 		methodChoice.add(radioRun);
 		methodChoice.add(radioScan);
 		methodChoice.add(radioBullet);
 		methodChoice.add(radioWall);
 
 		radioButtons.add(radioRun);
 		radioButtons.add(radioScan);
 		radioButtons.add(radioBullet);
 		radioButtons.add(radioWall);
 		radioButtons.setAlignmentX(Component.LEFT_ALIGNMENT);
 
 		JLabel currActionsText = new JLabel("Current Actions");
 		currActionsText.setFont(labelFont);
 
 		String columnNames[] = {"Action", "Parameters"};
 
 		rightTable = new JTable(null, columnNames);
 		rightTable.setModel(d.run);
 		rightTable.getColumnModel().getColumn(0).setResizable(false);
 		rightTable.getColumnModel().getColumn(1).setResizable(false);
 		rightTable.setPreferredScrollableViewportSize(new Dimension(200, 300));
 
 		JScrollPane rightScrollPane = new JScrollPane(rightTable);
 		rightScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
 		//rightScrollPane.setLocation(0, 70);
 		//rightScrollPane.setSize(278, 607);
 
 		rightPanel.add(Box.createRigidArea(new Dimension(0,10)));
 		rightPanel.add(modeBtnText);
 		rightPanel.add(Box.createRigidArea(new Dimension(0,5)));
 		rightPanel.add(radioButtons);
 		rightPanel.add(Box.createRigidArea(new Dimension(0,10)));
 		rightPanel.add(currActionsText);
 		rightPanel.add(Box.createRigidArea(new Dimension(0,5)));
 		rightPanel.add(rightScrollPane);
 		rightPanel.add(Box.createVerticalGlue());
 
 		// Create the live text editor
 		int codePanelWidth = 600;
 		int codeAreaHeight = 400;
 		codePanel = new JPanel();
 		codePanel.setPreferredSize(new Dimension(codePanelWidth,window.getHeight()));
 		codePanel.setLayout(new BoxLayout(codePanel, BoxLayout.PAGE_AXIS));
 		
 		JLabel codePanelText = new JLabel("Generated Code");
 		codePanelText.setFont(labelFont);
 		codePanelText.setAlignmentX(Component.LEFT_ALIGNMENT);
 		
 
 		codeTextArea = new JTextArea();
 		codeTextArea.setEditable(false);
		//codeTextArea.setPreferredSize(new Dimension(codePanelWidth-10, codeAreaHeight-10));
 		codeTextArea.setText("No Actions");
 		
 		codeTextArea.setAlignmentX(Component.LEFT_ALIGNMENT);
 
 		JScrollPane codeScrollPane = new JScrollPane(codeTextArea);
 		codeScrollPane.setPreferredSize(new Dimension(codePanelWidth, codeAreaHeight));
 		
 		codeScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
 		
 		codePanel.add(Box.createRigidArea(new Dimension(0,10)));
 		codePanel.add(codePanelText);
 		codePanel.add(Box.createRigidArea(new Dimension(0,5)));
 		codePanel.add(codeScrollPane);
 		codePanel.add(Box.createVerticalGlue());
 		
 		// Add the panes to the windows
 		Container windowContent = window.getContentPane();
 		windowContent.setLayout(new BoxLayout(windowContent, BoxLayout.LINE_AXIS));
 		
 		windowContent.add(Box.createHorizontalGlue());
 		windowContent.add(cmdPanel);
 		windowContent.add(Box.createRigidArea(new Dimension(10, 0)));
 		windowContent.add(addPanel);
 		windowContent.add(Box.createRigidArea(new Dimension(10, 0)));
 		windowContent.add(rightPanel);
 		windowContent.add(Box.createRigidArea(new Dimension(10, 0)));
 		windowContent.add(codePanel);
 		windowContent.add(Box.createHorizontalGlue());
 		
 
 		window.setJMenuBar(createMenuBar());
 	}
 
 	protected void addCodeToWindow() {
 		
 		File codeFile = new File("D:\\DD_Robot.java");
 		
 		try {
 			Scanner fileScan = new Scanner(codeFile);
 			
 			String fileLine = "";
 			
 			while(fileScan.hasNext()){
 				
 				fileLine += fileScan.nextLine() + "\n";
 				
 			}
 			
 			codeTextArea.setText(fileLine);
 			
 			fileScan.close();
 			
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		codePanel.repaint();
 		
 	}
 
 	private JMenuBar createMenuBar(){
 		JMenuBar menuBar = new JMenuBar();
 
 		JMenu fileMenu = new JMenu("File");
 		fileMenu.setMnemonic(KeyEvent.VK_F);
 
 		JMenuItem fileQuit = new JMenuItem("Quit");
 		fileQuit.setMnemonic(KeyEvent.VK_Q);
 		fileQuit.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if(JOptionPane.showConfirmDialog(window, "Are you sure you want to quit?") == JOptionPane.YES_OPTION){
 					System.exit(0);
 				}
 			}
 
 		});
 
 		JMenuItem fileAbout = new JMenuItem("About");
 		fileAbout.setMnemonic(KeyEvent.VK_A);
 		fileAbout.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				ImageIcon icon = new ImageIcon("images/acmLogo.png");
 
 				JOptionPane.showMessageDialog(window, "RoboGen V1.0\n" +
 						"Written by the members of the MSOE Chapter of ACM\n" +
 						"2011\n\n" +
 						"Designed for use with Robocode\n" +
 						"http://robocode.sourceforge.net/",
 
 						"About RoboGen", JOptionPane.INFORMATION_MESSAGE, icon);
 			}
 
 		});
 
 
 		fileMenu.add(fileAbout);
 		fileMenu.add(new JSeparator());
 		fileMenu.add(fileQuit);
 
 
 
 
 		menuBar.add(fileMenu);
 		return menuBar;
 
 	}
 
 	private JPanel robotDisplay(){
 
 		JPanel returnPanel = new JPanel();
 		returnPanel.setLayout(new BoxLayout(returnPanel, BoxLayout.PAGE_AXIS));
 		returnPanel.setMinimumSize(new Dimension(100, 200));
 
 		try{
 			JLayeredPane robotPane = new JLayeredPane();
 			robotPane.setAlignmentX(Component.CENTER_ALIGNMENT);
 			
 			JLabel robotText = new JLabel("Sample Robot");
 			robotText.setFont(labelFont);
 			robotText.setAlignmentX(Component.CENTER_ALIGNMENT);
 			
 			ImageIcon bodyIcon = new ImageIcon("images/body.png");
 			final BufferedImage turretIcon = toBufferedImage(Toolkit.getDefaultToolkit().createImage("images/turret.png"));
 
 			JLabel bodyLabel = new JLabel(bodyIcon);
 			bodyLabel.setBounds(60, 10, bodyIcon.getIconWidth(), bodyIcon.getIconHeight());
 			bodyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
 			
 			JPanel turretLabel = new JPanel(){
 				private static final long serialVersionUID = 1L;
 
 				@Override
 				public Dimension getPreferredSize(){
 					return new Dimension(turretIcon.getWidth(), turretIcon.getHeight());
 				}
 
 				@Override
 				protected void paintComponent(Graphics g){
 					super.paintComponent(g);
 
 					Graphics2D g2 = (Graphics2D)g;
 
 					g2.rotate(angle, (turretIcon.getWidth() / 2) + 26, (turretIcon.getHeight() / 2));
 					g2.drawImage(turretIcon, 26, 0, null);
 
 				}
 			};
 
 			turretLabel.setBounds(42, 0, 70, 70);
 			turretLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
 			turretLabel.setOpaque(false);
 			
 			robotPane.add(bodyLabel, new Integer(0));
 			robotPane.add(turretLabel, new Integer(1));
 		
 			//turretLabel.setBorder(BorderFactory.createLineBorder(Color.black));
 			
 			returnPanel.add(robotText);
 			returnPanel.add(Box.createRigidArea(new Dimension(0,5)));
 			returnPanel.add(robotPane);
 			//returnPanel.add(Box.createVerticalGlue());
 			//returnPanel.setBorder(BorderFactory.createLineBorder(Color.black));
 		}catch(Exception e){
 
 		}
 		return returnPanel;
 
 	}
 
 	private JPanel programInfo(){
 		ImageIcon icon = new ImageIcon("images/icon.png");
 		
 		JPanel returnPanel = new JPanel();
 		returnPanel.setLayout(new BoxLayout(returnPanel, BoxLayout.PAGE_AXIS));
 		
 		JLabel programIcon = new JLabel(icon);
 		programIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
 
 		JLabel programName = new JLabel("RoboGen");
 		programName.setFont(labelFont);
 		programName.setAlignmentX(Component.CENTER_ALIGNMENT);
 
 		JLabel programVersion = new JLabel("Version 1.0");
 		programVersion.setFont(labelFont);
 		programVersion.setAlignmentX(Component.CENTER_ALIGNMENT);
 		
 		returnPanel.add(programIcon);
 		returnPanel.add(Box.createRigidArea(new Dimension(0, 10)));
 		returnPanel.add(programName);
 		returnPanel.add(programVersion);
 		return returnPanel;
 	}
 	private void switchCmdList(int index) {
 		switch(index) {
 		case 0:
 			cmdList.setModel(d.bMove);
 			break;
 		case 1:
 			cmdList.setModel(d.bTurn);
 			break;
 		case 2:
 			cmdList.setModel(d.bGun);
 			break;
 		case 3:
 			cmdList.setModel(d.aTurn);
 			break;
 		default:
 			cmdList.setModel(d.bMove);
 			break;
 		}
 		cmdList.setSelectedIndex(0);
 		switchCmd(index,0);
 		gnum=index;
 	}
 
 	public void switchCmd(int group, int cmd) {
 		switch(group){
 		case 0:
 			switch(cmd){
 			case 0:
 			case 1:
 				cl.show(argPanel, "Moving");
 				break;
 			case 2:
 				cl.show(argPanel, "Wait");
 				break;
 			}
 			break;
 		case 1:
 			cl.show(argPanel, "Turning");
 			break;
 		case 2:
 			switch(cmd){
 			case 0:
 				cl.show(argPanel, "Fire");
 				break;
 			case 1:
 			case 2:
 			case 3:
 				cl.show(argPanel, "Turning");
 				break;
 			}
 			break;
 		case 3:
 			cl.show(argPanel, "Both");
 			break;
 		}
 	}
 
 	public String getParams() {
 
 		int group=gnum;
 		int cmd=cmdList.getSelectedIndex();
 		switch(group){
 		case 0:
 			if(cmd!=2)
 			{
 				return mAr.getText();
 			}
 			break;
 		case 1:
 			return tAr.getText();
 		case 2:
 			if(cmd!=0){
 				return tAr.getText();
 			}
 			break;
 		case 3:
 			return bAa.getText()+", "+bAb.getText();
 		}
 		return "";
 	}
 
 	private void switchRightList(int menu) {
 		switch(menu) {
 		case RUN_MENU:
 			rightTable.setModel(d.run);
 			break;
 		case SCAN_MENU:
 			rightTable.setModel(d.scan);
 			break;
 		case BULLET_MENU:
 			rightTable.setModel(d.bullet);
 			break;
 		case WALL_MENU:
 			rightTable.setModel(d.wall);
 			break;
 		default:
 			rightTable.setModel(d.run);
 			break;
 		}
 	}
 
 	/**
 	 * Pressing the add button. Gets the method chosen
 	 * and any arguments associated with the method.
 	 */
 	private void addCmd() {
 		rightTableModel rModel = (rightTableModel) rightTable.getModel();
 		String selectedValue = (String)cmdList.getSelectedValue();
 		rModel.addValueAt(selectedValue,getParams());
 		rightTable.repaint();
 	}
 
 	private void removeCmd(){
 		rightTableModel rModel = (rightTableModel) rightTable.getModel();
 		int[] selectedRows = rightTable.getSelectedRows();
 
 		for(int i = 0; i < selectedRows.length; i++){
 			System.out.println("" + i + " - Removing index: " + (selectedRows[i] - i));
 			rModel.removeValueAt(selectedRows[i] - i);
 		}
 
 		rightTable.repaint();
 	}
 
 	// This method returns a buffered image with the contents of an image
 	private static BufferedImage toBufferedImage(Image image) {
 		if (image instanceof BufferedImage) {
 			return (BufferedImage)image;
 		}
 
 		// This code ensures that all the pixels in the image are loaded
 		image = new ImageIcon(image).getImage();
 
 		// Determine if the image has transparent pixels; for this method's
 		// implementation, see Determining If an Image Has Transparent Pixels
 		boolean hasAlpha = hasAlpha(image);
 
 		// Create a buffered image with a format that's compatible with the screen
 		BufferedImage bimage = null;
 		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
 		try {
 			// Determine the type of transparency of the new buffered image
 			int transparency = Transparency.OPAQUE;
 			if (hasAlpha) {
 				transparency = Transparency.BITMASK;
 			}
 
 			// Create the buffered image
 			GraphicsDevice gs = ge.getDefaultScreenDevice();
 			GraphicsConfiguration gc = gs.getDefaultConfiguration();
 			bimage = gc.createCompatibleImage(
 					image.getWidth(null), image.getHeight(null), transparency);
 		} catch (HeadlessException e) {
 			// The system does not have a screen
 		}
 
 		if (bimage == null) {
 			// Create a buffered image using the default color model
 			int type = BufferedImage.TYPE_INT_RGB;
 			if (hasAlpha) {
 				type = BufferedImage.TYPE_INT_ARGB;
 			}
 			bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
 		}
 
 		// Copy image to buffered image
 		Graphics g = bimage.createGraphics();
 
 		// Paint the image onto the buffered image
 		g.drawImage(image, 0, 0, null);
 		g.dispose();
 
 		return bimage;
 	}
 
 	// This method returns true if the specified image has transparent pixels
 	private static boolean hasAlpha(Image image) {
 		// If buffered image, the color model is readily available
 		if (image instanceof BufferedImage) {
 			BufferedImage bimage = (BufferedImage)image;
 			return bimage.getColorModel().hasAlpha();
 		}
 
 		// Use a pixel grabber to retrieve the image's color model;
 		// grabbing a single pixel is usually sufficient
 		PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
 		try {
 			pg.grabPixels();
 		} catch (InterruptedException e) {
 		}
 
 		// Get the image's color model
 		ColorModel cm = pg.getColorModel();
 		return cm.hasAlpha();
 	}
 }
