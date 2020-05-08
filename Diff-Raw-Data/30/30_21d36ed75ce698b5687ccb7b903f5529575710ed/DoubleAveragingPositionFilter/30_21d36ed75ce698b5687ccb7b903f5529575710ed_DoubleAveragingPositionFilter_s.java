 package kinect.filters.position;
 
 import kinect.filters.DoubleAveragingFilter;
 import kinect.geometry.Position;
 
 /**
  * Created by IntelliJ IDEA.
  * User: John
  * Date: 23/03/12
  * Time: 15:34
  * To change this template use File | Settings | File Templates.
  */
 public class DoubleAveragingPositionFilter implements PositionFilter {
 
     private DoubleAveragingFilter x_filter;
     private DoubleAveragingFilter y_filter;
     private DoubleAveragingFilter z_filter;
 
     public DoubleAveragingPositionFilter(int samples_to_keep){
         x_filter = new DoubleAveragingFilter(samples_to_keep);
         y_filter = new DoubleAveragingFilter(samples_to_keep);
         z_filter = new DoubleAveragingFilter(samples_to_keep);
     }
 
     public Position get(){
        return new Position((int) x_filter.get(),(int) y_filter.get(), (int)z_filter.get());
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
