 import java.util.ArrayList;
 import java.util.List;
 
 
 /**
  * @author KarelPetranek
  * Represents a mental model corresponding to student answers
  */
 public class Model {
 	/**
 	 * @author KarelPetranek
 	 * Possible mental models appearing in the test 
 	 */
	@SuppressWarnings("javadoc")
 	public enum MainModel  {
 		NoModel,
 		M1,
 		M2,
 		M3,
 		M4,
 		M5,
 		M6,
 		M7,
 		M8,
 		M9,
 		M10,
 		M11
 	}
 	
 	/**
 	 * @author KarelPetranek
 	 * Mental submodels for the above models
 	 */
	@SuppressWarnings("javadoc")
 	public enum SubModel  {
 		NoSubmodel,
 		S1,
 		S2,
 		S3
 	}
 	
 	private MainModel main;
 	private SubModel sub;
 	
 	/**
 	 * @return Main model ID
 	 */
 	public MainModel getMain() {
 		return main;
 	}
 
 	/**
 	 * @return Submodel ID
 	 */
 	public SubModel getSub() {
 		return sub;
 	}
 	
 	/**
 	 * Creates a new model
 	 * @param main Main model
 	 * @param sub Submodel
 	 */
 	public Model(MainModel main, SubModel sub)  {
 		this.main = main;
 		this.sub = sub;
 	}
 	
 	@Override
 	public String toString()  {
 		String res = "";
 		res += main + "+" + sub;
 		return res;
 	}
 	
 	/**
 	 * Creates a list of model from a formatted string. The models are separated by pipes, model and
 	 * submodel are separated by a comma.
 	 * @param str The string representing the models
 	 * @return Parsed models
 	 */
 	public static List<Model> parseModels(String str)  {
 		String[] models = str.split("\\|");
 		List<Model> result = new ArrayList<Model>();
 		for (int i = 0; i < models.length; i++)  {
 			String[] modelAndSubmodel = models[i].split(",");
 			switch (modelAndSubmodel.length)  {
 			case 1:
 				result.add(new Model(stringToModel(modelAndSubmodel[0]), SubModel.NoSubmodel));
 			break;
 			
 			case 2:
 				result.add(new Model(stringToModel(modelAndSubmodel[0]), stringToSubmodel(modelAndSubmodel[1])));
 			break;
 			
 			default:
 				System.out.println("Warning: invalid model/submodel format: " + str);
 			}
 		}
 		
 		return result;
 	}
 
 	private static MainModel stringToModel(String string) {
 		for (MainModel m : MainModel.values())  {
 			if (string.toUpperCase().startsWith(m.toString().toUpperCase()))
 				return m;
 		}
 		return MainModel.NoModel;
 	}
 	
 	private static SubModel stringToSubmodel(String string) {
 		for (SubModel m : SubModel.values())  {
 			if (string.toUpperCase().startsWith(m.toString().toUpperCase()))
 				return m;
 		}
 		return SubModel.NoSubmodel;
 	}	
 	
 }
