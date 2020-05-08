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
 
 import com.stackframe.pattymelt.DCPU16.CPUEvent;
 import java.awt.Font;
 import javax.swing.*;
 
 /**
  * A GUI to peek into the state of the virtual machine.
  *
  * @author mcculley
  */
 public class StateViewer {
 
     private static final Font MONO = new Font("Monospaced", Font.PLAIN, 12);
     // FIXME: Add a field to show the decoded instruction
     // FIXME: Add ability to adjust registers
     private final DCPU16 cpu;
     private final Box box;
     private final JTextField pcField = makeRegisterField();
     private final JTextField spField = makeRegisterField();
     private final JTextField oField = makeRegisterField();
     private final JTextField instrField = makeRegisterField();
     private final JTextField disField = makeField(17);
     private final JTextField[] registerFields = new JTextField[DCPU16.Register.values().length];
 
     private static JTextField makeField(int numColumns) {
         JTextField field = new JTextField(numColumns);
         field.setEditable(false);
         field.setFont(MONO);
         return field;
     }
 
     private static JTextField makeRegisterField() {
         return makeField(4);
     }
 
     public StateViewer(DCPU16 cpu) {
         this.cpu = cpu;
         box = Box.createVerticalBox();
         JComponent pcBox = new JPanel();
         box.add(pcBox);
         JLabel pcLabel = new JLabel("PC:");
         pcBox.add(pcLabel);
         pcBox.add(pcField);
 
         pcBox.add(new JSeparator());
         pcBox.add(new JLabel("SP:"));
         pcBox.add(spField);
 
         pcBox.add(new JSeparator());
         pcBox.add(new JLabel("O:"));
         pcBox.add(oField);
 
         pcBox.add(new JSeparator());
         pcBox.add(new JLabel("INST:"));
         pcBox.add(instrField);
 
         pcBox.add(new JSeparator());
         pcBox.add(new JLabel("DIS:"));
         pcBox.add(disField);
 
         JComponent registerBox = new JPanel();
         box.add(registerBox);
 
         for (DCPU16.Register r : DCPU16.Register.values()) {
             registerBox.add(new JLabel(r.name() + ":"));
             JTextField registerField = makeRegisterField();
             registerBox.add(registerField);
             registerFields[r.ordinal()] = registerField;
         }
 
         cpu.addListener(new DCPU16.CPUEventListener() {
 
             @Override
             public void instructionExecuted(CPUEvent event) {
                 if (SwingUtilities.isEventDispatchThread()) {
                     update();
                 } else {
                     try {
                         SwingUtilities.invokeAndWait(new Runnable() {
 
                             @Override
                             public void run() {
                                 update();
                             }
                         });
                     } catch (Exception e) {
                         throw new RuntimeException(e);
                     }
                 }
             }
 
             @Override
             public void memoryModified(CPUEvent event) {
             }
            
         });
     }
 
     public JComponent getWidget() {
         return box;
     }
 
     private void update() {
        pcField.setText(String.format("%04X", cpu.PC()));
        spField.setText(String.format("%04X", cpu.SP()));
        oField.setText(String.format("%04X", cpu.O()));
        instrField.setText(String.format("%04X", cpu.memory().get(cpu.PC())));
        disField.setText(DCPU16Utilities.disassemble(cpu.memory(), cpu.PC()));
         for (DCPU16.Register r : DCPU16.Register.values()) {
             JTextField registerField = registerFields[r.ordinal()];
            registerField.setText(String.format("%04X", cpu.register(r)));
         }
     }
 }
