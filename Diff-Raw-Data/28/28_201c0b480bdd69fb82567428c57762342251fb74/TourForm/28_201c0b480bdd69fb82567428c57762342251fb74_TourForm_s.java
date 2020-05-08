 package models.form;
 
 import java.util.Date;
 
 import play.data.validation.Constraints.Max;
 import play.data.validation.Constraints.MaxLength;
 import play.data.validation.Constraints.Required;
 import play.data.format.Formats.DateTime;
 
 public class TourForm {
     @Required(message="Date must be selected")
     @DateTime(pattern="dd.MM.yyyy")
     public Date date;
     public String country;
     public String city;
     @Required(message="Tour must have a name")
    @MaxLength(value=255, message="The field must not be longer than 255 characters")
     public String name;
    @Max(value=Integer.MAX_VALUE,message="Price can not be greater than " + Integer.MAX_VALUE)
     public double price;
     public String photoName;
     public String descriptionFull;
     public String descriptionShort;
     public String descriptionMini;
     @Override
     public String toString() {
         return "CreateATour [date=" + date + ", country=" + country + ", city=" + city + ", name=" + name + ", price="
                 + price + ", photoName=" + photoName + ", descriptionFull=" + descriptionFull + ", descriptionShort="
                 + descriptionShort + ", descriptionMini=" + descriptionMini + "]";
     }
 }
