 package edu.psu.iam.cpr.utility;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.Date;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 
 import edu.psu.iam.cpr.core.database.Database;
 import edu.psu.iam.cpr.core.database.SessionFactoryUtil;
 import edu.psu.iam.cpr.utility.beans.Addresses;
 
 public class AddressesLoader implements BeanLoader {
 
 	@Override
 	public void loadTable(Database db, String primeDirectory, String tableName) {
 		BufferedReader bufferedReader = null;
 		try {
 			Date d = new Date();
 			String requestor = "SYSTEM";
 			
 			db.openSession(SessionFactoryUtil.getSessionFactory());
 			Session session = db.getSession();
 			
 			// Remove all of the records from the database table.
 			String sqlQuery = "delete from " + tableName;
 			Query query = session.createQuery(sqlQuery);
 			query.executeUpdate();
 
 			// Read in the first record containing the column headers.
 			bufferedReader = new BufferedReader(new FileReader(primeDirectory + System.getProperty("file.separator") + tableName));
 			String[] columns = bufferedReader.readLine().split("[|]");
 			String line;
 			
 			// Read and process the file.
 			while ((line = bufferedReader.readLine()) != null) {
 				String[] fields = line.split("[|]");
 				
 				Addresses bean = new Addresses();
 				bean.setCreatedBy(requestor);
 				bean.setCreatedOn(d);
 				bean.setLastUpdateBy(requestor);
 				bean.setLastUpdateOn(d);
 				bean.setStartDate(d);
 				bean.setEndDate(null);
 				
 				// person_id|address1|city|data_type_key|primary_flag|verified_flag|do_not_verify_flag|group_id
 
 				for (int i = 0; i < columns.length; ++i) {
 					if (columns[i].equals("person_id")) {
 						bean.setPersonId(new Long(fields[i]));
 					}
 					else if (columns[i].equals("address1")) {
 						bean.setAddress1(fields[i]);
 					}
 					else if (columns[i].equals("city")) {
 						bean.setCity(fields[i]);
 					}
 					else if (columns[i].equals("primary_flag")) {
 						bean.setPrimaryFlag(fields[i]);
 					}
 					else if (columns[i].equals("verified_flag")) {
 						bean.setVerifiedFlag(fields[i]);
 					}
 					else if (columns[i].equals("do_not_verify_flag")) {
 						bean.setDoNotVerifyFlag(fields[i]);
 					}
 					else if (columns[i].equals("group_id")) {
 						bean.setGroupId(new Long(fields[i]));
 					}
 
 				}
 				
 				session.save(bean);
 			}
 			db.closeSession();
 		}
 		catch (Exception e) {
 			db.rollbackSession();
 			e.printStackTrace();
 		}
 		finally {
 			try {
 				bufferedReader.close();
 			}
 			catch (Exception e) {
 			}
 		}
 
 	}
 
 }
