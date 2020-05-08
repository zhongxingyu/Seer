 package org.softwarehelps.learncs.SIMPLESQL;
 
 /* This file was automatically generated from a .mac file.*/
 
 import java.util.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.applet.*;
 
 public class SimpleSQL extends Frame
         implements ActionListener, ComponentListener,
                    MouseListener, ItemListener
 {
      TextArea queryTA;
      Label Label2;
      Label Label1;
      Button delB, newB, editB, exampleB, submitB, helpB;
      Choice queryCH;
      Image buffer;
      Graphics gg;
      Color acolor = new Color(246,227,218);
      final static int X=0;
      final static int Y=0;
 
      java.awt.List tablenames;
      Vector tables;
 
      int resultnumber = 0;
 
      public SimpleSQL() {
           setLayout(null);
           setTitle("Simple SQL DataBase System");
 
           Label1=new Label("Simple SQL");
           Label1.setFont (new Font("SansSerif", Font.BOLD, 36));
           Label1.setBackground(acolor);
           Label1.setBounds(X+250,Y+35,200, 35);
           add(Label1);
 
           Label1=new Label("Tables:");
           Label1.setBackground(acolor);
           Label1.setFont(new Font("SansSerif",Font.BOLD,12));
           Label1.setBounds(X+43,Y+75,106,20);
           add(Label1);
 
           tablenames=new java.awt.List(20);
           tablenames.setBackground(Color.white);
           tablenames.setBounds(X+166,Y+79,428,100);
           add(tablenames);
 
           int x = X+170;
 
           int y = Y+185;
 
           newB=new Button("New");
           newB.addActionListener(this);
           newB.setBounds(x,y,76,26);
           add(newB);
           x += newB.getSize().width + 5;
 
           editB=new Button("Edit");
           editB.addActionListener(this);
           editB.setBounds(x,y,76,26);
           add(editB);
           x += editB.getSize().width + 5;
 
           delB=new Button("Delete");
           delB.addActionListener(this);
           delB.setBounds(x,y,76,26);
           add(delB);
           x += delB.getSize().width + 5;
 
           exampleB=new Button("Example tables");
           exampleB.addActionListener(this);
           exampleB.setBounds(x,y,100,26);
           add(exampleB);
           x += exampleB.getSize().width + 5;
 
           helpB=new Button("Help");
           helpB.addActionListener(this);
           helpB.setBounds(x,y,40,26);
           add(helpB);
           x += helpB.getSize().width + 5;
 
           y = exampleB.getLocation().y + exampleB.getSize().height + 8;
 
           Label2=new Label("SQL Query:");
           Label2.setBackground(acolor);
           Label2.setFont(new Font("SansSerif",Font.BOLD,12));
           Label2.setBounds(X+41,y,106,20);
           add(Label2);
 
           queryTA=new TextArea(5,40);
           queryTA.setBackground(Color.white);
           queryTA.setBounds(X+167,y,428,90);
           add(queryTA);
 
           y += queryTA.getSize().height + 5;
 
           x = 200;
           submitB=new Button("Process Query");
           submitB.addActionListener(this);
           submitB.setBounds(x,y,100,26);
           add(submitB);
           x += submitB.getSize().width + 5;
 
 /*
           examplequeryB=new Button("Load Example Query");
           examplequeryB.addActionListener(this);
           examplequeryB.setBounds(x,y,130,26);
           add(examplequeryB);
           x += examplequeryB.getSize().width + 5;
 */
 
           queryCH = new Choice();
           queryCH.addItem(" -- example queries --");
           queryCH.addItem("Example 1");
           queryCH.addItem("Example 2");
           queryCH.addItem("Example 3");
           queryCH.addItem("Example 4");
           queryCH.addItem("Example 5");
           queryCH.addItem("Example 6");
           queryCH.addItem("Example 7");
           queryCH.addItem("Example 8");
           queryCH.addItem("Example 9");
           queryCH.setBounds(x,y,150,26);
           queryCH.addItemListener(this);
           add(queryCH);
           x += queryCH.getSize().width + 5;
 
           tables = new Vector();
 
           setBackground(acolor);
           setVisible(true);
           setLocation(10,10);
           setSize(700,400);
           buffer = createImage(getSize().width, getSize().height);
           gg = buffer.getGraphics();
           repaint();
 
           addWindowListener(
              new WindowAdapter() {
                 public void windowClosing (WindowEvent we) {
                      dispose();
                     // System.exit(1);
                 }
              }
           );
      }
      public void actionPerformed(ActionEvent e)
      {
           if (e.getSource() == delB) {
                String name = tablenames.getSelectedItem();
                tables.removeElement(findTable(name));
                tablenames.remove(name);
           }
           else if (e.getSource() == newB) {
                Table t = new Table();
                EditWindow ew = new EditWindow(this, t, "");
                tables.addElement(t);
           }
           else if (e.getSource() == editB) {
                String oldname = tablenames.getSelectedItem();
                if (oldname == null || oldname.length() == 0) {
                     Table t = new Table();
                     EditWindow ew = new EditWindow(this, t, oldname);
                }
                else {
                     Table t = findTable(oldname);
                     if (t == null) {
                          new Popup("No table named: "+oldname);
                     }
                     else {
                          EditWindow ew = new EditWindow(this, t, oldname);
                     }
                }
           }
           else if (e.getSource() == exampleB) 
                loadExample();
           else if (e.getSource() == submitB) 
                handleQuery();
           else if (e.getSource() == helpB) 
                help();
      }
 
      private void loadExampleQuery() {
           String s = queryCH.getSelectedItem();
           if (s.equals("Example 1"))
                queryTA.setText("select Name, Age from People where BirthPlace = 'Valentine'");
           else if (s.equals("Example 2"))
                queryTA.setText("select * from People where BirthPlace = 'Valentine'");
           else if (s.equals("Example 3"))
                queryTA.setText("select FamousThing from CityInfo");
           else if (s.equals("Example 4"))
                queryTA.setText("People join CityInfo");
           else if (s.equals("Example 5"))
                queryTA.setText("select * from People order by Age");
           else if (s.equals("Example 6"))
                queryTA.setText("select * from People where BirthPlace like '%ee'");
           else if (s.equals("Example 7"))
                queryTA.setText("select People.Name, CityInfo.FamousThing from People, CityInfo\n"+
                          "where People.BirthPlace = CityInfo.City\n"+
                          "and CityInfo.FamousThing = 'cattle' order by People.Name");
           else if (s.equals("Example 8"))
                queryTA.setText("select * from People where Age < 60 and BirthPlace='Valentine'");
           else if (s.equals("Example 9"))
                queryTA.setText("select * from People where Age < 20 or BirthPlace='Valentine'");
      }
 
      public void setTable (Table t, String oldname) {
           if (oldname == null) {
                tablenames.add(t.name);
                tables.addElement(t);
           }
           else if (!oldname.equals(t.name)) {
                try {
                     tablenames.remove(oldname);
                } catch (IllegalArgumentException e) {
                }
                tablenames.add(t.name);
           }
           repaint();
      }
    
      public void itemStateChanged(ItemEvent e) {
           if (e.getSource() == queryCH)
                loadExampleQuery();
      }
 
      public void componentResized(ComponentEvent e) {}
      public void componentHidden(ComponentEvent e) {}
      public void componentMoved(ComponentEvent e) {}
      public void componentShown(ComponentEvent e) {}
      public void mouseEntered(MouseEvent e) {}
      public void mouseExited(MouseEvent e) {}
      public void mousePressed(MouseEvent e) {}
      public void mouseReleased(MouseEvent e) {}
      public void mouseClicked(MouseEvent e) {
           if(!e.isMetaDown())
                if(e.getClickCount() == 2)
                     doubleClick(e);
      }
      public void doubleClick (MouseEvent e) {
      }
      public void paint(Graphics g)
      {
           if (buffer == null || gg == null) return;
           gg.setColor(acolor);
           // Write to gg first
           g.drawImage(buffer,0,0,this);
      }
      public void update (Graphics g) {
           paint (g);
      }
 
      public Table findTable(String name) {
           for (int i=0; i<tables.size(); i++) {
                Table t = (Table)tables.elementAt(i);
                if (t.name.equals(name))
                     return t;
           }
           return null;
      }
 
      public void handleQuery() {
           String query = queryTA.getText();
           if (query.length() == 0) return;
 
           query = U.replaceChar (query, '"', '\'');
           query = U.surroundWith (query, '=', ' ');
           query = U.replacePattern(query, "! =", "!=");
 
           String[] tokens = U.tokenize(query);
 
           if (tokens[1].equals("join")) {
                Table t = Table.join(findTable(tokens[0]), findTable(tokens[2]));
                t.name = "result"+resultnumber;
                resultnumber++;
                tables.addElement(t);
                tablenames.add(t.name);
 //             tablenames.select(t.name);
                return;
           }
 
           if (!tokens[0].equals("select"))
                error ("Query must start with \"select\"");
  
           int k = U.find("from", tokens);
           String[] fieldnames = U.getParts(tokens, 1, k);
           U.cleanCommas(fieldnames);
 
           String[] tablesused;
           int k2 = U.find("where", tokens);
           Table result;
 
           if (k2 == -1) {
                tablesused = U.getParts(tokens, k+1, tokens.length);
                result = simpleProjection(fieldnames, tablesused);
 
                if (result != null) {
                      int k3 = U.find("order", tokens);
                      if (k3 != -1) 
                           result.sort(tokens[k3+2]);
                }
                return;
           }
 
           tablesused = U.getParts(tokens, k+1, k2);
           U.cleanCommas(tablesused);
           
           String[] wherecondition;
           int k3 = U.find("order", tokens);
           if (k3 == -1) 
                wherecondition = U.getParts(tokens, k2+1, tokens.length);
           else
                wherecondition = U.getParts(tokens, k2+1, k3);
 
           result = selectProject(fieldnames, tablesused, wherecondition);
 
           if (result != null) {
                if (k3 != -1) 
                     result.sort(tokens[k3+2]);
           }
      }
 
      private Table simpleProjection(String[] fieldnames, String[] tablesused) {
           Table t = findTable(tablesused[0]);
           if (t == null) {
                error("Couldn't find table \""+tablesused[0]+"\"");
                return null;
           }
 
           if (fieldnames[0].equals("*")) 
                fieldnames = t.fieldnames;
 
           Table t2 = t.copy();
           t2.name = "result"+resultnumber;
           resultnumber++;
           t2.project(fieldnames);
           t2.suppressMultipleRows();
           tables.addElement(t2);
           tablenames.add(t2.name);
           return t2;
      }
 
      private Table selectProject(String[] fieldnames, String[] tablesused, 
                                  String[] wherecondition) {
           Table t;
           if (tablesused.length == 1) {
                t = findTable(tablesused[0]);
                if (t == null) {
                     error("Couldn't find table \""+tablesused[0]+"\"");
                     return null;
                }
                if (fieldnames[0].equals("*")) 
                     fieldnames = t.fieldnames;
           }
           else if (tablesused.length == 2) {
                t = Table.join(findTable(tablesused[0]), 
                               findTable(tablesused[1]));
                // Probably shouldn't use the * operator here!  
                //     Might be too many fields
                if (fieldnames[0].equals("*")) 
                     fieldnames = t.fieldnames;
           }
           else {
                new Popup("You can only mention 1 or 2 table names.");
                return null;
           }
 
           Table t2 = t.copy();
           t2.name = "result"+resultnumber;
           resultnumber++;
           t2.startSaved();
 
           int k = 0;
           String logicalOp = "and";
           while (k+3 <= wherecondition.length) {
                t2.saveRows(wherecondition[k], wherecondition[k+1], 
                            wherecondition[k+2], logicalOp);
                if (k+3 < wherecondition.length) 
                     logicalOp = wherecondition[k+3].toLowerCase();
                k += 4;    // skip over the logical connector ("and", "or")
           }
 
           t2.endSaved();
           t2.compressRows();
           t2.project(fieldnames);
           t2.suppressMultipleRows();
           tables.addElement(t2);
           tablenames.add(t2.name);
           return t2;
      }
 
      public void error (String s) {
           new Popup(s);
      }
 
      public void loadExample() {
           Table t = new Table();
           t.name = "People";
           t.numfields = 3;
           t.numrows = 5;
 
           t.fieldnames[0] = "Name";
           t.types[0] = "String";
           t.contents[0] = "Mark\nSally\nKathy\nDoran\nMadeline";
 
           t.fieldnames[1] = "Age";
           t.types[1] = "Number";
           t.contents[1] = "45\n65\n36\n40\n7";
 
           t.fieldnames[2] = "BirthPlace";
           t.types[2] = "String";
           t.contents[2] = "Valentine\nValentine\nAinsworth\nSan Francisco\nGreeley";
 
           tables.addElement(t);
           tablenames.add(t.name);
 
           // second example
 
           t = new Table();
           t.name = "CityInfo";
           t.numfields = 2;
           t.numrows = 3;
 
           t.fieldnames[0] = "City";
           t.types[0] = "String";
           t.contents[0] = "Valentine\nGreeley\nSan Francisco";
 
           t.fieldnames[1] = "FamousThing";
           t.types[1] = "String";
           t.contents[1] = "cattle\ncattle\nRice-a-Roni";
 
           tables.addElement(t);
           tablenames.add(t.name);
      }
 
      private void help() {
           new Popup ("How to use SIMPLE SQL:\n\n"+
                      "The top area has a list of tables.  To make\n"+
                      "some example tables, click on \"Example tables\".\n"+
                      "To view or change a table, click once on the\n"+
                      "name of the table (it will darken.)  Then click\n"+
                      "on the \"Edit\" button.\n"+
                      "You can also delete a table by first clicking on\n"+
                      "its name, then click the \"Delete\" button.\n"+
                      "\n"+
                      "To submit a query, write the SQL command in the\n"+
                      "lower text area and then click on \"Process query\".\n"+
                      "\n"+
                      "There are a number of example queries that you can\n"+
                      "experiment with and use to remind you of SQL syntax.\n"+
                      "In order for these queries to work, you must first\n"+
                      "click on the \"Example tables\" button so that the\n"+
                      "two tables \"People\" and \"Cityinfo\" are in the\n"+
                      "top list.\n",
 
                      180,100,300,400,acolor);
      }
 }
