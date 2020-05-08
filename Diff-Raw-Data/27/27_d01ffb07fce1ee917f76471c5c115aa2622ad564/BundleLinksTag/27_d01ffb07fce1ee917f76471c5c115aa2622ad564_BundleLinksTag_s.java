 package au.com.sensis.mobile.crf.presentation.tag;
 
 import java.io.IOException;
 
 import javax.servlet.jsp.JspWriter;
 
 /**
  * Tag that bundles the output of any child {@link LinkTag}s that register
  * {@link au.com.sensis.mobile.crf.service.Resource}s with
  * this {@link BundleLinksTag} via the {@link #addResourcesToBundle(java.util.List)} method.
  *
  * @author w12495
  */
 public class BundleLinksTag extends AbstractBundleTag {
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void writeTag(final String href) throws IOException {
         getJspContext().getOut().print("<link id=\"");
 
         getJspContext().getOut().print(getId());
 
         getJspContext().getOut().print("\" href=\"");
         getJspContext().getOut().print(href);
         getJspContext().getOut().print("\" ");
 
         writeDynamicTagAttributes(getJspContext().getOut());
 
        getJspContext().getOut().print("></link>");
     }
 
 
     private void writeDynamicTagAttributes(final JspWriter jspWriter) throws IOException {
         boolean relAttributeFound = false;
         boolean typeAttributeFound = false;
 
         for (final DynamicTagAttribute attribute : getDynamicAttributes()) {
 
             if ("rel".equals(attribute.getLocalName())) {
                 relAttributeFound = true;
             }
 
             if ("type".equals(attribute.getLocalName())) {
                 typeAttributeFound = true;
             }
 
             jspWriter.print(attribute.getLocalName());
             jspWriter.print("=\"");
             jspWriter.print(attribute.getValue());
             jspWriter.print("\" ");
         }
 
         writeRelAttributeIfNotFound(jspWriter, relAttributeFound);
         writeTypeAttributeIfNotFound(jspWriter, typeAttributeFound);
     }
 
     private void writeTypeAttributeIfNotFound(final JspWriter jspWriter,
             final boolean typeAttributeFound)
             throws IOException {
 
         if (!typeAttributeFound) {
             jspWriter.print("type=\"text/css\" ");
         }
     }
 
 
     private void writeRelAttributeIfNotFound(final JspWriter jspWriter,
             final boolean charsetAttributeFound) throws IOException {
 
         if (!charsetAttributeFound) {
             jspWriter.print("rel=\"stylesheet\" ");
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected String getBundleFileExtension() {
         return "css";
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected String getTagDependenciesBeanName() {
         return "crf.bundleLinksTagDependencies";
     }
 }
 
