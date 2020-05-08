 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author GuntherDW
  */
 public class SnowBallShapedRecipe {
 
     // private List<iw> ingredients;
     private Map<Integer, iw> ingredients;
     private iw result;
 
     public SnowBallShapedRecipe(iw result) {
         this.result = result;
         this.ingredients = new HashMap<Integer, iw>();
     }
 
     public SnowBallShapedRecipe(iw result, Map<Integer, iw> ingredients) {
         this.result = result;
         this.ingredients = ingredients;
     }
 
     public SnowBallShapedRecipe(iw result, List<iw> ing) {
         this.result = result;
         this.ingredients = new HashMap<Integer, iw>();
         for(int x=0; x<9; x++) {
             if(ing.get(x)!=null) {
                 ingredients.put(x, ing.get(x));
             }
         }
     }
 
     public iw getResult() {
         return result;
     }
 
     public void setResult(iw result) {
         this.result = result;
     }
 
     public Map<Integer, iw> getIngredients() {
         return ingredients;
     }
 
     public void setIngredients(Map<Integer, iw> ingredients) {
         this.ingredients = ingredients;
     }
 
     public void setIngredientSpot(int spot, iw ingredient) {
         if(spot>=0 && spot < 9) {
             this.ingredients.put(spot, ingredient);
         }
     }
 
     public iw getIngredientSpot(int spot) {
         if(this.ingredients.containsKey(spot)) {
             return this.ingredients.get(spot);
         } else {
             return null;
         }
     }
 
     public Object[] generateRecipeLine() {
         // Character c = 'A';
         List<Object> objects = new ArrayList<Object>();
         String biggestline = "";
         String line = "";
         List<Integer> ings = new ArrayList<Integer>();
         List<Object> inglines = new ArrayList<Object>();
         List<String> lines = new ArrayList<String>();
         Integer rows = 0, cols = 0;
 
         int ingcount = 0;
 
         for(iw ing : ingredients.values()) {
             if(!ings.contains(ing.c)) {
                 ings.add(ing.c);
                 char c = 'A';
                 c += ingcount;
                 inglines.add(Character.valueOf(c));
                 inglines.add(ing);
                 ingcount++;
             }
         }
         for(int x=0; x<9; x+=3) {
             line = "";
             for(int i=0; i<3; i++)
             {
                 if(ingredients.containsKey(i+x)) {
                     int itemID = ingredients.get(i+x).c;
                     int pos = ings.indexOf(itemID);
                     char chara = 'A';
                     chara += pos;
                     line+=chara;
                     // System.out.println("char : "+chara+" voor itemID : "+itemID+"!");
                 } else {
                     line+=' ';
                 }
             }
             int linelength = line.trim().length();
             if(cols<linelength) {
                 cols=linelength;
                 biggestline = line;
             }
             boolean add = true;
 
             if(linelength == 0 && !(x==3))
                 add=false;
 
             if(add)
                 lines.add(line);
         }
         if(cols==1) {
            for(int col=0; col<lines.size(); col++) {
                 String lin = lines.get(col).trim();
                 lines.set(col, lin);
             }
         } else if(cols==2) {
             boolean trimmode = (biggestline.charAt(2) == ' ');
            for(int col=0; col < lines.size(); col++) {
                 String lin = lines.get(col);
                // System.out.println(col+": "+lin);
                 if(trimmode) {
                     lin = lin.substring(0, 2);
                 } else {
                     lin = lin.substring(1, 2);
                 }
                 lines.set(col, lin);
             }
         }
         objects.addAll(lines);
         objects.addAll(inglines);
         /* for(String s : lines) {
            System.out.println("line:"+s.replace(' ', '.'));
         } */
         return objects.toArray();
     }
 }
