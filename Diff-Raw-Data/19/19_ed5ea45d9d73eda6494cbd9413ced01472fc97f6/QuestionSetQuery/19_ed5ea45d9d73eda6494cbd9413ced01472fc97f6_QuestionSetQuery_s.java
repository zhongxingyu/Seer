 package uk.ac.cam.sup.queries;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.criterion.CriteriaSpecification;
 import org.hibernate.criterion.Disjunction;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 
 import uk.ac.cam.sup.HibernateUtil;
 import uk.ac.cam.sup.models.Question;
 import uk.ac.cam.sup.models.QuestionSet;
 import uk.ac.cam.sup.models.Tag;
 import uk.ac.cam.sup.models.User;
 
 public class QuestionSetQuery {
 	private Criteria criteria;
 	private QuestionSetQuery(Criteria criteria) {
 		this.criteria = criteria;
 	}
 	
 	public static QuestionSetQuery all() {
 		QuestionSetQuery qsq = new QuestionSetQuery (
 				HibernateUtil.getTransactionSession()
 					.createCriteria(QuestionSet.class)
 					.createAlias("owner", "o")
 					.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
 					.addOrder(Order.desc("isStarred"))
 		);
 		qsq.criteria.addOrder(Order.desc("timeStamp"));
 		return qsq;
 	}
 	
 	public List<QuestionSet> list() {
 		@SuppressWarnings("unchecked")
 		List<QuestionSet> l = criteria.list();
 		return l;
 	}
 	
 	public static QuestionSet get(int id) {
 		Session session = HibernateUtil.getTransactionSession();
 
 		QuestionSet qs = (QuestionSet) session
 				.createQuery("from QuestionSet where id = :id")
 				.setParameter("id", id)
 				.uniqueResult();
 		
 		return qs;
 	}
 	
	public List<Map<String,Object>> maplist(boolean shadow) {
		List<Map<String,Object>> l = new ArrayList<Map<String,Object>>();
 		List<QuestionSet> all = list();
 		
 		for (QuestionSet qs: all) {
 			l.add(qs.toShortMap(shadow));
 		}
 		
 		return l;
 	}
 	
	public List<Map<String,Object>> maplist() {
 		return maplist(true);
 	}
 	
 	public QuestionSetQuery withTags(List<Tag> taglist) {
 		Disjunction d = Restrictions.disjunction();
 		for (Tag t: taglist) {
 			d.add(Restrictions.eq("t.name", t.getName()).ignoreCase());
 		}
 		criteria.createAlias("tags", "t").add(d);
 			
 		return this;
 	}
 	
 	public QuestionSetQuery withUsers(List<User> userlist) {
 		Disjunction d = Restrictions.disjunction();
 		for (User u: userlist) {
 			d.add(Restrictions.eq("owner.id", u.getId()).ignoreCase());
 		}
 		criteria.add(d);
 		
 		return this;
 	}
 	
 	public QuestionSetQuery withStar() {
 		criteria.add(Restrictions.eq("isStarred", true));
 		return this;
 	}
 	public QuestionSetQuery withoutStar() {
 		criteria.add(Restrictions.eq("isStarred", false));
 		return this;
 	}
 	
 	public QuestionSetQuery bySupervisor() {
 		criteria
 			.add(Restrictions.eq("o.supervisor", true));
 		return this;
 	}
 	
 	public QuestionSetQuery byStudent() {
 		criteria
 		.add(Restrictions.eq("o.supervisor", false));
 		return this;
 	}
 	
 	public QuestionSetQuery after(Date date) {
 		criteria.add(Restrictions.gt("timeStamp", date));
 		return this;
 	}
 	
 	public QuestionSetQuery before(Date date) {
 		criteria.add(Restrictions.lt("timeStamp", date));
 		return this;
 	}
 	
 	public QuestionSetQuery maxDuration(int mins) {
 		criteria.add(Restrictions.le("expectedDuration", mins));
 		return this;
 	}
 	
 	public QuestionSetQuery minDuration(int mins) {
 		criteria.add(Restrictions.ge("expectedDuration", mins));
 		return this;
 	}
 	
 	public QuestionSetQuery have(Question q) {
 		this.have(q.getId());
 		return this;
 	}
 	public QuestionSetQuery have(int qID) {
 		criteria
 			.createAlias("questions", "qp")
 			.createAlias("qp.question", "q")
 			.add(Restrictions.eq("q.id", qID));
 		return this;
 	}
 	
 	public Criteria getCriteria(){
 		return criteria;
 	}
 	
 }
