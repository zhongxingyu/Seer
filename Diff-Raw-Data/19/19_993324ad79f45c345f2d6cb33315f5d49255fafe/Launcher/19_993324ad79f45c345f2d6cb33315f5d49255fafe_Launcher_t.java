 package org.helper.launch;
 
 import org.helper.ui.HelperFrame;
import org.helper.util.HelperLoggerAppender;
 
 public class Launcher {
 	
 	public static void main(String[] args) {
 		HelperFrame frame = new HelperFrame();
 		frame.setVisible(true);
		HelperLoggerAppender.writeLog("Helper 启动成功");
 	}
 }
