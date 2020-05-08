 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.fiz.aas.services.auxiliaryobjects;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.ws.rs.core.Response;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.namespace.QName;
 
 /**
  *
  * @author bkl
  */
 
 @XmlRootElement(name = "users")
 @XmlAccessorType(XmlAccessType.NONE)
 //@XmlType(propOrder={"getList"})
 //@XmlType(factoryClass=JaxbStringList.class, factoryMethod="getList")
 //public class JaxbStringList<String> {
 public class JaxbStringList {
 
     protected List<String> _list;
 
     // ***************************************************************************
     public JaxbStringList() {
     }
 
     public JaxbStringList(List<String> list) {
         this._list = list;
         System.out.println("[" + this.getClass().getName() + "].create: " + this._list);
     }
 
     // ***************************************************************************
     @XmlElement(name = "uids")
     public Response getList() {
         System.out.println("[" + this.getClass().getName() + "].getList1: " + this._list.toString());
         //return Response.ok(this._list.toString()).build();
 
         return Response.ok(
            new JAXBElement<ArrayList>(new QName("ADDDDMIN"), ArrayList.class, (ArrayList<String>) this._list)).build();

     }
     /*
     public ArrayList<String> getList(){
       System.out.println("[" + this.getClass().getName() + "].getList2: " + this._list);
       return (ArrayList<String>) this._list;
     }
      */
 }
