 package fi.jpalomaki.ssh.jsch;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.nio.ByteBuffer;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import com.jcraft.jsch.ChannelExec;
 import com.jcraft.jsch.JSch;
 import com.jcraft.jsch.JSchException;
 import com.jcraft.jsch.Session;
 import fi.jpalomaki.ssh.SshClient;
 import fi.jpalomaki.ssh.SshClientException;
 import fi.jpalomaki.ssh.UserAtHost;
 import fi.jpalomaki.ssh.Result;
 import fi.jpalomaki.ssh.util.Assert;
 
 /**
  * Jsch-based {@link SshClient} implementation.
  * 
  * Only public key authentication is supported.
  * 
  * @author jpalomaki
  */
 public final class JschSshClient implements SshClient {
     
     private static final long CHANNEL_CLOSED_POLL_INTERVAL = 100L;
     
     private final String privateKey;
     private final byte[] passphrase;
     private final String knownHosts;
     private final Options options;
     
     /**
      * Constructs a new {@link JschSshClient} with a default known hosts
      * file (<code>~/.ssh/known_hosts</code>) and default {@link Options}.
      * 
      * Note that strict host key checking is used by default, which means
      * that a valid host key must be present in the SSH known hosts file.
      */
     public JschSshClient(String privateKey, String passphrase) {
         this(privateKey, passphrase, "~/.ssh/known_hosts", new Options());
     }
     
     /**
      * Constructs a new {@link JschSshClient} with the given parameters.
      * 
      * @param privateKey Path to private key file, not <code>null</code> or empty
      * @param passphrase Private key passphrase, may be <code>null</code> for empty passphrase
      * @param knownHosts Path to known hosts file, not <code>null</code> or empty
      * @param options Set of SSH client options, not <code>null</code>
      */
     public JschSshClient(String privateKey, String passphrase, String knownHosts, Options options) {
         Assert.hasText(privateKey, "Path to private key file must not be null");
         Assert.hasText(knownHosts, "Path to known hosts file must not be null");
         Assert.notNull(options, "Options must not be null");
         this.privateKey = privateKey;
         this.passphrase = passphrase != null ? passphrase.getBytes() : null;
         this.knownHosts = knownHosts;
         this.options = options;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Result executeCommand(String command, UserAtHost userAtHost) throws SshClientException {
         return executeCommand(command, ByteBuffer.wrap(new byte[0]), userAtHost);
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public Result executeCommand(String command, ByteBuffer stdin, UserAtHost userAtHost) throws SshClientException {
         Assert.hasText(command, "Command must not be null or empty");
         Assert.notNull(stdin, "Stdin must not be null (but may be empty)");
         Assert.notNull(userAtHost, "User at host must not be null");
         Session session = null;
         try {
             session = newSessionFor(userAtHost);
             return doExecuteCommand(command, stdin.array(), session);
         } catch (JSchException e) {
             throw new SshClientException("Failed to execute command '" + command + "' on " + userAtHost, e);
         } finally {
             if (session != null) {
                 session.disconnect();
             }
         }
     }
 
     private Session newSessionFor(UserAtHost userAtHost) throws JSchException {
         JSch jsch = new JSch();
         jsch.setKnownHosts(knownHosts);
         jsch.addIdentity(privateKey, passphrase);
         Session session = jsch.getSession(userAtHost.user, userAtHost.host, userAtHost.port);
         for (Map.Entry<String, String> entry : options.sshConfig.entrySet()) {
             session.setConfig(entry.getKey(), entry.getValue());
         }
         session.setConfig("PreferredAuthentications", "publickey");
         session.connect((int)options.connectTimeout);
         return session;
     }
     
     private Result doExecuteCommand(String command, byte[] bytesToStdin, Session session) throws JSchException, SshClientException {
         ByteArrayOutputStream stdout = new ByteArrayOutputStream();
         ByteArrayOutputStream stderr = new ByteArrayOutputStream();
         ByteArrayInputStream stdin = new ByteArrayInputStream(bytesToStdin);
         ChannelExec executionChannel = (ChannelExec)session.openChannel("exec");
         executionChannel.setCommand(command);
         if (stdin.available() > 0) {
             executionChannel.setInputStream(stdin);
         }
         executionChannel.setOutputStream(new BoundedOutputStream(options.maxStdoutBytes, stdout));
         executionChannel.setErrStream(new BoundedOutputStream(options.maxStderrBytes, stderr));
         executionChannel.connect();
         waitUntilChannelClosed(executionChannel);
         return new Result(executionChannel.getExitStatus(), stdout.toByteArray(), stderr.toByteArray());
     }
     
     private void waitUntilChannelClosed(ChannelExec executionChannel) {
         long waitTimeThusFar = 0L;
         long sessionTimeout = options.sessionTimeout;
         do {
             try {
                 Thread.sleep(CHANNEL_CLOSED_POLL_INTERVAL);
                 waitTimeThusFar += CHANNEL_CLOSED_POLL_INTERVAL;
                 if (sessionTimeout > 0L && waitTimeThusFar > sessionTimeout) {
                     break;
                 }
             } catch (InterruptedException e) {
                 // Ignore
             }
         } while (!executionChannel.isClosed());
         if (!executionChannel.isClosed()) {
             executionChannel.disconnect();
             throw new SshClientException("Session timeout (" + sessionTimeout + " ms) exceeded");
         }
     }
     
     /**
      * Container for SSH client options (immutable).
      * 
      * Note: Session timeout is a hard timeout to limit the duration of the
      * SSH session and it is enforced regardless of whether the session (or
      * connection) is idle or not.
      */
     public static class Options {
         
         private static final Map<String, Long> TIME_UNITS = timeUnits();
         private static final Map<String, Long> BYTE_UNITS = byteUnits();
         
         final long connectTimeout;
         final long sessionTimeout;
         final long maxStdoutBytes;
         final long maxStderrBytes;
         final Map<String, String> sshConfig;
 
         /**
          * Constructs default options (5s, 0s, 1M, 1M, StrictHostKeyChecking=yes).
          * 
         * @see #JschSshClient(String, String, String, String, String)
          */
         public Options() {
             this("5s", "0s", "1M", "1M", "StrictHostKeyChecking=yes");
         }
 
         /**
          * Constructs new {@link Options} with the given parameters.
          * 
          * Timeouts are specified in ms/s/m/h/d, e.g. 5s for five seconds or 2h for two hours.
          * 
          * Buffer sizes are specified in B/KiB/MiB/GiB, e.g. 128B for 128 bytes or 20M for 20 MiB.
          * 
          * SSH configuration options are specified as colon-separated key=value pairs, e.g. "CompressionLevel=3;ServerAliveInterval=5".
          * 
          * @param connectTimeout Connect timeout, 0s for no timeout
          * @param sessionTimeout Session timeout, 0s for no timeout
          * @param maxStdoutSize Maximum buffer size for stdout < 2G
          * @param maxStderrSize Maximum buffer size for stderr < 2G
          * @param sshConfig SSH config options, may be <code>null</code>
          */
         public Options(String connectTimeout, String sessionTimeout, String maxStdoutSize, String maxStderrSize, String sshConfig) {
             this(toMillis(connectTimeout), toMillis(sessionTimeout), toBytes(maxStdoutSize), toBytes(maxStderrSize), toMap(sshConfig));
         }
         
         private Options(long connectTimeout, long sessionTimeout, long maxStdoutBytes, long maxStderrBytes, Map<String, String> sshConfig) {
             Assert.isTrue(connectTimeout >= 0 && connectTimeout <= Integer.MAX_VALUE, "Connect timeout must be >= 0 and <= Integer.MAX_VALUE ms");
             Assert.isTrue(sessionTimeout >= 0, "Session timeout must be >= 0 ms");
             Assert.isTrue(maxStdoutBytes >= 0 && maxStdoutBytes <= Integer.MAX_VALUE, "Max stdout buffer size must be >= 0 and < 2G");
             Assert.isTrue(maxStderrBytes >= 0 && maxStderrBytes <= Integer.MAX_VALUE, "Max stderr buffer size must be >= 0 and < 2G");
             this.connectTimeout = connectTimeout;
             this.sessionTimeout = sessionTimeout;
             this.maxStdoutBytes = maxStdoutBytes;
             this.maxStderrBytes = maxStderrBytes;
             this.sshConfig = sshConfig != null ? 
                     Collections.unmodifiableMap(sshConfig) : Collections.<String, String>emptyMap();
         }
         
         private static long toMillis(String timeout) {
             Assert.hasText(timeout, "Timeout must not be null or empty");
             for (Map.Entry<String, Long> entry : TIME_UNITS.entrySet()) {
                 String unit = entry.getKey();
                 long coefficient = entry.getValue();
                 if (timeout.endsWith(unit)) {
                     return Long.parseLong(timeout.replace(unit, "")) * coefficient;
                 }
             }
             throw new IllegalArgumentException("Invalid timeout value: " + timeout + " (no unit specified?)");
         }
         
         private static long toBytes(String bufferSize) {
             Assert.hasText(bufferSize, "Buffer size must not be null or empty");
             for (Map.Entry<String, Long> entry : BYTE_UNITS.entrySet()) {
                 String unit = entry.getKey();
                 long coefficient = entry.getValue();
                 if (bufferSize.endsWith(unit)) {
                     return Long.parseLong(bufferSize.replace(unit, "")) * coefficient;
                 }
             }
             throw new IllegalArgumentException("Invalid buffer size: " + bufferSize);
         }
         
         private static Map<String, String> toMap(String sshConfig) {
             Map<String, String> map = new LinkedHashMap<String, String>();
             if (sshConfig != null && !sshConfig.trim().isEmpty()) {
                 String[] keyValuePairs = sshConfig.split(";");
                 for (String keyValuePair : keyValuePairs) {
                     String[] keyAndValue = keyValuePair.trim().split("=");
                     if (keyAndValue.length != 2) {
                         throw new IllegalArgumentException("Invalid SSH configuration string: " + sshConfig);
                     }
                     map.put(keyAndValue[0].trim(), keyAndValue[1].trim());
                 }
             }
             return map;
         }
         
         private static Map<String, Long> timeUnits() {
             Map<String, Long> map = new LinkedHashMap<String, Long>(5);
             map.put("ms", 1L);
             map.put("s", 1L * 1000);
             map.put("m", 1L * 1000 * 60);
             map.put("h", 1L * 1000 * 60 * 60);
             map.put("d", 1L * 1000 * 60 * 60 * 24);
             return Collections.unmodifiableMap(map);
         }
         
         private static Map<String, Long> byteUnits() {
             Map<String, Long> map = new LinkedHashMap<String, Long>(4);
             map.put("B", 1L);
             map.put("K", 1L * 1024);
             map.put("M", 1L * 1024 * 1024);
             map.put("G", 1L * 1024 * 1024 * 1024);
             return Collections.unmodifiableMap(map);
         }
     }
 }
