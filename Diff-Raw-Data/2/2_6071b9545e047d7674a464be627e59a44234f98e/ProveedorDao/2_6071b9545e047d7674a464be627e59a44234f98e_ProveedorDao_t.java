 package com.clase.dao;
 
 import java.util.ArrayList;
 
 
 import com.clase.models.Proveedor;
 
 public class ProveedorDao {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 
 	}
 	
 	public ArrayList<Proveedor> getLista() {
 		ArrayList<Proveedor> Lista = new ArrayList<Proveedor>();
 		String q = "select * from proveedor order by id_proveedor;";
 
 		ConnDB cx = new ConnDB();
 		try {
 			cx.consulta(q);
 			while (cx.getNext()) {
 				Proveedor myProv = new Proveedor();
 				myProv.setIdProveedor(cx.getInt("id_proveedor"));
 				myProv.setTipoProveedor(cx.getInt("id_tipo_proveedor"));
 				myProv.setNombre(cx.getString("nombre"));
 				myProv.setContacto(cx.getString("contacto"));
 				myProv.setTelefono(cx.getString("telefono"));
 				myProv.setDirPostal(cx.getString("direccion_postal"));
 				myProv.setPagWeb(cx.getString("pagina_web"));
 				myProv.setRuc(cx.getString("ruc"));
 				myProv.seteMail(cx.getString("e_mail"));
 				myProv.setEstado(cx.getString("estado"));
 				
 				Lista.add(myProv);
 			}
 			cx.cleanup();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return Lista;
 	}
 	
 	public Proveedor findByID(int idProv) {
 		Proveedor myProv = new Proveedor();
 		ConnDB cx = new ConnDB();
 
 		String sql = "Select * from proveedor where id_proveedor=?;";
 
 		try {
 			cx.Prepare(sql);
 			cx.setInts(1, idProv);
 
 			cx.executestmt();
 
 			while (cx.getNext()) {
				myProv.setTipoProveedor(cx.getInt("id_tipo_proveedor"));
 				myProv.setNombre(cx.getString("nombre"));
 				myProv.setContacto(cx.getString("contacto"));
 				myProv.setTelefono(cx.getString("telefono"));
 				myProv.setDirPostal(cx.getString("direccion_postal"));
 				myProv.setPagWeb(cx.getString("pagina_web"));
 				myProv.setRuc(cx.getString("ruc"));
 				myProv.seteMail(cx.getString("e_mail"));
 				myProv.setEstado(cx.getString("estado"));
 				
 				
 			}
 			cx.cleanup();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return myProv;
 	}
 
 }
