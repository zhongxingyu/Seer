 package br.ufpr.c3sl.daoFactory;
 
 import br.ufpr.c3sl.dao.MistakeDAO;
 import br.ufpr.c3sl.dao.RetroactionDAO;
 import br.ufpr.c3sl.dao.UserDAO;
 import br.ufpr.c3sl.daoconcrete.DB4OMistake;
 import br.ufpr.c3sl.daoconcrete.DB4ORetroaction;
 import br.ufpr.c3sl.daoconcrete.DB4OUserDAO;
 import br.ufpr.c3sl.view.principal.JpCarrie;
 
 import com.db4o.Db4oEmbedded;
 import com.db4o.EmbeddedObjectContainer;
 
 public class DB4ODAOFactory extends DAOFactory {
 	
 	public static String PROJECT_NAME = "";
 	
 //	private final static String DB4OFILENAME =
 //		System.getProperty("user.home") + "/carrie_"+PROJECT_NAME+"_DB4O.db";
 
 	private static EmbeddedObjectContainer container;
 	
 	public static EmbeddedObjectContainer getConnection(){
 		if (container == null)
 			container = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(),
					System.getProperty("user.home") + "/carrie_"+JpCarrie.getInstance().getName()+"_DB4O.db");
 		
 		return 	container;
 	}
 	
 //	private static EmbeddedConfiguration configure(){
 //        EmbeddedConfiguration configuration = Db4oEmbedded.newConfiguration();
 //            configuration.common().objectClass(Timestamp.class).storeTransientFields(true);
 //            return configuration;
 //    }
 	
 	@Override
 	public UserDAO getUserDAO() {
 		return new DB4OUserDAO();
 	}
 
 	@Override
 	public MistakeDAO getMistakeDAO() {
 		return new DB4OMistake();
 	}
 
 	@Override
 	public RetroactionDAO getRetroactionDAO() {
 		return new DB4ORetroaction();
 	}
 
 }
