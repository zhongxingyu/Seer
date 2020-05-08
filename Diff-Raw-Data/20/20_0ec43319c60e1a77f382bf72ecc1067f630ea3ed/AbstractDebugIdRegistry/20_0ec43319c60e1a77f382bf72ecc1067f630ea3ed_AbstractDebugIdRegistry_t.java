 /**
  * This file is part of Project Control Center (PCC).
  * 
  * PCC (Project Control Center) project is intellectual property of 
  * Dmitri Anatol'evich Pisarenko.
  * 
  * Copyright 2010, 2011 Dmitri Anatol'evich Pisarenko
  * All rights reserved
  *
  **/
 
 package ru.altruix.commons.api.debugids;
 
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ru.altruix.commons.api.fbprefixes.AbstractMessageCodePrefixRegistry;
 
 /**
  * @author DP118M
  * 
  */
public abstract class AbstractDebugIdRegistry
    <F extends Enum<F>, C extends DebugIdKey<F>, 
    R extends AbstractMessageCodePrefixRegistry<F>> {
     private static final String DEBUGIDS_FILE = "debugids/debugids.properties";
     private static final Logger LOGGER = LoggerFactory
             .getLogger(AbstractDebugIdRegistry.class);
    private DebugIdKeyFactory<F, C> debugIdKeyFactory;
     private Set<String> alreadyFetchedKeys = new HashSet<String>();
     private Properties properties = new Properties();
     private List<C> debugKeys = new ArrayList<C>();
     private R messageCodePrefixRegistry;
 
     protected AbstractDebugIdRegistry(final R aPrefixRegistry) {
         this.debugIdKeyFactory = getDebugIdKeyFactory();
         loadData(DEBUGIDS_FILE);
         this.messageCodePrefixRegistry = aPrefixRegistry;
     }
 
     private void loadData(final String aFileName) {
         try {
             this.properties.load(getClass().getClassLoader()
                     .getResourceAsStream(aFileName));
         } catch (final Exception exception) {
             LOGGER.error(ErrorCodes.M_001_LOAD_DATA, exception);
         }
 
         for (final Enumeration<?> names = this.properties.propertyNames(); names
                 .hasMoreElements();) {
             final String name = (String) names.nextElement();
             final int index = name.indexOf(R
                     .getMessageNumberSeparator());
             if (index != -1) {
                 final C debugIdKey = debugIdKeyFactory.create();
                 debugIdKey.setModule(stringToEnum(name.substring(0, index)));
                 debugIdKey.setKey(name.substring(index + 1));
 
                 this.debugKeys.add(debugIdKey);
             }
         }
     }
 
    public final String getDebugId(final F aModule, final String aKey) {
         final C debugIdKey = debugIdKeyFactory.create();
         debugIdKey.setModule(aModule);
         debugIdKey.setKey(aKey);
 
         return this.getDebugId(debugIdKey);
     }
 
    public final void setDebugIdsFile(final String aFileName) {
         loadData(aFileName);
     }
 
    public final String getDebugId(final C aDebugIdKey) {
         final String aModuleWithKey =
                 aDebugIdKey.getModule().toString()
                        + AbstractMessageCodePrefixRegistry
                                .getMessageNumberSeparator()
                         + aDebugIdKey.getKey();
 
         if (!this.debugKeys.contains(aDebugIdKey)) {
             throw new DebugIdKeyNotFoundException(aModuleWithKey);
         }
         if (this.alreadyFetchedKeys.contains(aModuleWithKey)) {
             throw new DebugIdUniquenessViolation(aModuleWithKey);
         }
 
         this.alreadyFetchedKeys.add(aModuleWithKey);
 
         return (this.messageCodePrefixRegistry.getPrefix(
                 aDebugIdKey.getModule()) + this.properties
                 .getProperty(aModuleWithKey));
     }
 
     protected abstract F stringToEnum(final String aString);
 
     protected abstract DebugIdKeyFactory<F, C> getDebugIdKeyFactory();
 }
