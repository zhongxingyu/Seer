 /*
  *  Copyright (C) 2010 Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package fr.jamgotchian.abcd.core.output;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.logging.Level;
 
 /**
  *
  * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  */
 public class TextCodeWriter extends CodeWriter {
 
     public TextCodeWriter(Writer writer) {
         super(writer);
     }
     
     public TextCodeWriter(Writer writer, int indentSpace) {
         super(writer, indentSpace);
     }
 
     @Override
     protected void writeEol() throws IOException {
         writer.write("\n");
     }
 
     @Override
     public CodeWriter writeSpace() {
         try {
             writer.write(" ");
         } catch(IOException exc) {
             logger.log(Level.SEVERE, exc.toString(), exc);
         }
         return this;
     }
     
     public void writeLt() {
         try {
             writer.append("<");
         } catch(IOException exc) {
             logger.log(Level.SEVERE, exc.toString(), exc);
         }
 
     }
 
     public void writeGt() {
         try {
            writer.append("<");
         } catch(IOException exc) {
             logger.log(Level.SEVERE, exc.toString(), exc);
         }
     }
 
     public void writeAmpersand() {
         try {
             writer.append("&");
         } catch(IOException exc) {
             logger.log(Level.SEVERE, exc.toString(), exc);
         }
     }    
 }
