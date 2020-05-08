 package budgetapp.util;
 /**
  * Money class. Instead of using doubles directly, this class is used to get more flexibility
  * @author Steen
  *
  */
 public class Money {
 	
 	private double value;
 	public static boolean after = true;
 	private static String currency = "kr";
 	private static double exchangeRate = 1;
 	
 	public Money()
 	{
 		value = 0;
 	}
 	public Money(double x)
 	{
 		value = x;
 	}
 	public Money(Money m)
 	{
 		value = m.get();
 	}
 	public static void setCurrency(String c)
 	{
 		currency = c;
 	}
 	
 	public static String getCurrency()
 	{
 		return currency;
 	}
 	
 	public static double getExchangeRate()
 	{
 		return exchangeRate;
 	}
 	
 	public static void setExchangeRate(double val)
 	{
 		exchangeRate = val;
 	}
 	
 	public void set(double val)
 	{
 		value = val;
 	}
 	public void set(Money val)
 	{
 		value = val.get();
 	}
 	public double get()
 	{
 		return value;
 	}
 	
 	public Money add(double x)
 	{
 		return new Money(this.value+x);
 	}
 	public Money add(Money x)
 	{
 		return new Money(this.value + x.get());
 	}
 	public Money subtract(double x)
 	{
 		return new Money(this.value - x);
 	}
 	public Money subtract(Money x)
 	{
 		return new Money(this.value - x.get());
 	}
 	public Money divide(double x)
 	{
 		if(x!=0)
 			return new Money(this.value/x);
 		else
 			return new Money();
 	}
 	public Money divide(Money x)
 	{
 		if(x.get()!=0)
 			return new Money(this.value/x.get());
 		else
 			return new Money();
 	}
 	public Money multiply(double x)
 	{
 		return new Money(this.value*x);
 	}
 	public Money multiply(Money x)
 	{
 		return new Money(this.value*x.get());
 	}
 	
 	/**
 	 * Returns a string of the value with some formatting to get the minus sign
 	 * at the right place and not print out decimals where it's not needed
 	 */
 	public String toString()
 	{
		double values = value/exchangeRate;
 		if(after)
 		{
			if(frac(values)<0.01)
				return String.format("%.0f "+currency,values);
 			else
				return String.format("%.2f "+currency,values);
 		}
 		else
 		{
			if(frac(value)<0.01)
				return (values<0.0 ? "-" : "") + String.format(currency+"%.0f ",Math.abs(values));
 			else
				return (values<0.0 ? "-" : "") + String.format(currency + "%.2f",Math.abs(values));
 		}
 	}
 	
 	double frac(double d)
 	{
 		return d-Math.floor(d);
 	}
 	
 
 }
