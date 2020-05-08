 package main;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.List;
 
 import org.encog.ensemble.EnsembleAggregator;
 import org.encog.ensemble.EnsembleMLMethodFactory;
 import org.encog.ensemble.EnsembleTrainFactory;
 import org.encog.ensemble.aggregator.WeightedAveraging.WeightMismatchException;
 
 import techniques.AdaBoostET.RequiresWeightedAggregatorException;
 import techniques.EvaluationTechnique;
 import helpers.ArgParser;
 import helpers.ArgParser.BadArgument;
 import helpers.DBConnect;
 import helpers.DataLoader;
 import helpers.Evaluator;
 import helpers.ChainParams;
 import helpers.FileLoader;
 import helpers.ProblemDescription;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Properties;
 
 import org.apache.commons.cli.*;
 
 public class Test {
 
 	private static int EXPERIMENT = 2;
 	Evaluator ev;
 	static DataLoader dataLoader;
 	static ProblemDescription problem;
 		
 	private static List<Integer> sizes;
 	private static List<Integer> dataSetSizes;
 	private static List<Double> trainingErrors;
 	private static int nFolds;
 	private static double activationThreshold;
 	private static EnsembleTrainFactory etf;
 	private static List<EnsembleMLMethodFactory> mlfs;
 	private static EnsembleAggregator agg;
 	private static String etType;
 	private static boolean verbose;
 	private static double selectionError;
 	//default is no limit
 	private static int targetRunCount = 0;
 	private static int maxIterations;
 	private static boolean noSQL;
 	
 	private static Connection sqlConnection;
 	private static String dbconn,dbuser,dbpass;
 	private static DBConnect reconnectCallback;
 	
 	public static void loop() throws SQLException, FileNotFoundException, IOException, WeightMismatchException, RequiresWeightedAggregatorException
 	{
 		DateFormat sqlDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 		Calendar cal = Calendar.getInstance();
 		Statement statement = null;
 		long chainId = 0;
 		if(!noSQL)
 		{
 			statement = sqlConnection.createStatement();
 			statement.setQueryTimeout(30);
 			ResultSet r = statement.executeQuery("SELECT COUNT(*) AS count FROM chains "
 					+ " WHERE folds = " + nFolds
 					+ " AND aggregation = '" + agg.getLabel() + "'"
 					+ " AND problem = '" + problem.getLabel() + "'"
 					+ " AND technique = '" + etType + "'"
 					+ " AND ensemble_training = '" + etf.getLabel() + "'"
 					+ " AND experiment = " + EXPERIMENT
 					+ " AND invalidated = 0"
 					);
 			if(r.next()) {
 				int alreadyDone = r.getInt("count");
 				if (targetRunCount == 0 || alreadyDone >= targetRunCount) {
 					System.out.println("Already reached run limit, not starting chain");
 					System.exit(1);
 				}
 			}
 			else
 			{
 				throw new SQLException("count(*) query returned 0 rows");
 			}
 			r.close();
 			statement.executeUpdate("INSERT INTO chains (experiment,folds,aggregation,problem,technique,start,ensemble_training,invalidated) VALUES (" + EXPERIMENT + ", " + nFolds + 
 					                ", '" + agg.getLabel() + "'" + 
 					                ", '" + problem.getLabel() + "'" +
 					                ", '" + etType + "'" +
 					                ", '" + sqlDateFormat.format(cal.getTime()) + "' " +
 					                ", '" + etf.getLabel() + "'" +
 					                ", 1)" +
 					                ";", Statement.RETURN_GENERATED_KEYS);
 			ResultSet rs = statement.getGeneratedKeys();
 			if(rs.next()) {
 				chainId = rs.getLong(1);
 			}
 			rs.close();
 			sqlConnection.close();
 		}
 		for (Integer dataSetSize : dataSetSizes)
 		for (int fold=0; fold < nFolds; fold++)
 		for (EnsembleMLMethodFactory mlf: mlfs)
 		{
 
 			EvaluationTechnique et = null;
 			ChainParams fullLabel = new ChainParams(problem.getLabel(),etType,etf.getLabel(),mlf.getLabel(),agg.getLabel(),dataSetSize);
 			try
 			{
 				et = ArgParser.technique(etType,sizes,dataSetSize,fullLabel,mlf,etf,agg,dataLoader,maxIterations);
 			} catch (BadArgument e)
 			{
 				help();
 			}
 			for (double te: trainingErrors)
 			{
 				Evaluator ev = new Evaluator(et, dataLoader, te, selectionError, verbose,fold,maxIterations);
 				ev.getResults(fullLabel,te,reconnectCallback,chainId,noSQL);
 			}
 			if(!noSQL)
 			{
 				sqlConnection = reconnectCallback.connect();
 				statement = sqlConnection.createStatement();
 				cal = Calendar.getInstance();
 				statement.executeUpdate("UPDATE chains SET invalidated = 0, end = '" + sqlDateFormat.format(cal.getTime()) + "' WHERE id = " + chainId);
 			}
 		}
 	}
 	
 	public static void main(String[] args) throws WeightMismatchException, RequiresWeightedAggregatorException
 	{
 		FileLoader fileLoader = new FileLoader();
 		Options options = new Options();
 		CommandLineParser parser = new BasicParser();
 		options.addOption("o", "stdout", false, "output to stdout rather than SQL");
 		options.addOption("v", "verbose", false, "verbose");
 		CommandLine commandLine = null;
 		try {
 			commandLine = parser.parse(options, args);
 		} catch (ParseException e1) {
 			help();
 		}
 		noSQL = commandLine.hasOption("o");
 		String[] parsedArgs = commandLine.getArgs();
 		if (parsedArgs.length != 1) 
 		{
 			help();
 		} 
 		try 
 		{
 			Properties problemPropFile = new Properties();
 			try {
 				problemPropFile.load(fileLoader.openOrFind(parsedArgs[0]));
 			} catch (FileNotFoundException e) {
 				System.err.println("Could not find " + parsedArgs[0]);
 				help();
 			} catch (IOException e) {
 				help();
 			}
 			problem = ArgParser.problem(problemPropFile.getProperty("problem"));
 			nFolds = ArgParser.intSingle(problemPropFile.getProperty("folds"));
			activationThreshold = ArgParser.doubleSingle(problemPropFile.getProperty("neuron_invalidation_threshold"));
 			etType = problemPropFile.getProperty("ensemble_method");
 			sizes = ArgParser.intList(problemPropFile.getProperty("ensemble_sizes"));
 			dataSetSizes = ArgParser.intList(problemPropFile.getProperty("dataset_resampling_sizes"));
 			trainingErrors = ArgParser.doubleList(problemPropFile.getProperty("training_errors"));
 			etf = ArgParser.ETF(problemPropFile.getProperty("ensemble_training"));
 			maxIterations = ArgParser.intSingle(problemPropFile.getProperty("max_training_iterations"));
 			mlfs = ArgParser.MLFS(problemPropFile.getProperty("member_types"));
 			agg = ArgParser.AGG(problemPropFile.getProperty("aggregator"));
 			verbose = Boolean.parseBoolean(problemPropFile.getProperty("verbose")) || commandLine.hasOption("v");
 			selectionError = ArgParser.doubleSingle(problemPropFile.getProperty("selection_error"));
 			if (nFolds < 2) {throw new BadArgument();};
 			dataLoader = problem.getDataLoader(activationThreshold,nFolds);
 			targetRunCount = ArgParser.intSingle(problemPropFile.getProperty("max_runs"));
 			EXPERIMENT = ArgParser.intSingle(problemPropFile.getProperty("experiment_id"));
 		}
 		catch (FileNotFoundException e)
 		{
 			System.err.println("Could not create dataLoader - data file not found");
 		}
 		catch (helpers.ProblemDescriptionLoader.BadArgument e) 
 		{
 			System.err.println("Could not create dataLoader - perhaps the mapper_type property is wrong");
 			e.printStackTrace();
 		} catch (BadArgument e) {
 			help();
 		}
 		if(!noSQL)
 		{
 			try 
 			{
 				Class.forName("com.mysql.jdbc.Driver");
 			}
 			catch (ClassNotFoundException e) 
 			{
 				System.err.println("Could not find MySQL JDBC driver!");
 			}
 		}
 		try
 		{
             Properties prop = new Properties();
             prop.load(fileLoader.openOrFind("config.properties"));
             String dbhost = prop.getProperty("dbhost");
             dbuser = prop.getProperty("dbuser");
             dbpass = prop.getProperty("dbpass");
             String dbport = prop.getProperty("dbport");
             String dbname = prop.getProperty("dbname");
             dbconn = "jdbc:mysql://" + dbhost + ":" + dbport + "/" + dbname;
 			reconnectCallback = new DBConnect() {
 				public Connection connect() throws FileNotFoundException, IOException, SQLException {
 					if(noSQL)
 					{
 						return null;
 					}
 					else
 					{
 						return DriverManager.getConnection(dbconn, dbuser, dbpass);
 					}
 				}
 			};
 			
 			sqlConnection = reconnectCallback.connect();
 			loop();
 		} catch(SQLException e)
 	    {
 	      System.err.println(e.getMessage());
 	      e.printStackTrace();
 	    } catch (IOException e)
         {
             e.printStackTrace();
         }
 	    finally
 	    {
 	      try
 	      {
 	        if(sqlConnection != null)
 	          sqlConnection.close();
 	      }
 	      catch(SQLException e)
 	      {
 	        System.err.println(e);
 	      }
 	    }
 		System.exit(0);
 	}
 
 	private static void help()
 	{
 		System.err.println("Usage: Test <propfile>");
 		System.err.println("<propfile> should contain, for example:\n"
 						+ "	ensemble_method = {bagging,adaboost,stacking,dropout}\n"
 						+ " problem = problems/uci_haberman\n"
 						+ " ensemble_sizes = 1,2,n...\n"
 						+ " dataset_sizes = 100,200,n...\n"
 						+ " training_errors = 0.1,0.05,n...\n"
 						+ " folds = n\n"
 						+ " activation_threshold = 0.1\n"
 						+ " ensemble_training = rprop{-0.5}\n"
 						+ " member_types = mlp:n:sigmoid\n"
 						+ " aggregator = {averaging, majorityvoting, metaclassifier-mlp:n:sigmoid-rprop[0.5]}\n"
 						+ " verbose = {true,false}\n"
 						+ " selection_error = 0.25\n"
 						+ " max_runs = 3000\n"
 						+ " max_training_iterations = 1000\n"
 						+ " experiment_id = 2\n");
 		System.err.println("nFolds must be > 1");
 		System.exit(2);
 	}
 }
