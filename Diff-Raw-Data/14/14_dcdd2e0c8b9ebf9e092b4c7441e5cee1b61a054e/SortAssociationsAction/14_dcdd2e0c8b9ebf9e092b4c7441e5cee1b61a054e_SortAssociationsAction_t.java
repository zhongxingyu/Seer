 package gov.nih.nci.cagwas.web.action;
 
 import gov.nih.nci.cagwas.reports.SNPAssociationFindingReport;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessages;
 import org.apache.log4j.Logger;
 
 import java.util.ArrayList;
 
 /**
  * The SortAssociationsAction class is called when the user clicks on a column in the
  * associations report to sort on that column
  * <P>
  * @author mholck
  * @see org.apache.struts.action.Action
  */
 public class SortAssociationsAction extends Action
 {
 	private static Logger logger = Logger.getLogger(SortAssociationsAction.class);
 	
 	/**
 	 * execute is called when this action is posted to
 	 * <P>
 	 * @param mapping The ActionMapping for this action as configured in struts
 	 * @param form The ActionForm that posted to this action if any
 	 * @param request The HttpServletRequest for the current post
 	 * @param response The HttpServletResponse for the current post
 	 */
 	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
 		throws Exception
 	{
 		ActionMessages errors = new ActionMessages();
 		ActionForward forward = null;
 		String column = (String)request.getParameter("column");
 		String order = (String)request.getParameter("order");
		String noOfCategories = (String)request.getParameter("NoOfCategories");
 		
 		ArrayList results = (ArrayList)request.getSession().getAttribute("assoc.results");
 		
 		if (results != null)
 		{
 			logger.debug("Sorting results by column " + column);
 			
 			ArrayList<SNPAssociationFindingReport> newResults = null;
 			
 			if (order.equals("ascending"))
 			{
 				newResults = sortAscending(results, column);
 			}
 			else
 			{
 				newResults = sortDescending(results, column);
 			}
 			
 			request.getSession().setAttribute("assoc.results", newResults);
 			request.setAttribute("numberFindings", newResults.size());
 			request.setAttribute("sortedColumn", column);
 			request.setAttribute("sortOrder", order);
			request.setAttribute("NoOfCategories", noOfCategories);
 		}
 		
 		// If there were errors then return to the input page else go on
 	    if (!errors.isEmpty())
 	    {
 	    	addErrors(request, errors);
 	    	forward = new ActionForward(mapping.getInput());
 	    }
 	    else
 	    {
 			forward = mapping.findForward("success");
 	    }
 		
 		return forward;
 	}
 	
 	/**
 	 * The sortAscending method will sort the results in ascending order using the column
 	 * provided.
 	 * <P>
 	 * @param results The results to sort
 	 * @param column The column to use for comparison
 	 * @return The sorted ArrayList
 	 */
 	private ArrayList<SNPAssociationFindingReport> sortAscending(ArrayList results, String column)
 	{
 		ArrayList<SNPAssociationFindingReport> newResults = new ArrayList<SNPAssociationFindingReport>();
 		
 		// Handle the multiple sort conditions
 		if (column.equals("analysis"))
 			results = sortAscending(results, "chromosome");
 		
 		int maxSize = results.size();
 		while(newResults.size() < maxSize)
 		{
 			int smallestIndex = 0;
 			SNPAssociationFindingReport smallest = (SNPAssociationFindingReport)results.get(0);
 			for (int i=1; i<results.size(); i++)
 			{
 				SNPAssociationFindingReport current = (SNPAssociationFindingReport)results.get(i);
 				
 				// Handle the column snpid
 				if (column.equals("snpid"))
 				{
 					boolean currentIdString = false;
 					boolean smallestIdString = false;
 					long currentId = 0;
 					long smallestId = 0;
 					
 					String currentSnpId = current.getDbsnpId().substring(2);
 					try
 					{
 						currentId = Long.parseLong(currentSnpId);
 					}
 					catch(NumberFormatException n)
 					{
 						currentIdString = true;
 					}
 					
 					String smallestSnpId = smallest.getDbsnpId().substring(2);
 					try
 					{
 						smallestId = Long.parseLong(smallestSnpId);
 					}
 					catch(NumberFormatException n)
 					{
 						smallestIdString = true;
 					}
 					
 					// Handle the different cases of number and string
 					if ((!currentIdString) && smallestIdString)
 					{
 						// If we are number but they are string we are smaller
 						smallest = current;
 						smallestIndex = i;
 					}
 					else if (currentIdString && smallestIdString)
 					{
 						// If we are both strings then let String compare them
 						if(currentSnpId.compareTo(smallestSnpId) < 0)
 						{
 							smallest = current;
 							smallestIndex = i;
 						}
 					}
 					else if ((!currentIdString) && (!smallestIdString))
 					{
 						// If we are both numbers then do the compare ourselves
 						if (currentId < smallestId)
 						{
 							smallest = current;
 							smallestIndex = i;
 						}
 					}
 				}
 				
 				// Handle the chromosome column
 				if (column.equals("chromosome"))
 				{
 					boolean currentChromString = false;
 					boolean smallestChromString = false;
 					int currentChromo = 0;
 					int smallestChromo = 0;
 					
 					String currentChrom = current.getChromosomeName();
 					try
 					{
 						currentChromo = Integer.parseInt(currentChrom);
 					}
 					catch(NumberFormatException n)
 					{
 						currentChromString = true;
 					}
 					
 					String smallestChrom = smallest.getChromosomeName();
 					try
 					{
 						smallestChromo = Integer.parseInt(smallestChrom);
 					}
 					catch(NumberFormatException n)
 					{
 						smallestChromString = true;
 					}
 					
 					// Handle the different cases of number and string
 					if ((!currentChromString) && smallestChromString)
 					{
 						// If we are number but they are string we are smaller
 						smallest = current;
 						smallestIndex = i;
 					}
 					else if (currentChromString && smallestChromString)
 					{
 						// If we are both strings then let String compare them
 						if(currentChrom.compareTo(smallestChrom) < 0)
 						{
 							smallest = current;
 							smallestIndex = i;
 						}
 					}
 					else if ((!currentChromString) && (!smallestChromString))
 					{
 						// If we are both numbers then do the compare ourselves
 						if (currentChromo < smallestChromo)
 						{
 							smallest = current;
 							smallestIndex = i;
 						}
 					}
 					
 					// Now check if we are both the same then sort by location
 					if (currentChrom.equals(smallestChrom))
 					{
 						long currentLoc = Long.parseLong(current.getChromosomeLocation());
 						long smallestLoc = Long.parseLong(smallest.getChromosomeLocation());
 						if (currentLoc < smallestLoc)
 						{
 							smallest = current;
 							smallestIndex = i;
 						}
 					}
 				}
 				
 				// Handle the analysis name column
 				if (column.equals("analysis"))
 				{
 					String currentAnal = current.getSnpAssociationAnalysisName();
 					String smallestAnal = smallest.getSnpAssociationAnalysisName();
 					if (currentAnal.compareTo(smallestAnal) < 0)
 					{
 						smallest = current;
 						smallestIndex = i;
 					}
 				}
 				
 				// Handle the column pvalue
 				if (column.equals("pvalue"))
 				{
					float currentValue = Float.MIN_VALUE;
					float smallestValue = Float.MIN_VALUE;
					if(current.getPvalue()!= null && current.getPvalue().trim().length()> 0){
						currentValue = Float.parseFloat(current.getPvalue());
					}
					if(smallest.getPvalue()!= null && smallest.getPvalue().trim().length()> 0){
						smallestValue = Float.parseFloat(smallest.getPvalue());
					}
 					if (currentValue < smallestValue)
 					{
 						smallest = current;
 						smallestIndex = i;
 					}
 				}
 				
 				// Handle the column rank
 				if (column.equals("rank"))
 				{
 					long currentRank = Long.parseLong(current.getRank());
 					long smallestRank = Long.parseLong(smallest.getRank());
 					if (currentRank < smallestRank)
 					{
 						smallest = current;
 						smallestIndex = i;
 					}
 				}
 			}
 			results.remove(smallestIndex);
 			newResults.add(smallest);
 		}
 		
 		return newResults;
 	}
 	
 	/**
 	 * The sortDescending method will sort the results in descending order using the column
 	 * provided.
 	 * <P>
 	 * @param results The results to sort
 	 * @param column The column to use for comparison
 	 * @return The sorted ArrayList
 	 */
 	private ArrayList<SNPAssociationFindingReport> sortDescending(ArrayList results, String column)
 	{
 		ArrayList<SNPAssociationFindingReport> newResults = new ArrayList<SNPAssociationFindingReport>();
 		
 		// Handle the multiple sort conditions
 		if (column.equals("analysis"))
 			results = sortAscending(results, "chromosome");
 		
 		int maxSize = results.size();
 		while(newResults.size() < maxSize)
 		{
 			int largestIndex = 0;
 			SNPAssociationFindingReport largest = (SNPAssociationFindingReport)results.get(0);
 			for (int i=1; i<results.size(); i++)
 			{
 				SNPAssociationFindingReport current = (SNPAssociationFindingReport)results.get(i);
 				
 				// Handle the column snpid
 				if (column.equals("snpid"))
 				{
 					boolean currentIdString = false;
 					boolean largestIdString = false;
 					long currentId = 0;
 					long largestId = 0;
 					
 					String currentSnpId = current.getDbsnpId().substring(2);
 					try
 					{
 						currentId = Long.parseLong(currentSnpId);
 					}
 					catch(NumberFormatException n)
 					{
 						currentIdString = true;
 					}
 					
 					String largestSnpId = largest.getDbsnpId().substring(2);
 					try
 					{
 						largestId = Long.parseLong(largestSnpId);
 					}
 					catch(NumberFormatException n)
 					{
 						largestIdString = true;
 					}
 					
 					// Handle the different cases of number and string
 					if (currentIdString && (!largestIdString))
 					{
 						// If we are string but they are number we are larger
 						largest = current;
 						largestIndex = i;
 					}
 					else if (currentIdString && largestIdString)
 					{
 						// If we are both strings then let String compare them
 						if(currentSnpId.compareTo(largestSnpId) > 0)
 						{
 							largest = current;
 							largestIndex = i;
 						}
 					}
 					else if ((!currentIdString) && (!largestIdString))
 					{
 						// If we are both numbers then do the compare ourselves
 						if (currentId > largestId)
 						{
 							largest = current;
 							largestIndex = i;
 						}
 					}
 				}
 				
 				// Handle the chromosome column
 				if (column.equals("chromosome"))
 				{
 					boolean currentChromString = false;
 					boolean largestChromString = false;
 					int currentChromo = 0;
 					int largestChromo = 0;
 					
 					String currentChrom = current.getChromosomeName();
 					try
 					{
 						currentChromo = Integer.parseInt(currentChrom);
 					}
 					catch(NumberFormatException n)
 					{
 						currentChromString = true;
 					}
 					
 					String largestChrom = largest.getChromosomeName();
 					try
 					{
 						largestChromo = Integer.parseInt(largestChrom);
 					}
 					catch(NumberFormatException n)
 					{
 						largestChromString = true;
 					}
 					
 					// Handle the different cases of number and string
 					if (currentChromString && (!largestChromString))
 					{
 						// If we are a string but they are a number we are larger
 						largest = current;
 						largestIndex = i;
 					}
 					else if (currentChromString && largestChromString)
 					{
 						// If we are both strings then let String compare them
 						if(currentChrom.compareTo(largestChrom) > 0)
 						{
 							largest = current;
 							largestIndex = i;
 						}
 					}
 					else if ((!currentChromString) && (!largestChromString))
 					{
 						// If we are both numbers then do the compare ourselves
 						if (currentChromo > largestChromo)
 						{
 							largest = current;
 							largestIndex = i;
 						}
 					}
 					
 					// Now check if we are both the same then sort by location
 					if (currentChrom.equals(largestChrom))
 					{
 						long currentLoc = Long.parseLong(current.getChromosomeLocation());
 						long largestLoc = Long.parseLong(largest.getChromosomeLocation());
 						if (currentLoc > largestLoc)
 						{
 							largest = current;
 							largestIndex = i;
 						}
 					}
 				}
 				
 				// Handle the analysis name column
 				if (column.equals("analysis"))
 				{
 					String currentAnal = current.getSnpAssociationAnalysisName();
 					String largestAnal = largest.getSnpAssociationAnalysisName();
 					if (currentAnal.compareTo(largestAnal) > 0)
 					{
 						largest = current;
 						largestIndex = i;
 					}
 				}
 				
 				// Handle the column rank
 				if (column.equals("pvalue"))
 				{
 					float currentValue = Float.parseFloat(current.getPvalue());
 					float largestValue = Float.parseFloat(largest.getPvalue());
 					if (currentValue > largestValue)
 					{
 						largest = current;
 						largestIndex = i;
 					}
 				}
 				
 				// Handle the column rank
 				if (column.equals("rank"))
 				{
 					long currentRank = Long.parseLong(current.getRank());
 					long largestRank = Long.parseLong(largest.getRank());
 					if (currentRank > largestRank)
 					{
 						largest = current;
 						largestIndex = i;
 					}
 				}
 			}
 			results.remove(largestIndex);
 			newResults.add(largest);
 		}
 		
 		return newResults;
 	}
 
 }
