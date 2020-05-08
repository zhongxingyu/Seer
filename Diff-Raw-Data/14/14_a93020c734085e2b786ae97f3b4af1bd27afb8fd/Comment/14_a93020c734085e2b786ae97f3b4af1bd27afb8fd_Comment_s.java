 package models;
 
 /**
  * 
  * An {@link Entry} containing a comment as its content.
  * 
  * @author Felix Langenegger
  * @author Tobias Brog (Review)
  * 
  */
 
 public class Comment extends Entry {
 
 	/** The id. */
 	private int id;
 
 	/** The entry. */
 	private Entry entry;
 
 	public Comment(int id, User owner, Entry entry, String content) {
 		super(owner, content);
 		this.entry = entry;
 		this.id = id;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see models.Entry#unregister()
 	 */
 	@Override
 	public void unregister() {
 		this.entry.unregister(this);
 		this.unregisterUser();
 		this.entry = null;
 	}
 
 	/**
 	 * Id.
 	 * 
 	 * @return the id of the <code>Comment</code>
 	 */
 	public int id() {
 		return this.id;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see models.Entry#unregister(models.Comment)
 	 */
 	@Override
 	/**
 	 * Just a stub. Should be removed with refactoring
 	 */
 	public void unregister(Comment comment) {
 		// TODO Auto-generated method stub
 	}
 
 	/**
 	 * Checks whether a <code>User</code> is registered in the Database.
 	 * 
 	 * @return boolean whether a User is registered or not
 	 */
 	public boolean isRegistered() {
 		return this.entry != null;
 	}
 
 }
