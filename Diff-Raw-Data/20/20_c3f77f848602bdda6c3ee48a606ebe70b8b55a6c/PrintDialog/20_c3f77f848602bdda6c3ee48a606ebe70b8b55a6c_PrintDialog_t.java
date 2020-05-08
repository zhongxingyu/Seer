 package de.unisiegen.gtitool.ui.logic;
 
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.print.PageFormat;
 import java.awt.print.Paper;
 import java.awt.print.Printable;
 import java.awt.print.PrinterException;
 import java.awt.print.PrinterJob;
 import java.util.HashMap;
 
 import javax.print.PrintService;
 import javax.print.PrintServiceLookup;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableColumnModel;
 import javax.swing.table.TableModel;
 
 import de.unisiegen.gtitool.core.parser.style.PrettyStringComponent;
 import de.unisiegen.gtitool.logger.Logger;
import de.unisiegen.gtitool.ui.i18n.Messages;
 import de.unisiegen.gtitool.ui.jgraph.JGTIGraph;
 import de.unisiegen.gtitool.ui.logic.interfaces.LogicClass;
 import de.unisiegen.gtitool.ui.netbeans.AboutDialogForm;
 import de.unisiegen.gtitool.ui.netbeans.PrintDialogForm;
 import de.unisiegen.gtitool.ui.swing.JGTITable;
 
 
 /**
  * The {@link PrintDialog}.
  * 
  * @author Christian Fehler
  * @version $Id$
  */
 public final class PrintDialog implements LogicClass < PrintDialogForm >,
     Printable
 {
 
   /**
    * The {@link PrintService} list cell renderer.
    * 
    * @author Benjamin Mies
    */
   public class PrintServiceListCellRenderer extends DefaultListCellRenderer
   {
 
     /**
      * The serial version uid.
      */
     private static final long serialVersionUID = 4446455799980336206L;
 
 
     /**
      * Allocates a new {@link PrintServiceListCellRenderer}.
      */
     public PrintServiceListCellRenderer ()
     {
       super ();
     }
 
 
     /**
      * {@inheritDoc}
      * 
      * @see DefaultListCellRenderer#getListCellRendererComponent(JList, Object,
      *      int, boolean, boolean)
      */
     @Override
     public Component getListCellRendererComponent ( JList list, Object value,
         int index, boolean isSelected, boolean cellHasFocus )
     {
       super.getListCellRendererComponent ( list, value, index, isSelected,
           cellHasFocus );
       setText ( ( ( PrintService ) value ).getName () );
       return this;
     }
   }
 
 
   /**
    * The {@link Logger} for this class.
    */
   private static final Logger logger = Logger.getLogger ( PrintDialog.class );
 
 
   /**
    * The {@link AboutDialogForm}.
    */
   private PrintDialogForm gui;
 
 
   /**
    * The parent {@link JFrame}.
    */
   private JFrame parent;
 
 
   /**
    * The {@link Printable} to print.
    */
   private Printable printable;
 
 
   /**
    * The {@link JGTITable}.
    */
   private JGTITable table = null;
 
 
   /**
    * The {@link TableColumnModel}.
    */
   private TableColumnModel tableColumnModel = null;
 
 
   /**
    * The {@link TableModel}.
    */
   private TableModel tableModel = null;
 
 
   /**
    * The {@link MachinePanel}.
    */
   private MachinePanel machinePanel;
 
 
   /**
    * Allocates a new {@link PrintDialog}.
    * 
    * @param parent The parent {@link JFrame}.
    * @param machinePanel The {@link MachinePanel}.
    */
   public PrintDialog ( JFrame parent, MachinePanel machinePanel )
   {
     logger.debug ( "AboutDialog", "allocate a new about dialog" ); //$NON-NLS-1$ //$NON-NLS-2$
     this.parent = parent;
     this.gui = new PrintDialogForm ( this, parent );
     this.machinePanel = machinePanel;
     this.printable = this.machinePanel.getJGTIGraph ();
 
     this.tableModel = machinePanel.getPDATableModel ();
     this.tableColumnModel = machinePanel.getPdaTableColumnModel ();
     this.table = new JGTITable ();
     this.table.setModel ( this.tableModel );
     this.table.setColumnModel ( this.tableColumnModel );
 
     initialize ();
   }
 
 
   /**
    * Allocates a new {@link PrintDialog}.
    * 
    * @param parent The parent {@link JFrame}.
    * @param tableModel The {@link TableModel} to print.
    * @param tableColumnModel The {@link TableColumnModel}.
    */
   public PrintDialog ( JFrame parent, TableModel tableModel,
       TableColumnModel tableColumnModel )
   {
     this.parent = parent;
 
     this.tableModel = tableModel;
     this.tableColumnModel = tableColumnModel;
     this.table = new JGTITable ();
     this.table.setModel ( this.tableModel );
     this.table.setColumnModel ( this.tableColumnModel );
 
     initialize ();
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see LogicClass#getGUI()
    */
   public final PrintDialogForm getGUI ()
   {
     return this.gui;
   }
 
 
   /**
    * Closes the {@link AboutDialogForm}.
    */
   public final void handleClose ()
   {
     logger.debug ( "handleClose", "handle close" ); //$NON-NLS-1$ //$NON-NLS-2$
     this.gui.dispose ();
   }
 
 
   /**
    * Shows the {@link AboutDialogForm}.
    */
   public final void show ()
   {
     logger.debug ( "show", "show the about dialog" ); //$NON-NLS-1$ //$NON-NLS-2$
     this.gui.pack ();
     this.gui.setLocationRelativeTo ( this.parent );
    if ( this.gui.jGTIComboBoxPrinter.getModel ().getSize () > 0 )
    {
      this.gui.setVisible ( true );
    }
    else
    {
      InfoDialog dialog = new InfoDialog ( this.parent, Messages
          .getString ( "PrintDialog.ErrorPrinterMessage" ), Messages //$NON-NLS-1$
          .getString ( "PrintDialog.ErrorPrinter" ) ); //$NON-NLS-1$
      dialog.show ();
    }
   }
 
 
   /**
    * Handle print action performed.
    */
   public void handlePrint ()
   {
     this.gui.setVisible ( false );
 
     if ( this.table != null && this.tableColumnModel != null
         && this.tableModel != null )
     {
       try
       {
         printTableModel ();
       }
       catch ( Exception exc )
       {
         exc.printStackTrace ();
         System.exit ( 1 );
       }
     }
     else
     {
       printJGraph ();
     }
   }
 
 
   /**
    * Print the {@link JGTIGraph}.
    */
   private void printJGraph ()
   {
     PrinterJob job = PrinterJob.getPrinterJob ();
     try
     {
 
       PageFormat pageFormat = new PageFormat ();
       Paper paper = new Paper ();
       paper.setSize ( 8.27 * 72, 11.69 * 72 );
       paper.setImageableArea ( 0, 0, paper.getWidth (), paper.getHeight () );
       pageFormat.setPaper ( paper );
       pageFormat.setOrientation ( PageFormat.PORTRAIT );
 
       job.setPrintService ( ( PrintService ) this.gui.jGTIComboBoxPrinter
           .getSelectedItem () );
       job.setPrintable ( this.printable, pageFormat );
       logger.debug ( "handlePrint", "printing" ); //$NON-NLS-1$ //$NON-NLS-2$
       job.print ();
       logger.debug ( "handlePrint", "printed" ); //$NON-NLS-1$ //$NON-NLS-2$
     }
     catch ( Exception exc )
     {
       exc.printStackTrace ();
       System.exit ( 1 );
     }
   }
 
 
   /**
    * Handle cancel action performed.
    */
   public void handleCancel ()
   {
     this.gui.dispose ();
   }
 
 
   /**
    * Prints the {@link TableModel}.
    * 
    * @throws Exception If the something with the print dialog fails.
    */
   private final void printTableModel () throws Exception
   {
     PrinterJob job = PrinterJob.getPrinterJob ();
 
     PageFormat pageFormat = new PageFormat ();
     Paper paper = new Paper ();
     paper.setSize ( 8.27 * 72, 11.69 * 72 );
     paper.setImageableArea ( 0, 0, paper.getWidth (), paper.getHeight () );
     pageFormat.setPaper ( paper );
     // pageFormat.setOrientation(this.printDialog.getPageFormat());
     pageFormat.setOrientation ( PageFormat.PORTRAIT );
     this.pageWidth = ( int ) pageFormat.getWidth ();
     this.pageHeight = ( int ) pageFormat.getHeight ();
 
     // // Copies
     // job.setCopies(((Number)
     // this.printDialog.jSpinnerCopies.getValue()).intValue());
     //
     // // Margin
     // this.marginLeft = (int) (2.8346456693 * ((Number)
     // this.printDialog.jSpinnerMarginLeft.getValue()).intValue());
     // this.marginRight = (int) (2.8346456693 * ((Number)
     // this.printDialog.jSpinnerMarginRight.getValue()).intValue());
     // this.marginTop = (int) (2.8346456693 * ((Number)
     // this.printDialog.jSpinnerMarginTop.getValue()).intValue());
     // this.marginBottom = (int) (2.8346456693 * ((Number)
     // this.printDialog.jSpinnerMarginBottom.getValue()).intValue());
 
     // Calculate the page count
     this.pageCount = getPageCount ();
 
     job.setPrintable ( this, pageFormat );
     job.setPrintService ( ( PrintService ) this.gui.jGTIComboBoxPrinter
         .getSelectedItem () );
 
     logger.debug ( "handlePrint", "printing" ); //$NON-NLS-1$ //$NON-NLS-2$
     job.print ();
     logger.debug ( "handlePrint", "printed" ); //$NON-NLS-1$ //$NON-NLS-2$ 
   }
 
 
   /**
    * Returns the page count.
    * 
    * @return The page count.
    */
   private final int getPageCount ()
   {
     int result = 1;
     int y = this.marginTop + HEADER_HEIGHT;
 
     for ( int i = 0 ; i < this.tableModel.getRowCount () ; i++ )
     {
       if ( isPageBreakNeeded ( y ) )
       {
         y = this.marginTop + HEADER_HEIGHT;
         result++ ;
       }
       y += ROW_HEIGHT;
     }
     return result;
   }
 
 
   /**
    * Initializes this {@link PrintDialog}.
    */
   private final void initialize ()
   {
     this.normalFont = new Font ( "Dialog", Font.PLAIN, FONT_SIZE ); //$NON-NLS-1$
     this.headerFont = this.normalFont.deriveFont ( Font.BOLD );
 
     // Printer
     PrintService [] printServices = PrintServiceLookup.lookupPrintServices (
     null, null );
 
     this.gui.jGTIComboBoxPrinter.setModel ( new DefaultComboBoxModel (
         printServices ) );
     this.gui.jGTIComboBoxPrinter
         .setRenderer ( new PrintServiceListCellRenderer () );
 
     // // Copies
     // this.printDialog.jSpinnerCopies.setModel(new SpinnerNumberModel(1, 1,
     // MAX_COPIES, 1));
     //
     // // Margin
     // this.printDialog.jSpinnerMarginLeft.setModel(new SpinnerNumberModel(20,
     // 10, 50, 1));
     // this.printDialog.jSpinnerMarginRight.setModel(new SpinnerNumberModel(20,
     // 10, 50, 1));
     // this.printDialog.jSpinnerMarginTop.setModel(new SpinnerNumberModel(20,
     // 10, 50, 1));
     // this.printDialog.jSpinnerMarginBottom.setModel(new SpinnerNumberModel(20,
     // 10, 50, 1));
 
   }
 
 
   /**
    * Returns true if a page break is needed.
    * 
    * @param y The y position.
    * @return True if a page break is needed.
    */
   private final boolean isPageBreakNeeded ( int y )
   {
     return ( y + ROW_HEIGHT + this.marginBottom ) > this.pageHeight;
   }
 
 
   /**
    * The page count.
    */
   private int pageCount;
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Printable#print(Graphics, PageFormat, int)
    */
   public int print ( Graphics g,
       @SuppressWarnings ( "unused" ) PageFormat pageFormat, int pageIndex )
       throws PrinterException
   {
     if ( ( pageIndex < 0 ) || ( pageIndex >= this.pageCount ) )
     {
       return NO_SUCH_PAGE;
     }
     try
     {
       int [] columnWidth = calculateColumnWidth ();
 
       drawHeader ( g, columnWidth );
 
       int y = this.marginTop + HEADER_HEIGHT;
 
       int start = 0;
       int end = this.tableModel.getRowCount ();
 
       // Get the printed rows
       Integer lastPage = new Integer ( pageIndex - 1 );
       start = this.printedRows.get ( lastPage ) == null ? 0 : this.printedRows
           .get ( lastPage ).intValue ();
 
       for ( int row = start ; row < end ; row++ )
       {
         if ( isPageBreakNeeded ( y ) )
         {
           drawBorder ( g, y );
 
           // Save the printed rows
           this.printedRows
               .put ( new Integer ( pageIndex ), new Integer ( row ) );
 
           return PAGE_EXISTS;
         }
 
         if ( ( row % 2 ) == 0 )
         {
           drawBackground ( g, y );
         }
 
         drawIconAndText ( g, y, row, columnWidth );
 
         y += ROW_HEIGHT;
       }
       drawBorder ( g, y );
     }
     catch ( Exception exc )
     {
       exc.printStackTrace ();
       throw new PrinterException ( exc.getMessage () );
     }
     return PAGE_EXISTS;
   }
 
 
   /**
    * The {@link HashMap} which contains the printed rows.
    */
   private HashMap < Integer, Integer > printedRows = new HashMap < Integer, Integer > ();
 
 
   /**
    * The page height.
    */
   private int pageHeight;
 
 
   /**
    * The page width.
    */
   private int pageWidth;
 
 
   /**
    * Returns the calculated column widths.
    * 
    * @return The calculated column widths.
    */
   private final int [] calculateColumnWidth ()
   {
     int printWidth = this.pageWidth - this.marginLeft - this.marginRight;
     int [] columnWidth = new int [ this.tableColumnModel.getColumnCount () ];
 
     for ( int i = 0 ; i < this.tableColumnModel.getColumnCount () ; i++ )
     {
       TableColumn tableColumn = this.tableColumnModel.getColumn ( i );
       if ( tableColumn.getMaxWidth () < Integer.MAX_VALUE )
       {
         columnWidth [ i ] = tableColumn.getMaxWidth ();
       }
       else
       {
         columnWidth [ i ] = 0;
       }
     }
 
     int sum = 0;
     for ( int element : columnWidth )
     {
       sum += element;
     }
 
     int variableColumns = 0;
     for ( int element : columnWidth )
     {
       if ( element == 0 )
       {
         variableColumns++ ;
       }
     }
 
     while ( ( sum + variableColumns * 50 ) > printWidth )
     {
       sum = 0;
       for ( int i = 0 ; i < columnWidth.length ; i++ )
       {
         columnWidth [ i ] = columnWidth [ i ] * 9 / 10;
         sum += columnWidth [ i ];
       }
     }
 
     int variableColumnsWidth = ( printWidth - sum ) / variableColumns;
     for ( int i = 0 ; i < columnWidth.length ; i++ )
     {
       if ( columnWidth [ i ] == 0 )
       {
         columnWidth [ i ] = variableColumnsWidth;
       }
     }
     return columnWidth;
   }
 
 
   /**
    * Draws the border.
    * 
    * @param g The {@link Graphics}.
    * @param y The y position.
    */
   private final void drawBackground ( Graphics g, int y )
   {
     g.setColor ( BACKGROUND );
     g.fillRect ( this.marginLeft, y, g.getClipBounds ().width - this.marginLeft
         - this.marginRight, ROW_HEIGHT );
     g.setColor ( Color.BLACK );
   }
 
 
   /**
    * Draws the border.
    * 
    * @param g The {@link Graphics}.
    * @param endY The y position of the table end.
    */
   private final void drawBorder ( Graphics g, int endY )
   {
     int x1 = this.marginLeft - BORDER_OFFSET;
     int x2 = g.getClipBounds ().width - this.marginRight + BORDER_OFFSET;
     int y1 = this.marginTop - BORDER_OFFSET;
     int y2 = this.marginTop - BORDER_OFFSET + HEADER_HEIGHT;
     int y3 = endY + BORDER_OFFSET;
 
     drawLine ( g, x1, y1, x2, y1, BORDER_WIDTH );
     drawLine ( g, x1, y3, x2, y3, BORDER_WIDTH );
     drawLine ( g, x1, y1, x1, y3, BORDER_WIDTH );
     drawLine ( g, x2, y1, x2, y3, BORDER_WIDTH );
     drawLine ( g, x1, y2, x2, y2, BORDER_WIDTH );
   }
 
 
   /**
    * Draws the border.
    * 
    * @param g The {@link Graphics}.
    * @param columnWidth The column widths.
    */
   private final void drawHeader ( Graphics g, int [] columnWidth )
   {
     g.setFont ( this.headerFont );
 
     int internOffset = 3;
     int x = this.marginLeft;
     int y = this.marginTop + ROW_HEIGHT - internOffset
         + ( ( HEADER_HEIGHT - ROW_HEIGHT ) / 2 );
     for ( int column = 0 ; column < this.tableColumnModel.getColumnCount () ; column++ )
     {
       TableColumn tableColumn = this.tableColumnModel.getColumn ( column );
 
       Object value = tableColumn.getHeaderValue ();
 
       String text = value == null ? "" : value.toString (); //$NON-NLS-1$
       int stringWidth = g.getFontMetrics ().stringWidth ( text );
 
       if ( HEADER_CENTERED )
       {
         g.drawString ( text, x + ( columnWidth [ column ] - stringWidth ) / 2,
             y );
       }
       else
       {
         g.drawString ( text, x, y );
       }
       x += columnWidth [ column ];
     }
   }
 
 
   /**
    * The left margin.
    */
   private int marginLeft;
 
 
   /**
    * The right margin.
    */
   private int marginRight;
 
 
   /**
    * The top margin.
    */
   private int marginTop;
 
 
   /**
    * The bottom margin.
    */
   private int marginBottom;
 
 
   /**
    * The normal {@link Font}.
    */
   private Font normalFont;
 
 
   /**
    * The header {@link Font}.
    */
   private Font headerFont;
 
 
   /**
    * Draws the border.
    * 
    * @param g The {@link Graphics}.
    * @param y The y position.
    * @param row The row.
    * @param columnWidth The column widths.
    */
   private final void drawIconAndText ( Graphics g, int y, int row,
       int [] columnWidth )
   {
     g.setFont ( this.normalFont );
 
     int internOffset = 2;
     int x = this.marginLeft;
     for ( int column = 0 ; column < this.tableColumnModel.getColumnCount () ; column++ )
     {
       TableColumn tableColumn = this.tableColumnModel.getColumn ( column );
 
       int modelColumn = tableColumn.getModelIndex ();
       Object value = this.tableModel.getValueAt ( row, modelColumn );
 
       TableCellRenderer renderer = tableColumn.getCellRenderer ();
       boolean performNormal = true;
 
       if ( renderer != null )
       {
         Component component = tableColumn.getCellRenderer ()
             .getTableCellRendererComponent ( this.table, value, false, false,
                 row, modelColumn );
         if ( component instanceof PrettyStringComponent )
         {
           PrettyStringComponent prettyStringComponent = ( PrettyStringComponent ) component;
 
           g.drawString ( prettyStringComponent.getPrettyString ().toString (),
               x, y + ROW_HEIGHT - internOffset );
           performNormal = false;
         }
       }
 
       if ( performNormal )
       {
         g
             .drawString (
                 value == null ? "" : value.toString (), x, y + ROW_HEIGHT - internOffset ); //$NON-NLS-1$
       }
       x += columnWidth [ column ];
     }
   }
 
 
   /**
    * The background {@link Color}.
    */
   private static final Color BACKGROUND = new Color ( 227, 227, 227 );
 
 
   /**
    * The border offset.
    */
   private static final int BORDER_OFFSET = 2;
 
 
   /**
    * The border width.
    */
   private static final int BORDER_WIDTH = 2;
 
 
   /**
    * The {@link Font} size.
    */
   private static final int FONT_SIZE = 7;
 
 
   /**
    * The header centered flag.
    */
   private static final boolean HEADER_CENTERED = false;
 
 
   /**
    * The row height.
    */
   private static final int ROW_HEIGHT = 10;
 
 
   /**
    * The header height.
    */
   private static final int HEADER_HEIGHT = ( int ) ( ROW_HEIGHT * 1.5 );
 
 
   // /**
   // * The maximum page offset.
   // */
   // private static final int MAX_PAGE_OFFSET = 30;
 
   /**
    * Draws a line.
    * 
    * @param g The {@link Graphics}.
    * @param x1 The x1.
    * @param y1 The y1.
    * @param x2 The x2.
    * @param y2 The y2.
    * @param lineWidth The line width.
    */
   private final void drawLine ( Graphics g, int x1, int y1, int x2, int y2,
       int lineWidth )
   {
     int xPoints[] = new int [ 4 ];
     int yPoints[] = new int [ 4 ];
 
     xPoints [ 0 ] = x1 + lineWidth - 1;
     yPoints [ 0 ] = y1 + lineWidth - 1;
     xPoints [ 1 ] = x1;
     yPoints [ 1 ] = y1;
     xPoints [ 2 ] = x2;
     yPoints [ 2 ] = y2;
     xPoints [ 3 ] = x2 + lineWidth - 1;
     yPoints [ 3 ] = y2 + lineWidth - 1;
 
     g.fillPolygon ( xPoints, yPoints, 4 );
   }
 }
