 package br.com.logica;
 
 import java.io.File;
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.example.tagedition.R;
 
 public class ImagemAdapter extends BaseAdapter{
 
 	private List<File> files;
 	private LayoutInflater mInflater;
 	
 	public ImagemAdapter(Context context, List<File> files){
 		mInflater = LayoutInflater.from(context);
 		this.files = files;
 	}
 	
 	@Override
 	public int getCount() {
 		return files.size();
 	}
 
 	@Override
 	public File getItem(int arg0) {
 		return files.get(arg0);
 	}
 
 	@Override
 	public long getItemId(int arg0) {
 		return arg0;
 	}
 
 	@Override
 	public View getView(int posicao, View view, ViewGroup arg2) {
 		view = mInflater.inflate(R.layout.item_grid, null);
 		
 		if (files.get(posicao).isDirectory()){
			TextView nome = (TextView)view.findViewById(R.id.nomeArquivo);
			nome.setText(files.get(posicao).getName());
 		}else{
 			Bitmap myBitmap = BitmapFactory.decodeFile(files.get(posicao).getAbsolutePath());
 			ImageView myImage = (ImageView) view.findViewById(R.id.imagem);
 			myImage.setImageBitmap(Bitmap.createScaledBitmap(myBitmap, 70, 70, false));
 		}
 		
 		return view;
 	}
 
 }
