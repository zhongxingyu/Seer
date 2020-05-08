 /*******************************************************************************
  * Copyright (c) 2007, 2014 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.swt.utils;
 
 import java.net.URI;
 
 import com.kitfox.svg.SVGCache;
 import com.kitfox.svg.SVGDiagram;
 
 import org.osgi.framework.Bundle;
 
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.graphics.Rectangle;
 
 import org.eclipse.riena.core.test.RienaTestCase;
 import org.eclipse.riena.core.test.collect.UITestCase;
 import org.eclipse.riena.core.util.ReflectionUtils;
 import org.eclipse.riena.internal.tests.Activator;
 import org.eclipse.riena.ui.core.resource.IconSize;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 import org.eclipse.riena.ui.swt.lnf.rienadefault.RienaDefaultLnf;
 
 /**
  * Tests the class {@link ImageStore}.
  */
 @UITestCase
 public class ImageStoreTest extends RienaTestCase {
 
 	/**
 	 * Tests the <i>private</i> method {@code getFullName}.
 	 */
 	public void testGetFullName() {
 
 		final ImageStore store = ImageStore.getInstance();
 		final String fullName = ReflectionUtils.invokeHidden(store, "getFullName", "abc", ImageFileExtension.JPG); //$NON-NLS-1$ //$NON-NLS-2$
 		assertEquals("abc.jpg", fullName); //$NON-NLS-1$
 
 	}
 
 	/**
 	 * Tests the method {@code getImage(String)}.
 	 */
 	public void testGetImage() {
 
 		final ImageStore store = ImageStore.getInstance();
 		store.update(new IImagePathExtension[] {});
 		Image image = store.getImage("spirit"); //$NON-NLS-1$
 		assertNull(image);
 
 		final IImagePathExtension extension = new IImagePathExtension() {
 
 			public Bundle getContributingBundle() {
 				return Activator.getDefault().getBundle();
 			}
 
 			public String getPath() {
 				return "icons"; //$NON-NLS-1$
 			}
 
 		};
 		store.update(new IImagePathExtension[] { extension });
 		image = store.getImage("spirit"); //$NON-NLS-1$
 		assertNotNull(image);
 
 	}
 
 	/**
 	 * Tests the method {@code getImage(String,IconSize)}.
 	 */
 	public void testGetImageIconSize() {
 
 		final ImageStore store = ImageStore.getInstance();
 
 		final Point dpi = SwtUtilities.getDpi();
 		final RienaDefaultLnf originalLnf = LnfManager.getLnf();
 
 		try {
 			LnfManager.setLnf(new ScaleLnf());
 
 			// find png
 			Image image = store.getImage("cloud.png", IconSize.A16); //$NON-NLS-1$
 			assertEquals(32, image.getBounds().width);
 
 			image = store.getImage("cloud", IconSize.A16); //$NON-NLS-1$
 			assertEquals(32, image.getBounds().width);
 
 			image = store.getImage("spirit", IconSize.A16); //$NON-NLS-1$
 			assertEquals(16, image.getBounds().width);
 
 			image = store.getImage("testimagea", IconSize.A16); //$NON-NLS-1$
 			assertEquals(16, image.getBounds().width);
 
 			ReflectionUtils.setHidden(SwtUtilities.class, "cachedDpi", new Point(144, 144)); //$NON-NLS-1$
 			image = store.getImage("testimagea", IconSize.A16); //$NON-NLS-1$
 			assertEquals(24, image.getBounds().width);
 
 			ReflectionUtils.setHidden(SwtUtilities.class, "cachedDpi", new Point(200, 200)); //$NON-NLS-1$
 			image = store.getImage("testimagea", IconSize.A16); //$NON-NLS-1$
 			assertEquals(16, image.getBounds().width);
 
			ReflectionUtils.setHidden(SwtUtilities.class, "cachedDpiFactors", new float[] { 0.0f, 0.0f }); //$NON-NLS-1$
 			final Point defaultDpi = SwtUtilities.getDefaultDpi();
 			ReflectionUtils.setHidden(SwtUtilities.class, "cachedDpi", defaultDpi); //$NON-NLS-1$
 
 			// find SVG
 			image = store.getImage("cloud_p_", IconSize.A16); //$NON-NLS-1$
 			assertEquals(16, image.getBounds().width);
 
 			image = store.getImage("cloud_p_", IconSize.B22); //$NON-NLS-1$
 			assertEquals(22, image.getBounds().width);
 
 			image = store.getImage("cloud_d_", IconSize.B22); //$NON-NLS-1$
 			assertNull(image);
 
 			image = store.getImage("cloud_p_a", IconSize.A16); //$NON-NLS-1$
 			assertEquals(16, image.getBounds().width);
 
 			image = store.getImage("cloud_p_b", IconSize.B22); //$NON-NLS-1$
 			assertEquals(22, image.getBounds().width);
 
 			image = store.getImage("cloud_p_c", IconSize.B22); //$NON-NLS-1$
 			assertNull(image);
 
 		} finally {
 			LnfManager.setLnf(originalLnf);
 			ReflectionUtils.setHidden(SwtUtilities.class, "cachedDpi", dpi); //$NON-NLS-1$
 		}
 
 	}
 
 	/**
 	 * Tests the method {@code getImage(String,ImageFileExtension)}.
 	 */
 	public void testGetImageImageFileExtension() {
 
 		final ImageStore store = ImageStore.getInstance();
 
 		final IImagePathExtension extension = new IImagePathExtension() {
 
 			public Bundle getContributingBundle() {
 				return Activator.getDefault().getBundle();
 			}
 
 			public String getPath() {
 				return "icons"; //$NON-NLS-1$
 			}
 
 		};
 		store.update(new IImagePathExtension[] { extension });
 
 		Image image = store.getImage("spirit", ImageFileExtension.PNG); //$NON-NLS-1$
 		assertNotNull(image);
 		image = store.getImage("spirit", ImageFileExtension.GIF); //$NON-NLS-1$
 		assertNull(image);
 		image = store.getImage("spirit", ImageFileExtension.JPG); //$NON-NLS-1$
 		assertNull(image);
 
 		image = store.getImage("eclipse", ImageFileExtension.PNG); //$NON-NLS-1$
 		assertNull(image);
 		image = store.getImage("eclipse", ImageFileExtension.GIF); //$NON-NLS-1$
 		assertNotNull(image);
 		image = store.getImage("eclipse", ImageFileExtension.JPG); //$NON-NLS-1$
 		assertNull(image);
 
 	}
 
 	/**
 	 * Tests the method {@code addImageScaleSuffix(String)}.
 	 * 
 	 * @throws Exception
 	 *             handled by JUnit
 	 */
 	public void testAddImageScaleSuffix() throws Exception {
 
 		final ImageStore store = ImageStore.getInstance();
 
 		final IImagePathExtension extension = new IImagePathExtension() {
 
 			public Bundle getContributingBundle() {
 				return Activator.getDefault().getBundle();
 			}
 
 			public String getPath() {
 				return "icons"; //$NON-NLS-1$
 			}
 
 		};
 		store.update(new IImagePathExtension[] { extension });
 
 		final RienaDefaultLnf originalLnf = LnfManager.getLnf();
 		try {
 			LnfManager.setLnf(new MyLnf());
 
 			String name = store.addImageScaleSuffix("imagebutton", ImageFileExtension.PNG); //$NON-NLS-1$
 			assertEquals("imagebutton_p_", name); //$NON-NLS-1$
 
 			name = store.addImageScaleSuffix("dontexits", ImageFileExtension.PNG); //$NON-NLS-1$
 			assertEquals("dontexits", name); //$NON-NLS-1$
 
 		} finally {
 			LnfManager.setLnf(originalLnf);
 		}
 
 	}
 
 	/**
 	 * Tests the method {@code getFullScaledName}.
 	 * 
 	 * @throws Exception
 	 *             handled by JUnit
 	 */
 	public void testGetFullScaledName() throws Exception {
 
 		final ImageStore store = ImageStore.getInstance();
 
 		final IImagePathExtension extension = new IImagePathExtension() {
 
 			public Bundle getContributingBundle() {
 				return Activator.getDefault().getBundle();
 			}
 
 			public String getPath() {
 				return "icons"; //$NON-NLS-1$
 			}
 
 		};
 		store.update(new IImagePathExtension[] { extension });
 
 		final RienaDefaultLnf originalLnf = LnfManager.getLnf();
 		try {
 			LnfManager.setLnf(new MyLnf());
 
 			final Point dpi = SwtUtilities.getDpi();
 
 			String name = ReflectionUtils.invokeHidden(store, "getFullScaledName", "abc.jpg", ImageFileExtension.JPG, dpi); //$NON-NLS-1$ //$NON-NLS-2$
 			assertNull(name);
 
 			name = ReflectionUtils.invokeHidden(store, "getFullScaledName", "", ImageFileExtension.PNG, dpi); //$NON-NLS-1$ //$NON-NLS-2$
 			assertNull(name);
 
 			name = ReflectionUtils.invokeHidden(store, "getFullScaledName", "imagebutton", null, dpi); //$NON-NLS-1$ //$NON-NLS-2$
 			assertNull(name);
 
 			name = ReflectionUtils.invokeHidden(store, "getFullScaledName", "imagebutton", ImageFileExtension.PNG, dpi); //$NON-NLS-1$ //$NON-NLS-2$
 			assertEquals("imagebutton_p_.png", name); //$NON-NLS-1$
 
 			name = ReflectionUtils.invokeHidden(store, "getFullScaledName", "dontexits", ImageFileExtension.PNG, dpi); //$NON-NLS-1$ //$NON-NLS-2$
 			assertEquals("dontexits.png", name); //$NON-NLS-1$
 
 			name = ReflectionUtils.invokeHidden(store, "getFullScaledName", "imagebutton", ImageFileExtension.JPG, dpi); //$NON-NLS-1$ //$NON-NLS-2$
 			assertEquals("imagebutton.jpg", name); //$NON-NLS-1$
 
 			name = ReflectionUtils.invokeHidden(store, "getFullScaledName", "dontexits", ImageFileExtension.JPG, dpi); //$NON-NLS-1$ //$NON-NLS-2$
 			assertEquals("dontexits.jpg", name); //$NON-NLS-1$
 
 		} finally {
 			LnfManager.setLnf(originalLnf);
 		}
 
 	}
 
 	/**
 	 * Tests the <i>private</i> method {@code getFullSvgName}.
 	 * 
 	 * @throws Exception
 	 *             handled by JUnit
 	 */
 	public void testGetFullSvgName() throws Exception {
 
 		final ImageStore store = ImageStore.getInstance();
 
 		String name = null;
 		IconSize size = null;
 		String fullName = ReflectionUtils.invokeHidden(store, "getFullSvgName", name, size); //$NON-NLS-1$
 		assertNull(fullName);
 
 		name = ""; //$NON-NLS-1$
 		fullName = ReflectionUtils.invokeHidden(store, "getFullSvgName", name, size); //$NON-NLS-1$
 		assertNull(fullName);
 
 		name = "hello.png"; //$NON-NLS-1$
 		fullName = ReflectionUtils.invokeHidden(store, "getFullSvgName", name, size); //$NON-NLS-1$
 		assertNull(fullName);
 
 		name = "hello.svg"; //$NON-NLS-1$
 		fullName = ReflectionUtils.invokeHidden(store, "getFullSvgName", name, size); //$NON-NLS-1$
 		assertEquals(name, fullName);
 
 		name = "hello"; //$NON-NLS-1$
 		fullName = ReflectionUtils.invokeHidden(store, "getFullSvgName", name, size); //$NON-NLS-1$
 		assertEquals("hello.svg", fullName); //$NON-NLS-1$
 
 		name = "hello"; //$NON-NLS-1$
 		size = IconSize.NONE;
 		fullName = ReflectionUtils.invokeHidden(store, "getFullSvgName", name, size); //$NON-NLS-1$
 		assertEquals("hello.svg", fullName); //$NON-NLS-1$
 
 		name = "hello"; //$NON-NLS-1$
 		size = IconSize.A16;
 		fullName = ReflectionUtils.invokeHidden(store, "getFullSvgName", name, size); //$NON-NLS-1$
 		assertEquals("hello.svg", fullName); //$NON-NLS-1$
 
 		name = "icona"; //$NON-NLS-1$
 		fullName = ReflectionUtils.invokeHidden(store, "getFullSvgName", name, size); //$NON-NLS-1$
 		assertEquals("icon.svg", fullName); //$NON-NLS-1$
 
 		name = "icona"; //$NON-NLS-1$
 		size = IconSize.B22;
 		fullName = ReflectionUtils.invokeHidden(store, "getFullSvgName", name, size); //$NON-NLS-1$
 		assertEquals("icona.svg", fullName); //$NON-NLS-1$
 
 		name = "iconaa"; //$NON-NLS-1$
 		size = IconSize.A16;
 		fullName = ReflectionUtils.invokeHidden(store, "getFullSvgName", name, size); //$NON-NLS-1$
 		assertEquals("icona.svg", fullName); //$NON-NLS-1$
 
 		name = "imageb"; //$NON-NLS-1$
 		size = IconSize.B22;
 		fullName = ReflectionUtils.invokeHidden(store, "getFullSvgName", name, size); //$NON-NLS-1$
 		assertEquals("image.svg", fullName); //$NON-NLS-1$
 
 	}
 
 	/**
 	 * Tests the <i>private</i> method {@code getFullSvgName}.
 	 * 
 	 * @throws Exception
 	 *             handled by JUnit
 	 */
 	public void testCreateSvgImage() throws Exception {
 
 		final ImageStore store = ImageStore.getInstance();
 
 		String name = null;
 		IconSize size = null;
 		Image image = ReflectionUtils.invokeHidden(store, "createSvgImage", name, size); //$NON-NLS-1$
 		assertNull(image);
 
 		name = "blaBla"; //$NON-NLS-1$
 		size = IconSize.A16;
 		image = ReflectionUtils.invokeHidden(store, "createSvgImage", name, size); //$NON-NLS-1$
 		assertNull(image);
 
 		name = "cloud"; //$NON-NLS-1$
 		size = IconSize.A16;
 		name = ReflectionUtils.invokeHidden(store, "getFullSvgName", name, size); //$NON-NLS-1$
 		image = ReflectionUtils.invokeHidden(store, "createSvgImage", name, size); //$NON-NLS-1$
 		assertNotNull(image);
 		int w = SwtUtilities.convertXToDpi(size.getWidth());
 		int h = SwtUtilities.convertYToDpi(size.getHeight());
 		assertEquals(w, image.getImageData().width);
 		assertEquals(h, image.getImageData().height);
 		if (!SwtUtilities.isDpiScalingEnabled()) {
 			final int pixelValue = image.getImageData().getPixel(0, 6);
 			final RGB rgb = image.getImageData().palette.getRGB(pixelValue);
 			assertEquals(new RGB(30, 30, 27), rgb);
 		}
 
 		name = "cloud"; //$NON-NLS-1$
 		size = IconSize.C32;
 		name = ReflectionUtils.invokeHidden(store, "getFullSvgName", name, size); //$NON-NLS-1$
 		image = ReflectionUtils.invokeHidden(store, "createSvgImage", name, size); //$NON-NLS-1$
 		assertNotNull(image);
 		w = SwtUtilities.convertXToDpi(size.getWidth());
 		h = SwtUtilities.convertYToDpi(size.getHeight());
 		assertEquals(w, image.getImageData().width);
 		assertEquals(h, image.getImageData().height);
 		if (!SwtUtilities.isDpiScalingEnabled()) {
 			final int pixelValue = image.getImageData().getPixel(0, 15);
 			final RGB rgb = image.getImageData().palette.getRGB(pixelValue);
 			assertEquals(new RGB(29, 29, 26), rgb);
 		}
 
 	}
 
 	/**
 	 * Tests the <i>private</i> method {@code getImageBounds}.
 	 * 
 	 * @throws Exception
 	 *             handled by JUnit
 	 */
 	public void testGetImageBounds() throws Exception {
 
 		final ImageStore store = ImageStore.getInstance();
 
		ReflectionUtils.setHidden(SwtUtilities.class, "cachedDpiFactors", new float[] { 0.0f, 0.0f }); //$NON-NLS-1$
 		final Point defaultDpi = SwtUtilities.getDefaultDpi();
 		ReflectionUtils.setHidden(SwtUtilities.class, "cachedDpi", defaultDpi); //$NON-NLS-1$
 
 		SVGDiagram svgDiagram = null;
 		IconSize size = null;
 		Rectangle bounds = ReflectionUtils.invokeHidden(store, "getImageBounds", svgDiagram, size); //$NON-NLS-1$
 		assertEquals(new Rectangle(0, 0, 0, 0), bounds);
 
 		size = IconSize.NONE;
 		bounds = ReflectionUtils.invokeHidden(store, "getImageBounds", svgDiagram, size); //$NON-NLS-1$
 		assertEquals(new Rectangle(0, 0, 0, 0), bounds);
 
 		size = IconSize.A16;
 		bounds = ReflectionUtils.invokeHidden(store, "getImageBounds", svgDiagram, size); //$NON-NLS-1$
 		int wh = SwtUtilities.convertPixelToDpi(16);
 		assertEquals(new Rectangle(0, 0, wh, wh), bounds);
 
 		size = IconSize.F128;
 		bounds = ReflectionUtils.invokeHidden(store, "getImageBounds", svgDiagram, size); //$NON-NLS-1$
 		wh = SwtUtilities.convertPixelToDpi(128);
 		assertEquals(new Rectangle(0, 0, wh, wh), bounds);
 
 		final URI imageUri = store.getImageUri("cloud", ImageFileExtension.SVG); //$NON-NLS-1$
 		svgDiagram = SVGCache.getSVGUniverse().getDiagram(imageUri);
 		size = IconSize.NONE;
 		bounds = ReflectionUtils.invokeHidden(store, "getImageBounds", svgDiagram, size); //$NON-NLS-1$
 		wh = SwtUtilities.convertPixelToDpi(512);
 		assertEquals(new Rectangle(0, 0, wh, wh), bounds);
 
 		size = IconSize.D48;
 		bounds = ReflectionUtils.invokeHidden(store, "getImageBounds", svgDiagram, size); //$NON-NLS-1$
 		wh = SwtUtilities.convertPixelToDpi(48);
 		assertEquals(new Rectangle(0, 0, wh, wh), bounds);
 
 		SVGCache.getSVGUniverse().clear();
 
 	}
 
 	/**
 	 * Tests the method {@code getImageUri}.
 	 * 
 	 * @throws Exception
 	 */
 	public void testGetImageUri() throws Exception {
 
 		final ImageStore store = ImageStore.getInstance();
 		final Point dpi = SwtUtilities.getDpi();
 		final RienaDefaultLnf originalLnf = LnfManager.getLnf();
 
 		try {
 			LnfManager.setLnf(new ScaleLnf());
 
 			// image file does not exits
 			URI imageUri = store.getImageUri("abc", ImageFileExtension.PNG); //$NON-NLS-1$
 			assertNull(imageUri);
 
 			// existing gif image file
 			imageUri = store.getImageUri("closed_16", ImageFileExtension.GIF); //$NON-NLS-1$
 			assertNotNull(imageUri);
 			assertEquals("/icons/closed_16.gif", imageUri.getPath()); //$NON-NLS-1$
 
 			imageUri = store.getImageUri("closed_16.gif", ImageFileExtension.PNG); //$NON-NLS-1$
 			assertNotNull(imageUri);
 			assertEquals("/icons/closed_16.gif", imageUri.getPath()); //$NON-NLS-1$
 
 			imageUri = store.getImageUri("closed_16.gif", null); //$NON-NLS-1$
 			assertNotNull(imageUri);
 			assertEquals("/icons/closed_16.gif", imageUri.getPath()); //$NON-NLS-1$
 
 			// image file does not exists
 			imageUri = store.getImageUri("closed_16", ImageFileExtension.JPG); //$NON-NLS-1$
 			assertNull(imageUri);
 
 			// existing png image file
 			imageUri = store.getImageUri("spirit", ImageFileExtension.PNG); //$NON-NLS-1$
 			assertNotNull(imageUri);
 			assertEquals("/icons/spirit.png", imageUri.getPath()); //$NON-NLS-1$
 
 			imageUri = store.getImageUri("spirit.png", ImageFileExtension.JPG); //$NON-NLS-1$
 			assertNotNull(imageUri);
 			assertEquals("/icons/spirit.png", imageUri.getPath()); //$NON-NLS-1$
 
 			imageUri = store.getImageUri("spirit.png", null); //$NON-NLS-1$
 			assertNotNull(imageUri);
 			assertEquals("/icons/spirit.png", imageUri.getPath()); //$NON-NLS-1$
 
 			// image file does not exists
 			imageUri = store.getImageUri("spirit", ImageFileExtension.GIF); //$NON-NLS-1$
 			assertNull(imageUri);
 
 			ReflectionUtils.setHidden(SwtUtilities.class, "cachedDpi", new Point(144, 144)); //$NON-NLS-1$
 
 			// image file with scaling exists
 			ReflectionUtils.setHidden(SwtUtilities.class, "cachedDpi", new Point(96, 96)); //$NON-NLS-1$
 			imageUri = store.getImageUri("testimagea", ImageFileExtension.PNG); //$NON-NLS-1$
 			assertEquals("/icons/testimagea00.png", imageUri.getPath()); //$NON-NLS-1$
 
 			ReflectionUtils.setHidden(SwtUtilities.class, "cachedDpi", new Point(144, 144)); //$NON-NLS-1$
 			imageUri = store.getImageUri("testimagea", ImageFileExtension.PNG); //$NON-NLS-1$
 			assertEquals("/icons/testimagea03.png", imageUri.getPath()); //$NON-NLS-1$
 
 			// image file with scaling does not exist, use default scaling
 			ReflectionUtils.setHidden(SwtUtilities.class, "cachedDpi", new Point(200, 200)); //$NON-NLS-1$
 			imageUri = store.getImageUri("testimagea", ImageFileExtension.PNG); //$NON-NLS-1$
 			assertEquals("/icons/testimagea00.png", imageUri.getPath()); //$NON-NLS-1$
 
 			// image file does not exists
 			ReflectionUtils.setHidden(SwtUtilities.class, "cachedDpi", new Point(96, 96)); //$NON-NLS-1$
 			imageUri = store.getImageUri("testimagea", ImageFileExtension.GIF); //$NON-NLS-1$
 			assertNull(imageUri);
 
 			// use given image file name
 			ReflectionUtils.setHidden(SwtUtilities.class, "cachedDpi", new Point(96, 96)); //$NON-NLS-1$
 			imageUri = store.getImageUri("testimagea03.png", null); //$NON-NLS-1$
 			assertEquals("/icons/testimagea03.png", imageUri.getPath()); //$NON-NLS-1$
 
 		} finally {
 			LnfManager.setLnf(originalLnf);
 			ReflectionUtils.setHidden(SwtUtilities.class, "cachedDpi", dpi); //$NON-NLS-1$
 		}
 
 	}
 
 	private static class MyLnf extends RienaDefaultLnf {
 		@Override
 		public String getIconScaleSuffix(final Point dpi) {
 
 			if (dpi == null) {
 				return "_h_"; //$NON-NLS-1$
 			}
 			if (dpi.x < 96) {
 				return "_h_"; //$NON-NLS-1$
 			}
 			return "_p_"; //$NON-NLS-1$
 
 		}
 	}
 
 	private static class ScaleLnf extends RienaDefaultLnf {
 		@Override
 		public String getIconScaleSuffix(final Point dpi) {
 			switch (dpi.x) {
 			case 96:
 				return "00"; //$NON-NLS-1$
 			case 120:
 				return "01"; //$NON-NLS-1$
 			case 128:
 				return "02"; //$NON-NLS-1$
 			case 144:
 				return "03"; //$NON-NLS-1$
 			default:
 				return null;
 			}
 		}
 	}
 
 }
