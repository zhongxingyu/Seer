 package nl.mineleni.cbsviewer.servlet.gazetteer.lusclient;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import nl.mineleni.openls.databinding.openls.Address;
 import nl.mineleni.openls.databinding.openls.GeocodeResponse;
 import nl.mineleni.openls.databinding.openls.GeocodeResponseList;
 import nl.mineleni.openls.databinding.openls.GeocodedAddress;
 
 /**
  * Utility klasse OpenLSClientUtil.
  * 
  * @author mprins
  */
 public final class OpenLSClientUtil {
 
 	/** De constante PLACE_TYPE_COUNTRYSUBDIVISION. {@value} */
 	public final static String PLACE_TYPE_COUNTRYSUBDIVISION = "CountrySubdivision";
 
 	/** De constante PLACE_TYPE_MUNICIPALITY. {@value} */
 	public static final String PLACE_TYPE_MUNICIPALITY = "Municipality";
 
 	/** De constante PLACE_TYPE_MUNICIPALITYSUBDIVISION. {@value} */
 	public static final String PLACE_TYPE_MUNICIPALITYSUBDIVISION = "MunicipalitySubdivision";
 
 	/**
 	 * private constructor voor deze utility klasse.
 	 */
 	private OpenLSClientUtil() {
 		/* private constructor voor deze utility klasse. */
 	}
 
 	/**
 	 * Gets the geocoded address list.
 	 * 
 	 * @param gcr
 	 *            the gcr
 	 * @return the geocoded address list
 	 */
 	public static List<GeocodedAddress> getGeocodedAddressList(
 			final GeocodeResponse gcr) {
 		final List<GeocodedAddress> addressList = new ArrayList<GeocodedAddress>();
 		for (int i = 0; i < gcr.getGeocodeResponseListSize(); i++) {
 			final GeocodeResponseList gcrl = gcr.getGeocodeResponseListAt(i);
 			for (int j = 0; j < gcrl.getGeocodedAddressSize(); j++) {
 				final GeocodedAddress gca = gcrl.getGeocodedAddressAt(j);
 				addressList.add(gca);
 			}
 		}
 		return addressList;
 	}
 
 	/**
 	 * Gets the client address list uit een GeocodeResponse.
 	 * 
 	 * @param gcr
 	 *            de GeocodeResponse
 	 * @param max
 	 *            het maximum aantal adressen om terug te geven
 	 * @return the open ls client address list
 	 */
 	public static List<OpenLSClientAddress> getOpenLSClientAddressList(
 			final GeocodeResponse gcr, int... max) {
 		final List<OpenLSClientAddress> addressList = new ArrayList<OpenLSClientAddress>();
 		final List<GeocodedAddress> gcal = getGeocodedAddressList(gcr);
 
 		int listSize = gcal.size();
 		if (max != null && max.length > 0) {
			listSize = (max[0] > listSize) ? listSize : max[0];
 		}
 
 		for (int i = 0; i < listSize; i++) {
 			final GeocodedAddress gca = gcal.get(i);
 			final OpenLSClientAddress addr = new OpenLSClientAddress();
 
 			if (!gca.hasPoint() || (gca.getPoint().getPosSize() == 0)) {
 				continue;
 			}
 
 			if (gca.getPoint().getSrsName().equalsIgnoreCase("EPSG:28992")) {
 				// afronden naar meters in het geval van Rijksdriekhoek
 				// WORKAROUND voor bug in PDOK gazetteer service die belachelijk
 				// hoge nauwkeurigheid hanteerd voor centroiden
 				addr.setxCoord((gca.getPoint().getPosAt(0).getX()).intValue()
 						+ "");
 				addr.setyCoord((gca.getPoint().getPosAt(0).getY()).intValue()
 						+ "");
 			} else {
 				addr.setxCoord((gca.getPoint().getPosAt(0).getX()).toString());
 				addr.setyCoord((gca.getPoint().getPosAt(0).getY()).toString());
 			}
 
 			if (gca.getAddress() != null) {
 				final Address adr = gca.getAddress();
 				if (adr.hasStreetAddress()
 						&& adr.getStreetAddress().hasStreet()) {
 					addr.setStreetName(adr.getStreetAddress().getStreet()
 							.getStreet());
 				}
 				if (adr.hasStreetAddress()
 						&& adr.getStreetAddress().hasBuilding()
 						&& adr.getStreetAddress().getBuilding().hasNumber()) {
 					addr.setStreetNumber(adr.getStreetAddress().getBuilding()
 							.getNumber());
 				}
 				if (adr.hasPostalCode() && adr.getPostalCode().hasPostalCode()) {
 					addr.setPostalCode(adr.getPostalCode().getPostalCode());
 				}
 				if (adr.getPlaceByType(PLACE_TYPE_COUNTRYSUBDIVISION) != null) {
 					addr.setCountrySubdivision(adr
 							.getPlaceByType(PLACE_TYPE_COUNTRYSUBDIVISION));
 				}
 				if (adr.getPlaceByType(PLACE_TYPE_MUNICIPALITY) != null) {
 					addr.setMunicipality(adr
 							.getPlaceByType(PLACE_TYPE_MUNICIPALITY));
 				}
 				if (adr.getPlaceByType(PLACE_TYPE_MUNICIPALITYSUBDIVISION) != null) {
 					addr.setMunicipalitySubdivision(adr
 							.getPlaceByType(PLACE_TYPE_MUNICIPALITYSUBDIVISION));
 				}
 			}
 			if (addr.isValidClientAddress()) {
 				addressList.add(addr);
 			}
 		}
 		return addressList;
 	}
 }
