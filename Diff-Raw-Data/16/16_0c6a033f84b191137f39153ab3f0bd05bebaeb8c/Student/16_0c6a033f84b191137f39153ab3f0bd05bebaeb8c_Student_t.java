 package edu.umw.cpsc.magpie.core;
 
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.Map;
 import java.util.HashMap;
 import java.sql.ResultSet;
 import java.sql.Date;
 import edu.umw.cpsc.magpie.util.DateHelper;
 import org.apache.log4j.Logger;
 
 public class Student extends User implements Comparable<Student> {
 	private static Logger log = Logger.getLogger(Student.class);
 
 	private final int SECOND_MS = 1000; // number of milliseconds in one second
 	private final int MINUTE_MS = 60 * SECOND_MS; // Number of milliseconds in one minute
 	private final int MAX_GAP = 5 * MINUTE_MS; // Maximum gap in between reviews that is counted in studying time, in milliseconds
 
 	private GradingGroup gradingGroup;
 	private ReviewList reviews = new ReviewList();
 	private Map<Integer, NextReview> nextReviews = Collections.synchronizedMap(new HashMap<Integer, NextReview>());
 	private List<DoneMarker> doneMarkers = Collections.synchronizedList(new ArrayList<DoneMarker>());
 	private List<TroubleMarker> troubleMarkers = Collections.synchronizedList(new ArrayList<TroubleMarker>());
 
 	private Hand hand = new Hand(this);
 	private Card lastCard;
 
 	public Student(ResultSet resultSet) {
 		super(resultSet);
 
 		try {
 			String group = resultSet.getString("gradingGroup");
 			gradingGroup = group.equals("score") ? GradingGroup.SCORE : 
                 (group.equals("timer") ? 
                     GradingGroup.TIMER : GradingGroup.MULT_CHOICE);
 
 			ResultSet reviewRs = MagpieConnection.instance().executeQuery("SELECT * FROM review WHERE uid='" + getId() + "'");
 			while (reviewRs.next())
 				reviews.add(new Review(reviewRs));
 
 			ResultSet nextReviewRs = MagpieConnection.instance().executeQuery("SELECT * FROM nextReview WHERE uid='" + getId() + "'");
 			while (nextReviewRs.next())
 				nextReviews.put(nextReviewRs.getInt("cid"), new NextReview(nextReviewRs));
 
 			ResultSet doneMarkerRs = MagpieConnection.instance().executeQuery("SELECT * FROM doneMarker WHERE uid='" + getId() + "'");
 			while (doneMarkerRs.next())
 				doneMarkers.add(new DoneMarker(doneMarkerRs));
 
 			ResultSet troubleMarkerRs = MagpieConnection.instance().executeQuery("SELECT * FROM troubleMarker WHERE uid='" + getId() + "'");
 			while (troubleMarkerRs.next())
 				troubleMarkers.add(new TroubleMarker(troubleMarkerRs));
 		} catch (Exception e) {
 			e.printStackTrace(System.err);
 		}
 	}
 
 	public Student(String username, String password, String firstName, String lastName, GradingGroup gradingGroup) {
 		super(username, password, firstName, lastName);
 
 		this.gradingGroup = gradingGroup;
 
 		insert();
 	}
 
 	public ReviewList getReviewsClone() {
 		return reviews.clone();
 	}
 
 	public Role getRole() {
 		return Role.STUDENT;
 	}
 
 	public GradingGroup getGradingGroup() {
 		return gradingGroup;
 	}
 
 	public synchronized Card getNextCard() {
 		log.debug("Getting next card for '" + username + "'");
 
 		Random random = new Random();
 
 		ArrayList<Card> scheduledCards = getActiveScheduledCards();
 		log.debug("Found " + scheduledCards.size() + " scheduled cards for '" + username + "'");
 
 		if (scheduledCards.size() > 0) {
 			if(scheduledCards.size() > 1 && lastCard != null)
 				scheduledCards.remove(lastCard);
 			
 			lastCard = scheduledCards.get(random.nextInt(scheduledCards.size()));
 			return lastCard;
 		}
 
 		ArrayList<Card> unmemorizedCards = getActiveUnmemorizedCards();
 		log.debug("Found " + unmemorizedCards.size() + " unmemorized cards for '" + username + "'");
 
 		hand.refillFrom(unmemorizedCards);
 		if(hand.hasNext()) {
 			lastCard = hand.next(lastCard);
 			return lastCard;
 		}
 
 		//log.debug("'" + username + "' has finished his/her cards. Checking for DoneMarker...");
 		if (!finishedToday()) {
 			log.debug("No DoneMarker for '" + username + "' today. Inserting new DoneMarker...");
 			doneMarkers.add(new DoneMarker(this, DateHelper.today()));
 		}
 
         // Spring 2011 - make all students complete the entire 15 minutes,
         // filling the remainder with random cards if necessary.
         // log.debug("Random!");
         // return getRandomActiveCard();
         return null;
 	}
 
 	public Review getLastReviewFor(Card card) {
 		ReviewList cardReviews = reviews.getFor(card);
 
 		if (cardReviews.size() == 0)
 			return null;
 
 		cardReviews.sortByDateTime();
 
 		return cardReviews.get(cardReviews.size() - 1);
 	}
 
 	public double getEasinessFor(Card card) {
 		return reviews.getFor(card).getEasiness();
 	}
 
 	public void gradeCard(int cid, boolean success, int time) {
 		gradeCard(cid, -1, success, time);
 	}
 
 	public void gradeCard(int cid, int grade, boolean success, int time) {
 		log.debug("Grading card '" + cid + "' for '" + getUsername() + "'");
 
 		Review review = new Review(id, cid, grade, success, time);
 		reviews.add(review);
 
 		reschedule(cid);
 	}
 
 	private void reschedule(int cid) {
 		Card card = CardManager.instance().get(cid);
 		Date date = reviews.getFor(card).getNextDate();
 
 		if (date == null) {
 			NextReview nextReview = nextReviews.get(card.getId());
 			if (nextReview != null) {
 				nextReview.remove();
 				nextReviews.remove(card.getId());
 			}
 		} else {
 			NextReview nextReview = new NextReview(this, card, date);
 			nextReviews.put(card.getId(), nextReview);
 		}
 	}
 
 	public synchronized ArrayList<Card> getActiveUnmemorizedCards() {
 		ArrayList<Card> matches = new ArrayList<Card>();
 
 		for(Deck deck : getActiveDecks()) {
 			for (Card card : deck.getCards()) {
 				if (!hasMemorized(card))
 					matches.add(card);
 			}
 		}
 
 		return matches;
 	}
 
 	public synchronized ArrayList<Card> getActiveScheduledCards() {
 		ArrayList<Card> matches = new ArrayList<Card>();
 
 		for(Deck deck : getActiveDecks()) {
 			for(Card card : deck.getCards()) {
 				if (hasScheduled(card))
 					matches.add(card);
 			}
 		}
 
 		return matches;
 	}
 
 	public synchronized ArrayList<Card> getNewCards() {
 		ArrayList<Card> matches = new ArrayList<Card>();
 
 		for(Deck deck : getActiveDecks()) {
 			for(Card card : deck.getCards()) {
 				if (!hasSeenBefore(card))
 					matches.add(card);
 			}
 		}
 
 		return matches;
 	}
 
 	public synchronized ArrayList<Card> getRecentlyMissedCards() {
 		ArrayList<Card> matches = new ArrayList<Card>();
 
 		for(Deck deck : getActiveDecks()) {
 			for(Card card : deck.getCards()) {
 				if (hasMissedRecently(card))
 					matches.add(card);
 			}
 		}
 
 		return matches;
 	}
 
 	public boolean hasMemorized(Card card) {
 		Review review = getLastReviewFor(card);
 		if (review == null)
 			return false;
 
 		return review.getSuccess();
 	}
 
 	public boolean hasSeenBefore(Card card) {
 		Review review = getLastReviewFor(card);
 		if (review == null)
 			return false;
         else
 			return true;
 	}
 
 	public boolean hasMissedRecently(Card card) {
 		Review review = getLastReviewFor(card);
 		if (review == null)
 			return false;
 
 		return !review.getSuccess();
 	}
 
 	public boolean hasScheduled(Card card) {
 		NextReview nextReview = nextReviews.get(card.getId());
 
 		if (nextReview == null)
 			return false;
 
 		return DateHelper.todayOrEarlier(nextReview.getNextDate());
 	}
 
 	public boolean finishedToday() {
 		return finishedOn(new java.util.Date());
 	}
 
 	public synchronized boolean finishedOn(java.util.Date date) {
 		for(DoneMarker doneMarker : doneMarkers) {
 			if (DateHelper.onSameDay(doneMarker.getDoneDate(), date))
 				return true;
 		}
 
 		return false;
 	}
 
 	public synchronized boolean troubleOn(java.util.Date date) {
 		for(TroubleMarker troubleMarker : troubleMarkers) {
 			if (DateHelper.onSameDay(troubleMarker.getDate(), date))
 				return true;
 		}
 
 		return false;
 	}
 
 	public int getTime() {
 		return getTime(DateHelper.today().getTime());
 	}
 
 	public int getTime(java.util.Date date) {
 		ReviewList dateReviews = reviews.getFor(date);
 		dateReviews.sortByDateTime();
 
 		long totalTime = 0;
 		for (int i = 1; i < dateReviews.size(); i++) {
 			long gap = dateReviews.get(i).getTimeSince(dateReviews.get(i - 1));
 			if (gap < MAX_GAP)
 				totalTime += gap;
 		}
 
 		return (int) ((totalTime + (MINUTE_MS/2)) / MINUTE_MS);
 	}
 
    public int getNumSuccessfulReviews(java.util.Date date) {
		ReviewList dateReviews = reviews.getFor(date);

		int totalSuccessful = 0;
		for (int i = 0; i < dateReviews.size(); i++) {
            if (dateReviews.get(i).getSuccess()) {
                totalSuccessful++;
            }
        }
        return totalSuccessful;
    }

    public int getNumTotalReviews(java.util.Date date) {
		return reviews.getFor(date).size();
    }

 	public double getAverageTime(Course course) {
 		// returns the time spent studying DURING a course, regardless of what material was actually being studied
 		double totalTime = 0;
 
 		Calendar calendar = DateHelper.stripTime(course.getStartDate());
 
 		while(DateHelper.daysBetween(calendar.getTime(), course.getEndDate()) >= 0)  {
 			totalTime += getTime(calendar.getTime());
 			calendar.add(Calendar.DATE, 1);
 		}
 
 		return totalTime / course.getLength();
 	}
 
 	public String getInsertValues() {
 		return "(username, password, firstname, lastname, role, gradingGroup) VALUES ('" +
 			username + "', SHA1('" + password + "'), '" + firstName + "', '" + lastName + "', 'student', '" + gradingGroup.toDbString() + "')";
 	}
 
 	public int compareTo(Student other) {
 		int lastNameComp = lastName.compareTo(other.lastName);
 		return lastNameComp != 0 ? lastNameComp : firstName.compareTo(other.firstName);
 	}
 
 	public void addReview(Review review) {
 		// Only used in unit tests
 		reviews.add(review);
 	}
 
 	public void removeNextReview(Card card) {
 		NextReview nextReview = nextReviews.get(card.getId());
 		if (nextReview != null) {
 			nextReview.remove();
 			nextReviews.remove(card.getId());
 		}
 	}
 
     public synchronized Card getRandomActiveCard() {
         Random random = new Random();
         ArrayList<Card> cards = getActiveCards();
 
         if (cards.size() == 0) {
             return null;
         }
         return cards.get(random.nextInt(cards.size()));
     }
 }
