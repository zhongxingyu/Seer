 package com.vi.clasificados.consultas.controller;
 
 import com.vi.clasificados.dominio.Publicidad;
 import com.vi.clasificados.services.PublicidadService;
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 
 /**
  * @author jerviver21
  */
 
 @ManagedBean(name="publicidadController")
 @SessionScoped
 public class PublicidadController {
     private Publicidad bannerSuperior;
     private Publicidad bannerLIEmp;
     private Publicidad bannerLIImb;
     private Publicidad bannerLIVeh;
     private Publicidad bannerLIOtr;
     private Publicidad bannerLD1Emp;
     private Publicidad bannerLD1Imb;
     private Publicidad bannerLD1Veh;
     private Publicidad bannerLD1Otr;
     private Publicidad bannerLD2Emp;
     private Publicidad bannerLD2Imb;
     private Publicidad bannerLD2Veh;
     private Publicidad bannerLD2Otr;
     private Publicidad bannerLD3Emp;
     private Publicidad bannerLD3Imb;
     private Publicidad bannerLD3Veh;
     private Publicidad bannerLD3Otr;
     
     private boolean renderSuperior = false;
     private boolean renderLIEmp = false;
     private boolean renderLIImb = false;
     private boolean renderLIVeh = false;
     private boolean renderLIOtr = false;
     private boolean renderLD1Emp = false;
     private boolean renderLD1Imb = false;
     private boolean renderLD1Veh = false;
     private boolean renderLD1Otr = false;
     private boolean renderLD2Emp = false;
     private boolean renderLD2Imb = false;
     private boolean renderLD2Veh = false;
     private boolean renderLD2Otr = false;
     private boolean renderLD3Emp = false;
     private boolean renderLD3Imb = false;
     private boolean renderLD3Veh = false;
     private boolean renderLD3Otr = false;
 
     
     @EJB
     PublicidadService pService;
     
     @PostConstruct
     public void init(){
        setBannerSuperior(pService.getBanner(PublicidadService.BANNER_SUPERIOR));
         setBannerLIEmp(pService.getBanner(PublicidadService.BANNER_LIEmp));
         setBannerLIImb(pService.getBanner(PublicidadService.BANNER_LIImb));
         setBannerLIVeh(pService.getBanner(PublicidadService.BANNER_LIVeh));
         setBannerLIOtr(pService.getBanner(PublicidadService.BANNER_LIOtr));
         setBannerLD1Emp(pService.getBanner(PublicidadService.BANNER_LD1Emp));
         setBannerLD1Imb(pService.getBanner(PublicidadService.BANNER_LD1Imb));
         setBannerLD1Veh(pService.getBanner(PublicidadService.BANNER_LD1Veh));
         setBannerLD1Otr(pService.getBanner(PublicidadService.BANNER_LD1Otr));
         setBannerLD2Emp(pService.getBanner(PublicidadService.BANNER_LD2Emp));
         setBannerLD2Imb(pService.getBanner(PublicidadService.BANNER_LD2Imb));
         setBannerLD2Veh(pService.getBanner(PublicidadService.BANNER_LD2Veh));
         setBannerLD2Otr(pService.getBanner(PublicidadService.BANNER_LD2Otr));
         setBannerLD3Emp(pService.getBanner(PublicidadService.BANNER_LD3Emp));
         setBannerLD3Imb(pService.getBanner(PublicidadService.BANNER_LD3Imb));
         setBannerLD3Veh(pService.getBanner(PublicidadService.BANNER_LD3Veh));
         setBannerLD3Otr(pService.getBanner(PublicidadService.BANNER_LD3Otr));
         
         renderSuperior = bannerSuperior!=null?true:false;
         renderLIEmp = bannerLIEmp!=null?true:false;
         renderLIImb = bannerLIImb!=null?true:false;
         renderLIVeh = bannerLIVeh!=null?true:false;
         renderLIOtr = bannerLIOtr!=null?true:false;
         renderLD1Emp = bannerLD1Emp!=null?true:false;
         renderLD1Imb = bannerLD1Imb!=null?true:false;
         renderLD1Veh = bannerLD1Veh!=null?true:false;
         renderLD1Otr = bannerLD1Otr!=null?true:false;
         renderLD2Emp = bannerLD2Emp!=null?true:false;
         renderLD2Imb = bannerLD2Imb!=null?true:false;
         renderLD2Veh = bannerLD2Veh!=null?true:false;
         renderLD2Otr = bannerLD2Otr!=null?true:false;
         renderLD3Emp = bannerLD3Emp!=null?true:false;
         renderLD3Imb = bannerLD3Imb!=null?true:false;
         renderLD3Veh = bannerLD3Veh!=null?true:false;
        renderLD3Otr = bannerLD3Otr!=null?true:false;
     }
     
     public String redirect(){
         return "http://www.google.com?faces-redirect=true";
     }
 
     /**
      * @return the bannerSuperior
      */
     public Publicidad getBannerSuperior() {
         return bannerSuperior;
     }
 
     /**
      * @param bannerSuperior the bannerSuperior to set
      */
     public void setBannerSuperior(Publicidad bannerSuperior) {
         this.bannerSuperior = bannerSuperior;
     }
 
     /**
      * @return the bannerLIEmp
      */
     public Publicidad getBannerLIEmp() {
         return bannerLIEmp;
     }
 
     /**
      * @param bannerLIEmp the bannerLIEmp to set
      */
     public void setBannerLIEmp(Publicidad bannerLIEmp) {
         this.bannerLIEmp = bannerLIEmp;
     }
 
     /**
      * @return the bannerLIImb
      */
     public Publicidad getBannerLIImb() {
         return bannerLIImb;
     }
 
     /**
      * @param bannerLIImb the bannerLIImb to set
      */
     public void setBannerLIImb(Publicidad bannerLIImb) {
         this.bannerLIImb = bannerLIImb;
     }
 
     /**
      * @return the bannerLIVeh
      */
     public Publicidad getBannerLIVeh() {
         return bannerLIVeh;
     }
 
     /**
      * @param bannerLIVeh the bannerLIVeh to set
      */
     public void setBannerLIVeh(Publicidad bannerLIVeh) {
         this.bannerLIVeh = bannerLIVeh;
     }
 
     /**
      * @return the bannerLIOtr
      */
     public Publicidad getBannerLIOtr() {
         return bannerLIOtr;
     }
 
     /**
      * @param bannerLIOtr the bannerLIOtr to set
      */
     public void setBannerLIOtr(Publicidad bannerLIOtr) {
         this.bannerLIOtr = bannerLIOtr;
     }
 
     /**
      * @return the bannerLD1Emp
      */
     public Publicidad getBannerLD1Emp() {
         return bannerLD1Emp;
     }
 
     /**
      * @param bannerLD1Emp the bannerLD1Emp to set
      */
     public void setBannerLD1Emp(Publicidad bannerLD1Emp) {
         this.bannerLD1Emp = bannerLD1Emp;
     }
 
     /**
      * @return the bannerLD1Imb
      */
     public Publicidad getBannerLD1Imb() {
         return bannerLD1Imb;
     }
 
     /**
      * @param bannerLD1Imb the bannerLD1Imb to set
      */
     public void setBannerLD1Imb(Publicidad bannerLD1Imb) {
         this.bannerLD1Imb = bannerLD1Imb;
     }
 
     /**
      * @return the bannerLD1Veh
      */
     public Publicidad getBannerLD1Veh() {
         return bannerLD1Veh;
     }
 
     /**
      * @param bannerLD1Veh the bannerLD1Veh to set
      */
     public void setBannerLD1Veh(Publicidad bannerLD1Veh) {
         this.bannerLD1Veh = bannerLD1Veh;
     }
 
     /**
      * @return the bannerLD1Otr
      */
     public Publicidad getBannerLD1Otr() {
         return bannerLD1Otr;
     }
 
     /**
      * @param bannerLD1Otr the bannerLD1Otr to set
      */
     public void setBannerLD1Otr(Publicidad bannerLD1Otr) {
         this.bannerLD1Otr = bannerLD1Otr;
     }
 
     /**
      * @return the bannerLD2Emp
      */
     public Publicidad getBannerLD2Emp() {
         return bannerLD2Emp;
     }
 
     /**
      * @param bannerLD2Emp the bannerLD2Emp to set
      */
     public void setBannerLD2Emp(Publicidad bannerLD2Emp) {
         this.bannerLD2Emp = bannerLD2Emp;
     }
 
     /**
      * @return the bannerLD2Imb
      */
     public Publicidad getBannerLD2Imb() {
         return bannerLD2Imb;
     }
 
     /**
      * @param bannerLD2Imb the bannerLD2Imb to set
      */
     public void setBannerLD2Imb(Publicidad bannerLD2Imb) {
         this.bannerLD2Imb = bannerLD2Imb;
     }
 
     /**
      * @return the bannerLD2Veh
      */
     public Publicidad getBannerLD2Veh() {
         return bannerLD2Veh;
     }
 
     /**
      * @param bannerLD2Veh the bannerLD2Veh to set
      */
     public void setBannerLD2Veh(Publicidad bannerLD2Veh) {
         this.bannerLD2Veh = bannerLD2Veh;
     }
 
     /**
      * @return the bannerLD2Otr
      */
     public Publicidad getBannerLD2Otr() {
         return bannerLD2Otr;
     }
 
     /**
      * @param bannerLD2Otr the bannerLD2Otr to set
      */
     public void setBannerLD2Otr(Publicidad bannerLD2Otr) {
         this.bannerLD2Otr = bannerLD2Otr;
     }
 
     /**
      * @return the bannerLD3Emp
      */
     public Publicidad getBannerLD3Emp() {
         return bannerLD3Emp;
     }
 
     /**
      * @param bannerLD3Emp the bannerLD3Emp to set
      */
     public void setBannerLD3Emp(Publicidad bannerLD3Emp) {
         this.bannerLD3Emp = bannerLD3Emp;
     }
 
     /**
      * @return the bannerLD3Imb
      */
     public Publicidad getBannerLD3Imb() {
         return bannerLD3Imb;
     }
 
     /**
      * @param bannerLD3Imb the bannerLD3Imb to set
      */
     public void setBannerLD3Imb(Publicidad bannerLD3Imb) {
         this.bannerLD3Imb = bannerLD3Imb;
     }
 
     /**
      * @return the bannerLD3Veh
      */
     public Publicidad getBannerLD3Veh() {
         return bannerLD3Veh;
     }
 
     /**
      * @param bannerLD3Veh the bannerLD3Veh to set
      */
     public void setBannerLD3Veh(Publicidad bannerLD3Veh) {
         this.bannerLD3Veh = bannerLD3Veh;
     }
 
     /**
      * @return the bannerLD3Otr
      */
     public Publicidad getBannerLD3Otr() {
         return bannerLD3Otr;
     }
 
     /**
      * @param bannerLD3Otr the bannerLD3Otr to set
      */
     public void setBannerLD3Otr(Publicidad bannerLD3Otr) {
         this.bannerLD3Otr = bannerLD3Otr;
     }
 
     /**
      * @return the renderSuperior
      */
     public boolean isRenderSuperior() {
         return renderSuperior;
     }
 
     /**
      * @param renderSuperior the renderSuperior to set
      */
     public void setRenderSuperior(boolean renderSuperior) {
         this.renderSuperior = renderSuperior;
     }
 
     /**
      * @return the renderLIEmp
      */
     public boolean isRenderLIEmp() {
         return renderLIEmp;
     }
 
     /**
      * @param renderLIEmp the renderLIEmp to set
      */
     public void setRenderLIEmp(boolean renderLIEmp) {
         this.renderLIEmp = renderLIEmp;
     }
 
     /**
      * @return the renderLIImb
      */
     public boolean isRenderLIImb() {
         return renderLIImb;
     }
 
     /**
      * @param renderLIImb the renderLIImb to set
      */
     public void setRenderLIImb(boolean renderLIImb) {
         this.renderLIImb = renderLIImb;
     }
 
     /**
      * @return the renderLIVeh
      */
     public boolean isRenderLIVeh() {
         return renderLIVeh;
     }
 
     /**
      * @param renderLIVeh the renderLIVeh to set
      */
     public void setRenderLIVeh(boolean renderLIVeh) {
         this.renderLIVeh = renderLIVeh;
     }
 
     /**
      * @return the renderLIOtr
      */
     public boolean isRenderLIOtr() {
         return renderLIOtr;
     }
 
     /**
      * @param renderLIOtr the renderLIOtr to set
      */
     public void setRenderLIOtr(boolean renderLIOtr) {
         this.renderLIOtr = renderLIOtr;
     }
 
     /**
      * @return the renderLD1Emp
      */
     public boolean isRenderLD1Emp() {
         return renderLD1Emp;
     }
 
     /**
      * @param renderLD1Emp the renderLD1Emp to set
      */
     public void setRenderLD1Emp(boolean renderLD1Emp) {
         this.renderLD1Emp = renderLD1Emp;
     }
 
     /**
      * @return the renderLD1Imb
      */
     public boolean isRenderLD1Imb() {
         return renderLD1Imb;
     }
 
     /**
      * @param renderLD1Imb the renderLD1Imb to set
      */
     public void setRenderLD1Imb(boolean renderLD1Imb) {
         this.renderLD1Imb = renderLD1Imb;
     }
 
     /**
      * @return the renderLD1Veh
      */
     public boolean isRenderLD1Veh() {
         return renderLD1Veh;
     }
 
     /**
      * @param renderLD1Veh the renderLD1Veh to set
      */
     public void setRenderLD1Veh(boolean renderLD1Veh) {
         this.renderLD1Veh = renderLD1Veh;
     }
 
     /**
      * @return the renderLD1Otr
      */
     public boolean isRenderLD1Otr() {
         return renderLD1Otr;
     }
 
     /**
      * @param renderLD1Otr the renderLD1Otr to set
      */
     public void setRenderLD1Otr(boolean renderLD1Otr) {
         this.renderLD1Otr = renderLD1Otr;
     }
 
     /**
      * @return the renderLD2Emp
      */
     public boolean isRenderLD2Emp() {
         return renderLD2Emp;
     }
 
     /**
      * @param renderLD2Emp the renderLD2Emp to set
      */
     public void setRenderLD2Emp(boolean renderLD2Emp) {
         this.renderLD2Emp = renderLD2Emp;
     }
 
     /**
      * @return the renderLD2Imb
      */
     public boolean isRenderLD2Imb() {
         return renderLD2Imb;
     }
 
     /**
      * @param renderLD2Imb the renderLD2Imb to set
      */
     public void setRenderLD2Imb(boolean renderLD2Imb) {
         this.renderLD2Imb = renderLD2Imb;
     }
 
     /**
      * @return the renderLD2Veh
      */
     public boolean isRenderLD2Veh() {
         return renderLD2Veh;
     }
 
     /**
      * @param renderLD2Veh the renderLD2Veh to set
      */
     public void setRenderLD2Veh(boolean renderLD2Veh) {
         this.renderLD2Veh = renderLD2Veh;
     }
 
     /**
      * @return the renderLD2Otr
      */
     public boolean isRenderLD2Otr() {
         return renderLD2Otr;
     }
 
     /**
      * @param renderLD2Otr the renderLD2Otr to set
      */
     public void setRenderLD2Otr(boolean renderLD2Otr) {
         this.renderLD2Otr = renderLD2Otr;
     }
 
     /**
      * @return the renderLD3Emp
      */
     public boolean isRenderLD3Emp() {
         return renderLD3Emp;
     }
 
     /**
      * @param renderLD3Emp the renderLD3Emp to set
      */
     public void setRenderLD3Emp(boolean renderLD3Emp) {
         this.renderLD3Emp = renderLD3Emp;
     }
 
     /**
      * @return the renderLD3Imb
      */
     public boolean isRenderLD3Imb() {
         return renderLD3Imb;
     }
 
     /**
      * @param renderLD3Imb the renderLD3Imb to set
      */
     public void setRenderLD3Imb(boolean renderLD3Imb) {
         this.renderLD3Imb = renderLD3Imb;
     }
 
     /**
      * @return the renderLD3Veh
      */
     public boolean isRenderLD3Veh() {
         return renderLD3Veh;
     }
 
     /**
      * @param renderLD3Veh the renderLD3Veh to set
      */
     public void setRenderLD3Veh(boolean renderLD3Veh) {
         this.renderLD3Veh = renderLD3Veh;
     }
 
     /**
      * @return the renderLD3Otr
      */
     public boolean isRenderLD3Otr() {
         return renderLD3Otr;
     }
 
     /**
      * @param renderLD3Otr the renderLD3Otr to set
      */
     public void setRenderLD3Otr(boolean renderLD3Otr) {
         this.renderLD3Otr = renderLD3Otr;
     }
 
     
     
     
 }
