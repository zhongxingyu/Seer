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
 package tutorial;
 
 import org.apache.camel.spring.SpringRouteBuilder;
 
 /**
  * A Camel Router
  * 
  * @version $
  */
 public class MyRouteBuilder extends SpringRouteBuilder {
 
 
 	public void configure() {
 		//handle exceptions and log them
		onException(IllegalArgumentException.class).
        maximumRedeliveries(0)
        .handled(true)
        .beanRef("accountService", "dumpTable")
        .to("file:target/messages?fileName=deadLetters.xml&fileExist=Append")
        .markRollbackOnly();
 
 		//our route definition
 		//noop option - if true, the file is not moved or deleted in any way
 		from("file:src/data?noop=true")
 		//mark the route as transacted
         .transacted()
         //execute spring bean methods
         .beanRef("accountService","credit")
         .beanRef("accountService","debit")
         .beanRef("accountService","dumpTable")
         //log the result
         .to("log:ExampleRouter");
 		
 	}
 }
