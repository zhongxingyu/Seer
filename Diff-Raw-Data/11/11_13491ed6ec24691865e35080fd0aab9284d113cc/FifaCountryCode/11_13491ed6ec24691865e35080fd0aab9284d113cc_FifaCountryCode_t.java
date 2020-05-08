 package dbs_fussball.model;
 
 /**
  * Contains a list of all current Fifa country codes as well as codes of former members
  * taken from this list http://www.rsssf.com/miscellaneous/fifa-codes.html
  *
 * @author Robert Böhnke
  *
  */
 public enum FifaCountryCode {
 	/* Africa */
 	
 	ALG("Algeria"),
 	ANG("Angola"),
 	BEN("Benin"),
 	BOT("Botswana"),
 	BFA("Burkina Faso"),
 	BDI("Burundi"),
 	CMR("Cameroon"),
 	CPV("Cape Verde Islands"),
 	CTA("Central African Republic"),
 	CHA("Chad"),
 	COM("Comoros Islands"),
 	CGO("Congo"),
 	COD("Congo DR (Zaire)"),
	CIV("Cote d’Ivoire"),
 	DJI("Djibouti"),
 	EGY("Egypt"),
 	EQG("Equatorial Guinea"),
 	ERI("Eritrea"),
 	ETH("Ethiopia"),
 	GAB("Gabon"),
 	GAM("Gambia"),
 	GHA("Ghana"),
 	GUI("Guinea"),
	GNB("Guinea – Bissau"),
 	KEN("Kenya"),
 	LES("Lesotho"),
 	LBR("Liberia"),
 	LBY("Libya"),
 	MAD("Madagascar"),
 	MWI("Malawi"),
 	MLI("Mali"),
 	MTN("Mauritania"),
 	MRI("Mauritius"),
 	MAR("Morocco"),
 	MOZ("Mozambique"),
 	NAM("Namibia"),
 	NIG("Niger"),
 	NGA("Nigeria"),
 	RWA("Rwanda"),
 	STP("Sao Tome e Principe"),
 	SEN("Senegal"),
 	SEY("Seychelles"),
 	SLE("Sierra Leone"),
 	SOM("Somalia"),
 	RSA("South Africa"),
 	SUD("Sudan"),
 	SWZ("Swaziland"),
 	TAN("Tanzania"),
 	TOG("Togo"),
 	TUN("Tunisia"),
 	UGA("Uganda"),
 	ZAM("Zambia"),
 	ZIM("Zimbabwe"),
 	
 	/* Asia */
 
 	AFG("Afghanistan"),
 	AUS("Australia"),
 	BHR("Bahrain"),
 	BAN("Bangladesh"),
 	BHU("Bhutan"),
 	BRU("Brunei Darussalam"),
 	CAM("Cambodia"),
 	CHN("China PR"),
 	TPE("Chinese Taipei"),
 	TLS("East Timor"),
 	GUM("Guam"),
 	HKG("Hong Kong"),
 	IND("India"),
 	IDN("Indonesia"),
 	IRN("Iran"),
 	IRQ("Iraq"),
 	JPN("Japan"),
 	JOR("Jordan"),
 	PRK("Korea DPR"),
 	KOR("Korea Republic"),
 	KUW("Kuwait"),
 	KGZ("Kyrgyzstan"),
 	LAO("Laos"),
 	LIB("Lebanon"),
 	MAC("Macao"),
 	MAS("Malaysia"),
 	MDV("Maldives"),
 	MGL("Mongolia"),
 	MYA("Myanmar"),
 	NEP("Nepal"),
 	OMA("Oman"),
 	PAK("Pakistan"),
 	PAL("Palestine"),
 	PHI("Philippines"),
 	QAT("Qatar"),
 	KSA("Saudi Arabia"),
 	SIN("Singapore"),
 	SRI("Sri Lanka"),
 	SYR("Syria"),
 	TJK("Tajikistan"),
 	THA("Thailand"),
 	TKM("Turkmenistan"),
 	UAE("United Arab Emirates"),
 	UZB("Uzbekistan"),
 	VIE("Vietnam"),
 	YEM("Yemen"),
 	
 	/* Europe */
 	
 	ALB("Albania"),
 	AND("Andorra"),
 	ARM("Armenia"),
 	AUT("Austria"),
 	AZE("Azerbaijan"),
 	BLR("Belarus"),
 	BEL("Belgium"),
 	BIH("Bosnia Herzegovina"),
 	BUL("Bulgaria"),
 	CRO("Croatia"),
 	CYP("Cyprus"),
 	CZE("Czech Republic"),
 	DEN("Denmark"),
 	ENG("England"),
 	EST("Estonia"),
 	FRO("Faeroe Islands"),
 	FIN("Finland"),
 	FRA("France"),
 	GEO("Georgia"),
 	GER("Germany"),
 	// Great Britain is no FIFA Member
 	GRE("Greece"),
 	NED("Holland"),
 	HUN("Hungary"),
 	ISL("Iceland"),
 	ISR("Israel"),
 	ITA("Italy"),
 	KAZ("Kazakhstan"),
 	LVA("Latvia"),
 	LIE("Liechtenstein"),
 	LTU("Lithuania"),
 	LUX("Luxembourg"),
 	MKD("Macedonia FYR"),
 	MLT("Malta"),
 	MDA("Moldova"),
 	// Monacco is no Fifa member
 	MNE("Montenegro"),
 	NIR("Northern Ireland"),
 	NOR("Norway"),
 	POL("Poland"),
 	POR("Portugal"),
 	IRL("Republic of Ireland"),
 	ROU("Romania"),
 	RUS("Russia"),
 	SMR("San Marino"),
 	SCO("Scotland"),
 	SRB("Serbia"),
 	SVK("Slovakia"),
 	SVN("Slovenia"),
 	ESP("Spain"),
 	SWE("Sweden"),
 	SUI("Switzerland"),
 	TUR("Turkey"),
 	UKR("Ukraine"),
 	WAL("Wales"),
 	// Vatican is no FIFA member
 	
 	/** North America */
 	
 	AIA("Anguilla"),
 	ATG("Antigua & Barbuda"),
 	ARU("Aruba"),
 	BAH("Bahamas"),
 	BRB("Barbados"),
 	BLZ("Belize"),
 	BER("Bermuda"),
 	VGB("British Virgin Islands"),
 	CAN("Canada"),
 	CAY("Cayman Islands"),
 	CRC("Costa Rica"),
 	CUB("Cuba"),
 	DMA("Dominica"),
 	DOM("Dominican Republic"),
 	SLV("El Salvador"),
 	GRN("Grenada"),
 	GUA("Guatemala"),
 	GUY("Guyana"),
 	HAI("Haiti"),
 	HON("Honduras"),
 	JAM("Jamaica"),
 	MEX("Mexico"),
 	MSR("Montserrat"),
 	ANT("Netherland Antilles"),
 	NCA("Nicaragua"),
 	PAN("Panama"),
 	PUR("Puerto Rico"),
 	SKN("St Kitts & Nevis"),
 	LCA("St Lucia"),
 	VIN("St Vincent & The Grenadines"),
 	SUR("Surinam"),
 	TRI("Trinidad & Tobago"),
 	TCA("Turks & Caicos Islands"),
 	USA("United States of America"),
 	VIR("United States Virgin Islands"),
 
 	/** Oceania */
 
 	ASA("American Samoa"),
 	// FIFA files Australia under Asia
 	COK("Cook Islands"),
 	FIJ("Fiji"),
 	// Guam is not a FIFA member
 	// Kiribati is not a FIFA member
 	// Marshall Islands is not a FIFA member
 	// Micronesia is not a FIFA member
 	// Nauru is not a FIFA member
 	NCL("New Caledonia"),
 	NZL("New Zealand"),
 	// Palau is not a FIFA member
 	PNG("Papua New Guinea"),
 	SAM("Samoa"),
 	SOL("Solomon Islands"),
 	TAH("Tahiti"),
 	TGA("Tonga"),
 	// Tuvalu is not a FIFA member
 	VAN("Vanuatu"),
 
 	/** South America */
 
 	ARG("Argentina"),
 	BOL("Bolivia"),
 	BRA("Brazil"),
 	CHI("Chile"),
 	COL("Colombia"),
 	ECU("Ecuador"),
 	PAR("Paraguay"),
 	PER("Peru"),
 	URU("Uruguay"),
 	VEN("Venezuela"),
 	
 	/* Old FIFA Codes
 	 * 
 	 * TODO Handle the relationship between these and the countries that followed them, i.e.
 	 * a player that played for the Soviet Union should be able to join the Russian team
 	 */
 	CIS("Commonwealth of Independent States"),
 	CUR("Curacao"),
 	TCH("Czechoslovakia"), 
 	DDR("East Germany"),
 	VNO("North Vietnam"),
 	NYE("North Yemen"),
 	SAA("Saarland"),
 	SCG("Serbia & Montenegro"),
 	VSO("South Vietnam"),
 	SYE("South Yemen"),
 	URS("Soviet Union"),
 	FRG("West Germany"),
 	YUG("Yugoslavia");
 		
 	/* Old Names of Countries / Independent States */
 	 
	// Let’s hope we don’t need those
 	 
 //	BOH("Bohemia (Later Czechoslovakia)"),
 //	BUR("Burma (Today Myanmar)"),
 //	CEY("Ceylon (Today Sri Lanka)"),
 //	CKN("Congo-Kinshasa (Later Zaire)"),
 //	COB("Congo-Brazzaville (Today Congo)"),
 //	TCH("Czechoslovakia * (See Below)"),
 //	DAH("Dahomey (Today Benin)"),
 //	DEI("Dutch Indies (Today Indonesia)"),
 //	HEB("New Hebrides (Today Vanuatu)"),
 //	PAL("Palestine (Today Israel/Jordan)"),
 //	RHO("Rhodesia (Today Zimbabwe)"),
 //	TAA("Tanganyika (Today part of Tanzania)"),
 //	TAI("Taiwan (Today Chinese Taipei)"),
 //	UAR("United Arab Republic"),
 //	UPV("Upper Volta (Today Burkina Faso)"),
 //	WSM("Western Samoa (Today Samoa)"),
 //	ZAI("Zaire (Today Congo DR)");
 
 	private String name;
 	
 	FifaCountryCode(String name) {
 		this.name = name;
 	}
 	
 	@Override
 	public String toString() {
 		return name;
 	}
 }
