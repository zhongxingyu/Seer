 package ru.ifmo.neerc.framework;
 
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicReference;
 
 public class Watch<Type> {
 	Set<Callback<Type>> listeners;
 	AtomicReference<Type> value;
 
 	public Type get() {
 		return value.get();
 	}
 
 	public void set(Type v) {
 		while (true) {
 			Type old = value.get();
 			if (old.equals(v)) {
 				return;
 			}
 
 			if (value.compareAndSet(old, v)) {
 				for (Callback<Type> f : listeners) {
 					f.exec(v);
 				}
 				return;
 			}
 		}
 	}
 
 	public void addListener(Callback<Type> c) {
 		listeners.add(c);
 	}
 }
