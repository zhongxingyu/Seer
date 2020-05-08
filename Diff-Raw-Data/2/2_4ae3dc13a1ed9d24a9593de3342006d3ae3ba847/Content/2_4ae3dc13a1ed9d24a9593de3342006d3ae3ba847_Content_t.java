 //  Copyright © 2012 bjarneh
 //
 //  This program is free software: you can redistribute it and/or modify
 //  it under the terms of the GNU General Public License as published by
 //  the Free Software Foundation, either version 3 of the License, or
 //  (at your option) any later version.
 //
 //  This program is distributed in the hope that it will be useful,
 //  but WITHOUT ANY WARRANTY; without even the implied warranty of
 //  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //  GNU General Public License for more details.
 //
 //  You should have received a copy of the GNU General Public License
 //  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
 package com.github.bjarneh.simple.mime;
 
 import java.net.URL;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.charset.Charset;
 import java.util.HashMap;
 import java.util.Scanner;
 
 import com.github.bjarneh.utilz.Handy;
 
 /**
  * Returns MIME types based on file endings.
  *
  * @author  bjarneh@ifi.uio.no
  * @version 1.0
  */
 
 public class Content{
 
     HashMap<String, String> mapping;
     String defaultCharset = "utf-8";
 
     public Content() throws IOException {
         mapping = new HashMap<String, String>();
         defaultCharset = Charset.defaultCharset().displayName();
         load();
     }
 
     private void load() throws IOException {
 
         String path = Handy.fromSlash("resources/txt/mime.types");
 
         URL url = getClass().getClassLoader().getResource(path);
         Scanner scanner = new Scanner(url.openStream());
         String line;
         String[] tokens;
 
         while( scanner.hasNextLine() ){
             line = scanner.nextLine();
             line = line.replaceFirst("#.*", "").trim();
             tokens = line.split("\\s+");
             if( tokens.length > 1 ){
                 if( tokens[0].startsWith("text/") ){
                    tokens[0] = tokens[0] +";charset=" + defaultCharset;
                 }
                 for(int i = 1; i < tokens.length; i++){
                     mapping.put(tokens[i], tokens[0]);
                 }
             }
         }
 
         scanner.close();
     }
 
     /**
      * Find mime type based on file ending.
      *
      * @param  path the path to a file
      * @return a fitting mimetype for path name
      */
     public String type(String path){
 
         String mimetype = null;
         String suf = Handy.suffix(path);
 
         if( suf != null ){ mimetype = mapping.get(suf); }
 
         return (mimetype != null)? mimetype : "application/octet-stream";
     }
 
     /**
      * Find mime type based on file ending.
      *
      * @param  file the File object used
      * @return a fitting mimetype for file (based on file ending)
      */
     public String type(File file){
         return type(file.getName());
     }
 
 }
