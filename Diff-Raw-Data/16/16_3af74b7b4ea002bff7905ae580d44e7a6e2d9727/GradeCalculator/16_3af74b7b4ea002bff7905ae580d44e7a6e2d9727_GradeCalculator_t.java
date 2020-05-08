 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Stack;
 
 
 public class GradeCalculator 
 {
 	
 	public static void main(String[] args)
 	{
 		Stack<Class> classList = new Stack<Class>();
 		Class csci261 = new EasyClass();
 		classList.push(csci261);
 		
 		Class csci262 = new MediumClass();
 		classList.push(csci262);
 		
		//Class csci407 = new MediumClass();
		//classList.push(csci407);
 		
 		int time = 3;
 		gradeAllocation(time, classList);
 		
 		for ( double i : averages)
 		{
 			System.out.println("Result of " + i);
 		}
 	}
 	
 	public static ArrayList<Double> averages = new ArrayList<Double>();
 	public static int counter = 0;
 	public static Map<String, Double> calculations =  new HashMap<String, Double>();
 	
 	
 	public static double gradeAllocation(int time, Stack<Class> classes)
 	{
 		System.out.println("gradeAllocation("+time+", classes)");
 		double sum = 0;
 		counter++;
 		//System.out.println(counter + " Iterations ");
 		
 		if (classes.size() == 1)
 		{
 			//System.out.println("in if");
 			Class currentClass = classes.peek();
 			System.out.println(currentClass.toString());
 			System.out.println(time);
 			if( ! calculations.containsKey(currentClass.toString() + ","+ time) )
 			{
 				calculations.put(currentClass.toString() + ","+ time, currentClass.calcGrade(time));
 			}
 			return calculations.get(currentClass.toString() + ","+ time);
 		}
 		else
 		{
 			for(int i = 0; i <= time; i++ )
 			{
 				Class currentClass = classes.pop();
 				System.out.println(currentClass.toString());
 				System.out.println("Time " + i);
 				System.out.println("Grade " + currentClass.calcGrade(i)+"\n");
 				double grade = 0;
 				
				if( calculations.containsKey(currentClass.toString() + ","+ i) )
 				{
 					
					grade = calculations.get(currentClass.toString() + ","+ i);
 				}
 				else
 				{
 					grade = currentClass.calcGrade(i);
					calculations.put(currentClass.toString() + ","+ i, grade);
 				}
 				sum = grade + gradeAllocation(time-i, classes);
 				System.out.println("Total grade: "+sum);
 				classes.push(currentClass);
				averages.add(sum/2.0);
 			}
 			return sum;
 		}
 	}
 
 }
