 package no.uis.ux.cipsi;
 
 import java.lang.reflect.Array;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import no.uis.ux.cipsi.net.MacAddress;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.HBaseConfiguration;
 import org.apache.hadoop.hbase.thrift.generated.Hbase.getColumnDescriptors_args;
 import org.apache.hadoop.hbase.util.Bytes;
 
 public class Utils { 
 
	private static final ArrayList<String> qualifiersMapping = new ArrayList<>(Arrays.asList(
 			"Date flow start",
 			"Date flow end", 
 			"Duration",
 			"Src IP Addr",
 			"Dst IP Addr",
 			"Src Pt",
 			"Dst Pt",
 			"Proto",
 			"Flags",
 			"Fwd",
 			"STos",
 			"In Pkt",
 			"In Byte",
 			"Out Pkt",
 			"Out Byte",
 			"Flows",
 			"Input",
 			"Output",
 			"Src AS",
 			"Dst AS",
 			"SMask",
 			"DMask",
 			"DTos",
 			"Dir",
 			"Next-hop IP",
 			"BGP next-hop IP",
 			"SVlan", 
 			"DVlan",
 			"In src MAC Addr",
 			"Out dst MAC Addr",
 			"In dst MAC Addr",
 			"Out src MAC Addr",
 			"Router IP",
 			"MPLS lbl 1",
 			"MPLS lbl 2",
 			"MPLS lbl 3",
 			"MPLS lbl 4"));
 	/**
 	 * The right implementation can be  persistent hash function which return 8bit. 
 	 * We should emphasize that the column qualifier must be kept as small as possible.
 	 * @param fieldName
 	 * @return
 	 * @throws Exception 
 	 */
 	public static byte[] getColumnQualifier(String fieldName) {
 //		Configuration conf = HBaseConfiguration.create();
 //        String table = "netflowcq";
 //		String[] tmp = new String[]{"a", "b"};
 		byte qc = -1;
 		if (qualifiersMapping.indexOf(fieldName) == -1){
 			try {
 				throw new Exception("ColumnQualifier Not Available: " + fieldName);
 			} catch (Exception e) {
 				e.printStackTrace();
 				System.err.println("The new qualifier should be added...");
 			}
 		}else{
 			qc = (byte) qualifiersMapping.indexOf(fieldName);
 		}
 		// BAD BAD BAD
 		return new byte[]{qc};
 	}
 	
 	public static String getColumnQualifierString(byte[] columnQualifierID){
 		int index = columnQualifierID[0];
 		return qualifiersMapping.get(index);
 	}
 
 	public static byte[] prepareMAC(String macString){
 		String[] mac = macString.split(":");
 		
 		// convert hex string to byte values
 		byte[] macBytes = new byte[6];
 		for(int j=0; j<6; j++){
 //			BigInteger hex = new BigInteger(mac[j], 16);
 //			System.out.println(hex);
 //			System.out.println(hex.toByteArray()[1]);
 //			byte tmp = hex.toByteArray()[1];
 			Byte b = Byte.decode("0xaa");
 			
 			System.out.println(b);
 //			macBytes[j] = hex.byteValue();
 		}
 		return macBytes;
 	}
 	
 	public static void main(String[] args) throws Exception {
 //		System.out.println(Bytes.toInt(getColumnQualifier("SVlan")));
 //		System.out.println(getColumnQualifier("SVlan")[0]);
 //		System.out.println(getColumnQualifierString(getColumnQualifier("SVlan")));
 //		System.out.println(prepareMAC("AA:BB:CC:DD:EE:FF"));
 //		byte[] mac = prepareMAC("AA:BB:CC:DD:EE:FF");
 		MacAddress address = new MacAddress("AA:BB:CC:DD:EE:FF");
 		for (int i = 0; i < address.getBytes().length; i++) {
 			System.out.println(address.getBytes()[i]);
 		}
 		MacAddress address2 = new MacAddress(address.getBytes());
 		System.out.println(address2.toString());
 		
 	}
 	
 	
 	
 }
