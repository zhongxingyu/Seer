 public class A1_Transaktion {

 	public static void main(String[] args) {
 		final String user = "anguser";
 		final String password = "angproj";
 		final String database = "jdbc:postgresql://localhost/angproj";

		try (Connection connection = DriverManager.getConnection(database,
				user, password)) {

 			try(Statement stmt = connection.createStatement()) {
 				connection.setAutoCommit(false);
 				connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
				stmt.execute("INSERT INTO angestellter VALUES (4242, 'Meier, Martha', 424, 12000.00, NULL, 1, 'Rapperswil', current_date, 0.00)");
				stmt.execute("UPDATE angestellter SET chef=4242 WHERE chef=1001");
 				//Batch Upddate
				stmt.addBatch("UPDATE projekt SET projleiter=4242 WHERE projleiter=1001");
				stmt.addBatch("DELETE FROM projektzuteilung WHERE persnr=1001");
				stmt.addBatch("DELETE FROM angestellter WHERE name = 'Marxer, Markus'");
 				stmt.executeBatch();
				
 				connection.commit();
 			} catch (SQLException ex) {
 				System.err.println(ex.getMessage() +"\nRollback...");
 				connection.rollback();
 			}
			
 		} catch (SQLException ex) {
 			System.err.println("SQLException: " + ex.getMessage());
 		}
 	}
 
 }
