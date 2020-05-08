 package com.ebaytools.util;
 
 import com.ebaytools.kernel.entity.FileSearching;
 
 import javax.swing.*;
 import javax.swing.table.TableColumnModel;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.List;
 
 public class FileSearchingDataImpl implements TableModelCheckBox.IData {
     private List<FileSearching> fileSearchings;
     private static final List<String> nameColumn = Arrays.asList("ID", "Path", "Condition", "ListType", "LeftTime", "Interval", "RunTime", "Type");
 
     public FileSearchingDataImpl(List<FileSearching> fileSearchings) {
         this.fileSearchings = fileSearchings;
     }
 
     @Override
     public List<Object[]> getData() {
         List<Object[]> objectFields = new ArrayList<Object[]>();
         for (FileSearching fileSearching : fileSearchings) {
             Object[] objects = new Object[9];
             objects[0] = Boolean.FALSE;
             objects[1] = fileSearching.getId();
             objects[2] = fileSearching.getPath();
             objects[3] = fileSearching.getCondition();
             objects[4] = fileSearching.getListType();
             objects[5] = fileSearching.getDayLeft();
             objects[6] = fileSearching.getTimeInterval();
             Calendar cal = fileSearching.getRunTime();
             objects[7] = cal != null ?  FormatterText.dateformatter.format(cal.getTime()) : " - ";
             objects[8] = fileSearching.getValueType();
             objectFields.add(objects);
         }
         return objectFields;
     }
 
     @Override
     public List<String> getNameColumn() {
         return nameColumn;
     }
 
     public void resizeColumnsByName(JTable table) {
         table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
         TableColumnModel tcm = table.getColumnModel();
         tcm.getColumn(0).setPreferredWidth(20);
         tcm.getColumn(1).setPreferredWidth(20);
         tcm.getColumn(2).setPreferredWidth(150);
         tcm.getColumn(3).setPreferredWidth(50);
         tcm.getColumn(4).setPreferredWidth(50);
         tcm.getColumn(5).setPreferredWidth(50);
         tcm.getColumn(6).setPreferredWidth(50);
        tcm.getColumn(7).setPreferredWidth(50);
         tcm.getColumn(8).setPreferredWidth(100);
     }
 }
