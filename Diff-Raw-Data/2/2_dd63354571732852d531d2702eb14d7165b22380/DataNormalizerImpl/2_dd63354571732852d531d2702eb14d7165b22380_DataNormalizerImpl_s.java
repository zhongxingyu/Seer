 package cz.cuni.mff.odcleanstore.datanormalization.impl;
 
 import cz.cuni.mff.odcleanstore.configuration.ConfigLoader;
 import cz.cuni.mff.odcleanstore.configuration.DataNormalizationConfig;
 import cz.cuni.mff.odcleanstore.connection.EnumLogLevel;
 import cz.cuni.mff.odcleanstore.connection.JDBCConnectionCredentials;
 import cz.cuni.mff.odcleanstore.connection.VirtuosoConnectionWrapper;
 import cz.cuni.mff.odcleanstore.connection.WrappedResultSet;
 import cz.cuni.mff.odcleanstore.connection.exceptions.DatabaseException;
 import cz.cuni.mff.odcleanstore.datanormalization.DataNormalizer;
 import cz.cuni.mff.odcleanstore.datanormalization.exceptions.DataNormalizationException;
 import cz.cuni.mff.odcleanstore.datanormalization.rules.DataNormalizationRule;
 import cz.cuni.mff.odcleanstore.datanormalization.rules.DataNormalizationRulesModel;
 import cz.cuni.mff.odcleanstore.shared.UniqueGraphNameGenerator;
 import cz.cuni.mff.odcleanstore.transformer.EnumTransformationType;
 import cz.cuni.mff.odcleanstore.transformer.TransformationContext;
 import cz.cuni.mff.odcleanstore.transformer.TransformedGraph;
 import cz.cuni.mff.odcleanstore.transformer.TransformedGraphException;
 import cz.cuni.mff.odcleanstore.transformer.TransformerException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.Serializable;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 /**
  * DataNormalizerImpl implements the default Data Normalization for ODCS
  *
  * It is meant to be used over dirty database only.
  *
  * @author Jakub Daniel
  */
 public class DataNormalizerImpl implements DataNormalizer, Serializable {
 	private static final long serialVersionUID = 1L;
 
 	private static final Logger LOG = LoggerFactory.getLogger(DataNormalizerImpl.class);
 
 	private static final String backupQueryFormat = "SPARQL INSERT INTO <%s> {?s ?p ?o} WHERE {GRAPH <%s> {?s ?p ?o}}";
 	private static final String selectQueryFormat = "SPARQL SELECT ?s ?p ?o FROM <%s> WHERE {?s ?p ?o}";
 	private static final String diffQueryFormat = "SPARQL DELETE FROM <%s> {?s ?p ?o} WHERE {GRAPH <%s> {?s ?p ?o}}";
 	private static final String dropBackupQueryFormat = "SPARQL CLEAR GRAPH <%s>";
 
	private static final String markTemporaryGraph = "INSERT INTO DB.ODCLEANSTORE.TEMPORARY_GRAPHS (graphName) VALUES (?)";
 	private static final String unmarkTemporaryGraph = "DELETE FROM DB.ODCLEANSTORE.TEMPORARY_GRAPHS WHERE graphName = ?";
 
 	/**
 	 * The following describer inner state of the transformer
 	 *   what database it works on
 	 *   what graph does it transform
 	 *
 	 * These need to be set consistently at each transformation
 	 */
 	private TransformedGraph inputGraph;
 	private TransformationContext context;
 	
 	/**
 	 * Debug utility graph names
 	 */
 	private String original;
 	private String modified;
 
 	/**
 	 * At construction the transformer is bound to use rules from particular rule groups
 	 *
 	 * Any arbitrary number of groups can be used
 	 *
 	 * The groups are selected by their IDs or Labels
 	 *
 	 * Either one needs to be not null after the transformer construction
 	 */
 	private Integer[] groupIds = null;
 	private String[] groupLabels = null;
 
 	private Collection<DataNormalizationRule> rules;
 
 	/**
 	 * constructs new data normalizer
 	 * @param groupIds the IDs of the rule groups to be used by the new instance
 	 */
 	public DataNormalizerImpl (Integer... groupIds) {
 		this.groupIds = groupIds;
 	}
 
 	/**
 	 * constructs new data normalizer
 	 * @param groupIds the Labels of the rule groups to be used by the new instance
 	 */
 	public DataNormalizerImpl (String... groupLabels) {
 		this.groupLabels = groupLabels;
 	}
 
 	/**
 	 * Connection to dirty database (needed in all cases to work on a new graph or a copy of an existing one)
 	 */
 	private VirtuosoConnectionWrapper dirtyConnection;
 
 	/**
 	 * constructs new connection to the dirty database.
 	 *
 	 * @return wrapped connection to the dirty database
 	 * @throws DatabaseException
 	 */
 	private VirtuosoConnectionWrapper getDirtyConnection () throws DatabaseException {
         if (dirtyConnection == null) {
         	dirtyConnection = VirtuosoConnectionWrapper.createConnection(context.getDirtyDatabaseCredentials());
        	}
 		return dirtyConnection;
 	}
 
 	/**
 	 * makes sure the connection to the dirty database is closed and not referenced
 	 */
 	private void closeDirtyConnection() {
 		try {
 			if (dirtyConnection != null) {
 				dirtyConnection.close();
 			}
 		} catch (DatabaseException e) {
 		} finally {
 			dirtyConnection = null;
 		}
 	}
 	
 	public static abstract class SerializableTransformedGraph implements TransformedGraph, Serializable {
 		private static final long serialVersionUID = 1L;
 	}
 
 	/**
 	 * constructs input graph for the transformer interface
 	 * @param name the name of the graph to be passed to the transformer
 	 * @return the input graph
 	 */
 	private static TransformedGraph prepareInputGraph (final String name) {
 		return new SerializableTransformedGraph() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public String getGraphName() {
 				return name;
 			}
 			@Override
 			public String getGraphId() {
 				return null;
 			}
 			@Override
 			public String getMetadataGraphName() {
 				return null;
 			}
 			@Override
             public String getProvenanceMetadataGraphName() {
                 return null;
             }
 			@Override
 			public Collection<String> getAttachedGraphNames() {
 				return null;
 			}
 			@Override
 			public void addAttachedGraph(String attachedGraphName)
 					throws TransformedGraphException {
 			}
 			@Override
 			public void deleteGraph() throws TransformedGraphException {
 			}
 			@Override
 			public boolean isDeleted() {
 				return false;
 			}
 		};
 	}
 
 	public static abstract class SerializableTransformationContext implements TransformationContext, Serializable {
 		private static final long serialVersionUID = 1L;
 	}
 
 	/**
 	 * constructs context for the transformer interface
 	 * @param clean the clean database connection credentials
 	 * @param dirty the dirty database connection credentials
 	 * @return the context
 	 */
 	private static TransformationContext prepareContext (final JDBCConnectionCredentials clean, final JDBCConnectionCredentials dirty) {
 		return new SerializableTransformationContext() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public JDBCConnectionCredentials getDirtyDatabaseCredentials() {
 				return dirty;
 			}
 			@Override
 			public JDBCConnectionCredentials getCleanDatabaseCredentials() {
 				return clean;
 			}
 			@Override
 			public String getTransformerConfiguration() {
 				return null;
 			}
 			@Override
 			public File getTransformerDirectory() {
 				return null;
 			}
 			@Override
 			public EnumTransformationType getTransformationType() {
 				return null;
 			}
 		};
 	}
 
 	/**
 	 * collects information about graph transformations
 	 * @param graphs input graphs
 	 * @param context context containing clean and dirty database connection credentials
 	 * @return per graph specification of modifications
 	 * @throws TransformerException
 	 */
 	public List<GraphModification> debugRules (HashMap<String, String> graphs, TransformationContext context)
 			throws TransformerException {
 		DataNormalizationConfig config = ConfigLoader.getConfig().getDataNormalizationGroup();
 
 		try {
 			Collection<String> originalGraphs = graphs.keySet();
 
 			/**
 			 * Start collecting modifications of the individual graphs
 			 */
 			List<GraphModification> result = new ArrayList<GraphModification>();
 
 			Iterator<String> it = originalGraphs.iterator();
 
 			/**
 			 * In case we need to know what changed we need to create copies of the graph to compare them after
 			 * the rule was applied
 			 */
 			UniqueGraphNameGenerator generator = new UniqueGraphNameGenerator(config.getTemporaryGraphURIPrefix() + this.getClass().getSimpleName() + "/diff/", context.getDirtyDatabaseCredentials());
 
 			original = generator.nextURI(0);
 			modified = generator.nextURI(1);
 
 			while (it.hasNext()) {
 				String originalName = it.next();
 				String temporaryName = graphs.get(originalName);
 
 				GraphModification subResult = getGraphModifications(temporaryName,
 						context.getCleanDatabaseCredentials(),
 						context.getDirtyDatabaseCredentials());
 				subResult.setGraphName(originalName);
 
 				result.add(subResult);
 			}
 
 			return result;
 		} catch (Exception e) {
 			LOG.error("Debugging of Data Normalization rules failed.");
 
 			throw new TransformerException(e);
 		}
 	}
 
 	/**
 	 * transforms graph in the dirty database
 	 * @param inputGraph the graph to be transformed
 	 * @param context the context specifying the connection credentials
 	 */
 	@Override
 	public void transformNewGraph(TransformedGraph inputGraph,
 			TransformationContext context) throws TransformerException {
 		transformExistingGraph(inputGraph, context);
 	}
 
 	/**
 	 * transforms graph in the dirty database (originally stored in clean db)
 	 * @param inputGraph the graph to be transformed
 	 * @param context the context specifying the connection credentials
 	 */
 	@Override
 	public void transformExistingGraph(TransformedGraph inputGraph,
 			TransformationContext context) throws TransformerException {
 		this.inputGraph = inputGraph;
 		this.context = context;
 
 		try
 		{
 			loadRules();
 			applyRules();
 		} catch (DataNormalizationException e) {
 			throw new TransformerException(e);
 		} finally {
 			closeDirtyConnection();
 		}
 
 		LOG.info("Data Normalization applied to graph {}", inputGraph.getGraphName());
 	}
 
 	/**
 	 * Triple that has either been deleted or inserted to a graph in one step of the normalization process
 	 * @author Jakub Daniel
 	 */
 	public class TripleModification implements Serializable {
 		private static final long serialVersionUID = 1L;
 
 		String subject;
 		String predicate;
 		String object;
 
 		public TripleModification(String s, String p, String o) {
 			this.subject = s;
 			this.predicate = p;
 			this.object = o;
 		}
 
 		public String getSubject() {
 			return subject;
 		}
 
 		public String getPredicate() {
 			return predicate;
 		}
 
 		public String getObject() {
 			return object;
 		}
 	}
 
 	/**
 	 * Collection of all the insertions and deletions that were applied by a certain rule
 	 * @author Jakub Daniel
 	 */
 	public class RuleModification implements Serializable {
 		private static final long serialVersionUID = 1L;
 
 		private Collection<TripleModification> insertions = new HashSet<TripleModification>();
 		private Collection<TripleModification> deletions = new HashSet<TripleModification>();
 
 		public void addInsertion(String s, String p, String o) {
 			insertions.add(new TripleModification(s, p, o));
 		}
 
 		public void addDeletion(String s, String p, String o) {
 			deletions.add(new TripleModification(s, p, o));
 		}
 
 		public Collection<TripleModification> getInsertions() {
 			return insertions;
 		}
 
 		public Collection<TripleModification> getDeletions() {
 			return deletions;
 		}
 	}
 
 	/**
 	 * The collection of all modifications done to a graph (grouped by the rules that did them)
 	 * @author Jakub Daniel
 	 */
 	public class GraphModification implements Serializable {
 		private static final long serialVersionUID = 1L;
 
 		private Map<DataNormalizationRule, RuleModification> modifications = new HashMap<DataNormalizationRule, RuleModification>();
 		private String graphName;
 
 		public void addInsertion (DataNormalizationRule rule, String s, String p, String o) {
 			if (modifications.containsKey(rule)) {
 				/**
 				 * Extend an existing modification done by a certain rule
 				 */
 				modifications.get(rule).addInsertion(s, p, o);
 			} else {
 				/**
 				 * Add new modification that corresponds to a certain rule
 				 */
 				RuleModification subModifications = new RuleModification();
 
 				subModifications.addInsertion(s, p, o);
 
 				modifications.put(rule, subModifications);
 			}
 		}
 
 		public void addDeletion(DataNormalizationRule rule, String s, String p, String o) {
 			if (modifications.containsKey(rule)) {
 				/**
 				 * Extend an existing modification done by a certain rule
 				 */
 				modifications.get(rule).addDeletion(s, p, o);
 			} else {
 				/**
 				 * Add new modification that corresponds to a certain rule
 				 */
 				RuleModification subModifications = new RuleModification();
 
 				subModifications.addDeletion(s, p, o);
 
 				modifications.put(rule, subModifications);
 			}
 		}
 
 		public Iterator<DataNormalizationRule> getRuleIterator() {
 			return modifications.keySet().iterator();
 		}
 
 		public RuleModification getModificationsByRule(DataNormalizationRule rule) {
 			return modifications.get(rule);
 		}
 
 		public String getGraphName() {
 			return graphName;
 		}
 
 		public void setGraphName(String graphName) {
 			this.graphName = graphName;
 		}
 	}
 
 	/**
 	 * collects modifications that are done to the given graph
 	 * @param graphName name of the graph to be transformed
 	 * @param clean the clean database connection credentials
 	 * @param source the source database (the one where the transformed graph is - normally dirty database) connection credentials
 	 * @return the collection of graph modifications grouped by individual rules
 	 * @throws TransformerException
 	 */
 	public GraphModification getGraphModifications(final String graphName,
 			final JDBCConnectionCredentials clean,
 			final JDBCConnectionCredentials source)
 			throws TransformerException {
 		this.inputGraph = prepareInputGraph(graphName);
 		this.context = prepareContext(clean, source);
 
 		GraphModification modifications = new GraphModification();
 
 		try
 		{
 			loadRules();
 
 			/**
 			 * Unlike during the transformation of graph running through the pipeline
 			 * collect the information about the whole process
 			 */
 			applyRules(modifications);
 		} catch (DataNormalizationException e) {
 			throw new TransformerException(e);
 		} finally {
 			closeDirtyConnection();
 		}
 
 		return modifications;
 	}
 
 	/**
 	 * selects rules to be used according to the specified rule groups
 	 * @throws DataNormalizationException
 	 */
 	private void loadRules() throws DataNormalizationException {
 		DataNormalizationRulesModel model = new DataNormalizationRulesModel(context.getCleanDatabaseCredentials());
 
 		/**
 		 * Either IDs or Labels need to be specified
 		 */
 		if (groupIds != null) {
 			rules = model.getRules(groupIds);
 		} else {
 			rules = model.getRules(groupLabels);
 		}
 
 		LOG.info("Data Normalization selected {} rules.", rules.size());
 	}
 
 	/**
 	 * applies all selected rules to the current input graph
 	 * @throws DataNormalizationException
 	 */
 	private void applyRules () throws DataNormalizationException {
 		applyRules(null);
 	}
 
 	/**
 	 * applies all selected rules to the current input graph
 	 * @param collection to be filled with the modifications in case it is not null
 	 * @throws DataNormalizationException
 	 */
 	private void applyRules(GraphModification modifications) throws DataNormalizationException {
 		try {
 			getDirtyConnection();
 
 			Iterator<DataNormalizationRule> i = rules.iterator();
 
 			/**
 			 * Ensure that the graph is either transformed completely or not at all
 			 */
 			getDirtyConnection().adjustTransactionLevel(EnumLogLevel.TRANSACTION_LEVEL, false);
 
 			while (i.hasNext()) {
 				DataNormalizationRule rule = i.next();
 
 				try {
 					performRule(rule, modifications);
 				} catch (Exception e) {
 					LOG.error(String.format(Locale.ROOT, "Debugging of rule %d failed: %s", rule.getId(), e.getMessage()));
 					throw new DataNormalizationException(e);
 				}
 			}
 
 			getDirtyConnection().commit();
 		} catch (DatabaseException e) {
 			throw new DataNormalizationException(e);
 		} catch (SQLException e) {
 			throw new DataNormalizationException(e);
 		}
 	}
 
 
 	/**
 	 * transforms the graph by one rule
 	 * @param rule the rule to be applied to the currently transformed graph
 	 * @param modifications collection to be filled with graph modifications done by this rule (in case it is not null)
 	 * @throws DataNormalizationException
 	 * @throws DatabaseException
 	 * @throws SQLException
 	 */
 	private void performRule(DataNormalizationRule rule, GraphModification modifications) throws DataNormalizationException, DatabaseException, SQLException {
 		if (inputGraph.getGraphName().length() == 0) {
 			throw new DataNormalizationException("Empty Graph Name is not allowed.");
 		}
 
 		if (modifications == null) {
 			/**
 			 * In case there is no interest in what was changed just perform all the components in the correct order
 			 */
 			performComponents(rule, inputGraph.getGraphName());
 		} else {
 			try {
 				getDirtyConnection().execute(markTemporaryGraph, original);
 				getDirtyConnection().execute(String.format(Locale.ROOT, backupQueryFormat, original, inputGraph.getGraphName()));
 
 				performComponents(rule, inputGraph.getGraphName());
 
 				getDirtyConnection().execute(markTemporaryGraph, modified);
 				getDirtyConnection().execute(String.format(Locale.ROOT, backupQueryFormat, modified, inputGraph.getGraphName()));
 
 				/**
 				 * Unfortunatelly "SELECT ?s ?p ?o WHERE {{GRAPH <%s> {?s ?p ?o}} MINUS {GRAPH <%s> {?s ?p ?o}}}"
 				 * throws "Internal error: 'output:valmode' declaration conflicts with 'output:format'"
 				 *
 				 * Therefore it is necessary to first create graphs with differences.
 				 */
 				getDirtyConnection().execute(String.format(Locale.ROOT, diffQueryFormat, modified, original));
 				getDirtyConnection().execute(String.format(Locale.ROOT, diffQueryFormat, original, inputGraph.getGraphName()));
 
 				WrappedResultSet inserted = getDirtyConnection().executeSelect(String.format(Locale.ROOT, selectQueryFormat, modified));
 
 				/**
 				 * All that is new to the transformed graph are insertions done by this rule (one of its components)
 				 */
 				while (inserted.next()) {
 					modifications.addInsertion(rule,
 							inserted.getString("s"),
 							inserted.getString("p"),
 							inserted.getString("o"));
 				}
 
 				WrappedResultSet deleted = getDirtyConnection().executeSelect(String.format(Locale.ROOT, selectQueryFormat, original));
 
 				/**
 				 * All that is missing from the transformed graph are deletions done by this rule (one of its components)
 				 */
 				while (deleted.next()) {
 					modifications.addDeletion(rule,
 							deleted.getString("s"),
 							deleted.getString("p"),
 							deleted.getString("o"));
 				}
 			} finally {
 				try {
 					getDirtyConnection().execute(String.format(Locale.ROOT, dropBackupQueryFormat, original));
 				} finally {}
 				try {
 					getDirtyConnection().execute(String.format(Locale.ROOT, dropBackupQueryFormat, modified));
 				} finally {}
 				getDirtyConnection().execute(unmarkTemporaryGraph, original);
 				getDirtyConnection().execute(unmarkTemporaryGraph, modified);
 			}
 		}
 		LOG.info(String.format(Locale.ROOT, "Data Normalization rule %d applied: %s", rule.getId(), rule.getDescription() != null ? rule.getDescription() : ""));
 	}
 
 	private void performComponents(DataNormalizationRule rule, String graphName) throws DataNormalizationException {
 		String[] components = rule.getComponents(graphName);
 
 		for (int j = 0; j < components.length; ++j) {
 			try {
 				getDirtyConnection().execute(components[j]);
 			} catch (Exception e) {
 				LOG.error(String.format(Locale.ROOT, "Failed to apply rule %d (component %d): %s\n\n%s\n\n%s",
 						rule.getId(),
 						rule.getComponents()[j].getId(),
 						rule.getDescription() != null ? rule.getDescription() : "",
 						components[j],
 						e.getMessage()));
 				throw new DataNormalizationException(e);
 			}
 		}
 	}
 
 	@Override
 	public void shutdown() throws TransformerException {
 	}
 }
