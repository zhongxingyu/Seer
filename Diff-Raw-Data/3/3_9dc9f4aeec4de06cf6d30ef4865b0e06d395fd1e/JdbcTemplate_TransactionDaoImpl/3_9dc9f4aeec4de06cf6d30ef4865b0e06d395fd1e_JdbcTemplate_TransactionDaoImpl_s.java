 /**
  * 
  */
 package com.grimesco.gcocentral.fidelity.dao;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired; 
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.jdbc.core.JdbcTemplate; 
 import org.springframework.jdbc.core.PreparedStatementCreator; 
 import org.springframework.jdbc.core.RowMapper; 
 import org.springframework.jdbc.support.GeneratedKeyHolder; 
 import org.springframework.jdbc.support.KeyHolder; 
 import org.springframework.stereotype.Repository;
 
 import com.grimesco.gcocentral.SqlUtil;
 import com.grimesco.gcocentral.td.dao.TDtransactionMapper;
 import com.grimesco.translateFidelity.model.POJO.FIcomment;
 import com.grimesco.translateFidelity.model.POJO.FItransaction;
 import com.grimesco.translateTD.model.TDtransaction;
 
 import java.sql.Connection; 
 import java.sql.PreparedStatement; 
 import java.sql.SQLException; 
 
 import javax.sql.DataSource;
 /**
  * @author jaeboston
  *
  */
 @Repository("FItransactionDao")
 public class JdbcTemplate_TransactionDaoImpl implements FItransactionDao {
 
 	
 	 private JdbcTemplate jdbcTemplate;
 	 
 	 //@Autowired  
 	 public void setJdbcTemplate(JdbcTemplate t){  
 		 this.jdbcTemplate = t; 
 	 } 
 	 
 	 @Autowired  
 	 public void setDataSource(@Qualifier("MSSQLtargetdataSource") final DataSource dataSource) {
 		 this.jdbcTemplate =  new JdbcTemplate(dataSource);
 	 }
 	 
 	 
 	/* (non-Javadoc)
 	 * 
 	 */
 	 @Override
 	 public void insert(FItransaction transaction) { 
 
 		 //KeyHolder keyHolder = new GeneratedKeyHolder(); 
 		 //this.jdbcTemplate.update(new PS_FItransactionCreator(transaction), keyHolder);
 		 //transaction.setID(keyHolder.getKey().intValue());
 	    
 	    this.jdbcTemplate.update(new PS_FItransactionCreator(transaction));
 
 	    //-- update arraylist of comments for the given transaction
 	    insertcomment(transaction);
 	    
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.grimesco.gcocentral.StockDao#update(com.grimesco.gcocentral.Stock)
 	 */
 	 @Override
 	 public void update(final FItransaction transaction) {
     
 		 //-- update transaction
 		 jdbcTemplate.update(new PreparedStatementCreator() { 
 			 public PreparedStatement createPreparedStatement(Connection connection) throws SQLException { 
                 
         		String sql = 	"UPDATE FIDELITY_TRANSACTION SET ID = ?, ACCOUNT_NUMBER = ?, TRANSACTION_TYPE = ?, " + 
                         		"TRANSACTION_DATE = ?, SECURITY_TYPE = ?, SYMBOL = ? " + 
                         		"AMOUNT = ?, SOURCE = ?, TRANSACTION_QUANTITY = ? " + 
                         		"BROKER_CODE = ?, COMMISSION = ?, SETTLEMENT_DATE = ? " + 
                         		"OPTION_SYMBOL = ? , SOURCE_DATE= ?" + 
                         		"WHERE ID = ?"; 
                 
                 PreparedStatement ps = connection.prepareStatement(sql); 
                 ps = FIsqlUtil.prepareStatementFItransactionReady(ps, transaction);
                 ps.setLong(15, transaction.ID);    
                 return ps;
             } 
 		 }); 
 		
 		 //-- update arraylist of comments for the given transaction
 		 if (transaction.txcommentList.size() > 0) {
 			 String sql = 	"UPDATE FIDELITY_TRANSACTION_COMMENT SET TRANSACTION_ID = ?, ACCOUNT_NUMBER = ?, TRANSACTION_DATE = ?, COMMENT = ? " + 
              				"WHERE ID = ?"; 
 
 				this.jdbcTemplate.batchUpdate(sql, new FIcommentBatch(transaction.txcommentList));
 		 }
 
 	 }
 
 	 
 	 @Override
 	 public void delete(FItransaction transaction) {
 		 
 		 //-- delete transactions
 		 jdbcTemplate.update("delete from FIDELITY_TRANSACTION where ID=? and  ACCOUNT_NUMBER = ? and TRANSACTION_TYPE = ? and TRANSACTION_DATE = ? and SECURITY_TYPE = ? and SYMBOL = ? and AMOUNT = ? and TRANSACTION_QUANTITY = ? ", 
 				 transaction.ID,
 				 String.valueOf(transaction.getACCOUNT_NUMBER()),  
 				 String.valueOf(transaction.getTRANSACTION_TYPE_CODE()) ,  
 				 transaction.getSQL_TRANSACTION_DATE(),
 				 String.valueOf(transaction.getTRANSACTION_SECURITY_TYPE_CODE()),
 				 String.valueOf(transaction.getSYMBOL()),
 				 SqlUtil.returnDouble(transaction.getAMOUNT()),
 				 SqlUtil.returnDouble(transaction.getTRANSACTION_QUANTITY())
 				 
 				 ); 
 
 		 //-- delete comments
 		 if (transaction.txcommentList.size() > 0) {
 			 String sql = 	"DELETE from FIDELITY_TRANSACTION_COMMENT where TRANSACTION_ID = ?";
 			 this.jdbcTemplate.update(sql, new Object[] {transaction.ID});
 		 } 
 	 }
 
 
 	 
 	 @Override
 	 public int insertBatch(final List<FItransaction> transactions, final List<FIcomment> comments) {
 		
 		 int sumOfRowsAffected = 0;
 		 
 		 String sql1 = 	"INSERT INTO FIDELITY_TRANSACTION " +
 						"(ID, ACCOUNT_NUMBER, TRANSACTION_TYPE, TRANSACTION_DATE, SECURITY_TYPE, SYMBOL, AMOUNT, SOURCE, TRANSACTION_QUANTITY, BROKER_CODE, COMMISSION, SETTLEMENT_DATE,OPTION_SYMBOL, SOURCE_DATE) " +
 						" VALUES (?, ?, ?, ?, ?, ?,?,?,?,?,?,?,?,?)"; 
 	
 		 String sql2 = 	"INSERT INTO FIDELITY_TRANSACTION_COMMENT " +
 						"(TRANSACTION_ID, ACCOUNT_NUMBER, TRANSACTION_DATE, COMMENT) " +
 						" VALUES (?, ?, ?, ?)"; 
 
 		//-- insert comment
 		this.jdbcTemplate.batchUpdate(sql2, new FIcommentBatch(comments));
 
 		
 		int[] rowsaffected = this.jdbcTemplate.batchUpdate(sql1, new FItransactionBatch(transactions));
 
 		for (int i : rowsaffected) {
 			sumOfRowsAffected += i;
 		}
 
 		return sumOfRowsAffected;
 
 		
 		
 	 }// END OF METHOD
 	
 	
 	 @Override
 	 public void deleteBatch(final List<FItransaction> transactions, List<FIcomment> comments) {
 			
 			String sql1 = 	"DELETE from FIDELITY_TRANSACTION where " + 
 							" ACCOUNT_NUMBER = ? 		and " + 
 							" TRANSACTION_TYPE = ? 		and " + 
 							" TRANSACTION_DATE = ? 		and " +
 							" SECURITY_TYPE = ? 		and " +
 							" SYMBOL = ? 				and " +
 							" AMOUNT = ? 				and " + 
 							" SOURCE = ?				and " +
 							" TRANSACTION_QUANTITY = ? 	and " +
 							" BROKER_CODE = ? 			and " +
 							" COMMISSION = ?            and " +
 							" SETTLEMENT_DATE = ?       and " +
 							" OPTION_SYMBOL = ?         and " +
 							" SOURCE_DATE = ?";
 
 			String sql2 = 	" DELETE from FIDELITY_TRANSACTION_COMMENT where " + 
 							" TRANSACTION_ID 	= ? 	and " +
 							" ACCOUNT_NUMBER = ? 		and " +
 							" TRANSACTION_DATE 	= ? 	and " +
 							" COMMENT 		= ? ";
 	
 			this.jdbcTemplate.batchUpdate(sql1,  	new FItransactionBatch(transactions));
 			this.jdbcTemplate.batchUpdate(sql2, 	new FIcommentBatch(comments));
 			
 		}
 	
 
 	 @Override
 	 public List<FItransaction> findAvailableTransactionByTXDate(java.util.Date txDate) {
 		
 		 java.sql.Date sqlTXDate = new java.sql.Date(txDate.getTime());
 		 String sql = 	" SELECT * from FIDELITY_TRANSACTION WHERE TRANSACTION_DATE = ? order by TRANSACTION_DATE"; 
 		 List<FItransaction> ret = jdbcTemplate.query( sql, new Object[]{sqlTXDate},  new FItransactionMapper());
 
 		 //-- loop through list of transactions and get comment
 		 for (FItransaction atransaction : ret) {
 			
 			 atransaction.txcommentList = (ArrayList<FIcomment>) findAvailableCommentByTransactionId(atransaction.ID);	
 		 }
 	     
 		 return ret; 		
 	 }
 
 	 
 	 @Override
 	 public int countAvailableTransactionBySourceDate(java.util.Date sourceDate) {
 			java.sql.Date sqlSourceDate = new java.sql.Date(sourceDate.getTime());
 			 String sql = 	" SELECT count(*) from FIDELITY_TRANSACTION WHERE SOURCE_DATE = ? "; 
 			 return jdbcTemplate.queryForInt(sql, new Object[]{sqlSourceDate}); 
 		}
 	 
 	 
 	 @Override
 	 public int deleteBySourceDate(java.util.Date sourceDate) {
 		 	
 		 java.sql.Date sqlSourceDate = new java.sql.Date(sourceDate.getTime());
 		 
 		 String sql1 = 	"delete from FIDELITY_TRANSACTION_COMMENT where TRANSACTION_ID in " + 
 				 		" ( select ID from FIDELITY_TRANSACTION where SOURCE_DATE = ? ) ";
 		 
 		 String sql2 = 	"DELETE from FIDELITY_TRANSACTION where SOURCE_DATE = ?";
 	
 		//-- delete comment
 		this.jdbcTemplate.update(sql1, new Object[]{sqlSourceDate});
 		
 		return this.jdbcTemplate.update(sql2, new Object[]{sqlSourceDate});
 	
 	 }
 	
 	 
 		
 	@Override
 	public List<FItransaction> findTradeBlotterTransactionBySourceDate(java.util.Date sourceDate) {
 			
 		 java.sql.Date sqlSourceDate = new java.sql.Date(sourceDate.getTime());
		 //String sql = 	" SELECT a.* from FIDELITY_TRANSACTION a WHERE a.SOURCE_DATE = ? and a.ID not in (select TD_TRANSACTION_ID from td_costbasis ) "; 
		 String sql = 	" SELECT a.* from FIDELITY_TRANSACTION a WHERE a.SOURCE_DATE = ? ";
 		 
 		 List<FItransaction> ret = jdbcTemplate.query( 	sql, new Object[]{sqlSourceDate}, new FItransactionMapper()); 
 			 
 		 return ret; 				
 		
 	 }
 
 		
 	 
 	@Override
 	public List<FItransaction> findCostBasisTransactionBySourceDate(java.util.Date sourceDate) {
 		
 		java.sql.Date sqlSourceDate = new java.sql.Date(sourceDate.getTime());
 
 		 String sql1 = 	" SELECT * from FIDELITY_TRANSACTION a " + 
              			" WHERE a.SOURCE_DATE = ? and a.AMOUNT = 0 and a.SOURCE = 'client' and  a.TRANSACTION_TYPE = 'by' and a.SECURITY_TYPE <> 'fc' and a.TRANSACTION_QUANTITY <> 0  "; 
 		 
 		 
 		 String sql2 = 	" SELECT * from FIDELITY_TRANSACTION a INNER JOIN FIDELITY_TRANSACTION_COMMENT b ON a.ID = b.TRANSACTION_ID" + 
       					" WHERE a.SOURCE_DATE = ? and a.AMOUNT = 0 and a.SOURCE = 'xxxxxxx' and  a.TRANSACTION_TYPE = 'by' and a.SECURITY_TYPE <> 'fc' and a.TRANSACTION_QUANTITY <> 0 and b.COMMENT like 'sd:$cash   conversion trans%'  "; 
 	
 		 List<FItransaction> ret1 = jdbcTemplate.query( 	sql1, new Object[]{sqlSourceDate}, new FItransactionMapper()); 
 		 List<FItransaction> ret2 = jdbcTemplate.query( 	sql2, new Object[]{sqlSourceDate}, new FItransactionMapper()); 
 		 
 		 List<FItransaction> ret = new ArrayList<FItransaction>();
 		 ret.addAll(ret1);
 		 ret.addAll(ret2);
 		 
 		 return ret; 				
 	}
 
 	
 	 @Override
 	 public List<FItransaction> get() {		 
 		List<FItransaction> ret = jdbcTemplate.query("select * from FIDELITY_TRANSACTION", new FItransactionMapper()); 
 	    return ret; 
 	}
 
 	 @Override
 	 public FItransaction get(long _transactionId) {		 
 		FItransaction ret = jdbcTemplate.queryForObject("select * from FIDELITY_TRANSACTION where ID = ?",new Object[]{_transactionId}, new FItransactionMapper()); 
 	    return ret; 
 	}
  
 	 
 	 /*
 	 @Override
 	 public List<FItransaction> findAvailableTransactionById(int id) {
 		 
 		 String sql = 	" SELECT * from FIDELITY_TRANSACTION WHERE ID = ? "; 
 		 List<FItransaction> ret = jdbcTemplate.query( 	sql, new Object[]{id}, new FItransactionMapper()); 
 		 return ret; 		
 
 	}
 	@Override
 	 public List<FItransaction> findAvailableTransactionBySymbol(String symbol) {
 
 		 String sql = 	" SELECT * from FIDELITY_TRANSACTION WHERE SYMBOL = ? order by TRANSACTION_DATE"; 
 		 List<FItransaction> ret = jdbcTemplate.query( 	sql, new Object[]{symbol},  new FItransactionMapper());
 
 		//-- loop through list of transactions and get comment
 		for (FItransaction atransaction : ret) {
 			atransaction.txcommentList = (ArrayList<FIcomment>) findAvailableCommentByTransactionId(atransaction.ID);	
 		}
 	     return ret; 		
 	
 	 }
 
 	 */
 	 
 
 	//----------------------------------------------------------------
 	//----- private methods
 	//----------------------------------------------------------------
 	public void insertcomment(FItransaction transaction) { 
 
 	    //-- update arraylist of comments for the given transaction
 	    if (transaction.txcommentList.size() > 0) {
 		    String sql = 	"INSERT INTO FIDELITY_TRANSACTION_COMMENT " +
 							"(TRANSACTION_ID, ACCOUNT_NUMBER, TRANSACTION_DATE, COMMENT) " +
 							" VALUES (?, ?, ?, ?)"; 
 			this.jdbcTemplate.batchUpdate(sql, new FIcommentBatch(transaction.txcommentList));
 	    }
 	}
 	
 	
 	public List<FIcomment> findAvailableCommentByTransactionId(long id) {
 
 		 String sql = 	" SELECT * from FIDELITY_TRANSACTION_COMMENT" + 
 	                	" WHERE TRANSACTION_ID = ? order by TRANSACTION_DATE"; 
 		 
 		 List<FIcomment> ret = jdbcTemplate.query( 	sql, new Object[]{id}, new FIcommentMapper() ); 
 		 return ret; 		
 	}
 
 	
 	
 	
 	
 	
 }//-- END OF CLASS
