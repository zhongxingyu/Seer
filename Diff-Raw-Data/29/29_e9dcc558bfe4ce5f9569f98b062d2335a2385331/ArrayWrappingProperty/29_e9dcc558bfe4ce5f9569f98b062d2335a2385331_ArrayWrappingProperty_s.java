 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.sourceforge.frcsimulator.gui.propertyeditor;
 
 import java.lang.reflect.Array;
 import net.sourceforge.frcsimulator.internals.FrcBotSimProperty;
 
 /**
  *
  * @author wolf
  */
 public class ArrayWrappingProperty<T> extends FrcBotSimProperty<T>{
 	private FrcBotSimProperty<T[]> m_property;
 	private int m_index;
 	public ArrayWrappingProperty(FrcBotSimProperty<T[]> property, int index) {
 		m_index = index;
 		m_property = property;
 	}
 	@Override
 	public void set(T to) {
 		// TODO figure out why get() != to when they're identical
 		//System.out.println(to.getClass() + ":"+get().getClass());
 		//System.out.println("Set "+to+" was "+get().equals(to) + " equal? "+(!get().equals(to)));
 		System.out.println(equals(to));
 		if (!equals(to)) {
			Array.set(m_property.get(),m_index,to);
 			triggerChange();
 		}
 	}
 	@Override
 	public T get() {
 		return (T)Array.get(m_property.get(),m_index);
 	}
 
 	public boolean equals(Object check) {
 		//System.out.println(".equals CALLED!");
 		T value=get();
 		if (value.equals(check)) {
 			//System.out.println("true 1");
 			return true;
 		} else // It might still be equal, run some other checks
 		if (value instanceof Number) {
 			//System.out.println("Number check: "+((Double)((Number)check).doubleValue())+":"+(Double)(((Number)value).doubleValue()));
 			//System.out.println(((Double)((Number)check).doubleValue()).equals((Double)(((Number)value).doubleValue())));
 			return ((Double)((Number)check).doubleValue()).equals((Double)(((Number)value).doubleValue()));
 		} else {
 			//System.out.println("false!");
 			return false; // As far as we can tell, it's not equal
 		}
 	}
 }
