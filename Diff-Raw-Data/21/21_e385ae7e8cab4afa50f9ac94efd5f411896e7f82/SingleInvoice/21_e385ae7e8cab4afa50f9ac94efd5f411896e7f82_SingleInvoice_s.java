 package de.softwarekollektiv.dbs.invoices;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.LinkedList;
 import java.util.List;
 
 import de.softwarekollektiv.dbs.app.MenuItem;
 import de.softwarekollektiv.dbs.dbcon.DbConnection;
 
 // TODO beautify this.
 /* Was ich eigentlich wollte, war eine (nummerierte) Liste der rentals
  * in jeweils einer Zeile, a la
  *
  * 1) 22.04.11 (speedy): Matrix........................  1.33.-
  * 2) 17.06.11 (flat): X-Men 3.........................  5.22.-
  * Flat-Gebuehr fuer Juni 2011...................... 10.00.-
  * 3) 24.12.11 (speedy): Merry Chrismas with ..........  6.72.-
  *
  * Filmtitel dabei moeglicherweise abgekuerzt (insgesamt vllt 
  * 120 Zeichen pro Zeile), Kosten schoen untereinander usw.
  * Und dann eine Auswahl
  *
  * 1) Detaillierte Ansicht fuer Ausleihvorgang
  * 2) Show again
  * 3) Return to main menu
  *
  * bei auswahl von 1) wird man dann nach der Nummer gefragt und bekommt
  * eine detaillierte Ausgabe wie etwa
  * 
  * Movie title: Matrix
  * Rental date: 22.04.11 17:34:22
  * Returned: 22.04.11 21:19:12
  * Price category: A
  * Chosen payment type: speedy
  * Price per {hour,day}: 0.19.-
  * Price: 1.33.-
  */
 
 public class SingleInvoice implements MenuItem {
 	private static final Calendar cal = new GregorianCalendar();
 	
 	private final PrintStream out;
 	private final DbConnection dbcon;
 	private final BufferedReader in;
 
 	public SingleInvoice(PrintStream out, InputStream in, DbConnection dbcon) {
 		this.out = out;
 		this.in = new BufferedReader(new InputStreamReader(in));
 		this.dbcon = dbcon;
 	}
 	
 	@Override
 	public String getTitle() {
 		return "Rental overview (for a customer)";
 	}
 
 	@Override
 	public String getDescription() {
 		return "Print an overview/invoice for a customer, showing all his/her rentals.";
 	}
 
 	@Override
 	public boolean run() throws Exception {
 
 		int id = readCustomerId();
 		if(id < 0)
 			return true;
 		
 		String name = getCustomerName(id);
 		if(name == null)
 			return true;
 		
 		List<InvoicePosition> positions = getPositions(id);
 		
 		printOverview(id, name, positions);
 		
 		return true;
 	}
 
 	private int readCustomerId() throws IOException {	
 		out.print("Insert customer ID: ");
 		String line = in.readLine();
 		try {
 			return Integer.parseInt(line);
 		} catch (NumberFormatException e) {
 			out.println("Not a valid customer ID.");
 			return -1;
 		}
 	}
 	
 	private String getCustomerName(int id) throws SQLException {
 		String retval = null;
 		
 		PreparedStatement cusIdStmt = dbcon.getConnection().prepareStatement(
 				"SELECT surename, forename FROM customers WHERE cus_id = ?");
 		cusIdStmt.setInt(1, id);
 		ResultSet rs = cusIdStmt.executeQuery();
 		if(rs.next())
 			retval = rs.getString(1) + ", " + rs.getString(2);
 		else {
 			out.println("Could not find customer with ID '" + id + "'.");
 		}	
 		
 		cusIdStmt.close();
 		return retval;
 	}
 	
 	private List<InvoicePosition> getPositions(int id) throws SQLException {
 		List<InvoicePosition> retval = new LinkedList<InvoicePosition>();
 		
 		PreparedStatement rentalsStmt = dbcon.getConnection().prepareStatement(
 				"SELECT title, price_category, type, startdate, duration FROM " +
 				"rentals JOIN movies ON rentals.mov_id = movies.mov_id WHERE " +
 				"cus_id = ? ORDER BY startdate");
 		rentalsStmt.setInt(1, id);
 		ResultSet rentals = rentalsStmt.executeQuery();
 		
 		boolean firstBseen = false;
 		Date lastRentalTS = null;
 		while(rentals.next()) {
 			RentalInvoicePosition rip = new RentalInvoicePosition();
 			
 			rip.title = rentals.getString("title");
 			rip.price_category = rentals.getString("price_category");
 			rip.type = rentals.getString("type");
 			rip.startdate = rentals.getTimestamp("startdate");
 			rip.duration = rentals.getInt("duration");
 			
 			if(rip.price_category.equals("A")) {
				if(rip.type.equals("speedy") || rip.type.equals("flat"))
 					rip.amount = (0.19 * rip.duration);
				else /* starter */
 					rip.amount = (1.29 * rip.duration);
 			} else {
				if(rip.type.equals("speedy"))
 					rip.amount = (0.15 * rip.duration);
				else if(rip.type.equals("starter"))
 					rip.amount = (0.79 * rip.duration);
				else {
 					if(!firstBseen) {
 						firstBseen = true;
 						rip.amount = 0.0;
 					} else
 						rip.amount = (0.19 * rip.duration);
 				}
 			}
 			retval.add(rip);
 			
 			// 10.- per month for flat
 			if(rip.type.equals("flat") && 
 				((lastRentalTS == null) || 
 				(getMonth(lastRentalTS) != getMonth(rip.startdate)) ||
 				(getYear(lastRentalTS) != getYear(rip.startdate)))) {
 				lastRentalTS = rip.startdate;
 				FlatFeeInvoicePosition ffip = new FlatFeeInvoicePosition();
 				ffip.month = rip.startdate;
 				retval.add(ffip);
 			}
 		}
 		
 		return retval;
 	}
 
 	private void printOverview(int cus_id, String name, List<InvoicePosition> positions) {
 		out.println();
 		out.println("Rentals overview for customer: " + name + " (" + cus_id + ")");
 		out.println("============================");
 		double total = 0;
 		for(InvoicePosition ip : positions) {
 			ip.print(out);
 			total += ip.getAmount();
 		}
 		out.println("----------------------------");
 		out.print("Total: " + total);
 		out.println();
 	}
 	
 	// Timestamp.getMonth() & getYear() are deprecated. Wtf
 	private static int getMonth(Date t) {
 		cal.setTimeInMillis(t.getTime());
 		return cal.get(Calendar.MONTH);
 	}
 	private static int getYear(Date t) {
 		cal.setTimeInMillis(t.getTime());
 		return cal.get(Calendar.YEAR);
 	}
 	
 	private interface InvoicePosition {
 		void print(PrintStream out);
 		void printDetails(PrintStream out);
 		double getAmount();
 	}
 	
 	private static class RentalInvoicePosition implements InvoicePosition {
 		String title;
 		String price_category;
 		Timestamp startdate;
 		int duration;
 		String type;
 		double amount;
 		
 		@Override
 		public void print(PrintStream out) {
 			out.println();
 			out.println("Date: " + startdate);
 			out.println("Title: " + title);
 			out.println("Cost: " + amount);
 		}
 		@Override
 		public void printDetails(PrintStream out) {
 			// TODO Auto-generated method stub
 			
 		}
 		@Override
 		public double getAmount() {
 			return amount;
 		}
 
 	}
 	
 	private static class FlatFeeInvoicePosition implements InvoicePosition {
 		Date month;
 		
 		@Override
 		public void print(PrintStream out) {
 			out.println(
 					"Flat fee for month " + (getMonth(month) + 1) + "."	+
 					getYear(month));
			out.println(10);
 		}
 
 		@Override
 		public void printDetails(PrintStream out) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public double getAmount() {
 			return 10;
 		}
 	}
 }
