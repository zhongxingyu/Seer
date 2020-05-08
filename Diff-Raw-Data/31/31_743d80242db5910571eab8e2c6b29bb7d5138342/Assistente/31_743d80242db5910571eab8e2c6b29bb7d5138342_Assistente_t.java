 public class Assistente extends Funcionario {
 	int matricula;
 
 	public Assistente(String nome, float salario, int matricula) {
 		this.nome = nome;
 		this.salario = salario;
 		this.matricula = matricula;
 	}
 
 	public int getMatricula() {
 		return matricula;
 	}
 
 	public void exibeDados() {
		super.exibeDados();
 
 		System.out.println("Matricula: " + matricula);
 	}
 }
