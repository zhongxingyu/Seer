 package br.gov.frameworkdemoiselle.spatial.geocode.google.impl;
 
 import java.math.BigDecimal;
 
 import br.gov.frameworkdemoiselle.spatial.geocode.Geocoding;
 import br.gov.frameworkdemoiselle.spatial.geocode.ReverseGeocoding;
 import br.gov.frameworkdemoiselle.spatial.geocode.model.GeocodingResponse;
 import br.gov.frameworkdemoiselle.spatial.geocode.model.GeocodingServiceReturnStatus;
 import br.gov.frameworkdemoiselle.spatial.geocode.model.Language;
 
 import com.google.code.geocoder.Geocoder;
 import com.google.code.geocoder.GeocoderRequestBuilder;
 import com.google.code.geocoder.model.LatLng;
 import com.google.code.geocoder.model.LatLngBounds;
 import com.vividsolutions.jts.geom.Envelope;
 import com.vividsolutions.jts.geom.Point;
 
public class GeocodingReverseGeocodingImpl implements Geocoding, ReverseGeocoding{
 
 	private final Geocoder geocoder = new Geocoder();
 	
 	private GeocoderRequestBuilder requestBuilder;
 	
 	private String fullAddress;
 	
 	private String street, number, cityDivision, city, state, country;
 	
 	private String postalcode;
 	
 	private LatLng location;
 	
 	public GeocodingReverseGeocodingImpl()
 	{
 		this.clearSearch();
 	}
 	
 	@Override
 	public GeocodingResponse search() {
 		
 		if(!this.verifyEmpty(this.fullAddress))
 		{
 			return new GeocoderJavaParserHelper().transform(this.geocoder.geocode(this.requestBuilder.setAddress(this.fullAddress).getGeocoderRequest()));
 		}
 		
 		else if(this.addressEmpty() && !this.verifyEmpty(this.postalcode))
 		{
 			return new GeocoderJavaParserHelper().transform(this.geocoder.geocode(this.requestBuilder.setAddress(this.postalcode).getGeocoderRequest()));
 		}
 		else if(!this.addressEmpty())
 		{
 			return new GeocoderJavaParserHelper().transform(this.geocoder.geocode(this.requestBuilder.setAddress(this.createAddressString()).getGeocoderRequest()));
 		}
 		else if(this.location != null)
 		{
 			return new GeocoderJavaParserHelper().transform(this.geocoder.geocode(this.requestBuilder.setLocation(location).getGeocoderRequest()));
 		}
 
 		GeocodingResponse response = new GeocodingResponse();
 		response.setReturnStatus(GeocodingServiceReturnStatus.CONFIG_ERROR);
 		
 		return response;
 	}
 	
 	@Override
 	public GeocodingResponse search(boolean clear) {
 		
 		GeocodingResponse response = this.search();
 		
 		this.clearSearch();
 		
 		return response;
 	}
 
 	@Override
 	public Geocoding setLanguage(Language language) {
 		
 		this.requestBuilder.setLanguage(language.value());
 		
 		return this;
 	}
 
 	@Override
 	public Geocoding setRegion(String region) {
 		
 		this.requestBuilder.setRegion(region);
 		
 		return this;
 	}
 
 	@Override
 	public Geocoding setBounds(Envelope boundary) {
 		
 		this.requestBuilder.setBounds(new LatLngBounds(new LatLng(""+boundary.getMinX(), ""+boundary.getMaxY()), new LatLng(""+boundary.getMaxX(), ""+boundary.getMinY())));
 		
 		return this;
 	}
 
 	@Override
 	public Geocoding setAddress(String address) {
 		
 		this.fullAddress = address;
 		
 		return this;
 	}
 
 	@Override
 	public Geocoding setAddress(String street, String number,
 			String cityDivision, String city, String state, String country) {
 		
 		this.street = street;
 		this.number = number;
 		this.cityDivision = cityDivision;
 		this.city = city;
 		this.state = state;
 		this.country = country;
 		
 		return this;
 	}
 
 	@Override
 	public Geocoding setPostalCode(String postalcode) {
 		
 		this.postalcode = postalcode;
 		
 		return this;
 	}
 	
 	@Override
 	public Geocoding setLocation(String lat, String lng) {
 		
 		this.location = new LatLng(lat, lng);
 		
 		return this;
 	}
 
 	@Override
 	public Geocoding setLocation(Point point) {
 		
 		this.location = new LatLng(BigDecimal.valueOf(point.getX()), BigDecimal.valueOf(point.getY()));
 		
 		return this;
 	}
 	
 
 	@Override
 	public void clearSearch() {
 		
 		this.requestBuilder = new GeocoderRequestBuilder();
 	
 		
 	}
 
 	/**
 	 * Verify if all address variables is empty
 	 * @return
 	 */
 	private boolean addressEmpty()
 	{
 		if(verifyEmpty(street) && verifyEmpty(number) && verifyEmpty(cityDivision) && verifyEmpty(city) && verifyEmpty(state) && verifyEmpty(country))
 		{
 			return true;
 		}
 		else
 			return false;
 	}
 	
 	/**
 	 * Verify if a string is empty
 	 * @param string
 	 * @return
 	 */
 	private boolean verifyEmpty(String string)
 	{
 		if(string!= null && !string.isEmpty())
 		{
 			return false;
 		}
 		else
 		return true;
 	}
 	
 	private String createAddressString()
 	{
 		StringBuffer sb = new StringBuffer();
 		
 		if(!verifyEmpty(this.street))
 		sb.append(this.street).append(" ");
 		
 		if(!verifyEmpty(this.number))
 		sb.append(this.number).append(" ");
 		
 		if(!verifyEmpty(this.cityDivision))
 		sb.append(this.cityDivision).append(" ");
 		
 		if(!verifyEmpty(this.city))
 		sb.append(this.city).append(" ");
 		
 		if(!verifyEmpty(this.state))
 		sb.append(this.state).append(" ");
 		
 		if(!verifyEmpty(this.country))
 		sb.append(this.country).append(" ");
 		
 		return sb.toString();
 		
 	}
 
 
 
 
 	
 	
 }
