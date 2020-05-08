 package com.onb.orderingsystem.domain;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.onb.orderingsystem.exceptions.CreditLimitExceededException;
 import com.onb.orderingsystem.utils.Enumerators.OrderStatus;
 
 
 public class Customer {
 	private int custID;
 	private String custCompanyName;
 	private BigDecimal custCreditLimit = null;
 	private List<Order> custOrder = new ArrayList<Order>();
 	
 	private static final BigDecimal DISCOUNT_REQUIREMENT = new BigDecimal(1000000.00);
 	
 	public Customer(int custID, String company, List<Order> custOrder) {
 		super();
 		this.custID = custID;
 		this.custCompanyName = company;
 		this.custOrder = custOrder;
 	}
 	
 	public Customer(int custID, String company, BigDecimal creditLimit ) {
 		super();
 		this.custID = custID;
 		this.custCompanyName = company;
 		this.custCreditLimit = creditLimit;
 	}
 
 	public Customer() {
 		super();
 	}
 
 	public int getCustID() {
 		return custID;
 	}
 
 	public void setCustID(int custID) {
 		this.custID = custID;
 	}
 
	public String getCustFirstName() {
		return custCompanyName;
	}

 	public String getCompanyName() {
 		return custCompanyName;
 	}
 
 	public void setCompanyName(String companyName) {
 		this.custCompanyName = companyName;
 	}
 
 	public BigDecimal getCustCreditLimit() {
 		return custCreditLimit;
 	}
 
 	public List<Order> getCustOrder() {
 		return custOrder;
 	}
 
 	public void setCustOrder(List<Order> custOrder) {
 		this.custOrder = custOrder;
 	}
 	
 	/**
 	 * @param order
 	 * Adds order to the existing orders of the customer
 	 */
 	public void addOrder(Order order) {
 		this.custOrder.add(order);
 	}
 	
 	/**
 	 * Computes the total paid orders of the customer from the history
 	 *
 	 */
 	public final BigDecimal computeTotalPaidOrders() {
 		BigDecimal totalPaidOrders = new BigDecimal(0.0);
 		for(Order order : this.custOrder){
 			if(order.getOrderStatus() == OrderStatus.PAID)
 				totalPaidOrders = totalPaidOrders.add(order.computeOrderTotalPrice());
 		}
 		return totalPaidOrders;
 	}
 	
 	/**
 	 * Computes the total unpaid orders of the customer from the history
 	 * 
 	 */
 	public BigDecimal computeTotalUnpaidOrders() {
 		BigDecimal totalUnpaidOrders = new BigDecimal(0.0);
 		for(Order order : this.custOrder){
 			if(order.getOrderStatus() == OrderStatus.UNPAID)
 				totalUnpaidOrders = totalUnpaidOrders.add(order.computeOrderTotalPrice());
 		}
 		return totalUnpaidOrders;
 	}
 	
 	/**
 	 * Computes the credit limit of a customer
 	 *
 	 */
 	public final BigDecimal computeCreditLimit() {
 		BigDecimal totalPaidOrders = this.computeTotalPaidOrders();
 		if(totalPaidOrders.compareTo(new BigDecimal(100000.00)) == -1)
 			return this.custCreditLimit = new BigDecimal(10000.00);
 		else if((totalPaidOrders.compareTo(new BigDecimal(100000.00)) == 1) && 
 					(totalPaidOrders.compareTo(new BigDecimal(500000.00)) == -1))
 			return this.custCreditLimit = new BigDecimal(30000.00);
 		else if((totalPaidOrders.compareTo(new BigDecimal(500000.00)) == 1) && 
 				(totalPaidOrders.compareTo(new BigDecimal(1000000.00)) == -1))
 			return this.custCreditLimit = new BigDecimal(75000.00);
 		else if((totalPaidOrders.compareTo(new BigDecimal(1000000.00)) == 1))
 			return this.custCreditLimit = new BigDecimal(150000.00);
 		else return null;
 	}
 	
 	/**
 	 * @throws CreditLimitExceededException
 	 * Checks the credit limit of a customer
 	 */
 	
 	public final BigDecimal checkCreditLimit() throws Exception{
 		BigDecimal totalUnpaidOrders = computeTotalUnpaidOrders();
 		BigDecimal custCreditLimit = computeCreditLimit();
 		BigDecimal remainingCreditLimit = computeCreditLimit().subtract(computeTotalUnpaidOrders());
 		if(totalUnpaidOrders.compareTo(custCreditLimit) == 1)
 			throw new CreditLimitExceededException("Credit limit exceeded, please change the order to be below the credit limit.");
 		else return remainingCreditLimit;
 	}
 	
 	/**
 	 * Checks if a customer is entitled a discount 
 	 *
 	 */
 	public boolean checkDiscount() {
 		int flag = this.computeTotalPaidOrders().compareTo(DISCOUNT_REQUIREMENT);
 		if (flag == 1) { return true; } else return false;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((custCompanyName == null) ? 0 : custCompanyName.hashCode());
 		result = prime * result
 				+ ((custCreditLimit == null) ? 0 : custCreditLimit.hashCode());
 		result = prime * result + custID;
 		result = prime * result
 				+ ((custOrder == null) ? 0 : custOrder.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Customer other = (Customer) obj;
 		if (custCompanyName == null) {
 			if (other.custCompanyName != null)
 				return false;
 		} else if (!custCompanyName.equals(other.custCompanyName))
 			return false;
 		if (custCreditLimit == null) {
 			if (other.custCreditLimit != null)
 				return false;
 		} else if (!custCreditLimit.equals(other.custCreditLimit))
 			return false;
 		if (custID != other.custID)
 			return false;
 		if (custOrder == null) {
 			if (other.custOrder != null)
 				return false;
 		} else if (!custOrder.equals(other.custOrder))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "Customer [custCompanyName=" + custCompanyName
 				+ ", custCreditLimit=" + custCreditLimit + ", custID=" + custID
 				+ ", custOrder=" + custOrder + "]";
 	}
 	
 }
