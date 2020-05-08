 /**
  ******************************************************
  * @file ManipuladorListas.java
  * @author victor flores sanchez
  * @date junio 2010
  * @version 0.1
  * Esta clase se encarga de gestionar las listas de profesores, cursos, carreras, semestres y facultades
  * Proporciona métodos para agregar y eliminar elementos de las listas
  * A la vez cuando agrega o elimina elementos crea las correspondientes referencias entre los demas objetos de las listas
  * Esta clase no se encarga de gestionar si hay nombres repetidos o tope horario.
  * Es esta clase a la que la interfaz gráfica le solicita sus métodos.
  ******************************************************
  */
 
 package jramos;
 
 import jramos.tiposDatos.Carrera;
 import jramos.tiposDatos.Curso;
 import jramos.tiposDatos.Facultad;
 import jramos.tiposDatos.Semestre;
 import jramos.tiposDatos.Hora;
 import jramos.tiposDatos.Profesor;
 import java.util.ArrayList;
 import jramos.excepciones.HoraNoDisponibleException;
 import jramos.excepciones.HourOutOfRangeException;
 import jramos.excepciones.StringVacioException;
 import jramos.excepciones.nombreRepetidoException;
 
 public class ManipuladorListas
 {       /* ATRIBUTOS */
         private ArrayList<Facultad> listaFacultades;
         private ArrayList<Carrera> listaCarreras;
         private ArrayList<Semestre> listaSemestres;
         private ArrayList<Curso> listaCursos;
         private ArrayList<Profesor> listaProfesores;
         
         /* CONSTRUCTOR */
         /**
          * Constructor que setea los atributos que corresponden a las listas de carreras, facultades, cursos, semestres y profesores
          * @param listaFacultades 
          * @param listaCarreras
          * @param listaSemestres
          * @param listaCursos
          * @param listaProfesores
          */
         public ManipuladorListas(ArrayList<Facultad> listaFacultades, ArrayList<Carrera> listaCarreras, ArrayList<Semestre> listaSemestres, ArrayList<Curso> listaCursos, ArrayList<Profesor> listaProfesores)
         {       this.listaFacultades = listaFacultades;
                 this.listaCarreras = listaCarreras;
                 this.listaSemestres = listaSemestres;
                 this.listaCursos = listaCursos;
                 this.listaProfesores = listaProfesores;
         }
 
         public ArrayList<Curso> getListaCursos()
         {       return this.listaCursos;
         }
 
         public ArrayList<Facultad> getListaFacultades()
         {       return this.listaFacultades;
         }
 
         public ArrayList<Carrera> getListaCarreras()
         {       return this.listaCarreras;
         }
 
         public ArrayList<Semestre> getListaSemestres()
         {       return this.listaSemestres;
         }
 
         public ArrayList<Profesor> getListaProfesores()
         {       return this.listaProfesores;
         }
 
         public void editaProfesor(Profesor profesorAEditar, String nuevonombre, String nuevoRut, String ramosDisponibles, String horasDisp) throws StringVacioException, nombreRepetidoException, HourOutOfRangeException
         {
 
         }
         
         public void editaCurso(Curso cursoAEditar, Profesor profesorAAsignarle, ArrayList<Hora> horasQueAsignarle, boolean comprobarHorarioSemestreAnterior, boolean comprobarHorarioSemestreSiguiente) throws HoraNoDisponibleException
         {       //compruebo que las horas asignadas estan dentro de las horas disponibles del profesor y fuera de las horas ya asignadas del profesor
                 for (Hora hora : horasQueAsignarle)
                 {       //Si no contiene las horas que se desean asignar en las horas disponibles del profesor o si las horas que se desean asignar existen en las horas ya asignadas:
                         if (!(profesorAAsignarle.getHorasDispArrayList().contains(hora)))
                         {       //Lanzo excepcion de horario no compatible
                                 throw new HoraNoDisponibleException(HoraNoDisponibleException.TOPE_HORAS_DISP_PROFE, "Tope horario con las horas disponibles del profesor");
                         }
                         if (profesorAAsignarle.getHorasAsigArrayList().contains(hora))
                         {       //Lanzo excepcion de horario no compatible
                                 throw new HoraNoDisponibleException(HoraNoDisponibleException.TOPE_HORAS_OCUP_PROFE, "Tope horario con  las horas ya asignadas del profesor");
                         }
                 }
                 
                 //Compruebo que las horas asignadas no coinciden con horas asignadas de cursos del mismo semestre
                 for (Hora hora : horasQueAsignarle)
                 {       for (Curso cursoDelSemestre : cursoAEditar.getEnSemestre().getCursosArrayList())
                         {       if (cursoDelSemestre.getHorasAsigArrayList().contains(hora))
                                 {       //Lanzo excepcion de tope horario
                                         throw new HoraNoDisponibleException(HoraNoDisponibleException.TOPE_NIVEL, "Tope horario con cursos del mismo semestre");
                                 }
                         }
                 }
 
                 if (comprobarHorarioSemestreAnterior)
                 {   //Compruebo que las horas asignadas idealmente no coinciden con horas asignadas de cursos del nivel siguiente
                     for (Hora hora :horasQueAsignarle)
                     {       for (Semestre semestreDeCarrera : cursoAEditar.getEnCarrera().getListaSemestres())
                             {       if (semestreDeCarrera.getNumeroSemestre() == cursoAEditar.getEnSemestre().getNumeroSemestre()+1)
                                     {       for (Curso cursoDelSemestreSiguiente : semestreDeCarrera.getCursosArrayList())
                                             {       if (cursoDelSemestreSiguiente.getHorasAsigArrayList().contains(hora))
                                                     {       //Lanzo excepcion de tope horario
                                                             throw new HoraNoDisponibleException(HoraNoDisponibleException.TOPE_NIVEL_SIG,"Tope horario con cursos del semestre siguiente");
                                                     }
                                             }
                                     }
                                     break;
                             }
                     }
                 }
                 if (comprobarHorarioSemestreSiguiente)
                 {   //compruebo que las horas asignadas idealmente no coinciden con horas asignadas de cursos del nivel anterior
                     for (Hora hora :horasQueAsignarle)
                     {       for (Semestre semestreDeCarrera : cursoAEditar.getEnCarrera().getListaSemestres())
                             {       if (semestreDeCarrera.getNumeroSemestre() == cursoAEditar.getEnSemestre().getNumeroSemestre()-1)
                                     {       for (Curso cursoDelSemestreSiguiente : semestreDeCarrera.getCursosArrayList())
                                             {       if (cursoDelSemestreSiguiente.getHorasAsigArrayList().contains(hora))
                                                     {       //Lanzo excepcion de tope horario
                                                             throw new HoraNoDisponibleException(HoraNoDisponibleException.TOPE_NIVEL_ANT, "Tope horario con cursos del semestre anterior");
                                                     }
                                             }
                                     }
                                     break;
                             }
                     }
                 }
 
                 
                 //Antes almaceno los valores antiguos del profesor asignado que posea el curso, si es que tenia uno ya asignado
                 if (cursoAEditar.getProfeAsig() != null)
                 {       //quito las referencias al curso que posee el profesor que tiene asignado de antes
                         Profesor antiguoProfeAsig = cursoAEditar.getProfeAsig();
                         for (Hora hora : cursoAEditar.getHorasAsigArrayList())
                         {       antiguoProfeAsig.modHorasAsignadas(hora, -1);
                         }
                 }
 
                 cursoAEditar.setProfesor(profesorAAsignarle);
                 //en caso que lo que se haya modificado es desAsignar un profesor:
                 if (profesorAAsignarle != null)
                 {       //agrego el curso a los cursos asignados del profesor
                         profesorAAsignarle.modCursosAsignados(cursoAEditar, 1);
                         //Si no ocurrió ningun problema con las condiciones para modificar el curso, se modifica:
                         for (Hora hora : horasQueAsignarle)
                         {       cursoAEditar.modHorario(hora, 1);
                                 profesorAAsignarle.modHorasAsignadas(hora, 1);
                         }
                 }
         }
 
         /**
          * Edita los atributos de una carrera y ademas modifica los objetos que dependan de la carrera
          * Cambia el nombre de la carrera
          * Cambia la descripción de la carrera
          * cambia la cantidad de semestres de la carrera:
          * si la nueva cantidad de semestres es mayor a la existente, crea nuevos objetos Semestre y los referencia
          * si la nueva cantidad de semestres es menor a la existente, elimina los semestres finales y los cursos que posean estos semestres
          * @param carreraAEditar 
          * @param nuevoNombre Corresponde al nuevo nombre que tendrá la  carrera.
          * @param nuevaDescrip Corresponde a la nueva descripción de la carrera.
          * @param nuevaCantidadSemestres Corresponde a la nueva cantidad de semestres que poseerá la carrera.
          * @throws nombreRepetidoException Se lanza está excepción cuando el nuevo nombre de la carrera que se desea editar se encuentra repetido con alguna de las demás carreras.
          * @throws StringVacioException Se lanza esta excepción cuando el nuevo nombre de la carrera que se desea editar es un string vacio.
          */
         public void editaCarrera(Carrera carreraAEditar, String nuevoNombre, String nuevaDescrip, int nuevaCantidadSemestres) throws nombreRepetidoException, StringVacioException
         {       //Compuebo que el nuevo nombre no sea String vacio
                 if (nuevoNombre.trim().equals(""))
                 {       //Lando Excepcion
                         throw new StringVacioException();
                 }
 
                 //Si modifico el nombre de la carrera, me aseguro que no hayan otras carreras con el nombre nuevo
                 if (!(carreraAEditar.getNombreCarrera().equals(nuevoNombre)))
                 {       for (Carrera carrera : this.listaCarreras)
                         {       if (carrera.getNombreCarrera().equals(nuevoNombre))
                                 {       //lanzo excepcion
                                         throw new nombreRepetidoException();
                                 }
                         }
                 }
 
                 //seteo el nuevo nombre y descripción
                 carreraAEditar.setDescripcion(nuevaDescrip);
                 carreraAEditar.setNombre(nuevoNombre);
                 int cantSemestresActuales = carreraAEditar.getIdSemestresArrayList().size();
                 //Si la nueva cantidad de semestres es la misma, dejo todo igual
                 //si la nueva cantidad de semestres es mayor, creo semestres nuevos y se los asigno a la carrera
                 if (cantSemestresActuales < nuevaCantidadSemestres)
                 {       int i;
                         Semestre semestreNuevo;
                         for (i = cantSemestresActuales; i < nuevaCantidadSemestres; i++)
                         {       semestreNuevo = new Semestre(i+1, carreraAEditar);
                                 carreraAEditar.modSemestres(semestreNuevo, 1);
                                 this.listaSemestres.add(semestreNuevo);
                         }
                 }
                 //Si la nueva cantidad de semestres es menor, elimino los ultimos semestres de la carrera y sus cursos
                 else if (carreraAEditar.getIdSemestresArrayList().size() > nuevaCantidadSemestres)
                 {       ArrayList<Semestre> listaSemestresAEliminar = new ArrayList(carreraAEditar.getListaSemestres().subList(nuevaCantidadSemestres, cantSemestresActuales));
                         for (Semestre semestre : listaSemestresAEliminar)
                         {       this.listaSemestres.remove(semestre);//< Acá elimino los semestres de la lista de semestres
                                 this.listaCursos.removeAll(semestre.getCursosArrayList()); //<Acá elimino los cursos que pertenecen a los semestres eliminados
                                 carreraAEditar.modSemestres(semestre, -1); //Acá elimino las referencias de los semestres eliminados que existan en la carrera.
                         }
 
                 }
         }
         /**
          * Este método elimina un curso desde la lista de cursos
          * Quita las referencias existentes del curso que existan en los semestres de la lista de semestres
          * Quita las referencias existentes del curso que existan en el profesor que tenga asignado
          * @param cursoABorrar el objeto Curso que se quiere eliminar de las listas
          */
         public void eliminaCurso(Curso cursoABorrar)
         {       //Quito el curso a eliminar de la lista de cursos
                 this.listaCursos.remove(cursoABorrar);
 
                 //Quito las referencias al curso que puedan existir en los semestres
                 cursoABorrar.getEnSemestre().modRamos(cursoABorrar, -1);
 
                 //Quito las referencias al curso que puedan existir en los profesores que tenga asignados
                 for (Profesor profesor : this.listaProfesores)
                 {       if (cursoABorrar.getIdProfeAsig() == profesor.getIdProfesor())
                         {       profesor.modCursosAsignados(cursoABorrar, -1);
                                 break ;
                         }
                 }
         }
         /**
          * Este metodo crea un objeto Curso y lo agrega a la lista de cursos,
          * ademas referencia al curso en la lista de cursos del semestre al cual pertenece
          * @param nombreCurso es el nombre del curso nuevo.
          * @param codCurso es el codigo del curso nuevo.
          * @param seccion es la seccion del curso nuevo, un String, por ejemplo: A01.
          * @param carreraAlQuePertenece es un objeto Carrera con la carrera a la cual pertenece el curso.
          * @param semestreAlQuePertenece es un objeto Semestre con el semestre al cual pertenece el curso.
          * @throws nombreRepetidoException Se lanza esta excepción cuando el nombre del curso se encuentra repetido con algún nombre de los demás cursos.
          * @throws StringVacioException Se lanza está excepción cuando el nombre o la sección son Strings Vacios
          * Se lanzan con un codigo de error especificado en los atributos estaticos de está clase: ERROR_NOMBRE o ERROR_SECCION
          * @throws NullPointerException Se lanza esta excepción cuando se intenta agregar un curso sin que existan semestres y por ende carreras.
          * @throws NumberFormatException Se lanza esta excepción cuando el codigo de curso no es un número.
          */
         public void agregaCurso(String nombreCurso, String codCursoStr, String seccion, Carrera carreraAlQuePertenece, Semestre semestreAlQuePertenece) throws nombreRepetidoException, StringVacioException, NumberFormatException, NullPointerException
         {       int codCurso;
                 //Compruebo que existan carreras y semestres disponibles para agregar un curso
                 if (this.listaSemestres.size() == 0)
                 {       throw new NullPointerException();
                 }
                 //Compruebo que el campo "nombre del curso"  no sea un string vacio
                 if (nombreCurso.trim().equals(""))
                 {       throw new StringVacioException();
                 }
                 
                 //Compruebo el campo de el codigo de curso si es válido
                 try
                 {       codCurso = Integer.valueOf(codCursoStr);
                 }
                 catch (NumberFormatException NFE)
                 {       throw NFE;
                 }
 
 
                 //Compruebo que el código de curso leido no se encuentra répetido entre los otros cursos, a excepción de cursos con el mismo nombre.
                 for (Curso curso : this.listaCursos)
                 {       if ((curso.getCodigoCurso() == codCurso) && !(curso.getNombreCurso().equals(nombreCurso)))
                         {       //Lanzo excepcion de nombre repetido con codigo 1
                                 throw new nombreRepetidoException(1);
                         }
                         //if ((curso.getCodigoCurso() == codCurso) && (curso.getNombreCurso().equals(nombreCurso)) && (curso.getEnCarrera().equals(carreraAlQuePertenece)))
                         //{       //Lanzo excepcion de nombre repetido con codigo 1
                         //        throw new nombreRepetidoException(2);
                         //}
                         //Si el codigo de curso es igual, el nombre es igual y la seccion es igual, lanzo dialogo de error.
                         if ((curso.getCodigoCurso() == codCurso) && (curso.getNombreCurso().equals(nombreCurso)) && (seccion.equals(curso.getSeccion())))
                         {       //Lanzo excepcion de nombre repetido con codigo 2
                                 throw new nombreRepetidoException(2);
                         }
 
                         if ((((curso.getCodigoCurso() == codCurso) && !(curso.getNombreCurso().equals(nombreCurso))) || (!(curso.getCodigoCurso() == codCurso) && (curso.getNombreCurso().equals(nombreCurso)))) && (curso.getEnCarrera() == carreraAlQuePertenece))
                         {       //Lanzo excepcion de nombre repetido con codigo 3
                                 throw new nombreRepetidoException(4);
                         }
                         //Si el codigo de curso o el nombres son iguales, en la misma carrera, lanzo dialogo de error
                         if (((curso.getCodigoCurso() == codCurso) || (curso.getNombreCurso().equals(nombreCurso))) && (curso.getEnCarrera() == carreraAlQuePertenece) && !(semestreAlQuePertenece.equals(curso.getEnSemestre())))
                         {       //Lanzo excepcion de nombre repetido con codigo 3
                                 throw new nombreRepetidoException(4);
                         }
                         if (((curso.getCodigoCurso() == codCurso) || (curso.getNombreCurso().equals(nombreCurso))) && (curso.getEnCarrera() == carreraAlQuePertenece) && (semestreAlQuePertenece.equals(curso.getEnSemestre()) && (seccion.equals(curso.getSeccion()))))
                         {       //Lanzo excepcion de nombre repetido con codigo 3
                                 throw new nombreRepetidoException(3);
                         }
                 }
 
                 //Creo el nuevo objeto curso y los agrego a la lista de cursos.
                 Curso cursoNuevo = new Curso(nombreCurso, codCurso);
                 cursoNuevo.modIdSemestre(semestreAlQuePertenece.getIdSemestre());
                 cursoNuevo.setSemestre(semestreAlQuePertenece);
                 cursoNuevo.setCarrera(carreraAlQuePertenece);
                 cursoNuevo.setSeccion(seccion);
                 this.listaCursos.add(cursoNuevo);
 
                 //Referencio el curso en la lista de cursos del semestre al cual pertenece
                 semestreAlQuePertenece.modRamos(cursoNuevo, 1);
         }
         /**
          * Crea y agrega un objeto Facultad a la lista de facultades.
          * @param nombreFacultad El string con el nombre de la facultad que se está creando.
          * @param descripcion El string con la descripción de la facultad que se está creando.
          * @throws StringVacioException Se lanza está excepción cuando el nombre de la facultad que se quiere crear es un string vacio.
          * @throws nombreRepetidoException Se lanza esta excepcion cuando el nombre de la facultad que se quiere crear está repetido con alguna de las demás facultades.
          */
         public void agregaFacultad(String nombreFacultad, String descripcion) throws StringVacioException, nombreRepetidoException
         {       if (nombreFacultad.trim().equals(""))
                 {       //Si el nombre de la facultad es un string vacio, lanzo excepcion
                         throw new StringVacioException();
                 }
 
                 //Si ya existe una facultad con el mismo nombre tampoco se agrega.
                 ArrayList<Facultad> listaCompletaFacultades = this.listaFacultades;
                 for (Facultad facultad : listaCompletaFacultades)
                 {       if (nombreFacultad.equals(facultad.getNombreFacultad()))
                         {       //lanzo error: ya existe una facultad con ese nombre
                                 throw new nombreRepetidoException();
                         }
                 }
                 Facultad facultadNueva = new Facultad(nombreFacultad);
                 facultadNueva.setDescripcion(descripcion);
                 this.listaFacultades.add(facultadNueva);
         }
 
         /**
          * Este método edita el nombre o descripción de la facultad que se pase como parametro
          * Se encarga de comprobar si el nuevo nombre que se le está dand oa la facultad se encuentra repetido con las demás facultades
          * @param facultadAEditar Es el objeto Facultad al cual se quiere editar sus atributos.
          * @param nuevoNombre Es el nuevo nombre que se le quiere setear a la facultad.
          * @param nuevaDescripcion Es la nueva descripción que se le quiere setear a la facultad.
          * @throws nombreRepetidoException Lanza esta excepción cuando el nuevo nombre de la facultad está repetido con alguno de las demás facultades.
          */
         public void editarFacultad(Facultad facultadAEditar, String nuevoNombre, String nuevaDescripcion) throws nombreRepetidoException
         {       //Compruebo que el nuevo nombre de la facultad no es igual al nombre de otra facultad
                 ArrayList<Facultad> listaCompletaFacultades = this.listaFacultades;
                 for (Facultad facultad : listaCompletaFacultades)
                 {       //si el nombre nuevo de la facultad es igual algun nombre de las facultades de la lista de facultades (A excepción de la facultad que se está modificando), lanzo dialogo de error.
                         if ((nuevoNombre.equals(facultad.getNombreFacultad())) && !(facultad.equals(facultadAEditar)))
                         {       //abro nueva ventana de error.
                                 throw new nombreRepetidoException();
                         }
                 }
 
                 //modifico la facultad
                 facultadAEditar.setNombre(nuevoNombre);
                 facultadAEditar.setDescripcion(nuevaDescripcion);
         }
 
         /**
          * Elimina una facultad de la lista de facultades
          * Elimina las carreras que posea esa facultad
          * Esto lo realiza llamando al metodo eliminaCarrera()
          * por lo tanto ademas se quitan las referencias que borra ese método.
          * @param facultadAEliminar Es el objeto Facultad que se desea eliminar de la lista de facultades
          */
         public void eliminaFacultad(Facultad facultadAEliminar)
         {       //Elimino todoas las carreras que pertenecen a esa facultad de la lista de carreras
                 ArrayList<Carrera> carrerasAEliminar = new ArrayList();
                 for (Carrera carrera : this.listaCarreras)
                 {       if (carrera.getFacultad().equals(facultadAEliminar))
                         {       carrerasAEliminar.add(carrera);
                         }
                 }
                 for (Carrera carrera : carrerasAEliminar) //Esto es para evitar un error en el acceso al ArrayList al ir eliminando elementos
                 {       this.eliminaCarrera(carrera.getCodigoCarrera());
                 }
                 
                 //Elimino la facultad de la lista de facultades
                 for (Facultad facultad :this.listaFacultades)
                 {       if (facultadAEliminar.equals(facultad))
                         {       this.listaFacultades.remove(facultad);
                                 break;
                         }
                 }
         }
 
         /**
          * Crea un objeto Profesor y lo agrega al sistema
          * Agrega el profesor a la lista de profesores
          * Setea el atributo de sus horas disponibles
          * @param nombreProfesor Es el string con el nombre del nuevo profesor
          * @param rut es el entero con el rut del profesor, sin dígito verificador
          * @param cursosDisponibles Es un Arraylist de Integer con los códigos de curso que puede dictar el profesor
          * @param horasDisponibles Es un ArrayList de Hora con las horas disponibles del profesor para hacer clases
          * @throws StringVacioException Lanza esta excepción cuando el nombre del profesor es un string vacio
          * @throws nombreRepetidoException Lanza esta excepción cuando el nombre del profesor está repetido entre los demas profesores
          * @throws NumberFormatException Lanza esta excepción cuando el codigo de curso no es un número o cuando el rut no es válido
          * para reconocer la diferencia, el método getMessage() de NumberFormatException contiene el String "rut" si el problema está en el rut
          * o el String "codCurso" si el problema fue originado por el codigo de curso incorrecto.
          * @throws HourOutOfRangeException Lanza esta excepción cuando las horas disponibles introducidas no son válidas
          */
         public void agregaProfesor(String nombreProfesor, String rutStr, String cursosDisponibles, String horasDisp) throws StringVacioException, nombreRepetidoException, NumberFormatException, HourOutOfRangeException
         {       int rut, codCurso, i, posicionEspacio;
                 ArrayList<Hora> listaHorasDisponibles = new ArrayList();
                 ArrayList<Integer> listaCodCursosDisponibles = new ArrayList();
                 //Si el nombre es un string vacio no se agrega y lanzo excepcion
                 if (nombreProfesor.trim().equals(""))
                 {       //Lanzo excepción
                         throw new StringVacioException();
                 }
 
                 //Si ya existe un profesor con el mismo nombre tampoco se agrega.
                 for (Profesor profesor : this.listaProfesores)
                 {       if (nombreProfesor.equals(profesor.getNombreProfesor()))
                         {       //Lanzo excepcion
                                 throw new nombreRepetidoException();
                         }
                 }
 
                 //Compruebo si el rut es válido
                 try
                 {       rut = Integer.valueOf(rutStr); //Esto puede lanzar excepcion por no ser numero
                         if ((rut < 2000000) || (rut > 25000000))
                             throw new NumberFormatException("rut"); //hago que lanze excepcion por no estar en el rango
                 }
                 catch (NumberFormatException NFE)
                 {       throw new NumberFormatException("rut");
                 }
                 //Transformo el string con los cursos que puede dictar a un ArrayList de Integer
                 try
                 {       //String codCursosDisp = this.campoRamosQueDicta.getText();
                         if (cursosDisponibles.length() != 0) //Seteo los codigos de curso que puede impartir
                         {       for (i = 0; cursosDisponibles.indexOf(" ") != -1;i++)
                                 {       System.out.println(cursosDisponibles.substring(0, cursosDisponibles.indexOf(" ")));
                                         codCurso = Integer.valueOf(cursosDisponibles.substring(0, cursosDisponibles.indexOf(" ")));
                                         posicionEspacio = cursosDisponibles.indexOf(" ");
                                         cursosDisponibles = cursosDisponibles.substring(posicionEspacio+1);
                                         listaCodCursosDisponibles.add(new Integer(Integer.valueOf(codCurso)));
                                 }
                                 //Agrego el ultimo que no fue agregado en el bucle:
                                 listaCodCursosDisponibles.add(new Integer(Integer.valueOf(cursosDisponibles)));
                         }
                 }
                 catch (NumberFormatException NFE)
                 {   throw new NumberFormatException("codCurso"); 
                 }
 
                 //Transformo el string de horas a un ArrayList de Hora, estó puede lanzar HourOutOfRangeException
                 Hora objHora;
                 if (horasDisp.length() != 0)
                 {       for (i = 0; horasDisp.indexOf(" ") != -1;i++)
                         {       System.out.println(horasDisp.substring(0, horasDisp.indexOf(" ")));
                                 objHora = new Hora(horasDisp.substring(0, horasDisp.indexOf(" ")));
                                 posicionEspacio = horasDisp.indexOf(" ");
                                 horasDisp = horasDisp.substring(posicionEspacio+1);
                                 listaHorasDisponibles.add(objHora);
                         }
                         //Agrego el ultimo que no fue agregado en el bucle:
                         listaHorasDisponibles.add(new Hora(horasDisp));
                 }
 
                 Profesor profesorNuevo = new Profesor(nombreProfesor, rut, listaCodCursosDisponibles);
                 for (Hora hora : listaHorasDisponibles)
                         profesorNuevo.modHorasDisponibles(hora, 1);
                 this.listaProfesores.add(profesorNuevo);
         }
 
         /**
          * Elimina un profesor del sistema
          * Elimina las referencias al profesor que puedan existir en los cursos.
          * @param profeABorrar Es el Objeto Profesor que se desea eliminar de la lista de profesores
          */
         public void eliminaProfesor(Profesor profeABorrar)
         {       //Elimino al profesor de la lista de profesores
                 this.listaProfesores.remove(profeABorrar);
                 //elimino las rederencias al profesor que puedan existir en sus cursos asignados
                 for (Curso curso : this.listaCursos)
                 {       if (curso.getIdProfeAsig() == profeABorrar.getIdProfesor())
                                 {       curso.setProfesor(null);
                                 }
                 }
         }
 
         /**
          * Este método se encarga de agregar una carrera a la lista de carreras
          * Setea sus atributos
          * Crea los semestres que debe poseer la nueva carrera
          * Referencia la carrera a la facultad a la que pertenece
          * @param nombreCarrera Nombre de la carrera que se desea agregar
          * @param facultadALaQuePertenece Objeto Facultad a la que va a pertenecer esta nueva carrera
          * @param descripcion La descripción de la carrera.
          * @param cantidadSemestres El número de semestres que tendrá la carrera
          * @throws StringVacioException Lanza esta excepción cuando el nombre de carrera es un string vacio
          * @throws nombreRepetidoException Lanza esta excepcion cuando el nombre de la carrera está repetido entre las demas carreras
          */
         public void agregaCarrera(String nombreCarrera, Facultad facultadALaQuePertenece, String descripcion, int cantidadSemestres) throws StringVacioException, nombreRepetidoException
         {       //si lo que se ha escrito es nada, entonces no se agrega la carrera
                 if (nombreCarrera.trim().equals(""))
                 {       //Lando Excepcion
                         throw new StringVacioException();
                 }
 
                 //Si ya existe una carrera con el mismo nombre tampoco se agrega.
                 ArrayList<Carrera> listaCompletaCarreras = this.listaCarreras;
                 for (Carrera carrera : listaCompletaCarreras)
                 {       if (nombreCarrera.equals(carrera.getNombreCarrera()))
                         {       //lanzo excepcion
                                 throw new nombreRepetidoException();
                         }
                 }
 
                 int i;
                 Carrera carreraNueva = new Carrera(nombreCarrera);
                 Semestre semestreNuevo;
                 carreraNueva.setFacultad(facultadALaQuePertenece);
                 carreraNueva.setDescripcion(descripcion);
                 facultadALaQuePertenece.modListaCarreras(carreraNueva, 1);
                 //Agrego los semestres que posea, pero sin cursos.
                 for (i = 0; i < cantidadSemestres; i++)
                 {       semestreNuevo = new Semestre(i+1, carreraNueva);
                         carreraNueva.modSemestres(semestreNuevo, 1);
                         this.listaSemestres.add(semestreNuevo);
                 }
 
                 this.listaCarreras.add(carreraNueva);
         }
 
         /**
          * Este método elimina una carrera de la lista de carreras del sistema buscandolo por su codigo de carrera
          * Elimina las referencias a esa carrera que puedan existir en las facultades.
          * Elimina los semestres que posee la carrera eliminada de la lista de semestres.
          * Elimina las referencias de los semestres eliminados que existan en los cursos.
          * Elimina las referencias de la carreras eliminada que existan en los cursos.
          * @param codCarreraAEliminar Es el codigo de la carrera aque se desea eliminar del sistema
          */
         public void eliminaCarrera(int codCarreraAEliminar)
         {       //elimino la carrera desde la lista de carreras
                 Carrera carreraAEliminar = null;
                 for (Carrera carrera : this.listaCarreras)
                 {       if (carrera.getCodigoCarrera() == codCarreraAEliminar)
                         {       carreraAEliminar = carrera;
                                 this.listaCarreras.remove(carrera);
                                 break;
                         }
 
                 }
                 //elimino la carrera desde las referencias de la facultad a la que pertenece
                 for (Facultad facultad : this.listaFacultades)
                 {       facultad.modListaCarreras(carreraAEliminar, -1); //borro todas las referencias de esa carrera de las facultades, si no existe, se muestra una advertencia
                 }
 
                 ArrayList<Semestre> semestresAEliminar = carreraAEliminar.getListaSemestres();
                 ArrayList<Curso> cursosAEliminar = new ArrayList();
                 //Elimino los cursos que pertenecen a la carrera
                 for (Semestre semestre : semestresAEliminar)
                 {       for (Curso curso : semestre.getCursosArrayList())
                         {       this.listaCursos.remove(curso);
                                 cursosAEliminar.add(curso);
                         }
                 }
                 //elimino las referencias de los cursos eliminados que existan en los profesores
                 for (Curso curso : cursosAEliminar)
                 {       for (Profesor profesor : this.listaProfesores)
                         {       if (profesor.getIdCursosAsignadosArrayList().contains(new Integer(curso.getIdCurso())))
                                         profesor.modCursosAsignados(curso, -1);
                         }
                 }
 
                 //elimino los semestres que posee la carrera eliminada de la lista de semestres
                 for (Semestre semestre : semestresAEliminar)
                 {       this.listaSemestres.remove(semestre);
                 }
         }
 
 }
