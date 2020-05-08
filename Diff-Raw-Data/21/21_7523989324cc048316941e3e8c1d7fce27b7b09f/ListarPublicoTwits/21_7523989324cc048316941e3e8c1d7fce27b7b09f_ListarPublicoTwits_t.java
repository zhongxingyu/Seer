 package view;
 
 import com.opensymphony.xwork2.ActionContext;
 import dao.ListarTwitsDao;
 import java.util.ArrayList;
 import java.util.Map;
 import model.Twits;
 import model.timelineView;
 import view.Parser;
 
 /**
  *
  * @author Franco
  */
 public class ListarPublicoTwits {
     
     public ArrayList <Twits> listaTwits = new ArrayList<Twits>();
     public ArrayList <timelineView> listaTabla = new ArrayList<timelineView>();
    int retweet;
     
     
      public String execute() throws Exception {
     
            Map auth = ActionContext.getContext().getSession();
 
        listaTwits = ListarTwitsDao.getPublicTwitList(Long.parseLong(auth.get("publicocontext").toString()));
        
       
       for (int i = 0; i < listaTwits.size(); i++) {
            timelineView e = new timelineView();
         listaTabla.add(i, e);
         listaTabla.get(i).setIdt(listaTwits.get(i).getIdt());
         listaTabla.get(i).setIdu(listaTwits.get(i).getIdu());
         listaTabla.get(i).setString(Parser.parseTweet( listaTwits.get(i).getString()));
         listaTabla.get(i).setTimestam(listaTwits.get(i).getTimestam());
         listaTabla.get(i).setNombre(ListarTwitsDao.getSingleUser(listaTwits.get(i).getIdu()).getNombre());
         listaTabla.get(i).setImagen(ListarTwitsDao.getSingleUser(listaTwits.get(i).getIdu()).getImagen());
        listaTabla.get(i).setIdsesion(((Number)auth.get("idusuario")).longValue());
 
         }
 
          
     return "fin";
     }
 
      /*GET AND SET*/
     public ArrayList<Twits> getlistaTwits() {
         return listaTwits;
     }
 
     public void setlistaTwits(ArrayList<Twits> listaTwits) {
         this.listaTwits = listaTwits;
     }
 
     public ArrayList<timelineView> getListaTabla() {
         return listaTabla;
     }
 
     public void setListaTabla(ArrayList<timelineView> listaTabla) {
         this.listaTabla = listaTabla;
     }
 
 }
