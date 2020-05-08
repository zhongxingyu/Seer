 package osobny.uctovnik.controllers;
 
 import java.util.Calendar;
 import java.util.List;
 import java.util.Random;
 
 import osobny.uctovnik.async.AsyncForecastTask;
 import osobny.uctovnik.datasources.PohybDataSource;
 import osobny.uctovnik.datasources.UcetDataSource;
 import osobny.uctovnik.helpers.Constants;
 import osobny.uctovnik.helpers.GlobalParams;
 import osobny.uctovnik.objects.ForecastObject;
 import osobny.uctovnik.objects.PohybForForecastObject;
 import osobny.uctovnik.objects.UcetObject;
 import android.content.Context;
 
 public class ForecastController {
 
 	public interface ForecastActivity {
 		void repaintForecast(Calendar odDatum, long[] povodne, long[] predpovedane);
 	}
 	
 	private Context context;
 	private ForecastActivity activity;
 	
 	public ForecastController(ForecastActivity activity, Context context) {
 		this.context = context;
 		this.activity = activity;
 	}
 	
 	public List<UcetObject> getUcty() {
 		UcetDataSource ucetDataSource = new UcetDataSource(context);
 		ucetDataSource.open();
 		List<UcetObject> ucetObjects = ucetDataSource.getAll(null);
 		ucetDataSource.close();
 		return ucetObjects;
 	}
 	
 	public void predpoved(Long ucetId, int pocetMesiacov) {
 		if (GlobalParams.isAsync) {
 			AsyncForecastTask forecastTask = new AsyncForecastTask(this, activity, ucetId);
 			forecastTask.execute(pocetMesiacov);
 		} else {
 			ForecastObject object = doForecastForInterval(pocetMesiacov, ucetId);
 			this.activity.repaintForecast(object.date, object.originalValues, object.forecastedValues);
 		}		
 	}
 	
 	public ForecastObject doForecastForInterval(int interval, Long ucetId) {
 		Calendar odDatum = Calendar.getInstance();
 		odDatum.add(Calendar.MONTH, - interval);//nastavit obdobie 3-6-12 mesiacov
 		Calendar doDatum = Calendar.getInstance();
 		doDatum.add(Calendar.DAY_OF_MONTH, 1);
 		
 		long[] vstup = new long[Constants.daysBetween(odDatum, doDatum) + 1];
 		
 		PohybDataSource pohybDs = new PohybDataSource(context);
 		pohybDs.open();
 		List<PohybForForecastObject> pohyby = pohybDs.getListForForecast(ucetId, odDatum);
 		pohybDs.close();
 		
 		UcetDataSource ucetDs = new UcetDataSource(context);
 		ucetDs.open();
 		UcetObject ucet = ucetDs.get(ucetId);
 		ucetDs.close();
 		Long aktStav = ucet.getZostatok();
 		
 		//zoznam pohybov je zoradeny podla datumu ASC
 		//iterujeme interval datumov od zadu, ak datum posledneho elementu pohybov sa rovna aktualnemu
 		//tak pridame a pohyb odstranime zo zoznamu (aby sme nemuseli vzdy preiterovat aj zoznam pohybov)
		for (int i = vstup.length-1; !doDatum.before(odDatum) && i>=0; doDatum.add(Calendar.DATE, -1), i--) {
 			if (pohyby.size() > 0 && Constants.areDatesEqual(doDatum,pohyby.get(pohyby.size()-1).getDatum())) { //ked k datumu existuje pohyb
 				aktStav += -1 * pohyby.get(pohyby.size()-1).getSuma();
 				//odstranime ten datum zo zoznamu pohybov
 				pohyby.remove(pohyby.size() - 1);
 			}
 			vstup[i] = aktStav;
 		}
 
 		long[] forecastedData = generateForecast(vstup);
 		return new ForecastObject(odDatum, vstup, forecastedData);
 	}
 	
 	public long[] generateForecast(long[] xValues) {
 		long[] difference = new long[xValues.length];
 		difference[0] = 0; // ak je difference[i] > 0 -> CR, ak < 0 -> DB
 		
 		long maxCr = 0, maxDb = 0; // maximalny CR, maximalny DB
 		int minIntCr = 0, maxIntCr = 0, crCount = 0; // min cas medzi 2 CR, max
 														// cas medzi 2 CR, pocet
 														// CR
 		long sumCr = 0; // suma CR
 		//int sumIntCr = 0; // suma casovych intervalov medzi CR
 		int lastCrInd = -1;
 		int minIntDb = 0, maxIntDb = 0, dbCount = 0; // min cas medzi 2 DB, max
 														// cas medzi 2 DB, pocet
 														// DB
 		long sumDb = 0; // suma DB
 		//int sumIntDb = 0; // suma casovych intervalov medzi DB
 		int lastDbInd = -1;
 
 		for (int i = 1; i < xValues.length; i++) { // iteracia cez zoznam historickych udajov
 			//ziskanie statistickych udajov ako maximalne hodnoty, intenzita CR a DB
 			difference[i] = xValues[i] - xValues[i - 1];
 			if (difference[i] < 0) { // DB
 				if (lastDbInd != -1) { // inicializovanie dat, prvy DB
 					int casMedziDb = i - lastDbInd;
 					if (casMedziDb > maxIntDb) {
 						maxIntDb = casMedziDb;
 					}
 					if (casMedziDb < minIntDb || minIntDb == 0) {
 						minIntDb = casMedziDb;
 					}
 					//sumIntDb += casMedziDb;
 				}
 				lastDbInd = i;
 				dbCount++;
 				sumDb += difference[i];
 
 				if (maxDb > difference[i]) {
 					maxDb = difference[i];
 				}
 			} else if (difference[i] > 0) { // CR
 				if (lastCrInd != -1) { // inicializovanie dat, prvy CR
 					int casMedziCr = i - lastCrInd;
 					if (casMedziCr > maxIntCr) {
 						maxIntCr = casMedziCr;
 					}
 					if (casMedziCr < minIntCr || minIntCr == 0) {
 						minIntCr = casMedziCr;
 					}
 					//sumIntCr += casMedziCr;
 				}
 				lastCrInd = i;
 				crCount++;
 				sumCr += difference[i];
 				if (maxCr < difference[i]) {
 					maxCr = difference[i];
 				}
 			}
 
 		}
 		
 		/*
 		System.out.println("maxCR:" + maxCr + " maxDB:" + maxDb);
 		System.out.println("Min. cas medzi CR:" + minIntCr);
 		System.out.println("Max. cas medzi CR:" + maxIntCr);
 		System.out.println("CR SUM:" + sumCr + " pocet:" + crCount + " Atlag:"
 				+ sumCr / crCount);
 		System.out.println("INT SUM:" + sumIntCr + " Atlag:" + sumIntCr
 				/ crCount);
 		System.out.println("Min. cas medzi DB:" + minIntDb);
 		System.out.println("Max. cas medzi DB:" + maxIntDb);
 		System.out.println("DB SUM:" + sumDb + " pocet:" + dbCount + " Atlag:"
 				+ sumDb / dbCount);
 		System.out.println("INT SUM:" + sumIntDb + " Atlag:" + sumIntDb
 				/ dbCount);
 		*/
 		
 		/*
 		 * Na generovanie CR a DB sa pouziva Normalne rozdelenie
 		 * */
 		double stredHodCr = 1.0 * sumCr / crCount;
 		double smerOdchCr = (maxCr - stredHodCr) / 3;
 		double stredHodDb = 1.0 * sumDb / dbCount;
 		double smerOdchDb = (maxDb - stredHodDb) / 3;
 
 		double pstCr = crCount * 1.0 / xValues.length; // pst vyskytu cr
 		double pstDb = dbCount * 1.0 / xValues.length; // pst vyskytu db
 
 		/*
 		 * na predpoved intervalu I potrebujeme historicke data z 2*I
 		 */
 		/*
 		int generCr = 0;
 		int generDb = 0;
 		long crss = 0;
 		long dbss = 0;
 		*/
 		long stavUctu = xValues[xValues.length - 1]; //posledny den z historickych udajov
 		//System.out.println("Posledny den-stav: " + stavUctu);
 		Random randomGeneratorCr = new Random(13);
 		Random randomGeneratorDb = new Random(17);
 		long[] retVal = new long[xValues.length/2];
 		for (int i = 0; i < xValues.length / 2; i++) {
 			double randCr = randomGeneratorCr.nextDouble();
 			double randDb = randomGeneratorDb.nextDouble();
 			if (randCr < pstCr) {
 				//generCr++;
 				long cr = (long)(randomGeneratorCr.nextGaussian() * smerOdchCr + stredHodCr);
 				//crss +=cr;
 				stavUctu += cr;
 			}
 
 			if (randDb < pstDb) {
 				//generDb++;
 				long db = (long)(randomGeneratorDb.nextGaussian() * smerOdchDb + stredHodDb);
 				//dbss += db;
 				stavUctu += db;
 			}
 			retVal[i] = stavUctu;
 			//System.out.println(" " + stavUctu);
 		}
 		
 		return retVal;
 	}
 }
