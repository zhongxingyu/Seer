 /*
  * Copyright 2012, Gene McCulley
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *   Redistributions of source code must retain the above copyright notice, 
  *   this list of conditions and the following disclaimer.
  *
  *   Redistributions in binary form must reproduce the above copyright 
  *   notice, this list of conditions and the following disclaimer in the 
  *   documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
  * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  */
 package com.stackframe.pattymelt;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import javax.swing.*;
 
 /**
  * A simple command line driver for DCPU-16.
  *
  * @author mcculley
  */
 public class PattyMelt {
 
     /**
      * Load a binary file into memory.
      *
      * @param memory a ShortBuffer to read the file into
      * @param file a File to read from
      * @throws IOException
      */
     private static void loadBinary(short[] memory, File file) throws IOException {
         // FIXME: Close streams correctly.
         int i = 0;
         InputStream inputStream = new FileInputStream(file);
         while (true) {
             int v1 = inputStream.read();
             if (v1 == -1) {
                 return;
             }
             
             int v2 = inputStream.read();
             if (v2 == -1) {
                 return;
             }
             
             short value = (short) ((v2 << 8) | v1);
             memory[i++] = value;
         }
     }
 
     /**
      * Load a file into memory. The file is assumed to be lines of hexadecimal
      * 16-bit words.
      *
      * @param memory a ShortBuffer to read the file into
      * @param reader a BufferedReader to read from
      * @throws IOException
      */
     private static void loadHex(short[] memory, BufferedReader reader) throws IOException {
         // FIXME: Close streams correctly.
         int i = 0;
         while (true) {
             String line = reader.readLine();
             if (line == null) {
                 return;
             }
             
             short value = (short) Integer.parseInt(line, 16);
             memory[i++] = value;
         }
     }
     
     private static boolean isBinary(File file) throws IOException {
         // FIXME: Close streams correctly.
         InputStream inputStream = new FileInputStream(file);
         while (true) {
             int value = inputStream.read();
             if (value == -1) {
                 return false;
             }
             
             char c = (char) value;
             boolean isLetterOrDigit = Character.isLetterOrDigit(c);
             boolean isWhitespace = Character.isWhitespace(c);
             if (!(isLetterOrDigit || isWhitespace)) {
                 return true;
             }
         }
     }
     
     private static String dumpMemory(int address, int numWords, short[] memory) {
         // FIXME: Make this better. It is a crude bit of debugging right now.
         StringBuilder buf = new StringBuilder();
         for (int i = 0; i < numWords; i++) {
             short value = memory[address + i];
             char c = (char) value;
             if (!(Character.isLetterOrDigit(c) || Character.isWhitespace(c))) {
                 c = '.';
             }
             
             buf.append(c);
         }
         
         return buf.toString();
     }
     private Console console;
     private StateViewer stateViewer;
     private final DCPU16 cpu = new DCPU16Emulator();
     private final JButton stepButton = new JButton("Step");
     private final JButton runButton = new JButton("Run");
     private final JButton stopButton = new JButton("Stop");
     private volatile boolean running;
     
     private void launch(String filename) throws Exception {
         final short[] memory = cpu.memory();
         System.err.println("Loading " + filename);
         File file = new File(filename);
 
         // Try to guess if this is binary or not. Should add an option to be explicit.
         if (isBinary(file)) {
             loadBinary(memory, file);
         } else {
             // FIXME: Close streams correctly.
             BufferedReader reader = new BufferedReader(new FileReader(filename));
             loadHex(memory, reader);
         }
         
         SwingUtilities.invokeAndWait(new Runnable() {
             
             @Override
             public void run() {
                 console = new Console(0x8000, memory);
                 JFrame frame = new JFrame("PattyMelt");
                frame.setSize(240, 240);
                 frame.getContentPane().add(console.getWidget());
                 frame.setVisible(true);
                 
                 stateViewer = new StateViewer(cpu);
                 JFrame stateFrame = new JFrame("CPU State");
                 stateFrame.getContentPane().setLayout(new BorderLayout());
                 stateFrame.getContentPane().add(stateViewer.getWidget(), BorderLayout.SOUTH);
                 
                 JComponent controlBox = new JPanel();
                 controlBox.add(stepButton);
                 controlBox.add(runButton);
                 stopButton.setEnabled(false);
                 controlBox.add(stopButton);
                 stateFrame.getContentPane().add(controlBox, BorderLayout.NORTH);
                 
                 stepButton.addActionListener(new ActionListener() {
                     
                     @Override
                     public void actionPerformed(ActionEvent ae) {
                         try {
                             stepCPU();
                         } catch (IllegalOpcodeException ioe) {
                             // FIXME: reflect in GUI
                             System.err.printf("Illegal opcode 0x%04x encountered.\n", ioe.opcode);
                         }
                     }
                 });
                 
                 runButton.addActionListener(new ActionListener() {
                     
                     @Override
                     public void actionPerformed(ActionEvent ae) {
                         runButton.setEnabled(false);
                         stopButton.setEnabled(true);
                         stepButton.setEnabled(false);
                         running = true;
                         Thread thread = new Thread(
                                 new Runnable() {
                                     
                                     @Override
                                     public void run() {
                                         runCPU();
                                     }
                                 }, "DCPU-16");
                         thread.start();
                     }
                 });
                 
                 stopButton.addActionListener(new ActionListener() {
                     
                     @Override
                     public void actionPerformed(ActionEvent ae) {
                         running = false;
                         runButton.setEnabled(true);
                         stopButton.setEnabled(false);
                         stepButton.setEnabled(true);
                     }
                 });
                 
                 stateFrame.pack();
                 stateFrame.setLocation(0, 100);
                 stateFrame.setVisible(true);                
             }
         });
         
         updatePeripherals();
     }
     
     private void updatePeripheralsInSwingThread() {
         assert SwingUtilities.isEventDispatchThread();
         console.update();
         stateViewer.update();
     }
     
     private void updatePeripherals() {
         if (SwingUtilities.isEventDispatchThread()) {
             updatePeripheralsInSwingThread();
         } else {
             try {
                 SwingUtilities.invokeAndWait(new Runnable() {
                     
                     @Override
                     public void run() {
                         updatePeripheralsInSwingThread();
                     }
                 });
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
         }
     }
     
     private void stepCPU() throws IllegalOpcodeException {
         cpu.step();
         updatePeripherals();
     }
     
     private void runCPU() {
         try {
             while (running) {
                 stepCPU();
             }
         } catch (IllegalOpcodeException ioe) {
             // FIXME: reflect in GUI
             System.err.printf("Illegal opcode 0x%04x encountered.\n", ioe.opcode);
         }
     }
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) throws Exception {
         PattyMelt application = new PattyMelt();
         String filename = args[0];
         application.launch(filename);
     }
     // FIXE: Add support for undoing/going back in time to debug.
 }
