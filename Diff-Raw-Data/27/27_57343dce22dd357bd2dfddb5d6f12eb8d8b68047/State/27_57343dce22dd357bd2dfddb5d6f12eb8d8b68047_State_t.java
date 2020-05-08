 package state;
 
 import agent.Agent;
 import agent.Predator;
 import agent.Prey;
 import util.Coordinates;
 
 import java.util.ArrayList;
 
 public abstract class State implements Cloneable {
     protected Coordinates preyCoordinates;
     protected ArrayList<Coordinates> predatorsCoordinates;
     protected double stateValue;	// Corresponds to value V (for the policy evaluation algorithm).
 
     public double getStateValue() {	return this.stateValue; }
 
     public double getPreyReward() {
         if (preyIsCaught())     { return -10.0; }
         if (predatorsCollide()) { return 10.0; }
         return 0.0;
     }
 
     public double getPredatorReward() {
         return -this.getPreyReward();
     }
 
     public double getReward(Agent agent) {
        if (agent instanceof Prey) {
            return this.getPreyReward();
        } if (agent instanceof Predator) {
            return this.getPredatorReward();
        } return Double.NEGATIVE_INFINITY;
     }
 
     /**
      * @return true if predator coordinates match prey coordinates, false otherwise.
      */
     private boolean preyIsCaught() { // TODO: handle predC collide on prey coordinates.
         for (Coordinates predator : this.predatorsCoordinates) {
             if (this.preyCoordinates.equals(predator)) { return true; }
         } return false;
     }
 
     /**
      * @return true if two predC share coordinates, false otherwise.
      */
     private boolean predatorsCollide() {
         switch (this.predatorsCoordinates.size()) {
             case 1:
                 return false;
             case 2:
                 return this.predatorsCoordinates.get(0).equals(this.predatorsCoordinates.get(1));
             case 3:
                 return (this.predatorsCoordinates.get(0).equals(this.predatorsCoordinates.get(1)) ||
                         this.predatorsCoordinates.get(1).equals(this.predatorsCoordinates.get(2)) ||
                         this.predatorsCoordinates.get(0).equals(this.predatorsCoordinates.get(2)));
             default:
                 Exception ex = new Exception();
                 ex.printStackTrace();
                 System.exit(-1);
         } return false;
     }
 
     public void setStateValue(double stateValue) { this.stateValue = stateValue; }
 
 	@Override
 	public String toString() {
 		String ret = "";
 		ret += "Value = " + this.stateValue + " Prey : " + this.preyCoordinates;
         for (Coordinates predator : this.predatorsCoordinates) { ret +=  " Predator : " + predator; }
 		return ret;
 	}
 
     @Override
 	public boolean equals(Object other) {
 		if (other == null) { return false; }
 	    if (other == this) { return true; }
 	    if (!(other instanceof State)) { return false; }
 
 	    State otherState = (State) other;
 
         int thisP = this.predatorsCoordinates.size();
         int otherP = otherState.predatorsCoordinates.size();
         if (thisP != otherP) { return false; }
         for (int i = 0 ; i < thisP; i ++ ){ // FIXME prominent bug depending on State implementation
             if (this.predatorsCoordinates.get(i) != otherState.predatorsCoordinates.get(i)) { return false; }
         }
         return this.preyCoordinates == otherState.preyCoordinates;
     }
 
     @Override
     public int hashCode() {
     	String hashString = "1" + this.preyCoordinates.getX() + this.preyCoordinates.getY();
         for (Coordinates predator : this.predatorsCoordinates) {
             hashString += predator.getX() +""+ predator.getY();
         }
         return Integer.parseInt(hashString);
     }
 
     public Coordinates getPreyCoordinates() { return this.preyCoordinates; }
     public ArrayList<Coordinates> getPredatorCoordinates() { return this.predatorsCoordinates; }
 
     public boolean isTerminal() { return preyIsCaught() || predatorsCollide(); }
 
 }
