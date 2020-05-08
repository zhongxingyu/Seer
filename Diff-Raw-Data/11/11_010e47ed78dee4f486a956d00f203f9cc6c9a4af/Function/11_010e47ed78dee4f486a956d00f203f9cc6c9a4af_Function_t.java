 package PeanutCracker;
 
 import java.util.ArrayList;
 
 public class Function extends ArrayList<Element>
 {
	/**
	 * generated serializable version UID added because it is a serializable class
	 */
	private static final long serialVersionUID = -1852458274303596909L;
 	//add to an interface Constants
 	public enum Operators {NONE, SUBSTITUTION, DERIVATIVE, INTEGRAL};
 	
 	Operators operator = Operators.NONE;
 	Double substitute;
 	String modFunction;
 	public Function()
 	{
 		
 	}
 	public Function(ArrayList<Element> func)
 	{
 		//create a function from an arraylist of elements, default operation is none
 		this(func, Operators.NONE);
 	}
 	public Function(ArrayList<Element> func, Operators op)
 	{
 		//create a function from an arraylist of elements, operation is set
 		this.setFunction(func);
 		operator = op;
 	}
 	public void setFunction(ArrayList<Element> func)
 	{
 		//set the function to be the array list of elements
 		for(Element e: func)
 		{
 			//remove previous function
 			this.removeAll(this);
 			//add in the pieces
 			this.add(e);
 		}
 	}
 	public void setOperator(Operators op)
 	{
 		//sets the enum operator
 		operator = op;
 	}
 	public ArrayList<Element> getFunction()
 	{
 		return this;
 	}
 	public Operators getOperator()
 	{
 		return operator;
 	}
 	public boolean checkOp()
 	{
 		//check to see if Op is not none or unset
 		boolean sam = true;
 		if (operator == Operators.NONE)
 		{
 			sam = false;
 		}
 		return sam;
 	}
 	public boolean checkFunc()
 	{
 		//checks to see if function has elements
 		boolean sam = true;
 		if (this.size()==0)
 		{
 			sam = false;
 		}
 		return sam;
 	}
 	public Function operate(Function dave)
 	{
 		//called so that the function operates on itself, and it should return a new function with no operation
 		//this will be moved to the mastermind class
 		if (dave.checkOp())
 		{
 			Function carl = dave;
 			carl.setOperator(Operators.NONE);
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
