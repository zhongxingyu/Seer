 package br.go.gov.saude.teste;
 
 import java.io.ObjectInputStream.GetField;
 
 /**
  * Classe de teste.
  * 
  * @author Rodrigo Freitas Melo
 *
  */
 public class Teste {
	
	String texto = "";
	
 	public static void main(String[] args) {
		String txt = "hello world!";
		System.out.println(txt);
 	}
 
 	public String getTexto() {
 		return texto;
 	}
 
 	public void setTexto(String texto) {
 		this.texto = texto;
 	}
 
 }
