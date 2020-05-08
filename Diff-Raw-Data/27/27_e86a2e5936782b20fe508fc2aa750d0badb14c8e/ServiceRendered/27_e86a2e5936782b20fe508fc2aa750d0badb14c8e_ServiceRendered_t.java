 package entity;
 
 import java.math.BigDecimal;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.*;
 
 import control.ChocAnApp;
 
 
 /**
  * Class ServiceRendered
  */
 public class ServiceRendered {
 
 	//
 	// Fields
 	//
 	
 	private int transaction_id;
 	private Date service_logged;
 	private Date service_rendered;
 	private BigDecimal fee;
 	private Provider provider;
 	private Service service;
 	private Member member;
 	private String comments;
 	
 	/// Prepared Statements
 	private static PreparedStatement insert_stmt = null;
 	private static PreparedStatement update_stmt = null;
 	private static PreparedStatement delete_stmt = null;
 	private static PreparedStatement search_by_provider_stmt = null;
 	private static PreparedStatement search_by_member_stmt = null;
 
 	//
 	// Constructors
 	//
 	
 	public ServiceRendered(
 		Date service_logged,
 		Date service_rendered,
 		BigDecimal fee,
 		Provider provider,
 		Service service,
 		Member member,
 		String comments) throws Exception {
 
 		initializeQueries();
 		
 		this.transaction_id = -1;
 		this.service_logged = service_logged;
 		this.service_rendered = service_rendered;
 		this.fee = fee;
 		this.provider = provider;
 		this.service = service;
 		this.member = member;
 		this.comments = comments;
 	}
 	
 	protected ServiceRendered(
 		int transaction_id,
 		Date service_logged,
 		Date service_rendered,
 		BigDecimal fee,
 		Provider provider,
 		Service service,
 		Member member,
 		String comments) throws Exception {
 
 		initializeQueries();
 		
 		this.transaction_id = transaction_id;
 		this.service_logged = service_logged;
 		this.service_rendered = service_rendered;
 		this.fee = fee;
 		this.provider = provider;
 		this.service = service;
 		this.member = member;
 		this.comments = comments;
 	}
 
 	static private void initializeQueries()
 		throws Exception{
 		
 		Connection conn = ChocAnApp.getConnection();
 		if (insert_stmt == null) {
 			insert_stmt = conn.prepareStatement(
 				"INSERT INTO services_rendered ( " +
 					"service_logged, " +
 					"service_provided, " +
 					"fee, " +
 					"provider_id, " +
 					"service_id, " +
 					"member_id, " +
 					"comments " +
 				") VALUES (" +
 					"datetime(? / 1000, 'unixepoch'), " +
 					"datetime(? / 1000, 'unixepoch'), " +
 					"?, ?, ?, ?, ?" +
 				")");
 		}
 		
 		if (update_stmt == null) {
 			update_stmt = conn.prepareStatement(
 				"UPDATE services_rendered " +
 				"SET " +
 					"service_logged = datetime(? / 1000, 'unixepoch'), " +
 					"service_provided = datetime(? / 1000, 'unixepoch'), " +
 					"fee = ?, " +
 					"provider_id = ?, " +
 					"service_id = ?, " +
 					"member_id = ?, " +
 					"comments = ? " +
 				"WHERE transaction_id = ?");
 		}
 		
 		if (delete_stmt == null) {
 			delete_stmt = conn.prepareStatement(
 				"DELETE FROM services_rendered WHERE transaction_id = ?");
 		}
 		
 		if (search_by_provider_stmt == null) {
 			search_by_provider_stmt = conn.prepareStatement(
 				"SELECT " +
 					"SR.transaction_id, " +
 					"strftime('%s', SR.service_logged) * 1000 as service_logged, " +
 					"strftime('%s', SR.service_provided) * 1000 as service_provided, " +
 					"SR.fee, " +
 					"S.service_id, " +
 					"S.service_name, " +
 					"S.fee as service_fee, " +
 					"M.member_id, " +
 					"M.full_name, " +
 					"M.member_status, " +
 					"M.street_address, " +
 					"M.city, " +
 					"M.state, " +
 					"M.zip_code, " +
 					"M.email as member_email, " +
 					"SR.comments " +
 				"FROM services_rendered SR " +
 				"LEFT JOIN services S ON S.service_id = SR.service_id " +
 				"LEFT JOIN members M ON M.member_id = SR.member_id " +
 				"WHERE SR.provider_id = ?");
 		}
 		
 		if (search_by_member_stmt == null) {
 			search_by_member_stmt = conn.prepareStatement(
 				"SELECT " +
 					"SR.transaction_id, " +
 					"strftime('%s', SR.service_logged) * 1000 as service_logged, " +
 					"strftime('%s', SR.service_provided) * 1000 as service_provided, " +
 					"SR.fee, " +
 					"S.service_id, " +
 					"S.service_name, " +
 					"S.fee as service_fee, " +
 					"P.provider_id, " +
 					"P.provider_name, " +
 					"P.email as provider_email, " +
 					"SR.comments " +
 				"FROM services_rendered SR " +
 				"LEFT JOIN services S ON S.service_id = SR.service_id " +
				"LEFT JOIN providers P ON P.provider_id = SR.provider_id " +
				"WHERE SR.member_id = ?");
 		}
 	}
 	
 	//
 	// Methods
 	//
 	
 	public void save() throws Exception{
 		if(this.transaction_id == -1){
 			insert_stmt.setDate(1, new java.sql.Date(service_logged.getTime()));
 			insert_stmt.setDate(2, new java.sql.Date(service_rendered.getTime()));
 			insert_stmt.setString(3,fee.toString());
 			insert_stmt.setInt(4, provider.getProviderId());
 			insert_stmt.setInt(5, service.getServiceId());
 			insert_stmt.setInt(6, member.getMemberId());
 			insert_stmt.setString(7, comments);
 			
			insert_stmt.executeUpdate();
			
 			ResultSet rs = insert_stmt.getGeneratedKeys();
 			rs.next();
 			this.transaction_id = rs.getInt(1);
			rs.close();
 		}else{
 			update_stmt.setDate(1, new java.sql.Date(service_logged.getTime()));
 			update_stmt.setDate(2, new java.sql.Date(service_rendered.getTime()));
 			update_stmt.setString(3,fee.toString());
 			update_stmt.setInt(4, provider.getProviderId());
 			update_stmt.setInt(5, service.getServiceId());
 			update_stmt.setInt(6, member.getMemberId());
 			update_stmt.setString(7, comments);
 			update_stmt.setInt(8, transaction_id);
			update_stmt.executeUpdate();
 		}
 	}
 	
 	public void delete() throws Exception {
 		if(this.transaction_id != -1){
 			delete_stmt.setInt(1, transaction_id);
 			delete_stmt.executeUpdate();
 			transaction_id  = -1;
 		}
 	}
 
 	//
 	// Accessor methods
 	//
 
 	public int getTransactionID(){
 		return transaction_id;
 	}
 	
 	/**
 	 * Set the value of service_logged
 	 * 
 	 * @param newVar
 	 *            the new value of service_logged
 	 */
 	public void setServiceLogged(Date newVar) {
 		service_logged = newVar;
 	}
 
 	/**
 	 * Get the value of service_logged
 	 * 
 	 * @return the value of service_logged
 	 */
 	public Date getServiceLogged() {
 		return service_logged;
 	}
 
 	
 	public BigDecimal getFee() {
 		return fee;
 	}
 
 	public void setFee(BigDecimal fee) {
 		this.fee = fee;
 	}
 
 	/**
 	 * Set the value of service_rendered
 	 * 
 	 * @param newVar
 	 *            the new value of service_rendered
 	 */
 	public void setServiceRendered(Date newVar) {
 		service_rendered = newVar;
 	}
 
 	/**
 	 * Get the value of service_rendered
 	 * 
 	 * @return the value of service_rendered
 	 */
 	public Date getServiceRendered() {
 		return service_rendered;
 	}
 
 	/**
 	 * Set the value of provider
 	 * 
 	 * @param newVar
 	 *            the new value of provider
 	 */
 	public void setProvider(Provider newVar) {
 		provider = newVar;
 	}
 
 	/**
 	 * Get the value of provider
 	 * 
 	 * @return the value of provider
 	 */
 	public Provider getProvider() {
 		return provider;
 	}
 
 	/**
 	 * Set the value of service
 	 * 
 	 * @param newVar
 	 *            the new value of service
 	 */
 	public void setService(Service newVar) {
 		service = newVar;
 	}
 
 	/**
 	 * Get the value of service
 	 * 
 	 * @return the value of service
 	 */
 	public Service getService() {
 		return service;
 	}
 
 	/**
 	 * Set the value of member
 	 * 
 	 * @param newVar
 	 *            the new value of member
 	 */
 	public void setMember(Member newVar) {
 		member = newVar;
 	}
 
 	/**
 	 * Get the value of member
 	 * 
 	 * @return the value of member
 	 */
 	public Member getMember() {
 		return member;
 	}
 
 	/**
 	 * Set the value of comments
 	 * 
 	 * @param newVar
 	 *            the new value of comments
 	 */
 	public void setComments(String newVar) {
 		comments = newVar;
 	}
 
 	/**
 	 * Get the value of comments
 	 * 
 	 * @return the value of comments
 	 */
 	public String getComments() {
 		return comments;
 	}
 
 	//
 	// Other methods
 	//
 
 	/**
 	 * @param provider
 	 * @throws Exception 
 	 */
 	public static final List<ServiceRendered> getServicesRenderedByProvider(Provider provider) throws Exception {
 		initializeQueries();
 		List<ServiceRendered> list = new LinkedList<ServiceRendered>();
 		
 		search_by_provider_stmt.setInt(1, provider.getProviderId());
 		ResultSet rs = search_by_provider_stmt.executeQuery();
 		while(rs.next()){
 			Member member = new Member(
 				rs.getInt("member_id"), 
 				rs.getString("full_name"), 
 				MemberStatus.fromID(rs.getInt("member_status")), 
 				rs.getString("street_address"), 
 				rs.getString("city"), 
 				rs.getString("state"), 
 				rs.getString("zip_code"), 
 				rs.getString("member_email"));
 			
 			Service service = new Service(
 				rs.getInt("service_id"),
 				rs.getString("service_name"),
 				new BigDecimal(rs.getString("service_fee")));
 			
 			ServiceRendered sr = new ServiceRendered(
 				rs.getInt("transaction_id"),
 				new Date(rs.getDate("service_logged").getTime()),
 				new Date(rs.getDate("service_provided").getTime()),
 				new BigDecimal(rs.getString("fee")),
 				provider,
 				service,
 				member,
 				rs.getString("comments"));
 			
 			list.add(sr);
 		}
 		return list;
 	}
 
 	/**
 	 * @param member
 	 * @throws Exception 
 	 */
 	public static final List<ServiceRendered> getServicesRenderedMember(Member member) throws Exception {
 		initializeQueries();
 		List<ServiceRendered> list = new LinkedList<ServiceRendered>();
 		
 		search_by_member_stmt.setInt(1, member.getMemberId());
 		
 		ResultSet rs = search_by_member_stmt.executeQuery();
 		while(rs.next()){
 			Service service = new Service(
 				rs.getInt("service_id"),
 				rs.getString("service_name"),
 				new BigDecimal(rs.getString("service_fee")));
 			
 			Provider provider = new Provider(
 				rs.getInt("provider_id"),
 				rs.getString("provider_name"),
 				rs.getString("provider_email"));
 			
 			ServiceRendered sr = new ServiceRendered(
 				rs.getInt("transaction_id"),
 				new Date(rs.getDate("service_logged").getTime()),
 				new Date(rs.getDate("service_provided").getTime()),
 				new BigDecimal(rs.getString("fee")),
 				provider,
 				service,
 				member,
 				rs.getString("comments"));
 			
 			list.add(sr);
 		}
 		return list;
 	}
 
 }
