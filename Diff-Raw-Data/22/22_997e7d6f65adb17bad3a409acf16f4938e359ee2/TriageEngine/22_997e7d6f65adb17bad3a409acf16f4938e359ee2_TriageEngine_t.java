 package edu.isi.bmkeg.skm.triage.controller;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import edu.isi.bmkeg.digitalLibrary.controller.DigitalLibraryEngine;
 import edu.isi.bmkeg.digitalLibrary.model.citations.ArticleCitation;
 import edu.isi.bmkeg.digitalLibrary.model.citations.Journal;
 import edu.isi.bmkeg.digitalLibrary.utils.pubmed.EFetcher;
 import edu.isi.bmkeg.skm.triage.dao.TriageDaoEx;
 import edu.isi.bmkeg.skm.triage.dao.vpdmf.TriageDaoExImpl;
 import edu.isi.bmkeg.skm.triage.model.TriageCode;
 import edu.isi.bmkeg.triage.dao.TriageDao;
 import edu.isi.bmkeg.triage.dao.impl.TriageDaoImpl;
 import edu.isi.bmkeg.triage.model.TriageCorpus;
 import edu.isi.bmkeg.triage.model.TriageScore;
 import edu.isi.bmkeg.vpdmf.dao.CoreDao;
 
 
 public class TriageEngine extends DigitalLibraryEngine {
 
 	private static Logger logger = Logger.getLogger(TriageEngine.class);
 
 	private TriageDaoEx exTriageDao;
 
 	private TriageDao triageDao;
 	
 	public TriageEngine() throws Exception {
 		super();
 	}
 
 	public TriageEngine(File pdfRuleFile) throws Exception {
 		super(pdfRuleFile);
 	}
 	
 	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 	// Getters and Setters
 	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 	public TriageDaoEx getExTriageDao() {
 		return exTriageDao;
 	}
 
 	public void setExTriageDao(TriageDaoEx triageDao) {
 		this.exTriageDao = triageDao;
 	}
 
 	public TriageDao getTriageDao() {
 		return triageDao;
 	}
 
 	public void setTriageDao(TriageDao triageDao) {
 		this.triageDao = triageDao;
 	}
 	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 	// VPDMf functions
 	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 
 	/**
 	 * Builds dao objects to input and output data to a VPDMf store.
 	 */
 	public void initializeVpdmfDao(String login, String password, String dbName)
 			throws Exception {
 
 		super.initializeVpdmfDao(login, password, dbName);
 
 		CoreDao coreDao = this.getFtdDao().getCoreDao();
 
 		this.exTriageDao = new TriageDaoExImpl(coreDao);
 		this.triageDao = new TriageDaoImpl(coreDao);
 
 	}
 
 	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 
 	public void createEmptyTriageCorpus(String name, 
 			String queryString,
 			String masterCorpus) throws Exception {
 
 		TriageCorpus tc = new TriageCorpus();
 		tc.setName(name);
 		tc.setCorpusCreationQuery(queryString);
 
 		this.exTriageDao.insertArticleTriageCorpus(tc);
 
 	}
 
 	public void createEmptyTriageCorpus(String name, 
 			String masterCorpus) throws Exception {
 
 		TriageCorpus tc = new TriageCorpus();
 		tc.setName(name);
 		
 		this.exTriageDao.insertArticleTriageCorpus(tc);
 
 	}
 	
 	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 	
 	public TriageCorpus findTriageCorpusByName(String name) throws Exception {
 		
 		return this.exTriageDao.findTriageCorpusByName(name);
 	
 	}
 	
 	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 
 	public Set<Integer> loadArticlesFromESearch(String queryString,
 			Set<Integer> pmidsToSkip) throws Exception {
 
 		EFetcher f = new EFetcher(queryString, pmidsToSkip);
 		while (f.hasNext()) {
 			ArticleCitation a = f.next();
 
 			if (a == null)
 				continue;
 
 			if (a.getVolume() == null || a.getVolume().length() == 0) {
 				a.setVolume("-");
 			}
 			if (a.getPages() == null || a.getPages().length() == 0) {
 				a.setPages("-");
 			}
 
 			String jStr = a.getJournal().getAbbr();
 			if (!this.getjLookup().containsKey(jStr)) {
 				logger.info("'" + jStr
 						+ "' not found in lookup, skipping PMID=" + a.getPmid());
 				continue;
 			}
 
 			Journal j = this.getjLookup().get(jStr);
 			a.setJournal(j);
 
 			try {
 				logger.info("inserting article, PMID=" + a.getPmid());
 				getCitDao().insertArticleCitation(a);
 			} catch (Exception e) {
 				logger.info("article insert failed, PMID=" + a.getPmid());
 				e.printStackTrace();
 			}
 
 		}
 
 		return f.getAllIds();
 
 	}
 
 	/**
 	 * Read triage-encoded files:
 	 * 
 	 * 12345673 OUT or '-' => code = -1 12345670 UNCLASSIFIED or '' => code = 0
 	 * 12345671 MAYBE or '?' => code = 1 12345672 IN or '+' => code = 2
 	 * 
 	 * @param pmidFile
 	 * @return
 	 * @throws Exception
 	 */
 	public Map<Integer, String> loadCodesFromPmidFile(File pmidFile)
 			throws Exception {
 
 		// Load file line by line as Map<String, Map<String,String>>
 		Map<Integer, String> idMap = new HashMap<Integer, String>();
 		FileInputStream fstream = new FileInputStream(pmidFile);
 
 		// Get the object of DataInputStream
 		DataInputStream in = new DataInputStream(fstream);
 		BufferedReader br = new BufferedReader(new InputStreamReader(in));
 
 		int i = 1;
 		String strLine;
 		List<String> missedLines = new ArrayList<String>();
 		while ((strLine = br.readLine()) != null) {
 			String[] splitLn = strLine.split("\\t+");
 			String pmid = splitLn[0];
 
 			if (splitLn.length == 1) {
 				idMap.put(new Integer(pmid), TriageCode.UNCLASSIFIED);
 				continue;
 			}
 
 			String s = splitLn[1];
 			if (s.equals("OUT") || s.equals("-")) {
 
 				idMap.put(new Integer(pmid), TriageCode.OUT);
 
 			} else if (s.equals("IN") || s.equals("+")) {
 
 				idMap.put(new Integer(pmid), TriageCode.IN);
 
 			} else if (s.equals("UNCLASSIFIED")) {
 
 				idMap.put(new Integer(pmid), TriageCode.UNCLASSIFIED);
 
 			} else if (s.equals("MAYBE") || s.equals("?")) {
 
 				idMap.put(new Integer(pmid), TriageCode.MAYBE);
 
 			} else {
 				missedLines.add(strLine);
 
 			}
 
 		}
 
 		in.close();
 
 		return idMap;
 
 	}
 	
 	public Set<Integer> loadCodedPmidsFromFile(File triageCodesFile, String code) throws Exception {
 
 		Set<Integer> pmids = new HashSet<Integer>();
 		
 		Map<Integer, String> pmidCodes = this.loadCodesFromPmidFile(triageCodesFile);
 		Iterator<Integer> pmidIt = pmidCodes.keySet().iterator();
 		while( pmidIt.hasNext() ) {
 			Integer pmid = pmidIt.next();
 			if( pmidCodes.get(pmid).equals(code) ) {
 				pmids.add(pmid);
 			}
 		}
 		
 		return pmids;
 		
 	}
 
 	public void populateArticleTriageCorpus(String triageCorpus, 
 			String targetCorpus, 
 			Map<Integer, String> inOutCodes)
 			throws Exception {
 
 		this.exTriageDao.addTriageDocumentsToCorpus(triageCorpus, targetCorpus, inOutCodes);
 
 	}
 
 	public void updateInScore(long vpdmfId, float inScore) throws Exception {
 		
		TriageScore td =  this.triageDao.findTriagedArticleById(vpdmfId);
 	
 		if (td == null) {
 			logger.warn("Failed to find TriagedDocument with id:" + vpdmfId);
 			return;
 		} 
 		
 		td.setInScore(inScore);
 		
		getExTriageDao().updateTriagedArticle(td);
 
 	}
 
 
 }
