 /*
  * See the NOTICE file distributed with this work for additional
  * information regarding copyright ownership.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.xwiki.editor.tool.autocomplete.internal;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.QueryParam;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.velocity.VelocityContext;
 import org.slf4j.Logger;
 import org.xwiki.component.annotation.Component;
 import org.xwiki.component.manager.ComponentLookupException;
 import org.xwiki.component.manager.ComponentManager;
 import org.xwiki.editor.tool.autocomplete.AutoCompletionMethodFinder;
 import org.xwiki.editor.tool.autocomplete.HintData;
 import org.xwiki.editor.tool.autocomplete.Hints;
 import org.xwiki.editor.tool.autocomplete.TargetContent;
 import org.xwiki.editor.tool.autocomplete.TargetContentLocator;
 import org.xwiki.editor.tool.autocomplete.TargetContentType;
 import org.xwiki.model.reference.DocumentReference;
 import org.xwiki.rest.XWikiRestComponent;
 import org.xwiki.velocity.VelocityManager;
 import org.xwiki.velocity.internal.util.InvalidVelocityException;
 import org.xwiki.velocity.internal.util.VelocityParser;
 import org.xwiki.velocity.internal.util.VelocityParserContext;
 
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.web.Utils;
 
 /**
  * REST Resource for returning autocompletion hints. The content to autocomplete is passed in the request body, the
  * position of the cursor and the syntax in which the content is written in are passed as request parameters.
  * 
  * @version $Id$
  */
 @Component("org.xwiki.editor.tool.autocomplete.internal.AutoCompletionResource")
 @Path("/autocomplete")
 public class AutoCompletionResource implements XWikiRestComponent
 {
     /**
      * Used to get the Velocity Context from which we retrieve the list of bound variables that are used for
      * autocompletion.
      */
     @Inject
     private VelocityManager velocityManager;
 
     /**
      * Used to dynamically find Autocompletion Method finder to handle specific cases.
      * 
      * @see AutoCompletionMethodFinder
      */
     @Inject
     private ComponentManager componentManager;
 
     /**
      * Used to autodiscover method hints.
      */
     @Inject
     private AutoCompletionMethodFinder defaultAutoCompletionMethodFinder;
 
     /**
      * Used to extract the content and the type under the cursor position.
      */
     @Inject
     private TargetContentLocator targetContentLocator;
 
     /**
      * Logging framework.
      */
     @Inject
     private Logger logger;
 
     /**
      * A Velocity Parser that we use to help parse Velocity content for figuring out autocompletion.
      */
     private VelocityParser parser = new VelocityParser();
 
     /**
      * Main REST entry point for getting Autocompletion hints.
      * 
      * @param offset the position of the cursor in the full content
      * @param syntaxId the syntax in which the content is written in
      * @param content the full wiki content
      * @return the list of autocompletion hints
      */
     @POST
     public Hints getAutoCompletionHints(@QueryParam("offset") int offset, @QueryParam("syntax") String syntaxId,
         String content)
     {
         Hints hints = new Hints();
 
         // Only support autocompletion on Velocity ATM
         TargetContent targetContent = this.targetContentLocator.locate(content, syntaxId, offset);
         if (targetContent.getType() == TargetContentType.VELOCITY) {
             hints = getHints(targetContent.getContent(), targetContent.getPosition());
         }
 
         // Subtract the temporary user input size from the initial offset to get the absolute start offset of the user's
         // input.
         hints.withStartOffset(offset - hints.getStartOffset());
 
         return hints;
     }
 
     /**
      * Parses the passed content and return the autocompletion hints for the passed cursor position.
      *
      * @param content the Velocity content to autocomplete
      * @param offset the position of the cursor relative to the Velocity content
      * @return the list of autocompletion hints
      */
     private Hints getHints(String content, int offset)
     {
         Hints results = new Hints();
 
         // General algorithm:
         // - We start parsing at the first dollar before the cursor
         // - We get the full reference (a reference in VTL can be a variable, a property or a method call, see
         //   http://velocity.apache.org/engine/devel/user-guide.html#References)
         // - We split the reference on "." and handle first the case of a variable. If there's no "." then it means
         //   we're autocompleting a variable and we find all matching Velocity context variables and return them.
         // - If there's at least one "." then we parse the whole chain of method calls till the last dot to find the
         //   return type of the last method call. This allows us to know the full list of methods for autocompletion.
 
         // Find the dollar sign before the current position
         char[] chars = content.toCharArray();
         VelocityContext velocityContext = getVelocityContext();
         int dollarPos = StringUtils.lastIndexOf(content, '$', offset);
 
         if (dollarPos == -1) {
             return results;
         }
 
         // Special case for when there's no variable after the dollar position since the Velocity Parser doesn't
         // support parsing this case.
         if (isCursorDirectlyAfterDollar(chars, dollarPos, offset)) {
             // Find all objects bound to the Velocity Context. We need to also look in the chained context since
             // this is where we store Velocity Tools
             results = getVelocityContextKeys("", velocityContext);
         } else {
             // The cursor is not directly after the dollar sign.
             try {
                 // Get all the references after the dollar sign. For example if the input is "$a.b().ccc" then
                 // we get "a.b().ccc".
                 VelocityParserContext context = new VelocityParserContext();
                 StringBuffer reference = new StringBuffer();
                 StringBuffer identifier = new StringBuffer();
                 int endPos = this.parser.getVar(chars, dollarPos, identifier, reference, context);
 
                 // If endPos matches the current cursor position then it means we have a valid token for
                 // autocompletion. Otherwise we don't autocomplete (for example there could be spaces between the
                 // reference and the cursor position).
                 // Note: We need to handle the special when the cursor is just after the '.' char.
                 if (endPos + 1 == offset && chars[endPos] == '.') {
                     endPos++;
                     reference.append('.');
                 }
                 if (endPos == offset) {
                     // Find out if we're autocompleting a variable. In this case there's no "." in the reference
                     int methodPos = reference.indexOf(".");
                     if (methodPos > -1) {
                         // Autocomplete a method!
                         results = getHintsForMethodCall(chars, dollarPos + methodPos, identifier.toString());
                     } else {
                         // Autocomplete a variable! Find all matching variables.
                         results = getVelocityContextKeys(identifier.toString(), velocityContext);
                     }
                 }
             } catch (InvalidVelocityException e) {
                 this.logger.debug("Failed to get autocomplete hints for content [{}] at offset [{}]",
                     new Object[] {content, offset, e});
             }
         }
 
         return results;
     }
 
     /**
      * Find hints for the passed content assuming thay it's representing method calls.
      *
      * @param chars the content to parse
      * @param currentPos the current position at which method calls are starting
      * @param variableName the name of the variable on which the first method is called
      * @return the list of autocompletion hints
      * @throws InvalidVelocityException if a parsing error occurs
      */
     private Hints getHintsForMethodCall(char[] chars, int currentPos, String variableName)
         throws InvalidVelocityException
     {
         Hints results = new Hints();
         VelocityParserContext context = new VelocityParserContext();
 
         // Find the next method after the currentPos.
         int pos = currentPos;
         AutoCompletionMethodFinder methodFinder = getMethodFinder(variableName);
         List<Class> methodClasses = Arrays.asList((Class) getVelocityContext().get(variableName).getClass());
         do {
             // Handle the special case when the cursor is after the dot ('.')
             String methodName;
             if (pos == chars.length - 1) {
                 methodName = "";
                 pos++;
             } else {
                 StringBuffer method = new StringBuffer();
                 pos = this.parser.getMethodOrProperty(chars, pos, method, context);
                 methodName = StringUtils.substringBefore(method.toString(), "(").substring(1);
             }
 
             if (pos == chars.length) {
                 // Find all methods matching methodName in methodClasses
                 for (Class methodClass : methodClasses) {
                     results.withHints(methodFinder.findMethods(methodClass, methodName));
                 }
                 results = results.withStartOffset(methodName.length());
                 break;
             } else {
                 // Find the returned type for method "methodName".
                 List<Class> returnTypes = new ArrayList<Class>();
                 for (Class methodClass : methodClasses) {
                     returnTypes.addAll(methodFinder.findMethodReturnTypes(methodClass, methodName));
                 }
                 methodClasses = returnTypes;

                // Reset the method finder since we use a specialized finder only for the first autocompletion method
                methodFinder = this.defaultAutoCompletionMethodFinder;
             }
         } while (true);
 
         return results;
     }
 
     /**
      * @param chars the content to parse
      * @param dollarPos the position of the dollar symbol
      * @param offset the position in the whole content of the cursor
      * @return false if there's a velocity variable after the dollar sign
      */
     private boolean isCursorDirectlyAfterDollar(char[] chars, int dollarPos, int offset)
     {
         boolean result = true;
 
         for (int i = dollarPos; i < offset; i++) {
             if (chars[i] != '$' && chars[i] != '!' && chars[i] != '{') {
                 result = false;
                 break;
             }
         }
 
         return result;
     }
 
     /**
      * Find out all Velocity variable names bound in the Velocity Context.
      * 
      * @param fragmentToMatch the prefix to filter with in order to return only variable whose names start with the
      *            passed string
      * @param velocityContext the Velocity Context from which to get the bound variables
      * @return the Velocity variables
      */
     private Hints getVelocityContextKeys(String fragmentToMatch, VelocityContext velocityContext)
     {
         Hints hints = new Hints();
 
         addVelocityKeys(hints, velocityContext.getKeys(), fragmentToMatch);
         if (velocityContext.getChainedContext() != null) {
             addVelocityKeys(hints, velocityContext.getChainedContext().getKeys(), fragmentToMatch);
         }
 
         return hints;
     }
 
     /**
      * Add variables to the passed results list.
      * 
      * @param results the list of variable names
      * @param keys the keys containing the variables to add
      * @param fragmentToMatch the filter in order to only add variable whose names start with the passed string
      */
     private void addVelocityKeys(Hints results, Object[] keys, String fragmentToMatch)
     {
         for (Object key : keys) {
             if (key instanceof String && ((String) key).startsWith(fragmentToMatch)) {
                 results.withHints(new HintData((String) key, (String) key));
             }
         }
     }
 
     /**
      * @param hint the hint of the finder to return. If no such component exist return the default finder
      * @return the {@link AutoCompletionMethodFinder} to use depending on the passed hint
      */
     private AutoCompletionMethodFinder getMethodFinder(String hint)
     {
         AutoCompletionMethodFinder finder = null;
 
         // Allow special handling for classes that have registered a custom introspection handler
         if (this.componentManager.hasComponent(AutoCompletionMethodFinder.class, hint)) {
             try {
                 finder = this.componentManager.getInstance(AutoCompletionMethodFinder.class, hint);
             } catch (ComponentLookupException e) {
                 // Component not found, continue with default finder...
             }
         }
 
         if (finder == null) {
             finder = this.defaultAutoCompletionMethodFinder;
         }
 
         return  finder;
     }
 
     /**
      * @return the Velocity Context used to find existing bound variables
      */
     protected VelocityContext getVelocityContext()
     {
         VelocityContext velocityContext = this.velocityManager.getVelocityContext();
 
         // We add the "doc", "tdoc" and "sdoc" mappings since we don't get them from the Velocity Manager as they are
         // normally added based on the document passed in the request. However since we return the same method names
         // whatever the doc, we can manually add them.
         XWikiDocument fakeDocument = createFakeXWikiDocument();
         velocityContext.put("doc", fakeDocument);
         velocityContext.put("sdoc", fakeDocument);
         velocityContext.put("tdoc", fakeDocument);
 
         return velocityContext;
     }
 
     /**
      * @return a fake XWiki Document instance
      */
     protected XWikiDocument createFakeXWikiDocument()
     {
         // Note: Creating an XWikiDocument instance requires that the static component manager be set up
         // unfortunately...
         Utils.setComponentManager(this.componentManager);
         return new XWikiDocument(new DocumentReference("notusedwiki", "notusedspace", "notusedpage"));
     }
 }
