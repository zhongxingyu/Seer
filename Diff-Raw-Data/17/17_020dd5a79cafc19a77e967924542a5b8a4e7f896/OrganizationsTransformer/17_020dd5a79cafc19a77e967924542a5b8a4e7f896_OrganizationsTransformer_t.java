 /**
  * (c) Copyright 2011 WESO, Computer Science Department,
  * Facultad de Ciencias, University of Oviedo, Oviedo, Asturias, Spain, 33007
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  * 3. The name of the author may not be used to endorse or promote products
  *    derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
  * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
  * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.weso.moldeas.transformer.organizations;
 
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 
 import org.apache.commons.validator.UrlValidator;
 import org.apache.log4j.Logger;
 import org.weso.moldeas.loader.resources.FilesResourceLoader;
 import org.weso.moldeas.loader.resources.ResourceLoader;
 import org.weso.moldeas.transformer.pscs.ChainTransformerAdapter;
 import org.weso.moldeas.utils.PrefixManager;
 import org.weso.moldeas.utils.PrettyPrinter;
 import org.weso.moldeas.utils.TransformerConstants;

import au.com.bytecode.opencsv.CSVReader;
 
 import com.hp.hpl.jena.rdf.model.Literal;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.sparql.vocabulary.FOAF;
 import com.hp.hpl.jena.vocabulary.RDF;
 import com.hp.hpl.jena.vocabulary.RDFS;
 import com.hp.hpl.jena.vocabulary.VCARD;
 
 public class OrganizationsTransformer extends ChainTransformerAdapter{
 
 	private static final String DEFAULT_HTTP_PURL_ORG_WESO_MOLDEAS = "http://purl.org/weso/moldeas/";
 	public static final String NOT_AVAILABLE = "not-available";
 	private static final int MAX_RESOURCE = 100000;
 	private static final String MOLDEAS_ORG_PREFIX = "moldeas-org";
 	protected static Logger logger = Logger.getLogger(OrganizationsTransformer.class);
 	static Pattern pattern =  Pattern.compile("^[0-9.()-]{10,25}$"); // ITU E.164 or IETF RfC 3966. 
  	/**
 	 *  
 	 *  0) 000012-2011->ID contract
 	 *  1) bg->ID country
 	 *  2) false
 	 *  3) 7
 	 *  4) 5    
 	 *  5) 1
 	 *  6) ContractingAuthority->Rewarded by
 	 *  7) Европейска централна банка
 	 *  8) ,
 	 *  9) ,
 	 *  10) ,
 	 *  11)"Kaiserstraße 29, 60311, Франкфурт на Майн"->Full address
 	 *  12)Kaiserstraße 29->Street
 	 *  13)60311->Postal code
 	 *  14)Франкфурт на Майн->Region
 	 *  15),
 	 *  16),
 	 *  17)"neubau-ausschreibung@ecb.europa.eu"->E-mail
 	 *  18)+49.691344 +49.691340->Telephone
 	 *  19)+49.691344 +49.696000->Fax
 	 *  20)http://www.ecb.europa.eu/->Web address
 	 */
 	@Override
 	protected void execute() throws Exception {
 		UrlValidator urlValidator = new UrlValidator();
 		Map process = new HashMap<String, String>();
 		Model model = ModelFactory.createDefaultModel();
 		addProperties(model);
 		ResourceLoader loader = new FilesResourceLoader(new String[]{"organizations/corporate-example.txt"});
 		InputStream data =loader.getKnowledgeResources()[0].getKnowledgeSourceData();
 		CSVReader reader = new CSVReader(new InputStreamReader(data),',','\"');
 		String [] nextLine;
 		int length = 0;
 		int i = 1;
 		int files = 0;
 		while ((nextLine = reader.readNext()) != null) {
 			length = nextLine.length;
 			String id = String.valueOf(i++);
 			String idContract = TransformerConstants.INDEX_ID_CONTRACT<length?nextLine[TransformerConstants.INDEX_ID_CONTRACT]:"";
 			String nutsCode = TransformerConstants.INDEX_NUTS_CODE<length?nextLine[TransformerConstants.INDEX_NUTS_CODE]:"";
 			String type = TransformerConstants.INDEX_TYPE<length?nextLine[TransformerConstants.INDEX_TYPE]:"";
 			String label = TransformerConstants.INDEX_LABEL<length?nextLine[TransformerConstants.INDEX_LABEL]:"";
 			String fullAddress = TransformerConstants.INDEX_FULL_ADDRESS<length?nextLine[TransformerConstants.INDEX_FULL_ADDRESS]:"";
 			String street = TransformerConstants.INDEX_STREET<length?nextLine[TransformerConstants.INDEX_STREET]:"";
 			String postalCode = TransformerConstants.INDEX_POSTAL_CODE<length?nextLine[TransformerConstants.INDEX_POSTAL_CODE]:"";
 			String region = TransformerConstants.INDEX_REGION<length?nextLine[TransformerConstants.INDEX_REGION]:"";
 			String email = searchEmail(nextLine,10,length);
 			String telephone = searchPhone(nextLine,10,length);
 			String homepage = searchHomePage(urlValidator,nextLine,10,length);
 			String fax= "";
 			OrganizationTO org = new OrganizationTO(id,idContract, nutsCode, type, label, fullAddress, 
 					street, postalCode, region, email, telephone, fax, homepage);
 			if (process.containsKey(idContract)){
 				org.setId((String) process.get(idContract));
 				addTriples(model,org);
 			}else{
 				createRDFResource(model, org);
 				process.put(idContract, id);
 			}
 			if (i > MAX_RESOURCE){
 				logger.info("Serializing "+i+" files generated "+files+" distinct contracts "+process.size()+" lines processed: "+(i*(files+1)));
 				PrettyPrinter.serializeModel(model, PrefixManager.getResourceBundle(), "generated/organization-"+files+".ttl", TransformerConstants.TURTLE_SYNTAX);
 				i = 0;
 				files++;
 				//Reconfigure model
 				model.removeAll();
 				model = null;
 				model = ModelFactory.createDefaultModel();
 				addProperties(model);
 			}
 		}
 		data.close();
 	//	PrettyPrinter.serializeModel(model, PrefixManager.getResourceBundle(), "generated/organization-"+files+".ttl", TransformerConstants.TURTLE_SYNTAX);
 		
 
 	}
 
 	private String searchHomePage(UrlValidator validator,String[] nextLine, int min, int max) {
 		boolean found = Boolean.FALSE;
 		String homePage = DEFAULT_HTTP_PURL_ORG_WESO_MOLDEAS;
 		for(int i = min;i<max && !found;i++){
 			if (validator.isValid(nextLine[i])){
 				found = Boolean.TRUE;
 				homePage = nextLine[i];
 			}
 		}
 		return homePage;
 	}
 
 	private String searchPhone(String[] nextLine, int min, int max) {
 		boolean found = Boolean.FALSE;
 		String phone =NOT_AVAILABLE;
 		for(int i = min;i<max && !found;i++){
 			if (isValidPhone(nextLine[i])){
 				found = Boolean.TRUE;
 				phone = nextLine[i];
 			}
 		}
 		return phone;
 	}
 
 	private boolean isValidPhone(String string) {
 		return pattern.matcher(string).matches();
 	}
 
 	private String searchEmail(String[] nextLine, int min, int max) {
 		boolean found = Boolean.FALSE;
 		String email =NOT_AVAILABLE;
 		for(int i = min;i<max && !found;i++){
 			if (isValidEmailAddress(nextLine[i])){
 				found = Boolean.TRUE;
 				email = nextLine[i];
 			}
 		}
 		return email;
 	}
 
 
 	private void addTriples(Model model, OrganizationTO org) {
 		Resource resource = model.getResource(PrefixManager.getURIPrefix(MOLDEAS_ORG_PREFIX)+"o"+org.getId());
 		Resource site  = null;
 		Resource organizationName = null;
 		Resource organizationAddress = null;
 		if (resource == null){
 			model.createResource(PrefixManager.getURIPrefix(MOLDEAS_ORG_PREFIX)+"o"+org.getId());			
 			//Init others
 			site = model.createResource(PrefixManager.getURIPrefix(MOLDEAS_ORG_PREFIX)+"o"+org.getId()+"#site");
 			organizationName = model.createResource(PrefixManager.getURIPrefix(MOLDEAS_ORG_PREFIX)+"o"+org.getId()+"#org");
 			organizationAddress = model.createResource(PrefixManager.getURIPrefix(MOLDEAS_ORG_PREFIX)+"o"+org.getId()+"#adr");
 		}
 
 		Resource organizationResource = model.getResource(PrefixManager.getURIPrefix(MOLDEAS_ORG_PREFIX)+"o"+org.getId());;
 		model.add(organizationResource,
 				RDFS.label, 
 				literalLang(model,org.getLabel(),org.getNutsCode()));
 		site = model.getResource(PrefixManager.getURIPrefix(MOLDEAS_ORG_PREFIX)+"o"+org.getId()+"#site");
 		model.add(site,
 				VCARD.FN,
 				literalLang(model,org.getLabel(),org.getNutsCode()));
 		organizationName = model.getResource(PrefixManager.getURIPrefix(MOLDEAS_ORG_PREFIX)+"o"+org.getId()+"#org");
 		model.add(organizationName,
 				VCARD.Orgname,
 				literalLang(model,org.getLabel(),org.getNutsCode()));
 		organizationAddress = model.getResource(PrefixManager.getURIPrefix(MOLDEAS_ORG_PREFIX)+"o"+org.getId()+"#adr");
 		model.add(organizationAddress,	
 				VCARD.Orgname,
 				literalLang(model,org.getLabel(),org.getNutsCode()));
 		model.add(organizationAddress,VCARD.Street,
 				literalLang(model,org.getFullAddress(),org.getNutsCode()));
 	
 	}
 
 	private void addProperties(Model model) {
 		model.createProperty(PrefixManager.getURIPrefix("org")+"identifier");
 		model.createProperty(PrefixManager.getURIPrefix("org")+"hasSite");
 		model.createProperty(PrefixManager.getURIPrefix("org")+"siteAddress");
 		model.createProperty(PrefixManager.getURIPrefix("moldeas-onto")+"ref-nuts");
 
 
 	}
 
 	private Resource createRDFResource(Model model, OrganizationTO org) {
 		Resource organizationResource = model.createResource(PrefixManager.getURIPrefix(MOLDEAS_ORG_PREFIX)+"o"+org.getId());
 		Resource site = model.createResource(PrefixManager.getURIPrefix(MOLDEAS_ORG_PREFIX)+"o"+org.getId()+"#site");
 		Resource siteAddress = model.createResource(PrefixManager.getURIPrefix(MOLDEAS_ORG_PREFIX)+"o"+org.getId()+"#siteAddress");
 		if (!org.getEmail().equalsIgnoreCase(""))
 			model.add(siteAddress,VCARD.EMAIL,model.createResource(org.getEmail()));
 
 		Resource organizationName = model.createResource(PrefixManager.getURIPrefix(MOLDEAS_ORG_PREFIX)+"o"+org.getId()+"#org");
 		model.add(organizationName,	VCARD.Orgname,literalLang(model,org.getLabel(),org.getNutsCode()));
 
 		Resource organizationAddress = model.createResource(PrefixManager.getURIPrefix(MOLDEAS_ORG_PREFIX)+"o"+org.getId()+"#adr");
 		model.add(organizationAddress,VCARD.Orgname,literalLang(model,org.getLabel(),org.getNutsCode()));
 		model.add(organizationAddress,VCARD.Locality,literalLang(model,org.getRegion(),org.getNutsCode()));
 		model.add(organizationAddress,VCARD.Pcode,org.getPostalCode());
 		model.add(organizationAddress,VCARD.Street,literalLang(model,org.getFullAddress(),org.getNutsCode()));
 
 		Resource organizationTel = model.createResource(PrefixManager.getURIPrefix(MOLDEAS_ORG_PREFIX)+"o"+org.getId()+"#tel");
 		model.add(organizationTel,RDF.value,org.getTelephone());
 
 
 		model.add(organizationResource,RDF.type,model.createResource("http://www.w3.org/ns/org#Organization"));
 		model.add(organizationResource,RDF.type,model.createResource(PrefixManager.getURIPrefix("moldeas-onto")+org.getType()));		
 
 
 		model.add(site,RDFS.label,literalLang(model,org.getLabel(),org.getNutsCode()));
 		model.add(site,VCARD.FN,literalLang(model,org.getLabel(),org.getNutsCode()));
 		//model.add(site,VCARD.ORG,organizationName);
 		model.add(site,VCARD.ADR,organizationAddress);
 		model.add(site,VCARD.TEL,organizationTel);
 
 
 		model.add(organizationResource,
 				model.getProperty(PrefixManager.getURIPrefix("org")+"hasSite"),
 				site);
 
 
 		if(!org.getHomepage().equalsIgnoreCase(""))
 			model.add(organizationResource,FOAF.homepage,model.createResource(org.getHomepage()));
 
 
 
 		return organizationResource;
 	}
 
 
 	public static Literal literalLang(Model m, String value, String lang){
 		return m.createLiteral(value, lang);
 	}
 
 	public static boolean isValidEmailAddress(String email) {
 		   boolean result = true;
 		   try {
 		      InternetAddress emailAddr = new InternetAddress(email);
 		      emailAddr.validate();
 		   } catch (AddressException ex) {
 		      result = false;
 		   }
 		   return result;
 		}
 	
 
 	public static void main(String []args) throws Exception{
 		new OrganizationsTransformer().execute();
 	}
 
 }
