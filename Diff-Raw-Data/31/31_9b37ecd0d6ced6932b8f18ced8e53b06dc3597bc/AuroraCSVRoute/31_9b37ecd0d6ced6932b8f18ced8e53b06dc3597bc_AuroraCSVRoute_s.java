 /**
  * @(#)AuroraCSVRoute.java
  */
 
 package aurora.hwc.report;
 
 import java.util.Vector;
 
 
 /**
  * Network routes
  * @author Gabriel Gomes
  */
 public class AuroraCSVRoute {
 	
 	public String name;
 	public Vector<String> linkids = new Vector<String>();
 	
 	public void setname(String n){name = n;};
 	
 	public void setlinks(Vector<String> L){
 		linkids.clear();			
 		for(int i=0;i<L.size();i++)
			linkids.add(L.get(i));
 	}
 	
 	public AuroraCSVRoute clone() {
 		AuroraCSVRoute z = new AuroraCSVRoute();
 		z.name = this.name;
 		for(int i=0;i<this.linkids.size();i++)
 			z.linkids.add(this.linkids.get(i));
 		return z;
 	}
 
 		
 }
