 package console;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Font;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.JTextArea;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 
 import java.util.LinkedList;
 
 import console.commands.*;
 
 public class Console extends JTextArea {
   private static final long serialVersionUID = -5603283231339635077L;
 
   int currentOffset = 0;
   int currentLineOffset = 0;
 
   ConsoleKeyListener listener = new ConsoleKeyListener();
 
   String delimiter = " > ";
   String currentLineContent = "";
 
   LinkedList<Command> commands = new LinkedList<Command>();
 
   public Console(int rows, int columns) {
     super(rows, columns);
     addKeyListener(listener);
     setAutoscrolls(true);
     setLineWrap(true);
     setWrapStyleWord(true);
     setFont(new Font("Monaco", 0, 12));
     reset();
     loadCommands();
   }
 
   public void reset() {
     setText("");
     currentOffset = 0;
     write(delimiter);
     currentLineOffset = 0;
     currentLineContent = "";
   }
 
   public synchronized void write(String toWrite) {
     setOffsetToEnd();
 
     try {
       getDocument().insertString(currentOffset, toWrite, null);
     } catch (BadLocationException e) {
       // silence is golden
     }
 
     currentOffset += toWrite.length();
     setCaretPosition(currentOffset);
   }
 
   public synchronized void writeln(String toWrite) {
     writeln(toWrite, true);
   }
 
   public synchronized void writeln(String toWrite, boolean withDelimiter) {
     if (withDelimiter) {
       write(toWrite+"\n"+delimiter);
     } else {
       try {
         int offset = currentOffset - currentLineContent.length()
                                    - delimiter.length();
         getDocument().insertString(offset, toWrite+"\n", null);
       } catch (BadLocationException e) {
         // silence...
       }
       currentOffset += toWrite.length()+1;
       setCaretPosition(currentOffset);
     }
   }
 
   public String getDelimiter() {
     return delimiter;
   }
 
   public void setDelimiter(String delimiter) {
     this.delimiter = delimiter;
   }
 
   public int getCurrentOffset() {
     return currentOffset;
   }
 
   public int getCurrentLineOffset() {
     return currentLineOffset;
   }
 
   public void setOffsetToEnd() {
     Document doc = getDocument();
     currentOffset = doc.getLength();
   }
 
   protected void loadCommands() {
     writeln("Loading commands...", false);
     LinkedList<Class<?>> classes = new LinkedList<Class<?>>();
     try {
      PackageInspector pi = new PackageInspector("console.commands");
      classes = pi.getClasses("Command");
     } catch (Exception e) {
       writeln("  WARNING: there was an error while loading the commands\n"
              +"           this console instance might be useless.", false);
     }
 
     writeln("  Found "+classes.size()+" commands...", false);
     for (Class<?> c : classes) {
       boolean isFine = true;
       try {
         Object newObj = c.newInstance();
         commands.add((Command) newObj);
       } catch(Exception e) {
         isFine = false;
       } finally {
         if (isFine) {
           writeln("  - "+c.getName()+" [OK]", false);
         } else {
           writeln("  - "+c.getName()+" [ERROR]", false);
         }
       }
     }
   }
 
   private boolean isCommand(String cmd) {
     for (Command c : commands)
       if (c.getClass().getName().equals(cmd)) return true;
     return false;
   }
 
   public synchronized void executeCommand() {
     // TODO: give command to world
 
     currentLineOffset = 0;
     currentLineContent = "";
   }
 
   protected class ConsoleKeyListener implements KeyListener {
     public void keyPressed(KeyEvent e) {
       Console c = (Console) e.getSource();
       if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
         if (c.currentLineOffset == 0) {
           e.consume();
         }
       }
     }
 
    public void keyReleased(KeyEvent e) {
       Console c = (Console) e.getSource();
      if (!e.isActionKey())
         switch (e.getKeyCode()) {
           case KeyEvent.VK_ENTER:
             c.executeCommand();
             c.write(c.delimiter);
             break;
 
           case KeyEvent.VK_BACK_SPACE:
             if (c.currentLineOffset > 0) c.currentLineOffset--;
             try {
               c.currentLineContent.substring(0,
                   c.currentLineContent.length()-1);
             } catch (StringIndexOutOfBoundsException e1) {
               // silence is golden
             }
             break;
 
           default:
             c.currentLineOffset++;
             c.currentLineContent += String.valueOf(e.getKeyChar());
         }
      c.currentOffset = c.getCaretPosition();
     }
 
     public void keyTyped(KeyEvent e) {}
   }
 }
