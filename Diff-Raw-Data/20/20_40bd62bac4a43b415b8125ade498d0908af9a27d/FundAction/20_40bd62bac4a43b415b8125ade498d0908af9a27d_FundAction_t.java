 package com.action;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Map;
 
 import com.bu.TransitionDay;
 import com.dao.FundDao;
 import com.dao.FundPriceHistoryDao;
 import com.dao.PositionDao;
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
 
 
 	private ArrayList<Fund> funds;
 	private ArrayList<Position> positions;
 
 	// --getters and setters to be here--//
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
 				DecimalFormat dFormat = new DecimalFormat("###,##0.00");
 				DecimalFormat dFormat2 = new DecimalFormat("###,##0.000");
 
 				p.setLastPriceString(dFormat.format(priceInDouble));
 				p.setShareString(dFormat2.format(shareInDouble));
 				p.setShareValueString(dFormat.format(shareValue));
 
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
 		// System.out.println("*****************name="+name);
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
 		/*
 		 * Fund f1 = new Fund(); f1.setName(name); // --check if there is a fund
 		 * already had the same name--// if (FundDao.getInstance().isExist(f1)
 		 * == true) { this.addActionError("The fund name already existed");
 		 * isSuccess = -1; return ERROR; } Fund f2 = new Fund();
 		 * f2.setSymbol(symbol); // if (FundDao.getInstance().isExist(f2) ==
 		 * true) { this.addActionError("The fund symbol already existed");
 		 * isSuccess = -1; return ERROR; }
 		 */
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
		DecimalFormat dFormat2 = new DecimalFormat("###,##0.000");
		outputShareString = dFormat2.format(shares);
		
 		return SUCCESS;
 	}
 
 	public String sell() throws Exception {
 		Map session = ActionContext.getContext().getSession();
 		Sysuser user = (Sysuser) session.get(LoginAction.SYSUSER);
 		// --check if the share want to sell is smaller--//
 		Position p = PositionDao.getInstance().getByCustomerIdFundId(
 				user.getId(), fundId);
 		//double currentShares = p.getShares() / 1000.00;
 		name = p.getFundName();
 		symbol = p.getFundSymbol();
 		shares = p.getShares() / 1000.0;
 /*		if (inputShares == 0) {
 			this.addActionError("You must enter one number!");
 			isSuccess = -1;
 			return ERROR;
 		}
 		if (inputShares > currentShares) {
 
 			this.addActionError("Can not over sell!");
 			isSuccess = -1;
 			return ERROR;
 		}
 	*/	
 		if(inputShareString.equals("") || inputShareString == null){
 			this.addActionError("You must one number!");
 			isSuccess = -1;
 			return ERROR;
 		}
 		if(inputShareString.matches("[1-9]*.[1-9]*")==false){
 			this.addActionError("You must enter numbers!");
 			isSuccess = -1;
 			return ERROR;
 		}
 		// -- share number too big needs to be here --//
 		
 		// -- change string type to double --//
 		inputShares = Double.valueOf(inputShareString);
 		if(inputShares > shares){
 			this.addActionError("You can not over sell!");
 			isSuccess = -1;
 			return ERROR;
 		}
 		
 		
 		// ---transaction here----//
 		
 		 Transaction t = new Transaction(); 
 		 long s = (long) (inputShares*1000);
 		 t.setShares(s);
 		 t.setStatus(Transaction.TRANS_STATUS_PENDING);
 		 t.setTransactionType(Transaction.TRANS_TYPE_SELL);
 		 TransitionDay.getInstance().newTransaction(user.getId(), fundId, t);
 		 return SUCCESS;
 
 	}
 
 	public String showBuyFund() throws Exception {
 		// --get user id from session--//
 		Map session = ActionContext.getContext().getSession();
 		Sysuser user = (Sysuser) session.get(LoginAction.SYSUSER);
 		Position p = PositionDao.getInstance().getByCustomerIdFundId(
 				user.getId(), fundId);
 		Fund f = FundDao.getInstance().getById(fundId);
 		if (p == null) {
 			shares = 0 / 100.0;
 		} else {
 			shares = p.getShares() / 100.0;
 		}
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
 		if (p == null) {
 			shares = 0 / 100.0;
 		} else {
 			shares = p.getShares() / 100.0;
 		}
 		if (amount.equals("") || amount == null) {
 			this.addActionError("You must enter amount!");
 			isSuccess = -1;
 			return ERROR;
 		}
 		if (amount.matches("[1-9]*") == false) {
 			this.addActionError("You must enter numbers!");
 			isSuccess = -1;
 			return ERROR;
 		}
		if(Long.valueOf(amount)<0){
			this.addActionError("You must enter positive numbers!");
			isSuccess = -1;
			return ERROR;
		}
 
 		t.setAmount(Long.valueOf(amount));
 		t.setStatus(Transaction.TRANS_STATUS_PENDING);
 		t.setTransactionType(Transaction.TRANS_TYPE_SELL);
 		TransitionDay.getInstance().newTransaction(user.getId(), fundId, t);
 
 		return SUCCESS;
 	}
 
 }
