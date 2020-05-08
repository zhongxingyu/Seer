 /*
  * Project: xdccBee
  * Copyright (C) 2009 snert@snert-lab.de,
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.snertlab.xdccBee.ui;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.TableEditor;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.ProgressBar;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableItem;
 import org.jibble.pircbot.DccFileTransfer;
 
 import de.snertlab.xdccBee.irc.DccDownload;
 
 /**
  * @author snert
  *
  */
 public class TableItemDownload {
 	
 	private static final long KILOBYTES = 1024L;
 
 	public static String STATE_DOWNLOAD_FINISHED = "finished";
 	public static String STATE_DOWNLOAD_WAITING  = "waiting";
 	public static String STATE_DOWNLOAD_DOWNLOAD = "downloading";
 	
 	private Table downloadTable;
 	private DccDownload dccDownload;
 	private TableItem itemDownload;
 	private ProgressBar bar;
 	
 	public TableItemDownload(Table downloadTable, DccDownload dccDownload){
 		dccDownload.setTableItemDownload(this);
 		this.downloadTable = downloadTable;
 		this.dccDownload = dccDownload;
 		makeTableItem();
 	}
 	
 	private void makeTableItem() {
 		downloadTable.getDisplay().asyncExec( new Runnable() {
 			public void run() {
 				itemDownload = new TableItem(downloadTable, SWT.NONE);
 				itemDownload.setText(dccDownload.getDccPacket().getName());
 				bar = new ProgressBar(downloadTable, SWT.NONE);
 		        TableEditor editor = new TableEditor(downloadTable);
 		        editor.grabHorizontal = true;
 		        editor.grabVertical = true;
 		        editor.setEditor(bar, itemDownload, 1);
 		        setState(STATE_DOWNLOAD_WAITING);
 			}
 		});
 	}
 		
 	public Display getDisplay(){
 		return downloadTable.getDisplay();
 	}
 
 	public void updateFileTransferDisplay(DccFileTransfer dccFileTransfer) {
 		bar.setMaximum((int)dccFileTransfer.getSize());
 		itemDownload.setText(2, " " + bytesToKb(dccFileTransfer.getTransferRate())+" KB/s");
 		bar.setSelection((int)dccFileTransfer.getProgress());
 		itemDownload.setText(3, getEstimateTime(dccFileTransfer));
 	}
 	
 	private long bytesToKb(long bytes){
 		return bytes / KILOBYTES;
 	}
 	
 	public String getEstimateTime(DccFileTransfer dccFileTransfer){
 		if(dccFileTransfer.getTransferRate()==0) return "~";
    	long a = (dccFileTransfer.getSize() - dccFileTransfer.getProgress()) / dccFileTransfer.getTransferRate();
    	SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
    	Calendar calCurrent = Calendar.getInstance();
     	Calendar cal = Calendar.getInstance();    	
    	cal.add(Calendar.SECOND, (int)(a) );
    	long diff = cal.getTimeInMillis() - calCurrent.getTimeInMillis(); //FIXME: Duration is wrong
    	String s1 = df.format(diff); 
     	return s1;
     }
 
 	public void setState(String state_download) {
 		itemDownload.setText(4, state_download); //TODO: Translations of the different states from xdccBeeMessages
 	}
 
 }
