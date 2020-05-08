 // The Grinder
 // Copyright (C) 2000, 2001 Paco Gomez
 // Copyright (C) 2000, 2001 Phil Dawes
 // Copyright (C) 2001  Phil Aston
 // Copyright (C) 2001  Paddy Spencer
 
 // This program is free software; you can redistribute it and/or
 // modify it under the terms of the GNU General Public License
 // as published by the Free Software Foundation; either version 2
 // of the License, or (at your option) any later version.
 
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 
 // You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 
 package net.grinder.plugin.http;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.io.UnsupportedEncodingException;
 
 import org.apache.regexp.RE;
 import org.apache.regexp.RESyntaxException;
 
 import net.grinder.tools.tcpsniffer.ConnectionDetails;
 import net.grinder.tools.tcpsniffer.EchoFilter;
 import net.grinder.tools.tcpsniffer.SnifferFilter;
 
 /**
  *
  * @author Paddy Spencer
  * @version $Revision$
  */
 public class URLRewriteFilter implements SnifferFilter
 {
     public void handle(ConnectionDetails connectionDetails, byte[] buffer,
 		       int bytesRead)
         throws IOException, RESyntaxException
     {
	final String ENCODING = "US-ASCII";
 
 	// as Phil would say: hackety do dah
 	final String protocol = connectionDetails.isSecure() ? "https" : "http";
 	final String host = connectionDetails.getLocalHost();
 
 	// not final because we do want to rewrite it if we find what
 	// we're looking for.
 	String string = new String(buffer, 0, bytesRead, ENCODING);
 		
 	// make two passes - one for href and one for target
 	final RE re1 = new RE("(href)\\s*=\\s*(\"|')?" +
 			      protocol + "://" + host + "(:\\d+)?/?",
 			      RE.MATCH_CASEINDEPENDENT);
 
 	final RE re2 = new RE("(target)\\s*=\\s*(\"|')?" +
 			      protocol + "://" + host + "(:\\d+)?/?",
 			      RE.MATCH_CASEINDEPENDENT);
 		
 	boolean href = re1.match(string);
 	boolean target = re2.match(string);
 	if (href || target){
 
 	    if (href) {
 		string = re1.subst(string, re1.getParen(1) + "=" +
 				   re1.getParen(2) + "/", 
 				   RE.REPLACE_ALL);
 	    }
 	    if (target) {
 		string = re2.subst(string, re2.getParen(1) + "=" +
 				   re2.getParen(2) + "/", 
 				   RE.REPLACE_ALL);
 	    }
 			
 	    try {
 		// to avoid the end of the original file still being
 		// at the end of the buffer, we make a tmpbuffer of
 		// the same size as the original, copy the rewritten
 		// buffer into it (which should leave the end of it
 		// still null) and then copy the whole thing over the
 		// original, which has the same size.(This relies on
 		// the VM to initialise arrays and objects and stuff
 		// to null, which it does.)
 				
 		byte[] tmpbuffer = new byte[bytesRead];
 		byte[] bytes = string.getBytes(ENCODING);
 				
 		System.arraycopy(bytes, 0, tmpbuffer, 0, bytes.length);
 		System.arraycopy(tmpbuffer, 0, buffer, 0, bytesRead);
 			
 			
 		// none of these exceptions should occur, as we've
 		// ensured that the arrays we're copying are of the
 		// right sizes and aren't null.
 	    } catch (IndexOutOfBoundsException e) {
 		e.printStackTrace();
 	    } catch (ArrayStoreException e) {
 		e.printStackTrace();
 	    } catch (NullPointerException e) {
 		e.printStackTrace();
 	    }
 		
 	} else {
 	    // nothing matched so no need to do anything.
 	}
 
     }
 }
 
 
 
