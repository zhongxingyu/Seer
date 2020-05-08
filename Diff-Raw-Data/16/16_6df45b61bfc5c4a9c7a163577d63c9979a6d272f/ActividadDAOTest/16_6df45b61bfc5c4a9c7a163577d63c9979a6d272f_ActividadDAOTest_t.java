 package com.odea;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.odea.dao.ActividadDAO;
 import com.odea.dao.EntradaDAO;
 import com.odea.domain.Actividad;
 import com.odea.domain.Usuario;
 
 public class ActividadDAOTest extends AbstractTestCase {
 	
 	@Autowired
 	private ActividadDAO dao;
 	
 	@Autowired
 	EntradaDAO daoe;
 	
 	public Actividad actividad = new Actividad(1,"Actividad 1");
 	public List<Integer> lista;
 	
 	public void setUp(){
 		super.setUp();
 		
 		lista = new List<Integer>() {
 
 			@Override
 			public boolean add(Integer e) {
 				// TODO Auto-generated method stub
 				return false;
 			}
 
 			@Override
 			public void add(int index, Integer element) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public boolean addAll(Collection<? extends Integer> c) {
 				// TODO Auto-generated method stub
 				return false;
 			}
 
 			@Override
 			public boolean addAll(int index, Collection<? extends Integer> c) {
 				// TODO Auto-generated method stub
 				return false;
 			}
 
 			@Override
 			public void clear() {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public boolean contains(Object o) {
 				// TODO Auto-generated method stub
 				return false;
 			}
 
 			@Override
 			public boolean containsAll(Collection<?> c) {
 				// TODO Auto-generated method stub
 				return false;
 			}
 
 			@Override
 			public Integer get(int index) {
 				// TODO Auto-generated method stub
 				return null;
 			}
 
 			@Override
 			public int indexOf(Object o) {
 				// TODO Auto-generated method stub
 				return 0;
 			}
 
 			@Override
 			public boolean isEmpty() {
 				// TODO Auto-generated method stub
 				return false;
 			}
 
 			@Override
 			public Iterator<Integer> iterator() {
 				// TODO Auto-generated method stub
 				return null;
 			}
 
 			@Override
 			public int lastIndexOf(Object o) {
 				// TODO Auto-generated method stub
 				return 0;
 			}
 
 			@Override
 			public ListIterator<Integer> listIterator() {
 				// TODO Auto-generated method stub
 				return null;
 			}
 
 			@Override
 			public ListIterator<Integer> listIterator(int index) {
 				// TODO Auto-generated method stub
 				return null;
 			}
 
 			@Override
 			public boolean remove(Object o) {
 				// TODO Auto-generated method stub
 				return false;
 			}
 
 			@Override
 			public Integer remove(int index) {
 				// TODO Auto-generated method stub
 				return null;
 			}
 
 			@Override
 			public boolean removeAll(Collection<?> c) {
 				// TODO Auto-generated method stub
 				return false;
 			}
 
 			@Override
 			public boolean retainAll(Collection<?> c) {
 				// TODO Auto-generated method stub
 				return false;
 			}
 
 			@Override
 			public Integer set(int index, Integer element) {
 				// TODO Auto-generated method stub
 				return null;
 			}
 
 			@Override
 			public int size() {
 				// TODO Auto-generated method stub
 				return 0;
 			}
 
 			@Override
 			public List<Integer> subList(int fromIndex, int toIndex) {
 				// TODO Auto-generated method stub
 				return null;
 			}
 
 			@Override
 			public Object[] toArray() {
 				// TODO Auto-generated method stub
 				return null;
 			}
 
 			@Override
 			public <T> T[] toArray(T[] a) {
 				// TODO Auto-generated method stub
 				return null;
 			}
 		};
 		lista.clear();
 		lista.add(1);
 		lista.add(3);
 		lista.add(5);
 		lista.add(8);
 	}
 	
 	@Test
 	public void insertarTest(){
 		System.out.println("hola");
 		Actividad act = new Actividad(199,"Actividad 1");
 		System.out.println(actividad.getIdActividad());
 		dao.insertarActividad(act);
 	}
 	
 	@Test
 	public void relacionarTest (){
		//dao.relacionarActividad(actividad,lista);
		//TODO:arreglar test
 	}
 	
 	@Test
 	public void borrarTest(){
 		dao.borrarActividad(actividad);
 	}
 	
 	@Test
 	public void getTotalHoras(){
 		Usuario user= new Usuario(57,"invitado","invitado");
 		int total = daoe.getHorasSemanales(user);
 		Assert.assertFalse("mal calculado", total<1);
 	}
 }
