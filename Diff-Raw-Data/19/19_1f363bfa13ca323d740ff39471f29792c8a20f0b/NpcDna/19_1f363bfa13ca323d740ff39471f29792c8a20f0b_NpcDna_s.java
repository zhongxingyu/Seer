 import java.awt.Color;
 
 /*
  * @author tgriswol
  */
 public class NpcDna implements Dna {
     private static final Color[] colors = {
         Color.RED,
         Color.YELLOW,
         Color.GREEN,
         Color.BLUE,
         Color.MAGENTA,
         new Color(0x6600cc),
         new Color(0x00ffff),
         new Color(0xcc6600)
     };
     private boolean[] nucleotides;
     private int eatingDuration, sleepDuration, colorIdx, gender;
 
     public NpcDna(){
         initNucleotides();
         initGender();
         initEatingDuration();
         initSleepDuration();
         initColorIdx();
     }
 
     public NpcDna(boolean[] n) {
         nucleotides = n;
         initGender();
         initEatingDuration();
         initSleepDuration();
         initColorIdx();
     }
 
     public void initNucleotides(){
         nucleotides = new boolean[Settings.NUCLEOTIDES_SIZE];
         for(int idx = 0; idx < Settings.NUCLEOTIDES_SIZE; idx++){
             if(Util.random.nextInt(2) == 1) //(Random number between 0 and 1)
                 nucleotides[idx] = true; 
             else
                 nucleotides[idx] = false; 
         }
     }
     private void initGender(){
         gender = toDecimal(nucleotides, Settings.GENDER_START_IDX, Settings.EATING_DURATION_START_IDX);
         Debug.echo("Initializing the Gender based off nucleotides to "+gender+".");
     }
     private void initEatingDuration(){
        eatingDuration = toDecimal(nucleotides, Settings.EATING_DURATION_START_IDX, Settings.SLEEPING_DURATION_START_IDX);
         Debug.echo("Initializing the EatingDuration based off nucleotides to "+eatingDuration+".");
     }
     private void initSleepDuration(){
        sleepDuration = toDecimal(nucleotides, Settings.SLEEPING_DURATION_START_IDX, Settings.COLORIDX_START_IDX);
         Debug.echo("Initializing the SleepDuration based off nucleotides to "+sleepDuration+".");
     }
     private void initColorIdx(){
         colorIdx = toDecimal(nucleotides, Settings.COLORIDX_START_IDX, Settings.NUCLEOTIDES_SIZE);
         Debug.echo("Initializing the ColorIdx based off nucleotides to "+colorIdx+".");
     }
 
     //Converts the given boolean array starting at startIdx (inclusive) to endIndex (exclusive) to decimal
     private static int toDecimal(boolean[] array, int startIdx, int endIdx){
         String binaryString = "";
         for(int idx = startIdx; idx < endIdx; idx++){
             if(array[idx] == true)
                 binaryString = binaryString + "1";
             else
                 binaryString = binaryString + "0";
         }
         return Integer.parseInt(binaryString, 2);
     }
 
     public int getEatingDuration(){
         return eatingDuration;
     }
     public int getSleepingDuration(){
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
     public Color getColor(){
         return colors[colorIdx%colors.length];
     }
 }
