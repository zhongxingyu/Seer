 package models;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToOne;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.StringUtils;
 
 import play.Logger;
 import play.data.validation.Constraints.Required;
 import play.db.ebean.Model;
 
 import com.avaje.ebean.Ebean;
 import com.avaje.ebean.ExpressionList;
 import com.avaje.ebean.Page;
 import com.typesafe.config.ConfigFactory;
 
 import forms.DvdForm;
 import forms.DvdListFrom;
 import forms.EDvdListOrderBy;
 import forms.EDvdListOrderHow;
 
 @Entity
 public class Dvd extends Model {
 
   /**
 	 * 
 	 */
   private static final long serialVersionUID = -8607299241692950618L;
 
   public static final String HULL_NR_SEARCH = "hull:";
 
   @Id
   public Long id;
 
   @ManyToOne
   public User owner;
 
   @OneToOne
   public User borrower;
 
   @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY, mappedBy = "dvds")
   public Set<DvdAttribute> attributes;
 
   public Long borrowDate;
 
   private final static int DEFAULT_DVDS_PER_PAGE = ConfigFactory.load().getInt("dvddb.dvds.perpage");
 
   /**
    * If this is set the user entered a free name which does not exists in the
    * database
    */
   public String borrowerName;
 
   /**
    * The number off the hull off the dvd
    */
   public Integer hullNr;
 
   /**
    * The movie which is on the dvd
    */
   @ManyToOne(fetch = FetchType.LAZY)
   @Column(nullable = false)
   public Movie movie;
 
   /**
    * The finder for the database for searching in the database
    */
   public static Finder<Long, Dvd> find = new Finder<Long, Dvd>(Long.class, Dvd.class);
 
   @Required
   @Column(nullable = false)
   public Long createdDate;
 
   /**
    * Persists the {@link Dvd} to the database
    * 
    * @param dvd
    * @return
    */
   public static Dvd create(final Dvd dvd) {
     dvd.save();
     return dvd;
   }
 
 /**
 	 * Creates a dvd from a {@link DvdForm)
 	 * @param userName
 	 * @param dvdForm
 	 * @return 
 	 * @throws Exception 
 	 */
   public static Dvd createFromForm(final String userName, final DvdForm dvdForm) throws Exception {
 
     final User owner = User.getUserByName(userName);
     if (owner == null) {
       throw new Exception("User does not exist in the Database");
     }
 
     final Dvd dvd = new Dvd();
     dvd.owner = owner;
 
     return Dvd.createOrUpdateFromForm(dvdForm, dvd);
   }
 
   /**
    * Edits a dvd from the informations from the form
    * 
    * @param userName
    * @param dvdForm
    * @throws Exception
    */
   public static Dvd editFromForm(final String userName, final DvdForm dvdForm) throws Exception {
     final Dvd dvdForUser = Dvd.getDvdForUser(dvdForm.dvdId, userName);
     if (dvdForUser == null) {
       throw new Exception("The dvd could not been edited !");
     }
 
     return Dvd.createOrUpdateFromForm(dvdForm, dvdForUser);
   }
 
   /**
    * Creates or updates the given dvd
    * 
    * @param userName
    * @param dvdForm
    * @param dvd
    * @return
    * @throws Exception
    */
   private static Dvd createOrUpdateFromForm(final DvdForm dvdForm, final Dvd dvd) throws Exception {
 
     final Movie movie = Movie.find.byId(dvdForm.movieId);
 
     if (movie == null) {
       final String message = "No movie by the id: " + dvdForm.movieId + " found.";
       Logger.error(message);
       throw new Exception(message);
     }
 
     dvd.movie = movie;
     dvd.hullNr = dvdForm.hullNr;
 
     if (dvd.id == null) {
       dvd.createdDate = new Date().getTime();
       Dvd.create(dvd);
     } else {
       Ebean.deleteManyToManyAssociations(dvd, "attributes");
       dvd.update();
     }
 
     Dvd.addSingleAttribute(dvdForm.box, EDvdAttributeType.BOX, dvd);
     Dvd.addSingleAttribute(dvdForm.collection, EDvdAttributeType.COLLECTION, dvd);
     Dvd.addSingleAttribute(dvdForm.ageRating, EDvdAttributeType.RATING, dvd);
     Dvd.addSingleAttribute(dvdForm.copyType, EDvdAttributeType.COPY_TYPE, dvd);
 
     dvd.update();
 
     return dvd;
   }
 
   /**
    * Adds a single Attribute to the dvd
    * 
    * @param attrToAdd
    * @param attributeType
    * @param dvd
    */
   // TODO: do we need this when we have the new EBEAN nand check also movie
   // and
   // attributes
   public static void addSingleAttribute(final String attrToAdd, final EDvdAttributeType attributeType, final Dvd dvd) {
     if (StringUtils.isEmpty(attrToAdd) == true) {
       return;
     }
     final Set<String> attribute = new HashSet<String>();
     attribute.add(attrToAdd);
     final Set<DvdAttribute> dbAttrs = DvdAttribute.gatherAndAddAttributes(attribute, attributeType);
     dvd.attributes.addAll(dbAttrs);
   }
 
   /**
    * Gets all dvds which have the same owner and the same attribute excluding
    * the given {@link Dvd}
    * 
    * @param box
    * @param dvd
    * @return
    */
   public static List<Dvd> getDvdByBoxOrCollection(final EDvdAttributeType attrType, final String attrValue, final Dvd dvd) {
 
     final List<Dvd> findList = Dvd.find.where().eq("attributes.attributeType", attrType).eq("attributes.value", attrValue).eq("owner.id", dvd.owner.id).ne("id", dvd.id).orderBy("movie.year asc").findList();
 
     return findList;
   }
 
   /**
    * Gets all dvds which fit into the Filter int the {@link DvdListFrom}
    * 
    * @param listFrom
    * @return
    */
   public static Page<Dvd> getDvdsByForm(final DvdListFrom listFrom) {
     final ExpressionList<Dvd> where = Dvd.find.where();
 
     if (StringUtils.isEmpty(listFrom.searchFor) == false) {
 
       if (listFrom.searchFor.startsWith(Dvd.HULL_NR_SEARCH) == true) {
         final String idToSearch = StringUtils.trimToNull(StringUtils.removeStart(listFrom.searchFor, Dvd.HULL_NR_SEARCH));
         if (idToSearch != null && StringUtils.isNumeric(idToSearch) == true) {
           where.eq("hullNr", idToSearch);
         }
 
       } else {
         where.like("movie.title", "%" + listFrom.searchFor + "%");
       }
     }
 
     if (StringUtils.isEmpty(listFrom.genre) == false) {
       where.eq("movie.attributes.value", listFrom.genre).eq("movie.attributes.attributeType", EMovieAttributeType.GENRE);
     }
 
     if (StringUtils.isEmpty(listFrom.actor) == false) {
       where.eq("movie.attributes.value", listFrom.actor).eq("movie.attributes.attributeType", EMovieAttributeType.ACTOR);
     }
 
     if (StringUtils.isEmpty(listFrom.director) == false) {
       where.eq("movie.attributes.value", listFrom.director).eq("movie.attributes.attributeType", EMovieAttributeType.DIRECTOR);
     }
 
     if (StringUtils.isEmpty(listFrom.ageRating) == false) {
       where.eq("attributes.value", listFrom.ageRating).eq("attributes.attributeType", EDvdAttributeType.RATING);
     }
 
     if (StringUtils.isEmpty(listFrom.userName) == false) {
       where.eq("owner.userName", listFrom.userName);
 
       if (listFrom.lendDvd == true) {
         where.isNotNull("borrowDate");
       }
     }
 
     if (listFrom.moviesToReview == true) {
       where.eq("movie.hasToBeReviewed", true);
     }
 
     return Dvd.getByDefaultPaging(where, listFrom.currentPage, listFrom.orderBy, listFrom.orderHow);
   }
 
   /**
    * Gets a dvd by a username and the id this should be used for deleting and
    * editing where only the owner can do this
    * 
    * @param id
    * @param username
    * @return
    */
   public static Dvd getDvdForUser(final Long id, final String username) {
     final Dvd userDvd = Dvd.find.fetch("movie").where().eq("owner.userName", username).eq("id", id).findUnique();
     return userDvd;
 
   }
 
   /**
    * Gets dvds which are in the same hull but not the dvd itself
    * 
    * @param dvd
    * @return
    */
   public static List<Dvd> getDvdUnBorrowedSameHull(final Dvd dvd) {
 
     // dont bother the database
     if (dvd.hullNr == null) {
       return null;
     }
 
     final List<Dvd> findList = Dvd.find.fetch("movie").where().eq("owner", dvd.owner).eq("hullNr", dvd.hullNr).ne("id", dvd.id).isNull("borrowDate").orderBy("movie.title").findList();
 
     if (CollectionUtils.isEmpty(findList) == true) {
       return null;
     }
 
     return findList;
   }
 
   public static List<Dvd> getDvdBorrowedSameHull(final Dvd dvd) {
 
     // dont bother the database
     if (dvd.hullNr == null) {
       return null;
     }
 
     final ExpressionList<Dvd> notNull = Dvd.find.fetch("movie").where().eq("owner", dvd.owner).eq("hullNr", dvd.hullNr).ne("id", dvd.id).isNotNull("borrowDate");
 
     Dvd.borrowerOrBorrowed(dvd, notNull);
 
     final List<Dvd> findList = notNull.orderBy("movie.title").findList();
 
     if (CollectionUtils.isEmpty(findList) == true) {
       return null;
     }
 
     return findList;
   }
 
   private static void borrowerOrBorrowed(final Dvd dvd, final ExpressionList<Dvd> notNull) {
     if (StringUtils.isEmpty(dvd.borrowerName) == false) {
       notNull.eq("borrowerName", dvd.borrowerName);
     } else {
       notNull.eq("borrower", dvd.borrower);
     }
   }
 
   /**
    * This does all the default paging etc stuff
    * 
    * @param expressionList
    * @param orderHow
    * @param orderBy
    * @return
    */
   private static Page<Dvd> getByDefaultPaging(final ExpressionList<Dvd> expressionList, Integer pageNr, final EDvdListOrderBy orderBy, final EDvdListOrderHow orderHow) {
 
     if (pageNr == null) {
       pageNr = 0;
     }
 
     final Page<Dvd> page = expressionList.orderBy(orderBy.dbField + " " + orderHow.dbOrder).fetch("owner", "userName").fetch("borrower", "userName").fetch("movie").findPagingList(
         Dvd.DEFAULT_DVDS_PER_PAGE).getPage(pageNr);
     return page;
   }
 
   /**
    * Lends the {@link Dvd} to a user or to a freename
    * 
    * @param dvdId
    * @param ownerName
    * @param userName
    * @param freeName
    */
   public static void lendDvdToUser(final Long dvdId, final String ownerName, final String userName, final String freeName, final Boolean alsoOthersInHull) {
     final Dvd dvdToLend = Dvd.getDvdForUser(dvdId, ownerName);
 
     if (dvdToLend != null) {
 
       final Set<Dvd> dvdsToLend = new HashSet<Dvd>();
       dvdsToLend.add(dvdToLend);
 
       // user also wants to lend dvds in the same hull
       if (alsoOthersInHull == true && dvdToLend.hullNr != null) {
         final Set<Dvd> findSet = Dvd.find.where().eq("hullNr", dvdToLend.hullNr).ne("id", dvdToLend.id).isNull("borrowDate").eq("owner", dvdToLend.owner).findSet();
         dvdsToLend.addAll(findSet);
       }
 
       for (final Dvd dvd : dvdsToLend) {
 
         boolean updated = false;
 
         if (StringUtils.isEmpty(userName) == false) {
           final User userByName = User.getUserByName(userName);
           if (userByName != null) {
             dvd.borrower = userByName;
             updated = true;
           }
         }
 
         if (StringUtils.isEmpty(freeName) == false && updated == false) {
           dvd.borrowerName = freeName;
           updated = true;
         }
 
         if (updated == true) {
           dvd.borrowDate = new Date().getTime();
           dvd.update();
         }
       }
       return;
     }
 
     Logger.error("Could not find dvd: " + dvdId + " for owner: " + ownerName);
   }
 
   /**
    * Lends the {@link Dvd} to a user or to a freename
    * 
    * @param dvdId
    * @param ownerName
    * @param userName
    * @param freeName
    */
  public static void unlendDvdToUser(final Long dvdId, final String ownerName, final Boolean alsoOthersInHull) {
     final Dvd dvdToUnlend = Dvd.getDvdForUser(dvdId, ownerName);
 
     if (dvdToUnlend != null) {
 
       final Set<Dvd> dvdsToUnlend = new HashSet<Dvd>();
       dvdsToUnlend.add(dvdToUnlend);
 
       // user also wants to lend dvds in the same hull
       if (alsoOthersInHull == true && dvdToUnlend.hullNr != null) {
 
         final ExpressionList<Dvd> expression = Dvd.find.where().eq("hullNr", dvdToUnlend.hullNr).ne("id", dvdToUnlend.id).eq("owner", dvdToUnlend.owner).isNotNull("borrowDate");
         Dvd.borrowerOrBorrowed(dvdToUnlend, expression);
 
         dvdsToUnlend.addAll(expression.findSet());
       }
 
       for (final Dvd dvd : dvdsToUnlend) {
 
         dvd.borrowDate = null;
         dvd.borrower = null;
         dvd.borrowerName = null;
         dvd.update();
       }
      return;
     }
 
     Logger.error("Could not find dvd: " + dvdId + " for owner: " + ownerName);
   }
 
   /**
    * Gets all dvds which the owner of th dvd has of the same movie series
    * 
    * @param attrValue
    * @param dvd
    * @return
    */
   public static List<Dvd> getbyMovieSeries(final String attrValue, final Dvd dvd) {
 
     final List<Dvd> findList = Dvd.find.where().eq("movie.attributes.attributeType", EMovieAttributeType.MOVIE_SERIES).eq("movie.attributes.value", attrValue).eq("owner.id", dvd.owner.id).ne(
         "id",
         dvd.id).findList();
 
     return findList;
   }
 
 }
