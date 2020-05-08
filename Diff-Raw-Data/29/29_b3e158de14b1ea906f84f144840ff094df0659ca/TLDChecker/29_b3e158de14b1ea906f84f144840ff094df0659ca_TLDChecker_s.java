 package com.jwhois.util;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class TLDChecker {
 	private static final String			REGEX_SLD_INVALID_CHAR	= "[\\^\\$\\*\\+\\\\/_\\|\\?\"&%#':;=~`!@\\(\\)\\[\\]\\{\\}\\<\\>\\s]";
 	private static final String			REGEX_NOT_IDN			= "[a-zA-Z0-9](([a-zA-Z0-9-]+)?[a-zA-Z0-9])?";
 
 	// all Generic Top Level Domain Names (gTLD)
	private static final String			gTLDs					= "aero|asia|biz|cat|com|coop|edu|gov|info|int|jobs|mil|mobi|museum|name|net|org|pro|travel|tel|post|xxx|kid|mail";
 	// all Country-Code Top Level Domain Names (ccTLD)
	private static final String			ccTLDs					= "ac|ad|ae|af|ag|ai|al|am|an|ao|aq|ar|as|at|au|aw|ax|az|ba|bb|bd|be|bf|bg|bh|bi|bj|bm|bn|bo|br|bs|bt|bw|by|bz|ca|cc|cd|cf|cg|ch|ci|ck|cl|cm|cn|co|cr|cu|cv|cx|cy|cz|de|dj|dk|dm|do|dz|ec|ee|eg|er|es|et|eu|fi|fj|fk|fm|fo|fr|ga|gd|ge|gf|gg|gh|gi|gl|gm|gn|gp|gq|gr|gs|gt|gu|gw|gy|hk|hm|hn|hr|ht|hu|id|ie|il|im|in|io|iq|ir|is|it|je|jm|jo|jp|ke|kg|kh|ki|km|kn|kp|kr|kw|ky|kz|la|lb|lc|li|lk|lr|ls|lt|lu|lv|ly|ma|mc|md|me|mg|mh|mk|ml|mm|mn|mo|mp|mq|mr|ms|mt|mu|mv|mw|mx|my|mz|na|nc|ne|nf|ng|ni|nl|no|np|nr|nu|nz|om|pa|pe|pf|pg|ph|pk|pl|pn|pr|ps|pt|pw|py|qa|re|ro|rs|ru|rw|sa|sb|sc|sd|se|sg|sh|si|sk|sl|sm|sn|sr|st|su|sv|sy|sz|tc|td|tf|tg|th|tj|tk|tl|tm|tn|to|tr|tt|tv|tw|tz|ua|ug|uk|us|uy|uz|va|vc|ve|vg|vi|vn|vu|wf|ws|ye|za|zm|zw";
 	// all Country-Code Top Level Domain Names (ccTLD) - 2nd Level
	private static final String			ccSLDs					= "ac:com|net|gov|org|mil%ae:co|net|gov|ac|sch|o%rg|mil%af:com|edu|gov|net|org%al:com|edu|gov|mil|net|org%ao:ed|gv|og|co|pb|it%ar:com|edu|gob|gov|int|mil|net|org|tur%at:gv|ac|co|or%au:com|net|org|edu|gov|csiro|asn|id%ba:org|net|edu|gov|mil|unsa|untz|unmo|unbi|unze|co|com|rs%bb:co|com|net|org|gov|info|store|tv|biz%bh:com|info|cc|edu|biz|net|org|gov%bo:com|net|org|tv|mil|int|gob|gov|edu%br:adm|adv|agr|am|arq|art|ato|b|bio|blog|bmd|cim|cng|cnt|com|coop|ecn|edu|eng|esp|etc|eti|far|flog|fm|fnd|fot|fst|g12|ggf|gov|imb|ind|inf|jor|jus|lel|mat|med|mil|mus|net|nom|not|ntr|odo|org|ppg|pro|psc|psi|qsl|rec|slg|srv|tmp|trd|tur|tv|vet|vlog|wiki|zlg%bs:com|net|org|edu|gov%bw:co|org%bz:com|edu|gov|net|org%ca:ab|bc|mb|nb|nf|nl|ns|nt|nu|on|pe|qc|sk|yk%ck:co|org|edu|gov|net|gen|biz|info%cm:co|com|net%cn:ac|com|edu|gov|mil|net|org%co:com|org|edu|gov|net|mil|nom%cr:ac|co|ed|fi|go|or|sa%cy:ac|net|gov|org|pro|name|ekloges|tm|ltd|biz|press|parliament|com%dm:com|net|org%do:edu|gob|gov|com|sld|org|net|web|mil|art%dz:com|org|net|gov|edu|asso|pol|art%ec:com|info|net|fin|med|pro|org|edu|gov|mil%eg:com|edu|eun|gov|mil|name|net|org|sci%er:com|edu|gov|mil|net|org|ind|rochest|w%es:com|nom|org|gob|edu%et:com|gov|org|edu|net|biz|name|info%fj:ac|biz|com|info|mil|name|net|org|pro%fk:co|org|gov|ac|nom|net%fr:tm|asso|nom|prd|presse|com|gouv%ge:com|gov|net%gg:co|net|org%gh:com|edu|gov|org|mil%gn:com|ac|gov|org|net%gp:com|net|mobi|edu|asso|org%gr:com|edu|net|org|gov|mil%gt:com|edu|net|gob|org|mil|ind%gu:com|net|gov|org|edu%hk:com|edu|gov|idv|net|org%hr:com%hu:co|gov|edu%id:ac|co|net|or|web|sch|mil|go%il:ac|co|org|net|k12|gov|muni|idf%in:co|firm|net|org|gen|ind|ac|edu|res|ernet|gov|mil%iq:gov|edu|com|mil|org|net%ir:ac|co|gov|id|net|org|sch%it:gov|edu%je:co|net|org%jm:com|net|org|edu|gov|mil%jo:com|net|gov|edu|org|mil|name|sch%jp:ac|ad|co|ed|go|gr|lg|ne|or%ke:co|or|ne|go|ac|sc%kg:gov|mil%kh:per|com|edu|gov|mil|net|org%ki:com|biz|net|info|org|gov|edu|mob|tel%km:com|coop|asso|nom|presse|tm|medecin|notaires|pharmaciens|veterinaire|edu|gouv|mil%kn:net|org|edu|gov%kr:co|ne|or|re|pe|go|mil|ac|hs|ms|es|sc|kg%kw:edu|com|net|org|gov%ky:edu|com|net|org|gov%kz:org|edu|net|gov|mil|com%lb:com|edu|gov|net|org%lk:gov|sch|net|int|com|org|edu|ngo|soc|web|ltd|assn|grp|hotel%lr:com|org|gov|edu|net%lv:com|edu|gov|org|mil|id|net|asn|conf%ly:com|net|org|edu|gov|plc|sch|med|id%ma:net|ac|org|gov|press|co%mc:tm|asso%me:co|net|org|edu|ac|gov|its|priv%mg:org|nom|gov|prd|tm|edu|mil|com%mk:com|org|net|edu|gov|inf|name|pro%ml:com|net|org|edu|gov|presse%mm:net|com|edu|org|gov%mn:gov|edu|org%mo:com|edu|gov|org|net%mp:gov|org|co%mr:gov%mt:com|org|net|edu|gov%mu:com|net|org|gov|ac|co|or%mv:aero|biz|com|coop|edu|gov|info|int|mil|museum|name|net|org|pro%mw:ac|co|com|coop|edu|gov|int|museum|net|org%mx:com|net|org|edu|gob%my:com|net|org|gov|edu|sch|mil|name%mz:co|org|gov|edu%na:co|com%nc:asso%nf:com|net|arts|store|web|firm|info|other|per|rec%ng:com|org|gov|edu|net|sch|name|mobi|biz|mil%ni:gob|co|com|ac|edu|org|nom|net|mil%np:com|org|edu|net|gov|mil%nr:edu|gov|biz|info|net|org|com|co%nz:ac|co|geek|gen|maori|net|org|school|cri|govt|iwi|mil|parliament|health%om:com|co|edu|ac|sch|gov|net|org|mil|museum|biz|pro|med%pa:net|com|ac|sld|gob|edu|org|abo|ing|med|nom%pe:edu|gob|nom|mil|sld|org|com|net%pf:com%pg:com|net|ac|gov|mil|org%ph:com|net|org|mil|ngo|i|gov|edu%pk:com|net|edu|org|fam|biz|web|gov|gob|gok|gon|gop|gos%pl:com|biz|net|art|edu|org|ngo|gov|info|mil|waw|warszawa|wroc|wroclaw|krakow|katowice|poznan|lodz|gda|gdansk|slupsk|radom|szczecin|lublin|bialystok|olsztyn|torun|gorzow|zgora%pr:biz|com|edu|gov|info|isla|name|net|org|pro|est|prof|ac%ps:com|net|org|edu|gov|plo|sec%pt:com|edu|gov|int|net|nome|org|publ%pw:co|ne|or|ed|go|belau%py:org|edu|mil|gov|net|com%qa:com|net|org|gov|mil|edu%re:asso|nom|com%ro:arts|com|firm|info|nom|nt|org|rec|store|tm|www%rs:co|org|edu|ac|gov|in%ru:ac|com|edu|gov|int|mil|net|org|pp%rw:gov|net|edu|ac|com|co|int|mil|gouv%sa:com|edu|sch|med|gov|net|org|pub%sb:com|net|edu|org|gov%sc|com|net|edu|gov|org%sd:com|net|org|edu|med|tv|gov|info%se:org|pp|tm|parti|press%sg:com|net|org|gov|edu|per|idn%sh:co|com|org|gov|edu|net|nom%sl:com|net|org|edu|gov%sn:art|com|edu|gouv|org|person|univ%st:gov|saotome|principe|consulado|embaixada|org|edu|net|com|store|mil|co%sv:edu|gob|com|org|red%sy:edu|gov|net|mil|com|org|news%sz:co|ac|org%tf:eu|us|net|edu%th:ac|co|in|go|mi|or|net%tj:ac|biz|co|com|edu|go|gov|info|int|mil|name|net|nic|org|test|web%tn:com|ens|fin|gov|ind|intl|nat|net|org|info|perso|tourism|edunet|rnrt|rns|rnu|mincom|agrinet|defense%tr:com|gen|org|biz|info|av|dr|pol|bel|tsk|bbs|k12|edu|name|net|gov|web|tel|tv%tt:co|com|org|net|biz|info|pro|int|coop|jobs|mobi|travel|museum|aero|cat|tel|name|mil|edu|gov%tw:edu|gov|mil|com|net|org|idv|game|ebiz|club%tz:co|ac|go|or|ne%ua:com|edu|gov|net|org|co|biz|in|me|pp%ug:co|ac|sc|go|ne|or|org%uk:ac|co|gov|ltd|me|mod|net|nhs|nic|org|parliament|plc|police|sch|bl|icnet|jet|nls%uy:com|edu|gub|net|mil|org%uz:co|com|org%ve:com|edu|gob|mil|net|org|info|co|web%vi:co|org|com|net|k12|biz%vn:com|biz|edu|gov|net|org|int|ac|pro|info|health|name%ws:org|gov|edu%ye:com|co|ltd|me|net|org|plc|gov%za:ac|city|co|edu|gov|law|mil|nom|org|school|alt|net|ngo|tm|web%zm:ac|co|com|edu|gov|net|org|sch%zw:co|ac|org";
 
	private static final String			INTERNET_KEYWORDS		= "telnet|ftp|email|http|www|web|smtp|dns|wais|news|rfc|ietf|mbone|bbs|tcp";
 
	private static final String			NOT_ALLOW_INTKEY		= "gr|es";
 
 	private static Map<String, String>	ccSLDIdx;
 
 	static {
 		ccSLDIdx = new HashMap<String, String>();
 		String[] blks = ccSLDs.split( "%" );
 		for (String blk : blks) {
 			if (blk != null && !"".equals( blk )) {
 				String[] tlds = blk.split( "\\:" );
 				if (tlds.length == 2)
 					ccSLDIdx.put( tlds[0], tlds[1] );
 			}
 		}
 	}
 
 	public static boolean isgTLD(String tld) {
 		tld = tld.toLowerCase();
		return (gTLDs.indexOf( tld ) >= 0);
 	}
 
 	public static boolean isccTLD(String tld) {
 		tld = tld.toLowerCase();
		return (ccTLDs.indexOf( tld ) >= 0);
 	}
 
 	public static boolean isTLD(String tld) {
 		tld = tld.toLowerCase();
 		return (isgTLD( tld ) || isccTLD( tld ));
 	}
 
 	public static boolean hasSLD(String tld) {
 		tld = tld.toLowerCase();
 		return ccSLDIdx.containsKey( tld );
 	}
 
 	public static boolean isSLD(String sld) {
 		sld = sld.toLowerCase();
 		String[] tlds = sld.split( "\\." );
 		if (tlds.length == 2 && hasSLD( tlds[1] )) {
			return (ccSLDIdx.get( tlds[1] ).indexOf( tlds[0] ) >= 0);
 		}
 		return false;
 	}
 
 	public static boolean isInternetKeywords(String test) {
 		test = test.toLowerCase();
		return (INTERNET_KEYWORDS.indexOf( test ) >= 0);
 	}
 
 	public static boolean isAllowInternetKeywords(String tld) {
 		tld = tld.toLowerCase();
		return !(NOT_ALLOW_INTKEY.indexOf( tld ) >= 0);
 	}
 
 	public static boolean isInvalidRegisterWords(String words, String tld) {
 		return (isInternetKeywords( words ) && !isAllowInternetKeywords( tld ));
 	}
 
 	public static boolean isIDN(String words) {
 		words = words.toLowerCase();
 		return !words.matches( REGEX_NOT_IDN );
 	}
 
 	public static String cleanDomainName(String domainName) {
 		String dom = "";
 		String s = domainName.toLowerCase();
 		s = s.replaceAll( REGEX_SLD_INVALID_CHAR, "" );
 
 		String sld = "";
 		String tld = "";
 		int pos = 0;
 
 		int dotnum = s.split( "\\." ).length - 1;
 		if (dotnum >= 2) {
 			pos = s.lastIndexOf( ".", s.lastIndexOf( "." ) - 1 );
 			tld = s.substring( pos + 1 );
 			sld = s.substring( s.lastIndexOf( ".", pos - 1 ) + 1, pos );
 			if (isSLD( tld ) && !isInvalidRegisterWords( sld, tld )) {
 				dom = sld + "." + tld;
 			}
 			else {
 				pos = s.lastIndexOf( "." );
 				tld = s.substring( pos + 1 );
 				sld = s.substring( s.lastIndexOf( ".", pos - 1 ) + 1, pos );
 				if (isTLD( tld ) && !isInvalidRegisterWords( sld, tld )) {
 					dom = sld + "." + tld;
 				}
 			}
 		}
 		else if (dotnum == 1 && !isSLD( s )) {
 			sld = s.split( "\\." )[0];
 			tld = s.split( "\\." )[1];
 			if (isTLD( tld ) && !isInvalidRegisterWords( sld, tld )) {
 				dom = s;
 			}
 		}
 
 		return dom;
 	}
 
 }
