 package ru.yandex.qatools.htmlelements.builder;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.TemporaryFolder;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 import java.io.*;
 
 import static org.junit.Assert.assertEquals;
 import static ru.yandex.qatools.htmlelements.builder.HtmlElementTypeSerializer.serialize;
 
 
 public class HtmlElementTypeTest {
 
     private static File getResourceFile(String name) {
         return new File(HtmlElementTypeTest.class.getResource(name).getPath());
     }
 
 
     @Test
     public void buildFromXmlTest() throws IOException, JAXBException {
         Reader reader = new BufferedReader(new FileReader(getResourceFile("/example.xml")));
         Unmarshaller unmarshaller = JAXBContext.newInstance(HtmlElementType.class.getPackage().getName()).createUnmarshaller();
         @SuppressWarnings("unchecked")
         HtmlElementType htmlElementType = ((JAXBElement<HtmlElementType>)unmarshaller.unmarshal(reader)).getValue();
         assertEquals(htmlElementType.getName(), "SimpleBlock");
         assertEquals(htmlElementType.getFindBy(), "xpath=/some/xpath");
     }
 
     @Test
    public void generateSimpleBlockTest() throws IOException {
         String expected = FileUtils.readFileToString(getResourceFile("/ru/yandex/qatools/htmlelements/examples/SimpleBlock.java"));
         HtmlElementType htmlElementType = new HtmlElementType();
         htmlElementType.setName("SimpleBlock");
         htmlElementType.setFindBy("xpath=/some/xpath");
         assertEquals(expected, serialize(htmlElementType));
     }
 
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private static void compareFiles(File file1, File file2) throws IOException {
        String expected = FileUtils.readFileToString(file1);
        String actual = FileUtils.readFileToString(file2);
        assertEquals(expected, actual);
    }

     @Test
    public void saveSimpleBlockTest() throws IOException, JAXBException {
         Reader reader = new BufferedReader(new FileReader(getResourceFile("/example.xml")));
         Unmarshaller unmarshaller = JAXBContext.newInstance(HtmlElementType.class.getPackage().getName()).createUnmarshaller();
         @SuppressWarnings("unchecked")
         HtmlElementType htmlElementType = ((JAXBElement<HtmlElementType>)unmarshaller.unmarshal(reader)).getValue();
         File folder = temporaryFolder.newFolder();
         FileWriter.write(folder, htmlElementType);
         compareFiles(
             getResourceFile("/ru/yandex/qatools/htmlelements/examples/SimpleBlock.java"),
             new File(folder.getAbsolutePath() + "/ru/yandex/qatools/htmlelements/examples/SimpleBlock.java")
         );
     }
 
     @Test
    public void generateBlockWithFields() throws IOException, JAXBException {
         Reader reader = new BufferedReader(new FileReader(getResourceFile("/example2.xml")));
         Unmarshaller unmarshaller = JAXBContext.newInstance(HtmlElementType.class.getPackage().getName()).createUnmarshaller();
         @SuppressWarnings("unchecked")
         HtmlElementType htmlElementType = ((JAXBElement<HtmlElementType>)unmarshaller.unmarshal(reader)).getValue();
         File folder = temporaryFolder.newFolder();
         FileWriter.write(folder, htmlElementType);
         compareFiles(
             getResourceFile("/ru/yandex/qatools/htmlelements/examples/BlockWithFields.java"),
             new File(folder.getAbsolutePath() + "/ru/yandex/qatools/htmlelements/examples/BlockWithFields.java")
         );
         compareFiles(
             getResourceFile("/ru/yandex/qatools/htmlelements/examples/SubBlockWithFields.java"),
             new File(folder.getAbsolutePath() + "/ru/yandex/qatools/htmlelements/examples/SubBlockWithFields.java")
         );
         compareFiles(
             getResourceFile("/ru/yandex/qatools/htmlelements/examples/SubSubBlockWithoutFields.java"),
             new File(folder.getAbsolutePath() + "/ru/yandex/qatools/htmlelements/examples/SubSubBlockWithoutFields.java")
         );
         compareFiles(
             getResourceFile("/ru/yandex/qatools/htmlelements/examples/SubBlockWithoutFields.java"),
             new File(folder.getAbsolutePath() + "/ru/yandex/qatools/htmlelements/examples/SubBlockWithoutFields.java")
         );
     }
 }
