 package org.kalibro.core.persistence;
 
 import static java.util.concurrent.TimeUnit.DAYS;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.persistence.TypedQuery;
 
 import org.apache.commons.io.FileUtils;
 import org.kalibro.Configuration;
 import org.kalibro.core.model.Project;
 import org.kalibro.core.model.enums.RepositoryType;
 import org.kalibro.core.persistence.record.ProjectRecord;
 import org.kalibro.core.processing.ProcessProjectTask;
 import org.kalibro.dao.ConfigurationDao;
 import org.kalibro.dao.ProjectDao;
 
 class ProjectDatabaseDao extends DatabaseDao<Project, ProjectRecord> implements ProjectDao {
 
 	private Map<String, ProcessProjectTask> processTasks;
 	private Map<String, Integer> processPeriods;
 
 	private ConfigurationDao configurationDao;
 
 	protected ProjectDatabaseDao(RecordManager recordManager) {
 		super(recordManager, ProjectRecord.class);
 		configurationDao = new ConfigurationDatabaseDao(recordManager);
 		processTasks = new HashMap<String, ProcessProjectTask>();
 		processPeriods = new HashMap<String, Integer>();
 	}
 
 	@Override
 	public void save(Project project) {
 		ProjectRecord record = createRecord(project);
		record = save(record);
 		project.setId(record.convert().getId());
 	}
 
 	@Override
 	public List<String> getProjectNames() {
 		return getAllNames();
 	}
 
 	@Override
 	public boolean hasProject(String projectName) {
 		return hasEntity(projectName);
 	}
 
 	@Override
 	public Project getProject(String projectName) {
 		return getByName(projectName);
 	}
 
 	@Override
 	public void removeProject(String projectName) {
 		Project project = getByName(projectName);
 		ProjectRecord record = createRecord(project);
 		recordManager.beginTransaction();
 		delete("MetricResult", "module.projectResult.project.name", projectName);
 		delete("Module", "projectResult.project.name", projectName);
 		delete("ProjectResult", "project.name", projectName);
 		recordManager.remove(record);
 		recordManager.commitTransaction();
 		FileUtils.deleteQuietly(project.getDirectory());
 	}
 
 	private ProjectRecord createRecord(Project project) {
 		Configuration configuration = configurationDao.getConfiguration(project.getConfigurationName());
 		return new ProjectRecord(project, configuration.getId());
 	}
 
 	private void delete(String table, String projectNameField, String projectName) {
 		String queryText = "DELETE FROM " + table + " t WHERE t." + projectNameField + " = :projectName";
 		TypedQuery<ProjectRecord> query = createRecordQuery(queryText);
 		query.setParameter("projectName", projectName);
 		query.executeUpdate();
 	}
 
 	@Override
 	public Set<RepositoryType> getSupportedRepositoryTypes() {
 		return RepositoryType.supportedTypes();
 	}
 
 	@Override
 	public void processProject(String projectName) {
 		new ProcessProjectTask(projectName).executeInBackground();
 	}
 
 	@Override
 	public void processPeriodically(String projectName, Integer periodInDays) {
 		cancelPeriodicProcess(projectName);
 		ProcessProjectTask task = new ProcessProjectTask(projectName);
 		processTasks.put(projectName, task);
 		processPeriods.put(projectName, periodInDays);
 		task.executePeriodically(periodInDays, DAYS);
 	}
 
 	@Override
 	public Integer getProcessPeriod(String projectName) {
 		return processPeriods.containsKey(projectName) ? processPeriods.get(projectName) : 0;
 	}
 
 	@Override
 	public void cancelPeriodicProcess(String projectName) {
 		if (processTasks.containsKey(projectName)) {
 			processTasks.get(projectName).cancelExecution();
 			processTasks.remove(projectName);
 			processPeriods.remove(projectName);
 		}
 	}
 }
