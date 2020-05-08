 package patterns.command;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Command Pattern Demo.
  *
  * Press: w,a,s,d to move the logo around.
  */
 public class Command {
 
     public static void main(String[] args){
         MovementWindow window = new MovementWindow();
         window.build();
     }
 
     /**
     * Invoker.
      */
     public static class MovementInvoker {
 
         void execute(ActionCommand command){
             if(command == null){
                 JOptionPane.showMessageDialog(null, "Up: w\nDown: s\nLeft: a\nRight: d\n", "Commands", JOptionPane.ERROR_MESSAGE);
             } else {
                 command.execute();
             }
         }
     }
 
     /**
     * Client.
      */
     public static class MovementWindow extends JFrame {
 
         private Logo logo;
 
         private Map<Character,ActionCommand> commands = new HashMap<Character,ActionCommand>();
 
         void build() {
             try {
                 this.logo = new Logo();
             } catch (MalformedURLException e) {
                 throw new IllegalStateException(e);
             }
 
             final MovementInvoker invoker = new MovementInvoker();
 
             commands.put('a', new MoveLeftCommand(this.logo));
             commands.put('d', new MoveRightCommand(this.logo));
             commands.put('w', new MoveUpCommand(this.logo));
             commands.put('s', new MoveDownCommand(this.logo));
 
             this.addKeyListener(new KeyAdapter() {
                 @Override
                 public void keyTyped(KeyEvent e) {
                     ActionCommand command = commands.get(e.getKeyChar());
                     invoker.execute(command);
                     repaint();
                 }
             });
 
             JPanel panel = new JPanel(){
                 public void paint(Graphics g) {
                     super.paint(g);
 
                     Graphics2D g2d = (Graphics2D)g;
                     g2d.drawImage(logo.getImage(), logo.x, logo.y, this);
                 }
             };
 
             this.add(panel);
 
             this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
             this.setSize(400, 300);
             this.setLocationRelativeTo(null);
             this.setTitle("Command Pattern Demo");
             this.setVisible(true);
         }
     }
 
     /**
     * Receiver.
      */
     public static class Logo extends ImageIcon {
 
         private int x;
         private int y;
 
         Logo() throws MalformedURLException {
             super(new URL("http://www.msse.umn.edu/sites/all/themes/umsec/images/umseclogo.gif"));
         }
     }
 
     //**********//
     // Commands //
     //**********//
     interface ActionCommand {
         void execute();
     }
 
     static abstract class AbstractMoveCommand implements ActionCommand {
         final protected int STEP = 10;
         Logo logo;
         AbstractMoveCommand(Logo logo){
             super();
             this.logo = logo;
         }
     }
 
     static class MoveUpCommand extends AbstractMoveCommand {
         MoveUpCommand(Logo logo){
             super(logo);
         }
 
         @Override
         public void execute() {
             logo.y -= STEP;
         }
     }
 
     static class MoveDownCommand extends AbstractMoveCommand {
         MoveDownCommand(Logo logo){
             super(logo);
         }
 
         @Override
         public void execute() {
             logo.y += STEP;
         }
     }
 
     static class MoveLeftCommand extends AbstractMoveCommand {
         MoveLeftCommand(Logo logo){
             super(logo);
         }
 
         @Override
         public void execute() {
             logo.x -= STEP;
         }
     }
 
     static class MoveRightCommand extends AbstractMoveCommand {
         MoveRightCommand(Logo logo){
             super(logo);
         }
 
         @Override
         public void execute() {
             logo.x += STEP;
         }
     }
 
 }
