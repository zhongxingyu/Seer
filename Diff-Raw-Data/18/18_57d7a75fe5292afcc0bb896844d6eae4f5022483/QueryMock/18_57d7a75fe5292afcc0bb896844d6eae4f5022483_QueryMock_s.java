 package arquillian.talk.ejb.mocks;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.persistence.FlushModeType;
 import javax.persistence.LockModeType;
 import javax.persistence.Parameter;
 import javax.persistence.Query;
 import javax.persistence.TemporalType;
 
 import arquillian.talk.ejb.PersonBeanJUnit;
 import arquillian.talk.jpa.Address;
 import arquillian.talk.jpa.Person;
 
 public class QueryMock implements Query {
 
 	private Map<String, Object> parameters = new HashMap<String, Object>();
 	
 	@Override
 	public Query setParameter(String key, Object value) {
 		parameters.put(key, value);
 		return this;
 	}
 
 	@Override
 	public Object getSingleResult() {
 		Person person = new Person();
 		person.setName((String) parameters.get("name"));
 		person.setDob(new Date());
 		person.setId(PersonBeanJUnit.MY_ID);
 		person.setAddresses(new HashSet<Address>());
 		return person;
 	}
 
 	@Override
 	public int executeUpdate() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public int getFirstResult() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public FlushModeType getFlushMode() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Map<String, Object> getHints() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public LockModeType getLockMode() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public int getMaxResults() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public Parameter<?> getParameter(String arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Parameter<?> getParameter(int arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public <T> Parameter<T> getParameter(String arg0, Class<T> arg1) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public <T> Parameter<T> getParameter(int arg0, Class<T> arg1) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public <T> T getParameterValue(Parameter<T> arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Object getParameterValue(String arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Object getParameterValue(int arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Set<Parameter<?>> getParameters() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
	@SuppressWarnings("rawtypes")
	@Override
	public List getResultList() {
		// TODO Auto-generated method stub
		return null;
	}

 	@Override
 	public boolean isBound(Parameter<?> arg0) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public Query setFirstResult(int arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Query setFlushMode(FlushModeType arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Query setHint(String arg0, Object arg1) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Query setLockMode(LockModeType arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Query setMaxResults(int arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public <T> Query setParameter(Parameter<T> arg0, T arg1) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Query setParameter(int arg0, Object arg1) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Query setParameter(Parameter<Calendar> arg0, Calendar arg1,
 			TemporalType arg2) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Query setParameter(Parameter<Date> arg0, Date arg1, TemporalType arg2) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Query setParameter(String arg0, Calendar arg1, TemporalType arg2) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Query setParameter(String arg0, Date arg1, TemporalType arg2) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Query setParameter(int arg0, Calendar arg1, TemporalType arg2) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Query setParameter(int arg0, Date arg1, TemporalType arg2) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public <T> T unwrap(Class<T> arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
