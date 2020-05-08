 package PeanutCracker;
 
 public abstract class Element
 {
 	public String name;
 	
 	public Element none()
 	{
 		return this;
 	}
 	public Element derive()
 	{
 		//remove this code, replace with code to return a derived element
 		Element mod = (Element) new Object();
 		return mod;
 	}
 	public constant substitute(Double d)
 	{
 		//remove this code, replace with code to return constant function when x is substituted
 		constant mod = (constant) new Object();
 		return mod;
 	}
 	public Element integrate()
 	{
 		//remove this code, replace with code to return an integrated element
 		Element mod = (Element) new Object();
 		return mod;
 	}
 }
