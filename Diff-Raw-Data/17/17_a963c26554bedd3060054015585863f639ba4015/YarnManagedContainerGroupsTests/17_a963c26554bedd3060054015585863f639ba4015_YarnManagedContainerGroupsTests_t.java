 /*
  * Copyright 2013 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.springframework.yarn.examples.grid.yarn;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.hadoop.yarn.api.records.Container;
 import org.apache.hadoop.yarn.api.records.ContainerId;
 import org.apache.hadoop.yarn.api.records.NodeId;
 import org.apache.hadoop.yarn.util.ConverterUtils;
 import org.apache.hadoop.yarn.util.Records;
 import org.junit.Test;
 import org.springframework.yarn.examples.grid.ContainerGridListener;
 import org.springframework.yarn.examples.grid.ContainerGroupsListener;
 
 /**
  * Tests for {@link YarnManagedContainerGroups}.
  *
  * @author Janne Valkealahti
  *
  */
 public class YarnManagedContainerGroupsTests {
 
 	private final static String CID1 = "container_1375001068632_0001_01_000001";
 	private final static String CID2 = "container_1375001068632_0001_01_000002";
 	private final static String HOST1 = "hostname1";
 	private final static String HOST2 = "hostname2";
 	private final static String EXTRA_GROUP = "foogroup";
 
 	@Test
 	public void testInitialStateWithDefaults() {
 		YarnManagedContainerGroups managedGroups = createYmcgWithDefaults();
 
 		Collection<YarnContainerGroup> groups = managedGroups.getGroups();
 		assertThat(groups.size(), is(1));
 
 		Collection<YarnContainerNode> nodes = managedGroups.getContainerNodes();
 		assertThat(nodes.size(), is(0));
 	}
 
 	@Test
 	public void testAddGroup() {
 		YarnManagedContainerGroups managedGroups = createYmcgWithDefaults();
 
 		managedGroups.addGroup(new YarnContainerGroup(EXTRA_GROUP));
 
 		Collection<YarnContainerGroup> groups = managedGroups.getGroups();
 		assertThat(groups.size(), is(2));
 	}
 
 	@Test
 	public void testAddNode() {
 		YarnManagedContainerGroups managedGroups = createYmcgResolveAllToDefaultGroup();
 
 		Container container = mockContainer1();
 		DefaultYarnContainerNode node = new DefaultYarnContainerNode(container);
 		managedGroups.addContainerNode(node);
 
 		Collection<YarnContainerNode> nodes = managedGroups.getContainerNodes();
 		assertThat(nodes.size(), is(1));
 
 		YarnContainerGroup group = managedGroups.getGroupByMember(CID1);
 		assertThat(group.getId(), is(YarnManagedContainerGroups.DEFAULT_GROUP));
 
 	}
 
 	@Test
 	public void testContainerGridListener() {
 		YarnManagedContainerGroups managedGroups = createYmcgWithDefaults();
 
 		TestContainerGridListener listener = new TestContainerGridListener();
 		managedGroups.addContainerGridListener(listener);
 
 		Container container1 = mockContainer1();
 		DefaultYarnContainerNode node1 = new DefaultYarnContainerNode(container1);
 		managedGroups.addContainerNode(node1);
 
 		Container container2 = mockContainer2();
 		DefaultYarnContainerNode node2 = new DefaultYarnContainerNode(container2);
 		managedGroups.addContainerNode(node2);
 
 		assertThat(listener.containerNodeAdded, is(2));
 		assertThat(listener.containerNodeRemoved, is(0));
 
 		managedGroups.removeContainerNode(CID1);
 		assertThat(listener.containerNodeAdded, is(2));
 		assertThat(listener.containerNodeRemoved, is(1));
 		managedGroups.removeContainerNode(CID2);
 		assertThat(listener.containerNodeAdded, is(2));
 		assertThat(listener.containerNodeRemoved, is(2));
 
 		managedGroups.removeContainerNode(CID1);
 		managedGroups.removeContainerNode(CID2);
 		assertThat(listener.containerNodeAdded, is(2));
 		assertThat(listener.containerNodeRemoved, is(2));
 	}
 
 	@Test
 	public void testContainerGroupsListener() {
 		YarnManagedContainerGroups managedGroups = createYmcgWithDefaults();
 
		YarnContainerGroup group1 = new YarnContainerGroup(EXTRA_GROUP);

 		Container container1 = mockContainer1();
 		DefaultYarnContainerNode node1 = new DefaultYarnContainerNode(container1);
 
 		Container container2 = mockContainer2();
 		DefaultYarnContainerNode node2 = new DefaultYarnContainerNode(container2);
 
 		TestContainerGroupsListener listener = new TestContainerGroupsListener();
 		managedGroups.addContainerGroupsListener(listener);
		managedGroups.addGroup(group1);
 
 		assertThat(listener.groupAdded, is(1));
 		assertThat(listener.groupRemoved, is(0));
 		assertThat(listener.groupMemberAdded, is(0));
 		assertThat(listener.groupMemberRemoved, is(0));
 
		managedGroups.removeGroup(EXTRA_GROUP);
 		assertThat(listener.groupAdded, is(1));
 		assertThat(listener.groupRemoved, is(1));
 		assertThat(listener.groupMemberAdded, is(0));
 		assertThat(listener.groupMemberRemoved, is(0));
 
 		managedGroups.addContainerNode(node1);
 		assertThat(listener.groupAdded, is(1));
 		assertThat(listener.groupRemoved, is(1));
 		assertThat(listener.groupMemberAdded, is(1));
 		assertThat(listener.groupMemberRemoved, is(0));
 
 		managedGroups.addContainerNode(node2);
 		assertThat(listener.groupAdded, is(1));
 		assertThat(listener.groupRemoved, is(1));
 		assertThat(listener.groupMemberAdded, is(2));
 		assertThat(listener.groupMemberRemoved, is(0));
 
 		managedGroups.removeContainerNode(CID1);
 		assertThat(listener.groupAdded, is(1));
 		assertThat(listener.groupRemoved, is(1));
 		assertThat(listener.groupMemberAdded, is(2));
 		assertThat(listener.groupMemberRemoved, is(1));
 		assertThat(listener.lastGroup.getId(), is(YarnManagedContainerGroups.DEFAULT_FALLBACK_GROUP));
 
 		managedGroups.removeContainerNode(CID2);
 		assertThat(listener.groupAdded, is(1));
 		assertThat(listener.groupRemoved, is(1));
 		assertThat(listener.groupMemberAdded, is(2));
 		assertThat(listener.groupMemberRemoved, is(2));
 		assertThat(listener.lastGroup.getId(), is(YarnManagedContainerGroups.DEFAULT_FALLBACK_GROUP));
 	}
 
 	/**
 	 * Mocks a yarn container with hostname myhost1 and container id {@link #CID1}
 	 *
 	 * @return the mocked Yarn Container
 	 */
 	private Container mockContainer1() {
 		Container container = Records.newRecord(Container.class);
 		NodeId nodeId = Records.newRecord(NodeId.class);
 		nodeId.setHost(HOST1);
 		container.setNodeId(nodeId);
 		ContainerId containerId = ConverterUtils.toContainerId(CID1);
 		container.setId(containerId);
 		return container;
 	}
 
 	/**
 	 * Mocks a yarn container with hostname myhost2 and container id {@link #CID2}
 	 *
 	 * @return the mocked Yarn Container
 	 */
 	private Container mockContainer2() {
 		Container container = Records.newRecord(Container.class);
 		NodeId nodeId = Records.newRecord(NodeId.class);
 		nodeId.setHost(HOST2);
 		container.setNodeId(nodeId);
 		ContainerId containerId = ConverterUtils.toContainerId(CID2);
 		container.setId(containerId);
 		return container;
 	}
 
 	private YarnManagedContainerGroups createYmcgWithDefaults() {
 		GenericContainerGroupResolver groupResolver = new GenericContainerGroupResolver();
 		Map<String, List<String>> resolves = new Hashtable<String, List<String>>();
 		groupResolver.setResolves(resolves);
 		YarnManagedContainerGroups managedGroups = new YarnManagedContainerGroups();
 		managedGroups.setResolver(groupResolver);
 		return managedGroups;
 	}
 
 	private YarnManagedContainerGroups createYmcgResolveAllToDefaultGroup() {
 		GenericContainerGroupResolver groupResolver = new GenericContainerGroupResolver();
 
 		// resolving default to all
 		Map<String, List<String>> resolves = new Hashtable<String, List<String>>();
 		resolves.put(YarnManagedContainerGroups.DEFAULT_GROUP, Arrays.asList(new String[]{"*"}));
 		groupResolver.setResolves(resolves);
 
 		YarnManagedContainerGroups managedGroups = new YarnManagedContainerGroups(true);
 		managedGroups.setResolver(groupResolver);
 
 		// default group size to 1
 		Map<String, Integer> groupSizes = new Hashtable<String, Integer>();
 		groupSizes.put(YarnManagedContainerGroups.DEFAULT_GROUP, 1);
 		managedGroups.setGroupSizes(groupSizes);
 		return managedGroups;
 	}
 
 	private YarnManagedContainerGroups createYmcgResolveAllToTwoGroups() {
 		GenericContainerGroupResolver groupResolver = new GenericContainerGroupResolver();
 
 		// resolving default to all
 		Map<String, List<String>> resolves = new Hashtable<String, List<String>>();
 		resolves.put(YarnManagedContainerGroups.DEFAULT_GROUP, Arrays.asList(new String[]{"*"}));
 		resolves.put(EXTRA_GROUP, Arrays.asList(new String[]{"*"}));
 		groupResolver.setResolves(resolves);
 
 		YarnManagedContainerGroups managedGroups = new YarnManagedContainerGroups(true);
 		managedGroups.setResolver(groupResolver);
 
 		// default group size to 1
 		Map<String, Integer> groupSizes = new Hashtable<String, Integer>();
 		groupSizes.put(YarnManagedContainerGroups.DEFAULT_GROUP, 1);
 		groupSizes.put(EXTRA_GROUP, 1);
 		managedGroups.setGroupSizes(groupSizes);
 		return managedGroups;
 	}
 
 	/**
 	 * Test implementation of {@link ContainerGridListener}.
 	 */
 	private static class TestContainerGridListener implements ContainerGridListener<YarnContainerNode> {
 		public int containerNodeAdded;
 		public int containerNodeRemoved;
 		@Override
 		public void containerNodeAdded(YarnContainerNode node) {
 			containerNodeAdded++;
 		}
 		@Override
 		public void containerNodeRemoved(YarnContainerNode node) {
 			containerNodeRemoved++;
 		}
 	}
 
 	/**
 	 * Test implementation of {@link ContainerGroupsListener}.
 	 */
 	private static class TestContainerGroupsListener implements ContainerGroupsListener<YarnContainerGroup, YarnContainerNode> {
 		public int groupAdded;
 		public int groupRemoved;
 		public int groupMemberAdded;
 		public int groupMemberRemoved;
 		public YarnContainerGroup lastGroup;
 		public YarnContainerNode lastNode;
 		@Override
 		public void groupAdded(YarnContainerGroup group) {
 			groupAdded++;
 			lastGroup = group;
 		}
 		@Override
 		public void groupRemoved(YarnContainerGroup group) {
 			groupRemoved++;
 			lastGroup = group;
 		}
 		@Override
 		public void groupMemberAdded(YarnContainerGroup group, YarnContainerNode node) {
 			groupMemberAdded++;
 			lastGroup = group;
 			lastNode = node;
 		}
 		@Override
 		public void groupMemberRemoved(YarnContainerGroup group, YarnContainerNode node) {
 			groupMemberRemoved++;
 			lastGroup = group;
 			lastNode = node;
 		}
		public void reset() {
			groupAdded = 0;
			groupRemoved = 0;
			groupMemberAdded = 0;
			groupMemberRemoved = 0;
			lastGroup = null;
			lastNode = null;
		}
 	}
 
 }
