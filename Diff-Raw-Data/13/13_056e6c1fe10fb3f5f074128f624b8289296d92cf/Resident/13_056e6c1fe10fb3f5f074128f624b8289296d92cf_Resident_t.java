 package models.residence;
 
 import java.util.Collections;
 import java.util.List;
 
 import javax.persistence.*;
 
 import models.inbox.Mail;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.codehaus.jackson.annotate.JsonIgnoreProperties;
 import org.codehaus.jackson.map.annotate.JsonDeserialize;
 import org.codehaus.jackson.map.annotate.JsonSerialize;
 import org.codehaus.jackson.map.ext.JodaSerializers;
 import org.joda.time.Days;
 import org.joda.time.LocalDate;
 
 import play.data.validation.Constraints;
 import play.db.ebean.Model;
 
 import com.avaje.ebean.Expr;
 import com.avaje.ebean.Expression;
 import com.avaje.ebean.ExpressionList;
 import com.google.common.base.Strings;
 import core.io.serialization.JodaLocalDateDeserializer;
 
 @Entity
 @JsonIgnoreProperties(ignoreUnknown = true)
 public class Resident extends Model {
 
     private static final Finder<Long, Resident> FINDER = new Finder<Long, Resident>(
             Long.class, Resident.class);
 
     @Id
     public Long id;
 
     public Gender gender;
 
     @Constraints.Required
     public String firstName;
 
     @Constraints.Required
     public String lastName;
 
     public String maidenName;
 
     @JsonSerialize(using = JodaSerializers.LocalDateSerializer.class)
     @JsonDeserialize(using = JodaLocalDateDeserializer.class)
     public LocalDate birthDate;
 
     public Department followingDepartment;
 
     public String followedBy;
 
     @OneToMany(mappedBy = "resident", cascade = CascadeType.ALL)
     public List<Residence> residences;
 
     @JsonIgnore
     @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL)
     public List<Mail> mail;
 
     //
 
     @Transient
     public Residence getLatestResidence() {
        Residence latestResidence = null;
        for (Residence residence : residences) {
            if (latestResidence == null) {
                latestResidence = residence;
            } else if (residence.startDate.isAfter(latestResidence.startDate)) {
                latestResidence = residence;
            }
        }
        return latestResidence;
     }
 
     @Transient
     public int getResidenceProgress() {
         Residence latestResidence = getLatestResidence();
         if (latestResidence == null
                 || LocalDate.now().isAfter(latestResidence.endDate)) {
             return -1;
         }
         return (int) (100 * (1 - (((double) Days.daysBetween(LocalDate.now(),
                 latestResidence.endDate).getDays()) / 365)));
     }
 
     @Transient
     public String getDisplay() {
         StringBuilder sb = new StringBuilder(String.format("%s %s", firstName,
                 lastName));
         if (!Strings.isNullOrEmpty(maidenName)) {
             sb.append(String.format(" (%s)", maidenName));
         }
         return sb.toString();
     }
 
     //
 
     /**
      * Fetch all residents. * @return All residents.
      */
     public static List<Resident> findAll() {
         return FINDER.all();
     }
 
     public static List<Resident> find(String queryString) {
         if (Strings.isNullOrEmpty(queryString) || queryString.length() < 3) {
             return Collections.emptyList();
         }
         ExpressionList<Resident> where = FINDER.where();
         for (String queryToken : queryString.split(" ")) {
             Expression ex = Expr.or(Expr.ilike("firstName", queryToken + "%"),
                     Expr.ilike("lastName", queryToken + "%"));
             ex = Expr.or(ex, Expr.ilike("maidenName", queryToken + "%"));
             where.add(ex);
         }
         return where.findList();
     }
 
     public static Resident byId(Long id) {
         return FINDER.fetch("residences").orderBy("residences.startDate DESC")
                 .where().idEq(id).findUnique();
     }
 }
