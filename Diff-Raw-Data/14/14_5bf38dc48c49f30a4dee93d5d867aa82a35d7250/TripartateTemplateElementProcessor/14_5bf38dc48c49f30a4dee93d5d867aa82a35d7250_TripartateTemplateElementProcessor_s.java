 package com.emergentideas.webhandle.templates;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.emergentideas.logging.Logger;
 import com.emergentideas.logging.SystemOutLogger;
 import com.emergentideas.webhandle.AppLocation;
 import com.emergentideas.webhandle.Location;
 import com.emergentideas.webhandle.WebAppLocation;
 import com.emergentideas.webhandle.output.SegmentedOutput;
 
 public class TripartateTemplateElementProcessor implements ElementProcessor {
 
 	protected Logger logger = SystemOutLogger.get(TripartateTemplateElementProcessor.class);
 	protected ExpressionFactory expressionFactory;
 	
 	protected static final String TEMPLATE_SOURCE_NAME = "TEMPLATE_SOURCE";
 	
 	public TripartateTemplateElementProcessor(ExpressionFactory expressionFactory) {
 		this.expressionFactory = expressionFactory;
 	}
 	
 	public boolean process(Location location, SegmentedOutput output, Element element, String elementSourceName, String... processingHints) {
 		if(element instanceof TripartateElement) {
 			TripartateElement te = (TripartateElement)element;
 			
 			
 			if(shouldRunTemplateBasedOnConditional(location, te) == false) {
 				// So, we've decided there's nothing we should do, but this is the sort element we should
 				// handle and did handle it (we just decided no output was required) so we'll return true
 				return true;
 			}
 			
 			TemplateInstance ti = null;
 			String templateName = null;
 			String templateCountName = null;
 			
 			templateName = determineTemplateName(location, te);
 			if(templateName != null) {
 				// create the iteration count variable name
 				templateCountName = templateName.replace('/', '_') + "_count";
 				
 				TemplateSource ts = (TemplateSource)output.getObject(TEMPLATE_SOURCE_NAME);
 				if(ts == null) {
 					ts = new WebAppLocation(location).getTemplateSource();
 					output.setObject(TEMPLATE_SOURCE_NAME, ts);
 				}
 				
 				ti = ts.get(templateName);
 				if(ti == null) {
 					logger.error("Could not find template named: " + templateName);
 					return true;
 				}
 			}
 			
 			String dataSelector = te.getDataSelectorExpression();
 			if(StringUtils.isBlank(dataSelector)) {
 				if(ti != null) {
 					ti.render(output, location, elementSourceName, null);
 				}
 			}
 			else {
 				Object result = determineSelectedObjects(location, te);
 				if(result != null) {
 					Collection<Object> data;
 					if(result instanceof Collection) {
 						data = (Collection<Object>)result;
 					}
 					else {
 						data = new ArrayList<Object>();
 						data.add(result);
 					}
 					
 					Iterator<Object> it = data.iterator();
 					int dataSize = data.size();
 					for(int i = 0; i < dataSize; i++) {
 						Object o = it.next();
 						if(ti != null) {
 							Location callLocation = new AppLocation(location);
 							callLocation.add(o);
 							if(templateCountName != null) {
 								callLocation.put(templateCountName, i);
 							}
 							
 							if(dataSize > 1) {
 								callLocation.put("has_next", i < (dataSize - 1));
 								callLocation.put("is_first", i == 0);
 								callLocation.put("is_last", !(i < (dataSize - 1)));
 								callLocation.put("current_count", i);
 							}
 							
 							ti.render(output, callLocation, elementSourceName, null);
 						}
 						else {
 							output.getStream(elementSourceName).append(o.toString());
 						}
 					}
 				}
 			}
 			
 			return true;
 		}
 		return false;
 	}
 	
 	protected Object determineSelectedObjects(Location location, TripartateElement te) {
 		if(StringUtils.isBlank(te.getDataSelectorExpression())) {
 			return null;
 		}
 		else {
 			Object result = evaluateExpression(location, te.getDataSelectorExpression());
 			return result;
 		}
 	}
 	
 	/**
 	 * Determine the which template should be used to process this data.  If prefixed with 
 	 * a $ we'll run the rest of the template name as an expression.  If not prefixed with
 	 * a $, we'll assume this is the name of a template.
 	 * @param location
 	 * @param te
 	 * @return
 	 */
 	protected String determineTemplateName(Location location, TripartateElement te) {
 		String templateName = null;
 		if(te.getHandlingExpression() != null) {
 			if(te.getHandlingExpression().startsWith("$")) {
 				// if the template name starts with a $ that means we should try to look up the template name 
 				// using using the following string as a location
 				Object objTemplateName = evaluateExpression(location, te.getHandlingExpression().substring(1));
 				if(objTemplateName == null) {
 					templateName = null;
 				}
 				
 				if(objTemplateName instanceof Collection) {
 					Iterator it = ((Collection)objTemplateName).iterator();
 					if(it.hasNext()) {
 						objTemplateName = it.next();
 					}
 					else {
 						return null;
 					}
 				}
 
 				if(objTemplateName instanceof String) {
 					templateName = (String)objTemplateName;
 				}
 				else {
 					templateName = objTemplateName.toString();
 				}
 			}
 			else {
 				templateName = te.getHandlingExpression();
 			}
 		}
 		
 		return templateName;
 	}
 	
 	/**
 	 * Takes an expression and evaluates it to determine a return value
 	 * @param location
 	 * @param expression
 	 * @return
 	 */
 	protected Object evaluateExpression(Location location, String expression) {
 		if(expressionFactory != null) {
 			Expression exp = expressionFactory.createExpression(expression);
 			Object o = exp.evaluate(location);
 			return o;
 		}
 		else {
 			throw new NullPointerException();
 //			return location.all(expression);
 		}
 	}
 	
 	/**
 	 * Returns true if the conditional expression is blank or evaluates to a true value which indicates
 	 * this template should be run.
 	 * @param location
 	 * @param expression
 	 * @return
 	 */
 	protected boolean shouldRunTemplateBasedOnConditional(Location location, TripartateElement expression) {
 		if(StringUtils.isBlank(expression.getConditionalExpression())) {
 			return true;
 		}
 		
 		Object o = evaluateExpression(location, expression.getConditionalExpression());
 		return shouldRunForConditionalExpression(o);
 	}
 	
 	protected boolean shouldRunForConditionalExpression(Object o) {
 		if(o == null) {
 			return false;
 		}
 		
 		if(o instanceof Boolean) {
 			return ((Boolean)o).booleanValue();
 		}
 		
 		return shouldRunForDataExpression(o);
 	}
 	
 	protected boolean shouldRunForDataExpression(Object o) {
 		if(o == null) {
 			return false;
 		}
 		
 		if(o instanceof String) {
 			if(StringUtils.isBlank((String)o)) {
 				return false;
 			}
 		}
 		
 		if(o instanceof Collection<?>) {
 			return ((Collection<?>)o).size() != 0;
 		}
 		
 		return true;
 	}
 
 }
