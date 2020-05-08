 package com.tomtom.amelinium.projectservice.corrector;
 
 import org.apache.ibatis.session.SqlSessionFactory;
 
 import com.tomtom.amelinium.backlogservice.factory.BacklogServiceFactory;
 import com.tomtom.amelinium.backlogservice.model.BacklogModel;
 import com.tomtom.amelinium.backlogservice.serializer.BacklogModelSerializer;
 import com.tomtom.amelinium.chartservice.factory.ChartServiceFactory;
 import com.tomtom.amelinium.db.config.Config;
 import com.tomtom.amelinium.db.logic.BacklogDbLogic;
 import com.tomtom.amelinium.db.logic.ChartDbLogic;
 import com.tomtom.amelinium.db.logic.ProjectDbLogic;
 import com.tomtom.amelinium.db.logic.SnapshotDbLogic;
 import com.tomtom.amelinium.projectservice.factory.ProjectSnapshotFactory;
 
 /**
  * Used for updating project model according to changes done by the user.
  * 
  * @author Natasza.Nowak@tomtom.com
  */
 public class ProjectCorrector {
 	private ProjectDbLogic projectDbLogic;
 	private BacklogDbLogic backlogDbLogic;
 	private ChartDbLogic chartDbLogic;
 	private SnapshotDbLogic snapshotDbLogic;
 	private BacklogServiceFactory backlogServiceFactory = new BacklogServiceFactory();
 	private ChartServiceFactory chartServiceFactory = new ChartServiceFactory();
 	private BacklogModelSerializer backlogModelSerializer = new BacklogModelSerializer();
 	private ProjectSnapshotFactory projectSnapshotFactory;// = new ProjectSnapshotFactory();
 
 	public ProjectCorrector(SqlSessionFactory sqlMapper) {
 		this.projectDbLogic = new ProjectDbLogic(sqlMapper);
 		this.projectSnapshotFactory = new ProjectSnapshotFactory(sqlMapper);
 		this.backlogDbLogic = new BacklogDbLogic(sqlMapper);
		this.chartDbLogic = new ChartDbLogic(sqlMapper);
 		this.snapshotDbLogic = new SnapshotDbLogic(sqlMapper);
 	}
 	/**
 	 * Reverts backlog to a specified revision.
 	 */
 	public void revertBacklog(int projectId, int revision) {
 		projectSnapshotFactory.generateSnapshotFromRevertedBacklog(projectId,
 				revision);
 	}
 
 	/**
 	 * Reverts chart to a specified revision.
 	 */
 	public void revertChart(int projectId, int revision) {
 		projectSnapshotFactory.generateSnapshotFromRevertedChart(projectId,
 				revision);
 	}
 
 	/**
 	 * Updates backlog of a given project based on a provided content.
 	 */
 	public Integer updateProjectWithNewBacklog(int projectId,
 			String wikiMarkupBacklog, boolean allowingMultilineFeatures) {
 		BacklogModel newBacklogModel = backlogServiceFactory
 				.readAndCorrectBacklogModelFromString(wikiMarkupBacklog,
 						allowingMultilineFeatures);
 		String newWikiMarkupBacklog = backlogModelSerializer
 				.serializeModel(newBacklogModel);
 		Integer result = projectSnapshotFactory.generateSnapshotFromNewBacklog(
 				projectId, newWikiMarkupBacklog);
 		return result;
 	}
 
 	/**
 	 * Updates chart of a given project based on a provided content. Chart gets
 	 * updated for a sprint specified in chart configuration table.
 	 * 
 	 * @param newWikiMarkupChart
 	 *            includes configuration table with sprint number
 	 */
 	public void updateProjectWithNewChart(int projectId,
 			String wikiMarkupBacklog, String newWikiMarkupChart) {
 		String correctedWikiMarkupChart = chartServiceFactory
 				.readCorrectAndSerializeChartModelFromStrings(
 						wikiMarkupBacklog, newWikiMarkupChart,
 						Config.allowingMultilineFeatures);
 		projectSnapshotFactory.generateSnapshotFromNewChart(projectId,
 				correctedWikiMarkupChart);
 	}
 
 	/**
 	 * Deletes specified project from the database. Deletes related backlog
 	 * entities. Deletes related chart entities. In the end deletes related
 	 * snapshot entities connecting these.
 	 */
 	public void deleteProject(int projectId) {
 		projectDbLogic.deleteProject(projectId);
 		backlogDbLogic.deleteBacklogs(projectId);
 		chartDbLogic.deleteBacklogs(projectId);
 		// important!! snapshots should be deleted in the last order!
 		snapshotDbLogic.deleteSnapshots(projectId);
 	}
 }
