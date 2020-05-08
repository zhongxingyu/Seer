 package mocks;
 
 public class MockPostRequest extends MockRequest {
 
     private String fooToReturn;
     private String barToReturn;
 
     public MockPostRequest(String headersToReturn, String fooToReturn, String barToReturn) {
 
         super(headersToReturn);
         this.fooToReturn = fooToReturn;
         this.barToReturn = barToReturn;
     }
 
    @Override
     public String fooValue() {
         return fooToReturn;
     }
 
    @Override
     public String barValue() {
         return barToReturn;
     }
 }
