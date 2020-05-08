 package org.sdu.database;
 
 import java.awt.*;
 import java.io.File;
 import java.lang.reflect.Method;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 
 /**
 * Launch the database management application. Optimized for menu bar and dock
 * on OS X.
  * 
 * @version 0.1 rev 8004 Jan. 2, 2013 Copyright (c) HyperCube Dev Team
  */
 public class Driver {
 	public static void main(String[] args) {
		if (System.getProperty("os.name").startsWith("Mac OS")) {
 			// Title
 			System.setProperty(
 					"com.apple.mrj.application.apple.menu.about.name", "数据库");
 			// Menu bar
 			System.setProperty("apple.laf.useScreenMenuBar", "true");
 			// Dock Icon
 			try {
 				Class<?> app = Class.forName("com.apple.eawt.Application");
				Method getapp = app
						.getMethod("getApplication", new Class<?>[0]);
 				Object app_obj = getapp.invoke(null, new Object[0]);
				Method seticon = app.getMethod("setDockIconImage",
						new Class[] { Image.class });
				seticon.invoke(app.cast(app_obj), new Object[] { ImageIO
						.read(new File("art/database/icon.png")) });
			} catch (Exception e) {
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
