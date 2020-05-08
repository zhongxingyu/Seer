 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.menu.classic.application;
 
 import static org.easymock.classextension.EasyMock.createNiceMock;
 
 import java.util.List;
 import java.util.LinkedList;
 
 import junit.framework.TestCase;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.globant.katari.core.security.MenuAccessFilterer;
 import com.globant.katari.core.web.MenuBar;
 import com.globant.katari.core.web.MenuNode;
 
 import com.globant.katari.core.security.SecureUrlAccessHelper;
 
 /* Test case for the menu bar component
  */
 public class MenuDisplayHelperTest extends TestCase {
 
   private static Log log = LogFactory.getLog(MenuDisplayHelperTest.class);
 
   private MenuBar menuBar;
   private MenuAccessFilterer filterer;
 
   @Override
   public void setUp() {
 
     menuBar = new MenuBar("root", "root");
 
     // create child nodes
     /*                 root           0     */
     /*                /    \                */
     /*               a      b         1     */
     /*              / \   / | \             */
     /*             a   b a  bb b      2     */
     /*            /   /     | / \           */
     /*           a   a      ba   b    3     */
     MenuNode a = new MenuNode(menuBar, "a", "a", 0, null);
     MenuNode b = new MenuNode(menuBar, "b", "b", 0, null);
 
     MenuNode a_a = new MenuNode(a, "a", "a", 0, null);
     MenuNode a_b = new MenuNode(a, "b", "b", 0, null);
 
     new MenuNode(b, "a", "a", 0, null, "link_b_a");
     MenuNode b_bb = new MenuNode(b, "bb", "bb", 0, null);
     MenuNode b_b = new MenuNode(b, "b", "b", 0, null);
 
     new MenuNode(a_a, "a", "a", 0, null, "link_a_a");
     new MenuNode(a_b, "a", "a", 0, null, "link_a_b");
 
     new MenuNode(b_bb, "b", "b", 0, null, "link_b_bb_b");
 
     new MenuNode(b_b, "a", "a", 0, null, "link_b_b_a");
     new MenuNode(b_b, "b", "b", 0, null, "link_b_b_b");
 
     SecureUrlAccessHelper helper = createNiceMock(SecureUrlAccessHelper.class);
     filterer = new MenuAccessFilterer(helper) {
       public List<MenuNode> filterMenuNodes(final List<MenuNode> nodes) {
         return nodes;
       }
     };
   }
 
   public void testGetMenuNodesForLevel() {
 
     MenuDisplayHelper helper = new MenuDisplayHelper(menuBar, filterer);
 
     List<MenuNodeDisplay> nodes;
     nodes = helper.getMenuNodesForLevel("/root/a/b/a", 2);
     assertEquals(2, nodes.size());
     assertTrue(!nodes.get(0).isSelected());
     assertTrue(nodes.get(1).isSelected());
 
     nodes = helper.getMenuNodesForLevel("/root/b/bb/b", 2);
     assertEquals(3, nodes.size());
     assertTrue(!nodes.get(0).isSelected());
     assertEquals("a", nodes.get(0).getMenuNode().getName());
     assertTrue(nodes.get(1).isSelected());
     assertEquals("bb", nodes.get(1).getMenuNode().getName());
     assertTrue(!nodes.get(2).isSelected());
     assertEquals("b", nodes.get(2).getMenuNode().getName());
 
     nodes = helper.getMenuNodesForLevel("/root/a/b/a", 3);
     assertEquals(1, nodes.size());
     assertTrue(nodes.get(0).isSelected());
     assertEquals("a", nodes.get(0).getMenuNode().getName());
     assertEquals("/root/a/b/a", nodes.get(0).getMenuNode().getPath());
   }
 
   public void testGetMenuNodesForLevel_onleaf() {
     MenuDisplayHelper helper = new MenuDisplayHelper(menuBar, filterer);
     helper.getMenuNodesForLevel("/root/a/b/a", 4);
   }
 
   public void testGetMenuNodesForPath() {
     MenuDisplayHelper helper = new MenuDisplayHelper(menuBar, filterer);
 
     List<MenuNodeDisplay> nodes;
     nodes = helper.getMenuNodesForPath("/root/b/b");
     assertEquals(2, nodes.size());
     assertEquals("a", nodes.get(0).getMenuNode().getName());
     assertEquals("b", nodes.get(1).getMenuNode().getName());
   }
 
   /* Tests what happens if the first child node is not accessible. The link of
    * the parent node should not point to the non accesible url.
    */
  public void testListPath_firstWithoutPermission() {
 
     // Simulates that the node /b/a is not accessible.
     SecureUrlAccessHelper accessHelper;
     accessHelper = createNiceMock(SecureUrlAccessHelper.class);
     filterer = new MenuAccessFilterer(accessHelper) {
       public List<MenuNode> filterMenuNodes(final List<MenuNode> nodes) {
         log.trace("Entering filterMenuNodes");
         List<MenuNode> result = new LinkedList<MenuNode>();
         for (MenuNode node : nodes) {
           if (!node.getPath().equals("/root/b/b/a")) {
             log.debug("Adding " + node.getPath());
             result.add(node);
           } else {
             log.debug("Skipping " + node.getPath());
           }
         }
         log.trace("Leaving filterMenuNodes");
         return result;
       }
     };
 
     MenuDisplayHelper helper = new MenuDisplayHelper(menuBar, filterer);
     List<MenuNodeDisplay> nodes;
 
     nodes = helper.getMenuNodesForLevel("/root/b/bb/b", 2);
 
     assertEquals(3, nodes.size());
     assertTrue(!nodes.get(0).isSelected());
     assertEquals("a", nodes.get(0).getMenuNode().getName());
     assertTrue(nodes.get(1).isSelected());
     assertEquals("bb", nodes.get(1).getMenuNode().getName());
     assertTrue(!nodes.get(2).isSelected());
     assertEquals("b", nodes.get(2).getMenuNode().getName());
     assertEquals("link_b_b_b", nodes.get(2).getLinkPath());
   }
 
   /* Tests that a non-leaf nodes with no accessible descendents is eliminated
    * from the list of displayable menus.
    */
   public void testGetMenuNodesForLevel_noAccessibleDescendants() {
 
     // Simulates that the node /b/a is not accessible.
     SecureUrlAccessHelper accessHelper;
     accessHelper = createNiceMock(SecureUrlAccessHelper.class);
     filterer = new MenuAccessFilterer(accessHelper) {
       public List<MenuNode> filterMenuNodes(final List<MenuNode> nodes) {
         log.trace("Entering filterMenuNodes");
         List<MenuNode> result = new LinkedList<MenuNode>();
         for (MenuNode node : nodes) {
           if (!node.getPath().equals("/root/b/b/a")
               && !node.getPath().equals("/root/b/b/b")) {
             log.debug("Adding " + node.getPath());
             result.add(node);
           } else {
             log.debug("Skipping " + node.getPath());
           }
         }
         log.trace("Leaving filterMenuNodes");
         return result;
       }
     };
 
     MenuDisplayHelper helper = new MenuDisplayHelper(menuBar, filterer);
     List<MenuNodeDisplay> nodes;
 
     nodes = helper.getMenuNodesForLevel("/root/b/bb/b", 2);
 
     assertEquals(3, nodes.size());
     assertTrue(!nodes.get(0).isSelected());
     assertEquals("a", nodes.get(0).getMenuNode().getName());
     assertTrue(nodes.get(1).isSelected());
     assertEquals("bb", nodes.get(1).getMenuNode().getName());
     assertTrue(!nodes.get(2).isSelected());
     assertEquals("b", nodes.get(2).getMenuNode().getName());
     // We are testing if the link path is empty here, because the node does not
     // have accessible descendants.
     assertEquals("/module/classic-menu/menu.do", nodes.get(2).getLinkPath());
   }
 }
 
