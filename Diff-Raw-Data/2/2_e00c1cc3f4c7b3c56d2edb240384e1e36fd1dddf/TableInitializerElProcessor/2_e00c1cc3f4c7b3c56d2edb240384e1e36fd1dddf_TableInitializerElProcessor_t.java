 package com.github.dandelion.datatables.thymeleaf.processor;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.thymeleaf.Arguments;
 import org.thymeleaf.context.IWebContext;
 import org.thymeleaf.dom.Element;
 import org.thymeleaf.processor.IElementNameProcessorMatcher;
 import org.thymeleaf.processor.ProcessorResult;
 import org.thymeleaf.processor.element.AbstractElementProcessor;
 
 import com.github.dandelion.datatables.core.exception.BadConfigurationException;
 import com.github.dandelion.datatables.core.model.HtmlTable;
 import com.github.dandelion.datatables.core.properties.PropertiesLoader;
 import com.github.dandelion.datatables.core.util.RequestHelper;
 import com.github.dandelion.datatables.core.util.ResourceHelper;
 import com.github.dandelion.datatables.thymeleaf.dialect.DataTablesDialect;
 
 /**
  * <p>
  * Element processor applied to the HTML <tt>table</tt> tag.
  * 
  * @author Thibault Duchateau
  */
 public class TableInitializerElProcessor extends AbstractElementProcessor {
 
 	// Logger
 	private static Logger logger = LoggerFactory.getLogger(TableInitializerElProcessor.class);
 
 	public TableInitializerElProcessor(IElementNameProcessorMatcher matcher) {
 		super(matcher);
 	}
 
 	@Override
 	public int getPrecedence() {
 		return DataTablesDialect.DT_HIGHEST_PRECEDENCE;
 	}
 
 	@Override
 	protected ProcessorResult processElement(Arguments arguments, Element element) {
 		String tableId = element.getAttributeValue("id");
 		logger.debug("{} element found with id {}", element.getNormalizedName(), tableId);
 
 		HttpServletRequest request = ((IWebContext) arguments.getContext()).getHttpServletRequest();
 
 		if (tableId == null) {
 			logger.error("The 'id' attribute is required.");
 			throw new IllegalArgumentException();
 		} else {
 			HtmlTable htmlTable = new HtmlTable(tableId, ResourceHelper.getRamdomNumber());
 			htmlTable.setCurrentUrl(RequestHelper.getCurrentUrl(request));
 
 			try {
 				// Load table properties
 				PropertiesLoader.load(htmlTable);
 			} catch (BadConfigurationException e) {
 				logger.error("Unable to load Dandelion-datatables configuration");
 				e.printStackTrace();
 			}
 
 			// Add default footer and header row
 			htmlTable.addHeaderRow();
 			htmlTable.addFooterRow();
 
 			// Add a "finalizing div" after the HTML table tag in order to
 			// finalize the Dandelion-datatables configuration generation
 			// The div will be removed in its processor
 			Element div = new Element("div");
 			div.setAttribute(DataTablesDialect.DIALECT_PREFIX + ":tmp", "internalUse");
 			div.setRecomputeProcessorsImmediately(true);
 			element.getParent().insertAfter(element, div);
 
 			// Store the htmlTable POJO as a request attribute, so all the
 			// others following HTML tags can access it and particularly the
 			// "finalizing div"
 			((IWebContext) arguments.getContext()).getHttpServletRequest().setAttribute(
 					"htmlTable", htmlTable);
 
 			// The table node is also saved in the request, to be easily accessed later
 			((IWebContext) arguments.getContext()).getHttpServletRequest().setAttribute(
 					"tableNode", element);
 			
 			// Don't forget to remove the attribute
 			element.removeAttribute(DataTablesDialect.DIALECT_PREFIX + ":table");
 
 			return ProcessorResult.OK;
 		}
 	}
 }
