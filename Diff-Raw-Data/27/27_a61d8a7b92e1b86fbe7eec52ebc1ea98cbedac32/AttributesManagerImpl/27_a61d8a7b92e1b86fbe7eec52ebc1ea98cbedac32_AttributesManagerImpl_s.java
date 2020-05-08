 /*
 * JBoss, a division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
 
 package org.picketlink.idm.impl.api.session.managers;
 
 import org.picketlink.idm.api.AttributesManager;
 import org.picketlink.idm.api.IdentityType;
 import org.picketlink.idm.api.User;
 import org.picketlink.idm.api.AttributeDescription;
 import org.picketlink.idm.api.CredentialType;
 import org.picketlink.idm.api.Credential;
 import org.picketlink.idm.api.Attribute;
 import org.picketlink.idm.api.Group;
 import org.picketlink.idm.common.exception.IdentityException;
 import org.picketlink.idm.spi.model.IdentityObjectCredential;
 import org.picketlink.idm.spi.model.IdentityObjectAttribute;
 import org.picketlink.idm.spi.model.IdentityObject;
 import org.picketlink.idm.spi.configuration.metadata.IdentityObjectAttributeMetaData;
 import org.picketlink.idm.impl.api.attribute.IdentityObjectAttributeMetaDataImpl;
 import org.picketlink.idm.impl.api.SimpleAttribute;
 import org.picketlink.idm.impl.api.PasswordCredential;
 import org.picketlink.idm.impl.api.SimpleCredentialType;
 import org.picketlink.idm.impl.api.session.IdentitySessionImpl;
 
 import java.util.Set;
 import java.util.Map;
 import java.util.HashMap;
 import java.io.Serializable;
 
 /**
  * @author <a href="mailto:boleslaw.dawidowicz at redhat.com">Boleslaw Dawidowicz</a>
  * @version : 0.1 $
  */
 public class AttributesManagerImpl extends AbstractManager implements AttributesManager, Serializable
 {
    private static final long serialVersionUID = 1285532201632609092L;
 
    public AttributesManagerImpl(IdentitySessionImpl session)
    {
       super(session);
    }
 
    public AttributeDescription getAttributeDescription(IdentityType identityType, String name)
    {
 
       checkNotNullArgument(identityType, "IdentityType");
       checkNotNullArgument(name, "Attribute name");
 
       Map<String, IdentityObjectAttributeMetaData> mdMap =
          getRepository().getAttributesMetaData(getInvocationContext(), createIdentityObject(identityType).getIdentityType());
 
       if (mdMap != null && mdMap.containsKey(name))
       {
          IdentityObjectAttributeMetaData attributeMD = mdMap.get(name);
          if (attributeMD instanceof AttributeDescription)
          {
             return (AttributeDescription)attributeMD;
          }
          else
          {
             return new IdentityObjectAttributeMetaDataImpl(attributeMD);
          }
       }
       
       return null;
 
    }
 
    public AttributeDescription getAttributeDescription(String id, String attributeName)
    {
       checkNotNullArgument(id, "IdentityType Id");
       checkNotNullArgument(attributeName, "Attribute name");
 
       IdentityType identityType = createIdentityTypeFromId(id);
 
       return getAttributeDescription(identityType, attributeName);
       
    }
 
    public Map<String, AttributeDescription> getSupportedAttributesDescriptions(IdentityType identityType)
    {
 
       checkNotNullArgument(identityType, "IdentityType");
 
       Map<String, IdentityObjectAttributeMetaData> mdMap =
          getRepository().getAttributesMetaData(getInvocationContext(), createIdentityObject(identityType).getIdentityType());
 
       Map<String, AttributeDescription> descriptionMap = new HashMap<String, AttributeDescription>();
 
       if (mdMap != null)
       {
          for (IdentityObjectAttributeMetaData attributeMD : mdMap.values())
          {
             if (attributeMD instanceof AttributeDescription)
             {
                descriptionMap.put(attributeMD.getName(), (AttributeDescription)attributeMD);
             }
             else
             {
                descriptionMap.put(attributeMD.getName(), new IdentityObjectAttributeMetaDataImpl(attributeMD));
             }
          }
       }
 
       return descriptionMap;
    }
 
    public Map<String, AttributeDescription> getSupportedAttributesDescriptions(String id)
    {
       checkNotNullArgument(id, "Id (Group) or name (User)");
 
       IdentityType identityType = createIdentityTypeFromId(id);
 
       return getSupportedAttributesDescriptions(identityType);
    }
 
    public Set<String> getSupportedAttributeNames(IdentityType identityType) throws IdentityException
    {
       checkNotNullArgument(identityType, "IdentityType");
 
       return getRepository().getSupportedAttributeNames(getInvocationContext(), createIdentityObject(identityType).getIdentityType());
    }
 
    public Set<String> getSupportedAttributeNames(String id) throws IdentityException
    {
       checkNotNullArgument(id, "Id (Group) or name (User)");
 
       IdentityType identityType = createIdentityTypeFromId(id);
 
       return getSupportedAttributeNames(identityType);
    }
 
    public Map<String, Attribute> getAttributes(IdentityType identityType) throws IdentityException
    {
       checkNotNullArgument(identityType, "IdentityType");
 
       if (cache != null)
       {
          Map<String, Attribute> attributes = cache.getAttributes(cacheNS, identityType.getKey());
          if (attributes != null)
          {
             return attributes;
          }
       }
 
       Map<String, IdentityObjectAttribute> map = getRepository().getAttributes(getInvocationContext(), createIdentityObject(identityType));
 
       Map<String, Attribute> newMap = new HashMap<String, Attribute>();
 
       for (Map.Entry<String, IdentityObjectAttribute> entry : map.entrySet())
       {
          newMap.put(entry.getKey(), convertAttribute(entry.getValue()));
       }
 
       if (cache != null)
       {
          cache.putAttributes(cacheNS, identityType.getKey(), newMap);
       }
       return newMap;
    }
 
    public Map<String, Attribute> getAttributes(String id) throws IdentityException
    {
 
       checkNotNullArgument(id, "Id (Group) or name (User)");
 
 
       IdentityType identityType = createIdentityTypeFromId(id);
 
       return getAttributes(identityType);
    }
 
    public void updateAttributes(IdentityType identity, Attribute[] attributes) throws IdentityException
    {
       checkNotNullArgument(identity, "IdentityType");
       checkNotNullArgument(attributes, "Attributes");
 
       preAttributesUpdate(identity, attributes);
 
       getRepository().updateAttributes(getInvocationContext(), createIdentityObject(identity), convertAttributes(attributes));
 
       if (cache != null)
       {
         Map<String, Attribute> attrMap = new HashMap<String, Attribute>();

         for (Attribute attribute : attributes)
         {
            attrMap.put(attribute.getName(), attribute);
         }

         cache.putAttributes(cacheNS, identity.getKey(), attrMap);
       }
 
       postAttributesUpdate(identity, attributes);
 
    }
 
    public void updateAttributes(String id, Attribute[] attributes) throws IdentityException
    {
       checkNotNullArgument(id, "Id (Group) or name (User)");
       checkNotNullArgument(attributes, "Attributes");
 
       IdentityType identityType = createIdentityTypeFromId(id);
 
       updateAttributes(identityType, attributes);
 
    }
 
    public Attribute getAttribute(IdentityType identityType, String attributeName) throws IdentityException
    {
       checkNotNullArgument(identityType, "IdentityType");
       checkNotNullArgument(attributeName, "Attribute name");
 
       return getAttributes(identityType).get(attributeName);
    }
 
    public Attribute getAttribute(String id, String attributeName) throws IdentityException
    {
       checkNotNullArgument(id, "Id (Group) or name (User)");
       checkNotNullArgument(attributeName, "Attribute name");
 
       IdentityType identityType = createIdentityTypeFromId(id);
 
       return getAttribute(identityType, attributeName);
    }
 
    public void addAttribute(IdentityType identityType, String attributeName, Object[] values) throws IdentityException
    {
       checkNotNullArgument(identityType, "IdentityType");
       checkNotNullArgument(attributeName, "Attribute name");
       checkNotNullArgument(values, "Attribute values");
 
       Attribute[] attrs = new Attribute[]{new SimpleAttribute(attributeName, values)};
 
 
       addAttributes(identityType, attrs);
 
    }
 
    public void addAttributes(String id, Attribute[] attributes) throws IdentityException
    {
       checkNotNullArgument(id, "Id (Group) or name (User)");
       checkNotNullArgument(attributes, "Attributes");
 
       IdentityType identityType = createIdentityTypeFromId(id);
 
       addAttributes(identityType, attributes);
 
    }
 
    public void addAttribute(IdentityType identityType, String attributeName, Object value) throws IdentityException
    {
       checkNotNullArgument(identityType, "IdentityType");
       checkNotNullArgument(attributeName, "Attribute name");
       checkNotNullArgument(value, "Attribute value");
 
       Attribute[] attrs = new Attribute[]{new SimpleAttribute(attributeName, value)};
 
 
       addAttributes(identityType, attrs);
 
    }
 
    public void addAttribute(String id, String attributeName, Object[] values) throws IdentityException
    {
       checkNotNullArgument(id, "Id (Group) or name (User)");
       checkNotNullArgument(attributeName, "Attribute name");
       checkNotNullArgument(values, "Attribute values");
 
       IdentityType identityType = createIdentityTypeFromId(id);
 
       addAttribute(identityType, attributeName, values);
 
    }
 
    public void addAttribute(String id, String attributeName, Object value) throws IdentityException
    {
       checkNotNullArgument(id, "Id (Group) or name (User)");
       checkNotNullArgument(attributeName, "Attribute name");
       checkNotNullArgument(value, "Attribute value");
 
       IdentityType identityType = createIdentityTypeFromId(id);
 
       addAttribute(identityType, attributeName, value);
 
    }
 
    public void addAttributes(IdentityType identityType, Attribute[] attributes) throws IdentityException
    {
       checkNotNullArgument(identityType, "IdentityType");
       checkNotNullArgument(attributes, "Attributes");
 
 
       preAttributesAdd(identityType, attributes);
 
       getRepository().addAttributes(getInvocationContext(), createIdentityObject(identityType), convertAttributes(attributes));
 
       if (cache != null)
       {
          // Grab the new profile and persist in cache
          cache.invalidateAttributes(cacheNS, identityType.getKey());
         cache.putAttributes(cacheNS, identityType.getKey(), this.getAttributes(identityType));
       }
 
       postAttributesAdd(identityType, attributes);
    }
 
 
 
    public void removeAttributes(IdentityType identityType, String[] attributeNames) throws IdentityException
    {
       checkNotNullArgument(identityType, "IdentityType");
       checkNotNullArgument(attributeNames, "Attribute names");
 
       preAttributesRemove(identityType, attributeNames);
 
       getRepository().removeAttributes(getInvocationContext(), createIdentityObject(identityType), attributeNames);
 
       if (cache != null)
       {
          // Grab the new profile and persist in cache
          cache.invalidateAttributes(cacheNS, identityType.getKey());
         cache.putAttributes(cacheNS, identityType.getKey(), this.getAttributes(identityType));
       }
 
       postAttributesRemove(identityType, attributeNames);
    }
 
    public void removeAttributes(String id, String[] attributeNames) throws IdentityException
    {
       checkNotNullArgument(id, "Id (Group) or name (User)");
       checkNotNullArgument(attributeNames, "Attribute names");
 
       IdentityType identityType = createIdentityTypeFromId(id);
 
       removeAttributes(identityType, attributeNames);
 
    }
 
    public boolean hasPassword(User user) throws IdentityException
    {
       checkNotNullArgument(user, "User");
       return getRepository().getSupportedFeatures().isCredentialSupported(createIdentityObject(user).getIdentityType(), PasswordCredential.TYPE);
    }
 
    public boolean validatePassword(User user, String password) throws IdentityException
    {
       checkNotNullArgument(user, "User");
       checkNotNullArgument(password, "Password");
       return getRepository().validateCredential(getInvocationContext(), createIdentityObject(user), new PasswordCredential(password));
    }
 
    public void updatePassword(User user, String password) throws IdentityException
    {
       checkNotNullArgument(user, "User");
       checkNotNullArgument(password, "Password");
 
       preCredentialUpdate(user, new PasswordCredential(password));
 
       getRepository().updateCredential(getInvocationContext(), createIdentityObject(user), new PasswordCredential(password));
 
       postCredentialUpdate(user, new PasswordCredential(password));
 
    }
 
    public boolean isCredentialTypeSupported(CredentialType credentialType) throws IdentityException
    {
       checkNotNullArgument(credentialType, "CredentialType");
 
       return getRepository().getSupportedFeatures().isCredentialSupported(getUserObjectType(), new SimpleCredentialType(credentialType.getName()));
    }
 
    public boolean validateCredentials(User user, Credential[] credentials) throws IdentityException
    {
       checkNotNullArgument(user, "User");
       checkNotNullArgument(credentials, "Credentials");
 
       for (Credential credential : credentials)
       {
          IdentityObjectCredential ioc = null;
 
          //Handle only those credentials that implement SPI
 
          if (!(credential instanceof IdentityObjectCredential))
          {
             throw new IdentityException("Unsupported Credential implementation: " + credential.getClass());
          }
 
          ioc = (IdentityObjectCredential)credential;
 
          // All credentials must pass
 
          if (!getRepository().validateCredential(getInvocationContext(), createIdentityObject(user), ioc))
          {
             return false;
          }
       }
 
       return true;
    }
 
    public void updateCredential(User user, Credential credential) throws IdentityException
    {
       checkNotNullArgument(user, "User");
       checkNotNullArgument(credential, "Credential");
 
       if (credential instanceof IdentityObjectCredential)
       {
          preCredentialUpdate(user, credential);
 
          getRepository().updateCredential(getInvocationContext(), createIdentityObject(user), (IdentityObjectCredential)credential);
 
          postCredentialUpdate(user, credential);
       }
       else
       {
          throw new IdentityException("Unsupported Credential implementation: " + credential.getClass());
       }
    }
 
    public User findUserByUniqueAttribute(String attributeName, Object value) throws IdentityException
    {
       checkNotNullArgument(attributeName, "Attribute name");
       checkNotNullArgument(value, "Attribute value");
 
       //TODO: cache
 
       IdentityObject io = getRepository().findIdentityObjectByUniqueAttribute(getInvocationContext(), getUserObjectType(), new SimpleAttribute(attributeName, value));
 
       if (io == null)
       {
          return null;
       }
 
       return createUser(io);
    }
 
    public Group findGroupByUniqueAttribute(String groupType, String attributeName, Object value) throws IdentityException
    {
       checkNotNullArgument(groupType, "GroupType");
       checkNotNullArgument(attributeName, "Attribute name");
       checkNotNullArgument(value, "Attribute value");
 
       //TODO: cache
 
       IdentityObject io = getRepository().findIdentityObjectByUniqueAttribute(getInvocationContext(),
          getIdentityObjectType(groupType),
          new SimpleAttribute(attributeName, value));
 
       if (io == null)
       {
          return null;
       }
 
       return createGroup(io);
    }
 }
