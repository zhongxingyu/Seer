 package linteners;
 
 import com.ebay.services.finding.SearchItem;
 import com.ebay.services.finding.SortOrderType;
 import util.Pair;
 import util.SearchUtil;
 import util.TextUtil;
 
 import javax.swing.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedInputStream;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 public class LoadFileListener implements ActionListener {
     private static SimpleDateFormat dateformatter = new SimpleDateFormat("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
     private JFileChooser fc;
     private JPanel main;
     private SearchUtil util;
     private JTextArea text;
     private JTextField condition;
     private JTextField listingType;
     private JComboBox<Pair<SortOrderType>> sortedTypeField;
     private JCheckBox golderSearch;
 
     public LoadFileListener(SearchUtil util, JPanel main, JTextField condition,
                             JTextField listingType,
                             JComboBox<Pair<SortOrderType>> sortedTypeField, JCheckBox golderSearch, JTextArea text) {
         this.fc = new JFileChooser();
         this.condition = condition;
         this.listingType = listingType;
         this.sortedTypeField = sortedTypeField;
         this.main = main;
         this.util = util;
         this.text = text;
         this.golderSearch = golderSearch;
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
         int returnVal = fc.showOpenDialog(main);
         if (returnVal == JFileChooser.APPROVE_OPTION) {
             File file = fc.getSelectedFile();
             String msg = "";
             if (file.exists()) {
                 File saveFile = new File("SearchItem_"+dateformatter.format(Calendar.getInstance().getTime())+".txt");
                 List<String> data = readFile(file);
                 StringBuilder sb = new StringBuilder();
                 sb.append("The file is loaded and to be made search.\n");
                 Pair<SortOrderType> pairSorted = sortedTypeField.getItemAt(sortedTypeField.getSelectedIndex());
                 String typeSearch = "";
                 if (golderSearch.isSelected()) {
                    typeSearch = "GoldenItems : \n";
                 } else {
                    typeSearch = "All items : \n";
                 }
                 sb.append(typeSearch);
                 StringBuilder dataForSave = new StringBuilder();
                 for (String id : data) {
                     if (TextUtil.isNotNull(id)) {
                         List<SearchItem> items = getResult(sb, pairSorted, id, "UPC");
                         if (items.isEmpty()) {
                             items = getResult(sb, pairSorted, id, "ReferenceID");
                             if (items.isEmpty()) {
                                dataForSave.append(typeSearch);
                                sb.append("\nid : ").append(id).append(" doesn't have any match by peference and upc id!\n\n");
                                dataForSave.append("\nid : ").append(id).append(" doesn't have any match by peference and upc id!\n\n");
                             } else {
                                 dataForSave.append(buildCsvFile(items,"ReferenceID", id, typeSearch));
                             }
                         } else {
                             dataForSave.append(buildCsvFile(items, "UPC", id, typeSearch));
                         }
                     }
                 }
                 SaveListener.save(saveFile, dataForSave.toString());
                 sb.append("Save was done. file : ").append(saveFile.getAbsolutePath()).append("\n");
                 msg = sb.toString();
             } else {
                 msg = "Error this file does not exist file : " + file.getAbsolutePath() + "\n";
             }
             text.setText(text.getText() + msg);
         }
     }
 
     /**
      * This method makes specail formats data for file
      * @param items
      * @param type
      * @param id
      * @return
      */
     public static String buildCsvFile(List<SearchItem> items, String type, String id, String typeSearch) {
         Map<String, List<SearchItem>> listByComdition = new LinkedHashMap<String, List<SearchItem>>(); //this map we use for sort our items by condition
         for (SearchItem item : items) {
             String condition = item.getCondition().getConditionDisplayName();
             List<SearchItem> conditionList = listByComdition.get(condition);
             if (conditionList == null) {
                 conditionList = new ArrayList<SearchItem>();
             }
             conditionList.add(item);
             listByComdition.put(condition, conditionList);
         }
         StringBuilder sb = new StringBuilder("Type search : " + typeSearch);
         sb.append(type).append(" (").append(id).append(")").append("\n\n");
         for (Map.Entry<String, List<SearchItem>> entry : listByComdition.entrySet()) {
             sb.append("Condition (").append(entry.getKey()).append(")").append("\n");
             for (SearchItem item : entry.getValue()) {
                 sb.append(item.getItemId()).append("\n");
             }
             sb.append("\n");
         }
         return sb.toString();
     }
 
     private List<SearchItem> getResult(final StringBuilder sb, Pair<SortOrderType> pairSorted, String id, String type) {
         List<SearchItem> items = new ArrayList<SearchItem>();
         List<SearchItem> searchItems = util.getItemsBySortedType(id, condition.getText(), listingType.getText(), pairSorted.getValue(), type);
         if (searchItems != null) {
             if (golderSearch.isSelected()) {
                 List<SearchItem> goldenItems = SearchUtil.getGoldenItems(searchItems);
                 items.addAll(goldenItems);
             } else {
                 items.addAll(searchItems);
             }
         }
         if (!items.isEmpty()) {
             sb.append(type).append(" : ").append(id).append("\n") ;
             for (SearchItem item : items) {
                 sb.append(item.getItemId()).append("\n");
                 sb.append(item.getTitle()).append("\n");
                 sb.append(item.getCondition().getConditionDisplayName()).append("\n");
                 sb.append(item.getListingInfo().getListingType()).append("\n\n");
             }
             sb.append("Total items : ").append(items.size()).append("\n\n");
         }
         return items;
     }
 
     /**
      * This method parses text from file. It starts with seconds line, because
      * first line is header
      *
      * @param data
      * @return
      */
     public static List<List<String>> buildTable(String data) {
         List<List<String>> table = new ArrayList<List<String>>();
         String[] lines = data.split("\\|");
         for (int i = 1; i != lines.length; ++i) {
             List<String> row = new ArrayList<String>();
             for (String value : lines[i].split(";")) {
                 value = value.substring(1, value.length()-1);
                 row.add(value);
             }
             table.add(row);
         }
         return table;
     }
 
     /**
      * This method reads text from a file.
      *
      * @param file
      * @return
      */
     public static List<String> readFile(File file) {
         List<String> list = new ArrayList<String>();
         try {
             FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis);
             DataInputStream dis = new DataInputStream(bis);
             while (dis.available() != 0) {
                 list.add(dis.readLine());
             }
             fis.close();
             bis.close();
             dis.close();
         } catch (Exception e) {
             System.err.println("Error: " + e.getMessage());
         }
         return list;
     }
 }
