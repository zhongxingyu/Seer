 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 
 
 
 public  class MeasurementContainer {
 	private static Object lock=new Object();
 	private static ArrayList<Measurement> mesurments=new ArrayList<Measurement>();
 	static{
 		SimpleMeasurement m=new SimpleMeasurement("test1");
 		m.addData((new Date()).getTime(), "0.5");
 		m.addData((new Date()).getTime(), "0.56");
 		m.addData((new Date()).getTime(), "0.6");
 		mesurments.add(m);
 		mesurments.add(new SimpleMeasurement("test2"));
 		mesurments.add(new SimpleMeasurement("test3"));
		mesurments.add(new SimpleMeasurement("test3"));
		ComplexMeasurement cm=new ComplexMeasurement(m.getName());
 		//(new Thread(new SensorReciver())).start();
 		
 	}
 	//TODO do zmiany gdy dogadamy sie co do ramki
 	public static void parseSensorData(String recievedData){
 		String [] data= recievedData.split(";");
 		synchronized(lock){
 		if(data.length==3) {
 			if(existMesurment(data[0])){
 				getByName(data[0]).addData((new Date()).getTime(), data[1]);
 			}
 			else{
 				SimpleMeasurement m=new SimpleMeasurement(data[0]);
 				mesurments.add(m);
 				m.addData((new Date()).getTime(), data[1]);
 			}
 				System.out.println(Arrays.asList(data));
 			}
 		}
 	}
 	public static String getJSON(Measurement m){
 		StringBuilder sb=new StringBuilder();
 		sb.append("{ ");
 		
 		sb.append("\"Name\" : \"").append(m.getName()).append("\" ,");
 		
 		sb.append("\"data\": [ ");
 		for(Data d:m.getData()){
 			sb.append(" { \"timestamp\":\"").append(d.getTimestamp()).append("\" , \"data\":\"").append(d.getData()).append("\" },");
 		}
 		sb.deleteCharAt(sb.length()-1);
 		sb.append("]");
 
 		sb.append("} ");
 		return sb.toString();
 	}
 	public static ArrayList<Integer> getIDs(){
 		ArrayList<Integer> list=new ArrayList<Integer>();
 		for(Measurement m:mesurments){
 			list.add(m.getID());
 		}
 		return list;
 	}
 	public static Measurement getById(int id){
 		for(Measurement m:mesurments){
 			if(m.getID()==id)
 				return m;
 		}
 		return null;
 	}
 	public static Measurement getByName(String name){
 		for(Measurement m:mesurments){
 			if(m.getName().equals(name))
 				return m;
 		}
 		return null;
 	}
 	public static boolean existMesurment(String name){
 		System.out.println("name: "+name);
 		for(Measurement m:mesurments){
 			System.out.println("m.name: "+m.getName());
 			if(m.getName().equals(name))
 				return true;
 		}
 		return false;
 	}
 	
 	synchronized public static void addMeasurement(Measurement m){
 		if(existMesurment(m.getName()))
 			return;
 		mesurments.add(m);
 	}
 }
