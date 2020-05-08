 package br.com.puc.sispol.modelo;
 
 import java.util.Calendar;
 import java.util.List;
 
 import javax.validation.constraints.NotNull;
 
 import org.hibernate.validator.constraints.NotEmpty;
 import org.springframework.format.annotation.DateTimeFormat;
 
 public class Simulado {
 	private Long codSimulado;
 
 	@NotNull(message = "A Data deve ser preenchida.")
 	@DateTimeFormat(pattern = "dd/MM/YYYY")
 	private Calendar dataDeRealizacao;
 
 	@NotEmpty(message = "A Hora deve ser preenchida.")
 	private String horaDeRealizacao;
 
 	// @NotEmpty(message = "A Hora deve ser preenchida.")
 	@NotNull(message = "A Duração deve ser preenchida.")
 	private Integer duracao;
 
 	@NotNull(message = "A Pontuação deve ser preenchida.")
 	private Integer pontuacaoMinima;
 
	@NotNull(message = "O Título deve ser preenchida.")
	@NotEmpty(message = "O Título deve ser preenchida.")
 	private String titulo;
 
 	private List<Questao> questoes;
 	private List<AreaDeConhecimentoQuantidade> areasDeConhecimentoQuantidade;
 
 	public List<AreaDeConhecimentoQuantidade> getAreasDeConhecimentoQuantidade() {
 		return areasDeConhecimentoQuantidade;
 	}
 
 	public void setAreasDeConhecimentoQuantidade(
 			List<AreaDeConhecimentoQuantidade> areasDeConhecimentoQuantidade) {
 		this.areasDeConhecimentoQuantidade = areasDeConhecimentoQuantidade;
 	}
 
 	public List<Questao> getQuestoes() {
 		return questoes;
 	}
 
 	public void setQuestoes(List<Questao> questoes) {
 		this.questoes = questoes;
 	}
 
 	public Long getCodSimulado() {
 		return codSimulado;
 	}
 
 	public void setCodSimulado(Long codSimulado) {
 		this.codSimulado = codSimulado;
 	}
 
 	public String getHoraDeRealizacao() {
 		return horaDeRealizacao;
 	}
 
 	public void setHoraDeRealizacao(String horaDeRealizacao) {
 		this.horaDeRealizacao = horaDeRealizacao;
 	}
 
 	public Calendar getDataDeRealizacao() {
 		return dataDeRealizacao;
 	}
 
 	public void setDataDeRealizacao(Calendar dataDeRealizacao) {
 		this.dataDeRealizacao = dataDeRealizacao;
 	}
 
 	public Integer getDuracao() {
 		return duracao;
 	}
 
 	public void setDuracao(Integer duracao) {
 		this.duracao = duracao;
 	}
 
 	public Integer getPontuacaoMinima() {
 		return pontuacaoMinima;
 	}
 
 	public void setPontuacaoMinima(Integer pontuacaoMinima) {
 		this.pontuacaoMinima = pontuacaoMinima;
 	}
 
 	public String getTitulo() {
 		return titulo;
 	}
 
 	public void setTitulo(String titulo) {
 		this.titulo = titulo;
 	}
 
 }
