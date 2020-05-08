 // Copyright (C) 2004 Philip Aston
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.console.swingui;
 
 import junit.framework.TestCase;
 
 import javax.swing.event.TreeModelEvent;
 import javax.swing.event.TreeModelListener;
 import javax.swing.tree.TreeModel;
 import javax.swing.tree.TreePath;
 
 import net.grinder.console.common.Resources;
 import net.grinder.console.editor.Buffer;
 import net.grinder.console.editor.EditorModel;
 
 import net.grinder.console.editor.StringTextSource;
 import net.grinder.testutility.CallData;
 import net.grinder.testutility.RandomStubFactory;
 
 
 /**
 * Unit tests for {@link TestBufferTreeModel}.
  *
  * @author Philip Aston
  * @version $Revision$
  */
 public class TestBufferTreeModel extends TestCase {
 
   private static final Resources s_resources =
       new Resources("net.grinder.console.swingui.resources.Console");
 
   public void testConstructionAndGetChildMethods() throws Exception {
     final StringTextSource.Factory stringTextSourceFactory =
       new StringTextSource.Factory();
 
     final EditorModel editorModel =
       new EditorModel(s_resources, stringTextSourceFactory);
     editorModel.selectDefaultBuffer();
 
     final BufferTreeModel bufferTreeModel = new BufferTreeModel(editorModel);
     assertTrue(bufferTreeModel instanceof TreeModel);
 
     final Object rootNode = bufferTreeModel.getRoot();
     assertNotNull(rootNode);
     assertEquals(1, bufferTreeModel.getChildCount(rootNode));
     assertNull(bufferTreeModel.getChild(rootNode, 1));
 
     final BufferTreeModel.BufferNode bufferNode =
       (BufferTreeModel.BufferNode)bufferTreeModel.getChild(rootNode, 0);
     assertSame(bufferNode.getBuffer(), editorModel.getSelectedBuffer());
     assertTrue(bufferNode.belongsToModel(bufferTreeModel));
     assertEquals(bufferNode.getBuffer().getDisplayName(),
                  bufferNode.toString());
     final Object[] path = bufferNode.getPath().getPath();
     assertEquals(2, path.length);
     assertSame(rootNode, path[0]);
     assertSame(bufferNode, path[1]);
 
     final BufferTreeModel.BufferNode anotherBufferNode =
       new BufferTreeModel(editorModel).new BufferNode(null);
     assertFalse(anotherBufferNode.belongsToModel(bufferTreeModel));
 
     assertEquals(0, bufferTreeModel.getChildCount(bufferNode));
     assertNull(bufferTreeModel.getChild(bufferNode, 0));
 
     assertEquals(-1, bufferTreeModel.getIndexOfChild(null, bufferNode));
     assertEquals(-1, bufferTreeModel.getIndexOfChild(rootNode, null));
     assertEquals(-1, bufferTreeModel.getIndexOfChild(bufferNode, rootNode));
     assertEquals(-1,
                  bufferTreeModel.getIndexOfChild(rootNode, anotherBufferNode));
 
     assertEquals(0, bufferTreeModel.getIndexOfChild(rootNode, bufferNode));
 
     assertTrue(bufferTreeModel.isLeaf(bufferNode));
     assertFalse(bufferTreeModel.isLeaf(rootNode));
   }
 
   public void testSettersAndListeners() throws Exception {
     final StringTextSource.Factory stringTextSourceFactory =
       new StringTextSource.Factory();
 
     final EditorModel editorModel =
       new EditorModel(s_resources, stringTextSourceFactory);
 
     final BufferTreeModel bufferTreeModel = new BufferTreeModel(editorModel);
 
     final RandomStubFactory listener1StubFactory =
       new RandomStubFactory(TreeModelListener.class);
     final TreeModelListener listener1 =
       (TreeModelListener)listener1StubFactory.getStub();
 
     final RandomStubFactory listener2StubFactory =
       new RandomStubFactory(TreeModelListener.class);
     final TreeModelListener listener2 =
       (TreeModelListener)listener2StubFactory.getStub();
 
     bufferTreeModel.addTreeModelListener(listener1);
     bufferTreeModel.addTreeModelListener(listener2);
 
     final TreePath treePath = new TreePath(new Object());
     bufferTreeModel.valueForPathChanged(treePath, null);
 
     final CallData treeNodesChangedCallData =
       listener1StubFactory.assertSuccess("treeNodesChanged",
                                          TreeModelEvent.class);
     final TreeModelEvent event =
       (TreeModelEvent)treeNodesChangedCallData.getParameters()[0];
     assertSame(treePath, event.getTreePath());
     assertSame(bufferTreeModel, event.getSource());
     listener1StubFactory.assertNoMoreCalls();
 
     listener2StubFactory.assertSuccess("treeNodesChanged",
                                        TreeModelEvent.class);
     listener2StubFactory.assertNoMoreCalls();
     
     bufferTreeModel.removeTreeModelListener(listener1);
 
     // removeTreeModelListener() can calls equals() on the listeners.
     listener1StubFactory.resetCallHistory();
     listener2StubFactory.resetCallHistory();
 
     editorModel.selectDefaultBuffer();
 
     listener1StubFactory.assertNoMoreCalls();
     listener2StubFactory.assertNoMoreCalls();
 
     final EditorModel editorModel2 =
       new EditorModel(s_resources, stringTextSourceFactory);
     editorModel2.selectNewBuffer();
     final Buffer anotherBuffer = editorModel2.getSelectedBuffer();
     bufferTreeModel.bufferChanged(anotherBuffer);
 
     listener1StubFactory.assertNoMoreCalls();
     listener2StubFactory.assertNoMoreCalls();
 
     final Buffer buffer = editorModel.getSelectedBuffer();
     bufferTreeModel.bufferChanged(buffer);
 
     listener1StubFactory.assertNoMoreCalls();
     listener2StubFactory.assertSuccess("treeNodesChanged",
                                        TreeModelEvent.class);
 
     editorModel.selectNewBuffer();
 
     final CallData treeStructureChangedCallData =
       listener2StubFactory.assertSuccess("treeStructureChanged",
                                          TreeModelEvent.class);
     final TreeModelEvent treeStructureChangedEvent =
       (TreeModelEvent)treeStructureChangedCallData.getParameters()[0];
     assertSame(bufferTreeModel, treeStructureChangedEvent.getSource());
     assertEquals(1, treeStructureChangedEvent.getPath().length);
     assertSame(bufferTreeModel.getRoot(),
                treeStructureChangedEvent.getPath()[0]);
 
     listener1StubFactory.assertNoMoreCalls();
     listener2StubFactory.assertNoMoreCalls();
 
     editorModel.closeBuffer(buffer);
 
     final CallData treeStructureChangedCallData2 =
       listener2StubFactory.assertSuccess("treeStructureChanged",
                                          TreeModelEvent.class);
     final TreeModelEvent treeStructureChangedEvent2 =
       (TreeModelEvent)treeStructureChangedCallData2.getParameters()[0];
     assertSame(bufferTreeModel, treeStructureChangedEvent2.getSource());
     assertEquals(1, treeStructureChangedEvent2.getPath().length);
     assertSame(bufferTreeModel.getRoot(),
                treeStructureChangedEvent2.getPath()[0]);
 
     listener1StubFactory.assertNoMoreCalls();
     listener2StubFactory.assertNoMoreCalls();
   }
 }
