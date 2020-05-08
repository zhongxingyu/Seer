 package net.cheney.motown.dav;
 
 import static net.cheney.motown.api.Response.clientErrorConflict;
 import static net.cheney.motown.api.Response.clientErrorLocked;
 import static net.cheney.motown.api.Response.clientErrorMethodNotAllowed;
 import static net.cheney.motown.api.Response.clientErrorNotFound;
 import static net.cheney.motown.api.Response.clientErrorPreconditionFailed;
 import static net.cheney.motown.api.Response.clientErrorUnsupportedMediaType;
 import static net.cheney.motown.api.Response.serverErrorInternal;
 import static net.cheney.motown.api.Response.successCreated;
 import static net.cheney.motown.api.Response.successNoContent;
 import static net.cheney.motown.dav.Elements.activeLock;
 import static net.cheney.motown.dav.Elements.href;
 import static net.cheney.motown.dav.Elements.lockDiscovery;
 import static net.cheney.motown.dav.Elements.multistatus;
 import static net.cheney.motown.dav.Elements.prop;
 import static net.cheney.motown.dav.Elements.propertyStatus;
 import static net.cheney.motown.dav.Elements.response;
 
 import java.io.IOException;
 import java.net.URI;
 import java.nio.ByteBuffer;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import net.cheney.motown.api.Depth;
 import net.cheney.motown.api.Header;
 import net.cheney.motown.api.Request;
 import net.cheney.motown.api.Response;
 import net.cheney.motown.api.Status;
 import net.cheney.motown.api.Response.Builder;
 import net.cheney.motown.dav.Elements.ACTIVE_LOCK;
 import net.cheney.motown.dav.Elements.MULTISTATUS;
 import net.cheney.motown.dav.Elements.PROPSTAT;
 import net.cheney.motown.dav.Elements.RESPONSE;
 import net.cheney.motown.dav.Lock.Scope;
 import net.cheney.motown.dav.Lock.Type;
 import net.cheney.motown.dav.annotations.COPY;
 import net.cheney.motown.dav.annotations.LOCK;
 import net.cheney.motown.dav.annotations.MKCOL;
 import net.cheney.motown.dav.annotations.MOVE;
 import net.cheney.motown.dav.annotations.PROPFIND;
 import net.cheney.motown.dav.annotations.PROPPATCH;
 import net.cheney.motown.dav.annotations.UNLOCK;
 import net.cheney.motown.dav.resource.api.DavResource;
 import net.cheney.motown.dav.resource.api.DavResource.ComplianceClass;
 import net.cheney.motown.dispatcher.dynamic.Context;
 import net.cheney.motown.dispatcher.dynamic.OPTIONS;
 import net.cheney.motown.webservice.controller.StaticController;
 import net.cheney.snax.model.Document;
 import net.cheney.snax.model.Element;
 import net.cheney.snax.model.ProcessingInstruction;
 import net.cheney.snax.model.QName;
 import net.cheney.snax.parser.XMLBuilder;
 import net.cheney.snax.writer.XMLWriter;
 import net.cheney.uri.Path;
 
 import org.apache.log4j.Logger;
 
 import com.google.common.collect.ArrayListMultimap;
 
 public abstract class DavController extends StaticController {
 	
 	private static final Logger LOG = Logger.getLogger(DavController.class);
 
 	private static final List<QName> ALL_PROPS = Arrays.asList(new QName[] {
 			Property.CREATION_DATE, Property.DISPLAY_NAME,
 			Property.GET_CONTENT_LENGTH, Property.GET_LAST_MODIFIED,
 			Property.RESOURCE_TYPE });
 	
 	private static final Charset CHARSET_UTF_8 = Charset.forName("UTF-8");	
 	
 	@LOCK
 	public Response lock(@Context Request request) throws IOException {
 		final Path path = Path.fromString(request.uri().getPath());
 		final DavResource resource = resolveResource(path);
 
 		if (resource.exists()) {
 			final Lock lock = resource.lock(Type.WRITE, Scope.EXCLUSIVE);
 			
 			final ACTIVE_LOCK activelock = activeLock(lock, request.getDepth(Depth.INFINITY), relativizeResource(resource));
 			final Element lockDiscovery = lockDiscovery(activelock);
 			final Element prop = prop(lockDiscovery);
 			final Document doc = new Document(ProcessingInstruction.XML_DECLARATION, prop);
 			Builder builder = Response.builder(Status.SUCCESS_OK);
 			builder.setHeader(Header.LOCK_TOKEN, "<" + lock.token() + ">");
 			builder.setBody(Charset.defaultCharset().encode(XMLWriter.write(doc)));
 			return builder.build();
 		} else {
 			return clientErrorNotFound();
 		}
 	}
 	
 	@OPTIONS
 	public Response options(@Context Request request) {
 		final Path path = Path.fromString(request.uri().getPath());
 		final DavResource resource = resolveResource(path);
 		
 		Response response = super.options(request);
 		
 		for(ComplianceClass value : resource.davOptions()) {
 			response.headers().put(Header.DAV, value.toString());
 		}
 		
 		return response;
 	}
 	
 	@UNLOCK
 	public Response unlock(@Context Request request) throws IOException {
 		final Path path = Path.fromString(request.uri().getPath());
 		final DavResource resource = resolveResource(path);
 		
 		if (resource.exists()) {
 			if (resource.isLocked()) {
 				resource.unlock();
 				return successNoContent();
 			} else {
 				return clientErrorLocked();
 			}
 		} else {
 			return clientErrorNotFound();
 		}
 	}
 	
 	@MOVE
 	public Response move(@Context Request request) throws IOException {
 		final Path path = Path.fromString(request.uri().getPath());
 		final DavResource source = resolveResource(path);
 
 		final URI destinationUri = request.getDestination();
 		final DavResource destination = resolveResource(Path.fromString(destinationUri.getPath()));
 		
 		if (destination.isLocked()) {
 			return Response.clientErrorLocked();
 		}
 		
 		final boolean overwrite = request.isOverwrite();
 		
 		if (source.exists()) {
 			if (source.isCollection()) { // source exists
 				if (destination.exists()) { // source exists and is a collection
 					if (destination.isCollection()) {
 						return (overwrite ? moveCollectionToCollection(source,
 								destination, overwrite)
 								: Response.clientErrorPreconditionFailed());
 					} else {
 						return (overwrite ? moveCollectionToResource(source,
 								destination, overwrite)
 								: Response.clientErrorPreconditionFailed());
 					}
 				} else {
 					return (destination.parent().exists() ? moveCollectionToCollection(
 							source, destination, false)
 							: Response.clientErrorPreconditionFailed());
 				}
 			} else {
 				if (destination.exists()) { // source exists
 					if (destination.isCollection()) { // source exists,
 						return (overwrite ? moveResourceToCollection(source,
 								destination, overwrite)
 								: Response.clientErrorPreconditionFailed());
 					} else {
 						return (overwrite ? moveResourceToResource(source,
 								destination, overwrite)
 								: Response.clientErrorPreconditionFailed());
 					}
 				} else {
 					if (destination.parent().exists()) {
 						return (overwrite ? Response.clientErrorPreconditionFailed()
 								: moveResourceToCollection(source, destination,
 										overwrite));
 					} else {
 						return clientErrorConflict();
 					}
 				}
 			}
 		} else {
 			return clientErrorNotFound();
 		}
 	}
 
 	@COPY
 	public Response copy(@Context Request request) throws IOException {
 		final Path path = Path.fromString(request.uri().getPath());
 		final DavResource source = resolveResource(path);
 
 		final URI destinationUri = request.getDestination();
 		final DavResource destination = resolveResource(Path.fromString(destinationUri.getPath()));
 		
 		if (destination.isLocked()) {
 			return clientErrorLocked();
 		}
 		
 		final boolean overwrite = request.isOverwrite();
 		
 		if (source.exists()) {
 			if (source.isCollection()) { // source exists
 				if (destination.exists()) { // source exists and is a collection
 					if (destination.isCollection()) {
 						return (overwrite ? copyCollectionToCollection(source,
 								destination, overwrite)
 								: clientErrorPreconditionFailed());
 					} else {
 						return (overwrite ? copyCollectionToResource(source,
 								destination, overwrite)
 								: clientErrorPreconditionFailed());
 					}
 				} else {
 					return (destination.parent().exists() ? copyCollectionToCollection(
 							source, destination, false)
 							: clientErrorPreconditionFailed());
 				}
 			} else {
 				if (destination.exists()) { // source exists
 					if (destination.isCollection()) { // source exists,
 						return (overwrite ? copyResourceToCollection(source,
 								destination, overwrite)
 								: clientErrorPreconditionFailed());
 					} else {
 						return (overwrite ? copyResourceToResource(source,
 								destination, overwrite)
 								: clientErrorPreconditionFailed());
 					}
 				} else {
 					if (destination.parent().exists()) {
 						return (overwrite ? clientErrorPreconditionFailed()
 								: copyResourceToCollection(source, destination,
 										overwrite));
 					} else {
 						return clientErrorConflict();
 					}
 				}
 			}
 		} else {
 			return clientErrorNotFound();
 		}
 	}
 
 	private Response copyResourceToResource(final DavResource source, final DavResource destination, final boolean overwrite) throws IOException {
 		source.copyTo(destination);
 		return successNoContent();
 	}
 
 	private Response copyResourceToCollection(final DavResource source, final DavResource destination, final boolean overwrite) throws IOException {
 		if (overwrite) {
 			destination.delete();
 		}
 		source.copyTo(destination);
 		return overwrite ? successNoContent() : successCreated();
 	}
 
 	private Response copyCollectionToResource(final DavResource source, final DavResource destination, final boolean overwrite) throws IOException {
 		source.copyTo(destination);
 		return successNoContent();
 	}
 
 	private Response copyCollectionToCollection(final DavResource source, final DavResource destination, final boolean overwrite) throws IOException {
 		source.copyTo(destination);
 		return successNoContent();
 	}
 	
 	private Response moveResourceToResource(final DavResource source, final DavResource destination, final boolean overwrite) throws IOException {
 		source.moveTo(destination);
 		return successNoContent();
 	}
 
 	private Response moveResourceToCollection(final DavResource source, final DavResource destination, final boolean overwrite) throws IOException {
 		if (overwrite) {
 			destination.delete();
 		}
 		source.moveTo(destination);
 		return overwrite ? successNoContent() : successCreated();
 	}
 
 	private Response moveCollectionToResource(final DavResource source, final DavResource destination, final boolean overwrite) throws IOException {
 		source.moveTo(destination);
 		return successNoContent();
 	}
 
 	private Response moveCollectionToCollection(final DavResource source, final DavResource destination, final boolean overwrite) throws IOException {
 		source.moveTo(destination);
 		return successNoContent();
 	}
 	
 	@MKCOL
 	public Response mkcol(@Context Request request) {
 		final Path path = Path.fromString(request.uri().getPath());
 		final DavResource resource = resolveResource(path);
 		
 		if (request.hasBody()) {
 			return clientErrorUnsupportedMediaType();
 		} else {
 			if (resource.exists()) {
 				return clientErrorMethodNotAllowed();
 			} else {
 				if (resource.parent().exists()) {
 					return (resource.mkcol() ? successCreated()
 							: serverErrorInternal());
 				} else {
 					return clientErrorConflict();
 				}
 			}
 		}
 	}
 	
 	@PROPPATCH
 	public Response proppatch(@Context Request request) throws IOException {
 		final Path path = Path.fromString(request.uri().getPath());
 		final DavResource resource = resolveResource(path);
 		final List<QName> properties = getProperties(request.body());
 		final List<RESPONSE> responses = propfind(properties, resource, request.getDepth(Depth.INFINITY));
 		return successMultiStatus(multistatus(responses));
 	}
 
 	private Response successMultiStatus(MULTISTATUS multiStatus) {
 		final Document doc = new Document(ProcessingInstruction.XML_DECLARATION, multiStatus);
 		return new Response(Status.SUCCESS_MULTI_STATUS, ArrayListMultimap.<Header, String>create(), CHARSET_UTF_8.encode(XMLWriter.write(doc)));
 	}
 
 	@PROPFIND
 	public Response propfind(@Context Request request) throws IOException {
		final Path path = Path.fromString(request.uri().getPath());
 		final DavResource resource = resolveResource(path);
 
			final List<QName> properties = getProperties(request.body());
			final List<RESPONSE> responses = propfind(properties, resource, request.getDepth(Depth.INFINITY));
 			if (resource.exists()) {
 				return successMultiStatus( multistatus(responses));
 			} else {
 				return clientErrorNotFound();
 			}
 	}
 
 	private final List<RESPONSE> propfind(final List<QName> properties, final DavResource resource, final Depth depth) {
 		final List<RESPONSE> responses = new ArrayList<RESPONSE>();
 		
 		responses.add(response(href(relativizeResource(resource)), getProperties(resource, properties)));
 		if (depth != Depth.ZERO) {
 			final Depth newdepth = depth.decreaseDepth();
 			for (final DavResource child : resource.members()) {
 				responses.addAll(propfind(properties, child, newdepth));
 			}
 		}
 		return responses;
 	}
 
 	private final List<PROPSTAT> getProperties(final DavResource resource, final List<QName> properties) {
 		final List<PROPSTAT> propstats = new ArrayList<PROPSTAT>(2);
 		final List<Element> foundProps = new ArrayList<Element>(properties.size());
 		final List<Element> notFoundProps = new ArrayList<Element>(properties.size());
 		for (final QName property : properties) {
 			final Element prop = resource.getProperty(property);
 			if(prop == null) {
 				notFoundProps.add(new Element(property));
 			} else {
 				foundProps.add(prop);
 			}
 		}
 		if(!foundProps.isEmpty()) {
 			propstats.add(propertyStatus(prop(foundProps), Status.SUCCESS_OK));
 		} 
 		if(!notFoundProps.isEmpty()) {
 			propstats.add(propertyStatus(prop(notFoundProps), Status.CLIENT_ERROR_NOT_FOUND));
 		}
 		return propstats;
 	}
 
 	private final List<QName> getProperties(final ByteBuffer buffer) throws IOException {
 		final Document doc = getPropfind(buffer);
 		final Element propfind = doc.rootElement();
 		final Element props = propfind.getFirstChild(Elements.PROP);
 		if (props == null || !props.hasChildren()) {
 			return ALL_PROPS;
 		} else {
 			final List<QName> properties = new ArrayList<QName>();
 			for (final Element prop : props.childElements()) {
 				properties.add(prop.qname()); 
 			}
 			return properties;
 		}
 	}
 
 	private final Document getPropfind(final ByteBuffer buffer) throws IOException {
 		XMLBuilder builder = new XMLBuilder();
 		Document document = builder.build(buffer, CHARSET_UTF_8);
 		LOG.debug("Request Body: "+XMLWriter.write(document));
 		return document;
 	}
 	
 	protected abstract DavResource resolveResource(Path path);
 	
 }
