 package it.d0ge01.scuole;
 
 import java.util.LinkedList;
 
 public class Scuola {
 	protected String name = "";
 	private LinkedList sch;
 	
 	public Scuola(String name) {
 		this.name = name;
 		this.sch = new LinkedList();
 	}
 	
 	public void addClass(String nome) {
 		this.sch.add(new Classe(nome));
 	}
 	
 	public LinkedList list() {
 		return this.sch;
 	}
 	
 	
 	public Studente searchStudent(String nome) {
 		Classe buff;
 		LinkedList buff2;
 		Studente buff3;
 		for ( int i = 0 ; i < this.sch.size() ; i++ ) {
 			buff = (Classe) this.sch.get(i);
 			buff2 = buff.list();
 			for ( int j = 0 ; j < buff2.size() ; j++ ) {
 				buff3 = (Studente) buff2.get(j);
				if ( buff3.name == nome )
 					return buff3;
 			}
 		}
 		return null;
 	}
 	
 	public String toStamp() {
 		String ret = "";
 		Classe buff;
 		for ( int i = 0 ; i < this.sch.size() ; i++ ) {
 			buff = (Classe) this.sch.get(i);
 			ret += "Classe " + buff.name + ":\n";
 			ret += buff.toStamp();
 		}
 		return ret;
 	}
 }
