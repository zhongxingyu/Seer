/*  $Id: YammPop3.java,v 1.5 2003/05/05 20:14:13 fredde Exp $
  *  Copyright (C) 1999-2003 Fredrik Ehnbom
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
 package org.gjt.fredde.yamm;
 
 import java.io.IOException;
 import java.text.*;
 import java.util.*;
 import org.gjt.fredde.util.net.Pop3;
 import org.gjt.fredde.yamm.YAMM;
 import org.gjt.fredde.yamm.mail.*;
 
 /**
  * This class gets mail from a server and prints out the debugging messages to
  * YAMM.debug.
  * @author Fredrik Ehnbom
 * @version $Revision: 1.5 $
  */
 public class YammPop3
 	extends Pop3
 {
 	public YammPop3(String user, String password, String server, String file, int port, boolean debug)
 		throws IOException
 	{
 		super(user, password, server, file, port, debug);
 	}
 
 	/**
 	 * Print the debugmessages to YAMM.debug
 	 * @param msg The message to print
 	 */
 	public void Debug(String msg) {
 		if (debug) {
 			YAMM.debug.println("pop3: " + msg);
 			YAMM.debug.flush();
 		}
 	}
 
 	private int getLength(int msg)
 		throws IOException
 	{
 		sendCommand("LIST " + msg, false);
 		String answer = in.readLine();
 		Debug("Reply: " + answer);
 
 		if (!answer.toLowerCase().startsWith("-err")) {
 			return Integer.parseInt(answer.substring(answer.lastIndexOf(" ") + 1));
 		}
 		throw new IOException("Error Getting messagesize!! (" + answer + ")");
 	}
 
 	public boolean getMessage(int msg, int messages, YAMM frame)
 		throws IOException
 	{
 		Object[] args = {"" + msg, "" + messages};
 		frame.status.setStatus(YAMM.getString("server.get", args));
 		frame.status.progress(0);
 
 		int length = getLength(msg);
 
 		sendCommand("RETR " + msg, false);
 		String answer = in.readLine();
 		Debug("Reply: " + answer);
 
 		if (!answer.toLowerCase().startsWith("-err")) {
 			int read = 0;
 
 			String temp = "";
 			String from = "someone";
 			SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.US);
 
			while (!"".equals(answer)) {
 				answer = in.readLine();
 				temp += answer + "\n";
 
 				frame.status.setStatus(YAMM.getString("server.get", args) + "   " + read + "/" + length );
 				frame.status.progress( (int) (((float) read / length) * 100) );
 
 				if (answer.startsWith("From:") && answer.indexOf("@") != -1) {
 					from = MessageParser.getEmail(answer);
 					break;
 				}
 			}
 
 			outFile.println("From " + from + " " + dateFormat.format(new Date(System.currentTimeMillis())));
 			outFile.print(temp);
 
 			while (!(answer = in.readLine()).equals(".")) {
 				outFile.println(answer);
 				read += answer.length() + 1;
 
 				frame.status.setStatus(YAMM.getString("server.get", args) + "   " + read + "/" + length );
 				frame.status.progress( (int) (((float) read / length) * 100) );
 			}
 			return true;
 		}
 		return false;
 	}
 }
 /*
  * Changes:
  * $Log: YammPop3.java,v $
 * Revision 1.5  2003/05/05 20:14:13  fredde
 * fixed parsing for the From-field in the headers
 *
  * Revision 1.4  2003/04/19 11:56:52  fredde
  * updated to work with the new mbox-format
  *
  * Revision 1.3  2003/03/06 20:17:39  fredde
  * Now getting message size with the pop3 command LIST
  *
  * Revision 1.2  2000/12/31 14:05:07  fredde
  * better progressbars
  *
  * Revision 1.1  2000/02/28 13:51:24  fredde
  * initial commit
  *
  */
