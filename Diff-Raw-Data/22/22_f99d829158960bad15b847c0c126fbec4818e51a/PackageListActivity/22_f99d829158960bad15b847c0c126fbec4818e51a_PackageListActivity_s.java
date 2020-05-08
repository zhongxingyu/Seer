 package uk.ac.cam.db538.dexter.android;
 
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.Window;
 
 import org.apache.commons.io.IOUtils;
 import org.jf.dexlib.DexFile;
 import org.jf.dexlib.DexFileFromMemory;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.util.jar.Attributes;
 import java.util.jar.JarEntry;
 import java.util.jar.JarOutputStream;
 import java.util.jar.Manifest;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipOutputStream;
 
 import dalvik.system.DexClassLoader;
import uk.ac.cam.db538.dexter.dex.AuxiliaryDex;
 import uk.ac.cam.db538.dexter.dex.Dex;
 import uk.ac.cam.db538.dexter.dex.type.ClassRenamer;
 import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;
 import uk.ac.cam.db538.dexter.utils.Pair;
 
 
 /**
  * An activity representing a list of Packages. This activity
  * has different presentations for handset and tablet-size devices. On
  * handsets, the activity presents a list of items, which when touched,
  * lead to a {@link PackageDetailActivity} representing
  * item details. On tablets, the activity presents the list of items and
  * item details side-by-side using two vertical panes.
  * <p>
  * The activity makes heavy use of fragments. The list of items is a
  * {@link PackageListFragment} and the item details
  * (if present) is a {@link PackageDetailFragment}.
  * <p>
  * This activity also implements the required
  * {@link PackageListFragment.Callbacks} interface
  * to listen for item selections.
  */
 public class PackageListActivity extends FragmentActivity implements PackageListFragment.Callbacks {
 
     /**
      * Whether or not the activity is in two-pane mode, i.e. running on a tablet
      * device.
      */
     private boolean mTwoPane;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_package_list);
 
         if (findViewById(R.id.package_detail_container) != null) {
             // The detail container view will be present only in the
             // large-screen layouts (res/values-large and
             // res/values-sw600dp). If this view is present, then the
             // activity should be in two-pane mode.
             mTwoPane = true;
 
             // In two-pane mode, list items should be given the
             // 'activated' state when touched.
             ((PackageListFragment) getSupportFragmentManager()
                     .findFragmentById(R.id.package_list))
                     .setActivateOnItemClick(true);
         }
     }
 
     /**
      * Callback method from {@link PackageListFragment.Callbacks}
      * indicating that the item with the given ID was selected.
      */
     @Override
     public void onItemSelected(Package pkg) {
         if (mTwoPane) {
             // In two-pane mode, show the detail view in this activity by
             // adding or replacing the detail fragment using a fragment transaction.
             PackageDetailFragment fragment = new PackageDetailFragment();
             fragment.setArgumentsFromPackage(pkg);
             getSupportFragmentManager().beginTransaction()
                     .replace(R.id.package_detail_container, fragment)
                     .commit();
 
         } else {
             // In single-pane mode, simply start the detail activity for the selected item ID.
             Intent detailIntent = new Intent(this, PackageDetailActivity.class);
             PackageFragment.createPackageArgs(detailIntent, pkg);
             startActivity(detailIntent);
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inf = getMenuInflater();
         inf.inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
         if (item.getItemId() == R.id.action_runTests)
             startActivity(new Intent(this, UnitTestActivity.class));
         return true;
     }
 }
