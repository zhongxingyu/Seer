 package com.cnnic.whois.util;
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public class WhoisUtil {
 	public static final String BLANKSPACE = "    ";
 	
 	public static final String IP = "ip";
 	public static final String DMOAIN = "domain";
 	public static final String ENTITY = "entity";
 	public static final String AUTUM = "autnum";
 	public static final String NAMESERVER = "nameserver";
 
 	public static final String DELEGATIONKEYS = "delegationkeys";
 	public static final String DNRDOMAIN = "dnrdomain";
 	public static final String DNRENTITY = "dnrentity";
 	public static final String LINK = "link";
 	public static final String LINKS = "links";
 	public static final String AUTNUM = "autnum";
 	public static final String PHONES = "phones";
 	public static final String POSTALASSDESS = "postaladdress";
 	public static final String RIRDOMAIN = "rirdomain";
 	public static final String RIRENTITY = "rirentity";
 	public static final String VARIANTS = "variants";
 
 	public static final String NOTICES = "notices";
 	public static final String REGISTRAR = "registrar";
 	public static final String SPONSOREDBY = "SponsoredBy";
 	public static final String REMARKS = "remarks";
 	public static final String EVENTS = "events";
 	public static final String ENTITIES = "entities";
 
 	public static final String PRX = "/";
 	public static final String JNDI_NAME = "java:comp/env/jdbc/DataSource";
 
 	public static final String SELECT_LIST_IPv4_1 = "select * from ip where StartLowAddress <=";
 	public static final String SELECT_LIST_IPv4_2 = " and ";
 	public static final String SELECT_LIST_IPv4_3 = " <= EndLowAddress";
 
 	public static final String SELECT_LIST_IPv6_1 = "select * from ip where (StartHighAddress <";
 	public static final String SELECT_LIST_IPv6_2 = " or (StartHighAddress = ";
 	public static final String SELECT_LIST_IPv6_3 = " && StartLowAddress <= ";
 	public static final String SELECT_LIST_IPv6_4 = " )) and (EndHighAddress >";
 	public static final String SELECT_LIST_IPv6_5 = " or (EndhighAddress = ";
 	public static final String SELECT_LIST_IPv6_6 = " and EndLowAddress >= ";
 	public static final String SELECT_LIST_IPv6_7 = " )) ";
 
 	public static final String SELECT_LIST_IPv6_8 = "select * ,EndHighAddress - StartHighAddress as high,"
 			+ "EndLowAddress - StartLowAddress as low  from ip where (StartHighAddress <";
 	public static final String SELECT_LIST_IPv6_9 = "order by high ,low  limit 1";
 
 	public static final String SELECT_LIST_IPv4_4 = "select * ,StartLowAddress-EndLowAddress as se from ip where StartLowAddress <=";
 	public static final String SELECT_LIST_IPv4_5 = "<= EndLowAddress order by se limit 1";
 
 	public static final String SELECT_LIST_RIRENTITY = "select * from RIREntity where Handle=";
 	public static final String SELECT_LIST_DNRENTITY = "select * from DNREntity where Handle=";
 	public static final String SELECT_LIST_RIRDOMAIN = "select * from RIRDomain where LdhName=";
 	public static final String SELECT_LIST_DNRDOMAIN = "select * from DNRDomain where LdhName=";
 	public static final String SELECT_LIST_NAMESREVER = "select * from nameServer where LdhName=";
 
 	public static final String SELECT_LIST_AS1 = "select * from autnum where Start_Autnum <=";
 	public static final String SELECT_LIST_AS2 = " and ";
 	public static final String SELECT_LIST_AS3 = "<= End_Autnum ";
 
 	public static final String SELECT_JOIN_LIST_LINK = "select * from link join m2m_link on m2m_link.linkId= link.linkId and m2m_link.Handle=";
 	public static final String SELECT_JOIN_LIST_JOINRIRENTITY = "select * from RIREntity join m2m_rirentity on m2m_rirentity.rirentityHandle=RIREntity.Handle and  m2m_rirentity.Handle=";
 	public static final String SELECT_JOIN_LIST_PHONE = "select * from phones join m2m_phones on m2m_phones.phoneId=phones.phonesId and m2m_phones.Handle=";
 	public static final String SELECT_JOIN_LIST_POSTALADDRESS = "select * from postaladdress join m2m_postaladress on m2m_postaladress.postalAddressId=postalAddress.postalAddressId and m2m_postaladress.Handle=";
 	public static final String SELECT_JOIN_LIST_DELEGATIONKEYS = "select * from delegationKeys join m2m_delegationKeys on m2m_delegationKeys.delegationKeyId= delegationKeys.delegationKeysId and  m2m_delegationKeys.Handle=";
 	public static final String SELECT_JOIN_LIST_NOTICES = "select * from notices join m2m_notices on m2m_notices.noticesId=notices.noticesId and  m2m_notices.Handle=";
 	public static final String SELECT_JOIN_LIST_REMARKS = "select * from remarks join m2m_remarks on m2m_remarks.remarksId=remarks.remarksId and  m2m_remarks.Handle=";
 	public static final String SELECT_JOIN_LIST_EVENTS = "select * from events join m2m_events on m2m_events.eventsId=events.eventsId and  m2m_events.Handle=";
 
 	public static final String SELECT_JOIN_LIST_REGISTRAR = "select * from registrar join m2m_registrar on m2m_registrar.registrarHandle=registrar.Handle and  m2m_registrar.Handle=";
 	public static final String SELECT_JOIN_LIST_JOINNAMESERVER = "select * from nameserver join m2m_nameserver on m2m_nameserver.nameserverHandle=nameserver.Handle and m2m_nameserver.Handle=";
 	public static final String SELECT_JOIN_LIST_VARIANTS = "select * from variants join m2m_variants on m2m_variants.variantsId=variants.variantsId and m2m_variants.Handle=";
 	public static final String SELECT_JOIN_LIST_JOINDNRENTITY = "select * from dnrentity join m2m_dnrentity on m2m_dnrentity.dnrentityHandle=dnrentity.Handle and m2m_dnrentity.Handle=";
 
 	public static final String SELECT_LIST_LINK = "select * from link where linkId=";
 	public static final String SELECT_LIST_PHONE = "select * from phones where phonesId=";
 	public static final String SELECT_LIST_POSTALADDRESS = "select * from postaladdress where postalAddressId=";
 	public static final String SELECT_LIST_VARIANTS = "select * from variants where variantsId=";
 	public static final String SELECT_LIST_DELEGATIONKEYS = "select * from delegationKeys where delegationKeyId=";
 	public static final String SELECT_LIST_NOTICES = "select * from notices where noticesId=";
 	public static final String SELECT_LIST_JOIN_REGISTRAR = "select * from registrar where Handle=";
 	public static final String SELECT_LIST_REGISTRAR = "select * from registrar where Entity_Names=";
 	
 	public static final String SELECT_LIST_REMARKS = "select * from remarks where remarksId=";
 	public static final String SELECT_LIST_EVENTS = "select * from events where eventsId=";
 
 	public static final String ARRAYFILEDPRX = "$array$";
 	public static final String JOINFILEDPRX = "$join$";
 	public static final String EXTENDPRX = "$x$";
 	public static final String MULTIPRX = "$mul$";
 	
 	public static final String MULTIPRXIP = "$mul$" + "IP";
 	public static final String MULTIPRXDMOAIN = "$mul$" + DMOAIN;
 	public static final String MULTIPRXENTITY = "$mul$" + ENTITY;
 	public static final String MULTIPRXAUTNUM = "$mul$" + AUTNUM;
 	public static final String MULTIPRXNAMESERVER = "$mul$" +"nameServer";
 	public static final String MULTIPRXLINK = "$mul$" + LINK;
 	public static final String MULTIPRXPHONES = "$mul$" + PHONES;
 	public static final String MULTIPRXPOSTALASSDESS = "$mul$" + "postalAddress";
 	public static final String MULTIPRXVARIANTS = "$mul$" + VARIANTS;
 	public static final String MULTIPRXDELEGATIONKEY = "$mul$" +"delegationKeys";
 	public static final String MULTIPRXNOTICES = "$mul$" + "notices";
 	public static final String MULTIPRXREGISTRAR = "$mul$" + "registrar";
 	public static final String MULTIPRXREMARKS = "$mul$" + REMARKS;
 	public static final String MULTIPRXEVENTS = "$mul$" + VARIANTS;
 	
 	public static final String JOINENTITESFILED = JOINFILEDPRX + "entities";
 	public static final String JOINLINKFILED = JOINFILEDPRX + "links";
 	public static final String JOINPHONFILED = JOINFILEDPRX + "phones";
 	public static final String JOINPOSTATLADDRESSFILED = JOINFILEDPRX
 			+ "postalAddress";
 	public static final String JOINVARIANTS = JOINFILEDPRX + "variants";
 	public static final String JOINNAMESERVER = JOINFILEDPRX + "nameServer";
 	public static final String JOINNAREGISTRAR = JOINFILEDPRX + "registrar";
 	public static final String JOINNANOTICES = JOINFILEDPRX + "notices";
 	public static final String JOINREMARKS = JOINFILEDPRX + "remarks";
 	public static final String JOINEVENTS = JOINFILEDPRX + "events";
 
 	public static final String JOINDALEGATIONKEYS = JOINFILEDPRX
 			+ "delegationKeys";
 	public static final String VALUEARRAYPRX = "'~'";
 	public static final String HANDLE = "Handle";
 	public static final String LINEBREAK = "<br/>";
 	public static final String IFNULL = null;
 	public static final String PERMISSIONPRX = "~";
 
 	public static String[] IPKeyFileds = { JOINNANOTICES, "Handle", "StartHighAddress",
 			"StartLowAddress", "EndLowAddress", "EndHighAddress", "Lang",
 			"IP_Version", "Name", //ARRAYFILEDPRX + "Description",
 			"Type", "Country", "Parent_Handle", JOINREMARKS,
 			ARRAYFILEDPRX + "Status", JOINEVENTS, JOINENTITESFILED,
 			JOINLINKFILED };
 
 	public static String[] DNREntityKeyFileds = { "Handle",
 			ARRAYFILEDPRX + "Entity_Names", "Lang", JOINNANOTICES,
 			ARRAYFILEDPRX + "Status", ARRAYFILEDPRX + "Roles", JOINPOSTATLADDRESSFILED,
 			ARRAYFILEDPRX + "Emails", JOINPHONFILED, JOINREMARKS,
 			JOINLINKFILED, "Port43", JOINEVENTS, JOINNAREGISTRAR };
 
 	public static String[] RIREntityKeyFileds = { "Handle",
 			ARRAYFILEDPRX + "Entity_Names", "Lang", JOINNANOTICES,
 			ARRAYFILEDPRX + "Roles", JOINPOSTATLADDRESSFILED,
 			ARRAYFILEDPRX + "Emails", JOINPHONFILED, JOINREMARKS,
 			JOINLINKFILED, JOINEVENTS };
 
 	public static String[] DNRDomainKeyFileds = { "Handle", "LdhName",
 			"UnicodeName", "Lang", JOINNANOTICES, JOINVARIANTS,
 			ARRAYFILEDPRX + "Status", JOINNAMESERVER, JOINDALEGATIONKEYS,
 			"Port43", JOINEVENTS, JOINENTITESFILED, JOINNAREGISTRAR,
 			JOINLINKFILED, JOINREMARKS };
 
 	public static String[] RIRDomainKeyFileds = { "Handle", "LdhName", "Lang",
 			JOINNANOTICES, JOINNAMESERVER, JOINDALEGATIONKEYS, JOINREMARKS,
 			JOINLINKFILED, JOINEVENTS, JOINENTITESFILED };
 
 	public static String[] ASKeyFileds = { "Handle", "Start_Autnum",
 			"End_Autnum", "Name", "Lang", JOINNANOTICES,
 			//ARRAYFILEDPRX + "Description", 
 			"Type", "Country", JOINREMARKS,
 			JOINLINKFILED, JOINEVENTS, JOINENTITESFILED };
 
 	public static String[] nameServerKeyFileds = { "Handle", "LdhName",
 			"UnicodeName", "Lang", JOINNANOTICES, ARRAYFILEDPRX + "Status",
 			ARRAYFILEDPRX + "IPV4_Addresses", ARRAYFILEDPRX + "IPV6_Addresses",
 			"Port43", JOINREMARKS, JOINLINKFILED, JOINEVENTS, JOINENTITESFILED,
 			JOINNAREGISTRAR };
 
 	public static String[] phonesKeyFileds = { ARRAYFILEDPRX + "Office",
 			ARRAYFILEDPRX + "Fax", ARRAYFILEDPRX + "Mobile", "phonesId" };
 
 	public static String[] variantsKeyFileds = { ARRAYFILEDPRX + "Relation",
 			ARRAYFILEDPRX + "VariantNames", "variantsId" };
 
 	public static String[] linkKeyFileds = { "Value", "Rel", "Href", "linkId",
 			"$array$hreflang", "$array$title", "media", "type" };
 
 	public static String[] postalAddressKeyFileds = { "Street", "Street1",
 			"Street2", "City", "SP", "Postal_Code", "County_Code",
 			"postalAddressId" };
 	public static String[] delegationKeyFileds = { "Algorithm", "Digest",
 			"Disgest_Type", "Key_Tag", "delegationKeysId" };
 
 	public static String[] registrarKeyFileds = { "Handle",
 			"Entity_Names", ARRAYFILEDPRX + "Status", "Roles",
 			JOINPOSTATLADDRESSFILED, ARRAYFILEDPRX + "Emails", JOINPHONFILED,
 			JOINREMARKS, JOINLINKFILED, "Port43", JOINEVENTS };
 
 	public static String[] noticesKeyFileds = { "Title",
 			ARRAYFILEDPRX + "Description", "noticesId", JOINLINKFILED };
 
 	public static String[] remarksKeyFileds = { "Title",
 			ARRAYFILEDPRX + "Description", "remarksId", JOINLINKFILED };
 
 	public static String[] eventsKeyFileds = { "EventAction", "EventActor",
 			"EventDate", "eventsId" };
 
 	public static String[] queryTypes = { "autnum", "delegationKeys", "domain",
 			"entity", "events", "ip", "links", "nameserver", "notices",
 			"phones", "postalAddress", "registrar", "remarks", "variants", "help" };
 
 	public static String[] extendColumnTableTypes = { "autnum",
 			"delegationkeys", "dnrdomain", "dnrentity", "events", "ip", "link",
 			"nameserver", "notices", "phones", "postaladdress", "registrar",
 			"remarks", "rirdomain", "rirentity", "variants" };
 
 	public static String[][] keyFiledsSet = { ASKeyFileds, delegationKeyFileds,
 			DNRDomainKeyFileds, DNREntityKeyFileds, eventsKeyFileds,
 			IPKeyFileds, linkKeyFileds, nameServerKeyFileds, noticesKeyFileds,
 			phonesKeyFileds, postalAddressKeyFileds, registrarKeyFileds,
 			remarksKeyFileds, RIRDomainKeyFileds, RIREntityKeyFileds,
 			variantsKeyFileds };
 
 	public static long[] IPV4Array = { 0x80000000l, // 1000 0000 0000 0000 0000
 													// 0000 0000 0000,//1
 			0xC0000000l, // 1100 0000 0000 0000 0000 0000 0000 0000,//2
 			0xE0000000l, // 1110 0000 0000 0000 0000 0000 0000 0000,//3
 			0xF0000000l, // 1111 0000 0000 0000 0000 0000 0000 0000,//4
 			0xF8000000l, // 1111 1000 0000 0000 0000 0000 0000 0000,//5
 			0xFC000000l, // 1111 1100 0000 0000 0000 0000 0000 0000,//6
 			0xFE000000l, // 1111 1110 0000 0000 0000 0000 0000 0000,//7
 			0xFF000000l, // 1111 1111 0000 0000 0000 0000 0000 0000,//8
 			0xFF800000l, // 1111 1111 1000 0000 0000 0000 0000 0000,//9
 			0xFFC00000l, // 1111 1111 1100 0000 0000 0000 0000 0000,//10
 			0xFFE00000l, // 1111 1111 1110 0000 0000 0000 0000 0000,//11
 			0xFFF00000l, // 1111 1111 1111 0000 0000 0000 0000 0000,//12
 			0xFFF80000l, // 1111 1111 1111 1000 0000 0000 0000 0000,//13
 			0xFFFC0000l, // 1111 1111 1111 1100 0000 0000 0000 0000,//14
 			0xFFFE0000l, // 1111 1111 1111 1110 0000 0000 0000 0000,//15
 			0xFFFF0000l, // 1111 1111 1111 1111 0000 0000 0000 0000,//16
 			0xFFFF8000l, // 1111 1111 1111 1111 1000 0000 0000 0000,//17
 			0xFFFFC000l, // 1111 1111 1111 1111 1100 0000 0000 0000,//18
 			0xFFFFE000l, // 1111 1111 1111 1111 1110 0000 0000 0000,//19
 			0xFFFFF000l, // 1111 1111 1111 1111 1111 0000 0000 0000,//20
 			0xFFFFF800l, // 1111 1111 1111 1111 1111 1000 0000 0000,//21
 			0xFFFFFC00l, // 1111 1111 1111 1111 1111 1100 0000 0000,//22
 			0xFFFFFE00l, // 1111 1111 1111 1111 1111 1110 0000 0000,//23
 			0xFFFFFF00l, // 1111 1111 1111 1111 1111 1111 0000 0000,//24
 			0xFFFFFF80l, // 1111 1111 1111 1111 1111 1111 1000 0000,//25
 			0xFFFFFFC0l, // 1111 1111 1111 1111 1111 1111 1100 0000,//26
 			0xFFFFFFE0l, // 1111 1111 1111 1111 1111 1111 1110 0000,//27
 			0xFFFFFFF0l, // 1111 1111 1111 1111 1111 1111 1111 0000,//28
 			0xFFFFFFF8l, // 1111 1111 1111 1111 1111 1111 1111 1000//29
 			0xFFFFFFFCl, // 1111 1111 1111 1111 1111 1111 1111 1100//30
 			0xFFFFFFFEl, // 1111 1111 1111 1111 1111 1111 1111 1110//31
 			0xFFFFFFFFl // 1111 1111 1111 1111 1111 1111 1111 1111//32
 	};
 
 	public static long[] IPV6Array = { 0x8000000000000000l, // 1
 			0xC000000000000000l, // 2
 			0xE000000000000000l, // 3
 			0xF000000000000000l, // 4
 			0xF800000000000000l, // 5
 			0xFC00000000000000l, // 6
 			0xFE00000000000000l, // 7
 			0xFF00000000000000l, // 8
 			0xFF80000000000000l, // 9
 			0xFFC0000000000000l, // 10
 			0xFFE0000000000000l, // 11
 			0xFFF0000000000000l, // 12
 			0xFFF8000000000000l, // 13
 			0xFFFC000000000000l, // 14
 			0xFFFE000000000000l, // 15
 			0xFFFF000000000000l, // 16
 			0xFFFF800000000000l, // 17
 			0xFFFFC00000000000l, // 18
 			0xFFFFE00000000000l, // 19
 			0xFFFFF00000000000l, // 20
 			0xFFFFF80000000000l, // 21
 			0xFFFFFC0000000000l, // 22
 			0xFFFFFE0000000000l, // 23
 			0xFFFFFF0000000000l, // 24
 			0xFFFFFF8000000000l, // 25
 			0xFFFFFFC000000000l, // 26
 			0xFFFFFFE000000000l, // 27
 			0xFFFFFFF000000000l, // 28
 			0xFFFFFFF800000000l, // 29
 			0xFFFFFFFC00000000l, // 30
 			0xFFFFFFFE00000000l, // 31
 			0xFFFFFFFF00000000l, // 32
 			0xFFFFFFFF80000000l, // 33
 			0xFFFFFFFFC0000000l, // 34
 			0xFFFFFFFFE0000000l, // 35
 			0xFFFFFFFFF0000000l, // 36
 			0xFFFFFFFFF8000000l, // 37
 			0xFFFFFFFFFC000000l, // 38
 			0xFFFFFFFFFE000000l, // 39
 			0xFFFFFFFFFF000000l, // 40
 			0xFFFFFFFFFF800000l, // 41
 			0xFFFFFFFFFFC00000l, // 42
 			0xFFFFFFFFFFE00000l, // 43
 			0xFFFFFFFFFFF00000l, // 44
 			0xFFFFFFFFFFF80000l, // 45
 			0xFFFFFFFFFFFC0000l, // 46
 			0xFFFFFFFFFFFE0000l, // 47
 			0xFFFFFFFFFFFF0000l, // 48
 			0xFFFFFFFFFFFF8000l, // 49
 			0xFFFFFFFFFFFFC000l, // 50
 			0xFFFFFFFFFFFFE000l, // 51
 			0xFFFFFFFFFFFFF000l, // 52
 			0xFFFFFFFFFFFFF800l, // 53
 			0xFFFFFFFFFFFFFC00l, // 54
 			0xFFFFFFFFFFFFFE00l, // 55
 			0xFFFFFFFFFFFFFF00l, // 56
 			0xFFFFFFFFFFFFFF80l, // 57
 			0xFFFFFFFFFFFFFFC0l, // 58
 			0xFFFFFFFFFFFFFFE0l, // 59
 			0xFFFFFFFFFFFFFFF0l, // 60
 			0xFFFFFFFFFFFFFFF8l, // 61
 			0xFFFFFFFFFFFFFFFCl, // 62
 			0xFFFFFFFFFFFFFFFEl, // 63
 			0xFFFFFFFFFFFFFFFFl // 64
 	};
 
 	public static final String ERRORCODE = "4143";
 	public static final String ERRORTITLE = "Eror Message";
	public static final String [] ERRORDESCRIPTION = {"NO_RESULT"};
 
 	public static final String COMMENDRRORCODE = "4144";
 	public static final String OMMENDERRORTITLE = "Eror Message";
	public static final String [] OMMENDERRORDESCRIPTION = {"COMMAND_SYNTAX_ERROR"};
 
 	public static final String UNCOMMENDRRORCODE = "4145";
 	public static final String UNOMMENDERRORTITLE = "Eror Message";
	public static final String [] UNOMMENDERRORDESCRIPTION = {"UNKNOWN_COMMAND"};
 
 	public static final String ADDCOLUMN1 = "alter table ";
 	public static final String ADDCOLUMN2 = " add column ";
 	public static final String ADDCOLUMN3 = " varchar(";
 	public static final String ADDCOLUMN4 = ")  default '' ";
 
 	public static final String PERMISSION = "permissions";
 	public static final String ADDCOLUMNNAMEPERMISSION1 = "insert into permissions (tableName,columnName,columnLength) values (";
 	public static final String ADDCOLUMNNAMEPERMISSION2 = "',";
 	public static final String ADDCOLUMNNAMEPERMISSION3 = ")";
 	public static final String POINT = "'";
 
 	public static final String LIST_COLUMNINFO = "select columnName,columnLength from permissions where tableName=? and columnName like '$x$%'";
 	public static final String SELECT_LIST_COLUMNNAME = "select tableName,columnName from permissions where tableName=? and columnName like '$x$%'";
 
 	public static final String UPDATE_COLUMNINFO1 = "alter table ";
 	public static final String UPDATE_COLUMNINFO2 = " change ";
 
 	public static final String UPDATE_COLUMNINFOERMISSION = "update permissions set columnName=? ,columnLength=? where tableName=? and columnName=?";
 
 	public static final String DELETE_COLUMNINFO1 = "alter table ";
 	public static final String DELETE_COLUMNINFO2 = " drop column ";
 
 	public static final String DELETE_COLUMNINFOERMISSION1 = "delete from permissions  where columnName='";
 	public static final String DELETE_COLUMNINFOERMISSION2 = "' and tableName='";
 
 	public static final String COLUMNNAME = "columnName";
 	public static final String COLUMNLENGTH = "columnLength";
 	public static final String ANONYMOUS = "anonymous";
 	public static final String AUTHENTICATED = "authenticated";
 	public static final String ROOT = "root";
 
 	public static final String SELECT_COLUMNINFOERMISSION = "select * from permissions where tableName=?";
 	public static final String UPDATE_PERMISSION = "update permissions set anonymous=?,authenticated=?,root=? where tableName=? and columnName=?  ";
 
 	public static final String SELECT_REDIRECT = "select * from ";
 	public static final String DELETE_REDIRECT1 = "delete from ";
 	public static final String DELETE_REDIRECT2 = " where id=?";
 
 	public static final String UPDATE_DOMAIN_REDIRECTION = "update domain_redirect set redirectType=";
 
 	public static final String UPDATE_REDIRECTION1 = " ,redirectURL=";
 	public static final String UPDATE_REDIRECTION2 = " where id=";
 
 	public static final String UPDATE_AUTNUM_REDIRECTION1 = " update autnum_redirect set startNumber= ";
 	public static final String UPDATE_AUTNUM_REDIRECTION2 = " ,endNumber= ";
 
 	public static final String UPDATE_IP_REDIRECTION1 = " update ip_redirect set StartHighAddress= ";
 	public static final String UPDATE_IP_REDIRECTION2 = " ,EndHighAddress= ";
 	public static final String UPDATE_IP_REDIRECTION3 = " ,StartLowAddress= ";
 	public static final String UPDATE_IP_REDIRECTION4 = " ,EndLowAddress= ";
 
 	public static final String INSERT_DOMAIN_REDIRECTION = "insert into domain_redirect (redirectType,redirectURL) values(";
 	public static final String INSERT_IP_REDIRECTION = "insert into ip_redirect (StartHighAddress,EndHighAddress,StartLowAddress,EndLowAddress,redirectURL) values(";
 	public static final String INSERT_AUTNUM_REDIRECTION = "insert into autnum_redirect (startNumber,endNumber,redirectURL) values(";
 
 	public static final String ISNULL_DOMAIN_REDIRECT = "select * from domain_redirect where redirectType=? and redirectURL=?";
 
 	public static final String ISNULL_IP_REDIRECT = "select * from ip_redirect where StartHighAddress=? and EndHighAddress=?"
 			+ " and StartLowAddress=? and StartLowAddress=? and redirectURL=?";
 
 	public static final String ISNULL_AUTNUM_REDIRECT = "select * from autnum_redirect where  startNumber=? and endNumber=? and redirectURL=? ";
 
 	public static final String INSERT_IP_REDIRECT1 = "insert into ";
 	public static final String INSERT_IP_REDIRECT2 = " (startNumber,endNumber,redirectURL) values(";
 
 	public static final String SELECT_URL_DOAMIN_REDIRECTION = "select redirectURL from domain_redirect where redirectType=";
 
 	public static final String SELECT_URL_AUTNUM_EDIRECTION1 = "select redirectURL from autnum_redirect where startNumber <=";
 	public static final String SELECT_URL_AUTNUM_EDIRECTION2 = " and endNumber >= ";
 
 	public static final String SELECT_URL_IPV4_REDIRECTION1 = "select redirectURL from ip_redirect where StartLowAddress <=";
 	public static final String SELECT_URL_IPV4_REDIRECTION2 = " and ";
 	public static final String SELECT_URL_IPV4_REDIRECTION3 = " <= EndLowAddress";
 
 	public static final String SELECT_URL_IPV6_REDIRECTION1 = "select redirectURL from ip_redirect where (StartHighAddress <";
 	public static final String SELECT_URL_IPV6_REDIRECTION2 = " or (StartHighAddress = ";
 	public static final String SELECT_URL_IPV6_REDIRECTION3 = " && StartLowAddress <= ";
 	public static final String SELECT_URL_IPV6_REDIRECTION4 = " )) and (EndHighAddress >";
 	public static final String SELECT_URL_IPV6_REDIRECTION5 = " or (EndhighAddress = ";
 	public static final String SELECT_URL_IPV6_REDIRECTION6 = " and EndLowAddress >= ";
 	public static final String SELECT_URL_IPV6_REDIRECTION7 = " )) ";
 
 	public static final String SELECT_PERMISSION = "select * from permissions where tableName = ?";
 
 	public static final String SELECT_PERMISSION_ISNULL = "select columnName from permissions where tableName = ? and columnName=?";
 
 	public static final Map<String, Long> queryRemoteIPMap = new HashMap<String, Long>();
 
 	public static long[] parsingIp(String ipInfo, int ipLength) {
 		long startHighAddr = 0, endHighAddr = 0, startLowAddr = 0, endLowAddr = 0;
 		long[] ipInfoLong = WhoisUtil.iptolong(ipInfo);
 		if (ipLength != 0) {
 			if (ipInfo.indexOf(":") != -1) {
 				if (ipLength < 65) {
 					startHighAddr = ipInfoLong[0]
 							& WhoisUtil.IPV6Array[ipLength];
 					startLowAddr = ipInfoLong[1];
 
 					long inversion = (~WhoisUtil.IPV6Array[ipLength]) & 0xFFFFFFFFFFFFFFFFL;
 					endHighAddr = ipInfoLong[0] | inversion;
 					endLowAddr = startLowAddr;
 				} else {
 					startHighAddr = ipInfoLong[0];
 					startLowAddr = ipInfoLong[1]
 							& WhoisUtil.IPV6Array[ipLength];
 
 					long inversion = (~WhoisUtil.IPV6Array[ipLength]) & 0xFFFFFFFFFFFFFFFFL;
 					endLowAddr = ipInfoLong[0] | inversion;
 					endHighAddr = startHighAddr;
 				}
 
 			} else {
 
 				startLowAddr = ipInfoLong[0] & WhoisUtil.IPV4Array[ipLength];
 				long inversion = (~WhoisUtil.IPV4Array[ipLength]) & 0xFFFF;
 				endLowAddr = ipInfoLong[0] | inversion;
 			}
 
 		} else {
 			if (ipInfo.indexOf(":") != -1) {
 				startHighAddr = ipInfoLong[0];
 				startLowAddr = ipInfoLong[1];
 			} else {
 				startLowAddr = ipInfoLong[0];
 			}
 		}
 
 		long[] iplongs = { startHighAddr, endHighAddr, startLowAddr, endLowAddr };
 		return iplongs;
 	}
 
 	public static long[] parsingIp(String startAddress, String endAddress,
 			int startIpLength, int endIpLength) {
 		long startHighAddr = 0, endHighAddr = 0, startLowAddr = 0, endLowAddr = 0;
 		long[] ipStartInfoLong = WhoisUtil.iptolong(startAddress);
 		long[] ipEndInfoLong = WhoisUtil.iptolong(endAddress);
 
 		if (startIpLength != 0 && endIpLength != 0) {
 			if (startAddress.indexOf(":") != -1
 					&& endAddress.indexOf(":") != -1) {
 				if (endIpLength < 65) {
 					startHighAddr = ipStartInfoLong[0]
 							& WhoisUtil.IPV6Array[startIpLength];
 					startLowAddr = ipStartInfoLong[1];
 
 					endHighAddr = ipEndInfoLong[0]
 							& WhoisUtil.IPV6Array[endIpLength];
 					endLowAddr = ipEndInfoLong[1];
 				} else {
 					startHighAddr = ipStartInfoLong[0];
 					startLowAddr = ipStartInfoLong[1]
 							& WhoisUtil.IPV6Array[startIpLength];
 
 					endLowAddr = ipEndInfoLong[0]
 							& WhoisUtil.IPV6Array[endIpLength];
 					endHighAddr = ipEndInfoLong[1];
 				}
 
 			} else {
 				startLowAddr = ipStartInfoLong[0]
 						& WhoisUtil.IPV4Array[startIpLength];
 				endLowAddr = ipStartInfoLong[0]
 						& WhoisUtil.IPV4Array[endIpLength];
 			}
 
 		} else {
 			if (startAddress.indexOf(":") != -1
 					&& endAddress.indexOf(":") != -1) {
 				startHighAddr = ipStartInfoLong[0];
 				startLowAddr = ipStartInfoLong[1];
 
 				endHighAddr = ipEndInfoLong[0];
 				endLowAddr = ipEndInfoLong[1];
 			} else {
 				startHighAddr = 0;
 				startLowAddr = ipStartInfoLong[0];
 
 				endHighAddr = 0;
 				endLowAddr = ipEndInfoLong[0];
 			}
 
 		}
 		long[] iplongs = { startHighAddr, endHighAddr, startLowAddr, endLowAddr };
 		return iplongs;
 	}
 
 	/**
 	 * Long converted to type IpV4
 	 * 
 	 * @param longip
 	 * @return ipv4String
 	 */
 
 	public static String longtoipV4(long longip) {
 		return String.format("%d.%d.%d.%d", longip >>> 24,
 				(longip & 0x00ffffff) >>> 16, (longip & 0x0000ffff) >>> 8,
 				longip & 0x000000ff);
 	}
 
 	/**
 	 * Long converted to type IpV6
 	 * 
 	 * @param highBits
 	 * @param lowBits
 	 * @return ipv6String
 	 */
 	public static String ipV6ToString(long highBits, long lowBits) {
 		short[] shorts = new short[8];
 		String[] strings = new String[shorts.length];
 
 		for (int i = 0; i < 8; i++) {
 			if (i >= 0 && i < 4)
 				strings[i] = String
 						.format("%x",
 								(short) (((highBits << i * 16) >>> 16 * (8 - 1)) & 0xFFFF));
 			else
 				strings[i] = String
 						.format("%x",
 								(short) (((lowBits << i * 16) >>> 16 * (8 - 1)) & 0xFFFF));
 
 		}
 
 		StringBuilder result = new StringBuilder();
 
 		for (int i = 0; i < strings.length; i++) {
 			result.append(strings[i]);
 			if (i < strings.length - 1)
 				result.append(":");
 		}
 		return result.toString();
 	}
 
 	/**
 	 * The abbreviated IPv6 converted into a standard wording
 	 * 
 	 * @param string
 	 * @return ipv6String
 	 */
 	public static String expandShortNotation(String string) {
 		if (!string.contains("::")) {
 			return string;
 		} else if (string.equals("::")) {
 			return generateZeroes(8);
 		} else {
 			final int numberOfColons = countOccurrences(string, ':');
 			if (string.startsWith("::"))
 				return string.replace("::", generateZeroes((7 + 2)
 						- numberOfColons));
 			else if (string.endsWith("::"))
 				return string.replace("::", ":"
 						+ generateZeroes((7 + 2) - numberOfColons));
 			else
 				return string.replace("::", ":"
 						+ generateZeroes((7 + 2 - 1) - numberOfColons));
 		}
 	}
 
 	/**
 	 * Generated IPv6 address 0
 	 * 
 	 * @param number
 	 * @return ipv6String
 	 */
 	public static String generateZeroes(int number) {
 		final StringBuilder builder = new StringBuilder();
 		for (int i = 0; i < number; i++) {
 			builder.append("0:");
 		}
 
 		return builder.toString();
 	}
 
 	/**
 	 * The record ipv6 address: Number of
 	 * 
 	 * @param haystack
 	 * @param needle
 	 * @return count
 	 */
 	public static int countOccurrences(String haystack, char needle) {
 		int count = 0;
 		for (int i = 0; i < haystack.length(); i++) {
 			if (haystack.charAt(i) == needle) {
 				count++;
 			}
 		}
 		return count;
 	}
 
 	/**
 	 * Ip converted to type long
 	 * 
 	 * @param ipStr
 	 * @return ipLongArr[]
 	 */
 	public static long[] iptolong(String ipStr) {
 		long[] ipLongArr = new long[2];
 		if (ipStr.indexOf(".") >= 0) {
 			String[] ip = ipStr.split("\\.");
 			ipLongArr[0] = (Long.parseLong(ip[0]) << 24)
 					+ (Long.parseLong(ip[1]) << 16)
 					+ (Long.parseLong(ip[2]) << 8) + Long.parseLong(ip[3]);
 			ipLongArr[1] = 0;
 		} else {
 			return Ipv6ToLong(ipStr);
 		}
 		return ipLongArr;
 	}
 
 	public static long[] Ipv6ToLong(String longip) {
 		String[] strings = expandShortNotation(longip).split(":");
 		long[] longs = new long[strings.length];
 
 		long high = 0L;
 		long low = 0L;
 		for (int i = 0; i < strings.length; i++) {
 
 			if (i >= 0 && i < 4)
 				high |= (Long.parseLong(strings[i], 16) << ((longs.length - i - 1) * 16));
 			else
 				low |= (Long.parseLong(strings[i], 16) << ((longs.length - i - 1) * 16));
 		}
 		longs[0] = high;
 		longs[1] = low;
 		return longs;
 	}
 
 	/**
 	 * Generate an error map collection
 	 * 
 	 * @param errorCode
 	 * @param title
 	 * @param description
 	 * @return map
 	 */
 	public static Map<String, Object> getErrorMessage(String errorCode,
			String title, String [] description) {
 		Map<String, Object> errorMessageMap = new HashMap<String, Object>();
 		errorMessageMap.put("errorCode", errorCode);
 		errorMessageMap.put("title", title);
 		errorMessageMap.put("description", description);
 		return errorMessageMap;
 	}
 
 	/**
 	 * Generated to store a collection of fields
 	 * 
 	 * @param tableName
 	 * @return List
 	 */
 	public static List<String> getKeyFileds(String tableName) {
 		List<String> keyList = new ArrayList<String>();
 		String[] keyFileds = null;
 		int index = Arrays.binarySearch(extendColumnTableTypes, tableName);
 		keyFileds = keyFiledsSet[index];
 		for (int i = 0; i < keyFileds.length; i++) {
 			keyList.add(keyFileds[i]);
 		}
 		return keyList;
 	}
 
 	/**
 	 * Chinese unicode string is converted to Chinese
 	 * 
 	 * @param url
 	 * @return ChineseUrl
 	 * @throws UnsupportedEncodingException
 	 */
 	public static String toChineseUrl(String url)
 			throws UnsupportedEncodingException {
 		//%E4%E5
 		String parra = "";
 		java.util.List<Byte> values = new ArrayList<Byte>();
 		boolean flag = true;
 		for (int i = 0; i < url.length(); i++) {
 			Character str3 = url.charAt(i);
 			if (str3.equals('%')) {
 				values.add((byte) Integer.parseInt(
 						new Character(url.charAt(++i)).toString()
 								+ new Character(url.charAt(++i)).toString(), 16));
 				flag = false;
 
 			} else {
 				if (!flag) {
 					byte[] valueByte = new byte[values.size()];
 					for (int index = 0; index < valueByte.length; index++) {
 						valueByte[index] = values.get(index);
 					}
 					parra += new String(valueByte, "utf-8") + str3.toString();
 				} else {
 					parra += str3.toString();
 
 				}
 				flag = true;
 				values = new ArrayList<Byte>();
 			}
 		}
 		if (!flag) {
 			byte[] valueByte = new byte[values.size()];
 			for (int index = 0; index < valueByte.length; index++) {
 				valueByte[index] = values.get(index);
 			}
 			parra += new String(valueByte, "utf-8");
 		}
 		return parra;
 	}
 	/**
 	 * Changes will be part of the data into the VCard style
 	 * 
 	 * @param map
 	 * @return map collection
 	 */
 	public static Map<String, Object> toVCard(Map<String, Object> map) {
 		List<List<String>> list = new ArrayList<List<String>>();
 		Object entityNames = map.get("Entity Names");
 		Object postalAddress = map.get("postalAddress");
 		Object emails = map.get("Emails");
 		Object phones = map.get("phones");
 
 		List<String> firstNameList = new ArrayList<String>();
 		firstNameList.add("version");
 		firstNameList.add("{}");
 		firstNameList.add("text");
 		firstNameList.add("4.0");
 		list.add(firstNameList);
 		if (entityNames != null) {
 			if (entityNames instanceof String[]) {
 				String[] namesArray = (String[]) entityNames;
 				for (String names : namesArray) {
 					List<String> nameList = new ArrayList<String>();
 					nameList.add("fn");
 					nameList.add("{}");
 					nameList.add("text");
 					nameList.add(names);
 					list.add(nameList);
 				}
 			}else{
 				List<String> nameList = new ArrayList<String>();
 				nameList.add("fn");
 				nameList.add("{}");
 				nameList.add("text");
 				nameList.add(entityNames.toString());
 				list.add(nameList);
 			}
 			map.remove("Entity Names");
 		}
 		if (postalAddress != null) {
 			if (postalAddress instanceof Map) {
 				Set<String> key = ((Map) postalAddress).keySet();
 				List<String> nameList = new ArrayList<String>();
 				nameList.add("label");
 				nameList.add("{}");
 				nameList.add("text");
 				for (String name : key) {
 					nameList.add(((Map) postalAddress).get(name).toString());
 				}
 				list.add(nameList);
 			} else if (postalAddress instanceof Object[]) {
 				for (Object postalAddressObject : ((Object[]) postalAddress)) {
 					Set<String> key = ((Map) postalAddressObject).keySet();
 					List<String> nameList = new ArrayList<String>();
 					nameList.add("label");
 					nameList.add("{}");
 					nameList.add("text");
 					for (String name : key) {
 						nameList.add(((Map) postalAddressObject).get(name)
 								.toString());
 					}
 					list.add(nameList);
 				}
 			}
 			map.remove("postalAddress");
 		}
 		;
 		if (emails != null) {
 			String[] namesArray = (String[]) emails;
 			for (String names : namesArray) {
 				List<String> nameList = new ArrayList<String>();
 				nameList.add("email");
 				nameList.add("{}");
 				nameList.add("text");
 				nameList.add(names);
 				list.add(nameList);
 			}
 			map.remove("Emails");
 		}
 		;
 		if (phones != null) {
 			if (phones instanceof Map) {
 				Set<String> key = ((Map) phones).keySet();
 				List<String> nameList = new ArrayList<String>();
 				for (String name : key) {
 					Object values = ((Map) phones).get(name);
 					if (values instanceof String[]) {
 						String typeName = "";
 						if (name.equals("Office")) {
 							typeName = "{type:work}";
 						} else if (name.equals("Fax")) {
 							typeName = "{type:fax}";
 						} else if (name.equals("Mobile")) {
 							typeName = "{type:cell}";
 						}
 						for (String valueName : (String[]) values) {
 							List<String> nameListArray = new ArrayList<String>();
 							nameListArray.add("tel");
 							nameListArray.add(typeName);
 							nameListArray.add("text");
 							nameListArray.add(valueName);
 							list.add(nameListArray);
 						}
 
 						continue;
 					}
 					nameList.add("tel");
 					nameList.add("{}");
 					nameList.add("text");
 					nameList.add(values.toString());
 					list.add(nameList);
 				}
 
 			} else if (phones instanceof Object[]) {
 				for (Object phonesObject : ((Object[]) phones)) {
 					Set<String> key = ((Map) phonesObject).keySet();
 					List<String> nameList = new ArrayList<String>();
 					for (String name : key) {
 						Object values = ((Map) phonesObject).get(name);
 						if (values instanceof String[]) {
 							String typeName = "";
 							if (name.equals("Office")) {
 								typeName = "{type:work}";
 							} else if (name.equals("Fax")) {
 								typeName = "{type:fax}";
 							} else if (name.equals("Mobile")) {
 								typeName = "{type:cell}";
 							}
 							for (String valueName : (String[]) values) {
 								List<String> nameListArray = new ArrayList<String>();
 								nameListArray.add("tel");
 								nameListArray.add(typeName);
 								nameListArray.add("text");
 								nameListArray.add(valueName);
 								list.add(nameListArray);
 							}
 							continue;
 						}
 						nameList.add("tel");
 						nameList.add("{}");
 						nameList.add("text");
 						nameList.add(values.toString());
 						list.add(nameList);
 					}
 				}
 			}
 			map.remove("phones");
 		}
 		map.put("vCard", list.toArray());
 		return map;
 	}
 }
