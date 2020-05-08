 package org.jboss.chirpr.roo.web;
 
import java.util.Set;

import org.jboss.chirpr.roo.domain.Profile;
import org.springframework.core.convert.converter.Converter;
 import org.springframework.format.FormatterRegistry;
 import org.springframework.format.support.FormattingConversionServiceFactoryBean;
 import org.springframework.roo.addon.web.mvc.controller.RooConversionService;
         
 /**
  * A central place to register application Converters and Formatters. 
  */
 @RooConversionService
 public class ApplicationConversionServiceFactoryBean extends FormattingConversionServiceFactoryBean {
 
 	@Override
 	protected void installFormatters(FormatterRegistry registry) {
 		super.installFormatters(registry);
	    registry.addConverter(getProfileSetConverter());
 	}
 	
	private Converter<Set<Profile>, String> getProfileSetConverter() {
        return new Converter<Set<Profile>, String>() {
            public String convert(Set<Profile> profiles) {
            	StringBuilder builder = new StringBuilder();
                for (Profile profile:profiles) {
                	if (builder.length()!=0) {
                		builder.append(",");
                	}
                	builder.append(profile.getUsername());	                	
                }
                return builder.toString();
            }
        };
    }
	
	private Converter<Profile, String> getProfileConverter() {
        return new Converter<Profile, String>() {
            public String convert(Profile profile) {
            	return profile.getUsername();
            }
        };
    }

	
 }
