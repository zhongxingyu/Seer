 // 
 // Copyright (c) 2004, Caltha - Gajda, Krzewski, Mach, Potempski Sp.J. 
 // All rights reserved. 
 // 
 // Redistribution and use in source and binary forms, with or without modification,  
 // are permitted provided that the following conditions are met: 
 // 
 // * Redistributions of source code must retain the above copyright notice,  
 //       this list of conditions and the following disclaimer. 
 // * Redistributions in binary form must reproduce the above copyright notice,  
 //       this list of conditions and the following disclaimer in the documentation  
 //       and/or other materials provided with the distribution. 
 // * Neither the name of the Caltha - Gajda, Krzewski, Mach, Potempski Sp.J.  
 //       nor the names of its contributors may be used to endorse or promote products  
 //       derived from this software without specific prior written permission. 
 // 
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"  
 // AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED  
 // WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 // IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,  
 // INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,  
 // BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
 // OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,  
 // WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)  
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE  
 // POSSIBILITY OF SUCH DAMAGE. 
 // 
  
 package net.cyklotron.cms.poll;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.objectledge.coral.BackendException;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.schema.AttributeDefinition;
 import org.objectledge.coral.schema.ResourceClass;
 import org.objectledge.coral.security.Role;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.InvalidResourceNameException;
 import org.objectledge.coral.store.ModificationNotPermitedException;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.coral.store.ValueRequiredException;
 
 import net.cyklotron.cms.CmsConstants;
 import net.cyklotron.cms.CmsData;
 import net.cyklotron.cms.CmsNodeResourceImpl;
 import net.cyklotron.cms.site.SiteResource;
 
 /**
  * An implementation of <code>cms.poll.vote</code> Coral resource class.
  *
  * @author Coral Maven plugin
  */
 public class VoteResourceImpl
     extends CmsNodeResourceImpl
     implements VoteResource
 {
     // class variables /////////////////////////////////////////////////////////
 
     /** Class variables initialization status. */
     private static boolean definitionsInitialized;
 	
     /** The AttributeDefinition object for the <code>moderator</code> attribute. */
     private static AttributeDefinition moderatorDef;
 
     /** The AttributeDefinition object for the <code>senderAddress</code> attribute. */
     private static AttributeDefinition senderAddressDef;
 
 	// custom injected fields /////////////////////////////////////////////////
 	
     /** The PollService. */
     protected PollService pollService;
 
     // initialization /////////////////////////////////////////////////////////
 
     /**
      * Creates a blank <code>cms.poll.vote</code> resource wrapper.
      *
      * <p>This constructor should be used by the handler class only. Use 
      * <code>load()</code> and <code>create()</code> methods to create
      * instances of the wrapper in your application code.</p>
      *
      * @param pollService the PollService.
      */
     public VoteResourceImpl(PollService pollService)
     {
         this.pollService = pollService;
     }
 
     // static methods ////////////////////////////////////////////////////////
 
     /**
      * Retrieves a <code>cms.poll.vote</code> resource instance from the store.
      *
      * <p>This is a simple wrapper of StoreService.getResource() method plus
      * the typecast.</p>
      *
      * @param session the CoralSession
      * @param id the id of the object to be retrieved
      * @return a resource instance.
      * @throws EntityDoesNotExistException if the resource with the given id does not exist.
      */
     public static VoteResource getVoteResource(CoralSession session, long id)
         throws EntityDoesNotExistException
     {
         Resource res = session.getStore().getResource(id);
         if(!(res instanceof VoteResource))
         {
             throw new IllegalArgumentException("resource #"+id+" is "+
                                                res.getResourceClass().getName()+
                                                " not cms.poll.vote");
         }
         return (VoteResource)res;
     }
 
     /**
      * Creates a new <code>cms.poll.vote</code> resource instance.
      *
      * @param session the CoralSession
      * @param name the name of the new resource
      * @param parent the parent resource.
      * @return a new VoteResource instance.
      * @throws InvalidResourceNameException if the name argument contains illegal characters.
      */
     public static VoteResource createVoteResource(CoralSession session, String name, Resource
         parent)
         throws InvalidResourceNameException
     {
         try
         {
             ResourceClass rc = session.getSchema().getResourceClass("cms.poll.vote");
             Map attrs = new HashMap();
             Resource res = session.getStore().createResource(name, parent, rc, attrs);
             if(!(res instanceof VoteResource))
             {
                 throw new BackendException("incosistent schema: created object is "+
                                            res.getClass().getName());
             }
             return (VoteResource)res;
         }
         catch(EntityDoesNotExistException e)
         {
             throw new BackendException("incompatible schema change", e);
         }
         catch(ValueRequiredException e)
         {
             throw new BackendException("incompatible schema change", e);
         }
     }
 
     // public interface //////////////////////////////////////////////////////
  
     /**
      * Returns the value of the <code>moderator</code> attribute.
      *
      * @return the value of the <code>moderator</code> attribute.
      */
     public Role getModerator()
     {
         return (Role)getInternal(moderatorDef, null);
     }
     
     /**
      * Returns the value of the <code>moderator</code> attribute.
      *
      * @param defaultValue the value to return if the attribute is undefined.
      * @return the value of the <code>moderator</code> attribute.
      */
     public Role getModerator(Role defaultValue)
     {
         return (Role)getInternal(moderatorDef, defaultValue);
     }    
 
     /**
      * Sets the value of the <code>moderator</code> attribute.
      *
      * @param value the value of the <code>moderator</code> attribute,
      *        or <code>null</code> to remove value.
      */
     public void setModerator(Role value)
     {
         try
         {
             if(value != null)
             {
                 set(moderatorDef, value);
             }
             else
             {
                 unset(moderatorDef);
             }
         }
         catch(ModificationNotPermitedException e)
         {
             throw new BackendException("incompatible schema change",e);
         }
         catch(ValueRequiredException e)
         {
             throw new BackendException("incompatible schema change",e);
         }
     }
    
 	/**
 	 * Checks if the value of the <code>moderator</code> attribute is defined.
 	 *
 	 * @return <code>true</code> if the value of the <code>moderator</code> attribute is defined.
 	 */
     public boolean isModeratorDefined()
 	{
 	    return isDefined(moderatorDef);
 	}
  
     /**
      * Returns the value of the <code>senderAddress</code> attribute.
      *
      * @return the value of the <code>senderAddress</code> attribute.
      */
     public String getSenderAddress()
     {
         return (String)getInternal(senderAddressDef, null);
     }
     
     /**
      * Returns the value of the <code>senderAddress</code> attribute.
      *
      * @param defaultValue the value to return if the attribute is undefined.
      * @return the value of the <code>senderAddress</code> attribute.
      */
     public String getSenderAddress(String defaultValue)
     {
         return (String)getInternal(senderAddressDef, defaultValue);
     }    
 
     /**
      * Sets the value of the <code>senderAddress</code> attribute.
      *
      * @param value the value of the <code>senderAddress</code> attribute,
      *        or <code>null</code> to remove value.
      */
     public void setSenderAddress(String value)
     {
         try
         {
             if(value != null)
             {
                 set(senderAddressDef, value);
             }
             else
             {
                 unset(senderAddressDef);
             }
         }
         catch(ModificationNotPermitedException e)
         {
             throw new BackendException("incompatible schema change",e);
         }
         catch(ValueRequiredException e)
         {
             throw new BackendException("incompatible schema change",e);
         }
     }
    
 	/**
 	 * Checks if the value of the <code>senderAddress</code> attribute is defined.
 	 *
 	 * @return <code>true</code> if the value of the <code>senderAddress</code> attribute is defined.
 	 */
     public boolean isSenderAddressDefined()
 	{
 	    return isDefined(senderAddressDef);
 	}
   
     // @custom methods ///////////////////////////////////////////////////////
     // @extends node
     // @import net.cyklotron.cms.CmsData
     // @import net.cyklotron.cms.poll.PollService
     // @import org.objectledge.coral.session.CoralSession
     // @import org.objectledge.coral.security.Subject
     // @import net.cyklotron.cms.CmsConstants
     // @field PollService pollService
     
     /**
      * Checks if this resource can be viewed at the given time.
      */
     public boolean isValid(java.util.Date time)
     {
        return true;
     }
 
     /**
      * Checks if this resource can be view.
      */
     public boolean canView(CoralSession coralSession, Subject subject)
     {
         return true;
     }
 
     /**
      * Checks if the specified subject can modify this resource.
      */
     public boolean canModify(CoralSession coralSession, Subject subject)
     {
         throw new UnsupportedOperationException();
     }
     
     /**
      * Checks if the specified subject can remove this resource.
      */
     public boolean canRemove(CoralSession coralSession, Subject subject)
     {
         throw new UnsupportedOperationException();
     }
     
     /**
      * Checks if the specified subject can add children to this resource.
      */
     public boolean canAddChild(CoralSession coralSession, Subject subject)
     {
         throw new UnsupportedOperationException();
     }
 
     /**
      * Checks if the specified subject can view this resource at the given time.
      */
     public boolean canView(CoralSession coralSession, Subject subject, java.util.Date time)
     {
         if(!canView(coralSession, subject))
         {
             return false;
         }
         return isValid(time);
     }
 
     /**
      * Checks if the specified subject can view this resource
      */
     public boolean canView(CoralSession coralSession, CmsData data, Subject subject)
     {
         if(data.getBrowseMode().equals(CmsConstants.BROWSE_MODE_ADMINISTER))
         {
             return canView(coralSession, subject);
         }
         else
         {
             return canView(coralSession, subject, data.getDate());
         }
     }
 
     /**
      * Returns the store flag of the field.
      *
      * @return the store flag.
      */
     public boolean isStored(String fieldName)
     {
         return false;
     }
     
     /**
      * Returns the indexed flag of the field.
      *
      * @return the indexed flag.
      */
     public boolean isIndexed(String fieldName)
     {
         return false;
     }
     
    // @custom methods ///////////////////////////////////////////////////////
     
     public SiteResource getSite()
     {
         Resource r = getParent();
         while(r != null && !(r instanceof SiteResource))
         {
             r = r.getParent();
         }
         return (SiteResource)r;
     }
 }
