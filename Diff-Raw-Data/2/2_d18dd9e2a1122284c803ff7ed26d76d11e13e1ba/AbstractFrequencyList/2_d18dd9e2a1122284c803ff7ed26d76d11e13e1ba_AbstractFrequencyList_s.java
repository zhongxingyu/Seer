 /*
  * Copyright (C) 2012 Fabian Hirschmann <fabian@hirschm.net>
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package com.github.fhirschmann.clozegen.lib.frequency;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import nu.xom.*;
 
 /**
  * Base class for all Frequency Distribution Lists.
  * <p>
  * This base class allows you to load a list of frequencies from an XML
  * file. These XML files must be of the following nature:
  * <pre>
  * <frequencies>
  *     <frequency value="1234" ... />
  *     <frequency value="4563" ... />
  * </frequencies>
  * </pre>
 * The only required xml attribute here is the "value" attribute which indicates
  * how many times the subject has occurred. You can add as many attributes
  * as you like and process them in processChild(), so the population
  * of the frequencies with ValueCount<T> is up to you.
  * </p>
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public abstract class AbstractFrequencyList<T> {
     /** The frequency count. */
     private final List<ValueCount<T>> frequencies = new ArrayList<ValueCount<T>>();
 
     /**
      * This method lets you process individual frequency-children in the XML file.
      *
      * @param child
      */
     abstract void processChild(Element child);
 
     /**
      * Loads the frequency list from an XML file.
      *
      * @param xmlFile
      * @throws ParsingException
      * @throws ValidityException
      * @throws IOException
      */
     public void load(final String xmlFile)
             throws ParsingException, ValidityException, IOException {
         final InputStream stream = this.getClass().getClassLoader().
                 getResourceAsStream(xmlFile);
 
         final Builder builder = new Builder();
         final Document doc = builder.build(stream);
         final Element root = doc.getRootElement();
         for (int i = 0; i < root.getChildCount(); i++) {
             final Element child = root.getChildElements().get(i);
             processChild(child);
         }
         Collections.sort(getFrequencies());
     }
 
     /**
      * @return the frequencies
      */
     public List<ValueCount<T>> getFrequencies() {
         return frequencies;
     }
 }
