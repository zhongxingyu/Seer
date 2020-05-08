 /*  Attachment.java - Parses attachments
  *  Copyright (C) 1999-2001 Fredrik Ehnbom
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
 
 import org.gjt.fredde.yamm.encode.*;
 import org.gjt.fredde.yamm.YAMM;
 import org.gjt.fredde.util.gui.*;
 
 import java.io.*;
 
 /**
  * This class parses attachments
  * @author Fredrik Ehnbom
 * @version $Id: Attachment.java,v 1.11 2003/03/07 14:06:27 fredde Exp $
  */
 public class Attachment {
 
 	public static final int ERROR		= -1;
 	public static final int END			=  0;
 	public static final int MESSAGE 	=  1;
 	public static final int ATTACHMENT 	=  2;
 	public static final int AMESSAGE 	=  3;
 
 	public String name = null;
 	public String contentType = null;
 	public String contentDisposition = null;
 
 	public Attachment() {
 	}
 
 	public void parse(BufferedReader in, String boundary, String baseFile)
 		throws IOException, MessageParseException
 	{
 		MessageHeaderParser mhp = new MessageHeaderParser();
 		mhp.parse(in);
 
 		String encoding = null;  // Encoding used for this attachment
 		String fileName = null; // the file we write the data to
 
 		if (mhp.getHeaderField("Content-Type") != null) {
 			contentType = name = mhp.getHeaderField("Content-Type").trim();
 
 			if (name.toLowerCase().indexOf("name=") != -1) {
 				// remove unwanted info
 				name = name.substring(name.toLowerCase().indexOf("name=") + 5, name.length()).trim();
 
 				if (name.indexOf(";") != -1) {
 					name = name.substring(0, name.indexOf(";")).trim();
 				}
 
 				if (name.startsWith("\"") && name.endsWith("\"")) {
 					name = name.substring(1, name.length() - 1);
 				}
 			} else {
 				name = null;
 			}
 		}
 
 		if (mhp.getHeaderField("Content-Disposition") != null) {
 			contentDisposition = mhp.getHeaderField("Content-Disposition");
 
 			if (name == null) {
 				name = contentDisposition;
 				if (name.toLowerCase().indexOf("filename=") != -1) {
 					name = name.substring(name.toLowerCase().indexOf("filename=") + 9, name.length()).trim();
 
 					if (name.indexOf(";") != -1) {
 						name = name.substring(0, name.indexOf(";"));
 					}
 
 					if (name.startsWith("\"") && name.endsWith("\"")) {
 						name = name.substring(1, name.length() - 1);
 					}
 				} else {
 					name = null;
 				}
 			}
 		}
 
 		if (name != null) name = Mailbox.unMime(name);
 
 		if (mhp.getHeaderField("Content-Transfer-Encoding") != null) {
 			encoding = mhp.getHeaderField("Content-Transfer-Encoding").trim();
 		}
 
 		if (contentType.indexOf("multipart/alternative") != -1) {
 			String temp = contentType.toLowerCase();
 			if (temp.indexOf("boundary=") != -1) {
 				temp = contentType.substring(temp.indexOf("boundary=") + 9, contentType.length());
 				if (temp.startsWith("\"")) {
 					temp = temp.substring(1, temp.length() - 1);
 				}
 				if (boundary.indexOf("\"") != -1) {
 					temp = temp.substring(0, temp.indexOf("\""));
 				}
 			}
 			Attachment a = new Attachment();
 			a.parse(in, temp, baseFile);
 
 			while ((temp = in.readLine()) != null) {
 				if (temp.startsWith("--" + boundary)) {
 					if (!temp.endsWith("--")) {
 						a = new Attachment();
 						a.parse(in, boundary, baseFile);
 						break;
 					} else {
 						break;
 					}
 				}
 			}
 			return;
 		}
 
 		PrintWriter out = null;
 
 		try {
 			String temp = null;
 			fileName = baseFile;
 			boolean html = false;
 
			if (contentDisposition != null) 
 				fileName = baseFile + ".attach." + name;
 			else if (contentType.indexOf("text/html") != -1) {
 				fileName = baseFile + ".message.html";
 				html = true;
 			} else if (contentType.indexOf("text/plain") != -1)
 				fileName = baseFile + ".message.txt";
 			else if (name != null) {
 				fileName = baseFile + ".attach." + name;
 			} else {
 				System.err.println(contentDisposition);
 				System.err.println(contentType);
 				throw new MessageParseException("Unknown type of attachment!");
 			}
 
 			out = new PrintWriter(
 				new BufferedOutputStream(
 					new FileOutputStream(fileName)
 				)
 			);
 
 			while ((temp = in.readLine()) != null) {
				if (temp.startsWith("--" + boundary)) {
 					if (!temp.endsWith("--")) {
						Attachment a = new Attachment();
						a.parse(in, boundary, baseFile);
 						break;
 					} else {
 						break;
 					}
 				} else {
 					out.println(temp);
 					out.flush();
 				}
 			}
 		} finally {
 			if (out != null) {
 				out.close();
 			}
 		}
 
 		if (fileName.indexOf(".message.") != -1) {
 			MessageParser.filter(contentType, encoding, fileName);
 		} else {
 			// attachments are not needed to display the message
 			// so we can filter them in a thread
 			final String c = contentType;
 			final String e = encoding;
 			final String f = fileName;
 
 			Thread t = new Thread()
 			{
 				public void run() {
 					try {
 						MessageParser.filter(c, e, f);
 					} catch (IOException ioe) {
 						new ExceptionDialog(
 							YAMM.getString("msg.error"),
 							ioe,
 							YAMM.exceptionNames
 						);
 					}
 				}
 			};
 			t.setPriority(Thread.MIN_PRIORITY);
 			t.start();
 		}
 	}
 }
 /*
  * Changes:
  * $Log: Attachment.java,v $
  * Revision 1.11  2003/03/07 14:06:27  fredde
  * better attachment name parsing. also unmimes attachment names.
  *
  * Revision 1.10  2003/03/07 10:52:36  fredde
  * filter attachments in a thread
  *
  * Revision 1.9  2003/03/06 20:14:32  fredde
  * rewrote mailparsing system
  *
  * Revision 1.8  2003/03/05 21:36:23  fredde
  * Huge improvements for html, mime and multipart messages
  *
  * Revision 1.7  2003/03/05 15:04:22  fredde
  * added Variable contentType
  *
  * Revision 1.6  2001/04/21 09:33:37  fredde
  * fixed for inlined messages
  *
  * Revision 1.5  2001/03/18 17:07:59  fredde
  * cleaned up
  *
  * Revision 1.4  2000/03/17 17:12:10  fredde
  * some fixes
  *
  */
