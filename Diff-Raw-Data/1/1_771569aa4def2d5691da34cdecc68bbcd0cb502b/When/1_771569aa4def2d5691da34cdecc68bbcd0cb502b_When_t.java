 package uk.co.epsilontechnologies.primer;
 
 import uk.co.epsilontechnologies.primer.domain.Request;
 import uk.co.epsilontechnologies.primer.domain.Response;
 
 /**
  * Container for programming the 'when' of the test case.
  *
  * @author Shane Gibson
  */
 public class When {
 
     /**
      * The primer instance that is being programmed
      */
     private final Primer primer;
 
     /**
      * The request that is being programmed
      */
     private final Request request;
 
     /**
      * Constructs the 'when' instance for the given primer and request
      * @param primer the primer instance that is being programmed
      * @param request the request instance that is being programmed
      */
     When(final Primer primer, final Request request) {
         this.primer = primer;
         this.request = request;
     }
 
     /**
      * Configures the primer to return the given responses (in sequence) for the primer and request
      * @param responses the responses to configure
      * @return The instance of the 'when' that is being programmed
      */
     public When thenReturn(final Response... responses) {
         this.primer.prime(request, responses);
         return this;
     }
 
 }
