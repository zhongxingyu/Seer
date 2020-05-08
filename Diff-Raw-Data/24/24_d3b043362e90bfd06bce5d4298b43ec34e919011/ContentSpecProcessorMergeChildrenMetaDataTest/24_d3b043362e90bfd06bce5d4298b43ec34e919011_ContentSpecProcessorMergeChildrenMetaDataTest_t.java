 package org.jboss.pressgang.ccms.contentspec.processor;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertSame;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 import static org.mockito.BDDMockito.given;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyInt;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import net.sf.ipsedixit.annotation.Arbitrary;
 import net.sf.ipsedixit.annotation.ArbitraryString;
 import net.sf.ipsedixit.core.StringType;
 import org.jboss.pressgang.ccms.contentspec.KeyValueNode;
 import org.jboss.pressgang.ccms.contentspec.Node;
 import org.jboss.pressgang.ccms.contentspec.SpecNode;
 import org.jboss.pressgang.ccms.utils.constants.CommonConstants;
 import org.jboss.pressgang.ccms.wrapper.CSNodeWrapper;
 import org.jboss.pressgang.ccms.wrapper.ContentSpecWrapper;
 import org.jboss.pressgang.ccms.wrapper.mocks.UpdateableCollectionWrapperMock;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 
 public class ContentSpecProcessorMergeChildrenMetaDataTest extends ContentSpecProcessorTest {
     @Arbitrary Integer id;
     @Arbitrary String value;
     @ArbitraryString(type = StringType.ALPHANUMERIC) String key;
     @ArbitraryString(type = StringType.ALPHANUMERIC) String randomAlphaString;
 
     @Mock ContentSpecWrapper contentSpecWrapper;
     @Mock CSNodeWrapper newCSNode;
     @Mock CSNodeWrapper foundCSNode;
 
     List<CSNodeWrapper> childrenNodes;
     UpdateableCollectionWrapperMock<CSNodeWrapper> updatedChildrenNodes;
     Map<SpecNode, CSNodeWrapper> nodeMap;
 
     @Before
     public void setUpCollections() {
         childrenNodes = new LinkedList<CSNodeWrapper>();
         updatedChildrenNodes = new UpdateableCollectionWrapperMock<CSNodeWrapper>();
         nodeMap = new HashMap<SpecNode, CSNodeWrapper>();
 
         when(contentSpecNodeProvider.newCSNode()).thenReturn(newCSNode);
         when(contentSpecNodeProvider.newCSNodeCollection()).thenReturn(new UpdateableCollectionWrapperMock<CSNodeWrapper>());
 
         // and the found metadata is already assigned to the content spec
         when(foundCSNode.getContentSpec()).thenReturn(contentSpecWrapper);
     }
 
     @Test
     public void shouldCreateNewMetaDataNode() throws Exception {
         final List<Node> childNodes = new ArrayList<Node>();
         setUpNodeToReturnNulls(newCSNode);
         // Given a content spec meta data that doesn't exist
         final KeyValueNode<String> metaData = new KeyValueNode<String>(key, value);
         childNodes.add(metaData);
         // and the content spec will return a collection
         given(contentSpecWrapper.getChildren()).willReturn(updatedChildrenNodes);
        // and the node type gets set in the mergeChildren method
        given(newCSNode.getNodeType()).willReturn(CommonConstants.CS_NODE_META_DATA);
 
         // When merging the children nodes
         try {
             processor.mergeChildren(childNodes, childrenNodes, providerFactory, null, contentSpecWrapper, nodeMap);
         } catch (Exception e) {
             e.printStackTrace();
             fail("An Exception should not have been thrown. Message: " + e.getMessage());
         }
 
         // Then a new node should exist in the updated collection.
         assertThat(updatedChildrenNodes.size(), is(1));
         assertThat(updatedChildrenNodes.getAddItems().size(), is(1));
         // and the basic details are correct
         verifyBaseNewMetaData(newCSNode);
     }
 
     @Test
     public void shouldMergeMetaDataWithDBIds() {
         final List<Node> childNodes = new ArrayList<Node>();
         // Given a content spec meta data that was created from a DB entity
         final KeyValueNode<String> metaData = new KeyValueNode<String>(key, value);
         childNodes.add(metaData);
         // And a matching child node exists in the database
         given(foundCSNode.getNodeType()).willReturn(CommonConstants.CS_NODE_META_DATA);
         given(foundCSNode.getTitle()).willReturn(key);
         // and is in the child nodes collection
         childrenNodes.add(foundCSNode);
         // and the content spec will return a collection
         given(contentSpecWrapper.getChildren()).willReturn(updatedChildrenNodes);
 
         // When merging the children nodes
         try {
             processor.mergeChildren(childNodes, childrenNodes, providerFactory, null, contentSpecWrapper, nodeMap);
         } catch (Exception e) {
             fail("An Exception should not have been thrown. Message: " + e.getMessage());
         }
 
         // Then a updated node should exist in the updated collection
         assertThat(updatedChildrenNodes.size(), is(1));
         assertThat(updatedChildrenNodes.getUpdateItems().size(), is(1));
         assertSame(updatedChildrenNodes.getItems().get(0), foundCSNode);
         // and the value was set
         verify(foundCSNode, times(1)).setAdditionalText(value);
         // and the main details haven't changed
         verifyBaseExistingMetaData(foundCSNode);
     }
 
     @Test
     public void shouldMergeMetaDataWithDBIdsWhenMultipleMetaData() {
         final List<Node> childNodes = new ArrayList<Node>();
         // Given a content spec meta data that was created from a DB entity
         final KeyValueNode<String> metaData = new KeyValueNode<String>(key, value);
         childNodes.add(metaData);
         // and a matching child node exists in the database
         given(foundCSNode.getNodeType()).willReturn(CommonConstants.CS_NODE_META_DATA);
         given(foundCSNode.getTitle()).willReturn(key);
         // and another node exists that won't match
         given(newCSNode.getNodeType()).willReturn(CommonConstants.CS_NODE_META_DATA);
         given(newCSNode.getTitle()).willReturn(randomAlphaString);
         // and is in the child nodes collection
         childrenNodes.add(newCSNode);
         childrenNodes.add(foundCSNode);
         // and the content spec will return a collection
         given(contentSpecWrapper.getChildren()).willReturn(updatedChildrenNodes);
 
         // When merging the children nodes
         try {
             processor.mergeChildren(childNodes, childrenNodes, providerFactory, null, contentSpecWrapper, nodeMap);
         } catch (Exception e) {
             fail("An Exception should not have been thrown. Message: " + e.getMessage());
         }
 
         // Then a updated node should exist in the updated collection
         assertThat(updatedChildrenNodes.size(), is(2));
         assertThat(updatedChildrenNodes.getUpdateItems().size(), is(1));
         assertSame(updatedChildrenNodes.getUpdateItems().get(0), foundCSNode);
         // and the other node should be set for removal, by still existing in the childrenNodes list
         assertThat(childrenNodes.size(), is(1));
         assertThat(updatedChildrenNodes.getRemoveItems().size(), is(1));
         assertTrue(childrenNodes.contains(newCSNode));
         // and the value was set
         verify(foundCSNode, times(1)).setAdditionalText(value);
         // and the value of the other node wasn't touched
         verify(newCSNode, never()).setAdditionalText(anyString());
         // and the main details haven't changed
         verifyBaseExistingMetaData(foundCSNode);
     }
 
     @Test
     public void shouldIgnoreMetaDataWithDBIdsWhereValueIsUnchanged() {
         final List<Node> childNodes = new ArrayList<Node>();
         // Given a content spec meta data that was created from a DB entity
         final KeyValueNode<String> metaData = new KeyValueNode<String>(key, value);
         childNodes.add(metaData);
         // And a matching child node exists in the database
         given(foundCSNode.getNodeType()).willReturn(CommonConstants.CS_NODE_META_DATA);
         given(foundCSNode.getTitle()).willReturn(key);
         // and the value is unchanged
         given(foundCSNode.getAdditionalText()).willReturn(value);
         // and is in the child nodes collection
         childrenNodes.add(foundCSNode);
         // and some values should return null
         given(foundCSNode.getNextNode()).willReturn(null);
         // and the content spec will return a collection
         given(contentSpecWrapper.getChildren()).willReturn(updatedChildrenNodes);
 
         // When merging the children nodes
         try {
             processor.mergeChildren(childNodes, childrenNodes, providerFactory, null, contentSpecWrapper, nodeMap);
         } catch (Exception e) {
             fail("An Exception should not have been thrown. Message: " + e.getMessage());
         }
 
         // Then a updated node shouldn't exist since nothing was modified
         assertThat(updatedChildrenNodes.size(), is(0));
         // and the value was set
         verify(foundCSNode, never()).setAdditionalText(value);
         // and the main details haven't changed
         verifyBaseExistingMetaData(foundCSNode);
     }
 
     /**
      * Sets up the mock to return nulls for all primitive type wrapper classes.
      *
      * @param mockNode
      */
     protected void setUpNodeToReturnNulls(final CSNodeWrapper mockNode) {
         when(mockNode.getRevision()).thenReturn(null);
         when(mockNode.getAdditionalText()).thenReturn(null);
         when(mockNode.getEntityRevision()).thenReturn(null);
         when(mockNode.getEntityId()).thenReturn(null);
         when(mockNode.getTitle()).thenReturn(null);
         when(mockNode.getTargetId()).thenReturn(null);
         when(mockNode.getNextNode()).thenReturn(null);
     }
 
     protected void verifyBaseNewMetaData(final CSNodeWrapper metaDataNode) {
         // and the node has the Spec Topic type set
         verify(metaDataNode, times(1)).setNodeType(CommonConstants.CS_NODE_META_DATA);
         // and the parent node should be null
         verify(metaDataNode, never()).setParent(any(CSNodeWrapper.class));
         // and the node had the key set
         verify(metaDataNode, times(1)).setTitle(key);
         // and the node had the value set
         verify(metaDataNode, times(1)).setAdditionalText(value);
         // and the node topic id wasn't set
         verify(metaDataNode, never()).setEntityId(anyInt());
         // and the topic revision wasn't set
         verify(metaDataNode, never()).setEntityRevision(anyInt());
     }
 
     protected void verifyBaseExistingMetaData(final CSNodeWrapper metaDataNode) {
         // and the node type hasn't changed
         verify(metaDataNode, never()).setNodeType(anyInt());
         // and the parent node should be null
         verify(metaDataNode, never()).setParent(any(CSNodeWrapper.class));
         // and the content spec wasn't changed
         verify(metaDataNode, never()).setContentSpec(any(ContentSpecWrapper.class));
         // and the node key wasn't changed
         verify(metaDataNode, never()).setTitle(anyString());
         // and the node topic id wasn't set
         verify(metaDataNode, never()).setEntityId(anyInt());
         // and the topic revision wasn't set
         verify(metaDataNode, never()).setEntityRevision(anyInt());
     }
 }
