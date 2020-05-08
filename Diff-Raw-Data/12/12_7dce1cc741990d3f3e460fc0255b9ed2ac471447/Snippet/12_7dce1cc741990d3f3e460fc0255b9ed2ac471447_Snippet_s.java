 package org.smartsnip.core;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.smartsnip.persistence.IPersistence;
 import org.smartsnip.shared.XCategory;
 import org.smartsnip.shared.XComment;
 import org.smartsnip.shared.XSnippet;
 
 /**
  * A snippet of the system. TODO: Write me
  * 
  * The hash code if the snippet is the internal integer hash.
  * 
  */
 public class Snippet {
 	/** Owner (creator) of the snippet */
 	public final String owner;
 	/** Snippet name */
 	private String name;
 	/** Unique hash code of the snippet */
 	public Long id = null;
 	/** Snippet description */
 	private String description;
 	/** Snippet associated category */
 	private String category;
 	/** Tags of the snippet */
 	private List<Tag> tags = new ArrayList<Tag>();
 	/** Comments of the snippet */
 	private List<Long> comments = new ArrayList<Long>();
 	/** Concrete snippet code */
 	private Code code;
 	/** License of the snippet. By default GPLv3 */
 	private String license = "GPLv3";
 	/** View counter */
 	private int viewcount = 0;
 
 	/** Averaged rating as cached value. */
 	private transient Float averageRating = null;
 
 	/** The snippet of the day */
 	private transient static Snippet snippetOfDay = null;
 	/** Time when the last time the snippet has been refreshed */
 	private transient static long snippetOfDayRefreshTime = 0;
 
 	/**
 	 * DB constructor must initialise all fields.
 	 * 
 	 * If a field is empty or null, a new {@link IllegalArgumentException} is
 	 * thrown
 	 * 
 	 * @param owner
 	 *            of the new snippet. Must not be null
 	 * @param name
 	 *            of the new snippet. Must not be null
 	 * @param description
 	 *            of the new snippet. Must not be null
 	 * @param id
 	 *            of the new snippet. Must not be null
 	 * @param code
 	 *            of the new snippet. Can be null
 	 * @param category
 	 *            of the new snippet. Must not be null
 	 * @param license
 	 *            of the new snippet. Must not be null
 	 * @param tags
 	 *            of the new snippet. Must not be null
 	 * @param comments
 	 *            of the new snippet. Must not be null
 	 * @param viewcount
 	 *            of the new snippet. If less than zero, will be set to zero
 	 * @throws IllegalArgumentException
 	 *             Thrown, if at least one of the arguments is null or empty
 	 */
 	Snippet(String owner, String name, String description, Long id,
 			String category, String license, List<Tag> tags,
 			List<Long> comments, int viewcount, Float averageRating) {
 
 		if (owner == null || owner.isEmpty())
 			throw new IllegalArgumentException(
 					"Owner of snippet cannot be null or empty");
 		if (name == null || name.isEmpty())
 			throw new IllegalArgumentException("Name of snippet cannot be null");
 		if (description == null)
 			description = "";
 		if (license == null)
 			license = "";
 		if (tags == null)
 			tags = new ArrayList<Tag>();
 		if (comments == null)
 			comments = new ArrayList<Long>();
 		if (viewcount < 0)
 			viewcount = 0;
 		if (averageRating < 0)
 			averageRating = 0F;
 
 		this.owner = owner;
 		this.name = name;
 		this.description = description;
 		this.code = CodeNull.getInstance();
 		this.id = id;
 		this.category = category;
 		this.license = license;
 		this.tags = tags;
 		this.comments = comments;
 		this.viewcount = viewcount;
 		this.averageRating = averageRating;
 	}
 
 	@Override
 	public int hashCode() {
 		if (id == null)
 			return 0;
 		return id.hashCode();
 	}
 
 	/**
 	 * Creates a snippet with the given parameters and adds the snippet to the
 	 * database. If the given snippet is alredy in the database, the given
 	 * snippet is not overwritten!
 	 * 
 	 * Throws an {@link IllegalArgumentException} if an argument is invalid
 	 * 
 	 * @param owner
 	 *            of the new snippet
 	 * @param name
 	 *            of the new snippet
 	 * @param description
 	 *            of the new snippet
 	 * @param hash
 	 *            of the new snippet
 	 * @param code
 	 *            of the new snippet
 	 * @param category
 	 *            of the new snippet
 	 * @param license
 	 *            of the new snippet
 	 * @param tags
 	 *            of the new snippet
 	 * @param comments
 	 *            of the new snippet
 	 * @param viewcount
 	 *            of the new snippet
 	 * @throws IllegalArgumentException
 	 *             Thrown if an argument is null or empty
 	 * @return the newly created snippet
 	 */
 	public static Snippet createSnippet(String owner, String name,
 			String description, String category, String code, String language,
 			String license, List<Tag> tags) throws IOException {
 
 		Snippet snippet = new Snippet(owner, name, description, null, category,
 				license, tags, null, 0, 0F);
 		addToDB(snippet);
 		// Create code object, with version 0 (begin)
 		snippet.code = Code.createCode(code, language, snippet, 0);
 
 		return snippet;
 	}
 
 	/**
 	 * Checks if the given hash code exists
 	 * 
 	 * @param hash
 	 *            to be checks
 	 * @return true if already registered hash, otherwise false
 	 */
 	synchronized static boolean exists(Long hash) {
 		if (hash == null)
 			return false;
 
 		try {
 			Snippet snippet = Persistence.instance.getSnippet(hash);
 			return snippet != null;
 		} catch (IOException e) {
 			System.err.println("IOException while exists(" + hash + ") "
 					+ e.getMessage());
 			e.printStackTrace(System.err);
 			return false;
 		}
 	}
 
 	/**
 	 * Searches for a snipped by his hash code and returns it if found. If no
 	 * snippet is found, null is returned
 	 * 
 	 * @param hash
 	 *            hash code of the snippet
 	 * @return the found snippet or null if not existsing
 	 */
 	public synchronized static Snippet getSnippet(Long hash) {
 		if (hash == null)
 			return null;
 
 		try {
 			// TODO Fix IPersistence:
 			// getSnippet(long)
 			Snippet snippet = Persistence.instance.getSnippet(hash);
 			return snippet;
 		} catch (IOException e) {
 			System.err.println("IOException while getSnippet(" + hash + ") "
 					+ e.getMessage());
 			e.printStackTrace(System.err);
 			return null;
 		}
 	}
 
 	/**
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Sets the name of the snippet. If the name is null or empty, nothing will
 	 * be done.
 	 * 
 	 * @param name
 	 *            the name to set
 	 */
 	public void setName(String name) {
 		if (name == null || name.length() == 0)
 			return;
 		if (this.name.equals(name))
 			return;
 
 		this.name = name;
 		refreshDB();
 	}
 
 	/**
 	 * @return the description
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * Sets the description of the snippet. If the description is empty or null,
 	 * nothing is done.
 	 * 
 	 * @param description
 	 *            the description to set
 	 */
 	public void setDescription(String description) {
 		if (description == null || description.length() == 0)
 			return;
 		if (this.description.equals(description))
 			return;
 
 		this.description = description;
 		refreshDB();
 	}
 
 	/**
 	 * @return the code of the snippet
 	 */
 	public Code getCode() {
 		if (code == null)
 			CodeNull.getInstance();
 		return code;
 	}
 
 	/**
 	 * Sets the code of the snippet. If null, nothing is done. The object to be
 	 * set cannot be a instance of CodeNull. In this case, the method returns
 	 * without effect.
 	 * 
 	 * @param code
 	 *            the code to set
 	 */
 	public void setCode(Code code) {
 		if (code == null || code instanceof CodeNull)
 			return;
 		if (this.code.equals(code))
 			return;
 
 		this.code = code;
 		refreshDB();
 	}
 
 	@Override
 	public String toString() {
 		return "Snippet: " + name;
 	}
 
 	/**
 	 * @return the category of the snippet
 	 */
 	public Category getCategory() {
 		return Category.getCategory(category);
 	}
 
 	/**
 	 * @return the average rating of the snippet
 	 */
 	public float getAverageRating() {
 		if (averageRating == null) {
 			try {
 				averageRating = Persistence.instance.getAverageRating(this);
 			} catch (IOException e) {
 				System.err
 						.println("IOException during getting average ratining of snippet \""
 								+ getName() + "\": " + e.getMessage());
 				e.printStackTrace(System.err);
 				if (averageRating == null)
 					return 0;
 			}
 		}
 		return averageRating;
 	}
 
 	/**
 	 * Set the snippet category. If null nothing happens.
 	 * 
 	 * This call removes the snippet from the old category and adds the snippet
 	 * to the new category.
 	 * 
 	 * @param category
 	 *            the category to set
 	 */
 	public void setCategory(Category category) {
 		if (category == null || this.category.equals(category))
 			return;
 
 		// IMPORTANT: This command must be in this order,
 		// otherwise we are in an endless loop!
 		this.category = category.getName();
 		category.addSnippet(this);
 
 		refreshDB();
 	}
 
 	/**
 	 * @return the license of the snippet
 	 */
 	public String getLicense() {
 		return license;
 	}
 
 	/**
 	 * Sets the new license of the snippet. If the license is null or empty,
 	 * nothing will be done
 	 * 
 	 * @param license
 	 *            the license to set
 	 */
 	public void setLicense(String license) {
 		if (license == null || license.length() == 0)
 			return;
 		if (this.license.equals(license))
 			return;
 		this.license = license;
 		refreshDB();
 	}
 
 	/**
 	 * @return the tags of the snippet
 	 */
 	public List<Tag> getTags() {
 		return tags;
 	}
 
 	/**
 	 * Adds a tag to the snippet. If the given tag is null, nothing will be
 	 * done. If the snippet has already been tagged with the tag, nothing is
 	 * done.
 	 * 
 	 * @param tag
 	 *            to be added to the snippet
 	 */
 	public synchronized void addTag(Tag tag) {
 		if (tag == null)
 			return;
 		if (tags.contains(tag))
 			return;
 
 		tags.add(tag);
 		refreshDB();
 	}
 
 	/**
 	 * Removes a tag from the snippet. If the given tag is null, nothing will be
 	 * done. If the snippet has not been tagged with the tag, nothing is done.
 	 * 
 	 * @param tag
 	 *            to be removed from the snippet
 	 */
 	public synchronized void removeTag(Tag tag) {
 		if (tag == null)
 			return;
 
 		if (!tags.contains(tag))
 			return;
 
 		tags.remove(tag);
 
 		refreshDB();
 
 	}
 
 	/**
 	 * @return the comments of the snippet
 	 */
 	public List<Comment> getComments() {
 		List<Comment> comments = null;
 
 		try {
 			comments = Persistence.getInstance().getComments(this);
 		} catch (IOException e) {
 			System.err
 					.println("IOException refreshing the comments of snippet \""
 							+ name + "\" (id=" + id + ": " + e.getMessage());
 			e.printStackTrace(System.err);
 		}
 		return comments;
 
 	}
 
 	/**
 	 * @return the counts of view
 	 */
 	public synchronized int getViewcount() {
 		return viewcount;
 	}
 
 	public synchronized void increaseViewCounter() {
 		viewcount++;
 		refreshDB();
 	}
 
 	/**
 	 * Adds a comment to the snippet. If the comment is null, nothing will be
 	 * done. If comment has already been added, nothing is done
 	 * 
 	 * @param comment
 	 *            to be added
 	 */
 	public void addComment(Comment comment) {
 		try {
 			if (comment == null)
 				return;
 			if (!this.equals(comment.getSnippet()))
 				throw new IOException(
 						"Comment owner not equals snippet to be added");
 
 			Persistence.instance.writeComment(comment, IPersistence.DB_DEFAULT);
 			refreshComments();
 
 		} catch (IOException e) {
 			Logging.printError("IOException during addComment(Comment object) "
 					+ e.getMessage(), e);
 			e.printStackTrace(Logging.err);
 		}
 	}
 
 	/**
 	 * Removes a comment from the snippet. If the comment is null, the nothing
 	 * is done. If the given comment is not in the comments list of the snippet
 	 * also the method returns without effect.
 	 * 
 	 * @param comment
 	 *            to be removed
 	 */
 	public void removeComment(Comment comment) {
 		if (comment == null)
 			return;
 
 		this.comments.remove(comment.getHashID());
 		refreshDB();
 	}
 
 	/**
 	 * Checks if the snippet has a given tag
 	 * 
 	 * @param tag
 	 *            to be checked
 	 * @return true if the snippet has been tagged with the given tag, otherwise
 	 *         false
 	 */
 	public boolean hasTag(Tag tag) {
 		if (tag == null)
 			return false;
 
 		// DO NOT USE tags.contains!
 
 		for (Tag item : tags)
 			if (item.equals(tag))
 				return true;
 		return false;
 	}
 
 	/**
 	 * @return the owner of the session
 	 */
 	public User getOwner() {
 		return User.getUser(owner);
 	}
 
 	/**
 	 * 
 	 * @return the total number of snippets in the system
 	 */
 	public static int totalCount() {
 		try {
 			return Persistence.instance.getSnippetsCount();
 		} catch (IOException e) {
 			System.err.println("IOException while totalCount() "
 					+ e.getMessage());
 			e.printStackTrace(System.err);
 			return 0;
 		}
 	}
 
 	/**
 	 * Invokes the refreshing process for the database
 	 */
 	protected synchronized void refreshDB() {
 		try {
 			Persistence.instance.writeSnippet(this, IPersistence.DB_DEFAULT);
 		} catch (IOException e) {
 			System.err.println("Error writing snippet " + name + " (" + id
 					+ "): " + e.getMessage());
 			e.printStackTrace(System.err);
 			return;
 		}
 	}
 
 	/**
 	 * Adds the given snippet to the database. If null, nothing happens
 	 * 
 	 * @param snippet
 	 *            to be added.
 	 */
 	static protected void addToDB(Snippet snippet) throws IOException {
 		if (snippet == null)
 			return;
 
 		snippet.id = Persistence.getInstance().writeSnippet(snippet,
 				IPersistence.DB_NEW_ONLY);
 	}
 
 	/**
 	 * Re-Read comments out of database
 	 */
 	protected final synchronized void refreshComments() {
 		// XXX Ugly hack although COW-method
 		try {
 			List<Comment> comments = Persistence.getInstance()
 					.getComments(this);
 			if (comments != null) {
 				List<Long> newComments = new ArrayList<Long>(comments.size());
 				for (Comment c : comments) {
 					newComments.add(c.getHashID());
 				}
 				this.comments = newComments;
 			}
 		} catch (IOException e) {
 			System.err
 					.println("IOException refreshing the comments of snippet \""
 							+ name + "\" (id=" + id + ": " + e.getMessage());
 			e.printStackTrace(System.err);
 		}
 	}
 
 	/**
 	 * Deletes the snippet
 	 */
 	public void delete() {
 		try {
 			Persistence.getInstance().removeSnippet(this,
 					IPersistence.DB_DEFAULT);
 		} catch (IOException e) {
 			System.err.println("IOException during delete of snippet \""
 					+ this.getName() + "\" (id=" + getHashId() + "): "
 					+ e.getMessage());
 			e.printStackTrace(System.err);
 		}
 	}
 
 	public List<String> getStringTags() {
 		List<String> result = new ArrayList<String>();
 		for (Tag tag : tags) {
 			result.add(tag.name);
 		}
 		return result;
 	}
 
 	public int getCommentCount() {
 		refreshComments();
 		return comments.size();
 	}
 
 	/**
 	 * Converts this snippet to a {@link XSnippet} parameter object
 	 * 
 	 * @return
 	 */
 	synchronized public XSnippet toXSnippet() {
 		XCategory category = new XCategory("Root", "Desc", "",
 				new ArrayList<String>());
 		XSnippet result = new XSnippet(owner, id, this.getName(), description,
 				this.category, new ArrayList<String>(getStringTags()),
 				code.getCode(), code.getHashID(), code.getFormattedHTML(),
 				code.getLanguage(), license, getViewcount());
 
 		result.rating = getAverageRating();
 
 		// Caution: Some settings (e.g. canEdit, isFavourite) are user or
 		// session specific and cannot be initialised here correctly.
 		// This initialisation is therefore considerer generic and safe but may
 		// not be correct
 		result.isFavorite = false;
 		result.canDelete = false;
 		result.canEdit = false;
 
 		return result;
 	}
 
 	/**
 	 * Creates a list with the shared {@link XComment} objects
 	 * 
 	 * @return
 	 */
 	ArrayList<XComment> getXComments() {
 		List<Comment> comments = getComments();
 		ArrayList<XComment> result = new ArrayList<XComment>(comments.size());
 
 		for (Comment comment : comments)
 			result.add(comment.toXComment());
 
 		return result;
 	}
 
 	/**
 	 * Creates a comment and adds the comment to this snippet. If the given
 	 * message string or the owner is null, nothing happens.
 	 * 
 	 * @param message
 	 *            Comment message. Must not be null or empty
 	 * @param owner
 	 *            Comment owner. Must not be null
 	 * @return the created comment or null, if the arguments are empty
 	 * @throws IOException
 	 *             Thrown, if occurring during creation of the comment
 	 */
 	public synchronized Comment addComment(String message, User owner)
 			throws IOException {
 		if (message == null || message.isEmpty() || owner == null)
 			return null;
 		Comment result = Comment.createComment(owner.getUsername(),
 				this.getHashId(), message);
 		return result;
 	}
 
 	/**
 	 * @return the id hash code of the snippet
 	 * @deprecated because of names convention. Use getHashID() instant.
 	 */
 	@Deprecated
 	public Long getHash() {
 		return id;
 	}
 
 	/**
 	 * @return the id hash code of the snippet
 	 */
 	public Long getHashId() {
 		return id;
 	}
 
 	/**
 	 * Sets the internal code object without writing it to the DB.
 	 * 
 	 * This call is temporary for the DB. Do not USE IT UNLESS YOU KNOW WHAT YOU
 	 * ARE DOING HERE.
 	 * 
 	 * @param fetchNewestCode
 	 *            new code object to fetch
 	 */
 	@Deprecated
 	public void setCodeWithoutWriting(Code fetchNewestCode) {
 		this.code = fetchNewestCode;
 	}
 
 	/**
 	 * @return the username of the owner
 	 */
 	public String getOwnerUsername() {
 		return owner;
 	}
 
 	/**
 	 * @return the category name
 	 */
 	public String getCategoryName() {
 		return this.category;
 	}
 
 	/**
 	 * Removes a rating from a given user. If the user is null, nothing happens
 	 * 
 	 * @param user
 	 *            the rating should be removed from
 	 */
 	public void unrate(User user) {
 		if (user == null)
 			return;
 		try {
 			Persistence.getInstance().unRate(user, this,
 					IPersistence.DB_DEFAULT);
 		} catch (IOException e) {
 			System.err.println("IOException during unrating of snippet "
 					+ getHashId() + " of user " + user.getUsername() + ": "
 					+ e.getMessage());
 			e.printStackTrace(System.err);
 		}
 	}
 
 	/**
 	 * Set the rating of this snippet. If the rating is zero, the rating is
 	 * removed. If the rating is not between 0 and 5, the rating is ignored and
 	 * nothing is done.
 	 * 
 	 * @param user
 	 *            that gives this rating. IF null, nothing is done
 	 * @param rate
 	 *            must be between 0 and 5. If 0, the rating is removed. If
 	 *            between 1 and 5, the according rating is set
 	 */
 	public void setRating(User user, int rate) {
 		if (user == null || rate < 0 || rate > 5)
 			return;
 
 		if (rate == 0)
 			unrate(user);
 		else {
 			try {
 				Persistence.getInstance().writeRating(rate, this, user,
 						IPersistence.DB_DEFAULT);
 			} catch (IOException e) {
 				System.err.println("IOException during rating of snippet "
 						+ getHashId() + " to score=" + rate + " of user "
 						+ user.getUsername() + ": " + e.getMessage());
 				e.printStackTrace(System.err);
 			}
 		}
 	}
 
 	/**
 	 * @return the snippet of the day
 	 */
 	public synchronized static Snippet getSnippetOfDay() {
 		if (snippetOfDay != null) {
 			// Check if it is fresh
 			long delay = System.currentTimeMillis() - snippetOfDayRefreshTime;
 			if (delay < 1000 * 60 * 60 * 24)
 				return snippetOfDay;
 		}
 
 		try {
 			snippetOfDay = Persistence.getInstance().getRandomSnippet(
 					Math.random());
 			snippetOfDayRefreshTime = System.currentTimeMillis();
 		} catch (IOException e) {
 			System.err.println("IOException fetching randomized snippet: "
 					+ e.getMessage());
 			e.printStackTrace(System.err);
 		}
 
 		return snippetOfDay;
 	}
 
 	/**
 	 * Edits the current snippet to the values, given with the XSnippet object.
 	 * 
 	 * If the hash id does not match, a {@link IllegalArgumentException} is
 	 * thrown If the given user is not found, a {@link IllegalArgumentException}
 	 * is thrown
 	 * 
 	 * <b>CAUTION</b> The code is not affected by this method!!</b>
 	 * 
 	 * @param snippet
 	 *            Data to be edited
 	 * @throws IllegalArgumentException
 	 *             Thrown, if an argument is illegal
 	 */
 	public void edit(XSnippet snippet) throws IllegalArgumentException {
 		/* Check snippet data that must be congruent */
 		if (snippet == null)
 			return;
 		if (snippet.hash != getHashId())
 			throw new IllegalArgumentException("Snippet hash id doesn't match");
 
 		User owner = User.getUser(snippet.owner);
 		if (owner == null)
 			throw new IllegalArgumentException("Illegal owner (not found)");
 		Category category = Category.getCategory(snippet.category);
 		if (category == null)
 			throw new IllegalArgumentException("Illegal category: not found");
 
 		// NOTE: WE DO NOT CHANGE THE CODE HERE!!
 
 		/* Data is checked - Now set data */
 		this.name = snippet.title;
 		this.category = category.getName();
 		this.description = snippet.description;
 		this.license = snippet.license;
 
 		/* Write out */
 		refreshDB();
 	}
 
 	/**
 	 * 
 	 * @return get the source code filename of the snippet, or null if no source
 	 *         is attached to the snippet
 	 */
 	public String getSourceFilename() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/**
 	 * Creates a new source code filename for the snippet. The snippet is after
 	 * this call associated to the new source code filename. A file is not
 	 * created, that has to be done outside this method
 	 * 
 	 * @return the filename of the new file
 	 */
 	public String createNewSourceFilename() {
 		return "snippet_source_file_" + getHashId();
 	}
 
 	/**
 	 * @return the languages which are tagged as default
 	 */
 	public static List<String> getSupportedLanguages() {
 		List<String> result;
 		try {
 			result = Persistence.instance
 					.getLanguages(IPersistence.LANGUAGE_GET_DEFAULTS);
 		} catch (IOException e) {
 			result = new ArrayList<String>(0);
 		}
 		return result;
 	}
 
 	/**
 	 * @return all available languages
 	 */
 	public static List<String> getAllLanguages() {
 		List<String> result;
 		try {
 			result = Persistence.instance
 					.getLanguages(IPersistence.LANGUAGE_GET_ALL);
 		} catch (IOException e) {
 			result = new ArrayList<String>(0);
 		}
 		return result;
 	}
 
 	/**
 	 * @return all languages which are not returned by
 	 *         {@link #getSupportedLanguages()}
 	 */
 	public static List<String> getNonDefaultLanguages() {
 		List<String> result;
 		try {
 			result = Persistence.instance
 					.getLanguages(IPersistence.LANGUAGE_GET_OTHERS);
 		} catch (IOException e) {
 			result = new ArrayList<String>(0);
 		}
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj == null)
 			return false;
 		if (obj instanceof Snippet) {
 			Snippet snippet = (Snippet) obj;
 			return (snippet.getHashId() == this.getHashId());
 		} else
 			return false;
 	}
 }
