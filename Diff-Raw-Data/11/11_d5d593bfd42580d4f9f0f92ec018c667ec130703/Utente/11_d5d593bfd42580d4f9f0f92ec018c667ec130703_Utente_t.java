 /*
  * Copyright (C) 2012 AXIA Studio (http://www.axiastudio.com)
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.axiastudio.suite.base.entities;
 
 import com.axiastudio.suite.procedimenti.entities.Delega;
 
 import javax.persistence.*;
 import java.io.Serializable;
 import java.util.Collection;
 
 /**
  *
  * @author Tiziano Lattisi <tiziano at axiastudio.it>
  */
 @Entity
 @Table(schema="BASE")
 @SequenceGenerator(name="genutente", sequenceName="base.utente_id_seq", initialValue=1, allocationSize=1)
 public class Utente implements Serializable, IUtente {
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="genutente")
     private Long id;
     @Column(name="nome")
     private String nome;
     @Column(name="sigla")
     private String sigla;
     @Column(name="email")
     private String email;
     @Column(name="login")
     private String login;
     @Column(name="password")
     private String password;
     @OneToMany(mappedBy = "utente", orphanRemoval = true, cascade=CascadeType.ALL)
     private Collection<UfficioUtente> ufficioUtenteCollection;
     @Column(name="amministratore")
     private Boolean amministratore=false;
     @Column(name="superutente")
     private Boolean superutente=false;
     @Column(name="operatoreanagrafiche")
     private Boolean operatoreanagrafiche=true;
     @Column(name="supervisoreanagrafiche")
     private Boolean supervisoreanagrafiche=false;
     @Column(name="operatoreprotocollo")
     private Boolean operatoreprotocollo=true;
     @Column(name="ricercatoreprotocollo")
     private Boolean ricercatoreprotocollo=false;
     @Column(name="attributoreprotocollo")
     private Boolean attributoreprotocollo=false;
     @Column(name="supervisoreprotocollo")
     private Boolean supervisoreprotocollo=false;
     @Column(name="operatorepratiche")
     private Boolean operatorepratiche=true;
     @Column(name="supervisorepratiche")
     private Boolean supervisorepratiche=false;
     @Column(name="modellatorepratiche")
     private Boolean modellatorepratiche=false;
     @Column(name="istruttorepratiche")
     private Boolean istruttorepratiche=false;
    @Column(name="disabilitato")
    private Boolean disabilitato=false;
     @OneToMany(mappedBy = "utente", orphanRemoval = true, cascade=CascadeType.ALL)
     private Collection<Delega> delegaCollection;
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getNome() {
         return nome;
     }
 
     public void setNome(String nome) {
         this.nome = nome;
     }
 
     public String getSigla() {
         return sigla;
     }
 
     public void setSigla(String sigla) {
         this.sigla = sigla;
     }
 
     public String getEmail() {
         return email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     public String getLogin() {
         return login;
     }
 
     public void setLogin(String login) {
         this.login = login;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public Collection<UfficioUtente> getUfficioUtenteCollection() {
         return ufficioUtenteCollection;
     }
 
     public void setUfficioUtenteCollection(Collection<UfficioUtente> ufficioUtenteCollection) {
         this.ufficioUtenteCollection = ufficioUtenteCollection;
     }
 
     public Boolean getAmministratore() {
         return amministratore;
     }
 
     public void setAmministratore(Boolean amministratore) {
         this.amministratore = amministratore;
     }
 
     public Boolean getOperatoreanagrafiche() {
         return operatoreanagrafiche;
     }
 
     public void setOperatoreanagrafiche(Boolean operatoreanagrafiche) {
         this.operatoreanagrafiche = operatoreanagrafiche;
     }
 
     public Boolean getSupervisoreanagrafiche() {
         return supervisoreanagrafiche;
     }
 
     public void setSupervisoreanagrafiche(Boolean supervisoreanagrafiche) {
         this.supervisoreanagrafiche = supervisoreanagrafiche;
     }
 
     public Boolean getOperatorepratiche() {
         return operatorepratiche;
     }
 
     public void setOperatorepratiche(Boolean operatorepratiche) {
         this.operatorepratiche = operatorepratiche;
     }
 
     public Boolean getOperatoreprotocollo() {
         return operatoreprotocollo;
     }
 
     public void setOperatoreprotocollo(Boolean operatoreprotocollo) {
         this.operatoreprotocollo = operatoreprotocollo;
     }
 
     public Boolean getRicercatoreprotocollo() {
         return ricercatoreprotocollo;
     }
 
     public void setRicercatoreprotocollo(Boolean ricercatoreprotocollo) {
         this.ricercatoreprotocollo = ricercatoreprotocollo;
     }
 
     public Boolean getAttributoreprotocollo() {
         return attributoreprotocollo;
     }
 
     public void setAttributoreprotocollo(Boolean attributoreprotocollo) {
         this.attributoreprotocollo = attributoreprotocollo;
     }
 
     public Boolean getSuperutente() {
         return superutente;
     }
 
     public void setSuperutente(Boolean superutente) {
         this.superutente = superutente;
     }
 
     public Boolean getSupervisorepratiche() {
         return supervisorepratiche;
     }
 
     public void setSupervisorepratiche(Boolean supervisorepratiche) {
         this.supervisorepratiche = supervisorepratiche;
     }
 
     public Boolean getModellatorepratiche() {
         return modellatorepratiche;
     }
 
     public void setModellatorepratiche(Boolean modellatorepratiche) {
         this.modellatorepratiche = modellatorepratiche;
     }
 
     public Boolean getSupervisoreprotocollo() {
         return supervisoreprotocollo;
     }
 
     public void setSupervisoreprotocollo(Boolean supervisoreprotocollo) {
         this.supervisoreprotocollo = supervisoreprotocollo;
     }
 
     public Collection<Delega> getDelegaCollection() {
         return delegaCollection;
     }
 
     public void setDelegaCollection(Collection<Delega> delegaCollection) {
         this.delegaCollection = delegaCollection;
     }
 
     public Boolean getIstruttorepratiche() {
         return istruttorepratiche;
     }
 
     public void setIstruttorepratiche(Boolean istruttorepratiche) {
         this.istruttorepratiche = istruttorepratiche;
     }
 
    public Boolean getDisabilitato() {
        return disabilitato;
    }

    public void setDisabilitato(Boolean disabilitato) {
        this.disabilitato = disabilitato;
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
         if (!(object instanceof Utente)) {
             return false;
         }
         Utente other = (Utente) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return this.getNome();
     }
     
 }
