 package com.action;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Map;
 
 import com.bu.TransitionDay;
 import com.dao.FundDao;
 import com.dao.SysuserDao;
 import com.dao.TransactionDao;
 import com.opensymphony.xwork2.ActionContext;
 import com.opensymphony.xwork2.ActionSupport;
 import com.pojo.Fund;
 import com.pojo.Sysuser;
 import com.pojo.Transaction;
 
 public class TransactionAction extends ActionSupport {
 	private Integer userId;
 	private Integer transactionType = -1;
 	private ArrayList<Transaction> transactions;
 	private Integer idFund;
 	private Fund fund;
 	private Sysuser user;
 	private Double amount;
 	private String amountString;
 	private String availBalanceString;
 	private int isSuccess;
 
 	private DecimalFormat cashDFormat = new DecimalFormat("###,##0.00");
 
 	public Integer getTransactionType() {
 		return transactionType;
 	}
 
 	public void setTransactionType(Integer transactionType) {
 		this.transactionType = transactionType;
 	}
 
 	public Integer getIdFund() {
 		return idFund;
 	}
 
 	public void setIdFund(Integer idFund) {
 		this.idFund = idFund;
 	}
 
 	public Fund getFund() {
 		return fund;
 	}
 
 	public void setFund(Fund fund) {
 		this.fund = fund;
 	}
 
 	public Sysuser getUser() {
 		return user;
 	}
 
 	public void setUser(Sysuser user) {
 		this.user = user;
 	}
 
 	public int getIsSuccess() {
 		return isSuccess;
 	}
 
 	public void setIsSuccess(int isSuccess) {
 		this.isSuccess = isSuccess;
 	}
 
 	public Integer getUserId() {
 		return userId;
 	}
 
 	public void setUserId(Integer userId) {
 		this.userId = userId;
 	}
 
 	public ArrayList<Transaction> getTransactions() {
 		return transactions;
 	}
 
 	public void setTransactions(ArrayList<Transaction> transactions) {
 		this.transactions = transactions;
 	}
 
 	public String getAmountString() {
 		return amountString;
 	}
 
 	public void setAmountString(String amountString) {
 		this.amountString = amountString;
 	}
 
 	public void setAmount(Double amount) {
 		this.amount = amount;
 	}
 
 	public Double getAmount() {
 		return amount;
 	}
 
 	public String getAvailBalanceString() {
 		return availBalanceString;
 	}
 
 	public void setAvailBalanceString(String availBalanceString) {
 		this.availBalanceString = availBalanceString;
 	}
 	
 	public String list() {
 
 		if (userId == null || transactionType == null) {
 			userId = 0;
 			isSuccess = -1;
 			this.addActionError("Error, Can not get User ID.");
 			return ERROR;
 		}
 		try {
 			if (transactionType == -1) {
 				transactions = TransactionDao.getInstance().getListByUserId(
 						userId);
 
 			} else {
 				System.out.println(userId);
 
 				transactions = TransactionDao.getInstance().displayByOperation(
 						userId, transactionType);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return SUCCESS;
 	}
 
 	public String listSelf() {
 		Map session = ActionContext.getContext().getSession();
 		Sysuser user = (Sysuser) session.get(LoginAction.SYSUSER);
 		this.user = user;
 		try {
 			if (transactionType == -1) {
 				transactions = TransactionDao.getInstance().getListByUserId(
 						user.getId());
 			} else {
 				transactions = TransactionDao.getInstance().displayByOperation(
 						user.getId(), transactionType);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return SUCCESS;
 	}
 
 	public String showWithdraw() throws Exception {
 		Map session = ActionContext.getContext().getSession();
 		Sysuser user = (Sysuser) session.get(LoginAction.SYSUSER);
 		isSuccess = 0;
 		this.user = user;
 		ArrayList<Transaction> transactions = TransactionDao.getInstance()
 				.getPendTransByUserIdOp(user.getId(),
 						Transaction.TRANS_TYPE_BUY);
 		ArrayList<Transaction> transactions2 = TransactionDao.getInstance()
 				.getPendTransByUserIdOp(user.getId(),
 						Transaction.TRANS_TYPE_WITHDRAW);
 		long avaiBalance = user.getCash();
 		if (transactions.size() != 0) {
 			for (Transaction t : transactions) {
 				avaiBalance -= t.getAmount();
 			}
 		}
 		if (transactions2.size() != 0) {
 			for (Transaction t : transactions2) {
 				avaiBalance -= t.getAmount();
 			}
 		}
 		// double as = avaiBalance / 100.0;
 		setAvailBalanceString(cashDFormat.format(avaiBalance / 100.0));
 
 		return SUCCESS;
 
 	}
 
 	public String withdraw() throws Exception {
 		Map session = ActionContext.getContext().getSession();
 		user = (Sysuser) session.get(LoginAction.SYSUSER);
 		isSuccess = 0;
 		userId = user.getId();
 		try {
 			user = SysuserDao.getInstance().getByUserId(userId);
 		} catch (Exception e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		if (user != null) {
 			ArrayList<Transaction> transactions = TransactionDao.getInstance()
 					.getPendTransByUserIdOp(user.getId(),
 							Transaction.TRANS_TYPE_BUY);
 			ArrayList<Transaction> transactions2 = TransactionDao.getInstance()
 					.getPendTransByUserIdOp(user.getId(),
 							Transaction.TRANS_TYPE_WITHDRAW);
 			long avaiBalance = user.getCash();
 			if (transactions.size() != 0) {
 				for (Transaction t : transactions) {
 					avaiBalance -= t.getAmount();
 				}
 			}
 			if (transactions2.size() != 0) {
 				for (Transaction t : transactions2) {
 					avaiBalance -= t.getAmount();
 				}
 			}
 
 			setAvailBalanceString(cashDFormat.format(avaiBalance / 100.0));
 
 			if (amountString == null || amountString.equals("")
 					|| Double.parseDouble(amountString) == 0
 					|| Double.parseDouble(amountString) < 0.01) {
 				this.addActionError("Request amount can not be empty or zero, and it should be larger than $0.01!");
 				this.isSuccess = -1;
 				return ERROR;
			}else if (!checkCashFormat(amountString, 9, 2)) {
				this.addActionError("Cahs Fomat Incorrect! 1.Cash amount should be than 1,000,000,000.00; 2.Must be a number with no more than 2 decimals");
 				isSuccess = -1;
 				return ERROR;
 			} else {
 				amount = 100 * Double.parseDouble(amountString);
 				Long a = Math.round(amount);
 
 				if (checkAndWithdraw(user.getId(), a) == false) {
 					this.addActionError("You do not have enough balance.");
 					isSuccess = -1;
 					return ERROR;
 				}
 
 				this.isSuccess = 1;
 				return SUCCESS;
 			}
 
 		}
 		this.addActionError("error!");
 		this.isSuccess = -1;
 		return ERROR;
 
 	}
 
 	public static synchronized boolean checkAndWithdraw(int uId,
 			long inputAmount) throws Exception {
 		// -- the transaction dao needs to be fixed here
 		ArrayList<Transaction> transactions = TransactionDao.getInstance()
 				.getPendTransByUserIdOp(uId, Transaction.TRANS_TYPE_BUY);
 		ArrayList<Transaction> transactions2 = TransactionDao.getInstance()
 				.getPendTransByUserIdOp(uId, Transaction.TRANS_TYPE_WITHDRAW);
 		Map session = ActionContext.getContext().getSession();
 		Sysuser user = (Sysuser) session.get(LoginAction.SYSUSER);
 		long avaiBalance = user.getCash();
 		if (avaiBalance < 0) {
 			return false;
 		}
 		if (transactions.size() != 0) {
 			for (Transaction t : transactions) {
 				avaiBalance -= t.getAmount();
 			}
 		}
 		if (transactions2.size() != 0) {
 			for (Transaction t : transactions2) {
 				avaiBalance -= t.getAmount();
 			}
 		}
 		if (avaiBalance < inputAmount) {
 			return false;
 		}
 		Transaction t = new Transaction();
 		t.setAmount(inputAmount);
 		t.setStatus(Transaction.TRANS_STATUS_PENDING);
 		t.setTransactionType(Transaction.TRANS_TYPE_WITHDRAW);
 		while (true) {
 			int i = TransitionDay.getInstance().newTransaction(uId, t);
 			if (i == TransitionDay.SUCCESS)
 				break;
 		}
 		return true;
 	}
 
 	public String showDeposit() throws Exception {
 		// Map session = ActionContext.getContext().getSession();
 		isSuccess = 0;
 		if (userId == null) {
 			userId = 0;
 			isSuccess = -1;
 			this.addActionError("Error, Can not get User ID.");
 			return ERROR;
 		} else {
 			try {
 				this.user = SysuserDao.getInstance().getByUserId(userId);
 			} catch (Exception e) {
 
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		// available balance check here
 		ArrayList<Transaction> transactions = TransactionDao.getInstance()
 				.getPendTransByUserIdOp(user.getId(),
 						Transaction.TRANS_TYPE_BUY);
 		ArrayList<Transaction> transactions2 = TransactionDao.getInstance()
 				.getPendTransByUserIdOp(user.getId(),
 						Transaction.TRANS_TYPE_WITHDRAW);
 		long avaiBalance = user.getCash();
 		if (transactions.size() != 0) {
 			for (Transaction t : transactions) {
 				avaiBalance -= t.getAmount();
 			}
 		}
 		if (transactions2.size() != 0) {
 			for (Transaction t : transactions2) {
 				avaiBalance -= t.getAmount();
 			}
 		}
 	//	double as = avaiBalance / 100.0;
 		setAvailBalanceString(cashDFormat.format(avaiBalance / 100.0));
 
 		return SUCCESS;
 
 	}
 
 	public String deposit() throws Exception {
 		// Map session = ActionContext.getContext().getSession();
 		isSuccess = 0;
 
 		try {
 			if (userId == null) {
 				userId = 0;
 				isSuccess = -1;
 				this.addActionError("Error, Can not get User ID.");
 				return ERROR;
 			} else {
 				user = SysuserDao.getInstance().getByUserId(userId);
 			}
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		if (user != null) {
 			ArrayList<Transaction> transactions = TransactionDao.getInstance()
 					.getPendTransByUserIdOp(user.getId(),
 							Transaction.TRANS_TYPE_BUY);
 			ArrayList<Transaction> transactions2 = TransactionDao.getInstance()
 					.getPendTransByUserIdOp(user.getId(),
 							Transaction.TRANS_TYPE_WITHDRAW);
 			long avaiBalance = user.getCash();
 			if (transactions.size() != 0) {
 				for (Transaction t : transactions) {
 					avaiBalance -= t.getAmount();
 				}
 			}
 			if (transactions2.size() != 0) {
 				for (Transaction t : transactions2) {
 					avaiBalance -= t.getAmount();
 				}
 			}
 			double as = avaiBalance / 100.0;
 			setAvailBalanceString(cashDFormat.format(avaiBalance / 100.0));
 			// amount = Double.parseDouble(amountString);
 			
 			if (amountString == null || amountString.equals("")
 					|| Double.parseDouble(amountString) == 0
 					|| Double.parseDouble(amountString) < 0.01) {
 				this.addActionError("Request amount can not be empty or zero, and it should be larger than $0.01!");
 				this.isSuccess = -1;
 				return ERROR;
 			} /*
 			 * else if (amountString.length() > 16) {
 			 * this.addActionError("The cash number can't be more than 15 digits!"
 			 * ); isSuccess = -1; return ERROR;}
			 */else if (!checkCashFormat(amountString, 9, 2)) {
				this.addActionError("Cahs Fomat Incorrect! 1.Cash amount should be less than 1,000,000,000.00; 2.Must be a number with no more than 2 decimals");
 				isSuccess = -1;
 				return ERROR;
 			} else {
 				// amount = Double.parseDouble(amountString);
 				// long a = (long) (amount * 100);
 				amount = 100 * Double.parseDouble(amountString);
 				System.out.println("amount" + amount);
 				Long a = Math.round(amount);
 				System.out.println("a" + a);
 				if (checkAndDeposit(user.getId(), a) == false) {
 					this.addActionError("You do not have enough balance.");
 					isSuccess = -1;
 					return ERROR;
 				}
 				this.isSuccess = 1;
 				return SUCCESS;
 
 			}
 
 		}
 		this.addActionError("error!");
 		this.isSuccess = -1;
 		return ERROR;
 	}
 
 	public static synchronized boolean checkAndDeposit(int uId, long inputAmount)
 			throws Exception {
 		// -- the transaction dao needs to be fixed here
 		ArrayList<Transaction> transactions = TransactionDao.getInstance()
 				.getPendTransByUserIdOp(uId, Transaction.TRANS_TYPE_BUY);
 		ArrayList<Transaction> transactions2 = TransactionDao.getInstance()
 				.getPendTransByUserIdOp(uId, Transaction.TRANS_TYPE_WITHDRAW);
 		Map session = ActionContext.getContext().getSession();
 		Sysuser user = SysuserDao.getInstance().getByUserId(uId);
 		long avaiBalance = user.getCash();
 		if (avaiBalance < 0) {
 			return false;
 		}
 		if (transactions.size() != 0) {
 			for (Transaction t : transactions) {
 				avaiBalance -= t.getAmount();
 			}
 		}
 		if (transactions2.size() != 0) {
 			for (Transaction t : transactions2) {
 				avaiBalance -= t.getAmount();
 			}
 		}
 
 		Transaction t = new Transaction();
 		t.setAmount(inputAmount);
 		System.out.println("inpuAmount" + inputAmount);
 		t.setStatus(Transaction.TRANS_STATUS_PENDING);
 		t.setTransactionType(Transaction.TRANS_TYPE_DEPOSIT);
 		while (true) {
 			int i = TransitionDay.getInstance().newTransaction(uId, t);
 			if (i == TransitionDay.SUCCESS)
 				break;
 		}
 		return true;
 	}
 
 	private static boolean checkCashFormat(String cashString, int beforeD,
 			int afterD) {
 		int i, j, flag = 0, loopTime = 0, flag2 = 0;
 		StringBuffer cashCheckZero = new StringBuffer();
 		for (j = 0; j < cashString.length() - 1; j++) {
 			if (cashString.charAt(j) == '0' && flag2 == 0
 					&& cashString.charAt(j + 1) != '.')
 				;
 			else {
 				flag2 = 1;
 				cashCheckZero.append(cashString.charAt(j));
 			}
 		}
 		cashCheckZero.append(cashString.charAt(j));
 		cashString = cashCheckZero.toString();
 		for (i = 0; i < cashString.length(); i++) {
 			int asc = cashString.charAt(i);
 			if (i == 0) {
 				if (asc < 48 || asc > 57)
 					return false;
 			}
 			if ((asc < 48 || asc > 57) && asc != 46)
 				return false;
 			if (asc == 46) {
 				flag = 1;
 				break;
 			}
 		}
 
 		if (i > beforeD)
 			return false;
 		for (i++; i < cashString.length() && flag == 1;) {
 			int asc = cashString.charAt(i);
 			if (asc < 48 || asc > 57)
 				return false;
 			i++;
 			loopTime++;
 		}
 		if (loopTime > afterD)
 			return false;
 		return true;
 	}
 
 
 
 }
