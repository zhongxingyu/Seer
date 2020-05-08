 /*
  Copyright (c) 2000-2005 University of Washington.  All rights reserved.
 
  Redistribution and use of this distribution in source and binary forms,
  with or without modification, are permitted provided that:
 
    The above copyright notice and this permission notice appear in
    all copies and supporting documentation;
 
    The name, identifiers, and trademarks of the University of Washington
    are not used in advertising or publicity without the express prior
    written permission of the University of Washington;
 
    Recipients acknowledge that this distribution is made available as a
    research courtesy, "as is", potentially with defects, without
    any obligation on the part of the University of Washington to
    provide support, services, or repair;
 
    THE UNIVERSITY OF WASHINGTON DISCLAIMS ALL WARRANTIES, EXPRESS OR
    IMPLIED, WITH REGARD TO THIS SOFTWARE, INCLUDING WITHOUT LIMITATION
    ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
    PARTICULAR PURPOSE, AND IN NO EVENT SHALL THE UNIVERSITY OF
    WASHINGTON BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL
    DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR
    PROFITS, WHETHER IN AN ACTION OF CONTRACT, TORT (INCLUDING
    NEGLIGENCE) OR STRICT LIABILITY, ARISING OUT OF OR IN CONNECTION WITH
    THE USE OR PERFORMANCE OF THIS SOFTWARE.
  */
 /* **********************************************************************
     Copyright 2005 Rensselaer Polytechnic Institute. All worldwide rights reserved.
 
     Redistribution and use of this distribution in source and binary forms,
     with or without modification, are permitted provided that:
        The above copyright notice and this permission notice appear in all
         copies and supporting documentation;
 
         The name, identifiers, and trademarks of Rensselaer Polytechnic
         Institute are not used in advertising or publicity without the
         express prior written permission of Rensselaer Polytechnic Institute;
 
     DISCLAIMER: The software is distributed" AS IS" without any express or
     implied warranty, including but not limited to, any implied warranties
     of merchantability or fitness for a particular purpose or any warrant)'
     of non-infringement of any current or pending patent rights. The authors
     of the software make no representations about the suitability of this
     software for any particular purpose. The entire risk as to the quality
     and performance of the software is with the user. Should the software
     prove defective, the user assumes the cost of all necessary servicing,
     repair or correction. In particular, neither Rensselaer Polytechnic
     Institute, nor the authors of the software are liable for any indirect,
     special, consequential, or incidental damages related to the software,
     to the maximum extent the law permits.
 */
 package edu.rpi.cct.uwcal.access;
 
 import java.io.CharArrayWriter;
 import java.io.Serializable;
 
 /** Object to represent an encoded acl for a calendar entity or service.
  *
  *  @author Mike Douglass   douglm@rpi.edu
  */
 public class EncodedAcl implements Serializable {
   /** We represent the acl as a sequence of characters which we try to
       process with the minimum of overhead.
    */
   private char[] encoded;
 
   /** Current position in the acl */
   private int pos;
 
  /* When encoding an acl we build it here.
    */
  private transient CharArrayWriter caw;
 
   /** Set an encoded value
    *
    * @param val char[] encoded value
    */
   public void setEncoded(char[] val) {
     encoded = val;
     pos = 0;
   }
 
   /** Get the encoded value
    *
    * @return char[] encoded value
    */
   public char[] getEncoded() {
     return encoded;
   }
 
   /** Provide segment of input for debugging and errors
    *
    * @return String segment
    */
   public String getErrorInfo() {
     StringBuffer sb = new StringBuffer();
 
     sb.append("at ");
     sb.append(pos - 1);
     sb.append(" in '");
     sb.append(encoded);
     sb.append("'");
 
     return sb.toString();
   }
 
   /* ====================================================================
    *                 Decoding methods
    * ==================================================================== */
 
   /** Get next char from encoded value. Return < 0 for no more
    *
    * @return char value
    */
   public char getChar() {
     if ((encoded == null) || (pos == encoded.length)) {
       return (char)-1;
     }
 
     char c = encoded[pos];
     pos++;
 
     return c;
   }
 
   /** Back off one char
    *
    * @throws AccessException
    */
   public void back() throws AccessException {
     back(1);
   }
 
   /** Back off n chars
    *
    * @param n   int number of chars
    * @throws AccessException
    */
   public void back(int n) throws AccessException {
     if (pos - n < 0) {
       throw AccessException.badACLRewind();
     }
 
     pos -= n;
   }
 
   /** Get current position
    *
    * @return int position
    */
   public int getPos() {
     return pos;
   }
 
   /** Set current position
    *
    * @param val  int position
    */
   public void setPos(int val) {
     pos = val;
   }
 
   /** Get number of chars remaining
    *
    * @return int number of chars remaining
    */
   public int remaining() {
     if (encoded == null) {
       return 0;
     }
     return encoded.length - pos;
   }
 
   /** Test for more
    *
    * @return boolean true for more
    */
   public boolean hasMore() {
     return remaining() > 0;
   }
 
   /** Test for no more
    *
    * @return boolean true for no more
    */
   public boolean empty() {
     return (encoded == null) || (encoded.length == 0);
   }
 
   /** Rewind to the start
    */
   public void rewind() {
     pos = 0;
   }
 
   /** Return the value of a blank terminated length. On success current pos
    * has been incremented.
    *
    * @return int length
    * @throws AccessException
    */
   public int getLength() throws AccessException {
     int res = 0;
 
     for (;;) {
       char c = getChar();
       if (c == ' ') {
         break;
       }
 
       if (c < 0) {
         throw AccessException.badACLLength();
       }
 
       if ((c < '0') || (c > '9')) {
         throw AccessException.badACL("digit=" + c);
       }
 
       res = res * 10 + Character.digit(c, 10);
     }
 
     return res;
   }
 
   /** Get a String from the encoded acl at the current position.
    *
    * @return String decoded String value
    * @throws AccessException
    */
   public String getString() throws AccessException {
     if (getChar() == 'N') {
       return null;
     }
     back();
     int len = getLength();
 
     if ((encoded.length - pos) < len) {
       throw AccessException.badACLLength();
     }
 
     String s = new String(encoded, pos, len);
     pos += len;
 
     return s;
   }
 
   /** Skip a String from the encoded acl at the current position.
    *
    * @throws AccessException
    */
   public void skipString() throws AccessException {
     if (getChar() == 'N') {
       return;
     }
 
     back();
     int len = getLength();
     pos += len;
   }
 
   /* ====================================================================
    *                 Encoding methods
    * ==================================================================== */
 
   /** Get ready to encode
    *
    */
   public void startEncoding() {
     caw = new CharArrayWriter();
   }
 
   /** Encode a blank terminated 0 prefixed length.
    *
    * @param len
    * @throws AccessException
    */
   public void encodeLength(int len) throws AccessException {
     try {
       String slen = String.valueOf(len);
       caw.write('0');
       caw.write(slen, 0, slen.length());
       caw.write(' ');
     } catch (Throwable t) {
       throw new AccessException(t);
     }
   }
 
   /** Encode a String with length prefix. String is encoded as <ul>
    * <li>One byte 'N' for null string or</li>
    * <li>length {@link #encodeLength(int)} followed by</li>
    * <li>String value.</li>
    * </ul>
    *
    * @param val
    * @throws AccessException
    */
   public void encodeString(String val) throws AccessException {
     try {
       if (val == null) {
         caw.write('N'); // flag null
       } else {
         encodeLength(val.length());
         caw.write(val, 0, val.length());
       }
     } catch (AccessException ae) {
       throw ae;
     } catch (Throwable t) {
       throw new AccessException(t);
     }
   }
 
   /** Add a character
    *
    * @param c char
    * @throws AccessException
    */
   public void addChar(char c) throws AccessException {
     try {
       caw.write(c);
     } catch (Throwable t) {
       throw new AccessException(t);
     }
   }
 
   /** Get the current encoed value
    *
    * @return char[] encoded value
    */
   public char[] getEncoding() {
     return caw.toCharArray();
   }
 
   /* ====================================================================
    *                   Object methods
    * ==================================================================== */
 /*
   public int hashCode() {
     return 31 * entityId * entityType;
   }
 
   public boolean equals(Object obj) {
     if (this == obj) {
       return true;
     }
 
     if (obj == null) {
       return false;
     }
 
     if (!(obj instanceof AttendeeVO)) {
       return false;
     }
 
     AttendeePK that = (AttendeePK)obj;
 
     return (entityId == that.entityId) &&
            (entityType == that.entityType);
   }
   */
 
   public String toString() {
     StringBuffer sb = new StringBuffer();
 
     sb.append("AclVO{");
 //    sb.append(entityId);
     sb.append("}");
 
     return sb.toString();
   }
 
   /*
   public Object clone() {
     return new AttendeePK(getEntityId(),
                           getEntityType());
   }*/
 }
 
