 package br.com.uni.modelo.persistencia;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import br.com.uni.modelo.Bookmark;
 
 public class BookmarkDAO {
 
 	private static List<Bookmark> LISTA = new ArrayList<Bookmark>();
 
 	public void salvar(Bookmark bookmark) {
		bookmark.setId(Bookmark.incrementId());
 		LISTA.add(bookmark);
 	}
 
 	public List<Bookmark> listar() {
 		return LISTA;
 	}
 
 	public Bookmark getByID(int id) {
 		for (Bookmark book : LISTA) {
 			if (book.getId() == id) {
 				return book;
 			}
 		}
 		return new Bookmark();
 	}
 
 	public void editar(Bookmark bookmark) {
 		ArrayList<Bookmark> _lista = new ArrayList<Bookmark>();
 		for (Bookmark book : LISTA) {
 			if (book.getId() == bookmark.getId()) {
 				_lista.add(bookmark);
 			} else
 				_lista.add(book);
 		}
 		LISTA.clear();
 		LISTA.addAll(_lista);
 
 	}
 
 	public void excluir(int id) {
 		ArrayList<Bookmark> _lista = new ArrayList<Bookmark>();
 		for (Bookmark book : LISTA) {
 			if (book.getId() == id) {
 				continue;
 			} else
 				_lista.add(book);
 		}
 		LISTA.clear();
 		LISTA.addAll(_lista);
 	}
 
 }
