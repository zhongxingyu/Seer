 package models;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 
 /**
  * A {@link Entry} containing a question as <code>content</code>, {@link Answer}
  * s and {@link Comments}.
  * 
  * @author Simon Marti
  * @author Mirco Kocher
  * 
  */
 public class Question extends Entry implements IObservable {
 
 	private IDTable<Answer> answers;
 	private IDTable<Comment> comments;
 	private final int id;
 
 	private Answer     bestAnswer;
 	private Calendar   settingOfBestAnswer;
 	private final ArrayList<Tag> tags = new ArrayList<Tag>();
 	
 	protected HashSet<IObserver> observers;
 
 
 	/**
 	 * Create a Question.
 	 * 
 	 * @param owner
 	 *            the {@link User} who posted the <code>Question</code>
 	 * @param content
 	 *            the question
 	 */
 	public Question(User owner, String content) {
 		this(owner, content, null);
 	}
 
 	public Question(User owner, String content, IDTable<Question> database) {
 		super(owner, content);
 		this.answers = new IDTable<Answer>();
 		this.comments = new IDTable<Comment>();
		this.observers = new HashSet<IObserver>();
 		this.id = database != null ? database.add(this) : -1;
 	}
 
 	/**
 	 * Unregisters all {@link Answer}s, {@link Comment}s, {@link Vote}s,
 	 * {@link Tag}s and itself.
 	 */
 	@Override
 	public void unregister() {
 		Collection<Answer> answers = this.answers.values();
 		Collection<Comment> comments = this.comments.values();
 		this.answers = new IDTable<Answer>();
 		this.comments = new IDTable<Comment>();
 		for (Answer answer : answers)
 			answer.unregister();
 		for (Comment comment : comments)
 			comment.unregister();
 		this.observers.clear();
 		if (this.id != -1)
 			questions.remove(this.id);
 		this.unregisterVotes();
 		this.unregisterUser();
 		this.setTagString("");
 	}
 
 	/**
 	 * Unregisters a deleted {@link Answer}.
 	 * 
 	 * @param answer
 	 *            the {@link Answer} to unregister
 	 */
 	public void unregister(Answer answer) {
 		this.answers.remove(answer.id());
 	}
 
 	/**
 	 * Unregisters a deleted {@link Comment}.
 	 * 
 	 * @param comment
 	 *            the {@link Comment} to unregister
 	 */
 	@Override
 	public void unregister(Comment comment) {
 		this.comments.remove(comment.id());
 	}
 
 	/**
 	 * Post a {@link Answer} to a <code>Question</code>
 	 * 
 	 * @param user
 	 *            the {@link User} posting the {@link Answer}
 	 * @param content
 	 *            the answer
 	 * @return an {@link Answer}
 	 */
 	public Answer answer(User user, String content) {
 		Answer answer = new Answer(this.answers.nextID(), user, this, content);
 		this.answers.add(answer);
 		return answer;
 	}
 
 	/**
 	 * Post a {@link Comment} to a <code>Question</code>
 	 * 
 	 * @param user
 	 *            the {@link User} posting the {@link Comment}
 	 * @param content
 	 *            the comment
 	 * @return an {@link Comment}
 	 */
 	public Comment comment(User user, String content) {
 		Comment comment = new Comment(this.comments.nextID(), user, this,
 				content);
 		this.comments.add(comment);
 		return comment;
 	}
 
 	/**
 	 * Checks if a {@link Answer} belongs to a <code>Question</code>
 	 * 
 	 * @param answer
 	 *            the {@link Answer} to check
 	 * @return true if the {@link Answer} belongs to the <code>Question</code>
 	 */
 	public boolean hasAnswer(Answer answer) {
 		return this.answers.contains(answer);
 	}
 
 	/**
 	 * Checks if a {@link Comment} belongs to a <code>Question</code>
 	 * 
 	 * @param comment
 	 *            the {@link Comment} to check
 	 * @return true if the {@link Comment} belongs to the <code>Question</code>
 	 */
 	public boolean hasComment(Comment comment) {
 		return this.comments.contains(comment);
 	}
 
 	/**
 	 * Get the <code>id</code> of the <code>Question</code>. The <code>id</code>
 	 * does never change.
 	 * 
 	 * @return id of the <code>Question</code>
 	 */
 	public int id() {
 		return this.id;
 	}
 
 	/**
 	 * Get all {@link Answer}s to a <code>Question</code>
 	 * 
 	 * @return {@link Collection} of {@link Answers}
 	 */
 	public List<Answer> answers() {
 		List<Answer> list = new ArrayList<Answer>(answers.values());
 		Collections.sort(list);
 		return Collections.unmodifiableList(list);
 	}
 
 	/**
 	 * Get all {@link Comment}s to a <code>Question</code>
 	 * 
 	 * @return {@link Collection} of {@link Comments}
 	 */
 	public List<Comment> comments() {
 		List<Comment> list = new ArrayList<Comment>(comments.values());
 		Collections.sort(list);
 		return Collections.unmodifiableList(list);
 	}
 
 	/**
 	 * Get a specific {@link Answer} to a <code>Question</code>
 	 * 
 	 * @param id
 	 *            of the <code>Answer</code>
 	 * @return {@link Answer} or null
 	 */
 	public Answer getAnswer(int id) {
 		return this.answers.get(id);
 	}
 
 	/**
 	 * Get a specific {@link Comment} to a <code>Question</code>
 	 * 
 	 * @param id
 	 *            of the <code>Comment</code>
 	 * @return {@link Comment} or null
 	 */
 	public Comment getComment(int id) {
 		return this.comments.get(id);
 	}
 
 	public boolean isBestAnswerSettable(Calendar now) {
 		Calendar thirtyMinutesAgo = ((Calendar) now.clone());
 		thirtyMinutesAgo.add(Calendar.MINUTE, -30);
 		return this.settingOfBestAnswer == null
 				|| !thirtyMinutesAgo.getTime().after(
 						this.settingOfBestAnswer.getTime());
 	}
 
 	/**
 	 * Sets the best answer. This answer can not be changed after 30min. This
 	 * Method enforces this and fails if it can not be set.
 	 * 
 	 * @param bestAnswer
 	 *            the answer the user chose to be the best for this question.
 	 * @return true if setting of best answer was allowed.
 	 */
 	public boolean setBestAnswer(Answer bestAnswer) {
 		Calendar now = Calendar.getInstance();
 		return setBestAnswer(bestAnswer, now);
 	}
 
 	public boolean setBestAnswer(Answer bestAnswer, Calendar now) {
 		if (this.isBestAnswerSettable(now)) {
 			this.bestAnswer = bestAnswer;
 			this.settingOfBestAnswer = now;
 			return true;
 		} else
 			return false;
 	}
 
 	public Answer getBestAnswer() {
 		return bestAnswer;
 	}
 
 	/**
 	 * @param tags
 	 *            a comma- or whitespace-separated list of tags to be associated
 	 *            with this question
 	 */
 	public void setTagString(String tags) {
 		for (Tag tag : this.tags)
 			tag.unregister(this);
 		this.tags.clear();
 
 		if (tags == null)
 			return;
 
 		String bits[] = tags.split("[\\s,]+");
 		for (String bit : bits) {
 			// make the tag conform to Tag.tagRegex
 			bit = bit.toLowerCase();
 			if (bit.length() > 32)
 				bit = bit.substring(0, 32);
 
 			Tag tag = Tag.get(bit);
 			if (tag != null && !this.tags.contains(tag)) {
 				this.tags.add(tag);
 				tag.register(this);
 			}
 		}
 		Collections.sort(this.tags);
 	}
 
 	public ArrayList<Tag> getTags() {
 		return (ArrayList<Tag>) this.tags.clone();
 	}
 
 	/**
 	 * @see models.IObservable#addObserver(models.IObserver)
 	 */
 	public void addObserver(IObserver o) {
 		if (o == null)
 			throw new IllegalArgumentException();
 		this.observers.add(o);
 	}
 
 	/**
 	 * @see models.IObservable#hasObserver(models.IObserver)
 	 */
 	public boolean hasObserver(IObserver o) {
 		return this.observers.contains(o);
 	}
 
 	/**
 	 * @see models.IObservable#removeObserver(models.IObserver)
 	 */
 	public void removeObserver(IObserver o) {
 		this.observers.remove(o);
 	}
 
 	/**
 	 * @see models.IObservable#notifyObservers(java.lang.Object)
 	 */
 	public void notifyObservers(Object arg) {
 		for (IObserver o : this.observers)
 			o.observe(this, arg);
 	}
 
 	/*
 	 * Static interface to access questions from controller (not part of unit
 	 * testing)
 	 */
 
 	private static IDTable<Question> questions = new IDTable();
 
 	public static Question register(User owner, String content) {
 		return new Question(owner, content, questions);
 	}
 
 	/**
 	 * Get a <@link Collection} of all <code>Questions</code>.
 	 * 
 	 * @return all <code>Questions</code>
 	 */
 	public static List<Question> questions() {
 		List<Question> list = new ArrayList<Question>(questions.values());
 		Collections.sort(list);
 		return list;
 	}
 
 	/**
 	 * Get the <code>Question</code> with the given id.
 	 * 
 	 * @param id
 	 * @return a <code>Question</code> or null if the given id doesn't exist.
 	 */
 	public static Question get(int id) {
 		return questions.get(id);
 	}
 
 	/*
 	 * Interface to access statistical data of Questions
 	 */
 
 	/**
 	 *Get all high rated answers in the system
 	 * 
 	 * @return ArrayList<Answer> an arraylist of all high rated answers
 	 */
 	public static ArrayList<Answer> getHighRatedAnswers() {
 		ArrayList<Answer> answers = new ArrayList<Answer>();
 		for (Question q : questions) {
 			for (Answer a : q.answers) {
 				if (a.isHighRated()) {
 					answers.add(a);
 				}
 			}
 		}
 		return answers;
 	}
 
 	/**
 	 *Get all best answers in the system
 	 * 
 	 * @return ArrayList<Answer> an arrayList of all best answers
 	 */
 	public static ArrayList<Answer> getBestRatedAnswers() {
 		ArrayList<Answer> answers = new ArrayList<Answer>();
 		for (Question q : questions) {
 			if (q.bestAnswer != null) {
 				answers.add(q.bestAnswer);
 			}
 		}
 		return answers;
 	}
 
 	/**
 	 *Get all answers in the system
 	 * 
 	 * @return ArrayList<Answer> an arrayList of all answers
 	 */
 	public static ArrayList<Answer> getAnswers() {
 		ArrayList<Answer> answers = new ArrayList<Answer>();
 		for (Question q : Question.questions) {
 			for (Answer a : q.answers) {
 				answers.add(a);
 			}
 		}
 		return answers;
 	}
 }
