 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import java.util.*;
 import java.awt.geom.AffineTransform;
 import java.awt.image.*;
 import javax.imageio.*;
 import java.io.*;
 
 public class LOGOPP extends JFrame implements KeyListener {
 	static JTextPane prev = new JTextPane();
 	static JTextArea cur = new JTextArea();
 	static JTextArea noti = new JTextArea();
 	static JPanel pane = new JPanel();
 	static final int PREV_HEIGHT = 100;
 	static final int CHAR_HEIGHT = 20;
 	static final int CUR_HEIGHT = 60;
 	static final int NOTI_HEIGHT = 20;
 	static final int MARGIN_HEIGHT = 5;
 	static final int ADDITIONAL_HEIGHT = 100;
 	static final int ADDITIONAL_WIDTH = 10;
 	static final int TIMER_INTERVAL = 10;
 	static boolean hasAnimation = true;
 	static boolean processingCmd = false;
 	static ActionListener updateCanvas = new ActionListener() {
 		public void actionPerformed(ActionEvent evt) {
 			javax.swing.SwingUtilities.invokeLater(new Runnable() {
 				public void run() {
 					LOGOPP.canvas.getCurTurtle().move();
 				}
 			});
 		}
 	};
 	static javax.swing.Timer timer = new javax.swing.Timer(TIMER_INTERVAL, updateCanvas);
 	static String cmd;
 	static LOGOIO io = new LOGOIO();
 	static LOGOSymbolTable symboltable = new LOGOSymbolTable();
 	static LOGOErrorHandler errorhandler = new LOGOErrorHandler(io);
 	static LOGOCanvas canvas = new LOGOCanvas("LOGO++", 600, 400);
 	static LOGOInterpreter interpreter = new LOGOInterpreter();
 	static LOGOBasic basic = new LOGOBasic();
 
 	static ArrayList<String> commandHistory = new ArrayList<String>();
 	static int curCmdIndex = 0;
 	static LOGOChallenge challenge = new LOGOChallenge();
 
 
 	public static void main(String[] args) {
 		javax.swing.SwingUtilities.invokeLater(new Runnable() {
 				public void run() {
 					createAndShowGUI();
 				}
 			});
 	}
 	
 	public LOGOPP(String name) {
 		super(name);
 	}
 
 	private static void createAndShowGUI() {
 		LOGOPP logoPP = new LOGOPP("LOGOGUI");
 		logoPP.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		logoPP.initComponents();
 		logoPP.addComponentsToPane();
 		logoPP.pack();
 		logoPP.setVisible(true);
 		logoPP.changeWindowSize(false);
 		//        logoPP.setResizable(false);
 		LOGOTurtle tur = new LOGOTurtle("local");
 		canvas.putTurtle(tur, canvas.getWidth() / 2, canvas.getHeight() / 2);
 		///////////
 		LOGOTurtle tur2 = new LOGOTurtle("tur2");
 		canvas.putTurtle(tur2, canvas.getWidth() / 2, canvas.getHeight() / 2);
 		canvas.addHistory();
 		noti.setText("Welcome to LOGO++!");
 	
 	}
 
 	private void initComponents() {
 		cur.setText("");
 		cur.setLineWrap(true);
 		cur.addKeyListener(this);
 		pane.setLayout(null);
 		this.add(pane);
 	}
 
 	private void addComponentsToPane() {
 		KeyStroke enter = KeyStroke.getKeyStroke("ENTER");
 		cur.getInputMap().put(enter, "none");
 		prev.setEditable(false);
 		noti.setEditable(false);
 		noti.setForeground(Color.GRAY);
 		JScrollPane scrollPane1 = new JScrollPane(prev);
 		scrollPane1.setPreferredSize(new Dimension(canvas.getWidth(), PREV_HEIGHT));
 		JScrollPane scrollPane2 = new JScrollPane(cur);
 		scrollPane2.setPreferredSize(new Dimension(canvas.getWidth(), CUR_HEIGHT));
 		JScrollPane scrollPane3 = new JScrollPane(noti);
 		scrollPane2.setPreferredSize(new Dimension(canvas.getWidth(), NOTI_HEIGHT));
 		canvas.setBounds(1,1,canvas.getWidth(), canvas.getHeight());
 		canvas.setWindow(this);
 		canvas.repaint();
 		pane.add(scrollPane1);
 		pane.add(scrollPane2);
 		pane.add(scrollPane3);
 		pane.add(canvas);
 		scrollPane1.setBounds(1, canvas.getHeight() + MARGIN_HEIGHT, canvas.getWidth(), PREV_HEIGHT);
 		scrollPane2.setBounds(1, canvas.getHeight() + MARGIN_HEIGHT * 2 + PREV_HEIGHT, 
 				      canvas.getWidth(), CUR_HEIGHT);
 		scrollPane3.setBounds(1, canvas.getHeight() + MARGIN_HEIGHT * 3 + PREV_HEIGHT + CUR_HEIGHT,
 						canvas.getWidth(), NOTI_HEIGHT);
 		pane.revalidate();
 		this.repaint();
 	}
 
 	public void addChallenge() {
 		pane.add(challenge);
 		challenge.setBounds(canvas.getWidth() + MARGIN_HEIGHT, 1,
 				    canvas.getWidth(), canvas.getHeight());
 		pane.revalidate();
 		this.repaint();
 	}
 
 	public void removeChallenge() {
 		pane.remove(challenge);
 		pane.revalidate();
 		this.repaint();
 	}
 
 	public void changeWindowSize(boolean isExpand) {
 		int width = canvas.getWidth() * (isExpand?2:1) + (isExpand?MARGIN_HEIGHT:0)
 			+ ADDITIONAL_WIDTH;
 		int height = canvas.getHeight() + LOGOPP.PREV_HEIGHT + LOGOPP.CUR_HEIGHT + NOTI_HEIGHT
 			+ ADDITIONAL_HEIGHT;
 		setSize(width, height);
 	}
 
 	public void keyTyped(KeyEvent e) {
 	}
     
 	public void keyPressed(KeyEvent e) {
 		switch(e.getKeyCode()) {
 		case KeyEvent.VK_ENTER:
 			//////////////////////
 			if (e.getModifiers() == KeyEvent.CTRL_MASK) {
 				cur.append("\n");
 			} else {
 				LOGOPP.io.in();
 			}
 			break;
 		case KeyEvent.VK_UP:
 			if (e.getModifiers() == KeyEvent.ALT_MASK) {
 				curCmdIndex = (curCmdIndex > 0) ? curCmdIndex - 1 : 0;
 				if (commandHistory.size() > 0 || curCmdIndex > 0) {
 					cur.setText(commandHistory.get(curCmdIndex));
 				}
 			}
 			break;
 		case KeyEvent.VK_DOWN:
 			if (e.getModifiers() == KeyEvent.ALT_MASK) {
 				if (curCmdIndex < commandHistory.size() - 1) {
 					cur.setText(commandHistory.get(curCmdIndex++));
 				} else {
 					cur.setText("");
 					curCmdIndex = commandHistory.size();
 				}
 			}
 			break;
 		case KeyEvent.VK_C:
 			if (e.getModifiers() == KeyEvent.CTRL_MASK && processingCmd) {
 				hasAnimation = false;
 				LOGOPP.io.notify("Now finishing rest work, you may need to wait for a while.");
 			}
 			break;
 		case KeyEvent.VK_Z:
 			if (e.getModifiers() == KeyEvent.CTRL_MASK && !processingCmd) {
 				canvas.undo();
 			}
 			break;
 		case KeyEvent.VK_Y:
 			if (e.getModifiers() == KeyEvent.CTRL_MASK && !processingCmd) {
 				canvas.redo();
 			}
 			break;
 		}
 	}
      
 	public void keyReleased(KeyEvent e) {
 	}
 
 	public static void execute(String str) {
 		if (errorhandler.error())
 			errorhandler.errorOut();
 		errorhandler.reset();
 		try {
 			LOGONode root = interpreter.parse(str);
 			/*if (root == null) {
 				if (errorhandler.error())
 					errorhandler.errorOut();
 				errorhandler.reset();
 				return;
 			}*/
 			
 			LOGOPP.io.notify("Processing commands");
 			processingCmd = true;
 			root.run();
 			if (errorhandler.error()) {
 				errorhandler.errorOut();
 				errorhandler.reset();
 			}
 			else {
 				if (!hasAnimation) {
 					canvas.getCurTurtle().clearAllPending();
 				}
 				else {
 					canvas.getCurTurtle().clearPending(true);
 				}
 				canvas.repaint();
 			}
 		}
 		catch (Exception e) {
 			//errorhandler.set(e.toString());
			errorhandler.set("Invalid command");
 			errorhandler.errorOut();
 			errorhandler.reset();
 		}
 		finally {
 			LOGOPP.io.notify("finished!");
 			processingCmd = false;
 			hasAnimation = true;
 			canvas.addHistory();
 		}
 	}
 }
