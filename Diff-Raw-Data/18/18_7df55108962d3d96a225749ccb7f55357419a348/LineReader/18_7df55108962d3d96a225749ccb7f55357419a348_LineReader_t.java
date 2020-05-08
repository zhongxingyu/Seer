 package ch20.ex04;
 
 import java.io.FilterReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringReader;
 
 public class LineReader extends FilterReader  {
 	 protected LineReader(Reader in) {
 	        super(in);
 	 }
 
     public String readLine() throws IOException {
         String line = System.getProperty("line.separator");
         StringBuffer sb = new StringBuffer();
 
         while (!sb.toString().endsWith(line)) {
        	if (!ready()) 
        		break;
             System.out.println("Reading...");
             sb.append((char)read());
         }
         return sb.toString();
     }
     
     public static void main(String[] args) throws IOException {
         String line = System.getProperty("line.separator");
         StringBuffer sb = new StringBuffer();
 
        sb.append("test1");
         sb.append(line);
        sb.append("test2");
         sb.append(line);
        sb.append("test3");
         sb.append(line);
 
         StringReader sr = new StringReader(sb.toString());
         LineReader lr = new LineReader(sr);
 
         for (int i = 0; i < 3; i++) {
         	System.out.println(lr.readLine());
         }
     }
 }
