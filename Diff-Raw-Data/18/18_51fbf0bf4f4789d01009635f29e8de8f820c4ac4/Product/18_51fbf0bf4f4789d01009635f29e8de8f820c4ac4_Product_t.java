 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements. See the NOTICE file distributed with this
  * work for additional information regarding copyright ownership. The ASF
  * licenses this file to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package jp.dip.komusubi.lunch.model;
 
 import java.io.Serializable;
 import java.util.Date;
 
 public class Product implements Serializable {
 
 	private static final long serialVersionUID = -2538213207179936427L;
 	public static final String DEFAULT_ID = "dummy";
	public static final String NOT_FOUND_ITEM = "not.found.item";
 	private String id;
 	private String refId;
 	private String name;
 	private int amount;
 //	private String shopId;
 	private Shop shop;
 	private Date start;
 	private Date finish;
 	
 	public Product() {
 		this(DEFAULT_ID);
 	}
 	
 	public Product(String id) {
 		this.id = id;
 	}
 
 	public int getAmount() {
 		return amount;
 	}
 
 	public Date getFinish() {
 		return finish;
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public String getRefId() {
 		return refId;
 	}
 
 	@Deprecated
 	public String getShopId() {
 //		return shopId;
 		return shop.getId();
 	}
 	
 	public Shop getShop() {
 		return shop;
 	}
 
 	public Date getStart() {
 		return start;
 	}
 
 	public Product setAmount(int amount) {
 		this.amount = amount;
 		return this;
 	}
 
 	public Product setFinish(Date finish) {
 		this.finish = finish;
 		return this;
 	}
 
 	public Product setName(String name) {
 		this.name = name;
 		return this;
 	}
 
 	public Product setRefId(String refId) {
 		this.refId = refId;
 		return this;
 	}
 
 //	public Product setShopId(String shopId) {
 //		this.shopId = shopId;
 //		return this;
 //	}
 	
 	public Product setShop(Shop shop) {
 		this.shop = shop;
 		return this;
 	}
 
 	public Product setStart(Date start) {
 		this.start = start;
 		return this;
 	}
 
 	public boolean salable(Date current) {
 		boolean result = false;
 		if (current == null)
 			return result;
 		if (getStart() != null && getFinish() != null) {
 			if (getStart().after(current) && getFinish().before(current))
 				result = true;
 		} else if (getStart() == null && getFinish() != null) {
 			if (getFinish().before(current))
 				result = true;
 		} else if (getStart() != null && getFinish() == null) {
 			if (getStart().after(current))
 				result = true;
 		} else if (getStart() == null && getFinish() == null) {
 			result = true;
 		}
 		return result;
 	}
 	
 	@Override
 	public String toString() {
 		StringBuilder builder = new StringBuilder();
 		builder.append("Product [id=").append(id).append(", refId=").append(refId)
 				.append(", name=").append(name).append(", amount=").append(amount)
 				.append(", shop=").append(shop).append(", start=").append(start)
 				.append(", finish=").append(finish).append("]");
 		return builder.toString();
 	}
 }
