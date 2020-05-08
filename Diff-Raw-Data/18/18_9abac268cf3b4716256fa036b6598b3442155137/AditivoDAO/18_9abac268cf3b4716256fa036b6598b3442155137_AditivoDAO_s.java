 
 package persistencia;
 
 import logica.Acabado;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import excepciones.DAOExcepcion;
 
 import logica.Acabado;
 import logica.Aditivo;
 import logica.Base;
 
 
 public class AditivoDAO implements IAditivoDAO{
 	private ConnectionManager connManager;
 	public AditivoDAO() throws DAOExcepcion{
 		super();
 		// TODO Auto-generated constructor stub
 		try{
 		connManager= new ConnectionManager("CALCPIGMENTOS");
 		}catch (ClassNotFoundException e){
 			throw new DAOExcepcion("DB_CONNECT_ERROR");
 		}
 }
 
     @Override
     public ArrayList<Aditivo> getAditivos() throws DAOExcepcion {
     ArrayList<Aditivo> aditivos = new ArrayList<Aditivo>();
     
 		try{
 			connManager.connect();
 			ResultSet rs=connManager.queryDB("select * from ADITIVOS");
 			connManager.close();
 			try {
 				
 				while (rs.next()){
 					String nombre = rs.getString("NOMBRE");
                                         Aditivo aditivo=new Aditivo(nombre);
 					aditivos.add(aditivo);
 		
 				}
 				}catch (SQLException e){
 					throw new DAOExcepcion("DB_READ_ERROR");
 				}
 			
 			}catch (DAOExcepcion e){
 				throw e;
 			}
 			return aditivos;
     }
 
     @Override
     public ArrayList<Aditivo> getAditivos(int idb) throws DAOExcepcion {
          ArrayList<Aditivo> aditivos = new ArrayList<Aditivo>();
                 
 		try{
                     
 			connManager.connect();
 			ResultSet rs=connManager.queryDB("select * from ADITIVOS_CANTIDAD WHERE IDB="+idb);
 			connManager.close();
                         
                         
 			try {
 				
 				while (rs.next()){
 
 					Aditivo aditivo = new Aditivo(rs.getString("NOMBRE"));
 					aditivo.setCantidad(rs.getDouble("CANTIDAD"));
                                         aditivos.add(aditivo);
 		
 				}
 				}catch (SQLException e){
 					throw new DAOExcepcion("DB_READ_ERROR");
 				}
 			
 			}catch (DAOExcepcion e){
 				throw e;
 			}
                 
 			return aditivos;
 
     }
 
     @Override
     public void insertarAditivo(Aditivo aditivo) throws DAOExcepcion {
     try {
 			this.connManager.connect();
 			this.connManager.updateDB("insert into ADITIVOS (NOMBRE) values ('"+
 					aditivo.getNombre()
 					+ "')");
 			this.connManager.close();
 
 		} catch (DAOExcepcion e) {
 			System.out.println("ERROR EN DAO");
 		}}
 
     @Override
     public void modificarAditivoNombre(String nombreAntes,String nombreAhora) throws DAOExcepcion {
         try{
 			connManager.connect();
 			connManager.updateDB("UPDATE ADITIVOS SET NOMBRE = '" + nombreAhora+ 
                         "'WHERE NOMBRE = " + nombreAntes);
 			connManager.close();
 
 		}catch (DAOExcepcion e){
 			e.printStackTrace();
 		}
      }
 
     @Override
     public void eliminarAditivo(Aditivo aditivo) throws DAOExcepcion {
     try{
 			connManager.connect();
 			connManager.updateDB("DELETE FROM ADITIVOS_CANTIDAD WHERE NOMBRE = '" + aditivo.getNombre()+"'");
 			connManager.updateDB("DELETE FROM ADITIVOS WHERE NOMBRE = '" + aditivo.getNombre()+"'");
 			connManager.close();
 
 		}catch (DAOExcepcion e){
 			e.printStackTrace();
 		}}
 
     @Override
     public void asociarAditivo(Base base, Aditivo aditivo) throws DAOExcepcion {
       try {
 			this.connManager.connect();
 			this.connManager.updateDB("insert into ADITIVOS_CANTIDAD (CANTIDAD,IDB,NOMBRE) values ("+
 					aditivo.getCantidad()+","+
                                         base.getId()+",'"+
                                         aditivo.getNombre()
 					+ "')");
 			this.connManager.close();
 
 		} catch (DAOExcepcion e) {
 			System.out.println("ERROR EN DAO");
 		}}
 
     @Override
     public void eliminaAsociacionAditivo(Base base, Aditivo aditivo) throws DAOExcepcion {
         connManager.connect();
 			connManager.updateDB("DELETE FROM ADITIVOS_CANTIDAD WHERE NOMBRE = " + aditivo.getNombre()+" AND"+
                                                 "IDB="+base.getId());
 			connManager.close();
     }
 
     @Override
     public Aditivo getAditivo(String nombre) throws DAOExcepcion {
        try{
 			connManager.connect();
 			ResultSet rs=connManager.queryDB("select * from ADITIVOS where NOMBRE= '"+nombre+"'");						
 			connManager.close();
 			try{
 				if (rs.next()){
 					return new Aditivo(rs.getString("NOMBRE"));
 				}
 				else
 					return null;
 			}catch (SQLException e){
 				throw new DAOExcepcion("DB_READ_ERROR");
 			}
 			
 			}catch (DAOExcepcion e){
 				throw e;
 			}
     }
 
     @Override
     public ArrayList<Aditivo> getAditivosNoAsignados(int idb) throws DAOExcepcion {
       ArrayList<Aditivo> aditivos = new ArrayList<Aditivo>();
                 
 		try{
                     
 			connManager.connect();
			ResultSet rs=connManager.queryDB("select * from ADITIVOS ad where NOT EXISTS(Select * from "
                                + "ADITIVOS_CANTIDAD adc WHERE IDB="+idb+" and ad.NOMBRE=adc.NOMBRE");
 			connManager.close();
                         
                         
 			try {
 				
 				while (rs.next()){
 
 					Aditivo aditivo = new Aditivo(rs.getString("NOMBRE"));
 					//aditivo.setCantidad(rs.getDouble("CANTIDAD"));
                                         aditivos.add(aditivo);
 		
 				}
 				}catch (SQLException e){
 					throw new DAOExcepcion("DB_READ_ERROR");
 				}
 			
 			}catch (DAOExcepcion e){
 				throw e;
 			}
                 
 			return aditivos;
 
     }
 
 }
