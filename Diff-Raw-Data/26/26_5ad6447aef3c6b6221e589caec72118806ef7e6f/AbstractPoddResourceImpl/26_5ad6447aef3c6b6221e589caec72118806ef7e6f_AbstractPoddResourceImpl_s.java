 /**
  * PODD is an OWL ontology database used for scientific project management
  * 
  * Copyright (C) 2009-2013 The University Of Queensland
  * 
  * This program is free software: you can redistribute it and/or modify it under the terms of the
  * GNU Affero General Public License as published by the Free Software Foundation, either version 3
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License along with this program.
  * If not, see <http://www.gnu.org/licenses/>.
  */
 package com.github.podd.resources;
 
 import java.util.List;
 
 import org.openrdf.model.URI;
 import org.restlet.data.Method;
 import org.restlet.data.Status;
 import org.restlet.representation.Variant;
 import org.restlet.resource.ResourceException;
 import org.restlet.resource.ServerResource;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.podd.api.PoddArtifactManager;
 import com.github.podd.api.PoddRepositoryManager;
 import com.github.podd.api.PoddSchemaManager;
 import com.github.podd.api.PoddSesameManager;
 import com.github.podd.restlet.PoddAction;
 import com.github.podd.restlet.PoddWebServiceApplication;
 
 public abstract class AbstractPoddResourceImpl extends ServerResource
 {
     protected final Logger log = LoggerFactory.getLogger(this.getClass());
     
     public AbstractPoddResourceImpl()
     {
         super();
     }
     
     /**
      * Checks the ability of the currently authenticated user to perform the given action and throws
      * an exception if the current user is not authorised for the given action.
      * 
      * @param action
      *            The PoddAction that is to be performed.
      * @throws ResourceException
      *             with Status.CLIENT_ERROR_UNAUTHORIZED (HTTP 401) if the user is not authorised to
      *             perform the given action
      */
     protected boolean checkAuthentication(final PoddAction action) throws ResourceException
     {
         // throws an error on failure
         return this.checkAuthentication(action, null, true);
     }
     
     /**
      * Checks the ability of the currently authenticated user to perform the given action and throws
      * an exception if the current user is not authorised for the given action.
      * 
      * @param action
      *            The PoddAction that is to be performed.
      * @param optionalObjectUris
      *            Collection of object URIs to be used for authorization, or an empty Collection if
      *            none are needed for authorization.
      * @throws ResourceException
      *             with Status.CLIENT_ERROR_UNAUTHORIZED (HTTP 401) if the user is not authorised to
      *             perform the given action
      */
     protected boolean checkAuthentication(final PoddAction action, final URI optionalObjectUris)
         throws ResourceException
     {
         // throws an error on failure
         return this.checkAuthentication(action, optionalObjectUris, true);
     }
     
     /**
      * Checks the ability of the currently authenticated user to perform the given action,
      * optionally throwing an exception instead of returning false in the case that the check fails.
      * 
      * @param action
      *            The PoddAction that is to be performed.
      * @param optionalObjectUris
      *            Collection of object URIs to be used for authorization, or an empty Collection if
      *            none are needed for authorization.
      * @param throwExceptionOnFailure
      *            If true, this method throws a ResourceException on failure instead of returning
      *            false
      * @return Returns true if the user is able to perform the given action on the given objects,
      *         and either throws an exception or returns false if they are not able to perform the
      *         given action, depending on the value of the throwExceptionOnFailure parameter.
      * @throws ResourceException
      *             with Status.CLIENT_ERROR_UNAUTHORIZED (HTTP 401) if the user is not authorised to
      *             perform the given action
      */
     protected boolean checkAuthentication(final PoddAction action, final URI optionalObjectUris,
             final boolean throwExceptionOnFailure) throws ResourceException
     {
         if(this.getPoddApplication().authenticate(action, this.getRequest(), this.getResponse(), optionalObjectUris))
         {
             return true;
         }
         else if(throwExceptionOnFailure)
         {
             this.log.warn("Client unauthorized. Throwing a ResourceException");
             throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED, action.getErrorMessage());
         }
         else
         {
             // do not log this, as it is a normal part of an operation, as evidenced by not wanting
             // to throw an exception
             return false;
         }
     }
     
     /**
      * Sets the data handler for this resource based on the application level data handler.
      * 
      * NOTE: This requires the application to be an instance of OasWebServiceApplication for it to
      * function correctly
      */
     @Override
     public synchronized void doInit()
     {
         super.doInit();
     }
     
     public PoddWebServiceApplication getPoddApplication()
     {
         final PoddWebServiceApplication application = (PoddWebServiceApplication)super.getApplication();
         
         return application;
     }
     
     public PoddArtifactManager getPoddArtifactManager()
     {
         return this.getPoddApplication().getPoddArtifactManager();
     }
     
     public PoddRepositoryManager getPoddRepositoryManager()
     {
         return this.getPoddApplication().getPoddRepositoryManager();
     }
     
     public PoddSchemaManager getPoddSchemaManager()
     {
         return this.getPoddApplication().getPoddSchemaManager();
     }
     
     public PoddSesameManager getPoddSesameManager()
     {
         return this.getPoddApplication().getPoddArtifactManager().getSesameManager();
     }
     
     /**
      * Overriding broken ServerResource.getVariants method
      * 
      * NOTE: This is not a caching implementation, so the way it is used may cause it to be a
      * performance bottleneck.
      */
     @Override
     protected List<Variant> getVariants(final Method method)
     {
         return super.getVariants(method);
     }
     
 }
