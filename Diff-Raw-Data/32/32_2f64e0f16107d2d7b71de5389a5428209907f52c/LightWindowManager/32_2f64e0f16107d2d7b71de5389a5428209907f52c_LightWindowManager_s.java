 package com.partyrock.gui;
 
 import java.util.HashSet;
 import java.util.Iterator;
 
 import org.eclipse.swt.widgets.Display;
 
 import com.partyrock.LightMaster;
 import com.partyrock.gui.elements.ElementDisplay;
 
 /**
  * LightWindowManager manages *all* GUI things. Constructing the
  * LightWindowManager automatically makes a LightWindow
  * @author Matthew
  * 
  */
 public class LightWindowManager {
 	private LightMaster master;
 	private LightWindow main;
 	private Display display;
 	private HashSet<ElementDisplay> elementDisplays;
 
 	public LightWindowManager(LightMaster master) {
 		this.master = master;
 		display = new Display();
 		elementDisplays = new HashSet<ElementDisplay>();
 		main = new LightWindow(master, this);
 		addElementDisplay(main);
 	}
 
 	public void loop() {
 		main.loop();
 	}
 
 	public Display getDisplay() {
 		return display;
 	}
 
 	public LightMaster getMaster() {
 		return master;
 	}
 
 	public LightWindow getMain() {
 		return main;
 	}
 
 	public void addElementDisplay(ElementDisplay display) {
 		elementDisplays.add(display);
 	}
 
 	/**
 	 * Update all windows the LightWindowManager knows about
 	 */
 	public void updateElements() {
 		Iterator<ElementDisplay> it = elementDisplays.iterator();
 
 		while (it.hasNext()) {
 			ElementDisplay display = it.next();
 			if (display.isDisposed()) {
 				it.remove();
 			} else {
 				display.updateElements();
 			}
 		}

 	}
 }
