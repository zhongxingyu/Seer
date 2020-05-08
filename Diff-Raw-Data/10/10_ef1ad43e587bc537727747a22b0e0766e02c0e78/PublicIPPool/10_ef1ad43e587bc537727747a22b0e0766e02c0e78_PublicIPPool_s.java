 /**
  * Copyright (C) 2011-2012 enStratus Networks Inc
  *
  * ====================================================================
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * ====================================================================
  */
 
 package org.dasein.cloud.opsource.network;
 
 import org.dasein.cloud.CloudException;
 import org.dasein.cloud.InternalException;
 import org.dasein.cloud.compute.VirtualMachine;
 import org.dasein.cloud.network.AddressType;
 import org.dasein.cloud.network.IpAddress;
 import org.dasein.cloud.network.RawAddress;
 import org.dasein.cloud.opsource.OpSource;
 import org.dasein.cloud.opsource.OpSourceMethod;
 import org.dasein.cloud.opsource.Param;
 import org.w3c.dom.*;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 
 public class PublicIPPool {
     private OpSource provider;
 
     public PublicIPPool(OpSource provider){
         this.provider = provider;
     }
 
     public String addPublicIpBlockToVLan(String providerVlanId) throws InternalException, CloudException{
         HashMap<Integer, Param> parameters = new HashMap<Integer, Param>();
 
         Param param = new Param(OpSource.NETWORK_BASE_PATH, null);
         parameters.put(0, param);
 
         param = new Param(providerVlanId, null);
         parameters.put(1, param);
 
         param = new Param("publicip", null);
         parameters.put(2, param);
 
         OpSourceMethod method = new OpSourceMethod(provider,
                 provider.buildUrl("reserveNewWithSize",true, parameters),
                 provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET", null));
         Document doc = method.invoke();
         Node docElement = doc.getDocumentElement();
         String sNS = "";
         try{
             sNS = docElement.getNodeName().substring(0, docElement.getNodeName().indexOf(":") + 1);
         }
         catch(IndexOutOfBoundsException ex){}
 
         NodeList response = doc.getDocumentElement().getChildNodes();
         for(int i=0;i<response.getLength();i++){
             Node additionalInformation = response.item(i);
 
             if(additionalInformation.getNodeType() == Node.TEXT_NODE)continue;
             else if(additionalInformation.getNodeName().equals(sNS + "additionalInformation")){
                 NamedNodeMap attributes = additionalInformation.getAttributes();
                 Node infoName = attributes.getNamedItem("name");
                 if(infoName.getNodeValue().equals("ipBlock.id")){
                     NodeList values = additionalInformation.getChildNodes();
                     for(int j=0;j<values.getLength();j++){
                         Node value = values.item(j);
                         if(value.getNodeName().equals(sNS + "value"))return value.getFirstChild().getNodeValue();
                     }
                 }
             }
         }
         throw new InternalException("Failed to allocate IP Block for unknown reason");
     }
 
     public void releasePublicIpBlock(String providerVlanId, String ipBlockId) throws InternalException, CloudException{
         HashMap<Integer, Param> parameters = new HashMap<Integer, Param>();
 
         Param param = new Param(OpSource.NETWORK_BASE_PATH, null);
         parameters.put(0, param);
 
         param = new Param(providerVlanId, null);
         parameters.put(1, param);
 
         param = new Param("publicip", null);
         parameters.put(2, param);
 
         param = new Param(ipBlockId, null);
         parameters.put(3, param);
 
         OpSourceMethod method = new OpSourceMethod(provider,
                 provider.buildUrl("release",true, parameters),
                 provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET", null));
         method.parseRequestResult("Releasing IP Block", method.invoke(), "result", "resultDetail");
     }
 
     public Collection<PublicIPBlock> listPublicIpBlocksForVLan(String providerVlanId)throws InternalException, CloudException{
         ArrayList<PublicIPBlock> ipBlocks = new ArrayList<PublicIPBlock>();
         HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
 
         Param param = new Param("network", null);
         parameters.put(0, param);
 
         param = new Param(providerVlanId, null);
         parameters.put(1, param);
 
         param = new Param("config", null);
         parameters.put(2, param);
 
         OpSourceMethod method = new OpSourceMethod(provider,
                 provider.buildUrl(null,true, parameters),
                 provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET", null));
 
         Document doc = method.invoke();
         String sNS = "";
         try{
             sNS = doc.getDocumentElement().getTagName().substring(0, doc.getDocumentElement().getTagName().indexOf(":") + 1);
         }
         catch(IndexOutOfBoundsException ex){}
         NodeList matches = doc.getElementsByTagName(sNS + "publicIps");
         if(matches != null){
             for( int i=0; i<matches.getLength(); i++ ) {
                 Node item = matches.item(i);
                 if(item.getNodeType() == Node.TEXT_NODE) continue;
                 NodeList blocks = item.getChildNodes();
                 for(int j = 0;j <blocks.getLength(); j++ ){
                     Node node = blocks.item(j);
                     if(node.getNodeType() == Node.TEXT_NODE) continue;
                     else if(node.getNodeName().equals(sNS + "IpBlock")){
                         String currentId = "";
                         String currentBaseIp = "";
                         int currentBlockSize = 0;
                         boolean currentNetworkDefault = false;
 
                         NodeList currentBlock = node.getChildNodes();
                         for(int k=0;k<currentBlock.getLength();k++){
                             Node blockInfo = currentBlock.item(k);
                             if(blockInfo.getNodeType() == Node.TEXT_NODE)continue;
                             else if(blockInfo.getNodeName().equals(sNS + "id")){
                                 currentId = blockInfo.getFirstChild().getNodeValue();
                             }
                             else if(blockInfo.getNodeName().equals(sNS + "baseIp")){
                                 currentBaseIp = blockInfo.getFirstChild().getNodeValue();
                             }
                             else if(blockInfo.getNodeName().equals(sNS + "subnetSize")){
                                 currentBlockSize = Integer.parseInt(blockInfo.getFirstChild().getNodeValue());
                             }
                             else if(blockInfo.getNodeName().equals(sNS + "networkDefault")){
                                 currentNetworkDefault = Boolean.parseBoolean(blockInfo.getFirstChild().getNodeValue());
                             }
                         }
                         ArrayList<IpAddress> ips = getIPs(currentBaseIp, currentBlockSize);
 
                         //Attempt to mark assigned IPs
                         ArrayList<String> assignedIPs = new ArrayList<String>();
                         try{
                             ArrayList<NatRule> rules = (ArrayList<NatRule>)listNatRules(providerVlanId);
                             for(NatRule rule : rules){
                                 for(IpAddress ip : ips){
                                     if(rule.getNatIp().equals(ip.getRawAddress().getIpAddress())){
                                         assignedIPs.add(rule.getNatIp());
                                     }
                                 }
                             }
                         }
                         catch(Exception ex){}
 
                         PublicIPBlock block = new PublicIPBlock(currentId, providerVlanId, ips, currentNetworkDefault, assignedIPs);
                        if(assignedIPs == null)System.out.println("AssignedIPs is null");
                        else System.out.println("Assigned count: " + assignedIPs.size());
                         ipBlocks.add(block);
                     }
                 }
             }
         }
         return ipBlocks;
     }
 
     public PublicIPBlock getPublicIpBlock(String providerVlanId, String publicIpBlockId) throws CloudException, InternalException{
         HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
 
         Param param = new Param("network", null);
         parameters.put(0, param);
 
         param = new Param(providerVlanId, null);
         parameters.put(1, param);
 
         param = new Param("config", null);
         parameters.put(2, param);
 
         OpSourceMethod method = new OpSourceMethod(provider,
                 provider.buildUrl(null,true, parameters),
                 provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET", null));
 
         Document doc = method.invoke();
         String sNS = "";
         try{
             sNS = doc.getDocumentElement().getTagName().substring(0, doc.getDocumentElement().getTagName().indexOf(":") + 1);
         }
         catch(IndexOutOfBoundsException ex){}
         NodeList matches = doc.getElementsByTagName(sNS + "publicIps");
 
         boolean requiredBlock = false;
         if(matches != null){
             for( int i=0; i<matches.getLength(); i++ ) {
                 Node item = matches.item(i);
                 if(item.getNodeType() == Node.TEXT_NODE) continue;
                 NodeList blocks = item.getChildNodes();
                 for(int j = 0;j <blocks.getLength(); j++ ){
                     Node node = blocks.item(j);
                     if(node.getNodeType() == Node.TEXT_NODE) continue;
                     else if(node.getNodeName().equals(sNS + "IpBlock")){
                         String currentId = "";
                         String currentBaseIp = "";
                         int currentBlockSize = 0;
                         boolean currentNetworkDefault = false;
 
                         NodeList currentBlock = node.getChildNodes();
                         for(int k=0;k<currentBlock.getLength();k++){
                             Node blockInfo = currentBlock.item(k);
                             if(blockInfo.getNodeType() == Node.TEXT_NODE)continue;
                             else if(blockInfo.getNodeName().equals(sNS + "id")){
                                 if(publicIpBlockId.equals(blockInfo.getFirstChild().getNodeValue())){
                                     requiredBlock = true;
                                     currentId = blockInfo.getFirstChild().getNodeValue();
                                 }
                                 else{
                                     requiredBlock = false;
                                     continue;
                                 }
                             }
                             else if(blockInfo.getNodeName().equals(sNS + "baseIp") && requiredBlock){
                                 currentBaseIp = blockInfo.getFirstChild().getNodeValue();
                             }
                             else if(blockInfo.getNodeName().equals(sNS + "subnetSize") && requiredBlock){
                                 currentBlockSize = Integer.parseInt(blockInfo.getFirstChild().getNodeValue());
                             }
                             else if(blockInfo.getNodeName().equals(sNS + "networkDefault")){
                                 currentNetworkDefault = Boolean.parseBoolean(blockInfo.getFirstChild().getNodeValue());
                             }
                         }
                         if(requiredBlock){
                             ArrayList<IpAddress> ips = getIPs(currentBaseIp, currentBlockSize);
                             //Attempt to mark assigned IPs
                            ArrayList<String> assignedIPs = null;
                             try{
                                 ArrayList<NatRule> rules = (ArrayList<NatRule>)listNatRules(providerVlanId);
                                 for(NatRule rule : rules){
                                     for(IpAddress ip : ips){
                                        if(rule.getNatIp().equals(ip.getRawAddress().getIpAddress()))assignedIPs.add(rule.getNatIp());
                                     }
                                 }
                             }
                             catch(Exception ex){}
                             PublicIPBlock block = new PublicIPBlock(currentId, providerVlanId, ips, currentNetworkDefault, assignedIPs);
                             return block;
                         }
                     }
                 }
             }
         }
         return null;
     }
 
     public ArrayList<IpAddress> getIPs(String currentBaseIp, int currentBlockSize){
         String firstPart = currentBaseIp.substring(0, currentBaseIp.lastIndexOf(".")+1);
         int lastPart = Integer.parseInt(currentBaseIp.substring(currentBaseIp.lastIndexOf(".")+1));
         ArrayList<IpAddress> ips = new ArrayList<IpAddress>();
         for(int i=0;i<currentBlockSize;i++){
             IpAddress ip = new IpAddress();
             ip.setAddressType(AddressType.PUBLIC);
             ip.setAddress(firstPart + lastPart);
             ips.add(ip);
             lastPart++;
         }
         return ips;
     }
 
     public String assignPublicIp(String virtualMachineId)throws InternalException, CloudException{
         VirtualMachine vm = provider.getComputeServices().getVirtualMachineSupport().getVirtualMachine(virtualMachineId);
         RawAddress[] privateAddresses = vm.getPrivateAddresses();
         if(privateAddresses != null && privateAddresses.length > 0){
             String vmPrivateIP = privateAddresses[0].getIpAddress();
 
             String nameSpace = "ns4:";
             Document doc = provider.createDoc();
             Element natRuleEl = doc.createElementNS("http://oec.api.opsource.net/schemas/network", nameSpace + "NatRule");
 
             Element nameElmt = doc.createElement(nameSpace + "name");
             nameElmt.setTextContent(vmPrivateIP);
 
             Element sourceIpElmt = doc.createElement(nameSpace + "sourceIp");
             sourceIpElmt.setTextContent(vmPrivateIP);
 
             natRuleEl.appendChild(nameElmt);
             natRuleEl.appendChild(sourceIpElmt);
             doc.appendChild(natRuleEl);
 
             HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
             Param param = new Param(OpSource.NETWORK_BASE_PATH, null);
             parameters.put(0, param);
             param = new Param(vm.getProviderVlanId(), null);
             parameters.put(1, param);
 
             param = new Param("natrule", null);
             parameters.put(2, param);
 
             OpSourceMethod method = new OpSourceMethod(provider,
                     provider.buildUrl(null,true, parameters),
                     provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "POST", provider.convertDomToString(doc)));
 
             Document responseDoc =  method.invoke();
 
             String sNS = "";
             try{
                 sNS = responseDoc.getDocumentElement().getTagName().substring(0, doc.getDocumentElement().getTagName().indexOf(":") + 1);
             }
             catch(IndexOutOfBoundsException ex){}
 
             NodeList attributes = responseDoc.getDocumentElement().getChildNodes();
             for(int i=0;i<attributes.getLength();i++){
                 Node node = attributes.item(i);
                 if(node.getNodeType() == Node.TEXT_NODE)continue;
                 else if(node.getNodeName().equalsIgnoreCase(sNS + "natIp")){
                     return node.getFirstChild().getNodeValue().trim();
                 }
             }
         }
         throw new InternalException("An error occured allocating a public IP address");
     }
 
     public void detachPublicIp(String providerVlanId, String providerServerId)throws InternalException, CloudException{
         ArrayList<NatRule> natRules = (ArrayList<NatRule>)listNatRules(providerVlanId);
         VirtualMachine vm = provider.getComputeServices().getVirtualMachineSupport().getVirtualMachine(providerServerId);
 
         NatRule rule = null;
         for(int i=0;i<natRules.size();i++){
             NatRule currentRule = natRules.get(i);
             RawAddress[] publicAddresses = vm.getPublicAddresses();
             if(publicAddresses != null){
                 for(RawAddress address : publicAddresses){
                     if(currentRule.getNatIp().equals(address.getIpAddress())){
                         rule = currentRule;
                         break;
                     }
                 }
             }
         }
         if(rule != null){
             HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
             Param param = new Param(OpSource.NETWORK_BASE_PATH, null);
             parameters.put(0, param);
             param = new Param(rule.getVlanId(), null);
             parameters.put(1, param);
 
             param = new Param("natrule", null);
             parameters.put(2, param);
 
             param = new Param(rule.getId(), null);
             parameters.put(3, param);
 
             OpSourceMethod method = new OpSourceMethod(provider,
                     provider.buildUrl("delete",true, parameters),
                     provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET", null));
 
             method.requestResult("Release Ip from server", method.invoke());
         }
         else{
             throw new InternalException("An error occured detaching IP from server");
         }
     }
 
     public Collection<NatRule> listNatRules(String providerVlanId)throws InternalException, CloudException{
         ArrayList<NatRule> list = new ArrayList<NatRule>();
 
         HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
         Param param = new Param(OpSource.NETWORK_BASE_PATH, null);
         parameters.put(0, param);
         param = new Param(providerVlanId, null);
         parameters.put(1, param);
 
         param = new Param("natrule", null);
         parameters.put(2, param);
 
         OpSourceMethod method = new OpSourceMethod(provider,
                 provider.buildUrl(null,true, parameters),
                 provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET",null));
 
         Document doc =  method.invoke();
 
         String sNS = "";
         try{
             sNS = doc.getDocumentElement().getTagName().substring(0, doc.getDocumentElement().getTagName().indexOf(":") + 1);
         }
         catch(IndexOutOfBoundsException ex){}
         NodeList matches = doc.getElementsByTagName(sNS + "NatRule");
         if(matches != null){
             for(int i = 0; i< matches.getLength();i++){
                 Node node = matches.item(i);
                 NatRule rule = toNatRule(node, sNS);
                 if(rule != null){
                     rule.setVlanId(providerVlanId);
                     list.add(rule);
                 }
             }
         }
         return list;
     }
 
     private NatRule toNatRule(Node node, String nameSpace){
         if(node == null){
             return null;
         }
         NatRule rule = new NatRule();
 
         NodeList attributes = node.getChildNodes();
 
         for( int i=0; i<attributes.getLength(); i++ ) {
             Node n = attributes.item(i);
             String name = n.getNodeName();
             String value;
 
             if( n.getChildNodes().getLength() > 0 ) {
                 value = n.getFirstChild().getNodeValue();
             }
             else {
                 continue;
             }
 
             if( name.equalsIgnoreCase(nameSpace + "id") ) {
                 rule.setId(value);
             }
             else if( name.equalsIgnoreCase(nameSpace + "natIp") ) {
                 rule.setNatIp(value);
             }
             else if( name.equalsIgnoreCase(nameSpace + "sourceIp") ) {
                 rule.setSourceIp(value);
             }
         }
 
         if(rule != null && rule.getId() != null && rule.getNatIp() != null && rule.getSourceIp() != null){
             return rule;
         }else{
             return null;
         }
     }
 
     public class PublicIPBlock{
         String id;
         String vlanId;
         ArrayList<IpAddress> addresses;
         boolean networkDefault = false;
         ArrayList<String> assignedIPs;
 
         PublicIPBlock(String id, String vlanId, ArrayList<IpAddress> addresses, boolean networkDefault, ArrayList<String> assignedIPs){
             this.id = id;
             this.vlanId = vlanId;
             this.addresses = addresses;
             this.networkDefault = networkDefault;
             this.assignedIPs = assignedIPs;
         }
 
         public String getId(){
             return id;
         }
 
         public String getVlanId(){
             return vlanId;
         }
 
         public ArrayList<IpAddress> getAddresses(){
             return addresses;
         }
 
         public boolean getNetworkDefault(){
             return networkDefault;
         }
 
         public ArrayList<String> getAssignedIPs(){
             return assignedIPs;
         }
     }
 
     public class NatRule{
         String id;
         String natIp;
         String sourceIp;
         String vlanId;
         String vmId;
 
         NatRule(){}
 
         NatRule(String id, String natIp, String sourceIp, String vlanId, String vmId){
             this.id = id;
             this.natIp = natIp;
             this.sourceIp = sourceIp;
             this.vlanId = vlanId;
             this.vmId = vmId;
         }
         public String getId(){
             return id;
         }
         public String getNatIp(){
             return this.natIp;
         }
         public String getSourceIp(){
             return this.sourceIp;
         }
         public void setId(String id){
             this.id = id;
         }
         public void setNatIp(String natIp){
             this.natIp = natIp;
         }
         public void setSourceIp(String sourceIp){
             this.sourceIp = sourceIp;
         }
         public void setVlanId(String vlanId){
             this.vlanId = vlanId;
         }
         public String getVlanId(){
             return vlanId;
         }
         public void setVmId(String vmId){
             this.vmId = vmId;
         }
         public String getVmId(){
             return vmId;
         }
     }
 }
