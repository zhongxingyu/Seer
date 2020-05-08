 package edu.isi.bmkeg.digitalLibrary.controller;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.log4j.Logger;
 
 import edu.isi.bmkeg.digitalLibrary.controller.medline.VpdmfMedlineHandler;
 import edu.isi.bmkeg.digitalLibrary.dao.CitationsDao;
 import edu.isi.bmkeg.digitalLibrary.dao.DigitalLibraryDao;
 import edu.isi.bmkeg.digitalLibrary.dao.impl.DigitalLibraryDaoImpl;
 import edu.isi.bmkeg.digitalLibrary.dao.vpdmf.VpdmfCitationsDao;
 import edu.isi.bmkeg.digitalLibrary.model.citations.ArticleCitation;
 import edu.isi.bmkeg.digitalLibrary.model.citations.Corpus;
 import edu.isi.bmkeg.digitalLibrary.model.citations.ID;
 import edu.isi.bmkeg.digitalLibrary.model.citations.Journal;
 import edu.isi.bmkeg.digitalLibrary.utils.FileLookupPersistentObject;
 import edu.isi.bmkeg.digitalLibrary.utils.JournalLookupPersistentObject;
 import edu.isi.bmkeg.digitalLibrary.utils.pubmed.EFetcher;
 import edu.isi.bmkeg.ftd.dao.FtdDao;
 import edu.isi.bmkeg.ftd.dao.impl.FtdDaoImpl;
 import edu.isi.bmkeg.ftd.model.FTD;
 import edu.isi.bmkeg.lapdf.controller.LapdfVpdmfEngine;
 import edu.isi.bmkeg.lapdf.model.LapdfDocument;
 import edu.isi.bmkeg.utils.Converters;
 import edu.isi.bmkeg.vpdmf.dao.CoreDao;
 
 public class DigitalLibraryEngine extends LapdfVpdmfEngine {
 
 	private static Logger logger = Logger.getLogger(DigitalLibraryEngine.class);
 
 	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 
 	private CitationsDao citDao;
 	private FtdDao ftdDao;
 	private DigitalLibraryDao digLibDao;
 	
 	private Map<String, Journal> jLookup;
 	private Map<String, File> fLookup;
 
 	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 
 	public DigitalLibraryEngine() throws Exception {
 		super();
 	}
 
 	public DigitalLibraryEngine(File pdfRuleFile) throws Exception {
 		super(pdfRuleFile);
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
 		this.citDao = new VpdmfCitationsDao(coreDao);
 		this.ftdDao = new FtdDaoImpl(coreDao);
 		this.digLibDao = new DigitalLibraryDaoImpl(coreDao);
 
 		// Build lookup table from journals on disk (persistent object)
 		JournalLookupPersistentObject jLookupPObj = new JournalLookupPersistentObject();
 		this.setjLookup(jLookupPObj.readJLookup(dbName, login, password));
 
 	}
 
 	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 
 	public CitationsDao getCitDao() {
 		return citDao;
 	}
 
 	public void setCitDao(CitationsDao citDao) {
 		this.citDao = citDao;
 	}
 
 	public Map<String, Journal> getjLookup() {
 		return jLookup;
 	}
 
 	public void setjLookup(Map<String, Journal> jLookup) {
 		this.jLookup = jLookup;
 	}
 
 	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 	// Loading data.
 	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 
 	public Map<Integer, Map<String, String>> loadIdMapFromPmidFile(File pmidFile)
 			throws Exception {
 
 		// Load file line by line as Map<String, Map<String,String>>
 		Map<Integer, Map<String, String>> idMap = new HashMap<Integer, Map<String, String>>();
 		FileInputStream fstream = new FileInputStream(pmidFile);
 
 		// Get the object of DataInputStream
 		DataInputStream in = new DataInputStream(fstream);
 		BufferedReader br = new BufferedReader(new InputStreamReader(in));
 
 		String strLine;
 		List<String> missedLines = new ArrayList<String>();
 		while ((strLine = br.readLine()) != null) {
 			String[] splitLn = strLine.split("\\t+");
 			String pmid = splitLn[0];
 			Map<String, String> otherIds = new HashMap<String, String>();
 			for (int i = 1; i < splitLn.length; i++) {
 				String[] splitId = splitLn[i].split(":");
 				otherIds.put(splitId[0], splitId[1]);
 			}
 			try {
 				idMap.put(new Integer(pmid), otherIds);
 			} catch (NumberFormatException e) {
 				missedLines.add(strLine);
 			}
 		}
 		in.close();
 
 		return idMap;
 
 	}
 
 	public Set<Integer> loadIdSetFromPmidFile(File pmidFile) throws Exception {
 
 		// Load file line by line as Map<String, Map<String,String>>
 		Set<Integer> idSet = new HashSet<Integer>();
 
 		if (pmidFile == null)
 			return idSet;
 
 		FileInputStream fstream = new FileInputStream(pmidFile);
 
 		// Get the object of DataInputStream
 		DataInputStream in = new DataInputStream(fstream);
 		BufferedReader br = new BufferedReader(new InputStreamReader(in));
 
 		String strLine;
 		Pattern p = Pattern.compile("^\\d+$");
 		List<String> missedLines = new ArrayList<String>();
 		while ((strLine = br.readLine()) != null) {
 			String[] splitLn = strLine.split("\\t+");
 			String pmid = splitLn[0];
 			Matcher m = p.matcher(pmid);
 			if (!m.find()) {
 				continue;
 			}
 			idSet.add(new Integer(pmid));
 		}
 		in.close();
 
 		return idSet;
 
 	}
 
 	public Map<Integer,Long> insertPmidPdfFileOrDir(File pdfOrDir) throws Exception {
 
 		Map<Integer,Long> pmidLookup = new HashMap<Integer,Long>();
 
 		if (pdfOrDir.isDirectory()) {
 
 			Pattern p = Pattern.compile("\\.pdf$");
 			List<File> pdfList = new ArrayList<File>(
 					Converters.recursivelyListFiles(pdfOrDir, p).values()
 					);
 			Collections.sort(pdfList);
 			Iterator<File> pdfIt = pdfList.iterator();
 			while (pdfIt.hasNext()) {
 				File pdf = pdfIt.next();
 
 				try {
 					pmidLookup.putAll(insertCodedPdfFile(pdf, "pmid"));
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 
 			}
 
 		} else {
 
 			pmidLookup.putAll(insertCodedPdfFile(pdfOrDir, "pmid"));
 
 		}
 
 		return pmidLookup;
 
 	}
 
 	public Map<Integer,Long> insertCodedPdfFile(File pdf, String idCode) throws Exception {
 
 		long t = System.currentTimeMillis();
 
 		String s = pdf.getName();
 		Pattern p = Pattern.compile("^(\\d+)");
 		Matcher m = p.matcher(s);
 
 		if (!m.find())
 			return null;
 
 		Integer id = new Integer(m.group(1));
 
 		ArticleCitation ac = null;
 		FTD ftd = null;
 		if( idCode.equals( "pmid" ) ) {
 			ac = this.citDao.findArticleByPmid(id);			
 			if (ac == null) {
 				Set<Integer> ii = new HashSet<Integer>();
 				ii.add(id);
 				List<ArticleCitation> ll = this.insertArticlesFromPmidList(ii);
 				if( ll == null || ll.size() == 0 ) {
 					throw new Exception( idCode + ":" + id + " cannot be inserted");							
 				}
 				ac = ll.get(0);
 			}
 			ftd = this.citDao.findArticleDocumentByPmid(id);
 		} else {
 			ac = this.citDao.findArticleById(idCode, id);
 			ftd = this.citDao.findArticleDocumentById(idCode, id);
 		}
 		
 		if (ac == null) {
 			throw new Exception( idCode + ":" + id + " cannot found in database");
 		}
 
 		if( ftd != null ) {
 			logger.info("pdf already uploaded (" + idCode + ":" + id + ")");
 			Map<Integer,Long> map = new HashMap<Integer,Long>();
 			map.put(ac.getPmid(), ac.getVpdmfId());
 			return map;
 		}
 		
 		LapdfDocument doc = this.blockifyPdfFile(pdf);
 
 		this.classifyDocumentWithBaselineRules(doc);
 		String text = this.readBasicText(doc);
 
 		this.citDao.addPdfToArticleCitation(doc, ac, pdf, text);
 
 		long deltaT = System.currentTimeMillis() - t;
 		logger.info("Aded PDF to article (" + idCode + ":" + id + ") in " + deltaT
 				/ 1000 + " s");
 
 		Map<Integer,Long> map = new HashMap<Integer,Long>();
 		map.put(ac.getPmid(), ac.getVpdmfId());
 		return map;
 
 	}
 	
 	public void insertArticleCorpus(Corpus c) throws Exception {
 		
 		this.citDao.insertArticleCorpus(c);
 		
 	}
 	
 	public Map<Integer,Long> updatePmidPdfFileOrDir(File pdfOrDir) throws Exception {
 
 		Map<Integer,Long> pmidLookup = new HashMap<Integer,Long>();
 
 		if (pdfOrDir.isDirectory()) {
 
 			Pattern p = Pattern.compile("\\.pdf$");
 			List<File> pdfList = new ArrayList<File>(
 					Converters.recursivelyListFiles(pdfOrDir, p).values()
 					);
 			Collections.sort(pdfList);
 			Iterator<File> pdfIt = pdfList.iterator();
 			while (pdfIt.hasNext()) {
 				File pdf = pdfIt.next();
 
 				try {
 					pmidLookup.putAll(updateCodedPdfFile(pdf, "pmid"));
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 
 			}
 
 		} else {
 
 			pmidLookup.putAll(updateCodedPdfFile(pdfOrDir, "pmid"));
 
 		}
 
 		return pmidLookup;
 
 	}
 	
 	public Map<Integer,Long> updateCodedPdfFile(File pdf, String idCode) throws Exception {
 
 		long t = System.currentTimeMillis();
 
 		String s = pdf.getName();
 		Pattern p = Pattern.compile("^(\\d+)");
 		Matcher m = p.matcher(s);
 
 		if (!m.find())
 			return null;
 
 		Integer id = new Integer(m.group(1));
 
 		ArticleCitation ac = null;
 		FTD ftd = null;
 		if( idCode.equals( "pmid" ) ) {
 			ac = this.citDao.findArticleByPmid(id);			
 			ftd = this.citDao.findArticleDocumentByPmid(id);
 		} else {
 			ac = this.citDao.findArticleById(idCode, id);
 			ftd = this.citDao.findArticleDocumentById(idCode, id);
 		}
 		
 		if (ac == null) {
 			throw new Exception( idCode + ":" + id + " cannot found in database");
 		}
 
 		if( ftd == null ) {
 			
 			LapdfDocument doc = this.blockifyPdfFile(pdf);
 
 			this.classifyDocumentWithBaselineRules(doc);
 			String text = this.readBasicText(doc);
 
 			this.citDao.addPdfToArticleCitation(doc, ac, pdf, text);
 		
 		} else if( ftd.getLaswf() == null ){
 		
 			this.citDao.addSwfToFtd(pdf, ftd);
			this.ftdDao.updateFTD(ftd);
 			
 		} else {
 		
 			logger.info("PMID: " + idCode + ", nothing to update" );
 			
 		}
 			
 
 			
 		long deltaT = System.currentTimeMillis() - t;
 		logger.info("Aded PDF to article (" + idCode + ":" + id + ") in " + deltaT
 					/ 1000 + " s");
 
 		Map<Integer,Long> map = new HashMap<Integer,Long>();
 		map.put(ac.getPmid(), ac.getVpdmfId());
 		return map;
 
 	}
 	
 	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 
 	public Map<Integer, Long> buildPmidLookupFromDb() throws Exception {
 
 		int articleCount = getCitDao().countArticles();
 
 		Map<Integer, Long> pmidsInDb = new HashMap<Integer, Long>();
 		int i = 0;
 		do {
 			pmidsInDb.putAll(getCitDao().listAllPmidsPaged(i, 10000));
 			i = i + 10000;
 		} while (i < articleCount);
 
 		return pmidsInDb;
 
 	}
 
 	public Map<Integer, Long> buildPmidLookup(Set<Integer> pmids)
 			throws Exception {
 
 		return (getCitDao().lookupPmids(pmids));
 
 	}
 
 	public void insertArticlesFromPmidList(File pmidFile) throws Exception {
 
 		// Load file line by line as Map<String, Map<String,String>>
 		Map<Integer, Map<String, String>> idMap = this
 				.loadIdMapFromPmidFile(pmidFile);
 
 		this.loadArticlesFromPmidList(idMap, new HashSet<Integer>());
 
 	}
 
 	public void insertArticlesFromPmidList(File pmidFile, Set<Integer> pmidsInDb)
 			throws Exception {
 
 		// Load file line by line as Map<String, Map<String,String>>
 		Map<Integer, Map<String, String>> idMap = this
 				.loadIdMapFromPmidFile(pmidFile);
 
 		Set<Integer> difference = new HashSet<Integer>(idMap.keySet());
 		difference.removeAll(pmidsInDb);
 
 		this.loadArticlesFromPmidList(idMap, pmidsInDb);
 
 	}
 
 	public List<ArticleCitation> insertArticlesFromPmidList(Set<Integer> pmidsToAdd)
 			throws Exception {
 
 		List<ArticleCitation> l = new ArrayList<ArticleCitation>();
 		
 		// run checks 
 		Map<Integer, Long> pmidsInDb = this.getCitDao().lookupPmids(pmidsToAdd);
 		Set<Integer> toAdd = new HashSet<Integer>(pmidsToAdd);
 		toAdd.removeAll(pmidsInDb.keySet());
 		
 		EFetcher f = new EFetcher(toAdd);
 		while (f.hasNext()) {
 			ArticleCitation a = f.next();
 
 			if (a == null)
 				continue;
 
 			if (a.getAuthorList() == null || a.getAuthorList().size() == 0)
 				continue;
 
 			if (a.getVolume() == null)
 				a.setVolume("-");
 
 			if (a.getIssue() == null)
 				a.setIssue("-");
 
 			if (a.getPages() == null)
 				a.setPages("-");
 
 			String jStr = a.getJournal().getAbbr();
 			if (!getjLookup().containsKey(jStr)) {
 				logger.info("'" + jStr
 						+ "' not found in lookup, skipping PMID=" + a.getPmid());
 				continue;
 			}
 
 			Journal j = getjLookup().get(jStr);
 			a.setJournal(j);
 
 			try {
 
 				long t = System.currentTimeMillis();
 
 				getCitDao().insertArticleCitation(a);
 
 				long deltaT = System.currentTimeMillis() - t;
 				logger.info("inserted article, PMID=" + a.getPmid() + ", "
 						+ deltaT / 1000.0 + " s");
 
 				l.add(a);
 				
 			} catch (Exception e) {
 				logger.info("article insert failed, PMID=" + a.getPmid());
 				e.printStackTrace();
 			}
 
 		}
 		
 		return l;
 
 	}
 
 	public void loadArticlesFromPmidList(
 			Map<Integer, Map<String, String>> pmidsToAdd,
 			Set<Integer> pmidsToSkip) throws Exception {
 
 		EFetcher f = new EFetcher(pmidsToAdd.keySet(), pmidsToSkip);
 		while (f.hasNext()) {
 			ArticleCitation a = f.next();
 
 			if (a == null)
 				return;
 
 			String jStr = a.getJournal().getAbbr();
 			if (!getjLookup().containsKey(jStr)) {
 				logger.info("'" + jStr
 						+ "' not found in lookup, skipping PMID=" + a.getPmid());
 				continue;
 			}
 
 			Journal j = getjLookup().get(jStr);
 			a.setJournal(j);
 
 			Map<String, String> idKeysMap = pmidsToAdd.get(a.getPmid());
 			if (idKeysMap == null) {
 				logger.info("Can't find PMID=" + a.getPmid()
 						+ " in original map, very strange.");
 			} else {
 				Iterator<String> keyIt = idKeysMap.keySet().iterator();
 				while (keyIt.hasNext()) {
 					String key = keyIt.next();
 					ID id = new ID();
 					id.setPublication(a);
 					a.getIds().add(id);
 					id.setIdType(key);
 					id.setIdValue(idKeysMap.get(key));
 				}
 			}
 
 			try {
 				logger.info("inserting article, PMID=" + a.getPmid());
 				getCitDao().insertArticleCitation(a);
 			} catch (Exception e) {
 				logger.info("article insert failed, PMID=" + a.getPmid());
 				e.printStackTrace();
 			}
 
 		}
 
 	}
 
 	public void loadArticlesFromPmidListToCorpus(File pmidFile,
 			String corpusName) throws Exception {
 
 		// Load file line by line as Map<String, Map<String,String>>
 		Map<Integer, Map<String, String>> idMap = this
 				.loadIdMapFromPmidFile(pmidFile);
 
 		this.getCitDao().addArticlesToCorpus(idMap.keySet(), corpusName);
 
 	}
 
 	public void loadArticlesFromPmidListToCorpus(Set<Integer> pmids,
 			String corpusName) throws Exception {
 
 		this.getCitDao().addArticlesToCorpus(pmids, corpusName);
 
 	}
 	
 	public void deleteArticleCitations(File pmidFile) throws Exception {
 
 		Map<Integer, Map<String, String>> idMap = this
 				.loadIdMapFromPmidFile(pmidFile);
 
 		List<Integer> ids = new ArrayList<Integer>(idMap.keySet());
 		Collections.sort(ids);
 		Iterator<Integer> idIt = ids.iterator();
 		while (idIt.hasNext()) {
 			Integer id = idIt.next();
 
 			ArticleCitation c = this.citDao.findArticleByPmid(id);
 
 			if (c != null) {
 				try {
 					long bmkegId = c.getVpdmfId();
 					this.citDao.deleteArticleCitation(bmkegId);
 				} catch (Exception e) {
 					logger.info("Can't delete article " + c.getVpdmfLabel());
 				}
 
 			}
 
 		}
 
 	}
 
 	public void deleteArticleCitationsFromCorpus(File pmidFile, String corpusName) throws Exception {
 		
 		Map<Integer, Map<String, String>> idMap = this
 				.loadIdMapFromPmidFile(pmidFile);
 
 		Corpus c = this.citDao.findCorpusByName(corpusName);
 		
 		List<Integer> ids = new ArrayList<Integer>(idMap.keySet());
 		Collections.sort(ids);
 		Iterator<Integer> idIt = ids.iterator();
 		while (idIt.hasNext()) {
 			Integer id = idIt.next();
 
 			ArticleCitation a = this.citDao.findArticleByPmid(id);
 
 			Iterator<Corpus> ccIt = a.getCorpora().iterator();
 			Corpus cc = null;
 			boolean found = false;
 			while( ccIt.hasNext() && !found ) {
 				cc = ccIt.next();
 				if( cc.getName().equals( c.getName() ) ) {
 					found = true;
 				}
 			}
 			
 			a.getCorpora().remove(cc);
 			
 			this.citDao.updateArticleCitation(a);
 			
 		}
 		
 	}
 
 	public void deleteCorpus(String corpusName) throws Exception {
 
 		Corpus c = this.citDao.findCorpusByName(corpusName);
 		
 		this.citDao.deleteCorpus(c);
 		
 	}
 
 	public void loadMedlineArchiveDirectory(File dir)
 			throws Exception {
 
 		FileFilter archiveFilesFilter = new FileFilter() {
 			public boolean accept(File file) {
 				
 				if( !file.getName().contains("medline") )
 					return false;
 				
 				String p = file.getAbsolutePath();
 				if( p.endsWith(".xml") || p.endsWith(".gz") || p.endsWith(".bz2") )
 					return true;
 				
 				return false;
 
 			}
 		};
 
 		File[] list = dir.listFiles(archiveFilesFilter);
 		int recsInDir = 0;
 		
 		FileLookupPersistentObject fLookupPO = new FileLookupPersistentObject();
 		this.fLookup = fLookupPO.getfLookup();
 		
 		for (int k = 0; k < list.length; k++) {
 			File f = list[k];
 
 			if( !this.getfLookup().containsKey( f.getName() ) ) {
 				
 				VpdmfMedlineHandler cmh = new VpdmfMedlineHandler(this.jLookup);
 				
 				List<ArticleCitation> aList = cmh.parseMedlineFileToList(f);
 				for( ArticleCitation a: aList) {
 					this.citDao.insertArticleCitation(a);
 				}
 							
 				this.getfLookup().put(f.getName(), f);
 				fLookupPO.setfLookup(this.getfLookup());
 				fLookupPO.saveFileLookupFile();
 				
 			} else {
 
  				logger.info(f.getPath() + " file already parsed, skipped.");
 
 			}
 			
 		}
 
 	}
 
 	public Map<String, File> getfLookup() {
 		return fLookup;
 	}
 
 	public void setfLookup(Map<String, File> fLookup) {
 		this.fLookup = fLookup;
 	}
 
 	public DigitalLibraryDao getDigLibDao() {
 		return digLibDao;
 	}
 
 	public void setDigLibDao(DigitalLibraryDao digLibDao) {
 		this.digLibDao = digLibDao;
 	}
 	
 }
