 /*
 Copyright (c) 2010 Geoff Lewis <gsl@gslsrc.net>
 
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */
 
 package net.gslsrc.dmex.render.xsl;
 
 import net.gslsrc.dmex.exercise.Problem;
 import net.gslsrc.dmex.render.RenderException;
 
 import java.io.Closeable;
 import java.io.InputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.ServiceLoader;
 import javax.xml.transform.Source;
 import javax.xml.transform.Templates;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.URIResolver;
 import javax.xml.transform.stream.StreamSource;
 
 /**
  * Maintains a map of {@link OutputType}s to XSL {@code Templates}.
  * Implementations provide the {@link #getXSLResource} method to supply the
  * classpath resource for an XSL stylesheet matching a given
  * {@code OutputType}.  If the stylesheet should be obtained from outside the
  * classpath, {@link #getXSLSource} can be overridden.
  * <p>
  * Note that XSL stylesheet components that are included or imported are also
  * sought on the classpath in the "xsl/" sub-directory, first relative to the
  * implementation's package, then relative to the {@code net.gslsrc.dmex.render}
  * package, which should contain any shared stylesheets.  If implementations
  * have other locations to search, they should override {@link #getURIResolver}
  * to provide their own resolver.
  * <p>
  * Templates can be obtained for a particular {@link Problem} type using the
  * {@link #getTemplates(java.lang.Class)} factory method.  This looks up
  * {@code ProblemTemplates} implementations that are registered as providers
 * of the {@code "net.gslsrc.dmex.render.xsl.ProblemTemplates"} service.
  *
  * @author Geoff Lewis
  */
 public abstract class ProblemTemplates {
 
     private static ServiceLoader<ProblemTemplates> loader =
             ServiceLoader.load(ProblemTemplates.class);
 
     private TransformerFactory transformerFactory;
 
     private Map<OutputType, Templates> templatesStore;
 
     protected ProblemTemplates() {}
 
     public static ProblemTemplates getTemplates(
             Class<? extends Problem> problemType) {
         if (problemType == null) {
             throw new NullPointerException("Problem type is null");
         }
 
         for (ProblemTemplates templates : loader) {
             if (templates.appliesTo(problemType)) {
                 return templates;
             }
         }
 
         throw new IllegalArgumentException("Unsupported problem type \""
                  + problemType.getName() + "\"");
     }
 
     public abstract boolean appliesTo(Class<? extends Problem> problemType);
 
     public abstract OutputType[] getSupportedTypes();
 
     public Templates getTemplates(OutputType type) throws RenderException {
         if (type == null) {
             throw new NullPointerException("Output type is null");
         }
 
         Templates templates = null;
 
         if (templatesStore != null && templatesStore.containsKey(type)) {
             templates = templatesStore.get(type);
         } else {
             Source source = getXSLSource(type);
 
             try {
                 templates = getTransformerFactory().newTemplates(source);
             } catch (TransformerConfigurationException tce) {
                 throw new RenderException(
                         "Failed to create transformer for \"" + type + "\"",
                         tce);
             } finally {
                 closeQuietly(source);
             }
 
             if (templatesStore == null) {
                 templatesStore = new HashMap<OutputType, Templates>();
             }
 
             templatesStore.put(type, templates);
         }
 
         return templates;
     }
 
     protected Source getXSLSource(OutputType type) throws RenderException {
         if (type == null) {
             throw new NullPointerException("Output type is null");
         }
 
         String resource = getXSLResource(type);
         if (resource == null) {
             throw new RenderException(this
                     + " missing XSL transform resource for "
                     + type + " output");
         }
 
         InputStream in = null;
         if (resource != null) {
             in = getClass().getResourceAsStream(resource);
         }
 
         if (in == null) {
             throw new RenderException(this
                     + " missing XSL transform resource \"" + resource
                     + "\" required for " + type + " output");
         }
 
         return new StreamSource(in);
     }
 
     protected abstract String getXSLResource(OutputType type);
 
     private TransformerFactory getTransformerFactory() {
         if (transformerFactory == null) {
             transformerFactory = TransformerFactory.newInstance();
             transformerFactory.setURIResolver(getURIResolver());
         }
 
         return transformerFactory;
     }
 
     protected URIResolver getURIResolver() {
         return new RendererURIResolver(getClass());
     }
 
     private static void closeQuietly(Source source) {
         if (source instanceof StreamSource) {
             closeQuietly(((StreamSource)source).getInputStream());
         }
     }
 
     private static void closeQuietly(Closeable c) {
         if (c != null) {
             try {
                 c.close();
             // CHECKSTYLE:OFF
             } catch (IOException ioe) {}
             // CHECKSTYLE:ON
         }
     }
 
     /**
      * The output type associated with a particular XSL stylesheet.  This is a
      * decorator interface typically applied to an enum.
      */
     public interface OutputType {
         String name();
     }
 
     /**
     * Resolves {@code <xsl:import>} and {@code <xsl:include>} URIs when
      * parsing XSL stylesheets.  Attempts to look the URI up on the classpath.
      * The rules are:
      *
      * <ul>
      * <li>stylesheets are sought in the local package first</li>
      * <li>then stylesheets are sought in the "xsl/" sub-directory of the
      * package (so "xsl/" is prepended to all hrefs).</li>
      * <li>first: the stylesheet is sought relative to this template
      * implementation's package.  This is the location for custom stylesheets
      * that apply to the associated exercise.</li>
      * <li>second: the stylesheet is sought relative to the package of
      * {@code ProblemTemplates}.  This is the location for common stylesheets
      * used by some or all exercises.  (In this case, prepending "xsl/" to the
      * href is likely to be unncessary as the package ends with "xsl".)</li>
      * </ul>
      *
      * If the stylesheet is not found in an "xsl/" directory on the classpath,
      * the resolver returns null and the standard resolving behaviour is used
      * which handles the URI normally (file, URL, etc.).
      */
     private class RendererURIResolver implements URIResolver {
         private Class<?> cls;
 
         RendererURIResolver(Class<?> cls) {
             assert cls != null;
 
             this.cls = cls;
         }
 
         @Override
         public Source resolve(String href, String base)
             throws TransformerException {
 
             InputStream in = resolve(href);
             if (in == null) {
                 in = resolve("xsl/" + href);
             }
 
             if (in != null) {
                 return new StreamSource(in);
             }
 
             return null;
         }
 
         private InputStream resolve(String resource) {
             if (resource == null) {
                 return null;
             }
 
             // Search relative to this impl first.
             InputStream in = cls.getResourceAsStream(resource);
 
             if (in == null) {
                 // Else search in the global templates.
                 in = ProblemTemplates.class.getResourceAsStream(resource);
             }
 
             return in;
         }
 
         @Override
         public String toString() {
             return "RendererURIResolver[" + cls.getSimpleName() + "]";
         }
     }
 }
