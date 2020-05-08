 /**
  * Copyright (C) 2012  Severin Heiniger <severinheiniger@gmail.com>
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 package valable.outline;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jface.viewers.TreeNode;
 import org.junit.Assert;
 import org.junit.Test;
 
 import valable.model.LocalFile;
 import valable.model.ValaProject;
 import valable.model.ValaSource;
 import valable.model.ValaType;
 
 /**
  * Tests {@link ValaContentProvider}.
  */
 public class ValaContentProviderTest {
 
 	@Test
 	public void testParse() throws CoreException, IOException {
 		IFile file = new LocalFile(new File("test/simple.vala"));
 		ValaSource source = new ValaSource(new ValaProject("Test"), file);
 		source.parse();
 
 		ValaType sampleType = source.getTypes().get("Simple");
 		ValaType fooType = source.getTypes().get("Foo");
 		TreeNode sampleTreeNode = new TreeNode(sampleType);
 		sampleTreeNode.setChildren(new TreeNode[] {
 				new TreeNode(sampleType.getField("age")),
 				new TreeNode(sampleType.getField("name")),
 				new TreeNode(sampleType.getField("count")),
 				new TreeNode(sampleType.getMethod("main")),
 				new TreeNode(sampleType.getMethod("doThing")),
 				new TreeNode(sampleType.getMethod("getParent")) });
 		TreeNode fooTreeNode = new TreeNode(fooType);
 		fooTreeNode.setChildren(new TreeNode[] {
 				new TreeNode(fooType.getMethod("getParent")),
 				new TreeNode(fooType.getMethod("removeParent")) });
 
		TreeNode[] expectedTreeNodes = { sampleTreeNode, fooTreeNode };
 
 		ValaContentProvider contentProvider = new ValaContentProvider();
 		TreeNode[] treeNodes = contentProvider.getElements(source);
 
 		Assert.assertArrayEquals(expectedTreeNodes, treeNodes);
 	}
 
 }
