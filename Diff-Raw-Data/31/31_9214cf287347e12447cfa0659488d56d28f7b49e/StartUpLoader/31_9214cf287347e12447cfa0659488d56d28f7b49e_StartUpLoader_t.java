 package com.gui.start;
 
 import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
 
import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 import javax.swing.JProgressBar;
 
import com.gui.ImagePanel;

 public class StartUpLoader extends JFrame implements Runnable{
 
 	private static final long serialVersionUID = 261490374486851708L;
 	public boolean done = false;
 	private int procent = 0;
 	private String comment = "loading";
 	private JProgressBar progress;
 	private static StartUpLoader instance = null;
 	
 	public static StartUpLoader getInstance() {
 		if(instance == null) {
 			return null;
 		} else {
 			return instance;
 		}
 	}
 	
 	public StartUpLoader() {
 		instance = this;
 		setUndecorated(true);
 		setSize(500, 350);
 		setLocationRelativeTo(null);
 		progress = new JProgressBar();
 		progress.setStringPainted(true);
 		
		BufferedImage image = null;
		try {
			image = ImageIO.read(this.getClass().getResource("/loader.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		setContentPane(new ImagePanel(image));
		setLayout(new BorderLayout());
		
 		add(progress, BorderLayout.SOUTH);
 		
 		setVisible(true);
 	}
 	
 	/**
 	 * @return the done
 	 */
 	public boolean isDone() {
 		return done;
 	}
 
 	/**
 	 * @param done the done to set
 	 */
 	public void setDone(boolean done) {
 		this.done = done;
 	}
 
 	/**
 	 * @return the procent
 	 */
 	public int getProcent() {
 		return procent;
 	}
 
 	/**
 	 * @param procent the procent to set
 	 */
 	public void setProcent(int procent) {
 		this.procent = procent;
 	}
 
 	/**
 	 * @return the comment
 	 */
 	public String getComment() {
 		return comment + " " + procent + "%";
 	}
 
 	/**
 	 * @param comment the comment to set
 	 */
 	public void setComment(String comment) {
 		this.comment = comment;
 	}
 
 	public void setWait(boolean wait) {
 		progress.setIndeterminate(wait);
 	}
 	
 	@Override
 	public void run() {
 		while(!done) {
 			progress.setValue(procent);
 			progress.setString(getComment());
 			
 			try {
 				Thread.sleep(100);
 				this.repaint();
 			} catch(InterruptedException e) {
 				
 			}
 		}
 		this.dispose();
 	}
 }
