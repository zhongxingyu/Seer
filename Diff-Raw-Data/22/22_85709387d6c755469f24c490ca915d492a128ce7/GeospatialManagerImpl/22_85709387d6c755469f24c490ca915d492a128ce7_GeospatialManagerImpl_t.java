 /***************************************************************************
  * Copyright (C) 2005 Global Biodiversity Information Facility Secretariat.
  * All Rights Reserved.
  * The contents of this file are subject to the Mozilla Public
  * License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of
  * the License at http://www.mozilla.org/MPL/
  * Software distributed under the License is distributed on an "AS
  * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * rights and limitations under the License.
  ***************************************************************************/
 package org.gbif.portal.service.impl;
 
 import org.gbif.portal.dao.geospatial.CellDensityDAO;
 import org.gbif.portal.dao.geospatial.CentiCellDensityDAO;
 import org.gbif.portal.dao.geospatial.CountryDAO;
 import org.gbif.portal.dao.geospatial.GeoRegionDAO;
 import org.gbif.portal.dao.occurrence.OccurrenceRecordDAO;
 import org.gbif.portal.dao.resources.DataProviderDAO;
 import org.gbif.portal.dao.resources.DataResourceDAO;
 import org.gbif.portal.dao.tag.BiRelationTagDAO;
 import org.gbif.portal.dao.tag.QuadRelationTagDAO;
 import org.gbif.portal.dao.tag.TagDAO;
 import org.gbif.portal.dto.CountDTO;
 import org.gbif.portal.dto.DTOFactory;
 import org.gbif.portal.dto.KeyValueDTO;
 import org.gbif.portal.dto.SearchResultsDTO;
 import org.gbif.portal.dto.geospatial.CellDensityDTO;
 import org.gbif.portal.dto.geospatial.CellDensityDTOFactory;
 import org.gbif.portal.dto.geospatial.CountryDTO;
 import org.gbif.portal.dto.geospatial.GeoRegionDTO;
 import org.gbif.portal.dto.geospatial.GeoRegionDTOFactory;
 import org.gbif.portal.dto.tag.BiRelationTagDTO;
 import org.gbif.portal.dto.tag.QuadRelationTagDTO;
 import org.gbif.portal.dto.util.BoundingBoxDTO;
 import org.gbif.portal.dto.util.EntityType;
 import org.gbif.portal.dto.util.SearchConstraints;
 import org.gbif.portal.model.ModelEntityType;
 import org.gbif.portal.model.geospatial.CellDensity;
 import org.gbif.portal.model.geospatial.CentiCellDensity;
 import org.gbif.portal.model.geospatial.Country;
 import org.gbif.portal.model.geospatial.CountryName;
 import org.gbif.portal.model.geospatial.GeoRegion;
 import org.gbif.portal.service.GeospatialManager;
 import org.gbif.portal.service.ServiceException;
 import org.gbif.portal.util.geospatial.CellIdUtils;
 import org.gbif.portal.util.geospatial.UnableToGenerateCellIdException;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 
