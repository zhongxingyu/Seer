 /*
  * Copyright (c) 2010 Paul Merlin <paul@nosphere.org>
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.codeartisans.qipki.ca.domain.ca;
 
 import java.io.StringReader;
 import java.math.BigInteger;
 import java.security.GeneralSecurityException;
 import java.security.KeyStoreException;
 import java.security.PrivateKey;
 import java.security.SecureRandom;
 import java.security.cert.X509CRL;
 import java.security.cert.X509Certificate;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.bouncycastle.asn1.misc.MiscObjectIdentifiers;
 import org.bouncycastle.asn1.misc.NetscapeCertType;
 import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
 import org.bouncycastle.asn1.x509.BasicConstraints;
 import org.bouncycastle.asn1.x509.CRLNumber;
 import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
 import org.bouncycastle.asn1.x509.KeyUsage;
 import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
 import org.bouncycastle.asn1.x509.X509Extensions;
 import org.bouncycastle.jce.PKCS10CertificationRequest;
 import org.bouncycastle.jce.provider.BouncyCastleProvider;
 import org.bouncycastle.x509.X509V2CRLGenerator;
 import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
 
 import org.codeartisans.qipki.ca.application.WrongParametersBuilder;
 import org.codeartisans.qipki.ca.domain.ca.profileassignment.X509ProfileAssignment;
 import org.codeartisans.qipki.ca.domain.ca.root.RootCAMixin;
 import org.codeartisans.qipki.ca.domain.revocation.Revocation;
 import org.codeartisans.qipki.ca.domain.revocation.RevocationFactory;
 import org.codeartisans.qipki.ca.domain.x509.X509;
 import org.codeartisans.qipki.ca.domain.x509profile.X509Profile;
 import org.codeartisans.qipki.core.QiPkiFailure;
 import org.codeartisans.qipki.crypto.algorithms.SignatureAlgorithm;
 import org.codeartisans.qipki.crypto.constants.Time;
 import org.codeartisans.qipki.crypto.io.CryptIO;
 import org.codeartisans.qipki.crypto.x509.RevocationReason;
 import org.codeartisans.qipki.crypto.x509.X509Generator;
 import org.codeartisans.qipki.crypto.x509.X509ExtensionsReader;
 import org.codeartisans.qipki.crypto.x509.X509ExtensionHolder;
 import org.codeartisans.qipki.crypto.x509.X509ExtensionsBuilder;
 
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
 
 import org.qi4j.api.injection.scope.Service;
 import org.qi4j.api.injection.scope.This;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public abstract class CAMixin
         implements CABehavior
 {
 
     private static final Logger LOGGER = LoggerFactory.getLogger( RootCAMixin.class );
     private static final Set<String> ALLOWED_REQUESTED_EXTENSIONS = new HashSet<String>();
 
     static {
         ALLOWED_REQUESTED_EXTENSIONS.add( X509Extensions.SubjectAlternativeName.getId() );
     }
 
     @Service
     private X509Generator x509Generator;
     @Service
     private X509ExtensionsReader x509ExtReader;
     @Service
     private X509ExtensionsBuilder x509ExtBuilder;
     @Service
     private CryptIO cryptIO;
     @Service
     private RevocationFactory revocationFactory;
     @This
     private CAState state;
 
     @Override
     public X509Certificate certificate()
     {
         try {
             return ( X509Certificate ) state.cryptoStore().get().loadKeyStore().getCertificate( state.identity().get() );
         } catch ( KeyStoreException ex ) {
             throw new QiPkiFailure( "Unable to load " + state.name().get() + " X509Certificate", ex );
         }
     }
 
     @Override
     public PrivateKey privateKey()
     {
         try {
             return ( PrivateKey ) state.cryptoStore().get().loadKeyStore().getKey( state.identity().get(), state.cryptoStore().get().password().get() );
         } catch ( GeneralSecurityException ex ) {
             throw new QiPkiFailure( "Unable to load " + state.name().get() + " PrivateKey", ex );
         }
     }
 
     @Override
     public X509Certificate sign( X509Profile x509profile, PKCS10CertificationRequest pkcs10 )
     {
         LOGGER.debug( "Handling a PKCS#10 Certificate Signing Request using X509Profile " + x509profile.name().get() );
         try {
 
             ensureX509ProfileIsAllowed( x509profile );
 
             List<X509ExtensionHolder> extensions = x509ExtReader.extractRequestedExtensions( pkcs10 );
             ensureNoIllegalRequestedExtensions( extensions );
 
             // Adding extensions commons to all profiles
             SubjectKeyIdentifier subjectKeyID = x509ExtBuilder.buildSubjectKeyIdentifier( pkcs10.getPublicKey() );
             extensions.add( new X509ExtensionHolder( X509Extensions.SubjectKeyIdentifier, false, subjectKeyID ) );
             AuthorityKeyIdentifier authKeyID = x509ExtBuilder.buildAuthorityKeyIdentifier( certificate().getPublicKey() );
             extensions.add( new X509ExtensionHolder( X509Extensions.AuthorityKeyIdentifier, false, authKeyID ) );
 
             // Applying X509Profile on issued X509Certificate
             if ( x509profile.basicConstraints().get().subjectIsCA().get() ) {
                 BasicConstraints bc = x509ExtBuilder.buildCABasicConstraints( x509profile.basicConstraints().get().pathLengthConstraint().get() );
                 extensions.add( new X509ExtensionHolder( X509Extensions.BasicConstraints, x509profile.basicConstraints().get().critical().get(), bc ) );
             } else {
                 BasicConstraints bc = x509ExtBuilder.buildNonCABasicConstraints();
                 extensions.add( new X509ExtensionHolder( X509Extensions.BasicConstraints, x509profile.basicConstraints().get().critical().get(), bc ) );
             }
             KeyUsage keyUsages = x509ExtBuilder.buildKeyUsages( x509profile.keyUsages().get().keyUsages().get() );
             extensions.add( new X509ExtensionHolder( X509Extensions.KeyUsage, x509profile.keyUsages().get().critical().get(), keyUsages ) );
 
             // TODO add ExtendedKeyUsages and NetscapeCertTypes
 
             ExtendedKeyUsage extendedKeyUsage = x509ExtBuilder.buildExtendedKeyUsage( x509profile.extendedKeyUsages().get().extendedKeyUsages().get() );
             extensions.add( new X509ExtensionHolder( X509Extensions.ExtendedKeyUsage, x509profile.extendedKeyUsages().get().critical().get(), extendedKeyUsage ) );
 
             NetscapeCertType netscapeCertType = x509ExtBuilder.buildNetscapeCertTypes( x509profile.netscapeCertTypes().get().netscapeCertTypes().get() );
             extensions.add( new X509ExtensionHolder( MiscObjectIdentifiers.netscapeCertType, x509profile.netscapeCertTypes().get().critical().get(), netscapeCertType ) );
 
             // TODO Climb up the CA hierarchy to add inherited CRL distpoints
             // CRLDistPoint crlDistPoints = x509ExtBuilder.buildCRLDistributionPoints( certificate().getSubjectX500Principal(), "http://qipki.org/crl" );
             // extensions.add( new X509ExtensionHolder( X509Extensions.CRLDistributionPoints, false, crlDistPoints ) );
 
             X509Certificate certificate = x509Generator.generateX509Certificate( privateKey(),
                                                                                  certificate().getSubjectX500Principal(),
                                                                                  BigInteger.probablePrime( 120, new SecureRandom() ),
                                                                                  pkcs10.getCertificationRequestInfo().getSubject(),
                                                                                  pkcs10.getPublicKey(),
                                                                                 Duration.standardDays( 365 ),
                                                                                  extensions );
 
             return certificate;
 
         } catch ( GeneralSecurityException ex ) {
             LOGGER.error( ex.getMessage(), ex );
             throw new QiPkiFailure( "Unable to enroll PKCS#10", ex );
         }
     }
 
     private void ensureX509ProfileIsAllowed( X509Profile x509profile )
     {
         for ( X509ProfileAssignment eachAssignment : state.allowedX509Profiles() ) {
             if ( eachAssignment.x509Profile().get().equals( x509profile ) ) {
                 return;
             }
         }
         throw new WrongParametersBuilder().illegals( "X509Profile " + x509profile.name().get() + " is not allowed on CA " + state.name().get() ).build();
     }
 
     private void ensureNoIllegalRequestedExtensions( List<X509ExtensionHolder> requestedExtensions )
     {
         for ( X509ExtensionHolder eachDerOid : requestedExtensions ) {
             if ( !ALLOWED_REQUESTED_EXTENSIONS.contains( eachDerOid.getDerOID().getId() ) ) {
                 throw new WrongParametersBuilder().illegals( "Illegal requested extension in PKCS#10 request: " + eachDerOid.getDerOID().getId() ).build();
             }
         }
     }
 
     @Override
     public Revocation revoke( X509 x509, RevocationReason reason )
     {
         try {
             Revocation revocation = revocationFactory.create( x509, reason );
             X509CRL x509CRL = cryptIO.readCRLPEM( new StringReader( state.crl().get().pem().get() ) );
             x509CRL = updateCRL( x509CRL, x509.x509Certificate(), reason );
             state.crl().get().pem().set( cryptIO.asPEM( x509CRL ).toString() );
             return revocation;
         } catch ( GeneralSecurityException ex ) {
             throw new QiPkiFailure( "Unable to update CRL", ex );
         }
     }
 
     // TODO move CRL updating crypto code into a crypto service
     private X509CRL updateCRL( X509CRL previousCRL, X509Certificate cert, RevocationReason reason )
             throws GeneralSecurityException
     {
         X509Certificate caCert = certificate();
         X509V2CRLGenerator crlGen = new X509V2CRLGenerator();
         crlGen.setIssuerDN( caCert.getSubjectX500Principal() );
         DateTime skewedNow = new DateTime().minus( Time.CLOCK_SKEW );
         crlGen.setThisUpdate( skewedNow.toDate() );
         crlGen.setNextUpdate( skewedNow.plusHours( 12 ).toDate() );
         crlGen.setSignatureAlgorithm( SignatureAlgorithm.SHA256withRSA.algoString() );
         crlGen.addExtension( X509Extensions.AuthorityKeyIdentifier, false, new AuthorityKeyIdentifierStructure( caCert ) );
         state.crl().get().lastCRLNumber().set( state.crl().get().lastCRLNumber().get().add( BigInteger.ONE ) );
         crlGen.addExtension( X509Extensions.CRLNumber, false, new CRLNumber( state.crl().get().lastCRLNumber().get() ) );
         crlGen.addCRL( previousCRL );
         crlGen.addCRLEntry( cert.getSerialNumber(), skewedNow.toDate(), reason.reason() );
         return crlGen.generate( privateKey(), BouncyCastleProvider.PROVIDER_NAME );
     }
 
 }
