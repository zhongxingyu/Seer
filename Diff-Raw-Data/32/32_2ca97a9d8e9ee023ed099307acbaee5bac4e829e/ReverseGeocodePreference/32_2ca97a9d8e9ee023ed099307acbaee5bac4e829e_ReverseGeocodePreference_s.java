 /*
  * Copyright (c) 2013, Dienst Landelijk Gebied - Ministerie van Economische Zaken
  * 
  * Gepubliceerd onder de BSD 2-clause licentie, 
  * zie https://github.com/MinELenI/CBSviewer/blob/master/LICENSE.md voor de volledige licentie.
  */
 package nl.mineleni.openls.databinding.openls;
 
 import nl.mineleni.openls.XmlNamespaceConstants;
 
 /**
  * OLS ReverseGeocodePreference.
  * http://schemas.opengis.net/ols/1.2.0/LocationUtilityService.xsd
  * 
  * <pre>
  * &lt;simpleType name="ReverseGeocodePreferenceType">
  * &lt;annotation>
  * 	&lt;documentation>Defines a Preference for Reverse Geocode response.&lt;/documentation>
  * &lt;/annotation>
  * &lt;restriction base="string">
  * &lt;enumeration value="StreetAddress"/>
  * &lt;enumeration value="IntersectionAddress"/>
  * &lt;/restriction>
  * &lt;/simpleType>
  * &lt;element name="ReverseGeocodePreference" type="xls:ReverseGeocodePreferenceType">
  * &lt;annotation>
  * 	&lt;documentation>Describes the preference for what the Reverse Geocoder service should return: 
  * 	StreetAddress, IntersectionAddress, or PositionOfInterest (Place and/or PostalCode). 
  * 	If not specified, then the service will return the nearest StreetAddress. .
  * 	&lt;/documentation>
  * &lt;/annotation>
  * &lt;/element>
  * </pre>
  * 
  * @author prinsmc
  * 
  */
 public class ReverseGeocodePreference implements XmlNamespaceConstants {
 
 	/**
 	 * serialization id.
 	 */
 	private static final long serialVersionUID = -7570918027344077813L;
 
 	private String preference;
 
 	public enum PREFERENCE {
 		StreetAddress("StreetAddress"), IntersectionAddress(
 				"IntersectionAddress"), PositionOfInterest("PositionOfInterest");
 		public final String preference;
 
 		PREFERENCE(final String preference) {
 			this.preference = preference;
 		}
 
 		@Override
 		public String toString() {
 			return this.preference;
 		}
 	}
 
 	/**
 	 * @return the preference
 	 */
 	public String getPreference() {
 		return preference;
 	}
 
 	/**
 	 * set gewenste preference.
 	 * 
	 * @param pref
 	 */
 	public void setPreference(String preference) {
 		this.preference = preference;
 	}
 
 	@Override
 	public String toXML() {
 		if (this.preference != null) {
 			final StringBuilder sb = new StringBuilder("<"
 					+ XmlNamespaceConstants.OPENLS_NAMESPACE_PREFIX
 					+ ":ReverseGeocodePreference>");
 			sb.append(this.preference);
 			sb.append("</" + XmlNamespaceConstants.OPENLS_NAMESPACE_PREFIX
 					+ ":ReverseGeocodePreference>");
 			return sb.toString();
 		}
 		return "";
 	}
 
 }
