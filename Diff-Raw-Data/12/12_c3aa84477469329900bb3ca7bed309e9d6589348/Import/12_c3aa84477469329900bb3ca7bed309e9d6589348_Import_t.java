 /*******************************************************************************
  * Copyright (c) 2012 Niklaus Giger <niklaus.giger@member.fsf.org>.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Niklaus Giger <niklaus.giger@member.fsf.org> - initial API and implementation
  ******************************************************************************/
 package org.oddb.ch;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.regex.Matcher;
 
 import org.yaml.snakeyaml.TypeDescription;
 import org.yaml.snakeyaml.Yaml;
 import org.yaml.snakeyaml.constructor.Constructor;
 import org.yaml.snakeyaml.constructor.SafeConstructor;
 import org.yaml.snakeyaml.nodes.Tag;
 
 import org.oddb.ch.Address2;
 import org.oddb.ch.Company;
 
 import ch.rgw.tools.Money;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Import {
 	public static final Logger logger = LoggerFactory.getLogger(Import.class);
 	
 	public class ElexisArtikel {
 		public String ean13;
 		public String name;
 		public Money EKPreis;
 		public Money VKPreis;
 		public String pharmacode;
 		public String atc_code;
 		public String verpackungsEinheit;
 		public String abgabeEinheit;
 		public String partString;
 		
 		public String toString(){
 			StringBuffer msg = new StringBuffer(" name: " + name);
 			if (partString != null)
 				msg.append(" partString: " + partString);
 			if (ean13 != null)
 				msg.append(" ean13: " + ean13);
 			if (EKPreis != null)
 				msg.append(String.format(" EK: %1$s %2$s", EKPreis.toString(),
 					EKPreis.getCentsAsString()));
 			if (VKPreis != null)
 				msg.append(String.format(" VK: %1$s %2$s", VKPreis.toString(),
 					VKPreis.getCentsAsString()));
 			if (verpackungsEinheit != null)
 				msg.append(" verpackungsEinheit: " + verpackungsEinheit);
 			if (abgabeEinheit != null)
 				msg.append(" abgabeEinheit: " + abgabeEinheit);
 			if (atc_code != null)
 				msg.append(" atc: " + atc_code);
 			if (pharmacode != null)
 				msg.append(" pharmacode: " + pharmacode);
 			return msg.toString();
 			
 		}
 	}
 	
 	/*
 	 * Access Registration via iksnr
 	 */
 	public HashMap<String, Registration> registrations = new HashMap<String, Registration>();
 	/*
 	 * Array of all companies
 	 */
 	public HashMap<String, Company> companies;
 	/*
 	 * Access Sequence via Sequenznumerierung aus der Registrationsurkunde
 	 */
 	public HashMap<String, Sequence> sequences;
 	/*
 	 * Access Package via ean13
 	 */
 	public HashMap<String, Package> packages;
 	public ArrayList<ElexisArtikel> articles;
 	
 	public Import(){
 		super();
 		registrations = new HashMap<String, Registration>();
 		companies = new HashMap<String, Company>();
 		sequences = new HashMap<String, Sequence>();
 		packages = new HashMap<String, Package>();
 		articles = new ArrayList<ElexisArtikel>();
 		Registration.counter = 0;
 		Address2.counter = 0;
 	}
 	
 	public class CustomConstructor extends SafeConstructor {
 		public CustomConstructor(){
 			// define tags which begin with !org.oddb...
 			this.yamlMultiConstructors.put(Version.ODDB_VERSION_PREFIX, new ConstructYamlMap());
 		}
 	}
 	
 	public boolean importFile(String filename){
 		try {
 			File file = new File(filename);
 			System.out.println(String.format("importFile: %1$s %2$d", file.getAbsolutePath(),
 				file.length()));
 			return importString(ch.rgw.io.FileTool.readTextFile(file, "ASCII"));
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	// This is an ugly patch, as snakeyaml has a problem with escaped UTF-8 characters
 	// produced by oddb.org.
 	// see http://code.google.com/p/snakeyaml/issues/detail?id=151
 	// Don't know where the problem lies. But this fixes the issues for the time beeing.
 	static private String patchUtfEscape(String input)
 	{			
 		HashMap<String, String> convertTable = new HashMap();
 		// see http://www.utf8-zeichentabelle.de/
 		convertTable.put("\\xC3\\xB6", "ö");
 		convertTable.put("\\xC3\\xA9", "é");
 		convertTable.put("\\xC3\\xA8", "è");
 		convertTable.put("\\xC3\\xA0", "à");
 		convertTable.put("\\xC3\\xC6", "Ö");
 		convertTable.put("\\xC3\\x9C", "Ü");
 		convertTable.put("\\xC3\\xBC", "ü");
 		convertTable.put("\\xC3\\x84", "Ä");
 		convertTable.put("\\xC3\\xA4", "ä");
 		convertTable.put("\\xC2\\xA0", " "); // No break space
 		convertTable.put("\\xC2\\xB5", "µ");
 		convertTable.put("\\xC3\\xAF", "ï");
 		convertTable.put("\\xC3\\xA2", "â");
 		convertTable.put("\\xC3\\x96", "Ö");
 		convertTable.put("\\xC2\\xB2", "²");
 		convertTable.put("\\xC2\\xAB", "«");
 		convertTable.put("\\xC2\\xBB", "»");
 		convertTable.put("\\xC3\\xAA", "ê");
 		convertTable.put("\\xC3\\xB4", "ô");
 		convertTable.put("\\xC3\\xA7", "ç");
 		convertTable.put("\\xC3\\xAB", "ë");
 		convertTable.put("\\xC3\\xB2", "ò");
 		convertTable.put("\\xC3\\x89", "É");
 		convertTable.put("\\xC3\\xAE", "î");
 		convertTable.put("\\xC3\\xBB", "û");
 		convertTable.put("\\xC2\\xB0", "°");
 		convertTable.put("\\xC3\\xB9", "ð");
 		convertTable.put("\\xC3\\xB3", "ó");
 		convertTable.put("\\xC2\\xB1", "±");
 		convertTable.put("\\xC3\\x80", "À");
 		convertTable.put("\\xC3\\xA1", "á");
 		convertTable.put("\\xC3\\xAC", "á");
 		Iterator iter = convertTable.entrySet().iterator();
 		while (iter.hasNext())
 		{
 			Entry<String, String> entry = (Entry<String, String>) iter.next();
 			input = input.replaceAll(Matcher.quoteReplacement(entry.getKey()), entry.getValue());
 		}
 		int offset = input.indexOf("\\xC");
 		if (offset >= 0)
 		{
 			String msg = String.format("Still UTF-8 in input at %1$d see %2$s",
 				offset, input.substring(offset, offset+ 200));
 			System.out.println(msg);
 			logger.error(msg);
 		}
 		return input;
 	}
 	public boolean importString(String yamlContent){
 		Constructor c = new ImportConstructor();
 		TypeDescription descr =
 			new TypeDescription(Address2.class, new Tag(Version.ODDB_VERSION_PREFIX + "::Adress2"));
 		c.addTypeDescription(descr);
 		Yaml yaml = new Yaml(c);
 		int counter = 0;
 		int nrExceptions = 0;
 		Date d1 = new Date(System.currentTimeMillis());
 		for (Object obj : yaml.loadAll(patchUtfEscape(yamlContent))) {
 			Company corp = (Company) obj;
 			companies.put(corp.getEan13(), corp);
 			for (int j = 0; j < corp.getRegistrations().length; j++) {
 				Registration registration = corp.getRegistrations()[j];
 				if (j < 3) {
 					logger.debug(registration.toString());
 				}
 				
 				registrations.put(registration.getIksnr(), registration);
 				Map<String, Sequence> seqs = registration.getSequences();
 				Iterator<Entry<String, Sequence>> it = seqs.entrySet().iterator();
 				while (it.hasNext()) {
 					Entry<String, Sequence> seqIter = it.next();
 					Sequence seq = seqIter.getValue();
 					Iterator<Entry<String, Package>> itP = seq.packages.entrySet().iterator();
 					while (itP.hasNext()) {
 						Entry<String, Package> p = itP.next();
 						Package pack;
 						try {
 							pack = p.getValue();
 							ElexisArtikel a = new ElexisArtikel();
 							if (seq.name_descr != null && seq.name_descr.length() > 0)
 								a.name = seq.name_base + " " + seq.name_descr;
 							else
 								a.name = seq.name_base;
 							a.atc_code = seq.atc_class.code;
 							a.ean13 = pack.ean13;
 							a.pharmacode = pack.pharmacode;
 							a.VKPreis = new Money(pack.price_public);
 							a.EKPreis = new Money(pack.price_exfactory);
 							List<Part> parts = pack.parts;
 							StringBuffer partS = new StringBuffer("");
 							for (int k = 0; k < parts.size(); k++) {
 								Part myPart = parts.get(k);
 								partS.append(myPart.toString());
 							}
							a.partString = parts.toString();
 							articles.add(a);
 							counter++;
 							// logger.debug(a.toString());
 						} catch (Exception e) {
 						nrExceptions++;
 							System.out.println(String.format("\n\n!!!	%1$d: Unexpected class %2$s \n%3$s %4$s\n%5$s\n\n", counter, p.getClass(),
 								obj.toString(), p.toString(), seq.toString()));
 						}
 					}
 				}
 			}
 			logger.info(corp.toString());
 		}
 		Date d2 = new Date(System.currentTimeMillis());
 		long difference = d2.getTime() - d1.getTime();
 		String msg = String
 				.format("Elapsed %1$d.%2$d seconds. Ignoring %3$d exception(s)", difference / 1000, difference % 1000, nrExceptions); 
 		System.out.println(msg);
 		logger.info(msg);
 		logger
 			.info(String
 				.format(
 					"loaded %1$d companies, %2$d adress2 entities, %3$d sequences, %4$d packages and %5$d registrations",
 					companies.size(), Address2.counter, sequences.size(), packages.size(),
 					registrations.size()));
 		logger.info(String.format("loaded %1$d Registration entities", Registration.counter));
 		return true;
 	}
 	
 	public static Map<Integer, Company> convertString(String yamlContent){
 		Map<Integer, Company> result = new HashMap<Integer, Company>();
 		try {
 			Constructor constructor = new Constructor(Company.class);// Car.class is root
 			TypeDescription companyDescription = new TypeDescription(Company.class);
 			companyDescription.putListPropertyType("adresses", Address2.class);
 			constructor.addTypeDescription(companyDescription);
 			Yaml yaml = new Yaml(constructor);
 			for (Object data : yaml.loadAll(yamlContent)) {
 				Company corp = (Company) data;
 				result.put(corp.getOid(), corp);
 			}
 		} catch (Exception e) {
 			logger.error("GotException converting String " + e.getMessage());
 			return null;
 		}
 		return result;
 	}
 }
