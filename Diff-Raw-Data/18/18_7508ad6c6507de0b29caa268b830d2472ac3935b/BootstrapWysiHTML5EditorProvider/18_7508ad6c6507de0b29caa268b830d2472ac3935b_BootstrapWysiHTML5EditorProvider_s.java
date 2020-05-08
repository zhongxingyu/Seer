 package main.origo.bootstrapwysihtml5.interceptors;
 
 import controllers.routes;
 import main.origo.core.annotations.*;
 import main.origo.core.helpers.ProviderHelper;
 import main.origo.core.ui.Element;
 import main.origo.core.ui.RenderingContext;
 import models.origo.core.Content;
 import play.api.templates.Html;
 import views.html.origo.bootstrapwysihtml5.decorators.forms.wysi.bootstrapwysihtml5_script;
 import views.html.origo.bootstrapwysihtml5.decorators.forms.wysi.bootstrapwysihtml5_templates_script;
 
 @Interceptor
 public class BootstrapWysiHTML5EditorProvider {
 
     private static final String JS_LOADED = "bootstrapwysihtml_js_loaded";
 
     public static class BootstrapWysiHtml5CustomTemplatesElement extends Element {
 
         public BootstrapWysiHtml5CustomTemplatesElement() {
             super("wysihtml5_editor_templates_script");
         }
 
         @Override
         public Html decorate(RenderingContext renderingContext) {
             return bootstrapwysihtml5_templates_script.render(this.id);
         }
     }
 
     public static class BootstrapWysiHtml5ScriptElement extends Element {
 
         public BootstrapWysiHtml5ScriptElement() {
             super("wysihtml5_editor_script");
         }
 
         @Override
         public Html decorate(RenderingContext renderingContext) {
             return bootstrapwysihtml5_script.render(this.id);
         }
     }
 
     @Provides(type = Core.Type.NODE, with = Core.With.EDITOR)
     public static Element createEditor(Provides.Context context) {
         Content content = (Content) context.args().get("content");
         if (content != null) {
             return new Element.InputTextArea().setId(content.identifier).setBody(content.value);
         } else {
             return new Element.InputTextArea();
         }
     }
 
     @OnLoad(type = Core.Type.NODE, with = Core.With.EDITOR)
     public static void setupEditor(OnLoad.Context context) {
         if (ProviderHelper.isProvider(context.withType(), BootstrapWysiHTML5EditorProvider.class) && !context.attributes().containsKey(JS_LOADED)) {
             String wysiHtmlScript = routes.Assets.at("javascripts/origo/bootstrapwysihtml5/wysihtml5-0.4.0pre.min.js").url();
             context.node().addTailElement(new Element.Script().setWeight(9999).addAttribute("src", wysiHtmlScript));
             String parserRulesScript = routes.Assets.at("javascripts/origo/bootstrapwysihtml5/parser_rules/advanced.js").url();
             context.node().addTailElement(new Element.Script().setWeight(9999).addAttribute("src", parserRulesScript));
 
             String bootstrapWysiHtmlScript = routes.Assets.at("javascripts/origo/bootstrapwysihtml5/bootstrap-wysihtml5.js").url();
             context.node().addTailElement(new Element.Script().setWeight(9999).addAttribute("src", bootstrapWysiHtmlScript));
 
            String mainStyle = routes.Assets.at("stylesheets/origo/bootstrapwysihtml5/bootstrap-wysihtml5.css").url();
             context.node().addHeadElement(new Element.Link().addAttribute("href", mainStyle).addAttribute("rel", "stylesheet"));
 
             context.attributes().put(JS_LOADED, true);
         }
     }
 
     @OnInsertElement(with = Element.InputTextArea.class, after = true)
     public static void insertScript(OnInsertElement.Context context) {
         if (context.attributes().containsKey(JS_LOADED)) {
             if (!context.attributes().containsKey("templates_created")) {
                 context.node().addTailElement(new BootstrapWysiHtml5CustomTemplatesElement());
                 context.attributes().put("templates_created", true);
             }
             context.node().addTailElement(new BootstrapWysiHtml5ScriptElement().setId(context.element.id));
         }
     }
 
 }
