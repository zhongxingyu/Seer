 package dans.model;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 
 public class Warning {
 	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
 	private Road road;
 	private List<String> warnings;
 	
 	public void addPropertyChangeListener(PropertyChangeListener listener) {
 		pcs.addPropertyChangeListener(listener);
 	}
 	
 	public void removePropertyChangeListener(PropertyChangeListener listener){
 		pcs.removePropertyChangeListener(listener);
 	}
 	
 	public void addWarning(String warning, int priority) {
 		warnings.add(priority, warning);
 		pcs.firePropertyChange("Warning", false, true);
 	}
 	
 	public void removeWarning(int index) {
 		if ((index<0) || (index>=warnings.size())) return;
		warnings.remove(0);
 		pcs.firePropertyChange("Warning", false, true);
 	}
 	
 	public Warning(Road road, String[] texts){
 		this.road = road;
 		this.road.addPropertyChangeListener(pcl);
 		this.warnings = new LinkedList<>(Arrays.asList(texts));
 	}
 	
 	public Road getRoad() {
 		return road;
 	}
 	public String[] getWarnings() {
 		return warnings.toArray(new String[0]);
 	}
 	
 	PropertyChangeListener pcl = new PropertyChangeListener() {
 		@Override
 		public void propertyChange(PropertyChangeEvent evt) {
 			pcs.firePropertyChange("Text", null, null);
 		}
 	};
 }
