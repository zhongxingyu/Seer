 /*
  * Vitry, copyright (C) Hans Hoglund 2011
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * See COPYING.txt for details.
  */
 package vitry.runtime.parse;
 
 import static java.lang.Math.min;
 import static java.lang.System.arraycopy;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.LineNumberReader;
 import java.io.PushbackReader;
 import java.io.Reader;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.Stack;
 
 import vitry.runtime.util.Utils;
 
 
 /**
  * Rewrites indentation as parentheses.
  * 
  * At the moment also handles comment stripping, this should be factored out.
  * Comments are indicate by any operator character at the beginning of a line
  * (no whitespace before).
  */
 public class IndentationReader extends Reader
     {   
         /**
          * Operator characters (sorted!).
          */
         private static final char[] OP_CHARS = {
             '!', '#', '$', '%', '&', '\'', '*', '+', ',', '-',
             '.', '/', ':', ';', '<', '=', '>', '?', '@', '\\', '^', 
             '_', '|', '~'
         };
         
         private Stack<Integer> levels = new Stack<Integer>();
 
         private final PushbackReader input;
         
         private char[] line = new char[10];
         int  length = 0;
         int position = 0;
         int linesRead = 0;
         int linesEmitted = 0;
         int state = 0;
         
         private static final int EOF = -1;
         
 
         public IndentationReader(Reader input) {
             this.input = new PushbackReader(input, 2);
             this.levels.push(0);
         }
         
         public int read() throws IOException {
             checkLine();
             if (state == EOF) return -1;
             return line[position++];
         }
         
         public int read(char[] sink, int offset, int length) throws IOException {
             int count = 0;
             int max = min(length, sink.length - offset);
 
             while (count < max) {
                 checkLine();
                 if (state == EOF) {
                     return -1; 
                 }
                 sink[count + offset] = line[position];
                 position++;
                 count++;
             }
             if (count > 0) {
                 return count;
             } else {
                 return -1;
             }
         }
 
         private void checkLine() throws IOException {
             if (position >= length) {
                 nextLine();                    
             }
         }
         
         /**
         * Fill [] with characters from the next line.
         * Update lpos, lend and lineRead.
          * If nothing more to read, set state to EOF and return. 
         * @throws IOException 
          */
         private void nextLine() throws IOException {
             int n = 0;
             boolean skip;
             int indent;
 
             do {
                 while(consumeBreak() > 0) linesRead++;
                 indent = consumeLineSpace();
                 
                char a = (char) peek();
                char b = (char) peek();
                 skip = (peek() == ';' || peek() == '\n');
                 while (skip && readLineChar() >= 0);
             } while (state != EOF && skip);
             
             while (levels.size() > 1 && indent < levels.peek() && linesEmitted >= 1) {
                 writeLine(n++, ')');
                 levels.pop();
             }
             if (levels.size() > 0 && indent == levels.peek() && linesEmitted >= 1) {
                 writeLine(n++, ')');
             }
             if (levels.size() > 0 && indent > levels.peek()) {
                 levels.push(indent);
             }
             if (linesEmitted >= 1)
                 writeLine(n++, '\n');
             linesEmitted++;
             
             for (int i = 0; i<indent; i++)
                 writeLine(n++, ' ');
             writeLine(n++, '(');
             
             int c;
             while (true) {
                 c = readLineChar();
                 if (c >= 0)
                     writeLine(n++, (char) c);
                 else
                     break;
             }
             
             while (state == EOF && levels.size() > 0) {
                 writeLine(n++, ')');
                 levels.pop();
             }
 
             
             position = 0;
             length = n;
         }
         
         private int peek() throws IOException {
             int a = input.read();
             if (a < 0) {
                 state = EOF;
                 return -1;
             }
             input.unread(a);
             return a;
         }
         
         private int readLineChar() throws IOException {
             int a = input.read();
             if (a < 0) {
                 state = EOF;
                 return -1;
             }
             if (a == '\n' || a == '\r') {
                 input.unread(a);
                 return -1;
             }
             return a;
         }
         
         private int consumeBreak() throws IOException {
             int a = input.read();
             int b = input.read();
             if (b < 0) {
                 state = EOF; 
             }
             if (a == '\n' && b == '\r') return 2;
             input.unread(b);
             if (a == '\n' || a == '\r') return 1;
             input.unread(a);
             return 0;
         }
 
         
 
         private int consumeLineSpace() throws IOException {
             int n = 0;
             int c;
             do {
                 c = input.read();
                 n++;
             } while (c >= 0 && c == ' ' || c == '\t'); 
             if (c < 0) {
                 state = EOF;
             } else {
                 input.unread(c);
                 n--;
             }
             return n;
         }
 
 
         
         private void writeLine(int n, char c) {
 System.err.write(c);
 System.err.flush();
             if (line.length <= n) {
                 char[] a = new char[(int) (n * 1.8)];
                 arraycopy(line, 0, a, 0, line.length);
                 this.line = a;
             }
             line[n] = c;
         }        
 
 
         
         
         public void close() throws IOException {
             input.close();
         }
         
 //        
 //        /**
 //         * Returns the current position in the current line, not taking
 //         * generated characters into account.
 //         */
 //        public int getLineNumber() {
 //            return lineNum;
 //        }
 //        
 //        /**
 //         * Returns the current position in the current line, not taking
 //         * generated characters into account.
 //         */
 //        public int getLinePosition() {
 //            return linePos - lineAddedChars;
 //        }
 
 
         
         
         
 
         private boolean isOpChar(char c) {
             return Arrays.binarySearch(OP_CHARS, c) >= 0;
         }
         
         
         
         
         
         
         public static void main(String[] args) throws IOException {
             Reader r = new InputStreamReader(ClassLoader.getSystemResourceAsStream("vitry/music/Time.vitry"));
             IndentationReader ir = new IndentationReader(r);
             BufferedReader br = new BufferedReader(ir);
             
             int c;
             while ( (c = ir.read()) >= 0) {
 //                System.out.write(c);
             }
             System.out.flush();
             
 //            String s;
 //            while ( (s = br.readLine()) != null) {
 //                System.out.println(s);
 //            }
 
 
         }
         
 
     }
