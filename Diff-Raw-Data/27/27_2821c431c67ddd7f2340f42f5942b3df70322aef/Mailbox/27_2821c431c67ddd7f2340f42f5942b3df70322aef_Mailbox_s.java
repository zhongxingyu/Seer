/*  $Id: Mailbox.java,v 1.58 2003/06/06 10:51:18 fredde Exp $
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
 
 package org.gjt.fredde.yamm.mail;
 
 import java.text.ParseException;
 import java.io.*;
 import java.util.*;
 import java.util.regex.*;
 
 import javax.swing.JTextArea;
 import org.gjt.fredde.yamm.YAMM;
 import org.gjt.fredde.util.gui.ExceptionDialog;
 import org.gjt.fredde.yamm.encode.*;
 
 /**
  * A class that handels messages and information about messages
  * @author Fredrik Ehnbom
 * @version $Revision: 1.58 $
  */
 public class Mailbox {
 
 	public static int BUFFERSIZE = 65536;
 
 	/**
 	 * removes mime from the subject
 	 * @param subject The subject to remove the mime tags from
 	 */
 	public static String unMime(String subject) {
 		if (subject == null) {
 			return null;
 		}
 
 		if (subject.indexOf("=?") == -1 || subject.indexOf("?=") == -1) {
 			return subject;
 		}
 
 		while (subject.indexOf("=?") != -1) {
 			String start = subject.substring(0, subject.indexOf("=?"));
			String end   = subject.substring(subject.indexOf("?=") + 2,
								subject.length());
 			int iso = subject.indexOf("?", start.length() + 2) + 1;
 			String encoding = subject.substring(iso, subject.indexOf("?", iso+1));
			subject      = subject.substring(iso + 1 + encoding.length(),
							subject.indexOf("?="));
 
 			subject = subject.replace('_', ' ');
//			start = start.replace('_', ' ');
//			end = end.replace('_', ' ');
 
 			ByteArrayOutputStream out = new ByteArrayOutputStream();
 			ByteArrayInputStream in = new ByteArrayInputStream(subject.getBytes());
 			Decoder dec = null;
 
			if (encoding.equals("B")) {
 				dec = new Base64sun();
			} else if (encoding.equals("Q")) {
 				dec = new QuotedPrintable();
 			}
 			try {
 				dec.decode(in, out);
 				subject = out.toString();
 			} catch (Exception e) {
 			}
 			subject = start + subject + end;
 		}
 		return subject;
 	}
 
 	public static String getIndexName(String box) {
 		int sep = box.lastIndexOf(File.separator);
 		return box.substring(0, sep + 1) + "." + box.substring(sep + 1) + ".index";
 	}
 
 	/**
 	 * puts the source code in a JTextArea.
 	 * @param whichBox Which box the message to view is in
 	 * @param whichmail Which mail to view
 	 * @param jtarea Which JTextArea to append the source to
 	 * @return The number of rows added to the JTextArea
 	 */
 	public static int viewSource(String whichBox, int whichmail, JTextArea jtarea) {
 		BufferedReader in = null;
 		String boxpath = whichBox.substring(whichBox.indexOf("boxes") +	6, whichBox.length());
 		String file = YAMM.home + "/tmp/cache/" + boxpath + "/" + whichmail + ".raw";
 
 
 		try {
 			int ret = 0;
 			in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
 			String temp;
 
 			while ((temp = in.readLine()) != null) {
 				jtarea.append(temp + "\n");
 				ret++;
 			}
 			return ret;
 		} catch (IOException ioe) {
 			new ExceptionDialog(YAMM.getString("msg.error"),
 					ioe,
 					YAMM.exceptionNames);
 			return -1;
 		} finally {
 			try {
 				if (in != null) in.close();
 			} catch (IOException ioe) {}
 		}
 	}
 
 	public static int[] getUnread(String box) {
 		Index idx = null;
 		int[] unread = new int[3];
 		try {
 			idx = new Index(box);
 			idx.open();
 
 			unread[0] = idx.messageNum;
 			unread[1] = idx.unreadNum;
 			unread[2] = idx.newNum;
 		} catch (IOException ioe) {
 			new ExceptionDialog(YAMM.getString("msg.error"), ioe, YAMM.exceptionNames);
 		} finally {
 			try {
 				if (idx != null) idx.close();
 			} catch (IOException ioe) {}
 		}
 
 		return unread;
 	}
 
 	/**
 	 * Creates a list with the mails in this box.
 	 *
 	 * @param whichBox Which box to get messages from.
 	 * @param mailList The String array which will recieve the information
 	 */
 	public static void createList(String whichBox, YAMM yamm) {
 		yamm.listOfMails = createList(whichBox);
 		yamm.keyIndex = new int[yamm.listOfMails.length];
 		for (int i = 0; i < yamm.keyIndex.length; i++) yamm.keyIndex[i] = i;
 	}
 
 	/**
 	 * Creates a list with the mails in this box.
 	 *
 	 * @param whichBox Which box to get messages from.
 	 */
 	public static IndexEntry[] createList(String whichBox) {
 		Index i = null;
 		IndexEntry[] entries = null;
 		try {
 			i = new Index(whichBox);
 			i.open();
 			entries = i.getEntries();
 		} catch(IOException ioe) {
 			new ExceptionDialog(
 				YAMM.getString("msg.error"),
 				ioe,
 				YAMM.exceptionNames
 			);
 		} finally {
 			try {
 				if (i != null) i.close();
 			} catch (IOException e) {}
 		}
 		return entries;
 	}
 
 	/**
 	 * Creates a list with the mails in the filter box.
 	 * @param mailList The vector that should contain the information
 	 */
 	public static void createFilterList(Vector list) {
 		String subject = null;
 		String from = null;
 		String to = null;
 		String reply = null;
 
 		String temp = null;
 		list.clear();
 		Vector vec1 = new Vector(4);
 		boolean makeItem = false;
 
 		int i = 0;
 
 		BufferedReader in = null;
 
 		try {
 			in = new BufferedReader(new InputStreamReader(new FileInputStream(YAMM.home + "/boxes/.filter")));
 
 			MessageHeaderParser mhp = new MessageHeaderParser();
 
 			try {
 				mhp.parse(in);
 			} catch (MessageParseException mpe) {
 				new ExceptionDialog(YAMM.getString("msg.error"),
 						mpe,
 						YAMM.exceptionNames);
 			}
 
 			subject = mhp.getHeaderField("Subject");
 			from    = mhp.getHeaderField("From");
 			to      = mhp.getHeaderField("To");
 			reply   = mhp.getHeaderField("Reply-To");
 			subject = unMime(subject);
 
 			for (;;) {
 				temp = in.readLine();
 
 				if (temp == null || Pattern.matches("From .* [a-zA-Z]{3} [a-zA-Z]{3} {1,2}[0-9]{1,2} [0-9]{2}:[0-9]{2}:[0-9]{2} [0-9]{4}", temp)) {
 					if (from != null) {
 						vec1.insertElementAt(from, 0);
 					} else {
 						vec1.insertElementAt("", 0);
 					}
 					if (to != null) {
 						vec1.insertElementAt(to, 1);
 					} else {
 						vec1.insertElementAt("", 1);
 					}
 					if (subject != null) {
 						vec1.insertElementAt(subject, 2);
 					} else {
 						vec1.insertElementAt("", 2);
 					}
 					if (reply != null) {
 						vec1.insertElementAt(reply, 3);
 					} else {
 						vec1.insertElementAt("", 3);
 					}
 					list.insertElementAt(vec1, i);
 					vec1 = new Vector(4);
 					makeItem = false;
 					i++;
 
 					try {
 						mhp.parse(in);
 					} catch (MessageParseException mpe) {
 						break;
 					}
 
 					subject = mhp.getHeaderField("Subject");
 					from = mhp.getHeaderField("From");
 					to = mhp.getHeaderField("To");
 					reply = mhp.getHeaderField("Reply-To");
 				}
 			}
 		} catch(IOException ioe) {
 			new ExceptionDialog(YAMM.getString("msg.error"),
 					ioe,
 					YAMM.exceptionNames);
 		} finally {
 			try {
 				if (in != null) in.close();
 			} catch (IOException ioe) {}
 		}
 	}
 
 
 	/**
 	 * This function sets the status of the mail.
 	 * @param whichBox The box the message is in.
 	 * @param whichmail The mail to set the status for.
 	 * @param status The status string.
 	 */
 	public static void setStatus(YAMM yamm, String whichBox, int whichmail, IndexEntry e, int statusChange) {
 		Index idx = null;
 		try {
 			idx = new Index(whichBox);
 			idx.open();
 			if ((statusChange & IndexEntry.STATUS_READ) != 0) {
 				idx.unreadNum--;
 				idx.newNum = 0;
 				idx.write();
 			}
 			idx.saveEntry(e, whichmail);
 		} catch (Exception ie) {
 			new ExceptionDialog(
 				YAMM.getString("msg.error"),
 				ie,
 				YAMM.exceptionNames
 			);
 		} finally {
 			try {
 				if (idx != null) {
 					idx.close();
 				}
 			} catch (IOException ioe) {}
 		}
 	}
 
 	public static long getMessageSize(String box, int msg) {
 		long length = 0;
 		try {
 			Index index = new Index(box);
 			index.open();
 			long skip = index.getEntry(msg).skip;
 
 			if (msg+1 < index.messageNum) {
 				length = index.getEntry(msg+1).skip - skip;
 			} else {
 				length = new File(box).length() - skip;
 			}
 			index.close();
 		} catch (IOException e) {
 		}
 		return length;
 	}
 
 	public static void saveRaw(String box, String output, int msg, long skip) {
 		BufferedInputStream is = null;
 		BufferedOutputStream os = null;
 
 		try {
 			is = new BufferedInputStream(new FileInputStream(box));
 			os = new BufferedOutputStream(new FileOutputStream(output));
 
 			is.skip(skip);
 			long length = getMessageSize(box, msg);
 			byte[] buffer = new byte[BUFFERSIZE];
 
 
 			while (length > 0) {
 				int read = is.read(buffer, 0, (int)Math.min(BUFFERSIZE, length));
 				length -= read;
 				os.write(buffer, 0, read);
 			}
 		} catch (IOException ioe) {
 			new ExceptionDialog(YAMM.getString("msg.error"),
 					ioe,
 					YAMM.exceptionNames);
 		} finally {
 			try {
 				if (is != null) is.close();
 				if (os != null) os.close();
 			} catch (IOException ioe) {}
 		}
 	}
 	
 	/**
 	 * Prints the mail to ~home/.yamm/tmp/cache/<whichBox>/<whichmail>.html
 	 * @param whichBox Which box the message is in
 	 * @param whichmail Whichmail to export
 	 * @param attach Which vector to add attachments to
 	 */
 	public static void getMail(String whichBox, int whichmail, long skip) {
 		String  temp = YAMM.home + File.separator;
 		int attaches = 0;
 
 		String	tempdir = temp + "tmp/";
 		String	boxpath = whichBox.substring(whichBox.indexOf("boxes") +	6, whichBox.length());
 		File	cache = new File(tempdir + "cache/" + boxpath + "/");
 		File	out   = new File(cache, whichmail + ".html");
 		File	raw	= new File(cache, whichmail + ".raw");
 
 
 		if (out.exists()) return;
 		if (!new File(cache, whichmail + ".html").exists()) {
 			if (!cache.exists()) {
 				if (!cache.mkdirs()) {
 					System.err.println("Couldn't create" +
 						" dir: " + cache.toString());
 				}
 			}
 		}
 
 		if (!hasMail(whichBox)) {
 			return;
 		}
 
 		saveRaw(whichBox, raw.toString(), whichmail, skip);
 
 		BufferedReader in = null;
 		PrintWriter outFile = null;
 
 		try {
 			in = new BufferedReader(new InputStreamReader(
 						new FileInputStream(raw)));
 			outFile = new PrintWriter(new BufferedOutputStream(
 						new FileOutputStream(out)));
 
 			MessageParser mp = new MessageParser(in,
 						outFile, out.toString());
 
 		} catch (IOException ioe) {
 			new ExceptionDialog(YAMM.getString("msg.error"),
 					ioe,
 					YAMM.exceptionNames);
 		} catch (MessageParseException mpe) {
 			new ExceptionDialog(YAMM.getString("msg.error"),
 					mpe,
 					YAMM.exceptionNames);
 		} finally {
 			try {
 				if (in != null) in.close();
 				if (outFile != null) outFile.close();
 			} catch (IOException ioe) {}
 		}
 	}
 
 	/**
 	 * Gets the from and subject field from specified message
 	 * @param whichBox Which box the message is in
 	 * @param whichmail Which mail to get the headers from
 	 */
 	public static MessageHeaderParser getMailHeaders(String whichBox, long skip) {
 		BufferedReader in = null;
 		MessageHeaderParser mhp = null;
 
 		try {
 			in = new BufferedReader(new InputStreamReader(
 						new FileInputStream(whichBox)));
 
 			in.skip(skip);
 
 			mhp = new MessageHeaderParser();
 			try {
 				mhp.parse(in);
 			} catch (MessageParseException mpe) {
 				new ExceptionDialog(YAMM.getString("msg.error"),
 						mpe,
 						YAMM.exceptionNames);
 			}
 		} catch (IOException ioe) {
 			new ExceptionDialog(YAMM.getString("msg.error"),
 				ioe,
 				YAMM.exceptionNames);
 		} finally {
 			try {
 				if (in != null) in.close();
 			} catch (IOException ioe) {}
 		}
 
 		return mhp;
 	}
 
 	/**
 	 * Gets the content of this mail and adds to the specified JTextArea
 	 * @param whichBox Which box the message is in
 	 * @param whichmail Which mail to export
 	 * @param jtarea The JTextArea to append the message to
 	 */
 	public static void getMailForReply(String whichBox, int whichmail, long skip, JTextArea jtarea) {
 		BufferedReader in = null;
 
 		String boxpath = whichBox.substring(whichBox.indexOf("boxes") +	6, whichBox.length());
 		String file = YAMM.home + "/tmp/cache/" + boxpath + "/" + whichmail + ".message.";
 
 		if (new File(file + "txt").exists()) 
 			file += "txt";
 		else if (new File(file + "html").exists())
 			file += "html";
 		else
 			return;
 
 		try {
 			in = new BufferedReader(
 				new InputStreamReader(new FileInputStream(file))
 			);
 
 			String temp = null;
 			while ((temp = in.readLine()) != null) {
 				jtarea.append(">" + temp + "\n");
 			}
 
 		} catch (IOException ioe) {
 			new ExceptionDialog(
 				YAMM.getString("msg.error"),
 				ioe,
 				YAMM.exceptionNames
 			);
 		} finally {
 			try {
 				if (in != null) in.close();
 			} catch (IOException ioe) {}
 		}
 	}
 
 	/**
 	 * Deletes the specified mail. Returns false if it fails.
 	 * @param whichBox The box the message is in
 	 * @param whichmail The mail to delete
 	 */
 	public static boolean deleteMail(String whichBox, int[] whichmail) {
 		String temp = null;
 		boolean firstmail = true;
 		String home = System.getProperty("user.home");
 		String sep = System.getProperty("file.separator");
 		String trash = YAMM.home + sep + "boxes" + sep + "trash";
 		int next = 0;
 
 		if (!whichBox.equals(trash)) {
 			moveMail(whichBox, trash, whichmail);
 		} else if (whichBox.equals(trash)) {
 			BufferedInputStream in = null;
 			BufferedOutputStream outSrc = null;
 
 			long skipSub = 0;
 
 			long lastPosition = 0;
 			long copyLen = 0;
 			long skipLen = 0;
 
 			try {
 				byte[] buffer = new byte[BUFFERSIZE];
 
 				File inFile = new File(whichBox);
 				File outSrcFile = new File(whichBox + ".tmp");
 				Index srcIndex = new Index(whichBox);
 				srcIndex.open();
 
 				in = new BufferedInputStream(new FileInputStream(inFile));
 				outSrc = new BufferedOutputStream(new FileOutputStream(outSrcFile));
 
 				// copy everything up to the first message to be deleted
 				copyLen = lastPosition = srcIndex.getEntry(whichmail[0]).skip;
 				while (copyLen > 0) {
 					int read = in.read(buffer, 0, (int)Math.min(BUFFERSIZE, copyLen));
 					copyLen -= read;
 					outSrc.write(buffer, 0, read);
 				}
 
 				for (int i = 0; i < whichmail.length; i++) {
 					IndexEntry entry = srcIndex.getEntry(whichmail[i]);
 					skipLen = entry.skip;
 
 					if (whichmail[i]+1 < srcIndex.messageNum) {
 						lastPosition = srcIndex.getEntry(whichmail[i]+1).skip;
 					} else {
 						lastPosition = inFile.length();
 					}
 					skipLen = lastPosition - skipLen;
 
 					if (i + 1 < whichmail.length) {
 						copyLen = srcIndex.getEntry(whichmail[i+1]).skip;
 					} else {
 						copyLen = inFile.length();
 					}
 					copyLen -= lastPosition;
 
 					if ((entry.status & IndexEntry.STATUS_READ) == 0) {
 						srcIndex.unreadNum--;
 						srcIndex.newNum = 0;
 					}
 					skipSub += skipLen;
 
 					int lastMessage = srcIndex.messageNum;
 					if (i + 1 < whichmail.length) {
 						lastMessage = whichmail[i+1];
 					}
 					for (int j = whichmail[i] + 1; j < lastMessage; j++) {
 						IndexEntry ie = srcIndex.getEntry(j);
 						srcIndex.raf.seek(Index.HEADERSIZE + (j - (i+1)) * IndexEntry.SIZE);
 						ie.skip -= skipSub;
 						ie.write(srcIndex.raf);
 					}
 
 					while (skipLen > 0) {
 						skipLen -= in.skip(Math.min(BUFFERSIZE, skipLen));
 					}
 					while (copyLen > 0) {
 						int read = in.read(buffer, 0, (int)Math.min(BUFFERSIZE, copyLen));
 						copyLen -= read;
 						outSrc.write(buffer, 0, read);
 					}
 				}
 
 
 				srcIndex.messageNum -= whichmail.length;
 				srcIndex.rewrite();
 
 				srcIndex.close();
 
 				in.close();
 				outSrc.close();
 
 				inFile.delete();
 				if (!outSrcFile.renameTo(inFile)) {
 					System.err.println("Couldn't rename " + outSrcFile + " to " + inFile);
 				}
 			} catch (IOException ioe) {
 				new ExceptionDialog(
 					YAMM.getString("msg.error"),
 					ioe,
 					YAMM.exceptionNames
 				);
 				return false;
 			} finally {
 				try {
 					if (in != null) in.close();
 					if (outSrc != null) outSrc.close();
 				} catch (IOException ioe) {}
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * This function tries to copy a mail from a box to another.
 	 * @param fromBox From which box
 	 * @param toBox To which box
 	 * @param whichmail The mail to copy from fromBox to toBox
 	 */
 	public static boolean copyMail(String fromBox, String toBox, int[] whichmail) {
 		if (fromBox.equals(toBox)) {
 			return false;
 		} else if (whichmail.length < 1) {
 			return false;
 		}
 
 		BufferedInputStream in = null;
 		BufferedOutputStream outTgt = null;
 
 		long tgtSkip = new File(toBox).length();
 
 		long lastPosition = 0;
 		long copyLen = 0;
 		long skipLen = 0;
 
 		try {
 			byte[] buffer = new byte[BUFFERSIZE];
 
 			File inFile = new File(fromBox);
 			Index srcIndex = new Index(fromBox);
 			Index tgtIndex = new Index(toBox);
 			srcIndex.open();
 			tgtIndex.open();
 			tgtIndex.raf.seek(Index.HEADERSIZE + tgtIndex.messageNum * IndexEntry.SIZE);
 
 			in = new BufferedInputStream(new FileInputStream(inFile));
 			outTgt = new BufferedOutputStream(new FileOutputStream(toBox, true));
 
 			for (int i = 0; i < whichmail.length; i++) {
 				IndexEntry entry = srcIndex.getEntry(whichmail[i]);
 				skipLen = copyLen = entry.skip;
 				skipLen -= lastPosition;
 
 				if (whichmail[i]+1 < srcIndex.messageNum) {
 					lastPosition = srcIndex.getEntry(whichmail[i]+1).skip;
 				} else {
 					lastPosition = inFile.length();
 				}
 				copyLen = lastPosition - copyLen;
 
 				entry.skip = tgtSkip;
 				entry.write(tgtIndex.raf);
 				tgtSkip += copyLen;
 
 				if ((entry.status & IndexEntry.STATUS_READ) == 0) {
 					tgtIndex.newNum++;
 					tgtIndex.unreadNum++;
 				}
 
 				while (skipLen > 0) {
 					long read = in.skip(Math.min(BUFFERSIZE, skipLen));
 					skipLen -= read;
 				}
 				while (copyLen > 0) {
 					int read = in.read(buffer, 0, (int)Math.min(BUFFERSIZE, copyLen));
 					copyLen -= read;
 					outTgt.write(buffer, 0, read);
 				}
 			}
 
 
 			tgtIndex.messageNum += whichmail.length;
 			tgtIndex.rewrite();
 
 			srcIndex.close();
 			tgtIndex.close();
 
 			in.close();
 			outTgt.close();
 
 		} catch (IOException ioe) {
 			new ExceptionDialog(
 				YAMM.getString("msg.error"),
 				ioe,
 				YAMM.exceptionNames
 			);
 			return false;
 		} finally {
 			try {
 				if (in != null) in.close();
 				if (outTgt != null) outTgt.close();
 			} catch (IOException ioe) {}
 		}
 
 		return true;
 	}
 
 	/**
 	 * Moves a mail from one box to another.
 	 * @param fromBox The box to move the mail from
 	 * @param toBox The box to move the mail to
 	 * @param whichmail The mail to move
 	 */
 	public static boolean moveMail(String fromBox, String toBox, int[] whichmail) {
 		if (fromBox.equals(toBox)) {
 			return false;
 		} else if (whichmail.length < 1) {
 			return false;
 		}
 
 		BufferedInputStream in = null;
 		BufferedOutputStream outSrc = null;
 		BufferedOutputStream outTgt = null;
 
 		long skipSub = 0;
 		long tgtSkip = new File(toBox).length();
 
 		long lastPosition = 0;
 		long copyLen = 0;
 		long skipLen = 0;
 
 		try {
 			byte[] buffer = new byte[BUFFERSIZE];
 
 			File inFile = new File(fromBox);
 			File outSrcFile = new File(fromBox + ".tmp");
 			Index srcIndex = new Index(fromBox);
 			Index tgtIndex = new Index(toBox);
 			srcIndex.open();
 			tgtIndex.open();
 			tgtIndex.raf.seek(Index.HEADERSIZE + tgtIndex.messageNum * IndexEntry.SIZE);
 
 			in = new BufferedInputStream(new FileInputStream(inFile));
 			outSrc = new BufferedOutputStream(new FileOutputStream(outSrcFile));
 			outTgt = new BufferedOutputStream(new FileOutputStream(toBox, true));
 
 			// copy everything up to the first message to be moved
 			copyLen = lastPosition = srcIndex.getEntry(whichmail[0]).skip;
 			while (copyLen > 0) {
 				int read = in.read(buffer, 0, (int)Math.min(BUFFERSIZE, copyLen));
 				copyLen -= read;
 				outSrc.write(buffer, 0, read);
 			}
 
 			for (int i = 0; i < whichmail.length; i++) {
 				IndexEntry entry = srcIndex.getEntry(whichmail[i]);
 				skipLen = entry.skip;
 
 				if (whichmail[i]+1 < srcIndex.messageNum) {
 					lastPosition = srcIndex.getEntry(whichmail[i]+1).skip;
 				} else {
 					lastPosition = inFile.length();
 				}
 				skipLen = lastPosition - skipLen;
 
 				if (i + 1 < whichmail.length) {
 					copyLen = srcIndex.getEntry(whichmail[i+1]).skip;
 				} else {
 					copyLen = inFile.length();
 				}
 				copyLen -= lastPosition;
 
 				entry.skip = tgtSkip;
 				entry.write(tgtIndex.raf);
 				tgtSkip += skipLen;
 
 				if ((entry.status & IndexEntry.STATUS_READ) == 0) {
 					tgtIndex.newNum++;
 					tgtIndex.unreadNum++;
 					srcIndex.unreadNum--;
 					srcIndex.newNum = 0;
 				}
 				skipSub += skipLen;
 
 				int lastMessage = srcIndex.messageNum;
 				if (i + 1 < whichmail.length) {
 					lastMessage = whichmail[i+1];
 				}
 				for (int j = whichmail[i] + 1; j < lastMessage; j++) {
 					IndexEntry ie = srcIndex.getEntry(j);
 					srcIndex.raf.seek(Index.HEADERSIZE + (j - (i+1)) * IndexEntry.SIZE);
 					ie.skip -= skipSub;
 					ie.write(srcIndex.raf);
 				}
 
 				while (skipLen > 0) {
 					int read = in.read(buffer, 0, (int)Math.min(BUFFERSIZE, skipLen));
 					skipLen -= read;
 					outTgt.write(buffer, 0, read);
 				}
 				while (copyLen > 0) {
 					int read = in.read(buffer, 0, (int)Math.min(BUFFERSIZE, copyLen));
 					copyLen -= read;
 					outSrc.write(buffer, 0, read);
 				}
 			}
 
 
 			srcIndex.messageNum -= whichmail.length;
 			tgtIndex.messageNum += whichmail.length;
 			srcIndex.rewrite();
 			tgtIndex.rewrite();
 
 			srcIndex.close();
 			tgtIndex.close();
 
 			in.close();
 			outSrc.close();
 			outTgt.close();
 
 			inFile.delete();
 			if (!outSrcFile.renameTo(inFile)) {
 				System.err.println("Couldn't rename " + outSrcFile + " to " + inFile);
 			}
 		} catch (IOException ioe) {
 			new ExceptionDialog(
 				YAMM.getString("msg.error"),
 				ioe,
 				YAMM.exceptionNames
 			);
 			return false;
 		} finally {
 			try {
 				if (in != null) in.close();
 				if (outSrc != null) outSrc.close();
 				if (outTgt != null) outTgt.close();
 			} catch (IOException ioe) {}
 		}
 
 		return true;
 	}
 
 	/**
 	 * Checks if the box has mail in it.
 	 * @param whichBox The box to check for mail
 	 */
 	public static boolean hasMail(String whichBox) {
 		String temp = null;
 		boolean mail = false;
 
 		BufferedReader in = null;
 		try {
 			in = new BufferedReader(new InputStreamReader(
 					new FileInputStream(whichBox)));
 
 			for(;;) {
 				temp = in.readLine();
 
 				if (temp == null) {
 					break;
 				} else if (temp.indexOf(":") != -1) {
 					mail = true;
 					break;
 				}
 			}
 		} catch (IOException ioe) {
 			new ExceptionDialog(YAMM.getString("msg.error"),
 						ioe,
 						YAMM.exceptionNames);
 
 			return false;
 		} finally {
 			try {
 				if (in != null) in.close();
 			} catch (IOException ioe) {}
 		}
 		return mail;
 	}
 }
 /*
  * Changes:
  * $Log: Mailbox.java,v $
  * Revision 1.58  2003/06/06 10:51:18  fredde
  * unread is now the total number of unread messages
  *
  * Revision 1.57  2003/04/27 07:59:13  fredde
  * updated getFilterList to work with the new mbox format
  *
  * Revision 1.56  2003/04/19 12:07:17  fredde
  * fixed unMime to work with strings containing multiple =?-entries
  *
  * Revision 1.55  2003/04/19 11:55:41  fredde
  * saves messages to a .raw-file for getMail and viewSource
  *
  * Revision 1.54  2003/04/16 12:44:59  fredde
  * replaced getMailForReplyHeaders with getMailHeaders
  *
  * Revision 1.53  2003/04/04 15:36:44  fredde
  * now parses =?-strings as it should
  *
  * Revision 1.52  2003/03/15 19:28:35  fredde
  * implemented deleteMail
  *
  * Revision 1.51  2003/03/11 15:20:13  fredde
  * Added getIndexName
  *
  * Revision 1.50  2003/03/10 21:49:37  fredde
  * moveMail: cleaned up, bug fixed. copyMail: implemented
  *
  * Revision 1.49  2003/03/10 12:32:09  fredde
  * unreadNum/newNum changes
  *
  * Revision 1.48  2003/03/10 09:42:45  fredde
  * non localized box filenames
  *
  * Revision 1.47  2003/03/09 18:04:56  fredde
  * removeQuote not needed anymore
  *
  * Revision 1.46  2003/03/09 17:44:33  fredde
  * cleaned up. now uses the new index system
  *
  * Revision 1.45  2003/03/08 16:57:03  fredde
  * updated getMailForReplyHeaders
  *
  * Revision 1.44  2003/03/07 22:23:19  fredde
  * removeQuote now also handles backslashes
  *
  * Revision 1.43  2003/03/07 20:22:00  fredde
  * do not regenerate the message if it has allready been cached. unMime before removeQuote
  *
  * Revision 1.42  2003/03/07 10:53:07  fredde
  * unMime fixes
  *
  * Revision 1.41  2003/03/06 23:51:11  fredde
  * fixed getMailForReply
  *
  * Revision 1.40  2003/03/06 20:14:32  fredde
  * rewrote mailparsing system
  *
  * Revision 1.39  2003/03/05 15:59:08  fredde
  * createList fixes
  *
  * Revision 1.38  2003/03/05 15:08:21  fredde
  * Setting status for messages should be much faster now
  *
  * Revision 1.37  2000/12/31 14:11:34  fredde
  * added createList(box)
  *
  * Revision 1.36  2000/12/26 11:24:42  fredde
  * YAMM.listOfMails is now of type String[][]
  *
  * Revision 1.35  2000/07/16 17:48:36  fredde
  * lots of Windows compatiblity fixes
  *
  * Revision 1.34  2000/04/15 13:05:44  fredde
  * close out-/inputstream before renaming file
  *
  * Revision 1.33  2000/04/01 20:50:09  fredde
  * fixed to make the profiling system work
  *
  * Revision 1.32  2000/03/25 15:46:52  fredde
  * updated the getMailForReplyHeaders method
  *
  * Revision 1.31  2000/03/15 11:13:45  fredde
  * boxes now show if and how many unread messages they have
  *
  * Revision 1.30  2000/03/14 21:19:12  fredde
  * better io-cleanup
  *
  * Revision 1.29  2000/03/12 17:18:05  fredde
  * still creates index when there's no subject/from field
  *
  */
