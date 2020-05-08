 package py.com.ait.gestion.view;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import javax.inject.Inject;
 
 import org.primefaces.event.DateSelectEvent;
 import org.primefaces.event.FileUploadEvent;
 import org.primefaces.model.DefaultStreamedContent;
 import org.primefaces.model.DualListModel;
 import org.primefaces.model.StreamedContent;
 import org.ticpy.tekoporu.annotation.NextView;
 import org.ticpy.tekoporu.annotation.PreviousView;
 import org.ticpy.tekoporu.stereotype.ViewController;
 import org.ticpy.tekoporu.template.AbstractListPageBean;
 
 import py.com.ait.gestion.business.ActividadBC;
 import py.com.ait.gestion.business.ActividadChecklistDetalleBC;
 import py.com.ait.gestion.business.CronogramaDetalleBC;
 import py.com.ait.gestion.business.DocumentoBC;
 import py.com.ait.gestion.business.ObservacionBC;
 import py.com.ait.gestion.business.ProcesoBC;
 import py.com.ait.gestion.business.RolBC;
 import py.com.ait.gestion.business.TipoAlarmaBC;
 import py.com.ait.gestion.business.UsuarioBC;
 import py.com.ait.gestion.constant.AppProperties;
 import py.com.ait.gestion.constant.Definiciones;
 import py.com.ait.gestion.constant.Definiciones.Estado;
 import py.com.ait.gestion.domain.Actividad;
 import py.com.ait.gestion.domain.ActividadChecklistDetalle;
 import py.com.ait.gestion.domain.CronogramaDetalle;
 import py.com.ait.gestion.domain.Documento;
 import py.com.ait.gestion.domain.Observacion;
 import py.com.ait.gestion.domain.Proceso;
 import py.com.ait.gestion.domain.Usuario;
 
 @ViewController
 @NextView("/pg/proceso_edit.xhtml")
 @PreviousView("/pg/proceso_list.xhtml")
 public class ProcesoListMB extends AbstractListPageBean<Proceso, Long> {
 
 	private static final long serialVersionUID = 1L;
 
 	@Inject
 	private AppProperties appProperties;
 
 	@Inject
 	private FacesContext facesContext;
 
 	@Inject
 	private ProcesoBC procesoBC;
 	
 	@Inject
 	private RolBC rolBC;
 
 	@Inject
 	private ObservacionBC observacionBC;
 
 	@Inject
 	private DocumentoBC documentoBC;
 
 	@Inject
 	ActividadChecklistDetalleBC actividadChecklistDetalleBC;
 
 	private List<Proceso> procesos;
 	private Proceso procesoSeleccionado;
 
 	private List<Actividad> actividades;
 	private List<Observacion> observaciones;
 	private List<Documento> documentos;
 	private List<ActividadChecklistDetalle> checklist;
 	
 	private String carpetaFileUpload;
 	private List<String> carpetas;
 
 	private String filtroEstadoProceso = "A";
 	
 	public Proceso getProcesoSeleccionado() {
 		return procesoSeleccionado;
 	}
 
 	public void setProcesoSeleccionado(Proceso procesoSeleccionado) {
 		this.procesoSeleccionado = procesoSeleccionado;
 	}
 
 	public List<Observacion> getObservaciones() {
 		return observaciones;
 	}
 
 	public void setObservacion(List<Observacion> observaciones) {
 		this.observaciones = observaciones;
 	}
 
 	public List<Documento> getDocumentos() {
 		return documentos;
 	}
 
 	public void setDocumentos(List<Documento> documentos) {
 		this.documentos = documentos;
 	}
 
 	public void elegirProceso() {
 		
 		String currentUser = FacesContext.getCurrentInstance().getExternalContext()
 				.getUserPrincipal().getName();
 		setActividades(procesoBC.getActividadesByProceso(procesoSeleccionado, currentUser));
 		// setCronogramaDetallesporCronograma(procesoSeleccionado.getCronograma().getCronogramaDetalles());
 		String numeroProceso = procesoSeleccionado.getNroProceso();
 		agregarMensaje("Proceso seleccionado: " + numeroProceso);
 	}
 
 	public void mostrarObsProceso() {
 		
 
 		this.setObservacion(observacionBC.getObsProceso(procesoSeleccionado
 				.getProcesoId()));
 
 		elegirProceso();
 	}
 
 	public void mostrarFileProceso() {
 
 		elegirProceso();
 		
 		String currentUser = FacesContext.getCurrentInstance().getExternalContext()
 				.getUserPrincipal().getName();
 		this.setDocumentos(documentoBC.getFileProceso(procesoSeleccionado
 				.getProcesoId(), currentUser));
 
 		updateCarpetas();
 		elegirProceso();
 	}
 
 	public void mostrarFileActividad() {
 
 		this.setDocumentos(documentoBC.getFileActividad(actividadSeleccionada
 				.getActividadId()));
 
 		String numeroActividad = actividadSeleccionada.getNroActividad();
 		agregarMensaje("Actividad seleccionada: " + numeroActividad);
 	}
 
 	public void mostrarChecklist() {
 
 		this.setChecklist(actividadChecklistDetalleBC.getChecklistByActividad(actividadSeleccionada));
 
 		agregarMensaje("Actividad seleccionada: " + actividadSeleccionada.getNroActividad());
 	}
 
 	public List<ActividadChecklistDetalle> getChecklist() {
 		return checklist;
 	}
 
 	public void setChecklist(List<ActividadChecklistDetalle> checklist) {
 		this.checklist = checklist;
 	}
 
 	public List<Actividad> getActividades() {
 		return actividades;
 	}
 
 	public void setActividades(List<Actividad> actividades) {
 		this.actividades = actividades;
 	}
 
 	public List<Proceso> getProcesos() {
 		String currentUser = FacesContext.getCurrentInstance().getExternalContext()
 				.getUserPrincipal().getName();		
 		procesos = procesoBC.listar(filtroEstadoProceso, currentUser);
 		return procesos;
 	}
 	
 	public void updateFiltroEstadoProceso() {
 		
 		this.actividades = null;
 		getProcesos();
 	}
 
 	public String getFiltroEstadoProceso() {
 		return filtroEstadoProceso;
 	}
 
 	public void setFiltroEstadoProceso(String filtroEstadoProceso) {
 		this.filtroEstadoProceso = filtroEstadoProceso;
 	}
 
 	public void setProcesos(List<Proceso> procesos) {
 		this.procesos = procesos;
 	}
 
 	public void eliminar(ActionEvent actionEvent) {
 		procesoBC.eliminar(procesoSeleccionado.getProcesoId());
 		procesoSeleccionado = new Proceso();
 		String currentUser = FacesContext.getCurrentInstance().getExternalContext()
 				.getUserPrincipal().getName();
 		setProcesos(procesoBC.listar(getFiltroEstadoProceso(), currentUser));
 		agregarMensaje("Proceso eliminado");
 	}
 
 	@Override
 	protected List<Proceso> handleResultList() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public void agregarMensaje(String mensaje) {
 		facesContext.addMessage("suceso", new FacesMessage(mensaje));
 	}
 
 	public void agregarMensajeError(String mensaje) {
 		facesContext.addMessage("error", new FacesMessage(
 				FacesMessage.SEVERITY_ERROR, mensaje, null));
 	}
 
 	// Actividades
 
 	@Inject
 	private ActividadBC actividadBC;
 
 	@Inject
 	private UsuarioBC usuarioBC;
 
 	@Inject
 	private CronogramaDetalleBC cronogramaDetalleBC;
 
 	@Inject
 	private TipoAlarmaBC tipoAlarmaBC;
 
 	private Long idResponsable;
 	private Long idCronogramaDetalle;
 	private Long idActividadAnterior;
 	private Long idAlarma;
 	private Long idAlerta;
 	private Long idSuperTarea;
 
 	private String nroActividad;
 	private String descripcion;
 	private Date fechaCreacion;
 	private Date fechaInicioPrevisto;
 	private Date fechaInicioReprogramado;
 	private String motivoReprogramacionInicio;
 	private Date fechaFinPrevista;
 	private Date fechaFinReprogramada;
 	private String motivoReprogramacion;
 	private Date fechaDevuelta;
 	private Date fechaResuelta;
 	private Date fechaCancelacion;
 	private String pregunta;
 	private String respuesta;
 	private String estado;
 	private String checklistCompleto;
 
 	private List<Usuario> usuariosPorRol;
 	private List<Usuario> sigteUsuariosPorRol;
 	private Usuario sigteUsuario;
 	private List<Usuario> allUsuarios;
 	private List<CronogramaDetalle> cronogramaDetallesporCronograma;
 	private List<Actividad> actividadesPorProceso;
 
 	private Actividad actividadSeleccionada;
 	private CronogramaDetalle sigteCronogramaDetalle;
 	private boolean subActividad;
 
 	public void mostrarObsActividad() {
 
 		this.setObservacion(observacionBC.getObsActividad(actividadSeleccionada
 				.getActividadId()));
 
 		String numeroActividad = actividadSeleccionada.getNroActividad();
 		agregarMensaje("Actividad seleccionada: " + numeroActividad);
 	}
 
 	public Actividad getActividadSeleccionada() {
 		return actividadSeleccionada;
 	}
 
 	public void setActividadSeleccionada(Actividad actividadSeleccionada) {
 		this.actividadSeleccionada = actividadSeleccionada;
 	}
 
 	public Long getIdResponsable() {
 		return idResponsable;
 	}
 
 	public void setIdResponsable(Long idResponsable) {
 		this.idResponsable = idResponsable;
 	}
 
 	public Long getIdCronogramaDetalle() {
 		return idCronogramaDetalle;
 	}
 
 	public void setIdCronogramaDetalle(Long idCronogramaDetalle) {
 		this.idCronogramaDetalle = idCronogramaDetalle;
 	}
 
 	public Long getIdActividadAnterior() {
 		return idActividadAnterior;
 	}
 
 	public void setIdActividadAnterior(Long idActividadAnterior) {
 		this.idActividadAnterior = idActividadAnterior;
 	}
 
 	public Long getIdAlarma() {
 		return idAlarma;
 	}
 
 	public void setIdAlarma(Long idAlarma) {
 		this.idAlarma = idAlarma;
 	}
 
 	public Long getIdAlerta() {
 		return idAlerta;
 	}
 
 	public void setIdAlerta(Long idAlerta) {
 		this.idAlerta = idAlerta;
 	}
 
 	public Long getIdSuperTarea() {
 		return idSuperTarea;
 	}
 
 	public void setIdSuperTarea(Long idSuperTarea) {
 		this.idSuperTarea = idSuperTarea;
 	}
 
 	public String getNroActividad() {
 		return nroActividad;
 	}
 
 	public void setNroActividad(String nroActividad) {
 		this.nroActividad = nroActividad;
 	}
 
 	public String getDescripcion() {
 		return descripcion;
 	}
 
 	public void setDescripcion(String descripcion) {
 		this.descripcion = descripcion;
 	}
 
 	public Date getFechaCreacion() {
 		return fechaCreacion;
 	}
 
 	public void setFechaCreacion(Date fechaCreacion) {
 		this.fechaCreacion = fechaCreacion;
 	}
 
 	public Date getFechaInicioPrevisto() {
 		return fechaInicioPrevisto;
 	}
 
 	public void setFechaInicioPrevisto(Date fechaInicioPrevisto) {
 		this.fechaInicioPrevisto = fechaInicioPrevisto;
 	}
 
 	public Date getFechaInicioReprogramado() {
 		return fechaInicioReprogramado;
 	}
 
 	public void setFechaInicioReprogramado(Date fechaInicioReprogramado) {
 		this.fechaInicioReprogramado = fechaInicioReprogramado;
 	}
 
 	public String getMotivoReprogramacionInicio() {
 		return motivoReprogramacionInicio;
 	}
 
 	public void setMotivoReprogramacionInicio(String motivoReprogramacionInicio) {
 		this.motivoReprogramacionInicio = motivoReprogramacionInicio;
 	}
 
 	public Date getFechaFinPrevista() {
 		return fechaFinPrevista;
 	}
 
 	public void setFechaFinPrevista(Date fechaFinPrevista) {
 		this.fechaFinPrevista = fechaFinPrevista;
 	}
 
 	public Date getFechaFinReprogramada() {
 		return fechaFinReprogramada;
 	}
 
 	public void setFechaFinReprogramada(Date fechaFinReprogramada) {
 		this.fechaFinReprogramada = fechaFinReprogramada;
 	}
 
 	public String getMotivoReprogramacion() {
 		return motivoReprogramacion;
 	}
 
 	public void setMotivoReprogramacion(String motivoReprogramacion) {
 		this.motivoReprogramacion = motivoReprogramacion;
 	}
 
 	public Date getFechaDevuelta() {
 		return fechaDevuelta;
 	}
 
 	public void setFechaDevuelta(Date fechaDevuelta) {
 		this.fechaDevuelta = fechaDevuelta;
 	}
 
 	public Date getFechaResuelta() {
 		return fechaResuelta;
 	}
 
 	public void setFechaResuelta(Date fechaResuelta) {
 		this.fechaResuelta = fechaResuelta;
 	}
 
 	public Date getFechaCancelacion() {
 		return fechaCancelacion;
 	}
 
 	public void setFechaCancelacion(Date fechaCancelacion) {
 		this.fechaCancelacion = fechaCancelacion;
 	}
 
 	public String getPregunta() {
 		return pregunta;
 	}
 
 	public void setPregunta(String pregunta) {
 		this.pregunta = pregunta;
 	}
 
 	public String getRespuesta() {
 		return respuesta;
 	}
 
 	public void setRespuesta(String respuesta) {
 		this.respuesta = respuesta;
 	}
 
 	public String getEstado() {
 		return estado;
 	}
 
 	public void setEstado(String estado) {
 		this.estado = estado;
 	}
 
 	public String getChecklistCompleto() {
 		return checklistCompleto;
 	}
 
 	public void setChecklistCompleto(String checklistCompleto) {
 		this.checklistCompleto = checklistCompleto;
 	}
 
 	public List<Usuario> getUsuariosPorRol() {
 
 		if (actividadSeleccionada != null) {
 
 			if (actividadSeleccionada.getCronogramaDetalle() == null
 					&& actividadSeleccionada.getSuperTarea() != null) {
 				// Es una subActividad, listar todos los usuarios posibles.
 				usuariosPorRol = usuarioBC.findAll();
 			} else {
 				// Listar los usuarios de acuerdo al rol del cronogramaDetalle
 				usuariosPorRol = usuarioBC
 						.getUsuariosByRol(actividadSeleccionada
 								.getCronogramaDetalle().getRolResponsable()
 								.getRolId());
 			}
 		}
 
 		return usuariosPorRol;
 	}
 
 	public void nextActividadUsuariosPorRol() {
 
 		if (actividadSeleccionada != null) {
 			// Obtener siguiente cronograma detalle para filtrar posibles responsables
 			CronogramaDetalle cd = cronogramaDetalleBC
 					.getNextCronogramaDetalle(
 							actividadSeleccionada.getCronogramaDetalle(),
 							actividadSeleccionada.getRespuesta());
 
 			// Listar los usuarios de acuerdo al rol del cronogramaDetalle
 			// siguiente
 			setSigteUsuariosPorRol(usuarioBC.getUsuariosByRol(cd.getRolResponsable()
 					.getRolId()));
 		}
 
 	}
 
 	public List<Usuario> getSigteUsuariosPorRol() {
 		return sigteUsuariosPorRol;
 	}
 
 	public void setSigteUsuariosPorRol(List<Usuario> sigteUsuariosPorRol) {
 		this.sigteUsuariosPorRol = sigteUsuariosPorRol;
 	}
 
 	public Usuario getSigteUsuario() {
 		return sigteUsuario;
 	}
 
 	public void setSigteUsuario(Usuario sigteUsuario) {
 		this.sigteUsuario = sigteUsuario;
 	}
 
 	public List<Usuario> getAllUsuarios() {
 
 		if (actividadSeleccionada != null) {
 
 			// Listar todos los usuarios posibles.
 			allUsuarios = usuarioBC.findAll();
 		}
 
 		return allUsuarios;
 	}
 
 	public void setUsuariosPorRol(List<Usuario> usuariosPorRol) {
 		this.usuariosPorRol = usuariosPorRol;
 	}
 
 	public List<CronogramaDetalle> getCronogramaDetallesporCronograma() {
 		cronogramaDetallesporCronograma = cronogramaDetalleBC.listar();
 		return cronogramaDetallesporCronograma;
 	}
 
 	public void setCronogramaDetallesporCronograma(
 			List<CronogramaDetalle> cronogramaDetallesporCronograma) {
 		this.cronogramaDetallesporCronograma = cronogramaDetallesporCronograma;
 	}
 
 	public List<Actividad> getActividadesPorProceso() {
 		actividadesPorProceso = actividadBC.listar();
 		return actividadesPorProceso;
 	}
 
 	public void setActividadesPorProceso(List<Actividad> actividadesPorProceso) {
 		this.actividadesPorProceso = actividadesPorProceso;
 	}
 
 	private Usuario getResponsable() {
 
 		return usuarioBC.load(getIdResponsable());
 	}
 
 	private CronogramaDetalle getCronogramaDetalle() {
 
 		return cronogramaDetalleBC.load(getIdCronogramaDetalle());
 	}
 
 	private Actividad getActividadAnterior() {
 
 		return actividadBC.load(getIdActividadAnterior());
 	}
 
 	private Actividad getSuperTarea() {
 
 		return actividadBC.load(getIdSuperTarea());
 	}
 
 	public void registrarActividad() {
 		if (procesoSeleccionado == null) {
 			agregarMensaje("ERROR: Proceso NO seleccionado");
 			limpiarCamposNuevo();
 		} else {
 			Proceso procesoSelec = procesoSeleccionado;
 
 			Actividad actividad = new Actividad();
 
 			actividad.setNroActividad(getNroActividad());
 			actividad.setDescripcion(getDescripcion());
 			actividad.setFechaCreacion(getFechaCreacion());
 			actividad.setFechaInicioPrevisto(getFechaInicioPrevisto());
 			actividad.setFechaInicioReprogramado(getFechaInicioReprogramado());
 			actividad
 					.setMotivoReprogramacionInicio(getMotivoReprogramacionInicio());
 			actividad.setFechaFinPrevista(getFechaFinPrevista());
 			actividad.setFechaFinReprogramada(getFechaFinReprogramada());
 			actividad.setMotivoReprogramacion(getMotivoReprogramacion());
 			actividad.setFechaDevuelta(getFechaDevuelta());
 			actividad.setFechaResuelta(getFechaResuelta());
 			actividad.setFechaCancelacion(getFechaCancelacion());
 			actividad.setPregunta(getPregunta());
 			actividad.setRespuesta(getRespuesta());
 			actividad.setEstado(getEstado());
 			actividad.setChecklistCompleto(getChecklistCompleto());
 
 			actividad.setResponsable(getResponsable());
 			actividad.setCronogramaDetalle(getCronogramaDetalle());
 			actividad.setActividadAnterior(getActividadAnterior());
 			actividad.setSuperTarea(getSuperTarea());
 
 			/*
 			 * actividad.setAlarma(getCronogramaDetalle().getAlarma());
 			 * actividad.setAlerta(getCronogramaDetalle().getAlerta());
 			 */
 			actividad.setMaster(procesoSelec);
 			actividadBC.registrar(actividad);
 			actividades.add(actividad);
 			agregarMensaje("Actividad creada");
 			limpiarCamposNuevo();
 
 		}
 	}
 
 	private void limpiarCamposNuevo() {
 		this.setNroActividad("");
 		this.setDescripcion("");
 		this.setFechaCreacion(null);
 		this.setFechaInicioPrevisto(null);
 		this.setFechaInicioReprogramado(null);
 		this.setMotivoReprogramacionInicio("");
 		this.setFechaFinPrevista(null);
 		this.setFechaFinReprogramada(null);
 		this.setMotivoReprogramacion("");
 		this.setFechaDevuelta(null);
 		this.setFechaResuelta(null);
 		this.setFechaCancelacion(null);
 		this.setPregunta("");
 		this.setRespuesta("");
 		this.setEstado("");
 		this.setChecklistCompleto("");
 
 	}
 
 	public void eliminarActividad(ActionEvent actionEvent) {
 		actividadBC.eliminar(actividadSeleccionada.getActividadId());
 		int index = actividades.indexOf(actividadSeleccionada);
 		actividades.remove(index);
 		// detalleSeleccionado = new CronogramaDetalle();
 
 		agregarMensaje("Actividad eliminada");
 	}
 
 	public void elegirActividad() {
 		Actividad actividad = actividadSeleccionada;
 
 		if (actividadSeleccionada.getCronogramaDetalle() != null){
 			setSigteCronogramaDetalle(cronogramaDetalleBC.getNextCronogramaDetalle(
 					actividadSeleccionada.getCronogramaDetalle(), actividadSeleccionada.getRespuesta()));
 			setSubActividad(false);
 		}else {
 			setSubActividad(true);
 		}
 
 		agregarMensaje("Actividad seleccionada: " + actividad.getNroActividad());
 
 	}
 
 	public void elegirChecklistDetalle() {
 		ActividadChecklistDetalle actividadCheklistDetalle = this.checklistDetalle;
 
 		agregarMensaje("Item de Checklist seleccionado: " + actividadCheklistDetalle.getDescripcion());
 
 	}
 
 	public void editarActividad() {
 		if (actividadSeleccionada == null) {
 			agregarMensaje("Actividad no seleccionada");
 		} else {
 			Actividad actividad = actividadSeleccionada;
 			if (actividad.getResponsable() != null) {
 				actividad.setResponsable(usuarioBC.load(actividad
 						.getResponsable().getUsuarioId()));
 			}
 			try{
 				actividadBC.editar(actividad);
 				agregarMensaje("Actividad editada");
 			} catch(Exception ex){
 				agregarMensajeError(ex.getMessage());
 			}
 		}
 	}
 
 	public List<Estado> getEstadosActividad() {
 
 		return Definiciones.EstadoActividad.getEstadosActividad();
 	}
 
 	public List<Estado> getEstadosSubActividad() {
 
 		return Definiciones.EstadoActividad.getEstadosSubActividad();
 	}
 
 	public List<Estado> getSiNoList() {
 
 		return Definiciones.getSiNoList();
 	}
 
 	public Long getActividadSeleccionadaResponsable() {
 
 		Long usuario = null;
 		if (actividadSeleccionada != null
 				&& actividadSeleccionada.getResponsable() != null)
 			usuario = actividadSeleccionada.getResponsable().getUsuarioId();
 
 		return usuario;
 	}
 
 	public Date calculoFechaFin(DateSelectEvent event) {
 
 		Calendar cal = new GregorianCalendar();
 		cal.setTime((Date) event.getDate());
 		cal.add(Calendar.DATE, actividadSeleccionada.getCronogramaDetalle()
 				.getDuracionTarea().intValue());
 		actividadSeleccionada.setFechaFinPrevista(cal.getTime());
 		return cal.getTime();
 	}
 
 	public CronogramaDetalle getSigteCronogramaDetalle() {
 		return sigteCronogramaDetalle;
 	}
 
 	public void setSigteCronogramaDetalle(CronogramaDetalle sigteCronogramaDetalle) {
 		this.sigteCronogramaDetalle = sigteCronogramaDetalle;
 	}
 	public boolean isSubActividad() {
 		return subActividad;
 	}
 
 	public void setSubActividad(boolean subActividad) {
 		this.subActividad = subActividad;
 	}
 
 	public void resolverActividad() {
 		if (actividadSeleccionada == null) {
 			agregarMensaje("Actividad no seleccionada");
 		} else {
 			Actividad actividad = actividadSeleccionada;
 			if (!validarSiPuedeResolverActividad()){
				elegirProceso();
 				return;
 			}
 
 			try {
 				actividadBC.resolveActividad(actividad,
 						getSigteUsuario());
 				if (actividad.getChecklistDetalle() != null) {
 					actividad.setTieneChecklist(true);
 				}
 				elegirProceso();
 				registrarObsP();
 				agregarMensaje("Ha pasado a la siguiente Actividad");
 			} catch (RuntimeException ex) {
 				ex.printStackTrace();
 				agregarMensajeError(ex.getMessage());
 			}
 			if (actividad.getResponsable() != null) {
 				actividad.setResponsable(usuarioBC.load(actividad
 						.getResponsable().getUsuarioId()));
 			}
 		}
 	}
 
 	/**
 	 * @return
 	 */
 	private boolean validarSiPuedeResolverActividad() {
 		Actividad actividad = actividadSeleccionada;
 		boolean aret = true;
 		if (actividad.getPregunta() != null
 				&& !actividad.getPregunta().equals("")
 				&& (actividad.getRespuesta() == null || actividad
 						.getRespuesta().equals(""))) {
 
 			String mensaje = "No se puede resolver una actividad con pregunta y sin respuesta.";
 			System.out.println("validarSiPuedeResolverActividad() " + mensaje);
 			agregarMensajeError(mensaje);
 			aret = false;
 
 		} else if (actividad.getRespuesta() != null
 				&& !actividad.getRespuesta().equals("SI")
 				&& !actividad.getRespuesta().equals("NO")) {
 
 			String mensaje = "La respuesta debe ser SI o NO.";
 			System.out.println("validarSiPuedeResolverActividad() " + mensaje);
 			agregarMensajeError(mensaje);
 			aret = false;
 
 		} else if (actividadBC.validarChecklistDetalles(actividad) == false) {
 
 			String mensaje = "No puede pasar a la siguiente actividad sin cumplir con todo el checklist.";
 			System.out.println("validarSiPuedeResolverActividad() " + mensaje);
 			agregarMensajeError(mensaje);
 			aret = false;
 
 		} else if (actividadBC.existenFacturasSinCobro(actividad.getMaster())){
 
 			String mensaje = "No puede pasar a la siguiente actividad con Factura y sin fecha de cobro.";
 			System.out.println("validarSiPuedeResolverActividad() " + mensaje);
 			agregarMensajeError(mensaje);
 			aret = false;
 
 		} else if  (procesoBC.existenSubTareasAbiertas(actividad)){
 
 			String mensaje = "No puede pasar a la siguiente actividad con subtareas no administrativas abiertas!";
 			System.out.println("validarSiPuedeResolverActividad() " + mensaje);
 			agregarMensajeError(mensaje);
 			aret = false;
 
 		}
 		return aret;
 	}
 
 	public void finalizarProceso() {
 		if (actividadSeleccionada == null) {
 			agregarMensaje("Actividad no seleccionada");
 		} else {
 			try {
 				Actividad actividad = actividadSeleccionada;
 				if (actividad.getResponsable() != null) {
 					actividad.setResponsable(usuarioBC.load(actividad
 							.getResponsable().getUsuarioId()));
 				}
 				actividadBC.resolveActividad(actividad,
 						getSigteUsuario());
 				elegirProceso();
 				agregarMensaje("Ha finalizado el Proceso");
 			} catch (RuntimeException ex) {
 				ex.printStackTrace();
 				agregarMensajeError(ex.getMessage());
 			}
 		}
 	}
 
 	public void devolverActividad() {
 		if (actividadSeleccionada == null) {
 			agregarMensaje("Actividad no seleccionada");
 		} else {
 			try {
 				if (!validarSiPuedeDevolverActividad()){
 					return;
 				}
 
 				Actividad actividad = actividadSeleccionada;
 				if (actividad.getResponsable() != null) {
 					actividad.setResponsable(usuarioBC.load(actividad
 							.getResponsable().getUsuarioId()));
 				}
 				actividadBC.devolverActividad(actividad);
 				elegirProceso();
 				agregarMensaje("Actividad devuelta");
 			} catch (RuntimeException ex) {
 				ex.printStackTrace();
 				agregarMensajeError(ex.getMessage());
 			}
 		}
 	}
 
 	/**
 	 * @return
 	 */
 	private boolean validarSiPuedeDevolverActividad() {
 		Actividad actividad = actividadSeleccionada;
 		boolean aret = true;
 		if  (procesoBC.existenSubTareas(actividad)){
 
 			String mensaje = "No puede devolver la actividad si cuenta con subtareas!";
 			System.out.println("validarSiPuedeResolverActividad() " + mensaje);
 			agregarMensajeError(mensaje);
 			aret = false;
 
 		}
 		return aret;
 	}
 
 	public void crearSubActividad() {
 		if (actividadSeleccionada == null) {
 			agregarMensaje("Actividad no seleccionada");
 		} else {
 			try {
 				Actividad actividad = actividadSeleccionada;
 				if (actividad.getResponsable() != null) {
 					actividad.setResponsable(usuarioBC.load(actividad
 							.getResponsable().getUsuarioId()));
 				}
 				actividadBC.crearSubActividad(actividad,
 						actividadSeleccionada.getDescripcion(),
 						actividadSeleccionada.getResponsable(), null, null);
 				elegirProceso();
 				agregarMensaje("SubActividad creada");
 			} catch (RuntimeException ex) {
 				ex.printStackTrace();
 				agregarMensajeError(ex.getMessage());
 			}
 		}
 	}
 
 	public boolean getMostrarCampoRespuesta() {
 
 		boolean show = false;
 		if (actividadSeleccionada != null) {
 
 			if (actividadSeleccionada.getPregunta() != null
 					&& !actividadSeleccionada.getPregunta().equals("")) {
 
 				show = true;
 			}
 		}
 		return show;
 	}
 
 	// Observaciones
 
 	private String descripcionObsP;
 	private String descripcionObsA;
 
 	public String getDescripcionObsA() {
 		return descripcionObsA;
 	}
 
 	public void setDescripcionObsA(String descripcionObsA) {
 		this.descripcionObsA = descripcionObsA;
 	}
 
 	private Observacion observacionSeleccionada;
 
 	private Documento documentoSeleccionado;
 
 	private ActividadChecklistDetalle checklistDetalle;
 
 	public Documento getDocumentoSeleccionado() {
 		return documentoSeleccionado;
 	}
 
 	public void setDocumentoSeleccionado(Documento documentoSeleccionado) {
 		this.documentoSeleccionado = documentoSeleccionado;
 	}
 
 	public ActividadChecklistDetalle getChecklistDetalle() {
 		return checklistDetalle;
 	}
 
 	public void setChecklistDetalle(ActividadChecklistDetalle checklistDetalle) {
 		this.checklistDetalle = checklistDetalle;
 	}
 
 	public String getDescripcionObsP() {
 		return descripcionObsP;
 	}
 
 	public Observacion getObservacionSeleccionada() {
 		return observacionSeleccionada;
 	}
 
 	public void setObservacionSeleccionada(Observacion observacionSeleccionada) {
 		this.observacionSeleccionada = observacionSeleccionada;
 	}
 
 	public void setDescripcionObsP(String descripcion) {
 		this.descripcionObsP = descripcion;
 	}
 
 	public void registrarObsP() {
 		if (procesoSeleccionado == null) {
 			agregarMensaje("ERROR: Proceso NO seleccionado");
 			this.setDescripcionObsP("");
 		} else {
 			Proceso procesoSelec = procesoSeleccionado;
 
 			Observacion obs = new Observacion();
 
 			obs.setDescripcion(getDescripcionObsP());
 
 			String nombreUsu = usuarioBC.getUsuarioActual();
 			Usuario actual = usuarioBC.findSpecificUser(nombreUsu);
 
 			obs.setUsuario(actual);
 			obs.setFechaHora(new Date());
 			obs.setEntidad("Proceso");
 			obs.setIdEntidad(procesoSelec.getProcesoId());
 
 			observacionBC.registrar(obs);
 			if (observaciones != null){
 				List<Observacion> nuevasObservaciones = new ArrayList<Observacion>();
 				nuevasObservaciones.add(obs);
 				nuevasObservaciones.addAll(observaciones);
 				observaciones = nuevasObservaciones;
 			}
 			agregarMensaje("Observacion creada");
 			this.setDescripcionObsP("");
 
 		}
 	}
 
 	public void registrarObsA() {
 		if (actividadSeleccionada == null) {
 			agregarMensaje("ERROR: Actividad NO seleccionada");
 			this.setDescripcionObsP("");
 		} else {
 			Actividad actividadSelec = actividadSeleccionada;
 
 			Observacion obs = new Observacion();
 
 			obs.setDescripcion(getDescripcionObsA());
 
 			String nombreUsu = usuarioBC.getUsuarioActual();
 			Usuario actual = usuarioBC.findSpecificUser(nombreUsu);
 
 			obs.setUsuario(actual);
 			obs.setFechaHora(new Date());
 			obs.setEntidad("Actividad");
 			obs.setIdEntidad(actividadSelec.getActividadId());
 
 			observacionBC.registrar(obs);
 			observaciones.add(obs);
 			agregarMensaje("Observacion creada");
 			this.setDescripcionObsA("");
 
 		}
 	}
 
 	public void elegirObservacion() {
 		Observacion obs = observacionSeleccionada;
 
 		agregarMensaje("Observacion seleccionada: " + obs.getDescripcion());
 
 	}
 
 	public void editarChecklistDetalle() {
 		if (checklistDetalle == null) {
 			agregarMensaje("Item de Checklist no seleccionado");
 		} else {
 			ActividadChecklistDetalle edited = checklistDetalle;
 			actividadChecklistDetalleBC.editar(edited);
 			agregarMensaje("Item de Checklist editado");
 		}
 	}
 
 	public void editarObservacion() {
 		if (observacionSeleccionada == null) {
 			agregarMensaje("Observacion no seleccionada");
 		} else {
 			String actual = usuarioBC.getUsuarioActual();
 			String obsUsu = observacionSeleccionada.getUsuario().getUsuario();
 			if (obsUsu.equals(actual)) {
 				Observacion obs = observacionSeleccionada;
 				obs.setFechaHora(new Date());
 				observacionBC.editar(obs);
 				agregarMensaje("Observacion editada");
 
 			} else {
 				agregarMensaje("No tiene permisos para realizar esta operación");
 			}
 		}
 	}
 
 	public void eliminarObservacion(ActionEvent actionEvent) {
 
 		if (observacionSeleccionada == null) {
 			agregarMensaje("Observacion no seleccionada");
 		} else {
 			//String actual = usuarioBC.getUsuarioActual();
 			//String obsUsu = observacionSeleccionada.getUsuario().getUsuario();
 			//if (obsUsu.equals(actual)) {
 
 				observacionBC.eliminar(observacionSeleccionada
 						.getObservacionId());
 				int index = observaciones.indexOf(observacionSeleccionada);
 				observaciones.remove(index);
 
 				agregarMensaje("Observacion eliminada");
 
 			//} 
 			//else {
 			//	agregarMensaje("No tiene permisos para realizar esta operación");
 			//}
 		}
 
 	}
 
 	// Archivos
 
 	public void handleFileUpload(FileUploadEvent event) {
 
 		// agregarMensaje("Success! " + event.getFile().getFileName() +
 		// " is uploaded.");
 		// Do what you want with the file
 		try {
 			copyFile(event.getFile().getFileName(), event.getFile()
 					.getInputstream());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void copyFile(String fileName, InputStream in) {
 		if (procesoSeleccionado == null) {
 			agregarMensaje("ERROR: Proceso NO seleccionado");
 		} else {
 
 			try {
 				
 				String currentUser = FacesContext.getCurrentInstance().getExternalContext()
 						.getUserPrincipal().getName();
 				Usuario usuarioActual = usuarioBC.findSpecificUser(currentUser);
 				
 				//get folder name
 				String folder = this.carpetaFileUpload;
 				if(folder == null) folder = "";
 				else if(folder.equals("---Sin carpeta---")) folder = "";
 				else if(folder.trim().equals("")) folder = "";
 				else folder += '/';
 				
 				String nombreCliente = procesoSeleccionado.getCliente()
 						.getNombre();
 				String descCronog = procesoSeleccionado.getCronograma()
 						.getNombre();
 				String nroProceso = procesoSeleccionado.getNroProceso();
 				String anho = "";
 				
 
 				int k = nroProceso.lastIndexOf('/');
 				if (k > 0) {
 					anho = nroProceso.substring(k - 4, k);
 					nroProceso = nroProceso.substring(k + 1);
 				}
 				
 				String extension = "";
 				String nombreArchivo = "";
 				int i = fileName.lastIndexOf('.');
 				if (i > 0) {
 					extension = fileName.substring(i + 1);
 					nombreArchivo = fileName.substring(0, i);
 				}
 				
 				String filePath = appProperties.getDocumentPath()
 						+ nombreCliente + '/' + anho + '/' + descCronog + '/'
 						+ nroProceso + '/' + folder;
 				
 				Documento documentoOrig = documentoBC.getDocumentoByFileName(nombreArchivo, filePath, extension);				
 				boolean puedoVer = documentoBC.puedoVer(documentoOrig, usuarioActual);				
 				if(!puedoVer){
 					
 					//error, el archivo ya existe y el usuario actual no posee privilegios para verlo
 					agregarMensajeError("Error, no posee privilegios sobre el archivo: " + fileName + " !!");
 				} else if(documentoOrig != null && documentoOrig.getBloqueado().equals("Si")
 					&& !documentoOrig.getUsuarioBloqueo().getUsuario().equals(currentUser)) {
 					
 					//error, el archivoya existe y está bloqueado por otro usuario
 					agregarMensajeError("Error, el archivo: " + fileName + " está bloqueado para edición!!\n" +
 										"Usuario que bloquéo: " + documentoOrig.getUsuarioBloqueo().getUsuario() + "\n" +
 										"Fecha de bloqueo: " + documentoOrig.getFechaBloqueo().toString());
 				} else {
 
 					//copiar el archivo
 					File rootFolder = new File(appProperties.getDocumentPath());
 					if (!rootFolder.exists()) {
 						rootFolder.mkdir();
 					}
 	
 					File clienteFolder = new File(appProperties.getDocumentPath()
 							+ nombreCliente + '/');
 					if (!clienteFolder.exists()) {
 						clienteFolder.mkdir();
 					}
 	
 					File anhoFolder = new File(appProperties.getDocumentPath()
 							+ nombreCliente + '/' + anho + '/');
 					if (!anhoFolder.exists()) {
 						anhoFolder.mkdir();
 					}
 	
 					File cronogFolder = new File(appProperties.getDocumentPath()
 							+ nombreCliente + '/' + anho + '/' + descCronog + '/');
 					if (!cronogFolder.exists()) {
 						cronogFolder.mkdir();
 					}
 	
 					File procesoFolder = new File(appProperties.getDocumentPath()
 							+ nombreCliente + '/' + anho + '/' + descCronog + '/'
 							+ nroProceso + '/');
 					if (!procesoFolder.exists()) {
 						procesoFolder.mkdir();
 					}
 					
 					File procesoSubFolder = new File(appProperties.getDocumentPath() + nombreCliente + '/'
 							+ anho + '/' + descCronog + '/' + nroProceso + '/' + folder);
 					if (!procesoSubFolder.exists()) {
 						procesoSubFolder.mkdir();
 					}
 					
 					OutputStream out = new FileOutputStream(new File(filePath + fileName));
 					int read = 0;
 					byte[] bytes = new byte[1024];
 	
 					while ((read = in.read(bytes)) != -1) {
 						out.write(bytes, 0, read);
 					}
 	
 					in.close();
 					out.flush();
 					out.close();					
 	
 					Proceso procesoSelec = procesoSeleccionado;
 					if(documentoOrig == null) {
 						
 						//nuevo documento, insertar						
 						Documento doc = new Documento();
 						doc.setFilename(nombreArchivo);
 						doc.setFileExtension(extension);
 						doc.setBloqueado("No");
 						doc.setFilepath(filePath);
 						doc.setEntidad("Proceso");
 						doc.setIdEntidad(procesoSelec.getProcesoId());
 						doc.setFechaUltimoUpdate(new Date());						
 						doc.setUsuarioCreacion(usuarioActual);
 		
 						documentoBC.registrar(doc);
 						documentos.add(doc);
 						agregarMensaje("Archivo subido correctamente");
 					} else {
 						
 						//documento ya existente, actualizar
 						documentoOrig.setFechaUltimoUpdate(new Date());
 						documentoBC.editar(documentoOrig);
 						documentos = documentoBC.getFileProceso(procesoSeleccionado.getProcesoId(), currentUser);
 						agregarMensaje("Archivo subido y actualizado correctamente");
 					}					
 				}
 
 			} catch (IOException e) {
 				agregarMensaje("Error subiendo el archivo");
 				// System.out.println(e.getMessage());
 			}
 		}
 	}
 
 	public void elegirDocumento() {
 		Documento doc = documentoSeleccionado;
 
 		try {
 			String downloadPath = doc.getFilepath();
 			String downloadName = doc.getFilename();
 			String downloadExt = doc.getFileExtension();
 
 			InputStream stream = new FileInputStream(downloadPath
 					+ downloadName + "." + downloadExt);
 			StreamedContent archivo = new DefaultStreamedContent(stream,
 					"application/octet-stream", downloadName + "."
 							+ downloadExt);
 			setFile(archivo);
 			agregarMensaje("Archivo seleccionado :" + downloadName + "."
 					+ downloadExt);
 			
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			agregarMensaje("Error seleccionando el archivo");
 		}
 
 	}
 
 	private StreamedContent file;
 
 	public void setFile(StreamedContent file) {
 
 		this.file = file;
 	}
 
 	public StreamedContent getFile() {
 
 		return file;
 	}
 
 	public void handleFileUploadA(FileUploadEvent event) {
 
 		// agregarMensaje("Success! " + event.getFile().getFileName() +
 		// " is uploaded.");
 		// Do what you want with the file
 		try {
 			copyFileA(event.getFile().getFileName(), event.getFile()
 					.getInputstream());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void copyFileA(String fileName, InputStream in) {
 		if (actividadSeleccionada == null) {
 			agregarMensaje("ERROR: Actividad NO seleccionada");
 		} else {
 
 			try {
 
 				String nombreCliente = procesoSeleccionado.getCliente()
 						.getNombre();
 				String descCronog = procesoSeleccionado.getCronograma()
 						.getNombre();
 				String nroProceso = procesoSeleccionado.getNroProceso();
 				String anho = "";
 				String nroActiv = actividadSeleccionada.getNroActividad();
 
 				int k = nroProceso.lastIndexOf('/');
 				if (k > 0) {
 					anho = nroProceso.substring(k + 1);
 					nroProceso = nroProceso.substring(0, k);
 				}
 
 				int h = nroActiv.lastIndexOf('/');
 				if (h > 0) {
 					nroActiv = nroActiv.substring(0, h);
 				}
 
 				File rootFolder = new File(appProperties.getDocumentPath());
 				if (!rootFolder.exists()) {
 					rootFolder.mkdir();
 				}
 
 				File clienteFolder = new File(appProperties.getDocumentPath()
 						+ nombreCliente + '/');
 				if (!clienteFolder.exists()) {
 					clienteFolder.mkdir();
 				}
 
 				File anhoFolder = new File(appProperties.getDocumentPath()
 						+ nombreCliente + '/' + anho + '/');
 				if (!anhoFolder.exists()) {
 					anhoFolder.mkdir();
 				}
 
 				File cronogFolder = new File(appProperties.getDocumentPath()
 						+ nombreCliente + '/' + anho + '/' + descCronog + '/');
 				if (!cronogFolder.exists()) {
 					cronogFolder.mkdir();
 				}
 
 				File procesoFolder = new File(appProperties.getDocumentPath()
 						+ nombreCliente + '/' + anho + '/' + descCronog + '/'
 						+ nroProceso + '/');
 				if (!procesoFolder.exists()) {
 					procesoFolder.mkdir();
 				}
 
 				File activFolder = new File(appProperties.getDocumentPath()
 						+ nombreCliente + '/' + anho + '/' + descCronog + '/'
 						+ nroProceso + '/' + nroActiv + '/');
 				if (!activFolder.exists()) {
 					activFolder.mkdir();
 				}
 
 				// write the inputStream to a FileOutputStream
 				OutputStream out = new FileOutputStream(new File(
 						appProperties.getDocumentPath() + nombreCliente + '/'
 								+ anho + '/' + descCronog + '/' + nroProceso
 								+ '/' + nroActiv + '/' + fileName));
 
 				int read = 0;
 				byte[] bytes = new byte[1024];
 
 				while ((read = in.read(bytes)) != -1) {
 					out.write(bytes, 0, read);
 				}
 
 				in.close();
 				out.flush();
 				out.close();
 
 				String extension = "";
 				String nombreArchivo = "";
 
 				int i = fileName.lastIndexOf('.');
 				if (i > 0) {
 					extension = fileName.substring(i + 1);
 					nombreArchivo = fileName.substring(0, i);
 				}
 
 				Actividad activSelec = actividadSeleccionada;
 
 				Documento doc = new Documento();
 
 				doc.setFilename(nombreArchivo);
 				doc.setFileExtension(extension);
 				doc.setBloqueado("No");
 				doc.setFechaBloqueo(new Date());
 				doc.setFechaDesbloqueo(new Date());
 				doc.setFilepath(appProperties.getDocumentPath() + nombreCliente
 						+ '/' + anho + '/' + descCronog + '/' + nroProceso
 						+ '/' + nroActiv + '/');
 
 				String nombreUsu = usuarioBC.getUsuarioActual();
 				Usuario actual = usuarioBC.findSpecificUser(nombreUsu);
 				doc.setUsuarioBloqueo(actual);
 				doc.setUsuarioDesbloqueo(actual);
 
 				doc.setEntidad("Actividad");
 				doc.setIdEntidad(activSelec.getActividadId());
 
 				documentoBC.registrar(doc);
 				documentos.add(doc);
 				agregarMensaje("Archivo subido");
 
 			} catch (IOException e) {
 				agregarMensaje("Error subiendo el archivo");
 				// System.out.println(e.getMessage());
 			}
 		}
 	}
 
 	public void eliminarDocumento(ActionEvent actionEvent) {
 
 		try {
 			if (documentoSeleccionado == null) {
 				agregarMensaje("Documento no seleccionado");
 			} else {
 				documentoBC.eliminar(documentoSeleccionado.getDocumentoId());
 				int index = documentos.indexOf(documentoSeleccionado);
 				documentos.remove(index);
 
 				agregarMensaje("Documento eliminado");
 
 			}
 		} catch (RuntimeException ex) {
 			ex.printStackTrace();
 			agregarMensajeError(ex.getMessage());
 		}
 	}
 	
 	public boolean getIsAdminUser() {
 		 
 		String currentUser = FacesContext.getCurrentInstance().getExternalContext()
 									.getUserPrincipal().getName();
 		
 		return usuarioBC.isAdminUser(currentUser);
 	}
 	
 	public Long getUsuarioId() {
 		 
 		String currentUser = FacesContext.getCurrentInstance().getExternalContext()
 									.getUserPrincipal().getName();
 		Usuario usuario = usuarioBC.findSpecificUser(currentUser);		
 		return usuario.getUsuarioId();
 	}
 	
 	public boolean getCanCreateProcess() {
 		 
 		String currentUser = FacesContext.getCurrentInstance().getExternalContext()
 									.getUserPrincipal().getName();
 		
 		return procesoBC.canCreateProcess(currentUser);
 	}
 	
 	public boolean getCanControlFactura() {
 		 
 		String currentUser = FacesContext.getCurrentInstance().getExternalContext()
 									.getUserPrincipal().getName();
 		
 		return procesoBC.canControlFactura(currentUser);
 	}
 	
 	public String getCarpetaFileUpload() {
 		
 		return this.carpetaFileUpload;
 	}
 	
 	public void setCarpetaFileUpload(String carpetaFileUpload) {
 		
 		this.carpetaFileUpload = carpetaFileUpload;
 	}
 	
 	public void updateCarpetas() {
 		
 		this.carpetas = null;
 		this.carpetaFileUpload = "";
 		if(procesoSeleccionado != null) {
 			
 			this.carpetas = procesoBC.getCarpetas(procesoSeleccionado);
 		}
 	}
 	
 	public List<String> getCarpetas() {
 		
 		return this.carpetas;
 	}
 	
 	/*
 	 * Manejo de roles para documentos 
 	 */	
 	//Lista dual para el pick list
 	private DualListModel<String> listaDual;
 	
 	public DualListModel<String> getListaDual() {
 		
 		listaDual = new DualListModel<String>();
 		if(documentoSeleccionado != null) {
 			List<String> documentoRoles = procesoBC.getDocumentoRoles(documentoSeleccionado.getDocumentoId());
 			List<String> rolesFiltrado = rolBC.getRolesFiltradosAsString(documentoRoles);
 			listaDual = new DualListModel<String>(rolesFiltrado, documentoRoles);
 		}
 		return listaDual;
 	}
 
 	public void setListaDual(DualListModel<String> listaDual) {
 		this.listaDual = listaDual;
 	}
 	
 	//Manejo de roles
 	public void guardarRoles() {
 		
 		try {
 			
 			if(documentoSeleccionado != null) {
 				List<String> roles = this.listaDual.getTarget();
 				procesoBC.guardarRoles(documentoSeleccionado, roles);
 				agregarMensaje("Cambios guardados correctamente");
 			}
 		} catch(RuntimeException ex) {
 			
 			agregarMensajeError(ex.getMessage());
 		}
 	}
 	
 	public void bloquearDocumento() {
 		
 		if(documentoSeleccionado != null) {
 			
 			documentoBC.updateBloqueoDocumento(documentoSeleccionado, true);
 			//String currentUser = FacesContext.getCurrentInstance().getExternalContext()
 			//		.getUserPrincipal().getName();
 			//this.setDocumentos(documentoBC.getFileProceso(procesoSeleccionado
 			//		.getProcesoId(), currentUser));
 			agregarMensaje("Documento: " + documentoSeleccionado.getFilename() + " bloqueado correctamente");
 		}
 	}
 
 	public void desbloquearDocumento() {
 		
 		if(documentoSeleccionado != null) {
 			
 			documentoBC.updateBloqueoDocumento(documentoSeleccionado, false);
 			//String currentUser = FacesContext.getCurrentInstance().getExternalContext()
 			//		.getUserPrincipal().getName();
 			//this.setDocumentos(documentoBC.getFileProceso(procesoSeleccionado
 			//		.getProcesoId(), currentUser));
 			agregarMensaje("Documento: " + documentoSeleccionado.getFilename() + " desbloqueado correctamente");
 		}
 	}	
 	
 }
