 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the Common Development and Distribution License, Version 1.0
  * only (the "License"). You may not use this file except in compliance with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE or http://www.escidoc.de/license. See the License for
  * the specific language governing permissions and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each file and include the License file at
  * license/ESCIDOC.LICENSE. If applicable, add the following below this CDDL HEADER, with the fields enclosed by
  * brackets "[]" replaced with your own identifying information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  *
  * Copyright 2006-2011 Fachinformationszentrum Karlsruhe Gesellschaft fuer wissenschaftlich-technische Information mbH
  * and Max-Planck-Gesellschaft zur Foerderung der Wissenschaft e.V. All rights reserved. Use is subject to license
  * terms.
  */
 
 package de.escidoc.core.common.util.aop;
 
 import de.escidoc.core.common.annotation.Validate;
 import de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException;
 import de.escidoc.core.common.exceptions.application.invalid.XmlSchemaValidationException;
 import de.escidoc.core.common.exceptions.system.WebserverSystemException;
 import de.escidoc.core.common.exceptions.system.XmlParserSystemException;
 import de.escidoc.core.common.util.xml.XmlUtility;
 import org.aspectj.lang.JoinPoint;
 import org.aspectj.lang.annotation.Aspect;
 import org.aspectj.lang.annotation.Before;
 import org.aspectj.lang.reflect.MethodSignature;
 import org.springframework.core.Ordered;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 
 /**
  * Interceptor to validate incoming XML documents. The validation takes only place if the called method is annotated
  * accordingly.
  *
  * @author Michael Schneider
  */
 @Aspect
 public class XmlValidationInterceptor implements Ordered {
 
     /**
      * See Interface for functional description.
      */
     @Override
     public int getOrder() {
 
         return AopUtil.PRECEDENCE_XML_VALIDATION_INTERCEPTOR;
     }
 
     /**
      * Before advice to perform the schema validation of xml data of the current request.
      *
      * @param joinPoint The current {@link JoinPoint}.
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      * @throws de.escidoc.core.common.exceptions.system.XmlParserSystemException
      * @throws de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException
      * @throws de.escidoc.core.common.exceptions.application.invalid.XmlSchemaValidationException
      */
    @Before("execution(public * de.escidoc.core.*.service.*.*(..))")
     public void validate(final JoinPoint joinPoint) throws XmlParserSystemException, WebserverSystemException,
         XmlSchemaValidationException, XmlCorruptedException {
 
         final Method calledMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
         final Annotation annotation = calledMethod.getAnnotation(Validate.class);
         if (annotation != null) {
             final Object[] arguments = joinPoint.getArgs();
             validate((String) arguments[((Validate) annotation).param()], ((Validate) annotation).resolver(),
                 ((Validate) annotation).root());
         }
     }
 
     /**
      * Validates the provided xml data using the specified schema.
      *
      * @param xml             The xml data to validate.
      * @param resolvingMethod The name of the resolving method used to identify the schema location.
      * @param root
      * @throws WebserverSystemException Thrown in case of an internal error.
      * @throws de.escidoc.core.common.exceptions.system.XmlParserSystemException
      * @throws de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException
      * @throws de.escidoc.core.common.exceptions.application.invalid.XmlSchemaValidationException
      */
     private void validate(final String xml, final String resolvingMethod, final String root)
         throws WebserverSystemException, XmlParserSystemException, XmlSchemaValidationException, XmlCorruptedException {
 
         XmlUtility.validate(xml, getSchemaLocation(resolvingMethod), root);
     }
 
     /**
      * Gets the location of the schema using the provided resolvingMethod value.
      *
      * @param resolvingMethod The name of the resolving method used to identify the schema location.
      * @return Returns the schema location.
      * @throws WebserverSystemException Thrown in case of an internal error.
      */
     private static String getSchemaLocation(final String resolvingMethod) throws WebserverSystemException {
         final Class[] paramTypes = {};
         try {
             final Method getSchemaLocationM = XmlUtility.class.getMethod(resolvingMethod, paramTypes);
             return (String) getSchemaLocationM.invoke(null);
         }
         catch (final Exception e) {
             throw new WebserverSystemException("Could not find schema location for schema " + resolvingMethod + '!', e);
         }
     }
 
 }
