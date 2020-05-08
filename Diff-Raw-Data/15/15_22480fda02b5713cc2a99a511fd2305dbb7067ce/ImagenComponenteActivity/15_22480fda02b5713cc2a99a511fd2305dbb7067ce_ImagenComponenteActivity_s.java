 package com.example.campomagnetico;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class ImagenComponenteActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.imagen_componente);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.imagen_componente, menu);
 		/** Se recogen los valores enviados desde otro activity*/
 		String nombre = getIntent().getStringExtra("nombre");
 		String desc = getIntent().getStringExtra("descripcion");
 		String img = getIntent().getStringExtra("foto");
 		/** Se tratan esos valores para su uso*/
 		int iimg = Integer.parseInt(img);
 		/** Se editan el imageView, el Titulo y la descripción con los valores recibidos*/
 		ImageView imagenMostrada = (ImageView) findViewById(R.id.imagenComponente);
 		imagenMostrada.setImageDrawable(getResources().getDrawable(iimg));
 		setTitle(getTitle() +" " + nombre);
 		TextView descView = (TextView) findViewById(R.id.imagenDescripcion);
 		descView.setText(desc);
 		return true;
 	}
 
 }
