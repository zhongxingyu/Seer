 
 package Cipher;
 
 import java.awt.event.MouseEvent;
 import java.io.*;
 import javax.swing.*;
 
 public class MainFrame extends javax.swing.JFrame {
 
     public JTextField fld;
     public static String store="", user, lev;
     public String list[];
     public MainFrame(String us) {
 
         user=us;
         list=new String[10]; 
         initComponents();
         a.setText(us);
         open_label.setVisible(false);
         open_list.setVisible(false);
         
     }
    String editfunc(String x)  {
        String nw="";
        int l=x.length(),i,j;
        for(i=0;i<l;i++)  {
            char ch=x.charAt(i),h;
            if(ch==' ') {    
                h=ch;
            
                while(h!=' ') {
                    nw+='\n';
                    i++;
                }
            }
            nw+=ch;
        }
        return(nw);
    }
    
    //level easy encoding
     void encodeone() {
         String a=input_text.getText();
         String c="",b="";
         int i,l;
         l=a.length();
         a=a.toUpperCase();
         for(i=0;i<l;i++) {
             char ch=a.charAt(i);
             c+=String.valueOf((int)ch);
         }
         for(i=0;i<c.length();i++) {
             char ch=c.charAt(i);
             b=String.valueOf(ch)+b;
         }
         output_text.setText(b);
     }
    
     //level easy decoding
     void decodeone() {
         String a=input_text.getText();
         String rev="",b="",nw="";
         int l,i,fl=0,x;
         char ch;
         l=a.length();
         for(i=l-1;i>=0;i--) {
             ch=a.charAt(i);
             rev+=String.valueOf(ch);
         }
         for(i=0;i<l;) {
             x=Integer.parseInt(rev.substring(i,i+2));
             if ((x>=97 && x<=99) || (x>=32 || x<=90) )
                 i+=2;
             else if(x>=100 && x<=125) {
                 b=rev.substring(i,i+3);
                 x=Integer.parseInt(b);
                 i+=3;
             }
             nw+=String.valueOf((char)x);
         }
         output_text.setText(nw);
     }
     
     //level hard encoding
     void encodetwo() {
         String at=input_text.getText();
         int lenn=at.length();
         int ind=at.indexOf('#');
         String a=at.substring(0,ind);
         int pc=Integer.parseInt(at.substring((ind+1),lenn));
         int x=pc,y=pc,c=0,len,r,mr,i,j,cn=0;
         String str="";
         char ch=' ';
         len=a.length();
         while(x>0) {
             x/=10;
             c++;
         }
         r=(len%c==0)?(len/c):((len/c)+1);
         String ar[][]=new String[r][c];
         for(i=0;i<r;i++)
         for(j=0;j<c;j++)
             ar[i][j]="00";
         for(i=0;i<r;i++) {
             for(j=0;j<c;j++) {
                 if(cn<len) {
                     ch=a.charAt(cn++);
                     if((ch>=65&&ch<=90)||(ch>=97&&ch<=122))
                     ar[i][j]=String.valueOf(ch);
                     else
                     ar[i][j]=String.valueOf((int)ch);
                 }
             }
         }
         while(y>0) {
             mr=(y%10)-1;
             for(i=0;i<r;i++)
             str+=ar[i][mr];
             y/=10;
         }//END OF WHILE
         String xxx=str+"#"+pc;
         output_text.setText(xxx);
     }
     
     //level medium decoding
     void decodetwo() {
         String a=input_text.getText();
         int i,ind,r,c,len,cn=0,j,cl,l,as,x=0;
         String pc,st,strnw="",yx="";
         char ch,chr;
         len=a.length();
         ind=a.indexOf('#');
         st=a.substring(0,ind);
         pc=a.substring((ind+1),len);
         l=st.length();
         for(i=0;i<l;i++) {
             int cx=(int)st.charAt(i);
             if(cx>=48 && cx<=57) {   
                 yx+=String.valueOf((char)Integer.parseInt(st.substring(i,i+2)));
                 i++;
             }
             else
                 yx+=String.valueOf(st.charAt(i));
         }
         l=yx.length();
         c=pc.length();
         r=(l/c);
         String ar[][]=new String[r][c];
         for(i=c-1;i>=0;i--) {
             ch=pc.charAt(i);
             cl=Integer.parseInt(String.valueOf(ch));
             for(j=0;j<r;j++)
                 ar[j][cl-1]=String.valueOf(yx.charAt(cn++));
                 
         }
             for(i=0;i<r;i++)
             for(j=0;j<c;j++)
             strnw+=ar[i][j];
             output_text.setText(strnw);
     }
     
     //level hard encoding
     
     void encodethree() {
         String st=input_text.getText();
         int len=st.length(); int h=len;
         for(int u=0;u <3-(len%3); u++)
             st+=".";
         len=st.length();
         String xx="",nw="";
         char bsvalue[]={'A',  'B',  'C',  'D',  'E',  'F',  'G',
                         'H',  'I',  'J',  'K',  'L',  'M',  'N',
                         'O',  'P',  'Q',  'R',  'S',  'T',  'U', 
                         'V',  'W',  'X',  'Y',  'Z',
                         'a',  'b',  'c',  'd',  'e',  'f',  'g',
                         'h',  'i',  'j',  'k',  'l',  'm',  'n',
                         'o',  'p',  'q',  'r',  's',  't',  'u', 
                         'v',  'w',  'x',  'y',  'z',
                         '0',  '1',  '2',  '3',  '4',  '5', 
                         '6',  '7',  '8',  '9',  '.',  ','};
                         
 
         for(int i=0;i<len;i++) {
             char ch=st.charAt(i);
             int as=(int)ch;
             int ar[]={0,0,0,0,0,0,0,0},cn=7;
             while(as>0) {
                 ar[cn--]=as%2;
                 as/=2;
             }
             for(int j=0;j<8;j++)
                 xx+=String.valueOf(ar[j]);
             nw+=xx;
             xx="";
         }
         String conv="";
         int ln=nw.length(),b=ln%6;
         for(int u=0;u<=b+1;u++)
             nw="0"+nw;
             ln+=b+2;
             int k=ln;
         for(;k>=5;k-=6) {
             String jj=nw.substring(k-6,k);
             int mm=Integer.valueOf(jj);
             int cnn=0,sm=0,r;
             while(mm>0)
             {
                 r=mm%10;
                 sm+=((int)Math.pow(2,cnn++))*r;
                 mm/=10;
             }
             conv=String.valueOf(bsvalue[sm])+conv;
         }
         output_text.setText(conv);
     }
  
     //level hard decoding
         void decodethree() {
         
         String s=input_text.getText();
         int l=s.length();
         char bsvalue[]={'A',  'B',  'C',  'D',  'E',  'F',  'G',
                         'H',  'I',  'J',  'K',  'L',  'M',  'N',
                         'O',  'P',  'Q',  'R',  'S',  'T',  'U', 
                         'V',  'W',  'X',  'Y',  'Z',
                         'a',  'b',  'c',  'd',  'e',  'f',  'g',
                         'h',  'i',  'j',  'k',  'l',  'm',  'n',
                         'o',  'p',  'q',  'r',  's',  't',  'u', 
                         'v',  'w',  'x',  'y',  'z',
                         '0',  '1',  '2',  '3',  '4',  '5', 
                         '6',  '7',  '8',  '9',  '.',  ','};
        
        String xx="",nw="";
        for(int j=0;j<l;j++) {
            int i=0;
            char ch=s.charAt(j);
            for(i=0;i<64;i++) {
                if(bsvalue[i]==ch)
                break;
            }
            int as=i;
            int ar[]={0,0,0,0,0,0},cn=5;
            while(as>0) {
                ar[cn--]=as%2;
                as/=2;
            }
            for(int k=0;k<6;k++)
                xx+=String.valueOf(ar[k]);
            nw+=xx;
            xx="";
        }
        String conv="";
        nw+="9";
        int ln=nw.length(),b=(ln-1)%8;
         for(int u=0;u<b;u++)
             nw="0"+nw;
        for(int k=ln-1;k>=7;k-=8) {
            int mm=Integer.valueOf(nw.substring(k-8,k));
            int cnn=0,sm=0,r;
            while(mm>0) {
                r=mm%10;
                sm+=((int)Math.pow(2,cnn++))*r;
                mm/=10;
            }
            conv=String.valueOf((char)sm)+conv;
        }
        output_text.setText(conv);
     }    
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         buttonGroup1 = new javax.swing.ButtonGroup();
         jRadioButtonMenuItem1 = new javax.swing.JRadioButtonMenuItem();
         jMenu1 = new javax.swing.JMenu();
         jMenuBar1 = new javax.swing.JMenuBar();
         jMenu2 = new javax.swing.JMenu();
         jMenu4 = new javax.swing.JMenu();
         jMenuBar3 = new javax.swing.JMenuBar();
         jMenu5 = new javax.swing.JMenu();
         jMenu6 = new javax.swing.JMenu();
         jPopupMenu1 = new javax.swing.JPopupMenu();
         jMenuBar4 = new javax.swing.JMenuBar();
         jMenu7 = new javax.swing.JMenu();
         jMenu8 = new javax.swing.JMenu();
         jMenuBar5 = new javax.swing.JMenuBar();
         jMenu9 = new javax.swing.JMenu();
         jMenu10 = new javax.swing.JMenu();
         jMenuItem4 = new javax.swing.JMenuItem();
         jPopupMenu2 = new javax.swing.JPopupMenu();
         jDialog1 = new javax.swing.JDialog();
         jMenuItem1 = new javax.swing.JMenuItem();
         jScrollPane1 = new javax.swing.JScrollPane();
         input_text = new javax.swing.JTextArea();
         leveOne_radio = new javax.swing.JRadioButton();
         leveTwo_radio = new javax.swing.JRadioButton();
         leveThree_radio = new javax.swing.JRadioButton();
         jScrollPane2 = new javax.swing.JScrollPane();
         output_text = new javax.swing.JTextArea();
         prw = new javax.swing.JLabel();
         level = new javax.swing.JLabel();
         jSeparator1 = new javax.swing.JSeparator();
         jSeparator3 = new javax.swing.JSeparator();
         action_button = new javax.swing.JComboBox();
         a = new javax.swing.JLabel();
         jLabel1 = new javax.swing.JLabel();
         open_label = new javax.swing.JLabel();
         jScrollPane3 = new javax.swing.JScrollPane();
         open_list = new javax.swing.JTextArea();
         jLabel5 = new javax.swing.JLabel();
         file_no = new javax.swing.JTextField();
         no_inp_button = new javax.swing.JButton();
         jLabel6 = new javax.swing.JLabel();
         jLabel2 = new javax.swing.JLabel();
         jMenuBar6 = new javax.swing.JMenuBar();
         file_menu = new javax.swing.JMenu();
         open_button = new javax.swing.JMenuItem();
         save_button = new javax.swing.JMenuItem();
         user_menu = new javax.swing.JMenu();
         logout_button = new javax.swing.JMenuItem();
         help_button = new javax.swing.JMenuItem();
 
         jRadioButtonMenuItem1.setSelected(true);
         jRadioButtonMenuItem1.setText("jRadioButtonMenuItem1");
 
         jMenu1.setText("jMenu1");
 
         jMenu2.setText("File");
         jMenuBar1.add(jMenu2);
 
         jMenu4.setText("Edit");
         jMenuBar1.add(jMenu4);
 
         jMenu5.setText("File");
         jMenuBar3.add(jMenu5);
 
         jMenu6.setText("Edit");
         jMenuBar3.add(jMenu6);
 
         jMenu7.setText("File");
         jMenuBar4.add(jMenu7);
 
         jMenu8.setText("Edit");
         jMenuBar4.add(jMenu8);
 
         jMenu9.setText("File");
         jMenuBar5.add(jMenu9);
 
         jMenu10.setText("Edit");
         jMenuBar5.add(jMenu10);
 
         jMenuItem4.setText("jMenuItem4");
 
         jPopupMenu2.setOpaque(false);
 
         javax.swing.GroupLayout jDialog1Layout = new javax.swing.GroupLayout(jDialog1.getContentPane());
         jDialog1.getContentPane().setLayout(jDialog1Layout);
         jDialog1Layout.setHorizontalGroup(
             jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 400, Short.MAX_VALUE)
         );
         jDialog1Layout.setVerticalGroup(
             jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 300, Short.MAX_VALUE)
         );
 
         jMenuItem1.setText("jMenuItem1");
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
         setBackground(new java.awt.Color(0, 51, 51));
 
         input_text.setColumns(20);
         input_text.setRows(5);
         input_text.setText("ENTER TEXT");
         jScrollPane1.setViewportView(input_text);
 
         buttonGroup1.add(leveOne_radio);
         leveOne_radio.setText("Easy");
         leveOne_radio.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 leveOne_radioMouseClicked(evt);
             }
         });
 
         buttonGroup1.add(leveTwo_radio);
         leveTwo_radio.setText("Medium");
         leveTwo_radio.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 leveTwo_radioMouseClicked(evt);
             }
         });
 
         buttonGroup1.add(leveThree_radio);
         leveThree_radio.setText("Hard");
         leveThree_radio.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 leveThree_radioMouseClicked(evt);
             }
         });
 
         output_text.setColumns(20);
         output_text.setRows(5);
         output_text.setText("EDITED TEXT");
         jScrollPane2.setViewportView(output_text);
 
         prw.setFont(new java.awt.Font("Baskerville Old Face", 0, 18)); // NOI18N
         prw.setText("Preview:-");
 
         level.setFont(new java.awt.Font("Trajan Pro", 0, 32)); // NOI18N
         level.setText("______");
 
         jSeparator3.setForeground(new java.awt.Color(51, 51, 0));
         jSeparator3.setPreferredSize(new java.awt.Dimension(50, 20));
 
         action_button.setFont(new java.awt.Font("Garamond", 1, 24)); // NOI18N
         action_button.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Encrypt", "Decrypt" }));
         action_button.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 action_buttonActionPerformed(evt);
             }
         });
 
         a.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
         a.setText("____");
 
         jLabel1.setFont(new java.awt.Font("Verdana", 2, 12)); // NOI18N
         jLabel1.setText("Welcome,");
 
         open_label.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         open_label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         open_label.setText("FILE SELECT :");
 
         open_list.setColumns(20);
         open_list.setEditable(false);
         open_list.setLineWrap(true);
         open_list.setRows(5);
         jScrollPane3.setViewportView(open_list);
 
         jLabel5.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         jLabel5.setText("Select File No. :");
 
         no_inp_button.setText("OK");
         no_inp_button.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 no_inp_buttonMouseClicked(evt);
             }
         });
 
         jLabel6.setFont(new java.awt.Font("Tekton Pro Ext", 2, 14)); // NOI18N
         jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         jLabel6.setText("Cipher v1.0.0");
 
         jLabel2.setFont(new java.awt.Font("Trajan Pro", 2, 11)); // NOI18N
         jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         jLabel2.setText("Copyright Reserved");
 
         jMenuBar6.setPreferredSize(new java.awt.Dimension(56, 30));
 
         file_menu.setText("File                                                                                ");
         file_menu.setFont(new java.awt.Font("Segoe UI Light", 1, 18)); // NOI18N
         file_menu.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
         file_menu.setPreferredSize(new java.awt.Dimension(50, 19));
 
         open_button.setText("Open");
         open_button.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 open_buttonActionPerformed(evt);
             }
         });
         file_menu.add(open_button);
 
         save_button.setText("Save");
         save_button.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 save_buttonActionPerformed(evt);
             }
         });
         file_menu.add(save_button);
 
         jMenuBar6.add(file_menu);
 
         user_menu.setText("User");
         user_menu.setAlignmentX(50.0F);
         user_menu.setFont(new java.awt.Font("Segoe UI Light", 1, 18)); // NOI18N
         user_menu.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         user_menu.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
 
         logout_button.setText("Logout");
         logout_button.setName("l");
         logout_button.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 logout_buttonActionPerformed(evt);
             }
         });
         user_menu.add(logout_button);
 
         help_button.setText("About");
         help_button.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 help_buttonActionPerformed(evt);
             }
         });
         user_menu.add(help_button);
 
         jMenuBar6.add(user_menu);
 
         setJMenuBar(jMenuBar6);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jSeparator3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addComponent(jSeparator1)
             .addGroup(layout.createSequentialGroup()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                 .addContainerGap(13, Short.MAX_VALUE)
                                 .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addGroup(layout.createSequentialGroup()
                                 .addGap(26, 26, 26)
                                 .addComponent(open_label, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addGap(0, 0, Short.MAX_VALUE)))
                         .addGap(18, 18, 18))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                         .addGap(0, 0, Short.MAX_VALUE)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addGroup(layout.createSequentialGroup()
                                 .addComponent(file_no, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addGap(18, 18, 18)
                                 .addComponent(no_inp_button)))
                         .addGap(26, 26, 26)))
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(level, javax.swing.GroupLayout.PREFERRED_SIZE, 314, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(action_button, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                         .addComponent(prw, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 462, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                 .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 462, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                     .addGroup(layout.createSequentialGroup()
                                         .addComponent(jLabel1)
                                         .addGap(18, 18, 18)
                                         .addComponent(a, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE))
                                     .addGroup(layout.createSequentialGroup()
                                         .addComponent(leveOne_radio)
                                         .addGap(147, 147, 147)
                                         .addComponent(leveTwo_radio)
                                         .addGap(135, 135, 135)
                                         .addComponent(leveThree_radio)))))))
                 .addGap(28, 28, 28))
             .addGroup(layout.createSequentialGroup()
                 .addGap(273, 273, 273)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(a)
                             .addComponent(jLabel1))
                         .addGap(44, 44, 44)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(leveThree_radio)
                             .addComponent(leveTwo_radio)
                             .addComponent(leveOne_radio)))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                         .addComponent(open_label, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(15, 15, 15)))
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(action_button, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(level))
                         .addGap(8, 8, 8)
                         .addComponent(prw)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(18, 18, 18)
                         .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(file_no, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(no_inp_button))))
                 .addGap(18, 18, 18)
                 .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jLabel2)
                 .addContainerGap(25, Short.MAX_VALUE))
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
     private void leveOne_radioMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_leveOne_radioMouseClicked
         // TODO add your handling code here:
         lev="easy";
         input_text.setLineWrap(true);
         level.setText("Level- EASY ");
         input_text.setText("Supports all characters \n Well almost all");
     }//GEN-LAST:event_leveOne_radioMouseClicked
 
     private void leveTwo_radioMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_leveTwo_radioMouseClicked
         // TODO add your handling code here:
         lev="med";
         input_text.setLineWrap(true);
         level.setText("Level- MEDIUM ");
         input_text.setText("Enter message with policy number. \n Format:- I am a boy#1432 "
                 + "\n POLICY NUMBER SHOULD BE OF DIGITS WIHOUT REPETATION WITHIN THE  LENGTH OF THE POLICY NUMBER."
                 + "\nEg- 312, 4321, 15423 etc");
     }//GEN-LAST:event_leveTwo_radioMouseClicked
 
     private void leveThree_radioMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_leveThree_radioMouseClicked
         // TODO add your handling code here:
         lev="hard";
         input_text.setLineWrap(true);
         input_text.setText("Note:- Supports only numbers and letters.");
     }//GEN-LAST:event_leveThree_radioMouseClicked
 
     private void action_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_action_buttonActionPerformed
         // TODO add your handling code here:
         int slct=action_button.getSelectedIndex();
         output_text.setLineWrap(true);
         
         if(slct==0) {
             prw.setText("Encryted Text is:-");
             if(leveOne_radio.isSelected())
                 encodeone();
             if(leveTwo_radio.isSelected())
                 encodetwo();
             if(leveThree_radio.isSelected())
                 encodethree();
         }
         else {
             prw.setText("Decryted Text is:-");
             if(leveOne_radio.isSelected())
                 decodeone();
             if(leveTwo_radio.isSelected())
                 decodetwo();
             if(leveThree_radio.isSelected())
                 decodethree();
         }
     }//GEN-LAST:event_action_buttonActionPerformed
 
     private void logout_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logout_buttonActionPerformed
         // TODO add your handling code here:
         new LoginFrame().setVisible(true);
         dispose();
     }//GEN-LAST:event_logout_buttonActionPerformed
     
     
     public void mouseEntered(MouseEvent e) {
         fld = (JTextField) e.getSource();
     }
     
     private void save_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_save_buttonActionPerformed
         // TODO add your handling code here:
         System.out.print(user);
         new SaveFile(user, lev, output_text.getText()).setVisible(true);
     }//GEN-LAST:event_save_buttonActionPerformed
 
     private void open_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_open_buttonActionPerformed
         // TODO add your handling code here:
         int c=0;
         String str;
         open_list.setVisible(true);
         open_label.setVisible(true);
         
        
         try {
            //read the user's list of file names
             FileReader fin=new FileReader("D:/Cipher/"+user+"_files.txt");
             BufferedReader in=new BufferedReader(fin);
             
            //form list array
             while((str=in.readLine())!= null) {
                 list[c++]=str;
             }
            
            //forming file_name_list to be opened for the given user
             str="";
             for(int i=0;i<c;i++) 
                 str+=(i+1)+". "+list[i].substring(0,list[i].indexOf('+'))+"\n";
             
             open_list.setText(str);
         } catch(Exception e) {
             open_list.setText("!!ERROR!!\nNO File\nFound");
         }
         
     }//GEN-LAST:event_open_buttonActionPerformed
 
     private void help_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_help_buttonActionPerformed
         // TODO add your handling code here:
         new About(user).setVisible(true);
         
     }//GEN-LAST:event_help_buttonActionPerformed
 
     private void no_inp_buttonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_no_inp_buttonMouseClicked
         // TODO add your handling code here:
         String a,file_name,str="";
         
         try {
             file_name=list[Integer.parseInt(file_no.getText())-1];
             FileReader fin=new FileReader("D:/Cipher/"+file_name+".txt");
             BufferedReader in=new BufferedReader(fin);
             
            String level=file_name.substring(file_name.lastIndexOf('+')+1,file_name.length());
            System.out.println(level);
             if(level.equals("easy"))
                 leveOne_radio.setSelected(true);
             else if(level.equals("med"))
                 leveTwo_radio.setSelected(true);
             else
                 leveThree_radio.setSelected(true);
             
             while((a=in.readLine())!= null) {
                 str+=a;
             }
             
             output_text.setText("");
             input_text.setText(str);
         } catch(Exception e) {
             input_text.setText("!!ERROR!!\nNO File\nFound");
             output_text.setText("");
         }     
     }//GEN-LAST:event_no_inp_buttonMouseClicked
     
     public static void main(String args[]) {
         
         //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         /*
          * If Nimbus (introduced in Java SE 6) is not available, stay with the
          * default look and feel. For details see
          * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
          */
         try {
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
 
         java.awt.EventQueue.invokeLater(new Runnable() {
 
             public void run() {
                  new MainFrame(user).setVisible(true);
             }
         });
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JLabel a;
     private javax.swing.JComboBox action_button;
     private javax.swing.ButtonGroup buttonGroup1;
     private javax.swing.JMenu file_menu;
     private javax.swing.JTextField file_no;
     private javax.swing.JMenuItem help_button;
     private javax.swing.JTextArea input_text;
     private javax.swing.JDialog jDialog1;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JMenu jMenu1;
     private javax.swing.JMenu jMenu10;
     private javax.swing.JMenu jMenu2;
     private javax.swing.JMenu jMenu4;
     private javax.swing.JMenu jMenu5;
     private javax.swing.JMenu jMenu6;
     private javax.swing.JMenu jMenu7;
     private javax.swing.JMenu jMenu8;
     private javax.swing.JMenu jMenu9;
     private javax.swing.JMenuBar jMenuBar1;
     private javax.swing.JMenuBar jMenuBar3;
     private javax.swing.JMenuBar jMenuBar4;
     private javax.swing.JMenuBar jMenuBar5;
     private javax.swing.JMenuBar jMenuBar6;
     private javax.swing.JMenuItem jMenuItem1;
     private javax.swing.JMenuItem jMenuItem4;
     private javax.swing.JPopupMenu jPopupMenu1;
     private javax.swing.JPopupMenu jPopupMenu2;
     private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem1;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JScrollPane jScrollPane3;
     private javax.swing.JSeparator jSeparator1;
     private javax.swing.JSeparator jSeparator3;
     private javax.swing.JRadioButton leveOne_radio;
     private javax.swing.JRadioButton leveThree_radio;
     private javax.swing.JRadioButton leveTwo_radio;
     private javax.swing.JLabel level;
     private javax.swing.JMenuItem logout_button;
     private javax.swing.JButton no_inp_button;
     private javax.swing.JMenuItem open_button;
     private javax.swing.JLabel open_label;
     private javax.swing.JTextArea open_list;
     private javax.swing.JTextArea output_text;
     private javax.swing.JLabel prw;
     private javax.swing.JMenuItem save_button;
     private javax.swing.JMenu user_menu;
     // End of variables declaration//GEN-END:variables
 
 }
