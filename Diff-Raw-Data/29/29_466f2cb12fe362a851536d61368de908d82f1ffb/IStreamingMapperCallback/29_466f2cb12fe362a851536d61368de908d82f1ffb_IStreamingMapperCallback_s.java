 package cmf.eventing.patterns.streaming;
 
 /**
  * Applies a mapping function to an element within an {@link java.util.Iterator} to allow the caller
  * to customize the elements before being published by the {@link IStreamingEventBus}
  * User: jholmberg
  * Date: 6/1/13
  */
public interface IStreamingMapperCallback<T> {
    public T map(Object element);
 }
