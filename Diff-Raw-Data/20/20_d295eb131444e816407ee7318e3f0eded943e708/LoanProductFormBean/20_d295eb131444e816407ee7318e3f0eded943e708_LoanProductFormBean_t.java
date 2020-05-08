 package org.mifos.ui.loan.command;
 
 import org.mifos.loan.domain.LoanProductStatus;
 import org.mifos.loan.service.LoanProductDto;
 
 public class LoanProductFormBean extends LoanProductDto {
 
 	protected void setDefaults(){
 		setStatus(LoanProductStatus.ACTIVE);
 	}
 		
 }
