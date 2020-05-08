 package be.example.jasper;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.ResourceBundle;
 
 import net.sf.jasperreports.engine.JRException;
 import net.sf.jasperreports.engine.JRParameter;
 import net.sf.jasperreports.engine.JasperReport;
 import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
 import net.sf.jasperreports.engine.query.JRXPathQueryExecuterFactory;
 import net.sf.jasperreports.engine.util.JRXmlUtils;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.w3c.dom.Document;
 
 import be.example.jasper.domain.Address;
 import be.example.jasper.domain.Person;
 
 public class ReportGeneratorTest {
 
     private static final String SUBREPORT_DIR_FOR_TEST = "src/main/generated-resources/jasper/";
     private ReportGenerator reportGenerator = new ReportGenerator();
     private List<Address> addressList;
     private List<Person> personList;
     private Map<String, Object> params;
    private Locale locale = new Locale("nl");
 
     @Before
     public void setUp() {
         addressList = PersonDataSourceFactory.createAddresses();
         personList = PersonDataSourceFactory.createPersons(addressList);
         params = new HashMap<String, Object>();
         params.put(JRParameter.REPORT_LOCALE, locale);
         params.put(JRParameter.REPORT_RESOURCE_BUNDLE, ResourceBundle.getBundle("i18n.jasper.labels", locale));
         params.put(ReportGenerator.SUBREPORT_DIR_PARAM, SUBREPORT_DIR_FOR_TEST);
     }
 
     @Test
     public void testCreatePdfWithXmlInput() throws JRException, IOException {
         InputStream xmlData = getClass().getResourceAsStream("/data/northwind.xml");
         Document document = JRXmlUtils.parse(xmlData);
         params.put(JRXPathQueryExecuterFactory.PARAMETER_XML_DATA_DOCUMENT, document);
         JasperReport jasperReport = ReportGenerator.getCompiledReport("jasper/CustomersReport.jasper");
         File reportFile = File.createTempFile("report_", ".pdf");
         reportGenerator.exportReport(params, jasperReport, new FileOutputStream(reportFile));
     }
 
     @Test
     public void testCreatePdfWithCollectionInput() throws JRException, IOException {
         JasperReport personReport = ReportGenerator.getCompiledReport("jasper/personReport.jasper");
         File reportFile = File.createTempFile("report_", ".pdf");
         params.put(JRParameter.REPORT_DATA_SOURCE, new JRBeanCollectionDataSource(personList));
         reportGenerator.exportReport(params, personReport, new FileOutputStream(reportFile));
     }
 
 }
