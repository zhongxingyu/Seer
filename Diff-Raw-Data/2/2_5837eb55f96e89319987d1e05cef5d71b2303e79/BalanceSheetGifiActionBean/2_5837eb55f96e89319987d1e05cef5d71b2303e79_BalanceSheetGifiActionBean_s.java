 package com.osc.web.action.report;
 
 import java.math.BigDecimal;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.LinkedList;
 import java.util.List;
 
 import net.sourceforge.stripes.action.DefaultHandler;
 import net.sourceforge.stripes.action.ForwardResolution;
 import net.sourceforge.stripes.action.Resolution;
 import net.sourceforge.stripes.integration.spring.SpringBean;
 
 import org.springframework.dao.DataAccessException;
 
 import com.osc.biz.bean.account.AccountBean;
 import com.osc.biz.enums.AccountType;
 import com.osc.biz.svc.account.AccountService;
 import com.osc.framework.cache.Parameter;
 import com.osc.web.action.GenericActionBean;
 
 public class BalanceSheetGifiActionBean extends GenericActionBean {
 
 
 	private List<AccountBean> assetList;
 	private List<AccountBean> liabilityList;
 	private List<AccountBean> equityList;
 	
 	private BigDecimal totalAssets;
 	private BigDecimal totalLiabilities;
 	private BigDecimal totalEquities;
 	private BigDecimal totalLiabEqu;
 	
 	private BigDecimal netIncome;
 	
 	private Date endDate = new Date();
 
 	
 	private AccountService accountService;
 	
 	@SpringBean("accountService")
 	public void injectAccountService(AccountService svc) {
 		accountService = svc;
 	}
 	
 
 	public List<AccountBean> getAssetList() {
 		return assetList;
 	}
 
 	public List<AccountBean> getLiabilityList() {
 		return liabilityList;
 	}
 
 	public List<AccountBean> getEquityList() {
 		return equityList;
 	}
 
 	public BigDecimal getTotalAssets() {
 		return totalAssets;
 	}
 
 	public BigDecimal getTotalLiabilities() {
 		return totalLiabilities;
 	}
 
 	public BigDecimal getTotalEquities() {
 		return totalEquities;
 	}
 
 	public BigDecimal getTotalLiabEqu() {
 		return totalLiabEqu;
 	}
 
 	public BigDecimal getNetIncome() {
 		return netIncome;
 	}
 
 	public Date getEndDate() {
 		return endDate;
 	}
 
 	public void setEndDate(Date endDate) {
 		this.endDate = endDate;
 	}
 	
 	public String getCompanyName() {
 		return getParameter(Parameter.COMPANY_NAME);
 	}
 
 
 	@DefaultHandler
 	public Resolution view() {
 		
 		try {
 			int reAccountId = Integer.parseInt(getParameter(Parameter.ACCOUNT_RETAINED_EARNINGS));
 			List<AccountBean> tempAccountList = accountService.getAccountBalances(getStartDate(), reAccountId);
 			
 			BigDecimal reBalance = BigDecimal.ZERO;
 			if(tempAccountList!=null && tempAccountList.size()>0) {
 				AccountBean account = tempAccountList.get(0);
 				reBalance = account.getBalance();
 			}
 			
 			tempAccountList = accountService.getGifiAccountBalances(endDate, 0);
 			
 			assetList = new LinkedList<AccountBean>();
 			liabilityList = new LinkedList<AccountBean>();
 			equityList = new LinkedList<AccountBean>();			
 			
 			totalAssets = BigDecimal.ZERO;
 			totalLiabilities = BigDecimal.ZERO;
 			totalEquities = BigDecimal.ZERO;
 			netIncome = BigDecimal.ZERO;
 			
 			int equityAccounts = 0;
 			
 			// calculate net income; it replaces Income SUmmary balance
 			for(AccountBean account : tempAccountList) {
 				if(account.getType() == AccountType.INCOME) {
 					if(account.isHasNegativeBalance())
 						netIncome = netIncome.subtract(account.getBalance());
 					else
 						netIncome = netIncome.add(account.getBalance());
 				} else if(account.getType() == AccountType.EXPENSE) {
 					if(account.isHasNegativeBalance())
 						netIncome = netIncome.add(account.getBalance());
 					else
 						netIncome = netIncome.subtract(account.getBalance());
 				}
 			}
 				
 			for(AccountBean account : tempAccountList) {
 				if(account.getType() == AccountType.ASSET) {
 					assetList.add(account);
 					if(account.isHasNegativeBalance())
 						totalAssets = totalAssets.subtract(account.getBalance());
 					else
 						totalAssets = totalAssets.add(account.getBalance());
 				} else if(account.getType() == AccountType.LIABILITY) {
 					liabilityList.add(account);
 					if(account.isHasNegativeBalance())
 						totalLiabilities = totalLiabilities.subtract(account.getBalance());
 					else
 						totalLiabilities = totalLiabilities.add(account.getBalance());
 				} else if(account.getType() == AccountType.OWNERS_EQUITY) {
 					equityList.add(account);
 					
 					if(String.valueOf(account.getAccountId()).equals(getParameter(Parameter.ACCOUNT_INCOME_SUMMARY_GIFI)))
 						account.setBalance(netIncome);
 					else if(String.valueOf(account.getAccountId()).equals(getParameter(Parameter.ACCOUNT_RETAINED_EARNINGS_GIFI))) {
 						equityAccounts += account.getAccountId();
 						account.setBalance(reBalance);
 						SimpleDateFormat format = new SimpleDateFormat("MMMM d, yyyy");
 						account.setName(account.getName() + " - as of " + format.format(getStartDate()));
 					} else if(String.valueOf(account.getAccountId()).equals(getParameter(Parameter.ACCOUNT_SHARE_CAPITAL_GIFI)))
 						equityAccounts += account.getAccountId();
 
 					if(account.isHasNegativeBalance())
 						totalEquities = totalEquities.subtract(account.getBalance());
 					else
 						totalEquities = totalEquities.add(account.getBalance());
 				}
 			}
 			totalLiabEqu = totalLiabilities.add(totalEquities);
 			fixEquityListIfNeeded(equityAccounts);
 	    }
 	    catch (DataAccessException e) {
 	    	return handleException(e);
 	    }
 		
         return new ForwardResolution("/report/balance-sheet-gifi.jsp");
     }
 	
 	
 	private Date getStartDate() {
 		Calendar c = GregorianCalendar.getInstance();
 		c.setTime(endDate);
 		c.set(Calendar.DATE, 1);
 		c.set(Calendar.MONTH, Calendar.JANUARY);
 		return c.getTime();
 	}
 
	
 	private void fixEquityListIfNeeded(int equityAccounts) {
 		int reAccountId = Integer.parseInt(getParameter(Parameter.ACCOUNT_RETAINED_EARNINGS_GIFI));
 		int scAccountId = Integer.parseInt(getParameter(Parameter.ACCOUNT_SHARE_CAPITAL_GIFI));		
 		int expectedEquityAccounts = reAccountId + scAccountId;
 		
 		if(equityAccounts == expectedEquityAccounts) return;
 		
 		if(equityAccounts == reAccountId) {
 			AccountBean account = accountService.getAccount(scAccountId);
 			account.setBalance(BigDecimal.ZERO);
 			equityList.add(account);			
 		} else if(equityAccounts == scAccountId) {
 			AccountBean account = accountService.getAccount(reAccountId);
 			account.setBalance(BigDecimal.ZERO);
 			equityList.add(account);			
 		} else {
 			AccountBean account = accountService.getAccount(scAccountId);
 			account.setBalance(BigDecimal.ZERO);
 			equityList.add(account);			
 			account = accountService.getAccount(reAccountId);
 			account.setBalance(BigDecimal.ZERO);
 			equityList.add(account);
 		}
 	}
 
 }
