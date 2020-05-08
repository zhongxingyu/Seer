 /*
  *  This file is part of OpenGov.
  *
  *  OpenGov is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  OpenGov is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with OpenGov.  If not, see <http://www.gnu.org/licenses/>.
  */
 package za.org.opengov.stockout.web.domain;
 
 import java.io.Serializable;
 import java.util.Date;
 
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Past;
 import javax.validation.constraints.Size;
 
 import org.hibernate.validator.constraints.Email;
 import org.hibernate.validator.constraints.NotEmpty;
 import org.springframework.format.annotation.DateTimeFormat;
 import org.springframework.format.annotation.NumberFormat;
 import org.springframework.format.annotation.NumberFormat.Style;
 
 /** Public Stockout Report used to submit form data when reporting a stock-out.
  *Contains validation annotations to ensure that fields meet the neccessary requirements
  * before being submitted.  **/
 public class PublicStockoutReport implements Serializable {
 
 	@Size(min=2, max=30)
 	private String Name;
 	
 	@Size(max=30)
 	private String Designation;
 	
	@DateTimeFormat(pattern="dd/mm/yyyy")
     @NotNull @Past
 	private Date dateOfOccurence;
 	
 	@NotNull
 	@NotEmpty
 	private String facilityName;
 
 	@Size(min=10,max=12)
 	private String cellNumber;
 	
 	@NotEmpty @Email
 	private String emailAddress;
 	
 	private String reasonForOccurrence;
 	
 	@NotNull
 	@NotEmpty(message="You must select a medicine to report")
 	private String selectedMedicines;
 
 	public String getName() {
 		return Name;
 	}
 
 	public String getDesignation() {
 		return Designation;
 	}
 
 	public Date getDateOfOccurence() {
 		return dateOfOccurence;
 	}
 
 	public String getFacilityName() {
 		return facilityName;
 	}
 
 	public String getCellNumber() {
 		return cellNumber;
 	}
 
 	public String getEmailAddress() {
 		return emailAddress;
 	}
 
 	public String getReasonForOccurrence() {
 		return reasonForOccurrence;
 	}
 
 	public String getSelectedMedicines() {
 		return selectedMedicines;
 	}
 
 	public void setName(String name) {
 		Name = name;
 	}
 
 	public void setDesignation(String designation) {
 		Designation = designation;
 	}
 
 	public void setDateOfOccurence(Date dateOfOccurence) {
 		this.dateOfOccurence = dateOfOccurence;
 	}
 
 	public void setFacilityName(String facilityName) {
 		this.facilityName = facilityName;
 	}
 
 	public void setCellNumber(String cellNumber) {
 		this.cellNumber = cellNumber;
 	}
 
 	public void setEmailAddress(String emailAddress) {
 		this.emailAddress = emailAddress;
 	}
 
 	public void setReasonForOccurrence(String reasonForOccurrence) {
 		this.reasonForOccurrence = reasonForOccurrence;
 	}
 
 	public void setSelectedMedicines(String selectedMedicines) {
 		this.selectedMedicines = selectedMedicines;
 	}
 
 }
