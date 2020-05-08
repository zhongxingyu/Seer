 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.artivisi.endy.belajar.jee.jsf;
 
 import com.artivisi.endy.belajar.jee.banking.entity.Nasabah;
 import com.artivisi.endy.belajar.jee.banking.service.BankingServiceBean;
 import java.util.List;
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.model.ListDataModel;
 
 /**
  *
  * @author student14
  */
 @ManagedBean
 @SessionScoped
 public class NasabahManagedBean {
     @EJB
     private BankingServiceBean bankingServiceBean;
     
     private Integer jumlahRequest = 0;
     private Nasabah nasabah = new Nasabah();
     
     private ListDataModel<Nasabah> dataModel = new ListDataModel<Nasabah>();
 
     public Nasabah getNasabah() {
         return nasabah;
     }
 
     public void setNasabah(Nasabah nasabah) {
         this.nasabah = nasabah;
     }
 
     public Integer getJumlahRequest() {
         return jumlahRequest;
     }
 
     public void setJumlahRequest(Integer jumlahRequest) {
         this.jumlahRequest = jumlahRequest;
     }
 
     public String edit(){
         nasabah = dataModel.getRowData();
         return "form?faces-redirect=true";
     }
     
     public String delete(){
         return "list?faces-redirect=true";
     }
     
     /**
      * Creates a new instance of NasabahManagedBean
      */
     public NasabahManagedBean() {
     }
     
     public ListDataModel<Nasabah> getDaftarNasabah(){
         dataModel = new ListDataModel<Nasabah>(bankingServiceBean.cariSemuaNasabah(0, 100));
         return dataModel;
     }
     
     public String simpan(){
         jumlahRequest++;
         System.out.println("Tombol simpan sudah ditekan sebanyak "
                 +jumlahRequest+" kali");
         
         // simpan object nasabah ke database
         bankingServiceBean.simpan(nasabah);
         
         // setelah simpan, reset menjadi object kosong lagi
         nasabah = new Nasabah();
         
         return "list?faces-redirect=true";
     }
 }
