 package com.action;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Map;
 
 import com.bu.TransitionDay;
 import com.dao.FundDao;
 import com.dao.FundPriceHistoryDao;
 import com.dao.PositionDao;
 import com.dao.TransactionDao;
 import com.opensymphony.xwork2.ActionContext;
 import com.opensymphony.xwork2.ActionSupport;
 import com.pojo.Fund;
 import com.pojo.FundPriceHistory;
 import com.pojo.Position;
 import com.pojo.Sysuser;
 import com.pojo.Transaction;
 
 public class FundAction extends ActionSupport {
 	private String name;
 	private String symbol;
 	private String keyword;
 	private String optionC;
 	private double lastPrice;
 	private double shareValue;
 
 	private int fundId;
 	private int userId;
 
 	private double shares;
 	private int isSuccess = 0;
 	private double inputShares;
 	private String amount;
 	private String inputShareString;
 	private String outputShareString;
 	private String outputBalanceString;
 	private String outputAvaiBalanceString;
 
 	private ArrayList<Fund> funds;
 	private ArrayList<Position> positions;
 
 	private DecimalFormat cashDFormat = new DecimalFormat("###,##0.00");
 	private DecimalFormat shareDFormat = new DecimalFormat("###,##0.000");
 
 	// --getters and setters to be here--//
 	public String getOutputAvaiBalanceString() {
 		return outputAvaiBalanceString;
 	}
 
 	public void setOutputAvaiBalanceString(String outputAvaiBalanceString) {
 		this.outputAvaiBalanceString = outputAvaiBalanceString;
 	}
 
 	public String getOutputBalanceString() {
 		return outputBalanceString;
 	}
 
 	public void setOutputBalanceString(String outputBalanceString) {
 		this.outputBalanceString = outputBalanceString;
 	}
 
 	public String getOutputShareString() {
 		return outputShareString;
 	}
 
 	public void setOutputShareString(String outputShareString) {
 		this.outputShareString = outputShareString;
 	}
 
 	public String getInputShareString() {
 		return inputShareString;
 	}
 
 	public void setInputShareString(String inputShareString) {
 		this.inputShareString = inputShareString;
 	}
 
 	public double getShareValue() {
 		return shareValue;
 	}
 
 	public void setShareValue(double shareValue) {
 		this.shareValue = shareValue;
 	}
 
 	public String getAmount() {
 		return amount;
 	}
 
 	public void setAmount(String amount) {
 		this.amount = amount;
 	}
 
 	public int getUserId() {
 		return userId;
 	}
 
 	public void setUserId(int userId) {
 		this.userId = userId;
 	}
 
 	public String getOptionC() {
 		return optionC;
 	}
 
 	public void setOptionC(String optionC) {
 		this.optionC = optionC;
 	}
 
 	public double getLastPrice() {
 		return lastPrice;
 	}
 
 	public void setLastPrice(double lastPrice) {
 		this.lastPrice = lastPrice;
 	}
 
 	public int getIsSuccess() {
 		return isSuccess;
 	}
 
 	public void setIsSuccess(int isSuccess) {
 		this.isSuccess = isSuccess;
 	}
 
 	public String getKeyword() {
 		return keyword;
 	}
 
 	public void setKeyword(String keyword) {
 		this.keyword = keyword;
 	}
 
 	public int getFundId() {
 		return fundId;
 	}
 
 	public void setFundId(int fundId) {
 		this.fundId = fundId;
 	}
 
 	public ArrayList<Position> getPositions() {
 		return positions;
 	}
 
 	public ArrayList<Fund> getFunds() {
 		return funds;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getSymbol() {
 		return symbol;
 	}
 
 	public void setSymbol(String symbol) {
 		this.symbol = symbol;
 	}
 
 	public String showCreate() {
 		return SUCCESS;
 	}
 
 	public double getShares() {
 		return shares;
 	}
 
 	public void setShares(double shares) {
 		this.shares = shares;
 	}
 
 	public double getInputShares() {
 		return inputShares;
 	}
 
 	public void setInputShares(double inputShares) {
 		this.inputShares = inputShares;
 	}
 
 	public String create() {
 		return SUCCESS;
 	}
 
 	public String showfund() {
 		return SUCCESS;
 	}
 
 	// --end of getter and setter--//
 
 	public String listAllFund() {
 		try {
 			funds = FundDao.getInstance().getAllList();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return SUCCESS;
 	}
 
 	// -- get all(a list) funds using user id to query -- //
 	// -- when user click sell fund go to this function -- //
 	// -- success go to customer_sell.jsp --//
 	public String listFundByUserId() {
 		Sysuser user;
 		Map session = ActionContext.getContext().getSession();
 		user = (Sysuser) session.get(LoginAction.SYSUSER);
 		int userId = user.getId();
 		try {
 			positions = PositionDao.getInstance().getPositionByCostomerId(
 					userId);
 			for (Position p : positions) {
 				int fId = p.getFund().getId();
 				// System.out.println("*********id="+fId);
 				FundPriceHistory fph = FundPriceHistoryDao.getInstance()
 						.getLatestFundHistoryById(fId);
 				long price;
 				// -- if no history then set price to zero -- //
 				if (fph != null)
 					price = FundPriceHistoryDao.getInstance()
 							.getLatestFundHistoryById(fId).getPrice();
 				else
 					price = 0;
 
 				double priceInDouble = price / 100.0;
 				double shareInDouble = p.getShares() / 1000.0;
 				double shareValue = priceInDouble * shareInDouble;
 				// DecimalFormat cashDFormat = new DecimalFormat("###,##0.00");
 				// set these string into position, it would be easier for .jsp
 				// to output the result
 				p.setLastPriceString(cashDFormat.format(priceInDouble));
 				p.setShareString(shareDFormat.format(shareInDouble));
 				p.setShareValueString(cashDFormat.format(shareValue));
 
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return SUCCESS;
 	}
 
 	public String employeeListFundByUserId() {
 		try {
 			positions = PositionDao.getInstance().getPositionByCostomerId(
 					userId);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return SUCCESS;
 	}
 
 	// --search fund by fund name--//
 	public String searchFundByName() throws Exception {
 		keyword = keyword.trim();
 		funds = FundDao.getInstance().getByName(keyword);
 		return SUCCESS;
 	}
 
 	// --search fund by fund name and user id--//
 	public String searchAllFundByOption() throws Exception {
 		Sysuser user;
 		Map session = ActionContext.getContext().getSession();
 		user = (Sysuser) session.get(LoginAction.SYSUSER);
 
 		if (keyword.equals("") || keyword == null) {
 			funds = FundDao.getInstance().getAllList();
 			this.addActionError("you must enter the search key!");
 			isSuccess = -1;
 			return ERROR;
 		}
 		keyword = keyword.trim();
 		// --if the user choose default--//
 		if (optionC.equals("default")) {
 			funds = FundDao.getInstance().getAllList();
 			return SUCCESS;
 		} else if (optionC.equals("fundName")) {
 			funds = FundDao.getInstance().getByName(keyword);
 			if (funds.size() == 0) {
 				this.addActionError("Can not find this fund!");
 				isSuccess = -1;
 				return ERROR;
 			}
 			isSuccess = 1;
 			return SUCCESS;
 		} else if (optionC.equals("fundSymbol")) {
 			funds = FundDao.getInstance().getBySymbol(keyword);
 			if (funds.size() == 0) {
 				this.addActionError("Can not find this fund!");
 				isSuccess = -1;
 				return ERROR;
 			}
 			isSuccess = 1;
 			return SUCCESS;
 		}
 		return SUCCESS;
 	}
 
 	// --search fund by fund name and user id--//
 	public String searchFundByOption() throws Exception {
 		Sysuser user;
 		Map session = ActionContext.getContext().getSession();
 		user = (Sysuser) session.get(LoginAction.SYSUSER);
 
 		if (keyword.equals("") || keyword == null) {
 			this.addActionError("you must enter the search key!");
 			positions = PositionDao.getInstance().getPositionByCostomerId(
 					user.getId());
 			isSuccess = -1;
 			return ERROR;
 		}
 		keyword = keyword.trim();
 		// --if the user choose default--//
 		if (optionC.equals("default")) {
 			positions = PositionDao.getInstance().getAllList();
 			return SUCCESS;
 		} else if (optionC.equals("fundName")) {
 			funds = FundDao.getInstance().getByName(keyword);
 			if (funds.size() == 0) {
 				this.addActionError("Can not find this fund!");
 				isSuccess = -1;
 				return ERROR;
 			}
 			positions = PositionDao.getInstance().getListByCustomerIdFundId(
 					user.getId(), funds.get(0).getId());
 			if (positions.size() == 0) {
 				this.addActionError("Can not find this fund!");
 				isSuccess = -1;
 				return ERROR;
 			}
 			isSuccess = 1;
 			return SUCCESS;
 		} else if (optionC.equals("fundSymbol")) {
 			funds = FundDao.getInstance().getBySymbol(keyword);
 			if (funds.size() == 0) {
 				this.addActionError("Can not find this fund!");
 				isSuccess = -1;
 				return ERROR;
 			}
 			positions = PositionDao.getInstance().getListByCustomerIdFundId(
 					user.getId(), funds.get(0).getId());
 			if (positions.size() == 0) {
 				this.addActionError("Can not find this fund!");
 				isSuccess = -1;
 				return ERROR;
 			}
 			isSuccess = 1;
 			return SUCCESS;
 		}
 		return SUCCESS;
 	}
 
 	public String createFund() throws Exception {
 		if (name == null || name.equals("")) {
 			this.addActionError("The fund can not be empty!");
 			isSuccess = -1;
 			return ERROR;
 		}
 		if (symbol == null || symbol.equals("")) {
 			this.addActionError("The symbol can not be empty!");
 			isSuccess = -1;
 			return ERROR;
 		}
 		if (symbol.length() > 5) {
 			this.addActionError("The symbol can not be over five letters!");
 			isSuccess = -1;
 			return ERROR;
 		}
 		name = name.trim();
 		symbol = symbol.trim();
 		if (symbol.matches("[a-zA-Z]*") == false) {
 			this.addActionError("The symbol should be all letters!");
 			isSuccess = -1;
 			return ERROR;
 		}
 		// --check if the fund name or symbol is duplicate --//
 		ArrayList<Fund> fs = FundDao.getInstance().getByName(name);
 		if (fs.size() != 0) {
 			this.addActionError("The fund name is already exist!");
 			isSuccess = -1;
 			return ERROR;
 		}
 		fs = FundDao.getInstance().getBySymbol(symbol);
 		if (fs.size() != 0) {
 			this.addActionError("The fund symbol is already exist!");
 			isSuccess = -1;
 			return ERROR;
 		}
 
 		FundDao.getInstance().createFund(name, symbol);
 		isSuccess = 1;
 		return SUCCESS;
 	}
 
 	public String showFundDetail() throws Exception {
 		Fund f = FundDao.getInstance().getById(fundId);
 		name = f.getName();
 		symbol = f.getSymbol();
 		return SUCCESS;
 	}
 
 	// --get info from customer_sell2.jsp and set info want to bo post on
 	// sellfund.jsp
 	public String showSellFund() throws Exception {
 		// --get user id from session--//
 		Map session = ActionContext.getContext().getSession();
 		Sysuser user = (Sysuser) session.get(LoginAction.SYSUSER);
 		// --query by userId and fundId--//
 		Position p = PositionDao.getInstance().getByCustomerIdFundId(
 				user.getId(), fundId);
 		if (p == null) {
 			this.addActionError("Can not find this fund");
 			return ERROR;
 		}
 		name = p.getFundName();
 		symbol = p.getFundSymbol();
 		shares = p.getShares() / 1000.00;
 		// Format cashDFormat = new DecimalFormat("###,##0.000");
 		outputShareString = shareDFormat.format(shares);
 
 		return SUCCESS;
 	}
 
 	public String sell() throws Exception {
 		Map session = ActionContext.getContext().getSession();
 		Sysuser user = (Sysuser) session.get(LoginAction.SYSUSER);
 		Position p = PositionDao.getInstance().getByCustomerIdFundId(
 				user.getId(), fundId);
 		name = p.getFundName();
 		symbol = p.getFundSymbol();
 		shares = p.getShares() / 1000.0;
 		outputShareString = shareDFormat.format(shares);
 
 		if (inputShareString.equals("") || inputShareString == null) {
 			this.addActionError("You must enter shares!");
 			isSuccess = -1;
 			return ERROR;
 		}
 		inputShareString = inputShareString.trim();
 		if (inputShareString.matches("^[0-9]+([.][0-9]+)?$") == false) {
 			this.addActionError("You must enter numbers!");
 			isSuccess = -1;
 			return ERROR;
 		}
 
 		if (checkAndSell(fundId, user.getId(), Long.valueOf(inputShareString)) == false) {
 			this.addActionError("You can not over sell!");
 			isSuccess = -1;
 			return ERROR;
 		}
 
 		return SUCCESS;
 
 	}
 
 	public static synchronized boolean checkAndSell(int fId, int uId,
 			long shares) throws Exception {
 		// -- the transaction dao needs to be fixed here
 		ArrayList<Transaction> transactions = TransactionDao.getInstance()
 				.getPendTransByUserIdFundId(uId, fId);
 		Position p = PositionDao.getInstance().getByCustomerIdFundId(uId, fId);
 		long avaiShares = p.getShares();
		if(transactions.size()==0){
 			for (Transaction t : transactions) {
 				if (t.getTransactionType() == Transaction.TRANS_TYPE_SELL) {
 					avaiShares -= t.getShares();
 				}
 			}
 		}
 		if (avaiShares < shares) {
 			return false;
 		}
 		Transaction t = new Transaction();
 		t.setShares(shares);
 		t.setStatus(Transaction.TRANS_STATUS_PENDING);
 		t.setTransactionType(Transaction.TRANS_TYPE_SELL);
 		TransitionDay.getInstance().newTransaction(uId, fId, t);
 		return true;
 	}
 
 	public static synchronized boolean checkAndBuy(int fId, int uId,
 			long inputAmount) throws Exception {
 		// -- the transaction dao needs to be fixed here
 		ArrayList<Transaction> transactions = TransactionDao.getInstance()
 				.getPendTransByUserIdOp(uId, Transaction.TRANS_TYPE_BUY);
 		Map session = ActionContext.getContext().getSession();
 		Sysuser user = (Sysuser) session.get(LoginAction.SYSUSER);
 		long avaiBalance = user.getCash();
 		if(transactions.size()!=0){
 			for (Transaction t : transactions) {
 				avaiBalance -= t.getAmount();
 			}
 		}
 		if (avaiBalance < inputAmount) {
 			return false;
 		}
 		Transaction t = new Transaction();
 		t.setAmount(inputAmount);
 		t.setStatus(Transaction.TRANS_STATUS_PENDING);
 		t.setTransactionType(Transaction.TRANS_TYPE_BUY);
 		TransitionDay.getInstance().newTransaction(uId, fId, t);
 		return true;
 	}
 
 	public String showBuyFund() throws Exception {
 		// --get user id from session--//
 		Map session = ActionContext.getContext().getSession();
 		Sysuser user = (Sysuser) session.get(LoginAction.SYSUSER);
 		Position p = PositionDao.getInstance().getByCustomerIdFundId(
 				user.getId(), fundId);
 		Fund f = FundDao.getInstance().getById(fundId);
 		if (p == null) {
 			shares = 0 / 1000.0;
 			outputShareString = "-";
 		} else {
 
 			shares = p.getShares() / 1000.0;
 			// Format cashDFormat2 = new DecimalFormat("###,##0.000");
 			outputShareString = shareDFormat.format(shares);
 		}
 		outputAvaiBalanceString = cashDFormat.format(user.getCashes());
 		name = f.getName();
 		symbol = f.getSymbol();
 		return SUCCESS;
 	}
 
 	public String buy() throws Exception {
 		Transaction t = new Transaction();
 		Map session = ActionContext.getContext().getSession();
 		Sysuser user = (Sysuser) session.get(LoginAction.SYSUSER);
 		Fund f = FundDao.getInstance().getById(fundId);
 		Position p = PositionDao.getInstance().getByCustomerIdFundId(
 				user.getId(), fundId);
 		name = f.getName();
 		symbol = f.getSymbol();
 		outputAvaiBalanceString = cashDFormat.format(user.getCashes());
 		if (p == null) {
 			shares = 0 / 1000.0;
 			outputShareString = "-";
 		} else {
 			shares = p.getShares() / 1000.0;
 			// Format cashDFormat = new DecimalFormat("###,##0.000");
 			outputShareString = shareDFormat.format(shares);
 		}
 		// -- error check here-- //
 		if (amount.equals("") || amount == null) {
 			this.addActionError("You must enter amount!");
 			isSuccess = -1;
 			return ERROR;
 		}
 		if (amount.matches("^[0-9]+([.][0-9]+)?$") == false) {
 			this.addActionError("You must enter numbers and it can not be negtive.");
 			isSuccess = -1;
 			return ERROR;
 		}
 		// --can not buy too much or too less check here --//
 		Double newAmount = Double.valueOf(amount);
 
 		if (newAmount == 0 || newAmount < 0.01) {
 			this.addActionError("You must enter non-zero numbers and it should be greater than 0.01.");
 			isSuccess = -1;
 			return ERROR;
 		}
 		if (newAmount > 100000) {
 			this.addActionError("You can not buy more than 100000 dollars.");
 			isSuccess = -1;
 			return ERROR;
 		}
 
 		if (checkAndBuy(fundId, userId, Long.valueOf(amount)) == false) {
 			this.addActionError("You do not have enough balance.");
 			isSuccess = -1;
 			return ERROR;
 		}
 
 		// newAmount*=100;
 		// long a = (long)(newAmount*100);
 		// ---transaction here----//
 		/*
 		 * t.setAmount(a); t.setStatus(Transaction.TRANS_STATUS_PENDING);
 		 * t.setTransactionType(Transaction.TRANS_TYPE_BUY);
 		 * TransitionDay.getInstance().newTransaction(user.getId(), fundId, t);
 		 */
 		return SUCCESS;
 	}
 
 }
