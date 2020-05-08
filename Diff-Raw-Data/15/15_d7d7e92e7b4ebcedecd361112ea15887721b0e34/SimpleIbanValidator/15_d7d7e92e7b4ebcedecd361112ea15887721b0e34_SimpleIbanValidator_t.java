 package uk.co.scattercode.iban;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.springframework.stereotype.Service;
 
 import uk.co.scattercode.iban.enums.CountryEnum;
 
 /**
  * This IBAN validator does no more than check that the country code is for a
  * valid country and that the IBAN passes a MOD-97 check.
  * 
  * @see{Mod97IbanValidator for details of the MOD-97 check.
  * 
  * @author Stephen Masters
  */
 @Service("simpleIbanValidator")
 public class SimpleIbanValidator implements IbanValidator {
 
     /**
      * A map containing the countries permitted.
      */
     private final Map<String, CountryEnum> countryMap;
 
     private static final Mod97IbanValidator MOD_97 = new Mod97IbanValidator();
 
     /**
      * Default constructor sets up the permitted countries based on all those
      * defined in the {@link CountryEnum} enum.
      */
     public SimpleIbanValidator() {
         countryMap = new HashMap<String, CountryEnum>();
         for (CountryEnum c : CountryEnum.values()) {
            countryMap.put(c.isoCode, c);
         }
     }
 
     public IbanValidationResult validate(String iban) {
         return new IbanValidationResult(iban, isValid(iban),
                 IbanUtil.sanitize(iban), IbanFormatter.printFormat(iban));
     }
 
     /**
      * Check that the country code is for a valid country and that the IBAN
      * passes a MOD-97 check.
      * 
      * @see{Mod97IbanValidator for details of the MOD-97 check.
      */
     private boolean isValid(String iban) {
         if (iban == null) {
             return false;
         }
         if (countryMap.get(iban.substring(0, 2)) == null) {
             // It's not a known country.
             return false;
         }
         // If the checksum divided by 97 leaves a remainder of 1,
         // the IBAN is valid.
         return MOD_97.isValid(IbanUtil.sanitize(iban));
     }
 
 }
