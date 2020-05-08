 package info.plagiatsjaeger;
 
 import info.plagiatsjaeger.enums.ErrorCode;
 import info.plagiatsjaeger.interfaces.IComparer;
 import info.plagiatsjaeger.interfaces.IOnlineSearch;
 import info.plagiatsjaeger.interfaces.OnCompareFinishedListener;
 import info.plagiatsjaeger.interfaces.OnLinkFoundListener;
 import info.plagiatsjaeger.onlinesearch.OnlineSearch;
 import info.plagiatsjaeger.types.CompareResult;
 import info.plagiatsjaeger.types.Settings;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.concurrent.Callable;
 import java.util.concurrent.CancellationException;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import org.apache.log4j.Logger;
 
 import com.ibm.icu.text.SimpleDateFormat;
 
 
 /**
  * Steuerung fuer den gesammten Ablauf.
  * 
  * @author Andreas
  */
 public class Control
 {
 	/**
 	 * Dateipfad fuer die Dateien auf dem Server.
 	 */
 	public  static final String		ROOT_FILES				= ConfigReader.getPropertyString("ROOTFILES");
 	private static final Logger		_logger					= Logger.getLogger(Control.class.getName());
 	private static final int		SIZE_THREADPOOL			= ConfigReader.getPropertyInt("THREADPOOLSIZE");
 	private static final int		NUM_CHECKS_IF_PARSED	= ConfigReader.getPropertyInt("PARSECHECKS");
 	private static final int		TIME_BETWEEN_CHECK		= ConfigReader.getPropertyInt("TIMEBETWEENCHECKS"); // min
 
 	private Settings				_settings;
 	private ExecutorService			_threadPool				= Executors.newFixedThreadPool(SIZE_THREADPOOL);
 	private ArrayList<Future<Void>>	_futures				= new ArrayList<Future<Void>>();
 
 	private ExecutorService			_threadPoolSearch		= Executors.newFixedThreadPool(SIZE_THREADPOOL);
 	private ArrayList<Future<Void>>	_futuresSearch			= new ArrayList<Future<Void>>();
 
 	private double					_similarity				= 0.0;
 	
 	public Control(int rId)
 	{
 		if (rId > 0)
 		{
 			// Settings zu beginn laden
 			_settings = new MySqlDatabaseHelper().getSettings(rId);
 		}
 	}
 
 	/**
 	 * <b>Noch nicht implementiert!</b></br> Konvertiert eine Datei in das
 	 * normalisierte pjt-Format.
 	 * 
 	 * @param dId
 	 * @return
 	 */
 	public boolean startParsing(final int dId)
 	{
 		boolean result = false;
 		try
 		{
 			_logger.info("Servlet called: " + dId);
 			new Thread(new Runnable()
 			{
 				@Override
 				public void run()
 				{
 					try
 					{
 						_logger.info("Thread started: " + dId);
 						FileParser fileParser = new FileParser();
 						if (fileParser.parseFile(dId))
 						{
 							new MySqlDatabaseHelper().setDocumentAsParsed(dId);
 						}
 						else
 						{
 							// TODO: parseerror erfassen
 						}
 					}
 					catch (Exception e)
 					{
 						_logger.fatal(e.getMessage(), e);
 						e.printStackTrace();
 					}
 				}
 			}).start();
 			result = true;
 		}
 		catch (Exception e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 		}
 		return result;
 
 	}
 
 	/**
 	 * Startet eine Plagiatssuche zu dem uebergebenen Report.
 	 * 
 	 * @param rId
 	 */
 	public boolean startPlagiatsSearch(final int rId)
 	{
 		try
 		{
 			final MySqlDatabaseHelper mySqlDatabaseHelper = new MySqlDatabaseHelper();
 			final int intDocumentId = mySqlDatabaseHelper.getDocumentID(rId);
 			_logger.info("Document: " + intDocumentId);
 
 			if (intDocumentId != 0)
 			{
 				_logger.info("Check started");
 				new Thread(new Runnable()
 				{
 
 					@Override
 					public void run()
 					{
 						// Kontrolliert ob das Dokument schon geparsed wurde.
 						int numTries = 0;
 						while (numTries < NUM_CHECKS_IF_PARSED && !mySqlDatabaseHelper.getDocumentParseState(intDocumentId))
 						{
 							if (numTries <= 0)
 							{
 								// TODO in parser aufrufen
 								// Parsen laeuft noch.
 								new MySqlDatabaseHelper().setReportState(rId, ErrorCode.Parsing);
 							}
 							try
 							{
 								Thread.sleep(TIME_BETWEEN_CHECK * 1000);
 							}
 							catch (InterruptedException e)
 							{
 								_logger.fatal(e.getMessage(), e);
 								e.printStackTrace();
 							}
 							numTries = 0;
 						}
 						if (numTries < NUM_CHECKS_IF_PARSED)
 						{
 							_logger.info("Thread started!");
 							mySqlDatabaseHelper.setReportState(rId, ErrorCode.Checking);
 							startPlagiatsSearch(ROOT_FILES + intDocumentId + ".pjt", rId);
 						}
 						else
 						{
 							mySqlDatabaseHelper.setReportState(rId, ErrorCode.Error);
 						}
 					}
 				}).start();
 				return true;
 			}
 		}
 		catch (Exception e)
 		{
 			_logger.fatal(e.getMessage(), e);
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	/**
 	 * Fuehrt eine Plagiatssuche zu dem uebergebenen Dokument durch.
 	 * 
 	 * @param filePath
 	 * @param rId
 	 */
 	public void startPlagiatsSearch(String filePath, final int rId)
 	{
 
 		final String strSourceText = SourceLoader.loadFile(filePath);
 		if (_settings.getCheckWWW())
 		{
 			final IOnlineSearch iOnlineSearch = new OnlineSearch(_settings);
 			iOnlineSearch.setOnLinkFoundListener(new OnLinkFoundListener()
 			{
 				@Override
 				public void onLinkFound(final String link)
 				{
 					_logger.info("OnLinkFound: " + link);
 					_futures.add(_threadPool.submit(new Callable<Void>()
 					{
 						@Override
 						public Void call()
 						{
 							MySqlDatabaseHelper mySqlDatabaseHelper = new MySqlDatabaseHelper();
 
 							String dbLink = SourceLoader.cleanUrl(mySqlDatabaseHelper.loadDocumentURL(mySqlDatabaseHelper.getDocumentID(rId)));
 
 							if (!dbLink.equals(SourceLoader.cleanUrl(link)))
 							{
 								_logger.info("Thread for Link started: " + link);
 								compare(rId, strSourceText, link, 0);
 							}
 							return null;
 						}
 					}));
 				}
 			});
 			_futuresSearch.add(_threadPoolSearch.submit(new Callable<Void>()
 			{
 				@Override
 				public Void call()
 				{
 					iOnlineSearch.searchAsync(strSourceText);
 					return null;
 				}
 			}));
 		}
 		ArrayList<Integer> localFolders = _settings.getLocalFolders();
 		if (localFolders != null)
 		{
 			int docId = new MySqlDatabaseHelper().getDocumentID(rId);
 			_logger.info("Check localFolders");
 			for (final int i : localFolders)
 			{
 				_logger.info("CheckDoc: " + docId);
 				_logger.info("NextDoc:  " + i);
 				if (i != docId)
 				{
 					_logger.info("Naechstes File: " + i);
 					_futures.add(_threadPool.submit(new Callable<Void>()
 					{
 						@Override
 						public Void call()
 						{
 							_logger.info("Thread for File started: " + i + ".pjt");
 							compare(rId, strSourceText, "", i);
 							return null;
 						}
 					}));
 				}
 			}
 		}
 		boolean succesful = true;
 		int numThreadsFinished = 0;
 		// Sicherstellen dass die Suche beendet ist.
 		for (Future<Void> future : _futuresSearch)
 		{
 			try
 			{
 				if (future != null)
 				{
 					future.get();
 					numThreadsFinished++;
 					_logger.info(numThreadsFinished + " searches finished");
 				}
 			}
 			catch (CancellationException e)
 			{
 				_logger.fatal(e.getMessage(), e);
 				e.printStackTrace();
 				succesful = false;
 			}
 			catch (InterruptedException e)
 			{
 				_logger.fatal(e.getMessage(), e);
 				e.printStackTrace();
 				succesful = false;
 			}
 			catch (ExecutionException e)
 			{
 				_logger.fatal(e.getMessage(), e);
 				e.printStackTrace();
 				succesful = false;
 			}
 		}
 
 		// Sicherstellen, dass alle Vergleiche fertiggestellt sind
 		numThreadsFinished = 0;
 		for (Future<Void> future : _futures)
 		{
 			try
 			{
 				if (future != null)
 				{
 					future.get();
 					numThreadsFinished++;
 					_logger.info(numThreadsFinished + " compare finished");
 				}
 			}
 			catch (CancellationException e)
 			{
 				_logger.fatal(e.getMessage(), e);
 				e.printStackTrace();
 				succesful = false;
 			}
 			catch (InterruptedException e)
 			{
 				_logger.fatal(e.getMessage(), e);
 				e.printStackTrace();
 				succesful = false;
 			}
 			catch (ExecutionException e)
 			{
 				_logger.fatal(e.getMessage(), e);
 				e.printStackTrace();
 				succesful = false;
 			}
 		}
 		MySqlDatabaseHelper mySqlDatabaseHelper = new MySqlDatabaseHelper();
 		if (succesful)
 		{
 			mySqlDatabaseHelper.setReportState(rId, ErrorCode.Succesful);
 			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 			mySqlDatabaseHelper.finishReport(rId, _similarity, simpleDateFormat.format(Calendar.getInstance().getTime()));
 			if ((_similarity * 100) > _settings.getThreshold())
 			{
 				_logger.info("Send Mail for Report: " + rId);
 				new SourceLoader().loadURL("http://192.168.4.28/sendmail.php?rID=" + rId, false, false);
 			}
 			_logger.info("Report " + rId + " fertiggestellt!");
 
 		}
 		if (!succesful || (localFolders == null && !_settings.getCheckWWW()))
 		{
 			mySqlDatabaseHelper.setReportState(rId, ErrorCode.Error);
 		}
 	}
 
 	/**
 	 * Vergleicht den sourceText entweder mit einer Internetseite oder einem
 	 * lokalen Dokument.
 	 * 
 	 * @param rId
 	 * @param checkText
 	 * @param link
 	 * @param docId
 	 */
 	private void compare(int rId, String checkText, String link, int docId)
 	{
 		IComparer comparer = new MyComparer(rId, _settings);
 		comparer.setOnCompareFinishedListener(new OnCompareFinishedListener()
 		{
 			@Override
 			public void onCompareResultFound(ArrayList<CompareResult> compareResult, int docId)
 			{
 				MySqlDatabaseHelper mySqlDatabaseHelper = new MySqlDatabaseHelper();
 				mySqlDatabaseHelper.insertCompareResults(compareResult, docId);
 				calcSimilarity(compareResult);
 			}
 
 			@Override
 			public void onCompareResultFound(ArrayList<CompareResult> compareResult, String link)
 			{
 				MySqlDatabaseHelper mySqlDatabaseHelper = new MySqlDatabaseHelper();
 				mySqlDatabaseHelper.insertCompareResults(compareResult, link);
 				calcSimilarity(compareResult);
 			}
 		});
 		if (docId <= 0)
 		{
 			_logger.info("Compare To URL: " + link);
 			comparer.compareText(checkText, new SourceLoader().loadURL(link), link);
 		}
 		else
 		{
 			_logger.info("Compare To Document: " + docId);
			comparer.compareText(checkText, SourceLoader.loadFile(ROOT_FILES + docId + ".pjt", false), docId);
 		}
 	}
 
 	/**
 	 * Berechnet die Gesammtaehnlichkeit
 	 * 
 	 * @param compareResults
 	 */
 	private synchronized void calcSimilarity(ArrayList<CompareResult> compareResults)
 	{
 		for (CompareResult result : compareResults)
 		{
 			if (_similarity < result.getSimilarity())
 			{
 				_similarity = result.getSimilarity();
 			}
 		}
 	}
 }
