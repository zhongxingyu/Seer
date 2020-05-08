 package models.statistics.populations;
 
 import java.lang.Object;
 import java.lang.Class;
 import java.lang.RuntimeException;
 import java.lang.ClassCastException;
 import java.lang.reflect.Constructor;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Set;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Arrays;
 import java.util.Collection;
 
 import javax.persistence.PersistenceException;
 import com.avaje.ebean.Ebean;
 
 import models.user.UserType;
 import models.user.User;
 import models.user.Independent;
 import models.user.Teacher;
 
 import models.dbentities.UserModel;
 import models.dbentities.ClassGroup;
 import models.dbentities.SchoolModel;
 
 /**
  * This is a helper class for choosing populations. I propose a popup with tabs
  * for each type of population as view. Place a DMTV-like searcher on each tab.
  * @author Felix Van der Jeugt
  */
 public class PopulationChooser {
 
     private Map<PopulationType, List<Population>> pops;
 
     public PopulationChooser() {
         pops = new HashMap<PopulationType, List<Population>>();
     }
 
     /**
      * Adds a new "tab" to the chooser.
      */
     public void newType(PopulationType t, List<Population> l) {
         if(pops.containsKey(t)) pops.get(t).addAll(l);
         else                    pops.put(t, l);
     }
 
     /**
      * Filters the given list of Populations. This makes sure there are no
      * populations in the provided list, that cannot be selected with this
      * PopulationChooser.
      * @param list The list to be filtered.
      * @return The filtered list.
      */
     public List<Population> filter(List<Population> list) {
         List<Population> newlist = new ArrayList<Population>();
         for(Population p : list) if(pops.containsKey(p.populationType())) {
             int i = pops.get(p.populationType()).indexOf(p);
             if(i >= 0) {
                 newlist.add(p);
                 pops.get(p.populationType()).set(i, p);
             }
         }
         return newlist;
     }
 
     /**
      * Lists the different types of populations this chooser allows.
      */
     public Set<PopulationType> types() {
         return pops.keySet();
     }
 
     /**
      * Lists the different populations of the given type.
      */
     public List<Population> populations(PopulationType t) {
         return pops.get(t);
     }
 
     /* ====================================================================== *\
                     Static retrieval of PopulationChoosers.
     \* ====================================================================== */
 
     /**
      * Creates a list of Populations the given user of the given type can view.
      */
     public static PopulationChooser getPopulationChooser(UserType type, User user) {
         return genMap.get(type).choose(user);
     }
 
     /**
      * Creates a list of Populations the given user of the given type can view.
      */
     public static PopulationChooser getPopulationChooser(User user) {
         return genMap.get(user.getType()).choose(user);
     }
 
     private static interface PopulationGenerator {
         /**
          * Lists the viewable populations for this user. Note that for
          * Anonymous users, the parameter may be null, as the populations are
          * the same for each of them.
          * @param user The current user.
          */
         public PopulationChooser choose(User user);
     }
 
     private static Map<UserType, PopulationGenerator> genMap =
         new HashMap<UserType, PopulationGenerator>();
     static {
         genMap.put(UserType.ADMINISTRATOR, new PopulationGenerator() {
             @Override public PopulationChooser choose(User user) {
                 PopulationChooser chooser = new PopulationChooser();

                 /* Single Users: All of them. */
                 chooser.newType(
                     PopulationType.INDIVIDUAL,
                     mapToPop(
                         SinglePopulation.class,
                         UserModel.class,
                         Ebean.find(UserModel.class).findList()
                     )
                 );
 
                 /* Classes: All of them. */
                 chooser.newType(
                     PopulationType.CLASS,
                     mapToPop(
                         ClassPopulation.class,
                         ClassGroup.class,
                         Ebean.find(ClassGroup.class).findList()
                     )
                 );
 
                 /* Schools: All of them. */
                 chooser.newType(
                     PopulationType.SCHOOL,
                     mapToPop(
                         SchoolPopulation.class,
                         SchoolModel.class,
                         Ebean.find(SchoolModel.class).findList()
                     )
                 );
 
                 /* The whole bunch. */
                 chooser.newType(
                     PopulationType.ALL,
                     Arrays.asList((Population) new AllPopulation())
                 );
 
                 return chooser;
             }
         });
         genMap.put(UserType.ORGANIZER, genMap.get(UserType.ADMINISTRATOR));
         genMap.put(UserType.PUPIL_OR_INDEP, new PopulationGenerator() {
             @Override public PopulationChooser choose(User user) {
                 PopulationChooser chooser = new PopulationChooser();
                 Independent indep = (Independent) user;
 
                 /* Single Users: Well, himself. */
                 chooser.newType(
                     PopulationType.INDIVIDUAL,
                     Arrays.asList((Population) new SinglePopulation(user.data))
                 );
 
                 /* Classes: Current and old. */
                 List<ClassGroup> cgs = new LinkedList<ClassGroup>();
 
                 // Adding current class if it exists.
                 ClassGroup current = null;
                 try { current = indep.getCurrentClass(); }
                 catch (PersistenceException e) {}
                 if(current != null) cgs.add(current);
 
                 // Adding the old classes, if they exist.
                 try { cgs.addAll(indep.getPreviousClasses()); }
                 catch (PersistenceException e) {}

                 chooser.newType(
                     PopulationType.CLASS,
                     mapToPop(
                         ClassPopulation.class,
                         ClassGroup.class,
                         cgs
                     )
                 );
 
                 /* Schools: His own school. */
                 if(current != null) {
                     chooser.newType(
                         PopulationType.SCHOOL,
                         mapToPop(
                             SchoolPopulation.class,
                             SchoolModel.class,
                             Ebean.find(SchoolModel.class)
                                 .where().eq("id", current.schoolid).findList()
                         )
                     );
                 }
 
                 /* The whole bunch. */
                 chooser.newType(
                     PopulationType.ALL,
                     Arrays.asList((Population) new AllPopulation())
                 );
 
                 return chooser;
             }
         });
         genMap.put(UserType.TEACHER, new PopulationGenerator() {
             @Override public PopulationChooser choose(User user) {
                 PopulationChooser chooser = new PopulationChooser();
                 Teacher teacher = (Teacher) user;
                 // Preparing some variables:
                 List<ClassGroup> classes = new ArrayList<ClassGroup>();
                 try {
                     classes.addAll(teacher.getClasses());
                     classes.addAll(teacher.getHelpClasses());
                 } catch(PersistenceException e) {}
 
                 /* Single Users: Himself and his students. */
                List<UserModel> users = Arrays.asList(user.data);
                 for(ClassGroup cg : classes) {
                    try { users.addAll(cg.getPupils(ClassGroup.PupilSet.ALL)); }
                     catch (PersistenceException e) {}
                 }
                 chooser.newType(
                     PopulationType.INDIVIDUAL,
                     mapToPop(SinglePopulation.class, UserModel.class, users)
                 );

                 /* Classes: Just his classes. */
                 chooser.newType(
                     PopulationType.CLASS,
                     mapToPop(ClassPopulation.class, ClassGroup.class, classes)
                 );

                 /* Schools: Where he teaches. */
                Set<Integer> schools = new HashSet<Integer>();
                for(ClassGroup cg : classes) schools.add(cg.schoolid);
                 chooser.newType(
                     PopulationType.SCHOOL,
                     mapToPop(SchoolPopulation.class, SchoolModel.class, schools)
                 );
 
                 /* The whole bunch. */
                 chooser.newType(
                     PopulationType.ALL,
                     Arrays.asList((Population) new AllPopulation())
                 );
 
                 return chooser;
             }
         });
         genMap.put(UserType.ANON, new PopulationGenerator() {
             @Override public PopulationChooser choose(User user) {
                 PopulationChooser chooser = new PopulationChooser();
 
                 /* Single Users: None. */
                 /* Classes: None. */
                 /* Schools: None. */
 
                 /* The whole bunch. */
                 chooser.newType(
                     PopulationType.ALL,
                     Arrays.asList((Population) new AllPopulation())
                 );
 
                 return chooser;
             }
         });
         genMap.put(UserType.AUTHOR, genMap.get(UserType.ANON));
     }
 
     // Help function. Takes a list of Objects a class b, which can by passed to
     // a constructor of class a as only parameter. Returns the populations
     // created.
     //
     // for instance:
     // mapToPop(ClassPopulation.class, ClassGroup.class, listOfClassGroups)
     private static List<Population> mapToPop(Class<? extends Population> a,
             Class<?> b, Collection<?> l) {
         List<Population> pops = new ArrayList<Population>(l.size());
 
         // Get constructor.
         Constructor<? extends Population> c;
         try { c = a.getConstructor(b); }
         catch (NoSuchMethodException e) {
             throw new RuntimeException("Population subclasses should have a "
                     + "constructor which takes a single parameter.");
         }
         catch (SecurityException e) {
             throw new RuntimeException("Dammit, make your constructors public, "
                     + "would you?");
         }
 
         // Create list.
         Iterator<?> it = l.iterator();
         try {
            while(it.hasNext()) pops.add(c.newInstance(it.next()));
         } catch(Exception e) {
             throw new RuntimeException(e.getMessage());
         }
 
         return pops;
     }
 }
