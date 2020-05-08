 package dit;
 
 import static dit.ManualMatchTableModel.displayIntoTable;
 import dit.panels.MatchDataPanel;
 import java.io.File;
 import java.util.*;
 import javax.swing.table.AbstractTableModel;
 
 
 
 /**
  *
  * @author Christian Prescott & Angelo
  */
 public class MatchTableModel extends AbstractTableModel {
     
     private String[] columnNames = new String[]{"Image File", "Date File ID", "Status"};
     private Object[][] data;
     private int mismatchCount, matchCount;
     
     
     public MatchTableModel() {
         mismatchCount = 0;
         matchCount = 0;
         data = new Object[0][0];
     }
     
     public MatchTableModel(List<File> images, Object[] demoIDs, final boolean isbyPath,final boolean ismulti) {
         ArrayList<Object[]> dataList = new ArrayList<Object[]>();
         
         // The demoIDs will ALWAYS be sorted, images probably will not be.
         // The standard string compareTo should be very reliable.
         Collections.sort(images, new Comparator<File>(){
             @Override
             public int compare(File f1, File f2) {
                 return FileUtils.getName(f1).compareToIgnoreCase(FileUtils.getName(f2));
             }
         });
         DeidData.IdFilename = new Hashtable<String, String>();
         Iterator<File> imageIt = images.iterator();
         File curFile = null;
         try {
             curFile = imageIt.next();
         } catch (NoSuchElementException e) {
             curFile = null;
         }
         int demoIDNdx = 0;
         if(!isbyPath){
             while (curFile != null && demoIDNdx < demoIDs.length) {
                 //if(Arrays.binarySearch(demoIDs, "1952") >= 0){ This is how to use this
                 //int searchResult = Arrays.binarySearch(demoIDs, FileUtils.getName(curFile));
                 //if(searchResult >= 0){
                 String filename = FileUtils.getName(curFile);
                 String mapDisplaytoFile=fileintoTable(curFile);   //do not delete this line unless you know its function
                 if (filename.equals(demoIDs[demoIDNdx])) {
                     dataList.add(new Object[]{ManualMatchTableModel.displayIntoTable(curFile), demoIDs[demoIDNdx], new Boolean(true)});
                     DeidData.IdFilename.put(fileintoTable(curFile), (String)demoIDs[demoIDNdx] );
                     //System.out.println("Found ID "+ demoIDs[searchResult] +" "+dataList.size());
                     try {
                         curFile = imageIt.next();
                     } catch (NoSuchElementException e) {
                         curFile = null;
                     }
                     if (!ismulti){
                         demoIDNdx++;
                     }
                     matchCount++;
                 } else if (filename.compareTo((String) demoIDs[demoIDNdx]) < 0) {
                     dataList.add(new Object[]{ManualMatchTableModel.displayIntoTable(curFile), null, new Boolean(false)});
                     //System.out.println("Did not find " + FileUtils.getName(curFile));
                     mismatchCount++;
                     try {
                         curFile = imageIt.next();
                     } catch (NoSuchElementException e) {
                         curFile = null;
                     }
                 } else {
                     dataList.add(new Object[]{null, demoIDs[demoIDNdx], new Boolean(false)});
                     //System.out.println("Did not find " + FileUtils.getName(curFile));
                     demoIDNdx++;
                 }
             }
             while (curFile != null) {
                 dataList.add(new Object[]{fileintoTable(curFile), null, new Boolean(false)});
                 //System.out.println("Did not find " + FileUtils.getName(curFile));
                 mismatchCount++;
                 try {
                     curFile = imageIt.next();
                 } catch (NoSuchElementException e) {
                     curFile = null;
                 }
             }
             while (demoIDNdx < demoIDs.length) {
                 dataList.add(new Object[]{null, demoIDs[demoIDNdx], new Boolean(false)});
                 //System.out.println("Did not find " + FileUtils.getName(curFile));
                 demoIDNdx++;
             }
         }
         else {
             while (curFile != null){
                 String mapDisplaytoFile=fileintoTable(curFile);   //do not delete this line unless you know its function
                 demoIDNdx = 0;
                 String filepath = curFile.getAbsolutePath();
                 int tmpmatchCount = 0;
                 int mismatchflag = 0;
                 int matchRecorder = 0;
                 while(demoIDNdx < demoIDs.length)
                 {
                     
                     if(getCountIndex(filepath, (String)demoIDs[demoIDNdx]) == 1)
                     {
                         //dataList.add(new Object[]{curFile.getName(), demoIDs[demoIDNdx], new Boolean(true)});
                         
                         tmpmatchCount++;
                         matchRecorder = demoIDNdx;
                         
                     }
                     else if(getCountIndex(filepath, (String)demoIDs[demoIDNdx]) > 1)
                     {
                         //dataList.add(new Object[]{curFile.getName(), demoIDs[demoIDNdx], new Boolean(false)});
                         mismatchflag = 1;
                         break;
                         
                     }
                     
                     
                     demoIDNdx++;
                     
                 }
                 
                 if (mismatchflag == 1 || tmpmatchCount > 1 || tmpmatchCount == 0 ) {
                     
                     mismatchCount++;
                     dataList.add(new Object[]{fileintoTable(curFile), null, new Boolean(false)});
                     
                 } else if (tmpmatchCount == 1 && demoIDNdx == demoIDs.length)
                 {
                     dataList.add(new Object[]{curFile.getParent()+ "/"+ curFile.getName(), demoIDs[matchRecorder], new Boolean(true)});
                     DeidData.IdFilename.put(fileintoTable(curFile), (String)demoIDs[matchRecorder] );
                     matchCount++;
                 }
                 
                 try {
                     curFile = imageIt.next();
                 } catch (NoSuchElementException e) {
                     curFile = null;
                 }
             }
             
             
         }
         Object[][] dataArray = new Object[dataList.size()][3];
         for (int ndx = 0; ndx < dataList.size(); ndx++) {
             dataArray[ndx] = dataList.get(ndx);
         }
         data = dataArray;
         DeidData.data = data;
     }
     // get the number of one sub string appearing in a string
     private int getCountIndex(String str, String substr){
         int count=0;
         int index=0;
         while(true){
             index = str.indexOf(substr,index);
             if(index==-1){
                 break;
             }
             index = index + substr.length();
             count++;
         }
         return count;
     }
     private String fileintoTable(File file){
         
         String abParent = file.getParent();
         String out = FileUtils.getName(file).toString();
        if (!DeidData.parentPath.equals("none"))    {
             
            out = abParent.replaceFirst(DeidData.parentPath, "").replaceFirst(DeidData.anaPath, "").replaceFirst(DeidData.dicomPath, "").replaceAll("/", "") + out;
        }
         MatchDataPanel.displayTofile.put(displayIntoTable(file),out );
         return out;
         
     }
     
     
     @Override
     public int getRowCount() {
         return data.length;
     }
     
     @Override
     public int getColumnCount() {
         return columnNames.length;
     }
     
     @Override
     public String getColumnName(int col) {
         return columnNames[col];
     }
     
     @Override
     public Object getValueAt(int row, int col) {
         return data[row][col];
     }
     
     public int getMismatchedImageCount() {
         return mismatchCount;
     }
     
     public int getMatchedImageCount(){
         return matchCount;
     }
 }
