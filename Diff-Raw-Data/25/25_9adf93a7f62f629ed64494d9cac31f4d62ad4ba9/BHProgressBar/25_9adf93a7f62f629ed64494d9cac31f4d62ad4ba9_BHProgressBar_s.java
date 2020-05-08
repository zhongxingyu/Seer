 package org.bh.gui.swing.comp;
 
 import javax.swing.JProgressBar;
import javax.swing.JSlider;
 
 import org.bh.gui.IBHComponent;
 
 public class BHProgressBar extends JProgressBar implements IBHComponent{
 
 	private String key;
 	
 	public BHProgressBar(Object key, int min, int max){
 		super(min, max);
 		this.key = key.toString();
 	}
 	
 	@Override
 	public String getKey() {
 		return key.toString();
 	}
 
 	@Override
 	public String getHint() {
		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException("This method has not been implemented");
 	}
 
 }
