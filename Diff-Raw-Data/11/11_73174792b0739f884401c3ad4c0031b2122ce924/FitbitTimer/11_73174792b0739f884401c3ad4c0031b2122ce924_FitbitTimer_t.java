 package servlets;
 
 import health.database.DAO.DataSummaryDAO;
 import health.database.DAO.DatastreamDAO;
 import health.database.DAO.Ext_API_Info_DAO;
 import health.database.DAO.HealthDataStreamDAO;
 import health.database.DAO.SleepDataDAO;
 import health.database.DAO.nosql.HBaseDatapointDAO;
 import health.database.models.DataSummary;
 import health.database.models.Datastream;
 import health.database.models.ExternalApiInfo;
 import health.database.models.FitbitLog;
 import health.database.models.SleepDataSummary;
 import health.hbase.models.HBaseDataImport;
 import health.input.jsonmodels.JsonDataPoints;
 import health.input.jsonmodels.JsonDataValues;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 import org.joda.time.LocalDateTime;
 import org.joda.time.LocalTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.DateTimeFormatterBuilder;
 import org.joda.time.format.ISODateTimeFormat;
 
 import server.exception.ErrorCodeException;
 import util.AllConstants;
 import util.DateUtil;
 
 import com.fitbit.api.FitbitAPIException;
 import com.fitbit.api.client.FitbitAPIEntityCache;
 import com.fitbit.api.client.FitbitApiClientAgent;
 import com.fitbit.api.client.FitbitApiCredentialsCache;
 import com.fitbit.api.client.FitbitApiCredentialsCacheMapImpl;
 import com.fitbit.api.client.FitbitApiEntityCacheMapImpl;
 import com.fitbit.api.client.FitbitApiSubscriptionStorage;
 import com.fitbit.api.client.FitbitApiSubscriptionStorageInMemoryImpl;
 import com.fitbit.api.client.LocalUserDetail;
 import com.fitbit.api.client.service.FitbitAPIClientService;
 import com.fitbit.api.common.model.devices.Device;
 import com.fitbit.api.common.model.sleep.Sleep;
 import com.fitbit.api.common.model.sleep.SleepLog;
 import com.fitbit.api.common.model.sleep.SleepSummary;
 import com.fitbit.api.common.model.timeseries.Data;
 import com.fitbit.api.common.model.timeseries.IntradayData;
 import com.fitbit.api.common.model.timeseries.IntradaySummary;
 import com.fitbit.api.common.model.timeseries.TimeSeriesResourceType;
 import com.fitbit.api.model.FitbitUser;
 
 /**
  * Servlet implementation class FitbitTimer
  */
 @WebServlet(name = "FitbitTimer", urlPatterns = { "/FitbitTimer" }
 // ,
 // loadOnStartup = 0
 )
 // loadOnStartup=0
 public class FitbitTimer extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private static Timer timer = null;
 	private String apiBaseUrl;
 	private String fitbitSiteBaseUrl;
 	private String clientConsumerKey;
 	private String clientSecret;
 	public static final String OAUTH_TOKEN = "oauth_token";
 	public static final String OAUTH_VERIFIER = "oauth_verifier";
 	public static String timerStatus="off";
 	private FitbitAPIEntityCache entityCache = new FitbitApiEntityCacheMapImpl();
 	private FitbitApiCredentialsCache credentialsCache = new FitbitApiCredentialsCacheMapImpl();
 	private FitbitApiSubscriptionStorage subscriptionStore = new FitbitApiSubscriptionStorageInMemoryImpl();
 	private static FitbitAPIClientService<FitbitApiClientAgent> apiClientService;
 	private DatastreamDAO dstreamDao = new DatastreamDAO();
 	private HealthDataStreamDAO healthDSdao = new HealthDataStreamDAO();
 	private HBaseDatapointDAO datapointDao = null;
 	private SleepDataDAO sleepDataDao = new SleepDataDAO();
 	private static final int apiRateLimit = 150;
 
 	@Override
 	public void destroy() {
 		// TODO Auto-generated method stub
 		super.destroy();
 		timer.cancel();
 		timer = null;
 
 	}
 
 	@Override
 	public void init() throws ServletException {
 		// TODO Auto-generated method stub
 		super.init();
 		System.out.println(getClass().getClassLoader().toString());
 		try {
 			Properties properties = new Properties();
 			properties.load(getClass().getClassLoader().getResourceAsStream(
 					"config.properties"));
 
 			System.out.println(getClass().getClassLoader().toString());
 			properties.load(getClass().getClassLoader().getResourceAsStream(
 					"config.properties"));
 			System.out.println("------After getting Property File-------");
 			apiBaseUrl = properties.getProperty("apiBaseUrl");
 			System.out.println("------Getting apiBaseUrl Property File-------");
 			fitbitSiteBaseUrl = properties.getProperty("fitbitSiteBaseUrl");
 			System.out
 					.println("------Getting fitbitSiteBaseUrl Property File-------");
 			clientConsumerKey = properties.getProperty("clientConsumerKey");
 			System.out
 					.println("------Getting clientConsumerKey Property File-------");
 			clientSecret = properties.getProperty("clientSecret");
 			System.out
 					.println("------Getting clientSecret Property File-------");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		try {
 			System.out.println("------Initializing DataPointDAO-------");
 			datapointDao = new HBaseDatapointDAO();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		System.out.println("before Fitbit Timer.......");
 		apiClientService = new FitbitAPIClientService<FitbitApiClientAgent>(
 				new FitbitApiClientAgent(apiBaseUrl, fitbitSiteBaseUrl,
 						credentialsCache), clientConsumerKey, clientSecret,
 				credentialsCache, entityCache, subscriptionStore);
 		System.out.println("starting Fitbit Timer.......");
 		// timer = new Timer();
 		// timer.schedule(new RemindTask(), 0, // initial delay
 		// 5 * 60 * 1000); // subsequent rate
 
 	}
 
 	class RemindTask extends TimerTask {
 		Ext_API_Info_DAO extDao = new Ext_API_Info_DAO();
 
 		public void run() {
 			timerStatus="on";
 			List<ExternalApiInfo> apiinfoList = extDao
 					.getExt_API_INFO_List(AllConstants.ExternalAPIConsts.fitbit_device);
 
 			for (ExternalApiInfo apiinfo : apiinfoList) {
 				Date now = new Date();
 				int apiCounter = apiinfo.getApiCounter();
 				if (apiCounter > (apiRateLimit - 10)
 						&& apiinfo.getLateDataUpdate() != null
 						&& (now.getTime() - apiinfo.getLateDataUpdate()
 								.getTime()) < 3600000L) {
 					System.out.println("over limit: " + apiinfo.getLoginID()
 							+ "," + apiCounter);
 					continue;
 				}
 				if (apiinfo.getLateDataUpdate() != null
 						&& (now.getTime() - apiinfo.getLateDataUpdate()
 								.getTime()) > 3600000L) {
 					apiCounter = 0;
 					System.out.println("Counter reset" + apiinfo.getLoginID());
 					apiinfo.setApiCounter(apiCounter);
 					apiinfo.setLateDataUpdate(new Date());
 					extDao.Update_A_ExtAPI(apiinfo);
 				}
 				try {
 
 					System.out.println("-------Fetching for--------"
 							+ apiinfo.getLoginID());
 					List<Device> deviceList = apiClientService.getClient()
 							.getDevices(
 									new LocalUserDetail(apiinfo.getLoginID(),
 											apiinfo.getExtId()));
 					System.out.println("API WORKS--"+new Date());
 					if (deviceList.size() != 1) {
 						System.out.println("----------------ERROR----------"
 								+ apiinfo.getLoginID());
 						continue;
 					}
 					DateTimeFormatter formatter = new DateTimeFormatterBuilder()
 							.append(DateTimeFormat.forPattern(
 									"yyyy-MM-dd'T'HH:mm:ss.SSS").getParser())
 							.toFormatter();
 
 					LocalDateTime lastSyncTime = LocalDateTime.parse(deviceList
 							.get(0).getLastSyncTime(), formatter);
 					long one_month = 1000 * 60 * 60 * 24 * 30L;
 					long day = 1000 * 60 * 60 * 24L;
 					// long calculate=start.getTime()-one_month;
 					// start=new Date(calculate);
 
 					// long calculate = start.getTime() - 30 * day;
 					// Date temp_date = new Date(calculate);
 					FitbitLog fitbitlog = extDao.getLastFetchFitbitLog(apiinfo
 							.getLoginID());
 					if (fitbitlog == null) {
 						Date start = new Date();
 						for (int i = 30; i > 0; i--) {
 							if (apiCounter < apiRateLimit - 10) {
 								long calculate = start.getTime() - i * day;
 								Date temp_date = new Date(calculate);
 								System.out
 										.println("Getting " + temp_date + " ");
 								// FitbitLog
 								// fitbitlog=extDao.getFitbitFetch(apiinfo.getLoginID(),
 								// temp_date);
 								LocalDate fitbitDate = LocalDate
 										.fromDateFields(temp_date);
 								importStepsData(apiinfo, fitbitDate,
 										apiClientService);
 								apiCounter++;
 								importFloorsData(apiinfo, fitbitDate,
 										apiClientService);
 								apiCounter++;
 								import_Calories_burned_Data(apiinfo,
 										fitbitDate, apiClientService);
 								apiCounter++;
 								apiCounter= import_Sleep_Data(apiinfo, fitbitDate,
 							     apiClientService,apiCounter);
 								now = new Date();
 //								apiinfo.setLateDataUpdate(now);
 								apiinfo.setApiCounter(apiCounter);
 								extDao.Update_A_ExtAPI(apiinfo);
 								fitbitlog = new FitbitLog();
 								fitbitlog.setLoginID(apiinfo.getLoginID());
 								fitbitlog.setFetchTime(now);
 								fitbitlog.setDate(fitbitDate.toDate());
 								if (fitbitDate.isEqual(lastSyncTime
 										.toLocalDate())) {
 									fitbitlog.setFinished(false);
 								} else if (fitbitDate.isAfter(lastSyncTime
 										.toLocalDate())) {
 									fitbitlog.setFinished(false);
 								} else {
 									fitbitlog.setFinished(true);
 								}
 								fitbitlog
 										.setFetchData("steps;floors;calories;sleep;");
 								extDao.create_FitbitLog(fitbitlog);
 							}
 
 						}
 					} else {
 						if (!fitbitlog.isFinished) {
 							LocalDate fitbitDate = LocalDate
 									.fromDateFields(fitbitlog.getDate());
 							importStepsData(apiinfo, fitbitDate,
 									apiClientService);
 							apiCounter++;
 							importFloorsData(apiinfo, fitbitDate,
 									apiClientService);
 							apiCounter++;
 							import_Calories_burned_Data(apiinfo, fitbitDate,
 									apiClientService);
 							apiCounter++;
 							apiCounter= import_Sleep_Data(apiinfo, fitbitDate,
 								     apiClientService,apiCounter);
 							apiCounter = apiCounter + 2;
 //							apiinfo.setLateDataUpdate(now);
 
 							fitbitlog.setFetchTime(now);
							System.out.println("last Sync:"+lastSyncTime.toLocalDate().plusDays(1));
							System.out.println("fitbitDate:"+fitbitDate);
							if (fitbitDate.isBefore(lastSyncTime.toLocalDate().plusDays(1))) {								
 								fitbitlog.setFinished(true);
 							}else{
 								fitbitlog.setFinished(false);
 							}
 							fitbitlog
 									.setFetchData("steps;floors;calories;sleep;");
 
 							LocalDate endDate = LocalDate.now();
 							LocalDate startDate = endDate.minusDays(30);
 							importdistanceSummary(apiinfo, startDate, endDate,
 									apiClientService);
 							apiCounter++;
 							extDao.update_FitbitLog(fitbitlog);
 
 							apiinfo.setApiCounter(apiCounter);
 							extDao.Update_A_ExtAPI(apiinfo);
 						} else {
 							LocalDate tempDate = LocalDate
 									.fromDateFields(fitbitlog.getDate());
 							tempDate = tempDate.plusDays(1);
 
 							for (int i = 0; i < 5; i++) {
 								
 								if ((apiCounter < apiRateLimit)
 										&& 
 							(!tempDate.isAfter(lastSyncTime.toLocalDate())))  {
 									LocalDate fitbitDate = tempDate;
 									importStepsData(apiinfo, fitbitDate,
 											apiClientService);
 									apiCounter++;
 									importFloorsData(apiinfo, fitbitDate,
 											apiClientService);
 									apiCounter++;
 									import_Calories_burned_Data(apiinfo,
 											fitbitDate, apiClientService);
 									apiCounter++;
 									apiCounter= import_Sleep_Data(apiinfo, fitbitDate,
 										     apiClientService,apiCounter);
 									apiCounter = apiCounter + 2;
 //									apiinfo.setLateDataUpdate(now);
 									apiinfo.setApiCounter(apiCounter);
 									extDao.Update_A_ExtAPI(apiinfo);
 									FitbitLog log = new FitbitLog();
 									log.setLoginID(apiinfo.getLoginID());
 									log.setFetchTime(now);
 									log.setDate(fitbitDate.toDate());
 									
 									if (fitbitDate.isEqual(lastSyncTime
 											.toLocalDate())) {
 										log.setFinished(false);
 									} else if (fitbitDate.isAfter(lastSyncTime
 											.toLocalDate())) {
 										log.setFinished(false);
 									} else {
 										log.setFinished(true);
 									}
 									log.setFetchData("steps;floors;calories;sleep;");
 									extDao.create_FitbitLog(log);
 									tempDate = tempDate.plusDays(1);
 								}
 							}
 						}
 
 					}
 
 				} catch (Exception ex) {
 					ex.printStackTrace();
 				}
 			}
 
 		}
 	}
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public FitbitTimer() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		String command = request.getParameter("command");
 		PrintWriter out = response.getWriter();
 
 		if (command == null) {
 			out.write("Timer Status:"+timerStatus);
 			return;
 		}
 		if (command.equalsIgnoreCase("on")) {
 			if (timer == null) {
 				timer = new Timer();
 				timer.schedule(new RemindTask(), 0, // initial delay
 						5 * 60 * 1000); // subsequent rate
 			}
 					out.write("Timer is on");
 			
 			return;
 		} else if (command.equalsIgnoreCase("off")) {
 			if (timer != null) {
 				timer.cancel();
 				timer = null;
 			}
 			timerStatus="off";
 			out.write("Timer is off");
 			return;
 		} else {
 			out.write("no command");
 			return;
 		}
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 
 		String command = request.getParameter("command");
 		PrintWriter out = response.getWriter();
 
 		if (command == null) {
 			out.write("no command");
 			return;
 		}
 		if (command.equalsIgnoreCase("on")) {
 			if (timer == null) {
 				timer = new Timer();
 				timer.schedule(new RemindTask(), 0, // initial delay
 						5 * 60 * 1000); // subsequent rate
 			}
 			out.write("Timer is on");
 			return;
 		} else if (command.equalsIgnoreCase("off")) {
 			timer.cancel();
 			timer = null;
 			out.write("Timer is off");
 			return;
 		} else {
 			out.write("no command");
 			return;
 		}
 	}
 
 	public void importStepsData(ExternalApiInfo apiinfo, LocalDate fitbitDate,
 			FitbitAPIClientService<FitbitApiClientAgent> apiClientService)
 			throws FitbitAPIException, NumberFormatException,
 			ErrorCodeException, IOException, ParseException {
 		FitbitUser fitbitUser = new FitbitUser(apiinfo.getExtId());
 
 		IntradaySummary stepSummary = apiClientService.getClient()
 				.getIntraDayTimeSeries(
 						new LocalUserDetail(apiinfo.getLoginID(),
 								apiinfo.getExtId()), fitbitUser,
 						TimeSeriesResourceType.STEPS, fitbitDate);
 		if (stepSummary == null || stepSummary.getIntradayDataset() == null
 				|| stepSummary.getIntradayDataset().getDataset() == null) {
 			System.out.println("-------No record-----" + fitbitDate.toString());
 			return;
 		}
 		// System.out.println("Size Summary:"
 		// + stepSummary.getIntradayDataset().getDataset().size());
 		List<IntradayData> intraDataList = stepSummary.getIntradayDataset()
 				.getDataset();
 		HBaseDataImport hbaseImport = new HBaseDataImport();
 
 		List<JsonDataPoints> jdataPList = new ArrayList<JsonDataPoints>();
 		Datastream ds = healthDSdao.getDefaultDatastreamOfType(
 				apiinfo.getLoginID(), "steps");
 
 		double summaryValue = 0;
 //		System.out.println("totalSize:" + intraDataList.size());
 		for (IntradayData data : intraDataList) {
 			Calendar cal = Calendar.getInstance(DateUtil.UTC);
 			cal.setTime(fitbitDate.toDate());
 			LocalTime tempLocalTime = LocalTime.parse(data.getTime());
 			cal.set(Calendar.HOUR_OF_DAY, tempLocalTime.getHourOfDay());
 			cal.set(Calendar.MINUTE, tempLocalTime.getMinuteOfHour());
 			cal.set(Calendar.SECOND, tempLocalTime.getSecondOfMinute());
		//	System.out.println("Steps: " + cal.getTime() + " " + data.getValue());
 			JsonDataPoints jdatapoint = new JsonDataPoints();
 			jdatapoint.setAt(Long.toString(cal.getTime().getTime()));
 			JsonDataValues jvalue = new JsonDataValues();
 			jvalue.setUnit_id(ds.getDatastreamUnitsList().get(0).getUnitID());
 			jvalue.setVal(Double.toString(data.getValue()));
 			List<JsonDataValues> jsonvalueList = new ArrayList<JsonDataValues>();
 			jsonvalueList.add(jvalue);
 
 			jdatapoint.setValue_list(jsonvalueList);
 			jdataPList.add(jdatapoint);
 			summaryValue = summaryValue + data.getValue();
 		}
 
 		hbaseImport.setData_points(jdataPList);
 		hbaseImport.setDatastream_id(ds.getStreamId());
 		datapointDao.importDatapointsDatapoints(hbaseImport);
 		DataSummary: {
 			DataSummary datasummry = new DataSummary();
 			DataSummaryDAO dsummaryDao = new DataSummaryDAO();
 			datasummry.setValue(summaryValue);
 			datasummry.setLoginID(ds.getOwner());
 			datasummry.setDstreamID(ds.getStreamId());
 			datasummry.setDate(fitbitDate.toDate());
 			datasummry.setTitle(ds.getTitle());
 			dsummaryDao.create_A_DataSummary(datasummry);
 		}
 	}
 
 	public void importFloorsData(ExternalApiInfo apiinfo, LocalDate fitbitDate,
 			FitbitAPIClientService<FitbitApiClientAgent> apiClientService)
 			throws FitbitAPIException, NumberFormatException,
 			ErrorCodeException, IOException, ParseException {
 		FitbitUser fitbitUser = new FitbitUser(apiinfo.getExtId());
 
 		IntradaySummary stepSummary = apiClientService.getClient()
 				.getIntraDayTimeSeries(
 						new LocalUserDetail(apiinfo.getLoginID(),
 								apiinfo.getExtId()), fitbitUser,
 						TimeSeriesResourceType.FLOORS, fitbitDate);
 		if (stepSummary == null || stepSummary.getIntradayDataset() == null
 				|| stepSummary.getIntradayDataset().getDataset() == null) {
 			System.out.println("-------No record-----" + fitbitDate.toString());
 			return;
 		}
 		// System.out.println("Size Summary:"
 		// + stepSummary.getIntradayDataset().getDataset().size());
 		List<IntradayData> intraDataList = stepSummary.getIntradayDataset()
 				.getDataset();
 		HBaseDataImport hbaseImport = new HBaseDataImport();
 
 		List<JsonDataPoints> jdataPList = new ArrayList<JsonDataPoints>();
 		Datastream ds = healthDSdao.getDefaultDatastreamOfType(
 				apiinfo.getLoginID(), "floor_climbed");
 		double summaryValue = 0;
 		for (IntradayData data : intraDataList) {
 			Calendar cal = Calendar.getInstance(DateUtil.UTC);
 			cal.setTime(fitbitDate.toDate());
 			LocalTime tempLocalTime = LocalTime.parse(data.getTime());
 			cal.set(Calendar.HOUR_OF_DAY, tempLocalTime.getHourOfDay());
 			cal.set(Calendar.MINUTE, tempLocalTime.getMinuteOfHour());
 			cal.set(Calendar.SECOND, tempLocalTime.getSecondOfMinute());
		//	 System.out.println("floors: "+cal.getTime()+" "+data.getValue());
 			JsonDataPoints jdatapoint = new JsonDataPoints();
 			jdatapoint.setAt(Long.toString(cal.getTime().getTime()));
 			JsonDataValues jvalue = new JsonDataValues();
 			jvalue.setUnit_id(ds.getDatastreamUnitsList().get(0).getUnitID());
 			jvalue.setVal(Double.toString(data.getValue()));
 			List<JsonDataValues> jsonvalueList = new ArrayList<JsonDataValues>();
 			jsonvalueList.add(jvalue);
 			jdatapoint.setValue_list(jsonvalueList);
 			jdataPList.add(jdatapoint);
 			summaryValue = summaryValue + data.getValue();
 		}
 		hbaseImport.setData_points(jdataPList);
 		hbaseImport.setDatastream_id(ds.getStreamId());
 		datapointDao.importDatapointsDatapoints(hbaseImport);
 		DataSummary: {
 			DataSummary datasummry = new DataSummary();
 			DataSummaryDAO dsummaryDao = new DataSummaryDAO();
 			datasummry.setValue(summaryValue);
 			datasummry.setLoginID(ds.getOwner());
 			datasummry.setDstreamID(ds.getStreamId());
 			datasummry.setDate(fitbitDate.toDate());
 			datasummry.setTitle(ds.getTitle());
 			dsummaryDao.create_A_DataSummary(datasummry);
 		}
 	}
 
 	public void import_Calories_burned_Data(ExternalApiInfo apiinfo,
 			LocalDate fitbitDate,
 			FitbitAPIClientService<FitbitApiClientAgent> apiClientService)
 			throws FitbitAPIException, NumberFormatException,
 			ErrorCodeException, IOException, ParseException {
 		FitbitUser fitbitUser = new FitbitUser(apiinfo.getExtId());
 
 		IntradaySummary stepSummary = apiClientService.getClient()
 				.getIntraDayTimeSeries(
 						new LocalUserDetail(apiinfo.getLoginID(),
 								apiinfo.getExtId()), fitbitUser,
 						TimeSeriesResourceType.CALORIES_OUT, fitbitDate);
 		if (stepSummary == null || stepSummary.getIntradayDataset() == null
 				|| stepSummary.getIntradayDataset().getDataset() == null) {
 			System.out.println("-------No record-----" + fitbitDate.toString());
 			return;
 		}
 		// System.out.println("Size Summary:"
 		// + stepSummary.getIntradayDataset().getDataset().size());
 		List<IntradayData> intraDataList = stepSummary.getIntradayDataset()
 				.getDataset();
 		HBaseDataImport hbaseImport = new HBaseDataImport();
 
 		List<JsonDataPoints> jdataPList = new ArrayList<JsonDataPoints>();
 		Datastream ds = healthDSdao.getDefaultDatastreamOfType(
 				apiinfo.getLoginID(), "calories_burned");
 		double summaryValue = 0;
 		for (IntradayData data : intraDataList) {
 			Calendar cal = Calendar.getInstance(DateUtil.UTC);
 			cal.setTime(fitbitDate.toDate());
 			LocalTime tempLocalTime = LocalTime.parse(data.getTime());
 			cal.set(Calendar.HOUR_OF_DAY, tempLocalTime.getHourOfDay());
 			cal.set(Calendar.MINUTE, tempLocalTime.getMinuteOfHour());
 			cal.set(Calendar.SECOND, tempLocalTime.getSecondOfMinute());
 			// System.out.println(" "+cal.getTime()+" "+data.getValue());
 			JsonDataPoints jdatapoint = new JsonDataPoints();
 			jdatapoint.setAt(Long.toString(cal.getTime().getTime()));
 			JsonDataValues jvalue = new JsonDataValues();
 			jvalue.setUnit_id(ds.getDatastreamUnitsList().get(0).getUnitID());
 			jvalue.setVal(Double.toString(data.getValue()));
 			List<JsonDataValues> jsonvalueList = new ArrayList<JsonDataValues>();
 			jsonvalueList.add(jvalue);
 			jdatapoint.setValue_list(jsonvalueList);
 			jdataPList.add(jdatapoint);
 			summaryValue = summaryValue + data.getValue();
 		}
 		hbaseImport.setData_points(jdataPList);
 		hbaseImport.setDatastream_id(ds.getStreamId());
 		datapointDao.importDatapointsDatapoints(hbaseImport);
 		DataSummary: {
 			DataSummary datasummry = new DataSummary();
 			DataSummaryDAO dsummaryDao = new DataSummaryDAO();
 			datasummry.setValue(summaryValue);
 			datasummry.setLoginID(ds.getOwner());
 			datasummry.setDstreamID(ds.getStreamId());
 			datasummry.setDate(fitbitDate.toDate());
 			datasummry.setTitle(ds.getTitle());
 			dsummaryDao.create_A_DataSummary(datasummry);
 		}
 	}
 
 	public int import_Sleep_Data(ExternalApiInfo apiinfo, LocalDate fitbitDate,
 			FitbitAPIClientService<FitbitApiClientAgent> apiClientService,
 			int apiCounter) throws FitbitAPIException, NumberFormatException,
 			ErrorCodeException, IOException, ParseException {
 		FitbitUser fitbitUser = new FitbitUser(apiinfo.getExtId());
 
 		Sleep sleep = apiClientService.getClient().getSleep(
 				new LocalUserDetail(apiinfo.getLoginID(), apiinfo.getExtId()),
 				fitbitUser, fitbitDate);
 		apiCounter++;
 		if (sleep == null) {
 			System.out.println("null sleep data");
 			return apiCounter;
 		}
 
 		SleepSummary sleepSummary = sleep.getSummary();
 		Date now = new Date();
 		if (sleepSummary != null && sleepSummary.getTotalSleepRecords() > 0) {
 			// exist sleep record
 			List<SleepLog> sleepLogList = sleep.getSleepLogs();
 			List<SleepDataSummary> sleepdataList = new ArrayList<SleepDataSummary>();
 			for (SleepLog log : sleepLogList) {
 
 				DateTimeFormatter formatter = new DateTimeFormatterBuilder()
 						.append(DateTimeFormat.forPattern(
 								"yyyy-MM-dd'T'HH:mm:ss.SSS").getParser())
 						.toFormatter();
 
 				LocalDateTime startTime = LocalDateTime.parse(
 						log.getStartTime(), formatter);
 
 				// LocalDate startDate=LocalDate.parse(log.getStartTime(),fmt);
 				LocalDateTime endTime = startTime.plusMillis((int) log
 						.getDuration());
 
 				HBaseDataImport hbaseImport = new HBaseDataImport();
 				List<JsonDataPoints> jdataPList = new ArrayList<JsonDataPoints>();
 				Datastream ds = healthDSdao.getDefaultDatastreamOfType(
 						apiinfo.getLoginID(), "sleep");
 
 				// Following is for creating sleep data summaries
 				SleepDataSummary sleepdatasummary = new SleepDataSummary();
 				sleepdatasummary.setDate(fitbitDate.toDate());
 				sleepdatasummary.setStartTime(startTime.toDate());
 				sleepdatasummary.setDstreamID(ds.getStreamId());
 				sleepdatasummary.setLoginID(ds.getOwner());
 				sleepdatasummary.setEfficiency(log.getEfficiency());
 				sleepdatasummary.setAwakeningCount(log.getAwakeningsCount());
 
 				sleepdatasummary.setEndtime(startTime.plusMillis(
 						(int) log.getDuration()).toDate());
 				sleepdatasummary.setInBedMinutes(log.getTimeInBed());
 				sleepdatasummary.setMinutesAfterWakeup(log
 						.getMinutesAfterWakeup());
 				sleepdatasummary.setMinutesAsleep(log.getMinutesAsleep());
 				sleepdatasummary.setMinutesAwake(log.getMinutesAwake());
 				sleepdatasummary.setMinutesToFallAsleep(log
 						.getMinutesToFallAsleep());
 				sleepdatasummary.setUpdated(now);
 				sleepdatasummary.setTotalSleepRecords(sleepSummary
 						.getTotalSleepRecords());
 				sleepdataList.add(sleepdatasummary);
 				// end
 
 				// same date sleep
 				if (startTime.toLocalDate().isEqual(endTime.toLocalDate())) {
 					System.out.println("****same Date Sleep****"
 							+ startTime.toLocalDate().toString());
 					IntradaySummary calroiesSummary = apiClientService
 							.getClient().getIntraDayTimeSeries(
 									new LocalUserDetail(apiinfo.getLoginID(),
 											apiinfo.getExtId()), fitbitUser,
 									TimeSeriesResourceType.CALORIES_OUT,
 									startTime.toLocalDate(),
 									startTime.toLocalTime(),
 									endTime.toLocalTime());
 					apiCounter++;
 					if (calroiesSummary == null
 							|| calroiesSummary.getIntradayDataset() == null
 							|| calroiesSummary.getIntradayDataset()
 									.getDataset() == null) {
 						System.out.println("-------No record-----"
 								+ startTime.toLocalDate().toString());
 						return apiCounter;
 					}
 					//
 					List<IntradayData> intraDataList = calroiesSummary
 							.getIntradayDataset().getDataset();
 					int counter = 0;
 					int end = intraDataList.size() - 1;
 					
 					double baseCalories = 5000;
 					for (IntradayData data : intraDataList) {
 						if (data.getValue() < baseCalories) {
 							baseCalories = data.getValue();
 						}
 					}
 					System.out.println(apiinfo.getLoginID()+":baseCalories:"+baseCalories);
 					
 					double summaryValue = 0;
 					for (IntradayData data : intraDataList) {
 						Calendar cal = Calendar.getInstance(DateUtil.UTC);
 						cal.setTime(startTime.toLocalDate().toDate());
 						LocalTime tempLocalTime = LocalTime.parse(data
 								.getTime());
 						cal.set(Calendar.HOUR_OF_DAY,
 								tempLocalTime.getHourOfDay());
 						cal.set(Calendar.MINUTE,
 								tempLocalTime.getMinuteOfHour());
 						cal.set(Calendar.SECOND,
 								tempLocalTime.getSecondOfMinute());
 						System.out.println(" " + cal.getTime() + " "
 								+ data.getValue());
 						JsonDataPoints jdatapoint = new JsonDataPoints();
 						jdatapoint
 								.setAt(Long.toString(cal.getTime().getTime()));
 						JsonDataValues jvalue = new JsonDataValues();
 						jvalue.setUnit_id(ds.getDatastreamUnitsList().get(0)
 								.getUnitID());
 						jvalue.setVal(Double.toString(data.getValue()-baseCalories));
 						List<JsonDataValues> jsonvalueList = new ArrayList<JsonDataValues>();
 						jsonvalueList.add(jvalue);
 						if (counter == 0) {
 							jvalue.setVal_tag("start");
 						} else if (counter == end) {
 							jvalue.setVal_tag("end");
 						}
 						jdatapoint.setValue_list(jsonvalueList);
 						jdataPList.add(jdatapoint);
 						summaryValue = summaryValue + data.getValue();
 						counter++;
 					}
 				} else {
 					//two days sleep
 					System.out.println("startTime.toLocalDate()"
 							+ startTime.toLocalDate() + ","
 							+ "endTime.toLocalDate()" + endTime.toLocalDate());
 					if (!startTime.toLocalDate().plusDays(1)
 							.isEqual(endTime.toLocalDate())) {
 						System.out.println("Error--Sleeping far too long");
 						return apiCounter;
 					}
 					System.out.println("****Different Dates****"
 							+ startTime.toLocalDate().toString());
 					IntradaySummary stepSummary1 = apiClientService.getClient()
 							.getIntraDayTimeSeries(
 									new LocalUserDetail(apiinfo.getLoginID(),
 											apiinfo.getExtId()), fitbitUser,
 									TimeSeriesResourceType.CALORIES_OUT,
 									startTime.toLocalDate(),
 									startTime.toLocalTime(),
 									LocalTime.parse("23:59:00"));
 					apiCounter++;
 					IntradaySummary stepSummary2 = apiClientService.getClient()
 							.getIntraDayTimeSeries(
 									new LocalUserDetail(apiinfo.getLoginID(),
 											apiinfo.getExtId()), fitbitUser,
 									TimeSeriesResourceType.CALORIES_OUT,
 									endTime.toLocalDate(),
 									LocalTime.parse("00:00:00"),
 									endTime.toLocalTime());
 					apiCounter++;
 					if (stepSummary1 == null
 							|| stepSummary1.getIntradayDataset() == null
 							|| stepSummary1.getIntradayDataset().getDataset() == null) {
 						System.out.println("-------No record-----"
 								+ startTime.toLocalDate().toString());
 						return apiCounter;
 					}
 					if (stepSummary2 == null
 							|| stepSummary2.getIntradayDataset() == null
 							|| stepSummary2.getIntradayDataset().getDataset() == null) {
 						System.out.println("-------No record-----"
 								+ startTime.toLocalDate().toString());
 						return apiCounter;
 					}
 					//
 					List<IntradayData> intraDataList1 = stepSummary1
 							.getIntradayDataset().getDataset();
 					List<IntradayData> intraDataList2 = stepSummary2
 							.getIntradayDataset().getDataset();
 					double baseCalories = 5000;
 					for (IntradayData data : intraDataList1) {
 						if (data.getValue() < baseCalories) {
 							baseCalories = data.getValue();
 						}
 					}
 					for (IntradayData data : intraDataList2) {
 						if (data.getValue() < baseCalories) {
 							baseCalories = data.getValue();
 						}
 					}
 					System.out.println(apiinfo.getLoginID()+":baseCalories:"+baseCalories);
 					int counter = 0;
 					double summaryValue = 0;
 					for (IntradayData data : intraDataList1) {
 						Calendar cal = Calendar.getInstance(DateUtil.UTC);
 						cal.setTime(startTime.toLocalDate().toDate());
 						LocalTime tempLocalTime = LocalTime.parse(data
 								.getTime());
 						cal.set(Calendar.HOUR_OF_DAY,
 								tempLocalTime.getHourOfDay());
 						cal.set(Calendar.MINUTE,
 								tempLocalTime.getMinuteOfHour());
 						cal.set(Calendar.SECOND,
 								tempLocalTime.getSecondOfMinute());
 						// System.out.println(" " + cal.getTime() + " "
 						// + data.getValue());
 						JsonDataPoints jdatapoint = new JsonDataPoints();
 						jdatapoint
 								.setAt(Long.toString(cal.getTime().getTime()));
 						JsonDataValues jvalue = new JsonDataValues();
 						jvalue.setUnit_id(ds.getDatastreamUnitsList().get(0)
 								.getUnitID());
 						jvalue.setVal(Double.toString(data.getValue()
 								- baseCalories));
 						List<JsonDataValues> jsonvalueList = new ArrayList<JsonDataValues>();
 						jsonvalueList.add(jvalue);
 						if (counter == 0) {
 							jvalue.setVal_tag("start");
 						}
 						jdatapoint.setValue_list(jsonvalueList);
 						jdataPList.add(jdatapoint);
 						summaryValue = summaryValue + data.getValue();
 						counter++;
 					}
 					for (IntradayData data : intraDataList2) {
 						Calendar cal = Calendar.getInstance(DateUtil.UTC);
 						cal.setTime(endTime.toLocalDate().toDate());
 						LocalTime tempLocalTime = LocalTime.parse(data
 								.getTime());
 						cal.set(Calendar.HOUR_OF_DAY,
 								tempLocalTime.getHourOfDay());
 						cal.set(Calendar.MINUTE,
 								tempLocalTime.getMinuteOfHour());
 						cal.set(Calendar.SECOND,
 								tempLocalTime.getSecondOfMinute());
 //						System.out.println(" " + cal.getTime() + " "
 //								+ data.getValue());
 						JsonDataPoints jdatapoint = new JsonDataPoints();
 						jdatapoint
 								.setAt(Long.toString(cal.getTime().getTime()));
 						JsonDataValues jvalue = new JsonDataValues();
 						jvalue.setUnit_id(ds.getDatastreamUnitsList().get(0)
 								.getUnitID());
 						jvalue.setVal(Double.toString(data.getValue()
 								- baseCalories));
 						List<JsonDataValues> jsonvalueList = new ArrayList<JsonDataValues>();
 						jsonvalueList.add(jvalue);
 						if (counter == 0) {
 							jvalue.setVal_tag("start");
 						}
 						jdatapoint.setValue_list(jsonvalueList);
 						jdataPList.add(jdatapoint);
 						summaryValue = summaryValue + data.getValue();
 						counter++;
 					}
 					jdataPList.get(jdataPList.size() - 1).setTimetag("end");
 				}
 				sleepDataDao.create_SameDay_SleepSummaries(sleepdataList);
 				hbaseImport.setData_points(jdataPList);
 				hbaseImport.setDatastream_id(ds.getStreamId());
 				datapointDao.importDatapointsDatapoints(hbaseImport);	
 			}
 			return apiCounter++;
 		} else {
 			System.out.println("no sleep data");
 			return apiCounter++;
 		}
 
 		// System.out.println("Size Summary:"
 		// + stepSummary.getIntradayDataset().getDataset().size());
 
 		// DataSummary: {
 		// DataSummary datasummry = new DataSummary();
 		// DataSummaryDAO dsummaryDao = new DataSummaryDAO();
 		// datasummry.setValue(summaryValue);
 		// datasummry.setDstreamID(ds.getStreamId());
 		// datasummry.setDate(fitbitDate.toDate());
 		// datasummry.setTitle(ds.getTitle());
 		// dsummaryDao.create_A_DataSummary(datasummry);
 		// }
 	}
 
 	public void importdistanceSummary(ExternalApiInfo apiinfo, LocalDate start,
 			LocalDate end,
 			FitbitAPIClientService<FitbitApiClientAgent> apiClientService)
 			throws FitbitAPIException, NumberFormatException,
 			ErrorCodeException, IOException, ParseException {
 		FitbitUser fitbitUser = new FitbitUser(apiinfo.getExtId());
 		DateUtil dateUtil = new DateUtil();
 		// LocalDate startDate = LocalDate.parse("2013-01-07",
 		// DateTimeFormat.forPattern(dateUtil.YearMonthDay_DateFormat_pattern));
 		// LocalDate endDate = LocalDate.parse("2013-01-11",
 		// DateTimeFormat.forPattern(dateUtil.YearMonthDay_DateFormat_pattern));
 
 		List<Data> dataSummaries = apiClientService.getClient().getTimeSeries(
 				new LocalUserDetail(apiinfo.getLoginID(), apiinfo.getExtId()),
 				fitbitUser, TimeSeriesResourceType.DISTANCE, start, end);
 		Datastream ds = healthDSdao.getDefaultDatastreamOfType(
 				apiinfo.getLoginID(), "distance_travel");
 		for (Data data : dataSummaries) {
 			// System.out.println(data.getDateTime() + "," + data.getValue());
 			DataSummary: {
 				DataSummary datasummry = new DataSummary();
 				DataSummaryDAO dsummaryDao = new DataSummaryDAO();
 				datasummry.setValue(Double.parseDouble(data.getValue()));
 				datasummry.setDstreamID(ds.getStreamId());
 				datasummry.setLoginID(ds.getOwner());
 				datasummry
 						.setDate(LocalDate
 								.parse(data.getDateTime(),
 										DateTimeFormat
 												.forPattern(dateUtil.YearMonthDay_DateFormat_pattern))
 								.toDate());
 				datasummry.setTitle(ds.getTitle());
 				dsummaryDao.create_A_DataSummary(datasummry);
 			}
 		}
 	}
 
 	public void importCaloriesSummary(ExternalApiInfo apiinfo, LocalDate start,
 			LocalDate end,
 			FitbitAPIClientService<FitbitApiClientAgent> apiClientService)
 			throws FitbitAPIException, NumberFormatException,
 			ErrorCodeException, IOException, ParseException {
 		FitbitUser fitbitUser = new FitbitUser(apiinfo.getExtId());
 		DateUtil dateUtil = new DateUtil();
 		// LocalDate startDate = LocalDate.parse("2013-01-07",
 		// DateTimeFormat.forPattern(dateUtil.YearMonthDay_DateFormat_pattern));
 		// LocalDate endDate = LocalDate.parse("2013-01-11",
 		// DateTimeFormat.forPattern(dateUtil.YearMonthDay_DateFormat_pattern));
 
 		List<Data> dataSummaries = apiClientService.getClient().getTimeSeries(
 				new LocalUserDetail(apiinfo.getLoginID(), apiinfo.getExtId()),
 				fitbitUser, TimeSeriesResourceType.ACTIVITY_CALORIES, start,
 				end);
 		Datastream ds = healthDSdao.getDefaultDatastreamOfType(
 				apiinfo.getLoginID(), "calories_burned");
 		for (Data data : dataSummaries) {
 //			System.out.println(data.getDateTime() + "," + data.getValue());
 			DataSummary: {
 				DataSummary datasummry = new DataSummary();
 				DataSummaryDAO dsummaryDao = new DataSummaryDAO();
 				datasummry.setValue(Double.parseDouble(data.getValue()));
 				datasummry.setDstreamID(ds.getStreamId());
 				datasummry.setLoginID(ds.getOwner());
 				datasummry
 						.setDate(LocalDate
 								.parse(data.getDateTime(),
 										DateTimeFormat
 												.forPattern(dateUtil.YearMonthDay_DateFormat_pattern))
 								.toDate());
 				datasummry.setTitle(ds.getTitle());
 				dsummaryDao.create_A_DataSummary(datasummry);
 			}
 		}
 	}
 
 	public static void main(String args[]) throws ServletException,
 			NumberFormatException, FitbitAPIException, ErrorCodeException,
 			IOException, ParseException {
 		FitbitTimer timer = new FitbitTimer();
 		timer.init();
 		Ext_API_Info_DAO extDao = new Ext_API_Info_DAO();
 		ExternalApiInfo info = extDao.getExt_API_INFO("dongdong", "fitbit",
 				null);
 		LocalDate endDate = LocalDate.now();
 
 		LocalDate startDate = endDate.minusDays(90);
 		LocalDate localDate = LocalDate.parse("2013-1-13");
 		System.out.println(timer.import_Sleep_Data(info, localDate, apiClientService,0));
 		// timer.importCaloriesSummary(info, startDate, endDate,
 		// apiClientService);
 		// timer.(info, localDate, apiClientService);
 	}
 }
