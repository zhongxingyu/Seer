 package util;
 
 
 import java.util.List;
 
 import entityBeans.*;
 
 
 
 
 
 public class ServerUtils {
 	private static boolean hasContact = false;
 	private ServerUtils() {
 	}
 
 	static public String getNewParameter(String name, String value) {
 		return ("?" + name + "=" + value);
 	}
 
 	/*
 	static public String generateRow(String key, String value) {
 		String generatedHtml = "";
 		generatedHtml += "<tr>";
 		generatedHtml += "<td style=\"width: 140px;\">" + key + "</td>";
 		generatedHtml += "<td valign=\"top\">" + value + "</td>";
 		generatedHtml += "</tr>";
 		return generatedHtml;
 	}
 	*/
 	
 	
 	static public String generateContactRow(String hId, String hFN, String hLN, String E,String street, String city, String zip, String country, String pk, String pn, String gn) {
 		String generatedHtml = "";
 		generatedHtml += "<tr>";
 		generatedHtml += "<td style=\"width: 50px;\"  >" + hId + "</td>";
 		generatedHtml += "<td style=\"width: 100px;\" >" + hFN + "</td>";
 		generatedHtml += "<td style=\"width: 100px;\" >" + hLN + "</td>";
 		generatedHtml += "<td style=\"width: 100px;\" >" + street + "</td>";
 		generatedHtml += "<td style=\"width: 100px;\" >" + city + "</td>";
 		generatedHtml += "<td style=\"width: 100px;\" >" + zip + "</td>";
 		generatedHtml += "<td style=\"width: 100px;\" >" + country + "</td>";
 		generatedHtml += "<td style=\"width: 100px;\" >" + pk + "</td>";
 		generatedHtml += "<td style=\"width: 100px;\" >" + pn + "</td>";
 		generatedHtml += "<td style=\"width: 100px;\" >" + gn + "</td>";
 		generatedHtml += "</tr>";
 		return generatedHtml;
 	}
 	static public String generateEntrepriseRow(String hId,String siret, String hFN, String hLN, String E,String street, String city, String zip, String country, String pk, String pn, String gn) {
 		String generatedHtml = "";
 		generatedHtml += "<tr>";
 		generatedHtml += "<td style=\"width: 50px;\"  >" + hId + "</td>";
 		generatedHtml += "<td style=\"width: 50px;\"  >" + siret + "</td>";
 		generatedHtml += "<td style=\"width: 100px;\" >" + hFN + "</td>";
 		generatedHtml += "<td style=\"width: 100px;\" >" + hLN + "</td>";
 		generatedHtml += "<td style=\"width: 100px;\" >" + street + "</td>";
		generatedHtml += "<td style=\"width: 100px;\" >" + city + "</td>";
 		generatedHtml += "<td style=\"width: 100px;\" >" + zip + "</td>";
 		generatedHtml += "<td style=\"width: 100px;\" >" + country + "</td>";
 		generatedHtml += "<td style=\"width: 100px;\" >" + pk + "</td>";
 		generatedHtml += "<td style=\"width: 100px;\" >" + pn + "</td>";
 		generatedHtml += "<td style=\"width: 100px;\" >" + gn + "</td>";
 		generatedHtml += "</tr>";
 		return generatedHtml;
 	}
 	static public String generateRow(String hId, String hFN, String hLN, String E) {
 		String generatedHtml = "";
 		generatedHtml += "<tr>";
 		generatedHtml += "<td style=\"width: 50px;\"  >" + hId + "</td>";
 		generatedHtml += "<td style=\"width: 100px;\" >" + hFN + "</td>";
 		generatedHtml += "<td style=\"width: 100px;\" >" + hLN + "</td>";
 		generatedHtml += "<td style=\"width: 100px;\" >" + E + "</td>";
 
 		generatedHtml += "</tr>";
 		return generatedHtml;
 	}
 	static public String generateRowEntrepriseFullHeader() {
 		String generatedHtml = "";
 		generatedHtml += "<tr>";
 		generatedHtml += "<td style=\"width: 100px;\" >" + "numSiret"      + "</td>";
 		generatedHtml += "<td style=\"width: 50px;\"  >" + "<B>ID</B>"         + "</td>";
 		generatedHtml += "<td style=\"width: 100px;\" >" + "<B>First Name</B>" + "</td>";
 		generatedHtml += "<td style=\"width: 100px;\" >" + "<B>Last Name</B>"  + "</td>";
 		generatedHtml += "<td style=\"width: 100px;\" >" + "<B>Email</B>"      + "</td>";
 		generatedHtml += "<td style=\"width: 100px;\" >" + "<B>Street</B>"      + "</td>";
 		generatedHtml += "<td style=\"width: 100px;\" >" + "<B>City</B>"      + "</td>";
 		generatedHtml += "<td style=\"width: 100px;\" >" + "<B>zip</B>"      + "</td>";
 		generatedHtml += "<td style=\"width: 100px;\" >" + "<B>Country</B>"      + "</td>";
 		//generatedHtml += "<td style=\"width: 100px;\" >" + "PhoneKind"      + "</td>";
 		//generatedHtml += "<td style=\"width: 100px;\" >" + "PhoneNumber"    + "</td>";
 		generatedHtml += "</tr>";
 		return generatedHtml;
 	}
 	static public String generateRowContactFullHeader() {
 		String generatedHtml = "";
 		generatedHtml += "<tr>";
 		generatedHtml += "<td style=\"width: 50px;\"  >" + "<B>ID</B>"         + "</td>";
 		generatedHtml += "<td style=\"width: 100px;\" >" + "<B>First Name</B>" + "</td>";
 		generatedHtml += "<td style=\"width: 100px;\" >" + "<B>Last Name</B>"  + "</td>";
 		generatedHtml += "<td style=\"width: 100px;\" >" + "<B>Email</B>"      + "</td>";
 		generatedHtml += "<td style=\"width: 100px;\" >" + "<B>Street</B>"      + "</td>";
 		generatedHtml += "<td style=\"width: 100px;\" >" + "<B>City</B>"      + "</td>";
 		generatedHtml += "<td style=\"width: 100px;\" >" + "<B>zip</B>"      + "</td>";
		generatedHtml += "<td style=\"width: 100px;\" >" + "<B>Country</B>"      + "</td>";
 		//generatedHtml += "<td style=\"width: 100px;\" >" + "PhoneKind"      + "</td>";
 		//generatedHtml += "<td style=\"width: 100px;\" >" + "PhoneNumber"    + "</td>";
 		generatedHtml += "</tr>";
 		return generatedHtml;
 	}
 	static public String generateRowEntrepriseFull(Entreprise c) {
 		
 		String generatedHtml = "";
 		generatedHtml += "<tr>";
 		generatedHtml += "<td valign=\"top\">" + c.getNumSiret() + "</td>";
 		generatedHtml += "<td valign=\"top\">" + c.getContactId() + "</td>";
 		generatedHtml += "<td valign=\"top\">" + c.getFirstName() + "</td>";
 		generatedHtml += "<td valign=\"top\">" + c.getLastName() + "</td>";
 		generatedHtml += "<td valign=\"top\">" + c.getEmail() + "</td>";
 		generatedHtml += "<td valign=\"top\">" + c.getAddress().getStreet() + "</td>";
 		generatedHtml += "<td valign=\"top\">" + c.getAddress().getCity() + "</td>";
 		generatedHtml += "<td valign=\"top\">" + c.getAddress().getZip() + "</td>";
 		generatedHtml += "<td valign=\"top\">" + c.getAddress().getCountry() + "</td>";
 		
 		return generatedHtml;
 	}
 	static public String generateRowContactFull(Contact c) {
 		
 		String generatedHtml = "";
 		generatedHtml += "<tr>";
 		generatedHtml += "<td valign=\"top\">" + c.getContactId() + "</td>";
 		generatedHtml += "<td valign=\"top\">" + c.getFirstName() + "</td>";
 		generatedHtml += "<td valign=\"top\">" + c.getLastName() + "</td>";
 		generatedHtml += "<td valign=\"top\">" + c.getEmail() + "</td>";
 		generatedHtml += "<td valign=\"top\">" + c.getAddress().getStreet() + "</td>";
 		generatedHtml += "<td valign=\"top\">" + c.getAddress().getCity() + "</td>";
 		generatedHtml += "<td valign=\"top\">" + c.getAddress().getZip() + "</td>";
 		generatedHtml += "<td valign=\"top\">" + c.getAddress().getCountry() + "</td>";
 		
 		return generatedHtml;
 	}
 	static public String generateRow(Contact c) {
 		
 		String generatedHtml = "";
 		generatedHtml += "<tr>";
 		generatedHtml += "<td valign=\"top\">" + c.getContactId() + "</td>";
 		generatedHtml += "<td valign=\"top\">" + c.getFirstName() + "</td>";
 		generatedHtml += "<td valign=\"top\">" + c.getLastName() + "</td>";
 		generatedHtml += "<td valign=\"top\">" + c.getEmail() + "</td>";
 //		generatedHtml += "<td valign=\"top\">" + c.getAddress().getStreet() + "</td>";
 //		generatedHtml += "<td valign=\"top\">" + c.getAddress().getCity() + "</td>";
 //		generatedHtml += "<td valign=\"top\">" + c.getAddress().getZip() + "</td>";
 //		generatedHtml += "<td valign=\"top\">" + c.getAddress().getCountry()+ "</td>";
 //		Set<PhoneNumber> phoneList = c.getPhoneNumbers();
 //		for (PhoneNumber p : phoneList)
 //		{
 //			generatedHtml += "<td valign=\"top\">" + p.getPhoneKind() + "</td>";
 //			generatedHtml += "<td valign=\"top\">" + p.getPhoneNumber() + "</td>";
 //		}
 //		Set<ContactGroup> groupList = c.getContactgroup();
 //		for (ContactGroup cg: groupList)
 //		{
 //			generatedHtml += "<td valign=\"top\">" + cg.getGroupName() + "</td>";
 //		}
 //		generatedHtml += "</tr>";
 		return generatedHtml;
 	}
 	static public String generateContactRow(Contact c,Address a, PhoneNumber p, ContactGroup cg) 
 	{
 		
 		String generatedHtml = "";
 		generatedHtml += "<tr>";
 		if (c != null)
 		{
 			generatedHtml += "<td valign=\"top\">" + c.getContactId() + "</td>";
 			generatedHtml += "<td valign=\"top\">" + c.getFirstName() + "</td>";
 			generatedHtml += "<td valign=\"top\">" + c.getLastName() + "</td>";
 			generatedHtml += "<td valign=\"top\">" + c.getEmail() + "</td>";
 			
 				
 		}
 		if (a != null)
 		{
 			generatedHtml += "<td valign=\"top\">" + a.getStreet() + "</td>";
 			generatedHtml += "<td valign=\"top\">" + a.getCity() + "</td>";
 			generatedHtml += "<td valign=\"top\">" + a.getZip() + "</td>";
 			generatedHtml += "<td valign=\"top\">" + a.getCountry()+ "</td>";
 		}
 		if (p != null)
 		{
 			generatedHtml += "<td valign=\"top\">" + p.getPhoneKind() + "</td>";
 			generatedHtml += "<td valign=\"top\">" + p.getPhoneNumber() + "</td>";
 		}
 		if (cg != null)
 		{
 			generatedHtml += "<td valign=\"top\">" + cg.getGroupName() + "</td>";
 		}
 		
 		generatedHtml += "</tr>";
 		return generatedHtml;
 	}
 	static public String generateEntrepriseRow(Entreprise c,Address a, PhoneNumber p, ContactGroup cg) 
 	{
 		
 		String generatedHtml = "";
 		generatedHtml += "<tr>";
 		if (c != null)
 		{
 			generatedHtml += "<td valign=\"top\">" + c.getContactId() + "</td>";
 			generatedHtml += "<td valign=\"top\">" + ((Entreprise)c).getNumSiret() + "</td>";
 			generatedHtml += "<td valign=\"top\">" + c.getFirstName() + "</td>";
 			generatedHtml += "<td valign=\"top\">" + c.getLastName() + "</td>";
 			generatedHtml += "<td valign=\"top\">" + c.getEmail() + "</td>";
 			
 				
 		}
 		if (a != null)
 		{
 			generatedHtml += "<td valign=\"top\">" + a.getStreet() + "</td>";
 			generatedHtml += "<td valign=\"top\">" + a.getCity() + "</td>";
 			generatedHtml += "<td valign=\"top\">" + a.getZip() + "</td>";
 			generatedHtml += "<td valign=\"top\">" + a.getCountry()+ "</td>";
 		}
 		if (p != null)
 		{
 			generatedHtml += "<td valign=\"top\">" + p.getPhoneKind() + "</td>";
 			generatedHtml += "<td valign=\"top\">" + p.getPhoneNumber() + "</td>";
 		}
 		if (cg != null)
 		{
 			generatedHtml += "<td valign=\"top\">" + cg.getGroupName() + "</td>";
 		}
 		
 		generatedHtml += "</tr>";
 		return generatedHtml;
 	}
 	static public String generateContactTable(List<Object[]> list, String header) {
 		String generatedHtml = "";
 		if (list.size() != 0) {
 
 			generatedHtml += "<div align=\"center\">";
 			generatedHtml += "<br><br>";
 			generatedHtml += "<table><tbody>";
 			//generatedHtml += "<TABLE BORDER=\"1\">";
 			generatedHtml += "<CAPTION><B>" + header + "</B> </CAPTION>";
 			generatedHtml += generateContactRow("<B>ID</B>", "<B>First Name</B>", "<B>Last Name</B>", "<B>Email</B>", "<B>Street</B>", "<B>City</B>", "<B>Zip</B>", "<B>Country</B>", "<B>PhoneKind</B>", "<B>PhoneNumber</B>", "<B>GroupName</B>");
 			for (Object[] objs: list)
 			{
 				Contact contact = (Contact) objs[0];
 				if (contact instanceof Entreprise)
 					continue;
 				Address address = (Address) objs[1];
 				PhoneNumber phoneNumber = (PhoneNumber)objs[2];
 				ContactGroup contactGroup = (ContactGroup)objs[3];
 				generatedHtml += generateContactRow(contact, address, phoneNumber, contactGroup);
 				System.out.println();
 				if (hasContact == false)
 					hasContact = true;
 			}
 			generatedHtml += "</tbody></table>";
 			generatedHtml += "</div>";
 		}
 		System.out.println("gen html" + generatedHtml);
 		if (!hasContact)
 			generatedHtml = "";
 		return generatedHtml;
 	}
 	static public String generateEntrepriseTable(List<Object[]> list, String header) {
 		String generatedHtml = "";
 		if (list.size() != 0) {
 
 			generatedHtml += "<div align=\"center\">";
 			generatedHtml += "<br><br>";
 			generatedHtml += "<table><tbody>";
 			//generatedHtml += "<TABLE BORDER=\"1\">";
 			generatedHtml += "<CAPTION><B>" + header + "</B> </CAPTION>";
 			generatedHtml += generateEntrepriseRow("<B>ID</B>","<B>SiretNumber</B>", "<B>First Name</B>", "<B>Last Name</B>", "<B>Email</B>", "<B>Street</B>", "<B>City</B>", "<B>Zip</B>", "<B>Country</B>", "<B>PhoneKind</B>", "<B>PhoneNumber</B>", "<B>GroupName</B>");
 			for (Object[] objs: list)
 			{
 				Entreprise entreprise = (Entreprise) objs[0];
 				Address address = (Address) objs[1];
 				PhoneNumber phoneNumber = (PhoneNumber)objs[2];
 				ContactGroup contactGroup = (ContactGroup)objs[3];
 				generatedHtml += generateEntrepriseRow(entreprise, address, phoneNumber, contactGroup);
 				System.out.println();
 			}
 			generatedHtml += "</tbody></table>";
 			generatedHtml += "</div>";
 		}
 		System.out.println("gen html" + generatedHtml);
 		return generatedHtml;
 	}
 	static public String generateTableEntreprise(List<Entreprise> lst, String header) {
 		String generatedHtml = "";
 		if (lst.size() != 0) {
 
 			generatedHtml += "<div align=\"center\">";
 			generatedHtml += "<br><br>";
 			generatedHtml += "<table><tbody>";
 			//generatedHtml += "<TABLE BORDER=\"1\">";
 			generatedHtml += "<CAPTION><B>" + header + "</B> </CAPTION>";
 			//generatedHtml += generateRow("<B>ID</B>", "<B>First Name</B>", "<B>Last Name</B>", "<B>Email</B>");
 			generatedHtml += generateRowEntrepriseFullHeader();
 			for (Entreprise e : lst) {
 				generatedHtml += generateRowEntrepriseFull(e);
 			}
 			generatedHtml += "</tbody></table>";
 			generatedHtml += "</div>";
 		}
 		System.out.println("gen html" + generatedHtml);
 		return generatedHtml;
 	}
 	static public String generateTable(List<Contact> lst, String header) {
 		String generatedHtml = "";
 		if (lst.size() != 0) {
 
 			generatedHtml += "<div align=\"center\">";
 			generatedHtml += "<br><br>";
 			generatedHtml += "<table><tbody>";
 			//generatedHtml += "<TABLE BORDER=\"1\">";
 			generatedHtml += "<CAPTION><B>" + header + "</B> </CAPTION>";
 			//generatedHtml += generateRow("<B>ID</B>", "<B>First Name</B>", "<B>Last Name</B>", "<B>Email</B>");
 			generatedHtml += generateRowContactFullHeader();
 			for (Contact c : lst) {
 				generatedHtml += generateRowContactFull(c);
 			}
 			generatedHtml += "</tbody></table>";
 			generatedHtml += "</div>";
 		}
 		System.out.println("gen html" + generatedHtml);
 		return generatedHtml;
 	}
 	
 	static public void showParameters(
 			String firstName
 			, String lastName
 			, String email
 			, String street
 			, String city
 			, String zip
 			, String country
 			, String phoneKind
 			, String phoneNumber
 			, String numSiret
 			){
 		
 		System.out.println("firstname="+ firstName);
 		System.out.println("lastname="+ lastName);
 		System.out.println("email="+ email);
 		System.out.println("street="+ street);
 		System.out.println("city="+ city);
 		System.out.println("zip="+ zip);
 		System.out.println("country="+ country);
 		System.out.println("phonekind="+ phoneKind);
 		System.out.println("phonenumber="+ phoneNumber);
 		System.out.println("numSiret="+ numSiret);
 	}
 	
 	
 	
 	public static String opFait = "<i><b>operation est Fait</i></b>";
 	public static String opNoRecods = "<i><b>Pas de records dans base de donées</i></b>";
 	public static String opTableRemoved  = "<i><b>Table est effacé</i></b>";
 }
