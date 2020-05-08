 package com.prisila.modelo.entidade;
 
import java.io.Serializable;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 
 @Entity
public class Usuario implements Serializable{
 	
	private static final long serialVersionUID = 1L;
 	@Id
 	@GeneratedValue
 	private Long id;
 	private String login;
 	private String senha;
 	private String nome;
 	
 	public Long getId() {
 		return id;
 	}
 	
 	public void setId(Long id) {
 		this.id = id;
 	}
 	
 	public String getLogin() {
 		return login;
 	}
 	
 	public void setLogin(String login) {
 		this.login = login;
 	}
 	
 	public String getSenha() {
 		return senha;
 	}
 	
 	public void setSenha(String senha) {
 		this.senha = senha;
 	}
 	
 	public String getNome() {
 		return nome;
 	}
 	
 	public void setNome(String nome) {
 		this.nome = nome;
 	}
 	
 	public String getSenhaEncriptada() {
 		byte[] bytes;
 		try {
 			bytes = getSenha().getBytes();
 			MessageDigest md = MessageDigest.getInstance("MD5");
 			BigInteger md5 = new BigInteger(1, md.digest(bytes));
 			return md5.toString(16);
 		} catch (Exception e) {
 			System.err.println("Erro no MD5 da senha");
 		}
 		return null;
 	}
 	
 }
