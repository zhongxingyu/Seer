 /**
  * Copyright (c) 2009 Red Hat, Inc.
  *
  * This software is licensed to you under the GNU General Public License,
  * version 2 (GPLv2). There is NO WARRANTY for this software, express or
  * implied, including the implied warranties of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
  * along with this software; if not, see
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
  *
  * Red Hat trademarks are not licensed under GPLv2. No permission is
  * granted to use or replicate Red Hat trademarks that are incorporated
  * in this software or its documentation.
  */
 package org.fedoraproject.candlepin.sync;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.fedoraproject.candlepin.model.ConsumerType;
 import org.fedoraproject.candlepin.model.ConsumerTypeCurator;
 import org.hibernate.exception.ConstraintViolationException;
 
 /**
  * ConsumerTypeImporter
  */
 public class ConsumerTypeImporter implements EntityImporter<ConsumerType> {
     private static Logger log = Logger.getLogger(ConsumerTypeImporter.class);
 
     private ConsumerTypeCurator curator;
     
     public ConsumerTypeImporter(ConsumerTypeCurator curator) {
         this.curator = curator;
     }
     
     public ConsumerType createObject(ObjectMapper mapper, Reader reader)
         throws IOException {
         ConsumerType consumerType = mapper.readValue(reader, ConsumerType.class);
         consumerType.setId(null);
         return consumerType;
     }
 
     /**
      * @param testType
      */
     public void store(Set<ConsumerType> consumerTypes) {
        Set<String> resolved = new HashSet();
         
         log.debug("Creating/updating consumer types");
         for (ConsumerType consumerType : consumerTypes) {
             if (curator.lookupByLabel(consumerType.getLabel()) == null) {
                 curator.create(consumerType);
                 log.debug("Created consumer type: " + consumerType.getLabel());
             }
             resolved.add(consumerType.getLabel());
         }
         log.debug("Deleting old consumer types");
         for (ConsumerType consumerType : curator.listAll()) {
             if (!resolved.contains(consumerType.getLabel())) {
                 try {
                     curator.delete(consumerType);
                     log.debug("Deleted consumer type: " + consumerType.getLabel());
                 }
                 catch (ConstraintViolationException e) {
                     log.debug("Skipping deletion (constraint violation raised) for: " +
                         consumerType.getLabel());
                 }
             }
         }
     }
 }
