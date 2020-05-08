 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package view;
 
 /**
  *
  * @author Admin */
 //package org.main.graphics;
 
 import controller.PrimerDesign;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.net.URL;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.*;
 import javax.swing.*;
 
 public class PCRAnimation {
     
      private static void createAndShowUI() {
         int dw = 1024; //resolution width
         int dh = 768; //resolution height
         int phase=0;
         Image taq = null;
         Image a1 = null;
         Image a2 = null;
         Image c1 = null;
         Image c2 = null;
         Image t1 = null;
         Image t2 = null;
         Image g1 = null;
         Image g2 = null;
         Image thermol = null;
         Image thermom = null;
         Image thermoh = null;
         try {
             URL path = ClassLoader.getSystemResource("view/AnimationImages/taq.png");
             taq = ImageIO.read(path);
             path = ClassLoader.getSystemResource("view/AnimationImages/nodeA1.png");
             a1 = ImageIO.read(path);
             path = ClassLoader.getSystemResource("view/AnimationImages/nodeA2.png");
             a2 = ImageIO.read(path);
             path = ClassLoader.getSystemResource("view/AnimationImages/nodeC1.png");
             c1 = ImageIO.read(path);
             path = ClassLoader.getSystemResource("view/AnimationImages/nodeC2.png");
             c2 = ImageIO.read(path);
             path = ClassLoader.getSystemResource("view/AnimationImages/nodeG1.png");
             g1 = ImageIO.read(path);
             path = ClassLoader.getSystemResource("view/AnimationImages/nodeG2.png");
             g2 = ImageIO.read(path);
             path = ClassLoader.getSystemResource("view/AnimationImages/nodeT1.png");
             t1 = ImageIO.read(path);
             path = ClassLoader.getSystemResource("view/AnimationImages/nodeT2.png");
             t2 = ImageIO.read(path);
             path = ClassLoader.getSystemResource("view/AnimationImages/thermo1.png");
             thermol = ImageIO.read(path);
             path = ClassLoader.getSystemResource("view/AnimationImages/thermo2.png");
             thermom = ImageIO.read(path);
             path = ClassLoader.getSystemResource("view/AnimationImages/thermo3.png");
             thermoh = ImageIO.read(path);
             final MyPanel panel = new MyPanel(taq,a1,a2,c1,c2,g1,g2,t1,t2,thermol,thermom,thermoh,dw,dh); 
             final JFrame frame = new JFrame("PCR Animation");
             
             //frame.getContentPane().add(prevBtn);
             frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
             
             frame.setSize(new Dimension(dw, dh));
             frame.getContentPane().add(panel);
             
             JButton nextBtn = new JButton("Next");
             JButton prevBtn = new JButton("Previous");
             JButton closeBtn = new JButton("Close");
             JButton restartBtn = new JButton("Restart exercise");
             panel.setLayout(null);
             closeBtn.setBounds((dw-180)/6, dh-270, (dw-180)/6 -15 , 30);
             restartBtn.setBounds(2*(dw-180)/6, dh-270, (dw-180)/6 -25, 30);
             prevBtn.setBounds(3*(dw-180)/6, dh-270, (dw-180)/6 -15, 30);
             nextBtn.setBounds(4*(dw-180)/6, dh-270, (dw-180)/6 -15, 30);
             panel.add(nextBtn);
             panel.add(prevBtn);
             panel.add(closeBtn);
             panel.add(restartBtn);
             
             frame.setLocationRelativeTo(null);
             frame.setVisible(true);
             closeBtn.addActionListener(new ActionListener(){
 
                    public void actionPerformed(ActionEvent event){    
                          frame.dispose();
                    }
              });
             restartBtn.addActionListener(new ActionListener(){
 
                    public void actionPerformed(ActionEvent event){    
                          frame.dispose();
                         JFrame window;
                         Splash splash;
                         window = new JFrame("Primer Design");
                         window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                         // add start panel to frame
                         splash = new Splash();
                         window.getContentPane().add(splash);

                         // size the window and show it
                         window.pack();
                         window.setVisible(true);
                    }
              });
             nextBtn.addActionListener(new ActionListener(){
 
                    public void actionPerformed(ActionEvent event){    
                          panel.nextStage();
                    }
              });
             prevBtn.addActionListener(new ActionListener(){
 
                    public void actionPerformed(ActionEvent event){    
                          panel.previousStage();
                    }
              });
             ActionListener timerAction = new ActionListener() {
                 public void actionPerformed(ActionEvent evt) {
                     panel.animate();
                 }
             };
             Timer timer = new Timer(5, timerAction);
             timer.setRepeats(true);
             timer.start();
 
         } catch (IOException e) {
             e.printStackTrace();
         }
 
     }
 
      
     static class MyPanel extends JPanel {
         private Image taq;
         private Image  a1; 
         private Image  a2;
         private Image  c1;
         private Image  c2;
         private Image  g1;
         private Image  g2;
         private Image  t1;
         private Image  t2;
         private Image  thermol;
         private Image  thermom;
         private Image  thermoh;
         private int coordinateX = 0;
         private int coordinateY = 0;
         private String text = "";
         private String text2 = "";
         private String text3 = "";
         private int time = 0;
         private int starttime=0;
         int step=0;
         int speed = 15; //speed of the animation, speed of 1 = fastest, default 15
         private boolean incrementX = true;
         private boolean incrementY = true;
         private String DNAseq;
         private String Bseq;
         private String fwPrimer;
         private String bwPrimer;
         private int areaStart;
         private int areaEnd;
         private int dw;
         private int dh;
         public MyPanel(Image taq, Image a1, Image a2, Image c1, Image c2,
                 Image g1, Image g2, Image t1, Image t2, Image thermol,
                 Image thermom, Image thermoh, int dw, int dh) {
             this.taq = taq;
             this.a1 = a1; 
             this.a2 = a2;
             this.c1 = c1;
             this.c2 = c2;
             this.g1 = g1;
             this.g2 = g2;
             this.t1 = t1;
             this.t2 = t2;
             this.thermol = thermol;
             this.thermom = thermom;
             this.thermoh = thermoh;
             this.DNAseq = "tcgcagtttgtgcaagttttcgcagtttgtgcaagttttcgcagtttgtgcaagttttcgcagtttgtg"; //forward strand
             this.Bseq = "agcgtcaaacacgttcaaaagcgtcaaacacgttcaaaagcgtcaaacacgttcaaaagcgtcaaacac"; //backward strand
             this.areaStart = 15; //area to be contained
             this.areaEnd = 45; //area to be contained
             this.fwPrimer = "agttttcgca";
             this.bwPrimer = "aaaagcgtca";
             this.dw = dw;
             this.dh = dh;
         }
 
         @Override
         protected void paintComponent(Graphics g) {
             super.paintComponent(g);
             int taqWidth = taq.getWidth(null);
             int taqHeight = taq.getHeight(null);
             int nodeWidth = a1.getWidth(null);
             int nodeHeight = a1.getHeight(null);
             int thermoWidth = thermol.getWidth(null);
             int thermoHeight = thermol.getHeight(null);
             int scaling = (DNAseq.length()*(nodeWidth*13/14)/this.dw);
             int nw = nodeWidth/scaling;
             int nh = nodeHeight/scaling;
             int bwstart;
             int bwcur;
             int fwcur;
             String bwString;
             String fwString;
             int scfix = nw/14;
             if (time<650) {
             this.drawStrand(DNAseq, 0, 210, true, g, nw, nh, scfix);
             this.drawStrand(Bseq, 0, 210+(nh), false, g, nw, nh, scfix);
             if (time<600) {
                 g.drawImage(thermol, dw-150, dh-280, 78, 240, null);
             } else
                 g.drawImage(thermom, dw-150, dh-280, 78, 240, null);
             } else
             if(time<900) {
                     g.drawImage(thermoh, dw-150, dh-280, 78, 240, null);
                     this.drawStrand(DNAseq, 0, 210-((time-650)*200/250), true, g, nw, nh, scfix);
                     this.drawStrand(Bseq, 0, 210+((time-650)*200/250)+(nh), false, g, nw, nh, scfix);
                     g.drawImage(thermoh, dw-150, dh-280, 78, 240, null);
             } else
             if (time<2700) {                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           
                     this.drawStrand(DNAseq, 0, 10, true, g, nw, nh, scfix);
                     this.drawStrand(Bseq, 0, 410+(nh), false, g, nw, nh, scfix);
                     g.drawImage(thermom, dw-150, dh-280, 78, 240, null);
                     if(time>2350) {
                         bwstart = areaEnd-bwPrimer.length()+1;
                         bwcur = bwstart - (bwstart*(time-2350))/200;
                         if(bwcur<1) bwcur = 1;
                         bwString = Bseq.substring(bwcur-1, areaEnd);
                         fwcur = areaStart + fwPrimer.length()+((DNAseq.length()-areaStart)*(time-2350))/200;
                         if(fwcur>DNAseq.length()) fwcur = DNAseq.length();
                         fwString = DNAseq.substring(areaStart-1, fwcur);
                         this.drawStrand(bwString, ((bwcur-1)*(nw))-((bwcur-1)*scfix), 10+nh, false, g, nw, nh, scfix);
                         this.drawStrand(fwString, ((areaStart-1)*(nw))-((areaStart-1)*scfix), 410, true, g, nw, nh, scfix);
                         if(bwcur>=areaStart) {
                             this.drawStrand(bwString, ((bwcur-1)*(nw))-((bwcur-1)*scfix), 260+nh, false, g, nw, nh, scfix);
                             g.drawImage(taq, ((bwcur-1)*(nw))-((bwcur-1)*scfix)-3*nw, 260+nh, 3*nw, nh*3/2, null);
                         } else {
                             this.drawStrand(Bseq.substring(areaStart-1,areaEnd), ((areaStart-1)*(nw))-((areaStart-1)*scfix), 260+nh, false, g, nw, nh, scfix);
                         }
                         if(fwcur<=areaEnd) {
                             this.drawStrand(fwString, ((areaStart-1)*(nw))-((areaStart-1)*scfix), 160, true, g, nw, nh, scfix);
                             g.drawImage(taq, ((fwcur-1)*(nw))-((fwcur-1)*scfix), 160, 3*nw, nh*3/2, null);
                         } else {
                             this.drawStrand(DNAseq.substring(areaStart-1,areaEnd), ((areaStart-1)*(nw))-((areaStart-1)*scfix), 160, true, g, nw, nh, scfix);
                         }
                         g.drawImage(taq, ((bwcur-1)*(nw))-((bwcur-1)*scfix)-3*nw, 10+nh, 3*nw, nh*3/2, null);
                         g.drawImage(taq, ((fwcur-1)*(nw))-((fwcur-1)*scfix), 410, 3*nw, nh*3/2, null);
                     }
                     if(time>2250) {
                         if(time<2330) g.drawImage(thermol, dw-150, dh-280, 78, 240, null);
                         bwString = Bseq.substring(0, areaEnd);
                         bwstart = areaEnd-bwPrimer.length()+1;
                         fwString = DNAseq.substring(areaStart-1, DNAseq.length());
                         this.drawStrand(bwPrimer, ((bwstart-1)*(nw))-((bwstart-1)*scfix), 10+nh, false, g, nw, nh, scfix);
                         this.drawStrand(fwPrimer, ((areaStart-1)*(nw))-((areaStart-1)*scfix), 410, true, g, nw, nh, scfix);
                         this.drawStrand(bwPrimer, ((bwstart-1)*(nw))-((bwstart-1)*scfix), 260+nh, false, g, nw, nh, scfix);
                         this.drawStrand(fwPrimer, ((areaStart-1)*(nw))-((areaStart-1)*scfix), 160, true, g, nw, nh, scfix);
                         this.drawStrand(bwString, 0, 10+nh + 150, false, g, nw, nh, scfix);
                         this.drawStrand(fwString, ((areaStart-1)*(nw))-((areaStart-1)*scfix), 260, true, g, nw, nh, scfix);
                     } else
                     if(time>2000) {
                         bwString = Bseq.substring(0, areaEnd);
                         fwString = DNAseq.substring(areaStart-1, DNAseq.length());
                         this.drawStrand(bwString, 0, 10+nh + ((time-2000)*150/250), false, g, nw, nh, scfix);
                         this.drawStrand(fwString, ((areaStart-1)*(nw))-((areaStart-1)*scfix), 410-((time-2000)*150/250), true, g, nw, nh, scfix);
                         g.drawImage(thermoh, dw-150, dh-280, 78, 240, null);
                         if(time>2200) {
                             g.drawImage(thermom, dw-150, dh-280, 78, 240, null);
                         }
                     }   else
                     if (time>1650) {
                         bwstart = areaEnd-bwPrimer.length()+1;
                         bwcur = bwstart - (bwstart*(time-1650))/200;
                         if(bwcur<1) bwcur = 1;
                         bwString = Bseq.substring(bwcur-1, areaEnd);
                         fwcur = areaStart + fwPrimer.length()+((DNAseq.length()-areaStart)*(time-1650))/200;
                         if(fwcur>DNAseq.length()) fwcur = DNAseq.length();
                         fwString = DNAseq.substring(areaStart-1, fwcur);
                         this.drawStrand(bwString, ((bwcur-1)*(nw))-((bwcur-1)*scfix), 10+nh, false, g, nw, nh, scfix);
                         this.drawStrand(fwString, ((areaStart-1)*(nw))-((areaStart-1)*scfix), 410, true, g, nw, nh, scfix);
                         g.drawImage(taq, ((bwcur-1)*(nw))-((bwcur-1)*scfix)-3*nw, 10+nh, 3*nw, nh*3/2, null);
                         g.drawImage(taq, ((fwcur-1)*(nw))-((fwcur-1)*scfix), 410, 3*nw, nh*3/2, null);
                     } else
                     if(time>1100) {
                         bwstart = areaEnd-bwPrimer.length()+1; 
                         this.drawStrand(bwPrimer, ((bwstart-1)*(nw))-((bwstart-1)*scfix), 10+nh, false, g, nw, nh, scfix);
                         this.drawStrand(fwPrimer, ((areaStart-1)*(nw))-((areaStart-1)*scfix), 410, true, g, nw, nh, scfix);
                         g.drawImage(thermol, dw-150, dh-280, 78, 240, null);
                         if (time>1550) {
                             g.drawImage(taq, ((bwstart-1)*(nw))-((bwstart-1)*scfix)-3*nw, 10+nh, 3*nw, nh*3/2, null);
                             g.drawImage(taq, ((areaStart+fwPrimer.length()-1)*(nw))-((areaStart+fwPrimer.length()-1)*scfix), 410, 3*nw, nh*3/2, null);
                             g.drawImage(thermom, dw-150, dh-280, 78, 240, null);
                         } else if(time>1350) {
                             g.drawImage(thermom, dw-150, dh-280, 78, 240, null);
                         }
                     }
                     else if(time>1000) {
                         g.drawImage(thermol, dw-150, dh-280, 78, 240, null);
                     }
                     
             } else
                  {
                     Font font = new Font("DejaVu Sans", Font.PLAIN, 15);
                     g.setFont(font);
                     int spacing=0;
                     fwString=DNAseq.substring(areaStart,areaEnd);
                     bwString=Bseq.substring(areaStart,areaEnd);
                     int widthscale = (int)((((fwString.length()*nodeWidth))*9)/(dw-8*15));
                     int heightscale = 64*nodeHeight/this.dh;
                     scaling=widthscale;
                     int snw = nodeWidth/scaling;
                     nw=snw;
                     nh = nodeHeight/scaling;
                     this.drawStrand(bwString, 15, 500/2,false, g, nw, nh, scfix);
                     this.drawStrand(fwString, 15, 500/2-nh,true, g, nw, nh, scfix);
                     for (int i=1;i<5;i++) {
                     spacing = 500/((int)Math.pow(2, i)+1);
                     scaling = (int) ((nodeHeight*((i*2) * (Math.pow(2,i))))/500);
                     if(scaling<heightscale) scaling=heightscale;
                     if(scaling<widthscale) scaling=widthscale;
                     nw = nodeWidth/scaling;
                     nh = nodeHeight/scaling;
                     g.drawString(Integer.toString(i)+" cycle", i*((snw*fwString.length())+15)+15, 550);
                     g.drawString(Integer.toString((int)Math.pow(2,i)), i*(snw*fwString.length()+15)+30, 570);
                         for (int j=1;j<=((int)Math.pow(2,i));j++) {
                             this.drawStrand(bwString,(i*((snw*fwString.length())+15)), j*spacing, false, g, nw, nh, scfix);
                             this.drawStrand(fwString,(i*((snw*fwString.length())+15)), j*spacing-nh, true, g, nw, nh, scfix);
                         }
                     }
                     g.drawString(Integer.toString(5)+" cycle", 5*((snw*fwString.length())+15)+30+(int)(snw*fwString.length()/2), 550);
                     g.drawString(Integer.toString((int)Math.pow(2,5)), 5*(snw*fwString.length()+15)+45+(int)(snw*fwString.length()/2), 570);
                     for (int i=0;i<2;i++) {
                         for (int j=1;j<=((int)Math.pow(2,4));j++) {
                                 this.drawStrand(bwString,(5*((snw*fwString.length())+15))+i*(snw*(fwString.length())+15)+15, j*spacing, false, g, nw, nh, scfix);
                                 this.drawStrand(fwString,(5*((snw*fwString.length())+15))+i*(snw*(fwString.length())+15)+15, j*spacing-nh, true, g, nw, nh, scfix);
                          }
                     }
                     g.drawString("...", 7*((snw*bwString.length())+15)+15, 550);
                     g.drawString(Integer.toString(30)+" cycle", 8*((snw*fwString.length())+15)+15
                              , 550);
                     g.drawString("10^9", 8*(snw*bwString.length()+15)+30, 570);
                     for(int i = spacing;i<475;i++) { g.drawLine(8*(snw*bwString.length()+15), i, 9*(snw*bwString.length()+15), i);}
                     
                     
                 }
             if(time<2700) {
                 g.drawImage(g1, dw-230, dh- 280, nodeWidth/20, nodeHeight/20, null);
                 g.drawImage(c1, dw-230, dh- 230, nodeWidth/20, nodeHeight/20, null);
                 g.drawImage(a1, dw-230, dh- 180, nodeWidth/20, nodeHeight/20, null);
                 g.drawImage(t1, dw-230, dh- 130, nodeWidth/20, nodeHeight/20, null);
                 Font font = new Font("Lucida Sans Typewriter", Font.PLAIN, 30);
                 g.setFont(font);
                 g.drawString("G", dw-180, dh- 245);
                 g.drawString("C", dw-180, dh- 195);
                 g.drawString("A", dw-180, dh- 145);
                 g.drawString("T", dw-180, dh- 95); }
            Font font = new Font("DejaVu Sans", Font.PLAIN, 19);
             g.setFont(font);
             g.drawString(text, 30, dh-170);
             g.drawString(text2, 30, dh-130);
             g.drawString(text3, 30, dh-90); }
         
         public void drawStrand (String s, int x, int y, boolean dir, Graphics g, int nw, int nh, int scfix) { //dir: forward-true, backward-false
             for (int i=0; i<s.length(); i++) {
                 if (dir) {
                     switch (s.charAt(i)) {
                         case 'g': g.drawImage(g1, x+(i*nw)-(i*scfix), y, nw, nh, null); //i*scfix is there to fix rescaling glitches
                             break;
                         case 'c': g.drawImage(c1, x+(i*nw)-(i*scfix), y, nw, nh, null);
                             break;
                         case 'a': g.drawImage(a1, x+(i*nw)-(i*scfix), y, nw, nh, null);
                             break;
                         case 't': g.drawImage(t1, x+(i*nw)-(i*scfix), y, nw, nh, null);
                             break;
                     }
                 } else
                     switch (s.charAt(i)) {
                         case 'g': g.drawImage(g2, x+(i*nw)-(i*scfix), y, nw, nh, null);
                             break;
                         case 'c': g.drawImage(c2, x+(i*nw)-(i*scfix), y, nw, nh, null);
                             break;
                         case 'a': g.drawImage(a2, x+(i*nw)-(i*scfix), y, nw, nh, null);
                             break;
                         case 't': g.drawImage(t2, x+(i*nw)-(i*scfix), y, nw, nh, null);
                             break;
                     }
             } 
         }
         
         public void nextStage() {
             step+=1;
             if(step>7) step=7;
             starttime = (int)System.currentTimeMillis();
             switch (step) {
                     case 7: starttime=starttime - 3100*speed;
                         break;
                     case 6: starttime=starttime - 2700*speed;
                         break;
                     case 5: starttime=starttime - 2350*speed;
                         break;
                     case 4: starttime=starttime - 1900*speed;
                         break;
                     case 3: starttime=starttime - 1200*speed;
                         break;
                     case 2: starttime=starttime - 600*speed;
                         break;      
                     case 1: starttime=starttime - 250*speed;
                         break;
                     case 0:
                         break;
                 }
             }
             
             
             
         
         
          public void previousStage() {
             step-=1;
             if(step<0) step=0;
             starttime = (int)System.currentTimeMillis();
             switch (step) {
                     case 7: starttime=starttime - 3100*speed;
                         break;
                     case 6: starttime=starttime - 2700*speed;
                         break;
                     case 5: starttime=starttime - 2350*speed;
                         break;
                     case 4: starttime=starttime - 1900*speed;
                         break;
                     case 3: starttime=starttime - 1200*speed;
                         break;
                     case 2: starttime=starttime - 600*speed;
                         break;      
                     case 1: starttime=starttime - 250*speed;
                         break;
                     case 0:
                         break;
                 }
             }
          
         public void animate() {
                 
                 if(time==0) {starttime = (int)System.currentTimeMillis();
                              time = 1;}
                 if(time<250) { 
                              text="Polymerase chain reaction (PCR) is a technique used to amplify";
                              text2="specific nucleic acid sequences from DNA samples.";
                              text3="";}
                 else if (time<600) {
                              text="The reaction requires a DNA sample, a left and right primer, nucleotides(dNTPs),";
                              text2="a suitable buffer and Taq DNA polymerase. These ingredients are mixed and placed in";
                              text3="a thermocycler where three reactions take place at different temperatures for ~ 30 cycles.";
                 } else if(time<1200) {
                              text="First, denaturation occurs at ~ 95°C separating the two DNA strands.";
                              text2="The temperature is then lowered to ~ 55-65°C so primers bind to the two";
                              text3="single strands. This defines what is amplified.";
                 } else if(time<1900) {
                              text="The temperature is then raised to 72°C and the Taq DNA polymerase binds to";
                              text2="the 3’ end of the primer and the enzyme adds nucleotides, using the complementary ";
                              text3="strand as a template, creating a complementary copy.";}
                 else if (time<2350) {
                              text="The same process is repeated in all subsequent cycles of the PCR.";
                              text2="The thermocycler re-heats the DNA sample, separating the";
                              text3="complementary strands, including the newly created copies of DNA.";}
                 else if (time<2700) {
                              text="The temperature is again lowered, allowing primers to bind, followed by the";
                              text2="Taq DNA polymerase synthesizing complementary strands when ";
                              text3="the temperature is slightly raised again to 72°C.";}
                 else if(time<3100) {  
                              text="With each cycle, the number of copies doubles, so after 30 cycles,";
                              text2="there will be a billion copies of the target sequence.";
                              text3="";
                 } else {
                     text="This concludes the PCR exercise, thank you for your participation,";
                     text2="and have a nice day!";
                     text3="";
                 }
                 time=(int)(System.currentTimeMillis()-starttime)/speed+1;
                 
                 repaint();
             
         }
     }
 
     public static void main(String[] args) {
         try {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (ClassNotFoundException ex) {
             Logger.getLogger(PrimerDesign.class.getName()).log(Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             Logger.getLogger(PrimerDesign.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             Logger.getLogger(PrimerDesign.class.getName()).log(Level.SEVERE, null, ex);
         } catch (UnsupportedLookAndFeelException ex) {
             Logger.getLogger(PrimerDesign.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 createAndShowUI();
             }
         });
     }
 }
