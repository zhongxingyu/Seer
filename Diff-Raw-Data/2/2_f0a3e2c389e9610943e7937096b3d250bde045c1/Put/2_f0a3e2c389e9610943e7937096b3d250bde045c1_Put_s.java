 package net.cheney.manhattan.application;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 
 import net.cheney.cocktail.application.Environment;
 import net.cheney.cocktail.message.Response;
 import net.cheney.manhattan.resource.Resource;
 import net.cheney.manhattan.resource.ResourceProvidor;
 
 public class Put extends BaseApplication {
 
 	public Put(ResourceProvidor providor) {
 		super(providor);
 	}
 
 	@Override
 	public Response call(Environment env) {
 		Resource resource = resolveResource(env);
 		boolean exists = resource.exists();
 		if (exists && resource.isCollection()) {
 			return clientErrorMethodNotAllowed();
 		}
 
 		Resource parent = resource.parent();
 		if (!parent.exists()) {
 			return clientErrorConflict();
 		}
 		
 		try {
 			if(env.hasBody()) {
				parent.create(resource.name(), (ByteBuffer) env.body().flip());
 			} else {
 				parent.create(resource.name(), ByteBuffer.allocate(0));
 			}
 			return exists ? successNoContent() : successCreated();
 		} catch (IOException e) {
 			return serverErrorInternal(e);
 		}
 	}
 
 }
