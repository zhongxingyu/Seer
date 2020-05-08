 import java.util.*;
 
 
 public class FilterComparison {
 
 	abstract class Comparison
 	{
 		public int identifier;
 		public List<String> names;
 		
 		public abstract boolean compareInteger( String testValue, 
 												String comparator,
 												String comparator2 );
 		
 		public abstract boolean compareFloat( String testValue, 
 												String comparator,
 												String comparator2 );
 		
 		public abstract boolean compareString( String testValue, 
 												String comparator,
 												String comparator2 );
 	}
 	
 	class EqualsComparison extends Comparison
 	{
 		
 		public EqualsComparison()
 		{
 			identifier = 4;
 			names = Arrays.asList("=", "equal to", "equals");
 		}
 		
 		@Override
 		public boolean compareInteger( String testValue, 
 												String comparator,
 												String comparator2 )
 		{
 			return Integer.parseInt(testValue) == Integer.parseInt(comparator);
 		}
 		
 		@Override
 		public boolean compareFloat( String testValue, 
 												String comparator,
 												String comparator2 )
 		{
 			float test = Float.parseFloat(testValue);
 			float comp = Float.parseFloat(comparator);
 			return (Float.compare(test, comp) == 0);
 		}
 		
 		@Override
 		public boolean compareString( String testValue, 
 												String comparator,
 												String comparator2 )
 		{
 			return testValue.equals(comparator);
 		}
 		
 	}
 	
 	private ArrayList<Comparison> allComparisons;
 	
 	public FilterComparison()
 	{
 		this.allComparisons = new ArrayList<Comparison>();
 		this.allComparisons.add( new EqualsComparison() );
 		//TODO add all comparisons
 	}
 
 	public boolean testFilterComparison( int type, FilterParameter filter, String testValue)
 	{
 		Comparison operation = null;
 		System.out.println("Comparison: "+filter.comparison);
 		for (Comparison test: this.allComparisons )
 		{
 			System.out.println(test.identifier);
 			if (test.identifier == filter.comparison)
 			{
 				operation = test;
 				break;
 			}
 		}
 		
 		if (operation == null)
 		{
 			throw new IllegalArgumentException("Invalid comparison");
 		}
 		
 		switch (type)
 		{
 		case 0:
 		{
 			//int
 			return operation.compareInteger(testValue, filter.comparator, filter.comparator2);
 		}
 		case 1:
 		{
 			//float
 			return operation.compareFloat(testValue, filter.comparator, filter.comparator2);
 		}
 		case 5:
 		{
 			//string
 			return operation.compareString(testValue, filter.comparator, filter.comparator2);
 		}
 		}
 		throw new IllegalArgumentException("Invalid data type");
 	}
 }
