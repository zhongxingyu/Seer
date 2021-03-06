 /*
  *  Licensed to the Apache Software Foundation (ASF) under one
  *  or more contributor license agreements.  See the NOTICE file
  *  distributed with this work for additional information
  *  regarding copyright ownership.  The ASF licenses this file
  *  to you under the Apache License, Version 2.0 (the
  *  "License"); you may not use this file except in compliance
  *  with the License.  You may obtain a copy of the License at
  *  
  *    http://www.apache.org/licenses/LICENSE-2.0
  *  
  *  Unless required by applicable law or agreed to in writing,
  *  software distributed under the License is distributed on an
  *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  *  KIND, either express or implied.  See the License for the
  *  specific language governing permissions and limitations
  *  under the License. 
  *  
  */
 package org.apache.directory.server.core.authz.support;
 
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.naming.directory.Attribute;
 
 import org.apache.directory.server.core.DefaultDirectoryService;
 import org.apache.directory.server.core.DirectoryService;
 import org.apache.directory.server.core.authz.support.OperationScope;
 import org.apache.directory.server.core.authz.support.RelatedProtectedItemFilter;
 import org.apache.directory.server.core.authz.support.RelatedUserClassFilter;
 import org.apache.directory.server.core.entry.DefaultServerAttribute;
 import org.apache.directory.server.core.entry.DefaultServerEntry;
 import org.apache.directory.server.core.entry.ServerAttribute;
 import org.apache.directory.server.core.entry.ServerEntry;
 import org.apache.directory.server.core.entry.ServerEntryUtils;
 import org.apache.directory.server.core.event.ExpressionEvaluator;
 import org.apache.directory.server.core.subtree.RefinementEvaluator;
 import org.apache.directory.server.core.subtree.RefinementLeafEvaluator;
 import org.apache.directory.shared.ldap.aci.ACITuple;
 import org.apache.directory.shared.ldap.aci.MicroOperation;
 import org.apache.directory.shared.ldap.aci.ProtectedItem;
 import org.apache.directory.shared.ldap.aci.UserClass;
 import org.apache.directory.shared.ldap.aci.ProtectedItem.MaxValueCountItem;
 import org.apache.directory.shared.ldap.aci.ProtectedItem.RestrictedByItem;
 import org.apache.directory.shared.ldap.constants.AuthenticationLevel;
 import org.apache.directory.shared.ldap.entry.client.ClientStringValue;
 import org.apache.directory.shared.ldap.name.LdapDN;
 import org.apache.directory.shared.ldap.schema.AttributeType;
 import org.apache.directory.shared.ldap.schema.registries.AttributeTypeRegistry;
 import org.apache.directory.shared.ldap.schema.registries.OidRegistry;
 
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 
 
 /**
  * Tests {@link RelatedUserClassFilter}.
  *
  * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
  * @version $Rev$, $Date$
  */
 public class RelatedProtectedItemFilterTest
 {
     private static final Collection<UserClass> EMPTY_USER_CLASS_COLLECTION = Collections.unmodifiableCollection( new ArrayList<UserClass>() );
     private static final Collection<ACITuple> EMPTY_ACI_TUPLE_COLLECTION = Collections.unmodifiableCollection( new ArrayList<ACITuple>() );
     private static final Set<MicroOperation> EMPTY_MICRO_OPERATION_SET = Collections.unmodifiableSet( new HashSet<MicroOperation>() );
 
     private static LdapDN GROUP_NAME;
     private static LdapDN USER_NAME;
     private static Set<LdapDN> USER_NAMES = new HashSet<LdapDN>();
     private static Set<LdapDN> GROUP_NAMES = new HashSet<LdapDN>();
 
    private static AttributeTypeRegistry atRegistryA;
    private static AttributeTypeRegistry atRegistryB;
     private static OidRegistry OID_REGISTRY;
 
     private static RelatedProtectedItemFilter filterA;
     private static RelatedProtectedItemFilter filterB;
 
     /** A reference to the directory service */
     private static DirectoryService service;
     
     /** The CN attribute Type */
     private static AttributeType CN_AT;
 
     /** The SN attribute Type */
     private static AttributeType SN_AT;
 
     
     @BeforeClass public static void setup() throws Exception
     {
         service = new DefaultDirectoryService();
         OID_REGISTRY = service.getRegistries().getOidRegistry();
 
         GROUP_NAME = new LdapDN( "ou=test,ou=groups,ou=system" );
         USER_NAME = new LdapDN( "ou=test, ou=users, ou=system" );
        
        atRegistryA = service.getRegistries().getAttributeTypeRegistry();
        atRegistryB = service.getRegistries().getAttributeTypeRegistry();
 
         filterA = new RelatedProtectedItemFilter( new RefinementEvaluator( new RefinementLeafEvaluator(
            OID_REGISTRY ) ), new ExpressionEvaluator( OID_REGISTRY, atRegistryA ), OID_REGISTRY, atRegistryA );
 
         filterB = new RelatedProtectedItemFilter( new RefinementEvaluator( new RefinementLeafEvaluator(
            OID_REGISTRY ) ), new ExpressionEvaluator( OID_REGISTRY, atRegistryB ), OID_REGISTRY, atRegistryB );
 
         USER_NAMES.add( USER_NAME );
         GROUP_NAMES.add( GROUP_NAME );
         CN_AT = service.getRegistries().getAttributeTypeRegistry().lookup( "cn" );
         SN_AT = service.getRegistries().getAttributeTypeRegistry().lookup( "sn" );
     }
 
     
     private Collection<Attribute> convert( Collection<ServerAttribute> attributes )
     {
         Set<Attribute> jndiAttributes = new HashSet<Attribute>();
         
         for ( ServerAttribute attribute:attributes )
         {
             jndiAttributes.add( ServerEntryUtils.toBasicAttribute( attribute ) );
         }
         
         return jndiAttributes;
     }
 
     @Test public void testZeroTuple() throws Exception
     {
         assertEquals( 0, filterA.filter( null, EMPTY_ACI_TUPLE_COLLECTION, OperationScope.ATTRIBUTE_TYPE_AND_VALUE, null, null,
             null, null, null, null, null, null, null, null, null ).size() );
     }
 
 
     @Test public void testEntry() throws Exception
     {
         Collection<ACITuple> tuples = getTuples( ProtectedItem.ENTRY );
 
         assertEquals( 1, filterA.filter( null, tuples, OperationScope.ENTRY, null, null, null, null,
             AuthenticationLevel.NONE, null, "ou", null, null, null, null ).size() );
     }
 
 
     @Test public void testAllUserAttributeTypes() throws Exception
     {
         Collection<ACITuple> tuples = getTuples( ProtectedItem.ALL_USER_ATTRIBUTE_TYPES );
 
         // Test wrong scope
         assertEquals( 0, filterA.filter( null, tuples, OperationScope.ENTRY, null, null, USER_NAME, null, null, null,
             "cn", null, null, null, null ).size() );
 
         tuples = getTuples( ProtectedItem.ALL_USER_ATTRIBUTE_TYPES );
 
         assertEquals( 1, filterA.filter( null, tuples, OperationScope.ATTRIBUTE_TYPE, null, null, USER_NAME, null,
             null, null, "cn", null, null, null, null ).size() );
     }
 
 
     @Test public void testAllUserAttributeTypesAndValues() throws Exception
     {
         Collection<ACITuple> tuples = getTuples( ProtectedItem.ALL_USER_ATTRIBUTE_TYPES_AND_VALUES );
 
         // Test wrong scope
         assertEquals( 0, filterA.filter( null, tuples, OperationScope.ENTRY, null, null, USER_NAME, null, null, null,
             "cn", null, null, null, null ).size() );
 
         tuples = getTuples( ProtectedItem.ALL_USER_ATTRIBUTE_TYPES_AND_VALUES );
 
         assertEquals( 1, filterA.filter( null, tuples, OperationScope.ATTRIBUTE_TYPE, null, null, USER_NAME, null,
             null, null, "cn", null, null, null, null ).size() );
     }
 
 
     @Test public void testAllAttributeValues() throws Exception
     {
         Collection<String> attrTypes = new ArrayList<String>();
         attrTypes.add( "cn" );
         Collection<ACITuple> tuples = getTuples( new ProtectedItem.AllAttributeValues( attrTypes ) );
 
         // Test wrong scope
         assertEquals( 0, filterA.filter( null, tuples, OperationScope.ENTRY, null, null, USER_NAME, null, null, null,
             "cn", null, null, null, null ).size() );
 
         tuples = getTuples( new ProtectedItem.AllAttributeValues( attrTypes ) );
 
         assertEquals( 1, filterA.filter( null, tuples, OperationScope.ATTRIBUTE_TYPE_AND_VALUE, null, null, USER_NAME, null,
             null, null, "cn", null, null, null, null ).size() );
 
         assertEquals( 0, filterB.filter( null, tuples, OperationScope.ATTRIBUTE_TYPE_AND_VALUE, null, null, USER_NAME, null,
             null, null, "sn", null, null, null, null ).size() );
     }
 
 
     @Test public void testAttributeType() throws Exception
     {
         Collection<String> attrTypes = new ArrayList<String>();
         attrTypes.add( "cn" );
         Collection<ACITuple> tuples = getTuples( new ProtectedItem.AttributeType( attrTypes ) );
 
         // Test wrong scope
         assertEquals( 0, filterA.filter( null, tuples, OperationScope.ENTRY, null, null, USER_NAME, null, null, null,
             "cn", null, null, null, null ).size() );
 
         tuples = getTuples( new ProtectedItem.AttributeType( attrTypes ) );
 
         assertEquals( 1, filterA.filter( null, tuples, OperationScope.ATTRIBUTE_TYPE, null, null, USER_NAME, null,
             null, null, "cn", null, null, null, null ).size() );
 
         assertEquals( 0, filterA.filter( null, tuples, OperationScope.ATTRIBUTE_TYPE, null, null, USER_NAME, null,
             null, null, "sn", null, null, null, null ).size() );
     }
 
 
     @Test public void testAttributeValue() throws Exception
     {
         Collection<ServerAttribute> attributes = new ArrayList<ServerAttribute>();
         attributes.add( new DefaultServerAttribute( "cn", CN_AT, "valueA" ) );
         Collection<ACITuple> tuples = getTuples( new ProtectedItem.AttributeValue( convert( attributes ) ) );
 
         // Test wrong scope
         assertEquals( 0, filterA.filter( null, tuples, OperationScope.ENTRY, null, null, USER_NAME, null, null, null,
             "cn", null, null, null, null ).size() );
         tuples = getTuples( new ProtectedItem.AttributeValue( convert( attributes )  ) );
         assertEquals( 0, filterA.filter( null, tuples, OperationScope.ATTRIBUTE_TYPE, null, null, USER_NAME, null,
             null, null, "cn", null, null, null, null ).size() );
 
         tuples = getTuples( new ProtectedItem.AttributeValue( convert( attributes )  ) );
 
         assertEquals( 1, filterA.filter( null, tuples, OperationScope.ATTRIBUTE_TYPE_AND_VALUE, null, null, USER_NAME,
             null, null, null, "cn", new ClientStringValue( "valueA" ), null, null, null ).size() );
 
         assertEquals( 0, filterA.filter( null, tuples, OperationScope.ATTRIBUTE_TYPE_AND_VALUE, null, null, USER_NAME,
             null, null, null, "cn", new ClientStringValue( "valueB" ), null, null, null ).size() );
 
         tuples = getTuples( new ProtectedItem.AttributeValue( convert( attributes )  ) );
 
         assertEquals( 0, filterA.filter( null, tuples, OperationScope.ATTRIBUTE_TYPE_AND_VALUE, null, null, USER_NAME,
             null, null, null, "sn", new ClientStringValue( "valueA" ), null, null, null ).size() );
     }
 
 
     public void testClasses() throws Exception
     {
         // TODO I don't know how to test with Refinement yet.
     }
 
 
     @Test public void testMaxImmSub() throws Exception
     {
         Collection<ACITuple> tuples = getTuples( new ProtectedItem.MaxImmSub( 2 ) );
 
         // Should always retain tuples.
         assertEquals( 1, filterA.filter( null, tuples, OperationScope.ENTRY, null, null, USER_NAME, null, null, null,
             "cn", null, null, null, null ).size() );
     }
 
 
     @Test public void testMaxValueCount() throws Exception
     {
         Collection<MaxValueCountItem> mvcItems = new ArrayList<MaxValueCountItem>();
         mvcItems.add( new MaxValueCountItem( "cn", 3 ) );
         Collection<ACITuple> tuples = getTuples( new ProtectedItem.MaxValueCount( mvcItems ) );
 
         // Test wrong scope
         assertEquals( 0, filterA.filter( null, tuples, OperationScope.ENTRY, null, null, USER_NAME, null, null, null,
             "cn", null, null, null, null ).size() );
         tuples = getTuples( new ProtectedItem.MaxValueCount( mvcItems ) );
         assertEquals( 0, filterA.filter( null, tuples, OperationScope.ATTRIBUTE_TYPE, null, null, USER_NAME, null,
             null, null, "cn", null, null, null, null ).size() );
 
         tuples = getTuples( new ProtectedItem.MaxValueCount( mvcItems ) );
 
         assertEquals( 1, filterA.filter( null, tuples, OperationScope.ATTRIBUTE_TYPE_AND_VALUE, null, null, USER_NAME,
             null, null, null, "cn", null, null, null, null ).size() );
 
         assertEquals( 0, filterA.filter( null, tuples, OperationScope.ATTRIBUTE_TYPE_AND_VALUE, null, null, USER_NAME,
             null, null, null, "sn", null, null, null, null ).size() );
     }
 
 
     /* this test requires a real registry with real values or the dummy registry
      * needs to be altered to contain some usable mock data.  This is a result of
      * using the registry now in this operation.    
      *
     public void testRangeOfValues() throws Exception
     {
         ServerEntry entry = new DefaultServerEntry( service.getRegistries(), USER_NAME );
         entry.put( "cn", "valueA" );
         Collection<ACITuple> tuples = getTuples( new ProtectedItem.RangeOfValues( new PresenceNode( "cn" ) ) );
 
         Assert.assertEquals( 1, filterA.filter( tuples, OperationScope.ENTRY, null, null, USER_NAME, null, null,
             new LdapDN( "ou=testEntry" ), null, null, entry, null ).size() );
 
         entry.remove( "cn" );
         Assert.assertEquals( 0, filterA.filter( service.getRegistries(), tuples, OperationScope.ATTRIBUTE_TYPE_AND_VALUE, null, null, USER_NAME,
             null, null, new LdapDN( "ou=testEntry" ), null, null, entry, null ).size() );
     }
     */
 
 
     @Test public void testRestrictedBy() throws Exception
     {
         Collection<RestrictedByItem> rbItems = new ArrayList<RestrictedByItem>();
         rbItems.add( new RestrictedByItem( "cn", "sn" ) );
         Collection<ACITuple> tuples = getTuples( new ProtectedItem.RestrictedBy( rbItems ) );
 
         // Test wrong scope
         assertEquals( 0, filterA.filter( null, tuples, OperationScope.ENTRY, null, null, USER_NAME, null, null, null,
             "cn", null, null, null, null ).size() );
         tuples = getTuples( new ProtectedItem.RestrictedBy( rbItems ) );
         assertEquals( 0, filterA.filter( null, tuples, OperationScope.ATTRIBUTE_TYPE, null, null, USER_NAME, null,
             null, null, "cn", null, null, null, null ).size() );
 
         tuples = getTuples( new ProtectedItem.RestrictedBy( rbItems ) );
 
         assertEquals( 1, filterA.filter( null, tuples, OperationScope.ATTRIBUTE_TYPE_AND_VALUE, null, null, USER_NAME,
             null, null, null, "cn", null, null, null, null ).size() );
 
         assertEquals( 0, filterA.filter( null, tuples, OperationScope.ATTRIBUTE_TYPE_AND_VALUE, null, null, USER_NAME,
             null, null, null, "sn", null, null, null, null ).size() );
     }
 
 
     @Test public void testSelfValue() throws Exception
     {
         Collection<String> attrTypes = new ArrayList<String>();
         attrTypes.add( "cn" );
         Collection<ACITuple> tuples = getTuples( new ProtectedItem.SelfValue( attrTypes ) );
 
         ServerEntry entry = new DefaultServerEntry( service.getRegistries(), USER_NAME );
         entry.put( "cn", USER_NAME.toNormName() );
 
         // Test wrong scope
         assertEquals( 0, filterA.filter( null, tuples, OperationScope.ENTRY, null, null, USER_NAME, null, null, null,
             "cn", null, entry, null, null ).size() );
 
         tuples = getTuples( new ProtectedItem.SelfValue( attrTypes ) );
 
         assertEquals( 1, filterA.filter( null, tuples, OperationScope.ATTRIBUTE_TYPE_AND_VALUE, null, null, USER_NAME,
             null, null, null, "cn", null, entry, null, null ).size() );
 
         entry.removeAttributes( "cn" );
         assertEquals( 0, filterA.filter( null, tuples, OperationScope.ATTRIBUTE_TYPE_AND_VALUE, null, null, USER_NAME,
             null, null, null, "cn", null, entry, null, null ).size() );
 
         tuples = getTuples( new ProtectedItem.SelfValue( attrTypes ) );
         assertEquals( 0, filterA.filter( null, tuples, OperationScope.ATTRIBUTE_TYPE_AND_VALUE, null, null, USER_NAME,
             null, null, null, "sn", null, entry, null, null ).size() );
     }
 
 
     private static Collection<ACITuple> getTuples( ProtectedItem protectedItem )
     {
         Collection<ProtectedItem> protectedItems = new ArrayList<ProtectedItem>();
         protectedItems.add( protectedItem );
 
         Collection<ACITuple> tuples = new ArrayList<ACITuple>();
         tuples.add( new ACITuple( EMPTY_USER_CLASS_COLLECTION, AuthenticationLevel.NONE, protectedItems, EMPTY_MICRO_OPERATION_SET, true, 0 ) );
 
         return tuples;
     }
 }
