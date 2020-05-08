 ////////////////////////////////////////////////////////////////////////////////
 //
 //  ADOBE SYSTEMS INCORPORATED
 //  Copyright 2004-2007 Adobe Systems Incorporated
 //  All Rights Reserved.
 //
 //  NOTICE: Adobe permits you to use, modify, and distribute this file
 //  in accordance with the terms of the license agreement accompanying it.
 //
 ////////////////////////////////////////////////////////////////////////////////
 
 /*
  * Written by Jeff Dyer
  * Copyright (c) 1998-2003 Mountain View Compiler Company
  * All rights reserved.
  */
 
 package macromedia.asc.parser;
 
 import macromedia.asc.util.*;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringReader;
 import java.io.UnsupportedEncodingException;
 
 import static macromedia.asc.parser.CharacterClasses.*;
 import static macromedia.asc.embedding.avmplus.Features.*;
 
 /**
  * InputBuffer.h
  *
  * Filters and buffers characters from the input stream.
  *
  * @author Jeff Dyer
  */
 public class InputBuffer
 {
	private StringBuilder text;
 	// Used by Flex's OffsetInputBuffer.java
 	protected IntList line_breaks;
 	private boolean atEOF = false;
 	
 	public Reader in;
 	private int pushBackChar = -1;
 	public String origin;
 
 	public StringBuilder curr_line;
 	public StringBuilder prev_line;
 	public StringBuilder raw_curr_line; // un-normalized verison of curr_line
 
 	public int curr_line_offset;
 	public int prev_line_offset;
 
 	public int pos;
 	public int colPos, lnNum; // <0,0> is the position of the first character.
 	
 	public boolean has_unixnewlines;
 
     public boolean report_pos = true;
 
     private byte[] bom = new byte[3];
 
 	public InputBuffer(InputStream in, String encoding, String origin) // Init lineA to make distinct from curr_line (see nextchar)
 	{
 		this.in = createReader(in, encoding);
 		this.text = new StringBuilder(8192);
 
 		init(origin, 0);
 	}
 
 	public InputBuffer(String in, String origin)
 	{
 		this.in = new StringReader(in);
 		this.text = new StringBuilder(in.length() + 10);
 
 		init(origin, 0);
 	}
 
 	/**
 	 * No arg constructor for subclasses that aren't InputStream or String based.
 	 */
 	protected InputBuffer()
 	{
 	}
 
 	private void init(String origin, int pos)
 	{
 		this.origin = origin;
 		curr_line = new StringBuilder(80);
 		prev_line = new StringBuilder(80);
 		prev_line_offset = -1;
 		this.pos = pos;
 		lnNum = -1;
 		colPos = 0;
 		raw_curr_line = new StringBuilder(80);
 
 		has_unixnewlines = false;
 
 		line_breaks = new IntList(200);
 		line_breaks.add(0);
 	}
 
 	private Reader createReader(InputStream in, String encoding)
 	{
 		// return new BufferedReader(new InputStreamReader(in));
 		try
 		{
 			in.read(bom, 0, 3);
 		}
 		catch (IOException ex)
 		{
 			ex.printStackTrace();
 			return new StringReader("");
 		}
 
 		if (bom[0] == (byte)0xef && bom[1] == (byte)0xbb && bom[2] == (byte)0xbf)
 		{
 			try
 			{
 				return new BufferedReader(new InputStreamReader(in, "UTF8"));
 			}
 			catch (UnsupportedEncodingException ex)
 			{
 				return new BufferedReader(new InputStreamReader(in));
 			}
 		}
 		else if (bom[0] == (byte)0xff && bom[1] == (byte)0xfe || bom[0] == (byte)0xfe && bom[1] == (byte)0xff)
 		{
 			try
 			{
 				return new BufferedReader(new InputStreamReader(new InputBufferStream(bom, in), "UTF16"));
 			}
 			catch (UnsupportedEncodingException ex)
 			{
 				return new BufferedReader(new InputStreamReader(new InputBufferStream(bom, in)));
 			}
 		}
         else if( encoding  != null )
         {   // try the user specified encoding if no BOM, and an encoding was specified.  If that doesn't work
             // try the platform default encoding.
             try
             {
                 return new BufferedReader(new InputStreamReader(new InputBufferStream(bom, in), encoding));
             }
             catch (UnsupportedEncodingException ex)
             {
                 return new BufferedReader(new InputStreamReader(new InputBufferStream(bom, in)));
             }
         }
 		else
 		{   // c++ version assumes input is utf8, so in the abscense of header info lets try that first.
 			try
 			{
 				return new BufferedReader(new InputStreamReader(new InputBufferStream(bom, in), "UTF8"));
 			}
 			catch (UnsupportedEncodingException ex)
 			{
 				return new BufferedReader(new InputStreamReader(new InputBufferStream(bom, in)));
 			}
 		}
 	}
 
 	/*
 	 * read
 	 *
 	 * Read the next character from the input reader and store it in a buffer
 	 * containing the full text of the program.
 	 */
 
 	public int read()
 	{
 		if (atEOF)
 		{
 			return -1;
 		}
 		if (pushBackChar != -1)
 		{
 			int result = pushBackChar;
 			pushBackChar = -1;
 			++pos;
 			return result;
 		}
 		try
 		{
 			int c;
 			c = in.read();
 			if (c == -1)
 			{               
 				atEOF = true;
 			}
             else
             {
                 text.append((char) c);
                 ++pos;
             }
 
 			return c;
 		}
 		catch (IOException ex)
 		{
 			atEOF = true;
 			++pos;
 			return -1;
 		}
 	}
 
 	/*
 	 * text
 	 *
 	 * Provide the caller with a reference to the full text of the program.
 	 */
 
 	public String source()
 	{
 		return text.toString();
 	}
 
 	public String source(int begin, int end)
 	{
 		return text.substring(begin, end);
 	}
 
 	/*
 	 * nextchar
 	 *
 	 * The basic function of nextchar() is to fetch the next character,
 	 * in the input array, increment next and return the fetched character.
 	 *
 	 * To simplify the Scanner, this method also does the following:
 	 * 1. normalizes certain special characters to a common character.
 	 * 2. skips unicode format control characters (category Cf).
 	 * 3. keeps track of line breaks, line position and line number.
 	 * 4. treats <cr>+<lf> as a single line terminator.
 	 * 5. returns 0 when the end of input is reached.
 	 */
 	public int nextchar()
 	{
 		return nextchar(false);
 	}
 
 	public int nextchar(boolean get_unnormalized)
 	{
 		int c = -1;
 		
 		// If the last character was at the end of a line,
 		// then swap buffers and fill the new one with characters
 		// from the input reader.
 
 		if (colPos == curr_line.length())
 		{
 			if (!atEOF)
 			{
 				lnNum++;
 			}
 			
 			colPos = 0;
 			raw_curr_line.setLength(0);
 
 			// If the current character is a newline, then read
 			// the next line of input into the other input buffer.
 
 			StringBuilder prevSave = prev_line;
 			prev_line = curr_line;
 			prev_line_offset = curr_line_offset;
 
 			curr_line = prevSave;
 			curr_line.setLength(0);
 			curr_line_offset = pos;
 
 			// Fill the current line with characters.
 
 			while (c != '\n' && c != 0)
 			{
 				int lastChar = c;
 				c = read();
 
 				if (lastChar == '\r' && c != '\n')
 				{
 					// A bare carriage return was encountered, not a CR-LF.
 					// Treat as line break by breaking out of loop.
 					pushBackChar = c;
 					pos--;
 					break;
 				}
 
 				// Skip Unicode 3.0 format-control (general category Cf in
 				// Unicode Character Database) characters.
 
 				while (true)
 				{
 					switch (c)
 					{
 						case 0x070f: // SYRIAC ABBREVIATION MARK
 						case 0x180b: // MONGOLIAN FREE VARIATION SELECTOR ONE
 						case 0x180c: // MONGOLIAN FREE VARIATION SELECTOR TWO
 						case 0x180d: // MONGOLIAN FREE VARIATION SELECTOR THREE
 						case 0x180e: // MONGOLIAN VOWEL SEPARATOR
 						case 0x200c: // ZERO WIDTH NON-JOINER
 						case 0x200d: // ZERO WIDTH JOINER
 						case 0x200e: // LEFT-TO-RIGHT MARK
 						case 0x200f: // RIGHT-TO-LEFT MARK
 						case 0x202a: // LEFT-TO-RIGHT EMBEDDING
 						case 0x202b: // RIGHT-TO-LEFT EMBEDDING
 						case 0x202c: // POP DIRECTIONAL FORMATTING
 						case 0x202d: // LEFT-TO-RIGHT OVERRIDE
 						case 0x202e: // RIGHT-TO-LEFT OVERRIDE
 						case 0x206a: // INHIBIT SYMMETRIC SWAPPING
 						case 0x206b: // ACTIVATE SYMMETRIC SWAPPING
 						case 0x206c: // INHIBIT ARABIC FORM SHAPING
 						case 0x206d: // ACTIVATE ARABIC FORM SHAPING
 						case 0x206e: // NATIONAL DIGIT SHAPES
 						case 0x206f: // NOMINAL DIGIT SHAPES
 						case 0xfeff: // ZERO WIDTH NO-BREAK SPACE
 						case 0xfff9: // INTERLINEAR ANNOTATION ANCHOR
 						case 0xfffa: // INTERLINEAR ANNOTATION SEPARATOR
 						case 0xfffb: // INTERLINEAR ANNOTATION TERMINATOR
 							c = read();
 							continue; // skip it.
 						default:
 							break;
 					}
 					break; // out of while loop.
 				}
 
 				switch(c)
 				{
 				// Line terminators.
 				case 0x000a:
 				case 0x000d:
 				case 0x2028:
 				case 0x2029:
 					raw_curr_line.append((char) c);
 					// skip line stuff for \r\n
 					if(lastChar == '\r' && c == '\n') {
 						line_breaks.set(lnNum + 1, pos);
 					} else {
 						curr_line.append('\n'); // normalize linebreaks to \n
 						line_breaks.resize(lnNum + 1); // resize the type vector, if needed
 						line_breaks.set(lnNum + 1, pos);
 					}
 					break;
 				// White space
 				case 0x0009:
 				case 0x000b:
 				case 0x000c:
 				case 0x0020:
 				case 0x00a0:
 					raw_curr_line.append((char) c);
 					c = ' ';
 					curr_line.append((char) c);
 					break;
 				// End of line
 				case -1:
 					c = 0;
 					raw_curr_line.append((char) c);
 					curr_line.append((char) c);
 					break;
 				// All other characters.
 				default:
 					// Use c as is.
 					raw_curr_line.append((char) c);
 					curr_line.append((char) c);
 				}
 			}
 
             // cn:  this if statement is wrong and should be removed.  \n is now in returned on the previous call to the call that
             //       re-fills the curr_line and exexutes this block.  The code below makes us return\n twice when get_unnormalized is used and the line was only terminated by a 0xA, not a 0xD,0xA.  Too scary to change at this
             //       point, but should be fixed soon after we ship.   Only known bug is handling of line continuation sequence ("\0xA") in a String when the string is withing an XML CDATA node (all newline sequences are normalized to a single 0xA in XML)
             if (get_unnormalized)  // return the newline we aught have unless
                 return '\n';
 		}
 
 		// Get the next character.	
 		c = get_unnormalized ? raw_curr_line.charAt(colPos++) : curr_line.charAt(colPos++);
 		
 		return c;
 	}
 
 	/*
 	 * retract
 	 *
 	 * Backup one character position in the input. If at the beginning
 	 * of the line, then swap buffers and point to the last character
 	 * in the other buffer.
 	 */
 
 	public void retract()
 	{
 		if ((--colPos) < 0)
 		{
 			StringBuilder currSave = curr_line;
 			curr_line = prev_line;
 			prev_line = currSave;
 			prev_line.setLength(0);
 			--lnNum;
 			colPos = curr_line.length() - 1;
 			curr_line_offset = prev_line_offset;
 		}
 		return;
 	}
 
 	/*
 	 * classOfNext
 	 */
 
     // utilities used by classOfNext
     final private int unHex(char c)
     {
        return Character.digit(c,16);
     }
 
     final private boolean isHex(char c)
     {
         return Character.digit(c,16) != -1;
     }
 
 	// utility for java branch only to convert a Character.getType() unicode type specifier to one of the enums
 	//  defined in CharacterClasses.java.  In the c++ branch, we use the character as an index to a table defined
 	//  in CharacterClasses.h
 	final private char javaTypeOfToCharacterClass(int javaClassOf)
 	{
 		switch(javaClassOf)
 		{
 			case Character.UPPERCASE_LETTER:		    return Lu;	// = 0x01 Letter, Uppercase
 			case Character.LOWERCASE_LETTER:		    return Ll;	// = 0x02 Letter, Lowercase
 			case Character.TITLECASE_LETTER:		    return Lt;	// = 0x03 Letter, Titlecase
 			case Character.NON_SPACING_MARK:		    return Mn;	// = 0x04 Mark, Non-Spacing
 			case Character.COMBINING_SPACING_MARK:	    return Mc;	// = 0x05 Mark, Spacing Combining
 			case Character.ENCLOSING_MARK:			    return Me;	// = 0x06 Mark, Enclosing
 			case Character.DECIMAL_DIGIT_NUMBER:	    return Nd;	// = 0x07 Number, Decimal Digit
 			case Character.LETTER_NUMBER:			    return Nl;	// = 0x08 Number, Letter
 			case Character.OTHER_NUMBER:			    return No;	// = 0x09 Number, Other
 			case Character.SPACE_SEPARATOR:			    return Zs;	// = 0x0a Separator, Space
 			case Character.LINE_SEPARATOR:			    return Zl;	// = 0x0b Separator, Line
 			case Character.PARAGRAPH_SEPARATOR:		    return Zp;	// = 0x0c Separator, Paragraph
 			case Character.CONTROL:                     return Cc;	// = 0x0d Other, Control
 			case Character.FORMAT:                      return Cf;	// = 0x0e Other, Format
 			case Character.SURROGATE:                   return Cs;	// = 0x0f Other, Surrogate
 			case Character.PRIVATE_USE:                 return Co;	// = 0x10 Other, Private Use
 			case Character.UNASSIGNED:                  return Cn;	// = 0x11 Other, Not Assigned (no characters in the file have this property)
 
 				// Non-normative classes.
 			case Character.MODIFIER_LETTER:			    return Lm;	// = 0x12 Letter, Modifier
 			case Character.OTHER_LETTER:			    return Lo;	// = 0x13 Letter, Other
 			case Character.CONNECTOR_PUNCTUATION:       return Pc;	// = 0x14 Punctuation, Connector
 			case Character.DASH_PUNCTUATION:		    return Pd;	// = 0x15 Punctuation, Dash
 			case Character.START_PUNCTUATION:		    return Ps;	// = 0x16 Punctuation, Open
 			case Character.END_PUNCTUATION:			    return Pe;	// = 0x17 Punctuation, Close
 			case Character.INITIAL_QUOTE_PUNCTUATION:	return Pi;	// = 0x18 Punctuation, Initial quote (may behave like Ps or Pe depending on usage)
 			case Character.FINAL_QUOTE_PUNCTUATION:		return Pf;	// = 0x19 Punctuation, Final quote (may behave like Ps or Pe depending on usage)
 			case Character.OTHER_PUNCTUATION:           return Po;	// = 0x1a Punctuation, Other
 			case Character.MATH_SYMBOL:                 return Sm;	// = 0x1b Symbol, Math
 			case Character.CURRENCY_SYMBOL:             return Sc;	// = 0x1c Symbol, Currency
 			case Character.MODIFIER_SYMBOL:             return Sk;	// = 0x1d Symbol, Modifier
 			case Character.OTHER_SYMBOL:                return So;	// = 0x1e Symbol, Other
 			
 			default: // DIRECTIONALITY_LEFT_TO_RIGHT, DIRECTIONALITY_RIGHT_TO_LEFT, DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC, etc.
 				// DIRECTIONALITY_EUROPEAN_NUMBER, etc.
 				return Cn; // or So ?
 		}
 	}
 
 	public char classOfNext()
 	{
 		// Return the Unicode character class of the current
 		// character, which is pointed to by 'next-1'.
         char c;
 
         if( curr_line.charAt(colPos-1) == '\\' &&
             curr_line.charAt(colPos) == 'u'    &&
             isHex(curr_line.charAt(colPos+1))  &&
             isHex(curr_line.charAt(colPos+2))  &&
             isHex(curr_line.charAt(colPos+3))  &&
             isHex(curr_line.charAt(colPos+4))  )
         {
            int ic =  (((((unHex(curr_line.charAt(colPos+1))  << 4) +
                           unHex(curr_line.charAt(colPos+2))) << 4) +
                           unHex(curr_line.charAt(colPos+3))) << 4) +
                           unHex(curr_line.charAt(colPos+4));
            /*  This is a jdk 1.5 only feature.  However, we only support 16bit chars, so its
 			*    o.k. to cast to (char) instead
 			* char[] ca = Character.toChars(ic);
             * c = ca[0];
 			*/
 			c = (char)(ic);
 
            nextchar();  // Skip over entire escaped char
            nextchar();
            nextchar();
            nextchar();
            nextchar();
         }
         else
         {
             // No need to look for utf8 lead byte and convert utf8 to unicode, the InputReader already
             //  does that (because we requested the UTF8 encoding option in the constructor).
             c = curr_line.charAt(colPos-1);
          }
 
 
          return javaTypeOfToCharacterClass(Character.getType(c));
 	}
 
 	/*
 	 * positionOfNext
 	 */
 
 	public int positionOfNext()
 	{
 		return curr_line_offset + colPos - 1;
 	}
 
 	/*
 	 * positionOfMark
 	 */
 
 	public int positionOfMark()
 	{
         if( !report_pos )
             return -1;
 
         final int ret;
 
 		// This may happen with abc imports
 		if(markLn < 0)
 		{
 			ret = positionOfNext();
 		}
 		// this happens when past the end of the file
 		else if(markLn >= line_breaks.size()) {
 			ret = markCol;
 		}
 		// otherwise all's normal
 		else {
 			ret = line_breaks.get(markLn)+markCol;
 		}
 
 		return ret;
 	}
 
 	/*
 	 * mark
 	 */
 
 	public int markCol;
 	public int markLn;
 
 	public int mark()
 	{
 		markLn = (lnNum == -1) ? 0 : lnNum; // In case nextchar hasn't been called yet.
 		markCol = colPos;
 		return markCol;
 	}
 
 	/*
 	 * copy
 	 */
 	public String copyWithoutInterpretingEscapedChars()
 	{
 		return curr_line.substring(markCol-1, colPos );
 	}
 
      /*  Copies a string from index <from> to <to>, interpreting escape characters
   		  Functionality split off from copy() so that we can call it on Parser's
   		  string_literal_buffer as well
   	 */
 	public String escapeString(StringBuilder source, int from, int to)
 	{
 		// C: only 1 string in 1000 needs escaping and the lengths of these strings are usually small,
 		//    so we can cut StringBuilder usage if we check '\\' up front.
 		int stop = to+1;
 		boolean required = false;
 
 		for (int i = from; i < stop; i++)
 		{
 			if (source.charAt(i) == '\\')
 			{
 				required = true;
 				break;
 			}
 		}
 
 		if (!required)
 		{
 			return source.substring(from, stop);
 		}
 
 		final StringBuilder buf = new StringBuilder(stop-from);
 		for (int i = from; i < stop; i++)
 		{
 			char c = source.charAt(i);
 			if (c == '\\')
 			{
 				int c2 = source.charAt(i + 1);
 				switch (c2)
 				{
 					case '\'':
 					case '\"':
 						continue;
 						
 					case '\\': // escaped escape char
 						c = '\\';
                         ++i;
                         break;
 
 					case 'u': // Token constructor will handle all embedded backslash u characters, within a string or not
                     {
                         int thisChar = 0;
                         int y, digit;
                         // calculate numeric value, bail if invalid
                         for( y=i+2; y<i+6 && y < to+1; y++ )
                         {
                             digit = Character.digit( source.charAt(y),16 );
                             if (digit == -1)
                                 break;
                             thisChar = (thisChar << 4) + digit;
                         }
                         if ( y != i+6 || Character.isDefined((char)thisChar) == false )  // if there was a problem or the char is invalid just escape the '\''u' with 'u'
                         {
                             c = source.charAt(++i);
                         }
                         else // use Character class to convert unicode codePoint into a char ( note, this will handle a wider set of unicode codepoints than the c++ impl does).
                         {
                             // jdk 1.5.2 only, but handles extended chars:  char[] ca = Character.toChars(thisChar);
                             c = (char)thisChar;
                             i += 5;
                         }
                         break;
                     }
 					default:
 				    {
 						if (PASS_ESCAPES_TO_BACKEND)
 						{
 							c = source.charAt(++i);
 							break; // else, unescape the unrecognized escape char
 						}
 	                    
 						switch (c2)
 						{
 							case 'b':
 								c = '\b';
 								++i;
 								break;
 							case 'f':
 								c = '\f';
 								++i;
 								break;
 							case 'n':
 								c = '\n';
 								++i;
 								break;
 							case 'r':
 								c = '\r';
 								++i;
 								break;
 							case 't':
 								c = '\t';
 								++i;
 								break;
 							case 'v':
 								// C: There is no \v in Java...
 								c = 0xb;
 								++i;
 								break;
 							case 'x':
 							{
 								if ( i+3 < colPos && isHex(source.charAt(i+2)) && isHex(source.charAt(i+3)))
 								{
 									c = (char) ((unHex(source.charAt(i+2)) << 4) + unHex(source.charAt(i+3)));
 									i += 3;
 									/*  Character.toChars is a jdk 1.5 only feature.  However, we only support 16bit chars, so its
 									 *    o.k. to cast to (char) instead
 									 * char[] ca = Character.toChars(ic);
 									 * c = ca[0];
 									 */
 								}
 								else // invalid number, just skip the '\' escape char
 								{
 									i++;
 									c = 'x';
 								}
                                 break;
 							} // end case 'x'
 
 							default:
 								c = source.charAt(++i);
 								break; // else, unescape the unrecognized escape char
 
 						} // end switch
 					}
 				} // end switch
 			}
 			buf.append(c);
 		}
 		return buf.toString();
 	}
 
 	/** Copies interpreting escaped characters */
 	public String copy()
 	{
 
 		/* Unnecessary to test anymore...
 			if (markLn != lnNum || markCol - 1 > colPos)
 			{
 			assert(false); // throw "Internal error: InputBuffer.copy()";
 			}
 			*/
 		return escapeString(curr_line, markCol-1, colPos-1);
 	}
 
     public String getLineText(int pos)
     {
         int i, end, a, len;
         for (i = 0; i < line_breaks.size() && (a = line_breaks.get(i)) <= pos && a >= 0 && i <= lnNum; i++);
 
         int begin = line_breaks.get(i - 1);
 
         for (end = begin, len = text.length();
             end < len && text.charAt(end) != 0x0a && 
             			 text.charAt(end) != 0x0d &&
             			 text.charAt(end) != 0x00
             			 /*&& c != 0x2028 && c != 0x2029*/;
             ++end);
 
         int size = end - begin;
 
         // C: 'size' could exceed the limit in ascap.exe and
         //    std::string.substr() happily returns the substring without saying
         //    index out of bound error...
         final String buf = (begin + size > text.length())
         				 ? text.substring(begin)
         				 : text.substring(begin, begin + size);
         return buf;
     }
 
     public int getLineBreakPos(int line){
     	return line_breaks.at(line-1);
     }
 
     public int getColPos(int pos)
 	{
         int line = getLnNum(pos);
 		return pos - ((line == 0) ? 0 : line_breaks.get(line - 1)) + 1;
 	}
 
     public int getColPos(int pos, int line)
     {
 	    return pos - ((line == 0) ? 0 : line_breaks.get(line - 1)) + 1;
     }
 
     public void getLnNumAndColPos(int pos, int line[], int col[])
 	{
 		line[0] = getLnNum (pos);
 		col[0]  = pos - ((line[0] == 0) ? 0 : line_breaks.get(line[0] - 1)) + 1;
 	}
 
 	//#define TESTOLDCODE
     public int getLnNum(int pos)
     {
 		int lo = 0;
 		int hi = lnNum;
 		while (hi >= lo)
 		{
 			int mid = (hi + lo) / 2; 
 			int val = line_breaks.get(mid);
 
 			if (val > pos) // too far forward, jump back and try again
 			{
 				hi = mid;
 			}
 			else if (val == pos)
 			{
 				return mid+1;
 			}
 			else
 			{
 				// guess is the last line, use it
 				if (mid == lnNum) {
                     if( pos > val)
                     {
                         // last line may not have had a line break, so won't show up in line_break table
                         // ok, because calculating the col will use the pos from the previous line, which is in the
                         // line_break table
                         return mid+1;
                     }
 					return mid;
 				}
 				
 				// if nextline > pos
 				if(line_breaks.size() == mid+1 || line_breaks.get(mid + 1) > pos) {
 					return mid + 1;
 				}
 
 				// else: need to jump forward some
 				lo = mid + 1;
 			}
 		}
 
 		// What do to if we fail to find a match?
 		// This never seems to get hit.
 		return lo;
     }
 
 
 	public static String getLinePointer(int pos)
 	{
 		final StringBuilder padding = new StringBuilder(pos);
 		for (int i = 0; i < pos - 1; ++i)
 		{
 			padding.append(".");
 		}
 		padding.append("^");
 		return padding.toString();
 	}
 
 	public void clearUnusedBuffers() 
 	{
 		// TODO: remove this and pre-size text
         text = new StringBuilder(text.toString());
 		try { in.close(); } catch (IOException e) {}
 		in = null;
 		curr_line = null;
 		prev_line = null;
 		bom = null;
 		
 		// text = null;
 		// line_breaks.clear();
 		// line_breaks = null;
 	}
 }
 
 final class InputBufferStream extends InputStream
 {
 	InputBufferStream(byte[] bom, final InputStream in)
 	{
 		this.bom = bom;
 		this.in = in;
 		index = 0;
 	}
 
 	private byte[] bom;
 	private final InputStream in;
 	private int index;
 
 	public int read() throws IOException
 	{
 		if(bom == null) {
 			return in.read();
 		}
 		
 		final int val = bom[index];
 		
 		if(index == 2) {
 			bom = null;
 		}
 		
 		++index;
 		
 		return val;
 	}
 
 	public int read(byte b[], int off, int len) throws IOException
 	{
 		if (bom == null)
 		{
 			return in.read(b, off, len);
 		}
 
 		int c, i = 0;
 		for (; i < len; i++)
 		{
 			c = read();
 			if (c == -1) {
 				return (i > 0 ? i : -1);
 			}
 
 			b[off + i] = (byte) c;
 		}
 		return i;
 	}
 
 	public long skip(long n) throws IOException
 	{
 		throw new UnsupportedOperationException("supports read() and close() only...");
 	}
 
 	public int available() throws IOException
 	{
 		final int num = in.available();
 		if (bom == null)
 		{
 			return num;
 		}
 		else
 		{
 			return (3 - index) + num;
 		}
 	}
 
 	public void close() throws IOException
 	{
 		in.close();
 	}
 
 	public synchronized void mark(int readlimit)
 	{
 		throw new UnsupportedOperationException("supports read() and close() only...");
 	}
 
 	public synchronized void reset() throws IOException
 	{
 		throw new UnsupportedOperationException("supports read() and close() only...");
 	}
 
 	public boolean markSupported()
 	{
 		return false;
 	}
 }