import net.sibcolombia.portal.dao.geospatial.DepartmentDAO;
import net.sibcolombia.portal.model.geospatial.Department;
 import org.apache.commons.lang.math.NumberUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * An implementation of the GeospatialManager interface that makes use of the
  * DAO layer objects for data access.
  * 
  * @author trobertson
  * @author dmartin
  */
 public class GeospatialManagerImpl implements GeospatialManager {
 
   protected static Log logger = LogFactory.getLog(GeospatialManagerImpl.class);
 
   /**
    * Default ISO code to use when locale is null
    * TODO Need to move to somewhere more accessible
    */
   protected String defaultISOLanguageCode = "en";
 
   /** DAOs */
   protected CellDensityDAO cellDensityDAO;
   protected CentiCellDensityDAO centiCellDensityDAO;
   /** Country DAO for Country queries */
   protected CountryDAO countryDAO;

  /** Department DAO for Department queries */
  protected DepartmentDAO departmentDAO;

   /** The GeoRegion DAO */
   protected GeoRegionDAO geoRegionDAO;
   /** Occurrence Record DAO */
   protected OccurrenceRecordDAO occurrenceRecordDAO;
   /** Data Resource DAO */
   protected DataResourceDAO dataResourceDAO;
   /** Data Provider DAO */
   protected DataProviderDAO dataProviderDAO;
 
   /** Tag DAO for relationship tags between host and country */
   protected BiRelationTagDAO hostCountryTagDAO;
 
   /** Tag DAO for relationship tags between host, country, kingdom & basis of record */
   protected QuadRelationTagDAO hostCountryKingdomBasisTagDAO;
 
   /** DTO factories */
   protected CellDensityDTOFactory cellDensityDTOFactory;
 
   protected GeoRegionDTOFactory geoRegionDTOFactory = new GeoRegionDTOFactory();
   /** DTO Factory for CountryDTOs */
   protected DTOFactory countryDTOFactory;
   /** DTO Factory for CountDTOs */
   protected DTOFactory countDTOFactory;
   /** The DTO factory for creating Key Value DTOs **/
   protected DTOFactory keyValueDTOFactory;
 
 
   protected boolean useCentiCellTable = true;
 
   /** An id used to associate the unknown country with a set of layers and properties in the database */
   protected Long unknownCountryId = 0l;
 
   /**
    * Parses the supplied key. Returns null if supplied string invalid
    * 
    * @param key
    * @return a concept key. Returns null if supplied string invalid key
    */
   protected static Long parseKey(String key) {
     Long parsedKey = null;
     try {
       parsedKey = Long.parseLong(key);
     } catch (NumberFormatException e) {
       // expected behaviour for invalid keys
     }
     return parsedKey;
   }
 
   /**
    * @param logger the logger to set
    */
   public static void setLogger(Log logger) {
     GeospatialManagerImpl.logger = logger;
   }
 
   /**
    * @see org.gbif.portal.service.GeospatialManager#countGeoreferencedPointsFor(org.gbif.portal.dto.util.EntityType)
    */
   public int countGeoreferencedPointsFor(EntityType entityType, List<String> entityKeys) throws ServiceException {
     if (logger.isDebugEnabled()) {
       logger.debug("retrieving counts for the entity type: " + entityType + ", with entity keys: " + entityKeys);
     }
     List<Long> entityIds = getEntityIds(entityType, entityKeys);
     ModelEntityType cdt = ModelEntityType.getModelEntityType(entityType.getId());
     return cellDensityDAO.getCellDensitiesTotal(cdt, entityIds);
   }
 
   /**
    * @see org.gbif.portal.service.GeospatialManager#countGeoreferencedPointsFor(org.gbif.portal.dto.util.EntityType,
    *      org.gbif.portal.dto.util.BoundingBoxDTO)
    */
   public int countGeoreferencedPointsFor(EntityType entityType, List<String> entityKeys, BoundingBoxDTO boundingBox)
     throws ServiceException {
     List<Long> entityIds = getEntityIds(entityType, entityKeys);
     ModelEntityType cdt = ModelEntityType.getModelEntityType(entityType.getId());
     try {
       int[] minMaxCellIds =
         CellIdUtils.getMinMaxCellIdsForBoundingBox(boundingBox.getLeft(), boundingBox.getLower(),
           boundingBox.getRight(), boundingBox.getUpper());
       return cellDensityDAO.getCellDensitiesTotal(cdt, entityIds, minMaxCellIds[0], minMaxCellIds[1]);
     } catch (UnableToGenerateCellIdException e) {
       logger.error(e.getMessage(), e);
       throw new ServiceException(e);
     }
   }
 
   /**
    * @see org.gbif.portal.service.GeospatialManager#countGeoreferencedPointsFor(org.gbif.portal.dto.util.EntityType)
    */
   public int countGeoreferencedPointsFor(EntityType entityType, String entityKey) throws ServiceException {
     List<String> keys = new ArrayList<String>();
     keys.add(entityKey);
     return countGeoreferencedPointsFor(entityType, keys);
   }
 
   /**
    * @see org.gbif.portal.service.GeospatialManager#countGeoreferencedPointsFor(org.gbif.portal.dto.util.EntityType,
    *      java.lang.String, org.gbif.portal.dto.util.BoundingBoxDTO)
    */
   public int countGeoreferencedPointsFor(EntityType entityType, String entityKey, BoundingBoxDTO boundingBox)
     throws ServiceException {
     List<String> keys = new ArrayList<String>();
     keys.add(entityKey);
     return countGeoreferencedPointsFor(entityType, keys, boundingBox);
   }
 
   /**
    * @see org.gbif.portal.service.GeospatialManager#findCountries(java.lang.String, boolean, java.util.Locale,
    *      org.gbif.portal.dto.util.SearchConstraints)
    */
   public SearchResultsDTO findAllCountries(Locale locale) {
     List<Country> countries = countryDAO.findAllCountries(locale);
     return countryDTOFactory.createResultsDTO(countries, 1000);
   }
 
   /**
    * @see org.gbif.portal.service.GeospatialManager#findCountries(java.lang.String, boolean, java.util.Locale,
    *      org.gbif.portal.dto.util.SearchConstraints)
    */
   public SearchResultsDTO findCountries(String nameStub, boolean fuzzy, boolean anyOccurrence,
     boolean onlySearchInLocale, Locale locale, SearchConstraints searchConstraints) {
 
     // search returns a country and the name used to match that country
     List<Object[]> countryAndInterpretedNames =
       countryDAO.findCountriesFor(nameStub, fuzzy, anyOccurrence, onlySearchInLocale, locale,
         searchConstraints.getStartIndex(), 1000);
     // sort out duplicates
     List distinctMatches = new ArrayList<Object[]>();
     List<String> isoCodes = new ArrayList<String>();
     for (Object[] countryAndInterpretedName : countryAndInterpretedNames) {
       Country country = (Country) countryAndInterpretedName[0];
       String interpretedFrom = (String) countryAndInterpretedName[1];
 
       if (!isoCodes.contains(country.getIsoCountryCode())) {
         isoCodes.add(country.getIsoCountryCode());
 
         // get the locale specific name
         Set<CountryName> countryNames = country.getCountryNames();
         // get the locale specific name
         String localeCountryName = null;
         // get the locale specific name
         String defaultLocaleCountryName = null;
 
         for (CountryName countryName : countryNames) {
           if (defaultISOLanguageCode.equals(countryName.getLocale())) {
             defaultLocaleCountryName = countryName.getName();
             // if supplied locale is null, no point looking further
             if (locale != null)
               break;
           }
           if (locale != null) {
             if (countryName.getLocale() != null && countryName.getLocale().equals(locale.getLanguage())) {
               localeCountryName = countryName.getName();
               break;
             }
           }
         }
         if (localeCountryName == null) {
           localeCountryName = defaultLocaleCountryName;
         }
         distinctMatches.add(new Object[] {country, localeCountryName, interpretedFrom});
       }
     }
 
     return countryDTOFactory.createResultsDTO(distinctMatches, searchConstraints.getMaxResults());
   }
 
   /**
    * @see org.gbif.portal.service.GeospatialManager#get0Point1DegCellDensities(int, java.lang.String, int)
    */
   @SuppressWarnings("unchecked")
   public List<CellDensityDTO> get0Point1DegCellDensities(EntityType type, List<String> keys, int cellId)
     throws ServiceException {
     List<Long> keysAsLongs = getEntityIds(type, keys);
     if (keysAsLongs != null && !keysAsLongs.isEmpty()) {
 // if(useCentiCellTable){
       ModelEntityType cdt = ModelEntityType.getModelEntityType(type.getId());
       List<CentiCellDensity> results = centiCellDensityDAO.getCentiCellDensities(cdt, keysAsLongs, cellId);
       return cellDensityDTOFactory.createDTOListFromCentiCellList(results);
 // } else {
 // List results = get0Point1DegCellDensitiesUsingOccurrenceRecord(type, keysAsLongs, cellId);
 // return cellDensityDTOFactory.createDTOList(results);
 // }
     }
     return new LinkedList<CellDensityDTO>();
   }
 
   /**
    * @see org.gbif.portal.service.GeospatialManager#get0Point1DegCellDensities(org.gbif.portal.dto.util.EntityType,
    *      java.lang.String, int)
    */
   public List<CellDensityDTO> get0Point1DegCellDensities(EntityType type, String key, int cellId)
     throws ServiceException {
     List<String> keys = new ArrayList<String>();
     keys.add(key);
     return get0Point1DegCellDensities(type, keys, cellId);
   }
 
   /**
    * @see org.gbif.portal.service.GeospatialManager#get1DegCellDensities(int, java.lang.String)
    */
   @SuppressWarnings("unchecked")
   public List<CellDensityDTO> get1DegCellDensities(EntityType type, List<String> keys) throws ServiceException {
     if (logger.isDebugEnabled())
       logger.debug("retrieving type:" + type);
     ModelEntityType cdt = ModelEntityType.getModelEntityType(type.getId());
     List<Long> keyAsLong = getEntityIds(type, keys);
     if (cdt != null && keyAsLong != null) {
       List<CellDensity> results = cellDensityDAO.getCellDensities(cdt, keyAsLong);
       if (logger.isDebugEnabled())
         logger.debug("Search returned: " + results.size());
       return cellDensityDTOFactory.createDTOList(results);
     }
     return new LinkedList<CellDensityDTO>();
   }
 
 // /**
 // * Retrieves the 0.1 degree cell densities using the occurrence record table.
 // *
 // * @param type
 // * @param keyAsLong
 // * @param cellId
 // * @return
 // * @throws ServiceException
 // */
 // private List<CellDensity> get0Point1DegCellDensitiesUsingOccurrenceRecord(EntityType type, List<Long> keysAsLongs,
 // int cellId) throws ServiceException {
 // if (type== EntityType.TYPE_TAXON) {
 // logger.debug("Performing taxon search");
 // return occurrenceRecordDAO.getCentiCellDensitiesForTaxonConcept(keysAsLongs, cellId);
 //
 // } else if (type == EntityType.TYPE_RESOURCE_NETWORK) {
 // logger.debug("Performing resource network search");
 // return occurrenceRecordDAO.getCentiCellDensitiesForResourceNetwork(keysAsLongs, cellId);
 //
 // } else if (type == EntityType.TYPE_DATA_PROVIDER) {
 // logger.debug("Performing data provider search");
 // return occurrenceRecordDAO.getCentiCellDensitiesForDataProvider(keysAsLongs, cellId);
 //
 // } else if (type == EntityType.TYPE_DATA_RESOURCE) {
 // logger.debug("Performing data resource search");
 // return occurrenceRecordDAO.getCentiCellDensitiesForDataResource(keysAsLongs, cellId);
 //
 // } else if (type == EntityType.TYPE_COUNTRY) {
 // CountryDTO countryDTO = getCountryFor(Long.toString(keysAsLongs), null);
 // if (countryDTO != null) {
 // if(logger.isDebugEnabled())
 // logger.debug("Performing country search for " + countryDTO.getIsoCountryCode());
 // return occurrenceRecordDAO.getCentiCellDensitiesForIsoCountryCode(countryDTO.getIsoCountryCode(), cellId);
 // } else {
 // logger.debug("No country code found for: " + keyAsLong);
 // }
 // } else {
 // logger.debug("Type " + type + " is not known - ignoring and no search will be issued");
 // }
 // return new LinkedList<CellDensity>();
 // }
 
   /**
    * @see org.gbif.portal.service.GeospatialManager#get1DegCellDensities(org.gbif.portal.dto.util.EntityType,
    *      java.lang.String, org.gbif.portal.dto.util.BoundingBoxDTO)
    */
   public List<CellDensityDTO> get1DegCellDensities(EntityType type, List<String> keys, BoundingBoxDTO boundingBox)
     throws ServiceException {
     ModelEntityType cdt = ModelEntityType.getModelEntityType(type.getId());
     List<Long> keysAsLongs = getEntityIds(type, keys);
     if (cdt != null && keysAsLongs != null && !keysAsLongs.isEmpty()) {
 
       int[] minMaxCellIds = null;
       try {
         minMaxCellIds =
           CellIdUtils.getMinMaxCellIdsForBoundingBox(boundingBox.getLeft(), boundingBox.getLower(),
             boundingBox.getRight(), boundingBox.getUpper());
       } catch (UnableToGenerateCellIdException e) {
         logger.error(e.getMessage(), e);
         return new LinkedList<CellDensityDTO>();
       }
       List<CellDensity> results = cellDensityDAO.getCellDensities(cdt, keysAsLongs, minMaxCellIds[0], minMaxCellIds[1]);
       return cellDensityDTOFactory.createDTOList(results);
     }
     return new LinkedList<CellDensityDTO>();
   }
 
 
   /**
    * @see org.gbif.portal.service.GeospatialManager#get1DegCellDensities(int, java.lang.String, java.util.Set)
    */
   @SuppressWarnings("unchecked")
   public List<CellDensityDTO> get1DegCellDensities(EntityType type, List<String> keys, Set<Integer> cellIds)
     throws ServiceException {
     if (logger.isDebugEnabled())
       logger.debug("retrieving type:" + type);
     ModelEntityType cdt = ModelEntityType.getModelEntityType(type.getId());
     // special case for ISO country codes
     List<Long> keysAsLongs = getEntityIds(type, keys);
     if (cdt != null && keysAsLongs != null) {
       List<CellDensity> results = cellDensityDAO.getCellDensities(cdt, keysAsLongs, cellIds);
       return cellDensityDTOFactory.createDTOList(results);
     }
     return new LinkedList<CellDensityDTO>();
   }
 
   /**
    * @see org.gbif.portal.service.GeospatialManager#get1DegCellDensities(org.gbif.portal.dto.util.EntityType,
    *      java.lang.String)
    */
   public List<CellDensityDTO> get1DegCellDensities(EntityType type, String key) throws ServiceException {
     List<String> keys = new ArrayList<String>();
     keys.add(key);
     return get1DegCellDensities(type, keys);
   }
 
   /**
    * @see org.gbif.portal.service.GeospatialManager#get1DegCellDensities(org.gbif.portal.dto.util.EntityType,
    *      java.lang.String, org.gbif.portal.dto.util.BoundingBoxDTO)
    */
   public List<CellDensityDTO> get1DegCellDensities(EntityType type, String key, BoundingBoxDTO boundingBox)
     throws ServiceException {
     List<String> keys = new ArrayList<String>();
     keys.add(key);
     return get1DegCellDensities(type, key, boundingBox);
   }
 
   /**
    * @see org.gbif.portal.service.GeospatialManager#get1DegCellDensities(org.gbif.portal.dto.util.EntityType,
    *      java.lang.String, java.util.Set)
    */
   public List<CellDensityDTO> get1DegCellDensities(EntityType type, String key, Set<Integer> cellIds)
     throws ServiceException {
     List<String> keys = new ArrayList<String>();
     keys.add(key);
     return get1DegCellDensities(type, keys, cellIds);
   }
 
   /**
    * @see org.gbif.portal.service.GeospatialManager#getAllGeoRegions()
    */
   public List<GeoRegionDTO> getAllGeoRegions() {
     List<GeoRegion> grs = geoRegionDAO.getGeoRegions();
     return geoRegionDTOFactory.createDTOList(grs);
   }
 
   /**
    * @see org.gbif.portal.service.GeospatialManager#getCountriesFor(java.lang.Character, boolean, java.util.Locale)
    */
   public List<CountryDTO> getCountriesFor(Character firstChar, boolean additionSorting, Locale locale) {
     List<Country> countries = countryDAO.getCountriesFor(firstChar, additionSorting, locale);
     return countryDTOFactory.createDTOList(countries);
   }
 
   /**
    * @see org.gbif.portal.service.GeospatialManager#getCountriesForDataResource(java.lang.String, boolean)
    */
   public List<CountDTO> getCountriesForDataResource(String dataResourceKey, boolean geoRefOnly) throws ServiceException {
     List<Object[]> counts = dataResourceDAO.getCountryCountsForDataResource(parseKey(dataResourceKey), geoRefOnly);
     return countDTOFactory.createDTOList(counts);
   }
 
   /**
    * @see org.gbif.portal.service.GeospatialManager#getCountriesForRegion(java.lang.String, java.util.Locale)
    */
   public List<CountryDTO> getCountriesForRegion(String regionCode, Locale locale) {
     List<Object[]> countries = countryDAO.getCountriesForRegion(regionCode, locale);
     return countryDTOFactory.createDTOList(countries);
   }
 
   /**
    * @see org.gbif.portal.service.GeospatialManager#getCountryAlphabet(java.util.Locale)
    */
   public List<Character> getCountryAlphabet(Locale locale) {
     return countryDAO.getCountryAlphabet(locale);
   }
 
   public List<CountDTO> getCountryCountsForCountry(String isoCountryCode, boolean geoRefOnly, Locale locale)
     throws ServiceException {
     List<Object[]> counts = countryDAO.getCountryCountsForCountry(isoCountryCode, geoRefOnly, locale);
 
 
     return countDTOFactory.createDTOList(counts);
   }
 
   /**
    * @see org.gbif.portal.service.GeospatialManager#getCountryFor(java.lang.String, java.util.Locale)
    */
   public CountryDTO getCountryFor(String countryKey, Locale locale) {
     Long countryId = parseKey(countryKey);
     if (countryId == null)
       return getCountryForIsoCountryCode(countryKey, locale);
     Object country = countryDAO.getCountryFor(countryId, locale);
     return (CountryDTO) countryDTOFactory.createDTO(country);
   }
 
   /**
    * @see org.gbif.portal.service.GeospatialManager#getCountryForIPAddress(java.lang.String, java.util.Locale)
    */
   public CountryDTO getCountryForIPAddress(String ipAddress, Locale locale) {
     Object country = countryDAO.getCountryForIP(ipAddress, locale);
     return (CountryDTO) countryDTOFactory.createDTO(country);
   }
 
   /**
    * @see org.gbif.portal.service.GeospatialManager#getCountryForIsoCountryCode(java.lang.String, java.util.Locale)
    */
   public CountryDTO getCountryForIsoCountryCode(String isoCountryCode, Locale locale) {
     Object country = countryDAO.getCountryForIsoCountryCode(isoCountryCode, locale);
     return (CountryDTO) countryDTOFactory.createDTO(country);
   }
 
   /**
    * Retrieve the country id for the supplied iso code.
    * 
    * @param hostCountryCode
    * @return
    */
   private Long getCountryIdForIso(String isoCountryCode) {
     if (isoCountryCode == null)
       return unknownCountryId;
 
     Object[] host = (Object[]) countryDAO.getCountryForIsoCountryCode(isoCountryCode, new Locale("en"));
     return ((Country) host[0]).getCountryId();
   }
 
   public List<KeyValueDTO> getCountryList() {
     List countryKVs = countryDAO.getCountryList();
     return keyValueDTOFactory.createDTOList(countryKVs);
   }
 
   /**
    * @see org.gbif.portal.service.GeospatialManager#getDataProviderCountsForCountry(java.lang.String)
    */
   public List<CountDTO> getDataProviderCountsForCountry(String isoCountryCode) throws ServiceException {
     List<Object[]> counts = dataProviderDAO.getDataProviderCountsForCountry(isoCountryCode);
     return countDTOFactory.createDTOList(counts);
   }
 
   /**
 	 * 
 	 */
   public List<CountDTO> getDataResourceCountsForCountry(String isoCountryCode, boolean geoRefOnly)
     throws ServiceException {
     List<Object[]> counts = dataResourceDAO.getDataResourceCountsForCountry(isoCountryCode, geoRefOnly);
     return countDTOFactory.createDTOList(counts);
   }
 
   /**
    * Util method that handles non numeric entity ids.
    * 
    * @param key
    * @return
    */
   private Long getEntityId(EntityType type, String key) {
     // special case for ISO country codes
     Long keyAsLong = parseKey(key);
     if (type.equals(EntityType.TYPE_COUNTRY) || type.equals(EntityType.TYPE_HOME_COUNTRY)) {
       Object countryAndName = countryDAO.getCountryForIsoCountryCode(key, null);
       if (countryAndName != null && countryAndName instanceof Object[]) {
         Object[] cn = (Object[]) countryAndName;
         if (cn.length > 0) {
           Country country = (Country) cn[0];
           keyAsLong = country.getCountryId();
         }
       }
    } else if (type.equals(EntityType.TYPE_DEPARTMENT)) {
      Object departmentAndName = departmentDAO.getDepartmentForIsoDepartmentCode(key);
      if (departmentAndName != null) {
        Department department = (Department) departmentAndName;
        keyAsLong = department.getDepartmentId();
      }
     }
     return keyAsLong;
   }
 
   private List<Long> getEntityIds(EntityType type, List<String> keysAsStrings) {
     List<Long> keys = new ArrayList<Long>();
     for (String key : keysAsStrings)
       keys.add(getEntityId(type, key));
     return keys;
   }
 
   /**
    * @see org.gbif.portal.service.GeospatialManager#getGeoRegionFor()
    */
   public GeoRegionDTO getGeoRegionFor(String geoRegionKey) {
     GeoRegion gr = geoRegionDAO.getGeoRegionFor(parseKey(geoRegionKey));
     return (GeoRegionDTO) geoRegionDTOFactory.createDTO(gr);
   }
 
   public List<KeyValueDTO> getGeoRegionList(String isoCountryCode) {
     List keyValues = geoRegionDAO.getGeoRegionsForCountry(isoCountryCode);
     return keyValueDTOFactory.createDTOList(keyValues);
   }
 
   /**
    * @see org.gbif.portal.service.GeospatialManager#getHostCountryISOCountryCodes()
    */
   @SuppressWarnings("unchecked")
   public List<String> getHostCountryISOCountryCodes() {
     List<String> sorted = new LinkedList<String>();
     List<String> codes = countryDAO.getHostCountryISOCountryCodes();
     for (String code : codes) {
       if (code != null) {
         sorted.add(code);
       }
     }
     Collections.sort(sorted);
     return sorted;
   }
 
   /**
    * @see org.gbif.portal.service.GeospatialManager#getTotalCountryCount()
    */
   public int getTotalCountryCount() throws ServiceException {
     return countryDAO.getTotalCountryCount();
   }
 
   /**
    * @see org.gbif.portal.service.GeospatialManager#getTotalsPerCountry(org.gbif.portal.dto.util.EntityType,
    *      java.lang.String)
    */
   public List<CountDTO> getTotalsPerCountry(EntityType type, String key) throws ServiceException {
     ModelEntityType met = ModelEntityType.getModelEntityType(type.getName());
     List<Object[]> counts = cellDensityDAO.getTotalsPerCountry(met, parseKey(key));
     return countDTOFactory.createDTOList(counts);
   }
 
   /**
    * @see org.gbif.portal.service.GeospatialManager#getTotalsPerRegion(org.gbif.portal.dto.util.EntityType,
    *      java.lang.String)
    */
   public List<CountDTO> getTotalsPerRegion(EntityType type, String key) throws ServiceException {
     ModelEntityType met = ModelEntityType.getModelEntityType(type.getName());
     List<Object[]> counts = cellDensityDAO.getTotalsPerRegion(met, parseKey(key));
     return countDTOFactory.createDTOList(counts);
   }
 
   /**
    * @see org.gbif.portal.service.GeospatialManager#isValidCountryKey(java.lang.String)
    */
   public boolean isValidCountryKey(String countryKey) {
     return parseKey(countryKey) != null;
   }
 
   /**
    * @see org.gbif.portal.service.GeospatialManager#isValidISOCountryCode(java.lang.String)
    */
   public boolean isValidISOCountryCode(String isoCountryCode) {
     if (isoCountryCode != null && isoCountryCode.length() == 2) {
       if (!NumberUtils.isNumber(isoCountryCode))
         return true;
     }
     return false;
   }
 
   /**
    * @see org.gbif.portal.service.GeospatialManager#retrieveBreakdownForCountry(java.lang.String)
    */
   public List<QuadRelationTagDTO> retrieveBreakdownForCountry(String isoCountryCode) throws Exception {
     Long countryId = getCountryIdForIso(isoCountryCode);
     return hostCountryKingdomBasisTagDAO.retrieveQuadRelationTagsForEntity2(TagDAO.HOSTCOUNTRY_COUNTRY_TAG_ID,
       countryId);
   }
 
   /**
    * @see org.gbif.portal.service.GeospatialManager#retrieveBreakdownForHost(java.lang.String)
    */
   public List<QuadRelationTagDTO> retrieveBreakdownForHost(String hostCountryCode) throws Exception {
     Long hostId = getCountryIdForIso(hostCountryCode);
     return hostCountryKingdomBasisTagDAO.retrieveQuadRelationTagsForEntity1(TagDAO.HOSTCOUNTRY_COUNTRY_TAG_ID, hostId);
   }
 
   /**
    * @see org.gbif.portal.service.GeospatialManager#retrieveBreakdownForHostCountry(java.lang.String, java.lang.String)
    */
   public List<QuadRelationTagDTO> retrieveBreakdownForHostCountry(String hostCountryCode, String isoCountryCode)
     throws Exception {
     Long hostId = getCountryIdForIso(hostCountryCode);
     Long countryId = getCountryIdForIso(isoCountryCode);
     return hostCountryKingdomBasisTagDAO.retrieveQuadRelationTagsFor(TagDAO.HOSTCOUNTRY_COUNTRY_TAG_ID, hostId,
       countryId);
   }
 
   /**
    * @see org.gbif.portal.service.GeospatialManager#retrieveCountriesWithDataHostedBy(java.lang.String)
    */
   public List<BiRelationTagDTO> retrieveCountriesWithDataHostedBy(String isoCountryCode) throws Exception {
 
     Long countryId = getCountryIdForIso(isoCountryCode);
     return hostCountryTagDAO.retrieveFromBiRelationTagsFor(TagDAO.HOSTCOUNTRY_COUNTRY_TAG_ID, countryId);
   }
 
   /**
    * @see org.gbif.portal.service.GeospatialManager#retrieveHostCountriesWithDataFor(java.lang.String)
    */
   public List<BiRelationTagDTO> retrieveHostCountriesWithDataFor(List<String> isoCountryCodes) throws Exception {
     List<BiRelationTagDTO> list = new ArrayList<BiRelationTagDTO>();
     for (String isoCountryCode : isoCountryCodes) {
       list.addAll(retrieveHostCountriesWithDataFor(isoCountryCode));
     }
     return list;
   }
 
   /**
    * @see org.gbif.portal.service.GeospatialManager#retrieveHostCountriesWithDataFor(java.lang.String)
    */
   public List<BiRelationTagDTO> retrieveHostCountriesWithDataFor(String isoCountryCode) throws Exception {
     Long countryId = getCountryIdForIso(isoCountryCode);
     return hostCountryTagDAO.retrieveToBiRelationTagsFor(TagDAO.HOSTCOUNTRY_COUNTRY_TAG_ID, countryId);
   }
 
   /**
    * @see org.gbif.portal.service.GeospatialManager#retrieveHostCountriesWithDataForRegion(java.util.List)
    */
   public List<BiRelationTagDTO> retrieveHostCountriesWithDataForRegion(List<String> regionCountryCodes)
     throws Exception {
     List<BiRelationTagDTO> list = new ArrayList<BiRelationTagDTO>();
     for (String regionCountryCode : regionCountryCodes) {
       List<Object[]> countries = countryDAO.getCountriesForRegion(regionCountryCode, new Locale("en"));
       for (Object[] country : countries) {
         list.addAll(retrieveHostCountriesWithDataFor(((Country) country[0]).getIsoCountryCode()));
       }
     }
     return list;
   }
 
   /**
    * @see org.gbif.portal.service.GeospatialManager#retrieveRepatTable()
    */
   public List<BiRelationTagDTO> retrieveRepatTable() throws Exception {
     return hostCountryTagDAO.retrieveBiRelationTagsFor(TagDAO.HOSTCOUNTRY_COUNTRY_TAG_ID);
   }
 
   /**
    * @see org.gbif.portal.service.GeospatialManager#retrieveTotalRecordHostedInFor(java.lang.String, java.lang.String)
    */
   public BiRelationTagDTO retrieveTotalRecordHostedInFor(String hostCountryCode, String isoCountryCode)
     throws Exception {
 
     Long hostId = getCountryIdForIso(hostCountryCode);
     Long countryId = getCountryIdForIso(isoCountryCode);
 
     List<BiRelationTagDTO> rt =
       hostCountryTagDAO.retrieveBiRelationTagFor(TagDAO.HOSTCOUNTRY_COUNTRY_TAG_ID, hostId, countryId);
     if (rt != null && !rt.isEmpty() && rt.size() == 1) {
       return rt.get(0);
     }
 
     return new BiRelationTagDTO(TagDAO.HOSTCOUNTRY_COUNTRY_TAG_ID, hostId, hostCountryCode, countryId, isoCountryCode,
       0);
   }
 
   /**
    * @param cellDensityDAO the cellDensityDAO to set
    */
   public void setCellDensityDAO(CellDensityDAO cellDensityDAO) {
     this.cellDensityDAO = cellDensityDAO;
   }
 
   /**
    * @param cellDensityDTOFactory the cellDensityDTOFactory to set
    */
   public void setCellDensityDTOFactory(CellDensityDTOFactory cellDensityDTOFactory) {
     this.cellDensityDTOFactory = cellDensityDTOFactory;
   }
 
   /**
    * @param centiCellDensityDAO the centiCellDensityDAO to set
    */
   public void setCentiCellDensityDAO(CentiCellDensityDAO centiCellDensityDAO) {
     this.centiCellDensityDAO = centiCellDensityDAO;
   }
 
   /**
    * @param countDTOFactory the countDTOFactory to set
    */
   public void setCountDTOFactory(DTOFactory countDTOFactory) {
     this.countDTOFactory = countDTOFactory;
   }
 
   /**
    * @param countryDAO the countryDAO to set
    */
   public void setCountryDAO(CountryDAO countryDAO) {
     this.countryDAO = countryDAO;
   }
 
   /**
    * @param countryDTOFactory the countryDTOFactory to set
    */
   public void setCountryDTOFactory(DTOFactory countryDTOFactory) {
     this.countryDTOFactory = countryDTOFactory;
   }
 
   /**
    * @param dataProviderDAO the dataProviderDAO to set
    */
   public void setDataProviderDAO(DataProviderDAO dataProviderDAO) {
     this.dataProviderDAO = dataProviderDAO;
   }
 
   /**
    * @param dataResourceDAO the dataResourceDAO to set
    */
   public void setDataResourceDAO(DataResourceDAO dataResourceDAO) {
     this.dataResourceDAO = dataResourceDAO;
   }
 
   /**
    * @param defaultISOLanguageCode the defaultISOLanguageCode to set
    */
   public void setDefaultISOLanguageCode(String defaultISOLanguageCode) {
     this.defaultISOLanguageCode = defaultISOLanguageCode;
   }
 
   /**
   * @param departmentDAO the departmentDAO to set
   */
  public void setDepartmentDAO(DepartmentDAO departmentDAO) {
    this.departmentDAO = departmentDAO;
  }

  /**
    * @param geoRegionDAO the geoRegionDAO to set
    */
   public void setGeoRegionDAO(GeoRegionDAO geoRegionDAO) {
     this.geoRegionDAO = geoRegionDAO;
   }
 
   /**
    * @param hostCountryKingdomBasisTagDAO the hostCountryKingdomBasisTagDAO to set
    */
   public void setHostCountryKingdomBasisTagDAO(QuadRelationTagDAO hostCountryKingdomBasisTagDAO) {
     this.hostCountryKingdomBasisTagDAO = hostCountryKingdomBasisTagDAO;
   }
 
   /**
    * @param hostCountryTagDAO the hostCountryTagDAO to set
    */
   public void setHostCountryTagDAO(BiRelationTagDAO hostCountryTagDAO) {
     this.hostCountryTagDAO = hostCountryTagDAO;
   }
 
   /**
    * @param keyValueDTOFactory the keyValueDTOFactory to set
    */
   public void setKeyValueDTOFactory(DTOFactory keyValueDTOFactory) {
     this.keyValueDTOFactory = keyValueDTOFactory;
   }
 
   /**
    * @param occurrenceRecordDAO the occurrenceRecordDAO to set
    */
   public void setOccurrenceRecordDAO(OccurrenceRecordDAO occurrenceRecordDAO) {
     this.occurrenceRecordDAO = occurrenceRecordDAO;
   }
 
   /**
    * @param unknownCountryId the unknownCountryId to set
    */
   public void setUnknownCountryId(Long unknownCountryId) {
     this.unknownCountryId = unknownCountryId;
   }
 
   /**
    * @param useCentiCellTable the useCentiCellTable to set
    */
   public void setUseCentiCellTable(boolean useCentiCellTable) {
     this.useCentiCellTable = useCentiCellTable;
   }
 
   public void synchronizeLists(List<CountDTO> georeferenced, List<CountDTO> nonGeoreferenced) {
     // Creating a clone list of non georeferenced records.
     List<CountDTO> nonGeoreferencedTemp = new ArrayList<CountDTO>();
     for (CountDTO count : nonGeoreferenced) {
       nonGeoreferencedTemp.add(count);
     }
     // Synchronizing...
     for (CountDTO count : georeferenced) {
       boolean exist = false;
       for (Iterator<CountDTO> iNon = nonGeoreferencedTemp.iterator(); !exist && iNon.hasNext();) {
         CountDTO nonCount = iNon.next();
         if (count.getKey().equals(nonCount.getKey())) {
           iNon.remove();
           exist = true;
         }
       }
       if (!exist) {
         CountDTO newCount = new CountDTO();
         newCount.setKey(count.getKey());
         newCount.setName(count.getName());
         newCount.setProperties(count.getProperties());
         newCount.setCount(0);
         nonGeoreferenced.add(newCount);
       }
     }
     for (CountDTO countTemp : nonGeoreferencedTemp) {
       CountDTO count = new CountDTO();
       count.setKey(countTemp.getKey());
       count.setName(countTemp.getName());
       count.setProperties(countTemp.getProperties());
       count.setCount(0);
       georeferenced.add(count);
     }
     nonGeoreferencedTemp.clear();
 
     // Both lists should be ordered.
     Collections.sort(georeferenced);
     Collections.sort(nonGeoreferenced);
   }
 }
