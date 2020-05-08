 package com.luzi82.nagatoquery.demo.desktop;
 
 import java.util.concurrent.Executor;
 import java.util.concurrent.Executors;
 
 import javax.swing.JFrame;
 
 import com.luzi82.nagatoquery.NagatoQuery;
 import com.luzi82.nagatoquery.NqSession;
 import com.luzi82.nagatoquery.NqStreamBump;
 import com.luzi82.nagatoquery.UtilCommand;
 import com.luzi82.nagatoquery.demo.GameOfLife;
 import com.luzi82.nagatoquery.demo.GolQuery;
 
 public class Main {
 
 	/**
	 * @param args
 	 */
 	public static void main(String[] argv) {
 		final Executor executor = Executors.newCachedThreadPool();
 
 		final GameOfLife gol = new GameOfLife(executor);
 
 		final GolPanel gp = new GolPanel(gol);
 		gp.setVisible(true);
 		JFrame jf = new JFrame();
 		jf.setContentPane(gp);
 		jf.setVisible(true);
 		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		jf.pack();
 
 		NagatoQuery nq = new NagatoQuery(executor);
 		nq.loadClass(UtilCommand.class);
 		nq.loadClass(NqSession.class);
 		GolQuery.initNq(nq, gol);
 		NqStreamBump.setDefaultPrefix(nq, "GOL> ");
 
 		NagatoQuery.main(nq);
 	}
 
 }
