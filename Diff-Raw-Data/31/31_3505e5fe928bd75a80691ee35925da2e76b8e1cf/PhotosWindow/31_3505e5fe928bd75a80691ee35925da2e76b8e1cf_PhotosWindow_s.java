 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package hy360;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.SQLException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.*;
 import databases.*;
 import java.sql.ResultSet;
 
 /**
  *
  * @author Ras
  */
 public class PhotosWindow {
     
     public JFrame photos_frame;
     
     public JRadioButton all_categories;
     public JRadioButton categories;
     public JCheckBox landscape;
     public JCheckBox portrait;
     public JCheckBox other;
     public JRadioButton all_type;
     public JRadioButton bnw;
     public JRadioButton color;
     public JRadioButton all_av;
     public JRadioButton av;
     public JRadioButton not_av;
     public JRadioButton all_size;
     public JRadioButton fixed;
     public JRadioButton not_fixed;
     JTextArea photos_display;
     public JTextField fixed_x_txt;
     public JTextField fixed_y_txt;
     public JTextField not_fixed_x1_txt;
     public JTextField not_fixed_x2_txt;
     public JTextField not_fixed_y1_txt;
     public JTextField not_fixed_y2_txt;
     public JTextField place_txt;
     public JTextField person_txt;
     public JCheckBox p1;
     public JCheckBox p2;
     public JCheckBox p3;
     public JCheckBox p4;
     public JCheckBox p5;
     public JCheckBox p6;
     public JTextField buy_txt;
     public JLabel buy_label;
     
     
     Databases myDatabase;
     ResultSet rs=null;
     public PhotosWindow(Databases data){
         myDatabase=data;
         photos_frame = new JFrame("Only Photos");
         photos_frame.setSize(1200, 700);
         photos_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
         /* Creating the menu bar */
         JMenuBar menu_bar = new JMenuBar();
         
         /* Creating the drop downs */
         JMenu options = new JMenu("Options");
         menu_bar.add(options);
         JMenu search = new JMenu("Search");
         menu_bar.add(search);
         JMenu info = new JMenu("Info");
         menu_bar.add(info);
         
         /* Filling File option */
         JMenuItem view_profile = new JMenuItem("View my profile");
         view_profile.addActionListener(new ViewProfileListener());
         options.add(view_profile);
         JSeparator file_sep = new JSeparator();
         options.add(file_sep);
         JMenuItem logout = new JMenuItem("Log Out");
         logout.addActionListener(new LogOutListener());
         options.add(logout);
         
         /* Filling Search option */
         JMenuItem search_photographers = new JMenuItem("Search Photographers");
         search_photographers.addActionListener(new PhotographersListener());
         search.add(search_photographers);
         
         /* Filling Info option */
         JMenuItem about = new JMenuItem("Top 10");
         about.addActionListener(new Top10Listener());
         info.add(about);
         
         /* Adding bar to main window */
         photos_frame.setJMenuBar(menu_bar);
         
         /* Photos Panel */
         Panel photos_panel = new Panel();
         photos_panel.setLayout(null);
         
         all_categories = new JRadioButton("All");
         all_categories.setSelected(true);
         all_categories.setBounds(10, 20, 50, 25);
         photos_panel.add(all_categories);
         
         categories = new JRadioButton("Categories");
         categories.setBounds(70, 20, 100, 25);
         photos_panel.add(categories);
         
         ButtonGroup category_group = new ButtonGroup();
         category_group.add(all_categories);
         category_group.add(categories);
         
         landscape = new JCheckBox("Landscape");
         landscape.setSelected(true);
         landscape.setBounds(70, 50, 100, 25);
         photos_panel.add(landscape);
         
         portrait = new JCheckBox("Portrait");
         portrait.setBounds(70, 80, 100, 25);
         photos_panel.add(portrait);
         
         other = new JCheckBox("Other");
         other.setBounds(70, 110, 100, 25);
         photos_panel.add(other);
         
         JLabel type = new JLabel("Type");
         type.setBounds(10, 175, 100, 25);
         photos_panel.add(type);
         
         all_type = new JRadioButton("All");
         all_type.setSelected(true);
         all_type.setBounds(70, 145, 100, 25);
         photos_panel.add(all_type);
         
         bnw = new JRadioButton("Black & White");
         bnw.setBounds(70, 175, 150, 25);
         photos_panel.add(bnw);
         
         color = new JRadioButton("Color");
         color.setBounds(70, 205, 100, 25);
         photos_panel.add(color);
         
         ButtonGroup type_group = new ButtonGroup();
         type_group.add(all_type);
         type_group.add(bnw);
         type_group.add(color);
         
         JLabel available = new JLabel("Available");
         available.setBounds(10, 270, 100, 25);
         photos_panel.add(available);
         
         all_av = new JRadioButton("All");
         all_av.setSelected(true);
         all_av.setBounds(70, 240, 100, 25);
         photos_panel.add(all_av);
         
         av = new JRadioButton("Yes");
         av.setBounds(70, 270, 100, 25);
         photos_panel.add(av);
         
         not_av = new JRadioButton("No");
         not_av.setBounds(70, 300, 100, 25);
         photos_panel.add(not_av);
         
         ButtonGroup av_group = new ButtonGroup();
         av_group.add(all_av);
         av_group.add(av);
         av_group.add(not_av);
         
         JLabel size = new JLabel("Size");
         size.setBounds(10, 350, 100, 25);
         photos_panel.add(size);
         
         all_size = new JRadioButton("All");
         all_size.setSelected(true);
         all_size.setBounds(70, 335, 100, 25);
         photos_panel.add(all_size);
         
         fixed = new JRadioButton("Fixed");
         fixed.setBounds(70, 365, 100, 25);
         photos_panel.add(fixed);
         
         fixed_x_txt = new JTextField(20);
         fixed_x_txt.setBounds(170, 365, 50, 25);
         photos_panel.add(fixed_x_txt);
         
         JLabel x1 = new JLabel("X");
         x1.setBounds(225, 365, 25, 25);
         photos_panel.add(x1);
         
         fixed_y_txt = new JTextField(20);
         fixed_y_txt.setBounds(240, 365, 50, 25);
         photos_panel.add(fixed_y_txt);
         
         not_fixed = new JRadioButton("Variable");
         not_fixed.setBounds(70, 395, 100, 25);
         photos_panel.add(not_fixed);
         
         not_fixed_x1_txt = new JTextField(20);
         not_fixed_x1_txt.setBounds(170, 395, 50, 25);
         photos_panel.add(not_fixed_x1_txt);
         
         JLabel x2 = new JLabel("X");
         x2.setBounds(225, 395, 25, 25);
         photos_panel.add(x2);
         
         not_fixed_x2_txt = new JTextField(20);
         not_fixed_x2_txt.setBounds(240, 395, 50, 25);
         photos_panel.add(not_fixed_x2_txt);
         
         JLabel size_sep = new JLabel("-");
         size_sep.setBounds(305, 395, 25, 25);
         photos_panel.add(size_sep);
         
         not_fixed_y1_txt = new JTextField(20);
         not_fixed_y1_txt.setBounds(325, 395, 50, 25);
         photos_panel.add(not_fixed_y1_txt);
         
         JLabel x3 = new JLabel("X");
         x3.setBounds(380, 3950, 50, 25);
         photos_panel.add(x3);
         
         not_fixed_y2_txt = new JTextField(20);
         not_fixed_y2_txt.setBounds(395, 395, 50, 25);
         photos_panel.add(not_fixed_y2_txt);
         
         ButtonGroup size_group = new ButtonGroup();
         size_group.add(all_size);
         size_group.add(fixed);
         size_group.add(not_fixed);
         
         JLabel place = new JLabel("Place");
         place.setBounds(10, 440, 100, 25);
         photos_panel.add(place);
         
         place_txt = new JTextField(100);
         place_txt.setBounds(70, 440, 100, 25);
         photos_panel.add(place_txt);
         
         JLabel person = new JLabel("Person");
         person.setBounds(10, 475, 100, 25);
         photos_panel.add(person);
         
         person_txt = new JTextField(100);
         person_txt.setBounds(70, 475, 100, 25);
         photos_panel.add(person_txt);
         
         JLabel price = new JLabel("Price");
         price.setBounds(10, 510, 100, 25);
         photos_panel.add(price);
         
         p1 = new JCheckBox("0 - 20");
         p1.setSelected(true);
         p1.setBounds(70, 510, 70 ,25);
         photos_panel.add(p1);
         
         p2 = new JCheckBox("20 - 40");
         p2.setBounds(140, 510, 70, 25);
         photos_panel.add(p2);
         
         p3 = new JCheckBox("40 - 60");
         p3.setBounds(220, 510, 70, 25);
         photos_panel.add(p3);
         
         p4 = new JCheckBox("60 - 80");
         p4.setBounds(290, 510, 70, 25);
         photos_panel.add(p4);
         
         p5 = new JCheckBox("80 - 100");
         p5.setBounds(360, 510, 70, 25);
         photos_panel.add(p5);
         
         p6 = new JCheckBox("100 + ");
         p6.setBounds(430, 510, 70, 25);
         photos_panel.add(p6);
         
         JButton photos_search = new JButton("Search");
         photos_search.addActionListener(new PhotoSearchListener());
         photos_search.setBounds(10, 555, 80, 25);
         photos_panel.add(photos_search);
         
         JSeparator photos_separator = new JSeparator(JSeparator.VERTICAL);
         photos_separator.setBounds(600,0, 1, 700);
         photos_panel.add(photos_separator);
         
         photos_display = new JTextArea(200,500);
         photos_display.setEditable(false);
         JScrollPane photos_scroll = new JScrollPane(photos_display);
         photos_scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
         photos_scroll.setBounds(610, 20, 570, 500);
         photos_panel.add(photos_scroll);
         
         JButton buy = new JButton("Buy");
         buy.addActionListener(new BuyListener());
         buy.setBounds(610, 535, 80, 25);
         photos_panel.add(buy);
         
         buy_label = new JLabel("Select a photo id to buy");
         buy_label.setBounds(700, 535, 200, 25);
         photos_panel.add(buy_label);
         
         buy_txt = new JTextField(100);
         buy_txt.setBounds(610, 570, 100, 25);
         photos_panel.add(buy_txt);
         
         /* Start window */
         photos_frame.add(photos_panel);
         photos_frame.setVisible(true);
     }
     
 private class ViewProfileListener implements ActionListener{
 
         @Override
         public void actionPerformed(ActionEvent e) {
             photos_frame.dispose();
             if(myDatabase.currentId == 1)
             {
                 AdminWindow admin_window = new AdminWindow(myDatabase);
             }
             else
             {
                 ProfileWindow profile_window = new ProfileWindow(myDatabase);
             }
         } 
 }
     
 private class LogOutListener implements ActionListener{
 
         @Override
         public void actionPerformed(ActionEvent e) {
             photos_frame.dispose();
             myDatabase.currentId = -1;
             LoginWindow login_window = new LoginWindow(myDatabase);
         } 
 }
     
 private class Top10Listener implements ActionListener{
 
         @Override
         public void actionPerformed(ActionEvent e) {
             photos_frame.dispose();
             Top10Window top10_window = new Top10Window(myDatabase);
         }  
 }
 
 private class PhotographersListener implements ActionListener{
 
         @Override
         public void actionPerformed(ActionEvent e) {
             photos_frame.dispose();
             PhotographersWindow photographers_window = new PhotographersWindow(myDatabase);
         }
     
 }
 
 private class BuyListener implements ActionListener{
 
         @Override
         public void actionPerformed(ActionEvent e) {
             String photo_id = buy_txt.getText();
             //gia ta minimata tou buy -> buy_label.setText();
         }
     
 }
 
 private class PhotoSearchListener implements ActionListener{
 
         @Override
         public void actionPerformed(ActionEvent e) {
             String out="",sql="SELECT * FROM photos WHERE ";
             boolean hasOther=false,hasOr=false;
             int i;
             /* KATIGORIES */
             if((categories.isSelected()==true)&&(landscape.isSelected()==false)&&(portrait.isSelected()==false)&&(other.isSelected()==false)){
                 all_categories.setSelected(true);
             }
             if((categories.isSelected()==true)&&(landscape.isSelected()==true)&&(portrait.isSelected()==true)&&(other.isSelected()==true)){
                 all_categories.setSelected(true);
             }
             if (categories.isSelected() == true)
             {
                 if(((landscape.isSelected()==true)&&(portrait.isSelected()==true))||((portrait.isSelected()==true)&&(other.isSelected()==true))||((landscape.isSelected()==true)&&(other.isSelected()==true))){
                     hasOr=true;
                 }
                 if(hasOr==true){
                     sql+="(";
                 }
                 if(landscape.isSelected() == true)
                 {
                    sql+="Category='Landscape' ";
                    hasOther=true;
                     /*try {
                         rs=myDatabase.executeStatement(sql);
                     } catch (SQLException ex) {
                         Logger.getLogger(PhotosWindow.class.getName()).log(Level.SEVERE, null, ex);
                     }
                     out=constructResult(rs);
                     photos_display.setText(out);*/
                 }
                 if(portrait.isSelected() == true)
                 {
                     
                    if(hasOther==true){
                        sql+="or Category='Portrait' ";
                    } 
                    else{
                        sql+="Category='Portrait' ";
                        hasOther=true;
                    }
                     /*try {
                         rs=myDatabase.executeStatement(sql);
                     } catch (SQLException ex) {
                         Logger.getLogger(PhotosWindow.class.getName()).log(Level.SEVERE, null, ex);
                     }*/
                     //out=constructResult(rs);
                     //photos_display.setText(out);
                 }
                 if(other.isSelected() == true)
                 {
                     
                     if(hasOther==true){
                        sql+="or Category='Other' ";
                    } 
                    else{
                        sql+="Category='Other' ";
                        hasOther=true;
                    }
                     /*try {
                         rs=myDatabase.executeStatement(sql);
                     } catch (SQLException ex) {
                         Logger.getLogger(PhotosWindow.class.getName()).log(Level.SEVERE, null, ex);
                     }*/
                     //out=constructResult(rs);
                     //photos_display.setText(out);
                 }
                 if(hasOr==true){
                     sql+=") ";
                 }
             }
             /* All */
             else
             {
                 if((all_type.isSelected()==true)&&(all_av.isSelected()==true)&&(all_size.isSelected()==true)&&(person_txt.getText().equals(""))&&(place_txt.getText().equals(""))&&(p1.isSelected()==false)&&(p2.isSelected()==false)&&(p3.isSelected()==false)&&(p4.isSelected()==false)&&(p5.isSelected()==false)&&(p6.isSelected()==false)){
                     
                 
                     try {
                         rs=myDatabase.executeStatement("SELECT * FROM photos");
                     } catch (SQLException ex) {
                         Logger.getLogger(PhotosWindow.class.getName()).log(Level.SEVERE, null, ex);
                     }
                     out=constructResult(rs);
                     photos_display.setText(out);
                     return;
                 }
             }
             
             /* COLOR */
             if(all_type.isSelected() == true)
             {
                 //ola ta xromata
             }
             else if(bnw.isSelected() == true)
             {
                 if(hasOther==true){
                     sql+="and Type='Black'";
                 }
                 else{
                     sql+="Type='Black'";
                     hasOther=true;
                 }
                 try {
                     System.out.println(sql);
                     rs=myDatabase.executeStatement(sql);
                 } catch (SQLException ex) {
                     Logger.getLogger(PhotosWindow.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 out=constructResult(rs);
                 photos_display.setText(out);
                 
             }
             else if(color.isSelected() == true)
             {
                 if(hasOther==true){
                     sql+="and Type='Coloured'";
                 }
                 else{
                     sql+="Type='Coloured'";
                     hasOther=true;
                 }
                 try {
                     rs=myDatabase.executeStatement(sql);
                 } catch (SQLException ex) {
                     Logger.getLogger(PhotosWindow.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 out=constructResult(rs);
                 photos_display.setText(out);
             }
             
             /* AVAILABILITY */
             if(all_av.isSelected() == true)
             {
                 
             }
             else if(av.isSelected() == true)
             {
                 if(hasOther==true){
                     sql+="and Availability='1' ";
                 }
                 else{
                     sql+="Availability='1' ";
                     hasOther=true;
                 }
                 try {
                     rs=myDatabase.executeStatement(sql);
                 } catch (SQLException ex) {
                     Logger.getLogger(PhotosWindow.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 out=constructResult(rs);
                 photos_display.setText(out);
             }
             else if(not_av.isSelected() == true)
             {
                 if(hasOther==true){
                     sql+="and Availability='0' ";
                 }
                 else{
                     sql+="Availability='0' ";
                     hasOther=true;
                 }
                 try {
                     rs=myDatabase.executeStatement(sql);
                 } catch (SQLException ex) {
                     Logger.getLogger(PhotosWindow.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 out=constructResult(rs);
                 photos_display.setText(out);
             }
             
             /* SIZE */
             if(all_size.isSelected() == true)
             {
                 //ola ta megethi
             }
             else if(fixed.isSelected() == true)
             {
                 
                 if((!(fixed_x_txt.getText().equals(""))&&!(fixed_y_txt.getText().equals("")))){
                 if(hasOther==true){
                     sql+="and Width= "+fixed_x_txt.getText()+" and Height= "+fixed_y_txt.getText()+" ";
                 }
                 else{
                     sql+="Width= "+fixed_x_txt.getText()+" and Height= "+fixed_y_txt.getText()+" ";
                     System.out.println(sql);
                     hasOther=true;
                 }
                 try {
                     rs=myDatabase.executeStatement(sql);
                 } catch (SQLException ex) {
                     Logger.getLogger(PhotosWindow.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 out=constructResult(rs);
                 photos_display.setText(out);
                 }
                 else{return;}
                 //x -> fixed_x_txt.getText();
                 //y -> fixed_y_txt.getText();
             }
             else if(not_fixed.isSelected() == true)
             {
                 //metablito megetheos
                 //X x X -> not_fixed_x1_txt.getText(); not_fixed_x2_txt.getText();
                 // adistixa me y1 y2 gia to apo-mexi tou y
             }
             
             /* PLACE */
             //place_txt.getText();
             
             /* PERSON */
             //person_txt.getText();
             
             /* PRICE */
             if(p1.isSelected() == true)
             {
                 //0-20
             }
             else if(p2.isSelected() == true)
             {
                 //20-40
             }
             else if(p3.isSelected() == true)
             {
                 //40-60
             }
             else if(p4.isSelected() == true)
             {
                 //60-80
             }
             else if(p4.isSelected() == true)
             {
                 //80-100
             }
             else if(p6.isSelected() == true)
             {
                 //100+
             }
         }
     
 }
 private String constructResult(ResultSet r){
     String out="";
         try {
             
             int i;
             while(rs.next()){
                                 for(i=1;i<10;i++){
                                     if(rs.getString(i)!=null){
                                         out+=rs.getString(i)+" ";
                                     }
                             } 
                                 out+="\n";
                          
                         }
             
         } catch (SQLException ex) {
             Logger.getLogger(PhotosWindow.class.getName()).log(Level.SEVERE, null, ex);
         }
         return out;
 }
 }
