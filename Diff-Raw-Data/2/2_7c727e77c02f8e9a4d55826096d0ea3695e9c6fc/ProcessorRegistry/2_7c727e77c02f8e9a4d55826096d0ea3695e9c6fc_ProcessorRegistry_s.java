 package hemera.core.structure.runtime;
 
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import hemera.core.structure.interfaces.IModule;
 import hemera.core.structure.interfaces.IProcessor;
 import hemera.core.structure.interfaces.IRequest;
 import hemera.core.structure.interfaces.IResponse;
 import hemera.core.structure.interfaces.runtime.IProcessorRegistry;
 import hemera.core.utility.logging.FileLogger;
 
 /**
  * <code>ProcessorRegistry</code> defines an internal
  * writable <code>IProcessorRegister</code> providing
  * thread-safe and highly concurrent read and write
  * registration operations.
  *
  * @author Yi Wang (Neakor)
  * @version 1.0.0
  */
 class ProcessorRegistry implements IProcessorRegistry {
 	/**
 	 * The <code>FileLogger</code> instance.
 	 */
 	private final FileLogger logger;
 	/**
 	 * The <code>ConcurrentMap</code> of combined
 	 * processor path <code>String</code> to the
 	 * <code>IProcessor</code> instance.
 	 */
 	private final ConcurrentMap<String, IProcessor<?, ?>> repository;
 
 	/**
 	 * Constructor of <code>ProcessorRegistry</code>.
 	 */
 	ProcessorRegistry() {
 		this.logger = FileLogger.getLogger(this.getClass());
 		// Use default concurrency level since the number of
 		// concurrent updating threads shouldn't be too large.
 		this.repository = new ConcurrentHashMap<String, IProcessor<?,?>>();
 	}
 
 	/**
 	 * Register all the processors within the given
 	 * module to the registry.
 	 * @param module The <code>IModule</code> to be
 	 * registered.
 	 */
 	void register(final IModule module) {
 		final Iterable<IProcessor<?, ?>> processors = module.getProcessors();
 		for (final IProcessor<?, ?> processor : processors) {
 			final String path = this.getCombinedPath(module, processor);
 			final Object existing = this.repository.put(path, processor);
 			// Override processor.
 			if (existing != null) {
 				final StringBuilder builder = new StringBuilder();
 				builder.append("There are more than one processor defined at the same REST path:");
 				builder.append(path).append(".");
 				this.logger.warning(builder.toString());
 			}
 			this.logger.info("Processor registered at REST path: " + path);
 		}
 	}
 
 	/**
 	 * Retrieve the valid REST path by removing or
 	 * appending the proper separator character.
 	 * @param path The <code>String</code> path to
 	 * check.
 	 * @return The valid <code>String</code> path
 	 * with the format <code>/path</code> with all
 	 * letters in lower case.
 	 */
 	private String getValidPath(final String path) {
 		final StringBuilder builder = new StringBuilder();
 		// Beginning slash.
 		builder.append("/");
 		// Content.
 		final int length = path.length();
 		for (int i = 0; i < length; i++) {
 			final char c = path.charAt(i);
			if (Character.isLetterOrDigit(c)) {
 				builder.append(c);
 			}
 		}
 		return builder.toString().toLowerCase();
 	}
 
 	/**
 	 * Retrieve the combined path of the given processor
 	 * that belongs to the given module.
 	 * @param module The parent <code>IModule</code>.
 	 * @param processor The <code>IProcessor</code>
 	 * instance.
 	 * @return The <code>String</code> combined path
 	 * in the format <code>/module/processor</code> with
 	 * all letters in lower case.
 	 */
 	private String getCombinedPath(final IModule module, final IProcessor<?, ?> processor) {
 		final StringBuilder builder = new StringBuilder();
 		final String validModulePath = this.getValidPath(module.getPath());
 		final String validProcessorPath = this.getValidPath(processor.getPath());
 		builder.append(validModulePath).append(validProcessorPath);
 		return builder.toString();
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public <T extends IRequest, R extends IResponse> IProcessor<T, R> getProcessor(final String path) {
 		return (IProcessor<T, R>)this.repository.get(path);
 	}
 }
