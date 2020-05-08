 /*
  * JBILLING CONFIDENTIAL
  * _____________________
  *
  * [2003] - [2012] Enterprise jBilling Software Ltd.
  * All Rights Reserved.
  *
  * NOTICE:  All information contained herein is, and remains
  * the property of Enterprise jBilling Software.
  * The intellectual and technical concepts contained
  * herein are proprietary to Enterprise jBilling Software
  * and are protected by trade secret or copyright law.
  * Dissemination of this information or reproduction of this material
  * is strictly forbidden.
  */
 
 package com.sapienter.jbilling.server.pricing.db;
 
 import java.io.Serializable;
 
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Pattern;
 import javax.validation.constraints.Size;
 
 import org.apache.commons.lang.StringUtils;
 
import com.sapienter.jbilling.server.util.api.validation.CreateValidationGroup;
 import com.sapienter.jbilling.server.util.api.validation.UpdateValidationGroup;
 import com.sapienter.jbilling.server.util.sql.JDBCUtils;
 
 /**
  * @author Panche Isajeski
  * @since 04-04-2012
  */
 public class RateCardWS implements Serializable {
 	
 	public static final String TABLE_PREFIX = "rate_";
 	
 	private Integer id;
     @NotNull(message="validation.error.notnull")
     @Size(min = 1, max = 50, message = "validation.error.size,1,50")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "validation.error.field.format", groups={CreateValidationGroup.class})
 	private String name;
     @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "validation.error.field.format", groups={UpdateValidationGroup.class})
 	private String tableName;
 	
 	public RateCardWS() {
 		super();
 	}
 
 	public RateCardWS(String name, String tableName) {
 		super();
 		this.name = name;
 		this.tableName = tableName;
 	}
 	
 	public RateCardWS(RateCardDTO rateCard) {
 		
 		this.id = rateCard.getId();
 		this.name = rateCard.getName();
 		this.tableName = rateCard.getTableName();
 	}
 	
 	public RateCardDTO toRateCardDTO() {
 		
 		RateCardDTO rateCard = new RateCardDTO();
 		rateCard.setId(id);
 		rateCard.setName(name);
 		rateCard.setTableName(tableName);
 		return rateCard;
 	}
 	
 	public Integer getId() {
 		return id;
 	}
 
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getTableName() {
 		
 		RateCardDTO rateCard = toRateCardDTO();
 		if (rateCard != null) {
 			return rateCard.getTableName();
 		}
 		return tableName;
 	}
 
 	public void setTableName(String tableName) {
 		this.tableName = tableName;
 	}
 
 	@Override
 	public String toString() {
 		return "RateCardWS [id=" + id + ", name=" + name + ", tableName="
 				+ tableName + "]";
 	}
 }
