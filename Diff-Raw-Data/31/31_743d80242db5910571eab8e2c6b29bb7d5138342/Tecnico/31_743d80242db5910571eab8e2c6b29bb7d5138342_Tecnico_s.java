 // bonus salarial
 public class Tecnico extends Assistente {
 	float bonusSalarial;
 
 	public Tecnico(String nome, float salario, int matricula, float bonusSalarial) {
 		//java:
 		//super(nome, salario, matricula);
 
 		this.nome = nome;
 		this.salario = salario;
 		this.matricula = matricula;
 		//-----------------------------------------------
 
 		this.bonusSalarial = bonusSalarial;
 	}
 
 	public void exibeDados() {
		//java:
		//super.exibeDados();

		System.out.println("======== DADOS ========");
		System.out.println("Nome: " + nome);
		System.out.println("Salario: " + salario);

		System.out.println("Matricula: " + matricula);
		//-----------------------------------------------
 
 		float resultante = salario + bonusSalarial;
 		System.out.println("Salario + bonus: " + resultante);//(salario + bonusSalarial));
 	}
 }
