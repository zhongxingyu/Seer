 package pe.edu.pucp.proyectorh.miinformacion;
 
 import java.text.SimpleDateFormat;
 import java.util.Collections;
 import java.util.Comparator;
 
 import pe.edu.pucp.proyectorh.R;
 import pe.edu.pucp.proyectorh.model.Colaborador;
 import pe.edu.pucp.proyectorh.model.Evento;
 import android.annotation.SuppressLint;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 @SuppressLint({ "ValidFragment", "ValidFragment", "ValidFragment" })
 public class EventoFragment extends Fragment {
 
 	private View rootView;
 	private Evento evento;
 
 	public EventoFragment(Evento evento) {
 		this.evento = evento;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		rootView = inflater.inflate(R.layout.evento_layout, container, false);
 		mostrarEvento();
 		return rootView;
 	}
 
 	private void mostrarEvento() {
 		SimpleDateFormat formatoFecha = new SimpleDateFormat();
 		formatoFecha.applyPattern("dd/MM/yyyy");
 
 		TextView nombreEventoText = (TextView) rootView
 				.findViewById(R.id.nombre_evento);
 		nombreEventoText.setText(evento.getNombre());
 		TextView tipoEventoText = (TextView) rootView
 				.findViewById(R.id.tipo_evento_content);
 		tipoEventoText.setText(evento.getTipo());
 		TextView fechaInicioText = (TextView) rootView
 				.findViewById(R.id.fecha_inicio_content);
 		fechaInicioText.setText(evento.getFechaInicio());
 		TextView fechaFinText = (TextView) rootView
 				.findViewById(R.id.fecha_fin_content);
 		fechaFinText.setText(evento.getFechaFin());
 		TextView creadorEventoText = (TextView) rootView
 				.findViewById(R.id.creador_evento_content);
		creadorEventoText.setText(evento.getCreador().getNombres()
				+ evento.getCreador().getApellidos());
 
 		ListView listaInvitados = (ListView) rootView
 				.findViewById(R.id.lista_invitados_evento);
 		Collections.sort(evento.getInvitados(), new Comparator<Colaborador>() {
 
 			@Override
 			public int compare(Colaborador colaborador1,
 					Colaborador colaborador2) {
 				return colaborador1.toString().compareTo(
 						colaborador2.toString());
 
 			}
 		});
 		final ArrayAdapter<Colaborador> invitadosAdapter = new ArrayAdapter<Colaborador>(
 				this.getActivity(), android.R.layout.simple_list_item_1,
 				evento.getInvitados());
 		listaInvitados.setAdapter(invitadosAdapter);
 	}
 }
