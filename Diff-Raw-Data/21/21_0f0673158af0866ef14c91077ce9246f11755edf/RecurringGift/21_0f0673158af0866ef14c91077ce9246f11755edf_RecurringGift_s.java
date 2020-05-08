 /*
  * Copyright (c) 2009. Orange Leap Inc. Active Constituent
  * Relationship Management Platform.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.orangeleap.tangerine.domain.paymentInfo;
 
 import com.orangeleap.tangerine.domain.Schedulable;
 import org.springframework.util.StringUtils;
 
 import javax.xml.bind.annotation.XmlType;
 import java.math.BigDecimal;
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 @XmlType (namespace="http://www.orangeleap.com/orangeleap/schemas")
 public class RecurringGift extends Commitment implements Schedulable {
 
 	private static final long serialVersionUID = 1L;
 
 	private Date nextRunDate;
 	private String recurringGiftStatus = STATUS_PENDING;
 	private boolean autoPay = false;
 	private boolean activate = false;
 
 	public RecurringGift() { }
 
 	public RecurringGift(Date nextRunDate) {
 		this.nextRunDate = nextRunDate;
 	}
 
	public RecurringGift(Long id, String recurringGiftStatus, Date startDate, Date endDate) {
 		this.id = id;
 		this.recurringGiftStatus = recurringGiftStatus;
 		this.startDate = startDate;
 		this.endDate = endDate;
 	}
 
 	public Date getNextRunDate() {
 		return nextRunDate;
 	}
 
 	public void setNextRunDate(Date nextRunDate) {
 		this.nextRunDate = nextRunDate;
 	}
 
 	public String getRecurringGiftStatus() {
 		return recurringGiftStatus;
 	}
 
 	public void setRecurringGiftStatus(String recurringGiftStatus) {
 		this.recurringGiftStatus = recurringGiftStatus;
 	}
 
 	public boolean isAutoPay() {
 		return autoPay;
 	}
 
 	public void setAutoPay(boolean autoPay) {
 		this.autoPay = autoPay;
 	}
 
 	public boolean isActivate() {
 		return activate;
 	}
 
 	public void setActivate(boolean activate) {
 		this.activate = activate;
 	}
 
 	public String getShortDescription() {
 		StringBuilder sb = new StringBuilder();
 		sb.append(getAmountPerGift()).append(", ");
         if (StringUtils.hasText(getCustomFieldValue("recurringGiftName"))) {
             sb.append(getCustomFieldValue("recurringGiftName")).append(", ");
         }
 		sb.append(getFrequency()).append(", ");
 
 		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
 		if (startDate != null) {
 			sb.append(df.format(startDate));
 		}
 		if (endDate != null) {
 			sb.append(" - ").append(df.format(endDate));
 		}
 		return sb.toString();
 	}
 	
     @Override
     public BigDecimal getSchedulingAmount() {
     	return this.amountPerGift;
     }
     
     @Override
     public void setSchedulingAmount(BigDecimal schedulingAmount) {
     	throw new RuntimeException("Value not settable.");
     }
 
 	@Override
 	public void prePersist() {
 		super.prePersist();
 
 		if (getAmountPaid() == null) {
 			setAmountPaid(BigDecimal.ZERO);
 		}
 		if (getEndDate() != null) {
 			if (getAmountTotal() != null && getAmountPaid() != null) {
 				setAmountRemaining(getAmountTotal().subtract(getAmountPaid()));
 			}
 			else {
 				setAmountRemaining(getAmountTotal());
 			}
 		}
 		else {
 			setAmountRemaining(null);
 			setAmountTotal(null);
 		}
 	}
 
 	@Override
 	public String toString() {
 		return super.toString(); // TODO
 	}
 }
