 package ar.com.trabajos.broker.client;
 
 
 import java.util.HashMap;
 
 import ar.com.trabajos.broker.client.event.DatosPersonaEvent;
 import ar.com.trabajos.broker.client.event.DatosPersonaEventHandler;
 import ar.com.trabajos.broker.client.event.PedirCotizacionEvent;
 import ar.com.trabajos.broker.client.event.PedirCotizacionEventHandler;
 import ar.com.trabajos.broker.client.presenter.FormAutoPresenter;
 import ar.com.trabajos.broker.client.presenter.FormPersonaPresenter;
 import ar.com.trabajos.broker.client.presenter.Presenter;
 import ar.com.trabajos.broker.client.presenter.ResultPresenter;
 import ar.com.trabajos.broker.client.view.FormAutoViewImpl;
 import ar.com.trabajos.broker.client.view.FormPersonaViewImpl;
 import ar.com.trabajos.broker.client.view.ResultViewImpl;
 import ar.com.trabajos.broker.shared.entities.RequestDTO;
 import ar.com.trabajos.broker.shared.entities.ResponseDTO;
 import ar.com.trabajos.broker.shared.enums.BrokerEnum;
 import ar.com.trabajos.broker.shared.enums.DatosAutoEnum;
 import ar.com.trabajos.broker.shared.enums.DatosPersonaEnum;
 import ar.com.trabajos.broker.shared.enums.ViewsEnum;
 
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HasWidgets;
 import com.google.gwt.user.client.ui.RootPanel;
 
 public class AppController implements Presenter, ValueChangeHandler<String> {
 	private final HandlerManager eventBus;
 	private final BrokerServiceAsync rpcService; 
 	private HasWidgets container;
 	private FormPersonaViewImpl formPersonaView=null;
 	private FormAutoViewImpl formAutoView=null;
 	private ResultViewImpl resultView=null;
 	private HashMap<DatosPersonaEnum, String> datosPersona= null;
 	private HashMap<DatosAutoEnum, String> datosAuto= null;
 	private HashMap<BrokerEnum, ResponseDTO> respuestas= null;
 	private ResultPresenter resultPresenter=null;
 
 	public AppController(BrokerServiceAsync rpcService, HandlerManager eventBus) {
 		this.eventBus = eventBus;
 		this.rpcService = rpcService;
 		bind();
 	}
 
 	private void bind() {
 		History.addValueChangeHandler(this);
 
 		eventBus.addHandler(DatosPersonaEvent.TYPE, new DatosPersonaEventHandler() {
 
 			@Override
 			public void onDatosCompletados(DatosPersonaEvent event) {
 				datosPersona=event.getVariables();
 				History.newItem("datosauto");
 			}
 		});
 
 		eventBus.addHandler(PedirCotizacionEvent.TYPE, new PedirCotizacionEventHandler() {
 
 			@Override
 			public void onPedirCotizacion(PedirCotizacionEvent event) {
 				datosAuto=event.getDataAuto();
 				History.newItem("cotizacion");
 			}
 
 		});
 
 	}
 
 
 	public void onValueChange(ValueChangeEvent<String> event) {
 		String token = event.getValue();
 
 		if (token != null) {
 			Presenter presenter = null;
 
 			if (token.equals("datospersona")) {
 				if (formPersonaView==null)
 					formPersonaView= new FormPersonaViewImpl();
 				presenter = new FormPersonaPresenter(rpcService, eventBus, formPersonaView);
 				prepararMenu(ViewsEnum.DATOS_PERSONA);
 
 			}else if (token.equals("datosauto")) {
 				if (datosPersona!=null){
 
 					if (formAutoView==null)
 						formAutoView= new FormAutoViewImpl();
 					presenter = new FormAutoPresenter(rpcService, eventBus, formAutoView, datosPersona);
 					prepararMenu(ViewsEnum.DATOS_AUTO);
 				}else{
 					History.newItem("datospersona");
 				}
 			}else if (token.equals("cotizacion")) {
 				//TODO REMOVER ESTO
				
 				if (datosAuto==null && datosPersona==null){
 					testDatosPersona();
 					testDatosAuto();
 				}
				
 				//FIN REMOVER
 				
 				//Esto va
 				if (datosPersona==null)
 					History.newItem("datospersona");
 				else if (datosAuto==null)
 					History.newItem("datosAuto");
 				else{
 					prepararMenu(ViewsEnum.COTIZACION);
 					if (resultView==null)
 						resultView= new ResultViewImpl();
 					resultPresenter = new ResultPresenter(rpcService, eventBus, resultView, datosPersona, datosAuto);
 					resultPresenter.go(container);
 					pedirCotizaciones();
 				}
 
 			}
 
 			if (presenter != null) {
 				presenter.go(container);
 			}
 		}
 	}
 
 	private void testDatosAuto() {
 		datosAuto= new HashMap<DatosAutoEnum, String>();
 		datosAuto.put(DatosAutoEnum.MARCA, "17");
 		datosAuto.put(DatosAutoEnum.MARCA_STRING, "FIAT");
 		datosAuto.put(DatosAutoEnum.COD_INFOAUTO, "17389");
 		datosAuto.put(DatosAutoEnum.MODELO_AUTO_STRING, "Fiat Uno Fire 3p");
 		datosAuto.put(DatosAutoEnum.MODELO, "2008");
 		datosAuto.put(DatosAutoEnum.SUMA_ASEGURADA, "35000");
 		datosAuto.put(DatosAutoEnum.TIPO_USO, "Particular");
 	}
 
 	private void testDatosPersona() {
 		datosPersona= new HashMap<DatosPersonaEnum, String>();
 		datosPersona.put(DatosPersonaEnum.NOMBRE_APELLIDO, "Federico");
 		datosPersona.put(DatosPersonaEnum.EMPRESA, "Testing");
 		datosPersona.put(DatosPersonaEnum.CORREO, "federicoaw@gmail.com");
 		datosPersona.put(DatosPersonaEnum.TELEFONO, "478779512");
 		datosPersona.put(DatosPersonaEnum.EDAD, "26");
 		datosPersona.put(DatosPersonaEnum.PROVINCIA, "2");
 		datosPersona.put(DatosPersonaEnum.PROVINCIA_STRING, "Buenos Aires");
 		datosPersona.put(DatosPersonaEnum.CODIGO_POSTAL, "1636");
 	}
 
 	private void prepararMenu(ViewsEnum vista) {
 		switch (vista) {
 		case DATOS_PERSONA:
 			RootPanel.get("menuPersona").addStyleName("active");
 			RootPanel.get("menuAuto").removeStyleName("active");
 			RootPanel.get("menuCotizacion").removeStyleName("active");
 			break;
 		case DATOS_AUTO:
 			RootPanel.get("menuAuto").addStyleName("active");
 			RootPanel.get("menuPersona").removeStyleName("active");
 			RootPanel.get("menuCotizacion").removeStyleName("active");
 			break;
 		case COTIZACION:
 			RootPanel.get("menuCotizacion").addStyleName("active");
 			RootPanel.get("menuPersona").removeStyleName("active");
 			RootPanel.get("menuAuto").removeStyleName("active");
 			break;
 
 		default:
 			break;
 		}
 
 
 	}
 
 	@Override
 	public void go(HasWidgets container) {
 		this.container = container;
 		if ("".equals(History.getToken())) {
 			History.newItem("datospersona");
 		}
 		else {
 			History.fireCurrentHistoryState();
 		}
 	} 
 
 	private void recibirCotizacion(ResponseDTO dto) {
 		resultPresenter.agregarCotizacion(dto);		
 		if (respuestas==null)
 			respuestas=new HashMap<BrokerEnum, ResponseDTO>();
 		respuestas.put(dto.getBroker(), dto);
 		boolean todos = true;
 		for (BrokerEnum broker : BrokerEnum.values()) {
 			if (respuestas.get(broker)==null)
 				todos=false;			
 		}
 		if (todos){
 			enviarMail();
 			respuestas.clear();
 		}
 	}
 
 
 	private void enviarMail() {
 		rpcService.enviarMail(respuestas, datosAuto, datosPersona, new AsyncCallback<Void>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				
 			}
 
 			@Override
 			public void onSuccess(Void result) {
 				
 			}
 			
 		});
 	}
 
 	private void pedirCotizaciones() {
 		RequestDTO request= new RequestDTO();
 		
 //		request.setMarca(datosAuto.get(DatosAutoEnum.MARCA));
 		request.setModelo(datosAuto.get(DatosAutoEnum.MODELO));
 		request.setCodInfoAuto(datosAuto.get(DatosAutoEnum.COD_INFOAUTO));
 		request.setTipoUso(datosAuto.get(DatosAutoEnum.TIPO_USO));			
 		request.setSumaAsegurada(datosAuto.get(DatosAutoEnum.SUMA_ASEGURADA));
 		
 		request.setProvincia(Integer.parseInt(datosPersona.get(DatosPersonaEnum.PROVINCIA)));
 		request.setCodigoPostal(datosPersona.get(DatosPersonaEnum.CODIGO_POSTAL));
 		request.setEdad(datosPersona.get(DatosPersonaEnum.EDAD));
 		
 		if (resultView==null)
 			resultView= new ResultViewImpl();
 		resultPresenter = new ResultPresenter(rpcService, eventBus, resultView, datosPersona, datosAuto);
 		resultPresenter.go(container);
 
 		History.newItem("cotizacion");
 
 		
 
 		this.guardarCotizacion(request);
 		this.enviarPedidoCotizacion(request, BrokerEnum.ELCOMERCIO);
 		this.enviarPedidoCotizacion(request, BrokerEnum.LIBERTY);
 		this.enviarPedidoCotizacion(request, BrokerEnum.NACION);
 		this.enviarPedidoCotizacion(request, BrokerEnum.ALLIANZ);
 
 
 	}
 
 	private void guardarCotizacion(RequestDTO request) {
 		request.setTelefono(datosPersona.get(DatosPersonaEnum.TELEFONO));
 		request.setCorreo(datosPersona.get(DatosPersonaEnum.CORREO));
 		request.setNombreApellido(datosPersona.get(DatosPersonaEnum.NOMBRE_APELLIDO));
 		request.setEmpresa(datosPersona.get(DatosPersonaEnum.EMPRESA));
 		rpcService.guardarDatosRequest(request, new AsyncCallback<Void>() {
 
 			@Override
 			public void onSuccess(Void result) {
 
 			}
 
 			@Override
 			public void onFailure(Throwable caught) {
 				System.out.println("Ha ocurrido un error en el envio para el guardado del request.");				
 			}
 		});
 	}
 
 	private void enviarPedidoCotizacion(RequestDTO request, final BrokerEnum broker) {
 		request.setBroker(broker);
 		rpcService.requestPresupuesto(request, new AsyncCallback<ResponseDTO>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				ResponseDTO result = new ResponseDTO();
 				result.setBroker(broker);
 				result.setError("No se pudo obtener la cotización.");
 				recibirCotizacion(result);
 			}
 
 			@Override
 			public void onSuccess(ResponseDTO result) {
 				recibirCotizacion(result);
 			}
 		});
 
 	}
 }
