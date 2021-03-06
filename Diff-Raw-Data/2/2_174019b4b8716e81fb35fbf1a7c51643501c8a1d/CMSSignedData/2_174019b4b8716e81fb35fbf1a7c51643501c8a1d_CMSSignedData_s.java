 package org.bouncycastle.cms;
 
 import org.bouncycastle.asn1.ASN1EncodableVector;
 import org.bouncycastle.asn1.ASN1InputStream;
 import org.bouncycastle.asn1.ASN1OctetString;
 import org.bouncycastle.asn1.ASN1OutputStream;
 import org.bouncycastle.asn1.ASN1Sequence;
 import org.bouncycastle.asn1.ASN1Set;
 import org.bouncycastle.asn1.BERSequence;
 import org.bouncycastle.asn1.DERNull;
 import org.bouncycastle.asn1.DERObject;
 import org.bouncycastle.asn1.DERObjectIdentifier;
 import org.bouncycastle.asn1.DERSet;
 import org.bouncycastle.asn1.cms.ContentInfo;
 import org.bouncycastle.asn1.cms.SignedData;
 import org.bouncycastle.asn1.cms.SignerInfo;
 import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
 import org.bouncycastle.x509.NoSuchStoreException;
 import org.bouncycastle.x509.X509Store;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.security.NoSuchAlgorithmException;
 import java.security.NoSuchProviderException;
 import org.bouncycastle.jce.cert.CertStore;
 import org.bouncycastle.jce.cert.CertStoreException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 /**
  * general class for handling a pkcs7-signature message.
  *
  * A simple example of usage - note, in the example below the validity of
  * the certificate isn't verified, just the fact that one of the certs 
  * matches the given signer...
  *
  * <pre>
  *  CertStore               certs = s.getCertificatesAndCRLs("Collection", "BC");
  *  SignerInformationStore  signers = s.getSignerInfos();
  *  Collection              c = signers.getSigners();
  *  Iterator                it = c.iterator();
  *  
  *  while (it.hasNext())
  *  {
  *      SignerInformation   signer = (SignerInformation)it.next();
  *      Collection          certCollection = certs.getCertificates(signer.getSID());
  *  
  *      Iterator        certIt = certCollection.iterator();
  *      X509Certificate cert = (X509Certificate)certIt.next();
  *  
  *      if (signer.verify(cert.getPublicKey()))
  *      {
  *          verified++;
  *      }   
  *  }
  * </pre>
  */
 public class CMSSignedData
 {
     private static CMSSignedHelper HELPER = CMSSignedHelper.INSTANCE;
     
     SignedData              signedData;
     ContentInfo             contentInfo;
     CMSProcessable          signedContent;
     CertStore               certStore;
     SignerInformationStore  signerInfoStore;
     X509Store               attributeStore;
     X509Store               certificateStore;
     X509Store               crlStore;
     private Map             hashes;
 
     private CMSSignedData(
         CMSSignedData   c)
     {
         this.signedData = c.signedData;
         this.contentInfo = c.contentInfo;
         this.signedContent = c.signedContent;
         this.certStore = c.certStore;
         this.signerInfoStore = c.signerInfoStore;
     }
 
     public CMSSignedData(
         byte[]      sigBlock)
         throws CMSException
     {
         this(CMSUtils.readContentInfo(sigBlock));
     }
 
     public CMSSignedData(
         CMSProcessable  signedContent,
         byte[]          sigBlock)
         throws CMSException
     {
         this(signedContent, CMSUtils.readContentInfo(sigBlock));
     }
 
     /**
      * Content with detached signature, digests precomputed
      *
      * @param hashes a map of precomputed digests for content indexed by name of hash.
      * @param sigBlock the signature object.
      */
     public CMSSignedData(
         Map     hashes,
         byte[]  sigBlock)
         throws CMSException
     {
         this(hashes, CMSUtils.readContentInfo(sigBlock));
     }
 
     /**
      * base constructor - content with detached signature.
      *
      * @param signedContent the content that was signed.
      * @param sigData the signature object.
      */
     public CMSSignedData(
         CMSProcessable  signedContent,
         InputStream     sigData)
         throws CMSException
     {
         this(signedContent, CMSUtils.readContentInfo(new ASN1InputStream(sigData)));
     }
 
     /**
      * base constructor - with encapsulated content
      */
     public CMSSignedData(
         InputStream sigData)
         throws CMSException
     {
         this(CMSUtils.readContentInfo(sigData));
     }
 
     public CMSSignedData(
         CMSProcessable  signedContent,
         ContentInfo     sigData)
     {
         this.signedContent = signedContent;
         this.contentInfo = sigData;
         this.signedData = SignedData.getInstance(contentInfo.getContent());
     }
 
     public CMSSignedData(
         Map             hashes,
         ContentInfo     sigData)
     {
         this.hashes = hashes;
         this.contentInfo = sigData;
         this.signedData = SignedData.getInstance(contentInfo.getContent());
     }
 
     public CMSSignedData(
         ContentInfo sigData)
     {
         this.contentInfo = sigData;
         this.signedData = SignedData.getInstance(contentInfo.getContent());
 
         //
         // this can happen if the signed message is sent simply to send a
         // certificate chain.
         //
         if (signedData.getEncapContentInfo().getContent() != null)
         {
             this.signedContent = new CMSProcessableByteArray(
                     ((ASN1OctetString)(signedData.getEncapContentInfo()
                                                 .getContent())).getOctets());
         }
         else
         {
             this.signedContent = null;
         }
     }
 
     /**
      * Return the version number for this object
      */
     public int getVersion()
     {
         return signedData.getVersion().getValue().intValue();
     }
 
     /**
      * return the collection of signers that are associated with the
      * signatures for the message.
      */
     public SignerInformationStore getSignerInfos()
     {
         if (signerInfoStore == null)
         {
             ASN1Set         s = signedData.getSignerInfos();
             List            signerInfos = new ArrayList();
 
             for (int i = 0; i != s.size(); i++)
             {
                 if (hashes == null)
                 {
                     signerInfos.add(new SignerInformation(SignerInfo.getInstance(s.getObjectAt(i)), signedData.getEncapContentInfo().getContentType(), signedContent, null));
                 }
                 else
                 {
                     SignerInfo info = SignerInfo.getInstance(s.getObjectAt(i));
                     byte[] hash = (byte[])hashes.get(info.getDigestAlgorithm().getObjectId().getId());
 
                     signerInfos.add(new SignerInformation(info, signedData.getEncapContentInfo().getContentType(), null, hash));
                 }
             }
 
             signerInfoStore = new SignerInformationStore(signerInfos);
         }
 
         return signerInfoStore;
     }
 
     /**
      * return a X509Store containing the attribute certificates, if any, contained
      * in this message.
      *
      * @param type type of store to create
      * @param provider provider to use
      * @return a store of attribute certificates
      * @exception NoSuchProviderException if the provider requested isn't available.
      * @exception NoSuchStoreException if the store type isn't available.
      * @exception CMSException if a general exception prevents creation of the X509Store
      */
     public X509Store getAttributeCertificates(
         String type,
         String provider)
         throws NoSuchStoreException, NoSuchProviderException, CMSException
     {
         if (attributeStore == null)
         {
             attributeStore = HELPER.createAttributeStore(type, provider, signedData.getCertificates());
         }
 
         return attributeStore;
     }
 
     /**
      * return a X509Store containing the public key certificates, if any, contained
      * in this message.
      *
      * @param type type of store to create
      * @param provider provider to use
      * @return a store of public key certificates
      * @exception NoSuchProviderException if the provider requested isn't available.
      * @exception NoSuchStoreException if the store type isn't available.
      * @exception CMSException if a general exception prevents creation of the X509Store
      */
     public X509Store getCertificates(
         String type,
         String provider)
         throws NoSuchStoreException, NoSuchProviderException, CMSException
     {
         if (certificateStore == null)
         {
             certificateStore = HELPER.createCertificateStore(type, provider, signedData.getCertificates());
         }
 
         return certificateStore;
     }
 
     /**
      * return a X509Store containing CRLs, if any, contained
      * in this message.
      *
      * @param type type of store to create
      * @param provider provider to use
      * @return a store of CRLs
      * @exception NoSuchProviderException if the provider requested isn't available.
      * @exception NoSuchStoreException if the store type isn't available.
      * @exception CMSException if a general exception prevents creation of the X509Store
      */
     public X509Store getCRLs(
         String type,
         String provider)
         throws NoSuchStoreException, NoSuchProviderException, CMSException
     {
         if (crlStore == null)
         {
             crlStore = HELPER.createCRLsStore(type, provider, signedData.getCRLs());
         }
 
         return crlStore;
     }
 
     /**
      * return a CertStore containing the certificates and CRLs associated with
      * this message.
      *
      * @exception NoSuchProviderException if the provider requested isn't available.
      * @exception NoSuchAlgorithmException if the cert store isn't available.
      * @exception CMSException if a general exception prevents creation of the CertStore
      */
     public CertStore getCertificatesAndCRLs(
         String  type,
         String  provider)
         throws NoSuchAlgorithmException, NoSuchProviderException, CMSException
     {
         if (certStore == null)
         {
             ASN1Set certSet = signedData.getCertificates();
             ASN1Set crlSet = signedData.getCRLs();
 
             certStore = HELPER.createCertStore(type, provider, certSet, crlSet);
         }
 
         return certStore;
     }
 
     /**
      * Return the a string representation of the OID associated with the
      * encapsulated content info structure carried in the signed data.
      * 
      * @return the OID for the content type.
      */
     public String getSignedContentTypeOID()
     {
         return signedData.getEncapContentInfo().getContentType().getId();
     }
     
     public CMSProcessable getSignedContent()
     {
         return signedContent;
     }
 
     /**
      * return the ASN.1 encoded representation of this object.
      */
     public byte[] getEncoded()
         throws IOException
     {
         ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
         ASN1OutputStream        aOut = new ASN1OutputStream(bOut);
 
         aOut.writeObject(contentInfo);
 
         return bOut.toByteArray();
     }
     
     /**
      * Replace the signerinformation store associated with this
      * CMSSignedData object with the new one passed in. You would
      * probably only want to do this if you wanted to change the unsigned 
      * attributes associated with a signer, or perhaps delete one.
      * 
      * @param signedData the signed data object to be used as a base.
      * @param signerInformationStore the new signer information store to use.
      * @return a new signed data object.
      */
     public static CMSSignedData replaceSigners(
         CMSSignedData           signedData,
         SignerInformationStore  signerInformationStore)
     {
         //
         // copy
         //
         CMSSignedData   cms = new CMSSignedData(signedData);
         
         //
         // replace the store
         //
         cms.signerInfoStore = signerInformationStore;
 
         //
         // replace the signers in the SignedData object
         //
         ASN1EncodableVector digestAlgs = new ASN1EncodableVector();
         ASN1EncodableVector vec = new ASN1EncodableVector();
         
         Iterator    it = signerInformationStore.getSigners().iterator();
         while (it.hasNext())
         {
             SignerInformation   signer = (SignerInformation)it.next();
             AlgorithmIdentifier digAlgId;
 
             try
             {
                 digAlgId = makeAlgId(signer.getDigestAlgOID(),
                                                        signer.getDigestAlgParams());
             }
             catch (IOException e)
             {
                throw new RuntimeException("encoding error.", e);
             }
 
             digestAlgs.add(digAlgId);
             vec.add(signer.toSignerInfo());
         }
 
         ASN1Set             digests = new DERSet(digestAlgs);
         ASN1Set             signers = new DERSet(vec);
         ASN1Sequence        sD = (ASN1Sequence)signedData.signedData.getDERObject();
 
         vec = new ASN1EncodableVector();
         
         //
         // signers are the last item in the sequence.
         //
         vec.add(sD.getObjectAt(0)); // version
         vec.add(digests);
 
         for (int i = 2; i != sD.size() - 1; i++)
         {
             vec.add(sD.getObjectAt(i));
         }
         
         vec.add(signers);
         
         cms.signedData = SignedData.getInstance(new BERSequence(vec));
         
         //
         // replace the contentInfo with the new one
         //
         cms.contentInfo = new ContentInfo(cms.contentInfo.getContentType(), cms.signedData);
         
         return cms;
     }
 
     /**
      * Replace the certificate and CRL information associated with this
      * CMSSignedData object with the new one passed in.
      * 
      * @param signedData the signed data object to be used as a base.
      * @param certsAndCrls the new certificates and CRLs to be used.
      * @return a new signed data object.
      * @exception CMSException if there is an error processing the CertStore
      */
     public static CMSSignedData replaceCertificatesAndCRLs(
         CMSSignedData   signedData,
         CertStore       certsAndCrls)
         throws CMSException
     {
         //
         // copy
         //
         CMSSignedData   cms = new CMSSignedData(signedData);
         
         //
         // replace the store
         //
         cms.certStore = certsAndCrls;
         
         //
         // replace the certs and crls in the SignedData object
         //
         ASN1Set             certs = null;
         ASN1Set             crls = null;
 
         try
         {
             ASN1Set set = CMSUtils.createBerSetFromList(CMSUtils.getCertificatesFromStore(certsAndCrls));
 
             if (set.size() != 0)
             {
                 certs = set;
             }
         }
         catch (CertStoreException e)
         {
             throw new CMSException("error getting certs from certStore", e);
         }
 
         try
         {
             ASN1Set set = CMSUtils.createBerSetFromList(CMSUtils.getCRLsFromStore(certsAndCrls));
 
             if (set.size() != 0)
             {
                 crls = set;
             }
         }
         catch (CertStoreException e)
         {
             throw new CMSException("error getting crls from certStore", e);
         }
         
         //
         // replace the CMS structure.
         //
         cms.signedData = new SignedData(signedData.signedData.getDigestAlgorithms(), 
                                    signedData.signedData.getEncapContentInfo(),
                                    certs,
                                    crls,
                                    signedData.signedData.getSignerInfos());
         
         //
         // replace the contentInfo with the new one
         //
         cms.contentInfo = new ContentInfo(cms.contentInfo.getContentType(), cms.signedData);
         
         return cms;
     }
 
     private static DERObject makeObj(
         byte[]  encoding)
         throws IOException
     {
         if (encoding == null)
         {
             return null;
         }
 
         ASN1InputStream         aIn = new ASN1InputStream(encoding);
 
         return aIn.readObject();
     }
 
     private static AlgorithmIdentifier makeAlgId(
         String  oid,
         byte[]  params)
         throws IOException
     {
         if (params != null)
         {
             return new AlgorithmIdentifier(
                             new DERObjectIdentifier(oid), makeObj(params));
         }
         else
         {
             return new AlgorithmIdentifier(
                             new DERObjectIdentifier(oid), new DERNull());
         }
     }
 }
