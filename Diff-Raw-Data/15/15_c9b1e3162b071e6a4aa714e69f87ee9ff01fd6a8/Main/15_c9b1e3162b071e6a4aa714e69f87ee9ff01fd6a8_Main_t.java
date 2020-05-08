 package com.emn.fil.automaticdiscover;
 
 import java.io.IOException;
 
 import javax.swing.UIManager;
 
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.apache.log4j.Logger;
 
 import com.emn.fil.automaticdiscover.dto.IP;
 import com.emn.fil.automaticdiscover.dto.IPMask;
 import com.emn.fil.automaticdiscover.ihm.Frame;
 import com.emn.fil.automaticdiscover.nmap.Nmap;
 
 public class Main {
 
 	public static Logger log = Logger.getLogger(Main.class);
 
 	public static void main(String[] args) {
 
 		// Set System L&F
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Exception e) {
 			Main.log.trace("ERROR : Impossible de modifier le theme");
 		}
 
 		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext-base.xml");
 		Nmap nmap = (Nmap) ctx.getBean("nmap");
 		IPMask ipMask = new IPMask(new IP("172.17.2.0"), 24);
 		try {
 			nmap.scanner(ipMask);
 		} catch (IOException e) {
			e.printStackTrace();
 		}
 
 		Frame frame = (Frame) ctx.getBean("frame");
 		frame.initFrame();
 	}
 }
