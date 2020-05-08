 package com.vhly.epubmaker;
 
 import java.io.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Created by IntelliJ IDEA.
  * User: vhly[FR]
  * Date: 13-12-3
  * Email: vhly@163.com
  */
 public class TxtSplit {
     public static void main(String[] args) {
         int argc = args.length;
         if (argc != 2) {
             System.out.println("Usage: TxtSplit <txt> <target folder>");
         } else {
             File file = new File(args[0]);
             if (file.exists() && file.canRead()) {
                 File targetDir = new File(args[1]);
                 boolean bok = true;
                 if (!targetDir.exists()) {
                     bok = targetDir.mkdirs();
                 }
                 if (bok) {
                     String name = file.getName();
                     int index = name.lastIndexOf('.');
                     if (index != -1) {
                         name = name.substring(0, index);
                     }
                     targetDir = new File(targetDir, name);
                     if (!targetDir.exists()) {
                         bok = targetDir.mkdirs();
                     }
                     if (bok) {
                         StringBuilder sb = new StringBuilder();
 
                         FileReader fr = null;
                         BufferedReader br = null;
                         FileOutputStream fout = null;
                         OutputStreamWriter ow = null;
                         PrintWriter pw = null;
                         File outFile = null;
 
                         String line = null;
 
                         try {
                             fr = new FileReader(file);
                             br = new BufferedReader(fr);
                             Pattern pattern = Pattern.compile("^第.*章\\s+.*\\S");
                             Matcher matcher = pattern.matcher("abc");
                             int titleCount = 0;
                             StringBuilder ssb = new StringBuilder();
 
                             while (true) {
                                 line = br.readLine();
                                 if (line == null) {
                                     if (sb.length() > 0) {
                                         if (pw != null && ow != null && fout != null) {
                                             pw.print(sb.toString());
                                             sb.setLength(0);
                                             printFoot(pw);
                                            pw.close();
                                            ow.close();
                                            fout.close();
                                         }
                                     }
                                     break;
                                 }
                                 line = line.replaceAll(">", "&gt;");
                                 line = line.replaceAll("<", "&lt;");
                                 line = line.replaceAll("&", "&amp;");
                                 line = line.trim();
                                 if (line.length() > 0) {
                                     matcher.reset(line);
                                     if (matcher.find()) {
                                         if (pw != null) {
                                             if (sb.length() > 0) {
                                                 pw.print(sb.toString());
                                                 sb.setLength(0);
                                                 printFoot(pw);
                                             }
                                             pw.close();
                                         }
                                         if (ow != null) {
                                             try {
                                                 ow.close();
                                             } catch (IOException e) {
                                                 e.printStackTrace();
                                             }
                                         }
                                         if (fout != null) {
                                             try {
                                                 fout.close();
                                             } catch (IOException e) {
                                                 e.printStackTrace();
                                             }
                                         }
 
                                         titleCount++;
                                         int start = matcher.start();
                                         int end = matcher.end();
                                         String title = line.substring(start, end);
                                         String s = toLongString(titleCount);
                                         String entryName = s + ".xhtml";
                                         outFile = new File(targetDir, entryName);
 
                                         ssb.append("<chapter>\n");
                                         ssb.append("<title>").append(title).append("</title>\n")
                                                 .append("<ename>").append(entryName).append("</ename>\n")
                                                 .append("<path>").append(outFile.getAbsolutePath()).append("</path>\n")
                                                 .append("</chapter>\n");
                                         System.out.println(ssb.toString());
                                         ssb.setLength(0);
 
                                         fout = new FileOutputStream(outFile);
                                         ow = new OutputStreamWriter(fout, "UTF-8");
                                         pw = new PrintWriter(ow);
                                         printHead(pw, title);
 
                                     } else {
                                         sb.append("\n<p>").append(line).append("</p>");
                                     }
                                 }
                             }
                         } catch (IOException e) {
                             e.printStackTrace();
                         } finally {
                             if (br != null) {
                                 try {
                                     br.close();
                                 } catch (IOException e) {
                                     e.printStackTrace();
                                 }
                             }
                             if (fr != null) {
                                 try {
                                     fr.close();
                                 } catch (IOException e) {
                                     e.printStackTrace();
                                 }
                             }
                         }
                     }
                 }
             }
         }
     }
 
     private static void printFoot(PrintWriter pw) {
         if (pw != null) {
             pw.print("</body></html>");
         }
     }
 
     private static void printHead(PrintWriter pw, String title) {
         if (pw != null) {
             StringBuilder sb = new StringBuilder();
             sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
             sb.append("<!DOCTYPE html\n" +
                     "        PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n" +
                     "        \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
                     "<html xmlns=\"http://www.w3.org/1999/xhtml\">");
             sb.append("<head>");
             if (title != null) {
                 sb.append("<title>").append(title).append("</title>");
             }
             sb.append("<meta name=\"author\" content=\"ePubMaker(vhly)\"/>");
             sb.append("</head>");
             sb.append("<body>\n");
             sb.append("<h2>").append(title).append("</h2>");
             pw.print(sb.toString());
             sb = null;
         }
     }
 
     private static String toLongString(int index) {
         StringBuilder sb = new StringBuilder();
         String si = Integer.toString(index);
         int len = si.length();
         if (len < 5) {
             int cc = 5 - len;
             for (int i = 0; i < cc; i++) {
                 sb.append('0');
             }
             sb.append(si);
         } else {
             sb.append(si);
         }
         String ret = sb.toString();
         sb = null;
         return ret;
     }
 }
