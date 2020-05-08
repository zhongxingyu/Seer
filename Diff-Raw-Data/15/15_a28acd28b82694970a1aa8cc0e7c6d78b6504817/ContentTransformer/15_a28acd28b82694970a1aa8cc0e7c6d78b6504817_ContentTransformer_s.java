 package com.gentics.cr.lucene.indexer.transformer;
 
 import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 //import org.apache.lucene.index.IndexWriter;
 
 import com.gentics.api.lib.exception.ParserException;
 import com.gentics.api.lib.expressionparser.Expression;
 import com.gentics.api.lib.expressionparser.ExpressionEvaluator;
 import com.gentics.api.lib.expressionparser.ExpressionParser;
 import com.gentics.api.lib.expressionparser.ExpressionParserException;
 import com.gentics.cr.CRResolvableBean;
 import com.gentics.cr.configuration.GenericConfiguration;
 import com.gentics.cr.exceptions.CRException;
 import com.gentics.cr.monitoring.MonitorFactory;
 import com.gentics.cr.monitoring.UseCase;
 
 /**
  * 
  * Last changed: $Date: 2009-06-24 17:10:19 +0200 (Mi, 24 Jun 2009) $
  * @version $Revision: 99 $
  * @author $Author: supnig@constantinopel.at $
  *
  */
 public abstract class ContentTransformer {
 	protected static final Logger LOGGER = Logger.getLogger(ContentTransformer.class);
 	private static ExpressionEvaluator evaluator = new ExpressionEvaluator();
 	private Expression expr;
 	private String rule;
 
 	private static final String TRANSFORMER_RULE_KEY = "rule";
 	private static final String DEFAULT_TRANSFORMER_RULE = "1==1";
 
 	private String transformerkey = "";
 
 	/**
 	 * Parameters for the ContentTransformer which can be used for processing (e.g. the request could be set).
 	 */
 	protected ConcurrentHashMap<String, Object> parameters = new ConcurrentHashMap<String, Object>();
 
 	/**
 	 * Gets the Transformerkey of current Transformer.
 	 */
 	public String getTransformerKey() {
 		return transformerkey;
 	}
 
 	protected void setTransformerkey(String key) {
 		this.transformerkey = key;
 	}
 
 	protected ContentTransformer(GenericConfiguration config) {
 		rule = (String) config.get(TRANSFORMER_RULE_KEY);
 		if (rule == null || "".equals(rule)) {
 			rule = DEFAULT_TRANSFORMER_RULE;
 		}
 		try {
 			expr = ExpressionParser.getInstance().parse(rule);
 		} catch (ParserException e) {
 			LOGGER.error("Could not generate valid Expression from configured Rule: " + rule, e);
 		}
 	}
	
 	public static Logger getLogger() {
 		return LOGGER;
 	}
 
 	/**
 	 * Destroys the transformer.
 	 */
 	public abstract void destroy();
 
 	/**
 	 * Process the specified bean with monitoring.
 	 * @param bean
 	 * @throws CRException
 	 */
 	public void processBeanWithMonitoring(CRResolvableBean bean) throws CRException {
 		UseCase pcase = MonitorFactory.startUseCase("Transformer:" + this.getClass());
 		try {
 			processBean(bean);
 		} finally {
 			pcase.stop();
 		}
 	}
 
 	/**
 	 * Processes the specified bean.
 	 * @param bean
 	 * @throws CRException throws exception if bean could not be processed
 	 */
 	public abstract void processBean(CRResolvableBean bean) throws CRException;
 
 	/**
 	 * Tests if the specified CRResolvableBean should be processed by the transformer.
 	 * @param object
 	 * @return true if rule matches
 	 */
 	public boolean match(CRResolvableBean object) {
 		if (object != null) {
 			try {
 				return (evaluator.match(expr, object));
 			} catch (ExpressionParserException e) {
 				LOGGER.error("Could not evaluate Expression with gived object and rule: " + rule, e);
 			}
 		}
 		return (false);
 	}
 
 	private static final String TRANSFORMER_CLASS_KEY = "transformerclass";
 	private static final String TRANSFORMER_KEY = "transformer";
 
 	/**
 	 * @param config - configuration containing the definition of the transformers
 	 * @return List of ContentTransformer defined in the confguration.
 	 */
 	public static List<ContentTransformer> getTransformerList(final GenericConfiguration config) {
 		GenericConfiguration tconf = (GenericConfiguration) config.get(TRANSFORMER_KEY);
 		if (tconf != null) {
 			Map<String, GenericConfiguration> confs = tconf.getSortedSubconfigs();
 			if (confs != null && confs.size() > 0) {
 				ArrayList<ContentTransformer> ret = new ArrayList<ContentTransformer>(confs.size());
 				for (Map.Entry<String, GenericConfiguration> e : confs.entrySet()) {
 					GenericConfiguration c = e.getValue();
 					String transformerClass = (String) c.get(TRANSFORMER_CLASS_KEY);
 					try {
 						ContentTransformer t = null;
						t = (ContentTransformer) Class.forName(transformerClass)
								.getConstructor(new Class[] { GenericConfiguration.class }).newInstance(c);
 						if (t != null) {
 							t.setTransformerkey(e.getKey());
 							ret.add(t);
 						}
 					} catch (Exception ex) {
						LOGGER.error("Invalid configuration found. Could not instantiate " + transformerClass);
						ex.printStackTrace();
 					}
 
 				}
 				return (ret);
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * Allow to set parameters.
 	 * @param key parameter key
 	 * @param value value of the parameter
 	 */
 	public final void setParameter(final String key, final Object value) {
 		parameters.put(key, value);
 	}
 
 	/**
 	 * Get a parameter from the parameters ConcurrentHashMap.
 	 * @param key Key used for retrieval.
 	 * @return Value stored as an object
 	 */
 	public final Object getParameter(final String key) {
 		return parameters.get(key);
 	}
 
 	/**
 	 * @param key Key used for retrieval.
 	 * @return Value stored as an object
 	 */
 	public final Object get(final String key) {
 		return getParameter(key);
 	}
 }
