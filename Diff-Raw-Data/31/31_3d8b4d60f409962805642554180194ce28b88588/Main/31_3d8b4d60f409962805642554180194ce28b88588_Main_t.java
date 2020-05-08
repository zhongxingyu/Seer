 package teste;
 
 import java.util.Date;
 import java.util.List;
 
 import dao.PacienteDAO;
 
 import model.Ala;
 import model.Leito;
 import model.Paciente;
 
 public class Main {
 	public static void main(String[] args) {
 		/*Paciente p = new Paciente();
 		p.setCpf("99999999999");
 		p.setDataNascimento(new Date("12/12/1956"));
 		p.setEnderecoBairro("Luzia");
 		p.setEnderecoCEP("49000000");
 		p.setEnderecoCidade("Aracaju");
 		p.setEnderecoComplemento("Atrs do Posto Ipiranga");
 		p.setEnderecoEstado("Sergipe");
 		p.setEnderecoNumero(100);
 		p.setEnderecoRua("Hermes Fontes");
 		p.setNome("Joo dos Santos");
 		p.setNomeMae("Maria dos Santos");
 		p.setNumeroSus("999999999999999");
 		p.setNumeroTelefone("7932222222");
 		
 		PacienteDAO pdao = new PacienteDAO();
 		pdao.insert(p);*/
 		
 //		Paciente p = new Paciente();
 //		p.setNumeroSus("333333333333333");		
 //		p = p.localizarPaciente();
 //		System.out.println(p.toString());
 		
 //		Ala a = new Ala();
 //		List<Ala> lal = a.getAlas();
 //		for (int i=0; i<lal.size(); i++) {
 //			System.out.println(lal.get(i).getNomeAla());
 //		}
 		
 //		Leito l = new Leito();
 //		l.setCodigoAla(1);
//		l.setCodigoPatrimonio(1);
 //		l.cadastrarLeito();
 		
		Leito l = new Leito();
		l.setCodigoStatusLeito(2);
		l.setCodigoLeito(1);
		l.liberarLeito();
 		
 //		Leito l = new Leito();
 //		List<Leito> lle = l.getLeitos(1);
 //		for(int i=0; i<lle.size(); i++) {
 //			System.out.println(lle.get(i).toString());
 //		}
 		
//		Leito l = new Leito();
//		List<Leito> lle = l.getLeitosLivres();
//		for(int i=0; i<lle.size(); i++) {
//			System.out.println(lle.get(i).toString());
//		}
 	}
 	
 
 }
