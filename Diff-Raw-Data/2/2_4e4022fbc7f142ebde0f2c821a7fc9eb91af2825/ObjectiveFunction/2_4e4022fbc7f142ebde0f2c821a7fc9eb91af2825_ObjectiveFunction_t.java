 public class ObjectiveFunction {
 
   protected double[] xc;
   protected double[] xa;
   protected double[] lower;
   protected double[] upper;
  protected Function f;
 
   private int[] acceptCount;
 
   public double eval(){
     return f.eval( xc );
   }
   
   public void step( final int dir, final double step ){
     xc[dir] += step;
   }
 
   public boolean inBounds( final int dir, 
                            final double step ){
     return ((xc[dir] + step) <= upper[dir]) && ((xc[dir] + step) >= lower[dir]);
       
   }
   public double[] getCurrent(){
     return xc;
   }
   public double[] getAccepted(){
     return xa;
   }
 
   public int dimension(){
     return xa.length;
   }
 
   public void accept( final int dir ){
     xa = xc.clone();
     acceptCount[ dir ]++;
   }
 
   public void reject() {
     xc = xa.clone();
   }
 
   public int getAcceptCount( final int dir ){
     return acceptCount[ dir ];
   }
 
   public void reset() {
     java.util.Arrays.fill(acceptCount, 0);
   }
 
   public void set( double[] x0 ){
     this.xa = x0.clone();
     this.xc = x0.clone();
   }
 
   public ObjectiveFunction( final double[] x0, 
                             final double[] lower, 
                             final double[] upper, 
                             final Function f ){
     this.xa = x0.clone();
     this.xc = x0.clone();
     this.lower = lower;
     this.upper = upper;
     this.f = f;
     acceptCount = new int[ this.dimension() ];
   }
 
 }
 
 
     
 
