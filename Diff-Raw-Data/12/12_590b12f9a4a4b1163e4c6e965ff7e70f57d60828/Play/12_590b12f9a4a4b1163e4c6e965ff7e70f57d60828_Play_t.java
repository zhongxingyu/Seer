 package no.uis.ux.cipsi;
 
 import java.io.IOException;
 import java.lang.reflect.Array;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.nio.ByteBuffer;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.TimeZone;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.HBaseConfiguration;
 import org.apache.hadoop.hbase.KeyValue;
 import org.apache.hadoop.hbase.client.Get;
 import org.apache.hadoop.hbase.client.HTable;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.client.ResultScanner;
 import org.apache.hadoop.hbase.client.Scan;
 import org.apache.hadoop.hbase.filter.InclusiveStopFilter;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.apache.http.util.ByteArrayBuffer;
 
 import com.google.common.base.Splitter;
 import com.google.common.collect.Lists;
 
 public class Play {
 
 	private static void date() {
 		// TODO Auto-generated method stub
 		String t = "2013-03-21 10:29:33.730";
 //		String t = "2001.07.04 AD at 12:08:56 PDT";
 		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
 //		df.setTimeZone(TimeZone.getDefault());
 //		df.setLenient(false);
 	    Date date;
 		try {
 			date = df.parse(t);
 			
 			long epoch = date.getTime();
 			System.out.println(TimeZone.getDefault());
 			System.out.println(date.getHours());
 			System.out.println(date.getMinutes());
 			System.out.println(date.getSeconds());
 			System.out.println();
 			System.out.println(date.getMonth());
 			System.out.println(date.getDate());
 			System.out.println(date.getYear());
 			System.out.println(epoch);
 			System.out.println(new Date(epoch));
 			System.out.println(Bytes.toBytes(epoch).length);
 			for (int i = 0; i < Bytes.toBytes(epoch).length; i++) {
 				System.out.println(Bytes.toBytes(epoch)[i]);
 			}
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	private static void stringInt() {
 		// TODO Auto-generated method stub
 		String s = "";
 		System.out.println(Bytes.toBytes(s).length);
 		System.out.println(Bytes.toBytes(Integer.parseInt(s)).length);
 		System.out.println(Bytes.toBytes(Short.parseShort(s)).length);
 	}
 	
 	private static void stringIP(){
 		try {
 			InetAddress address = InetAddress.getByName("::1");
 			System.out.println(address.getAddress().length);
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		}
 	}
     private static void showStats( ByteBuffer b ) 
     {
     System.out.println( 
                  " bufferPosition: " +
                  b.position() +
                  " limit: " +
                  b.limit() +
                  " remaining: " +
                  b.remaining() +
                  " capacity: " +
                  b.capacity() );
     }
 	private static void byteBuffer(){
 		ByteBuffer buffer = ByteBuffer.allocate(4);
 		InetAddress address;
 		try {
 			address = InetAddress.getByName("152.94.1.131");
 			buffer.put(address.getAddress());
 //			buffer.putChar('b');
 //			showStats(buffer);
 			byte[] tmp = buffer.array();
 			System.out.println(InetAddress.getByAddress(tmp).getHostAddress());
 			System.out.println(InetAddress.getByAddress(tmp).getCanonicalHostName());
 			System.out.println(InetAddress.getByAddress(tmp).getHostName());
 			System.out.println(InetAddress.getByAddress(tmp));
 		} catch (UnknownHostException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 //		ArrayUtils.			
 //		InetAddress address;
 //		try {
 //			address = InetAddress.getByName("152.94.0.223");
 //			System.out.println(address.getAddress().length);
 //			ByteArrayBuffer arrayBuffer = new ByteArrayBuffer(10);
 //			arrayBuffer.append(address.getAddress(), 0, address.getAddress().length);
 //			arrayBuffer.append(5);
 //			System.out.println(arrayBuffer.buffer().length);
 //			for (int i = 0; i < arrayBuffer.length(); i++) {
 //				System.out.println(InetAddress.getByAddress(Arrays.copyOfRange(arrayBuffer.toByteArray(), 1, 5)));
 //			}
 //			
 //			List<Byte> list = new ArrayList<Byte>();
 //			Byte[] tmp = ArrayUtils.toObject("Hello".getBytes());
 //			
 //			list.addAll(Arrays.asList(ArrayUtils.toObject("Hello".getBytes())));
 //		} catch (UnknownHostException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		}
 	}
 	
 	private static void lists(){
		List<String> list1 = new ArrayList<String>();
 		list1.add("1");
 		list1.add("2");
 		list1.add("3");
 		list1.add("4");
 		list1.add("5");
 		list1.add("6");
 		list1.add("7");
 		for (int i = 0; i < list1.size(); i++) {
 			System.out.println(i + ": " + list1.get(i));
 		}
 //		list1.remove(2);
 //		list1.remove(5);
 		list1.set(2, null);
 		list1.set(5, null);
 		System.out.println();
 		for (int i = 0; i < list1.size(); i++) {
 			System.out.println(i + ": " + list1.get(i));
 		}
 		
 		list1.remove(null);
 		list1.remove(null);
 		System.out.println();
 		for (int i = 0; i < list1.size(); i++) {
 			System.out.println(i + ": " + list1.get(i));
 		}
 		
 		
		List<String> list2 = new ArrayList<String>();
 		
 	}
 	
 	private static void longop(){
 		long l =  9223372036854775807l;
 		System.out.println(l);
 		System.out.println(Bytes.toBytes(l).length);
 		for (int i = 0; i < Bytes.toBytes(l).length; i++) {
 			System.out.println(Bytes.toBytes(l)[i]);
 		}
 		byte[] bl = new byte[8];
 		System.arraycopy(bl, 0, Bytes.toBytes(l), 3, 4);
 		for (int i = 0; i < bl.length; i++) {
 			System.out.println(bl[i]);
 		}
 		System.out.println(Bytes.toLong(bl));
 	}
 	private static void byteop() {
 		byte dtos = Byte.parseByte("127");
 		System.out.println(Byte.toString((new byte[]{dtos})[0]));
 	}
 	
     /**
      * Get a row
      */
     public static void getOneRecord (Configuration conf, String tableName, byte[] rowKey) throws IOException{
         HTable table = new HTable(conf, tableName);
         Get get = new Get(rowKey);
         Result rs = table.get(get);
         for(KeyValue kv : rs.raw()){
             NetFlowCSVParser.printRowKeyT1(kv.getRow());
             System.out.print(kv.getTimestamp() + " " );
             NetFlowV5Record.decodeCFQValues(new CFQValue(kv.getFamily(), kv.getQualifier(), kv.getValue()));
         }
     }
     
     /**
      * Get a set of rows
      */
     public static void getRecords (Configuration conf, String tableName, byte[] rowKey) throws IOException{
         HTable table = new HTable(conf, tableName);
         Scan s = new Scan();
         System.out.println(bytesToBits(rowKey));
         s.setStartRow(rowKey);
         s.setMaxVersions();
 
 //        ByteBuffer buffer = ByteBuffer.allocate(9);
 //        buffer.put(rowKey);
 //        buffer.put((byte) -1);
 //        System.out.println(bytesToBits(buffer.array()));
 //        s.setStopRow(buffer.array());
 
         
         byte[] rowKeyStop = new byte[rowKey.length];
         System.arraycopy(rowKey, 0, rowKeyStop, 0, rowKey.length - 1);
         rowKeyStop[rowKey.length -1 ] = (byte) (rowKey[rowKey.length -1 ] + 1);
         System.out.println(bytesToBits(rowKeyStop));
         s.setStopRow(rowKeyStop);
         
 //        System.out.println(bytesToBits(rowKey));
 //        s.setStopRow(Bytes.add(nullb, rowKey));
 //        s.setFilter(new InclusiveStopFilter(Bytes.add(new byte[]{0}, rowKey)));
 //        s.setStopRow(Bytes.add(Bytes.toBytes((byte)0), rowKey));
 //        s.setStopRow(Bytes.add(rowKey, Bytes.toBytes((byte)0)));
 //        s.setStopRow(rowKey);
         
         
         int count = 0;
         ResultScanner ss = table.getScanner(s);
         boolean first = true;
         for(Result r:ss){
         	System.out.println("------------------------------------------------");
         	first = true;
 	        for(KeyValue kv : r.raw()){
 	        	if (first){
 	        		System.out.println(NetFlowCSVParser.decodeRowKeyT1(kv.getRow()));
 	        		System.out.println(bytesToBits(kv.getRow()));
 //		            System.out.print(kv.getTimestamp() + " " );
 //		            System.out.println(NetFlowV5Record.decodeCFQValues(new CFQValue(kv.getFamily(), kv.getQualifier(), kv.getValue())));
 //		            System.out.println("****************");
 		            first = false;
 	        	}
 	        }
 	        System.out.println("------------------------------------------------");
 	        count++;
         }
         System.out.println("Returned results: " + count);
         System.out.println("------------------------------------------------");
         ss.close();
     }
     /**
      * Scan (or list) a table
      */
     public static void getAllRecord (Configuration conf, String tableName) {
         try{
              HTable table = new HTable(conf, tableName);
              Scan s = new Scan();
              ResultScanner ss = table.getScanner(s);
              int count = 0;
              boolean first = true;
              for(Result r:ss){
             	 first = true;
             	 for(KeyValue kv : r.raw()){
                      if (first){
                     	 System.out.println(NetFlowCSVParser.decodeRowKeyT1(kv.getRow()));
                     	 System.out.print(bytesToBits(kv.getRow()));
                     	 System.out.println();
 //	                     System.out.println("KeyValue TS: " + new Date(kv.getTimestamp()));
                     	 first = false;
                      }
 //                     System.out.println(NetFlowV5Record.decodeCFQValues(new CFQValue(kv.getFamily(), kv.getQualifier(), kv.getValue())));
                  }
                  count++;
              }
              System.out.println("Returned results: " + count);
         } catch (IOException e){
             e.printStackTrace();
         }
     }
 	
     private static void hbaseClient(){
 		Configuration conf = HBaseConfiguration.create();
 		try {
 			InetAddress address = InetAddress.getByName("192.216.59.74");
 //			byte[] rowkey = Bytes.add(address.getAddress(), Bytes.toBytes(Integer.parseInt("119")));
 			ByteBuffer buffer = ByteBuffer.allocate(8);
 			buffer.put(address.getAddress());
 			buffer.put(Bytes.toBytes(Integer.parseInt("119")));
 			byte[] rowkey = buffer.array();
 //			byte[] rowkey = address.getAddress();
 //			byte[] rowkey = Bytes.head(address.getAddress(), 1);
 //			byte[] rowkey = Bytes.toBytes(1362308365166l);
 			getRecords(conf, "T1", rowkey);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 //			getAllRecord(conf, "netflowsample");
 	}
 	
 	public static void intOp(){
 		System.out.println(bytesToBits(new byte[]{-127}));
 		System.out.println((byte) -1);
 	}
 	
 	public static String bytesToBits(byte[] b){
 		String bits = "";
 		for (int j = 0; j < b.length; j++) {
 			for (int i = 7; i > -1; i--) {
 				if ((b[j] & (1 << i)) != 0) {
 					bits += "1";
 				}else {
 					bits += "0";
 				}
 //			System.out.println(bits + " " + i);
 			}
 			bits += " ";
 			
 		}
 		return bits;
 	}
 	public static void main(String[] args) {
 //		ArrayList<String> columnStrings = Lists.newArrayList(
 //		        Splitter.on(',').trimResults().split("ab ,b,c"));
 //		System.out.println(columnStrings);
 //		stringInt();
 //		stringIP();
 //		byteBuffer();
 //		lists();
 //		byteop();
 		hbaseClient();
 //		intOp();
 //		System.out.println(byteToBits((byte)10));
 	}
 }
