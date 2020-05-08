 package kinect.filters.position;
 
 import kinect.filters.DoubleExponentialFilter;
 import kinect.geometry.Position;
 
 /**
  * Created by IntelliJ IDEA.
  * User: John
  * Date: 23/03/12
  * Time: 15:34
  * To change this template use File | Settings | File Templates.
  */
 public class DoubleExponentialPositionFilter implements PositionFilter {
 
     private DoubleExponentialFilter x_filter;
     private DoubleExponentialFilter y_filter;
     private DoubleExponentialFilter z_filter;
 
     public DoubleExponentialPositionFilter(double alpha,double gamma){
         x_filter = new DoubleExponentialFilter(alpha,gamma);
         y_filter = new DoubleExponentialFilter(alpha,gamma);
         z_filter = new DoubleExponentialFilter(alpha,gamma);
     }
 
     public Position get(){
        return new Position(x_filter.get(),y_filter.get(),z_filter.get());
     }
 
     public void put(Position sample) {
         x_filter.put(sample.x);
         y_filter.put(sample.y);
         z_filter.put(sample.z);
     }
 
     public Position forecast(int k){
         return get();
     }
 
     public void reset(){
         x_filter.reset();
         y_filter.reset();
         z_filter.reset();
     }
 
 }
