 package za.co.paulscott.musicgraph.entities.artist;
 
 import za.co.paulscott.musicgraph.enums.ArtistAliasType;
import za.co.paulscott.musicgraph.enums.Locale;
 
 /**
  * Localised names
  * 
  * The locale should be used to capture the official name as normally used in a particular language and/or country.
  * An artist can only have one localised name for each locale. 
  * If there is more than one, use the most recent name for an artist. 
  * If multiple names are used in an interchangeable manner, use the most common one. 
  * Other names can still be added as aliases without a locale.
  * 
  * The locale should not be more specific than it needs to be, so the country should only be included when necessary, 
  * e.g. If an artist name is the same in all English speaking countries, "English" with no country is sufficient. 
  * If the name is normally the same, but is different (usually for legal reasons) in certain countries, 
  * then the country should only be used to identify where the name is different from the usual name for that language.
  * 
  * Examples
  * Пётр Ильич Чайковский is known as "Pyotr Ilyich Tchaikovsky" in English.
  * The Beatles are known as "ザ・ビートルズ" in Japanese.
  * Yazoo are normally known as "Yazoo" in English, but are known as "Yaz" in English in the United States. 
  * 
  * When to use aliases
  * 
  * There are many cases where using aliases is appropriate:
  * 
  * Misspellings:
  * These are the most common, and function as a simplistic automatic spelling corrector
  * e.g. Led Zepplin = Led Zeppelin
  * 
  * Variants:
  * An artist may use several similar names interchangeably without making a distinction, from release to release
  * e.g. Hootie and the Blowfish = Hootie & the Blowfish; Monty Python's Flying Circus = Monty Python
  * 
  * Numbers:
  * Even if the artist prefers spelled out or numerical form, they may or may not be spelled out by users
  * e.g. The 3 Tenors = The Three Tenors
  * 
  * Stylized Names:
  * Many artists feel a need to spell their names with strange spacing, odd characters and punctuation, etc.
  * e.g. NSync = 'N Sync
  * 
  * Missing Titles:
  * Artists add or drop titles, monikers and/or articles from their names
  * e.g. The Sex Pistols = Sex Pistols; Tiësto = DJ Tiësto; Cheb Khaled = Khaled
  * 
  * Acronyms:
  * Artists with long and unwieldy names are often better known by their acronyms, and may use them on their release covers
  * e.g. B.D.P. = BDP = Boogie Down Productions
  * 
  * Initials:
  * Overlaps somewhat with acronyms, but there are sometimes middle initials not generally used in the artist's name
  * e.g. J.S. Bach = Johann Sebastian Bach
  * 
  * Incomplete Names:
  * When credited as "Strauss", it's usually Johan Strauss II and not Richard or one of the others that is intended
  * e.g. Strauss = Johann Strauss II
  * 
  * Lead Performers:
  * Sting is a member of The Police - it is not a collaboration, and the band does not officially include his name in theirs, 
  * however compilations often list featured members explicitly by name in this way
  * e.g. Sting & The Police = The Police
  * 
  * Misencodings:
  * Artist names entered in FreeDB using non-UTF-8 encodings; these are somewhat like typos, but in non-Unicode locales, 
  * these may in fact be more accurate than an automatic conversion from UTF-8
  * e.g. ©PªN­Û­ = Jay Chou
  * 
  * Localization:
  * While English-speakers are used to "Tchaikovsky", he is known elsewhere in the world by different spellings
  * e.g. Piotr Ilitch Tchaïkovski = Pyotr Ilyich Tchaikovsky
  * 
  * Transliterations:
  * There are often several ways to transliterate non-Roman characters according to different standards
  * e.g. Jay Chow = Jay Chou
  * 
  * "Translated" Names:
  * Many Asian artists have "English" names in addition to their given "Chinese" or "Japanese" etc. names - 
  * in some cases, the artists prefer the English name even in non-English text
  * e.g. Chou Jie Lun = Jay Chou
  * 
  * Legal Changes:
  * Artists are often forced to change their names for legal reasons, sometimes only in part of the world
  * e.g. Yaz = Yazoo. 
  * 
  * @author paul
  *
  */
 public class ArtistAlias {
 
 	private String alias;
 	
 	private SpecificLocale locale;
 	
 	private ArtistAliasType type;
 
 	public String getAlias() {
 		return alias;
 	}
 
 	public void setAlias(String alias) {
 		this.alias = alias;
 	}
 
 	public SpecificLocale getLocale() {
 		return locale;
 	}
 
 	public void setLocale(SpecificLocale locale) {
 		this.locale = locale;
 	}
 
 	public ArtistAliasType getType() {
 		return type;
 	}
 
 	public void setType(ArtistAliasType type) {
 		this.type = type;
 	}
 	
 }
