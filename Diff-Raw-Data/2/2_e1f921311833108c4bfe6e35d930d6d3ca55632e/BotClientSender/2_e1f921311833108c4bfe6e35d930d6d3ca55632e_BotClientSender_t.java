 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author geza
  */
 
 import maslab.telemetry.*;
 import maslab.telemetry.channel.*;
 import java.util.*;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.image.*;
 
 public class BotClientSender extends java.lang.Thread {
 	public ImageChannel origim = new ImageChannel("origim");
 	public ImageChannel procim = new ImageChannel("procim");
 	public BufferedImage origI = null;
 	public BufferedImage procI = null;
 	public boolean running = true;
 
 	public void start() {
 		while (running) {
 		try {
 		java.lang.Thread.sleep(1000);
 		origim.publish(origI);
		procim.publish(procI);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		}
 	}
 
 	public void bye() {
 		running = false;
 	}
 }
