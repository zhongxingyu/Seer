 package beans.config;
 
 import java.io.File;
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.lang3.StringUtils;
 import org.reflections.ReflectionUtils;
 
 import utils.Utils;
 
 import com.google.common.base.Predicate;
 
 /**
  * User: guym
  * Date: 12/13/12
  * Time: 3:12 PM
  */
 public class ServerConfig {
 
 
 
     public PoolConfiguration pool = new PoolConfiguration();
 
 
     public ScriptEnvironmentConf environment = new ScriptEnvironmentConf();
 
 
     public BootstrapConfiguration bootstrap = new BootstrapConfiguration();
     
     public CloudBootstrapConfiguration cloudBootstrap = new CloudBootstrapConfiguration();
 
     public DefaultAdmin admin = new DefaultAdmin();
 
     @Config( ignoreNullValues = true )
     public long sessionTimeoutMillis = Utils.parseTimeToMillis( "15mn" );
 
     public static class PoolConfiguration{
         @Config( ignoreNullValues = true )
         public boolean coldInit = false ;
         @Config( ignoreNullValues = true )
         public int minNode = 2;
         @Config( ignoreNullValues = true )
         public int maxNodes = 5;
         @Config( ignoreNullValues = true )
         public long expirationTimeMillis = Utils.parseTimeToMillis("60mn");
         @Config( ignoreNullValues = true )
         public long maxExpirationTimeMillis = Utils.parseTimeToMillis("30mn");
         @Config( ignoreNullValues = true )
         public long minExpiryTimeMillis = Utils.parseTimeToMillis("10mn");
 
     }
 
     /**
      *
      *
      * this is a configuration for system environment variables while running scripts.
      * it will help us remove hard-coded strings like cloudify home location and enable us to have
      * more flexible environment in development and production.
      *
      *
      * We need a Java Object for holding the environment variables for 2 reasons
      * 1. We already have a good support for configuration and it would be a shame not to use it
      *      For example - this way we get a print of all the variables.
      * 2. We might need the value of the variable in Java code in a non script related matter.
      * 3. We can validate the values
      *
      *
      *
      * NOTE : I assume that all properties are STRINGs at the moment..
      *
      **/
 
 
     public static class ScriptEnvironmentConf {
 
         @Environment( key = "CLOUDIFY_HOME" )
         public String cloudifyHome = Utils.getFileByRelativePath("cloudify-folder").getAbsolutePath();
 
        public boolean useSystemEnvAsDefault = false; // this will also pass JAVA_OPTS, be careful with this! for windows development mainly.
 
         private Map<String,String> environment = null ;
 
         public Map getEnvironment() {
 
             try {
                 if (environment == null) {
                     environment = new HashMap<String, String>();
 
                     if ( useSystemEnvAsDefault ){
                         environment.putAll( System.getenv() );
                     }
 
                     Set<Field> allFields = ReflectionUtils.getAllFields(this.getClass(), new Predicate<Field>() {
                         @Override
                         public boolean apply(Field field) {
                             return field.getType() == String.class && Modifier.isPublic( field.getModifiers() );
                         }
                     });
                     for (Field field : allFields) {
                         String name = field.getName();
                         if (field.isAnnotationPresent(Environment.class)) {
                             Environment envAnnotation = field.getAnnotation(Environment.class);
                             name = StringUtils.isEmpty(envAnnotation.key()) ? name : envAnnotation.key();
                         }
                         String value = (String) field.get(this);
                         environment.put(name, value);
                     }
                 }
                 return environment;
             } catch (Exception e) {
                 throw new RuntimeException("unable to populate execution map", e);
             }
         }
     }
 
     public static class BootstrapConfiguration{
         public String serverNamePrefix="cloudify_pool_server";
         public String zoneName="az-1.region-a.geo-l";
         public String keyPair="cloudify";
         public String securityGroup="default";
         public String flavorId="102";
         public String imageId="1358";
         public SshConfiguration ssh = new SshConfiguration();
         public String apiKey="<HP cloud Password>";
         public String username="<tenant>:<user>";
         public String cloudProvider="hpcloud-compute";
         public File script;
         public String tags = null;
         public ApiCredentials api = new ApiCredentials();
     }
 
     public static class ApiCredentials{
         public String project;
         public String key;
         public String secretKey;
     }
     
     // cloud bootstrap configuration.
     public static class CloudBootstrapConfiguration {
     	public String cloudName = "hp";
         @Config(ignoreNullValues = true)
         public File remoteBootstrap = Utils.getFileByRelativePath("/bin/remote_bootstrap.sh");
         public String keyPairName = "cloudify";
         public String cloudifyHpUploadDirName = "upload";
         public String cloudPropertiesFileName = "hp-cloud.properties";
         public String zoneName = "az-2.region-a.geo-1";
         public String hardwareId = zoneName + "/102";
         public String linuxImageId = zoneName + "/221";
         public String securityGroup = "cloudifySecurityGroup";
         public String cloudProvider = "hpcloud-compute";
         public String cloudifyEscDirRelativePath = "tools/cli/plugins/esc/";
         public String existingManagementMachinePrefix = "cloudify-manager";
     }
 
     public static class SshConfiguration{
         public String user="root";
         @Config( ignoreNullValues = true )
         public int port=22;
         public File privateKey= Utils.getFileByRelativePath( "/bin/hpcloud.pem" );
     }
 
     public static class DefaultAdmin{
         public String username = "admin@cloudifysource.org";
         public String password = "admin1324";
     }
 }
