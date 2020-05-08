 package org.jcrop;
 
 import org.apache.wicket.Application;
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.markup.head.CssHeaderItem;
 import org.apache.wicket.markup.head.IHeaderResponse;
 import org.apache.wicket.markup.head.JavaScriptHeaderItem;
 import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
 import org.apache.wicket.request.cycle.RequestCycle;
 import org.apache.wicket.request.resource.PackageResourceReference;
 import org.apache.wicket.util.template.PackageTextTemplate;
 import org.jcrop.utils.JsFunction;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 
 public class JcropBehavior extends AbstractDefaultAjaxBehavior {
 
     public JcropBehavior() {
         this.settings = new CroppableSettings();
     }
 
     public JcropBehavior(CroppableSettings settings) {
         if (null == settings) {
             throw new IllegalArgumentException("Croppable settings shouldn't be null");
         }
         this.settings = settings;
     }
 
     public JcropBehavior(CroppableSettings settings, boolean useDefaultJsImplementation) {
         if (null == settings) {
             throw new IllegalArgumentException("Croppable settings shouldn't be null");
         }
         this.settings = settings;
         provideDefaultJsImplementation = useDefaultJsImplementation;
     }
 
     protected void provideDefaultJsImplementation(String wicketId) {
         if (settings.isShowingPreview()) {
 
             PreviewSettings preview = settings.getPreview();
             JsFunction previewJsFunction = CroppableImageHelper.getDefaultPreviewFunction(
                     wicketId,
                     getProportionVariableName(wicketId),
                     preview.getPreviewDivName(),
                     preview.getPreviewWidth(),
                     preview.getPreviewHeight()
             );
             settings.putOnChangeCallback(previewJsFunction);
         }
        settings.putOnSelectCallback(CroppableImageHelper.getDefaulOnSelectFunction());
     }
 
     protected String getProportionVariableName(String markupId) {
         return markupId + "_jcrop_image_proportion";
     }
 
     @Override
     public void renderHead(Component component, IHeaderResponse response) {
         super.renderHead(component, response);
         attachResources(response);
 
         if (provideDefaultJsImplementation) {
             provideDefaultJsImplementation(component.getMarkupId());
         }
 
         String generatedScript = generateInitJs(component);
         response.render(OnDomReadyHeaderItem.forScript(generatedScript));
     }
 
     /**
      * if you want provide newer version of Jcrop override this method
      * @param response
      */
     protected void attachResources(IHeaderResponse response) {
         response.render(JavaScriptHeaderItem.forReference(Application.get().getJavaScriptLibrarySettings().getJQueryReference()));
         response.render(CssHeaderItem.forReference(new PackageResourceReference(JcropBehavior.class, "jquery.Jcrop.css")));
         if (Application.get().usesDevelopmentConfig()) {
             response.render(JavaScriptHeaderItem.forReference(new PackageResourceReference(JcropBehavior.class, "jquery.Jcrop.js")));
         } else {
             response.render(JavaScriptHeaderItem.forReference(new PackageResourceReference(JcropBehavior.class, "jquery.Jcrop.min.js")));
         }
     }
 
     protected String generateInitJs(Component component) {
         String appendPreviewImage = "";
         if (settings.isShowingPreview()) {
             PreviewSettings preview = settings.getPreview();
 
             appendPreviewImage = String.format("var imageSrc = $('#%s').attr('src'); " +
                     "$('#%s').append('<img id=\"previewImg\" src=\"' + imageSrc + '\" />').css({'width': '%dpx', 'height':'%dpx', 'overflow': 'hidden'});"
                     , component.getMarkupId(), preview.getPreviewDivName(), preview.getPreviewWidth(), preview.getPreviewHeight());
         }
         Map<String, String> valuesForTemplate = new HashMap<String, String>();
         valuesForTemplate.put("appendPreviewImage", appendPreviewImage);
         valuesForTemplate.put("bodyForJsFunction", renderBodyOfJsFunctions());
         valuesForTemplate.put("markupId", component.getMarkupId());
         valuesForTemplate.put("settings", settings.generateSettings());
         valuesForTemplate.put("renderApiController", renderApiController(component.getMarkupId()));
 
        String result = new PackageTextTemplate(JcropBehavior.class, "InitScript.template").asString(valuesForTemplate);
        return result;
     }
 
     private String renderApiController(String markupIp) {
         String result = "";
         if (settings.isProvideApiController()) {
           String apiControllerVariableName = "window.jcrop_api_" + markupIp;
           apiController = new JcropController(apiControllerVariableName);
           return ", function() {"+apiControllerVariableName+"=this}";
         }
         return result;
     }
 
     protected String renderBodyOfJsFunctions() {
         StringBuilder result = new StringBuilder();
         result.append(renderBodyOfAdditionalFunctions());
         result.append(renderBodyOfMainFunctions());
         return result.toString();
     }
 
     protected String renderBodyOfAdditionalFunctions() {
         List<JsFunction> jsFunctionsBodies = new ArrayList<JsFunction>();
         jsFunctionsBodies.addAll(settings.getHelperFunctions());
         jsFunctionsBodies.addAll(settings.getOnSelectCallbacks());
         jsFunctionsBodies.addAll(settings.getOnChangeCallbacks());
         jsFunctionsBodies.addAll(settings.getOnReleaseCallbacks());
         return renderJsFunction(jsFunctionsBodies, getCallbackUrl());
     }
 
     protected String renderBodyOfMainFunctions() {
         StringBuilder result = new StringBuilder();
         result.append(renderMainCallbackFunctionWithBodies("onSelect", settings.getOnSelectCallbacks())).append("\n");
         result.append(renderMainCallbackFunctionWithBodies("onChange", settings.getOnChangeCallbacks())).append("\n");
         result.append(renderMainCallbackFunctionWithBodies("onRelease", settings.getOnReleaseCallbacks())).append("\n");
         return result.toString();
     }
 
     protected String renderMainCallbackFunctionWithBodies(String callbackName, List<JsFunction> callbackFunctions) {
         //if current callback doesn't have any function inside, don't render anything
         if ((null != callbackFunctions) && (callbackFunctions.size() > 0)) {
             StringBuilder callbackFunction = new StringBuilder();
             callbackFunction.append("var ").append(callbackName)
                     .append(" = function(wicket_coordinats) {").append("\n");
             for (JsFunction jsFunction : callbackFunctions) {
                 callbackFunction.append(jsFunction.getName()).append(("(wicket_coordinats);"));
                 callbackFunction.append("\n");
             }
             callbackFunction.append("};");
             return callbackFunction.toString();
         }
         return "";
     }
 
     protected String renderJsFunction(List<JsFunction> jsFunctions, CharSequence callbackUrl) {
         StringBuilder result = new StringBuilder();
         //this conversion is necessery because user could bind single jsfunction with multiple callbacks
         Map<String, JsFunction> nameToJsFunction = new HashMap<String, JsFunction>();
         for (JsFunction jsFunction : jsFunctions) {
             nameToJsFunction.put(jsFunction.getName(), jsFunction);
         }
 
         for (Map.Entry<String, JsFunction> entry : nameToJsFunction.entrySet()) {
             JsFunction jsFunction = entry.getValue();
             Pattern pattern = Pattern.compile("\\$\\{callbackUrl\\}");
             String replacedBodyFunction = jsFunction.getBody().replaceAll(pattern.pattern(), callbackUrl.toString());
             result.append("var ").append(jsFunction.getName()).append(" = ").append(replacedBodyFunction).append("\n");
         }
         return result.toString();
     }
 
     @Override
     protected void respond(AjaxRequestTarget target) {
         Integer x = RequestCycle.get().getRequest().getRequestParameters().getParameterValue("x").toOptionalInteger();
         Integer y = RequestCycle.get().getRequest().getRequestParameters().getParameterValue("y").toOptionalInteger();
         Integer x2 = RequestCycle.get().getRequest().getRequestParameters().getParameterValue("x2").toOptionalInteger();
         Integer y2 = RequestCycle.get().getRequest().getRequestParameters().getParameterValue("y2").toOptionalInteger();
        onCoordinatesChange(new Coordinates(x, y, x2, y2));
     }
 
    protected void onCoordinatesChange(Coordinates coordinates) {
     }
 
     public JcropController getApiController() {
         return apiController;
     }
 
     private CroppableSettings settings = null;
 
     private JcropController apiController = null;
 
     private boolean provideDefaultJsImplementation = true;
 }
