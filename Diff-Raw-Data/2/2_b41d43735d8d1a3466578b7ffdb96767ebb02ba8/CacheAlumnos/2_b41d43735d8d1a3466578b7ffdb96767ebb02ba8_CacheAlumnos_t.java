 package Ajax;
 
 	import java.util.HashMap;
 
 
 
 	public class CacheAlumnos {
 
 		
 		
 		
 		private static CacheAlumnos instancia;
 		private HashMap<String,Alumno> listaAlumnos=new HashMap<String,Alumno>();
 		private HashMap<String,Materia> listaMaterias = new HashMap<String, Materia>();
 
 		
 		private CacheAlumnos()
 		{
 			Materia m1 = new Materia();
 			m1.setCodigo(1);
 			m1.setNombre("Educacion Fisica");
 			
 			Materia m2 = new Materia();
 			
 			m2.setCodigo(2);
 			m2.setNombre("Programacion");
 			
 			listaMaterias.put("educ fisica",m1);
 			listaMaterias.put("Programacion",m2);
 		
 		
 			Alumno a1 = new Alumno("pepe","lopez");
			Alumno a2= new Alumno("pancho","gimenez");
 			
 			a1.setListaMaterias(listaMaterias);
 			a2.setListaMaterias(listaMaterias);
 			
 			listaAlumnos.put("pepe",a1);
 			listaAlumnos.put("pancho",a2);
 			
 			
 		
 		}
 		
 		public static CacheAlumnos getInstance()
 		{
 			if(instancia == null)
 			{
 				
 			instancia=new CacheAlumnos();	
 			}
 			return instancia;
 		}
 
 
 		public Alumno ObtenerAlumno(String nombre)
 		{
 			
 			Alumno a = new Alumno();
 			a=(Alumno)this.listaAlumnos.get(nombre);
 			
 		
 			
 			return a; 
 		}
 		
 
 
 		
 	}
 
 
 
