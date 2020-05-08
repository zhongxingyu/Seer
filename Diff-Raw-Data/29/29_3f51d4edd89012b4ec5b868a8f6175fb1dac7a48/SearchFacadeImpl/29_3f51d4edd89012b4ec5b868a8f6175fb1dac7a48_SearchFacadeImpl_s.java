 /*
  *  Ara - Capture Species and Specimen Data
  *
  * Copyright © 2009  INBio (Instituto Nacional de Biodiversidad).
  * Heredia, Costa Rica.
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
 
 package org.inbio.ara.facade.search.impl;
 
import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import javax.ejb.EJB;
 import org.inbio.ara.facade.search.*;
 import javax.ejb.Stateless;
 import org.inbio.ara.dto.gis.SiteDTO;
 import org.inbio.ara.dto.gis.SiteDTOFactory;
 import org.inbio.ara.dto.inventory.GatheringObservationDTO;
 import org.inbio.ara.dto.inventory.GatheringObservationDTOFactory;
 import org.inbio.ara.dto.inventory.IdentificationDTO;
 import org.inbio.ara.dto.inventory.IdentificationDTOFactory;
 import org.inbio.ara.dto.inventory.IdentifierDTO;
 import org.inbio.ara.dto.inventory.SpecimenDTO;
 import org.inbio.ara.dto.inventory.SpecimenDTOFactory;
 import org.inbio.ara.dto.transaction.TransactedSpecimenDTO;
 import org.inbio.ara.dto.transaction.TransactionDTO;
 import org.inbio.ara.dto.transaction.TransactionDTOFactory;
 import org.inbio.ara.eao.agent.PersonEAOLocal;
 import org.inbio.ara.eao.gathering.GatheringObservationDetailEAOLocal;
 import org.inbio.ara.eao.gathering.GatheringObservationEAOLocal;
 import org.inbio.ara.eao.gis.GeoreferencedSiteEAOLocal;
 import org.inbio.ara.eao.gis.SiteEAOLocal;
 import org.inbio.ara.eao.identification.IdentifierEAOLocal;
 import org.inbio.ara.eao.identification.IdentificationEAOLocal;
 import org.inbio.ara.eao.reports.DarwinCore14EAOLocal;
 import org.inbio.ara.eao.specimen.SpecimenEAOLocal;
 import org.inbio.ara.eao.taxonomy.TaxonEAOLocal;
 import org.inbio.ara.eao.transaction.TransactionEAOLocal;
 import org.inbio.ara.facade.gis.GisFacadeRemote;
 import org.inbio.ara.facade.inventory.InventoryFacadeRemote;
 import org.inbio.ara.facade.transaction.TransactionFacadeRemote;
 import org.inbio.ara.persistence.gathering.GatheringObservation;
 import org.inbio.ara.persistence.gis.GeographicLayerEntity;
 import org.inbio.ara.persistence.gis.Site;
 import org.inbio.ara.persistence.person.Person;
 import org.inbio.ara.persistence.specimen.Specimen;
 import org.inbio.ara.persistence.identification.Identification;
 import org.inbio.ara.persistence.transaction.Transaction;
 import org.inbio.ara.util.QueryNode;
 
 /**
  *
  * @author herson
  */
 @Stateless
 public class SearchFacadeImpl implements SearchFacadeRemote {
     @EJB
     private SpecimenEAOLocal specimenEAOImpl;
     @EJB
     private GatheringObservationEAOLocal gatheringObservationEAOImpl;
     @EJB
     private GatheringObservationDetailEAOLocal gatheringObservationDetailEAOImpl;
     @EJB
     private PersonEAOLocal personEAOImpl;
     @EJB
     private SiteEAOLocal siteEAOImpl;
     @EJB
     private TaxonEAOLocal taxonEAOImpl;
     @EJB
     private IdentificationEAOLocal identificationEAOImpl;
     @EJB
     private IdentifierEAOLocal identifierEAOImpl;
     @EJB
     private InventoryFacadeRemote inventoryFacadeImpl;
     @EJB
     private GeoreferencedSiteEAOLocal georeferencedSiteEAOImpl;
     @EJB
     private GisFacadeRemote gisFacade;
     @EJB
     private DarwinCore14EAOLocal darwinCoreEAOImpl;
 
     @EJB
     private TransactionFacadeRemote transactionFacadeImpl;
 
     @EJB
     private TransactionEAOLocal transactionEAOImpl;
 
     //DTO factories
     private SiteDTOFactory siteDTOFactory =
             new SiteDTOFactory();
     private IdentificationDTOFactory identificationDTOFactory =
             new IdentificationDTOFactory();
     private GatheringObservationDTOFactory gathObsDTOFactory =
             new GatheringObservationDTOFactory();
     private SpecimenDTOFactory specimenDTOFactory = new SpecimenDTOFactory();
 
     private TransactionDTOFactory transactionDTOFactory = new TransactionDTOFactory();
     
 
     public List<SpecimenDTO> test() {
         return null;
     }
 
     /***************************************************************************
      *********************** ========SITE======== ******************************
      **************************************************************************/
     // <editor-fold defaultstate="collapsed" desc="comment">
     /**
      *
      * @param inputDTO Built in the advanced search
      * @param base First result retrieved
      * @param offset How many results I want
      * @return Count of sites
      */
     public Long countSitesByCriteria(SiteDTO inputDTO) {
          Integer i = new Integer(getSiteIds(inputDTO).size());
          return i.longValue();
     }
 
     public Long countSitesByCriteria(String query) {
         String[] parts = this.splitQuery(query);
         if(parts == null)
             return 0L;
         HashSet siteIds = (HashSet) unstructuredSiteQuery(parts);
         Integer i = new Integer(siteIds.size());
         return i.longValue();
     }
 
     public List<SiteDTO> searchSiteByCriteria(String query, int base, int offset) {
         List<Site> siteList = new ArrayList();
         String[] parts = this.splitQuery(query);
         if(parts == null)
             siteDTOFactory.createDTOList(siteList);
 
         HashSet<Long> siteIds = (HashSet) unstructuredSiteQuery(parts);
         siteList = getEntities(siteIds, Site.class, base, offset);
 
        // return siteDTOFactory.createDTOList(siteList);
         return gisFacade.updateCountryAndProvinceName(siteDTOFactory.createDTOList(siteList));
     }
 
     public List<SiteDTO> searchSiteByCriteria(SiteDTO inputDTO, int base, int offset) {
         Set<Long> siteIds = getSiteIds(inputDTO);
         List<Site> siteList = new ArrayList();
         
         siteList = getEntities(siteIds, Site.class, base, offset);
 
        // return siteDTOFactory.createDTOList(siteList);
         return gisFacade.updateCountryAndProvinceName(siteDTOFactory.createDTOList(siteList));
     }
 
     private Set<Long> getSiteIds(SiteDTO inputDTO) {
         Set<Long> siteIds = new HashSet();
         boolean firstFilter = true; //helps with the intersection of data
 
         Long siteId         = inputDTO.getSiteId();
 //        String responsibleName = inputDTO.getResponsibleName();
         String localityDescr   = inputDTO.getDescription();
         String taxonName       = inputDTO.getTaxonName();
         Double latitude        = inputDTO.getLatitude();
         Double longitude       = inputDTO.getLongitude();
         Integer radius         = inputDTO.getRadio();
         Long countryId         = inputDTO.getCountryId();
         Long provinceId        = inputDTO.getProvinceId();
 //        Calendar initialDate   = inputDTO.getInitialDateTime();
 //        Calendar finalDate     = inputDTO.getFinalDateTime();
 
         if(siteId != null) {
             siteIds.add(siteId);
             firstFilter = false;
         }
 //        if(responsibleName != null && !responsibleName.trim().isEmpty()) {
 //            List<Long> newGathObsIds =
 //                    findGathObsByResponsibleName(responsibleName);
 //            if(firstFilter) {
 //                gathObsIds.addAll(newGathObsIds);
 //                firstFilter = false;
 //            } else {
 //                gathObsIds.retainAll(newGathObsIds);
 //            }
 //        }
         if(localityDescr != null && !localityDescr.trim().isEmpty()) {
             List<Long> newSiteIds =
                     findSiteByDescription(localityDescr);
             if(firstFilter) {
                 siteIds.addAll(newSiteIds);
                 firstFilter = false;
             } else {
                 siteIds.retainAll(newSiteIds);
             }
         }
 
         if(taxonName != null && !taxonName.trim().isEmpty()) {
             List<Long> newSiteIds =
                     findSiteByTaxonName(taxonName);
             if(firstFilter) {
                 siteIds.addAll(newSiteIds);
                 firstFilter = false;
             } else {
                 siteIds.retainAll(newSiteIds);
             }
         }
 
         if(latitude != null && longitude != null && radius != null) {
             List<Long> newSiteIds = findSiteByCoordinates(latitude, longitude, radius);
             if(firstFilter) {
                 siteIds.addAll(newSiteIds);
                 firstFilter = false;
             } else {
                 siteIds.retainAll(newSiteIds);
             }
         }
         if(provinceId != null && countryId != null) {
             List<Long> newSiteIds = findSiteByGeoLayer(GeographicLayerEntity.PROVINCE.getId(), provinceId);
             if(firstFilter) {
                 siteIds.addAll(newSiteIds);
                 firstFilter = false;
             } else {
                 siteIds.retainAll(newSiteIds);
             }
         } else if(countryId != null) {
             List<Long> newSiteIds = findSiteByGeoLayer(GeographicLayerEntity.COUNTRY.getId(), countryId);
             if(firstFilter) {
                 siteIds.addAll(newSiteIds);
                 firstFilter = false;
             } else {
                 siteIds.retainAll(newSiteIds);
             }
         }
 //        if(initialDate != null) {
 //            List<Long> newGathObsIds = gatheringObservationEAOImpl.
 //                    findByInitialDate(initialDate);
 //            if(firstFilter) {
 //                gathObsIds.addAll(newGathObsIds);
 //                firstFilter = false;
 //            } else {
 //                gathObsIds.retainAll(newGathObsIds);
 //            }
 //        }
 //        if(finalDate != null) {
 //            List<Long> newGathObsIds = gatheringObservationEAOImpl.
 //                    findByFinalDate(finalDate);
 //            if(firstFilter) {
 //                gathObsIds.addAll(newGathObsIds);
 //                firstFilter = false;
 //            } else {
 //                gathObsIds.retainAll(newGathObsIds);
 //            }
 //        }
         return siteIds;
     }
 
     private List<Long> findSiteByDescription(String descr) {
         List<Long> siteIds = siteEAOImpl.findByDescription(descr);
 
         return siteIds;
     }
 
     private List<Long> findSiteByCoordinates(double latitude, double longitude, int radius) {
         List<Long> siteList = siteEAOImpl.findByCoordinates(latitude, longitude, radius);
 
         return siteList;
     }
 
     private List<Long> findSiteByGeoLayer(Long geoLayerType, Long geoLayerId) {
         List<Long> siteIds = georeferencedSiteEAOImpl.findSiteByGeoLayerId(geoLayerType, geoLayerId);
 
         return siteIds;
     }
 
     /**
      * This method could overload the server.
      * @param name String pattern of the taxon name
      * @return  List of sites with gathering/observations of "taxon name"
      */
     private List<Long> findSiteByTaxonName(String name) {
         ArrayList<Long> finalSpecimenList = new ArrayList();
         ArrayList<Long> finalGathsObsList = new ArrayList();
         ArrayList<Long> finalSiteList     = new ArrayList();
 
         List<Long> taxonIds = taxonEAOImpl.findByTaxonName(name);
 
         for (Long taxonId : taxonIds) {
             List<Long> specimenList =
                     identificationEAOImpl.findSpecimenByTaxonId(taxonId);
             finalSpecimenList.addAll(specimenList);
         }
 
         for (Long specimenId : finalSpecimenList) {
 	    // findBySpecimenId() = (select gathering_id from specimen s where s.specimenId = specimenId;)
             List<Long> gathsObsList =
                     gatheringObservationEAOImpl.findBySpecimenId(specimenId);
             finalGathsObsList.addAll(gathsObsList);
         }
 
         for (Long gathObsId : finalGathsObsList) {
             List<Long> siteList =
                     siteEAOImpl.findByGathObsId(gathObsId);
             finalSiteList.addAll(siteList);
         }
 
         return finalSiteList;
     }
 
     private Set<Long> unstructuredSiteQuery(String[] parts) {
         HashSet<Long> finalSiteIds = new HashSet();
         List<String> strings = new ArrayList();
         List<Long> numbers = new ArrayList();
         for (String part : parts) {
             try {
                 Long d = Long.parseLong(part);
                 numbers.add(d);
                 strings.add(d.toString());
             } catch (Exception e){
                 strings.add(part.toLowerCase());
             }
         }
 
         for (String inputString : strings) {
             /********************BUSQUEDA POR SITE DESCRIPTION*****************/
             List<Long> listBySiteDescription = findSiteByDescription(inputString);
             finalSiteIds.addAll(listBySiteDescription);
         }
 
         return finalSiteIds;
     }
 
 
     // </editor-fold>
 
     /***************************************************************************
      *********************** ===IDENTIFICATION=== ******************************
      **************************************************************************/
     // <editor-fold defaultstate="collapsed" desc="comment">
     /**
      *
      * @param inputDTO Built in the advanced search
      * @param base First result retrieved
      * @param offset How many results I want
      * @return List of identifications
      */
     public List<IdentificationDTO>
             searchIdentificationByCriteria(IdentificationDTO inputDTO,
             int base, int offset) {
         
         List<Identification> identificationsList = new ArrayList();
         Set<Long> identificationIds = getIdentificationIds(inputDTO);
 
         identificationsList = getEntities(identificationIds,
                 Identification.class, base, offset);
         return identificationDTOFactory.createDTOList(identificationsList);
     }
 
     /**
      * Used to retrieve some identifications and to know the total amount of
      * records (count)
      * @param inputDTO Built in the advanced search
      * @return Set of identification Ids.
      */
     private Set<Long> getIdentificationIds(IdentificationDTO inputDTO) {
         Set<Long> identificationIds = new HashSet();
 
         boolean firstFilter = true; //helps with the intersection of data
 
         String catalogNumber = inputDTO.getCatalogNumber();
         String taxonName = inputDTO.getTaxonString();
         List<IdentifierDTO> identifiers = inputDTO.getIdentifiers();
         Long statusId = inputDTO.getStatusId();
         Long typeId = inputDTO.getTypeId();
         Long collectionId = inputDTO.getCollectionId();
 
         if(catalogNumber != null && !catalogNumber.trim().isEmpty()) {
             Long specimenId =specimenEAOImpl.findByCatalogNumber(catalogNumber);
             identificationIds.add(specimenId);
             firstFilter = false;
         }
         if(taxonName != null && !taxonName.trim().isEmpty()) {
             HashSet<Long> tmpSet = new HashSet();
             List<Long> taxonIds = taxonEAOImpl.findByTaxonName(taxonName);
             for (Long taxonId : taxonIds) {
                 List<Long> specimenList =
                         identificationEAOImpl.findSpecimenByTaxonId(taxonId);
                 tmpSet.addAll(specimenList);
             }
             if(firstFilter) {
                 identificationIds.addAll(tmpSet);
                 firstFilter = false;
             } else {
                 identificationIds.retainAll(tmpSet);
             }
         }
         if(identifiers != null) {
             HashSet<Long> tmpSet = new HashSet();
             for (IdentifierDTO identifierDTO : identifiers) {
                 List<Person> personId = personEAOImpl.
                         findByName(identifierDTO.getIdentifierName());
                 for (Person person : personId) {
                     List<Long> specimensByIdentifier = identifierEAOImpl.
                             findSpecimensByIdentifierId(person.getPersonId());
                     tmpSet.addAll(specimensByIdentifier);
                 }
             }
             if(firstFilter) {
                 identificationIds.addAll(tmpSet);
                 firstFilter = false;
             } else {
                 identificationIds.retainAll(tmpSet);
             }
         }
         if(statusId != null) {
             List<Long> specimenList =
                     identificationEAOImpl.findSpecimenByStatusId(statusId);
             if(firstFilter) {
                 identificationIds.addAll(specimenList);
                 firstFilter = false;
             } else {
                 identificationIds.retainAll(specimenList);
             }
         }
         if(typeId != null) {
             List<Long> specimenList =
                     identificationEAOImpl.findSpecimenByTypeId(typeId);
             if(firstFilter) {
                 identificationIds.addAll(specimenList);
                 firstFilter = false;
             } else {
                 identificationIds.retainAll(specimenList);
             }
         }
 
         //Not visible to the user. This parameter is set in the
         //IdentificationSessionBean
         if(collectionId != null) {
             List<Long> specimenList =
                     specimenEAOImpl.findByCollectionId(collectionId);
             if(firstFilter) {
                 identificationIds.addAll(specimenList);
                 firstFilter = false;
             } else {
                 identificationIds.retainAll(specimenList);
             }
         }
         return identificationIds;
     }
 
     /**
      *
      * @param query String unstructured query
      * @param base
      * @param offset
      * @return
      * @deprecated
      */
     public List<IdentificationDTO>
             searchIdentificationByCriteria(String query, int base, int offset) {
         List<Identification> identificationsList = new ArrayList();
         String[] parts = this.splitQuery(query);
         if(parts == null)
             return identificationDTOFactory.createDTOList(identificationsList);
         
         HashSet<Long> specimenIds = (HashSet<Long>)
                 unstructuredIdentificationQuery(parts);
         identificationsList = getEntities(specimenIds,
                 Identification.class, base, offset);
         return identificationDTOFactory.createDTOList(identificationsList);
     }
     public List<IdentificationDTO> searchIdentificationByCriteria(String query,
             Long collectionId, int base, int offset) {
         List<Identification> identificationsList = new ArrayList();
         String[] parts = this.splitQuery(query);
         if(parts == null)
             return identificationDTOFactory.createDTOList(identificationsList);
 
         HashSet<Long> specimenIds = (HashSet<Long>)
                 unstructuredIdentificationQuery(parts);
 
         //Set intersection -> filter by collection
         specimenIds.retainAll(specimenEAOImpl.
                 findByCollectionId(collectionId));
         //Retrieve entities
         identificationsList = getEntities(specimenIds,
                 Identification.class, base, offset);
         return identificationDTOFactory.createDTOList(identificationsList);
     }
 
     private Set<Long> unstructuredIdentificationQuery(String[] parts) {
         Set<Long> finalSpecimenIds = new HashSet();
         List<String> strings = new ArrayList();
         List<Long> numbers = new ArrayList();
         for (String part : parts) {
             try {
                 Long d = Long.parseLong(part);
                 numbers.add(d);
                 strings.add(d.toString());
             } catch (Exception e){
                 strings.add(part.toLowerCase());
             }
         }
         for (String inputString : strings) {
             /****************BUSQUEDA POR CATALOG NUMBER***********************/
             Long specimenId = specimenEAOImpl.findByCatalogNumber(inputString);
             if(specimenId != null) {
                 List<Long> listByCatalogNumber = new ArrayList();
                 listByCatalogNumber.add(specimenId);
                 finalSpecimenIds.addAll(listByCatalogNumber);
             }
             /**************** BUSQUEDA POR TAXON NAME *************************/
             List<Long> taxonIds = taxonEAOImpl.findByTaxonName(inputString);
             for (Long taxonId : taxonIds) {
                 List<Long> specimenList =
                         identificationEAOImpl.findSpecimenByTaxonId(taxonId);
                 finalSpecimenIds.addAll(specimenList);
             }
             /**************** BUSQUEDA POR IDENTIFIER NAME ********************/
             List<Person> personId = personEAOImpl.findByName(inputString);
             for (Person person : personId) {
                 List<Long> specimensByIdentifier = identifierEAOImpl.
                         findSpecimensByIdentifierId(person.getPersonId());
                 finalSpecimenIds.addAll(specimensByIdentifier);
             }
 
         }
         for (Long inputLong : numbers) {
             /****************BUSQUEDA POR CATALOG NUMBER***********************/
             Long specimenId = specimenEAOImpl.findByCatalogNumber(inputLong.
                     toString());
             List<Long> listByCatalogNumber = new ArrayList();
             listByCatalogNumber.add(specimenId);
             finalSpecimenIds.addAll(listByCatalogNumber);
         }
         return finalSpecimenIds;
     }
 
     /**
      *
      * @param query
      * @return
      * @deprecated
      */
     public Long countIdentificationByCriteria(String query) {
         String[] parts = this.splitQuery(query);
         if(parts == null)
             return 0L;
         HashSet specimenIds = (HashSet) unstructuredIdentificationQuery(parts);
         Integer i = new Integer(specimenIds.size());
         return i.longValue();
     }
 
     public Long countIdentificationByCriteria(String query, Long collectionId) {
         String[] parts = this.splitQuery(query);
         if(parts == null)
             return 0L;
         HashSet specimenIds = (HashSet) unstructuredIdentificationQuery(parts);
         //Set intersection -> filter by collection
         specimenIds.retainAll(specimenEAOImpl.
                 findByCollectionId(collectionId));
         Integer i = new Integer(specimenIds.size());
         return i.longValue();
     }
 
     public Long countIdentificationByCriteria(IdentificationDTO inputDTO) {
          Integer i = new Integer(getIdentificationIds(inputDTO).size());
          return i.longValue();
     }
     // </editor-fold>
 
     /***************************************************************************
      ************************ ===GATHERING=== **********************************
      **************************************************************************/
     // <editor-fold defaultstate="collapsed" desc="comment">
     /**
      *
      * @param inputDTO Built in the advanced search
      * @param base First result retrieved
      * @param offset How many results I want
      * @return List of identifications
      */
     public List<GatheringObservationDTO> searchGathObsByCriteria(
             GatheringObservationDTO inputDTO, int base, int offset) {
         Set<Long> gathObsIds = getGathObsIds(inputDTO);
         List<GatheringObservation> gathObsList = new ArrayList();
         
         gathObsList = getEntities(gathObsIds,
                 GatheringObservation.class, base, offset);
         return inventoryFacadeImpl.updateGathObsCountryAndProvinceName(
                 gathObsDTOFactory.createDTOList(gathObsList));
     }
 
     /**
      * Used to retrieve some identifications and to know the total amount of
      * records (count)
      * @param inputDTO Built in the advanced search
      * @return Set of identification Ids.
      */
     private Set<Long> getGathObsIds(GatheringObservationDTO inputDTO) {
         Set<Long> gathObsIds = new HashSet();
         boolean firstFilter = true; //helps with the intersection of data
 
         Long gathObsId         = inputDTO.getGatheringObservationId();
         String responsibleName = inputDTO.getResponsibleName();
         String localityDescr   = inputDTO.getLocalityDescription();
         Double latitude        = inputDTO.getLatitude();
         Double longitude       = inputDTO.getLongitude();
         Integer radius         = inputDTO.getRadio();
         Long countryId         = inputDTO.getCountryId();
         Long provinceId        = inputDTO.getProvinceId();
         Calendar initialDate   = inputDTO.getInitialDateTime();
         Calendar finalDate     = inputDTO.getFinalDateTime();
 
         if(gathObsId != null) {
             gathObsIds.add(gathObsId);
             firstFilter = false;
         }
         if(responsibleName != null && !responsibleName.trim().isEmpty()) {
             List<Long> newGathObsIds =
                     findGathObsByResponsibleName(responsibleName);
             if(firstFilter) {
                 gathObsIds.addAll(newGathObsIds);
                 firstFilter = false;
             } else {
                 gathObsIds.retainAll(newGathObsIds);
             }
         }
         if(localityDescr != null && !localityDescr.trim().isEmpty()) {
             List<Long> newGathObsIds =
                     findGathObsBySiteDescription(localityDescr);
             if(firstFilter) {
                 gathObsIds.addAll(newGathObsIds);
                 firstFilter = false;
             } else {
                 gathObsIds.retainAll(newGathObsIds);
             }
         }
         if(latitude != null && longitude != null && radius != null) {
             List<Long> newGathObsIds =
                     findGathObsByCoordinates(latitude, longitude, radius);
             if(firstFilter) {
                 gathObsIds.addAll(newGathObsIds);
                 firstFilter = false;
             } else {
                 gathObsIds.retainAll(newGathObsIds);
             }
         }
         if(provinceId != null && countryId != null) {
             List<Long> newGathObsIds =
                    findGathObsByGeoLayer(GeographicLayerEntity.PROVINCE.getId(),
                    provinceId);
             if(firstFilter) {
                 gathObsIds.addAll(newGathObsIds);
                 firstFilter = false;
             } else {
                 gathObsIds.retainAll(newGathObsIds);
             }
         } else if(countryId != null) {
             List<Long> newGathObsIds =
                    findGathObsByGeoLayer(GeographicLayerEntity.COUNTRY.getId(),
                    countryId);
             if(firstFilter) {
                 gathObsIds.addAll(newGathObsIds);
                 firstFilter = false;
             } else {
                 gathObsIds.retainAll(newGathObsIds);
             }
         }
         if(initialDate != null) {
             List<Long> newGathObsIds = gatheringObservationEAOImpl.
                     findByInitialDate(initialDate);
             if(firstFilter) {
                 gathObsIds.addAll(newGathObsIds);
                 firstFilter = false;
             } else {
                 gathObsIds.retainAll(newGathObsIds);
             }
         }
         if(finalDate != null) {
             List<Long> newGathObsIds = gatheringObservationEAOImpl.
                     findByFinalDate(finalDate);
             if(firstFilter) {
                 gathObsIds.addAll(newGathObsIds);
                 firstFilter = false;
             } else {
                 gathObsIds.retainAll(newGathObsIds);
             }
         }
         return gathObsIds;
     }
 
     public Long countGathObsByCriteria(GatheringObservationDTO inputDTO) {
          Integer i = new Integer(getGathObsIds(inputDTO).size());
          return i.longValue();
     }
 
     /**
      * @param query String unstructured query
      * @param base
      * @param offset
      * @return
      * @deprecated
      */
     public List<GatheringObservationDTO>
             searchGathObsByCriteria(String query, int base, int offset) {
         List<GatheringObservation> gathObsList = new ArrayList();
         String[] parts = this.splitQuery(query);
         if(parts == null)
             gathObsDTOFactory.createDTOList(gathObsList);
 
         HashSet<Long> gathObsIds = (HashSet) unstructuredGathObsQuery(parts);
         gathObsList = getEntities(gathObsIds,
                 GatheringObservation.class, base, offset);
         //return gathObsDTOFactory.createDTOList(gathObsList);
         return inventoryFacadeImpl.updateGathObsCountryAndProvinceName(
                 gathObsDTOFactory.createDTOList(gathObsList));
     }
 
     public List<GatheringObservationDTO> searchGathObsByCriteria(String query,
             Long collectionId, int base, int offset) {
         List<GatheringObservation> gathObsList = new ArrayList();
         String[] parts = this.splitQuery(query);
         if(parts == null)
             gathObsDTOFactory.createDTOList(gathObsList);
 
         HashSet<Long> gathObsIds = (HashSet) unstructuredGathObsQuery(parts);
         //Set intersection -> filter by collection
         gathObsIds.retainAll(gatheringObservationEAOImpl.
                 findByCollectionId(collectionId));
         //Retrieve entities
         gathObsList = getEntities(gathObsIds,
                 GatheringObservation.class, base, offset);
         //return gathObsDTOFactory.createDTOList(gathObsList);
         return inventoryFacadeImpl.updateGathObsCountryAndProvinceName(
                 gathObsDTOFactory.createDTOList(gathObsList));
     }
 
     /**
      *
      * @param query
      * @return
      * @deprecated
      */
     public Long countGathObsByCriteria(String query) {
         String[] parts = this.splitQuery(query);
         if(parts == null)
             return 0L;
         HashSet gathObsIds = (HashSet) unstructuredGathObsQuery(parts);
         Integer i = new Integer(gathObsIds.size());
         return i.longValue();
     }
 
     public Long countGathObsByCriteria(String query, Long collectionId) {
         String[] parts = this.splitQuery(query);
         if(parts == null)
             return 0L;
         HashSet gathObsIds = (HashSet) unstructuredGathObsQuery(parts);
         //Set intersection -> filter by collection
         gathObsIds.retainAll(gatheringObservationEAOImpl.
                 findByCollectionId(collectionId));
         Integer i = new Integer(gathObsIds.size());
         return i.longValue();
     }
 
     private Set<Long> unstructuredGathObsQuery(String[] parts) {
         HashSet<Long> finalGathObsIds = new HashSet();
         List<String> strings = new ArrayList();
         List<Long> numbers = new ArrayList();
         for (String part : parts) {
             try {
                 Long d = Long.parseLong(part);
                 numbers.add(d);
                 strings.add(d.toString());
             } catch (Exception e){
                 strings.add(part.toLowerCase());
             }
         }
 
         for (String inputString : strings) {
             /**************BUSQUEDA POR PROVINCIA Y/O PAIS*********************/
             //TODO
 //            List<Long> listByGeographicLayer =
 //                    findSpecimenByGeographicLayer(inputString);
 //            finalGathObsIds.addAll(listByGeographicLayer);
             /********************BUSQUEDA POR SITE DESCRIPTION*****************/
             List<Long> listBySiteDescription =
                     findGathObsBySiteDescription(inputString);
             finalGathObsIds.addAll(listBySiteDescription);
             /*************** BUSQUEDA POR RESPONSIBLE NAME ********************/
             List<Long> listByResponsibleName =
                     findGathObsByResponsibleName(inputString);
             finalGathObsIds.addAll(listByResponsibleName);
         }
         for (Long inputLong : numbers) {
             /******************** BUSQUEDA POR GATH/OBS ID ********************/
 //            Long gathObsById = gatheringObservationEAOImpl.
 //                    findById(GatheringObservation.class, inputLong).
 //                    getGatheringObservationId();
             finalGathObsIds.add(inputLong);
         }
         return finalGathObsIds;
     }
 
     private List<Long> findGathObsBySiteDescription(String desc) {
         ArrayList<Long> finalGathObsList = new ArrayList();
         List<Long> siteList = siteEAOImpl.findByDescription(desc);
         for (Long siteId : siteList) {
             List<Long> gathObsBySite =
                     gatheringObservationEAOImpl.findBySiteId(siteId);
             finalGathObsList.addAll(gathObsBySite);
         }
         return finalGathObsList;
     }
 
     private List<Long> findGathObsByResponsibleName(String name) {
         List<Long> finalGathObsIds = new ArrayList();
         List<Person> personId = personEAOImpl.findByName(name);
         for (Person person : personId) {
             List<Long> gathObsByResponsible = gatheringObservationEAOImpl.
                     findByResponsibleId(person.getPersonId());
             finalGathObsIds.addAll(gathObsByResponsible);
         }
         return finalGathObsIds;
     }
 
     private List<Long> findGathObsByCoordinates(double latitude,
             double longitude, int radius) {
         ArrayList<Long> finalGathObsList = new ArrayList();
         List<Long> siteList =
                 siteEAOImpl.findByCoordinates(latitude, longitude, radius);
         for (Long siteId : siteList) {
             List<Long> gathObsBySite =
                     gatheringObservationEAOImpl.findBySiteId(siteId);
             finalGathObsList.addAll(gathObsBySite);
         }
         return finalGathObsList;
     }
 
     private List<Long> findGathObsByGeoLayer(Long geoLayerType, Long geoLayerId)
     {
         ArrayList<Long> finalGathObsList = new ArrayList();
         List<Long> siteIds = georeferencedSiteEAOImpl.
                 findSiteByGeoLayerId(geoLayerType, geoLayerId);
         System.out.println("-+-"+siteIds.toString());
         for (Long siteId : siteIds) {
             List<Long> gathObsBySite =
                     gatheringObservationEAOImpl.findBySiteId(siteId);
             finalGathObsList.addAll(gathObsBySite);
         }
         return finalGathObsList;
     }//findSiteByGeoLayerId(GeographicLayerEntity.PROVINCE.getId(), 3L))
     // </editor-fold>
     /***************************************************************************
      ************************* ===SPECIMEN=== **********************************
      **************************************************************************/
     // <editor-fold defaultstate="collapsed" desc="comment">
     public List<SpecimenDTO> searchSpecimenByCriteria(String query,
             Long collectionId, int base, int offset) {
         SpecimenDTOFactory factory = new SpecimenDTOFactory();
         List<Specimen> specimenList = new ArrayList();
         String[] parts = this.splitQuery(query);
         if(parts == null)
             return factory.createDTOList(specimenList);
 
         HashSet<Long> specimenIds = (HashSet<Long>)
                 unstructuredSpecimenQuery(parts);
         //Set intersection -> filter by collection
         specimenIds.retainAll(specimenEAOImpl.findByCollectionId(collectionId));
         //Retrieve entities
         specimenList = getEntities(specimenIds, Specimen.class, base, offset);
 
         List<SpecimenDTO> updated = inventoryFacadeImpl.updateCountryAndProvinceName(factory.createDTOList(specimenList));
         return inventoryFacadeImpl.updateScientificName(updated);
 
     }
 
     /**
      * @param query String unstructured query
      * @param base
      * @param offset
      * @return List<SpecimenDTO>
      * @deprecated Use instead searchSpecimenByCriteria(String query,
      * Long collectionId, int base, int offset) {
      */
     public List<SpecimenDTO> searchSpecimenByCriteria(String query,
             int base, int offset) {
         SpecimenDTOFactory factory = new SpecimenDTOFactory();
         List<Specimen> specimenList = new ArrayList();
         String[] parts = this.splitQuery(query);
         if(parts == null)
             return factory.createDTOList(specimenList);
         
         HashSet<Long> specimenIds = (HashSet<Long>) 
                 unstructuredSpecimenQuery(parts);
 
         specimenList = getEntities(specimenIds, Specimen.class, base, offset);
 
         List<SpecimenDTO> updated = inventoryFacadeImpl.
             updateCountryAndProvinceName(factory.createDTOList(specimenList));
         return inventoryFacadeImpl.updateScientificName(updated);
     }
 
     /**
      * @param inputDTO Built in the advanced search
      * @param base First result retrieved
      * @param offset How many results I want
      * @return List of specimens
      */
     public List<SpecimenDTO> searchSpecimenByCriteria(SpecimenDTO inputDTO,
             int base, int offset) {
         Set<Long> specimenIds = getSpecimenIds(inputDTO);
         List<Specimen> specimenList = new ArrayList();
 
         specimenList = getEntities(specimenIds, Specimen.class, base, offset);
         List<SpecimenDTO> specimenDTOList = specimenDTOFactory.
                 createDTOList(specimenList);
 
         List<SpecimenDTO> updated = inventoryFacadeImpl.updateCountryAndProvinceName
                 (specimenDTOList);
         return inventoryFacadeImpl.updateScientificName(updated);
     }
     
     private Set<Long> getSpecimenIds(SpecimenDTO inputDTO) {
         Set<Long> specimenIds = new HashSet();
         boolean firstFilter = true; //helps with the intersection of data
 
         String catalogNumber   = inputDTO.getCatalogNumber();
         Long institutionId     = inputDTO.getInstitutionId();
         Long collectionId      = inputDTO.getCollectionId();
         String taxonName       = inputDTO.getTaxonName();
         String localityDescrip = inputDTO.getLocalityDescription();
         Long countryId         = inputDTO.getCountryId();
         Long provinceId        = inputDTO.getProvinceId();
         Double latitude        = inputDTO.getLatitude();
         Double longitude       = inputDTO.getLongitude();
         Integer radius         = inputDTO.getRadio();
         String responsibleName = inputDTO.getResponsibleName();
 
         if (catalogNumber != null && !catalogNumber.trim().isEmpty()) {
             List<Long> specimenByCN = findSpecimenByCatalogNumber(catalogNumber);
             specimenIds.addAll(specimenByCN);
             firstFilter = false;
         }
         if(institutionId != null) {
             List<Long> specimenByInstitution =
                     specimenEAOImpl.findByInstitutionId(institutionId);
             if(firstFilter) {
                 specimenIds.addAll(specimenByInstitution);
                 firstFilter = false;
             } else {
                 specimenIds.retainAll(specimenByInstitution);
             }
         }
         if(collectionId != null) {
             List<Long> specimenByCollection =
                     specimenEAOImpl.findByCollectionId(collectionId);
             if(firstFilter) {
                 specimenIds.addAll(specimenByCollection);
                 firstFilter = false;
             } else {
                 specimenIds.retainAll(specimenByCollection);
             }
         }
         if(taxonName != null && !taxonName.trim().isEmpty()) {
             List<Long> specimenByTaxonName =
                     //specimenEAOImpl.findByTaxonName(taxonName);
                     identificationEAOImpl.findSpecimenByTaxonName(taxonName);                 
             if(firstFilter) {
                 specimenIds.addAll(specimenByTaxonName);
                 firstFilter = false;
             } else {
                 specimenIds.retainAll(specimenByTaxonName);
             }
         }
         if(localityDescrip != null && !localityDescrip.trim().isEmpty()) {
              List<Long> specimenBySiteDescr =
                      findSpecimenBySiteDescription(localityDescrip);
             if(firstFilter) {
                 specimenIds.addAll(specimenBySiteDescr);
                 firstFilter = false;
             } else {
                 specimenIds.retainAll(specimenBySiteDescr);
             }
         }
         if(countryId != null && provinceId != null) {
             List<Long> specimenByGeoLayer =
                 findSpecimenByGeoLayer(GeographicLayerEntity.PROVINCE.getId(),
                 countryId);
             if(firstFilter) {
                 specimenIds.addAll(specimenByGeoLayer);
                 firstFilter = false;
             } else {
                 specimenIds.retainAll(specimenByGeoLayer);
             }
         } else if (countryId != null) {
             List<Long> specimenByGeoLayer =
                 findSpecimenByGeoLayer(GeographicLayerEntity.COUNTRY.getId(),
                 countryId);
             if(firstFilter) {
                 specimenIds.addAll(specimenByGeoLayer);
                 firstFilter = false;
             } else {
                 specimenIds.retainAll(specimenByGeoLayer);
             }
         }
         if(latitude != null && longitude != null && radius != null) {
             List<Long> specimenByCoords =
                     findSpecimenByCoords(latitude, longitude, radius);
             if(firstFilter) {
                 specimenIds.addAll(specimenByCoords);
                 firstFilter = false;
             } else {
                 specimenIds.retainAll(specimenByCoords);
             }
         }
         if(responsibleName != null && !responsibleName.trim().isEmpty()) {
             List<Long> specimenByResponsible =
                     findSpecimenByResponsibleName(responsibleName);
             if(firstFilter) {
                 specimenIds.addAll(specimenByResponsible);
                 firstFilter = false;
             } else {
                 specimenIds.retainAll(specimenByResponsible);
             }
         }
         return specimenIds;
     }
 
     /**
      *
      * @param query
      * @return Count about how many results will be returned in a simple query
      * @deprecated Use instead: countSpecimensByCriteria(String query,
      *      <b>Long collectionId</b>)
      */
     public Long countSpecimensByCriteria(String query) {
         String[] parts = this.splitQuery(query);
         if(parts == null)
             return 0L;
         HashSet specimenIds = (HashSet) unstructuredSpecimenQuery(parts);
         Integer i = new Integer(specimenIds.size());
         return i.longValue();
     }
 
     /**
      *
      * @param query
      * @param collectionId
      * @return Count about how many results will be returned in a simple query,
      * filtered by collection
      */
     public Long countSpecimensByCriteria(String query, Long collectionId) {
         String[] parts = this.splitQuery(query);
         if(parts == null)
             return 0L;
         HashSet specimenIds = (HashSet) unstructuredSpecimenQuery(parts);
         //Set intersection -> filter by collection
         specimenIds.retainAll(specimenEAOImpl.findByCollectionId(collectionId));
         Integer i = new Integer(specimenIds.size());
         return i.longValue();
     }
     
     public Long countSpecimensByCriteria(SpecimenDTO inputDTO) {
          Integer i = new Integer(getSpecimenIds(inputDTO).size());
          return i.longValue();
     }
 
 
     /***
      * count specimens by taxonomical 
      * @param inputDTO
      * @param TaxonLevel
      * @return
      */
      public Long countSpecimensByCriteria(SpecimenDTO inputDTO, String TaxonLevel, String catalogEnd,Long initialGathObserDetail,Long finalGathObserDetail,Long initialGathObser, Long finalGathObser,Calendar  initialDate, Calendar finalDate, Long identicatorId) {
         Integer i = new Integer(this.getSpecimenIds(inputDTO,TaxonLevel,catalogEnd,initialGathObserDetail, finalGathObserDetail,initialGathObser, finalGathObser,initialDate, finalDate,identicatorId).size());
          return i.longValue();
     }
 
 
 
     private Set<Long> unstructuredSpecimenQuery(String[] parts){
         Set<Long> finalSpecimenIds = new HashSet();
         List<String> strings = new ArrayList();
         List<Long> numbers = new ArrayList();
         for (String part : parts) {
             try {
                 Long d = Long.parseLong(part);
                 numbers.add(d);
                 strings.add(d.toString());
             } catch (Exception e){
                 strings.add(part.toLowerCase());
             }
         }
 
         for (String inputString : strings) {
             /*******************BUSQUEDA POR RESPONSIBLE NAME******************/
             List<Long> listByResponsibleName =
                     findSpecimenByResponsibleName(inputString);
             finalSpecimenIds.addAll(listByResponsibleName);
             /******************BUSQUEDA POR TAXON NAME*************************/
             List<Long> listByTaxonName =
                     findSpecimenByTaxonName(inputString);
             finalSpecimenIds.addAll(listByTaxonName);
             /********************BUSQUEDA POR SITE DESCRIPTION*****************/
             List<Long> listBySiteDescription =
                     findSpecimenBySiteDescription(inputString);
             finalSpecimenIds.addAll(listBySiteDescription);
             /**************BUSQUEDA POR PROVINCIA Y/O PAIS*********************/
             //TODO
 //            List<Long> listByGeographicLayer =
 //                    findSpecimenByGeographicLayer(inputString);
 //            finalSpecimenIds.addAll(listByGeographicLayer);
             /****************BUSQUEDA POR COLLECTION NAME**********************/
             //Creo que no deberia buscar por collection name, sino devolver
             //aquellos especimenes que pertenecen a la coleccion con la que se
             //trabaja
 //            List<Long> listByCollectionName =
 //                    findSpecimenByCollectionName(inputString);
 //            finalSpecimenIds.addAll(listByCollectionName);
         }
         for (Long inputLong : numbers) {
             /**
              * FIXME: CatalogNumber no necesariamente puede ser casteado a Long
              * Puede incluir letras y numeros. Debe moverse para el "for"
              * anterior donde se tratan Strings
              */
             /****************BUSQUEDA POR CATALOG NUMBER***********************/
             List<Long> listByCatalogNumber = findSpecimenByCatalogNumber(String.
                     valueOf(inputLong.intValue()));
             finalSpecimenIds.addAll(listByCatalogNumber);
         }
         return finalSpecimenIds;
     }
 
     private List<Long> findSpecimenByCollectionName(String name) {
         return specimenEAOImpl.findByCollectionName(name);
     }
 
     private List<Long> findSpecimenByCatalogNumber(String catalogNumber) {
         List list =  new ArrayList<Long>();
         list.add(specimenEAOImpl.findByCatalogNumber(catalogNumber));
         return list;
     }
 
     private List<Long> findSpecimenByGeoLayer(Long geoLayerType, Long layerId) {
         List<Long> specimenIds = new ArrayList();
         List<Long> gathObsIds = findGathObsByGeoLayer(geoLayerType, layerId);
         for (Long gathObsId : gathObsIds) {
             specimenIds.addAll(specimenEAOImpl.findByGathObsId(gathObsId));
         }
         return specimenIds;
     }
 
     /**
      * This method could overload the server.
      * @param name String pattern of the taxon name
      * @return  List of specimens identified until "taxon name"
      */
     private List<Long> findSpecimenByTaxonName(String name) {
         ArrayList<Long> finalSpecimenList = new ArrayList();
         List<Long> taxonIds = taxonEAOImpl.findByTaxonName(name);
         for (Long taxonId : taxonIds) {
             List<Long> specimenList =
                     identificationEAOImpl.findSpecimenByTaxonId(taxonId);            
             finalSpecimenList.addAll(specimenList);
         }
         return finalSpecimenList;
     }
 
     /**
      * This method could overload the server.
      * @param name of the responsible person
      * @return List of specimens related to "person"
      */
     private List<Long> findSpecimenByResponsibleName(String name) {
         ArrayList<Long> finalSpecimenList = new ArrayList();
         List<Long> gathObsList = this.findGathObsByResponsibleName(name);
         for (Long gatObsId : gathObsList) {
             finalSpecimenList.addAll(specimenEAOImpl.findByGathObsId(gatObsId));
         }
         return finalSpecimenList;
     }
 
     /***************************************************************************
      ********************** === LABEL  === ********************************
      **************************************************************************/
 
     private List<Long> findSpecimenByResponsibleId(Long personId) {
         ArrayList<Long> finalSpecimenList = new ArrayList();
         List<Long> gathObsList = this.findGathObsDetailByResponsibleId(personId);
         
         for (Long gatObsId : gathObsList) {
             finalSpecimenList.addAll(specimenEAOImpl.findByGathObsId(gatObsId));
         }
         return finalSpecimenList;
     }
     /**
      * @param inputDTO Built in the advanced search
      * @param base First result retrieved
      * @param offset How many results I want
      * @return List of specimens
      */
     public List<SpecimenDTO> searchSpecimenByCriteria(SpecimenDTO inputDTO, String taxonomyLevel,String catalogEnd,
            Long initialGathObserDetail, Long finalGathObserDetail, Long initialGathObser, Long finalGathObser, Calendar initialDate, Calendar  finalDate, Long identicatorId,int base, int offset) {
         Set<Long> specimenIds =  this.getSpecimenIds(inputDTO,taxonomyLevel, catalogEnd,initialGathObserDetail,finalGathObserDetail, initialGathObser,finalGathObser, initialDate, finalDate, identicatorId);
         List<Specimen> specimenList = new ArrayList();
 
         specimenList = getEntities(specimenIds, Specimen.class, base, offset);
         List<SpecimenDTO> specimenDTOList = specimenDTOFactory.
                 createDTOList(specimenList);
 
         List<SpecimenDTO> updated = inventoryFacadeImpl.updateCountryAndProvinceName
                 (specimenDTOList);
         return inventoryFacadeImpl.updateScientificName(updated);
     }
 
 
     /**
      * get the id of the gathering observation ID
      * @param personId
      * @return
      */
     private List<Long> findGathObsDetailByResponsibleId(Long personId) {
        List<Long> finalGathObsIds = new ArrayList();
 
        List<Long> gathObsByResponsible = this.getGatheringObservationDetailEAOImpl().findByResponsibleId(personId);
                finalGathObsIds.addAll(gathObsByResponsible);
 
         return finalGathObsIds;
     }
 
      private List<Long> findGathObsDetailByResponsibleId(Long personId, Long initialGathObserDetail,Long finalGathObserDetail ) {
        List<Long> finalGathObsIds = new ArrayList();
 
        List<Long> gathObsByResponsible = this.getGatheringObservationDetailEAOImpl().findByResponsibleId(personId, initialGathObserDetail, finalGathObserDetail);
               finalGathObsIds.addAll(gathObsByResponsible);
 
         return finalGathObsIds;
 
     }
    private List<Long> findGathObsDetailByResponsibleId(Long personId, Long initialGathObserDetail ) {
        List<Long> finalGathObsIds = new ArrayList();
 
        List<Long> gathObsByResponsible = this.getGatheringObservationDetailEAOImpl().findByResponsibleId(personId, initialGathObserDetail);
               finalGathObsIds.addAll(gathObsByResponsible);
 
         return finalGathObsIds;
     }
 
     private List<Long> findSpecimenByValuerPersonId(Long personId) {
  
         List<Long> finalSpecimenList = this.identificationEAOImpl.findSpecimenByValuerPersonId(personId);
 
         System.out.println("size "  + finalSpecimenList.size());
       
         return finalSpecimenList;
     }
 
 
     private List<Long> findSpecimenByResponsibleId(Long personId,Long initialGathObs , Long finalGathObs) {
         ArrayList<Long> finalSpecimenList = new ArrayList();
         List<Long> gathObsList = this.findGathObsDetailByResponsibleId(personId,initialGathObs ,finalGathObs);
         
         for (Long gatObsId : gathObsList) {
             finalSpecimenList.addAll(specimenEAOImpl.findByGathObsId(gatObsId));
         }
         return finalSpecimenList;
     }
 
     private List<Long> findSpecimenByResponsibleId(Long personId,Long initialGathObs ) {
         ArrayList<Long> finalSpecimenList = new ArrayList();
         List<Long> gathObsList = this.findGathObsDetailByResponsibleId(personId, initialGathObs);
 
         for (Long gatObsId : gathObsList) {
             finalSpecimenList.addAll(specimenEAOImpl.findByGathObsId(gatObsId));
         }
         return finalSpecimenList;
     }
 
 
     private List<Long> findSpecimenByInitailDateGathering(Calendar initialDate) {
         ArrayList<Long> finalSpecimenList = new ArrayList();
         List<Long> gathObsList = this.gatheringObservationEAOImpl.findByInitialDate(initialDate);
 
         for (Long gatObsId : gathObsList) {
             finalSpecimenList.addAll(specimenEAOImpl.findByGathObsId(gatObsId));
         }
         return finalSpecimenList;
     }
 
 
       private List<Long> findSpecimenByFinalDateGathering(Calendar  finalDate ) {
         ArrayList<Long> finalSpecimenList = new ArrayList();
         List<Long> gathObsList = this.gatheringObservationEAOImpl.findByFinalDate(finalDate);
 
         for (Long gatObsId : gathObsList) {
             finalSpecimenList.addAll(specimenEAOImpl.findByGathObsId(gatObsId));
         }
         return finalSpecimenList;
     }
 
 
         private List<Long> findSpecimenByCatalogNumber(String catalogNumberFirst,String catalogNumberEnd) {
 
         Long catalogEnd = Long.parseLong(catalogNumberEnd);
 
         List<Long> specimenIds = new ArrayList();
         specimenIds = this.specimenEAOImpl.findByCatalogNumber(catalogNumberFirst,catalogNumberEnd);
 
         List<Long> specimenIdsF = new ArrayList();
 
         for (Long Id : specimenIds)
         {
             if(Id <= catalogEnd)
               specimenIdsF.add(Id);
         }
         return  specimenIdsF;
     }
 
         /* modificate by paula*/
      private Set<Long> getSpecimenIds(SpecimenDTO inputDTO, String taxonLevel,String catalogEnd,Long initialGathObserDetail, Long finalGathObserDetail,Long initialGathObser, Long finalGathObser,Calendar initialDate, Calendar finalDate, Long identicatorId) {
         Set<Long> specimenIds = new HashSet();
 
         boolean firstFilter = true; //helps with the intersection of data
 
         String catalogNumber   = inputDTO.getCatalogNumber();
         Long institutionId     = inputDTO.getInstitutionId();
         Long collectionId      = inputDTO.getCollectionId();
         String taxonName       = inputDTO.getTaxonName();
         String localityDescrip = inputDTO.getLocalityDescription();
         Long countryId         = inputDTO.getCountryId();
         Long provinceId        = inputDTO.getProvinceId();
         Double latitude        = inputDTO.getLatitude();
         Double longitude       = inputDTO.getLongitude();
         Integer radius         = inputDTO.getRadio();
         Long responsibleId     = inputDTO.getResponsibleId();
 
 
         if (catalogNumber != null) {
 
             //search by catalog  number
             if(catalogEnd == null)
             {
               List<Long> specimenByCN = findSpecimenByCatalogNumber(catalogNumber);
               specimenIds.addAll(specimenByCN);
               firstFilter = false;
             }
             //search by range of catalog first and catalog last
             else
             {
                 List<Long> specimenByCN = findSpecimenByCatalogNumber(catalogNumber,catalogEnd);
                 specimenIds.addAll(specimenByCN);
                 firstFilter = false;
             }
         }
         if(institutionId != null) {
             List<Long> specimenByInstitution =
                     specimenEAOImpl.findByInstitutionId(institutionId);
             if(firstFilter) {
                 specimenIds.addAll(specimenByInstitution);
                 firstFilter = false;
             } else {
                 specimenIds.retainAll(specimenByInstitution);
             }
         }
         if(collectionId != null) {
             List<Long> specimenByCollection =
                     specimenEAOImpl.findByCollectionId(collectionId);
             if(firstFilter) {
                 specimenIds.addAll(specimenByCollection);
                 firstFilter = false;
             } else {
                 specimenIds.retainAll(specimenByCollection);
             }
         }
         if(taxonName != null && !taxonName.trim().isEmpty()) {
             List<Long> specimenByTaxonName =
                     identificationEAOImpl.findSpecimenByTaxonNameAndTaxonomicalLevel(taxonLevel,taxonName);
             if(firstFilter) {
                 specimenIds.addAll(specimenByTaxonName);
                 firstFilter = false;
             } else {
                 specimenIds.retainAll(specimenByTaxonName);
             }
         }
         if(localityDescrip != null && !localityDescrip.trim().isEmpty()) {
              List<Long> specimenBySiteDescr =
                      findSpecimenBySiteDescription(localityDescrip);
             if(firstFilter) {
                 specimenIds.addAll(specimenBySiteDescr);
                 firstFilter = false;
             } else {
                 specimenIds.retainAll(specimenBySiteDescr);
             }
         }
         if(countryId != null && provinceId != null) {
             List<Long> specimenByGeoLayer =
                 findSpecimenByGeoLayer(GeographicLayerEntity.PROVINCE.getId(),
                 countryId);
             if(firstFilter) {
                 specimenIds.addAll(specimenByGeoLayer);
                 firstFilter = false;
             } else {
                 specimenIds.retainAll(specimenByGeoLayer);
             }
         } else if (countryId != null) {
             List<Long> specimenByGeoLayer =
                 findSpecimenByGeoLayer(GeographicLayerEntity.COUNTRY.getId(),
                 countryId);
             if(firstFilter) {
                 specimenIds.addAll(specimenByGeoLayer);
                 firstFilter = false;
             } else {
                 specimenIds.retainAll(specimenByGeoLayer);
             }
         }
         if(latitude != null && longitude != null && radius != null) {
             List<Long> specimenByCoords =
                     findSpecimenByCoords(latitude, longitude, radius);
             if(firstFilter) {
                 specimenIds.addAll(specimenByCoords);
                 firstFilter = false;
             } else {
                 specimenIds.retainAll(specimenByCoords);
             }
         }
         //preguantar si nos nulos los rangos
         if(responsibleId != null ) {
 
             if(initialGathObserDetail != null  && finalGathObserDetail != null )
             {
                 List<Long> specimenByResponsible =
                         this.findSpecimenByResponsibleId(responsibleId, initialGathObserDetail, finalGathObserDetail);
 
                 if(firstFilter) {
                     specimenIds.addAll(specimenByResponsible);
                     firstFilter = false;
                 } else {
                     specimenIds.retainAll(specimenByResponsible);
                 }
             }
             else  if(initialGathObserDetail != null  && finalGathObserDetail == null )
             {
                   List<Long> specimenByResponsible = this.findSpecimenByResponsibleId(responsibleId,initialGathObserDetail);
 
                   if(firstFilter) {
                     specimenIds.addAll(specimenByResponsible);
                     firstFilter = false;
                 } else {
                     specimenIds.retainAll(specimenByResponsible);
                 }
 
             }
 
             else if(initialGathObserDetail == null  && finalGathObserDetail == null )
             {
 
                   List<Long> specimenByResponsible = this.findSpecimenByResponsibleId(responsibleId);
 
                    if(firstFilter)
                    {
                       specimenIds.addAll(specimenByResponsible);
                       firstFilter = false;
                    }
                    else
                    {
                      specimenIds.retainAll(specimenByResponsible);
                    }
             }
 
         }
 
         else if(initialGathObser != null)
         {
               List<Long> specimenByResponsible;
 
              if(finalGathObser != null)
              {
                 specimenByResponsible =  this.specimenEAOImpl.findByGathObsId(initialGathObser, finalGathObser);
              }
              else
              {
                 specimenByResponsible =  this.specimenEAOImpl.findByGathObsId(initialGathObser);
              }
 
                  if(firstFilter)
                  {
                        specimenIds.addAll(specimenByResponsible);
                        firstFilter = false;
                  }
                  else
                  {
                       specimenIds.retainAll(specimenByResponsible);
                  }
         }
         //search by person id, who identific the person
         else if(identicatorId != null)
         {
             List<Long> specimenByResponsible =  this.findSpecimenByValuerPersonId(identicatorId);
 
              if(firstFilter)
              {
                    specimenIds.addAll(specimenByResponsible);
                    firstFilter = false;
              }
              else
              {
                   specimenIds.retainAll(specimenByResponsible);
              }
         }
         //search by the date of gathering
         else if (initialDate != null)
         {
 
             List<Long> specimenByResponsible =  this.findSpecimenByInitailDateGathering(initialDate);
 
              if(firstFilter)
              {
                    specimenIds.addAll(specimenByResponsible);
                    firstFilter = false;
              }
              else
              {
                   specimenIds.retainAll(specimenByResponsible);
              }
 
         }
         else if(finalDate != null)
         {
 
          List<Long> specimenByResponsible =  this.findSpecimenByFinalDateGathering(finalDate);
 
          if(firstFilter)
          {
                specimenIds.addAll(specimenByResponsible);
                firstFilter = false;
          }
          else
 
          {
               specimenIds.retainAll(specimenByResponsible);
          }
 
         }
         return specimenIds;
     }
 
 
     /**
      * @return the gatheringObservationDetailEAOImpl
      */
     public GatheringObservationDetailEAOLocal getGatheringObservationDetailEAOImpl() {
         return gatheringObservationDetailEAOImpl;
     }
 
     /**
      * @param gatheringObservationDetailEAOImpl the gatheringObservationDetailEAOImpl to set
      */
     public void setGatheringObservationDetailEAOImpl(GatheringObservationDetailEAOLocal gatheringObservationDetailEAOImpl) {
         this.gatheringObservationDetailEAOImpl = gatheringObservationDetailEAOImpl;
     }
     /**
      * This method could overload the server.
      * @param descr String pattern of the locality
      * @return  List of specimens related to multiple localities that matched
      * with the pattern
      */
     private List<Long> findSpecimenBySiteDescription(String descr) {
         ArrayList<Long> finalSpecimenList = new ArrayList();
         List<Long> gathObsList = findGathObsBySiteDescription(descr);
         for (Long gathObsId : gathObsList) {
             finalSpecimenList.addAll(
                     specimenEAOImpl.findByGathObsId(gathObsId));
         }
         return finalSpecimenList;
     }
 
     private List<Long> findSpecimenByCoords(double latitude, double longitude,
             int radius) {
         ArrayList<Long> specimenIds = new ArrayList();
         List<Long> gathObsByCoords =
                 findGathObsByCoordinates(latitude, longitude, radius);
         for (Long gathObsId : gathObsByCoords) {
             specimenIds.addAll(specimenEAOImpl.findByGathObsId(gathObsId));
         }
         return specimenIds;
     }
 // </editor-fold>
     /***************************************************************************
      ************************* === VARIOS === **********************************
      **************************************************************************/
     // <editor-fold defaultstate="collapsed" desc="comment">
     private String[] splitQuery(String query) {
         if(query == null || query.length() == 0)
             return null;
         return query.split(" ");
     }
 
     private List getEntities(Set<Long> ids, Class t, int base, int offset) {
         List entitiesList = new ArrayList();
 		Object[] sortedIdentificationIds = ids.toArray();
         java.util.Arrays.sort(sortedIdentificationIds);
         int entitiesCounter = 0;
         int baseCounter = 0;
         if(sortedIdentificationIds.length > base) {
             for (Object id : sortedIdentificationIds) {
                 if (baseCounter < base) {
                     baseCounter++;
                 } else if(entitiesCounter < offset) {
                     if(t == Identification.class) {
                         entitiesList.add(identificationEAOImpl.
                             findBySpecimenId((Long)id));
                     } else if(t == Specimen.class) {
                         entitiesList.add(specimenEAOImpl.findById(t, (Long)id));
                     } else if(t == GatheringObservation.class) {
                         entitiesList.add(gatheringObservationEAOImpl.findById(t,
                                 (Long)id));
                     } else if(t == Site.class) {
                         entitiesList.add(siteEAOImpl.findById(t, (Long)id));
                     } else if(t == Transaction.class) {
                        entitiesList.add(transactionEAOImpl.findById(t, (Long)id));
                    entitiesCounter++;
                     }
                 }
 
             }
         }
         return entitiesList;
     }
 
     public Long countQueryElements(LinkedList<QueryNode> sll) {
         
         String jpqlQuery = "select count(o) from DarwinCore14 as o where ";
 
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
 
         return darwinCoreEAOImpl.countQueryElements(jpqlQuery);
     }
 
     public List findAllDwCPaginated(int first, int amount) {
         return darwinCoreEAOImpl.findAllDwCPaginated(first, amount);
     }
 
     public List makePaginatedQuery
             (LinkedList<QueryNode> sll, int first, int amount) {
         return darwinCoreEAOImpl.makePaginatedQuery(sll, first, amount);
     }
 
     public Long countAllDwC() {
         return darwinCoreEAOImpl.findTotalDwc();
     }
 
 // </editor-fold>
 
     /***************************************************************************
      ********************** === TRANSACTION === ********************************
      **************************************************************************/
     // <editor-fold defaultstate="collapsed" desc="comment">
     /**
      *
      * @param inputDTO Built in the advanced search
      * @param base First result retrieved
      * @param offset How many results I want
      * @return List of transactions
      */
     public List<TransactionDTO> searchTransactionsByCriteria(TransactionDTO inputTransactionDTO,
             TransactedSpecimenDTO inputTransactedSpecimenDTO, int base, int offset) {
         Set<Long> transactionsIds = getTransactionsIds(inputTransactionDTO, inputTransactedSpecimenDTO);
         List<Transaction> transactionsList = new ArrayList();
 
         transactionsList = getEntities(transactionsIds,
                 Transaction.class, base, offset);
         return transactionFacadeImpl.updateNames(
                 transactionDTOFactory.createDTOList(transactionsList));
     }
 
     /**
      * Used to retrieve some identifications and to know the total amount of
      * records (count)
      * @param inputDTO Built in the advanced search
      * @return Set of identification Ids.
      */
     public Set<Long> getTransactionsIds(TransactionDTO inputTransactionDTO,
             TransactedSpecimenDTO inputTransactedSpecimenDTO) {
         Set<Long> transactionIds = new HashSet();
         boolean firstFilter = true; //helps with the intersection of data
 
         Long collectionId = inputTransactionDTO.getCollectionId();
 
         Long transactionId = inputTransactionDTO.getTransactionId();
         String invoiceNumber = inputTransactionDTO.getInvoiceNumber();
         String description = inputTransactionDTO.getDescription();
         
         
         Long estimatedSpecimenCount = inputTransactionDTO.getEstimatedSpecimenCount();
         Long senderInstitutionId = inputTransactionDTO.getSenderInstitutionId();
         Long senderPersonId = inputTransactionDTO.getSenderPersonId();
         Long receiverInstitutionId = inputTransactionDTO.getReceiverInstitutionId();
         Long receiverPersonId = inputTransactionDTO.getReceiverPersonId();
         Long transactionTypeId = inputTransactionDTO.getTransactionTypeId();
 
         Calendar initialTransactionDate = inputTransactionDTO.getTransactionDate();
         Calendar finalTransactionDate = inputTransactionDTO.getFinalTransactionDate();
         Calendar initialExpirationDate = inputTransactionDTO.getExpirationDate();
         Calendar finalExpirationDate = inputTransactionDTO.getFinalExpirationDate();
 
         // Campos del Transacted Specimen DTO
         String catalogNumber = inputTransactedSpecimenDTO.getCatalogNumber();
         Boolean waitingForReturn = inputTransactedSpecimenDTO.getWaitingForReturn();
         Calendar initialDeliveryDate = inputTransactedSpecimenDTO.getDeliveryDate();
         Calendar finalDeliveryDate = inputTransactedSpecimenDTO.getFinalDeliveryDate();
         Calendar initialReceivingDate = inputTransactedSpecimenDTO.getReceivingDate();
         Calendar finalReceivingDate = inputTransactedSpecimenDTO.getFinalReceivingDate();
         Long transactedSpecimenStatusId = inputTransactedSpecimenDTO.getTransactedSpecimenStatusId();
         String transactedSpecimenDescription = inputTransactedSpecimenDTO.getDescription();
 
         if (transactionId != null) {
             if (transactionEAOImpl.existsTransactionId(transactionId, collectionId)) {
                 transactionIds.add(transactionId);
                 
             }
             firstFilter = false;
         }
         if (invoiceNumber != null && !invoiceNumber.trim().isEmpty()) {
             List<Long> newTransactionIds =
                     this.transactionEAOImpl.findByInvoiceNumber(invoiceNumber, collectionId);
             if (firstFilter) {
                 transactionIds.addAll(newTransactionIds);
                 firstFilter = false;
             } else {
                 transactionIds.retainAll(newTransactionIds);
             }
         }
 
         if (description != null && !description.trim().isEmpty()) {
             List<Long> newTransactionIds =
                     this.transactionEAOImpl.findByDescription(description, collectionId);
             if (firstFilter) {
                 transactionIds.addAll(newTransactionIds);
                 firstFilter = false;
             }
             else {
                 transactionIds.retainAll(newTransactionIds);
             }
         }
 
         if (estimatedSpecimenCount != null) {
             List<Long> newTransactionIds =
                     this.transactionEAOImpl.findByEstimatedSpecimenCount(estimatedSpecimenCount, collectionId);
             if (firstFilter) {
                 transactionIds.addAll(newTransactionIds);
                 firstFilter = false;
             }
             else {
                 transactionIds.retainAll(newTransactionIds);
             }
         }
 
         if (senderInstitutionId != null) {
             List<Long> newTransactionIds =
                     this.transactionEAOImpl.findBySenderInstitutionId(senderInstitutionId, collectionId);
             if (firstFilter) {
                 transactionIds.addAll(newTransactionIds);
                 firstFilter = false;
             }
             else {
                 transactionIds.retainAll(newTransactionIds);
             }
         }
 
         if (senderPersonId != null) {
             List<Long> newTransactionIds =
                     this.transactionEAOImpl.findBySenderPersonId(senderPersonId, collectionId);
             if (firstFilter) {
                 transactionIds.addAll(newTransactionIds);
                 firstFilter = false;
             }
             else {
                 transactionIds.retainAll(newTransactionIds);
             }
         }
 
         if (receiverInstitutionId != null) {
             List<Long> newTransactionIds =
                     this.transactionEAOImpl.findByReceiverInstitutionId(receiverInstitutionId, collectionId);
             if (firstFilter) {
                 transactionIds.addAll(newTransactionIds);
                 firstFilter = false;
             }
             else {
                 transactionIds.retainAll(newTransactionIds);
             }
         }
 
         if (receiverPersonId != null) {
             List<Long> newTransactionIds =
                     this.transactionEAOImpl.findByReceiverPersonId(receiverPersonId, collectionId);
             if (firstFilter) {
                 transactionIds.addAll(newTransactionIds);
                 firstFilter = false;
             }
             else {
                 transactionIds.retainAll(newTransactionIds);
             }
         }
 
         if (transactionTypeId != null) {
             List<Long> newTransactionIds =
                     this.transactionEAOImpl.findByTransactionTypeId(transactionTypeId, collectionId);
             if (firstFilter) {
                 transactionIds.addAll(newTransactionIds);
                 firstFilter = false;
             }
             else {
                 transactionIds.retainAll(newTransactionIds);
             }
         }
 
         if (initialTransactionDate != null || finalTransactionDate != null) {
             List<Long> newTransactionIds =
                     this.transactionEAOImpl.findByTransactionDateRange(initialTransactionDate, finalTransactionDate, collectionId);
             if (firstFilter) {
                 transactionIds.addAll(newTransactionIds);
                 firstFilter = false;
             }
             else {
                 transactionIds.retainAll(newTransactionIds);
             }
         }
 
         if (initialExpirationDate != null || finalExpirationDate != null) {
             List<Long> newTransactionIds =
                     this.transactionEAOImpl.findByExpirationDateRange(initialExpirationDate, finalExpirationDate, collectionId);
             if (firstFilter) {
                 transactionIds.addAll(newTransactionIds);
                 firstFilter = false;
             }
             else {
                 transactionIds.retainAll(newTransactionIds);
             }
         }
 
         if (catalogNumber != null && !catalogNumber.trim().isEmpty()) {
             List<Long> specimen = findSpecimenByCatalogNumber(catalogNumber);
             List<Long> newTransactionIds = new ArrayList();
             if (specimen.size() > 0) {
                 newTransactionIds =
                     this.transactionEAOImpl.findBySpecimenId(collectionId, specimen.get(0));
             }
             if (firstFilter) {
                 transactionIds.addAll(newTransactionIds);
                 firstFilter = false;
             }
             else {
                 transactionIds.retainAll(newTransactionIds);
             }
         }
 
         if (initialDeliveryDate != null || finalDeliveryDate != null) {
             List<Long> newTransactionIds =
                     this.transactionEAOImpl.findByDeliveryDateRange(initialDeliveryDate, finalDeliveryDate, collectionId);
             if (firstFilter) {
                 transactionIds.addAll(newTransactionIds);
                 firstFilter = false;
             }
             else {
                 transactionIds.retainAll(newTransactionIds);
             }
         }
 
         if (initialReceivingDate != null || finalReceivingDate != null) {
             List<Long> newTransactionIds =
                     this.transactionEAOImpl.findByReceivingDateRange(initialReceivingDate, finalReceivingDate, collectionId);
             if (firstFilter) {
                 transactionIds.addAll(newTransactionIds);
                 firstFilter = false;
             }
             else {
                 transactionIds.retainAll(newTransactionIds);
             }
         }
 
         if (transactedSpecimenStatusId != null) {
             List<Long> newTransactionIds =
                     this.transactionEAOImpl.findByTransactedSpecimenStatusId(transactedSpecimenStatusId, collectionId);
             if (firstFilter) {
                 transactionIds.addAll(newTransactionIds);
                 firstFilter = false;
             }
             else {
                 transactionIds.retainAll(newTransactionIds);
             }
         }
 
         if (transactedSpecimenDescription != null && !transactedSpecimenDescription.trim().isEmpty()) {
             List<Long> newTransactionIds =
                     this.transactionEAOImpl.findByTransactedSpecimenDescription(transactedSpecimenDescription, collectionId);
             if (firstFilter) {
                 transactionIds.addAll(newTransactionIds);
                 firstFilter = false;
             }
             else {
                 transactionIds.retainAll(newTransactionIds);
             }
         }
         
         if (catalogNumber != null && !catalogNumber.trim().isEmpty()) {
             List<Long> specimen = findSpecimenByCatalogNumber(catalogNumber);
             List<Long> newTransactionIds = new ArrayList();
             if (specimen.size() > 0) {
                 newTransactionIds =
                     this.transactionEAOImpl.findBySpecimenId(collectionId, specimen.get(0));
             }
             if (firstFilter) {
                 transactionIds.addAll(newTransactionIds);
                 firstFilter = false;
             }
             else {
                 transactionIds.retainAll(newTransactionIds);
             }
         }
 
         if (initialDeliveryDate != null || finalDeliveryDate != null) {
             List<Long> newTransactionIds =
                     this.transactionEAOImpl.findByDeliveryDateRange(initialDeliveryDate, finalDeliveryDate, collectionId);
             if (firstFilter) {
                 transactionIds.addAll(newTransactionIds);
                 firstFilter = false;
             }
             else {
                 transactionIds.retainAll(newTransactionIds);
             }
         }
 
         if (initialReceivingDate != null || finalReceivingDate != null) {
             List<Long> newTransactionIds =
                     this.transactionEAOImpl.findByReceivingDateRange(initialReceivingDate, finalReceivingDate, collectionId);
             if (firstFilter) {
                 transactionIds.addAll(newTransactionIds);
                 firstFilter = false;
             }
             else {
                 transactionIds.retainAll(newTransactionIds);
             }
         }
 
         if (transactedSpecimenStatusId != null) {
             List<Long> newTransactionIds =
                     this.transactionEAOImpl.findByTransactedSpecimenStatusId(transactedSpecimenStatusId, collectionId);
             if (firstFilter) {
                 transactionIds.addAll(newTransactionIds);
                 firstFilter = false;
             }
             else {
                 transactionIds.retainAll(newTransactionIds);
             }
         }
 
         if (transactedSpecimenDescription != null && !transactedSpecimenDescription.trim().isEmpty()) {
             List<Long> newTransactionIds =
                     this.transactionEAOImpl.findByTransactedSpecimenDescription(transactedSpecimenDescription, collectionId);
             if (firstFilter) {
                 transactionIds.addAll(newTransactionIds);
                 firstFilter = false;
             }
             else {
                 transactionIds.retainAll(newTransactionIds);
             }
         }
 
         return transactionIds;
     }
 
     public Long countTransactionsByCriteria(TransactionDTO inputTransactionDTO,
             TransactedSpecimenDTO inputTransactedSpecimenDTO) {
         Integer i = new Integer(getTransactionsIds(inputTransactionDTO, inputTransactedSpecimenDTO).size());
         return i.longValue();
     }
 
     public Long countTransactionsByCriteria(String query, Long collectionId) {
         String[] parts = this.splitQuery(query);
         if(parts == null)
             return 0L;
         HashSet TransactionIds = (HashSet) unstructuredTransactionsQuery(parts, collectionId);
         //Set intersection -> filter by collection
         TransactionIds.retainAll(transactionEAOImpl.
                 findByCollectionId(collectionId));
         Integer i = new Integer(TransactionIds.size());
         return i.longValue();
     }
 
     public List<TransactionDTO> searchTransactionsByCriteria(String query,
             Long collectionId, int base, int offset) {
         List<Transaction> transactionsList = new ArrayList();
         String[] parts = this.splitQuery(query);
         if(parts == null)
             transactionDTOFactory.createDTOList(transactionsList);
 
         HashSet<Long> transactionIds = (HashSet) unstructuredTransactionsQuery(parts, collectionId);
         //Set intersection -> filter by collection
         transactionIds.retainAll(transactionEAOImpl.
                 findByCollectionId(collectionId));
         //Retrieve entities
         transactionsList = getEntities(transactionIds,
                 Transaction.class, base, offset);
         return transactionFacadeImpl.updateNames(transactionDTOFactory.createDTOList(transactionsList));
     }
 
     private Set<Long> unstructuredTransactionsQuery(String[] parts, Long collectionId) {
         HashSet<Long> finalTransactionIds = new HashSet();
         List<String> strings = new ArrayList();
         List<Long> numbers = new ArrayList();
         for (String part : parts) {
             try {
                 Long d = Long.parseLong(part);
                 numbers.add(d);
                 strings.add(d.toString());
             } catch (Exception e){
                 strings.add(part.toLowerCase());
             }
         }
 
         for (String inputString : strings) {
 
             List<Long> listByDescription =
                     this.transactionEAOImpl.findByDescription(inputString, collectionId);
             finalTransactionIds.addAll(listByDescription);
 
             List<Long> listByInvoiceNumber =
                     this.transactionEAOImpl.findByInvoiceNumber(inputString, collectionId);
             finalTransactionIds.addAll(listByInvoiceNumber);
 
             List<Long> listBySenderPersonName =
                     findTransactionBySenderPersonName(inputString, collectionId);
             finalTransactionIds.addAll(listBySenderPersonName);
 
             List<Long> listByReceiverPersonName =
                     findTransactionByReceiverPersonName(inputString, collectionId);
             finalTransactionIds.addAll(listByReceiverPersonName);
 
             List<Long> listByInstitutionName =
                     findTransactionByInstitutionName(inputString, collectionId);
             finalTransactionIds.addAll(listByInstitutionName);
         }
         for (Long inputLong : numbers) {
 
             if(transactionEAOImpl.existsTransactionId(inputLong, collectionId)) {
                 finalTransactionIds.add(inputLong);
             }
         }
         return finalTransactionIds;
     }
 
     private List<Long> findTransactionBySenderPersonName(String name, Long collectionId) {
         ArrayList<Long> finalTransactionList = new ArrayList();
         List<Long> personList = transactionEAOImpl.findPersonIdByPersonName(name);
         for (Long personId : personList) {
             List<Long> transactionsBySenderPersonName =
                     transactionEAOImpl.findBySenderPersonId(personId, collectionId);
             finalTransactionList.addAll(transactionsBySenderPersonName);
         }
         return finalTransactionList;
     }
 
     private List<Long> findTransactionByReceiverPersonName(String name, Long collectionId) {
         ArrayList<Long> finalTransactionList = new ArrayList();
         List<Long> personList = transactionEAOImpl.findPersonIdByPersonName(name);
         for (Long personId : personList) {
             List<Long> transactionsByReceiverPersonName =
                     transactionEAOImpl.findByReceiverPersonId(personId, collectionId);
             finalTransactionList.addAll(transactionsByReceiverPersonName);
         }
         return finalTransactionList;
     }
 
     private List<Long> findTransactionByInstitutionName(String name, Long collectionId) {
         ArrayList<Long> finalTransactionList = new ArrayList();
         List<Long> institutionList = transactionEAOImpl.findInstitutionIdByInstitutionCode(name);
         for (Long institutionId : institutionList) {
             List<Long> transactionsByInstitutionName =
                     transactionEAOImpl.findBySenderInstitutionId(institutionId, collectionId);
             finalTransactionList.addAll(transactionsByInstitutionName);
             transactionsByInstitutionName =
                     transactionEAOImpl.findByReceiverInstitutionId(institutionId, collectionId);
             finalTransactionList.addAll(transactionsByInstitutionName);
         }
         return finalTransactionList;
     }
 
     // </editor-fold>
 }
