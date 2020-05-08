 /*
  * Copyright (C) 2013 University of Edinburgh.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.org.ukfederation.mda.validate;
 
 import java.math.BigInteger;
 import java.security.PublicKey;
 import java.security.cert.X509Certificate;
 import java.security.interfaces.RSAPublicKey;
 
 import javax.annotation.Nonnull;
 import javax.annotation.concurrent.ThreadSafe;
 
 import net.shibboleth.metadata.Item;
 import net.shibboleth.utilities.java.support.logic.Constraint;
 
 /**
  * Validator class to check RSA public exponent values in X.509 certificates.
  * 
  * An instance of the class can be configured to have both a warning boundary and an
  * error boundary. The default is to give an error for any exponent less than or equal to
  * three, with no provision for warnings.
  * 
  * This NIST recommendation is for at least 65537 (2**16+1) but it's not obvious where
  * this came from so doesn't seem worth insisting on by default.
  */
 @ThreadSafe
 public class X509CertificateRSAExponentValidator extends AbstractX509CertificateValidator {
 
     /** The RSA public exponent value below which an error should result. Default: 5. */
     private BigInteger errorBoundary = BigInteger.valueOf(5);
     
     /** The RSA public exponent value below which a warning should result. Default: 0 (disabled). */
     private BigInteger warningBoundary = BigInteger.ZERO;
     
     /**
      * Constructor.
      */
     public X509CertificateRSAExponentValidator() {
         super();
         setId("RSAExponent");
     }
 
     /**
      * Get the RSA public exponent below which an error will result.
      * 
      * @return the RSA public exponent below which an error will result.
      */
     public long getErrorBoundary() {
         return errorBoundary.longValue();
     }
     
     /**
      * Set the RSA public exponent below which an error should result.
      * 
      * @param length the RSA public exponent below which an error should result
      */
     public void setErrorBoundary(final long length) {
         Constraint.isGreaterThanOrEqual(0, length, "boundary value must not be negative");
         errorBoundary = BigInteger.valueOf(length);
     }
     
     /**
      * Get the RSA public exponent below which a warning will result.
      * 
      * @return the RSA public exponent below which a warning will result.
      */
     public long getWarningBoundary() {
         return warningBoundary.longValue();
     }
     
     /**
      * Set the RSA public exponent below which a warning should result.
      * 
      * @param length the RSA public exponent below which a warning should result
      */
     public void setWarningBoundary(final long length) {
         Constraint.isGreaterThanOrEqual(0, length, "boundary value must not be negative");
         warningBoundary = BigInteger.valueOf(length);
     }
     
     /** {@inheritDoc} */
     @Override
     public void validate(@Nonnull final X509Certificate cert, @Nonnull final Item<?> item,
             @Nonnull final String stageId) {
         final PublicKey key = cert.getPublicKey();
         if ("RSA".equals(key.getAlgorithm())) {
             final RSAPublicKey rsaKey = (RSAPublicKey) key;
             final BigInteger exponent = rsaKey.getPublicExponent();
             if (!exponent.testBit(0)) {
                addError("RSA publica exponent of " + exponent + " must be odd", item, stageId);
             } else if (exponent.compareTo(errorBoundary) < 0) {
                 addError("RSA public exponent of " + exponent + " is less than required " + errorBoundary,
                         item, stageId);
             } else if (exponent.compareTo(warningBoundary) < 0) {
                 addWarning("RSA public exponent of " + exponent + " is less than recommended " + warningBoundary,
                         item, stageId);
             }
         }
     }
 
 }
