 package travellinktech.ticket_printer;
 
 import java.awt.print.Printable;
 import java.awt.print.PrinterException;
 import java.awt.print.PrinterJob;
 import java.util.List;
 import javax.print.PrintService;
 import travellinktech.ticket_printer.exceptions.NoSuchPrinterException;
 import travellinktech.ticket_printer.exceptions.NoTicketPageSettingsAssigned;
 import travellinktech.ticket_printer.printables.PrintableTickets;
 
 
 /*
  * TicketPrintCommand
  *  - page settings
  *  - printer
  *  - tickets Ticket[]
  */
 public class TicketPrinter {
 
   private final PrinterJob printerJob;
   private TicketPageSettings ticketPageSettings;
   static final Object ticketPrintingLock = new Object();
 
   public TicketPrinter() {
     this.printerJob = PrinterJob.getPrinterJob();
   }
 
   public void setTicketPageSettings(double width, double height, double margin) {
     this.ticketPageSettings = new TicketPageSettings(width, height, margin);
   }
 
   public void setTicketPageSettings(TicketPageSettings ticketPageSettings) {
     this.ticketPageSettings = ticketPageSettings;
   }
 
   /* 
    * Shows printer dialog, allowing user to choose printer
    * Returns true if user hit's OK, false if CANCEL
    */
   public boolean printDialog() {
     return printerJob.printDialog();
   }
 
   /*
    * Set the printer by index from the known printServices
    * */
   public void setPrinter(int printerIndex) throws PrinterException, NoSuchPrinterException {
     PrintService requestedPrintService = new PrintServiceLocator().findByIndex(printerIndex);
     this.printerJob.setPrintService(requestedPrintService);
   }
 
   /*
    * Set the printer by name from the known printServices
    * */
   public void setPrinter(String name) throws PrinterException, NoSuchPrinterException {
     PrintService requestedPrintService = new PrintServiceLocator().findByName(name);
     this.printerJob.setPrintService(requestedPrintService);
   }
 
 
   /*
    * Prints the tickets using the configured page sizes as set out in initializer-passed printerJob
    */
   public boolean printTickets(List<Ticket> tickets) throws PrinterException, NoTicketPageSettingsAssigned {
     if (ticketPageSettings == null) {
      throw new NoTicketPageSettingsAssigned("No TicketPageSettings were set");
     }
 
     // Generate a printable for tickets
     Printable printable = new PrintableTickets(tickets);
 
     // Lock here since each ticket prints as separate job -- OS will interleave tickets
     synchronized (ticketPrintingLock) {
       printerJob.setPrintable(printable,  ticketPageSettings.getPageFormat());
       printerJob.print(null);
     }
     
     return true;
   }
 
 }
