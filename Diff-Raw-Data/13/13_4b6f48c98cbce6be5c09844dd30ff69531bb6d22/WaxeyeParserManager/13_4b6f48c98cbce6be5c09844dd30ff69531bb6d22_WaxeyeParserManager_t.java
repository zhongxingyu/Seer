 /**
 (C) Kyle Kamperschroer 2013
  */
 
 package com.kylek.ripe.core;
 
 import java.util.List;
 import java.util.Scanner;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.waxeye.input.InputBuffer;
 import org.waxeye.parser.ParseError;
 import org.waxeye.parser.ParseResult;
 import org.waxeye.ast.IAST;
 
 // Import our generated parser and types
 import com.kylek.ripe.waxeye.*;
 
 public class WaxeyeParserManager {
 
    ////////////////////////////////////////
    // Members
    ////////////////////////////////////////
 
    // Our recipe splitter, responsible for high level separation
    private RecipeSplitter mRecipeSplitter;
    
    // Our attributes parser, generated from the attributes grammar
    private AttributesParser mAttributesParser;
 
    // Our ingredients list parser, generated from the ingredients list grammar
    private IngredientsListParser mIngredientsListParser;
 
    // Our attributes parse result tree that we can iterate through
    private ParseResult<AttributesType> mAttributesResults;
 
    // Our ingredients list parse result tree that we can iterate through
    private ParseResult<IngredientsListType> mIngredientsListResults;
 
    // The attributes string we need to pull pieces out of
    private String mAttributesString;
 
    // The ingredients list string we need to pull pieces out of
    private String mIngredientsListString;
 
    // A bool to determine if the last parse was successful
    private boolean mPreviousSuccess;
 
    // A string that represents our error message
    private String mErrorMessage;
 
    ////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////
 
    // Default constructor
    public WaxeyeParserManager(){
       mRecipeSplitter = new RecipeSplitter();
       mAttributesParser = new AttributesParser();
       mIngredientsListParser = new IngredientsListParser();
       mAttributesResults = null;
       mIngredientsListResults = null;
       mPreviousSuccess = false;
       mErrorMessage = "";
    }
 
    ////////////////////////////////////////
    // Setters and getters
    ////////////////////////////////////////
 
    // Get the error message
    public String getErrorMessage() { return mErrorMessage; }
 
    ////////////////////////////////////////
    // Other methods
    ////////////////////////////////////////
 
    // A method for parsing
    public boolean parseString(String input){
 
       // Start by splitting up the input string
       try{
          mRecipeSplitter.splitRecipe(input);
       }catch (Exception e){
          // There was an issue splitting!
          System.out.println("Split error!");
          System.out.println(e.toString());
          e.printStackTrace();
          // TODO
          mPreviousSuccess = false;
          mErrorMessage = "There was an error splitting the recipe into" +
             " meaningful sections. Did you format the input recipe appropriately?" +
             "<br/><br/>" + e.getMessage();
          return mPreviousSuccess;
       }
 
       // Start by taking care of the attributes list
       mAttributesString = mRecipeSplitter.getAttributes();
       InputBuffer inputBuffer =
          new InputBuffer(mAttributesString.toCharArray());
       mAttributesResults = mAttributesParser.parse(inputBuffer);
 
       ParseError error = mAttributesResults.getError();
       if (error != null){
          // Get an error message ready
          if (mAttributesString.equals("")){
             mErrorMessage = "<p>Unable to find the attributes of the recipe!</p>\n";
          }
          else{
             mErrorMessage = generateErrorMessage(error, mAttributesString);
          }
          mPreviousSuccess = false;
          return mPreviousSuccess;
       }else{
     	 System.out.println("\nAttributes Tree:\n" + mAttributesResults.toString());
          mErrorMessage = "";
          mPreviousSuccess = true;
       }
 
       // Now we need to take care of the ingredients list parsing
       mIngredientsListString = mRecipeSplitter.getIngredientsList();
       inputBuffer =
          new InputBuffer(mIngredientsListString.toCharArray());
       mIngredientsListResults = mIngredientsListParser.parse(inputBuffer);
 
       // Check if there is any error
       error = mIngredientsListResults.getError();
       if (error != null){
          // Get an error message ready
          if (mIngredientsListString.equals("")){
             mErrorMessage = "<p>Unable to find the ingredients list of the recipe!</p>\n";
          }
          else{
             mErrorMessage = generateErrorMessage(error, mIngredientsListString);
          }
          mPreviousSuccess = false;
          return mPreviousSuccess;
       }else{
     	 System.out.println("\nIngredients List Tree:\n" + mIngredientsListResults.toString());
          mErrorMessage = "";
          mPreviousSuccess = true;
       }
 
       // Looks good
       return mPreviousSuccess;
    }
 
    // A method for getting an error message
    private String generateErrorMessage(ParseError error, String input){
      String err =
         "There was an error parsing the recipe you've provided. " +
         "The formatting looks valid, but the system does not know how " +
         "to handle the value pointed at below. Are you sure it's valid?" +
         "<br/><br/><pre>";
          
       // Cycle through some of the input string)
       Scanner scanner = new Scanner(input);
       int count = 1; // Waxeye starts at 1
       int errLine = error.getLine();
       err += "<font color=\"gray\">\n";
       while (scanner.hasNextLine()) {
          String curLine = scanner.nextLine();
          if (count == errLine){
             // We want this line in red
             err += "</font>" +
                "<font color=\"red\">" +
                curLine +
                "</font>\n";
 
             // Now we need to add a line to get to the correct column
             for (int i=0; i<error.getColumn(); i++){
                err += " ";
             }
 
             err += "^ should be \"" +
                error.getNT() +
                "\"\n<font color=\"gray\">\n";
          }
          else{
             err += curLine + "\n";
          }
          count++;
       }
       err += "</pre>";
       scanner.close();
       return err;
    }
 
    // A method for getting out the built recipe object
    public Recipe getParsedRecipe() throws Exception{
       // Return null if the previous parse was not a success.
       if (!mPreviousSuccess){
          return null;
       }
       
       // Build the return recipe object
       Recipe retRecipe = new Recipe();
 
       // The splitter already has found our title and directions
       retRecipe.setName(mRecipeSplitter.getTitle());
       retRecipe.setDirections(mRecipeSplitter.getDirections());
       
       // Navigate through the tree and build the Recipe.
       IAST<AttributesType> attributesRoot =
          mAttributesResults.getAST();
       IAST<IngredientsListType> ingredientsListRoot =
          mIngredientsListResults.getAST();
 
       // Update the recipes attributes (retRecipe passed by ref)
       getAttributes(attributesRoot, retRecipe);
 
       // Update the recipes ingredients list
       retRecipe.setIngredients(getIngredientsList(ingredientsListRoot));
       
       // Return the built recipe
       return retRecipe;
    }
 
    // Get the title out of the parse
    private String getAttributesText(IAST<AttributesType> iAST){
       // Get the start and end indices
       final int startIdx = iAST.getPosition().getStartIndex();
       final int endIdx = iAST.getPosition().getEndIndex();
 
       // Now return the information from the parsed attributes
       return mAttributesString.substring(startIdx, endIdx).trim();
    }
 
    // Get the title out of the parse
    private String getIngredientsListText(IAST<IngredientsListType> iAST){
       // Get the start and end indices
       final int startIdx = iAST.getPosition().getStartIndex();
       final int endIdx = iAST.getPosition().getEndIndex();
 
       // Now return the information from the parsed string
       return mIngredientsListString.substring(startIdx, endIdx).trim();
    }
 
    // Get the attributes out of the list and update the recipe
    private void getAttributes(IAST<AttributesType> attributesChild,
                               Recipe recipe){
       // Cycle though the attributes, getting each one out
       List<IAST<AttributesType>> curAttributesChildren = attributesChild.getChildren();
       List<IAST<AttributesType>> attributesChildrensChildren;
       IAST<AttributesType> curAttributesChild;
       IAST<AttributesType> theActualAttributesAst;
       AttributesType theActualAttributesType;
       // Iterate through the children
       for (int i=0; i<curAttributesChildren.size(); i++){
          curAttributesChild = curAttributesChildren.get(i);
          
          // Think of a better name yet?
          attributesChildrensChildren = curAttributesChild.getChildren();
          assert attributesChildrensChildren.size() == 1;
          theActualAttributesAst = attributesChildrensChildren.get(0);
          theActualAttributesType = theActualAttributesAst.getType();
 
          // Do whatever is necessary for this type
          if (theActualAttributesType == AttributesType.YIELD){
             recipe.setYield(getYield(theActualAttributesAst));
          } else if (theActualAttributesType == AttributesType.COOK_TIME){
             recipe.setCookTime(getCookTime(theActualAttributesAst));
          } else if (theActualAttributesType == AttributesType.PREP_TIME){
             recipe.setPrepTime(getPrepTime(theActualAttributesAst));
          } else if (theActualAttributesType == AttributesType.OVERALL_TIME){
             recipe.setOverallTime(getOverallTime(theActualAttributesAst));
          } else if (theActualAttributesType == AttributesType.COOL_TIME){
             recipe.setCoolTime(getCoolTime(theActualAttributesAst));
          }
       }
    }
 
    // Get the yield information out of the current piece
    private Yield getYield(IAST<AttributesType> yieldChild){
       // Yield tree looks like this for the phrase "Serves: 10 persons"
       // YIELD
       // - YIELD_PREFIX
       // -- Serves
       // - :
       // - AMOUNT
       // -- NUMBER_OR_FRACTION
       // --- 10
       // - UNIT
       // -- persons
 
       // Create our Yield object
       Yield yield = new Yield();
 
       // Get the yield children
       List<IAST<AttributesType>> yieldChildren = yieldChild.getChildren();
 
       // Iterate through the children
       IAST<AttributesType> curChild;
       for (int i=0; i<yieldChildren.size(); i++){
          curChild = yieldChildren.get(i);
          AttributesType curType = curChild.getType();
          if (curType == AttributesType.AMOUNT){
             String amountStr = getAttributesText(curChild);
             double amountDbl = Double.parseDouble(amountStr);
             yield.setValue(amountDbl);
          }
          else if (curType == AttributesType.UNIT){
             yield.setUnit(getAttributesText(curChild));
          }
       }
 
       // Return out built yield.
       return yield;
    }
 
    // Get the cook time out of the current piece
    private String getCookTime(IAST<AttributesType> cookTimeChild){
       // Cook time tree looks like this for "Cook time: 20 Minutes"
       // COOK_TIME
       // - COOK_TIME_PREFIX
       // -- CookTime
       // - :
       // - AMOUNT
       // -- NUMBER_OR_FRACTION
       // --- 20
       // - TIME_UNITS
       // -- Minutes
       List<IAST<AttributesType>> cookTimeChildren = cookTimeChild.getChildren();
 
       IAST<AttributesType> curChild;
       String cookTime = "";
       for (int i=0; i<cookTimeChildren.size(); i++){
          // Get the current child
          curChild = cookTimeChildren.get(i);
          AttributesType curType = curChild.getType();
          // We are ignoring the prefix. It's not important
          if (curType == AttributesType.AMOUNT){
             cookTime += " " + getAttributesText(curChild);
          }
          else if (curType == AttributesType.TIME_UNITS){
             cookTime += " " + getAttributesText(curChild);
          }
       }
 
       // Return our built time
       return cookTime.trim();
    }
 
    // Get the prep time out of the current piece
    private String getPrepTime(IAST<AttributesType> prepTimeChild){
       // The tree for prep is the same as cook, so use the same
       // method
       return getCookTime(prepTimeChild);
    }
 
    // Get the overall time out of the current piece
    private String getOverallTime(IAST<AttributesType> overallTimeChild){
       // The tree for overall is the same as cook, so use the
       // same method.
       return getCookTime(overallTimeChild);
    }
 
    // Get the cool time out of the current piece
    private String getCoolTime(IAST<AttributesType> coolTimeChild){
       // The tree for cool is the same as cook, so use the
       // same method.
       return getCookTime(coolTimeChild);
    }
 
    // Get the ingredients list and update the given recipe
    private IngredientsList getIngredientsList(IAST<IngredientsListType> ingredientsListChild){
       // The ingredients list tree looks something like this:
       // - INGREDIENTS_LIST
       // -- INGREDIENT
       // --- MEASUREMENT
       // ---- AMOUNT
       // ----- 2
       // ---- SPECIFIER (opt)
       // ----- ( (opt)
       // ------ MEASUREMENT
       // ------- AMOUNT
       // -------- NUMBER_OR_FRACTION
       // --------- 26
       // ------- UNIT
       // -------- ounce
       // ------ ) (opt)
       // ---- UNIT (opt)
       // ----- jars
       // --- PRODUCT
       // ---- WORDS
       // ----- spaghetti sauce
       // -- INGREDIENT
       // ... repeats
 
       // Build the ingredients list object, which will contain
       // all of the ingredients.
       IngredientsList ingList = new IngredientsList();
       
       // Let's go through each ingredient, getting each part
       List<IAST<IngredientsListType>> ingredients = ingredientsListChild.getChildren();
 
       // Iterate through, grabbing the ingredient for each
       // building up our list.
       IAST<IngredientsListType> curIngredient;
       for (int i=0; i<ingredients.size(); i++){
          curIngredient = ingredients.get(i);
          if (curIngredient.getType() == IngredientsListType.INGREDIENT){
             // Now get the actual ingredient from this child.
             MeasurementAndIngredient measAndIng =
                getMeasurementAndIngredient(curIngredient);
             if (measAndIng != null){
                ingList.addIngredient(measAndIng);
             }
          }
       }
       return ingList;
    }
 
    // A method for grabbing a MeasurementAndIngredient from a given child.
    private MeasurementAndIngredient getMeasurementAndIngredient(
       IAST<IngredientsListType> measurementAndIngredientChild){
       // Just for reference, here is what our tree is at this point:
       // - INGREDIENT
       // -- MEASUREMENT
       // --- AMOUNT
       // ---- 2
       // --- SPECIFIER (opt)
       // ---- ( (opt)
       // ----- MEASUREMENT
       // ------ AMOUNT
       // ------- NUMBER_OR_FRACTION
       // -------- 26
       // ------ UNIT
       // ------- ounce
       // ----- ) (opt)
       // --- UNIT (opt)
       // ---- jars
       // -- PRODUCT
       // --- WORDS
       // ---- spaghetti sauce
 
       // Build the MeasurementAndIngredient object we will return
       MeasurementAndIngredient measAndIng = new MeasurementAndIngredient();
       
       // Get the children of our current node
       List<IAST<IngredientsListType>> children = measurementAndIngredientChild.getChildren();
 
       boolean measurementHit = false;
       
       // Iterate through!
       IAST<IngredientsListType> curChild;
       for (int i=0; i<children.size(); i++){
          curChild = children.get(i);
          IngredientsListType curChildType = curChild.getType();
 
          if (curChildType == IngredientsListType.MEASUREMENT){
             if (!measurementHit){
                measAndIng.setMeasurement(getMeasurement(curChild));
                measurementHit = true;
             }
             else{
                measAndIng.setMeasurement2(getMeasurement(curChild));
             }
          }
          else if (curChildType == IngredientsListType.PRODUCT){
             Ingredient retIng = getIngredient(curChild);
             // It's possible it's null, if the "ingredient" is something
             // like "Directions" or "Ingredients"
             if (retIng != null){
                measAndIng.setIngredient(retIng);
             }else{
                // This likely isn't a real ingredient, so ignore it.
                return null;
             }
          }else if (curChildType == IngredientsListType.SPECIAL_DIRECTIONS){
             measAndIng.getIngredient().setSpecialDirections(getIngredientsListText(curChild));
          }
       }
 
       // Return our built MeasurementAndIngredient
       return measAndIng;
    }
 
    // A method for getting the measurement from a given child
    private Measurement getMeasurement(IAST<IngredientsListType> measurementChild){
       // At this point, our tree looks something like this:
       // - MEASUREMENT
       // -- AMOUNT
       // --- 2
       // -- SPECIFIER (opt)
       // --- ( (opt)
       // ---- MEASUREMENT
       // ----- AMOUNT
       // ------ NUMBER_OR_FRACTION
       // ------- 26
       // ----- UNIT
       // ------ ounce
       // ---- ) (opt)
       // -- UNIT (opt)
       // --- jars
 
       // Build the Measurement object we will return
       Measurement meas = new Measurement();
 
       // Get our children
       List<IAST<IngredientsListType>> children = measurementChild.getChildren();
 
       // With certain children being optional, it's likely best
       // to iterate through, dealing with each type we see instead
       // of making assumptions about positioning.
       
       IAST<IngredientsListType> curChild;
       for (int i=0; i<children.size(); i++){
          curChild = children.get(i);
          IngredientsListType curChildType = curChild.getType();
 
          if (curChildType == IngredientsListType.AMOUNT){
             meas.setAmount(getIngredientsListText(curChild));
          }
          else if (curChildType == IngredientsListType.SPECIFIER){
             meas.setSpecifier(getIngredientsListText(curChild));
          }
          else if (curChildType == IngredientsListType.UNIT){
             meas.setUnit(getIngredientsListText(curChild));
          }
       }
 
       // Return our built up Measurement
       return meas;
    }
 
    // A method for getting the product/ingredient from a child
    private Ingredient getIngredient(IAST<IngredientsListType> ingredientChild){
       // At this point, our tree looks something like this:
       // - PRODUCT
       // -- WORDS
       // --- spaghetti sauce
 
       // Build our ingredient object that we will return
       Ingredient ing = new Ingredient();
 
       // Get all of the children
       List<IAST<IngredientsListType>> children = ingredientChild.getChildren();
 
       // Iterate through the children
       IAST<IngredientsListType> curChild;
       for (int i=0; i<children.size(); i++){
          curChild = children.get(i);
          IngredientsListType curChildType = curChild.getType();
 
          if (curChildType == IngredientsListType.PRODUCT_WORDS_NO_BREAK){
             String curName = getIngredientsListText(curChild);
             if (shouldIgnoreIngredient(curName)){
                return null; // We don't want this so called ingredient.
             }
             ing.setName(curName);
          }
       }
       
       // Return our built ingredient
       return ing;
    }
 
    // A method to check if this "ingredient" matches something we want to
    // ignore.
    private boolean shouldIgnoreIngredient(String curIng){
       final String ingredientsPattern =
          "[I|i]ngriedient(s)?(:)? (List(:)?)?";
       final String directionsPattern =
          "[D|d]irection(s)?(:)?";
 
       Pattern pattern = Pattern.compile(ingredientsPattern);
       Matcher matcher = pattern.matcher(curIng);
       if (matcher.find()){
          return true; // We should ignore this "ingredient"
       }
       pattern = Pattern.compile(directionsPattern);
       matcher = pattern.matcher(curIng);
       if (matcher.find()){
          return true; // Again, we should ignore this.
       }
 
       // Ok, it seems legitimate. Let it through.
       return false;
    }
 }
