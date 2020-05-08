 /**
  * 
  */
 package net.frontlinesms.data.repository.hibernate;
 
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.DetachedCriteria;
 import org.hibernate.criterion.Restrictions;
 import org.hibernate.criterion.SimpleExpression;
 
 import net.frontlinesms.data.EntityField;
 import net.frontlinesms.data.Order;
 import net.frontlinesms.data.domain.Email;
 import net.frontlinesms.data.domain.Keyword;
 import net.frontlinesms.data.domain.Message;
 import net.frontlinesms.data.domain.Message.Field;
 import net.frontlinesms.data.domain.Message.Type;
 import net.frontlinesms.data.repository.MessageDao;
 
 /**
  * Hibernate implementation of {@link MessageDao}.
  * @author Alex
  */
 public class HibernateMessageDao extends BaseHibernateDao<Message> implements MessageDao {
 	/** Create instance of this class */
 	public HibernateMessageDao() {
 		super(Message.class);
 	}
 
 	/** @see MessageDao#deleteMessage(Message) */
 	public void deleteMessage(Message message) {
 		super.delete(message);
 	}
 
 	/** @see MessageDao#getAllMessages() */
 	public List<Message> getAllMessages() {
 		return super.getAll();
 	}
 
 	/** @see MessageDao#getAllMessages(int, Field, Order, Long, Long, int, int) */
 	public List<Message> getAllMessages(Message.Type messageType, Field sortBy, Order order, Long start, Long end, int startIndex, int limit) {
 		DetachedCriteria criteria = super.getSortCriterion(sortBy, order);
 		addTypeCriteria(criteria, messageType);
 		addDateCriteria(criteria, start, end);
 		return super.getList(criteria, startIndex, limit);
 	}
 
 	/** @see MessageDao#getMessageCount(int, Integer[]) */
 	public int getMessageCount(Message.Type messageType, Integer[] messageStati) {
 		DetachedCriteria criteria = DetachedCriteria.forClass(Email.class);
 		addStatusCriteria(criteria, messageStati);
 		addTypeCriteria(criteria, messageType);
 		return getCount(criteria);
 	}
 
 	/** @see MessageDao#getMessageCount(int, Long, Long) */
 	public int getMessageCount(Message.Type messageType, Long start, Long end) {
 		DetachedCriteria criteria = super.getCriterion();
 		addDateCriteria(criteria, start, end);
 		addTypeCriteria(criteria, messageType);
 		return getCount(criteria);
 	}
 
 	/** @see MessageDao#getMessageCount(int, Keyword, Long, Long) */
 	public int getMessageCount(Message.Type messageType, Keyword keyword, Long start, Long end) {
 		DetachedCriteria criteria = super.getCriterion();
 		addDateCriteria(criteria, start, end);
 		addTypeCriteria(criteria, messageType);
 		addKeywordMatchCriteria(criteria, keyword);
 		return getCount(criteria);
 	}
 
 	/** @see MessageDao#getMessageCountForMsisdn(int, String, Long, Long) */
 	public int getMessageCountForMsisdn(Message.Type messageType, String phoneNumber, Long start, Long end) {
 		DetachedCriteria criteria = super.getCriterion();
 		addTypeCriteria(criteria, messageType);
 		addDateCriteria(criteria, start, end);
 		addPhoneNumberMatchCriteria(criteria, phoneNumber, true, true);
 		return getCount(criteria);
 	}
 
 	/** @see MessageDao#getMessageForStatusUpdate(String, int) */
 	public Message getMessageForStatusUpdate(String targetMsisdnSuffix, int smscReference) {
 		DetachedCriteria criteria = super.getCriterion();
 		criteria.add(Restrictions.eq(Field.RECIPIENT_MSISDN.getFieldName(), targetMsisdnSuffix));
 		criteria.add(Restrictions.eq(Field.SMSC_REFERENCE.getFieldName(), smscReference));
 		return super.getUnique(criteria);
 	}
 
 	/** @see MessageDao#getMessages(int, Field, Order) */
 	public List<Message> getMessages(Message.Type messageType, Field sortBy, Order order) {
 		DetachedCriteria criteria = super.getSortCriterion(sortBy, order);
 		addTypeCriteria(criteria, messageType);
 		return getList(criteria);
 	}
 
 	/** @see MessageDao#getMessagesForKeyword(int, Keyword, Field, Order, Long, Long, int, int) */
 	public List<Message> getMessages(Message.Type messageType, Keyword keyword, Field sortBy, Order order) {
 		DetachedCriteria criteria = getSortCriterion(sortBy, order);
 		addTypeCriteria(criteria, messageType);
 		addKeywordMatchCriteria(criteria, keyword);
 		return getList(criteria);
 	}
 
 	/** @see MessageDao#getMessages(int, Integer[]) */
 	public Collection<Message> getMessages(Message.Type messageType, Integer[] status) {
 		DetachedCriteria criteria = super.getCriterion();
 		addTypeCriteria(criteria, messageType);
 		addStatusCriteria(criteria, status);
 		return getList(criteria);
 	}
 	
 	public int getMessageCount(Message.Type messageType, List<String> phoneNumbers,
 			Long messageHistoryStart, Long messageHistoryEnd) {
 		return super.getCount(getCriteria(messageType, phoneNumbers,
 				messageHistoryStart, messageHistoryEnd));
 	}
 	
 	public List<Message> getMessages(Message.Type messageType,
 			List<String> phoneNumbers, Long messageHistoryStart,
 			Long messageHistoryEnd) {
 		return super.getList(getCriteria(messageType, phoneNumbers,
 				messageHistoryStart, messageHistoryEnd));
 	}
 
 	private DetachedCriteria getCriteria(Message.Type messageType,
 			List<String> phoneNumbers, Long messageHistoryStart,
 			Long messageHistoryEnd) {
 		DetachedCriteria criteria = super.getCriterion();
 		addTypeCriteria(criteria, messageType);
 		addDateCriteria(criteria, messageHistoryStart, messageHistoryEnd);
 		addPhoneNumberMatchCriteria(criteria, phoneNumbers, true, true);
 		return criteria;
 	}
 
 	/** @see MessageDao#getMessagesForKeyword(int, Keyword, Field, Order, Long, Long, int, int) */
 	public List<Message> getMessagesForKeyword(Message.Type messageType, Keyword keyword, Field sortBy, Order order, Long start, Long end, int startIndex, int limit) {
 		PartialQuery<Message> q = createQueryStringForKeyword(messageType, keyword);
 		q.appendWhereOrAnd();
 		q.append("(message." + Message.Field.DATE.getFieldName() + ">=?", start);
 		q.append("AND message." + Message.Field.DATE.getFieldName() + "<=?)", end);
		String pwals = q.getQueryString();
 		q.addSorting(sortBy, order);
 		return super.getList(q.getQueryString(), startIndex, limit, q.getInsertValues());
 	}
 
 	/** @see MessageDao#getMessagesForKeyword(int, Keyword) */
 	@SuppressWarnings("unchecked")
 	public List<Message> getMessagesForKeyword(Message.Type messageType, Keyword keyword) {
 		PartialQuery q = createQueryStringForKeyword(messageType, keyword);
 		return super.getList(q.getQueryString(), q.getInsertValues());
 	}
 	
 	List<String> getSimilarKeywords(Keyword keyword) {
 		if(keyword.getKeyword().length() == 0) {
 			// Get all keywords apart from the blank one
 			List<String> allKeywordsExceptBlank = this.getHibernateTemplate().find("SELECT k.keyword FROM Keyword AS k WHERE LENGTH(k.keyword) > 0");
 			return allKeywordsExceptBlank;
 		} else {
			String likeKeyword = keyword.getKeyword() + " %";
			List<String> similarKeywords = this.getHibernateTemplate().find("SELECT k.keyword FROM Keyword  AS k WHERE k.keyword LIKE ?", likeKeyword);
 			for(String k : similarKeywords) {
 				System.out.println("Similar keyword: " + k);
 			}
 			return similarKeywords;
 		}
 	}
 
 	/** @see MessageDao#getMessagesForMsisdn(int, String, Field, Order, Long, Long, int, int) */
 	public List<Message> getMessagesForMsisdn(Message.Type messageType, String phoneNumber, Field sortBy, Order order, Long start, Long end, int startIndex, int limit) {
 		DetachedCriteria criteria = super.getSortCriterion(sortBy, order);
 		addTypeCriteria(criteria, messageType);
 		addDateCriteria(criteria, start, end);
 		addPhoneNumberMatchCriteria(criteria, phoneNumber, true, true);
 		return super.getList(criteria, startIndex, limit);
 	}
 
 	/** @see MessageDao#getMessagesForMsisdn(int, String, Field, Order, Long, Long) */
 	public List<Message> getMessagesForMsisdn(Message.Type messageType, String phoneNumber, Field sortBy, Order order, Long start, Long end) {
 		DetachedCriteria criteria = super.getSortCriterion(sortBy, order);
 		addTypeCriteria(criteria, messageType);
 		addDateCriteria(criteria, start, end);
 		addPhoneNumberMatchCriteria(criteria, phoneNumber, true, true);
 		return super.getList(criteria);
 	}
 
 	/** @see MessageDao#getMessagesForStati(int, Integer[], Field, Order, int, int) */
 	public List<Message> getMessagesForStati(Message.Type messageType, Integer[] messageStati, Field sortBy, Order order, int startIndex, int limit) {
 		DetachedCriteria criteria = super.getSortCriterion(sortBy, order);
 		addTypeCriteria(criteria, messageType);
 		addStatusCriteria(criteria, messageStati);
 		return super.getList(criteria, startIndex, limit);
 	}
 
 	/** @see MessageDao#getSMSCount(Long, Long) */
 	public int getSMSCount(Long start, Long end) {
 		DetachedCriteria criteria = super.getCriterion();
 		addDateCriteria(criteria, start, end);
 		return super.getCount(criteria);
 	}
 
 	/** @see MessageDao#getSMSCountForKeyword(Keyword, Long, Long) */
 	public int getSMSCountForKeyword(Keyword keyword, Long start, Long end) {
 		DetachedCriteria criteria = super.getCriterion();
 		addDateCriteria(criteria, start, end);
 		addKeywordMatchCriteria(criteria, keyword);
 		return super.getCount(criteria);
 	}
 
 	/** @see MessageDao#getSMSCountForMsisdn(String, Long, Long) */
 	public int getSMSCountForMsisdn(String phoneNumber, Long start, Long end) {
 		DetachedCriteria criteria = super.getCriterion();
 		addDateCriteria(criteria, start, end);
 		addPhoneNumberMatchCriteria(criteria, phoneNumber, true, true);
 		return super.getCount(criteria);
 	}
 
 	/** @see MessageDao#saveMessage(Message) */
 	public void saveMessage(Message message) {
 		super.saveWithoutDuplicateHandling(message);
 	}
 
 	/** @see MessageDao#updateMessage(Message) */
 	public void updateMessage(Message message) {
 		super.updateWithoutDuplicateHandling(message);
 	}
 	
 	/**
 	 * Augments the supplied criteria with that required to match a keyword.
 	 * @param criteria
 	 * @param keyword 
 	 */
 	private void addKeywordMatchCriteria(DetachedCriteria criteria, Keyword keyword) {
 		String keywordString = keyword.getKeyword();
 		// FIXME this should be case-insensitive
 		Criterion matchKeyword = Restrictions.or(
 				Restrictions.ilike(Field.MESSAGE_CONTENT.getFieldName(), keywordString), // This should match the keyword exactly, case insensitive
 				Restrictions.ilike(Field.MESSAGE_CONTENT.getFieldName(), keywordString + ' '));
 		criteria.add(matchKeyword);
 	}
 	
 	/**
 	 * Augments the supplied criteria with that required to match an msisdn, either for the sender, the receiver or both.
 	 * @param criteria
 	 * @param phoneNumber 
 	 * @param sender 
 	 * @param receiver 
 	 */
 	private void addPhoneNumberMatchCriteria(DetachedCriteria criteria, String phoneNumber, boolean sender, boolean receiver) {
 		if(!sender && !receiver) {
 			throw new IllegalStateException("This neither sender nor receiver matching is requested.");
 		}
 		SimpleExpression eqSender = Restrictions.eq(Field.SENDER_MSISDN.getFieldName(), phoneNumber);
 		SimpleExpression eqReceiver = Restrictions.eq(Field.RECIPIENT_MSISDN.getFieldName(), phoneNumber);
 		if(sender && receiver) {
 			criteria.add(Restrictions.or(eqSender, eqReceiver));
 		} else if(sender) {
 			criteria.add(eqSender);
 		} else if(receiver) {
 			criteria.add(eqReceiver);
 		}
 	}
 	
 	/**
 	 * Augments the supplied criteria with that required to match an msisdn, either for the sender, the receiver or both.
 	 * @param criteria
 	 * @param phoneNumber 
 	 * @param sender 
 	 * @param receiver 
 	 */
 	private void addPhoneNumberMatchCriteria(DetachedCriteria criteria, List<String> phoneNumbers, boolean sender, boolean receiver) {
 		if(!sender && !receiver) {
 			throw new IllegalStateException("This neither sender nor receiver matching is requested.");
 		}
 
 		// build multi level OR
 		Criterion ors = null;
 		for(String phoneNumber : phoneNumbers) {
 			if(sender) {
 				SimpleExpression eqSender = Restrictions.eq(Field.SENDER_MSISDN.getFieldName(), phoneNumber);
 				if(ors == null) ors = eqSender;
 				else ors = Restrictions.or(ors, eqSender);
 			}
 
 			if(receiver) {
 				SimpleExpression eqReceiver = Restrictions.eq(Field.RECIPIENT_MSISDN.getFieldName(), phoneNumber);
 				if(ors == null) ors = eqReceiver;
 				else ors = Restrictions.or(ors, eqReceiver);
 			}
 		}
 		
 		// If we've made any ORs, apply them to the criteria
 		if(ors != null) {
 			criteria.add(ors);
 		}
 	}
 	
 	/**
 	 * Augments the supplied criteria with that required to match a date range.
 	 * @param criteria
 	 * @param start
 	 * @param end
 	 */
 	private void addDateCriteria(DetachedCriteria criteria, Long start, Long end) {
 		if(start != null) {
 			criteria.add(Restrictions.ge(Field.DATE.getFieldName(), start));
 		}
 		if(end != null) {
 			criteria.add(Restrictions.le(Field.DATE.getFieldName(), end));
 		}
 	}
 	
 	/**
 	 * Augments the supplied criteria with that required to match a date range.
 	 * @param criteria
 	 * @param messageType 
 	 */
 	private void addTypeCriteria(DetachedCriteria criteria, Message.Type messageType) {
 		if(messageType != Type.TYPE_ALL) {
 			criteria.add(Restrictions.eq(Field.TYPE.getFieldName(), messageType));
 		}
 	}
 
 	/**
 	 * Augments the supplied criteria with that required to match a date range.
 	 * @param criteria 
 	 * @param statuses 
 	 */
 	private void addStatusCriteria(DetachedCriteria criteria, Integer[] statuses) {
 		criteria.add(Restrictions.in(Field.STATUS.getFieldName(), statuses));
 	}
 	
 	/**
 	 * 
 	 */
 	private PartialQuery<Message> createQueryStringForKeyword(Message.Type messageType, Keyword keyword) {
 		PartialQuery<Message> q = new PartialQuery<Message>();
 		// Build a list of values to insert into the query string
 		q.append("SELECT message FROM Message message");
 		
 		if(messageType != Message.Type.TYPE_ALL) {
 			q.append("WHERE");
 			q.append("message.type=?", messageType);
 		}
 		
 		if(keyword.getKeyword().length() > 0) {
 			q.appendWhereOrAnd();
			String likeKeyword = keyword.getKeyword() + " %";
			
 			q.append("(UPPER(message." + Message.Field.MESSAGE_CONTENT.getFieldName() + ") LIKE ?", keyword.getKeyword());
			q.append("OR UPPER(message." + Message.Field.MESSAGE_CONTENT.getFieldName() + ") LIKE ?)", likeKeyword);
 		}
 		
 		List<String> similarKeywords = getSimilarKeywords(keyword);
 		if(similarKeywords.size() > 0) {
 			// Build the query to ignore messages that match similar keywords
 			q.appendWhereOrAnd();
 			for(int i=0; i<similarKeywords.size(); ++i) {
 				if(i > 0) {
 					q.append("AND");
 				}
 				
 				String similarKeyword = similarKeywords.get(i);
				String likeSimilarKeyword = similarKeyword + " %";
				
 				q.append("NOT (UPPER(message." + Message.Field.MESSAGE_CONTENT.getFieldName() + ") LIKE ?", similarKeyword);
				q.append("OR UPPER(message." + Message.Field.MESSAGE_CONTENT.getFieldName() + ") LIKE ?)", likeSimilarKeyword);
 				
 			}
 		}
 		
 		return q;
 	}
 }
 
 class PartialQuery<E> {
 	private boolean whereAdded;
 	private final StringBuilder queryStringBuilder = new StringBuilder();
 	private final LinkedList<Object> insertValues = new LinkedList<Object>();
 	
 	private EntityField<E> orderBy; 
 	private Order order;
 	
 	boolean isWhereClauseAdded() {
 		return this.whereAdded;
 	}
 	String getQueryString() {
 		String queryString = this.queryStringBuilder.toString();
 		
 		// Add sorting if required
 		if(orderBy != null) {
 			queryString += "ORDER BY " + orderBy.getFieldName() + " " + order.toHqlString() + " ";
 		}
 		
 		return queryString;
 	}
 	Object[] getInsertValues() {
 		return this.insertValues.toArray(new Object[0]);
 	}
 	
 	public void appendWhereOrAnd() {
 		if(!isWhereClauseAdded()) {
 			this.append("WHERE");
 		} else {
 			this.append("AND");
 		}
 	}
 	
 	public void append(String s) {
 		this.queryStringBuilder.append(s);
 		this.queryStringBuilder.append(' ');
 		
 		if(s.contains("WHERE")) {
 			assert(whereAdded == false) : "CANNOT INSERT 2 WHERE CLAUSES INTO ONE QUERY";
 			whereAdded = true;
 		}
 	}
 	
 	public void append(EntityField<E> field) {
 		this.queryStringBuilder.append(field.getFieldName());
 	}
 	
 	public void append(String s, Object... insertValues) {
 		this.append(s);
 		for(Object insertValue : insertValues) {
 			this.insertValues.add(insertValue);
 		}
 	}
 
 	public void addSorting(EntityField<E> orderBy, Order order) {
 		this.orderBy = orderBy;
 		this.order = order;
 	}
 }
