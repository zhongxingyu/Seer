 package org.lareferencia.backend;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.lareferencia.backend.harvester.HarvesterRecord;
 import org.lareferencia.backend.transformer.ITransformer;
 import org.lareferencia.backend.util.MedatadaDOMHelper;
 import org.lareferencia.backend.validator.FieldValidator;
 import org.lareferencia.backend.validator.LengthContentValidationRule;
 import org.lareferencia.backend.validator.ControlledValueContentValidationRule;
 import org.lareferencia.backend.validator.IContentValidationRule;
 import org.lareferencia.backend.validator.IValidator;
 import org.lareferencia.backend.validator.RegexContentValidationRule;
 import org.lareferencia.backend.validator.ValidatorImpl;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 @ContextConfiguration
 @RunWith(SpringJUnit4ClassRunner.class)
 public class ValidatorTests {
 	
 	static String xmlstring = "<metadata xmlns=\"http://www.openarchives.org/OAI/2.0/\">" +
 			"<oai_dc:dc xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd\">" +
 			"<dc:title xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">Discovering geographic services from textual use cases</dc:title>" +
 			"<dc:identifier xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">http://sedici.unlp.edu.ar/handle/10915/9670</dc:identifier>" +
 			"<dc:type xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">info:eu-repo/semantics/article</dc:type>" +
 			"<dc:type xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">info:eu-repo/semantics/publishedVersion</dc:type>" +
 			"</oai_dc:dc>" +
 			"</metadata>";
 	
 	
 	static String validRecord = "<metadata xmlns=\"http://www.openarchives.org/OAI/2.0/\">" +
 			"<oai_dc:dc xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd\">" +
 			"<dc:creator xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">Grieco y Bavio, Alfredo</dc:creator>" +
 			"<dc:title xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">Nec Substantiam sed Culpam : el cervantismo alegórico de Alicia Parodi y el caso del Licenciado Vidriera</dc:title>" +
 			"<dc:subject xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">Humanidades</dc:subject>" +
 			"<dc:subject xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">Letras</dc:subject>" +
 			"<dc:subject xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">Literatura</dc:subject>" +
 			"<dc:subject xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">Crítica literaria</dc:subject>" +
 			"<dc:subject xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">Cervantes; novelas ejemplares; alegoría; crítica textual</dc:subject>" +
 			"<dc:subject xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">Cervantes; allegory; text critique</dc:subject>" +
 			"<dc:language xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">es</dc:language>" +
 			"<dc:date xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">2005</dc:date>" +
			"<dc:identifier xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">http://sedici.unlp.edu.ar:8080/handle/10915/12185</dc:identifier>" +
			"<dc:identifier xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">http://hdl.handle.net/10915/12185@5</dc:identifier>" +
 			"<dc:identifier xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">http://www.memoria.fahce.unlp.edu.ar/art_revistas/pr.3374/pr.3374.pdf</dc:identifier>" +
 			"<dc:relation xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">Olivar</dc:relation>" +
 			"<dc:relation xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">vol. 6, no. 6</dc:relation>" +
 			"<dc:description xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">Alicia Parodi encuentra en el sentido alegórico de Cervantes la historia de la salvación. Su crítica se define como alegórica. No obstante, el interés y la preocupación por la crítica textual está presente en todos sus textos.</dc:description>" +
 			"<dc:description xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">Número monográfico. El cervantismo argentino : una historia tentativa</dc:description>" +
 			"<dc:description xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">Monographic issue. El cervantismo argentino : una historia tentativa</dc:description>" +
 			"<dc:description xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">Volume monográfico. El cervantismo argentino : una historia tentativa</dc:description>" +
 			"<dc:description xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">Centro de Estudios de Teoría y Crítica Literaria</dc:description>" +
 			"<dc:type xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">info:eu-repo/semantics/article</dc:type>" +
 			"<dc:type xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">artículo</dc:type>" +
 			"<dc:type xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">info:eu-repo/semantics/publishedVersion</dc:type>" +
 			"<dc:type xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">Articulo</dc:type>" +
 			"<dc:date xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">2005</dc:date>" +
 			"<dc:rights xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">info:eu-repo/semantics/openAccess</dc:rights>" +
 			"<dc:format xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">p. 115-134</dc:format>" +
 			"</oai_dc:dc>" +
 			"</metadata>";
 	
 	
 	static String invalidRecord = "<metadata xmlns=\"http://www.openarchives.org/OAI/2.0/\">" +
 			"<oai_dc:dc xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd\">" +
 			"<dc:creator xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">Grieco y Bavio, Alfredo</dc:creator>" +
 			"<dc:title xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">Nec Substantiam sed Culpam : el cervantismo alegórico de Alicia Parodi y el caso del Licenciado Vidriera</dc:title>" +
 			"<dc:subject xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">Humanidades</dc:subject>" +
 			"<dc:subject xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">Letras</dc:subject>" +
 			"<dc:subject xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">Literatura</dc:subject>" +
 			"<dc:subject xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">Crítica literaria</dc:subject>" +
 			"<dc:subject xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">Cervantes; novelas ejemplares; alegoría; crítica textual</dc:subject>" +
 			"<dc:subject xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">Cervantes; allegory; text critique</dc:subject>" +
 			"<dc:language xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">es</dc:language>" +
 			"<dc:date xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">2005</dc:date>" +
 			"<dc:identifier xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">http://sedici.unlp.edu.ar/handle/10915/12185</dc:identifier>" +
 			"<dc:identifier xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">http://hdl.handle.net/10915/12185</dc:identifier>" +
 			"<dc:identifier xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">http://www.memoria.fahce.unlp.edu.ar/art_revistas/pr.3374/pr.3374.pdf</dc:identifier>" +
 			"<dc:relation xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">Olivar</dc:relation>" +
 			"<dc:relation xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">vol. 6, no. 6</dc:relation>" +
 			"<dc:description xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">Alicia Parodi encuentra en el sentido alegórico de Cervantes la historia de la salvación. Su crítica se define como alegórica. No obstante, el interés y la preocupación por la crítica textual está presente en todos sus textos.</dc:description>" +
 			"<dc:description xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">Número monográfico. El cervantismo argentino : una historia tentativa</dc:description>" +
 			"<dc:description xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">Monographic issue. El cervantismo argentino : una historia tentativa</dc:description>" +
 			"<dc:description xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">Volume monográfico. El cervantismo argentino : una historia tentativa</dc:description>" +
 			"<dc:description xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">Centro de Estudios de Teoría y Crítica Literaria</dc:description>" +
 			//"<dc:type xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">info:eu-repo/semantics/article</dc:type>" +
 			"<dc:type xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">doctoralthesis</dc:type>"+
 			"<dc:type xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">artículo</dc:type>" +
 			"<dc:type xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">published</dc:type>" +
 			//"<dc:type xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">Articulo</dc:type>" +
 			"<dc:date xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">2005</dc:date>" +
 			"<dc:rights xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">info:eu-repo/semantics/openAccess</dc:rights>" +
 			"<dc:format xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.driver-repository.eu/\">p. 115-134</dc:format>" +
 			"</oai_dc:dc>" +
 			"</metadata>";
 	
 	@Autowired
 	IValidator validator;
 	
 	@Autowired
 	ITransformer transformer;
 	
 	@Test
 	public void testLengthRule() throws Exception {
 		
 		
 		Document doc = MedatadaDOMHelper.parseXML(xmlstring);
 		
 		HarvesterRecord record = new HarvesterRecord("dumyid",doc);
 		
 		LengthContentValidationRule clrule = new LengthContentValidationRule();
 		clrule.setMinLength(2);
 		clrule.setMaxLength(100);
 	
 		
 		for (String content: record.getFieldOcurrences("dc:type")) {	
 			assertTrue("dc:type  " + clrule, clrule.validate(content).isValid() == content.length() >= 2 && content.length() <= 100  );
 			System.out.println( clrule.validate(content) );
 		}
 		
 		for (String content: record.getFieldOcurrences("dc:title")) {	
 			assertTrue("dc:title  " + clrule, clrule.validate(content).isValid() == content.length() >= 2 && content.length() <= 100  );
 			System.out.println( clrule.validate(content) );
 
 		}
 	
 	}
 	
 	
 	@Test
 	public void testControlledValueRule() throws Exception {
 		
 		
 		Document doc = MedatadaDOMHelper.parseXML(xmlstring);
 		
 		HarvesterRecord record = new HarvesterRecord("dumyid",doc);
 		
 		ControlledValueContentValidationRule ccrule = new ControlledValueContentValidationRule();
 
 		ArrayList<String> cclist = new ArrayList<String>();
 		cclist.add("info:eu-repo/semantics/publishedVersion");
 		cclist.add("info:eu-repo/semantics/article");
 		
 		
 		ccrule.setControlledValues(cclist);
 		for (String content: record.getFieldOcurrences("dc:type")) {	
 			assertTrue("dc:type  " + ccrule, ccrule.validate(content).isValid()  );
 			System.out.println( ccrule.validate(content) );
 
 		}
 		
 		for (String content: record.getFieldOcurrences("dc:title")) {	
 			assertFalse("dc:type  " + ccrule, ccrule.validate(content).isValid()  );
 			System.out.println( ccrule.validate(content) );
 
 		}
 	}
 	
 	@Test
 	public void testRegexRule() throws Exception {
 			
 		Document doc = MedatadaDOMHelper.parseXML(xmlstring);
 		
 		HarvesterRecord record = new HarvesterRecord("dumyid",doc);
 		
 		RegexContentValidationRule rerule = new RegexContentValidationRule();
 
 		rerule.setRegexString("info.*");
 		for (String content: record.getFieldOcurrences("dc:type")) {	
 			assertTrue("dc:type  " + rerule, rerule.validate(content).isValid()  );
 			System.out.println( rerule.validate(content) );		
 		}
 		
 		rerule.setRegexString("noamatchexpresion");
 		for (String content: record.getFieldOcurrences("dc:type")) {	
 			assertFalse("dc:type  " + rerule, rerule.validate(content).isValid()  );
 			System.out.println( rerule.validate(content) );
 		}
 		
 		
		rerule.setRegexString("^(http|https)\\://((?!hdl\\.handle\\.net/123456789$)[a-zA-Z0-9/\\-\\.\\_\\?\\=\\&\\;\\:\\@])+");
 		assertTrue(rerule.validate("http://hdl.handle.net/10915/12185").isValid()  );
		assertTrue(rerule.validate("https://server.com:8080/janium-bin/janium_zui.pl?jzd=/janium/fotos/bpp-f-009/0502.jzd&amp;amp;fn=8502").isValid());
 		assertFalse(rerule.validate("http://hdl.handle.net/123456789").isValid()  );
		assertTrue(rerule.validate("http://www.maxwell.lambda.ele.puc-rio.br/Busca_etds.php?strSecao=resultado&nrSeq=11212@1").isValid());
 				
 		rerule.setRegexString("(^\\d{4}$)|(^\\d{4}-\\d{2}$)|(^\\d{4}-\\d{2}-\\d{2}$)|(^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}([+-]\\d{2}:\\d{2}|Z)$)");
 		assertTrue(rerule.validate("2000").isValid()  );
 		assertTrue(rerule.validate("2000-02").isValid()  );
 		assertTrue(rerule.validate("2000-02-02").isValid()  );
 		assertFalse(rerule.validate("200").isValid()  );
 		assertFalse(rerule.validate("200a-02-02").isValid()  );	
		
		
		
		
 	}
 	
 	@Test
 	public void testFieldValidator() throws Exception {
 		
 		Document doc = MedatadaDOMHelper.parseXML(xmlstring);
 		HarvesterRecord record = new HarvesterRecord("dumyid",doc);
 	
 		ArrayList<String> typeList1 = new ArrayList<String>();
 		typeList1.add("info:eu-repo/semantics/article");
 		
 		ArrayList<String> typeList2 = new ArrayList<String>();
 		typeList2.add("info:eu-repo/semantics/publishedVersion");
 				
 		FieldValidator fvalidator = new FieldValidator("dc:type",true);
 		fvalidator.getContentRules().add(  new ControlledValueContentValidationRule(typeList1, IContentValidationRule.QUANTIFIER_ONE_OR_MORE) );
 		fvalidator.getContentRules().add(  new ControlledValueContentValidationRule(typeList2, IContentValidationRule.QUANTIFIER_ONE_OR_MORE) );
 	
 		System.out.println( fvalidator.validate(record) );		
 	}
 	
 
 	
 	@Test
 	public void testRecordValidator() throws Exception {
 		
 		Document doc = MedatadaDOMHelper.parseXML(xmlstring);
 		HarvesterRecord record = new HarvesterRecord("dumyid",doc);
 	
 		ArrayList<String> typeList1 = new ArrayList<String>();
 		typeList1.add("info:eu-repo/semantics/article");
 		
 		ArrayList<String> typeList2 = new ArrayList<String>();
 		typeList2.add("info:eu-repo/semantics/publishedVersion");
 				
 		FieldValidator type_validator = new FieldValidator("dc:type",true);
 		type_validator.getContentRules().add( new ControlledValueContentValidationRule(typeList1, IContentValidationRule.QUANTIFIER_ONE_OR_MORE) );
 		type_validator.getContentRules().add( new ControlledValueContentValidationRule(typeList2, IContentValidationRule.QUANTIFIER_ONE_OR_MORE));
 	
 		FieldValidator identifier_validator = new FieldValidator("dc:identifier",true);
 		identifier_validator.getContentRules().add( new RegexContentValidationRule("^http.*",IContentValidationRule.QUANTIFIER_ONE_OR_MORE) );
 		
 		
 		IValidator validator = new ValidatorImpl();
 		validator.getFieldValidators().add( type_validator );
 		validator.getFieldValidators().add(identifier_validator);
 
 		
 		System.out.println( validator.validate(record) );		
 	}
 	
 	@Test
 	public void testRecordWiredDriverValidator() throws Exception {
 		
 		Document doc = MedatadaDOMHelper.parseXML(validRecord);
 		HarvesterRecord record = new HarvesterRecord("dumyid",doc);
 		
 		System.out.println( validator.validate(record) );	
 		assertTrue( validator.validate(record).isValid() );		
 
 	}
 	
 	@Test
 	public void testRecordWiredDriverTransformer() throws Exception {
 		
 		Document doc = MedatadaDOMHelper.parseXML(invalidRecord);
 		HarvesterRecord record = new HarvesterRecord("dumyid",doc);
 		
 		assertFalse( validator.validate(record).isValid() );		
 		
 		record = transformer.transform(record);
 		
 		System.out.println( validator.validate(record) );	
 
 		assertTrue( validator.validate(record).isValid() );		
 		
 		System.out.println( MedatadaDOMHelper.Node2XMLString(record.getMetadataDOMnode()) );
 		
 	}
 	
 	
 	
 }
