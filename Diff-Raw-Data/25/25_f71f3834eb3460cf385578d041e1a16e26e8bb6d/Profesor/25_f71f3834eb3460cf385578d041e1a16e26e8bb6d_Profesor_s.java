 // Probando nuevos cambios para que el jefe este contento xD
 
 /**
 ******************************************************
 * @file Profesor.java
 * @author AirZs
 * @date mayo 2010
 * @version 0.1
 * Esta clase representa un curso de la universidad
 * Se define como una instancia de un curso diferente cuando un
 * curso posee un codigo de curso y sección distinto a otro curso
 * Para diferenciar una instancia de otra se usa un id.
 * @brief En este archivo se especifica la clase del tipo Profesor.
 *****************************************************/
 
 package jramos.tiposDatos;
 import java.util.ArrayList;
 public class Profesor {
     static int idProfesorActual = 0;
 
     private String nomProfe;
     private ArrayList<Hora> horaDisp;
     private ArrayList<Hora> horaOcup;
     private ArrayList<Integer> cursosDisp; //Solo codigos de curso
     private ArrayList<Curso> cursosAsig; //Referencias reales de curso
     private ArrayList<Integer> idCursosAsig; //Solo id de cursos
     private int rutProfe;
     private int idProfesor;
 
     //Contructor
 
     /**
      * 1º Constructor para Profesor:
      * Este constructor debe ser utilizado para crear un objeto Profesor la primera vez. Se le asigna un
      * id de forma automatica.
      * Se instancian los ArrayList del objeto como listas vacias.
      * @param nombreProfesor string con el Nombre del Profesor
      * @param cursosParaImpartir ArrayList<Integer> con los codigos de los cursos que este profesor puede impartir
      */
     public Profesor(String nombreProfesor, int rut, ArrayList<Integer> cursosParaImpartir){
         this.nomProfe = nombreProfesor;
         this.cursosDisp = cursosParaImpartir;
         this.rutProfe = rut;
         this.horaDisp = new ArrayList<Hora>();
         this.horaOcup = new ArrayList<Hora>();
         this.cursosAsig = new ArrayList<Curso>();
         this.idCursosAsig =  new ArrayList<Integer>();
         this.idProfesor = ++idProfesorActual;
 
     }
 
     /**
      * 2º Constructor para Profesor:
      * Este constructor debe ser utilizado para crear un objeto Profesor a partir de un registro o archivo.
      * Se instancian los ArrayList del objeto como listas vacias.
      * @param nombreProfesor string con el Nombre del Profesor
      * @param cursosParaImpartir ArrayList<Integer> con los codigos de los cursos que este profesor puede impartir
      * @param id int con el id del profesor
      */
     public Profesor(String nombreProfesor, int rut, ArrayList<Integer> cursosParaImpartir, int id){
         this.nomProfe = nombreProfesor;
         this.cursosDisp = cursosParaImpartir;
         this.horaDisp = new ArrayList<Hora>();
         this.horaOcup = new ArrayList<Hora>();
         this.cursosAsig = new ArrayList<Curso>();
         this.idCursosAsig =  new ArrayList<Integer>();
         this.idProfesor = id;
         this.rutProfe = rut;
     }
 
     // Metodos de Obtener variables
 
     /**
      * Obtiene el nombre del Profesor
      *
      * @return String con el nombre del profesor
      */
     public String getNombreProfesor(){
         return this.nomProfe;
     }
 
     /**
      * Obtiene la lista de Integer con los codigos de cursos que el profesor puede impartir
      * @return Devuelve un ArrayList con los codigos de curso que el profesor puede impartir.
      */
     public ArrayList<Integer> getCodCursosQueImparteArrayList()
     {       return this.cursosDisp;
     }
 
     /**
      * Obtiene las horas que el profesor tiene disponibles
      * Las horas son devueltas según el 2° formato de hora especificado en la clase Hora
      * @return String con las horas en el 2° formato separadas por un "|"
      */
     public String getHorasDisponibles(){ 
         StringBuilder text = new StringBuilder();
         for (Hora hora: this.horaDisp){
             try{
                 text.append(hora.getHoraStr()).append("|");
             }
             catch (Exception e){
                 System.out.println("Error al pedir datos de hora");
             }
         }
         text.deleteCharAt(text.length()-1);
         return text.toString();
     }
 
     public ArrayList<Hora> getHorasAsigArrayList()
     {       return this.horaOcup;
     }
 
     public ArrayList<Hora> getHorasDispArrayList()
     {       return this.horaDisp;
     }
 
     /**
      * Obtiene las horas que el profesor ya tiene asignadas para trabajar en la universidad
      *
      * @return String con las horas en formato Str separadas con |
      */
     public String getHorasAsignadas(){
         StringBuilder text = new StringBuilder();
         if (this.horaOcup.size() !=0)
         {   for (Hora hora: this.horaOcup){
                 try{
                     text.append(hora.getHoraStr()).append("|");
                 }
                 catch (Exception e){
                     System.out.println("Error al pedir datos de hora");
                 }
             }
             text.deleteCharAt(text.length()-1);
         }
         return text.toString();
     }
 
     @Override
     public String toString()
     {       return this.nomProfe;
     }
 
     /**
      * Obtiene los códigos de los curso que puede impartir este profesor
      *
      * @return String con los códigos de cursos separados por un "|"
      */
     public String getCodCursosQueImparte(){
         StringBuilder text = new StringBuilder();
         for (Integer codCurso: this.cursosDisp){
             text.append(String.valueOf(codCurso)).append("|");
         }
         text.deleteCharAt(text.length()-1);
         return text.toString();
     }
 
     /**
      * Metodo para obtener los cursos que ya esta impartiendo este profesor
      *
      * @return String con formato BNF  <curso>:=<nombreCurso> - <Seccion> Separados con |
      */
     public String getCursosAsignados(){
         if (this.cursosAsig.size() != 0)
         {   StringBuilder text = new StringBuilder();
             for (Curso curso: this.cursosAsig){
                 text.append(curso + " - "+curso.getSeccion() + "\n");
             }
             text.deleteCharAt(text.length()-1);
             return text.toString();
         }
         else
             return "";
     }
     public ArrayList<Curso> getCursosAsigArrayList()
     {       return this.cursosAsig;
     }
 
     /**
      * Obtiene los códigos de los cursos asignados
      * 
      * @return String con los códigos de cursos asignados al profesor separados por un "|"
      */
     public String getIdCursosAsignados(){
         if (this.idCursosAsig.size() != 0)
         {   StringBuilder text = new StringBuilder();
             for (Integer idCurso: this.idCursosAsig){
                 text.append(String.valueOf(idCurso));
                 text.append("|");
             }
             text.deleteCharAt(text.length()-1);
             return text.toString();
         }
         else
             return "";
     }
 
     public ArrayList<Integer> getIdCursosAsignadosArrayList(){
         return this.idCursosAsig;
     }
 
     /**
      * Obtiene el rut del profesor como un entero
      * El rut del profesor no posee el guión ni el digito verificador
      * @return int con el rut del profesor
      */
     public int getRutProfesor(){
         return this.rutProfe;
     }
 
     /**
      * Obtiene el id unico del profesor como valor entero
      *
      * @return int con el id del profesor
      */
     public int getIdProfesor(){
         return this.idProfesor;
     }
 
     /**
      * Obtiene el ultimo id que fue asignado a algun profesor por la clase Profesor
      *
      * @return int con la cuenta id de la clase Profesor
      */
     static public int getIdProfesorGlobal(){
         return Profesor.idProfesorActual;
     }
 
     // Metodos para agregar o modificar Variables
 
     /**
      * Metodo para agregar o quitar las horas que el profesor tiene disponibles
      * Si la hora ya existe no la agrega
      * Si el selector no es válido no hace nada.
      * @param horaToAsig Objeto Hora que se quiere agregar o quitar
      * @param selector 1: Agregar  -1:Quitar
      */
     public void modHorasDisponibles(Hora horaToAsig, int selector){
         if (selector == 1){
             if (!this.horaDisp.contains(horaToAsig)){
                 this.horaDisp.add(horaToAsig);
             }
             else{
                 System.out.println("Ya existe esa hora..");
             }
         }
         else if (selector == -1){
             if (this.horaDisp.contains(horaToAsig)){
                 this.horaDisp.remove(horaToAsig);
             }
             else{
                 System.out.println("No existe esa hora...");
             }
         }
         else{
             System.out.println("Error en la seleccion...");
         }
     }
 
     /**
      * Metodo para agregar o quitar las horas que el profesor tiene asignadas
      *
      * @param horaToAsig Objeto Hora que se quiere agregar o quitar
      * @param selector 1: Agregar  -1:Quitar
      */
     public void modHorasAsignadas(Hora horaToAsig, int selector){
         if (selector == 1)
         {       if (!this.horaOcup.contains(horaToAsig))
                 {       this.horaOcup.add(horaToAsig);
                 }
                 else
                 {
                         System.out.println("Ya existe esa hora...");
                 }
         }
         else if (selector == -1){
             if (this.horaOcup.contains(horaToAsig)){
                 this.horaOcup.remove(horaToAsig);
             }
             else{
                 System.out.println("No existe esa hora...");
             }
         }
         else {
             System.out.println("Error en la seleccion...");
         }
     }
 
     /**
      * Metodo para agregar o quitar los códigos a la lista de los cursos que el profesor puede impartir
      *
      * @param codCursoToAsig valor entero con el codigo del curso que se quiere agregar o quitar
      * @param selector 1: Agregar  -1:Quitar
      */
     public void modCursosParaImpartir(int codCursoToAsig, int selector){
         if (selector == 1){
             if (!this.cursosDisp.contains(codCursoToAsig)){
                 this.cursosDisp.add(codCursoToAsig);
             }
             else{
                 System.out.println("Ya existe ese curso...");
             }
         }
         else if (selector == -1){
             if (this.cursosDisp.contains(codCursoToAsig)){
                 this.cursosDisp.remove(codCursoToAsig);
             }
             else{
                 System.out.println("No existe ese curso...");
             }
         }
         else{
             System.out.println("Error en la seleccion...");
         }
     }
 
     /**
      * Metodo para agregar o quitar cursos de la lista de cursos asignados del profesor.
      * Si el curso ya existe, no lo agrega.
      * Si el selector no es válido no hace nada.
      * @param cursoToAsig Objeto Curso que se quiere agregar o quitar
      * @param selector 1: Agregar  -1:Quitar
      */
     public void modCursosAsignados(Curso cursoToAsig,  int selector){
        Integer codigoActual = new Integer(cursoToAsig.getCodigoCurso());
         if (selector == 1){
             if (!this.cursosAsig.contains(cursoToAsig)){
                 this.cursosAsig.add(cursoToAsig);
                if(this.idCursosAsig.contains(codigoActual)){
                    this.idCursosAsig.add(codigoActual);
                 }
             }
             else{
                 System.out.println("Ya existe ese curso...");
             }
         }
         else if (selector == -1){
             if (this.cursosAsig.contains(cursoToAsig)){
                 this.cursosAsig.remove(cursoToAsig);
                if (this.idCursosAsig.contains(codigoActual))
                    this.idCursosAsig.remove(codigoActual);
             }
             else{
                 System.out.println("No existe ese curso...");
             }
         }
         else{
             System.out.println("Error en la seleccion...");
         }
     }
 
     /**
      * Metodo para agregar o quitar cursos de la lista de cursos asignados del profesor.
      * Si el curso ya existe, no lo agrega.
      * Si el selector no es válido no hace nada.
      * @param idCursoToAsig Objeto Integer con el id del curso que se quiere agregar o quitar
      * @param selector 1: Agregar  -1:Quitar
      */
     public void modIdCursosAsignados(Integer idCursoToAsig, int selector){
         if (selector == 1){
             if (!this.idCursosAsig.contains(idCursoToAsig)){
                 this.idCursosAsig.add(idCursoToAsig);
             }
             else{
                 System.out.println("Ya existe ese curso...");
             }
         }
         else if (selector == -1){
             if (!this.idCursosAsig.contains(idCursoToAsig)){
                 this.idCursosAsig.remove(idCursoToAsig);
             }
             else{
                 System.out.println("No existe ese curso...");
             }
         }
         else{
             System.out.println("Error en la seleccion...");
         }
     }
 
     /**
      * Metodo para setear el rut del profesor
      * setea el atributo rut del profesor, este debe ser un entero sin digito verificador
      * @param rut valor entero con el rut del profesor
      */
     public void setRutProfesor(int rut){
         this.rutProfe = rut;
     }
 
     /**
      * Metodo para setear la cuenta de Id del profesor actual(variable static). Se debe obtener desde un archivo.
      * Se utiliza este metodo siempre que se cargue el sistema, para no volver a utilizar los mismos id.
      * 
      * @param id valor entero con el id static para ser seteado.
      */
     static public void setIdProfesores(int id){
         Profesor.idProfesorActual = id;
     }
 
     }
