 /**
  * Copyright (C) 2012  Severin Heiniger <severinheiniger@gmail.com>
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 package valable.outline;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.File;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.jface.viewers.TreeNode;
 import org.gnome.vala.Field;
 import org.gnome.vala.Method;
import org.gnome.vala.SymbolAccessibility;
import org.gnome.vala.VoidType;
 import org.junit.Test;
 
 import valable.AbstractTest;
 import valable.ValaPluginConstants;
 import valable.model.LocalFile;
 import valable.model.ValaPackage;
 import valable.model.ValaProject;
 import valable.model.ValaSource;
 
 /**
  * Tests {@link ValaLabelProvider}.
  */
 public class ValaLabelProviderTest extends AbstractTest {
 
 	@Test
 	public void testImageKey() {
 		ValaLabelProvider labelProvider = new ValaLabelProvider();
 		IFile file = new LocalFile(new File("test/simple.vala"));
 		ValaProject project = new ValaProject("project");
 		ValaSource source = new ValaSource(project, file);
 		ValaPackage valaPackage = new ValaPackage("package");
		Field field = new Field("field", new VoidType());
		field.setAccessibility(SymbolAccessibility.INTERNAL);
 
 		assertEquals(ValaPluginConstants.IMG_OBJECT_VALA,
 				labelProvider.getImageKey(source));
 		assertEquals(ValaPluginConstants.IMG_OBJECT_PACKAGE,
 				labelProvider.getImageKey(valaPackage));
 		assertEquals(ValaPluginConstants.IMG_OBJECT_UNKNOWN,
 				labelProvider.getImageKey(new Object()));
 
 		assertEquals(ValaPluginConstants.IMG_OBJECT_FIELD_DEFAULT,
 				labelProvider.getImageKey(field));
 		TreeNode fieldTreeNode = new TreeNode(field);
 		assertEquals(ValaPluginConstants.IMG_OBJECT_FIELD_DEFAULT,
 				labelProvider.getImageKey(fieldTreeNode));
 	}
 
 	@Test
 	public void testText() {
 		ValaLabelProvider labelProvider = new ValaLabelProvider();
		Field field = new Field("field", new VoidType());
 		TreeNode fieldTreeNode = new TreeNode(field);
		String expectedFieldText = "field : void";
 
 		assertEquals(expectedFieldText, labelProvider.getText(field));
 		assertEquals(expectedFieldText, labelProvider.getText(fieldTreeNode));
 
		Method method = new Method("method", new VoidType());
		assertEquals("method() : void", labelProvider.getText(method));
 	}
 
 }
