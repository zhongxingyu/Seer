 package mobi.monaca.framework.test;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import mobi.monaca.framework.MonacaApplication;
 import mobi.monaca.framework.MonacaPageActivity;
 import mobi.monaca.framework.nativeui.UIContext;
 import mobi.monaca.framework.nativeui.component.PageComponent;
 import mobi.monaca.framework.nativeui.exception.DuplicateIDException;
 import mobi.monaca.framework.nativeui.exception.KeyNotValidException;
 import mobi.monaca.framework.util.MyLog;
 
 import org.apache.commons.io.IOUtils;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Intent;
 import android.test.ActivityUnitTestCase;
 
 public class NativeComponentTest extends ActivityUnitTestCase<MonacaPageActivity>{
 	private static final String TAG = NativeComponentTest.class.getSimpleName();
 
 	private UIContext mUIContext;
 	public NativeComponentTest() {
 		super(MonacaPageActivity.class);
 	}
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		MonacaApplication application = new MonacaApplication();
 		setApplication(application);
 		startActivity(new Intent(getInstrumentation().getTargetContext(), MonacaPageActivity.class), null, null);
 		mUIContext = getActivity().getUiContext();
 	}
 	
 	public void testDuplicateID(){
 		try {
 			JSONObject pageJSON = getJSON(R.raw.duplicate_id);
 			new PageComponent(mUIContext, pageJSON);
 		} catch (DuplicateIDException e) {
 			assertEquals("Duplicate id 'id1' in [Toolbar, Button]", e.getMessage());
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail(e.getMessage());
 		} 
 	}
 	
 	public void testPageInvalidKey(){
 		try {
 			JSONObject pageJSON = getJSON(R.raw.page_invalid_key);
 			MyLog.v(TAG, "pageJSON: " + pageJSON);
 			new PageComponent(mUIContext, pageJSON);
 		} catch (KeyNotValidException e) {
 			assertEquals("Page 'can_u_accept_me' is not a valid key. Did you meant one of this? [top, bottom, event, style, menu]"
 					, e.getMessage());
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail(e.getMessage());
 		}
 	}
 	
 	public void testTopInvalidKey(){
 		try {
 			JSONObject pageJSON = getJSON(R.raw.top_invalid_key);
 			MyLog.v(TAG, "pageJSON: " + pageJSON);
 			new PageComponent(mUIContext, pageJSON);
 		} catch (KeyNotValidException e) {
 			assertEquals("Toolbar 'can_u_accept_me' is not a valid key. Did you meant one of this? [container, style, iosStyle, androidStyle, id, left, center, right]"
 					, e.getMessage());
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail(e.getMessage());
 		}
 	}
 	
 	private JSONObject getJSON(int id) throws JSONException, IOException{
 		InputStream jsonStream = getActivity().getResources().openRawResource(id);
 		JSONObject pageJSON = new JSONObject(IOUtils.toString(jsonStream));
 		return pageJSON;
 	}
 
 	
 }
