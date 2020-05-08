 package org.onebusaway.wiki.xwik.macros;
 
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.struts2.components.Component;
 import org.apache.struts2.dispatcher.Dispatcher;
 import org.xwiki.component.annotation.Requirement;
 import org.xwiki.component.manager.ComponentManager;
 import org.xwiki.rendering.block.Block;
 import org.xwiki.rendering.block.ParagraphBlock;
 import org.xwiki.rendering.block.RawBlock;
 import org.xwiki.rendering.block.XDOM;
 import org.xwiki.rendering.macro.AbstractMacro;
 import org.xwiki.rendering.macro.MacroExecutionException;
 import org.xwiki.rendering.parser.Parser;
 import org.xwiki.rendering.renderer.PrintRenderer;
 import org.xwiki.rendering.renderer.PrintRendererFactory;
 import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
 import org.xwiki.rendering.renderer.printer.WikiPrinter;
 import org.xwiki.rendering.syntax.Syntax;
 import org.xwiki.rendering.syntax.SyntaxType;
 import org.xwiki.rendering.transformation.MacroTransformationContext;
 import org.xwiki.rendering.transformation.Transformation;
 
 import com.opensymphony.xwork2.ActionContext;
 import com.opensymphony.xwork2.inject.Container;
 import com.opensymphony.xwork2.util.ValueStack;
 
 /**
  * Abstrat support class that makes it easy to wrap an Apache Struts 2
  * {@link Component} content rendering tag with an XWiki macro.
  * 
  * @author bdferris
  * 
  * @param <T>
  * @param <V>
  */
 public abstract class AbstractStrutsTagMacro<T extends CommonMacroParameters, V extends Component>
     extends AbstractMacro<T> {
 
   /**
    * The syntax representing the output of this macro (used for the RawBlock).
    */
   private static final Syntax XHTML_SYNTAX = new Syntax(SyntaxType.XHTML, "1.0");
 
   /**
    * Used to find the parser from syntax identifier.
    */
   @Requirement
   private ComponentManager componentManager;
 
   @Requirement("xhtml/1.0")
   private PrintRendererFactory xhtmlRendererFactory;
 
   public AbstractStrutsTagMacro(String name, String description,
       Class<T> parametersBeanClass) {
     super(name, description, parametersBeanClass);
   }
 
   public List<Block> execute(T parameters, String content,
       MacroTransformationContext context) throws MacroExecutionException {
 
     ActionContext actionContext = ActionContext.getContext();
     ValueStack stack = actionContext.getValueStack();
 
     V component = getBean(stack);
     Container container = Dispatcher.getInstance().getContainer();
     container.inject(component);
 
     populateParams(component, parameters);
 
     if (content == null)
       content = "";
 
     if (parameters.isWiki() && content.trim().length() > 0)
       content = renderWikiSyntax(content, context);
 
     StringWriter writer = new StringWriter();
     boolean evaluateBody = component.start(writer);
     if (!evaluateBody)
       content = "";
     component.end(writer, content);
 
     List<Block> wordBlockAsList = Arrays.<Block> asList(new RawBlock(
         writer.toString(), XHTML_SYNTAX));
 
     // Handle both inline mode and standalone mode.
     if (context.isInline()) {
       return wordBlockAsList;
     } else {
       // Wrap the result in a Paragraph Block since a WordBlock is an inline
       // element and it needs to be
       // inside a standalone block.
       return Arrays.<Block> asList(new ParagraphBlock(wordBlockAsList));
     }
   }
 
   public boolean supportsInlineMode() {
     return true;
   }
 
   protected abstract V getBean(ValueStack stack);
 
   protected void populateParams(V component, T params) {
 
   }
 
   private String renderWikiSyntax(String content,
       MacroTransformationContext context) throws MacroExecutionException {

    Transformation transformation = context.getMacroTransformation();
     Syntax wikiSyntax = context.getSyntax();
 
     String xhtml;
 
     try {
       // Parse the wiki syntax
       Parser parser = this.componentManager.lookup(Parser.class,
           wikiSyntax.toIdString());
       XDOM xdom = parser.parse(new StringReader(content));
 
       // Transform any macros within the parsed wiki dom
       transformation.transform(xdom, wikiSyntax);
 
       // Render the whole parsed content as an XHTML string
       WikiPrinter printer = new DefaultWikiPrinter();
       PrintRenderer renderer = this.xhtmlRendererFactory.createRenderer(printer);
       for (Block block : xdom.getChildren()) {
         block.traverse(renderer);
       }
 
       xhtml = printer.toString();
     } catch (Exception e) {
       throw new MacroExecutionException("Failed to parse content [" + content
           + "] written in [" + wikiSyntax + "] syntax.", e);
     }
 
     return xhtml;
   }
 }
