 /**
  * @(#)AuroraCSVLink.java
  */
 
 package aurora.hwc.report;
 
 import aurora.hwc.TypesHWC;
 
 
 /**
  * Network link configuration.
  * @author Gabriel Gomes 
  */
 public class AuroraCSVLink {
 	
 	public String id;
 	public String name;
 	public int type;
 	public float length;
 	public float lanes;
 	public boolean issource;
 	
 	public void setid(String x){
		id = x;
 	}
 
 	public void setname(String x){
		name = x;
 	}
 	
 	public void settype(String x){
 		
 		if(x.equalsIgnoreCase("Dummy Link"))
 			type = TypesHWC.LINK_DUMMY;
 		
 		if(x.equalsIgnoreCase("Freeway"))
 			type = TypesHWC.LINK_FREEWAY;
 
 		if(x.equalsIgnoreCase("HOV"))
 			type = TypesHWC.LINK_HOV;
 
 		if(x.equalsIgnoreCase("Highway"))
 			type = TypesHWC.LINK_HIGHWAY;
 
 		if(x.equalsIgnoreCase("On-Ramp"))
 			type = TypesHWC.LINK_ONRAMP;
 
 		if(x.equalsIgnoreCase("Off-Ramp"))
 			type = TypesHWC.LINK_OFFRAMP;
 
 		if(x.equalsIgnoreCase("Interconnect"))
 			type = TypesHWC.LINK_INTERCONNECT;
 
 		if(x.equalsIgnoreCase("Street"))
 			type = TypesHWC.LINK_STREET;
 
 		if(x.equalsIgnoreCase("HOT"))
 			type = TypesHWC.LINK_HOT;
 			
 		if(x.equalsIgnoreCase("HV"))
 			type = TypesHWC.LINK_HV;
 			
 		if(x.equalsIgnoreCase("ETC"))
 			type = TypesHWC.LINK_ETC;
 		
 	}
 	
 	public void setlength(String x){
 		length = Float.parseFloat(x.trim());
 	}
 	
 	public void setlanes(String x){
 		lanes = (int) Float.parseFloat(x.trim());
 	}
 	
 	public void setsource(String x){
 		issource = x.trim().toLowerCase().equals("yes");
 	}
 
 	public AuroraCSVLink clone() {
 		AuroraCSVLink z = new AuroraCSVLink();
 		z.id = this.id;
 		z.name = this.name;
 		z.type = this.type;
 		z.length = this.length;
 		z.lanes = this.lanes;
 		z.issource = this.issource;
 		return z;
 	}
 
 }
