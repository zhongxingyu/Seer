 /*
  *  hbIRCS
  *  
  *  Copyright 2005 Boris HUISGEN <bhuisgen@hbis.fr>
  * 
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Library General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
  */
 
 package fr.hbis.ircs;
 
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.charset.CharacterCodingException;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetDecoder;
 import java.nio.charset.CharsetEncoder;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import fr.hbis.ircs.lib.acl.ACL;
 import fr.hbis.ircs.lib.acl.ACLEntry;
 import fr.hbis.ircs.lib.acl.ACLRule;
 
 /**
  * The class <code>Utilities</code> implements all utilities functions used by
  * this program.
  * 
  * @author bhuisgen
  */
 public class Utilities
 {
 	/**
 	 * Private constructor.
 	 */
 	private Utilities ()
 	{
 	}
 
 	/*
 	 * String helpers
 	 */
 
 	/**
 	 * Returns a copy of the string with leading and trailing white space
 	 * characters omitted.
 	 * 
 	 * @param str
 	 *            the string to trim.
 	 * 
 	 * @return a copy of the string with leading and trailing white space
 	 *         removed, or this string if it has no leading or trailing white
 	 *         space.
 	 */
 	public static final String trim (String str)
 	{
 		if (str == null)
 			throw new NullPointerException ();
 
 		int len = str.length ();
 		int st = 0;
 		char[] val = str.toCharArray ();
 
 		while ((st < len) && (val[st] == ' '))
 		{
 			st++;
 		}
 
 		while ((st < len) && (val[len - 1] == ' '))
 		{
 			len--;
 		}
 
 		if ((st > 0) || (len < str.length ()))
 			return (str.substring (st, len));
 		else
 			return (str);
 	}
 
 	/**
 	 * Returns a copy of the string with leading white space characters omitted.
 	 * 
 	 * @param str
 	 *            the string to trim.
 	 * 
 	 * @return a copy of the string with leading white space removed, or this
 	 *         string if it has no leading white space.
 	 */
 	public static final String ltrim (String str)
 	{
 		if (str == null)
 			throw new NullPointerException ();
 
 		char[] buf = str.toCharArray ();
 		int len = str.length ();
 		int i = 0;
 
 		while ((i < len) && (buf[i] == ' '))
 		{
 			i++;
 		}
 
 		if (i > 0)
 			return (str.substring (i));
 		else
 			return (str);
 	}
 
 	/**
 	 * Returns a copy of the string with trailing white space characters
 	 * omitted.
 	 * 
 	 * @param str
 	 *            the string to trim.
 	 * 
 	 * @return a copy of the string with trailing white space removed, or this
 	 *         string if it has no trailing white space.
 	 */
 	public static final String rtrim (String str)
 	{
 		if (str == null)
 			throw new NullPointerException ();
 
 		char[] buf = str.toCharArray ();
 		int len = str.length ();
 
 		while ((len > 0) && (buf[len - 1] == ' '))
 		{
 			len--;
 		}
 
 		if (len < str.length ())
 			return (str.substring (0, len));
 		else
 			return (str);
 	}
 
 	/**
 	 * Encrypts a string with the SHA algorithm.
 	 * 
 	 * @param str
 	 *            the string to encrypt.
 	 * @return the encrypted string or <code>null</code> if the encryption has
 	 *         failed.
 	 */
 	public static final byte[] encrypt (String str)
 	{
 		if (str == null)
 			throw new NullPointerException ();
 
 		try
 		{
 			return (MessageDigest.getInstance ("SHA").digest (str.getBytes ()));
 		}
 		catch (NoSuchAlgorithmException noSuchAlgorithmException)
 		{
 			return (null);
 		}
 	}
 
 	/*
 	 * ByteBuffer helpers
 	 */
 
 	/**
 	 * Encode a message to a byte buffer with the given charset.
 	 * 
 	 * @param msg
 	 *            the message to encode.
 	 * @param charset
 	 *            the charset of the encoder; <code>null</code> to use default
 	 *            charset.
 	 * 
 	 * @return the <code>ByteBuffer</code> object of the encoded message.
 	 */
 	public static final ByteBuffer encodeMessage (String msg, Charset charset)
 	{
 		if (msg == null)
 			throw new NullPointerException ();
 
 		CharsetEncoder encoder;
 
 		if (charset != null)
 			encoder = charset.newEncoder ();
 		else
 			encoder = Charset.defaultCharset ().newEncoder ();
 
 		ByteBuffer buffer = null;
 
 		try
 		{
 			CharBuffer c = CharBuffer.wrap (msg.toCharArray ());
 			buffer = encoder.encode (c);
 		}
 		catch (CharacterCodingException characterCodingException)
 		{
 			return (null);
 		}
 
 		return (buffer);
 	}
 
 	/**
 	 * Decodes a message from a byte buffer with the given charset.
 	 * 
 	 * @param buffer
 	 *            the byte buffer to decoded.
 	 * @param charset
 	 *            the charset of the decoder; <code>null</code> to use default
 	 *            charset.
 	 * 
 	 * @return the <code>String</code> object of the decoded message
 	 */
 	public static final String decodeMessage (ByteBuffer buffer, Charset charset)
 	{
 		if (buffer == null)
 			throw new NullPointerException ();
 
 		CharsetDecoder decoder;
 
 		if (charset != null)
 			decoder = charset.newDecoder ();
 		else
 			decoder = Charset.defaultCharset ().newDecoder ();
 
 		CharBuffer cBuffer = null;
 
 		try
 		{
 			cBuffer = decoder.decode (buffer);
 		}
 		catch (CharacterCodingException characterCodingException)
 		{
 			return (null);
 		}
 
 		return (cBuffer.toString ());
 	}
 
 	/*
 	 * ACL helpers
 	 */
 
 	/**
	 * Checks if an ACL entry is matched by a mask.
 	 * 
 	 * @param acl
	 *            the ACL.
 	 * @param aclName
	 *            the ACL entry.
 	 * @param mask
 	 *            the mask.
 	 * 
	 * @return <code>true</code> if and only if the ACL entry is matched;
 	 *         <code>false</code> otherwise.
 	 */
 	public final static boolean isACLMatched (ACL acl, String aclName,
 			String mask)
 	{
 		if (acl == null)
 			throw new IllegalArgumentException ("invalid acl");
 
 		if ((aclName == null) || ("".equals (aclName)))
 			throw new IllegalArgumentException ("invalid acl name");
 
 		if ((mask == null) || ("".equals (mask)))
 			throw new IllegalArgumentException ("invalid mask");
 
 		ACLEntry aclEntry = acl.getEntry (aclName);
 		if (aclEntry == null)
 			return (false);
 
 		ACLRule[] rules = aclEntry.rules ();
 		for (ACLRule rule : rules)
 		{
 			if (rule.getMask ().equals (Constants.CONFIG_ACL_ALL))
 				return (rule.isAllowRule () ? true : false);
 			else
 			{
 				ACLEntry maskEntry = acl.getEntry (rule.getMask ());
 				if (maskEntry != null)
 					return (Utilities.isACLMatched (acl, maskEntry.getName (),
 							mask));
 				else
 				{
 					if (Utilities.isACLMaskMatched (mask, rule.getMask ()))
 						return (rule.isAllowRule () ? true : false);
 				}
 			}
 		}
 
 		return (false);
 	}
 
 	/**
 	 * Checks if a a given buffer matches a ACL mask.
 	 * 
 	 * @param buffer
 	 *            the buffer.
 	 * @param mask
 	 *            the mask.
 	 * 
 	 * @return <code>true</code> if and only if the buffer matches the mask;
 	 *         <code>false</code> otherwise.
 	 */
 	private static final boolean isACLMaskMatched (String buffer, String mask)
 	{
 		int i, j;
 		char c;
 
 		for (i = 0, j = 0; i < mask.length (); i++, j++)
 		{
 			if (mask.charAt (i) == Constants.ACL_WILDCARD_ALL)
 			{
 				// end mask
 				if (i + 1 >= mask.length ())
 					return (true);
 
 				c = Character.toLowerCase (mask.charAt (i + 1));
 
 				// '+' -> ' '
 				if (c == Constants.ACL_WILDCARD_ONE)
 					c = ' ';
 
 				// seach the character
 				for (; Character.toLowerCase (buffer.charAt (j)) != c; j++)
 				{
 					if (j == buffer.length ())
 						return (false);
 				}
 
 				j--;
 			}
 			else if (mask.charAt (i) == Constants.ACL_WILDCARD_ONE)
 			{
 				if (buffer.charAt (j) != ' ')
 					return (false);
 			}
 			/*
 			 * current character is not special
 			 */
 			else if (Character.toLowerCase (mask.charAt (i)) != Character
 					.toLowerCase (buffer.charAt (j)))
 				return (false);
 		}
 
 		// mask and buffer ends
 		if (j >= buffer.length ())
 			return (true);
 
 		return (false);
 	}
 }
