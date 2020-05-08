 package com.osc.biz.dao.account.impl;
 
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.util.Date;
 import java.util.List;
 
 import javax.sql.DataSource;
 
 import org.springframework.jdbc.core.SqlParameter;
 import org.springframework.jdbc.object.MappingSqlQuery;
 
 import com.osc.biz.bean.account.AccountBean;
 import com.osc.biz.bean.account.GifiAccountBean;
 import com.osc.biz.dao.account.GifiDao;
 import com.osc.biz.dao.account.impl.AccountDaoImpl.GetAccountBalancesQuery;
 import com.osc.biz.enums.AccountType;
 import com.osc.framework.dao.AbstractJdbcDaoSupport;
 
 
 public class GifiDaoImpl extends AbstractJdbcDaoSupport implements GifiDao {
 	
 	
 	private AccountsQueryByType accountsQueryByType;
 	private GetGifiAccountBalancesQuery gifiAccountBalancesQuery;
 
 	
 	@Override
 	protected void initDao() throws Exception {
 		accountsQueryByType = new AccountsQueryByType(getDataSource());
 		gifiAccountBalancesQuery = new GetGifiAccountBalancesQuery(getDataSource());
 	}
 
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<GifiAccountBean> getGifiAccountList(AccountType accountType) {
 		Object[] params = new Object[] {accountType.getId()};
 		List<GifiAccountBean> list = (List<GifiAccountBean>)accountsQueryByType.execute(params);
 		return list;
 	}
 	
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<AccountBean> getGifiAccountBalances(Date cutOffDate, AccountType accountType, Integer accountId) {
		Object[] params = new Object[] {cutOffDate, cutOffDate, accountId == 0 ? null : accountId, 
 				accountType == null ? null : accountType.getId()};
 		List<AccountBean> list = (List<AccountBean>)gifiAccountBalancesQuery.execute(params);
 		return list;
 	}
 	
 	
 	private static class AccountsQueryByType extends MappingSqlQuery {
 		
 		private static final String QUERY = 
 			"SELECT GIFI_CODE, GIFI_NAME, COMMENTS, TYPE FROM GIFI WHERE TYPE = ? ORDER BY GIFI_CODE";
 		
 		public AccountsQueryByType(DataSource ds) {
 	        super(ds, QUERY);
 	        declareParameter(new SqlParameter(Types.VARCHAR));
 	        compile();
 	    }
 		
 		@Override
 		protected Object mapRow(ResultSet rs, int index) throws SQLException {			
 			GifiAccountBean result = new GifiAccountBean();
 			result.setGifiCode(rs.getInt(1));
 			result.setGifiName(rs.getString(2));
 			result.setComments(rs.getString(3));
 			result.setType(AccountType.searchById(rs.getString(4)));
 			return result;
 		}
 	}
 	
 	
 	private static class GetGifiAccountBalancesQuery extends GetAccountBalancesQuery {
 		
 		static {
 			SQL = "select g.gifi_code account_id, g.gifi_name \"name\", a.type, a.negative_balance, "+
 			"case  "+
         "when a.type in ('A', 'X') and a.negative_balance = 'N' or a.type not in ('A', 'X') and a.negative_balance = 'Y' then "+ 
          " sum(case when l.debit_credit = 'D' then 1 else -1 end * l.amount)  "+
         "else  "+
          " sum(case when l.debit_credit = 'C' then 1 else -1 end * l.amount) "+ 
         "end balance  "+
 		"	from ledger l, account a, journal j, gifi g "+ 
 		"	where l.account_id = a.account_id  "+
         "and l.transaction_id = j.transaction_id  "+
         "and a.gifi_code = g.gifi_code "+
         "and ( "+
         "  j.transaction_date < ? "+
         "    or "+
         "  j.transaction_date = ? and j.type <> 'C') "+
         "and coalesce(?, a.account_id) = a.account_id  "+
         "and coalesce(?, a.type) = a.type "+
         "	group by g.gifi_code, g.gifi_name, a.type, a.negative_balance "+ 
 		"	order by g.gifi_code";
 		}
 		
 		public GetGifiAccountBalancesQuery(DataSource dataSource) {
 			super(dataSource);
 		}
 	}
 }
