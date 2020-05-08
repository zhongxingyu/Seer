 package code.google.com;
 
 import java.util.ArrayList;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class TodasParcelasIconListView extends ListActivity {
 	   private ArrayList<Local> m_locals = null;
 	    private IconListViewAdapter m_adapter;
 	    private String dni;
 	    private ArrayList objetosCompletos;
 		
 	    @Override
 	    public void onCreate(Bundle savedInstanceState) {
 	        super.onCreate(savedInstanceState);
 	        setContentView(R.layout.listabusqueda);
 	        
 	       
 	        final TextView lblDni= (TextView) findViewById(R.id.tvTituloBuscar);
 	        final EditText txtDNI = (EditText) findViewById(R.id.txtDNIBuscar);
 	        final Button cmdBuscar = (Button) findViewById(R.id.cmdBuscar);
 	        
 	        lblDni.setText("Dni:");
 	        cmdBuscar.setText(getString(R.string.search));
 	        
 	        /*
 	         * Al crear la clase se inicializa el ListView que muestra los aperos
 	         */          
 	        
 	        m_locals = new ArrayList<Local>();
 	        this.m_adapter = new IconListViewAdapter(this, R.layout.iconrow, m_locals);
 	        setListAdapter(this.m_adapter);
 	        
 	        cmdBuscar.setOnClickListener(new View.OnClickListener(){
 	        	
 
 				public void onClick(View arg0) {
 					
 					String dniBuscado = txtDNI.getText().toString();
 					
 					inicializarLocales(dniBuscado);
 					
 				}
 	        	
 	        	
 	        });
 
 	    }
 	    
 	    @Override
 	    protected void onListItemClick(ListView l, View v, int position, long id) {
 
 	    	
 	    	
 	    	final ParcelasDatos parcelaSeleccionada = (ParcelasDatos) objetosCompletos.get(position);
 
 	    	
 	    
 	        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(TodasParcelasIconListView.this);
 	        dialogBuilder.setMessage(getString(R.string.msgAddLots));
 	        dialogBuilder.setCancelable(true).setTitle(getString(R.string.Tools));
 	        dialogBuilder.setPositiveButton(getString(R.string.add),new DialogInterface.OnClickListener() { 
 	            public void onClick(DialogInterface dialog, int arg1) {
 
 	            	try{
 	            		
 	            	
 		            	Bundle extras = getIntent().getExtras();
 		            	String idParcela = parcelaSeleccionada.getIdparcela();
 		            	String dniUsuario = extras.getString("dni");

 		            	
 		            	String consulta = "http://79.108.245.167/OpenGisMobile/VerParcelasUsuariosWebService.php?dni="+dniUsuario+"&idparcela="+idParcela+"";
 		            	
 		            	Object[] parcelasUsuario = AccesoWebService.convertirDatosJSONParcelasUsuarios(AccesoWebService.recogerDatosWebService(consulta));

 		            	
		            	if(parcelasUsuario==null){
 		            		
		            		String url = "http://79.108.245.167/OpenGisMobile/NuevaParcelaEnListaWebService.php?dni="+dniUsuario+"&idparcela="+idParcela+"";
 		            		
		            		boolean acceso = AccesoWebService.InsertarEnWebService(url);
 			            	
 			            	if(acceso){
 			            		
 			            		
 			            		Toast toast = Toast.makeText(getApplicationContext(),getString(R.string.msgAddLotOK), Toast.LENGTH_SHORT);
 			            		toast.show();
 			            		
 			            		
 			            	}else{
 			            		
 			            		
 			            		//No se ha podido realizar la insercin correctamente
 			            		
 			            		
 			            	}
 		            		
 		            		
 		            	}else{
 		            		
 		            		
 			            	
 			            	String url = "http://79.108.245.167/OpenGisMobile/ActivarParcelaWebService.php?dni="+dniUsuario+"&idparcela="+idParcela+"";
 			            	
 			            	boolean acceso = AccesoWebService.InsertarEnWebService(url);
 			            	
 			            	if(acceso){
 			            		
 			            		
 			            		Toast toast = Toast.makeText(getApplicationContext(),getString(R.string.msgAddLotOK), Toast.LENGTH_SHORT);
 			            		toast.show();
 			            		
 			            		
 			            	}else{
 			            		
 			            		
 			            		//No se ha podido realizar la insercin correctamente
 			            		
 			            		
 			            	}
 		            		
 		            		
 		            		
 		            	}
 
 		            	
 	
 		            	
 		            }catch(Exception e2){
 	            	
 	            	
 	            }
 	            	
 	            }
 		        }); 
 		        
 		        dialogBuilder.setNegativeButton(getString(R.string.view),new DialogInterface.OnClickListener() { 
 		            public void onClick(DialogInterface dialog, int arg1) { 
 		                
 		            	
 		            	String url = "http://sigpac.mapa.es/fega/salidasgraficas/AspPrintLotProvider.aspx?layer=PARCELA&RCat=" //$NON-NLS-1$
 	                                  +parcelaSeleccionada.getProvincia()+","+parcelaSeleccionada.getPoblacion()+
 	                                  ",0,0,"+parcelaSeleccionada.getPoligono()+ //$NON-NLS-1$
 	                                  ","+parcelaSeleccionada.getNumero()+"&visibleLayers=PARCELA;RECINTO;ARBOLES&etiquetas=true";
 		            	
 		            	
 		            	 Intent i = new Intent("android.intent.action.VIEW", Uri.parse(url));
 		                 startActivity(i);
 		            	
 		            	
 		            } 
 		        }); 
 		        dialogBuilder.create().show();
 		        
 
 		}
 	    	
 	    	
 	    
 
 	   
 	    
 	    /*
 	     * Inicializacion del mapa
 	     */
 	    
 	    private void inicializarLocales(String dniBuscado){
 	    	
 	    	if(dniBuscado == " "){
 	    		
         		Toast toast = Toast.makeText(getApplicationContext(),getString(R.string.msgInsertDni), Toast.LENGTH_SHORT);
         		toast.show(); 
 	    		
 	    	}else{
 	    	
 		    	try {
 		    	
 		    	///////////DE AQUI//////////////
 		    	m_locals = new ArrayList<Local>();
 		    		
 				String url = "http://79.108.245.167/OpenGisMobile/MostrarParcelasWebService.php?dni="+dniBuscado+"";
 				
 				String data = AccesoWebService.recogerDatosWebService(url);
 				
 				final Object[] listaParcelas = AccesoWebService.convertirDatosJSONParcelas(data);
 				
 				objetosCompletos = new ArrayList();
 				
 				for(int i=0;i<listaParcelas.length;i++){
 					
 					
 					ParcelasDatos parcela = (ParcelasDatos) listaParcelas[i];
 					
 					// En caso de que ese apero del usuario est activo, lo aadiremos
 					
 					if(parcela.getActivo().equals("1")){
 	
 						String productoMostrar = parcela.getAlias();
 						objetosCompletos.add(parcela);
 						
 						Local loc = new Local();
 						loc.setLocalName(parcela.getAlias());
 						loc.setLocalMedida("ID: " + parcela.getIdparcela() + " - DNI: " + parcela.getDNIPropietario());
 						loc.setLocalImage(R.drawable.ic_launcher);
 						
 						m_locals.add(loc);
 					}
 					
 				}
 		    	
 		    	//////////A AQUI///////////////
 		    	
 		            /*m_locals = new ArrayList<Local>();
 	
 		            Local o1 = new Local();
 		            o1.setLocalName("Apero2");
 		            o1.setLocalMedida("5,7m");
 		            o1.setLocalImage(R.drawable.ic_launcher);
 		           
 	
 		            m_locals.add(o1);*/
 	       
 		            Log.i("Parcelas añadidos ", ""+ m_locals.size());
 		            
 		          } catch (Exception e) {
 		            Log.e("BACKGROUND_PROC", e.getMessage());
 		          }
 		          
 		    	
 		    	
 		          if(m_locals != null && m_locals.size() > 0){
 		              for(int i=0;i<m_locals.size();i++)
 		              m_adapter.add(m_locals.get(i));
 		          }
 	
 		          m_adapter.notifyDataSetChanged();
 		          
 	    	}
 	    	
 	    }  
     
 
     public class IconListViewAdapter extends ArrayAdapter<Local> {
 
     	        private ArrayList<Local> items;
 
     	        public IconListViewAdapter(Context context, int textViewResourceId, ArrayList<Local> items) {
     	                super(context, textViewResourceId, items);
     	                this.items = items;
     	        }
     	        @Override
     	        public View getView(int position, View convertView, ViewGroup parent) {
     	                View v = convertView;
     	                if (v == null) {
     	                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     	                    v = vi.inflate(R.layout.iconrow, null);
     	                }
     	                Local o = items.get(position);
     	                if (o != null) {
     	                	
     	                	//poblamos la lista de elementos
     	                	
     	                        TextView tt = (TextView) v.findViewById(R.id.row_toptext);
     	                        TextView tta = (TextView) v.findViewById(R.id.row_bottomtext);
     	                        ImageView im = (ImageView) v.findViewById(R.id.icon);
     
     	                        
     	                        if (im!= null) {
     	                        	im.setImageResource(o.getLocalImage());
     	                        }
     	                        if (tta!= null) {
     	                        	tta.setText(o.getLocalMedida());
     	                        }    
     	                        if (tt != null) {             
     	                            tt.setText(o.getLocalName());                             
     	                        }    	                    	                        
     	                }
     	                return v;
     	        }
     	}
     
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event)  {
         if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
         	Intent vPrincipal = new Intent(TodasParcelasIconListView.this,principalActivity.class);
         	vPrincipal.putExtra("dni",dni);
         	startActivity(vPrincipal);
         	
             return true;
         }
 
         return super.onKeyDown(keyCode, event);
     }
     	
 
     public void alertaMensaje(String cadena,String titulo) {
         
         AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
         dialogBuilder.setMessage(cadena);
         dialogBuilder.setCancelable(true).setTitle(titulo);
         dialogBuilder.create().show();
         }
     
     
 }
