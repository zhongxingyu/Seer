 
 package jvg.dexma;
 
 
 public class Reference_devices{
    	private Number fridge;
    	private Number hvac;
    	private Number lighting;
    	private Number main;
    	private Number otemp;
 
  	public Reference_devices(Number fridge, Number hvac, Number lighting,
         Number main, Number otemp) {
       super();
       this.fridge = fridge;
       this.hvac = hvac;
       this.lighting = lighting;
       this.main = main;
       this.otemp = otemp;
     }
   public Number getFridge(){
 		return this.fridge;
 	}
 	public void setFridge(Number fridge){
 		this.fridge = fridge;
 	}
  	public Number getHvac(){
 		return this.hvac;
 	}
 	public void setHvac(Number hvac){
 		this.hvac = hvac;
 	}
  	public Number getLighting(){
 		return this.lighting;
 	}
 	public void setLighting(Number lighting){
 		this.lighting = lighting;
 	}
  	public Number getMain(){
 		return this.main;
 	}
 	public void setMain(Number main){
 		this.main = main;
 	}
  	public Number getOtemp(){
 		return this.otemp;
 	}
 	public void setOtemp(Number otemp){
 		this.otemp = otemp;
 	}
  
   @Override
   public boolean equals(Object obj) {
     if (this == obj)
       return true;
     if (obj == null)
       return false;
     if (getClass() != obj.getClass())
       return false;
     Reference_devices other = (Reference_devices) obj;
     if (fridge == null) {
       if (other.fridge != null)
         return false;
     } else if (!fridge.equals(other.fridge))
       return false;
     if (hvac == null) {
       if (other.hvac != null)
         return false;
     } else if (!hvac.equals(other.hvac))
       return false;
     if (lighting == null) {
       if (other.lighting != null)
         return false;
     } else if (!lighting.equals(other.lighting))
       return false;
     if (main == null) {
       if (other.main != null)
         return false;
     } else if (!main.equals(other.main))
       return false;
     if (otemp == null) {
       if (other.otemp != null)
         return false;
     } else if (!otemp.equals(other.otemp))
       return false;
     return true;
   }
 }
