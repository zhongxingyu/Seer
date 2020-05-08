 package gui;
 
import handler.*;

 public class EditorLauncher {

   public static void main ( String[] args ) {
      EditorGUI gui = new EditorGUI(new Client());
      gui.launch();
   }
 
}
