 package org.ocha.hdx.model;
 
 import java.util.Arrays;
 import java.util.List;
 
 public class DataSerie implements Comparable<DataSerie> {
 
 	// List of indicators for country overview
 	public static final String[] COUNTRY_OVERVIEW_indicatorsList = new String[] { "CG020", "CG030", "CG060", "CG070", "CG080", "CG100", "CG120", "CG140", "CG150", "CG290", "CG310", "CG320" };
 
 	// List of indicators for country RW overview
 	public static final String[] COUNTRY_OVERVIEW_RW_indicatorsList = new String[] { "RW001", "RW002" };
 
 	// List of indicators for country FTS overview
 	public static final String[] COUNTRY_OVERVIEW_FTS_indicatorsList = new String[] { "FA010", "FA140", "FY010", "FY020", "FY040", "FY190", "FY200", "FY210", "FY240", "FY360", "FY370", "FY380",
 			"FY500", "FY510", "FY520", "FY540", "FY550", "FY620", "FY630" };
 
 	// List of indicators relevant for country - crisis history
 	public static final List<DataSerie> COUNTRY_CRISIS_HISTORY_dataSeries = Arrays.asList(new DataSerie("CH070", "emdat"), new DataSerie("CH080", "emdat"), new DataSerie("CH090", "emdat"),
 			new DataSerie("CH100", "emdat"));
 
 	// List of indicators relevant for country - socio-economic
	public static final List<DataSerie> COUNTRY_SOCIO_ECONOMIC_dataSeries = Arrays.asList(new DataSerie("PCX140", "world-bank"), new DataSerie("PSE030", "world-bank"), new DataSerie("PSE090",
			"world-bank"), new DataSerie("PSE110", "data.undp.org"), new DataSerie("PSE120", "world-bank"), new DataSerie("PSE130", "world-bank"), new DataSerie("PSE140", "world-bank"),
			new DataSerie("PSE150", "world-bank"), new DataSerie("PSE160", "data.undp.org"), new DataSerie("PSE170", "world-bank"), new DataSerie("PSE200", "world-bank"), new DataSerie("PSE210",
					"world-bank"), new DataSerie("PSE220", "data.undp.org"), new DataSerie("PSE240", "data.undp.org"), new DataSerie("PSP010", "esa-unpd-wpp2012"), new DataSerie("PSP060",
					"world-bank"), new DataSerie("PSP070", "esa-unpd-wpp2012"), new DataSerie("PSP080", "esa-unpd-wpp2012"), new DataSerie("PSP090", "world-bank"), new DataSerie("PSP100",
					"esa-unpd-wup2011"), new DataSerie("PSP110", "world-bank"), new DataSerie("PVE010", "data.undp.org"), new DataSerie("PVE030", "data.undp.org"), new DataSerie("PVE040",
					"data.undp.org"), new DataSerie("PVE110", "data.undp.org"), new DataSerie("PVE120", "data.undp.org"));
 
 	// List of indicators relevant for country - vulnerability
 	public static final List<DataSerie> COUNTRY_VULNERABILITY_dataSeries = Arrays.asList(new DataSerie("PVE130", "mdgs"), new DataSerie("PVF020", "faostat3"), new DataSerie("PVF040", "wfp-vam"),
 			new DataSerie("PVF050", "wfp-vam"), new DataSerie("PVH140", "mdgs"), new DataSerie("PVN010", "fao-foodsec"), new DataSerie("PVN050", "mdgs"), new DataSerie("PVW010", "mdgs"),
 			new DataSerie("PVW040", "mdgs"), new DataSerie("PVX010", "echo"), new DataSerie("PVX020", "echo"), new DataSerie("PVX040", "acled"));
 
 	// List of indicators relevant for country - capacity
 	public static final List<DataSerie> COUNTRY_CAPACITY_dataSeries = Arrays.asList(new DataSerie("PCX051", "mdgs"), new DataSerie("PCX080", "mdgs"), new DataSerie("PCX090", "world-bank"),
 			new DataSerie("PCX100", "world-bank"), new DataSerie("PCX130", "world-bank"), new DataSerie("PVL010", "worldaerodata"), new DataSerie("PVL030", "world-bank"), new DataSerie("PVL040",
 					"world-bank"));
 
 	// List of indicators relevant for country - other
 	public static final List<DataSerie> COUNTRY_OTHER_dataSeries = Arrays.asList(new DataSerie("CG300", "world-bank"), new DataSerie("PCH090", "unicef-infobycountry"),
 			new DataSerie("PCH100", "mdgs"), new DataSerie("PCH110", "world-bank"), new DataSerie("PSE230", "world-bank"), new DataSerie("PSE250", "world-bank"),
 			new DataSerie("PSP120", "world-bank"), new DataSerie("PVH080", "world-bank"), new DataSerie("PVH090", "world-bank"), new DataSerie("PVN060", "mdgs"), new DataSerie("PVN070", "mdgs"),
 			new DataSerie("PVX080", "emdat"), new DataSerie("PVX090", "emdat"), new DataSerie("PVX100", "emdat"), new DataSerie("TT008", "who-gho"), new DataSerie("TT009", "who-gho"), new DataSerie(
 					"TT010", "who-gho"), new DataSerie("TT011", "who-gho"), new DataSerie("TT013", "who-gho"), new DataSerie("TT013R", "who-gho"), new DataSerie("TT013U", "who-gho"), new DataSerie(
 					"TT014", "mdgs"), new DataSerie("TT015", "who-gho"), new DataSerie("TT016", "who-gho"), new DataSerie("TT017", "who-gho"), new DataSerie("TT018", "who-gho"), new DataSerie(
 					"TT019", "who-gho"), new DataSerie("TT020", "who-gho"), new DataSerie("TT024", "who-gho"), new DataSerie("TT024F", "who-gho"), new DataSerie("TT024M", "who-gho"), new DataSerie(
 					"TT025", "who-gho"), new DataSerie("TT025F", "who-gho"), new DataSerie("TT025M", "who-gho"), new DataSerie("TT026", "who-gho"), new DataSerie("TT026F", "who-gho"), new DataSerie(
 					"TT026M", "who-gho"), new DataSerie("TT027", "desa-pop-mig"), new DataSerie("TT027T", "world-bank"), new DataSerie("TT029", "unodc"), new DataSerie("TT030", "world-bank"),
 			new DataSerie("TT031R2", "who-gho"), new DataSerie("TT031R3", "who-gho"), new DataSerie("TT031R5", "who-gho"), new DataSerie("TT031U2", "who-gho"), new DataSerie("TT031U3", "who-gho"),
 			new DataSerie("TT031U5", "who-gho"), new DataSerie("TT032", "who-gho"), new DataSerie("TT033R2", "who-gho"), new DataSerie("TT033R3", "who-gho"), new DataSerie("TT033R5", "who-gho"),
 			new DataSerie("TT033U2", "who-gho"), new DataSerie("TT033U3", "who-gho"), new DataSerie("TT033U5", "who-gho"), new DataSerie("TT034", "who-gho"), new DataSerie("TT035", "who-gho"),
 			new DataSerie("TT036", "world-bank"), new DataSerie("TT037R3", "who-gho"), new DataSerie("TT037R5", "who-gho"), new DataSerie("TT037U3", "who-gho"), new DataSerie("TT037U5", "who-gho"));
 
 	// List of indicators relevant for country - 5 years
 	public static final List<DataSerie> COUNTRY_5_YEARS_dataSeries = Arrays.asList(new DataSerie("PSP050", "esa-unpd-wpp2012"), new DataSerie("PVH010", "esa-unpd-wpp2012"), new DataSerie("PVH050",
 			"esa-unpd-wpp2012"), new DataSerie("PVH100", "esa-unpd-wpp2012"), new DataSerie("PVH120", "data.undp.org"), new DataSerie("PVH150", "esa-unpd-wpp2012"), new DataSerie("PVH180",
 			"data.undp.org"), new DataSerie("PVH190", "mdgs"), new DataSerie("PVH200", "esa-unpd-wpp2012"));
 
 	// List of indicators relevant for country - RW
 	public static final List<DataSerie> COUNTRY_RW_dataSeries = Arrays.asList(new DataSerie("RW002", "RW"), new DataSerie("RW001", "RW"));
 
 	// List of indicators relevant for country - FTS
 	public static final List<DataSerie> COUNTRY_FTS_dataSeries = Arrays.asList(new DataSerie("FY010", "fts"), new DataSerie("FY020", "fts"), new DataSerie("FY040", "fts"), new DataSerie("FY190",
 			"fts"), new DataSerie("FY200", "fts"), new DataSerie("FY210", "fts"), new DataSerie("FY240", "fts"), new DataSerie("FY360", "fts"), new DataSerie("FY370", "fts"), new DataSerie("FY380",
 			"fts"), new DataSerie("FY500", "fts"), new DataSerie("FY510", "fts"), new DataSerie("FY520", "fts"), new DataSerie("FY540", "fts"), new DataSerie("FY550", "fts"), new DataSerie("FY620",
 			"fts"), new DataSerie("FY630", "fts"), new DataSerie("FA010", "fts"), new DataSerie("FA140", "fts"));
 
 	private final String indicatorCode;
 	private final String sourceCode;
 
 	public DataSerie(final String indicatorCode, final String sourceCode) {
 		super();
 		this.indicatorCode = indicatorCode;
 		this.sourceCode = sourceCode;
 	}
 
 	public String getIndicatorCode() {
 		return indicatorCode;
 	}
 
 	public String getSourceCode() {
 		return sourceCode;
 	}
 
 	@Override
 	public int compareTo(final DataSerie o) {
 		final int indicatorTypeComparison = indicatorCode.compareTo(o.indicatorCode);
 		if (indicatorTypeComparison != 0) {
 			return indicatorTypeComparison;
 		} else {
 			return sourceCode.compareTo(o.sourceCode);
 		}
 
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = (prime * result) + ((indicatorCode == null) ? 0 : indicatorCode.hashCode());
 		result = (prime * result) + ((sourceCode == null) ? 0 : sourceCode.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(final Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		final DataSerie other = (DataSerie) obj;
 		if (indicatorCode == null) {
 			if (other.indicatorCode != null) {
 				return false;
 			}
 		} else if (!indicatorCode.equals(other.indicatorCode)) {
 			return false;
 		}
 		if (sourceCode == null) {
 			if (other.sourceCode != null) {
 				return false;
 			}
 		} else if (!sourceCode.equals(other.sourceCode)) {
 			return false;
 		}
 		return true;
 	}
 }
