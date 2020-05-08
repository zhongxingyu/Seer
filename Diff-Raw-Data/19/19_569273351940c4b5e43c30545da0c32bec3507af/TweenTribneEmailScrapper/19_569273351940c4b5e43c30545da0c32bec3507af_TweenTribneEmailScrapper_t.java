 package com.TweenTribneEmailScrapper;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.util.StringTokenizer;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.HashMap;
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JCheckBox;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 import javax.swing.UIManager;
 import javax.swing.WindowConstants;
 import org.jsoup.Jsoup;
 import org.jsoup.helper.Validate;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 
 import java.io.BufferedReader;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 import org.apache.poi.xssf.usermodel.XSSFWorkbook;
 import org.apache.poi.ss.usermodel.Row;
 
 @SuppressWarnings("serial")
 public class TweenTribneEmailScrapper extends JFrame {
     // Variables declaration
 
     public ArrayList<String> emailLink = new ArrayList<String>();
     private JLabel labelContent, labelUrl, labelFileDest, labelSearch, labelSite, labelType, jLabelTitle, frmName, frmEmail;
     private JTextField jTextField1, jTextField2, jTextField3, jTextField4, jTextField5;
     private JLabel labelKey, labelValue;
     private JTextField key1, value1, key2, value2, key3, value3, key4, value4, key5, value5, key6, value6, key7, value7, key8, value8, key9, value9, key10, value10;
     private JButton jButton1, fetchbtn, exportbtn, backbtn, searchcsvbtn, exportallbtn;
     private JPanel contentPane;
     private JComboBox selectTypeBox, selectsearchUrl;
     private HashMap<String, String> hashMap = new HashMap<String, String>();
     private ArrayList<String> arrayList = new ArrayList<String>();
     // for second panel code starts here
     private JLabel jl1, jl2, jl3, jl4, searchresultlabel, csvreadlabel, alexa_site_url_heading, alexa_site_rank_heading, alexa_page_heading;
     public JLabel email_label[] = new JLabel[12];
     public JLabel alexa_site_url[], alexa_site_rank[];
     private JTextField jtf1, jtf2, jtf3, jtf4, csvreadtextbox;
     private JButton jb1, csvreadbtn;
     public JButton alexa_export_btn = new JButton();
     public JButton exportall_btn = new JButton();
     private JPanel jp1, c_import_panel, exportall_panel;
     public JFrame jf1, jf2, c_import_frame, exportall_frame;
     private JCheckBox[] cbox = new JCheckBox[20];
     public int alexa_csv_count_row;
     String[] email_store = new String[12];
     //for second panel code end
     String fileData = "", typeString = "";
     String nextPage = "";
     String containUrl = "";
     String a = "";
     String url = "";
 
     public TweenTribneEmailScrapper() {
         super();
         create();
         jf2 = this;
         this.setVisible(true);
         this.setSize(500, 500);
     }
 
     private void create() {
         labelContent = new JLabel();
         labelUrl = new JLabel();
         labelFileDest = new JLabel();
         labelSearch = new JLabel();
         labelSite = new JLabel();
         labelType = new JLabel();
         labelKey = new JLabel();
         labelValue = new JLabel();
         frmName = new JLabel("Name");
         frmEmail = new JLabel("E-Mail Address");
         jLabelTitle = new JLabel();
         String course[] = {"select type", "1 level deep", "click to getEmail", "profile", "recursive", "redirect", "1 level deep(post)", "click to getEmail(post)"};
         selectTypeBox = new JComboBox(course);
         selectTypeBox.setBackground(Color.CYAN);
         selectTypeBox.setForeground(Color.blue);
 
         //String searchweburl[] = {"Select url", "http://www.google.com", "http://www.bing.com", "http://www.dogpile.com"};
         String searchweburl[] = {"Select url", "http://www.google.com"};
         selectsearchUrl = new JComboBox(searchweburl);
         //selectsearchUrl.setBackground(Color.CYAN);
         //selectsearchUrl.setForeground(Color.blue);
 
         selectTypeBox.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent paramActionEvent) {
                 String type = (String) selectTypeBox.getSelectedItem();
                 if (type.equalsIgnoreCase("1 level deep(post)")
                         || type.equalsIgnoreCase("click to getEmail(post)")) {
                     key1.setVisible(true);
                     value1.setVisible(true);
                     key2.setVisible(true);
                     value2.setVisible(true);
                     key3.setVisible(true);
                     value3.setVisible(true);
                     key4.setVisible(true);
                     value4.setVisible(true);
                     key5.setVisible(true);
                     value5.setVisible(true);
                     key6.setVisible(true);
                     value6.setVisible(true);
                     key7.setVisible(true);
                     value7.setVisible(true);
                     key8.setVisible(true);
                     value8.setVisible(true);
                     key9.setVisible(true);
                     value9.setVisible(true);
                     key10.setVisible(true);
                     value10.setVisible(true);
                     labelKey.setVisible(true);
                     labelValue.setVisible(true);
                 } else {
                     key1.setVisible(false);
                     value1.setVisible(false);
                     key2.setVisible(false);
                     value2.setVisible(false);
                     key3.setVisible(false);
                     value3.setVisible(false);
                     key4.setVisible(false);
                     value4.setVisible(false);
                     key5.setVisible(false);
                     value5.setVisible(false);
                     key6.setVisible(false);
                     value6.setVisible(false);
                     key7.setVisible(false);
                     value7.setVisible(false);
                     key8.setVisible(false);
                     value8.setVisible(false);
                     key9.setVisible(false);
                     value9.setVisible(false);
                     key10.setVisible(false);
                     value10.setVisible(false);
                     labelKey.setVisible(false);
                     labelValue.setVisible(false);
                 }
             }
         });
 
         //---start
 
         jl1 = new JLabel("Value 1 ");
         jl2 = new JLabel("Value 2 ");
         jl3 = new JLabel("Value 3 ");
         jl4 = new JLabel("Value 4 ");
         jtf1 = new JTextField(10);
         jtf2 = new JTextField(10);
         jtf3 = new JTextField(10);
         jtf4 = new JTextField(10);
         jb1 = new JButton("Press Me");
         jp1 = new JPanel();
         //---end
         jTextField1 = new JTextField();
         jTextField2 = new JTextField();
         jTextField3 = new JTextField();
         jTextField4 = new JTextField();
         jTextField5 = new JTextField();
         key1 = new JTextField();
         value1 = new JTextField();
         key2 = new JTextField();
         value2 = new JTextField();
         key3 = new JTextField();
         value3 = new JTextField();
         key4 = new JTextField();
         value4 = new JTextField();
         key5 = new JTextField();
         value5 = new JTextField();
         key6 = new JTextField();
         value6 = new JTextField();
         key7 = new JTextField();
         value7 = new JTextField();
         key8 = new JTextField();
         value8 = new JTextField();
         key9 = new JTextField();
         value9 = new JTextField();
         key10 = new JTextField();
         value10 = new JTextField();
 
         jButton1 = new JButton();
         contentPane = (JPanel) this.getContentPane();
         
         searchcsvbtn = new JButton();
         contentPane = (JPanel) this.getContentPane();
 
         labelUrl.setHorizontalAlignment(SwingConstants.LEFT);
         //labelUrl.setForeground(new Color(0, 0, 255));
         labelUrl.setText("Select URL:");
 
         labelSearch.setHorizontalAlignment(SwingConstants.LEFT);
         //labelSearch.setForeground(new Color(0, 0, 255));
         labelSearch.setText("Search String:");
 
 
 
         jLabelTitle.setHorizontalAlignment(SwingConstants.CENTER);
         //jLabelTitle.setForeground(Color.MAGENTA);
         jLabelTitle.setText("Ebizon Email Scraper");
         jLabelTitle.setFont(new Font("Serif", Font.BOLD, 18));
 
 
         jTextField2.setForeground(new Color(0, 0, 255));
         jTextField2.setToolTipText("Enter your URL");
 
         //jButton1.setBackground(new Color(204, 204, 204));
         //jButton1.setForeground(new Color(0, 0, 255));
         jButton1.setText("Enter");
         
         //searchcsvbtn.setBackground(new Color(204, 204, 204));
         //searchcsvbtn.setForeground(new Color(0, 0, 255));
         searchcsvbtn.setText("Export alexa ranks");
         
         
         jButton1.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 jButton1_actionPerformed(e);
 
             }
         });
         //Action listner of searchcsvbtn.
         searchcsvbtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 searchcsvbtn_actionPerformed(e);
 
             }
         });
         
         //Create the object of export all button.
         exportallbtn = new JButton();
         exportallbtn.setText("Export All detail");
         //export_all_btn.setBackground(new Color(204, 204, 204));
         //export_all_btn.setForeground(new Color(0, 0, 255));
         //Action listner of searchcsvbtn.
         exportallbtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 exportallbtn_actionPerformed(e);
             }
         });
         
         // Implementation of contentPane.
         contentPane.setLayout(null);
         contentPane.setBorder(BorderFactory.createEtchedBorder());
         contentPane.setBackground(new Color(204, 204, 204));
         addComponent(contentPane, jLabelTitle, 135, 5, 206, 35);
         addComponent(contentPane, labelUrl, 65, 77, 97, 18);
         addComponent(contentPane, labelSearch, 65, 150, 130, 18);
         addComponent(contentPane, selectsearchUrl, 185, 75, 183, 22);
         addComponent(contentPane, jTextField4, 185, 145, 183, 22);
         addComponent(contentPane, jButton1, 195, 200, 83, 28);
         addComponent(contentPane, searchcsvbtn, 170, 350, 175, 28);
         addComponent(contentPane, exportallbtn, 170, 400, 175, 28);
 
         this.setTitle("Java Scraping Tool");
         this.setLocation(new Point(300, 30));
         this.setSize(new Dimension(700, 700));
         this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         this.setResizable(false);
     }
 
     /**
      * Add Component Without a Layout Manager (Absolute Positioning)
      */
     private void addComponent(Container container, Component c, int x, int y, int width, int height) {
         c.setBounds(x, y, width, height);
         container.add(c);
     }
 
     private void jButton1_actionPerformed(ActionEvent e) {
         url = (String) selectsearchUrl.getSelectedItem();
         String searchstring = new String(jTextField4.getText());
         searchstring += " (site:linkedin.com/pub OR site:linkedin.com/in) -\"pub/dir\"";
         String web_url = null;
         if (url.equals("http://www.google.com")) {
             web_url = url + "/search?q=";
         }
         if (url.equals("http://www.dogpile.com")) {
             web_url = url + "/info.dogpl/search?q=";
         }
         String final_serachurl = web_url + URLEncoder.encode(searchstring);
         //String final_serachurl = url + "/search?q=" + URLEncoder.encode(searchstring);
         //JOptionPane.showMessageDialog(null, searchstring);
         String serach_key = new String(jTextField4.getText());
         String searchstring_validate = new String(jTextField4.getText());
         if (url.equals("Select url") || searchstring_validate.equals("")) {
             jButton1.setEnabled(false);
             JLabel errorFields = new JLabel("<HTML><FONT COLOR = black>You must enter all blank fields.</FONT></HTML>");
             JOptionPane.showMessageDialog(null, errorFields);
             jButton1.setEnabled(true);
             this.setVisible(true);
         } else {
             try {
 
                 int i, total_item = 0, total, serialn;
                 String Cname[] = new String[12];
                 Document doc;
                 //doc = Jsoup.connect("http://google.com/search?q=rajcomics").userAgent("Mozilla").get();
                 doc = Jsoup.connect(final_serachurl).userAgent("Mozilla").timeout(10 * 1000).get();
 
                 Elements links = doc.select("a[href]");
                 System.out.println("Total = " + links.size());
                 total_item = links.size();
                 String name = "";
                 searchresultlabel = new JLabel("Search results of " + serach_key);
                 searchresultlabel.setBounds(150, 5, 300, 25);
                 //Font f1 = new Font(searchresultlabel.getFont().getName(),Font.BOLD,searchresultlabel.getFont().getSize());
                 Font f1 = new Font(searchresultlabel.getFont().getName(), Font.BOLD, 15);
                 searchresultlabel.setFont(f1);
 
                 fetchbtn = new JButton(" Fetch E-Mail ");
                 exportbtn = new JButton(" Export ");
                 backbtn = new JButton(" Back ");
                 fetchbtn.setBounds(220, 600, 150, 25);
                 backbtn.setBounds(40, 600, 90, 25);
                 JLabel label[] = new JLabel[total_item];
                 i = 0;
                 total = 0;
                 serialn = 0;
                 jp1.setLayout(null);
                 String str1, str2 = "";
                 for (Element link : links) {
                     String find_string = link.text();
                     //if(find_string.contains("LinkedIn")){
                     //System.out.println(link.text());
                     //}
                     //Get the value from href attribute
                     //if (link.text().equalsIgnoreCase("Similar")) {
                     //continue;
                     //}
                     //if(link.text().equalsIgnoreCase("1")||link.text().equalsIgnoreCase("2")||link.text().equalsIgnoreCase("3"))
                     //break;
                     //if (i >= 37 && !link.text().equalsIgnoreCase("Cached")) {
                     if (find_string.contains("LinkedIn")) {
                         str2 = "";
                         cbox[serialn] = new JCheckBox();
                         str1 = link.text();
                         for (int j = 0; j < str1.length(); j++) {
                             if (str1.charAt(j) == '-' || str1.charAt(j) == '|') {
                                 break;
                             }
                             str2 += str1.charAt(j);
                         }
                         Cname[total] = str2;
                         total++;
                         //cbox[serialn].setBounds(50, 80 + (25 * (i - 37)), 250, 20);
                         cbox[serialn].setBounds(50, 30 + (50 * (serialn + 1)), 250, 20);
                         cbox[serialn].setText(str2);
                         jp1.add(cbox[serialn]);
                         if (serialn == 10) {
                             break;
                         }
                         serialn++;
                     }
                     i++;
                 }
                 exportbtn.setBounds(450, 600, 150, 25);
                 //Action listner of Export email button.
                 exportbtn.addActionListener(
                         new ActionListener() {
                     public void actionPerformed(ActionEvent paramActionEvent) {
                         //.xlsx creation starts here
                         try {
                             Workbook wb = null;
                             String location = JOptionPane.showInputDialog(null, "Enter File URL.", "", 1);
                             String xlsx = "xls";
                             if (xlsx.equalsIgnoreCase("xlsx")) {
                                 wb = new XSSFWorkbook();
                             } else {
                                 wb = new HSSFWorkbook();
                             }
                             Sheet sheet = wb.createSheet("Email Scrapper");
                             int k = 1;
                             sheet.setDefaultColumnWidth(20);
                             Row row[] = new Row[12];
                             Cell cell[][] = new Cell[12][2];
                             //Cell cell;
                             row[0] = sheet.createRow((short) 0);
                             cell[0][0] = row[0].createCell(0);
                             cell[0][0].setCellValue(" Name ");
 
                             cell[0][1] = row[0].createCell(1);
                             cell[0][1].setCellValue("E-Mail Address");
                             for (int z = 0; z < 10; z++) {
                                 if (cbox[z].isSelected() && !cbox[z].getText().isEmpty()) {
                                     row[k] = sheet.createRow((short) k);
                                     cell[k][0] = row[k].createCell(0);
                                     cell[k][0].setCellValue(cbox[z].getText());
                                     cell[k][1] = row[k].createCell(1);
                                     cell[k][1].setCellValue(email_store[k - 1]);
                                     k++;
                                 }
                             }
                             String excelFileName = "EmailScrapperContacts.xls";
                             if (wb instanceof XSSFWorkbook) {
                                 excelFileName += "x";
                             }
                             //java.io.File fs =new java.io.File("c:\")
                             FileOutputStream fos = new FileOutputStream(location + "/" + excelFileName);
                             wb.write(fos);
                             fos.flush();
                             fos.close();
                             JOptionPane.showMessageDialog(null, "File has been exported.");
                         } catch (Exception exc) {
                             JOptionPane.showMessageDialog(null, "Export Fails : " + exc.getMessage());
                         }
                         //.xlsx creation ends here
                     }
                 });
                 //Action listner of fetch email button.
                 fetchbtn.addActionListener(
                         new ActionListener() {
                     public void actionPerformed(ActionEvent paramActionEvent) {
                         try {
                             //unset the previous search result from email.
                             try{
                             for (int z = 0; z < 10; z++) {
                                 email_label[z].setText("");
                             }
                             }catch(Exception e3){
                                 
                             }
                             //unset the previous search result from email end here.
                             String siteurl = JOptionPane.showInputDialog(null, "Enter site url of users.(Please donot use http or www)", "", 1);
                             int email_label_counter = 0;
                             for (int z = 0; z < 10; z++) {
                                 if (cbox[z].isSelected() && !cbox[z].getText().isEmpty()) {
                                     String checked_value = cbox[z].getText();
                                     //String search_value = "(email | mailto | contact)" + " * " + siteurl + " " +checked_value;
                                     //String final_search_value = url + "/search?q=" + URLEncoder.encode(search_value);   
                                     String search_value = "(email | mailto | contact) email " + siteurl + " " + checked_value;
                                     String web_url1 = null;
                                     if (url.equals("http://www.google.com")) {
                                         web_url1 = url + "/search?q=";
                                     }
                                     if (url.equals("http://www.dogpile.com")) {
                                         web_url1 = url + "/info.dogpl/search?q=";
                                     }
                                     String final_search_value = web_url1 + URLEncoder.encode(search_value);
                                     //String final_search_value = url + "/search?q=" + URLEncoder.encode(search_value);
 
                                     //JOptionPane.showMessageDialog(null, final_search_value);
                                     Document emaildoc = null;
                                     emaildoc = Jsoup.connect(final_search_value).userAgent("Mozilla").timeout(10 * 1000).get();
 
                                     Elements mails = emaildoc.select("[class=st]");
 
                                     int email_incr = 0;
                                     for (Element mail : mails) {
                                         String mail_body = mail.text();
                                         if (mail_body.contains("@" + siteurl)) {
                                             String[] splits_mail_body = mail_body.split(" ");
                                             int temp = splits_mail_body.length - 1;
                                             for (int i = 0; i <= temp; i++) {
                                                 if (splits_mail_body[i].contains("@" + siteurl)) {
                                                     email_store[email_incr] = splits_mail_body[i];
                                                     email_incr++;
                                                 }
                                                 if (email_incr == 4) {
                                                     break;
                                                 }
 
                                             }
                                         }
                                         //System.out.println("email : " + email_store);
                                         //System.out.println("text : " + mail.outerHtml());
                                     }
                                     //JOptionPane.showMessageDialog(null,"demo1"); 
                                     //System.out.println("email : " + z);
                                     if (email_store[email_label_counter] == "" || email_store[email_label_counter] == null) {
                                         email_store[email_label_counter] = "Not Found.";
                                         email_label[email_label_counter] = new JLabel("Not Found.");
                                         email_label[email_label_counter].setBounds(300, 30 + (50 * (z + 1)), 300, 20);
                                     } else {
                                         email_label[email_label_counter] = new JLabel(email_store[email_label_counter]);
                                         email_label[email_label_counter].setBounds(300, 30 + (50 * (z + 1)), 300, 20);
                                     }
                                     jp1.add(email_label[email_label_counter]);
                                     email_label_counter++;
                                 }
 
 
                             }
                         } catch (Exception exc) {
                             JOptionPane.showMessageDialog(null, "Cancel action: " + exc.getMessage());
                         }
                         jf1.setVisible(false);
                         jf1.setVisible(true);
                     }
                 });
                 //Action listner of back button.
                 backbtn.addActionListener(
                         new ActionListener() {
                     public void actionPerformed(ActionEvent paramActionEvent) {
                         try {
                             for (int i = 0; i <= 20; i++) {
                                 try {
                                     jp1.remove(cbox[i]);
                                     jp1.remove(email_label[i]);
                                     email_label[i].setText("");
                                     cbox[i].setText("");
                                 } catch (Exception e2) {
                                 }
 
                             }
                             jp1.remove(searchresultlabel);
                             jp1.remove(frmName);
                             jp1.remove(frmEmail);
                         } catch (Exception e) {
                         }
 
                         jf1.remove(jp1);
                         jf2.setVisible(true);
                         jf1.setVisible(false);
                     }
                 });
                 frmName.setBounds(50, 50, 150, 20);
                 frmName.setFont(f1);
                 frmEmail.setBounds(300, 50, 300, 20);
                 frmEmail.setFont(f1);
                 jp1.add(frmName);
                 jp1.add(frmEmail);
                 jp1.add(searchresultlabel);
                 jp1.add(fetchbtn);
                 jp1.add(backbtn);
                 jp1.add(exportbtn);
                 this.setVisible(false);
                 contentPane = (JPanel) this.getContentPane();
 
                 this.remove(contentPane);
                 this.setVisible(false);
                 jf1 = new JFrame();
                 jf1.add(jp1);
                 jf1.setVisible(true);
 
                 jf1.setTitle("Ebizon e-mail Scrapper Tool");
                 jf1.setLocation(new Point(300, 30));
                 jf1.setSize(new Dimension(950, 730));
                 jf1.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
             } catch (Exception ex) {
                 JOptionPane.showMessageDialog(null, "Error  :  " + ex.getMessage());
             }
         }
     }
     
     //Implementation of searchcsvbtn_actionPerformed.
     private void searchcsvbtn_actionPerformed(ActionEvent e) {
         c_import_panel = new JPanel();
         c_import_panel.setLayout(null);
         this.setVisible(false);
         contentPane = (JPanel) this.getContentPane();
         this.remove(contentPane);
         this.setVisible(false);
         //Create the object of label textbox and submit button.
         csvreadlabel = new JLabel();
         csvreadtextbox = new JTextField();
         csvreadbtn = new JButton();
         alexa_page_heading = new JLabel();
         csvreadlabel.setBounds(60, 50, 120, 20); //set the position of csvreadlabel.
         csvreadtextbox.setBounds(190, 50, 200, 20); //set the position of csvreadtextbox.
         
         //csvreadtextbox.setText("/Users/ebizondev/Desktop/shashank/export/sample.csv");
        
         csvreadbtn.setBounds(400, 50, 100, 20); //set the position of csvreadbtn.
         csvreadlabel.setText("Enter csv file path:");
         csvreadbtn.setText("Search");
         
         alexa_page_heading.setBounds(200, 10, 250, 20); //set the position of csvreadbtn.
         alexa_page_heading.setText("Search sites alexa ranks");
         Font f_alexa_page = new Font(alexa_page_heading.getFont().getName(), Font.BOLD, 15);
         alexa_page_heading.setFont(f_alexa_page);
         //Action listner of csvreadbtn button.
         csvreadbtn.addActionListener(
                  new ActionListener() {
                  public void actionPerformed(ActionEvent paramActionEvent){
                   String csvFile = (String) csvreadtextbox.getText();
                   if (csvFile.equals("")) {
                     //csvreadbtn.setEnabled(false);
                     JLabel errorFields_csv = new JLabel("<HTML><FONT COLOR = black>Please enter csv file path.</FONT></HTML>");
                     JOptionPane.showMessageDialog(null, errorFields_csv);
                     csvreadbtn.setEnabled(true);
                     //this.setVisible(true);
                   } else {
                     try { 
                     //create BufferedReader to read csv file
                     BufferedReader br = new BufferedReader(new FileReader(csvFile));
                     String line = "";
                     StringTokenizer st = null;
                     int lineNumber = 0; 
                     int tokenNumber = 0;
                     //read comma separated file line by line and find the count of lines.
                     BufferedReader count_br = new BufferedReader(new FileReader(csvFile));
                     String count_line = "";
                     int count_lineNumber = 0; 
                     while ((count_line = count_br.readLine()) != null) {
                       count_lineNumber++;
                     }
                     
                     alexa_csv_count_row = count_lineNumber;
                     //Create the label array.
                     //JLabel alexa_site_url[] = new JLabel[count_lineNumber];
                     //JLabel alexa_site_rank[] = new JLabel[count_lineNumber];
                     alexa_site_url = new JLabel[count_lineNumber];
                     alexa_site_rank = new JLabel[count_lineNumber];
                     
                     alexa_site_url_heading = new JLabel();
                     alexa_site_url_heading.setBounds(60, 100, 250, 30);
                     alexa_site_url_heading.setText("URL");
                     c_import_panel.add(alexa_site_url_heading);
                     
                     alexa_site_rank_heading = new JLabel();
                     alexa_site_rank_heading.setBounds(350, 100, 250, 30);
                     alexa_site_rank_heading.setText("Alexa ranks");
                     c_import_panel.add(alexa_site_rank_heading);
                     Font f_alexa1 = new Font(alexa_site_url_heading.getFont().getName(), Font.BOLD, 15);
                     alexa_site_url_heading.setFont(f_alexa1);
                     Font f_alexa2 = new Font(alexa_site_rank_heading.getFont().getName(), Font.BOLD, 15);
                     alexa_site_rank_heading.setFont(f_alexa2);
                     //Create the object of alexa_export_btn.
                     //alexa_export_btn = new JButton();
                     alexa_export_btn.setBounds(530, 50, 130, 20);
                     alexa_export_btn.setText("Export Alexa");
                     c_import_panel.add(alexa_export_btn);
                     
                     while ((line = br.readLine()) != null) {
                       lineNumber++;
                       //use comma as token separator
                       st = new StringTokenizer(line, ",");
                       while (st.hasMoreTokens()) {
                         tokenNumber++;
                         String ourSite = st.nextToken();
                         //String ourSite ="www.javadomain.in";
                         String url_alexa = "http://www.alexa.com/search?q="+ourSite+"&r=home_home&p=bigtop";
                         Document document = Jsoup.connect(url_alexa).userAgent("Mozilla").timeout(10 * 1000).get();
                         String alexa_india_rank;
                         try
                         {
                         Elements india = document.select("div.row");
                         List mylist = new ArrayList();
                         for (Element answerer : india) {
                             mylist.add(answerer.text().toString());
                         }
                         String world_india = (String) mylist.get(5);
                         String[] world_india_arr;
                         String delimiter = ":";  
                         world_india_arr = world_india.split(delimiter);
                         alexa_india_rank = world_india_arr[2];
                         }
                         catch(Exception ex1)
                         {
                             alexa_india_rank = "No regional data";
                         }
                         //Create the label object.
                         alexa_site_url[lineNumber-1]=new JLabel();
                         alexa_site_url[lineNumber-1].setBounds(60, 110 + lineNumber * 30, 250, 30);
                         alexa_site_url[lineNumber-1].setText(ourSite);
                         c_import_panel.add(alexa_site_url[lineNumber-1]);
                         
                         alexa_site_rank[lineNumber-1]=new JLabel();
                         alexa_site_rank[lineNumber-1].setBounds(350, 110 + lineNumber * 30, 250, 30);
                         alexa_site_rank[lineNumber-1].setText(alexa_india_rank);
                         c_import_panel.add(alexa_site_rank[lineNumber-1]);
                         
                         //System.out.print("hjghgjghj"); //csv_searchurl);
                       }
                       //reset token number
                       tokenNumber = 0;
                     }
                   } catch (Exception e) {
                     //System.err.println("CSV file cannot be read : " + e);
                     JOptionPane.showMessageDialog(null, "CSV file cannot be read : " + e);
                   }
                     c_import_frame.setVisible(false);
                     c_import_frame.setVisible(true);
                   }
                   //System.out.println(csvFile);
                  }
         });
         //Action listner of Export button alexa_export_btn.
         alexa_export_btn.addActionListener(
                         new ActionListener() {
                     public void actionPerformed(ActionEvent paramActionEvent) {
                         //.xlsx creation starts here
                         try {
                             Workbook wb1 = null;
                             String location_alexa_file = JOptionPane.showInputDialog(null, "Enter File URL.", "", 1);
                            
                             String xlsx1 = "xls";
                             if (xlsx1.equalsIgnoreCase("xlsx")) {
                                 wb1 = new XSSFWorkbook();
                             } else {
                                 wb1 = new HSSFWorkbook();
                             }
                             Sheet sheet1 = wb1.createSheet("Site Alexa ranking");
                             int alexa_c = 1;
                             sheet1.setDefaultColumnWidth(25);
                             Row row[] = new Row[alexa_csv_count_row+1];
                             Cell cell[][] = new Cell[alexa_csv_count_row+1][2];
                             //Cell cell;
                             row[0] = sheet1.createRow((short) 0);
                             cell[0][0] = row[0].createCell(0);
                             cell[0][0].setCellValue(" URL ");
                             cell[0][1] = row[0].createCell(1);
                             cell[0][1].setCellValue("Alexa ranks");
                            
                             for (int alexa_counter = 0; alexa_counter < alexa_csv_count_row; alexa_counter++) {
                                    try{
                                     row[alexa_c] = sheet1.createRow((short) alexa_c);
                                     cell[alexa_c][0] = row[alexa_c].createCell(0);
                                     cell[alexa_c][0].setCellValue(alexa_site_url[alexa_counter].getText());
                                     cell[alexa_c][1] = row[alexa_c].createCell(1);
                                     cell[alexa_c][1].setCellValue(alexa_site_rank[alexa_counter].getText());
                                     alexa_c++;
                                    }catch(Exception err){
                                        
                                    }
                                    
                             }
                             
                             String excelFileName = "sites_alexarating.xls";
                             
                             if (wb1 instanceof XSSFWorkbook) {
                                 excelFileName += "x";
                             }
                             //java.io.File fs =new java.io.File("c:\")
                             FileOutputStream fos = new FileOutputStream(location_alexa_file + "/" + excelFileName);
                             wb1.write(fos);
                             fos.flush();
                             fos.close();
                             JOptionPane.showMessageDialog(null, "Sites alexa rating file has been exported.");
                         } catch (Exception exc1) {
                            JOptionPane.showMessageDialog(null, "Export Fails1 : " + exc1.getMessage());
                         }
                         //.xlsx creation ends here
                     }
                 });
         c_import_panel.add(csvreadlabel);
         c_import_panel.add(csvreadtextbox);
         c_import_panel.add(csvreadbtn);
         c_import_panel.add(alexa_page_heading);
         c_import_frame = new JFrame();
         c_import_frame.add(c_import_panel);
         c_import_frame.setVisible(true);
         c_import_frame.setTitle("Ebizon e-mail Scrapper Tool");
         c_import_frame.setLocation(new Point(300, 30));
         c_import_frame.setSize(new Dimension(700, 700));
         c_import_frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
     }
     
     //Implementation of exportallbtn_actionPerformed.
     private void exportallbtn_actionPerformed(ActionEvent e) {
        /*
         exportall_panel = new JPanel();
         exportall_panel.setLayout(null);
         this.setVisible(false);
         contentPane = (JPanel) this.getContentPane();
         this.remove(contentPane);
         this.setVisible(false);
         //Create the object of label textbox and submit button.
         csvreadlabel = new JLabel();
         csvreadtextbox = new JTextField();
         csvreadbtn = new JButton();
         alexa_page_heading = new JLabel();
         csvreadlabel.setBounds(60, 50, 120, 20); //set the position of csvreadlabel.
         csvreadtextbox.setBounds(190, 50, 200, 20); //set the position of csvreadtextbox.
         
         //csvreadtextbox.setText("/Users/ebizondev/Desktop/shashank/export/sample.csv");
        
         csvreadbtn.setBounds(400, 50, 100, 20); //set the position of csvreadbtn.
         csvreadlabel.setText("Enter csv file path:");
         csvreadbtn.setText("Search");
         
         alexa_page_heading.setBounds(200, 10, 250, 20); //set the position of csvreadbtn.
         alexa_page_heading.setText("Search sites alexa ranks");
         Font f_alexa_page = new Font(alexa_page_heading.getFont().getName(), Font.BOLD, 15);
         alexa_page_heading.setFont(f_alexa_page);
         //Action listner of csvreadbtn button.
         csvreadbtn.addActionListener(
                  new ActionListener() {
                  public void actionPerformed(ActionEvent paramActionEvent){
                   String csvFile = (String) csvreadtextbox.getText();
                   if (csvFile.equals("")) {
                     //csvreadbtn.setEnabled(false);
                     JLabel errorFields_csv = new JLabel("<HTML><FONT COLOR = black>Please enter csv file path.</FONT></HTML>");
                     JOptionPane.showMessageDialog(null, errorFields_csv);
                     csvreadbtn.setEnabled(true);
                     //this.setVisible(true);
                   } else {
                     try { 
                     //create BufferedReader to read csv file
                     BufferedReader br = new BufferedReader(new FileReader(csvFile));
                     String line = "";
                     StringTokenizer st = null;
                     int lineNumber = 0; 
                     int tokenNumber = 0;
                     //read comma separated file line by line and find the count of lines.
                     BufferedReader count_br = new BufferedReader(new FileReader(csvFile));
                     String count_line = "";
                     int count_lineNumber = 0; 
                     while ((count_line = count_br.readLine()) != null) {
                       count_lineNumber++;
                     }
                     
                     alexa_csv_count_row = count_lineNumber;
                     //Create the label array.
                     //JLabel alexa_site_url[] = new JLabel[count_lineNumber];
                     //JLabel alexa_site_rank[] = new JLabel[count_lineNumber];
                     alexa_site_url = new JLabel[count_lineNumber];
                     alexa_site_rank = new JLabel[count_lineNumber];
                     
                     alexa_site_url_heading = new JLabel();
                     alexa_site_url_heading.setBounds(60, 100, 250, 30);
                     alexa_site_url_heading.setText("URL");
                     exportall_panel.add(alexa_site_url_heading);
                     
                     alexa_site_rank_heading = new JLabel();
                     alexa_site_rank_heading.setBounds(350, 100, 250, 30);
                     alexa_site_rank_heading.setText("Alexa ranks");
                     exportall_panel.add(alexa_site_rank_heading);
                     Font f_alexa1 = new Font(alexa_site_url_heading.getFont().getName(), Font.BOLD, 15);
                     alexa_site_url_heading.setFont(f_alexa1);
                     Font f_alexa2 = new Font(alexa_site_rank_heading.getFont().getName(), Font.BOLD, 15);
                     alexa_site_rank_heading.setFont(f_alexa2);
                     //Create the object of alexa_export_btn.
                     //alexa_export_btn = new JButton();
                     alexa_export_btn.setBounds(530, 50, 130, 20);
                     alexa_export_btn.setText("Export Alexa");
                     exportall_panel.add(alexa_export_btn);
                     
                     while ((line = br.readLine()) != null) {
                       lineNumber++;
                       //use comma as token separator
                       st = new StringTokenizer(line, ",");
                       while (st.hasMoreTokens()) {
                         tokenNumber++;
                         String ourSite = st.nextToken();
                         //String ourSite ="www.javadomain.in";
                         String url_alexa = "http://www.alexa.com/search?q="+ourSite+"&r=home_home&p=bigtop";
                         Document document = Jsoup.connect(url_alexa).userAgent("Mozilla").timeout(10 * 1000).get();
                         String alexa_india_rank;
                         try
                         {
                         Elements india = document.select("div.row");
                         List mylist = new ArrayList();
                         for (Element answerer : india) {
                             mylist.add(answerer.text().toString());
                         }
                         String world_india = (String) mylist.get(5);
                         String[] world_india_arr;
                         String delimiter = ":";  
                         world_india_arr = world_india.split(delimiter);
                         alexa_india_rank = world_india_arr[2];
                         }
                         catch(Exception ex1)
                         {
                             alexa_india_rank = "No regional data";
                         }
                         //Create the label object.
                         alexa_site_url[lineNumber-1]=new JLabel();
                         alexa_site_url[lineNumber-1].setBounds(60, 110 + lineNumber * 30, 250, 30);
                         alexa_site_url[lineNumber-1].setText(ourSite);
                         exportall_panel.add(alexa_site_url[lineNumber-1]);
                         
                         alexa_site_rank[lineNumber-1]=new JLabel();
                         alexa_site_rank[lineNumber-1].setBounds(350, 110 + lineNumber * 30, 250, 30);
                         alexa_site_rank[lineNumber-1].setText(alexa_india_rank);
                         exportall_panel.add(alexa_site_rank[lineNumber-1]);
                         
                         //System.out.print("hjghgjghj"); //csv_searchurl);
                       }
                       //reset token number
                       tokenNumber = 0;
                     }
                   } catch (Exception e) {
                     //System.err.println("CSV file cannot be read : " + e);
                     JOptionPane.showMessageDialog(null, "CSV file cannot be read : " + e);
                   }
                     exportall_frame.setVisible(false);
                     exportall_frame.setVisible(true);
                   }
                   //System.out.println(csvFile);
                  }
         });
         //Action listner of Export button alexa_export_btn.
         alexa_export_btn.addActionListener(
                         new ActionListener() {
                     public void actionPerformed(ActionEvent paramActionEvent) {
                         //.xlsx creation starts here
                         try {
                             Workbook wb1 = null;
                             String location_alexa_file = JOptionPane.showInputDialog(null, "Enter File URL.", "", 1);
                             String xlsx1 = "xls";
                             if (xlsx1.equalsIgnoreCase("xlsx")) {
                                 wb1 = new XSSFWorkbook();
                             } else {
                                 wb1 = new HSSFWorkbook();
                             }
                             Sheet sheet1 = wb1.createSheet("Site Alexa ranking");
                             int alexa_c = 1;
                             sheet1.setDefaultColumnWidth(25);
                             Row row[] = new Row[alexa_csv_count_row+1];
                             Cell cell[][] = new Cell[alexa_csv_count_row+1][2];
                             //Cell cell;
                             row[0] = sheet1.createRow((short) 0);
                             cell[0][0] = row[0].createCell(0);
                             cell[0][0].setCellValue(" URL ");
                             cell[0][1] = row[0].createCell(1);
                             cell[0][1].setCellValue("Alexa ranks");
                             for (int alexa_counter = 0; alexa_counter < alexa_csv_count_row; alexa_counter++) {
                                     row[alexa_c] = sheet1.createRow((short) alexa_c);
                                     cell[alexa_c][0] = row[alexa_c].createCell(0);
                                     cell[alexa_c][0].setCellValue(alexa_site_url[alexa_counter].getText());
                                     cell[alexa_c][1] = row[alexa_c].createCell(1);
                                     cell[alexa_c][1].setCellValue(alexa_site_rank[alexa_counter].getText());
                                     alexa_c++;
                             }
                             
                            
                             String excelFileName = "sites_alexarating.xls";
                             
                             if (wb1 instanceof XSSFWorkbook) {
                                 excelFileName += "x";
                             }
                             //java.io.File fs =new java.io.File("c:\")
                             FileOutputStream fos = new FileOutputStream(location_alexa_file + "/" + excelFileName);
                             wb1.write(fos);
                             fos.flush();
                             fos.close();
                             JOptionPane.showMessageDialog(null, "Sites alexa rating file has been exported.");
                         } catch (Exception exc1) {
                             JOptionPane.showMessageDialog(null, "Export Fails : " + exc1.getMessage());
                         }
                         //.xlsx creation ends here
                     }
                 });
         
         exportall_panel.add(csvreadlabel);
         exportall_panel.add(csvreadtextbox);
         exportall_panel.add(csvreadbtn);
         exportall_panel.add(alexa_page_heading);
         exportall_frame = new JFrame();
         exportall_frame.add(exportall_panel);
         exportall_frame.setVisible(true);
         exportall_frame.setTitle("Ebizon e-mail Scrapper Tool for search email contact and alexa rank.");
         exportall_frame.setLocation(new Point(130, 30));
         exportall_frame.setSize(new Dimension(1024, 700));
         exportall_frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        */
     }
     
     public static void main(String[] args) {
         JFrame.setDefaultLookAndFeelDecorated(true);
         JDialog.setDefaultLookAndFeelDecorated(true);
         try {
             UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
         } catch (Exception ex) {
             System.out.println("Failed loading L&F: ");
             System.out.println(ex);
         }
         new TweenTribneEmailScrapper();
     }
 
     public void actionPerformed(ActionEvent arg0) {
     }
 
     public void clickToGetPost(String container1, String url1, String dest) {
         String fileData = "";
         BufferedWriter bufferWritter = null;
         try {
             String container = container1.trim();
             String url = url1;
             String destination = dest;
 
 
 
             Document doc = postUrl(url);
             Elements links = doc.select(container + " a[href]");
 
             //JLabel errorFields123 = new JLabel(destination);
             //JOptionPane.showMessageDialog(null, errorFields123);
 
             print("\nLinks: (%d)", links.size());
             //System.out.println(links);
 
             try {
                 for (Element link : links) {
                     if (link.toString().contains("getEmail")) {
                         String temp = link.toString().substring(link.toString().indexOf("('"), link.toString().indexOf("')")).replace("('", "");
                         String temp1 = temp.replace("'", "");
                         String params[] = temp1.split(",");
                         fileData += getEmail(params[0], params[1], params[2]).concat("\n");
                     }
                 }
                 FileWriter fstream = new FileWriter(destination + "out.txt", true);
                 bufferWritter = new BufferedWriter(fstream);
                 bufferWritter.write(fileData);
             } catch (Exception e) {
                 System.err.println("Error: " + e.getMessage());
             }
         } catch (Exception e) {
             System.err.println("Error: " + e.getMessage());
         } finally {
             try {
                 bufferWritter.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
             JLabel errorFields = new JLabel("<HTML><FONT COLOR = Blue>Success</FONT></HTML>");
             JOptionPane.showMessageDialog(null, errorFields);
 
         }
     }
 
     private static void print(String msg, Object... args) {
         System.out.println(String.format(msg, args));
     }
 
     private void getValues() {
         String paramKey1 = key1.getText().trim();
         String paramValue1 = value1.getText().trim();
         String paramKey2 = key2.getText().trim();
         String paramValue2 = value2.getText().trim();
         String paramKey3 = key3.getText().trim();
         String paramValue3 = value3.getText().trim();
         String paramKey4 = key4.getText().trim();
         String paramValue4 = value4.getText().trim();
         String paramKey5 = key5.getText().trim();
         String paramValue5 = value5.getText().trim();
         String paramKey6 = key6.getText().trim();
         String paramValue6 = value6.getText().trim();
         String paramKey7 = key7.getText().trim();
         String paramValue7 = value7.getText().trim();
         String paramKey8 = key8.getText().trim();
         String paramValue8 = value8.getText().trim();
         String paramKey9 = key9.getText().trim();
         String paramValue9 = value9.getText().trim();
         String paramKey10 = key10.getText().trim();
         String paramValue10 = value10.getText().trim();
 
 
 
         if (!paramKey1.equalsIgnoreCase("")) {
             arrayList.add(paramKey1);
         }
         if (!paramKey2.equalsIgnoreCase("")) {
             arrayList.add(paramKey2);
         }
         if (!paramKey3.equalsIgnoreCase("")) {
             arrayList.add(paramKey3);
         }
         if (!paramKey4.equalsIgnoreCase("")) {
             arrayList.add(paramKey4);
         }
         if (!paramKey5.equalsIgnoreCase("")) {
             arrayList.add(paramKey5);
         }
         if (!paramKey6.equalsIgnoreCase("")) {
             arrayList.add(paramKey6);
         }
         if (!paramKey7.equalsIgnoreCase("")) {
             arrayList.add(paramKey7);
         }
         if (!paramKey8.equalsIgnoreCase("")) {
             arrayList.add(paramKey8);
         }
         if (!paramKey9.equalsIgnoreCase("")) {
             arrayList.add(paramKey9);
         }
         if (!paramKey10.equalsIgnoreCase("")) {
             arrayList.add(paramKey10);
         }
         for (int i = 0; i <= arrayList.size() - 1; i++) {
             if (i == 0) {
                 hashMap.put(arrayList.get(i), paramValue1);
             }
             if (i == 1) {
                 hashMap.put(arrayList.get(i), paramValue2);
             }
             if (i == 2) {
                 hashMap.put(arrayList.get(i), paramValue3);
             }
             if (i == 3) {
                 hashMap.put(arrayList.get(i), paramValue4);
             }
             if (i == 4) {
                 hashMap.put(arrayList.get(i), paramValue5);
             }
             if (i == 5) {
                 hashMap.put(arrayList.get(i), paramValue6);
             }
             if (i == 6) {
                 hashMap.put(arrayList.get(i), paramValue7);
             }
             if (i == 7) {
                 hashMap.put(arrayList.get(i), paramValue8);
             }
             if (i == 8) {
                 hashMap.put(arrayList.get(i), paramValue9);
             }
             if (i == 9) {
                 hashMap.put(arrayList.get(i), paramValue10);
             }
         }
     }
 
     public String getEmail(String id, String user, String site) {
         String tempUser = "";
         int userCount = user.indexOf("*");
         user = user.substring(0, userCount);
         String reverse = new StringBuffer(user).reverse().toString();
         reverse = reverse + tempUser + '@' + site;
         return reverse;
     }
 
     public void oneLevelDeepPost(String container1, String url1, String dest) {
         String fileData = "";
         BufferedWriter bufferWritter = null;
         try {
             String container = container1;
             String url = url1;
             String destination = dest;
 
             Document doc = postUrl(url);
             Elements links = doc.select(container + " a[href]");
             print("\nLinks: (%d)", links.size());
 
             try {
                 for (Element link : links) {
                     boolean connected = false;
                     int attempts = 0;
                     while (!connected) {
                         if (attempts++ == 1) {
                             break;
                         }
                         try {
                             org.jsoup.Connection connection = org.jsoup.Jsoup.connect(link.attr("abs:href")).timeout(10 * 1000);
                             connection.followRedirects(false);
                             org.jsoup.Connection.Response response = connection.execute();
                             if (response.statusCode() == 200) {
                                 Document doc1 = connection.get();
                                 fileData += doc1.toString().concat("\n");
 
                             }
                         } catch (Exception ex) {
                             System.out.println("Connection failure #" + attempts + ": " + link);
                             System.out.println(ex.getMessage());
                         }
                     }
                 }
                 FileWriter fstream = new FileWriter(destination + "out.txt", true);
                 bufferWritter = new BufferedWriter(fstream);
                 bufferWritter.write(fileData);
             } catch (Exception e) {
                 System.err.println("Error: " + e.getMessage());
             }
         } catch (Exception e) {
             System.err.println("Error: " + e.getMessage());
         } finally {
             try {
                 bufferWritter.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
             JLabel errorFields = new JLabel("<HTML><FONT COLOR = Blue>Success</FONT></HTML>");
             JOptionPane.showMessageDialog(null, errorFields);
         }
     }
 
     public Document postUrl(String url) {
         int sizeOfData = arrayList.size();
         Document doc = null;
         try {
             if (sizeOfData == 1) {
                 doc = org.jsoup.Jsoup.connect(url).timeout(10 * 1000).userAgent("Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.65 Safari/535.11").data(arrayList.get(0), hashMap.get(arrayList.get(0))).post();
             }
             if (sizeOfData == 2) {
                 doc = org.jsoup.Jsoup.connect(url).timeout(10 * 1000).userAgent("Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.65 Safari/535.11").data(arrayList.get(0), hashMap.get(arrayList.get(0))).data(arrayList.get(1), hashMap.get(arrayList.get(1))).post();
             }
             if (sizeOfData == 3) {
                 doc = org.jsoup.Jsoup.connect(url).timeout(10 * 1000).userAgent("Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.65 Safari/535.11").data(arrayList.get(0), hashMap.get(arrayList.get(0))).data(arrayList.get(1), hashMap.get(arrayList.get(1))).data(arrayList.get(2), hashMap.get(arrayList.get(2))).post();
             }
             if (sizeOfData == 4) {
                 doc = org.jsoup.Jsoup.connect(url).timeout(10 * 1000).userAgent("Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.65 Safari/535.11").data(arrayList.get(0), hashMap.get(arrayList.get(0))).data(arrayList.get(1), hashMap.get(arrayList.get(1))).data(arrayList.get(2), hashMap.get(arrayList.get(2))).data(arrayList.get(3), hashMap.get(arrayList.get(3))).post();
             }
             if (sizeOfData == 5) {
                 doc = org.jsoup.Jsoup.connect(url).timeout(10 * 1000).userAgent("Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.65 Safari/535.11").data(arrayList.get(0), hashMap.get(arrayList.get(0))).data(arrayList.get(1), hashMap.get(arrayList.get(1))).data(arrayList.get(2), hashMap.get(arrayList.get(2))).data(arrayList.get(3), hashMap.get(arrayList.get(3))).data(arrayList.get(4), hashMap.get(arrayList.get(4))).post();
             }
             if (sizeOfData == 6) {
                 doc = org.jsoup.Jsoup.connect(url).timeout(10 * 1000).userAgent("Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.65 Safari/535.11").data(arrayList.get(0), hashMap.get(arrayList.get(0))).data(arrayList.get(1), hashMap.get(arrayList.get(1))).data(arrayList.get(2), hashMap.get(arrayList.get(2))).data(arrayList.get(3), hashMap.get(arrayList.get(3))).data(arrayList.get(4), hashMap.get(arrayList.get(4))).data(arrayList.get(5), hashMap.get(arrayList.get(5))).post();
             }
             if (sizeOfData == 6) {
                 doc = org.jsoup.Jsoup.connect(url).timeout(10 * 1000).userAgent("Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.65 Safari/535.11").data(arrayList.get(0), hashMap.get(arrayList.get(0))).data(arrayList.get(1), hashMap.get(arrayList.get(1))).data(arrayList.get(2), hashMap.get(arrayList.get(2))).data(arrayList.get(3), hashMap.get(arrayList.get(3))).data(arrayList.get(4), hashMap.get(arrayList.get(4))).data(arrayList.get(5), hashMap.get(arrayList.get(5))).post();
             }
             if (sizeOfData == 7) {
                 doc = org.jsoup.Jsoup.connect(url).timeout(10 * 1000).userAgent("Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.65 Safari/535.11").data(arrayList.get(0), hashMap.get(arrayList.get(0))).data(arrayList.get(1), hashMap.get(arrayList.get(1))).data(arrayList.get(2), hashMap.get(arrayList.get(2))).data(arrayList.get(3), hashMap.get(arrayList.get(3))).data(arrayList.get(4), hashMap.get(arrayList.get(4))).data(arrayList.get(5), hashMap.get(arrayList.get(5))).data(arrayList.get(5), hashMap.get(arrayList.get(5))).data(arrayList.get(6), hashMap.get(arrayList.get(6))).post();
             }
             if (sizeOfData == 8) {
                 doc = org.jsoup.Jsoup.connect(url).timeout(10 * 1000).userAgent("Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.65 Safari/535.11").data(arrayList.get(0), hashMap.get(arrayList.get(0))).data(arrayList.get(1), hashMap.get(arrayList.get(1))).data(arrayList.get(2), hashMap.get(arrayList.get(2))).data(arrayList.get(3), hashMap.get(arrayList.get(3))).data(arrayList.get(4), hashMap.get(arrayList.get(4))).data(arrayList.get(5), hashMap.get(arrayList.get(5))).data(arrayList.get(6), hashMap.get(arrayList.get(6))).data(arrayList.get(7), hashMap.get(arrayList.get(7))).post();
             }
             if (sizeOfData == 9) {
                 doc = org.jsoup.Jsoup.connect(url).timeout(10 * 1000).userAgent("Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.65 Safari/535.11").data(arrayList.get(0), hashMap.get(arrayList.get(0))).data(arrayList.get(1), hashMap.get(arrayList.get(1))).data(arrayList.get(2), hashMap.get(arrayList.get(2))).data(arrayList.get(3), hashMap.get(arrayList.get(3))).data(arrayList.get(4), hashMap.get(arrayList.get(4))).data(arrayList.get(5), hashMap.get(arrayList.get(5))).data(arrayList.get(6), hashMap.get(arrayList.get(6))).data(arrayList.get(7), hashMap.get(arrayList.get(7))).data(arrayList.get(8), hashMap.get(arrayList.get(8))).post();
             }
             if (sizeOfData == 10) {
                 doc = org.jsoup.Jsoup.connect(url).timeout(10 * 1000).userAgent("Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.65 Safari/535.11").data(arrayList.get(0), hashMap.get(arrayList.get(0))).data(arrayList.get(1), hashMap.get(arrayList.get(1))).data(arrayList.get(2), hashMap.get(arrayList.get(2))).data(arrayList.get(3), hashMap.get(arrayList.get(3))).data(arrayList.get(4), hashMap.get(arrayList.get(4))).data(arrayList.get(5), hashMap.get(arrayList.get(5))).data(arrayList.get(6), hashMap.get(arrayList.get(6))).data(arrayList.get(7), hashMap.get(arrayList.get(7))).data(arrayList.get(8), hashMap.get(arrayList.get(8))).data(arrayList.get(9), hashMap.get(arrayList.get(9))).post();
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return doc;
     }
 }
