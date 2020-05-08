 package storage;
 
 import internalformatting.Formula;
 import internalformatting.Operator;
 import internalformatting.Term;
 import internalformatting.Unit;
 import internalformatting.Variable;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 //import userinterface.CLI;
 
 /**
  * This class saves and loads all of our data objects through the process of serialization
  * 
  * @author Clayven Anderson
  *@author May Camp (made ALL the formulas stored)
  *
  */
 
 
 public class Saver
 {
   
   /**
    * Serializes a FormulaDatabase object full for formulas to be used later use by the system
    * @param Forms a FormulaDatabase object that is to be serialized
    */
 	public static void saveForms(FormulaDatabase Forms){
 		try{
 			File dir = new File("data");
 			if(dir.exists()== false){
         dir.mkdir();
      }
 			File store = new File("data/" ,"FormulaDatabase.ntb" );
 			FileOutputStream fs = new FileOutputStream(store);
 			ObjectOutputStream os = new ObjectOutputStream(fs);
 
 			os.writeObject(Forms);
 			os.close();
 
 		}catch (IOException ex){
 			ex.printStackTrace();
 		}      
 	}
 /**
  * Deserializes the FormulaDatabase object to recover our formulas
  * @return returns a Deserialized FormulaDatabase object
  */
 	public static  FormulaDatabase loadForms(){ //throws IOException{
 		FormulaDatabase Database = new FormulaDatabase(); 
 		try{ //looks for an existing ntb file
 			ObjectInputStream is = new ObjectInputStream(new FileInputStream("data/FormulaDatabase.ntb"));
 			Database = (FormulaDatabase) is.readObject();
 
 		} catch(Exception ex){
 	  System.out.println("generating default formula database");//if ntb isn't found; it creates a new one with the main
 		  try
       {
         generateFormData();
         ObjectInputStream is = new ObjectInputStream(new FileInputStream("data/FormulaDatabase.ntb"));
         try
         {
           Database = (FormulaDatabase) is.readObject();
         }
         catch (ClassNotFoundException e)
         {
           // TODO Auto-generated catch block
           e.printStackTrace();
         }
       }
       catch (IOException e)
       {
         // TODO Auto-generated catch block
         e.printStackTrace();
       }
 		  //CLI.main(null);
 		}
 
 		return Database;
 	}
 
 	 /**
    * Serializes a FormulaDatabase object full for formulas to be used later use by the system
    * @param Forms a FormulaDatabase object that is to be serialized
    */
   public static void saveVars(VariableDatabase Vars){
     try{
       File dir = new File("data");
       if(dir.exists()== false){
          dir.mkdir();
       }
       File store = new File("data/" ,"VariableDatabase.ntb" );
       FileOutputStream fs = new FileOutputStream(store);
       ObjectOutputStream os = new ObjectOutputStream(fs);
 
       os.writeObject(Vars);
       os.close();
 
     }catch (IOException ex){
       ex.printStackTrace();
     }      
   }
 /**
  * Deserializes the FormulaDatabase object to recover our formulas
  * @return returns a Deserialized FormulaDatabase object
  */
   public static  VariableDatabase loadVars(){ //throws IOException{
     VariableDatabase Database = new VariableDatabase(); 
     try{ //looks for an existing ntb file
       ObjectInputStream is = new ObjectInputStream(new FileInputStream("data/VariableDatabase.ntb"));
       Database = (VariableDatabase) is.readObject();
 
     } catch(Exception ex){
      System.out.println("generating default variable database");//if ntb isn't found; it creates a new one with the main
       try
       {
         generateVarData();
         ObjectInputStream is = new ObjectInputStream(new FileInputStream("data/VariableDatabase.ntb"));
         try
         {
           Database = (VariableDatabase) is.readObject();
         }
         catch (ClassNotFoundException e)
         {
           // TODO Auto-generated catch block
           e.printStackTrace();
         }
       }
       catch (IOException e)
       {
         // TODO Auto-generated catch block
         e.printStackTrace();
       }
       //CLI.main(null);
     }
 
     return Database;
   }
 	
   
   /**
    * Serializes a UnitDatabase object
    * @param Units a UnitDatabase object
    */
   public static void saveUnits(UnitDatabase Units){
     try{
       File dir = new File("data");
       if(dir.exists()== false){
          dir.mkdir();
       }
       File store = new File("data/" ,"UnitDatabase.ntb" );
       FileOutputStream fs = new FileOutputStream(store);
       ObjectOutputStream os = new ObjectOutputStream(fs);
 
       os.writeObject(Units);
       os.close();
 
     }catch (IOException ex){
       ex.printStackTrace();
     }      
   }
 /**
  * Deserializes the UnitDatabase object to recover our Units
  * @return returns a Deserialized UnitDatabase object
  */
   public static  UnitDatabase loadUnits(){ //throws IOException{
     UnitDatabase Database = new UnitDatabase(); 
     try{ //looks for an existing ntb file
      ObjectInputStream is = new ObjectInputStream(new FileInputStream("data/VariableDatabase.ntb"));
       Database = (UnitDatabase) is.readObject();
 
     } catch(Exception ex){
      System.out.println("generating default unit database");//if ntb isn't found; it creates a new one with the main
       try
       {
         generateUnitData();
         ObjectInputStream is = new ObjectInputStream(new FileInputStream("data/UnitDatabase.ntb"));
         try
         {
           Database = (UnitDatabase) is.readObject();
         }
         catch (ClassNotFoundException e)
         {
           // TODO Auto-generated catch block
           e.printStackTrace();
         }
       }
       catch (IOException e)
       {
         // TODO Auto-generated catch block
         e.printStackTrace();
       }
       //CLI.main(null);
     }
 
     return Database;
   }
   
   
   /**
    * Serializes a FormulaSheet object full for formulas to be used later use by the user
    * @param Forms a FormulaSheet object that is to be serialized
    */
   public static void saveSheet(FormulaSheet sheet){
     try{
       File dir = new File("data");
       if(dir.exists()== false){
         dir.mkdir();
      }
       File store = new File("data/" , sheet.getName()+".ntb" );
       FileOutputStream fs = new FileOutputStream(store);
       ObjectOutputStream os = new ObjectOutputStream(fs);
 
       os.writeObject(sheet);
       os.close();
 
     }catch (IOException ex){
       ex.printStackTrace();
     }      
   }
 /**
  * Deserializes a FormulaSheet object to recover a user generated formula sheet
  * @return returns a Deserialized FormulaSheet object
  */
   public static  FormulaSheet loadSheet(String name){ //throws IOException{
     FormulaSheet sheet = new FormulaSheet(); 
     try{ //looks for an existing ntb file
       ObjectInputStream is = new ObjectInputStream(new FileInputStream("data/" + name +".ntb"));
       sheet = (FormulaSheet) is.readObject();
 
     } catch(Exception ex){
     System.out.println("formula sheet not found");//if ntb isn't found;
     }
      
     return sheet;
   }
   
   /**
    * generates a new VariableDatabase in case on is not found
    * @throws IOException
    */
 	public static void generateVarData() throws IOException{
 	  
 	  Variable v_av = new Variable("v_(av)");
     Variable x_1 = new Variable("x_(1)");
     Variable x_2 = new Variable("x_(2)");
     Variable t_1 = new Variable("t_(1)");
     Variable t_2 = new Variable("t_(2)");
     
     Variable x = new Variable("x");
     Variable x_0 = new Variable("x_0");
     Variable v_0 = new Variable("v_0");
     Variable t = new Variable("t");
     Variable a = new Variable("a");
     Variable v = new Variable("v");
     
     Variable F = new Variable("F");
     Variable m = new Variable("m");
     
     Variable w = new Variable("w");
     Variable g = new Variable("g");
     
     VariableDatabase VBase = new VariableDatabase();
     
     VBase.addVariable(v_av);
     VBase.addVariable(x_1);
     VBase.addVariable(x_2);
     VBase.addVariable(t_1);
     VBase.addVariable(t_2);
     VBase.addVariable(x);
     VBase.addVariable(x_0);
     VBase.addVariable(v_0);
     VBase.addVariable(t);
     VBase.addVariable(a);
     VBase.addVariable(v);
     VBase.addVariable(F);
     VBase.addVariable(m);
     VBase.addVariable(w);
     VBase.addVariable(g);
     
     saveVars(VBase);
 	}
 	
 	/**
 	 * generates a default unit Database in case on is not found
 	 * @throws IOException
 	 */
 	public static void generateUnitData() throws IOException{
 	  
 ////new units
    Unit gram = new Unit("gram");
    Unit kilogram = new Unit("kilogram");
    Unit meter = new Unit("meter");
    Unit liter = new Unit("liter");
    Unit second = new Unit("second");
    Unit meterPerSecond = new Unit("meter per second");
    Unit meterPerSecondSquared = new Unit("meter per second squared");
    Unit secondSquared = new Unit("second squared");
    Unit newton = new Unit("newton");
    
    //unit - gram
    gram.setInfo("The units of mass.");
    gram.setTypicalForm("g");
    
    //unit - kilogram
    kilogram.setInfo("The units of mass.");
    kilogram.setTypicalForm("kg");
    
    //unit - meter
    meter.setInfo("The units of length.");
    meter.setTypicalForm("m");
    
    //unit - liter
    liter.setInfo("The units of volume.");
    liter.setTypicalForm("l");
    
    //unit - second
    second.setInfo("The units of time.");
    second.setTypicalForm("s");
    
    //unit - meter per second
    meterPerSecond.setInfo("The units of velocity.");
    meterPerSecond.setTypicalForm("m/s");
    
    //unit - meter per second squared
    meterPerSecondSquared.setInfo("The units of acceleration.");
    meterPerSecondSquared.setTypicalForm("m/s^2");
    
    //unit - second squared
    secondSquared.setInfo("The units of time squared.");
    secondSquared.setTypicalForm("s^2");
    
    //unit - newton
    newton.setInfo("The units of force. Can also be written as kg * m / s^2");
    newton.setTypicalForm("N");
 	  
    UnitDatabase UBase = new UnitDatabase();
    
    UBase.addUnit(gram);
    UBase.addUnit(kilogram);
    UBase.addUnit(liter);
    UBase.addUnit(meter);
    UBase.addUnit(meterPerSecond);
    UBase.addUnit(meterPerSecondSquared);
    UBase.addUnit(secondSquared);
    UBase.addUnit(newton);
    
    saveUnits(UBase);
 	}
 	
 	/**
 	 * generate a default formulaDatabase in case one is not found
 	 * @throws IOException
 	 */
 	public static void generateFormData() throws IOException{
 
 		////May's default formulas
 		// new formula ArrayList for all formulas and stuff
 
 		////all operators
 		Operator leftParen = new Operator("(");
 		Operator rightParen = new Operator(")");
 		Operator plus = new Operator("+");
 		Operator minus = new Operator("-");
 		Operator times = new Operator("*");
 		Operator divide = new Operator("/");
 		Operator equals = new Operator("=");
 		
 		//// new units
 		Unit gram = new Unit("gram");
 		Unit kilogram = new Unit("kilogram");
 		Unit meter = new Unit("meter");
 		Unit liter = new Unit("liter");
 		Unit second = new Unit("second");
 		Unit meterPerSecond = new Unit("meter per second");
 		Unit meterPerSecondSquared = new Unit("meter per second squared");
 		Unit secondSquared = new Unit("second squared");
 		Unit newton = new Unit("newton");
 		
 		//unit - gram
 		gram.setInfo("The units of mass.");
 		gram.setTypicalForm("g");
 		
 		//unit - kilogram
 		kilogram.setInfo("The units of mass.");
 		kilogram.setTypicalForm("kg");
 		
 		//unit - meter
 		meter.setInfo("The units of length.");
 		meter.setTypicalForm("m");
 		
 		//unit - liter
 		liter.setInfo("The units of volume.");
 		liter.setTypicalForm("l");
 		
 		//unit - second
 		second.setInfo("The units of time.");
 		second.setTypicalForm("s");
 		
 		//unit - meter per second
 		meterPerSecond.setInfo("The units of velocity.");
 		meterPerSecond.setTypicalForm("m/s");
 		
 		//unit - meter per second squared
 		meterPerSecondSquared.setInfo("The units of acceleration.");
 		meterPerSecondSquared.setTypicalForm("m/s^2");
 		
 		//unit - second squared
 		secondSquared.setInfo("The units of time squared.");
 		secondSquared.setTypicalForm("s^2");
 		
 		//unit - newton
 		newton.setInfo("The units of force. Can also be written as kg * m / s^2");
 		newton.setTypicalForm("N");
 		
 		
 		////variables
 		Variable v_av = new Variable("v_(av)");
 		Variable x_1 = new Variable("x_(1)");
 		Variable x_2 = new Variable("x_(2)");
 		Variable t_1 = new Variable("t_(1)");
 		Variable t_2 = new Variable("t_(2)");
 		
 		Variable x = new Variable("x");
 		Variable x_0 = new Variable("x_0");
 		Variable v_0 = new Variable("v_0");
 		Variable t = new Variable("t");
 		Variable a = new Variable("a");
 		Variable v = new Variable("v");
 		
 		Variable F = new Variable("F");
 		Variable m = new Variable("m");
 		
 		Variable w = new Variable("w");
 		Variable g = new Variable("g");
 
 		//term - average velocity
 		Term avVelTerm_v_av = new Term(1, v_av, 1, meterPerSecond);
 		Term avVelTerm_x_1 = new Term(1, x_1, 1, meter);
 		Term avVelTerm_x_2 = new Term(1, x_2, 1, meter);
 		Term avVelTerm_t_1 = new Term(1, t_1, 1, second);
 		Term avVelTerm_t_2 = new Term(1, t_2, 1, second);
 		
 		//term - position at constant acceleration
 		Term posTerm_x = new Term(1, x, 1, meter);
 		Term posTerm_x_0 = new Term(1, x_0, 1, meter);
 		Term posTerm_v_0 = new Term(1, v_0, 1, meterPerSecond);
 		Term posTerm_t = new Term(1, t, 1, second);
 		Term posTerm_0_5a = new Term(0.5, a, 1, meterPerSecondSquared);
 		Term posTerm_t2 = new Term(1, t, 2, secondSquared);
 		
 		//term - velocity at constant acceleration
 		Term velTerm_v = new Term(1, v, 1, meterPerSecond);
 		Term velTerm_v_0 = new Term(1, v_0, 1, meterPerSecond);
 		Term velTerm_a = new Term(1, a, 1, meterPerSecondSquared);
 		Term velTerm_t = new Term(1, t, 1, second);
 		
 		//term - Newton's second law of motion
 		Term newt2ndLawTerm_F = new Term(1, F, 1, newton);
 		Term newt2ndLawTerm_m = new Term(1, m, 1, kilogram);
 		Term new2ndLawTerm_a = new Term(1, a, 1, meterPerSecond);
 		
 		//term - weight
 		Term weightTerm_w = new Term(1, w, 1, newton);
 		Term weightTerm_m = new Term(1, m, 1, kilogram);
 		Term weightTerm_g = new Term(1, g, 1, meterPerSecond);
 
 		////new formulas to put in ArrayList of formulas
 		Formula avVelForm = new Formula();
 		Formula posForm = new Formula();
 		Formula velForm = new Formula();
 		Formula newt2ndLawForm = new Formula();
 		Formula weightForm = new Formula();
 
 		//formula - average velocity
 		//v_av = ( x_2 - x_1 ) / ( t_2 - t_1 )
 		avVelForm.setName("Average Velocity");
 		avVelForm.setInfo("Average velocity of a particle during a certain time period.");
 		avVelForm.addTerm(avVelTerm_v_av);
 		avVelForm.add(equals);
 		avVelForm.add(leftParen);
 		avVelForm.addTerm(avVelTerm_x_2);
 		avVelForm.add(minus);
 		avVelForm.addTerm(avVelTerm_x_1);
 		avVelForm.add(rightParen);
 		avVelForm.add(divide);
 		avVelForm.add(leftParen);
 		avVelForm.addTerm(avVelTerm_t_2);
 		avVelForm.add(minus);
 		avVelForm.addTerm(avVelTerm_t_1);
 		avVelForm.add(rightParen);
 		
 		//formula - position with constant acceleration
 		//x = x_0 + v_0 * t + 0.5a * t^2
 		posForm.setName("Position with Constant Acceleration");
 		posForm.setInfo("Position of a particle at a certain time t, given initial velocity, constant acceleration, and time.");
 		posForm.addTerm(posTerm_x);
 		posForm.add(equals);
 		posForm.addTerm(posTerm_x_0);
 		posForm.add(plus);
 		posForm.addTerm(posTerm_v_0);
 		posForm.add(times);
 		posForm.addTerm(posTerm_t);
 		posForm.add(plus);
 		posForm.addTerm(posTerm_0_5a);
 		posForm.add(times);
 		posForm.addTerm(posTerm_t2);
 		
 		//formula - velocity with constant acceleration
 		// v = v_0 + a * t
 		velForm.setName("Velocity with Constant Acceleration");
 		velForm.setInfo("Velocity of a particle at a certain time t, given initial velocity, constant acceleration, and time.");
 		velForm.addTerm(velTerm_v);
 		velForm.add(equals);
 		velForm.addTerm(velTerm_v_0);
 		velForm.add(plus);
 		velForm.addTerm(velTerm_a);
 		velForm.add(times);
 		velForm.addTerm(velTerm_t);
 		
 		//formula - Newton's second law of motion
 		// F = m * a
 		newt2ndLawForm.setName("Newton's Second Law of Motion");
 		newt2ndLawForm.setInfo("Force at a particular mass and acceleration. If a net external force acts on a body, the body accelerates. The direction of aceleration is the same as the direction of the net force. The mass of the body times the acceleration of the body equals the net force vector.");
 		newt2ndLawForm.addTerm(newt2ndLawTerm_F);
 		newt2ndLawForm.add(equals);
 		newt2ndLawForm.addTerm(newt2ndLawTerm_m);
 		newt2ndLawForm.add(times);
 		newt2ndLawForm.addTerm(new2ndLawTerm_a);
 		
 		//formula - weight
 		weightForm.setName("weight");
 		weightForm.setInfo("weight of a particular mass and gravity");
 		weightForm.addTerm(weightTerm_w);
 		weightForm.add(equals);
 		weightForm.addTerm(weightTerm_m);
 		weightForm.add(times);
 		weightForm.addTerm(weightTerm_g);
 		
 		////formula tags - average velocity
 		avVelForm.addTag("average");
 		avVelForm.addTag("velocity");
 		avVelForm.addTag("x_1");
 		avVelForm.addTag("x_2");
 		avVelForm.addTag("t_1");
 		avVelForm.addTag("t_2");
 		avVelForm.addTag("x");
 		avVelForm.addTag("t");
 		avVelForm.addTag("meter");
 		avVelForm.addTag("meters");
 		avVelForm.addTag("per");
 		avVelForm.addTag("second");
 		
 		//formula tags - position with const acceleration
 		posForm.addTag("position");
 		posForm.addTag("constant");
 		posForm.addTag("acceleration");
 		posForm.addTag("initial");
 		posForm.addTag("velocity");
 		posForm.addTag("time");
 		posForm.addTag("squared");
 		posForm.addTag("meter");
 		posForm.addTag("meters");
 		
 		//formula tags - velocity with constant acceleration
 		velForm.addTag("velocity");
 		velForm.addTag("constant");
 		velForm.addTag("acceleration");
 		velForm.addTag("time");
 		velForm.addTag("initial");
 		velForm.addTag("meter");
 		velForm.addTag("per");
 		velForm.addTag("second");
 		
 		//formula tags - Newton's second law of motion
 		newt2ndLawForm.addTag("newton");
 		newt2ndLawForm.addTag("second");
 		newt2ndLawForm.addTag("2nd");
 		newt2ndLawForm.addTag("law");
 		newt2ndLawForm.addTag("motion");
 		newt2ndLawForm.addTag("force");
 		newt2ndLawForm.addTag("mass");
 		newt2ndLawForm.addTag("acceleration");
 		newt2ndLawForm.addTag("kilograms");
 		newt2ndLawForm.addTag("meters");
 		newt2ndLawForm.addTag("squared");
 		
 		//formula tags - weight
 		weightForm.addTag("weight");
 		weightForm.addTag("mass");
 		weightForm.addTag("gravity");
 		weightForm.addTag("newton");
 		weightForm.addTag("kilogram");
 		weightForm.addTag("meter");
 		weightForm.addTag("second");
 		weightForm.addTag("squared");
 
 		FormulaDatabase Base = new FormulaDatabase();
 		// FormulaDatabase Res = new FormulaDatabase();
 
 		Base.addFormula(avVelForm);
 		Base.addFormula(posForm);
 		Base.addFormula(velForm);
 		Base.addFormula(newt2ndLawForm);
 
 		saveForms(Base);
 
 		Base = null;
 
 		//Res = (FormulaDatabase) LoadForms();
 
 		//for(int i=0; i<someFormulas.size(); i++){
 		//	Formula Res = ((FormulaDatabase) loadForms()).get(i);
 		//	System.out.println(Res.getName() + " " + Res + " " + Res.getInfo() + " " + Res.getAllTags() );
 		//}
 	}  
 
 	public static void main(String args[]){
 	  loadVars();
 	  loadForms();
 	  loadUnits();
 	  loadSheet("doesnt exist");
 	  
 	}
 }
