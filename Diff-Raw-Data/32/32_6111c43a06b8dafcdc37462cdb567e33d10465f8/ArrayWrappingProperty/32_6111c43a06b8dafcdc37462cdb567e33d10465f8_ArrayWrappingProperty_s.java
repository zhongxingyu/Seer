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
		if (get() != to) {
 			Array.set(m_property.get(),m_index,to);
 			triggerChange();
 		}
 	}
 	@Override
 	public T get() {
 		return (T)Array.get(m_property.get(),m_index);
 	}
 
 }
