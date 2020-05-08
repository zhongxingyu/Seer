 package org.schematron.filter;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.schematron.model.SchematronResult;
 import org.schematron.model.SchematronResultImpl;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 public class SVRLValidationFilterTest {
 
     @Test
     public void testIncorrectValidationReport() throws IOException, SAXException {
         InputStream inputStream = getClass().getClassLoader().getResourceAsStream("org/schematron/filter/svrl-validationreport-error.xml");
         SchematronResultTransformer validationFilter = new SVRLSchematronResultTransformer();
         SchematronResult result = validationFilter.transform(new InputSource(inputStream));
 
         Assert.assertEquals(result.getFatals().size(), 2);
         Assert.assertEquals("[BIICORE-T10-R000]-This XML instance is NOT a core BiiTrdm010 transaction Warning when performing test [contains(cbc:CustomizationID, 'urn:www.cenbii.eu:transaction:biicoretrdm010:ver1.0')].\n" +
                 "[BIICORE-T10-R001]-An invoice SHOULD not contain empty elements. Warning when performing test [not(count(//*[not(text())]) > 0)].\n" +
                 "[EUGEN-T10-R024]-Currency Identifier MUST be in stated in the currency stated on header level. Fatal error when performing test [not(//@currencyID != //cbc:DocumentCurrencyCode)].\n" +
                 "[NONAT-T10-R001]-The Norwegian legal registration ID for the supplier MUST be provided according to \"FOR 2004-12-01 nr 1558 5-1-1. Point 2\" Fatal error when performing test [(cac:PartyLegalEntity/cbc:CompanyID != '') and (//cac:AccountingSupplierParty/cac:Party/cac:PostalAddress/cac:Country/cbc:IdentificationCode = 'NO') or not((//cac:AccountingSupplierParty/cac:Party/cac:PostalAddress/cac:Country/cbc:IdentificationCode = 'NO'))].\n",
                 result.toString());
 
        Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><schematronResult><errors/><fatals><fatal><description>[EUGEN-T10-R024]-Currency Identifier MUST be in stated in the currency stated on header level. Fatal error when performing test [not(//@currencyID != //cbc:DocumentCurrencyCode)].</description><name>[EUGEN-T10-R024]-Currency Identifier MUST be in stated in the currency stated on header level.</name></fatal><fatal><description>[NONAT-T10-R001]-The Norwegian legal registration ID for the supplier MUST be provided according to \"FOR 2004-12-01 nr 1558 5-1-1. Point 2\" Fatal error when performing test [(cac:PartyLegalEntity/cbc:CompanyID != '') and (//cac:AccountingSupplierParty/cac:Party/cac:PostalAddress/cac:Country/cbc:IdentificationCode = 'NO') or not((//cac:AccountingSupplierParty/cac:Party/cac:PostalAddress/cac:Country/cbc:IdentificationCode = 'NO'))].</description><name>[NONAT-T10-R001]-The Norwegian legal registration ID for the supplier MUST be provided according to \"FOR 2004-12-01 nr 1558 5-1-1. Point 2\"</name></fatal></fatals><warnings><warning><description>[BIICORE-T10-R000]-This XML instance is NOT a core BiiTrdm010 transaction Warning when performing test [contains(cbc:CustomizationID, 'urn:www.cenbii.eu:transaction:biicoretrdm010:ver1.0')].</description><name>[BIICORE-T10-R000]-This XML instance is NOT a core BiiTrdm010 transaction</name></warning><warning><description>[BIICORE-T10-R001]-An invoice SHOULD not contain empty elements. Warning when performing test [not(count(//*[not(text())]) &gt; 0)].</description><name>[BIICORE-T10-R001]-An invoice SHOULD not contain empty elements.</name></warning></warnings></schematronResult>", ((SchematronResultImpl)result).toXml());
     }
 
     @Test
     public void testCorrectValidationReport() throws IOException, SAXException {
         InputStream inputStream = getClass().getClassLoader().getResourceAsStream("org/schematron/filter/svrl-validationreport-ok.xml");
         SchematronResultTransformer validationFilter = new SVRLSchematronResultTransformer();
         SchematronResult result = validationFilter.transform(new InputSource(inputStream));
 
         Assert.assertTrue(result.getErrors().isEmpty());
         Assert.assertTrue(result.getFatals().isEmpty());
     }
 
 }
