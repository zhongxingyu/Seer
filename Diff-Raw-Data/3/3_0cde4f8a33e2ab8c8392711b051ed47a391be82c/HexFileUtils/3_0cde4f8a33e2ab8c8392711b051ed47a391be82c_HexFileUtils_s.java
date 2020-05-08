 /**
  * 
  */
 package hu.modembed.utils;
 
 import hexfile.AddressType;
 import hexfile.Entry;
 import hexfile.HexFile;
 import hexfile.HexfileFactory;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.emf.ecore.util.EcoreUtil;
 
 /**
  * @author balazs.grill
  *
  */
 public class HexFileUtils {
 
 	public static HexFile extract(HexFile source, long from, long to){
 		HexFile output = HexfileFactory.eINSTANCE.createHexFile();
 		output.setAddressType(AddressType.EXTENDED_LINEAR);
 		
 		for(Entry e : source.getEntries()){
			if (e.getAddress() >= from && e.getAddress()+e.getData().length < to){
 				output.getEntries().add(EcoreUtil.copy(e));
 			}
 		}
 		
 		return output;
 	}
 	
 	public static HexFile merge(HexFile... hexFiles){
 		List<Entry> entries = new LinkedList<Entry>();
 
 		for(HexFile hf : hexFiles){
 			hf = EcoreUtil.copy(hf);
 			entries.addAll(hf.getEntries());
 		}
 
 		HexFile output = HexfileFactory.eINSTANCE.createHexFile();
 
 		Collections.sort(entries, new Comparator<Entry>() {
 
 			@Override
 			public int compare(Entry o1, Entry o2) {
 				Integer a1 = o1.getAddress();
 				Integer a2 = o2.getAddress();
 				return a1.compareTo(a2);
 			}
 		});
 
 		output.setAddressType(AddressType.EXTENDED_LINEAR);
 		output.getEntries().addAll(entries);
 		return output;
 	}
 	
 }
