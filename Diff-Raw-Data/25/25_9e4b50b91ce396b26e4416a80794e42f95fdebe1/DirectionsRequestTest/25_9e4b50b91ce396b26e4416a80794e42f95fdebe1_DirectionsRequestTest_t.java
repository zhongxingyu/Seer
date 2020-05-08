 package com.HuskySoft.metrobike.backend.test;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.List;
 
 import junit.framework.Assert;
 import junit.framework.TestCase;
 
 import com.HuskySoft.metrobike.backend.DirectionsRequest;
 import com.HuskySoft.metrobike.backend.DirectionsRequest.RequestParameters;
 import com.HuskySoft.metrobike.backend.DirectionsStatus;
 import com.HuskySoft.metrobike.backend.Route;
 import com.HuskySoft.metrobike.backend.StubGoogleAPIWrapper;
 import com.HuskySoft.metrobike.backend.TravelMode;
 
 /**
  * This class tests the DirectionsRequest class.
  * 
  * @author coreyh3 check style: Sam Wilson
  */
 public final class DirectionsRequestTest extends TestCase {
 
     /**
      * Max distance bike in meter.
      */
     private static final int BAD_MAX_DIST_BIKE_METER = 400;
 
     /**
      * Min distance bike in meter.
      */
     private static final int BAD_MIN_DIST_BIKE_METER = 500;
 
     /**
      * Negative bus transfer value.
      */
     private static final int NEGATIVE_MIN_BUS_TRANSFERS = -2;
 
     /**
      * Max bus transfer.
      */
     private static final int MAX_BUS_TRANSFER_1 = 5;
 
     /**
      * The max distance to bike in meters.
      */
     private static final int MAX_DIST_IN_METER = 2000;
 
     /**
      * The min distance to bike in meters.
      */
     private static final int MIN_DIST_IN_METER = 1000;
 
     /**
      * The arrival time in ms.
      */
     private static final int ARRIVAL_TIME = 4000000;
 
     /**
      * The departure time.
      */
     private static final int DEPARTURE_TIME = 100;
     
     /**
      * Time to wait after certain Google API-using tests
      */
     private static final long TIME_TO_WAIT_AFTER_TEST_MS = 2000; 
 
     /**
      * An output constant.
      */
     private static final String ACTUAL_STATUS_WAS_LABEL = "Actual status for request.doRequest() call was: ";
 
     /**
      * This holds a directionsRequest object for use by other testing methods.
      */
     private DirectionsRequest request = null;
 
     /**
      * A private variable for getting results back from a testing thread. See
      * the testCancelRunningQuery() test.
      */
     private volatile DirectionsStatus threadedResult;
 
     /**
      * A thread class for running doRequest() in a separate thread. Used for
      * testing the cancel feature.
      * 
      * @author dutchscout
      */
     private class RequestRunner implements Runnable {
 
         @Override
         public void run() {
             setUp();
             Thread.yield();
             threadedResult = request.doRequest();
             waitBetweenLiveTests();
         }
     }
 
     /**
      * This sets up the test class to use a new directions request object in
      * future tests.
      * 
      * @throws Exception
      */
     // @Before
     public void setUp() {
         request = new DirectionsRequest();
 
         String startAddress = "6504 Latona Ave NE,Seattle,WA";
         String endAddress = "3801 Brooklyn Ave NE,Seattle,WA";
         request.setStartAddress(startAddress);
         request.setEndAddress(endAddress);
         request.setArrivalTime(ARRIVAL_TIME);
         request.setTravelMode(TravelMode.MIXED);
         request.setMinDistanceToBikeInMeters(MIN_DIST_IN_METER);
         request.setMaxDistanceToBikeInMeters(MAX_DIST_IN_METER);
         request.setMinNumberBusTransfers(RequestParameters.DONT_CARE);
         request.setMaxNumberBusTransfers(RequestParameters.DONT_CARE);
     }
 
     /**
      * WhiteBox: This tests the toString method.
      */
     // @Test
     public void testToStringTest() {
         setUp();
         String expected = "DirectionsRequest: RequestParameters:\nstartAddress: 6504 "
                 + "Latona Ave NE,Seattle,WA\n" + "endAddress: 3801 Brooklyn Ave NE,Seattle,WA\n"
                 + "arrivalTime: 4000000\n" + "departureTime: " + RequestParameters.DONT_CARE_STRING
                 + "\n" + "travelMode: MIXED\n" + "minDistanceToBikeInMeters: 1000\n"
                 + "maxDistanceToBikeInMeters: 2000\n" + "minNumberBusTransfers: "
                 + RequestParameters.DONT_CARE_STRING + "\n" + "maxNumberBusTransfers: "
                 + RequestParameters.DONT_CARE_STRING + "\n" + "solutions: null";
 
         String actual = request.toString();
         Assert.assertEquals("Problem comparing toString()", expected, actual);
     }
 
     /**
      * BlackBox: This tests the doDummyRequest method in the success case.
      */
     // @Test
     public void testDoDummyRequest1Test() {
 
         request = new DirectionsRequest();
 
         String startAddress = "University of Washington";
         String endAddress = "3801 Brooklyn Ave NE,Seattle,WA";
         request.setStartAddress(startAddress);
         request.setEndAddress(endAddress);
         request.setDepartureTime(ARRIVAL_TIME);
         request.setTravelMode(TravelMode.MIXED);
         request.setMinDistanceToBikeInMeters(MIN_DIST_IN_METER);
         request.setMaxDistanceToBikeInMeters(MAX_DIST_IN_METER);
 
         DirectionsStatus expected = DirectionsStatus.REQUEST_SUCCESSFUL;
 
         DirectionsStatus actual = request.doDummyRequest();
         Assert.assertEquals(ACTUAL_STATUS_WAS_LABEL + actual.getMessage(), expected, actual);
     }
 
     /**
      * BlackBox: This tests the doDummyRequest method in the success case.
      */
     // @Test
     public void testDoDummyRequest2Test() {
         request = new DirectionsRequest();
 
         String startAddress = "The Space Needle";
         String endAddress = "University of Washington, Seattle";
         request.setStartAddress(startAddress);
         request.setEndAddress(endAddress);
         request.setDepartureTime(ARRIVAL_TIME);
         request.setTravelMode(TravelMode.MIXED);
         request.setMinNumberBusTransfers(0);
         request.setMaxNumberBusTransfers(MAX_BUS_TRANSFER_1);
 
         DirectionsStatus expected = DirectionsStatus.REQUEST_SUCCESSFUL;
 
         DirectionsStatus actual = request.doDummyRequest();
         Assert.assertEquals(ACTUAL_STATUS_WAS_LABEL + actual.getMessage(), expected, actual);
     }
 
     /**
      * BlackBox: This tests the doDummyRequest method in the success case.
      */
     // @Test
     public void testDoDummyRequest3Test() {
 
         request = new DirectionsRequest();
 
         String startAddress = "Jaylens place";
         String endAddress = "Corey's place";
         request.setStartAddress(startAddress);
         request.setEndAddress(endAddress);
         request.setArrivalTime(ARRIVAL_TIME);
         request.setTravelMode(TravelMode.MIXED);
         request.setMinDistanceToBikeInMeters(MIN_DIST_IN_METER);
         request.setMaxDistanceToBikeInMeters(MAX_DIST_IN_METER);
         request.setMinNumberBusTransfers(0);
         request.setMaxNumberBusTransfers(0);
         DirectionsStatus expected = DirectionsStatus.REQUEST_SUCCESSFUL;
 
         DirectionsStatus actual = request.doDummyRequest();
         Assert.assertEquals(ACTUAL_STATUS_WAS_LABEL + actual.getMessage(), expected, actual);
     }
 
     /**
      * BlackBox: This tests the doRequest method in the success case.
      * 
      * @Warning I don't believe we can have more than one test in the test suite
      *          that makes requests to the Google Maps API or else they reject
      *          us causing the test to fail.
      */
     // @Test
     public void testDoRequestTest() {
         setUp();
         DirectionsStatus expected = DirectionsStatus.REQUEST_SUCCESSFUL;
 
         request.setArrivalTime(RequestParameters.DONT_CARE);
         request.setDepartureTime(ARRIVAL_TIME);
 
         DirectionsStatus actual = request.doRequest();
         waitBetweenLiveTests();
         
         Assert.assertEquals(ACTUAL_STATUS_WAS_LABEL + actual.getMessage() + " with errors ["
                 + request.getVerboseErrorMessages() + "]", expected, actual);
     }
     
     /**
      * Waits between live Google API tests to avoid overuse (and failure).
      */
     private void waitBetweenLiveTests(){
         try {
             Thread.sleep(TIME_TO_WAIT_AFTER_TEST_MS);
         } catch (InterruptedException e) {
             // do nothing if interrupted
         }
     }
 
     /**
      * BlackBox: This tests the doRequest method in the case where the distance
      * is too large to be returned.
      */
     // @Test
    // This test commented-out because it takes ~10 minutes to run.
    /*
     public void testDoLargeRequestTest() {
 
         request = new DirectionsRequest();
 
         String startAddress = "6504 Latona Ave NE, Seattle, WA";
         String endAddress = "Central Park, New York City, NY";
         request.setStartAddress(startAddress);
         request.setEndAddress(endAddress);
         request.setArrivalTime(ARRIVAL_TIME);
         request.setTravelMode(TravelMode.MIXED);
         request.setMinDistanceToBikeInMeters(RequestParameters.DONT_CARE);
         request.setMaxDistanceToBikeInMeters(RequestParameters.DONT_CARE);
         request.setMinNumberBusTransfers(RequestParameters.DONT_CARE);
         request.setMaxNumberBusTransfers(RequestParameters.DONT_CARE);
         
         DirectionsStatus expected = DirectionsStatus.NO_RESULTS_FOUND;
 
         DirectionsStatus actual = request.doRequest();
         waitBetweenLiveTests();
         
         Assert.assertEquals(ACTUAL_STATUS_WAS_LABEL + actual.getMessage() + " with errors ["
                 + request.getVerboseErrorMessages() + "]", expected, actual);
    }*/
 
     /**
      * WhiteBox: This tests the doRequest method with invalid params.
      */
     // @Test
     public void testDoRequestInvalidParamsTest() {
         setUp();
         DirectionsStatus expected = DirectionsStatus.INVALID_REQUEST_PARAMS;
         request.setStartAddress(null);
 
         DirectionsStatus actual = request.doRequest();
         Assert.assertEquals(ACTUAL_STATUS_WAS_LABEL + actual.getMessage(), expected, actual);
     }
 
     /**
      * WhiteBox: This tests the doDummyRequest with the arrival value set.
      */
     // @Test
     public void testDoDummyRequestArrivalSetTest() {
         setUp();
         DirectionsStatus expected = DirectionsStatus.REQUEST_SUCCESSFUL;
 
         DirectionsStatus actual = request.doDummyRequest();
         Assert.assertEquals(ACTUAL_STATUS_WAS_LABEL + actual.getMessage(), expected, actual);
     }
 
     /**
      * WhiteBox: This tests the doDummyRequest with the departure value set.
      */
     // @Test
     public void testDoDummyRequestDepartureSetTest() {
         setUp();
         DirectionsStatus expected = DirectionsStatus.REQUEST_SUCCESSFUL;
         request.setArrivalTime(RequestParameters.DONT_CARE);
         request.setDepartureTime(ARRIVAL_TIME);
 
         DirectionsStatus actual = request.doDummyRequest();
         Assert.assertEquals(ACTUAL_STATUS_WAS_LABEL + actual.getMessage(), expected, actual);
     }
 
     /**
      * WhiteBox: This tests the doDummyRequest with the start address set to
      * null.
      */
     // @Test
     public void testDoDummyRequestStartAddressNullTest() {
         setUp();
         DirectionsStatus expected = DirectionsStatus.INVALID_REQUEST_PARAMS;
         request.setStartAddress(null);
         DirectionsStatus actual = request.doDummyRequest();
         Assert.assertEquals(ACTUAL_STATUS_WAS_LABEL + actual.getMessage(), expected, actual);
     }
 
     /**
      * WhiteBox: This tests the doDummyRequest with the start address empty
      * ("").
      */
     // @Test
     public void testDoDummyRequestStartAddressEmptyTest() {
         setUp();
         DirectionsStatus expected = DirectionsStatus.INVALID_REQUEST_PARAMS;
         request.setStartAddress("");
         DirectionsStatus actual = request.doDummyRequest();
         Assert.assertEquals(ACTUAL_STATUS_WAS_LABEL + actual.getMessage(), expected, actual);
     }
 
     /**
      * WhiteBox: This tests the doDummyRequest with the endAddress null.
      */
     // @Test
     public void testDoDummyRequestEndAddressNullTest() {
         setUp();
         DirectionsStatus expected = DirectionsStatus.INVALID_REQUEST_PARAMS;
         request.setEndAddress(null);
         DirectionsStatus actual = request.doDummyRequest();
         Assert.assertEquals(ACTUAL_STATUS_WAS_LABEL + actual.getMessage(), expected, actual);
     }
 
     /**
      * WhiteBox: This tests the doDummyRequest with an empty end address.
      */
     // @Test
     public void testDoDummyRequestEndAddressEmptyTest() {
         setUp();
         DirectionsStatus expected = DirectionsStatus.INVALID_REQUEST_PARAMS;
         request.setEndAddress("");
         DirectionsStatus actual = request.doDummyRequest();
         Assert.assertEquals(ACTUAL_STATUS_WAS_LABEL + actual.getMessage(), expected, actual);
     }
 
     /**
      * WhiteBox: This tests the doDummyRequest without setting the departure or
      * arrival times for a transit call.
      */
     // @Test
     public void testDoDummyRequestFailToSetDepartureAndArrivalTimeforTransitOrMixedTest() {
         setUp();
         DirectionsStatus expected = DirectionsStatus.INVALID_REQUEST_PARAMS;
         request.setArrivalTime(RequestParameters.DONT_CARE);
         DirectionsStatus actual = request.doDummyRequest();
         Assert.assertEquals(ACTUAL_STATUS_WAS_LABEL + actual.getMessage(), expected, actual);
     }
 
     /**
      * WhiteBox: This test should ignore that fact that the departure and
      * arrival times equal zero when bicycling.
      */
     // @Test
     public void testDoDummyRequestBreakOnBicylingModeTest() {
         setUp();
         DirectionsStatus expected = DirectionsStatus.REQUEST_SUCCESSFUL;
 
         request.setTravelMode(TravelMode.BICYCLING);
         request.setArrivalTime(0);
         DirectionsStatus actual = request.doDummyRequest();
         Assert.assertEquals(ACTUAL_STATUS_WAS_LABEL + actual.getMessage(), expected, actual);
     }
 
     /**
      * WhiteBox: This tests the do dummy Request with the invalid mode of
      * walking.
      */
     // @Test
     public void testDoDummyRequestInvalidTravelModeWalkingTest() {
         setUp();
         DirectionsStatus expected = DirectionsStatus.INVALID_REQUEST_PARAMS;
 
         request.setTravelMode(TravelMode.WALKING);
         DirectionsStatus actual = request.doDummyRequest();
         Assert.assertEquals(ACTUAL_STATUS_WAS_LABEL + actual.getMessage(), expected, actual);
     }
 
     /**
      * WhiteBox: This tests the doDummyRequest method and sets the min greater
      * than the max.
      */
     // @Test
     public void testDoDummyRequestMinDistanceToBikeGreaterThanMaxDistanceTest() {
         setUp();
         DirectionsStatus expected = DirectionsStatus.INVALID_REQUEST_PARAMS;
 
         request.setMinDistanceToBikeInMeters(BAD_MIN_DIST_BIKE_METER);
         request.setMaxDistanceToBikeInMeters(BAD_MAX_DIST_BIKE_METER);
         DirectionsStatus actual = request.doDummyRequest();
         Assert.assertEquals(ACTUAL_STATUS_WAS_LABEL + actual.getMessage(), expected, actual);
     }
 
     /**
      * WhiteBox: This tests the doDummyRequest method and sets the min greater
      * than the max.
      */
     // @Test
     public void testDoDummyRequestMinNumberOfTransfersGreaterThanMaxNumberTest() {
         setUp();
         DirectionsStatus expected = DirectionsStatus.INVALID_REQUEST_PARAMS;
 
         request.setMinNumberBusTransfers(1 + 1 + 1);
         request.setMaxNumberBusTransfers(1 + 1);
         DirectionsStatus actual = request.doDummyRequest();
         Assert.assertEquals(ACTUAL_STATUS_WAS_LABEL + actual.getMessage(), expected, actual);
     }
 
     /**
      * WhiteBox: This tests the doDummyRequest and sets the value to less than
      * 0.
      */
     // @Test
     public void testDoDummyRequestMinNumberOfTransfersLessThanZeroTest() {
         setUp();
         DirectionsStatus expected = DirectionsStatus.INVALID_REQUEST_PARAMS;
 
         request.setMinNumberBusTransfers(NEGATIVE_MIN_BUS_TRANSFERS);
         DirectionsStatus actual = request.doDummyRequest();
         Assert.assertEquals(ACTUAL_STATUS_WAS_LABEL + actual.getMessage(), expected, actual);
     }
 
     /**
      * WhiteBox: This tests the doDummyRequest and sets the value to less than
      * zero.
      * 
      * @Bug https://github.com/alaurenz/metrobike/issues/93
      */
     // @Test
     public void testDoDummyRequestMaxNumberOfTransfersLessThanZeroTest() {
         setUp();
         DirectionsStatus expected = DirectionsStatus.INVALID_REQUEST_PARAMS;
 
         request.setMaxNumberBusTransfers(NEGATIVE_MIN_BUS_TRANSFERS);
         DirectionsStatus actual = request.doDummyRequest();
         Assert.assertEquals(ACTUAL_STATUS_WAS_LABEL + actual.getMessage(), expected, actual);
     }
 
     /**
      * WhiteBox: This tests the doDummyRequest and sets the value to less than
      * 0.
      */
     // @Test
     public void testDoDummyRequestMinDistanceToBikeLessThanZeroTest() {
         setUp();
         DirectionsStatus expected = DirectionsStatus.INVALID_REQUEST_PARAMS;
 
         request.setMinDistanceToBikeInMeters(NEGATIVE_MIN_BUS_TRANSFERS);
         DirectionsStatus actual = request.doDummyRequest();
         Assert.assertEquals(ACTUAL_STATUS_WAS_LABEL + actual.getMessage(), expected, actual);
     }
 
     /**
      * WhiteBox: This tests setting the departure time and then the arrival
      * time. It should throw an IllegalArgumentException that is caught.
      */
     // @Test
     public void testSetDepartureThenArrivalTimeTest() {
         setUp();
         request.setArrivalTime(RequestParameters.DONT_CARE);
         request.setDepartureTime(DEPARTURE_TIME);
         try {
             request.setArrivalTime(DEPARTURE_TIME);
         } catch (IllegalArgumentException iae) {
             // It should throw an exception here
             return;
         }
 
         Assert.fail("An IllegalArgumentException should have been thrown.");
     }
 
     /**
      * WhiteBox: This tests setting the arrival time and then the departure
      * time. It should throw an IllegalArgumentException that is caught.
      */
     // @Test
     public void testSetArrivalThenDepartureTimeTest() {
         setUp();
         try {
             request.setDepartureTime(DEPARTURE_TIME);
         } catch (IllegalArgumentException iae) {
             // It should throw an exception here.
             return;
         }
 
         Assert.fail("An IllegalArgumentException should have been thrown.");
     }
 
     /**
      * WhiteBox: This tests the doDUmmyRequest and sets the value to less than
      * 0.
      */
     // @Test
     public void testDoDummyRequestMaxDistanceToBikeLessThanZeroTest() {
         setUp();
         DirectionsStatus expected = DirectionsStatus.INVALID_REQUEST_PARAMS;
 
         request.setMaxDistanceToBikeInMeters(NEGATIVE_MIN_BUS_TRANSFERS);
         DirectionsStatus actual = request.doDummyRequest();
         Assert.assertEquals(ACTUAL_STATUS_WAS_LABEL + actual.getMessage(), expected, actual);
     }
 
     /**
      * WhiteBox: This tests the getErrorMessages method.
      */
     // @Test
     public void testGetErrorMessagesTest() {
         setUp();
         String expected = null;
         request.doDummyRequest();
         String actual = request.getVerboseErrorMessages();
         Assert.assertEquals("Actual error messages from request.getErrorMessages() is: " + actual,
                 expected, actual);
     }
 
     /**
      * WhiteBox: This tests the getSolutions method and checks that the returned
      * list is not null.
      */
     // @Test
     public void testGetSolutionsTest() {
         setUp();
         request.doDummyRequest();
         List<Route> actual = request.getSolutions();
         Assert.assertFalse("Actual value for actual.equals(null) should have been false.",
                 actual.equals(null));
     }
 
     /**
      * WhiteBox: Tests the cancel feature by starting a test query, disabling
      * the backend query system, waiting for the test query to finish, and
      * re-enabling the backend query system.
      */
     public void testCancelRunningQuery() {
         threadedResult = null;
 
         Thread requestThread = new Thread(new RequestRunner());
         requestThread.start();
         DirectionsRequest.disableBackendQueries();
 
         try {
             requestThread.join();
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
 
         Assert.assertEquals("Backend query didn't stop when disabled!",
                 DirectionsStatus.CONNECTION_ERROR, threadedResult);
 
         DirectionsRequest.enableBackendQueries();
     }
 
     /**
      * BlackBox: Tests to be sure we can safely serialize and deserialize a
      * DirectionsRequest object. This functionality is used in the
      * intent-passing system.
      * 
      * @throws IOException
      *             if an IO exception occurs during processing
      * @throws ClassNotFoundException
      *             if a class cannot be found
      */
     public void testSerializationTestEmptyDRObject() throws IOException, ClassNotFoundException {
         DirectionsRequest testRequest = new DirectionsRequest();
 
         // Serialize the empty request, then de-serialize it
         byte[] theBytes = helpSerialize(testRequest);
         DirectionsRequest recreatedRequest = helpDeserialize(theBytes);
 
         // Use string equality to check the request
         Assert.assertEquals("The toString() representation of a serialized->deserialized"
                 + " object should remain unchanged.", testRequest.toString(),
                 recreatedRequest.toString());
     }
 
     /**
      * BlackBox: Tests to be sure we can safely serialize and deserialize a
      * non-empty DirectionsRequest object. This functionality is used in the
      * intent-passing system.
      * 
      * @throws IOException
      *             if an IO exception occurs during processing
      * @throws ClassNotFoundException
      *             if a class cannot be found
      */
     public void testSerializationTestNonEmptyDRObject() throws IOException, ClassNotFoundException {
         setUp();
 
         // Serialize the request, then de-serialize it
         byte[] theBytes = helpSerialize(request);
         DirectionsRequest recreatedRequest = helpDeserialize(theBytes);
 
         // Use string equality to check the request
         Assert.assertEquals("The toString() representation of a serialized->deserialized"
                 + " object should remain unchanged.", request.toString(),
                 recreatedRequest.toString());
     }
 
     /**
      * WhiteBox: Tests to make sure that when DoRequest is configured to use the
      * stubGoogleAPIWrapper it actually does.
      */
     public void testStubDoRequestCase1StandardResponse() {
         request = new DirectionsRequest();
 
         String startAddress = "302 NE 50th St,Seattle,WA";
         String endAddress = "3801 Brooklyn Ave NE,Seattle,WA";
         request.setStartAddress(startAddress);
         request.setEndAddress(endAddress);
         request.setTravelMode(TravelMode.BICYCLING);
 
         request.setResource(new StubGoogleAPIWrapper());
 
         DirectionsStatus expected = DirectionsStatus.REQUEST_SUCCESSFUL;
 
         DirectionsStatus actual = request.doRequest();
         Assert.assertEquals(ACTUAL_STATUS_WAS_LABEL + actual.getMessage(), expected, actual);
     }
 
     /**
      * WhiteBox:LoadTest: Test that uses the StubGoogleAPIWrapper and makes sure
      * that the code can handle large JSON responses.
      * 
      * This load tests the system by returning an approximately 60,000 character
      * String.
      */
     public void testStubDoRequestCase2StressTest() {
         request = new DirectionsRequest();
 
         String startAddress = "302 NE 50th St,Seattle,WA";
         String endAddress = "Everett";
         request.setStartAddress(startAddress);
         request.setEndAddress(endAddress);
         request.setTravelMode(TravelMode.BICYCLING);
         request.setResource(new StubGoogleAPIWrapper());
 
         DirectionsStatus expected = DirectionsStatus.REQUEST_SUCCESSFUL;
         DirectionsStatus actual = request.doRequest();
         Assert.assertEquals(ACTUAL_STATUS_WAS_LABEL + actual.getMessage(), expected, actual);
     }
 
     /**
      * WhiteBox:SubSystem Disabled: Test that uses the StubGoogleAPIWrapper and
      * makes sure that the code can handle large JSON responses.
      * 
      * This tests how the system handles not having an Internet connection. It
      * simulates this by having the stub throw a IOException(), which is what
      * would happen if the system was not able to contact Google.
      */
     public void testStubDoRequestCase3ConnectionFailedThrowIOException() {
         request = new DirectionsRequest();
 
         String startAddress = "3801 Brooklyn Ave NE,Seattle,WA";
         String endAddress = "1320 S Maple Grove Road, Boise, ID";
         request.setStartAddress(startAddress);
         request.setEndAddress(endAddress);
         request.setTravelMode(TravelMode.BICYCLING);
         request.setResource(new StubGoogleAPIWrapper());
 
         DirectionsStatus expected = DirectionsStatus.CONNECTION_ERROR;
         DirectionsStatus actual = request.doRequest();
         Assert.assertEquals(ACTUAL_STATUS_WAS_LABEL + actual.getMessage(), expected, actual);
     }
 
     /**
      * WhiteBox: Test that uses the StubGoogleAPIWrapper and makes sure that the
      * code can handle large JSON responses.
      */
     public void testStubDoRequestCase4GoogleReturnsNoResults() {
         request = new DirectionsRequest();
 
         String startAddress = "pig";
         String endAddress = "bacon";
         request.setStartAddress(startAddress);
         request.setEndAddress(endAddress);
         request.setTravelMode(TravelMode.BICYCLING);
         request.setResource(new StubGoogleAPIWrapper());
 
         DirectionsStatus expected = DirectionsStatus.NO_RESULTS_FOUND;
         DirectionsStatus actual = request.doRequest();
         Assert.assertEquals(ACTUAL_STATUS_WAS_LABEL + actual.getMessage(), expected, actual);
     }
 
     /**
      * WhiteBox: Test that uses the StubGoogleAPIWrapper and makes sure that the
      * code can handle large JSON responses. Note that this actual request would
      * technically return a status of ZERO_RESULTS, but I can't get the system
      * to generate an invalid query so I'm using this one in conjunction with
      * the stub.
      */
     public void testStubDoRequestCase5GoogleDeniesRequest() {
         request = new DirectionsRequest();
 
         String startAddress = "cow";
         String endAddress = "steak";
         request.setStartAddress(startAddress);
         request.setEndAddress(endAddress);
         request.setTravelMode(TravelMode.BICYCLING);
         request.setResource(new StubGoogleAPIWrapper());
 
         DirectionsStatus expected = DirectionsStatus.NO_RESULTS_FOUND;
         DirectionsStatus actual = request.doRequest();
         Assert.assertEquals(ACTUAL_STATUS_WAS_LABEL + actual.getMessage(), expected, actual);
     }
 
     /**
      * Helper function for serializing a DirectionsRequest object.Help on
      * testing this based on.
      * http://www.ibm.com/developerworks/library/j-serialtest/index.html
      * 
      * @param toSerialize
      *            the DirectionsRequest to serialize
      * @return a byte array representing the serialized DirectionsRequest
      * @throws IOException
      *             if an IO exception occurs during processing
      */
     private byte[] helpSerialize(final DirectionsRequest toSerialize) throws IOException {
         ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
         ObjectOutputStream objectOut = new ObjectOutputStream(byteOut);
         objectOut.writeObject(toSerialize);
         objectOut.close();
         return byteOut.toByteArray();
     }
 
     /**
      * Helper function for serializing a DirectionsRequest object.Help on
      * testing this based on.
      * http://www.ibm.com/developerworks/library/j-serialtest/index.html
      * 
      * @param toDeSerialize
      *            a byte array representing the serialized DirectionsRequest
      * @return the deserialized DirectionsRequest
      * @throws IOException
      *             if an IO exception occurs during processing
      * @throws ClassNotFoundException
      *             if a class cannot be found
      */
     private DirectionsRequest helpDeserialize(final byte[] toDeSerialize)
             throws ClassNotFoundException, IOException {
         ByteArrayInputStream byteIn = new ByteArrayInputStream(toDeSerialize);
         ObjectInputStream objectIn = new ObjectInputStream(byteIn);
         return (DirectionsRequest) objectIn.readObject();
     }
 }
