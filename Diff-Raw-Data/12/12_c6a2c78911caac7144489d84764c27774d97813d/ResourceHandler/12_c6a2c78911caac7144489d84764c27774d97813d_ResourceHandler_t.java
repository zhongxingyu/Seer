 package net.northfuse.resources;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.core.io.ByteArrayResource;
 import org.springframework.core.io.Resource;
 import org.springframework.core.io.support.ResourcePatternResolver;
 import org.springframework.http.MediaType;
 import org.springframework.util.FileCopyUtils;
 
 import javax.annotation.PostConstruct;
 import java.io.*;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * @author tylers2
  */
 public abstract class ResourceHandler implements ApplicationContextAware {
 	private final List<String> resourcePaths = new LinkedList<String>();
 	private final List<Resource> resources = new LinkedList<Resource>();
 	private final MediaType mediaType;
 	private boolean debug;
 	private Resource resource;
 	private String mapping;
 	private ResourcePatternResolver resourceResolver;
 	private long lastModified = -1;
 
 	private static final Logger LOG = LoggerFactory.getLogger(ResourceHandler.class);
 
 	/**
 	 * Creates a resource handler.
 	 *
 	 * @param mediaType The MediaType
 	 */
 	ResourceHandler(MediaType mediaType) {
 		this.mediaType = mediaType;
 	}
 
 	/**
 	 * Sets debug mode.
 	 *
 	 * @param debug Whether or not to enable debug mode
 	 */
 	public final void setDebug(boolean debug) {
 		this.debug = debug;
 	}
 
 	/**
 	 * Gets the mapping of this resource.
 	 *
 	 * @return The mapping
 	 */
 	public final String getMapping() {
 		return mapping;
 	}
 
 	/**
 	 * Sets the mapping.
 	 *
 	 * @param mapping The mapping
 	 */
 	public final void setMapping(String mapping) {
 		this.mapping = mapping;
 	}
 
 	/**
 	 * Sets the resources.
 	 *
 	 * @param resources The new resources to add
 	 */
 	public final void setResources(List<String> resources) {
 		this.resourcePaths.addAll(resources);
 	}
 
 	/**
 	 * Sets the application context.
 	 *
 	 * @param applicationContext The application context
 	 */
 	@Override
 	public final void setApplicationContext(ApplicationContext applicationContext) {
 		setResourceResolver(applicationContext);
 	}
 
 	/**
 	 * Sets the resource resolver.
 	 * @param resourceResolver The resource resolver
 	 */
 	public final void setResourceResolver(ResourcePatternResolver resourceResolver) {
 		this.resourceResolver = resourceResolver;
 	}
 
 	/**
	 * Gets the last modified time.
 	 * @return the last modified time
 	 */
	public final long lastModified() {
 		return lastModified;
 	}
 
 	/**
 	 * Aggregates the resource, creating a new resource.
 	 * If debug mode is enabled, re-aggregates them based off of the timestamps of the files.
 	 * If debug mode is not enabled, return the cached resource
 	 *
 	 * @return a non-null resource
 	 */
 	public final Resource aggregatedResource() {
 		if (debug) {
 			synchronized (this.resources) {
 				this.resources.clear();
 				resolveResources();
 				return buildResource(false);
 			}
 		} else {
 			return resource;
 		}
 	}
 
 	/**
 	 * Initialize (cache) resource.
 	 */
 	@PostConstruct
 	public final void init() {
 		try {
 			if (!debug) {
 				resolveResources();
 				generateResource();
 			}
 		} catch (Throwable t) {
 			LOG.error("Unable to initialize resource: " + mapping, t);
 		}
 	}
 
 	/**
 	 * resolves resources.
 	 */
 	private void resolveResources() {
 		boolean debug = LOG.isDebugEnabled();
 		if (debug) {
 			LOG.debug("resolving resources");
 		}
 		for (String resourcePath : resourcePaths) {
 			if (debug) {
 				LOG.debug("resolving resource path [" + resourcePath + "]");
 			}
 			try {
 				Resource[] resources = resourceResolver.getResources(resourcePath);
 				if (debug) {
 					LOG.debug("Found " + resources.length + " resources:");
 					for (Resource resource : resources) {
 						LOG.debug("\t" + resource.getDescription());
 					}
 				}
 				this.resources.addAll(Arrays.asList(resources));
 			} catch (IOException e) {
 				throw new IllegalStateException("Unable to get resources for resourcePath [" + resourcePath + "]", e);
 			}
 		}
 	}
 
 	/**
 	 * Generates resource and caches it.
 	 */
 	private void generateResource() {
 		resource = buildResource(true);
 	}
 
 	/**
 	 * Builds the resource from all the given resources.
 	 *
 	 * @param minify Whether or not to minify
 	 *
 	 * @return The constructed resource
 	 */
 	private Resource buildResource(boolean minify) {
 		final long newLastModified = findLastModifiedTime();
 		if (newLastModified <= lastModified && resource != null) {
 			return resource;
 		}
 		lastModified = newLastModified;
 		final byte[] data = aggregate(minify);
 		LOG.debug("Built resource with " + data.length + " bytes @" + lastModified);
 		resource = new ByteArrayResource(data) {
 			@Override
 			public long lastModified() throws IOException {
 				return newLastModified;
 			}
 
 			@Override
 			public long contentLength() throws IOException {
 				return data.length;
 			}
 		};
 		return resource;
 	}
 
	/**
	 * Finds the last modified time of the newest resource.
	 * @return the last modified time
	 */
 	private long findLastModifiedTime() {
 		long lastModified = -1;
 		for (Resource resource : resources) {
 			try {
 				long resourceLastModified = resource.lastModified();
 				if (resourceLastModified > lastModified) {
 					lastModified = resourceLastModified;
 				}
 			} catch (IOException e) {
 				LOG.debug("couldn't get resource last modified time", e);
 			}
 		}
 		return lastModified;
 	}
 
 	/**
 	 * Aggregates the resources.
 	 *
 	 * @param minify Whether or not to minify.
 	 *
 	 * @return A non-null byte array
 	 */
 	private byte[] aggregate(boolean minify) {
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		LOG.debug("Building " + mapping);
 		for (Resource resource : resources) {
 			try {
 				LOG.debug("Adding " + resource.getDescription());
 				InputStream is = resource.getInputStream();
 				if (minify) {
 					is = wrapWithMinify(is);
 				} else if (debug) {
 					String description = resource.getDescription();
 					String text = "/WEB-INF/classes";
 					int index = description.indexOf(text);
 					if (index > 0) {
 						description = description.substring(index + text.length());
 					}
 					is = new LineWrapperInputStream(is, description);
 				}
 				FileCopyUtils.copy(is, baos);
 				baos.write('\n');
 			} catch (IOException e) {
 				throw new IllegalStateException("Unable to copy resource file [" + resource.getDescription() + "]", e);
 			}
 		}
 		LOG.debug("Built " + mapping);
 		return baos.toByteArray();
 	}
 
 	/**
 	 * Wraps the input stream with an input stream that minifies it.
 	 *
 	 * @param is The input stream to wrap
 	 * @return A wrapped input stream
 	 * @throws IOException If an io exception occurs.
 	 */
 	protected abstract InputStream wrapWithMinify(InputStream is) throws IOException;
 
 	/**
 	 * Gets the media type.
 	 *
 	 * @return A non-null media-type
 	 */
 	public final MediaType getMediaType() {
 		return mediaType;
 	}
 }
