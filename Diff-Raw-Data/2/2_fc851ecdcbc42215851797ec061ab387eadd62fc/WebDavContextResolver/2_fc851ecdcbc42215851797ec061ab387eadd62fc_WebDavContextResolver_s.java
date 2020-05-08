 /*
  * Copyright 2008, 2009 Markus KARG
  *
  * This file is part of webdav-jaxrs.
  *
  * webdav-jaxrs is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * webdav-jaxrs is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with webdav-jaxrs.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.java.dev.webdav.jaxrs.xml;
 
 import net.java.dev.webdav.jaxrs.xml.conditions.*;
 import net.java.dev.webdav.jaxrs.xml.elements.*;
 import net.java.dev.webdav.jaxrs.xml.elements.Error;
 import net.java.dev.webdav.jaxrs.xml.properties.*;
 
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.ext.ContextResolver;
 import javax.ws.rs.ext.Provider;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * Provides support for custom extensions to WebDAV, like custom Properties and XML Elements.<br>
  * <p/>
  * WebDAV allows custom extensions for XML Elements and Properties. To enable JAX-RS to deal with these,
  * each of them must be implemented as a JAXB class and registered by passing it to the constructor of this resolver.
  * <p/>
  * This version of the class has been extended to provide the text/html mime type. This is required by tools like
  * perl-HTTP-DAV, which do not accept application/xml responses as valid WedDAV responses.
  *
  * @author Markus KARG (mkarg@users.dev.java.net)
  * @see <a href="http://www.webdav.org/specs/rfc4918.html#xml-extensibility">Chapter 17 "XML Extensibility in DAV" of RFC 2616 "Hypertext Transfer Protocol -- HTTP/1.1"</a>
  */
 @Provider
 @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML})
 public class WebDavContextResolver implements ContextResolver<JAXBContext> {
 
     private final JAXBContext context;
 
     public WebDavContextResolver() throws JAXBException {
         this(null);
     }
 
     /**
      * Creates an instance of this resolver, registering the provided custom XML
      * Elements and Properties.
      *
      * @param additionalClasses The custom extensions (JAXB classes) to be registered (can be
      *                          left blank).
      * @throws JAXBException if an error was encountered while creating the JAXBContext,
      *                       such as (but not limited to): No JAXB implementation was
      *                       discovered, Classes use JAXB annotations incorrectly, Classes
      *                       have colliding annotations (i.e., two classes with the same
      *                       type name), The JAXB implementation was unable to locate
      *                       provider-specific out-of-band information (such as additional
      *                       files generated at the development time.)
      */
     @SuppressWarnings("unchecked")
     public WebDavContextResolver(final Class<?>... additionalClasses) throws JAXBException {
         final List<Class<?>> classesInContext = new ArrayList<Class<?>>(Arrays.asList(ActiveLock.class, AllProp.class, CannotModifyProtectedProperty.class,
                 Collection.class, CreationDate.class, Depth.class, DisplayName.class, Error.class, Exclusive.class, GetContentLanguage.class,
                 GetContentLength.class, GetContentType.class, GetETag.class, GetLastModified.class, HRef.class, Include.class, Location.class,
                 LockDiscovery.class, LockEntry.class, LockInfo.class, LockRoot.class, LockScope.class, LockToken.class, LockTokenMatchesRequestUri.class,
                 LockTokenSubmitted.class, LockType.class, MultiStatus.class, NoConflictingLock.class, NoExternalEntities.class, Owner.class,
                 PreservedLiveProperties.class, Prop.class, PropertyUpdate.class, PropFind.class, PropFindFiniteDepth.class, PropName.class, PropStat.class,
                 Remove.class, ResourceType.class, Response.class, ResponseDescription.class, Set.class, Shared.class, Status.class,
                 SupportedLock.class, TimeOut.class, Write.class, FixedCreationDate.class));
 
         if (additionalClasses != null)
             classesInContext.addAll(Arrays.asList(additionalClasses));
 
         this.context = JAXBContext.newInstance(classesInContext.toArray(new Class[classesInContext.size()]));
     }
 
     /**
      * @return A single, shared context for both, WebDAV XML Elements and
      *         Properties and custom extensions.
      */
     @Override
     public JAXBContext getContext(final Class<?> cls) {
 
        if (cls.getPackage().getName().startsWith("net.java.dev.webdav.jaxrs.xml")) {
             return this.context;
         }
 
         return null;
     }
 
 }
