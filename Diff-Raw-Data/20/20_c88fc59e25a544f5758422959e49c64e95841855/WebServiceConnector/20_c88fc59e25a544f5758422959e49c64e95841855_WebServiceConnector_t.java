 /*
  * ====================
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 2011 Tirasa. All rights reserved.
  *
  * The contents of this file are subject to the terms of the Common Development
  * and Distribution License("CDDL") (the "License").  You may not use this file
  * except in compliance with the License.
  *
  * You can obtain a copy of the License at
  * http://connid.googlecode.com/svn/trunk/legal/license.txt
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  * When distributing the Covered Code, include this CDDL Header Notice in each
  * file and include the License file at connid/legal/license.txt.
  * If applicable, add the following below this CDDL Header, with the fields
  * enclosed by brackets [] replaced by your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  * ====================
  */
 package org.connid.bundles.soap;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import org.connid.bundles.soap.exceptions.ProvisioningException;
 import org.connid.bundles.soap.provisioning.interfaces.Provisioning;
 import org.connid.bundles.soap.to.WSAttribute;
 import org.connid.bundles.soap.to.WSAttributeValue;
 import org.connid.bundles.soap.to.WSChange;
 import org.connid.bundles.soap.to.WSUser;
 import org.connid.bundles.soap.utilities.AttributeType;
 import org.connid.bundles.soap.utilities.Operand;
 import org.identityconnectors.common.logging.Log;
 import org.identityconnectors.common.security.GuardedByteArray;
 import org.identityconnectors.common.security.GuardedString;
 import org.identityconnectors.framework.common.exceptions.ConnectorException;
 import org.identityconnectors.framework.common.objects.Attribute;
 import org.identityconnectors.framework.common.objects.AttributeBuilder;
 import org.identityconnectors.framework.common.objects.AttributeInfo;
 import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
 import org.identityconnectors.framework.common.objects.AttributeUtil;
 import org.identityconnectors.framework.common.objects.ConnectorObject;
 import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
 import org.identityconnectors.framework.common.objects.Name;
 import org.identityconnectors.framework.common.objects.ObjectClass;
 import org.identityconnectors.framework.common.objects.ObjectClassInfo;
 import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder;
 import org.identityconnectors.framework.common.objects.OperationOptions;
 import org.identityconnectors.framework.common.objects.OperationalAttributeInfos;
 import org.identityconnectors.framework.common.objects.OperationalAttributes;
 import org.identityconnectors.framework.common.objects.ResultsHandler;
 import org.identityconnectors.framework.common.objects.Schema;
 import org.identityconnectors.framework.common.objects.SchemaBuilder;
 import org.identityconnectors.framework.common.objects.SyncDeltaBuilder;
 import org.identityconnectors.framework.common.objects.SyncDeltaType;
 import org.identityconnectors.framework.common.objects.SyncResultsHandler;
 import org.identityconnectors.framework.common.objects.SyncToken;
 import org.identityconnectors.framework.common.objects.Uid;
 import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
 import org.identityconnectors.framework.spi.Configuration;
 import org.identityconnectors.framework.spi.Connector;
 import org.identityconnectors.framework.spi.ConnectorClass;
 import org.identityconnectors.framework.spi.PoolableConnector;
 import org.identityconnectors.framework.spi.operations.AuthenticateOp;
 import org.identityconnectors.framework.spi.operations.CreateOp;
 import org.identityconnectors.framework.spi.operations.DeleteOp;
 import org.identityconnectors.framework.spi.operations.ResolveUsernameOp;
 import org.identityconnectors.framework.spi.operations.SchemaOp;
 import org.identityconnectors.framework.spi.operations.SearchOp;
 import org.identityconnectors.framework.spi.operations.SyncOp;
 import org.identityconnectors.framework.spi.operations.TestOp;
 import org.identityconnectors.framework.spi.operations.UpdateOp;
 
 @ConnectorClass(displayNameKey = "SOAP_CONNECTOR",
 configurationClass = WebServiceConfiguration.class)
 public class WebServiceConnector implements
         PoolableConnector,
         AuthenticateOp,
         CreateOp,
         DeleteOp,
         SchemaOp,
         SearchOp<Operand>,
         SyncOp,
         TestOp,
         UpdateOp,
         ResolveUsernameOp {
 
     /**
      * Setup logging for the {@link WebServiceConnector}.
      */
     private static final Log LOG = Log.getLog(WebServiceConnector.class);
 
     /**
      * Place holder for the Connection created in the init method.
      */
     private WebServiceConnection connection;
 
     /**
      * Place holder for the {@link Configuration} passed into the init() method {@link WebServiceConnector#init}.
      */
     private WebServiceConfiguration config;
 
     /**
      * Schema.
      */
     private Schema schema = null;
 
     /**
      * Web Service Attributes.
      */
     private HashMap<String, WSAttribute> wsAttributes = null;
 
     /**
      * Gets the Configuration context for this connector.
      */
     @Override
     public Configuration getConfiguration() {
         return this.config;
     }
 
     /**
      * Callback method to receive the {@link Configuration}.
      *
      * @param cfg connector configuration
      * @see Connector#init
      */
     @Override
     public void init(final Configuration cfg) {
         LOG.ok("Connector initialization");
 
         this.config = (WebServiceConfiguration) cfg;
         this.connection = new WebServiceConnection(this.config);
     }
 
     /**
      * Disposes of the {@link WebServiceConnector}'s resources.
      *
      * @see Connector#dispose()
      */
     @Override
     public void dispose() {
         LOG.ok("Dispose connector resources");
 
         config = null;
 
         if (connection != null) {
             connection.dispose();
         }
 
         connection = null;
     }
 
     /**
      * Checks if resource is alive.
      *
      * @see Connector#test()
      */
     @Override
     public void checkAlive() {
         LOG.ok("Connection test");
 
         connection.test();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Uid authenticate(
             final ObjectClass objectClass,
             final String username,
             final GuardedString password,
             final OperationOptions options) {
 
         LOG.ok("User uthentication");
 
         // check objectclass
         if (objectClass == null || (!objectClass.equals(ObjectClass.ACCOUNT))) {
             throw new IllegalArgumentException("Invalid objectclass");
         }
 
         // check creadentials
         if (username == null || password == null) {
             throw new IllegalArgumentException("Invalid credentuals");
         }
 
         // get web service client
         Provisioning provisioning = connection.getProvisioning();
         if (provisioning == null) {
             throw new IllegalStateException("Web Service client not found");
         }
 
         try {
             return new Uid(provisioning.authenticate(username, password.toString()));
         } catch (ProvisioningException e) {
             throw new ConnectorException("Authentication failed", e);
         } catch (Throwable t) {
             throw new ConnectorException("Communication exception", t);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Uid create(
             final ObjectClass objClass,
             final Set<Attribute> attrs,
             final OperationOptions options) {
 
         LOG.ok("Account creation");
 
         // check objectclass
         if (objClass == null || (!objClass.equals(ObjectClass.ACCOUNT))) {
             throw new IllegalArgumentException("Invalid objectclass");
         }
 
         // check attributes
         if (attrs == null || attrs.isEmpty()) {
             throw new IllegalArgumentException("No attribute specified");
         }
 
         // get web service client
         Provisioning provisioning = connection.getProvisioning();
         if (provisioning == null) {
             throw new IllegalStateException("Web Service client not found");
         }
 
         // get account name
         Name name = AttributeUtil.getNameFromAttributes(attrs);
         if (name == null) {
             throw new IllegalArgumentException("No name specified");
         }
 
         LOG.ok("Account to be created: " + name.getNameValue());
 
         // to be user in order to pass information to the web service
         final List<WSAttributeValue> attributes =
                 new ArrayList<WSAttributeValue>();
 
         WSAttributeValue wsAttributeValue;
         WSAttribute wsAttribute;
 
         // retrieve attributes
         for (Attribute attr : attrs) {
 
             wsAttribute = new WSAttribute(attr.getName());
 
             if (attr.is(Name.NAME)) {
                 wsAttribute.setKey(true);
                 wsAttribute.setNullable(false);
             }
 
             if (attr.is(OperationalAttributeInfos.PASSWORD.getName())) {
                 wsAttribute.setName(OperationalAttributeInfos.PASSWORD.getName());
                 wsAttribute.setPassword(true);
             }
 
             if (LOG.isOk()) {
                 LOG.ok(
                         "\nAttribute: "
                         + "\n\tName: " + wsAttribute.getName()
                         + "\n\tType: " + wsAttribute.getType()
                         + "\n\tIsKey: " + wsAttribute.isKey()
                         + "\n\tIsPassword: " + wsAttribute.isPassword());
             }
 
             wsAttributeValue = new WSAttributeValue(wsAttribute);
             attributes.add(wsAttributeValue);
 
             List value = attr.getValue();
 
             if (value != null && value.size() == 1
                     && (value.get(0) instanceof GuardedString
                     || value.get(0) instanceof GuardedByteArray)) {
 
                 wsAttributeValue.setValues(Collections.singletonList(value.toString()));
             } else {
                 wsAttributeValue.setValues(value);
             }
         }
 
         LOG.ok("\nUser " + name.getNameValue() + "\n\tattributes: " + attributes.size());
 
         try {
             // user creation
             provisioning.create(attributes);
         } catch (ProvisioningException e) {
             throw new ConnectorException("Creation failed", e);
         } catch (Throwable t) {
             throw new ConnectorException("Communication exception", t);
         }
 
         // return Uid
         return new Uid(name.getNameValue());
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void delete(
             final ObjectClass objClass,
             final Uid uid,
             final OperationOptions options) {
 
         LOG.ok("Account deletion");
 
         // check objectclass
         if (objClass == null || (!objClass.equals(ObjectClass.ACCOUNT))) {
             throw new IllegalArgumentException("Invalid objectclass");
         }
 
         // get web service client
         Provisioning provisioning = connection.getProvisioning();
         if (provisioning == null) {
             throw new IllegalStateException("Web Service client not found");
         }
 
         try {
             provisioning.delete(uid.getUidValue());
         } catch (ProvisioningException e) {
             throw new ConnectorException("Deletion failed", e);
         } catch (Throwable t) {
             throw new ConnectorException("Communication exception", t);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Schema schema() {
         LOG.ok("Schema retrieving");
 
         Provisioning provisioning = connection.getProvisioning();
 
         if (provisioning == null) {
             throw new IllegalStateException("Web Service client not found");
         }
 
         List<WSAttribute> wsAttrs = provisioning.schema();
 
         Set<AttributeInfo> attributes = new HashSet<AttributeInfo>();
 
         if (wsAttributes != null) {
             wsAttributes.clear();
         }
 
         wsAttributes = new HashMap<String, WSAttribute>();
 
         for (WSAttribute attribute : wsAttrs) {
 
             wsAttributes.put(getAttributeName(attribute), attribute);
 
             if (LOG.isOk()) {
                 LOG.ok("\nAttribute: "
                         + "\n\tName: " + attribute.getName()
                         + "\n\tType: " + attribute.getType()
                         + "\n\tIsKey: " + attribute.isKey()
                         + "\n\tIsPassword: " + attribute.isPassword()
                         + "\n\tIsNullable: " + attribute.isNullable());
             }
 
             try {
                 attributes.add(buildAttribute(attribute));
             } catch (IllegalArgumentException ila) {
 
                 LOG.error("Invalid attribute " + attribute.getName(), ila);
             }
         }
 
         final SchemaBuilder schemaBld = new SchemaBuilder(getClass());
 
         final ObjectClassInfoBuilder objectclassInfoBuilder =
                 new ObjectClassInfoBuilder();
 
         objectclassInfoBuilder.setType(ObjectClass.ACCOUNT_NAME);
         objectclassInfoBuilder.addAllAttributeInfo(attributes);
 
         final ObjectClassInfo objectclassInfo = objectclassInfoBuilder.build();
         schemaBld.defineObjectClass(objectclassInfo);
 
         /*
          * Note: AuthenticateOp, and all the 'SPIOperation'-s are by default added by Reflection API to the Schema.
          *
          * See for details: FrameworkUtil.getDefaultSupportedOperations() ReflectionUtil.getAllInterfaces(connector); is
          * the line that *does* acquire the implemented interfaces by the connector class.
          */
         if (!provisioning.isAuthenticationSupported()) {
             LOG.ok("Authentication is not supported.");
 
             schemaBld.removeSupportedObjectClass(
                     AuthenticateOp.class, objectclassInfo);
         }
 
         if (!provisioning.isSyncSupported()) {
             LOG.ok("Synchronization is not supported.");
 
             schemaBld.removeSupportedObjectClass(
                     SyncOp.class, objectclassInfo);
         }
 
         schema = schemaBld.build();
         return schema;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public FilterTranslator<Operand> createFilterTranslator(
             final ObjectClass oclass,
             final OperationOptions options) {
 
         if (oclass == null || (!oclass.equals(ObjectClass.ACCOUNT))) {
             throw new IllegalArgumentException("Invalid objectclass");
         }
 
         return new WebServiceFilterTranslator();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void executeQuery(
             final ObjectClass objClass,
             final Operand query,
             final ResultsHandler handler,
             final OperationOptions options) {
 
         LOG.ok("Execute query: " + query);
 
         // check objectclass
         if (objClass == null || (!objClass.equals(ObjectClass.ACCOUNT))) {
             throw new IllegalArgumentException("Invalid objectclass");
         }
 
         // check handler
         if (handler == null) {
             throw new IllegalArgumentException("Invalid handler");
         }
 
         // get web service client
         Provisioning provisioning = connection.getProvisioning();
         if (provisioning == null) {
             throw new IllegalStateException("Web Service client not found");
         }
 
         try {
             List<WSUser> resultSet = provisioning.query(query);
             if (resultSet == null) {
                 return;
             }
 
             WSUser user;
             boolean handle = true;
             for (Iterator<WSUser> i = resultSet.iterator(); i.hasNext() && handle;) {
 
                 user = i.next();
                 LOG.ok("Found user: " + user.getAccountid());
 
                try {
                    handle = handler.handle(buildConnectorObject(user.getAttributes()).build());
                    LOG.ok("Handle:" + handle);
                } catch (IllegalArgumentException e) {
                    LOG.error("Error building connector object for {}", user.getAccountid(), e);
                }
             }
         } catch (Exception e) {
             throw new IllegalArgumentException(e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void test() {
         connection.test();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Uid update(final ObjectClass objclass,
             final Uid uid,
             final Set<Attribute> replaceAttributes,
             final OperationOptions options) {
 
         // check objectclass
         if (objclass == null || (!objclass.equals(ObjectClass.ACCOUNT))) {
             throw new IllegalArgumentException("Invalid objectclass");
         }
 
         // check accountid
         if (uid == null) {
             throw new IllegalArgumentException("No uid specified");
         }
 
         // check attributes
         if (replaceAttributes == null || replaceAttributes.size() == 0) {
             throw new IllegalArgumentException("No attribute specified");
         }
 
         // get web service client
         Provisioning provisioning = connection.getProvisioning();
         if (provisioning == null) {
             throw new IllegalStateException("Web Service client not found");
         }
 
         // to be user in order to pass information to the web service
         final List<WSAttributeValue> attributes =
                 new ArrayList<WSAttributeValue>();
 
         WSAttributeValue wsAttributeValue;
         WSAttribute wsAttribute;
 
         // retrieve attributes
         for (Attribute attr : replaceAttributes) {
 
             wsAttribute = new WSAttribute(attr.getName());
 
             if (attr.is(Name.NAME)) {
                 wsAttribute.setKey(true);
                 wsAttribute.setNullable(false);
             }
 
             if (attr.is(OperationalAttributeInfos.PASSWORD.getName())) {
                 wsAttribute.setName(
                         OperationalAttributeInfos.PASSWORD.getName());
                 wsAttribute.setPassword(true);
             }
 
             if (LOG.isOk()) {
                 LOG.ok("\nAttribute: "
                         + "\n\tName: " + wsAttribute.getName()
                         + "\n\tType: " + wsAttribute.getType()
                         + "\n\tIsKey: " + wsAttribute.isKey()
                         + "\n\tIsPassword: " + wsAttribute.isPassword());
             }
 
             wsAttributeValue = new WSAttributeValue(wsAttribute);
             attributes.add(wsAttributeValue);
 
             List value = attr.getValue();
 
             if (value != null && value.size() == 1
                     && (value.get(0) instanceof GuardedString
                     || value.get(0) instanceof GuardedByteArray)) {
 
                 wsAttributeValue.setValues(
                         Collections.singletonList(value.toString()));
             } else {
                 wsAttributeValue.setValues(value);
             }
         }
 
         Uid uuid = null;
 
         try {
             // user creation
             uuid = new Uid(provisioning.update(uid.getUidValue(), attributes));
         } catch (ProvisioningException e) {
             throw new ConnectorException("Update failed", e);
         } catch (Throwable t) {
             throw new ConnectorException("Communication exception", t);
         }
 
         return uuid;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void sync(
             final ObjectClass objClass,
             final SyncToken token,
             final SyncResultsHandler handler,
             final OperationOptions options) {
 
         // check objectclass
         if (objClass == null || (!objClass.equals(ObjectClass.ACCOUNT))) {
             throw new IllegalArgumentException("Invalid objectclass");
         }
 
         // check objectclass
         if (handler == null) {
             throw new IllegalArgumentException("Invalid handler");
         }
 
         // get web service client
         Provisioning provisioning = connection.getProvisioning();
         if (provisioning == null) {
             throw new IllegalStateException("Web Service client not found");
         }
 
         try {
             List<WSChange> changes = provisioning.sync();
 
             SyncDeltaBuilder sdb = null;
 
             boolean handle = true;
             for (Iterator<WSChange> i = changes.iterator();
                     i.hasNext() && handle;) {
 
                WSChange change = i.next();

                try {
                    sdb = buildSyncDelta(change);
                    handle = handler.handle(sdb.build());
                } catch (IllegalArgumentException e) {
                    LOG.error("Error building connector object for change {}", change.getId(), e);
                }
             }
         } catch (ProvisioningException e) {
             LOG.error("Synchronization failed");
 
             throw new IllegalStateException(e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public SyncToken getLatestSyncToken(final ObjectClass objectClass) {
 
         // check objectclass
         if (objectClass == null || (!objectClass.equals(ObjectClass.ACCOUNT))) {
             throw new IllegalArgumentException("Invalid objectclass");
         }
 
         // get web service client
         Provisioning provisioning = connection.getProvisioning();
         if (provisioning == null) {
             throw new IllegalStateException("Web Service client not found");
         }
 
         SyncToken token = null;
 
         try {
             token = new SyncToken(provisioning.getLatestChangeNumber());
         } catch (ProvisioningException e) {
             throw new ConnectorException("getLatestSyncToken failed", e);
         } catch (Throwable t) {
             throw new ConnectorException("Communication exception", t);
         }
 
         return token;
     }
 
     @Override
     public Uid resolveUsername(final ObjectClass objectClass,
             final String username, final OperationOptions options) {
 
         // check objectclass
         if (objectClass == null || (!objectClass.equals(ObjectClass.ACCOUNT))) {
             throw new IllegalArgumentException("Invalid objectclass");
         }
 
         // check accountid
         if (username == null) {
             throw new IllegalArgumentException("No username specified");
         }
 
         // get web service client
         Provisioning provisioning = connection.getProvisioning();
         if (provisioning == null) {
             throw new IllegalStateException("Web Service client not found");
         }
 
         String uid = null;
         try {
             uid = provisioning.resolve(username);
         } catch (ProvisioningException e) {
             throw new ConnectorException("Resolve failed", e);
         } catch (Throwable t) {
             throw new ConnectorException("Communication exception", t);
         }
 
         Uid uuid = null;
         if (uid != null) {
             LOG.ok("Not able to resolve '" + username + "'");
             uuid = new Uid(uid);
         }
 
         return uuid;
     }
 
     private AttributeInfo buildAttribute(WSAttribute attribute) {
         final AttributeInfoBuilder builder = new AttributeInfoBuilder();
 
         try {
             if (attribute.isPassword()) {
                 return OperationalAttributeInfos.PASSWORD;
             }
 
             if (attribute.isKey()) {
                 builder.setName(Name.NAME);
                 builder.setReadable(true);
 
                 return builder.build();
             }
 
             // Check the type
             Class.forName(AttributeType.valueOf(attribute.getType()).getClassName());
 
             builder.setName(attribute.getName());
             builder.setRequired(attribute.isNullable());
 
             return builder.build();
         } catch (ClassNotFoundException e) {
             LOG.error("Invalid data type", e);
 
             throw new IllegalArgumentException(e);
         } catch (Throwable t) {
             LOG.error("Unexpected exception", t);
 
             throw new IllegalArgumentException(t);
         }
     }
 
     private ConnectorObjectBuilder buildConnectorObject(
             final Set<WSAttributeValue> attributes) {
 
         ConnectorObjectBuilder bld = new ConnectorObjectBuilder();
 
         String uid = null;
 
         for (WSAttributeValue attribute : attributes) {
 
             if (uid == null && (attribute.isKey()
                     || Name.NAME.equals(attribute.getName()) || Uid.NAME.equals(attribute.getName()))) {
                 uid = attribute.getStringValue();
                 bld.setName(uid);
                 bld.addAttribute(AttributeBuilder.build(attribute.getName(), attribute.getValues()));
 
             } else if (OperationalAttributes.ENABLE_NAME.equals(attribute.getName())) {
 
                 bld.addAttribute(AttributeBuilder.buildEnabled(
                         attribute.getValues() == null
                         || attribute.getValues().isEmpty()
                         || Boolean.parseBoolean(attribute.getValues().get(0).toString())));
 
             } else {
 
                 if (attribute.getValues() == null) {
                     bld.addAttribute(AttributeBuilder.build(attribute.getName()));
                 } else {
                     bld.addAttribute(AttributeBuilder.build(attribute.getName(), attribute.getValues()));
                 }
 
             }
         }
 
         // To be sure that uid and name are present
         if (uid == null) {
             throw new IllegalStateException("Invalid uid");
         }
 
         // Add Uid attribute to object
         bld.setUid(new Uid(uid));
 
         // Add objectclass
         bld.setObjectClass(ObjectClass.ACCOUNT);
 
         return bld;
     }
 
     private String getAttributeName(final WSAttribute attribute) {
         String attributeName = null;
 
         if (attribute.isKey()) {
             attributeName = Name.NAME;
         }
 
         if (attribute.isPassword()) {
             attributeName = OperationalAttributeInfos.PASSWORD.getName();
         }
 
         if (!attribute.isKey() && !attribute.isPassword()) {
             attributeName = attribute.getName();
         }
 
         return attributeName;
     }
 
     private SyncDeltaBuilder buildSyncDelta(final WSChange change) {
         SyncDeltaBuilder bld = new SyncDeltaBuilder();
 
         ConnectorObject object = buildConnectorObject(change.getAttributes()).build();
 
         bld.setToken(new SyncToken(change.getId()));
         bld.setObject(object);
 
         if ("CREATE_OR_UPDATE".equalsIgnoreCase(change.getType())) {
             bld.setDeltaType(SyncDeltaType.CREATE_OR_UPDATE);
         }
 
         if ("DELETE".equalsIgnoreCase(change.getType())) {
             bld.setDeltaType(SyncDeltaType.DELETE);
         }
 
         bld.setUid(object.getUid());
 
         return bld;
     }
 }
