 package gui;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 
 import javax.swing.AbstractAction;
 import javax.swing.KeyStroke;
 import javax.swing.text.BadLocationException;
 
 /**
  * Run commands.
  */
 public class REPLTextArea extends SchemeTextArea {
 	private static final long serialVersionUID = 8753865168892947915L;
 	
 	// Store previously entered commands.
 	List<String> commandHistory;
 	int currentCommand = 0;
 	
 	/**
 	 * Create a new REPL area.
 	 */
 	public REPLTextArea() {
 		commandHistory = new ArrayList<String>();
 		setPreferredSize(new Dimension(100, 100));
 		
 		// When the user hits the 'ENTER' key, check for a complete command.
         code.getInputMap().put(
                 KeyStroke.getKeyStroke("ENTER"),
                 new AbstractAction() {
                     private static final long serialVersionUID = 723647997099071931L;
 
 					public void actionPerformed(ActionEvent e) {
                         Stack<Character> brackets = new Stack<Character>();
                         for (char c : getText().toCharArray()) {
                             if (c == '(') brackets.push(')');
                             else if (c == '[') brackets.push(']');
                             else if (c == ')' || c == ']')
                                 if (!brackets.empty() && brackets.peek() == c)
                                     brackets.pop();
                                 else
                                     return;
                         }
 
                         if (brackets.empty()) {
                         	commandHistory.add(getText());
                         	currentCommand = commandHistory.size();
                         	
                             MainFrame.me().doCommand(getText());
                             setText("");
                         } else {
                             try {
                                 code.getDocument().insertString(code.getCaretPosition(), SchemeTextArea.NL, null);
                             } catch (BadLocationException ble) {
                                 System.err.println("badwolf");
                             }
                             tab();
                         }
                     }
                 });
         
         // When the user hits the up arrow, it they are on the first line, reload the previous command.
         code.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent arg0) {
 				if (arg0.getKeyCode() == KeyEvent.VK_UP) {
 					if (getText().lastIndexOf("\n", code.getCaretPosition()) == -1) {
 						if (currentCommand == commandHistory.size())
 							commandHistory.add(getText());
 						
 						if (currentCommand == 0)
 							return;
 						
 						currentCommand--;
 						setText(commandHistory.get(currentCommand));
 					}
 				}
 				
 				if (arg0.getKeyCode() == KeyEvent.VK_DOWN) {
 					if (getText().indexOf("\n", code.getCaretPosition()) == -1) {
 						if (currentCommand == commandHistory.size()) {
 							return;
 						} else if (currentCommand == commandHistory.size() - 1) {
 							setText(commandHistory.remove(commandHistory.size() - 1));
 						} else {
 							currentCommand++;
 							setText(commandHistory.get(currentCommand));
 						}
 					}
 				}
 			}
 
 			@Override
			public void keyReleased(KeyEvent arg0) {}

			@Override
 			public void keyTyped(KeyEvent arg0) {
 				
 			}
         });
 	}
 }
