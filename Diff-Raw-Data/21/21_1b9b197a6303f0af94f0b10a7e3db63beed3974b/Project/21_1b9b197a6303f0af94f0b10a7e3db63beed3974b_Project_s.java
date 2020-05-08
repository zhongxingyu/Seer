 package org.synyx.minos.skillz.domain;
 
 import javax.persistence.Entity;
 import javax.persistence.Lob;
 import javax.persistence.ManyToOne;
 
 import org.apache.commons.lang.StringUtils;
 import org.synyx.hades.domain.auditing.AbstractAuditable;
 import org.synyx.minos.core.domain.User;
 import org.synyx.minos.util.Assert;
 
 
 /**
  * Captures high level project information. This is a rather textual description
  * of the project and issues and problems the project deals with. Primarily used
  * to have a consistent project description in various {@link Resume}s.
  * 
  * @author Oliver Gierke - gierke@synyx.de
  */
 @Entity
 public class Project extends AbstractAuditable<User, Long> {
 
     private static final long serialVersionUID = -2562908001067672752L;
     private static final String[] DELIMITERS =
             new String[] { ".\r", ".\n", ".\r\n" };
 
     private String name;
 
     @Lob
     private String description;
 
     private String customer;
 
     private String industry;
 
     @Lob
     private String platform;
 
     @ManyToOne
     private User owner;
 
 
     protected Project() {
 
     }
 
 
     /**
      * Creates a new {@link Project}.
      * 
      * @param name
      */
     public Project(String name) {
 
         this(name, null);
     }
 
 
     /**
      * Creates a new {@link Project} with the given name and description.
      * 
      * @param name
      * @param description
      */
     public Project(String name, String description) {
 
         this(name, description, null);
     }
 
 
     public Project(String name, String description, User owner) {
 
         Assert.hasText(name, "Name must not be empty!");
 
         this.name = name;
         this.description = description;
         this.owner = owner;
     }
 
 
     /**
      * @return the name
      */
     public String getName() {
 
         return name;
     }
 
 
     /**
      * @param name the name to set
      */
     public void setName(String name) {
 
         this.name = name;
     }
 
 
     /**
      * @return the description
      */
     public String getDescription() {
 
         return description;
     }
 
 
     /**
      * @param description the description to set
      */
     public void setDescription(String description) {
 
         this.description = description;
     }
 
 
     /**
      * @return the industry
      */
     public String getIndustry() {
 
         return industry;
     }
 
 
     /**
      * @param industry the industry to set
      */
     public void setIndustry(String industry) {
 
         this.industry = industry;
     }
 
 
     /**
      * @return the customer
      */
     public String getCustomer() {
 
         return customer;
     }
 
 
     /**
      * @param customer the customer to set
      */
     public void setCustomer(String customer) {
 
         this.customer = customer;
     }
 
 
     /**
      * @return the platform
      */
     public String getPlatform() {
 
         return platform;
     }
 
 
     /**
      * @param platform the platform to set
      */
     public void setPlatform(String platform) {
 
         this.platform = platform;
     }
 
 
     /**
      * @return the owner
      */
     public User getOwner() {
 
         return owner;
     }
 
 
     /**
      * @param owner the owner to set
      */
     public void setOwner(User owner) {
 
         this.owner = owner;
     }
 
 
     /**
      * Returns whether the project is a private one.
      * 
      * @return the custom
      */
     public boolean isCustom() {
 
         return null != owner;
     }
 
 
     /**
      * Returns whether the project belongs to the given {@link User}.
      * 
      * @param user
      * @return
      */
     public boolean belongsTo(User user) {
 
         return this.owner != null && this.owner.equals(user);
     }
 
 
     /**
      * Returns the abstract of the project. This will include the first
      * paragraph of the description delimited by a dot and a newline.
      * 
      * @return
      */
     public String getAbstract() {
 
         int index = hasDescriptionDelimiter();
 
         if (index == -1) {
             return description;
         }
 
         return description.substring(0, index) + ". ...";
     }
 
 
     /**
      * Returns the index of a found delimiter or -1 if none of the delimiters
      * has been found.
      * 
      * @return
      */
     private int hasDescriptionDelimiter() {
 
         for (String delimiter : DELIMITERS) {
 
             int index = description.indexOf(delimiter);
 
             if (index != -1) {
                 return index;
             }
         }
 
         return -1;
     }
 
 
     /*
      * (non-Javadoc)
      * 
      * @see org.synyx.hades.domain.support.AbstractPersistable#toString()
      */
     @Override
     public String toString() {
 
         StringBuilder builder = new StringBuilder(getName());
 
         if (null != description) {
             builder.append(": ");
             builder.append(StringUtils.substring(getDescription(), 0, 50)
                     .replaceAll("\n", ""));
         }
 
         return builder.toString();
     }
 }
