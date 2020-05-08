 package de.unisiegen.informatik.bs.alvis.sync.run;
 
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.io.xml.DomDriver;
 
 import de.unisiegen.informatik.bs.alvis.extensionpoints.IRunVisualizer;
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.PCObject;
 import de.unisiegen.informatik.bs.alvis.sync.datatypes.PCScenario;
 import de.unisiegen.informatik.bs.alvis.sync.graphicalrepresentations.AlvisSave;
 import de.unisiegen.informatik.bs.alvis.sync.graphicalrepresentations.AlvisScenario;
 import de.unisiegen.informatik.bs.alvis.sync.graphicalrepresentations.AlvisSerialize;
 
 public class RunVisualizer implements IRunVisualizer {
 
 	private AlvisScenario myScenario;
 	private Composite myParent;
 	private String myInputFilePath;
 	private PCScenario codeScenario;
 
 	@Override
 	public Image getImage() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Object getSourceCode() {
 
 		return null;
 	}
 
 	@Override
 	public boolean isRun() {
 		return true;
 	}
 
 	public static Object deserialize(String filename) {
 		Object o = null;
 		BufferedInputStream bis = null;
 		XStream x = new XStream(new DomDriver());
 		try {
 			bis = new BufferedInputStream(new FileInputStream(filename));
 			o = x.fromXML(bis);
 			bis.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return o;
 	}
 
 	@Override
	public boolean addVisualizing(Composite parent, String inputFile, String fileExtension) {
		if(!fileExtension.equals(".sync"))
			return false;
		
 		if (parent == null | inputFile == null) {
 			return false;
 		}
 		myParent = parent;
 		myInputFilePath = inputFile;
 		if (!createScenario()) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 	@Override
 	public ArrayList<PCObject> chooseVariable(PCObject typ, String bezeichner) {
 		ArrayList<PCObject> res = new ArrayList<PCObject>();
 
 		if (typ instanceof PCScenario) {
 			res.add(codeScenario);
 			return res;
 		}
 		return null;
 	}
 
 	public boolean createScenario() {
 		boolean res = false;
 //		try {
 //			AlvisSerialize seri = (AlvisSerialize) deserialize(myInputFilePath);
 //			myScenario = new AlvisScenario(myParent);
 //			new AlvisSave(myScenario, seri);
 //			codeScenario = new PCScenario();
 //			res = true;
 //		} catch (ClassCastException e) {
 //			return false;
 //		}
 		return res;
 	}
 
 }
