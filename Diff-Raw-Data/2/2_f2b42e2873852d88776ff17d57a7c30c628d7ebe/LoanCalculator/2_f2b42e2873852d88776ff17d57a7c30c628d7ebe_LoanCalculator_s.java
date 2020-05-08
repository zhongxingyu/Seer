 /*
  * $Id$
  *
  * Copyright 2012 Valentyn Kolesnikov
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.github.loancalculator;
 
 import com.github.loancalculator.PaymentCalculator.Payment;
 import com.google.gson.Gson;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.AbstractMap.SimpleEntry;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 public class LoanCalculator extends javax.swing.JFrame {
     private Gson jsonObject = new Gson();
     private Json json;
 
     public static class Json {
         public Insurance[] insurance_cf_special;
         public Insurance[] insurance_cf;
         public Long[] insurance_terms;
         public Product[] products;
     }
 
     public enum POS {
         Comfy("cf"),
         Euroset("Евросеть"),
         Allo("Алло"),
         Orus("ОРУС"),
         Bt("БТ"),
         Chisto("Чистотехника"),
         Dm("ДМ"),
         Margo("Марго"),
         Moyo("moyo"),
         Nn("nn"),
         Apple("apple"),
         AC("АЦ"),
         Technopolis("Технополис"),
         Techno("Техно_");
 
         private String mask;
         private POS(String mask) {
             this.mask = mask;
         }
 
         public String getMask() {
             return mask;
         }
         public static POS getPOSforProduct(Product product) {
             for (POS pos : values()) {
                 if (product.name.contains(pos.getMask())) {
                     return pos;
                 }
             }
             return null;
         }
     }
 
     public static class Product implements Comparable {
         public int id;
         public String name;
         public Long month_min;
         public Long month_max;
         public Double minimal_credit;
         public Double maximal_credit;
         public Double rate;
         public Double first_fee_into;
         public Double monthly_fee;
         public Double minimal_first_charge;
         public Double additional_service_fee;
         public POS pos;
 
         @Override
         public String toString() {
             return name + " (от " + month_min + " до " + month_max + " мес.)";
         }
 
         public int compareTo(Object o) {
             return -Integer.valueOf(((Product) o).id).compareTo(id);
         }
     }
 
     private void setupDurations(Product product) {
         List<SimpleEntry<Long, String>> durations = new ArrayList<SimpleEntry<Long, String>>();
         for (long duration = product.month_min; duration <= product.month_max; duration += 1) {
             durations.add(new SimpleEntry<Long, String>(duration, duration + " мес.") {
                 @Override
                 public String toString() {
                     return getValue();
                 }
             });
         }
         jComboBox4.setModel(new DefaultComboBoxModel(durations.toArray()));
     }
     
     public class Insurance {
         public String name;
         public Double tariff;
 
         @Override
         public String toString() {
             return name;
         }
     }
     
 
     /** Creates new form Find */
     public LoanCalculator() {
         setLookAndFeel();
         initComponents();
         loadJson();
         fillModels();
         doCalculateRepayment();
     }
     
     private void fillModels() {
         Arrays.sort(json.products);
         jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(json.products));
         jComboBox1.setSelectedIndex(0);
         jComboBox3.setModel(new javax.swing.DefaultComboBoxModel(json.insurance_cf));
         jComboBox4.setSelectedIndex(0);
         Set<POS> poses = new LinkedHashSet<POS>();
         poses.add(null);
         for (Product product : json.products) {
             product.pos = POS.getPOSforProduct(product);
             if (product.pos != null) {
                 poses.add(product.pos);
             }
         }
         jComboBox5.setModel(new javax.swing.DefaultComboBoxModel(poses.toArray()));
     }
     
     private String readFile() throws IOException {
         BufferedReader reader = null;
         try {
             reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("LoanCalculator-config.json"), "UTF-8"));
             String line  = null;
             StringBuilder stringBuilder = new StringBuilder();
             String ls = System.getProperty("line.separator");
             while(( line = reader.readLine() ) != null) {
                 stringBuilder.append( line );
                 stringBuilder.append( ls );
             }
             return stringBuilder.toString();
         } finally{
             try {
                 reader.close();
             } catch (IOException ex) {
                 Logger.getLogger(LoanCalculator.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 
     private void loadJson() {
         try {
             /** Read the contents of the given file. */
             String fileData = readFile().replaceAll("\\n\\s*(\"\\w+\") :(\\r|\\n)\\s*\\{","\n{\"id\": $1,");
             json = jsonObject.fromJson(fileData, Json.class);
         } catch (IOException ex) {
             Logger.getLogger(LoanCalculator.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     private static void setLookAndFeel() {
         javax.swing.UIManager.LookAndFeelInfo[] infos = UIManager.getInstalledLookAndFeels();
         String firstFoundClass = null;
         for (javax.swing.UIManager.LookAndFeelInfo info : infos) {
             String foundClass = info.getClassName();
            if ("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel".equals(foundClass)) {
                 firstFoundClass = foundClass;
                 break;
             }
             if (null == firstFoundClass) {
                 firstFoundClass = foundClass;
             }
         }
 
         if (null == firstFoundClass) {
             throw new IllegalArgumentException("No suitable Swing looks and feels");
         } else {
             try {
                 UIManager.setLookAndFeel(firstFoundClass);
             } catch (ClassNotFoundException ex) {
                 Logger.getLogger(LoanCalculator.class.getName()).log(Level.SEVERE, null, ex);
             } catch (InstantiationException ex) {
                 Logger.getLogger(LoanCalculator.class.getName()).log(Level.SEVERE, null, ex);
             } catch (IllegalAccessException ex) {
                 Logger.getLogger(LoanCalculator.class.getName()).log(Level.SEVERE, null, ex);
             } catch (UnsupportedLookAndFeelException ex) {
                 Logger.getLogger(LoanCalculator.class.getName()).log(Level.SEVERE, null, ex);
             }
             return;
         }
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jTabbedPane1 = new javax.swing.JTabbedPane();
         jPanel2 = new javax.swing.JPanel();
         jComboBox4 = new javax.swing.JComboBox();
         jLabel11 = new javax.swing.JLabel();
         jPanel1 = new javax.swing.JPanel();
         jLabel7 = new javax.swing.JLabel();
         jLabel8 = new javax.swing.JLabel();
         jLabel10 = new javax.swing.JLabel();
         jTextField3 = new javax.swing.JTextField();
         jTextField4 = new javax.swing.JTextField();
         jTextField5 = new javax.swing.JTextField();
         jLabel12 = new javax.swing.JLabel();
         jTextField6 = new javax.swing.JTextField();
         jComboBox5 = new javax.swing.JComboBox();
         jTextField2 = new javax.swing.JTextField();
         jLabel6 = new javax.swing.JLabel();
         jTextField1 = new javax.swing.JTextField();
         jLabel5 = new javax.swing.JLabel();
         jLabel1 = new javax.swing.JLabel();
         jLabel4 = new javax.swing.JLabel();
         jComboBox2 = new javax.swing.JComboBox();
         jLabel2 = new javax.swing.JLabel();
         jComboBox1 = new javax.swing.JComboBox();
         jComboBox3 = new javax.swing.JComboBox();
         jLabel3 = new javax.swing.JLabel();
         jPanel3 = new javax.swing.JPanel();
         jScrollPane1 = new javax.swing.JScrollPane();
         jTable1 = new javax.swing.JTable();
         jPanel4 = new javax.swing.JPanel();
         jLabel9 = new javax.swing.JLabel();
         jLabel13 = new javax.swing.JLabel();
         jLabel14 = new javax.swing.JLabel();
         jLabel15 = new javax.swing.JLabel();
         jLabel16 = new javax.swing.JLabel();
         jLabel17 = new javax.swing.JLabel();
         jLabel18 = new javax.swing.JLabel();
         jLabel19 = new javax.swing.JLabel();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("Кредитный калькулятор \"Моментальный кредит\"");
 
         jTabbedPane1.setBackground(new java.awt.Color(239, 239, 239));
         jTabbedPane1.setTabPlacement(javax.swing.JTabbedPane.RIGHT);
         jTabbedPane1.setFont(new java.awt.Font("Tahoma", 0, 18));
         jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 jTabbedPane1StateChanged(evt);
             }
         });
 
         jPanel2.setBackground(new java.awt.Color(239, 239, 239));
 
         jComboBox4.setFont(new java.awt.Font("Tahoma", 0, 14));
         jComboBox4.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 jComboBox4ItemStateChanged(evt);
             }
         });
         jComboBox4.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jComboBox4ActionPerformed(evt);
             }
         });
 
         jLabel11.setFont(new java.awt.Font("Tahoma", 0, 14));
         jLabel11.setText("Точка продаж");
 
         jPanel1.setBackground(new java.awt.Color(239, 239, 239));
         jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
 
         jLabel7.setFont(new java.awt.Font("Tahoma", 0, 14));
         jLabel7.setText("Сумма кредита");
 
         jLabel8.setFont(new java.awt.Font("Tahoma", 0, 14));
         jLabel8.setText("Сумма страхового платежа");
 
         jLabel10.setFont(new java.awt.Font("Tahoma", 0, 14));
         jLabel10.setText("Сумма платежа за месяц");
 
         jTextField3.setFont(new java.awt.Font("Tahoma", 0, 14));
         jTextField3.setText("0,00");
 
         jTextField4.setFont(new java.awt.Font("Tahoma", 0, 14));
         jTextField4.setText("0,00");
 
         jTextField5.setFont(new java.awt.Font("Tahoma", 0, 14));
         jTextField5.setText("0,00");
 
         jLabel12.setFont(new java.awt.Font("Tahoma", 0, 14));
         jLabel12.setText("Эффективная проц. ставка");
 
         jTextField6.setFont(new java.awt.Font("Tahoma", 0, 14));
         jTextField6.setText("0,00");
 
         org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(jPanel1Layout.createSequentialGroup()
                         .add(jLabel10, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
                         .add(17, 17, 17))
                     .add(jPanel1Layout.createSequentialGroup()
                         .add(jLabel8, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                     .add(jPanel1Layout.createSequentialGroup()
                         .add(jLabel7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                     .add(jPanel1Layout.createSequentialGroup()
                         .add(jLabel12, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE)
                         .add(18, 18, 18)))
                 .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, jTextField6)
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, jTextField5)
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, jTextField4)
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, jTextField3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 567, Short.MAX_VALUE))
                 .addContainerGap())
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                 .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jTextField3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(jLabel7))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jTextField4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(jLabel8))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jTextField5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(jLabel10))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel12)
                     .add(jTextField6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(26, Short.MAX_VALUE))
         );
 
         jComboBox5.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
         jComboBox5.setMaximumRowCount(15);
         jComboBox5.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jComboBox5ActionPerformed(evt);
             }
         });
 
         jTextField2.setFont(new java.awt.Font("Tahoma", 0, 14));
         jTextField2.setText("100");
         jTextField2.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 jTextField2KeyReleased(evt);
             }
         });
 
         jLabel6.setFont(new java.awt.Font("Tahoma", 0, 14));
         jLabel6.setText("Срок кредитования");
 
         jTextField1.setFont(new java.awt.Font("Tahoma", 0, 14));
         jTextField1.setText("1100");
         jTextField1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jTextField1ActionPerformed(evt);
             }
         });
         jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 jTextField1KeyReleased(evt);
             }
         });
 
         jLabel5.setFont(new java.awt.Font("Tahoma", 0, 14));
         jLabel5.setText("Первый взнос");
 
         jLabel1.setFont(new java.awt.Font("Tahoma", 0, 14));
         jLabel1.setText("Вид кредитного продукта");
 
         jLabel4.setFont(new java.awt.Font("Tahoma", 0, 14));
         jLabel4.setText("Стоимость товара");
 
         jComboBox2.setFont(new java.awt.Font("Tahoma", 0, 14));
         jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Сумму платежа в месяц" }));
 
         jLabel2.setFont(new java.awt.Font("Tahoma", 0, 14));
         jLabel2.setText("Расчитать");
 
         jComboBox1.setFont(new java.awt.Font("Tahoma", 0, 14));
         jComboBox1.setMaximumRowCount(20);
         jComboBox1.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 jComboBox1ItemStateChanged(evt);
             }
         });
         jComboBox1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jComboBox1ActionPerformed(evt);
             }
         });
 
         jComboBox3.setFont(new java.awt.Font("Tahoma", 0, 14));
         jComboBox3.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 jComboBox3ItemStateChanged(evt);
             }
         });
 
         jLabel3.setFont(new java.awt.Font("Tahoma", 0, 14));
         jLabel3.setText("С учетом страховки");
 
         org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
         jPanel2.setLayout(jPanel2Layout);
         jPanel2Layout.setHorizontalGroup(
             jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 1020, Short.MAX_VALUE)
             .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                 .add(jPanel2Layout.createSequentialGroup()
                     .add(62, 62, 62)
                     .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                         .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .add(jPanel2Layout.createSequentialGroup()
                             .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                 .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                     .add(jPanel2Layout.createSequentialGroup()
                                         .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                             .add(jLabel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                             .add(jLabel3))
                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                                     .add(jPanel2Layout.createSequentialGroup()
                                         .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                             .add(jLabel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)
                                             .add(jLabel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)
                                             .add(jPanel2Layout.createSequentialGroup()
                                                 .add(jLabel6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE)
                                                 .add(18, 18, 18)))
                                         .add(10, 10, 10))
                                     .add(jPanel2Layout.createSequentialGroup()
                                         .add(jLabel1)
                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                                 .add(jPanel2Layout.createSequentialGroup()
                                     .add(jLabel11)
                                     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                             .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                 .add(org.jdesktop.layout.GroupLayout.LEADING, jComboBox4, 0, 632, Short.MAX_VALUE)
                                 .add(org.jdesktop.layout.GroupLayout.LEADING, jTextField2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 632, Short.MAX_VALUE)
                                 .add(org.jdesktop.layout.GroupLayout.LEADING, jTextField1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 632, Short.MAX_VALUE)
                                 .add(org.jdesktop.layout.GroupLayout.LEADING, jComboBox3, 0, 632, Short.MAX_VALUE)
                                 .add(org.jdesktop.layout.GroupLayout.LEADING, jComboBox1, 0, 632, Short.MAX_VALUE)
                                 .add(jComboBox2, 0, 632, Short.MAX_VALUE)
                                 .add(jPanel2Layout.createSequentialGroup()
                                     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                     .add(jComboBox5, 0, 632, Short.MAX_VALUE)))))
                     .add(62, 62, 62)))
         );
         jPanel2Layout.setVerticalGroup(
             jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 511, Short.MAX_VALUE)
             .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                 .add(jPanel2Layout.createSequentialGroup()
                     .add(21, 21, 21)
                     .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                         .add(jComboBox5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 35, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .add(jLabel11))
                     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                     .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                         .add(jComboBox1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 32, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .add(jLabel1))
                     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                     .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                         .add(jLabel2)
                         .add(jComboBox2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 32, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                     .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                         .add(jLabel3)
                         .add(jComboBox3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 32, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                     .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                     .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                         .add(jLabel4)
                         .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                     .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                         .add(jLabel5)
                         .add(jTextField2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                     .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                     .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                         .add(jLabel6)
                         .add(jComboBox4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 32, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                     .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                     .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .addContainerGap(27, Short.MAX_VALUE)))
         );
 
         jTabbedPane1.addTab("Расчитать", jPanel2);
 
         jPanel3.setBackground(new java.awt.Color(239, 239, 239));
 
         jScrollPane1.setBackground(new java.awt.Color(239, 239, 239));
 
         jTable1.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
         jTable1.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null, null, null, null, null, null},
                 {null, null, null, null, null, null, null, null, null},
                 {null, null, null, null, null, null, null, null, null},
                 {null, null, null, null, null, null, null, null, null}
             },
             new String [] {
                 "Дата платежа", "Сумма платежа за расчетный период, грн", "Погашение основоной суммы кредита", "Проценты за пользование кредитом", "Комиссия за управления кредитом в первый месяц обслуживания, грн", "Комиссия за управления кредитом (ежемесячная), грн", "Платежи в пользу третьих сторон, связанные со страховкой, грн.", "Реальная процентная ставка, %", "Абсолютное значение подорожания кредита, грн"
             }
         ) {
             Class[] types = new Class [] {
                 java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
             };
             boolean[] canEdit = new boolean [] {
                 false, false, false, false, false, false, false, false, false
             };
 
             public Class getColumnClass(int columnIndex) {
                 return types [columnIndex];
             }
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         jTable1.getTableHeader().setReorderingAllowed(false);
         jScrollPane1.setViewportView(jTable1);
 
         jPanel4.setBackground(new java.awt.Color(239, 239, 239));
 
         jLabel9.setFont(new java.awt.Font("Tahoma", 0, 18));
         jLabel9.setText("Кредит банка");
 
         jLabel13.setFont(new java.awt.Font("Tahoma", 0, 18));
         jLabel13.setText("Переплата");
 
         jLabel14.setFont(new java.awt.Font("Tahoma", 0, 18));
         jLabel14.setText("Сумма выплат");
 
         jLabel15.setFont(new java.awt.Font("Tahoma", 0, 18));
         jLabel15.setText("Срок выплат");
 
         jLabel16.setFont(new java.awt.Font("Tahoma", 0, 14));
         jLabel16.setText("1000,00");
 
         jLabel17.setFont(new java.awt.Font("Tahoma", 0, 14));
         jLabel17.setText("1089,00");
 
         jLabel18.setFont(new java.awt.Font("Tahoma", 0, 14));
         jLabel18.setText("89,00");
 
         jLabel19.setFont(new java.awt.Font("Tahoma", 0, 14));
         jLabel19.setText("12 месяцев");
 
         org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
         jPanel4.setLayout(jPanel4Layout);
         jPanel4Layout.setHorizontalGroup(
             jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel4Layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(jLabel14)
                     .add(jLabel13)
                     .add(jLabel15)
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
                         .add(jLabel9, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 126, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .add(6, 6, 6)))
                 .add(28, 28, 28)
                 .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                     .add(jLabel16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 47, Short.MAX_VALUE)
                     .add(jLabel17, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .add(jLabel18, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .add(jLabel19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 207, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(582, Short.MAX_VALUE))
         );
         jPanel4Layout.setVerticalGroup(
             jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel4Layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel9)
                     .add(jLabel16))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel14)
                     .add(jLabel17))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel13)
                     .add(jLabel18))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel15)
                     .add(jLabel19))
                 .addContainerGap(50, Short.MAX_VALUE))
         );
 
         org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
         jPanel3.setLayout(jPanel3Layout);
         jPanel3Layout.setHorizontalGroup(
             jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel3Layout.createSequentialGroup()
                 .add(51, 51, 51)
                 .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .add(jPanel3Layout.createSequentialGroup()
                         .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 886, Short.MAX_VALUE)
                         .add(73, 73, 73)))
                 .addContainerGap())
         );
         jPanel3Layout.setVerticalGroup(
             jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel3Layout.createSequentialGroup()
                 .add(26, 26, 26)
                 .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
 
         jTabbedPane1.addTab("График платежей", jPanel3);
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .add(jTabbedPane1)
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jTabbedPane1)
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void jComboBox4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox4ActionPerformed
 
     }//GEN-LAST:event_jComboBox4ActionPerformed
 
     private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
         javax.swing.ComboBoxModel model = ((javax.swing.JComboBox) evt.getSource()).getModel();
         Product product = (Product) model.getSelectedItem();
         setupDurations(product);
     }//GEN-LAST:event_jComboBox1ActionPerformed
 
     private void jComboBox5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox5ActionPerformed
         javax.swing.ComboBoxModel model = ((javax.swing.JComboBox) evt.getSource()).getModel();
         POS pos = (POS) model.getSelectedItem();
         List<Product> selectedProducts = new ArrayList<Product>();
         for (Product product : json.products) {
             if (pos == null || product.pos == pos) {
                 selectedProducts.add(product);
             }
         }
         jComboBox1.setModel(new DefaultComboBoxModel(selectedProducts.toArray()));
         setupDurations(selectedProducts.get(0));
         doCalculateRepayment();      
     }//GEN-LAST:event_jComboBox5ActionPerformed
 
     private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane1StateChanged
     }//GEN-LAST:event_jTabbedPane1StateChanged
 
     private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
     }//GEN-LAST:event_jTextField1ActionPerformed
 
     private void jTextField1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyReleased
         doCalculateRepayment();
     }//GEN-LAST:event_jTextField1KeyReleased
 
     private void jTextField2KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField2KeyReleased
         doCalculateRepayment();
     }//GEN-LAST:event_jTextField2KeyReleased
 
     private void jComboBox1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox1ItemStateChanged
         javax.swing.ComboBoxModel model = ((javax.swing.JComboBox) evt.getSource()).getModel();
         Product product = (Product) model.getSelectedItem();
         setupDurations(product);
         doCalculateRepayment();
     }//GEN-LAST:event_jComboBox1ItemStateChanged
 
     private void jComboBox3ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox3ItemStateChanged
         doCalculateRepayment();
     }//GEN-LAST:event_jComboBox3ItemStateChanged
 
     private void jComboBox4ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox4ItemStateChanged
         doCalculateRepayment();
     }//GEN-LAST:event_jComboBox4ItemStateChanged
     
     private void doCalculateRepayment() {
         // initialization
         if (jComboBox4.getSelectedItem() != null) {
             Double cost = Double.valueOf(jTextField1.getText().replace(',', '.'));
             Long term = ((SimpleEntry<Long, String>) jComboBox4.getSelectedItem()).getKey();
             Double first_charge = Double.valueOf(jTextField2.getText().replace(',', '.'));
             Product product = (Product) jComboBox1.getSelectedItem();
             Insurance insurance = (Insurance) jComboBox3.getSelectedItem();
             Double insurance_payment = calc_insurance(product, insurance, cost-first_charge, term);
             if (first_charge < product.minimal_first_charge/100*cost) {
     //            alert('First charge is too low: ' + first_charge + ' vs. ' + product.minimal_first_charge/100*cost); return
                         }
             // Changed by Kudar's request 06.02.2009: credit =  ((aCost-aFirstCharge) + insurance_payment) / (1 - aFirstFee / 100) 
             Double credit = calc_full_credit(cost + insurance_payment, first_charge, product.first_fee_into, term);
 
             if (credit > product.maximal_credit) {
     //            alert('Credit is too large: ' + cost + ' vs. ' + product.maximal_credit); return
             }
             if (credit < product.minimal_credit) {
     //            alert('Credit is too low: ' + cost + ' vs. ' + product.minimal_credit);  return
             }
             Double monthly_rate = product.rate/100/12;
             double monthly_charge = calc_monthly_charge(credit, monthly_rate, term);
             monthly_charge += product.monthly_fee/100*credit;
 
             // OUTPUT
             jTextField3.setText(new DecimalFormat("0.00").format(credit));
             jLabel16.setText(new DecimalFormat("0.00").format(credit));
             jTextField5.setText(new DecimalFormat("0.00").format(monthly_charge));
             jTextField4.setText(new DecimalFormat("0.00").format(insurance_payment));
             long duration = ((SimpleEntry<Long, String>) jComboBox4.getSelectedItem()).getKey();
             jLabel17.setText(new DecimalFormat("0.00").format(monthly_charge * duration));
             jLabel18.setText(new DecimalFormat("0.00").format(monthly_charge * duration - credit) + " ("
                 + new DecimalFormat("0.00").format((monthly_charge * duration - credit) * 100 / (monthly_charge * duration))
                 + "%)");
             jLabel19.setText(new DecimalFormat("0").format(duration) + " месяцев");
             List<Double> days = new ArrayList<Double>();
             List<Double> payments = new ArrayList<Double>();
             payments.add(-credit);
             days.add(0D);
             for (int index = 0; index < duration; index += 1) {
                 days.add((index +1) * 30.59);
                 payments.add(Math.ceil(monthly_charge*100)/100);
             }
             double xirr = new Xirr().calc(0.1, payments, days);
             jTextField6.setText(new DecimalFormat("0.00%").format(xirr));
             PaymentCalculator.PaymentInput paymentInput = new PaymentCalculator.PaymentInput();
             paymentInput.duration = duration;
             paymentInput.insurancePayment = insurance_payment;
             paymentInput.amountOfLoan = credit;
             paymentInput.rate = product.rate;
             paymentInput.openingFee = product.first_fee_into;
             paymentInput.monthlyFee = product.monthly_fee;
             paymentInput.monthly_charge = monthly_charge;
             List<Payment> tablePayments = new PaymentCalculator(paymentInput).calc();
             ((javax.swing.table.DefaultTableModel) jTable1.getModel()).setRowCount(tablePayments.size());
             int index = 0;
             for (Payment payment : tablePayments) {
                 if (index == tablePayments.size() - 1) {
                     jTable1.getModel().setValueAt("Всего", index, 0);
                 } else if (payment.date != null) {
                     jTable1.getModel().setValueAt(new SimpleDateFormat("dd.MM.yyyy").format(payment.date), index, 0);
                 }
                 jTable1.getModel().setValueAt(new DecimalFormat("0.00").format(payment.totalInstalmentPayment), index, 1);
                 jTable1.getModel().setValueAt(new DecimalFormat("0.00").format(payment.capitalPayment), index, 2);
                 jTable1.getModel().setValueAt(new DecimalFormat("0.00").format(payment.interestPayment), index, 3);
                 jTable1.getModel().setValueAt(new DecimalFormat("0.00").format(payment.openingFee), index, 4);
                 jTable1.getModel().setValueAt(new DecimalFormat("0.00").format(payment.monthlyFeePayment), index, 5);
                 jTable1.getModel().setValueAt(new DecimalFormat("0.00").format(payment.insurancePayment), index, 6);
                 jTable1.getModel().setValueAt(new DecimalFormat("0.00%").format(xirr), index, 7);
                 jTable1.getModel().setValueAt(new DecimalFormat("0.00").format(monthly_charge * duration - credit), index, 8);
                 index += 1;
             }
         }
     }
 
     private double calc_full_credit(double aCost, double aFirstCharge, double aFirstFee, long term) {
         return (aCost-aFirstCharge)/(1-aFirstFee/100);
     }
 
     private Double calc_insurance(Product product,
             Insurance insurance, Double credit, Long term) {
         if (insurance == null) {
             return 0D;
         }
         Long round_term = json.insurance_terms[0];
         for (int i = 0; i < json.insurance_terms.length; i++) {
             if (json.insurance_terms[i] >= term) {
                 return credit * insurance.tariff * json.insurance_terms[i];
             }
             round_term = json.insurance_terms[i];
         }
         return credit * insurance.tariff * term;
     }
 
     private double calc_monthly_charge(double credit, double rate, long term) {
         double res = credit * rate * Math.pow((1 + rate), term) / (Math.pow((1 + rate), term) - 1);
         return res;
     }
 
     /**
      * @param args the command line arguments
      */
     public static void main(String args[]) {
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 new LoanCalculator().setVisible(true);
             }
         });
     }
     
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JComboBox jComboBox1;
     private javax.swing.JComboBox jComboBox2;
     private javax.swing.JComboBox jComboBox3;
     private javax.swing.JComboBox jComboBox4;
     private javax.swing.JComboBox jComboBox5;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel10;
     private javax.swing.JLabel jLabel11;
     private javax.swing.JLabel jLabel12;
     private javax.swing.JLabel jLabel13;
     private javax.swing.JLabel jLabel14;
     private javax.swing.JLabel jLabel15;
     private javax.swing.JLabel jLabel16;
     private javax.swing.JLabel jLabel17;
     private javax.swing.JLabel jLabel18;
     private javax.swing.JLabel jLabel19;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JLabel jLabel8;
     private javax.swing.JLabel jLabel9;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JPanel jPanel4;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JTabbedPane jTabbedPane1;
     private javax.swing.JTable jTable1;
     private javax.swing.JTextField jTextField1;
     private javax.swing.JTextField jTextField2;
     private javax.swing.JTextField jTextField3;
     private javax.swing.JTextField jTextField4;
     private javax.swing.JTextField jTextField5;
     private javax.swing.JTextField jTextField6;
     // End of variables declaration//GEN-END:variables
     
 }
