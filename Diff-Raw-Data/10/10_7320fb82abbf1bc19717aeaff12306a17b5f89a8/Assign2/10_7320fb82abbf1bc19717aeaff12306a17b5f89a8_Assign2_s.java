 import java.io.FileWriter;
 import java.util.ArrayList;
 
 public class Assign2 {
 
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		try {	
			ArrayList<CakeRecipe> recipes = CakeRecipeUtil.parseDirectory(args[0]);
 			for (CakeRecipe recipe:recipes)
 			{
 				FileWriter file = new FileWriter(recipe.getName()+".txt");
 				file.write(recipe.output());
 				file.close();
 			}

    			Gui theGui = new Gui();
    			theGui.launchFrame();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			System.out.println("exception: " + e);
 			System.exit(0);
 		}
 	}
 }
