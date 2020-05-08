 package com.podio.crunchbaseapi;
 
 import org.codehaus.jackson.annotate.JsonProperty;
 import org.joda.time.LocalDate;
 
 public class BaseCompany extends BaseObject {
 
 	private String name;
 
 	private Integer numberOfEmployees;
 
 	private Integer foundedYear;
 
 	private Integer foundedMonth;
 
 	private Integer foundedDay;
 
 	private String emailAddress;
 
 	private String phoneNumber;
 
 	private String description;
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	@JsonProperty("number_of_employees")
 	public Integer getNumberOfEmployees() {
 		return numberOfEmployees;
 	}
 
 	@JsonProperty("number_of_employees")
 	public void setNumberOfEmployees(Integer numberOfEmployees) {
 		this.numberOfEmployees = numberOfEmployees;
 	}
 
 	public LocalDate getFounded() {
		if (foundedYear != null && foundedMonth != null && foundedDay != null
				&& foundedYear > 1900) {
 			return new LocalDate(foundedYear, foundedMonth, foundedDay);
 		} else {
 			return null;
 		}
 	}
 
 	@JsonProperty("founded_year")
 	public Integer getFoundedYear() {
 		return foundedYear;
 	}
 
 	@JsonProperty("founded_year")
 	public void setFoundedYear(Integer foundedYear) {
 		this.foundedYear = foundedYear;
 	}
 
 	@JsonProperty("founded_month")
 	public Integer getFoundedMonth() {
 		return foundedMonth;
 	}
 
 	@JsonProperty("founded_month")
 	public void setFoundedMonth(Integer foundedMonth) {
 		this.foundedMonth = foundedMonth;
 	}
 
 	@JsonProperty("founded_day")
 	public Integer getFoundedDay() {
 		return foundedDay;
 	}
 
 	@JsonProperty("founded_day")
 	public void setFoundedDay(Integer foundedDay) {
 		this.foundedDay = foundedDay;
 	}
 
 	@JsonProperty("email_address")
 	public String getEmailAddress() {
 		return emailAddress;
 	}
 
 	@JsonProperty("email_address")
 	public void setEmailAddress(String emailAddress) {
 		this.emailAddress = emailAddress;
 	}
 
 	@JsonProperty("phone_number")
 	public String getPhoneNumber() {
 		return phoneNumber;
 	}
 
 	@JsonProperty("phone_number")
 	public void setPhoneNumber(String phoneNumber) {
 		this.phoneNumber = phoneNumber;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 }
