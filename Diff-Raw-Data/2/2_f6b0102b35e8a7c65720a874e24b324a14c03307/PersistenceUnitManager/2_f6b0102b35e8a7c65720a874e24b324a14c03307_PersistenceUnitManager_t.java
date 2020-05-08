 /*
  * @(#)PersistenceUnitManager.java   10/04/19
  *
  * Copyright (c) 2010 Roger Suen(SUNRUJUN)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  */
 package org.javaplus.netbeans.api.persistence;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArraySet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.openide.util.Lookup;
 import org.openide.util.LookupEvent;
 import org.openide.util.LookupListener;
 import org.openide.util.lookup.Lookups;
 
 /**
  * The basic service for managing the registry of persistence units in
  * the Persistence Explorer.
  * 
  * @author Roger Suen
  */
 public final class PersistenceUnitManager {
 
     /**
      * The constant holds the name of layer folder in which all
      * persistence units are registered.
      */
     public static final String LAYER_FOLDER = "Persistence/PersistenceUnits";
     /**
      * The singleton default instance.
      */
     private static final PersistenceUnitManager DEFAULT =
             new PersistenceUnitManager();
     /**
      * The singleton instance of logger.
      */
     private static final Logger logger = Logger.getLogger(PersistenceUnitManager.class.getName());
     /**
      * The lookup result of <tt>PeristenceUnit</tt> instances.
      */
     private final Lookup.Result<PersistenceUnit> lookupResult;
     /**
      * The thread-safe set of listeners of the persistence unit registry.
      */
     private final Set<PersistenceUnitRegistryListener> registryListeners =
             new CopyOnWriteArraySet<PersistenceUnitRegistryListener>();
     /**
      * The singleton instance of registry change registryChangeEvent.
      */
     private final PersistenceUnitRegistryEvent registryChangeEvent =
             new PersistenceUnitRegistryEvent(this);
 
     /**
      * Constructor.
      */
     private PersistenceUnitManager() {
         lookupResult = Lookups.forPath(LAYER_FOLDER).lookupResult(PersistenceUnit.class);
         lookupResult.addLookupListener(new LookupListener() {
 
             public void resultChanged(LookupEvent ev) {
                 fireChangeEvent();
             }
         });
     }
 
     /**
      * Returns the default instance of the <tt>PersistenceUnitManager</tt>.
      * @return the default instance
      */
     public static PersistenceUnitManager getDefault() {
         return DEFAULT;
     }
 
     /**
      * Returns all registered persistence unit as an array.
      * @return an array of <tt>PersistenceUnit</tt>
      */
     public PersistenceUnit[] getUnits() {
         Collection<? extends PersistenceUnit> units = lookupResult.allInstances();
 
         if (logger.isLoggable(Level.FINER)) {
             String[] unitNames = new String[units.size()];
             int i = 0;
             for (PersistenceUnit unit : units) {
                 unitNames[i++] = unit.getName();
             }
             logger.log(Level.FINER, "{0} persistence unit(s) found: {1}",
                     new Object[]{unitNames.length, Arrays.toString(unitNames)});
         }
 
         return units.toArray(new PersistenceUnit[units.size()]);
     }
 
     /**
      * Adds the specified persistence unit to the registry.
      * 
      * @param pu the persistence unit to add, cannot be <tt>null</tt>
      * @throws NullPointerException if <tt>pu</tt> is <tt>null</tt>
      * @throws PersistenceUnitManagerException if unexpected error occurs
      */
     public void addUnit(PersistenceUnit pu) throws
             PersistenceUnitManagerException {
         if (pu == null) {
             throw new NullPointerException("null persistence unit");
         }
 
         if (logger.isLoggable(Level.FINER)) {
             logger.log(Level.FINER, "Adding persistence unit: {0}", pu);
         }
 
         try {
             PersistenceUnitConverter.writeToFileObject(pu);
         } catch (IOException ex) {
             logger.log(Level.WARNING,
                     "IOException occurred when writing the peristence unit "
                     + "to the file system: pu=" + pu
                     + " message=" + ex.getMessage(),
                     ex);
 
             throw new PersistenceUnitManagerException(
                     "Failed to write the persistence unit to the file system: "
                     + ex.getMessage(), ex);
         }
 
         if (logger.isLoggable(Level.FINER)) {
             logger.log(Level.FINER,
                     "Successfully added persistence unit: {0}",
                     pu.getName());
         }
     }
 
     /**
      * Add a listener that is notified each time the persistence unit registry
      * changes.
      * @param listener the listener to add, cannot be <tt>null</tt>
      * @throws NullPointerException if <tt>listener</tt> is <tt>null</tt>
      */
     public void addRegistryListener(PersistenceUnitRegistryListener listener) {
         if (listener == null) {
             throw new NullPointerException("null listener");
         }
         registryListeners.add(listener);
     }
 
     /**
      * Remove a listener that is notified each time the persistence unit
      * registry changes.
      * @param listener the listener to remove, cannot be <tt>null</tt>
      * @throws NullPointerException if <tt>listener</tt> is <tt>null</tt>
      */
     public void removeRegistryListener(
             PersistenceUnitRegistryListener listener) {
         if (listener == null) {
             throw new NullPointerException("null listener");
         }
         registryListeners.remove(listener);
     }
 
     private void fireChangeEvent() {
         if (logger.isLoggable(Level.FINER)) {
             logger.finer(
                     "The persistence unit registry changed, "
                     + "notifying all registered listeners");
         }
 
         for (PersistenceUnitRegistryListener listener : registryListeners) {
             listener.registryChanged(registryChangeEvent);
         }
 
         if (logger.isLoggable(Level.FINER)) {
             logger.log(Level.FINER,
                     "The persistence unit registry changed, "
                    + "{0} listener(s) were notified.",
                     registryListeners.size());
         }
     }
 }
