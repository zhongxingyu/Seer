 package amp.eventing;
 
 
 import java.util.Map.Entry;
 import java.util.UUID;
 
 import cmf.bus.Envelope;
 import cmf.bus.EnvelopeHeaderConstants;
 import org.apache.commons.codec.binary.Base64;
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
 
 
 public class EnvelopeHelper {
 
     private Envelope env;
 
     public EnvelopeHelper() {
         env = new Envelope();
     }
 
     public EnvelopeHelper(Envelope envelope) {
         env = envelope;
     }
 
     public String flatten() {
         return this.flatten(",");
     }
 
     public String flatten(String separator) {
         StringBuilder sb = new StringBuilder();
 
         sb.append("[");
 
         for (Entry<String, String> kvp : env.getHeaders().entrySet()) {
             sb.append(String.format("%s{%s:%s}", separator, kvp.getKey(), kvp.getValue()));
         }
 
         if (sb.length() > 1) {
             sb.delete(1, 1 + separator.length());
         }
 
         sb.append("]");
 
         return sb.toString();
     }
 
     public UUID getCorrelationId() {
         UUID cid = null;
 
         String cidString = env.getHeader(EnvelopeHeaderConstants.MESSAGE_CORRELATION_ID);
         if (cidString != null) {
             cid = UUID.fromString(cidString);
         }
 
         return cid;
     }
 
     public DateTime getCreationTime() {
         String createTicks = null;
 
         if (env.getHeaders().containsKey(EnvelopeHeaderConstants.ENVELOPE_CREATION_TIME)) {
             createTicks = env.getHeaders().get(EnvelopeHeaderConstants.ENVELOPE_CREATION_TIME);
         }
 
        return new DateTime(Long.parseLong(createTicks));
     }
 
     public byte[] getDigitalSignature() {
         String base64String = env.getHeader(EnvelopeHeaderConstants.MESSAGE_SENDER_SIGNATURE);
         if (null == base64String) {
             return null;
         }
 
         return Base64.decodeBase64(base64String);
     }
 
     public Envelope getEnvelope() {
         return env;
     }
 
     public String getHeader(String key) {
         return env.getHeader(key);
     }
 
     public UUID getMessageId() {
         UUID id = null;
 
         String idString = env.getHeader(EnvelopeHeaderConstants.MESSAGE_ID);
 
         if (idString != null) {
             id = UUID.fromString(idString);
         }
 
         return id;
     }
 
     public String getMessagePattern() {
         return env.getHeader(EnvelopeHeaderConstants.MESSAGE_PATTERN);
     }
 
     public String getMessageTopic() {
         return env.getHeader(EnvelopeHeaderConstants.MESSAGE_TOPIC);
     }
 
     public String getMessageType() {
         return env.getHeader(EnvelopeHeaderConstants.MESSAGE_TYPE);
     }
 
     public byte[] getPayload() {
         return env.getPayload();
     }
 
     public DateTime getReceiptTime() {
         String receiptTicks = null;
 
         if (env.getHeaders().containsKey(EnvelopeHeaderConstants.ENVELOPE_RECEIPT_TIME)) {
             receiptTicks = env.getHeaders().get(EnvelopeHeaderConstants.ENVELOPE_RECEIPT_TIME);
         }
 
         return new DateTime(Long.parseLong(receiptTicks));
     }
 
     public Duration getRpcTimeout() {
         String timeString = env.getHeader(EnvelopeHeaderConstants.MESSAGE_PATTERN_RPC_TIMEOUT);
         if (null == timeString) {
             return Duration.ZERO;
         }
 
         long totalMilliseconds = Long.parseLong(timeString);
         return new Duration(totalMilliseconds);
     }
 
     public String getSenderIdentity() {
         return env.getHeader(EnvelopeHeaderConstants.MESSAGE_SENDER_IDENTITY);
     }
 
     public boolean IsPubSub() {
         return EnvelopeHeaderConstants.MESSAGE_PATTERN_PUBSUB.equals(env
                         .getHeader(EnvelopeHeaderConstants.MESSAGE_PATTERN));
     }
 
     public boolean IsRequest() {
         // we assume that the envelope is holding a request if it is marked
         // as an rpc message that has no correlation id set.
         UUID correlationId = new EnvelopeHelper(env).getCorrelationId();
 
         return new EnvelopeHelper(env).IsRpc() && null == correlationId;
     }
 
     public boolean IsRpc() {
         return EnvelopeHeaderConstants.MESSAGE_PATTERN_RPC.equals(env
                         .getHeader(EnvelopeHeaderConstants.MESSAGE_PATTERN));
     }
 
     public EnvelopeHelper setCorrelationId(UUID cid) {
         env.setHeader(EnvelopeHeaderConstants.MESSAGE_CORRELATION_ID, cid.toString());
         return this;
     }
 
     public EnvelopeHelper setCreationTime(DateTime date) {
         env.getHeaders().put(EnvelopeHeaderConstants.ENVELOPE_CREATION_TIME, Long.toString(date.getMillis()));
         return this;
     }
 
     public EnvelopeHelper setDigitalSignature(byte[] signature) {
         env.setHeader(EnvelopeHeaderConstants.MESSAGE_SENDER_SIGNATURE, Base64.encodeBase64String(signature));
         return this;
     }
 
     public EnvelopeHelper setHeader(String key, String value) {
         env.setHeader(key, value);
         return this;
     }
 
     public EnvelopeHelper setMessageId(UUID id) {
         env.setHeader(EnvelopeHeaderConstants.MESSAGE_ID, id.toString());
         return this;
     }
 
     public EnvelopeHelper setMessagePattern(String pattern) {
         env.setHeader(EnvelopeHeaderConstants.MESSAGE_PATTERN, pattern);
         return this;
     }
 
     public EnvelopeHelper setMessageTopic(String topic) {
         env.setHeader(EnvelopeHeaderConstants.MESSAGE_TOPIC, topic);
         return this;
     }
 
     public EnvelopeHelper setMessageType(String messageType) {
         env.setHeader(EnvelopeHeaderConstants.MESSAGE_TYPE, messageType);
         return this;
     }
 
     public EnvelopeHelper setPayload(byte[] payload) {
         env.setPayload(payload);
         return this;
     }
 
     public EnvelopeHelper setReceiptTime(DateTime date) {
         env.getHeaders().put(EnvelopeHeaderConstants.ENVELOPE_RECEIPT_TIME, Long.toString(date.getMillis()));
         return this;
     }
 
     public EnvelopeHelper setRpcTimeout(Duration timeout) {
         env.setHeader(EnvelopeHeaderConstants.MESSAGE_PATTERN_RPC_TIMEOUT, Long.toString(timeout.getMillis()));
         return this;
     }
 
     public EnvelopeHelper setSenderIdentity(String distinguishedName) {
         env.setHeader(EnvelopeHeaderConstants.MESSAGE_SENDER_IDENTITY, distinguishedName);
         return this;
     }
 
 }
