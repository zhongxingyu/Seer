 package eionet.eunis.util;
 
 public interface Constants {
 		/** */
 	   //Warning messages
 	   public static final int SEVERITY_INFO = 1;
 	   public static final int SEVERITY_CAUTION = 2;
 	   public static final int SEVERITY_WARNING = 3;
 	   public static final int SEVERITY_ERROR = 4;
 	   
 	   //Link info constants
 	   public static final String GEOSPECIES_IDENTIFIER = "GEOSPECIES_IDENTIFIER";
 	   public static final String GBIF_PAGE = "hasGBIFPage";
 	   public static final String BIOLIB_PAGE = "hasBioLibPage";
 	   public static final String BBC_PAGE = "hasBBCPage";
 	   public static final String WIKIPEDIA_ARTICLE = "hasWikipediaArticle";
 	   public static final String WIKISPECIES_ARTICLE = "hasWikispeciesArticle";
 	   public static final String BUG_GUIDE = "hasBugGuidePage";

	   //Same Species
	   public static final String SAME_SPECIES_EOL = "sameSpeciesEOL"; // www.eol.org
	   public static final String SAME_SPECIES_GBIF = "sameSpeciesGBIF"; // www.gbif.org
	   public static final String ITIS = "sameSpeciesITIS"; // www.itis.gov
	   public static final String NCBI = "sameSpeciesNCBI"; // www.ncbi.nlm.nih.gov

	   //Same Synonym
	   public static final String SAME_SYNONYM_EOL = "sameSynonymEOL";
	   public static final String SAME_SYNONYM_GBIF = "sameSynonymGBIF";
	   public static final String SAME_SYNONYM_ITIS = "sameSynonymITIS";
	   public static final String SAME_SYNONYM_NCBI = "sameSynonymNCBI";
 }
