 package com.spontecorp.ferreasesor.controller.encuesta;
 
 import com.spontecorp.ferreasesor.entity.Encuesta;
 import com.spontecorp.ferreasesor.entity.Pregunta;
 import com.spontecorp.ferreasesor.entity.RespuestaConf;
 import com.spontecorp.ferreasesor.entity.RespuestaObtenida;
 import com.spontecorp.ferreasesor.jpa.EncuestaFacade;
 import com.spontecorp.ferreasesor.jpa.PreguntaFacade;
 import com.spontecorp.ferreasesor.jpa.RespuestaConfFacade;
 import com.spontecorp.ferreasesor.jpa.RespuestaObtenidaFacade;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @ManagedBean(name = "fillEncuestaBean")
 @ViewScoped
 public class FillEncuestaBeanController implements Serializable {
 
     private RespuestaConf respuesta;
     private String preguntaTexto;
     private String promptPreguntaTextual;
     private String respuestaTexto;
     private String respuestaNumeric;
     private String preguntaSeleccion;
     private String preguntaSeleccionItem;
     private List<String> preguntaSeleccionValores;
     private List<Pregunta> preguntaList = null;
     //private List<String> respuestaList = new ArrayList();
     //  private String[] respuestaList;
     private String respuestaList[] = new String[25];
     private List<RespuestaConf> opcionsList = null;
     private Integer respuestaSeleccion;
     private Integer respuestaRating;
     private int tipoPregunta;
     private Encuesta encuesta;
     @EJB
     private PreguntaFacade preguntaFacade;
     @EJB
     private RespuestaConfFacade respuestaFacade;
     @EJB
     private EncuestaFacade encuestaFacade;
     @EJB
     private RespuestaObtenidaFacade respObtenidaFacade;
     private static final int ENCUESTA_ACTIVA = 1;
     //Tipos de Preguntas
     private static final int TEXTUAL = 1;
     private static final int NUMERICO = 2;
     private static final int SELECCION = 3;
     private static final int CALIFICACION = 4;
     private boolean showMessageResp = false;
     private static final Logger logger = LoggerFactory.getLogger(PreguntaBeanController.class);
 
     public FillEncuestaBeanController() {
     }
 
     private PreguntaFacade getPreguntaFacade() {
         return preguntaFacade;
     }
 
     private RespuestaConfFacade getRespuestaFacade() {
         return respuestaFacade;
     }
 
     public EncuestaFacade getEncuestaFacade() {
         return encuestaFacade;
     }
 
     public RespuestaObtenidaFacade getRespObtenidaFacade() {
         return respObtenidaFacade;
     }
 
     public String getRespuestaTexto() {
         return respuestaTexto;
     }
 
     public String[] getRespuestaList() {
         return respuestaList;
     }
 
     public void setRespuestaTexto(String respuestaTexto) {
         this.respuestaTexto = respuestaTexto;
     }
 
     public String getPreguntaTexto() {
         return preguntaTexto;
     }
 
     public void setPreguntaTexto(String preguntaTexto) {
         this.preguntaTexto = preguntaTexto;
     }
 
     public int getTipoPregunta() {
         return tipoPregunta;
     }
 
     public void setTipoPregunta(int tipoPregunta) {
         this.tipoPregunta = tipoPregunta;
     }
 
     public String getPromptPreguntaTextual() {
         return promptPreguntaTextual;
     }
 
     public void setPromptPreguntaTextual(String promptPreguntaTextual) {
         this.promptPreguntaTextual = promptPreguntaTextual;
     }
 
     public String getRespuestaNumeric() {
         return respuestaNumeric;
     }
 
     public void setRespuestaNumeric(String respuestaNumeric) {
         this.respuestaNumeric = respuestaNumeric;
     }
 
     public String getPreguntaSeleccionItem() {
         return preguntaSeleccionItem;
     }
 
     public void setPreguntaSeleccionItem(String preguntaSeleccionItem) {
         this.preguntaSeleccionItem = preguntaSeleccionItem;
     }
 
     public List<String> getPreguntaSeleccionValores() {
         return preguntaSeleccionValores;
     }
 
     public void setPreguntaSeleccionValores(List<String> preguntaSeleccionValores) {
         this.preguntaSeleccionValores = preguntaSeleccionValores;
     }
 
     public String getPreguntaSeleccion() {
         return preguntaSeleccion;
     }
 
     public void setPreguntaSeleccion(String preguntaSeleccion) {
         this.preguntaSeleccion = preguntaSeleccion;
     }
 
     public Integer getRespuestaSeleccion() {
         return respuestaSeleccion;
     }
 
     public void setRespuestaSeleccion(Integer respuestaSeleccion) {
         this.respuestaSeleccion = respuestaSeleccion;
     }
 
     public Integer getRespuestaRating() {
         return respuestaRating;
     }
 
     public void setRespuestaRating(Integer respuestaRating) {
         this.respuestaRating = respuestaRating;
     }
 
     public Encuesta getEncuestaActiva() {
         encuesta = getEncuestaFacade().findAll(ENCUESTA_ACTIVA);
         return encuesta;
     }
 
     public Encuesta getEncuesta() {
         return encuesta;
     }
 
     public void setEncuesta(Encuesta encuesta) {
         this.encuesta = encuesta;
     }
 
     /**
      * Listado de Preguntas
      *
      * @return
      */
     public List<Pregunta> getPreguntaList() {
         encuesta = getEncuestaActiva();
         preguntaList = null;
         opcionsList = null;
 
         if (encuesta != null && preguntaList == null && opcionsList == null) {
             preguntaSeleccionValores = new ArrayList();
             preguntaList = getPreguntaFacade().findAll(encuesta);
             for (Pregunta pregunta : preguntaList) {
                 opcionsList = getRespuestaFacade().findRespuestaConf(pregunta);
                 pregunta.setRespuestaConfList(opcionsList);
             }
         }
 
         return preguntaList;
 
     }
 
     /**
      * Metodo para Guardar Respuesta por Preguntas
      *
      * @param actionEvent
      * @return
      */
     public String guardaRespuesta() {
         
         int counter = 0;
 
         for (int i = 0; i < preguntaList.size(); i++) {
 
             if (preguntaList.get(i).getTipo() == TEXTUAL) {
                 if (respuestaList[i].toString().length() > 0) {
                     respuestaTexto = respuestaList[i];
                 } else {
                     respuestaTexto = null;
                 }
             } else {
                 if (respuestaList[i] != null) {
                     respuestaTexto = respuestaList[i];
                 } else {
                     respuestaTexto = null;
                 }
             }
             //Si la pregunta es de Tipo Calificación y la Respuesta
             //es igual a cero, no se gurada en la BD
             if (preguntaList.get(i).getTipo() == CALIFICACION) {
                 if(respuestaList[i].equals("0")){
                      respuestaTexto = null;
                 }
             }
 
             RespuestaObtenida respObtenida = new RespuestaObtenida();
 
             if (respuestaTexto != null) {
                 if (preguntaList.get(i).getTipo() == SELECCION) {
                     respuesta = getRespuestaFacade().find(Integer.valueOf(respuestaTexto));
                     respObtenida.setRespuesta(null);
                 } else {
                     respuesta = preguntaList.get(i).getRespuestaConfList().get(0);
                     respObtenida.setRespuesta(respuestaTexto);
                 }
 
                 respObtenida.setRespuestaConfId(respuesta);
                 respObtenida.setEncuestaId(encuesta);
                 respObtenida.setPreguntaId(preguntaList.get(i));
                 respObtenidaFacade.create(respObtenida);
                 counter++;
             }
         }
         
         for (int i = 0; i < preguntaList.size(); i++) {
             respuestaList[i] = "";
         }
 
         setRespuestaTexto(null);
         return showMessage(counter);
         //JsfUtil.addSuccessMessage("La Encuesta fue enviada con éxito!");
         //return "message?faces-redirect=true";
     }
 
     public String showMessage(int counter) {
         if(counter > 0){
            return "/message?faces-redirect=true";
         }else{
             showMessageResp = true;
            return "/main?faces-redirect=true";
         }   
     }
 
 }
 
