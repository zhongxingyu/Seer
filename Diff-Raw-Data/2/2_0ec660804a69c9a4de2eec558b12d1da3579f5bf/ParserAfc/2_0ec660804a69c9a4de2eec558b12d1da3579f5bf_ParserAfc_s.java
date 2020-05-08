 package br.com.caelum.parsac.parser;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 import java.util.regex.Pattern;
 
 import br.com.caelum.parsac.modelo.Alternativa;
 import br.com.caelum.parsac.modelo.Curso;
 import br.com.caelum.parsac.modelo.Exercicio;
 import br.com.caelum.parsac.modelo.ExercicioMultiplaEscolha;
 import br.com.caelum.parsac.modelo.Secao;
 
 public class ParserAfc {
 
 	private List<String> linguagens = new ArrayList<String>();
 	private List<String> links = new ArrayList<String>();
 	private List<String> respostas = new ArrayList<String>();
 
 	public ParserAfc() {
 		linguagens.add("java");
 		linguagens.add("xml");
 		linguagens.add("html");
 		linguagens.add("ruby");
 		linguagens.add("js");
 		linguagens.add("sql");
 		linguagens.add("css");
 		linguagens.add("bash");
 		linguagens.add("c#");
 	}
 
 	public String parseiaTagsOnline(String string) throws IOException {
 
 		for (String linguagem : linguagens) {
 			string = string.replaceAll(Pattern.quote("[") + "(" + linguagem
 					+ ")" + Pattern.quote("]"), "[code " + linguagem + "]");
 			string = string.replaceAll(Pattern.quote("[/") + "(" + linguagem
 					+ ")" + Pattern.quote("]"), "[/code]");
 		}
 
 		string = string.replaceAll("class=\"linked-list\"", "");
 
 		string = string.replaceAll("<[ ]*(b|strong|u)[ ]*>", "**");
 		string = string.replaceAll("<[ ]*/[ ]*(b|strong|u)[ ]*>", "**");
 
 		string = string.replaceAll("<[ ]*(em|i)[ ]*>", "::");
 		string = string.replaceAll("<[ ]*/[ ]*(em|i)[ ]*>", "::");
 
 		string = string.replaceAll("<[ ]*code[ ]*>", "%%");
 		string = string.replaceAll("<[ ]*/[ ]*code[ ]*>", "%%");
 
 		string = string.replaceAll("<[ ]*h1[ ]*>", "[section ");
 		string = string.replaceAll("<[ ]*/[ ]*h1[ ]*>", "]");
 		
 		string = string.replaceAll("<[ ]*h[0-9]*[ ]*>", "[title ");
 		string = string.replaceAll("<[ ]*/[ ]*h[0-9]*[ ]*>", "]");
 
 		string = string.replaceAll("<[ ]*(ul|ol)[ ]*>", "[list]");
 		string = string.replaceAll("<[ ]*/[ ]*(ul|ol)[ ]*>", "[/list]");
 
 		string = string.replaceAll("<[ ]*li[ ]*>", "*");
 		string = string.replaceAll("<[ ]*/[ ]*li[ ]*>", "");
 
 		string = string.replaceAll("<[ ]*hr[ ]*>", "");
 		string = string.replaceAll("<[ ]*hr[ ]*/[ ]*>", "");
 
 		string = string.replaceAll("<[ ]*a[ ]*href[ ]*=[ ]*\"", "");
 		string = string.replaceAll("\"[ ]*>[a-z A-Z_0-9]*<[ ]*/[ ]*a[ ]*>", "");
 		string = string.replaceAll("<[ ]*/[ ]*a[ ]*>", "");
 
 		string = string.replaceAll("<[ ]*img ", "[img images/");
 		string = string.replaceAll("src=\"http(s)*://[a-z A-Z_0-9./-]*/", "");
		// string = string.replaceAll("\"(|/| | /)>", " w=50]");
 		string = string.replaceAll("width=\"[0-9]*%\"", "");
 		string = string.replaceAll("<[ ]*img ", "");
 
 
 		return string;
 
 	}
 
 	public List<String> pegaLinksDasImagens(String string) {
 		Scanner scanner = new Scanner(string);
 
 		while (scanner.hasNext()) {
 			String token = scanner.next();
 
 			if (token.matches("src=\"http(s)*://[a-z A-Z_0-9./-]*\"((|/)>)*")) {
 				String link = (token.split("\"", 3)[1]);
 				links.add(link);
 			}
 		}
 
 		scanner.close();
 
 		return links;
 	}
 
 	public String parseiaCurso(Curso curso, int numeroDaSecao)
 			throws IOException {
 
 		Secao secao = curso.getSecoes().get(numeroDaSecao);
 		String texto = "[chapter " + secao.getTitulo() + "]";
 
 		System.out.println(">>\tParseando tags da explicação...");
 
 		texto += "\n" + parseiaTagsOnline(secao.getExplicacao());
 
 		System.out.println(">>\tParseando exercicios...");
 
 		texto += "\n\n[section Exercicios]\n\n[exercise]";
 
 		for (Exercicio exercicio : secao.getExercicios().getExercicios()) {
 			texto += "\n\t[question]";
 
 			texto += parseiaTagsOnline(exercicio.getEnunciado());
 			if (exercicio instanceof ExercicioMultiplaEscolha) {
 				texto += "[list]";
 
 				for (Alternativa alternativa : exercicio.getAlternativas()) {
 					texto += "\n* " + parseiaTagsOnline(alternativa.getTexto());
 				}
 
 				texto += "[/list]";
 			}
 
 			texto += "\n[/question]";
 			respostas.add(parseiaTagsOnline(exercicio.getResposta()));
 		}
 
 		texto += "\n[/exercise]\n[note]\n**Respostas:**\n\n";
 
 		for (int i = 0; i < respostas.size(); i++) {
 			texto += i + 1 + ") " + respostas.get(i) + "\n\n";
 		}
 
 		texto += "[/note]";
 
 		return texto;
 	}
 
 }
