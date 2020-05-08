 /*
  * @author tgriswol
  */
 public class NpcDna implements Dna {
	public static final int nucleotideSize = 1;
 	private boolean[] nucleotides;
 	private int eatingDuration, sleepDuration, gender;
 	
 	public NpcDna(){
 		initNucleotides();
 		initEatingDuration();
 		initSleepDuration();
 		initGender();
 	}
 	
 	public void initNucleotides(){
 		nucleotides = new boolean[nucleotideSize];
 		for(int idx = 0; idx < nucleotideSize; idx++){
 			if(Util.random.nextInt(2) == 0) //(Random number between 0 and 1)
 				nucleotides[idx] = true; 
 			else
 				nucleotides[idx] = false; 
 		}
 	}
 	private void initEatingDuration(){
 		Debug.echo("Initializing the EatingDuration based off nucleotides.");
 		eatingDuration = 1;
 	}
 	private void initSleepDuration(){
 		Debug.echo("Initializing the SleepDuration based off nucleotides.");
 		sleepDuration = 1;
 	}
 	private void initGender(){
 		Debug.echo("Initializing the Gender based off nucleotides.");
		if(nucleotides[0] == true)
			gender = Const.MALE;
 		else
 			gender = Const.FEMALE;
 	}
 	
 	public int getEatingDuration(){
 		return eatingDuration;
 	}
 	public int getSleepDuration(){
 		return sleepDuration;
 	}
 	public int getGender(){
 		return gender;
 	}
     public boolean[] getNucleotides(){
 		boolean[] nucleotidesCopy = new boolean[nucleotides.length];
 		System.arraycopy(nucleotides, 0, nucleotidesCopy, 0, nucleotides.length);
 		return nucleotidesCopy;
 	}
 }
