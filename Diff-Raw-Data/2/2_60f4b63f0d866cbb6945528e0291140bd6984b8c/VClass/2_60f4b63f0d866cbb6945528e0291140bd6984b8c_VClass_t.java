 import java.util.List;
 
 public class VClass extends CuType {
 	String data_s;
 	List<CuType> pt;
 	public VClass(String s, List<CuType> pt){
 		data_s=s;
 		this.pt = pt;
		super.text=data_s+ " "+ listTypes(pt);
 	}
 	private String listTypes(List<CuType> es) {
 		String s = "< ";
 		for (CuType e : es) {
 			s += e.toString() + " , ";
 		}
 		int l = s.lastIndexOf(", ");
 		if (l > 0) s = s.substring(0, l);
 		s += ">";
 		return s;
 	}
 }
