 /*
  * Copyright (c) 2012, Dienst Landelijk Gebied - Ministerie van Economische Zaken
  * 
  * Gepubliceerd onder de BSD 2-clause licentie, 
  * zie https://github.com/MinELenI/CBSviewer/blob/master/LICENSE.md voor de volledige licentie.
  */
 package nl.mineleni.cbsviewer.util;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * De enum StringConstants bevat alle string constanten voor de applicatie.
  * 
  * @author mprins
  * @since 1.5
  * @note bevat de constanten en default waarden voor de applicatie
  */
 public enum StringConstants {
 
 	/** request param naam voor straal. */
 	REQ_PARAM_STRAAL("straal"),
 	/** request param naam voor xcoord. */
 	REQ_PARAM_XCOORD("xcoord"),
 	/** request param naam voor ycoord. */
 	REQ_PARAM_YCOORD("ycoord"),
 	/** request param naam voor adres. */
 	REQ_PARAM_ADRES("adres"),
 	/** request param naam voor gevonden adres. */
 	REQ_PARAM_GEVONDEN("gevonden"),
 	/** request param naam voor pagina offset. */
 	REQ_PARAM_FID("fid"),
 	/** request param naam voor forward of html output. */
 	REQ_PARAM_FORWARD("forward"),
 	/** request param naam voor de beschrijvende naam van de kaart. */
 	REQ_PARAM_MAPNAME("mapname"),
 	/** request param naam voor de id van de kaart. */
 	REQ_PARAM_MAPID("mapid"),
 	/** request param naam voor het id van de achtergrondkaart. */
 	REQ_PARAM_BGMAP("achtergrond"),
 
 	/**
 	 * request param naam voor coreonly optie. De enige waarde waar naar wordt
 	 * gekeken is "true".
 	 */
 	REQ_PARAM_COREONLY("coreonly"),
 	/** request param naam voor bounding box. */
 	REQ_PARAM_BBOX("bbox"),
 	/** request param naam voor font size. */
 	REQ_PARAM_FONTSIZE("fsize"),
 	/** request param naam voor kleurenschema. */
 	REQ_PARAM_COLORSCHEME("cscheme"),
 	/** request param naam voor response format. */
 	REQ_PARAM_RESPONSE_FORMAT("format"),
 
 	/** parameter featureinfo. */
 	REQ_PARAM_FEATUREINFO("featureinfo"),
 	/** parameter legendas. */
 	REQ_PARAM_LEGENDAS("legendas"),
 	/** parameter kaart. */
 	REQ_PARAM_KAART("kaart"),
 	/** parameter cache dir. */
 	REQ_PARAM_CACHEDIR("dir"),
 	/** download link. */
 	REQ_PARAM_DOWNLOADLINK("downloadLink"),
 
 	/** openls request param naam voor request. */
 	OPENLS_REQ_PARAM_REQUEST("Request"),
 	/** openls request param naam voor request value. */
 	OPENLS_REQ_VALUE_GEOCODE("geocode"),
 	/** openls request param naam voor search. */
 	OPENLS_REQ_PARAM_SEARCH("zoekterm"),
 
 	/** directory in de webapp waar de wms resultaten worden gecached. */
 	MAP_CACHE_DIR("maps");
 
 	/** De code (waarde) van dit object. */
 	public final String code;
 
 	/**
 	 * Maakt een nieuw object aan met de gegeven code waarde.
 	 * 
 	 * @param code
 	 *            de code voor dit object
 	 * @see #code
 	 */
 	StringConstants(final String code) {
 		this.code = code;
 	}
 
 	/**
 	 * Lijst met alle code namen (immutable).
 	 * 
 	 * @return De lijst met alle code namen.
 	 */
 	public static List<String> codeNamen() {
 		final List<String> codeNames = new ArrayList<>();
 		for (final StringConstants strConst : StringConstants.values()) {
 			codeNames.add(strConst.code);
 		}
 		return Collections.unmodifiableList(codeNames);
 	}
 
 	/**
 	 * Geeft de code van dit object terug. Analoog van {@link #code}
 	 * 
 	 * @return de code
 	 * @see #code
 	 * @see java.lang.Enum#toString()
 	 */
 	@Override
 	public String toString() {
 		return this.code;
 	}
 }
