 /*  YammSmtp.java - send mail
  *  Copyright (C) 2000 Fredrik Ehnbom
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
 import org.gjt.fredde.util.net.Smtp;
 import org.gjt.fredde.yamm.YAMM;
 
 /**
  * This class sends mail and prints the debugmessages to YAMM.debug
  * @author Fredrik Ehnbom
 * @version $Id: YammSmtp.java,v 1.1 2000/02/28 13:51:24 fredde Exp $
  */
public class YammSmtp extends Smtp {
 
 	public YammSmtp(String server, int port, boolean debug) throws IOException {
 		super(server, port, debug);
 	}
 
 
 	/**
 	 * Print the debugmessage to YAMM.debug
 	 * @param msg The message to print
 	 */
 	public void Debug(String msg) {
 		if (debug) {
 			YAMM.debug.println("smtp: " + msg);
 			YAMM.debug.flush();
 		}
 	}
 }
 /*
  * Changes:
  * $Log: YammSmtp.java,v $
  * Revision 1.1  2000/02/28 13:51:24  fredde
  * initial commit
  *
  */
