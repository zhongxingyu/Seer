 /*
  * Copyright (c) 2008-2013 David Kellum
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you
  * may not use this file except in compliance with the License.  You may
  * obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied.  See the License for the specific language governing
  * permissions and limitations under the License.
  */
 
 package iudex.core;
 
 import static org.junit.Assert.*;
 import iudex.core.Domains;
 
 import org.junit.Test;
 
 public class DomainTest
 {
     @Test
     public void testRegistrationLevelDomain()
     {
         assertEquals( "timesonline.co.uk", rld( "m.w.timesonline.co.uk" ) );
         assertEquals( "timesonline.co.uk", rld(   "w.timesonline.co.uk" ) );
         assertEquals( "timesonline.co.uk", rld(     "timesonline.co.uk" ) );
         assertEquals( null,                rld(                 "co.uk" ) );
 
         assertEquals( "google.com", rld( "more.www.google.com" ) );
         assertEquals( "google.com", rld(      "www.google.com" ) );
         assertEquals( "google.com", rld(          "google.com" ) );
         assertEquals( null,         rld(                 "com" ) );
 
        assertEquals( "gov.ar",      rld( "www.some.gov.ar" ) );
         assertEquals( "nacion.ar",   rld(   "www.nacion.ar" ) );
 
         assertEquals( null, rld( "." ) );
         assertEquals( null, rld( "" ) );
         assertEquals( null, rld( null ) );
     }
 
     private String rld( String name )
     {
         return Domains.registrationLevelDomain( name );
     }
 
     @Test
     public void testParent()
     {
         assertEquals( "foo.bar", Domains.parent( "www.foo.bar" ) );
         assertEquals( "bar",     Domains.parent( "foo.bar" ) );
         assertEquals( null,      Domains.parent( "bar" ) );
         assertEquals( null,      Domains.parent( "bar." ) );
         assertEquals( null,      Domains.parent( "." ) );
         assertEquals( null,      Domains.parent( "" ) );
     }
 
     @Test
     public void testNormalize()
     {
         assertEquals( "foo.bar", Domains.normalize( "FOO.bAr" ) );
         assertEquals( "foo.bar", Domains.normalize( "FOO.bAr." ) );
         assertEquals( null,      Domains.normalize( "." ) );
         assertEquals( null,      Domains.normalize( "" ) );
         assertEquals( null,      Domains.normalize( null ) );
     }
 
 }
