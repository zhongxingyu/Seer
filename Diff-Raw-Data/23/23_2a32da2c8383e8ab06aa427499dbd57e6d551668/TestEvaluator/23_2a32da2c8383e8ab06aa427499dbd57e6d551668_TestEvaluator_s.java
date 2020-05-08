 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package xquery4j;
 
 import javax.xml.namespace.QName;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.junit.Test;
 import org.w3c.dom.Node;
 
 public class TestEvaluator {
     
     private static Log __log = LogFactory.getLog(TestEvaluator.class);
 
     private int id = 0;
     
     public static class MyFunctions {
         public static String myHello(String arg) {
             TestEvaluator te = (TestEvaluator) XQueryEvaluator.contextObjectTL.get();
             te.id++;
             return "hello(" + arg + te.id + ")";
         }
     }
     
     public XQueryEvaluator buildQueryEvaluator() {
         XQueryEvaluator evaluator = new XQueryEvaluator();
         evaluator.setContextObject(this);
         evaluator.declareJavaClass("http://xmlbeans.apache.org/samples/xquery/employees", MyFunctions.class);
         evaluator.bindVariable(QName.valueOf("myVar"), 123);
         return evaluator;
     }
     
     @Test
     public void testEvaluate() throws Exception {
         XQueryEvaluator evaluator = buildQueryEvaluator();
         Node result = (Node) evaluator.evaluateExpression(IOUtils.toString(getClass().getResourceAsStream("/query.xml")), DOMUtils.parse(getClass().getResourceAsStream("/employees.xml"))).get(0);
         __log.debug(DOMUtils.domToString(result));
     }
 }
