 import bsh.Interpreter;
 import javax.swing.*;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.ArrayList;
 
 public class ScriptWindow extends Frame implements KeyListener, ActionListener {
 
     private JTextField input;
     private JTextArea output;
     private JScrollPane scrollPane;
     private String playerName = "Player";
 
     private ArrayList<String> history = new ArrayList<String>();
     private int historyIndex = 0;
 
     private Interpreter interpreter = new Interpreter();
 
     public ScriptWindow(String starttext) {
         setTitle("Minecraft Hax");
         setLayout(new BorderLayout());
         output = new JTextArea();
         output.setEditable(false);
         output.setLineWrap(true);
         output.setWrapStyleWord(true);
         output.setText(starttext + "\n");
         scrollPane = new JScrollPane(output);
         scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
         input = new JTextField();
         input.addActionListener(this);
         input.addKeyListener(this);
         add(scrollPane, BorderLayout.CENTER);
         add(input, BorderLayout.SOUTH);
         pack();
         setSize(300, 200);
         addObject("scriptwindow", this, false);
         addObject("interpreter", interpreter, false);
     }
 
     public void addline(String line) {
         output.append(line + "\n");
         output.setCaretPosition(output.getDocument().getLength());
     }
 
     public void nameSetting(String name) {
         if ((name == null) || name.isEmpty())
             return;
         playerName = name;
     }
 
     public boolean addObject(String name, Object obj, boolean cl) {
         try {
             interpreter.set(name, obj);
             addline("# Added: " + name);
             if (cl) {
                 interpreter.setClassLoader(obj.getClass().getClassLoader());
                 addline("% Classloader: " + name);
                interpreter.eval("game.j.b = \"" + playerName + "\"");
             }
             return true;
         } catch (Exception e) {
             addline("@ Error adding " + name + " - " + e.toString());
         }
         return false;
     }
 
     public void actionPerformed(ActionEvent event) {
         final String in = input.getText();
         history.add(in);
         historyIndex = history.size();
         addline(">> " + in);
         Thread t = new Thread(){
             public void run() {
                 Object o = null;
                 try {
                     o = interpreter.eval(in);
                     if (o != null)
                         addline("<< " + o.toString());
                     else
                         addline("<<!");
                 } catch (Exception e) {
                     addline("@ " + e.toString());
                 }
             }
         };
         t.start();
         input.setText("");
     }
 
     public void keyPressed(KeyEvent e) {
         if (history.size() == 0)
             return;
 
         boolean changed = false;
 
         switch (e.getKeyCode()) {
             case KeyEvent.VK_UP:
                 if (historyIndex > 0)
                     historyIndex--;
                 else
                     historyIndex = 0;
                 changed = true;
                 break;
 
             case KeyEvent.VK_DOWN:
                 if (historyIndex < history.size() - 1)
                     historyIndex++;
                 else
                     historyIndex = history.size() - 1;
                 changed = true;
                 break;
         }
 
         if (changed)
             input.setText(history.get(historyIndex));
     }
 
     public void keyTyped(KeyEvent e) {
     }
 
     public void keyReleased(KeyEvent e) {
     }
 
 }
