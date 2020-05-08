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
 
 package ru.altruix.commons.api.commons;
 
 import java.util.Map;
 
 import ru.altruix.commons.api.fbprefixes.AbstractMessageCodePrefixRegistry;
 
 /**
  * @author DP118M
  * 
  */
 public class CommonsMessageCodePrefixRegistry extends
         AbstractMessageCodePrefixRegistry<CommonsFunctionalBlock> {
     private static volatile CommonsMessageCodePrefixRegistry instance;
 
     public static CommonsMessageCodePrefixRegistry getInstance() {
         if (instance == null) {
             instance = new CommonsMessageCodePrefixRegistry();
         }
         return instance;
     }
 
     @Override
    protected final
             void
             fillPrefixesByModules(
                     final Map<CommonsFunctionalBlock, String> aPrefixesByFunctionalBlocks) {
         aPrefixesByFunctionalBlocks.put(CommonsFunctionalBlock.conventions,
                 "001");
         aPrefixesByFunctionalBlocks.put(CommonsFunctionalBlock.commons, "002");
         aPrefixesByFunctionalBlocks.put(CommonsFunctionalBlock.debugids, "003");
         aPrefixesByFunctionalBlocks.put(CommonsFunctionalBlock.di, "004");
         aPrefixesByFunctionalBlocks.put(CommonsFunctionalBlock.fbprefixes,
                 "005");
         aPrefixesByFunctionalBlocks.put(CommonsFunctionalBlock.gui, "006");
         aPrefixesByFunctionalBlocks.put(CommonsFunctionalBlock.vaadin, "007");
         aPrefixesByFunctionalBlocks.put(CommonsFunctionalBlock.version, "008");
     }
 }
