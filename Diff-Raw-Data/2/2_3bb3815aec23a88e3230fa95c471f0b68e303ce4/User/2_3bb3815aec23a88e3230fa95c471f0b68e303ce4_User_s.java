 package org.bloodtorrent.dto;
 
 import lombok.Getter;
 import lombok.Setter;
 import org.hibernate.validator.constraints.NotBlank;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Table;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Pattern;
 import javax.validation.constraints.Size;
 import java.util.Date;
 
 
 @Entity
 @Table(name = "USER")
 @Getter @Setter
 @SuppressWarnings("PMD.UnusedPrivateField")
 public class User {
     private static final String PLEASE_FILL_OUT_ALL_THE_MANDATORY_FIELDS = "Please fill out all the mandatory fields";
     private static final String PLEASE_CHECK = "Please check";
 
     @Id
     @Pattern(regexp = "^([0-9a-zA-Z_-]|(([0-9a-zA-Z_-]+[\\\\.]?)+[0-9a-zA-Z_-]))@([0-9a-zA-Z_-]+)(\\.[0-9a-zA-Z_-]+){1,2}$", message= PLEASE_CHECK + " email address")
     @Size(min = 5, max = 100, message= PLEASE_CHECK + " email address")
     @NotBlank(message=PLEASE_FILL_OUT_ALL_THE_MANDATORY_FIELDS+"(email)")
     //TODO: why are we validating a field named "id" as an email address? --Kris
     private String id;
 
     @Size(min = 8, max = 25, message= PLEASE_CHECK + " password")
     @Pattern(regexp ="\\D*\\d+\\D*", message= PLEASE_CHECK + " password")
     @NotBlank(message=PLEASE_FILL_OUT_ALL_THE_MANDATORY_FIELDS+"(password)")
     private String password;
 
     private String role;
 
     @Column(name = "first_name")
     @NotBlank(message=PLEASE_FILL_OUT_ALL_THE_MANDATORY_FIELDS+"(first name)")
     @Size(min = 1, max = 35, message= PLEASE_CHECK + " first name")
     private String firstName;
 
     @Column(name = "last_name")
     @NotBlank(message=PLEASE_FILL_OUT_ALL_THE_MANDATORY_FIELDS+"(last name)")
     @Size(min = 1, max = 35, message= PLEASE_CHECK + " last name")
     private String lastName;
 
     @NotNull
     @Size(min = 10, max = 10, message= PLEASE_CHECK + " phone number")
     @Pattern(regexp = "\\d*", message= PLEASE_CHECK + " phone number")
     @Column(name = "cell_phone")
     private String cellPhone;
 
     private String gender;
 
     @Column(name = "blood_group")
     @NotBlank(message=PLEASE_FILL_OUT_ALL_THE_MANDATORY_FIELDS+"(blood group)")
     @Pattern(regexp = "^(A\\+)|(A-)|(B\\+)|(B-)|(AB\\+)|(AB-)|(O\\+)|(O-)|(Unknown)$", message= PLEASE_CHECK + " blood group")
     private String bloodGroup;
 
     private boolean anonymous;
 
     @NotBlank(message=PLEASE_FILL_OUT_ALL_THE_MANDATORY_FIELDS+"(city)")
     @Size(min = 1, max = 255, message= PLEASE_CHECK + " city size(1-255)")
     private String city;
 
     @NotBlank(message=PLEASE_FILL_OUT_ALL_THE_MANDATORY_FIELDS+"(state)")
     @Size(min = 1, max = 255, message= PLEASE_CHECK + " state size(1-255)")
     @Pattern(regexp = "^(Andhra Pradesh)|(Arunachal Pradesh)|(Asom \\(Assam\\))|(Bihar)|(Karnataka)|(Kerala)|(Chhattisgarh)|(Goa)|(Gujarat)|(Haryana)|(Himachal Pradesh)|(Jammu And Kashmir)|(Jharkhand)|(West Bengal)|(Madhya Pradesh)|(Maharashtra)|(Manipur)|(Meghalaya)|(Mizoram)|(Nagaland)|(Orissa)|(Punjab)|(Rajasthan)|(Sikkim)|(Tamilnadu)|(Tripura)|(Uttarakhand \\(Uttaranchal\\))|(Uttar Pradesh)$")
     private String state;
 
     @NotBlank(message= PLEASE_FILL_OUT_ALL_THE_MANDATORY_FIELDS + "(address)")
     @Size(min = 1, max = 1000 , message= PLEASE_CHECK + " address size(1-1000)")
     private String address;
 
     @NotBlank(message= PLEASE_FILL_OUT_ALL_THE_MANDATORY_FIELDS + "(distance)")
     @Pattern(regexp = "^5|10|20|50$" , message= PLEASE_CHECK + " distance")
     private String distance;
 
     @Column(name = "birth_day")
     @Pattern(regexp ="^((0[1-9]|[1-2][0-9]|3[0-1])\\-(0[0-9]|1[0-2])\\-(19[0-9][0-9]|20\\d{2}))*$", message= PLEASE_CHECK + " date of birth")
     private String birthDay;
 
     @Column(name = "last_donate_date")
     private Date lastDonateDate;
 
     @Column(name = "latitude")
     private double latitude;
 
     @Column(name = "longitude")
     private double longitude;
 
     @Column(name = "is_admin")
    private char isAdmin;
 }
