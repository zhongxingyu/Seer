 package com.wixpress.fjarr.client;
 
 import com.wixpress.fjarr.util.DisjointUnion;
 import com.wixpress.fjarr.util.MultiMap;
 
 import java.util.Set;
 
 /**
  * @author alexeyr
  * @since 7/5/11 10:55 AM
  */
 
 public class RpcResponseContext
 {
    private DisjointUnion outcome;
     private boolean error = false;
     private final RpcInvocationResponse response;
     private final long requestDurationMillis;
 
     public RpcResponseContext(Throwable throwable, RpcInvocationResponse response, long requestDurationMillis)
     {
         this.requestDurationMillis = requestDurationMillis;
        this.outcome = DisjointUnion.from(throwable);
         this.response = response;
         this.error = true;
     }
 
     public RpcResponseContext(Object responseObject, RpcInvocationResponse response, long requestDurationMillis)
     {
        this.outcome = DisjointUnion.from(responseObject);
         this.response = response;
         this.requestDurationMillis = requestDurationMillis;
         this.error = false;
 
     }
 
     /**
      * DisjointUnion that can contain an Object or a Throwable
      * @return
      */
    public DisjointUnion getOutcome()
     {
        return outcome;
     }
 
     public boolean isError()
     {
         return error;
     }
 
 
 
     public void setResult(Object result)
     {
        outcome = DisjointUnion.from(result);
         error = false;
     }
 
     public MultiMap<String, String> getAllHeaders()
     {
         return response.getAllHeaders();
     }
 
     public Set<String> getHeaders(String name)
     {
         return response.getAllHeaders().getAll(name);
     }
 
     public String getHeader(String name)
     {
         return response.getAllHeaders().get(name);
     }
 
     public long getRequestDurationMillis()
     {
         return requestDurationMillis;
     }
 }
