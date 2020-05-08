 package uk.co.epii.conservatives.williamcavendishbentinck.extensions;
 
 import uk.co.epii.conservatives.williamcavendishbentinck.tables.DeliveryPointAddress;
 
 import java.util.ArrayList;
 
 /**
  * User: James Robinson
  * Date: 30/09/2013
  * Time: 23:45
  */
 public class DeliveryPointAddressExtensions {
 
     public static String getAddress(DeliveryPointAddress deliveryPointAddress) {
         StringBuilder stringBuilder = new StringBuilder(deliveryPointAddress.getPostcode());
         ArrayList<String> addressLines = new ArrayList<String>();
        addressLines.add(deliveryPointAddress.getPostTown());
         addressLines.add(deliveryPointAddress.getDependentLocality());
         addressLines.add(deliveryPointAddress.getDoubleDependentLocality());
         addressLines.add(deliveryPointAddress.getThoroughfareName());
         addressLines.add(deliveryPointAddress.getDependentThoroughfareName());
         addressLines.add(deliveryPointAddress.getBuildingNumber() == null ? null : deliveryPointAddress.getBuildingNumber()+ "");
         addressLines.add(deliveryPointAddress.getBuildingName());
         addressLines.add(deliveryPointAddress.getSubBuildingName());
         addressLines.add(deliveryPointAddress.getDepartmentName());
         addressLines.add(deliveryPointAddress.getOrganisationName());
         for (String addressLine : addressLines) {
             if (addressLine != null) {
                 stringBuilder.insert(0, ", ");
                 stringBuilder.insert(0, addressLine);
             }
         }
         return stringBuilder.toString();
     }
 
 }
