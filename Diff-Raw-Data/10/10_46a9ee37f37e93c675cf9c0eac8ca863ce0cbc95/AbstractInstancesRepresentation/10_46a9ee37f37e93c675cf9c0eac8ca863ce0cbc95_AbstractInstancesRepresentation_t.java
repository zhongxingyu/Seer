 package com.kdcloud.lib.rest.ext;
 
 import java.io.IOException;
 
 import org.restlet.data.MediaType;
 import org.restlet.representation.CharacterRepresentation;
 import org.restlet.representation.Representation;
 
 import weka.core.Instances;
 import weka.core.converters.Loader;
 
 public abstract class AbstractInstancesRepresentation extends CharacterRepresentation {
 	
 	private Instances instances;
 	private Representation representation;
 	
 	public AbstractInstancesRepresentation(MediaType mediaType) {
 		super(mediaType);
 	}
 	
 	public AbstractInstancesRepresentation(MediaType mediaType,
 			Instances instances) {
 		super(mediaType);
 		this.instances = instances;
 	}
 
 	public AbstractInstancesRepresentation(Representation representation) {
		super(representation.getMediaType());
 		this.representation = representation;
 	}
 
 	protected abstract Loader getLoader() throws IOException;
 	
 	public void setInstances(Instances instances) {
 		this.instances = instances;
 	}
 
 	public Instances getInstances() throws IOException {
 		if (instances == null) {
 			if (representation == null)
 				throw new IOException("instances is null");
 			if (representation instanceof AbstractInstancesRepresentation)
 				instances = ((AbstractInstancesRepresentation) representation)
 						.getInstances();
 			else {
 				Loader loader = getLoader();
 				loader.setSource(representation.getStream());
 				instances = loader.getDataSet();
 			}
 		}
 		return instances;
 	}
 
 }
