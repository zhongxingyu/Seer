 package com.xtremelabs.droidsugar.util;
 
 import android.test.mock.MockContext;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import com.google.android.maps.MapView;
 import com.xtremelabs.droidsugar.DroidSugarAndroidTestRunner;
 import com.xtremelabs.droidsugar.R;
 import com.xtremelabs.droidsugar.fakes.FakeTextView;
 import com.xtremelabs.droidsugar.fakes.FakeView;
 import com.xtremelabs.droidsugar.fakes.FakeViewGroup;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import java.io.File;
 
 import static android.test.MoreAsserts.assertNotEqual;
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertSame;
 import static org.junit.Assert.assertThat;
 
 @RunWith(DroidSugarAndroidTestRunner.class)
 public class ViewLoaderTest {
     private ViewLoader viewLoader;
     private MockContext context;
 
     @Before
     public void setUp() throws Exception {
         DroidSugarAndroidTestRunner.addProxy(View.class, FakeView.class);
         DroidSugarAndroidTestRunner.addProxy(ViewGroup.class, FakeViewGroup.class);
         DroidSugarAndroidTestRunner.addProxy(TextView.class, FakeTextView.class);
 
         ResourceExtractor resourceExtractor = new ResourceExtractor();
         resourceExtractor.addRClass(R.class);
         StringResourceLoader stringResourceLoader = new StringResourceLoader(resourceExtractor);
         stringResourceLoader.loadDirs(new File("test/res/values"));
         viewLoader = new ViewLoader(resourceExtractor, stringResourceLoader);
         viewLoader.loadDirs(new File("test/res/layout"));
 
         context = new MockContext();
     }
 
     @Test
     public void testCreatesCorrectClasses() throws Exception {
         ViewGroup view = (ViewGroup) viewLoader.inflateView(context, "layout/media");
         TestUtil.assertInstanceOf(LinearLayout.class, view);
 
         assertSame(context, view.getContext());
     }
 
     @Test
     public void testAddsChildren() throws Exception {
         ViewGroup view = (ViewGroup) viewLoader.inflateView(context, "layout/media");
         assertNotEqual(0, view.getChildCount());
 
         assertSame(context, view.getChildAt(0).getContext());
     }
 
     @Test
     public void testFindsChildrenById() throws Exception {
         ViewGroup mediaView = (ViewGroup) viewLoader.inflateView(context, "layout/media");
         TestUtil.assertInstanceOf(TextView.class, mediaView.findViewById(R.id.title));
 
         ViewGroup mainView = (ViewGroup) viewLoader.inflateView(context, "layout/main");
         TestUtil.assertInstanceOf(View.class, mainView.findViewById(R.id.title));
     }
 
     @Test
     public void testInclude() throws Exception {
         ViewGroup mediaView = (ViewGroup) viewLoader.inflateView(context, "layout/media");
         TestUtil.assertInstanceOf(TextView.class, mediaView.findViewById(R.id.snippet_text));
     }
 
     @Test
     public void testIncludeShouldRetainAttributes() throws Exception {
         ViewGroup mediaView = (ViewGroup) viewLoader.inflateView(context, "layout/media");
         assertThat(mediaView.findViewById(R.id.snippet_text).getVisibility(), is(View.GONE));
     }
 
     @Test
     public void testMerge() throws Exception {
         ViewGroup mediaView = (ViewGroup) viewLoader.inflateView(context, "layout/outer");
         TestUtil.assertInstanceOf(TextView.class, mediaView.findViewById(R.id.inner_text));
     }
 
     @Test
     public void testViewGroupsLooksAtItsOwnId() throws Exception {
         TextView mediaView = (TextView) viewLoader.inflateView(context, "layout/snippet");
         assertSame(mediaView, mediaView.findViewById(R.id.snippet_text));
     }
 
     @Test
     public void testViewVisibilityIsSet() throws Exception {
         View mediaView = viewLoader.inflateView(context, "layout/media");
         assertThat(mediaView.findViewById(R.id.title).getVisibility(), equalTo(View.VISIBLE));
         assertThat(mediaView.findViewById(R.id.subtitle).getVisibility(), equalTo(View.GONE));
     }
 
     @Test
     public void testTextViewTextIsSet() throws Exception {
         View mediaView = viewLoader.inflateView(context, "layout/main");
        assertThat(((TextView) mediaView.findViewById(R.id.title)).getText().toString(), equalTo("Hola!"));
         assertThat(((TextView) mediaView.findViewById(R.id.subtitle)).getText().toString(), equalTo("Hello"));
     }
 
     @Test
     public void testMapView() throws Exception {
         RelativeLayout mainView = (RelativeLayout) viewLoader.inflateView(context, "layout/mapview");
         TestUtil.assertInstanceOf(MapView.class,  mainView.findViewById(R.id.map_view));
     }
 }
