 package org.computer.knauss.reqtDiscussion.scripts;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.Properties;
 
 import org.computer.knauss.reqtDiscussion.io.DAOException;
 import org.computer.knauss.reqtDiscussion.io.IDiscussionDAO;
 import org.computer.knauss.reqtDiscussion.io.jazz.rest.JazzJDOMDAO;
 import org.computer.knauss.reqtDiscussion.io.jazz.util.ui.DialogBasedJazzAccessConfiguration;
 import org.computer.knauss.reqtDiscussion.io.sql.psql.PSQLDiscussionDAO;
 import org.computer.knauss.reqtDiscussion.io.sql.psql.PSQLDiscussionEventDAO;
 import org.computer.knauss.reqtDiscussion.model.Discussion;
 
 public class FetchJazzToPSQL {
 
 	/**
 	 * @param args
 	 * @throws IOException
 	 * @throws FileNotFoundException
 	 * @throws DAOException
 	 * @throws SQLException 
 	 */
 	public static void main(String[] args) throws FileNotFoundException,
 			IOException, DAOException, SQLException {
 		Properties p = new Properties();
 		p.load(new FileInputStream("jazz-properties.txt"));
 
 		DialogBasedJazzAccessConfiguration config = new DialogBasedJazzAccessConfiguration();
 		config.configure(p);
 		IDiscussionDAO jazzDiscussions = new JazzJDOMDAO(config);
 		((JazzJDOMDAO) jazzDiscussions)
 				.setProjectArea("Rational Team Concert");
 		PSQLDiscussionDAO psqlDiscussions = new PSQLDiscussionDAO();
 
 		PSQLDiscussionEventDAO psqlDiscussionEvents = new PSQLDiscussionEventDAO();
 
 		psqlDiscussionEvents.dropSchema();
 		psqlDiscussions.dropSchema();
 		
 		for (Discussion d : jazzDiscussions.getDiscussions()) {
 			psqlDiscussions.storeDiscussion(d);
 			psqlDiscussionEvents.storeDiscussionEvents(d.getAllComments());
 		}
 	}
 
 }
