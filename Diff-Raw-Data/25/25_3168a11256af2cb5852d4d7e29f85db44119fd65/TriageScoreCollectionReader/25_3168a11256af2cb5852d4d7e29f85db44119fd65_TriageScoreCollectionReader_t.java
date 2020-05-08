 package edu.isi.bmkeg.skm.triage.cleartk.cr;
 
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import org.apache.log4j.Logger;
 import org.apache.uima.UimaContext;
 import org.apache.uima.collection.CollectionException;
 import org.apache.uima.collection.CollectionReader;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.resource.ResourceInitializationException;
 import org.apache.uima.resource.metadata.TypeSystemDescription;
 import org.apache.uima.util.Progress;
 import org.uimafit.component.JCasCollectionReader_ImplBase;
 import org.uimafit.descriptor.ConfigurationParameter;
 import org.uimafit.factory.CollectionReaderFactory;
 import org.uimafit.factory.ConfigurationParameterFactory;
 import org.uimafit.factory.TypeSystemDescriptionFactory;
 
 import edu.isi.bmkeg.skm.triage.controller.TriageEngine;
 import edu.isi.bmkeg.skm.triage.model.TriageCode;
 import edu.isi.bmkeg.triage.uimaTypes.TriageScore;
 
 /**
  * This Collection Reader runs over every citation in a given corpus to provide
  * access to the abstract, the full-text of the document or a predefined section
  * of the document. We want to optimize this interaction for speed, so we run a
  * manual query over the underlying database involving a minimal subset of
  * tables.
  * 
  * @author burns
  * 
  */
 public class TriageScoreCollectionReader extends JCasCollectionReader_ImplBase {
 
 	private static Logger logger = Logger.getLogger(TriageScoreCollectionReader.class);
 
 	public static final String START_AT = ConfigurationParameterFactory
 			.createConfigurationParameterName(TriageScoreCollectionReader.class,
 					"startAt");
 	@ConfigurationParameter(mandatory = false, description = "The starting point of the read.")
 	protected int startAt;
 
 	public static final String END_AT = ConfigurationParameterFactory
 			.createConfigurationParameterName(TriageScoreCollectionReader.class,
 					"endAt");
 	@ConfigurationParameter(mandatory = false, description = "The ending point of the read.")
 	protected int endAt;
 
 	public static final String TRIAGE_CORPUS_NAME = ConfigurationParameterFactory
 			.createConfigurationParameterName(TriageScoreCollectionReader.class,
 					"triageCorpusName");
 	@ConfigurationParameter(mandatory = false, description = "The name of the triage corpus to be read")
 	protected String triageCorpusName;
 
 	public static final String TARGET_CORPUS_NAME = ConfigurationParameterFactory
 			.createConfigurationParameterName(TriageScoreCollectionReader.class,
 					"targetCorpusName");
 	@ConfigurationParameter(mandatory = false, description = "The name of the target corpus to be read")
 	protected String targetCorpusName;
 
 	public static final String LOGIN = ConfigurationParameterFactory
 			.createConfigurationParameterName(TriageScoreCollectionReader.class,
 					"login");
 	@ConfigurationParameter(mandatory = false, description = "Login for the Digital Library")
 	protected String login;
 
 	public static final String PASSWORD = ConfigurationParameterFactory
 			.createConfigurationParameterName(TriageScoreCollectionReader.class,
 					"password");
 	@ConfigurationParameter(mandatory = false, description = "Password for the Digital Library")
 	protected String password;
 
 	public static final String DB_URL = ConfigurationParameterFactory
 			.createConfigurationParameterName(TriageScoreCollectionReader.class,
 					"dbUrl");
 	@ConfigurationParameter(mandatory = false, description = "The Digital Library URL")
 	protected String dbUrl;
 	
 	public static final String SKIP_UNKNOWNS = ConfigurationParameterFactory
 			.createConfigurationParameterName(TriageScoreCollectionReader.class,
 					"skipUnknowns");
 	@ConfigurationParameter(mandatory = false, description = "The Digital Library URL", 
 			defaultValue = "false")
 	protected boolean skipUnknowns;
 	
 	protected ResultSet rs;
 
	private boolean eof = false;
	
 	protected long startTime, endTime;
 
 	protected int pos = 0, count = 0;
 	
 	protected TriageEngine triageEngine = null;
 	
 	public static CollectionReader load(
 			String triageCorpusName, String targetCorpusName,
 			String login,
 			String password, String dbName)
 			throws ResourceInitializationException {
 
 		return load(triageCorpusName, targetCorpusName, login, password, dbName, false);
 		
 	}
 
 	public static CollectionReader load(
 			String triageCorpusName, String targetCorpusName,
 			String login,
 			String password, String dbName,
 			boolean skipUnknowns)
 			throws ResourceInitializationException {
 
 		TypeSystemDescription typeSystem = TypeSystemDescriptionFactory
 				.createTypeSystemDescription("uimaTypes.triage",
 						"edu.isi.bmkeg.skm.cleartk.TypeSystem");
 		
 		CollectionReader reader = 
 				CollectionReaderFactory.createCollectionReader(
 				TriageScoreCollectionReader.class, typeSystem, 
 				TRIAGE_CORPUS_NAME, triageCorpusName,
 				TARGET_CORPUS_NAME, targetCorpusName,
 				LOGIN, login, 
 				PASSWORD, password, 
 				DB_URL, dbName,
 				SKIP_UNKNOWNS, skipUnknowns);
 		
 		return reader;
 	
 	}
 
 	@Override
 	public void initialize(UimaContext context) throws ResourceInitializationException {
 
 		try {
 
 			triageEngine = new TriageEngine();
 			triageEngine.initializeVpdmfDao(login, password, dbUrl);				
 			
 			// Query constructed with SqlQueryBuilder based on the TriagedArticle view.
 			// (FTD added manually).
 			String sql = "SELECT DISTINCT LiteratureCitation_0__ArticleCitation." + 
 					"pmid,FTD_0__FTD.text,TriageScore_0__TriageScore.inOutCode, TriageScore_0__TriageScore.vpdmfId " + 
 					"FROM FTD AS FTD_0__FTD, LiteratureCitation AS LiteratureCitation_0__LiteratureCitation, " + 
 					"ArticleCitation AS LiteratureCitation_0__ArticleCitation, TriageCorpus AS TriageCorpus_0__TriageCorpus, " +
 					"Corpus AS TriageCorpus_0__Corpus, Corpus AS TargetCorpus_0__Corpus, TriageScore AS TriageScore_0__TriageScore " + 
 					" WHERE TriageCorpus_0__Corpus.name = '" + triageCorpusName + "' AND " + 
 					" TargetCorpus_0__Corpus.name = '" + targetCorpusName +  "' AND " +
 					" LiteratureCitation_0__LiteratureCitation.vpdmfId=TriageScore_0__TriageScore.citation_id AND " +
 					" TriageCorpus_0__TriageCorpus.vpdmfId=TriageScore_0__TriageScore.triageCorpus_id AND " + 
 					" TriageCorpus_0__TriageCorpus.vpdmfId=TriageCorpus_0__Corpus.vpdmfId AND " + 
 					" TargetCorpus_0__Corpus.vpdmfId=TriageScore_0__TriageScore.targetCorpus_id AND " +
 					" LiteratureCitation_0__LiteratureCitation.vpdmfId=LiteratureCitation_0__ArticleCitation.vpdmfId AND " + 
 					" FTD_0__FTD.vpdmfId=LiteratureCitation_0__LiteratureCitation.fullText_id";
 			
 			triageEngine.getCitDao().getCoreDao().getCe().connectToDB();
 			
 			this.rs = triageEngine.getCitDao().getCoreDao().getCe().executeRawSqlQuery(sql);
 			
 			this.pos = 0;
 			
 			this.startTime = System.currentTimeMillis();
 			
			// Caches the first row so we can cache eof to compute the result of hasNext()			
			moveNext();
 			
 		} catch (Exception e) {
 
 			throw new ResourceInitializationException(e);
 
 		}
 
 	}
 
 	/**
 	 * @see com.ibm.uima.collection.CollectionReader#getNext(com.ibm.uima.cas.CAS)
 	 */
 	public void getNext(JCas jcas) throws IOException, CollectionException {
 
 		try {
 						
 			long vpdmfId = this.rs.getLong("vpdmfId");
 			String code = this.rs.getString("inOutCode");
 			String text = this.rs.getString("text");
 	
 			if( text == null )
 			    jcas.setDocumentText("");
 			else
 				jcas.setDocumentText( text );
 
 			TriageScore doc = new TriageScore(jcas);
 		    doc.setVpdmfId(vpdmfId);
 		    doc.setInOutCode(code);
 		    doc.addToIndexes(jcas);
 
 		    moveNext();
 		    
 		} catch (Exception e) {
 
 			throw new CollectionException(e);
 
 		}
 
 	}
 
 	/**
 	 * @see com.ibm.uima.arg0collection.base_cpm.BaseCollectionReader#hasNext()
 	 */
 	public boolean hasNext() throws IOException, CollectionException {
		return !eof;
 
 	}
 		
 	public void close() throws IOException {
 		try {
 			triageEngine.getCitDao().getCoreDao().getCe().closeDbConnection();
 		} catch (Exception e) {
 			throw new IOException(e);
 		}
 	}
 
 	private void moveNext() throws SQLException {
		eof = !rs.next();
		if (skipUnknowns) {
			while (!eof) {
 				String code = this.rs.getString("inOutCode");
 				if (TriageCode.IN.equals(code) || TriageCode.OUT.equals(code) ) break;
				eof = !rs.next();
 			}
 		} 
 	}
 	
 	protected void error(String message) {
 		logger.error(message);
 	}
 
 	@SuppressWarnings("unused")
 	private void warn(String message) {
 		logger.warn(message);
 	}
 
 	@SuppressWarnings("unused")
 	private void debug(String message) {
 		logger.error(message);
 	}
 
 	public Progress[] getProgress() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
