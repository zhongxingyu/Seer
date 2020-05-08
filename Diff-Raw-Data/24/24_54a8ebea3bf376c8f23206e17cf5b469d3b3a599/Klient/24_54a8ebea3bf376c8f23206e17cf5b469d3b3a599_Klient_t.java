 package klient;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.swing.JFrame;
 
 public class Klient extends JFrame {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		new Klient();
 	}
 	
 	
 	KlientGUI gui;
 	
 	public Klient()
 	{
 		super("Klient");
 		setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
 		gui = new KlientGUI();
 		setContentPane(gui);
 	    pack();
 	    setVisible(true);
 	    
 	    run();
 	}
 
 	private void run() {
 		URL[] linker = new URL[3];
 		try {
 			linker[0] = new URL("http://www.wallng.com/images/2013/08/image-explosion-colors-background-beautiful-263613.jpg");
 			linker[1] = new URL("http://www.nasa.gov/images/content/693952main_pia15817-full_full.jpg");
 			linker[2] = new URL("file:///C:/Users/Bruker/desktop/2012-12-10_03.12.03.png");
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 		gui.GiBilder(linker);
 		
 		try {
			Thread.sleep(60000);
 		} catch (InterruptedException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		linker = new URL[5];
 		try {
 			linker[0] = new URL("http://images5.fanpop.com/image/photos/28000000/randomised-random-28065165-1024-819.jpg");
 			linker[1] = new URL("http://www.miataturbo.net/attachments/insert-bs-here-4/90179-random-pictures-thread-sfw-gets-horse-random-random-31108109-500-502-jpg?dateline=1380373426");
 			linker[2] = new URL("http://images2.wikia.nocookie.net/__cb20090724010737/wikiality/images/c/c7/BlankSnowyOrlyOwl.jpg");
 			linker[3] = new URL("http://www.pulp-paperworld.com/images/stories/BASF/Amflora1_EN.jpg");
 			linker[4] = new URL("http://bloggfiler.no/charlotte1987.blogg.no/images/430486-6-1261950736602.jpg");
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 		gui.GiBilder(linker);
 	}
 
 }
