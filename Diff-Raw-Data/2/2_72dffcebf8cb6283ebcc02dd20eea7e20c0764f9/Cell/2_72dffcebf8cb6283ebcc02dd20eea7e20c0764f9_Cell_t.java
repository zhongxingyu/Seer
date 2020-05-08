 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 package org.amanzi.splash.swing;
 
 import java.awt.Color;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.net.URI;
 import java.text.Format;
 import java.text.MessageFormat;
 import java.text.ParseException;
 
 import org.amanzi.neo.core.database.nodes.CellID;
 import org.amanzi.neo.core.utils.ActionUtil;
 import org.amanzi.splash.ui.SplashPlugin;
 import org.amanzi.splash.utilities.Messages;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.ErrorDialog;
 
 import com.eteks.openjeks.format.CellFormat;
 
 public class Cell implements Serializable
 {
     /** long serialVersionUID field */
     private static final long serialVersionUID = -1270174718451028243L;
 
     /*
      * Default value of Cell
      */
     public static final String DEFAULT_VALUE = "";
     
     /*
      * Default definition of Cell
      */
     public static final String DEFAULT_DEFINITION = "";
     
     public static final String CELL_CYLIC_ERROR = "ERROR:cyclic";
     
     private/* transient */Object value;
 	private String definition;
 	private Cell cellGraphInfo;
     private transient CellFormat cellFormat;
 	
 	private CellID cellID;
 	private int row;
 	private int column;
 	
 	//Lagutko: new attributes 
 	private URI scriptURI;
 	private boolean hasReference;
 	
 	/*
 	 * Is exception appeared on formatting a value
 	 */
 	private boolean isFormatError;
 	
 	/**
 	 * Another constructor (create an expression using definition and value), used with 
 	 * file loading mechanism
 	 * @param definition
 	 * @param value
 	 */
 	public Cell (String definition, String value)
 	{
 		this.definition = definition;
 		this.value = value;
 		
 		cellFormat = new CellFormat();
 		//Lagutko: Cell hasn't reference to script on creation
 		hasReference = false;
 	}
 	
 	public void renameCell(String oldCellID, String newCellID)
 	{
 		setCellID(new CellID(newCellID));		
 		setRow(getCellID().getRowIndex());
 		setColumn(getCellID().getColumnIndex());
 		
 	}
 	
 	/**
 	 * Constructor using row and column
 	 * @param row
 	 * @param column
 	 */
 	public Cell (int    row,int    column)
 	{
 		this.row    = row;
 		this.column = column;
 		this.value = DEFAULT_VALUE;
 		this.definition = DEFAULT_DEFINITION;
 		cellFormat = new CellFormat();
 		hasReference = false;
 	}
 
 	public Cell(int row, int column, String definition, Object value,
 			CellFormat c) {
 		this.row    = row;
 		this.column = column;
 		
 		this.definition = definition;
 		//SplashJRubyInterpreter s = new SplashJRubyInterpreter();
 		this.value = value;
 		
 		this.cellID = new CellID(row, column);
 		
 		cellFormat = c;
 		hasReference = false;
 	}
 	
 	public Cell(Object value, String definition,
 			Cell cellGraphInfo, CellFormat cellFormat) {
 		super();
 		this.value = value;
 		this.definition = definition;
 		this.cellGraphInfo = cellGraphInfo;
 		
 		cellFormat = new CellFormat();
 		
 		this.cellFormat = cellFormat;
 		hasReference = false;
 	}
 
 	public Cell(Object value, String definition,
 			Cell cellGraphInfo) {
 		super();
 		this.value = value;
 		this.definition = definition;
 		this.cellGraphInfo = cellGraphInfo;
 		cellFormat = new CellFormat();
 		hasReference = false;
 	}
 
 	/**
 	 * Get value
 	 * @return
 	 */
 	public Object getValue ()
 	{
 		return value;
 	}
 
 	/**
 	 * Set value to null
 	 */
 	public void invalidateValue ()
 	{
 		value = null;
 	}
 
 	public String getDefinition() {
 		return definition;
 	}
 
 	public void setDefinition(String definition) {
 		this.definition = definition;
 	}
 
 	public Cell getCellGraphInfo() {
 		return cellGraphInfo;
 	}
 
 	public void setCellGraphInfo(Cell cellGraphInfo) {
 		this.cellGraphInfo = cellGraphInfo;
 	}
 
 	public CellFormat getCellFormat() {
 		return cellFormat;
 	}
 
 	public void setCellFormat(CellFormat cellFormat) {
 	    //Lagutko, 5.10.2009, if format was changed than we should update value of Cell with new format
 	    boolean formatChanged = this.cellFormat.getFormat() != cellFormat.getFormat();
 	    CellFormat previousFormat = this.cellFormat;
 	    this.cellFormat = cellFormat;
 	    
 	    //Lagutko, 6.10.2009, if Format of Cell was not changed
 	    //than it's no need to update value
 	    Format prevDataFormat = previousFormat.getFormat();
 	    Format currentDataFormat = cellFormat.getFormat();
 	    if ((prevDataFormat != null) &&
 	        (currentDataFormat != null) &&
 	        previousFormat.getFormat().getClass().equals(cellFormat.getFormat().getClass())) {
 	        return;
 	    }
 	    
 	    if (formatChanged) {
 	        Object value = getValue();
 	        //Lagutko, 6.10.2009, if Value is Date than convert is to String using Format
	        if ((prevDataFormat != null) && !isEmpty(value) && (!(value instanceof String))) {
 	            setValue(previousFormat.getFormat().format(value));
 	        }
 	        else {
 	            //otherwise use only toString()
 	            setValue(value.toString());
 	        }
 	        //if there was an error on changing format than set previous format
 	        if (isFormatError) {
 	            this.cellFormat = previousFormat;
 	        }
 	    }
 	}
 	
 	/**
 	 * Is Value empty?
 	 *
 	 * @param value value to check
 	 * @return is contains null or empty string
 	 */
 	private boolean isEmpty(Object value) {
 	    String stringValue = value.toString();
 	    return ((value == null) || (stringValue.length() == 0));
 	}
 	
 	public void setValue(String value) {
 	    isFormatError = false;
 
 	    //Lagutko, 5.10.2009, format a value from String to Object 
 	    if ((cellFormat.getFormat() == null) || 
 	        isEmpty(value) ||
 	        (cellFormat.getFormat() instanceof MessageFormat)) {
 	        
 	        //if there are no format or value is empty than didn't convert a value
 	        this.value = value;
 	    }
 	    else {
 	        try {
 	            //otherwise try to convert to given type	            
 	            this.value = cellFormat.getFormat().parseObject(value);	            
 	        }
 	        catch (final ParseException e) {
 	            //if it's unable to convert than show a Error Dialog
 	            ActionUtil.getInstance().runTask(new Runnable() {
 	                public void run() {	                    
 	                    ErrorDialog.openError(null, Messages.Format_Error_Title,	                                          
 	                                          Messages.Format_Error_Message, 
 	                                          new Status(Status.ERROR, SplashPlugin.getId(), e.getMessage()));
 	                }
 	            }, false);	
 	            isFormatError = true;
 	        }
 	    }
 	}
 	
 	/**
 	 * Returns is Exception appeared on formatting a value
 	 *
 	 * @return is exception appeared
 	 */
 	public boolean isFormatError() {
 	    return isFormatError;
 	}
 
 	public CellID getCellID() {
 		return cellID;
 	}
 
 	public void setCellID(CellID cellID) {
 		this.cellID = cellID;
 	}
 
 	public int getRow() {
 		return row;
 	}
 
 	public void setRow(int row) {
 		this.row = row;
 	}
 
 	public int getColumn() {
 		return column;
 	}
 
 	public void setColumn(int column) {
 		this.column = column;
 	}
 
 	
 	/**
 	 * Check equality
 	 */
 	public boolean equals (Object object)
 	{
 		return    object instanceof Cell
 		&& ((Cell)object).row == row
 		&& ((Cell)object).column == column;
 	}
 
 	/**
 	 * return hash code
 	 */
 	public int hashCode ()
 	{
 		return (row % 0xFFFF) | ((column % 0xFFFF) << 16);
 	}
 
 	/**
 	 * convert to string
 	 */
 	public String toString ()
 	{
 		return row + " " + column;
 	}
 	
 	//Lagutko: getter and setter for script name
 	
 	/**
 	 * Set name of script and sets that cell has reference to script
 	 * @param newScriptName name of scipt
 	 * @author Lagutko_N
 	 */
 	public void setScriptURI(URI newScriptName) {
 		scriptURI = newScriptName;
 		hasReference = scriptURI != null;		
 	}
 	
 	/**
 	 * Returns name of script
 	 * 
 	 * @return name of script
 	 * @author Lagutko_N
 	 */
 	public URI getScriptURI() {
 		return scriptURI;
 	}
 	
 	/**
 	 * Is this cell has reference to script?
 	 * 
 	 * @return Is this cell has reference to script?
 	 * @author Lagutko_N
 	 */
 	public boolean hasReference() {
 		return hasReference;
 	}
 
     private void writeObject(ObjectOutputStream stream) throws IOException {
         stream.defaultWriteObject();
         stream.writeObject(cellFormat.getBackgroundColor());
         stream.writeObject(cellFormat.getFontColor());
         stream.writeObject(cellFormat.getFontName());
         stream.writeObject(cellFormat.getFontSize());
         stream.writeObject(cellFormat.getFontStyle());
         stream.writeObject(cellFormat.getFormat());
         stream.writeObject(cellFormat.getHorizontalAlignment());
         stream.writeObject(cellFormat.getVerticalAlignment());
     }
 
     private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
         stream.defaultReadObject();
         cellFormat = new CellFormat();
         cellFormat.setBackgroundColor((Color)stream.readObject());
         cellFormat.setFontColor((Color)stream.readObject());
         cellFormat.setFontName((String)stream.readObject());
         cellFormat.setFontSize((Integer)stream.readObject());
         cellFormat.setFontStyle((Integer)stream.readObject());
         cellFormat.setFormat((Format)stream.readObject());
         cellFormat.setHorizontalAlignment((Integer)stream.readObject());
         cellFormat.setVerticalAlignment((Integer)stream.readObject());
     }
 }
