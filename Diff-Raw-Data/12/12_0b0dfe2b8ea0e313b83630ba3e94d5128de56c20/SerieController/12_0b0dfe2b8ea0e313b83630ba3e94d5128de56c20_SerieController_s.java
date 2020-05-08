 package com.mycompany.biblio.controller;
 
 import com.mycompany.biblio.business.SerieEJB;
 import com.mycompany.biblio.model.Serie;
 import javax.faces.bean.ManagedBean;
 import javax.ejb.EJB;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import javax.faces.bean.SessionScoped;
 import javax.faces.component.html.HtmlDataTable;
 import javax.faces.model.ListDataModel;
 
 
 @ManagedBean
 @SessionScoped
public class SerieController {
 
     // ======================================
     // =             Attributes             =
     // ======================================
 
     @EJB
     private SerieEJB SerieEJB ;
 
     private HtmlDataTable dataTable;
 
     private Serie serie = new Serie();
     private ListDataModel serieList;
     
     private void updateSerieList() {
         serieList = new ListDataModel(SerieEJB.findAll());
     }
 
     // ======================================
     // =           Public Methods           =
     // ======================================
 
     public String doNew() {
         serie = new Serie();
         return "newSerie.xhtml";
     }
 
     public String doCreate() {
         SerieEJB.create(serie);
         return "listSeries.xhtml";
     }
     
     public String doDelete() {
         List<Serie> series = (List<Serie>)serieList.getWrappedData();
         SerieEJB.delete(onlySelected(series));
         return "listSeries.xhtml";
     }
 
     private List<Serie> onlySelected(List<Serie> list) {
         for (Iterator<Serie> it = list.iterator(); it.hasNext(); ) {
             if (!(it.next().isSelected()))
                 it.remove();
         }
         return list;
     }
 
     public String doEdit() {
         serie = (Serie)serieList.getRowData();
         return "editSerie.xhtml";
     }
 
     public String doSave() {
         SerieEJB.update(serie);
        return "listSeires.xhtml";
     }
     // ======================================
     // =          Getters & Setters         =
     // ======================================
 
     public Serie getSerie() {
         return serie;
     }
 
     public void setSerie(Serie serie) {
         this.serie = serie;
     }
 
     public ListDataModel getSerieList() {
         updateSerieList();
         return serieList;
     }
 
     public void setSerieList(ListDataModel serieList) {
         this.serieList = serieList;
     }
 
     public HtmlDataTable getDataTable() {
         return dataTable;
     }
 
     public void setDataTable(HtmlDataTable dataTable) {
         this.dataTable = dataTable;
     }
 
     
 }
