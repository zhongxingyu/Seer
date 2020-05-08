 package br.com.uni.modelo;
 
 import java.io.Serializable;
 
 public class Bookmark  implements Serializable{
    
	 public static int COUNT = 1;
	 
 	 private int id;
 	 
 	 private String descricao;
 	 
 	 private String link;
 	 
 	 //construtor
 	 public Bookmark() {
 		 
 	 }
 
 	 public Bookmark(int id, String descricao, String link){
 		 this.descricao = descricao;
 		 this.link = link;
 		 this.id = id;
 	 }
 	 
 	 public Bookmark(String descricao, String link){
 		 this.descricao = descricao;
 		 this.link = link;
 	 }
 	 
 	 //gets
 	public String getDescricao() {
 		return descricao;
 	}
 
 	
 	public String getLink() {
 		return link;
 	}
 
 	public int getId(){
 		return id;
 	}
 	 
 	public void setId(int id){
 		this.id =  id;
 	}
 
 	public void setDescricao(String descricao) {
 		this.descricao = descricao;
 	}
 
 	public void setLink(String link) {
 		this.link = link;
 	}
 
	 public static int incrementId(){
		 Bookmark.COUNT = Bookmark.COUNT++;
		 return Bookmark.COUNT;
	 }
	 
 	
 }
