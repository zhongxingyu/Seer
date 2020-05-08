 /*
  * Copyright (C) 2013 Schlichtherle IT Services.
  * All rights reserved. Use is subject to license terms.
  */
 package de.schlichtherle.demo.guice.inject;
 
 import java.lang.annotation.*;
 import static java.lang.annotation.ElementType.*;
 import javax.inject.Qualifier;
 
 /**
  * A qualifier which defines a class as the context of an injection point.
  * Use like this:
  * <pre>{@code
 class MyPrinter implements Printer {
 
    &#64;Inject MyPrinter(@Context(MyPrinter.class) Printer printer) {
         ...
     }
 
     ...
 }
  * }</pre>
  * <p>
  * The annotation defines {@code MyPrinter} as the context of the injected
  * {@code printer} parameter.
  * When setting up an IOC container, this can be used to configure a specific
  * class as the dependency of this class.
  * Please check the documentation of your IOC container to see how qualifier
  * annotations can be used to select a particular dependency.
  *
  * @author Christian Schlichtherle
  */
 @Qualifier
 @Target({ FIELD, PARAMETER })
 @Retention(RetentionPolicy.RUNTIME)
 public @interface Context {
     /** The context class. */
     Class<?> value();
 }
