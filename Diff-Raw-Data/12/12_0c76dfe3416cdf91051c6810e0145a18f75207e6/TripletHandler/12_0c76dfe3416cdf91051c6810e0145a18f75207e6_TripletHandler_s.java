 package org.modica.afp.modca.triplets;
 
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.modica.afp.modca.Context;
 import org.modica.afp.modca.Parameters;
 import org.modica.afp.modca.triplets.foca.ResourceManagement;
 import org.modica.afp.modca.triplets.fullyqualifiedname.FullyQualifiedName;
 
 /**
  * A handler for parsing Structured Field Triplets and Triplet groups.
  */
 public final class TripletHandler {
 
     private TripletHandler() {
     }
 
     /**
      * Parse the triplets from the parameters given starting at position reading length bytes.
      *
      * @param params the triplet parameters
      * @param position the position in the parameters byte array to begin
      * @param length the length of the triplet group
      * @param context holds contextual information about the structured fields being parsed
      * @return A List of triplets
      * @throws MalformedURLException if an error was thrown parsing a URL
      * @throws UnsupportedEncodingException if an error was thrown decoding a String
      */
    public static List<Triplet> parseTriplet(Parameters params, int position, int length,
             Context context) throws MalformedURLException, UnsupportedEncodingException {
        params.skipTo(position);
         List<Triplet> tripletList = new ArrayList<Triplet>();
         // The length field of the recurring group is included in the
         while (params.getPosition() < position + length) {
             int tripletLength = (int) params.getUInt(1);
             TripletIdentifiers tId = TripletIdentifiers.getTripletId(params.getByte());
             switch (tId) {
             case coded_graphic_character_set_global_identifier:
                 tripletList.add(Cgcsgid.parse(params, context));
                 break;
             case character_rotation:
                 tripletList.add(new CharacterRotation(params));
                 break;
             case descriptor_position:
                 tripletList.add(new DescriptorPosition(params.getByte()));
                 break;
             case fully_qualified_name:
                 tripletList.add(FullyQualifiedName.parse(params, tripletLength));
                 break;
             case mapping_option:
                 tripletList.add(new MappingOption(params.getByte()));
                 break;
             case measurement_units:
                 tripletList.add(new MeasurementUnits(params));
                 break;
             case object_area_size:
                 tripletList.add(new ObjectAreaSize(params));
                 break;
             case object_function_set_specification:
                 tripletList.add(new ObjectFunctionSetSpecification(params, tripletLength));
                 break;
             case resource_local_identifier:
                 tripletList.add(new ResourceLocalId(params));
                 break;
             case resource_management:
                 tripletList.add(ResourceManagement.parse(params));
                 break;
             default:
                 params.skip(tripletLength - 2);
             }
         }
        params.skipTo(0);
         return tripletList;
     }
 
     /**
      * Parse the triplet from the parameters starting at position, reading the full byte array.
      *
      * @param params the triplet parameters
      * @param position the position in the parameters byte array to begin
      * @param context holds contextual information about the structured fields being parsed
      * @return A List of triplets
      * @throws MalformedURLException if an error was thrown parsing a URL
      * @throws UnsupportedEncodingException if an error was thrown decoding a String
      */
     public static List<Triplet> parseTriplet(Parameters params, int position, Context context)
             throws MalformedURLException, UnsupportedEncodingException {
         if (position > params.size()) {
             return new ArrayList<Triplet>();
         }
        return parseTriplet(params, position, params.size() - position, context);
     }
 
     /**
      * Parse the repeating group of triplets from the given parameters data.
      *
      * @param params the triplet parameters
      * @param context holds contextual information about the structured fields being parsed
      * @return A {@link RepeatingTripletGroup}
      * @throws MalformedURLException if an error was thrown parsing a URL
      * @throws UnsupportedEncodingException if an error was thrown decoding a String
      */
     public static RepeatingTripletGroup parseRepeatingGroup(Parameters params, Context context)
             throws MalformedURLException, UnsupportedEncodingException {
         int byteIndex = 2;
         List<List<Triplet>> repeatingTriplets = new ArrayList<List<Triplet>>();
 
         while (byteIndex < params.size()) {
             int rgLength = (int) params.getUInt(2);
             List<Triplet> triplets = TripletHandler.parseTriplet(params, byteIndex,
                     rgLength - 2, context);
             repeatingTriplets.add(triplets);
             byteIndex += rgLength;
         }
         return new RepeatingTripletGroup(repeatingTriplets);
     }
 }
