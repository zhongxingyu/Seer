       /**
  * 
  */
 package no.whg.mini;
 
 import java.io.Serializable;
 
 /**
  * Class that holds the data of each object (or list item) in the table
  * @author Fredrik
  *
  */
 public class TableData implements Serializable {
     /**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private String type;
     private String varName;
     private String text;
     private String row;
     private String column;
     private String rows;
     private String columns;
     private String fill;
     private String anchor;
     private String optionsHeight;
     private String optionsWidth;
     private String optionsRows;
     private String optionsColumns;
     private boolean optionsScrollPane;
     private boolean optionsComboBox;
     private boolean optionsWordWrapping;
     private String[] optionsSpinnerListArray;
     private float optionsStartValue;
     private float optionsMinValue;
     private float optionsMaxValue;
     private float optionsStepValue;
     
     
     
     /**
 	 * @return the optionsWordWrapping
 	 */
 	public boolean isOptionsWordWrapping() {
 		return optionsWordWrapping;
 	}
 
 	/**
 	 * @param optionsWordWrapping the optionsWordWrapping to set
 	 */
 	public void setOptionsWordWrapping(boolean optionsWordWrapping) {
 		this.optionsWordWrapping = optionsWordWrapping;
 	}
 
 	/**
 	 * @return the optionsHeight
 	 */
 	public String getOptionsHeight() {
 		return optionsHeight;
 	}
 
 	/**
 	 * @param optionsHeight the optionsHeight to set
 	 */
 	public void setOptionsHeight(String optionsHeight) {
 		this.optionsHeight = optionsHeight;
 	}
 
 	/**
 	 * @return the optionsWidth
 	 */
 	public String getOptionsWidth() {
 		return optionsWidth;
 	}
 
 	/**
 	 * @param optionsWidth the optionsWidth to set
 	 */
 	public void setOptionsWidth(String optionsWidth) {
 		this.optionsWidth = optionsWidth;
 	}
 
 	/**
 	 * @return the optionsRows
 	 */
 	public String getOptionsRows() {
 		return optionsRows;
 	}
 
 	/**
 	 * @param optionsRows the optionsRows to set
 	 */
 	public void setOptionsRows(String optionsRows) {
 		this.optionsRows = optionsRows;
 	}
 
 	/**
 	 * @return the optionsColumns
 	 */
 	public String getOptionsColumns() {
 		return optionsColumns;
 	}
 
 	/**
 	 * @param optionsColumns the optionsColumns to set
 	 */
 	public void setOptionsColumns(String optionsColumns) {
 		this.optionsColumns = optionsColumns;
 	}
 
 	/**
 	 * @return the optionsScrollPane
 	 */
 	public boolean isOptionsScrollPane() {
 		return optionsScrollPane;
 	}
 
 	/**
 	 * @param optionsScrollPane the optionsScrollPane to set
 	 */
 	public void setOptionsScrollPane(boolean optionsScrollPane) {
 		this.optionsScrollPane = optionsScrollPane;
 	}
 
 	/**
 	 * @return the optionsComboBox
 	 */
 	public boolean isOptionsComboBox() {
 		return optionsComboBox;
 	}
 
 	/**
 	 * @param optionsComboBox the optionsComboBox to set
 	 */
 	public void setOptionsComboBox(boolean optionsComboBox) {
 		this.optionsComboBox = optionsComboBox;
 	}
 
 	/**
 	 * @return the optionsSpinnerListArray
 	 */
 	public String[] getOptionsSpinnerListArray() {
 		return optionsSpinnerListArray;
 	}
 
 	/**
 	 * @param optionsSpinnerListArray the optionsSpinnerListArray to set
 	 */
 	public void setOptionsSpinnerListArray(String[] optionsSpinnerListArray) {
 		this.optionsSpinnerListArray = optionsSpinnerListArray;
 	}
 
 	/**
 	 * @return the optionsStartValue
 	 */
 	public float getOptionsStartValue() {
 		return optionsStartValue;
 	}
 
 	/**
 	 * @param optionsStartValue the optionsStartValue to set
 	 */
 	public void setOptionsStartValue(float optionsStartValue) {
 		this.optionsStartValue = optionsStartValue;
 	}
 
 	/**
 	 * @return the optionsMinValue
 	 */
 	public float getOptionsMinValue() {
 		return optionsMinValue;
 	}
 
 	/**
 	 * @param optionsMinValue the optionsMinValue to set
 	 */
 	public void setOptionsMinValue(float optionsMinValue) {
 		this.optionsMinValue = optionsMinValue;
 	}
 
 	/**
 	 * @return the optionsMaxValue
 	 */
 	public float getOptionsMaxValue() {
 		return optionsMaxValue;
 	}
 
 	/**
 	 * @param optionsMaxValue the optionsMaxValue to set
 	 */
 	public void setOptionsMaxValue(float optionsMaxValue) {
 		this.optionsMaxValue = optionsMaxValue;
 	}
 
 	/**
 	 * @return the optionsStepValue
 	 */
 	public float getOptionsStepValue() {
 		return optionsStepValue;
 	}
 
 	/**
 	 * @param optionsStepValue the optionsStepValue to set
 	 */
 	public void setOptionsStepValue(float optionsStepValue) {
 		this.optionsStepValue = optionsStepValue;
 	}
 
 	public TableData() {
         type = "JButton";
         varName = "var";
         text = "text";
         row = "1";
         column = "1";
         rows = "1";
         columns = "1";
        fill = "NONE";
        anchor = "CENTER";
     }
     
     /**
      * @param varName
      */
     public TableData(String varName) {
    	type = "JButton";
         this.varName = varName;
         text = "text";
         row = "1";
         column = "1";
         rows = "1";
         columns = "1";
         fill = "1";
         anchor = "";
         
         System.out.println(optionsScrollPane);
     }
     /**
      * @param type
      * @param varName
      * @param text
      * @param row
      * @param column
      * @param rows
      * @param columns
      * @param fill
      * @param anchor
      */
     public TableData(String type, String varName, String text, String row,
             String column, String rows, String columns, String fill, String anchor) {
         super();
         this.type = type;
         this.varName = varName;
         this.text = text;
         this.row = row;
         this.column = column;
         this.rows = rows;
         this.columns = columns;
         this.fill = fill;
         this.anchor = anchor;
     }
     /**
      * @return the type
      */
     public String getType() {
         return type;
     }
     /**
      * @param type the type to set
      */
     public void setType(String type) {
         this.type = type;
     }
     /**
      * @return the varName
      */
     public String getVarName() {
         return varName;
     }
     /**
      * @param varName the varName to set
      */
     public void setVarName(String varName) {
         this.varName = varName;
         
     }
     /**
      * @return the text
      */
     public String getText() {
         return text;
     }
     /**
      * @param text the text to set
      */
     public void setText(String text) {
         this.text = text;
     }
     /**
      * @return the row
      */
     public String getRow() {
         return row;
     }
     /**
      * @param row the row to set
      */
     public void setRow(String row) {
         this.row = row;
     }
     /**
      * @return the column
      */
     public String getColumn() {
         return column;
     }
     /**
      * @param column the column to set
      */
     public void setColumn(String column) {
         this.column = column;
     }
     /**
      * @return the rows
      */
     public String getRows() {
         return rows;
     }
     /**
      * @param rows the rows to set
      */
     public void setRows(String rows) {
         this.rows = rows;
     }
     /**
      * @return the columns
      */
     public String getColumns() {
         return columns;
     }
     /**
      * @param columns the columns to set
      */
     public void setColumns(String columns) {
         this.columns = columns;
     }
     /**
      * @return the fill
      */
     public String getFill() {
         return fill;
     }
     /**
      * @param fill the fill to set
      */
     public void setFill(String fill) {
         this.fill = fill;
         
         if(this.fill.equals("0"))
         {
         	this.fill = "BOTH";
         }
         else if(this.fill.equals("1"))
         {
         	this.fill = "HORIZONTAL";
         }
         else if(this.fill.equals("2"))
         {
         	this.fill = "NONE";
         }
         else if(this.fill.equals("3"))
         {
         	this.fill = "VERTICAL";
         }
     }
     /**
      * @return the anchor
      */
     public String getAnchor() {
         return anchor;
     }
     /**
      * @param anchor the anchor to set
      */ 
     public void setAnchor(String anchor) {
         this.anchor = anchor;
         
         if(this.anchor.equals("0"))
         {
         	this.anchor = "CENTER";    			
         }
         else if(this.anchor.equals("1"))
         {
         	this.anchor = "EAST";
         }
         else if(this.anchor.equals("2"))
         {
         	this.anchor = "NORTH";
         }
         else if(this.anchor.equals("3"))
         {
         	this.anchor = "NORTHEAST";
         }
         else if(this.anchor.equals("4"))
         {
         	this.anchor = "NORTHWEST";
         }
         else if(this.anchor.equals("5"))
         {
         		this.anchor = "SOUTH";
         }
         else if(this.anchor.equals("6"))
         {
         	this.anchor = "SOUTHEAST";
         }
         else if(this.anchor.equals("7"))
         {
         	this.anchor = "SOUTHWEST";
         }
         else if(this.anchor.equals("8"))
         {
         	this.anchor = "SOUTHWEST";
         }
     }
 
 }
