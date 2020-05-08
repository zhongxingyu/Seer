 /*
  * Copyright 2012 Danylo Vashchilenko
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package census.persistence;
 
 import java.io.Serializable;
 import java.util.Date;
 import javax.persistence.*;
 
 /**
  *
  * @author Danylo Vashchilenko
  */
 @Entity
 @Table(name = "client_freeze_cfz")
 @NamedQueries({
     @NamedQuery(name = "ClientFreeze.findAll", query = "SELECT c FROM ClientFreeze c"),
     @NamedQuery(name = "ClientFreeze.findById", query = "SELECT c FROM ClientFreeze c WHERE c.id = :id"),
     @NamedQuery(name = "ClientFreeze.findByClient", query = "SELECT c FROM ClientFreeze c WHERE c.client = :client"),
     @NamedQuery(name = "ClientFreeze.findByDateIssued", query = "SELECT c FROM ClientFreeze c WHERE c.dateIssued = :dateIssued"),
    @NamedQuery(name = "ClientFreeze.findByDateIssuedRange" , query = "SELECT c FROM ClientFreeze c WHERE c.dateIssued BETWEEN :rangeBegin AND :rangeEnd"),
    @NamedQuery(name = "ClientFreeze.findByClientAndDateIssuedRange" , query = "SELECT c FROM ClientFreeze c WHERE c.client = :client AND c.dateIssued BETWEEN :rangeBegin AND :rangeEnd")})
 public class ClientFreeze implements Serializable {
 
     private static final long serialVersionUID = 1L;
     
     @Id
     @GeneratedValue(strategy= GenerationType.IDENTITY)
     @Basic(optional = false)
     @Column(name = "id_cfz")
     private Short id;
             
     @Basic(optional = false)
     @Column(name = "date_issued")
     @Temporal(TemporalType.DATE)
     private Date dateIssued;
     
     @Basic(optional = false)
     @Column(name = "days")
     private Short days;
     
     @Basic(optional = false)
     @Lob
     @Column(name = "note")
     private String note;
     
     @JoinColumn(name = "idcln_cfz", referencedColumnName = "id_cln")
     @ManyToOne(optional = false)
     private Client client;
     
     @JoinColumn(name = "idadm_cfz", referencedColumnName = "id_adm")
     @ManyToOne(optional=false)
     private Administrator administrator;
     
 
     public ClientFreeze() {
     }
     
     public Short getId() {
         return id;
     }
 
     public void setId(Short id) {
         this.id = id;
     }
 
     public Client getClient() {
         return client;
     }
 
     public void setClient(Client client) {
         this.client = client;
     }
 
         public Date getDateIssued() {
         return dateIssued;
     }
 
     public void setDateIssued(Date dateIssued) {
         this.dateIssued = dateIssued;
     }
 
     public Short getDays() {
         return days;
     }
 
     public void setDays(Short days) {
         this.days = days;
     }
 
     public Administrator getAdministrator() {
         return administrator;
     }
 
     public void setAdministrator(Administrator administrator) {
         this.administrator = administrator;
     }
 
     public String getNote() {
         return note;
     }
 
     public void setNote(String note) {
         this.note = note;
     }
     
     @Override
     public int hashCode() {
         int hash = 0;
         hash += (id != null ? id.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object object) {
         // TODO: Warning - this method won't work in the case the id fields are not set
         if (!(object instanceof ClientFreeze)) {
             return false;
         }
         ClientFreeze other = (ClientFreeze) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "census.persistence.ClientFreeze[ id=" + id + " ]";
     }
 }
