 /*
  * MailHeaderFilter.java
  * This file is part of Freemail, copyright (C) 2006 Dave Baker
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
  * USA
  * 
  */
 
 /*
  * MailHeaderFilter - A class to parse an Email message line by line
  * and strip out information we'd rather not send
  */
 
 package freemail;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.lang.StringBuffer;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.TimeZone;
import java.util.Calendar;
 import java.text.ParseException;
 
 class MailHeaderFilter {
 	private final BufferedReader reader;
 	private final StringBuffer buffer;
 	private boolean foundEnd;
 	private static final SimpleDateFormat sdf;
 	private static final TimeZone gmt;
	private static final Calendar cal;
 	
 	static {
		sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
 		gmt = TimeZone.getTimeZone("GMT");
		cal = Calendar.getInstance(gmt);
 	}
 	
 	public MailHeaderFilter(BufferedReader rdr) {
 		this.reader = rdr;
 		this.buffer = new StringBuffer();
 		this.foundEnd = false;
 	}
 	
 	public String readHeader() throws IOException {
 		String retval = null;
 		
 		while (retval == null) {
 			if (this.foundEnd) {
 				return this.flush();
 			}
 			
 			String line = this.reader.readLine();
 			if (line == null) {
 				System.out.println("Warning - reached end of message file before reaching end of headers! This shouldn't happen!");
 				throw new IOException("Header filter reached end of message file before reaching end of headers");
 			}
 			
 			if (line.length() == 0) {
 				// end of the headers
 				this.foundEnd = true;
 				retval = this.flush();
 			} else if (line.startsWith(" ") || line.startsWith("\t")) {
 				// continuation of the previous header
 				this.buffer.append("\r\n"+line.trim());
 			} else {
 				retval = this.flush();
 				this.buffer.append(line);
 			}
 		}
 		return retval;
 	}
 	
 	// this is called once a header is in the buffer
 	// if the header is invalid or filtered out entirely,
 	// return null. Otherwise return the filtered header.
 	private String flush() {
 		if (this.buffer.length() == 0) return null;
 		
 		String[] bits = this.buffer.toString().split(": ", 2);
 		this.buffer.delete(0, this.buffer.length());
 		
 		// invalid header - ditch it.
 		if (bits.length < 2) return null;
 		
 		bits[1] = this.filterHeader(bits[0], bits[1]);
 		if (bits[1] == null) return null;
 		
 		return bits[0]+": "+bits[1];
 	}
 	
 	private String filterHeader(String name, String val) {
 		// simple blacklist filter for now
 		// a whitelist filter is probably excessive
 		if (name.equalsIgnoreCase("Date")) {
 			// the norm is to put the sender's local time here, with the sender's local time offset
 			// at the end. Rather than giving away what time zone we're in, parse the date in
 			// and return it as a GMT time.
 			
 			Date d = null;
 			try {
 				d = sdf.parse(val);
 			} catch (ParseException pe) {
 				// ...the compiler whinges unless we catch this exception...
 				System.out.println("Warning: couldn't parse date: "+val+" (caught exception)");
 				return null;
 			}
 			// but the docs don't say that it throws it, but says that it return null
 			// http://java.sun.com/j2se/1.5.0/docs/api/java/text/SimpleDateFormat.html#parse(java.lang.String, java.text.ParsePosition)
 			if (d == null) {
 				// invalid date - ditch the header
 				System.out.println("Warning: couldn't parse date: "+val+" (got null)");
 				return null;
 			}
			cal.setTime(d);
			cal.setTimeZone(gmt);
			return sdf.format(cal.getTime());
 		} else if (name.equalsIgnoreCase("User-Agent")) {
 			// might as well hide this
 			return null;
 		} else {
 			return val;
 		}
 	}
 }
