 package com.tor.swing.table;
 
 import obuchenie.data.DataToTest;
 import obuchenie.data.Person;
 
 import javax.swing.*;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableModel;
 import javax.swing.table.TableRowSorter;
 import java.awt.*;
 import java.util.ArrayList;
 import java.util.Comparator;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Admin
  * Date: 25.01.14
  * Time: 16:35
  * examples
  */
 public class RowSorterExample extends JPanel {
     public RowSorterExample() {
         Object[] columnNames = {"human", "Type", "Company", "Shares", "Price", "Boolean"};
         Object[][] data = {
                 {DataToTest.getPersonCollection().get(0), "Buy", "IBM", new Integer(1000), new Double(80.5), Boolean.TRUE},
                 {DataToTest.getPersonCollection().get(1), "Sell", "Dell", new Integer(2000), new Double(6.25), Boolean.FALSE},
                 {DataToTest.getPersonCollection().get(2), "Short Sell", "Apple", new Integer(3000), new Double(7.35), Boolean.TRUE},
                 {DataToTest.getPersonCollection().get(3), "Buy", "MicroSoft", new Integer(4000), new Double(27.50), Boolean.FALSE},
                 {DataToTest.getPersonCollection().get(4), "Short Sell", "Cisco", new Integer(5000), new Double(20), Boolean.TRUE}
         };
         TableModel model = new DefaultTableModel(data, columnNames) {
             public Class getColumnClass(int col) {
                 return getValueAt(0, col).getClass();
             }
         };
         JTabbedPane tabbedPane = new JTabbedPane();
         tabbedPane.addTab("OnlyFirst", createOnlyFirst(model));
        tabbedPane.addTab("GerComparator", GerComparator(model));
         add(tabbedPane);
     }
 
     private Component createOnlyFirst(TableModel model) {
         String toolTip = "Есть задача отсортировать таблицу по одной колонке. Сортировка должна производиться\n" +
                 "ТОЛЬКО по возрастанию. Т.е. пользователь ввел данные в новую строку и она сразу встала на нужное место";
         final int column = 1;
 
 
         JTable jTable = new JTable(model);
         TableRowSorter sorter = new TableRowSorter(jTable.getModel()); //Создаем сортировщик
         sorter.setSortable(0, false); //
         sorter.setSortable(column, true); //Указываем, что сортировать будем в  колонке
         sorter.setSortable(2, false); // а в других нет
         sorter.setSortable(3, false);
         ArrayList<RowSorter.SortKey> keys = new ArrayList<RowSorter.SortKey>(); // создаем коллецию ключей сортировки
 
         /*Записываем два ключа !!! (если задать один раз, то сортировщик по-умолчанию DefaultRowSorter от которого происходит
          TableRowSorter автоматически добавит SortOrder.ASCENDING */
         keys.add(new RowSorter.SortKey(column, SortOrder.DESCENDING));
         keys.add(new RowSorter.SortKey(column, SortOrder.DESCENDING));
         sorter.setSortKeys(keys);                                   //Добавляем ключи к сортировщику
 
         sorter.toggleSortOrder(column);                                  //Сортируем первую колонку
         sorter.setSortsOnUpdates(true);                         //Указываем автоматически сортировать
         //при изменении модели данных
         jTable.setRowSorter(sorter);                        //Устанавливаем сортировщик в таблицу
 
         jTable.setToolTipText(toolTip);
         return new JScrollPane(jTable);
     }
 
    private Component GerComparator(TableModel model) {
 
         JTable jTable = new JTable(model);
        TableRowSorter sorter = new TableRowSorter(jTable.getModel()) {
             @Override
             public Comparator<?> getComparator(int column) {
                 Comparator comparator = super.getComparator(column);
                if (column == 1) comparator = new Comparator() {
                     @Override
                     public int compare(Object o1, Object o2) {
                         Person p1 = (Person) o1;
                         Person p2 = (Person) o2;
                         String s1 = p1.getFio().getLastName() + p1.getFio().getFirstName() + p1.getFio().getMidllName();
                         String s2 = p2.getFio().getLastName() + p2.getFio().getFirstName() + p2.getFio().getMidllName();
                         return s1.compareTo(s2);
                     }
                 };
                 return comparator;
             }
        };

 
         jTable.setRowSorter(sorter);
 
         return new JScrollPane(jTable);
     }
 
     public static void createAndShowGUI() {
         JFrame.setDefaultLookAndFeelDecorated(true);
         JFrame frame = new JFrame("Table Row Rendering");
         frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         frame.add(new RowSorterExample());
         frame.pack();
         frame.setLocationRelativeTo(null);
         frame.setVisible(true);
     }
 
     public static void main(String[] args) {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 createAndShowGUI();
             }
         });
     }
 }
