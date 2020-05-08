 #set( $symbol_pound = '#' )
 #set( $symbol_dollar = '$' )
 #set( $symbol_escape = '\' )
 package ${package}.client.local;
 
 import org.jboss.errai.bus.client.framework.Configuration;
 
 /**
  *
  */
 public class Config implements Configuration {
     @Override
     public String getRemoteLocation() {
        return "${erraiServerUrl}/${artifactId}/";
     }
 }
