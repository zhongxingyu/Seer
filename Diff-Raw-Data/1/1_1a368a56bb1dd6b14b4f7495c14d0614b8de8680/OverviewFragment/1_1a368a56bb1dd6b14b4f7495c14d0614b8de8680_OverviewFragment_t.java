 package home.example.opdsbrowser.view;
 
 import home.example.opdsbrowser.R;
 import home.example.opdsbrowser.data.Book;
 import home.example.opdsbrowser.data.OpdsContext;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class OverviewFragment extends Fragment {
 
 	@Override
 	public void onStart() {
		super.onStart();
 		Book b = OpdsContext.getContext().getThisBook();
 		ImageView cover = (ImageView) getView().findViewById(R.id.info_cover);
 		TextView author = (TextView) getView().findViewById(R.id.info_author);
 		TextView title = (TextView) getView().findViewById(R.id.info_title);
 		cover.setImageBitmap(OpdsContext.getContext().getImage());
 		author.setText(b.getAuthor());
 		title.setText(b.getTitle());
 		CoverAsynkTask imageTask = new CoverAsynkTask(cover);
     	imageTask.execute(b.getCover());
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.overview_screen, container, false);
 	}
 	
 	
 
 }
