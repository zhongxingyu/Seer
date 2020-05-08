 package pals.service;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Query;
 
 import org.apache.log4j.Logger;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import pals.Configuration;
 import pals.Globals;
 import pals.analysis.AnalysisException;
 import pals.dao.DAO;
 import pals.entity.Analysable;
 import pals.entity.Analysis;
 import pals.entity.AnalysisType;
 import pals.entity.ModelOutput;
 import pals.utils.ImageUtil;
 import pals.utils.PalsFileUtils;
 
 @Transactional
 public class AnalysisService 
 {
 	public DAO dao;
 	
 	private static final Logger log = Logger.getLogger(AnalysisService.class);
 	
 	public static final String OUTPUT_TYPE_PDF = "pdf";
 	public static final String OUTPUT_TYPE_PNG = "png";
 	
 	@Transactional(propagation=Propagation.REQUIRES_NEW)
     public void startAnalysisRun(Analysis analysis)
     {
 		log.info("Starting analysis run:" + analysis.getId());
 		analysis.setStartTime(new Date());
 		analysis.setStatus(Analysis.STATUS_RUNNING);
 		dao.update(analysis);
 		try {
 			generatePlot(analysis,OUTPUT_TYPE_PDF);
 			generatePlot(analysis,OUTPUT_TYPE_PNG);
 			generateThumbnail(analysis);
 			analysis.setStatus(Analysis.STATUS_COMPLETE);
 		} catch (AnalysisException e) {
 			e.printStackTrace();
			log.error(e.getMessage());
 			analysis.setStatus(Analysis.STATUS_ERROR);
			analysis.setErrorMessage("R script failure");
 		} catch (IOException e) {
 			e.printStackTrace();
			log.error(e.getMessage());
 			analysis.setStatus(Analysis.STATUS_ERROR);
			analysis.setErrorMessage("Failed to locate data");
 		} catch (InterruptedException e) {
 			e.printStackTrace();
			log.error(e.getMessage());
 			analysis.setStatus(Analysis.STATUS_ERROR);
			analysis.setErrorMessage("Processing interrupted");
 		} 
 		analysis.setEndTime(new Date());
 		dao.update(analysis);
 		
 		// now run the benchmarks and don't complain if there is an error
 		try {
 			generateBenchmark(analysis,OUTPUT_TYPE_PNG);
 			generateBenchmark(analysis,OUTPUT_TYPE_PDF);
 		} catch (AnalysisException e) {
 			log.error(e);
 			e.printStackTrace();
 		} catch (IOException e) {
 			log.error(e);
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			log.error(e);
 			e.printStackTrace();
 		}
     }
     
 	public void generatePlot(Analysis analysis, String outType) throws AnalysisException, IOException, InterruptedException
 	{
 		Analysable analysable = analysis.getAnalysable();
 		String execPath = analysis.getAnalysisType().getExecutablePath();
 		String outputFilePath = analysis.getAnalysable().retrieveOutputFilePath();
 		String outfileLabel = PalsFileUtils.getAnalysisRunFileLabel(analysis);
 		String siteNameForPlot = analysis.getAnalysable().retrieveSiteName().replace(' ', Globals.REPLACEMENT_CHAR);
 		String cmd = "";
 		if( analysis.getAnalysisType().getType().equals(AnalysisType.DATA_SET_VERSION_ANALYSIS_TYPE) )
 		{
 		    cmd = execPath + " " + outputFilePath + " " + outfileLabel + " " + siteNameForPlot + " " + outType;
 		}
 		else if( analysis.getAnalysisType().getType().equals(AnalysisType.MODEL_OUTPUT_ANALYSIS_TYPE) )
 		{
 			ModelOutput modelOutput = (ModelOutput) analysable;
 			String dataSetVersionPath = modelOutput.getDataSetVersion().retrieveFluxFilePath();
 			cmd = execPath + " " + outputFilePath + " " + dataSetVersionPath
 			    + " " + outfileLabel + " " + siteNameForPlot + " " + modelOutput.getName() + " " + outType;
 		}
 		cmd += " " + Configuration.getInstance().PLOTS_WIDTH + " " + Configuration.getInstance().PLOTS_HEIGHT;
 		PalsFileUtils.executeCommand(cmd);
 	}
 
 	public void deleteAllAnalysisForAnalysable(Analysable analysable)
 	{
 		// first delete all the generated png and pdf files
 		String queryString = "from Analysis a WHERE a.analysable.id=:id";
 		Query query = dao.getEntityManager().createQuery(queryString);
 		query.setParameter("id", analysable.getId());
 		List<Analysis> analysisList = query.getResultList();
 		for( Analysis analysis : analysisList )
 		{
 			File pngFile = PalsFileUtils.getAnalysisRunFilePNG(analysis);
             log.info("Deleting: " + pngFile.getAbsolutePath());
 			if( !pngFile.delete() ) log.warn("Failed to delete file: " + pngFile.getAbsolutePath());
 			File pdfFile = PalsFileUtils.getAnalysisRunFilePDF(analysis);
 			if( !pdfFile.delete() ) log.warn("Failed to delete file: " + pdfFile.getAbsolutePath());
 			File thumbFile = PalsFileUtils.getAnalysisRunFileThumb(analysis);
 			if( !thumbFile.delete() ) log.warn("Failed to delete file: " + thumbFile.getAbsolutePath());
 		}
 		
 		// then delete the objects from the database
 		queryString = "DELETE from Analysis a WHERE a.analysable.id=:id";
 		query = dao.getEntityManager().createQuery(queryString);
 		query.setParameter("id", analysable.getId());
 		query.executeUpdate();
 	}
 	
 	private void generateThumbnail(Analysis analRun) throws IOException
 	{
 		String pngFilePath = PalsFileUtils.getAnalysisRunFilePNG(analRun).getAbsolutePath();
 		String thumbFilePath = PalsFileUtils.getAnalysisRunFileThumb(analRun).getAbsolutePath();
 		ImageUtil.createThumbnail(new File(pngFilePath), new File(thumbFilePath));
 	}
 
 	public DAO getDao() {
 		return dao;
 	}
 
 	public void setDao(DAO dao) {
 		this.dao = dao;
 	}
 
 	public void generateBenchmark(Analysis analysis,String outType) 
 	    throws AnalysisException, IOException, InterruptedException 
 	{
 		Analysable analysable = analysis.getAnalysable();
 		String execPath = Configuration.getInstance().getStringProperty("R_COMMAND");
 		String rDir = Configuration.getInstance().getStringProperty("R_DIR");
 		String commandName = analysis.getAnalysisType().getName().replace("Model","Bench");
 //		String outputFilePath = analysis.getAnalysable().retrieveOutputFilePathBench();
 		String outputFilePath = PalsFileUtils.getAnalysisRunFileLabelBench(analysis);
 		String userBenchDeclaration = rDir + analysis.getId() + "BenchDeclaration.R";
 		String siteNameForPlot = analysis.getAnalysable().retrieveSiteName().replace(' ', Globals.REPLACEMENT_CHAR);
 		String cmd = "";
 		if( analysis.getAnalysisType().getType().equals(AnalysisType.MODEL_OUTPUT_ANALYSIS_TYPE) )
 		{	
 			String executable = rDir + commandName + ".R";
 			if( new File(executable).exists() )
 			{
 				executable = execPath + " " + executable;
 				ModelOutput modelOutput = (ModelOutput) analysable;
 				
                 File file = new File(userBenchDeclaration);
                 BufferedWriter out = new BufferedWriter(new FileWriter(file));
                 out.write("UserBenchNames = c('B_Emp1lin','B_Emp2lin','B_Emp3km27')");
                 out.newLine();
                 String benchPath = modelOutput.getDataSetVersion().retrieveBenchFilePath();
                 out.write("UserBenchPaths = c('");
                 out.write(benchPath + "','");
                 out.write(benchPath+"','");
                 out.write(benchPath+"')");
                 out.newLine();
                 out.close();
 				
 				String moFilePath = modelOutput.retrieveOutputFilePath();
 				String dataSetVersionPath = modelOutput.getDataSetVersion().retrieveFluxFilePath();
 				cmd = executable + " " + 
 				userBenchDeclaration + " " + 
 				moFilePath + " " + 
 				dataSetVersionPath + " " + 
 				//analysis.getAnalysisType().getAnalysisTypeName().replace("Model","Bench") 
 				outputFilePath
 				+ " " +
 			    siteNameForPlot + " " + 
 			    modelOutput.getName() + " " + 
 			    outType + " " +
 				Configuration.getInstance().PLOTS_WIDTH + " " + 
 				Configuration.getInstance().PLOTS_HEIGHT; // + " " +
 				//outputFilePath;
 				PalsFileUtils.executeCommand(cmd);
 				
 				//file.delete();
 			}
 			else
 			{
 				log.info("Executable does not exist: " + executable);
 			}
 		}
 	}
 }
