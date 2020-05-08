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
 
 package uk.ac.ebi.mdk.domain.identifier.basic;
 
 import java.io.IOException;
 import java.io.ObjectInput;
 
 import org.apache.log4j.Logger;
 import uk.ac.ebi.mdk.lang.annotation.Brief;
 import uk.ac.ebi.mdk.lang.annotation.Description;
 import uk.ac.ebi.mdk.domain.identifier.AbstractIdentifier;
 
 /**
  *          ChromosomeNumber - 2011.10.17 <br>
  *          A simple chromosome identifier which is a number
  * @version $Rev$ : Last Changed $Date$
  * @author  johnmay
  * @author  $Author$ (this version)
  */
 @Brief("Chromosome")
 @Description("A number that identifiers a chromosome")
 public class ChromosomeNumber extends AbstractIdentifier {
 
     private static final Logger LOGGER = Logger.getLogger(ChromosomeNumber.class);
     public static int ticker;
     public int number;
 
     public ChromosomeNumber() {
     }
 
     public ChromosomeNumber(int number) {
         this.number = number;
         super.setAccession(Integer.toString(number));
     }
 
     @Override
     public void setAccession(String accession) {
         number = accession.isEmpty() ? 1 : Integer.parseInt(accession);
     }
 
     public int getNumber() {
         return number;
     }
 
     public ChromosomeNumber nextIdentifier() {
         return new ChromosomeNumber(++ticker);
     }
 
     public ChromosomeNumber newInstance() {
         return new ChromosomeNumber();
     }
 
     @Override
     public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
         super.readExternal(in);
         number = Integer.parseInt(getAccession());
     }
 
 }
