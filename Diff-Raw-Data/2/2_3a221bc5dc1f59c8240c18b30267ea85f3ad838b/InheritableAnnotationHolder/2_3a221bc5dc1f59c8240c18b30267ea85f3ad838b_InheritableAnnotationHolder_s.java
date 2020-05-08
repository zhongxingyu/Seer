 /*
  * JBoss, the OpenSource J2EE webOS
  *
  * Distributable under LGPL license.
  * See terms of license at gnu.org.
  */
 package org.jboss.reflect;
 
 import java.util.HashMap;
 
 /**
  * comment
  *
  * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
  */
 public class InheritableAnnotationHolder  implements AnnotatedInfo
 {
    protected HashMap declaredAnnotations;
    protected HashMap allAnnotations;
    protected AnnotationValue[] allAnnotationsArray;
    protected AnnotationValue[] declaredAnnotationsArray;
    protected InheritableAnnotationHolder superHolder;
 
 
    public InheritableAnnotationHolder()
    {
    }
 
    public InheritableAnnotationHolder(AnnotationValue[] annotations)
    {
       setupAnnotations(annotations);
    }
 
 
    public AnnotationValue[] getAnnotations()
    {
       return allAnnotationsArray;
    }
 
    public AnnotationValue[] getDeclaredAnnotations()
    {
       return declaredAnnotationsArray;
    }
 
    public AnnotationValue getAnnotation(String name)
    {
       return (AnnotationValue) allAnnotations.get(name);
    }
 
    public boolean isAnnotationPresent(String name)
    {
       return allAnnotations.containsKey(name);
    }
 
    protected void setupAnnotations(AnnotationValue[] annotations)
    {
       if (annotations != null && annotations.length > 0)
       {
          declaredAnnotations = new HashMap();
          declaredAnnotationsArray = annotations;
          for (int i = 0; i < annotations.length; i++)
          {
             declaredAnnotations.put(annotations[i].getAnnotationType().getName(), annotations[i]);
          }
          allAnnotations = new HashMap();
 
          if (superHolder != null && superHolder.allAnnotationsArray != null)
          {
             for (int i = 0; i < superHolder.allAnnotationsArray.length; i++)
             {
                AnnotationValue av = superHolder.allAnnotationsArray[i];
               if (av.getAnnotationType().isAnnotationPresent(java.lang.annotation.Inherited.class.getName()))
                {
                   allAnnotations.put(av.getAnnotationType().getName(), av);
                }
             }
          }
          else
          {
             allAnnotationsArray = declaredAnnotationsArray;
          }
          for (int i = 0; i < annotations.length; i++)
          {
             allAnnotations.put(annotations[i].getAnnotationType().getName(), annotations[i]);
          }
 
          allAnnotationsArray = (AnnotationValue[])allAnnotations.values().toArray(new AnnotationValue[allAnnotations.size()]);
       }
       else
       {
          if (superHolder != null)
          {
             allAnnotations = superHolder.allAnnotations;
             allAnnotationsArray = superHolder.allAnnotationsArray;
          }
       }
    }
 
 }
