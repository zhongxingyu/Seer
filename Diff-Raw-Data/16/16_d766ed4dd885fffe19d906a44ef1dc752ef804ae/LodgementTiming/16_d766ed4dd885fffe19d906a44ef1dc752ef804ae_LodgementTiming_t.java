 /*
  *  Food and Agriculture Orgainsation (FAO) of the United Nations
  *  Solutions for Open Land Administration - Sola.
  * 
  * 
  *  Copyright 2011, FAO and UN, and individual contributors as indicated
  *  by the @authors. See the copyright text in the distribution for a
  *  full listing of individual contributors.
  * 
  *  This is free software; you can redistribute it and/or modify it
  *  under the terms of the GNU Lesser General Public License as
  *  published by the Free Software Foundation; either version 2.1 of
  *  the License, or (at your option) any later version.
  * 
  *  This software is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  *  Lesser General Public License for more details.
  * 
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this software; if not, write to the Free
  *  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  *  02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.sola.services.ejb.application.repository.entities;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import javax.persistence.Column;
 import org.sola.services.common.repository.entities.AbstractReadOnlyEntity;
 
 /**
  *
  * @author RizzoM
  */
 public class LodgementTiming extends AbstractReadOnlyEntity {
 
     public static final String PARAMETER_FROM = "fromDate";
     public static final String PARAMETER_TO = "toDate";
     public static final String QUERY_GETLODGEMENT = "select * from application.getlodgetiming(#{" + 
             PARAMETER_FROM + "}, #{" + PARAMETER_TO + "}) "
            + "  as TimingReport(resultCode varchar, resultTotal integer, resultDailyAvg float, ord integer) ";
 
     @Column(name="resultCode")
     private String  resultCode;
     @Column(name="resultTotal")
     private Integer resultTotal;
     @Column(name="resultDailyAvg")
    private double resultDailyAvg;
     
     public LodgementTiming() {
         super();
     }
 
    public double getResultDailyAvg() {
         return resultDailyAvg;
     }
 
    public void setResultDailyAvg(double resultDailyAvg) {
         this.resultDailyAvg = resultDailyAvg;
     }
 
     public Integer getResultTotal() {
         return resultTotal;
     }
 
     public void setResultTotal(Integer resultTotal) {
         this.resultTotal = resultTotal;
     }
 
     
     public String getResultCode() {
         return resultCode;
     }
 
     public void setResultCode(String resultCode) {
         this.resultCode = resultCode;
     }
     
 }
