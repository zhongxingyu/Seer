 /*******************************************************************************
  * Copyright (c) 2004, 2012 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.core.functions;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 
 import org.eclipse.jubula.tools.exception.InvalidDataException;
 
 /**
  * Function that rounds a number to a given number of decimal places.
  */
 public class RoundFunctionEvaluator extends AbstractFunctionEvaluator {
 
     /**
      * 
      * {@inheritDoc}
      * @throws InvalidDataException 
      */
     public String evaluate(String[] arguments) throws InvalidDataException {
         validateParamCount(arguments, 2);
        BigDecimal toRound = new BigDecimal(arguments[0]);
         int precision = Integer.parseInt(arguments[1]);
 
        BigDecimal rounded = 
                toRound.setScale(precision, RoundingMode.HALF_UP);
 
        return rounded.toPlainString();
     }
 
 }
