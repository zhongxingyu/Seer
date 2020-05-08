 /**
 *   ORCC rapid content creation for entertainment, education and media production
 *   Copyright (C) 2012 Michael Heinzelmann, Michael Heinzelmann IT-Consulting
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 package org.mcuosmipcuter.orcc.soundvis.gui;
 
 import java.io.InputStream;
 
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 
 import org.mcuosmipcuter.orcc.gui.Main;
 import org.mcuosmipcuter.orcc.util.IOUtil;
 
 /**
  * Utility to show the about box
  * @author Michael Heinzelmann
  */
 public class AboutBox {
 	/**
 	 * Show modal about box using the /license.txt resource file as model
 	 * and  {@link JTextArea} {@link JScrollPane} and a {@link JOptionPane} view components
 	 */
 	public static void showModal() {
 		InputStream is = Main.class.getResourceAsStream("/license.txt");
 		try {
			byte[] bytes = new byte[is.available()];
			is.read(bytes);
			String text = new String(bytes);
 			JTextArea ta = new JTextArea(20, 50);
 			JScrollPane sp = new JScrollPane(ta);
			ta.setText(text);
 			ta.setCaretPosition(0);
 			Object[] array = {sp}; 
 			JOptionPane.showMessageDialog(null, array, 
 					"ORCC is free software and comes WITHOUT ANY WARRANTY see license below:", 
 					JOptionPane.INFORMATION_MESSAGE);
 		}
 		catch(Exception ex) {
 			ex.printStackTrace();
 		}
 		finally {
			IOUtil.safeClose(is);
 		}
 	}
 }
