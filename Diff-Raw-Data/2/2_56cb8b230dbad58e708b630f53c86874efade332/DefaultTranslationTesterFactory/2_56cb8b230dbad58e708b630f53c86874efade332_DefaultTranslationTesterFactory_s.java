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
 
 package ru.altruix.commons.impl.translationtester;
 
 import ru.altruix.commons.api.translationtester.TranslationTester;
 import ru.altruix.commons.api.translationtester.TranslationTesterFactory;
 
 /**
  * @author DP118M
  *
  */
 public class DefaultTranslationTesterFactory implements
         TranslationTesterFactory {
 
     @Override
    public TranslationTester create() {
         return new DefaultTranslationTester();
     }
 }
