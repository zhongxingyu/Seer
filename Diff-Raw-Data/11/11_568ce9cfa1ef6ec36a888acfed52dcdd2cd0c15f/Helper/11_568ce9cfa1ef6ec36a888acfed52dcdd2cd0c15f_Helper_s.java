 /*
  *
  * Copyright (C) 2011 SW 11 Inc.
  * 
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  * 
  * 
  * 
  * 
  */
 
 /*
  */
 package models;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.font.TextAttribute;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.image.BufferedImage;
 import java.io.*;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.charset.CharacterCodingException;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetDecoder;
 import java.text.AttributedCharacterIterator.Attribute;
 import java.text.AttributedString;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 import play.Play;
 import utils.Substitution;
 
 public class Helper {
 
     public static void copy(File source, File dest) throws Exception {
         RandomAccessFile datei = new RandomAccessFile(source.getAbsolutePath(), "r");
         RandomAccessFile neudatei = new RandomAccessFile(dest.getAbsolutePath(), "rw");
 
         while (neudatei.length() < datei.length()) {
             neudatei.write(datei.read());
         }
 
         datei.close();
         neudatei.close();
     }
 
 
     public static boolean isUtf8(String v) {
         byte bytearray[] = v.getBytes();
         CharsetDecoder d = Charset.forName("UTF-8").newDecoder();
         try {
             CharBuffer r = d.decode(ByteBuffer.wrap(bytearray));
             r.toString();
         } catch (CharacterCodingException e) {
             return false;
         }
         return true;
     }
 
 
     public static void pdfToImage(File source, File destination) throws Exception {
 
         ProcessBuilder imageBuilder = new ProcessBuilder("java", "-jar", "pdfbox.jar", "PDFToImage", "-endPage", "1", source.getAbsolutePath());
         File directory = new File(Play.applicationPath.getAbsolutePath() + "/lib/");
         imageBuilder.directory(directory);
 
         Process p;
         try {
             p = imageBuilder.start();
         } catch (IOException ex) {
             System.out.println(ex.toString());
             return;
         }
 
         String tmp = null;
         String error = null;
 
         BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
 
 
         try {
             while ((tmp = br.readLine()) != null) {
                 System.out.println("In while");
                 error = error + tmp + "\n";
             }
 
         } catch (IOException ioe) {
             System.out.println(ioe.toString());
         }
         System.out.println(error);
 
         try {
             p.waitFor();
         } catch (InterruptedException ex) {
             System.out.println(ex.toString());
         }
         if (p.exitValue() == 0) {
             System.out.println("Bildi fertig.");
             String[] new_source = source.getAbsolutePath().split(".pdf");
 
             File file = new File(new_source[0] + "1.jpg");
 
             System.out.println("Filename: " + file.getName());
 
             String[] split = file.getName().split("1.jpg");
 
 // Destination directory
             File dir = new File(Play.applicationPath.getAbsolutePath() + "/public/templates/" + split[0] + ".tex.jpg");
 
 // Move file to new directory
 
             boolean success = false;
 
             try {
                 System.out.println("Kopiere datei: " + file.getAbsolutePath());
                 System.out.println("Nach: " + dir.getAbsolutePath());
 
                 success = file.renameTo(dir);
             } catch (Exception e) {
                 System.out.println("Exception: " + e.getMessage());
             }
 
             if (!success) {
                 System.out.println("Scheiße passiert");
             }
 
 
 
             return;
         } else {
             return;
         }
 
     }
 
   public static void templateToImage(Template template) throws FileNotFoundException
     {
         BufferedImage image = new BufferedImage(315, 446, BufferedImage.TYPE_INT_RGB);
         System.out.println("DOCUMENTPATH: " + Play.applicationPath.getAbsolutePath() + "/public/templates/" + template.filename_ + ".jpg");
 
         File image_file = new File(Play.applicationPath.getAbsolutePath() + "/public/templates/" + template.filename_ + ".jpg");
 
         Image image2 = Toolkit.getDefaultToolkit().createImage(Play.applicationPath.getAbsolutePath() + "/public/templates/" + template.filename_ + ".jpg");
         System.out.println("image2" + image2);
 
 
         Substitution sub = new Substitution(template.textFile);
         
         Map map = new HashMap(template.templates_);
                 Iterator it = map.keySet().iterator();
                 
                 while(it.hasNext())
                 {
                     String key = (String) it.next();                    
                     map.put(key, key);
                 }
                 sub.replace(map);
 
 
         image.createGraphics().drawImage(image2, 0, 0, null);
         image.getGraphics().setColor(Color.WHITE);
         image.getGraphics().fillRect(0, 0, 315, 446);
         image.getGraphics().setColor(Color.BLACK);
         image.getGraphics().setFont(new Font("Serif", Font.PLAIN, 12));
 
         String[] output = sub.getText().split("\n");
 
         for (int i = 0; i < output.length; i++)
         {
             if (output[i].length() == 0)
             {
                 continue;
             }
             AttributedString as = new AttributedString(output[i]);
 
 
             as.addAttribute(TextAttribute.FOREGROUND, Color.BLACK);
 
             image.getGraphics().drawString(as.getIterator(), 10, 20 + (20 * i));
         }
 
         try
         {
             ImageIO.write(image, "jpg", image_file);
         } catch (Exception e)
         {
             System.out.println(e);
         }
         
       
 
     }
 
 
     public static void textToImage(String text,File source)
     {
         
         BufferedImage image = new BufferedImage(315, 446, BufferedImage.TYPE_INT_RGB);
 
         File image_file = new File(source.getAbsolutePath()+".jpg");
         Image image2 = Toolkit.getDefaultToolkit().createImage(source.getAbsolutePath()+ ".jpg");
         
 
 
         image.createGraphics().drawImage(image2, 0, 0, null);
         image.getGraphics().setColor(Color.WHITE);
         image.getGraphics().fillRect(0, 0, 315, 446);
         image.getGraphics().setColor(Color.BLACK);
         image.getGraphics().setFont(new Font("Serif", Font.PLAIN, 12));
 
         String[] output = text.split("\n");
 
         for (int i = 0; i < output.length; i++)
         {
             if (output[i].length() == 0)
             {
                 continue;
             }
             AttributedString as = new AttributedString(output[i]);
 
 
             as.addAttribute(TextAttribute.FOREGROUND, Color.BLACK);
 
             image.getGraphics().drawString(as.getIterator(), 10, 20 + (20 * i));
         }
 
         try
         {
             ImageIO.write(image, "jpg", image_file);
         } catch (Exception e)
         {
             System.out.println(e);
         }
         
         
     }
 
 
     public static Boolean texToPdf(File tex, File dest) {
        System.out.println(dest.getAbsolutePath());
         System.out.println(tex.getAbsolutePath());
        ProcessBuilder texBuilder = new ProcessBuilder("pdflatex", tex.getAbsolutePath(), "-output-directory=" + dest.getAbsolutePath());
         texBuilder.redirectErrorStream(true);
         Process p;
         try {
             p = texBuilder.start();
         } catch (IOException ex) {
             System.out.println(ex.toString());
             return false;
         }
         String tmp = null;
         String error = null;
         BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
         System.out.println("latex wird kompiliert");
 
         try {
             while ((tmp = br.readLine()) != null) {
                 System.out.println("In while");
                 error = error + tmp + "\n";
             }
 
         } catch (IOException ioe) {
             System.out.println(ioe.toString());
         }
         System.out.println(error);
 
         try {
             p.waitFor();
         } catch (InterruptedException ex) {
             System.out.println(ex.toString());
         }
         if (p.exitValue() == 0) {
             System.out.println("Latex compilition successfull.");
             return true;
         } else {
             return false;
         }
     }
 
 
 
     public static void textToImage(Template template) throws FileNotFoundException {
         BufferedImage image = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);
         System.out.println("DOCUMENTPATH: " + Play.applicationPath.getAbsolutePath() + "/public/templates/" + template.filename_ + ".jpg");
 
         File image_file = new File(Play.applicationPath.getAbsolutePath() + "/public/templates/" + template.filename_ + ".jpg");
 
         Image image2 = Toolkit.getDefaultToolkit().createImage(Play.applicationPath.getAbsolutePath() + "/public/templates/" + template.filename_ + ".jpg");
         System.out.println("image2" + image2);
 
         image.createGraphics().drawImage(image2, 0, 0, null);
         // image.getGraphics().setColor(Color.WHITE);
         //image.getGraphics().fillRect(0, 0, 200, 200);
         image.getGraphics().setColor(Color.RED);
         image.getGraphics().setFont(new Font("Serif", Font.PLAIN, 12));
 
         image.getGraphics().drawString(template.textFile, 10, 10);
 
         try {
             ImageIO.write(image, "jpg", image_file);
         } catch (Exception e) {
             System.out.println(e);
         }
 
 
 
     }
 }
