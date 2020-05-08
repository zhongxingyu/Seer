 package logica;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.jdom2.Element;
 
 import excecoes.DataInvalidaException;
 import excecoes.HoraInvalidaException;
 import excecoes.OrigemInvalidaException;
 import excecoes.VagaInvalidaException;
 import excecoes.XMLNaoGeradaException;
 
 import xml.FactoryXml;
 import xml.Xml;
 
 /**
  * Classe que representa uma carona no sistema.
  * 
  * @author ANTONIOR
  * 
  */
 
 public class Carona {
 
 	private String id;
 	private String origem;
 	private String destino;
 	private String data;
 	private String hora;
 	private Object vagas;
 	private int vagasTotal;
 	private Map<String, String> pontosSugeridos;
 	private String pontoDeEncontro;
 	private List<Solicitacao> solicitacoes;
 	private List<Solicitacao> caroneirosConfirmados;
 	private String criador;
 	private List<Review> reviews;
 	
 	private boolean isFinalizada;
 	
 	private Xml xmlCreator;
 
 	public Carona(String origem, String destino, String data, String hora,
 			Object vagas, String criador) throws Exception {
 
 		this.setOrigem(origem);
 		this.setDestino(destino);
 		this.setData(data);
 		this.setHora(hora);
 		this.setVagas(vagas);
 		this.setCriador(criador);
 		this.pontosSugeridos = new HashMap<String, String>();
 		this.solicitacoes = new ArrayList<Solicitacao>();
 		this.caroneirosConfirmados = new ArrayList<Solicitacao>();
 		this.reviews = new ArrayList<Review>();
 		GeradorDeID gerador = new GeradorDeID();
 		id = gerador.geraId();
 
 		setVagasTotal((Integer) vagas);
 		
 		this.setFinalizada(false);
 		
 		this.xmlCreator = new FactoryXml("Xml carona");
 	}
 
 	public List<Review> getReviews() {
 		return reviews;
 	}
 
 	public void setReviews(List<Review> reviews) {
 		this.reviews = reviews;
 	}
 
 	public Map<String, String> getPontosSugeridos() {
 		return pontosSugeridos;
 	}
 
 	public Collection<String> getPontosSugeridosValues() {
 		return pontosSugeridos.values();
 	}
 
 	public void setPontosSugeridos(Map<String, String> pontosSugeridos) {
 		this.pontosSugeridos = pontosSugeridos;
 	}
 
 	public List<Solicitacao> getCaroneiros() {
 		return caroneirosConfirmados;
 	}
 
 	public void setCaroneiros(List<Solicitacao> caroneiros) {
 		this.caroneirosConfirmados = caroneiros;
 	}
 
 	public void addCaroneiro(Solicitacao caroneiro) {
 		this.caroneirosConfirmados.add(caroneiro);
 	}
 
 	public void removeCaroneiro(Usuario caroneiro) {
 		this.caroneirosConfirmados.remove(caroneiro);
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	public void setPonto(String ponto) {
 		this.pontoDeEncontro = ponto;
 	}
 
 	public void setSolicitacoes(List<Solicitacao> solicitacoes) {
 		this.solicitacoes = solicitacoes;
 	}
 
 	private void setCriador(String criador) {
 		this.criador = criador;
 	}
 
 	public String getPontoDeEncontro() {
 		return pontoDeEncontro;
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public String getOrigem() {
 		return origem;
 	}
 
 	public List<Solicitacao> getSolicitacoes() {
 		return solicitacoes;
 	}
 
 	public String getCriador() {
 		return criador;
 	}
 
 	public void addSolicitacao(Solicitacao solicitacao) {
 		solicitacoes.add(solicitacao);
 	}
 
 	public void aceitaSolicitacao(Solicitacao solicitacao) {
 		solicitacoes.remove(solicitacao);
 		solicitacao.setEstado("Confirmada");
 		addSolicitacao(solicitacao);
 	}
 
 	public void setOrigem(String origem) throws Exception {
 		if (origem == null || origem.equals("") || contemCharInvalidos(origem)) {
 			throw new OrigemInvalidaException();
 		} else {
 			this.origem = origem;
 		}
 	}
 
 	public String getDestino() {
 		return destino;
 	}
 
 	public Map<String, String> getPontos() {
 		return pontosSugeridos;
 	}
 
 	public void setPontosSugeridos(String id, String pontos) {
 
 		pontosSugeridos.clear();
 		if (pontos != null && !pontos.equals("")) {
 			pontosSugeridos.put(id, pontos);
 		}
 
 	}
 
 	public void setDestino(String destino) throws Exception {
 		if (destino == null || destino.equals("")
 				|| contemCharInvalidos(destino)) {
 			throw new excecoes.DestinoInvalidoException();
 		} else {
 			this.destino = destino;
 		}
 	}
 
 	private static boolean contemCharInvalidos(String nome) {
 		String[] palavra = nome.trim().split("");
 
 		for (int i = 1; i < palavra.length; i++) {
 			if ("'!@#$%&*()+=[{]};:>,</|/0123456789*-"
 					.contains(palavra[i])) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	public String getData() {
 		return data;
 	}
 
 	public void setData(String data) throws Exception {
 		if (data == null || data.equals("") || !validaData(data)) {
 			throw new DataInvalidaException();
 		} else {
 			this.data = data;
 		}
 	}
 
 	@SuppressWarnings("deprecation")
 	private static boolean validaData(String data) throws Exception {
 		Date date = new Date();
 		int ano;
 		int mes;
 		int dia;
 		if (data.length() != 10) {
 			return false;
 		}
 		try {
 
 			dia = Integer.parseInt(data.substring(0, 2));
 			mes = Integer.parseInt(data.substring(3, 5));
 			ano = Integer.parseInt(data.substring(6));
 
 		} catch (Exception e) {
 			throw new DataInvalidaException();
 		}
 
 		// Verifica ano >= que atual
 		if (ano < date.getYear() + 1900) {
 			return false;
 		}
 
 		// Verifica mes entre 01 e 12
 		if (mes < 1 || mes > 12) {
 			return false;
 		}
 
 		// Verifica para meses com 31 dias
 		if ((mes == 1 || mes == 3 || mes == 5 || mes == 7 || mes == 8
 				|| mes == 10 || mes == 12)
 				&& (dia < 1 || dia > 31)) {
 			return false;
 
 		}
 
 		// Verifica para meses com 30 dias
 		if ((mes == 4 || mes == 6 || mes == 9 || mes == 11)
 				&& (dia < 1 || dia > 30)) {
 			return false;
 
 		}
 
 		// Verifica para fevereiro bissexto com 29 dias
 		if ((mes == 2) && (ano % 4 == 0) && (dia < 1 || dia > 29)) {
 			return false;
 		}
 
 		// Verifica para fevereiro no bissexto com 28 dias
 		if ((mes == 2) && (ano % 4 != 0) && (dia < 1 || dia > 28)) {
 			return false;
 		}
 
 		// Compara com a data atual
 		if (comparaDataAtual(dia, mes, ano) < 0) {
 			return false;
 		}
 
 		// Aceita a data
 		return true;
 
 	}
 
 	private static int comparaDataAtual(int dia, int mes, int ano) {
 		Date date = new Date();
 		if (ano == date.getYear() + 1900) {
 			if (mes == date.getMonth() + 1) {
 				if (dia == date.getDay() + 1) {
 					return 0;
 				} else {
 					if (dia > date.getDay() + 1) {
 						return 1;
 					} else {
 						return -1;
 					}
 				}
 			} else {
 				if (mes > date.getMonth() + 1) {
 					return 1;
 				} else {
 					return -1;
 				}
 			}
 		} else {
 			if (ano > date.getYear() + 1900) {
 				return 1;
 			} else {
 				return 0;
 			}
 		}
 	}
 
 	public String getHora() {
 		return this.hora;
 	}
 
 	public void setHora(String hora) throws Exception {
 		if (hora == null || hora.equals("") || !validaHora(hora)) {
 			throw new HoraInvalidaException();
 		} else {
 			this.hora = hora;
 		}
 	}
 
 	private boolean validaHora(String hora) {
 		if (hora.length() != 5) {
 			return false;
 		} else {
 			String[] caracteres = hora.split("");
 			for (int i = 1; i < caracteres.length; i++) {
 				if (i == 3) {
 					if (!caracteres[3].equals(":")) {
 						return false;
 					}
 				} else {
 					if (temInvalidoNosNumeros((caracteres[i]))) {
 						return false;
 					}
 				}
 			}
 
 		}
 		return true;
 
 	}
 
 	public int getVagas() {
 		int intVagas = (Integer) vagas;
 
 		return intVagas;
 	}
 
 	// verificar
 	public void setVagas(Object vagas) throws Exception {
 
 		if (vagas == null) {
 			throw new VagaInvalidaException();
 		}
 
 		int intVagas;
 
 		try {
 			intVagas = (Integer) vagas;
 
 		} catch (Exception e) {
 			throw new VagaInvalidaException();
 		}
 
 		String stringVagas = "" + vagas;
 
 		if (intVagas < 0 || stringVagas == null) {
 			throw new VagaInvalidaException();
 		}
 		this.vagas = intVagas;
 	}
 
 	public static boolean temInvalidoNosNumeros(String s) {
 		if (!"0123456789".contains(s)) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public void removeSolicitacao(Solicitacao solicitacao) {
 
 		solicitacoes.remove(solicitacao);
 
 	}
 
 	public void removeSugestao(String idSugestao) {
 		pontosSugeridos.remove(idSugestao);
 
 	}
 
 	@Override
 	public String toString() {
 
 		String carona = getOrigem() + " para " + getDestino() + ", no dia "
 				+ getData() + ", as " + getHora();
 
 		return carona;
 	}
 
 	public void addReview(Review review) {
		reviews.add(review);
 
 	}
 
 	public int getVagasTotal() {
 		return (Integer) vagasTotal;
 	}
 
 	public boolean isFinalizada() {
 		return isFinalizada;
 	}
 
 	public void setFinalizada(boolean isFinalizada) {
 		this.isFinalizada = isFinalizada;
 	}
 	
 	public void geraXml(){
 		this.xmlCreator.geraXML(this);
 	}
 	
 	public Element getXml() throws XMLNaoGeradaException{
 		return this.xmlCreator.getRaiz();
 	}
 	
 	public void setVagasTotal(int vagas){
 		vagasTotal = vagas;
 	}
 	
 	public void setCaroneirosConfirmados(List<Solicitacao> lista){
 		this.caroneirosConfirmados = lista;
 	}
 
 }
