 package org.apache.wss4j.common.kerberos;
 
 import org.apache.directory.server.kerberos.shared.crypto.encryption.CipherTextHandler;
 import org.apache.directory.server.kerberos.shared.crypto.encryption.EncryptionType;
 import org.apache.directory.server.kerberos.shared.crypto.encryption.KeyUsage;
 import org.apache.directory.server.kerberos.shared.io.decoder.ApplicationRequestDecoder;
 import org.apache.directory.server.kerberos.shared.messages.ApplicationRequest;
 import org.apache.directory.server.kerberos.shared.messages.components.EncTicketPart;
 import org.apache.directory.server.kerberos.shared.messages.value.EncryptionKey;
 
 import javax.security.auth.Subject;
 import javax.security.auth.kerberos.KerberosKey;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Iterator;
 import java.util.Set;
 
 public class KerberosTokenDecoderImpl implements KerberosTokenDecoder {
    
    private static org.slf4j.Logger log =
        org.slf4j.LoggerFactory.getLogger(KerberosTokenDecoderImpl.class);
 
     private static final String KERBEROS_OID = "1.2.840.113554.1.2.2";
 
     private byte[] serviceTicket;
     private Subject subject;
 
     private boolean decoded = false;
     private EncTicketPart encTicketPart;
 
     /**
      * Clear all internal information
      */
     public void clear() {
         serviceTicket = null;
         subject = null;
         decoded = false;
         encTicketPart = null;
     }
 
     /**
      * Set the AP-REQ Kerberos Token
      *
      * @param token the AP-REQ Kerberos Token
      */
     public void setToken(byte[] token) {
         serviceTicket = token;
     }
 
     /**
      * Set the Subject
      *
      * @param subject the Subject
      */
     public void setSubject(Subject subject) {
         this.subject = subject;
     }
 
     /**
      * Get the session key from the token
      *
      * @return the session key from the token
      */
     public byte[] getSessionKey() {
         if (!decoded) {
             decodeServiceTicket();
         }
        if (encTicketPart != null && encTicketPart.getSessionKey() != null) {
            return encTicketPart.getSessionKey().getKeyValue();
        }
        return null;
     }
 
     /**
      * Get the client principal name from the decoded service ticket.
      *
      * @return the client principal name
      */
     public String getClientPrincipalName() {
         if (!decoded) {
             decodeServiceTicket();
         }
         return encTicketPart.getClientPrincipal().toString();
     }
 
     // Decode the service ticket.
     private synchronized void decodeServiceTicket() {
         try {
             parseServiceTicket(serviceTicket);
             decoded = true;
         } catch (Exception e) {
            log.debug("Error retrieving a service ticket", e);
         }
     }
 
     // Parses the service ticket (GSS AP-REQ token)
     private void parseServiceTicket(byte[] ticket) throws Exception {
 
         // I didn't find a better way how to parse this Kerberos Message...
 
         org.bouncycastle.asn1.ASN1InputStream asn1InputStream =
                 new org.bouncycastle.asn1.ASN1InputStream(new ByteArrayInputStream(ticket));
         org.bouncycastle.asn1.DERApplicationSpecific derToken =
                 (org.bouncycastle.asn1.DERApplicationSpecific) asn1InputStream.readObject();
         if (derToken == null || !derToken.isConstructed()) {
             asn1InputStream.close();
             throw new IllegalArgumentException("invalid kerberos token");
         }
         asn1InputStream.close();
 
         asn1InputStream = new org.bouncycastle.asn1.ASN1InputStream(new ByteArrayInputStream(derToken.getContents()));
         org.bouncycastle.asn1.DERObjectIdentifier kerberosOid =
                 (org.bouncycastle.asn1.DERObjectIdentifier) asn1InputStream.readObject();
         if (!kerberosOid.getId().equals(KERBEROS_OID)) {
             asn1InputStream.close();
             throw new IllegalArgumentException("invalid kerberos token");
         }
 
         int readLowByte = asn1InputStream.read() & 0xff;
         int readHighByte = asn1InputStream.read() & 0xff;
         int read = (readHighByte << 8) + readLowByte;
         if (read != 0x01) {
             throw new IllegalArgumentException("invalid kerberos token");
         }
 
         ApplicationRequestDecoder applicationRequestDecoder = new ApplicationRequestDecoder();
         ApplicationRequest applicationRequest = applicationRequestDecoder.decode(toByteArray(asn1InputStream));
 
         final int encryptionType = applicationRequest.getTicket().getEncPart().getEType().getOrdinal();
         KerberosKey kerberosKey = getKrbKey(subject, encryptionType);
 
         EncryptionKey encryptionKey =
                 new EncryptionKey(EncryptionType.getTypeByOrdinal(encryptionType), kerberosKey.getEncoded());
 
         CipherTextHandler cipherTextHandler = new CipherTextHandler();
         this.encTicketPart = (EncTicketPart) cipherTextHandler.unseal(
                 EncTicketPart.class, encryptionKey, applicationRequest.getTicket().getEncPart(), KeyUsage.NUMBER2);
     }
 
     private KerberosKey getKrbKey(Subject sub, int keyType) throws Exception {
         Set<Object> creds = sub.getPrivateCredentials(Object.class);
         for (Iterator<Object> i = creds.iterator(); i.hasNext(); ) {
             Object cred = i.next();
             if (cred instanceof KerberosKey) {
                 KerberosKey key = (KerberosKey) cred;
                 if (key.getKeyType() == keyType) {
                     return (KerberosKey) cred;
                 }
             }
         }
         return null;
     }
 
     private static byte[] toByteArray(InputStream inputStream) throws IOException {
         ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
         int read;
         byte[] buf = new byte[1024];
         while ((read = inputStream.read(buf)) != -1) {
             byteArrayOutputStream.write(buf, 0, read);
         }
         return byteArrayOutputStream.toByteArray();
     }
 }
