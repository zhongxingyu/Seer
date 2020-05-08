 package ru.spbstu.students.web.actions;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import org.apache.log4j.Logger;
 import org.apache.struts2.interceptor.SessionAware;
 
import ru.spbstu.students.dao.AutobidsDAO;
 import ru.spbstu.students.dao.BidDAO;
 import ru.spbstu.students.dao.ItemDAO;
 import ru.spbstu.students.dao.UserDAO;
import ru.spbstu.students.dto.AutobidInfo;
 import ru.spbstu.students.dto.BidInfo;
 import ru.spbstu.students.util.PriorityThread;
 import ru.spbstu.students.web.PriorityAddBid;
 import ru.spbstu.students.web.PriorityGetBidList;
 import ru.spbstu.students.web.UserCategories;
 
 import com.opensymphony.xwork2.ModelDriven;
 
 public class BidAction extends BaseAction implements SessionAware, ModelDriven<BidInfo>  {
 
 	private static final Logger log = Logger.getLogger(BidAction.class);
 
 	private static final long serialVersionUID = -5972555139531303599L;
 	private BidInfo bid = new BidInfo();
 	private Map<String, Object> session; 
 	private BidDAO bidDao;
 	private List<BidInfo> bidList;
 	private String aucType;
 	private UserDAO userDao;
 	private ItemDAO itemDao;
 	private double lastBid;
	private AutobidsDAO autobidDao;
 	
 	public String bid(){
 		if (!session.containsKey("email"))
 			return ERROR;
 		
 		int userId = userDao.getUser((String)session.get("email")).getId();
 		bid.setUserID(userId);
 		bid.setTime(new Date());
 		
 		//int requestId = new Random().nextInt(Integer.MAX_VALUE);
 		
 		//log.info("Start  addBid transaction. Request ID: " + requestId + "User ID: " + userId + "Item ID: " + bid.getItemID());
 		
 		PriorityAddBid addBid = new PriorityAddBid();
 		addBid.setBid(bid);
 		addBid.setBidDao(bidDao);
 		addBid.setUserType(UserCategories.getKeyByLabel(userDao.getUser((String)session.get("email")).getCategory()));
 		
 		boolean isFind = false;
 		String result = null;
 		PriorityThread.addQueue.add(addBid);
 		while (true) {
 			for (PriorityAddBid b : PriorityThread.resQueue) {
 				if ((b.getBid().getUserID() == userId) && (b.getBid().getItemID() == bid.getItemID()) && b.getResult() != null) {
 					isFind = true;
 					result = b.getResult();
 					PriorityThread.resQueue.remove(b);
 					break;
 				}
 			}
 			if (isFind)
 				break;
		}
		
		List<AutobidInfo> autobidList = autobidDao.getAutobidList(bid.getItemID());
		if (!autobidList.isEmpty()) {
			bidDao.refreshBids(bid.getItemID());
		}
 		
 		if(!result.equals("success")){
 			//log.info("Finish addBid transaction, request ID: " + requestId);
 			return ERROR;
 		}
 		//log.info("Finish addBid transaction, request ID: " + requestId);
 		return SUCCESS;
 	}
 	
 	public String getBids(){	
 		
 		if (!session.containsKey("email"))
 			return ERROR;
 		
 		int itemId = (Integer) session.get("itemId");
 		int userId = userDao.getUser((String)session.get("email")).getId();
 		int requestId = new Random().nextInt(Integer.MAX_VALUE);
 		log.info("Start  getBid transaction. Request ID: " + requestId + ", User ID: " + userId + ", Item ID: " + itemId);
 		
 		try{
 			PriorityGetBidList bid = new PriorityGetBidList();
 			bid.setEmail((String)session.get("email"));
 			bid.setItemId(itemId);
 			bid.setUserType(UserCategories.getKeyByLabel(userDao.getUser((String)session.get("email")).getCategory()));
 			bid.setBidDao(bidDao);
 
 			boolean isFind = false;
 			PriorityThread.inQueue.add(bid);
 			while (true) {
 				for (PriorityGetBidList b : PriorityThread.outQueue) {
 					if (b.getEmail().equals((String) session.get("email"))
 							&& (b.getItemId() == itemId) && (b.getBidList() != null)) {
 						isFind = true;
 						bidList = b.getBidList();
 						PriorityThread.outQueue.remove(b);
 						break;
 					}
 				}
 				if (isFind)
 					break;
 			}
 		}
 		catch(Exception e){
 			log.info("Finish getBid transaction, Request ID: " + requestId);
 			return ERROR;
 		}
 		aucType = itemDao.getItem(itemId).getType();
 		if (!bidList.isEmpty()) {
 			lastBid = bidList.get(bidList.size()-1).getAmount();
 		}
 		log.info("Finish getBid transaction, Request ID: " + requestId);
 		return SUCCESS;
 	}
 	
 	public String buyItemDutch(){
 
 		if (!session.containsKey("email"))
 			return ERROR;
 		
 		int itemId = (Integer) session.get("itemId");
 		int userId = userDao.getUser((String)session.get("email")).getId();
 		try {
 			bidDao.dutchBuy(itemId, userId);
 		} catch (Exception e) {
 			return ERROR;
 		}
 		return SUCCESS;
 	}
 	
 	public void setUserDao(UserDAO userDao) {
 		this.userDao = userDao;
 	}
 	
 	public void setSession(Map<String, Object> session) {
 		this.session = session; 
 	}
 	
 	public BidInfo getModel() {
 		return bid;
 	}
 	
 	public void setBidDao(BidDAO bidDao) {
 		this.bidDao = bidDao;
 	}
 
 	public List<BidInfo> getBidList() {
 		return bidList;
 	}
 
 	public void setBidList(List<BidInfo> bidList) {
 		this.bidList = bidList;
 	}
 
 	public BidInfo getBid() {
 		return bid;
 	}
 
 	public void setBid(BidInfo bid) {
 		this.bid = bid;
 	}
 
 	public String getAucType() {
 		return aucType;
 	}
 
 	public void setAucType(String aucType) {
 		this.aucType = aucType;
 	}
 
 	public void setItemDao(ItemDAO itemDao) {
 		this.itemDao = itemDao;
 	}
 
 	public double getLastBid() {
 		return lastBid;
 	}		
 
 }
