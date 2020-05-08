 package mocks;
 
 public class MockPostRequest extends MockRequest {
 
     private String fooToReturn;
     private String barToReturn;
 
     public MockPostRequest(String headersToReturn, String fooToReturn, String barToReturn) {
 
         super(headersToReturn);
         this.fooToReturn = fooToReturn;
         this.barToReturn = barToReturn;
     }
 
     public String fooValue() {
         return fooToReturn;
     }
 
     public String barValue() {
         return barToReturn;
     }
 }
