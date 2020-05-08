 package es.weso.data.impl;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 
 import es.weso.model.Country;
 import es.weso.model.CountryGroup;
 import es.weso.model.Indicator;
 import es.weso.model.NamedUri;
 import es.weso.model.Observation;
 import es.weso.model.ValuedNamedUri;
 import es.weso.util.Conf;
 import es.weso.util.JenaMemcachedClient;
 
 /**
  * Abstract class with utility methods to get data from the
  * {@link JenaMemcachedClient}
  * 
  * @author Alejandro Montes García
  * @since 04/07/2013
  * @version 1.0
  */
 public abstract class AbstractDataManager {
 
 	protected JenaMemcachedClient client;
 	
 	public String toISO3Code(String ISO2Code) {
 		return getString(client.executeQuery(Conf.getQuery("isos", ISO2Code))
 				.next(), "code3");
 	}
 
 	protected Country querySolutionToCountry(QuerySolution qs) {
 		Country country = new Country();
 		country.setUri(getURI(qs, "country"));
 		country.setBelongsTo(Collections
 				.singleton(querySolutionToNamedUriRegion(qs)));
 		country.setIsoCode2(getString(qs, "code2"));
 		country.setIsoCode3(getString(qs, "code3"));
 		country.setName(getString(qs, "countryLabel"));
 		country.setLat(getDouble(qs, "lat"));
 		country.setLon(getDouble(qs, "long"));
 		return country;
 	}
 
 	protected Observation querySolutionToObservation(QuerySolution qs) {
 		Observation obs = new Observation();
 		obs.setUri(getURI(qs, "obs"));
 		obs.setCountryCode(getString(qs, "countryCode"));
 		obs.setCountryUri(getURI(qs, "country"));
 		obs.setCountryName(getString(qs, "countryLabel"));
 		obs.setIndicator(querySolutionToNamedUriIndicator(qs));
 		obs.setName(getString(qs, "label"));
 		obs.setValue(getDouble(qs, "value"));
 		obs.setYear(getInt(qs, "year"));
 		return obs;
 	}
 
 	protected Country resultSetToCountry(ResultSet rs) {
 		Country country = new Country();
 		QuerySolution qs = rs.next();
 		country.setUri(getURI(qs, "country"));
 		country.setBelongsTo(Collections
 				.singleton(querySolutionToNamedUriRegion(qs)));
 		country.setIsoCode2(getString(qs, "code2"));
 		country.setIsoCode3(getString(qs, "code3"));
 		country.setName(getString(qs, "countryLabel"));
 		country.setLat(getDouble(qs, "lat"));
 		country.setLon(getDouble(qs, "long"));
 		Collection<ValuedNamedUri> observations = new HashSet<ValuedNamedUri>();
 		observations.add(querySolutionToValuedNamedUri(qs));
 		while (rs.hasNext()) {
 			qs = rs.next();
 			observations.add(querySolutionToValuedNamedUri(qs));
 		}
 		country.setObservations(observations);
 		return country;
 	}
 	
 	protected CountryGroup resultSetToCountryGroup(ResultSet rs) {
 		CountryGroup cg = new CountryGroup();
 		QuerySolution qs = rs.next();
 		cg.setContinent(getURI(qs, "type").endsWith("Region"));
 		cg.setUri(getURI(qs, "region"));
 		cg.setName(getString(qs, "regionLabel"));
 		Collection<NamedUri> countries = new HashSet<NamedUri>();
 		countries.add(querySolutionToNamedUriCountry(qs));
 		while (rs.hasNext()) {
 			qs = rs.next();
 			countries.add(querySolutionToNamedUriCountry(qs));
 		}
 		cg.setCountries(countries);
 		return cg;
 	}
 
 	protected Collection<CountryGroup> resultSetToContinentCollection(
 			ResultSet rs) {
 		List<CountryGroup> groups = new LinkedList<CountryGroup>();
 		while (rs.hasNext()) {
 			QuerySolution qs = rs.next();
 			CountryGroup continent = querySolutionToContinent(qs);
 			if (groups.contains(continent)) {
 				groups.get(groups.indexOf(continent)).getCountries()
 						.addAll(continent.getCountries());
 			} else {
 				groups.add(continent);
 			}
 		}
 		return groups;
 	}
 
 	protected CountryGroup querySolutionToContinent(QuerySolution qs) {
 		CountryGroup g = new CountryGroup();
 		HashSet<NamedUri> s = new HashSet<NamedUri>();
 		s.add(querySolutionToNamedUriCountry(qs));
 		g.setContinent(true);
 		g.setName(getString(qs, "regionLabel"));
 		g.setUri(getURI(qs, "region"));
 		g.setCountries(s);
 		return g;
 	}
 	
 	protected Indicator querySolutionToIndicator(QuerySolution qs) {
 		Indicator indicator = new Indicator();
 		indicator.setComment(getString(qs, "comment"));
 		indicator.setEnd(getInt(qs, "end"));
 		indicator.setStart(getInt(qs, "start"));
 		indicator.setHigherBetter(getURI(qs, "high").endsWith("igh"));
 		indicator.setUri(getURI(qs, "indicator"));
		indicator.setLabel(getString(qs, "indicatorLabel"));
 		return indicator;
 	}
 
 	private ValuedNamedUri querySolutionToValuedNamedUri(QuerySolution qs) {
 		ValuedNamedUri vnu = new ValuedNamedUri();
 		vnu.setName(getString(qs, "obsLabel"));
 		vnu.setUri(getURI(qs, "obs"));
 		vnu.setValue(getDouble(qs, "value"));
 		return vnu;
 	}
 
 	private NamedUri querySolutionToNamedUriRegion(QuerySolution qs) {
 		return querySolutionToNamedUri(qs, "region");
 	}
 
 	private NamedUri querySolutionToNamedUriCountry(QuerySolution qs) {
 		return querySolutionToNamedUri(qs, "country");
 	}
 
 	private NamedUri querySolutionToNamedUriIndicator(QuerySolution qs) {
 		return querySolutionToNamedUri(qs, "indicator");
 	}
 
 	private NamedUri querySolutionToNamedUri(QuerySolution qs, String type) {
 		NamedUri nu = new NamedUri();
 		nu.setUri(getURI(qs, type));
 		nu.setName(getString(qs, type + "Label"));
 		return nu;
 	}
 
 	private String getURI(QuerySolution qs, String resource) {
 		try {
 			return qs.getResource(resource).getURI();
 		} catch (NullPointerException npe) {
 			return "";
 		}
 	}
 
 	private String getString(QuerySolution qs, String literal) {
 		try {
 			return qs.getLiteral(literal).getString();
 		} catch (NullPointerException npe) {
 			return "";
 		}
 	}
 
 	private Double getDouble(QuerySolution qs, String literal) {
 		try {
 			return qs.getLiteral(literal).getDouble();
 		} catch (NullPointerException npe) {
 			return 0.0;
 		}
 	}
 
 	private Integer getInt(QuerySolution qs, String literal) {
 		try {
 			return qs.getLiteral(literal).getInt();
 		} catch (NullPointerException npe) {
 			return 0;
 		}
 	}
 
 }
