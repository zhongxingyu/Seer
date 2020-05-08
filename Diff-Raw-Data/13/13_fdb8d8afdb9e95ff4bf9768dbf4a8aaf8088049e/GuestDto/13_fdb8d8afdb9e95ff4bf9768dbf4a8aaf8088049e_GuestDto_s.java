 package net.mklew.hotelms.inhouse.web.dto;
 
 import com.fasterxml.jackson.annotation.JsonIgnore;
 import net.mklew.hotelms.domain.guests.DocumentType;
 import net.mklew.hotelms.domain.guests.Gender;
 import net.mklew.hotelms.domain.guests.Guest;
 import net.mklew.hotelms.inhouse.web.dto.dates.DateParser;
 import org.joda.time.DateTime;
 
 import javax.ws.rs.core.MultivaluedMap;
 import java.util.ArrayList;
 import java.util.Collection;
 
 /**
  * @author Marek Lewandowski <marek.m.lewandowski@gmail.com>
  * @since 11/30/12
  *        time 8:14 PM
  */
 public class GuestDto
 {
     public String id;
 
     public String firstName;
 
     public String middleName;
 
     public String surname;
 
     public String displayed;
 
     public String socialTitle;
 
     public String sex;
 
     public String phoneNumber;
 
     public String nationality;
 
     public String idNumber;
 
     public String dateOfBirth;
 
     public String preferences;
 
     private String vip;
 
     public String documentType; // TODO need to change naming idType and documentType are meant to be the same,
     // it should be something like 'documentType' and 'documentTypeEnum' or something
 
 
     @JsonIgnore
     public transient DocumentType idType;
     @JsonIgnore
     public transient DateTime dateOfBirthDate;
     @JsonIgnore
     public transient Gender gender;
 
 
     public String getVip()
     {
         return vip;
     }
 
     public void setVip(String vip)
     {
         this.vip = vip;
     }
 
     public GuestDto()
     {
     }
 
     public static Collection<GuestDto> fromGuests(Collection<Guest> guests)
     {
         Collection<GuestDto> dtos = new ArrayList<>(guests.size());
         for (Guest guest : guests)
         {
             dtos.add(fromGuest(guest));
         }
         return dtos;
     }
 
     public boolean exists()
     {
        return !"".equals(id);
     }
 
     private boolean blank(String str)
     {
         return str == null || str.trim().equals("");
     }
 
     public void init() throws MissingGuestInformation
     {
         validate();
         if (!blank(dateOfBirth))
         {
             dateOfBirthDate = DateParser.fromString(dateOfBirth);
         }
     }
 
     public void validate() throws MissingGuestInformation
     {
 
         if (blank(socialTitle))
         {
             throw new MissingGuestInformation("Missing socialTitle");
         }
         if (blank(firstName))
         {
             throw new MissingGuestInformation("Missing first name");
         }
         if (blank(surname))
         {
             throw new MissingGuestInformation("Missing surname");
         }
         if (blank(sex))
         {
             throw new MissingGuestInformation("Missing sex");
         }
         if (blank(phoneNumber))
         {
             throw new MissingGuestInformation("Missing phone number");
         }
         if (blank(nationality))
         {
             throw new MissingGuestInformation("Missing nationality");
         }
         if (idType == null)
         {
             throw new MissingGuestInformation("Missing id type");
         }
         if (blank(idNumber))
         {
             throw new MissingGuestInformation("Missing id number");
         }
     }
 
     public static GuestDto fromReservationForm(MultivaluedMap<String, String> formData) throws MissingGuestInformation
     {
         validateRequiredInformation(formData);
         GuestDto dto = new GuestDto();
         // populate required properties
         dto.id = formData.getFirst("ownerId") != null ? formData.getFirst("ownerId") : "";
         dto.socialTitle = formData.getFirst("socialTitle");
         dto.firstName = formData.getFirst("firstName");
         //dto.middleName;
         dto.surname = formData.getFirst("surname");
         dto.sex = formData.getFirst("sex");
         dto.gender = Gender.fromName(formData.getFirst("sex"));
         dto.phoneNumber = formData.getFirst("phoneNumber");
         dto.nationality = formData.getFirst("nationality");
         dto.idType = DocumentType.fromString(formData.getFirst("idType"));
         dto.idNumber = formData.getFirst("idNumber");
 
         // populate optional properties
         String dateOfBirthString = formData.getFirst("dateOfBirth");
         if (dateOfBirthString != null)
         {
             dto.dateOfBirth = dateOfBirthString;
             dto.dateOfBirthDate = DateParser.fromString(dateOfBirthString);
         }
         else
         {
             dto.dateOfBirth = "";
         }
         dto.preferences = formData.getFirst("preferences") != null ? formData.getFirst("preferences") : "";
 
         // TODO create home address from data provided
         // TODO AddressDTO
 
         return dto;
     }
 
 
     private static void validateRequiredInformation(MultivaluedMap<String,
             String> formData) throws MissingGuestInformation
     {
         if (!(formData.getFirst("socialTitle") != null))
         {
             throw new MissingGuestInformation("Missing socialTitle");
         }
         if (!(formData.getFirst("firstName") != null))
         {
             throw new MissingGuestInformation("Missing first name");
         }
         if (!(formData.getFirst("surname") != null))
         {
             throw new MissingGuestInformation("Missing surname");
         }
         if (!(formData.getFirst("sex") != null))
         {
             throw new MissingGuestInformation("Missing sex");
         }
         if (!(formData.getFirst("phoneNumber") != null))
         {
             throw new MissingGuestInformation("Missing phone number");
         }
         if (!(formData.getFirst("nationality") != null))
         {
             throw new MissingGuestInformation("Missing nationality");
         }
         if (!(formData.getFirst("idType") != null))
         {
             throw new MissingGuestInformation("Missing id type");
         }
         if (!(formData.getFirst("idNumber") != null))
         {
             throw new MissingGuestInformation("Missing id number");
         }
     }
 
     public void validateRequired() throws MissingGuestInformation
     {
         notEmpty(socialTitle);
         notEmpty(firstName);
         notEmpty(surname);
         notEmpty(sex);
         notEmpty(phoneNumber);
         notEmpty(nationality);
         notEmpty(documentType);
         notEmpty(idNumber);
     }
 
     public static void notEmpty(String property) throws MissingGuestInformation
     {
         if (property == null || "".equals(property))
         {
             throw new MissingGuestInformation("Missing property");
         }
     }
 
     public static GuestDto fromGuest(Guest guest)
     {
         GuestDto dto = new GuestDto();
         dto.firstName = guest.getFirstName();
         dto.middleName = guest.getMiddleName() != null ? guest.getMiddleName() : "";
         dto.surname = guest.getSurname();
         dto.displayed = guest.getFirstName() + " " + guest.getSurname();
         dto.id = String.valueOf(guest.getId());
         dto.socialTitle = guest.getSocialTitle();
         dto.sex = guest.getGender().getName();
         dto.phoneNumber = guest.getPhoneNumber();
         dto.nationality = guest.getNationality();
         dto.documentType = guest.getDocumentType().getName();
         dto.idType = guest.getDocumentType(); // this is not needed, just wanna check how it looks in json
         dto.gender = guest.getGender(); // same as above
         dto.idNumber = guest.getDocumentId();
         dto.dateOfBirth = guest.getDateOfBirth() != null ? DateParser.fromDate(guest.getDateOfBirth()) : "";
         dto.preferences = guest.getPreferences();
         return dto;
     }
 
     public static GuestDto fromNewGuestForm(MultivaluedMap<String, String> formParams) throws MissingGuestInformation
     {
         // TODO sort these method names out, because probably both will have same data
         return fromReservationForm(formParams);
     }
 
     public GuestDto initIgnored()
     {
         idType = DocumentType.fromString(documentType);
         if (dateOfBirth != null && !dateOfBirth.equals(""))
         {
             dateOfBirthDate = DateParser.fromString(dateOfBirth);
         }
         gender = Gender.fromName(sex);
         return this;
     }
 
     public String getId()
     {
         return id;
     }
 
     public void setId(String id)
     {
         this.id = id;
     }
 
     public String getFirstName()
     {
         return firstName;
     }
 
     public void setFirstName(String firstName)
     {
         this.firstName = firstName;
     }
 
     public String getMiddleName()
     {
         return middleName;
     }
 
     public void setMiddleName(String middleName)
     {
         this.middleName = middleName;
     }
 
     public String getSurname()
     {
         return surname;
     }
 
     public void setSurname(String surname)
     {
         this.surname = surname;
     }
 
     public String getDisplayed()
     {
         return displayed;
     }
 
     public void setDisplayed(String displayed)
     {
         this.displayed = displayed;
     }
 
     public String getSocialTitle()
     {
         return socialTitle;
     }
 
     public void setSocialTitle(String socialTitle)
     {
         this.socialTitle = socialTitle;
     }
 
     public String getSex()
     {
         return sex;
     }
 
     public void setSex(String sex)
     {
         this.sex = sex;
     }
 
     public String getPhoneNumber()
     {
         return phoneNumber;
     }
 
     public void setPhoneNumber(String phoneNumber)
     {
         this.phoneNumber = phoneNumber;
     }
 
     public String getNationality()
     {
         return nationality;
     }
 
     public void setNationality(String nationality)
     {
         this.nationality = nationality;
     }
 
     public String getIdNumber()
     {
         return idNumber;
     }
 
     public void setIdNumber(String idNumber)
     {
         this.idNumber = idNumber;
     }
 
     public String getDateOfBirth()
     {
         return dateOfBirth;
     }
 
     public void setDateOfBirth(String dateOfBirth)
     {
         this.dateOfBirth = dateOfBirth;
     }
 
     public String getPreferences()
     {
         return preferences;
     }
 
     public void setPreferences(String preferences)
     {
         this.preferences = preferences;
     }
 
     public String getDocumentType()
     {
         return documentType;
     }
 
     public void setDocumentType(String documentType)
     {
         this.documentType = documentType;
     }
 
     public DocumentType getIdType()
     {
         return idType;
     }
 
     public void setIdType(DocumentType idType)
     {
         this.idType = idType;
     }
 
     public DateTime getDateOfBirthDate()
     {
         return dateOfBirthDate;
     }
 
     public void setDateOfBirthDate(DateTime dateOfBirthDate)
     {
         this.dateOfBirthDate = dateOfBirthDate;
     }
 
     public Gender getGender()
     {
         return gender;
     }
 
     public void setGender(Gender gender)
     {
         this.gender = gender;
     }
 }
