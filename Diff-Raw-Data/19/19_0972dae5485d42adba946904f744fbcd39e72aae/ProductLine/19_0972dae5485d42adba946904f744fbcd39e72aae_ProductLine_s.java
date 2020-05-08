 package com.omartech.tdg.model;
 
 public class ProductLine {
 	
 	private int id;
 	private String name; //中文名
 	private String english;//英文名
 	private int parentId;//父id
 	private int level;//层级
	private float discount;//平台折扣
	private float commission;//平台佣金费用
 	
 	public int getId() {
 		return id;
 	}
 	public void setId(int id) {
 		this.id = id;
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public String getEnglish() {
 		return english;
 	}
 	public void setEnglish(String english) {
 		this.english = english;
 	}
 	public int getParentId() {
 		return parentId;
 	}
 	public void setParentId(int parentId) {
 		this.parentId = parentId;
 	}
 	public int getLevel() {
 		return level;
 	}
 	public void setLevel(int level) {
 		this.level = level;
 	}
 	public float getDiscount() {
 		return discount;
 	}
 	public void setDiscount(float discount) {
 		this.discount = discount;
 	}
 	public float getCommission() {
 		return commission;
 	}
 	public void setCommission(float commission) {
 		this.commission = commission;
 	}
 }
