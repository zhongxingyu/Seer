 package functions;
 
 import backEnd.Model;
 
 public abstract class Function {
 
     private Model myModel;
     private int inputNum; 
 
     public Function (int num, Model model) {
         myModel = model;
         inputNum = num;
     }
     
     public abstract double execute (String[] input);
 
     public double getValue (String[] input) {
         if(!myModel.getMap().containsKey(input[1])) {
             return Double.parseDouble(input[1]);
         }
         return myModel.getMap().get(input[1]).execute(newArray(input, 1));
     }
     
     public double[] getValue (String[] input, int numVals) {
         double[] values = new double[numVals];
         String[] intermediate = input;
         for(int i = 0; i < numVals; i++) {
             values[i] = getValue(intermediate);
             intermediate = getIntermediate(intermediate);
         }
         return values;
     }
     
     public String[] getIntermediate (String[] input) {
         String[] intArray = null;
         for (int i = 0; i < input.length; i++) {
             if(!myModel.getMap().containsKey(input[i])) {
                 intArray = newArray(input, i + 1);
                 break;
             }
         }
         String[] result = new String[intArray.length + 1];
         result[0] = input[0];
         for(int j = 1; j < result.length; j++) {
             result[j] = intArray[j-1];
         }
         return result;
     }
     
     public String[] getOutput (String[] args) {
         String[] result = null;
         int count = 0;
         if(inputNum == 0) {
             return newArray(args, 1);
         }
         for(int i = 0; i < args.length; i++) {
            if(myModel.getMap().containsKey(args[i])) {
                 count -= myModel.getMap().get(args[i]).getArgs() - 1;
             }
             if(!myModel.getMap().containsKey(args[i])) {
                 count++;
                 if(count == inputNum) {
                     result = newArray(args, i + 1);
                     break;
                 }
             }
         }
         return result;
     }
 
     private String[] newArray (String[] array, int overlap) {
         String[] output = new String[array.length - overlap];
         for(int i = overlap; i < array.length; i++) {
             output[i-overlap] = array[i];
         }
         return output;
     }
     
     public int getArgs () {
         return inputNum;
     }
 }
