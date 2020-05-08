 package models;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 /**
  * The Class Question delivers all functionality of the questions that other
  * votables don't have (those would be located in the class @see Votable.java.
  */
 public class Question extends Post {
 
 	/** The best answer to this question. */
 	private Answer bestAnswer;
 
 	/** The time when the best answer has been defined. */
 	private Date bestAnswerSetTime;
 
 	/** The tags of this question. */
 	private ArrayList<String> tags = new ArrayList<String>();
 
 	private Date lastChangedDate;
 	private String title;
 
 	/**
 	 * Instantiates a new question.
 	 * 
 	 * @param content
 	 *            - The content of the question.
 	 * @param questionOwner
 	 *            - The user who asked the question.
 	 * @param addQuestionToList
 	 *            - true if the new Question shall be added to the Answer list
 	 *            and false otherwise.
 	 */
 	public Question(Boolean addQuestionToList, String content, String title,
 			User questionOwner) {
 		this.owner = questionOwner;
 		this.editedBy = questionOwner;
 		this.content = content;
 		this.score = 0;
 		this.title = title;
 
		stripImageTags();

 		date = new Date();
 		lastChangedDate = new Date();
 		if (addQuestionToList) {
 			oldVersions = new ArrayList<Post>();
 			manager.addQuestion(this);
 			questionOwner.addActivity("Asked question <" + content + ">");
 		}
 	}
 
 	/**
 	 * Instantiates a new question and adds it to the question list.
 	 * 
 	 * @param content
 	 *            - The content of the question
 	 * @param questionOwner
 	 *            - The user who asked the question.
 	 */
 	public Question(String content, String title, User questionOwner) {
 		new Question(true, content, title, questionOwner);
 	}
 
 	/**
 	 * Checks if an answer has been selected as the best answer.
 	 * 
 	 * @return - true if the best answer has been selected of false if no answer
 	 *         is set as best one.
 	 */
 	public boolean hasBestAnswer() {
 		return bestAnswer != null;
 	}
 
 	/**
 	 * Checks if you can still change the choice of the best answer. This is
 	 * possible in a timespan of 30 min after the first choice.
 	 * 
 	 * @return - true if best answer was set less than 30 min ago or no best
 	 *         answer is set yet, false otherwise.
 	 */
 	public boolean bestAnswerChangeable() {
 		long now = new Date().getTime();
 		long then = hasBestAnswer() ? bestAnswerSetTime.getTime() : now;
 		long diff = now - then; // time difference in ms
 
 		return (diff / (1000 * 60)) < 30;
 	}
 
 	/**
 	 * Adds tags to the question separated by spaces and also adds new Tags to
 	 * the tag-List in the manager.
 	 * 
 	 * @param tags
 	 *            - A string containing all tags separated by spaces
 	 */
 	public void addTags(String tags) {
 		String delimiter = "[ ]+";
 		tags = tags.toLowerCase();
 		if (!tags.equals(" ") && !tags.equals("")) {
 			for (String newTag : tags.split(delimiter)) {
 				// Leerzeichen entfernen
 				newTag = newTag.replace(" ", "");
 				if (!this.tags.contains(newTag)) {
 					this.tags.add(newTag);
 				}
 				if (!manager.getTagList().contains(newTag.toLowerCase())) {
 					manager.addTag(newTag.toLowerCase());
 				}
 			}
 		}
 	}
 
 	/**
 	 * Adds all tags in the ArrayList to the question and also adds new Tags to
 	 * the tag-List in the manager.
 	 * 
 	 * @param tags
 	 *            - An ArrayList of Strings containing individual tags.
 	 */
 	public void addTags(ArrayList<String> tags) {
 		this.tags.addAll(tags);
 	}
 
 	/**
 	 * Checks whether some tags from the parameter 'tags' are similar to some
 	 * that have already been entered previously using the Levenshtein-distance.
 	 * 
 	 * @param tags
 	 *            - the String of tags - separated by spaces - that are to be
 	 *            checked.
 	 * @return - A string containing all tags that already exist, separated by a
 	 *         space and each starting with a '#'.
 	 */
 	public static String checkTags(String tags) {
 		String delimiter = "[ ]+";
 		// The minimum Levenshtein distance two strings need to have.
 		int minDistance = 2;
 		String storeTags = "" + tags;
 		tags = tags.toLowerCase();
 		String existingTags = new String();
 		for (String newTag : tags.split(delimiter)) {
 			for (String existingTag : manager.getTagList()) {
 				if (models.algorithms.Levenshtein.getLevenshteinDistance(
 						newTag.toLowerCase(), existingTag) <= minDistance
 						&& !manager.getTagList().contains(newTag)) {
 					existingTags = existingTags + "#" + existingTag + " ";
 				}
 			}
 		}
 		tags = storeTags;
 		return existingTags;
 	}
 
 	/**
 	 * Searches for questions that have similar tags and similar content and
 	 * returns the top 3.
 	 * 
 	 * @return - An ArrayList with the top 3 similar Questions (as type: Post)
 	 */
 	public ArrayList<Post> similarQuestions() {
 		ArrayList<Post> similar = new ArrayList<Post>();
 		// Search similar questions
 		SearchManager searchManager = new SearchManager(this.getTagsString()
 				+ " " + this.getContent());
 
 		// Fill the list of similar questions with different search results.
 		int i = 0;
 		while (i < searchManager.getSearchResults().size()
 				&& similar.size() < 3) {
 			if (!similar.contains(searchManager.getSearchResults().get(i)
 					.getQuestion())
 					&& !this.equals(searchManager.getSearchResults().get(i)
 							.getQuestion()))
 				similar.add(0, searchManager.getSearchResults().get(i)
 						.getQuestion());
 			i++;
 		}
 		return similar;
 	}
 
 	/**
 	 * Edits a question and adds the actual contents and tags to the list of
 	 * older versions.
 	 * 
 	 * @param content
 	 *            - the new content of the question
 	 * @param tags
 	 *            - the new tags
 	 * @param uname
 	 *            - the name of the user who adds the new version.
 	 */
 	public void addVersion(String content, String tags, String uname) {
 		Question question = new Question(false, this.content, this.title,
 				this.owner);
 		question.addTags(this.getTags());
 		question.setEditor(this.getEditor().getName());
 		question.isVoteable = false;
 
 		this.oldVersions.add(0, question);
 		super.setContent(content, uname);
 		this.setTags("" + tags);
 		manager.getUserByName(uname).addActivity(
 				"Edited Question " + this.id + " by writing: <" + content
 						+ ">.");
 		this.setLastChanged(getDate());
 	}
 
 	public void restoreOldVersion(String oldContent, String oldTags,
 			String uname) {
 		addVersion(oldContent, oldTags, uname);
 	}
 
 	/**
 	 * Notifies the owner of this question about its change.
 	 */
 	public void notifyChange() {
 		this.getOwner().notifyChange("Your question was answered", this);
 	}
 
 	/** Getters */
 	public String getTitle() {
 		return title;
 	}
 
 	public String getTagByIndex(int i) {
 		return tags.get(i);
 	}
 
 	public Answer getBestAnswer() {
 		return bestAnswer;
 	}
 
 	public ArrayList<String> getTags() {
 		return this.tags;
 	}
 
 	public String getTagsString() {
 		StringBuffer result = new StringBuffer();
 		for (int i = 0; i < tags.size(); i++) {
 			result.append(tags.get(i) + " ");
 		}
 		return result.toString();
 	}
 
 	/**
 	 * Gets all Comments which belongs to this question
 	 * 
 	 * @return - a sorted list of comments
 	 */
 	public ArrayList<Comment> getComments() {
 		return manager.getAllCommentsByQuestionIdSortedByDate(this.getId());
 	}
 
 	/**
 	 * Gets a comment to this question by the id - cid
 	 * 
 	 * @param cid
 	 * @return
 	 */
 	public Comment getCommentbyId(int cid) {
 		if (manager.getCommentById(cid).getCommentedPost().equals(this)) {
 			return manager.getCommentById(cid);
 		}
 		return null;
 	}
 
 	/**
 	 * Gets all answers which belongs to this question
 	 * 
 	 * @return
 	 */
 	public ArrayList<Answer> getAnswers() {
 		return manager.getAllAnswersByQuestionId(this.getId());
 	}
 
 	/**
 	 * Gets the date of the last change (means adding of answer, comment, vote)
 	 * 
 	 */
 	public Date getLastChangedDate() {
 		return this.lastChangedDate;
 	}
 
 	/** Setters */
 
 	/**
 	 * Sets date when answer has last changed.
 	 * 
 	 * @param date
 	 *            - the date when the answer has been changed.
 	 */
 	public void setLastChanged(Date date) {
 		this.lastChangedDate = date;
 	}
 
 	/**
 	 * Deletes all previous tags and adds the ones in the String 'tags'.
 	 * 
 	 * @param tags
 	 *            - A String object containing the tags to be set.
 	 */
 	public void setTags(String tags) {
 		this.tags = new ArrayList<String>();
 		this.addTags(tags);
 	}
 
 	/**
 	 * Set an answer as the best answer to this question.
 	 * 
 	 * @param answer
 	 *            - the answer that is best answering this question.
 	 */
 	public void setBestAnswer(Answer answer) {
 		if (bestAnswerChangeable() && answer.belongsToQuestion(id)) {
 			if (hasBestAnswer()) {
 				bestAnswer.markAsBestAnswer(false);
 			}
 
 			answer.markAsBestAnswer(true);
 			bestAnswerSetTime = new Date();
 			bestAnswer = answer;
 		}
 	}
 }
