 package org.eclipse.jst.jsf.facelet.core.internal.tagmodel;
 
 import org.eclipse.jst.jsf.common.runtime.internal.view.model.common.IJSFTagElement;
 
 /**
  * A description of the a facelet tag
  * 
  * @author cbateman
  *
  */
 public abstract class FaceletTag implements IJSFTagElement
 {
     /**
      * 
      */
     private static final long serialVersionUID = 3027895246947365781L;
     private final String  _uri;
     private final String  _name;
     private final TagType _type;
     private final String  _tagHandlerClass;
 
     protected FaceletTag(final String uri, final String name, final TagType type, final String tagHandlerClassName)
     {
         _uri = uri;
         _name = name;
         _type = type;
         _tagHandlerClass = tagHandlerClassName;
     }
 
     //    protected FaceletTag(final String name, final TagType type)
     //    {
     //        this(name, type, null);
     //    }
 
     /**
      * @return the name of the tag
      */
     public final String getName() {
         return _name;
     }
 
     public final TagType getType() {
         return _type;
     }
 
     public String getUri()
     {
         return _uri;
     }
 
     public String getTagHandlerClassName() {
         return _tagHandlerClass;
     }
 
     @Override
     public String toString()
     {
         return "Tag Name: " + getName() + "Tag Type: " + getType();
     }

    public boolean isLocked()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public void setLocked()
    {
        // TODO Auto-generated method stub
    }
 }
