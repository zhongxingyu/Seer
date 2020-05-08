 /**
  * A distance metric that calculates distance based on 
 * weighted union of a bit map interception
  *
 * @author Diameter Kongevold(dimitryk)
  * 
  */
 
 import java.util.ArrayList;
 
 /**
  * A distance metric that calculates distance based on 
  * weighed union of a bit map interception
  * @version 240911.01 
  * @author dimitryk
  */
 public class bitmapDistance implements DistanceMetric{
 	//private int[] purpMap;
 	//private int[] recipMap;
 	//private int[] retMap;
 	
 	bitmapDistance(){
 	//	purpMap = new int[12];
 	//	recipMap = new int[6];
 	//	retMap = new int[5];
 	}
 	
 	/**
 	 * internal method that makes a bit map of 6 ints
 	 * each place in an array corresponds to a spesfic value
 	 * of a Recipient field
 	 *  
 	 * @author dimitryk
 	 * @param list input arraylist of ReciPients for method...
 	 * @return Map as bitmap
 	 */
 	
 	private int[] MakeRecipMap(ArrayList<Recipient> list){
 		int[] Map = new int[6];
 		for(int i=0;i<list.size();i++){
 			switch (list.get(i)){
 			case OURS: //weights can come in here 2
 				Map[0]=1;
 				break;
 			case DELIVERY:
 				Map[1]=1;
 				break;
 			case SAME:
 				Map[2]=1;
 				break;
 			case OTHER_RECIPIENT:
 				Map[3]=1;
 				break;
 			case UNRELATED:
 				Map[4]=1;
 				break;
 			case PUBLIC:
 				Map[5]=1;
 				break;
 			default:break;
 			}
 		}
 		return Map;
 	}
 	
 	/**
 	 * internal method that makes a bit map of 12 ints
 	 * each place in an array corresponds to a spesfic value
 	 * of a Purpose Enum
 	 * @author dimitryk
 	 * @param list input arraylist of Purpose for method...
 	 * @return Map as bitmap
 	 */
 	
 	private int[] MakePurposeMap(ArrayList<Purpose> list){
 		int[] Map = new int[12];
 		for(int i=0;i<list.size();i++){
 			switch (list.get(i)){
 			case CURRENT:
 				Map[0]=1;//weights can come in here 2
 				break;
 			case ADMIN:
 				Map[1]=1;
 				break;
 			case DEVELOP:
 				Map[2]=1;
 				break;
 			case TAILORING:
 				Map[3]=1;
 				break;
 			case PSEUDO_ANALYSIS:
 				Map[4]=1;
 				break;
 			case PSEUDO_DECISION:
 				Map[5]=1;
 				break;
 			case INDIVIDUAL_ANALYSIS:
 				Map[6]=1;
 				break;
 			case INDIVIDUAL_DECISION:
 				Map[7]=1;
 				break;
 			case CONTACT:
 				Map[8]=1;
 				break;
 			case HISTORICAL:
 				Map[9]=1;
 				break;
 			case TELEMARKETING:
 				Map[10]=1;
 				break;
 			case OTHER_PURPOSE:
 				Map[11]=1;
 				break;
 			default:break;
 			}
 		}
 		return Map;
 	}
 	
 	/**
 	 * internal method that makes a bit map of 5 ints
 	 * each place in an array corresponds to a spesfic value
 	 * of a Retention Enum
 	 * @author dimitryk
 	 * @param list input arraylist of Purpose for method...
 	 * @return Map as bitmap
 	 */
 	
 	private int[] MakeRetentionMap(ArrayList<Retention> list){
 		int[] Map = new int[5];
 		for(int i=0;i<list.size();i++){
 			switch (list.get(i)){
 			case NO_RETENTION: //weights can come in here 2
 				Map[0]=1;
 				break;
 			case STATED_PURPOSE:
 				Map[1]=1;
 				break;
 			case LEGAL_REQUIREMENT:
 				Map[2]=1;
 				break;
 			case BUSINESS_PRACTICES:
 				Map[3]=1;
 				break;
 			case INDEFINITELY:
 				Map[4]=1;
 				break;
 			default:break;
 			}
 		}
 		return Map;
 	}
 	
 	/**
 	 * Calculates distance in the Recipien field from case a to case b
 	 * using bit map distance = a interseption b * weight
 	 * @author dimitryk
 	 * @param a input case.
 	 * @param b input case.
 	 * @return dis double for the distance between to.
 	 */
 	
 	public double getDistRecip(Case a, Case b) {
 		int[] MapA, MapB;
		MapA = MakeRecipMap(a.getRecipients());
		MapB = MakeRecipMap(b.getRecipients());
 		double dis=0;
 		
 		for(int i=0;i<6;i++){
 			if(MapA[i]!=MapB[i]){
 				//dis=dis+(Math.max(MapA[i], MapB[i])-Math.min(MapA[i], MapB[i]));
 				dis=dis+1; //for now... weights are jet not operational.
 			}
 		}
 		
 		
 		return dis;// dis*weight later
 	}
 
	/*
 	 * Calculates distance in the Retention field from case a to case b
 	 * using bit map distance = a interseption b * weight
 	 * @author dimitryk
 	 * @param a input case.
 	 * @param b input case.
 	 * @return dis double for the distance between to.
 	 */
	//TODO getRententions returns an arraylist- compare elements in union, please fix
	//TODO assign default weight; current logic would give a pair of cases A & B, with fields f= {(a,b)(b,c)} further spacing than if the f was ={(a,b)(c,d)} even though they have more in common (in the second case)
 	public double getDistReten(Case a, Case b) {
 		int[] MapA, MapB;
		MapA = MakeRetentionMap(a.getRetentions());
		MapB = MakeRetentionMap(b.getRetentions());
 		double dis=0;
 		
 		for(int i=0;i<5;i++){
 			if(MapA[i]!=MapB[i]){
 				dis=dis+1; //for now... weights are jet not operational.
 			}
 		}
 		
 		
 		return dis;// dis*weight later
 	}
 
 	/**
 	 * Calculates distance in the Purpose field from case a to case b
 	 * using bit map distance = a interseption b * weight
 	 * @author dimitryk
 	 * @param a input case.
 	 * @param b input case.
 	 * @return dis double for the distance between to.
 	 */
 	
 	public double getDistPurpose(Case a, Case b) {
 		int[] MapA, MapB;
		MapA = MakePurposeMap(a.getPurposes());
		MapB = MakePurposeMap(b.getPurposes());
 		double dis=0;
 		
 		for(int i=0;i<12;i++){
 			if(MapA[i]!=MapB[i]){
 				dis=dis+1; //for now... weights are jet not operational.
 			}
 		}
 		
 		
 		return dis;// dis*weight later
 	}
 
 	@Override
 	public double getTotalDistance(Case a, Case b) {
 		
 		return getDistPurpose(a,b)+getDistRecip(a,b)+getDistReten(a,b);
 	}
 
 }
