 import ui.DrawPanel;
 import ui.Window;
 import life.LifeAlgo;
 import life.LifeDrawer;
 import life.hashlife.*;
 import util.RLE;
import util.RLEtmp;
 
 public class Main {
 	
 	final static BooleanCell f = BooleanCell.off;
 	final static BooleanCell t = BooleanCell.on;
 	
 	static public void main(String[] args) {
 		Window w = new Window();
 		w.setVisible(true);
 		
 		//testHashlife();
 		testDrawer(w.getDrawPanel());
 		//testRLE();
 	}
 	
 	static void testRLE() {
 		
 		int[][] t = RLE.read("media/puffer.rle");
		RLEtmp r = new RLEtmp("media/puffer.rle");
		int[][] t2 = r.cells;
 		
		for(int i=0; i<t.length; i++)
			for(int j=0; j<t[i].length; j++)
				if(t[i][j] != t2[j][i])
					System.out.println(i + " - " + j);
		
		//System.out.println(MacroCell.niceStringFromTab(t));
		//System.out.println(MacroCell.niceStringFromTab(t2));
 		
 	}
 	
 	static void testHashlife() {
 		int[][] t = RLE.read("media/ticker.rle");
 
 		HashLifeState s = new HashLifeState(t);
 		
 		long time = System.currentTimeMillis();
 		s.evolve(1);
 		s.evolve(1);
 		s.evolve(1);
 		s.evolve(1);
 		s.evolve(1);
 		System.out.println(System.currentTimeMillis() - time);
 	}
 	
 	static void testDrawer(DrawPanel drawer) {
 		
 		LifeAlgo a = new HashLifeAlgo();
		RLEtmp t = new RLEtmp("media/puffer.rle");
		a.loadFromArray(t.cells);
		//a.loadFromArray(RLE.read("media/puffer.rle"));
 		
 		drawer.setLifeAlgo(a);
 
 		long time;
 		while(true){
 			time =System.currentTimeMillis();
 			drawer.reDraw();
 			if(System.currentTimeMillis() - time > 50)
 			System.out.println("Draw: " + (System.currentTimeMillis() - time));
 			time =System.currentTimeMillis();
			a.evolve(1);
 			if(System.currentTimeMillis() - time > 50)
 			System.out.println("Evolve: " + (System.currentTimeMillis() - time));
 			
 			/*try {
 				Thread.sleep(100);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}*/
 		}
 	}
 }
