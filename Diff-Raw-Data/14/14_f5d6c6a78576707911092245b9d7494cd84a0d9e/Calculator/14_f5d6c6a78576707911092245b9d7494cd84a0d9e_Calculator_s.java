 package kamkowarrier.collab.resistorreader;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 
 public class Calculator {
 	
 	double val1;
 	double val2;
 	double val3;
 	double mul;
 	double tol;
 	double result;
 	String[] bounds;
 	
 	// DEEPS: This method is called at the end of the ResistorView class. ResistorView communicates with a set instance of Calculator  
 	public String calculate(int[] bandColors, ColorBand[] bandTypeArray) {
 		String str = "";
 	    ArrayList<Double> values = new ArrayList<Double>();
 	    for (int i = 0; i < bandColors.length; i++) {
 	        values.add(bandTypeArray[i].colorToValue(bandColors[i]));
 	    }
 	    if (values.size() == 4) {
 	    	val1 = values.get(0);
 	    	val2 = values.get(1);
 	    	mul = values.get(2);
 	    	tol = values.get(3);
	    	result = (((10 * val1) + val2) * Math.pow(10, mul));
 	    	str = addSuffix(result,1);
 	    }
 	    else { //Not accounting for six bands!
 	    	val1 = values.get(0);
 	    	val2 = values.get(1);
 	    	val3 = values.get(2);
 	    	mul = values.get(3);
 	    	tol = values.get(4);
	    	result = (((10 * val1) + val2 + .1*val3) * Math.pow(10, mul));
 	    	str = addSuffix(result,1);
 	    }
 	    bounds = new String[2];
     	bounds[0] = addSuffix(result-((tol/100.0)*result),2);
     	bounds[1] = addSuffix(result+((tol/100.0)*result),2);
 	    return str;
 	
 	}
 	
     public String calculate(int a, int b, int c, int d) {
     	
     	this.val1 = a;
     	this.val2 = b;
     	this.mul = c;
     	this.tol = d;
     	
     	switch ((int) tol) {
     	case 0:
     		tol = 0.1;
     		break;
     	case 1:
     		tol = 0.25;
     		break;
     	case 2:
     		tol = 0.5;
     		break;
     	case 3:
     		tol = 1;
     		break;
     	case 4:
     		tol = 2;
     		break;
     	case 5:
     		tol = 5;
     		break;
     	case 6:
     		tol = 10;
     		break;
     	
     	}
     	
     	double result;
     	result = (((10 * val1) + val2) * Math.pow(10, mul)); //
 
     	bounds = new String[2];
     	bounds[0] = addSuffix(result-((tol/100.0)*result),2);
     	bounds[1] = addSuffix(result+((tol/100.0)*result),2);
         return addSuffix(result,1);
     }
     
     public String addSuffix(double result,int scale) {
     	String output;
     	if (result > 1e6) {
     		double mill = result / 1e6;
     		BigDecimal mill2 = new BigDecimal(mill);
     		mill2 = mill2.setScale(scale,BigDecimal.ROUND_HALF_DOWN);
     		output = mill2 + "M";
     	} else if (result > 1e3) {
     		double thou = result / 1e3;
     		BigDecimal thou2 = new BigDecimal(thou);
     		thou2 = thou2.setScale(scale,BigDecimal.ROUND_HALF_DOWN);
     		output = thou2 + "K";
     	} else {
     		int resultInt = (Double.valueOf(result)).intValue();
     		output = (Integer.valueOf(resultInt)).toString();
     	}
     return output;
     }
 }
