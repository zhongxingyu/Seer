 // turno e adicional noturno
 public class Administrativo extends Assistente {
 	//char turno; // D - dia, N - noite
 	int turno; // 1 - dia, 2 - noite
 	float adicionalNoturno;
 
 	//	public Administrativo(String nome, float salario, int matricula, char turno, float adicionalNoturno) {
 	public Administrativo(String nome, float salario, int matricula, int turno, float adicionalNoturno) {
 		//java:
 		//super(nome, salario, matricula);
 
 		this.nome = nome;
 		this.salario = salario;
 		this.matricula = matricula;
 		//-----------------------------------------------
 
 		this.turno = turno;
 		this.adicionalNoturno = adicionalNoturno;
 	}
 
 	public void exibeDados() {
		//java:
		//super.exibeDados();
 
 		System.out.println("======== DADOS ========");
 		System.out.println("Nome: " + nome);
 		System.out.println("Salario: " + salario);
 
 		System.out.println("Matricula: " + matricula);
 		//-----------------------------------------------
 
 		String turnoExtenso;
 		//java:
 		//float salarioTotal = 0.0f;
 		float salarioTotal = 0.0;
 
 		if(turno == 1) {//'D') {
 			turnoExtenso = "diurno";
 		}else {
 			salarioTotal = salario + adicionalNoturno;
 			turnoExtenso = "noturno";
 		}
 
 		System.out.println("Turno: " + turnoExtenso);
 
 		if(turno == 2)//'N')
 			System.out.println("Salario + adicional noturno: " + salarioTotal);
 	}
 }
