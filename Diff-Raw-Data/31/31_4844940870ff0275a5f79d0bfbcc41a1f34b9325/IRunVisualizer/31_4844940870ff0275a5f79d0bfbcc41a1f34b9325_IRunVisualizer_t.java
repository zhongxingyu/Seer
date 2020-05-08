 package de.unisiegen.informatik.bs.alvis.extensionpoints;
 
 import java.util.ArrayList;
 
 import org.eclipse.swt.widgets.Composite;
 
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.PCObject;
 
 public interface IRunVisualizer extends IExportItem{
 
 	/**
 	 * Adds all necessary elements to the parent of the run view.
 	 * @param parent
 	 */
	public boolean addVisualizing(Composite parent, String inputFile, String fileExtension);
 	
 	/**
 	 * 
 	 * @param typ
 	 * @param bezeichner
 	 * @return
 	 */
 	public ArrayList<PCObject> chooseVariable(PCObject typ, String bezeichner);
 	
 }
