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
		super.exibeDados();
 
 		float resultante = salario + bonusSalarial;
 		System.out.println("Salario + bonus: " + resultante);//(salario + bonusSalarial));
 	}
 }
