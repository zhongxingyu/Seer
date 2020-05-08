 package py.com.ait.gestion.constant;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public final class Definiciones {
 
 	public static class Operacion
 	{
 		public static final String Insert="I";
 		public static final String Update="U";
 		public static final String Delete="D";
 		
 	}
 
 	public static class TiposRegistros
 	{
 		public static final String Nuevo="Registro Nuevo";
 		public static final String Borrado="Registro Borrado";
 	}
 
 	public static final String CadenaVacia = "";
 	public static final String Separador = "#";
 
 	public static class EstadoProceso
 	{
 		public static final String EnProceso="PRO";
 		public static final String Resuelto="RES";
 		public static final String Cancelado="CAN";
 		
 		public static List<Estado> getEstadosList() {
 			
 			List<Estado> list = new ArrayList<Estado>();			
 			list.add(new Estado(EnProceso, "En Proceso"));
 			list.add(new Estado(Resuelto, "Resuelto"));
 			list.add(new Estado(Cancelado, "Cancelado"));
 			
 			return list;
 		}
 		
 		public static String getEstadosCerrados() {
 			
 			return ("'" + Resuelto + "','" + Cancelado + "'");			
 		}
 		
 	}
 	
 	public static class EstadoActividad
 	{
 		public static final String Nueva="NUE";
 		public static final String EnProceso="PRO";
 		public static final String Resuelta="RES";
 		public static final String Devuelta="DEV";
 		public static final String Cancelada="CAN";
 		
 		public static List<Estado> getList() {
 			
 			List<Estado> list = new ArrayList<Estado>();
 			list.add(new Estado(Nueva, "Nueva"));
 			list.add(new Estado(EnProceso, "En Proceso"));
 			list.add(new Estado(Resuelta, "Resuelta"));
 			list.add(new Estado(Devuelta, "Devuelta"));
 			list.add(new Estado(Cancelada, "Cancelada"));
 			
 			return list;
 		}
 		
 		public static List<Estado> getEstadosActividad() {
 			
 			List<Estado> list = new ArrayList<Estado>();
 			list.add(new Estado(Nueva, "Nueva"));
 			list.add(new Estado(EnProceso, "En Proceso"));
 //			list.add(new Estado(Cancelada, "Cancelada"));
 			
 			return list;
 		}
 		
 		public static List<Estado> getEstadosSubActividad() {
 			
 			List<Estado> list = new ArrayList<Estado>();
 			list.add(new Estado(Nueva, "Nueva"));
 			list.add(new Estado(EnProceso, "En Proceso"));
 			list.add(new Estado(Cancelada, "Cancelada"));
 			list.add(new Estado(Resuelta, "Resuelta"));
 			
 			return list;
 		}
 	}
 	
 	public static class Estado {
 		
 		private String codigo;
 		private String descripcion;
 		
 		public Estado() { }
 		
 		public Estado(String codigo, String descripcion) {
 			
 			this.codigo = codigo;
 			this.descripcion = descripcion;
 		}
 		
 		public String getCodigo() {
 			return this.codigo;
 		}
 		
 		public String getDescripcion() {
 			return this.descripcion;
 		}
 		
 		public void setCodigo(String codigo) {
 			this.codigo = codigo;
 		}
 		
 		public void setDescripcion(String descripcion) {
 			this.descripcion = descripcion;
 		}
 	}
 	
 	public static List<Estado> getSiNoList() {
 		
 		List<Estado> list = new ArrayList<Estado>();
 		list.add(new Estado("SI", "SI"));
 		list.add(new Estado("NO", "NO"));
 		
 		return list;
 	}
 	
 	public static class EstadoNotificacion
 	{
 		public static final String Activo="ACT";
 		public static final String Inactivo="INA";
 		public static final String Notificado="NOT";
 	}
 	
 	public static class TipoNotificacion
 	{
 		public static final String AlertaInicio="ALE_ACT_INICIO";
 		public static final String AlertaReprogramacionProceso="ALE_PRO_REPROGRAMACION";
 		public static final String AlertaActividad="ALE_ACT";
 		public static final String AlarmaActividad="ALA_ACT";
 		public static final String AlertaActividadFinalizada="ALE_ACT_FINALIZADA";
 		public static final String AlertaSubActividadFinalizada="ALE_ACT_SUB_FINALIZADA";
 		public static final String AlertaSubActividadNueva="ALE_ACT_SUB_NUEVA";
 	}
 }
