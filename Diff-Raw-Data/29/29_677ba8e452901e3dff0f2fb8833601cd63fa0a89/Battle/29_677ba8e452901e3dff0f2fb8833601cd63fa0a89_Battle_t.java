 package warborn.model;
 
 import java.util.Arrays;
 
 public class Battle extends Template {
 	
 	private Warborn model;
 	private int[] a = new int[3], d = new int[2];
	private int unitsAttacker, unitsDefender;
 	
 	public Battle(Warborn model){
 		this.model = model;
 	}
 	public void fight(){
		for (int i: d){
			a[i]=0;
			d[i]=0;
		}
		a[2]=0;
 		unitsAttacker = getFirstTerritory().getNbrOfUnits() - 1;
 		unitsDefender = getSecondTerritory().getNbrOfUnits();
 		
 		for(int i = 0; i < unitsAttacker && i < 3; i++){
 			a[i] = Dice.d6Roll();
 		}
 		Arrays.sort(a);
 		int i = 1;
 		do{
 			d[i-1] = Dice.d6Roll();
			i++;
 		}while(i < unitsDefender && i < 3);
 		
 		if(a[2] <= Math.max(d[0], d[1])){
 			getFirstTerritory().setNbrOfUnits(unitsAttacker);
 		}
 		else{
 			getSecondTerritory().setNbrOfUnits(unitsDefender - 1);
 		}
		if(a[1] != 0 && a[1] <= Math.min(d[0], d[1]) ){
 			getFirstTerritory().setNbrOfUnits(getFirstTerritory().getNbrOfUnits() - 1);
 		}
		else if(Math.min(d[0], d[1]) != 0 && Math.min(d[0], d[1]) < a[1]){
 			getSecondTerritory().setNbrOfUnits(getSecondTerritory().getNbrOfUnits() -1);
 		}
 		model.changed();
 	}
 }
