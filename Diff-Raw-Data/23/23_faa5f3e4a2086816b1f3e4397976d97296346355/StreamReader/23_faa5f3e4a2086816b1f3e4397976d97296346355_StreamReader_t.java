 /*
  * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.dcc.kde;
 
 import com.dmdirc.logger.ErrorLevel;
 import com.dmdirc.logger.Logger;
 import com.dmdirc.util.StreamUtil;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.List;
 
 public class StreamReader extends Thread {
 
     /** This is the Input Stream we are reading */
     private InputStream stream;
 
     /** List to store output in */
     private List<String> list = null;
 
     /**
      * Create a new Stream Reader
      *
      * @param stream The stream to read
      * @param list The list to store the output from the stream in (null for no saving)
      */
     public StreamReader(final InputStream stream, final List<String> list) {
        super("DCC-StreamReader");

         this.stream = stream;
         this.list = list;
     }
 
     /**
      * Get the list that the output is being stored in.
      *
      * @return The output list
      */
     public List<String> getList() {
         return list;
     }
 
     /**
      * Wait for input on stream, and output/throw away/save to list
      */
     @Override
     public void run() {
         BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
         try {
             String line;
             while ((line = reader.readLine()) != null) {
                 if (list != null) {
                     list.add(line);
                 }
             }
         } catch (IOException ex) {
             Logger.userError(ErrorLevel.MEDIUM, "Error reading stream", ex);
         } finally {
             StreamUtil.close(stream);
         }
     }
 
 }
