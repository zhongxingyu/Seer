 package org.synyx.minos.skillz.domain;
 
import static org.hibernate.annotations.CascadeType.*;

 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.OneToMany;
 
import org.hibernate.annotations.Cascade;
 import org.synyx.hades.domain.auditing.AbstractAuditable;
 import org.synyx.minos.core.domain.User;
 import org.synyx.minos.util.Assert;
 
 
 /**
  * Categories group {@link Skill}s together.
  * 
  * @author Oliver Gierke - gierke@synyx.de
  */
 @Entity
 public class Category extends AbstractAuditable<User, Long> {
 
     private static final long serialVersionUID = 1447239273288778108L;
 
     private String name;
     private String description;
 
    @OneToMany
    @Cascade( { PERSIST })
     private final List<Skill> skills = new ArrayList<Skill>();
 
 
     protected Category() {
 
     }
 
 
     /**
      * Creates a new {@link Category} with the given name and no description.
      * 
      * @param string
      */
     public Category(String name) {
 
         this(name, null);
     }
 
 
     /**
      * Creates a new {@link Category} with the given name and description.
      * 
      * @param name
      * @param description
      */
     public Category(String name, String description) {
 
         this.name = name;
         this.description = description;
     }
 
 
     /**
      * Returns the name.
      * 
      * @return the name
      */
     public String getName() {
 
         return name;
     }
 
 
     /**
      * Sets the name.
      * 
      * @param name the name to set
      */
     public void setName(String name) {
 
         this.name = name;
     }
 
 
     /**
      * Returns the description.
      * 
      * @return the description
      */
     public String getDescription() {
 
         return description;
     }
 
 
     /**
      * Sets the description.
      * 
      * @param description the description to set
      */
     public void setDescription(String description) {
 
         this.description = description;
     }
 
 
     /**
      * Adds the given {@link Skill} to the category. Assigns the category to the skill if not already assigned.
      * 
      * @param skill
      * @return
      */
     public Category add(Skill skill) {
 
         this.skills.add(skill);
 
         if (!skill.has(this)) {
             skill.setCategory(this);
         }
 
         return this;
     }
 
 
     /**
      * Returns the skills of that {@link Category}.
      * 
      * @return the skills
      */
     public List<Skill> getSkillz() {
 
         return skills;
     }
 
 
     /**
      * Assigns the given {@link Skill}s to the category.
      * 
      * @param skills the skills to set
      */
     public void setSkills(Collection<Skill> skills) {
 
         Assert.notNull(skills);
 
         for (Skill skill : skills) {
             add(skill);
         }
     }
 
 
     /**
      * Returns whether the given skill is assigned to the {@link Category}.
      * 
      * @param skill
      * @return
      */
     public boolean has(Skill skill) {
 
         return this.skills.contains(skill);
     }
 
 
     void remove(Skill skill) {
 
         this.skills.remove(skill);
     }
 
 
     /*
      * (non-Javadoc)
      * 
      * @see org.synyx.hades.domain.support.AbstractPersistable#toString()
      */
     @Override
     public String toString() {
 
         StringBuilder builder = new StringBuilder(getName());
         builder.append(": ");
 
         for (Skill skill : skills) {
             builder.append(skill.getName());
             builder.append(", ");
         }
 
         return builder.substring(0, builder.length() - 2).toString();
     }
 }
