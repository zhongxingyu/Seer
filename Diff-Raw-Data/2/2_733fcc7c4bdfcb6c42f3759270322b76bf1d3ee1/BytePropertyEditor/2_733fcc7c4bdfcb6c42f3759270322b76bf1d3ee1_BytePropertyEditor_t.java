 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.sourceforge.frcsimulator.gui.propertyeditor;
 
 import javax.swing.JCheckBox;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import net.sourceforge.frcsimulator.internals.FrcBotSimProperty;
 
 /**
  *
  * @author wolf
  */
 public class BytePropertyEditor extends PropertyEditor<Byte> {
 	protected JCheckBox[] n_checkboxes = new JCheckBox[8];
 	protected FrcBotSimProperty<Byte> n_property;
 	protected boolean[] n_bits = new boolean[8];
 	protected static int bitIndex;
     @Override
 	public void initialize(String key, FrcBotSimProperty<Byte> iProperty) {
 		n_property = iProperty;
                for(bitIndex = 7; bitIndex >= 0;bitIndex--){
                     n_checkboxes[bitIndex] = new JCheckBox();
                     n_checkboxes[bitIndex].setSelected((n_property.get()&(byte)((char)1<<bitIndex))==(byte)((char)1<<bitIndex));
                     n_checkboxes[bitIndex].addChangeListener(new ChangeListener(){
                         public void stateChanged(ChangeEvent ce){
                             n_property.set((byte)(n_property.get()^((char)1<<bitIndex)));
                         }
                     });
                     add(n_checkboxes[bitIndex]);
                 }
 	}
 }
