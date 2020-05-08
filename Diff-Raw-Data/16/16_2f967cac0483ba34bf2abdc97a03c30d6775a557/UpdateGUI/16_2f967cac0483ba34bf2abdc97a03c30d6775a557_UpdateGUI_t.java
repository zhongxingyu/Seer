 package com.Jessy1237.McVS;
 
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.FileInputStream;
 import java.io.IOException;
 
 import javax.swing.*;
 
 @SuppressWarnings("serial")
 public class UpdateGUI extends JFrame{
 	public JButton yes;
 	public JButton SH;
 	public int SHb = 1;
 	public JButton no;
 	public JLabel a;
 	
 	public UpdateGUI(){
 		SHb = 1;
 		this.setTitle("Minecraft Version Switcher: Update");
 		this.setResizable(false);
 		this.setSize(new Dimension(480, 135));
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.setLayout(null);
 		this.setLocationRelativeTo(null);
 		
 		SH = new JButton();
 		SH.setFont(new Font("ARIAL", Font.PLAIN, 14));
 		SH.setText("Show Bugfixes");
 		SH.setBounds(165, 30, 150, 30);
 		
 		yes = new JButton();
 		yes.setText("Yes");
 		yes.setBounds(102, 70, 60, 30);
 		
 		no = new JButton();
 		no.setText("No");
 		no.setBounds(318, 70, 60, 30);
 		
 		a = new JLabel();
 		a.setFont(new Font("ARIAL", Font.PLAIN, 14));
 		a.setBounds(45, 5, 460, 20);
 
 		SH.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				if(SHb == 1){
 					try{
 					FileInputStream in = new FileInputStream(Update.ver);
 					Update.prop.load(in);
 					Update.desc = Update.prop.getProperty("desc");
 					in.close();
 					}catch(IOException e1){
 						e1.printStackTrace();
 					}
 				}
 				UpdateBugfixGUI u = new UpdateBugfixGUI();
 				u.JTA.setText("Bugfixes: " + "\n " + Update.desc);
 				u.setVisible(true);
 			}
 		});
 		
 		yes.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				dLoadJars.v = "McVS";
 				Download.c = 0;
 				setVisible(false);
 				Download p = new Download();
 				p.setVisible(true);
				Update.ver.delete();
				Update.dir.delete();
 			}
 		});
 		
 		no.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				Update.ver.delete();
 				Update.dir.delete();
 				setVisible(false);
 				McVS o = new McVS();
 				o.setVisible(true);
 			}
 		});
 		
 		add(a);
 		add(SH);
 		add(yes);
 		add(no);
 	}
 }
