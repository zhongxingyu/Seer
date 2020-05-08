 package ca.ualberta.team2recipefinder;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 public class IngredientList extends Model<View>{
 	private String filename="IngredientList.sav";
 	private String path;
 	
 	ArrayList<Ingredient> ingredientList;
 	
 	public IngredientList(){
 		// gets the folder where we should put the files
 		// created by the application (and appends filename)
 		path = RecipeFinderApplication.getAppContext().getFilesDir() + "/" + filename;
 		
 		ingredientList = load();
 	}
 	
 	private ArrayList<Ingredient> load() {  
 		ArrayList<Ingredient> ingredientList = new ArrayList<Ingredient>();
 		   
 		try {  
 			ObjectInputStream in = new ObjectInputStream(new FileInputStream(path));  
 			ingredientList = (ArrayList<Ingredient>) in.readObject();
 			in.close();
 		} 
 		catch (IOException e) {  
 			e.printStackTrace();  
 		} catch (ClassNotFoundException e) {  
 			e.printStackTrace();  
 		}
 			
 		return ingredientList;
 	}
 	   
 	public void add(Ingredient ingredient){		
 		boolean alreadyThere = false;
 		for(int n = 0; n<ingredientList.size(); n++){
			if(ingredientList.get(n).getType().equalsIgnoreCase(ingredient.getType())){
 				alreadyThere = true;
 				break;
 			}
 		}
 		if(!alreadyThere){
 			ingredientList.add(ingredient);
 		}
 		
 		sort(ingredientList);
 		write(ingredientList);
 		notifyViews();
 	}
 	public void remove(Ingredient ingredient){
 		ingredientList.remove(ingredient);
 		write(ingredientList);
 		notifyViews();
 	}
 	public boolean search(Ingredient ingredient){
 		if(ingredientList.contains(ingredient))
 			return true;
 		return false;
 	}
 	
 	private void write(ArrayList<Ingredient> ingredientList){
 		try {  
 			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path));
 			out.writeObject(ingredientList);
 			out.close();  
 		} catch (FileNotFoundException e) {  
 			e.printStackTrace();  
 		} catch (IOException e) {  
 			e.printStackTrace();  
 		}  	
 	}
 	
 	private void sort(ArrayList<Ingredient> ingredientList){
 		Ingredient temp;
 		   
 		for(int n = ingredientList.size()-1; n > 1; n--)
 			for(int i = 0; i<n; i++){
 				if(ingredientList.get(i).getType().compareToIgnoreCase(ingredientList.get(i+1).getType()) > 0){
 					temp = ingredientList.get(i);
 		   			ingredientList.set(i, ingredientList.get(i+1));
 		   			ingredientList.set(i+1, temp);
 		   		}				   
 			}
 	}
 	
 	// get ingredient with a specific type
 	public Ingredient getIngredient(String type) {		
 		for (Ingredient ingredient : ingredientList) {
 			if (ingredient.getType().equals(type)) {
 				return ingredient;
 			}
 		}
 		
 		return null;
 	}
 	
 	// replace two ingredients with same type
 	public void replaceIngredient(Ingredient oldIngredient, Ingredient newIngredient) {
 		// only replace if they represent the same ingredient
 		if (oldIngredient.getType().equals(newIngredient.getType())) {
 			ingredientList.remove(oldIngredient);
 			ingredientList.add(newIngredient);
 			sort(ingredientList);
 			write(ingredientList);
 			notifyViews();
 		}
 		else {
 			// error case
 		}
 	}
 	
 	public ArrayList<Ingredient> searchIngredient(String keyword) {
 		ArrayList<Ingredient> matchingIngredients = new ArrayList<Ingredient>();
 		   
 		   
 		for(int i = 0; i<ingredientList.size(); i++){
 			if(ingredientList.get(i).getType().toLowerCase(Locale.ENGLISH).contains(keyword.toLowerCase(Locale.ENGLISH)) || ingredientList.get(i).getUnity().toLowerCase(Locale.ENGLISH).contains(keyword.toLowerCase(Locale.ENGLISH))){
 				matchingIngredients.add(ingredientList.get(i));
 			}
 		}
 		   	   
 		return matchingIngredients;
 		   
 	}
 	
 	public List<Ingredient> getIngredients() {
 		return ingredientList;
 	}
 }
