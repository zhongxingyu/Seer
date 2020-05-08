 /*
  * Copyright © 2012 Sebastian Hoß <mail@shoss.de>
  * This work is free. You can redistribute it and/or modify it under the
  * terms of the Do What The Fuck You Want To Public License, Version 2,
  * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
  */
 package com.github.sebhoss.contract.verifier.impl;
 
 import ch.qos.cal10n.BaseName;
 import ch.qos.cal10n.Locale;
 import ch.qos.cal10n.LocaleData;
 
 import com.github.sebhoss.common.annotation.CompilerWarnings;
 import com.github.sebhoss.contract.verifier.ContractExceptionFactory;
 
 /**
  * Enumeration of errors of a {@link ContractExceptionFactory}
  */
 @BaseName("errors")
 @LocaleData({ @Locale("en") })
 @SuppressWarnings(CompilerWarnings.JAVADOC)
 public enum ExceptionFactoryErrors {
 
     TYPE_IS_ABSTRACT,
 
    NO_ACCESSIBLE_CONSTRUCTOR;
 
 }
