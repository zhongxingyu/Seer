 package org.ccci.deployment;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.io.Closeables;
 import org.ccci.deployment.basic.AppserverType;
 import org.ccci.deployment.basic.BasicApplication;
 import org.ccci.deployment.basic.BasicEnvironment;
 import org.ccci.deployment.basic.BasicWebappDeployment;
 import org.ccci.deployment.basic.OS;
 import org.ccci.deployment.basic.PingConfig;
 import org.ccci.deployment.basic.StaticConfig;
 import org.ccci.util.mail.EmailAddress;
 import org.hibernate.validator.constraints.NotBlank;
 import org.hibernate.validator.constraints.NotEmpty;
 import org.yaml.snakeyaml.Yaml;
 import org.yaml.snakeyaml.error.YAMLException;
 import org.yaml.snakeyaml.introspector.BeanAccess;
 
 import javax.validation.ConstraintViolation;
 import javax.validation.ConstraintViolationException;
 import javax.validation.Valid;
 import javax.validation.Validation;
 import javax.validation.Validator;
 import javax.validation.constraints.Max;
 import javax.validation.constraints.Min;
 import javax.validation.constraints.NotNull;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * @author Matt Drees
  */
 public class BasicApplicationBuilder {
 
     Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
 
     /**
      * Builds a {@link BasicApplication} from a given yml configuration file
      *
      * @throws  IllegalArgumentException if the configuration file is invalid
      */
     public BasicApplication buildFrom(File applicationConfiguration) {
         Preconditions.checkNotNull(applicationConfiguration);
         Preconditions.checkArgument(applicationConfiguration.exists(), applicationConfiguration + " does not exist");
         Preconditions.checkArgument(applicationConfiguration.isFile(), applicationConfiguration + " is not a file");
 
         FileInputStream inputStream;
         try {
             inputStream = new FileInputStream(applicationConfiguration);
         } catch (FileNotFoundException e) {
             throw new AssertionError(applicationConfiguration + " was checked to exist");
         }
 
         return parseValidateAndBuild(inputStream, applicationConfiguration.toString());
     }
 
     /**
      * Similar to {@link #buildFrom(File)}, except that a generic URL can be used
      */
     public BasicApplication buildFrom(URL applicationConfiguration) {
         Preconditions.checkNotNull(applicationConfiguration);
 
         InputStream inputStream;
         try {
             inputStream = applicationConfiguration.openStream();
         } catch (IOException e) {
             throw new RuntimeException("cannot open " + applicationConfiguration, e);
         }
 
         return parseValidateAndBuild(inputStream, applicationConfiguration.toString());
     }
 
 
     private BasicApplication parseValidateAndBuild(InputStream applicationConfigurationStream, String location) {
         BasicApplicationConfig config;
         try {
             config = parse(applicationConfigurationStream, location);
         } finally {
             Closeables.closeQuietly(applicationConfigurationStream);
         }
 
         validate(config, location);
         return buildFromConfig(config);
     }
 
     private BasicApplicationConfig parse(InputStream applicationConfigurationStream, String location) {
         Yaml yaml = new Yaml();
         yaml.setBeanAccess(BeanAccess.FIELD);
         try {
             return yaml.loadAs(applicationConfigurationStream, BasicApplicationConfig.class);
         }
         catch (YAMLException e)
         {
             throw new IllegalArgumentException("unable to parse config from " + location, e);
         }
 
     }
 
     private void validate(BasicApplicationConfig config, String location) {
         Set<ConstraintViolation<BasicApplicationConfig>> constraintViolations = validator.validate(config);
        if (constraintViolations.isEmpty())
            return;
 
         ConstraintViolationException cvException = new ConstraintViolationException(removeConstraintViolationTypeParameter(constraintViolations));
 
         throw new IllegalArgumentException(composeViolationMessage(constraintViolations, location), cvException);
     }
 
     /**
      * The java compiler makes it difficult to convert a `Set<Foo<Bar>>` to a `Set<Foo<?>>`,
      * even if you know it is safe.
      *
      * Oddly, to do this, you have to cast to `Set<?>` first and then cast to `Set<Foo<?>>`.
      * Not sure if there's a better workaround.
      *
      * ConstraintViolation methods never use the type parameter as a method argument, so this is safe.
      */
     @SuppressWarnings("unchecked")
     private <T> Set<ConstraintViolation<?>> removeConstraintViolationTypeParameter(Set<ConstraintViolation<T>> constraintViolations) {
         return (Set<ConstraintViolation<?>>) (Set<?>) constraintViolations;
     }
 
     private String composeViolationMessage(
         Set<ConstraintViolation<BasicApplicationConfig>> constraintViolations,
         String location) {
         StringBuilder builder = new StringBuilder("invalid configuration: (");
         builder.append(location);
         builder.append(")");
         for (ConstraintViolation<BasicApplicationConfig> constraintViolation : constraintViolations) {
             builder.append("\n");
             builder.append("  * ");
             builder.append(messageFrom(constraintViolation));
             if (constraintViolation.getInvalidValue() != null)
             {
                 builder.append(" (value is ");
                 builder.append(constraintViolation.getInvalidValue());
                 builder.append(")");
             }
         }
         return builder.toString();
     }
 
     private String messageFrom(ConstraintViolation<BasicApplicationConfig> constraintViolation) {
         return constraintViolation.getPropertyPath() + ": " + constraintViolation.getMessage();
     }
 
     private BasicApplication buildFromConfig(BasicApplicationConfig config) {
         StaticConfig staticConfig = new StaticConfig();
         staticConfig.setEnvironments(buildEnvironments(config));
         staticConfig.setPingConfig(buildPingConfig(config));
         staticConfig.setApplicationName(config.applicationName);
         staticConfig.setWaitTimeBetweenNodes(config.waitTimeBetweenNodes);
         staticConfig.setDeployment(buildDeployment(config));
         staticConfig.setOs(buildOs(config));
         staticConfig.setType(buildType(config));
 
         return new BasicApplication(staticConfig);
     }
 
     private Map<String, BasicEnvironment> buildEnvironments(BasicApplicationConfig config) {
         ImmutableMap.Builder<String, BasicEnvironment> environments = ImmutableMap.builder();
         for(Map.Entry<String, Environment> entry : config.environments.entrySet())
         {
             String name = entry.getKey();
             Environment configEnvironment = entry.getValue();
             List<Node> nodes = buidNodes(configEnvironment.nodes);
             Set<EmailAddress> deploymentSubscribers = buildDeploymentSubscribers(configEnvironment.deploymentSubscribers);
 
             environments.put(name, new BasicEnvironment(
                 configEnvironment.serviceName,
                 configEnvironment.port,
                 configEnvironment.appserverBasePath,
                 nodes,
                 deploymentSubscribers
             ));
         }
         return environments.build();
     }
 
     private Set<EmailAddress> buildDeploymentSubscribers(List<String> deploymentSubscribers) {
         ImmutableSet.Builder<EmailAddress> subscribers = ImmutableSet.builder();
         for (String deploymentSubscriber : deploymentSubscribers) {
             subscribers.add(EmailAddress.valueOf(deploymentSubscriber));
         }
         return subscribers.build();
     }
 
     private List<Node> buidNodes(List<NodeConfig> configNodes) {
         ImmutableList.Builder<Node> builder = ImmutableList.builder();
         for (NodeConfig configNode : configNodes) {
             builder.add(new Node(
                 configNode.name,
                 configNode.hostname
             ));
         }
         return builder.build();
     }
 
     private PingConfig buildPingConfig(BasicApplicationConfig config) {
         Ping ping = config.deploymentVerification.ping;
         Preconditions.checkArgument(ping != null, "at the moment, 'ping' is the only kind of deployment verification");
 
         return new PingConfig(
             ping.path,
             ping.expectedContentRegex,
             ping.secondsBeforeTimeout);
     }
 
 
     private OS buildOs(BasicApplicationConfig config) {
         return OS.valueOf(config.os.toUpperCase());
     }
 
     private AppserverType buildType(BasicApplicationConfig config) {
         return AppserverType.valueOf(config.type.toUpperCase());
     }
 
     private BasicWebappDeployment buildDeployment(BasicApplicationConfig config) {
         BasicWebappDeployment deployment = new BasicWebappDeployment();
         deployment.setName(config.deployment.name);
         deployment.setDeployedWarName(config.deployment.name);
 
         if (config.deployment.explodedPackaging != null)
         {
             deployment.setPackaging(WebappDeployment.Packaging.EXPLODED);
             deployment.setDeploymentFileDescription(buildDeploymentFileDescription(config));
         }
         else
         {
             deployment.setPackaging(WebappDeployment.Packaging.ARCHIVE);
         }
         return deployment;
     }
 
     private DeploymentFileDescription buildDeploymentFileDescription(BasicApplicationConfig config) {
         DeploymentFileDescription description = new DeploymentFileDescription();
         description.getDeploymentSpecificPaths().addAll(config.deployment.explodedPackaging.deploymentSpecificPaths);
         description.getIgnoredPaths().addAll(config.deployment.explodedPackaging.ignoredPaths);
         description.setLogPath(config.deployment.explodedPackaging.logPath);
         return description;
     }
 
 
     public static class BasicApplicationConfig {
         @NotNull
         String type;
 
         @NotNull
         String os;
 
         @NotNull
         String applicationName;
 
         @NotNull
         @Valid
         DeploymentConfig deployment;
 
         @Min(0)
         @Max(value = 60 * 60, message = "we don't think you really want to wait more than an hour")
         int waitTimeBetweenNodes = 10;
 
         @NotNull
         @Valid
         DeploymentVerification deploymentVerification;
 
         @NotEmpty
         @Valid
         Map<String, Environment> environments;
     }
 
     private static class DeploymentConfig {
         @NotNull
         String name;
 
         @Valid
         ExplodedPackaging explodedPackaging;
     }
 
     private static class ExplodedPackaging {
         List<String> deploymentSpecificPaths = Collections.emptyList();
         List<String> ignoredPaths = Collections.emptyList();
         String logPath;
     }
 
     private static class DeploymentVerification {
         @NotNull
         @Valid
         Ping ping;
     }
 
     private static class Ping {
         @NotBlank
         String path;
 
         @NotBlank
         String expectedContentRegex;
 
         @Min(0)
         @Max(value = 60 * 60, message = "we don't think you really want to wait more than an hour")
         int secondsBeforeTimeout;
     }
 
     private static class Environment {
 
         @NotBlank
         String appserverBasePath;
 
         @NotBlank
         String serviceName;
 
         @NotEmpty
         @Valid
         List<NodeConfig> nodes;
 
         List<String> deploymentSubscribers = Collections.emptyList();
 
         @Min(1) @Max(65535)
         int port;
     }
 
     private static class NodeConfig {
         @NotBlank
         String name;
 
         @NotBlank
         String hostname;
     }
 
 }
