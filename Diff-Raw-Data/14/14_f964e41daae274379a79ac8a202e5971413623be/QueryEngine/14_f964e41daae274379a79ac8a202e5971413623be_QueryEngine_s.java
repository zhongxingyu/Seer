 package com.db.training.blb.dao;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import javax.management.Query;
 
 public class QueryEngine {
 	private ConnectionEngine connectionEngine;
 	
 	public QueryEngine(){}
 	public QueryEngine(ConnectionEngine connectionEngine) throws ClassNotFoundException, SQLException{
 		this.connectionEngine=connectionEngine;
 		this.connectionEngine.connect();
 	}
 	
 	@Override
 	protected void finalize() throws Throwable {
 		this.connectionEngine.close();
 	}
 
 	public ConnectionEngine getConnectionEngine() {
 		return connectionEngine;
 	}
 
 	public void setConnectionEngine(ConnectionEngine connectionEngine) {
 		this.connectionEngine = connectionEngine;
 	}
 	
 	public String getUserForSessionId(String sessionId) throws SQLException{
 		if(connectionEngine==null){
 			throw new RuntimeException("No connection engine was provided!");
 		}
 		ResultSet rs=connectionEngine.query("select username from blb.login_session where session_id=? and abs(TIMESTAMPDIFF(MINUTE,now(),timestamp(last_activity)))<=5 limit 1",sessionId);
 		if(!rs.next()){
 			return null;
 		}
 		String user=rs.getString(1);
 		connectionEngine.update("update login_session set last_activity=now() where username=?",user);
 		return user;
 	}
 	
 	public boolean checkUserCredentials(String username, String password) throws SQLException{
 		if(connectionEngine==null){
 			throw new RuntimeException("No connection engine was provided!");
 		}
 		if(username==null || password==null){
 			return false;
 		}
 		ResultSet rs=connectionEngine.query("select count(t.username) from (select username  from blb.customers where username=? and password_hash=md5(?) union select username from blb.traders where username=? and password_hash=md5(?)) t",username,password,username,password);
 		if(!rs.next()){
 			return false;
 		}else{
 			if(rs.getInt(1)==1){
 				recordLoginInfo(username);
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Returns SESSION
 	 * @param username
 	 * @return
 	 * @throws SQLException
 	 */
 	public String createSession(String username) throws SQLException{
 		if(connectionEngine==null){
 			throw new RuntimeException("No connection engine was provided!");
 		}
 		connectionEngine.update("insert into login_session(session_id,username,last_activity) values (md5(concat(?,timestamp(now()))),?,now())",username,username);
 		ResultSet rs=connectionEngine.query("select session_id from login_session where username=? and abs(TIMESTAMPDIFF(MINUTE,now(),timestamp(last_activity)))<=1 limit 1", username);
 		if(!rs.next())
 			return null;
 		return rs.getString(1);
 	}
 	
 	public void recordLoginInfo(String username) throws SQLException{
 		connectionEngine.update("insert into log(login_date, username) values (timestamp(now()),?)", username);
 	}
 	
 	public ResultSet query(String query, String... args) throws SQLException{
 		return connectionEngine.query(query,args);
 	}
 	
 	public ResultSet getTraderIdFromSessionId(String sessionId) throws SQLException {
 		return connectionEngine.query("select t.id from traders t join login_session s on t.username = s.username and s.session_id=?", sessionId);
 	}
 	
 	/**
 	 * check if the trader can trade on behalf of this customer
 	 * @param customerId
 	 * @param traderSessionId
 	 * @return true if allowed to trade, false otherwise
 	 * @throws SQLException
 	 */
 	public boolean checkPortfolioManagementPermission(String customerId,String traderSessionId) throws SQLException{
 		return connectionEngine.query("select c.id,c.username from customers c inner join (select gg.participant_id from groups gg inner join (select g.id from groups g where g.participant_id=(select t.id from traders t inner join login_session l on t.username=l.username where l.session_id=? limit 1) and g.group_type<1) curtrdrgrps on curtrdrgrps.id=gg.id) cotrdrs on cotrdrs.participant_id=c.trader_id where c.id=?",traderSessionId,customerId).next();
 	}
 	
 	public ResultSet getBondsForGivenCustomerId(String customerId,String traderSessionId) throws SQLException{
 		if(!checkPortfolioManagementPermission(customerId, traderSessionId)){
 			throw new RuntimeException("You cannot manage this customer's portfolio.");
 		}
 		return connectionEngine.query("select b.bond_name as 'Bond Name', b.issuer_name as 'Issuer', (case when b.bond_type<1 then 'governmental' else 'corporate' end) as 'Type', b.cusip as 'CUSIP', b.rating as 'Rating', b.coupon_rate as 'Coupon Rate', b.current_yield as 'Current Yield', b.maturity_yield as 'Maturity Yield', b.maturity_date as 'Maturity Date', b.par_value as 'Par Value', b.price as 'Price', b.quantity_owned as 'Quantity' from bonds b inner join (select g.id from groups g where g.participant_id=? and g.group_type>0) o on b.group_id=o.id where b.quantity_owned>0",customerId);
 	}
 	
 	public ResultSet getBondsForGivenSessionId(String sessionId) throws SQLException{
 		return connectionEngine.query("select b.bond_name as 'Bond Name', b.issuer_name as 'Issuer', (case when b.bond_type<1 then 'governmental' else 'corporate' end) as 'Type', b.cusip as 'CUSIP', b.rating as 'Rating', b.coupon_rate as 'Coupon Rate', b.current_yield as 'Current Yield', b.maturity_yield as 'Maturity Yield', b.maturity_date as 'Maturity Date', b.par_value as 'Par Value', b.price as 'Price', b.quantity_owned as 'Quantity' from bonds b inner join (select g.id from groups g inner join (select c.id,c.username from customers c inner join login_session l on c.username=l.username where l.session_id=?) cus on g.participant_id=cus.id) o on b.group_id=o.id where b.quantity_owned>0",sessionId);
 	}
 	
 	public ResultSet getBondData(String cusip) throws SQLException {
 		if(connectionEngine==null){
 			throw new RuntimeException("No connection engine was provided!");
 		}
 		
 		ResultSet rs = connectionEngine.query("select * from blb.bonds where CUSIP =? AND group_id ='0'",cusip);
 		if(!rs.next()){
 			return null;
 		}
 		return rs;
 	}
 	
 }
