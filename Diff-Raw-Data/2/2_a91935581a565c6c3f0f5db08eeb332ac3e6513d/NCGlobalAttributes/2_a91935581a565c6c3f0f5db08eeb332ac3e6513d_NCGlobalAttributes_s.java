 /**
  * @author Micho Garcia
  */
 package co.geomati.netcdf;
 
 import java.util.ArrayList;
 
 import ucar.nc2.Attribute;
 
 /**
  * @author Micho Garcia
  *
  */
 public class NCGlobalAttributes implements Iterable<Attribute>{
 	
	private ArrayList<Attribute> globalAttributes;
 
 	public void addAtribute(String name, String value) {
 		Attribute atributte = new Attribute(name, value);
 		globalAttributes.add(atributte);
 	}
 	
 	@Override
 	public java.util.Iterator<Attribute> iterator() {
 		return globalAttributes.iterator();
 	}
 
 }
