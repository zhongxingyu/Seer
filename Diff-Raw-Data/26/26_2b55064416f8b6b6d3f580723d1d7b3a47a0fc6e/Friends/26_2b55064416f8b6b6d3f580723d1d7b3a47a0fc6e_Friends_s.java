 package api;
 
 /**
  *
  * @author Franco
  */
 import java.util.List;
  
 import com.opensymphony.xwork2.Action;
 import model.Usuarios;
  
 public class Friends{
  
 	//Valores Retornados en el Json (Twits)
 	 List <Usuarios> amigos;
          private String message = "OK";
 
 	//Parametros (no getter method, will not include in the JSON)
         private String key;
         private String aplication;
         private String type; //0 means newers
         
 	public Friends(){
 		//lists.add("list1"
 	}
  
        public String execute() {
         if(dao.ApiDao.CheckKey(key) == true) {
            if(dao.ApiDao.CheckAplication(aplication) == true) {
                if(type != null) {
                
                if(type.equals("1")) {
                amigos = dao.ApiDao.Friends(key,"1");
                }
                if(type.equals("2")) {
                 amigos = dao.ApiDao.Friends(key,"2");
                }
               else{
                   message="FAIL - type no reconocido";
               }
                }
                else {
                    message="FAIL - type no encontrado";
                }
            }
            else{
                message="FAIL - Aplicación no autorizada";
            }    
        }
        
        else {
            message="FAIL - Key no verificada";
        }
       
       return Action.SUCCESS;
         }
 
     public void setKey(String key) {
         this.key = key;
     }
 
     public void setAplication(String aplication) {
         this.aplication = aplication;
     }
 
     public void setType(String type) {
         this.type = type;
     }
 
     
     
     public List<Usuarios> getAmigos() {
         return amigos;
     }
 
     public void setAmigos(List<Usuarios> amigos) {
         this.amigos = amigos;
     }
 
     public String getMessage() {
         return message;
     }
 
     public void setMessage(String message) {
         this.message = message;
     }
  
 }
