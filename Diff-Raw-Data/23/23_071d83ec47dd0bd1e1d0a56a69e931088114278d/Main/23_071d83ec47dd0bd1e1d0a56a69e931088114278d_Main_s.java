 package by.bsu.samples.memento;
 
 import java.util.Random;
 
 public class Main {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
        GameState gamestate = new GameState();
        Controller controller = new Controller(gamestate);
         
         Random rnd = new Random();
         Boolean processing = true;
         while( processing ){
             String input = new String();
             System.out.println("Press 1 to emulate adding element, 2 to undo,3 to redo, 4 to exit");
             byte b[] = new byte[1];
             try {
                 System.in.read(b);
             }
             catch (java.io.IOException e ) {
                 System.out.println(e.toString());
                 continue;
             }
             switch(b[0]){
                 case '1':
                     int x,y,nmbr;
                    x = java.lang.Math.abs( rnd.nextInt() % GameState.AREA_SIZE);
                    y = java.lang.Math.abs( rnd.nextInt() % GameState.AREA_SIZE);
                    nmbr =java.lang.Math.abs( rnd.nextInt()% GameState.AREA_SIZE + 1 );
                     controller.addElement(x, y, nmbr);
                     System.out.printf("%d added at (%d,%d)\n",nmbr,x,y);
                    gamestate.draw();
                     break;
                 case '2':
                     controller.undo();
                    gamestate.draw();
                     break;
                 case '3':
                     controller.redo();
                    gamestate.draw();
                     break;
                 case '4':
                     processing = false;
                     break;
             }
             System.out.flush();
         }
     }
 
 }
