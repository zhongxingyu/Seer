
public class test {
 	public static void main(String[] a) {
 		String w="Realtek Gbe & Ethernet PCI-E NIC Drivers";
 		String l=" &-gkabcdeqwrtlthnuiovs";
 		for (int i=0;i<w.length();){
 			String ch=""+w.charAt(i++); 
 			System.out.print((l.indexOf((ch).toLowerCase())!=-1)?ch:"_ ");
 		}
 	}
 }
