 // Copyright (C) 2000, 2001, 2002, 2003, 2004 Philip Aston
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.util;
 
 
 /**
  * <p>StringFormatter for fixed width text cells</p>
  *
  * <p>All white space is converted to plain spaces.</p>
  *
  * <p>When flow policy is <code>FLOW_WORD_WRAP</code>, newlines in the
  * source are treated as preferred line breaks.</p>
  *
  * @author Philip Aston
  * @version $Revision$
  **/
 public class FixedWidthFormatter {
 
   /** Constant indicating left alignment. */
   public static final int ALIGN_LEFT = 0;
 
   /** Constant indicating centre alignment. */
   public static final int ALIGN_CENTRE = 1;
 
   /** Constant indicating right alignment. */
   public static final int ALIGN_RIGHT = 2;
 
   /** Constant indicating flow should be truncated. */
   public static final int FLOW_TRUNCATE = 8;
 
   /** Constant indicating flow should be wrapped. */
   public static final int FLOW_WRAP = 9;
 
   /** Constant indicating flow should be word-wrapped. */
   public static final int FLOW_WORD_WRAP = 10;
 
   /** Blank space to copy for padding. **/
   private static final char[] s_space = new char[256];
 
   static {
     for (int i = 0; i < s_space.length; i++) {
       s_space[i] = ' ';
     }
   }
 
   private final int m_alignment;
   private final int m_flow;
   private final int m_width;
 
   /**
    * Constructor.
    *
    * @param alignment Alignment policy. One of {
    * <code>ALIGN_LEFT</code>, <code>ALIGN_CENTRE</code>,
    * <code>ALIGN_RIGHT</code> }
    * @param flow Flow policy. One of { <code>FLOW_TRUNCATE</code>,
    * <code>FLOW_WRAP</code>, <code>FLOW_WORD_WRAP</code> }
    * @param width The cell width.
    * @exception IllegalArgumentException If <code>alignment</code>,
    * <code>flow</code> or <code>width</code> are invalid.
    *
    */
   public FixedWidthFormatter(int alignment, int flow, int width) {
     if (alignment != ALIGN_LEFT &&
         alignment != ALIGN_CENTRE &&
         alignment != ALIGN_RIGHT) {
       throw new IllegalArgumentException("Invalid alignment value");
     }
 
     if (flow != FLOW_TRUNCATE &&
         flow != FLOW_WRAP &&
         flow != FLOW_WORD_WRAP) {
       throw new IllegalArgumentException("Invalid flow value");
     }
 
     if (width <= 0) {
       throw new IllegalArgumentException("Invalid width value");
     }
 
     m_alignment = alignment;
     m_flow = flow;
     m_width = width;
   }
 
   /**
    * <p>Search  to set splitPosition as follows:
    * <ol>
    * <li>First new line in first m_width+1 characters (we replace all
    * new lines with splits)</li>
    * <li>If no new line, last white space in first m_width+1 characters</li>
    *<li>If no new line or white space, the full width</li>
    * </ol></p>
    *
    * <p>If the buffer is less than m_width wide, only new lines are
    * taken into account. In this case, if no split is necessary -1
    * is returned.</p>
    *
    * @param buffer Ensure that buffer.length() > m_width
    * @return Split position in range [0, m_width]
    **/
   private int findWordWrapSplitPosition(StringBuffer buffer) {
     final int length = buffer.length();
     final int right = Math.min(length, m_width);
 
     int splitPosition = 0;
 
     while (splitPosition < right) {
       if (buffer.charAt(splitPosition) == '\n') {
         return splitPosition;
       }
 
       ++splitPosition;
     }
 
     if (length > m_width) {
       splitPosition = m_width;
 
       do {
         if (Character.isWhitespace(buffer.charAt(splitPosition))) {
           return splitPosition;
         }
       }
       while (--splitPosition >= 0);
 
       return m_width;
     }
     else {
       return -1;
     }
   }
 
   /**
    * Alter buffer to contain a single line according to the policy of
    * this <code>FixedWidthFormatter</code>. Insert remaining text at
    * the start of <code>remainder</code>.
    *
    * @param buffer Buffer to transform to a single line.
    * @param remainder Left overs.
    */
   public final void transform(StringBuffer buffer, StringBuffer remainder) {
 
     int length = buffer.length();
 
     switch (m_flow) {
 
     case FLOW_TRUNCATE:
       if (length > m_width) {
         // Truncate.
         buffer.setLength(m_width);
       }
       break;
 
     case FLOW_WRAP:
       if (length > m_width) {
         // We prepend our remainder to the existing one.
         remainder.insert(0, buffer.substring(m_width));
 
         // Truncate.
         buffer.setLength(m_width);
       }
       break;
 
     case FLOW_WORD_WRAP:
      if (m_width == 1 && length > 1) {
        // Enhancement to allow single column of "vertical"
        // text. Replace white space with line breaks.
        if (Character.isWhitespace(buffer.charAt(0))) {
          buffer.setCharAt(0, '\n');
        }
      }

       // end will be set to the length of the new buffer after
       // accounting for possible split position and trailing space.
       int end = length;
 
       final int splitPosition = findWordWrapSplitPosition(buffer);
 
       if (splitPosition >= 0) {
         // Search forward to ignore white space until the first new
         // line, and set everything from there on as the remainder.
         int nextText = splitPosition;
 
         while (nextText < length) {
           final char c = buffer.charAt(nextText);
 
           if  (!Character.isWhitespace(c)) {
             break;
           }
 
           ++nextText;
 
           if (c == '\n') {
             // If alignment is ALIGN_LEFT, white space after the new
             // line will become leading space on the next line.
             break;
           }
         }
 
         if (nextText < length) {
           remainder.insert(0, buffer.substring(nextText));
         }
 
         end = splitPosition;
       }
 
       // Strip trailing space.
       while (end > 0 && Character.isWhitespace(buffer.charAt(end - 1))) {
         --end;
       }
 
       buffer.setLength(end);
 
      if (m_alignment != ALIGN_LEFT && end > 0) {
         // Strip leading space.
         int start = 0;
 
         while (Character.isWhitespace(buffer.charAt(start))) {
           ++start;
         }
 
         buffer.delete(0, start);
       }
 
       break;
 
     default:
       throw new RuntimeException("Assertion failed: invalid flow");
     }
 
     length = buffer.length();
 
     // Canonicalise remaining space.
     for (int k = 0; k < length; k++) {
       if (Character.isWhitespace(buffer.charAt(k))) {
         buffer.setCharAt(k, ' ');
       }
     }
 
     if (length < m_width) {
       // Buffer is less than width, have to pad.
 
       switch (m_alignment) {
       case ALIGN_LEFT:
         buffer.append(s_space, 0, m_width - length);
         break;
 
       case ALIGN_CENTRE:
         final int charsLeft = (m_width - length + 1) / 2;
         final int charsRight = (m_width - length) / 2;
         buffer.insert(0, s_space, 0, charsLeft);
         buffer.append(s_space, 0, charsRight);
         break;
 
       case ALIGN_RIGHT:
         buffer.insert(0, s_space, 0, m_width - length);
         break;
 
       default:
         throw new RuntimeException("Assertion failed: invalid alignment");
       }
     }
   }
 
   /**
    * Convenience method.
    *
    * @param input Input text.
    * @return Formatted result.
    */
   public String format(String input) {
     final StringBuffer result = new StringBuffer();
     StringBuffer buffer = new StringBuffer(input);
 
     while (buffer.length() > 0) {
       final StringBuffer remainder = new StringBuffer();
       transform(buffer, remainder);
 
       if (result.length() > 0) {
         result.append("\n");
       }
 
       result.append(buffer);
 
       buffer = remainder;
     }
 
     return result.toString();
   }
 }
