 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package scheduler;
 
 import DAO.DAOFactory;
 import DAO.interfaces.NotificacionDAO;
 import DAO.interfaces.PuntoLimpioDAO;
 import DAO.interfaces.AdministradorDAO;
 import DAO.interfaces.ConfiguracionDAO;
 import entities.Contenedor;
 import entities.Notificacion;
 import entities.PuntoLimpio;
 import entities.Administrador;
 import entities.Configuracion;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.logging.Level;
 import javax.ejb.Stateless;
 import java.util.logging.Logger;
 import javax.annotation.Resource;
 import javax.ejb.EJB;
 import javax.ejb.Schedule;
 import javax.ejb.ScheduleExpression;
 import javax.ejb.Timer;
 import javax.ejb.Timeout;
 import javax.ejb.TimerConfig;
 import javax.ejb.TimerService;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import scheduler.algoritmosCalculo.AlgoritmoCalculo;
 import scheduler.algoritmosCalculo.FactoryAlgoritmosCalculo;
 import sessionBeans.CrudPuntoLimpioLocal;
 
 
 /**
  *
  * @author Armando
  */
 @Stateless
 public class GeneradorProgramadoNotificaciones implements GeneradorProgramadoNotificacionesLocal {
     @EJB
     private CrudPuntoLimpioLocal crudPuntoLimpio;
     
     @PersistenceContext(unitName = "coplime-ejbPU")
     private EntityManager em;
     
     @Resource
     TimerService servicioTemporizador;
     
     //Variables de control del temporizador
    private Long milisegsIntervaloOriginal=new Long (10000), milisegsIntervalo= new Long(10000);//para detectar cambios en la configuración del intervalo
    //private static final int DIA_EN_MILISEGS = 86400000;
    private static final int DIA_EN_MILISEGS = 120000;
     
     //TODO Cambiar por Programatic Timers (buscar en la documentación)
     //@Schedule(minute = "0", second = "0", dayOfMonth = "*", month = "*", year = "*", hour = "01")
     @Timeout
     @Override
     //NOTA: Los parámetros de periodicidad de la tarea programada deben ser configurables externamente
     //a través de la aplicación.
     public void calculoLlenadoAutomatico() {
         //Inicialización de las variables
         Collection<PuntoLimpio> listaPuntosLimpios;
         boolean resultadoOperacion;
         
         // TODO: Borrar esta línea
         System.out.println("Timer event [" + new Date() + "]: Generando notificaciones automáticas.");
         
         //Le pedimos al sistema que nos entregue los puntos limpios existentes
         listaPuntosLimpios = crudPuntoLimpio.getAllPuntosLimpios();
         /* Con la lista de puntos limpios pedimos la aplicación del algoritmo
          * y si es necesario la generación de notificaciones automáticas.
          */
         resultadoOperacion = evaluar(listaPuntosLimpios);
         if (resultadoOperacion == true){
             Logger.getLogger(GeneradorProgramadoNotificaciones.class.getName()).log(Level.FINER, "Generación de notificaciones automáticas finalizada exitosamente.");
             //Actualizamos la fecha/hora de última ejecución
             DAOFactory fabricaDAO = DAOFactory.getDAOFactory(DAOFactory.JPA, em);
             ConfiguracionDAO configDAO = fabricaDAO.getConfiguracionDAO();
             Configuracion fechaUltimaEjecucionEstimador = new Configuracion();
             fechaUltimaEjecucionEstimador.setIdParam("timer_estimacion_contenedores_ultima_ejecucion");
             Calendar cal = Calendar.getInstance();
             fechaUltimaEjecucionEstimador.setValorParam(Long.toString(cal.getTimeInMillis()));
         } else {
             Logger.getLogger(GeneradorProgramadoNotificaciones.class.getName()).log(Level.WARNING, "Error en generación de notificaciones automáticas.");
         }
     }
         
      /**
      * Dada una lista de puntos limpios procede a aplicar un algoritmo
      * de estimación de fecha de llenado de cada contenedor para cada punto limpio en la
      * lista, refrescando las fechas de próxima revisión del punto limpio en la base
      * de datos a través de la interfaz DAO correspondiente. Además, si la fecha de
      * próxima revisión corresponde al día de mañana, genera la notificación asociada.
      * 
      * Si un contenedor está lleno, la fecha de revisión es fijada para HOY.
      * Si un contenedor NO tiene puntos en su historial, se genera una notificación al Administrador.
      * @param puntosLimpiosEvaluar: listado con los puntos limpios a los cuales se le estimará la fecha de próxima revisión.
      * @return True cuando la operación es exitosa, False si existieron errores.
      */
     private boolean evaluar(Collection<PuntoLimpio> puntosLimpiosEvaluar){
         AlgoritmoCalculo ac = FactoryAlgoritmosCalculo.getAlgoritmoCalculo(FactoryAlgoritmosCalculo.PROMEDIO_PENDIENTES);
         Date fechaRevision,menorFechaRevision=null;
         for (PuntoLimpio p:puntosLimpiosEvaluar){
             for (Contenedor c: p.getContenedores()){
                 //Se establece como fecha de revisión aquella para la cual se estima que el contenedor C
                 //alcanzará el porcentaje establecido.
                 fechaRevision = ac.estimar(c.getHistorialContenedor(), 100);
                 /* Cazando alerta de contenedor sin puntos en el historial:
                  * Si la fecha de revisión está fijada para el día de AYER, generar notificaciones respectivas.
                  */
                 Calendar calendario = Calendar.getInstance();
                 calendario.add(Calendar.DAY_OF_MONTH, -1);
                 if(fechaRevision.equals(calendario.getTime())){//¿Es la fecha de revisión "Ayer"?
                     generarNotificacionContenedorSinHistorial(p, c);
                     String stringAviso = "Contenedor ID ".concat(c.getId().toString());
                     stringAviso = stringAviso.concat(", Material ").concat(c.getMaterialDeAcopio().getNombre_material().toString());
                     stringAviso = stringAviso.concat(" perteneciente al punto limpio ").concat(p.getNombre());
                     stringAviso = stringAviso.concat(" no contiene registros en su historial de llenado.");
                     Logger.getLogger(GeneradorProgramadoNotificaciones.class.getName()).log(Level.WARNING, stringAviso);
                     //Volvemos al día de "hoy" sumando 1 al valor de la variable calendario
                     calendario.add(Calendar.DAY_OF_MONTH, 1);
                     //Se corrige el valor de la fecha de revisión de "ayer" a "hoy"
                     fechaRevision = calendario.getTime();//Hoy
                 }
                 
                 /* Se establece la fecha más próxima para tener que revisar el punto limpio
                  * Si no hay fechas, se escoge la primera.
                  * Si hay fechas, se compara con la menor actual.
                  */
         
                 if (menorFechaRevision==null){
                     menorFechaRevision = fechaRevision;
                 } else {
                     if(fechaRevision.before(menorFechaRevision)){
                         menorFechaRevision = fechaRevision;
                     }
                 }
             }
             actualizarFechaRevisionPL(p, menorFechaRevision);
             
             Calendar magnana = Calendar.getInstance();
             magnana.add(Calendar.DATE, 2);
             Calendar menorFechaRevisionCalendar = Calendar.getInstance();
             menorFechaRevisionCalendar.setTime(menorFechaRevision);
             if(menorFechaRevision.before(magnana.getTime())){
                 generarNotificacionRevisionPL(menorFechaRevisionCalendar, p);
             }
             //Es necesario resetear la menor fecha de revisión para un nuevo punto limpio
             menorFechaRevision = null;
             
         }
         
         //@TODO cazar excepciones para retornar false en casos de fallo.
         return true;
     }
     
     /**
      * Establece una nueva fecha de revisión para un punto limpio P
      * @param p : Punto limpio cuya fecha de revisión se modificará
      * @param nuevaFecha : Nueva fecha de revisión
      */
     
     private void actualizarFechaRevisionPL(PuntoLimpio p,Date nuevaFecha){
         DAOFactory fabricaDAO = DAOFactory.getDAOFactory(DAOFactory.JPA, em);
         PuntoLimpioDAO ptosLimpiosDAO = fabricaDAO.getPuntoLimpioDAO();
         Calendar cal = Calendar.getInstance();
         cal.setTime(nuevaFecha);
         p.setFechaProxRevision(cal);
         ptosLimpiosDAO.update(p);
     }
     
     /**
      * Dada la lista de Puntos Limpios y dada la lista de resultados
      * asociados a cada Punto Limpio, le indica al sistema generar las Notificaciones correspondientes
      * según corresponda.
      * @param fechaRevision : fecha en la cual el punto limpio deberá revisarse
      * @param pl : Punto limpio asociado
      */
     
     private void generarNotificacionRevisionPL(Calendar fechaRevision, PuntoLimpio pl){
        //Nota: ¿por qué la fecha del MENSAJE de la notificación no es pl.getFechaHora()?
         DAOFactory fabricaDAO = DAOFactory.getDAOFactory(DAOFactory.JPA, em);
         NotificacionDAO notifDAO = fabricaDAO.getNotificacionDAO();
         Notificacion nuevaNotificacion = new Notificacion();
         
         nuevaNotificacion.setPuntoLimpio(pl);
         nuevaNotificacion.setFechaHora(Calendar.getInstance());
         //Se verifica que el punto limpio disponga de inspector, caso contrario el usuario a cargo
         //será cualquier administrador otorgado por la interfaz AdministradorDAO
         if(pl.getInspectorEncargado()==null){
             AdministradorDAO adminDAO = fabricaDAO.getAdministradorDAO();
             nuevaNotificacion.setUsuarioEncargado(adminDAO.findAnyAdministrador().getUsuario());
         } else {
             nuevaNotificacion.setUsuarioEncargado(pl.getInspectorEncargado().getUsuario());
         }
         nuevaNotificacion.setComentario("El punto limpio número ".concat(pl.getId().toString()).concat(" de nombre ").concat(pl.getNombre()).concat(" necesita ser revisado. Causa: probable llenado de uno de los contenedores el día ").concat(fechaRevision.toString()).concat("."));
         //TODO resolver tipo de incidencia
         //nuevaNotificacion.setTipoIncidencia(null);
         notifDAO.insert(nuevaNotificacion);        
         }
     
     private void generarNotificacionContenedorSinHistorial(PuntoLimpio pl, Contenedor c){
         DAOFactory fabricaDAO = DAOFactory.getDAOFactory(DAOFactory.JPA, em);
         NotificacionDAO notifDAO = fabricaDAO.getNotificacionDAO();
         AdministradorDAO adminDAO = fabricaDAO.getAdministradorDAO();
         
     
         Administrador admin = adminDAO.findAnyAdministrador();
         Notificacion nuevaNotificacion = new Notificacion();
         nuevaNotificacion.setFechaHora(Calendar.getInstance());
         nuevaNotificacion.setPuntoLimpio(pl);
         nuevaNotificacion.setUsuarioEncargado(admin.getUsuario());
         //TODO resolver tipo de incidencia
         //nuevaNotificacion.setTipoIncidencia(null);
         nuevaNotificacion.setComentario("Contenedor ID ".concat(c.getId().toString()).concat(" (".concat(c.getMaterialDeAcopio().toString()).concat(") perteneciente al Punto Limpio ".concat(pl.getNombre()).concat(" no tiene historial de llenado.\nPor favor, ingrese una revisión del punto limpio para generar el historial."))));
         notifDAO.insert(nuevaNotificacion);
     }
     
     
     /**
      * Programa el temporizador que determina cada cuánto tiempo se ejecuta la tarea de estimación
      * de llenado de los contenedores registrados.
      * @param milisegundosIntervaloTransicion el intervalo en milisegundos del tiempo restante del temporizador anterior más el intervalo en milisegundos en el que se ejecuta la tarea
      * @param milisegundosIntervalo el intervalo en milisegundos entre el que se ejecuta la tarea.
      */
     @Override
     public void setTemporizadorEstimacionLlenadoContenedor(Long milisegundosIntervaloTransicion, Long milisegundosIntervalo){
         //Verificamos si ya existe un timer fijado previamente
         Collection<Timer> listaTimers;
         listaTimers = (Collection<Timer>) servicioTemporizador.getTimers();
         if (listaTimers.isEmpty() == true) {
             Timer temporizador = servicioTemporizador.createIntervalTimer(milisegundosIntervalo, milisegundosIntervalo, new TimerConfig());
             Logger.getLogger(GeneradorProgramadoNotificaciones.class.getName()).log(Level.INFO, "Se ha establecido un temporizador para la estimación automática de llenado de contenedores, con un intervalo de ".concat(milisegundosIntervalo.toString()).concat(" milisegundos."));
         } else {
             //Cancelar el timer original
             for (Timer timer : listaTimers) {
                 try{
                     if("ScheduleExpression [second=0;minute=*;hour=*;dayOfMonth=*;month=*;dayOfWeek=*;year=*;timezoneID=null;start=null;end=null]".equals(timer.getSchedule().toString())){
                         System.out.println("Detectado timer asociado a scheduler, se mantiene operativo.");
                     }else{
                         System.out.println("Cancelando timer: " + timer.getSchedule());
                         timer.cancel(); 
                     }
                 }catch(IllegalStateException e){
                     System.out.println("Temporizador cancelado por excepción: ".concat(e.toString()));
                     timer.cancel();
                 }catch (Exception e){
                     System.out.println("Excepción no determinada: ".concat(e.toString()));
                     System.out.println("Por seguridad, el timer que lanza la excepción se cancelará.");
                     timer.cancel();
                 }
             }
          }
             Timer temporizador = servicioTemporizador.createIntervalTimer(milisegundosIntervaloTransicion, milisegundosIntervalo, new TimerConfig());
             Logger.getLogger(GeneradorProgramadoNotificaciones.class.getName()).log(Level.INFO, "Se ha reprogramado un temporizador para la estimación automática de llenado de contenedores, con un intervalo de ".concat(milisegundosIntervalo.toString()).concat(" milisegundos."));
         
     }
     
     /**
      * Ajusta el valor del temporizador automático de la estimación de llenado de contenedores.
      * Si no existe un valor para el temporizador automático, establece uno por defecto para 24 horas
      * 
      */
     @Schedule(hour = "*", minute = "*")
     public void ajusteTemporizadorEstimacion(){
         System.out.println("Ejecutándose tarea programada: Ajuste Temporizador Estimación.");
        //Verificamos la existencia de la configuración
         DAOFactory fabricaDAO = DAOFactory.getDAOFactory(DAOFactory.JPA, em);
         ConfiguracionDAO configDAO = fabricaDAO.getConfiguracionDAO();
         Configuracion timerConfig = configDAO.buscarParamExacto("timer_estimacion_contenedores_intervalo");
         Configuracion timerUltimaEjec = configDAO.buscarParamExacto("timer_estimacion_contenedores_ultima_ejecucion");
         
         //failsafe
         if (timerUltimaEjec == null){
             System.out.println("El generador programado de notificaciones ha establecido la fecha y hora actuales como la última ejecución del temporizador al no existir registro previo.");
             timerUltimaEjec = new Configuracion();
             timerUltimaEjec.setIdParam("timer_estimacion_contenedores_ultima_ejecucion");
             timerUltimaEjec.setValorParam(Long.toString(Calendar.getInstance().getTimeInMillis()));
             configDAO.insert(timerUltimaEjec);
         }
 
         //Se consulta si existe la configuración de temporizador, en caso contrario, se crea
         if(timerConfig == null){
             //Nunca se ha configurado el temporizador
             configDAO.update(timerUltimaEjec);
             timerConfig = new Configuracion();
             timerConfig.setIdParam("timer_estimacion_contenedores_intervalo");
             timerConfig.setValorParam(Integer.toString(DIA_EN_MILISEGS));
             configDAO.insert(timerConfig);
         }
         
         //Se verifica si ya hay un Timer funcionando, esto se verifica a través de la cantidad de timers
         //que contiene este sessionBean. Si es igual o menor a uno, entonces se crea un nuevo Timer.
         Collection<Timer> listaTimers;
         listaTimers = (Collection<Timer>) servicioTemporizador.getTimers();
         if (!(listaTimers.size()>1)) {
            //No hay más temporizadores que el programado a través de @Schedule
             this.setTemporizadorEstimacionLlenadoContenedor(Long.valueOf(DIA_EN_MILISEGS), Long.valueOf(DIA_EN_MILISEGS));
            milisegsIntervalo = Long.valueOf(DIA_EN_MILISEGS);
            milisegsIntervaloOriginal = Long.valueOf(DIA_EN_MILISEGS);
         }else{
             //Existe un temporizador ya en curso, sólo se evalúa si la configuración
             //ha cambiado el intervalo
             
             //Se obtiene el valor de intervalo
             milisegsIntervalo = Long.parseLong(timerConfig.getValorParam());
             //Se determina si existen cambios en la configuración desde la última vez
            if(milisegsIntervalo.longValue() != milisegsIntervaloOriginal.longValue()){
                System.out.println("Diferencia de configuración: ".concat(milisegsIntervaloOriginal.toString()).concat(" / ").concat(milisegsIntervalo.toString()).concat(" ."));
                System.out.println("Se renovará el Timer.");
                 /*
                  * Si la configuración ha cambiado, se determina la diferencia con la última
                  * ejecución y el tiempo restante del timer original.
                  */
                 
                 //Calculando tiempo restante del temporizador en curso
                 Calendar cal = Calendar.getInstance();
                 Long milisegsCal = cal.getTimeInMillis();
                 Long milisPendientes = milisegsCal - Long.parseLong(timerUltimaEjec.getValorParam());
                 //Long diferenciaIntervalos = milisegsIntervalo - milisegsIntervaloOriginal;
                 
                 //Se determina la próxima ejecución y se le suma un minuto
                 Long proxEje = (milisegsIntervalo % (milisPendientes))+60000;
                 
                 this.setTemporizadorEstimacionLlenadoContenedor(proxEje, milisegsIntervalo);
                 
                 //Por último, igualamos ambos valores a fin de detectar nuevos cambios
                 milisegsIntervaloOriginal = milisegsIntervalo;
         }
       }
     }
 
     
 }
