 /*******************************************************************************
  * Copyright (c) 2011 Sam Bell.
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published
  * by the Free Software Foundation, either version 3 of the License,
  * or  any later version.
  * 
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
  * the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * Contributors:
  *     Sam Bell - initial API and implementation
  ******************************************************************************/
 /**
  * 
  */
 package podsalinan;
 
 import java.awt.BorderLayout;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 //import javax.swing.JEditorPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 //import javax.swing.JSplitPane;
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableColumn;
 
 @SuppressWarnings("serial")
 public class DownloadList extends JPanel {
 
 	static final int IS_PODCAST = 0;
 	static final int IS_URL_WINDOW = 1;
 	
 	private DefaultTableModel downloadList;
 	private int listType;
 	private JTable table;
 	
 	public DownloadList(int windowType){
 		super();
 		
 		Object headers[];
 
 		listType = windowType;
 		setLayout(new BorderLayout());
 		switch(listType){
 			case IS_URL_WINDOW:
 				headers = new Object[]{"Filename",
 									   "Progress"};
 				break;
 			case IS_PODCAST:
 			default:
 				headers = new Object[]{"Title",
 									   "Date",
 									   "Progress"};
 				break;
 		}
 		downloadList = new DefaultTableModel(headers,1);
 		table = new JTable(downloadList){
 		    public boolean isCellEditable(int rowIndex, int vColIndex) {
 		        return false;
 		    }
 		};
 		
 		// removing grid from table
 		table.setShowGrid(false);
 		
 		table.setRowSelectionAllowed(true);
 		
 		Object newRow[];
 		TableColumn myCol;
 		
 		switch(listType){
 			case IS_URL_WINDOW:
 				newRow = new Object [] {"","0%"};
 				downloadList.addRow(newRow);
 				downloadList.removeRow(0);
 				myCol = table.getColumnModel().getColumn(1);
 				myCol.setCellRenderer(new ProgressCellRenderer());
 				break;
 			case IS_PODCAST:
 			default:
 				newRow = new Object [] {"","","0%"};
 				downloadList.addRow(newRow);
 				downloadList.removeRow(0);
 				myCol = table.getColumnModel().getColumn(2);
 				myCol.setCellRenderer(new ProgressCellRenderer());
 				break;
 		}
 		add(new JScrollPane(table));
 	}
 	
 	public String removeDownload(){
 		int row = table.getSelectedRow();
 		String url = (String) downloadList.getValueAt(row, 0);
 		downloadList.removeRow(row);
 		
 		return url;
 	}
 
 	public void removeDownload(String url){
 		for (int tblc=0; tblc < table.getRowCount(); tblc++){
 			String tableUrl = (String)downloadList.getValueAt(tblc, 0);
 			if (tableUrl.equals(url)){
 				downloadList.removeRow(tblc);
 			}
 		}
 	}
 	
 	public int getRow(String url){
 		int rowNum=0;
 		
 		while(rowNum<downloadList.getRowCount()){
 			String tableURL = (String)downloadList.getValueAt(rowNum, 0);
 			if (tableURL.equals(url.substring(url.lastIndexOf('/')+1))){
 				return rowNum;
 			} else
 				rowNum++;
 		}
 		return -1;
 	}
 	
 	/** Used for Adding a download in a podcast
 	 * 
 	 * @param title
 	 * @param date
 	 * @param filename
 	 * @param progress
 	 */
 	public void addDownload(String title, String date, String filename,String progress, String dateFormat){
 		Object newRow[];
 		
 		if (filename!=null){
 			if (listType==IS_PODCAST){
 				newRow = new Object[] {title,date,"0%"};
 				if ((downloadList.getRowCount()<2)&&
 					(downloadList.getValueAt(0, 0).toString().length()==0)){
 					/* If this is the first item to be added to the downloadList window add the new row
 					 * and delete the blank row.
 					 */
 					downloadList.addRow(newRow);
 					downloadList.removeRow(0);
 				} else {
 					/* If this is not the first item added to the downloadList window, check the date,
 					 * to position the download in the appropriate time space. 
 					 */
 					int rowCount=0;
 					boolean found=false;
 					while ((!found)&&(rowCount<downloadList.getRowCount())){
 						if (downloadList.getValueAt(rowCount, 0).toString().length()!=0){
 							// Date format as defined in the Episode class
 							DateFormat newFormat = new SimpleDateFormat(dateFormat);
 							try {
 								// Grab date from current row in the downloadList
 								Date currentDate = newFormat.parse(downloadList.getValueAt(rowCount, 1).toString());
 								// Grab the date from the new Item to be added
 								Date newDate = newFormat.parse(date);
 								// If the new date is earlier then the current date in the list mark as found
 								if (newDate.before(currentDate)){
 									found=true;
 								} else {
 									rowCount++;
 								}
 							} catch (ParseException e) {
 								//e.printStackTrace();
 							}
 							
 						}
 					}
 					// Add new item to downloadList at selected position.
 					downloadList.insertRow(rowCount, newRow);
 				}
 			}
 		}
 	}
 	
 	/** Used for Adding a stand-alone download
 	 * 
 	 * @param newDownload
 	 */
 	public void addDownload(String newDownload){
 		Object newRow[];
 	
 		if (newDownload != null){
 			// Adding just the filename to the window
 			newDownload=newDownload.substring(newDownload.lastIndexOf('/')+1);
 
 			newRow = new Object[] {newDownload,"0%"};
 			downloadList.addRow(newRow);
 			
 			if (downloadList.getValueAt(0, 0).toString().length()==0){
 				downloadList.removeRow(0);
 			}
 		}
 	}
 
 	public void downloadSpeed (int dlc, String speed){
 		downloadList.setValueAt(speed, dlc, 2);
 	}
 	
 	public void downloadProgress(int dlc, String progress){
 		downloadList.setValueAt(progress, dlc, 1);
 	}
 	
 	public void downloadProgress(int dlc, int progress){
 		String newProgress = Integer.toString(progress)+"%";
 
		if (listType==IS_PODCAST)
			downloadList.setValueAt(newProgress, dlc, 2);
		else if (listType==IS_URL_WINDOW)
			downloadList.setValueAt(newProgress, dlc, 1);
 	}
 }
