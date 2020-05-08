 /**
  * Copyright 2011 Alexandre Dutra
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package fr.xebia.confluence2wordpress.core.converter;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map.Entry;
 
 import org.htmlcleaner.CleanerProperties;
 import org.htmlcleaner.CleanerTransformations;
 import org.htmlcleaner.HtmlCleaner;
 import org.htmlcleaner.HtmlSerializer;
 import org.htmlcleaner.SimpleHtmlSerializer;
 import org.htmlcleaner.TagNode;
 import org.htmlcleaner.TagNodeVisitor;
 import org.htmlcleaner.TagTransformation;
 import org.htmlcleaner.WhitespaceTolerantTagInfoProvider;
 
 import com.atlassian.confluence.core.ContentEntityObject;
 import com.atlassian.confluence.renderer.MacroManager;
 import com.atlassian.renderer.WikiStyleRenderer;
 
 import fr.xebia.confluence2wordpress.core.converter.postprocessors.PostProcessor;
 import fr.xebia.confluence2wordpress.core.converter.postprocessors.PressReviewHeaderPostProcessor;
 import fr.xebia.confluence2wordpress.core.converter.postprocessors.TableOfContentsPostProcessor;
 import fr.xebia.confluence2wordpress.core.converter.preprocessors.IgnoredMacrosPreProcessor;
 import fr.xebia.confluence2wordpress.core.converter.preprocessors.PreProcessor;
 import fr.xebia.confluence2wordpress.core.converter.visitors.AttachmentsProcessor;
 import fr.xebia.confluence2wordpress.core.converter.visitors.CdataProcessor;
 import fr.xebia.confluence2wordpress.core.converter.visitors.CssClassNameCleaner;
 import fr.xebia.confluence2wordpress.core.converter.visitors.EmptySpanStripper;
 import fr.xebia.confluence2wordpress.core.converter.visitors.LegacyCodeMacroProcessor;
 import fr.xebia.confluence2wordpress.core.converter.visitors.MoreMacroProcessor;
 import fr.xebia.confluence2wordpress.core.converter.visitors.NewCodeMacroProcessor;
 import fr.xebia.confluence2wordpress.core.converter.visitors.SyncInfoMacroProcessor;
 
 public class Converter {
 
     private WikiStyleRenderer wikiStyleRenderer;
 
     private MacroManager macroManager;
 
     public Converter(
         WikiStyleRenderer wikiStyleRenderer,
         MacroManager macroManager) {
         super();
         this.wikiStyleRenderer = wikiStyleRenderer;
         this.macroManager = macroManager;
     }
 
     public String convert(ContentEntityObject page, ConverterOptions options) {
     	
         String wiki = page.getContent();
         
         //Wiki pre-processing
         List<PreProcessor> preProcessors = getPreProcessors(options);
         for (PreProcessor preProcessor : preProcessors) {
         	wiki = preProcessor.preProcess(wiki, options);
         }
         
         //wiki -> html conversion
        String originalTitle = page.getTitle();
        String confluenceHtml;
        try {
        	//temporarily replace page title to get correct anchors
        	//(I know it's ugly)
        	//Otherwise the anchors are build by
        	//com.atlassian.renderer.v2.macro.basic.BasicAnchorMacro.getAnchor(RenderContext, String)
        	//basically it's GeneralUtil.urlEncode((page title + "-" + heading title).trim().replaceAll(" ", ""))
        	//see also com.atlassian.confluence.util.GeneralUtil.urlEncode(String)
            page.setTitle(options.getPageTitle());
            confluenceHtml = wikiStyleRenderer.convertWikiToXHtml(page.toPageContext(), wiki);
        } finally {
            page.setTitle(originalTitle);
        }
 
         //HTML cleanup
         HtmlCleaner cleaner = getHtmlCleaner(options);
         TagNode root = cleaner.clean(confluenceHtml);
         TagNode body = root.findElementByName("body", false);
 
         //DOM traversal
         List<TagNodeVisitor> visitors = getTagNodeVisitors(options);
         for (TagNodeVisitor visitor : visitors) {
             body.traverse(visitor);
         }
         
         //serialization
         String html = serialize(body, cleaner.getProperties());
 
         //HTML post-processing
         List<PostProcessor> postProcessors = getPostProcessors(options);
         for (PostProcessor postProcessor : postProcessors) {
             html = postProcessor.postProcess(html, body, options);
         }
         
         return html;
     }
 
     protected HtmlCleaner getHtmlCleaner(ConverterOptions options) {
         //HtmlCleaner is NOT thread-safe
     	CleanerProperties cleanerProps = getCleanerProperties(options);
         HtmlCleaner cleaner = new HtmlCleaner(new WhitespaceTolerantTagInfoProvider(), cleanerProps);
         CleanerTransformations transformations = getCleanerTransformations(options);
         cleaner.setTransformations(transformations);
 		return cleaner;
 	}
 
 	protected String serialize(TagNode body, CleanerProperties cleanerProps) {
         try {
             //PrettyHtmlSerializer does not work very well
             //JTidy is too violent and Jericho almost OK but no
             HtmlSerializer serializer = new SimpleHtmlSerializer(cleanerProps);
             return serializer.getAsString(body, "UTF-8", true);
         } catch (IOException e) {
             // should not occur with string writers
             return null;
         }
     }
     
     protected CleanerProperties getCleanerProperties(ConverterOptions options) {
         CleanerProperties properties = new CleanerProperties();
         properties.setOmitXmlDeclaration(options.isOmitXmlDeclaration());
         properties.setUseCdataForScriptAndStyle(options.isUseCdataForScriptAndStyle());
         properties.setOmitComments(options.isOmitComments());
         properties.setUseEmptyElementTags(options.isUseEmptyElementTags());
         return properties;
     }
 
     protected CleanerTransformations getCleanerTransformations(ConverterOptions options) {
         CleanerTransformations transformations = new CleanerTransformations();
         //tag transformations
         if(options.getTagTransformations() != null) {
             for (Entry<String,String> entry : options.getTagTransformations().entrySet()) {
                 transformations.addTransformation(new TagTransformation(entry.getKey(), entry.getValue()));
             }
         }
         //font -> span
         if(options.isConvertFontTagToSpan()) {
             TagTransformation tt = new TagTransformation("font", "span", true);
             tt.addAttributeTransformation("size");
             tt.addAttributeTransformation("face");
             tt.addAttributeTransformation(
                 "style",
                 "${style};font-family=${face};font-size=${size};"
                 );
             transformations.addTransformation(tt);
         }
         return transformations;
     }
 
     protected List<TagNodeVisitor> getTagNodeVisitors(ConverterOptions options) {
         List<TagNodeVisitor> visitors = new ArrayList<TagNodeVisitor>();
         visitors.add(new MoreMacroProcessor());
         visitors.add(new SyncInfoMacroProcessor());
         visitors.add(new LegacyCodeMacroProcessor(options.getSyntaxHighlighterPlugin()));
         visitors.add(new NewCodeMacroProcessor(options.getSyntaxHighlighterPlugin()));
         if(options.getAttachmentsMap() != null) {
             visitors.add(new AttachmentsProcessor(options.getAttachmentsMap()));
         }
         visitors.add(new CdataProcessor());
         visitors.add(new CssClassNameCleaner());
         visitors.add(new EmptySpanStripper());
         return visitors;
     }
 
     protected List<PreProcessor> getPreProcessors(ConverterOptions options) {
         List<PreProcessor> processors = new ArrayList<PreProcessor>();
         processors.add(new IgnoredMacrosPreProcessor(macroManager));
         return processors;
 	}
 
     protected List<PostProcessor> getPostProcessors(ConverterOptions options) {
         List<PostProcessor> processors = new ArrayList<PostProcessor>();
         processors.add(new TableOfContentsPostProcessor());
         processors.add(new PressReviewHeaderPostProcessor());
         return processors;
     }
 
 
 }
