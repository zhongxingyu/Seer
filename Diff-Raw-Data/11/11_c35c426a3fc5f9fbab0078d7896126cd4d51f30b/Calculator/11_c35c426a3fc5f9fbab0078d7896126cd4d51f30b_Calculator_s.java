 package hygeia;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.Scanner;
 
 
 
 public class Calculator {
 	
 // Declarations
 double f = 0.5;
  
 public static double percentBodyFat(String sex, String w, double hips, 
 		double waist, double height, String wr) 
   throws FileNotFoundException
 {
  // Declarations
  double weight, doubRes;
  double constantA = 0;
  double constantB = 0;
  double constantC = 0;
  double percentBodyFat = 0;
  String result;
  boolean flag =true;
 //Declarations
  double f = 0.5;
  
  /*
   * If user is female
   */
  if(sex.equals("f"))
  {
   Scanner hipConst = new Scanner(new BufferedReader(
     new FileReader("/home/anne/www/WEB-INF/src/hygeia/femaleTable.txt")));
   Scanner hConst = new Scanner(new BufferedReader(
     new FileReader("/home/anne/www/WEB-INF/src/hygeia/femaleTable.txt")));
   Scanner abdConst = new Scanner(new BufferedReader(
     new FileReader("/home/anne/www/WEB-INF/src/hygeia/femaleTable.txt")));
 
   weight = Double.parseDouble(w);
   
   /*
    * Round hips measurements
    */
   hips = f* Math.round(hips/f);
   System.out.println("hips:" + hips);
   
   /*
    * Round waist measurement
    */
   waist = f* Math.round(waist/f);
   System.out.println("Waist: "+ waist);
 
   /* 
    * Find values in database
    */
   
   result = hipConst.next();
   
   while(!hipConst.hasNextDouble())
   {
    hipConst.nextLine();
    result = hipConst.next();
   }
   
   while((result != null) && flag)
   {
    doubRes = Double.parseDouble(result);
    
    if(doubRes == hips)
    {
     flag = false;
     result = hipConst.next();
     constantA = Double.parseDouble(result);
     System.out.println("ConstantA: " + constantA);
     
    }
    else
    {
     hipConst.nextLine();
     result = hipConst.next();
   
    } 
   }
   
   flag = true;
   abdConst.next();   
   abdConst.next();
   result = abdConst.next();
   
   while(!abdConst.hasNextDouble())
   {
    abdConst.nextLine();
    abdConst.next();
    abdConst.next();
    result = abdConst.next();
   }
   
   while((result != null) && flag)
   {
    doubRes = Double.parseDouble(result);
    
    if(doubRes == waist)
    {
     flag = false;
     result = abdConst.next();
     constantB = Double.parseDouble(result);
     System.out.println("ConstantB: " + constantB);
     
    }
    else
    {
     abdConst.nextLine();
     abdConst.next();   
     abdConst.next();
     result = abdConst.next();
   
    } 
   }
   
   flag = true;
   hConst.next();   
   hConst.next();
   hConst.next();
   hConst.next();
   result = hConst.next();
   
   while(!hConst.hasNextDouble())
   {
    hConst.nextLine();
    hConst.next();
    hConst.next();
    hConst.next();
    hConst.next();
    result = hConst.next();
   }
   
   while((result != null) && flag)
   {
    doubRes = Double.parseDouble(result);
    
    if(doubRes == height)
    {
     flag = false;
     result = hConst.next();
     constantC = Double.parseDouble(result);
     System.out.println("ConstantC: " + constantC);
     
    }
    else
    {
     hConst.nextLine();
     hConst.next();
     hConst.next();
     hConst.next();
     hConst.next();
     result = hConst.next();
   
    } 
   }
   
   /*
    * Calculate percent body fat
    */
   
   percentBodyFat = ((constantA + constantB) - constantC)/100;
   
   
  }
   
  if(sex.equals("m"))
  {
   Scanner table = new Scanner(new BufferedReader(
     new FileReader("/home/anne/www/WEB-INF/src/hygeia/maleTable.txt")));
   double wrist, ww;
   
    weight = Double.parseDouble(w);
   
    /*
     * Get 3 Abdomen measurements and take the average
     */
    waist = f* Math.round(waist/f);
    System.out.println("Waist: "+waist);
    
          // wrist measurement
    wrist = Double.parseDouble(wr);
    System.out.println(wrist);
    
    /*
     * Wrist - Waist value used for table look up 
     */
    
    ww = waist - wrist;
    
    /*
     * Find values in table
     */
    
    int col = 1;
 
    result = table.next();
 
    while(Double.parseDouble(result) != ww)
    {
     result = table.next(); 
     col++;
    }
    
    System.out.println("Waist - Wrist:  " + result);
    table.nextLine();
    result = table.next();
    
    while(Double.parseDouble(result) != weight)
    {
     table.nextLine();
     result = table.next();
    }
    
    System.out.println("Weight:  " + result);
    
    for(int i = 0; i < col; i++)
    {
     result = table.next();
     percentBodyFat = (Double.parseDouble(result))/100;
    }
    
    System.out.println(percentBodyFat + "% body fat");
    
  }
  return percentBodyFat;
 }
 
 public static double leanBodyMass(double weight, double percentBodyFat)
 {
   // Declaration
   double leanBodyMass;
   double bodyFat = 0;
 //Declarations
   double f = 0.5;
   
   /*
    * Calculate Lean Body Mass
    */
   bodyFat = weight * percentBodyFat;
   leanBodyMass = weight - bodyFat;
   leanBodyMass = f* Math.round(leanBodyMass/f);
   System.out.println(leanBodyMass + " is your lean body" +
     " mass.");
  
   return leanBodyMass;
  
 }
 
 public static double protein(double leanBodyMass, int activLevel)
{  
 	double act = 0;
 	double protein;
 	
 	if(activLevel == 1)
 	{
 		act = 0.5;
 	}
 	if(activLevel == 2)
 	{
 		act = 0.6;
 	}
 	if(activLevel == 3)
 	{
 		act = 0.7;
 	}
 	if(activLevel == 4)
 	{
 		act = 0.8;
 	}
 	if(activLevel == 5)
 	{
 		act = 0.9;
 	}
 	if(activLevel == 6)
 	{
 		act = 1.0;
 	}
 	
   /*
    * Calculate protein requirement
    */
   
   protein = leanBodyMass * act;
   protein = f* Math.round(protein/f);
   
   System.out.println(protein + " is you daily protein requirement.");
   
   return protein;
 }
 
 }
