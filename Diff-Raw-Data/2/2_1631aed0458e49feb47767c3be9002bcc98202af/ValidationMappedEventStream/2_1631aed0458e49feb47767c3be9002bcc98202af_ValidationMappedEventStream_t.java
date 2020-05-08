 package org.kantega.falls.stream;
 
 import fj.Effect;
 import fj.Show;
 import fj.data.Validation;
 import org.kantega.falls.EventStream;
 import org.kantega.falls.EventStreamSubscriber;
 import org.kantega.falls.OpenEventStream;
 import org.kantega.falls.StreamEvent;
 
 public class ValidationMappedEventStream<E, T> extends EventStream<T>
 {
     private final EventStream<Validation<E, T>> wrappedStream;
 
     private final Show<E> errorShow;
 
     public ValidationMappedEventStream(EventStream<Validation<E, T>> stream, Show<E> show)
     {
         this.wrappedStream = stream;
         this.errorShow = show;
     }
 
     @Override
     public OpenEventStream<T> open(final Effect<StreamEvent<T>> effect)
     {
         OpenEventStream<Validation<E, T>> open =
                wrappedStream.open(
                         EventStreamSubscriber.create(
                                 new Effect<StreamEvent.Next<Validation<E, T>>>()
                                 {
                                     @Override
                                     public void e(StreamEvent.Next<Validation<E, T>> next)
                                     {
                                         if (next.value.isSuccess())
                                         { effect.e(StreamEvent.next(next.value.success())); } else
                                         { effect.e(StreamEvent.<T>error(new Exception(errorShow.showS(next.value.fail())))); }
                                     }
                                 },
                                 EventStreamSubscriber.<Validation<E, T>,T>forwardError(effect),
                                 EventStreamSubscriber.<Validation<E, T>,T>forwardDone(effect)));
 
         return OpenEventStream.wrap(this, open);
     }
 }
