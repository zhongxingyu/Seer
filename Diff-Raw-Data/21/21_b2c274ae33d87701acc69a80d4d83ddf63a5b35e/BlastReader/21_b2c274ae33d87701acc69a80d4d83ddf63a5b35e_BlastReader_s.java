 /**
  * BlastReader.java
  *
  * 2011.09.19
  *
  * This file is part of the CheMet library
  *
  * The CheMet library is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * CheMet is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with CheMet.  If not, see <http://www.gnu.org/licenses/>.
  */
 package uk.ac.ebi.mdk.io.xml.blast;
 
 import au.com.bytecode.opencsv.CSVReader;
 import org.apache.log4j.Logger;
 import org.codehaus.stax2.XMLInputFactory2;
 import org.codehaus.stax2.XMLStreamReader2;
 import uk.ac.ebi.mdk.domain.entity.AnnotatedEntity;
 import uk.ac.ebi.mdk.domain.entity.GeneProduct;
 import uk.ac.ebi.mdk.domain.observation.sequence.LocalAlignment;
 
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.events.XMLEvent;
import java.io.*;
 import java.security.InvalidParameterException;
import java.util.*;
 
 /**
  * BlastReader – 2011.09.19 <br>
  * Class description
  *
  * @author johnmay
  * @author $Author$ (this version)
  * @version $Rev$ : Last Changed $Date$
  */
 public class BlastReader {
 
     private static final Logger LOGGER = Logger.getLogger(BlastReader.class);
     public Collection<Integer> supportedFormats = new HashSet();
     private Map<String, Integer> columnMap = new HashMap();
 
     public BlastReader() {
         supportedFormats.add(5); // xml
         supportedFormats.add(6); // tsv
 
         int index = 0;
         for (String name : Arrays.asList("Query.ID", "Subject.ID", "Perc.Ident", "Aln.Len",
                                          "Mismatches", "Gap.Openings", "Q.start", "Q.end", "S.start",
                                          "S.end", "Expected", "Bit.Score")) {
             columnMap.put(name, index++);
         }
     }
 
     public void load(Map<String, GeneProduct> products, File outputFile, Integer format, String version, AnnotatedEntity task) throws IOException, XMLStreamException {
 
         LOGGER.debug("parsing blast file: " + outputFile + " outfmt: " + format + " version: " + version);
 
         if (supportedFormats.contains(format) == false) {
             throw new InvalidParameterException("Unsupported format " + format
                                                         + ". Currently supported:" + supportedFormats);
         } else {
             switch (format) {
                 case 5:
                     loadFromXML(products, outputFile.getAbsolutePath(), version, task);
                     break;
                 case 6:
                     loadFromTSV(products, new FileReader(outputFile), version, task);
                     break;
             }
         }
 
     }
 
     /**
      * Loads blast results from TSV format (-m 8)
      *
      * @param products
      * @param reader
      * @param version
     *
      * @throws java.io.IOException
      */
     public void loadFromTSV(Map<String, GeneProduct> products, Reader reader, String version, AnnotatedEntity task) throws IOException {
 
         CSVReader tsvReader = new CSVReader(reader, '\t', '\0');
 
         long start = System.currentTimeMillis();
 
         BLASTRowParser parser = ParserFactory.getInstance().getBLASTRowParser(version);
         String[] row;
         while ((row = tsvReader.readNext()) != null) {
 
             LocalAlignment alignment = parser.parse(row);
             alignment.setSource(task); // set the options on the task
 
             GeneProduct product = products.get(alignment.getQuery());
 
             if (product != null) {
                 // add observation
                 product.addObservation(alignment);
            } else {
                LOGGER.error("unable to find matching product whilst loading");
             }
 
         }
 
         long end = System.currentTimeMillis();
 
         System.out.println("Completed " + (end - start) + " ms");
 
 
         tsvReader.close();
 
     }
 
     public void loadFromXML(Map<String, ? extends AnnotatedEntity> entities, String filename, String version, AnnotatedEntity task) throws XMLStreamException, FileNotFoundException {
 
         LOGGER.info("Begun parsing blast xml output");
 
         XMLInputFactory2 xmlif = (XMLInputFactory2) XMLInputFactory2.newInstance();
         xmlif.setProperty(
                 XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES,
                 Boolean.FALSE);
         xmlif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
         xmlif.setProperty(
                 XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES,
                 Boolean.TRUE);
         xmlif.setProperty(
                 XMLInputFactory.IS_COALESCING,
                 Boolean.FALSE);
         xmlif.configureForLowMemUsage();
 
 
         XMLStreamReader2 xmlr = (XMLStreamReader2) xmlif.createXMLStreamReader(filename, new FileInputStream(filename));
 
         BLASTXMLParser parser = new BLASTXMLParser_V2_2_24();
         int iterations = 0;
         long start = System.currentTimeMillis();
         while (xmlr.hasNext()) {
             int eventType = xmlr.next();
             switch (eventType) {
                 case XMLEvent.START_ELEMENT:
                     if (xmlr.getName().toString().equals("Iteration")) {
                         parser.parse(entities, task, xmlr);
                         iterations++;
                     }
                     break;
             }
         }
         long end = System.currentTimeMillis();
         LOGGER.debug("Completed parsing " + iterations + " iterations in " + (end - start) + " ms ");
     }
 }
