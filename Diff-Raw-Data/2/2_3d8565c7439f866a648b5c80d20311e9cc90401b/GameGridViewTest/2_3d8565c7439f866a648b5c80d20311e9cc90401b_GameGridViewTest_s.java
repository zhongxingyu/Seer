 package net.todd.games.boardgame;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertSame;
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.media.j3d.BranchGroup;
 import javax.media.j3d.Node;
 
 import net.todd.common.uitools.IListener;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class GameGridViewTest {
 	private BranchGroupFactoryStub branchGroupFactory;
 	private BranchGroupStub branchGroup;
 
 	@Before
 	public void setup() {
 		branchGroup = new BranchGroupStub();
 		branchGroupFactory = new BranchGroupFactoryStub();
 		branchGroupFactory.branchGroup = branchGroup;
 	}
 
 	@Test
 	public void testViewNotifiesItsListenersWhenPickerListenerFiresWithATileSelectedNode() {
 		Tile tile = TileFixture.getTile();
 		PickerStub picker = new PickerStub();
 		PickerFactoryStub pickerFactory = new PickerFactoryStub();
 		pickerFactory.picker = picker;
 		picker.selectedNode = tile;
 		GameGridView gameGridView = new GameGridView(pickerFactory, branchGroupFactory);
 		ListenerStub listener1 = new ListenerStub();
 		ListenerStub listener2 = new ListenerStub();
 		gameGridView.addTileSelectedListener(listener1);
 		gameGridView.addTileSelectedListener(listener2);
 
 		assertFalse(listener1.fired);
 		assertFalse(listener2.fired);
 		assertNull(gameGridView.getSelectedTile());
 
 		picker.listener.fireEvent();
 
 		assertTrue(listener1.fired);
 		assertTrue(listener2.fired);
 		assertSame(tile.getTileData(), gameGridView.getSelectedTile());
 	}
 
 	@Test
 	public void testViewReturnsBranchCreatedByTheFactory() {
 		PickerStub picker = new PickerStub();
 		PickerFactoryStub pickerFactory = new PickerFactoryStub();
 		pickerFactory.picker = picker;
 		GameGridView gameGridView = new GameGridView(pickerFactory, branchGroupFactory);
 
 		assertSame(branchGroup, gameGridView.getBranchGroup());
 	}
 
 	@Test
 	public void testViewConstructsGridGivenTileData() {
 		PickerStub picker = new PickerStub();
 		PickerFactoryStub pickerFactory = new PickerFactoryStub();
 		pickerFactory.picker = picker;
 		GameGridView gameGridView = new GameGridView(pickerFactory, branchGroupFactory);
 		TileData tileDatum1 = TileFixture.getTileData();
 		TileData tileDatum2 = TileFixture.getTileData();
 		TileData tileDatum3 = TileFixture.getTileData();
 		TileData tileDatum4 = TileFixture.getTileData();
 		TileData[][] tileData = new TileData[][] { { tileDatum1, tileDatum2 },
 				{ tileDatum3, tileDatum4 } };
 
 		gameGridView.constructGrid(tileData);
 
 		assertEquals(4, branchGroup.nodes.size());
 		assertEquals(tileDatum1, ((Tile) branchGroup.nodes.get(0)).getTileData());
 		assertEquals(tileDatum2, ((Tile) branchGroup.nodes.get(1)).getTileData());
 		assertEquals(tileDatum3, ((Tile) branchGroup.nodes.get(2)).getTileData());
 		assertEquals(tileDatum4, ((Tile) branchGroup.nodes.get(3)).getTileData());
 	}
 
 	private static class PickerFactoryStub implements IPickerFactory {
 		IPicker picker;
 
 		public IPicker createPicker(IBranchGroup branchGroup) {
 			return picker;
 		}
 	}
 
 	private static class PickerStub implements IPicker {
 		Tile selectedNode;
 		IListener listener;
 
 		public void addListener(IListener listener) {
 			this.listener = listener;
 		}
 
 		public Node getSelectedNode() {
 			return selectedNode;
 		}
 
 		public void removeListener(IListener listener) {
 			throw new UnsupportedOperationException();
 		}
 	}
 
 	private static class ListenerStub implements IListener {
 		boolean fired;
 
 		public void fireEvent() {
 			fired = true;
 		}
 	}
 
 	private static class BranchGroupFactoryStub implements IBranchGroupFactory {
 		BranchGroupStub branchGroup;
 
 		public IBranchGroup createBranchGroup() {
 			return branchGroup;
 		}
 	}
 
 	private static class BranchGroupStub implements IBranchGroup {
 		List<Node> nodes = new ArrayList<Node>();
 
 		public void addChild(Node node) {
 			nodes.add(node);
 		}
 
 		public void addChild(IBranchGroup child) {
			throw new UnsupportedOperationException();
 		}
 
 		public void compile() {
 			throw new UnsupportedOperationException();
 		}
 
 		public BranchGroup getInternal() {
 			throw new UnsupportedOperationException();
 		}
 
 		public void removeAllChildren() {
 			throw new UnsupportedOperationException();
 		}
 	}
 }
