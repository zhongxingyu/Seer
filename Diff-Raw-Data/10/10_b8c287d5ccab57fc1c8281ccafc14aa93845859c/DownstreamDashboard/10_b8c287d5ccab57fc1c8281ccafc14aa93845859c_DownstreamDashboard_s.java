 package com.attask.downstreamdashboard;
 
 import hudson.Extension;
 import hudson.model.*;
 import jenkins.model.Jenkins;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 import org.kohsuke.stapler.export.Exported;
 import org.kohsuke.stapler.export.ExportedBean;
 
 import javax.servlet.ServletException;
 import java.io.IOException;
 import java.util.*;
 
 /**
  * User: Joel Johnson
  * Date: 3/23/13
  * Time: 2:35 PM
  */
 @ExportedBean
 public class DownstreamDashboard extends View {
 	private static final String DEFAULT_TREE_QUERY = " ";
 	private static final int DEFAULT_COUNT = 5;
 	private static final String DEFAULT_SEARCH_QUERY = "projectName=${jobName}";
 
 	private String labels;
 	private String jobNames;
 	private String counts;
 	private String queries;
 	private String treeQueries;
 	private transient List<TableConfiguration> tableConfigurations;
 
 	private transient volatile Map<TableConfiguration, Table> tables;
 
 	@DataBoundConstructor
 	public DownstreamDashboard(String name) {
 		super(name);
 	}
 
 	@Override
 	protected void submit(StaplerRequest request) throws IOException, ServletException, Descriptor.FormException {
 		// Don't set these until we successfully populate the configurations.
 		// This is to prevent it a bad submit from getting all the fields out of sync
 		String labels = RequestUtils.getParameter(request, "_.labels");
 		String jobNames = RequestUtils.getParameter(request, "_.jobNames");
 		String counts = RequestUtils.getParameter(request, "_.counts");
 		String queries = RequestUtils.getParameter(request, "_.queries");
 		String treeQueries = RequestUtils.getParameter(request, "_.treeQueries");
 
 		this.tableConfigurations = populateTableConfigurations(split(labels), split(jobNames), split(counts), split(queries), split(treeQueries));
 
 		// Success! Overwrite fields.
 		this.labels = labels;
 		this.jobNames = jobNames;
 		this.counts = counts;
 		this.queries = queries;
 		this.treeQueries = treeQueries;
 	}
 
 	private static List<String> split(String toSplit) {
 		List<String> result = new ArrayList<String>();
 		if(toSplit != null && !toSplit.isEmpty()) {
 			Scanner scanner = new Scanner(toSplit);
 			while(scanner.hasNextLine()) {
 				String line = scanner.nextLine();
 				result.add(line);
 			}
 		}
 		return result;
 	}
 
 	private List<TableConfiguration> populateTableConfigurations(List<String> labels, List<String> jobNames, List<String> counts, List<String> queries, List<String> treeQueries) {
 		int lastCount = DEFAULT_COUNT;
 		String lastQuery = DEFAULT_SEARCH_QUERY;
 		String lastTreeQuery = DEFAULT_TREE_QUERY;
 
 		List<TableConfiguration> tableConfigurations = new ArrayList<TableConfiguration>(jobNames.size());
 		for(int i = 0; i < jobNames.size(); i++) {
 			String jobName = jobNames.get(i);
 
 			String label = jobName;
 			if(labels.size() > i) {
 				label = labels.get(i);
 			}
 
 			if(counts.size() > i) {
 				lastCount = Integer.parseInt(counts.get(i));
 			}
 			if(queries.size() > i) {
 				lastQuery = queries.get(i);
 				if(lastQuery == null || lastQuery.isEmpty()) {
 					lastQuery = DEFAULT_SEARCH_QUERY;
 				}
 			}
 			if(treeQueries.size() > i) {
 				lastTreeQuery = treeQueries.get(i);
 				if(lastTreeQuery == null || lastTreeQuery.isEmpty()) {
 					lastTreeQuery = DEFAULT_TREE_QUERY;
 				}
 			}
 
 			TableConfiguration tableConfiguration = new TableConfiguration(label, jobName, lastCount, lastQuery, lastTreeQuery);
 			tableConfigurations.add(tableConfiguration);
 		}
 
 		return tableConfigurations;
 	}
 
 	/**
 	 * Invalidates the search map so the entire job's build list is re-indexed.
 	 */
 	@SuppressWarnings("UnusedDeclaration")
 	public void doInvalidate(StaplerRequest request, StaplerResponse response) {
 		checkPermission(View.DELETE);
 		tables = null;
 	}
 
 	@Exported
 	public List<Table> getTables() {
 		Map<TableConfiguration, Table> tables = this.tables;
 		if(tables == null) {
 			synchronized (this) {
 				tables = this.tables;
 				if(tables == null) {
 					tables = new HashMap<TableConfiguration, Table>();
 					this.tables = tables;
 				}
 			}
 		}
 
 		List<TableConfiguration> tableConfigurations = findTableConfigurations();
 		List<Table> result = new ArrayList<Table>(tableConfigurations.size());
 		for (TableConfiguration tableConfiguration : tableConfigurations) {
 			Table table = tables.get(tableConfiguration);
 			if(table == null) {
 				//noinspection SynchronizationOnLocalVariableOrMethodParameter
 				synchronized (tables) {
 					table = tables.get(tableConfiguration);
 					if(table == null) {
 						table = new Table(tableConfiguration);
 						tables.put(tableConfiguration, table);
 					}
 				}
 			}
 			result.add(table);
 		}
 
 		return result;
 	}
 
 	public String getLabels() {
 		return labels;
 	}
 
 	public String getJobNames() {
 		return jobNames;
 	}
 
 	public String getCounts() {
 		return counts;
 	}
 
 	public String getQueries() {
 		return queries;
 	}
 
 	public String getTreeQueries() {
 		return treeQueries;
 	}
 
 	public List<TableConfiguration> findTableConfigurations() {
 		if(tableConfigurations == null) {
 			tableConfigurations = populateTableConfigurations(split(getLabels()), split(getJobNames()), split(getCounts()), split(getQueries()), split(getTreeQueries()));
 		}
 		return tableConfigurations;
 	}
 
 
 	@Override
 	public void onJobRenamed(Item item, String oldName, String newName) {
 		if(tables != null && oldName != null) {
 			for (TableConfiguration tableConfiguration : tableConfigurations) {
 				if(oldName.equals(tableConfiguration.getJobName())) {
 					tableConfiguration.setJobName(newName);
 					//noinspection SynchronizeOnNonFinalField
 					synchronized (tables) {
 						tables.remove(tableConfiguration);
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public Item doCreateItem(StaplerRequest request, StaplerResponse response) throws IOException, ServletException {
 		return Jenkins.getInstance().doCreateItem(request, response);
 	}
 
 	@Override
 	public boolean contains(TopLevelItem item) {
 		return false;
 	}
 
 	@Override
 	public Collection<TopLevelItem> getItems() {
 		return Collections.emptyList();
 	}
 
 	@Extension
 	public static final class DescriptorImpl extends ViewDescriptor {
 		@Override
 		public String getDisplayName() {
 			return "Downstream View";
 		}
 	}
 }
