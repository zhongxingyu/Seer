 package poo.sca;
 
 public class Disciplina {
 	
 	private String nome;
 	private int codigo;
 	
 	public Disciplina(String nome, int codigo){
 		this.setNome(nome);
 		this.setCodigo(codigo);
 	}
 
 	public String getNome() {
 		return nome;
 	}
 
 	public void setNome(String nome) {
 		this.nome = nome;
 	}
 
 	public int getCodigo() {
 		return codigo;
 	}
 
 	public void setCodigo(int codigo) {
 		this.codigo = codigo;
 	}
 
 }
