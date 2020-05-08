 package kinect.filters.position;
 
 import kinect.filters.AveragingFilter;
 import kinect.geometry.Position;
 
 /**
  * Created by IntelliJ IDEA.
  * User: John
  * Date: 23/03/12
  * Time: 15:34
  * To change this template use File | Settings | File Templates.
  */
 public class AveragingPositionFilter implements PositionFilter {
 
     private AveragingFilter x_filter;
     private AveragingFilter y_filter;
     private AveragingFilter z_filter;
 
     public AveragingPositionFilter(int samples_to_keep){
         x_filter = new AveragingFilter(samples_to_keep);
         y_filter = new AveragingFilter(samples_to_keep);
         z_filter = new AveragingFilter(samples_to_keep);
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
