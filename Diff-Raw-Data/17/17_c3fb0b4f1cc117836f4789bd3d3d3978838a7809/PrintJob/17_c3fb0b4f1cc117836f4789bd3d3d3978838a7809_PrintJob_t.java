 package dennis.markmann.MyLibraries.DefaultJobs.Print;
 
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.awt.print.PageFormat;
 import java.awt.print.Printable;
 import java.awt.print.PrinterException;
 import java.awt.print.PrinterJob;
 import java.util.ArrayList;
 import java.util.List;
 
import javax.print.PrintServiceLookup;
 import javax.swing.JTextArea;
 import javax.swing.JWindow;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.JTextComponent;
 import javax.swing.text.Utilities;
 
 /**
  * Used to print out texts.
  * 
  * @author dennis.markmann
  * @since jdk1.7.0_21
  * @version 1.0
  */
 public class PrintJob implements Printable {
 
     private final PageFormat pageFormat;
     private final Font fontForPrint;
     private String textPassage;
     private final List<String> textPassages = new ArrayList<String>();
     private final JWindow windowForPrint = new JWindow();
     private final JTextArea textareaForPrint = new JTextArea();
     private final FontMetrics fontMetrics;
     private Dimension pageDim;
     private BufferedImage bufferedImage;
     private static final int CONS = 2;
     private int linesTotal = 0;
     private int linesMaxOnPage = 0;
     private int numberOfPages = 0;
     private final int[][] pageBorders = new int[999][2];
 
     private final PrinterJob printerJob = PrinterJob.getPrinterJob();
 
     public PrintJob(final String printText) {
 
        this.getPrintService();
         this.fontForPrint = new Font("Arial", Font.PLAIN, 16 * CONS);
         this.pageFormat = new PageFormat();
 
         this.pageDim = new Dimension(
                 ((int) this.pageFormat.getImageableWidth() - 10) * CONS,
                 ((int) this.pageFormat.getImageableHeight()) * CONS);
 
         this.textareaForPrint.setFont(this.fontForPrint);
         this.fontMetrics = this.textareaForPrint.getFontMetrics(this.fontForPrint);
         this.textareaForPrint.setLineWrap(true);
         this.textareaForPrint.setWrapStyleWord(true);
         this.textareaForPrint.setPreferredSize(this.pageDim);
         this.textareaForPrint.setTabSize(4);
         this.textareaForPrint.setText(printText);
 
         // Add on JWindow
         this.windowForPrint.add(this.textareaForPrint);
         this.windowForPrint.pack();
 
         // Wrapp text and give to TextArea
         this.textareaForPrint.setText(this.getWrappedText(this.textareaForPrint));
         this.pageDim = new Dimension(
                 (int) this.pageFormat.getImageableWidth() * CONS,
                 (int) this.pageFormat.getImageableHeight() * CONS);
         this.textareaForPrint.setPreferredSize(this.pageDim);
         this.windowForPrint.pack();
 
         // Calculate specifications of TextArea
         this.linesMaxOnPage = this.getMaxLines();
         this.linesTotal = this.textareaForPrint.getLineCount();
         this.numberOfPages = this.getNumberOfPages();
 
         // Calculate Start and End of the pages and store in pageBorders
         // And split text in passages and store in textPassages
         try {
             for (int i = 0; i < this.numberOfPages; i++) {
                 this.pageBorders[i][0] = this.textareaForPrint.getLineStartOffset(i * this.linesMaxOnPage);
             }
             for (int i = 0; i < this.numberOfPages - 1; i++) {
                 this.pageBorders[i][1] = this.pageBorders[i + 1][0] - 1;
             }
             this.pageBorders[this.numberOfPages - 1][1] = this.textareaForPrint.getLineEndOffset(this.linesTotal - 1);
             for (int i = 0; i < this.numberOfPages; i++) {
                 this.textPassages.add(this.textareaForPrint.getText(this.pageBorders[i][0], this.pageBorders[i][1]
                         - this.pageBorders[i][0]));
             }
         } catch (final BadLocationException e) {
             new PrintOperationException(e.getStackTrace()).showDialog();
             return;
         }
     }
 
     private boolean printPage(final int page) {
         if (page < 0 | page > this.numberOfPages - 1) {
             return false;
         }
         this.printerJob.setPrintable(this, this.pageFormat);
         this.textPassage = this.textPassages.get(page);
         try {
             this.printerJob.print();
         } catch (final PrinterException e) {
             new PrintOperationException(e.getStackTrace()).showDialog();
             return false;
         }
         return true;
     }
 
     public final void printText() {
         this.printerJob.setPrintable(this, this.pageFormat);
         for (int i = 0; i < this.numberOfPages; i++) {
             this.printPage(i);
         }
     }
 
     @Override
     public final int print(final Graphics g, final PageFormat pFormat, final int pageIndex) throws PrinterException {
         if (pageIndex > 0) {
             return Printable.NO_SUCH_PAGE;
         }
 
         final Graphics2D g2 = (Graphics2D) g;
 
         g2.translate((int) pFormat.getImageableX(), (int) pFormat.getImageableY());
         g2.scale(1.0 / CONS, 1.0 / CONS);
 
         this.textareaForPrint.setText(this.textPassage);
         this.bufferedImage = null;
         this.bufferedImage = new BufferedImage(this.pageDim.width, this.pageDim.height, BufferedImage.TYPE_BYTE_GRAY);
         this.textareaForPrint.paint(this.bufferedImage.getGraphics());
 
         g2.drawImage(this.bufferedImage, 0, 0, this.textareaForPrint);
         g2.dispose();
 
         return Printable.PAGE_EXISTS;
     }
 
     private String getWrappedText(final JTextComponent c) {
         final int len = c.getDocument().getLength();
         int offset = 0;
         StringBuffer buf = new StringBuffer((int) (len * 1.30));
         String s = "";
         try {
             while (offset < len) {
                 int end = Utilities.getRowEnd(c, offset);
                 if (end < 0) {
                     break;
                 }
                 end = Math.min(end + 1, len);
                 s = c.getDocument().getText(offset, end - offset);
                 buf.append(s);
                 if (!s.endsWith("\n")) {
                     buf.append('\n');
                 }
                 offset = end;
             }
         } catch (final BadLocationException e) {
             new PrintOperationException(e.getStackTrace()).showDialog();
         }
         try {
             return buf.toString();
         } finally {
             buf = null;
             s = null;
         }
     }
 
     private int getNumberOfPages() {
         final int max = this.getMaxLines();
         final int total = this.textareaForPrint.getLineCount();
         final int pages = (int) Math.ceil((double) total / (double) max);
         return pages;
     }
 
     private int getMaxLines() {
         return this.textareaForPrint.getHeight() / this.fontMetrics.getHeight();
     }
 
     // get defaultPrinter or opens printerSelectionDialog
     private void getPrintService() {
         final PrinterSelector instance = PrinterSelector.getInstance();
         try {
             if (instance.getService() != null) {
                 this.printerJob.setPrintService(instance.getService());
             } else {
                 this.printerJob.setPrintService(instance.selectPrinter());
             }
         } catch (final PrinterException e) {
            instance.setPrinter(PrintServiceLookup.lookupDefaultPrintService());
             this.getPrintService();
            new PrinterSelectionException(e.getStackTrace()).showDialog();
             return;
         }
     }
 }
