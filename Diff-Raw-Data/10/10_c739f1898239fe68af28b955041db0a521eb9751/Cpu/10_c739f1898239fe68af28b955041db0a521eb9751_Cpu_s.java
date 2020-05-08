 package org.softwarehelps.learncs.CPU;
 
 /* This file was automatically generated from a .mac file. */
 
import java.util.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
import java.applet.*;
 
 public class Cpu extends Frame
         implements ActionListener, ComponentListener, ItemListener
 {
      int pc, acc, ir;
      boolean mustStop;
      String sinstruction;        // String form of opcode
      String soperand;            // String form of operand
      int numsteps;
      int speed;          // 0=manual, 1=normal, 2=fast
 
      boolean running = false;
 
      Button runB, stepB, stopB, clearB, resetB, exampleB, advanceB;
      Button logB, asmB, helpB, opcodesB, moreB;
      Button loadB, saveB;
      Choice exampleCH;
      int buttonHeight = 25;
 
      Color buttonColor;
 
      Choice speedCH;
 
      Checkbox cpuCB, memCB;
      boolean cpuBinary, memBinary;
 
      TextField irTF, shadowirTF;
      TextField accTF, tempTF;
      TextField pcTF;
      TextField outputTF;
      TextField inputTF;
      TextField memory0, memory1, memory2, memory3, memory4, 
                memory5, memory6, memory7, memory8, memory9, 
                memory10, memory11, memory12, memory13, memory14, memory15;
      TextField[] memory;
      TextField statusTF, advanceMsgTF;
 
      Image buffer;
      Graphics gg;
      Color acolor = new Color(230,250,230);
 
      final static int X=0;
      final static int Y=20;
 
      int cputop = Y+70;
      int cpubot;
      int cpuleft = X+180;            // used to be 230
      int iotop = Y+70;
      int buttontop;
 
      int memtop = Y+30;
 
      int busTop;
      int busLeft = cpuleft;
      int busRight = busLeft + 165;
 
      int busValue;
      int busPosX;
      boolean showBusValue = false;
 
      int pcY, accY, tempY, irY;
 
      boolean returnSignaled = false;
 
      int click1;     // these are used to allow the user to step through
      int click2;     // the stages of a single instruction.
 
      LogWindow logwindow;
      Assembler asm;
 
      public Cpu() {
           setLayout(null);
 
           setTitle("Super Simple CPU");
 
           inputTF=new TextField(40);
           inputTF.addActionListener(this);
           inputTF.setBackground(Color.white);
           inputTF.setBounds(X+27,iotop+10,132,24);
           add(inputTF);
 
           outputTF=new TextField(40);
           outputTF.addActionListener(this);
           outputTF.setBackground(Color.white);
           outputTF.setBounds(X+27,iotop+60,132,24);
           add(outputTF);
 
           int separation = 27;
 
           pcY = 19;
           pcTF=new TextField(40);
           pcTF.addActionListener(this);
           pcTF.setBackground(Color.white);
           pcTF.setBounds(cpuleft+31,cputop+pcY,125,23);
           add(pcTF);
 
           accY = pcY + separation;
           accTF=new TextField(40);
           accTF.addActionListener(this);
           accTF.setBackground(Color.white);
           accTF.setBounds(cpuleft+31,cputop+accY,125,23);
           add(accTF);
 
           tempY = accY + separation;
           tempTF=new TextField(40);
           tempTF.addActionListener(this);
           tempTF.setBackground(Color.white);
           tempTF.setBounds(cpuleft+31,cputop+tempY,125,23);
           add(tempTF);
 
           irY = tempY + separation;
           irTF=new TextField(40);
           irTF.addActionListener(this);
           irTF.setBackground(Color.white);
           irTF.setBounds(cpuleft+31,cputop+irY,125,23);
           add(irTF);
 
           shadowirTF=new TextField(40);
           shadowirTF.addActionListener(this);
           shadowirTF.setBackground(acolor);
           shadowirTF.setBounds(cpuleft+31,cputop+irY+separation,125,23);
           add(shadowirTF);
 
           int tempy = cputop + irY + separation + shadowirTF.getSize().height;
 
           cpuCB=new Checkbox("binary",true);
           cpuBinary = true;
           cpuCB.addItemListener(this);
           cpuCB.setBounds(cpuleft+39,tempy,97,17);
           cpuCB.setBackground(acolor);
           add(cpuCB);
 
           cpubot = tempy + cpuCB.getSize().height + 5;
           busTop = cpubot + 10;
 
           int y = memtop+8;
 
           int memoryWidth = 120;
 
           memory0=new TextField(40);
           memory0.addActionListener(this);
           memory0.setBackground(Color.white);
           memory0.setBounds(X+460,Y+y,memoryWidth,23);
           add(memory0);
           y += 26;
 
           memory1=new TextField(40);
           memory1.addActionListener(this);
           memory1.setBackground(Color.white);
           memory1.setBounds(X+460,Y+y,memoryWidth,23);
           add(memory1);
           y += 26;
 
           memory2=new TextField(40);
           memory2.addActionListener(this);
           memory2.setBackground(Color.white);
           memory2.setBounds(X+460,Y+y,memoryWidth,23);
           add(memory2);
           y += 26;
 
           memory3=new TextField(40);
           memory3.addActionListener(this);
           memory3.setBackground(Color.white);
           memory3.setBounds(X+460,Y+y,memoryWidth,23);
           add(memory3);
           y += 26;
 
           memory4=new TextField(40);
           memory4.addActionListener(this);
           memory4.setBackground(Color.white);
           memory4.setBounds(X+460,Y+y,memoryWidth,23);
           add(memory4);
           y += 26;
 
           memory5=new TextField(40);
           memory5.addActionListener(this);
           memory5.setBackground(Color.white);
           memory5.setBounds(X+460,Y+y,memoryWidth,23);
           add(memory5);
           y += 26;
 
           memory6=new TextField(40);
           memory6.addActionListener(this);
           memory6.setBackground(Color.white);
           memory6.setBounds(X+460,Y+y,memoryWidth,23);
           add(memory6);
           y += 26;
 
           memory7=new TextField(40);
           memory7.addActionListener(this);
           memory7.setBackground(Color.white);
           memory7.setBounds(X+460,Y+y,memoryWidth,23);
           add(memory7);
           y += 26;
 
           memory8=new TextField(40);
           memory8.addActionListener(this);
           memory8.setBackground(Color.white);
           memory8.setBounds(X+460,Y+y,memoryWidth,23);
           add(memory8);
           y += 26;
 
           memory9=new TextField(40);
           memory9.addActionListener(this);
           memory9.setBackground(Color.white);
           memory9.setBounds(X+460,Y+y,memoryWidth,23);
           add(memory9);
           y += 26;
 
           memory10=new TextField(40);
           memory10.addActionListener(this);
           memory10.setBackground(Color.white);
           memory10.setBounds(X+460,Y+y,memoryWidth,23);
           add(memory10);
           y += 26;
 
           memory11=new TextField(40);
           memory11.addActionListener(this);
           memory11.setBackground(Color.white);
           memory11.setBounds(X+460,Y+y,memoryWidth,23);
           add(memory11);
           y += 26;
 
           memory12=new TextField(40);
           memory12.addActionListener(this);
           memory12.setBackground(Color.white);
           memory12.setBounds(X+460,Y+y,memoryWidth,23);
           add(memory12);
           y += 26;
 
           memory13=new TextField(40);
           memory13.addActionListener(this);
           memory13.setBackground(Color.white);
           memory13.setBounds(X+460,Y+y,memoryWidth,23);
           add(memory13);
           y += 26;
 
           memory14=new TextField(40);
           memory14.addActionListener(this);
           memory14.setBackground(Color.white);
           memory14.setBounds(X+460,Y+y,memoryWidth,23);
           add(memory14);
           y += 26;
 
           memory15=new TextField(40);
           memory15.addActionListener(this);
           memory15.setBackground(Color.white);
           memory15.setBounds(X+460,Y+y,memoryWidth,23);
           add(memory15);
           y += 26;
 
           memCB=new Checkbox("binary",true);
           memBinary = true;
           memCB.addItemListener(this);
           memCB.setBounds(X+460,y+20,95,17);
           memCB.setBackground(acolor);
           add(memCB);
 
           memory = new TextField[16];
           memory[0] = memory0;
           memory[1] = memory1;
           memory[2] = memory2;
           memory[3] = memory3;
           memory[4] = memory4;
           memory[5] = memory5;
           memory[6] = memory6;
           memory[7] = memory7;
           memory[8] = memory8;
           memory[9] = memory9;
           memory[10] = memory10;
           memory[11] = memory11;
           memory[12] = memory12;
           memory[13] = memory13;
           memory[14] = memory14;
           memory[15] = memory15;
 
           int x = X+92;
           buttontop = cpubot + 40;
 
           statusTF = new TextField(50);
           statusTF.setBounds(X+15,buttontop,350,25);
           statusTF.setBackground(Color.cyan);
           add(statusTF);
 
           buttontop += statusTF.getSize().height + 10;
 
           exampleCH = new Choice();
           exampleCH.setBackground(Color.white);
           exampleCH.addItem("  -- examples -- ");
           exampleCH.addItem("Example 1 -- sequence");
           exampleCH.addItem("Example 2 -- input and output");
           exampleCH.addItem("Example 3 -- negative numbers");
           exampleCH.addItem("Example 4 -- copy a number");
           exampleCH.addItem("Example 5 -- counting loop");
           exampleCH.addItem("Example 6 -- GCD");
           exampleCH.addItemListener(this);
           exampleCH.setBounds(X+15,buttontop,190,25);
           add(exampleCH);
 
 /*
           exampleB=new Button("Load Example");
           exampleB.addActionListener(this);
           exampleB.setBounds(X+25+exampleCH.getSize().width+5,buttontop,
                              100,buttonHeight);
           add(exampleB);
 */
 
           buttontop += exampleCH.getSize().height + 10;
 
           speedCH=new Choice();
           speedCH.setBackground(Color.white);
           speedCH.addItem("manual");
           speedCH.addItem("normal");
           speedCH.addItem("fast");
           speedCH.addItemListener(this);
           speedCH.setBounds(X+15,buttontop,80,25);
           add(speedCH);
           speedCH.select("normal");
           speed = 1;
 
           x = speedCH.getLocation().x + speedCH.getSize().width + 5;
 
           runB=new Button("Run");
           runB.addActionListener(this);
           runB.setBounds(x,buttontop,40,buttonHeight);
           add(runB);
           x += runB.getSize().width + 5;
 
           stepB=new Button("1 Step");
           stepB.addActionListener(this);
           stepB.setBounds(x,buttontop,50,buttonHeight);
           add(stepB);
           x += stepB.getSize().width + 5;
 
           stopB=new Button("Stop");
           stopB.addActionListener(this);
           stopB.setBounds(x,buttontop,40,buttonHeight);
           add(stopB);
           x += stopB.getSize().width + 5;
 
           resetB=new Button("Reset");
           resetB.addActionListener(this);
           resetB.setBounds(x,buttontop,40,buttonHeight);
           add(resetB);
           x += resetB.getSize().width + 5;
 
           clearB=new Button("Clear Mem");
           clearB.addActionListener(this);
           clearB.setBounds(x,buttontop,80,buttonHeight);
           add(clearB);
           x += clearB.getSize().width + 5;
 
           x = runB.getLocation().x - 80;
           buttontop += clearB.getSize().height + 5;
 
           x = X + 15;
 
           advanceB = new Button("Advance");
           advanceB.addActionListener(this);
           advanceB.setBounds(x,buttontop,55,buttonHeight);
           advanceB.setEnabled(false);
           add(advanceB);
 //        buttonColor = advanceB.getBackground();
 //        buttonColor = new Color(230,230,230);
           x += advanceB.getSize().width + 5;
 
           advanceMsgTF = new TextField("");
           advanceMsgTF.setBounds(x, buttontop, 60, buttonHeight);
           advanceMsgTF.setBackground(Color.white);
           add(advanceMsgTF);
 
           x += advanceMsgTF.getSize().width + 5;
 
           logB=new Button("Log Window");
           logB.addActionListener(this);
           logB.setBounds(x,buttontop,90,buttonHeight);
           add(logB);
           x += logB.getSize().width + 5;
 
           asmB=new Button("Assemble Window");
           asmB.addActionListener(this);
           asmB.setBounds(x,buttontop,120,buttonHeight);
           add(asmB);
           x += asmB.getSize().width + 5;
 
 
           buttontop += advanceB.getSize().height + 5;
           x = X+92;
 
           helpB=new Button("Help");
           helpB.addActionListener(this);
           helpB.setBounds(x,buttontop,40,buttonHeight);
           add(helpB);
           x += helpB.getSize().width + 5;
 
           opcodesB=new Button("Opcode list");
           opcodesB.addActionListener(this);
           opcodesB.setBounds(x,buttontop,85,buttonHeight);
           add(opcodesB);
           x += opcodesB.getSize().width + 5;
 
           moreB=new Button("More info");
           moreB.addActionListener(this);
           moreB.setBounds(x,buttontop,70,buttonHeight);
           add(moreB);
           x += moreB.getSize().width + 5;
 
           buttontop += moreB.getSize().height + 5;
           x = X+140;
 
           loadB=new Button("Load");
           loadB.addActionListener(this);
           loadB.setBounds(x,buttontop,40,buttonHeight);
           add(loadB);
           x += loadB.getSize().width + 5;
 
           saveB=new Button("Save");
           saveB.addActionListener(this);
           saveB.setBounds(x,buttontop,40,buttonHeight);
           add(saveB);
           x += saveB.getSize().width + 5;
 
           logwindow = new LogWindow("Running log of instructions", 10, 10,
                                     400, 350, Color.white);
 
           asm = new Assembler(this);
 
           addWindowListener(
                new WindowAdapter() {
                     public void windowClosing (WindowEvent we) {
                          dispose();
                          System.exit(1);
                     }
                }    
           );
 
           setSize(750, 600);
           setVisible(true);
 
           setBackground(acolor);
           setVisible(true);
           setLocation(10,10);
           setSize(688,562);
           buffer = createImage(getSize().width, getSize().height);
           gg = buffer.getGraphics();
           repaint();
      }
 
      public void actionPerformed(ActionEvent e)
      {
           if (e.getSource() == runB) {
                if (memory0.getText().length() == 0) {
                     new Popup("You must type in a program\n"+
                               "or load an example first!");
                     return;
                }
                if (pcTF.getText().length() == 0)
                     reset();
                if (!running)
                     run(1000);
           }
 
          if (e.getSource() == stepB) 
                run(1);
 
           if (e.getSource() == advanceB) {
                if (running && speed == 0)
                     registerClick();
           }
 
           if (e.getSource() == stopB) 
                if (running) {
                     mustStop = true;
                     running = false;
                }
 
           if (e.getSource() == resetB) {
                reset();
                repaint();
           }
 
           if (e.getSource() == clearB) 
                clearMem();
 
 /*
           if (e.getSource() == exampleB) 
                loadExample();
 */
 
           if (e.getSource() == logB) 
                logwindow.setVisible(true);
 
           if (e.getSource() == asmB) 
                asm.setVisible(true);
 
           if (e.getSource() == helpB) 
                help();
 
           if (e.getSource() == opcodesB) 
                opcodes();
 
           if (e.getSource() == moreB) 
                more();
 
           if (e.getSource() == inputTF) 
                returnSignaled = true;
 
           if (e.getSource() == loadB) 
                loadMachineProgram();
 
           if (e.getSource() == saveB) 
                saveMachineProgram();
      }
 
 
      public void componentResized(ComponentEvent e) {}
      public void componentHidden(ComponentEvent e) {}
      public void componentMoved(ComponentEvent e) {}
      public void componentShown(ComponentEvent e) {}
 
      private void clearMem() {
           for (int i=0; i<16; i++) {
                if (memBinary)
                     memory[i].setText("0000000000000000");
                else
                     memory[i].setText("0");
           }
      }
 
      private void reset() {
           pc = 0;
           setPC(0);
           setIR(0);
           setACC(0);
           tempTF.setText("0000000000000000");
           statusTF.setText("");
           shadowirTF.setText("");
           inputTF.setText("");
           outputTF.setText("");
      }
 
      private void loadExample() {
           clearMem();
           reset();
           if (exampleCH.getSelectedItem().startsWith("Example 1")) {
                      // simple sequence
                setMem(0,  "0100 0000 0000 0101", true);  // LDI 5
                setMem(1,  "0001 0000 0000 0100", true);  // ADD X
                setMem(2,  "0101 0000 0000 0101", true);  // STD Y
                setMem(3,  "1111 0000 0000 0000", true);  // STP
                setMem(4,  "0000 0000 0010 1000", true);  // -- 40   (X)
                setMem(5,  "0000 0000 0000 0000", true);  // --- 0   (Y)
           }
           if (exampleCH.getSelectedItem().startsWith("Example 2")) {
                      // input and output
                setMem(0,  "0110 0000 0000 0000", true);  // INP
                setMem(1,  "0111 0000 0000 0000", true);  // OUT
                setMem(2,  "1111 0000 0000 0000", true);  // STP
           }
           if (exampleCH.getSelectedItem().startsWith("Example 3")) {
                      // negative numbers
                setMem(0,  "0100 0000 0000 0100", true);  // 0  LDI  4
                setMem(1,  "0010 0000 0000 0101", true);  // 1  SUB  5
                setMem(2,  "1111 0000 0000 0000", true);  // 2  STP   
                setMem(5,  "0000 0000 0000 0110", true);  // 5  ---  6
           }
           if (exampleCH.getSelectedItem().startsWith("Example 4")) {
                      // copy a number
                setMem(0,  "0100 0000 0000 1101", true);  // 0  LDI  13
                setMem(1,  "0011 0000 0000 1101", true);  // 1  LOD  13
                setMem(2,  "0101 0000 0000 1100", true);  // 2  STO  12
                setMem(3,  "1111 0000 0000 0000", true);  // 3  STP   
                setMem(13, "0000 0000 0000 0110", true);  // 13 ---  6
           }
           if (exampleCH.getSelectedItem().startsWith("Example 5")) {
                      // counting loop
                setMem(0,  "0100 0000 0000 0110", true);  // 0  LDI  6
                setMem(1,  "1001 0000 0000 0100", true);  // 1  JNG  4
                setMem(2,  "0010 0000 0000 0101", true);  // 2  SUB  5
                setMem(3,  "1000 0000 0000 0001", true);  // 3  JMP  1
                setMem(4,  "1111 0000 0000 0000", true);  // 4  STP   
                setMem(5,  "0000 0000 0000 0001", true);  // 5  ---  1
           }
           if (exampleCH.getSelectedItem().startsWith("Example 6")) {
                      // gcd
                setMem(0,  "0011 0000 0000 1110", true);  // 0  LOD  A
                setMem(1,  "0010 0000 0000 1111", true);  // 1  SUB  B
                setMem(2,  "1010 0000 0000 1010", true);  // 2  JZR  10
                setMem(3,  "1001 0000 0000 0110", true);  // 3  JNG  6 
                setMem(4,  "0101 0000 0000 1110", true);  // 4  STO  A 
                setMem(5,  "1000 0000 0000 0000", true);  // 5  JMP  0 
                setMem(6,  "0011 0000 0000 1111", true);  // 6  LOD  B
                setMem(7,  "0010 0000 0000 1110", true);  // 7  SUB  A 
                setMem(8,  "0101 0000 0000 1111", true);  // 8  STO  B 
                setMem(9,  "1000 0000 0000 0000", true);  // 9  JMP  0
                setMem(10, "1111 0000 0000 0000", true);  // 10 STP    
                setMem(14, "0000 0000 0001 0010", true);  // 14 .... A=18
                setMem(15, "0000 0000 0001 1000", true);  // 15 .... B=24
           }
      }
 
      public void itemStateChanged (ItemEvent e) {
           if (e.getSource() == cpuCB) {
                cpuBinary = !cpuBinary;
                if (cpuBinary)
                     convertCpu2Binary();
                else
                     convertCpu2Decimal();
           }
           else if (e.getSource() == memCB) {
                memBinary = !memBinary;
                if (memBinary)
                     convertMem2Binary();
                else
                     convertMem2Decimal();
           }
           else if (e.getSource() == speedCH) {
                String s = speedCH.getSelectedItem();
                     if (s.equals("manual")) speed = 0;
                else if (s.equals("normal")) speed = 1;
                else if (s.equals("fast"))   speed = 2;
 
                advanceB.setEnabled(speed == 0);
           }
           else if (e.getSource() == exampleCH)
                loadExample();
      }
 
      public void paint(Graphics g)
      {
           if (buffer == null || gg == null) return;
           gg.setColor(acolor);
           gg.fillRect(0,0,getSize().width, getSize().height);
 
           // This is the bus, a big red line
 
           gg.setColor(Color.red);
           gg.fillRect(15,busTop,405,20);        // long horizontal bar
           gg.fillRect(100,iotop+10,20,180);     // vert. bar from input
           gg.fillRect(60,iotop+60,20,130);      // vert. bar from output
           gg.fillRect(cpuleft+69,cpubot,20,20); // vert. bar from CPU
 
           gg.setColor(Color.black);
           gg.setFont(new Font("SansSerif", Font.BOLD, 36));
           gg.drawString("Super Simple CPU", 15, Y+37);
 
           gg.drawRect (420, memtop+10, 180, 470);              // memory
           gg.drawRect (cpuleft, cputop, 165, cpubot-cputop);   // cpu
           gg.drawRect (24, iotop-10, 140, 100);                // io
 
           gg.setFont(new Font("SansSerif", Font.PLAIN, 12));
 
           int y = memtop+42;
           for (int i=0; i<16; i++) {
                int x = 445;
                if (i > 9)
                     x = x-7;
                gg.drawString(i+"",x,y);
                if (pc == i) 
                     drawArrow(gg, y, Color.red);
                y += 26;
           }
 
           int yoffset = 15;
           gg.drawString("PC",  cpuleft+9, cputop + pcY  + yoffset);
           gg.drawString("ACC", cpuleft+4, cputop + accY + yoffset);
           gg.drawString("TEMP", cpuleft+0, cputop + tempY + yoffset);
           gg.drawString("IR",  cpuleft+8, cputop + irY  + yoffset);
 
           gg.setFont(new Font("SansSerif", Font.BOLD, 16));
           gg.drawString("CPU", cpuleft+30, cputop-10);
 
           gg.drawString("MEMORY", 470, memtop);
           gg.drawString("INPUT", 27, iotop+5);
           gg.drawString("OUTPUT", 27, iotop+55);
 
           if (showBusValue) {
                gg.setColor(Color.yellow);
                gg.setFont(new Font("SansSerif", Font.PLAIN, 10));
                gg.drawString(U.padout(U.dec2bin(busValue), '0', 16),
                              busPosX, busTop+10);
           }
 
           g.drawImage(buffer,0,0,this);
      }
 
      public void update (Graphics g) {
           paint (g);
      }
 
      private void convertMem2Decimal() {
           for (int i=0; i<16; i++) {
                memory[i].setText(""+U.bin2dec(memory[i].getText()));
           }
      }
 
      private void convertMem2Binary() {
           for (int i=0; i<16; i++) {
                String s = U.dec2bin(U.atoi(memory[i].getText()));
                s = U.padout(s, '0', 16);
                memory[i].setText(s);
           }
      }
 
      private void convertCpu2Decimal() {
           pcTF.setText(""+U.bin2dec(pcTF.getText()));
           accTF.setText(""+U.bin2dec(accTF.getText()));
           tempTF.setText(""+U.bin2dec(tempTF.getText()));
           irTF.setText(""+U.bin2dec(irTF.getText()));
           inputTF.setText(""+U.bin2dec(inputTF.getText()));
           outputTF.setText(""+U.bin2dec(outputTF.getText()));
      }
 
      private void convertCpu2Binary() {
           pcTF.setText(U.padout(U.dec2bin(U.atoi(pcTF.getText())),'0', 16));
           accTF.setText(U.padout(U.dec2bin(U.atoi(accTF.getText())),'0', 16));
           tempTF.setText(U.padout(U.dec2bin(U.atoi(tempTF.getText())),'0', 16));
           irTF.setText(U.padout(U.dec2bin(U.atoi(irTF.getText())),'0', 16));
           inputTF.setText(U.padout(U.dec2bin(U.atoi(inputTF.getText())),'0', 16));
           outputTF.setText(U.padout(U.dec2bin(U.atoi(outputTF.getText())),'0', 16));
      }
 
      public boolean okAddress (int address) {
           if (address < 0 || address > 15) {
                new Popup("Attempting to access illegal memory address: "+
                          address);
                mustStop = true;
                return false;
           }
           return true;
      }
 
      public int getMem (int address) {
           if (!okAddress(address)) return 0;
           if (memBinary)
                return U.bin2dec(memory[address].getText());
           else
                return U.atoi(memory[address].getText());
      }
 
      public void setMem (int address, int value) {
           if (!okAddress(address)) return;
           if (memBinary)
                memory[address].setText(U.padout(U.dec2bin(value),'0',16));
           else
                memory[address].setText(value+"");
      }
 
      public void setMem (int address, String svalue, boolean inBinary) {
           if (!okAddress(address)) return;
           svalue = U.squish(svalue, ' ');
           int value = U.bin2dec(svalue);
           setMem(address, value);
      }
 
      public int getPC () {
           if (cpuBinary)
                return U.bin2dec(pcTF.getText());
           else
                return U.atoi(pcTF.getText());
      }
 
      public void setPC (int value) {
           if (cpuBinary)
                pcTF.setText(U.padout(U.dec2bin(value),'0',16));
           else
                pcTF.setText(value+"");
           pc = value;
      }
 
      public int getACC () {
           if (cpuBinary)
                return U.bin2dec(accTF.getText());
           else
                return U.atoi(accTF.getText());
      }
      public void setACC (int value) {
           if (cpuBinary)
                accTF.setText(U.padout(U.dec2bin(value),'0',16));
           else
                accTF.setText(value+"");
      }
 
      public int getIR () {
           if (cpuBinary)
                return U.bin2dec(irTF.getText());
           else
                return U.atoi(irTF.getText());
      }
      public void setIR (int value) {
           if (cpuBinary)
                irTF.setText(U.padout(U.dec2bin(value),'0',16));
           else
                irTF.setText(value+"");
      }
 
      public int getIN () {
           if (cpuBinary)
                return U.bin2dec(inputTF.getText());
           else
                return U.atoi(inputTF.getText());
      }
      public void setIN (int value) {
           if (cpuBinary)
                inputTF.setText(U.padout(U.dec2bin(value),'0',16));
           else
                inputTF.setText(value+"");
      }
 
      public int getOUT () {
           if (cpuBinary)
                return U.bin2dec(outputTF.getText());
           else
                return U.atoi(outputTF.getText());
      }
      public void setOUT (int value) {
           if (cpuBinary)
                outputTF.setText(U.padout(U.dec2bin(value),'0',16));
           else
                outputTF.setText(value+"");
      }
 
      public static String translate (String binopcode) {
                if (binopcode.equals("1111"))   return "STP";
           else if (binopcode.equals("0001"))   return "ADD";
           else if (binopcode.equals("0010"))   return "SUB";
           else if (binopcode.equals("0011"))   return "LOD";
           else if (binopcode.equals("0100"))   return "LDI";
           else if (binopcode.equals("0101"))   return "STO";
           else if (binopcode.equals("0110"))   return "INP";
           else if (binopcode.equals("0111"))   return "OUT";
           else if (binopcode.equals("1000"))   return "JMP";
           else if (binopcode.equals("1001"))   return "JNG";
           else if (binopcode.equals("1010"))   return "JZR";
           return "???";        // default, otherwise Java complains
      }
 
      public static String detranslate (String mnemonic) {
                if (mnemonic.equals("STP"))   return "1111";
           else if (mnemonic.equals("ADD"))   return "0001";
           else if (mnemonic.equals("SUB"))   return "0010";
           else if (mnemonic.equals("LOD"))   return "0011";
           else if (mnemonic.equals("LDI"))   return "0100";
           else if (mnemonic.equals("STO"))   return "0101";
           else if (mnemonic.equals("INP"))   return "0110";
           else if (mnemonic.equals("OUT"))   return "0111";
           else if (mnemonic.equals("JMP"))   return "1000";
           else if (mnemonic.equals("JNG"))   return "1001";
           else if (mnemonic.equals("JZR"))   return "1010";
           return "ERROR";        // default, otherwise Java complains
      }
 
      private void perform (int instruction) {
 
           if (sinstruction.equals("STP")) {
                mustStop = true;
                status("Stopping the computer");
                running = false;
           }
           else if (sinstruction.equals("ADD")) {
                status("Fetching operand");
                waitForClick();
 
                int operandAddress = U.bin2dec(soperand);
                int operand = getMem(operandAddress);
                animate(operandAddress, "right");
                tempSelectTF(memory[operandAddress]);
                animate(operand, "left");
                if (cpuBinary)
                     tempTF.setText(U.padout(U.dec2bin(operand), '0', 16));
                else
                     tempTF.setText(operand+"");
                tempSelectTF(tempTF);
 
                status("Peforming ADD instruction");
                waitForClick();
 
                status("Saving result into ACC");
                waitForClick();
                acc = getACC();
                acc += operand;
                setACC(acc);
 
                delay(10);
 
                status("Updating PC");
                waitForClick();
                setPC(pc+1);
           }
           else if (sinstruction.equals("SUB")) {
                status("Fetching operand");
                waitForClick();
 
                int operandAddress = U.bin2dec(soperand);
                int operand = getMem(operandAddress);
                animate(operandAddress, "right");
                tempSelectTF(memory[operandAddress]);
                animate(operand, "left");
                if (cpuBinary)
                     tempTF.setText(U.padout(U.dec2bin(operand), '0', 16));
                else
                     tempTF.setText(operand+"");
                tempSelectTF(tempTF);
 
                status("Peforming SUB instruction");
                waitForClick();
 
                status("Saving result into ACC");
                waitForClick();
                acc = getACC();
                acc -= operand;
                setACC(acc);
 
                delay(10);
 
                status("Updating PC");
                waitForClick();
                setPC(pc+1);
           }
           else if (sinstruction.equals("OUT")) {
                status("Getting value out of ACC");
                waitForClick();
 
                acc = getACC();
 
                status("Outputing value");
                waitForClick();
 
                animate (acc, "left", "IO");
                setOUT(acc);
                tempSelectTF(outputTF);
                delay(10);
 
                status("Updating PC");
                waitForClick();
                setPC(pc+1);
           }
           else if (sinstruction.equals("INP")) {
                status("Asking INPUT device for value");
                waitForClick();
 
                returnSignaled = false;
                if (cpuBinary)
                     status("Waiting for your (binary) input...press RETURN when done");
                else
                     status("Waiting for your (decimal) input...press RETURN when done");
 
                inputTF.setBackground(Color.yellow);
                inputTF.requestFocus();
                while (!returnSignaled) 
                     delay(50);
 
                status("Saving result");
                inputTF.setBackground(Color.white);
                animate (getIN(), "right", "IO");
                setACC(getIN());
                tempSelectTF(accTF);
                delay(10);
 
                status("Updating PC");
                waitForClick();
                setPC(pc+1);
           }
           else if (sinstruction.equals("LDI")) {
                status("Loading constant");
                waitForClick();
                int operand = U.bin2dec(soperand);
 
                status("Saving into ACC");
                waitForClick();
                setACC(operand);
                acc = operand;
                delay(10);
 
                status("Updating PC");
                waitForClick();
                setPC(pc+1);
           }
           else if (sinstruction.equals("LOD")) {
                status("Fetching operand");
                waitForClick();
 
                int operandAddress = U.bin2dec(soperand);
                animate(operandAddress, "right");
                tempSelectTF(memory[operandAddress]);
                int operand = getMem(operandAddress);
                animate(operand, "left");
 
                status("Saving into ACC");
                waitForClick();
                acc = operand;
                setACC(acc);
                delay(10);
 
                status("Updating PC");
                waitForClick();
                setPC(pc+1);
           }
           else if (sinstruction.equals("STO")) {
                status("Storing result into memory");
                waitForClick();
 
                int operandAddress = U.bin2dec(soperand);
                animate(operandAddress,"right");
                setMem(operandAddress, getACC());
                tempSelectTF(memory[operandAddress]);
                delay(10);
 
                status("Updating PC");
                waitForClick();
                setPC(pc+1);
           }
           else if (sinstruction.equals("JMP")) {
                status("Jumping to new PC");
                waitForClick();
 
                int operandAddress = U.bin2dec(soperand);
                animate(operandAddress,"right");
                setPC(operandAddress);
           }
           else if (sinstruction.equals("JNG")) {
                status("Checking accumulator value, is it negative?");
                waitForClick();
 
                acc = getACC();
                delay(10);
 
                if (acc < 0) {
                     status("Jumping to new PC");
                     waitForClick();
                     int operandAddress = U.bin2dec(soperand);
                     setPC(operandAddress);
                }
                else {
                     status("No, so updating PC normally");
                     waitForClick();
                     setPC(pc+1);
                }
           }
           else if (sinstruction.equals("JZR")) {
                status("Checking accumulator value, is it zero?");
                waitForClick();
 
                acc = getACC();
                delay(10);
 
                if (acc == 0) {
                     status("Jumping to new PC");
                     waitForClick();
                     int operandAddress = U.bin2dec(soperand);
                     setPC(operandAddress);
                }
                else {
                     status("No, so updating PC normally");
                     waitForClick();
                     setPC(pc+1);
                }
           }
           repaint();
           delay(50);
      }
 
      public void run (int tempnumsteps) {
           numsteps = tempnumsteps;
           running = true;
           new Thread() {
                public void run() {
                     mustStop = false;
                     int kount = 0;
                     while (!mustStop && kount < numsteps) {
                          shadowirTF.setText("");
 
                          status("Locating next instruction");
                          tempSetColor(pcTF,"yellow");
                          pc = getPC();
                          animate(pc, "right");
                          tempSelectTF(memory[pc]);
                          delay(10);
 
                          status("Fetching next instruction");
                          waitForClick();
 
                          animate(getMem(pc), "left");
                          setIR (getMem(pc));
                          tempSetColor(irTF,"red");
 
                          int instruction = getIR();
                          String temp = U.padout(U.dec2bin(instruction),'0',16);
                          if (cpuBinary)
                               irTF.select(0,4);
                
                          status("Decoding instruction");
                          shadowirTF.setText("");
                          repaint();
                          waitForClick();
                
                          sinstruction = translate(temp.substring(0,4));
                          soperand = temp.substring(4);
                          shadowirTF.setText(sinstruction+"  "+U.bin2dec(soperand));
                          logwindow.add(pc+": "+sinstruction+"  "+U.bin2dec(soperand));
                          repaint();
                          delay(100);
 
                          status("Performing instruction");
                          waitForClick();
 
                          perform (instruction);
                          logwindow.addx("  acc="+acc);
                          delay(400);
                          kount++;
                     }
                }
           }.start();
      }
 
      private void registerClick() {
 //        advanceB.setBackground(buttonColor);
           advanceMsgTF.setText("");
           Thread.yield();
           click1++;
      }
 
      private void waitForClick() {
           if (speed > 0) return;
 //        advanceB.setBackground(Color.pink);
           advanceMsgTF.setText("ready...");
           Thread.yield();
           click2 = click1+1;
           while (click1 < click2) 
                U.sleep(30);
      }
 
      private void animate (int value, String direction) {
           animate(value, direction, "regular");
      }
 
      private void animate (int value, String direction, String type) {
           int busLeft = cpuleft;
           int busRight = busLeft + 165;
 
           if (type.equals("IO")) {
                busLeft = 10;
                busRight = cpuleft;
           }
 
           if (speed > 1) return;
           if (direction.equals("right")) {
                showBusValue = true;
                int x = busLeft;
                while (x < busRight-10) {
                     busValue = value;
                     busPosX = x;
                     repaint();
                     delay(50);
                     x += 20;
                }
           }
           else if (direction.equals("left")) {
                showBusValue = true;
                int x = busRight-10;
                while (x > busLeft) {
                     busValue = value;
                     busPosX = x;
                     repaint();
                     delay(50);
                     x -= 20;
                }
           }
           showBusValue = false;
           repaint();
      }
 
      private void tempSetColor (TextField tf, String c) {
           tf.setBackground(U.translateColor(c));
           U.sleep(150);
           tf.setBackground(Color.white);
      }
 
      private void tempSelectTF (TextField tf) {
           tf.select(0,16);
           U.sleep(150);
           tf.select(0,0);
      }
 
      private void status (String s) {
           statusTF.setText(s);
           U.sleep(75);
      }
 
      private void drawArrow (Graphics g, int position, Color c) {
           Color saved = g.getColor();
           g.setColor(c);
           position -= 5;
           int endingX = 440;
           g.drawLine (433,position-5,endingX,position);
           g.drawLine (425,position,endingX,position);
           g.drawLine (433,position+5,endingX,position);
           g.setColor(saved);
      }
 
      private void delay (int milliseconds) {
           if (speed > 1)     
                U.sleep(20);
           else
                U.sleep(200);
      }
 
      private void help () {
           Popup p = new Popup("HELP\n",100,100,400,350,Color.pink);
           p.add("--------------------------------------------\n");
           p.add("This applet demonstrates the fetch/decode/execute\n");
           p.add("cycle of a very simple von-Neumann computer.\n");
           p.add("Put your program in the 16 memory cells and click run.\n");
           p.add("Or select one of the examples.  Then press load example\n");
           p.add("and run.\n\n");
           p.add("Numbers are stored as 16-bit binary values, using\n");
           p.add("2's complement for negative numbers.\n");
           p.add("You can view the values in binary or decimal by\n");
           p.add("checking or unchecking the appropriate box.\n");
           p.add("The CPU and memory are viewed separately.\n");
           p.add("\n");
           p.add("When responding to the INP (input) instruction,\n");
           p.add("type your value in the INPUT text area and press RETURN.\n");
           p.add("If you are viewing numbers in binary, you must enter\n");
           p.add("the value in binary.  Otherwise type a decimal number.\n");
           p.add("\n");
           p.add("You can stop a program with an infinite loop by pressing\n");
           p.add("the STOP button.\n");
           p.add("\n");
           p.add("To restart an example program, click Load example again.\n");
           p.add("If you typed in your own program, click on RESET.\n");
           p.add("\n");
           p.add("To view the program's actions slowly, click on the 1 step\n");
           p.add("button to watch it advance just 1 instruction.\n");
      }
 
      private void opcodes() {
           Popup p = new Popup("OPCODES\n\n",100,100,400,350,Color.pink);
           p.setFont (new Font("Courier",Font.PLAIN,12));
           p.add("Binary  Mnemonic        Short Explanation     \n");
           p.add("--------------------------------------------\n");
           p.add("1111    STP         Stop the computer\n");
           p.add("0001    ADD         Add accum. to operand\n");
           p.add("0010    SUB         Subtract operand from accum.\n");
           p.add("0011    LOD         Load memory cell into accum.\n");
           p.add("0100    LDI         Load immediate into accum.\n");
           p.add("0101    STO         Store accum. into memory cell\n");
           p.add("0110    INP         Input value and store into accum.\n");
           p.add("0111    OUT         Output the value from accum.\n");
           p.add("1000    JMP         Jump to instruction\n");
           p.add("1001    JNG         Jump to instruction if accum<0\n");
           p.add("1010    JZR         Jump to instruction if accum=0\n");
      }
 
      private void more() {
           Popup p = new Popup("",100,100,500,500,Color.pink);
           p.setFont (new Font("Courier",Font.PLAIN,12));
           p.add("                MORE ABOUT THE INSTRUCTIONS\n");
           p.add("==================================================================\n");
           p.add("1111 STP  -- this stops the computer, no more fetch/decode/execute\n");
           p.add("             cycles until you reset.\n");
           p.add("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
           p.add("0001 ADD  -- fetch a number from memory and add it to the\n");
           p.add("             contents of the accumulator, replacing the value\n");
           p.add("             value in the accumulator.\n");
           p.add("\n");
           p.add("  E.g.  0001 000000001111  -- get the value at memory location 15\n");
           p.add("                and add that to accumulator.\n");
           p.add("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
           p.add("0010 SUB  -- just like ADD, only subtract.\n");
           p.add("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
           p.add("0011 LOD  -- fetch a number from memory and store it into\n");
           p.add("             the accumulator, replacing its old value.\n");
           p.add("\n");
           p.add("  E.g.  0011 000000001111  -- get the value at memory location 15\n");
           p.add("                and store that value into the accumulator.\n");
           p.add("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
           p.add("0100 LDI  -- load immediate; the value to be put into the\n");
           p.add("             accumulator is the rightmost 12 bits of the\n");
           p.add("             instruction; do not go to memory like LOD\n");
           p.add("\n");
           p.add("  E.g.  0100 000000001111  -- store the value 15 into the\n");
           p.add("                accumulator.\n");
           p.add("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
           p.add("0101 STO  -- store the accumulator's value into memory at the\n");
           p.add("             indicated location.\n");
           p.add("\n");
           p.add("  E.g.  0101 000000001111  -- store the accumulator's value\n");
           p.add("                into memory location 15.\n");
           p.add("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
           p.add("0110 INP  -- ask the user for one number and store that into\n");
           p.add("             the accumulator.\n");
           p.add("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
           p.add("0111 OUT  -- copy the value in the accumulator to the output\n");
           p.add("             area.\n");
           p.add("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
           p.add("1000 JMP  -- jump to the instruction at the indicated memory\n");
           p.add("             address.\n");
           p.add("\n");
           p.add("  E.g.  1000 000000001111  -- put the value 15 into the PC\n");
           p.add("                which will cause the next instruction to\n");
           p.add("                be taken from location 15 of memory.\n");
           p.add("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
           p.add("1001 JNG  -- jump to the instruction at the indicated memory\n");
           p.add("             location if the accumulator's value is negative;\n");
           p.add("             otherwise just add 1 to the PC.\n");
           p.add("\n");
           p.add("  E.g.  1001 000000001111  -- put the value 15 into the PC\n");
           p.add("                if accumulator < 0, otherwise go to the\n");
           p.add("                next instruction.\n");
           p.add("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
           p.add("1010 JZR  -- jump to the instruction at the indicated memory\n");
           p.add("             location if the accumulator's value is zero;\n");
           p.add("             otherwise just add 1 to the PC.\n");
           p.add("\n");
           p.add("  E.g.  1010 000000001111  -- put the value 15 into the PC\n");
           p.add("                if accumulator = 0, otherwise go to the\n");
           p.add("                next instruction.\n");
           p.add("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
      }
 
      public void loadMachineProgram() {
           String text = "";
 
           FileDialog loadfile = new
                FileDialog(this, "Load Machine Program", FileDialog.LOAD);
           loadfile.setFile("*.txt");
           loadfile.show();
 
           String filename = loadfile.getFile();
           String directory = loadfile.getDirectory();
 //System.out.println ("directory="+directory);
           if (filename == null)
                return;
           if (!filename.endsWith(".txt")) {
                System.err.println ("An machine language file must have an extension of .txt.");
                return;
           }
 
           BufferedReader is;        // input stream
 
           if(filename == null || filename.length() == 0)
                return;
 
           this.setTitle(filename);
           int i = 0;
           try {
                is = new BufferedReader(new FileReader(directory+"\\"+filename));
                String line;
                while ((line = is.readLine()) != null) 
                     memory[i++].setText(line);
                is.close();
           }
           catch(IOException ioe) {
                System.err.println("LoadProgram:  i/o exception.");
                ioe.printStackTrace();
           }
           return;
      }
 
      public void saveMachineProgram () {
           FileDialog savefile =
                new FileDialog(this,"Save Machine Program",FileDialog.SAVE);
           savefile.setFile("*.txt");
           savefile.show();
 
           String filename = savefile.getFile();
           String directory = savefile.getDirectory();
           String fullname = directory + "\\"+filename;
           
           if(filename == null)
                return;
 
           BufferedWriter os;        // output stream
 
           if(filename == null || filename.length() == 0)
                return;
           else {
                this.setTitle(filename);
                try {
                     os = new BufferedWriter(new FileWriter(fullname, false));
 
                     for (int i=0; i<16; i++)
                          os.write(memory[i].getText()+"\n");
                     os.close();
                }
                catch(IOException ioe) {
                     System.err.println("SaveProgram:  i/o exception.");
                }
           }
      }
 }
