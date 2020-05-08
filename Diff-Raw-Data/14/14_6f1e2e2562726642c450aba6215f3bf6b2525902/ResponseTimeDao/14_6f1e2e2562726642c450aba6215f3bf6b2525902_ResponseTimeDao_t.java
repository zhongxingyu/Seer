 package net.java.dev.cejug.classifieds.server.reference.dao;
 
 import java.util.List;
 
 import javax.persistence.EntityTransaction;
 
 import net.java.dev.cejug.classifieds.server.dao.AbstractClassifiedsServerDao;
 import net.java.dev.cejug.classifieds.server.generated.contract.OperationTimestamp;
 import net.java.dev.cejug.classifieds.server.reference.dao.pojos.OperationTimestampEntity;
 
 public class ResponseTimeDao extends
 		AbstractClassifiedsServerDao<OperationTimestamp> {
 
 	@Override
 	public OperationTimestamp create() throws Exception {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void delete(OperationTimestamp type) throws Exception {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public List<OperationTimestamp> get(String query, int limit)
 			throws Exception {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public List<OperationTimestamp> getAll(int limit) throws Exception {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void update(OperationTimestamp source) throws Exception {
 		try {
 			OperationTimestampEntity entity = new OperationTimestampEntity(
 					source);
 			EntityTransaction transaction = manager.getTransaction();
 			transaction.begin();
 			manager.persist(entity);
 			transaction.commit();
			// TODO: log...
 		} catch (Exception e) {
			// TODO: log...
 			e.printStackTrace();
			if (manager == null)
				System.out.print("DATASOURCE NULO :( configuraçao errada...");
 		}
 	}
 }
