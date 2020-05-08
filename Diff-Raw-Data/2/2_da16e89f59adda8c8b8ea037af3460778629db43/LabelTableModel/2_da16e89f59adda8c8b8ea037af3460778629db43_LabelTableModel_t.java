 package org.esa.beam.chris.ui;
 
 import org.esa.beam.chris.operators.ClusterProperties;
 import org.esa.beam.framework.datamodel.ColorPaletteDef;
 import org.esa.beam.framework.datamodel.ImageInfo;
 
 import javax.swing.table.AbstractTableModel;
 import java.awt.*;
 import java.text.MessageFormat;
 import java.util.Arrays;
 
 /**
  * User: Marco Peters
  * Date: 15.05.2008
  */
 class LabelTableModel extends AbstractTableModel {
 
     private static final String[] COLUMN_NAMES = new String[]{
             "Label", "Colour", "Cloud", "Reject", "Brightness", "Occurrence"
     };
     private static final Class<?>[] COLUMN_TYPES = new Class<?>[]{
             String.class, Color.class, Boolean.class,
             Boolean.class, Double.class, Double.class
     };
 
     private final ImageInfo imageInfo;
     private final boolean[] cloud;
     private final boolean[] rejected;
     private ClusterProperties clusterProperties;
     private final int rowCount;
 
     LabelTableModel(ImageInfo imageInfo) {
         this.imageInfo = imageInfo;
        rowCount = imageInfo.getColorPaletteDef().getNumPoints();
         cloud = new boolean[rowCount];
         rejected = new boolean[rowCount];
         clusterProperties = new ClusterProperties(rowCount);
     }
 
     public ImageInfo getImageInfo() {
         return imageInfo;
     }
 
     public int[] getRejectedIndexes() {
         return getSelectedIndexes(rejected);
     }
 
     public int[] getCloudIndexes() {
         return getSelectedIndexes(cloud);
     }
 
     static int[] getSelectedIndexes(boolean[] array) {
         int[] indexes = new int[array.length];
         int indexCount = 0;
         for (int i = 0; i < array.length; i++) {
             if (array[i]) {
                 indexes[indexCount] = i;
                 indexCount++;
             }
         }
         int[] result = new int[indexCount];
         System.arraycopy(indexes, 0, result, 0, indexCount);
         return result;
     }
 
     static void setSelectedIndexes(boolean[] array, int[] indexes) {
         Arrays.fill(array, false);
         if (indexes != null) {
             for (final int index : indexes) {
                 if (index >= 0 && index < array.length) {
                     array[index] = true;
                 }
             }
         }
     }
 
     public int[] getSurfaceIndexes() {
         boolean[] surface = new boolean[cloud.length];
         for (int i = 0; i < surface.length; i++) {
             surface[i] = !(cloud[i] || rejected[i]);
         }
         return getSelectedIndexes(surface);
     }
 
     @Override
     public String getColumnName(int column) {
         return COLUMN_NAMES[column];
     }
 
     @Override
     public Class<?> getColumnClass(int columnIndex) {
         return COLUMN_TYPES[columnIndex];
     }
 
     public int getColumnCount() {
         return COLUMN_NAMES.length;
     }
 
     public int getRowCount() {
         return rowCount;
     }
 
 
     public Object getValueAt(int rowIndex, int columnIndex) {
         final ColorPaletteDef.Point point = imageInfo.getColorPaletteDef().getPointAt(rowIndex);
         switch (columnIndex) {
             case 0:
                 return point.getLabel();
             case 1:
                 return point.getColor();
             case 2:
                 return cloud[rowIndex];
             case 3:
                 return rejected[rowIndex];
             case 4:
                 return clusterProperties.getBrightnesses()[rowIndex];
             case 5:
                 return clusterProperties.getOccurrences()[rowIndex];
             default:
                 return 0;
         }
     }
 
     @Override
     public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
         final ColorPaletteDef.Point point = imageInfo.getColorPaletteDef().getPointAt(rowIndex);
         switch (columnIndex) {
             case 0:
                 point.setLabel((String) aValue);
                 fireTableCellUpdated(rowIndex, 0);
                 break;
             case 1:
                 point.setColor((Color) aValue);
                 fireTableCellUpdated(rowIndex, 1);
                 break;
             case 2:
                 cloud[rowIndex] = (Boolean) aValue;
                 if (cloud[rowIndex] && rejected[rowIndex]) {
                     rejected[rowIndex] = false;
                     fireTableCellUpdated(rowIndex, 3);
                 }
                 fireTableCellUpdated(rowIndex, 2);
                 break;
             case 3:
                 rejected[rowIndex] = (Boolean) aValue;
                 if (cloud[rowIndex] && rejected[rowIndex]) {
                     cloud[rowIndex] = false;
                     fireTableCellUpdated(rowIndex, 2);
                 }
                 fireTableCellUpdated(rowIndex, 3);
                 break;
             default:
                 final String msg = MessageFormat.format("Invalid column index [{0}]", columnIndex);
                 throw new IllegalStateException(msg);
         }
     }
 
     @Override
     public boolean isCellEditable(int rowIndex, int columnIndex) {
         return columnIndex >= 0 && columnIndex <= 3;
     }
 
     public void setCloudClusterIndexes(int[] cloudClusterIndexes) {
         setSelectedIndexes(cloud, cloudClusterIndexes);
     }
 
     public void setRejectedIndexes(int[] rejectedIndexes) {
         setSelectedIndexes(rejected, rejectedIndexes);
     }
 
     public void setClusterProperties(ClusterProperties clusterProperties) {
         this.clusterProperties = clusterProperties;
         for (int i = 0; i < rowCount; ++i) {
             fireTableCellUpdated(i, 4);
             fireTableCellUpdated(i, 5);
         }
     }
 }
