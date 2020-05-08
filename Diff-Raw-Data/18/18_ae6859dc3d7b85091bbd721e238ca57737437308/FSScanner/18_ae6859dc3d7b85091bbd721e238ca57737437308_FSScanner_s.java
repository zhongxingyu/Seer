 /*
  * Copyright (C) 2009 eXo Platform SAS.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.exoplatform.services.web.css;
 
 import org.exoplatform.services.web.css.model.StylesheetObject;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.StringWriter;
 
 /**
  * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
  * @version $Revision$
  */
 public class FSScanner
 {
 
    public static void main(String[] args)
    {
       File root = new File("/Users/julien/java/exo/eXoProjects");
       new FSScanner(root).doScan();
    }
 
    private File root;
 
    private int size1;
 
    private int size2;
 
    public FSScanner(File root)
    {
       this.root = root;
    }
 
    public void doScan()
    {
       doScan(root);
       int ratio = (size2 * 100) / size1;
      System.out.println("ratio = " + ratio);
    }
 
    private void doScan(File f)
    {
       if (f.isDirectory())
       {
          for (File child : f.listFiles())
          {
             doScan(child);
          }
       }
       else if (f.isFile() && f.getName().endsWith(".css"))
       {
         System.out.println("About to process " + f.getAbsolutePath());
          try
          {
             FileReader reader = new FileReader(f);
             StringWriter writer1 = new StringWriter();
             char[] buf = new char[256];
             for (int i = reader.read(buf); i != -1; i = reader.read(buf))
             {
                writer1.write(buf, 0, i);
             }
             String css1 = writer1.toString();
 
             //
             if (css1.length() > 0)
             {
                StylesheetObject stylesheet = StylesheetObject.createStylesheet(css1);
                StringWriter writer2 = new StringWriter();
                stylesheet.writeTo(writer2);
                writer2.close();
                String css2 = writer2.toString();
 
                //
                size1 += css1.length();
                size2 += css2.length();
             }
          }
          catch (IOException e)
          {
            e.printStackTrace(System.err);
          }
          catch (UnsupportedOperationException e)
          {
            e.printStackTrace(System.err);
          }
       }
    }
 }
