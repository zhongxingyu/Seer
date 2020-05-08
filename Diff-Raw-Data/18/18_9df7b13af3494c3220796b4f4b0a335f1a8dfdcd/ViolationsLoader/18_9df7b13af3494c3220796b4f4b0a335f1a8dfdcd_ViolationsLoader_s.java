 package org.sonar.ide.shared;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.sonar.wsclient.Sonar;
 import org.sonar.wsclient.services.Violation;
 import org.sonar.wsclient.services.ViolationQuery;
 
 import java.util.Collection;
 
 /**
  * @author Evgeny Mandrikov
  */
 public final class ViolationsLoader {
   private static final Logger LOG = LoggerFactory.getLogger(ViolationsLoader.class);
 
   public Collection<Violation> getViolations(Sonar sonar, String resourceKey) {
     LOG.info("Loading violations for {}", resourceKey);
     ViolationQuery query = ViolationQuery.createForResource(resourceKey);
     Collection<Violation> violations = sonar.findAll(query);
     LOG.info("Loaded {} violations", violations.size());
     return violations;
   }
 
   public static String getDescription(Violation violation) {
    return violation.getResourceName() + " : " + violation.getMessage();
   }
 }
