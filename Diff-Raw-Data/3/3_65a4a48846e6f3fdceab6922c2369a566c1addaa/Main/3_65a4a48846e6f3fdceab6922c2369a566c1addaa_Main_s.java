 package per.andy.test;
 
 import android.os.Bundle;
 
 public class Main extends Activity {
 
         @Override
 	protected void onCreate(Bundle savedInstanceState) {    //On Activity Create
 		super.onCreate(savedInstanceState);  //Invoke Superclass (Activity)'s onCreate() method.
 		setContentView(R.layout.activity_main);  //Set view.
 		Button b = (Button) findViewById(R.id.b);  //Basic test button
 		Button m = (Button) findViewById(R.id.m);  //Mid test button
 		Button e = (Button) findViewById(R.id.e);  //Extreme test button
 		Button s = (Button) findViewById(R.id.stop);  //Stop button
 		b.setOnClickListener(new ocl(){   //Set button b listener
 			@Override
 			public void onClick(View v) {
 				new Methods().basic(); //Run test.
 			}
 		});
 		
 		m.setOnClickListener(new ocl(){  //Set button m listener
 			@Override
 			public void onClick(View v) {
 				new Methods().mid();  //Run test.
 			}
 		});
 		
 		e.setOnClickListener(new ocl(){  //Set button e listener
 			@Override
 			public void onClick(View v) {
 				new Methods().extreme(); //Run test.
 			}
 		});
 		
 		s.setOnClickListener(new ocl(){
 			@Override
 			public void onClick(View v){  //Interrupt all threads
                                ThreadPool.emptyit(0); //Empty all threads
 				ThreadPool.t[0].interrupt();
 				ThreadPool.t[1].interrupt();
 				ThreadPool.t[2].interrupt();
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {  //Generate method
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
         public void onDestroy(){
                 ThreadPool.emptyit(1);
                 super.onDestroy();
         }
 	interface ocl extends OnClickListener{} //Create inner interface ocl,extends OnClickListener.
 }
