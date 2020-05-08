 /*
  * Copyright (c) 2013. EMBL, European Bioinformatics Institute
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
 
 package uk.ac.ebi.mdk.io.xml.sbml;
 
 import org.junit.Assert;
 import org.junit.Test;
 import uk.ac.ebi.mdk.domain.annotation.crossreference.CrossReference;
 import uk.ac.ebi.mdk.domain.entity.DefaultEntityFactory;
 import uk.ac.ebi.mdk.domain.entity.Metabolite;
 import uk.ac.ebi.mdk.domain.entity.reaction.MetabolicReaction;
 import uk.ac.ebi.mdk.domain.tool.AutomaticCompartmentResolver;
 
 import javax.xml.stream.XMLStreamException;
 import java.io.IOException;
 
 import static org.hamcrest.CoreMatchers.is;
 
 /** @author John May */
 public class SBMLReactionReaderTest {
 
     /**
      * Simple text to check
      *
      * @throws XMLStreamException
      * @throws IOException
      */
     @Test
     public void testSpeciesAnnotations() throws XMLStreamException,
                                                 IOException {
         SBMLReactionReader reader = new SBMLReactionReader(
             getClass().getResourceAsStream("single-species.xml"),
             DefaultEntityFactory.getInstance(),
             new AutomaticCompartmentResolver());
         while (reader.hasNext()) {
             MetabolicReaction reaction = reader.next();
             Metabolite metabolite = reaction.getReactants().get(
                 0).getMolecule();
             Assert.assertFalse(metabolite.getAnnotationsExtending(
                 CrossReference.class).isEmpty());
         }
 
         reader.close();
     }
 
     @Test
    public void testCacacetate() throws XMLStreamException, IOException {
        SBMLReactionReader reader = new SBMLReactionReader(
            getClass().getResourceAsStream("cacacetate.xml"),
            DefaultEntityFactory.getInstance(),
            new AutomaticCompartmentResolver());
        while (reader.hasNext()) {
            MetabolicReaction rxn = reader.next();
        }

        reader.close();
    }

    @Test
     public void inputOfREx() throws XMLStreamException, IOException {
         SBMLReactionReader reader = new SBMLReactionReader(
             getClass().getResourceAsStream("single-species+REx.xml"),
             DefaultEntityFactory.getInstance(),
             new AutomaticCompartmentResolver());
         while (reader.hasNext()) {
             MetabolicReaction reaction = reader.next();
             Assert.assertThat(reaction.getAnnotations().size(), is(1));
         }
 
         reader.close();
     }
 
 }
