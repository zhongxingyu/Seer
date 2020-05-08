 /*
  * Copyright (c) 2013. John May <jwmay@users.sf.net>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.ebi.mdk.io.xml.hmdb;
 
 import org.junit.Test;
 import uk.ac.ebi.mdk.io.xml.hmdb.marshal.HMDBDefaultMarshals;
 import uk.ac.ebi.mdk.io.xml.hmdb.marshal.HMDBXMLMarshal;
 
 import javax.xml.stream.XMLStreamException;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 /**
  * Test for the HMDB Parser, note isolated parsing tests are located in {@link
  * uk.ac.ebi.mdk.io.xml.hmdb.marshal.HMDBDefaultMarshals}
  *
  * @author John May
  * @see uk.ac.ebi.mdk.io.xml.hmdb.marshal.HMDBDefaultMarshalsTest
  */
 public class HMDBParserTest {
 
     @Test public void testLoad_HMDB00001() throws IOException {
 
         File file = new File(getClass().getResource("HMDB00001.xml").getFile());
         HMDBMetabolite metabolite = HMDBParser.load(file);
 
         assertThat(metabolite.getAccession(), is("HMDB00001"));
         assertThat(metabolite.getCommonName(), is("1-Methylhistidine"));
 
         assertThat(metabolite.getInChI(), is("InChI=1S/C7H11N3O2/c1-10-3-5(9-4-10)2-6(8)7(11)12/h3-4,6H,2,8H2,1H3,(H,11,12)/t6-/m0/s1"));
         assertThat(metabolite.getSMILES(), is("CN1C=NC(C[C@H](N)C(O)=O)=C1"));
 
     }
 
     @Test public void testLoad_HMDB00001_NameOnly() throws IOException {
 
         File file = new File(getClass().getResource("HMDB00001.xml").getFile());
         HMDBMetabolite metabolite = HMDBParser.load(file,
                                                     HMDBDefaultMarshals.COMMON_NAME);
 
         assertThat(metabolite.getAccession(), is(""));
         assertThat(metabolite.getCommonName(), is("1-Methylhistidine"));
 
         assertThat(metabolite.getInChI(), is(""));
         assertThat(metabolite.getSMILES(), is(""));
 
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testLoad_Null() throws IOException {
         HMDBParser.load(null);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testLoad_MissingFile() throws IOException {
         HMDBParser.load(new File(""));
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testLoad_None() throws IOException {
         File file = new File(getClass().getResource("HMDB00000.xml").getFile());
         HMDBParser.load(file);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testLoad_Multiple() throws IOException {
         File file = new File(getClass().getResource("HMDB00001-HMDB00005.xml").getFile());
         HMDBParser.load(file);
     }
 
     @Test public void testLoadAll() throws IOException {
         File file = new File(getClass().getResource("HMDB00001-HMDB00005.xml").getFile());
         Collection<HMDBMetabolite> entries = HMDBParser.loadAll(file,
                                                                 HMDBDefaultMarshals.values());
         assertThat(entries.size(), is(3));
         List<HMDBMetabolite> metabolites = new ArrayList<HMDBMetabolite>(entries);
         assertThat(metabolites.get(0).getAccession(), is("HMDB00001"));
         assertThat(metabolites.get(1).getAccession(), is("HMDB00002"));
         assertThat(metabolites.get(2).getAccession(), is("HMDB00005"));
     }
 
     @Test public void testLoadAll_Empty() throws IOException {
         File file = new File(getClass().getResource("HMDB00000.xml").getFile());
         Collection<HMDBMetabolite> entries = HMDBParser.loadAll(file, HMDBDefaultMarshals.values());
         assertThat(entries.size(), is(0));
     }
 
     @Test public void testLoadDirectory() throws IOException {
         File dir = new File(getClass().getResource("dir").getFile());
        Iterator<HMDBMetabolite> entries = HMDBParser.loadDirectory(dir);
        assertTrue(entries.hasNext());
        assertThat(entries.next().getAccession(), is("HMDB00001"));
        assertTrue(entries.hasNext());
        assertThat(entries.next().getAccession(), is("HMDB00002"));
        assertTrue(entries.hasNext());
        assertThat(entries.next().getAccession(), is("HMDB00005"));
        assertFalse(entries.hasNext());
     }
 
     @Test public void testLoadDirectory_NameOnly() throws IOException {
         File dir = new File(getClass().getResource("dir").getFile());
         Iterator<HMDBMetabolite> entries = HMDBParser.loadDirectory(dir,
                                                                     HMDBDefaultMarshals.COMMON_NAME);
         HMDBMetabolite entry;
         assertTrue(entries.hasNext());
         entry = entries.next();
         assertThat(entry.getAccession(), is(""));
         assertThat(entry.getCommonName(), is("1-Methylhistidine"));
         assertTrue(entries.hasNext());
         entry = entries.next();
         assertThat(entry.getAccession(), is(""));
         assertThat(entry.getCommonName(), is("1,3-Diaminopropane"));
         assertTrue(entries.hasNext());
         entry = entries.next();
         assertThat(entry.getAccession(), is(""));
         assertThat(entry.getCommonName(), is("2-Ketobutyric acid"));
         assertFalse(entries.hasNext());
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testConstructorNull() throws IOException,
                                              XMLStreamException {
         new HMDBParser(null);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testConstructorNoMarshals() throws IOException,
                                                    XMLStreamException {
         new HMDBParser(null, new HMDBXMLMarshal[0]);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testConstructorNulLMarshal() throws IOException,
                                                     XMLStreamException {
         new HMDBParser(null, new HMDBXMLMarshal[]{null});
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testConstructor_AccessionOnly() throws IOException,
                                                        XMLStreamException {
         File file = new File(getClass().getResource("HMDB00000.xml").getFile());
         new HMDBParser(new FileReader(file), HMDBDefaultMarshals.ACCESSION);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testConstructor_AccessionOnly_WithSecondary() throws
                                                               IOException,
                                                               XMLStreamException {
         File file = new File(getClass().getResource("HMDB00000.xml").getFile());
         new HMDBParser(new FileReader(file), HMDBDefaultMarshals.ACCESSION,
                        HMDBDefaultMarshals.SECONDARY_ACCESSION);
     }
 
     // intended typo (it is in the xml)
     @Test(expected = IllegalArgumentException.class)
     public void testConstructor_AccessionOnly_WithSecoundary() throws
                                                                IOException,
                                                                XMLStreamException {
         File file = new File(getClass().getResource("HMDB00000.xml").getFile());
         new HMDBParser(new FileReader(file), HMDBDefaultMarshals.ACCESSION,
                        HMDBDefaultMarshals.SECOUNDARY_ACCESSION);
     }
 
     @Test public void testConstructor_AccessionOnly_Valid() throws
                                                             IOException,
                                                             XMLStreamException {
         File file = new File(getClass().getResource("HMDB00000.xml").getFile());
         new HMDBParser(new FileReader(file), HMDBDefaultMarshals.ACCESSION,
                        HMDBDefaultMarshals.SECONDARY_ACCESSION,
                        HMDBDefaultMarshals.SECOUNDARY_ACCESSION);
     }
 
 }
