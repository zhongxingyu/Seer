 package edu.isi.bmkeg.skm.triage.dao.vpdmf;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 
 import edu.isi.bmkeg.skm.triage.dao.TriageDaoEx;
 import edu.isi.bmkeg.triage.model.TriageCorpus;
 import edu.isi.bmkeg.triage.model.TriageScore;
 import edu.isi.bmkeg.vpdmf.controller.queryEngineTools.ChangeEngine;
 import edu.isi.bmkeg.vpdmf.controller.queryEngineTools.VPDMfChangeEngineInterface;
 import edu.isi.bmkeg.vpdmf.dao.CoreDao;
 import edu.isi.bmkeg.vpdmf.model.definitions.VPDMf;
 import edu.isi.bmkeg.vpdmf.model.definitions.ViewDefinition;
 import edu.isi.bmkeg.vpdmf.model.instances.AttributeInstance;
 import edu.isi.bmkeg.vpdmf.model.instances.LightViewInstance;
 import edu.isi.bmkeg.vpdmf.model.instances.ViewBasedObjectGraph;
 import edu.isi.bmkeg.vpdmf.model.instances.ViewInstance;
 
 @Repository
 public class TriageDaoExImpl implements TriageDaoEx {
 
 	private static Logger logger = Logger.getLogger(TriageDaoExImpl.class);
 
 	@Autowired
 	private CoreDao coreDao;
 
 	// ~~~~~~~~~~~~
 	// Constructors
 	// ~~~~~~~~~~~~
 	public TriageDaoExImpl() {}
 
 	public TriageDaoExImpl(CoreDao coreDao) {
 		this.coreDao = coreDao;
 	}
 
 	// ~~~~~~~~~~~~~~~~~~~
 	// Getters and Setters
 	// ~~~~~~~~~~~~~~~~~~~
 	public void setCoreDao(CoreDao coreDao) {
 		this.coreDao = coreDao;
 	}
 
 	public CoreDao getCoreDao() {
 		return coreDao;
 	}
 
 	private VPDMfChangeEngineInterface getCe() {
 		return coreDao.getCe();
 	}
 
 	private Map<String, ViewBasedObjectGraph> generateVbogs() throws Exception {
 		return coreDao.generateVbogs();
 	}
 
 	private VPDMf getTop() {
 		return coreDao.getTop();
 	}
 
 	// ~~~~~~~~~~~~~~~
 	// Count functions
 	// ~~~~~~~~~~~~~~~
 
 	@Override
 	public int countTriagedArticlesInCorpus(String corpusName) throws Exception {
 
 		int count = 0;
 	
 		try {
 	
 			getCe().connectToDB();
 			getCe().turnOffAutoCommit();
 			
 			ViewDefinition vd = getTop().getViews().get("TriagedArticle");
 			ViewInstance vi = new ViewInstance(vd);
 			AttributeInstance ai = vi.readAttributeInstance(
 					"]TriageCorpus|TriageCorpus.name", 0);
 			ai.writeValueString(corpusName);
 			
 			count = getCe().executeCountQuery(vi);
 		
 		} finally {
 			getCe().closeDbConnection();
 		}
 
 		return count;
 	
 	}
 	
 	// ~~~~~~~~~~~~~~~~~~~
 	// Insert Functions
 	// ~~~~~~~~~~~~~~~~~~~
 	@Override
 	public void insertArticleTriageCorpus(TriageCorpus tc) throws Exception {
 
 		getCoreDao().insertVBOG(tc, "ArticleTriageCorpus");
 
 	}
 
 	// ~~~~~~~~~~~~~~~~~~~
 	// Update Functions
 	// ~~~~~~~~~~~~~~~~~~~
 
 	@Override
 	public long updateTriageScore(TriageScore td)  throws Exception {
 		return getCoreDao().update(td,"TriageScore");
 	}
 	
 	// ~~~~~~~~~~~~~~~~~~~
 	// Delete Functions
 	// ~~~~~~~~~~~~~~~~~~~
 
 	// ~~~~~~~~~~~~~~~~~~~~
 	// Find by id Functions
 	// ~~~~~~~~~~~~~~~~~~~~
 
 	@Override
 	public TriageCorpus findTriageCorpusByName(String name) throws Exception {
 
 		return (TriageCorpus) getCoreDao().findVBOGByAttributeValue(
 				"TriageCorpus", "Corpus", "Corpus", "name", name);
 
 	}
 
 	@Override
 	public TriageScore findTriageScoreById(long id) throws Exception {
 
 		return (TriageScore) this.coreDao.findVBOGById(id, "TriageScore");
 		
 	}
 	
 	// ~~~~~~~~~~~~~~~~~~~~
 	// Retrieve functions
 	// ~~~~~~~~~~~~~~~~~~~~
 
 	// ~~~~~~~~~~~~~~
 	// List functions
 	// ~~~~~~~~~~~~~~
 	@Override
 	public List<LightViewInstance> listTriageArticlesByTriageCorpus(
 			String corpusName) throws Exception {
 		
 		List<LightViewInstance> l = null;
 		try {
 			getCe().connectToDB();
 			l = this.coreDao.goGetLightViewList("TriagedArticle",
 					"]TriageCorpus|TriageCorpus.name", corpusName);
 		} finally {
 			getCe().closeDbConnection();
 		}
 		return l;
 	}
 
 	// ~~~~~~~~~~~~~~~~~~~~
 	// Add x to y functions
 	// ~~~~~~~~~~~~~~~~~~~~
 	@Override
 	public void addTriageDocumentsToCorpus(String triageCorpus, 
 			String targetCorpus,
 			Map<Integer, String> pmidCodes) throws Exception {
 
 		try {
 
 			int count = 0;
 			long t = System.currentTimeMillis();
 			
 			ChangeEngine ce = (ChangeEngine) this.coreDao.getCe();
 			VPDMf top = ce.readTop();
 
 			ce.connectToDB();
 			ce.turnOffAutoCommit();
 
 			ViewDefinition vd = top.getViews().get("TriagedArticle");
 			ViewDefinition articleVd = top.getViews().get("ArticleCitation");
 
 			List<Integer> pmids = new ArrayList<Integer>(pmidCodes.keySet());
 			Collections.sort(pmids);
 			Iterator<Integer> it = pmids.iterator();
 			while (it.hasNext()) {
 				Integer pmid = it.next();
 
 				ViewInstance qvi = new ViewInstance(articleVd);
 				AttributeInstance ai = qvi.readAttributeInstance(
 						"]LiteratureCitation|ArticleCitation.pmid", 0);
 				ai.writeValueString(pmid + "");
 				List<LightViewInstance> l = ce.executeListQuery(qvi);
 				if( l.size() == 0 ) {
 					continue;
 				} else if( l.size() > 1 ) {
 					throw new Exception("PMID " + pmid + " ambiguous.");
 				}
 				LightViewInstance lvi = l.get(0);
 					
 				ViewInstance vi = new ViewInstance(vd);
 
 				ai = vi.readAttributeInstance(
 						"]TriageCorpus|Corpus.name", 0);
 				ai.writeValueString(triageCorpus);
 
 				ai = vi.readAttributeInstance(
 						"]TargetCorpus|Corpus.name", 0);
 				ai.writeValueString(targetCorpus);
 				
 				String code = pmidCodes.get(pmid);
 				
 				ai = vi.readAttributeInstance(
 						"]LiteratureCitation|ViewTable.vpdmfLabel", 0);
 				ai.writeValueString(lvi.getVpdmfLabel());
 
 				ai = vi.readAttributeInstance(
 						"]LiteratureCitation|ViewTable.vpdmfId", 0);
 				ai.writeValueString(lvi.getVpdmfId() + "");
 				
 				// May may need to delete the existing data in the database.
 				List<LightViewInstance> lviList = getCe().executeListQuery(vi);
 				if( lviList.size() > 0 ) {
 					
 					//
 					// REMOVE EXISTING DATA FROM THE TRIAGE SCORE TABLE.
 					// NEED TO UPDATE THE DELETION FUNCTIONS WITHIN VPDMf
 					//
 					String sql = "DELETE ts.*, vt.* " +
 								 "FROM TriageScore AS ts, " + 
 								 " ViewTable AS vt, " +
 								 " LiteratureCitation AS litcit, " +
 								 " ArticleCitation AS artcit, " +
 								 " Corpus AS targetc, " +
 								 " Corpus AS triagec " +
 								 "WHERE vt.vpdmfId = ts.vpdmfId " +
 								 "  AND ts.citation_id = litcit.vpdmfId " +		
 								 "  AND litcit.vpdmfId = artcit.vpdmfId " +		
 								 "  AND artcit.pmid = '" + pmid + "'" +
 								 "  AND ts.targetCorpus_id = targetc.vpdmfId " +		
 								 "  AND targetc.name = '" + targetCorpus + "'" +
 								 "  AND ts.triageCorpus_id = triagec.vpdmfId " +
 								 "  AND triagec.name = '" + triageCorpus + "';";					
 					
 					this.getCoreDao().getCe().executeRawUpdateQuery(sql);
 					
 					this.coreDao.getCe().prettyPrintSQL(sql);
 					
 				}
 
 				count++;
 
 				if( (count % 50 == 0) )
 					logger.info("Updated " + count + " / " + pmids.size() 
 							+ " documents in " + 
 							(System.currentTimeMillis() - t) / 1000.0 + " s");
 				
 				ai = vi.readAttributeInstance(
 						"]TriageScore|TriageScore.inOutCode", 0);
 				ai.setValue(code);
 
 				ai = vi.readAttributeInstance(
 						"]TriageScore|TriageScore.inScore", 0);
 				ai.writeValueString("0");
 				
 				getCe().executeInsertQuery(vi);
 
 			}
 
 			ce.commitTransaction();
 
 			long deltaT = System.currentTimeMillis() - t;
 			logger.info("Added " + count + " entries in " + deltaT / 1000.0 + " s\n");
 
 		} catch (Exception e) {
 
 			e.printStackTrace();
 
 		} finally {
 
 			getCe().closeDbConnection();
 
 		}
 
 	}
 
 
 
 }
