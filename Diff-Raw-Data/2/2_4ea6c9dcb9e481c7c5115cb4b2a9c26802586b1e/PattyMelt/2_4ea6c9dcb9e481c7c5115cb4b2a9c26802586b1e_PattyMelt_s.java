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
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Arrays;
 import java.util.LinkedList;
 import javax.swing.*;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.TableColumn;
 
 /**
  * A simple command line driver for DCPU-16.
  *
  * @author mcculley
  */
 public class PattyMelt {
 
     // FIXE: Add support for undoing/going back in time to debug.
     private final DCPU16 cpu = new DCPU16Emulator();
     private volatile boolean running;
 
     /**
      * Load a binary file into memory.
      *
      * @param memory a Memory to read the file into
      * @param file a File to read from
      * @throws IOException
      */
     private static void loadBinary(Memory memory, File file) throws IOException {
         InputStream inputStream = new FileInputStream(file);
         try {
             DCPU16Utilities.load(inputStream, memory, 0);
         } finally {
             inputStream.close();
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
     private static void loadHex(Memory memory, BufferedReader reader) throws IOException {
         int i = 0;
         while (true) {
             String line = reader.readLine();
             if (line == null) {
                 return;
             }
 
             short value = Short.parseShort(line, 16);
             memory.put(i++, value);
         }
     }
 
     private static boolean isBinary(File file) throws IOException {
         InputStream inputStream = new FileInputStream(file);
         try {
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
         } finally {
             inputStream.close();
         }
     }
 
     private void launch(final boolean start, final boolean openConsole, final boolean openStateViewer, final boolean openMemoryViewer, String filename) throws Exception {
         final Memory memory = cpu.memory();
         File file = new File(filename);
 
         // Try to guess if this is binary or not. Should add an option to be explicit.
         if (isBinary(file)) {
             loadBinary(memory, file);
         } else {
             BufferedReader reader = new BufferedReader(new FileReader(filename));
             try {
                 loadHex(memory, reader);
             } finally {
                 reader.close();
             }
         }
 
         SwingUtilities.invokeAndWait(new Runnable() {
 
             @Override
             public void run() {
                 if (openConsole) {
                     openConsole();
                 }
 
                 if (openStateViewer) {
                     openStateViewer();
                 }
 
                 if (openMemoryViewer) {
                     openMemoryViewer();
                 }
             }
         });
 
         if (start) {
             launchCPUThread();
         }
     }
 
     private void openConsole() {
         Console console = new Console();
         cpu.install(console.getScreen(), 0x8000);
         cpu.install(console.getKeyboard(), 0x9000);
         JFrame frame = new JFrame("PattyMelt");
        frame.setSize(342, 330);
         frame.getContentPane().add(console.getWidget());
         frame.setVisible(true);
     }
 
     private void openStateViewer() {
         StateViewer stateViewer = new StateViewer(cpu);
         JFrame stateFrame = new JFrame("CPU State");
         stateFrame.getContentPane().setLayout(new BorderLayout());
         stateFrame.getContentPane().add(stateViewer.getWidget(), BorderLayout.SOUTH);
 
         final JButton stepButton = new JButton("Step");
         final JButton runButton = new JButton("Run");
         final JButton stopButton = new JButton("Stop");
 
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
                     cpu.step();
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
                 launchCPUThread();
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
 
     private void launchCPUThread() {
         running = true;
         Runnable r = new Runnable() {
 
             @Override
             public void run() {
                 runCPU();
             }
         };
         Thread thread = new Thread(r, "DCPU-16");
         thread.start();
     }
 
     private void openMemoryViewer() {
         MemoryTableModel memoryTableModel = new MemoryTableModel(cpu);
         JFrame memoryFrame = new JFrame("Memory");
         JTable memoryTable = new JTable(memoryTableModel);
         Font font = new Font("Monospaced", Font.PLAIN, 18);
         memoryFrame.getContentPane().add(new JScrollPane(memoryTable));
         memoryTable.setFont(font);
         memoryTable.getTableHeader().setFont(font);
         memoryFrame.pack();
         memoryFrame.setLocation(0, 250);
         memoryFrame.setVisible(true);
 
         memoryTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
         for (int i = 0; i < 9; i++) {
             DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
             dtcr.setHorizontalAlignment(SwingConstants.CENTER);
             TableColumn column = memoryTable.getColumnModel().getColumn(i);
             column.setCellRenderer(dtcr);
         }
     }
 
     private void runCPU() {
         try {
             while (running) {
                 cpu.step();
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
         LinkedList<String> argList = new LinkedList<String>(Arrays.asList(args));
         String filename = null;
         boolean openConsole = true;
         boolean openStateViewer = true;
         boolean openMemoryViewer = true;
         boolean start = false;
         while (!argList.isEmpty()) {
             String arg = argList.removeFirst();
             if (arg.equals("-console")) {
                 openConsole = true;
             } else if (arg.equals("-noconsole")) {
                 openConsole = false;
             } else if (arg.equals("-stateviewer")) {
                 openStateViewer = true;
             } else if (arg.equals("-nostateviewer")) {
                 openStateViewer = false;
             } else if (arg.equals("-memoryviewer")) {
                 openMemoryViewer = true;
             } else if (arg.equals("-nomemoryviewer")) {
                 openMemoryViewer = false;
             } else if (arg.equals("-start")) {
                 start = true;
             } else {
                 filename = arg;
             }
         }
 
         if (filename == null) {
             System.err.println("usage: [-start] [-[no]console] [-[no]stateviewer] [-[no]memoryviewer] PattyMelt <hex or binary file>");
         } else {
             PattyMelt application = new PattyMelt();
             application.launch(start, openConsole, openStateViewer, openMemoryViewer, filename);
         }
     }
 }
