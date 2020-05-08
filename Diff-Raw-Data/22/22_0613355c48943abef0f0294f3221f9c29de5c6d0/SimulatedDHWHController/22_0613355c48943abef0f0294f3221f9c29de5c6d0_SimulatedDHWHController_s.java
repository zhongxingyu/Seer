 package at.fhv.smartdevices.singleNodeDHWH;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import at.fhv.smartdevices.commons.*;
 import at.fhv.smartgrid.rasbpi.internal.ImpulsCounterInformation;
 import at.fhv.smartgrid.rasbpi.internal.MarketPriceAtom;
 import at.fhv.smartgrid.rasbpi.internal.SensorInformation;
 
 /**
  * @author kepe Simulates a dhwh by using the single node dhwh thermal model
  */
 public class SimulatedDHWHController implements ISimulatedSmartController {
 
 	private long _stateTimeStamp = -1;
 	private List<SensorInformation> _sis;
 	public String _mainTempSensorId = "0";
 	private float _currentTemp;
 	private List<ImpulsCounterInformation> _icis;
 	private List<MarketPriceAtom> _cmp;
 	private long _cmpTimeStamp;
 	private boolean _relaisState;
 	private float[] _demands;
 	private float[][] _prices;
 	private final String COUNTER_ID = "impuls";
 
 	public SimulatedDHWHController() {
 	}
 	
 	public void setHysteresisMode(float tempMin, float tempMax){
 		SingleNodeDHWHThermalModel.setTempMax(tempMax);
 		SingleNodeDHWHThermalModel.setTempMin(tempMin);
 	}
 
 	@Override
 	public List<SensorInformation> getSensorInformation() {
 
 		return _sis;
 	}
 
 	@Override
 	public List<ImpulsCounterInformation> getImpulsCounterInformation() {
 		return _icis;
 	}
 
 	@Override
 	public long getCurrentMarketPricesListTimestamp() {
 		return _cmpTimeStamp;
 	}
 
 	@Override
 	public List<MarketPriceAtom> getCurrentMarketPrices() {
 		return _cmp;
 	}
 
 	@Override
 	public void setRelaisPowerState(boolean newState) {
 		_relaisState = newState;
 	}
 
 	@Override
 	public boolean getRelaisPowerState() {
 		return _relaisState;
 	}
 
 	@Override
 	public void SetTime(long now) {
 
 		if (_stateTimeStamp == -1) {
 			initialize(now);
 		} else {
 			update(now);
 		}
 	}
 
 	private void update(long now) {
 
 		Calendar startDate = Calendar.getInstance();
 		startDate.setTimeInMillis(_stateTimeStamp);
 		Calendar dateNow = Calendar.getInstance();
 		dateNow.setTimeInMillis(now);
 
 		double elapsedTime = (dateNow.getTimeInMillis() - startDate.getTimeInMillis());
 		if (elapsedTime <= 60000) {
 			// no detectable change!
 			return;
 		} else {
 			// simulate dhwh behavior
 			double[][] result = simulateDHWH(dateNow, startDate);
 
 			// set current temperature
 			_currentTemp = (float) result[1][result[1].length - 1];
 
 			// set sensor information accordingly
 			setSensorInformation();
 
 			// set relais state
 			_relaisState = result[0][result[0].length - 1] == 1;
 
 			// Set impuls counter
 			setImpulsCounter(startDate, result[0]);
 
 			// check for new price signal.
 			checkForNewPriceSignal(now);
 
 			// set new time
 			_stateTimeStamp = now;
 		}
 	}
 
 	private void initialize(long now) {
 		_stateTimeStamp = now;
 		_currentTemp = 40;
 		_relaisState = false;
 		_cmp = new ArrayList<MarketPriceAtom>();
 
 		try {
 			_demands = readFromFile();
 			_prices = readPricesFromFile();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		setSensorInformation();
 
 	}
 
 	private void setImpulsCounter(Calendar startDate, double[] discreteSwitch) {
 
 		_icis = new ArrayList<ImpulsCounterInformation>();
 		ImpulsCounterInformation ic = new ImpulsCounterInformation();
 		ic.impulsCounterId = COUNTER_ID;
 		ic.countingStart = startDate.getTime();
 		ic.impulsOccurences = new ArrayList<Long>();
 
 		// in Wh
 		float consumption = 0;
 		for (int seconds = 0; seconds < discreteSwitch.length * 60; seconds++) {
 			int minute = (int) Math.floor(seconds / 60.0);
 			consumption += discreteSwitch[minute] * SingleNodeDHWHThermalModel.pEl / 3600.0;
 			if (consumption > 1) {
 				// System.out.println(minute+":"+seconds+"/"+consumption);
 				ic.impulsOccurences.add((long) (seconds * 1000));
 				consumption -= 1;
 			}
 		}
 		if (ic.impulsOccurences.size() > 0) {
 			_icis.add(ic);
 		}
 	}
 
 	private void setSensorInformation() {
 		_sis = new ArrayList<SensorInformation>();
 		SensorInformation si = new SensorInformation();
 		si.setSensorId(_mainTempSensorId);
 		si.setSensorValue(_currentTemp);
 		_sis.add(si);
 	}
 
 	private void checkForNewPriceSignal(long now) {
 		Calendar cnow = Calendar.getInstance();
 		cnow.setTimeInMillis(now);
 		boolean pastTwelve = cnow.get(Calendar.HOUR_OF_DAY) >= 12;
 		boolean oneDayPast = _cmpTimeStamp / (24 * 3600 * 1000) <= (now / (24 * 3600 * 1000)) - 1;
 		// if new price signal is available
 		if (pastTwelve && oneDayPast) {
 			int dayOfTheYear = cnow.get(Calendar.DAY_OF_YEAR);
 			// since 1st day of the year is index 0 in prices, this indexing is
 			// right:
 			float[] tomorrowsPrices = _prices[dayOfTheYear];
 
 			_cmp = new ArrayList<MarketPriceAtom>();
 
 			long startTime = getNextDayStart(cnow);
 			for (int i = 0; i < 24; i++) {
 				MarketPriceAtom a = new MarketPriceAtom();
 				a.validFrom = startTime + (i * 60 * 60 * 1000);
 				a.price = (int) Math.floor(tomorrowsPrices[i] * 100);
 				_cmp.add(a);
 			}
 			_cmpTimeStamp = now;
 		}
 	}
 
 	private long getNextDayStart(Calendar cnow) {
 
 		Calendar dayStart = Calendar.getInstance();
 		dayStart.setTimeInMillis(cnow.getTimeInMillis());
 		dayStart.set(Calendar.SECOND, 0);
 		dayStart.set(Calendar.MINUTE, 0);
 		dayStart.set(Calendar.HOUR, 0);
 		dayStart.set(Calendar.AM_PM, Calendar.AM);
 		return dayStart.getTimeInMillis() + 24 * 60 * 60 * 1000;
 	}
 
 	/**
 	 * simulates the dhwh behavior and returns the switch [0] and the
 	 * temperature [1]
 	 * 
 	 * @param dateNow
 	 * @param startDate
 	 * @return
 	 */
 	private double[][] simulateDHWH(Calendar dateNow, Calendar startDate) {
 		int minNow = getMinutesSinceYearStarted(dateNow);
 		int minStartDate = getMinutesSinceYearStarted(startDate);
 		int amount = minNow - minStartDate;
 
 		double[] demand = new double[amount];
 		byte[] u = new byte[amount];
 		byte currentRelaisState = getCurrentRelaisStateAsByte();
 
 		for (int minute = 0; minute < amount; minute++) {
 			demand[minute] = _demands[minStartDate + minute];
 			u[minute] = currentRelaisState;
 		}
 
 		double[][] result = SingleNodeDHWHThermalModel.simulateDHWH(u, _currentTemp, demand, 60000);
 		return result;
 	}
 
 	private byte getCurrentRelaisStateAsByte() {
 		byte currentRelaisState = 0;
 		if (_relaisState) {
 			currentRelaisState = 1;
 		}
 		return currentRelaisState;
 	}
 
 	private int getMinutesSinceYearStarted(Calendar date) {
 		Calendar c = Calendar.getInstance();
 		c.set(Calendar.DAY_OF_MONTH, 1);
 		c.set(Calendar.MONTH, 0);
 		c.set(Calendar.HOUR_OF_DAY, 1);
 		c.set(Calendar.MINUTE, 0);
 		c.set(Calendar.SECOND, 0);
 		c.set(Calendar.YEAR, date.get(Calendar.YEAR));
 		int minutesSinceStartOfYear = (int) Math.max(0, Math.floor((date.getTimeInMillis() - c.getTimeInMillis()) / 1000.0 / 60.0));
 		return minutesSinceStartOfYear;
 	}
 
 	private float[] readFromFile() throws IOException {
 		ArrayList<String> values = new ArrayList<String>();
 		FileReader fr = new FileReader("./simData/Deterministic1min_DHW.txt");
 		BufferedReader br = new BufferedReader(fr);
 		String line;
 		while ((line = br.readLine()) != null) {
 			values.add(line);
 		}
 		br.close();
 		fr.close();
 
 		float[] retVal = new float[values.size()];
 		for (int i = 0; i < values.size(); i++) {
			retVal[i] = Float.parseFloat(values.get(i));
 		}
 		return retVal;
 	}
 
 	private float[][] readPricesFromFile() throws IOException {
 		ArrayList<String> values = new ArrayList<String>();
 		FileReader fr = new FileReader("./simData/prices2012.csv");
 		BufferedReader br = new BufferedReader(fr);
 		String line;
 		while ((line = br.readLine()) != null) {
 			values.add(line);
 		}
 		br.close();
 		fr.close();
 
 		float[][] retVal = new float[values.size()][24];
 		for (int i = 0; i < values.size(); i++) {
 			String value = values.get(i);
 			String[] valueSplit = value.split(";");
 			for (int j = 0; j < 24; j++) {
 				try {
 					retVal[i][j] = Float.parseFloat(valueSplit[j]);
 				} catch (Exception e) {
 					// e.printStackTrace();
 				}
 			}
 		}
 		return retVal;
 	}
 
 }
