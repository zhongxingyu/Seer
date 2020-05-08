 /*  Filter.java - A class that filters incoming messages
  *  Copyright (C) 1999 Fredrik Ehnbom
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package org.gjt.fredde.yamm.mail;
 
 import java.io.*;
 import java.util.StringTokenizer;
 import java.util.Vector;
 import org.gjt.fredde.yamm.YAMM;
 
 /**
  * A class that filters incoming messages
  */
 public class Filter {
 
 	/**
 	 * Starts the filtering
 	 */
 	public Filter() throws IOException {
 		int type = 0;
 		int cheat[] = new int[1];
 		Vector list = new Vector();
 		BufferedReader in = null;
 
 		Mailbox.createFilterList(list);
 
 		try {
 			in = new BufferedReader(
 					new InputStreamReader(
 					new FileInputStream(YAMM.home +
 								".filters")));
 
 			String temp = null;
 
 			for (;;) {
 				temp = in.readLine();
 
 				if (temp == null) {
 					break;
 				}
 
 				StringTokenizer tok = new StringTokenizer(temp,
 									";");
 				String con1 = tok.nextToken();
 				String con2 = tok.nextToken();
 				String con3 = tok.nextToken();
 				String exec = tok.nextToken();
 
 				if (con1.equals("from")) {
 					 type = 0;
 				} else if (con1.equals("to")) {
 					type = 1;
 				} else if (con1.equals("subject")) {
 					type = 2;
 				} else if (con1.equals("reply")) {
 					type = 3;
 				}
 
 				if (con2.equals("is")) {
 					for (int i = 0; i < list.size(); i++) {
 						Vector v = (Vector)
 							list.elementAt(i);
 						String temp2 =
 							v.elementAt(type).
 								toString();
 						if (temp2.equalsIgnoreCase(
 									con3)) {
 							list.remove(i);
 							cheat[0] = i;
 							Mailbox.moveMail(
 								YAMM.home +
 								"boxes/.filter",
 								YAMM.home +
 								"boxes/" +
 								exec, cheat);
 							i--;
 						}
 					}
 				} else if (con2.equals("contains")) {
 					for (int i = 0; i < list.size(); i++) {
 						Vector v = (Vector)
 							list.elementAt(i);
 						String temp2 =
 							v.elementAt(type).
 								toString();
 
 						if (temp2.toLowerCase().
 								indexOf(con3.
 								toLowerCase())
 									!= -1) {
 							list.remove(i);
 							cheat[0] = i;
 							Mailbox.moveMail(
 								YAMM.home +
 								"boxes/.filter",
 								YAMM.home +
 								"boxes/" +
 								exec, cheat);
 							i--;
 						}
 					}
 				}
 			}
 		} finally {
 			in.close();
 		}
 
 		cheat[0] = 0;
		while (Mailbox.hasMail(YAMM.home + "boxes/.filter")) {
			Mailbox.moveMail(YAMM.home + "boxes/.filter",
						YAMM.home + "boxes/" +
 						YAMM.getString("box.inbox"),
 									cheat);
 		}
 	}
 }
 
