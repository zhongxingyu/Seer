 package adstimator.data;
 
 import java.util.Observable;
 
 /**
  * A container for a KnowledgeBase.
  * 
 * The container is Observable and thus other objects may register themselves as listeners. By doing so they will be
  * notified when someone replaces the knowledge base with a new instance.
  *
  * @author erikbrannstrom
  */
 public class KnowledgeBaseContainer extends Observable
 {
 	private KnowledgeBase kb;
 
 	/**
 	 * Initialize a new container with a specific knowledge base.
 	 * 
 	 * @param kb 
 	 */
 	public KnowledgeBaseContainer(KnowledgeBase kb)
 	{
 		this.setKnowledgeBase(kb);
 	}
 	
 	/**
 	 * Initialize a new container with the knowledge base with the given ID.
 	 * 
 	 * @param id Unique knowledge base ID
 	 */
 	public KnowledgeBaseContainer(int id)
 	{
 		this.setKnowledgeBase(id);
 	}
 
 	/**
 	 * Return the knowledge base instance.
 	 * 
 	 * @return Active knowledge base
 	 */
 	public KnowledgeBase getKnowledgeBase()
 	{
 		return kb;
 	}
 
 	/**
 	 * Replace the knowledge base with a new instance. This method will automatically notify all listeners that a change
 	 * has occurred.
 	 * 
 	 * @param kb New knowledge base
 	 */
 	public final void setKnowledgeBase(KnowledgeBase kb)
 	{
 		if (this.kb != null && kb.id() == this.kb.id()) {
 			return;
 		}
 		this.kb = kb;
 		this.updated();
 	}
 	
 	/**
 	 * Mark this object as changed and notify all observers of that fact.
 	 */
 	public void updated()
 	{
 		this.setChanged();
 		this.notifyObservers();
 	}
 	
 	/**
 	 * Convenience method for setting the knowledge base from a known ID.
 	 * 
 	 * @param id 
 	 */
 	public final void setKnowledgeBase(int id)
 	{
 		this.setKnowledgeBase(KnowledgeBase.find(id));
 	}
 	
 }
