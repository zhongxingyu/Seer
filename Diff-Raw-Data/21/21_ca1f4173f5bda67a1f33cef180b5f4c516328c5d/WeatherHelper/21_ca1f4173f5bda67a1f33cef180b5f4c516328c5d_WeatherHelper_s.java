 package org.agmip.functions;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import static org.agmip.common.Functions.*;
 import org.agmip.common.Functions.CompareMode;
 import org.agmip.util.MapUtil;
 import static org.agmip.util.MapUtil.*;
 import org.agmip.util.MapUtil.BucketEntry;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Provide static functions for weather data handling
  *
  * @author Meng zhang
  */
 public class WeatherHelper {
 
     private static final Logger LOG = LoggerFactory.getLogger(WeatherHelper.class);
 
     /**
      * Calculate the AMP (annual amplitude of mean monthly temperature oC) and
      * TAV (Annual average ambient temperature oC)
      *
      * @param data The data map
      *
      * @return A {@code HashMap} contains {@code TAV} and {@code TAMP}, the key
      * is their ICASA variable name
      */
     public static HashMap<String, String> getTavAndAmp(HashMap data) {
 
         HashMap<String, String> results = new HashMap<String, String>();
         HashMap<Integer, MonthlyAvg> tyear = new HashMap<Integer, MonthlyAvg>();
         MonthlyAvg tmonth;
         MonthlyAvg tavAllYears;
         ArrayList<String> tampAllYears;
         String tav;
         String tamp;
         ArrayList<HashMap<String, String>> dailyArr = getDailyData(data);
 
         // Load daily data
         for (int i = 0; i < dailyArr.size(); i++) {
 
             String date;
             int year;
             int month;
             String tmax;
             String tmin;
             String tavgDaily;
 
             HashMap<String, String> dailyData = dailyArr.get(i);
             date = getValueOr(dailyData, "w_date", "").trim();
             if (date.equals("")) {
                 LOG.warn("There is daily data do not having a valid date");
                 continue;
             } else {
                 Calendar cal = Calendar.getInstance();
                 cal.setTime(convertFromAgmipDateString(date));
                 year = cal.get(Calendar.YEAR);
                 month = cal.get(Calendar.MONTH);
                 tmonth = tyear.get(year);
                 if (tmonth == null) {
                     tmonth = new MonthlyAvg();
                     tyear.put(year, tmonth);
                 }
             }
 
             tmax = getValueOr(dailyData, "tmax", "").trim();
             tmin = getValueOr(dailyData, "tmin", "").trim();
             tavgDaily = average(tmax, tmin);
             if (tavgDaily != null) {
                 tmonth.add(month, tavgDaily);
             }
         }
 
         // Calculate daily data
         tavAllYears = new MonthlyAvg();
         tampAllYears = new ArrayList();
         for (Iterator<Integer> it = tyear.keySet().iterator(); it.hasNext();) {
             int year = it.next();
             tmonth = tyear.get(year);
             String[] tavgs = tmonth.getAllAvg();
             // TAV
             for (int month = 0; month < tavgs.length; month++) {
                 tavAllYears.add(month, tavgs[month]);
             }
             // TAMP
             String tampt = substract(max(tavgs), min(tavgs));
             if (tampt != null) {
                 tampAllYears.add(tampt);
             }
 
         }
         tav = average(2, removeNull(tavAllYears.getAllAvg()));
         tamp = average(2, tampAllYears.toArray(new String[0]));
 
         if (tav != null) {
             results.put("tav", tav);
         }
         if (tamp != null) {
             results.put("tamp", tamp);
         }
         return results;
     }
 
     /**
      * Calculate the monthly average for each month
      */
     private static class MonthlyAvg {
 
         private int[] days = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
         private String[] avg = new String[12];
 
         public void add(int month, String val) {
             if (val == null) {
                 return;
             }
             if (this.avg[month] == null) {
                 this.avg[month] = val;
                 days[month] = 1;
             } else {
                 this.avg[month] = sum(this.avg[month], val);
                 days[month]++;
             }
         }
 
         public String getAvg(int month) {
             if (days[month] <= 0) {
                 return null;
             } else {
                 return divide(avg[month], days[month] + "");
             }
         }
 
         public String[] getAllAvg() {
             String[] ret = new String[days.length];
             for (int i = 0; i < days.length; i++) {
                 ret[i] = getAvg(i);
             }
             return ret;
         }
     }
 
     /**
      * Get weather daily data array from data holder.
      *
      * @param data The experiment data holder
      * @return The weather daily data array
      */
     protected static ArrayList getDailyData(HashMap data) {
         if (data.containsKey("weather")) {
             return MapUtil.getBucket(data, "weather").getDataList();
         } else {
             return new BucketEntry(data).getDataList();
         }
     }
 
     /**
      * Get weather station data set from data holder.
      *
      * @param data The experiment data holder
      * @return The weather station data set
      */
     protected static HashMap getWthData(HashMap data) {
         if (data.containsKey("weather")) {
             return getObjectOr(data, "weather", new HashMap());
         } else {
             return data;
         }
     }
 
     /**
      * Calculate the reference evapotranspiration (ETo) by means of the
      * FAO-Penman Monteith equation.
      *
      * @param data The data map
      *
      * @return An {@code ArrayList} of {@code ETo} for daily weather record.
      */
     public static HashMap<String, ArrayList<String>> getEto(HashMap data) {
         HashMap<String, ArrayList<String>> results = new HashMap<String, ArrayList<String>>();
         ArrayList<HashMap<String, String>> dailyArr = getDailyData(data);
 
         // Step 1. Atmospheric pressure (P) [kPa]
        String wst_elev = getValueOr(data, "wst_elev", "");
         if (wst_elev.equals("")) {
             return results;
         }
         String P = multiply("101.3", pow(divide(substract("293", product("0.0065", wst_elev)), "293"), "5.26"));
 
         // Step 2. Psychrometric constant (γ)
         String gamma = product("0.664742", "0.001", P);
 
         // Eq.17 (Step 8)
         // latitude [rad]
        String wst_lat = getValueOr(data, "wst_lat", "");
         String phi = divide(product(wst_lat, Math.PI + ""), "180");
 
         // Get other potentially necessary meta data from Ace data set
        String psyvnt = getValueOr(data, "psyvnt", "").trim();
         String aPsy = "";
         if (psyvnt.equals("Forced")) {
             aPsy = "0.000662";
         } else if (psyvnt.equals("Natural")) {
             aPsy = "0.00800";
         }
         String rPsy = multiply(aPsy, P);
        String amth = getValueOr(data, "amth", "0.25");
        String bmth = getValueOr(data, "bmth", "0.50");
 
         // Calculate daily ETO
         ArrayList<String> etoArr = new ArrayList<String>();
         for (int i = 0; i < dailyArr.size(); i++) {
             HashMap<String, String> dailyData = dailyArr.get(i);
 
             // Get daily TMAX and TMIN
             String tMin = getValueOr(dailyData, "tmin", "").trim();
             String tMax = getValueOr(dailyData, "tmax", "").trim();
             if (tMin.equals("") || tMax.equals("")) {
                 etoArr.add(null);
                 continue;
             }
 
             // Step 3. Mean air temperature (Tmean) [°C]
             String tMean = average(tMin, tMax);
 
             // Step 4. Saturation vapour pressure (es)
             String e_tMax = multiply("0.6108", exp(divide(multiply("17.27", tMax), sum(tMax, "237.3"))));
             String e_tMin = multiply("0.6108", exp(divide(multiply("17.27", tMin), sum(tMin, "237.3"))));
             String es = average(e_tMax, e_tMin);
 
             // Step 5. Slope of the saturation vapour pressure curve (Δ)
             String slope = divide(product("4098", "0.6108", exp(divide(multiply("17.27", tMean), sum(tMean, "237.3")))), pow(sum(tMean, "237.3"), "2"));
 
             // Step 6. Actual vapour pressure (ea) [kPa]
             String ea;
             String alt1;
             String alt2;
             // Method 1 IF VPRSD is available in the ACE data base
             if (!(alt1 = getValueOr(dailyData, "vprsd", "").trim()).equals("")) {
                 // ea = VPRSD
                 ea = alt1;
 
             } // Method 2 IF TDEW is available in the ACE data base
             else if (!(alt1 = getValueOr(dailyData, "tdew", "").trim()).equals("")) {
                 // derive ea from the given dew point temperature
                 ea = multiply("0.6108", exp(divide(multiply("17.27", alt1), sum(alt1, "237.3"))));
 
             } // Method 3 IF RHMND and RHMXD are available in the ACE data base
             else if (!(alt1 = getValueOr(dailyData, "rhmxd", "").trim()).equals("")
                     && !(alt2 = getValueOr(dailyData, "rhmnd", "").trim()).equals("")) {
                 // ea from the given maximum and minimum Relative Humidity
                 ea = average(product(e_tMin, alt1, "0.01"), product(e_tMax, alt2, "0.01"));
 
             } // Method 4 IF TDRY, TWET and PSYVNT are available in the ACE data base
             else if (!(alt1 = getValueOr(dailyData, "tdry", "").trim()).equals("")
                     && !(alt2 = getValueOr(dailyData, "twet", "").trim()).equals("")
                     && rPsy != null) {
                 // derive ea from the psychrometric data
                 String e_tWet = multiply("0.6108", exp(divide(multiply("17.27", alt2), sum(alt2, "237.3"))));
                 ea = substract(e_tWet, multiply(rPsy, substract(alt1, alt2)));
 
             } // Method 5 use Tmin as an approximation of Tdew
             else {
                 ea = e_tMin;
             }
 
             // Step 7. Vapour pressure difference (es - ea)
             String vpDiff = substract(es, ea);
 
             // Step 8. Extra terrestrial radiation (Ra) and daylight hours (N)
             Date w_date = convertFromAgmipDateString(getValueOr(dailyData, "w_date", ""));
             if (w_date == null) {
                 etoArr.add(null);
                 continue;
             }
             Calendar cal = Calendar.getInstance();
             cal.setTime((w_date));
             String J = cal.get(Calendar.DAY_OF_YEAR) + "";
             String dr = sum("1", multiply("0.033", cos(product("2", divide(Math.PI + "", "365"), J))));
             String delta = multiply("0.409", sin(substract(product("2", divide(Math.PI + "", "365"), J), "1.39")));
             String omegas = acos(product("-1", tan(phi), tan(delta)));
             String ra = divide(product("1440", "0.0820", dr, sum(product(omegas, sin(phi), sin(delta)), product(cos(phi), cos(delta), sin(omegas)))), Math.PI + ""); // TODO
             String N = divide(multiply("24", omegas), Math.PI + "");
 
             // Step 9. Solar radiation (Rs)
             String rs;
             // Method 1. IF SRAD is available in the ACE data base
             if (!(alt1 = getValueOr(dailyData, "srad", "").trim()).equals("")) {
                 rs = alt1;
             } // Method 2. IF SUNH is available in the ACE data base
             else if (!(alt1 = getValueOr(dailyData, "sunh", "").trim()).equals("")) {
                 rs = multiply(sum(amth, divide(multiply(bmth, alt1), N)), ra);
             } // Method 3. LSE use Tmin and Tmax to estimate Rs by means of the Hargreaves equation
             else {
                rs = product(getKrsValue(data), sqrt(substract(tMax, tMin)), ra);
             }
 
             // Step 10. Clear-Sky solar radiation (Rso)
             String rso = multiply(sum("0.75", product("2e-5", wst_elev)), ra);
 
             // Step 11. Net solar radiation (Rns)
             String rns = multiply(substract("1", "0.23"), rs);
 
             // Step 12. Net long wave  radiation (Rnl)
             String rnl = product("4.903e-9",
                     average(pow(sum(tMax, "273.16"), "4"), pow(sum(tMin, "273.16"), "4")),
                     substract("0.34", multiply("0.14", sqrt(ea))),
                     substract(divide(multiply("1.35", rs), rso), "0.35"));
 
             // Step 13. Net radiation (Rn)
             String rn = substract(rns, rnl);
 
             // Step 14. Wind speed at 2 meter above ground level (u2)
             // Method 1. WIND is given in the ACE data base
             String u2;
             if (!(alt1 = getValueOr(dailyData, "wind", "").trim()).equals("")) {
                 String uz = divide(multiply("1000", alt1), "86400", 4);
                 // CASE 1. Reference height for wind speed measurement (WNDHT)  is 2 meter
                if (compare(alt2 = getValueOr(data, "wndht", "").trim(), "2", CompareMode.EQUAL)) {
                     u2 = uz;
                 } // CASE 2. Reference height for wind speed measurement (WNDHT)  is NOT 2 meter
                 else {
                     u2 = divide(multiply(uz, "4.87"), log(substract(multiply("67.8", alt2), "5.42")));
                 }
             } else {
                 u2 = "2";
             }
 
             // Step 15. Reference evapotranspiration (ETo)
             String eto = divide(
                     sum(product("0.408", slope, rn), divide(product(gamma, "900", u2, vpDiff), sum(tMean, "273"))),
                     sum(slope, multiply(gamma, sum("1", multiply("0.34", u2)))));
             etoArr.add(round(eto, 2));
         }
         results.put("eto", etoArr);
         return results;
     }
 
     /**
      * Based on the location of weather station, return the adjustment
      * coefficient used for Solar radiation calculation. The default value is
      * 0.16 for interior locations and 0.19 for coastal locations
      *
      * @param data The data map
      * @return adjustment coefficient [°C-0.5]
      */
     private static String getKrsValue(HashMap data) {
         // TODO waiting for GIS system imported
         return "0.16";  // or "0.19";
     }
 }
