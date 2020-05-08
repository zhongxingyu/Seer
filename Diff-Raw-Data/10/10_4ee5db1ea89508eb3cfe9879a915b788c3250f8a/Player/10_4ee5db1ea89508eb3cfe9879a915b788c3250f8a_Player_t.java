 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.regex.Pattern;
 
 import com.sun.tools.javac.util.Pair;
 
 
 
 public class Player {
   
   public static void main(String args[]){
     Player p = new Player("test");
     String fromS = "ADD|6,-6 9,-5 3,-4 2,-3 5,-2 10,-1 8,0|in=-73.0,out=-19.0";
     p.setStatus(fromS);
     p._usedWeights.add(1);p._usedWeights.add(2);p._usedWeights.add(4);
     p._usedWeights.add(5);p._usedWeights.add(7);p._usedWeights.add(8);
     p._usedWeights.add(10);
     
     System.out.println("Final result:  " + p.play());    
   }
   
   private final String _name;
   private int _mode; // if mode = 0, its REMOVE, if 1 its ADD
   //stores key = position, value = weight of occupied positions 
   private HashMap<Integer, Integer> _occupied = new HashMap<Integer, Integer>();
   private Double _rightTorque;
   private Double _leftTorque;
   public int _numOfWeights;
   private Collection<Integer> _usedWeights = new ArrayList<Integer>();
   public Player(String name){
     _name = name;
     _numOfWeights = 10;
   }
 
   public String play(){
     //ADD
     //if(_mode==1){
     List<ChoicePair> poss = buildChoices(_occupied);
     if(poss.isEmpty()) System.out.println(poss+"nomore..?");
     //only left with one weight so return which ever.but think..about..this...later for removing..?
     System.out.println("size of occupied is: " + _occupied.size());
     ChoicePair finalResult = null;
     Random gen = new Random();
     if(poss.isEmpty() || _occupied.size() == 29){
       //get random
       List<ChoicePair> choices = buildLosingChoices(_occupied);//this will tip..
       if(!choices.isEmpty()){
         int r = gen.nextInt(choices.size());
         finalResult = choices.get(r);
       }
       else finalResult = new ChoicePair(-1,-1); //invalid
     }
     else if(poss.size()==1) finalResult = poss.get(0);
     else{
       Pair<Double, ChoicePair> res = alphaBeta(_occupied, poss, 
           new Pair<Double, ChoicePair>(Double.MIN_VALUE, poss.get(0)), 
           new Pair<Double, ChoicePair>(Double.MAX_VALUE, poss.get(0)), 
           null, 0);
       finalResult = res.snd;
     }
     //}
     System.out.println("use weight!!! "+finalResult.weight+ " at pos:" + finalResult.position);
     _usedWeights.add(finalResult.weight);
     return finalResult.toString();
   }
   /**parses details sent from Server and sets Game Status
    * @fromServer looks like "ADD|3,-4|in=-9.0,out=3.0
    */
   public void setStatus(String fromServer){
     Pattern splitter = Pattern.compile("\\|");
     String[] words = splitter.split(fromServer);
     if(words[0].startsWith("ADD")) _mode = 1;
     else _mode = 0;
     //    System.out.println(words.length);
     //    for(int i=0, n=words.length; i<n;i++ ){
     //      System.out.println(words[i]);
     //    }
     String data = words[1];
     StringTokenizer parser = new StringTokenizer(data, " "); 
     //feed the occupied map
     while(parser.hasMoreTokens()){
       String[] token = parser.nextToken().split(",");
       _occupied.put(Integer.valueOf(token[1]), Integer.valueOf(token[0]));
     }
 
     //printStatus();
 
   }
   private void printStatus(){
     System.out.println("Current playing mode is " + _mode);
     System.out.println("Current position occupied are: " + _occupied.toString());
 //    System.out.println("Current torques are Right: "+
 //        _rightTorque + " left:"+_leftTorque);
     calculate_torque();
     System.out.println("Current calculated torques " +_rightTorque +
         " out="+_leftTorque );
   }
   /*
    * formula to calulate torque:
    * in1 and out1 calculates torque on lever at -1
    * in3 and out3 calculates torque on lever at -3
    * 
    * At -3, the tip occurs when torque on the left > torque on right or out3-in3 > 0
    * At -1, the tip occurs when torque on the right > torque on left or in1-out1 > 0
    * If either of the conditions hold true, the player loses the game
    */	
   private void calculate_torque() {		
     double in1=0,out1=0,in3=0,out3=0;
     //the board weights 3 kgs which is concentrated at 0, so there is some intorque at -3 and -1
     in3 += 9;
     in1 += 3;
 
     Set<Integer> s = _occupied.keySet();
     for (Integer i: s){
       int pos = i;
       int wt;
       //System.out.println("position " + pos + " is occupied with wt" + _occupied.get(pos));
       wt = _occupied.get(pos); 
 
 
       if (pos < -3)
         out3 += (-1) * (pos-(-3)) * wt;
       else
         in3 += (pos-(-3))* wt;
 
 
       if (pos < -1)
         out1 += (-1) * (pos-(-1)) * wt;
       else
         in1 += (pos-(-1))* wt;			
     }
 
     //    System.out.println("in1=" + in1);
     //    System.out.println("out1=" + out1);
     //    System.out.println("in3=" + in3);
     //    System.out.println("out3=" + out3);
     _rightTorque= in1 - out1;
     _leftTorque = out3 - in3;
   }
   
   private List<ChoicePair> buildLosingChoices(HashMap<Integer, Integer> occupied){
 
     System.out.println("Losing..usedWeights:"+_usedWeights);
     
     Set<Integer> takenPositions = occupied.keySet();
     ArrayList<ChoicePair> choices = new ArrayList<ChoicePair>();
     for(int i=-15; i<16;i++){
       if(!takenPositions.contains(i)){
         for(int j=1; j<=_numOfWeights;j++){
           if(!_usedWeights.contains(j)){
             choices.add(new ChoicePair(i, j));
            //System.out.println("Honmani akan no?" + new ChoicePair(i,j).willTipWith(null));
           }
         }
       }
     }
     //System.out.println("choices:"+choices);
     return choices;
   }
 
   private List<ChoicePair>buildChoices(HashMap<Integer, Integer> occupied){
     Set<Integer> takenPositions = occupied.keySet();   
     Collection<Integer> usedW = occupied.values();
     ArrayList<ChoicePair> possibilities = new ArrayList<ChoicePair>();
     //System.out.println("taken Positions:" + takenPositions);
     for(int i=-15; i<16;i++){
       if(!takenPositions.contains(i)){
         for(int j=1; j<=_numOfWeights;j++){
           if(!_usedWeights.contains(j) && !usedW.contains(j)){
             ChoicePair choice = new ChoicePair(i,j);
             if(!choice.willTipWith(null)) possibilities.add(new ChoicePair(i, j));            
           }
         }
       }
     }
     return possibilities;
   }
   /**
    * Calculate heuristic scores for each occupied
    * Kill huerisitcs to stop
    * Scores are based on as far as you can go within certain time limits
    * The number of options you leave to the opponent
    * The number of options that leaves you in the next level
    * The heavier the weight, the worse,,
    * the smaller the choice you produce... better???
    * best if you eliminate the search space faster...
    * @param position
    * @param weight
    * @param choices of positions and weights
    * @return
    */
   private double getScore(HashMap<Integer, Integer> curr, List<ChoicePair> choices){
     //if(node.willTip()) return 0.0;
     //else
     return 3.0;
   }
   private Pair<Double, ChoicePair> alphaBeta(HashMap<Integer, Integer> curr, List<ChoicePair> possibilities, Pair<Double, ChoicePair> alpha, 
       Pair<Double, ChoicePair> beta, ChoicePair node, double depth){
 //    if(depth==4){
 //      double score = 1/(depth*node.weight);
 //      return new Pair<Double, ChoicePair>(score, node);
 //    }
     //this is good..
     if(possibilities.size()==0) return new Pair<Double, ChoicePair>(1.0, node);
     if(possibilities.size()==1){
       //invert the depth = depth smaller the better -> small should give high score
       //plus heavier the better
       //the smaller the resulting possibilities,, better
       double score = 1/(depth*node.weight);
       return new Pair<Double, ChoicePair>(score, node);
     }
     for(ChoicePair choice: possibilities){
       HashMap<Integer, Integer> newOccupied = new HashMap<Integer, Integer>(curr);
       newOccupied.put(choice.position, choice.weight);
       //System.out.println("for choice: " +choice+" new Occupied: " +newOccupied);
       possibilities = buildChoices(newOccupied);
       //System.out.println("for choice:" + choice+" size of possibilities in search:"+possibilities.size());
       Pair<Double, ChoicePair> newAlpha = new Pair<Double, ChoicePair>(alpha.fst*-1, alpha.snd);//negate
       Pair<Double, ChoicePair> newBeta = new Pair<Double, ChoicePair>(beta.fst*-1, beta.snd);//negate
       Pair<Double, ChoicePair>result = alphaBeta(newOccupied, possibilities, newBeta, newAlpha, choice, depth++);
       double score = -1*result.fst; 
       //System.out.println("for choice:" + choice+" size of poss:"+possibilities.size()+" score:" + score);
       //max(alpha, -alphabeta(...))
       if(score>alpha.fst){
         alpha = new Pair<Double, ChoicePair>(score, choice); //keep the winner
       }
       if (beta.fst<= alpha.fst) break; //don't give a fuck!
       }
     return alpha;
   }
 
 
   class ChoicePair{
     private int position;
     private int weight;
     private HashMap<Integer, Integer> myOccupied;
     ChoicePair(int position, int weight){
       this.position = position;
       this.weight = weight;
       this.myOccupied = new HashMap<Integer, Integer>(_occupied);
       myOccupied.put(position, weight);
     }
     boolean willTipWith(HashMap<Integer, Integer> newOccupied){
       if(newOccupied==null) newOccupied = myOccupied;
       double rightT,leftT = 0.0; 
       double in1=0,out1=0,in3=0,out3=0;
       Set<Integer> s = newOccupied.keySet();
       for (Integer i: s){
         int pos = i;
         int wt;
         wt = newOccupied.get(pos); 
         if (pos < -3)
           out3 += (-1) * (pos-(-3)) * wt;
         else
           in3 += (pos-(-3))* wt;
         if (pos < -1)
           out1 += (-1) * (pos-(-1)) * wt;
         else
           in1 += (pos-(-1))* wt;
       }
       rightT= in1 - out1;
       leftT = out3 - in3;
       if(rightT>0 || leftT>0) return true;
       return false;
 
     }
 
     @Override
     public String toString() {
       return weight+"," + position;
     }
 
 
   }
 }
