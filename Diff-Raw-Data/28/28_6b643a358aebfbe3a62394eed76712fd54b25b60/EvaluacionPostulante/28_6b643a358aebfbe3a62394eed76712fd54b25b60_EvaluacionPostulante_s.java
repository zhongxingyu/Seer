 package pe.edu.pucp.proyectorh.reclutamiento;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import pe.edu.pucp.proyectorh.R;
 import pe.edu.pucp.proyectorh.connection.ConnectionManager;
 import pe.edu.pucp.proyectorh.model.Evaluacion;
 import pe.edu.pucp.proyectorh.model.Funcion;
 import pe.edu.pucp.proyectorh.model.OfertaLaboral;
 import pe.edu.pucp.proyectorh.model.Postulante;
 import pe.edu.pucp.proyectorh.model.Respuesta;
 import pe.edu.pucp.proyectorh.services.AsyncCall;
 import pe.edu.pucp.proyectorh.services.ConstanteServicio;
 import pe.edu.pucp.proyectorh.services.ErrorServicio;
 import pe.edu.pucp.proyectorh.services.Servicio;
 import android.annotation.SuppressLint;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentTransaction;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.RatingBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 @SuppressLint({ "ValidFragment", "ValidFragment" })
 public class EvaluacionPostulante extends Fragment {
 
 	private View rootView;
 	private Postulante postulante;
 	private OfertaLaboral oferta;
 	private ArrayList<Funcion> funciones;
 	private ArrayList<Respuesta> respuestas;
 	private Evaluacion evaluacion;
 	private int numPagina;
 	private int totalPaginas;
 	private final int PREGUNTAS_X_PAGINA = 4;
 
 	public EvaluacionPostulante(OfertaLaboral oferta, Postulante postulante) {
 		this.oferta = oferta;
 		this.postulante = postulante;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		rootView = inflater.inflate(R.layout.rendir_evaluaciones, container,
 				false);
 		llamarServicioObtenerEvaluacion();
 		activarBotonAtras();
 		activarBotonFinalizar();
 		activarBotonSiguiente();
 		numPagina = 0;
 		return rootView;
 	}
 
 	private void activarBotonSiguiente() {
 		Button botonSiguiente = (Button) rootView
 				.findViewById(R.id.siguienteEvaluacion);
 		botonSiguiente.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if (numPagina < totalPaginas - 1) {
 					guardarRespuestas();
 					numPagina++;
 					refreshLayout();
 				} else {
 					Toast.makeText(getActivity(),
 							"Estas son las ltimas preguntas de la evaluacin",
 							Toast.LENGTH_SHORT).show();
 				}
 			}
 		});
 	}
 
 	private void activarBotonAtras() {
 		Button botonAtras = (Button) rootView
 				.findViewById(R.id.atrasEvaluacion);
 		botonAtras.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if (numPagina > 0) {
 					guardarRespuestas();
 					numPagina--;
 					refreshLayout();
 				} else {
 					Toast.makeText(
 							getActivity(),
 							"Estas son las primeras preguntas de la evaluacin",
 							Toast.LENGTH_SHORT).show();
 				}
 			}
 		});
 	}
 
 	private void activarBotonFinalizar() {
 		Button botonFinalizar = (Button) rootView.findViewById(R.id.finalizar);
 		botonFinalizar.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				guardarRespuestas();
 				if (seCompletoEvaluacion()) {
 					AlertDialog.Builder builder = new AlertDialog.Builder(
 							getActivity());
 					builder.setTitle("Finalizar evaluacin");
 					builder.setMessage("Desea finalizar la evaluacin con los resultados registrados?");
 					builder.setCancelable(false);
 					builder.setNegativeButton("Cancelar",
 							new DialogInterface.OnClickListener() {
 
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									dialog.cancel();
 								}
 							});
 					builder.setPositiveButton("Aceptar",
 							new DialogInterface.OnClickListener() {
 
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									FragmentTransaction ft = getActivity()
 											.getSupportFragmentManager()
 											.beginTransaction();
 									ConfirmacionEvaluacion fragment = new ConfirmacionEvaluacion(
 											oferta, postulante, funciones,
 											respuestas, evaluacion);
 									ft.setCustomAnimations(
 											android.R.anim.slide_in_left,
 											android.R.anim.slide_out_right);
 									ft.replace(R.id.opcion_detail_container,
 											fragment, "detailFragment")
 											.commit();
 								}
 							});
 					builder.create();
 					builder.show();
 				} else {
 					Toast.makeText(
 							getActivity(),
 							"Debe completar todas las preguntas de la evaluacin para finalizar",
 							Toast.LENGTH_LONG).show();
 				}
 			}
 		});
 	}
 
 	protected void refreshLayout() {
 		TextView pregunta1Text = (TextView) rootView
 				.findViewById(R.id.pregunta1);
 		pregunta1Text.setText(numPagina
 				* PREGUNTAS_X_PAGINA
 				+ 1
 				+ ") "
 				+ funciones.get(numPagina * PREGUNTAS_X_PAGINA + 0)
 						.getDescripcion());
 		TextView pregunta2Text = (TextView) rootView
 				.findViewById(R.id.pregunta2);
 		pregunta2Text.setText(numPagina
 				* PREGUNTAS_X_PAGINA
 				+ 2
 				+ ") "
 				+ funciones.get(numPagina * PREGUNTAS_X_PAGINA + 1)
 						.getDescripcion());
 		TextView pregunta3Text = (TextView) rootView
 				.findViewById(R.id.pregunta3);
 		pregunta3Text.setText(numPagina
 				* PREGUNTAS_X_PAGINA
 				+ 3
 				+ ") "
 				+ funciones.get(numPagina * PREGUNTAS_X_PAGINA + 2)
 						.getDescripcion());
 		TextView pregunta4Text = (TextView) rootView
 				.findViewById(R.id.pregunta4);
 		pregunta4Text.setText(numPagina
 				* PREGUNTAS_X_PAGINA
 				+ 4
 				+ ") "
 				+ funciones.get(numPagina * PREGUNTAS_X_PAGINA + 3)
 						.getDescripcion());
 
 		RatingBar ratingPregunta1 = (RatingBar) rootView
 				.findViewById(R.id.ratingPregunta1);
 		ratingPregunta1.setRating(respuestas.get(
 				numPagina * PREGUNTAS_X_PAGINA + 0).getPuntaje());
 		RatingBar ratingPregunta2 = (RatingBar) rootView
 				.findViewById(R.id.ratingPregunta2);
 		ratingPregunta2.setRating(respuestas.get(
 				numPagina * PREGUNTAS_X_PAGINA + 1).getPuntaje());
 		RatingBar ratingPregunta3 = (RatingBar) rootView
 				.findViewById(R.id.ratingPregunta3);
 		ratingPregunta3.setRating(respuestas.get(
 				numPagina * PREGUNTAS_X_PAGINA + 2).getPuntaje());
 		RatingBar ratingPregunta4 = (RatingBar) rootView
 				.findViewById(R.id.ratingPregunta4);
 		ratingPregunta4.setRating(respuestas.get(
 				numPagina * PREGUNTAS_X_PAGINA + 3).getPuntaje());
 	}
 
 	protected void guardarRespuestas() {
 		RatingBar ratingPregunta1 = (RatingBar) rootView
 				.findViewById(R.id.ratingPregunta1);
 		RatingBar ratingPregunta2 = (RatingBar) rootView
 				.findViewById(R.id.ratingPregunta2);
 		RatingBar ratingPregunta3 = (RatingBar) rootView
 				.findViewById(R.id.ratingPregunta3);
 		RatingBar ratingPregunta4 = (RatingBar) rootView
 				.findViewById(R.id.ratingPregunta4);
 		respuestas.get(numPagina * PREGUNTAS_X_PAGINA + 0).setPuntaje(
 				(int) ratingPregunta1.getRating());
 		respuestas.get(numPagina * PREGUNTAS_X_PAGINA + 1).setPuntaje(
 				(int) ratingPregunta2.getRating());
 		respuestas.get(numPagina * PREGUNTAS_X_PAGINA + 2).setPuntaje(
 				(int) ratingPregunta3.getRating());
 		respuestas.get(numPagina * PREGUNTAS_X_PAGINA + 3).setPuntaje(
 				(int) ratingPregunta4.getRating());
 	}
 
 	private void llamarServicioObtenerEvaluacion() {
 		obtenerEvaluacionPostulante();
 	}
 
 	private void mostrarEvaluacion() {
 		TextView tituloPuestoText = (TextView) rootView
 				.findViewById(R.id.puesto_title);
 		tituloPuestoText.setText("Puesto: " + oferta.getPuesto().getNombre());
 		TextView tituloPostulanteText = (TextView) rootView
 				.findViewById(R.id.postulante_title);
 		tituloPostulanteText.setText("Postulante: " + postulante.toString());
 
 		TextView pregunta1Text = (TextView) rootView
 				.findViewById(R.id.pregunta1);
 		pregunta1Text.setText("1) " + funciones.get(0).getDescripcion());
 		TextView pregunta2Text = (TextView) rootView
 				.findViewById(R.id.pregunta2);
 		pregunta2Text.setText("2) " + funciones.get(1).getDescripcion());
 		TextView pregunta3Text = (TextView) rootView
 				.findViewById(R.id.pregunta3);
		pregunta3Text.setText("3) " + funciones.get(3).getDescripcion());
 		TextView pregunta4Text = (TextView) rootView
 				.findViewById(R.id.pregunta4);
		pregunta4Text.setText("4) " + funciones.get(4).getDescripcion());
 	}
 
 	private boolean seCompletoEvaluacion() {
 		for (Respuesta respuesta : respuestas) {
 			if (!seEvaluo(respuesta.getPuntaje())) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	private boolean seEvaluo(int puntaje) {
 		return puntaje > 1 ? true : false;
 	}
 
 	private void obtenerEvaluacionPostulante() {
 		if (ConnectionManager.connect(getActivity())) {
 			String request = Servicio.ObtenerEvaluacionTerceraFase
 					+ "?idOfertaLaboral=" + oferta.getID();
 			new ObtencionEvaluacion().execute(request);
 		} else {
 			ErrorServicio.mostrarErrorConexion(getActivity());
 		}
 	}
 
 	public class ObtencionEvaluacion extends AsyncCall {
 		@Override
 		protected void onPostExecute(String result) {
 			System.out.println("Recibido: " + result.toString());
 			try {
 				JSONObject jsonObject = new JSONObject(result);
 				String respuesta = jsonObject.getString("success");
 				if (procesaRespuesta(respuesta)) {
 					JSONObject datosObject = (JSONObject) jsonObject
 							.get("data");
 					JSONArray funcionesListObject = (JSONArray) datosObject
 							.get("funciones");
 					funciones = new ArrayList<Funcion>();
 					for (int i = 0; i < funcionesListObject.length(); ++i) {
 						JSONObject funcionObject = funcionesListObject
 								.getJSONObject(i);
 						Funcion funcion = new Funcion();
 						funcion.setID(funcionObject.getInt("ID"));
 						funcion.setDescripcion(funcionObject
								.getString("Descripcion"));
 						funcion.setPuestoID(funcionObject.getString("PuestoID"));
 						funciones.add(funcion);
 					}
 					agregarFuncionesMock();
 					prepararRespuestasYEvaluacion();
 					mostrarEvaluacion();
 				}
 			} catch (JSONException e) {
 				ErrorServicio.mostrarErrorComunicacion(e.toString(),
 						getActivity());
 			} catch (NullPointerException ex) {
 				ErrorServicio.mostrarErrorComunicacion(ex.toString(),
 						getActivity());
 			}
 		}
 	}
 
 	private void agregarFuncionesMock() {
 		String puestoID = funciones.get(0).getPuestoID();
 		Funcion funcion1 = new Funcion(28,
 				"Desarrollar proyecciones de variables por campaa y anual.",
 				puestoID);
 		Funcion funcion2 = new Funcion(
 				29,
 				"Desarrollar informes y presentaciones requeridos por la corporacin, para talleres y eventos.",
 				puestoID);
 		Funcion funcion3 = new Funcion(30,
 				"Desarrollar proyecciones de variables por campaa y anual.",
 				puestoID);
 		Funcion funcion4 = new Funcion(
 				31,
 				"Desarrollar el informe de cobertura / rentabilidad de zonas por campaa.",
 				puestoID);
		Funcion funcion5 = new Funcion(
				32,
				"Realizar anlisis que soporte el diagnstico de desempeo de tcticas comerciales del pas.",
				puestoID);
 		funciones.add(funcion1);
 		funciones.add(funcion2);
 		funciones.add(funcion3);
 		funciones.add(funcion4);
		funciones.add(funcion5);
 	}
 
 	public void prepararRespuestasYEvaluacion() {
 		respuestas = new ArrayList<Respuesta>();
 		for (Funcion funcion : funciones) {
 			Respuesta respuesta = new Respuesta();
 			respuesta.setFuncionID(funcion.getID());
 			respuesta.setPuntaje(0);
 			respuestas.add(respuesta);
 		}
 
 		evaluacion = new Evaluacion();
 		Date actual = new Date(System.currentTimeMillis());
 		SimpleDateFormat formatoFecha = new SimpleDateFormat();
 		formatoFecha.applyPattern("dd/MM/yyyy");
 		evaluacion.setFechaInicio(formatoFecha.format(actual));
 
 		totalPaginas = funciones.size() / PREGUNTAS_X_PAGINA;
 	}
 
 	public boolean procesaRespuesta(String respuestaServidor) {
 		if (ConstanteServicio.SERVICIO_OK.equals(respuestaServidor)) {
 			return true;
 		} else if (ConstanteServicio.SERVICIO_ERROR.equals(respuestaServidor)) {
 			// Se muestra mensaje de servicio no disponible
 			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 			builder.setTitle("Servicio no disponible");
 			builder.setMessage("No se pudo obtener la evaluacin. Intente nuevamente");
 			builder.setCancelable(false);
 			builder.setPositiveButton("Ok", null);
 			builder.create();
 			builder.show();
 			return false;
 		} else {
 			// Se muestra mensaje de la respuesta invalida del servidor
 			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 			builder.setTitle("Problema en el servidor");
 			builder.setMessage("Hay un problema en el servidor.");
 			builder.setCancelable(false);
 			builder.setPositiveButton("Ok", null);
 			builder.create();
 			builder.show();
 			return false;
 		}
 	}
 }
