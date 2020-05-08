 package org.openmrs.module.drawing.elements;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Concept;
 import org.openmrs.Obs;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.drawing.AnnotatedImage;
 import org.openmrs.module.drawing.DrawingUtil;
 import org.openmrs.module.drawing.ImageAnnotation;
 import org.openmrs.module.htmlformentry.FormEntryContext;
 import org.openmrs.module.htmlformentry.FormEntryContext.Mode;
 import org.openmrs.module.htmlformentry.FormEntrySession;
 import org.openmrs.module.htmlformentry.FormSubmissionError;
 import org.openmrs.module.htmlformentry.HtmlFormEntryUtil;
 import org.openmrs.module.htmlformentry.action.FormSubmissionControllerAction;
 import org.openmrs.module.htmlformentry.element.HtmlGeneratorElement;
 import org.openmrs.obs.ComplexData;
 import org.openmrs.web.WebConstants;
 
 public class DrawingSubmissionElement implements HtmlGeneratorElement, FormSubmissionControllerAction {
 	
 	private static final Log log = LogFactory.getLog(DrawingSubmissionElement.class);
 	
 	private String id;
 	
 	private Concept questionConcept;
 	
 	private Obs existingObs;
 	
 	public DrawingSubmissionElement(FormEntryContext context, Map<String, String> parameters) {
 		String questionConceptId = parameters.get("questionConceptId");
 		id = parameters.get("id");
 		
 		if (StringUtils.isBlank(questionConceptId))
 			throw new RuntimeException("questionConceptId cannot be empty");
 		else if (StringUtils.isBlank(id))
 			throw new RuntimeException("id cannot be empty");
 		
 		questionConcept = HtmlFormEntryUtil.getConcept(questionConceptId);
 		if (questionConcept == null)
 			throw new IllegalArgumentException("Cannot find concept for value " + questionConceptId
 			        + " in conceptId attribute value. Parameters: " + parameters);
 		Concept c = null;
 		Obs o = context.removeExistingObs(questionConcept, c);
 		if (o != null)
 			existingObs = Context.getObsService().getComplexObs(o.getId(), "");
 		
 	}
 	
 	@Override
 	public Collection<FormSubmissionError> validateSubmission(FormEntryContext context, HttpServletRequest submission) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	@Override
 	public void handleSubmission(FormEntrySession session, HttpServletRequest submission) {
 		String encodedImage = submission.getParameter("encodedImage" + id);
 		if (!StringUtils.isNotBlank(encodedImage) && encodedImage.contains(","))
 			throw new RuntimeException("Image not encoded in proper format");
 		
 		try {
 			AnnotatedImage ai = new AnnotatedImage(DrawingUtil.base64ToImage(encodedImage));
 			ai.setAnnotations(DrawingUtil.getAnnotations(submission, id));
 			if (session.getContext().getMode() == Mode.EDIT && existingObs != null)
 				session.getSubmissionActions().modifyObs(existingObs, questionConcept,
 				    new ComplexData(existingObs.getComplexData().getTitle(), ai), null, null);
 			else
 				session.getSubmissionActions().createObs(questionConcept, new ComplexData("drawingObs.png", ai), null, null);
 			
 		}
 		catch (Exception e) {
 			log.error("cannot create obs :" + e.getMessage(), e);
 			throw new RuntimeException("Unable to save complex Observation!");
 		}
 	}
 	
 	@Override
 	public String generateHtml(FormEntryContext context) {
 		StringBuilder sb = new StringBuilder();
 		
 		if (context.getMode().equals(Mode.VIEW) && existingObs != null) {
 			Obs complexObs = Context.getObsService().getComplexObs(existingObs.getId(), "");
 			AnnotatedImage ai = (AnnotatedImage) existingObs.getComplexData().getData();
 			String encodedImage = null;
 			try {
 				encodedImage = DrawingUtil.imageToBase64(ai.getImage());
 			}
 			catch (IOException e) {
 				log.error("unable to encode image to Base64 format", e);
 			}
 			sb.append("<link rel='stylesheet' media='screen' type='text/css' href='/" + WebConstants.WEBAPP_NAME
 			        + "/moduleResources/drawing/drawingHtmlForm.css' />");
 			sb.append("<script type='text/javascript'>var ancount"
 			        + id
 			        + "=0;    var redDot ='/openmrstru/moduleResources/drawing/images/red-dot.png';var close = '/openmrstru/moduleResources/drawing/close.gif';function createMarker"
 			        + id
 			        + "(identification, x, y, text, stat) {var annotationId = \"marker"
 			        + id
 			        + "\" + ancount"
 			        + id
 			        + ";ancount"
 			        + id
 			        + "++;var annDivData = \"<img src='\" + close + \"' style='float:right' onClick='$j(this).parent().parent().fadeOut(500)'/><span style='background-color:white'>\" + text + \"</span></br>\";var v = '<div class=\"container\"><div style=\"position:absolute;z-index:5;display:none\"><div id=\"' + annotationId + '_data\" class=\"divContainerDown\">' + annDivData + '</div><div class=\"calloutDown\"><div class=\"calloutDown2\"></div></div></div>';$j('#canvasDiv"
 			        + id
 			        + "').append(v + '<img id=\"' + annotationId + '\" src=\"' + redDot + '\" style=\"top:' + y + 'px;left:' + x + 'px;position:absolute;z-index:4\"/></div>');$j('#' + annotationId).click(function(event) {$j('#' + annotationId + '_data').parent().css('top', event.pageY-$j('#canvasDiv"
 			        + id
 			        + "').offset().top - $j('#' + annotationId + '_data').parent().height());$j('#' + annotationId + '_data').parent().css('left', event.pageX-$j('#canvasDiv"
 			        + id
 			        + "').offset().left - $j('#' + annotationId + '_data').parent().width() / 8 - 5);$j('#' + annotationId + '_data').parent().show();});}");
 			
 			sb.append("$j(document).ready(function(){");
 			for (ImageAnnotation annotation : ai.getAnnotations())
 				sb.append("createMarker" + id + "(" + annotation.getId() + "," + annotation.getLocation().getX() + ","
 				        + annotation.getLocation().getY() + ",'" + annotation.getText() + "','" + annotation.getStatus()
 				        + "');");
 			sb.append("})</script>");
 			sb.append("<h4>" + complexObs.getConcept().getName(Context.getLocale()).getName() + "</h4>");
 			sb.append("<div id='canvasDiv" + id + "' style='position:relative'><img  src='" + encodedImage + "'></div>");
 			
 			//sb.append(complexData.getData());
 		} else if (context.getMode() == Mode.EDIT || context.getMode() == Mode.ENTER) {
 			sb.append("<link rel='stylesheet' type='text/css' href='/" + WebConstants.WEBAPP_NAME
 			        + "/moduleResources/drawing/paint.css' />");
 			sb.append("<link rel='stylesheet' media='screen' type='text/css' href='/" + WebConstants.WEBAPP_NAME
 			        + "/moduleResources/drawing/colorpicker.css' />");
 			sb.append("<script type='text/javascript' src='/" + WebConstants.WEBAPP_NAME
 			        + "/moduleResources/drawing/paint.js'></script>");
 			sb.append("<script type='text/javascript' src='/" + WebConstants.WEBAPP_NAME
 			        + "/moduleResources/drawing/colorpicker.js'></script>");
 			sb.append("<script type='text/javascript'>$j(document).ready(function(){var v" + id + "=new DrawingEditor('"
 			        + id + "');v" + id + ".prepareCanvas();");
 			
 			if (context.getMode() == Mode.EDIT && existingObs != null && existingObs.isComplex()
 			        && existingObs.getComplexData() != null) {
 				AnnotatedImage ai = (AnnotatedImage) existingObs.getComplexData().getData();
 				
 				for (ImageAnnotation annotation : ai.getAnnotations())
 					sb.append("v" + id + ".createMarker(" + annotation.getId() + "," + annotation.getLocation().getX() + ","
 					        + annotation.getLocation().getY() + ",'" + annotation.getText() + "','" + annotation.getStatus()
 					        + "');");
 				String encodedImage = null;
 				try {
 					encodedImage = DrawingUtil.imageToBase64(ai.getImage());
 				}
 				catch (IOException e) {
 					log.error("unable to encode image to Base64 format", e);
 				}
 				if (StringUtils.isNotEmpty(encodedImage))
 					sb.append("v" + id + ".loadExistingImage('" + encodedImage + "');");
 				
 			}
 			
 			sb.append("v" + id + ".setSubmit(false);v" + id + ".setFormId('htmlform');}); </script>");
 			sb.append("<div class='editorContainer'>");
 			sb.append("<div id='drawingHeader'>");
 			sb.append("<div id='cursorDiv" + id + "' class='iconDiv'><img id='cursor' src='/" + WebConstants.WEBAPP_NAME
 			        + "/moduleResources/drawing/images/cursor_icon.png' alt='cursor' class='imageprop' /></div>");
 			
 			sb.append("<div id='pencilDiv" + id + "' class='iconDiv'><img id='pencil' src='/" + WebConstants.WEBAPP_NAME
 			        + "/moduleResources/drawing/images/pencil_icon.png' alt='pencil' class='imageprop' /></div>");
 			sb.append("<div id='eraserDiv" + id + "' class='iconDiv'><img id='eraser' src='/" + WebConstants.WEBAPP_NAME
 			        + "/moduleResources/drawing/images/eraser_icon.png' alt='eraser' class='imageprop' /></div>");
 			sb.append("<div id='textDiv" + id + "' class='iconDiv'><img id='text' src='/" + WebConstants.WEBAPP_NAME
 			        + "/moduleResources/drawing/images/text_icon.png' alt='text' class='imageprop' /></div>");
 			sb.append("<div id='fontpropertiesDiv"
 			        + id
 			        + "' style='display: none;float: left;margin-left: 5px' ><div id='boldDiv"
 			        + id
 			        + "' class='iconDiv'><img src='/"
 			        + WebConstants.WEBAPP_NAME
 			        + "/moduleResources/drawing/images/bold_icon.png' alt='bold' class='imageprop' /></div><div id='italicDiv"
 			        + id
 			        + "' class='iconDiv'><img src='/"
 			        + WebConstants.WEBAPP_NAME
			        + "/moduleResources/drawing/images/italic_icon.png' alt='italic'  class='imageprop'/></div><div class='selection'>Font Size:<select id='fontSize"
 			        + id
			        + "'><option>24</option><option>28</option><option>32</option><option>38</option></select></div></div>");
			sb.append("<div class='selection'><div id='thicknessDiv" + id
			        + "' style='display: none;float: left;margin-left: 5px' >Thickness:<select id='thickness" + id
			        + "'><option>2</option><option>4</option><option>6</option><option>8</option></select></div></div>");
 			sb.append("<div id='colorSelector" + id
 			        + "' style='float: left' class='colorselector'><div class='colorselector_innerdiv'></div></div>");
 			sb.append("<div style='clear:both;'></div>");
 			sb.append("</div>");
 			sb.append("<div id='canvasDiv" + id + "' class='canvasDiv'></div>");
 			sb.append("<div id='textAreaPopUp" + id
 			        + "' style='position:absolute;display:none;z-index:1;'><textarea id='writableTextarea" + id
 			        + "' style='width:100px;height:50px;'></textarea><input type='button' value='save' id='saveText" + id
 			        + "'></div>");
 			sb.append("<div id='drawingFooter'><input type='button' id='clearCanvas" + id
 			        + "' value='Clear canvas' /><input type='button' id='saveImage" + id + "' value='Done Drawing'/>");
 			sb.append("<input type='hidden' id='encodedImage" + id + "' name='encodedImage" + id + "' ");
 			sb.append("/><input type='file' id='imageUpload" + id + "' value='Open Image' /><span id='saveNotification" + id
 			        + "' style='display:none;color:#ffffff;float:right'>DRAWING SAVED</span> </div>");
 			sb.append("</div>");
 		}
 		return sb.toString();
 	}
 	
 }
