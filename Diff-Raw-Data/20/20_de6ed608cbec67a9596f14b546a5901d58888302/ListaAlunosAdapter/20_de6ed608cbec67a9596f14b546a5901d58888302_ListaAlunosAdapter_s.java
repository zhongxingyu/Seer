 package br.com.caelum.alunos.adapter;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 import br.com.caelum.alunos.R;
 import br.com.caelum.alunos.modelo.Aluno;
 
 public class ListaAlunosAdapter extends BaseAdapter {
 	private final List<Aluno> alunos;
 	private final Activity activity;
 	
 	public ListaAlunosAdapter(Activity activity, List<Aluno> alunos){
 		this.activity = activity;
 		this.alunos = alunos;
 	}
 
 	@Override
 	public int getCount() {
 		// TODO Auto-generated method stub
 		return alunos.size();
 	}
 
 	@Override
 	public Object getItem(int posicao) {
 		// TODO Auto-generated method stub
 		return alunos.get(posicao);
 	}
 
 	@Override
 	public long getItemId(int posicao) {
 		// TODO Auto-generated method stub
 		return alunos.get(posicao).getId();
 	}
 
 	@Override
 	public View getView(int posicao, View convertView, ViewGroup parent) {
 		View view = activity.getLayoutInflater().inflate(R.layout.item, null);
 		
 		Aluno aluno = alunos.get(posicao);
 		if(posicao % 2 == 0){
 			view.setBackgroundColor(activity.getResources().getColor(R.color.linha_par));
 		}
 		
 		TextView nome = (TextView) view.findViewById(R.id.nome);
 		nome.setText(aluno.toString());
 		
 		Bitmap bm;
 		
 		if(aluno.getFoto() != null){
 			bm = BitmapFactory.decodeFile(aluno.getFoto());
 		} else {
 			bm = BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_no_image);
 		}
 		
 		bm = Bitmap.createScaledBitmap(bm, 100, 100, true);
 		
 		ImageView foto = (ImageView) view.findViewById(R.id.foto);
 		foto.setImageBitmap(bm);
 		
 		return view;
 		
 	}
 
 }
