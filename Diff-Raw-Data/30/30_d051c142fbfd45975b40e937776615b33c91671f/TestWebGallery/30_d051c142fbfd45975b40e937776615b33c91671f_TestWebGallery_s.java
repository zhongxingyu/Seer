 package de.raptor2101.GalDroid.Testing.ComponentTest.Activities.TestImplementations;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.Buffer;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Semaphore;
 
 import junit.framework.Assert;
 import junit.framework.Test;
 
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.json.JSONException;
 
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 
 import de.raptor2101.GalDroid.WebGallery.GalleryStream;
 import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryDownloadObject;
 import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObject;
 import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObjectComment;
 import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryProgressListener;
 import de.raptor2101.GalDroid.WebGallery.Interfaces.WebGallery;
 
 public class TestWebGallery implements WebGallery {
	private final int PERMITS = 0; 
 	private List<TestGalleryObject> mTestGalleryObject;
 
	private final Semaphore mSemaphore_GetDisplayObjects;
 	private final Resources mResources;
 	public TestWebGallery(Resources resources) {
		mSemaphore_GetDisplayObjects = new Semaphore(PERMITS, true);
 		mResources = resources;
 	}
 	
	public void waitForGetDisplayObjectsCall() throws InterruptedException {
		mSemaphore_GetDisplayObjects.acquire();
	}
	
 	@Override
 	public GalleryObject getDisplayObject(String path)
 			throws ClientProtocolException, IOException, JSONException {
 		Assert.fail("Call not implemented TestMethod");
 		return null;
 	}
 
 	@Override
 	public List<GalleryObject> getDisplayObjects() {
 		Assert.fail("Call not implemented TestMethod");
 		return null;
 	}
 
 	@Override
 	public List<GalleryObject> getDisplayObjects(
 			GalleryProgressListener progressListener) {
 		Assert.fail("Call not implemented TestMethod");
 		return null;
 	}
 
 	@Override
 	public List<GalleryObject> getDisplayObjects(String path) {
 		Assert.fail("Call not implemented TestMethod");
 		return null;
 	}
 
 	@Override
 	public List<GalleryObject> getDisplayObjects(String path,
 			GalleryProgressListener progressListener) {
 		Assert.fail("Call not implemented TestMethod");
 		return null;
 	}
 
 	@Override
 	public List<GalleryObject> getDisplayObjects(GalleryObject galleryObject) {
 		Assert.fail("Call not implemented TestMethod");
 		return null;
 	}
 
 	@Override
 	public List<GalleryObject> getDisplayObjects(GalleryObject galleryObject,
 			GalleryProgressListener progressListener) {
 		if(galleryObject instanceof TestGalleryObject) {
 			TestGalleryObject testGalleryObject = (TestGalleryObject) galleryObject;
 			
 			Assert.assertTrue("The requestes GalleryObject has no childen",testGalleryObject.hasChildren());
 			
 			List<TestGalleryObject> children = testGalleryObject.getChildren();
 			if(progressListener != null){
 				
 				progressListener.handleProgress(children.size(), children.size());
 			}
 			List<GalleryObject> castedChildren = new ArrayList<GalleryObject>(children.size());
 			for(TestGalleryObject child:children) {
 				castedChildren.add(child);
 			}
			mSemaphore_GetDisplayObjects.release();
 			return castedChildren;
 			
 		} else {
 			Assert.fail("Someone tries to use non-testing GalleryObjects on testing WebGallery");
 			return new ArrayList<GalleryObject>(0);
 		}
 	}
 
 	@Override
 	public List<String> getDisplayObjectTags(GalleryObject galleryObject,
 			GalleryProgressListener progressListener) throws IOException {
 		Assert.fail("Call not implemented TestMethod");
 		return null;
 	}
 
 	@Override
 	public List<GalleryObjectComment> getDisplayObjectComments(
 			GalleryObject galleryObject,
 			GalleryProgressListener progressListener) throws IOException,
 			ClientProtocolException, JSONException {
 		Assert.fail("Call not implemented TestMethod");
 		return null;
 	}
 
 	@Override
 	public void setPreferedDimensions(int height, int width) {
 		// TODO currently not tracked for testing
 	}
 
 	@Override
 	public String getSecurityToken(String user, String password)
 			throws SecurityException {
 		Assert.fail("Call not implemented TestMethod");
 		return null;
 	}
 
 	@Override
 	public GalleryStream getFileStream(GalleryDownloadObject downloadObject)
 			throws IOException, ClientProtocolException {
 		if(downloadObject instanceof TestDownloadObject) {
 			TestDownloadObject testDownloadObject = (TestDownloadObject) downloadObject;
 		Assert.assertEquals("Wrong image size requested",TestDownloadObject.ImageSize.Image,testDownloadObject.getRequestedSize());
 		BitmapDrawable drawable = (BitmapDrawable) mResources.getDrawable(testDownloadObject.getResourceId());
 		Bitmap bitmap = (Bitmap)((BitmapDrawable) drawable).getBitmap();
 	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
 	    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
 	    byte[] byteArray = outputStream.toByteArray();
 	    InputStream inputStream = new ByteArrayInputStream(byteArray);
 		return new GalleryStream(inputStream, byteArray.length );
 		} else {
 			Assert.fail("Someone tries to use non-testing GalleryObjects on testing WebGallery");
 			return null;
 		}
 		
 	}
 
 	@Override
 	public void setSecurityToken(String token) {
 		Assert.fail("Call not implemented TestMethod");
 	}
 
 	@Override
 	public void setHttpClient(HttpClient httpClient) {
 		Assert.fail("Call not implemented TestMethod");
 	}
 
 	public void setTestGalleryObjects(List<TestGalleryObject> testGalleryObjects) {
 		mTestGalleryObject = testGalleryObjects;
 	}
 	
 	public List<TestGalleryObject> getTestGalleryObjects() {
 		return mTestGalleryObject;
 	}
 
 }
