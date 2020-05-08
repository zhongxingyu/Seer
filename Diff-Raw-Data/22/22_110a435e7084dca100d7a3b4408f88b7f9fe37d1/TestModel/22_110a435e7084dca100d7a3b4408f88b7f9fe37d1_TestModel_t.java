 /*
  * ====================================================================
  * This software is subject to the terms of the Common Public License
  * Agreement, available at the following URL:
  *   http://www.opensource.org/licenses/cpl.html .
  * Copyright (C) 2003-2004 TONBELLER AG.
  * All Rights Reserved.
  * You must accept the terms of that agreement to use this software.
  * ====================================================================
  *
  * 
  */
 package com.tonbeller.wcf.table;
 
import java.text.DecimalFormat;
 import java.util.Date;
 import java.util.Random;
 
 /**
  * a table model for testing
  */
 
 public class TestModel extends AbstractTableModel {
   String[] columnTitles = { "String", "Date", "Double", "Integer", "Click Me", "Image 1", "Image 2" };
   TableRow[] rows;
   Random rnd = new Random(1234527L);
   String title = "Test Table model";
 
   public TestModel() {
     int N = 20;
     rows = new TableRow[N];
    DecimalFormat df = new DecimalFormat("00");
     for (int i = 0; i < N; i++) {
       DefaultCell h1 = new DefaultCell("/", new Integer(rnd.nextInt(1000)), null);
       DefaultCell h2 = new DefaultCell("/", null, nextImg());
       DefaultCell h3 = new DefaultCell("/", "Text " + rnd.nextInt(100), nextImg());
      String s = "2002-02-" + df.format(i + 1);
       Object[] values = new Object[] {
         "Hello World: " + i,
        java.sql.Date.valueOf(s),
         new Double((double)rnd.nextInt(10000) / (double)rnd.nextInt(100) ),
         new Integer(rnd.nextInt(1000)),
         h1, h2, h3
       };
       rows[i] = new DefaultTableRow(values);
     }
   }
 
   public String nextImg() {
     if (rnd.nextBoolean())
       return "wcf/table/sort-ac.png";
     return "wcf/table/sort-dn.png";
   }
 
   public int getRowCount() {
     return rows.length;
   }
   public TableRow getRow(int rowIndex) {
     return rows[rowIndex];
   }
   public int getColumnCount() {
     return columnTitles.length;
   }
   public String getColumnTitle(int columnIndex) {
     return columnTitles[columnIndex];
   }
   public String getTitle() {
     return title;
   }
   public void setTitle(String string) {
     title = string;
   }
 
 }
