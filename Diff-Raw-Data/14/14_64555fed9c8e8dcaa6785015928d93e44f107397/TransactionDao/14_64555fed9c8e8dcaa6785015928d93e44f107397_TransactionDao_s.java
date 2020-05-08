 package com.dao;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.Restrictions;
 
 import com.pojo.Fund;
 import com.pojo.Transaction;
 
 public class TransactionDao extends BaseDao<Transaction> {
 	private static TransactionDao instance = new TransactionDao();
 
 	public static TransactionDao getInstance() {
 		return instance;
 	}
 
 	private TransactionDao() {
 		super(Transaction.class);
 	}
 
 	public ArrayList<Transaction> getListByUserId(int userId) throws Exception {
 		List<Criterion> criterions = new ArrayList<Criterion>();
 		Criterion criterion = Restrictions.eq("sysuser.id", userId);
 		criterions.add(criterion);
		return (ArrayList<Transaction>) super.getList(criterions);
 	}
 	
 	public Transaction getById(Integer id) throws Exception {
 		List<Criterion> criterions = new ArrayList<Criterion>();
 		Criterion criterion = Restrictions.eq("id", id);
 		criterions.add(criterion);
 		return super.get(criterions);
 	}
 	
 	
 	public ArrayList<Transaction> displayByOperation(int userId, int operation) throws Exception {
 		List<Criterion> criterions = new ArrayList<Criterion>();
 		Criterion criterion1 = Restrictions.eq("sysuser.id", userId);
 		criterions.add(criterion1);
 		Criterion criterion2 = Restrictions.eq("transactionType", operation);
 		criterions.add(criterion2);
		return (ArrayList<Transaction>) super.getList(criterions);	
 	}
 	
 	public ArrayList<Transaction> getTransByDate(Date date) throws Exception {
 		List<Criterion> criterions = new ArrayList<Criterion>();
 		Criterion criterion1 = Restrictions.eq("executeDate", date);
 		criterions.add(criterion1);
 		Criterion criterion2 = Restrictions.eq("status", Transaction.TRANS_STATUS_PENDING);
 		criterions.add(criterion2);
 		return (ArrayList<Transaction>) super.getList(0, 0, "id", true, criterions);	
 	}
 	
 	public void createTransaction(Transaction transaction) throws Exception{
 		super.create(transaction);
 
 	}
 	
 	public void update(Transaction transaction) throws Exception{
 		super.update(transaction);
 
 	}
 
 	public ArrayList<Transaction> getTransByStatus() throws Exception {
 		List<Criterion> criterions = new ArrayList<Criterion>();
 		Criterion criterion2 = Restrictions.eq("status", Transaction.TRANS_STATUS_PENDING);
 		criterions.add(criterion2);
 		return (ArrayList<Transaction>) super.getList(0, 0, "id", true, criterions);	
 	}
 /*	public ArrayList<Transaction> getPendTransByUserIdFundId(int userId, int fundId) throws Exception {
 		List<Criterion> criterions = new ArrayList<Criterion>();
 		Criterion criterion1 = Restrictions.eq("sysuser.id", userId);
 		Criterion criterion2 = Restrictions.eq("status", Transaction.TRANS_STATUS_PENDING);
 		//--fix here--//
 		Criterion criterion3 = Restrictions.eq("status", Transaction.TRANS_STATUS_PENDING);
 		criterions.add(criterion1);
 		criterions.add(criterion2);
 		return (ArrayList<Transaction>) super.getList(0, 0, "id", true, criterions);	
 	}
 	*/
 	public ArrayList<Transaction> getPendTransByUserIdOp(int userId, int operation) throws Exception {
 		List<Criterion> criterions = new ArrayList<Criterion>();
 		Criterion criterion1 = Restrictions.eq("sysuser.id", userId);
 		Criterion criterion2 = Restrictions.eq("status", Transaction.TRANS_STATUS_PENDING);
 		Criterion criterion3 = Restrictions.eq("transactionType", operation);
 		criterions.add(criterion1);
 		criterions.add(criterion2);
 		criterions.add(criterion3);
 		return (ArrayList<Transaction>) super.getList(0, 0, "id", true, criterions);	
 	}
 	
 }
