 package org.eclipse.emf.emfstore.client.test.common.observerbus.assets;
 
import org.eclipse.emf.emfstore.common.observer.IObserver;
 
public interface B extends A, IObserver {
 
 	public void setMSGToFoo(CImpl tester);
 }
