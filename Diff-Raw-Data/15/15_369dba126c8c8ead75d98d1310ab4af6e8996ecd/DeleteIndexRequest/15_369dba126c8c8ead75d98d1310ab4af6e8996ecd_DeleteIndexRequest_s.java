 /*
  * Licensed to Elasticsearch under one or more contributor
  * license agreements. See the NOTICE file distributed with
  * this work for additional information regarding copyright
  * ownership. Elasticsearch licenses this file to you under
  * the Apache License, Version 2.0 (the "License"); you may
  * not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package org.elasticsearch.action.admin.indices.delete;
 
 import org.elasticsearch.action.ActionRequestValidationException;
 import org.elasticsearch.action.support.IndicesOptions;
 import org.elasticsearch.action.support.master.AcknowledgedRequest;
 import org.elasticsearch.action.support.master.MasterNodeOperationRequest;
 import org.elasticsearch.common.io.stream.StreamInput;
 import org.elasticsearch.common.io.stream.StreamOutput;
 import org.elasticsearch.common.unit.TimeValue;
 
 import java.io.IOException;
 
 import static org.elasticsearch.action.ValidateActions.addValidationError;
 import static org.elasticsearch.common.unit.TimeValue.readTimeValue;
 
 /**
  * A request to delete an index. Best created with {@link org.elasticsearch.client.Requests#deleteIndexRequest(String)}.
  */
 public class DeleteIndexRequest extends MasterNodeOperationRequest<DeleteIndexRequest> {
 
     private String[] indices;
     // Delete index should work by default on both open and closed indices.
     private IndicesOptions indicesOptions = IndicesOptions.fromOptions(false, true, true, true);
     private TimeValue timeout = AcknowledgedRequest.DEFAULT_ACK_TIMEOUT;
 
     DeleteIndexRequest() {
     }
 
     /**
      * Constructs a new delete index request for the specified index.
      */
     public DeleteIndexRequest(String index) {
         this.indices = new String[]{index};
     }
 
     public DeleteIndexRequest(String... indices) {
         this.indices = indices;
     }
 
     public IndicesOptions indicesOptions() {
         return indicesOptions;
     }
 
     public DeleteIndexRequest indicesOptions(IndicesOptions indicesOptions) {
         this.indicesOptions = indicesOptions;
         return this;
     }
 
     @Override
     public ActionRequestValidationException validate() {
         ActionRequestValidationException validationException = null;
         if (indices == null || indices.length == 0) {
             validationException = addValidationError("index / indices is missing", validationException);
         }
         return validationException;
     }
 
     public DeleteIndexRequest indices(String... indices) {
         this.indices = indices;
         return this;
     }
 
     /**
      * The index to delete.
      */
     String[] indices() {
         return indices;
     }
 
     /**
      * Timeout to wait for the index deletion to be acknowledged by current cluster nodes. Defaults
      * to <tt>10s</tt>.
      */
     public TimeValue timeout() {
         return timeout;
     }
 
     /**
      * Timeout to wait for the index deletion to be acknowledged by current cluster nodes. Defaults
      * to <tt>10s</tt>.
      */
     public DeleteIndexRequest timeout(TimeValue timeout) {
         this.timeout = timeout;
         return this;
     }
 
     /**
      * Timeout to wait for the index deletion to be acknowledged by current cluster nodes. Defaults
      * to <tt>10s</tt>.
      */
     public DeleteIndexRequest timeout(String timeout) {
         return timeout(TimeValue.parseTimeValue(timeout, null));
     }
 
     @Override
     public void readFrom(StreamInput in) throws IOException {
         super.readFrom(in);
         indices = in.readStringArray();
         indicesOptions = IndicesOptions.readIndicesOptions(in);
         timeout = readTimeValue(in);
     }
 
     @Override
     public void writeTo(StreamOutput out) throws IOException {
         super.writeTo(out);
         out.writeStringArray(indices);
         indicesOptions.writeIndicesOptions(out);
         timeout.writeTo(out);
     }
 }
