 package ch.hsr.objectCaching.testFrameworkServer;
 
 import ch.hsr.objectCaching.action.Action;
 import ch.hsr.objectCaching.action.IncrementAction;
 
 public class ResultGenerator 
 {
 	private TestCase testCase;
	private double result;
	public ResultGenerator(TestCase testCase, double startValue)
 	{
 		this.testCase = testCase;
 		result = startValue;
 		calculateResult();
 	}
 	
 	private void calculateResult()
 	{
 		for(int i = 0; i < testCase.getScenariosCount(); i++)
 		{
 			for(int x = 0; x < testCase.getScenarios().get(i).getActionList().size(); x++)
 			{
 				Action action = testCase.getScenarios().get(i).getActionList().get(x);
 				if(action instanceof IncrementAction)
 				{
					result = result * ((IncrementAction)action).getFactor();
 				}
 			}
 		}
 	}
 	
	public double getResult()
 	{
 		return result;
 	}
 }
