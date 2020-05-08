 package au.com.sensis.mobile.crf.presentation.tag;
 
 
 import javax.servlet.jsp.PageContext;
 
 import org.apache.commons.lang.StringUtils;
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 
 /**
  * Facade to a script tag that uses the Content Rendering Framework to resolve the
  * resource path set into {@link #setSrc(String)}.
  *
  * <p>
  * This facade also prevents duplicate tags from being written in the current
  * HTTP request. The unique id of each tag is governed by the value of the href
  * attribute.
  * </p>
  *
  * @author Adrian.Koh2@sensis.com.au
  */
 public class ScriptTag extends AbstractDuplicatePreventingTag {
 
     private String src;
     private String name;
 
 
     /**
      * Attribute name used to store a map of ({@link TagWriter#getId()},
      * {@link TagWriter}) pairs.
      */
     public static final String SCRIPT_WRITER_MAP_ATTRIBUTE_NAME =
             ScriptTag.class.getName() + ".scriptTagWriterMap";
 
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void validateAttributes() {
         validateOneOfSrcOrNameSet();
         validateSrcIfNotBlank();
     }
 
     private void validateSrcIfNotBlank() {
         if (StringUtils.isNotBlank(getSrc())
                 && (getSrc().startsWith("..") || getSrc().startsWith("/"))) {
             throw new IllegalArgumentException("src must not start with '..' or '/'. Was: '"
                     + getSrc() + "'");
         }
 
     }
 
     private void validateOneOfSrcOrNameSet() {
         if (bothBlankOrBothNotBlank(getSrc(), getName())) {
             throw new IllegalArgumentException(
                     "You must set either the src or name attributes but not both. src='" + getSrc()
                             + "'; name='" + getName() + "'");
         }
 
     }
 
     private boolean bothBlankOrBothNotBlank(final String value1,
             final String value2) {
        return StringUtils.isBlank(value1) && StringUtils.isBlank(value2)
                || StringUtils.isNotBlank(value1) && StringUtils.isNotBlank(value2);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected TagWriter createTagWriter() {
         final BundleScriptsTag parentBundleScriptsTag =
             (BundleScriptsTag) findAncestorWithClass(this,
                     BundleScriptsTag.class);
 
         return ScriptTagWriterFactory
             .getSingletonInstance()
                 .createScriptTagWriter(getDevice(),
                         getDynamicAttributes(), getSrc(), getName(),
                         getTagDependencies(), parentBundleScriptsTag);
     }
 
     private ScriptTagDependencies getTagDependencies() {
         final PageContext pc = (PageContext) getJspContext();
 
         final WebApplicationContext webApplicationContext =
                 WebApplicationContextUtils.getRequiredWebApplicationContext(pc
                         .getServletContext());
         return (ScriptTagDependencies) webApplicationContext
                 .getBean(ScriptTagDependencies.BEAN_NAME);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getTagWriterMapAttributeName() {
         return SCRIPT_WRITER_MAP_ATTRIBUTE_NAME;
     }
 
     /**
      * @return the src
      */
     @Override
     public final String getPathAttribute() {
         return getSrc();
     }
 
     /**
      * @return the src
      */
     public final String getSrc() {
         return src;
     }
 
     /**
      * @param src the src to set.
      */
     public final void setSrc(final String src) {
         this.src = src;
     }
 
     /**
      * @return the name
      */
     public String getName() {
         return name;
     }
 
     /**
      * @param name the name to set
      */
     public void setName(final String name) {
         this.name = name;
     }
 
 }
 
