 package com.terradue.dsi;
 
 /*
  *  Copyright 2012 Terradue srl
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 import static java.lang.System.exit;
 import static java.util.Arrays.asList;
 
 import java.util.Collection;
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import com.sun.jersey.api.client.GenericType;
 import com.terradue.dsi.model.Account;
 
 /**
  * @since 0.2
  */
 public final class DescribeUsers
     extends AbstractDescribeCommand
 {
 
     public static void main( String[] args )
     {
         exit( new DescribeUsers().execute( args ) );
     }
 
     @Override
     @Inject
     public void setServiceUrl( @Named( "service.accounts" ) String serviceUrl )
     {
         super.setServiceUrl( serviceUrl );
     }
 
     @Override
     protected List<String> getDefaultFields()
     {
         return asList( "id", "name", "owner" );
     }
 
     @Override
     protected void execute()
         throws Exception
     {
         log( restClient.resource( serviceUrl ).get( new GenericType<Collection<Account>>(){} ) );
     }
 
 }
