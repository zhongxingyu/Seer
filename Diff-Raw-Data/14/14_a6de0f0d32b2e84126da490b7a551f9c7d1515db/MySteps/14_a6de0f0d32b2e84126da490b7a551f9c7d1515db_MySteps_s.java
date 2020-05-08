 package com.example.appl2.steps;
 
 import org.jbehave.core.annotations.Given;
 import org.jbehave.core.annotations.Pending;
 import org.jbehave.core.annotations.Then;
 import org.jbehave.core.annotations.When;
 import org.jbehave.core.model.ExamplesTable;
 import org.jbehave.core.model.OutcomesTable;
 import org.jbehave.core.steps.Parameters;
 import org.hamcrest.Matchers;
 
 
 import java.util.HashMap;
 import java.util.ArrayList;
 import java.util.Map;
 
 public class MySteps
 {
 
 	private ArrayList<Map<String, Integer>> results = new ArrayList<Map<String, Integer>>();
 	private ExamplesTable inputData;
 	private ExamplesTable resultTable;
 
 	@Given("a table of factors:$factors")
	public void factors(ExamplesTable inputTable)
	{
		ExamplesTable inputData = inputTable;
 	}
 
 	@When("math is done")
	public void math()
	{
		for(Parameters parameter : inputData.getRowsAsParameters())
		{
 			int first = parameter.valueAs("first", Integer.class);
 			int second = parameter.valueAs("second", Integer.class);
 			HashMap<String, Integer> resultMap = new HashMap<String, Integer>();
 			resultMap.put("product", first * second);
 			resultMap.put("sum", first + second);
 			results.add(resultMap);
 		}
 	}
 
 	@Then("I should have:$resultTable")
 	public void results(ExamplesTable table)
 	{
 		OutcomesTable expected = new OutcomesTable();
 		for (int i = 0; i < results.size();i++)
 		{
 			Parameters row = resultTable.getRowAsParameters(i);
 			Map<String, Integer> actual = results.get(i);
 			for (String key : actual.keySet())
 			{
 				expected.addOutcome(key, actual.get(key), (Matchers.equalTo(row.valueAs(key, Integer.class))));
 			}
 		}
 		expected.verify();
 	}
 }
