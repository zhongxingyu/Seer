 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cz.muni.fi.fresneleditor.gui.mod.portal.model;
 
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  *
  * @author nodrock
  */
 @XmlRootElement(name="service")
 public class Service {
     private Integer id;
     private String name;
     private String url;
 
     public Service(Integer id, String name, String url) {
         this.id = id;
         this.name = name;
         this.url = url;
     }
 
     public Service() {
     }
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }    
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getUrl() {
         return url;
     }
 
     public void setUrl(String url) {
         this.url = url;
     }
     
     @Override
     public String toString() {
        return name; //"Service {" + " id = " + id + ", name = " + name + ", url = " + url + '}';
     }
 }
