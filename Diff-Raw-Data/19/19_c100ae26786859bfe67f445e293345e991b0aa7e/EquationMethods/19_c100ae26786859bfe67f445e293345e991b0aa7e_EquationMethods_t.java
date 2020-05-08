 package mx.udlap.equations;
 
 import android.annotation.SuppressLint;
 
 @SuppressLint("DefaultLocale")
 public class EquationMethods {
 	
 	public String solver(String equation){
         //Finding out the roots
         String answer = "";
         int a=0,b=0,c=0;
         Integer[] results;
         
         try {
             results = parsePolynomial(equation);
             a = results[0];
             b = results[1];
             c = results[2];
         } 
         catch (NumberFormatException e) {
             System.out.println("    Result: Malformed input polynomial");
         }
         
         double temp1 = Math.sqrt(b * b - 4 * a * c);
         double root1 = (-b +  temp1) / (2*a) ;
         double root2 = (-b -  temp1) / (2*a) ;
         if (temp1 >= 0.0){
             answer = Double.toString(root1) + Double.toString(root2);
         }
         else {
             answer = "The roots are not real numbers.";
         }
         return answer;
     }
 	
 	public String getX1X2(String equation){
 		String answer ="";
 		int a=0,b=0,c=0;
         Integer[] results;
         
         try {
             results = parsePolynomial(equation);
             a = results[0];
             b = results[1];
             c = results[2];
         } 
         catch (NumberFormatException e) {
             System.out.println("    Result: Malformed input polynomial");
         }
         
         double temp1 = Math.sqrt(b * b - 4 * a * c);
         double root1 = (-b +  temp1) / (2*a) ;
         double root2 = (-b -  temp1) / (2*a) ;
         if (temp1 >= 0.0){
             answer = "X1 = "+Double.toString(root1)+" / X2 = "+ Double.toString(root2);
         }
         else {
             answer = "The roots are not real numbers.";
         }
         return answer;
 	}
 	
 	public int getA(String equation){
         //Finding out the roots
         int a=0;
         Integer[] results;
         
         try {
             results = parsePolynomial(equation);
             a = results[0];
             return a;
         } 
         catch (NumberFormatException e) {
             System.out.println("    Result: Malformed input polynomial");
         } 
         return a;
     }
 	
 	public int getB(String equation){
         //Finding out the roots
         int b=0;
         Integer[] results;
         
         try {
             results = parsePolynomial(equation);
             b = results[1];
         } 
         catch (NumberFormatException e) {
             System.out.println("    Result: Malformed input polynomial");
         } 
         return b;
     }
 	
 	public int getC(String equation){
         //Finding out the roots
         int c=0;
         Integer[] results;
         
         try {
             results = parsePolynomial(equation);
             c = results[2];
         } 
         catch (NumberFormatException e) {
             System.out.println("    Result: Malformed input polynomial");
         } 
         return c;
     }
 	
 	static Integer[] parsePolynomial(String input) {
         if(input.indexOf("x") == -1) throw new NumberFormatException();
         if(input.indexOf("=") == -1) throw new NumberFormatException();
         Integer[] result = new Integer[10];
         //remove all whitespaces
         input = input.replaceAll(" ", "");
         String[] splitEquals = input.split("=");
         String beforeEquals;
         if(splitEquals.length == 2 ){
             String constantAfterEquals;
 
             if(splitEquals[0].indexOf("x") >= 0) {
                 constantAfterEquals = splitEquals[1];
                 beforeEquals = splitEquals[0];
             }
             else {
                 constantAfterEquals = splitEquals[0];
                 beforeEquals = splitEquals[1];
             }
                 result[2] = new Integer(constantAfterEquals);
         }
         else beforeEquals = input;
         //replace all the '-' by '+-' so we can now split everything using '+' while keeping the '-' intact
         beforeEquals = beforeEquals.replaceAll("-", "+-");
         //If there is a leading '+' remove it
         if(beforeEquals.indexOf("+") == 0) beforeEquals = beforeEquals.replaceFirst("\\+", "");
         String[] splitPlus = beforeEquals.split("\\+");
         if(splitPlus.length < 1 || (splitPlus.length == 1 && splitPlus[0].indexOf('x') == -1)) throw new NumberFormatException();
             for(int i = 0; i < splitPlus.length; i++) {
                 if(splitPlus[i].indexOf("x^2") >= 0) {
                 	if (splitPlus[i].indexOf("x^2") == 0){
                         //System.out.println("entro1");
                         result[0] = 1;
                      }
                 	 
                 	else{
                         String[] splitX2 = splitPlus[i].split("x");
                         if(splitX2.length != 2) throw new NumberFormatException();
                         result[0] = new Integer(splitX2[0]);
                 	}
                 }
                 else if(splitPlus[i].indexOf("x^1") >= 0) {
                     String[] splitX = splitPlus[i].split("x");
                     if(splitX.length != 2) throw new NumberFormatException();
                     result[1] = new Integer(splitX[0]);
                 }
                 else if(splitPlus[i].indexOf("x") >= 0) {
                     String[] splitX = splitPlus[i].split("x");
                     if(splitX.length != 1) throw new NumberFormatException();
                     result[1] = new Integer(splitX[0]);
                 }
                 else {
                     result[2] = (new Integer(splitPlus[i]) - result[2]);
                 }
         } 
         return result;
     }
 	
	public double division (double a, double b){
		
		double bi = a/b;
		return bi;
		
	}
	
	
	
 	public String PasoaPaso (int x, int y, int z, String ecuacion) {
         int a = x;
         int b = y;
         int c = z;
         int b1 = b *b;
         int ac = a *c;
         int a1 = a *2;
         int ac1 = -4*ac;
         int b2 = b1+ac1;
        
         
        double  i = division(-b,a1);	
        //String.format("%.3g%n", bi);
         double sq = Math.sqrt(b2);
         double x1 = (-1 * b) + sq;
         double x2 = (-1 * b) - sq;
         double xf1 = x1 /a1;
         double xf2= x2 /a1;
         
         
        // double temp1 = Math.sqrt(b * b - 4 * a * c);
 
        // double root1 = (-b +  temp1) / (2*a) ;
        // double root2 = (-b -  temp1) / (2*a) ;
         
         
        String result = ("Ecuacion: "+ecuacion+"\n"); 
        //result+=(a+"x\u00b2"+b+"x"+c+"= 0");
        result +=("\n");
        result +=("Aplicando Formula general:\n\n"); 
        //result += ("        --------------------------------------\n");
        result +=("x = - ("+b+")	 \u00b1 \u221A "+b+"\u00b2 -4("+a+") ("+c+")\n");
        result +=("         -----------------------------\n");
        result +=("                        2 ("+a+")\n");
        
        result +=("\n");
        
        result +=("Paso 1:\n\n");
        result +=("x = - ("+b+") \u00b1 \u221A "+b1+"  -4("+ac+")\n");
        result +=("         ------------------------\n");
        result +=("                      ("+a1+")\n");
        result +=("\n");
        
        
        result +=("Paso 2:\n\n");
        result +=("x = - ("+b+") \u00b1 \u221A "+b1+" + ("+ac1+")\n");
        result +=("        --------------------------\n");
        result +=("                       ("+a1+")\n");
        result +=("\n");
       
        
        result +=("Paso 3:\n\n");
        result +=("x = - ("+b+") \u00b1 \u221A ("+b2+")\n");
        result +=("        -----------------\n");
        result +=("                 ("+a1+")\n");
        result +=("\n");
        
        if(b2 < 0){
        	//b2 = -b2;
     	
     	
     	   result +=("Paso 4:\n\n");
            result +=("x1 = - ("+b+") + ("+b2+") i\n");
            result +=("           ------------\n");
            result +=("                 ("+a1+")\n");
            
            result +=("x2 = - ("+b+") - ("+b2+") i\n");
            result +=("           ------------\n");
            result +=("                 ("+a1+")\n");
            result +=("\n");
            
            
            result +=("Resultado:\n\n");
           result +=("x1 = - ("+i+") + ("+b2+") i\n");
           result +=("x2 = - ("+i+") - ("+b2+") i\n");
           
          
         
        }
        
        else{
        result +=("Paso 4:\n\n");
        result +=("x = - ("+b+") \u00b1 ("+sq+")\n");
        result +=("          --------------\n");
        result +=("                ("+a1+")\n");
        result +=("\n");
        
        result +=("Paso 5:\n\n");
        result +=("x1 = - ("+b+") + ("+sq+")\n");
        result +=("           --------------\n");
        result +=("                 ("+a1+")\n");
        result +=("\n");
        
        result +=("x2 = - ("+b+") - ("+sq+")\n");
        result +=("           --------------\n");
        result +=("                 ("+a1+")\n");
        result +=("\n");
        
        result +=("Paso 6:\n\n");
        result +=("x1 =  ("+x1+")\n");
        result +=("          ------\n");
        result +=("           ("+a1+")\n");
        result +=("\n");
        
        result +=("x2 =  ("+x2+")\n");
        result +=("          ------\n");
        result +=("           ("+a1+")\n");
        result +=("\n");
        
        result +=("Resultado:\n\n");
        result +=("\n");
        result +=("x1 =  ("+xf1+")\n");
        result +=("x2 = ("+xf2+")\n");
        
        } 
        return (result);
 	}
 	
 	
 }
