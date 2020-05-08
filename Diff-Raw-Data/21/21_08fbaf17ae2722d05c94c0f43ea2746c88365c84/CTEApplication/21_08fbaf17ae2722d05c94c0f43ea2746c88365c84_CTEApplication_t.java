 
 
 import network.*;
 import user.*;
 import gui.*;
 import handler.*;
 import java.util.Observable;
 import static java.lang.System.out;
 
 public class CTEApplication {
     
     public static void main(String[] args) {
         out.println("Begin Main");
         Client _client = new Client();
        _client.setDocument("");
         EditorGUI gui = new EditorGUI(_client);
         _client.addObserver(gui);
         gui.launch();
         //gui.update(_client, _client.getDocument().toString());
     }        
     
 }
