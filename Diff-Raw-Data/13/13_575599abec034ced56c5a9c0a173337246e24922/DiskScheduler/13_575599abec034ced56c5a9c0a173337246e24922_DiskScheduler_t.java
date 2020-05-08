 package org.softwarehelps.learncs.DISKSCHED;
 
 import java.util.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.applet.*;
 
 public class DiskScheduler extends Applet implements ActionListener
 {
      Button runB, stopB, exampleB, addB;
      Choice algoCH;
      TextArea requestsTA;
      TextField addTF;
      Label Label1;
 
      Image buffer;
      Graphics gg;
 
      int[] requests = new int[105];              // 100 places + margin of 5
      int numrequests = 0;
 
      Color acolor = new Color(198,214,244);
 
      boolean running = false;
 
      public void init() {
           setLayout(null);
 
           Label lab = new Label("Disk Scheduling");
           lab.setBounds(125,5,380,38);
           lab.setFont (new Font("SansSerif", Font.BOLD, 36));
           lab.setBackground(acolor);
           add(lab);
 
           Label1=new Label("Disk Tracks");
           Label1.setBackground(acolor);
           Label1.setBounds(30,65,80,24);
           add(Label1);
 
 
           int y = 10 + lab.getSize().height;
 
           int x = 160;
 
           Label1=new Label("Requests:");
           Label1.setBackground(acolor);
           Label1.setBounds(x,65,116,24);
           add(Label1);
 
           requestsTA=new TextArea(5,40);
           requestsTA.setBounds(x,92,114,351);
           requestsTA.setBackground(Color.white);
           add(requestsTA);
 
           y = 180;
 
           x = 300;
 
           Label1=new Label("Algorithm:");
           Label1.setBackground(acolor);
           Label1.setBounds(x,65,116,24);
           add(Label1);
 
           algoCH=new Choice();
           algoCH.addItem("First-Come First-Served");
           algoCH.addItem("Shortest Seek Time First");
           algoCH.addItem("SCAN Disk (elevator)");
           algoCH.setBounds(x,90,157,26);
           add(algoCH);
 
           runB=new Button("Run");
           runB.addActionListener(this);
           runB.setBounds(x,y,67,26);
           add(runB);
           x += 2 + runB.getSize().width;
 
           stopB=new Button("Stop");
           stopB.addActionListener(this);
           stopB.setBounds(x,y,67,26);
           add(stopB);
           y += 5 + stopB.getSize().height;
           x = runB.getLocation().x;
 
           exampleB=new Button("Example");
           exampleB.addActionListener(this);
           exampleB.setBounds(x,y,136,26);
           add(exampleB);
           y += 5 + exampleB.getSize().height;
 
           addB=new Button("Add");
           addB.addActionListener(this);
           addB.setBounds(x,y,50,26);
           add(addB);
           x += 5 + addB.getSize().width;
 
           addTF = new TextField(5);
           addTF.addActionListener(this);
           addTF.setBounds(x,y,60,26);
           add(addTF);
 
           setBackground(acolor);
           setVisible(true);
           setLocation(10,10);
           setSize(650,500);
           buffer = createImage(getSize().width, getSize().height);
           gg = buffer.getGraphics();
           repaint();
      }
 
      public void actionPerformed(ActionEvent e)
      {
           if (e.getSource() == runB)
                run();
           else if (e.getSource() == stopB)
                stop();
           else if (e.getSource() == exampleB)
                makeExample();
           else if (e.getSource() == addB || e.getSource() == addTF) {
                addDynamically(U.atoi(addTF.getText()));
                addTF.setText("");
           }
      }
 
      public void paint(Graphics g)
      {
           if (buffer == null || gg == null) return;
           gg.setColor(acolor);
           gg.fillRect(0,0,buffer.getWidth(this),buffer.getHeight(this));
           paintDisk(gg);
           g.drawImage(buffer,0,0,this);
      }
 
      public void update (Graphics g) {
           paint (g);
      }
 
      int currenttrack = 50;      // where the read/write head is now
      int trackToPaint = 50;      // changes slow when moving between tracks!
 
      final static int DOWN = 1;
      final static int UP   = -1;
      int direction = DOWN;
 
      char algo;              // 'F' = first come first served
                              // 'S' = shortest seek time first
                              // 'E' = elevator
 
      int startx = 50;
      int starty = 100;
      int length = 300;
      int width = 25;
      int indicatorHeight = 5;
      int mustDelete = -1;
 
      private void paintDisk(Graphics g) {
           int x = 50;
  
           g.setColor(Color.black);
           g.drawLine(startx, starty, startx, starty+length);
           g.drawLine(startx+width, starty, startx+width, starty+length);
 
           g.drawLine(startx, starty, startx+width, starty);
           g.drawLine(startx, starty+length, startx+width, starty+length);
 
           int y = translatePosition(0);
           g.drawString ("0", startx-20, y+7);
           y = translatePosition(99);
           g.drawString ("99", startx-20, y+7);
 
           g.setColor(Color.yellow);
           g.fillRect(startx+1, starty+1, width-1, length-1);
 
           g.setColor(Color.black);
           if (!running) return;
 
           for (int i=0; i<numrequests; i++) {
                if (requests[i] == -1) continue;
                y = translatePosition (requests[i]);
                g.drawString (""+requests[i], startx-20, y+7);
           }
           
           y = translatePosition (trackToPaint);
           if (y < indicatorHeight)
                y = indicatorHeight;
 
           g.setColor(Color.red);
           g.fillRect(startx, y, width, indicatorHeight);
      }
 
      private int translatePosition (int tracknum) {
           double position = tracknum / 100.0;
           return (int)(position * length) + starty;
      }
 
      private void makeExample() {
           requestsTA.setText("19\n4\n21\n3\n97\n40\n50\n62\n10\n86\n2");
      }
 
      private void addDynamically(int n) {
           if (n < 0 || n > 99) return;
           if (requestsTA.getText().length() == 0) {
                requestsTA.setText(""+n);
           }
           else
                requestsTA.setText(requestsTA.getText() + "\n" + n);
           requests[numrequests++] = n;
      }
 
      private void moveTo(int tracknumber) {
           int xtrack = currenttrack;         
           int increment = 1;
           if (tracknumber < currenttrack)
                increment = -increment;
 
           while (abs(xtrack - tracknumber) > 5) {
                xtrack += increment;
                trackToPaint = xtrack;
                repaint();
                Thread.yield();
                U.sleep(75);
           }
 
           currenttrack = tracknumber;
           trackToPaint = currenttrack;
           repaint();
           requests[mustDelete] = -1;          // retire this one
           U.sleep(500);
      }
 
      public void run() {
           makeRequests();
           repaint();
           running = true;
           currenttrack = 50;
           String temp = algoCH.getSelectedItem();
           if (temp.startsWith("First"))
                algo = 'F';
           else if (temp.startsWith("Shortest"))
                algo = 'S';
           else if (temp.startsWith("SCAN"))
                algo = 'E';
           new Thread() {
                public void run() {
                     while (running) {
                          int nextspot = selectNextRequest();
                          if (nextspot == -1) break;
                          moveTo(nextspot);
                     }
                     requestsTA.select(0,0);
                     repaint();
                }
           }.start();
      }
 
      public void stop() {
           running = false;
      }
 
      private int selectNextRequest() {
           if (algo == 'F') {
                int foundRequest = -1;
                for (int i=0; i<numrequests; i++) {
                     if (requests[i] == -1) continue;
                     foundRequest = requests[i];
                     highlight(i);
                     mustDelete = i;
                     break;
                }
                return foundRequest;
           }
           if (algo == 'S') {
                int foundRequest = -1;
                int smallestdistance = 1000000;
                int nearesti = -1;
                int nearest = 0;
                for (int i=0; i<numrequests; i++) {
                     if (requests[i] == -1) continue;
                     int distance = abs(currenttrack - requests[i]);
                     if (distance < smallestdistance) {
                          smallestdistance = distance;
                          nearest = requests[i];
                          nearesti = i;
                     }
                }
                if (nearesti == -1)
                     return -1;
                foundRequest = nearest;
                highlight(nearest);
                mustDelete = nearesti;
                return foundRequest;
           }
           if (algo == 'E') {
                int foundRequest = -1;
                for (int k=0; k<2; k++) {
                     int smallestdistance = 1000000;
                     int nearesti = -1;
                     int nearest = 0;
                     for (int i=0; i<numrequests; i++) {
                          if (requests[i] == -1) continue;
                          int distance = direction * (currenttrack - requests[i]);
                          if (distance >= 0 && distance < smallestdistance) {
                               smallestdistance = distance;
                               nearest = requests[i];
                               nearesti = i;
                          }
                     }
 
                     if (nearesti == -1) 
                          direction = direction * -1;
                     else {
                          foundRequest = nearest;
                          highlight(nearest);
                          mustDelete = nearesti;
                          return foundRequest;
                     }
                }
           }
           return -1;
      }
 
      private void makeRequests() {
           // This initial code gets rid of any ASCII 13's that might be
           // lurking in the text.  I discovered that when you manually
           // type numbers into a textarea, pressing return after each
           // number, Windows puts ASCII 13 plus a newline there!  Yuck!
 
           String s = requestsTA.getText();
           String news = "";
           for (int i=0; i<s.length(); i++) {
                char ch = s.charAt(i);
                if (ch != 13) news = news + ch;
           }
           requestsTA.setText(news);   // change to new version so highlight
                                       // will work properly
 
           StringTokenizer st = new StringTokenizer(news, "\n");
           numrequests = st.countTokens();
 
           for (int i=0; i<numrequests; i++) {
                String temp = st.nextToken();
                requests[i] = U.atoi(temp);
           }
      }
 
      private static int abs(int n) { 
           if (n < 0)
                return -n;
           return n;
      }
 
     // TODO: may not be working correctly, it seems to just hide text rather highlight it
      private void highlight (int n) {
           String s = requestsTA.getText();
           int newline = 0;
           int begin = -1, end = -1;
           if (n == 0)
                begin = 0;
           for (int i=0; i<s.length(); i++) {
                char ch = s.charAt(i);
                if (ch == '\n') {
                     newline++;
                     if (begin >= 0) {
                          end = i;
                          break;
                     }
                     if (newline == n)
                          begin = i+1;
                }
           }
           if (end == -1)
                end = s.length();
           if (begin > -1)
                requestsTA.select(begin, end);
           repaint();
      }
 }
