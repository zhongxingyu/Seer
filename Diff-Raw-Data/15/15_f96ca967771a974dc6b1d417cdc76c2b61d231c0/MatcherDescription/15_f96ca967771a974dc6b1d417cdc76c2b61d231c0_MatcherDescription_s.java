 package uk.ac.ebi.mdk.ui.component.compare;
 
 import org.apache.log4j.Logger;
 import uk.ac.ebi.mdk.domain.entity.Entity;
 import uk.ac.ebi.mdk.tool.match.EntityMatcher;
 
 /**
  * A simple wraper around the match descriptor that provides some user level information
  *
  * @author John May
  */
 public class MatcherDescription<E extends Entity> {
 
     private static final Logger LOGGER = Logger.getLogger(MatcherDescription.class);
 
     private String name;
     private String description;
    private EntityMatcher<E> matcher;
 
     public MatcherDescription(String name,
                               String description,
                              EntityMatcher<E> matcher) {
         this.name        = name;
         this.description = description;
         this.matcher     = matcher;
     }
 
     public String getName() {
         return name;
     }
 
     public String getDescription() {
         return description;
     }
 
    public EntityMatcher<E> getMatcher() {
         return matcher;
     }
 }
