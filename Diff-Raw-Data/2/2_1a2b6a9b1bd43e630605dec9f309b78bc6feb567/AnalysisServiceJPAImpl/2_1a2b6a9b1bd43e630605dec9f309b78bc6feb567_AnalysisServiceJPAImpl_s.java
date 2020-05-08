 package pals.service;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 import org.apache.log4j.Logger;
 import org.springframework.transaction.annotation.Transactional;
 
 import pals.Configuration;
 import pals.analysis.AnalysisException;
 import pals.entity.Analysable;
 import pals.entity.Analysis;
 import pals.entity.AnalysisType;
 import pals.entity.ModelOutput;
 import pals.utils.ImageUtil;
 import pals.utils.PalsFileUtils;
 
//@Transactional
 public class AnalysisServiceJPAImpl implements AnalysisServiceInterface {
 
 	private static Logger logger = Logger.getLogger(AnalysisServiceJPAImpl.class);
 	
 	private int MAX_THREADS = 10;
 	private int threads = 0;
 	private static int WAIT_TIME = 10000;
 	List<Thread> threadList;
 	
 	private EntityManager entityManager;
 	private AnalysableService analysableService;
 	private AnalysisService analysisEntityService;
 
     @PersistenceContext
     public void setEntityManager(EntityManager entityManager) {
 	   this.entityManager = entityManager;
     }
 	
     public List<AnalysisType> getAnalysisList() {
 		Query query = entityManager.createQuery("from AnalysisType");
 		return query.getResultList();
 	}
 	
 	private List<Analysis> getNewAnalysisRuns() {
 		Query query = entityManager.createQuery ( "from Analysis where status= :status" ).setParameter("status", Analysis.STATUS_NEW);
 		return query.getResultList();
 	}
 	
 	private List<Analysis> getCompleteAnalysisRuns() {
 		Query query = entityManager.createQuery ( "from Analysis where status=:statusC or status=:statusE" );
 		query.setParameter("statusC",Analysis.STATUS_COMPLETE);
 		query.setParameter("statusE", Analysis.STATUS_ERROR);
 		return query.getResultList();
 	}
 
 	public void persistAnalysis(AnalysisType analysis) {
 		entityManager.persist(analysis);
 	}
 	
 	public void mergeAnalysis(AnalysisType analysis) {
 		entityManager.merge(analysis);
 	}
 	
 	public void persistInput(Analysable input) {
 		entityManager.persist(input);
 	}
     
 	public List<Analysable> getAnalysableAwaitingPreAnalCheck() {
 		Query query = entityManager.createQuery ( "from Analysable where status= :status" ).setParameter("status", ModelOutput.STATUS_PREPARED);
 		return query.getResultList();
 	}
 	
 	
 	
 	private List<ModelOutput> getModelOutputsRunning() {
 		Query query = entityManager.createQuery ( "from ModelOutput where status= :status" ).setParameter("status", ModelOutput.STATUS_ANALYSIS);
 		return query.getResultList();
 	}
 	
 	private List<ModelOutput> getModelOutputsAwaitingRerun() {
 		Query query = entityManager.createQuery ( "from ModelOutput where status= :status" ).setParameter("status", ModelOutput.STATUS_RERUN);
 		return query.getResultList();
 	}
 	
 	private boolean existsAnalysisRun(Integer modelOutputId, Integer analysisId) {
 		Query query = entityManager.createQuery( "from AnalysisRun where modelOutputId = :modelOutputId and analysisId = :analysisId").setParameter("modelOutputId",modelOutputId).setParameter("analysisId", analysisId);
 		return !query.getResultList().isEmpty();
 	}
 	
 	private List<Analysis> getAnalysisRunsForModelOutput(ModelOutput modelOutput) {
 		Query query = entityManager.createQuery ( "from AnalysisRun where modelOutputId = :modelOutputId" ).setParameter("modelOutputId", modelOutput.getId());
 		return query.getResultList();
 	}
 	
 	public void persistAnalysable(Analysable modelOutput) {
 		entityManager.persist(modelOutput);
 	}
 	
 	/**
 	 * Delete all analyses that have run on this ModelOutput
 	 */
 	private void deleteAnalyses(ModelOutput modelOutput) {
 		Iterator<Analysis> analysisRuns = getAnalysisRunsForModelOutput(modelOutput).iterator();
 		Analysis ar;
 		while (analysisRuns.hasNext()) {
 			ar = analysisRuns.next();
 			ar.setVisible(false);
 			entityManager.merge(ar);
 		}
 	}
 	
 	/**
 	 * 
 	 * Look in the database for recently uploaded Model Outputs, check them and run analyses on them 
 	 * and set their status to STATUS_ANALYSIS.
 	 * 
 	 */
 	public void pollPreparedAnalysable() {
 		logger.info("Polling Prepared Analysables.....");
 		List<Analysable> mos = getAnalysableAwaitingPreAnalCheck();
 		for( Analysable analysable : mos )
 		{
 			logger.debug("Initiating Pre-analysis checking for analysable: " + analysable.getId());
 			analysableService.runAnalysis(getAnalysisList(),analysable);
 		}
 	}
 	
 	/***
 	 * Look in database for ModelOutputs requiring rerun, delete their analyses and
 	 * set their status to PREPARED.
 	 */
 	public void pollRerunModelOutputs() {
 		logger.info("Polling Prepared ModelOutputs.....");
 		List<ModelOutput> mos = getModelOutputsAwaitingRerun();
 		Iterator<ModelOutput>iter = mos.iterator();
 		ModelOutput modelOutput;
 		while (iter.hasNext()) {
 			modelOutput = iter.next();
 			deleteAnalyses(modelOutput);
 			modelOutput.setStatus(ModelOutput.STATUS_PREPARED);
 		}
 	}
 	
 	/***
 	 * Check all running Model Outputs and if their analysis runs are complete, set their status to
 	 * Completed or Error.
 	 */
 	public void pollRunningModelOutputs() {
 		logger.info("Polling Running ModelOutputs.....");
 		List<ModelOutput> mos = getModelOutputsRunning();
 		Iterator<ModelOutput>iter = mos.iterator();
 		ModelOutput modelOutput;
 		Query query;
 		while (iter.hasNext()) {
 			modelOutput = iter.next();
 			// check status of this model outputs analyses
 			query = entityManager.createQuery("from Analysis where analysable.id = :modelOutputId and status in (:status1, :status2)");
 			query.setParameter("modelOutputId",modelOutput.getId());
 			query.setParameter("status1", Analysis.STATUS_NEW);
 			query.setParameter("status2", Analysis.STATUS_RUNNING);
 			if (query.getResultList().isEmpty()) {
 				// All analyses have finished. 
 				query = entityManager.createQuery("from Analysis where analysable.id = :modelOutputId and status = :status");
 				query.setParameter("modelOutputId",modelOutput.getId());
 				query.setParameter("status", Analysis.STATUS_COMPLETE);
 				if (query.getResultList().isEmpty()) {
 					// no Analyses completed successfully. Set this Model Output to error state.
 					modelOutput.setStatus(ModelOutput.STATUS_ERROR);
 				} else {
 					modelOutput.setStatus(ModelOutput.STATUS_COMPLETE);
 				}
 				entityManager.merge(modelOutput);
 			}
 		}
 		
 	}
 	
 	
 	/***
 	 * Generate a PDF file that is the result of this analysis running, put it in the directory
 	 * associated with the ModelOutput.
 	 * @param analRun
 	 */
 	private void generateModelOutputPDF(Analysis analRun)  throws AnalysisException, IOException, InterruptedException {
 		if( analRun.getAnalysable() instanceof ModelOutput )
 		{
 		    ModelOutput modelOutput = (ModelOutput) analRun.getAnalysable();
 		    String execPath = analRun.getAnalysisType().getExecutablePath();
 		    String modelOutputFilePath = analRun.getAnalysable().retrieveOutputFilePath();
 		    String dataSetFilePath = PalsFileUtils.getDataSetVersionFluxFilePath(modelOutput.getDataSetVersion());
 		    String outfileLabel = PalsFileUtils.getAnalysisRunFileLabel(analRun);
 		    String siteNameForPlot = modelOutput.getDataSetVersion().getDisplayName();
 		    siteNameForPlot = siteNameForPlot.replace(' ', '_'); // @fix yuk, R can't handle arguments with spaces
 		    String outType = "pdf";
 		    String pdfCmd = execPath + " " + modelOutputFilePath + " " + dataSetFilePath + " " + outfileLabel + " \"" + siteNameForPlot + "\" " + outType;
 		    PalsFileUtils.executeCommand(pdfCmd);
 		}
 		else
 		{
 			throw new AnalysisException("Attempted to generate ModelOutputPDF using: " + analRun.getAnalysable().getClass().getName());
 		}
 	}
 	
 	/***
 	 * Generate a PNG Image file for this analysis in the directory associated with the ModelOutput.
 	 * 
 	 * At the moment this function just reruns the analysis with PNG argument instead of PDF.
 	 * Ultimately this should just convert the PDF to PNG, which will be quicker.
 	 * @param analRun
 	 */
 	private void generateModelOutputPNGUsingR(Analysis analRun) throws AnalysisException, IOException, InterruptedException {
 		if( analRun.getAnalysable() instanceof ModelOutput )
 		{
 			ModelOutput modelOutput = (ModelOutput) analRun.getAnalysable();
 			String execPath = analRun.getAnalysisType().getExecutablePath();
 			String modelOutputFilePath = PalsFileUtils.getModelOutputFilePath(modelOutput);
 			String dataSetFilePath = PalsFileUtils.getDataSetVersionFluxFilePath(modelOutput.getDataSetVersion());
 			String outfileLabel = PalsFileUtils.getAnalysisRunFileLabel(analRun);
 			String siteNameForPlot = modelOutput.getDataSetVersion().getDisplayName();
 			siteNameForPlot = siteNameForPlot.replace(' ', '_'); // @fix yuk, R can't handle arguments with spaces
 			String outType = "png";
 			String pngCmd = execPath + " " + modelOutputFilePath + " " + dataSetFilePath + " " + outfileLabel + " \"" + siteNameForPlot + "\" " + outType;
 			PalsFileUtils.executeCommand(pngCmd);
 		}
 		else
 		{
 			throw new AnalysisException("Attempted to generate ModelOutputPDF using: " + analRun.getAnalysable().getClass().getName());
 		}
 	}
 	
 	private void generatePNGUsingConvert(Analysis analRun) throws IOException, InterruptedException, AnalysisException {
 		String pdfFilePath = PalsFileUtils.getAnalysisRunFilePDF(analRun).getAbsolutePath();
 		String pngFilePath = PalsFileUtils.getAnalysisRunFilePNG(analRun).getAbsolutePath();
 		String convertCMD = Configuration.getInstance().CONVERT_CMD;
 		String pngCmd = convertCMD + " " + pdfFilePath + " " + pngFilePath;
 		PalsFileUtils.executeCommand(pngCmd);
 	}
 	
 	private void generateModelOutputPNG(Analysis analRun) throws IOException, InterruptedException, AnalysisException {
 		
 		if (Configuration.getInstance().USE_CONVERT_FOR_PNGS) {
 			generatePNGUsingConvert(analRun);
 		} else {
 			generateModelOutputPNGUsingR(analRun);
 		}
 	}
 	
 	private void generateThumbnail(Analysis analRun) throws IOException
 	{
 		//String pdfFilePath = PalsFileUtils.getAnalysisRunFilePDF(analRun).getAbsolutePath();
 		String pngFilePath = PalsFileUtils.getAnalysisRunFilePNG(analRun).getAbsolutePath();
 		String thumbFilePath = PalsFileUtils.getAnalysisRunFileThumb(analRun).getAbsolutePath();
 		ImageUtil.createThumbnail(new File(pngFilePath), new File(thumbFilePath));
 	}
 	
 	/**
 	 * Look in the database for AnalysisRuns with status NEW and
 	 * start these analysis runs.
 	 */
 	public void pollAnalysisRuns() {
 		logger.info("Polling AnalysisRuns....");
 		threadList = new ArrayList<Thread>();
 		for( Analysis analysis : getNewAnalysisRuns())
 		{
 			while( !startThread() )
 			{
 				try 
 				{
 					Thread.sleep(WAIT_TIME);
 				} 
 				catch (InterruptedException e) 
 				{
 					logger.error(e.getMessage());
 				}
 			}
 			Thread thread = 
 				new StartAnalysisRun(analysis, analysisEntityService, this);
 			threadList.add(thread);
 			thread.start();
 		}
 		for( Thread thread : threadList )
 		{
 			try 
 			{
 				thread.join();
 			} 
 			catch (InterruptedException e) 
 			{
 				logger.info(e.getMessage());
 			}
 		}
 	}
 	
 	/**
 	 * Look in the database for AnalysisRuns with status COMPLETE or ERROR and
 	 * start these analysis runs.
 	 */
 	public void reRunAnalysisRuns() {
 		logger.info("ReRunning AnalysisRuns....");
 		threadList = new ArrayList<Thread>();
 		for( Analysis analysis : getCompleteAnalysisRuns())
 		{
 			logger.info("Processing analysis: " + analysis.getId());
 			while( !startThread() )
 			{
 				logger.info("Waiting for thread to be available");
 				try 
 				{
 					Thread.sleep(WAIT_TIME);
 				} 
 				catch (InterruptedException e) 
 				{
 					logger.error(e.getMessage());
 				}
 			}
 			logger.info("Re-running analysis run: " + analysis.getId());
 			Thread thread = 
 				new StartAnalysisRun(analysis, analysisEntityService, this);
 			threadList.add(thread);
 			thread.start();
 		}
 		for( Thread thread : threadList )
 		{
 			try 
 			{
 				thread.join();
 			} 
 			catch (InterruptedException e) 
 			{
 				logger.info(e.getMessage());
 			}
 		}
 	}
 	
 	public synchronized boolean startThread()
 	{
 		if( threads >= MAX_THREADS )
 		{
 			logger.info("Max threads exceeded " + threads);
 			return false;
 		}
 		else
 		{
 			logger.info("Allocating a new thread");
 			++threads;
 			return true;
 		}
 	}
 	
 	private synchronized void stopThread()
 	{
 		logger.info("Thread finished, remaining: " + threads);
 		--threads;
 	}
 	
 	public class StartAnalysisRun extends Thread
 	{
 		Analysis analysis;
 		AnalysisService analysisService;
 		AnalysisServiceJPAImpl parent;
 		
 		public StartAnalysisRun(Analysis analysis, AnalysisService analysisService,
 				AnalysisServiceJPAImpl parent)
 		{
 			super();
 			this.analysis = analysis;
 			this.analysisService = analysisService;
 			this.parent = parent;
 		}
 		
 		public void run()
 		{
 			try
 			{
 			    analysisService.startAnalysisRun(analysis);
 			}
 			catch( Exception  e )
 			{
 				logger.info(e);
 				e.printStackTrace();
 			}
 			finally
 			{
 			    parent.stopThread();
 			}
 		}
 	}
 
 	public AnalysableService getAnalysableService() {
 		return analysableService;
 	}
 
 	public void setAnalysableService(AnalysableService analysableService) {
 		this.analysableService = analysableService;
 	}
 
 	public AnalysisService getAnalysisEntityService() {
 		return analysisEntityService;
 	}
 
 	public void setAnalysisEntityService(AnalysisService analysisEntityService) {
 		this.analysisEntityService = analysisEntityService;
 	}
 	
 	
 	
 }
