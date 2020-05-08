 /*
  * Copyright 2012 EMBL - European Bioinformatics Institute
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or impl
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.ebi.fg.annotare2.magetabcheck.checks.sdrf;
 
import org.easymock.EasyMock;
import org.easymock.IAnswer;
 import org.junit.Test;
 import uk.ac.ebi.fg.annotare2.magetabcheck.checks.idf.AbstractCheckTest;
 import uk.ac.ebi.fg.annotare2.magetabcheck.efo.MageTabCheckEfo;
import uk.ac.ebi.fg.annotare2.magetabcheck.efo.MageTabCheckEfoImpl;
 import uk.ac.ebi.fg.annotare2.magetabcheck.extension.KnownTermSource;
 import uk.ac.ebi.fg.annotare2.magetabcheck.model.idf.Protocol;
 import uk.ac.ebi.fg.annotare2.magetabcheck.model.sdrf.SdrfPerformerAttribute;
 import uk.ac.ebi.fg.annotare2.magetabcheck.model.sdrf.SdrfProtocolNode;
import uk.ac.ebi.fg.annotare2.magetabcheck.efo.EfoService;
import uk.ac.ebi.fg.annotare2.magetabcheck.efo.EfoTerm;
 
 import java.util.Collections;
 
 import static org.easymock.EasyMock.*;
 
 /**
  * @author Olga Melnichuk
  */
 public class ProtocolNodePerformerAttributeTest extends AbstractCheckTest {
 
    private static final String SEQUENCING_PROTOCOL_TYPE = "nucleic acid sequencing protocol";
 
     @Test(expected = AssertionError.class)
     public void nullAttributeSequencingProtocolTest() {
         new SdrfSimpleChecks(mockEfo()).sequencingProtocolNodeMustHavePerformerAttribute(
                 createSequencingProtocolNode(null)
         );
     }
 
     @Test(expected = AssertionError.class)
     public void emptyAttributeSequencingProtocolTest() {
         new SdrfSimpleChecks(mockEfo()).sequencingProtocolNodeMustHavePerformerAttribute(
                 createSequencingProtocolNode(createPerformerAttribute(""))
         );
     }
 
     @Test(expected = AssertionError.class)
     public void whitespaceAttributeSequencingProtocolTest() {
         new SdrfSimpleChecks(mockEfo()).sequencingProtocolNodeMustHavePerformerAttribute(
                 createSequencingProtocolNode(createPerformerAttribute(" "))
         );
     }
 
     @Test
     public void nonEmptyAttributeSequencingProtocolTest() {
         new SdrfSimpleChecks(mockEfo()).sequencingProtocolNodeMustHavePerformerAttribute(
                 createSequencingProtocolNode(createPerformerAttribute("test"))
         );
     }
 
     @Test
     public void nullAttributeNonSequencingProtocolTest() {
         new SdrfSimpleChecks(mockEfo()).sequencingProtocolNodeMustHavePerformerAttribute(
                 createNonSequencingProtocolNode(null)
         );
     }
 
     @Test
     public void emptyAttributeNonSequencingProtocolTest() {
         new SdrfSimpleChecks(mockEfo()).sequencingProtocolNodeMustHavePerformerAttribute(
                 createNonSequencingProtocolNode(createPerformerAttribute(""))
         );
     }
 
     @Test
     public void whitespaceAttributeNonSequencingProtocolTest() {
         new SdrfSimpleChecks(mockEfo()).sequencingProtocolNodeMustHavePerformerAttribute(
                 createNonSequencingProtocolNode(createPerformerAttribute(" "))
         );
     }
 
     @Test
     public void nonEmptyAttributeNonSequencingProtocolTest() {
         new SdrfSimpleChecks(mockEfo()).sequencingProtocolNodeMustHavePerformerAttribute(
                 createNonSequencingProtocolNode(createPerformerAttribute("test"))
         );
     }
 
 
     private static SdrfPerformerAttribute createPerformerAttribute(String value) {
         SdrfPerformerAttribute attr = createMock(SdrfPerformerAttribute.class);
         expect(attr.getLine()).andReturn(0);
         expect(attr.getColumn()).andReturn(0);
         expect(attr.getFileName()).andReturn("no file");
         expect(attr.getValue()).andReturn(value);
         replay(attr);
         return attr;
     }
 
     private static Protocol getProtocol(String protocolType) {
         return createProtocol(
                 "name",
                 "description",
                 Collections.<String>emptyList(),
                 "invalid hardware name",
                 "software",
                 "contact",
                 protocolType,
                 createTermSource(
                         KnownTermSource.EFO.name(),
                         KnownTermSource.EFO.getUrl()));
     }
 
     private static SdrfProtocolNode createNonSequencingProtocolNode(SdrfPerformerAttribute attribute) {
         SdrfProtocolNode node = createMock(SdrfProtocolNode.class);
         expect(node.getLine()).andReturn(0);
         expect(node.getColumn()).andReturn(0);
         expect(node.getFileName()).andReturn("no file");
         expect(node.getPerformer()).andReturn(attribute);
         expect(node.getProtocol()).andReturn(getProtocol(""));
         replay(node);
         return node;
     }
 
     private static SdrfProtocolNode createSequencingProtocolNode(SdrfPerformerAttribute attribute) {
         SdrfProtocolNode node = createMock(SdrfProtocolNode.class);
         expect(node.getLine()).andReturn(0);
         expect(node.getColumn()).andReturn(0);
         expect(node.getFileName()).andReturn("no file");
         expect(node.getPerformer()).andReturn(attribute);
         expect(node.getProtocol()).andReturn(getProtocol(SEQUENCING_PROTOCOL_TYPE));
         replay(node);
         return node;
     }
 
     private static MageTabCheckEfo mockEfo() {
        EfoService mock =  EasyMock.createMock(EfoService.class);
        expect(mock.findTermByLabelOrAccession(isA(String.class), isA(String.class), isA(String.class))).andAnswer(
                new IAnswer<EfoTerm>() {
                    @Override
                    public EfoTerm answer() throws Throwable {
                        String title = (String) getCurrentArguments()[0];
                        return SEQUENCING_PROTOCOL_TYPE.equals(title) ? new EfoTerm("", "", "", false, Collections.<String>emptyList()) : null;
                    }
                }
        );
         replay(mock);
        return new MageTabCheckEfoImpl(mock);
     }
 
 }
