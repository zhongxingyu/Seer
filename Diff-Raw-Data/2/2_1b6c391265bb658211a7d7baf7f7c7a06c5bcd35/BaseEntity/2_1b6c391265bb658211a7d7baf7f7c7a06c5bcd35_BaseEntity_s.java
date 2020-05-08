 /*
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
 package au.id.hazelwood.sos.model.framework;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.apache.commons.lang.builder.ToStringStyle;
 
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.MappedSuperclass;
 import java.io.Serializable;
 
 /**
  * Base class for Model objects. Implements equals, hashCode and toString using reflection.
  */
 @MappedSuperclass
 @SuppressWarnings("UnusedDeclaration")
 public abstract class BaseEntity implements Serializable
 {
     protected static final String USERNAME_REGEX = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*";
     protected static final String DOMAIN_REGEX = "(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?)+(?:\\.(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?)+)*\\.[a-z]{2,9}";
     protected static final String IP_REGEX = "[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}";
     protected static final String NAME_REGEX = "^(?:[a-z]('?[a-z]+)?((-| )(?:[a-z]('?[a-z]+)?))*)?$";
     protected static final String EMAIL_REGEX = "^" + USERNAME_REGEX + "@(" + DOMAIN_REGEX + "|" + IP_REGEX + ")$";
     @Id
     @GeneratedValue
     private Long id;
 
     protected BaseEntity()
     {
     }
 
     public Long getId()
     {
         return id;
     }
 
     public void setId(Long id)
     {
         this.id = id;
     }
 
     @Override
    @SuppressWarnings({"EqualsWhichDoesntCheckParameterClass"})
     public boolean equals(Object o)
     {
         return EqualsBuilder.reflectionEquals(this, o, getExcludedFieldsForEqualsAndHashCode());
     }
 
     @Override
     public int hashCode()
     {
         return HashCodeBuilder.reflectionHashCode(this, getExcludedFieldsForEqualsAndHashCode());
     }
 
     @Override
     public String toString()
     {
         return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
     }
 
     /**
      * Get an array of fields to ignore when determining equals / calculating hashCode.  Override this if your class wants extra fields to be
      * ignored.
      *
      * @return An array of identifiers for fields to be ignored.
      */
     protected String[] getExcludedFieldsForEqualsAndHashCode()
     {
         return new String[]{"id"};
     }
 }
