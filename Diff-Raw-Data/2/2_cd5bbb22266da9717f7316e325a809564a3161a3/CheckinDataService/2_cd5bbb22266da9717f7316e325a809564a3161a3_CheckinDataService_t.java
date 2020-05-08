 package com.heartyoh.service;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.dbist.dml.Dml;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.multipart.MultipartHttpServletRequest;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.FetchOptions;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.PreparedQuery;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Query.FilterOperator;
 import com.heartyoh.model.Driver;
 import com.heartyoh.model.DriverRunSum;
 import com.heartyoh.model.DriverSpeedSum;
 import com.heartyoh.model.Vehicle;
 import com.heartyoh.model.VehicleRunSum;
 import com.heartyoh.model.VehicleSpeedSum;
 import com.heartyoh.util.ConnectionManager;
 import com.heartyoh.util.DataUtils;
 import com.heartyoh.util.DatasourceUtils;
 import com.heartyoh.util.GreenFleetConstant;
 import com.heartyoh.util.SessionUtils;
 
 @Controller
 public class CheckinDataService extends EntityService {
 	
 	/**
 	 * 키가 되는 시간 정보 컬럼 
 	 */
 	private static final String KEY_TIME_COLUMN = "engine_end_time";
 	/**
 	 * logger
 	 */
 	private static final Logger logger = LoggerFactory.getLogger(CheckinDataService.class);	
 	
 	@Override
 	protected String getEntityName() {
 		return "CheckinData";
 	}
 
 	@Override
 	protected boolean useFilter() {
 		return true;
 	}
 
 	/**
 	 * date 형태의 문자열 데이터를 Date 객체로 변경 
 	 * 
 	 * @param timeStr
 	 * @param timezone
 	 * @return
 	 */
 	private Date parseDate(String timeStr, int timezone) {
 		if(DataUtils.isEmpty(timeStr))
 			return null;
 		
 		return SessionUtils.stringToDateTime(timeStr, GreenFleetConstant.DEFAULT_DATE_TIME_FORMAT, timezone);
 	}
 	
 	/**
 	 * create, update 전에 request map으로 부터 문자열 시간 정보를 모두 타임존 적용하여 Date로 바꾼다.
 	 */
 	@Override
 	protected void adjustRequestMap(DatastoreService datastore, Map<String, Object> map) throws Exception {
 		String datetimeStr = (String)map.remove("datetime");
 		String engineStartTimeStr = (String)map.remove("engine_start_time");
 		String engineEndTimeStr = (String)map.remove(KEY_TIME_COLUMN);
 		
 		Entity company = datastore.get((Key)map.get("_company_key"));
 		String timezoneStr = (String)company.getProperty("timezone");
 		int timezone = Integer.parseInt(timezoneStr);
 		
 		map.put("datetime", this.parseDate(datetimeStr, timezone));
 		map.put("engine_start_time", this.parseDate(engineStartTimeStr, timezone));
 		map.put(KEY_TIME_COLUMN, this.parseDate(engineEndTimeStr, timezone));
 	}	
 	
 	@Override
 	protected String getIdValue(Map<String, Object> map) {		
 		String engineEndTimeStr = DataUtils.dateToString((Date)map.get(KEY_TIME_COLUMN), GreenFleetConstant.DEFAULT_DATE_TIME_FORMAT);
 		return map.get("terminal_id") + "@" + engineEndTimeStr;
 	}
 
 	@Override
 	protected void onCreate(Entity entity, Map<String, Object> map, DatastoreService datastore) throws Exception {
 		entity.setProperty("terminal_id", map.get("terminal_id"));
 		entity.setProperty(KEY_TIME_COLUMN, map.get(KEY_TIME_COLUMN));
 		super.onCreate(entity, map, datastore);
 	}
 
 	@Override
 	protected void onSave(Entity entity, Map<String, Object> map, DatastoreService datastore) throws Exception {
 
 		entity.setProperty("vehicle_id", stringProperty(map, "vehicle_id"));
 		entity.setProperty("driver_id", stringProperty(map, "driver_id"));
 		entity.setProperty("engine_start_time", map.get("engine_start_time"));
 		entity.setProperty("datetime", map.get("datetime"));
 		
 		entity.setUnindexedProperty("distance", doubleProperty(map, "distance"));
 		entity.setUnindexedProperty("running_time", intProperty(map, "running_time"));
 		entity.setUnindexedProperty("idle_time", intProperty(map, "idle_time"));
 		entity.setUnindexedProperty("eco_driving_time", intProperty(map, "eco_driving_time"));
 		entity.setUnindexedProperty("average_speed", doubleProperty(map, "average_speed"));
 		entity.setUnindexedProperty("max_speed", intProperty(map, "max_speed"));
 		entity.setUnindexedProperty("fuel_consumption", doubleProperty(map, "fuel_consumption"));
 		entity.setUnindexedProperty("fuel_efficiency", doubleProperty(map, "fuel_efficiency"));
 		entity.setUnindexedProperty("sudden_accel_count", intProperty(map, "sudden_accel_count"));
 		entity.setUnindexedProperty("sudden_brake_count", intProperty(map, "sudden_brake_count"));
 		entity.setUnindexedProperty("over_speed_time", intProperty(map, "over_speed_time"));
 		entity.setUnindexedProperty("co2_emissions", doubleProperty(map, "co2_emissions"));
 		entity.setUnindexedProperty("max_cooling_water_temp", doubleProperty(map, "max_cooling_water_temp"));
 		entity.setUnindexedProperty("avg_battery_volt", doubleProperty(map, "avg_battery_volt"));
 		
 		entity.setUnindexedProperty("less_than_10km", intProperty(map, "less_than_10km"));
 		entity.setUnindexedProperty("less_than_20km", intProperty(map, "less_than_20km"));
 		entity.setUnindexedProperty("less_than_30km", intProperty(map, "less_than_30km"));
 		entity.setUnindexedProperty("less_than_40km", intProperty(map, "less_than_40km"));
 		entity.setUnindexedProperty("less_than_50km", intProperty(map, "less_than_50km"));
 		entity.setUnindexedProperty("less_than_60km", intProperty(map, "less_than_60km"));
 		entity.setUnindexedProperty("less_than_70km", intProperty(map, "less_than_70km"));
 		entity.setUnindexedProperty("less_than_80km", intProperty(map, "less_than_80km"));
 		entity.setUnindexedProperty("less_than_90km", intProperty(map, "less_than_90km"));
 		entity.setUnindexedProperty("less_than_100km", intProperty(map, "less_than_100km"));
 		entity.setUnindexedProperty("less_than_110km", intProperty(map, "less_than_110km"));
 		entity.setUnindexedProperty("less_than_120km", intProperty(map, "less_than_120km"));
 		entity.setUnindexedProperty("less_than_130km", intProperty(map, "less_than_130km"));
 		entity.setUnindexedProperty("less_than_140km", intProperty(map, "less_than_140km"));
 		entity.setUnindexedProperty("less_than_150km", intProperty(map, "less_than_150km"));
 		entity.setUnindexedProperty("less_than_160km", intProperty(map, "less_than_160km"));
 
 		super.onSave(entity, map, datastore);
 	}
 	
 	@Override
 	protected void saveEntity(Entity checkin, Map<String, Object> map, DatastoreService datastore) throws Exception {
 		
 		Vehicle vehicle = 
 				DatasourceUtils.findVehicle(checkin.getParent().getName(), (String)checkin.getProperty("vehicle_id"));
 		
 		if(vehicle != null) {
 			// 차량 정보의 공인연비, 평균 연비로 에코 지수를 계산하여 checkin 데이터에 추가 
 			float avgEffcc = DataUtils.toFloat(checkin.getProperty("fuel_efficiency"));
 			float officialEffcc = vehicle.getOfficialEffcc();
 			
 			if(officialEffcc > 0) {
 				int ecoIndex = Math.round((avgEffcc / officialEffcc) * 100);
 				checkin.setUnindexedProperty("eco_index", ecoIndex);
 			}
 		}
 		
 		datastore.put(checkin);
 	}	
 
 	@RequestMapping(value = "/checkin_data/import", method = RequestMethod.POST)
 	public @ResponseBody
 	String imports(MultipartHttpServletRequest request, HttpServletResponse response) throws Exception {
 		return super.imports(request, response);
 	}
 
 	@RequestMapping(value = "/checkin_data/save", method = RequestMethod.POST)
 	public @ResponseBody
 	String save(HttpServletRequest request, HttpServletResponse response) throws Exception {
 		return super.save(request, response);
 	}
 
 	@RequestMapping(value = "/checkin_data/delete", method = RequestMethod.POST)
 	public @ResponseBody
 	String delete(HttpServletRequest request, HttpServletResponse response) {
 		return super.delete(request, response);
 	}
 
 	@RequestMapping(value = "/checkin_data", method = RequestMethod.GET)
 	public @ResponseBody
 	Map<String, Object> retrieve(HttpServletRequest request, HttpServletResponse response) {
 		return super.retrieve(request, response);
 	}
 	
 	@Override
 	protected void addFilter(Query q, String property, Object value) {
 		
 		if("date".equals(property)) {
 			long dateMillis = DataUtils.toLong(value);
 			if(dateMillis > 1) {
 				Date[] fromToDate = DataUtils.getFromToDate(dateMillis * 1000, 0, 1);
 				q.addFilter(KEY_TIME_COLUMN, Query.FilterOperator.GREATER_THAN_OR_EQUAL, fromToDate[0]);
 				q.addFilter(KEY_TIME_COLUMN, Query.FilterOperator.LESS_THAN_OR_EQUAL, fromToDate[1]);
 			}
 		} else {
 			q.addFilter(property, FilterOperator.EQUAL, value);
 		}
 	}
 	
 	@Override
 	protected void buildQuery(Query q, HttpServletRequest request) {
 		
 		String fromDateStr = request.getParameter("from_date");
 		String toDateStr = request.getParameter("to_date");
 		
 		if(!DataUtils.isEmpty(fromDateStr) && !DataUtils.isEmpty(toDateStr)) {
 			q.addFilter(KEY_TIME_COLUMN, Query.FilterOperator.GREATER_THAN_OR_EQUAL, SessionUtils.stringToDate(fromDateStr));
 			q.addFilter(KEY_TIME_COLUMN, Query.FilterOperator.LESS_THAN_OR_EQUAL, SessionUtils.stringToDate(toDateStr));
 			
 		} else if(!DataUtils.isEmpty(fromDateStr) && DataUtils.isEmpty(toDateStr)) {
 			q.addFilter(KEY_TIME_COLUMN, Query.FilterOperator.GREATER_THAN_OR_EQUAL, SessionUtils.stringToDate(fromDateStr));
 			
 		} else if(DataUtils.isEmpty(fromDateStr) && !DataUtils.isEmpty(toDateStr)) {
 			q.addFilter(KEY_TIME_COLUMN, Query.FilterOperator.LESS_THAN_OR_EQUAL, SessionUtils.stringToDate(toDateStr));
 		}
 	}
 	
 	/**
 	 * Checkin daily summary : VehicleRunSum, DriverRunSum, VehicleSpeedSum, DriverSpeedSum에 반영 
 	 * 
 	 * @param request
 	 * @param response
 	 * @return
 	 * @throws Exception
 	 */
 	public void dailySummary(String company) throws Exception {
 		
 		Key companyKey = KeyFactory.createKey("Company", company);
 		// 이틀 전 데이터를 서머리한다. 
 		Date stDate = DataUtils.addDate(DataUtils.getToday(), - 2);
 		Calendar c = Calendar.getInstance();
 		c.setTime(stDate);
 		int year = c.get(Calendar.YEAR);
 		int month = c.get(Calendar.MONTH) + 1;				
 		Date fromDate = DataUtils.getBeginDateOfMonth(stDate);
 		Date toDate = DataUtils.getEndDateOfMonth(stDate);
 		this.updateVehicleSummary(companyKey, year, month, fromDate, toDate);
 		this.updateDriverSummary(companyKey, year, month, fromDate, toDate);
 	}
 	
 	/**
 	 * VehicleRunSum, VehicleSpeedSum, Vehicle(주행거리)에 반영 
 	 * 
 	 * @param company
 	 * @param year
 	 * @param month
 	 * @param firstDateOfMonth
 	 * @throws Exception
 	 */
 	private void updateVehicleSummary(Key companyKey, int year, int month, Date fromDate, Date toDate) throws Exception {
 		
 		List<Vehicle> vehicles = DatasourceUtils.findAllVehicles(companyKey.getName());
 
 		// 각 Vehicle에 대해서 모두 수집 
 		for(Vehicle vehicle : vehicles) {
 			List<Entity> checkinsByVehicle = this.findCheckins(companyKey, fromDate, toDate, "vehicle_id", vehicle.getId());
 			this.updateVehicleSumInfo(vehicle, year, month, checkinsByVehicle);
 			if(logger.isInfoEnabled())
 				logger.info("Vehicle [" + vehicle.getId() + "] summary completed!");
 		}
 	}
 
 	/**
 	 * VehicleRunSum, VehicleSpeedSum, Vehicle의 정보 업데이트 
 	 * 
 	 * @param vehicle
 	 * @param year
 	 * @param month
 	 * @param checkins
 	 * @throws Exception
 	 */
 	private void updateVehicleSumInfo(Vehicle vehicle, int year, int month, List<Entity> checkins) throws Exception {
 		
 		Dml dml = ConnectionManager.getInstance().getDml();
 		Map<String, Object> params = DataUtils.newMap("company", vehicle.getCompany());
 		params.put("vehicle", vehicle.getId());
 		params.put("year", year);
 		params.put("month", month);
 		VehicleRunSum runSum = dml.select(VehicleRunSum.class, params);
 		VehicleSpeedSum speedSum = dml.select(VehicleSpeedSum.class, params);
 		
 		if(runSum == null)
 			runSum = new VehicleRunSum(vehicle.getCompany(), vehicle.getId(), year, month);
 		
 		if(speedSum == null)
 			speedSum = new VehicleSpeedSum(vehicle.getCompany(), vehicle.getId(), year, month);
 		
 		int checkInCount = checkins.size();
 		int runTime = 0;
 		float runDist = 0;
 		float consmpt = 0;
 		float co2Emss = 0;
 		int sudAccelCnt = 0;
 		int sudBrakeCnt = 0;
 		int ecoDrvTime = 0;
 		int ovrSpdTime = 0;
 		int idleTime = 0;
 		Map<String, Integer> speedMap = new HashMap<String, Integer>();
 		
 		for(Entity checkin : checkins) {
 			runTime += DataUtils.toInt(checkin.getProperty("running_time"));
 			runDist += DataUtils.toFloat(checkin.getProperty("distance"));
 			consmpt += DataUtils.toFloat(checkin.getProperty("fuel_consumption"));
 			co2Emss += DataUtils.toFloat(checkin.getProperty("co2_emissions"));	
 			sudAccelCnt += DataUtils.toInt(checkin.getProperty("sudden_accel_count"));
 			sudBrakeCnt += DataUtils.toInt(checkin.getProperty("sudden_brake_count"));
 			ecoDrvTime += DataUtils.toInt(checkin.getProperty("eco_driving_time"));
 			ovrSpdTime += DataUtils.toInt(checkin.getProperty("over_speed_time"));
 			idleTime += DataUtils.toInt(checkin.getProperty("idle_time"));
 			
 			for(int i = 1 ; i <= 16 ; i++) {
 				String key = "less_than_" + i + "0km";
 				if(!speedMap.containsKey(key)) {
 					speedMap.put(key, DataUtils.toInt(checkin.getProperty(key)));
 				} else {
 					int chkSpd = DataUtils.toInt(checkin.getProperty(key));
 					int oriSpd = speedMap.get(key);
 					speedMap.put(key, chkSpd + oriSpd);
 				}
 			}
 		}
 		
 		if(checkInCount > 0) {
 			// 1. VehicleRunSum
 			runSum.setRunTime(runTime);
 			runSum.setRunDist(runDist);
 			runSum.setConsmpt(consmpt);
 			runSum.setCo2Emss(co2Emss);
 			runSum.setSudAccelCnt(sudAccelCnt);
 			runSum.setSudBrakeCnt(sudBrakeCnt);
 			runSum.setEcoDrvTime(ecoDrvTime);
 			runSum.setOvrSpdTime(ovrSpdTime);
 			runSum.setIdleTime(idleTime);
 			runSum.setEffcc((float)(runDist / consmpt));
 			float officialEffcc = vehicle.getOfficialEffcc();
 			int ecoIndex = Math.round((runSum.getEffcc() / officialEffcc) * 100);
 			runSum.setEcoIndex(ecoIndex);
 			dml.upsert(runSum);
 			
 			// 2. VehicleSpeedSum
 			speedSum.setSpdLt10(speedMap.get("less_than_10km") + speedSum.getSpdLt10());
 			speedSum.setSpdLt20(speedMap.get("less_than_20km") + speedSum.getSpdLt20());
 			speedSum.setSpdLt30(speedMap.get("less_than_30km") + speedSum.getSpdLt30());
 			speedSum.setSpdLt40(speedMap.get("less_than_40km") + speedSum.getSpdLt40());
 			speedSum.setSpdLt50(speedMap.get("less_than_50km") + speedSum.getSpdLt50());
 			speedSum.setSpdLt60(speedMap.get("less_than_60km") + speedSum.getSpdLt60());
 			speedSum.setSpdLt70(speedMap.get("less_than_70km") + speedSum.getSpdLt70());
 			speedSum.setSpdLt80(speedMap.get("less_than_80km") + speedSum.getSpdLt80());
 			speedSum.setSpdLt90(speedMap.get("less_than_90km") + speedSum.getSpdLt90());
 			speedSum.setSpdLt100(speedMap.get("less_than_100km") + speedSum.getSpdLt100());
 			speedSum.setSpdLt110(speedMap.get("less_than_110km") + speedSum.getSpdLt110());
 			speedSum.setSpdLt120(speedMap.get("less_than_120km") + speedSum.getSpdLt120());
 			speedSum.setSpdLt130(speedMap.get("less_than_130km") + speedSum.getSpdLt130());
 			speedSum.setSpdLt140(speedMap.get("less_than_140km") + speedSum.getSpdLt140());
 			speedSum.setSpdLt150(speedMap.get("less_than_150km") + speedSum.getSpdLt150());
 			speedSum.setSpdLt160(speedMap.get("less_than_160km") + speedSum.getSpdLt160());
 			dml.upsert(speedSum);
 			
 			// 3. Vehicle - 총 주행거리, 총 주행시간 
 			vehicle.setTotalDistance(vehicle.getTotalDistance() + runDist);
 			vehicle.setTotalRunTime(vehicle.getTotalRunTime() + runTime);
 			dml.upsert(vehicle);
 		}
 	}
 	
 	/**
 	 * checkin data를 조회 
 	 * 
 	 * @param companyKey
 	 * @param fromDate
 	 * @param toDate
 	 * @param filterName
 	 * @param filterValue
 	 * @return
 	 * @throws Exception
 	 */
 	private List<Entity> findCheckins(Key companyKey, Date fromDate, Date toDate, String filterName, String filterValue) throws Exception {
 		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 		Query q = new Query(getEntityName());
 		q.setAncestor(companyKey);
 		q.addFilter(filterName, Query.FilterOperator.EQUAL, filterValue);
 		q.addFilter(KEY_TIME_COLUMN, Query.FilterOperator.GREATER_THAN_OR_EQUAL, fromDate);
 		q.addFilter(KEY_TIME_COLUMN, Query.FilterOperator.LESS_THAN_OR_EQUAL, toDate);
 		PreparedQuery pq = datastore.prepare(q);
 		return pq.asList(FetchOptions.Builder.withLimit(Integer.MAX_VALUE).offset(0));
 	}
 	
 	/**
 	 * DriverRunSum, DriverSpeedSum에 반영 
 	 * 
 	 * @param company
 	 * @param year
 	 * @param month
 	 * @param fromDate
 	 * @param toDate
 	 * @throws Exception
 	 */
 	private void updateDriverSummary(Key companyKey, int year, int month, Date fromDate, Date toDate) throws Exception {
 		@SuppressWarnings("unchecked")
 		List<Driver> drivers = (List<Driver>) DatasourceUtils.findEntities(Driver.class, DataUtils.newMap("company",companyKey.getName()));
 
 		// 각 Driver에 대해서 모두 수집 
 		for(Driver driver : drivers) {
 			List<Entity> checkinsByDriver = this.findCheckins(companyKey, fromDate, toDate, "driver_id", driver.getId());
 			this.updateDriverSumInfo(driver, year, month, checkinsByDriver);
 			
 			if(logger.isInfoEnabled())
 				logger.info("Driver [" + driver.getId() + "] summary completed!");			
 		}		
 	}
 	
 	/**
 	 * VehicleSpeedSum 정보 업데이트 
 	 * 
 	 * @param driver
 	 * @param year
 	 * @param month
 	 * @param checkins
 	 * @throws Exception
 	 */
 	private void updateDriverSumInfo(Driver driver, int year, int month, List<Entity> checkins) throws Exception {
 		
 		Dml dml = ConnectionManager.getInstance().getDml();
 		Map<String, Object> params = DataUtils.newMap("company", driver.getCompany());
 		params.put("driver", driver.getId());
 		params.put("year", year);
 		params.put("month", month);
 		DriverRunSum runSum = dml.select(DriverRunSum.class, params);
 		DriverSpeedSum speedSum = dml.select(DriverSpeedSum.class, params);
 		
 		if(runSum == null)
 			runSum = new DriverRunSum(driver.getCompany(), driver.getId(), year, month);
 		
 		if(speedSum == null)
 			speedSum = new DriverSpeedSum(driver.getCompany(), driver.getId(), year, month);		
 		
 		int checkInCount = checkins.size();
 		int runTime = 0;
 		float runDist = 0;
 		float consmpt = 0;
 		float co2Emss = 0;
 		int sudAccelCnt = 0;
 		int sudBrakeCnt = 0;
 		int ecoDrvTime = 0;
 		int ovrSpdTime = 0;
 		int idleTime = 0;
 		int ecoIndex = 0;
 		Map<String, Integer> speedMap = new HashMap<String, Integer>();
 		
 		for(Entity checkin : checkins) {
 			runTime += DataUtils.toInt(checkin.getProperty("running_time"));
 			runDist += DataUtils.toFloat(checkin.getProperty("distance"));
 			consmpt += DataUtils.toFloat(checkin.getProperty("fuel_consumption"));
 			co2Emss += DataUtils.toFloat(checkin.getProperty("co2_emissions"));	
 			sudAccelCnt += DataUtils.toInt(checkin.getProperty("sudden_accel_count"));
 			sudBrakeCnt += DataUtils.toInt(checkin.getProperty("sudden_brake_count"));
 			ecoDrvTime += DataUtils.toInt(checkin.getProperty("eco_driving_time"));
 			ovrSpdTime += DataUtils.toInt(checkin.getProperty("over_speed_time"));
 			idleTime += DataUtils.toInt(checkin.getProperty("idle_time"));
 			
 			if(checkin.hasProperty("eco_index")) {
 				int ecoIndexValue = DataUtils.toInt(checkin.getProperty("eco_index"));
 				ecoIndex += ecoIndexValue;
 			}
 			
 			for(int i = 1 ; i <= 16 ; i++) {
 				String key = "less_than_" + i + "0km";
 				if(!speedMap.containsKey(key)) {
 					speedMap.put(key, DataUtils.toInt(checkin.getProperty(key)));
 				} else {
 					int chkSpd = DataUtils.toInt(checkin.getProperty(key));
 					int oriSpd = speedMap.get(key);
 					speedMap.put(key, chkSpd + oriSpd);
 				}
 			}			
 		}
 		
 		if(checkInCount > 0) {
 			// 1. DriverRunSum
 			runSum.setRunTime(runTime);
 			runSum.setRunDist(runDist);
 			runSum.setConsmpt(consmpt);
 			runSum.setCo2Emss(co2Emss);		
 			runSum.setSudAccelCnt(sudAccelCnt);
 			runSum.setSudBrakeCnt(sudBrakeCnt);
 			runSum.setEcoDrvTime(ecoDrvTime);
 			runSum.setOvrSpdTime(ovrSpdTime);
 			runSum.setIdleTime(idleTime);
 			runSum.setEffcc((float)(runDist / consmpt));			
			runSum.setEcoIndex(Math.round(ecoIndex / checkInCount));
 			dml.upsert(runSum);
 			
 			// 2. DriverSpeedSum
 			speedSum.setSpdLt10(speedMap.get("less_than_10km") + speedSum.getSpdLt10());
 			speedSum.setSpdLt20(speedMap.get("less_than_20km") + speedSum.getSpdLt20());
 			speedSum.setSpdLt30(speedMap.get("less_than_30km") + speedSum.getSpdLt30());
 			speedSum.setSpdLt40(speedMap.get("less_than_40km") + speedSum.getSpdLt40());
 			speedSum.setSpdLt50(speedMap.get("less_than_50km") + speedSum.getSpdLt50());
 			speedSum.setSpdLt60(speedMap.get("less_than_60km") + speedSum.getSpdLt60());
 			speedSum.setSpdLt70(speedMap.get("less_than_70km") + speedSum.getSpdLt70());
 			speedSum.setSpdLt80(speedMap.get("less_than_80km") + speedSum.getSpdLt80());
 			speedSum.setSpdLt90(speedMap.get("less_than_90km") + speedSum.getSpdLt90());
 			speedSum.setSpdLt100(speedMap.get("less_than_100km") + speedSum.getSpdLt100());
 			speedSum.setSpdLt110(speedMap.get("less_than_110km") + speedSum.getSpdLt110());
 			speedSum.setSpdLt120(speedMap.get("less_than_120km") + speedSum.getSpdLt120());
 			speedSum.setSpdLt130(speedMap.get("less_than_130km") + speedSum.getSpdLt130());
 			speedSum.setSpdLt140(speedMap.get("less_than_140km") + speedSum.getSpdLt140());
 			speedSum.setSpdLt150(speedMap.get("less_than_150km") + speedSum.getSpdLt150());
 			speedSum.setSpdLt160(speedMap.get("less_than_160km") + speedSum.getSpdLt160());
 			dml.upsert(speedSum);
 			
 			// 3. Driver - 총 주행거리, 총 주행시간 
 			driver.setTotalDistance(driver.getTotalDistance() + runDist);
 			driver.setTotalRunTime(driver.getTotalRunTime() + runTime);
 			dml.upsert(driver);
 		}
 	}	
 }
