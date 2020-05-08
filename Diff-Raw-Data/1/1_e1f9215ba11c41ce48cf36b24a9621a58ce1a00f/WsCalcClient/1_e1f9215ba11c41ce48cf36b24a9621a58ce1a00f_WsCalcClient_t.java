 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.fabric3.samples.ws.client;
 
 import java.net.URL;
 import javax.xml.namespace.QName;
 
 import org.fabric3.samples.ws.calculator.CalculatorService;
 import org.fabric3.samples.ws.calculator.CalculatorServiceService;
 
 /**
  * @version $Rev$ $Date$
  */
 public class WsCalcClient {
 
     public static void main(String[] args) throws Exception {
         // URL url = new URL("http://localhost:8080/calculator?wsdl");
         // URL when calculator deployed in the cluster without a load-balancer
         URL url = new URL("http://localhost:8181/calculator?wsdl");
         QName name = new QName("http://calculator.ws.samples.fabric3.org/", "CalculatorServiceService");
         CalculatorServiceService service = new CalculatorServiceService(url, name);
         CalculatorService calculator = service.getCalculatorServicePort();
         System.out.println("1 + 2 = " + calculator.add(1, 2));
     }
 }
