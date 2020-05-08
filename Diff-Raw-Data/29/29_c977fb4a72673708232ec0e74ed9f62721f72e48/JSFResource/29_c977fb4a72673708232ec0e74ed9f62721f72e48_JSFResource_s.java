 package org.eclipse.jst.jsf.designtime.internal.resources;
 
 import org.eclipse.jst.jsf.common.internal.resource.ContentTypeResolver;
 
 /**
  * Represents a JSF loadable resource (spec 2.6).
  * 
  * @author cbateman
  * 
  */
 public abstract class JSFResource extends JSFResourceFragment implements IJSFResource
 {
     private final ContentTypeResolver _contentTypeResolver;
 
     /**
      * @param id
      * @param contentTypeResolver
      */
     public JSFResource(final ResourceIdentifier id,
             final ContentTypeResolver contentTypeResolver)
     {
         super(id, Type.RESOURCE);
         _contentTypeResolver = contentTypeResolver;
     }
 
     /**
      * @return the identifier for this resource.
      */
     public final ResourceIdentifier getId()
     {
         return (ResourceIdentifier) super.getId();
     }
 
     /**
      * @return true if this jsf resource is currently accessible.
      */
     @Override
     public abstract boolean isAccessible();
 
     public boolean isContentType(final String contentTypeName)
     {
        return _contentTypeResolver.matchesType(contentTypeName, getId()
                .getResourceName());
     }
 
     @Override
     public String toString()
     {
         return getId().toString();
     }
 }
