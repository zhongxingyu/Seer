 /**
  * This class builds the GUI for the PING application window
  */
 
package com.hackathon.ping;
 
 import javax.swing.*;
import com.hackathon.ping.objects.PINGButton;
 import com.sun.org.apache.xerces.internal.impl.RevalidationHandler;
 
 import java.awt.*;
 import java.awt.event.*;
 
 /**
  * @author river226
  */
 
 public class PingMainFrame extends JFrame {
 	JPanel base, menu, wiki, repo, account;
 	PINGButton winex, winmax, winmin;
 	Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
 	int w = 900;
 	int h = 600;
 	int x = (dim.width-w)/2;
 	int y = (dim.height-h)/2;
 	boolean drag = false;
 
 	public PingMainFrame() {
 		super("Ping: PING is not Git");
 		
 		base = new JPanel();
 		menu = new JPanel();
 		wiki = new JPanel();
 		base.setBackground(Color.WHITE);
 
 		setUndecorated(true);
 		setResizable(false);
 		setSize(w, h);
 		setLocation(x, y);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		
 		
 		base.setLayout(null);		
 		buildWinOpArea();
 		buildMenu();
 		buildPanels();
 		
 		base.add(menu);
 		base.add(wiki);
 		base.add(repo);
 		base.add(account);
 		
 		add(base);
 		addMouseMotionListener(new MouseMotionListener() {
 			int xx, yy;
 			@Override
 			public void mouseDragged(MouseEvent e) {
 				x = e.getX();
 				y = e.getY();
 				
 				setLocation(x, y);
 //				repaint();
 			}
 			@Override
 			public void mouseMoved(MouseEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		
 	}
 	
 	private void buildPanels() {
 		account = new JPanel();
 		account.setBounds((int)((w*0.2)+10), 50, (int)((w*0.3)-10), h-60);
 		account.setBackground(Color.BLACK);
 		account.setLayout(new BoxLayout(account, BoxLayout.Y_AXIS));
 		buildAccounts();
 		
 		repo = new JPanel();
 		repo.setLayout(new BoxLayout(repo, BoxLayout.Y_AXIS));
 		repo.setBounds(10, 50, (int)((w*0.2)-10), h-60);
 		repo.setBackground(Color.RED);
 		repo.setLayout(new BoxLayout(repo, BoxLayout.Y_AXIS));
 		
 		wiki = new JPanel();
 		wiki.setBounds((int)((w/2)+10), 50, (int)((w/2)-20), h-60);
 		wiki.setBackground(Color.LIGHT_GRAY);
 		wiki.setLayout(new BoxLayout(wiki, BoxLayout.Y_AXIS));
 	}
 
 	private void buildRepos() {
 		// TODO
 		
 	}
 
 	private void buildAccounts() {
 		// TODO
 		
 	}
 
 	private void buildMenu() {
 		JMenu settings = new JMenu("Settings");
 		settings.add(new JMenuItem("Add Account"));
 		settings.add(new JMenuItem("Edit Account"));
 		settings.add(new JMenuItem("Add Local Repo"));
 		settings.setBackground(Color.WHITE);
 		settings.setBorderPainted(false);
 		settings.setEnabled(true);
 		// To be completed 
 		
 		menu = new JPanel();
 		menu.setBackground(Color.WHITE);
 		menu.setBounds(0, 0, 75, 40);
 		
 		menu.add(settings);
 	}
 	
 	private void buildRepo() {
 		// TODO
 	}
 
 	private void buildWinOpArea() {
 		
 		ImageIcon iExit = new ImageIcon("icons/exit.png");
 		System.out.print(iExit.getIconHeight());
 		base.setBounds(w-120, 0, 120, 40);
 		
 		winex = new PINGButton("X");
 		winex.setBounds(w-40,0, 40, 40);
 		winmax = new PINGButton("O");
 		winmax.setBounds(w-80,0, 40, 40);
 		winmin = new PINGButton("_"); // Minimize Button
 		winmin.setBounds(w-120,0, 40, 40);
 		winex.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) { System.exit(0); }
 		});
 		base.setAlignmentY(w);
 		base.add(winex);
 		//winmin.addActionListener(new ActionListener(){
 		//	public void actionPerformed(ActionEvent arg0) { Frame.setState(Frame.ICONIFIED) }
 		//});
 		//base.add(winmin);
 		// Other buttons to be added with necessary functionality for them is set. 
 	}
 }
