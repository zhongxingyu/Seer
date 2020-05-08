 package com.omartech.tdg.service;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.omartech.tdg.exception.ShopException;
 import com.omartech.tdg.mapper.FinanceUnitMapper;
 import com.omartech.tdg.mapper.ShopSettingMapper;
 import com.omartech.tdg.model.Coinage;
 import com.omartech.tdg.model.FinanceRecord;
 import com.omartech.tdg.model.FinanceUnit;
 import com.omartech.tdg.model.Order;
 import com.omartech.tdg.model.OrderItem;
 import com.omartech.tdg.model.Page;
 import com.omartech.tdg.model.Product;
 import com.omartech.tdg.model.ProductLine;
 import com.omartech.tdg.model.ShopSetting;
 import com.omartech.tdg.model.TranslationTask;
 import com.omartech.tdg.utils.FinanceType;
 import com.omartech.tdg.utils.OrderStatus;
 import com.omartech.tdg.utils.SystemDefaultSettings;
 import com.omartech.tdg.utils.TaskStatus;
 import com.omartech.tdg.utils.UserType;
 @Service
 public class FinanceService {
 	
 	@Autowired
 	private FinanceUnitMapper financeUnitMapper;
 	@Autowired
 	private FinanceRecordService financeRecordService;
 	@Autowired
 	private ShopSettingMapper shopSettingMapper;
 	@Autowired
 	private ProductLineService productLineService;
 	@Autowired
 	private OrderService orderService;
 	@Autowired
 	private ProductService productService;
 	/**
 	 * 根据unit的id查找
 	 * @param id
 	 * @return
 	 */
 	public FinanceUnit getFinanceUnitById(int id){
 		return financeUnitMapper.getFinanceUnitById(id);
 	}
 	
 	/**
 	 * 根据订单id查找其所有账目项
 	 * @param orderId
 	 * @return
 	 */
 	public List<FinanceUnit> getFinanceUnitsByOrderId(int orderId){
 		return financeUnitMapper.getFinanceUnitsByRelatedIdAndRelatedType(orderId, FinanceUnit.OrderRelated);
 	}
 	public List<FinanceUnit> getFinanceUnitsByTransitionId(int translationTaskId){
 		return financeUnitMapper.getFinanceUnitsByRelatedIdAndRelatedType(translationTaskId, FinanceUnit.TranslationRelated);
 	}
 	
 	@Transactional(rollbackFor = Exception.class)
 	public void computeForSeller(Date begin, Date end, int userId){
 		String user = contructID(userId, UserType.SELLER);
 		FinanceRecord record = new FinanceRecord();
 		//收入
 		List<FinanceUnit> unitsReceiveThisRun = financeUnitMapper.getFinanceByReceiverAndMonthByPage(user, begin, end, null);
 		float receive = 0f;//从平台收的钱
 		/**
 		 * 计算平台需要支付给卖家的钱
 		 * 1. 正常买卖
 		 */
 		for(FinanceUnit unit : unitsReceiveThisRun){
 			
 			if(unit.getStatus() != FinanceUnit.NOPAY){
 				continue;
 			}
 			
 			int financeType = unit.getFinanceType();
 			int financeDetailsType = unit.getFinanceDetailsType();
 			float money = unit.getMoney();
 			switch(financeType){
 			case FinanceType.Order://订单的钱
 				if(financeDetailsType == FinanceType.Normal){//正常订单
 					Order order = orderService.getOrderById(unit.getRelatedId());
 					int orderStatus  = order.getOrderStatus();
 					if(
 						orderStatus == OrderStatus.AUTOCLOSE || //自动结束
 						orderStatus == OrderStatus.RECEIVE || //已收货
 						orderStatus == OrderStatus.CLOSE //管理员关闭
 					){
 						receive += money;
 						record.addId(unit.getId());
 					}
 				}
 				break;
 			case FinanceType.Other:
 				receive += money;
 				record.addId(unit.getId());
 				break;
 			}
 			
 		}
 		//支出
 		List<FinanceUnit> unitsSendThisRun = financeUnitMapper.getFinanceBySenderAndMonthByPage(user, begin, end, null);
 		float translationFee = 0f;//付给平台的翻译钱
 		float returnFee = 0f; //返款给平台的钱
 		float storeFee = 0f;//使用仓库的钱
 		float serviceFee = 0f;//付给平台的服务钱
 		float otherFee = 0f;
 		/**
 		 * 计算需要付给平台的钱
 		 * 1. 翻译
 		 * 2. 退款
 		 */
 		for(FinanceUnit unit : unitsSendThisRun){
 			if(unit.getStatus() != FinanceUnit.NOPAY){
 				continue;
 			}
 			int financeType = unit.getFinanceType();
 			int financeDetailsType = unit.getFinanceDetailsType();
 			float money = unit.getMoney();
 			switch(financeType){
 			case FinanceType.Translation://翻译的钱
 				translationFee += money;
 				record.addId(unit.getId());
 				break;
 			case FinanceType.Order://给平台的返款，如退款
 				if(financeDetailsType == FinanceType.Return){
 					returnFee += money;
 					record.addId(unit.getId());
 				}
 				break;
 			case FinanceType.Other:
 				otherFee += money;
 				record.addId(unit.getId());
 				break;
 			case FinanceType.Store:{
 				Order order = orderService.getOrderById(unit.getRelatedId());
 				int orderStatus  = order.getOrderStatus();
 				if(
 						orderStatus == OrderStatus.AUTOCLOSE || //自动结束
 						orderStatus == OrderStatus.RECEIVE || //已收货
 						orderStatus == OrderStatus.CLOSE //管理员关闭
 					){
 					storeFee += money;
 					record.addId(unit.getId());
 					}
 				}
 				break;
 			case FinanceType.Service:{
 				Order order = orderService.getOrderById(unit.getRelatedId());
 				int orderStatus  = order.getOrderStatus();
 				if(
 						orderStatus == OrderStatus.AUTOCLOSE || //自动结束
 						orderStatus == OrderStatus.RECEIVE || //已收货
 						orderStatus == OrderStatus.CLOSE //管理员关闭
 					){
 					serviceFee += money;
 					record.addId(unit.getId());
 					}
 				}
 				break;
 			}
 		}
 		
 		float totalGetFromAdmin = receive;
 		float payAdmin = translationFee + returnFee + storeFee + serviceFee + otherFee;
 		float total = totalGetFromAdmin - payAdmin;
 		
 		ShopSetting shopSetting = shopSettingMapper.getShopSettingBySellerId(userId);
 		int coinage  = 0;
 		if(shopSetting != null){
 			coinage = shopSetting.getDefaultCoinage();
 		}
 		
 		
 		record.setOrderMoney(totalGetFromAdmin);
 		record.setOtherMoney(otherFee);
 		record.setReceiver(contructID(userId, UserType.SELLER));
 		record.setServiceMoney(serviceFee);
 		record.setStoreMoney(storeFee);
 		record.setTotalGetFromAdmin(totalGetFromAdmin);
 		record.setTotalPayAdmin(payAdmin);
 		record.setTotal(total);
 		record.setBeginDate(begin);
 		record.setEndDate(end);
 		record.setCoinage(coinage);
 		
 		financeRecordService.insert(record);
 		
 	}
 	
 	/**
 	 * 对于翻译人员的财务
 	 * @param begin
 	 * @param end
 	 * @param userId
 	 */
 	@Transactional(rollbackFor = Exception.class)
 	public void computeForTranslator(Date begin, Date end, int userId){
 		String user = contructID(userId, UserType.TRANSLATOR);
 		FinanceRecord record = new FinanceRecord();
 		//收入
 		List<FinanceUnit> unitsReceiveThisRun = financeUnitMapper.getFinanceByReceiverAndMonthByPage(user, begin, end, null);
 		float receive = 0f;
 		for(FinanceUnit unit : unitsReceiveThisRun){
 			int financeType = unit.getFinanceType();
 			float money = unit.getMoney();
 			switch(financeType){
 			case FinanceType.Translation://翻译的钱
 				receive += money;
 				record.addId(unit.getId());
 				break;
 			}	
 		}
 		record.setTotalGetFromAdmin(receive);
 		record.setReceiver(contructID(userId, UserType.TRANSLATOR));
 		record.setBeginDate(begin);
 		record.setEndDate(end);
 		financeRecordService.insert(record);
 	}
 	/**
 	 * 批量更新财务单元状态
 	 * @param ids
 	 * @param status
 	 */
 	public void batchUpdate(Set<Integer> idset, int status){
 		if(idset!=null && idset.size()>0)
 			financeUnitMapper.batchUpdate(idset, status);
 	}
 	/**
 	 * 某类用户的收入情况
 	 * @param userId
 	 * @param userType
 	 * @param begin
 	 * @param end
 	 * @param page
 	 * @return
 	 */
 	public List<FinanceUnit> getFinanceByReceiverAndMonthByPage(int userId, String userType, Date begin, Date end, Page page){
 		String receiver = contructID(userId, userType);
 		
 		if(begin !=null && end !=null){
 			return financeUnitMapper.getFinanceByReceiverAndMonthByPage(receiver, begin, end, page);
 		}else{
 			return financeUnitMapper.getFinanceByReceiverByPage(receiver, page);
 		}
 	}
 	/**
 	 * 某类用户的支出情况
 	 * @param userId
 	 * @param userType
 	 * @param begin
 	 * @param end
 	 * @param page
 	 * @return
 	 */
 	public List<FinanceUnit> getFinanceBySenderAndMonthByPage(int userId, String userType, Date begin, Date end, Page page){
 		String sender = contructID(userId, userType);
 		
 		if(begin !=null && end !=null){
 			return financeUnitMapper.getFinanceBySenderAndMonthByPage(sender, begin, end, page);
 		}else{
 			return financeUnitMapper.getFinanceBySenderByPage(sender, page);
 		}
 	}
 	
 	/**
 	 * 插入订单相关项
 	 * @param order
 	 */
 	@Transactional(rollbackFor = Exception.class)
 	public void insertOrderFinance(Order order, int newStatus){
 		int originStatus = order.getOrderStatus();
 //		int orderId = order.getId();
 		switch(newStatus){
 			case OrderStatus.PAID://买家付款
 				if (originStatus == OrderStatus.NOPAY){//如果之前状态是未付款，才可以付款
 					insertPaidOrder(order);
 					insertServiceMoney(order);
 				}
 				break;
 //			case OrderStatus.COMPLAIN://订单被投诉
 //				//1.找到平台->卖家那条数据，改变状态
 //				//针对不同的原状态，操作不同
 //				FinanceUnit unit = getFinanceUnitByRelatedIdAndDetailsTypeForSeller(orderId, FinanceType.Normal);//订单id，同时是普通订单状态，是唯一的
 //				unit.setFinanceDetailsType(FinanceType.Claim);
 //				unit.setStatus(FinanceUnit.LOCK);
 //				unit.setCreateAt(new Date());
 //				update(unit);
 //				break;
 //			case OrderStatus.RETURN:
 //				FinanceUnit unit2 = getFinanceUnitByRelatedIdAndDetailsTypeForSeller(orderId, FinanceType.Normal);
 //				unit2.setFinanceDetailsType(FinanceType.Return);
 //				unit2.setStatus(FinanceUnit.LOCK);
 //				unit2.setCreateAt(new Date());
 //				update(unit2);
 //				break;
 		}
 	}
 	/**
 	 * 订单号，类型，收款人是卖家
 	 * @param relatedId
 	 * @param financeType
 	 * @return
 	 */
 	public FinanceUnit getFinanceUnitByRelatedIdAndDetailsTypeForSeller(int relatedId, int financeType){
 		return getFinanceUnitByRelatedIdAndDetailsType(relatedId, financeType, UserType.SELLER);
 	}
 	public FinanceUnit getFinanceUnitByRelatedIdAndDetailsTypeForAdmin(int relatedId, int financeType){
 		return getFinanceUnitByRelatedIdAndDetailsType(relatedId, financeType, UserType.ADMIN);
 	}
 	public FinanceUnit getFinanceUnitByRelatedIdAndDetailsTypeForTranslator(int relatedId, int financeType){
 		return getFinanceUnitByRelatedIdAndDetailsType(relatedId, financeType, UserType.TRANSLATOR);
 	}
 	private FinanceUnit getFinanceUnitByRelatedIdAndDetailsType(int relatedId, int financeType, String userType){
 		return financeUnitMapper.getFinanceUnitsByRelatedIdAndDetailsType(relatedId, financeType, userType);
 	}
 	
 	/**
 	 * 插入该订单的佣金记录
 	 * @param order
 	 */
 	public void insertServiceMoney(Order order){
 		List<OrderItem> orderItems = order.getOrderItems();
 		float moneySum = 0;
 		for(OrderItem item : orderItems){
 			int productLineId = item.getProductLineId();
 			ProductLine productLine = productLineService.getProductLineById(productLineId);
 			float serviceRate = productLine.getCommission();
 			float money = serviceRate * item.getPrice() * item.getNum();
 			moneySum += money;
 		}
 		FinanceUnit unit = new FinanceUnit(order);
 		unit.setMoney(moneySum);
 		unit.setReceiver(UserType.ADMIN);
 		unit.setSender(contructID(order.getSellerId(), UserType.SELLER));
 		unit.setFinanceType(FinanceType.Service);
 		unit.setFinanceDetailsType(FinanceType.ServiceNormal);
 		insert(unit);
 	}
 	/**
 	 * 插入某订单的退货仓库费
 	 */
 	public void insertStoreMoney(int orderId){
 		Order order = orderService.getOrderById(orderId);
 		FinanceUnit unit = new FinanceUnit(order);
 		unit.setFinanceType(FinanceType.Store);
 		float money = 0f;
 		
 		List<OrderItem> orderItems = order.getOrderItems();
 		for(OrderItem orderItem : orderItems){
 			int productId = orderItem.getProductId();
 			Product product = productService.getSimpleProductById(productId);
 			String size = product.getSizeWithPackage();
 			float moneyTmp = computeStoreFee(size);
 			money += (moneyTmp*orderItem.getNum());
 		}
 		int coinage = order.getCoinage();
 		money = Coinage.convertToAimCoinage(coinage, money, Coinage.Dollar);
 		unit.setMoney(money);
 		unit.setSender(UserType.SELLER+"-"+order.getSellerId());
 		unit.setReceiver(UserType.ADMIN);
 		insert(unit);
 	}
 	private float computeStoreFee(String size){
 		String[] nums = size.split("\\*");
 		float res = 1f;
 		for(String tmp : nums){
 			try{
 				float num = Float.parseFloat(tmp);
 				res *=num;
 			}catch(Exception e){
 				System.err.println("Conver wrong when compute the store fee");
 				res = 0;
 				break;
 			}
 		}
 		res = (float) (res/(100.0 * 100.0 * 100.0));
 		if(res < 1){//小于1立方 按 1立方收费
 			return SystemDefaultSettings.MoneyPerSizeOfStore;
 		}else{
 			return res * SystemDefaultSettings.MoneyPerSizeOfStore;
 		}
 	}
 	
 	/**
 	 * 翻译任务
 	 * 插入两条记录
 	 * 1.平台支付给翻译者
 	 * 2.卖家支付给平台
 	 * @param tt
 	 */
 	@Transactional(rollbackFor = Exception.class)
 	public void insertTranslationFinance(TranslationTask tt){
 		int status = tt.getStatus();//获取该任务的之前状态
 		FinanceUnit toTranslator = new FinanceUnit(tt);
 		toTranslator.setReceiver(contructID(tt.getTranslatorId(), UserType.TRANSLATOR));
 		toTranslator.setSender(UserType.ADMIN);
 		
 		FinanceUnit toAdmin = new FinanceUnit(tt);
 		toAdmin.setReceiver(UserType.ADMIN);
 		toAdmin.setSender(contructID(tt.getSellerId(), UserType.SELLER));
 		//翻译跟重新翻译的价格不同
 		switch(status){
 		case TaskStatus.NEW://新任务
 			toTranslator.setFinanceDetailsType(FinanceType.FirstTranslation);
 			toAdmin.setFinanceDetailsType(FinanceType.FirstTranslation);
 			break;
 		case TaskStatus.REDO://重新翻译
 			toTranslator.setFinanceDetailsType(FinanceType.ReTranslation);
 			toAdmin.setFinanceDetailsType(FinanceType.ReTranslation);
 			break;
 		}
 		insert(toTranslator);
 		insert(toAdmin);
 	}
 	/**
 	 * 插入两条记录，一条是 买家付给平台，一条是平台付给卖家
 	 */
 	private boolean insertPaidOrder(Order order){
 		FinanceUnit unit = new FinanceUnit(order);//买家付给平台
 		unit.setReceiver(UserType.ADMIN);
 		unit.setSender(contructID(order.getCustomerId(), UserType.CUSTOMER));
 		unit.setMoney(order.getPriceRMB());
 		unit.setFinanceDetailsType(FinanceType.Normal);
 		insert(unit);
 		
 		FinanceUnit unit2 = new FinanceUnit(order);//平台付给卖家
 		unit2.setReceiver(contructID(order.getSellerId(), UserType.SELLER));
 		unit2.setSender(UserType.ADMIN);
 		unit2.setMoney(order.getOriginTotal());
 		unit2.setFinanceDetailsType(FinanceType.Normal);
 		insert(unit2);
 		return true;
 	}
 	/**
 	 * 自动检测款项双方，同时设置币种
 	 * @param unit
 	 * @return
 	 */
 	public boolean insert(FinanceUnit unit){
 		String receiver = unit.getReceiver();
 		String sender = unit.getSender();
 		int userId = 0;
 		if(receiver.contains(UserType.SELLER)){//收款人是卖家
 			String[] tmpArray = receiver.split("-");
 			userId = Integer.parseInt(tmpArray[1]);
 			if(userId != 0){//给卖家设置币种
 				ShopSetting ss = shopSettingMapper.getShopSettingBySellerId(userId);
 				if(ss == null){
 					throw new ShopException();
 				}
 				int coinage = ss.getDefaultCoinage();
 				unit.setCoinage(coinage);
 			}
 		}else if(receiver.contains(UserType.TRANSLATOR)){//翻译人员默认是人民币
 			unit.setCoinage(Coinage.RMB);
 		}else if(receiver.contains(UserType.ADMIN)){//收款人是管理员
 			if(sender.contains(UserType.CUSTOMER)){
 				unit.setCoinage(Coinage.RMB);
 			}else if(sender.contains(UserType.SELLER)){
 				String[] tmpArray = sender.split("-");
 				userId = Integer.parseInt(tmpArray[1]);
 				ShopSetting ss = shopSettingMapper.getShopSettingBySellerId(userId);
 				if(ss == null){
 					throw new ShopException();
 				}
 				int coinage = ss.getDefaultCoinage();
 				unit.setCoinage(coinage);
 			}
 		}else if(receiver.contains(UserType.CUSTOMER)){//收款人是买家
 			unit.setCoinage(Coinage.RMB);
 		}
 		financeUnitMapper.insert(unit);
 		return true;
 	}
 	/**
 	 * 每次更新，订单时间就会变化
 	 * @param unit
 	 * @return
 	 */
 	public boolean update(FinanceUnit unit){
 		unit.setCreateAt(new Date());
 		financeUnitMapper.update(unit);
 		return true;
 	}
 	
 	/**
 	 * 更新该账单号对应的所有财务项的时间
 	 * @param orderId
 	 */
 	public void freshUnitTime(int orderId){
 		List<FinanceUnit> units = getFinanceUnitsByOrderId(orderId);
 		for(FinanceUnit unit : units){
 			update(unit);
 		}
 	}
 	
 	/**
 	 * 被投诉||申请退款中
 	 * 锁定支付给卖家的钱
 	 * @param orderId
 	 */
 	public void lockTheOrderById(int orderId){
 		List<FinanceUnit> units = getFinanceUnitsByOrderId(orderId);
 		for(FinanceUnit unit : units){
 			if(unit.getStatus() == FinanceUnit.NOPAY){
 				unit.setStatus(FinanceUnit.LOCK);
 				update(unit);
 			}
 		}
 	}
 	/**
 	 * 解锁支付给卖家的钱
 	 * @param orderId
 	 */
 	public void unlockTheOrderById(int orderId){
 		List<FinanceUnit> units = getFinanceUnitsByOrderId(orderId);
 		for(FinanceUnit unit : units){
 			if(unit != null && unit.getStatus() == FinanceUnit.LOCK){
 				unit.setStatus(FinanceUnit.NOPAY);
 				unit.setCreateAt(new Date());
 				update(unit);
 			}
 		}
 	}
 	
 	public void payMoneyBack(int orderId, float percent){
 		if(percent > 100){
 			System.err.println("the return money percent > 100 in payMoneyBack");
 			return ;
 		}
 
 		FinanceUnit originToSeller = getFinanceUnitByRelatedIdAndDetailsTypeForSeller(orderId, FinanceType.Normal);
 		FinanceUnit originToPlatform = getFinanceUnitByRelatedIdAndDetailsTypeForAdmin(orderId, FinanceType.Normal);
 		FinanceUnit serviceMoney = getFinanceUnitByRelatedIdAndDetailsTypeForAdmin(orderId, FinanceType.ServiceNormal);
 		
 		FinanceUnit sellerToPlatform = new FinanceUnit(originToSeller, percent);
 		sellerToPlatform.setReceiver(originToSeller.getSender());
 		sellerToPlatform.setSender(originToSeller.getReceiver());
 		sellerToPlatform.setFinanceDetailsType(FinanceType.Return);
 		
 		FinanceUnit platformToCustomer = new FinanceUnit(originToPlatform, percent);
 		platformToCustomer.setReceiver(originToPlatform.getSender());
 		platformToCustomer.setSender(originToPlatform.getReceiver());
 		platformToCustomer.setFinanceDetailsType(FinanceType.Return);
 		
 		FinanceUnit serviceMoneyBack = new FinanceUnit(serviceMoney, percent);
 		serviceMoneyBack.setReceiver(serviceMoney.getSender());
 		serviceMoneyBack.setSender(serviceMoney.getReceiver());
 		serviceMoneyBack.setFinanceDetailsType(FinanceType.ServiceBack);
 		
 		
 		insertDirectly(platformToCustomer);
 		insertDirectly(sellerToPlatform);
 		insertDirectly(serviceMoneyBack);
 	}
 	
 	
 	/**
 	 * 退全款
 	 * @param id
 	 * @param userType
 	 * @return
 	 */
 	public void payAllMoneyBack(int orderId){
 		payMoneyBack(orderId, 100);
 	}
 	private void insertDirectly(FinanceUnit unit){
 		financeUnitMapper.insert(unit);
 	}
 	
 	private String contructID(int id, String userType){
 		if(userType.equals(UserType.ADMIN)){
 			return UserType.ADMIN;
 		}else{
 			return userType +"-"+id;
 		}
 	}
 	
 	public void delete(int id){
 		FinanceUnit unit = getFinanceUnitById(id);
 		if(unit != null && unit.getStatus() == FinanceUnit.NOPAY){
 			financeUnitMapper.delete(id);
 		}
 		
 	}
 
 }
