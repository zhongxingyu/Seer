 package net.cheney.motown.resource.controller;
 
 import static com.google.common.collect.Iterables.getOnlyElement;
 import static net.cheney.motown.common.api.Depth.ZERO;
 import static net.cheney.motown.common.api.Header.ALLOW;
 import static net.cheney.motown.common.api.Header.CONTENT_TYPE;
 import static net.cheney.motown.common.api.Header.DAV;
 import static net.cheney.motown.common.api.Header.IF_MODIFIED_SINCE;
 import static net.cheney.motown.common.api.Header.LOCK_TOKEN;
 import static net.cheney.motown.common.api.MimeType.APPLICATION_OCTET_STREAM;
 import static net.cheney.motown.common.api.MimeType.APPLICATION_XML;
 import static net.cheney.motown.common.api.Response.clientErrorConflict;
 import static net.cheney.motown.common.api.Response.clientErrorLocked;
 import static net.cheney.motown.common.api.Response.clientErrorMethodNotAllowed;
 import static net.cheney.motown.common.api.Response.clientErrorNotFound;
 import static net.cheney.motown.common.api.Response.clientErrorPreconditionFailed;
 import static net.cheney.motown.common.api.Response.clientErrorUnsupportedMediaType;
 import static net.cheney.motown.common.api.Response.redirectionNotModified;
 import static net.cheney.motown.common.api.Response.serverErrorInternal;
 import static net.cheney.motown.common.api.Response.serverErrorNotImplemented;
 import static net.cheney.motown.common.api.Response.successCreated;
 import static net.cheney.motown.common.api.Response.successNoContent;
 import static net.cheney.motown.common.api.Status.SUCCESS_MULTI_STATUS;
 import static net.cheney.motown.common.api.Status.SUCCESS_OK;
 import static net.cheney.motown.resource.api.Elements.activeLock;
 import static net.cheney.motown.resource.api.Elements.href;
 import static net.cheney.motown.resource.api.Elements.lockDiscovery;
 import static net.cheney.motown.resource.api.Elements.multistatus;
 import static net.cheney.motown.resource.api.Elements.prop;
 import static net.cheney.motown.resource.api.Elements.propertyStatus;
 import static net.cheney.motown.resource.api.Elements.response;
 import static net.cheney.motown.resource.api.Lock.Scope.EXCLUSIVE;
 import static net.cheney.motown.resource.api.Lock.Type.WRITE;
 import static net.cheney.snax.model.ProcessingInstruction.XML_DECLARATION;
 
 import java.io.IOException;
 import java.net.URI;
 import java.nio.ByteBuffer;
 import java.nio.charset.Charset;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 import net.cheney.motown.common.api.Depth;
 import net.cheney.motown.common.api.Header;
 import net.cheney.motown.common.api.Message;
 import net.cheney.motown.common.api.Request;
 import net.cheney.motown.common.api.Response;
 import net.cheney.motown.common.api.Status;
 import net.cheney.motown.common.api.Message.Method;
 import net.cheney.motown.resource.api.Elements;
 import net.cheney.motown.resource.api.Lock;
 import net.cheney.motown.resource.api.Property;
 import net.cheney.motown.resource.api.Resource;
 import net.cheney.motown.resource.api.ResourceProvidor;
 import net.cheney.motown.resource.api.Elements.ACTIVE_LOCK;
 import net.cheney.motown.resource.api.Elements.MULTISTATUS;
 import net.cheney.motown.resource.api.Elements.PROPSTAT;
 import net.cheney.motown.resource.api.Elements.RESPONSE;
 import net.cheney.motown.resource.api.Resource.ComplianceClass;
 import net.cheney.motown.server.dispatcher.dynamic.COPY;
 import net.cheney.motown.server.dispatcher.dynamic.DELETE;
 import net.cheney.motown.server.dispatcher.dynamic.Destination;
 import net.cheney.motown.server.dispatcher.dynamic.Fragment;
 import net.cheney.motown.server.dispatcher.dynamic.GET;
 import net.cheney.motown.server.dispatcher.dynamic.LOCK;
 import net.cheney.motown.server.dispatcher.dynamic.MKCOL;
 import net.cheney.motown.server.dispatcher.dynamic.MOVE;
 import net.cheney.motown.server.dispatcher.dynamic.OPTIONS;
 import net.cheney.motown.server.dispatcher.dynamic.Overwrite;
 import net.cheney.motown.server.dispatcher.dynamic.PROPFIND;
 import net.cheney.motown.server.dispatcher.dynamic.PROPPATCH;
 import net.cheney.motown.server.dispatcher.dynamic.PUT;
 import net.cheney.motown.server.dispatcher.dynamic.PathTranslated;
 import net.cheney.motown.server.dispatcher.dynamic.UNLOCK;
 import net.cheney.motown.uri.Path;
 import net.cheney.snax.SNAX;
 import net.cheney.snax.model.Document;
 import net.cheney.snax.model.Element;
 import net.cheney.snax.model.QName;
 import net.cheney.snax.writer.XMLWriter;
 
 import org.apache.log4j.Logger;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Iterables;
 
 public class ResourceController {
 	
 	private static final Logger LOG = Logger.getLogger(ResourceController.class);
 
 	private static final List<QName> ALL_PROPS = Arrays.asList(new QName[] {
 			Property.CREATION_DATE, Property.DISPLAY_NAME,
 			Property.GET_CONTENT_LENGTH, Property.GET_LAST_MODIFIED,
 			Property.RESOURCE_TYPE });
 	
 	private static final Charset CHARSET_UTF_8 = Charset.forName("UTF-8");
 
 	private final ResourceProvidor providor;	
 	
 	public ResourceController(ResourceProvidor providor) {
 		this.providor = providor;
 	}
 
 	@GET
 	public Message get(@PathTranslated Path path, @Context Request request) throws IOException {
 		final Resource resource = resolveResource(path);
 		if (resource.exists()) {
 			if(resource.isCollection()) {
 				return clientErrorNotFound();
 			} else {
 				return getResource(request);
 			}
 		} else {
 			return clientErrorNotFound();
 		}
 	}
 	
 	@PUT
 	public Message put(@PathTranslated Path path, @Context Request request) throws IOException {
 		final Resource resource = resolveResource(path);
 		if (!resource.parent().exists()) {
 			return clientErrorConflict();
 		}
 
 		if (resource.exists() && resource.isCollection()) {
 			return clientErrorMethodNotAllowed();
 		}
 
 		ByteBuffer entity = request.buffer();
 		resource.put(entity);
 		return successCreated();
 	}
 	
 	private final Message getResource(final Request request) throws IOException {
 		return request.containsHeader(Header.IF_MATCH) ?
 				handleRequestWithIfMatch(request) : handleRequestWithoutIfMatch(request);
 	}
 	
 	private Message handleRequestWithIfMatch(Request request) throws IOException {
 		final Path path = Path.fromString(request.uri().getPath());
 		final Resource resource = resolveResource(path);
 
 		final String ifMatch = getOnlyElement(request.headers().get(Header.IF_MATCH));
 		
 		if (ifMatch.equals("*") || !ifMatch.equals(resource.etag())) {
 			return handleRequestWithoutIfMatch(request);
 		} else {
 			return Response.clientErrorPreconditionFailed();
 		}
 	}
 	
 	private Message handleRequestWithoutIfMatch(Request request) throws IOException {
 		if (request.containsHeader(Header.IF_UNMODIFIED_SINCE)) {
 			return handleRequestWithIfUnmodifiedSince();
 		} else {
 			return handleRequestWithoutIfUnmodifiedSince(request);
 		}
 	}
 	
 	private Message handleRequestWithIfUnmodifiedSince() {
 		return Response.serverErrorNotImplemented();
 	}
 
 	private Message handleRequestWithoutIfUnmodifiedSince(Request request) throws IOException {
 		if (request.containsHeader(Header.IF_NONE_MATCH)) {
 			return handleRequestWithIfNoneMatch();
 		} else {
 			return handleRequestWithoutIfNoneMatch(request);
 		}
 	}
 	
 	private Message handleRequestWithIfNoneMatch() {
 		return serverErrorNotImplemented();
 	}
 
 	private Message handleRequestWithoutIfNoneMatch(Request request) throws IOException {
 		if (request.containsHeader(Header.IF_MODIFIED_SINCE)) {
 			return handleRequestWithIfModifiedSince(request);
 		} else {
 			return handleRequestWithoutIfModifiedSince(request);
 		}
 	}
 	
 	private Message handleRequestWithoutIfModifiedSince(final Request request) throws IOException {
 		switch (request.method()) {
 		case POST:
 
 		case PUT:
 
 //		case DELETE:
 //			return delete(request);
 
 		case GET:
 		case HEAD:
 			if (request.headers().containsKey(Header.ACCEPT)) {
 				return handleRequestWithAccept(request);
 			} else {
 				return handleRequestWithoutAccept(request);
 			}
 
 		default:
 			return clientErrorMethodNotAllowed();
 		}
 	}
 	
 	private Message handleRequestWithAccept(final Request request) {
 		return handleRequestWithoutAccept(request);
 	}
 
 	private Message handleRequestWithoutAccept(final Request request) {
 		if (request.containsHeader(Header.ACCEPT_LANGUAGE)) {
 			return handleRequestWithAcceptLanguage(request);
 		} else {
 			return handleRequestWithoutAcceptLanguage(request);
 		}
 	}
 
 	private Message handleRequestWithAcceptLanguage(final Request request) {
 		return handleRequestWithoutAcceptLanguage(request);
 	}
 
 	private Message handleRequestWithoutAcceptLanguage(final Request request) {
 		if (request.containsHeader(Header.ACCEPT_CHARSET)) {
 			return handleRequestWithAcceptCharset(request);
 		} else {
 			return handleRequestWithoutAcceptCharset(request);
 		}
 	}
 
 	private Message handleRequestWithAcceptCharset(final Request request) {
 		return handleRequestWithoutAcceptCharset(request);
 	}
 
 	private Message handleRequestWithoutAcceptCharset(final Request request) {
 		if(request.containsHeader(Header.RANGE)) {
 			return handleRequestWithRange(request);
 		} else {
 			return handleRequestWithoutRange(request);
 		}
 	}
 
 	private Message handleRequestWithRange(Request request) {
 		return serverErrorNotImplemented();
 	}
 
 	private Message handleRequestWithoutRange(Request request) {
 		final Path path = Path.fromUri(request.uri());
 		final Resource resource = resolveResource(path);
 		
 		try {
 			return Response.build(SUCCESS_OK).header(CONTENT_TYPE).set(APPLICATION_OCTET_STREAM.toString()).setBody(resource.channel());
 		} catch (IOException e) {
 			return serverErrorInternal();
 		}
 	}
 
 	private Message handleRequestWithIfModifiedSince(final Request request) throws IOException {
 		final Path path = Path.fromUri(request.uri());
 		final Resource resource = resolveResource(path);
 
 		try {
 			String date = getOnlyElement(request.headers().get(IF_MODIFIED_SINCE));
 			Date ifModifiedSince = new SimpleDateFormat().parse(date);
 			if (!ifModifiedSince.after(new Date())
 					&& !(resource.lastModified().after(new Date()))) {
 				return redirectionNotModified();
 			} else {
 				return handleRequestWithoutIfModifiedSince(request);
 			}
 		} catch (ParseException e) {
 			return handleRequestWithoutIfModifiedSince(request);
 		}
 	}
 	
 	@DELETE
 	public Message delete(@PathTranslated Path path, @Fragment String fragment) throws IOException {
 		final Resource resource = resolveResource(path);
 		
 		if (fragment != null) {
 			return clientErrorMethodNotAllowed();
 		} else {
 			if (resource.isLocked()) {
 				return clientErrorLocked();
 			}
 			if (resource.exists()) {
 				if (resource.isLocked()) {
 					return clientErrorLocked();
 				} else {
 					if (resource.isCollection()) {
 						return (resource.delete() ? successNoContent() : serverErrorInternal());
 					} else {
 						return (resource.delete() ? successNoContent() : serverErrorInternal());
 					}
 				}
 			} else {
 				return clientErrorNotFound();
 			}
 		}
 	}
 	
 	@LOCK
 	public Message lock(@PathTranslated Path path, Depth depth) throws IOException {
 		final Resource resource = resolveResource(path);
 
 		if (resource.exists()) {
 			final Lock lock = resource.lock(WRITE, EXCLUSIVE);
 			
 			final ACTIVE_LOCK activelock = activeLock(lock, depth, relativizeResource(resource));
 			final Element lockDiscovery = lockDiscovery(activelock);
 			final Element prop = prop(lockDiscovery);
 			final Document doc = new Document(XML_DECLARATION, prop);
 			return Response.build(SUCCESS_OK)
 				.header(LOCK_TOKEN).set("<" + lock.token() + ">")
 				.setBody(Charset.defaultCharset().encode(XMLWriter.write(doc)));
 		} else {
 			return clientErrorNotFound();
 		}
 	}
 	
 	@OPTIONS
 	public Message options(@PathTranslated Path path) {
 		final Resource resource = resolveResource(path);
 
 		Message response = Response.successNoContent();
 		for(Method method : resource.supportedMethods()) {
 			response.headers().put(ALLOW, method.name());
 		}
 		
 		for(ComplianceClass value : resource.davOptions()) {
 			response.headers().put(DAV, value.toString());
 		}
 		
 		return response;
 	}
 	
 	@UNLOCK
 	public Message unlock(@PathTranslated Path path) throws IOException {
 		final Resource resource = resolveResource(path);
 		
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
 	public Message move(@PathTranslated Path path, @Destination URI destinationUri, @Overwrite boolean overwrite) throws IOException {
 		final Resource source = resolveResource(path);
 
 		final Resource destination = resolveResource(Path.fromString(destinationUri.getPath()));
 		
 		if (destination.isLocked()) {
 			return Response.clientErrorLocked();
 		}
 		
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
	public Message copy(@PathTranslated Path path, @Context Request request) throws IOException {
 		final Resource source = resolveResource(path);
 
		final URI destinationUri = request.getDestination();
 		final Resource destination = resolveResource(Path.fromString(destinationUri.getPath()));
 		
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
 
 	private Message copyResourceToResource(final Resource source, final Resource destination, final boolean overwrite) throws IOException {
 		source.copyTo(destination);
 		return successNoContent();
 	}
 
 	private Message copyResourceToCollection(final Resource source, final Resource destination, final boolean overwrite) throws IOException {
 		if (overwrite) {
 			destination.delete();
 		}
 		source.copyTo(destination);
 		return overwrite ? successNoContent() : successCreated();
 	}
 
 	private Message copyCollectionToResource(final Resource source, final Resource destination, final boolean overwrite) throws IOException {
 		source.copyTo(destination);
 		return successNoContent();
 	}
 
 	private Message copyCollectionToCollection(final Resource source, final Resource destination, final boolean overwrite) throws IOException {
 		source.copyTo(destination);
 		return successNoContent();
 	}
 	
 	private Message moveResourceToResource(final Resource source, final Resource destination, final boolean overwrite) throws IOException {
 		source.moveTo(destination);
 		return successNoContent();
 	}
 
 	private Message moveResourceToCollection(final Resource source, final Resource destination, final boolean overwrite) throws IOException {
 		if (overwrite) {
 			destination.delete();
 		}
 		source.moveTo(destination);
 		return overwrite ? successNoContent() : successCreated();
 	}
 
 	private Message moveCollectionToResource(final Resource source, final Resource destination, final boolean overwrite) throws IOException {
 		source.moveTo(destination);
 		return successNoContent();
 	}
 
 	private Message moveCollectionToCollection(final Resource source, final Resource destination, final boolean overwrite) throws IOException {
 		source.moveTo(destination);
 		return successNoContent();
 	}
 	
 	@MKCOL
	public Message mkcol(@PathTranslated Path path, @Context Request request) {
 		final Resource resource = resolveResource(path);
 		
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
 	public Message proppatch(@PathTranslated Path path, @Context Request request, Depth depth) throws IOException {
 		final Resource resource = resolveResource(path);
 		final Iterable<QName> properties = getProperties(request.buffer());
 		final List<RESPONSE> responses = propfind(properties, resource, depth);
 		return successMultiStatus(multistatus(responses));
 	}
 
 	private Message successMultiStatus(MULTISTATUS multiStatus) {
 		final Document doc = new Document(XML_DECLARATION, multiStatus);
 		return Response.build(SUCCESS_MULTI_STATUS).header(CONTENT_TYPE).set(APPLICATION_XML.toString()).setBody(CHARSET_UTF_8.encode(XMLWriter.write(doc)));
 	}
 
 	@PROPFIND
 	public Message propfind(@PathTranslated Path path, Depth depth, @Context Request request) throws IOException {
 		return propfind(path, depth, request.buffer());
 	}
 
 	private Message propfind(Path path, Depth depth, ByteBuffer body) throws IOException {
 		final Resource resource = resolveResource(path);
 
 			final Iterable<QName> properties = getProperties(body);
 			final List<RESPONSE> responses = propfind(properties, resource, depth);
 			if (resource.exists()) {
 				return successMultiStatus( multistatus(responses));
 			} else {
 				return clientErrorNotFound();
 			}
 	}
 
 	private final List<RESPONSE> propfind(final Iterable<QName> properties, final Resource resource, final Depth depth) {
 		final List<RESPONSE> responses = new ArrayList<RESPONSE>();
 		
 		responses.add(response(href(relativizeResource(resource)), getProperties(resource, properties)));
 		if (depth != ZERO) {
 			for (final Resource child : resource.members()) {
 				responses.addAll(propfind(properties, child, depth.decreaseDepth()));
 			}
 		}
 		return responses;
 	}
 
 	private final List<PROPSTAT> getProperties(final Resource resource, final Iterable<QName> properties) {
 		final List<PROPSTAT> propstats = new ArrayList<PROPSTAT>(2);
 		final List<Element> foundProps = new ArrayList<Element>();
 		final List<Element> notFoundProps = new ArrayList<Element>();
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
 
 	private final Iterable<QName> getProperties(final ByteBuffer buffer) throws IOException {
 		final Document doc = getPropfind(buffer);
 		final Element propfind = doc.rootElement();
 		final Element props = propfind.getChildren(Elements.PROP).first();
 		if (props == null || !props.hasChildren()) {
 			return ALL_PROPS;
 		} else {
 			return Iterables.transform(props.childElements(), new Function<Element, QName>() {
 				@Override
 				public QName apply(Element property) {
 					return property.qname();
 				}
 			});
 		}
 	}
 
 	private final Document getPropfind(final ByteBuffer buffer) throws IOException {
 		Document document = SNAX.parse(CHARSET_UTF_8.decode(buffer));
 		LOG.debug("Request Body: "+XMLWriter.write(document));
 		return document;
 	}
 	
 	protected URI relativizeResource(Resource resource) {
 		return providor.relativizeResource(resource);
 	}
 	
 	protected Resource resolveResource(Path path) {
 		return providor.resolveResource(path);
 	}
 }
