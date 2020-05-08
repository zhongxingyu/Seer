 /*
  * Copyright © 2012 Sebastian Hoß <mail@shoss.de>
  * This work is free. You can redistribute it and/or modify it under the
  * terms of the Do What The Fuck You Want To Public License, Version 2,
  * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
  */
 package com.github.sebhoss.guice;
 
 import org.junit.Assert;
import org.junit.Rule;
 import org.junit.Test;
import org.junit.rules.ExpectedException;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 
 /**
  * Test cases for the {@link InsuranceCompany}.
  */
@SuppressWarnings({ "static-method", "null", "nls" })
 public class InsuranceCompanyTest {
 
    /** Catches expected exceptions */
    @Rule
    public ExpectedException thrown = ExpectedException.none();

     /**
      * Ensures that the insurance company can calculate payouts based on a positive damage input.
      */
     @Test
     public void shouldAcceptPositiveDamages() {
         // Given
         final Injector injector = Guice.createInjector(new GuiceModule());
         final InsuranceCompany instance = injector.getInstance(InsuranceCompany.class);
 
         // When
         final double result = instance.calculateCover(10);
 
         // Then
         Assert.assertEquals(5.0, result, 0d);
     }
 
     /**
      * Ensures that the insurance company can calculate payouts based on a positive damage input.
      */
     @Test
     public void shouldNotAcceptNegativeDamages() {
         // Given
         final Injector injector = Guice.createInjector(new GuiceModule());
         final InsuranceCompany instance = injector.getInstance(InsuranceCompany.class);
 
         // When
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Reported damage must be positive!");
 
         // Then
        instance.calculateCover(-10);
     }
 
 }
