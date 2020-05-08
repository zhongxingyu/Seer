 package pl.org.olo.krbldap.apacheds.extras.extended.ads_impl.krbLdap;
 
 import java.nio.ByteBuffer;
 
 import org.apache.directory.shared.asn1.EncoderException;
 import org.apache.directory.shared.kerberos.messages.KerberosMessage;
 import org.apache.directory.shared.ldap.codec.api.ExtendedResponseDecorator;
 import org.apache.directory.shared.ldap.codec.api.LdapApiService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import pl.org.olo.krbldap.apacheds.extras.extended.KrbLdapResponse;
 
 /**
  *
  */
 public class KrbLdapResponseDecorator extends ExtendedResponseDecorator<KrbLdapResponse> implements KrbLdapResponse {
     // ------------------------------ FIELDS ------------------------------
 
     private static final int BUFFER_CAPACITY_MARGIN = 1024;
     private static final Logger LOG = LoggerFactory.getLogger(KrbLdapResponseDecorator.class);
 
     // --------------------------- CONSTRUCTORS ---------------------------
 
     /**
      * Makes a ExtendedResponse encodable.
      *
      * @param decoratedMessage the decorated ExtendedResponse
      */
     public KrbLdapResponseDecorator(LdapApiService codec, KrbLdapResponse decoratedMessage) {
         super(codec, decoratedMessage);
     }
 
     // ------------------------ INTERFACE METHODS ------------------------
 
 
     // --------------------- Interface KrbLdapResponse ---------------------
     public KerberosMessage getKerberosReply() {
         return getDecorated().getKerberosReply();
     }
 
     public void setKerberosReply(KerberosMessage kerberosReply) {
         getDecorated().setKerberosReply(kerberosReply);
     }
 
     // -------------------------- OTHER METHODS --------------------------
 
     @Override
     public byte[] getResponseValue() {
        final KerberosMessage kerberosReply = getKerberosReply();
        if (kerberosReply == null) {
             LOG.warn("Response value requested while no Kerberos reply has been set. Returning null.");
             return null;
         }
        final int kerberosReplyLength = kerberosReply.computeLength();
        LOG.debug("Length of Kerberos reply: {}", kerberosReplyLength);
        final ByteBuffer buffer = ByteBuffer.allocate(kerberosReplyLength);// + BUFFER_CAPACITY_MARGIN);
         try {
            kerberosReply.encode(buffer);
         } catch (EncoderException e) {
             LOG.error("Returning null instead of encoded KrbLDAP response because of exception [" +
                     e.getClass().getName() + "] when encoding, message: " + e.getMessage());
             return null;
         }
        LOG.debug("Encoded Kerberos message as extended response value.");
         return buffer.array();
     }
 
 
 }
