 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.abada.eva.action.dimex.sepsis;
 
 import ca.uhn.hl7v2.HL7Exception;
 import ca.uhn.hl7v2.model.v25.group.ORU_R01_OBSERVATION;
 import ca.uhn.hl7v2.model.v25.group.ORU_R01_ORDER_OBSERVATION;
 import ca.uhn.hl7v2.model.v25.message.ORU_R01;
 import ca.uhn.hl7v2.model.v25.segment.OBX;
 import com.abada.eva.action.dimex.AbstractDimexUpdateListener;
 import es.sacyl.eva.beans.CDABean;
 import es.sacyl.eva.beans.CodificacionBean;
 import es.sacyl.eva.beans.DatoBean;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  *
  * @author jesus
  */
 public class SevereSepsisUpdateListener extends AbstractDimexUpdateListener<SevereSepsis>{
     
     private String url;
     private Map<String, String> symptoms;
     private List<String> dimexValues;
     
 
     public SevereSepsisUpdateListener() {
         super();
     }
 
     private void addORUValues(ORU_R01Custom oru, Map<String, Object> values) {
      
         Map<String,Object> oruv = oru.getSymptons();
         
         for(String v : dimexValues){
             
             if(oruv.containsKey(v)){
                 values.put(v, oruv.get(v));
             }
             
         }
         
     }
     
     @Override
     protected Map<String, Object> getData(Object[] oldMessages, Object[] newMessages) {
 
         Map<String, Object> values = new HashMap<String, Object>();
 
         for (Object event : newMessages) {
             if (event instanceof ORU_R01Custom) {
                values.putAll(((ORU_R01Custom) event).getSymptons());
             }else if (event instanceof SimpleSepsis) {
                 values.put(SepsisConstants.NHC, ((SimpleSepsis)event).getNhc());
             } else if (event instanceof CDABean) {
                 this.addCDAValues(values, (CDABean) event);
             }
         }
         
         values.put(SepsisConstants.SIMPLE_SEPSIS, true);
         
         return values;
     }
 
     @Override
     protected URI getUrl(Map<String, Object> data) {
         try {
             return new URI(this.url);
         } catch (URISyntaxException ex) {
         }
         return null;
     }
 
     public void setUrl(String url) {
         this.url = url;
     }
 
 
 
     private void addCDAValues(Map<String, Object> values, CDABean cdaBean) {
         for (DatoBean dat : cdaBean.getDatos()) {
             for(CodificacionBean cb : dat.getCodigos()){
                 if((cb.getCode()+cb.getCodeSystem()).equals(SepsisConstants.TENSION_ARTERIAL_CODE)){
                     
                     this.addTensionArterialValues(dat.getDato(), values);
                     
                 }else if(symptoms.containsKey(cb.getCode()+cb.getCodeSystem())){
                     values.put(symptoms.get(cb.getCode()+cb.getCodeSystem()), dat.getDato());
                 }
             }
         }
        
     }
     
     public Map<String, String> getSymptoms() {
         return symptoms;
     }
     
     public void setSymptoms(Map<String, String> symptoms) {
         this.symptoms = symptoms;
     }
     
     public List<String> getDimexValues() {
         return dimexValues;
     }
 
     public void setDimexValues(List<String> dimexValues) {
         this.dimexValues = dimexValues;
     }
 
     private void addTensionArterialValues(String dato, Map<String, Object> values) {
         
         String d = dato.toUpperCase();
         d = d.replaceAll("TA", "");
         String[] ds = d.split("/");
         int tas = Integer.parseInt(ds[0].trim());
         int tad = Integer.parseInt(ds[1].trim());
         Double tam = ((tas - tad)/3.0)+tad;
         values.put(SepsisConstants.TENSION_ARTERIAL_SISTOLICA,ds[0]);
         values.put(SepsisConstants.TENSION_ARTERIAL_MEDIA,tam.toString());
     }
 }
