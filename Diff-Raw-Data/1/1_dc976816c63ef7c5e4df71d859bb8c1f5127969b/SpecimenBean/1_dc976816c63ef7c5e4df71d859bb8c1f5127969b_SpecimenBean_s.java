 /* Ara - capture species and specimen data
  * 
  * Copyright (C) 2009  INBio ( Instituto Naciona de Biodiversidad )
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.inbio.ara.facade.specimen;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.LinkedList;
 import java.util.List;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.persistence.EntityExistsException;
 import javax.persistence.EntityManager;
 import javax.persistence.FlushModeType;
 import javax.persistence.NoResultException;
 import javax.persistence.NonUniqueResultException;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 import javax.persistence.TransactionRequiredException;
 import org.inbio.ara.dto.LifeStageSexDTO;
 import org.inbio.ara.dto.factory.LifeStageSexDTOFactory;
 import org.inbio.ara.eao.SpecimenLifeStageSexLocalEAO;
 import org.inbio.ara.eao.SpecimenLocalEAO;
 import org.inbio.ara.persistence.gathering.GatheringObservationDetail;
 import org.inbio.ara.persistence.gathering.GatheringObservationMethod;
 import org.inbio.ara.persistence.gathering.MorphologicalDescription;
 import org.inbio.ara.persistence.specimen.DwcCategory;
 import org.inbio.ara.persistence.specimen.DwcElement;
 import org.inbio.ara.persistence.specimen.ExtractionType;
 import org.inbio.ara.persistence.specimen.LifeStage;
 import org.inbio.ara.persistence.specimen.Origin;
 import org.inbio.ara.persistence.specimen.PreservationMedium;
 import org.inbio.ara.persistence.specimen.Sex;
 import org.inbio.ara.persistence.specimen.Specimen;
 import org.inbio.ara.persistence.specimen.SpecimenCategory;
 import org.inbio.ara.persistence.specimen.SpecimenLifeStageSex;
 import org.inbio.ara.persistence.specimen.SpecimenLifeStageSexPK;
 import org.inbio.ara.persistence.specimen.SpecimenType;
 import org.inbio.ara.persistence.specimen.StorageType;
 import org.inbio.ara.persistence.specimen.Substrate;
 import org.inbio.ara.util.QueryNode;
 
 /**
  *
  * @author roaguilar
  */
 @Stateless
 public class SpecimenBean implements SpecimenRemote, SpecimenLocal {
 
     @PersistenceContext
     private EntityManager em;
     /**
      * @deprecated
      */
     private Specimen specimen;
     /**
      * @deprecated 
      */
     private List specimenList;
     /**
      * @deprecated 
      */
     private String message;
     public static final int FILTER_COLUMNS = 5;
     @EJB
     private SpecimenLocalEAO specimenEAO;
     @EJB
     private SpecimenLifeStageSexLocalEAO specimenLifeStageSexEAO;
 
     public List findAllDwC() {
         Query q = em.createQuery("from DarwinCore14 as o");
         List ret = (List) q.getResultList();
         return ret;
     }
 
     @Override
     public List findAllDwCPaginated(int first, int amount) {
 
 
         Query q = em.createQuery("from DarwinCore14 as o");
         q.setFirstResult(first);
         q.setMaxResults(amount);
 
         List ret = (List) q.getResultList();
         return ret;
     }
 
     
 
     public Specimen getSpecimenByCatalogNumber(Long cn) {
         return specimenEAO.getSpecimenByCatalogNumber(cn);
     }
 
     public Sex findSex(Long id) {
         return em.find(Sex.class, id);
     }
 
     /** Creates a new instance of SpecimenBean */
     public SpecimenBean() {
     }
 
     public Specimen getSpecimen() {
         return specimen;
     }
 
     public void setSpecimen(Specimen specimen) {
         this.specimen = specimen;
     }
 
     public DwcElement getDwCElementById(BigDecimal id) {
         return em.find(DwcElement.class, id);
     }
 
     public List<DwcCategory> getDwCCategories() {
         Query q = em.createQuery("from DwcCategory");
         List elements = (List) q.getResultList();
         return elements;
     }
 
     public void setSpecimenList(List specimenList) {
         this.specimenList = specimenList;
     }
 
     public String getMessage() {
         return message;
     }
 
     public void setMessage(String message) {
         this.message = message;
     }
 
     public boolean generate(SpecimenGenerator spGen) {
         List<Specimen> specimenList = spGen.getGeneratedSpecimens();
         if (specimenList.size() > 0) {
             for (Specimen tmpSp : specimenList) {
                 create(tmpSp);
             }
             return true;
         } else {
             setMessage(spGen.getMessage());
             return false;
         }
     }
 
     public boolean persist() {
         boolean persisted = false;
         if (!isSpecimenNull(specimen)) {
             if (this.isSpecimenUnique(specimen)) {
                 try {
                     // Incorporar las entidades auxiliares al contexto de la transacción
                     // Primero las obligatorias
                     //specimen.setProject(em.find(Project.class,specimen.getProject().getId()));
                     specimen.setSpecimenCategory(em.find(SpecimenCategory.class, specimen.getSpecimenCategory().getId()));
 
                     //specimen.setGathering(em.find(Gathering.class,specimen.getGathering().getId()));
                     //specimen.setCollection(em.find(Collection.class,specimen.getCollection().getId()));
                     // Luego las opcionales
                     if (specimen.getSpecimenType() != null) {
                         specimen.setSpecimenType(em.find(SpecimenType.class, specimen.getSpecimenType().getId()));
                     }
                     if (specimen.getStorageType() != null) {
                         specimen.setStorageType(em.find(StorageType.class, specimen.getStorageType().getId()));
                     }
                     if (specimen.getSubstrate() != null) {
                         specimen.setSubstrate(em.find(Substrate.class, specimen.getSubstrate().getId()));
                     }
                     if (specimen.getOrigin() != null) {
                         specimen.setOrigin(em.find(Origin.class, specimen.getOrigin().getId()));
                     }
                     if (specimen.getPreservationMedium() != null) {
                         specimen.setPreservationMedium(em.find(PreservationMedium.class, specimen.getPreservationMedium().getId()));
                     }
                     /*
                     if(specimen.getGatheringDetail()!=null) {
                     specimen.setGatheringDetail(em.find(GatheringDetail.class,specimen.getGatheringDetail().getGatheringDetailPK()));
                     }
                      */
                     if (specimen.getMorphologicalDescription() != null) {
                         specimen.setMorphologicalDescription(em.find(MorphologicalDescription.class, specimen.getMorphologicalDescription().getId()));
                     }
                     if (specimen.getLifeStage() != null) {
                         specimen.setLifeStage(em.find(LifeStage.class, specimen.getLifeStage().getId()));
                     }
                     if (specimen.getSex() != null) {
                         specimen.setSex(em.find(Sex.class, specimen.getSex().getId()));
                     }
                     /*
                     if(specimen.getExtractionType()!=null){
                     specimen.setExtractionType(em.find(ExtractionType.class,specimen.getExtractionType().getId()));
                     }
                      */
                     em.persist(this.specimen);
                     persisted = true;
                 } catch (EntityExistsException ex0) {
                     this.setMessage(ex0.getMessage());
                     return false;
                 } catch (IllegalStateException ex1) {
                     this.setMessage(ex1.getMessage());
                     return false;
                 } catch (IllegalArgumentException ex2) {
                     this.setMessage(ex2.getMessage());
                     return false;
                 } catch (TransactionRequiredException ex3) {
                     this.setMessage(ex3.getMessage());
                     return false;
                 }
             } else {
                 setMessage("El especimen ya existe en el sistema.");
                 persisted = false;
             }
         } else {
             this.setMessage(this.getMessage() + " El registro no fue creado.");
         }
         return persisted;
     }
 
     public boolean create(Specimen specimen) {
         setSpecimen(specimen);
         return persist();
     }
 
     public boolean update(Specimen specimen) {
         boolean updated = false;
         if (!isSpecimenNull(specimen)) {
             try {
                 if (em.isOpen()) {
                     this.specimen = specimen;
                     em.merge(this.specimen);
                     updated = true;
                 } else {
                     System.out.println("El EM esta cerrado.");
                 }
             } catch (IllegalStateException ex1) {
                 System.err.println(ex1.getMessage());
                 this.setMessage(ex1.getMessage());
                 return false;
             } catch (IllegalArgumentException ex2) {
                 System.err.println(ex2.getMessage());
                 this.setMessage(ex2.getMessage());
                 return false;
             } catch (TransactionRequiredException ex3) {
                 System.err.println(ex3.getMessage());
                 this.setMessage(ex3.getMessage());
                 return false;
             }
         } else {
             this.setMessage(this.getMessage() + " El registro no fue actualizado.");
         }
         return updated;
     }
 
     public boolean delete(Long specimenId) {
         try {
             // Buscar la entidad a borrar
             Specimen specimen = (Specimen) em.find(Specimen.class, specimenId);
             // Verificar si la entidad realmente existe
             if (specimen == null) {
                 setMessage("No existe un especímen asociado al Id " + specimenId);
                 return false;
             }
             // Incorporar la entidad al contexto de la transacción
             this.specimen = em.merge(specimen);
             if (canDeleteSpecimen()) {
                 // Eliminar la entidad
                 em.remove(this.specimen);
                 return true;
             } else {
                 return false;
             }
         } catch (IllegalStateException ex1) {
             this.setMessage(ex1.getMessage());
             return false;
         } catch (IllegalArgumentException ex2) {
             this.setMessage(ex2.getMessage());
             return false;
         } catch (TransactionRequiredException ex3) {
             this.setMessage(ex3.getMessage());
             return false;
         }
     }
 
     public boolean discard(Long specimenId) {
         Specimen specimen;
         specimen = em.find(Specimen.class, specimenId);
         specimen.setDiscarded("s");
         return update(specimen);
     }
 
     public boolean isSpecimenNull(Specimen specimen) {
         /*
         if (specimen.getProject()==null) {
         setMessage("Falta el proyecto.");
         return true;
         }*/
         /*
         if (specimen.getGathering()==null) {
         setMessage("Falta la recolección");
         return true;
         }*/
         if (specimen.getSpecimenCategory() == null) {
             setMessage("Falta la categoría");
             return true;
         }
         /*
         if (specimen.getCollection()==null){
         setMessage("Falta la colección");
         return true;
         }*/
         return false;
     }
 
     public boolean isSpecimenUnique(Specimen specimen) {
         boolean isUnique = true;
         String hql;
         Long tmpId;
 
         hql = "Select o from Specimen as o ";
         hql += "where o.id = " + specimen.getId();
 
         if (em.createQuery(hql).getResultList().size() > 0) {
             isUnique = false;
         }
         return isUnique;
     }
 
     public boolean canDeleteSpecimen() {
         /*
          * El usuario puede eliminar un registro físico de espécimen,
         solamente en el caso en que no haya sido generada información
         asociada, tal como: descripción morfológica, componentes,
         identificaciones, transacciones, imágenes y anotaciones.
          */
         if (getSpecimen().getMorphologicalDescription() != null) {
             setMessage("El espécimen no puede ser borrado pues tiene descripciones morfológicas.");
             return false;
         }
         if (getSpecimen().getComponentSet().size() > 0) {
             setMessage("El espécimen no puede ser borrado pues tiene componentes asociados.");
             return false;
         }
         if (getSpecimen().getIdentificationSet().size() > 0) {
             setMessage("El espécimen no puede ser borrado pues tiene identificaciones asociadas.");
             return false;
         }
         if (getSpecimen().getTransactedSpecimenSet().size() > 0) {
             setMessage("El espécimen no puede ser borrado pues está siendo referenciado en un transacción.");
             return false;
         }
         return true;
     }
 
     /**
      * @deprecated este metdo debe estar en un EAO [jgutierrez]
      *
      * @param specimenId
      * @return
      */
     public Specimen find(Long specimenId) {
         String queryString = "from Specimen as o where o.id = " + specimenId;
 
         Query q = em.createQuery(queryString);
         try {
             this.specimen = (Specimen) q.getSingleResult();
         //this.setTaxonDescription((TaxonDescription) q.getSingleResult());
         } catch (NoResultException noResultEx) {
             this.setMessage("No se encontraron registros para el Id dado");
             return null;
         } catch (NonUniqueResultException noUniqueResultEx) {
             this.setMessage("La consulta produjo mas de un resultado");
             return null;
         } catch (IllegalStateException stateException) {
             this.setMessage("Error en la consulta");
             return null;
         }
         return this.specimen;
     }
 
     /**
      * @deprecated
      * @param specimenId
      * @return
      */
     public List getSpecimenLifeStageSex(Long specimenId) {
         Query q;
         try {
             Specimen sp = this.find(specimenId);
             //FIXME: esto esta alambrado!!! asco!
             if (sp.getSpecimenCategory().getId().equals(2L)) {
                 // Obtener los datos directamente de la entidad specimen
                 SpecimenLifeStageSex tmp = new SpecimenLifeStageSex();
                 tmp.setLifeStage(sp.getLifeStage());
                 tmp.setSex(sp.getSex());
                 tmp.setSpecimen(sp);
                 tmp.setQuantity(1L);
                 tmp.postLoad();
                 List lst = new ArrayList();
                 lst.add(tmp);
                 return lst;
             } else {
                 // Obtener los datos de SpecimenLifeStageSex
                 q = em.createQuery("Select object(o) from SpecimenLifeStageSex as o where o.specimenLifeStageSexPK.specimenId = " + specimenId);
                 return q.getResultList();
             }
         } catch (IllegalStateException ex1) {
             this.setMessage(ex1.getMessage());
             return null;
         } catch (IllegalArgumentException ex2) {
             this.setMessage(ex2.getMessage());
             return null;
         }
     }
 
     public SpecimenCategory findSpecimenCategory(Long id) {
         return em.find(SpecimenCategory.class, id);
     }
 
     public ExtractionType findExtractionType(Long id) {
         return em.find(ExtractionType.class, id);
     }
 
     public SpecimenType findSpecimenType(Long id) {
         return em.find(SpecimenType.class, id);
     }
 
     public Origin findOrigin(Long id) {
         return em.find(Origin.class, id);
     }
 
     public PreservationMedium findPreservationMedium(Long id) {
         return em.find(PreservationMedium.class, id);
     }
 
     public Substrate findSubstrate(Long id) {
         return em.find(Substrate.class, id);
     }
 
     public StorageType findStorageType(Long id) {
         return em.find(StorageType.class, id);
     }
 
     public GatheringObservationMethod findGOMethod(Long id) {
         return em.find(GatheringObservationMethod.class, id);
     }
 
     public GatheringObservationDetail findGOD(Long id) {
         return em.find(GatheringObservationDetail.class, id);
     }
 
     @Override
     public List findAll() {
         em.setFlushMode(FlushModeType.COMMIT);
         Query q = em.createQuery("from Specimen as o");
         this.specimenList = (List) q.getResultList();
         em.setFlushMode(FlushModeType.AUTO);
         return this.specimenList;
     }
 
     public LifeStage findLifeStage(Long aLong) {
         return em.find(LifeStage.class, aLong);
     }
 
     public boolean saveSpecimenLifeStageSexList(List<LifeStageSexSimple> stageSexList) {
 
         List<SpecimenLifeStageSex> list = getSpecimenLifeStageSex(this.getSpecimen().getId());
 
         for (SpecimenLifeStageSex obj : list) {
             em.remove(obj);
             em.flush();
         }
 
         System.out.println("Borrados todos--------------------------------");
 
 
         List<LifeStageSexSimple> list2 = stageSexList;
 
         for (LifeStageSexSimple obj : list2) {
 
             SpecimenLifeStageSexPK pk = new SpecimenLifeStageSexPK();
             pk.setSpecimenId(specimen.getId());
             pk.setLifeStageId(obj.getLifeStage().getId());
             pk.setSexId(obj.getSex().getId());
 
             SpecimenLifeStageSex specimenLifeStageSex = new SpecimenLifeStageSex(pk);
             specimenLifeStageSex.setQuantity(obj.getQuantity());
             specimenLifeStageSex.setCreatedBy(specimen.getCreatedBy());
             specimenLifeStageSex.setLastModificationBy(specimen.getCreatedBy());
 
             em.merge(specimenLifeStageSex);
             em.flush();
         }
 
         return true;
     }
 
     public List getDwCRecords() {
         Query q = em.createNativeQuery("SELECT column_name FROM information_schema.columns WHERE table_name = 'darwin_core_1_4' order by column_name;");
         List elements = (List) q.getResultList();
         return elements;
     }
 
     public List<DwcElement> getDwCElements() {
         Query q = em.createQuery("from DwcElement");
         List elements = (List) q.getResultList();
         return elements;
     }
 
     public List<DwcElement> categoryElements(BigDecimal catId) {
         Query q = em.createQuery("from DwcElement as o where o.elementCategoryId.categoryId = " + catId);
         List elements = (List) q.getResultList();
         return elements;
     }
 
     public List makeQuery(LinkedList<QueryNode> sll) {
         String jpqlQuery = "from DarwinCore14 as o where ";
 
         //Mandatory
         QueryNode qn = sll.getFirst();
         jpqlQuery += "lower(o." + qn.getDwcElement() + ")";
         jpqlQuery += " " + qn.getComparator() + " ";
         if (qn.getComparator().equals("like")) {
             jpqlQuery += "'%" + qn.getUserEntry().toLowerCase() + "%'";
         } else {
             jpqlQuery += "'" + qn.getUserEntry().toLowerCase() + "'";
         }
 
         //Optional
         for (int i = 1; i < sll.size(); i++) {
             qn = sll.get(i);
             jpqlQuery += " " + qn.getLogicalOperator() + " ";
             jpqlQuery += "lower(o." + qn.getDwcElement() + ")";
             jpqlQuery += " " + qn.getComparator() + " ";
             if (qn.getComparator().equals("like")) {
                 jpqlQuery += "'%" + qn.getUserEntry().toLowerCase() + "%'";
             } else {
                 jpqlQuery += "'" + qn.getUserEntry().toLowerCase() + "'";
             }
         }
         /*
         for (QueryNode queryNode : sll) {
         if (queryNode.getLogicalOperator() != null) {
         jpqlQuery += " " + queryNode.getLogicalOperator() + " ";
         }
         jpqlQuery += "o." + queryNode.getDwcElement();
         jpqlQuery += " " + queryNode.getComparator() + " ";
         if (queryNode.getComparator() == "like") {
         jpqlQuery += "'%" + queryNode.getUserEntry() + "%'";
         } else {
         jpqlQuery += "'" + queryNode.getUserEntry() + "'";
         }
         }*/
 
         System.out.println(jpqlQuery);
         Query q = em.createQuery(jpqlQuery);
         List elements = (List) q.getResultList();
         return elements;
     }
 
     @Override
     public List makePaginatedQuery(LinkedList<QueryNode> sll, int first, int amount) {
         String jpqlQuery = "from DarwinCore14 as o where ";
 
         //Mandatory
         QueryNode qn = sll.getFirst();
         jpqlQuery += "lower(o." + qn.getDwcElement() + ")";
         jpqlQuery += " " + qn.getComparator() + " ";
         if (qn.getComparator().equals("like")) {
             jpqlQuery += "'%" + qn.getUserEntry().toLowerCase() + "%'";
         } else {
             jpqlQuery += "'" + qn.getUserEntry().toLowerCase() + "'";
         }
 
         //Optional
         for (int i = 1; i < sll.size(); i++) {
             qn = sll.get(i);
             jpqlQuery += " " + qn.getLogicalOperator() + " ";
             jpqlQuery += "lower(o." + qn.getDwcElement() + ")";
             jpqlQuery += " " + qn.getComparator() + " ";
             if (qn.getComparator().equals("like")) {
                 jpqlQuery += "'%" + qn.getUserEntry().toLowerCase() + "%'";
             } else {
                 jpqlQuery += "'" + qn.getUserEntry().toLowerCase() + "'";
             }
         }
 
         jpqlQuery += " order by o.globaluniqueidentifier ";
 
         Query q = em.createQuery(jpqlQuery);
         q.setFirstResult(first);
         q.setMaxResults(amount);
         List elements = (List) q.getResultList();
         return elements;
     }
 
       /**
      *
      */
     public void reloadDarwinCoreTable() {
         String creationString = "create table ara.DARWIN_CORE_1_4" +
                                 "( GlobalUniqueIdentifier varchar," +
                                 " DateLastModified timestamp," +
                                 " InstitutionCode varchar," +
                                 " CollectionCode varchar," +
                                 " CatalogNumber varchar," +
                                 " CatalogNumberNumeric  numeric," +
                                 " ScientificName varchar," +
                                 " BasisOfRecord varchar," +
                                 " InformationWithheld varchar," +
                                 " KingdomId numeric, " +
                                 " Phylum_id numeric, " +
                                 " Class_id numeric, " +
                                 " Orders_id numeric, " +
                                 " Family_id numeric, " +
                                 " Genus_id numeric, " +
                                 " SpecificEpithet_id numeric, " +
                                 " InfraSpecificEpithet_id numeric," +
                                 " HigherTaxon varchar," +
                                 " Kingdom varchar," +
                                 " Phylum varchar," +
                                 " Class varchar," +
                                 " Orders varchar," +
                                 " Family varchar," +
                                 " Genus varchar," +
                                 " SpecificEpithet varchar, " +
                                 " InfraSpecificEpithet varchar," +
                                 " InfraspecificRank varchar," +
                                 " AuthorYearOfScientificName varchar," +
                                 " NomenclaturalCode varchar," +
                                 " IdentificationQualifier varchar," +
                                 " IdentifiedBy varchar," +
                                 " DateIdentified timestamp, " +
                                 " TypeStatus varchar," +
                                 " CollectingMethod varchar," +
                                 " ValidDistributionFlag varchar," +
                                 " CollectorNumber varchar," +
                                 " FieldNumber  varchar," +
                                 " Collector varchar," +
                                 " EarliestDateCollected timestamp," +
                                 " LatestDateCollected timestamp," +
                                 " VerbatimCollectingDate varchar," +
                                 " DayOfYear numeric," +
                                 " FieldNotes varchar," +
                                 " HigherGeography varchar," +
                                 " Continent varchar," +
                                 " WaterBody varchar," +
                                 " IslandGroup varchar," +
                                 " Island varchar," +
                                 " Country varchar," +
                                 " StateProvince  varchar," +
                                 " County varchar," +
                                 " Locality varchar," +
                                 " DecimalLongitude varchar," +
                                 " VerbatimLongitude varchar," +
                                 " DecimalLatitude varchar," +
                                 " VerbatimLatitude varchar," +
                                 " GeodeticDatum varchar," +
                                 " VerbatimCoordinateSystem varchar," +
                                 " GeoreferenceProtocol varchar," +
                                 " CoordinateUncertaintyInMeters varchar," +
                                 " GeoreferenceRemarks varchar," +
                                 " FootprintWKT varchar," +
                                 " MinimumElevationInMeters double precision," +
                                 " MaximumElevationInMeters double precision," +
                                 " VerbatimElevation double precision," +
                                 " MinimumDepthInMeters double precision," +
                                 " MaximumDepthInMeters double precision," +
                                 " Sex varchar," +
                                 " LifeStage varchar," +
                                 " Preparations varchar," +
                                 " IndividualCount numeric," +
                                 " GenBankNum varchar," +
                                 " OtherCatalogNumbers varchar," +
                                 " RelatedCatalogItems varchar," +
                                 " Remarks varchar," +
                                 " Attributes  varchar," +
                                 " ImageURL varchar," +
                                 " RelatedInformation varchar," +
                                 " Disposition varchar," +
                                 " PointRadiusSpatialFit decimal," +
                                 " FootprintSpatialFit  decimal," +
                                 " VerbatimCoordinates varchar," +
                                 " GeoreferenceSources varchar," +
                                 " GeoreferenceVerificationStatus varchar," +
                                 " PRIMARY KEY ( GlobalUniqueIdentifier));";
         String insertString = "insert into ara.DARWIN_CORE_1_4" +
                               " ( GlobalUniqueIdentifier," +
                               "   DateLastModified ," +
                               "   InstitutionCode," +
                               "   CollectionCode," +
                               "   CatalogNumber," +
                               "   CatalogNumberNumeric ," +
                               "   ScientificName," +
                               "   BasisOfRecord," +
                               "   InformationWithheld," +
                               "   KingdomId, " +
                               "   Phylum_id , " +
                               "   Class_id , " +
                               "   Orders_id , " +
                               "   Family_id , " +
                               "   Genus_id, " +
                               "   SpecificEpithet_id , " +
                               "   InfraSpecificEpithet_id ," +
                               "   HigherTaxon," +
                               "   Kingdom," +
                               "   Phylum ," +
                               "   Class ," +
                               "   Orders," +
                               "   Family ," +
                               "   Genus," +
                               "   SpecificEpithet , " +
                               "   InfraSpecificEpithet," +
                               "   InfraspecificRank ," +
                               "   AuthorYearOfScientificName," +
                               "   NomenclaturalCode," +
                               "   IdentificationQualifier," +
                               "   IdentifiedBy ," +
                               "   DateIdentified , " +
                               "   TypeStatus ," +
                               "   CollectingMethod," +
                               "   ValidDistributionFlag ," +
                               "   CollectorNumber," +
                               "   FieldNumber ," +
                               "   Collector ," +
                               "   EarliestDateCollected ," +
                               "   LatestDateCollected ," +
                               "   VerbatimCollectingDate  ," +
                               "   DayOfYear ," +
                               "   FieldNotes," +
                               "   HigherGeography ," +
                               "   Continent ," +
                               "   WaterBody ," +
                               "   IslandGroup ," +
                               "   Island ," +
                               "   Country," +
                               "   StateProvince ," +
                               "   County ," +
                               "   Locality ," +
                               "   DecimalLongitude ," +
                               "   VerbatimLongitude ," +
                               "   DecimalLatitude ," +
                               "   VerbatimLatitude ," +
                               "   GeodeticDatum ," +
                               "   VerbatimCoordinateSystem," +
                               "   GeoreferenceProtocol ," +
                               "   CoordinateUncertaintyInMeters ," +
                               "   GeoreferenceRemarks ," +
                               "   FootprintWKT ," +
                               "   MinimumElevationInMeters," +
                               "   MaximumElevationInMeters ," +
                               "   VerbatimElevation," +
                               "   MinimumDepthInMeters ," +
                               "   MaximumDepthInMeters ," +
                               "   Sex," +
                               "   LifeStage," +
                               "   Preparations," +
                               "   IndividualCount," +
                               "   GenBankNum ," +
                               "   OtherCatalogNumbers," +
                               "   RelatedCatalogItems ," +
                               "   Remarks," +
                               "   Attributes," +
                               "   ImageURL," +
                               "   RelatedInformation," +
                               "   Disposition," +
                               "   PointRadiusSpatialFit," +
                               "   FootprintSpatialFit," +
                               "   VerbatimCoordinates," +
                               "   GeoreferenceSources," +
                               "   GeoreferenceVerificationStatus)" +
                               " select ins.INSTITUTION_CODE ||':' || col.name ||':' || s.CATALOG_NUMBER  as GlobalUniqueIdentifier," +
                               " current_date as DateLastModified," +
                               " null as InstitutionCode," +
                               " col.name as CollectionCode ," +
                               " to_char(s.specimen_id, '000000000000') as CatalogNumber," +
                               " s.specimen_id as CatalogNumberNumeric ," +
                               " t.default_name as ScientificName," +
                               " '' as BasisOfRecord," +
                               " null as InformationWithheld," +
                               " r.taxon_id as KingdomId, " +
                               " fl.taxon_id as Phylum_id, " +
                               " c.taxon_id as Class_id, " +
                               " tor.taxon_id as Orders_id, " +
                               "  tf.taxon_id as Family_id, " +
                               "  g.taxon_id as Genus_id, " +
                               "  sp.taxon_id as SpecificEpithet_id, " +
                               "  subsp.taxon_id as InfraSpecificEpithet_id ," +
                               "  null as HigherTaxon," +
                               "  r.default_name as Kingdom," +
                               "  fl.default_name as Phylum," +
                               "  c.default_name as Class, " +
                               "  tor.default_name as Orders," +
                               "  tf.default_name as Family," +
                               "  g.default_name as genus,     " +
                               "  sp.default_name as SpecificEpithet," +
                               "  subsp.default_name as InfraSpecificEpithet ," +
                               "  'Sub.' as InfraspecificRank,      " +
                               "  null as AuthorYearOfScientificName," +
                               "  null as NomenclaturalCode," +
                               "  it.name as IdentificationQualifier," +
                               "  perid.FIRST_NAME || ' '|| perid.initials || ' '|| perid.last_name as IdentifiedBy," +
                               "  i.identification_date as DateIdentified," +
                               "  null as TypeStatus," +
                               "  cm.name as CollectingMethod," +
                               "  null as ValidDistributionFlag," +
                               "  god.gathering_observation_detail_number as CollectorNumber," +
                               "  to_char(gat.gathering_observation_id, '00000000000000') as FieldNumber," +
                               "  percol.FIRST_NAME || ' '|| percol.initials || ' '|| percol.last_name as collector," +
                               "  gat.initial_date as  EarliestDateCollected," +
                               "  gat.final_DATE as  LatestDateCollected," +
                               "  to_char(gat.initial_date,'Mon DD, YYYY') as VerbatimCollectingDate," +
                               "  null as DayOfYear," +
                               "  null as FieldNotes," +
                               "  null as HigherGeography," +
                               "  null as Continent,      " +
                               "  null as WaterBody ," +
                               "  null as IslandGroup, " +
                               "  null as Island, " +
                               "  null as Country," +
                               "  null as StateProvince," +
                               "  null as County," +
                               "  sit.description as Locality," +
                               "  sitCoor.longitude as DecimalLongitude," +
                               "  to_char(sitCoor.Longitude, '999999999.999999') as VerbatimLongitude," +
                               "  sitCoor.latitude as DecimalLatitude," +
                               "  to_char(sitCoor.Latitude, '999999999.999999') as VerbatimLatitude," +
                               "  sit.geodetic_datum as GeodeticDatum," +
                               "  'Decimal degrees' as VerbatimCoordinateSystem ," +
                               "  scm.name as GeoreferenceProtocol," +
                               "  to_char(sit.precision, '999999')  as CoordinateUncertaintyInMeters," +
                               "  null as georeferenceremarks," +
                               "  null as FootprintWKT," +
                               "  gat.minimum_elevation as MinimumElevationInMeters," +
                               "  gat.maximum_elevation as MaximumElevationInMeters," +
                               "  null as VerbatimElevation," +
                               "  gat.minimum_depth as MinimumDepthInMeters," +
                               "  gat.maximum_depth as MaximumDepthInMeters," +
                               "  null as Sex, " +
                               "  null as LifeStage," +
                               "  pm.name as Preparations ," +
                               "  null as individualcount," +
                               "  null as GenBankNum," +
                               "  null as OtherCatalogNumbers, " +
                               "  null as RelatedCatalogItems, " +
                               "  null as Remarks," +
                               "  null as Attributes, " +
                               "  null as ImageURL, " +
                               "  null as RelatedInformation, " +
                               "  null as Disposition, " +
                               "  null as PointRadiusSpatialFit, " +
                               "  null as FootprintSpatialFit, " +
                               "  null as VerbatimCoordinates," +
                               "  null as GeoreferenceSources," +
                               "  null as GeoreferenceVerificationStatus" +
                               " from ara.specimen s left outer join ara.identification i on (s.specimen_id = i.specimen_id)" +
                               "  left outer join ara.taxon t on (t.taxon_id = i.taxon_id)" +
                               "  left outer join ara.taxon r on (r.taxon_id = t.kingdom_taxon_id)" +
                               "  left outer join ara.taxon fl on (fl.taxon_id = t.phylum_division_taxon_id)" +
                               "  left outer join ara.taxon c  on (c.taxon_id = t.class_taxon_id )" +
                               "  left outer join ara.taxon tor on (tor.taxon_id = t.order_taxon_id ) " +
                               "  left outer join ara.taxon tf on (tf.taxon_id = t.family_taxon_id)" +
                               "  left outer join ara.taxon g on (g.taxon_id = t.genus_taxon_id)" +
                               "  left outer join ara.taxon sp on (sp.taxon_id = t.species_taxon_id)" +
                               "  left outer join ara.taxon subsp on (subsp.taxon_id = t.subspecies_taxon_id)" +
                               "  left outer join ara.gathering_observation_detail god on (s.GATHERING_OBSERVATION_DETAIL_ID =   god.GATHERING_OBSERVATION_DETAIL_ID)" +
                               "  left outer join ara.gathering_observation gat on (gat.gathering_observation_id = s.gathering_observation_id)" +
                               "  left outer join ara.collector_observer colObs on (colObs.gathering_observation_id = gat.gathering_observation_id and  colObs.SEQUENCE = 1)" +
                               "  left outer join ara.site sit on (sit.site_id = gat.site_id)" +
                               "  left outer join ara.site_coordinate sitCoor on (sit.site_id = sitCoor.site_id and  sitCoor.SEQUENCE = 1)" +
                               "  left outer join ara.preservation_medium pm on (s.preservation_medium_id = pm.preservation_medium_id )" +
                               "  left outer join ara.collection col on (s.collection_id = col.collection_id )" +
                               "  left outer join ara.gathering_observation_method cm on (cm.gathering_observation_method_id = s.gathering_observation_method_id)" +
                               "  left outer join ara.person percol on (percol.person_id = colObs.collector_person_id)" +
                               "  left outer join ara.site_calculation_method scm on (sit.site_calculation_method_id = scm.site_calculation_method_id)" +
                               "  left outer join ara.identifier sid on (s.specimen_id = sid.specimen_id and sid.identification_sequence = 1 and sid.identifier_sequence = 1)" +
                               "  left outer join ara.person perid on (perid.person_id = sid.identifier_person_id)" +
                               "  left outer join ara.identification_type it on (i.identification_type_id = it.identification_type_id)" +
                               "  left outer join ara.institution ins on (ins.institution_id = s.institution_id)" +
                               "where specimen_category_id <> 4;";
         Query q1 = em.createNativeQuery("drop table darwin_core_1_4;");
         System.out.println(q1.executeUpdate() + " drop dwc1_4");
         Query q2 = em.createNativeQuery(creationString);
         System.out.println(q2.executeUpdate() + " create dwc1_4");
         Query q3 = em.createNativeQuery(insertString);
         System.out.println(q3.executeUpdate() + " inserts dwc1_4");
     }
 
     /**
      *
      * @param specimenId
      * @return
      */
     public List<LifeStageSexDTO> getLifeStageSexDTOList(Long specimenId) {
 
         Specimen sp = this.specimenEAO.getSpecimenById(specimenId);
         LifeStageSexDTOFactory lssDTOFactory = new LifeStageSexDTOFactory();
         LifeStageSexDTO lssDTO;
         List<LifeStageSexDTO> result;
 
         /**
          *  Si el specimen es observacion o individual entonces
          * el stage y el sex deben de sacarse directamente de la tabla
          * specimen y sino de la tabla specimenLifeStageSex
          */
         Query q;
         try {
 
             //if (SpecimenCategory.INDIVIDUAL.equals(sp.getSpecimenCategory().getId())
             // || SpecimenCategory.OBSERVATION.equals(sp.getSpecimenCategory().getId())){
 
             //    lssDTO = lssDTOFactory.createDTO(sp.getLifeStage(), sp.getSex(), "1");
             //    // Obtener los datos directamente de la entidad specimen
             //    result = new ArrayList<LifeStageSexDTO>();
             //    result.add(lssDTO);
             //    return result;
             //} else {
 
             List<SpecimenLifeStageSex> slssList = this.getSpecimenLifeStageSexEAO().getAllBySpecimenId(specimenId);
             return lssDTOFactory.createDTOList(slssList);
         //}
         } catch (IllegalStateException ex1) {
             this.setMessage(ex1.getMessage());
             return null;
         } catch (IllegalArgumentException ex2) {
             this.setMessage(ex2.getMessage());
             return null;
         }
     }
 
     /**
      * Se seguirá la lógica de borrar todo y volverlos a crear, que es la usada en otros lados.
      *
      * @param specimenId
      * @param specimenLifeStageSexList
      */
     public void updateSpecimenLifeStageSexList(Long specimenId, List<LifeStageSexDTO> specimenLifeStageSexList) {
 
         //borra todo!
         List<SpecimenLifeStageSex> oldList = specimenLifeStageSexEAO.getAllBySpecimenId(specimenId);
         for (SpecimenLifeStageSex slss : oldList) {
             em.remove(slss);
             em.flush();
         //revisar si esas dos lineas se susbstituyen con esta:
         //specimenLifeStageSexEAO.delete(SpecimenLifeStageSex.class, slss.getSpecimenLifeStageSexPK());
         }
 
         //los vuelve a crear!
         for (LifeStageSexDTO lssDTO : specimenLifeStageSexList) {
             saveSpecimenLifeStageSex(specimenId, lssDTO);
         }
     }
 
     /**
      * 
      * @param specimenId
      * @param lifeStageSexDTO
      */
     public void saveSpecimenLifeStageSex(Long specimenId, LifeStageSexDTO lifeStageSexDTO) {
         SpecimenLifeStageSexPK slssPK = new SpecimenLifeStageSexPK(specimenId, lifeStageSexDTO.getLifeStageKey(), lifeStageSexDTO.getSexKey());
         SpecimenLifeStageSex slss = new SpecimenLifeStageSex(slssPK);
         slss.setQuantity(Long.valueOf(lifeStageSexDTO.getQuantity()));
         slss.setCreatedBy("alambrado");
         slss.setLastModificationBy("alambrado");
 
         specimenLifeStageSexEAO.create(slss);
     }
 
     /**
      * @return the specimenEAO
      */
     public SpecimenLocalEAO getSpecimenEAO() {
         return specimenEAO;
     }
 
     /**
      * @param specimenEAO the specimenEAO to set
      */
     public void setSpecimenEAO(SpecimenLocalEAO specimenEAO) {
         this.specimenEAO = specimenEAO;
     }
 
     /**
      * @return the specimenLifeStageSexEAO
      */
     public SpecimenLifeStageSexLocalEAO getSpecimenLifeStageSexEAO() {
         return specimenLifeStageSexEAO;
     }
 
     /**
      * @param specimenLifeStageSexEAO the specimenLifeStageSexEAO to set
      */
     public void setSpecimenLifeStageSexEAO(SpecimenLifeStageSexLocalEAO specimenLifeStageSexEAO) {
         this.specimenLifeStageSexEAO = specimenLifeStageSexEAO;
     }
 
     public void deleteSpecimenLifeStageSex(Long specimenId, Long lifeStageId, Long sexId) {
 
         SpecimenLifeStageSexPK slssPk = new SpecimenLifeStageSexPK(specimenId, lifeStageId, sexId);
         this.specimenLifeStageSexEAO.delete(SpecimenLifeStageSex.class, slssPk);
     }
 }
 
