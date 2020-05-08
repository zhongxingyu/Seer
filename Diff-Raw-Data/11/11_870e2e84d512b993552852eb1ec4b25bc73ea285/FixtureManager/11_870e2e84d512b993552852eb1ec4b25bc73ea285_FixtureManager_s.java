 package it.polimi.chansonnier.test;
 
 import java.io.File;
 import java.io.InputStream;
 
 import org.eclipse.smila.blackboard.Blackboard;
 import org.eclipse.smila.blackboard.path.Path;
 import org.eclipse.smila.datamodel.id.Id;
 import org.eclipse.smila.datamodel.id.IdFactory;
 import org.eclipse.smila.datamodel.record.Literal;
 import org.eclipse.smila.processing.WorkflowProcessor;
 
 public class FixtureManager {
 	private WorkflowProcessor processor;
 	private Blackboard blackboard;
 	private String pipelineName;
 
 	public FixtureManager(WorkflowProcessor processor, Blackboard blackboard, String pipelineName) {
 		this.processor = processor;
 		this.blackboard = blackboard;
 		this.pipelineName = pipelineName;
 	}
 	
 	public Id[] addSong(String key, InputStream beautifulDayFlv, String pageTitle) throws Exception {
 		if (!processor.getWorkflowNames().contains(pipelineName)) {
 			throw new Exception("Pipeline " + pipelineName + " is not active.");
 		}
 		Id hero = createRecord(key, beautifulDayFlv, pageTitle);
 		final Id[] result = processor.process(pipelineName, blackboard, new Id[] { hero });
 		return result;
 	}
 	
 	private Id createRecord(String key, InputStream original, String pageTitle) throws Exception {
 		final Id song = createBlackboardRecord("youtube", key);
 		final Literal pageTitleL = blackboard.createLiteral(song);
 		pageTitleL.setStringValue(pageTitle);
 		blackboard.setLiteral(song, new Path("PageTitle"), pageTitleL);
 		blackboard.setAttachmentFromStream(song, "Original", original);
 		return song;
 	}
 	
 	  /**
 	   * create a new record on the blackboard.
 	   * 
 	   * @param source
 	   *          source value of ID
 	   * @param key
 	   *          key value of ID
 	   * @return id of created record.
 	   */
 	  protected Id createBlackboardRecord(String source, String key) {
 	    final Id id = IdFactory.DEFAULT_INSTANCE.createId(source, key);
 	    blackboard.invalidate(id);
 	    blackboard.create(id);
 	    return id;
 	  }
 }
