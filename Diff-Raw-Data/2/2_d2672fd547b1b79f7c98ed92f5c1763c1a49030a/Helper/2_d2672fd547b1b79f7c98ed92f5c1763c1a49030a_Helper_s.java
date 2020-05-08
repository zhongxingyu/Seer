 /*
  * COPYRIGHT INFORMATION
  * 
  * Developed by ALTernative + F4ntastic FOUR
  * 
  *  This program is free software; you can redistribute it and/or modify
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
  */
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package models;
 
 /**
  *
  * @author Benedikt
  */
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.font.TextAttribute;
 import java.awt.image.BufferedImage;
 import java.io.*;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.charset.CharacterCodingException;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetDecoder;
 import java.text.AttributedCharacterIterator.Attribute;
 import java.text.AttributedString;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 import play.Play;
 
 public class Helper
 {
 
     public static void copy(File source, File dest) throws Exception
     {
         RandomAccessFile datei = new RandomAccessFile(source.getAbsolutePath(), "r");
         RandomAccessFile neudatei = new RandomAccessFile(dest.getAbsolutePath(), "rw");
 
         while (neudatei.length() < datei.length())
         {
             neudatei.write(datei.read());
         }
 
         datei.close();
         neudatei.close();
     }
 
     public static boolean isUtf8(String v)
     {
         byte bytearray[] = v.getBytes();
         CharsetDecoder d = Charset.forName("UTF-8").newDecoder();
         try
         {
             CharBuffer r = d.decode(ByteBuffer.wrap(bytearray));
             r.toString();
         } catch (CharacterCodingException e)
         {
             return false;
         }
         return true;
     }
 
     public static Boolean texToPdf(File tex, File dest)
     {
         System.out.println(dest.getAbsolutePath());
         System.out.println(tex.getAbsolutePath());
        ProcessBuilder texBuilder = new ProcessBuilder("pdflatex", tex.getAbsolutePath(), "-output-directory=" + dest.getAbsolutePath());
         texBuilder.redirectErrorStream(true);
         Process p;
         try
         {
             p = texBuilder.start();
         } catch (IOException ex)
         {
             System.out.println(ex.toString());
             return false;
         }
         String tmp = null;
         String error = null;
         BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
         System.out.println("latex wird kompiliert");
 
         try
         {
             while ((tmp = br.readLine()) != null)
             {
                 error = error + tmp + "\n";
             }
 
         } catch (IOException ioe)
         {
             System.out.println(ioe.toString());
         }
         System.out.println(error);
 
         try
         {
             p.waitFor();
         } catch (InterruptedException ex)
         {
             System.out.println(ex.toString());
         }
         if (p.exitValue() == 0)
         {
             System.out.println("Latex compilition successfull.");
             return true;
         } else
         {
             return false;
         }
     }
 
     public static void pdfToImage(File source, File destination)
     {
         ProcessBuilder imageBuilder = new ProcessBuilder("java -jar pdfbox-app-x.y.z.jar PDFToImage -endPage 1 ", source.getAbsolutePath());
         File directory = new File(Play.applicationPath.getAbsolutePath() + "/lib/");
         imageBuilder.directory(directory);
 
         Process p;
         try
         {
             p = imageBuilder.start();
         } catch (IOException ex)
         {
             System.out.println(ex.toString());
             return;
         }
 
         String tmp = null;
         String error = null;
         BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
         System.out.println("latex wird kompiliert");
 
         try
         {
             while ((tmp = br.readLine()) != null)
             {
                 System.out.println("In while");
                 error = error + tmp + "\n";
             }
 
         } catch (IOException ioe)
         {
             System.out.println(ioe.toString());
         }
         System.out.println(error);
 
         try
         {
             p.waitFor();
         } catch (InterruptedException ex)
         {
             System.out.println(ex.toString());
         }
         if (p.exitValue() == 0)
         {
             System.out.println("Latex compilition successfull.");
             return;
         } else
         {
             return;
         }
 
     }
 
     public static void textToImage(Template template) throws FileNotFoundException
     {
         BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB);
         System.out.println("DOCUMENTPATH: " + Play.applicationPath.getAbsolutePath() + "/public/templates/" + template.filename_ + ".jpg");
 
         File image_file = new File(Play.applicationPath.getAbsolutePath() + "/public/templates/" + template.filename_ + ".jpg");
 
         Image image2 = Toolkit.getDefaultToolkit().createImage(Play.applicationPath.getAbsolutePath() + "/public/templates/" + template.filename_ + ".jpg");
         System.out.println("image2" + image2);
 
 
 
 
         image.createGraphics().drawImage(image2, 0, 0, null);
         image.getGraphics().setColor(Color.WHITE);
         image.getGraphics().fillRect(0, 0, 400, 300);        
         image.getGraphics().setColor(Color.BLACK);
         image.getGraphics().setFont(new Font("Serif", Font.PLAIN, 12));
 
         String[] output = template.textFile.split("\n");
 
         for (int i = 0; i < output.length; i++)
         {
             if(output[i].length() == 0)
                 continue;
             AttributedString as = new AttributedString(output[i]);
 
             
             as.addAttribute(TextAttribute.FOREGROUND, Color.BLACK);
 
             image.getGraphics().drawString(as.getIterator(), 10, 20+(20*i));
         }
 
         try
         {
             ImageIO.write(image, "jpg", image_file);
         } catch (Exception e)
         {
             System.out.println(e);
         }
 
 
 
     }
 }
