 /*
  * The MIT License
  *
  * Copyright 2013 Bernhard Sirlinger.
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
 package mss;
 
import mss.integratoren.Rechenmodul;

 /**
  *
  * @author Bernhard Sirlinger
  */
 public class Controller implements Runnable {
     private final View frame;
     private final Thread thread;
     
     public Controller() {
         this.frame = new View("MSS");
         
         Thread frameThread = new Thread(this.frame);
         frameThread.setName("MainFrame");
         this.thread = frameThread;
     }
     
     public void start() {
         this.thread.start();
     }
     
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         Thread main = new Thread(new Controller(), "controller");
         main.start();
     }
 
     @Override
     public void run() {
         this.start();
     }
 }
