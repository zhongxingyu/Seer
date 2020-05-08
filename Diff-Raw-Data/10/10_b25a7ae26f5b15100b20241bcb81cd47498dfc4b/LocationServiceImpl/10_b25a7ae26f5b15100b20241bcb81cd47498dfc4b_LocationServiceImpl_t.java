 package com.nedap.healthcare.kadasterbagclient.api.service;
 
 import javax.validation.constraints.NotNull;
 import javax.xml.ws.WebServiceException;
 
 import nl.kadaster.schemas.bag_verstrekkingen.bevragingen_apd.v20090901.AntwoordberichtAPDADO;
 import nl.kadaster.schemas.bag_verstrekkingen.bevragingen_apd.v20090901.ApplicatieException;
 import nl.kadaster.schemas.bag_verstrekkingen.bevragingen_apd.v20090901.IBagVsRaadplegenDatumADOV20090901;
 import nl.kadaster.schemas.bag_verstrekkingen.bevragingen_apd.v20090901.VraagberichtAPDADOAdres;
 import nl.kadaster.schemas.bag_verstrekkingen.bevragingen_selecties.v20090901.APD;
 import nl.kadaster.schemas.bag_verstrekkingen.bevragingen_selecties.v20090901.NUMPostcodeAdres;
 import nl.kadaster.schemas.imbag.apd.v20090901.Verblijfsobject;
 
 import org.hibernate.validator.constraints.NotBlank;
 import org.joda.time.DateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.nedap.healthcare.kadasterbagclient.api.dao.AddressDao;
 import com.nedap.healthcare.kadasterbagclient.api.exception.FaildCommunicationWithServer;
 import com.nedap.healthcare.kadasterbagclient.api.exception.UnExistingLocation;
 import com.nedap.healthcare.kadasterbagclient.api.model.Address;
 import com.nedap.healthcare.kadasterbagclient.api.model.AddressDTO;
 import com.nedap.healthcare.kadasterbagclient.api.util.BasselCoordinates;
 import com.nedap.healthcare.kadasterbagclient.api.util.CoordinatesConverterUtil;
 import com.nedap.healthcare.kadasterbagclient.api.util.RDCoordinates;
 
 /**
  * {@link LocationServiceHelper} implementation.
  * 
  * @author Dusko Vesin
  */
 @Service
 @Transactional
 class LocationServiceImpl implements LocationServiceHelper {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(LocationServiceImpl.class);
 
     /**
      * Max valid time for location beans fetched from configuration file.
      */
     private @Value("${max.validation.period.sec}")
     Integer maxValidPeriod;
 
     @Autowired
     private AddressDao locationDao;
 
     @Autowired
     private final IBagVsRaadplegenDatumADOV20090901 clientService = null;
 
     @Override
     public AddressDTO getAddress(@NotBlank final String zipCode, @NotBlank final Integer houseNumber)
             throws UnExistingLocation, FaildCommunicationWithServer, ApplicatieException {
 
         LOGGER.debug("Find location in Netherlands for zipCode:{} and houseNumber: {} ", zipCode, houseNumber);
         Address location = locationDao.findByCountryPostalCodeAndNumber(NL_COUNTRY_CODE, zipCode, houseNumber);
 
         if (location != null && isExpired(location)) {
             locationDao.delete(location);
			location = null;
         }
 
         if (location == null) {
             AntwoordberichtAPDADO result = null;
             try {
                 result = clientService
                         .zoekenAdresseerbaarObjectByPostcodeHuisnummerAndActueelOrPeildatum(wrapZipCodeAndHouseNumberToVraagberichtAPDADOAdres(
                                 zipCode, houseNumber));
             } catch (WebServiceException ex) {
                 LOGGER.error("Web Service communication error : " + ex.toString());
                 throw new FaildCommunicationWithServer();
             }
 
             if (result == null || result.getAntwoord().getProducten().getADOProduct().size() == 0) {
                 throw new UnExistingLocation(zipCode, houseNumber);
             }
             LOGGER.info("service result " + result);
             location = convertAndSave(result);
         }
 
         return convertToDto(location);
     }
 
     @Override
     public boolean isExpired(@NotNull final Address location) {
         final Long creationTime = location.getCreationDate().getMillis();
         final Long currentTime = new DateTime().getMillis();
 
         final Long difference = currentTime - creationTime;
         return difference > maxValidPeriod * 1000;
     }
 
     @Override
     public AddressDTO convertToDto(@NotNull final Address location) {
         final AddressDTO locationDTO = new AddressDTO();
 
         locationDTO.setCountryCode(location.getCountryCode());
         locationDTO.setLatitude(location.getLatitude());
         locationDTO.setLongitude(location.getLongitude());
         locationDTO.setNumber(location.getNumber());
         locationDTO.setNumberPostfix(location.getNumberPostfix());
         locationDTO.setPostalCode(location.getPostalCode());
         locationDTO.setValidFrom(location.getValidFrom());
         locationDTO.setValidTo(location.getValidTo());
         locationDTO.setCity(location.getCity());
         locationDTO.setStreet(location.getStreet());
 
         return locationDTO;
     }
 
     @Override
     public Address convertAndSave(@NotNull final AntwoordberichtAPDADO kadasterLocation) {
 
         Address location = new Address();
         Verblijfsobject object = kadasterLocation.getAntwoord().getProducten().getADOProduct().get(0)
                 .getVerblijfsobject();// TODO ? more than one result
         RDCoordinates rdc = new RDCoordinates(object.getVerblijfsobjectGeometrie().getPoint().getPos().getValue()
                 .get(0), object.getVerblijfsobjectGeometrie().getPoint().getPos().getValue().get(1));
         BasselCoordinates bassel = CoordinatesConverterUtil.transformRijksdriehoeksmetingToBassel(rdc);
 
         location.setCountryCode(LocationService.NL_COUNTRY_CODE);
         location.setCreationDate(new DateTime());
 
         location.setLatitude(bassel.getA().toString());
         location.setLongitude(bassel.getF().toString());
 
         location.setValidFrom(object.getGerelateerdeAdressen().getHoofdadres().getTijdvakgeldigheid()
                 .getBegindatumTijdvakGeldigheid());
         location.setValidTo(object.getGerelateerdeAdressen().getHoofdadres().getTijdvakgeldigheid()
                 .getEinddatumTijdvakGeldigheid());
         location.setNumber(object.getGerelateerdeAdressen().getHoofdadres().getHuisnummer());
         location.setNumberPostfix(object.getGerelateerdeAdressen().getHoofdadres().getHuisnummertoevoeging());
         location.setPostalCode(object.getGerelateerdeAdressen().getHoofdadres().getPostcode());
         location.setCity(object.getGerelateerdeAdressen().getHoofdadres().getGerelateerdeWoonplaats()
                 .getWoonplaatsNaam());
         location.setStreet(object.getGerelateerdeAdressen().getHoofdadres().getGerelateerdeOpenbareRuimte()
                 .getOpenbareRuimteNaam());
 
         locationDao.save(location);
 
         return location;
     }
 
     /**
      * Function wraps passed parameters into {@link VraagberichtAPDADOAdres} object.
      * 
      * @param zipCode
      * @param houseNumber
      * @return
      */
     private VraagberichtAPDADOAdres wrapZipCodeAndHouseNumberToVraagberichtAPDADOAdres(final String zipCode,
             final Integer houseNumber) {
         APD apd = new APD();
         apd.setGegVarActueel(true);
 
         NUMPostcodeAdres numPostcodeAdres = new NUMPostcodeAdres();
         numPostcodeAdres.setHuisnummer(houseNumber);
         numPostcodeAdres.setPostcode(zipCode);
 
         VraagberichtAPDADOAdres.Vraag vraag = new VraagberichtAPDADOAdres.Vraag();
         vraag.setNUMPostcodeAdres(numPostcodeAdres);
         vraag.setAPD(apd);
 
         VraagberichtAPDADOAdres result = new VraagberichtAPDADOAdres();
         result.setVraag(vraag);
 
         return result;
     }
 
     @Override
     public Integer getMaxValidPeriod() {
         return maxValidPeriod;
     }
 
     @Override
     public IBagVsRaadplegenDatumADOV20090901 getWSClient() {
         return clientService;
     }
 }
