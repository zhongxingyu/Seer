 package nz.co.searchwellington.model;
 
 import java.util.HashSet;
 
import junit.framework.TestCase;
 
public class TagTest extends TestCase {
     
     public void testIsParentOfTag() throws Exception {          
         Tag childTag = new Tag(1, "child", "Child", null, new HashSet<Tag>(), 0, false);        
         Tag grandChildTag = new Tag(2, "grandchild", "Grand Child", null, new HashSet<Tag>(), 0, false);
         
         childTag.addChild(grandChildTag);
         grandChildTag.setParent(childTag);
         
         assertTrue(childTag.isParentOf(grandChildTag));
         assertFalse(grandChildTag.isParentOf(childTag));
                 
         Tag parent = new Tag(3, "parent", "Parent", null, new HashSet<Tag>(), 0, false);
         parent.addChild(childTag);
         childTag.setParent(parent);
         
         assertTrue(parent.isParentOf(childTag));
         assertTrue(parent.isParentOf(grandChildTag));
     }
     
 }
