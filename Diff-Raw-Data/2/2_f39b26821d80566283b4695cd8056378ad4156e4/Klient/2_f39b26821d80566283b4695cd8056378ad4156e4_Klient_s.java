 package klient;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.swing.JFrame;
 
 import klient.Gui.KlientGUI;
 import klient.Nettverk.KlientNettInn;
 import klient.Nettverk.KlientNettUt;
 
 public class Klient extends JFrame implements KeyListener {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		new Klient();
 	}
 
 	KlientGUI gui;
 	KlientNettInn nettInn;
 	KlientNettUt nettUt;
 
 	public Klient()
 	{
 		super("Klient");
 		setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
 		gui = new KlientGUI();
 		setContentPane(gui);
 		setUndecorated(true);
 		pack();
 		setVisible(true);
 		
 		nettInn = new KlientNettInn();
 		nettUt = new KlientNettUt();
 		Thread ni = new Thread(nettInn);
 		Thread nu = new Thread(nettUt);
 		ni.start();
 		nu.start();
 		this.addKeyListener(this);
 
 		run();
 	}
 
 	private void run() {
 		String[] tags = {""};
 		String login = "";
 
 		while(true)
 		{
 			/*String[] nyetags = gui.LesTags();
 			if(erUlike(tags, nyetags))
 				nettUt.send(nyetags);*/
 			nettUt.poke(); // Denne erstattes med kommentert-ut kode ovenfor hvis klienten skal kunne sende tags selv.
 
 			login = gui.sjekkLogin();
 			if(!login.equals(""))
 				nettUt.sendLogin(login);
 				
 			for(int i = 0; i<10; i++)
 			{
 				try {
 					Thread.sleep(500);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 
 				String[] inntags = nettInn.getTags();
 				if(erUlike(inntags, tags) /*&& !erUlike(inntags, nyetags)*/)
 				{
 					gui.GiBilder(nettInn.getURLs());
 					tags = inntags.clone();
 					break;
 				}
 			}
 
 			if(nettInn.erLoginKorrekt())
				gui.login();
 		}
 	}
 
 	private boolean erUlike(String[] tags, String[] nyetags) {
 		if(tags.length != nyetags.length)
 			return false;
 		boolean ret = true;
 		for(int i = 0; i<tags.length; i++)
 		{
 			boolean funnet = false;
 			for(int j = 0; j<tags.length; j++)
 			{
 				if(tags[i].toUpperCase().equals(nyetags[j].toUpperCase()))
 				{
 					funnet = true;
 					break;
 				}
 			}
 			if(!funnet)
 			{
 				ret=false;
 				break;
 			}
 		}
 		return !ret;
 	}
 
 	@Override
 	public void keyPressed(KeyEvent arg0) {
 		if(arg0.getKeyCode() == arg0.VK_ESCAPE)
 			System.exit(0);
 
 	}
 
 	@Override
 	public void keyReleased(KeyEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void keyTyped(KeyEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
