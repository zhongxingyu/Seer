 import java.io.*;
 import java.util.*;
 import java.util.Map.Entry;
import java.lang.Exception;
 import java.nio.ByteBuffer;
 
 
 public class HTSMsg {
 	public Map<String,Object> map;
 	public static final int HMF_MAP  = 1;
 	public static final int HMF_S64  = 2;
 	public static final int HMF_STR  = 3;
 	public static final int HMF_BIN  = 4;
 	public static final int HMF_LIST = 5;
 	private Integer noName;
 
 	public HTSMsg(){
 		this.map = new HashMap<String, Object>();
 		noName = new Integer(0);
 	}
 	
 	public HTSMsg(String method, Map<String,Object> map){
 		this();
 		this.map.putAll(map);
 		this.map.put("method", method);
 	}
 	public HTSMsg(String method){
 		this();
 		this.map.put("method", method);
 	}
 	public HTSMsg(byte[] msg){
 		this();
 		deserialize(msg);
 	}
 	
 	private int getType(Object entry){
 		int ret = 0;
 		
 		if(entry instanceof Map){
 			ret = HMF_MAP;
 		}else if (entry instanceof Integer){
 			ret = HMF_S64;
 		}else if (entry instanceof String){
 			ret = HMF_STR;
 		}else if (entry instanceof byte[]){
 			ret = HMF_BIN;
 		}else if (entry instanceof List){
 			ret = HMF_LIST;
 		}else {
 			//TODO find some exception to throw
 		}
 		
 		return ret;
 	}
 
 	public byte[] serialize() throws IOException{
 		int length = 0; //TODO set a proper length
 		ByteArrayOutputStream msg = new ByteArrayOutputStream();
 		for (Entry<String, Object> entry : map.entrySet()){
 			String name = entry.getKey();
 			Object value = entry.getValue();
 			Integer type = getType(value);
 			byte[] data = new byte[0];
 			if(type.equals(HMF_MAP)){
 				//TODO get the data from the map
 			}else if(type.equals(HMF_S64)){
 				int intData=((Number)value).intValue();
 				data = new byte[]
 						{ 
 							(byte)(intData >> 24), 
 							(byte)(intData >> 16 & 0xff), 
 							(byte)(intData >> 8 & 0xff), 
 							(byte)(intData & 0xff) 
 						};
 			}else if(type.equals(HMF_STR)){
 				data=((String)value).getBytes();
 			}else if(type.equals(HMF_BIN)){
 				data=(byte[])value;
 			}else if(type.equals(HMF_LIST)){
 				System.out.println("Got a list, dot know what to do.");
 				//TODO get the data from the list
 			}else{
 				//TODO throw invalid type exception
 			}
 			
 			//TYPE
 			msg.write(type & 0xff);
 			//NAMELENGTH
 			msg.write((name.getBytes("UTF-8").length));
 			//DATALENGTH
 			for(int i=3;i>=0;i--)
 				msg.write((byte)((data.length >>> i*8) & 0xff));
 			//NAME
 			msg.write(name.getBytes("UTF-8"));
 			//DATA
 			msg.write(data);
 		}
 		length = msg.toByteArray().length;
 		byte[] ret = new byte[length+4];
 		for (int i = 0; i < 4; i++) {
 			int offset = (3 - i) * 8;
 			ret[i] = (byte) ((length >>> offset) & 0xFF);
 		}
 		int i=0;
 		for (byte b:msg.toByteArray()){
 			ret[i+4]=b;
 			i++;
 		}
 		return ret;
 	}
 
 	public void deserialize(byte[] msg){
 		int i = 0;
 
 		while(i<msg.length){			
 			Integer type=0;
 			short nameLength=0;
 			long dataLength=0;
 			String name="";
 			Object data="";
 			type = (int)msg[(int)i];
 			i++;
 			nameLength = msg[(int)i];
 			i++;
 			ByteBuffer buff = ByteBuffer.wrap(Arrays.copyOfRange(msg, i, i+4));
 			dataLength = buff.getInt();
 			i+=4;
 			try {
 				name = new String(Arrays.copyOfRange(msg, i, i+nameLength), "UTF-8");
 			} catch (UnsupportedEncodingException e) {
 				name = "";
 				e.printStackTrace();
 			}
 			i+=nameLength;
 			byte[] dataBytes = Arrays.copyOfRange(msg, i, (int)(i + dataLength));
 			if(type.equals(HMF_MAP)){
 				data = getMap(dataBytes,dataLength);
 			}else if(type.equals(HMF_S64)){
 				data = getS64(dataBytes,dataLength);
 			}else if(type.equals(HMF_STR)){
 				data = getString(dataBytes,dataLength);
 			}else if(type.equals(HMF_BIN)){
 				data = getBytes(dataBytes,dataLength);
 			}else if(type.equals(HMF_LIST)){
 				data = getList(dataBytes,dataLength);
 				System.out.println("Got a list, " + name + " " + Arrays.asList((Object[])data));
 			}else{
 				data="";
 				//TODO throw invalid type exception
 			}
 			if(map.containsKey(name)){
 				name = noName.toString();
 				noName++;
 			}
 			if (data.equals(null)) {
 				System.out.println("datan r null. " + name);				
 			}
 			if (name.equals("error")){
 				System.out.println(data);
 			}
 			
 			map.put(name, data);
 			i+=dataLength;			
 		}
 	}
 
 	private Object[] getList(byte[] dataBytes, long dataLength) {
 		Object[] data=((new HTSMsg(dataBytes)).map.values().toArray());
 		return data;
 	}
 
 	private Object getBytes(byte[] dataBytes, long dataLength) {
 		return dataBytes.clone();
 	}
 
 	private Object getString(byte[] dataBytes, long dataLength) {
 		try {
 			return new String(ByteBuffer.wrap(dataBytes, 0, (int)dataLength).array(), "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return "";
 		}		
 	}
 
 	private int getS64(byte[] dataBytes, long dataLength) {
 		byte[] s64 = new byte[4];
 		for (int k = 4-(int)dataLength ; k<4 ;k++)
 			s64[k] = dataBytes[k-4+(int)dataLength];
 		ByteBuffer buff = ByteBuffer.wrap(s64);
 		int data = buff.getInt();
 		return data;
 	}
 
 	private Map<String,Object> getMap(byte[] dataBytes, long dataLength) {
 		Map<String,Object> data = (new HTSMsg(dataBytes)).map;
 		return data;
 	}
 	
 	
 }
