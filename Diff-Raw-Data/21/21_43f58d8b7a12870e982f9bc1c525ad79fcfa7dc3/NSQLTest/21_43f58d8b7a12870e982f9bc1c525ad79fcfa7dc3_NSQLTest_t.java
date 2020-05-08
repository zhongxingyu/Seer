//    Data SQL is a light JDBC wrapper.
//    Copyright (C) 2014 Adrián Romero Corchado.
//
//    This file is part of Data SQL
//
//     Licensed under the Apache License, Version 2.0 (the "License");
//     you may not use this file except in compliance with the License.
//     You may obtain a copy of the License at
//     
//         http://www.apache.org/licenses/LICENSE-2.0
//     
//     Unless required by applicable law or agreed to in writing, software
//     distributed under the License is distributed on an "AS IS" BASIS,
//     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//     See the License for the specific language governing permissions and
//     limitations under the License.
 
 package com.adr.datasql;
 
 import java.text.ParseException;
 import org.junit.Assert;
 import org.junit.Test;
 
 /**
  *
  * @author adrian
  */
 public class NSQLTest {
     
     public NSQLTest() {
     }
 
     @Test
     public void testQueries() throws ParseException {
         
         Assert.assertEquals(
                 "select * from table where name = ? and surname = ?[name, surname]", 
                 new NSQL("select * from table where name = :name and surname = :surname").toString());
         Assert.assertEquals(
                 "select * from table where date > ? and date < ?[mydate, mydate]", 
                 new NSQL("select * from table where date > :mydate and date < :mydate").toString());
         Assert.assertEquals(
                 "select * from table where date > ':mydate' and date < ?[mydate]", 
                 new NSQL("select * from table where date > ':mydate' and date < :mydate").toString());
         try {
             new NSQL("select * from table where date > :mydate and date < :").toString();
         } catch (ParseException e) {
            Assert.assertEquals(e.getMessage(), 53, e.getErrorOffset());
         }
 
     
     }
 }
