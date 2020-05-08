 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.ObjectInput;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutput;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 
 public class StylesData implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 5665254358739912680L;
 	static StylesData instance = new StylesData();
 	String masterStyle = "";
 	List<DRLStyle> styles = new ArrayList<DRLStyle>();
 	String masterSelection = "";
 	String excelSelection = "";
  
 	protected StylesData() {
 	}
 
 	public String getMasterStyleFilePath() {
 		return masterStyle;
 	}
 	public void setMasterStyle(String masterStyle) {
 		this.masterStyle = masterStyle;
 	}
 	public List<DRLStyle> getStyles() {
 		return styles;
 	}
 	public void setStyles(List<DRLStyle> styles) {
 		this.styles = styles;
 	}
 	public String getMasterSelection() {
 		return masterSelection;
 	}
 	public void setMasterSelection(String masterSelection) {
 		this.masterSelection = masterSelection;
 	}
 	public String getExcelSelection() {
 		return excelSelection;
 	}
 	public void setExcelSelection(String excelSelection) {
 		this.excelSelection = excelSelection;
 	}
 	public static StylesData getInstance() {
 		return instance;
 	}
 	
 	public static void loadClassFromFile() {
 		try {
 			ObjectInput input = new ObjectInputStream(new BufferedInputStream(new FileInputStream("styledata.ser")));
 			try {
 				instance = (StylesData) input.readObject();
 			} finally {
 				input.close();
 			}
 		} catch (Exception e) {
 			instance = new StylesData();
 		}
 	}
 	
 	public static void saveClassToFile() {
 		try {
			File file = new File("styledata.ser");
			boolean makeNewFile = !file.exists();
			
			ObjectOutput output = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file,makeNewFile)));
 			try {
 				output.writeObject(instance);
 			} finally {
 				output.close();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
