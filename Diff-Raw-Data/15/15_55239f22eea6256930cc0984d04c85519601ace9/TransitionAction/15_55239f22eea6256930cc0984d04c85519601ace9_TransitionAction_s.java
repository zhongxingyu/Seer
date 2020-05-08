 package com.action;
 
 import java.text.DecimalFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 
 import com.bu.TransitionDay;
 import com.opensymphony.xwork2.ActionSupport;
 import com.pojo.Fund;
 import com.pojo.FundPriceHistory;
 
 @SuppressWarnings("serial")
 public class TransitionAction extends ActionSupport {
 
 	private ArrayList<Fund> funds;
 	private FundPriceHistory fundPriceHistory;
 	private Date lastTradingDate;
 	private String lastTradingDateString;
 	private Date closingDate;
 	private String closingDateString;
 	private ArrayList<Double> closingPrice;
 	private ArrayList<String> closingPriceString;
 	private ArrayList<Integer> fundid;
 	private int isSuccess;
 
 	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
 	SimpleDateFormat df2 = new SimpleDateFormat("MM-dd-yyyy");
 
 	//private DecimalFormat cashDFormat = new DecimalFormat("###,##0.00");
 
 	public String showTransition() {
 		// this.isSuccess = 0;
 		this.lastTradingDate = TransitionDay.getInstance()
 				.getLastTransitionDay();
 		if (lastTradingDate != null) {
 			this.lastTradingDateString = df2.format(this.lastTradingDate);
 		} else {
 			this.lastTradingDateString = "-";
 		}
 
 		funds = TransitionDay.getInstance().getFundList();
 
 		return SUCCESS;
 
 	}
 
 	public String transition() {
 		this.isSuccess = 0;
 		this.lastTradingDate = TransitionDay.getInstance().getLastTransitionDay();
 
 		if (lastTradingDate != null) {
 			this.lastTradingDateString = df2.format(this.lastTradingDate);
 		} else {
 			this.lastTradingDateString = "-";
 		}
 
 		funds = TransitionDay.getInstance().getFundList();
 
 		if (fundid != null && closingPriceString != null) {
 			HashMap<Integer, Integer> idmaps = new HashMap<Integer, Integer>();
 			for (int j = 0; j < fundid.size(); j++) {
 				idmaps.put(fundid.get(j), j);
 			}
 
 			for (int i = 0; i < funds.size(); i++) {
 				Fund fund = funds.get(i);
 				if (idmaps.containsKey(fund.getId())) {
 					int index = idmaps.get(fund.getId());
 					if (index >= closingPriceString.size()
 							|| closingPriceString.get(index) == null
 							|| closingPriceString.get(index) == "") {
 						this.addActionError("Fund value can not be empty or zero, and it should be larger than $0.01!");
 						isSuccess = -1;
 					} else {
 						if (Double.parseDouble(closingPriceString.get(index)) == 0
 								|| Double.parseDouble(closingPriceString.get(index)) < 0.01) {
 							this.addActionError("Fund value can not be empty or zero, and it should be larger than $0.01!");
 							isSuccess = -1;
 						}  else if (!checkCashFormat(closingPriceString.get(index), 15, 2)) {
 							this.addActionError("Cahs Fomat Incorrect! 1.Cash amount can't be larger than 1,0,000.00; 2.Must be a number with no more than 2 decimals");
 							isSuccess = -1;
 						} else if (Double.parseDouble(closingPriceString
 								.get(index)) >= 10000) {
 							this.addActionError("Fund unit price should less than $10,000");
 							isSuccess = -1;
 						} else
 							fund.setCur(Double.parseDouble(closingPriceString
 									.get(index)));
 					}
 
 				}
 			}
 		} else {
 			isSuccess = -1;
 			this.addActionError("Error!");
 		}
 
 		if (closingDateString != null && !closingDateString.equals("")) {
 			// System.out.println(closingDateString);
 			try {
 				this.closingDate = df.parse(closingDateString);
				System.out.println(closingDate);
 				// this.lastTradingDate = df.parse(lastTradingDateString);
 				// System.out.println(lastTradingDate);
 			} catch (ParseException e) {
 				// TODO Auto-generated catch block
 				this.addActionError("The choosen date passed, please choose a future date!");
 				this.setIsSuccess(-1);
 				return ERROR;
 			}
 			if (this.lastTradingDate.getTime() >= this.closingDate.getTime()) {
 				this.addActionError("The choosen date passed, please choose a future date!");
 				this.setIsSuccess(-1);
 				return ERROR;
 			} /*
 			 * else if(fundid!=null&&closingPriceString!=null){
 			 * 
 			 * HashMap<Integer, Integer> idmaps = new HashMap<Integer,
 			 * Integer>(); for (int j = 0; j < fundid.size(); j++) {
 			 * idmaps.put(fundid.get(j), j); }
 			 * 
 			 * for (int i = 0; i < funds.size(); i++) { Fund fund =
 			 * funds.get(i); if (idmaps.containsKey(fund.getId())) { int index =
 			 * idmaps.get(fund.getId()); if (index >= closingPriceString.size()
 			 * || closingPriceString.get(index) == null ||
 			 * closingPriceString.get(index).equals(0)) {
 			 * this.addActionError("Fund value can not be empty or zero!");
 			 * isSuccess = -1; } else { if
 			 * (closingPriceString.get(index).length() > 16) {
 			 * this.addActionError
 			 * ("The cash number can't be more than 15 digits!"); isSuccess =
 			 * -1; } else if (!checkCashFormat(closingPriceString .get(index)))
 			 * { this.addActionError(
 			 * "The cash format isn't correct. You must input number with no more than 2 decimals!"
 			 * ); isSuccess = -1; } else if
 			 * (Double.parseDouble(closingPriceString .get(index)) >= 100000) {
 			 * this.addActionError("UnitPrice can not larger than 10,000");
 			 * isSuccess = -1; } else
 			 * fund.setCur(Double.parseDouble(closingPriceString.get(index))); }
 			 * 
 			 * } } } else { isSuccess = -1;
 			 * this.addActionError("There is no funds!"); }
 			 */
 
 			if (isSuccess == -1) {
 				return ERROR;
 			}
 
 			if (funds.size() != fundid.size()) {
 				isSuccess = -1;
 				this.addActionError("There is a new fund, please provide its price.");
 				return ERROR;
 			}
 
 			int checkNum = TransitionDay.getInstance().commitTransitionDay(
 					funds, closingDate);
 			if (checkNum == -3) {
 				isSuccess = -1;
 				this.addActionError("There is a new fund, please provide its price.");
 				return ERROR;
 			} else if (checkNum == 0) {
 				isSuccess = 1;
 				return SUCCESS;
 			} else if (checkNum == -1) {
 				isSuccess = -1;
 				this.addActionError("This transition is failed!");
 				return ERROR;
 			} else if (checkNum == -2) {
 				isSuccess = -1;
 				this.addActionError("Another transition is processing, please retry later!");
 				return ERROR;
 			}
 		}
 
 		else {
 			this.addActionError("Closing date can not be empty!");
 			this.setIsSuccess(-1);
 			return ERROR;
 		}
 
 		this.addActionError("Error!");
 		this.setIsSuccess(-1);
 		return SUCCESS;
 
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
 
 	private String checkStatus(int checkNum) {
 		// if (checkNum == -3) {
 		// isSuccess = -1;
 		// this.addActionError("There is a new fund, please provide its price.");
 		// checkNum = TransitionDay.getInstance().commitTransitionDay(funds,
 		// closingDate);
 		// return ERROR;
 		// }
 		if (checkNum == 0) {
 			isSuccess = 1;
 			return SUCCESS;
 		} else if (checkNum == -1) {
 			isSuccess = -1;
 			this.addActionError("This transition is failed!");
 			return ERROR;
 		} else if (checkNum == -2) {
 			isSuccess = -1;
 			this.addActionError("System busy, please retry later!");
 			return ERROR;
 		} // else if (checkNum == -3) {
 			// isSuccess = -1;
 			// this.addActionError("There is a new fund, please provide its price.");
 			// return ERROR;
 		// }
 
 		this.addActionError("There is a new fund, please provide its price.");
 		// checkNum = TransitionDay.getInstance().commitTransitionDay(funds,
 		// closingDate)
 		return ERROR;
 
 	}
 
 	private String updatePrice() {
 
 		for (int i = 0; i < funds.size(); i++) {
 			for (int j = 0; j < fundid.size(); j++) {
 
 				if (funds.get(i).getId().equals(fundid.get(j))) {
 					if (closingPrice.get(j) == null
 							|| closingPrice.get(j).equals(0)) {
 						this.addActionError("Fund value can not be empty or zero!");
 						this.setIsSuccess(-1);
 						return ERROR;
 					} else {
 						funds.get(i).setCur(closingPrice.get(j));
 					}
 
 				}
 			}
 		}
 		return SUCCESS;
 
 	}
 
 	public ArrayList<Fund> getFunds() {
 		return funds;
 	}
 
 	public void setFunds(ArrayList<Fund> funds) {
 		this.funds = funds;
 	}
 
 	public FundPriceHistory getFundPriceHistory() {
 		return fundPriceHistory;
 	}
 
 	public void setFundPriceHistory(FundPriceHistory fundPriceHistory) {
 		this.fundPriceHistory = fundPriceHistory;
 	}
 
 	public Date getLastTradingDate() {
 		return lastTradingDate;
 	}
 
 	public void setLastTradingDate(Date lastTradingDate) {
 		this.lastTradingDate = lastTradingDate;
 	}
 
 	public Date getClosingDate() {
 		return closingDate;
 	}
 
 	public void setClosingDate(Date closingDate) {
 		this.closingDate = closingDate;
 	}
 
 	public String getLastTradingDateString() {
 		return lastTradingDateString;
 	}
 
 	public void setLastTradingDateString(String lastTradingDateString) {
 		this.lastTradingDateString = lastTradingDateString;
 	}
 
 	public int getIsSuccess() {
 		return isSuccess;
 	}
 
 	public void setIsSuccess(int isSuccess) {
 		this.isSuccess = isSuccess;
 	}
 
 	public String getClosingDateString() {
 		return closingDateString;
 	}
 
 	public void setClosingDateString(String closingDateString) {
 		this.closingDateString = closingDateString;
 	}
 
 	public ArrayList<Double> getClosingPrice() {
 		return closingPrice;
 	}
 
 	public void setClosingPrice(ArrayList<Double> closingPrice) {
 		this.closingPrice = closingPrice;
 	}
 
 	public ArrayList<Integer> getFundid() {
 		return fundid;
 	}
 
 	public void setFundid(ArrayList<Integer> fundid) {
 		this.fundid = fundid;
 	}
 
 	public ArrayList<String> getClosingPriceString() {
 		return closingPriceString;
 	}
 
 	public void setClosingPriceString(ArrayList<String> closingPriceString) {
 		this.closingPriceString = closingPriceString;
 	}
 
 }
