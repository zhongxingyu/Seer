 /*
  *  ___  ___   _      ___                 _    
  * |   \/ __| /_\    / __|___ _ _  ___ __(_)___
  * | |) \__ \/ _ \  | (_ / -_) ' \/ -_|_-< (_-<
  * |___/|___/_/ \_\  \___\___|_||_\___/__/_/__/
  *
  * -----------------------------------------------------------------------------
  * @author: Herbert Veitengruber 
  * @version: 1.0.0
  * -----------------------------------------------------------------------------
  *
  * Copyright (c) 2013 Herbert Veitengruber 
  *
  * Licensed under the MIT license:
  * http://www.opensource.org/licenses/mit-license.php
  */
 package dsagenesis.editor.coredata;
 
 import java.awt.Toolkit;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.ClipboardOwner;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.StringSelection;
 import java.awt.datatransfer.Transferable;
 import java.util.StringTokenizer;
import java.util.Vector;
 
 import jhv.util.debug.logger.ApplicationLogger;
 
 import dsagenesis.core.ui.PopupDialogFactory;
 import dsagenesis.editor.coredata.table.CoreEditorTable;
 
 /**
  * ClipboardCellTransfer
  * 
  * ClipboardOwner for copy/paste Core Editor table cells.
  * You can only copy/paste inside the same table and from to the same column(s).
  */
 public class ClipboardCellTransfer
 		implements ClipboardOwner
 {
 
 	// ============================================================================
 	//  Variables
 	// ============================================================================
 	
 	/**
 	 * singleton instance
 	 */
 	private static ClipboardCellTransfer instance;
 	
 	/**
 	 * stores the Table for copy/paste
 	 */
 	private CoreEditorTable copyTable = null;
 
 	/**
 	 * stores the selected start column for copy/paste
 	 */
 	private int selectedStartColumn = -1;
 	
 	
 	// ============================================================================
 	//  Constructors
 	// ============================================================================
 	
 	/**
 	 * Constructor
 	 */
 	private ClipboardCellTransfer()
 	{
 		
 	}
 
 	// ============================================================================
 	//  Functions
 	// ============================================================================
 	
 	public static ClipboardCellTransfer getInstance()
 	{
 		if( instance == null )
 			instance = new ClipboardCellTransfer();
 		
 		return instance;
 	}
 	
 	@Override
 	public void lostOwnership(Clipboard c, Transferable t) 
 	{
 		this.selectedStartColumn = -1;
 		this.copyTable = null;
 	}
 	
 	/**
 	 * setClipboardContents
 	 * 
 	 * sets the system clip board with content from the table.
 	 * returns false if copying failed.
 	 * 
 	 * @param table
 	 * 
 	 * @return
 	 */
 	public boolean setClipboardContents(CoreEditorTable table)
 	{
 		this.selectedStartColumn = (table.getSelectedColumns())[0];
 		this.copyTable = table;
 		
 		StringBuffer sbf = new StringBuffer();
 		int numcols = table.getSelectedColumnCount();
 		int numrows = table.getSelectedRowCount();
 		int[] rowsselected = table.getSelectedRows();
 		int[] colsselected = table.getSelectedColumns();
 
 		if( !( (numrows-1 == rowsselected[rowsselected.length-1]-rowsselected[0] 
 					&& numrows == rowsselected.length ) 
 				&& ( numcols-1 == colsselected[colsselected.length-1]-colsselected[0] 
 					&& numcols == colsselected.length) )
 			)
 		{
 			PopupDialogFactory.copyDatabaseError();
 			return false;
 		}
 		
 		for( int i = 0; i < numrows; i++ )
 		{
 			for( int j = 0; j < numcols; j++ )
 			{
 				Object value = table.getValueAt(rowsselected[i],colsselected[j]);
 				if( value != null && !(value.equals("")) )
 				{
					// indicate value as vector
					if( value instanceof Vector )
						sbf.append("Vector");
					
					sbf.append(value.toString());
 				} else {
 					sbf.append(" ");
 				}
 				if( j < (numcols-1) )
 					sbf.append("\t");
 			}
 			sbf.append("\n");
 		}
 		StringSelection stsel  = new StringSelection(sbf.toString());
 		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stsel,this);
 		
 		return true;
 	}
 	
 	/**
 	 * getClipboardContent
 	 * 
 	 * updates the table with contents from the clipboard.
 	 * returns false if pasting failed.
 	 * 
 	 * @param table
 	 * 
 	 * @return
 	 */
 	public boolean getClipboardContents(CoreEditorTable table)
 	{
 		if( table != copyTable )
 		{
 			PopupDialogFactory.pasteDatabaseError();
 			return false;
 		}
 		int startRow = (table.getSelectedRows())[0];
 		int startCol = (table.getSelectedColumns())[0];
 
 		if( this.selectedStartColumn != startCol) 
 		{
 			PopupDialogFactory.pasteDatabaseError();
 			return false;
 		}
 
 		try
 		{
 			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
 			String trstring = (String)(clipboard.getContents(this).getTransferData(
 					DataFlavor.stringFlavor
 				));
         	
 			StringTokenizer st1 = new StringTokenizer(trstring,"\n");
 			for( int i = 0; st1.hasMoreTokens(); i++ )
 			{
 				String rowstring = st1.nextToken();
 				StringTokenizer st2 = new StringTokenizer(rowstring,"\t");
 				for( int j = 0; st2.hasMoreTokens(); j++ )
 				{
 					String value = (String)st2.nextToken();
 					if( startRow+i < table.getRowCount() 
 							&& startCol+j < table.getColumnCount() )
 					{
						if( value.startsWith("Vector[") )
						{
							// convert back to vector
							Vector<Object> vec = new Vector<Object>();
							String vecstring = value.substring("Vector[".length(), value.length()-1);
							StringTokenizer st3 = new StringTokenizer(vecstring,", ");
							while( st3.hasMoreTokens() )
								vec.add(st3.nextToken());
							
							table.setValueAt(vec,startRow+i,startCol+j);
							
						} else {
							table.setValueAt(value.trim(),startRow+i,startCol+j);
						}
 					}
 				}
 			}
 		} catch( Exception e ) {
 			ApplicationLogger.logWarning(e.getMessage());
 			return false;
 		}
 		return true;
 	}
 }
