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
 
 import com.HuskySoft.metrobike.algorithm.AlgorithmWorker;
 import com.HuskySoft.metrobike.algorithm.BicycleOnlyAlgorithm;
 import com.HuskySoft.metrobike.algorithm.SimpleComboAlgorithm;
 
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
      * This determines whether or not to use the StubGoogleAPIWrapper.
      */
     private APIQuery resource = null;
     
     /**
      * Constructs a new DirectionsRequest object.
      */
     public DirectionsRequest() {
         myParams = new RequestParameters();
         solutions = null;
         errorMessages = null;
     }
 
     /**
      * Getter for the resource field.
      * 
      * @return Returns the current APIQuery Object.
      */
     public APIQuery getResource() {
         return resource;
     }
     
     
     /**
      * Initiates the request calculation. This is a blocking call. NOTE: This
      * method is currently under heavy testing and does not currently meet style
      * guidelines.
      * 
      * @return the final status of the doRequest process
      */
     public DirectionsStatus doDummyRequest() {
 
         // First, validate our parameters!
         if (!myParams.validateParameters()) {
             return DirectionsStatus.INVALID_REQUEST_PARAMS;
         }
 
         System.err.println(TAG + "RUNNING ON DUMMY JSON RESPONSE");
 
         solutions = new ArrayList<Route>();
 
         JSONObject myJSON;
         try {
             myJSON = new JSONObject(DUMMY_BICYCLE_JSON);
             // Commenting this out until we have this part completely working.
             // myJSON = new JSONObject(jsonResponse);
             if (!myJSON.getString(WebRequestJSONKeys.STATUS.getLowerCase())
                     .equalsIgnoreCase(
                             GoogleMapsResponseStatusCodes.OK.toString())) {
                 System.err.println(TAG
                         + "JSON Response returned: "
                         + myJSON.getString(WebRequestJSONKeys.STATUS
                                 .getLowerCase()));
             }
         } catch (JSONException e) {
             appendErrorMessage("Error parsing JSON.");
             return DirectionsStatus.PARSING_ERROR;
         }
 
         JSONArray routesArray;
 
         try {
             routesArray = myJSON.getJSONArray(WebRequestJSONKeys.ROUTES
                     .getLowerCase());
             for (int i = 0; i < routesArray.length(); i++) {
                 Route currentLeg = Route.buildRouteFromJSON(routesArray
                         .getJSONObject(i));
                 solutions.add(currentLeg);
             }
             System.err.println("JSON_TEST" + "Processed "
                     + routesArray.length() + " routes!");
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
         switch (myParams.getTravelMode()) {
         case BICYCLING:
             return doAlgorithm(new BicycleOnlyAlgorithm());
         case TRANSIT:
             return doAlgorithm(new BicycleOnlyAlgorithm());
         case MIXED:
             // Travel Mode Mix also needs the bicycle only routes
             DirectionsStatus comboStatus = doAlgorithm(new SimpleComboAlgorithm());
 
             DirectionsStatus bikeStatus = doAlgorithm(new BicycleOnlyAlgorithm());
             if (comboStatus.isError() && bikeStatus.isError()) {
                 return DirectionsStatus.NO_RESULTS_FOUND;
             }
             return DirectionsStatus.REQUEST_SUCCESSFUL;
         default:
             System.err.println("Incorrect Travel Mode here: "
                     + myParams.getTravelMode().toString());
             return DirectionsStatus.UNSUPPORTED_TRAVEL_MODE_ERROR;
         }
 
     }
 
     /**
      * Run the given algorithm and add all the routes.
      * 
      * @param algorithmWorker
      *            : the abstracted algorithm class
      * @return direction status after the algorithm completes
      */
     private DirectionsStatus doAlgorithm(final AlgorithmWorker algorithmWorker) {
         // DirectionsStatus firstStatus = firstAlg.findRoutes(myParams);
         DirectionsStatus status = algorithmWorker.findRoutes(myParams);
 
         /*
          * if(firstStatus.isError()) { extendedErrors = firstAlg.getErrors();
          * return firstStatus; }
          */
         if (status.isError()) {
             appendErrorMessage(algorithmWorker.getErrors());
             return status;
         }
 
         // List<Route> firstRoutes = firstAlg.getResults();
         List<Route> routes = algorithmWorker.getResults();
 
         /*
          * if (firstRoutes == null) { System.err.println(TAG +
          * "Got null from SimpleAlgorithm without an error");
          * appendErrorMessage(DirectionsStatus.NO_RESULTS_FOUND.getMessage());
          * return DirectionsStatus.NO_RESULTS_FOUND; }
          */
         if (routes == null) {
             System.err.println(TAG
                     + "Got null from SimpleComboAlgorithm without an error");
             appendErrorMessage(DirectionsStatus.NO_RESULTS_FOUND.getMessage());
             return DirectionsStatus.NO_RESULTS_FOUND;
         }
         solutions.addAll(routes);
 
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
         private static final long serialVersionUID = 1L;
 
         /**
          * Represents a number-based option that hasn't been specified by the
          * user.
          */
         public static final long DONT_CARE = -1;
 
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
         private long arrivalTime = DONT_CARE;
 
         /**
          * Time the user would like to depart from their starting location. If
          * this is set (ie. it equals a nonzero value), then arrival time should
          * not be set.
          */
         private long departureTime = DONT_CARE;
 
         /**
          * The travel mode the user is requesting directions for. Default is
          * "MIXED."
          */
         private TravelMode travelMode = TravelMode.MIXED;
 
         /**
          * An optional field. It sets a minimum distance the user would like to
          * bike.
          */
         private long minDistanceToBikeInMeters = DONT_CARE;
 
         /**
          * An optional field. It sets the maximum distance the user would like
          * to bike. (In case they are exercise averse)
          */
         private long maxDistanceToBikeInMeters = DONT_CARE;
 
         /**
          * An optional field. The minimum number of bus transfers to have. In
          * case they like multiple transfers.
          */
         private long minNumberBusTransfers = DONT_CARE;
 
         /**
          * An optional field. Setting the maximum number of bus transfers in
          * case the user doesn't want routes with lots of transfers.
          */
         private long maxNumberBusTransfers = DONT_CARE;
 
         /**
          * This determines whether or not to use the StubGoogleAPIWrapper.
          */
         private APIQuery resource = null;
         
         /**
          * Getter for the resource field.
          * 
          * @return Returns the current APIQuery Object.
          */
         public APIQuery getResource() {
             return resource;
         }
         
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
         public long getMinNumberBusTransfers() {
             return minNumberBusTransfers;
         }
 
         /**
          * @return the maxNumberBusTransfers
          */
         public long getMaxNumberBusTransfers() {
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
                 // TODO: Be sure no bus transfer options are set
                 break;
             case TRANSIT:
             case MIXED:
                 // Check to be sure we got a time!
                 if (departureTime == DONT_CARE && arrivalTime == DONT_CARE) {
                     appendErrorMessage("Directions for transit must include"
                             + " a departure or arrival time.");
                     return false;
                 }
                 break;
             default:
                 // Including walking since the Algorithm switch statements
                 // don't check for walking.
                 appendErrorMessage("Unsupported desired travel mode "
                         + travelMode);
                 return false;
             }
 
             // Not checking if the departure time is valid (if set)
             // as google lets you set times in the past.
 
             // Validate optional parameters
             if ((minDistanceToBikeInMeters != DONT_CARE && minDistanceToBikeInMeters < 0)
                     || (maxDistanceToBikeInMeters != DONT_CARE && maxDistanceToBikeInMeters < 0)
                     || (minNumberBusTransfers != DONT_CARE && minNumberBusTransfers < 0)
                     || (maxNumberBusTransfers != DONT_CARE && maxNumberBusTransfers < 0)) {
                 appendErrorMessage("All optional parameters (biking distance and bus transfers)"
                         + " must be greater than or equal to zero");
                 return false;
             }
 
             if (minDistanceToBikeInMeters != DONT_CARE &&
                     maxDistanceToBikeInMeters != DONT_CARE &&
                     minDistanceToBikeInMeters > maxDistanceToBikeInMeters) {
                 appendErrorMessage("Min > Max for distance to bike.");
                 return false;
             }
 
             if (minNumberBusTransfers != DONT_CARE &&
                     maxNumberBusTransfers != DONT_CARE &&
                     minNumberBusTransfers > maxNumberBusTransfers) {
                 appendErrorMessage("Min > Max for number of transfers");
                 return false;
             }
 
             // Printing out the parameters for debug purposes
             System.out.println(RP_TAG + "StartAddress: " + startAddress);
             System.out.println(RP_TAG + "EndAddress: " + endAddress);
             System.out.println(RP_TAG + "ArrivalTime: " + arrivalTime);
             System.out.println(RP_TAG + "DepartureTime: " + departureTime);
             System.out.println(RP_TAG + "TravelMode: " + travelMode);
             System.out.println(RP_TAG + "MinDistanceToBikeInMeters: "
                     + minDistanceToBikeInMeters);
             System.out.println(RP_TAG + "MaxDistanceToBikeInMeters: "
                     + maxDistanceToBikeInMeters);
             System.out.println(RP_TAG + "MinNumberBusTransfers: "
                     + minNumberBusTransfers);
             System.out.println(RP_TAG + "MaxNumberBusTransfers: "
                     + maxNumberBusTransfers);
 
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
         private void writeObject(final ObjectOutputStream out)
                 throws IOException {
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
             out.writeLong(minNumberBusTransfers);
             out.writeLong(maxNumberBusTransfers);
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
             minNumberBusTransfers = in.readLong();
             maxNumberBusTransfers = in.readLong();
         }
 
         @Override
         public String toString() {
             return "RequestParameters:" + "\nstartAddress: " + startAddress
                     + "\nendAddress: " + endAddress + "\narrivalTime: "
                     + arrivalTime + "\ndepartureTime: " + departureTime
                     + "\ntravelMode: " + travelMode.toString()
                     + "\nminDistanceToBikeInMeters: "
                     + minDistanceToBikeInMeters
                     + "\nmaxDistanceToBikeInMeters: "
                     + maxDistanceToBikeInMeters + "\nminNumberBusTransfers: "
                     + minNumberBusTransfers + "\nmaxNumberBusTransfers: "
                     + maxNumberBusTransfers + "\n";
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
        if (myParams.departureTime != RequestParameters.DONT_CARE) {
             throw new IllegalArgumentException("departureTime was already "
                     + "set.");
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
        if (myParams.arrivalTime != RequestParameters.DONT_CARE) {
            throw new IllegalArgumentException("arrivalTime was "
                     + "already set.");
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
     public DirectionsRequest setMinDistanceToBikeInMeters(
             final long newMinDistanceToBikeInMeters) {
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
     public DirectionsRequest setMaxDistanceToBikeInMeters(
             final long newMaxDistanceToBikeInMeters) {
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
     public DirectionsRequest setMinNumberBusTransfers(
             final int newMinNumberBusTransfers) {
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
     public DirectionsRequest setMaxNumberBusTransfers(
             final int newMaxNumberBusTransfers) {
         myParams.maxNumberBusTransfers = newMaxNumberBusTransfers;
         return this;
     }
 
     /**
      * Returns the verbose error messages. This may contain repeated error
      * messages, and so may not be suitable for displaying to users. To display
      * user-friendly error messages, call .getMessage() on the return value of
      * doRequest().
      * 
      * @return the verbose errorMessages. Returns null if no messages have been
      *         set.
      */
     public String getVerboseErrorMessages() {
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
     private void readObject(final ObjectInputStream in) throws IOException,
             ClassNotFoundException {
         // Read each field from the stream in a specific order.
         // Specifying this order helps shield the class from problems
         // in future versions.
         // The order must be the same as the writing order in writeObject()
         myParams = (RequestParameters) in.readObject();
         solutions = (List<Route>) in.readObject();
     }
 
     /**
      * Dummy JSON bicycle directions.
      */
     private static final String DUMMY_BICYCLE_JSON = "{"
             + "\n   \"routes\" : [\n    "
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
 
 }
