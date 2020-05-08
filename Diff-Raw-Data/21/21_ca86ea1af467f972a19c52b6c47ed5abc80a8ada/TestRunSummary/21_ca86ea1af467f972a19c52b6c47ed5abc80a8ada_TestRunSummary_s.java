 package org.tcrun.slickij.api.data;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  *
  * @author jcorbett
  */
 public class TestRunSummary extends Testrun implements Serializable
 {
 	private static final Map<String, Integer> statusOrderValues;
 	static {
 		statusOrderValues = new HashMap<String, Integer>();
 		statusOrderValues.put(ResultStatus.PASS.toString(), new Integer(1));
 		statusOrderValues.put(ResultStatus.FAIL.toString(), new Integer(2));
 		statusOrderValues.put(ResultStatus.BROKEN_TEST.toString(), new Integer(3));
 		statusOrderValues.put(ResultStatus.NOT_TESTED.toString(), new Integer(4));
 		statusOrderValues.put(ResultStatus.SKIPPED.toString(), new Integer(5));
 		statusOrderValues.put(ResultStatus.NO_RESULT.toString(), new Integer(6));
 	}
 
 	private Map<String, Long> resultsByStatus;
 	private List<String> statusListOrdered;
 	private TestplanReference testplan;
 
 	public TestRunSummary(Testrun run, TestplanReference ref)
 	{
 		resultsByStatus = new HashMap<String, Long>();
 		statusListOrdered = new ArrayList<String>();
 		this.setId(run.getObjectId());
 		this.setTestplanId(run.getTestplanObjectId());
 		this.setBuild(run.getBuild());
 		this.setConfig(run.getConfig());
 		this.setDateCreated(run.getDateCreated());
 		this.setExtensions(run.getExtensions());
 		this.setName(run.getName());
 		this.setProject(run.getProject());
 		this.setRelease(run.getRelease());
 		this.testplan = ref;
 	}
 
 	public Map<String, Long> getResultsByStatus()
 	{
 		return resultsByStatus;
 	}
 
 	public void setResultsByStatus(Map<String, Long> resultsByStatus)
 	{
 		this.resultsByStatus = resultsByStatus;
 	}
 
 	public List<String> getStatusListOrdered()
 	{
 		if(statusListOrdered == null || statusListOrdered.isEmpty())
 		{
 			this.statusListOrdered = new ArrayList<String>(getResultsByStatus().keySet());
 			Collections.sort(statusListOrdered, new Comparator<String>() {
 
 				@Override
 				public int compare(String o1, String o2)
 				{
 					Integer o1SortOrder = statusOrderValues.get(o1);
 					if(o1SortOrder == null)
 						o1SortOrder = new Integer(50);
 					Integer o2SortOrder = statusOrderValues.get(o2);
 					if(o2SortOrder == null)
 						o2SortOrder = new Integer(50);
 					return o1SortOrder.compareTo(o2SortOrder);
 				}
 
 			});
 
 		}
 		return statusListOrdered;
 	}
 
 	public Long getTotal()
 	{
 		Long retval = new Long(0);
 		for(Long bystatus : getResultsByStatus().values())
 			retval += bystatus;
 		return retval;
 	}
 
 	public TestplanReference getTestplan()
 	{
 		return testplan;
 	}
 
 }
