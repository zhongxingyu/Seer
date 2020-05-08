 package kembe.stream;
 
 import fj.Effect;
 import fj.data.Option;
 import kembe.EventStream;
 import kembe.EventStreamSubscriber;
 import kembe.OpenEventStream;
 import kembe.StreamEvent;
 
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.atomic.AtomicInteger;
 
 public class EventStreamFork<A>{
 
     private final EventStream<A> source;
     private CopyOnWriteArrayList<EventStreamSubscriber<A>> subscribers = new CopyOnWriteArrayList<>(  );
     private volatile Option<OpenEventStream<A>> openStream = Option.none();
     private AtomicInteger count = new AtomicInteger(0);
     private AtomicInteger opened = new AtomicInteger( 0 );
 
     public EventStreamFork(EventStream<A> source) {
         this.source = source;
     }
 
     public EventStream<A> newStream(){
         count.incrementAndGet();
         return buildStream();
     }
 
 
     private EventStream<A> buildStream(){
         EventStream<A> es = new EventStream<A>() {
             @Override public OpenEventStream<A> open(EventStreamSubscriber<A> subscriber) {
                 subscribers.add( subscriber );
                 if(opened.incrementAndGet()==count.get()){
                     OpenEventStream<A> os = source.open( new EventStreamSubscriber<A>() {
                         @Override public void e(StreamEvent<A> event) {
                             distribute( event );
                         }
                     } );
                 }
                 return new OpenEventStream<A>() {
                     @Override public EventStream<A> close() {
                         if(opened.decrementAndGet()==0){
                             openStream.foreach( new Effect<OpenEventStream<A>>() {
                                 @Override public void e(OpenEventStream<A> aOpenEventStream) {
                                     aOpenEventStream.close();
                                 }
                             } );
                         }
                         return buildStream();
                     }
                 };
             }
         };
         return es;
     }
 
     public void distribute(StreamEvent<A> event) {
         for(EventStreamSubscriber<A> s:subscribers){
             s.e( event );
         }
     }
 }
