 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package com.softlysoftware.jxero.core;
 
 import java.math.BigDecimal;
 import java.text.NumberFormat;
 import java.text.DecimalFormat;
 import java.text.ParseException;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 
 @XmlRootElement(name = "LineItem")
 @XmlAccessorType(XmlAccessType.NONE)
 public class LineItem {
 
 	private static NumberFormat MONEY_FORMAT = new DecimalFormat("0.00");
 
 	private static NumberFormat QUANTITY_FORMAT = new DecimalFormat("0.0000");
 
 	@XmlElement(name = "Description")
 	private String description;
 	public String getDescription(){return description;}
 	public void setDescription(String description){this.description = description;}
 
 	@XmlElement(name = "Quantity")
 	private String quantity;
 	public double getQuantity(){
 		try {return QUANTITY_FORMAT.parse(quantity).doubleValue();}
 		catch (ParseException e) {throw new RuntimeException(e);}
 	}
 	public void setQuantity(double quantity){this.quantity = QUANTITY_FORMAT.format(quantity);}
	public void setQuantity(BigDecimal quantity){this.quantity = QUANTITY_FORMAT.format(quantity);}
 	
 	@XmlElement(name = "UnitAmount")
 	private String unitAmount;
 	public double getUnitAmount(){
 		try {return MONEY_FORMAT.parse(unitAmount).doubleValue();}
 		catch (ParseException e) {throw new RuntimeException(e);}
 	}
 	public void setUnitAmount(double unitAmount){this.unitAmount = MONEY_FORMAT.format(unitAmount);}
 
 	@XmlElement(name = "TaxType")
 	private String taxType;
 	public String getTaxType(){return taxType;}
 	public void setTaxType(String taxType){this.taxType = taxType;}
 
 	@XmlElement(name = "TaxAmount")
 	private String taxAmount;
 	public double getTaxAmount(){
 		try {return MONEY_FORMAT.parse(taxAmount).doubleValue();}
 		catch (ParseException e) {throw new RuntimeException(e);}
 	}
 	public void setTaxAmount(double taxAmount){this.taxAmount = MONEY_FORMAT.format(taxAmount);}
 
 	@XmlElement(name = "LineAmount")
 	private String lineAmount;
 	public double getLineAmount(){
 		try {return MONEY_FORMAT.parse(lineAmount).doubleValue();}
 		catch (ParseException e) {throw new RuntimeException(e);}
 	}
 	public void setLineAmount(double lineAmount){this.lineAmount = MONEY_FORMAT.format(lineAmount);}
 
 	@XmlElement(name = "AccountCode")
 	private String accountCode;
 	public String getAccountCode(){return accountCode;}
 	public void setAccountCode(String accountCode){this.accountCode = accountCode;}
 
 	@XmlElement(name = "ItemCode")
 	private String itemCode;
 	public String getItemCode(){return itemCode;}
 	public void setItemCode(String itemCode){this.itemCode = itemCode;}
 
 }
