 public class BaseCalculator {
    private String sequence;
    private double[] props;
    private int nucleoCount;
 
    public BaseCalculator(String s) {
       this.sequence = s;
       this.props = baseProportions(s);
       this.nucleoCount = s.length();
      if(s.charAt(s.length()-1) == '$')
         this.nucleoCount--;
    }
 
    public double expectedOccurences(String s) {
       double prob = 1.0;
 
       for(int i = 0; i < s.length(); i++) {
          switch(s.charAt(i)) {
             case 'A': prob *= props[0];
                       break;
             case 'T': prob *= props[1];
                       break;
             case 'G': prob *= props[2]; 
                       break;
             case 'C': prob *= props[3];
                       break;
             default:  break;
          }
       }
 
       return prob * (double)nucleoCount;
    }
 
    public static double[] baseProportions(String s) {
       int aCount = 0;
       int tCount = 0;
       int gCount = 0;
       int cCount = 0;
       int count = 0;
 
       for(int i = 0; i < s.length(); i++) {
          switch(s.charAt(i)) {
             case 'A': aCount++;
                      count++;
                       break;
             case 'T': tCount++;
                      count++;
                       break;
             case 'G': gCount++; 
                      count++;
                       break;
             case 'C': cCount++;
                      count++;
                       break;
             default:  break;
          }
       }
       
       double[] vals = new double[4];
 
       vals[0] = (double)aCount / (double)count;
       vals[1] = (double)tCount / (double)count;
       vals[2] = (double)gCount / (double)count;
       vals[3] = (double)cCount / (double)count;
 
       return vals;
    }
 
 }
