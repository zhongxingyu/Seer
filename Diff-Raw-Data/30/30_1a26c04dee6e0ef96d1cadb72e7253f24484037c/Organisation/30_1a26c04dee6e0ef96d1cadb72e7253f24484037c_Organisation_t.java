 package com.gkaakash.controller;
 
 import org.xmlrpc.android.XMLRPCException;
 
 import com.gkaakash.coreconnection.CoreConnection;
 
 public class Organisation {
    
     private CoreConnection conn;
     private boolean organisation;
     private Object[] projects;
     Object[] Params;
     private String orgtype;
     /***
      *
      */
     public Organisation() {
         conn = new CoreConnection();
     }
     /***
      * setOrganisation method will call xmlrpc_setOrganisation from rpc_organisation.py
      * @param ,client_id
      * @return
      */
     public boolean setOrganisation(Object[] params,Object client_id) {
        
         try {
            
             if(params[0].equals("NGO"))
             {
                 System.out.println("rg date:"+params[17]);
               Params=new Object[]{params[0],params[1],params[2],
                         params[3],params[4],params[5],params[6],params[7],
                         params[8],params[9],params[10],params[11],"","",
                         params[14],params[15],params[16],params[17]};
                 organisation = (Boolean)conn.getClient().call("organisation.setOrganisation",Params,client_id);
             }
             else
             {
               Params=new Object[]{params[0],params[1],params[2],
                             params[3],params[4],params[5],params[6],params[7],
                             params[8],params[9],params[10],params[11],params[12],
                             params[13],"","","",""};
                 organisation = (Boolean)conn.getClient().call("organisation.setOrganisation",Params,client_id);
             }
         } catch (XMLRPCException e) {
            
             e.printStackTrace();
         }
         return organisation;
     }
    
     /***
      * call getAllGroups method from core_engine groups.py
      * @param params client id
      * @return list contain gropucode , groupname , description
      */ 
     public Object getAllProjects(Object client_id) {
        
         try {
             System.out.println("wr are in projects");
             projects = (Object[])conn.getClient().call("organisation.getAllProjects",client_id);
             System.out.println("wr are in projects..."+projects);
         } catch (XMLRPCException e) {
            
             e.printStackTrace(); 
         }
         return projects;
     }
      
    /*
     * get organisation type by name
     */
 	public String getorgTypeByname(Object[] params,Object client_id) {
 	       
 	        try {
	            orgtype = (String)conn.getClient().call("organisation.getorgTypeByname",params,client_id);
 	        } catch (XMLRPCException e) {
 	           
 	            e.printStackTrace();
 	        }
 	        return orgtype;
 	    }
    
  
 }
