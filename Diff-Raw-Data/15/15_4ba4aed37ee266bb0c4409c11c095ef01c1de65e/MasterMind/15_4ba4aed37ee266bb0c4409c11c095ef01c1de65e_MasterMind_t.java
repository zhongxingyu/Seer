 package PeanutCracker;
 
 /**
  * Created with IntelliJ IDEA.
  * Editted with Aptana Studio 3
  * Creator: dongcarl
  * Editor: Buck
  * Date: 2/1/13
  * Time: 2:56 PM
  */
public class MasterMind implements Operator
 {
 	Double substitute;
 	String modFunction;
 	public MasterMind(ControlCenter cathy)
 	{
 		//The constructor takes in a ControlCenter to pass messages to and a function to act on
 	}
 	public Function operate(Function dave)
 	{
 		//called so that the function operates on itself, and it should return a new function with no operation
 		//this will be moved to the mastermind class
 		if (dave.checkOp())
 		{
 			Function carl = dave;
 			if (carl.getOperator() == Operators.SUBSTITUTION)
 			{
 				double frank = substitute(carl,substitute);
 				constant fred = new constant(frank);
 				carl = new Function();
 				carl.add(fred);
 			}
 			else if (carl.getOperator() == Operators.DERIVATIVE)
 			{
				carl = differentiate(carl);
 			}
 			else if (carl.getOperator() == Operators.INTEGRAL)
 			{
				carl = integrate(carl);
 			}
 			carl.setOperator(Operators.NONE);
 			return carl;
 		}
 		else
 		{
 			return dave;
 		}
 	}
 	private double substitute(Function mode, double replace)
 	{
 		//substitutes and evaluates the function for the given variable in place of "x"
 		Function mod = mode;
 		double sum = 0.0;
 		for (Element e:mod)
 		{
 			constant charlie = e.substitute(replace);
 			sum += charlie.getValue();
 			//should return a constant function with one element e
 		}
 		return sum;
 	}
 	private Function integrate(Function mode)
 	{
 		Function mod = mode;
 		for (Element e:mod)
 		{
 			e = e.derive();
 		}
 		return mod;
 	}
 	private Function differentiate(Function mode)
 	{
 		Function mod = mode;
 		for (Element e:mod)
 		{
 			e = e.derive();
 		}
 		return mod;
 	}
 }
