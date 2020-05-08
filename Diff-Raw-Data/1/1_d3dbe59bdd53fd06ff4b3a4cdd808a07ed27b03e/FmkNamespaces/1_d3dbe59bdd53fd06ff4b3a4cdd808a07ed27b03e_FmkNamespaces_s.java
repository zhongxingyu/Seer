 package dk.medicinkortet.xmlschema;
 
 import com.trifork.xmlquery.Namespaces;
 
 public class FmkNamespaces {
 
 	public static Namespaces getNamespaces() {
 		Namespaces namespaces = Namespaces.getOIONamespaces();
 		namespaces.addNamespace("mc2008", "http://www.dkma.dk/medicinecard/xml.schema/2008/06/01");
 		namespaces.addNamespace("mc2009", "http://www.dkma.dk/medicinecard/xml.schema/2009/01/01");
 		namespaces.addNamespace("mc2010", "http://www.dkma.dk/medicinecard/xml.schema/2010/06/01");
 		namespaces.addNamespace("mc2011", "http://www.dkma.dk/medicinecard/xml.schema/2011/01/01");
 		namespaces.addNamespace("mc2012", "http://www.dkma.dk/medicinecard/xml.schema/2012/01/01");
 		namespaces.addNamespace("medicinecard20120901", "http://www.dkma.dk/medicinecard/xml.schema/2012/09/01");
 		namespaces.addNamespace("cpr", "http://rep.oio.dk/cpr.dk/xml/schemas/core/2005/03/18/");
 		namespaces.addNamespace("cpr2002", "http://rep.oio.dk/cpr.dk/xml/schemas/core/2002/06/28/");
 		namespaces.addNamespace("cpr2006", "http://rep.oio.dk/cpr.dk/xml/schemas/core/2006/01/17/");
 		namespaces.addNamespace("xkom", "http://rep.oio.dk/xkom.dk/xml/schemas/2006/01/06/");
 		namespaces.addNamespace("dkcc", "http://rep.oio.dk/ebxml/xml/schemas/dkcc/2003/02/13/");
 		namespaces.addNamespace("dkcc2005", "http://rep.oio.dk/ebxml/xml/schemas/dkcc/2005/03/15/");
 		namespaces.addNamespace("medcom", "http://www.medcom.dk/dgws/2006/04/dgws-1.0.xsd");
 		namespaces.addNamespace("itst2005", "http://rep.oio.dk/itst.dk/xml/schemas/2005/01/10/");
 		namespaces.addNamespace("itst", "http://rep.oio.dk/itst.dk/xml/schemas/2006/01/17/");
 		namespaces.addNamespace("dkcc2005-2", "http://rep.oio.dk/ebxml/xml/schemas/dkcc/2005/05/13/");
 		namespaces.addNamespace("xs", "http://www.w3.org/2001/XMLSchema");
 		namespaces.addNamespace("rs", "http://dkma.dk/receptserver/apotekssnitflade/xml/schemas/");
 		namespaces.addNamespace("pem", "http://dkma.dk/receptserver/apotekssnitflade/xml/schemas/");
 		namespaces.addNamespace("dkma", "http://rep.oio.dk/dkma.dk/xml/schemas/2006/01/15/");
 		namespaces.addNamespace("sundcom", "http://rep.oio.dk/sundcom.dk/medcom.dk/xml/schemas/2005/08/07/");
 		namespaces.addNamespace("xsd", "http://www.w3.org/2001/XMLSchema");
 		namespaces.addNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
 		namespaces.addNamespace("ean", "http://rep.oio.dk/ean/xml/schemas/2005/01/10/");
 		namespaces.addNamespace("sdsd", "http://www.sdsd.dk/dgws/2010/08");
 		
 		namespaces.addNamespace("saml", "urn:oasis:names:tc:SAML:2.0:assertion");
 		namespaces.addNamespace("wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
 		namespaces.addNamespace("wsu", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
 		namespaces.addNamespace("wsa", "http://schemas.xmlsoap.org/ws/2004/08/addressing");
 		namespaces.addNamespace("wst", "http://schemas.xmlsoap.org/ws/2005/02/trust");
 		namespaces.addNamespace("soap", "http://schemas.xmlsoap.org/soap/envelope/");
 		namespaces.addNamespace("ds", "http://www.w3.org/2000/09/xmldsig#");
 		
 		return namespaces;
 	}
 	
 	
 }
