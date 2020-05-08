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
    @Required(message="Please, choose country")
     public String country;
    @Required(message="Please, choose city")
     public String city;
     @Required(message="Tour must have a name")
     public String name;
    @Required(message="Price is required. Put 0 if the tour is free")
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
