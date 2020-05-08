 /**********************************************************************
  * $Source: /cvsroot/jameica/jameica.ca/src/de/willuhn/jameica/ca/store/EntryFactory.java,v $
 * $Revision: 1.10 $
 * $Date: 2012/03/28 22:19:40 $
  * $Author: willuhn $
  * $Locker:  $
  * $State: Exp $
  *
  * Copyright (c) by willuhn software & services
  * All rights reserved
  *
  **********************************************************************/
 
 package de.willuhn.jameica.ca.store;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.math.BigInteger;
 import java.security.KeyPair;
 import java.security.KeyPairGenerator;
 import java.security.PrivateKey;
 import java.security.Provider;
 import java.security.SecureRandom;
 import java.security.Security;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Vector;
 
 import org.bouncycastle.asn1.ASN1InputStream;
 import org.bouncycastle.asn1.ASN1Sequence;
 import org.bouncycastle.asn1.DERObjectIdentifier;
 import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
 import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
 import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
 import org.bouncycastle.asn1.x509.X509Extensions;
 import org.bouncycastle.asn1.x509.X509Name;
 import org.bouncycastle.jce.provider.BouncyCastleProvider;
 import org.bouncycastle.x509.X509V3CertificateGenerator;
 
 import de.willuhn.jameica.ca.store.format.Format;
 import de.willuhn.jameica.ca.store.template.Attribute;
 import de.willuhn.jameica.ca.store.template.Extension;
 import de.willuhn.jameica.ca.store.template.Template;
 import de.willuhn.logging.Logger;
 import de.willuhn.util.ProgressMonitor;
 
 /**
  * Factory zum Erzeugen, Lesen und Schreiben von Schluesseln.
  */
 public class EntryFactory
 {
   private Callback callback = null;
   
   static
   {
     if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null)
     {
       Provider p = new BouncyCastleProvider();
       Logger.info("applying security provider " + p.getInfo());
       Security.addProvider(p);
     }
 
   }
   
   /**
    * ct.
    * @param callback der Callback fuer Passwort-Rueckfragen.
    */
   public EntryFactory(Callback callback)
   {
     this.callback = callback;
   }
   
   /**
    * Liest ein Schluesselpaar ein. 
    * @param cert Dateiname des Zertifikates.
    * @param privateKey Dateiname des Private-Keys. Optional.
   * @param format das Format des Keys.
    * @return der erzeugte Entry.
    * @throws Exception
    */
   public Entry read(File cert, File privateKey, Format format) throws Exception
   {
     InputStream is = null;
 
     try
     {
       Entry e = new Entry();
 
       // Public Key
       is = new BufferedInputStream(new FileInputStream(cert));
       e.setCertificate(format.readCertificate(is));
       is.close();
       is = null;
 
       // Private Key
       if (privateKey != null)
       {
         is = new BufferedInputStream(new FileInputStream(privateKey));
         PrivateKey key = null;
         
         // Wir versuchen es erstmal mit einem leeren Passwort.
         // Das wird bei Webserver-Zertifikaten haeufig so gemacht.
         // Wenn das fehlschlaegt, koennen wir den User allemal noch fragen
         try
         {
           Logger.info("trying to read " + privateKey + " with empty password");
           key = format.readPrivateKey(is,new char[0]);
           
           // Wir machen noch einen Test zur sicherheit
           if (key.getEncoded().length == 0)
             throw new Exception();
         }
         catch (Exception ex)
         {
           Logger.info(privateKey + " seems to have a password, asking user");
           key = format.readPrivateKey(is,this.callback.getPassword(privateKey));
         }
         
         e.setPrivateKey(key);
         is.close();
         is = null;
       }
 
       return e;
     }
     finally
     {
       if (is != null)
         is.close();
     }
   }
   
   /**
    * Schreibt einen Schluessel in die angegebenen Dateien.
    * @param entry der zu exportierende Schluessel.
    * @param cert Dateiname fuer das Zertifikate.
    * @param privateKey Dateiname fuer den Private-Key. Optional.
    * @param format das Dateiformat.
    * @throws Exception
    */
   public void write(Entry entry, File cert, File privateKey, Format format) throws Exception
   {
     OutputStream os = null;
 
     try
     {
       os = new BufferedOutputStream(new FileOutputStream(cert));
       format.writeCertificate(entry.getCertificate(),os);
       os.close();
       os = null;
 
       if (privateKey != null)
       {
         PrivateKey key = entry.getPrivateKey();
         if (key != null)
         {
           os = new BufferedOutputStream(new FileOutputStream(privateKey));
           format.writePrivateKey(key,os);
           os.close();
           os = null;
         }
       }
     }
     finally
     {
       if (os != null)
         os.close();
     }
   }
   
   /**
    * Erstellt einen neuen Schluessel basierend auf dem angegebenen Template.
    * @param template das Template.
    * @param monitor optionaler Fortschritts-Monitor.
    * @return der erstellte Schluessel.
    * @throws Exception
    */
   public Entry create(Template template, ProgressMonitor monitor) throws Exception
   {
     if (monitor != null) monitor.setStatus(ProgressMonitor.STATUS_RUNNING);
 
     try
     {
       Entry entry = new Entry();
       Entry issuer = template.getIssuer();
 
 
       if (monitor != null)
       {
         monitor.addPercentComplete(5);
         monitor.setStatusText("creating key pair");
       }
       Logger.info("creating key pair");
       
 
       ////////////////////////////////////////////////////////////////////////////
       // Schluesselpaar erstellen
       KeyPairGenerator kp = KeyPairGenerator.getInstance("RSA",BouncyCastleProvider.PROVIDER_NAME);
       kp.initialize(template.getKeySize());
       KeyPair keypair = kp.generateKeyPair();
       entry.setPrivateKey(keypair.getPrivate());
       //
       ////////////////////////////////////////////////////////////////////////////
 
 
       Logger.info("creating certificate");
       if (monitor != null)
       {
         monitor.setStatusText("creating certificate");
         monitor.addPercentComplete(20);
       }
       
 
       ////////////////////////////////////////////////////////////////////////////
       // Generator initialisieren
       X509V3CertificateGenerator generator = new X509V3CertificateGenerator();
       generator.setPublicKey(keypair.getPublic());
       generator.setSignatureAlgorithm(template.getSignatureAlgorithm());
       ////////////////////////////////////////////////////////////////////////////
 
 
       Logger.info("  algorithm: " + template.getSignatureAlgorithm());
       if (monitor != null) monitor.addPercentComplete(20);
 
       
       //////////////////////////////////////////////////////////////////////////
       // Gueltigkeit
       generator.setNotAfter(template.getValidTo());
       generator.setNotBefore(template.getValidFrom());
       //////////////////////////////////////////////////////////////////////////
 
       
       Logger.info("  validity: " + template.getValidFrom() + " - " + template.getValidTo());
       if (monitor != null) monitor.addPercentComplete(10);
 
       
       //////////////////////////////////////////////////////////////////////////
       // Attribute
       Hashtable<DERObjectIdentifier, String> props = new Hashtable();
       Vector<DERObjectIdentifier> order = new Vector<DERObjectIdentifier>();
       List<Attribute> attributes = template.getAttributes();
       for (Attribute a:attributes)
       {
         String value = a.getValue();
         if (value == null || value.length() == 0)
           continue;
         DERObjectIdentifier oid = new DERObjectIdentifier(a.getOid());
         props.put(oid,value);
         order.add(oid);
       }
       generator.setSubjectDN(new X509Name(order,props));
       //////////////////////////////////////////////////////////////////////////
 
       
       if (monitor != null) monitor.addPercentComplete(10);
 
       
       //////////////////////////////////////////////////////////////////////////
       // Seriennummer
       byte[] serno = new byte[8];
       SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
       random.setSeed((long) (new Date().getTime()));
       random.nextBytes(serno);
       BigInteger serial = new BigInteger(serno).abs();
       generator.setSerialNumber(serial);
       //////////////////////////////////////////////////////////////////////////
 
 
       Logger.info("  serial number: " + serial);
       if (monitor != null) monitor.addPercentComplete(10);
 
       
       //////////////////////////////////////////////////////////////////////////
       // Extensions
 
       // Template-Extensions
       List<Extension> extensions = template.getExtensions();
       for (Extension e:extensions)
       {
         generator.addExtension(e.getOid(),e.isCritical(),e.getValue());
       }
 
       // Subject Key-Identifier als Extension hinzufuegen
       SubjectPublicKeyInfo spki = new SubjectPublicKeyInfo((ASN1Sequence) new ASN1InputStream(new ByteArrayInputStream(keypair.getPublic().getEncoded())).readObject());
       SubjectKeyIdentifier ski = new SubjectKeyIdentifier(spki);
       generator.addExtension(X509Extensions.SubjectKeyIdentifier.getId(),false, ski);
 
       // CA Key-Identifier als Extension hinzufuegen
       if (issuer != null)
       {
         SubjectPublicKeyInfo apki = new SubjectPublicKeyInfo((ASN1Sequence) new ASN1InputStream(new ByteArrayInputStream(issuer.getCertificate().getPublicKey().getEncoded())).readObject());
         AuthorityKeyIdentifier aki = new AuthorityKeyIdentifier(apki);
         generator.addExtension(X509Extensions.AuthorityKeyIdentifier.getId(),false, aki);
       }
       //////////////////////////////////////////////////////////////////////////
 
 
       if (monitor != null) monitor.addPercentComplete(20);
 
 
       //////////////////////////////////////////////////////////////////////////
       // Aussteller
       PrivateKey key = null;
       
       if (issuer != null)
       {
         Logger.info("  issuer: " + issuer.getCertificate().getSubjectDN().getName());
         if (monitor != null) monitor.setStatusText("creating ca signed certificate");
         key = issuer.getPrivateKey();
         generator.setIssuerDN(issuer.getCertificate().getSubjectX500Principal());
       }
       else
       {
         Logger.info("  issuer: <selfsigned>");
         if (monitor != null) monitor.setStatusText("creating self signed certificate");
         key = entry.getPrivateKey();
         generator.setIssuerDN(new X509Name(order,props));
       }
       
       // Zertifikat erstellen
       entry.setCertificate(generator.generate(key));
       ////////////////////////////////////////////////////////////////////////////
 
       Logger.info("certificate created");
       if (monitor != null)
       {
         monitor.setPercentComplete(100);
         monitor.setStatus(ProgressMonitor.STATUS_DONE);
         monitor.setStatusText("certificate created successfully");
       }
       
       
       return entry;
     }
     catch (Exception e)
     {
       if (monitor != null)
       {
         monitor.setPercentComplete(100);
         monitor.setStatus(ProgressMonitor.STATUS_ERROR);
         monitor.setStatusText("error while creating certificate: " + e.getMessage());
       }
       throw e;
     }
   }
 }
 
 
 /**********************************************************************
  * $Log: EntryFactory.java,v $
 * Revision 1.10  2012/03/28 22:19:40  willuhn
 * @D javadoc Fixes
 *
  * Revision 1.9  2009/10/27 16:47:20  willuhn
  * @N Support zum Ueberschreiben/als Kopie anlegen beim Import
  * @N Integration in Jameica-Suche
  *
  * Revision 1.8  2009/10/15 15:25:25  willuhn
  * @N Reload des Tree nach Erstellen/Loeschen eines Schluessels
  *
  * Revision 1.7  2009/10/07 16:38:59  willuhn
  * @N GUI-Code zum Anzeigen und Importieren von Schluesseln
  *
  * Revision 1.6  2009/10/07 11:47:59  willuhn
  * *** empty log message ***
  *
  * Revision 1.5  2009/10/07 11:39:28  willuhn
  * *** empty log message ***
  *
  * Revision 1.4  2009/10/06 16:47:58  willuhn
  * @N Aussteller fehlte
  *
  * Revision 1.3  2009/10/06 16:36:00  willuhn
  * @N Extensions
  * @N PEM-Writer
  *
  * Revision 1.2  2009/10/06 00:27:37  willuhn
  * *** empty log message ***
  *
  * Revision 1.1  2009/10/05 16:02:38  willuhn
  * @N Neues Jameica-Plugin: "jameica.ca" - ein Certifcate-Authority-Tool zum Erstellen und Verwalten von SSL-Zertifikaten
  *
  **********************************************************************/
