 package lang;
 
 import java.util.ListResourceBundle;
 
 public class Idat_it extends ListResourceBundle{
   
   private Object[][] contents = { {"language", "Lingua"},
                                   {"french", "fracese"},
                                   {"english", "inglese"},
                                   {"italian", "italiano"},
                                   {"play", "Gioca"},
                                   {"Pause", "Pausa"},
                                  {"option", "Option"},
                                   {"gameover", "GAME OVER !!\nHai perso!"},
                                   {"gamepause", "Gioco in pausa"},
                                   {"gamestop", "Gioco fermato"},
                                   {"score", "Punti"},
                                   {"cntEaten", "Insetti mangiati"},
                                   {"speed", "Velocit"}    
   };
   
   protected Object[][] getContents() {
     return contents;
   }
   
 
 }
