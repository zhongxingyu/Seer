 package com.HuskySoft.metrobike.backend;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 
 import com.HuskySoft.metrobike.algorithm.SimpleAlgorithm;
 
 /**
  * @author coreyh3
  * @author dutchscout Represents a request for directions to the MetroBike
  *         backend.
  */
 public final class DirectionsRequest implements Serializable {
     /**
      * Part of serializability, this id tracks if a serialized object can be
      * deserialized using this version of the class.
      * 
      * NOTE: Please add 1 to this number every time you change the readObject()
      * or writeObject() methods, so we don't have old-version DirectionsRequest
      * objects (ex: from the log) being made into new-version DirectionsRequest
      * objects.
      */
     private static final long serialVersionUID = 2L;
 
     /**
      * The tag for Android logging.
      */
     private static final String TAG = "MetroBikeDirectionsRequest";
 
     /**
      * The request parameters object for this request, which specifies all of
      * the important request details.
      */
     private RequestParameters myParams;
 
     /**
      * When the request is complete, this holds a list of all of the chosen
      * routes.
      */
     private List<Route> solutions;
 
     /**
      * Holds any error message(s) generated during execution.
      */
     private String errorMessages;
 
     /**
      * Constructs a new DirectionsRequest object.
      */
     public DirectionsRequest() {
         myParams = new RequestParameters();
         solutions = null;
         errorMessages = null;
     }
 
     /**
      * Initiates the request calculation. This is a blocking call. NOTE: This
      * method is currently under heavy testing and does not currently meet style
      * guidelines. //TODO replace this dummy method with the live code.
      * 
      * @return the final status of the doRequest process
      */
     public DirectionsStatus doDummyRequest() {
 
         // First, validate our parameters!
         if (!myParams.validateParameters()) {
             return DirectionsStatus.INVALID_REQUEST_PARAMS;
         }
 
         Log.w(TAG, "RUNNING ON DUMMY JSON RESPONSE");
 
         // TODO Remove this hard-coded JSON test
         solutions = new ArrayList<Route>();
 
         JSONObject myJSON;
         try {
             myJSON = new JSONObject(DUMMY_TRANSIT_MULTI_JSON);
             // Commenting this out until we have this part completely working.
             // myJSON = new JSONObject(jsonResponse);
             // TODO: Decide how to handle this if the response is not "OK"
             // For now just log the error and continue.
             if (!myJSON.getString(WebRequestJSONKeys.STATUS.getLowerCase()).equalsIgnoreCase(
                     GoogleMapsResponseStatusCodes.OK.toString())) {
                 Log.e(TAG,
                         "JSON Response returned: "
                                 + myJSON.getString(WebRequestJSONKeys.STATUS.getLowerCase()));
             }
         } catch (JSONException e) {
             appendErrorMessage("Error parsing JSON.");
             return DirectionsStatus.PARSING_ERROR;
         }
 
         JSONArray routesArray;
 
         try {
             routesArray = myJSON.getJSONArray(WebRequestJSONKeys.ROUTES.getLowerCase());
             for (int i = 0; i < routesArray.length(); i++) {
                 Route currentLeg = Route.buildRouteFromJSON(routesArray.getJSONObject(i));
                 solutions.add(currentLeg);
             }
             Log.v("JSON_TEST", "Processed " + routesArray.length() + " routes!");
         } catch (JSONException e1) {
             appendErrorMessage("Error parsing JSON routes.");
             return DirectionsStatus.PARSING_ERROR;
         }
 
         return DirectionsStatus.REQUEST_SUCCESSFUL;
     }
 
     /**
      * Initiates the request calculation. This is a blocking call.
      * 
      * @return the final status of the doRequest process
      */
     public DirectionsStatus doRequest() {
 
         // First, validate our parameters!
         if (!myParams.validateParameters()) {
             return DirectionsStatus.INVALID_REQUEST_PARAMS;
         }
 
         solutions = new ArrayList<Route>();
 
         /*
          * TODO: Idea: Instantiate the algorithm workers into a
          * List<AlgorithmWorkers> in a different method, then call them in a
          * nested foreach loop to get all of the results (?)
          */
 
         // Query the simple algorithm first
         SimpleAlgorithm firstAlg = new SimpleAlgorithm();
         DirectionsStatus firstStatus = firstAlg.findRoutes(myParams);
         if (firstStatus.isError()) {
             appendErrorMessage(firstAlg.getErrors());
             return firstStatus;
         } else {
             List<Route> firstRoutes = firstAlg.getResults();
 
             if (firstRoutes == null) {
                 Log.e(TAG, "Got null from SimpleAlgorithm without an error");
                 appendErrorMessage(DirectionsStatus.NO_RESULTS_FOUND.getMessage());
                 return DirectionsStatus.NO_RESULTS_FOUND;
             }
 
             solutions.addAll(firstRoutes);
         }
 
         return DirectionsStatus.REQUEST_SUCCESSFUL;
     }
 
     /**
      * Holds the parameters of a request for directions. Made separate from
      * DirectionsRequest so it can be passed to various Algorithm
      * implementations.
      * 
      * @author dutchscout
      */
     public final class RequestParameters implements Serializable {
         /*
          * NOTE: The structure of this inner class is weird. The getters are
          * inside the class, but the setters are outside the class (inside the
          * DirectionsRequest). This is attempting to make the class read-only to
          * the world, but writable by its enclosing DirectionsRequest object.
          * That way, the DirectionsRequest object can control if/how other
          * classes can modify the RequestParameters, but we can still pass
          * around the RequestParameters object as input to the AlgorithmWorker
          * classes.
          */
 
         /**
          * Part of serializability, this id tracks if a serialized object can be
          * deserialized using this version of the class.
          * 
          * NOTE: Please add 1 to this number every time you change the
          * readObject() or writeObject() methods, so we don't have old-version
          * DirectionsRequest objects (ex: from the log) being made into
          * new-version DirectionsRequest objects.
          */
         private static final long serialVersionUID = 0L;
 
         /**
          * The Android log tag for this inner class.
          */
         private static final String RP_TAG = "DirectionsRequest";
 
         /**
          * The starting location that the user wants directions from.
          */
         private String startAddress = null;
 
         /**
          * The ending location that the user wants directions to.
          */
         private String endAddress = null;
 
         /**
          * Time the user would like to arrive at their destination. If this is
          * set (ie. it equals a nonzero value), then departure time should not
          * be set.
          */
         private long arrivalTime = 0;
 
         /**
          * Time the user would like to depart from their starting location. If
          * this is set (ie. it equals a nonzero value), then arrival time should
          * not be set.
          */
         private long departureTime = 0;
 
         /**
          * The travel mode the user is requesting directions for. Default is
          * "MIXED."
          */
         private TravelMode travelMode = TravelMode.MIXED;
 
         /**
          * An optional field. It sets a minimum distance the user would like to
          * bike.
          */
         private long minDistanceToBikeInMeters = 0;
 
         /**
          * An optional field. It sets the maximum distance the user would like
          * to bike. (In case they are exercise averse)
          */
         private long maxDistanceToBikeInMeters = 0;
 
         /**
          * An optional field. The minimum number of bus transfers to have. In
          * case they like multiple transfers.
          */
         private int minNumberBusTransfers = 0;
 
         /**
          * An optional field. Setting the maximum number of bus transfers in
          * case the user doesn't want routes with lots of transfers.
          */
         private int maxNumberBusTransfers = 0;
 
         /**
          * @return the startAddress
          */
         public String getStartAddress() {
             return startAddress;
         }
 
         /**
          * @return the endAddress
          */
         public String getEndAddress() {
             return endAddress;
         }
 
         /**
          * @return the arrivalTime
          */
         public long getArrivalTime() {
             return arrivalTime;
         }
 
         /**
          * @return the departureTime
          */
         public long getDepartureTime() {
             return departureTime;
         }
 
         /**
          * @return the travelMode
          */
         public TravelMode getTravelMode() {
             return travelMode;
         }
 
         /**
          * @return the minDistanceToBikeInMeters
          */
         public long getMinDistanceToBikeInMeters() {
             return minDistanceToBikeInMeters;
         }
 
         /**
          * @return the maxDistanceToBikeInMeters
          */
         public long getMaxDistanceToBikeInMeters() {
             return maxDistanceToBikeInMeters;
         }
 
         /**
          * @return the minNumberBusTransfers
          */
         public int getMinNumberBusTransfers() {
             return minNumberBusTransfers;
         }
 
         /**
          * @return the maxNumberBusTransfers
          */
         public int getMaxNumberBusTransfers() {
             return maxNumberBusTransfers;
         }
 
         /**
          * Returns whether the request parameters are valid. If they are not
          * valid, also set errors indicating the problem.
          * 
          * @return true if parameters are valid, false otherwise
          */
         public boolean validateParameters() {
 
             if (startAddress == null || startAddress.trim().isEmpty()) {
                 appendErrorMessage("Origin address may not be empty.");
                 return false;
             }
 
             if (endAddress == null || endAddress.trim().isEmpty()) {
                 appendErrorMessage("Destination address may not be empty.");
                 return false;
             }
 
             switch (travelMode) {
             case BICYCLING:
                 // This mode requires no extra checks.
                 break;
             case TRANSIT:
             case MIXED:
                 // Check to be sure we got a time!
                 if (departureTime == 0 && arrivalTime == 0) {
                     appendErrorMessage("Directions for transit must include"
                             + " a departure or arrival time.");
                     return false;
                 }
                 break;
             default:
                 // Including walking since the Algorithm switch statements
                 // don't check for walking.
                 appendErrorMessage("Unsupported desired travel mode " + travelMode);
                 return false;
             }
 
             // Not checking if the departure time is valid (if set)
             // as google lets you set times in the past.
 
             // Validate optional parameters
             if (minDistanceToBikeInMeters < 0 || maxDistanceToBikeInMeters < 0
                     || minNumberBusTransfers < 0 || maxDistanceToBikeInMeters < 0) {
                 appendErrorMessage("All optional parameters (biking distance and bus transfers)"
                         + " must be greater than or equal to zero");
                 return false;
             }
 
             if (minDistanceToBikeInMeters > maxDistanceToBikeInMeters) {
                 appendErrorMessage("Min > Max for distance to bike.");
                 return false;
             }
 
             if (minNumberBusTransfers > maxNumberBusTransfers) {
                 appendErrorMessage("Min > Max for number of transfers");
                 return false;
             }
 
             // Printing out the parameters for debug purposes
             Log.v(RP_TAG, "StartAddress: " + startAddress);
             Log.v(RP_TAG, "EndAddress: " + endAddress);
             Log.v(RP_TAG, "ArrivalTime: " + arrivalTime);
             Log.v(RP_TAG, "DepartureTime: " + departureTime);
             Log.v(RP_TAG, "TravelMode: " + travelMode);
             Log.v(RP_TAG, "MinDistanceToBikeInMeters: " + minDistanceToBikeInMeters);
             Log.v(RP_TAG, "MaxDistanceToBikeInMeters: " + maxDistanceToBikeInMeters);
             Log.v(RP_TAG, "MinNumberBusTransfers: " + minNumberBusTransfers);
             Log.v(RP_TAG, "MaxNumberBusTransfers: " + maxNumberBusTransfers);
 
             return true;
         }
 
         /**
          * Implements a custom serialization of a DirectionsRequest object.
          * 
          * @param out
          *            the ObjectOutputStream to write to
          * @throws IOException
          *             if the stream fails
          */
         private void writeObject(final ObjectOutputStream out) throws IOException {
             // Write each field to the stream in a specific order.
             // Specifying this order helps shield the class from problems
             // in future versions.
             // The order must be the same as the read order in readObject()
             out.writeObject(startAddress);
             out.writeObject(endAddress);
             out.writeLong(arrivalTime);
             out.writeLong(departureTime);
             out.writeObject(travelMode);
             out.writeLong(minDistanceToBikeInMeters);
             out.writeLong(maxDistanceToBikeInMeters);
             out.writeInt(minNumberBusTransfers);
             out.writeInt(maxNumberBusTransfers);
         }
 
         /**
          * Implements a custom deserialization of a DirectionsRequest object.
          * 
          * @param in
          *            the ObjectInputStream to read from
          * @throws IOException
          *             if the stream fails
          * @throws ClassNotFoundException
          *             if a class is not found
          */
         private void readObject(final ObjectInputStream in) throws IOException,
                 ClassNotFoundException {
             // Read each field from the stream in a specific order.
             // Specifying this order helps shield the class from problems
             // in future versions.
             // The order must be the same as the writing order in writeObject()
             startAddress = (String) in.readObject();
             endAddress = (String) in.readObject();
             arrivalTime = in.readLong();
             departureTime = in.readLong();
             travelMode = (TravelMode) in.readObject();
             minDistanceToBikeInMeters = in.readLong();
             maxDistanceToBikeInMeters = in.readLong();
             minNumberBusTransfers = in.readInt();
             maxNumberBusTransfers = in.readInt();
         }
 
         @Override
         public String toString() {
             return "RequestParameters:" + "\nstartAddress: " + startAddress + "\nendAddress: "
                     + endAddress + "\narrivalTime: " + arrivalTime + "\ndepartureTime: "
                     + departureTime + "\ntravelMode: " + travelMode.toString()
                     + "\nminDistanceToBikeInMeters: " + minDistanceToBikeInMeters
                     + "\nmaxDistanceToBikeInMeters: " + maxDistanceToBikeInMeters
                     + "\nminNumberBusTransfers: " + minNumberBusTransfers
                     + "\nmaxNumberBusTransfers: " + maxNumberBusTransfers + "\n";
         }
 
     }
 
     /**
      * Set the starting address for the trip.
      * 
      * @param newStartAddress
      *            the address to start from.
      * @return the modified DirectionsRequest object. Used as part of the
      *         builder pattern.
      */
     public DirectionsRequest setStartAddress(final String newStartAddress) {
         myParams.startAddress = newStartAddress;
         return this;
     }
 
     /**
      * Set the ending address for the trip.
      * 
      * @param newEndAddress
      *            the address to end at.
      * @return the modified DirectionsRequest object. Used as part of the
      *         builder pattern.
      */
     public DirectionsRequest setEndAddress(final String newEndAddress) {
         myParams.endAddress = newEndAddress;
         return this;
     }
 
     /**
      * Set the arrival time. If this is set then departure time should not be
      * set.
      * 
      * @param newArrivalTime
      *            the time the user would like to arrive at their destination.
      * @return the modified DirectionsRequest object. Used as part of the
      *         builder pattern.
      */
     public DirectionsRequest setArrivalTime(final long newArrivalTime) {
         if (myParams.departureTime != 0) {
             throw new IllegalArgumentException("departureTime was already " + "set.");
         }
         myParams.arrivalTime = newArrivalTime;
         return this;
     }
 
     /**
      * Set the departure time. If this is set then the arrival time should not
      * be set.
      * 
      * @param newDepartureTime
      *            the time the user would like to depart from their starting
      *            location.
      * @return the modified DirectionsRequest object. Used as part of the
      *         builder pattern.
      */
     public DirectionsRequest setDepartureTime(final long newDepartureTime) {
         if (myParams.arrivalTime != 0) {
             throw new IllegalArgumentException("departureTime was " + "already set.");
         }
         myParams.departureTime = newDepartureTime;
         return this;
     }
 
     /**
      * Set the travel mode that the user would like to arrive at their
      * destination. The default travel mode is MIXED.
      * 
      * @param newTravelMode
      *            the TravelMode that the user would like to arrive at their
      *            destination.
      * @return the modified DirectionsRequest object. Used as part of the
      *         builder pattern.
      */
     public DirectionsRequest setTravelMode(final TravelMode newTravelMode) {
         myParams.travelMode = newTravelMode;
         return this;
     }
 
     /**
      * Set the minimum distance the user would like to bike in meters. This
      * parameter is optional.
      * 
      * @param newMinDistanceToBikeInMeters
      *            the minimum distance to bike in mneters
      * @return the modified DirectionsRequest object. Used as part of the
      *         builder pattern.
      */
     public DirectionsRequest setMinDistanceToBikeInMeters(final long newMinDistanceToBikeInMeters) {
         myParams.minDistanceToBikeInMeters = newMinDistanceToBikeInMeters;
         return this;
     }
 
     /**
      * Set the maximum distance to bike in meters. This parameter is optional.
      * 
      * @param newMaxDistanceToBikeInMeters
      *            the max distance to bike in meters.
      * @return the modified DirectionsRequest object. Used as part of the
      *         builder pattern.
      */
     public DirectionsRequest setMaxDistanceToBikeInMeters(final long newMaxDistanceToBikeInMeters) {
         myParams.maxDistanceToBikeInMeters = newMaxDistanceToBikeInMeters;
         return this;
     }
 
     /**
      * Set the minimum number of bus transfers. This parameter is optional.
      * 
      * @param newMinNumberBusTransfers
      *            the minimum number of bus transfers.
      * @return the modified DirectionsRequest object. Used as part of the
      *         builder pattern.
      */
     public DirectionsRequest setMinNumberBusTransfers(final int newMinNumberBusTransfers) {
         myParams.minNumberBusTransfers = newMinNumberBusTransfers;
         return this;
     }
 
     /**
      * Set the maximum number of bus transfers. This parameter is optional.
      * 
      * @param newMaxNumberBusTransfers
      *            the maximum number of bus transfers.
      * @return the modified DirectionsRequest object. Used as part of the
      *         builder pattern.
      */
     public DirectionsRequest setMaxNumberBusTransfers(final int newMaxNumberBusTransfers) {
         myParams.maxNumberBusTransfers = newMaxNumberBusTransfers;
         return this;
     }
 
     /**
      * @return the errorMessages. Returns null if no messages have been set.
      */
     public String getErrorMessages() {
         return errorMessages;
     }
 
     /**
      * @param errorMessage
      *            the errorMessage to add
      */
     private void appendErrorMessage(final String errorMessage) {
         if (errorMessages == null) {
             errorMessages = errorMessage;
         } else {
             errorMessages = errorMessages + "\n" + errorMessage;
         }
     }
 
     /**
      * Return the solutions to the request for directions. This field is null
      * before doRequest() is called.
      * 
      * @return the solutions
      */
     public List<Route> getSolutions() {
         return solutions;
     }
 
     @Override
     public String toString() {
         return "DirectionsRequest: " + myParams.toString() + "solutions: "
                 + Utility.listPrettyPrint(solutions, 0);
     }
 
     /**
      * Implements a custom serialization of a DirectionsRequest object.
      * 
      * @param out
      *            the ObjectOutputStream to write to
      * @throws IOException
      *             if the stream fails
      */
     private void writeObject(final ObjectOutputStream out) throws IOException {
         // Write each field to the stream in a specific order.
         // Specifying this order helps shield the class from problems
         // in future versions.
         // The order must be the same as the read order in readObject()
         out.writeObject(myParams);
         out.writeObject(solutions);
     }
 
     /**
      * Implements a custom deserialization of a DirectionsRequest object.
      * 
      * @param in
      *            the ObjectInputStream to read from
      * @throws IOException
      *             if the stream fails
      * @throws ClassNotFoundException
      *             if a class is not found
      */
     @SuppressWarnings("unchecked")
     private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
         // Read each field from the stream in a specific order.
         // Specifying this order helps shield the class from problems
         // in future versions.
         // The order must be the same as the writing order in writeObject()
         myParams = (RequestParameters) in.readObject();
         solutions = (List<Route>) in.readObject();
     }
 
     /**
      * URL to grab dummy bicycle directions.
      */
     private static final String DUMMY_BICYCLE_URL = "http://maps.googleapis.com"
             + "/maps/api/directions/json?origin=6504%20Latona%20Ave%20NE%2CSea"
             + "ttle%2CWA&destination=3801%20Brooklyn%20Ave%20NE%2CSeattle%2CWA"
             + "&sensor=false&mode=bicycling";
 
     /**
      * Dummy JSON bicycle directions.
      */
     private static final String DUMMY_BICYCLE_JSON = "{" + "\n   \"routes\" : [\n    "
             + "  {\n         \"bounds\" : {\n            \"northeast\" : {\n  "
             + "             \"lat\" : 47.676040,\n               \"lng\" : -12"
             + "2.313250\n            },\n            \"southwest\" : {\n      "
             + "         \"lat\" : 47.65358000000001,\n               \"lng\" :"
             + " -122.325820\n            }\n         },\n         \"copyrights"
             + "\" : \"Map data \u00a92013 Google\",\n         \"legs\" : [\n  "
             + "          {\n               \"distance\" : {\n                 "
             + " \"text\" : \"2.1 mi\",\n                  \"value\" : 3402\n  "
             + "             },\n               \"duration\" : {\n             "
             + "     \"text\" : \"13 mins\",\n                  \"value\" : 761"
             + "\n               },\n               \"end_address\" : \"3801 Br"
             + "ooklyn Avenue Northeast, University of Washington, Seattle, WA "
             + "98105, USA\",\n               \"end_location\" : {\n           "
             + "       \"lat\" : 47.65390000000001,\n                  \"lng\" "
             + ": -122.314480\n               },\n               \"start_addres"
             + "s\" : \"6504 Latona Avenue Northeast, Seattle, WA 98115, USA\","
             + "\n               \"start_location\" : {\n                  \"la"
             + "t\" : 47.676040,\n                  \"lng\" : -122.325820\n    "
             + "           },\n               \"steps\" : [\n                  "
             + "{\n                     \"distance\" : {\n                     "
             + "   \"text\" : \"59 ft\",\n                        \"value\" : 1"
             + "8\n                     },\n                     \"duration\" :"
             + " {\n                        \"text\" : \"1 min\",\n            "
             + "            \"value\" : 6\n                     },\n           "
             + "          \"end_location\" : {\n                        \"lat\""
             + " : 47.675910,\n                        \"lng\" : -122.325690\n "
             + "                    },\n                     \"html_instruction"
             + "s\" : \"Head \\u003cb\\u003esoutheast\\u003c/b\\u003e on \\u003"
             + "cb\\u003eLatona Ave NE\\u003c/b\\u003e toward \\u003cb\\u003eNE"
             + " 65th St\\u003c/b\\u003e\",\n                     \"polyline\" "
             + ": {\n                        \"points\" : \"gv~aHjwriVXY\"\n   "
             + "                  },\n                     \"start_location\" :"
             + " {\n                        \"lat\" : 47.676040,\n             "
             + "           \"lng\" : -122.325820\n                     },\n    "
             + "                 \"travel_mode\" : \"BICYCLING\"\n             "
             + "     },\n                  {\n                     \"distance\""
             + " : {\n                        \"text\" : \"0.2 mi\",\n         "
             + "               \"value\" : 252\n                     },\n      "
             + "               \"duration\" : {\n                        \"text"
             + "\" : \"1 min\",\n                        \"value\" : 63\n      "
             + "               },\n                     \"end_location\" : {\n "
             + "                       \"lat\" : 47.67588000000001,\n          "
             + "              \"lng\" : -122.322330\n                     },\n "
             + "                    \"html_instructions\" : \"Turn \\u003cb\\u0"
             + "03eleft\\u003c/b\\u003e onto \\u003cb\\u003eNE 65th St\\u003c/b"
             + "\\u003e\",\n                     \"polyline\" : {\n            "
             + "            \"points\" : \"mu~aHpvriV@s@@qE@gE?w@?u@?cB\"\n    "
             + "                 },\n                     \"start_location\" : "
             + "{\n                        \"lat\" : 47.675910,\n              "
             + "          \"lng\" : -122.325690\n                     },\n     "
             + "                \"travel_mode\" : \"BICYCLING\"\n              "
             + "    },\n                  {\n                     \"distance\" "
             + ": {\n                        \"text\" : \"0.4 mi\",\n          "
             + "              \"value\" : 593\n                     },\n       "
             + "              \"duration\" : {\n                        \"text"
             + "\" : \"2 mins\",\n                        \"value\" : 142\n    "
             + "                 },\n                     \"end_location\" : {"
             + "\n                        \"lat\" : 47.67201000000001,\n       "
             + "                 \"lng\" : -122.317360\n                     },"
             + "\n                     \"html_instructions\" : \"Turn \\u003cb"
             + "\\u003eright\\u003c/b\\u003e onto \\u003cb\\u003eNE Ravenna Blv"
             + "d\\u003c/b\\u003e\",\n                     \"polyline\" : {\n  "
             + "                      \"points\" : \"gu~aHpariVZYn@m@bA{@x@s@PO"
             + "FI`@]XWBCBEZYNOh@e@dB{ALMPOjC}BZYN]Jq@|@}G\"\n                 "
             + "    },\n                     \"start_location\" : {\n          "
             + "              \"lat\" : 47.67588000000001,\n                   "
             + "     \"lng\" : -122.322330\n                     },\n          "
             + "           \"travel_mode\" : \"BICYCLING\"\n                  }"
             + ",\n                  {\n                     \"distance\" : {\n"
             + "                        \"text\" : \"1.1 mi\",\n               "
             + "         \"value\" : 1775\n                     },\n           "
             + "          \"duration\" : {\n                        \"text\" : "
             + "\"6 mins\",\n                        \"value\" : 378\n         "
             + "            },\n                     \"end_location\" : {\n    "
             + "                    \"lat\" : 47.65609000000001,\n             "
             + "           \"lng\" : -122.317880\n                     },\n    "
             + "                 \"html_instructions\" : \"Turn \\u003cb\\u003e"
             + "right\\u003c/b\\u003e onto \\u003cb\\u003eRoosevelt Way NE\\u00"
             + "3c/b\\u003e\",\n                     \"polyline\" : {\n        "
             + "                \"points\" : \"a}}aHnbqiVnA?X?nC?jC?V?f@?|@?VAp"
             + "C@fA@~E@~B?hA@P?r@?xB@b@?^?`A@R?jCAjB@~GDj@F^PRLHDHBFBJ@L?tC?~@"
             + "BpA@r@@`DBlD@|BB\\\\?^@r@?L@@?PDHB\"\n                     },\n"
             + "                     \"start_location\" : {\n                  "
             + "      \"lat\" : 47.67201000000001,\n                        \"l"
             + "ng\" : -122.317360\n                     },\n                  "
             + "   \"travel_mode\" : \"BICYCLING\"\n                  },\n     "
             + "             {\n                     \"distance\" : {\n        "
             + "                \"text\" : \"0.2 mi\",\n                       "
             + " \"value\" : 348\n                     },\n                    "
             + " \"duration\" : {\n                        \"text\" : \"2 mins"
             + "\",\n                        \"value\" : 114\n                 "
             + "    },\n                     \"end_location\" : {\n            "
             + "            \"lat\" : 47.65598000000001,\n                     "
             + "   \"lng\" : -122.313250\n                     },\n            "
             + "         \"html_instructions\" : \"Turn \\u003cb\\u003eleft\\u0"
             + "03c/b\\u003e onto \\u003cb\\u003eNE Campus Pkwy\\u003c/b\\u003e"
             + "\",\n                     \"polyline\" : {\n                   "
             + "     \"points\" : \"qyzaHveqiVDS@I@IAkAByB?OBkE@sB?C?qB@wB@qB\""
             + "\n                     },\n                     \"start_locatio"
             + "n\" : {\n                        \"lat\" : 47.65609000000001,\n"
             + "                        \"lng\" : -122.317880\n                "
             + "     },\n                     \"travel_mode\" : \"BICYCLING\"\n"
             + "                  },\n                  {\n                    "
             + " \"distance\" : {\n                        \"text\" : \"0.2 mi"
             + "\",\n                        \"value\" : 268\n                 "
             + "    },\n                     \"duration\" : {\n                "
             + "        \"text\" : \"1 min\",\n                        \"value"
             + "\" : 31\n                     },\n                     \"end_lo"
             + "cation\" : {\n                        \"lat\" : 47.653580000000"
             + "01,\n                        \"lng\" : -122.313340\n           "
             + "          },\n                     \"html_instructions\" : \"Tu"
             + "rn \\u003cb\\u003eright\\u003c/b\\u003e onto \\u003cb\\u003eUni"
             + "versity Way NE\\u003c/b\\u003e\",\n                     \"polyl"
             + "ine\" : {\n                        \"points\" : \"{xzaHxhpiV^B|"
             + "A@hCDlB@L@zA@\"\n                     },\n                     "
             + "\"start_location\" : {\n                        \"lat\" : 47.65"
             + "598000000001,\n                        \"lng\" : -122.313250\n "
             + "                    },\n                     \"travel_mode\" : "
             + "\"BICYCLING\"\n                  },\n                  {\n     "
             + "                \"distance\" : {\n                        \"tex"
             + "t\" : \"371 ft\",\n                        \"value\" : 113\n   "
             + "                  },\n                     \"duration\" : {\n  "
             + "                      \"text\" : \"1 min\",\n                  "
             + "      \"value\" : 21\n                     },\n                "
             + "     \"end_location\" : {\n                        \"lat\" : 47"
             + ".654220,\n                        \"lng\" : -122.314470\n      "
             + "               },\n                     \"html_instructions\" :"
             + " \"Turn \\u003cb\\u003eright\\u003c/b\\u003e onto \\u003cb\\u00"
             + "3eBurke-Gilman Trail\\u003c/b\\u003e\",\n                     "
             + "\"polyline\" : {\n                        \"points\" : \"{izaHj"
             + "ipiVM`@Sh@EHCDEBIDGDEDINEJOb@Un@\"\n                     },\n  "
             + "                   \"start_location\" : {\n                    "
             + "    \"lat\" : 47.65358000000001,\n                        \"lng"
             + "\" : -122.313340\n                     },\n                    "
             + " \"travel_mode\" : \"BICYCLING\"\n                  },\n       "
             + "           {\n                     \"distance\" : {\n          "
             + "              \"text\" : \"115 ft\",\n                        "
             + "\"value\" : 35\n                     },\n                     "
             + "\"duration\" : {\n                        \"text\" : \"1 min\","
             + "\n                        \"value\" : 6\n                     }"
             + ",\n                     \"end_location\" : {\n                 "
             + "       \"lat\" : 47.65390000000001,\n                        \""
             + "lng\" : -122.314480\n                     },\n                 "
             + "    \"html_instructions\" : \"Turn \\u003cb\\u003eleft\\u003c/b"
             + "\\u003e onto \\u003cb\\u003eBrooklyn Ave NE\\u003c/b\\u003e\\u0"
             + "03cdiv style=\\\"font-size:0.9em\\\"\\u003eDestination will be "
             + "on the right\\u003c/div\\u003e\",\n                     \"polyl"
             + "ine\" : {\n                        \"points\" : \"{mzaHlppiVp@@"
             + "L?\"\n                     },\n                     \"start_loc"
             + "ation\" : {\n                        \"lat\" : 47.654220,\n    "
             + "                    \"lng\" : -122.314470\n                    "
             + " },\n                     \"travel_mode\" : \"BICYCLING\"\n    "
             + "              }\n               ],\n               \"via_waypoi"
             + "nt\" : []\n            }\n         ],\n         \"overview_poly"
             + "line\" : {\n            \"points\" : \"gv~aHjwriVXY@s@ByK?mB?cB"
             + "ZYrBiBjAcAfAcAxAuApGwFZYN]hAoIdK?tCAxPDbLDvF?~GDj@Fr@^RHRDbD?fJ"
             + "JhJF`A@RDHBDSBS@eEB{EBaJ@qB^BfFFzBBzA@M`@Yr@IHQJOTUn@Un@p@@L?\""
             + "\n         },\n         \"summary\" : \"Roosevelt Way NE\",\n  "
             + "       \"warnings\" : [\n            \"Bicycling directions are"
             + " in beta. Use caution \u2013 This route may contain streets tha"
             + "t aren\'t suited for bicycling.\"\n         ],\n         \"wayp"
             + "oint_order\" : []\n      }\n   ],\n   \"status\" : \"OK\"\n}";
 
     /**
      * URL to grab dummy transit directions.
      */
     private static final String DUMMY_TRANSIT_URL = "http://"
             + "maps.googleapis.com/maps/api/directions/json?origin=6504%2"
             + "0Latona%20Ave%20NE%2CSeattle%2CWA&destination=3801%20Brooklyn%2"
             + "0Ave%20NE%2CSeattle%2CWA&sensor=false&arrival_time=1368644400&m" + "ode=transit";
 
     /**
      * Dummy JSON transit directions. TODO: Format this code if we're going to
      * keep it here
      */
     private static final String DUMMY_TRANSIT_JSON =
             "{\n   \"routes\" : [\n      {\n         \"bounds\" : {\n            \"northeast\" : {\n               \"lat\" : 47.676040,\n               \"lng\" : -122.311880\n            },\n            \"southwest\" : {\n               \"lat\" : 47.65390000000001,\n               \"lng\" : -122.325820\n            }\n         },\n         \"copyrights\" : \"Map data \u00a92013 Google\",\n         \"legs\" : [\n            {\n               \"arrival_time\" : {\n                  \"text\" : \"11:50am\",\n                  \"time_zone\" : \"America/Los_Angeles\",\n                  \"value\" : 1368643806\n               },\n               \"departure_time\" : {\n                  \"text\" : \"11:31am\",\n                  \"time_zone\" : \"America/Los_Angeles\",\n                  \"value\" : 1368642661\n               },\n               \"distance\" : {\n                  \"text\" : \"2.3 mi\",\n                  \"value\" : 3742\n               },\n               \"duration\" : {\n                  \"text\" : \"19 mins\",\n                  \"value\" : 1144\n               },\n               \"end_address\" : \"3801 Brooklyn Avenue Northeast, University of Washington, Seattle, WA 98105, USA\",\n               \"end_location\" : {\n                  \"lat\" : 47.65390000000001,\n                  \"lng\" : -122.314480\n               },\n               \"start_address\" : \"6504 Latona Avenue Northeast, Seattle, WA 98115, USA\",\n               \"start_location\" : {\n                  \"lat\" : 47.676040,\n                  \"lng\" : -122.325820\n               },\n               \"steps\" : [\n                  {\n                     \"distance\" : {\n                        \"text\" : \"0.2 mi\",\n                        \"value\" : 343\n                     },\n                     \"duration\" : {\n                        \"text\" : \"4 mins\",\n                        \"value\" : 254\n                     },\n                     \"end_location\" : {\n                        \"lat\" : 47.67580,\n                        \"lng\" : -122.321360\n                     },\n                     \"html_instructions\" : \"Walk to NE 65th St & NE Ravenna Blvd\",\n                     \"polyline\" : {\n                        \"points\" : \"gv~aHjwriVXY@s@@qE@gE?w@?u@?cB@eB?{AL?\"\n                     },\n                     \"start_location\" : {\n                        \"lat\" : 47.676040,\n                        \"lng\" : -122.325820\n                     },\n                     \"steps\" : [\n                        {\n                           \"distance\" : {\n                              \"text\" : \"59 ft\",\n                              \"value\" : 18\n                           },\n                           \"duration\" : {\n                              \"text\" : \"1 min\",\n                              \"value\" : 15\n                           },\n                           \"end_location\" : {\n                              \"lat\" : 47.675910,\n                              \"lng\" : -122.325690\n                           },\n                           \"html_instructions\" : \"Head \\u003cb\\u003esoutheast\\u003c/b\\u003e on \\u003cb\\u003eLatona Ave NE\\u003c/b\\u003e toward \\u003cb\\u003eNE 65th St\\u003c/b\\u003e\",\n                           \"polyline\" : {\n                              \"points\" : \"gv~aHjwriVXY\"\n                           },\n                           \"start_location\" : {\n                              \"lat\" : 47.676040,\n                              \"lng\" : -122.325820\n                           },\n                           \"travel_mode\" : \"WALKING\"\n                        },\n                        {\n                           \"distance\" : {\n                              \"text\" : \"0.2 mi\",\n                              \"value\" : 325\n                           },\n                           \"duration\" : {\n                              \"text\" : \"4 mins\",\n                              \"value\" : 239\n                           },\n                           \"end_location\" : {\n                              \"lat\" : 47.67580,\n                              \"lng\" : -122.321360\n                           },\n                           \"html_instructions\" : \"Turn \\u003cb\\u003eleft\\u003c/b\\u003e onto \\u003cb\\u003eNE 65th St\\u003c/b\\u003e\",\n                           \"polyline\" : {\n                              \"points\" : \"mu~aHpvriV@s@@qE@gE?w@?u@?cB@eB?{AL?\"\n                           },\n                           \"start_location\" : {\n                              \"lat\" : 47.675910,\n                              \"lng\" : -122.325690\n                           },\n                           \"travel_mode\" : \"WALKING\"\n                        }\n                     ],\n                     \"travel_mode\" : \"WALKING\"\n                  },\n                  {\n                     \"distance\" : {\n                        \"text\" : \"1.9 mi\",\n                        \"value\" : 3028\n                     },\n                     \"duration\" : {\n                        \"text\" : \"11 mins\",\n                        \"value\" : 650\n                     },\n                     \"end_location\" : {\n                        \"lat\" : 47.655050,\n                        \"lng\" : -122.312210\n                     },\n                     \"html_instructions\" : \"Bus towards Mount Baker Transit Center, University District\",\n                     \"polyline\" : {\n                        \"points\" : \"wt~aHn{qiVM??K@Y?W@eA?o@?C?Q?y@@mC?_C@iC?_B?a@@sI@{@?{A?oB@yB@yB?uB?}A?Y~@?t@?tACJ?fA?hA?p@?nHCT?T?r@?D?b@?~@?v@?V?JA`G?tDAB?J?tKFX?xGBhA?~GDfGDpAAP?^CjBCzCFpA@rEBjHFlA@h@?l@@~B?x@@?V\"\n                     },\n                     \"start_location\" : {\n                        \"lat\" : 47.67580,\n                        \"lng\" : -122.321360\n                     },\n                     \"transit_details\" : {\n                        \"arrival_stop\" : {\n                           \"location\" : {\n                              \"lat\" : 47.65504840,\n                              \"lng\" : -122.312210\n                           },\n                           \"name\" : \"15th Ave NE & NE 40th St\"\n                        },\n                        \"arrival_time\" : {\n                           \"text\" : \"11:46am\",\n                           \"time_zone\" : \"America/Los_Angeles\",\n                           \"value\" : 1368643566\n                        },\n                        \"departure_stop\" : {\n                           \"location\" : {\n                              \"lat\" : 47.67579650,\n                              \"lng\" : -122.3213580\n                           },\n                           \"name\" : \"NE 65th St & NE Ravenna Blvd\"\n                        },\n                        \"departure_time\" : {\n                           \"text\" : \"11:35am\",\n                           \"time_zone\" : \"America/Los_Angeles\",\n                           \"value\" : 1368642916\n                        },\n                        \"headsign\" : \"Mount Baker Transit Center, University District\",\n                        \"line\" : {\n                           \"agencies\" : [\n                              {\n                                 \"name\" : \"Metro Transit\",\n                                 \"phone\" : \"(206) 553-3000\",\n                                 \"url\" : \"http://metro.kingcounty.gov/\"\n                              }\n                           ],\n                           \"short_name\" : \"48\",\n                           \"url\" : \"http://metro.kingcounty.gov/tops/bus/schedules/s048_0_.html\",\n                           \"vehicle\" : {\n                              \"icon\" : \"//maps.gstatic.com/mapfiles/transit/iw/6/bus.png\",\n                              \"name\" : \"Bus\",\n                              \"type\" : \"BUS\"\n                           }\n                        },\n                        \"num_stops\" : 10\n                     },\n                     \"travel_mode\" : \"TRANSIT\"\n                  },\n                  {\n                     \"distance\" : {\n                        \"text\" : \"0.2 mi\",\n                        \"value\" : 371\n                     },\n                     \"duration\" : {\n                        \"text\" : \"4 mins\",\n                        \"value\" : 240\n                     },\n                     \"end_location\" : {\n                        \"lat\" : 47.65390000000001,\n                        \"lng\" : -122.314480\n                     },\n                     \"html_instructions\" : \"Walk to 3801 Brooklyn Avenue Northeast, University of Washington, Seattle, WA 98105, USA\",\n                     \"polyline\" : {\n                        \"points\" : \"aszaHhbpiV?Uy@AAlFApBAfA?l@`B@bCBp@@L?\"\n                     },\n                     \"start_location\" : {\n                        \"lat\" : 47.655050,\n                        \"lng\" : -122.312210\n                     },\n                     \"steps\" : [\n                        {\n                           \"distance\" : {\n                              \"text\" : \"105 ft\",\n                              \"value\" : 32\n                           },\n                           \"duration\" : {\n                              \"text\" : \"1 min\",\n                              \"value\" : 28\n                           },\n                           \"end_location\" : {\n                              \"lat\" : 47.655340,\n                              \"lng\" : -122.312090\n                           },\n                           \"html_instructions\" : \"Head \\u003cb\\u003enorth\\u003c/b\\u003e on \\u003cb\\u003e15th Ave NE\\u003c/b\\u003e toward \\u003cb\\u003eNE 40th St\\u003c/b\\u003e\",\n                           \"polyline\" : {\n                              \"points\" : \"aszaHhbpiV?Uy@A\"\n                           },\n                           \"start_location\" : {\n                              \"lat\" : 47.655050,\n                              \"lng\" : -122.312210\n                           },\n                           \"travel_mode\" : \"WALKING\"\n                        },\n                        {\n                           \"distance\" : {\n                              \"text\" : \"0.1 mi\",\n                              \"value\" : 176\n                           },\n                           \"duration\" : {\n                              \"text\" : \"2 mins\",\n                              \"value\" : 118\n                           },\n                           \"end_location\" : {\n                              \"lat\" : 47.655370,\n                              \"lng\" : -122.314440\n                           },\n                           \"html_instructions\" : \"Turn \\u003cb\\u003eleft\\u003c/b\\u003e onto \\u003cb\\u003eNE 40th St\\u003c/b\\u003e\",\n                           \"polyline\" : {\n                              \"points\" : \"{tzaHpapiVAlFApBAfA?l@\"\n                           },\n                           \"start_location\" : {\n                              \"lat\" : 47.655340,\n                              \"lng\" : -122.312090\n                           },\n                           \"travel_mode\" : \"WALKING\"\n                        },\n                        {\n                           \"distance\" : {\n                              \"text\" : \"0.1 mi\",\n                              \"value\" : 163\n                           },\n                           \"duration\" : {\n                              \"text\" : \"2 mins\",\n                              \"value\" : 94\n                           },\n                           \"end_location\" : {\n                              \"lat\" : 47.65390000000001,\n                              \"lng\" : -122.314480\n                           },\n                           \"html_instructions\" : \"Turn \\u003cb\\u003eleft\\u003c/b\\u003e onto \\u003cb\\u003eBrooklyn Ave NE\\u003c/b\\u003e\",\n                           \"polyline\" : {\n                              \"points\" : \"auzaHfppiV`B@bCBp@@L?\"\n                           },\n                           \"start_location\" : {\n                              \"lat\" : 47.655370,\n                              \"lng\" : -122.314440\n                           },\n                           \"travel_mode\" : \"WALKING\"\n                        }\n                     ],\n                     \"travel_mode\" : \"WALKING\"\n                  }\n               ],\n               \"via_waypoint\" : []\n            }\n         ],\n         \"overview_polyline\" : {\n            \"points\" : \"gv~aHjwriVXY@s@ByK@wH?{AL?M??K@q@@uB@}E@kKB{QBgM?Y~@?jCCnE?pPCzMC~LFbJBfPJbBAjCGlFH~NJdHBx@@?V?Uy@AC~IAtBdHF\"\n         },\n         \"warnings\" : [\n            \"Walking directions are in beta.    Use caution \u2013 This route may be missing sidewalks or pedestrian paths.\"\n         ],\n         \"waypoint_order\" : []\n      }\n   ],\n   \"status\" : \"OK\"\n}";
 
     /**
      * URL to grab dummy multi-route transit directions.
      */
     private static final String DUMMY_TRANSIT_MULTI_URL = "http://"
             + "maps.googleapis.com/maps/api/directions/json?origin=6504%20Lato"
             + "na%20Ave%20NE%2CSeattle%2CWA&destination=3801%20Brooklyn%20Ave%"
             + "20NE%2CSeattle%2CWA&sensor=false&arrival_time=1368644400&mode=t"
             + "ransit&alternatives=true";
 
     /**
      * Dummy JSON multi-route transit directions. TODO: Format this code if
      * we're going to keep it here
      */
     private static final String DUMMY_TRANSIT_MULTI_JSON =
             "{\n   \"routes\" : [\n      {\n         \"bounds\" : {\n            \"northeast\" : {\n               \"lat\" : 47.676040,\n               \"lng\" : -122.311880\n            },\n            \"southwest\" : {\n               \"lat\" : 47.65390000000001,\n               \"lng\" : -122.325820\n            }\n         },\n         \"copyrights\" : \"Map data \u00a92013 Google\",\n         \"legs\" : [\n            {\n               \"arrival_time\" : {\n                  \"text\" : \"11:50am\",\n                  \"time_zone\" : \"America/Los_Angeles\",\n                  \"value\" : 1368643806\n               },\n               \"departure_time\" : {\n                  \"text\" : \"11:31am\",\n                  \"time_zone\" : \"America/Los_Angeles\",\n                  \"value\" : 1368642661\n               },\n               \"distance\" : {\n                  \"text\" : \"2.3 mi\",\n                  \"value\" : 3742\n               },\n               \"duration\" : {\n                  \"text\" : \"19 mins\",\n                  \"value\" : 1144\n               },\n               \"end_address\" : \"3801 Brooklyn Avenue Northeast, University of Washington, Seattle, WA 98105, USA\",\n               \"end_location\" : {\n                  \"lat\" : 47.65390000000001,\n                  \"lng\" : -122.314480\n               },\n               \"start_address\" : \"6504 Latona Avenue Northeast, Seattle, WA 98115, USA\",\n               \"start_location\" : {\n                  \"lat\" : 47.676040,\n                  \"lng\" : -122.325820\n               },\n               \"steps\" : [\n                  {\n                     \"distance\" : {\n                        \"text\" : \"0.2 mi\",\n                        \"value\" : 343\n                     },\n                     \"duration\" : {\n                        \"text\" : \"4 mins\",\n                        \"value\" : 254\n                     },\n                     \"end_location\" : {\n                        \"lat\" : 47.67580,\n                        \"lng\" : -122.321360\n                     },\n                     \"html_instructions\" : \"Walk to NE 65th St & NE Ravenna Blvd\",\n                     \"polyline\" : {\n                        \"points\" : \"gv~aHjwriVXY@s@@qE@gE?w@?u@?cB@eB?{AL?\"\n                     },\n                     \"start_location\" : {\n                        \"lat\" : 47.676040,\n                        \"lng\" : -122.325820\n                     },\n                     \"steps\" : [\n                        {\n                           \"distance\" : {\n                              \"text\" : \"59 ft\",\n                              \"value\" : 18\n                           },\n                           \"duration\" : {\n                              \"text\" : \"1 min\",\n                              \"value\" : 15\n                           },\n                           \"end_location\" : {\n                              \"lat\" : 47.675910,\n                              \"lng\" : -122.325690\n                           },\n                           \"html_instructions\" : \"Head \\u003cb\\u003esoutheast\\u003c/b\\u003e on \\u003cb\\u003eLatona Ave NE\\u003c/b\\u003e toward \\u003cb\\u003eNE 65th St\\u003c/b\\u003e\",\n                           \"polyline\" : {\n                              \"points\" : \"gv~aHjwriVXY\"\n                           },\n                           \"start_location\" : {\n                              \"lat\" : 47.676040,\n                              \"lng\" : -122.325820\n                           },\n                           \"travel_mode\" : \"WALKING\"\n                        },\n                        {\n                           \"distance\" : {\n                              \"text\" : \"0.2 mi\",\n                              \"value\" : 325\n                           },\n                           \"duration\" : {\n                              \"text\" : \"4 mins\",\n                              \"value\" : 239\n                           },\n                           \"end_location\" : {\n                              \"lat\" : 47.67580,\n                              \"lng\" : -122.321360\n                           },\n                           \"html_instructions\" : \"Turn \\u003cb\\u003eleft\\u003c/b\\u003e onto \\u003cb\\u003eNE 65th St\\u003c/b\\u003e\",\n                           \"polyline\" : {\n                              \"points\" : \"mu~aHpvriV@s@@qE@gE?w@?u@?cB@eB?{AL?\"\n                           },\n                           \"start_location\" : {\n                              \"lat\" : 47.675910,\n                              \"lng\" : -122.325690\n                           },\n                           \"travel_mode\" : \"WALKING\"\n                        }\n                     ],\n                     \"travel_mode\" : \"WALKING\"\n                  },\n                  {\n                     \"distance\" : {\n                        \"text\" : \"1.9 mi\",\n                        \"value\" : 3028\n                     },\n                     \"duration\" : {\n                        \"text\" : \"11 mins\",\n                        \"value\" : 650\n                     },\n                     \"end_location\" : {\n                        \"lat\" : 47.655050,\n                        \"lng\" : -122.312210\n                     },\n                     \"html_instructions\" : \"Bus towards Mount Baker Transit Center, University District\",\n                     \"polyline\" : {\n                        \"points\" : \"wt~aHn{qiVM??K@Y?W@eA?o@?C?Q?y@@mC?_C@iC?_B?a@@sI@{@?{A?oB@yB@yB?uB?}A?Y~@?t@?tACJ?fA?hA?p@?nHCT?T?r@?D?b@?~@?v@?V?JA`G?tDAB?J?tKFX?xGBhA?~GDfGDpAAP?^CjBCzCFpA@rEBjHFlA@h@?l@@~B?x@@?V\"\n                     },\n                     \"start_location\" : {\n                        \"lat\" : 47.67580,\n                        \"lng\" : -122.321360\n                     },\n                     \"transit_details\" : {\n                        \"arrival_stop\" : {\n                           \"location\" : {\n                              \"lat\" : 47.65504840,\n                              \"lng\" : -122.312210\n                           },\n                           \"name\" : \"15th Ave NE & NE 40th St\"\n                        },\n                        \"arrival_time\" : {\n                           \"text\" : \"11:46am\",\n                           \"time_zone\" : \"America/Los_Angeles\",\n                           \"value\" : 1368643566\n                        },\n                        \"departure_stop\" : {\n                           \"location\" : {\n                              \"lat\" : 47.67579650,\n                              \"lng\" : -122.3213580\n                           },\n                           \"name\" : \"NE 65th St & NE Ravenna Blvd\"\n                        },\n                        \"departure_time\" : {\n                           \"text\" : \"11:35am\",\n                           \"time_zone\" : \"America/Los_Angeles\",\n                           \"value\" : 1368642916\n                        },\n                        \"headsign\" : \"Mount Baker Transit Center, University District\",\n                        \"line\" : {\n                           \"agencies\" : [\n                              {\n                                 \"name\" : \"Metro Transit\",\n                                 \"phone\" : \"(206) 553-3000\",\n                                 \"url\" : \"http://metro.kingcounty.gov/\"\n                              }\n                           ],\n                           \"short_name\" : \"48\",\n                           \"url\" : \"http://metro.kingcounty.gov/tops/bus/schedules/s048_0_.html\",\n                           \"vehicle\" : {\n                              \"icon\" : \"//maps.gstatic.com/mapfiles/transit/iw/6/bus.png\",\n                              \"name\" : \"Bus\",\n                              \"type\" : \"BUS\"\n                           }\n                        },\n                        \"num_stops\" : 10\n                     },\n                     \"travel_mode\" : \"TRANSIT\"\n                  },\n                  {\n                     \"distance\" : {\n                        \"text\" : \"0.2 mi\",\n                        \"value\" : 371\n                     },\n                     \"duration\" : {\n                        \"text\" : \"4 mins\",\n                        \"value\" : 240\n                     },\n                     \"end_location\" : {\n                        \"lat\" : 47.65390000000001,\n                        \"lng\" : -122.314480\n                     },\n                     \"html_instructions\" : \"Walk to 3801 Brooklyn Avenue Northeast, University of Washington, Seattle, WA 98105, USA\",\n                     \"polyline\" : {\n                        \"points\" : \"aszaHhbpiV?Uy@AAlFApBAfA?l@`B@bCBp@@L?\"\n                     },\n                     \"start_location\" : {\n                        \"lat\" : 47.655050,\n                        \"lng\" : -122.312210\n                     },\n                     \"steps\" : [\n                        {\n                           \"distance\" : {\n                              \"text\" : \"105 ft\",\n                              \"value\" : 32\n                           },\n                           \"duration\" : {\n                              \"text\" : \"1 min\",\n                              \"value\" : 28\n                           },\n                           \"end_location\" : {\n                              \"lat\" : 47.655340,\n                              \"lng\" : -122.312090\n                           },\n                           \"html_instructions\" : \"Head \\u003cb\\u003enorth\\u003c/b\\u003e on \\u003cb\\u003e15th Ave NE\\u003c/b\\u003e toward \\u003cb\\u003eNE 40th St\\u003c/b\\u003e\",\n                           \"polyline\" : {\n                              \"points\" : \"aszaHhbpiV?Uy@A\"\n                           },\n                           \"start_location\" : {\n                              \"lat\" : 47.655050,\n                              \"lng\" : -122.312210\n                           },\n                           \"travel_mode\" : \"WALKING\"\n                        },\n                        {\n                           \"distance\" : {\n                              \"text\" : \"0.1 mi\",\n                              \"value\" : 176\n                           },\n                           \"duration\" : {\n                              \"text\" : \"2 mins\",\n                              \"value\" : 118\n                           },\n                           \"end_location\" : {\n                              \"lat\" : 47.655370,\n                              \"lng\" : -122.314440\n                           },\n                           \"html_instructions\" : \"Turn \\u003cb\\u003eleft\\u003c/b\\u003e onto \\u003cb\\u003eNE 40th St\\u003c/b\\u003e\",\n                           \"polyline\" : {\n                              \"points\" : \"{tzaHpapiVAlFApBAfA?l@\"\n                           },\n                           \"start_location\" : {\n                              \"lat\" : 47.655340,\n                              \"lng\" : -122.312090\n                           },\n                           \"travel_mode\" : \"WALKING\"\n                        },\n                        {\n                           \"distance\" : {\n                              \"text\" : \"0.1 mi\",\n                              \"value\" : 163\n                           },\n                           \"duration\" : {\n                              \"text\" : \"2 mins\",\n                              \"value\" : 94\n                           },\n                           \"end_location\" : {\n                              \"lat\" : 47.65390000000001,\n                              \"lng\" : -122.314480\n                           },\n                           \"html_instructions\" : \"Turn \\u003cb\\u003eleft\\u003c/b\\u003e onto \\u003cb\\u003eBrooklyn Ave NE\\u003c/b\\u003e\",\n                           \"polyline\" : {\n                              \"points\" : \"auzaHfppiV`B@bCBp@@L?\"\n                           },\n                           \"start_location\" : {\n                              \"lat\" : 47.655370,\n                              \"lng\" : -122.314440\n                           },\n                           \"travel_mode\" : \"WALKING\"\n                        }\n                     ],\n                     \"travel_mode\" : \"WALKING\"\n                  }\n               ],\n               \"via_waypoint\" : []\n            }\n         ],\n         \"overview_polyline\" : {\n            \"points\" : \"gv~aHjwriVXY@s@ByK@wH?{AL?M??K@q@@uB@}E@kKB{QBgM?Y~@?jCCnE?pPCzMC~LFbJBfPJbBAjCGlFH~NJdHBx@@?V?Uy@AC~IAtBdHF\"\n         },\n         \"warnings\" : [\n            \"Walking directions are in beta.    Use caution \u2013 This route may be missing sidewalks or pedestrian paths.\"\n         ],\n         \"waypoint_order\" : []\n      },\n      {\n         \"bounds\" : {\n            \"northeast\" : {\n               \"lat\" : 47.676040,\n               \"lng\" : -122.31190\n            },\n            \"southwest\" : {\n               \"lat\" : 47.65390000000001,\n               \"lng\" : -122.325820\n            }\n         },\n         \"copyrights\" : \"Map data \u00a92013 Google\",\n         \"legs\" : [\n            {\n               \"arrival_time\" : {\n                  \"text\" : \"11:58am\",\n                  \"time_zone\" : \"America/Los_Angeles\",\n                  \"value\" : 1368644314\n               },\n               \"departure_time\" : {\n                  \"text\" : \"11:31am\",\n                  \"time_zone\" : \"America/Los_Angeles\",\n                  \"value\" : 1368642703\n               },\n               \"distance\" : {\n                  \"text\" : \"2.3 mi\",\n                  \"value\" : 3769\n               },\n               \"duration\" : {\n                  \"text\" : \"27 mins\",\n                  \"value\" : 1625\n               },\n               \"end_address\" : \"3801 Brooklyn Avenue Northeast, University of Washington, Seattle, WA 98105, USA\",\n               \"end_location\" : {\n                  \"lat\" : 47.65390000000001,\n                  \"lng\" : -122.314480\n               },\n               \"start_address\" : \"6504 Latona Avenue Northeast, Seattle, WA 98115, USA\",\n               \"start_location\" : {\n                  \"lat\" : 47.676040,\n                  \"lng\" : -122.325820\n               },\n               \"steps\" : [\n                  {\n                     \"distance\" : {\n                        \"text\" : \"0.7 mi\",\n                        \"value\" : 1083\n                     },\n                     \"duration\" : {\n                        \"text\" : \"15 mins\",\n                        \"value\" : 878\n                     },\n                     \"end_location\" : {\n                        \"lat\" : 47.675470,\n                        \"lng\" : -122.312030\n                     },\n                     \"html_instructions\" : \"Walk to 15th Ave NE & NE 65th St\",\n                     \"polyline\" : {\n                        \"points\" : \"gv~aHjwriVXY@s@@qE@gE?w@?u@?cB@eB?kB?a@?G?gA?A@s@?A?kA?eC@iC@oA?]?o@BgB?qCAuA?qB@oB@y@?{@?]@{B?wB?{B@cA?o@z@?BR\"\n                     },\n                     \"start_location\" : {\n                        \"lat\" : 47.676040,\n                        \"lng\" : -122.325820\n                     },\n                     \"steps\" : [\n                        {\n                           \"distance\" : {\n                              \"text\" : \"59 ft\",\n                              \"value\" : 18\n                           },\n                           \"duration\" : {\n                              \"text\" : \"1 min\",\n                              \"value\" : 15\n                           },\n                           \"end_location\" : {\n                              \"lat\" : 47.675910,\n                              \"lng\" : -122.325690\n                           },\n                           \"html_instructions\" : \"Head \\u003cb\\u003esoutheast\\u003c/b\\u003e on \\u003cb\\u003eLatona Ave NE\\u003c/b\\u003e toward \\u003cb\\u003eNE 65th St\\u003c/b\\u003e\",\n                           \"polyline\" : {\n                              \"points\" : \"gv~aHjwriVXY\"\n                           },\n                           \"start_location\" : {\n                              \"lat\" : 47.676040,\n                              \"lng\" : -122.325820\n                           },\n                           \"travel_mode\" : \"WALKING\"\n                        },\n                        {\n                           \"distance\" : {\n                              \"text\" : \"0.6 mi\",\n                              \"value\" : 1031\n                           },\n                           \"duration\" : {\n                              \"text\" : \"14 mins\",\n                              \"value\" : 837\n                           },\n                           \"end_location\" : {\n                              \"lat\" : 47.67579000000001,\n                              \"lng\" : -122.311930\n                           },\n                           \"html_instructions\" : \"Turn \\u003cb\\u003eleft\\u003c/b\\u003e onto \\u003cb\\u003eNE 65th St\\u003c/b\\u003e\",\n                           \"polyline\" : {\n                              \"points\" : \"mu~aHpvriV@s@@qE@gE?w@?u@?cB@eB?kB?a@?G?gA?A@s@?A?kA?eC@iC@oA?]?o@BgB?qCAuA?qB@oB@y@?{@?]@{B?wB?{B@cA?o@\"\n                           },\n                           \"start_location\" : {\n                              \"lat\" : 47.675910,\n                              \"lng\" : -122.325690\n                           },\n                           \"travel_mode\" : \"WALKING\"\n                        },\n                        {\n                           \"distance\" : {\n                              \"text\" : \"112 ft\",\n                              \"value\" : 34\n                           },\n                           \"duration\" : {\n                              \"text\" : \"1 min\",\n                              \"value\" : 26\n                           },\n                           \"end_location\" : {\n                              \"lat\" : 47.675470,\n                              \"lng\" : -122.312030\n                           },\n                           \"html_instructions\" : \"Turn \\u003cb\\u003eright\\u003c/b\\u003e onto \\u003cb\\u003e15th Ave NE\\u003c/b\\u003e\",\n                           \"polyline\" : {\n                              \"points\" : \"ut~aHp`piVz@?BR\"\n                           },\n                           \"start_location\" : {\n                              \"lat\" : 47.67579000000001,\n                              \"lng\" : -122.311930\n                           },\n                           \"travel_mode\" : \"WALKING\"\n                        }\n                     ],\n                     \"travel_mode\" : \"WALKING\"\n                  },\n                  {\n                     \"distance\" : {\n                        \"text\" : \"1.5 mi\",\n                        \"value\" : 2350\n                     },\n                     \"duration\" : {\n                        \"text\" : \"9 mins\",\n                        \"value\" : 534\n                     },\n                     \"end_location\" : {\n                        \"lat\" : 47.65630,\n                        \"lng\" : -122.315460\n                     },\n                     \"html_instructions\" : \"Bus towards Downtown Seattle, University District\",\n                     \"polyline\" : {\n                        \"points\" : \"ur~aHdapiV?Qt@?tACJ?fA?hA?p@?nHCT?x@jA~AzBNPHBNDX?f@?F?dGArD?D?d@?vJDrHBxC@nEBbD@fE@zGHH?hHB`HHB?zB@AfF?tBAxAK?\"\n                     },\n                     \"start_location\" : {\n                        \"lat\" : 47.675470,\n                        \"lng\" : -122.312030\n                     },\n                     \"transit_details\" : {\n                        \"arrival_stop\" : {\n                           \"location\" : {\n                              \"lat\" : 47.65629960,\n                              \"lng\" : -122.315460\n                           },\n                           \"name\" : \"NE Campus Pkwy & 12th Ave NE\"\n                        },\n                        \"arrival_time\" : {\n                           \"text\" : \"11:55am\",\n                           \"time_zone\" : \"America/Los_Angeles\",\n                           \"value\" : 1368644100\n                        },\n                        \"departure_stop\" : {\n                           \"location\" : {\n                              \"lat\" : 47.67546840,\n                              \"lng\" : -122.3120270\n                           },\n                           \"name\" : \"15th Ave NE & NE 65th St\"\n                        },\n                        \"departure_time\" : {\n                           \"text\" : \"11:46am\",\n                           \"time_zone\" : \"America/Los_Angeles\",\n                           \"value\" : 1368643566\n                        },\n                        \"headsign\" : \"Downtown Seattle, University District\",\n                        \"line\" : {\n                           \"agencies\" : [\n                              {\n                                 \"name\" : \"Metro Transit\",\n                                 \"phone\" : \"(206) 553-3000\",\n                                 \"url\" : \"http://metro.kingcounty.gov/\"\n                              }\n                           ],\n                           \"short_name\" : \"72\",\n                           \"url\" : \"http://metro.kingcounty.gov/tops/bus/schedules/s072_0_.html\",\n                           \"vehicle\" : {\n                              \"icon\" : \"//maps.gstatic.com/mapfiles/transit/iw/6/bus.png\",\n                              \"name\" : \"Bus\",\n                              \"type\" : \"BUS\"\n                           }\n                        },\n                        \"num_stops\" : 7\n                     },\n                     \"travel_mode\" : \"TRANSIT\"\n                  },\n                  {\n                     \"distance\" : {\n                        \"text\" : \"0.2 mi\",\n                        \"value\" : 336\n                     },\n                     \"duration\" : {\n                        \"text\" : \"4 mins\",\n                        \"value\" : 213\n                     },\n                     \"end_location\" : {\n                        \"lat\" : 47.65390000000001,\n                        \"lng\" : -122.314480\n                     },\n                     \"html_instructions\" : \"Walk to 3801 Brooklyn Avenue Northeast, University of Washington, Seattle, WA 98105, USA\",\n                     \"polyline\" : {\n                        \"points\" : \"{zzaHrvpiVL?@}A?C@oBh@@v@@`@?b@@`B@bCBp@@L?\"\n                     },\n                     \"start_location\" : {\n                        \"lat\" : 47.65630,\n                        \"lng\" : -122.315460\n                     },\n                     \"steps\" : [\n                        {\n                           \"distance\" : {\n                              \"text\" : \"259 ft\",\n                              \"value\" : 79\n                           },\n                           \"duration\" : {\n                              \"text\" : \"1 min\",\n                              \"value\" : 64\n                           },\n                           \"end_location\" : {\n                              \"lat\" : 47.656210,\n                              \"lng\" : -122.314410\n                           },\n                           \"html_instructions\" : \"Head \\u003cb\\u003eeast\\u003c/b\\u003e on \\u003cb\\u003eNE Campus Pkwy\\u003c/b\\u003e toward \\u003cb\\u003eBrooklyn Ave NE\\u003c/b\\u003e\",\n                           \"polyline\" : {\n                              \"points\" : \"{zzaHrvpiVL?@}A?C@oB\"\n                           },\n                           \"start_location\" : {\n                              \"lat\" : 47.65630,\n                              \"lng\" : -122.315460\n                           },\n                           \"travel_mode\" : \"WALKING\"\n                        },\n                        {\n                           \"distance\" : {\n                              \"text\" : \"0.2 mi\",\n                              \"value\" : 257\n                           },\n                           \"duration\" : {\n                              \"text\" : \"2 mins\",\n                              \"value\" : 149\n                           },\n                           \"end_location\" : {\n                              \"lat\" : 47.65390000000001,\n                              \"lng\" : -122.314480\n                           },\n                           \"html_instructions\" : \"Turn \\u003cb\\u003eright\\u003c/b\\u003e onto \\u003cb\\u003eBrooklyn Ave NE\\u003c/b\\u003e\",\n                           \"polyline\" : {\n                              \"points\" : \"izzaH`ppiVh@@v@@`@?b@@`B@bCBp@@L?\"\n                           },\n                           \"start_location\" : {\n                              \"lat\" : 47.656210,\n                              \"lng\" : -122.314410\n                           },\n                           \"travel_mode\" : \"WALKING\"\n                        }\n                     ],\n                     \"travel_mode\" : \"WALKING\"\n                  }\n               ],\n               \"via_waypoint\" : []\n            }\n         ],\n         \"overview_polyline\" : {\n            \"points\" : \"gv~aHjwriVXY@s@ByK@wH@aJB}JBwCAgFBwI@mK@sBz@?BR?Qt@?`BCrNCT?x@jAnBlCXH`A?fMAjZJrJDbNJrHBdHHzB@AfFAnEK?L?@aB@oBh@@xA@zHHL?\"\n         },\n         \"warnings\" : [\n            \"Walking directions are in beta.    Use caution \u2013 This route may be missing sidewalks or pedestrian paths.\"\n         ],\n         \"waypoint_order\" : []\n      },\n      {\n         \"bounds\" : {\n            \"northeast\" : {\n               \"lat\" : 47.676040,\n               \"lng\" : -122.311880\n            },\n            \"southwest\" : {\n               \"lat\" : 47.65390000000001,\n               \"lng\" : -122.325820\n            }\n         },\n         \"copyrights\" : \"Map data \u00a92013 Google\",\n         \"legs\" : [\n            {\n               \"arrival_time\" : {\n                  \"text\" : \"11:35am\",\n                  \"time_zone\" : \"America/Los_Angeles\",\n                  \"value\" : 1368642906\n               },\n               \"departure_time\" : {\n                  \"text\" : \"11:16am\",\n                  \"time_zone\" : \"America/Los_Angeles\",\n                  \"value\" : 1368641761\n               },\n               \"distance\" : {\n                  \"text\" : \"2.3 mi\",\n                  \"value\" : 3742\n               },\n               \"duration\" : {\n                  \"text\" : \"19 mins\",\n                  \"value\" : 1144\n               },\n               \"end_address\" : \"3801 Brooklyn Avenue Northeast, University of Washington, Seattle, WA 98105, USA\",\n               \"end_location\" : {\n                  \"lat\" : 47.65390000000001,\n                  \"lng\" : -122.314480\n               },\n               \"start_address\" : \"6504 Latona Avenue Northeast, Seattle, WA 98115, USA\",\n               \"start_location\" : {\n                  \"lat\" : 47.676040,\n                  \"lng\" : -122.325820\n               },\n               \"steps\" : [\n                  {\n                     \"distance\" : {\n                        \"text\" : \"0.2 mi\",\n                        \"value\" : 343\n                     },\n                     \"duration\" : {\n                        \"text\" : \"4 mins\",\n                        \"value\" : 254\n                     },\n                     \"end_location\" : {\n                        \"lat\" : 47.67580,\n                        \"lng\" : -122.321360\n                     },\n                     \"html_instructions\" : \"Walk to NE 65th St & NE Ravenna Blvd\",\n                     \"polyline\" : {\n                        \"points\" : \"gv~aHjwriVXY@s@@qE@gE?w@?u@?cB@eB?{AL?\"\n                     },\n                     \"start_location\" : {\n                        \"lat\" : 47.676040,\n                        \"lng\" : -122.325820\n                     },\n                     \"steps\" : [\n                        {\n                           \"distance\" : {\n                              \"text\" : \"59 ft\",\n                              \"value\" : 18\n                           },\n                           \"duration\" : {\n                              \"text\" : \"1 min\",\n                              \"value\" : 15\n                           },\n                           \"end_location\" : {\n                              \"lat\" : 47.675910,\n                              \"lng\" : -122.325690\n                           },\n                           \"html_instructions\" : \"Head \\u003cb\\u003esoutheast\\u003c/b\\u003e on \\u003cb\\u003eLatona Ave NE\\u003c/b\\u003e toward \\u003cb\\u003eNE 65th St\\u003c/b\\u003e\",\n                           \"polyline\" : {\n                              \"points\" : \"gv~aHjwriVXY\"\n                           },\n                           \"start_location\" : {\n                              \"lat\" : 47.676040,\n                              \"lng\" : -122.325820\n                           },\n                           \"travel_mode\" : \"WALKING\"\n                        },\n                        {\n                           \"distance\" : {\n                              \"text\" : \"0.2 mi\",\n                              \"value\" : 325\n                           },\n                           \"duration\" : {\n                              \"text\" : \"4 mins\",\n                              \"value\" : 239\n                           },\n                           \"end_location\" : {\n                              \"lat\" : 47.67580,\n                              \"lng\" : -122.321360\n                           },\n                           \"html_instructions\" : \"Turn \\u003cb\\u003eleft\\u003c/b\\u003e onto \\u003cb\\u003eNE 65th St\\u003c/b\\u003e\",\n                           \"polyline\" : {\n                              \"points\" : \"mu~aHpvriV@s@@qE@gE?w@?u@?cB@eB?{AL?\"\n                           },\n                           \"start_location\" : {\n                              \"lat\" : 47.675910,\n                              \"lng\" : -122.325690\n                           },\n                           \"travel_mode\" : \"WALKING\"\n                        }\n                     ],\n                     \"travel_mode\" : \"WALKING\"\n                  },\n                  {\n                     \"distance\" : {\n                        \"text\" : \"1.9 mi\",\n                        \"value\" : 3028\n                     },\n                     \"duration\" : {\n                        \"text\" : \"11 mins\",\n                        \"value\" : 650\n                     },\n                     \"end_location\" : {\n                        \"lat\" : 47.655050,\n                        \"lng\" : -122.312210\n                     },\n                     \"html_instructions\" : \"Bus towards Mount Baker Transit Center, University District\",\n                     \"polyline\" : {\n                        \"points\" : \"wt~aHn{qiVM??K@Y?W@eA?o@?C?Q?y@@mC?_C@iC?_B?a@@sI@{@?{A?oB@yB@yB?uB?}A?Y~@?t@?tACJ?fA?hA?p@?nHCT?T?r@?D?b@?~@?v@?V?JA`G?tDAB?J?tKFX?xGBhA?~GDfGDpAAP?^CjBCzCFpA@rEBjHFlA@h@?l@@~B?x@@?V\"\n                     },\n                     \"start_location\" : {\n                        \"lat\" : 47.67580,\n                        \"lng\" : -122.321360\n                     },\n                     \"transit_details\" : {\n                        \"arrival_stop\" : {\n                           \"location\" : {\n                              \"lat\" : 47.65504840,\n                              \"lng\" : -122.312210\n                           },\n                           \"name\" : \"15th Ave NE & NE 40th St\"\n                        },\n                        \"arrival_time\" : {\n                           \"text\" : \"11:31am\",\n                           \"time_zone\" : \"America/Los_Angeles\",\n                           \"value\" : 1368642666\n                        },\n                        \"departure_stop\" : {\n                           \"location\" : {\n                              \"lat\" : 47.67579650,\n                              \"lng\" : -122.3213580\n                           },\n                           \"name\" : \"NE 65th St & NE Ravenna Blvd\"\n                        },\n                        \"departure_time\" : {\n                           \"text\" : \"11:20am\",\n                           \"time_zone\" : \"America/Los_Angeles\",\n                           \"value\" : 1368642016\n                        },\n                        \"headsign\" : \"Mount Baker Transit Center, University District\",\n                        \"line\" : {\n                           \"agencies\" : [\n                              {\n                                 \"name\" : \"Metro Transit\",\n                                 \"phone\" : \"(206) 553-3000\",\n                                 \"url\" : \"http://metro.kingcounty.gov/\"\n                              }\n                           ],\n                           \"short_name\" : \"48\",\n                           \"url\" : \"http://metro.kingcounty.gov/tops/bus/schedules/s048_0_.html\",\n                           \"vehicle\" : {\n                              \"icon\" : \"//maps.gstatic.com/mapfiles/transit/iw/6/bus.png\",\n                              \"name\" : \"Bus\",\n                              \"type\" : \"BUS\"\n                           }\n                        },\n                        \"num_stops\" : 10\n                     },\n                     \"travel_mode\" : \"TRANSIT\"\n                  },\n                  {\n                     \"distance\" : {\n                        \"text\" : \"0.2 mi\",\n                        \"value\" : 371\n                     },\n                     \"duration\" : {\n                        \"text\" : \"4 mins\",\n                        \"value\" : 240\n                     },\n                     \"end_location\" : {\n                        \"lat\" : 47.65390000000001,\n                        \"lng\" : -122.314480\n                     },\n                     \"html_instructions\" : \"Walk to 3801 Brooklyn Avenue Northeast, University of Washington, Seattle, WA 98105, USA\",\n                     \"polyline\" : {\n                        \"points\" : \"aszaHhbpiV?Uy@AAlFApBAfA?l@`B@bCBp@@L?\"\n                     },\n                     \"start_location\" : {\n                        \"lat\" : 47.655050,\n                        \"lng\" : -122.312210\n                     },\n                     \"steps\" : [\n                        {\n                           \"distance\" : {\n                              \"text\" : \"105 ft\",\n                              \"value\" : 32\n                           },\n                           \"duration\" : {\n                              \"text\" : \"1 min\",\n                              \"value\" : 28\n                           },\n                           \"end_location\" : {\n                              \"lat\" : 47.655340,\n                              \"lng\" : -122.312090\n                           },\n                           \"html_instructions\" : \"Head \\u003cb\\u003enorth\\u003c/b\\u003e on \\u003cb\\u003e15th Ave NE\\u003c/b\\u003e toward \\u003cb\\u003eNE 40th St\\u003c/b\\u003e\",\n                           \"polyline\" : {\n                              \"points\" : \"aszaHhbpiV?Uy@A\"\n                           },\n                           \"start_location\" : {\n                              \"lat\" : 47.655050,\n                              \"lng\" : -122.312210\n                           },\n                           \"travel_mode\" : \"WALKING\"\n                        },\n                        {\n                           \"distance\" : {\n                              \"text\" : \"0.1 mi\",\n                              \"value\" : 176\n                           },\n                           \"duration\" : {\n                              \"text\" : \"2 mins\",\n                              \"value\" : 118\n                           },\n                           \"end_location\" : {\n                              \"lat\" : 47.655370,\n                              \"lng\" : -122.314440\n                           },\n                           \"html_instructions\" : \"Turn \\u003cb\\u003eleft\\u003c/b\\u003e onto \\u003cb\\u003eNE 40th St\\u003c/b\\u003e\",\n                           \"polyline\" : {\n                              \"points\" : \"{tzaHpapiVAlFApBAfA?l@\"\n                           },\n                           \"start_location\" : {\n                              \"lat\" : 47.655340,\n                              \"lng\" : -122.312090\n                           },\n                           \"travel_mode\" : \"WALKING\"\n                        },\n                        {\n                           \"distance\" : {\n                              \"text\" : \"0.1 mi\",\n                              \"value\" : 163\n                           },\n                           \"duration\" : {\n                              \"text\" : \"2 mins\",\n                              \"value\" : 94\n                           },\n                           \"end_location\" : {\n                              \"lat\" : 47.65390000000001,\n                              \"lng\" : -122.314480\n                           },\n                           \"html_instructions\" : \"Turn \\u003cb\\u003eleft\\u003c/b\\u003e onto \\u003cb\\u003eBrooklyn Ave NE\\u003c/b\\u003e\",\n                           \"polyline\" : {\n                              \"points\" : \"auzaHfppiV`B@bCBp@@L?\"\n                           },\n                           \"start_location\" : {\n                              \"lat\" : 47.655370,\n                              \"lng\" : -122.314440\n                           },\n                           \"travel_mode\" : \"WALKING\"\n                        }\n                     ],\n                     \"travel_mode\" : \"WALKING\"\n                  }\n               ],\n               \"via_waypoint\" : []\n            }\n         ],\n         \"overview_polyline\" : {\n            \"points\" : \"gv~aHjwriVXY@s@ByK@wH?{AL?M??K@q@@uB@}E@kKB{QBgM?Y~@?jCCnE?pPCzMC~LFbJBfPJbBAjCGlFH~NJdHBx@@?V?Uy@AC~IAtBdHF\"\n         },\n         \"warnings\" : [\n            \"Walking directions are in beta.    Use caution \u2013 This route may be missing sidewalks or pedestrian paths.\"\n         ],\n         \"waypoint_order\" : []\n      },\n      {\n         \"bounds\" : {\n            \"northeast\" : {\n               \"lat\" : 47.676040,\n               \"lng\" : -122.314480\n            },\n            \"southwest\" : {\n               \"lat\" : 47.653480,\n               \"lng\" : -122.327540\n            }\n         },\n         \"copyrights\" : \"Map data \u00a92013 Google\",\n         \"legs\" : [\n            {\n               \"arrival_time\" : {\n                  \"text\" : \"11:40am\",\n                  \"time_zone\" : \"America/Los_Angeles\",\n                  \"value\" : 1368643223\n               },\n               \"departure_time\" : {\n                  \"text\" : \"11:13am\",\n                  \"time_zone\" : \"America/Los_Angeles\",\n                  \"value\" : 1368641585\n               },\n               \"distance\" : {\n                  \"text\" : \"2.3 mi\",\n                  \"value\" : 3757\n               },\n               \"duration\" : {\n                  \"text\" : \"14 mins\",\n                  \"value\" : 865\n               },\n               \"end_address\" : \"3801 Brooklyn Avenue Northeast, University of Washington, Seattle, WA 98105, USA\",\n               \"end_location\" : {\n                  \"lat\" : 47.65390000000001,\n                  \"lng\" : -122.314480\n               },\n               \"start_address\" : \"6504 Latona Avenue Northeast, Seattle, WA 98115, USA\",\n               \"start_location\" : {\n                  \"lat\" : 47.676040,\n                  \"lng\" : -122.325820\n               },\n               \"steps\" : [\n                  {\n                     \"distance\" : {\n                        \"text\" : \"335 ft\",\n                        \"value\" : 102\n                     },\n                     \"duration\" : {\n                        \"text\" : \"1 min\",\n                        \"value\" : 71\n                     },\n                     \"end_location\" : {\n                        \"lat\" : 47.67595000000001,\n                        \"lng\" : -122.324570\n                     },\n                     \"html_instructions\" : \"Walk to 4th Ave NE & NE 65th St\",\n                     \"polyline\" : {\n                        \"points\" : \"gv~aHjwriVXY@s@?kDI?\"\n                     },\n                     \"start_location\" : {\n                        \"lat\" : 47.676040,\n                        \"lng\" : -122.325820\n                     },\n                     \"steps\" : [\n                        {\n                           \"distance\" : {\n                              \"text\" : \"59 ft\",\n                              \"value\" : 18\n                           },\n                           \"duration\" : {\n                              \"text\" : \"1 min\",\n                              \"value\" : 15\n                           },\n                           \"end_location\" : {\n                              \"lat\" : 47.675910,\n                              \"lng\" : -122.325690\n                           },\n                           \"html_instructions\" : \"Head \\u003cb\\u003esoutheast\\u003c/b\\u003e on \\u003cb\\u003eLatona Ave NE\\u003c/b\\u003e toward \\u003cb\\u003eNE 65th St\\u003c/b\\u003e\",\n                           \"polyline\" : {\n                              \"points\" : \"gv~aHjwriVXY\"\n                           },\n                           \"start_location\" : {\n                              \"lat\" : 47.676040,\n                              \"lng\" : -122.325820\n                           },\n                           \"travel_mode\" : \"WALKING\"\n                        },\n                        {\n                           \"distance\" : {\n                              \"text\" : \"276 ft\",\n                              \"value\" : 84\n                           },\n                           \"duration\" : {\n                              \"text\" : \"1 min\",\n                              \"value\" : 56\n                           },\n                           \"end_location\" : {\n                              \"lat\" : 47.67595000000001,\n                              \"lng\" : -122.324570\n                           },\n                           \"html_instructions\" : \"Turn \\u003cb\\u003eleft\\u003c/b\\u003e onto \\u003cb\\u003eNE 65th St\\u003c/b\\u003e\",\n                           \"polyline\" : {\n                              \"points\" : \"mu~aHpvriV@s@?kDI?\"\n                           },\n                           \"start_location\" : {\n                              \"lat\" : 47.675910,\n                              \"lng\" : -122.325690\n                           },\n                           \"travel_mode\" : \"WALKING\"\n                        }\n                     ],\n                     \"travel_mode\" : \"WALKING\"\n                  },\n                  {\n                     \"distance\" : {\n                        \"text\" : \"1.5 mi\",\n                        \"value\" : 2432\n                     },\n                     \"duration\" : {\n                        \"text\" : \"7 mins\",\n                        \"value\" : 422\n                     },\n                     \"end_location\" : {\n                        \"lat\" : 47.655610,\n                        \"lng\" : -122.326920\n                     },\n                     \"html_instructions\" : \"Bus towards Downtown Seattle, Fremont\",\n                     \"polyline\" : {\n                        \"points\" : \"uu~aHporiVJ?AnDvC@j@?lB?xC@vCBl@?dA@lCBd@@hB@rC?d@?lB@pCBt@?|A?nC@d@?hB@pC?j@@bB@?fBp@?xHD^?jIBf@?xDB|FBn@@hE??pBv@?vJB?RM?\"\n                     },\n                     \"start_location\" : {\n                        \"lat\" : 47.67595000000001,\n                        \"lng\" : -122.324570\n                     },\n                     \"transit_details\" : {\n                        \"arrival_stop\" : {\n                           \"location\" : {\n                              \"lat\" : 47.65560530,\n                              \"lng\" : -122.326920\n                           },\n                           \"name\" : \"2nd Ave NE & NE 40th St\"\n                        },\n                        \"arrival_time\" : {\n                           \"text\" : \"11:21am\",\n                           \"time_zone\" : \"America/Los_Angeles\",\n                           \"value\" : 1368642066\n                        },\n                        \"departure_stop\" : {\n                           \"location\" : {\n                              \"lat\" : 47.67594910,\n                              \"lng\" : -122.324570\n                           },\n                           \"name\" : \"4th Ave NE & NE 65th St\"\n                        },\n                        \"departure_time\" : {\n                           \"text\" : \"11:14am\",\n                           \"time_zone\" : \"America/Los_Angeles\",\n                           \"value\" : 1368641644\n                        },\n                        \"headsign\" : \"Downtown Seattle, Fremont\",\n                        \"line\" : {\n                           \"agencies\" : [\n                              {\n                                 \"name\" : \"Metro Transit\",\n                                 \"phone\" : \"(206) 553-3000\",\n                                 \"url\" : \"http://metro.kingcounty.gov/\"\n                              }\n                           ],\n                           \"short_name\" : \"26\",\n                           \"url\" : \"http://metro.kingcounty.gov/tops/bus/schedules/s026_0_.html\",\n                           \"vehicle\" : {\n                              \"icon\" : \"//maps.gstatic.com/mapfiles/transit/iw/6/bus.png\",\n                              \"name\" : \"Bus\",\n                              \"type\" : \"BUS\"\n                           }\n                        },\n                        \"num_stops\" : 13\n                     },\n                     \"travel_mode\" : \"TRANSIT\"\n                  },\n                  {\n                     \"distance\" : {\n                        \"text\" : \"157 ft\",\n                        \"value\" : 48\n                     },\n                     \"duration\" : {\n                        \"text\" : \"1 min\",\n                        \"value\" : 48\n                     },\n                     \"end_location\" : {\n                        \"lat\" : 47.655480,\n                        \"lng\" : -122.327540\n                     },\n                     \"html_instructions\" : \"Walk to NE 40th St & 1st Ave NE\",\n                     \"polyline\" : {\n                        \"points\" : \"qvzaHf~riVXzB\"\n                     },\n                     \"start_location\" : {\n                        \"lat\" : 47.655610,\n                        \"lng\" : -122.326920\n                     },\n                     \"steps\" : [\n                        {\n                           \"distance\" : {\n                              \"text\" : \"157 ft\",\n                              \"value\" : 48\n                           },\n                           \"duration\" : {\n                              \"text\" : \"1 min\",\n                              \"value\" : 48\n                           },\n                           \"end_location\" : {\n                              \"lat\" : 47.655480,\n                              \"lng\" : -122.327540\n                           },\n                           \"polyline\" : {\n                              \"points\" : \"qvzaHf~riVXzB\"\n                           },\n                           \"start_location\" : {\n                              \"lat\" : 47.655610,\n                              \"lng\" : -122.326920\n                           },\n                           \"travel_mode\" : \"WALKING\"\n                        }\n                     ],\n                     \"travel_mode\" : \"WALKING\"\n                  },\n                  {\n                     \"distance\" : {\n                        \"text\" : \"0.7 mi\",\n                        \"value\" : 1074\n                     },\n                     \"duration\" : {\n                        \"text\" : \"4 mins\",\n                        \"value\" : 227\n                     },\n                     \"end_location\" : {\n                        \"lat\" : 47.653480,\n                        \"lng\" : -122.315320\n                     },\n                     \"html_instructions\" : \"Bus towards University District, Fremont\",\n                     \"polyline\" : {\n                        \"points\" : \"wuzaHbbsiVM?@oC?cC?}A@_F@qABsA?oACmC^?pCBV??I?M?Q@U?aA?aF?WCk@Ce@CgA?c@PeANs@BMJa@V_A^y@\\\\s@Pc@ZmABODk@B_A?I?O@a@@}FN?\"\n                     },\n                     \"start_location\" : {\n                        \"lat\" : 47.655480,\n                        \"lng\" : -122.327540\n                     },\n                     \"transit_details\" : {\n                        \"arrival_stop\" : {\n                           \"location\" : {\n                              \"lat\" : 47.65348430,\n                              \"lng\" : -122.3153230\n                           },\n                           \"name\" : \"NE Pacific St & Brooklyn Ave NE\"\n                        },\n                        \"arrival_time\" : {\n                           \"text\" : \"11:38am\",\n                           \"time_zone\" : \"America/Los_Angeles\",\n                           \"value\" : 1368643126\n                        },\n                        \"departure_stop\" : {\n                           \"location\" : {\n                              \"lat\" : 47.65547940,\n                              \"lng\" : -122.3275380\n                           },\n                           \"name\" : \"NE 40th St & 1st Ave NE\"\n                        },\n                        \"departure_time\" : {\n                           \"text\" : \"11:34am\",\n                           \"time_zone\" : \"America/Los_Angeles\",\n                           \"value\" : 1368642899\n                        },\n                        \"headsign\" : \"University District, Fremont\",\n                        \"line\" : {\n                           \"agencies\" : [\n                              {\n                                 \"name\" : \"Metro Transit\",\n                                 \"phone\" : \"(206) 553-3000\",\n                                 \"url\" : \"http://metro.kingcounty.gov/\"\n                              }\n                           ],\n                           \"short_name\" : \"32\",\n                           \"url\" : \"http://metro.kingcounty.gov/tops/bus/schedules/s032_0_.html\",\n                           \"vehicle\" : {\n                              \"icon\" : \"//maps.gstatic.com/mapfiles/transit/iw/6/bus.png\",\n                              \"name\" : \"Bus\",\n                              \"type\" : \"BUS\"\n                           }\n                        },\n                        \"num_stops\" : 2\n                     },\n                     \"travel_mode\" : \"TRANSIT\"\n                  },\n                  {\n                     \"distance\" : {\n                        \"text\" : \"331 ft\",\n                        \"value\" : 101\n                     },\n                     \"duration\" : {\n                        \"text\" : \"2 mins\",\n                        \"value\" : 97\n                     },\n                     \"end_location\" : {\n                        \"lat\" : 47.65390000000001,\n                        \"lng\" : -122.314480\n                     },\n                     \"html_instructions\" : \"Walk to 3801 Brooklyn Avenue Northeast, University of Washington, Seattle, WA 98105, USA\",\n                     \"polyline\" : {\n                        \"points\" : \"gizaHvupiVOA@cDQAo@?C?\"\n                     },\n                     \"start_location\" : {\n                        \"lat\" : 47.653480,\n                        \"lng\" : -122.315320\n                     },\n                     \"steps\" : [\n                        {\n                           \"distance\" : {\n                              \"text\" : \"203 ft\",\n                              \"value\" : 62\n                           },\n                           \"duration\" : {\n                              \"text\" : \"1 min\",\n                              \"value\" : 49\n                           },\n                           \"end_location\" : {\n                              \"lat\" : 47.653550,\n                              \"lng\" : -122.314490\n                           },\n                           \"html_instructions\" : \"Head \\u003cb\\u003eeast\\u003c/b\\u003e on \\u003cb\\u003eNE Pacific St\\u003c/b\\u003e toward \\u003cb\\u003eBrooklyn Ave NE\\u003c/b\\u003e\",\n                           \"polyline\" : {\n                              \"points\" : \"gizaHvupiVOA@cD\"\n                           },\n                           \"start_location\" : {\n                              \"lat\" : 47.653480,\n                              \"lng\" : -122.315320\n                           },\n                           \"travel_mode\" : \"WALKING\"\n                        },\n                        {\n                           \"distance\" : {\n                              \"text\" : \"128 ft\",\n                              \"value\" : 39\n                           },\n                           \"duration\" : {\n                              \"text\" : \"1 min\",\n                              \"value\" : 48\n                           },\n                           \"end_location\" : {\n                              \"lat\" : 47.65390000000001,\n                              \"lng\" : -122.314480\n                           },\n                           \"html_instructions\" : \"Turn \\u003cb\\u003eleft\\u003c/b\\u003e onto \\u003cb\\u003eBrooklyn Ave NE\\u003c/b\\u003e\",\n                           \"polyline\" : {\n                              \"points\" : \"uizaHpppiVQAo@?C?\"\n                           },\n                           \"start_location\" : {\n                              \"lat\" : 47.653550,\n                              \"lng\" : -122.314490\n                           },\n                           \"travel_mode\" : \"WALKING\"\n                        }\n                     ],\n                     \"travel_mode\" : \"WALKING\"\n                  }\n               ],\n               \"via_waypoint\" : []\n            }\n         ],\n         \"overview_polyline\" : {\n            \"points\" : \"gv~aHjwriVXY@s@?kDI?J?AnDbE@lMDbJHxD?~FDhI@jKD?fBp@?xIDjXJxF@?pBv@?vJB?RM?XzBM?@oC?aFBqHBcDCmC^?hDB@_A?cHCcAGmB?c@PeARaAb@aB|@mBPc@ZmAH{@BiA@q@@}FN?OA@cDaAAC?\"\n         },\n         \"warnings\" : [\n            \"Walking directions are in beta.    Use caution \u2013 This route may be missing sidewalks or pedestrian paths.\"\n         ],\n         \"waypoint_order\" : []\n      }\n   ],\n   \"status\" : \"OK\"\n}";
 }
