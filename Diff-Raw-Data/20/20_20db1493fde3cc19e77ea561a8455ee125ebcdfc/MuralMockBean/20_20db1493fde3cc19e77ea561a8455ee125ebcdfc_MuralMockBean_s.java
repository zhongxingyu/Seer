 package net.peboleiros.mockbean;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 
 import net.peboleiros.mockentitys.MensagemMural;
 import net.peboleiros.mockentitys.Partida;
 
 import org.primefaces.model.chart.CartesianChartModel;
 import org.primefaces.model.chart.ChartSeries;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Controller;
 
 @Controller("muralMockBean")
 @Scope("session")
 public class MuralMockBean implements Serializable {
 
 	private static final long serialVersionUID = -6642734447578687952L;
 
 	public MuralMockBean(){
 		
 		
 		MensagemMural mensagemUm = new MensagemMural(1, "Cara o Gildo é muito ruim!", "Paulinho","paulo@gmail.com", new GregorianCalendar(2012,GregorianCalendar.DECEMBER,2,15,22,14));
 		MensagemMural mensagemDois = new MensagemMural(2, "queria ver se alguém é melhor que eu!", "Fenomeno","chicao@gmail.com", new GregorianCalendar(2012,GregorianCalendar.DECEMBER,3,9,20,11));
 		MensagemMural mensagemTres = new MensagemMural(3, "visite meu álbum bit.ly/a2sa2", "Pepe","pepe@gmail.com", new GregorianCalendar(2012,GregorianCalendar.NOVEMBER,21,11,22,13));
 		MensagemMural mensagemQuatro = new MensagemMural(4, "muito legal! convidarei meus amigos", "Rapha","r10@gmail.com", new GregorianCalendar(2012,GregorianCalendar.OCTOBER,4,15,22,14));
 		MensagemMural mensagemCinco = new MensagemMural(5, "enviando um abraço pra galera de Mapinguari", "Roger","caxias@hotmail.com", new GregorianCalendar(2012,GregorianCalendar.NOVEMBER,9,15,22,14));
 		MensagemMural mensagemSeis = new MensagemMural(6, "é noixx!", "Joca Rox","joqua@hotmail.com", new GregorianCalendar(2012,GregorianCalendar.DECEMBER,02,15,22,14));
 		MensagemMural mensagemSete = new MensagemMural(7, "vamos vamos!", "Cobra Cega","jonatas@gmail.com", new GregorianCalendar(2012,GregorianCalendar.DECEMBER,02,15,22,14));
 		MensagemMural mensagemOito = new MensagemMural(8, "ei cara, me diz uma coisa...", "John","john.paul@uol.com", new GregorianCalendar(2012,GregorianCalendar.DECEMBER,02,15,22,14));
 		MensagemMural mensagemNove = new MensagemMural(9, "não véio, na real você...", "Gordito","eder@gmail.com", new GregorianCalendar(2012,GregorianCalendar.DECEMBER,02,15,22,14));
 		MensagemMural mensagemDez = new MensagemMural(10, "fala galerinha, vcs estão sendos testados", "Alvaro","alvaro@gmail.com", new GregorianCalendar(2012,GregorianCalendar.DECEMBER,02,15,22,14));
 		
 		
 		recadosMural.add(mensagemUm);
 		recadosMural.add(mensagemDois);
 		recadosMural.add(mensagemTres);
 		recadosMural.add(mensagemQuatro);
 		recadosMural.add(mensagemCinco);
 		recadosMural.add(mensagemSeis);
 		recadosMural.add(mensagemSete);
 		recadosMural.add(mensagemOito);
 		recadosMural.add(mensagemNove);
 		recadosMural.add(mensagemDez);
 		
 		timesResult = new CartesianChartModel();
 		
 		ChartSeries results = new ChartSeries();
 		
 		results.setLabel("Resultado");
 		results.set("Fluminense", 52);
 		results.set("Atlético-MG", 52);
 		results.set("São Paulo", 52);
 		results.set("Corinthians", 52);
 		
 		partidas.add(new Partida("Japiim x Alvorada", "2 x 1", "Planeta Bola"));		
 		partidas.add(new Partida("União x Pebóides", "1 x 3", "3B Soccer"));
 		partidas.add(new Partida("Barça x Chelsear", "3 x 3", "Planet Soccer"));
 		partidas.add(new Partida("OVNI x Capinzal", "2 x 0", "Planeta Bola"));
 		
 		
 	}
 	
 	private List<MensagemMural> recadosMural = new ArrayList<MensagemMural>(10);
 	private List<Partida> partidas = new ArrayList<Partida>(10);
 	private CartesianChartModel timesResult; 
 	private Partida partidaSelecionada;
 	
 	
 	public void confirmarConvite(ActionEvent actionEvent){  
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Sua presença foi confirmada na partida!",  null);  
         FacesContext.getCurrentInstance().addMessage(null, message);  
     } 
 	
 	public Partida getPartidaSelecionada() {
 		return partidaSelecionada;
 	}
 
 	public void setPartidaSelecionada(Partida partidaSelecionada) {
 		this.partidaSelecionada = partidaSelecionada;
 	}
 
 	public CartesianChartModel getTimesResult() {
 		return timesResult;
 	}
 
 	public void setTimesResult(CartesianChartModel timesResult) {
 		this.timesResult = timesResult;
 	}
 
 	public List<MensagemMural> getRecadosMural() {
 		return recadosMural;
 	}
 
 	public void setRecadosMural(List<MensagemMural> recadosMural) {
 		this.recadosMural = recadosMural;
 	}
 
 	public List<Partida> getPartidas() {
 		return partidas;
 	}
 
 	public void setPartidas(List<Partida> partidas) {
 		this.partidas = partidas;
 	}
 	
 	
 
 }
