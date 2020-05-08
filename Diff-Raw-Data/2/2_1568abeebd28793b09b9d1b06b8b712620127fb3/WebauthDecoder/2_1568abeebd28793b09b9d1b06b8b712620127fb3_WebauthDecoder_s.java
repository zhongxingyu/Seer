 /* This file is part of the University of Cambridge Web Authentication
  * System Java Toolkit
  *
  * Copyright 2005 University of Cambridge
  *
  * This toolkit is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * The toolkit is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this toolkit; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  * USA
  *
  * $Id: WebauthDecoder.java,v 1.3 2005/03/30 13:17:05 jw35 Exp $
  *
  */
 
 package uk.ac.cam.ucs.webauth;
 
 import java.io.IOException;
 
 import org.apache.commons.codec.binary.Base64;
 
 /**
  * Implements a decoder for encoded binary strings as used by the
  * authentication system. The encoding scheme is actually just BASE64
  * as defined in RFC 1521, except that the characters '+', '/', and '='
  * are replaced by '-', '.' and '_' to reduce the URL-encoding
  * overhead.
  *
  * @version $Revision: 1.3 $ $Date: 2005/03/30 13:17:05 $
  */
 
 public class WebauthDecoder {
 
     /** 
      * Default constructor
      *
      */
      
     public WebauthDecoder() {
     }
 
     /** 
      * Decode a string.
      *
      * @param encoded  the encoded string for processing
      *
      * @return the decoded data
      *
      * @throws IOException if the encoded string is malformed 
      */
 
     public byte[] decodeBuffer(String encoded) throws IOException {
 	
 	StringBuffer buff = new StringBuffer(encoded.length());
 	for (int i=0; i<encoded.length();++i) {
 	    char c = encoded.charAt(i);
 	    if (c == '-') {
 		c = '+';
 	    }
 	    else if (c == '.') {
 		c = '/';
 	    }
 	    else if (c == '_') {
 		c = '=';
 	    }
 	    buff.append(c);
 	}
 
	return Base64.decodeBase64(buff.toString());
     }
  
 }
 
