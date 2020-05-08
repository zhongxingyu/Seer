 package cz.cuni.mff.odcleanstore.qualityassessment.impl;
 
 import cz.cuni.mff.odcleanstore.configuration.ConfigLoader;
 import cz.cuni.mff.odcleanstore.connection.EnumLogLevel;
 import cz.cuni.mff.odcleanstore.connection.JDBCConnectionCredentials;
 import cz.cuni.mff.odcleanstore.connection.VirtuosoConnectionWrapper;
 import cz.cuni.mff.odcleanstore.connection.WrappedResultSet;
 import cz.cuni.mff.odcleanstore.connection.exceptions.DatabaseException;
 import cz.cuni.mff.odcleanstore.qualityassessment.QualityAggregator;
 import cz.cuni.mff.odcleanstore.qualityassessment.exceptions.QualityAssessmentException;
 import cz.cuni.mff.odcleanstore.transformer.EnumTransformationType;
 import cz.cuni.mff.odcleanstore.transformer.TransformationContext;
 import cz.cuni.mff.odcleanstore.transformer.TransformedGraph;
 import cz.cuni.mff.odcleanstore.transformer.TransformedGraphException;
 import cz.cuni.mff.odcleanstore.transformer.TransformerException;
 import cz.cuni.mff.odcleanstore.vocabulary.ODCS;
 import cz.cuni.mff.odcleanstore.vocabulary.XMLSchema;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.Locale;
 
 public class QualityAggregatorImpl implements QualityAggregator {
 	private static final Logger LOG = LoggerFactory.getLogger(QualityAggregatorImpl.class);
 
 	public static void main(String[] args) {
 		try {
 			ConfigLoader.loadConfig();
 			new QualityAggregatorImpl().transformNewGraph(new TransformedGraph () {
 
 				@Override
 				public String getGraphName() {
 					// TODO Auto-generated method stub
 					return "http://opendata.cz/data/namedGraph/3";
 				}
 
 				@Override
 				public String getGraphId() {
 					// TODO Auto-generated method stub
 					return null;
 				}
 
 				@Override
 				public String getMetadataGraphName() {
 					// TODO Auto-generated method stub
 					return "http://opendata.cz/data/metadata";
 				}
 
 				@Override
 				public Collection<String> getAttachedGraphNames() {
 					// TODO Auto-generated method stub
 					return null;
 				}
 
 				@Override
 				public void addAttachedGraph(String attachedGraphName)
 						throws TransformedGraphException {
 					// TODO Auto-generated method stub
 
 				}
 
 				@Override
 				public void deleteGraph() throws TransformedGraphException {
 					// TODO Auto-generated method stub
 
 				}
 
 				@Override
 				public boolean isDeleted() {
 					// TODO Auto-generated method stub
 					return false;
 				}
 
                 @Override
                 public String getProvenanceMetadataGraphName() {
                  // TODO Auto-generated method stub
                     return "http://opendata.cz/data/provenanceMetadata";
                 }
 
 			}, new TransformationContext() {
 
 				@Override
 				public JDBCConnectionCredentials getDirtyDatabaseCredentials() {
 					// TODO Auto-generated method stub
 					return new JDBCConnectionCredentials("jdbc:virtuoso://localhost:1112/UID=dba/PWD=dba", "dba", "dba");
 				}
 
 				@Override
 				public JDBCConnectionCredentials getCleanDatabaseCredentials() {
 					// TODO Auto-generated method stub
 					return new JDBCConnectionCredentials("jdbc:virtuoso://localhost:1111/UID=dba/PWD=dba", "dba", "dba");
 				}
 
 				@Override
 				public String getTransformerConfiguration() {
 					return null;
 				}
 
 				@Override
 				public File getTransformerDirectory() {
 					// TODO Auto-generated method stub
 					return null;
 				}
 
 				@Override
 				public EnumTransformationType getTransformationType() {
 					// TODO Auto-generated method stub
 					return null;
 				}
 
 			});
 		} catch (Exception e) {
 			LOG.error(e.getMessage());
 		}
 	}
 
 	//TODO: null is just a special graph (might be replaced by something more configurable)
 
 	private final static String dropOutdatedQueryFormat = "SPARQL DELETE FROM <%s> {?publisher <" + ODCS.publisherScore + "> ?score} WHERE {?publisher <" + ODCS.publisherScore + "> ?score. FILTER (?publisher = <%s>)}";
 	private final static String computeSumUpdatedQueryFormat = "SPARQL SELECT SUM(?score) WHERE {?graph <" + ODCS.score + "> ?score; <" + ODCS.publishedBy + "> <%s>}";
 	private final static String computeCountUpdatedQueryFormat = "SPARQL SELECT COUNT(?score) WHERE {?graph <" + ODCS.score + "> ?score; <" + ODCS.publishedBy + "> <%s>}";
 	private final static String storeUpdatedQueryFormat = "SPARQL INSERT DATA INTO <%s> {<%s> <" + ODCS.publisherScore + "> \"%f\"^^<" + XMLSchema.doubleType + ">}";
 	private final static String graphPublisherQueryFormat = "SPARQL SELECT ?publisher FROM <%s> WHERE {<%s> <" + ODCS.publishedBy + "> ?publisher}";
 	private final static String graphScoreQueryFormat = "SPARQL SELECT ?score FROM <%s> WHERE {<%s> <" + ODCS.score + "> ?score}";
 
 	private TransformationContext context;
 
 	/**
 	 * The aggregated score consists of all scores of graphs in the clean database and the score of the new graph (restrained to graphs published by the same publisher
 	 * who published the currently processed graph).
 	 */
 	@Override
 	public void transformNewGraph(TransformedGraph inputGraph,
 			TransformationContext context) throws TransformerException {
 		this.context = context;
 
 		try
 		{
 			Double newScore = getGraphScore(inputGraph.getGraphName(), inputGraph.getMetadataGraphName(), getDirtyConnection());
 
 			updatePublisherScore(inputGraph.getGraphName(),
 					inputGraph.getMetadataGraphName(),
 					newScore,
 					1);
 		} catch (QualityAssessmentException e) {
 			throw new TransformerException(e);
 		} catch (DatabaseException e) {
 			throw new TransformerException(e);
 		} finally {
 			closeCleanConnection();
 			closeDirtyConnection();
 		}
 	}
 
 	/**
 	 * Just compute score from all graphs in the clean database that share the publisher (the current graph is among them)
 	 */
 	@Override
 	public void transformExistingGraph(TransformedGraph inputGraph,
 			TransformationContext context) throws TransformerException {
 		this.context = context;
 
 		try
 		{
 			Double outdatedScore = getGraphScore(inputGraph.getGraphName(), inputGraph.getMetadataGraphName(), getCleanConnection());
 			Double updatedScore = getGraphScore(inputGraph.getGraphName(), inputGraph.getMetadataGraphName(), getDirtyConnection());
 
 			updatePublisherScore(inputGraph.getGraphName(),
 					inputGraph.getMetadataGraphName(),
 					updatedScore - outdatedScore,
 					0);
 		} catch (QualityAssessmentException e) {
 			throw new TransformerException(e);
 		} catch (DatabaseException e) {
 			throw new TransformerException(e);
 		} finally {
 			closeCleanConnection();
 			closeDirtyConnection();
 		}
 	}
 
 	/**
 	 * Takes all the graphs from clean database that share the publisher with the input graph. Adds delta (score of deltaCount other graphs) into the average.
 	 */
 	private void updatePublisherScore (String graph,
 			String metadataGraph,
 			Double delta,
 			Integer deltaCount) throws TransformerException {
 		try
 		{
 			final String publisher = getGraphPublisher(graph, metadataGraph);
 
 			if (publisher != null) {
 				try {
 					final String computeSumUpdated = String.format(Locale.ROOT, computeSumUpdatedQueryFormat, publisher);
 					final String computeCountUpdated = String.format(Locale.ROOT, computeCountUpdatedQueryFormat, publisher);
 
 					WrappedResultSet rsSum = getCleanConnection().executeSelect(computeSumUpdated);
 					WrappedResultSet rsCount = getCleanConnection().executeSelect(computeCountUpdated);
 
 					Double score;
 
 					if (rsSum.next() && rsCount.next()) {
 						//Clean DB: the graph is also present in clean DB (only its score may change for the copy in dirty DB) => no division by zero
 						//Dirty DB: deltaCount == 1 => no division by zero

					    Double sum = rsSum.getDouble(1) != null ? rsSum.getDouble(1) : 0F;
					    Double count = rsCount.getDouble(1) != null ? rsCount.getDouble(1) : 0F;
						if (sum == null || count == null || count + deltaCount == 0)
 						{
 							//Very special cases => abort assignment of score for the current publisher
 							return;
 						}

						score = (sum + delta) / (count + deltaCount);
 
 						LOG.info("Publisher <" + publisher + "> scored " + score + ".");
 					} else {
 						throw new QualityAssessmentException("Publisher has no score.");
 					}
 
 					getCleanConnection().adjustTransactionLevel(EnumLogLevel.TRANSACTION_LEVEL, false);
 
 					final String dropOutdated = String.format(Locale.ROOT, dropOutdatedQueryFormat, ConfigLoader.getConfig().getQualityAssessmentGroup().getAggregatedPublisherScoreGraphURI(), publisher);
 					getCleanConnection().execute(dropOutdated);
 
 					final String storeUpdated = String.format(Locale.ROOT, storeUpdatedQueryFormat, ConfigLoader.getConfig().getQualityAssessmentGroup().getAggregatedPublisherScoreGraphURI(), publisher, score);
 					getCleanConnection().execute(storeUpdated);
 
 					getCleanConnection().commit();
 				} catch (DatabaseException e) {
 					throw new TransformerException(e);
 				} catch (SQLException e) {
 					throw new TransformerException(e);
 				}
 			}
 		} catch (QualityAssessmentException e) {
 			throw new TransformerException(e);
 		}
 	}
 
 	@Override
 	public void shutdown() throws TransformerException {
 	}
 
 	private VirtuosoConnectionWrapper cleanConnection;
 	private VirtuosoConnectionWrapper dirtyConnection;
 
 	private VirtuosoConnectionWrapper getCleanConnection () throws DatabaseException {
         if (cleanConnection == null) {
         	cleanConnection = VirtuosoConnectionWrapper.createConnection(context.getCleanDatabaseCredentials());
        	}
 		return cleanConnection;
 	}
 
 	private void closeCleanConnection() {
 		try {
 			if (cleanConnection != null) {
 				cleanConnection.close();
 			}
 		} catch (DatabaseException e) {
 		} finally {
 			cleanConnection = null;
 		}
 	}
 
 	private VirtuosoConnectionWrapper getDirtyConnection () throws DatabaseException {
         if (dirtyConnection == null) {
         	dirtyConnection = VirtuosoConnectionWrapper.createConnection(context.getDirtyDatabaseCredentials());
        	}
 		return dirtyConnection;
 	}
 
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
 
 	/**
 	 * @param graph
 	 * @param metadataGraph
 	 * @return The publisher of the given graph.
 	 * @throws QualityAssessmentException
 	 */
 	private String getGraphPublisher (final String graph, final String metadataGraph) throws QualityAssessmentException {
 		final String query = String.format(Locale.ROOT, graphPublisherQueryFormat, metadataGraph, graph);
 		WrappedResultSet results = null;
 		String publisher = null;
 
 		try
 		{
 			results = getDirtyConnection().executeSelect(query);
 
 			if (results.next()) {
 				publisher = results.getString("publisher");
 			}
 		} catch (DatabaseException e) {
 			throw new QualityAssessmentException(e);
 		} catch (SQLException e) {
 			throw new QualityAssessmentException(e);
 		} finally {
 			if (results != null) {
 				results.closeQuietly();
 			}
 		}
 
 		return publisher;
 	}
 
 	/**
 	 * @param graph
 	 * @param metadataGraph
 	 * @return Score of the given graph.
 	 * @throws QualityAssessmentException
 	 */
 	private Double getGraphScore (final String graph,
 			final String metadataGraph,
 			final VirtuosoConnectionWrapper connection) throws QualityAssessmentException {
 		final String query = String.format(Locale.ROOT, graphScoreQueryFormat, metadataGraph, graph);
 		WrappedResultSet results = null;
 		Double score = 0.0;
 
 		try
 		{
 			results = connection.executeSelect(query);
 
 			if (results.next()) {
 				score = results.getDouble("score");
 			}
 		} catch (DatabaseException e) {
 			throw new QualityAssessmentException(e);
 		} catch (SQLException e) {
 			throw new QualityAssessmentException(e);
 		} finally {
 			if (results != null) {
 				results.closeQuietly();
 			}
 		}
 
 		return score;
 	}
 }
