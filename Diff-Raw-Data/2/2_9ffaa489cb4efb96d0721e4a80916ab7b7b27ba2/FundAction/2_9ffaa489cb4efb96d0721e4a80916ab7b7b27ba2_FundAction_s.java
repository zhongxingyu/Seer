 package com.action;
 
 import java.util.ArrayList;
 import java.util.Map;
 
 import com.bu.TransitionDay;
 import com.dao.FundDao;
 import com.dao.PositionDao;
 import com.opensymphony.xwork2.ActionContext;
 import com.opensymphony.xwork2.ActionSupport;
 import com.pojo.Fund;
 import com.pojo.Position;
 import com.pojo.Sysuser;
 import com.pojo.Transaction;
 
 public class FundAction extends ActionSupport{
 	private String name;
 	private String symbol;
 	private String keyword;
 	private String optionC;
 	public String getOptionC() {
 		return optionC;
 	}
 	public void setOptionC(String optionC) {
 		this.optionC = optionC;
 	}
 	private double lastPrice;
 	
 	private int fundId;
 	private int userId;
 	public int getUserId() {
 		return userId;
 	}
 	public void setUserId(int userId) {
 		this.userId = userId;
 	}
 	private double shares;
 	private int isSuccess = 0;
 	
 	private ArrayList<Fund> funds;
 	private ArrayList<Position> positions;
 
 	
 	//--getters and setters to be here--//
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
 
 	public ArrayList<Fund> getFunds(){
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
 	public double getShares() {
 		return shares;
 	}
 	//--end of getter and setter--//
 	
 	
 	public void setShares(double shares) {
 		this.shares = shares;
 	}
 	
 	public String showCreate() {
 		return SUCCESS;
 	}
 	
 	public String create(){
 		return SUCCESS;
 	}
 	
 	public String showfund() {
 		return SUCCESS;
 	}
 	
 	public String listAllFund(){
 		try {
 			funds=FundDao.getInstance().getAllList();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return SUCCESS;
 	}
 	//-- get all(a list) funds using user id to query --//
 	public String listFundByUserId(){
 		Sysuser user;
 		Map session = ActionContext.getContext().getSession();
 		user = (Sysuser) session.get(LoginAction.SYSUSER);
 		int userId = user.getId();
 		try{
 			positions=PositionDao.getInstance().getPositionByCostomerId(userId);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}		
 		return SUCCESS;
 	}
 	public String employeeListFundByUserId(){
 		try{
 			positions=PositionDao.getInstance().getPositionByCostomerId(userId);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}		
 		return SUCCESS;	
 	}
 	//--search fund by fund name--//
 	public String searchFundByName() throws Exception{
 		keyword = keyword.trim();
 		funds=FundDao.getInstance().getByName(keyword);
 		return SUCCESS;
 	}
 	//--search fund by fund name and user id--//
 	public String searchFundByOption() throws Exception{
 		Sysuser user;
 		Map session = ActionContext.getContext().getSession();
 		user = (Sysuser) session.get(LoginAction.SYSUSER);
 		
 		if(keyword.equals("") || keyword==null){
 			this.addActionError("you must enter the search key!");
 			isSuccess=-1;
 			return ERROR;
 		}
 		keyword = keyword.trim();
 		//--if the user choose default--//
 		if(optionC.equals("default")){
 			positions=PositionDao.getInstance().getAllList();
 			return SUCCESS;
 		}
 		else if(optionC.equals("fundName")){
 			funds = FundDao.getInstance().getByName(keyword);
 			if(funds.size()==0){
 				this.addActionError("Can not find this fund!");
 				isSuccess=-1;
 				return ERROR;
 			}
 			positions = PositionDao.getInstance().getListByCustomerIdFundId(user.getId(), funds.get(0).getId());
 			if(positions.size()==0){
 				this.addActionError("Can not find this fund!");
 				isSuccess=-1;
 				return ERROR;
 			}
 			isSuccess=1;
 			return SUCCESS;
 		}
 		else if(optionC.equals("fundSymbol")){
 			funds = FundDao.getInstance().getBySymbol(keyword);
 			if(funds.size()==0){
 				this.addActionError("Can not find this fund!");
 				isSuccess=-1;
 				return ERROR;
 			}
 			positions = PositionDao.getInstance().getListByCustomerIdFundId(user.getId(), funds.get(0).getId());
 			if(positions.size()==0){
 				this.addActionError("Can not find this fund!");
 				isSuccess=-1;
 				return ERROR;
 			}
 			isSuccess=1;
 			return SUCCESS;
 		}
 		return SUCCESS;
 	}
 	
 	public String createFund() throws Exception{
 		//System.out.println("*****************name="+name);
 		if(name==null || name .equals("")){
 			this.addActionError("The fund can not be empty!");
 			isSuccess=-1;
 			return ERROR;
 		}
 		if(symbol==null || symbol.equals("")){
 			this.addActionError("The symbol can not be empty!");
 			isSuccess=-1;
 			return ERROR;
 		}
 		if(symbol.length()>5){
 			this.addActionError("The symbol can not be over five letters!");
 			isSuccess=-1;
 			return ERROR;
 		}
 		name=name.trim();
 		symbol=symbol.trim();
		if(symbol.matches("[a-zA-Z]")==false){
 			this.addActionError("The symbol should be all letters!");
 			isSuccess=-1;
 			return ERROR;
 		}
 		
 		Fund f1 = new Fund();
 		f1.setName(name);
 		//--check if there is a fund already had the same name--//
 		if(FundDao.getInstance().isExist(f1)==true){
 			this.addActionError("The fund name already existed");
 			isSuccess=-1;
 			return ERROR;
 		}
 		Fund f2 = new Fund();
 		f2.setSymbol(symbol);
 		//
 		if(FundDao.getInstance().isExist(f2)==true){
 			this.addActionError("The fund symbol already existed");
 			isSuccess=-1;
 			return ERROR;
 		}
 		FundDao.getInstance().createFund(name, symbol);
 		isSuccess=1;
 		return SUCCESS;
 	}
 	public String showFundDetail() throws Exception{
 		Fund f=FundDao.getInstance().getById(fundId);
 		name=f.getName();
 		symbol=f.getSymbol();
 		return SUCCESS;
 	}
 	
 	//--get info from customer_sell2.jsp and set info want to bo post on sellfund.jsp
 	public String showSellFund() throws Exception{
 		//--get user id from session--//
 		Map session = ActionContext.getContext().getSession();
 		Sysuser user = (Sysuser) session.get(LoginAction.SYSUSER);
 		//--query by userId and fundId--//
 		Position p= PositionDao.getInstance().getByCustomerIdFundId(user.getId(), fundId);
 		if(p==null){
 			this.addActionError("Can not find this fund");
 			return ERROR;
 		}
 		name=p.getFundName();
 		symbol=p.getFundSymbol();
 		shares=p.getShares()/100.0;
 		return SUCCESS;
 	}
 	public String sell(){
 		Map session = ActionContext.getContext().getSession();
 		Sysuser user = (Sysuser) session.get(LoginAction.SYSUSER);
 		
 	//	this.addActionError("test here");
 	//	return ERROR;
 		
 		//--transaction needs to be filled in--//
 		
 		Transaction t = new Transaction();
 		long s = (long) (shares*1000);
 		t.setShares(s);
 		t.setStatus(Transaction.TRANS_STATUS_PENDING);
 		t.setTransactionType(Transaction.TRANS_TYPE_SELL);	
 		TransitionDay.getInstance().newTransaction(user.getId(), fundId, t);
 		
 		return SUCCESS;
 		
 	}
 	public String showBuyFund() throws Exception{
 		//--get user id from session--//
 		Map session = ActionContext.getContext().getSession();
 		Sysuser user = (Sysuser) session.get(LoginAction.SYSUSER);
 		Position p= PositionDao.getInstance().getByCustomerIdFundId(user.getId(), fundId);
 		Fund f = FundDao.getInstance().getById(fundId);
 		if(p==null){
 			shares=0/100.0;	
 		}
 		else{
 			shares=p.getShares()/100.0;
 		}
 		name=f.getName();
 		symbol=f.getSymbol();
 		
 		
 		return SUCCESS;
 	}
 
 
 }
