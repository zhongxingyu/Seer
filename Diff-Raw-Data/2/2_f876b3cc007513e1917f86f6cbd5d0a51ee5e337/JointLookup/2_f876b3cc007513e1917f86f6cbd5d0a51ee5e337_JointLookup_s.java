 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.joint;
 
 import org.joint.spi.Jointed;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 import org.joint.annotations.Joint;
 import org.joint.annotations.Load;
 import org.openide.util.Lookup;
 import org.openide.util.Lookup.Item;
 
 /**
  * The core, really just a wrapper around org.openide.util.Lookup with some
  * added functionality for looking up Jointed classes using various aspects
  * of the project.
  *
  * @todo Figure out how to test this. Probably will have to test in a separate
  * project which exports a DummyJoint service
  *
  * @finishme
  *
  * @author btilford
  */
 public class JointLookup {
 
     private JointLookup() {
     }
 
 
     /**
      * For future safety if a new implementation of Lookup is used. Currently
      * returns Lookup.getDefault()
      *
      * To be safe use this method if you need to get an instance of Lookup as
      * opposed to calling Lookup.getDefault() on your own.
      *
      * @return
      */
     public static synchronized Lookup getJointLookup() {
         return Lookup.getDefault();
     }
 
     /**
      * Lookup all implementations of a class which extends T
      * @param <T> must extend Jointed
      * @param type must extend T
      * @return all implementations of type
      */
     public static synchronized <T extends Jointed> Set<Class<? extends T>> lookupJoints(
             final Class<T> type) {
         Lookup.Template<T> template = new Lookup.Template<T>(type);
         Lookup.Result<T> result = Lookup.getDefault().lookup(template);
         Collection<? extends Item<T>> items = result.allItems();
         Set<Class<? extends T>> joints = new HashSet<Class<? extends T>>(
                 items.size());
         for (Item<T> item : items) {
             joints.add(item.getType());
         }
         return joints;
     }
 
     /**
      * Sounded usefull but will probably go away
      * @deleteme
      * 
      * @param <T>
      * @param type
      * @param joint
      * @return
      */
     public static synchronized <T extends Jointed> Class<T> findByJoint(
             final Class<T> type, final Joint joint) {
         return findByJointId(type, joint.id());
     }
 
     /**
      * If you know a Joint's id you can look up that class.
      *
      * @param <T> extends Jointed
      * @param type class extending Jointed
      * @param id Joint.id of the Jointed class instance you are looking for.
      * @return first class extending T with a matching @Joint.id
      */
     @SuppressWarnings("unchecked")
     public static <T extends Jointed> Class<T> findByJointId(
             final Class<T> type, final String id) {
 
 
         Set<Class<? extends T>> joints = null;
 
         Object owner = new Object();
         synchronized (owner) {
             joints = lookupJoints(type);
         }
 
         Class<? extends T> result = null;
         for (Class<? extends T> joint : joints) {
            if (id.equals(getJointId(type))) {
                 result = joint;
                 break;
             }
         }
         return (Class<T>) result;
     }
 
     /**
      * Given an class extending Jointed get the @Joint annotation on it.
      * @param <T> a type extending Jointed
      * @param type a class extending Jointed
      * @return the @Joint annotation on type
      */
     public static synchronized <T extends Jointed> Joint getJoint(
             final Class<T> type) {
         return type.getAnnotation(Joint.class);
     }
 
     /**
      * Get the Joint id of the given extension of Jointed
      * @param <T> extends Jointed
      * @param type Class extending Jointed
      * @return @Joint.id of type
      */
     public static <T extends Jointed> String getJointId(
             final Class<T> type) {
         Object owner = new Object();
         Joint joint = null;
 
         synchronized (owner) {
             joint = getJoint(type);
         }
         String id = null;
         if (joint != null) {
             id = joint.id();
         }
         return id;
     }
 
     /**
      * Create a new instance of Jointed
      *
      * Note:
      * If class is an implementation of Loadable it's load() method will be
      * called after construction unless the load() method is marked with
      * @Load(postConstruct=false)
      *
      * @param <T> extends Jointed
      * @param type class extending Jointed
      * @param id Joint.id of the Jointed class instance you are looking for.
      * @return a new instance of type which has a @Joint.id of <param>id</param>
      * @throws InstantiationException
      * @throws IllegalAccessException
      * @throws JointNotFoundException 
      */
     public static <T extends Jointed> T newJointInstance(
             final Class<? extends T> type, final String id) throws
             InstantiationException,
             IllegalAccessException,
             JointNotFoundException {
         Class<? extends T> jointClass = null;
         Object owner = new Object();
 
         synchronized (owner) {
             jointClass = findByJointId(type, id);
         }
 
         T joint = null;
         if (jointClass != null) {
             joint = jointClass.newInstance();
             if (joint != null && joint instanceof Loadable &&
                     joint.getClass().getAnnotation(Load.class).postConstruct()) {
                 ((Loadable) joint).load();
             }
         }
         return joint;
     }
 }
