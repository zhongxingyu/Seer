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
 package com.axiastudio.suite.procedimenti.entities;
 
 import com.axiastudio.suite.anagrafiche.entities.Soggetto;
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.List;
 import javax.persistence.*;
 
 /**
  *
  * @author Tiziano Lattisi <tiziano at axiastudio.it>
  */
 @Entity
 @Table(schema="PROCEDIMENTI")
 @SequenceGenerator(name="genprocedimento", sequenceName="procedimenti.procedimento_id_seq", initialValue=1, allocationSize=1)
 public class Procedimento implements Serializable {
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="genprocedimento")
     private Long id;
     @Column(name="descrizione")
     private String descrizione;
     @Column(name="normativa")
     private String normativa;
     @Column(name="maxgiorniistruttoria")
     private Integer maxGiorniIstruttoria;
     @Enumerated(EnumType.STRING)
     private Iniziativa iniziativa;
     @JoinColumn(name = "soggetto", referencedColumnName = "id")
     @ManyToOne
     private Soggetto soggetto;
     @Column(name="attivo")
     private Boolean attivo=true;
     @OneToMany(mappedBy = "procedimento", orphanRemoval = true, cascade=CascadeType.ALL)
     private Collection<NormaProcedimento> normaProcedimentoCollection;
     @OneToMany(mappedBy = "procedimento", orphanRemoval = true, cascade=CascadeType.ALL)
     @OrderColumn(name="progressivo")
     private List<FaseProcedimento> faseProcedimentoCollection;
     @OneToMany(mappedBy = "procedimento", orphanRemoval = true, cascade=CascadeType.ALL)
     private Collection<UfficioProcedimento> ufficioProcedimentoCollection;
     @OneToMany(mappedBy = "procedimento", orphanRemoval = true, cascade=CascadeType.ALL)
     private Collection<UtenteProcedimento> utenteProcedimentoCollection;
     @OneToMany(mappedBy = "procedimento", orphanRemoval = true, cascade=CascadeType.ALL)
     private Collection<TipoPraticaProcedimento> tipopraticaProcedimentoCollection;
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getDescrizione() {
         return descrizione;
     }
 
     public void setDescrizione(String descrizione) {
         this.descrizione = descrizione;
     }
 
     public String getNormativa() {
         return normativa;
     }
 
     public void setNormativa(String normativa) {
         this.normativa = normativa;
     }
 
     public Integer getMaxGiorniIstruttoria() {
         return maxGiorniIstruttoria;
     }
 
     public void setMaxGiorniIstruttoria(Integer maxGiorniIstruttoria) {
         this.maxGiorniIstruttoria = maxGiorniIstruttoria;
     }
 
     public Iniziativa getIniziativa() {
         return iniziativa;
     }
 
     public void setIniziativa(Iniziativa iniziativa) {
         this.iniziativa = iniziativa;
     }
 
     public Soggetto getSoggetto() {
         return soggetto;
     }
 
     public void setSoggetto(Soggetto soggetto) {
         this.soggetto = soggetto;
     }
 
     public Boolean getAttivo() {
         return attivo;
     }
 
     public void setAttivo(Boolean attivo) {
         this.attivo = attivo;
     }
 
     public Collection<NormaProcedimento> getNormaProcedimentoCollection() {
         return normaProcedimentoCollection;
     }
 
     public void setNormaProcedimentoCollection(Collection<NormaProcedimento> normaProcedimentoCollection) {
         this.normaProcedimentoCollection = normaProcedimentoCollection;
     }
 
    public Collection<FaseProcedimento> getFaseProcedimentoCollection() {
         return faseProcedimentoCollection;
     }
 
     public void setFaseProcedimentoCollection(List<FaseProcedimento> faseProcedimentoCollection) {
         this.faseProcedimentoCollection = faseProcedimentoCollection;
     }
 
     public Collection<UfficioProcedimento> getUfficioProcedimentoCollection() {
         return ufficioProcedimentoCollection;
     }
 
     public void setUfficioProcedimentoCollection(Collection<UfficioProcedimento> ufficioProcedimentoCollection) {
         this.ufficioProcedimentoCollection = ufficioProcedimentoCollection;
     }
 
     public Collection<UtenteProcedimento> getUtenteProcedimentoCollection() {
         return utenteProcedimentoCollection;
     }
 
     public void setUtenteProcedimentoCollection(Collection<UtenteProcedimento> utenteProcedimentoCollection) {
         this.utenteProcedimentoCollection = utenteProcedimentoCollection;
     }
 
     public Collection<TipoPraticaProcedimento> getTipopraticaProcedimentoCollection() {
         return tipopraticaProcedimentoCollection;
     }
 
     public void setTipopraticaProcedimentoCollection(Collection<TipoPraticaProcedimento> tipoPraticaProcedimentoCollection) {
         this.tipopraticaProcedimentoCollection = tipoPraticaProcedimentoCollection;
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
         if (!(object instanceof Procedimento)) {
             return false;
         }
         Procedimento other = (Procedimento) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return this.getDescrizione();
     }
     
 }
