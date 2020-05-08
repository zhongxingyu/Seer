 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 //package redneural;
 
 import java.util.Vector;
 
 /**
  *
  * @author Gimbet
  */
 public class RedNeural 
 {
     double bias = 0.1;          
     int numIterations = 30;
     Vector<Double> errors = new Vector<Double>();// new double[numIterations]; 
	int numberOfWeights = example[0].length;
	double[] weights = new double[numberOfWeights];
	double[] deltaW = new double[numberOfWeights];
 	double k = 0; // Number of iterations
 	double error = 0;
 	double output = 0;
 	double target = 0;
      
     /* Create training examples
      * type: 0: AND, 1 : OR, 2: XOR
      * returns a two-dimension array
      * with 'number' of examples and
      * 3 positions regarding the binary expression and
      * the target
      */
      public boolean[][] createExamples(int type, int number)
      {
         boolean[][] example = new boolean[number][3];
         boolean bit = false;
          switch(type)
          {
              case 0:
                  for (int i = 0; i < 2; i++)
                  {
                     example[i][0] = bit;
                     example[i][1] = !bit;
                     example[i][2] = false;
                     bit = !bit;
                  }
                  for (int i = 2; i < 4; i++)
                  {
                     example[i][0] = bit;
                     example[i][1] = bit;
                     example[i][2] = bit && bit; //true
                     bit = !bit;
                  }
                  System.out.println((bit && !bit) + " " + example[0][2]);
                  break;
              case 1: 
                   for (int i= 0; i<2; i++){
                     example[i][0] = bit;
                     example[i][1] = !bit;
                     example[i][2] = (bit || !bit);
                     bit = !bit;
                  }
                  for (int i= 2; i<4; i++){
                     example[i][0] = bit;
                     example[i][1] = bit;
                     example[i][2] = (bit || bit);
                     bit = !bit;
                  }               
                  break;
              case 2:
                     for (int i= 0; i<2; i++){
                     example[i][0] = bit;
                     example[i][1] = !bit;
                     example[i][2] = true;
                     bit = !bit;
                  }
                  for (int i= 2; i<4; i++){
                     example[i][0] = bit;
                     example[i][1] = bit;
                     example[i][2] = false;
                     bit = !bit;
                  } 
                  break;
          }
          
         for (int i = 0; i < number; i++)
             for (int j = 0; j < 3; j++)
                System.out.println("example[" + i + "][" + j + "] = " + example[i][j]);
 
          return example;
      }
      
      public double toDoub(boolean b)
      {
          if (b) 
             return 1;
          else 
             return  0;         
      }
 
       public double toDoubTarg(boolean b)
      {
          if (b) 
             return 1;
          else 
             return  -1;         
      }
      
      public double calcOutput(double[] weight, boolean[] example)
      {
          double output = weight[0] * toDoub(example[0]);
          output += weight[1] * toDoub(example[1]);
          output += weight[2]; //W0
          if (output > 0)
              return 1;
          else 
              return -1; 
      }
 
 
 			// Perceptron
       void PLR(boolean[][] example, double n)
       {
       	int numberOfWeights = example[0].length;
         double[] weights = new double[numberOfWeights];
        	double[] deltaW = new double[numberOfWeights];
 				double b = 1.0;
 				double k = 0; // Number of iterations
           
 
        	for (int j = 0; j < numberOfWeights; j++)
         {
         	weights[j] = Math.random()/10;
           System.out.println("WEIGHTS "+ weights[j]);
         }
         double error = 0;
         do
         {
           error = 0;
         	for (int i = 0; i < example.length; i++)
           {		
           	double output = calcOutput(weights, example[i]);
             double target = toDoubTarg(example[i][2]);
             error += Math.pow( (target - output), 2);
       /*      System.out.println(" Ejemplo "+ i + "\n");
             System.out.println("Output : "+ output + "\n");
             System.out.println("Target : "+(example[i][2]) );
             System.out.println("Target - Output "+ (target - output) );						
             System.out.println("Error Cuadratico : "+ error);
             System.out.println ("Peso 1 " +weights[0]+ "  Peso 2 "+weights[1] + " Peso 3 " +weights[2]+  "\n");
 						*/
             for (int j = 0; j < numberOfWeights -1; j++) 
            	{
             	deltaW[j] = n * ( target - output) * toDoub(example[i][j]);
              	weights[j] += deltaW[j];
             //  System.out.println("delta: " + deltaW[j]);
            	}
             deltaW[2] = n * (target - output);
             weights[2] += deltaW[2];  				
 					                
           }
           error /= example.length;
 
           k++;
           errors.add(error);  
           System.out.println("\n");
         }
         while (error != 0 && k < 100);
          System.out.println("Iteration k: " + k);
       }
 
 
       // Delta Learning Rule
       // Batch Mode
       void DLRB(boolean[][] example, double n)
       {       

         for (int j = 0; j < numberOfWeights; j++)
         {
           weights[j] = 0;
           deltaW[j] = 0;
         }
 
         do{
         
           // Initialize DeltaW to zero
           for (int j = 0; j< numberOfWeights; j++){
             deltaW[j] = 0;
           }
           error = 0;
           //For each example
           for (int i = 0; i< example.length; i++){
             //Compute the output
             output = (calcOutput(weights, example[i]));
             target = toDoubTarg(example[i][2]);
             error += Math.pow( (target - output), 2);
             
             //For each unit weight, do
             for (int j = 0; j < numberOfWeights -1; j++){
               deltaW[j] += (target - output) * toDoub(example[i][j]); 
             }
             deltaW[2] +=  (target - output);
           }
 
           //Then, update each weight
           for (int i = 0; i< numberOfWeights - 1; i++){
             weights[i] += n * deltaW[i];
           }
             weights[2] += n * deltaW[2];
 
           // Verifying stop criterion          
 
           error /= example.length; 
           errors.add(error);
           k++;
         } while(error != 0 && k < 10);
         System.out.println ("Peso 1 " +weights[0]+ "  Peso 2 "+weights[1] + " Peso 3 " +weights[2]+  "\n");
       }
 
       //Delta Learning Rule
       //Incremental Mode
       void DLRI(boolean[][] example, double n){
         PLR(example,n);
       }
 
       // ADALINE
       void ADALINE(boolean[][] example, double n){
        for (int j = 0; j < numberOfWeights; j++)
         {
           weights[j] = 0;
           deltaW[j] = 0;
         }
         do{
           error = 0;
           for (int j = 0; j < numberOfWeights; j++)
           {
          
             deltaW[j] = 0;
           }
           for (int i= 0; i<example.length; i++){
             double output = calcOutput(weights,example[i]);
             double target = toDoubTarg(example[i][2]);
             error += Math.pow((target - output),2);
             for (int j = 0; j<numberOfWeights-1; j++){
               deltaW[j] += n * (target - output)* toDoub(example[i][j]); 
             }
             deltaW[2] +=   n * (target - output);
           }
           for (int j = 0; j<numberOfWeights-1; j++){
               weights[j] += deltaW[j]; 
           } 
           weights[2] += deltaW[2];
           k++;
           error /= example.length;
           errors.add(error);
 
         }while(error != 0 && k < 10);
         System.out.println ("Peso 1 " +weights[0]+ "  Peso 2 "+weights[1] + " Peso 3 " +weights[2]+  "\n");
       }
 
 //Truncate
 private static double truncate(double x){
   if ( x > 0 )
     return Math.floor(x * 100)/100;
   else
     return Math.ceil(x * 100)/100;
 }
    
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
       // TODO code application logic here
     	RedNeural rn = new RedNeural();
  
       rn.DLRB(rn.createExamples(0, 4), 0.1);
          
       System.out.println("Printing chart");
       XYChart xychart = new XYChart();
       
       double[] elCosoEste = new double[rn.errors.size()];
       
       for (int i = 0; i < rn.errors.size() ; i++)
         elCosoEste[i] = rn.errors.get(i);
       
       xychart.getChart(elCosoEste);
       
     }
 }
