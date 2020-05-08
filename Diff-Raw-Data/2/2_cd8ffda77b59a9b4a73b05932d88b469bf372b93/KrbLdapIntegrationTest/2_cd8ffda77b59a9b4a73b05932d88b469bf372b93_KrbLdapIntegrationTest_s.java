 package pl.org.olo.krbldap.apacheds.test;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import org.apache.directory.server.annotations.CreateKdcServer;
 import org.apache.directory.server.annotations.CreateLdapServer;
 import org.apache.directory.server.annotations.CreateTransport;
 import org.apache.directory.server.annotations.SaslMechanism;
 import org.apache.directory.server.core.annotations.ApplyLdifFiles;
 import org.apache.directory.server.core.annotations.ContextEntry;
 import org.apache.directory.server.core.annotations.CreateDS;
 import org.apache.directory.server.core.annotations.CreateIndex;
 import org.apache.directory.server.core.annotations.CreatePartition;
 import org.apache.directory.server.core.integ.AbstractLdapTestUnit;
 import org.apache.directory.server.core.integ.FrameworkRunner;
 import org.apache.directory.server.core.kerberos.KeyDerivationInterceptor;
 import org.apache.directory.server.kerberos.kdc.KdcServer;
 import org.apache.directory.server.kerberos.protocol.KerberosProtocolHandler;
 import org.apache.directory.server.kerberos.shared.store.DirectoryPrincipalStore;
 import org.apache.directory.server.kerberos.shared.store.PrincipalStore;
 import org.apache.directory.server.ldap.ExtendedOperationHandler;
 import org.apache.directory.server.ldap.handlers.bind.cramMD5.CramMd5MechanismHandler;
 import org.apache.directory.server.ldap.handlers.bind.digestMD5.DigestMd5MechanismHandler;
 import org.apache.directory.server.ldap.handlers.bind.gssapi.GssapiMechanismHandler;
 import org.apache.directory.server.ldap.handlers.bind.ntlm.NtlmMechanismHandler;
 import org.apache.directory.server.ldap.handlers.bind.plain.PlainMechanismHandler;
 import org.apache.directory.shared.ldap.codec.api.LdapApiService;
 import org.apache.directory.shared.ldap.codec.api.LdapApiServiceFactory;
 import org.apache.directory.shared.ldap.model.constants.SupportedSaslMechanisms;
 import org.apache.directory.shared.ldap.model.exception.LdapInvalidDnException;
 import org.apache.directory.shared.ldap.model.message.ExtendedRequest;
 import org.apache.directory.shared.ldap.model.message.ExtendedResponse;
 import org.apache.directory.shared.ldap.model.name.Dn;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import pl.org.olo.krbldap.apacheds.extras.extended.KrbLdapRequest;
 import pl.org.olo.krbldap.apacheds.extras.extended.ads_impl.krbLdap.KrbLdapFactory;
 import pl.org.olo.krbldap.apacheds.handlers.extended.KrbLdapAuthServiceHandler;
 
 /**
  * This test suite tests PAM-krbldap integration with ApacheDS-krbldap.
  */
 @RunWith(FrameworkRunner.class)
 @CreateDS(name = "SaslGssapiBindITest-class",
         allowAnonAccess = true,
         partitions = {@CreatePartition(
                 name = "example",
                 suffix = "dc=example,dc=com",
                 contextEntry = @ContextEntry(
                         entryLdif = "dn: dc=example,dc=com\n" + "dc: example\n" + "objectClass: top\n" +
                                 "objectClass: domain\n\n"),
                 indexes = {@CreateIndex(attribute = "objectClass"), @CreateIndex(attribute = "dc"),
                         @CreateIndex(attribute = "ou"), @CreateIndex(attribute = "uid")})},
         additionalInterceptors = {KeyDerivationInterceptor.class})
 @CreateLdapServer(
         transports = {@CreateTransport(protocol = "LDAP", port = 1389)},
         allowAnonymousAccess = true,
         extendedOpHandlers = KrbLdapAuthServiceHandler.class,
         saslHost = "localhost",
         saslPrincipal = "ldap/localhost@EXAMPLE.COM",
         saslMechanisms = {@SaslMechanism(name = SupportedSaslMechanisms.PLAIN, implClass = PlainMechanismHandler.class),
                 @SaslMechanism(name = SupportedSaslMechanisms.CRAM_MD5, implClass = CramMd5MechanismHandler.class),
                 @SaslMechanism(name = SupportedSaslMechanisms.DIGEST_MD5, implClass = DigestMd5MechanismHandler.class),
                 @SaslMechanism(name = SupportedSaslMechanisms.GSSAPI, implClass = GssapiMechanismHandler.class),
                 @SaslMechanism(name = SupportedSaslMechanisms.NTLM, implClass = NtlmMechanismHandler.class),
                 @SaslMechanism(name = SupportedSaslMechanisms.GSS_SPNEGO, implClass = NtlmMechanismHandler.class)})
 @CreateKdcServer(
         transports = {@CreateTransport(protocol = "UDP", port = 6088), @CreateTransport(protocol = "TCP", port = 6088)})
 @ApplyLdifFiles("test.ldif")
 public class KrbLdapIntegrationTest extends AbstractLdapTestUnit {
     /**
      * Pathname of the client test shell script
      */
     private static final String CLIENT_TEST_SCRIPT =
             "/var/soft/PAM/krb5-github/src/pam_krb5/tests/run-tests-krbldap-direct.sh";
     /**
      * KRB5 conf file location relative to classpath
      */
     private static final String KRB5_CONF_RESOURCE_LOCATION = "krb5.conf";
 
     /**
      * Creates a new instance of SaslGssapiBindTest and sets JAAS system properties.
      */
     public KrbLdapIntegrationTest() {
         String krbConfPath = getClass().getClassLoader().getResource(KRB5_CONF_RESOURCE_LOCATION).getFile();
         System.setProperty("java.security.krb5.conf", krbConfPath);
         System.setProperty("sun.security.krb5.debug", "true");
 
     }
 
 
     @Test
     public void testShouldPerformSuccessfulAuthentication() throws Exception {
         registerKrbLdapExtendedRequest();
         configureKrbLdapAuthServiceHandler();
 
 
         // The server has been set up. Run the integration test client shell script:
         final Process process = Runtime.getRuntime().exec(CLIENT_TEST_SCRIPT);
         final InputStream errorStream = process.getErrorStream();
         final InputStream inputStream = process.getInputStream();
         final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
         final BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
         System.out.println(
                 "Testing started. Check your system's syslog (facility AUTH) for any messages from the PAM module.");
         while (true) {
             while (reader.ready()) {
                 System.out.println(reader.readLine());
             }
             while (errorReader.ready()) {
                 System.out.println(errorReader.readLine());
             }
             Thread.sleep(50);
         }
 
         /*
         final int retValue = process.waitFor();
         System.out.println("Return code: [" + retValue + "]");
         System.out.println("STDOUT:");
         System.out.println(IOUtils.toString(inputStream));
         System.out.println("STDOUT END.");
         System.out.println("STDERR:");
         System.out.println(IOUtils.toString(errorStream));
         System.out.println("STDERR END.");
         if (retValue != 0) {
             throw new RuntimeException("error code [" + retValue + "] returned from process.");
         }
         */
     }
 
     private void configureKrbLdapAuthServiceHandler() throws LdapInvalidDnException {
         // Configure the KrbLdapAuthServiceHandler by injecting it with a new KerberosProtocolHandler
         // based on the present KdcServer:
         final ExtendedOperationHandler<ExtendedRequest<ExtendedResponse>, ExtendedResponse> extendedOperationHandler =
                 getLdapServer().getExtendedOperationHandler(KrbLdapRequest.EXTENSION_OID);
         Object tmp = extendedOperationHandler;
         if (!(tmp instanceof KrbLdapAuthServiceHandler)) {
             throw new IllegalStateException(
                     "extendedOperationHandler not instanceof " + KrbLdapAuthServiceHandler.class.getName());
         }
         final KrbLdapAuthServiceHandler krbLdapAuthServiceHandler = (KrbLdapAuthServiceHandler) tmp;
         final KdcServer kdcServer = getKdcServer();
         System.out.println("kdcServer: " + kdcServer);
         PrincipalStore principalStore =
                 new DirectoryPrincipalStore(kdcServer.getDirectoryService(), new Dn(kdcServer.getSearchBaseDn()));
         final KerberosProtocolHandler kerberosProtocolHandler = new KerberosProtocolHandler(kdcServer, principalStore);
         krbLdapAuthServiceHandler.setKerberosProtocolHandler(kerberosProtocolHandler);
     }
 
     private void registerKrbLdapExtendedRequest() {
         final LdapApiService ldapApiService = LdapApiServiceFactory.getSingleton();
         final KrbLdapFactory krbLdapFactory = new KrbLdapFactory(ldapApiService);
         ldapApiService.registerExtendedRequest(krbLdapFactory);
     }
 
 }
