 package by.bsu.samples.memento;
 
 import java.util.Random;
 
 public class Main {
	private static GameState gameState;
	private static Controller controller;
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
        gameState = new GameState();
        controller = new Controller(gameState);
         
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
                    x = Math.abs( rnd.nextInt() % GameState.AREA_SIZE);
                    y = Math.abs( rnd.nextInt() % GameState.AREA_SIZE);
                    nmbr = Math.abs( rnd.nextInt()% GameState.AREA_SIZE + 1 );
                     controller.addElement(x, y, nmbr);
                     System.out.printf("%d added at (%d,%d)\n",nmbr,x,y);
                    gameState.draw();
                     break;
                 case '2':
                     controller.undo();
                    gameState.draw();
                     break;
                 case '3':
                     controller.redo();
                    gameState.draw();
                     break;
                 case '4':
                     processing = false;
                     break;
             }
             System.out.flush();
         }
     }
 
 }
