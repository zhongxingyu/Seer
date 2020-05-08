 package edu.cudenver.bios.powercalculator.client.panels;
 
 import java.util.ArrayList;
 
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.MouseOutEvent;
 import com.google.gwt.event.dom.client.MouseOutHandler;
 import com.google.gwt.event.dom.client.MouseOverEvent;
 import com.google.gwt.event.dom.client.MouseOverHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 import edu.cudenver.bios.powercalculator.client.PowerCalculatorGUI;
 
 public class ResizableMatrix extends Composite 
 implements ChangeHandler, MetaDataListener
 {
     protected static final String RANDOM = "random";
     protected static final String MATRIX_STYLE = "matrix";
 	protected static final String DIMENSION_STYLE = "matrixDimensions";
 	protected static final String MATRIX_DATA_STYLE = "matrixData";
 	protected static final String MATRIX_CELL_STYLE = "matrixCell";
 	protected static final String HEADER_STYLE = "matrixHeader";
 	protected static final String COLUMN_META_DATA_STYLE = "columnMetaData";
 	protected static final String ROW_META_DATA_STYLE = "rowMetaData";
 	protected static final String DEFAULT_VALUE = "0";
 	protected static final int MAX_ROWS = 50;
 	protected static final int MAX_COLS = 50;
 	protected Grid matrixData;
 	protected TextBox rowTextBox;
 	protected TextBox columnTextBox;
 	protected HTML headerHTML;
 	protected boolean isSquare;
 	protected boolean isSymmetric;
 	protected String name;
 	protected ArrayList<MatrixResizeListener> resizeListeners = new ArrayList<MatrixResizeListener>();
 	protected ArrayList<MetaDataListener> metaDataListeners = new ArrayList<MetaDataListener>();
 	protected boolean hasMetaData = false;
 	
 	public ResizableMatrix(String name, String label, String details, 
 	        int rows, int cols, boolean hasMetaData) 
 	{
 	    this.name = name;
 	    this.hasMetaData = hasMetaData;
 		// overall layout panel    
 	    VerticalPanel matrixPanel = new VerticalPanel();
 
 		// pop-up with matrix description
 		final PopupPanel detailsPanel = new PopupPanel();
 		detailsPanel.add(new HTML(details));
 		
 		// matrix name
 		headerHTML = new HTML(label);
 		headerHTML.addMouseOverHandler(new MouseOverHandler() {
 			public void onMouseOver(MouseOverEvent e)
 			{
 				detailsPanel.showRelativeTo(headerHTML);
 			}
 		});
 		headerHTML.addMouseOutHandler(new MouseOutHandler() {
 			public void onMouseOut(MouseOutEvent e)
 			{
 				detailsPanel.hide();
 			}
 		});
 		// matrix dimensions
 		rowTextBox = new TextBox();
 		rowTextBox.addChangeHandler(this);
 		rowTextBox.setValue(Integer.toString(rows), false);
 		columnTextBox = new TextBox();
 		columnTextBox.addChangeHandler(this);
 		columnTextBox.setValue(Integer.toString(cols), false);
 		
 		// layout the matrix dimensions
 		HorizontalPanel matrixDimensions = new HorizontalPanel();
 		matrixDimensions.add(rowTextBox);
 		matrixDimensions.add(new HTML(PowerCalculatorGUI.constants.matrixDimensionSeparator()));
 		matrixDimensions.add(columnTextBox);
 	    
 		// build matrix itself
 	    if (hasMetaData)
 	    	matrixData = new Grid(rows+1, cols+1); // +1 for row and column meta data
 	    else
 	    	matrixData = new Grid(rows, cols);
 	    
 		// initialize cells to 0
 		initMatrixData();
 		
 		// add the widgets to the vertical panel
 		matrixPanel.add(headerHTML);
 		matrixPanel.add(matrixDimensions);
 		matrixPanel.add(matrixData);
 		
 		// set up styles
 		matrixPanel.setStyleName(MATRIX_STYLE);
 		headerHTML.setStyleName(HEADER_STYLE);
 		matrixDimensions.setStyleName(DIMENSION_STYLE);
 		matrixData.setStyleName(MATRIX_DATA_STYLE);
 		
 		// initialize the widget
 		initWidget(matrixPanel);
 	}
 	
 	public void setIsSquare(boolean isSquare)
 	{
 		this.isSquare = isSquare;
 		if (isSquare)
 		{
 			int rows = Integer.parseInt(rowTextBox.getValue());
 			int cols = Integer.parseInt(columnTextBox.getValue());
 			if (rows != cols)
 			{
 				columnTextBox.setText(rowTextBox.getValue());
 			}
 		}
 		columnTextBox.setEnabled(!isSquare);
 	}
 	
 	public void setIsSymmetric(boolean isSymmetric)
 	{
 		this.isSymmetric = isSymmetric;
 
 	}
 	
 	public void onChange(ChangeEvent event)
 	{
 		int oldRows = (hasMetaData ? matrixData.getRowCount()-1 : matrixData.getRowCount());
 		int oldCols = (hasMetaData ? matrixData.getColumnCount()-1 : matrixData.getColumnCount());
 		int newRows = 0;
 		int newCols = 0;
 		try
 		{
 			newRows = Integer.parseInt(rowTextBox.getValue());
 			if (isSquare) columnTextBox.setText(rowTextBox.getValue());
 			newCols = Integer.parseInt(columnTextBox.getValue());
 			
 			if (!resizeMatrix(newRows, newCols)) throw new IllegalArgumentException("failed to set dimension");
 			notifyOnMatrixResize(newRows, newCols);
 		}
 		catch (Exception e)
 		{ 
 			rowTextBox.setText(Integer.toString(oldRows));
 			columnTextBox.setText(Integer.toString(oldCols));
 			Window.alert(PowerCalculatorGUI.constants.errorMatrixDimensionInvalid());
 		}
 	}
 	
 	public void setDimensions(int newRows, int newCols)
 	{
 		resizeMatrix(newRows, newCols);
 	}
 	
 	private boolean resizeMatrix(int rows, int cols)
 	{		
 		int newRows = rows;
 		int newCols = cols;
 		int maxRows = (hasMetaData ? MAX_ROWS+1 : MAX_ROWS);
 		int maxCols = (hasMetaData ? MAX_COLS+1 : MAX_COLS);
 		if (newRows > 0 && newRows <= maxRows && newCols > 0 && newCols <= maxCols)
 		{	
 			if (hasMetaData)
 			{
 				newRows++;
 				newCols++;
 			}
 			if (isSquare && newRows != newCols) return false;
 			
 			int oldRows = matrixData.getRowCount();
 			int oldCols = matrixData.getColumnCount();
 			if (newCols != oldCols || newRows != oldRows) 
 			{
 				rowTextBox.setText(Integer.toString(rows));
 				columnTextBox.setText(Integer.toString(cols));
 				matrixData.resize(newRows, newCols);
 				if (newCols != oldCols)
 				{
 					for(int c = oldCols; c < newCols; c++) initColumn(c);
 				}
 				if (newRows != oldRows)
 				{
 					for(int r = oldRows; r < newRows; r++) initRow(r);
 				}
 			}
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 	
 	public int getRowDimension()
 	{
 		return matrixData.getRowCount();
 	}
 	
 	public int getColumnDimension()
 	{
 		return matrixData.getColumnCount();
 	}
 	
 	private void initMatrixData()
 	{
 		for(int r = 0; r < matrixData.getRowCount(); r++)
 		{
 			initRow(r);
 		}
 	}
 	
 	private void initRow(int row)
 	{
 		if (hasMetaData && row == 0)
 		{
 			matrixData.setWidget(0, 0, new HTML("Row Label"));
 			for(int c = 1; c < matrixData.getColumnCount(); c++)
 			{
 				matrixData.setWidget(0, c, new ColumnMetaDataEntry(c, this));
 			}
 		}
 		else
 		{
 			if (hasMetaData) matrixData.setWidget(row, 0, new RowMetaDataEntry(row, this));
 			for(int c = (hasMetaData ? 1 : 0); c < matrixData.getColumnCount(); c++)
 			{
 				TextBox textBox = new TextBox();
 				textBox.setValue(DEFAULT_VALUE);
 				textBox.setStyleName(MATRIX_CELL_STYLE);
 				matrixData.setWidget(row, c, textBox);
 			}
 		}
 	}
 	
 	private void initColumn(int col)
 	{
 		if (hasMetaData && col == 0)
 		{
 			for(int r = 1; r < matrixData.getRowCount(); r++)
 			{
 				matrixData.setWidget(r, col, new RowMetaDataEntry(r, this));
 			}
 		}
 		else 
 		{
 			if (hasMetaData) matrixData.setWidget(0, col, new ColumnMetaDataEntry(col, this));
 			for(int r = (hasMetaData ? 1 : 0); r < matrixData.getRowCount(); r++)
 			{
 				TextBox textBox = new TextBox();
 				textBox.setValue(DEFAULT_VALUE);
 				textBox.setStyleName(MATRIX_CELL_STYLE);
 				matrixData.setWidget(r, col, textBox);
 			}
 		}
 	}
 	
 	public String columnMetaDataToXML()
 	{
 		StringBuffer buffer = new StringBuffer();
 		buffer.append("<columnMetaData>");
 		for(int c = 1; c < matrixData.getColumnCount(); c++)
 		{
 			ColumnMetaDataEntry colMD = (ColumnMetaDataEntry) matrixData.getWidget(0, c);
 			buffer.append(colMD.toXML());
 		}
		buffer.append("</columnMetaData>");
 		return buffer.toString();
 	}
 	
 	public String matrixDataToXML()
 	{
 		StringBuffer buffer = new StringBuffer();
 		
		int start = 0;
		int rows = matrixData.getRowCount();
		int cols = matrixData.getColumnCount();
		if (hasMetaData)
		{
			start = 1;
			rows--;
			cols--;
		}
		buffer.append("<matrix name='" + name + "' rows='" +  rows + "' columns='" + 
		        cols + "'>");
 
 		for(int r = start; r < matrixData.getRowCount(); r++)
 		{
 			buffer.append("<r>");
 			for(int c = start; c < matrixData.getColumnCount(); c++)
 			{
 				TextBox txt = (TextBox) matrixData.getWidget(r, c);
 				buffer.append("<c>" + txt.getValue() + "</c>");
 			}
 			buffer.append("</r>");
 		}
 		buffer.append("</matrix>");
 
 		return buffer.toString();
 	}
 	
 	private void notifyOnMatrixResize(int rows, int cols)
 	{
 		for(MatrixResizeListener listener: resizeListeners) listener.onMatrixResize(rows, cols);
 	}
 		
 	public void addMatrixResizeListener(MatrixResizeListener listener)
 	{
 		resizeListeners.add(listener);
 	}
 	
 	public void addMetaDataListener(MetaDataListener listener)
 	{
 		if (hasMetaData)
 		{
 			metaDataListeners.add(listener);
 		}
 	}
 	
     public void onFixed(int col)
     {
     	// kick the callback up the chain
     	for(MetaDataListener listener: metaDataListeners) listener.onFixed(col);
     }
     
     public void onRandom(int col, double mean, double variance)
     {
     	if (hasMetaData)
     	{
     		// reset the other 
     		for(int c = 1; c < matrixData.getColumnCount(); c++)
     		{
     			if (c != col)
     			{
     				ColumnMetaDataEntry colMD = (ColumnMetaDataEntry) matrixData.getWidget(0, c);
     				colMD.setFixed();
     			}
     		}     
     	}
     	// kick the callback up the chain
     	for(MetaDataListener listener: metaDataListeners) listener.onRandom(col, mean, variance);
     	
     }
     
     public void onRowName(int row, String name)
     {
     	// kick the callback up the chain
     	for(MetaDataListener listener: metaDataListeners) listener.onRowName(row, name);
     }
 }
