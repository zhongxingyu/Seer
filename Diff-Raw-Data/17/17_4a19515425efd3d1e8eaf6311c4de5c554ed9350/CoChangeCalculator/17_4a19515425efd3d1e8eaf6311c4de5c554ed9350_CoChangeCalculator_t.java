 package br.ufpa.linc.xflow.metrics.cochange;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 
 import br.ufpa.linc.xflow.core.processors.cochanges.CoChangesAnalysis;
 import br.ufpa.linc.xflow.data.dao.core.DependencyDAO;
 import br.ufpa.linc.xflow.data.dao.core.FileDependencyObjectDAO;
 import br.ufpa.linc.xflow.data.database.DatabaseManager;
 import br.ufpa.linc.xflow.data.entities.Analysis;
 import br.ufpa.linc.xflow.data.entities.Dependency;
 import br.ufpa.linc.xflow.data.entities.FileDependencyObject;
 import br.ufpa.linc.xflow.data.representation.matrix.Matrix;
 import br.ufpa.linc.xflow.exception.persistence.DatabaseException;
 
 public class CoChangeCalculator {
 
 	public void calculate(CoChangesAnalysis analysis) throws DatabaseException{
 		FileDependencyObjectDAO fileDependencyDAO = 
 			new FileDependencyObjectDAO();
 
 		//Obtains the last dependency from the analysis
 		Dependency<FileDependencyObject,FileDependencyObject> lastDependency = 
 			getLastDependency(analysis);
 		
 		//Builds the Co-Change Matrix
 		Matrix matrix = analysis.processHistoricalDependencyMatrix(lastDependency);
 		
 		List<CoChange> coChangeList = getCoChangeList(analysis,
 				fileDependencyDAO, matrix);
 		
 		System.out.println("CoChanges list size: " + coChangeList.size());
 		
 		//Persists Co-Changes
 		for(CoChange coChange : coChangeList){
 			insert(coChange);
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
 
 	private List<CoChange> getCoChangeList(CoChangesAnalysis analysis,
 			FileDependencyObjectDAO fileDependencyDAO, Matrix matrix)
 			throws DatabaseException {
 		
 		//Retrieves all file paths
 		List<String> filePathList = 
 			fileDependencyDAO.getFilePathsOrderedByStamp(analysis);
 			
 		//Builds the list of Co-Changes
 		List<CoChange> coChangeList = new ArrayList<CoChange>();
 		for (int i = 0; i < matrix.getRows(); i++) {
 			String a = filePathList.get(i);
			int aChanges = matrix.getValueAt(i,i);
 			
 			for (int j = i+1; j < matrix.getColumns(); j++) {
				int support = matrix.getValueAt(i,j);
 				
 				if(support > 0){
 					String b = filePathList.get(j);
					int bChanges = matrix.getValueAt(j,j);
 					
 					coChangeList.add(new CoChange(a, i, b, j, support, aChanges));
 					coChangeList.add(new CoChange(b, j, a, i, support, bChanges));
 				}
 			}
 		}
 		return coChangeList;
 	}
 	
 	private void insert(CoChange coChange){
 		try {
 			EntityManager manager = DatabaseManager.getDatabaseSession();
 			manager.getTransaction().begin();
 			manager.persist(coChange);
 			manager.getTransaction().commit();
 		} catch (Exception e){
 			e.printStackTrace();
 		}
 	}
 }
