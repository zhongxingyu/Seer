 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import java.util.*;
 
 public class LOGOPP extends JFrame implements KeyListener {
 	JTextArea prev;
     JTextField cur;
     static final int PREV_HEIGHT = 100;
     static final int CUR_HEIGHT = 25;
     static final int MARGIN_HEIGHT = 5;
     String cmd;
 	static LOGOIO io = new LOGOIO();
 	static LOGOSymbolTable symboltable = new LOGOSymbolTable();
 	static LOGOErrorHandler errorhandler = new LOGOErrorHandler(io);
 	static LOGOCanvas canvas = new LOGOCanvas("LOGO++", 600, 400);
 	static LOGOInterpreter interpreter = new LOGOInterpreter();
 	static LOGOBasic basic = new LOGOBasic();
 	static HashMap<String, Object> variableTable = new HashMap<String, Object>();
 	//TODO:static HashMap<String, LOGOFunc> functionTable = new HashMap<String, LOGOFunc>();
 
     static ArrayList<String> commandHistory = new ArrayList<String>();
     static int curCmdIndex = 0;
 
 
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
         logoPP.addComponentsToPane();
         logoPP.pack();
         logoPP.setSize(canvas.getWidth() + 2 * MARGIN_HEIGHT ,canvas.getHeight() 
         				+ PREV_HEIGHT +  CUR_HEIGHT * 2 + MARGIN_HEIGHT * 2);
         logoPP.setVisible(true);
         LOGOTurtle tur = new LOGOTurtle("local", canvas.getWidth() / 2, canvas.getHeight() / 2);
         canvas.putTurtle(tur);
         ///////////
         LOGOTurtle tur2 = new LOGOTurtle("tur2", canvas.getWidth() / 2, canvas.getHeight() / 2);
         canvas.putTurtle(tur2);
     }
 
     private void addComponentsToPane() {
         cur = new JTextField(20);
         cur.addKeyListener(this);
         prev = new JTextArea();
         prev.setEditable(false);
         JScrollPane scrollPane = new JScrollPane(prev);
         scrollPane.setPreferredSize(new Dimension(canvas.getWidth(), 200));
         getContentPane().add(cur);
         getContentPane().add(scrollPane);
         getContentPane().add(canvas);
         scrollPane.setBounds(1, canvas.getHeight() + MARGIN_HEIGHT, canvas.getWidth(), PREV_HEIGHT);
 		cur.setBounds(1, canvas.getHeight() + PREV_HEIGHT + 2 * MARGIN_HEIGHT, 
 						canvas.getWidth(), CUR_HEIGHT);
 		repaint();
     }
 
     public void keyTyped(KeyEvent e) {
     }
      
     public void keyPressed(KeyEvent e) {
         switch(e.getKeyCode()) {
         case KeyEvent.VK_ENTER:
             //////////////////////
            if (cur.getText().length() >=5 && cur.getText().substring(0,5).equals("tur2:")) {
                 cmd = cur.getText().substring(5);
                 canvas.changeToTurtle("tur2");
             } else {
                 cmd = cur.getText();
                 canvas.changeToTurtle("local");
             }
             new Thread(){
                 public void run() {
                     execute(cmd);
                 }
             }.start();
             commandHistory.add(cur.getText());
             curCmdIndex++;
             prev.append(cur.getText()+"\n");
             cur.setText("");
             break;
         case KeyEvent.VK_UP:
             curCmdIndex = (curCmdIndex > 0) ? curCmdIndex - 1 : 0;
             cur.setText(commandHistory.get(curCmdIndex));
             break;
         case KeyEvent.VK_DOWN:
             if (curCmdIndex < commandHistory.size() - 1) {
                 cur.setText(commandHistory.get(curCmdIndex++));
             } else {
                 cur.setText("");
                 curCmdIndex = commandHistory.size();
             }
             break;
         }
     }
      
     public void keyReleased(KeyEvent e) {
     }
 
 	public void execute(String str) {
         if (errorhandler.error())
             errorhandler.errorOut();
 		errorhandler.reset();
 		try {
 			LOGONode root = interpreter.parse(str);
 			root.run();
             canvas.repaint();
 		}
 		catch (Exception e) {
 			errorhandler.set(e.toString());
 		}
 	}
 }
