 // ========================================================================
// Copyright 2008-2010 NEXCOM Systems
 // ------------------------------------------------------------------------
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at 
 // http://www.apache.org/licenses/LICENSE-2.0
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 // ========================================================================
 
 
 package org.cipango.example;
 
 import java.util.List;
 
 public class RtpConnection {
 
     private String _host;
     private int _port;
     private List<Integer> _payloadTypes;
 
     public RtpConnection(String host, int port, List<Integer> payloadTypes)
     {
         _host = host;
         _port = port;
         _payloadTypes = payloadTypes;
     }
 
     public String getHost()
     {
         return _host;
     }
 
     public int getPort()
     {
         return _port;
     }
 
     public List<Integer> getPayloadTypes() {
         return _payloadTypes;
     }
 
 }
