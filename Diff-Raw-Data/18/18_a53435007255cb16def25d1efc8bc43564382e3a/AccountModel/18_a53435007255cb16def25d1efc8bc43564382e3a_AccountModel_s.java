 package account.model;
 
 import account.model.AbstractModel;
 import account.model.ModelEvent;
 
 public class AccountModel extends AbstractModel {
 
 	private int key;
 	private String displayName;
	private double funds;
 	
 	public AccountModel(int id, double d, String name){
 		key = id;
 		displayName = name;
 		funds = d;
 		}
 	public int getKey(){ return key;}
 	public String getName(){ return displayName;}
 	public double getFunds(){ return funds;}
 	public void setFunds(double newVal){
 		funds = newVal;
 		ModelEvent me = new ModelEvent(this, key, "", funds);
 		notifyChanged(me);
 	}
 	
 	
 	
 }
