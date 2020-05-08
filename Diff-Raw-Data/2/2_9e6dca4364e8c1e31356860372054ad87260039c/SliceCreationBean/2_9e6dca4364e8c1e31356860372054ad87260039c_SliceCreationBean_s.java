 package de.zib.gndmc.DSpace.beans;
 
 /*
  * Copyright 2008-2011 Zuse Institute Berlin (ZIB)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 
 
 import java.util.Properties;
 
 /**
  * @author  try ma ik jo rr a zib
  * @version $Id$
  *          <p/>
  *          User: mjorra, Date: 26.07.2010, Time: 13:15:59
  */
 public class SliceCreationBean extends SubSpaceBean {
 
    public final static String SLICE_KIND_URI_KEY = "DSpace.Slice.creation.sliceKindeURI";
     public final static String SIZE_KEY = "DSpace.Slice.creation.size";
     public final static String LIFE_SPAN_KEY = "DSpace.Slice.creation.lifeSpan";
 
 
     private String sliceKindURI;
     private Long size;
     private Long lifeSpan; //lifetime in minutes
 
 
     public String getSliceKindURI() {
         return sliceKindURI;
     }
 
 
     public void setSliceKindURI( String sliceKindURI ) {
         this.sliceKindURI = sliceKindURI;
     }
 
 
     public long getSize() {
         return size;
     }
 
 
     public void setSize( long size ) {
         this.size = size;
     }
 
 
     public long getLifeSpan() {
         return lifeSpan;
     }
 
 
     public void setLifeSpan( long lifeSpan ) {
         this.lifeSpan = lifeSpan;
     }
 
 
     @Override
     public void createExampleProperties( Properties prop ) {
         super.createExampleProperties( prop );
         prop.setProperty( SLICE_KIND_URI_KEY, "<the-key-of-the-slice-kind>" );
         prop.setProperty( SIZE_KEY, "<the-max-space-of-the-new-slice-in-byte>" );
         prop.setProperty( LIFE_SPAN_KEY, "<the-time-to-life-of-the-slice-in-minutes>" );
     }
 
 
     @Override
     public void setProperties( Properties prop ) {
         super.setProperties( prop );
 
         sliceKindURI = prop.getProperty( SLICE_KIND_URI_KEY );
         if( prop.contains( SIZE_KEY ) )
             size = Math.abs( Long.parseLong( prop.getProperty( SIZE_KEY ) ) );
         if( prop.contains( LIFE_SPAN_KEY ) )
             lifeSpan = Math.abs( Long.parseLong( prop.getProperty( LIFE_SPAN_KEY ) ) );
     }
 
 
     public boolean hasLifeSpan() {
 
         return lifeSpan != null && lifeSpan != 0;
     }
 
 
     public boolean hasSize( ) {
 
         return size != null && size != 0;
     }
 }
