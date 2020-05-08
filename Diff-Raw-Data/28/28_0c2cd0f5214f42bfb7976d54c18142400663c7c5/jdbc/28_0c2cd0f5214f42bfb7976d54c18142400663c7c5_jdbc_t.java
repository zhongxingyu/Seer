 public class A1_Transaktion {
 	public static void main(String[] args) {
 		final String user = "anguser";
 		final String password = "angproj";
 		final String database = "jdbc:postgresql://localhost/angproj";
		try (Connection connection = DriverManager.getConnection(database,user, password)) {
 			try(Statement stmt = connection.createStatement()) {
 				connection.setAutoCommit(false);
 				connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
				stmt.execute("INSERT INTO ang VALUES (42, 'Meier, Max', 42)");
				stmt.execute("UPDATE ang SET chef=42 WHERE chef=11");
				stmt.executeUpdate("DROP TABLE Wegdamit"); //Also valid for CREATE INSERT DELETE
				ResultSet rs = stmt.executeQuery("SELECT * FROM Nochda");
				while(rs.next()){	
					String sbuff = rs.getString(1);
					System.out.println(sbuff);
				}
 				//Batch Upddate
				stmt.addBatch("UPDATE projekt SET leiter=42 WHERE leiter=11");
				stmt.addBatch("DELETE FROM zuteilung WHERE persnr=11");
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
