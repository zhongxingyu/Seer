 package com.magiccube.order.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * 订单优惠
  * @author jcchen
  *
  */
 public class OrderDiscount {
 	
 	/** 内部保存的关于优惠信息的状态值 */
 	private int state = 0;
 	
 	public OrderDiscount() {
 	}
 	
 	/**
 	 * 根据已有的状态值构造一个优惠信息
 	 * @param state 表示优惠信息的10进制数值.
 	 */
 	public OrderDiscount(int state) {
 		this.state = state;
 	}
 	
 	/**
 	 * 根据已有的状态值构造一个优惠信息
 	 * @param state 表示二进制状态的01字符串.
 	 */
 	public OrderDiscount(String state) {
 		this.state = Integer.valueOf(state, 2);
 	}
 	
 	/**
 	 * 添加某个优惠信息
 	 * @param t Type 优惠的类型
 	 */
 	public void addDiscount(Type t) {
 		this.state = this.state | t.getState();
 	}
 	
 	/**
 	 * 去除某个优惠信息
 	 * @param t Type 优惠的类型
 	 */
 	public void removeDiscount(Type t) {
 		this.state = this.state & (~ this.getState());
 	}
 	
 	/**
 	 * 是否满足某项优惠
 	 * @param t
 	 */
 	public boolean isDiscount(Type t) {
 		int state = t.getState();
 		return (this.state & state) == state;
 	}
 	
 	/**
 	 * 是否存在满足某项优惠
 	 * @return
 	 */
 	public boolean isDiscount() {
 		return this.state > 0;
 	}
 	
 	/**
 	 * 获取所有的优惠信息
 	 * @return
 	 */
 	public List<Type> getDiscounts() {
 		List<Type> discounts = new ArrayList<Type>();
 		for(Type type : Type.values()) {
 			if(this.isDiscount(type)) {
 				discounts.add(type);
 			}
 		}
 		return discounts;
 	}
 	
 	/**
 	 * 获取总优惠的价格
 	 * @return
 	 */
 	public double getDiscountPrice() {
 		int price = 0;
 		for(Type type : Type.values()) {
 			if(this.isDiscount(type)) {
 				price += type.getPriceDiscount();
 			}
 		}
 		return price;
 	}
 	
 	public int getState() {
 		return state;
 	}
 
 	public void setState(int state) {
 		this.state = state;
 	}
 
 	/**
 	 * 优惠类型
 	 * @author jcchen
 	 *
 	 */
 	public enum Type {
		FIRST_ORDER(1, 1, "首次订餐每份优惠1元     "), //首次订餐优惠1元. 状态位:001
		GT5_PIECE(2, 1, "5份以上每份优惠1元     "), //5份以上,每份优惠1元. 状态位:010
 		EARLIER_THEN_10(4, 0, "10点半前下单送饮品一份"); //10点前订餐,送东西，钱不少. 状态位:100
 		
 		/**
 		 * 对应二进制的状态值
 		 */
 		private int state;
 		
 		/**
 		 * 优惠价格(元)
 		 */
 		private double priceDiscount;
 		
 		/**
 		 * 优惠显示信息
 		 */
 		private String showText;
 		
 		Type(int state, double priceDiscount, String showText) {
 			this.state = state;
 			this.priceDiscount = priceDiscount;
 			this.showText = showText;
 		}
 
 		public int getState() {
 			return state;
 		}
 
 		public void setState(int state) {
 			this.state = state;
 		}
 
 		public double getPriceDiscount() {
 			return priceDiscount;
 		}
 
 		public void setPriceDiscount(double priceDiscount) {
 			this.priceDiscount = priceDiscount;
 		}
 
 		public String getShowText() {
 			return showText;
 		}
 
 		public void setShowText(String showText) {
 			this.showText = showText;
 		}
 	}
 }
