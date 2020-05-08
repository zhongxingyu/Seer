 package panchat.addressing;
 
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import javax.swing.table.AbstractTableModel;
 
 public class ListaUsuarios extends AbstractTableModel {
 
 	private static final long serialVersionUID = 1L;
 
 	private LinkedList<Usuario> listaUsuarios;
 
 	public ListaUsuarios() {
 		listaUsuarios = new LinkedList<Usuario>();
 	}
 
 	public Iterator<Usuario> getIterator() {
 		return listaUsuarios.iterator();
 	}
 
	public void añadirUsuario(Usuario usuario) {
 		listaUsuarios.add(usuario);
 		Collections.sort(listaUsuarios);
 	}
 	
 	public void eliminarUsuario(Usuario usuario) {
 		listaUsuarios.remove(usuario);
 	}
 
 	public boolean contains(Usuario usuario) {
 		return listaUsuarios.contains(usuario);
 	}
 
 	/*
 	 * Métodos del AbstractTableModel
 	 */
 	@Override
 	public String getColumnName(int col) {
 		switch (col) {
 		case 0:
 			return "Usuarios conectados";
 		case 1:
 			return "IP";
 		case 2:
 			return "Port";
 		default:
 			return "UUID";
 		}
 	}
 
 	@Override
 	public int getColumnCount() {
 		return 4;
 	}
 
 	@Override
 	public int getRowCount() {
 		return listaUsuarios.size();
 	}
 
 	@Override
 	public Object getValueAt(int rowIndex, int columnIndex) {
 		switch (columnIndex) {
 		case 0:
 			return listaUsuarios.get(rowIndex).nickName;
 		case 1:
 			return listaUsuarios.get(rowIndex).ip;
 		case 2:
 			return listaUsuarios.get(rowIndex).port;
 		default:
 			return listaUsuarios.get(rowIndex).uuid;
 		}
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj instanceof ListaUsuarios)
 			return listaUsuarios.equals(((ListaUsuarios) obj).listaUsuarios);
 		else
 			return false;
 	}
 
 	@Override
 	public String toString() {
 		return listaUsuarios.toString();
 	}
 
 	public int getLenght() {
 		return listaUsuarios.size();
 	}
 
 	@SuppressWarnings("unchecked")
 	public LinkedList<Usuario> diferenciaUsuarios(
 			LinkedList<Usuario> pListaUsuarios) {
 		LinkedList<Usuario> lista = (LinkedList<Usuario>) listaUsuarios.clone();
 		lista.removeAll(pListaUsuarios);
 		return lista;
 	}
 
 }
