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
 package org.eclipse.jubula.client.core.utils;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import org.eclipse.jubula.client.core.functions.FunctionDefinition;
 import org.eclipse.jubula.client.core.functions.FunctionRegistry;
 import org.eclipse.jubula.client.core.functions.IFunctionEvaluator;
 import org.eclipse.jubula.client.core.i18n.Messages;
 import org.eclipse.jubula.client.core.model.IParamDescriptionPO;
 import org.eclipse.jubula.client.core.utils.ParamValueConverter.ConvValidationState;
 import org.eclipse.jubula.tools.exception.InvalidDataException;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.osgi.util.NLS;
 
 /**
  * Token that represents a function call.
  */
 public class FunctionToken extends AbstractParamValueToken 
     implements INestableParamValueToken {
 
     /** the tokens comprising the function arguments */
     private IParamValueToken[] m_argTokens;
     
     /** the string at the beginning of the function : ?&lt;name&gt;(*/
     private String m_prefix;
 
     /** the string at the end of the function : ) */
     private String m_suffix;
     
     /** the name of the called Function */
     private String m_functionName;
     
     /**
      * Constructor
      * 
      * @param s the entire token
      * @param functionPrefix the text at the beginning of the token
      * @param functionSuffix the text at the end of the token
      * @param pos index of first character of token in entire string
      * @param desc param description belonging to currently edited parameter value
      * @param functionName the name of the called Function
      * @param argTokens the tokens that comprise the arguments for the function
      */
     public FunctionToken(String s,
             String functionPrefix, String functionSuffix, 
             int pos, IParamDescriptionPO desc, 
             String functionName,
             IParamValueToken[] argTokens) {
 
         super(s, pos, desc);
         m_argTokens = argTokens;
         m_prefix = functionPrefix;
         m_suffix = functionSuffix;
         m_functionName = functionName;
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public ConvValidationState validate() {
 
         FunctionDefinition function =
                 FunctionRegistry.getInstance().getFunction(m_functionName);
         if (function == null) {
             setErrorKey(MessageIDs.E_FUNCTION_NOT_REGISTERED);
             return ConvValidationState.invalid;
         }
 
         int paramCount = function.getParameters().length;
         boolean hasVarArgs = function.getVarArgs() != null;
         int argCount = getArgumentCount();
         if ((!hasVarArgs && argCount != paramCount) 
                 || (hasVarArgs && argCount < paramCount)) {
 
             setErrorKey(MessageIDs.E_WRONG_NUM_FUNCTION_ARGS);
             return ConvValidationState.invalid;
         }
         
         ConvValidationState state = ConvValidationState.valid;
         Integer errorKey = null;
         
         for (IParamValueToken childToken : getNestedTokens()) {
             ConvValidationState childState = childToken.validate();
             if (childState == ConvValidationState.invalid) {
                 setErrorKey(childToken.getErrorKey());
                 return childState;
             }
             if (childState == ConvValidationState.undecided) {
                 state = childState;
                 errorKey = childToken.getErrorKey();
             }
         }
 
         setErrorKey(errorKey);
         return state;
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public String getExecutionString(List<ExecObject> stack, Locale locale) 
         throws InvalidDataException {
         
         FunctionDefinition function = 
                 FunctionRegistry.getInstance().getFunction(m_functionName);
         if (function == null) {
             throw new InvalidDataException(
                     NLS.bind(Messages.FunctionNotDefined, m_functionName), 
                     MessageIDs.E_NO_FUNCTION);
         }
         
         IFunctionEvaluator evaluator = function.getEvaluator();
         List<String> argList = new ArrayList<String>();
         StringBuilder argBuilder = new StringBuilder();
         for (IParamValueToken argToken : getNestedTokens()) {
             if (argToken instanceof FunctionArgumentSeparatorToken) {
                 argList.add(argBuilder.toString());
                 argBuilder.setLength(0);
             } else {
                 argBuilder.append(argToken.getExecutionString(stack, locale));
             }
         }
         argList.add(argBuilder.toString());
         
        try {
            return evaluator.evaluate(
                    argList.toArray(new String[argList.size()]));
        } catch (Throwable t) {
            if (t instanceof InvalidDataException) {
                throw (InvalidDataException)t;
            }

            throw new InvalidDataException(t.getLocalizedMessage(), 
                    MessageIDs.E_FUNCTION_EVAL_ERROR);
        }
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public String getGuiString() {
         StringBuilder guiStringBuilder = new StringBuilder();
         guiStringBuilder.append(m_prefix);
         
         for (IParamValueToken nestedToken : getNestedTokens()) {
             guiStringBuilder.append(nestedToken.getGuiString());
         }
         
         guiStringBuilder.append(m_suffix);
         return guiStringBuilder.toString();
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public String getModelString() {
         StringBuilder modelStringBuilder = new StringBuilder();
         modelStringBuilder.append(m_prefix);
         
         for (IParamValueToken nestedToken : getNestedTokens()) {
             modelStringBuilder.append(nestedToken.getModelString());
         }
         
         modelStringBuilder.append(m_suffix);
         return modelStringBuilder.toString();
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public IParamValueToken[] getNestedTokens() {
         return m_argTokens;
     }
 
     /**
      * 
      * @return the number of arguments entered for this Function.
      */
     private int getArgumentCount() {
         if (m_argTokens.length == 0) {
             return 0;
         }
         
         int argCount = 1;
         for (IParamValueToken token : m_argTokens) {
             if (token instanceof FunctionArgumentSeparatorToken) {
                 argCount++;
             }
         }
         
         return argCount;
     }
 }
