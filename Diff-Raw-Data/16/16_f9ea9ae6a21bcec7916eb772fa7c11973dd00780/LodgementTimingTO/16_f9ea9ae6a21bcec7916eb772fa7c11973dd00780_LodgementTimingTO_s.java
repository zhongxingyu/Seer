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
 package org.sola.services.boundary.transferobjects.casemanagement;
 
import java.math.BigDecimal;
import java.math.BigInteger;
 import org.sola.services.common.contracts.AbstractBasicIdTO;
 
 
 
 
 /**
  *
  * @author RizzoM
  */
 public class LodgementTimingTO  extends AbstractBasicIdTO {
 
     private String  resultCode;
     private int resultTotal;
    private String  resultDailyAvg;
 
    public String getResultDailyAvg() {
         return resultDailyAvg;
     }
 
    public void setResultDailyAvg(String resultDailyAvg) {
         this.resultDailyAvg = resultDailyAvg;
     }
    
 
     public LodgementTimingTO() {
         super();
     }
 
  
   
     public int getResultTotal() {
         return resultTotal;
     }
 
     public void setResultTotal(int resultTotal) {
         this.resultTotal = resultTotal;
     }
 
     public String getResultCode() {
         return resultCode;
     }
 
     public void setResultCode(String resultCode) {
         this.resultCode = resultCode;
     }
 
  
 }
