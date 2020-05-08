 package org.lttng.flightbox.ui;
 
 import java.io.File;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.graphics.ImageLoader;
 import org.junit.Before;
import org.junit.BeforeClass;
 import org.junit.Test;
 import org.lttng.flightbox.state.VersionizedStack;
 
 public class TestSubsystemProxy {
 
 	VersionizedStack<String> stack;
 
 	@Before
 	public void setup() {
 		stack = new VersionizedStack<String>();
 		stack.push("FOO", 1000L);
 		stack.push("BAR", 2000L);
 		stack.push("BAZ", 3000L);
 		stack.pop(4000L);
 		stack.push("BAZ", 5000L);
 		stack.pop(6000L);
 		stack.pop(7000L);
 		stack.pop(8000L);
 	}
 
 	public void saveImage(Image img, String path) {
 		/* for manual visual inspection */
 		ImageLoader imageLoader = new ImageLoader();
 		imageLoader.data = new ImageData[] { img.getImageData() };
 		imageLoader.save(path, SWT.IMAGE_BMP);
 	}
 
 	@Test
 	public void testIntervalRender() {
 		ImageRender r = new IntervalRender();
 		r.setDataObject(stack);
 		Image img = null;
 
 		File zoomDir = new File("./tests/interval-render/zoom/");
 		zoomDir.mkdirs();
 		File translateDir = new File("./tests/interval-render/translate/");
 		translateDir.mkdirs();
 
 		// zooming
 		for (int i = 0; i < 5000; i = i + 100) {
 			img = r.render((long) i, 10000 - (long) i, 400, 23);
			saveImage(img, zoomDir.getPath() + "/" + String.format("%05d", i)
 					+ ".bmp");
 		}
 
 		// translation
 		for (int i = 0; i < 8000; i = i + 100) {
 			img = r.render((long) i, (long) i + 2000, 400, 23);
 			saveImage(img,
					translateDir.getPath() + "/" + String.format("%05d", i)
 							+ ".bmp");
 		}
 
 		if (img != null && !img.isDisposed()) {
 			img.dispose();
 		}
 	}
	
 	@Test
 	public void testIntervalProxy() {
		
 	}
 
 }
