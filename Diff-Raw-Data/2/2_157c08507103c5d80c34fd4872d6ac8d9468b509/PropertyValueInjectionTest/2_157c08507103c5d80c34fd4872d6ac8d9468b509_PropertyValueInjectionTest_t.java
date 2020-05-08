 package org.carlspring.ioc;
 
 /**
  * Copyright 2012 Martin Todorov.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import org.junit.Test;
 
 import java.io.IOException;
 
 import static junit.framework.Assert.assertNull;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 /**
  * @author mtodorov
  */
 public class PropertyValueInjectionTest
 {
 
 
     @Test
     public void testInjectionNoParents()
             throws InjectionException
     {
         System.out.println("Testing class without inheritance...");
 
         PropertyHolder holder = new PropertyHolder();
 
         PropertyValueInjector.inject(holder);
 
         assertNotNull("Failed to inject property 'jdbc.username'!", holder.getUsername());
         assertNotNull("Failed to inject property 'jdbc.password'!", holder.getPassword());
 
         System.out.println("Username: " + holder.getUsername());
         System.out.println("Password: " + holder.getPassword());
     }
 
     @Test
     public void testInjectionFromParents()
             throws InjectionException
     {
         System.out.println("Testing class with inheritance...");
 
         ExtendedPropertyHolder holder = new ExtendedPropertyHolder();
 
         PropertyValueInjector.inject(holder);
 
         assertNotNull("Failed to inject property 'jdbc.username'!", holder.getUsername());
         assertNotNull("Failed to inject property 'jdbc.password'!", holder.getPassword());
         assertNotNull("Failed to inject property 'jdbc.url'!", holder.getUrl());
 
         System.out.println("Username: " + holder.getUsername());
         System.out.println("Password: " + holder.getPassword());
         System.out.println("URL:      " + holder.getUrl());
     }
 
     @Test
     public void testInjectionOverrideFromSystemProperties()
             throws InjectionException
     {
         System.out.println("Testing class with inheritance...");
 
         System.getProperties().setProperty("jdbc.password", "mypassw0rd");
 
         ExtendedPropertyHolder holder = new ExtendedPropertyHolder();
 
         PropertyValueInjector.inject(holder);
 
         assertNotNull("Failed to inject property 'jdbc.username'!", holder.getUsername());
         assertNotNull("Failed to inject property 'jdbc.password'!", holder.getPassword());
         assertNotNull("Failed to inject property 'jdbc.url'!", holder.getUrl());
         assertEquals("Failed to override property with system value!", "mypassw0rd", holder.getPassword());
 
         System.out.println("Username: " + holder.getUsername());
         System.out.println("Password: " + holder.getPassword());
         System.out.println("URL:      " + holder.getUrl());
     }
 
 	@Test
 	 public void testInjectionWithIncorrectResource()
 	         throws InjectionException
 	 {
 	     System.out.println("Testing class with incorrect resource...");
 
 	     System.getProperties().setProperty("jdbc.password", "mypassw0rd");
 
 	     PropertyHolderWithIncorrectResource holder = new PropertyHolderWithIncorrectResource();
 
 	     PropertyValueInjector.inject(holder);
 
	     assertNull("Should have failed to inject property 'jdbc.password'!", holder.getPassword());
 	 }
 
     @PropertiesResources(resources = { "META-INF/properties/jdbc.properties" })
     private class PropertyHolder
     {
         @PropertyValue(key = "jdbc.username")
         String username;
 
         // Let's have a private field in the parent class.
         @PropertyValue(key = "jdbc.password")
         private String password;
 
         @PropertyValue(key = "")
         String blah;
 
 
         private PropertyHolder()
         {
         }
 
         public String getUsername()
         {
             return username;
         }
 
         public void setUsername(String username)
         {
             this.username = username;
         }
 
         public String getPassword()
         {
             return password;
         }
 
         public void setPassword(String password)
         {
             this.password = password;
         }
     }
 
     private class ExtendedPropertyHolder extends PropertyHolder
     {
         @PropertyValue(key = "jdbc.url")
         private String url;
 
 
         private ExtendedPropertyHolder()
         {
         }
 
         public String getUrl()
         {
             return url;
         }
 
         public void setUrl(String url)
         {
             this.url = url;
         }
 
     }
 
 	@PropertiesResources(resources = { "META-INF/properties/incorrect.properties" })
 	private class PropertyHolderWithIncorrectResource {
 
         // Let's have a private field in the parent class.
         @PropertyValue(key = "jdbc.password")
         private String password;
 
 
 		private PropertyHolderWithIncorrectResource() {
 		}
 
 		public String getPassword() {
 			return password;
 		}
 
 		public void setPassword(String password) {
 			this.password = password;
 		}
 
 	}
 
 }
