 package aurora.hwc.fdcalibration;
 
 import java.net.*;
 import java.io.*;
 import java.text.ParsePosition;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 import aurora.service.*;
 
 public class PeMSClearinghouseInterpreter {
 
 	public HashMap<String,DataSource> PeMSFileToVDS;
 
 	public PeMSClearinghouseInterpreter(HashMap<String,DataSource> f){
 		PeMSFileToVDS=f;
 	}
 
     public void Read5minData(HashMap <Integer,FiveMinuteData> data, Updatable updater) throws Exception {
 
     	// step through data file
     	if (updater != null)
     		updater.notify_update(5);
     
     	int count = 0;
     	for(DataSource datasource : PeMSFileToVDS.values()){
     		URL dataurl = datasource.getUrl();
     		String dataformat = datasource.getFormat();
     		
     		count++;
    
     		System.out.println(count);
  
     		if(dataformat.equalsIgnoreCase("pems"))
 				ReadPeMSClearingHouse(data,datasource,dataurl);
 
     		if(dataformat.equalsIgnoreCase("caltransdbx"))
 				ReadCaltransDbx(data,datasource,dataurl);
     			
             if (updater != null)
             	updater.notify_update(Math.round(50*(((float)count+1)/ PeMSFileToVDS.size())));
     	}
     	 
     }
 
     private static Date ConvertTime(final String timestr) {
         SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
         ParsePosition pp = new ParsePosition(0);
         return formatter.parse(timestr,pp);
     }
     
     private static void ReadPeMSClearingHouse(HashMap <Integer,FiveMinuteData> data,DataSource datasource,URL dataurl) throws NumberFormatException, IOException{
 		int lane;
     	String line,str;
     	int indexof;
     	ArrayList<Integer> lanes;
    	ArrayList<Float> laneflw = new ArrayList<Float>();
    	ArrayList<Float> laneocc = new ArrayList<Float>();
    	ArrayList<Float> lanespd = new ArrayList<Float>();
         Calendar calendar = Calendar.getInstance();
     	float flw,dty,spd;
     	long time;
     	
 		URLConnection uc = dataurl.openConnection();
 		BufferedReader fin = new BufferedReader(new InputStreamReader(uc.getInputStream()));
 		
     	while ((line=fin.readLine()) != null) {
             String f[] = line.split(",");
             int vds = Integer.parseInt(f[1]);
 
             indexof = datasource.getFor_vds().indexOf(vds);
             if(indexof<0)
             	continue;
             
     		calendar.setTime(ConvertTime(f[0]));
     		time = calendar.getTime().getTime()/1000;
     
             lanes = datasource.getFor_vdslanes().get(indexof);
        	laneflw.clear();
        	laneocc.clear();
        	lanespd.clear();
         
         	// store in lane-wise ArrayList
             for (int j = 0; j < lanes.size(); j++) {
             	lane = lanes.get(j)-1;
             	str = f[5*(lane+1)+8];
             	if(str.isEmpty())
                 	laneflw.add(Float.NaN); 
             	else
             		laneflw.add(Float.parseFloat(str)); 
             	str = f[5*(lane+1)+9];
             	if(str.isEmpty())
             		laneocc.add(Float.NaN); 
             	else
             		laneocc.add(Float.parseFloat(str)); 
             	str = f[5*(lane+1)+10];
             	if(str.isEmpty())
             		lanespd.add(Float.NaN); 
             	else
             		lanespd.add(Float.parseFloat(str));
             }
 
             // compute totals
             flw = 0;
             dty = 0;
             spd = 0;
             for (int j = 0; j < lanes.size(); j++) {
             	flw += laneflw.get(j)*12f;
             	spd += lanespd.get(j);
             }
             spd /= lanes.size();
             dty = flw/spd;
             
             // find the data structure and store. 
             FiveMinuteData D = data.get(vds);
             D.flw.add(flw);
             D.dty.add(dty);
             D.spd.add(spd);
             D.time.add(time);
         }    	
         fin.close();
     }
     
     private static void ReadCaltransDbx(HashMap <Integer,FiveMinuteData> data,DataSource datasource,URL dataurl) throws NumberFormatException, IOException{
 		int lane;
     	String line,str;
     	int indexof;
     	ArrayList<Integer> lanes;
    	ArrayList<Float> laneflw = new ArrayList<Float>();
    	ArrayList<Float> laneocc = new ArrayList<Float>();
    	ArrayList<Float> lanespd = new ArrayList<Float>();
         Calendar calendar = Calendar.getInstance();
     	float flw,dty,spd;
     	long time;
     	
 		URLConnection uc = dataurl.openConnection();
 		BufferedReader fin = new BufferedReader(new InputStreamReader(uc.getInputStream()));
 		line=fin.readLine(); 	// discard the header
     	while ((line=fin.readLine()) != null) {
             String f[] = line.split("\t",-1);
             int vds = Integer.parseInt(f[1]);
 
             indexof = datasource.getFor_vds().indexOf(vds);
             if(indexof<0)
             	continue;
             
     		calendar.setTime(ConvertTime(f[0]));
     		time = calendar.getTime().getTime()/1000;
     
             lanes = datasource.getFor_vdslanes().get(indexof);
        	laneflw.clear();
        	laneocc.clear();
        	lanespd.clear();
         
         	// store in lane-wise ArrayList
             for (int j = 0; j < lanes.size(); j++) {
             	lane = lanes.get(j)-1;
             	str = f[6*(lane+1)+20];
             	if(str.isEmpty())
                 	laneflw.add(Float.NaN); 
             	else
             		laneflw.add(Float.parseFloat(str)); 
             	str = f[6*(lane+1)+22];
             	if(str.isEmpty())
             		laneocc.add(Float.NaN); 
             	else
             		laneocc.add(Float.parseFloat(str)); 
             	str = f[6*(lane+1)+23];
             	if(str.isEmpty())
             		lanespd.add(Float.NaN); 
             	else
             		lanespd.add(Float.parseFloat(str));
             }
 
             // compute totals
             flw = 0;
             dty = 0;
             spd = 0;
             for (int j = 0; j < lanes.size(); j++) {
             	flw += laneflw.get(j)*12f;
             	spd += lanespd.get(j);
             }
             spd /= lanes.size();
             dty = flw/spd;
             
             // find the data structure and store. 
             FiveMinuteData D = data.get(vds);
             D.flw.add(flw);
             D.dty.add(dty);
             D.spd.add(spd);
             D.time.add(time);
         }   
         fin.close(); 	
     }
     
 }
