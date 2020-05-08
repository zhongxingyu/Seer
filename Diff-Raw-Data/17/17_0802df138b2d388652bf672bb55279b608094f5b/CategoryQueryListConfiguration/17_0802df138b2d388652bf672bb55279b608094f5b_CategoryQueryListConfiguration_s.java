 package net.cyklotron.cms.category.query;
 
 import org.objectledge.parameters.Parameters;
 
 /**
  * Provides default parameter values for category query list component's configuration.
  * 
  * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: CategoryQueryListConfiguration.java,v 1.2 2005-01-20 05:45:22 pablo Exp $ 
  */
 public class CategoryQueryListConfiguration
 {
 	private String header;
 	private String queryPoolName;
 	private String sortColumn;
 	private boolean sortDir;
 
 	public CategoryQueryListConfiguration(Parameters componentConfig)
 	{
 		header = componentConfig.get("header",null);
 		queryPoolName = componentConfig.get("queryPoolName",null);
 		sortColumn = componentConfig.get("querySortColumn","name");
		sortDir = componentConfig.getBoolean("querySortDir",false);
 	}
 
 	public String getHeader()
 	{
 		return header;
 	}
     
 	public String getQueryPoolName()
 	{
 		return queryPoolName;
 	}
     
 	public String getSortColumn()
 	{
 		return sortColumn;
 	}
 
 	public boolean getSortDir()
 	{
 		return sortDir;
 	}
 }
