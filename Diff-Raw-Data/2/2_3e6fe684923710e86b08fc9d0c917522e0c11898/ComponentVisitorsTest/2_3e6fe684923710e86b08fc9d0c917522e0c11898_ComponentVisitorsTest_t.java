 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.hobsoft.symmetry.ui.traversal;
 
 import org.hobsoft.symmetry.ui.ComboBox;
 import org.hobsoft.symmetry.ui.Component;
 import org.hobsoft.symmetry.ui.Container;
 import org.hobsoft.symmetry.ui.Grid;
 import org.hobsoft.symmetry.ui.Table;
 import org.hobsoft.symmetry.ui.Tree;
 import org.hobsoft.symmetry.ui.model.TreePath;
 import org.hobsoft.symmetry.ui.test.DummyComponent;
 import org.hobsoft.symmetry.ui.test.FakeContainer;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.jmock.integration.junit4.JMock;
 import org.jmock.integration.junit4.JUnit4Mockery;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import com.google.common.reflect.TypeToken;
 
 import static org.hobsoft.symmetry.ui.test.traversal.MockComponentVisitors.createContainerVisitor;
 import static org.hobsoft.symmetry.ui.test.traversal.MockComponentVisitors.createGridVisitor;
 import static org.hobsoft.symmetry.ui.test.traversal.MockComponentVisitors.createHierarchicalVisitor;
 import static org.hobsoft.symmetry.ui.test.traversal.MockComponentVisitors.createListBoxVisitor;
 import static org.hobsoft.symmetry.ui.test.traversal.MockComponentVisitors.createTableVisitor;
 import static org.hobsoft.symmetry.ui.test.traversal.MockComponentVisitors.createTreeVisitor;
 import static org.hobsoft.symmetry.ui.traversal.ComponentVisitors.asContainerVisitor;
 import static org.hobsoft.symmetry.ui.traversal.ComponentVisitors.asGridVisitor;
 import static org.hobsoft.symmetry.ui.traversal.ComponentVisitors.asListBoxVisitor;
 import static org.hobsoft.symmetry.ui.traversal.ComponentVisitors.asTableVisitor;
 import static org.hobsoft.symmetry.ui.traversal.ComponentVisitors.asTreeVisitor;
 import static org.hobsoft.symmetry.ui.traversal.ComponentVisitors.skipChildren;
 import static org.hobsoft.symmetry.ui.traversal.ComponentVisitors.skipSiblings;
 import static org.hobsoft.symmetry.ui.traversal.HierarchicalComponentVisitor.EndVisit.SKIP_SIBLINGS;
 import static org.hobsoft.symmetry.ui.traversal.HierarchicalComponentVisitor.EndVisit.VISIT_SIBLINGS;
 import static org.hobsoft.symmetry.ui.traversal.HierarchicalComponentVisitor.Visit.SKIP_CHILDREN;
 import static org.hobsoft.symmetry.ui.traversal.HierarchicalComponentVisitor.Visit.VISIT_CHILDREN;
 import static org.junit.Assert.assertEquals;
 
 /**
  * Tests {@code ComponentVisitors}.
  * 
  * @author Mark Hobson
  * @see ComponentVisitors
  */
 @RunWith(JMock.class)
 public class ComponentVisitorsTest
 {
 	// types ------------------------------------------------------------------
 	
 	private static class GenericDummyComponent<T> extends DummyComponent
 	{
 		// simple subtype
 	}
 	
 	private static class ClassImplementationVisitor
 		implements HierarchicalComponentVisitor<DummyComponent, Void, RuntimeException>
 	{
 		@Override
 		public Visit visit(DummyComponent component, Void parameter)
 		{
 			return null;
 		}
 		
 		@Override
 		public EndVisit endVisit(DummyComponent component, Void parameter)
 		{
 			return null;
 		}
 	}
 
 	private static class ParameterizedTypeImplementationVisitor
 		implements HierarchicalComponentVisitor<GenericDummyComponent<?>, Void, RuntimeException>
 	{
 		@Override
 		public Visit visit(GenericDummyComponent<?> component, Void parameter)
 		{
 			return null;
 		}
 		
 		@Override
 		public EndVisit endVisit(GenericDummyComponent<?> component, Void parameter)
 		{
 			return null;
 		}
 	}
 	
 	private static class TypeVariableImplementationVisitor<T extends Component>
 		implements HierarchicalComponentVisitor<T, Void, RuntimeException>
 	{
 		@Override
 		public Visit visit(T component, Void parameter)
 		{
 			return null;
 		}
 		
 		@Override
 		public EndVisit endVisit(T component, Void parameter)
 		{
 			return null;
 		}
 	}
 
 	private static class SuperclassClassImplementationVisitor
 		extends NullHierarchicalComponentVisitor<DummyComponent, Void, RuntimeException>
 	{
 		// simple subtype
 	}
 	
	private interface IndirectHierarchicalComponentVisitor<T extends Component, P, E extends Exception>
 		extends HierarchicalComponentVisitor<T, P, E>
 	{
 		// simple subtype for level of indirection
 	}
 
 	private static class SuperinterfaceClassImplementationVisitor
 		implements IndirectHierarchicalComponentVisitor<DummyComponent, Void, RuntimeException>
 	{
 		@Override
 		public Visit visit(DummyComponent component, Void parameter)
 		{
 			return null;
 		}
 		
 		@Override
 		public EndVisit endVisit(DummyComponent component, Void parameter)
 		{
 			return null;
 		}
 	}
 	
 	// fields -----------------------------------------------------------------
 	
 	private final Mockery mockery = new JUnit4Mockery();
 
 	// skipChildren tests -----------------------------------------------------
 	
 	@Test
 	public void skipChildrenVisit() throws Exception
 	{
 		assertEquals(SKIP_CHILDREN, skipChildren().visit(null, null));
 	}
 	
 	@Test
 	public void skipChildrenEndVisit() throws Exception
 	{
 		assertEquals(VISIT_SIBLINGS, skipChildren().endVisit(null, null));
 	}
 	
 	@Test
 	public void skipChildrenWithDelegateVisit() throws Exception
 	{
 		final HierarchicalComponentVisitor<Component, String, RuntimeException> delegate = createHierarchicalVisitor(
 			mockery);
 		final DummyComponent component = new DummyComponent();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(delegate).visit(component, "x"); will(returnValue(VISIT_CHILDREN));
 		} });
 		
 		assertEquals(SKIP_CHILDREN, skipChildren(delegate).visit(component, "x"));
 	}
 	
 	@Test
 	public void skipChildrenWithDelegateEndVisit()
 	{
 		final HierarchicalComponentVisitor<Component, String, RuntimeException> delegate = createHierarchicalVisitor(
 			mockery);
 		final DummyComponent component = new DummyComponent();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(delegate).endVisit(component, "x"); will(returnValue(VISIT_SIBLINGS));
 		} });
 		
 		assertEquals(VISIT_SIBLINGS, skipChildren(delegate).endVisit(component, "x"));
 	}
 	
 	@Test
 	public void skipChildrenWithNullDelegateVisit() throws Exception
 	{
 		assertEquals(SKIP_CHILDREN, skipChildren(null).visit(null, null));
 	}
 	
 	@Test
 	public void skipChildrenWithNullDelegateEndVisit() throws Exception
 	{
 		assertEquals(VISIT_SIBLINGS, skipChildren(null).endVisit(null, null));
 	}
 	
 	// skipSiblings tests -----------------------------------------------------
 	
 	@Test
 	public void skipSiblingsVisit() throws Exception
 	{
 		assertEquals(VISIT_CHILDREN, skipSiblings().visit(null, null));
 	}
 	
 	@Test
 	public void skipSiblingsEndVisit() throws Exception
 	{
 		assertEquals(SKIP_SIBLINGS, skipSiblings().endVisit(null, null));
 	}
 	
 	@Test
 	public void skipSiblingsWithDelegateVisit() throws Exception
 	{
 		final HierarchicalComponentVisitor<Component, String, RuntimeException> delegate = createHierarchicalVisitor(
 			mockery);
 		final DummyComponent component = new DummyComponent();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(delegate).visit(component, "x"); will(returnValue(VISIT_CHILDREN));
 		} });
 		
 		assertEquals(VISIT_CHILDREN, skipSiblings(delegate).visit(component, "x"));
 	}
 	
 	@Test
 	public void skipSiblingsWithDelegateEndVisit()
 	{
 		final HierarchicalComponentVisitor<Component, String, RuntimeException> delegate = createHierarchicalVisitor(
 			mockery);
 		final DummyComponent component = new DummyComponent();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(delegate).endVisit(component, "x"); will(returnValue(VISIT_SIBLINGS));
 		} });
 		
 		assertEquals(SKIP_SIBLINGS, skipSiblings(delegate).endVisit(component, "x"));
 	}
 	
 	@Test
 	public void skipSiblingsWithNullDelegateVisit() throws Exception
 	{
 		assertEquals(VISIT_CHILDREN, skipSiblings(null).visit(null, null));
 	}
 	
 	@Test
 	public void skipSiblingsWithNullDelegateEndVisit() throws Exception
 	{
 		assertEquals(SKIP_SIBLINGS, skipSiblings(null).endVisit(null, null));
 	}
 	
 	// asContainerVisitor tests -----------------------------------------------
 	
 	@Test
 	public void asContainerVisitorVisitWithHierarchicalComponentVisitor()
 	{
 		final HierarchicalComponentVisitor<Container, String, RuntimeException> visitor = createHierarchicalVisitor(
 			mockery);
 		final Container container = new FakeContainer();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).visit(container, "x"); will(returnValue(VISIT_CHILDREN));
 		} });
 		
 		assertEquals(VISIT_CHILDREN, asContainerVisitor(visitor).visit(container, "x"));
 	}
 	
 	@Test
 	public void asContainerVisitorVisitWithContainerVisitor()
 	{
 		final ContainerVisitor<Container, String, RuntimeException> visitor = createContainerVisitor(mockery);
 		final Container container = new FakeContainer();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).visit(container, "x"); will(returnValue(VISIT_CHILDREN));
 		} });
 		
 		assertEquals(VISIT_CHILDREN, asContainerVisitor(visitor).visit(container, "x"));
 	}
 	
 	@Test
 	public void asContainerVisitorVisitWithNull()
 	{
 		ContainerVisitor<Container, String, RuntimeException> visitor = null;
 		
 		assertEquals(VISIT_CHILDREN, asContainerVisitor(visitor).visit(new FakeContainer(), "x"));
 	}
 	
 	@Test
 	public void asContainerVisitorEndVisitWithHierarchicalComponentVisitor()
 	{
 		final HierarchicalComponentVisitor<Container, String, RuntimeException> visitor = createHierarchicalVisitor(
 			mockery);
 		final Container container = new FakeContainer();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).endVisit(container, "x"); will(returnValue(VISIT_SIBLINGS));
 		} });
 		
 		assertEquals(VISIT_SIBLINGS, asContainerVisitor(visitor).endVisit(container, "x"));
 	}
 	
 	@Test
 	public void asContainerVisitorEndVisitWithContainerVisitor()
 	{
 		final ContainerVisitor<Container, String, RuntimeException> visitor = createContainerVisitor(mockery);
 		final Container container = new FakeContainer();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).endVisit(container, "x"); will(returnValue(VISIT_SIBLINGS));
 		} });
 		
 		assertEquals(VISIT_SIBLINGS, asContainerVisitor(visitor).endVisit(container, "x"));
 	}
 	
 	@Test
 	public void asContainerVisitorEndVisitWithNull()
 	{
 		ContainerVisitor<Container, String, RuntimeException> visitor = null;
 		
 		assertEquals(VISIT_SIBLINGS, asContainerVisitor(visitor).endVisit(new FakeContainer(), "x"));
 	}
 	
 	@Test
 	public void asContainerVisitorVisitChildWithHierarchicalComponentVisitor()
 	{
 		HierarchicalComponentVisitor<Container, String, RuntimeException> visitor = createHierarchicalVisitor(mockery);
 		
 		assertEquals(VISIT_CHILDREN, asContainerVisitor(visitor).visitChild(new FakeContainer(), "x", 1));
 	}
 	
 	@Test
 	public void asContainerVisitorVisitChildWithContainerVisitor()
 	{
 		final ContainerVisitor<Container, String, RuntimeException> visitor = createContainerVisitor(mockery);
 		final Container container = new FakeContainer();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).visitChild(container, "x", 1); will(returnValue(VISIT_CHILDREN));
 		} });
 		
 		assertEquals(VISIT_CHILDREN, asContainerVisitor(visitor).visitChild(container, "x", 1));
 	}
 	
 	@Test
 	public void asContainerVisitorVisitChildWithContainerVisitorWithNull()
 	{
 		ContainerVisitor<Container, String, RuntimeException> visitor = null;
 		
 		assertEquals(VISIT_CHILDREN, asContainerVisitor(visitor).visitChild(new FakeContainer(), "x", 1));
 	}
 	
 	@Test
 	public void asContainerVisitorEndVisitChildWithHierarchicalComponentVisitor()
 	{
 		HierarchicalComponentVisitor<Container, String, RuntimeException> visitor = createHierarchicalVisitor(mockery);
 		
 		assertEquals(VISIT_SIBLINGS, asContainerVisitor(visitor).endVisitChild(new FakeContainer(), "x", 1));
 	}
 	
 	@Test
 	public void asContainerVisitorEndVisitChildWithContainerVisitor()
 	{
 		final ContainerVisitor<Container, String, RuntimeException> visitor = createContainerVisitor(mockery);
 		final Container container = new FakeContainer();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).endVisitChild(container, "x", 1); will(returnValue(VISIT_SIBLINGS));
 		} });
 		
 		assertEquals(VISIT_SIBLINGS, asContainerVisitor(visitor).endVisitChild(container, "x", 1));
 	}
 	
 	@Test
 	public void asContainerVisitorEndVisitChildWithNull()
 	{
 		ContainerVisitor<Container, String, RuntimeException> visitor = null;
 		
 		assertEquals(VISIT_SIBLINGS, asContainerVisitor(visitor).endVisitChild(new FakeContainer(), "x", 1));
 	}
 	
 	// asGridVisitor tests ----------------------------------------------------
 	
 	@Test
 	public void asGridVisitorVisitWithHierarchicalComponentVisitor()
 	{
 		final HierarchicalComponentVisitor<Grid, String, RuntimeException> visitor = createHierarchicalVisitor(mockery);
 		final Grid grid = new Grid();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).visit(grid, "x"); will(returnValue(VISIT_CHILDREN));
 		} });
 		
 		assertEquals(VISIT_CHILDREN, asGridVisitor(visitor).visit(grid, "x"));
 	}
 	
 	@Test
 	public void asGridVisitorVisitWithContainerVisitor()
 	{
 		final ContainerVisitor<Grid, String, RuntimeException> visitor = createContainerVisitor(mockery);
 		final Grid grid = new Grid();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).visit(grid, "x"); will(returnValue(VISIT_CHILDREN));
 		} });
 		
 		assertEquals(VISIT_CHILDREN, asGridVisitor(visitor).visit(grid, "x"));
 	}
 	
 	@Test
 	public void asGridVisitorVisitWithGridVisitor()
 	{
 		final GridVisitor<Grid, String, RuntimeException> visitor = createGridVisitor(mockery);
 		final Grid grid = new Grid();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).visit(grid, "x"); will(returnValue(VISIT_CHILDREN));
 		} });
 		
 		assertEquals(VISIT_CHILDREN, asGridVisitor(visitor).visit(grid, "x"));
 	}
 	
 	@Test
 	public void asGridVisitorVisitWithNull()
 	{
 		GridVisitor<Grid, String, RuntimeException> visitor = null;
 		
 		assertEquals(VISIT_CHILDREN, asGridVisitor(visitor).visit(new Grid(), "x"));
 	}
 	
 	@Test
 	public void asGridVisitorEndVisitWithHierarchicalComponentVisitor()
 	{
 		final HierarchicalComponentVisitor<Grid, String, RuntimeException> visitor = createHierarchicalVisitor(mockery);
 		final Grid grid = new Grid();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).endVisit(grid, "x"); will(returnValue(VISIT_SIBLINGS));
 		} });
 		
 		assertEquals(VISIT_SIBLINGS, asGridVisitor(visitor).endVisit(grid, "x"));
 	}
 	
 	@Test
 	public void asGridVisitorEndVisitWithContainerVisitor()
 	{
 		final ContainerVisitor<Grid, String, RuntimeException> visitor = createContainerVisitor(mockery);
 		final Grid grid = new Grid();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).endVisit(grid, "x"); will(returnValue(VISIT_SIBLINGS));
 		} });
 		
 		assertEquals(VISIT_SIBLINGS, asGridVisitor(visitor).endVisit(grid, "x"));
 	}
 	
 	@Test
 	public void asGridVisitorEndVisitWithGridVisitor()
 	{
 		final GridVisitor<Grid, String, RuntimeException> visitor = createGridVisitor(mockery);
 		final Grid grid = new Grid();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).endVisit(grid, "x"); will(returnValue(VISIT_SIBLINGS));
 		} });
 		
 		assertEquals(VISIT_SIBLINGS, asGridVisitor(visitor).endVisit(grid, "x"));
 	}
 	
 	@Test
 	public void asGridVisitorEndVisitWithNull()
 	{
 		GridVisitor<Grid, String, RuntimeException> visitor = null;
 		
 		assertEquals(VISIT_SIBLINGS, asGridVisitor(visitor).endVisit(new Grid(), "x"));
 	}
 	
 	@Test
 	public void asGridVisitorVisitChildWithHierarchicalComponentVisitor()
 	{
 		HierarchicalComponentVisitor<Grid, String, RuntimeException> visitor = createHierarchicalVisitor(mockery);
 		
 		assertEquals(VISIT_CHILDREN, asGridVisitor(visitor).visitChild(new Grid(), "x", 1));
 	}
 	
 	@Test
 	public void asGridVisitorVisitChildWithContainerVisitor()
 	{
 		final ContainerVisitor<Grid, String, RuntimeException> visitor = createContainerVisitor(mockery);
 		final Grid grid = new Grid();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).visitChild(grid, "x", 1); will(returnValue(VISIT_CHILDREN));
 		} });
 		
 		assertEquals(VISIT_CHILDREN, asGridVisitor(visitor).visitChild(grid, "x", 1));
 	}
 	
 	@Test
 	public void asGridVisitorVisitChildWithGridVisitor()
 	{
 		final GridVisitor<Grid, String, RuntimeException> visitor = createGridVisitor(mockery);
 		final Grid grid = new Grid();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).visitChild(grid, "x", 1); will(returnValue(VISIT_CHILDREN));
 		} });
 		
 		assertEquals(VISIT_CHILDREN, asGridVisitor(visitor).visitChild(grid, "x", 1));
 	}
 	
 	@Test
 	public void asGridVisitorVisitChildWithNull()
 	{
 		GridVisitor<Grid, String, RuntimeException> visitor = null;
 		
 		assertEquals(VISIT_CHILDREN, asGridVisitor(visitor).visitChild(new Grid(), "x", 1));
 	}
 	
 	@Test
 	public void asGridVisitorEndVisitChildWithHierarchicalComponentVisitor()
 	{
 		HierarchicalComponentVisitor<Grid, String, RuntimeException> visitor = createHierarchicalVisitor(mockery);
 		
 		assertEquals(VISIT_SIBLINGS, asGridVisitor(visitor).endVisitChild(new Grid(), "x", 1));
 	}
 	
 	@Test
 	public void asGridVisitorEndVisitChildWithContainerVisitor()
 	{
 		final ContainerVisitor<Grid, String, RuntimeException> visitor = createContainerVisitor(mockery);
 		final Grid grid = new Grid();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).endVisitChild(grid, "x", 1); will(returnValue(VISIT_SIBLINGS));
 		} });
 		
 		assertEquals(VISIT_SIBLINGS, asGridVisitor(visitor).endVisitChild(grid, "x", 1));
 	}
 	
 	@Test
 	public void asGridVisitorEndVisitChildWithGridVisitor()
 	{
 		final GridVisitor<Grid, String, RuntimeException> visitor = createGridVisitor(mockery);
 		final Grid grid = new Grid();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).endVisitChild(grid, "x", 1); will(returnValue(VISIT_SIBLINGS));
 		} });
 		
 		assertEquals(VISIT_SIBLINGS, asGridVisitor(visitor).endVisitChild(grid, "x", 1));
 	}
 	
 	@Test
 	public void asGridVisitorEndVisitChildWithNull()
 	{
 		GridVisitor<Grid, String, RuntimeException> visitor = null;
 		
 		assertEquals(VISIT_SIBLINGS, asGridVisitor(visitor).endVisitChild(new Grid(), "x", 1));
 	}
 	
 	@Test
 	public void asGridVisitorVisitColumnsWithHierarchicalComponentVisitor()
 	{
 		HierarchicalComponentVisitor<Grid, String, RuntimeException> visitor = createHierarchicalVisitor(mockery);
 		
 		assertEquals(VISIT_CHILDREN, asGridVisitor(visitor).visitColumns(new Grid(), "x"));
 	}
 	
 	@Test
 	public void asGridVisitorVisitColumnsWithContainerVisitor()
 	{
 		ContainerVisitor<Grid, String, RuntimeException> visitor = createContainerVisitor(mockery);
 		
 		assertEquals(VISIT_CHILDREN, asGridVisitor(visitor).visitColumns(new Grid(), "x"));
 	}
 	
 	@Test
 	public void asGridVisitorVisitColumnsWithGridVisitor()
 	{
 		final GridVisitor<Grid, String, RuntimeException> visitor = createGridVisitor(mockery);
 		final Grid grid = new Grid();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).visitColumns(grid, "x"); will(returnValue(VISIT_CHILDREN));
 		} });
 		
 		assertEquals(VISIT_CHILDREN, asGridVisitor(visitor).visitColumns(grid, "x"));
 	}
 	
 	@Test
 	public void asGridVisitorVisitColumnsWithNull()
 	{
 		GridVisitor<Grid, String, RuntimeException> visitor = null;
 		
 		assertEquals(VISIT_CHILDREN, asGridVisitor(visitor).visitColumns(new Grid(), "x"));
 	}
 	
 	@Test
 	public void asGridVisitorVisitColumnWithHierarchicalComponentVisitor()
 	{
 		HierarchicalComponentVisitor<Grid, String, RuntimeException> visitor = createHierarchicalVisitor(mockery);
 		
 		assertEquals(VISIT_SIBLINGS, asGridVisitor(visitor).visitColumn(new Grid(), "x", 1));
 	}
 	
 	@Test
 	public void asGridVisitorVisitColumnWithContainerVisitor()
 	{
 		ContainerVisitor<Grid, String, RuntimeException> visitor = createContainerVisitor(mockery);
 		
 		assertEquals(VISIT_SIBLINGS, asGridVisitor(visitor).visitColumn(new Grid(), "x", 1));
 	}
 	
 	@Test
 	public void asGridVisitorVisitColumnWithGridVisitor()
 	{
 		final GridVisitor<Grid, String, RuntimeException> visitor = createGridVisitor(mockery);
 		final Grid grid = new Grid();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).visitColumn(grid, "x", 1); will(returnValue(VISIT_SIBLINGS));
 		} });
 		
 		assertEquals(VISIT_SIBLINGS, asGridVisitor(visitor).visitColumn(grid, "x", 1));
 	}
 	
 	@Test
 	public void asGridVisitorVisitColumnWithNull()
 	{
 		GridVisitor<Grid, String, RuntimeException> visitor = null;
 		
 		assertEquals(VISIT_SIBLINGS, asGridVisitor(visitor).visitColumn(new Grid(), "x", 1));
 	}
 	
 	@Test
 	public void asGridVisitorEndVisitColumnsWithHierarchicalComponentVisitor()
 	{
 		HierarchicalComponentVisitor<Grid, String, RuntimeException> visitor = createHierarchicalVisitor(mockery);
 		
 		assertEquals(VISIT_SIBLINGS, asGridVisitor(visitor).endVisitColumns(new Grid(), "x"));
 	}
 	
 	@Test
 	public void asGridVisitorEndVisitColumnsWithContainerVisitor()
 	{
 		ContainerVisitor<Grid, String, RuntimeException> visitor = createContainerVisitor(mockery);
 		
 		assertEquals(VISIT_SIBLINGS, asGridVisitor(visitor).endVisitColumns(new Grid(), "x"));
 	}
 	
 	@Test
 	public void asGridVisitorEndVisitColumnsWithGridVisitor()
 	{
 		final GridVisitor<Grid, String, RuntimeException> visitor = createGridVisitor(mockery);
 		final Grid grid = new Grid();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).endVisitColumns(grid, "x"); will(returnValue(VISIT_SIBLINGS));
 		} });
 		
 		assertEquals(VISIT_SIBLINGS, asGridVisitor(visitor).endVisitColumns(grid, "x"));
 	}
 	
 	@Test
 	public void asGridVisitorEndVisitColumnsWithNull()
 	{
 		GridVisitor<Grid, String, RuntimeException> visitor = null;
 		
 		assertEquals(VISIT_SIBLINGS, asGridVisitor(visitor).endVisitColumns(new Grid(), "x"));
 	}
 	
 	@Test
 	public void asGridVisitorVisitRowsWithHierarchicalComponentVisitor()
 	{
 		HierarchicalComponentVisitor<Grid, String, RuntimeException> visitor = createHierarchicalVisitor(mockery);
 		
 		assertEquals(VISIT_CHILDREN, asGridVisitor(visitor).visitRows(new Grid(), "x"));
 	}
 	
 	@Test
 	public void asGridVisitorVisitRowsWithContainerVisitor()
 	{
 		ContainerVisitor<Grid, String, RuntimeException> visitor = createContainerVisitor(mockery);
 		
 		assertEquals(VISIT_CHILDREN, asGridVisitor(visitor).visitRows(new Grid(), "x"));
 	}
 	
 	@Test
 	public void asGridVisitorVisitRowsWithGridVisitor()
 	{
 		final GridVisitor<Grid, String, RuntimeException> visitor = createGridVisitor(mockery);
 		final Grid grid = new Grid();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).visitRows(grid, "x"); will(returnValue(VISIT_CHILDREN));
 		} });
 		
 		assertEquals(VISIT_CHILDREN, asGridVisitor(visitor).visitRows(grid, "x"));
 	}
 	
 	@Test
 	public void asGridVisitorVisitRowsWithNull()
 	{
 		GridVisitor<Grid, String, RuntimeException> visitor = null;
 		
 		assertEquals(VISIT_CHILDREN, asGridVisitor(visitor).visitRows(new Grid(), "x"));
 	}
 	
 	@Test
 	public void asGridVisitorVisitRowWithHierarchicalComponentVisitor()
 	{
 		HierarchicalComponentVisitor<Grid, String, RuntimeException> visitor = createHierarchicalVisitor(mockery);
 		
 		assertEquals(VISIT_CHILDREN, asGridVisitor(visitor).visitRow(new Grid(), "x", 1));
 	}
 	
 	@Test
 	public void asGridVisitorVisitRowWithContainerVisitor()
 	{
 		ContainerVisitor<Grid, String, RuntimeException> visitor = createContainerVisitor(mockery);
 		
 		assertEquals(VISIT_CHILDREN, asGridVisitor(visitor).visitRow(new Grid(), "x", 1));
 	}
 	
 	@Test
 	public void asGridVisitorVisitRowWithGridVisitor()
 	{
 		final GridVisitor<Grid, String, RuntimeException> visitor = createGridVisitor(mockery);
 		final Grid grid = new Grid();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).visitRow(grid, "x", 1); will(returnValue(VISIT_CHILDREN));
 		} });
 		
 		assertEquals(VISIT_CHILDREN, asGridVisitor(visitor).visitRow(grid, "x", 1));
 	}
 	
 	@Test
 	public void asGridVisitorVisitRowWithNull()
 	{
 		GridVisitor<Grid, String, RuntimeException> visitor = null;
 		
 		assertEquals(VISIT_CHILDREN, asGridVisitor(visitor).visitRow(new Grid(), "x", 1));
 	}
 	
 	@Test
 	public void asGridVisitorEndVisitRowWithHierarchicalComponentVisitor()
 	{
 		HierarchicalComponentVisitor<Grid, String, RuntimeException> visitor = createHierarchicalVisitor(mockery);
 		
 		assertEquals(VISIT_SIBLINGS, asGridVisitor(visitor).endVisitRow(new Grid(), "x", 1));
 	}
 	
 	@Test
 	public void asGridVisitorEndVisitRowWithContainerVisitor()
 	{
 		ContainerVisitor<Grid, String, RuntimeException> visitor = createContainerVisitor(mockery);
 		
 		assertEquals(VISIT_SIBLINGS, asGridVisitor(visitor).endVisitRow(new Grid(), "x", 1));
 	}
 	
 	@Test
 	public void asGridVisitorEndVisitRowWithGridVisitor()
 	{
 		final GridVisitor<Grid, String, RuntimeException> visitor = createGridVisitor(mockery);
 		final Grid grid = new Grid();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).endVisitRow(grid, "x", 1); will(returnValue(VISIT_SIBLINGS));
 		} });
 		
 		assertEquals(VISIT_SIBLINGS, asGridVisitor(visitor).endVisitRow(grid, "x", 1));
 	}
 	
 	@Test
 	public void asGridVisitorEndVisitRowWithNull()
 	{
 		GridVisitor<Grid, String, RuntimeException> visitor = null;
 		
 		assertEquals(VISIT_SIBLINGS, asGridVisitor(visitor).endVisitRow(new Grid(), "x", 1));
 	}
 	
 	@Test
 	public void asGridVisitorEndVisitRowsWithHierarchicalComponentVisitor()
 	{
 		HierarchicalComponentVisitor<Grid, String, RuntimeException> visitor = createHierarchicalVisitor(mockery);
 		
 		assertEquals(VISIT_SIBLINGS, asGridVisitor(visitor).endVisitRows(new Grid(), "x"));
 	}
 	
 	@Test
 	public void asGridVisitorEndVisitRowsWithContainerVisitor()
 	{
 		ContainerVisitor<Grid, String, RuntimeException> visitor = createContainerVisitor(mockery);
 		
 		assertEquals(VISIT_SIBLINGS, asGridVisitor(visitor).endVisitRows(new Grid(), "x"));
 	}
 	
 	@Test
 	public void asGridVisitorEndVisitRowsWithGridVisitor()
 	{
 		final GridVisitor<Grid, String, RuntimeException> visitor = createGridVisitor(mockery);
 		final Grid grid = new Grid();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).endVisitRows(grid, "x"); will(returnValue(VISIT_SIBLINGS));
 		} });
 		
 		assertEquals(VISIT_SIBLINGS, asGridVisitor(visitor).endVisitRows(grid, "x"));
 	}
 	
 	@Test
 	public void asGridVisitorEndVisitRowsWithNull()
 	{
 		GridVisitor<Grid, String, RuntimeException> visitor = null;
 		
 		assertEquals(VISIT_SIBLINGS, asGridVisitor(visitor).endVisitRows(new Grid(), "x"));
 	}
 	
 	// asListBoxVisitor tests -------------------------------------------------
 	
 	@Test
 	public void asListBoxVisitorVisitWithHierarchicalComponentVisitor()
 	{
 		final HierarchicalComponentVisitor<ComboBox<?>, String, RuntimeException> visitor = createHierarchicalVisitor(
 			mockery);
 		final ComboBox<?> comboBox = new ComboBox<Object>();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).visit(comboBox, "x"); will(returnValue(VISIT_CHILDREN));
 		} });
 		
 		assertEquals(VISIT_CHILDREN, asListBoxVisitor(visitor).visit(comboBox, "x"));
 	}
 	
 	@Test
 	public void asListBoxVisitorVisitWithListBoxVisitor()
 	{
 		final ListBoxVisitor<ComboBox<?>, String, RuntimeException> visitor = createListBoxVisitor(mockery);
 		final ComboBox<?> comboBox = new ComboBox<Object>();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).visit(comboBox, "x"); will(returnValue(VISIT_CHILDREN));
 		} });
 		
 		assertEquals(VISIT_CHILDREN, asListBoxVisitor(visitor).visit(comboBox, "x"));
 	}
 	
 	@Test
 	public void asListBoxVisitorVisitWithNull()
 	{
 		ListBoxVisitor<ComboBox<?>, String, RuntimeException> visitor = null;
 		
 		assertEquals(VISIT_CHILDREN, asListBoxVisitor(visitor).visit(new ComboBox<Object>(), "x"));
 	}
 	
 	@Test
 	public void asListBoxVisitorEndVisitWithHierarchicalComponentVisitor()
 	{
 		final HierarchicalComponentVisitor<ComboBox<?>, String, RuntimeException> visitor = createHierarchicalVisitor(
 			mockery);
 		final ComboBox<?> comboBox = new ComboBox<Object>();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).endVisit(comboBox, "x"); will(returnValue(VISIT_SIBLINGS));
 		} });
 		
 		assertEquals(VISIT_SIBLINGS, asListBoxVisitor(visitor).endVisit(comboBox, "x"));
 	}
 	
 	@Test
 	public void asListBoxVisitorEndVisitWithListBoxVisitor()
 	{
 		final ListBoxVisitor<ComboBox<?>, String, RuntimeException> visitor = createListBoxVisitor(mockery);
 		final ComboBox<?> comboBox = new ComboBox<Object>();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).endVisit(comboBox, "x"); will(returnValue(VISIT_SIBLINGS));
 		} });
 		
 		assertEquals(VISIT_SIBLINGS, asListBoxVisitor(visitor).endVisit(comboBox, "x"));
 	}
 	
 	@Test
 	public void asListBoxVisitorEndVisitWithNull()
 	{
 		ListBoxVisitor<ComboBox<?>, String, RuntimeException> visitor = null;
 		
 		assertEquals(VISIT_SIBLINGS, asListBoxVisitor(visitor).endVisit(new ComboBox<Object>(), "x"));
 	}
 	
 	@Test
 	public void asListBoxVisitorVisitItemWithHierarchicalComponentVisitor()
 	{
 		HierarchicalComponentVisitor<ComboBox<?>, String, RuntimeException> visitor = createHierarchicalVisitor(
 			mockery);
 		
 		assertEquals(VISIT_CHILDREN, asListBoxVisitor(visitor).visitItem(new ComboBox<Object>(), "x", 1));
 	}
 	
 	@Test
 	public void asListBoxVisitorVisitItemWithListBoxVisitor()
 	{
 		final ListBoxVisitor<ComboBox<?>, String, RuntimeException> visitor = createListBoxVisitor(mockery);
 		final ComboBox<?> comboBox = new ComboBox<Object>();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).visitItem(comboBox, "x", 1); will(returnValue(VISIT_CHILDREN));
 		} });
 		
 		assertEquals(VISIT_CHILDREN, asListBoxVisitor(visitor).visitItem(comboBox, "x", 1));
 	}
 	
 	@Test
 	public void asListBoxVisitorVisitItemWithNull()
 	{
 		ListBoxVisitor<ComboBox<?>, String, RuntimeException> visitor = null;
 		
 		assertEquals(VISIT_CHILDREN, asListBoxVisitor(visitor).visitItem(new ComboBox<Object>(), "x", 1));
 	}
 	
 	@Test
 	public void asListBoxVisitorEndVisitItemWithHierarchicalComponentVisitor()
 	{
 		HierarchicalComponentVisitor<ComboBox<?>, String, RuntimeException> visitor = createHierarchicalVisitor(
 			mockery);
 		
 		assertEquals(VISIT_SIBLINGS, asListBoxVisitor(visitor).endVisitItem(new ComboBox<Object>(), "x", 1));
 	}
 	
 	@Test
 	public void asListBoxVisitorEndVisitItemWithListBoxVisitor()
 	{
 		final ListBoxVisitor<ComboBox<?>, String, RuntimeException> visitor = createListBoxVisitor(mockery);
 		final ComboBox<?> comboBox = new ComboBox<Object>();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).endVisitItem(comboBox, "x", 1); will(returnValue(VISIT_SIBLINGS));
 		} });
 		
 		assertEquals(VISIT_SIBLINGS, asListBoxVisitor(visitor).endVisitItem(comboBox, "x", 1));
 	}
 	
 	@Test
 	public void asListBoxVisitorEndVisitItemWithNull()
 	{
 		ListBoxVisitor<ComboBox<?>, String, RuntimeException> visitor = null;
 		
 		assertEquals(VISIT_SIBLINGS, asListBoxVisitor(visitor).endVisitItem(new ComboBox<Object>(), "x", 1));
 	}
 	
 	// asTableVisitor tests ---------------------------------------------------
 	
 	@Test
 	public void asTableVisitorVisitWithHierarchicalComponentVisitor()
 	{
 		final HierarchicalComponentVisitor<Table, String, RuntimeException> visitor = createHierarchicalVisitor(
 			mockery);
 		final Table table = new Table();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).visit(table, "x"); will(returnValue(VISIT_CHILDREN));
 		} });
 		
 		assertEquals(VISIT_CHILDREN, asTableVisitor(visitor).visit(table, "x"));
 	}
 	
 	@Test
 	public void asTableVisitorVisitWithTableVisitor()
 	{
 		final TableVisitor<Table, String, RuntimeException> visitor = createTableVisitor(mockery);
 		final Table table = new Table();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).visit(table, "x"); will(returnValue(VISIT_CHILDREN));
 		} });
 		
 		assertEquals(VISIT_CHILDREN, asTableVisitor(visitor).visit(table, "x"));
 	}
 	
 	@Test
 	public void asTableVisitorVisitWithNull()
 	{
 		TableVisitor<Table, String, RuntimeException> visitor = null;
 		
 		assertEquals(VISIT_CHILDREN, asTableVisitor(visitor).visit(new Table(), "x"));
 	}
 	
 	@Test
 	public void asTableVisitorEndVisitWithHierarchicalComponentVisitor()
 	{
 		final HierarchicalComponentVisitor<Table, String, RuntimeException> visitor = createHierarchicalVisitor(
 			mockery);
 		final Table table = new Table();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).endVisit(table, "x"); will(returnValue(VISIT_SIBLINGS));
 		} });
 		
 		assertEquals(VISIT_SIBLINGS, asTableVisitor(visitor).endVisit(table, "x"));
 	}
 	
 	@Test
 	public void asTableVisitorEndVisitWithTableVisitor()
 	{
 		final TableVisitor<Table, String, RuntimeException> visitor = createTableVisitor(mockery);
 		final Table table = new Table();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).endVisit(table, "x"); will(returnValue(VISIT_SIBLINGS));
 		} });
 		
 		assertEquals(VISIT_SIBLINGS, asTableVisitor(visitor).endVisit(table, "x"));
 	}
 	
 	@Test
 	public void asTableVisitorEndVisitWithNull()
 	{
 		TableVisitor<Table, String, RuntimeException> visitor = null;
 		
 		assertEquals(VISIT_SIBLINGS, asTableVisitor(visitor).endVisit(new Table(), "x"));
 	}
 	
 	@Test
 	public void asTableVisitorVisitHeaderWithHierarchicalComponentVisitor()
 	{
 		HierarchicalComponentVisitor<Table, String, RuntimeException> visitor = createHierarchicalVisitor(mockery);
 		
 		assertEquals(VISIT_CHILDREN, asTableVisitor(visitor).visitHeader(new Table(), "x"));
 	}
 	
 	@Test
 	public void asTableVisitorVisitHeaderWithTableVisitor()
 	{
 		final TableVisitor<Table, String, RuntimeException> visitor = createTableVisitor(mockery);
 		final Table table = new Table();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).visitHeader(table, "x"); will(returnValue(VISIT_CHILDREN));
 		} });
 		
 		assertEquals(VISIT_CHILDREN, asTableVisitor(visitor).visitHeader(table, "x"));
 	}
 	
 	@Test
 	public void asTableVisitorVisitHeaderWithNull()
 	{
 		TableVisitor<Table, String, RuntimeException> visitor = null;
 		
 		assertEquals(VISIT_CHILDREN, asTableVisitor(visitor).visitHeader(new Table(), "x"));
 	}
 	
 	@Test
 	public void asTableVisitorVisitHeaderCellWithHierarchicalComponentVisitor()
 	{
 		HierarchicalComponentVisitor<Table, String, RuntimeException> visitor = createHierarchicalVisitor(mockery);
 		
 		assertEquals(VISIT_CHILDREN, asTableVisitor(visitor).visitHeaderCell(new Table(), "x", 1));
 	}
 	
 	@Test
 	public void asTableVisitorVisitHeaderCellWithTableVisitor()
 	{
 		final TableVisitor<Table, String, RuntimeException> visitor = createTableVisitor(mockery);
 		final Table table = new Table();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).visitHeaderCell(table, "x", 1); will(returnValue(VISIT_CHILDREN));
 		} });
 		
 		assertEquals(VISIT_CHILDREN, asTableVisitor(visitor).visitHeaderCell(table, "x", 1));
 	}
 	
 	@Test
 	public void asTableVisitorVisitHeaderCellWithNull()
 	{
 		TableVisitor<Table, String, RuntimeException> visitor = null;
 		
 		assertEquals(VISIT_CHILDREN, asTableVisitor(visitor).visitHeaderCell(new Table(), "x", 1));
 	}
 	
 	@Test
 	public void asTableVisitorEndVisitHeaderCellWithHierarchicalComponentVisitor()
 	{
 		HierarchicalComponentVisitor<Table, String, RuntimeException> visitor = createHierarchicalVisitor(mockery);
 		
 		assertEquals(VISIT_SIBLINGS, asTableVisitor(visitor).endVisitHeaderCell(new Table(), "x", 1));
 	}
 	
 	@Test
 	public void asTableVisitorEndVisitHeaderCellWithTableVisitor()
 	{
 		final TableVisitor<Table, String, RuntimeException> visitor = createTableVisitor(mockery);
 		final Table table = new Table();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).endVisitHeaderCell(table, "x", 1); will(returnValue(VISIT_SIBLINGS));
 		} });
 		
 		assertEquals(VISIT_SIBLINGS, asTableVisitor(visitor).endVisitHeaderCell(table, "x", 1));
 	}
 	
 	@Test
 	public void asTableVisitorEndVisitHeaderCellWithNull()
 	{
 		TableVisitor<Table, String, RuntimeException> visitor = null;
 		
 		assertEquals(VISIT_SIBLINGS, asTableVisitor(visitor).endVisitHeaderCell(new Table(), "x", 1));
 	}
 	
 	@Test
 	public void asTableVisitorEndVisitHeaderWithHierarchicalComponentVisitor()
 	{
 		HierarchicalComponentVisitor<Table, String, RuntimeException> visitor = createHierarchicalVisitor(mockery);
 		
 		assertEquals(VISIT_SIBLINGS, asTableVisitor(visitor).endVisitHeader(new Table(), "x"));
 	}
 	
 	@Test
 	public void asTableVisitorEndVisitHeaderWithTableVisitor()
 	{
 		final TableVisitor<Table, String, RuntimeException> visitor = createTableVisitor(mockery);
 		final Table table = new Table();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).endVisitHeader(table, "x"); will(returnValue(VISIT_SIBLINGS));
 		} });
 		
 		assertEquals(VISIT_SIBLINGS, asTableVisitor(visitor).endVisitHeader(table, "x"));
 	}
 	
 	@Test
 	public void asTableVisitorEndVisitHeaderWithNull()
 	{
 		TableVisitor<Table, String, RuntimeException> visitor = null;
 		
 		assertEquals(VISIT_SIBLINGS, asTableVisitor(visitor).endVisitHeader(new Table(), "x"));
 	}
 	
 	@Test
 	public void asTableVisitorVisitBodyWithHierarchicalComponentVisitor()
 	{
 		HierarchicalComponentVisitor<Table, String, RuntimeException> visitor = createHierarchicalVisitor(mockery);
 		
 		assertEquals(VISIT_CHILDREN, asTableVisitor(visitor).visitBody(new Table(), "x"));
 	}
 	
 	@Test
 	public void asTableVisitorVisitBodyWithTableVisitor()
 	{
 		final TableVisitor<Table, String, RuntimeException> visitor = createTableVisitor(mockery);
 		final Table table = new Table();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).visitBody(table, "x"); will(returnValue(VISIT_CHILDREN));
 		} });
 		
 		assertEquals(VISIT_CHILDREN, asTableVisitor(visitor).visitBody(table, "x"));
 	}
 	
 	@Test
 	public void asTableVisitorVisitBodyWithNull()
 	{
 		TableVisitor<Table, String, RuntimeException> visitor = null;
 		
 		assertEquals(VISIT_CHILDREN, asTableVisitor(visitor).visitBody(new Table(), "x"));
 	}
 	
 	@Test
 	public void asTableVisitorVisitRowWithHierarchicalComponentVisitor()
 	{
 		HierarchicalComponentVisitor<Table, String, RuntimeException> visitor = createHierarchicalVisitor(mockery);
 		
 		assertEquals(VISIT_CHILDREN, asTableVisitor(visitor).visitRow(new Table(), "x", 1));
 	}
 	
 	@Test
 	public void asTableVisitorVisitRowWithTableVisitor()
 	{
 		final TableVisitor<Table, String, RuntimeException> visitor = createTableVisitor(mockery);
 		final Table table = new Table();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).visitRow(table, "x", 1); will(returnValue(VISIT_CHILDREN));
 		} });
 		
 		assertEquals(VISIT_CHILDREN, asTableVisitor(visitor).visitRow(table, "x", 1));
 	}
 	
 	@Test
 	public void asTableVisitorVisitRowWithNull()
 	{
 		TableVisitor<Table, String, RuntimeException> visitor = null;
 		
 		assertEquals(VISIT_CHILDREN, asTableVisitor(visitor).visitRow(new Table(), "x", 1));
 	}
 	
 	@Test
 	public void asTableVisitorVisitCellWithHierarchicalComponentVisitor()
 	{
 		HierarchicalComponentVisitor<Table, String, RuntimeException> visitor = createHierarchicalVisitor(mockery);
 		
 		assertEquals(VISIT_CHILDREN, asTableVisitor(visitor).visitCell(new Table(), "x", 1, 2));
 	}
 	
 	@Test
 	public void asTableVisitorVisitCellWithTableVisitor()
 	{
 		final TableVisitor<Table, String, RuntimeException> visitor = createTableVisitor(mockery);
 		final Table table = new Table();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).visitCell(table, "x", 1, 2); will(returnValue(VISIT_CHILDREN));
 		} });
 		
 		assertEquals(VISIT_CHILDREN, asTableVisitor(visitor).visitCell(table, "x", 1, 2));
 	}
 	
 	@Test
 	public void asTableVisitorVisitCellWithNull()
 	{
 		TableVisitor<Table, String, RuntimeException> visitor = null;
 		
 		assertEquals(VISIT_CHILDREN, asTableVisitor(visitor).visitCell(new Table(), "x", 1, 2));
 	}
 	
 	@Test
 	public void asTableVisitorEndVisitCellWithHierarchicalComponentVisitor()
 	{
 		HierarchicalComponentVisitor<Table, String, RuntimeException> visitor = createHierarchicalVisitor(mockery);
 		
 		assertEquals(VISIT_SIBLINGS, asTableVisitor(visitor).endVisitCell(new Table(), "x", 1, 2));
 	}
 	
 	@Test
 	public void asTableVisitorEndVisitCellWithTableVisitor()
 	{
 		final TableVisitor<Table, String, RuntimeException> visitor = createTableVisitor(mockery);
 		final Table table = new Table();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).endVisitCell(table, "x", 1, 2); will(returnValue(VISIT_SIBLINGS));
 		} });
 		
 		assertEquals(VISIT_SIBLINGS, asTableVisitor(visitor).endVisitCell(table, "x", 1, 2));
 	}
 	
 	@Test
 	public void asTableVisitorEndVisitCellWithNull()
 	{
 		TableVisitor<Table, String, RuntimeException> visitor = null;
 		
 		assertEquals(VISIT_SIBLINGS, asTableVisitor(visitor).endVisitCell(new Table(), "x", 1, 2));
 	}
 	
 	@Test
 	public void asTableVisitorEndVisitRowWithHierarchicalComponentVisitor()
 	{
 		HierarchicalComponentVisitor<Table, String, RuntimeException> visitor = createHierarchicalVisitor(mockery);
 		
 		assertEquals(VISIT_SIBLINGS, asTableVisitor(visitor).endVisitRow(new Table(), "x", 1));
 	}
 	
 	@Test
 	public void asTableVisitorEndVisitRowWithTableVisitor()
 	{
 		final TableVisitor<Table, String, RuntimeException> visitor = createTableVisitor(mockery);
 		final Table table = new Table();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).endVisitRow(table, "x", 1); will(returnValue(VISIT_SIBLINGS));
 		} });
 		
 		assertEquals(VISIT_SIBLINGS, asTableVisitor(visitor).endVisitRow(table, "x", 1));
 	}
 	
 	@Test
 	public void asTableVisitorEndVisitRowWithNull()
 	{
 		TableVisitor<Table, String, RuntimeException> visitor = null;
 		
 		assertEquals(VISIT_SIBLINGS, asTableVisitor(visitor).endVisitRow(new Table(), "x", 1));
 	}
 	
 	@Test
 	public void asTableVisitorEndVisitBodyWithHierarchicalComponentVisitor()
 	{
 		HierarchicalComponentVisitor<Table, String, RuntimeException> visitor = createHierarchicalVisitor(mockery);
 		
 		assertEquals(VISIT_SIBLINGS, asTableVisitor(visitor).endVisitBody(new Table(), "x"));
 	}
 	
 	@Test
 	public void asTableVisitorEndVisitBodyWithTableVisitor()
 	{
 		final TableVisitor<Table, String, RuntimeException> visitor = createTableVisitor(mockery);
 		final Table table = new Table();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).endVisitBody(table, "x"); will(returnValue(VISIT_SIBLINGS));
 		} });
 		
 		assertEquals(VISIT_SIBLINGS, asTableVisitor(visitor).endVisitBody(table, "x"));
 	}
 	
 	@Test
 	public void asTableVisitorEndVisitBodyWithNull()
 	{
 		TableVisitor<Table, String, RuntimeException> visitor = null;
 		
 		assertEquals(VISIT_SIBLINGS, asTableVisitor(visitor).endVisitBody(new Table(), "x"));
 	}
 	
 	// asTreeVisitor tests ----------------------------------------------------
 	
 	@Test
 	public void asTreeVisitorVisitWithHierarchicalComponentVisitor()
 	{
 		final HierarchicalComponentVisitor<Tree, String, RuntimeException> visitor = createHierarchicalVisitor(mockery);
 		final Tree tree = new Tree();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).visit(tree, "x"); will(returnValue(VISIT_CHILDREN));
 		} });
 		
 		assertEquals(VISIT_CHILDREN, asTreeVisitor(visitor).visit(tree, "x"));
 	}
 	
 	@Test
 	public void asTreeVisitorVisitWithTreeVisitor()
 	{
 		final TreeVisitor<Tree, String, RuntimeException> visitor = createTreeVisitor(mockery);
 		final Tree tree = new Tree();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).visit(tree, "x"); will(returnValue(VISIT_CHILDREN));
 		} });
 		
 		assertEquals(VISIT_CHILDREN, asTreeVisitor(visitor).visit(tree, "x"));
 	}
 	
 	@Test
 	public void asTreeVisitorVisitWithNull()
 	{
 		TreeVisitor<Tree, String, RuntimeException> visitor = null;
 		
 		assertEquals(VISIT_CHILDREN, asTreeVisitor(visitor).visit(new Tree(), "x"));
 	}
 	
 	@Test
 	public void asTreeVisitorEndVisitWithHierarchicalComponentVisitor()
 	{
 		final HierarchicalComponentVisitor<Tree, String, RuntimeException> visitor = createHierarchicalVisitor(mockery);
 		final Tree tree = new Tree();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).endVisit(tree, "x"); will(returnValue(VISIT_SIBLINGS));
 		} });
 		
 		assertEquals(VISIT_SIBLINGS, asTreeVisitor(visitor).endVisit(tree, "x"));
 	}
 	
 	@Test
 	public void asTreeVisitorEndVisitWithTreeVisitor()
 	{
 		final TreeVisitor<Tree, String, RuntimeException> visitor = createTreeVisitor(mockery);
 		final Tree tree = new Tree();
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).endVisit(tree, "x"); will(returnValue(VISIT_SIBLINGS));
 		} });
 		
 		assertEquals(VISIT_SIBLINGS, asTreeVisitor(visitor).endVisit(tree, "x"));
 	}
 	
 	@Test
 	public void asTreeVisitorEndVisitWithNull()
 	{
 		TreeVisitor<Tree, String, RuntimeException> visitor = null;
 		
 		assertEquals(VISIT_SIBLINGS, asTreeVisitor(visitor).endVisit(new Tree(), "x"));
 	}
 	
 	@Test
 	public void asTreeVisitorVisitNodeWithHierarchicalComponentVisitor()
 	{
 		HierarchicalComponentVisitor<Tree, String, RuntimeException> visitor = createHierarchicalVisitor(mockery);
 		
 		assertEquals(VISIT_CHILDREN, asTreeVisitor(visitor).visitNode(new Tree(), "x", new TreePath("y")));
 	}
 	
 	@Test
 	public void asTreeVisitorVisitNodeWithTreeVisitor()
 	{
 		final TreeVisitor<Tree, String, RuntimeException> visitor = createTreeVisitor(mockery);
 		final Tree tree = new Tree();
 		final TreePath path = new TreePath("y");
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).visitNode(tree, "x", path); will(returnValue(VISIT_CHILDREN));
 		} });
 		
 		assertEquals(VISIT_CHILDREN, asTreeVisitor(visitor).visitNode(tree, "x", path));
 	}
 	
 	@Test
 	public void asTreeVisitorVisitNodeWithNull()
 	{
 		TreeVisitor<Tree, String, RuntimeException> visitor = null;
 		
 		assertEquals(VISIT_CHILDREN, asTreeVisitor(visitor).visitNode(new Tree(), "x", new TreePath("y")));
 	}
 	
 	@Test
 	public void asTreeVisitorVisitNodeChildrenWithHierarchicalComponentVisitor()
 	{
 		HierarchicalComponentVisitor<Tree, String, RuntimeException> visitor = createHierarchicalVisitor(mockery);
 		
 		assertEquals(VISIT_CHILDREN, asTreeVisitor(visitor).visitNodeChildren(new Tree(), "x", new TreePath("y")));
 	}
 	
 	@Test
 	public void asTreeVisitorVisitNodeChildrenWithTreeVisitor()
 	{
 		final TreeVisitor<Tree, String, RuntimeException> visitor = createTreeVisitor(mockery);
 		final Tree tree = new Tree();
 		final TreePath path = new TreePath("y");
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).visitNodeChildren(tree, "x", path); will(returnValue(VISIT_CHILDREN));
 		} });
 		
 		assertEquals(VISIT_CHILDREN, asTreeVisitor(visitor).visitNodeChildren(tree, "x", path));
 	}
 	
 	@Test
 	public void asTreeVisitorVisitNodeChildrenWithNull()
 	{
 		TreeVisitor<Tree, String, RuntimeException> visitor = null;
 		
 		assertEquals(VISIT_CHILDREN, asTreeVisitor(visitor).visitNodeChildren(new Tree(), "x", new TreePath("y")));
 	}
 	
 	@Test
 	public void asTreeVisitorEndVisitNodeChildrenWithHierarchicalComponentVisitor()
 	{
 		HierarchicalComponentVisitor<Tree, String, RuntimeException> visitor = createHierarchicalVisitor(mockery);
 		
 		assertEquals(VISIT_SIBLINGS, asTreeVisitor(visitor).endVisitNodeChildren(new Tree(), "x", new TreePath("y")));
 	}
 	
 	@Test
 	public void asTreeVisitorEndVisitNodeChildrenWithTreeVisitor()
 	{
 		final TreeVisitor<Tree, String, RuntimeException> visitor = createTreeVisitor(mockery);
 		final Tree tree = new Tree();
 		final TreePath path = new TreePath("y");
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).endVisitNodeChildren(tree, "x", path); will(returnValue(VISIT_SIBLINGS));
 		} });
 		
 		assertEquals(VISIT_SIBLINGS, asTreeVisitor(visitor).endVisitNodeChildren(tree, "x", path));
 	}
 	
 	@Test
 	public void asTreeVisitorEndVisitNodeChildrenWithNull()
 	{
 		TreeVisitor<Tree, String, RuntimeException> visitor = null;
 		
 		assertEquals(VISIT_SIBLINGS, asTreeVisitor(visitor).endVisitNodeChildren(new Tree(), "x", new TreePath("y")));
 	}
 	
 	@Test
 	public void asTreeVisitorEndVisitNodeWithHierarchicalComponentVisitor()
 	{
 		HierarchicalComponentVisitor<Tree, String, RuntimeException> visitor = createHierarchicalVisitor(mockery);
 		
 		assertEquals(VISIT_SIBLINGS, asTreeVisitor(visitor).endVisitNode(new Tree(), "x", new TreePath("y")));
 	}
 	
 	@Test
 	public void asTreeVisitorEndVisitNodeWithTreeVisitor()
 	{
 		final TreeVisitor<Tree, String, RuntimeException> visitor = createTreeVisitor(mockery);
 		final Tree tree = new Tree();
 		final TreePath path = new TreePath("y");
 		
 		mockery.checking(new Expectations() { {
 			oneOf(visitor).endVisitNode(tree, "x", path); will(returnValue(VISIT_SIBLINGS));
 		} });
 		
 		assertEquals(VISIT_SIBLINGS, asTreeVisitor(visitor).endVisitNode(tree, "x", path));
 	}
 	
 	@Test
 	public void asTreeVisitorEndVisitNodeWithNull()
 	{
 		TreeVisitor<Tree, String, RuntimeException> visitor = null;
 		
 		assertEquals(VISIT_SIBLINGS, asTreeVisitor(visitor).endVisitNode(new Tree(), "x", new TreePath("y")));
 	}
 	
 	// getComponentType tests -------------------------------------------------
 	
 	@Test
 	public void getComponentTypeWithClassImplementation()
 	{
 		assertEquals(TypeToken.of(DummyComponent.class),
 			ComponentVisitors.getComponentType(new ClassImplementationVisitor()));
 	}
 	
 	@Test
 	public void getComponentTypeWithParameterizedTypeImplementation()
 	{
 		assertEquals(new TypeToken<GenericDummyComponent<?>>() { /**/ },
 			ComponentVisitors.getComponentType(new ParameterizedTypeImplementationVisitor()));
 	}
 	
 	@Test(expected = IllegalArgumentException.class)
 	public void getComponentTypeWithTypeVariableImplementation()
 	{
 		ComponentVisitors.getComponentType(new TypeVariableImplementationVisitor<DummyComponent>());
 	}
 
 	@Test
 	public void getComponentTypeWithSuperclassClassImplementation()
 	{
 		assertEquals(TypeToken.of(DummyComponent.class),
 			ComponentVisitors.getComponentType(new SuperclassClassImplementationVisitor()));
 	}
 	
 	@Test
 	public void getComponentTypeWithSuperinterfaceClassImplementation()
 	{
 		assertEquals(TypeToken.of(DummyComponent.class),
 			ComponentVisitors.getComponentType(new SuperinterfaceClassImplementationVisitor()));
 	}
 }
