 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import javax.swing.text.*;
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
 	static final int PREV_HEIGHT       = 100;
 	static final int CHAR_HEIGHT       = 20;
 	static final int CUR_HEIGHT        = 60;
 	static final int CUR_LEFT          = 20;
 	static final int NOTI_HEIGHT       = 20;
 	static final int MARGIN_HEIGHT     = 5;
 	static final int ADDITIONAL_HEIGHT = 100;
 	static final int ADDITIONAL_WIDTH  = 10;
 	static final int TIMER_INTERVAL    = 20;
 	static boolean hasAnimation        = true;
 	static boolean processingCmd       = false;
 	static boolean isInterrupted       = false;
 	static ActionListener updateCanvas = new ActionListener() {
 		public void actionPerformed(ActionEvent evt) {
 			javax.swing.SwingUtilities.invokeLater(new Runnable() {
 				public void run() {
 					LOGOPP.eventQueue.tick();
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
 	static LOGOEventQueue eventQueue = new LOGOEventQueue();
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
 
 		LOGOTurtle tur2 = new LOGOTurtle("tur2");
 		tur2.setShowTurtle(false);
 		canvas.putTurtle(tur2, canvas.getWidth() / 2, canvas.getHeight() / 2);
 		LOGOTurtle tur = new LOGOTurtle("local");
 		canvas.putTurtle(tur, canvas.getWidth() / 2, canvas.getHeight() / 2);
 		canvas.addHistory();
 		
 		LOGOPP.canvas.clearScreen();
 		LOGOPP.io.setStatus("Welcome to LOGO++!");
 		LOGOPP.io.showState();
 
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
 		KeyStroke lParen = KeyStroke.getKeyStroke('(');
 		cur.getInputMap().put(lParen, "none");
 		KeyStroke lCB = KeyStroke.getKeyStroke('{');
 		cur.getInputMap().put(lCB, "none");
 		KeyStroke lB = KeyStroke.getKeyStroke('"');
 		cur.getInputMap().put(lB, "none");
 		cur.setText("");
 		prev.setEditable(false);
 		noti.setEditable(false);
 		noti.setForeground(Color.GRAY);
 		JScrollPane scrollPane1 = new JScrollPane(prev);
 		scrollPane1.setPreferredSize(new Dimension(canvas.getWidth(), PREV_HEIGHT));
 		JScrollPane scrollPane2 = new JScrollPane(cur);
 		scrollPane2.setPreferredSize(new Dimension(canvas.getWidth() - CUR_LEFT, CUR_HEIGHT));
 		JLabel label = new JLabel("Input:");
 		canvas.setBounds(1,1,canvas.getWidth(), canvas.getHeight());
 		canvas.setWindow(this);
 		canvas.repaint();
 		pane.add(scrollPane1);
 		pane.add(scrollPane2);
 		pane.add(noti);
 		pane.add(canvas);
 		pane.add(label);
 		scrollPane1.setBounds(1, canvas.getHeight(), canvas.getWidth(), PREV_HEIGHT);
 		label.setBounds(1,canvas.getHeight() + PREV_HEIGHT, canvas.getWidth(), CHAR_HEIGHT);
 		scrollPane2.setBounds(1, canvas.getHeight() + CHAR_HEIGHT  + PREV_HEIGHT, canvas.getWidth(), CUR_HEIGHT);
 		noti.setBounds(1, canvas.getHeight() + CHAR_HEIGHT + PREV_HEIGHT + CUR_HEIGHT,
 						canvas.getWidth(), NOTI_HEIGHT);
 		noti.setBorder(BorderFactory.createEmptyBorder());
 		noti.setOpaque(false);
 		this.addWindowListener( new WindowAdapter() {
 		    public void windowOpened( WindowEvent e ){
 		        cur.requestFocus();
 		    }
 		}); 
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
 			if (e.getModifiers() == KeyEvent.CTRL_MASK) {
 				int pos = cur.getCaretPosition();
 				String content = cur.getText();
 				if (pos == content.length())
 					cur.append("\n");
 				else
 					cur.setText(content.substring(0,pos) + "\n" + content.substring(pos));
 				cur.setCaretPosition(pos + 1);
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
 		case KeyEvent.VK_G:
 			if (e.getModifiers() == KeyEvent.CTRL_MASK && processingCmd) {
 				hasAnimation = false;
 				LOGOPP.io.setStatus("Processing, please wait.");
 				LOGOPP.io.showState();
 			}
 			break;
 		case KeyEvent.VK_C:
 			if (e.getModifiers() == KeyEvent.CTRL_MASK && processingCmd) {
 				isInterrupted = true;
 				LOGOPP.io.setStatus("Interrupted!");
 				LOGOPP.io.showState();
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
 		case KeyEvent.VK_Q:
 			if (e.getModifiers() == KeyEvent.CTRL_MASK && !processingCmd) {
 				System.exit(0);
 			}
 			break;
 		case KeyEvent.VK_9:
 			if (e.getModifiers() == KeyEvent.SHIFT_MASK && !processingCmd) {
 				int pos = cur.getCaretPosition();
 				String content = cur.getText();
 				if (pos == content.length())
 					cur.append("()");
 				else
 					cur.setText(content.substring(0,pos) + "()" + content.substring(pos));
 				cur.setCaretPosition(pos + 1);
 			}
 			break;
 		case KeyEvent.VK_OPEN_BRACKET :
 			if (e.getModifiers() == KeyEvent.SHIFT_MASK && !processingCmd) {
 				int pos = cur.getCaretPosition();
 				String content = cur.getText();
 				if (pos == content.length())
 					cur.append("{}");
 				else
 					cur.setText(content.substring(0,pos) + "{}" + content.substring(pos));
 				cur.setCaretPosition(pos + 1);
 			}
 			break;
 		case KeyEvent.VK_QUOTE :
 			if (e.getModifiers() == KeyEvent.SHIFT_MASK && !processingCmd) {
 				int pos = cur.getCaretPosition();
 				String content = cur.getText();
 				if (pos == content.length())
 					cur.append("\"\"");
 				else
 					cur.setText(content.substring(0,pos) + "\"\"" + content.substring(pos));
 				cur.setCaretPosition(pos + 1);
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
 			LOGOPP.io.setStatus("Processing...");
 			LOGOPP.io.showState();
 			processingCmd = true;
 			root.run();
 			if (errorhandler.error()) {
 				errorhandler.errorOut();
 				errorhandler.reset();
 				if (isInterrupted) {
 					LOGOPP.eventQueue.interrupt();
 					LOGOPP.canvas.interrupt();
 					isInterrupted = false;
 				}
 			}
 			else {
 				if (!hasAnimation) {
 					LOGOPP.eventQueue.clearAllPending();
 				}
 				else {
 					LOGOPP.eventQueue.clearPending(true);
 				}
 				canvas.repaint();
 			}
 		}
 		catch (Exception e) {
 			if (!errorhandler.error())
 				errorhandler.set("Wrong input (Syntax error)!");
 			errorhandler.errorOut();
 			errorhandler.reset();
 		}
 		finally {
 			LOGOPP.io.setStatus("finished!");
 			LOGOPP.io.showState();
 			isInterrupted = false;
 			processingCmd = false;
 			hasAnimation = true;
 			canvas.addHistory();
 		}
 	}
 }
