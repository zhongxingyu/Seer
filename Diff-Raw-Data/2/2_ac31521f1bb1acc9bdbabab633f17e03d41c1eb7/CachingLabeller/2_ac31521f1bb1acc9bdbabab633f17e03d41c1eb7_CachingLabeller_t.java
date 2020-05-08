 package org.xbrlapi.aspects.alt;
 
 import java.net.URI;
 
 import org.apache.log4j.Logger;
 import org.xbrlapi.utilities.XBRLException;
 
 /**
  * <p>
  * A generic aspect labeller that uses a caching system.  It is made specific
  * to a given aspect by setting its labeller property as part of the constructor.
  * </p>
  * 
  * @author Geoff Shuetrim (geoff@galexy.net)
  */
 public class CachingLabeller extends LabellerImpl implements Labeller {
 
     /**
      * 
      */
     private static final long serialVersionUID = 5414469666286671643L;
 
     protected final static Logger logger = Logger
     .getLogger(CachingLabeller.class);
     
     private Labeller labeller;
     
     /**
      * The aspect value label caching system.
      */
     private LabelCache cache;
     
     /**
      * @param aspect The aspect to be a labeller for.
      */
     public CachingLabeller(LabelCache cache, Labeller labeller) throws XBRLException {
         super();
         if (labeller == null) throw new XBRLException("The labeller must not be null.");
         this.labeller = labeller;
         if (cache == null) throw new XBRLException("The label cache must not be null.");
         this.cache = cache;
     }
 
     /**
      * @return the aspect of the embedded labeller.
      * @see Labeller#getAspect()
      */
     @Override
     public Aspect getAspect() {
         return labeller.getAspect();
     }
     
     /**
      * @see Labeller#getAspectLabel(String, URI, URI)
      */
     @Override
     public String getAspectLabel(String locale, URI resourceRole, URI linkRole) {
         return labeller.getAspectLabel(locale, resourceRole, linkRole);
     }
     
     /**
      * @see Labeller#getAspectValueLabel(AspectValue, String, URI, URI)
      */
     @Override
     public String getAspectValueLabel(AspectValue value, String locale,
             URI resourceRole, URI linkRole) {
         
         URI aspectId = getAspect().getId();
         String valueId = value.getId();
         
         try {
             String label = cache.getLabel(aspectId, valueId, locale, resourceRole, linkRole);
             if (label != null) {
                 return label;
             }
             label = labeller.getAspectValueLabel(value, locale, resourceRole, linkRole);
            this.cache.cacheLabel(aspectId, valueId, locale, resourceRole,linkRole, label);
             return label;
         } catch (Throwable e) {
             String label = labeller.getAspectValueLabel(value,locale,resourceRole,linkRole);
             try {
                 this.cache.cacheLabel(aspectId, valueId,locale, resourceRole,linkRole, label);
             } catch (XBRLException x) {
                 ; // Ignore the exception - effectively bailing out of the caching operation.
             }
             return label;
         }
 
     }
 
 }
