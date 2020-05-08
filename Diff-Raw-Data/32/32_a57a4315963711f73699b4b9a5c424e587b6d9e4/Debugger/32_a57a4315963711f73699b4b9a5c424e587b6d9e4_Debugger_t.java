 package ch.almana.android.unibas.perssearch.helper;
 
 public class Debugger {
 
	public static boolean DEBUG = true;
 
 	public enum QueryType {
 		DEFAULT, LOCAL_DATA, FIXED_QUERY
 	};
 
 	public static final QueryType DUMMY_QUERY = QueryType.LOCAL_DATA;
 	public static final String QUERY_STRING = "patrick vogt";
 
 	public static final String JSON_STRING = "{\"registeredaddress\":\"\",\"unibaschhomefax\":\"061 666 66 66\","
 			+ "\"homepostaladdress\":\"Name der privaten Addresse$Privat Zusatz 1$Privat Zusatz 2$Privatestrasse 11$ch-4321 Basel\","
			+ "\"sn\":\"Insert\",\"postaladdress\":\"Test institut das einen wirklich langen Namen hat damit es umgebrochen wird$Abteilung Tests$Gruppe für Tests$Teststrasse 1$CH-4000 Basel\","
 			+ "\"dn\":\"uid=vogtpa, ou=people, ou=intern, ou=psearch, dc=unibas, dc=ch\",\"uid\":\"vogtpa\","
 			+ "\"facsimiletelephonenumber\":\"061 222 22 22\",\"unibaschsalutation\":\"M\",\"labeleduri\":\"http:\\/\\/example.com\\/mitEinerWirklichLangenErweiterungundeinifenparamteren\","
			+ "\"rating\":0.0,\"telephonenumber\":\"061 111 11 11\",\"cn\":\"Test User\",\"mail\":\"P.Vogt@unibas.ch\","
			+ "\"displayname\":\"Test User\",\"unibaschhomemail\":\"p.vogt@gmul.ch\",\"edupersonaffiliation\":\"\","
			+ "\"mailalternateaddress\":\"Pa.Vogt@unibas.ch\",\"givenname\":\"Test\",\"unibaschorgroledisplay\":\"\","
 			+ "\"employeetype\":\"Administration\",\"homephone\":\"061 555 55 55\",\"unibaschhomemobile\":\"079 777 77 77\","
 			+ "\"edupersonorgunitdn\":\"ou=urz,ou=units,ou=psearch,dc=unibas,dc=ch\"}";
 	public static final String JSON_STRING2 = " {\"registeredaddress\":\"\",\"unibaschhomefax\":\"\",\"homepostaladdress\":\"\",\"sn\":\"Vogt\",\n"
 			+ "	 \"postaladdress\":\"Universitätsrechenzentrum$Linux Gruppe$Mail Team$Klingelbergstrasse 70$CH-4056 Basel\",\n"
 			+ "	 \"dn\":\"uid=vogtp, ou=people, ou=intern, ou=psearch, dc=unibas, dc=ch\",\"uid\":\"vogtp\",\n"
 			+ "	 \"facsimiletelephonenumber\":\"+41 61 267 22 82\",\"unibaschsalutation\":\"M\",\"labeleduri\":\"\",\"rating\":1.0,\n"
 			+ "	 \"telephonenumber\":4.1612671522E10,\"cn\":\"Patrick Vogt\",\"mail\":\"Patrick.Vogt@unibas.ch\",\n"
 			+ "	 \"displayname\":\"Patrick Vogt\",\"unibaschhomemail\":\"\",\"edupersonaffiliation\":\"\",\"mailalternateaddress\":\"\",\n"
 			+ "	 \"givenname\":\"Patrick\",\"unibaschorgroledisplay\":\"\",\"employeetype\":\"\",\"homephone\":\"\",\n"
 			+ "	 \"unibaschhomemobile\":\"\",\"edupersonorgunitdn\":\"ou=urz,ou=units,ou=psearch,dc=unibas,dc=ch\"},";
 }
