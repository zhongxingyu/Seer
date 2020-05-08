 package pe.edu.pucp.proyectorh.reportes;
 
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.reflect.TypeToken;
 
 import pe.edu.pucp.proyectorh.R;
 import pe.edu.pucp.proyectorh.connection.ConnectionManager;
 import pe.edu.pucp.proyectorh.reportes.ReportePersonalBSCGrafico.HistoricoBSC;
 import pe.edu.pucp.proyectorh.reportes.ReportePersonalBSCGrafico.getReporteObjPerRef;
 import pe.edu.pucp.proyectorh.services.AsyncCall;
 import pe.edu.pucp.proyectorh.utils.NetDateTimeAdapter;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.graphics.drawable.Drawable;
 import android.graphics.drawable.GradientDrawable;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.view.WindowManager;
 import android.webkit.WebView;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ProgressBar;
 import android.widget.Spinner;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 public class ReporteCubrimientoGrafico extends Fragment{
 	
 	private WebView browser;
 	int idpuesto;
 
 	
 	List<ROferta> ofertas;
 	TextView displayFecha;
 	
 	Button btnRefrescar;
 	ProgressBar pbarra;
 	
 	int mostrando = 0;
 	
 	Button btnPostulantes;
 	Button btnFase1;
 	Button btnFase2;
 	Button btnFase3;
 	
 	Button btnDetalle;
 	
 	int procesoSelec;
 	
 	TextView displayTitulo;
 	Spinner spinnerOferta;
 	
 	int faseSelec;
 	
 	
 	public ReporteCubrimientoGrafico(){
 		
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 	}
 
 	@SuppressWarnings("rawtypes")
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		
 		View rootView = inflater.inflate(R.layout.reportecubrimientografico,
 				container, false);
 		
 		idpuesto = getArguments().getInt("PuestoSelec");
 
 		
 		displayFecha = (TextView)rootView.findViewById(R.id.reportecubDisplayFechagrafico);
 		browser = (WebView)rootView.findViewById(R.id.reportecubWebkit);
 		displayTitulo = (TextView)rootView.findViewById(R.id.reportecubtitulobargraf);
 		displayTitulo.setText("Postulantes por Oferta Laboral");
 		
 		String titulo = getArguments().getString("titulo");
 		// set value into textview
 		TextView textView = (TextView)rootView.findViewById(R.id.reportecubTitulografico);
 		textView.setText(titulo);
 		pbarra = (ProgressBar) rootView.findViewById(R.id.reportecubprogressbarRef);
 		
 		procesoSelec = 0;
 		
 		spinnerOferta = (Spinner) rootView.findViewById(R.id.reportecubofertaspinnerGraf);
 		
 		
 		
 		
 		btnRefrescar = (Button)rootView.findViewById(R.id.reportecubActualizar);
 		
 		btnRefrescar.setOnClickListener(new OnClickListener() {
 			 
 			  @Override
 			  public void onClick(View v) {
 				  
 				  obtenerReporteOffline(idpuesto);
 				  
 				  
 			  }
 		});
 		
 		btnDetalle = (Button)rootView.findViewById(R.id.reportecubbtnDetalle);
 		
 		btnDetalle.setOnClickListener(new OnClickListener() {
 			 
 			  @Override
 			  public void onClick(View v) {
 				  
 				  if (ofertas.get(procesoSelec).getFases().get(faseSelec).getPostulantes() != null){
 					  
 					  	
 						
 					    TableLayout tableLayout = new TableLayout(getActivity());
 					    TableRow tableRow = new TableRow(getActivity());;
 					    TextView textView;
 
					    for (int i = 0; i < 5; i++) {
 
 				            textView = new TextView(getActivity());
 				            if (i==0) textView.setText("Puntaje");
 				            if (i==1) textView.setText("Nombres");
 				            if (i==2) textView.setText("Grado");
 				            if (i==3) textView.setText("Universidad");
				            if (i==4) textView.setText("Resultado");
 				            if (i>0) textView.setPadding(5, 5, 15, 5);
 				            textView.setTextSize(18);
 				            tableRow.addView(textView);
 					        
 					        
 					    }
 					    tableLayout.addView(tableRow);
 
 						
 						for(int i=0;i<ofertas.get(procesoSelec).getFases().get(faseSelec).getPostulantes().size();i++){
 							
 							tableRow = new TableRow(getActivity());
 							
 							textView = new TextView(getActivity());
 							textView.setText(""+ (int)ofertas.get(procesoSelec).getFases().get(faseSelec).getPostulantes().get(i).getPuntaje());
 							textView.setPadding(5, 5, 5, 5);
 				            textView.setTextSize(18);
 				            tableRow.addView(textView);
 				            
 				            textView = new TextView(getActivity());
 							textView.setText(ofertas.get(procesoSelec).getFases().get(faseSelec).getPostulantes().get(i).getNombre());
 							textView.setPadding(5, 5, 15, 5);
 				            textView.setTextSize(18);
 				            tableRow.addView(textView);
 				            
 				            textView = new TextView(getActivity());
 							textView.setText(ofertas.get(procesoSelec).getFases().get(faseSelec).getPostulantes().get(i).getGradoAcademico());
 							textView.setPadding(5, 5, 15, 5);
 				            textView.setTextSize(18);
 				            tableRow.addView(textView);
 				            
 				            textView = new TextView(getActivity());
 							textView.setText(ofertas.get(procesoSelec).getFases().get(faseSelec).getPostulantes().get(i).getProveniencia());
 							textView.setPadding(5, 5, 15, 5);
 				            textView.setTextSize(18);
 				            tableRow.addView(textView);
 				            
				            textView = new TextView(getActivity());
				            textView.setText("");
				            for (int j = 0; j< ofertas.get(procesoSelec).getFases().get(faseSelec + 1).getPostulantes().size();j++){
								
								if (ofertas.get(procesoSelec).getFases().get(faseSelec).getPostulantes().get(i).getNombre().equals(ofertas.get(procesoSelec).getFases().get(faseSelec + 1).getPostulantes().get(j).getNombre())){
									textView.setText("Aprobado");
									break;
								}
				            }

							textView.setPadding(5, 5, 15, 5);
				            textView.setTextSize(18);
				            tableRow.addView(textView);
				               
 				            tableLayout.addView(tableRow);
 							
 							
 						}
 						
 						
 						AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 						builder.setTitle("Detalle de fase");
 						//builder.setMessage(cadena);
 						builder.setCancelable(false);
 						builder.setPositiveButton("Ok", null);
 
 						
 						Dialog d = builder.setView(tableLayout).create();
 						// (That new View is just there to have something inside the dialog that can grow big enough to cover the whole screen.)
 
 						d.show();
 
 						WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
 						lp.copyFrom(d.getWindow().getAttributes());
 						lp.width = 900;
 						lp.height = 600;
 
 						//change position of window on screen
 						//lp.x = mwidth/2; //set these values to what work for you; probably like I have here at
 						//lp.y = mheight/2;        //half the screen width and height so it is in center
 
 						//set the dim level of the background
 						//lp.dimAmount=0.1f; //change this value for more or less dimming
 
 						d.getWindow().setAttributes(lp);
 
 						
 						
 					  }
 				  
 				  
 			  }
 		});
 		
 		
 		btnPostulantes = (Button)rootView.findViewById(R.id.reportecubbtnPost);
 		
 		btnPostulantes.setTextColor(Color.WHITE);
 		btnPostulantes.setBackgroundDrawable(getBackg(0xFF35AA47,0xFF35AA47));
 		
 		btnPostulantes.setOnClickListener(new OnClickListener() {
 			 
 			  @Override
 			  public void onClick(View v) {
 				  displayTitulo.setText("Postulantes por Oferta Laboral");
 				  faseSelec = 0;
 				  cambiarFase(0);
 				 
 				  
 			  }
 		});
 		
 		btnFase1 = (Button)rootView.findViewById(R.id.reportecubbtnFase1);
 		btnFase1.setTextColor(Color.WHITE);
 		btnFase1.setBackgroundDrawable(getBackg(0xFFFFB848,0xFFFFB848));
 		
 		btnFase1.setOnClickListener(new OnClickListener() {
 			 
 			  @Override
 			  public void onClick(View v) {
 				  displayTitulo.setText("Postulantes por Oferta Laboral: Fase 1");
 				  faseSelec = 1;
 				  cambiarFase(1);
 				  
 				  
 			  }
 		});
 		
 		btnFase2 = (Button)rootView.findViewById(R.id.reportecubbtnFase2);
 		btnFase2.setTextColor(Color.WHITE);
 		btnFase2.setBackgroundDrawable(getBackg(0xFF4D90FE,0xFF4D90FE));
 		
 		btnFase2.setOnClickListener(new OnClickListener() {
 			 
 			  @Override
 			  public void onClick(View v) {
 				  displayTitulo.setText("Postulantes por Oferta Laboral: Fase 2");
 				  faseSelec = 2;
 				  cambiarFase(2);
 				  
 				  
 			  }
 		});
 		
 		btnFase3 = (Button)rootView.findViewById(R.id.reportecubbtnFase3);
 		btnFase3.setTextColor(Color.WHITE);
 		btnFase3.setBackgroundDrawable(getBackg(0xFFD84A38,0xFFD84A38));
 		
 		btnFase3.setOnClickListener(new OnClickListener() {
 			 
 			  @Override
 			  public void onClick(View v) {
 				  displayTitulo.setText("Postulantes por Oferta Laboral: Fase 3");
 				  faseSelec = 3;
 				  cambiarFase(3);
 				  
 				  
 			  }
 		});
 
 		ofertas= new ArrayList<ROferta>();
 		cargarGraficoPuesto(idpuesto);
 		
 		customizarEstilos(getActivity(), rootView);
 		return rootView;
 	}
 	
 	public Drawable getBackg(int startColor, int endColor){
 		GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, 
 				new int[] {startColor, endColor });
 	    //gd.setCornerRadius(0f);
 	    gd.setStroke(1, Color.parseColor("#000000"));
 	    //gd.setCornerRadius(3f);
 	    return gd;
 	}
 	
 	
 	public void  cambiarFase(int fase){
 		
 		if (ofertas.size()>0){
 			
 			System.out.println("total fases: " + ofertas.get(procesoSelec).getFases().size() + " ...entrando a fase " + fase);
 		
 		ArrayList<Double> bachiller = new ArrayList<Double>() {{ add(0.0); add(0.0); add(0.0); }};  //numPost, promedioPunt, aprob
 		ArrayList<Double> master = new ArrayList<Double>()  {{ add(0.0); add(0.0); add(0.0); }}; 
 		ArrayList<Double> doctorado = new ArrayList<Double>() {{ add(0.0); add(0.0); add(0.0); }};  
 		ArrayList<Double> tecnico = new ArrayList<Double>()  {{ add(0.0); add(0.0); add(0.0); }};  
 		ArrayList<Double> estudiante = new ArrayList<Double>()  {{ add(0.0); add(0.0); add(0.0); }}; 
 		ArrayList<Double> licenciado = new ArrayList<Double>()  {{ add(0.0); add(0.0); add(0.0); }}; 
 		
 		for (int i = 0; i< ofertas.get(procesoSelec).getFases().get(fase).getPostulantes().size();i++){
 			
 			
 			
 			if (ofertas.get(procesoSelec).getFases().get(fase).getPostulantes().get(i).getGradoAcademico().equals("Bachiller")){
 				bachiller.set(0, bachiller.get(0) + 1); //numpost++
 				bachiller.set(1, bachiller.get(1) + ofertas.get(procesoSelec).getFases().get(fase).getPostulantes().get(i).getPuntaje()); //promedioPunt
 				
 				if ((fase + 1)< ofertas.get(procesoSelec).getFases().size()){
 					
 				
 				
 					for (int j = 0; j< ofertas.get(procesoSelec).getFases().get(fase + 1).getPostulantes().size();j++){
 											
 						if (ofertas.get(procesoSelec).getFases().get(fase).getPostulantes().get(i).getNombre().equals(ofertas.get(procesoSelec).getFases().get(fase + 1).getPostulantes().get(j).getNombre())){
 							bachiller.set(2, bachiller.get(2) + 1); //aprob++
 							break;
 						}
 						
 					}
 				
 				}
 				
 			}
 			
 			if (ofertas.get(procesoSelec).getFases().get(fase).getPostulantes().get(i).getGradoAcademico().equals("Master")){
 				master.set(0, master.get(0) + 1); //numpost++
 				master.set(1, master.get(1) + ofertas.get(procesoSelec).getFases().get(fase).getPostulantes().get(i).getPuntaje()); //promedioPunt
 				
 				if ((fase + 1)< ofertas.get(procesoSelec).getFases().size()){
 				
 				for (int j = 0; j< ofertas.get(procesoSelec).getFases().get(fase + 1).getPostulantes().size();j++){
 					
 					if (ofertas.get(procesoSelec).getFases().get(fase).getPostulantes().get(i).getNombre().equals(ofertas.get(procesoSelec).getFases().get(fase + 1).getPostulantes().get(j).getNombre())){
 						master.set(2, master.get(2) + 1); //aprob++
 						break;
 					}
 					
 				}
 				
 				}
 				
 			}
 			
 			if (ofertas.get(procesoSelec).getFases().get(fase).getPostulantes().get(i).getGradoAcademico().equals("Doctor")){
 				doctorado.set(0, doctorado.get(0) + 1); //numpost++
 				doctorado.set(1, doctorado.get(1) + ofertas.get(procesoSelec).getFases().get(fase).getPostulantes().get(i).getPuntaje()); //promedioPunt
 				
 				if ((fase + 1)< ofertas.get(procesoSelec).getFases().size()){
 				
 				for (int j = 0; j< ofertas.get(procesoSelec).getFases().get(fase + 1).getPostulantes().size();j++){
 					
 					if (ofertas.get(procesoSelec).getFases().get(fase).getPostulantes().get(i).getNombre().equals(ofertas.get(procesoSelec).getFases().get(fase + 1).getPostulantes().get(j).getNombre())){
 						doctorado.set(2, doctorado.get(2) + 1); //aprob++
 						break;
 					}
 					
 				}
 				
 				}
 				
 			}
 			
 			if (ofertas.get(procesoSelec).getFases().get(fase).getPostulantes().get(i).getGradoAcademico().equals("Tcnico")){
 				tecnico.set(0, tecnico.get(0) + 1); //numpost++
 				tecnico.set(1, tecnico.get(1) + ofertas.get(procesoSelec).getFases().get(fase).getPostulantes().get(i).getPuntaje()); //promedioPunt
 				
 				if ((fase + 1)< ofertas.get(procesoSelec).getFases().size()){
 				
 				for (int j = 0; j< ofertas.get(procesoSelec).getFases().get(fase + 1).getPostulantes().size();j++){
 					
 					if (ofertas.get(procesoSelec).getFases().get(fase).getPostulantes().get(i).getNombre().equals(ofertas.get(procesoSelec).getFases().get(fase + 1).getPostulantes().get(j).getNombre())){
 						tecnico.set(2, tecnico.get(2) + 1); //aprob++
 						break;
 					}
 					
 				}
 				
 				}
 				
 			}
 			
 			if (ofertas.get(procesoSelec).getFases().get(fase).getPostulantes().get(i).getGradoAcademico().equals("Estudiante")){
 				estudiante.set(0, estudiante.get(0) + 1); //numpost++
 				estudiante.set(1, estudiante.get(1) + ofertas.get(procesoSelec).getFases().get(fase).getPostulantes().get(i).getPuntaje()); //promedioPunt
 				
 				if ((fase + 1)< ofertas.get(procesoSelec).getFases().size()){
 				
 				for (int j = 0; j< ofertas.get(procesoSelec).getFases().get(fase + 1).getPostulantes().size();j++){
 					
 					if (ofertas.get(procesoSelec).getFases().get(fase).getPostulantes().get(i).getNombre().equals(ofertas.get(procesoSelec).getFases().get(fase + 1).getPostulantes().get(j).getNombre())){
 						estudiante.set(2, estudiante.get(2) + 1); //aprob++
 						break;
 					}
 					
 				}
 				
 				}
 				
 			}
 			
 			if (ofertas.get(procesoSelec).getFases().get(fase).getPostulantes().get(i).getGradoAcademico().equals("Licenciado")){
 				licenciado.set(0, licenciado.get(0) + 1); //numpost++
 				licenciado.set(1, licenciado.get(1) + ofertas.get(procesoSelec).getFases().get(fase).getPostulantes().get(i).getPuntaje()); //promedioPunt
 				
 				if ((fase + 1)< ofertas.get(procesoSelec).getFases().size()){
 				
 				for (int j = 0; j< ofertas.get(procesoSelec).getFases().get(fase + 1).getPostulantes().size();j++){
 					
 					if (ofertas.get(procesoSelec).getFases().get(fase).getPostulantes().get(i).getNombre().equals(ofertas.get(procesoSelec).getFases().get(fase + 1).getPostulantes().get(j).getNombre())){
 						licenciado.set(2, licenciado.get(2) + 1); //aprob++
 						break;
 					}
 					
 				}
 				
 				}
 				
 			}
 			
 			
 		}
 		
 		//promedio
 		if(bachiller.get(0)> 0) bachiller.set(1, bachiller.get(1)/bachiller.get(0)); 
 		if(master.get(0)> 0) master.set(1, master.get(1)/master.get(0)); 
 		if(doctorado.get(0)> 0) doctorado.set(1, doctorado.get(1)/doctorado.get(0)); 
 		if(estudiante.get(0)> 0) estudiante.set(1, estudiante.get(1)/estudiante.get(0)); 
 		if(tecnico.get(0)> 0) tecnico.set(1, tecnico.get(1)/tecnico.get(0)); 
 		if(licenciado.get(0)> 0) licenciado.set(1, licenciado.get(1)/licenciado.get(0)); 
 		
 		
 		
 		browser.getSettings().setJavaScriptEnabled(true);
 		browser.getSettings().setPluginsEnabled(true);
 		
 		DataObject data = new DataObject( bachiller, master, doctorado, estudiante,tecnico ,licenciado );
 		
 		InterfaceChartLineal intface = new InterfaceChartLineal(getActivity(),data);
 		
 		browser.addJavascriptInterface(intface, "Android");
 		
 		
 		browser.loadUrl("file:///android_asset/ReporteCubrimientochart.html");
 		}
 	}
 	
 	
 	protected void obtenerReporteOffline(int idPuesto){
 		
 		if (ConnectionManager.connect(getActivity())) {
 			// construir llamada al servicio
 			
 			String request = ReporteServices.obtenerOfertas + "?puesto=" + idPuesto;
 
 			new getReporteOfertasRef().execute(request);
 			
 		} else {
 			// Se muestra mensaje de error de conexion con el servicio
 			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 			builder.setTitle("Error de conexin");
 			builder.setMessage("No se pudo conectar con el servidor. Revise su conexin a Internet.");
 			builder.setCancelable(false);
 			builder.setPositiveButton("Ok", null);
 			builder.create();
 			builder.show();
 		}
 		
 	}
 	
 	public class getReporteOfertasRef extends AsyncCall{
 		
 		@Override
 		protected void onPreExecute(){
 			pbarra.setVisibility(View.VISIBLE);
 		}
 		
 		@Override
 		protected void onPostExecute(String result) {
 			
 			pbarra.setVisibility(View.INVISIBLE);
 			
 			String trama = result;
 			
 			System.out.println("Recibido: " + result.toString());
 			
 			List<ROferta> pruebaOferta=null;
 			
 			//prueba
 			try{
 				Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new NetDateTimeAdapter()).create();
 				pruebaOferta = gson.fromJson(trama, new TypeToken<List<ROferta>>(){}.getType()); 
 				
 			}
 			catch(Exception e){
 				Toast.makeText(getActivity(), "Error al actualizar el reporte", Toast.LENGTH_SHORT).show();
 				
 				
 			}
 			
 			if (pruebaOferta!=null){
 			
 				String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
 		
 				PersistentHandler.agregarArchivoPersistente(currentDateTimeString + "\n" + result, getActivity(), "ReporteOferta" + idpuesto +".txt");
 				
 				//actualizar gridview
 				cargarGraficoPuesto(idpuesto);
 			
 			}
 		}
 		
 	}
 	
 	
 	
 	public void cargarGraficoPuesto(int idpuesto){
 		ofertas = null;
 		
 		ofertas = PersistentHandler.getReporteOfertasFromFile(getActivity(), "ReporteOferta" + idpuesto +".txt");
 		  if (ofertas != null){
 			  String fechaarch = PersistentHandler.getFechaReporte(getActivity(), "ReporteOferta" + idpuesto +".txt");
 			  displayFecha.setText("Fecha de reporte: " + fechaarch);
 			  
 			  if (ofertas.size()==0){
 					 String summary = "<html><body>No se encontraron resultados</body></html>";
 					 browser.loadData(summary, "text/html", null);
 			  }
 			  else{
 				  //procesoSelec = 0;
 
 				  List<String> listaOferta = new ArrayList<String>();
 					for(int i = 0; i<ofertas.size();i++){
 						
 						listaOferta.add(ofertas.get(i).getDescripcion());
 						
 					}
 				  ArrayAdapter dataAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, listaOferta);
 				  dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 				  spinnerOferta.setAdapter(dataAdapter);
 				  spinnerOferta.setOnItemSelectedListener(new OnItemSelectedListener(){
 						
 						@Override
 						public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
 							
 							procesoSelec=pos;
							faseSelec=0;
 							displayTitulo.setText("Postulantes por Oferta Laboral");
							cambiarFase(0);
 						}
 						
 					
 						@Override
 						  public void onNothingSelected(AdapterView<?> arg0) {
 							// TODO Auto-generated method stub
 						  }
 						
 					 });
 				  
 				  
 			  }
 			  
 			  
 			  
 		  }
 		  else{
 			  ofertas = new ArrayList<ROferta>();
 			  displayFecha.setText("No se puede recuperar la informacin");
 			  
 		  }
 		
 		
 	}
 	
 	
 	public class InterfaceChartLineal {
 	    Context mContext;
 	    DataObject obj;
 	    /** Instantiate the interface and set the context */
 	    InterfaceChartLineal(Context c, DataObject o) {
 	        mContext = c;
 	        obj = o;
 	    }
 
 	    
 	    public String getData(){
 	    	
 	    	
 	    	Gson gson = new Gson();
 	    	 
 	    	// convert java object to JSON format,
 	    	// and returned as JSON formatted string
 	    	String json = gson.toJson(obj);
 	    	System.out.println(json);
 	    	return json;
 	    }
 
 
 	}
 	
 	public class DataObject {
 		
 		
 		double[] bachiller;
 		double[] master;
 		double[] doctorado;
 		double[] tecnico;
 		double[] licenciado;
 		double[] estudiante; 
 		
 		public DataObject(ArrayList<Double> arrayBach, ArrayList<Double> arrayMaster,ArrayList<Double> arrayDoctor, ArrayList<Double> arrayEst, ArrayList<Double> arrayTec,ArrayList<Double> arrayLic){
 			
 			bachiller = new double[3];
 			master = new double[3];
 			doctorado = new double[3];
 			tecnico = new double[3];
 			licenciado = new double[3];
 			estudiante = new double[3];
 			
 			for (int i=0;i<3;i++){
 				bachiller[i] = arrayBach.get(i);
 				master[i] = arrayMaster.get(i);
 				doctorado[i] = arrayDoctor.get(i);
 				tecnico[i] = arrayTec.get(i);
 				licenciado[i] = arrayLic.get(i);
 				estudiante[i] = arrayEst.get(i);
 			}
 			
 		}
 		
 
 	}
 	
 	public class ROfertasLaborales
 	{
 	        private String nombreProveniencia;
 	        private String gradoAcademico;
 	        
 	        public String getNombreProveniencia() {
 				return nombreProveniencia;
 			}
 			public void setNombreProveniencia(String nombreProveniencia) {
 				this.nombreProveniencia = nombreProveniencia;
 			}
 			public String getGradoAcademico() {
 				return gradoAcademico;
 			}
 			public void setGradoAcademico(String gradoAcademico) {
 				this.gradoAcademico = gradoAcademico;
 			}
 			public int getCantPostulantes() {
 				return cantPostulantes;
 			}
 			public void setCantPostulantes(int cantPostulantes) {
 				this.cantPostulantes = cantPostulantes;
 			}
 			public int getCantElegidos() {
 				return cantElegidos;
 			}
 			public void setCantElegidos(int cantElegidos) {
 				this.cantElegidos = cantElegidos;
 			}
 			
 			private int cantPostulantes;
 			private int cantElegidos;
 	}
 	
 	
 	public class RPostulante
     {
         public String Proveniencia;
         public String gradoAcademico;
         public String nombre;
         public double puntaje;
         public int ID;
 		public String getProveniencia() {
 			return Proveniencia;
 		}
 		public void setProveniencia(String proveniencia) {
 			Proveniencia = proveniencia;
 		}
 		public String getGradoAcademico() {
 			return gradoAcademico;
 		}
 		public void setGradoAcademico(String gradoAcademico) {
 			this.gradoAcademico = gradoAcademico;
 		}
 		public String getNombre() {
 			return nombre;
 		}
 		public void setNombre(String nombre) {
 			this.nombre = nombre;
 		}
 		public double getPuntaje() {
 			return puntaje;
 		}
 		public void setPuntaje(double puntaje) {
 			this.puntaje = puntaje;
 		}
 		public int getID() {
 			return ID;
 		}
 		public void setID(int iD) {
 			ID = iD;
 		}
         
         
     }
     public class RFase
     {
         public String nombreFase;
         public int numPostulantes;
         public List<RPostulante>  Postulantes;
         public double PuntajeMaximo;
         public double PuntajeMinimo;
         public double PuntajePromedio;
 		public String getNombreFase() {
 			return nombreFase;
 		}
 		public void setNombreFase(String nombreFase) {
 			this.nombreFase = nombreFase;
 		}
 		public int getNumPostulantes() {
 			return numPostulantes;
 		}
 		public void setNumPostulantes(int numPostulantes) {
 			this.numPostulantes = numPostulantes;
 		}
 		public List<RPostulante> getPostulantes() {
 			return Postulantes;
 		}
 		public void setPostulantes(List<RPostulante> postulantes) {
 			Postulantes = postulantes;
 		}
 		public double getPuntajeMaximo() {
 			return PuntajeMaximo;
 		}
 		public void setPuntajeMaximo(double puntajeMaximo) {
 			PuntajeMaximo = puntajeMaximo;
 		}
 		public double getPuntajeMinimo() {
 			return PuntajeMinimo;
 		}
 		public void setPuntajeMinimo(double puntajeMinimo) {
 			PuntajeMinimo = puntajeMinimo;
 		}
 		public double getPuntajePromedio() {
 			return PuntajePromedio;
 		}
 		public void setPuntajePromedio(double puntajePromedio) {
 			PuntajePromedio = puntajePromedio;
 		}
         
         
     }
     public class ROferta
     {
         public String descripcion;
         public String fecha;
         public List<RFase> Fases;
 		public String getDescripcion() {
 			return descripcion;
 		}
 		public void setDescripcion(String descripcion) {
 			this.descripcion = descripcion;
 		}
 		public String getFecha() {
 			return fecha;
 		}
 		public void setFecha(String fecha) {
 			this.fecha = fecha;
 		}
 		public List<RFase> getFases() {
 			return Fases;
 		}
 		public void setFases(List<RFase> fases) {
 			Fases = fases;
 		}
         
         
         
     }
 	
     
     private void customizarEstilos(Context context, View view) {
 		try {
 			if (view instanceof ViewGroup) {
 				ViewGroup vg = (ViewGroup) view;
 				for (int i = 0; i < vg.getChildCount(); i++) {
 					View child = vg.getChildAt(i);
 					customizarEstilos(context, child);
 				}
 			} else if (view instanceof TextView) {
 			((TextView) view).setTypeface(Typeface.createFromAsset(
 			context.getAssets(), "OpenSans-Light.ttf"));
 		}
 		} catch (Exception e) {
 		}
 	}
 	
 
 }
