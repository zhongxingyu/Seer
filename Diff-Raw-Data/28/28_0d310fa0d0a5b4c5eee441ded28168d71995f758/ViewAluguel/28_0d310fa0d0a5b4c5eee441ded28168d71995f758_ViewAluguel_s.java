 package br.com.locadora.view;
 
 import java.text.NumberFormat;
 
 import br.com.locadora.model.Cliente;
 import br.com.locadora.model.DVD;
 
 
 public class ViewAluguel {
 
 	public static void mostraAluguelRealizado(Cliente cliente, DVD dvd, double saldo, String dataDevolucao) {
 		/*
 		DVD Alugado com sucesso:\n
 		<Cdigo do Cliente>-<Nome do Cliente>\n
 		<Cdigo do DVD>-<Nome do DVD>\n
 		Data de devoluo:<Data de Devoluo (formato DD/MM/YYYY)>\n
 		Saldo do Cliente: R$<Saldo do cliente>\n
 		DVDs disponveis: <Nmero deste DVD ainda disponveis>\n
 		*/		
 		
 		StringBuilder mensagem = new StringBuilder();
 		mensagem.append("DVD Alugado com sucesso:\n")
 				.append(String.format("%s-%s\n", cliente.getCodigo(), cliente.getNome()))
 				.append(String.format("%s-%s\n", dvd.getCodigo(), dvd.getTitulo()))
 				.append(String.format("Data de devoluo: %s\n", dataDevolucao))
 				.append(String.format("Saldo do Cliente: %s\n",NumberFormat.getCurrencyInstance().format(saldo)))
 				.append(String.format("DVDs disponveis: %d\n", dvd.getCopias()));
 		System.out.println(mensagem.toString());
 	}
 
 	public static void mostraAluguelDevolvido(Cliente cliente, DVD dvd,
 			double saldo, String dataAluguel, String dataDevolucao) {
 		/*
 		DVD Alugado com sucesso:\n
 		<Cdigo do Cliente>-<Nome do Cliente>\n
 		<Cdigo do DVD>-<Nome do DVD>\n
 		Data do aluguel:<Data do Aluguel (formato DD/MM/YYYY)>\n
 		Data de devoluo:<Data de Devoluo (formato DD/MM/YYYY)>\n
 		Saldo do Cliente: R$<Saldo do cliente>\n
 		DVDs disponveis: <Nmero deste DVD ainda disponveis>\n
 		*/		
 		
 		StringBuilder mensagem = new StringBuilder();
 		mensagem.append("DVD Devolvido com sucesso:\n")
 				.append(String.format("%s-%s\n", cliente.getCodigo(), cliente.getNome()))
 				.append(String.format("%s-%s\n", dvd.getCodigo(), dvd.getTitulo()))
 				.append(String.format("Data do aluguel: %s\n", dataAluguel))
 				.append(String.format("Data de devoluo: %s\n", dataDevolucao))
 				.append(String.format("Saldo do Cliente: %s\n",NumberFormat.getCurrencyInstance().format(saldo)))
 				.append(String.format("DVDs disponveis: %d\n", dvd.getCopias()));
 		System.out.println(mensagem.toString());
 		
 	}
 
 	public static void dadoNaoEncontrado() {
 		System.out.println("DVD ou Cliente no encontrado.");
 	}
 
 	public static void aluguelNaoEncontrado() {
 		System.out.println("Aluguel no existente.\n");
 	}
 
 	public static void copiasIndisponiveis(DVD dvd) {
 		System.out.println("No h cpias disponveis do DVD");
 	}
 
 }
