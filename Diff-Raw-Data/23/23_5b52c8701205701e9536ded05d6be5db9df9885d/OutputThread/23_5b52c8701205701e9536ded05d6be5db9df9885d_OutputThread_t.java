 /*
  * The MIT License
  *
  * Copyright 2013 Jason Unger <entityreborn@gmail.com>.
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
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package me.entityreborn.socbot.core;
 
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author Jason Unger <entityreborn@gmail.com>
  */
 public class OutputThread extends Thread {
 
    private final List<String> sendqueue = new ArrayList<String>();
     private final BufferedWriter out;
    private final Engine engine;
 
     public OutputThread(OutputStream o, Engine e) {
        out = new BufferedWriter(new OutputStreamWriter(o));
         engine = e;
     }
 
     public synchronized void send(String line) {
         if (line == null || line.trim().isEmpty() || 
                 line.trim().equals("\r\n")) {
             return;
         }
 
         sendqueue.add(line);
     }
 
     public synchronized void sendNow(String line) {
         if (line == null || line.isEmpty() || Thread.interrupted()) {
             return;
         }
 
         if (!line.endsWith("\r\n")) {
             line = line + "\r\n";
         }
 
         try {
             out.write(line);
             out.flush();
         } catch (IOException ex) {
             engine.handleException(ex);
         }
     }
 
     @Override
     public void run() {
        while (!Thread.interrupted() && engine.isConnected()) {
             try {
                 if (sendqueue.size() > 0) {
                     String line = sendqueue.remove(0);
 
                     sendNow(line);
                 }
 
                 Thread.sleep(250);
             } catch (InterruptedException ex) {
                 return;
             }
         }
     }
 }
