 package org.grails.plugin.gcomet;
 
 import org.codehaus.groovy.grails.commons.ArtefactHandlerAdapter;
 import org.codehaus.groovy.grails.commons.GrailsClass;
import org.grails.plugin.gcomet.DefaultGCometComponent;
 
 public class GCometComponentArtefactHandler extends ArtefactHandlerAdapter{
 	public static final String TYPE = "GCometComponent";
 	public static final String SUFFIX = "GCometComponent";
 	
 	public GCometComponentArtefactHandler() {
 		super(TYPE, GrailsClass.class, DefaultGCometComponent.class, SUFFIX);
 	}
 }
