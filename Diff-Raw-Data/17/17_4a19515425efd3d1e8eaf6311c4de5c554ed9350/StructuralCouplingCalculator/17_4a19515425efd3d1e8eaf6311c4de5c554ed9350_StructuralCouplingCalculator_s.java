 package br.ufpa.linc.xflow.metrics.cochange;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 
 import br.ufpa.linc.xflow.core.processors.callgraph.CallGraphAnalysis;
 import br.ufpa.linc.xflow.data.dao.core.DependencyDAO;
 import br.ufpa.linc.xflow.data.dao.core.FileDependencyObjectDAO;
 import br.ufpa.linc.xflow.data.database.DatabaseManager;
 import br.ufpa.linc.xflow.data.entities.Analysis;
 import br.ufpa.linc.xflow.data.entities.Dependency;
 import br.ufpa.linc.xflow.data.entities.FileDependencyObject;
 import br.ufpa.linc.xflow.data.representation.matrix.Matrix;
 import br.ufpa.linc.xflow.exception.persistence.DatabaseException;
 
 public class StructuralCouplingCalculator {
 
 	public void calculate(CallGraphAnalysis analysis) throws DatabaseException{
 		FileDependencyObjectDAO fileDependencyDAO = 
 			new FileDependencyObjectDAO();
 
 		//Obtains the last dependency from the analysis
 		Dependency<FileDependencyObject,FileDependencyObject> lastDependency = 
 			getLastDependency(analysis);
 		
 		//Builds the Co-Change Matrix
 		Matrix matrix = analysis.processHistoricalDependencyMatrix(lastDependency);
 				
 		List<StructuralCoupling> couplingsList = 
 			getStructuralCouplinsList(analysis,fileDependencyDAO, matrix);
 		
 		System.out.println("Couplings list: " + couplingsList.size());
 		
 		//Persists Couplings
 		for(StructuralCoupling coupling : couplingsList){
 			insert(coupling);
 			DatabaseManager.getDatabaseSession().clear();
 		}
 	}
 
 	private Dependency<FileDependencyObject,FileDependencyObject> getLastDependency(Analysis analysis) throws DatabaseException{
 		DependencyDAO dependencyDAO = new DependencyDAO();
 		
 		Dependency<FileDependencyObject, FileDependencyObject> lastDependency = 
 			dependencyDAO.findHighestDependencyByEntry(
 				analysis.getId(), 
 				analysis.getLastEntry().getId(), 
 				Dependency.TASK_DEPENDENCY);
 		
 		return lastDependency; 
 	}
 
 	private List<StructuralCoupling> getStructuralCouplinsList(CallGraphAnalysis analysis,
 			FileDependencyObjectDAO fileDependencyDAO, Matrix matrix)
 			throws DatabaseException {
 		
 		//Retrieves all file paths
 		List<String> filePathList = 
 			fileDependencyDAO.getFilePathsOrderedByStamp(analysis);
 			
 		//Builds the couplingsList
 		List<StructuralCoupling> couplingsList = new ArrayList<StructuralCoupling>();
 		for (int i = 0; i < matrix.getRows(); i++) {
 			String supplier = filePathList.get(i);
 			
 			for (int j = 0; j < matrix.getColumns(); j++) {	
 				if(i != j){
 					
					int clientCalls = matrix.get(i,j);
					int clientChanges = matrix.get(j,j);
 					
 					if(clientCalls > 0){
 						String client = filePathList.get(j);
 						
 						couplingsList.add(new StructuralCoupling(client, j, 
 								supplier, i, clientCalls, clientChanges));
 					}			
 				}
 			}
 		}
 		return couplingsList;
 	}
 	
 	private void insert(StructuralCoupling coupling){
 		try {
 			EntityManager manager = DatabaseManager.getDatabaseSession();
 			manager.getTransaction().begin();
 			manager.persist(coupling);
 			manager.getTransaction().commit();
 		} catch (Exception e){
 			e.printStackTrace();
 		}
 	}
 }
