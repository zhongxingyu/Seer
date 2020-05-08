 package ca.cammisuli.empublite;
 
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentStatePagerAdapter;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 
 /**
  * Created by jcammisuli on 06/09/13.
  */
 public class ContentsAdapter extends FragmentStatePagerAdapter {
 
     private BookContents contents = null;
 
     public ContentsAdapter(SherlockFragmentActivity ctxt, BookContents contents) {
         super(ctxt.getSupportFragmentManager());
 
         this.contents = contents;
     }
 
     @Override
     public Fragment getItem(int position) {
         String path = contents.getChapterFile(position);
 
        return (SimpleContentFragment.newInstance("file:///android_assets/book/"+path));
     }
 
     @Override
     public int getCount() {
         return(contents.getChapterCount());
     }
 }
