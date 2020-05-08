 /*
  * Copyright 2012 University of Helsinki.
  *
  * This file is part of BMVis².
  *
  * BMVis² is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * BMVis² is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with BMVis².  If not, see
  * <http://www.gnu.org/licenses/>.
  */
 
 package biomine.bmvis2.help;
 
 import biomine.bmvis2.Logging;
 import biomine.bmvis2.Vis;
 import biomine.bmvis2.utils.ResourceUtils;
 //import com.sun.jndi.toolkit.url.UrlUtil;
 
 import java.awt.Component;
 import java.io.*;
 import java.net.URL;
 
 import javax.swing.JOptionPane;
 
 public class Help {
 	private static String mouseHelp;
 
 	static {
 		try {
            ResourceUtils.printResourceFiles(Vis.class, "");
            URL u = Vis.class.getResource("/mousehelp.html");
             Logging.debug("ui", "u: " + u + ",  u.getFile(): ");
            File f = new File("mousehelp.html");
 			BufferedReader br = new BufferedReader(new FileReader(f));
 			try {
 				StringBuilder bld = new StringBuilder();
 				while (true) {
 					String line = br.readLine();
 					if (line == null)
 						break;
 					bld.append(line);
 				}
 				mouseHelp = bld.toString();
 
 			} finally {
 				br.close();
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public static void showHelp(Component parent) {
 		if (mouseHelp == null)
 			return;
 		JOptionPane.showMessageDialog(parent, mouseHelp);
 	}
 }
