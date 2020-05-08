 package net.cheney.manhattan.dav;
 
 import java.io.IOException;
 
 import net.cheney.cocktail.application.Environment;
 import net.cheney.cocktail.application.Path;
 import net.cheney.cocktail.message.Response;
 import net.cheney.manhattan.resource.api.Resource;
 import net.cheney.manhattan.resource.api.ResourceProvidor;
 
 import org.apache.log4j.Logger;
 
 public class Copy extends RFC4918 {
 	private static final Logger LOG = Logger.getLogger(Copy.class);
 
 	public Copy(ResourceProvidor providor) {
 		super(providor);
 	}
 
 	@Override
 	public Response call(Environment env) {
 		final Resource source = resolveResource(env);
 		
 		final Resource destination = resolveResource(Path.fromURI(destination(env)));
 		
		if (destination.isLocked()) {
 			return clientErrorLocked();
 		}
 		
 		boolean overwrite = overwrite(env);
 		
 		try {
 			return copy(source, destination, overwrite);
 		} catch (IOException e) {
 			return serverErrorInternal(e);
 		}
 	}
 
 	private Response copy(Resource source, Resource destination, boolean overwrite) throws IOException {
 		LOG.debug(String.format("COPY: src[%s], dest[%s], overwrite[%b]", source, destination, overwrite));
 		if (source.exists()) {
 			if (source.isCollection()) { // source exists
 				if (destination.exists()) { // source exists and is a collection
 					if (destination.isCollection()) {
 						if(overwrite) {
 							destination.delete();
 							source.copyTo(destination);
 							return successNoContent();
 						} else {
 							return clientErrorPreconditionFailed();
 						}
 					} else {
 						if(overwrite) {
 							source.copyTo(destination.parent());
 							return successCreated();
 						} else {
 							return clientErrorPreconditionFailed();
 						}
 					}
 				} else {
 					if(destination.parent().exists()) {
 						source.copyTo(destination);
 						return successNoContent();
 					} else {
 						return clientErrorPreconditionFailed();
 					}
 				}
 			} else {
 				if (destination.exists()) { // source exists
 					if (destination.isCollection()) {
 						if(overwrite) {
 							source.copyTo(destination);
 							return successNoContent();
 						} else {
 							return clientErrorPreconditionFailed();
 						}
 					} else {
 						if(overwrite) {
 							source.copyTo(destination);
 							return successNoContent();
 						} else {
 							return clientErrorPreconditionFailed();
 						}
 					}
 				} else {
 					if (destination.parent().exists()) {
 						source.copyTo(destination);
 						return successCreated();
 					} else {
 						return clientErrorConflict();
 					}
 				}
 			}
 		} 
 		return clientErrorNotFound();
 	}
 
 }
