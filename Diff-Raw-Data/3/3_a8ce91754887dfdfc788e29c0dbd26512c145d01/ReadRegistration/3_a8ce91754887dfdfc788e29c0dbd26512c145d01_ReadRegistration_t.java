 package net.idea.restnet.u.db;
 
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.idea.modbcum.i.IQueryRetrieval;
 import net.idea.modbcum.i.exceptions.AmbitException;
 import net.idea.modbcum.i.query.QueryParam;
 import net.idea.modbcum.q.conditions.EQCondition;
 import net.idea.modbcum.q.query.AbstractQuery;
 import net.idea.restnet.db.aalocal.user.IDBConfig;
 import net.idea.restnet.u.RegistrationStatus;
 import net.idea.restnet.u.UserRegistration;
 
 
 public class ReadRegistration  extends AbstractQuery<String, UserRegistration, EQCondition, UserRegistration>  implements 
 IQueryRetrieval<UserRegistration>, IDBConfig {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 6228939989116141217L;
 
 	protected String databaseName = null;
 	@Override
 	public void setDatabaseName(String name) {
 		databaseName = name;
 	}
 	@Override
 	public String getDatabaseName() {
 		return databaseName;
 	}
	protected static String sql = "SELECT user_name,created,confirmed,code,status from %s.user_registration where code=? and status='confirmed' and date_add(created,interval 2 day)>=now()";
 	
 	public ReadRegistration(String code) {
 		super();
 		setValue(new UserRegistration(code));
 	}
 	
 	public ReadRegistration(UserRegistration registration) {
 		super();
 		setValue(registration);
 	}
 
 	@Override
 	public double calculateMetric(UserRegistration object) {
 		return 1;
 	}
 	public boolean isPrescreen() {
 		return false;
 	}
 
 
 	public List<QueryParam> getParameters() throws AmbitException {
 		List<QueryParam> params = null;
 		if (getValue()==null) throw new AmbitException("Empty argument!");
 		params = new ArrayList<QueryParam>();
 		params.add(new QueryParam<String>(String.class, getValue().getConfirmationCode()));
 		return params;
 	}
 
 	public String getSQL() throws AmbitException {
 		if (getValue()==null) throw new AmbitException("Empty argument!");
 		if (getDatabaseName()==null) throw new AmbitException("Database not specified!");
 		return String.format(sql,getDatabaseName());
 	}
 
 	@Override
 	public UserRegistration getObject(ResultSet rs) throws AmbitException {
 		try {
 			UserRegistration p =  new UserRegistration();
 			p.setConfirmationCode(rs.getString("code"));
 			p.setStatus(RegistrationStatus.valueOf(rs.getString("status")));
 			p.setTimestamp_confirmed(rs.getLong("confirmed"));
 			p.setTimestamp_created(rs.getLong("created"));
 			return p;
 		} catch (Exception x) {
 			return null;
 		}
 	}
 	@Override
 	public String toString() {
 		return "Registration";
 	}
 
 }
 
 
