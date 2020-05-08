 package pals.service;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.persistence.Query;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.log4j.Logger;
 import org.springframework.beans.BeanUtils;
 import org.springframework.transaction.annotation.Transactional;
 
 import pals.Configuration;
 import pals.analysis.AnalysisException;
 import pals.dao.DAO;
 import pals.entity.Analysis;
 import pals.entity.DataSet;
 import pals.entity.DataSetVersion;
 import pals.entity.Experiment;
 import pals.entity.PalsFile;
 import pals.entity.User;
 import pals.utils.PalsFileUtils;
 
 public class DataSetVersionService 
 {
 	static final Integer FIRST_ROW_COLUMN = 3;
 	static final String DATE_FORMAT_IN = "dd/MM/yyyy";
 	static final String DATE_FORMAT_OUT = "yyyy-MM-dd";
 	static final String DATE_FORMAT_FIRST_LAST = "MM/dd/yyyy";
 	
 	public static String COLUMN_NAME = "name";
 	public static String COLUMN_COUNTRY = "country";
 	public static String COLUMN_VEG_TYPE = "vegType";
 	public static String COLUMN_CREATED_BY = "createdBy";
 	
 	DAO dao;
 	AnalysisService analysisEntityService;
 	
 	private static final Logger log = Logger.getLogger(DataSetVersionService.class);
 	
     public void delete(Integer id)
     {
     	Object object = dao.get(DataSetVersion.class.getName(), "id", id);
     	DataSetVersion dsv = (DataSetVersion) object;
     	analysisEntityService.deleteAllAnalysisForAnalysable(dsv);
     	File metFile = new File(dsv.retrieveMetFilePath());
     	metFile.delete();
     	File fluxFile = new File(dsv.retrieveOutputFilePath());
     	fluxFile.delete();
     	File qcPlotsFile = new File(dsv.retrieveQCPlotsFilePath());
     	qcPlotsFile.delete();
     	File uploadedFile = new File(dsv.uploadedFilePath());
     	uploadedFile.delete();
    	File benchFile = new File(dsv.retrieveBenchFilePath());
    	benchFile.delete();
     	dao.delete(DataSetVersion.class.getName(), "id", dsv.getId());
     }
     
     public DataSetVersion get(Integer id)
     {
     	Object object = dao.get(DataSetVersion.class.getName(), "id", id);
     	return (DataSetVersion) object;
     }
     
     public List<DataSetVersion> getPublicDataSetVersions(Experiment experiment)
     {
     	return getPublicDataSetVersions(experiment,
     	    Configuration.getInstance().getIntProperty("PUBLIC_DATA_SET_VERSIONS_PER_PAGE"),0,null,true);
     }
     
 	public List<DataSetVersion> getPublicDataSetVersions(Experiment experiment, int limit, int offset,
 	    String sortColumn, boolean asc) 
 	{
 		String queryString = "from DataSet ds";
 		if( experiment != null )
 		{
 			queryString += " where ds.experiment.id=:experimentId";
 		}
 		else
 		{
 			queryString += " left outer join ds.experiment e where (e = NULL or e.shareWithAll = true)";
 		}
 		queryString += " and ds.latestVersion != NULL";
 		if( sortColumn == null )
 		{
 		    queryString += " order by ds.id desc";
 		}
 		else if( sortColumn != null )
 		{ 
 			if( sortColumn.equals(COLUMN_NAME) )
 			{
 			    queryString += " order by ds.name";
 			}
 			else if( sortColumn.equals(COLUMN_COUNTRY) )
 			{
 				queryString += " order by ds.country.name";
 			}
 			else if( sortColumn.equals(COLUMN_VEG_TYPE) )
 			{
 				queryString += " order by ds.vegType.vegetationType";
 			}
 			else if( sortColumn.equals(COLUMN_CREATED_BY) )
 			{
 				queryString += " order by ds.owner.fullName";
 			}
 			else
 			{
 				log.error("Sort Column code not found: " + sortColumn);
 				queryString += " order by ds.id";
 			}
 			if( asc )
 			{
 				queryString += " asc";
 			}
 			else
 			{
 				queryString += " desc";
 			}
 		}
         log.info(queryString);
 		Query query = dao.getEntityManager().createQuery(queryString);
 		query.setFirstResult(offset);
 		query.setMaxResults(limit);
 		if( experiment != null )
 		{
 			query.setParameter("experimentId", experiment.getId());
 		}
 		
 		List<DataSetVersion> dataSetVersionList = new ArrayList<DataSetVersion>();
 		
 		if( experiment != null )
 		{
 			List<DataSet> list = query.getResultList();
 			for( DataSet dataSet : list )
 			{
 				dataSetVersionList.add(dataSet.getLatestVersion());
 			}
 		}
 		else
 		{
 			List<Object[]> list = query.getResultList();
 			for( Object[] arrayItem : list )
 			{
 				DataSet dataSet = (DataSet) arrayItem[0];
 				dataSet.setExperiment((Experiment) arrayItem[1]);
 				dataSetVersionList.add(dataSet.getLatestVersion());
 			}
 		}
 		
 		return dataSetVersionList;
 	}
 	
 	public long getPublicDataSetVersionsCount(Experiment experiment) {
 		
 		String queryString = "select count(id) from DataSet";
 		if( experiment != null )
 		{
 			queryString += " where experiment.id=:experimentId";
 		}
 		else
 		{
 			queryString += " where experiment = NULL";
 		}
 		queryString += " and latestVersion != NULL";
 
 		Query query = dao.getEntityManager().createQuery(queryString);
 		if( experiment != null )
 		{
 			query.setParameter("experimentId", experiment.getId());
 		}
 		long count = (Long) query.getSingleResult();
 		if( experiment == null )
 		{
 			queryString = "select count(id) from DataSet";
 			queryString += " where experiment.shareWithAll = true";
 			queryString += " and latestVersion != NULL";
 
 			query = dao.getEntityManager().createQuery(queryString);
 			count += (Long) query.getSingleResult();
 		}
 		return count;
 	}
 	
 	public long getPublicDataSetVersionsCountAllExperiments() 
 	{
 		// count data sets with latest versions
 		String queryString = "select count(id) from DataSet";
 		queryString += " where latestVersion != NULL";
 		Query query = dao.getEntityManager().createQuery(queryString);
 		long count = (Long) query.getSingleResult();
 		
 		// count data sets that are shared with all with no latest version
 		queryString = "select count(id) from DataSet";
 		queryString += " where experiment.shareWithAll = true";
 		queryString += " and latestVersion != NULL";
 		query = dao.getEntityManager().createQuery(queryString);
 		count += (Long) query.getSingleResult();
 			
 		return count;
 	}
     
     public void update(DataSetVersion dsv)
     {
     	dao.update(dsv);
     }
     
 	/**
 	 * Copies the DSV and all its data
 	 * @throws IOException 
 	 */
     @Transactional(rollbackFor = { IOException.class } )
     public DataSetVersion copy(DataSetVersion old, DataSet dataSet) throws IOException
     {
     	DataSetVersion copy = new DataSetVersion();
     	BeanUtils.copyProperties(old, copy);
     	copy.setId(null);
     	copy.setStatus(Analysis.STATUS_NEW);
     	if( old.getEndDate() != null )
     	    copy.setEndDate((Date)old.getEndDate().clone());
     	if( old.getStartDate() != null )
     		copy.setStartDate((Date)old.getStartDate().clone());
     	if( old.getUploadDate() != null )
     		copy.setUploadDate((Date)old.getUploadDate().clone());
     	copy.setDataSet(dataSet);
     	copy.setDataSetId(dataSet.getId());
     	copy.setFiles(new ArrayList<PalsFile>());
     	copy.setCopiedTo(new ArrayList<Experiment>());
     	dao.persist(copy);
     	try
     	{
     	    FileUtils.copyFile(new File(old.uploadedFilePath()), new File(copy.uploadedFilePath()));
     	    FileUtils.copyFile(new File(old.retrieveMetFilePath()), new File(copy.retrieveMetFilePath()));
     	    FileUtils.copyFile(new File(old.retrieveOutputFilePath()), new File(copy.retrieveOutputFilePath()));
     	    FileUtils.copyFile(new File(old.retrieveQCPlotsFilePath()), new File(copy.retrieveQCPlotsFilePath()));
     	    
     	    // copy across benchmark file as well
     	    FileUtils.copyFile(new File(old.retrieveBenchFilePath()), new File(copy.retrieveBenchFilePath()));
     	}
     	catch( FileNotFoundException e )
     	{
     		log.error("Error copying data set version " + old.getId() + " files " + e.getMessage());
     	}
     	return copy;
     }
 
 	public DAO getDao() {
 		return dao;
 	}
 
 	public void setDao(DAO dao) {
 		this.dao = dao;
 	}
 
 	public AnalysisService getAnalysisEntityService() {
 		return analysisEntityService;
 	}
 
 	public void setAnalysisEntityService(AnalysisService analysisEntityService) {
 		this.analysisEntityService = analysisEntityService;
 	}
 	
 	public List<String> fixTimeCSV(String filename) throws IOException, ParseException
 	{
 		return fixTimeCSV(filename,null);
 	}
 	
 	public List<String> fixTimeCSV(String filename, String compareDate) throws IOException, ParseException
 	{
 		List<String> data = new ArrayList<String>();
 		
 		File file = new File(filename);
 		BufferedReader reader = new BufferedReader(new FileReader(file));
 		String line;
 		int i=0;
 		int added = 0;
 		
 		compareDate = parseDate(compareDate);
 
 		System.out.println("Loading data set starting from date: " + compareDate);
 		
 		while( (line = reader.readLine()) != null )
 		{
 			if( i >= FIRST_ROW_COLUMN )
 			{
 				line = line.trim();
 				if( line.length() > 0 )
 				{
 					String[] splitLine = line.split("\\,");
 					String dateString = splitLine[0];
 					if( added < Configuration.getInstance().MAX_DYNAMIC_DATA_POINTS )
 					{
 						String localTimeHours = splitLine[1];
 						SimpleDateFormat sdfIn = new SimpleDateFormat(DATE_FORMAT_IN);
 						Date date = sdfIn.parse(dateString);
 						float hoursFloat = Float.parseFloat(localTimeHours);
 						int integerPart = (int) hoursFloat;
 						float fractionalPart = hoursFloat - integerPart;
 						int minutes = (int) (fractionalPart*60);
 						SimpleDateFormat sdfOut = new SimpleDateFormat(DATE_FORMAT_OUT);
 						String outDate = sdfOut.format(date);
 						String outString = outDate + " ";
 						if( integerPart < 10 ) outString += "0";
 						outString += integerPart + ":";
 						if( minutes < 10 ) outString += "0";
 						outString += minutes;
 						if( compareDate == null || outString.compareTo(compareDate) >= 0)
 						{
 							for( int j=2; j < splitLine.length; ++j )
 							{
 								outString += "," + splitLine[j];
 							}
 							outString += "\n"; 
 							data.add(outString);
 							++added;
 						}
 					}
 				}
 			}
 		    ++i;
 		}
 		return data;
 	}
 	
 	public String getFirstDate(String filename) 
 	    throws NumberFormatException, IOException, ParseException
 	{	
 		File file = new File(filename);
 		BufferedReader reader = new BufferedReader(new FileReader(file));
 		String line;
 		int i=0;
 		
 		while( (line = reader.readLine()) != null )
 		{
 			if( i >= FIRST_ROW_COLUMN )
 			{
 				line = line.trim();
 				if( line.length() > 0 )
 				{
 					return getFirstLastDate(line);
 				}
 			}
 		    ++i;
 		}
 		return null;
 	}
 
 	public String getLastDate(String filename) 
         throws NumberFormatException, IOException, ParseException
 	{	
 		File file = new File(filename);
 		BufferedReader reader = new BufferedReader(new FileReader(file));
 		String line;
 		int i=0;
 		
 		String lastDate = null;
 		
 		while( (line = reader.readLine()) != null )
 		{
 			if( i >= FIRST_ROW_COLUMN )
 			{
 				line = line.trim();
 				if( line.length() > 0 )
 				{
 					lastDate =  getFirstLastDate(line);
 				}
 			}
 		    ++i;
 		}
 		return lastDate;
 	}
 	
 	public String getFirstLastDate(String line) throws ParseException
 	{
 		String[] splitLine = line.split("\\,");
 		String dateString = splitLine[0];
 		String localTimeHours = splitLine[1];
 		SimpleDateFormat sdfIn = new SimpleDateFormat(DATE_FORMAT_IN);
 		Date date = sdfIn.parse(dateString);
 		float hoursFloat = Float.parseFloat(localTimeHours);
 		int integerPart = (int) hoursFloat;
 		float fractionalPart = hoursFloat - integerPart;
 		int minutes = (int) (fractionalPart*60);
 		SimpleDateFormat sdfOut = new SimpleDateFormat(DATE_FORMAT_FIRST_LAST);
 		String outDate = sdfOut.format(date);
 		return outDate;
 	}
 	
 	public String parseDate(String date)
 	{
 		if( date == null || date.trim().length() <= 0 ) return null;
 		String[] splitLine = date.trim().split("\\,");
 		if( splitLine.length == 3 )
 		{
 			String year = splitLine[0];
 			String month = splitLine[1];
 			if( month.length() <= 1 ) month = '0' + month;
 			String day = splitLine[2];
 			if( day.length() <= 1 ) day = '0' + day;
 			String dateString = year + "-" + month + "-" + day + " 00:00";
 			return dateString;
 		}
 		else return null;
 	}
 	
 	public List<DataSetVersion> getDataSetVersions() 
 	{
 		Query query = dao.getEntityManager().createQuery ( "from DataSetVersion order by id desc" );
 		List<DataSetVersion> list = query.getResultList();
 		if (list == null) list = new LinkedList<DataSetVersion>();
 		return list;
 	}
 	
 	/**
 	 * If the column with the given index has -9999 in every row, false is returned, else
 	 * true is returned
 	 */
 	public boolean[] dataAvailableForColumns(String filename) throws IOException
 	{
 		List<String> data = new ArrayList<String>();
 		
 		File file = new File(filename);
 		BufferedReader reader = new BufferedReader(new FileReader(file));
 		String line;
 		
 		boolean[] availableColumns = null;
 		
 		int row = 1;
 		while( (line = reader.readLine()) != null )
 		{
 			if( row > 3 ) // skip header rows
 			{
 				line = line.trim();
 				if( line.length() > 0 )
 				{
 		            String[] splitLine = line.split("\\,");
 		            if( availableColumns == null ) 
 		            {
 		            	availableColumns = new boolean[splitLine.length];
 		            	// initialise all to false
 		                for( int i=0; i < availableColumns.length; ++i )
 		                {
 		                	availableColumns[i] = false;
 		                }
 		            }
 		            for( int i=2; i < splitLine.length; ++i )
 		            {
 		            	Float floatValue = new Float(splitLine[i]);
 		            	Float compare = Configuration.getInstance().getFloatProperty("NULL_FLOAT_VALUE");
 		            	if( !floatValue.equals(compare) )
 		            	{
 		            		availableColumns[i] = true;
 		            	}
 		            }
 				}
 			}
 			++row;
 		}
 		return availableColumns;
 	}
 	
 	public Thread empiricalBenchmarks(DataSetVersion dsv, User user) throws IOException, AnalysisException, InterruptedException
 	{
 		// Load all other data set verions
 		
 		List<DataSetVersion> dsvList = 	getPublicDataSetVersions(user.getCurrentExperiment(),
 		    10000, 0, null, true);  
 		
 		log.info("Running empirical benchmarks, datasets found: " + dsvList.size());
 		
 		List<String> metFiles = new ArrayList<String>();
 		List<String> fluxFiles = new ArrayList<String>();
 		
 		for( DataSetVersion dsvTrain : dsvList )
 		{
 			if( !dsvTrain.getId().equals(dsv.getId()) )
 			{
 				metFiles.add(PalsFileUtils.correctSlashesForR(PalsFileUtils.getDataSetVersionMetFilePath(dsvTrain)));
 				fluxFiles.add(PalsFileUtils.correctSlashesForR(PalsFileUtils.getDataSetVersionFluxFilePath(dsvTrain)));
 			}
 		}
 		
 	    // Get met file
 		String metFile = PalsFileUtils.correctSlashesForR(PalsFileUtils.getDataSetVersionMetFilePath(dsv));
 		
 		// Get output file path
 		String outFile = PalsFileUtils.correctSlashesForR(dsv.getBenchmarkFile());
 		
 		BufferedWriter out = new BufferedWriter(new FileWriter(outFile));
 		
 		out.write("TrainPathsMet <- c(");
 		boolean first = true;
 		for( String path : metFiles )
 		{
 			if( !first ) out.write(",");
 			else first = false;
 			out.write("'"+path+"'");
 		}
 		out.write(")");
 		out.newLine();
 		
 		out.write("TrainPathsFlux <- c(");
 		first = true;
 		for( String path : fluxFiles )
 		{
 			if( !first ) out.write(",");
 			else first = false;
 			out.write("'"+path+"'");
 		}
 		out.write(")");
 		out.newLine();
 		
 		out.write("PredictPathMet='"+metFile+"'");
 		out.newLine();
 		
 		out.write("DataSetName='"+dsv.getDataSet().getName()+"'");
 		out.newLine();
 		
 		out.write("DataSetVersion='"+dsv.getName()+"'");
 		out.newLine();
 		
 		out.write("PredictNcPath='"+PalsFileUtils.correctSlashesForR(PalsFileUtils.getDataSetVersionBenchmarkFilePath(dsv))+"'");
 		out.newLine();
 		
 		// Get site lat lon
 		
 		out.write("SiteLat="+dsv.getDataSet().getLatitude());
 		out.newLine();
 		out.write("SiteLon="+dsv.getDataSet().getLongitude());
 		out.newLine();
 		
 		out.write("library(pals)");
 		out.newLine();
 		out.write("analysisType = 'BenchmarkGeneration'");
 		out.newLine();
 		//out.write("checkUsage(analysisType)");
         //out.newLine();
 		out.write("removeflagged = TRUE # only use non-gapfilled data?");
 		out.newLine();
 
 		out.write("varnames=c(QleNames[1],QhNames[1],NEENames[1],QgNames[1])");
 		out.newLine();
 		out.write("varsunits=c(QleUnits$name[1],QhUnits$name[1],NEEUnits$name[1],QgUnits$name[1])");
 		out.newLine();
 
 		out.write("GenerateBenchmarkSet(TrainPathsMet,TrainPathsFlux,PredictPathMet,DataSetName,DataSetVersion,PredictNcPath,SiteLat,SiteLon,varnames,varsunits,removeflagged)");
 		out.newLine();
 		
 		out.close();
 		
 		// run R running file
 		Thread runner = new EmpiricalBenchmarkRunner(outFile);
 		runner.start();
 		return runner;
 	}
 	
 	public class EmpiricalBenchmarkRunner extends Thread
 	{
 		String fileName;
 		
 		public EmpiricalBenchmarkRunner(String fileName)
 		{
 			this.fileName = fileName;
 		}
 		
 		@Override
 		public void run()
 		{
 			String command = Configuration.getInstance().getStringProperty("R_COMMAND");
 			command += " " + fileName;
 			try {
 				PalsFileUtils.executeCommand(command);
 			} catch (AnalysisException e) {
 				log.error(e);
 			} catch (IOException e) {
 				log.error(e);
 			} catch (InterruptedException e) {
 				log.error(e);
 			}
 			finally
 			{
 			    new File(fileName).delete();
 			}
 		}
 	}
 
 	public void generateBenchmarks(User user) 
 	{
 		// retrieve all data set versions for this experiment
 		//List<DataSetVersion> dsvs = getAllDataSetVersions(user.getCurrentExperiment());
 		List<DataSetVersion> dsvs = getAllDataSetVersions();
 		
 		// run empirical benchmarks on them
 		for( DataSetVersion dsv : dsvs )
 		{
 			try 
 			{
 				Thread thread = empiricalBenchmarks(dsv,user);
 				thread.join();
 			} 
 			catch (AnalysisException e) 
 			{
 				log.error("Failed to generate benchmarks for dsv: " + dsv.getId());
 				log.error(e);
 			} 
 			catch (IOException e) 
 			{
 				log.error("Failed to generate benchmarks for dsv: " + dsv.getId());
 				log.error(e);
 			} 
 			catch (InterruptedException e) 
 			{
 				log.error("Failed to generate benchmarks for dsv: " + dsv.getId());
 				log.error(e);
 			}
 		}
 	}
 	
 	public List<DataSetVersion> getAllDataSetVersions(Experiment experiment) 
 		{
 			String queryString = "from DataSetVersion dsv";
 			if( experiment != null )
 			{
 				queryString += " where dsv.dataSet.experiment.id=:experimentId";
 			}
 			else
 			{
 				queryString += " left outer join dsv.dataSet.experiment e where (e = NULL or e.shareWithAll = true)";
 			}
 			queryString += " and dsv.dataSet.latestVersion != NULL";
 	        log.info(queryString);
 			Query query = dao.getEntityManager().createQuery(queryString);
 			if( experiment != null )
 			{
 				query.setParameter("experimentId", experiment.getId());
 			}
 			List<Object> results = query.getResultList();
 			List<DataSetVersion> dsvs = new ArrayList<DataSetVersion>();
 			for( Object result : results )
 			{
 				DataSetVersion dsv = null;
 				if( result instanceof DataSetVersion )
 				{
 					dsv = (DataSetVersion) result;
 				}
 				else
 				{
 					Object[] resultObjectArray = (Object[]) result;
 					dsv = (DataSetVersion) resultObjectArray[0];
 				}
 				dsvs.add(dsv);
 			}
 			return dsvs;
 		}
 	
 	/**
 	 * Retrieves every single data set version in the database
 	 * @return
 	 */
 	public List<DataSetVersion> getAllDataSetVersions() 
 	{
 		String queryString = "from DataSetVersion dsv";
 		Query query = dao.getEntityManager().createQuery(queryString);
 		List<Object> results = query.getResultList();
 		List<DataSetVersion> dsvs = new ArrayList<DataSetVersion>();
 		for( Object result : results )
 		{
 			DataSetVersion dsv = null;
 			if( result instanceof DataSetVersion )
 			{
 				dsv = (DataSetVersion) result;
 			}
 			else
 			{
 				Object[] resultObjectArray = (Object[]) result;
 				dsv = (DataSetVersion) resultObjectArray[0];
 			}
 			dsvs.add(dsv);
 		}
 		return dsvs;
 	}
 }
