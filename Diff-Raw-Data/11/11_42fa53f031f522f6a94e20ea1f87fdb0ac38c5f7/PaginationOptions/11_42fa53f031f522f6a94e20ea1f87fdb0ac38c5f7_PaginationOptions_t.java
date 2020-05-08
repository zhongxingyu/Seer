 /*		
  * Copyright 2008 The Fepss Pty Ltd. 
  * site: http://www.fepss.com
  * file: $Id$
  * created at:2009-4-27
  */
 package corner.model;
 
 import corner.protobuf.ProtocolBuffer;
 
 import java.io.Serializable;
 
 import com.google.protobuf.Descriptors;
 import com.google.protobuf.InvalidProtocolBufferException;
 import org.apache.tapestry5.json.JSONObject;
 
 /**
  * wrapper for pagination proto buffer
  * @author <a href="jun.tsai@fepss.com">Jun Tsai</a>
  * @version $Revision$
  * @since 0.0.1
  */
 public class PaginationOptions implements Serializable, ProtocolBuffer {
     private static final long serialVersionUID = 1L;
     private final PaginationProtoBuffer.Pagination.Builder builder = PaginationProtoBuffer.Pagination.newBuilder();
 
     public int getPage() {
         return builder.getPage();
     }
 
     public JSONObject getParameters() {
         if(builder.getParameters()!=null){
             return new JSONObject(builder.getParameters());
         }
         return null;
     }
 
     public int getPerPage() {
         return builder.getPerPage();
     }
 
     public long getTotalRecord() {
         return builder.getTotalRecord();
     }
 
     public void setPage(int value) {
         builder.setPage(value);
     }
 
     public void setParameters(JSONObject value) {
         builder.setParameters(value.toString());
     }
 
     public void setPerPage(int value) {
         builder.setPerPage(value);
     }
 
     public void setTotalRecord(long value) {
         builder.setTotalRecord(value);
     }
 
     /**
      * @see .ProtocolBuffer#getData()
      */
     public byte[] getData() {
         return this.builder.clone().build().toByteArray();
     }
 
     /**
      * @see .ProtocolBuffer#mergeData(byte[])
      */
     public void mergeData(byte[] byteData) {
         try {
             this.builder.mergeFrom(byteData);
         } catch (InvalidProtocolBufferException e) {
             throw new RuntimeException(e);
         }
     }
 }
