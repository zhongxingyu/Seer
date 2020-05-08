 package org.georepublic.json;
 
 import java.net.*;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.apache.wink.json4j.*; 
 import org.georepublic.bean.*;
 
 public class JsonProc {
 
     private String url = null;
     private ArrayList <StreamGauge> asgList   = new ArrayList<StreamGauge>();
     private ArrayList <TideGauge> tdList      = new ArrayList<TideGauge>();
     private ArrayList <RainGauge>   rainList  = new ArrayList<RainGauge>();
     private ArrayList <WaterGauge>  waterList = new ArrayList<WaterGauge>();
     
     public JsonProc(String url) {
         this.setUrl( url );
     }
 
     public void setUrl( String url ) {
         this.url = url;
     }
     
     public void getJsonData() {
                 
         if( this.url == null )
             return;
         
         try {
             URL mUrl = new URL(this.url);
 
             JSONObject jo  = (JSONObject)JSON.parse(mUrl.openStream());
             JSONObject aws = jo.getJSONObject("aws");
             JSONObject asg = jo.getJSONObject("asg");
             JSONObject arg = jo.getJSONObject("arg");
             JSONObject td  = jo.getJSONObject("td");
             
             this.setASGData(asg);
             this.setAWSData(aws);
             this.setARGData(arg);
             this.setTDData(td);
                         
         } catch (Exception e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         
         return;
     }
 
     /**
      * Automated Water Station
      * @param JSONObject aws
      */
     public void setAWSData(JSONObject aws) {
         Iterator<?> asgKeys = aws.keys();
         String sensor       = new String();
         
         ArrayList<WaterGauge> aList = new ArrayList<WaterGauge>();
         
         while( asgKeys.hasNext() ) {
             sensor = (String)asgKeys.next();
             
             try {
             
                 JSONObject station = aws.getJSONObject( sensor );
                 JSONObject vals    = station.getJSONObject("values");
                 JSONObject temp    = vals.getJSONObject("temp");
                 JSONObject humi    = vals.getJSONObject("humi");
                 JSONObject pres    = vals.getJSONObject("pres");
                 JSONObject rain    = vals.getJSONObject("rain");
                 
                 String stationID    = station.getString("station_id");
                 String stationTime  = station.getString("time");
                 double stationLat   = Double.parseDouble(
                         station.getString("lat"));
                 double stationLon   = Double.parseDouble(
                         station.getString("lng"));
                 double stationTemp  = Double.parseDouble(
                         temp.getString("value") );
                 double stationHumi  = Double.parseDouble(
                         humi.getString("value") );
                 double stationPres  = Double.parseDouble(
                         pres.getString("value") );
                 double stationRain  = Double.parseDouble(
                         rain.getString("value") );
 
                 WaterGauge wg = new WaterGauge();
                 wg.setName(stationID);
                 wg.setTime(stationTime);
                 wg.setTemp(stationTemp);
                 wg.setHumi(stationHumi);
                 wg.setPres(stationPres);
                 wg.setRain(stationRain);
                 wg.setLat(stationLat);
                 wg.setLon(stationLon);
 
                 aList.add(wg);
             }
             catch( Exception e ) {
                 //e.printStackTrace();
             }
         }        
         
         this.setWaterList(aList);
         
     }
     
     /**
      * Automated Rain Gauge
      * @param JSONObject arg
      */
     public void setARGData(JSONObject arg) {
         Iterator<?> asgKeys = arg.keys();
         String sensor       = new String();
         
         ArrayList<RainGauge> rList = new ArrayList<RainGauge>();
         
         while( asgKeys.hasNext() ) {
             sensor = (String)asgKeys.next();
             
             try {
             
                 JSONObject station = arg.getJSONObject( sensor );
                 JSONObject vals    = station.getJSONObject("values");
                 JSONObject rain    = vals.getJSONObject("rain");
                 
                 String stationID    = station.getString("station_id");
                 String stationTime  = station.getString("time");
                 double stationLat   = Double.parseDouble(
                         station.getString("lat"));
                 double stationLon   = Double.parseDouble(
                         station.getString("lng"));
                 double stationVal   = Double.parseDouble(
                         rain.getString("value") );
 
                 RainGauge rg = new RainGauge();
                 rg.setName(stationID);
                 rg.setTime(stationTime);
                 rg.setValue(stationVal);
                 rg.setLat(stationLat);
                 rg.setLon(stationLon);
                 
                 rList.add(rg);
             }
             catch( Exception e ) {
                 //e.printStackTrace();
             }
         }     
         
         this.setRainList(rList);
     }
 
     /**
      * Automated Stream Gauge
      * @param asg
      */
     public void setASGData(JSONObject asg) {
         Iterator<?> asgKeys = asg.keys();
         String sensor       = new String();
         
         ArrayList <StreamGauge> flist = new ArrayList<StreamGauge>();
         
         while( asgKeys.hasNext() ) {
             sensor = (String)asgKeys.next();
             
             try {
             
                 JSONObject station = asg.getJSONObject( sensor );
                 JSONObject vals    = station.getJSONObject("values");
                 JSONObject wls     = vals.getJSONObject("wl");
             
                 String stationID   = station.getString("station_id");
                 String stationTime = station.getString("time");
                 double stationLat  = Double.parseDouble(
                         station.getString("lat"));
                 double stationLon  = Double.parseDouble(
                         station.getString("lng"));
                 double stationVal  = Double.parseDouble(
                         wls.getString("value") );
                 
                 double water_level_change = Double.parseDouble(
                         wls.getString("water_level_change"));
                 
                 double time_difference    = Double.parseDouble(
                         wls.getString("time_difference"));
                 
                 StreamGauge fd = new StreamGauge();
                 fd.setName(stationID);
                 fd.setTime(stationTime);
                 fd.setValue(stationVal);
                 fd.setLat(stationLat);
                 fd.setLon(stationLon);
                 fd.setTime_difference(time_difference);
                 fd.setWater_level_change(water_level_change);
                 
                 flist.add(fd);
             }
             catch( Exception e ) {
                // e.printStackTrace();
             }
         }
         
         this.setAsgList(flist);
     }
     
     /**
      * Tide data
      * @param JSONObject td
      */
     public void setTDData( JSONObject td ) {
         Iterator<?> asgKeys = td.keys();
         String sensor       = new String();
         
         ArrayList <TideGauge> flist = new ArrayList<TideGauge>();
         
         while( asgKeys.hasNext() ) {
             sensor = (String)asgKeys.next();
             
             try {
             
                 JSONObject station    = td.getJSONObject( sensor );
                 JSONObject vals       = station.getJSONObject("values");
                 JSONObject other_data = station.getJSONObject("other_data");
                 JSONObject tdval      = vals.getJSONObject("td");
                 JSONArray  sun        = other_data.getJSONArray("sun");
                 JSONArray  moon       = other_data.getJSONArray("moon");
                                 
                 String stationID   = station.getString("station_id");
                 String stationTime = station.getString("time");
                 
                 double stationLat  = Double.parseDouble(
                         station.getString("lat"));
                 double stationLon  = Double.parseDouble(
                         station.getString("lng"));
                 double stationVal  = Double.parseDouble(
                         tdval.getString("value") );
                 double water_level_change  = Double.parseDouble(
                         tdval.getString("water_level_change") );
                 double time_difference  = Double.parseDouble(
                         tdval.getString("time_difference") );
                 
                 String sunrise  = sun.getJSONArray(0).getString(1);
                 String sunset   = sun.getJSONArray(1).getString(1);
                 String moonrise = moon.getJSONArray(0).getString(1);
                 String moonset  = moon.getJSONArray(1).getString(1);
                 
                 TideGauge fd = new TideGauge();
                 
                 fd.setName(stationID);
                 fd.setTime(stationTime);
                 fd.setValue(stationVal);
                 fd.setLat(stationLat);
                 fd.setLon(stationLon);
                 fd.setWater_level_change(water_level_change);
                 fd.setTime_difference(time_difference);
                 fd.setSunrise( sunrise );
                 fd.setSunset( sunset );
                 fd.setMoonrise(moonrise);
                 fd.setMoonset(moonset);
                 
                 flist.add(fd);
             }
             catch( Exception e ) {
                 e.printStackTrace();
             }
         }
         this.setTdList(flist);
     }
 
     /**
      * @return the asgList
      */
     public ArrayList<StreamGauge> getAsgList() {
         return asgList;
     }
 
     /**
      * @param asgList the asgList to set
      */
     public void setAsgList(ArrayList<StreamGauge> asgList) {
         this.asgList = asgList;
     }
 
     /**
      * @return the tdList
      */
     public ArrayList<TideGauge> getTdList() {
         return tdList;
     }
 
     /**
      * @param tdList the tdList to set
      */
     public void setTdList(ArrayList<TideGauge> tdList) {
         this.tdList = tdList;
     }
 
     /**
      * @return the rainList
      */
     public ArrayList<RainGauge> getRainList() {
         return rainList;
     }
 
     /**
      * @param rainList the rainList to set
      */
     public void setRainList(ArrayList<RainGauge> rainList) {
         this.rainList = rainList;
     }
 
     /**
      * @return the waterList
      */
     public ArrayList<WaterGauge> getWaterList() {
         return waterList;
     }
 
     /**
      * @param waterList the waterList to set
      */
     public void setWaterList(ArrayList<WaterGauge> waterList) {
         this.waterList = waterList;
     }  
 }
