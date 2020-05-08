 package ar.noxit.ehockey.model;
 
 import ar.noxit.ehockey.exception.ErrorDeLoginException;
 import ar.noxit.hasher.Hasher;
 
 public abstract class Usuario {
 
     private String user; // id del usuario
     private String password;
     private String nombre;
     private String apellido;
 
     // estos atributos no se persisten
     private boolean logueado = false;
 
     public Usuario(String user, String password, Hasher hasher) {
         this.user = user;
         this.password = hasher.hash(password);
     }
 
     protected Usuario() {
     }
 
     public String getUser() {
         return user;
     }
 
     public String getNombre() {
         return nombre;
     }
 
     public String getApellido() {
         return apellido;
     }
 
     public void setNombre(String nombre) {
         this.nombre = nombre;
     }
 
     public void setApellido(String apellido) {
         this.apellido = apellido;
     }
 
     public void setPassword(String password, Hasher hasher) {
         this.password = hasher.hash(password);
     }
 
     public abstract String[] getRoles();
 
     /**
      * Logueo del usuario después de verificar la contraseña
      * 
      * @param password
      *            constraseña con la que se quiere loguear (sin encriptar)
      * @throws ErrorDeLoginException
      *             cuando no se puede loguear por error de contraseña
      */
     public void loguearse(String password, Hasher hasher) throws ErrorDeLoginException {
         if (!this.password.equals(hasher.hash(password))) {
             throw new ErrorDeLoginException("Password incorrecto");
         }
         this.logueado = true;
     }
 
     public void desloguearse() {
         this.logueado = false;
     }
 
     public boolean estaLogueado() {
         return logueado;
     }
 
     @Override
     public boolean equals(Object obj) {
        if (obj.getClass().equals(this.getClass())) {
            Usuario otro = (Usuario) obj;
            return (this.user.equals(otro.user) && this.password
                    .equals(otro.password));
        } else {
             return false;
        }
     }
 
     public abstract boolean puedeVer(Club club);
 }
