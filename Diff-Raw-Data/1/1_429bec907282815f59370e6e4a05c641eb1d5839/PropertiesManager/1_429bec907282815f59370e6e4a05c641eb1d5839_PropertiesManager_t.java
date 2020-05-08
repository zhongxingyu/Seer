 package de.thischwa.pmcms.configuration;
 
 import java.util.Properties;
 
 import org.springframework.stereotype.Component;
 
 import de.thischwa.pmcms.tool.PropertiesTool;
 
 @Component
 public class PropertiesManager {
 
 	private Properties props;
 	private Properties defaultSiteProps;
 	private Properties siteProps;
 	
 	public void setProperties(final Properties props) {
 		defaultSiteProps = PropertiesTool.getProperties(props, "pmcms.site");
		siteProps = new Properties(defaultSiteProps);
 		this.props = props;
 	}
 	
 	public void setSiteProperties(final Properties siteProps) {
 		this.siteProps = new Properties(defaultSiteProps);
 		this.siteProps.putAll(siteProps);
 	}
 	
 	public String getProperty(final String key) {
 		return props.getProperty(key);
 	}
 	
 	public String getSiteProperty(final String key) {
 		return siteProps.getProperty(key);
 	}
 	
 	public Properties getVelocityProperties() {
 		return PropertiesTool.getProperties(props, "velocity", true);
 	}
 	
 	Properties getAllProperties() {
 		return props;
 	}
 }
