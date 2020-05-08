 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package view;
 
 import com.opensymphony.xwork2.ActionContext;
 import dao.ListarTwitsDao;
 import java.util.ArrayList;
 import java.util.Map;
 import model.Hashtags;
 import model.Twits;
 import model.Usuarios;
 import model.timelineView;
 
 /**
  *
  * @author el pampa
  */
 public class BusquedaAction {
 private String buscate;    
 private ArrayList<Usuarios>nombresUsuarios=new ArrayList<Usuarios> ();
private int valoru=0;
 private ArrayList<Twits>twitsUsuarios=new ArrayList<Twits> ();
 private ArrayList<timelineView>listaTabla=new ArrayList<timelineView>();
  
private int valort=0;
 private ArrayList<Hashtags>temasUsuarios=new ArrayList<Hashtags> ();
private int valorh=0;
 private int valor=1;
 
  public ArrayList<timelineView> getListaTabla() {
         return listaTabla;
     }
 
     public void setListaTabla(ArrayList<timelineView> listaTabla) {
         this.listaTabla = listaTabla;
     }
     public String getBuscate() {
         return buscate;
     }
 
     public void setBuscate(String buscate) {
         this.buscate = buscate;
     }
 
     public ArrayList<Usuarios> getNombresUsuarios() {
         return nombresUsuarios;
     }
 
     public void setNombresUsuarios(ArrayList<Usuarios> nombresUsuarios) {
         this.nombresUsuarios = nombresUsuarios;
     }
 
     public int getValoru() {
         return valoru;
     }
 
     public void setValoru(int valoru) {
         this.valoru = valoru;
     }
 
     public ArrayList<Twits> getTwitsUsuarios() {
         return twitsUsuarios;
     }
 
     public void setTwitsUsuarios(ArrayList<Twits> twitsUsuarios) {
         this.twitsUsuarios = twitsUsuarios;
     }
 
     public int getValort() {
         return valort;
     }
 
     public void setValort(int valort) {
         this.valort = valort;
     }
 
     public ArrayList<Hashtags> getTemasUsuarios() {
         return temasUsuarios;
     }
 
     public void setTemasUsuarios(ArrayList<Hashtags> temasUsuarios) {
         this.temasUsuarios = temasUsuarios;
     }
 
     public int getValorh() {
         return valorh;
     }
 
     public void setValorh(int valorh) {
         this.valorh = valorh;
     }
    
     public BusquedaAction() {
     }
     
     public String execute() throws Exception{
         Map auth = ActionContext.getContext().getSession();
         
         if (buscate.charAt(0)=='@' | buscate.charAt(0) == '#') {
        
             buscate=buscate.substring(1, buscate.length());
            
         
         }
        if(dao.BusquedaDao.getUsuarios(buscate).size()>0){
            nombresUsuarios=dao.BusquedaDao.getUsuarios(buscate);
            valoru=1;
           }
        else {
        valoru=2;
        }
        
        
        if(dao.BusquedaDao.getHashtags(buscate).size()>0){
            temasUsuarios=dao.BusquedaDao.getHashtags(buscate);
            valorh=1;
        }else {
        valorh=2;
        }
        
       
        
        
        if(dao.BusquedaDao.getTwitList(buscate).size()>0){
             twitsUsuarios=dao.BusquedaDao.getTwitList(buscate);
        for (int i = 0; i < twitsUsuarios.size(); i++) {
            timelineView e = new timelineView();
         listaTabla.add(i, e);
         listaTabla.get(i).setIdt(twitsUsuarios.get(i).getIdt());
         listaTabla.get(i).setIdu(twitsUsuarios.get(i).getIdu());
         listaTabla.get(i).setString(Parser.parseTweet(twitsUsuarios.get(i).getString()));
         listaTabla.get(i).setTimestam(twitsUsuarios.get(i).getTimestam());
         listaTabla.get(i).setNombre(ListarTwitsDao.getSingleUser(twitsUsuarios.get(i).getIdu()).getNombre());
         listaTabla.get(i).setImagen(ListarTwitsDao.getSingleUser(twitsUsuarios.get(i).getIdu()).getImagen());
         listaTabla.get(i).setIdsesion(((Number)auth.get("idusuario")).longValue());
 
         }
        
            
            
           
             valort=1;
        }else {
        valort=2;
        }
        System.out.println("aca va la busqueda: "+buscate);
         System.out.println("nombres de usuarios: "+valoru);
         System.out.println("nombres de tweets: "+valort);
         System.out.println("nombres de temas: "+valorh);
        return "bien";
         }
    
     
 }
