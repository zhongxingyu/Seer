 /*
  * Copyright (C) 2013 tarent AG
  *
  * Permission is hereby granted, free of charge, to any person obtaining
  * a copy of this software and associated documentation files (the
  * "Software"), to deal in the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to
  * the following conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
  * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
  * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
  * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package org.osiam.resources.scim;
 
 import java.util.HashSet;
 import java.util.Set;
 
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
 
 
 /**
  * Java class for Group complex type.
  */
@JsonInclude(Include.NON_EMPTY)
 public class Group extends CoreResource{
 
     private String displayName;
    private Set<MemberRef> members;
 
     //JSON Serializing
     public Group(){}
 
 
     public Group(Builder builder) {
         super(builder);
         this.displayName = builder.displayName;
         this.members = builder.members;
     }
 
     public static class Builder extends CoreResource.Builder{
 
         private String displayName;
        private Set<MemberRef> members = new HashSet<>();
 
         public Builder(){}
 
         public Builder(Group group) {
             id = group.getId();
             meta = group.getMeta();
             externalId = group.getExternalId();
             displayName = group.displayName;
             members = group.members;
         }
 
         public Builder setDisplayName(String displayName) {
             this.displayName = displayName;
             return this;
         }
 
         public Builder setId(String id) {
             super.id = id;
             return this;
         }
         
         public Builder setMeta(Meta meta) {
             super.meta = meta;
             return this;
         }
         
         public Builder setExternalId(String externalId) {
             super.externalId = externalId;
             return this;
         }
         
        public Builder setMembers(Set<MemberRef> members) {
             this.members = members;
             return this;
         }
 
         public Group build(){
             return new Group(this);
         }
     }
 
     /**
      * Gets the value of the displayName property.
      *
      * @return
      *     possible object is
      *     {@link String }
      *
      */
     public String getDisplayName() {
         return displayName;
     }
 
    public Set<MemberRef> getMembers() {
         return members;
     }
 
 }
