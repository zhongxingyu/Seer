 package  iensen.IO;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.math.BigInteger;
 import java.util.InputMismatchException;
 
 public class InputReader {
 
     private InputStream stream;
     int curCharIndex = 0;
     int charsInBuf = 0;
      byte buf[] = new byte[16*1024];
 
     public InputReader(InputStream stream) {
         this.stream = stream;
     }
 
 
     public int readChar() {
 
         if (curCharIndex >= charsInBuf) {
             curCharIndex = 0;
             try {
                 charsInBuf = stream.read(buf);
             } catch (IOException e) {
                 throw new InputMismatchException();
             }
             if (charsInBuf <= 0)
                 return -1;
         }
         return buf[curCharIndex++];
     }
 
 
     public String nextLine() {
         StringBuilder buf = new StringBuilder();
         int c = readChar();
         while (c != '\n' && c != -1) {
             if (c != '\r')
                 buf.appendCodePoint(c);
                 c = readChar();
         }
         return buf.toString();
     }
 
     public int nextInt() {
 
         int c;
         do {
             c = readChar();
         }while(isWhitespace(c));
 
         int sign = 1;
         if (c == '-') {
             c = readChar();
             sign = -1;
 
         }
         int res = 0;
         do {
             res *= 10;
             res += c - '0';
             c = readChar();
         } while (!isWhitespace(c) && c!=-1);
         return sign * res;
     }
 
     public long nextLong() {
 
         int c;
         do {
             c = readChar();
         }while(isWhitespace(c));
 
         int sign = 1;
         if (c == '-') {
             c = readChar();
             sign = -1;
 
         }
         long res = 0;
         do {
             res *= 10;
             res += c - '0';
             c = readChar();
         } while (!isWhitespace(c) && c!=-1);
         return sign * res;
     }
 
 
 
 
     public String nextToken() {
         int c;
         do {
             c = readChar();
         }while(isWhitespace(c));
 
         StringBuilder res = new StringBuilder();
         do {
             res.appendCodePoint(c);
             c = readChar();
         } while (!isWhitespace(c) && c!=-1);
         return res.toString();
     }
 
     /*
     * For the purpose of compatibilty with Chelper Chrome extension
      */
 
     public String next() {
         return nextToken();
     }
 
 
     public double nextDouble() {
         return Double.parseDouble(nextToken());
     }
 
 
 
     private boolean isWhitespace(int c) {
         return  c==' ' || c=='\n'|| c=='\r' || c=='\t';
     }
 }
 
 
 
