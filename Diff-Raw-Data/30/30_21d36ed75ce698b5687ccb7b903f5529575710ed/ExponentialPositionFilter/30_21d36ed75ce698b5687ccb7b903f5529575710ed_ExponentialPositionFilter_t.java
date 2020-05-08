 package kinect.filters.position;
 
 import kinect.filters.ExponentialFilter;
 import kinect.geometry.Position;
 
 /**
  * Created by IntelliJ IDEA.
  * User: John
  * Date: 23/03/12
  * Time: 15:34
  * To change this template use File | Settings | File Templates.
  */
 public class ExponentialPositionFilter implements PositionFilter {
 
     private ExponentialFilter x_filter;
     private ExponentialFilter y_filter;
     private ExponentialFilter z_filter;
 
     public ExponentialPositionFilter(double alpha){
         x_filter = new ExponentialFilter(alpha);
         y_filter = new ExponentialFilter(alpha);
         z_filter = new ExponentialFilter(alpha);
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
