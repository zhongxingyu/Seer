 package controller;
 
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
 
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableModel;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
 
 import controller.ControllerListener;
 
 import oracle.jdbc.pool.OracleDataSource;
 
 public class Controller implements ControllerInterface {
 	public static final String URL = "jdbc:oracle:thin:bd2a14/bobas47@ikar.elka.pw.edu.pl:1521/elka.elka.pw.edu.pl";
 
	private List<ControllerListener> controllerListeners = new LinkedList<ControllerListener>();
 	private Connection conn;
 	private Statement stmt;
 	private PreparedStatement prepStatement;
 
 	@Override
 	public void connect() {
 		Thread t = new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				try {
 					OracleDataSource ods = new OracleDataSource();
 					ods.setURL(URL);
 					conn = ods.getConnection();
 					stmt = conn.createStatement();
 					prepStatement = conn
 							.prepareStatement("SELECT P.NUMER_POKOJU, PR.OD, PR.DO "
 									+ "FROM POKOJE P, POZYCJE_REZERWACJI PR "
 									+ "WHERE P.NUMER_POKOJU = PR.NUMER_POKOJU "
 									+ "AND PR.DO >= ? "
 									+ "AND PR.OD <= ? "
 									+ "ORDER BY P.NUMER_POKOJU");
 					fireSessionStateChanged(ControllerInterface.State.CONNECTED);
 				} catch (SQLException e) {
 					fireError(e.getMessage());
 					return;
 				}
 
 			}
 
 		});
 		t.start();
 	}
 
 	@Override
 	public void disconnect() {
 		try {
 			prepStatement.close();
 			stmt.close();
 			conn.close();
 			fireSessionStateChanged(ControllerInterface.State.DISCONNECTED);
 		} catch (Exception e) {
 			return;
 		}
 	}
 
 	@Override
 	public void showBookings(final JTable destTable, final Date from, final Date to) {
 		Thread t = new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				try {
 					System.out.println("showBookings");
 					prepStatement.setDate(1, from);
 					prepStatement.setDate(2, to);
 					ResultSet rset = prepStatement.executeQuery();
 					DefaultTableModel model = new DefaultTableModel();
 					destTable.setModel(model);
 					model.addColumn("Room");
 					model.addColumn("From");
 					model.addColumn("To");
 					while (rset.next()) {
 						System.out.println("addRow");
 						model.addRow(new Object[] { rset.getInt(1), rset.getDate(2),
 								rset.getDate(3) });
 					}
 					rset.close();
 				} catch (Exception e) {
 					fireError(ControllerInterface.State.DISCONNECTED.toString());
 					return;
 				}
 			}
 			
 		});
 		t.start();
 	}
 
 	public void addControllerListener(ControllerListener cl) {
 		controllerListeners.add(cl);
 	}
 
 	protected void fireSessionStateChanged(ControllerInterface.State s) {
 		System.out.println("fireSessionStateChanged");
		for(ControllerListener cl : controllerListeners){
			cl.sessionStateChanged(s);
 		}
 	}
 
 	protected void fireError(String err) {
 		System.out.println("fireError");
		for(ControllerListener cl : controllerListeners){
			cl.error(err);
 		}
 	}
 
 }
