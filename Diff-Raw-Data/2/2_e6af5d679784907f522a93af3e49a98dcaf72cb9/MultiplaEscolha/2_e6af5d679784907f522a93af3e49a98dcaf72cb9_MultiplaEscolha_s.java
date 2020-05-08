 package br.com.caelum.parsac.modelo;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import br.com.caelum.parsac.util.AlternativaCorretaConverter;
 
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 import com.thoughtworks.xstream.annotations.XStreamConverter;
 
 @XStreamAlias("exercicio-multiplaEscolha")
 public class MultiplaEscolha extends Exercicio {
 
 	private List<Alternativa> alternativas = new ArrayList<Alternativa>();
 
 	@XStreamConverter(AlternativaCorretaConverter.class)
	private Alternativa resposta;
 
 	public MultiplaEscolha(String enunciado, List<Alternativa> alternativas,
 			Alternativa resposta) {
 		super(enunciado, resposta);
 		this.alternativas = alternativas;
 		this.resposta = resposta;
 	}
 
 	public MultiplaEscolha() {
 	}
 
 	public List<Alternativa> getAlternativas() {
 		return alternativas;
 	}
 
 	public String getResposta() {
 		return this.resposta.getTexto();
 	}
 
 	public void setEnunciado(String enunciado) {
 		this.enunciado = enunciado;
 	}
 	
 	public void setResposta(String resposta) {
 		this.resposta.setTexto(resposta);
 	}
 	
 	public String toString() {
 		return this.enunciado + "\n\n" + this.alternativas + "\nResposta: "
 				+ this.resposta;
 	}
 
 	
 
 }
