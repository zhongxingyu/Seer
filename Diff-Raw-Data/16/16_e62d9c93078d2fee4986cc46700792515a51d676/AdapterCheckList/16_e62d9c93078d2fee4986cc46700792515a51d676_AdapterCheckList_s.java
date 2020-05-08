 package br.com.logica;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import br.com.activity.MainActivity;
 import br.com.activity.SetValueTag;
 
 import com.example.tagedition.R;
 
 import android.content.Context;
 import android.content.Intent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.CheckBox;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class AdapterCheckList extends BaseAdapter{
 	
 	private List<File> files;
 	private LayoutInflater mInflater;
 	private List<File> selecionados;
 	
 	public AdapterCheckList(Context context, List<File> files) {
 		mInflater = LayoutInflater.from(context);
 		this.files = files;
 		this.selecionados = new ArrayList<File>();
 	}
 	
 	@Override
 	public int getCount() {
 		return files.size();
 	}
 
 	@Override
 	public Object getItem(int arg0) {
 		return files.get(arg0);
 	}
 
 	@Override
 	public long getItemId(int arg0) {
 		return arg0;
 	}
 
 	@Override
 	public View getView(final int posicao, View view, ViewGroup arg2) {
 		view = mInflater.inflate(R.layout.item_check_list, null); 
 		
 		ImageView image = (ImageView)view.findViewById(R.id.tipoArquivo);
 		if (this.files.get(posicao).isDirectory()){
 			image.setImageResource(R.drawable.folder_blue_music);
 		}else{
 			image.setImageResource(R.drawable.music_icon);
 		}
 		
 		
 		TextView nome = (TextView)view.findViewById(R.id.nomeArquivo);
 		nome.setText(files.get(posicao).getName());
 		
 		final CheckBox check = (CheckBox)view.findViewById(R.id.checkBox);
 		
 		check.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				if (check.isChecked()){
 					System.out.println("entrou");
 					check.setButtonDrawable(R.drawable.com_facebook_button_check_on);
					selecionados.add(files.get(posicao));
				}else{
					System.out.println("entrou2");				
 					check.setButtonDrawable(R.drawable.com_facebook_button_check_off);
 					selecionados.remove(files.get(posicao));
 				}
 			}
 		});
 		
 		return view;
 	}
 	
 	public List<File> getSelecionados(){
 		return selecionados;
 	}
 }
