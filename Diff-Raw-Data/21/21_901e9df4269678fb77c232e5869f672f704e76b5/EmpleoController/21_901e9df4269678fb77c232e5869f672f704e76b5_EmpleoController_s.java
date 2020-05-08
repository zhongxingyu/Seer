 
 package com.vi.clasificados.consultas.controller;
 
 import com.vi.clasificados.dominio.Clasificado;
 import com.vi.clasificados.services.ClasificadosService;
 import com.vi.clasificados.services.ConsultasService;
 import com.vi.clasificados.utils.ClasificadosTipo;
 import com.vi.locator.ComboLocator;
 import com.vi.util.FacesUtil;
 import java.util.List;
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.model.SelectItem;
 
 @ManagedBean(name="empleoController")
 @SessionScoped
 public class EmpleoController {
     private List<Clasificado> clasificados;
     
     private int tipoOferta = ClasificadosTipo.EMPOFERTA.getId();
     private int area = 0;
     private int rangoSalarial = 0;
     
     private List<SelectItem> tipos;
     private List<SelectItem> areas;
     private List<SelectItem> rangos;
     
     
     @EJB
     ClasificadosService service;
     
     //Otros objetos necesarios
     ComboLocator comboLocator;
     
     @PostConstruct
     public void init(){
         comboLocator = ComboLocator.getInstance();
        clasificados = service.consultar(ConsultasService.EMPLEO, tipoOferta, area, rangoSalarial);
        
         tipos = FacesUtil.getSelectsItem(comboLocator.getDataForCombo(ComboLocator.EMP_TIPO));
         areas = FacesUtil.getSelectsItem(comboLocator.getDataForCombo(ComboLocator.EMP_AREA));
         rangos = FacesUtil.getSelectsItem(comboLocator.getDataForCombo(ComboLocator.EMP_RANGO));
     }
     
     public String cambiarFiltro() {
         clasificados = service.consultar(ConsultasService.EMPLEO, tipoOferta, area, rangoSalarial);
         return "/consultas/empleo.xhtml";
     }
     
 
     /**
      * @return the tipoOferta
      */
     public int getTipoOferta() {
         return tipoOferta;
     }
 
     /**
      * @param tipoOferta the tipoOferta to set
      */
     public void setTipoOferta(int tipoOferta) {
         this.tipoOferta = tipoOferta;
     }
 
     /**
      * @return the area
      */
     public int getArea() {
         return area;
     }
 
     /**
      * @param area the area to set
      */
     public void setArea(int area) {
         this.area = area;
     }
 
     /**
      * @return the rangoSalarial
      */
     public int getRangoSalarial() {
         return rangoSalarial;
     }
 
     /**
      * @param rangoSalarial the rangoSalarial to set
      */
     public void setRangoSalarial(int rangoSalarial) {
         this.rangoSalarial = rangoSalarial;
     }
 
     /**
      * @return the clasificados
      */
     public List<Clasificado> getClasificados() {
         return clasificados;
     }
 
     /**
      * @param clasificados the clasificados to set
      */
     public void setClasificados(List<Clasificado> clasificados) {
         this.clasificados = clasificados;
     }
 
     /**
      * @return the tipos
      */
     public List<SelectItem> getTipos() {
         return tipos;
     }
 
     /**
      * @return the areas
      */
     public List<SelectItem> getAreas() {
         return areas;
     }
 
     /**
      * @return the rangos
      */
     public List<SelectItem> getRangos() {
         return rangos;
     }
 
   
 }
