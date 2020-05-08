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
			if (get() instanceof Byte){
				Array.set(m_property.get(),m_index,((Number)to).byteValue());
			} else if(get() instanceof Short){
				Array.set(m_property.get(),m_index,((Number)to).shortValue());
			} else if(get() instanceof Integer){
				Array.set(m_property.get(),m_index,((Number)to).intValue());
			} else if(get() instanceof Float){
				Array.set(m_property.get(),m_index,((Number)to).floatValue());
			} else if(get() instanceof Long){
				Array.set(m_property.get(),m_index,((Number)to).longValue());
			} else if(get() instanceof Double){
				Array.set(m_property.get(),m_index,((Number)to).doubleValue());
			} else if(get() instanceof Boolean){
				Array.set(m_property.get(),m_index,Boolean.parseBoolean(to.toString()));
			} else if(get() instanceof Character){
				Array.set(m_property.get(),m_index,to.toString().charAt(0));
			} else if(get() instanceof String){
				Array.set(m_property.get(),m_index,to.toString());
			} else{
				Array.set(m_property.get(),m_index,to);
			}
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
