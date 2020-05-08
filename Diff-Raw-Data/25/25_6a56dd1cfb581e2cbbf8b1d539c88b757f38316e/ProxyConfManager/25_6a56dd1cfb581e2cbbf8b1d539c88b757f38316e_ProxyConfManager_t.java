 package org.esbhive.proxyconf.mgt;
 
 /**
  *
  * @author jeewantha
  */
 import java.util.ArrayList;
 import java.util.HashMap;
 import org.esbhive.node.mgt.ESBNode;
 import org.wso2.carbon.proxyadmin.ProxyData;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 
 
 /**
  * @scr.component name="proxyconf.manager" immediate="true"
  * @scr.service interface="org.esbhive.proxyconf.mgt.ProxyConfManagerInterface"
  */
 
 public class ProxyConfManager implements ProxyConfManagerInterface{
 
    
    private static HashMap<String, ProEsb>   ProESBs = new HashMap<String, ProEsb>();
    private static HashMap<String, ProxyDataList>   PDlist = new HashMap<String, ProxyDataList>();
 
   //private static List<ProEsb>   nodes = new ArrayList<ProEsb>();
     private static final Log log2 = LogFactory.getLog("org.wso2.carbon.ProxyConfManager");
 
     public ProxyConfManager() {
       
     }
 
     public void addProxyConf(ProxyData prodata, ESBNode[] esbarray) {
         String name=prodata.getName();
         if(ProESBs.containsKey(name)){
             ProEsb proesb=ProESBs.get(name);
             proesb.addArrayToList(esbarray); //can have duplicate entries
             //ProESBs.remove(name);
             //ProESBs.put(name, proesb);
         }
         else{
 
             ProEsb proesb1 = new ProEsb();
             proesb1.addArrayToList(esbarray);
             proesb1.setProxyData(prodata);
 
             ProESBs.put(prodata.getName(), proesb1);
         }
 
         for(int i=0;i<esbarray.length;i++){
             String ipandport=esbarray[i].getIpAndPort();
            if(PDlist.containsKey(ipandport)){
                 ProxyDataList proxydatalist= PDlist.get(ipandport);
                 proxydatalist.addToList(prodata);//can have duplicate entries
             }
             else{
                 ProxyDataList proxydatalist1 = new ProxyDataList();
                 proxydatalist1.addToList(prodata);
 
                 PDlist.put(ipandport, proxydatalist1);
             }
         }
 
     }
 
    public ProEsb getProEsb(String proxyname){
         ProEsb proesb= ProESBs.get(proxyname);
         return proesb;
    }
 
    public ProxyDataList getProxyDataList(String ipandport){
         ProxyDataList prodatalist=PDlist.get(ipandport);
         return prodatalist;
    }
 
    public void deleteESB(String ipandport){
         PDlist.remove(ipandport);
 
         ProEsb[] proesb = new ProEsb[ProESBs.size()];
         proesb=ProESBs.values().toArray(proesb);
         for(int i=0;i<proesb.length;i++){
             ArrayList<ESBNode> arraylist=proesb[i].getESBNodesArrayList();
             ESBNode[] esbnodes= new ESBNode[arraylist.size()];
             esbnodes=arraylist.toArray(esbnodes);
             for(int j=0;j<esbnodes.length;j++){
                 if(esbnodes[j].getIpAndPort().equals(ipandport)){
                     arraylist.remove(j);//use proesb[i].setESBNodes() which returns a ProEsb
                     //get name of i and add name+ProEsb
                 }
             }
         }
    }
    public void deleteProxy(String proxyname){
         ProESBs.remove(proxyname);
 
         ProxyDataList[] pdatalist= new ProxyDataList[PDlist.size()];
         pdatalist=PDlist.values().toArray(pdatalist);
         for(int i=0;i<pdatalist.length;i++){
             ArrayList<ProxyData> arraylist=pdatalist[i].getProxyDataArrayList();
             ProxyData[] proxydatas= new ProxyData[arraylist.size()];
             proxydatas=arraylist.toArray(proxydatas);
             for(int j=0;j<proxydatas.length;j++){
                 if(proxydatas[j].getName().equals(proxyname)){
                     arraylist.remove(j);
                 }
             }
         }
    }
 
 
 /*    public ProEsb[] getlist() {
 	   
 	   ProEsb temProEsb=new ProEsb();
                 ProxyData[] prodata = new ProxyData[nodes.size()];
                 ESBNode[] esbnodes = new ESBNode[nodes.size()];
 
 		prodata=nodes.keySet().toArray(prodata);
                 esbnodes=nodes.values().toArray(esbnodes);
 
                 for(int i=0;i<proesb.length;i++){
                     temProEsb.setProxyData(prodata[i]);
                    String keys[] temProEsb.setESBNode(esbnodes[i]);
 		    proesb[i]=temProEsb;
                 }
 
             return proesb;
     }
  *
  * 
  */
 /*
         ProEsb[] proesb = new ProEsb[nodes.size()];
         ProxyData[] prodata = new ProxyData[nodes.size()];
         ESBNode[] esbnodes = new ESBNode[nodes.size()];
  *
  */
 
      //   prodata = nodes.keySet().toArray(prodata);
        // esbnodes = nodes.values().toArray(esbnodes);
 
 
     /*    for (int i = 0; i < proesb.length; i++) {
             proesb[i].setProxyData(prodata[i]);
             proesb[i].setESBNode(esbnodes[i]);
         }
      
          return new ProEsb[2];
 
 
 
     }
 
     public void deleteNode(String proxyname) {
      ProxyData proxydata = null;
             ProxyData [] proxyarray= new ProxyData[nodes.size()];
                     nodes.keySet().toArray(proxyarray);
             for(int i=0;i<nodes.size();i++){
                 if(proxyarray[i].getName().equals(proxyname)){
                     proxydata=proxyarray[i];
                     break;
                 }
             }
                     nodes.remove(proxydata);
 
     }
 
     public ESBNode[] getEsbNodeList() {
         ESBNode[] esbnodes = new ESBNode[1];
         esbnodes[0] = new ESBNode();
         return esbnodes;
     }
 
    public ProxyData[] getProxyDataList() {
         ProxyData[] prodata = new ProxyData[nodes.size()];
        // prodata = nodes.keySet().toArray(prodata);
         return  prodata;
     }
 
     *
      */
 }
