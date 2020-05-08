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
 
 import org.junit.Test;
 import uk.ac.ebi.fg.annotare2.magetabcheck.model.sdrf.SdrfPerformerAttribute;
 import uk.ac.ebi.fg.annotare2.magetabcheck.model.sdrf.SdrfProtocolNode;
 
 import static org.easymock.EasyMock.*;
 
 /**
  * @author Olga Melnichuk
  */
 public class ProtocolNodePerformerAttributeTest {
 
     @Test(expected = AssertionError.class)
     public void nullAttributeTest() {
         new SdrfSimpleChecks().protocolNodeMustHavePerformerAttribute(
                 createProtocolNode(null)
         );
     }
 
     @Test(expected = AssertionError.class)
     public void emptyAttributeTest() {
         new SdrfSimpleChecks().protocolNodeMustHavePerformerAttribute(
                 createProtocolNode(createPerformerAttribute(""))
         );
     }
 
     @Test(expected = AssertionError.class)
     public void whitespaceAttributeTest() {
         new SdrfSimpleChecks().protocolNodeMustHavePerformerAttribute(
                 createProtocolNode(createPerformerAttribute(" "))
         );
     }
 
     @Test
     public void nonEmptyAttributeTest() {
         new SdrfSimpleChecks().protocolNodeMustHavePerformerAttribute(
                 createProtocolNode(createPerformerAttribute("test"))
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
 
     private static SdrfProtocolNode createProtocolNode(SdrfPerformerAttribute attribute) {
         SdrfProtocolNode node = createMock(SdrfProtocolNode.class);
         expect(node.getLine()).andReturn(0);
         expect(node.getColumn()).andReturn(0);
         expect(node.getFileName()).andReturn("no file");
         expect(node.getPerformer()).andReturn(attribute);
         replay(node);
         return node;
     }
 
 }
