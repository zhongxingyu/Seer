 /*
  * creator : Latief Al Amin
  * e-mail  : al _amin_o4_032@yahoo.co.id
  * create  : Oct 2, 2010
  */
 package com.secondstack.swing.table;
 
 import com.secondstack.swing.engine.BeanClass;
 import com.secondstack.swing.enumeration.EnumMonth;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.swing.table.AbstractTableModel;
 
 /**
  *
  * @author Latief Al Amin
  */
 public class BeanTableModel extends AbstractTableModel {
 
     private List beanList;
     private Map<String, Object> mapBeanList;
     private String[] columnNames;
     private boolean[] columnEditable;
     private Class[] columnClass;
     private boolean[] columnVisible;
     private boolean sortAscDsc = true;
     /**
      * Apakah ditambahkan fitur check di TableModel?
      */
     private boolean withCheck = false;
     private String checkColumnName = "Check";
     private Boolean[] checkValue;
     private boolean checkAll = false;
 
     public BeanTableModel() {
     }
 
     public BeanTableModel(List beanList, String[] columnNames) {
         this.beanList = beanList;
         this.columnNames = columnNames;
     }
 
     public BeanTableModel(List beanList, String[] columnNames, boolean[] columnEditable) {
         this.beanList = beanList;
         this.columnNames = columnNames;
         this.columnEditable = columnEditable;
     }
 
     public BeanTableModel(List beanList, String[] columnNames, boolean[] columnEditable, Class[] columnClass) {
         this.beanList = beanList;
         this.columnNames = columnNames;
         this.columnEditable = columnEditable;
         this.columnClass = columnClass;
     }
 
     public BeanTableModel(List beanList, String[] columnNames, boolean[] columnEditable, Class[] columnClass, boolean[] columnVisible) {
         this.beanList = beanList;
         this.columnNames = columnNames;
         this.columnEditable = columnEditable;
         this.columnClass = columnClass;
         this.columnVisible = columnVisible;
     }
 
     @Override
     public Object getValueAt(int row, int column) {
         /**
          * Jika withCheck true dan pada column pertama/ke 0, maka return kan
          * true, untuk dichecked.
          */
         if (withCheck && column == 0) {
             return checkValue[row];
         }
 
         int columnIndexReal = convertToRealColumnIndex(column);
 
         Object value = null;
         if (sortAscDsc) {
             value = BeanClass.getterMethod(beanList.get(row), columnIndexReal);
         } else {
             value = BeanClass.getterMethod(beanList.get(beanList.size() - row - 1), columnIndexReal);
         }
 
         if (value instanceof Calendar) {
             Calendar cal = (Calendar) value;
             value = cal.get(Calendar.DATE) + " " + EnumMonth.values()[(cal.get(Calendar.MONTH))] + " " + cal.get(Calendar.YEAR);
         }
 
         return value;
     }
 
     @Override
     public int getColumnCount() {
         if (columnNames == null) {
             return 0;
         } else {
             /** 
              * Jika columnVisible null, returnkan sesuai columnNames
              * (anggap semua column tampil)
              */
             if (columnVisible == null) {
                 return withCheck ? columnNames.length + 1 : columnNames.length;
             } else {
                 //Returnkan jumlah column yang tampil
                 int columnVisibleCount = 0;
                 for (int col = 0; col < columnVisible.length; col++) {
                     if (columnVisible[col]) {
                         columnVisibleCount++;
                     }
                 }
                 return withCheck ? columnVisibleCount + 1 : columnVisibleCount;
             }
         }
     }
 
     @Override
     public String getColumnName(int column) {
         if (withCheck && column == 0) // Jika withCheck true dan pada column pertama
         {
             return checkColumnName;   // kembalikan checkColumnName untuk columnName
         }
         return columnNames[convertToRealColumnIndex(column)];
     }
 
     @Override
     public Class<?> getColumnClass(int columnIndex) {
         if (withCheck && columnIndex == 0) // Jika withCheck true dan pada column pertama
         {
             return Boolean.class;        // kembalikan tipe columnnya
         }
         int columnIndexReal = convertToRealColumnIndex(columnIndex);
         /**
          * Jika array jenis column Class tidak ada atau null. Jenis class
          * diambil dengan alternatif mengambil getClass() dari beanList
          */
         if (columnClass == null) {
             //Jika beanList null, kasih saja tipe class Object
             if (beanList == null) {
                 return Object.class;
             } //Ambil tipe Class dari beanList ke-0, column/field ke-columnIndex
             else {
                 Object o = getValueAt(0, columnIndex);
                 if (o == null) {
                     return Object.class;
                 } else {
                     return o.getClass();
                 }
             }
         } /**
          * Ambil jenis class column dari columnClass.
          */
         else {
             return columnClass[columnIndexReal];
         }
     }
 
     @Override
     public int getRowCount() {
         if (beanList == null) {
             return 0;
         } else {
             return beanList.size();
         }
     }
 
     @Override
     public boolean isCellEditable(int rowIndex, int columnIndex) {
         if (withCheck && columnIndex == 0) // Jika withCheck true dan pada column pertama
         {
             return true;        // kembalikan jadikan bisa editable
         }
         if (columnEditable == null) {
             return false;
         } else {
             return columnEditable[convertToRealColumnIndex(columnIndex)];
         }
     }
 
     @Override
     public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
         if (withCheck && columnIndex == 0) {  // Jika withCheck true dan pada column pertama
             checkValue[rowIndex] = (Boolean) aValue;
             return;        // 
         }
 
         int columnIndexReal = convertToRealColumnIndex(columnIndex);
         if (sortAscDsc) {
             BeanClass.setterMethod(beanList.get(rowIndex), columnIndexReal, aValue);
         } else {
             BeanClass.setterMethod(beanList.get(beanList.size() - rowIndex - 1), columnIndexReal, aValue);
         }
         fireTableCellUpdated(rowIndex, columnIndex);
     }
 
     public List getBeanList() {
         return beanList;
     }
 
     public void setBeanList(List beanList) {
         this.beanList = beanList;
         createMapBeanList();
     }
 
     public String[] getColumnNames() {
         return columnNames;
     }
 
     public void setColumnNames(String[] columnNames) {
         this.columnNames = columnNames;
     }
 
     private void initCheckValue() {
         //Inisialiasasi nilai dari checkValue.default false.
         if (withCheck) {
             checkValue = new Boolean[getRowCount()];
             for (int i = 0; i < getRowCount(); i++) {
                 checkValue[i] = checkAll;
             }
         }
     }
 
     /**
      *
      * Fungsi untuk mengkonversikan index dari column yang visible(yang ditampilkan)
      * ke index column yang sebenarnya (yang tampak dan yang hidden).
      * Fungsi ini berguna ketika ada beberapa column yang hendak di hidden. yang
      * menyebabkan index dari column yang ditampilkan akan berubah. Sehingga index
      * dari column yang ditampilkan tidak selalu sama dengan index column
      * sebenarnya.
      *
      * @param columnIndexReal index dari column yang nampak
      * @return hasil dari konversi index yang nampak ke indek column sebenarnya
      */
     public int convertToRealColumnIndex(int columnIndexVisible) {
         /**
          * Jika withCheck true, maka column table berkurang satu. 
          * sedangkan data sebenarnya tidak bertambah columnnya
          * jadi column dikurangi satu ketika withCheck = true.
          */
         if (withCheck) {
             columnIndexVisible--;
         }
         /**
          * Jika columnVisible null, anggap saja semua column tampil.
          */
         if (columnVisible == null) {
             return columnIndexVisible;
         }
         /**
          * Pengecekan dengan menghitung incremental column yang nampak.
          */
         int incColumnIndexVisible = -1;
         /**
          * Dilakukan looping untuk mengecek sebenarnya berada di index
          * mana columnIndexReal
          */
         int realColumnIndex = 0;
         for (; realColumnIndex < columnVisible.length; realColumnIndex++) {
             /**
              * jika column nampak, incColumnIndexVisible di incrementkan.
              * ini berarti yang tidak tampak. tidak dihitung.
              */
             if (columnVisible[realColumnIndex]) {
                 incColumnIndexVisible++;
             }
             /**
              * ketika hasil incColumnIndexVisible == columnIndexReal.
              * hentikan loop
              */
             if (incColumnIndexVisible == columnIndexVisible) {
                 break;
             }
         }
         return realColumnIndex;
     }
 
     /**
      * Dapatkan beanList yang tercentang.
      */
     public List getBeanListCheck() {
         List list = new ArrayList();
 
         for (int i = 0; i < getRowCount(); i++) {
             if (checkValue[i]) {
                 list.add(beanList.get(i));
             }
         }
 
         return list;
     }
 
     public List<String> getBeanListCheckKey(){
         List<String> keys = new ArrayList<String>();
         List list = getBeanListCheck();
         
         for(Object o:list){
             keys.add(createMapBeanListKey(o));
         }
         
         return keys;
     }
     
     /**
      * Buat map untuk menampung BeanList. Map ini berguna ketika untuk mencari
      * BeanList mana yang sedang di sorot/pilih.
      * Key pada map berdasarkan nilai dari field yang visible yang dicasting ke
      * String dan di concatkan.
      * syarat dari mapBeanList adalah harus pasti bahwa field yang visible
      * mempunyai nilai yang pasti unik.
      */
     private void createMapBeanList() {
         mapBeanList = new HashMap<String, Object>();
 
         for (Object o : beanList) {
             mapBeanList.put(createMapBeanListKey(o), o);
         }
     }
 
     /**
      * Buat kunci untuk tiap object beanList
      * @param o
      * @return 
      */
     private String createMapBeanListKey(Object o) {
         String key = "";
 
        for (int i = 0; i < (withCheck ? getColumnCount() -1 : getColumnCount()); i++) {
             if (columnVisible == null || columnVisible[i]) {
                 key = key + BeanClass.getterMethod(o, i);
             }
         }
         return key;
     }
 
     /***
      * Dapatkan Bean List dengan kunci string.
      * @param key
      * @return 
      */
     public Object getSelectedBean(String key) {
         return mapBeanList.get(key);
     }
 
     /**
      * Tampilkan urutan atas ke bawah data dari indek pertama ke indek trakhir
      * atau dari indek terakhir ke indek pertama
      *
      * @return true jika data ditampilkan atas ke bawah urut indeks pertama ke akhir.
      * false sebaliknya
      */
     public boolean isSortAscDsc() {
         return sortAscDsc;
     }
 
     /**
      * Tampilkan urutan atas ke bawah data dari indek pertama ke indek trakhir
      * atau dari indek terakhir ke indek pertama
      *
      * @param sortAscDsc true jika Ascending, false jika descending
      */
     public void setSortAscDsc(boolean sortAscDsc) {
         this.sortAscDsc = sortAscDsc;
     }
 
     public boolean[] getColumnEditable() {
         return columnEditable;
     }
 
     public void setColumnEditable(boolean[] columnEditable) {
         this.columnEditable = columnEditable;
     }
 
     public Class[] getColumnClass() {
         return columnClass;
     }
 
     public void setColumnClass(Class[] columnClass) {
         this.columnClass = columnClass;
     }
 
     public boolean[] getColumnVisible() {
         return columnVisible;
     }
 
     public void setColumnVisible(boolean[] columnVisible) {
         this.columnVisible = columnVisible;
     }
 
     /**
      * Apakah TableModel dengan auto checked?
      * @return 
      */
     public boolean isWithCheck() {
         return withCheck;
     }
 
     /**
      * setting untuk menambahkan auto checked jika bernilai true
      * @param withCheck 
      */
     public void setWithCheck(boolean withCheck) {
         this.withCheck = withCheck;
         initCheckValue();
     }
 
     public String getCheckColumnName() {
         return checkColumnName;
     }
 
     public void setCheckColumnName(String checkColumnName) {
         this.checkColumnName = checkColumnName;
     }
 
     public Boolean[] getCheckValue() {
         return checkValue;
     }
 
     public void setCheckValue(Boolean[] checkValue) {
         this.checkValue = checkValue;
     }
 
     public boolean isCheckAll() {
         int benar = 0;
         int salah = 0;
         for(boolean b:checkValue){
             if(b)
                 benar++;
             else
                 salah++;
         }
         
        if(benar == getColumnCount())
             checkAll = true;
         else
             checkAll = false;
         
         return checkAll;
     }
 
     public void setCheckAll(boolean checkAll) {
         this.checkAll = checkAll;
         initCheckValue();
         fireTableDataChanged();
     }
 }
