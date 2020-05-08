 
 package TrolleyRegistration;
 
 
 public class Trolley {
     
    private int trolleyId; 
     private int ownWeight; 
     private int totalWeight; 
     
    public Trolley(int trolleyId, int egenVekt){
        this.trolleyId = trolleyId; 
         this.ownWeight = egenVekt;
         this.totalWeight = ownWeight; 
         
     }
     
    public int getTrolleyId(){
        return trolleyId; 
     }
     
     
     public int getOwnWeight(){
         return ownWeight; 
     }
     
     
     public int getTotalWeight(){
         return totalWeight; 
     }
     
     public void setTotalWeight(int readWeight){
          totalWeight = readWeight; 
     }
     
     public int getPayLoad(){
         return totalWeight - ownWeight; 
     }
    
     
     @Override
     public String toString(){
        return "Vogn " + trolleyId + " Egenvekt: " + ownWeight + " Lastvekt: " + getPayLoad() + " Totalvekt: " + getTotalWeight(); 
     }
 
 }
