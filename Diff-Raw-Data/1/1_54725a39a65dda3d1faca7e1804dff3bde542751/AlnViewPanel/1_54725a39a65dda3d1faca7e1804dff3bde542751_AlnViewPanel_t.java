 package newgui.gui.alignmentViewer;
 
 import gui.ErrorWindow;
 
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.MouseInfo;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.BitSet;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.JComponent;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JTextField;
 import javax.swing.JViewport;
 import javax.swing.Timer;
 
 
 import newgui.UIConstants;
 import newgui.alignment.UnrecognizedBaseException;
 import newgui.datafile.AlignmentFile;
 import newgui.datafile.XMLConversionError;
 import newgui.gui.ViewerWindow;
 import newgui.gui.alignmentViewer.rowPainters.AbstractRowPainter;
 import newgui.gui.alignmentViewer.rowPainters.GC_AT_RowPainter;
 
 import sequence.Alignment;
 import sequence.AlignmentMask;
 import sequence.BasicSequenceAlignment;
 import sequence.Sequence;
 import sequence.SimpleSequence;
 
 
 /**
  * A panel that displays a group of sequences in tabular form. In an effort to improve performance, we use
  * a image drawing scheme similar to that used by SequenceFigure, in which we draw and retain BufferedImages
  * that constitute the column header, row header, and main content area, and redraw them only as needed (as
  * opposed to having paintComponent always redraw everything). 
  * 
  *  This thing also supports a semi-complex selection scheme, which has two modes, row selection and column
  * selection, indicated by the state of the selectionMode variablable. Dragging over columns in the content area
  * turns column selection on, dragging over rows on the rowHeader turns row selection on (and clears the column
  * selection). Individually selected rows / columns are indicated by the set bits in the 'selection' bitset, 
  * that is, if row selection is on, then row i is selected if selection.get(i) is true. 
  * 
  *  
  * @author brendan
  *
  */
 public class AlnViewPanel extends JPanel {
 
 	public static final Color selectionRegionColor = new Color(10, 100, 250, 130); //Color of selection region 
 	
 	int[] selectedRows;
 	int[] selectedColumns;
 	
 	public enum Selection {ROWS, COLUMNS};
 	
 	Selection selectionMode = Selection.COLUMNS;
 	
 	JScrollPane scrollPane = null;
 	RowHeader rowHeader = new RowHeader(this);
 	AlnViewColumnHeader colHeader = new AlnViewColumnHeader(this);
 	
 	boolean dragOffEdge = false; //True if we're dragging off the edges of the sequences
 	int dragStart = -1;
 	int dragEnd = -1;
 	
 	int columnWidth = 10; 	//Initial width of columns
 	int rowHeight = 20;		//Initial height of rows
 	int rowHeaderWidth = 50;	//Initial width of row header
 	
 	JViewport viewport;
 	
 	Font headerFont = new Font("Sans", Font.PLAIN, 12);
 	
 	Alignment seqs;
 	
 	private JTextField seqRenamer;
 	
 	//Cursor shape when mouse is over right edge of row header
 	Cursor edgeAdjustCursor = new Cursor(Cursor.W_RESIZE_CURSOR);
 	
 	//Interpretation depends on selectionMode, but indicates in true/false fashion or not row/col is selected
 	BitSet selection = new BitSet(); 
 	
 	AbstractRowPainter rowPainter = null;
 	SGRowHeaderPainter headerPainter = new DefaultHeaderPainter();
 	
 	BufferedImage rowHeaderImage;
 	
 	int prevRowHeaderWidth = -1; //We flag these to see if we need to re-construct various images
 	int prevColHeaderWidth = -1;
 	int prevContentHeight = -1;
 	int prevContentWidth = -1;
 	boolean redrawImages = true;
 	
 	boolean first = true;
 	
 	Color flashColor = new Color(250, 250, 250, 150);
 	Color selectionColor = selectionRegionColor;
 	
 	int zeroColumn = -1; //The column in the display that the user sees as 'column zero', this is shadowed in the columnHeader
 	List<ZeroColumnListener> zeroColListeners = new ArrayList<ZeroColumnListener>(2);
 	
 	Timer flashTimer; //Used to 'flash' the selected sequences
 	private boolean flashing = false; //Whether or not we are in the middle of a flash
 	
 	//When the user drags the mouse off the edge of the screen, we start this so that we can 
 	//continue to scroll the viewport to the correct spot, even if new mouseDrag events are
 	//not fired
 	Timer dragOffEdgeTimer;
 	
 	public AlnViewPanel() {
 		
 		PanelMouseListener mouseListener = new PanelMouseListener();
 		addMouseListener(mouseListener);
 		addMouseMotionListener(mouseListener);
 	
 		addZeroColumnListener(colHeader);
 		setBackground(Color.LIGHT_GRAY);
 		
 		seqs = new BasicSequenceAlignment();
 		
 		initializePopup();
 		
 		dragOffEdgeTimer = new Timer(30, new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				updateViewportLocation();
 			}
 		});
 	}
 	
 	
 	/**
 	 * Construct the popup menu for the main content area
 	 */
 	private void initializePopup() {
 	      /// Popup Menu ///
 
 		popup = new JPopupMenu();
 		popup.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY) );
 		
 		JMenuItem popupItemNew = new JMenuItem("New from selection");
 		popupItemNew.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 newAlignmentFromSelection();
             }
         });
 		popup.add(popupItemNew);		
 		
 		
 		JMenuItem popupItemRemoveSelection = new JMenuItem("Remove Selection");
 		popupItemRemoveSelection.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 removeSelection();
             }
         });
 		popup.add(popupItemRemoveSelection);
 		
 		PopupListener popupListener = new PopupListener(); 
 		addMouseListener(popupListener);
 	}
 	
 	
 	protected void newAlignmentFromSelection() {
 		if (getNumSelectedColumns()>0) {
 			Alignment aln = getAlignment().newAlignmentFromColumns(getSelectedColumns());			
 			AlignmentFile file = new AlignmentFile(aln);			
 			ViewerWindow.getViewer().getFileManager().showSaveDialog(file, "new_alignment");
 
 			return;
 		}
 		
 		if (getNumSelectedRows()>0) {
 			Alignment aln = new BasicSequenceAlignment();
 			int[] rows = getSelectedRows();
 			for(int i=0; i<rows.length; i++) {
 				Sequence seq = getSequenceForRow(rows[i]);
 				try {
 					aln.addSequence( new SimpleSequence( seq.getLabel(), seq.getSequenceString()));
 				} catch (UnrecognizedBaseException e) {
 					//Should never happen..
 					ErrorWindow.showErrorWindow(e, "Error creating new sequence");
 					e.printStackTrace();
 				}
 			}
 			if (aln.getSequenceCount()>0) {
 				AlignmentFile file = new AlignmentFile(aln);			
 				ViewerWindow.getViewer().getFileManager().showSaveDialog(file, "new_alignment");
 			}
 		}
 	}
 
 
 	/**
 	 * Called to associate a sequence group with this content panel. This initializes a handful
 	 * of default fields 
 	 * @param sg
 	 */
 	public void setAlignment(Alignment sg) {
 		seqs = sg;
 		
 		int nameWidth = getMaxNameLength(sg);
 		headerPainter.setFont(headerFont);
 		rowHeaderWidth = 20 + nameWidth*9;
 		
 		if (rowHeaderWidth > 200)
 			rowHeaderWidth = 200;
 		
 		Container vp = this.getParent();
 		if (vp instanceof JViewport) {
 			viewport = (JViewport) vp;
 			Container js = vp.getParent();
 			if (js instanceof JScrollPane) {
 				scrollPane = (JScrollPane)js;
 				scrollPane.setViewportBorder(null);
 				scrollPane.setRowHeaderView(rowHeader);
 				scrollPane.setColumnHeaderView(colHeader);
 				//The following corrects a confusing bug that incorrectly painted the column header on mac systems...
 				scrollPane.getColumnHeader().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
 			}
 		}
 		else {
 			throw new IllegalStateException("SGContentPanels must be contained in a scroll pane");
 		}
 		
 		if (!first) {
 			setToNaturalSize();
 			colHeader.drawColumnHeaderImage();
 			drawRowHeaderImage();
 			rowHeader.repaint();
 		}
 		
 		if (rowPainter != null)
 			rowPainter.setAlignment(sg);
 		else {
 			rowPainter = new GC_AT_RowPainter(sg);
 		}
 		this.setBackground(Color.white);
 	}
 
 	public void setRowPainter(AbstractRowPainter rowPainter) {
 		this.rowPainter = rowPainter;
 		redrawImages = true;
 		repaint();
 	}
 	
 	/**
 	 * Starts a timer that causes the selection color to briefly change, then revert. 
 	 */
 	public void flashSequences() {
 		if (flashTimer==null) {
 			
 			flashTimer = new Timer(100, new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					switchFlash();
 				}
 			});
 			flashTimer.setInitialDelay(100);
 		}
 		
 		selectionColor = flashColor;
 		this.paintImmediately(viewport.getViewRect());
 		rowHeader.paintImmediately(0, 0, rowHeader.getWidth(), rowHeader.getHeight());
 		flashTimer.start();
 	}
 
 	/**
 	 * Returns the number of characters in the longest sequence label of the given alignment
 	 * @param aln
 	 * @return
 	 */
 	private static int getMaxNameLength(Alignment aln) {
 		int max = 0;
 		for(String label : aln.getLabels()) {
 			if (label.length() > max)
 				max = label.length();
 		}
 		return max;
 	}
 	
 	protected void switchFlash() {
 		selectionColor = selectionRegionColor;
 		this.paintImmediately(viewport.getViewRect());
 		rowHeader.paintImmediately(0, 0, rowHeader.getWidth(), rowHeader.getHeight());
 	}
 
 	/**
 	 * Get the column number that the user sees as column #0
 	 * @return
 	 */
 	public int getZeroColumn() {
 		return zeroColumn;
 	}
 	
 	/**
 	 * Return the number of selected rows, or zero if columns are selected
 	 * @return
 	 */
 	public int getNumSelectedRows() {
 		if (selectionMode == Selection.ROWS) {
 			
 			if (selection.length()>seqs.getSequenceCount())
 				selection.set(seqs.getSequenceCount(), selection.length(), false);
 			
 			return selection.cardinality();
 		}
 		return 0;
 	}
 	
 	/**
 	 * Return the number of selected columns, or zero if rows are selected
 	 * @return
 	 */
 	public int getNumSelectedColumns() {
 		if (selectionMode == Selection.COLUMNS) {
 			//If rows were previously selected and there were more rows than columns, some bits may still be 
 			//set beyond maxSeqLength columns, we need to clear these..
 			if (selection.length()>seqs.getSequenceLength())
 				selection.set(seqs.getSequenceLength()+1, selection.length(), false);
 			
 			int dragCols = Math.abs(dragStart - dragEnd);
 			
 			return selection.cardinality()+dragCols;
 		}
 		return 0;
 	}
 	
 	/**
 	 * Returns the row that contains the given point
 	 * @param p
 	 * @return
 	 */
 	public int getRowForPoint(Point p) {
 		return p.y / rowHeight;
 	}
 	
 	/**
 	 * Returns the column that contains the given point. Returns -1 if the header was clicked.
 	 * @param p
 	 * @return
 	 */
 	public int getColumnForPoint(Point p) {
 		int col = p.x / columnWidth;
 		if (col < 0)
 			col = -1;
 		return col;
 	}
 	
 	/**
 	 * Set the width of the columns to the specified number of pixels.
 	 * @param width
 	 */
 	public void setColumnWidth(int width) {
 		if (this.columnWidth != width) {
 			this.columnWidth = width;
 			
 			if (this.getGraphicsConfiguration() != null) {
 				colHeader.drawColumnHeaderImage();
 				//drawContentImage();
 			}
 			
 			setToNaturalSize();
 		}
 	}
 	
 	/**
 	 * Set the height in pixels of the rows
 	 * @param height
 	 */
 	public void setRowHeight(int height) {
 		this.rowHeight = height;
 	}
 	
 	/**
 	 * Compute the 'natural size' of this object, in which all columns have width columnWidth
 	 * and all rows have height rowHeight
 	 * @return
 	 */
 	public Dimension getNaturalSize() {
 		if (seqs == null || seqs.getSequenceCount()==0){
 			return new Dimension(100, 100);
 		}
 		
 		int width = columnWidth*seqs.getSequenceLength();
 		int height = seqs.getSequenceCount()*rowHeight;
 		return new Dimension(width, height);
 	}
 	
 	public int getColumnWidth() {
 		return columnWidth;
 	}
 	
 	public int getRowHeight() {
 		return rowHeight;
 	}
 	
 	/**
 	 * Set the size of this panel to be exactly that returned by getNaturalSize(), where width is 
 	 * rowHeaderWidth + 
 	 */
 	public void setToNaturalSize() {
 		Dimension size = getNaturalSize();
 		this.setMinimumSize(size);
 		this.setPreferredSize(size);
 		
 		colHeader.setMinimumSize(new Dimension(size.width, 2));
 		colHeader.setPreferredSize(new Dimension(size.width, colHeader.getTotalHeight()));
 		colHeader.revalidate();
 		
 		Dimension rowHeaderSize = new Dimension(rowHeaderWidth, size.height);
 		rowHeader.setMinimumSize(rowHeaderSize);
 		rowHeader.setPreferredSize(rowHeaderSize);
 		rowHeader.setMaximumSize(rowHeaderSize);
 
 		rowHeader.revalidate();
 		this.revalidate();
 		
 		scrollPane.repaint();
 	}
 	
 	
 	/**
 	 * Repaint the contentImage and selection area (if any)
 	 */
 	public void paintComponent(Graphics g) {
 		Graphics2D g2d = (Graphics2D)g;
 		Rectangle rect = this.getVisibleRect(); 
 		int xMin = rect.x;
 		int xMax = xMin + rect.width;
 		int yMin = rect.y;
 		int yMax = yMin + rect.height;
 		
 		if (redrawImages) {
 			drawAllImages();
 		}
 		
 		if (first) {
 			rowHeader.repaint();
 			colHeader.repaint();
 			first = false;
 		}
 		
 		g2d.setColor(Color.white);
 		g2d.fill(rect);
 
 		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 					RenderingHints.VALUE_ANTIALIAS_ON);
 		
 		int firstVisCol = (int)Math.floor( xMin / columnWidth );
 		int lastVisCol = (int)Math.floor( xMax / columnWidth )+1;
 		int firstRow = (int)Math.floor( yMin / rowHeight );
 		int lastRow = (int)Math.floor( yMax / rowHeight )+1;
 		
 		lastVisCol = Math.min(seqs.getSequenceLength(), lastVisCol);
 		
 		if (rowPainter == null) {
 			return;
 		}
 		
 		for(int row=firstRow; row<Math.min(seqs.getSequenceCount(), lastRow); row++) {
 			rowPainter.paintRow(g2d, row, firstVisCol, lastVisCol, 0, row*rowHeight, columnWidth, rowHeight);
 		}
 		
 		//Paint masked columns directly over bases/colors, but under the selection region (if it exists)
 		AlignmentMask mask = seqs.getMask();
 		if (mask!= null) {
 			int firstMaskedCol = mask.getFirstMaskedColumn();
 			int lastMaskedCol = mask.getLastMaskedColumn();
 			if (firstMaskedCol<lastVisCol && lastMaskedCol>firstVisCol) {
 				Integer[] maskedCols = mask.getMaskedColumns();
 				
 				char[] nChars = new char[]{'N'};
 				for(int i=0; i<maskedCols.length; i++) {
 					g2d.setColor(Color.GRAY);
 					g2d.fillRect(columnWidth*maskedCols[i], 0, columnWidth, getHeight());
 					g2d.setColor(Color.DARK_GRAY);
 					for(int j=0; j<seqs.getSequenceCount(); j++) {
 						g2d.drawChars(nChars, 0, 1, columnWidth*maskedCols[i], rowHeight*j+16);
 					}
 				}
 			}
 				
 		}
 		
 		if (selectionMode == Selection.COLUMNS) {
 			int i = selection.nextSetBit(Math.max(0, firstVisCol-1));
 			g2d.setColor(selectionColor);
 			int min = Math.min(dragStart, dragEnd);
 			int max = Math.max(dragStart, dragEnd);
 			max = Math.min(max, seqs.getSequenceLength());
 			
 			while (i>-1 && i<=lastVisCol) {
 				if (i<min || i>=max)
 					g2d.fillRect(i*columnWidth, 0, columnWidth, getHeight());
 				i = selection.nextSetBit(i+1);
 			}
 			
 
 			if  (Math.max(dragStart, dragEnd)>-1) {
 				int minX = min*columnWidth;
 				int maxX = max*columnWidth;
 				g2d.setColor(selectionColor);
 				g2d.fillRect(minX, 0, maxX-minX, getHeight());
 				g2d.setColor(Color.DARK_GRAY);
 				g2d.drawRect(minX, 0, maxX-minX, getHeight());
 				g2d.setColor(new Color(250, 250, 250, 100));
 				g2d.drawLine(minX-1, 0, minX-1, getHeight());
 				g2d.drawLine(maxX+1, 0, maxX+1, getHeight());
 			}
 		}
 		
 		if (selectionMode == Selection.ROWS) {
 			int min = Math.min(dragStart, dragEnd);
 			int max = Math.max(dragStart, dragEnd);
 			
 			if (Math.max(dragStart, dragEnd)>-1) {
 				int minY = min*rowHeight;
 				int maxY = max*rowHeight;
 				g2d.setColor(selectionColor);
 				g2d.fillRect(0, minY, getWidth(), maxY-minY);
 				g2d.setColor(Color.DARK_GRAY);
 				g2d.drawLine(0, minY, getWidth(), minY);
 				g2d.drawLine(0, maxY, getWidth(), maxY);
 
 				g2d.setColor(new Color(250, 250, 250, 100));
 				g2d.drawLine(0, minY-1, getWidth(), minY-1);
 				g2d.drawLine(0, maxY+1, getWidth(), maxY+1);
 			}
 			
 			int i = selection.nextSetBit(firstRow);
 			g2d.setColor(selectionColor);
 			while (i>-1 && i<=lastRow) {
 				if (i < min || i>= max)
 					g2d.fillRect(0, i*rowHeight, getWidth(), rowHeight);	
 				i = selection.nextSetBit(i+1);
 			}
 		}
 	}
 	
 	/**
 	 * Clear the current selection interval and set the bits from begin-end (inclusive, exclusive)
 	 * in the selection bitset
 	 * @param begin
 	 * @param end
 	 */
 	public void setSelectionInterval(int begin, int end) {
 		selection.clear();
 		int min = Math.min(begin, end);
 		int max = Math.max(begin, end);
 		min = Math.max(0, min); 
 		selection.set(min, max);
 		colHeader.repaint();
 	}
 	
 	/**
 	 * Set the row header width to the specified value, redraw the row header image, and repaint
 	 * @param x
 	 */
 	protected void setRowHeaderWidth(int x) {
 		rowHeaderWidth = x;
 		rowHeader.setMinimumSize(new Dimension(rowHeaderWidth, 1));
 		rowHeader.setPreferredSize(new Dimension(rowHeaderWidth, getHeight()));
 		rowHeader.setMaximumSize(new Dimension(rowHeaderWidth, Integer.MAX_VALUE));
 		rowHeader.revalidate();
 		drawRowHeaderImage();
 		scrollPane.repaint();
 	}
 	
 	/**
 	 * Unselect all rows / columns
 	 */
 	public void clearSelection() {
 		selection.clear();
 		dragStart = -1;
 		dragEnd = -1;
 		repaint();
 	}
 	
 	/**
 	 * Return the number of columns in the matrix
 	 * @return
 	 */
 	public int getColumnCount() {
 		return seqs.getSequenceLength();
 	}
 
 	/**
 	 * Select columns from start (inclusive) to end (inclusive)
 	 * @param start
 	 * @param end
 	 */
 	public void addColumnSelectionInterval(int start, int end) {
 		if (selectionMode == Selection.ROWS) {
 			selection.clear();
 			selectionMode = Selection.COLUMNS;
 		}
 		dragStart = -1;
 		dragEnd = -1;
 		//System.out.println("Selecting from " + start + " to " + end);
 		selection.set(start, end+1);
 		repaint();
 	}
 
 	/**
 	 * Return the number of rows in the table, which is equal to the number of sequences in the
 	 * current SG
 	 * @return
 	 */
 	public int getRowCount() {
 		return seqs.getSequenceCount();
 	}
 
 	/**
 	 * Sets row i to be selected. If the current selection mode is columns, this clears the current selection
 	 * and sets the mode to rows. 
 	 * @param i
 	 */
 	public void selectRow(int i) {
 		if (selectionMode == Selection.COLUMNS) {
 			selectionMode = Selection.ROWS;
 			selection.clear();
 		}
 	
 		selection.set(i);
 		rowHeader.repaint();
 		repaint();
 	}
 
 	/**
 	 * Return sequence i from the current sequence group
 	 * @param i
 	 * @return
 	 */
 	public Sequence getSequenceForRow(int i) {
 		return seqs.getSequence(i);
 	}
 	
 	/**
 	 * Obtain the current alignment displayed in this panel, may be null if alignment has
 	 * never been set
 	 * @return
 	 */
 	public Alignment getAlignment() {
 		return seqs;
 	}
 	
 	private void drawRowHeaderImage() {
 		Dimension natSize = getNaturalSize();
 		if (rowHeaderImage==null || rowHeaderWidth != prevRowHeaderWidth || prevContentHeight != natSize.height) {
 			prevContentHeight = natSize.height;
 			prevRowHeaderWidth = rowHeaderWidth;
 			rowHeaderImage = this.getGraphicsConfiguration().createCompatibleImage(rowHeaderWidth, natSize.height);
 		}
 		Graphics2D g2d = rowHeaderImage.createGraphics();
 		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                 RenderingHints.VALUE_ANTIALIAS_ON);
 		
 		g2d.setColor(getBackground());
 		g2d.fillRect(0, 0, getWidth(), natSize.height);
 
 		if (seqs == null) {
 			return;
 		}
 		for(int row=0; row<seqs.getSequenceCount(); row++) {
 			headerPainter.paintHeaderCell(g2d, row, 0, row*rowHeight, rowHeaderWidth, rowHeight, seqs);
 		}
 	}
 	
 	/**
 	 * Draw the content image, creating a new contentImage BufferedImage and associated graphics
 	 * if necessary. 
 	 */
 //	private void drawContentImage() {
 //		//We now draw the image on the fly
 //	}
 	
 	/**
 	 * Redraw the rowHeaderImage, columnHeaderImage, and contentImage. This happens when the
 	 * columnWidth or rowHeight has changed, or when the user has selected a new type of rowPainter
 	 */
 	public void drawAllImages() {
 		drawRowHeaderImage();
 		colHeader.drawColumnHeaderImage();
 		//drawContentImage();
 		redrawImages = false;
 	}
 	
 
 	/**
 	 * Return the indices of all selected rows; this returns null if 
 	 * @return
 	 */
 	public int[] getSelectedRows() {
 		if (selectionMode==Selection.ROWS) {
 			int min = Math.min(dragStart, dragEnd);
 			int max = Math.max(dragStart, dragEnd);
 			if (max>min) {
 				selection.clear();
 				selection.set(min, max);
 			}
 			
 			int[] rows = new int[selection.cardinality()];
 			int i = selection.nextSetBit(0);
 			int count = 0;
 			while (i>-1) {
 				rows[count] = i;
 				if (i>seqs.getSequenceCount()) {
 					//throw new IllegalStateException("A row was selected whose index is greater than the number of sequences...probably selection didn't get cleared after a mode switch");
 					System.err.println("A row was selected whose index is greater than the number of sequences...probably selection didn't get cleared after a mode switch");
 					break;
 				}
 				count++;
 				i = selection.nextSetBit(i+1);
 			}
 			return rows;
 		}
 		return null;
 	}
 	
 	/**
 	 * Returns true if there are any selected rows. 
 	 * @return
 	 */
 	public boolean hasSelectedRows() {
 		if (selectionMode==Selection.ROWS) {
 			if (dragStart!=dragEnd)
 				return true;
 			if (selection.cardinality()>0)
 				return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Returns true if there are any selected columns
 	 * @return
 	 */
 	public boolean hasSelectedColumns() {
 		if (selectionMode==Selection.COLUMNS) {
 			if (dragStart!=dragEnd)
 				return true;
 			if (selection.cardinality()>0)
 				return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Return the indices of all selected rows. These are zero indexed, such that if the first column in an alignment is 
 	 * selected, the first element in the returned array will be 0.  
 	 * @return
 	 */
 	public int[] getSelectedColumns() {
 		if (selectionMode==Selection.COLUMNS) {
 			int min = Math.min(dragStart, dragEnd);
 			int max = Math.max(dragStart, dragEnd);
 			if (max>min) {
 				selection.clear();
 				selection.set(min, max);
 			}
 			int[] cols = new int[selection.cardinality()];
 			int i = selection.nextSetBit(0);
 			int count = 0;
 			while (i>-1) {
 				cols[count] = i;
 				if (i>seqs.getSequenceLength()) {
 					throw new IllegalStateException("A column was selected whose index is greater than the max seq length...probably selection didn't get cleared after a mode switch");
 				}
 				i = selection.nextSetBit(i+1);
 				count++;
 			}
 			return cols;
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the selected rows or columns as a new sequence group. Sequence name is always preserved.
 	 * If rows are selected, then sequence annotations are also preserved. Seq. change listeners are
 	 * never preserved. If there is no selection an empty sg is returned.  
 	 * @return A new sequence group built from the selected rows or columns.
 	 */
 //	public Alignment getSelectionAsSG() {
 //		Alignment sg = new Alignment();
 //		if (hasSelectedRows()) {
 //			int[] rows = getSelectedRows();
 //			for(int i=0; i<rows.length; i++) {
 //				Sequence seq = seqs.get(rows[i]).clone();
 //				seq.removeSequenceChangeListeners();
 //				sg.add(seq);
 //			}
 //		}
 //		
 //		if (hasSelectedColumns()) {
 //			int[] cols = getSelectedColumns();
 //			for(int i=0; i<seqs.size(); i++) {
 //				StringBuilder strb = new StringBuilder();
 //				for(int j=0; j<cols.length; j++) {
 //					strb.append( seqs.get(i).at(cols[j]));
 //				}
 //				sg.add(new StringSequence(strb.toString(), seqs.get(i).getName()));
 //			}
 //		}
 //		
 //		//System.out.println("Returning a new sg with " + sg.size() + " sequences and max length: " + sg.getSequenceLength());
 //		return sg;
 //	}
 	
 	protected void exportSelectionActionPerformed(ActionEvent evt) {
 		
 	}
 	
 	/**
 	 * Set the zero column and fire a zeroColumnChanged event to all zeroColumnListeners. This also 
 	 * forces a redraw of the columnHeaderImage and repaints the column header component
 	 * @param col
 	 */
 	public void setZeroColumn(int col) {
 			zeroColumn = col;
 			for(ZeroColumnListener z : zeroColListeners)
 				z.zeroColumnChanged(zeroColumn);
 			
 			colHeader.drawColumnHeaderImage();
 			colHeader.repaint();
 	}
 	
 	/**
 	 * Add the given zeroColumnListener to the list of zeroColumnListeners that are notified when the 
 	 * zero column changes. This checks for redundancies and won't add if the item is already in the list. 
 	 * @param zl
 	 */
 	public void addZeroColumnListener(ZeroColumnListener zl) {
 		if (!zeroColListeners.contains(zl))
 			zeroColListeners.add(zl);
 	}
 	
 	/**
 	 * Remove the given zeroColumnListener from the list of objects notified when the zero col. changes
 	 * @param zl
 	 * @return
 	 */
 	public boolean removeZeroColumnListener(ZeroColumnListener zl) {
 		return zeroColListeners.remove(zl);
 	}
 	
 	
 	public void removeSelection() {
 		if (hasSelectedRows()) {
 			int[] rows = getSelectedRows();
 			seqs.removeRows( rows );
 		}
 		
 		if (hasSelectedColumns()) {
 			int[] cols = getSelectedColumns();
 			seqs.removeCols( cols );
 		}
 		
 		selection.clear();
 		dragStart = -1;
 		dragEnd = -1;
 		dragOffEdge = false;
 		setAlignment(seqs);
 		repaint();
 	}
 	
 
 	/**
 	 * This is called only when the user has begun to drag the mouse and has then dragged it out of
 	 * the bounds of this component. When this happens we begin a Timer, dragOffEndTimer, that
 	 * periodically calls this method until the user has released the mouse or moved it back in
 	 * bounds. This examines the mouse position to find out which we we should be scrolling, then
 	 * updates the selection rectangle and moves the viewport appropriately
 	 */
 	protected void updateViewportLocation() {
 		
 		Point mousePos = MouseInfo.getPointerInfo().getLocation();
 		int rightEdgeOfDisplay = scrollPane.getLocationOnScreen().x + viewport.getWidth();
 		int leftEdgeOfDisplay = scrollPane.getLocationOnScreen().x;
 		int topEdgeOfDisplay = scrollPane.getLocationOnScreen().y;
 		int bottomEdgeOfDisplay = scrollPane.getLocationOnScreen().y + viewport.getHeight();
 		
 		//System.out.println("Top edge of display : " + topEdgeOfDisplay + " mousepos: " + mousePos.y);
 		
 		Rectangle newRect = new Rectangle(viewport.getVisibleRect().x, viewport.getVisibleRect().y, viewport.getVisibleRect().width, viewport.getVisibleRect().height);
 		if (mousePos.y < topEdgeOfDisplay) { //Scroll up
 			int increment = -1* scrollPane.getVerticalScrollBar().getUnitIncrement();
 			newRect.y += increment;
 			
 			if (selectionMode == Selection.ROWS) {
 				dragEnd = getRowForPoint( new Point(0, scrollPane.getVerticalScrollBar().getValue()) );
 				dragEnd = Math.max(dragEnd, 0);
 				setSelectionInterval(dragStart, dragEnd);
 			}
 		}
 		
 		if (mousePos.y > bottomEdgeOfDisplay) { //Scroll down
 			int increment = scrollPane.getVerticalScrollBar().getUnitIncrement();
 			newRect.y += increment;
 			
 			if (selectionMode == Selection.ROWS) {
 				dragEnd = getRowForPoint( new Point(0, scrollPane.getVerticalScrollBar().getValue()+viewport.getHeight()) );
 				dragEnd = Math.min(seqs.getSequenceCount(), dragEnd);
 				setSelectionInterval(dragStart, dragEnd);
 			}
 		}
 		
 		if (mousePos.x < leftEdgeOfDisplay || mousePos.x > rightEdgeOfDisplay) {
 			//Slide viewport horizontally
 			int dif = 0;
 			if (mousePos.x < leftEdgeOfDisplay)
 				dif = leftEdgeOfDisplay - mousePos.x;
 			if (mousePos.x > rightEdgeOfDisplay)
 				dif = rightEdgeOfDisplay - mousePos.x;
 
 			//if dif is positive, we scroll left
 			int increment = scrollPane.getHorizontalScrollBar().getUnitIncrement();
 			if (dif > 0) { //User has dragged off of left edge, so the increment is negative
 				increment *= -1;
 				if (selectionMode == Selection.COLUMNS)
 					dragEnd = getColumnForPoint( new Point(scrollPane.getHorizontalScrollBar().getValue(), 0) );
 			}
 			else {
 				if (selectionMode == Selection.COLUMNS)
 					dragEnd = getColumnForPoint( new Point(scrollPane.getHorizontalScrollBar().getValue()+viewport.getWidth(), 0) );
 			}
 
 			if (selectionMode == Selection.COLUMNS) {
 				dragEnd = Math.min(dragEnd, seqs.getSequenceLength()); //Make sure we dont selects columns outside of the appropriate range
 				dragEnd = Math.max(0, dragEnd);
 				setSelectionInterval(dragStart, dragEnd);
 			}
 			
 			newRect.x += 2*increment;
 			if (dif > 40) //Go faster if the user drags the mouse far away from the edge
 				newRect.x += 2*increment;
 			if (dif > 100)
 				newRect.x += 2*increment;
 		}
 		
 		
 		viewport.scrollRectToVisible(newRect);	
 		
 	}
 
 	/**
 	 * Cause the given colums to be selected 
 	 * @param ranges
 	 * @param posOne
 	 * @param posTwo
 	 * @param posThree
 	 */
 	public void selectColumns(ArrayList<ColumnSelectionFrame.IntegerRange> ranges, boolean posOne,
 			boolean posTwo, boolean posThree) {
 		
 		clearSelection(); //Using this clear selection clears it in both the table and the row header
 		int sum = 0;
 		for(ColumnSelectionFrame.IntegerRange range : ranges) {
 			if (posOne && posTwo && posThree) {
 				addColumnSelectionInterval(Math.max(0, range.start), Math.min(getColumnCount()-1, range.end));	
 			}
 			else {
 				if (posOne) {
 					for(int i=Math.max(0, range.start);  i<Math.min(getColumnCount(), range.end); i+=3) {
 						addColumnSelectionInterval(i, i);
 						sum++;
 					}
 				}
 				if (posTwo) {
 					for(int i=Math.max(0, range.start)+1;  i<Math.min(getColumnCount(), range.end); i+=3) {
 						addColumnSelectionInterval(i, i);
 						sum++;
 					}
 				}
 				if (posThree) {
 					for(int i=Math.max(0, range.start)+2;  i<Math.min(getColumnCount(), range.end); i+=3) {
 						addColumnSelectionInterval(i, i);
 						sum++;
 					}
 				}
 			}
 			
 		}
 		
 	}
 
 	
 	/**
 	 * A class to handle painting the row header, it needs to be a JComponent so it can 
 	 * be set as the rowHeaderView in the parent scrollpane. It also listens for some mouse
 	 * events so it can handle selection. It's interior since it's (well, sort of) small and
 	 * directly handles/manipulates so many fields of the SGContentPanel
 	 * @author brendan
 	 *
 	 */
 	class RowHeader extends JPanel {
 		boolean dragging = false;
 		JComponent parent;
 		boolean mouseOverRightEdge = false;
 		JPopupMenu rowHeaderPopup;
 		int editingRow = -1;	//Keeps track of which row we're editing
 		
 		public RowHeader(JComponent parent) {
 			this.parent = parent;
 			setLayout(null);
 			seqRenamer = new JTextField();
 			seqRenamer.setHorizontalAlignment(JTextField.RIGHT);
 			this.add(seqRenamer);
 			seqRenamer.addActionListener(new ActionListener() {
 
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					seqs.getSequence(editingRow).setLabel(seqRenamer.getText());
 					drawRowHeaderImage();
 					repaint();
 					seqRenamer.setVisible(false);
 					editingRow = -1;
 				}
 			});
 			
 			rowHeaderPopup = new JPopupMenu();
 			rowHeaderPopup.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY) );
 			rowHeaderPopup.setBackground(new Color(100,100,100) );
 			
 //			JMenuItem popupCopy = new JMenuItem("Copy");
 //			popupCopy.addActionListener(new java.awt.event.ActionListener() {
 //	            public void actionPerformed(java.awt.event.ActionEvent evt) {
 //	            	sgDisplay.copy();
 //	            }
 //	        });
 //			rowHeaderPopup.add(popupCopy);
 //			
 //			JMenuItem popupCut = new JMenuItem("Cut");
 //			popupCut.addActionListener(new java.awt.event.ActionListener() {
 //	            public void actionPerformed(java.awt.event.ActionEvent evt) {
 //	            	sgDisplay.cut();
 //	            }
 //	        });
 //			rowHeaderPopup.add(popupCut);
 //			
 //			JMenuItem popupPaste = new JMenuItem("Paste");
 //			popupPaste.addActionListener(new java.awt.event.ActionListener() {
 //	            public void actionPerformed(java.awt.event.ActionEvent evt) {
 //	            	sgDisplay.paste();
 //	            }
 //	        });
 //			rowHeaderPopup.add(popupPaste);
 			
 			JMenuItem popupDisplay = new JMenuItem("New from selection");
 			popupDisplay.addActionListener(new java.awt.event.ActionListener() {
 	            public void actionPerformed(java.awt.event.ActionEvent evt) {
 	            	exportSelectionActionPerformed(evt);
 	            }
 	        });
 			rowHeaderPopup.add(popupDisplay);
 			
 			JMenuItem popupRemove = new JMenuItem("Delete selection");
 			popupRemove.addActionListener(new java.awt.event.ActionListener() {
 	            public void actionPerformed(java.awt.event.ActionEvent evt) {
 	            	removeSelection();
 	            }
 	        });
 			rowHeaderPopup.add(popupRemove);
 			
 
 			
 			addMouseMotionListener(new MouseMotionListener() {
 				
 				@Override
 				public void mouseDragged(MouseEvent e) {
 					if (mouseOverRightEdge) {
 						setRowHeaderWidth(e.getX());
 						setToNaturalSize();
 					}
 					else {
 						selectionMode = Selection.ROWS;
 						
 						if (!dragging) 
 							dragStart = getRowForPoint(e.getPoint());
 						dragEnd = getRowForPoint(e.getPoint());
 						dragEnd = Math.min(dragEnd, seqs.getSequenceLength());
 						setSelectionInterval(dragStart, dragEnd);
 						repaintParent();
 						dragging = true;
 					}
 					
 					if (e.getY() > ( (viewport.getViewRect().y+viewport.getViewRect().height)-20)) {
 						int curPos = scrollPane.getVerticalScrollBar().getValue();
 						int increment = scrollPane.getVerticalScrollBar().getUnitIncrement();
 						if (curPos < scrollPane.getVerticalScrollBar().getMaximum()) {
 							Rectangle newRect = new Rectangle(viewport.getVisibleRect().x, viewport.getVisibleRect().y, viewport.getVisibleRect().width, viewport.getVisibleRect().height);
 							newRect.y += increment;
 							viewport.scrollRectToVisible(newRect);
 						}
 					}
 					if (e.getY() < (viewport.getViewRect().y+20)) {
 						int curPos = scrollPane.getVerticalScrollBar().getValue();
 						int increment = scrollPane.getVerticalScrollBar().getUnitIncrement();
 						if (curPos > 0) {
 							Rectangle newRect = new Rectangle(viewport.getVisibleRect().x, viewport.getVisibleRect().y, viewport.getVisibleRect().width, viewport.getVisibleRect().height);
 							newRect.y -= increment;
 							viewport.scrollRectToVisible(newRect);
 						}
 					}
 					repaint();
 				}
 
 				public void mouseMoved(MouseEvent arg0) {
 					Point mousePos = arg0.getPoint();
 
 					if (mousePos.x>(getWidth()-10) && !mouseOverRightEdge) {
 						mouseOverRightEdge = true;
 						setCursor(edgeAdjustCursor);
 					}
 					
 					if (mousePos.x<(getWidth()-10) && mouseOverRightEdge) {
 						mouseOverRightEdge = false;
 						setCursor(Cursor.getDefaultCursor());
 					}
 				}
 			});
 			
 			addMouseListener(new MouseListener() {
 
 				@Override
 				public void mouseReleased(MouseEvent e) {
 					dragging = false;
 					dragOffEdge = false;
 					dragOffEdgeTimer.stop();
 				}
 				
 				@Override
 				public void mouseClicked(MouseEvent e) {
 					if (e.getClickCount()>1) {
 						int row = getRowForPoint(e.getPoint());
 						if (row<0 || row>=seqs.getSequenceLength()) {
 							return;
 						}
 						editingRow = row;
 						Rectangle taBounds = new Rectangle(15, row*rowHeight, rowHeaderWidth-15, rowHeight+4);
 						seqRenamer.setBounds(taBounds);
 			
 						seqRenamer.setText(seqs.getSequence(row).getLabel());
 						seqRenamer.setOpaque(true);
 						seqRenamer.setVisible(true);
 
 						return;
 					}
 					if (e.isPopupTrigger() || (UIConstants.isMac() && e.isControlDown()) || (e.getButton()==MouseEvent.BUTTON3)) {
 						rowHeaderPopup.show(rowHeader, e.getX(), e.getY());
 					}
 					else {
 						if (editingRow > -1) {
 							seqRenamer.setVisible(false);
 							editingRow = -1;
 						}
 						dragStart = -1;
 						dragEnd = -1;
 						selection.clear();
 						repaintParent();
 					}
 				}
 
 				@Override
 				public void mousePressed(MouseEvent e) {}
 
 				public void mouseEntered(MouseEvent arg0) {	
 					if (selectionMode == Selection.ROWS && dragging) {
 						dragOffEdgeTimer.stop();
 						dragOffEdge = false;
 					}
 				}
 
 				public void mouseExited(MouseEvent arg0) {	
 					if (arg0.getX()<(getWidth()-10) && mouseOverRightEdge) {
 						mouseOverRightEdge = false;
 						setCursor(Cursor.getDefaultCursor());
 					}
 					if (selectionMode == Selection.ROWS && dragging) {
 						dragOffEdgeTimer.start();
 						dragOffEdge = true;
 						System.out.println("Mouse exited row header, starting drag off edge timer...");
 					}
 				}
 
 
 
 			});
 		}
 
 		protected void repaintParent() {
 			repaint();
 			parent.repaint();
 		}
 
 		public void paintComponent(Graphics g) {
 			g.setColor(Color.white);
 			g.fillRect(0, 0, getWidth(), getHeight());
 			g.drawImage(rowHeaderImage, 0, 0, null);
 			
 			Graphics2D g2d = (Graphics2D)g;
 			
 			//System.out.println("Painting row header, width is : " +rowHeaderWidth);
 			
 			if ( (selectionMode == Selection.ROWS) ) {
 				int min = Math.min(dragStart, dragEnd);
 				int max = Math.max(dragStart, dragEnd);
 
 				if (Math.max(dragStart, dragEnd)>-1) {
 					int minY = min*rowHeight;
 					int maxY = max*rowHeight;
 					g2d.setColor(selectionColor);
 					g2d.fillRect(0, minY, getWidth(), maxY-minY);
 					g2d.setColor(Color.DARK_GRAY);
 					g2d.drawLine(0, minY, getWidth(), minY);
 					g2d.drawLine(0, maxY, getWidth(), maxY);
 					g2d.setColor(new Color(250, 250, 250, 150));
 					g2d.drawLine(0, minY-1, getWidth(), minY-1);
 					g2d.drawLine(0, maxY+1, getWidth(), maxY+1);
 
 				}
 				int i = selection.nextSetBit(0);
 				g2d.setColor(selectionColor);
 				while (i>-1) {
 					if (i<min || i>=max)
 						g2d.fillRect(0, i*rowHeight, getWidth(), rowHeight);	
 					i = selection.nextSetBit(i+1);
 				}
 			}
 
 		}
 		
 	}
 
 	/**
 	 * Set the selection mode to columns and the dragStart and dragEnd variables to the supplied values. This is a 
 	 * more efficient way to select a big region, since it avoids repainting each individual column 
 	 * @param start
 	 * @param end
 	 */
 	public void setColumnDragInterval(int start, int end) {
 		selectionMode = Selection.COLUMNS;
 		setSelectionInterval(start, end);
 	}
 
 	/**
 	 * Set the 'letter mode' of the current row painter - this affects how (and whether) individual
 	 * bases / amino acids are drawn (but the coloring scheme)
 	 * @param selectedIndex
 	 */
 	public void setLetterMode(int mode) {
 		rowPainter.setLetterMode(mode);
 	}
 	
 	
 	/**
 	 * A mouse listening class for the main content panel area
 	 * @author brendan
 	 *
 	 */
 	class PanelMouseListener implements MouseListener, MouseMotionListener {
 
 		boolean dragging = false;
 		
 		@Override
 		public void mouseClicked(MouseEvent e) {
 			if (e.isPopupTrigger() || (UIConstants.isMac() && e.isControlDown()) || (e.getButton()==MouseEvent.BUTTON3)) {
 				return;
 			}
 			dragStart = -1;
 			dragEnd = -1;
 			dragOffEdge = false;
 			clearSelection();
 			colHeader.repaint();
 			rowHeader.repaint();
 			repaint();
 		}
 
 		@Override
 		public void mouseEntered(MouseEvent e) {
 			//If we're dragging and we drag off the edge and then back into the component we
 			//must stop the timer from firing events to update the viewport. We're not scrolling
 			//anymore
 			if (dragging) {
 				dragOffEdge = false;
 				dragOffEdgeTimer.stop();
 			}
 		}
 
 		@Override
 		public void mouseExited(MouseEvent e) {
 			//If we're dragging and we drag off the edge, start a timer to periodically 
 			//make sure we move the the viewport to the right position
 			if (dragging) { 
 				dragOffEdge = true;
 				dragOffEdgeTimer.start();
 			}
 		}
 
 		@Override
 		public void mousePressed(MouseEvent e) { }
 
 		@Override
 		public void mouseReleased(MouseEvent e) { 
 			dragging = false;
 			dragOffEdge = false;
 			dragOffEdgeTimer.stop();
 			repaint();
 		}
 
 		@Override
 		public void mouseDragged(MouseEvent e) {
 			//System.out.println("Mouse has been dragged to position: " + e.getX() + ", " + e.getY());
 			if (selectionMode != Selection.COLUMNS) {
 				selection.clear();
 				rowHeader.repaint();
 			}
 			selectionMode = Selection.COLUMNS;
 
 			if (!dragging)
 				dragStart = getColumnForPoint(e.getPoint());
 			
 			dragEnd = getColumnForPoint(e.getPoint());
 			dragEnd = Math.min(dragEnd, seqs.getSequenceLength()); //Make sure we dont selects columns outside of the appropriate range
 			dragEnd = Math.max(0, dragEnd);
 			setSelectionInterval(dragStart, dragEnd);
 			colHeader.repaint(); //So we can draw the fancy selection indicator
 
 			dragging = true;
 			
 			//System.out.println("Drag interval: " + Math.min(dragStart, dragEnd) + " .. " + Math.max(dragStart, dragEnd));
 			
 			//Handle viewport moving to drag location
 			if (viewport != null) {
 				if (e.getX() < (viewport.getViewRect().x+20)) {
 					//System.out.println("Near left edge of view rect, mousepos : " + e.getX() + " rect x: " + viewport.getViewRect().x);
 					int curPos = scrollPane.getHorizontalScrollBar().getValue();
 					int increment = scrollPane.getHorizontalScrollBar().getUnitIncrement();
 					if (curPos > 0) {
 //						scrollPane.getHorizontalScrollBar().setValue( Math.max(0, curPos-increment));
 						Rectangle newRect = new Rectangle(viewport.getVisibleRect().x, viewport.getVisibleRect().y, viewport.getVisibleRect().width, viewport.getVisibleRect().height);
 						newRect.x -= increment;
 						viewport.scrollRectToVisible(newRect);
 					}
 				}
 				
 				if (e.getX() > ( (viewport.getViewRect().x+viewport.getViewRect().width)-20)) {
 					int curPos = scrollPane.getHorizontalScrollBar().getValue();
 					int increment = scrollPane.getHorizontalScrollBar().getUnitIncrement();
 					if (curPos < scrollPane.getHorizontalScrollBar().getMaximum()) {
 						Rectangle newRect = new Rectangle(viewport.getVisibleRect().x, viewport.getVisibleRect().y, viewport.getVisibleRect().width, viewport.getVisibleRect().height);
 						newRect.x += increment;
 						viewport.scrollRectToVisible(newRect);
 					}
 				}
 				if (e.getY() > ( (viewport.getViewRect().y+viewport.getViewRect().height)-20)) {
 					int curPos = scrollPane.getVerticalScrollBar().getValue();
 					int increment = scrollPane.getVerticalScrollBar().getUnitIncrement();
 					if (curPos < scrollPane.getVerticalScrollBar().getMaximum()) {
 						Rectangle newRect = new Rectangle(viewport.getVisibleRect().x, viewport.getVisibleRect().y, viewport.getVisibleRect().width, viewport.getVisibleRect().height);
 						newRect.y += increment;
 						viewport.scrollRectToVisible(newRect);
 					}
 				}
 				if (e.getY() < (viewport.getViewRect().y+20)) {
 					int curPos = scrollPane.getVerticalScrollBar().getValue();
 					int increment = scrollPane.getVerticalScrollBar().getUnitIncrement();
 					if (curPos > 0) {
 						Rectangle newRect = new Rectangle(viewport.getVisibleRect().x, viewport.getVisibleRect().y, viewport.getVisibleRect().width, viewport.getVisibleRect().height);
 						newRect.y -= increment;
 						viewport.scrollRectToVisible(newRect);
 					}
 				}
 				
 			}
 			repaint();
 		}
 
 		@Override
 		public void mouseMoved(MouseEvent e) {	}
 		
 	}
 
 	/**
 	 * Small class to listen for the popup menu trigger and show it
 	 * @author brendan
 	 *
 	 */
 	private class PopupListener extends MouseAdapter {
 	    public void mousePressed(MouseEvent e) {
 	        maybeShowPopup(e);
 	    }
 
 	    public void mouseReleased(MouseEvent e) {
 	        maybeShowPopup(e);
 	    }
 
 	    private void maybeShowPopup(MouseEvent e) {
 	        if (e.isPopupTrigger()) {
 	        	
 	            popup.show(e.getComponent(),
 	                       e.getX(), e.getY());
 	        }
 	    }
 	}
 
 	
 	private JPopupMenu popup;
 }
