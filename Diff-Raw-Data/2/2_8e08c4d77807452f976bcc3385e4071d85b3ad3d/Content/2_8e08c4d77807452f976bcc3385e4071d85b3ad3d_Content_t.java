 package applets.Abbildungen_I04_2VerschiedeneAbb;
 
 import java.awt.Point;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 public class Content {
 
 	Applet applet;
 	
 	public Content(Applet applet) {
 		this.applet = applet;		
 	}
 	
 	public void init() {
 		applet.setSize(560, 678);
 	}
 
 	public void run() {
 		PZuweisung zuw1 = new PZuweisung(applet);
 		zuw1.dotsCountA = 4;
 		zuw1.dotsCountB = 3;
 		//zuw1.dotBNames = Arrays.asList(new String[] {"a","b","c","d","e","f"});
 		zuw1.reset();
 		
 		PZuweisung zuw2 = new PZuweisung(applet) {
 
 			public boolean isCorrect() {
 				if(!existsConnectionForAllDotsA() || !copySrc.existsConnectionForAllDotsA())
 					return false;
 				
 				return !connectionsAreEqual(copySrc);
 			}
 			
 			public String getResultMsg() {
 				if(!copySrc.existsConnectionForAllDotsA())
 					return "f ist nicht vollständig definiert";
 				if(!existsConnectionForAllDotsA())
 					return "g ist nicht vollständig definiert";
 				
 				if(connectionsAreEqual(copySrc))
 					return "f ist gleich g";
 
 				return "das ist korrekt, f ist ungleich g";
 			}
 		};
 		zuw2.copyFrom(zuw1, false);
 		
 		applet.vtmeta.setExtern(new VisualThing[] {
 				new VTImage("zuw1", 10, 5, 600, 200, zuw1),
 				new VTImage("zuw2", 10, 5, 600, 200, zuw2)
 		});
 		
 	}
 }
