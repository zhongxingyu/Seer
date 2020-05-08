 package org.apache.uima.annotator.language;
 
 import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
 import org.apache.uima.test.junit_extension.AnnotatorTester;
 import org.junit.Test;
 
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 /**
  * @author tommaso
  */
 public class SimpleLanguageAnnotatorTest {
   private final String mockedDescPath = "desc/SimpleLanguageAnnotatorAggregateDescriptor.xml";
 
   @Test
   public void testItalianText() {
     try {
       AnnotatorTester annotatorTester = new AnnotatorTester(mockedDescPath);
       String text = "The Ecology of Infectious Diseases program solicitation supports the development of predictive models and the discovery of principles governing the transmission dynamics of infectious disease agents. To that end, research proposals should focus on understanding the ecological and socio-ecological determinants of transmission by vectors or abiotic agents, the population dynamics of reservoir species, the transmission to humans or other hosts, or the cultural, social, behavioral, and economic dimensions of disease communication. Research may be on zoonotic, vector-borne or enteric diseases of either terrestrial, freshwater, or marine systems and organisms, including diseases of non-human animals and plants, at any scale from specific pathogens to inclusive environmental systems. Proposals for research on disease systems of public health concern to developing countries are strongly encouraged. Investigators are encouraged to include links to the public health research community, including for example, participation of epidemiologists, physicians, veterinarians, medical social scientists, medical entomologists, virologists, or parasitologists.";
       testAnnotator(text, annotatorTester, "en");
     } catch (Exception e) {
       e.printStackTrace();
       fail(e.getLocalizedMessage());
     }
   }
 
   @Test
   public void testEnglishText() {
     try {
       AnnotatorTester annotatorTester = new AnnotatorTester(mockedDescPath);
      String text = "La Divisione di Prevenzione e Genetica Oncologica dell?Istituto Europeo di Oncologia bandisce una borsa di studio annuale di 18.000,00 Euro lordi, con possibilit di rinnovo, per lo sviluppo del progetto di ricerca ?Optimizing endocrine therapies for the individual patient with breast cancer: identifying targets for tailored treatment?.";
       testAnnotator(text, annotatorTester, "it");
     } catch (Exception e) {
       e.printStackTrace();
       fail(e.getLocalizedMessage());
     }
   }
 
  private void testAnnotator(String text, AnnotatorTester annotatorTester, String expected) throws Exception, CASException {
     CAS cas = annotatorTester.performTest(text, "x-unspecified");
    assertTrue("I was not expecting "+cas.getDocumentLanguage(),cas.getDocumentLanguage().equals(expected));
   }
 
 }
