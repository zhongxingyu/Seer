 package dk.statsbiblioteket.doms.ingest.reklamepbcoremapper;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 import dk.statsbiblioteket.util.Files;
 import dk.statsbiblioteket.util.xml.DOM;
 import dk.statsbiblioteket.util.xml.XPathSelector;
 
 import javax.xml.transform.TransformerException;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import java.util.TimeZone;
 
 /**
  * Map all tv2 reklamefilm data to PBCore.
  */
 public class Tv2PBCoreMapper {
     private static final SimpleDateFormat ALTERNATIVE_INPUT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM");
     private static final SimpleDateFormat ALTERNATIVE_OUTPUT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM");
     private static final SimpleDateFormat INPUT_DATE_FORMAT = new SimpleDateFormat("yyMMdd");
     private static final SimpleDateFormat OUTPUT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-ddZ");
     private static final SimpleDateFormat DURATION_FORMAT = new SimpleDateFormat("HH:mm:ss");
     static {
         DURATION_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
     }
 
     private static class MappingTuple {
         enum Type {STRING, DURATION, LANGUAGE, DATE, LANGUAGEEXTENSION, FILE}
 
         final int cellindex;
         final String xpath;
         final Type type;
 
         private MappingTuple(int cellindex, String xpath, Type type) {
             this.cellindex = cellindex;
             this.xpath = xpath;
             this.type = type;
         }
     }
 
     private final List<MappingTuple> pbcoreTv2TemplateMappingTuples = new ArrayList<MappingTuple>(Arrays.asList(
             new MappingTuple(1, "/p:PBCoreDescriptionDocument/p:pbcoreSubject[1]/p:subject",
                              Tv2PBCoreMapper.MappingTuple.Type.STRING),
             new MappingTuple(2, "/p:PBCoreDescriptionDocument/p:pbcoreSubject[2]/p:subject",
                              Tv2PBCoreMapper.MappingTuple.Type.STRING),
             new MappingTuple(3, "/p:PBCoreDescriptionDocument/p:pbcoreTitle[1]/p:title",
                              Tv2PBCoreMapper.MappingTuple.Type.STRING),
             new MappingTuple(4, "/p:PBCoreDescriptionDocument/p:pbcoreInstantiation/p:formatDuration",
                              Tv2PBCoreMapper.MappingTuple.Type.DURATION),
             new MappingTuple(5, "/p:PBCoreDescriptionDocument/p:pbcoreTitle[2]/p:title",
                              Tv2PBCoreMapper.MappingTuple.Type.STRING),
             new MappingTuple(6, "/p:PBCoreDescriptionDocument/p:pbcoreInstantiation/p:dateIssued",
                              Tv2PBCoreMapper.MappingTuple.Type.DATE),
             new MappingTuple(7, "/p:PBCoreDescriptionDocument/p:pbcoreCreator[1]/p:creator",
                              Tv2PBCoreMapper.MappingTuple.Type.STRING),
             new MappingTuple(8, "/p:PBCoreDescriptionDocument/p:pbcoreCreator[2]/p:creator",
                              Tv2PBCoreMapper.MappingTuple.Type.STRING),
             new MappingTuple(9, "/p:PBCoreDescriptionDocument/p:pbcoreCreator[3]/p:creator",
                              Tv2PBCoreMapper.MappingTuple.Type.STRING),
             new MappingTuple(10, "/p:PBCoreDescriptionDocument/p:pbcoreInstantiation/p:language",
                              Tv2PBCoreMapper.MappingTuple.Type.LANGUAGE),
             new MappingTuple(10, "/p:PBCoreDescriptionDocument/p:pbcoreExtension/p:extension",
                              Tv2PBCoreMapper.MappingTuple.Type.LANGUAGEEXTENSION),
             new MappingTuple(11, "/p:PBCoreDescriptionDocument/p:pbcoreIdentifier/p:identifier",
                              Tv2PBCoreMapper.MappingTuple.Type.STRING),
             new MappingTuple(11, "/p:PBCoreDescriptionDocument/p:pbcoreInstantiation/p:pbcoreFormatID/p:formatIdentifier",
                              Tv2PBCoreMapper.MappingTuple.Type.STRING),
             new MappingTuple(11, "/p:PBCoreDescriptionDocument/p:pbcoreInstantiation/p:formatLocation",
                              Tv2PBCoreMapper.MappingTuple.Type.FILE)));
 
 
     public void mapCsvDataToPBCoreFiles(File csvFile, File outputdir) throws IOException {
         List<List<String>> csvData = CsvParser.readCsvData(Files.loadString(csvFile));
         mapCsvDataToPBCoreFiles(csvData, outputdir);
     }
 
     private void mapCsvDataToPBCoreFiles(List<List<String>> csvData, File outputdir) {
         for (List<String> row : csvData) {
             try {
                 mapCsvRowToPBCoreFile(row, outputdir);
             } catch (Exception e) {
                 //TODO logging
                 e.printStackTrace(System.err);
             }
         }
     }
 
     private void mapCsvRowToPBCoreFile(List<String> row, File outputdir)
             throws ParseException, IOException, TransformerException {
         // Initiate pbcore template
         Document pbcoreDocument = DOM
                 .streamToDOM(getClass().getClassLoader().getResourceAsStream("pbcoretv2template.xml"), true);
         XPathSelector xPath = DOM.createXPathSelector("p", "http://www.pbcore.org/PBCore/PBCoreNamespace.html");
         // Inject data
         for (MappingTuple pbcoreTv2TemplateMappingTuple : pbcoreTv2TemplateMappingTuples) {
             String value = row.get(pbcoreTv2TemplateMappingTuple.cellindex);
             switch (pbcoreTv2TemplateMappingTuple.type) {
                 case DATE:
                     if (value != null && !value.isEmpty()) {
                        value = OUTPUT_DATE_FORMAT.format(INPUT_DATE_FORMAT.parse(value));
                     } else {
                         // Fall back to month date
                         value = ALTERNATIVE_OUTPUT_DATE_FORMAT.format(ALTERNATIVE_INPUT_DATE_FORMAT.parse(row.get(0)));
                     }
                     break;
                 case DURATION:
                     value = DURATION_FORMAT.format(new Date(Long.parseLong(value) * 1000L));
                     break;
                 case FILE:
                     value += ".mpg";
                     break;
                 case LANGUAGE:
                     if (value.equals("Dansk")) {
                         value = "dan";
                     } else if (value.equals("Ukendt")) {
                         value = "und";
                     } else {
                         value = "mis";
                         // TODO add more languages, what to do with "Udenlandsk" and "Versioneret"
                     }
                     break;
                 case LANGUAGEEXTENSION:
                     value = "locationoforiginalproduction: " + value;
                 default:
                     break;
             }
             Node node = xPath.selectNode(pbcoreDocument, pbcoreTv2TemplateMappingTuple.xpath);
             node.setTextContent(value);
         }
 
         //Write pbcore to template with file name
         new FileOutputStream(new File(outputdir, row.get(11) + ".xml")).write(DOM.domToString(pbcoreDocument, true).getBytes());
     }
 }
