 package game;
 
 import javax.swing.*;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.IOException;
 
 public class ParamsUI extends JFrame {
 
 			/**
 	 * The script textbox could be bigger
 	 */
 	private static final long serialVersionUID = 1L;
 		JLabel header,fish,good,bad,good1,bad1,sound,visual,subjectid,experimenterid,soundType,videoType,gameType,mintime,maxtime,scrip;
 		JLabel hit,miss,total,ghit,gmiss,bhit,bmiss,gtot,btot,htot,mtot,tot;
 		JPanel matrix1,matrix2,matrix3,matrix4,matrix5,matrix6;
 		JButton button1;
 		JPanel col1,col2,col3,row1,row2,row3;
 		//JTextField gs,bs,gc,bc;
 		int slow,fast,min,max,gh,gms,bh,bm,gt,bt,mt,ht,t;
 		int[] times;
 		String[] speeds,videotypes,gametypes,soundtypes;
 		//JSpinner gs,bs,gv,bv,vidtype,gamtype,stype,mintim,maxtim;
 		JComboBox gs,bs,gv,bv,vidtype,gamtype,stype;
 		String SubjectID,ExperimenterID,script,type;
 		JTextField expId,subId,scr,mintim,maxtim,numfish;
 		//JCheckBox ster;
 		JFileChooser fc;
 		JButton start;
 		JButton stop;
 		GameModel gm;
 	//	SpinnerModel minmodel,maxmodel;
 		JScrollPane jsp;
 		
 		JTextField goodVisualHzTF = new JTextField("6");
 		JTextField badVisualHzTF = new JTextField("8");
 		
 		
 		JLabel minSizeLab = new JLabel("Min Size(%)");
 		JTextField minSizeTF = new JTextField("100");
 		JLabel maxSizeLab = new JLabel("Max Size(%)");
 		JTextField maxSizeTF = new JTextField("120");
 		
 		JTextField goodSoundTF = new JTextField("sounds/fish6hz0p");
 		JTextField badSoundTF = new JTextField("sounds/fish8hz0p");
 		
 	
 		
 	
 	public ParamsUI(final GameModel gm, final DrawDemo gameView) {
 		
 		super("Parameters");
 		this.gm=gm;
 		this.setSize(500,500);
 		
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		this.setLayout(new GridLayout(3,2));
 		
 		fc = new JFileChooser();
 		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
 		
 		expId= new JTextField("Experimenter");
 		subId = new JTextField("Subject");
 		scr= new JTextField("demoscript.txt");
 		mintim = new JTextField("20");
 		maxtim = new JTextField("60");
 		numfish= new JTextField("# of fish generated");
 		
 		header = new JLabel("Settings");
 		fish=new JLabel("Fish Type:");
 		good=new JLabel("Good");
 		bad=new JLabel("Bad");
 		good1=new JLabel("Good");
 		bad1=new JLabel("Bad");
 		sound=new JLabel("Sound");
 		visual=new JLabel("Visual");
 		soundType=new JLabel("Sound Type: ");
 		videoType=new JLabel("Video Type: ");
 		gameType=new JLabel("Game Type: ");
 		mintime=new JLabel("Min Time: ");
 		maxtime= new JLabel("Max Time: ");
 		scrip = new JLabel("ScriptFile: ");	
 		
 		
 		
 		hit= new JLabel("Hits");
 		miss = new JLabel("Misses:");
 		ghit = new JLabel(gh+"");
 		bhit = new JLabel(bh+"");
 		gmiss = new JLabel(gms+"");
 		bmiss = new JLabel(bm+"");
 		gtot = new JLabel(gt+"");
 		btot = new JLabel(bt+"");
 		htot = new JLabel(ht+"");
 		mtot = new JLabel(mt+"");
 		tot = new JLabel(t+"");
 		total = new JLabel("Total");
 		
 		
 		min = 20;
 		max = 60;
 
 		
 		
 	
 
 		
 		speeds=new String[]{"none","slow","fast"};
 		
 /*		final SpinnerModel speedmodel0 = new SpinnerListModel(speeds);
 		final SpinnerModel speedmodel1 = new SpinnerListModel(speeds);
 		final SpinnerModel speedmodel2 = new SpinnerListModel(speeds);
 		final SpinnerModel speedmodel3 = new SpinnerListModel(speeds);
 	*/	
 		videotypes=new String[]{"Throb","Flicker"};
 	
 	//	SpinnerModel videoTypeModel = new SpinnerListModel(videotypes);
 		vidtype = new JComboBox(videotypes);
 		
 		gametypes=new String[]{"Generated","Scripted"};
 	//	final SpinnerModel gameTypeModel = new SpinnerListModel(gametypes);
 		gamtype = new JComboBox(gametypes);
 		gamtype.setSelectedIndex(1);
 		soundtypes=new String[]{"Stereo","Mono"};
 	//	SpinnerModel soundTypeModel = new SpinnerListModel(soundtypes);
 		stype = new JComboBox(soundtypes);
 		
 		gs=new JComboBox(speeds);
 		gs.setSelectedIndex(1);
 		bs=new JComboBox(speeds);
 		bs.setSelectedIndex(2);
 		gv=new JComboBox(speeds);
 		gv.setSelectedIndex(1);
 		bv=new JComboBox(speeds);
 		bv.setSelectedIndex(2);
 		
 		JTextArea textArea = new JTextArea(5, 20);
 		jsp = new JScrollPane(textArea); 
 		textArea.setEditable(true);
 		
 		// the start button will look at the values of the params interface and change the game model accordingly
 		start= new JButton("start");
 
 		start.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e){
 				
 				GameActor.GAME_START = System.nanoTime();
 				
 				gm.mRate = (int) Double.parseDouble(mintim.getText());
 				gm.sRate = (int) Double.parseDouble(maxtim.getText());
 				
 				gm.visualMin = (int) Double.parseDouble(minSizeTF.getText());
 				gm.visualMax = (int) Double.parseDouble(maxSizeTF.getText());
 			
 				script=scr.getText();
 				SubjectID=subId.getText();
 				ExperimenterID=expId.getText();
 				if (gamtype.getSelectedItem().toString().equals("Scripted")){
 					gm.scripted=true;
 					type = "Scripted";
 					gm.inputScriptFileName = script;
 					//gm.typescript=script;
 				} else {
 					gm.scripted=false;
 					type = "Random";
 				}
 				
 				gm.goodFishSounds = goodSoundTF.getText();
 				gm.badFishSounds = badSoundTF.getText();
 				
 				if (gs.getSelectedItem().toString().equals("fast")){
 					gm.goodaudiohz=fast;
 				} else if (gs.getSelectedItem().toString().equals("slow")){
 					gm.goodaudiohz=slow;
 				}else if (gs.getSelectedItem().toString().equals("none")){
 					gm.goodaudiohz=0;
 				}
 				if (bs.getSelectedItem().toString().equals("fast")){
 					gm.badaudiohz=fast;
 				} else if (bs.getSelectedItem().toString().equals("slow")){
 					gm.badaudiohz=slow;
 				}else if (bs.getSelectedItem().toString().equals("none")){
 					gm.badaudiohz=0;
 				}
 				
 				gm.goodvisualhz = (int)Double.parseDouble(goodVisualHzTF.getText());
 				gm.badvisualhz = (int)Double.parseDouble(badVisualHzTF.getText());
 				
 				
 				/*
 				if (gv.getSelectedItem().toString().equals("fast")){
 					gm.goodvisualhz=fast;
 				} else if (gv.getSelectedItem().toString().equals("slow")){
 					gm.goodvisualhz=slow;
 				}else if (gv.getSelectedItem().toString().equals("none")){
 					gm.goodvisualhz=0;
 				}
 				if (bv.getSelectedItem().toString().equals("fast")){
 					gm.badvisualhz=fast;
 				} else if (bv.getSelectedItem().toString().equals("slow")){
 					gm.badvisualhz=slow;
 				}else if (bv.getSelectedItem().toString().equals("none")){
 					gm.badvisualhz=0;
 				}
 				*/
 
 			
 				
 				System.out.println(gm.badaudiohz);
 				System.out.println(gm.badvisualhz);
 				System.out.println(gm.goodaudiohz);
 				System.out.println(gm.goodvisualhz);
 				
 	//			slow = min;
 				System.out.println(script);
 				System.out.println(gm.mRate);
 				try {
					gm.logfile.write("Version: 1.0 (7/11/2013)" + "\n" +
 									 "Experimenter:           "+ ExperimenterID + "\n" + 
 				                     "Subject:                " + SubjectID + "\n" + 
 				                     "Date:                   "+ (new java.util.Date()).toString()+"\n"+
 							         "Game Type:              "+ type + "\n"+
 				                     "Script File:            " + gm.inputScriptFileName + "\n"+
 				                     "Good Sounds:            "+ gm.goodFishSounds + "\n"+
 							         "Bad Sounds:             " + gm.badFishSounds +"\n"+
 				                     "Good Visual Hertz:      " +gm.goodvisualhz +"\n"+
 				                     "Bad Visual Hertz:       " +gm.badvisualhz +"\n"+
 				                     "Visual Min Scale:       "+ gm.visualMin + "\n"+ 
 				                     "Visual Max Scale:       "+ gm.visualMin + "\n"+
 				                     "Min Delay:              "+ gm.mRate+"\n"+
 							         "Max Delay:              "+gm.sRate+"\n" + "\nstart\n");
 					gm.logfile.flush();
 				} catch (IOException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 				gm.start();
 				GameLoop gl = new GameLoop(gm,gameView.gameboard);
 				Thread t = new Thread(gl);
 				t.start();
 	
 				/*
 				// Finally, show the actual game window!
 				gameView.frame.setVisible(true);
 				GameLoop gl = new GameLoop(gm,gameView.gameboard);
 				Thread t = new Thread(gl);
 				System.out.println("gameloop");
 				t.start();
 				gameView.gameboard.requestFocus();
 				*/
 			
 			}
 		});
 
 		stop = new JButton("stop");
 		
 		stop.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e){
 				gm.stop();
 			}
 		});
 		
 		col1 = new JPanel();
 		col1.setLayout(new GridLayout(3,1));
 		col2 = new JPanel();
 		col2.setLayout(new GridLayout(3,1));
 		col3 = new JPanel();
 		col3.setLayout(new GridLayout(3,1));
 		
 		row1 = new JPanel();
 		row1.setLayout(new GridLayout(1,3));
 		row2 = new JPanel();
 		row2.setLayout(new GridLayout(1,3));
 		row3 = new JPanel();
 		row3.setLayout(new GridLayout(1,3));
 		
 		matrix1 = new JPanel();
 		matrix1.setLayout(new GridLayout(1,3));
 		matrix2 = new JPanel();
 		matrix2.setLayout(new GridLayout(3,1));
 		
 		matrix3 = new JPanel();
 		matrix3.setLayout(new GridLayout(5,2));
 		
 		matrix4 = new JPanel();
 		matrix4.setLayout(new GridLayout(3,1));
 		
 		matrix5 = new JPanel();
 		matrix5.setLayout(new GridLayout(4,4));
 		
 		matrix6 = new JPanel();
 		
 		JPanel blank=new JPanel();
 		JPanel blank1=new JPanel();
 
 		
 /*		button1 = new JButton();
 		button1.setText("Push Me");
 		button1.addActionListener(
 				new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				System.out.println("Yay I love being pushed");
 			}
 		});
 		
 		*/
 		
 		col1.add(fish);
 		col1.add(good);
 		col1.add(bad);
 		
 		col2.add(sound);
 		//col2.add(gs);
 		//col2.add(bs);
 		col2.add(this.goodSoundTF);
 		col2.add(this.badSoundTF);
 		
 		col3.add(visual);
 		//col3.add(gv);
 		//col3.add(bv);
 		col3.add(this.goodVisualHzTF);
 		col3.add(this.badVisualHzTF);
 		
 		col1.setBackground(Color.cyan);
 		col2.setBackground(Color.cyan);
 		col3.setBackground(Color.cyan);
 		
 		
 		row1.add(soundType);
 		row1.add(stype);
 		
 		row2.add(videoType);
 		row2.add(vidtype);
 		
 		row3.add(gameType);
 		row3.add(gamtype);
 		
 		
 		matrix1.add(col1);
 		matrix1.add(col2);
 		matrix1.add(col3);
 		
 		matrix2.add(row1);
 		matrix2.add(row2);
 		matrix2.add(row3);
 		
 		
 
 		matrix3.setBorder(javax.swing.BorderFactory.createTitledBorder("Gen and Vis") );
 		matrix3.add(mintime);
 		matrix3.add(mintim);
 		matrix3.add(maxtime);
 		matrix3.add(maxtim);
 		matrix3.add(scrip);
 		matrix3.add(scr);
 		matrix3.add(minSizeLab);
 		matrix3.add(minSizeTF);
 		matrix3.add(maxSizeLab);
 		matrix3.add(maxSizeTF);
 		
 		matrix4.setBorder(javax.swing.BorderFactory.createTitledBorder("Main Control") );
 		matrix4.add(subId);
 		matrix4.add(expId);
 		matrix4.add(start);
 		matrix4.add(stop);
 		matrix4.add(numfish);
 		
 		matrix5.add(blank);
 		matrix5.add(hit);
 		matrix5.add(miss);
 		matrix5.add(total);
 		matrix5.add(good1);
 		matrix5.add(ghit);
 		matrix5.add(gmiss);
 		matrix5.add(gtot);
 		matrix5.add(bad1);
 		matrix5.add(bhit);
 		matrix5.add(bmiss);
 		matrix5.add(btot);
 		matrix5.add(blank1);
 		matrix5.add(htot);
 		matrix5.add(mtot);
 		matrix5.add(tot);
 		
 		matrix6.add(jsp);
 		matrix1.setBackground(Color.red);
 		matrix2.setBackground(Color.green);
 		matrix3.setBackground(Color.blue);
 		matrix4.setBackground(Color.gray);
 		matrix5.setBackground(Color.pink);
 		matrix6.setBackground(Color.yellow);
 		matrix6.setBorder(javax.swing.BorderFactory.createTitledBorder("jsp") );
 		
 		this.add(matrix1);
 		this.add(matrix2);
 		this.add(matrix3);
 		this.add(matrix4);
 		//this.add(matrix5);
 		//this.add(matrix6);
 	}
 }
