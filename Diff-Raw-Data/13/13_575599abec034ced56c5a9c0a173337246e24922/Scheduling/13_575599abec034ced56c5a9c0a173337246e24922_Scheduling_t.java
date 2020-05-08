 package org.softwarehelps.learncs.SCHEDULING;
 
 import java.util.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.applet.*;
 
 public class Scheduling extends Applet 
         implements ActionListener, ComponentListener, ItemListener
 {
      TextField timeTF, timeleftTF, runningTF, quantumTF, quantumleftTF;
 
      TextArea logTA, joblistTA;
 
      Label titleLabel, label1, label2, label3, label4, label5, label6, label7,
            label8, label9;
 
      Button resetB, runB;
 
      Choice algoCH, exampleCH, speedCH;
      Color lightGray = new Color(240,240,240);
 
      Image buffer;
      Graphics gg;
 
      int theTime = 0;
      int current = -1;
      Job[] jobs;
      int numjobs;
      int quantumleft;
 
      int sleepTime = 300;               // Medium speed
 
      int timeQuantum = 50;
      int totalTimeInSystem = 0;
      boolean running = false;
 
      char algo;            // 'F' = First Come First Served
                            // 'S' = Shortest Job First
                            // 'R' = Round Robin
      Color acolor = new Color(230,230,230);
      Color lightBlue = new Color(255,255,30);
 
      GanttChart gchart;
      int ganttline = 435;
 
      public void init() {
           setLayout(null);
 
           titleLabel = new Label("Process Scheduling");
           titleLabel.setBounds(100,5,380,45);
           titleLabel.setFont (new Font("SansSerif", Font.BOLD, 36));
           titleLabel.setBackground(acolor);
           add(titleLabel);
 
           int y = 10 + titleLabel.getSize().height;
           int y2 = 10 + titleLabel.getSize().height;
 
           label1=new Label("Process List", Label.RIGHT);
           label1.setBackground(acolor);
           label1.setBounds(25,y,100,27);
           add(label1);
 
           y += label1.getSize().height + 2;
 
           joblistTA=new TextArea(5,40);
           joblistTA.setBackground(lightBlue);
           joblistTA.setBounds(15,y,140,120);
           joblistTA.setFont(new Font("Monospaced", Font.PLAIN, 12));
           add(joblistTA);
 
           y += 10 + joblistTA.getSize().height;
 
           y2 += 20;
 
           runB=new Button("Run");
           runB.addActionListener(this);
           runB.setBounds(168,y2,50,25);
           runB.setBackground(new Color(220,220,220));
           add(runB);
 
           y2 += 10 + runB.getSize().height;
 
           exampleCH = new Choice();
           exampleCH.setBounds(168,y2,155,25);
           exampleCH.addItem("Example 1");
           exampleCH.addItem("Example 2");
           exampleCH.addItem("Example from textbook");
           exampleCH.addItemListener(this);
           add(exampleCH);
 
           y2 += 10 + exampleCH.getSize().height;
 
           resetB=new Button("Reset");
           resetB.addActionListener(this);
           resetB.setBounds(168,y2,50,25);
           resetB.setBackground(new Color(220,220,220));
           add(resetB);
 
           //-----------------stuff on left side----------------------
 
           int labelWidth = 140;
           int leftEdge = 15;
 
           label3=new Label("Time:", Label.RIGHT);
           label3.setBackground(acolor);
           label3.setBounds(leftEdge,y,labelWidth,24);
           add(label3);
 
           timeTF=new TextField(40);
           timeTF.addActionListener(this);
           timeTF.setBounds(leftEdge+labelWidth+3,y,80,23);
           timeTF.setBackground(Color.white);
           add(timeTF);
 
           y += 10 + timeTF.getSize().height;
 
           label2=new Label("Now Running:", Label.RIGHT);
           label2.setBackground(acolor);
           label2.setBounds(leftEdge,y,labelWidth,20);
           add(label2);
 
           runningTF=new TextField(40);
           runningTF.addActionListener(this);
           runningTF.setBounds(leftEdge+labelWidth+3,y,80,23);
           runningTF.setBackground(Color.white);
           add(runningTF);
 
           y += 10 + runningTF.getSize().height;
 
           label4=new Label("Time left for this process:", Label.RIGHT);
           label4.setBackground(acolor);
           label4.setBounds(leftEdge,y,labelWidth,20);
           add(label4);
 
           timeleftTF=new TextField(40);
           timeleftTF.addActionListener(this);
           timeleftTF.setBounds(leftEdge+labelWidth+3,y,134,20);
           timeleftTF.setBackground(Color.white);
           add(timeleftTF);
 
           y += 10 + timeleftTF.getSize().height;
 
           label5=new Label("Quantum size:", Label.RIGHT);
           label5.setBackground(acolor);
           label5.setBounds(leftEdge,y,labelWidth,20);
           add(label5);
 
           quantumTF=new TextField(40);
           quantumTF.addActionListener(this);
           quantumTF.setBounds(leftEdge+labelWidth+3,y,134,20);
           quantumTF.setBackground(lightGray);
           quantumTF.setText("50");
           add(quantumTF);
 
           y += 10 + quantumTF.getSize().height;
 
           label6=new Label("Time left in quantum:", Label.RIGHT);
           label6.setBackground(acolor);
           label6.setBounds(leftEdge,y,labelWidth,20);
           add(label6);
 
           quantumleftTF=new TextField(40);
           quantumleftTF.addActionListener(this);
           quantumleftTF.setBounds(leftEdge+labelWidth+3,y,134,20);
           quantumleftTF.setBackground(lightGray);
           quantumleftTF.setText("");
           add(quantumleftTF);
 
           y += 10 + quantumleftTF.getSize().height;
 
           label7=new Label("Simulation speed:", Label.RIGHT);
           label7.setBackground(acolor);
           label7.setBounds(leftEdge,y,labelWidth,20);
           add(label7);
 
           speedCH = new Choice();
           speedCH.setBounds(leftEdge+labelWidth+3,y,134,25);
           speedCH.addItem("Crawl");
           speedCH.addItem("Very slow");
           speedCH.addItem("Slow");
           speedCH.addItem("Medium");
           speedCH.addItem("Fast");
           speedCH.addItem("Very fast");
           speedCH.addItem("No delay");
           speedCH.addItemListener(this);
           speedCH.select("Medium");
           add(speedCH);
 
           y += 10 + speedCH.getSize().height;
 
           //-------------back to the right side---------------
 
           y2 = titleLabel.getSize().height + 10;
 
           label6=new Label("Algorithm:", Label.RIGHT);
           label6.setBackground(acolor);
           label6.setBounds(331, y2, 80, 25);
           add(label6);
 
           y2 += label6.getSize().height + 1;
 
           algoCH=new Choice();
           algoCH.addItem("First Come First Served");
           algoCH.addItem("Shortest Job First");
           algoCH.addItem("Round Robin");
           algoCH.setBounds(331,y2,169,28);
           algoCH.addItemListener(this);
           add(algoCH);
   
           y2 += algoCH.getSize().height + 5;
 
           label8 = new Label("Messages and statistics:");
           label8.setBackground(acolor);
           label8.setBounds(331,y2,180,20);
           add(label8);
 
           y2 += label5.getSize().height + 5;
 
           logTA=new TextArea(5,40);
           logTA.setBounds(331,y2,300,300);
           logTA.setBackground(lightBlue);
           add(logTA);
 
           label9=new Label("GANTT CHART");
           label9.setBackground(acolor);
           label9.setBounds(27,ganttline,100,25);
           add(label9);
 
           setVisible(true);
           setLocation(10,10);
           setSize(650,500);
           buffer = createImage(getSize().width, getSize().height);
           gg = buffer.getGraphics();
           repaint();
      }
 
      public void actionPerformed(ActionEvent e)
      {
           if (e.getSource() == runB) {
                if (runB.getLabel().equals("Run")) {
                     if (joblistTA.getText().indexOf("(done)") > -1)
                          redisplayJobs(true);
                     runB.setLabel("Stop");
                     run();
                }
                else {
                     runB.setLabel("Run");
                     stop();
                }
           }
           else if (e.getSource() == resetB) {
                redisplayJobs(true);
                timeTF.setText("0");
                runningTF.setText("");
                timeleftTF.setText("");
                quantumleftTF.setText("");
           }
      }
      public void componentResized(ComponentEvent e) {}
      public void componentHidden(ComponentEvent e) {}
      public void componentMoved(ComponentEvent e) {}
      public void componentShown(ComponentEvent e) {}
 
      public void paint(Graphics g)
      {
           if (buffer == null || gg == null) return;
           gg.setColor(acolor);
           gg.fillRect(0,0,buffer.getWidth(this),buffer.getHeight(this));
           gg.setColor(Color.black);
           gg.drawLine(0,ganttline-2,getWidth(),ganttline-2);
           // Write to gg first
 
           if (gchart != null)
                gchart.paint(gg);
 
           g.drawImage(buffer,0,0,this);
      }
 
      public void update (Graphics g) {
           paint (g);
      }
 
      public void itemStateChanged (ItemEvent e) {
           if (e.getSource() == exampleCH) {
                String temp = exampleCH.getSelectedItem();
                if (temp.equals("Example 1")) 
                     joblistTA.setText("1     20\n"+
                                       "2     80\n"+
                                       "3     35\n"+
                                       "4     60\n");
                else if (temp.equals("Example 2")) 
                     joblistTA.setText("job1  80\n"+
                                       "job2  55\n"+
                                       "job3  20\n"+
                                       "job4  41\n");
                else if (temp.equals("Example from textbook")) 
                     joblistTA.setText("p1  140\n"+
                                       "p2  75\n"+
                                       "p3  320\n"+
                                       "p4  280\n"+
                                       "p5  125\n");
           }
           else if (e.getSource() == speedCH) {
                String temp = speedCH.getSelectedItem();
                if (temp.equals("Crawl"))     sleepTime = 2500;
                if (temp.equals("Very slow")) sleepTime = 1200;
                if (temp.equals("Slow"))      sleepTime = 800;
                if (temp.equals("Medium"))    sleepTime = 300;
                if (temp.equals("Fast"))      sleepTime = 100;
                if (temp.equals("Very fast")) sleepTime = 25;
                if (temp.equals("No delay"))  sleepTime = 0;
           }
           else if (e.getSource() == algoCH) {
                String temp = algoCH.getSelectedItem();
                if (temp.equals("Round Robin")) {
                     quantumTF.setBackground(Color.white);
                     quantumleftTF.setBackground(Color.white);
                     quantumTF.setText(timeQuantum+"");
                     quantumleft = 0;
                }
                else {
                     quantumTF.setBackground(lightGray);
                     quantumleftTF.setBackground(lightGray);
                     quantumTF.setText("");
                     quantumleftTF.setText("");
                }
           }
      }
 
      public void run() {
           makeJobs();
 
           int maxtime = 0;
          // TODO: 2 when run with learnCS.AppletLauncher jobs == null
           for (int i=0; i<jobs.length; i++) 
                maxtime += jobs[i].timeRequired;
           gchart = new GanttChart(jobs.length, /*startx*/5, 
                                   /*starty*/ ganttline+27,
                                   /*bar width*/ 400, maxtime);
 
           timeTF.setText("0");
           logTA.setText("");
           totalTimeInSystem = 0;
           current = -1;
           algo = algoCH.getSelectedItem().charAt(0);
           timeQuantum = U.atoi(quantumTF.getText());
           if (algo == 'R')
                logTA.setText("Length of time quantum = "+timeQuantum);
 
           running = true;
           new Thread() {
                public void run() {
                     timeleftTF.setText("0");
                     changeJobs();        // get the first one started
                     quantumleft = timeQuantum;
                     quantumleftTF.setText(quantumleft+"");
                     int timeThisSlice = 0;
                     while (running && numjobs > 0) {
                          if (jobs[current].timeleft == 0) {
                               numjobs--;
                               gchart.addEntry(jobs[current].id, current, 
                                               timeThisSlice);
                               changeJobs();
                               timeThisSlice = 0;
                               if (numjobs == 0) break;
                               timeleftTF.setText(jobs[current].timeleft+"");
                               quantumleftTF.setText(timeQuantum+"");
                               U.sleep(sleepTime);
                               repaint();
                               continue;
                          }
                          if (algo == 'R' && quantumleft == 0) {
                               gchart.addEntry(jobs[current].id, current, 
                                               timeThisSlice);
                               switchJob();
                               timeThisSlice = 0;
                          }
                          jobs[current].timeleft--;
                          timeThisSlice++;
                          timeleftTF.setText(jobs[current].timeleft+"");
                          if (algo == 'R') {
                               quantumleft--;
                               quantumleftTF.setText(quantumleft+"");
                          }
                          timeTF.setText(""+(getTime()+1));
                          U.sleep(sleepTime);
                          repaint();
                     }
                     double avgtime = totalTimeInSystem / (double)jobs.length;
                     log (jobs.length+" completed.");
 /*
 not shown in book, don't need
                     log ("total time of all processes in system = "+
                           totalTimeInSystem);
                     log ("average time in system = "+avgtime);
 */
                     int totalTimeToComplete = 0;
                     for (int i=0; i<jobs.length; i++)
                          totalTimeToComplete += jobs[i].timeEnded;
                     double avgTurnaroundTime = totalTimeToComplete / 
                               (double)jobs.length;
                     log ("average turnaround time = "+avgTurnaroundTime);
                     joblistTA.select(0,0);
                     runB.setLabel("Run");
                     repaint();
                }
           }.start();
      }
 
      public void stop() {
           running = false;
      }
 
      private void switchJob() {
           jobs[current].state = Job.WAITING;
           while (true) {
                current = (current+1) % jobs.length;
                if (jobs[current].state == Job.WAITING) 
                     break;
           }
           log("switching to process "+jobs[current].id+" at time "+getTime());
           jobs[current].state = Job.RUNNING;
           quantumleft = Math.min(timeQuantum, jobs[current].timeleft);
           runningTF.setText(""+jobs[current].id);
           selectJob(current);
      }
 
      private void changeJobs() {
           algo = algoCH.getSelectedItem().charAt(0);
           if (current > -1) {
                jobs[current].state = Job.DONE;
                log("Finishing process "+jobs[current].id+" at time "+getTime());
                jobs[current].timeEnded = getTime();
                redisplayJobs(false);
           }
           if (numjobs == 0) return;
           switch (algo) {
                case 'R': while (true) {
                               current++;
                               if (current >= jobs.length) current = 0;
                               if (jobs[current].state == Job.WAITING) break;
                          }
                          break;
                case 'F': for (int i=0; i<jobs.length; i++) {
                               if (jobs[i].state == Job.WAITING) {
                                    current = i;
                                    break;
                               }
                          }
                          break;
                case 'S': int smallest = 1000000;
                          int smallesti = -1;
                          for (int i=0; i<jobs.length; i++) {
                               if (jobs[i].state == Job.WAITING) {
                                    if (jobs[i].timeRequired < smallest) {
                                         smallest = jobs[i].timeRequired;
                                         smallesti = i;
                                    }
                               }
                          }
                          if (smallesti != -1)
                               current = smallesti;
                          break;
           }
           selectJob(current);
           log("Starting process "+jobs[current].id+" at time "+ getTime());
           totalTimeInSystem += getTime();
           jobs[current].state = Job.RUNNING;
           quantumleft = Math.min(timeQuantum, jobs[current].timeleft);
           runningTF.setText(""+jobs[current].id);
      }
 
      private void redisplayJobs(boolean reset) {
           String news = "";
           for (int i=0; i<jobs.length; i++) {
                news += jobs[i].id+"   "+jobs[i].timeRequired;
                if (!reset)
                     if (jobs[i].state == Job.DONE) 
                          news += "(done)";
                if (i < jobs.length-1)
                     news += "\n";
           }
           joblistTA.setText(news);
      }
 
      private int getTime() {
           return U.atoi(timeTF.getText());
      }
 
      private void log (String s) {
           String olds = logTA.getText();
           if (olds.length() == 0)
                logTA.setText(s);
           else
                logTA.setText(logTA.getText()+"\n"+s);
           logTA.select(logTA.getText().length(),logTA.getText().length());
      }
 
      private void selectJob (int current) {
           int newline = 0;
           int start = -1, end = 0;
 
           String s = joblistTA.getText();
           if (!s.endsWith("\n")) 
                joblistTA.setText(s+"\n");
 
           if (current == 0) 
                start = 0;
 
           for (int i=0; i<joblistTA.getText().length(); i++) {
                char ch = joblistTA.getText().charAt(i);
                if (ch == '\n') {
                     newline++;
                     if (start > -1) {
                          end = i+1;
                          break;
                     }
                     if (newline == current)
                          start = i+1;
                }
           }
 
           joblistTA.select(start,end);
      }
 
      private void makeJobs() {
           if (joblistTA.getText().trim().length() == 0) {
                jobs = null;
                return;
           }
 
           // This initial code gets rid of any ASCII 13's that might be
           // lurking in the text.  I discovered that when you manually
           // type numbers into a textarea, pressing return after each
           // number, Windows puts ASCII 13 plus a newline there!  Yuck!
 
           String s = joblistTA.getText();
           String news = "";
           for (int i=0; i<s.length(); i++) {
                char ch = s.charAt(i);
                if (ch != 13) news = news + ch;
           }
           joblistTA.setText(news);    // change to new version so highlight
                                       // will work properly
 
           StringTokenizer st = new StringTokenizer(joblistTA.getText(), "\n");
           numjobs = st.countTokens();
           jobs = new Job[numjobs];
 
           for (int i=0; i<numjobs; i++) {
                String line = st.nextToken();
                jobs[i] = new Job(line);
           }
      }
 }
 
 class Job {
 //   int id;
      String id;
      int timeRequired;
      int timeStarted;
      int timeEnded;
      int timeleft;
 
      int state;
      final static int DONE = 0;
      final static int WAITING = 1;
      final static int RUNNING = 2;
 
      public Job (String s) {
           StringTokenizer st = new StringTokenizer(s, " ");
           if (st.countTokens() != 2) {
                new Popup("Illegal job description! "+s+"\n"+
                            "You must put the id followed by time:\n"+
                            "  e.g.    3  150\n"+
                            "  or    job17 150");
                id = "null";         // defaults!
                timeRequired = 0;
                return;
           }
           id = st.nextToken();
           timeRequired = U.atoi(st.nextToken());
           timeleft = timeRequired;
           state = WAITING;
      }
 }
 
 class GanttChart {
      String[] jobnames;
      Color[] colors;
      int[] lengths;
      int numentries = 0;
 
      final static int MAXENTRIES = 1000;
 
      Color[] colorTranslations;
 
      int startx, starty, width, totaltime;
 
      public GanttChart(int numjobs, int startx, int starty, int width,
                        int totaltime) {
           this.startx = startx;
           this.starty = starty;
           this.width = width;
           this.totaltime = totaltime;
 
           if (numjobs > 12) numjobs = 12;
           colorTranslations = new Color[12];
           colorTranslations[0] = Color.red;
           colorTranslations[1] = Color.blue;
           colorTranslations[2] = Color.yellow;
           colorTranslations[3] = Color.green;
           colorTranslations[4] = Color.cyan;
           colorTranslations[5] = Color.magenta;
           colorTranslations[6] = Color.orange;
           colorTranslations[7] = Color.pink;
           colorTranslations[8] = Color.gray;
           colorTranslations[9] = new Color(250,110,75);
           colorTranslations[10] = new Color(85,200,37);
           colorTranslations[11] = new Color(100,120,26);
 
           jobnames = new String[MAXENTRIES];
           colors = new Color[MAXENTRIES];
           lengths = new int[MAXENTRIES];
      }
 
      public void reset() {
           numentries = 0;
      }
 
      public void addEntry (String jobname, int jobnum, int time) {
           if (numentries >= MAXENTRIES) return;
           jobnames[numentries] = jobname;
           lengths[numentries] = time;
           colors[numentries] = colorTranslations[jobnum];
           numentries++;
      }
 
      public void paint (Graphics g) {
           int x = startx;
           for (int i=0; i<numentries; i++) {
                int widthToPaint = (int)(lengths[i]/(double)totaltime * width);
                Color oldcolor = g.getColor();
                g.setColor(colors[i]);
                g.fillRect(x,starty,widthToPaint,10);
                g.setColor(Color.black);
                g.drawString(jobnames[i],x+1,starty+20);
                g.setColor(oldcolor);
                x += widthToPaint;
           }
      }
 }
