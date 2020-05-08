 
 package tajmi.som;
 
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Random;
 import java.util.concurrent.Callable;
 import scala.Tuple2;
 
 /**
  *
  * @author badi
  */
 public class SOM<F,D> implements Callable<Field<F>> {
 
     List<D> data;
     SOMParams<F,D> somparams;
 
     public SOM (List<D> data, SOMParams<F,D> params) {
         this.data = data;
         this.somparams = params;
     }
 
 
     private Tuple2<List<D>,Random> shuffle (List<D> data, Random random_gen){
         synchronized(data){
             if(random_gen == null)
                 Collections.shuffle(data);
             else
                 Collections.shuffle(data, random_gen);
         }
         return new Tuple2<List<D>, Random> (data, random_gen);
     }
 
     public Field<F> call () {
 
         Tuple2<List<D>, Random> res = shuffle(data, somparams.random_gen);
         data = res._1();
         somparams.random_gen = res._2();
 
         Iterator<D> itr = data.iterator();
         while ( !somparams.stop_func.params(somparams).call() ){
 
             // we need to cycle over inputs
             if( !itr.hasNext() ) {
                 itr = data.iterator();
                 somparams.iterations++;
                somparams.projections = 0;
             }
 
             // the magic happens here
             somparams = somparams.project_func.params(itr.next(), somparams).call();
 
             // update the user and some state
             somparams.show_status_func.params(somparams).call();
             somparams.projections++;
         }
         
         return somparams.field;
     }
 
 
 
 }
