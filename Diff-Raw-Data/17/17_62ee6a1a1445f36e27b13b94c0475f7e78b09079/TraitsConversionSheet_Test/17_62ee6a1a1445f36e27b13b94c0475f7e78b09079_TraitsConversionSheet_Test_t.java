 package net.sf.anathema.tools.conversion;
 
 import static net.sf.anathema.test.xml.XMLUnitUtils.*;
 
 import java.io.File;
 
 import org.junit.Before;
 import org.junit.Test;
 
 @SuppressWarnings("nls")
 public class TraitsConversionSheet_Test {
 
   private File sheet;
 
   @Before
   public void testname() throws Exception {
     sheet = new File("src/net/sf/anathema/tools/conversion/charmTraits.xslt");
   }
 
   @Test
   public void getsRidOfAllTheGarbage() throws Exception {
     String xml = "<charmlist>"
         + "<charm id=\"Dragon-Blooded.DragonGracedArrow\" exalt=\"Dragon-Blooded\" group=\"Archery\">"
         + " <prerequisite>"
         + "  <trait id=\"Archery\" value=\"3\"/>"
         + "  <essence value=\"2\"/>"
         + " </prerequisite>"
         + " <cost>"
         + "   <essence cost=\"1\"/>"
         + " </cost>"
         + " <duration duration=\"Instant\"/>"
         + " <charmtype type=\"Supplemental\"/>"
         + " <charmAttribute attribute=\"Combo-OK\" visualize=\"true\"/>"
         + " <charmAttribute attribute=\"Elemental\" visualize=\"true\"/>"
         + " <charmAttribute attribute=\"Obvious\" visualize=\"true\"/>"
         + "  <source source=\"DragonBlooded2nd\"/>"
         + "</charm>"
         + "</charmlist>";
     String expectedResult = "<charmTraitsList>"
         + "<charmTraits charmId=\"Dragon-Blooded.DragonGracedArrow\" essenceMinimum=\"2\" primaryMinimum=\"3\"/>"
         + "</charmTraitsList>";
     assertTransformationConvertsSimilar(xml, expectedResult, sheet);
   }
 
   @Test
   public void doesNotConvertPrimaryMinimumIfDefault() throws Exception {
     String xml = "<charmlist>"
         + "<charm id=\"charmId\">"
         + " <prerequisite>"
         + "  <trait id=\"Archery\" value=\"1\"/>"
         + "  <essence value=\"2\"/>"
         + " </prerequisite>"
         + "</charm>"
         + "</charmlist>";
     String expectedResult = "<charmTraitsList>"
         + "  <charmTraits charmId=\"charmId\" essenceMinimum=\"2\"/>"
         + "</charmTraitsList>";
     assertTransformationConvertsSimilar(xml, expectedResult, sheet);
   }
 
   @Test
   public void doesNotConvertEssenceMiniumIfDefault() throws Exception {
     String xml = "<charmlist>"
         + "<charm id=\"charmId\">"
         + " <prerequisite>"
         + "  <trait id=\"Archery\" value=\"2\"/>"
         + "  <essence value=\"1\"/>"
         + " </prerequisite>"
         + "</charm>"
         + "</charmlist>";
     String expectedResult = "<charmTraitsList>"
         + "<charmTraits charmId=\"charmId\" primaryMinimum=\"2\"/>"
         + "</charmTraitsList>";
     assertTransformationConvertsSimilar(xml, expectedResult, sheet);
   }
 
   @Test
   public void doesNotConvertDefaultCharm() throws Exception {
     String xml = "<charmlist>"
         + "<charm id=\"charmId\">"
         + " <prerequisite>"
         + "  <trait id=\"Archery\" value=\"1\"/>"
         + "  <essence value=\"1\"/>"
         + " </prerequisite>"
         + "</charm>"
         + "</charmlist>";
     String expectedResult = "<charmTraitsList/>";
     assertTransformationConvertsSimilar(xml, expectedResult, sheet);
   }

  @Test
  public void choosesFirstValueOfMultipleTraits() throws Exception {
    String xml = "<charmlist>"
        + "<charm id=\"charmId\">"
        + " <prerequisite>"
        + "  <trait id=\"Archery\" value=\"2\"/>"
        + "  <trait id=\"Melee\" value=\"4\"/>"
        + "  <essence value=\"3\"/>"
        + " </prerequisite>"
        + "</charm>"
        + "</charmlist>";
    String expectedResult = "<charmTraitsList>"
        + "<charmTraits charmId=\"charmId\" essenceMinimum=\"3\" primaryMinimum=\"2\"/>"
        + "</charmTraitsList>";
    assertTransformationConvertsSimilar(xml, expectedResult, sheet);
  }
 }
