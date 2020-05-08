 package com.ubs.opi.domain;
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.Vector;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.context.SecurityContext;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.core.userdetails.User;
 
 public class Utils {
 	public static final Log log = LogFactory.getLog(Utils.class);
 
 	static private Map<String, String> countries = new LinkedHashMap<String, String>();
 	static private Map<String, String> nationalities = new LinkedHashMap<String, String>();
 	// static private List<String> nationalities = new Vector<String>();
 	static private Map<Long, String> advisors = new TreeMap<Long, String>();
 	static private Map<Long, String> investors = new LinkedHashMap<Long, String>();
 	static private Map<Long, String> appstatus = new TreeMap<Long, String>();
 	static private Map<Long, String> paymentstatus = new TreeMap<Long, String>();
 	static private Map<Long, String> investortypes = new TreeMap<Long, String>();
 	static private List<String> titles = new Vector<String>();
 	static private List<String> states = new Vector<String>();
 
 	static public boolean hasAuthority(String auth) {
 		Collection<? extends GrantedAuthority> authorities = SecurityContextHolder
 				.getContext().getAuthentication().getAuthorities();
 		for (GrantedAuthority authority : authorities) {
			if (authority.equals(auth))
 				return true;
 		}
 		return false;
 	}
 
 	static public void logout() {
 		SecurityContextHolder.setContext(null);
 	}
 
 	static public User getUser() {
 		User user = null;
 		SecurityContext context = SecurityContextHolder.getContext();
 		Authentication authentication = context.getAuthentication();
 		if (authentication != null) {
 			user = (User) authentication.getPrincipal();
 			if (user != null) {
 				log.info("Username=" + user.getUsername());
 			} else {
 				log.info("User not Logged on!");
 			}
 		}
 		return user;
 	}
 
 	static {
 
 		// Investor (and Company) Types
 		investortypes.put(Constants.INDIVIDUAL_TYPE, Constants.INDIVIDUAL);
 		investortypes.put(Constants.JOINT_TYPE, Constants.JOINT);
 		investortypes.put(Constants.CORPORATE_TYPE, Constants.CORPORATE);
 		investortypes
 				.put(Constants.PRIVATE_TRUST_TYPE, Constants.PRIVATE_TRUST);
 		investortypes.put(Constants.CORPORATE_TRUST_TYPE,
 				Constants.CORPORATE_TRUST);
 
 		appstatus.put(Constants.DRAFT_STATUS, Constants.DRAFT);
 		appstatus.put(Constants.SUBMITTED_STATUS, Constants.SUBMITTED);
 		appstatus.put(Constants.PROCESSING_STATUS, Constants.PROCESSING);
 		appstatus.put(Constants.INFORMATION_REQUIRED_STATUS,
 				Constants.INFORMATION_REQUIRED);
 		appstatus.put(Constants.CONDITIONAL_APPROVAL_STATUS,
 				Constants.CONDITIONAL_APPROVAL);
 		appstatus.put(Constants.APPROVED_STATUS, Constants.APPROVED);
 		appstatus.put(Constants.REJECTED_STATUS, Constants.REJECTED);
 		appstatus.put(Constants.WITHDRAWN_STATUS, Constants.WITHDRAWN);
 		appstatus.put(Constants.CANCELLED_STATUS, Constants.CANCELLED);
 
 		// Payment State Map
 		paymentstatus.put(Constants.PAY_STATE_PENDING_KEY,
 				Constants.PAY_STATE_PENDING);
 		paymentstatus.put(Constants.PAY_STATE_SCHEDULED_KEY,
 				Constants.PAY_STATE_SCHEDULED);
 		paymentstatus.put(Constants.PAY_STATE_PAID_KEY,
 				Constants.PAY_STATE_PAID);
 		paymentstatus.put(Constants.PAY_STATE_QUERY_KEY,
 				Constants.PAY_STATE_QUERY);
 
 		investors.put(Constants.SELECT_INVESTOR_TYPE, "Select Investor");
 		investors.put(Constants.INDIVIDUAL_TYPE, Constants.NEW_
 				+ Constants.INDIVIDUAL);
 		investors.put(Constants.JOINT_TYPE, Constants.NEW_ + Constants.JOINT);
 		investors.put(Constants.CORPORATE_TYPE, Constants.NEW_
 				+ Constants.CORPORATE);
 		investors.put(Constants.PRIVATE_TRUST_TYPE, Constants.NEW_
 				+ Constants.PRIVATE_TRUST);
 		investors.put(Constants.CORPORATE_TRUST_TYPE, Constants.NEW_
 				+ Constants.CORPORATE_TRUST);
 
 		titles.add("Mr");
 		titles.add("Mrs");
 		titles.add("Miss");
 		titles.add("Ms");
 		titles.add("Dr");
 
 		states.add("NSW");
 		states.add("VIC");
 		states.add("QLD");
 		states.add("ACT");
 		states.add("SA");
 		states.add("WA");
 		states.add("TAS");
 		states.add("NT");
 
 		countries.put("", "Select Country"); // fudge to put at top of list,
 												// should be AUS
 		countries.put("AUSTRALIA", "AUSTRALIA"); // fudge to put at top of list,
 													// should be AUS
 		countries.put("AFGHANISTAN", "AFGHANISTAN");
 		countries.put("ALBANIA", "ALBANIA");
 		countries.put("ALGERIA", "ALGERIA");
 		countries.put("AMERICAN SAMOA", "AMERICAN SAMOA");
 		countries.put("ANDORRA", "ANDORRA");
 		countries.put("ANGOLA", "ANGOLA");
 		countries.put("ANTIGUA, BARBUDA AND REDONDA",
 				"ANTIGUA, BARBUDA AND REDONDA");
 		countries.put("ARGENTINA", "ARGENTINA");
 		countries.put("ARMENIA", "ARMENIA");
 		countries.put("ARUBA", "ARUBA");
 		countries.put("AUSTRIA", "AUSTRIA");
 		countries.put("AZERBAIJAN", "AZERBAIJAN");
 		countries.put("BAHAMAS", "BAHAMAS");
 		countries.put("BAHRAIN", "BAHRAIN");
 		countries.put("BANGLADESH", "BANGLADESH");
 		countries.put("BARBADOS", "BARBADOS");
 		countries.put("BELARUS", "BELARUS");
 		countries.put("BELGIUM", "BELGIUM");
 		countries.put("BELIZE", "BELIZE");
 		countries.put("BENIN", "BENIN");
 		countries.put("BERMUDA", "BERMUDA");
 		countries.put("BHUTAN", "BHUTAN");
 		countries.put("BOLIVIA", "BOLIVIA");
 		countries.put("BORNEO", "BORNEO");
 		countries.put("BOSNIA and HERZEGOWINA", "BOSNIA and HERZEGOWINA");
 		countries.put("BOTSWANA", "BOTSWANA");
 		countries.put("BOUVET ISLAND", "BOUVET ISLAND");
 		countries.put("BRAZIL", "BRAZIL");
 		countries.put("BRITISH INDIAN OCEAN TERRITORY",
 				"BRITISH INDIAN OCEAN TERRITORY");
 		countries.put("BRITISH VIRGIN ISLANDS", "BRITISH VIRGIN ISLANDS");
 		countries.put("BRUNEI DARUSSALAM", "BRUNEI DARUSSALAM");
 		countries.put("BULGARIA", "BULGARIA");
 		countries.put("BURKINA FASO", "BURKINA FASO");
 		countries.put("BURMA", "BURMA");
 		countries.put("BURUNDI", "BURUNDI");
 		countries.put("BYELORUSSIAN SSR", "BYELORUSSIAN SSR");
 		countries.put("CAMBODIA", "CAMBODIA");
 		countries.put("CAMEROON", "CAMEROON");
 		countries.put("CANADA", "CANADA");
 		countries.put("CANTON AND ENDERBURY ISLANDS",
 				"CANTON AND ENDERBURY ISLANDS");
 		countries.put("CAPE VERDE ISLANDS", "CAPE VERDE ISLANDS");
 		countries.put("CAYMAN ISLANDS", "CAYMAN ISLANDS");
 		countries.put("CENTRAL AFRICAN REPUBLIC", "CENTRAL AFRICAN REPUBLIC");
 		countries.put("CHAD", "CHAD");
 		countries.put("CHANNEL ISLANDS", "CHANNEL ISLANDS");
 		countries.put("CHILE", "CHILE");
 		countries.put("CHINA", "CHINA");
 		countries.put("CHRISTMAS ISLAND", "CHRISTMAS ISLAND");
 		countries.put("COCOS ISLANDS", "COCOS ISLANDS");
 		countries.put("COLOMBIA", "COLOMBIA");
 		countries.put("COMOROS", "COMOROS");
 		countries.put("CONGO", "CONGO");
 		countries.put("COOK ISLANDS", "COOK ISLANDS");
 		countries.put("COSTA RICA", "COSTA RICA");
 		countries.put("CROATIA", "CROATIA");
 		countries.put("CUBA", "CUBA");
 		countries.put("CYPRUS", "CYPRUS");
 		countries.put("CZECH REPUBLIC", "CZECH REPUBLIC");
 		countries.put("DENMARK", "DENMARK");
 		countries.put("DJIBOUTI", "DJIBOUTI");
 		countries.put("DOMINCA", "DOMINCA");
 		countries.put("DOMINICAN REPUBLIC", "DOMINICAN REPUBLIC");
 		countries.put("DRONNING MAUD LAND", "DRONNING MAUD LAND");
 		countries.put("EAST TIMOR", "EAST TIMOR");
 		countries.put("ECUADOR", "ECUADOR");
 		countries.put("EGYPT", "EGYPT");
 		countries.put("EIRE", "EIRE");
 		countries.put("EL SALVADOR", "EL SALVADOR");
 		countries.put("EQUATORIAL GUINEA", "EQUATORIAL GUINEA");
 		countries.put("ERITREA", "ERITREA");
 		countries.put("ESTONIA", "ESTONIA");
 		countries.put("ETHIOPIA", "ETHIOPIA");
 		countries.put("FALKLAND ISLANDS", "FALKLAND ISLANDS");
 		countries.put("FAROE ISLANDS", "FAROE ISLANDS");
 		countries.put("FIJI", "FIJI");
 		countries.put("FINLAND", "FINLAND");
 		countries.put("FRANCE", "FRANCE");
 		countries.put("FRANCE,METROPOLITAN", "FRANCE,METROPOLITAN");
 		countries.put("FRENCH GUIANA", "FRENCH GUIANA");
 		countries.put("FRENCH POLYNESIA", "FRENCH POLYNESIA");
 		countries.put("FRENCH SOUTHERN TERRITORIES",
 				"FRENCH SOUTHERN TERRITORIES");
 		countries.put("GABON", "GABON");
 		countries.put("GAMBIA", "GAMBIA");
 		countries.put("GEORGIA", "GEORGIA");
 		countries.put("GERMANY", "GERMANY");
 		countries.put("GHANA", "GHANA");
 		countries.put("GIBRALTAR", "GIBRALTAR");
 		countries.put("GREECE", "GREECE");
 		countries.put("GREENLAND", "GREENLAND");
 		countries.put("GRENADA", "GRENADA");
 		countries.put("GUADELOUPE", "GUADELOUPE");
 		countries.put("GUAM", "GUAM");
 		countries.put("GUATEMALA", "GUATEMALA");
 		countries.put("GUINEA", "GUINEA");
 		countries.put("GUINEA-BISSAU", "GUINEA-BISSAU");
 		countries.put("GUYANA", "GUYANA");
 		countries.put("HAITI", "HAITI");
 		countries.put("HONDURAS", "HONDURAS");
 		countries.put("HONG KONG", "HONG KONG");
 		countries.put("HUNGARY", "HUNGARY");
 		countries.put("ICELAND", "ICELAND");
 		countries.put("INDIA", "INDIA");
 		countries.put("INDONESIA", "INDONESIA");
 		countries.put("IRAN", "IRAN");
 		countries.put("IRAQ", "IRAQ");
 		countries.put("IRELAND", "IRELAND");
 		countries.put("ISLE OF MANN", "ISLE OF MANN");
 		countries.put("ISRAEL", "ISRAEL");
 		countries.put("ITALY", "ITALY");
 		countries.put("IVORY COAST", "IVORY COAST");
 		countries.put("JAMAICA", "JAMAICA");
 		countries.put("JAPAN", "JAPAN");
 		countries.put("JOHNSTON ISLAND", "JOHNSTON ISLAND");
 		countries.put("JORDAN", "JORDAN");
 		countries.put("KAZAKHSTAN", "KAZAKHSTAN");
 		countries.put("KENYA", "KENYA");
 		countries.put("KIRIBATI", "KIRIBATI");
 		countries.put("KOREA DEMOCRATIC PEOPLES REPUBLIC)",
 				"KOREA DEMOCRATIC PEOPLES REPUBLIC");
 		countries.put("KOREA REPUBLIC OF (SOUTH)", "KOREA REPUBLIC OF (SOUTH)");
 		countries.put("KUWAIT", "KUWAIT");
 		countries.put("KYRGYZSTAN", "KYRGYZSTAN");
 		countries.put("LAO PEOPLES DEMOCRATIC REP",
 				"LAO PEOPLES DEMOCRATIC REP");
 		countries.put("LATVIA", "LATVIA");
 		countries.put("LEBANON", "LEBANON");
 		countries.put("LESOTHO", "LESOTHO");
 		countries.put("LIBERIA", "LIBERIA");
 		countries.put("LIBYAN ARAB JAMAHIRIYA", "LIBYAN ARAB JAMAHIRIYA");
 		countries.put("LIECHTENSTEIN", "LIECHTENSTEIN");
 		countries.put("LITHUANIA", "LITHUANIA");
 		countries.put("LUXEMBOURG", "LUXEMBOURG");
 		countries.put("MACAU", "MACAU");
 		countries.put("MACEDONIA", "MACEDONIA");
 		countries.put("MADAGASCAR", "MADAGASCAR");
 		countries.put("MALAWI", "MALAWI");
 		countries.put("MALAYSIA", "MALAYSIA");
 		countries.put("MALDIVES", "MALDIVES");
 		countries.put("MALI", "MALI");
 		countries.put("MALTA", "MALTA");
 		countries.put("MARSHALL ISLANDS", "MARSHALL ISLANDS");
 		countries.put("MARTINIQUE", "MARTINIQUE");
 		countries.put("MAURITANIA ISLAMIC REPUBLIC OF",
 				"MAURITANIA ISLAMIC REPUBLIC OF");
 		countries.put("MAURITIUS", "MAURITIUS");
 		countries.put("MAYOTTE", "MAYOTTE");
 		countries.put("MEXICO", "MEXICO");
 		countries.put("MICRONESIA", "MICRONESIA");
 		countries.put("MIDWAY ISLANDS", "MIDWAY ISLANDS");
 		countries.put("MOLDOVA,REPUBLIC OF", "MOLDOVA,REPUBLIC OF");
 		countries.put("MONACO", "MONACO");
 		countries.put("MONGOLIA", "MONGOLIA");
 		countries.put("MONSERRAT", "MONSERRAT");
 		countries.put("MOROCCO", "MOROCCO");
 		countries.put("MOZAMBIQUE", "MOZAMBIQUE");
 		countries.put("MYANMAR", "MYANMAR");
 		countries.put("NAMIBIA", "NAMIBIA");
 		countries.put("NAURU", "NAURU");
 		countries.put("NEPAL", "NEPAL");
 		countries.put("NETHERLANDS", "NETHERLANDS");
 		countries.put("NETHERLANDS ANTILLES", "NETHERLANDS ANTILLES");
 		countries.put("NEUTRAL ZONE", "NEUTRAL ZONE");
 		countries.put("NEW CALEDONIA", "NEW CALEDONIA");
 		countries.put("NEW ZEALAND", "NEW ZEALAND");
 		countries.put("NICARAGUA", "NICARAGUA");
 		countries.put("NIGER", "NIGER");
 		countries.put("NIGERIA", "NIGERIA");
 		countries.put("NIUE", "NIUE");
 		countries.put("NORFOLK ISLAND", "NORFOLK ISLAND");
 		countries.put("NORTHERN MARIANA ISLANDS", "NORTHERN MARIANA ISLANDS");
 		countries.put("NORWAY", "NORWAY");
 		countries.put("OMAN", "OMAN");
 		countries.put("OVERSEAS", "OVERSEAS");
 		countries.put("PAKISTAN", "PAKISTAN");
 		countries.put("PALAU", "PALAU");
 		countries.put("PANAMA", "PANAMA");
 		countries.put("PAPUA NEW GUINEA", "PAPUA NEW GUINEA");
 		countries.put("PARAGUAY", "PARAGUAY");
 		countries.put("PERU", "PERU");
 		countries.put("PHILIPPINES", "PHILIPPINES");
 		countries.put("PITCAIRN ISLAND", "PITCAIRN ISLAND");
 		countries.put("POLAND", "POLAND");
 		countries.put("PORTUGAL", "PORTUGAL");
 		countries.put("PUERTO RICO", "PUERTO RICO");
 		countries.put("QATAR", "QATAR");
 		countries.put("REUNION", "REUNION");
 		countries.put("ROMANIA", "ROMANIA");
 		countries.put("RUSSIAN FEDERATION", "RUSSIAN FEDERATION");
 		countries.put("RWANDA", "RWANDA");
 		countries.put("SAINT VINCENT GRENADINES", "SAINT VINCENT GRENADINES");
 		countries.put("SAMOA", "SAMOA");
 		countries.put("SAN MARINO", "SAN MARINO");
 		countries.put("SAO TOME AND PRINCIPE", "SAO TOME AND PRINCIPE");
 		countries.put("SAUDI ARABIA", "SAUDI ARABIA");
 		countries.put("SENEGAL", "SENEGAL");
 		countries.put("SEYCHELLES", "SEYCHELLES");
 		countries.put("SIERRA LEONE", "SIERRA LEONE");
 		countries.put("SINGAPORE", "SINGAPORE");
 		countries.put("SLOVAKIA", "SLOVAKIA");
 		countries.put("SLOVENIA", "SLOVENIA");
 		countries.put("SOLOMON ISLANDS", "SOLOMON ISLANDS");
 		countries.put("SOMALIA", "SOMALIA");
 		countries.put("SOUTH AFRICA", "SOUTH AFRICA");
 		countries.put("SOUTH GEORGIA AND THE SOUTH SA",
 				"SOUTH GEORGIA AND THE SOUTH SA");
 		countries.put("SPAIN", "SPAIN");
 		countries.put("SRI LANKA", "SRI LANKA");
 		countries.put("ST HELENA", "ST HELENA");
 		countries.put("ST KITTS-NEVIS-ANGUILLA", "ST KITTS-NEVIS-ANGUILLA");
 		countries.put("ST LUCIA", "ST LUCIA");
 		countries.put("ST PIERRE AND MIQUELON", "ST PIERRE AND MIQUELON");
 		countries.put("SUDAN", "SUDAN");
 		countries.put("SURINAME", "SURINAME");
 		countries.put("SVALBARD AND JAN MAYEN ISLANDS",
 				"SVALBARD AND JAN MAYEN ISLANDS");
 		countries.put("SWAZILAND", "SWAZILAND");
 		countries.put("SWEDEN", "SWEDEN");
 		countries.put("SWITZERLAND", "SWITZERLAND");
 		countries.put("SYRIAN ARAB REPUBLIC", "SYRIAN ARAB REPUBLIC");
 		countries.put("TAIWAN, PROVINCE OF CHINA", "TAIWAN, PROVINCE OF CHINA");
 		countries.put("TAJIKISTAN", "TAJIKISTAN");
 		countries.put("TANZANIA, UNITED REPUBLIC OF",
 				"TANZANIA, UNITED REPUBLIC OF");
 		countries.put("THAILAND", "THAILAND");
 		countries.put("TOGO", "TOGO");
 		countries.put("TOKELAU", "TOKELAU");
 		countries.put("TONGA", "TONGA");
 		countries.put("TRINIDAD AND TOBAGO", "TRINIDAD AND TOBAGO");
 		countries.put("TRUST TERRITORY OF PACIFIC ISLAND)",
 				"TRUST TERRITORY OF PACIFIC ISLAND");
 		countries.put("TUNISIA", "TUNISIA");
 		countries.put("TURKEY", "TURKEY");
 		countries.put("TURKMENISTAN", "TURKMENISTAN");
 		countries.put("TURKS AND CAICOS ISLANDS", "TURKS AND CAICOS ISLANDS");
 		countries.put("TUVALU", "TUVALU");
 		countries.put("UGANDA", "UGANDA");
 		countries.put("UKRAINIAN SSR", "UKRAINIAN SSR");
 		countries.put("UNITED ARAB EMIRATES", "UNITED ARAB EMIRATES");
 		countries.put("UNITED KINGDOM", "UNITED KINGDOM");
 		countries.put("UNITED STATES MINOR OUTLYING ISLAND",
 				"UNITED STATES MINOR OUTLYING ISLANDS");
 		countries.put("UNITED STATES MISC PACIFIC ISLANDS",
 				"UNITED STATES MISC PACIFIC ISLANDS");
 		countries.put("UNITED STATES OF AMERICA", "UNITED STATES OF AMERICA");
 		countries.put("UNITED STATES VIRGIN ISLANDS",
 				"UNITED STATES VIRGIN ISLANDS");
 		countries.put("UPPER VOLTA", "UPPER VOLTA");
 		countries.put("URUGUAY", "URUGUAY");
 		countries.put("USSR", "USSR");
 		countries.put("UZBEKISTAN", "UZBEKISTAN");
 		countries.put("VANUATU", "VANUATU");
 		countries.put("VATICAN CITY STATE (HOLY SEE)",
 				"VATICAN CITY STATE (HOLY SEE)");
 		countries.put("VENEZUELA", "VENEZUELA");
 		countries.put("VIETNAM", "VIETNAM");
 		countries.put("WAKE ISLAND", "WAKE ISLAND");
 		countries.put("WALLIS AND FUTUNA ISLAND", "WALLIS AND FUTUNA ISLAND");
 		countries.put("WESTERN SAHARA", "WESTERN SAHARA");
 		countries.put("YEMEN", "YEMEN");
 		countries.put("YEMEN, DEMOCRATIC", "YEMEN, DEMOCRATIC");
 		countries.put("YUGOSLAVIA", "YUGOSLAVIA");
 		countries.put("ZAIRE (FORMERLY CONGO)", "ZAIRE (FORMERLY CONGO)");
 		countries.put("ZAMBIA", "ZAMBIA");
 		countries.put("ZIMBABWE (FORMERLY RHODESIA)",
 				"ZIMBABWE (FORMERLY RHODESIA)");
 		countries.put("OTHER", "OTHER");
 
 		nationalities.put("", "Select Nationality");
 		nationalities.put("AUSTRALIAN", "AUSTRALIAN");
 		nationalities.put("AFGHAN", "AFGHAN");
 		nationalities.put("ALBANIAN", "ALBANIAN");
 		nationalities.put("ALGERIAN", "ALGERIAN");
 		nationalities.put("AMERICAN SAMOAN", "AMERICAN SAMOAN");
 		nationalities.put("AMERICAN", "AMERICAN");
 		nationalities.put("ANDORRAN", "ANDORRAN");
 		nationalities.put("ANGOLAN", "ANGOLAN");
 		nationalities.put("ANTIGUA, BARBUDA AND REDONDA",
 				"ANTIGUA, BARBUDA AND REDONDA");
 		nationalities.put("ARGENTINE", "ARGENTINE");
 		nationalities.put("ARMENIAN", "ARMENIAN");
 		nationalities.put("AUSTRIAN", "AUSTRIAN");
 		nationalities.put("BAHAMIAM", "BAHAMIAM");
 		nationalities.put("BAHRAINI", "BAHRAINI");
 		nationalities.put("BANGLADESHI", "BANGLADESHI");
 		nationalities.put("BARBADIAN", "BARBADIAN");
 		nationalities.put("BASOTHO", "BASOTHO");
 		nationalities.put("BATSWANA", "BATSWANA");
 		nationalities.put("BELARUSIAN", "BELARUSIAN");
 		nationalities.put("BELGIAN", "BELGIAN");
 		nationalities.put("BELIZEAN", "BELIZEAN");
 		nationalities.put("BENINESE", "BENINESE");
 		nationalities.put("BERMUDAN", "BERMUDAN");
 		nationalities.put("BHUTANESE", "BHUTANESE");
 		nationalities.put("BOLIVIAN", "BOLIVIAN");
 		nationalities.put("BOSNIAN and HERZEGOVINIAN",
 				"BOSNIAN and HERZEGOVINIAN");
 		nationalities.put("BRAZILIAN", "BRAZILIAN");
 		nationalities.put("BRITISH INDIAN OCEAN TERRITORIAN",
 				"BRITISH INDIAN OCEAN TERRITORIAN");
 		nationalities.put("BRITISH VIRGIN ISLANDER", "BRITISH VIRGIN ISLANDER");
 		nationalities.put("BRITISH", "BRITISH");
 		nationalities.put("BRUNEIAN", "BRUNEIAN");
 		nationalities.put("BULGARIAN", "BULGARIAN");
 		nationalities.put("BURKINABE", "BURKINABE");
 		nationalities.put("BURMEESE", "BURMEESE");
 		nationalities.put("BURMESE", "BURMESE");
 		nationalities.put("BURUNDIAN", "BURUNDIAN");
 		nationalities.put("BYELORUSSIAN", "BYELORUSSIAN");
 		nationalities.put("CAMBODIAN", "CAMBODIAN");
 		nationalities.put("CAMEROONIAN", "CAMEROONIAN");
 		nationalities.put("CANADIAN", "CANADIAN");
 		nationalities.put("CAYMAN", "CAYMAN");
 		nationalities.put("CHADIAN", "CHADIAN");
 		nationalities.put("CHILEAN", "CHILEAN");
 		nationalities.put("CHINESE", "CHINESE");
 		nationalities.put("COLOMBIAN", "COLOMBIAN");
 		nationalities.put("CONGOLESE", "CONGOLESE");
 		nationalities.put("COSTA RICAN", "COSTA RICAN");
 		nationalities.put("CROATIAN", "CROATIAN");
 		nationalities.put("CUBAN", "CUBAN");
 		nationalities.put("CYPRIOT", "CYPRIOT");
 		nationalities.put("CZECH", "CZECH");
 		nationalities.put("DANE", "DANE");
 		nationalities.put("DOMINICAN", "DOMINICAN");
 		nationalities.put("DUTCH", "DUTCH");
 		nationalities.put("EAST TIMORESE", "EAST TIMORESE");
 		nationalities.put("ECUADORIAN", "ECUADORIAN");
 		nationalities.put("EGYPTIAN", "EGYPTIAN");
 		nationalities.put("EL SALVADORIAN", "EL SALVADORIAN");
 		nationalities.put("EMIRIAN", "EMIRIAN");
 		nationalities.put("ERITREAN", "ERITREAN");
 		nationalities.put("ESTONIAN", "ESTONIAN");
 		nationalities.put("ETHIOPIAN", "ETHIOPIAN");
 		nationalities.put("FIJIAN", "FIJIAN");
 		nationalities.put("FILIPINO", "FILIPINO");
 		nationalities.put("FINN", "FINN");
 		nationalities.put("FRENCH", "FRENCH");
 		nationalities.put("GABONESE", "GABONESE");
 		nationalities.put("GAMBIAN", "GAMBIAN");
 		nationalities.put("GEORGIAN", "GEORGIAN");
 		nationalities.put("GERMAN", "GERMAN");
 		nationalities.put("GHANAIAN", "GHANAIAN");
 		nationalities.put("GREEK", "GREEK");
 		nationalities.put("GRENADIAN", "GRENADIAN");
 		nationalities.put("GUADELOUPIAN", "GUADELOUPIAN");
 		nationalities.put("GUAM", "GUAM");
 		nationalities.put("GUATEMALAN", "GUATEMALAN");
 		nationalities.put("GUINEAN", "GUINEAN");
 		nationalities.put("GUYANESE", "GUYANESE");
 		nationalities.put("HAITIAN", "HAITIAN");
 		nationalities.put("HONDURAN", "HONDURAN");
 		nationalities.put("HUNGARIAN", "HUNGARIAN");
 		nationalities.put("ICELANDER", "ICELANDER");
 		nationalities.put("INDIAN", "INDIAN");
 		nationalities.put("INDONESIAN", "INDONESIAN");
 		nationalities.put("IRANIAN", "IRANIAN");
 		nationalities.put("IRAQI", "IRAQI");
 		nationalities.put("IRISH", "IRISH");
 		nationalities.put("ISRAELI", "ISRAELI");
 		nationalities.put("ITALIAN", "ITALIAN");
 		nationalities.put("IVORIAN", "IVORIAN");
 		nationalities.put("JAMAICAN", "JAMAICAN");
 		nationalities.put("JAPANESE", "JAPANESE");
 		nationalities.put("JORDANIAN", "JORDANIAN");
 		nationalities.put("KAZAKHSTANI", "KAZAKHSTANI");
 		nationalities.put("KENYAN", "KENYAN");
 		nationalities.put("KIRIBATIAN", "KIRIBATIAN");
 		nationalities.put("KOREAN", "KOREAN");
 		nationalities.put("KUWAITI", "KUWAITI");
 		nationalities.put("KYRGYZSTANI", "KYRGYZSTANI");
 		nationalities.put("LAO", "LAO");
 		nationalities.put("LATVIAN", "LATVIAN");
 		nationalities.put("LEBANESE", "LEBANESE");
 		nationalities.put("LIBERIAN", "LIBERIAN");
 		nationalities.put("LIBYAN", "LIBYAN");
 		nationalities.put("LIECHTENSTEINER", "LIECHTENSTEINER");
 		nationalities.put("LITHUANIAN", "LITHUANIAN");
 		nationalities.put("LUXEMBOURGER", "LUXEMBOURGER");
 		nationalities.put("MACEDONIAN", "MACEDONIAN");
 		nationalities.put("MADAGASY", "MADAGASY");
 		nationalities.put("MALAWIAN", "MALAWIAN");
 		nationalities.put("MALAYSIAN", "MALAYSIAN");
 		nationalities.put("MALDIVAN", "MALDIVAN");
 		nationalities.put("MALIAN", "MALIAN");
 		nationalities.put("MALTESE", "MALTESE");
 		nationalities.put("MAURITANIAN", "MAURITANIAN");
 		nationalities.put("MAURITIAN", "MAURITIAN");
 		nationalities.put("MEXICAN", "MEXICAN");
 		nationalities.put("MICRONESIAN", "MICRONESIAN");
 		nationalities.put("MOLDOVIAN", "MOLDOVIAN");
 		nationalities.put("MONEGASQUE", "MONEGASQUE");
 		nationalities.put("MONGOLIAN", "MONGOLIAN");
 		nationalities.put("MONSERRATIAN", "MONSERRATIAN");
 		nationalities.put("MOROCCAN", "MOROCCAN");
 		nationalities.put("MOZAMBICAN", "MOZAMBICAN");
 		nationalities.put("NAMIBIAN", "NAMIBIAN");
 		nationalities.put("NAURUAN", "NAURUAN");
 		nationalities.put("NEPALESE", "NEPALESE");
 		nationalities.put("NEW CALEDONIAN", "NEW CALEDONIAN");
 		nationalities.put("NEW ZEALANDER", "NEW ZEALANDER");
 		nationalities.put("NI-VANUATU", "NI-VANUATU");
 		nationalities.put("NICARAGUAN", "NICARAGUAN");
 		nationalities.put("NIGERIAN", "NIGERIAN");
 		nationalities.put("NIGERIEN", "NIGERIEN");
 		nationalities.put("NORFOLK ISLANDER", "NORFOLK ISLANDER");
 		nationalities.put("NORWEGIAN", "NORWEGIAN");
 		nationalities.put("OMANI", "OMANI");
 		nationalities.put("PAKISTANI", "PAKISTANI");
 		nationalities.put("PALAUAN", "PALAUAN");
 		nationalities.put("PANAMAMANIAN", "PANAMAMANIAN");
 		nationalities.put("PAPUA NEW GUINEAN", "PAPUA NEW GUINEAN");
 		nationalities.put("PARAGUAYAN", "PARAGUAYAN");
 		nationalities.put("PERUVIAN", "PERUVIAN");
 		nationalities.put("PITCAIRN ISLANDER", "PITCAIRN ISLANDER");
 		nationalities.put("POLE", "POLE");
 		nationalities.put("PORTUGUESE", "PORTUGUESE");
 		nationalities.put("PUERTO RICAN", "PUERTO RICAN");
 		nationalities.put("QATARI", "QATARI");
 		nationalities.put("ROMANIAN", "ROMANIAN");
 		nationalities.put("RUSSIAN", "RUSSIAN");
 		nationalities.put("RWANDAN", "RWANDAN");
 		nationalities.put("SAMMARINESE", "SAMMARINESE");
 		nationalities.put("SAMOAN", "SAMOAN");
 		nationalities.put("SAUDI", "SAUDI");
 		nationalities.put("SENEGALESE", "SENEGALESE");
 		nationalities.put("SEYCHELLOIS", "SEYCHELLOIS");
 		nationalities.put("SIERRA LEONEAN", "SIERRA LEONEAN");
 		nationalities.put("SINGAPORIAN", "SINGAPORIAN");
 		nationalities.put("SLOVAKIAN", "SLOVAKIAN");
 		nationalities.put("SLOVENIAN", "SLOVENIAN");
 		nationalities.put("SOLOMON ISLANDER", "SOLOMON ISLANDER");
 		nationalities.put("SOMALIAN", "SOMALIAN");
 		nationalities.put("SOUTH AFRICAN", "SOUTH AFRICAN");
 		nationalities.put("SPAINISH", "SPAINISH");
 		nationalities.put("SRI LANKAN", "SRI LANKAN");
 		nationalities.put("ST HELENAN", "ST HELENAN");
 		nationalities.put("ST LUCIAN", "ST LUCIAN");
 		nationalities.put("SUDANESE", "SUDANESE");
 		nationalities.put("SURINAMER", "SURINAMER");
 		nationalities.put("SWAZI", "SWAZI");
 		nationalities.put("SWEDE", "SWEDE");
 		nationalities.put("SWISS", "SWISS");
 		nationalities.put("SYRIAN", "SYRIAN");
 		nationalities.put("TAIWANESE", "TAIWANESE");
 		nationalities.put("TAJIK", "TAJIK");
 		nationalities.put("TANZANIAN", "TANZANIAN");
 		nationalities.put("THAI", "THAI");
 		nationalities.put("TONGAN", "TONGAN");
 		nationalities.put("TRINIDADIAN", "TRINIDADIAN");
 		nationalities.put("TUNISIAN", "TUNISIAN");
 		nationalities.put("TURKISH", "TURKISH");
 		nationalities.put("TURKMEN", "TURKMEN");
 		nationalities.put("UGANDAN", "UGANDAN");
 		nationalities.put("UKRAINIAN", "UKRAINIAN");
 		nationalities.put("URUGUAYAN", "URUGUAYAN");
 		nationalities.put("UZBEK", "UZBEK");
 		nationalities.put("VENEZUELAN", "VENEZUELAN");
 		nationalities.put("VIETNAMESE", "VIETNAMESE");
 		nationalities.put("WAKE ISLAND", "WAKE ISLAND");
 		nationalities.put("YEMENESE", "YEMENESE");
 		nationalities.put("ZAIREAN", "ZAIREAN");
 		nationalities.put("ZAMBIAN", "ZAMBIAN");
 		nationalities.put("ZIMBABWEAN", "ZIMBABWEAN");
 		nationalities.put("OTHER", "OTHER");
 
 	}
 
 	public static boolean isAllDigits(String str) {
 		for (int z = 0, max = str.length(); z < max; z++) {
 			if (!Character.isDigit(str.charAt(z))) {
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	public static boolean isAllDigitsOrSpaces(String str) {
 		for (int z = 0, max = str.length(); z < max; z++) {
 			if (!Character.isDigit(str.charAt(z))) {
 				if (!Character.isSpaceChar(str.charAt(z))) {
 					return false;
 				}
 			}
 		}
 
 		return true;
 	}
 
 	public static boolean isAllLetters(String str) {
 		String tmp = str.trim();
 
 		for (int z = 0, max = tmp.length(); z < max; z++) {
 			if (!Character.isLetter(tmp.charAt(z))) {
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	// public static Number isAlphaNumeric(String str)
 	// {
 	// Number number = null;
 	//
 	// try
 	// {
 	// number = new Number(str);
 	// }
 	// catch (Exception e)
 	// {
 	// }
 	//
 	// return number;
 	// }
 
 	/**
 	 * Return true if the passed oracle date is in the future
 	 */
 	public static boolean isFutureDate(Date dateValue) {
 		return dateValue.getTime() > (new java.util.Date()).getTime();
 	}
 
 	/**
 	 * Returns true if the string is either null or empty.
 	 * 
 	 * @param s
 	 *            The string to evaluate
 	 * @return True if the string is either null or empty.
 	 */
 	public static boolean isNullOrEmpty(String s) {
 		return (s == null) || s.trim().equals("");
 	}
 
 	/**
 	 * Return true if the passed oracle date is empty or null Note: At this time
 	 * a length of 0 means empty - this should never occur. FIXME: For my
 	 * implementation I mainly want the null check however someone in the future
 	 * may want to see what a default constructor date creates and match
 	 * 'emptyness' against that in this method.
 	 */
 	public static boolean isNullOrEmpty(Date dateValue) {
 		boolean dateEmptyOrNull = false;
 
 		if (dateValue == null) {
 			dateEmptyOrNull = true;
 		} else if (dateValue.toString().length() == 0) {
 			dateEmptyOrNull = true;
 		}
 
 		return dateEmptyOrNull;
 	}
 
 	public static Map<String, String> getCountries() {
 		return countries;
 	}
 
 	public static Map<String, String> getDefaultCountries() {
 		Map<String, String> defaultCountries = new LinkedHashMap<String, String>(
 				Utils.getCountries());
 		log.info("remove : " + defaultCountries.remove("AAA"));
 		return defaultCountries;
 	}
 
 	public static List<String> getDefaultStates() {
 		List<String> defaultstates = new Vector<String>(states);
 		defaultstates.remove("");
 		return defaultstates;
 	}
 
 	public static List<String> getStates() {
 		return states;
 	}
 
 	public static List<String> getTitles() {
 		return titles;
 	}
 
 	public static Map<Long, String> getInvestors() {
 		return investors;
 	}
 
 	public static Map<Long, String> getAdvisors() {
 		return advisors;
 	}
 
 	public static Map<Long, String> getAppstatus() {
 		return appstatus;
 	}
 
 	public static Map<Long, String> getPaymentstatus() {
 		return paymentstatus;
 	}
 
 	public static Map<Long, String> getInvestortypes() {
 		return investortypes;
 	}
 
 	public static void setInvestortypes(Map<Long, String> investortypes) {
 		Utils.investortypes = investortypes;
 	}
 
 	public static Map<String, String> getNationalities() {
 		return nationalities;
 	}
 
 }
