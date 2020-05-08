 package org.sdu.database;
 
 import java.awt.*;
 import java.io.File;
 import java.lang.reflect.Method;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 
 /**
 * Launch the database management application on OS X. Optimized for menu bar
 * and dock.
  * 
 * @version 0.1 rev 8003 Dec. 30, 2012
 * Copyright (c) HyperCube Dev Team
  */
 public class Driver {
 	public static void main(String[] args) {
		if (System.getProperty("os.name").startsWith("Mac OS X")) {
 			// Title
 			System.setProperty(
 					"com.apple.mrj.application.apple.menu.about.name", "数据库");
 			// Menu bar
 			System.setProperty("apple.laf.useScreenMenuBar", "true");
 			// Dock Icon
 			try {
 				Class<?> app = Class.forName("com.apple.eawt.Application");
				Method getapp = app.getMethod("getApplication", new Class<?>[0]);
 				Object app_obj = getapp.invoke(null, new Object[0]);
				Method seticon = app.getMethod("setDockIcon", new Class[] { Image.class });
				seticon.invoke(app.cast(app_obj), new Object[] { ImageIO.read(new File("art/database/icon.png")) });
				
				//com.apple.eawt.Application.getApplication().setDockIconImage(
				//		new ImageIcon("art/database/icon.png").getImage());
			} catch(Exception e) {
				e.printStackTrace();
				return ;
 			}
 		}
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					Main frame = new Main();
 					frame.setIconImage(new ImageIcon("art/database/icon.png")
 							.getImage());
 					frame.nopic = ImageIO.read(new File(
 							"art/database/nopic.png"));
 					frame.setVisible(true);
 				} catch (Exception e) {
 					JOptionPane.showMessageDialog(null, "找不到图片", "缺少文件",
 							JOptionPane.ERROR_MESSAGE);
 					System.exit(-1);
 				}
 			}
 		});
 	}
 }
