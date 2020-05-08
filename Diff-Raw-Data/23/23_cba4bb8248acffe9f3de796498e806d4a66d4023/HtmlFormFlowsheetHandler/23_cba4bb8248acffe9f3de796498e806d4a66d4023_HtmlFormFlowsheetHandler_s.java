 package org.openmrs.module.htmlformflowsheet.handlers;
 
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Concept;
 import org.openmrs.Form;
 import org.openmrs.Patient;
 import org.openmrs.module.htmlformentry.FormEntryContext.Mode;
 import org.openmrs.module.htmlformentry.FormEntrySession;
 import org.openmrs.module.htmlformentry.handler.AbstractTagHandler;
 import org.openmrs.module.htmlformentry.handler.AttributeDescriptor;
 import org.openmrs.module.htmlformflowsheet.HtmlFormFlowsheetUtil;
 import org.openmrs.web.WebConstants;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 
 /**
  * The tag handler for rendering an htmlform within a parent htmlform.
  * 
  * POSSIBLE ATTRIBUTES:
  * formId (required) -- expects the real form Id, not the htmlform ID.  Describes the form to render.
  * sharedEncounter (optional, default = 'false') -- loads all encounters of the same EncounterType, rather than default behavior,
  *              which is to load only Encounters mapped to the form given in formId.  Also, this will cause 
  *              a drop-down to be appended to the display that allows you to select an encounter to append to
  *              when performing a new entry.
  * addAnotherButtonLabel (optional, default = 'Add Another') -- changes the text label for the 'Add Another' button.
  * windowHeight (optional, default = 400) -- allows you to specify the height of the pop-up that you get when
  *              you click the 'Add New' button.
  * showHtmlForm (optional, default = 'false') If you set this to 'true', the htmlformflowsheet default table view of form observations
  *              will be substituted with a Mode.VIEW version of the rendered htmlform itself for each encounter in the table. 
  * readOnly (optional, default = 'false') -- if true, will render the htmlformflowsheet, but without the 
  *              'Add Another' button, or edit links.  in htmlformentry VIEW mode, this is always false.
  * showProvider (optional) -- show an additional column that shows the encounter provider name next to the date column. The value of 
  * 				attribute will be used as the display header for the provider column.
  */
 public class HtmlFormFlowsheetHandler extends AbstractTagHandler {
 
     /** The logger to use with this class */
     protected final Log log = LogFactory.getLog(getClass());
 
     protected List<AttributeDescriptor> createAttributeDescriptors() {
 		List<AttributeDescriptor> attributeDescriptors = new ArrayList<AttributeDescriptor>();
 		attributeDescriptors.add(new AttributeDescriptor("formId", Form.class));
 		attributeDescriptors.add(new AttributeDescriptor("conceptsToShow", Concept.class));
 		return Collections.unmodifiableList(attributeDescriptors);
 	}
     
     public boolean doStartTag(FormEntrySession session, PrintWriter out, Node parent, Node node) {
 
 		Map<String, String> attributes = new HashMap<String, String>();
         NamedNodeMap map = node.getAttributes();
         for (int i = 0; i < map.getLength(); ++i) {
             Node attribute = map.item(i);
             attributes.put(attribute.getNodeName(), attribute.getNodeValue());
         }
 
         Patient patient = session.getPatient();
 
         if (patient != null) {
 
 			// Retrieve and validate the form id that this is loading
 			String formId = null;
 			String formIdAtt = attributes.get("formId");
 			if (StringUtils.isEmpty(formIdAtt)) {
 				throw new IllegalArgumentException("You must specify a 'formId' attribute in your htmlformflowsheet tag");
 			}
 			try {
 				Form form = HtmlFormFlowsheetUtil.getFormFromString(formIdAtt);
 				formId = HtmlFormFlowsheetUtil.getFormIdAsString(form);
 			}
 			catch (Exception e) {
 				throw new IllegalArgumentException("The 'formId' attribute in your htmlformflowsheet tag is invalid", e);
 			}
 
 			// Construct the portlet to load based on the input attributes
 			StringBuilder source = new StringBuilder();
 			source.append("/"+WebConstants.WEBAPP_NAME+"/module/htmlformflowsheet/patientWidgetChart.list?patientId=" + patient.getPatientId());
 			source.append("&fullPage=false&configuration=F:BOO:" + formId);
 
 			String readOnly = attributes.get("readOnly");
 			if (StringUtils.isEmpty(readOnly)) {
 				readOnly = Boolean.toString(session.getContext().getMode().equals(Mode.VIEW));
 			}
 			source.append("&readOnly=" + readOnly);
 
 			String showHtmlForm = Boolean.toString("true".equals(attributes.get("showHtmlForm")));
 			source.append("&showHtmlForm=" + showHtmlForm);
 
 			String windowHeight = attributes.get("windowHeight");
 			source.append("&windowHeight=" + windowHeight);
 
 			if ("true".equals(attributes.get("sharedEncounter"))) {
 				source.append("&showAllEncsWithEncType=true");
 			}
 
 			boolean showProvider = attributes.get("showProvider") != null && !"false".equals(attributes.get("showProvider"));
 			if (showProvider) {
 				source.append("&showProvider=true" + showProvider);
 				if (StringUtils.isNotEmpty(attributes.get("providerHeader"))) {
 					source.append("&providerHeader=" + attributes.get("providerHeader"));
 				}
 			}
 
 			String addAnotherButtonLabel = attributes.get("addAnotherButtonLabel");
 			if (StringUtils.isEmpty(addAnotherButtonLabel)) {
 				addAnotherButtonLabel = attributes.get("addNewButtonLabel"); // TODO: Remove this when possible
 			}
 			if (StringUtils.isNotEmpty(addAnotherButtonLabel)) {
 				try {
 					addAnotherButtonLabel = URLEncoder.encode(addAnotherButtonLabel, "UTF-8");;
 				}
 				catch (Exception e) {
 					throw new IllegalArgumentException("The value of '" + addAnotherButtonLabel + "' is invalid", e);
 				}
 				source.append("&addAnotherButtonLabel="+addAnotherButtonLabel);
 			}
 
 			String conceptsToShow = attributes.get("conceptsToShow");
 			if (StringUtils.isNotEmpty(conceptsToShow)) {
 				source.append("&conceptsToShow=" + conceptsToShow);
 			}
 
 			StringBuilder sb = new StringBuilder();
 			sb.append("<iframe id='iframeFor" + formId + "' name='iframeFor" + formId + "'");
			sb.append(" src='/"+WebConstants.WEBAPP_NAME+"/moduleResources/htmlformflowsheet/pleaseWait.htm'  ");
			sb.append(" width='100%' frameborder='0' scrolling='no'></iframe><br/><script>window.frames['iframeFor" + formId + "'].innerHTML = 'please wait...'; \n function iframe"+formId+"(){window.frames['iframeFor" + formId + "'].location = '"+source+"';} \n setTimeout('iframe"+formId+"();', 1);</script>");
             out.print(sb.toString());
             
         } else {
             out.print("<div>You must create the patient first!</div>");
         }
         
         return true;
     }
 
     public void doEndTag(FormEntrySession session, PrintWriter out, Node parent, Node node) {
     }
 }
