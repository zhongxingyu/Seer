 /**
  * Author: Stefan Giermair ( zstegi@gmail.com )
  * 
  * This file is part of ncmjb.
  * ncmjb is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * ncmjb is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with ncmjb.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package mplayeripc.demo;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.util.HashMap;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.JTextField;
 import javax.swing.UIManager;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import mplayeripc.MPlayerBufferedImage;
 import mplayeripc.MPlayerControl;
 import mplayeripc.MPlayerProcess;
 import mplayeripc.MPlayerProcessListener;
 import mplayeripc.MPlayerSharedMemory;
 import mplayeripc.MPlayerControl.Pausing;
 import mplayeripc.MPlayerProcess.Error;
 import mplayeripc.MPlayerSharedMemory.BufferMode;
 
 public class SimplePlayerUI extends JFrame implements WindowListener, ActionListener, ChangeListener, MPlayerProcessListener {
 	
 	final private JTextField mplayerLocation = new JTextField(30);
 	final private JTextField file = new JTextField(30);
 	final private JButton bplay = new JButton("play");
 	final private JButton bstop = new JButton("stop");
 	final private JButton bpause = new JButton("pause");
 	final private JSlider slider = new JSlider();
 	final private JLabel timepos = new JLabel("000:00.00");
 	final private JSlider volume = new JSlider();
 		
 	final private MPlayerSharedMemory msm;
 	private MPlayerControl mpc = null;  
 	private HashMap<String, String> args = new HashMap<String, String>();
 	
 	private static JFrame videoframe = null;
 	
 	private volatile boolean stop = true; 
 
 	public SimplePlayerUI(String mpLocation, String filetoplay, MPlayerSharedMemory msm) {
 		super("SimplePlayerUI");			
 		
 		args.put("-osdlevel", "3");
 		
 		mplayerLocation.setText(mpLocation);
 		mplayerLocation.setEditable(false);
 		file.setText(filetoplay);
 		this.msm = msm;
 		
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.addWindowListener(this);
 		
 		JPanel cpane = new JPanel();
 		this.setContentPane(cpane);
 		cpane.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));
 		Box b = Box.createHorizontalBox();
 		cpane.add(b);
 		b.add(new JLabel("mplayerLocation: "));
 		b.add(mplayerLocation);
 		
 		b = Box.createHorizontalBox();
 		cpane.add(b);
 		b.add(new JLabel("file: "));
 		b.add(file);
 		
 		b = Box.createHorizontalBox();
 		cpane.add(b);
 		b.add(timepos);
 		slider.setValue(0);
 		b.add(slider);
 		slider.addChangeListener(this);
 		
 		b = Box.createHorizontalBox();
 		cpane.add(b);
 		b.add(bplay);
 		b.add(bpause);
 		b.add(bstop);
 		
 		bplay.addActionListener(this);
 		bstop.addActionListener(this);
 		bpause.addActionListener(this);
 		
 		b.add(new JLabel(" Volume:"));
 		volume.setValue(100);
 		b.add(volume);
 		volume.addChangeListener(this);
 		
 		this.pack();
 		this.setVisible(true);		
 		
 		new Thread() {
 			public void run() {
 				while (true) {
 					try {
 						if (!stop && mpc != null) {
 							final float temp = mpc.getTimePos();
 							timepos.setText(String.format("%03d:%02d.%02d", 
 							(int)temp/60, (int)temp%60, (int)(temp*100%100)));
 							slider.setValue(mpc.getPercentPos().intValue());
 						}					
 						Thread.sleep(1000);
 					} catch (Exception e) { 
 						stop = true; 
 						timepos.setText("000:00.00");
 						slider.setValue(0);
 						if (videoframe != null)
 							videoframe.setTitle("");
 						//e.printStackTrace(); 
 					}
 				}
 			}
 		}.start();
 	}	
 	
 	public void pressedPlayButton() {
 		if (mpc == null) {
 			mpc = new MPlayerControl(mplayerLocation.getText(), this, msm, args);
 		}
 		try {
 			if (mpc.isPaused())
 				mpc.play(file.getText());
 			else 
 				mpc.play(file.getText(), Pausing.toogle);			
 			mpc.setVolume(volume.getValue());
 			System.out.println("play");
 			if (mpc.isPaused())
 				mpc.pause();
 			if (videoframe != null)
 				videoframe.setTitle(mpc.getFileName());
 			stop = false;			
 		} catch (Exception e) { 
 			//e.printStackTrace();
 		}
 	}
 	
 	public void pressedStopButton() {
 		stop = true;
 		if (mpc != null)
 			mpc.stop();
 		slider.setValue(0);
 		timepos.setText("000:00.00");
 		if (videoframe != null)
 			videoframe.setTitle("");
 		System.out.println("stop");
 	}
 	
 	public void pressedPauseButton() {
 		if (mpc != null)
 			mpc.pause();
 		System.out.println("pause");
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource().equals(bplay))
 			pressedPlayButton();
 		else if (e.getSource().equals(bstop))
 			pressedStopButton();
 		else if (e.getSource().equals(bpause))
 			pressedPauseButton();
 	}
 	
 	@Override
 	public void stateChanged(ChangeEvent e) {
 		if (mpc != null) {
 			if (e.getSource().equals(slider) && slider.getValueIsAdjusting()){			
 				mpc.setPercentPos(slider.getValue());
 			} else if (e.getSource().equals(volume) && volume.getValueIsAdjusting()) {
 				mpc.setVolume(volume.getValue());
 			}
 		}
 	}
 	
 	@Override
 	public void windowClosing(WindowEvent e) {
 		System.out.println("window is closing");
 		if (mpc != null)
 			mpc.quit();
 		msm.close();	
 	}
 	
 	@Override
 	public void errorOccurred(Error error) {
 		System.out.println(error.getErrorString() == null ? error : error + " - " + error.getErrorString());
 		
 	}
 
 	@Override
 	public void handleMplayerStdOutErr(String line) {
 		// TODO Auto-generated method stub
 		//System.out.println(line);
 		
 	}
 
 	@Override
 	public void processEnded(Integer exitValue) {
 		System.out.println("mplayer process ended");
 		mpc = null;
 		(new File(mplayerLocation.getText())).delete();
 	}	
 	
 	@Override
 	public void windowClosed(WindowEvent e) {
 	}
 
 	@Override
 	public void windowActivated(WindowEvent e) {		
 	}		
 
 	@Override
 	public void windowDeactivated(WindowEvent e) {		
 	}
 
 	@Override
 	public void windowDeiconified(WindowEvent e) {		
 	}
 
 	@Override
 	public void windowIconified(WindowEvent e) {	
 	}
 
 	@Override
 	public void windowOpened(WindowEvent e) {		
 	}
 	
 	public static void main(String[] args) throws Exception {
 		//System.setProperty("sun.java2d.xrender", "True");
 		
 		System.out.println(System.getProperty("java.version"));
 		System.out.println(System.getProperty("java.library.path"));
 		System.out.println(System.getProperty("java.vendor"));
		System.out.println(System.getProperty("os.name"));
		System.out.println(System.getProperty("os.arch"));
		System.out.println(System.getProperty("os.version"));
 		
 		UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel"); 
 		//UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		
 		
 		File mplayer = MPlayerSharedMemory.loadMplayer();
 		
 		final MPlayerBufferedImage mbi = new MPlayerBufferedImage(BufferMode.Double);
 
 		//mbi.scaleVideo(false);
 		//mbi.doAspectCorrection(false);
 		//mbi.centerHorizontal(false);
 		//mbi.centerVertical(false);
 		//mbi.setVideoPosition(50, 50);
 		
 		videoframe = new JFrame() {			
 			public void paint(Graphics g) {				
 				//super.paint(g);	
 				mbi.componentResized();
 				mbi.update();				
 			}
 		};		
 		
 		mbi.setComponent(videoframe.getContentPane());
 		new Thread(mbi).start();	
 
 		SimplePlayerUI spui = new SimplePlayerUI(mplayer.getAbsolutePath(), 
 				System.getProperty("user.home"), mbi);
 		
 		videoframe.addWindowListener(spui);
 		videoframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
 		videoframe.getContentPane().setPreferredSize(new Dimension(848,360));
 		videoframe.pack();		
 		//videoframe.setLocation(0, this.getHeight()+30);
 		videoframe.setVisible(true);
 		videoframe.createBufferStrategy(2);	
 		videoframe.getContentPane().setBackground(Color.BLACK);
 	}	
 }
