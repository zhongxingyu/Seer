 /*
  * Main class
  */
 package com.faisalman.phoneidentifier;
 
 /**
  *
  * @author Faisal Salman
  */
 public abstract class PhoneIdentifier {
     public static Phone identify (String num) {
         Phone phone = new Phone();        
         // set default values
         phone.areaCode = "";
         phone.country = Country.Indonesia;
         phone.provider = Provider.Unknown;
        phone.region = new Region("");
         // check country code
         if (!num.startsWith("0")) {
             if (!num.matches("^\\+?62\\d+")) {
                 return phone; // unsupported phone number outside Indonesia
             } else {
                 num = num.replaceFirst("\\+?\\d\\d", "0");
             }
         }
         // set provider
         if (num.startsWith("08")) {
             if (num.matches("^08(11|12|13|21|22|52|53|23)\\d+")) {
                 phone.provider = Provider.Telkomsel;
             } else if (num.matches("^08(14|15|16|55|58|56|57)\\d+")) {
                 phone.provider = Provider.Indosat;
             } else if (num.matches("^08(17|18|19|59|74|76|77|78|79)\\d+")) {
                 phone.provider = Provider.XL;
             } else if (num.matches("^08(99|98|97|96)\\d+")) {
                 phone.provider = Provider.Three;
             } else if (num.matches("^08(38|32|31)\\d+")) {
                 phone.provider = Provider.Axis;
             } else if (num.matches("^08(81|82|88)\\d+")) {
                 phone.provider = Provider.Smartfren;
             } else if (num.matches("^08(27|28)\\d+")) {
                 phone.provider = Provider.Ceria;
             } else if (num.matches("^08681\\d+")) {
                 phone.provider = Provider.Byru;
             }
         } else {
             // sanitize area code
             if(num.matches("^0(61|21|22|24|31)\\d+")) {
                 phone.areaCode = num.substring(1, 3);
                 num = num.substring(3);
             } else {
                 phone.areaCode = num.substring(1, 4);
                 num = num.substring(4);
             }
             // set region
            phone.region = new Region(phone.areaCode);
             // set provider
             if (num.matches("^(30|60|61|62|63|90)\\d+")) {
                 phone.provider = Provider.StarOne;
             } else if (num.matches("^(9|80|83)\\d+")) {
                 phone.provider = Provider.Esia;
             } else if (num.matches("^(70|68|54|80|81|3)\\d+")) {
                 phone.provider = Provider.Flexi;
             } else if (num.matches("^(50|21|31)\\d+")) {
                 phone.provider = Provider.Smartfren;
             }
         }
         return phone;
     }
 }
