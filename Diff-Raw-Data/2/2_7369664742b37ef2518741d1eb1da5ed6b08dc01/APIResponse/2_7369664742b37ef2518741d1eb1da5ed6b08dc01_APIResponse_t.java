 package com.scottbyrns.api.client;
 
 import com.scottbyrns.utilities.FatalMappingException;
 import com.scottbyrns.utilities.InvalidJSONStringException;
 import com.scottbyrns.utilities.JSONObjectMapper;
 
 import java.io.IOException;
 
 /**
  * API Response
  */
 public class APIResponse<ResponseEntity>
 {
 
     private String rawResponseString;
     private ResponseEntity responseEntity;
 
     /**
      * Get the raw response string of the apis response.
      *
      * @return The raw response string.
      */
     public String getRawResponseString()
     {
         return rawResponseString;
     }
 
     /**
      * Set the raw response string of the apis response.
      *
      * @param rawResponseString The raw response string.
      */
     public void setRawResponseString(String rawResponseString)
     {
         this.rawResponseString = rawResponseString;
     }
 
     /**
      * Get the response entity.
      * This is an entity hydrated with the raw api response.
      *
      * @return The response entity.
      */
     public ResponseEntity getResponseEntity(Class entityClass)
     {
         if (null == responseEntity)
         {
             try
             {
                responseEntity = (ResponseEntity)JSONObjectMapper.getInstance().<ResponseEntity>mapJSONStringToEntity(
                         getRawResponseString(),
                         entityClass
                 );
 
             }
             catch (InvalidJSONStringException e) {
                 e.printStackTrace();
             }
             catch (FatalMappingException e) {
                 e.printStackTrace();
             }
         }
         return responseEntity;
     }
 
     /**
      * Set the response entity.
      * This is an entity hydrated with the raw api response.
      *
      * @param responseEntity The response entity.
      */
     public void setResponseEntity(ResponseEntity responseEntity)
     {
         this.responseEntity = responseEntity;
     }
 
 //    public Class<?> getCreateClass()
 //    {
 //
 //        return (Class<?>) (
 //                    (ParameterizedType)(
 //                            this.getClass().getTypeParameters()[0].getBounds()[0]
 //                    )
 //                )
 //                .getActualTypeArguments()[0];
 //    }
 }
