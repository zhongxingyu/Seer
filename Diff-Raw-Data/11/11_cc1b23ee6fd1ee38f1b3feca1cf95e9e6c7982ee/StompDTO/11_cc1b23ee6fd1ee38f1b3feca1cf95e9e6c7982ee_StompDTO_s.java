 /**
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.activemq.apollo.stomp.dto;
 
 import org.apache.activemq.apollo.dto.AddUserHeaderDTO;
 import org.apache.activemq.apollo.dto.ProtocolDTO;
 
 import javax.xml.bind.annotation.*;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Allow you to customize the stomp protocol implementation.
  *
  * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
  */
 @XmlRootElement(name="stomp")
 @XmlAccessorType(XmlAccessType.FIELD)
 public class StompDTO extends ProtocolDTO {
 
     @XmlAttribute(name="add_user_header")
     public String add_user_header;
 
     /**
      * A broker accepts connections via it's configured connectors.
      */
     @XmlElement(name="add_user_header")
     public List<AddUserHeaderDTO> add_user_headers = new ArrayList<AddUserHeaderDTO>();
 
     /**
      * If set, it will add the configured header name with the value
     * set the a timestamp of when the message is recieved.
      */
     @XmlAttribute(name="add_timestamp_header")
     public String add_timestamp_header;
 
     @XmlAttribute(name="max_header_length")
     public Integer max_header_length;
 
     @XmlAttribute(name="max_headers")
     public Integer max_headers;
 
     @XmlAttribute(name="max_data_length")
     public Integer max_data_length;
 
     /**
      * A broker accepts connections via it's configured connectors.
      */
     @XmlElement(name="protocol_filter")
     public List<String> protocol_filters = new ArrayList<String>();
 
     @XmlAttribute(name="queue_prefix")
     public String queue_prefix;
 
     @XmlAttribute(name="topic_prefix")
     public String topic_prefix;
 
     @XmlAttribute(name="destination_separator")
     public String destination_separator;
 
     @XmlAttribute(name="path_separator")
     public String path_separator;
 
     @XmlAttribute(name="any_child_wildcard")
     public String any_child_wildcard;
 
     @XmlAttribute(name="any_descendant_wildcard")
     public String any_descendant_wildcard;
 
     @XmlAttribute(name="regex_wildcard_start")
     public String regex_wildcard_start;
 
     @XmlAttribute(name="regex_wildcard_end")
     public String regex_wildcard_end;
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
         if (!super.equals(o)) return false;
 
         StompDTO stompDTO = (StompDTO) o;
 
         if (add_user_header != null ? !add_user_header.equals(stompDTO.add_user_header) : stompDTO.add_user_header != null)
             return false;
         if (add_user_headers != null ? !add_user_headers.equals(stompDTO.add_user_headers) : stompDTO.add_user_headers != null)
             return false;
         if (any_child_wildcard != null ? !any_child_wildcard.equals(stompDTO.any_child_wildcard) : stompDTO.any_child_wildcard != null)
             return false;
         if (any_descendant_wildcard != null ? !any_descendant_wildcard.equals(stompDTO.any_descendant_wildcard) : stompDTO.any_descendant_wildcard != null)
             return false;
         if (destination_separator != null ? !destination_separator.equals(stompDTO.destination_separator) : stompDTO.destination_separator != null)
             return false;
         if (max_data_length != null ? !max_data_length.equals(stompDTO.max_data_length) : stompDTO.max_data_length != null)
             return false;
         if (max_header_length != null ? !max_header_length.equals(stompDTO.max_header_length) : stompDTO.max_header_length != null)
             return false;
         if (max_headers != null ? !max_headers.equals(stompDTO.max_headers) : stompDTO.max_headers != null)
             return false;
         if (path_separator != null ? !path_separator.equals(stompDTO.path_separator) : stompDTO.path_separator != null)
             return false;
         if (protocol_filters != null ? !protocol_filters.equals(stompDTO.protocol_filters) : stompDTO.protocol_filters != null)
             return false;
         if (queue_prefix != null ? !queue_prefix.equals(stompDTO.queue_prefix) : stompDTO.queue_prefix != null)
             return false;
         if (regex_wildcard_end != null ? !regex_wildcard_end.equals(stompDTO.regex_wildcard_end) : stompDTO.regex_wildcard_end != null)
             return false;
         if (regex_wildcard_start != null ? !regex_wildcard_start.equals(stompDTO.regex_wildcard_start) : stompDTO.regex_wildcard_start != null)
             return false;
         if (topic_prefix != null ? !topic_prefix.equals(stompDTO.topic_prefix) : stompDTO.topic_prefix != null)
             return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = super.hashCode();
         result = 31 * result + (add_user_header != null ? add_user_header.hashCode() : 0);
         result = 31 * result + (add_user_headers != null ? add_user_headers.hashCode() : 0);
         result = 31 * result + (max_header_length != null ? max_header_length.hashCode() : 0);
         result = 31 * result + (max_headers != null ? max_headers.hashCode() : 0);
         result = 31 * result + (max_data_length != null ? max_data_length.hashCode() : 0);
         result = 31 * result + (protocol_filters != null ? protocol_filters.hashCode() : 0);
         result = 31 * result + (queue_prefix != null ? queue_prefix.hashCode() : 0);
         result = 31 * result + (topic_prefix != null ? topic_prefix.hashCode() : 0);
         result = 31 * result + (destination_separator != null ? destination_separator.hashCode() : 0);
         result = 31 * result + (path_separator != null ? path_separator.hashCode() : 0);
         result = 31 * result + (any_child_wildcard != null ? any_child_wildcard.hashCode() : 0);
         result = 31 * result + (any_descendant_wildcard != null ? any_descendant_wildcard.hashCode() : 0);
         result = 31 * result + (regex_wildcard_start != null ? regex_wildcard_start.hashCode() : 0);
         result = 31 * result + (regex_wildcard_end != null ? regex_wildcard_end.hashCode() : 0);
         return result;
     }
 }
