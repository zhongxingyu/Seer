 package reactive;
 
 import lang.Maybe;
 
 import org.reactive.Signal;
 import org.reactive.Source;
 
public class ListSource<T> implements ListSignal2<T> {
 
 	
 	private Source<Integer> sizeSource = new Source<Integer>(0); 
 	ListSourceElementsByIndex<T> signalByIndex = new ListSourceElementsByIndex<T>();
 
 	@Override
 	public synchronized Signal<Integer> size() {
 		return sizeSource;
 	}
 
 	public synchronized void add(T value) {
 		
 		if (value == null)
 			throw new IllegalArgumentException("value must not be null");
 		
 		int size = sizeSource.currentValue().intValue() + 1;
 		int index = size - 1;
 
 		signalByIndex.supply(index, value);
 		
 		sizeSource.supply(size);
 	}
 
 	public synchronized Signal<Maybe<T>> get(Signal<Integer> source) {
 		
 		return new ListElementSignal<T>(source, signalByIndex).output();
 
 	}
 
 	public synchronized Maybe<T> currentGet(int i) {
 		Signal<Maybe<T>> value = signalByIndex.get(i);
 		return value.currentValue();
 	}
 
 	public synchronized void remove(int i) {
 		signalByIndex.supply(i, null);	
 		sizeSource.supply(sizeSource.currentValue() - 1);
 	}
 
 	@Override
 	public synchronized int currentSize() {
 		return sizeSource.currentValue();
 	}
 
 	public synchronized void remove(T value) {
 		remove(signalByIndex.indexOf(value));
 	}
 }
