 package br.ufpa.linc.xflow.core.processors.cochanges;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.DiscriminatorValue;
 import javax.persistence.Entity;
 import javax.persistence.Transient;
 
 import br.ufpa.linc.xflow.core.AnalysisFactory;
 import br.ufpa.linc.xflow.data.dao.core.DependencyDAO;
 import br.ufpa.linc.xflow.data.dao.core.DependencySetDAO;
 import br.ufpa.linc.xflow.data.database.DatabaseManager;
 import br.ufpa.linc.xflow.data.entities.Analysis;
 import br.ufpa.linc.xflow.data.entities.Dependency;
 import br.ufpa.linc.xflow.data.entities.DependencySet;
 import br.ufpa.linc.xflow.data.entities.Entry;
 import br.ufpa.linc.xflow.data.representation.Converter;
 import br.ufpa.linc.xflow.data.representation.jung.JUNGGraph;
 import br.ufpa.linc.xflow.data.representation.matrix.Matrix;
 import br.ufpa.linc.xflow.data.representation.matrix.MatrixFactory;
 import br.ufpa.linc.xflow.exception.persistence.DatabaseException;
 
 //TODO: AINDA TEM COISA PRA FAZER AQUI!
 @Entity(name = "cochanges_analysis")
 @DiscriminatorValue(""+AnalysisFactory.COCHANGES_ANALYSIS)
 public final class CoChangesAnalysis extends Analysis {
 
 	@Transient
 	private Matrix matrixCache = null;
 	
 	@Transient
 	private Dependency dependencyCache = null;
 	
 	@Transient
 	private JUNGGraph graphCache = null;
 	
 	public CoChangesAnalysis(){
 		this.setType(AnalysisFactory.COCHANGES_ANALYSIS);
 	}
 	
 	@Override
 	public boolean checkCutoffValues(Entry entry) {
 		if(this.getMaxFilesPerRevision() <= 0) return true;
 		if(entry.getEntryFiles().size() > this.getMaxFilesPerRevision()){
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	@SuppressWarnings("rawtypes")
 	public Matrix processEntryDependencyMatrix(Entry entry, int dependencyType) throws DatabaseException {
 		final Matrix matrix;
 		Dependency dependency = new DependencyDAO().findDependencyByEntry(this.getId(), entry.getId(), dependencyType);
 		
 		if(dependency == null){
 			if(dependencyCache != null){
 				return matrixCache;
 			}
 			else{
 				return null;
 			}
 		}
 		
 //		if(matrixCache == null){
 			matrix = processHistoricalDependencyMatrix(dependency);
 //			matrixCache = matrix;
 //			dependencyCache = dependency;
 //		}
 //		else{
 //			Matrix processedMatrix = processDependencyMatrix(dependency);
 //			matrix = processedMatrix.sumDifferentOrderMatrix(matrixCache);
 //			matrixCache = matrix;
 //			dependencyCache = dependency;
 //		}
 		
 		return matrix;
 	}
 
 	
 	@Override
 	@SuppressWarnings("rawtypes")
 	public final JUNGGraph processEntryDependencyGraph(final Entry entry, final int dependencyType) throws DatabaseException {
 		final Matrix matrix;
 		Dependency dependency = new DependencyDAO().findDependencyByEntry(this.getId(), entry.getId(), dependencyType);
 		
 		if(dependency == null){
 			if(dependencyCache != null){
 				return graphCache;
 			}
 			else{
 				return null;
 			}
 		}
 		
 		if(matrixCache == null){
 			matrix = processHistoricalDependencyMatrix(dependency);
 			matrixCache = matrix;
 			dependencyCache = dependency;
 			graphCache = JUNGGraph.convertMatrixToJUNGGraph(matrix, dependency);
 		}
 		else{
 			Matrix processedMatrix = processDependencyMatrix(dependency);
 //			matrix = processedMatrix.sumDifferentOrderMatrix(matrixCache);
 //			matrixCache = matrix;
 //			dependencyCache = dependency;
 			graphCache = JUNGGraph.convertMatrixToJUNGGraph(processedMatrix, dependency, graphCache);
 		}
 		
 		return graphCache;
 	}
 	
 	@SuppressWarnings("rawtypes")
 	public final JUNGGraph processDependencyGraph(final Dependency entryDependency) throws DatabaseException {
 		final Matrix matrix;
 		
 		if(entryDependency == null){
 			if(dependencyCache != null){
 				return graphCache;
 			}
 			else{
 				return null;
 			}
 		}
 		
 		if(matrixCache == null){
 			matrix = processHistoricalDependencyMatrix(entryDependency);
 			matrixCache = matrix;
 			dependencyCache = entryDependency;
 			graphCache = JUNGGraph.convertMatrixToJUNGGraph(matrix, entryDependency);
 		}
 		else{
 			Matrix processedMatrix = processDependencyMatrix(entryDependency);
 //			matrix = processedMatrix.sumDifferentOrderMatrix(matrixCache);
 //			matrixCache = matrix;
 //			dependencyCache = dependency;
 			graphCache = JUNGGraph.convertMatrixToJUNGGraph(processedMatrix, entryDependency, graphCache);
 		}
 		
 		return graphCache;
 	}
 	
 	@SuppressWarnings("rawtypes")
 	public final JUNGGraph processDependencyGraph2(final Dependency entryDependency) throws DatabaseException {
 		final Matrix matrix;
 		
 		if(entryDependency == null){
 			if(dependencyCache != null){
 				return graphCache;
 			}
 			else{
 				return null;
 			}
 		}
 		
 		if(matrixCache == null){
 			matrix = processHistoricalDependencyMatrix(entryDependency);
 			matrixCache = matrix;
 			dependencyCache = entryDependency;
 			graphCache = JUNGGraph.convertMatrixToJUNGGraph(matrix, entryDependency);
 		}
 		else{
 			Matrix processedMatrix = processDependencyMatrix(entryDependency);
 //			matrix = processedMatrix.sumDifferentOrderMatrix(matrixCache);
 //			matrixCache = matrix;
 //			dependencyCache = dependency;
 			graphCache = JUNGGraph.convertMatrixToJUNGGraph(processedMatrix, entryDependency, graphCache);
 		}
 		
 		return graphCache;
 	}
 	
 	@SuppressWarnings("rawtypes")
 	public final JUNGGraph processDependencyGraph3(final Dependency initialEntryDependency, final Dependency finalEntryDependency) throws DatabaseException {
 		Matrix matrix = new br.ufpa.linc.xflow.data.representation.matrix.sparse.XFlowSparseMatrixImpl();
 		
 		if(finalEntryDependency == null){
 			if(dependencyCache != null){
 				return graphCache;
 			}
 			else{
 				return null;
 			}
 		}
 		
 		if(matrixCache == null){
 			List<DependencySet> dependencies = new DependencySetDAO().getAllDependenciesSetBetweenDependencies(initialEntryDependency, finalEntryDependency);
 			matrix = processHistoricalDependencyMatrix2(finalEntryDependency, dependencies);
 			matrixCache = matrix;
 			dependencyCache = finalEntryDependency;
 			graphCache = JUNGGraph.convertMatrixToJUNGGraph(matrix, finalEntryDependency);
 		}
 		else{
 			Matrix processedMatrix = processDependencyMatrix(finalEntryDependency);
 //			matrix = processedMatrix.sumDifferentOrderMatrix(matrixCache);
 //			matrixCache = matrix;
 //			dependencyCache = dependency;
 			graphCache = JUNGGraph.convertMatrixToJUNGGraph(processedMatrix, finalEntryDependency, graphCache);
 		}
 		
 //		if(matrixCache == null){
 //			List<Dependency> dependencies = new DependencyDAO().findDependenciesBetweenDependencies(initialEntryDependency, finalEntryDependency);
 //			for (Dependency dependency : dependencies) {
 //				matrix = matrix.sumDifferentOrderMatrix(processDependencyMatrix(dependency));
 //			}
 //			matrixCache = matrix;	
 //			dependencyCache = finalEntryDependency;
 //			graphCache = JUNGGraph.convertMatrixToJUNGGraph(matrix, finalEntryDependency);
 //		}
 //		else{
 //			Matrix processedMatrix = processDependencyMatrix(finalEntryDependency);
 ////			matrix = processedMatrix.sumDifferentOrderMatrix(matrixCache);
 ////			matrixCache = matrix;
 ////			dependencyCache = dependency;
 //			graphCache = JUNGGraph.convertMatrixToJUNGGraph(processedMatrix, finalEntryDependency, graphCache);
 //			System.out.println("?");
 //		}
 		
 		return graphCache;
 	}
 	
 	@SuppressWarnings("rawtypes")
 	public final Matrix processHistoricalDependencyMatrix(final Dependency dependency) throws DatabaseException {
 		if(!dependency.getDependencies().isEmpty()){
 			final List<DependencySet> dependencySets = new DependencySetDAO().getAllDependenciesSetUntilDependency(dependency);
			//final Matrix matrix = Converter.convertDependenciesToMatrix(dependencySets, false);
			final Matrix matrix = Converter.convertDependenciesToMatrix(dependencySets, dependency.isDirectedDependency());
			
			//FIXME: (Move this code to the Calculators)
 			//As we don't have an application layer yet, it is necessary 
 			//to frequently clear the persistence context to avoid memory issues
			//DatabaseManager.getDatabaseSession().clear();
 			return matrix;
 		}
 		else{
 			return matrixCache;
 		}
 	}
 	
 	@SuppressWarnings("rawtypes")
 	public final Matrix processHistoricalDependencyMatrix(final Dependency initialDependency, final Dependency finalDependency) throws DatabaseException {
 		List<DependencySet> dependencies = new DependencySetDAO().getAllDependenciesSetBetweenDependencies(initialDependency, finalDependency);
 		final Matrix matrix = Converter.convertDependenciesToMatrix(dependencies, initialDependency.isDirectedDependency());
 		return matrix;
 	}
 	
 	@SuppressWarnings("rawtypes")
 	public final Matrix processHistoricalDependencyMatrix2(Dependency dependency, List<DependencySet> dependencies) throws DatabaseException {
 		final Matrix matrix = Converter.convertDependenciesToMatrix(dependencies, dependency.isDirectedDependency());
 		return matrix;
 	}
 
 	@SuppressWarnings("rawtypes")
 	public final Matrix processDependencyMatrix(final Dependency dependency) throws DatabaseException {
 		
 		if(!dependency.getDependencies().isEmpty()){
 			return Converter.convertDependenciesToMatrix(new ArrayList<DependencySet>(dependency.getDependencies()), dependency.isDirectedDependency());
 		}
 		else {
 			return MatrixFactory.createMatrix();
 		}
 	}
 }
